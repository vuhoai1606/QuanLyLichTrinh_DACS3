import reportService from "@services/ReportService";
import { successResponse, errorResponse } from "@utils/errors";

export class ReportController {
  // Get productivity report
  async getProductivityReport(query: any) {
    const { user_id, start_date, end_date } = query;

    if (!user_id || !start_date || !end_date) {
      return errorResponse(400, "user_id, start_date, and end_date required", "MISSING_FIELDS");
    }

    try {
      const report = await reportService.getProductivityReport(user_id, start_date, end_date);
      return successResponse(report, "Productivity report retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get focus analysis
  async getFocusAnalysis(query: any) {
    const { user_id, start_date, end_date } = query;

    if (!user_id || !start_date || !end_date) {
      return errorResponse(400, "user_id, start_date, and end_date required", "MISSING_FIELDS");
    }

    try {
      const analysis = await reportService.getFocusAnalysis(user_id, start_date, end_date);
      return successResponse(analysis, "Focus analysis retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get task completion rate
  async getTaskCompletionRate(query: any) {
    const { user_id, start_date, end_date } = query;

    if (!user_id || !start_date || !end_date) {
      return errorResponse(400, "user_id, start_date, and end_date required", "MISSING_FIELDS");
    }

    try {
      const rate = await reportService.getTaskCompletionRate(user_id, start_date, end_date);
      return successResponse(rate, "Task completion rate retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get EXP history
  async getExpHistory(userId: string) {
    try {
      const history = await reportService.getExpHistory(userId);
      return successResponse(history, "EXP history retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Export report
  async exportReport(body: any) {
    const { user_id, format = "json", report_type } = body;

    if (!user_id || !report_type) {
      return errorResponse(400, "user_id and report_type required", "MISSING_FIELDS");
    }

    try {
      const exported = await reportService.exportReport(user_id, format, report_type);
      return successResponse(exported, "Report exported");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Compare with previous period
  async compareWithPreviousPeriod(query: any) {
    const { user_id, report_type = "productivity" } = query;

    if (!user_id) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const comparison = await reportService.compareWithPreviousPeriod(user_id, report_type);
      return successResponse(comparison, "Period comparison retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get health score
  async getHealthScore(userId: string) {
    try {
      const score = await reportService.getHealthScore(userId);
      return successResponse(score, "Health score retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}

export default new ReportController();
