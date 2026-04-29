import { AppDataSource } from "@config/database";
import { AppError } from "@utils/errors";

export class SettingsService {
  async getAppSettings(): Promise<any> {
    return {
      appVersion: "1.0.0",
      apiVersion: "1.0",
      environment: process.env.NODE_ENV || "development",
      features: {
        gamification: true,
        notifications: true,
        collaboration: true,
        export: true,
        analytics: true,
      },
      limits: {
        maxSchedulesPerUser: 500,
        maxGroupsPerUser: 50,
        maxCollaboratorsPerTask: 20,
        maxNotificationsStored: 1000,
      },
      serverTime: new Date(),
    };
  }

  async getUserSettings(userId: string): Promise<any> {
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    const settings = await userSettingsRepository.findOne({
      where: { user_id: userId },
    });

    if (!settings) {
      throw new AppError(404, "User settings not found", "SETTINGS_NOT_FOUND");
    }

    return settings;
  }

  async updateUserSettings(userId: string, data: any): Promise<any> {
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    let settings = await userSettingsRepository.findOne({
      where: { user_id: userId },
    });

    if (!settings) {
      settings = userSettingsRepository.create({
        user_id: userId,
        ...data,
      });
    } else {
      Object.assign(settings, data);
    }

    return await userSettingsRepository.save(settings);
  }

  async changeTheme(userId: string, theme: string): Promise<any> {
    const validThemes = ["LIGHT", "DARK", "SYSTEM"];

    if (!validThemes.includes(theme)) {
      throw new AppError(400, "Invalid theme", "INVALID_THEME");
    }

    return await this.updateUserSettings(userId, { theme });
  }

  async changeLanguage(userId: string, language: string): Promise<any> {
    const validLanguages = ["en", "vi", "es", "fr", "de", "zh", "ja"];

    if (!validLanguages.includes(language)) {
      throw new AppError(400, "Invalid language", "INVALID_LANGUAGE");
    }

    return await this.updateUserSettings(userId, { language });
  }

  async setNotificationPreferences(userId: string, preferences: any): Promise<any> {
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    const settings = await userSettingsRepository.findOne({
      where: { user_id: userId },
    });

    if (!settings) {
      throw new AppError(404, "User settings not found", "SETTINGS_NOT_FOUND");
    }

    settings.notifications_enabled = preferences.enabled ?? settings.notifications_enabled;
    
    if (preferences.notificationTypes) {
      settings.notification_types = preferences.notificationTypes;
    }

    if (preferences.quietHours) {
      settings.quiet_hours_start = preferences.quietHours.start;
      settings.quiet_hours_end = preferences.quietHours.end;
    }

    return await userSettingsRepository.save(settings);
  }

  async getNotificationPreferences(userId: string): Promise<any> {
    const settings = await this.getUserSettings(userId);

    return {
      enabled: settings.notifications_enabled,
      types: settings.notification_types || ["ALL"],
      quietHours: {
        start: settings.quiet_hours_start,
        end: settings.quiet_hours_end,
      },
      email: {
        dailyDigest: true,
        weeklyReport: true,
      },
    };
  }

  async setPrivacySettings(userId: string, privacy: any): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    user.privacy_level = privacy.level || "PRIVATE"; // PRIVATE, FRIENDS_ONLY, PUBLIC
    user.show_profile = privacy.showProfile ?? true;
    user.show_stats = privacy.showStats ?? false;
    user.show_leaderboard = privacy.showLeaderboard ?? false;

    return await userRepository.save(user);
  }

  async getPrivacySettings(userId: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    return {
      level: user.privacy_level || "PRIVATE",
      showProfile: user.show_profile ?? true,
      showStats: user.show_stats ?? false,
      showLeaderboard: user.show_leaderboard ?? false,
    };
  }

  async setDataPreferences(userId: string, data: any): Promise<any> {
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    return await this.updateUserSettings(userId, {
      auto_archive_old_tasks: data.autoArchive ?? true,
      archive_after_days: data.archiveAfterDays || 90,
      data_retention_days: data.retentionDays || 365,
    });
  }

  async exportUserData(userId: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const sessionRepository = AppDataSource.getRepository("FocusSession");

    const user = await userRepository.findOne({ where: { id: userId } });
    const schedules = await scheduleRepository.find({ where: { creator_id: userId } });
    const sessions = await sessionRepository.find({ where: { user_id: userId } });

    return {
      exportDate: new Date(),
      user: {
        id: user?.id,
        email: user?.email,
        fullName: user?.full_name,
        createdAt: user?.created_at,
        totalExp: user?.total_exp,
        currentRank: user?.current_rank,
      },
      data: {
        schedulesCount: schedules.length,
        focusSessionsCount: sessions.length,
      },
      fileFormat: "json",
      size: "pending",
    };
  }
}

export default new SettingsService();
