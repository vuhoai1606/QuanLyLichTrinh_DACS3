import { AppDataSource } from "@config/database";

class DashboardService {
  /**
   * Get comprehensive dashboard summary for home screen
   * Includes: user info, rank, recent schedules, stats, streak, etc.
   */
  async getDashboardSummary(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      const scheduleRepo = AppDataSource.getRepository("Schedule");
      const rankRepo = AppDataSource.getRepository("Rank");
      const focusSessionRepo = AppDataSource.getRepository("FocusSession");

      // Get user info
      const user = await userRepo.findOne({
        where: { id: userId },
        select: ["id", "full_name", "email", "avatar_url", "total_exp", "current_rank"],
      });

      if (!user) {
        throw new Error("User not found");
      }

      // Get rank info
      let currentRank: any = null;
      let nextRank: any = null;
      let expInCurrentRank = 0;
      let expToNextRank = 0;

      const userRank = await rankRepo.findOne({
        where: { rank_name: user.current_rank || "Rookie" },
      });

      if (userRank) {
        currentRank = userRank;
        expInCurrentRank = user.total_exp - (userRank.min_exp || 0);
        expToNextRank = (userRank.max_exp || 9999) - (userRank.min_exp || 0);
      }

      // Get next rank if available
      if (userRank && userRank.id < 7) {
        nextRank = await rankRepo.findOne({
          where: { id: userRank.id + 1 },
        });
      }

      // Get today's and recent schedules (last 7 days)
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

      const recentSchedules = await scheduleRepo
        .createQueryBuilder("s")
        .leftJoinAndSelect("s.category", "category")
        .where(
          "s.creator_id = :userId AND s.created_at >= :sevenDaysAgo",
          { userId, sevenDaysAgo }
        )
        .orderBy("s.created_at", "DESC")
        .take(10)
        .getMany();

      // Get today's schedules specifically
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);

      const todaySchedules = recentSchedules.filter((s) => {
        const scheduleDate = new Date(s.created_at);
        scheduleDate.setHours(0, 0, 0, 0);
        return scheduleDate.getTime() === today.getTime();
      });

      // Calculate stats
      const completedTotal = recentSchedules.filter(
        (s) => s.status === "COMPLETED"
      ).length;
      const pendingTotal = recentSchedules.filter(
        (s) => s.status === "PENDING"
      ).length;
      const totalRecent = recentSchedules.length;

      const completedToday = todaySchedules.filter(
        (s) => s.status === "COMPLETED"
      ).length;
      const pendingToday = todaySchedules.filter(
        (s) => s.status === "PENDING"
      ).length;
      const totalToday = todaySchedules.length;

      // Get today's focus sessions
      const todayFocusSessions = await focusSessionRepo
        .createQueryBuilder("fs")
        .where(
          "fs.user_id = :userId AND DATE(fs.created_at) = DATE(:today)",
          { userId, today }
        )
        .getMany();

      const focusMinutesToday = todayFocusSessions.reduce(
        (sum, s) => sum + s.duration_minutes,
        0
      );

      // Calculate focus streak
      const streak = await this.calculateStreak(userId);

      // Map schedules to ScheduleDto format
      const recentSummary = recentSchedules.map((schedule) => ({
        id: schedule.id,
        title: schedule.title,
        description: schedule.description,
        type: schedule.type,
        status: schedule.status,
        category_name: schedule.category?.name || "General",
        category_color: schedule.category?.color || "#6366F1",
        priority: schedule.priority || "MEDIUM",
        tags: schedule.tags || [],
        created_at: schedule.created_at,
        is_today: todaySchedules.some((t) => t.id === schedule.id),
      }));

