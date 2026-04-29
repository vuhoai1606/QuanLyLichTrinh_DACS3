import settingsService from "@services/SettingsService";
import { successResponse, errorResponse } from "@utils/errors";

export class SettingsController {
  // Get app settings
  async getAppSettings() {
    try {
      const settings = await settingsService.getAppSettings();
      return successResponse(settings, "App settings retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get user settings
  async getUserSettings(userId: string) {
    try {
      const settings = await settingsService.getUserSettings(userId);
      return successResponse(settings, "User settings retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Update user settings
  async updateUserSettings(userId: string, body: any) {
    try {
      const settings = await settingsService.updateUserSettings(userId, body);
      return successResponse(settings, "User settings updated");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Change theme
  async changeTheme(userId: string, body: any) {
    const { theme } = body;

    if (!theme) {
      return errorResponse(400, "theme required", "MISSING_FIELDS");
    }

    try {
      const result = await settingsService.changeTheme(userId, theme);
      return successResponse(result, "Theme changed");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Change language
  async changeLanguage(userId: string, body: any) {
    const { language } = body;

    if (!language) {
      return errorResponse(400, "language required", "MISSING_FIELDS");
    }

    try {
      const result = await settingsService.changeLanguage(userId, language);
      return successResponse(result, "Language changed");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get notification preferences
  async getNotificationPreferences(userId: string) {
    try {
      const prefs = await settingsService.getNotificationPreferences(userId);
      return successResponse(prefs, "Notification preferences retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Set notification preferences
  async setNotificationPreferences(userId: string, body: any) {
    try {
      const prefs = await settingsService.setNotificationPreferences(userId, body);
      return successResponse(prefs, "Notification preferences updated");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get privacy settings
  async getPrivacySettings(userId: string) {
    try {
      const privacy = await settingsService.getPrivacySettings(userId);
      return successResponse(privacy, "Privacy settings retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Set privacy settings
  async setPrivacySettings(userId: string, body: any) {
    try {
      const privacy = await settingsService.setPrivacySettings(userId, body);
      return successResponse(privacy, "Privacy settings updated");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Export user data
  async exportUserData(userId: string) {
    try {
      const exported = await settingsService.exportUserData(userId);
      return successResponse(exported, "User data exported");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}

export default new SettingsController();
