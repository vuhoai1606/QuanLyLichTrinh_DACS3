import adminService from "@services/AdminService";
import { successResponse, errorResponse } from "@utils/errors";

export class AdminController {
  // Get all users
  async getAllUsers(query: any) {
    const { limit = 20, offset = 0 } = query;

    try {
      const users = await adminService.getAllUsers(parseInt(limit), parseInt(offset));
      return successResponse(users, "Users list retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get user details
  async getUserDetails(userId: string) {
    try {
      const user = await adminService.getUserDetails(userId);

      if (!user) {
        return errorResponse(404, "User not found", "USER_NOT_FOUND");
      }

      return successResponse(user, "User details retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Change user status
  async changeUserStatus(userId: string, body: any) {
    const { status } = body;

    if (!status) {
      return errorResponse(400, "status required", "MISSING_FIELDS");
    }

    try {
      const user = await adminService.changeUserStatus(userId, status);
      return successResponse(user, "User status updated");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get system statistics
  async getSystemStatistics() {
    try {
      const stats = await adminService.getSystemStatistics();
      return successResponse(stats, "System statistics retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Trigger maintenance
  async triggerMaintenance(body: any) {
    const { action } = body;

    if (!action) {
      return errorResponse(400, "action required", "MISSING_FIELDS");
    }

    try {
      const result = await adminService.triggerMaintenance(action);
      return successResponse(result, "Maintenance action triggered");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get system logs
  async getSystemLogs(query: any) {
    const { limit = 100 } = query;

    try {
      const logs = await adminService.getSystemLogs(parseInt(limit));
      return successResponse(logs, "System logs retrieved");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Search users
  async searchUsers(query: any) {
    const { q, limit = 20 } = query;

    if (!q) {
      return errorResponse(400, "q (search query) required", "MISSING_FIELDS");
    }

    try {
      const results = await adminService.searchUsers(q, parseInt(limit));
      return successResponse(results, "Search results");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Reset user password
  async resetUserPassword(userId: string, body: any) {
    const { newPassword } = body;

    if (!newPassword) {
      return errorResponse(400, "newPassword required", "MISSING_FIELDS");
    }

    try {
      const result = await adminService.resetUserPassword(userId, newPassword);
      return successResponse(result, "User password reset");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Send bulk notification
  async sendBulkNotification(body: any) {
    const { user_ids, notification } = body;

    if (!user_ids || !Array.isArray(user_ids) || !notification) {
      return errorResponse(400, "user_ids array and notification required", "MISSING_FIELDS");
    }

    try {
      const result = await adminService.sendBulkNotification(user_ids, notification);
      return successResponse(result, "Bulk notification sent");
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}

export default new AdminController();
