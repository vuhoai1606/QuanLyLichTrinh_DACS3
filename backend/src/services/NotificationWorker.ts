import { AppDataSource } from "@config/database";
import { logger } from "@utils/logger";
import { LessThanOrEqual, IsNull } from "typeorm";
import emailService from "./EmailService";

export class NotificationWorker {
  private intervalId?: Timer;

  start() {
    // Run every 1 minute
    this.intervalId = setInterval(() => this.checkReminders(), 60000);
    logger.info("NotificationWorker started");
  }

  stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

private async checkReminders() {
    try {
      const scheduleRepository = AppDataSource.getRepository("Schedule");
      const notificationRepository = AppDataSource.getRepository("Notification");
      const now = new Date();
      // Add 5 minutes buffer
      const triggerTime = new Date(now.getTime() + 5 * 60000);

      // Only find schedules that are PENDING and haven't started yet
      const schedules = await scheduleRepository.find({
        where: {
          start_time: LessThanOrEqual(triggerTime),
          status: "PENDING"
        },
        relations: ["creator", "assignments", "assignments.assignee"],
      });

      for (const schedule of schedules) {
        // Prevent duplicate notifications
        const exist = await notificationRepository.findOne({
            where: { related_id: schedule.id, type: "SYSTEM" }
        });
        if (exist) continue;
      
        const title = "Task Starting Soon";
        const message = Task "" is starting soon!;

        if (schedule.creator) {
          const notification = notificationRepository.create({
            user_id: schedule.creator.id,
            type: "SYSTEM",
            title,
            message,
            related_id: schedule.id,
          });
          await notificationRepository.save(notification);
        }
        
        if (schedule.assignments) {
          for (const assignment of schedule.assignments) {
            const notification = notificationRepository.create({
              user_id: assignment.user_id,
              type: "SYSTEM",
              title,
              message,
              related_id: schedule.id,
            });
            await notificationRepository.save(notification);
          }
        }
        
        // Mark as doing so we don't spam
        await scheduleRepository.update(schedule.id, { status: "DOING" });
      }
    } catch (error) {
      logger.error("Error in NotificationWorker", error);
    }
  }
}

export default new NotificationWorker();
