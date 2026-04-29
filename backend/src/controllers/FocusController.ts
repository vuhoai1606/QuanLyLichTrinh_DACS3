import focusService from "@services/FocusService";
import { successResponse, errorResponse, AppError } from "@utils/errors";

export class FocusController {
  // Create focus session
  async createSession(body: any) {
    const { user_id, duration_minutes, status } = body;

    if (!user_id || !duration_minutes || !status) {
      return errorResponse(400, "user_id, duration_minutes and status required", "MISSING_FIELDS");
    }

    try {
      const session = await focusService.createFocusSession(user_id, duration_minutes, status);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Focus session created successfully",
          data: session,
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

  // Get history
  async getHistory(userId: string, limit: number = 50) {
    try {
      const history = await focusService.getFocusHistory(userId, limit);
      return successResponse(history, "Focus history retrieved successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get stats
  async getStats(userId: string) {
    try {
      const stats = await focusService.getFocusStats(userId);
      return successResponse(stats, "Focus stats retrieved successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get today's summary
  async getTodaysSummary(userId: string) {
    try {
      const summary = await focusService.getTodaysSummary(userId);
      return successResponse(summary, "Today's focus summary retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get streak
  async getStreak(userId: string) {
    try {
      const streak = await focusService.getStreak(userId);
      return successResponse(streak, "Focus streak retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Pause session
  async pauseSession(sessionId: string) {
    if (!sessionId) {
      return errorResponse(400, "sessionId required", "MISSING_FIELDS");
    }

    try {
      const result = await focusService.pauseSession(sessionId);
      return successResponse(result, "Focus session paused");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Resume session
  async resumeSession(sessionId: string) {
    if (!sessionId) {
      return errorResponse(400, "sessionId required", "MISSING_FIELDS");
    }

    try {
      const result = await focusService.resumeSession(sessionId);
      return successResponse(result, "Focus session resumed");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Bulk create sessions
  async bulkCreateSessions(body: any) {
    const { user_id, sessions } = body;

    if (!user_id || !sessions || !Array.isArray(sessions)) {
      return errorResponse(400, "user_id and sessions array required", "MISSING_FIELDS");
    }

    try {
      const result = await focusService.bulkCreateSessions(user_id, sessions);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Focus sessions created in bulk",
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

  // Compare with other users
  async compareWithOtherUsers(userId: string, metric: string = "totalMinutes") {
    try {
      const comparison = await focusService.compareWithOtherUsers(userId, metric);
      return successResponse(comparison, "User comparison retrieved");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new FocusController();
