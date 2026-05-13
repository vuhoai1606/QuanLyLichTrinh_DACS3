import { logger } from "@utils/logger";

/**
 * Metric data point
 */
export interface MetricDataPoint {
  timestamp: Date;
  value: number;
  labels?: Record<string, string>;
}

/**
 * Metric aggregation
 */
export interface MetricAggregation {
  count: number;
  min: number;
  max: number;
  avg: number;
  sum: number;
  p50: number;
  p95: number;
  p99: number;
}

/**
 * Metrics collector for application monitoring
 */
export class MetricsCollector {
  private static metrics = new Map<string, MetricDataPoint[]>();
  private static maxDataPoints = 1000; // Keep last 1000 data points per metric

  /**
   * Record a metric value
   */
  static recordMetric(
    metricName: string,
    value: number,
    labels?: Record<string, string>
  ): void {
    if (!this.metrics.has(metricName)) {
      this.metrics.set(metricName, []);
    }

    const dataPoints = this.metrics.get(metricName)!;
    dataPoints.push({
      timestamp: new Date(),
      value,
      labels,
    });

    // Keep only last N data points
    if (dataPoints.length > this.maxDataPoints) {
      dataPoints.shift();
    }
  }

  /**
   * Record HTTP request
   */
  static recordHttpRequest(
    method: string,
    path: string,
    statusCode: number,
    durationMs: number
  ): void {
    // Record response time
    this.recordMetric("http_request_duration_ms", durationMs, {
      method,
      path,
      status: String(statusCode),
    });

    // Record request count
    this.recordMetric("http_requests_total", 1, {
      method,
      status: String(statusCode),
    });
  }

  /**
   * Record database operation
   */
  static recordDatabaseOperation(
    operation: string,
    durationMs: number,
    success: boolean
  ): void {
    this.recordMetric("db_operation_duration_ms", durationMs, {
      operation,
      status: success ? "success" : "error",
    });

    this.recordMetric("db_operations_total", 1, {
      operation,
      status: success ? "success" : "error",
    });
  }

  /**
   * Record cache operation
   */
  static recordCacheOperation(
    operation: "hit" | "miss" | "set",
    durationMs: number
  ): void {
    this.recordMetric("cache_operation_duration_ms", durationMs, {
      operation,
    });

    this.recordMetric("cache_operations_total", 1, {
      operation,
    });
  }

  /**
   * Record error
   */
  static recordError(errorType: string, severity: string): void {
    this.recordMetric("errors_total", 1, {
      type: errorType,
      severity,
    });
  }

  /**
   * Get metric data
   */
  static getMetricData(metricName: string): MetricDataPoint[] {
    return this.metrics.get(metricName) || [];
  }

  /**
   * Get metric aggregation
   */
  static getMetricAggregation(metricName: string): MetricAggregation | null {
    const dataPoints = this.metrics.get(metricName);
    if (!dataPoints || dataPoints.length === 0) {
      return null;
    }

    const values = dataPoints.map((p) => p.value).sort((a, b) => a - b);
    const sum = values.reduce((a, b) => a + b, 0);
    const avg = sum / values.length;

    return {
      count: values.length,
      min: values[0],
      max: values[values.length - 1],
      avg: Math.round(avg * 100) / 100,
      sum,
      p50: values[Math.floor(values.length * 0.5)],
      p95: values[Math.floor(values.length * 0.95)],
      p99: values[Math.floor(values.length * 0.99)],
    };
  }

  /**
   * Get all metrics
   */
  static getAllMetrics(): Record<string, MetricAggregation | null> {
    const result: Record<string, MetricAggregation | null> = {};

    this.metrics.forEach((dataPoints, metricName) => {
      result[metricName] = this.getMetricAggregation(metricName);
    });

    return result;
  }

  /**
   * Get metrics summary
   */
  static getSummary(): Record<string, any> {
    const metrics: Record<string, any> = {};

    this.metrics.forEach((dataPoints, metricName) => {
      if (dataPoints.length === 0) return;

      const agg = this.getMetricAggregation(metricName);
      if (agg) {
        metrics[metricName] = agg;
      }
    });

    return {
      timestamp: new Date().toISOString(),
      totalMetrics: this.metrics.size,
      metrics,
    };
  }

  /**
   * Export as Prometheus format
   */
  static exportPrometheus(): string {
    let output = "";

    this.metrics.forEach((dataPoints, metricName) => {
      const agg = this.getMetricAggregation(metricName);
      if (!agg) return;

      // Export as Prometheus gauge
      output += `# TYPE ${metricName} gauge\n`;
      output += `${metricName}_avg ${agg.avg}\n`;
      output += `${metricName}_min ${agg.min}\n`;
      output += `${metricName}_max ${agg.max}\n`;
      output += `${metricName}_p95 ${agg.p95}\n`;
      output += `${metricName}_p99 ${agg.p99}\n`;
      output += `${metricName}_count ${agg.count}\n\n`;
    });

    return output;
  }

  /**
   * Clear old metrics
   */
  static clearOldMetrics(olderThanMs: number = 3600000): number {
    // 1 hour default
    let cleared = 0;
    const now = Date.now();

    this.metrics.forEach((dataPoints, metricName) => {
      const filtered = dataPoints.filter(
        (p) => now - p.timestamp.getTime() < olderThanMs
      );

      if (filtered.length < dataPoints.length) {
        if (filtered.length === 0) {
          this.metrics.delete(metricName);
        } else {
          this.metrics.set(metricName, filtered);
        }
        cleared += dataPoints.length - filtered.length;
      }
    });

    if (cleared > 0) {
      logger.debug(`Cleared ${cleared} old metric data points`);
    }

    return cleared;
  }

  /**
   * Reset all metrics
   */
  static reset(): void {
    this.metrics.clear();
    logger.info("Metrics reset");
  }
}

/**
 * Request metrics collector
 */
export class RequestMetrics {
  private totalRequests = 0;
  private successRequests = 0;
  private errorRequests = 0;
  private totalDuration = 0;

  recordRequest(statusCode: number, durationMs: number): void {
    this.totalRequests++;
    this.totalDuration += durationMs;

    if (statusCode >= 200 && statusCode < 400) {
      this.successRequests++;
    } else {
      this.errorRequests++;
    }
  }

  getStats() {
    return {
      totalRequests: this.totalRequests,
      successRequests: this.successRequests,
      errorRequests: this.errorRequests,
      errorRate: this.totalRequests > 0 
        ? ((this.errorRequests / this.totalRequests) * 100).toFixed(2) + "%"
        : "0%",
      avgDuration: this.totalRequests > 0 
        ? Math.round(this.totalDuration / this.totalRequests) + "ms"
        : "0ms",
    };
  }

  reset(): void {
    this.totalRequests = 0;
    this.successRequests = 0;
    this.errorRequests = 0;
    this.totalDuration = 0;
  }
}
