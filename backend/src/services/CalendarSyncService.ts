import { logger } from "@utils/logger";
import { AppDataSource } from "@config/database";
import { Schedule } from "@models/Schedule";
import { generateUUID } from "@utils/validation";

export class CalendarSyncService {
  /**
   * Syncs external calendar events (e.g. from Google Calendar / Apple Calendar)
   * This is a generalized import function that takes normalized event data.
   */
  async importExternalEvents(userId: string, events: any[]) {
    try {
      logger.info(`Syncing ${events.length} external calendar events for user ${userId}`);
      const scheduleRepo = AppDataSource.getRepository("Schedule");
      
      const newSchedules = [];
      let syncedCount = 0;

      for (const event of events) {
        let existing = null;
        if (event.external_id && event.external_source) {
          existing = await scheduleRepo.findOne({
            where: {
              creator_id: userId,
              external_id: event.external_id,
              external_source: event.external_source
            }
          });
        }
        
        if (!existing) {
          // Fallback to title and time deduplication if no external_id
          existing = await scheduleRepo.findOne({
            where: {
              creator_id: userId,
              title: event.title,
              start_time: new Date(event.start_time)
            }
          });
        }

        if (!existing) {
          const schedule = scheduleRepo.create({
            id: generateUUID(),
            creator_id: userId,
            title: event.title,
            description: event.description || "Imported from external calendar",
            location: event.location || null,
            type: event.type || "EVENT",
            priority: "MEDIUM",
            start_time: new Date(event.start_time),
            end_time: event.end_time ? new Date(event.end_time) : new Date(event.start_time),
            is_all_day: event.is_all_day || false,
            status: "PENDING",
            external_id: event.external_id || null,
            external_source: event.external_source || null,
            created_at: new Date()
          });
          newSchedules.push(schedule);
          syncedCount++;
        } else {
          // Update existing with new data from Google if it has external_id
          if (event.external_id && event.external_source) {
            const googleUpdated = event.updated_at ? new Date(event.updated_at).getTime() : 0;
            const bfyUpdated = existing.updated_at ? new Date(existing.updated_at).getTime() : 0;

            if (googleUpdated > bfyUpdated) {
              existing.title = event.title;
              existing.description = event.description || existing.description;
              existing.location = event.location || existing.location;
              existing.start_time = new Date(event.start_time);
              existing.end_time = event.end_time ? new Date(event.end_time) : new Date(event.start_time);
              existing.is_all_day = event.is_all_day || false;
              existing.type = event.type || existing.type;
              existing.external_id = event.external_id;
              existing.external_source = event.external_source;
              newSchedules.push(existing);
              syncedCount++;
            }
          }
        }
      }

      if (newSchedules.length > 0) {
        await scheduleRepo.save(newSchedules);
      }

      return { success: true, syncedCount, message: `Successfully synced ${syncedCount} external events.` };
    } catch (error) {
      logger.error("External Calendar Sync failed", error);
      throw error;
    }
  }
}

export default new CalendarSyncService();
