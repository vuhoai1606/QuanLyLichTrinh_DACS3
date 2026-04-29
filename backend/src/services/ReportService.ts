import { AppDataSource } from "@config/database";
import { AppError } from "@utils/errors";

export class ReportService {
  async getProductivityReport(userId: string, startDate?: Date, endDate?: Date): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const start = startDate || new Date(new Date().setDate(new Date().getDate() - 30));
    const end = endDate || new Date();

    const completed = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "DONE",
        updated_at: { between: [start, end] },
      },
    });

    const pending = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "PENDING",
      },
    });

    const inProgress = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "DOING",
      },
    });

    const completionRate = pending + inProgress === 0 ? 100 : (completed / (completed + pending + inProgress)) * 100;

    return {
      period: { start, end },
      completed,
      pending,
      inProgress,
      completionRate: parseFloat(completionRate.toFixed(2)),
      timestamp: new Date(),
    };
  }

  async getFocusAnalysis(userId: string, startDate?: Date, endDate?: Date): Promise<any> {
    const sessionRepository = AppDataSource.getRepository("FocusSession");
    
    const start = startDate || new Date(new Date().setDate(new Date().getDate() - 30));
    const end = endDate || new Date();

    const sessions = await sessionRepository.find({
      where: {
        user_id: userId,
        end_time: { between: [start, end] },
      },
    });

    const totalMinutes = sessions.reduce((sum, s) => sum + (s.duration_minutes || 0), 0);
    const avgSession = sessions.length > 0 ? (totalMinutes / sessions.length).toFixed(2) : 0;
    const longestSession = Math.max(...sessions.map(s => s.duration_minutes || 0), 0);

    return {
      period: { start, end },
      totalSessions: sessions.length,
      totalMinutes,
      averageSession: parseFloat(String(avgSession)),
      longestSession,
      daysActive: new Set(sessions.map(s => s.start_time?.toDateString())).size,
    };
  }

  async getTaskCompletionRate(userId: string, startDate?: Date, endDate?: Date): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const start = startDate || new Date(new Date().setDate(new Date().getDate() - 30));
    const end = endDate || new Date();

    const tasks = await scheduleRepository.find({
      where: {
        creator_id: userId,
        type: "TASK",
        created_at: { between: [start, end] },
      },
    });

    const completed = tasks.filter(t => t.status === "DONE").length;
    const rate = tasks.length > 0 ? (completed / tasks.length) * 100 : 0;

    return {
      period: { start, end },
      totalTasks: tasks.length,
      completedTasks: completed,
      completionRate: parseFloat(rate.toFixed(2)),
      breakdown: {
        done: tasks.filter(t => t.status === "DONE").length,
        doing: tasks.filter(t => t.status === "DOING").length,
        pending: tasks.filter(t => t.status === "PENDING").length,
      },
    };
  }

  async getExpHistory(userId: string): Promise<any> {
    const focusRepository = AppDataSource.getRepository("FocusSession");
    
    const sessions = await focusRepository.find({
      where: { user_id: userId },
      order: { start_time: "DESC" },
      take: 100,
    });

    const expHistory = sessions.map(s => ({
      date: s.start_time,
      expGained: (s.duration_minutes || 0) * 10, // 10 EXP per minute
      duration: s.duration_minutes,
      type: "focus_session",
    }));

    const totalExp = expHistory.reduce((sum, h) => sum + h.expGained, 0);

    return {
      totalExp,
      history: expHistory,
      monthlyAverage: (totalExp / 30).toFixed(2),
    };
  }

  async exportReport(userId: string, format: string = "json", reportType: string = "productivity"): Promise<any> {
    let report: any;

    switch (reportType) {
      case "productivity":
        report = await this.getProductivityReport(userId);
        break;
      case "focus":
        report = await this.getFocusAnalysis(userId);
        break;
      case "completion":
        report = await this.getTaskCompletionRate(userId);
        break;
      case "exp":
        report = await this.getExpHistory(userId);
        break;
      default:
        throw new AppError(400, "Invalid report type", "INVALID_REPORT");
    }

    const timestamp = new Date().toISOString().replace(/[:.]/g, "-");

    return {
      fileName: `report_${reportType}_${timestamp}`,
      format,
      data: report,
      generatedAt: new Date(),
      userId,
    };
  }

  async compareWithPreviousPeriod(userId: string, reportType: string = "productivity"): Promise<any> {
    const now = new Date();
    const startOfCurrent = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfPrevious = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const endOfPrevious = new Date(startOfCurrent.getTime() - 1);

    let current, previous;

    switch (reportType) {
      case "productivity":
        current = await this.getProductivityReport(userId, startOfCurrent, now);
        previous = await this.getProductivityReport(userId, startOfPrevious, endOfPrevious);
        break;
      case "focus":
        current = await this.getFocusAnalysis(userId, startOfCurrent, now);
        previous = await this.getFocusAnalysis(userId, startOfPrevious, endOfPrevious);
        break;
      default:
        current = await this.getTaskCompletionRate(userId, startOfCurrent, now);
        previous = await this.getTaskCompletionRate(userId, startOfPrevious, endOfPrevious);
    }

    return {
      current,
      previous,
      comparison: {
        improvement: ((current.completionRate - previous.completionRate) / previous.completionRate) * 100,
        trend: current.completionRate > previous.completionRate ? "📈 Increasing" : "📉 Decreasing",
      },
    };
  }

  async getHealthScore(userId: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const sessionRepository = AppDataSource.getRepository("FocusSession");
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const user = await userRepository.findOne({ where: { id: userId } });
    if (!user) throw new AppError(404, "User not found", "USER_NOT_FOUND");

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    const focusSessions = await sessionRepository.count({
      where: {
        user_id: userId,
        start_time: { between: [today, tomorrow] },
      },
    });

    const tasksCompleted = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "DONE",
        updated_at: { between: [today, tomorrow] },
      },
    });

    const score = Math.min(100, focusSessions * 20 + tasksCompleted * 15 + Math.min(user.total_exp / 100, 20));

    return {
      score: parseFloat(score.toFixed(2)),
      rank: user.current_rank || "Rookie",
      level: Math.floor((user.total_exp || 0) / 500) + 1,
      components: {
        focusSessions: Math.min(focusSessions * 20, 40),
        tasksCompleted: Math.min(tasksCompleted * 15, 30),
        expProgress: Math.min(user.total_exp / 100, 20),
        streakBonus: Math.min((user.current_streak || 0) * 2, 10),
      },
      timestamp: new Date(),
    };
  }
}

export default new ReportService();
