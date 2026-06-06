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
      const now = new Date();
      // Add 5 minutes buffer
      const triggerTime = new Date(now.getTime() + 5 * 60000);

      const schedules = await scheduleRepository.find({
        where: {
          start_time: LessThanOrEqual(triggerTime),
        },
        relations: ["creator", "assignments", "assignments.assignee"],
      });

      for (const schedule of schedules) {
        // Mock notification logic
        // In a real app, we would check if a notification was already sent
        // For now, we will just log it
        if (schedule.creator) {
          logger.info([ALARM/NOTIFICATION] Task "" is starting soon! Notifying creator );
        }
        
        if (schedule.assignments) {
          for (const assignment of schedule.assignments) {
            logger.info([ALARM/NOTIFICATION] Task "" is starting soon! Notifying assignee );
          }
        }
      }
    } catch (error) {
      logger.error("Error in NotificationWorker", error);
    }
  }
}

export default new NotificationWorker();
