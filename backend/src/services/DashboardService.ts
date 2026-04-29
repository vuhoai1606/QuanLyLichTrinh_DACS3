import { AppDataSource } from "@config/database";

class DashboardService {
  async getDashboardSummary(userId: string) {
    try {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);

      const scheduleRepo = AppDataSource.getRepository("Schedule");
      const focusSessionRepo = AppDataSource.getRepository("FocusSession");

      // Count today's tasks by type
      const todaySchedules = await scheduleRepo
        .createQueryBuilder("s")
        .where(
          "s.creator_id = :userId AND s.created_at >= :today AND s.created_at < :tomorrow",
          { userId, today, tomorrow }
        )
        .select("s.type, s.status")
        .getMany();

      const events = todaySchedules.filter((s) => s.type === "EVENT").length;
      const tasks = todaySchedules.filter((s) => s.type === "TASK").length;
      const todos = todaySchedules.filter((s) => s.type === "TODO").length;
      const tasksPending = todaySchedules.filter(
        (s) => s.type === "TASK" && s.status === "PENDING"
      ).length;

      // Get today's focus sessions
      const todayFocusSessions = await focusSessionRepo
        .createQueryBuilder("fs")
        .where(
          "fs.user_id = :userId AND fs.created_at >= :today AND fs.created_at < :tomorrow",
          { userId, today, tomorrow }
        )
        .getMany();

      const totalFocusMinutes = todayFocusSessions.reduce(
        (sum, session) => sum + session.duration_minutes,
        0
      );
      const totalExpToday = todayFocusSessions.reduce(
        (sum, session) => sum + session.exp_earned,
        0
      );

      // Get user stats
      const userRepo = AppDataSource.getRepository("User");
      const user = await userRepo.findOne({ where: { id: userId } });

      // Get this week's focus time
      const weekStart = new Date(today);
      weekStart.setDate(weekStart.getDate() - weekStart.getDay());
      const thisWeekFocusSessions = await focusSessionRepo
        .createQueryBuilder("fs")
        .where(
          "fs.user_id = :userId AND fs.created_at >= :weekStart AND fs.created_at < :tomorrow",
          { userId, weekStart, tomorrow }
        )
        .getMany();

      const totalFocusMinutesWeek = thisWeekFocusSessions.reduce(
        (sum, session) => sum + session.duration_minutes,
        0
      );

      return {
        date: today.toISOString().split("T")[0],
        today: {
          events,
          tasks,
          todos,
          pending_tasks: tasksPending,
          focus_minutes: totalFocusMinutes,
          exp_earned: totalExpToday,
          focus_sessions: todayFocusSessions.length,
        },
        this_week: {
          focus_minutes: totalFocusMinutesWeek,
          focus_sessions: thisWeekFocusSessions.length,
        },
        user: {
          total_exp: user?.total_exp || 0,
          current_rank: user?.current_rank || "Rookie",
        },
      };
    } catch (error) {
      console.error("❌ Get dashboard summary error:", error);
      throw error;
    }
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
