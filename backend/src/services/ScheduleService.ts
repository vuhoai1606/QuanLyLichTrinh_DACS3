import { AppDataSource } from "@config/database";
import { Like, ILike } from "typeorm";
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

  async getCategoriesForUser(userId: string): Promise<any[]> {
    const categoryRepository = AppDataSource.getRepository("Category");
    return categoryRepository.find({ where: { user_id: userId } });
  }

  async getOrCreateCategory(userId: string, name: string, hexColor?: string): Promise<any> {
    const categoryRepository = AppDataSource.getRepository("Category");
    let category = await categoryRepository.findOne({ where: { user_id: userId, name } });
    if (!category) {
      let finalColor = hexColor || "#AD7BFF";
      if (!isValidHexColor(finalColor)) {
        finalColor = "#AD7BFF";
      }
      category = categoryRepository.create({
        id: generateUUID(),
        user_id: userId,
        name,
        hex_color: finalColor,
      });
      await categoryRepository.save(category);
    }
    return category;
  }

  async createSchedule(data: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const reminderRepository = AppDataSource.getRepository("Reminder");
    
    if (data.type === "TODO" && !data.title) {
      throw new AppError(400, "Title is required", "MISSING_TITLE");
    }



    if (data.type === "EVENT") {
      const startTime = data.start_time || data.start_date;
      const endTime = data.end_time || data.end_date;

      if (!startTime || !endTime) {
        throw new AppError(400, "Start time and end time required for EVENT", "MISSING_TIME");
      }
      if (new Date(endTime) <= new Date(startTime)) {
        throw new AppError(400, "End time must be after start time", "INVALID_TIME");
      }
    }

    const { reminders, collaborators, assignees, id, ...scheduleData } = data;
    const finalId = (id && id !== "") ? id : generateUUID();

    let resolvedCategoryId = data.category_id || null;
    if (data.category_name) {
      const category = await this.getOrCreateCategory(data.creator_id, data.category_name, data.category_color);
      resolvedCategoryId = category.id;
    }

    const schedule = scheduleRepository.create({
      ...scheduleData,
      id: finalId,
      creator_id: data.creator_id,
      category_id: resolvedCategoryId,
      start_time: data.start_time || data.start_date,
      end_time: data.end_time || data.end_date,
      status: "PENDING",
    });

    const savedSchedule = await scheduleRepository.save(schedule);

    // Save reminders if provided
    if (reminders && Array.isArray(reminders)) {
      const reminderEntities = reminders.map((r: any) => 
        reminderRepository.create({
          id: generateUUID(),
          schedule_id: savedSchedule.id,
          trigger_type: r.trigger_type || "MIN_15",
          is_alarm: r.is_alarm || false,
          custom_time: r.custom_time,
        })
      );
      await reminderRepository.save(reminderEntities);
    }

    // Save collaborators if provided
    if (collaborators && Array.isArray(collaborators)) {
      const collaboratorRepository = AppDataSource.getRepository("TaskCollaborator");
      const collaboratorEntities = collaborators.map((userId: string) => 
        collaboratorRepository.create({
          schedule_id: savedSchedule.id,
          user_id: userId,
          permission_level: "VIEW",
        })
      );
      await collaboratorRepository.save(collaboratorEntities);
    }

    // Save assignees if provided
    if (assignees && Array.isArray(assignees)) {
      const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");
      const assignmentEntities = assignees.map((userId: string) =>
        assignmentRepository.create({
          schedule_id: savedSchedule.id,
          assignee_id: userId,
          assign_status: "PENDING",
        })
      );
      await assignmentRepository.save(assignmentEntities);
    }

    // Notify group members if it's an ANNOUNCEMENT in a group
    if (data.type === "ANNOUNCEMENT" && data.group_id) {
      try {
        const groupMemberRepository = AppDataSource.getRepository("GroupMember");
        const notificationRepository = AppDataSource.getRepository("Notification");

        const members = await groupMemberRepository.find({
          where: { group_id: data.group_id },
          relations: ["user"]
        });

        const notifications = members
          .filter(m => m.user_id !== data.creator_id) // Don't notify the creator
          .map(m => notificationRepository.create({
            id: generateUUID(),
            user_id: m.user_id,
            sender_id: data.creator_id,
            type: "GROUP_TASK",
            title: `New Announcement in Group`,
            message: `An announcement "${savedSchedule.title}" was created.`,
            related_id: savedSchedule.id,
            ia_read: false
          }));

        if (notifications.length > 0) {
          await notificationRepository.save(notifications);
        }
      } catch (err) {
        console.error("Failed to send group announcement notifications:", err);
      }
    }

    return this.getScheduleById(savedSchedule.id);
  }

  async getSchedulesForUser(userId: string, limit: number = 100, offset: number = 0): Promise<any> {
    try {
      const scheduleRepository = AppDataSource.getRepository("Schedule");
      const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");
      const { In, IsNull } = require("typeorm");

      const assignments = await assignmentRepository.find({ where: { assignee_id: userId } });
      const assignedScheduleIds = assignments.map(a => a.schedule_id);

      const whereClause = assignedScheduleIds.length > 0 
        ? [ { creator_id: userId, group_id: IsNull() }, { id: In(assignedScheduleIds), group_id: IsNull() } ]
        : { creator_id: userId, group_id: IsNull() };

      const [schedules, total] = await scheduleRepository.findAndCount({
        where: whereClause,
        relations: ["category", "reminders", "assignments"],
        order: { created_at: "DESC" },
        skip: offset,
        take: limit,
      });

      return {
        data: schedules.map((s: any) => ({
          id: s.id,
          title: s.title,
          description: s.description,
          type: s.type,
          status: s.status,
          priority: s.priority,
          start_time: s.start_time,
          end_time: s.end_time,
          deadline: s.deadline,
          creator_id: s.creator_id,
          created_at: s.created_at,
          is_all_day: s.is_all_day,
          rrule: s.rrule,
          is_recurring: s.is_recurring,
          recurrence_type: s.recurrence_type,
          is_countdown_enabled: s.is_countdown_enabled,
          category_name: s.category?.name ?? null,
          category_color: s.category?.hex_color ?? null,
          reminders: s.reminders ?? [],
          external_id: s.external_id,
          external_source: s.external_source,
          updated_at: s.updated_at,
        })),
        total,
        limit,
        offset,
      };
    } catch (error) {
      console.error("Error in getSchedulesForUser:", error);
      throw error;
    }
  }

  async getTimelineForUser(userId: string, startDate: Date, endDate: Date): Promise<any[]> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const assignmentRepository = AppDataSource.getRepository("ScheduleAssignment");
    const { In, IsNull } = require("typeorm");

    const assignments = await assignmentRepository.find({ where: { assignee_id: userId } });
    const assignedScheduleIds = assignments.map(a => a.schedule_id);

    const whereClause = assignedScheduleIds.length > 0 
      ? [ { creator_id: userId, group_id: IsNull() }, { id: In(assignedScheduleIds), group_id: IsNull() } ]
      : { creator_id: userId, group_id: IsNull() };

    const schedules = await scheduleRepository.find({
      where: whereClause,
      relations: ["category", "reminders", "assignments"],
      order: { start_time: "DESC", deadline: "DESC", created_at: "DESC" },
    });

    return schedules.filter((s: any) => {
      const compareDate = s.start_time || s.deadline || s.created_at;
      return new Date(compareDate) >= new Date(startDate) && new Date(compareDate) <= new Date(endDate);
    });
  }

  async getScheduleById(scheduleId: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");

    const schedule = await scheduleRepository.findOne({
      where: { id: scheduleId },
      relations: ["category", "reminders", "collaborators", "collaborators.user"],
    });

    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    return {
      id: schedule.id,
      title: schedule.title,
      description: schedule.description,
      location: schedule.location,
      type: schedule.type,
      status: schedule.status,
      priority: (schedule as any).priority,
      start_time: schedule.start_time,
      end_time: schedule.end_time,
      deadline: schedule.deadline,
      category_name: schedule.category?.name ?? null,
      category_color: schedule.category?.hex_color ?? null,
      creator_id: schedule.creator_id,
      group_id: schedule.group_id,
      reminders: schedule.reminders,
      collaborators: schedule.collaborators?.map((c: any) => ({
        id: c.user.id,
        full_name: c.user.full_name,
        avatar_url: c.user.avatar_url,
      })),
      is_all_day: schedule.is_all_day,
      rrule: schedule.rrule,
      is_recurring: schedule.is_recurring,
      recurrence_type: schedule.recurrence_type,
      is_countdown_enabled: schedule.is_countdown_enabled,
      created_at: schedule.created_at,
      external_id: schedule.external_id,
      external_source: schedule.external_source,
      updated_at: schedule.updated_at,
    };
  }

  async updateSchedule(scheduleId: string, userId: string, updates: any): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const reminderRepository = AppDataSource.getRepository("Reminder");
    
    const schedule = await scheduleRepository.findOne({ where: { id: scheduleId }, relations: ["reminders"] });
    if (!schedule) {
      throw new AppError(404, "Schedule not found", "SCHEDULE_NOT_FOUND");
    }

    const isAssignee = await AppDataSource.getRepository("ScheduleAssignment").findOne({
      where: { schedule_id: scheduleId, assignee_id: userId }
    });

    if (schedule.creator_id !== userId && !isAssignee) {
      throw new AppError(403, "Unauthorized to update this schedule", "UNAUTHORIZED");
    }

    const { reminders, ...otherUpdates } = updates;
    const oldStatus = schedule.status;
    
    let resolvedCategoryId = otherUpdates.category_id !== undefined ? otherUpdates.category_id : schedule.category_id;
    if (otherUpdates.category_name) {
      const category = await this.getOrCreateCategory(userId, otherUpdates.category_name, otherUpdates.category_color);
      resolvedCategoryId = category.id;
    } else if (otherUpdates.category_name === null) {
      resolvedCategoryId = null;
    }

    let timeChanged = false;
    if (otherUpdates.start_time !== undefined) {
      const newTime = otherUpdates.start_time ? new Date(otherUpdates.start_time).getTime() : null;
      const oldTime = schedule.start_time ? new Date(schedule.start_time).getTime() : null;
      if (newTime !== oldTime) timeChanged = true;
    }
    if (otherUpdates.end_time !== undefined) {
      const newTime = otherUpdates.end_time ? new Date(otherUpdates.end_time).getTime() : null;
      const oldTime = schedule.end_time ? new Date(schedule.end_time).getTime() : null;
      if (newTime !== oldTime) timeChanged = true;
    }
    if (otherUpdates.deadline !== undefined) {
      const newTime = otherUpdates.deadline ? new Date(otherUpdates.deadline).getTime() : null;
      const oldTime = schedule.deadline ? new Date(schedule.deadline).getTime() : null;
      if (newTime !== oldTime) timeChanged = true;
    }

    if (timeChanged && oldStatus === "DONE") {
      otherUpdates.status = "PENDING";
    }

    Object.assign(schedule, { ...otherUpdates, category_id: resolvedCategoryId });

    if (otherUpdates.status === "DONE" && oldStatus !== "DONE") {
      schedule.completed_at = new Date();
      // Award EXP for completion
      try {
        const GamificationService = (await import("./GamificationService")).default;
        await GamificationService.updateUserStreakAndRank(schedule.creator_id, 10);
      } catch (expError) {
        console.error("Failed to award EXP in updateSchedule:", expError);
      }
    }

    // Handle reminders update explicitly to avoid duplicates/orphans
    if (reminders !== undefined) {
      // Remove old reminders
      if (schedule.reminders && schedule.reminders.length > 0) {
        await reminderRepository.remove(schedule.reminders);
      }
      
      // Add new reminders
      if (Array.isArray(reminders)) {
        schedule.reminders = reminders.map((r: any) => 
          reminderRepository.create({
            id: generateUUID(),
            schedule_id: scheduleId,
            trigger_type: r.trigger_type || "MIN_15",
            is_alarm: r.is_alarm || false,
            custom_time: r.custom_time,
          })
        );
      } else {
        schedule.reminders = [];
      }
    }

    await scheduleRepository.save(schedule);
    return this.getScheduleById(scheduleId);
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

    let query: any = { creator_id: userId, group_id: require("typeorm").IsNull() };

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
        { creator_id: userId, title: ILike(`%${query}%`) },
        { creator_id: userId, description: ILike(`%${query}%`) },
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
      // Award EXP for completion
      try {
        const GamificationService = (await import("./GamificationService")).default;
        await GamificationService.updateUserStreakAndRank(schedule.creator_id, 10);
      } catch (expError) {
        console.error("Failed to award EXP:", expError);
      }
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
  async getWeeklyGoalProgress(userId: string): Promise<any> {
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    const today = new Date();
    const weekStart = new Date(today);
    weekStart.setDate(weekStart.getDate() - weekStart.getDay());
    weekStart.setHours(0, 0, 0, 0);

    const completedThisWeek = await scheduleRepository.count({
      where: {
        creator_id: userId,
        status: "DONE",
        updated_at: { moreThanOrEqual: weekStart } as any,
      },
    });

    const settings = await userSettingsRepository.findOne({ where: { user_id: userId } });
    const goal = settings?.weekly_task_goal || 10;

    return {
      completed: completedThisWeek,
      total: goal,
      percent: Math.min(100, Math.round((completedThisWeek / goal) * 100)),
    };
  }
}

export default new ScheduleService();
