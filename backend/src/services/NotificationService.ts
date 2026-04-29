import { AppDataSource } from "@config/database";

class NotificationService {
  async saveFCMToken(
    userId: string,
    token: string,
    platform: "ANDROID" | "IOS" | "WEB",
    deviceName?: string
  ) {
    try {
      const fcmTokenRepo = AppDataSource.getRepository("FCMToken");

      // Check if token already exists for this user
      const existing = await fcmTokenRepo.findOne({
        where: { user_id: userId, token },
      });

      if (existing) {
        await fcmTokenRepo.update({ id: existing.id }, { device_name: deviceName });
        return existing;
      }

      const fcmToken = await fcmTokenRepo.save({
        user_id: userId,
        token,
        platform,
        device_name: deviceName,
      });

      return fcmToken;
    } catch (error) {
      console.error("❌ Save FCM token error:", error);
      throw error;
    }
  }

  async removeFCMToken(tokenId: string) {
    try {
      const fcmTokenRepo = AppDataSource.getRepository("FCMToken");
      await fcmTokenRepo.delete({ id: tokenId });
      return true;
    } catch (error) {
      console.error("❌ Remove FCM token error:", error);
      throw error;
    }
  }

  async getFCMTokens(userId: string) {
    try {
      const fcmTokenRepo = AppDataSource.getRepository("FCMToken");
      return await fcmTokenRepo.find({
        where: { user_id: userId },
      });
    } catch (error) {
      console.error("❌ Get FCM tokens error:", error);
      throw error;
    }
  }

  async createNotification(
    userId: string,
    type: "SHARE" | "COLLAB_INVITE" | "TASK_ASSIGNED" | "GROUP_TASK" | "SYSTEM",
    title: string,
    message: string,
    senderId?: string,
    relatedId?: string
  ) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const notification = await notificationRepo.save({
        user_id: userId,
        sender_id: senderId || null,
        type,
        title,
        message,
        related_id: relatedId || null,
      });

      return notification;
    } catch (error) {
      console.error("❌ Create notification error:", error);
      throw error;
    }
  }

  async getNotifications(userId: string, limit: number = 50, offset: number = 0) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const notifications = await notificationRepo
        .createQueryBuilder("n")
        .leftJoinAndSelect("n.sender", "sender", "sender.id = n.sender_id")
        .where("n.user_id = :userId", { userId })
        .select([
          "n.id",
          "n.type",
          "n.title",
          "n.message",
          "n.is_read",
          "n.related_id",
          "n.created_at",
          "sender.id",
          "sender.full_name",
          "sender.avatar_url",
        ])
        .orderBy("n.created_at", "DESC")
        .skip(offset)
        .take(limit)
        .getMany();

      return notifications;
    } catch (error) {
      console.error("❌ Get notifications error:", error);
      throw error;
    }
  }

  async getUnreadNotifications(userId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      return await notificationRepo
        .createQueryBuilder("n")
        .where("n.user_id = :userId AND n.is_read = false", { userId })
        .getMany();
    } catch (error) {
      console.error("❌ Get unread notifications error:", error);
      throw error;
    }
  }

  async markNotificationAsRead(notificationId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");
      await notificationRepo.update({ id: notificationId }, { is_read: true });
      return true;
    } catch (error) {
      console.error("❌ Mark notification as read error:", error);
      throw error;
    }
  }

  async markAllNotificationsAsRead(userId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");
      await notificationRepo.update(
        { user_id: userId, is_read: false },
        { is_read: true }
      );
      return true;
    } catch (error) {
      console.error("❌ Mark all notifications as read error:", error);
      throw error;
    }
  }

  async deleteNotification(notificationId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");
      await notificationRepo.delete({ id: notificationId });
      return true;
    } catch (error) {
      console.error("❌ Delete notification error:", error);
      throw error;
    }
  }

  async getNotificationStats(userId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const total = await notificationRepo.count({
        where: { user_id: userId },
      });

      const unread = await notificationRepo.count({
        where: { user_id: userId, is_read: false },
      });

      const byType = await notificationRepo
        .createQueryBuilder("n")
        .select("n.type", "type")
        .addSelect("COUNT(*)", "count")
        .where("n.user_id = :userId", { userId })
        .groupBy("n.type")
        .getRawMany();

      return {
        total,
        unread,
        read: total - unread,
        byType: byType.reduce((acc: any, { type, count }) => {
          acc[type] = parseInt(count);
          return acc;
        }, {}),
      };
    } catch (error) {
      console.error("❌ Get notification stats error:", error);
      throw error;
    }
  }

  async sendTestNotification(userId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const notification = await notificationRepo.save({
        user_id: userId,
        type: "SYSTEM",
        title: "Test Notification",
        message: "This is a test notification to verify your notification settings.",
        created_at: new Date(),
      });

      return notification;
    } catch (error) {
      console.error("❌ Send test notification error:", error);
      throw error;
    }
  }

  async updateNotification(notificationId: string, data: any) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      await notificationRepo.update({ id: notificationId }, {
        title: data.title,
        message: data.message,
      });

      return await notificationRepo.findOne({ where: { id: notificationId } });
    } catch (error) {
      console.error("❌ Update notification error:", error);
      throw error;
    }
  }

  async clearAllNotifications(userId: string) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const result = await notificationRepo.delete({
        user_id: userId,
      });

      return {
        deletedCount: result.affected || 0,
        message: "All notifications cleared",
      };
    } catch (error) {
      console.error("❌ Clear all notifications error:", error);
      throw error;
    }
  }

  async setNotificationPreferences(userId: string, preferences: any) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const user = await userRepo.findOne({ where: { id: userId } });
      if (!user) throw new Error("User not found");

      user.notification_preferences = preferences;
      await userRepo.save(user);

      return preferences;
    } catch (error) {
      console.error("❌ Set notification preferences error:", error);
      throw error;
    }
  }

  async getNotificationPreferences(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const user = await userRepo.findOne({
        where: { id: userId },
        select: ["notification_preferences"],
      });

      return user?.notification_preferences || {
        enabled: true,
        types: ["ALL"],
        quietHours: { start: null, end: null },
      };
    } catch (error) {
      console.error("❌ Get notification preferences error:", error);
      throw error;
    }
  }

  async sendBulkNotifications(userIds: string[], notification: any) {
    try {
      const notificationRepo = AppDataSource.getRepository("Notification");

      const notifications = userIds.map(userId =>
        notificationRepo.create({
          user_id: userId,
          type: notification.type || "SYSTEM",
          title: notification.title,
          message: notification.message,
        })
      );

      const result = await notificationRepo.save(notifications);

      return {
        sentCount: result.length,
        sentAt: new Date(),
      };
    } catch (error) {
      console.error("❌ Send bulk notifications error:", error);
      throw error;
    }
  }
}

export default new NotificationService();
