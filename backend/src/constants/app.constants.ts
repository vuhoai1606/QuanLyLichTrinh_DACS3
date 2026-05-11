/**
 * Application Constants
 * Centralized constants to avoid magic strings
 */

export const APP_CONSTANTS = {
  // HTTP Status Codes
  HTTP: {
    OK: 200,
    CREATED: 201,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    CONFLICT: 409,
    UNPROCESSABLE: 422,
    RATE_LIMITED: 429,
    INTERNAL_ERROR: 500,
    SERVICE_UNAVAILABLE: 503,
  },

  // Error Codes
  ERROR_CODES: {
    // Auth
    INVALID_CREDENTIALS: "INVALID_CREDENTIALS",
    MISSING_TOKEN: "MISSING_TOKEN",
    INVALID_TOKEN: "INVALID_TOKEN",
    TOKEN_EXPIRED: "TOKEN_EXPIRED",
    EMAIL_EXISTS: "EMAIL_EXISTS",
    USER_NOT_FOUND: "USER_NOT_FOUND",
    UNAUTHORIZED: "UNAUTHORIZED",
    FORBIDDEN: "FORBIDDEN",
    CONFLICT: "CONFLICT",

    // Validation
    MISSING_FIELDS: "MISSING_FIELDS",
    INVALID_EMAIL: "INVALID_EMAIL",
    INVALID_PASSWORD: "INVALID_PASSWORD",
    VALIDATION_ERROR: "VALIDATION_ERROR",

    // Rate Limiting
    RATE_LIMIT_EXCEEDED: "RATE_LIMIT_EXCEEDED",

    // Server
    INTERNAL_ERROR: "INTERNAL_ERROR",
    NOT_FOUND: "NOT_FOUND",
    SERVICE_UNAVAILABLE: "SERVICE_UNAVAILABLE",
  },

  // Password Rules
  PASSWORD: {
    MIN_LENGTH: 8,
    REQUIRE_UPPERCASE: true,
    REQUIRE_LOWERCASE: true,
    REQUIRE_NUMBERS: true,
    REQUIRE_SPECIAL_CHARS: false,
  },

  // User
  USER: {
    NAME_MIN_LENGTH: 2,
    NAME_MAX_LENGTH: 100,
    BIO_MAX_LENGTH: 280,
    DEFAULT_TIMEZONE: "UTC",
    DEFAULT_LANGUAGE: "en",
    DEFAULT_THEME: "SYSTEM",
  },

  // Schedule
  SCHEDULE: {
    TYPES: ["TODO", "TASK", "EVENT"],
    STATUSES: ["PENDING", "DOING", "DONE"],
    PRIORITIES: ["LOW", "MEDIUM", "HIGH"],
  },

  // JWT
  JWT: {
    DEFAULT_EXPIRY: "7d",
    DEFAULT_REFRESH_EXPIRY: "30d",
  },

  // Pagination
  PAGINATION: {
    DEFAULT_PAGE: 1,
    DEFAULT_LIMIT: 20,
    MAX_LIMIT: 100,
  },

  // Cache
  CACHE: {
    USER_PROFILE_TTL: 5 * 60, // 5 minutes
    SCHEDULE_LIST_TTL: 10 * 60, // 10 minutes
  },

  // Email
  EMAIL: {
    FROM: "noreply@bfy.com",
    VERIFICATION_EXPIRY: 24 * 60 * 60, // 24 hours
    PASSWORD_RESET_EXPIRY: 1 * 60 * 60, // 1 hour
  },

  // Dates
  DATES: {
    DATE_FORMAT: "YYYY-MM-DD",
    TIME_FORMAT: "HH:mm:ss",
    DATETIME_FORMAT: "YYYY-MM-DD HH:mm:ss",
  },
};

// Role & Permission Constants
export const ROLES = {
  ADMIN: "admin",
  USER: "user",
  MODERATOR: "moderator",
};

export const PERMISSIONS = {
  // Admin
  MANAGE_USERS: "manage_users",
  VIEW_LOGS: "view_logs",
  MANAGE_SYSTEM: "manage_system",

  // User
  READ_OWN_DATA: "read_own_data",
  UPDATE_OWN_DATA: "update_own_data",
  DELETE_OWN_DATA: "delete_own_data",
};

// Feature Flags
export const FEATURES = {
  GAMIFICATION_ENABLED: true,
  NOTIFICATIONS_ENABLED: true,
  COLLABORATION_ENABLED: true,
  REPORTS_ENABLED: true,
  EXPORT_ENABLED: true,
};
