import { Context } from "elysia";
import { errorResponse } from "@utils/errors";

interface RateLimitOptions {
  windowMs: number; // Time window in milliseconds
  maxRequests: number; // Max requests per window
  message?: string;
}

interface RateLimitStore {
  [key: string]: {
    count: number;
    resetTime: number;
  };
}

/**
 * In-memory rate limiter (for production, use Redis)
 */
class RateLimiter {
  private store: RateLimitStore = {};
  private cleanupInterval: NodeJS.Timer;

  constructor() {
    // Clean up expired entries every 5 minutes
    this.cleanupInterval = setInterval(() => this.cleanup(), 5 * 60 * 1000);
  }

  private cleanup(): void {
    const now = Date.now();
    for (const key in this.store) {
      if (this.store[key].resetTime < now) {
        delete this.store[key];
      }
    }
  }

  isAllowed(identifier: string, options: RateLimitOptions): boolean {
    const now = Date.now();
    const key = identifier;

    if (!this.store[key] || this.store[key].resetTime < now) {
      this.store[key] = {
        count: 1,
        resetTime: now + options.windowMs,
      };
      return true;
    }

    this.store[key].count++;
    return this.store[key].count <= options.maxRequests;
  }

  destroy(): void {
    clearInterval(this.cleanupInterval);
  }
}

export const rateLimiter = new RateLimiter();

/**
 * Rate limit middleware factory
 * Usage: .middleware(rateLimit(15 * 60 * 1000, 100)) // 100 requests per 15 minutes
 */
export const rateLimit = (windowMs: number, maxRequests: number, message?: string) => {
  return (ctx: Context) => {
    if (process.env.NODE_ENV === "test") {
      return;
    }
    // Use IP address as identifier
    const identifier = ctx.request.headers.get("x-forwarded-for") ||
                     ctx.request.headers.get("x-real-ip") ||
                     "unknown";

    const allowed = rateLimiter.isAllowed(identifier, {
      windowMs,
      maxRequests,
      message,
    });

    if (!allowed) {
      return errorResponse(
        429,
        message || "Too many requests, please try again later",
        "RATE_LIMIT_EXCEEDED"
      );
    }
  };
};

/**
 * Specific rate limiters for common endpoints
 */
export const AuthRateLimit = {
  // 5 login attempts per 15 minutes
  login: () => rateLimit(15 * 60 * 1000, 5, "Too many login attempts"),
  
  // 3 register attempts per hour
  register: () => rateLimit(60 * 60 * 1000, 3, "Too many registration attempts"),
  
  // 5 password reset per day
  forgotPassword: () => rateLimit(24 * 60 * 60 * 1000, 5, "Too many password reset requests"),
};

export const ApiRateLimit = {
  // Standard API: 100 requests per 15 minutes
  standard: () => rateLimit(15 * 60 * 1000, 100),
  
  // Strict: 10 requests per minute
  strict: () => rateLimit(60 * 1000, 10),
  
  // Generous: 1000 requests per hour
  generous: () => rateLimit(60 * 60 * 1000, 1000),
};
