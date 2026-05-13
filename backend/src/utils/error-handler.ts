import { APP_CONSTANTS } from "@constants/app.constants";
import { logger } from "@utils/logger";

/**
 * Comprehensive error handler for try-catch blocks
 */
export class ErrorHandler {
  /**
   * Handle service/database errors
   */
  static handleServiceError(error: any, context: string = "Service"): AppError {
    logger.error(`${context} Error:`, error instanceof Error ? error : new Error(String(error)));

    if (error instanceof AppError) {
      return error;
    }

    // Database errors
    if (error?.code === "ER_DUP_ENTRY" || error?.code === "23505") {
      return new AppError(
        APP_CONSTANTS.HTTP.CONFLICT,
        "Duplicate entry found",
        APP_CONSTANTS.ERROR_CODES.CONFLICT || "DUPLICATE_ENTRY"
      );
    }

    if (error?.code === "ER_NO_REFERENCED_ROW" || error?.code === "23503") {
      return new AppError(
        APP_CONSTANTS.HTTP.BAD_REQUEST,
        "Referenced record not found",
        APP_CONSTANTS.ERROR_CODES.VALIDATION_ERROR
      );
    }

    // Timeout errors
    if (error?.code === "ETIMEDOUT" || error?.code === "ECONNREFUSED") {
      return new AppError(
        APP_CONSTANTS.HTTP.SERVICE_UNAVAILABLE,
        "Service temporarily unavailable",
        APP_CONSTANTS.ERROR_CODES.SERVICE_UNAVAILABLE
      );
    }

    // Generic error
    return new AppError(
      APP_CONSTANTS.HTTP.INTERNAL_ERROR,
      "An unexpected error occurred",
      APP_CONSTANTS.ERROR_CODES.INTERNAL_ERROR
    );
  }

  /**
   * Validate input and return errors
   */
  static validateInput(data: any, schema: Record<string, any>): string[] {
    const errors: string[] = [];

    for (const [field, rules] of Object.entries(schema)) {
      const value = data[field];

      // Required validation
      if (rules.required && !value) {
        errors.push(`${field} is required`);
        continue;
      }

      if (!value) continue;

      // Type validation
      if (rules.type && typeof value !== rules.type) {
        errors.push(`${field} must be of type ${rules.type}`);
      }

      // Min length
      if (rules.minLength && value.length < rules.minLength) {
        errors.push(`${field} must be at least ${rules.minLength} characters`);
      }

      // Max length
      if (rules.maxLength && value.length > rules.maxLength) {
        errors.push(`${field} must not exceed ${rules.maxLength} characters`);
      }

      // Pattern (regex)
      if (rules.pattern && !rules.pattern.test(value)) {
        errors.push(`${field} format is invalid`);
      }

      // Enum
      if (rules.enum && !rules.enum.includes(value)) {
        errors.push(`${field} must be one of: ${rules.enum.join(", ")}`);
      }
    }

    return errors;
  }

  /**
   * Wrap async function with error handling
   */
  static async handleAsync<T>(
    fn: () => Promise<T>,
    context: string = "Operation"
  ): Promise<T | null> {
    try {
      return await fn();
    } catch (error) {
      throw this.handleServiceError(error, context);
    }
  }
}

/**
 * Custom App Error
 */
export class AppError extends Error {
  constructor(
    public status: number,
    message: string,
    public code?: string
  ) {
    super(message);
    Object.setPrototypeOf(this, AppError.prototype);
  }
}

/**
 * Create error response
 */
export const errorResponse = (status: number, message: string, code?: string) => {
  return new Response(
    JSON.stringify({
      status,
      success: false,
      message,
      code: code || "ERROR",
      data: null,
    }),
    { status, headers: { "Content-Type": "application/json" } }
  );
};

/**
 * Create success response
 */
export const successResponse = (data: any, message: string = "Success", httpStatus: number = 200) => {
  return new Response(
    JSON.stringify({
      status: httpStatus,
      success: true,
      message,
      data,
    }),
    { status: httpStatus, headers: { "Content-Type": "application/json" } }
  );
};

/**
 * Validation result
 */
export interface ValidationResult {
  valid: boolean;
  errors?: string[];
  data?: any;
}

/**
 * Create validation error response
 */
export const validationError = (errors: string[]): ValidationResult => {
  return {
    valid: false,
    errors,
  };
};

/**
 * Create validation success response
 */
export const validationSuccess = (data: any): ValidationResult => {
  return {
    valid: true,
    data,
  };
};
