import { logger } from "@utils/logger";
import { v4 as uuidv4 } from "uuid";

/**
 * Request context - stores request metadata
 */
export interface RequestContext {
  requestId: string;
  startTime: number;
  method: string;
  path: string;
  ip: string;
  userAgent?: string;
  userId?: string;
}

/**
 * Store request contexts for tracing
 */
const requestContextMap = new Map<string, RequestContext>();

/**
 * Generate unique request ID
 */
export const generateRequestId = (): string => {
  return `req_${Date.now()}_${uuidv4().split("-")[0]}`;
};

/**
 * Get request context
 */
export const getRequestContext = (requestId: string): RequestContext | undefined => {
  return requestContextMap.get(requestId);
};

/**
 * Store request context
 */
export const storeRequestContext = (context: RequestContext): void => {
  requestContextMap.set(context.requestId, context);

  // Cleanup old requests after 30 minutes
  setTimeout(() => {
    requestContextMap.delete(context.requestId);
  }, 30 * 60 * 1000);
};

/**
 * Request ID middleware - Add request ID to every request
 */
export const requestIdMiddleware = (ctx: any) => {
  const requestId = generateRequestId();

  // Extract client IP (handles proxies)
  const ip =
    ctx.request.headers.get("x-forwarded-for")?.split(",")[0].trim() ||
    ctx.request.headers.get("x-real-ip") ||
    ctx.socket?.remoteAddress ||
    "unknown";

  // Create request context
  const context: RequestContext = {
    requestId,
    startTime: Date.now(),
    method: ctx.request.method,
    path: new URL(ctx.request.url).pathname,
    ip,
    userAgent: ctx.request.headers.get("user-agent") || "unknown",
  };

  // Store context
  storeRequestContext(context);

  // Add request ID to response headers
  if (!ctx.set) ctx.set = {};
  if (!ctx.set.headers) ctx.set.headers = {};
  ctx.set.headers["X-Request-ID"] = requestId;

  // Log request
  logger.debug("📨 Incoming Request", {
    requestId,
    method: context.method,
    path: context.path,
    ip: context.ip,
    userAgent: context.userAgent,
  });

  return context;
};

/**
 * Request summary middleware - Log request/response details
 */
export const requestSummaryMiddleware = (ctx: any, requestId: string) => {
  const context = getRequestContext(requestId);
  if (!context) return;

  const duration = Date.now() - context.startTime;
  const statusCode = ctx.response?.status || 200;
  const statusCategory =
    statusCode >= 200 && statusCode < 300
      ? "✅"
      : statusCode >= 300 && statusCode < 400
      ? "🔄"
      : statusCode >= 400 && statusCode < 500
      ? "⚠️"
      : "❌";

  logger.info("📤 Request Complete", {
    requestId,
    method: context.method,
    path: context.path,
    status: statusCode,
    duration: `${duration}ms`,
    ip: context.ip,
  });

  // Log slow requests (> 1 second)
  if (duration > 1000) {
    logger.warn("🐢 Slow Request Detected", {
      requestId,
      path: context.path,
      duration: `${duration}ms`,
      threshold: "1000ms",
    });
  }
};

/**
 * Request correlation ID helper
 * Use this in services/controllers to correlate logs
 */
export const withRequestContext = async <T>(
  requestId: string,
  fn: (context: RequestContext) => Promise<T>
): Promise<T> => {
  const context = getRequestContext(requestId);
  if (!context) {
    throw new Error(`Request context not found for ID: ${requestId}`);
  }

  try {
    return await fn(context);
  } catch (error) {
    logger.error("Error in request context", error instanceof Error ? error : new Error(String(error)), {
      requestId,
      path: context.path,
    });
    throw error;
  }
};

/**
 * Request tracing utility
 */
export class RequestTracer {
  static trace(message: string, requestId: string, data?: any): void {
    const context = getRequestContext(requestId);
    logger.debug(`[${requestId}] ${message}`, { ...data, path: context?.path });
  }

  static traceError(message: string, requestId: string, error: any): void {
    const context = getRequestContext(requestId);
    logger.error(`[${requestId}] ${message}`, error instanceof Error ? error : new Error(String(error)), {
      path: context?.path,
    });
  }

  static getContext(requestId: string): RequestContext | undefined {
    return getRequestContext(requestId);
  }

  static getAllContexts(): Map<string, RequestContext> {
    return new Map(requestContextMap);
  }

  static clearOldContexts(olderThanMs: number = 60 * 60 * 1000): number {
    let cleared = 0;
    const now = Date.now();

    requestContextMap.forEach((context, requestId) => {
      if (now - context.startTime > olderThanMs) {
        requestContextMap.delete(requestId);
        cleared++;
      }
    });

    if (cleared > 0) {
      logger.debug(`Cleared ${cleared} old request contexts`);
    }

    return cleared;
  }
}