      return {
        user: {
          id: user.id,
          name: user.full_name,
          email: user.email,
          avatar_url: user.avatar_url,
        },
        rank: {
          id: currentRank?.id || 1,
          name: currentRank?.rank_name || "Rookie",
          level: currentRank?.id || 1,
          total_exp: user.total_exp,
          exp_in_current_rank: expInCurrentRank,
          exp_to_next_rank: expToNextRank,
          exp_progress_percent:
            expToNextRank > 0
              ? Math.round((expInCurrentRank / expToNextRank) * 100)
              : 100,
          next_rank: nextRank
            ? {
                id: nextRank.id,
                name: nextRank.rank_name,
                level: nextRank.id,
                min_exp: nextRank.min_exp,
              }
            : null,
        },
        summary: recentSummary,
        stats: {
          completed: completedToday,
          pending: pendingToday,
          total: totalToday,
          today: {
            completed: completedToday,
            pending: pendingToday,
            total: totalToday,
            focus_minutes: focusMinutesToday,
            focus_sessions: todayFocusSessions.length,
          },
          recent_7_days: {
            completed: completedTotal,
            pending: pendingTotal,
            total: totalRecent,
          },
          streak: streak,
        },
        message: this.generateMotivationalMessage(
          completedToday,
          pendingToday,
          streak.current
        ),
      };
    } catch (error) {
      console.error("❌ Get dashboard summary error:", error);
      throw error;
    }
  }

  /**
   * Calculate focus session streak for user
   */
  private async calculateStreak(userId: string) {
    try {
      const focusSessionRepo = AppDataSource.getRepository("FocusSession");
      let current = 0;
      let best = 0;
      let lastDate: Date | null = null;

      // Get all focus sessions ordered by date descending
      const allSessions = await focusSessionRepo
        .createQueryBuilder("fs")
        .where("fs.user_id = :userId", { userId })
        .orderBy("DATE(fs.created_at)", "DESC")
        .getMany();

      // Group by date and count
      const dateMap = new Map<string, number>();
      for (const session of allSessions) {
        const dateStr = new Date(session.created_at)
          .toISOString()
          .split("T")[0];
        dateMap.set(dateStr, (dateMap.get(dateStr) || 0) + 1);
      }

      // Calculate streaks
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const sortedDates = Array.from(dateMap.keys()).sort().reverse();

      let streak = 0;
      let currentDate = today;

      for (const dateStr of sortedDates) {
        const date = new Date(dateStr);
        const expectedDate = new Date(currentDate);
        expectedDate.setDate(expectedDate.getDate() - streak);

        if (date.getTime() === expectedDate.getTime()) {
          streak++;
        } else {
          break;
        }
      }

      current = streak;
      best = sortedDates.length;

      return {
        current,
        best,
        last_session_date: sortedDates.length > 0 ? sortedDates[0] : null,
      };
    } catch (error) {
      console.error("❌ Calculate streak error:", error);
      return { current: 0, best: 0, last_session_date: null };
    }
  }

  /**
   * Generate motivational message based on user progress
   */
  private generateMotivationalMessage(
    completed: number,
    pending: number,
    streak: number
  ): string {
    const messages = [
      `Great start! You've completed ${completed} task${completed !== 1 ? "s" : ""} today.`,
      `Keep the momentum! You have ${pending} pending task${pending !== 1 ? "s" : ""} to tackle.`,
      `🔥 Amazing streak of ${streak} day${streak !== 1 ? "s" : ""}! Keep it going!`,
      "You're making excellent progress today!",
      "Every task completed brings you closer to your goals!",
      "Stay focused and productive!",
      "You're unstoppable! Keep crushing your goals!",
      "Remember: Progress over perfection!",
    ];

    if (streak > 0 && completed > 0) {
      return `🔥 Amazing streak of ${streak} day${streak !== 1 ? "s" : ""}! You've completed ${completed} task${completed !== 1 ? "s" : ""} today!`;
    } else if (completed > 0) {
      return `Great job! You've completed ${completed} task${completed !== 1 ? "s" : ""} today. Keep the momentum!`;
    } else if (pending > 0) {
      return `You have ${pending} pending task${pending !== 1 ? "s" : ""} today. Let's get started!`;
    }

    return messages[Math.floor(Math.random() * messages.length)];
  }

  async getWeeklyStats(userId: string) {
    try {
      const today = new Date();
      const weekStart = new Date(today);
      weekStart.setDate(weekStart.getDate() - weekStart.getDay());
      weekStart.setHours(0, 0, 0, 0);

      const focusSessionRepo = AppDataSource.getRepository("FocusSession");

      const dailyStats = [];

      for (let i = 0; i < 7; i++) {
        const dayStart = new Date(weekStart);
        dayStart.setDate(dayStart.getDate() + i);
        const dayEnd = new Date(dayStart);
        dayEnd.setDate(dayEnd.getDate() + 1);

        const daySessions = await focusSessionRepo
          .createQueryBuilder("fs")
          .where(
            "fs.user_id = :userId AND fs.created_at >= :dayStart AND fs.created_at < :dayEnd",
            { userId, dayStart, dayEnd }
          )
          .getMany();

        const dayName = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][dayStart.getDay()];
        const focusMinutes = daySessions.reduce((sum, s) => sum + s.duration_minutes, 0);
        const expEarned = daySessions.reduce((sum, s) => sum + s.exp_earned, 0);

        dailyStats.push({
          day: dayName,
          date: dayStart.toISOString().split("T")[0],
          focus_minutes: focusMinutes,
          exp_earned: expEarned,
          sessions: daySessions.length,
        });
      }

      return dailyStats;
    } catch (error) {
      console.error("❌ Get weekly stats error:", error);
      throw error;
    }
  }

  async getMonthlyStats(userId: string) {
    try {
      const today = new Date();
      const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
      monthStart.setHours(0, 0, 0, 0);
      const monthEnd = new Date(monthStart);
      monthEnd.setMonth(monthEnd.getMonth() + 1);

      const focusSessionRepo = AppDataSource.getRepository("FocusSession");

      const monthlySessions = await focusSessionRepo
        .createQueryBuilder("fs")
        .where(
          "fs.user_id = :userId AND fs.created_at >= :monthStart AND fs.created_at < :monthEnd",
          { userId, monthStart, monthEnd }
        )
        .getMany();

      const totalFocusMinutes = monthlySessions.reduce((sum, s) => sum + s.duration_minutes, 0);
      const totalExp = monthlySessions.reduce((sum, s) => sum + s.exp_earned, 0);

      return {
        month: monthStart.toLocaleDateString("en-US", { month: "long", year: "numeric" }),
        total_focus_minutes: totalFocusMinutes,
        total_exp: totalExp,
        total_sessions: monthlySessions.length,
        average_session_duration: monthlySessions.length > 0 ? Math.round(totalFocusMinutes / monthlySessions.length) : 0,
      };
    } catch (error) {
      console.error("❌ Get monthly stats error:", error);
      throw error;
    }
  }
}

export default new DashboardService();
