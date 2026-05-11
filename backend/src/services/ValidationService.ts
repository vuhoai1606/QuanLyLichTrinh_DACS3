/**
 * Validation Service - Handles input validation for all endpoints
 * Ensures data integrity and security across the application
 */
class ValidationService {
  /**
   * Validate schedule creation/update
   */
  validateSchedule(data: any): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    // Required fields
    if (!data.title || typeof data.title !== "string" || data.title.trim().length === 0) {
      errors.push("Title is required and must be a non-empty string");
    }

    if (data.title && data.title.length > 255) {
      errors.push("Title must not exceed 255 characters");
    }

    if (data.description && data.description.length > 1000) {
      errors.push("Description must not exceed 1000 characters");
    }

    // Validate type
    const validTypes = ["TODO", "TASK", "EVENT"];
    if (data.type && !validTypes.includes(data.type)) {
      errors.push(`Type must be one of: ${validTypes.join(", ")}`);
    }

    // Validate priority
    const validPriorities = ["LOW", "MEDIUM", "HIGH"];
    if (data.priority && !validPriorities.includes(data.priority)) {
      errors.push(`Priority must be one of: ${validPriorities.join(", ")}`);
    }

    // Validate status
    const validStatuses = ["PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"];
    if (data.status && !validStatuses.includes(data.status)) {
      errors.push(`Status must be one of: ${validStatuses.join(", ")}`);
    }

    // Validate dates if provided
    if (data.start_date && isNaN(Date.parse(data.start_date))) {
      errors.push("Invalid start_date format");
    }

    if (data.due_date && isNaN(Date.parse(data.due_date))) {
      errors.push("Invalid due_date format");
    }

    return {
      valid: errors.length === 0,
      errors,
    };
  }

  /**
   * Validate focus session
   */
  validateFocusSession(data: any): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!data.user_id) {
      errors.push("user_id is required");
    }

    if (data.duration_minutes === undefined || data.duration_minutes === null) {
      errors.push("duration_minutes is required");
    } else if (typeof data.duration_minutes !== "number" || data.duration_minutes <= 0) {
      errors.push("duration_minutes must be a positive number");
    }

    if (data.duration_minutes > 480) {
      errors.push("duration_minutes cannot exceed 8 hours (480 minutes)");
    }

    const validStatuses = ["COMPLETED", "FAILED", "PAUSED"];
    if (data.status && !validStatuses.includes(data.status)) {
      errors.push(`Status must be one of: ${validStatuses.join(", ")}`);
    }

    return {
      valid: errors.length === 0,
      errors,
    };
  }

  /**
   * Validate group creation
   */
  validateGroup(data: any): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!data.group_name || typeof data.group_name !== "string" || data.group_name.trim().length === 0) {
      errors.push("group_name is required and must be non-empty");
    }

    if (data.group_name && data.group_name.length > 100) {
      errors.push("group_name must not exceed 100 characters");
    }

    if (data.description && data.description.length > 500) {
      errors.push("description must not exceed 500 characters");
    }

    if (data.max_members !== undefined) {
      if (typeof data.max_members !== "number" || data.max_members < 2) {
        errors.push("max_members must be a number >= 2");
      }
    }

    return {
      valid: errors.length === 0,
      errors,
    };
  }

  /**
   * Validate notification preferences
   */
  validateNotificationPreferences(data: any): { valid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (data.task_reminders !== undefined && typeof data.task_reminders !== "boolean") {
      errors.push("task_reminders must be boolean");
    }

    if (data.focus_notifications !== undefined && typeof data.focus_notifications !== "boolean") {
      errors.push("focus_notifications must be boolean");
    }

    if (data.group_invitations !== undefined && typeof data.group_invitations !== "boolean") {
      errors.push("group_invitations must be boolean");
    }

    if (data.achievement_notifications !== undefined && typeof data.achievement_notifications !== "boolean") {
      errors.push("achievement_notifications must be boolean");
    }

    return {
      valid: errors.length === 0,
      errors,
    };
  }

  /**
   * Validate email format
   */
  validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  /**
   * Validate password strength
   */
  validatePassword(password: string): { valid: boolean; message: string } {
    if (password.length < 8) {
      return { valid: false, message: "Password must be at least 8 characters long" };
    }

    if (!/[A-Z]/.test(password)) {
      return { valid: false, message: "Password must contain at least one uppercase letter" };
    }

    if (!/[a-z]/.test(password)) {
      return { valid: false, message: "Password must contain at least one lowercase letter" };
    }

    if (!/[0-9]/.test(password)) {
      return { valid: false, message: "Password must contain at least one digit" };
    }

    return { valid: true, message: "Password is valid" };
  }

  /**
   * Validate pagination parameters
   */
  validatePagination(page: any, limit: any): { valid: boolean; page: number; limit: number } {
    const defaultPage = 1;
    const defaultLimit = 20;
    const maxLimit = 100;

    let pageNum = parseInt(page) || defaultPage;
    let limitNum = parseInt(limit) || defaultLimit;

    pageNum = Math.max(1, pageNum);
    limitNum = Math.min(Math.max(1, limitNum), maxLimit);

    return {
      valid: true,
      page: pageNum,
      limit: limitNum,
    };
  }
}

export default new ValidationService();
