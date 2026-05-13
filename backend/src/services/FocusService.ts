import { AppDataSource } from "@config/database";
import { generateUUID } from "@utils/validation";
import { AppError } from "@utils/errors";
import validationService from "./ValidationService";

const EXP_MULTIPLIER = 10;

export class FocusService {
  /**
   * Create focus session with validation
   */
  async createFocusSession(
    userId: string,
    duration_minutes: number,
    status: "COMPLETED" | "FAILED" = "COMPLETED",
    title?: string
  ): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    const userRepository = AppDataSource.getRepository("User");

    // Validate input
    const validation = validationService.validateFocusSession({
      user_id: userId,
      duration_minutes,
      status,
    });

    if (!validation.valid) {
      throw new AppError(400, validation.errors.join(", "), "INVALID_INPUT");
    }

    const exp_earned = status === "COMPLETED" ? Math.round(duration_minutes * EXP_MULTIPLIER) : 0;

    const session = focusSessionRepository.create({
      id: generateUUID(),
      user_id: userId,
      duration_minutes,
      status,
      exp_earned,
      created_at: new Date(),
    });

    const savedSession = await focusSessionRepository.save(session);

    // Update user XP if session completed
    if (status === "COMPLETED") {
      const gamificationService = require("./GamificationService").default;
      await gamificationService.updateUserRank(userId, exp_earned);
    }

