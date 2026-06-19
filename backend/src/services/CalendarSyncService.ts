import { logger } from "@utils/logger";
import { AppDataSource } from "@config/database";
import { Schedule } from "@models/Schedule";
import { User } from "@models/User";
import { generateUUID } from "@utils/validation";
import { IsNull } from "typeorm";
import { google } from "googleapis";
import { config } from "@config/env";
import { AppError } from "@utils/errors";

export class CalendarSyncService {
  /**
   * Save Google OAuth tokens for a user
   */
  async saveGoogleTokens(userId: string, tokens: any) {
    const userRepo = AppDataSource.getRepository("User");
    const user = await userRepo.findOne({ where: { id: userId } });
    
    if (!user) {
      throw new AppError(404, "User not found", "USER_NOT_FOUND");
    }

    if (tokens.access_token) user.google_access_token = tokens.access_token;
    if (tokens.refresh_token) user.google_refresh_token = tokens.refresh_token;

    await userRepo.save(user);
    logger.info(`Saved Google tokens for user ${userId}`);
    return true;
  }

  /**
   * Initialize Google OAuth2 Client for a specific user
   */
  private async getGoogleClient(userId: string) {
    const userRepo = AppDataSource.getRepository("User");
    const user = await userRepo.findOne({ where: { id: userId } });

    if (!user || !user.google_refresh_token) {
      throw new AppError(401, "Google Calendar not connected or missing refresh token", "G_CAL_NOT_CONNECTED");
    }

    const oauth2Client = new google.auth.OAuth2(
      config.google.clientId,
      config.google.clientSecret,
      config.google.redirectUri
    );

    oauth2Client.setCredentials({
      access_token: user.google_access_token,
      refresh_token: user.google_refresh_token,
    });

    // Event listener to automatically save new access tokens if they are refreshed
    oauth2Client.on('tokens', async (tokens) => {
      if (tokens.access_token) {
        user.google_access_token = tokens.access_token;
        if (tokens.refresh_token) {
          user.google_refresh_token = tokens.refresh_token;
        }
        await userRepo.save(user);
      }
    });

    return google.calendar({ version: 'v3', auth: oauth2Client });
  }

  /**
   * Bi-directional sync with Google Calendar
   */
  async syncWithGoogleCalendar(userId: string) {
    try {
      logger.info(`Starting Google Calendar sync for user ${userId}`);
      const calendar = await this.getGoogleClient(userId);
      const scheduleRepo = AppDataSource.getRepository("Schedule");

      // 1. Pull from Google Calendar (events from the last 30 days to 6 months future)
      const timeMin = new Date();
      timeMin.setDate(timeMin.getDate() - 30);
      const timeMax = new Date();
      timeMax.setMonth(timeMax.getMonth() + 6);

      const response = await calendar.events.list({
        calendarId: 'primary',
        timeMin: timeMin.toISOString(),
        timeMax: timeMax.toISOString(),
        singleEvents: true,
        orderBy: 'startTime',
      });

      const googleEvents = response.data.items || [];
      const normalizedEvents = googleEvents.map(item => ({
        external_id: item.id,
        external_source: "google_calendar",
        title: item.summary || "No Title",
        description: item.description || "",
        location: item.location || "",
        start_time: item.start?.dateTime || item.start?.date,
        end_time: item.end?.dateTime || item.end?.date,
        is_all_day: !item.start?.dateTime,
        updated_at: item.updated,
      })).filter(e => e.start_time); // Must have start time

      // Import pulled events into local DB
      await this.importExternalEvents(userId, normalizedEvents);

      // 2. Push to Google Calendar (only BFY schedules that don't have an external_id yet)
      const localSchedulesToPush = await scheduleRepo.find({
        where: {
          creator_id: userId,
          external_id: IsNull() // Assume only null needs pushing initially, or use a "needs_sync" flag
        }
      });

      let pushedCount = 0;
      for (const schedule of localSchedulesToPush) {
        // Build Google event object
        const event: any = {
          summary: schedule.title,
          description: schedule.description,
          location: schedule.location,
        };

        const startTime = schedule.start_time;
        // Default end_time to start_time + 1 hour if it is null
        const endTime = schedule.end_time ? new Date(schedule.end_time) : new Date(startTime.getTime() + 60 * 60 * 1000);

        if (schedule.is_all_day) {
          event.start = { date: startTime.toISOString().split('T')[0] };
          // Google all-day events require end date to be exclusive (the next day)
          // But if end_time was same as start_time, we add 1 day
          let endDay = new Date(endTime);
          if (endDay.toISOString().split('T')[0] === startTime.toISOString().split('T')[0]) {
              endDay = new Date(endDay.getTime() + 24 * 60 * 60 * 1000);
          }
          event.end = { date: endDay.toISOString().split('T')[0] };
        } else {
          event.start = { dateTime: startTime.toISOString() };
          event.end = { dateTime: endTime.toISOString() };
        }

        const res = await calendar.events.insert({
          calendarId: 'primary',
          requestBody: event,
        });

        // Save the Google ID back to our local schedule
        if (res.data.id) {
          schedule.external_id = res.data.id;
          schedule.external_source = "google_calendar";
          await scheduleRepo.save(schedule);
          pushedCount++;
        }
      }

      return { 
        success: true, 
        pulledCount: normalizedEvents.length, 
        pushedCount, 
        message: "Google Calendar sync completed successfully" 
      };

    } catch (error) {
      logger.error(`Sync failed for user ${userId}`, error);
      throw error;
    }
  }

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
