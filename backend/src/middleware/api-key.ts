import { logger } from "@utils/logger";
import { APP_CONSTANTS } from "@constants/app.constants";
import { errorResponse } from "@utils/errors";
import { v4 as uuidv4 } from "uuid";

/**
 * API Key configuration
 */
export interface ApiKeyConfig {
  key: string;
  name: string;
  permissions: string[];
  rateLimit?: number;
  createdAt: Date;
  expiresAt?: Date;
  lastUsed?: Date;
  isActive: boolean;
}

/**
 * API Key Store (In production, use database)
 */
class ApiKeyStore {
  private keys = new Map<string, ApiKeyConfig>();
  private keyIndex = new Map<string, string>(); // Map hashed key to original key

  /**
   * Generate new API key
   */
  generateKey(name: string, permissions: string[] = ["read"], expiresInDays?: number): string {
    const key = `bfy_${uuidv4().replace(/-/g, "")}`;
    const config: ApiKeyConfig = {
      key,
      name,
      permissions,
      createdAt: new Date(),
      isActive: true,
      rateLimit: 1000,
    };

    if (expiresInDays) {
      config.expiresAt = new Date(Date.now() + expiresInDays * 24 * 60 * 60 * 1000);
    }

    this.keys.set(key, config);
    this.keyIndex.set(this.hashKey(key), key);

    logger.info("API Key generated", { keyName: name, permissions });
    return key;
  }

  /**
   * Validate API key
   */
  validateKey(key: string): { valid: boolean; config?: ApiKeyConfig; error?: string } {
    // Check if key exists
    const config = this.keys.get(key);
    if (!config) {
      return { valid: false, error: "Invalid API key" };
    }

    // Check if key is active
    if (!config.isActive) {
      return { valid: false, error: "API key is inactive" };
    }

    // Check if key expired
    if (config.expiresAt && new Date() > config.expiresAt) {
      return { valid: false, error: "API key expired" };
    }

    // Update last used
    config.lastUsed = new Date();

    return { valid: true, config };
  }

  /**
   * Check if key has permission
   */
  hasPermission(key: string, permission: string): boolean {
    const config = this.keys.get(key);
    if (!config) return false;
    return config.permissions.includes(permission) || config.permissions.includes("*");
  }

  /**
   * Revoke API key
   */
  revokeKey(key: string): boolean {
    const config = this.keys.get(key);
    if (!config) return false;
    config.isActive = false;
    logger.info("API Key revoked", { keyName: config.name });
    return true;
  }

  /**
   * Get all keys (excluding actual key values)
   */
  getAllKeys() {
    return Array.from(this.keys.values()).map((config) => ({
      name: config.name,
      permissions: config.permissions,
      createdAt: config.createdAt,
      expiresAt: config.expiresAt,
      lastUsed: config.lastUsed,
      isActive: config.isActive,
    }));
  }

  /**
   * Hash key for safe storage
   */
  private hashKey(key: string): string {
    return key.split("_")[1]?.substring(0, 8) || key;
  }
}

export const apiKeyStore = new ApiKeyStore();

/**
 * API Key middleware - Validate API key from headers
 */
export const apiKeyMiddleware = (ctx: any, allowedPermissions?: string[]): Response | null => {
  const apiKey = ctx.request.headers.get("x-api-key") ||
    ctx.request.headers.get("authorization")?.replace("Bearer ", "");

  if (!apiKey) {
    logger.warn("Missing API key", { path: ctx.request.url });
    return errorResponse(
      APP_CONSTANTS.HTTP.UNAUTHORIZED,
      "API key required",
      APP_CONSTANTS.ERROR_CODES.MISSING_TOKEN
    );
  }

  const validation = apiKeyStore.validateKey(apiKey);
  if (!validation.valid) {
    logger.warn("Invalid API key", { error: validation.error });
    return errorResponse(
      APP_CONSTANTS.HTTP.UNAUTHORIZED,
      validation.error || "Invalid API key",
      APP_CONSTANTS.ERROR_CODES.INVALID_TOKEN
    );
  }

  // Check permissions if specified
  if (allowedPermissions && allowedPermissions.length > 0) {
    const hasPermission = allowedPermissions.some((perm) =>
      apiKeyStore.hasPermission(apiKey, perm)
    );

    if (!hasPermission) {
      logger.warn("Insufficient permissions", {
        key: validation.config?.name,
        required: allowedPermissions,
        granted: validation.config?.permissions,
      });
      return errorResponse(
        APP_CONSTANTS.HTTP.FORBIDDEN,
        "Insufficient permissions",
        APP_CONSTANTS.ERROR_CODES.FORBIDDEN
      );
    }
  }

  // Store in context for later use
  ctx.state = ctx.state || {};
  ctx.state.apiKey = apiKey;
  ctx.state.apiKeyConfig = validation.config;

  logger.debug("API key validated", { keyName: validation.config?.name });
  return null; // null means validation passed
};

/**
 * API Key information helper
 */
export const getApiKeyInfo = (key: string) => {
  const validation = apiKeyStore.validateKey(key);
  if (!validation.config) return null;

  return {
    name: validation.config.name,
    permissions: validation.config.permissions,
    createdAt: validation.config.createdAt,
    expiresAt: validation.config.expiresAt,
    lastUsed: validation.config.lastUsed,
    isActive: validation.config.isActive,
    isExpired: validation.config.expiresAt ? new Date() > validation.config.expiresAt : false,
  };
};

/**
 * Test API Keys (for development/testing)
 */
export const initializeTestKeys = () => {
  // Create test keys for different use cases
  apiKeyStore.generateKey("Frontend App", ["read", "write"], 365);
  apiKeyStore.generateKey("Mobile App", ["read", "write"], 365);
  apiKeyStore.generateKey("Analytics Service", ["read"], 365);
  apiKeyStore.generateKey("Admin Panel", ["*"], 365);

  logger.info("✅ Test API keys initialized");
  logger.info("Available keys:", apiKeyStore.getAllKeys());
};
