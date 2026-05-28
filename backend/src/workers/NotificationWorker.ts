import { AppDataSource } from "@config/database";
import { Schedule } from "@models/Schedule";
import { Reminder } from "@models/Reminder";
import { FCMToken } from "@models/FCMToken";
import { Notification } from "@models/Notification";
import { ScheduleAssignment } from "@models/ScheduleAssignment";
import notificationService from "@services/NotificationService";
import { logger } from "@utils/logger";
import { generateUUID } from "@utils/validation";

export class NotificationWorker {
  private timer: NodeJS.Timer | null = null;
  private isRunning = false;

  public start() {
    if (this.timer) return;
    logger.info("🕒 Starting NotificationWorker...");
    // Check every 60 seconds
    this.timer = setInterval(() => this.processReminders(), 60000);
    // Run immediately once
    this.processReminders();
  }

  public stop() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
      logger.info("⏹️ NotificationWorker stopped.");
    }
  }

  private async processReminders() {
    if (this.isRunning) return;
    this.isRunning = true;

    try {
      if (!AppDataSource || !AppDataSource.isInitialized) {
        this.isRunning = false;
        return;
      }

      const reminderRepository = AppDataSource.getRepository("Reminder");
      const scheduleRepository = AppDataSource.getRepository("Schedule");
      const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");
      const notificationRepository = AppDataSource.getRepository("Notification");
      const fcmTokenRepository = AppDataSource.getRepository("FCMToken");

      const now = new Date();
      // Add a slight buffer (e.g. 1 minute) to allow catching exact matches
      const threshold = new Date(now.getTime() + 60000); 

      // Find all reminders that haven't been triggered yet
      const pendingReminders = await reminderRepository
        .createQueryBuilder("reminder")
        .leftJoinAndSelect("reminder.schedule", "schedule")
        .where("reminder.last_triggered_at IS NULL")
        .andWhere("schedule.status != 'DONE'")
        .getMany();

      for (const reminder of pendingReminders) {
        const schedule = reminder.schedule;
        if (!schedule) continue;

        const baseTime = schedule.type === "EVENT" ? schedule.start_time : schedule.deadline;
        if (!baseTime) continue;

        const triggerTime = new Date(baseTime);
        let offsetMinutes = 0;

        switch (reminder.trigger_type) {
          case "MIN_5": offsetMinutes = 5; break;
          case "MIN_10": offsetMinutes = 10; break;
          case "MIN_30": offsetMinutes = 30; break;
          case "HOUR_1": offsetMinutes = 60; break;
          case "DAY_1": offsetMinutes = 1440; break;
          case "WEEK_1": offsetMinutes = 10080; break;
          case "CUSTOM":
            if (reminder.custom_time) {
               triggerTime.setTime(new Date(reminder.custom_time).getTime());
            }
            break;
          case "WHEN_STARTS":
          default:
            offsetMinutes = 0;
            break;
        }

        if (reminder.trigger_type !== "CUSTOM") {
          triggerTime.setMinutes(triggerTime.getMinutes() - offsetMinutes);
        }

        // If current time is past or equal to the trigger time
        if (now >= triggerTime) {
          // Identify recipients
          const recipients = new Set<string>();
          recipients.add(schedule.creator_id);

          const assignments = await assignmentRepository.find({ where: { schedule_id: schedule.id } });
          assignments.forEach(a => recipients.add(a.assignee_id));

          // Generate DB notifications & send pushes
          for (const userId of recipients) {
            // Create notification record
            const notification = notificationRepository.create({
              id: generateUUID(),
              user_id: userId,
              type: "SYSTEM",
              title: `Upcoming: ${schedule.title}`,
              message: `Your ${schedule.type.toLowerCase()} starts ${offsetMinutes > 0 ? 'in ' + offsetMinutes + ' minutes' : 'now'}.`,
              related_id: schedule.id,
              ia_read: false
            });
            await notificationRepository.save(notification);

            // Fetch FCM tokens and push
            const tokens = await fcmTokenRepository.find({ where: { user_id: userId } });
            for (const fcm of tokens) {
              await notificationService.sendPushNotification(
                fcm.token, 
                notification.title, 
                notification.message, 
                { schedule_id: schedule.id }
              );
            }
          }

          // Mark reminder as triggered
          reminder.last_triggered_at = now;
          await reminderRepository.save(reminder);
          logger.info(`🔔 Triggered reminder for schedule ${schedule.id}`);
        }
      }

    } catch (error) {
      logger.error("Error processing reminders in NotificationWorker", error);
    } finally {
      this.isRunning = false;
    }
  }
}

export default new NotificationWorker();
