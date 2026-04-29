import notificationService from "@services/NotificationService";
import { successResponse, errorResponse } from "@utils/errors";

export class NotificationController {
  // Save FCM token
  async saveFCMToken(body: any) {
    const { user_id, token, platform, device_name } = body;

    if (!user_id || !token || !platform) {
      return errorResponse(400, "user_id, token, and platform required", "MISSING_FIELDS");
    }

    try {
      const fcmToken = await notificationService.saveFCMToken(
        user_id,
        token,
        platform,
        device_name
      );
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "FCM token saved successfully",
          data: fcmToken,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Remove FCM token
  async removeFCMToken(tokenId: string) {
    try {
      await notificationService.removeFCMToken(tokenId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "FCM token removed",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get FCM tokens
  async getFCMTokens(userId: string) {
    try {
      const tokens = await notificationService.getFCMTokens(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: tokens,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get notifications
  async getNotifications(query: any) {
    const { user_id, limit = 50, offset = 0 } = query;

    if (!user_id) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const notifications = await notificationService.getNotifications(
        user_id,
        parseInt(limit),
        parseInt(offset)
      );
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: notifications,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get unread notifications
  async getUnreadNotifications(userId: string) {
    try {
      const notifications = await notificationService.getUnreadNotifications(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: notifications,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Mark as read
  async markAsRead(notificationId: string) {
    try {
      await notificationService.markNotificationAsRead(notificationId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "Notification marked as read",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Mark all as read
  async markAllAsRead(userId: string) {
    try {
      await notificationService.markAllNotificationsAsRead(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "All notifications marked as read",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Delete notification
  async deleteNotification(notificationId: string) {
    try {
      await notificationService.deleteNotification(notificationId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "Notification deleted",
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get notification stats
  async getNotificationStats(userId: string) {
    try {
      const stats = await notificationService.getNotificationStats(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: stats,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Send test notification
  async sendTestNotification(userId: string) {
    try {
      const notification = await notificationService.sendTestNotification(userId);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Test notification sent",
          data: notification,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Update notification
  async updateNotification(notificationId: string, body: any) {
    try {
      const notification = await notificationService.updateNotification(notificationId, body);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "Notification updated",
          data: notification,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Clear all notifications
  async clearAllNotifications(userId: string) {
    try {
      const result = await notificationService.clearAllNotifications(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: result,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get notification preferences
  async getNotificationPreferences(userId: string) {
    try {
      const prefs = await notificationService.getNotificationPreferences(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: prefs,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Set notification preferences
  async setNotificationPreferences(userId: string, body: any) {
    try {
      const prefs = await notificationService.setNotificationPreferences(userId, body);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Preferences updated",
          data: prefs,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Send bulk notifications
  async sendBulkNotifications(body: any) {
    const { user_ids, notification } = body;

    if (!user_ids || !Array.isArray(user_ids) || !notification) {
      return errorResponse(400, "user_ids array and notification required", "MISSING_FIELDS");
    }

    try {
      const result = await notificationService.sendBulkNotifications(user_ids, notification);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Bulk notifications sent",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}

export default new NotificationController();
