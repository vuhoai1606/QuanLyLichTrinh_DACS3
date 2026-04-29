import { AppDataSource } from "@config/database";
import { AppError } from "@utils/errors";

export class AdminService {
  async getAllUsers(limit: number = 50, offset: number = 0): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");

    const [users, total] = await userRepository.findAndCount({
      skip: offset,
      take: limit,
      order: { created_at: "DESC" },
    });

    return {
      data: users,
      pagination: {
        total,
        limit,
        offset,
        pages: Math.ceil(total / limit),
      },
    };
  }

  async getUserDetails(userId: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const sessionRepository = AppDataSource.getRepository("FocusSession");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    const schedules = await scheduleRepository.count({
      where: { creator_id: userId },
    });

    const sessions = await sessionRepository.count({
      where: { user_id: userId },
    });

    return {
      user,
      statistics: {
        totalSchedules: schedules,
        totalSessions: sessions,
      },
      account: {
        createdAt: user.created_at,
        lastLogin: user.last_login,
        status: user.is_active ? "ACTIVE" : "INACTIVE",
      },
    };
  }

  async changeUserStatus(userId: string, status: "ACTIVE" | "INACTIVE" | "BANNED"): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    if (status === "BANNED") {
      user.is_active = false;
      user.banned_at = new Date();
    } else if (status === "ACTIVE") {
      user.is_active = true;
      user.banned_at = null;
    } else {
      user.is_active = false;
    }

    await userRepository.save(user);

    return {
      userId,
      newStatus: status,
      changedAt: new Date(),
    };
  }

  async getSystemStatistics(): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const sessionRepository = AppDataSource.getRepository("FocusSession");
    const groupRepository = AppDataSource.getRepository("Group");

    const totalUsers = await userRepository.count();
    const activeUsers = await userRepository.count({ where: { is_active: true } });
    const totalSchedules = await scheduleRepository.count();
    const totalSessions = await sessionRepository.count();
    const totalGroups = await groupRepository.count();

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const newUsersToday = await userRepository.count({
      where: { created_at: { between: [today, tomorrow] } },
    });

    return {
      users: {
        total: totalUsers,
        active: activeUsers,
        inactive: totalUsers - activeUsers,
        newToday: newUsersToday,
      },
      schedules: {
        total: totalSchedules,
      },
      sessions: {
        total: totalSessions,
      },
      groups: {
        total: totalGroups,
      },
      timestamp: new Date(),
    };
  }

  async triggerMaintenance(action: string): Promise<any> {
    const actions = ["CACHE_CLEAR", "DB_OPTIMIZE", "LOG_CLEANUP"];

    if (!actions.includes(action)) {
      throw new AppError(400, "Invalid maintenance action", "INVALID_ACTION");
    }

    return {
      action,
      status: "INITIATED",
      startTime: new Date(),
      estimatedDuration: "5-10 minutes",
      message: `Maintenance action '${action}' has been initiated`,
    };
  }

  async getSystemLogs(limit: number = 100): Promise<any> {
    // Mock implementation - would connect to actual logging system
    return {
      logs: [
        {
          id: 1,
          timestamp: new Date(),
          level: "INFO",
          message: "Server started successfully",
          source: "system",
        },
        {
          id: 2,
          timestamp: new Date(Date.now() - 3600000),
          level: "WARNING",
          message: "High database response time detected",
          source: "database",
        },
      ],
      total: 2,
      limit,
    };
  }

  async searchUsers(query: string, limit: number = 50): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");

    const users = await userRepository.find({
      where: [
        { email: { like: `%${query}%` } },
        { full_name: { like: `%${query}%` } },
      ],
      take: limit,
    });

    return {
      query,
      results: users,
      count: users.length,
    };
  }

  async resetUserPassword(userId: string, newPassword: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const { hashPassword } = require("@utils/password");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    user.password_hash = await hashPassword(newPassword);
    await userRepository.save(user);

    return {
      userId,
      passwordReset: true,
      resetAt: new Date(),
    };
  }

  async sendBulkNotification(userIds: string[], notification: any): Promise<any> {
    const notificationRepository = AppDataSource.getRepository("Notification");

    const notifications = userIds.map(userId =>
      notificationRepository.create({
        user_id: userId,
        type: "SYSTEM",
        title: notification.title,
        message: notification.message,
      })
    );

    await notificationRepository.save(notifications);

    return {
      sent: notifications.length,
      timestamp: new Date(),
    };
  }
}

export default new AdminService();
