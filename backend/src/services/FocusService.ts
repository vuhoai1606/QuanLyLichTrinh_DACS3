import { AppDataSource } from "@config/database";
import { generateUUID } from "@utils/validation";
import { AppError } from "@utils/errors";

const EXP_MULTIPLIER = 10;

export class FocusService {
  async createFocusSession(userId: string, duration_minutes: number, status: "COMPLETED" | "FAILED"): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    const userRepository = AppDataSource.getRepository("User");

    if (duration_minutes <= 0) {
      throw new AppError(400, "Duration must be positive", "INVALID_DURATION");
    }

    const exp_earned = status === "COMPLETED" ? duration_minutes * EXP_MULTIPLIER : 0;

    const session = focusSessionRepository.create({
      id: generateUUID(),
      user_id: userId,
      duration_minutes,
      status,
      exp_earned,
    });

    const savedSession = await focusSessionRepository.save(session);

    if (status === "COMPLETED") {
      const user = await userRepository.findOne({ where: { id: userId } });
      if (user) {
        user.total_exp = (user.total_exp || 0) + exp_earned;
        this.updateUserRank(user);
        await userRepository.save(user);
      }
    }

    return savedSession;
  }

  async getFocusHistory(userId: string, limit: number = 50): Promise<any[]> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    
    return focusSessionRepository.find({
      where: { user_id: userId },
      order: { created_at: "DESC" },
      take: limit,
    });
  }

  async getFocusStats(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    
    const sessions = await focusSessionRepository.find({
      where: { user_id: userId },
    });

    return {
      total_minutes: sessions.reduce((sum, s) => sum + s.duration_minutes, 0),
      total_sessions: sessions.length,
      completed: sessions.filter((s) => s.status === "COMPLETED").length,
    };
  }

  private updateUserRank(user: any): void {
    const ranks = [
      { name: "Rookie", minExp: 0 },
      { name: "Novice", minExp: 100 },
      { name: "Apprentice", minExp: 500 },
      { name: "Expert", minExp: 1000 },
      { name: "Master", minExp: 5000 },
      { name: "Legend", minExp: 10000 },
    ];

    let newRank = "Rookie";
    for (const rank of ranks) {
      if (user.total_exp >= rank.minExp) {
        newRank = rank.name;
      }
    }

    if (newRank !== user.current_rank) {
      user.current_rank = newRank;
    }
  }

  async getTodaysSummary(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const sessions = await focusSessionRepository.find({
      where: {
        user_id: userId,
        start_time: { between: [today, tomorrow] },
      },
    });

    const totalMinutes = sessions.reduce((sum, s) => sum + (s.duration_minutes || 0), 0);
    const expEarned = sessions
      .filter(s => s.status === "COMPLETED")
      .reduce((sum, s) => sum + (s.exp_earned || 0), 0);

    const tasksCompleted = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "DONE",
        updated_at: { between: [today, tomorrow] },
      },
    });

    return {
      date: today,
      focusSessions: sessions.length,
      totalMinutes,
      expEarned,
      tasksCompleted,
      goalMet: totalMinutes >= 120, // 2 hour daily goal
    };
  }

  async getStreak(userId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const sessions = await focusSessionRepository.find({
      where: { user_id: userId },
      order: { start_time: "DESC" },
    });

    let streak = 0;
    const today = new Date();
    let currentDate = new Date(today);
    currentDate.setHours(0, 0, 0, 0);

    for (const session of sessions) {
      const sessionDate = new Date(session.start_time);
      sessionDate.setHours(0, 0, 0, 0);

      if (sessionDate.getTime() === currentDate.getTime()) {
        streak++;
        currentDate.setDate(currentDate.getDate() - 1);
      } else if (sessionDate.getTime() < currentDate.getTime()) {
        break;
      }
    }

    return {
      currentStreak: streak,
      bestStreak: Math.max(...sessions.map((_, i) => i + 1), 0),
      lastSessionDate: sessions[0]?.start_time,
    };
  }

  async pauseSession(sessionId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const session = await focusSessionRepository.findOne({ where: { id: sessionId } });
    if (!session) {
      throw new AppError(404, "Session not found", "SESSION_NOT_FOUND");
    }

    session.is_paused = true;
    session.paused_at = new Date();

    return focusSessionRepository.save(session);
  }

  async resumeSession(sessionId: string): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const session = await focusSessionRepository.findOne({ where: { id: sessionId } });
    if (!session) {
      throw new AppError(404, "Session not found", "SESSION_NOT_FOUND");
    }

    if (session.is_paused) {
      const pausedTime = session.paused_at ? Date.now() - session.paused_at.getTime() : 0;
      session.is_paused = false;
      session.paused_at = null;
      session.paused_duration = (session.paused_duration || 0) + pausedTime;
    }

    return focusSessionRepository.save(session);
  }

  async bulkCreateSessions(userId: string, sessions: any[]): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");

    const created = sessions.map(s =>
      focusSessionRepository.create({
        id: s.id || generateUUID(),
        user_id: userId,
        ...s,
      })
    );

    return focusSessionRepository.save(created);
  }

  async compareWithOtherUsers(userId: string, metricType: string = "totalMinutes"): Promise<any> {
    const focusSessionRepository = AppDataSource.getRepository("FocusSession");
    const userRepository = AppDataSource.getRepository("User");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) {
      throw new AppError(404, "User not found", "USER_NOT_FOUND");
    }

    // Get user's stats
    const userSessions = await focusSessionRepository.find({
      where: { user_id: userId },
    });

    const userTotal = userSessions.reduce((sum, s) => sum + (s.duration_minutes || 0), 0);

    // Get top users for comparison
    const topSessions = await focusSessionRepository.find({
      take: 10,
    });

    const userStats = new Map();
    topSessions.forEach(s => {
      userStats.set(s.user_id, (userStats.get(s.user_id) || 0) + (s.duration_minutes || 0));
    });

    const ranking = Array.from(userStats.entries())
      .map(([userId, total]) => ({ userId, total }))
      .sort((a, b) => b.total - a.total);

    const userRank = ranking.findIndex(r => r.userId === userId) + 1;

    return {
      userTotal,
      userRank,
      topUsers: ranking.slice(0, 5),
      percentileRank: ((1 - userRank / ranking.length) * 100).toFixed(2),
    };
  }
}

export default new FocusService();
