import { AppDataSource } from "@config/database";
import { FocusSession } from "@models/FocusSession";
import { Schedule } from "@models/Schedule";
import { Between } from "typeorm";
import { DateTime } from "luxon";

export class AnalyticsService {
  async getUserProductivityStats(userId: string) {
    const focusRepo = AppDataSource.getRepository("FocusSession");
    const scheduleRepo = AppDataSource.getRepository("Schedule");

    const now = DateTime.now();
    const startOfMonth = now.startOf("month").toJSDate();
    const endOfMonth = now.endOf("month").toJSDate();

    // Focus minutes per day for heatmap
    const sessions = await focusRepo.find({
      where: {
        user_id: userId,
        created_at: Between(startOfMonth, endOfMonth)
      }
    });

    const focusHeatmap: Record<string, number> = {};
    sessions.forEach(s => {
      const day = DateTime.fromJSDate(s.created_at).toISODate();
      if (day) {
        focusHeatmap[day] = (focusHeatmap[day] || 0) + s.duration_minutes;
      }
    });

    // Task completion rate
    const tasks = await scheduleRepo.find({
      where: { creator_id: userId, type: "TASK" }
    });

    const totalTasks = tasks.length;
    const completedTasks = tasks.filter(t => t.status === "DONE").length;
    const completionRate = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;

    // Focus trends (last 7 days)
    const sevenDaysAgo = now.minus({ days: 7 }).toJSDate();
    const recentSessions = sessions.filter(s => s.created_at >= sevenDaysAgo);
    
    const dailyTrend: Record<string, number> = {};
    for (let i = 0; i < 7; i++) {
      const date = now.minus({ days: i }).toISODate();
      if (date) dailyTrend[date] = 0;
    }

    recentSessions.forEach(s => {
      const day = DateTime.fromJSDate(s.created_at).toISODate();
      if (day && dailyTrend[day] !== undefined) {
        dailyTrend[day] += s.duration_minutes;
      }
    });

    // Golden hours calculation
    const hourMap: Record<number, number> = {};
    for (let i = 0; i < 24; i++) hourMap[i] = 0;
    
    sessions.forEach(s => {
      const hour = DateTime.fromJSDate(s.created_at).hour;
      hourMap[hour] += s.duration_minutes;
    });
    
    const goldenHours = Object.keys(hourMap)
      .map(hour => ({ hour: parseInt(hour), minutes: hourMap[parseInt(hour)] }))
      .sort((a, b) => b.minutes - a.minutes)
      .slice(0, 3)
      .filter(h => h.minutes > 0);

    return {
      focusHeatmap,
      completionRate,
      dailyTrend,
      goldenHours,
      totalFocusMinutes: sessions.reduce((acc, s) => acc + s.duration_minutes, 0),
      completedTasksCount: completedTasks
    };
  }
}

export default new AnalyticsService();
