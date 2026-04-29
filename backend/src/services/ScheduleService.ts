import { AppDataSource } from "@config/database";
import { generateUUID, isValidHexColor } from "@utils/validation";
import { AppError } from "@utils/errors";

export class ScheduleService {
  async createCategory(userId: string, name: string, hex_color: string): Promise<any> {
    const categoryRepository = AppDataSource.getRepository("Category");
    
    if (!isValidHexColor(hex_color)) {
      throw new AppError(400, "Invalid hex color format", "INVALID_COLOR");
    }

    const existing = await categoryRepository.findOne({ where: { user_id: userId, name } });
    if (existing) {
      throw new AppError(400, "Category name already exists", "DUPLICATE_CATEGORY");
    }

    const category = categoryRepository.create({
      id: generateUUID(),
      user_id: userId,
      name,
      hex_color,
    });

    return categoryRepository.save(category);
  }

  async createSchedule(data: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    if (data.type === "TODO" && !data.title) {
      throw new AppError(400, "Title is required", "MISSING_TITLE");
    }

    if (data.type === "TASK" && !data.deadline) {
      throw new AppError(400, "Deadline is required for TASK", "MISSING_DEADLINE");
    }

    if (data.type === "EVENT") {
      if (!data.start_date || !data.end_date) {
        throw new AppError(400, "Start date and end date required for EVENT", "MISSING_TIME");
      }
      if (data.end_date <= data.start_date) {
        throw new AppError(400, "End date must be after start date", "INVALID_TIME");
      }
    }

    const schedule = scheduleRepository.create({
      id: generateUUID(),
      ...data,
      status: "PENDING",
    });

    return scheduleRepository.save(schedule);
  }

  async getTimelineForUser(userId: string, startDate: Date, endDate: Date): Promise<any[]> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const schedules = await scheduleRepository.find({
      where: { creator_id: userId },
      relations: ["category"],
      order: { start_date: "DESC", deadline: "DESC", created_at: "DESC" },
    });

    return schedules.filter((s: any) => {
      const compareDate = s.start_date || s.deadline || s.created_at;
      return compareDate >= startDate && compareDate <= endDate;
    });
  }

  async updateSchedule(scheduleId: string, userId: string, updates: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const schedule = await scheduleRepository.findOne({ where: { id: scheduleId } });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    if (schedule.creator_id !== userId) {
      throw new AppError(403, "Unauthorized to update this schedule", "UNAUTHORIZED");
    }

    Object.assign(schedule, updates);
    return scheduleRepository.save(schedule);
  }

  async deleteSchedule(scheduleId: string, userId: string): Promise<void> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const schedule = await scheduleRepository.findOne({ where: { id: scheduleId } });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    if (schedule.creator_id !== userId) {
      throw new AppError(403, "Unauthorized to delete this schedule", "UNAUTHORIZED");
    }

    await scheduleRepository.remove(schedule);
  }

  async createRecurringSchedule(data: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    
    const recurrenceTypes = ["DAILY", "WEEKLY", "MONTHLY"];
    if (!recurrenceTypes.includes(data.recurrence)) {
      throw new AppError(400, "Invalid recurrence type", "INVALID_RECURRENCE");
    }

    const schedule = scheduleRepository.create({
      id: generateUUID(),
      ...data,
      is_recurring: true,
      recurrence_type: data.recurrence,
    });

    return scheduleRepository.save(schedule);
  }

  async bulkCreateSchedules(userId: string, schedules: any[]): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const created = schedules.map(s =>
      scheduleRepository.create({
        id: generateUUID(),
        creator_id: userId,
        ...s,
      })
    );

    const saved = await scheduleRepository.save(created);

    return {
      created: saved.length,
      schedules: saved,
    };
  }

  async filterSchedules(userId: string, filters: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    let query: any = { creator_id: userId };

    if (filters.type) query.type = filters.type;
    if (filters.status) query.status = filters.status;
    if (filters.category_id) query.category_id = filters.category_id;

    if (filters.startDate && filters.endDate) {
      query.deadline = { between: [filters.startDate, filters.endDate] };
    }

    if (filters.priority) query.priority = filters.priority;

    const schedules = await scheduleRepository.find({
      where: query,
      order: { deadline: "ASC" },
      take: filters.limit || 50,
      skip: filters.offset || 0,
    });

    return {
      count: schedules.length,
      schedules,
      filters,
    };
  }

  async searchSchedules(userId: string, query: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const schedules = await scheduleRepository.find({
      where: [
        { creator_id: userId, title: { like: `%${query}%` } },
        { creator_id: userId, description: { like: `%${query}%` } },
      ],
      take: 50,
    });

    return {
      query,
      count: schedules.length,
      schedules,
    };
  }

  async cloneSchedule(scheduleId: string, userId: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const original = await scheduleRepository.findOne({ where: { id: scheduleId } });
    if (!original) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    const cloned = scheduleRepository.create({
      id: generateUUID(),
      ...original,
      title: `${original.title} (Copy)`,
      created_at: new Date(),
    });

    await scheduleRepository.save(cloned);

    return {
      originalId: scheduleId,
      clonedId: cloned.id,
      cloned,
    };
  }

  async exportSchedules(userId: string, format: string = "json"): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const schedules = await scheduleRepository.find({
      where: { creator_id: userId },
    });

    const exportData = {
      format,
      exportedAt: new Date(),
      totalSchedules: schedules.length,
      data: schedules,
    };

    if (format === "csv") {
      const csv = [
        ["ID", "Title", "Type", "Status", "Deadline", "Priority"].join(","),
        ...schedules.map(s =>
          [s.id, s.title, s.type, s.status, s.deadline, s.priority].join(",")
        ),
      ].join("\n");

      return {
        format: "csv",
        fileName: `schedules_${Date.now()}.csv`,
        data: csv,
      };
    }

    return {
      format: "json",
      fileName: `schedules_${Date.now()}.json`,
      data: exportData,
    };
  }

  async archiveSchedules(userId: string, olderThanDays: number = 30): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - olderThanDays);

    const schedules = await scheduleRepository.find({
      where: {
        creator_id: userId,
        status: "DONE",
        updated_at: { lessThan: cutoffDate },
      },
    });

    const archived = schedules.map(s => {
      s.is_archived = true;
      s.archived_at = new Date();
      return s;
    });

    await scheduleRepository.save(archived);

    return {
      archivedCount: archived.length,
      archivedBefore: cutoffDate,
    };
  }

  async updateScheduleStatus(scheduleId: string, status: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const validStatuses = ["PENDING", "DOING", "DONE", "CANCELLED"];
    if (!validStatuses.includes(status)) {
      throw new AppError(400, "Invalid status", "INVALID_STATUS");
    }

    const schedule = await scheduleRepository.findOne({ where: { id: scheduleId } });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    schedule.status = status;
    schedule.updated_at = new Date();

    if (status === "DONE") {
      schedule.completed_at = new Date();
    }

    return scheduleRepository.save(schedule);
  }

  async bulkDeleteSchedules(userId: string, scheduleIds: string[]): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const schedules = await scheduleRepository.find({
      where: {
        id: { in: scheduleIds },
        creator_id: userId,
      },
    });

    await scheduleRepository.remove(schedules);

    return {
      deletedCount: schedules.length,
      deletedIds: scheduleIds,
    };
  }
}

export default new ScheduleService();