    return {
      ...savedSession,
      message: `Focus session ${status === "COMPLETED" ? "completed" : "recorded"}. ${exp_earned} XP earned!`,
    };
  }

  /**
   * Pause focus session
   */
  async pauseSession(sessionId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const session = await focusSessionRepository.findOne({ where: { id: sessionId } });
    if (!session) {
      throw new AppError(404, "Session not found", "NOT_FOUND");
    }

    if (session.status === "PAUSED") {
      throw new AppError(400, "Session is already paused", "INVALID_STATE");
    }

    session.status = "PAUSED";
    const updated = await focusSessionRepository.save(session);

    return {
      ...updated,
      message: "Session paused successfully",
    };
  }

  /**
   * Resume focus session
   */
  async resumeSession(sessionId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const session = await focusSessionRepository.findOne({ where: { id: sessionId } });
    if (!session) {
      throw new AppError(404, "Session not found", "NOT_FOUND");
    }

    if (session.status !== "PAUSED") {
      throw new AppError(400, "Session cannot be resumed", "INVALID_STATE");
    }

    session.status = "COMPLETED";
    const updated = await focusSessionRepository.save(session);

    return {
      ...updated,
      message: "Session resumed successfully",
    };
  }

  /**
   * Get focus history
   */
  async getFocusHistory(userId: string, limit: number = 50): Promise<any[]> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const sessions = await focusSessionRepository
      .createQueryBuilder("fs")
      .where("fs.user_id = :userId", { userId })
      .orderBy("fs.created_at", "DESC")
      .take(Math.min(limit, 100))
      .getMany();

    return sessions;
  }

  /**
   * Get comprehensive focus stats
   */
  async getFocusStats(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const sessions = await focusSessionRepository.find({
      where: { user_id: userId },
      order: { created_at: "DESC" },
    });

    const completedSessions = sessions.filter((s) => s.status === "COMPLETED");
    const failedSessions = sessions.filter((s) => s.status === "FAILED");

    const totalMinutes = sessions.reduce((sum, s) => sum + s.duration_minutes, 0);
    const completedMinutes = completedSessions.reduce((sum, s) => sum + s.duration_minutes, 0);
    const totalXpEarned = completedSessions.reduce((sum, s) => sum + s.exp_earned, 0);

    // Calculate average session duration
    const avgDuration = sessions.length > 0 ? Math.round(totalMinutes / sessions.length) : 0;

    // Calculate this week's stats
    const thisWeekStart = new Date();
    thisWeekStart.setDate(thisWeekStart.getDate() - thisWeekStart.getDay());
    thisWeekStart.setHours(0, 0, 0, 0);

    const thisWeekSessions = completedSessions.filter(
      (s) => new Date(s.created_at) >= thisWeekStart
    );
    const thisWeekMinutes = thisWeekSessions.reduce((sum, s) => sum + s.duration_minutes, 0);

    // Calculate this month's stats
    const thisMonthStart = new Date();
    thisMonthStart.setDate(1);
    thisMonthStart.setHours(0, 0, 0, 0);

    const thisMonthSessions = completedSessions.filter(
      (s) => new Date(s.created_at) >= thisMonthStart
    );
    const thisMonthMinutes = thisMonthSessions.reduce((sum, s) => sum + s.duration_minutes, 0);

    return {
      total_sessions: sessions.length,
      completed_sessions: completedSessions.length,
      failed_sessions: failedSessions.length,
      completion_rate:
        sessions.length > 0 ? ((completedSessions.length / sessions.length) * 100).toFixed(1) : 0,
      total_focus_minutes: totalMinutes,
      completed_focus_minutes: completedMinutes,
      total_xp_earned: totalXpEarned,
      average_session_duration: avgDuration,
      this_week: {
        sessions: thisWeekSessions.length,
        minutes: thisWeekMinutes,
      },
      this_month: {
        sessions: thisMonthSessions.length,
        minutes: thisMonthMinutes,
      },
    };
  }

  /**
   * Get user's focus streak
   */
  async getStreak(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const sessions = await focusSessionRepository
      .createQueryBuilder("fs")
      .where("fs.user_id = :userId AND fs.status = 'COMPLETED'", { userId })
      .orderBy("DATE(fs.created_at)", "DESC")
      .getMany();

    if (sessions.length === 0) {
      return { current: 0, best: 0, last_session_date: null };
    }

    // Group sessions by date
    const dateMap = new Map<string, number>();
    for (const session of sessions) {
      const dateStr = new Date(session.created_at).toISOString().split("T")[0];
      dateMap.set(dateStr, (dateMap.get(dateStr) || 0) + 1);
    }

    const sortedDates = Array.from(dateMap.keys()).sort().reverse();

    // Calculate current streak
    let currentStreak = 0;
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    for (let i = 0; i < sortedDates.length; i++) {
      const date = new Date(sortedDates[i]);
      const expectedDate = new Date(today);
      expectedDate.setDate(expectedDate.getDate() - i);

      if (date.getTime() === expectedDate.getTime()) {
        currentStreak++;
      } else {
        break;
      }
    }

    return {
      current: currentStreak,
      best: sortedDates.length,
      last_session_date: sortedDates[0] || null,
    };
  }

  /**
   * Get today's focus summary
   */
  async getTodaysSummary(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const todaysSessions = await focusSessionRepository
      .createQueryBuilder("fs")
      .where(
        "fs.user_id = :userId AND fs.created_at >= :today AND fs.created_at < :tomorrow",
        { userId, today, tomorrow }
      )
      .getMany();

    const completedSessions = todaysSessions.filter((s) => s.status === "COMPLETED");
    const totalMinutes = todaysSessions.reduce((sum, s) => sum + s.duration_minutes, 0);
    const totalXpEarned = completedSessions.reduce((sum, s) => sum + s.exp_earned, 0);

    // Get user goal
    const userSettingsRepo = AppDataSource.getRepository("UserSettings");
    const settings = await userSettingsRepo.findOne({ where: { user_id: userId } });

    return {
      date: today.toISOString().split("T")[0],
      total_sessions: todaysSessions.length,
      completed_sessions: completedSessions.length,
      total_focus_minutes: totalMinutes,
      total_xp_earned: totalXpEarned,
      daily_goal_minutes: settings?.daily_focus_goal_minutes || 120,
      sessions: todaysSessions,
    };
  }

  /**
   * Compare user with others
   */
  async compareWithOtherUsers(userId: string, metric: "exp" | "focus_time" = "exp"): Promise<any> {
    const userRepo = AppDataSource.getRepository("User");
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const user = await userRepo.findOne({ where: { id: userId } });
    if (!user) {
      throw new AppError(404, "User not found", "NOT_FOUND");
    }

    if (metric === "exp") {
      // Compare by total exp
      const ranking = await userRepo
        .createQueryBuilder("u")
        .select(["u.id", "u.full_name", "u.total_exp", "u.current_rank"])
        .orderBy("u.total_exp", "DESC")
        .take(10)
        .getMany();

      const userRank = ranking.findIndex((u) => u.id === userId) + 1;

      return {
        metric: "total_exp",
        user_rank: userRank || "Not in top 10",
        user_exp: user.total_exp,
        top_users: ranking,
      };
    } else {
      // Compare by focus time
      const userFocusSessions = await focusSessionRepository.find({
        where: { user_id: userId, status: "COMPLETED" },
      });
      const userFocusTime = userFocusSessions.reduce((sum, s) => sum + s.duration_minutes, 0);

      // Get top 10 users by focus time
      const allUsers = await userRepo.find();
      const userStats = await Promise.all(
        allUsers.map(async (u: any) => {
          const sessions = await focusSessionRepository.find({
            where: { user_id: u.id, status: "COMPLETED" },
          });
          const focusTime = sessions.reduce((sum, s) => sum + s.duration_minutes, 0);
          return { id: u.id, full_name: u.full_name, focus_time: focusTime };
        })
      );

      const ranking = userStats.sort((a, b) => b.focus_time - a.focus_time).slice(0, 10);
      const userRank = ranking.findIndex((u) => u.id === userId) + 1;

      return {
        metric: "focus_time_minutes",
        user_rank: userRank || "Not in top 10",
        user_focus_time: userFocusTime,
        top_users: ranking.map((u) => ({
          id: u.id,
          full_name: u.full_name,
          focus_time: u.focus_time,
        })),
      };
    }
  }

  /**
   * Bulk create focus sessions (for batch operations)
   */
  async bulkCreateSessions(userId: string, sessions: any[]): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    const userRepository = AppDataSource.getRepository("User");

    let totalExpEarned = 0;
    const createdSessions = [];

    for (const session of sessions) {
      const validation = validationService.validateFocusSession({
        user_id: userId,
        duration_minutes: session.duration_minutes,
        status: session.status || "COMPLETED",
      });

      if (!validation.valid) {
        continue;
      }

      const expEarned =
        (session.status || "COMPLETED") === "COMPLETED"
          ? Math.round(session.duration_minutes * EXP_MULTIPLIER)
          : 0;

      const newSession = focusSessionRepository.create({
        id: generateUUID(),
        user_id: userId,
        duration_minutes: session.duration_minutes,
        status: session.status || "COMPLETED",
        exp_earned: expEarned,
      });

      const saved = await focusSessionRepository.save(newSession);
      createdSessions.push(saved);
      totalExpEarned += expEarned;
    }

    // Update user XP
    if (totalExpEarned > 0) {
      const user = await userRepository.findOne({ where: { id: userId } });
      if (user) {
        user.total_exp = (user.total_exp || 0) + totalExpEarned;
        this.updateUserRank(user);
        await userRepository.save(user);
      }
    }

    return {
      created_sessions: createdSessions.length,
      total_exp_earned: totalExpEarned,
      sessions: createdSessions,
    };
  }
}

export default new FocusService();
