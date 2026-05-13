import scheduleService from "@services/ScheduleService";
import dashboardService from "@services/DashboardService";
import { successResponse, errorResponse, AppError } from "@utils/errors";

export class ScheduleController {
  // Get all schedules for user
  async getSchedules(userId: string) {
    if (!userId) {
      return errorResponse(400, "userId required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.getSchedulesForUser(userId, 100, 0);
      return successResponse(result.data, "Schedules retrieved", 200);
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get schedule by ID
  async getScheduleById(scheduleId: string) {
    if (!scheduleId) {
      return errorResponse(400, "scheduleId required", "MISSING_FIELDS");
    }

    try {
      const schedule = await scheduleService.getScheduleById(scheduleId);
      return successResponse(schedule, "Schedule retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Create category
  async createCategory(body: any) {
    const { user_id, name, hex_color } = body;

    if (!user_id || !name || !hex_color) {
      return errorResponse(400, "user_id, name and hex_color required", "MISSING_FIELDS");
    }

    try {
      const category = await scheduleService.createCategory(user_id, name, hex_color);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Category created successfully",
          data: category,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Create schedule
  async createSchedule(body: any) {
    const type = body.type || "TODO";
    const { creator_id, title, ...rest } = body;

    if (!creator_id || !title) {
      return errorResponse(400, "creator_id and title required", "MISSING_FIELDS");
    }

    try {
      const schedule = await scheduleService.createSchedule({
        ...rest,
        creator_id,
        title,
        type,
      });

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Schedule created successfully",
          data: schedule,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Create recurring schedule
  async createRecurring(body: any) {
    const { creator_id, title, recurrence } = body;

    if (!creator_id || !title || !recurrence) {
      return errorResponse(400, "creator_id, title, and recurrence required", "MISSING_FIELDS");
    }

    try {
      const schedule = await scheduleService.createRecurringSchedule({
        creator_id,
        title,
        ...body,
      });

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Recurring schedule created",
          data: schedule,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Bulk create schedules
  async bulkCreate(body: any) {
    const { user_id, schedules } = body;

    if (!user_id || !schedules || !Array.isArray(schedules)) {
      return errorResponse(400, "user_id and schedules array required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.bulkCreateSchedules(user_id, schedules);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Schedules created in bulk",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Filter schedules
  async filterSchedules(body: any) {
    const { user_id } = body;

    if (!user_id) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.filterSchedules(user_id, body);
      return successResponse(result, "Filtered schedules retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Search schedules
  async searchSchedules(query: string, userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.searchSchedules(userId, query);
      return successResponse(result, "Search results");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Clone schedule
  async cloneSchedule(scheduleId: string, userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.cloneSchedule(scheduleId, userId);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Schedule cloned successfully",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Export schedules
  async exportSchedules(userId: string, format: string = "json") {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.exportSchedules(userId, format);
      return successResponse(result, "Schedules exported");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Archive schedules
  async archiveSchedules(body: any) {
    const { user_id, days } = body;

    if (!user_id) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.archiveSchedules(user_id, days);
      return successResponse(result, "Schedules archived");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Update status
  async updateStatus(scheduleId: string, body: any) {
    const { status } = body;

    if (!status) {
      return errorResponse(400, "status required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.updateScheduleStatus(scheduleId, status);
      return successResponse(result, "Schedule status updated");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Bulk delete
  async bulkDelete(body: any) {
    const { user_id, schedule_ids } = body;

    if (!user_id || !schedule_ids || !Array.isArray(schedule_ids)) {
      return errorResponse(400, "user_id and schedule_ids array required", "MISSING_FIELDS");
    }

    try {
      const result = await scheduleService.bulkDeleteSchedules(user_id, schedule_ids);
      return successResponse(result, "Schedules deleted");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Dashboard summary
  async getDashboardSummary(userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const summary = await dashboardService.getDashboardSummary(userId);
      return successResponse(summary, "Dashboard summary");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Weekly stats
  async getWeeklyStats(userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const stats = await dashboardService.getWeeklyStats(userId);
      return successResponse(stats, "Weekly statistics");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Monthly stats
  async getMonthlyStats(userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const stats = await dashboardService.getMonthlyStats(userId);
      return successResponse(stats, "Monthly statistics");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Weekly goal progress
  async getWeeklyGoalProgress(userId: string) {
    if (!userId) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const progress = await scheduleService.getWeeklyGoalProgress(userId);
      return successResponse(progress, "Weekly goal progress");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new ScheduleController();
