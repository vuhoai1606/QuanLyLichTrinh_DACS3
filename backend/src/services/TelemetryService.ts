import { logger } from "@utils/logger";
import { APP_CONSTANTS } from "@constants/app.constants";

/**
 * Telemetry event for error tracking and monitoring
 */
export interface TelemetryEvent {
  timestamp: Date;
  type: "error" | "warning" | "info" | "performance";
  severity: "critical" | "high" | "medium" | "low" | "info";
  message: string;
  context: Record<string, any>;
  stackTrace?: string;
  userId?: string;
  requestId?: string;
  duration?: number;
  tags?: string[];
}

/**
 * Telemetry service for centralized error and event tracking
 */
export class TelemetryService {
  private static events: TelemetryEvent[] = [];
  private static maxEvents = 10000;
  private static errorSummary = new Map<string, number>();
  private static performanceMetrics: Record<string, number[]> = {};

  /**
   * Report an error event
   */
  static reportError(
    message: string,
    error: Error | unknown,
    context: Record<string, any> = {},
    severity: "critical" | "high" | "medium" = "high"
  ): void {
    const event: TelemetryEvent = {
      timestamp: new Date(),
      type: "error",
      severity,
      message,
      context,
      stackTrace: error instanceof Error ? error.stack : undefined,
      tags: ["error"],
    };

    this.recordEvent(event);

    // Track error frequency
    const key = `${message}`;
    this.errorSummary.set(key, (this.errorSummary.get(key) || 0) + 1);

    // Log critical errors
    if (severity === "critical") {
      logger.error(`🚨 CRITICAL: ${message}`, undefined, context);
    }
  }

  /**
   * Report a performance event
   */
  static reportPerformance(
    operation: string,
    durationMs: number,
    context: Record<string, any> = {},
    threshold: number = 1000
  ): void {
    const isSlow = durationMs > threshold;
    const severity = isSlow ? "high" : "info";

    const event: TelemetryEvent = {
      timestamp: new Date(),
      type: "performance",
      severity: severity as "high" | "info",
      message: `${operation} took ${durationMs}ms`,
      context,
      duration: durationMs,
      tags: ["performance", isSlow ? "slow" : "normal"],
    };

    this.recordEvent(event);

    // Track performance metrics
    if (!this.performanceMetrics[operation]) {
      this.performanceMetrics[operation] = [];
    }
    this.performanceMetrics[operation].push(durationMs);

    // Warn if slow
    if (isSlow) {
      logger.warn(`⚠️ Slow Operation: ${operation} (${durationMs}ms)`, context);
    }
  }

  /**
   * Report a custom event
   */
  static reportEvent(
    message: string,
    context: Record<string, any> = {},
    tags: string[] = []
  ): void {
    const event: TelemetryEvent = {
      timestamp: new Date(),
      type: "info",
      severity: "info",
      message,
      context,
      tags,
    };

    this.recordEvent(event);
  }

  /**
   * Record event in memory
   */
  private static recordEvent(event: TelemetryEvent): void {
    this.events.push(event);

    // Trim if exceeds max
    if (this.events.length > this.maxEvents) {
      this.events = this.events.slice(-this.maxEvents);
    }
  }

  /**
   * Get all events
   */
  static getEvents(filter?: {
    type?: string;
    severity?: string;
    limit?: number;
  }): TelemetryEvent[] {
    let filtered = [...this.events];

    if (filter?.type) {
      filtered = filtered.filter((e) => e.type === filter.type);
    }

    if (filter?.severity) {
      const severityLevels: Record<string, number> = {
        critical: 4,
        high: 3,
        medium: 2,
        low: 1,
        info: 0,
      };
      const threshold = severityLevels[filter.severity] || 0;
      filtered = filtered.filter((e) => severityLevels[e.severity] >= threshold);
    }

    const limit = filter?.limit || 100;
    return filtered.slice(-limit);
  }

  /**
   * Get error summary
   */
  static getErrorSummary(): Record<string, number> {
    return Object.fromEntries(this.errorSummary);
  }

  /**
   * Get performance metrics
   */
  static getPerformanceMetrics(): Record<
    string,
    {
      count: number;
      min: number;
      max: number;
      avg: number;
      p95: number;
      p99: number;
    }
  > {
    const metrics: Record<string, any> = {};

    for (const [operation, durations] of Object.entries(
      this.performanceMetrics
    )) {
      if (durations.length === 0) continue;

      const sorted = [...durations].sort((a, b) => a - b);
      const sum = sorted.reduce((a, b) => a + b, 0);

      metrics[operation] = {
        count: durations.length,
        min: sorted[0],
        max: sorted[sorted.length - 1],
        avg: Math.round(sum / durations.length),
        p95: sorted[Math.floor(durations.length * 0.95)],
        p99: sorted[Math.floor(durations.length * 0.99)],
      };
    }

    return metrics;
  }

  /**
   * Get telemetry summary
   */
  static getSummary(): Record<string, any> {
    const events = this.events;
    const errorCount = events.filter((e) => e.type === "error").length;
    const warningCount = events.filter((e) => e.severity === "high").length;
    const performanceEvents = events.filter((e) => e.type === "performance");

    return {
      totalEvents: events.length,
      errorCount,
      warningCount,
      performanceEvents: performanceEvents.length,
      errorSummary: this.getErrorSummary(),
      performanceMetrics: this.getPerformanceMetrics(),
      topErrors: Array.from(this.errorSummary.entries())
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10),
      timestamp: new Date().toISOString(),
    };
  }

  /**
   * Clear all events
   */
  static clear(): void {
    this.events = [];
    this.errorSummary.clear();
    this.performanceMetrics = {};
    logger.info("Telemetry data cleared");
  }

  /**
   * Export events for external service
   */
  static exportForSentry(): Record<string, any> {
    const summary = this.getSummary();
    return {
      dsn: process.env.SENTRY_DSN || "not-configured",
      environment: process.env.NODE_ENV || "development",
      release: "1.0.0",
      ...summary,
    };
  }

  /**
   * Export events for DataDog
   */
  static exportForDataDog(): Record<string, any> {
    const metrics = this.getPerformanceMetrics();
    return {
      apiKey: process.env.DATADOG_API_KEY || "not-configured",
      site: process.env.DATADOG_SITE || "datadoghq.com",
      metrics,
      errors: this.getErrorSummary(),
      timestamp: Date.now(),
    };
  }
}

/**
 * Performance timer utility
 */
export class PerformanceTimer {
  private startTime: number;
  private label: string;

  constructor(label: string) {
    this.label = label;
    this.startTime = Date.now();
  }

  /**
   * End timer and report
   */
  end(context?: Record<string, any>, threshold?: number): number {
    const duration = Date.now() - this.startTime;
    TelemetryService.reportPerformance(this.label, duration, context, threshold);
    return duration;
  }

  /**
   * Get elapsed time without ending
   */
  elapsed(): number {
    return Date.now() - this.startTime;
  }
}
