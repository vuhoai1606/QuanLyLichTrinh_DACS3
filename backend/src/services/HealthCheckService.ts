import { DataSource } from "typeorm";
import { logger } from "@utils/logger";
import { PerformanceTimer } from "@services/TelemetryService";

/**
 * Health check status
 */
export type HealthStatus = "healthy" | "degraded" | "unhealthy";

/**
 * Health check component
 */
export interface HealthComponent {
  name: string;
  status: HealthStatus;
  uptime?: number;
  responseTime?: number;
  details?: Record<string, any>;
  error?: string;
  checkedAt: Date;
}

/**
 * Full health report
 */
export interface HealthReport {
  status: HealthStatus;
  timestamp: Date;
  uptime: number;
  components: Record<string, HealthComponent>;
  checks: {
    database: boolean;
    memory: boolean;
    diskSpace: {
      healthy: boolean;
      free: string;
      size: string;
      usagePercent: string;
    };
    external: boolean;
  };
}

/**
 * Health check service
 */
export class HealthCheckService {
  private static startTime = Date.now();
  private static components = new Map<string, HealthComponent>();

  /**
   * Register a component for health checking
   */
  static registerComponent(name: string, checkFn: () => Promise<boolean>): void {
    // Check on registration
    this.checkComponent(name, checkFn);

    // Check periodically (every 30 seconds)
    setInterval(() => {
      this.checkComponent(name, checkFn);
    }, 30000);
  }

  /**
   * Check a single component
   */
  private static async checkComponent(
    name: string,
    checkFn: () => Promise<boolean>
  ): Promise<void> {
    const timer = new PerformanceTimer(`Health Check: ${name}`);

    try {
      const result = await checkFn();
      const responseTime = timer.elapsed();

      this.components.set(name, {
        name,
        status: result ? "healthy" : "degraded",
        responseTime,
        checkedAt: new Date(),
      });
    } catch (error) {
      this.components.set(name, {
        name,
        status: "unhealthy",
        responseTime: timer.elapsed(),
        checkedAt: new Date(),
        error: error instanceof Error ? error.message : String(error),
      });
    }
  }

  /**
   * Check database health
   */
  static async checkDatabase(dataSource: DataSource): Promise<boolean> {
    try {
      await dataSource.query("SELECT 1");
      return true;
    } catch (error) {
      logger.error("Database health check failed", error instanceof Error ? error : new Error(String(error)));
      return false;
    }
  }

  /**
   * Check disk space health
   */
  static async checkDiskSpace(): Promise<{ healthy: boolean; free: string; size: string; usagePercent: string }> {
    try {
      const { execSync } = await import("child_process");
      const isWindows = process.platform === "win32";
      let free = 0;
      let size = 0;

      if (isWindows) {
        // Windows: wmic logicaldisk get size,freespace,caption
        const output = execSync('wmic logicaldisk get size,freespace,caption').toString();
        const lines = output.trim().split('\n').map(l => l.trim()).filter(l => l.length > 0);
        // Find C: or first drive
        const driveLine = lines.find(l => l.startsWith('C:')) || lines[1];
        if (driveLine) {
          const parts = driveLine.split(/\s+/);
          free = parseInt(parts[1]);
          size = parseInt(parts[2]);
        }
      } else {
        // Unix/Linux: df -B1 /
        const output = execSync('df -B1 /').toString();
        const lines = output.trim().split('\n');
        const parts = lines[1].split(/\s+/);
        size = parseInt(parts[1]);
        free = parseInt(parts[3]);
      }

      if (size === 0) return { healthy: true, free: "N/A", size: "N/A", usagePercent: "N/A" };

      const usagePercent = ((size - free) / size) * 100;
      return {
        healthy: usagePercent < 90, // Alert at 90%
        free: `${Math.round(free / 1024 / 1024 / 1024)}GB`,
        size: `${Math.round(size / 1024 / 1024 / 1024)}GB`,
        usagePercent: `${usagePercent.toFixed(2)}%`,
      };
    } catch (error) {
      return { healthy: false, free: "Error", size: "Error", usagePercent: "Error" };
    }
  }

  /**
   * Check memory health
   */
  static checkMemory(): { healthy: boolean; usage: Record<string, any> } {
    const memUsage = process.memoryUsage();
    const heapUsagePercent = (memUsage.heapUsed / memUsage.heapTotal) * 100;

    return {
      healthy: heapUsagePercent < 85, // Alert at 85%
      usage: {
        heapUsed: `${Math.round(memUsage.heapUsed / 1024 / 1024)}MB`,
        heapTotal: `${Math.round(memUsage.heapTotal / 1024 / 1024)}MB`,
        heapUsagePercent: `${heapUsagePercent.toFixed(2)}%`,
        external: `${Math.round(memUsage.external / 1024 / 1024)}MB`,
        rss: `${Math.round(memUsage.rss / 1024 / 1024)}MB`,
      },
    };
  }

  /**
   * Get full health report
   */
  static async getHealthReport(): Promise<HealthReport> {
    const components: Record<string, HealthComponent> = {};
    let overallStatus: HealthStatus = "healthy";

    this.components.forEach((component, name) => {
      components[name] = component;

      if (component.status === "unhealthy") {
        overallStatus = "unhealthy";
      } else if (component.status === "degraded" && overallStatus === "healthy") {
        overallStatus = "degraded";
      }
    });

    const memHealth = this.checkMemory();
    const diskHealth = await this.checkDiskSpace();

    if (!memHealth.healthy || !diskHealth.healthy) {
      overallStatus = "degraded";
    }

    return {
      status: overallStatus,
      timestamp: new Date(),
      uptime: (Date.now() - this.startTime) / 1000, // seconds
      components,
      checks: {
        database: components["database"]?.status === "healthy",
        memory: memHealth.healthy,
        diskSpace: diskHealth,
        external: components["external"]?.status === "healthy",
      },
    };
  }

  /**
   * Get health status for REST endpoint
   */
  static async getHealthStatus(): Promise<Record<string, any>> {
    const report = await this.getHealthReport();
    const memHealth = this.checkMemory();

    return {
      status: report.status,
      uptime: `${Math.floor(report.uptime / 3600)}h ${Math.floor((report.uptime % 3600) / 60)}m`,
      timestamp: report.timestamp.toISOString(),
      components: report.components,
      memory: memHealth.usage,
      disk: report.checks.diskSpace,
      systemHealth: {
        isHealthy: report.status === "healthy",
        isDegraded: report.status === "degraded",
        hasErrors: report.status === "unhealthy",
      },
    };
  }
}

/**
 * Service status tracker
 */
export class ServiceStatus {
  private status: HealthStatus = "healthy";
  private lastError?: string;
  private errorCount = 0;
  private successCount = 0;

  /**
   * Record success
   */
  recordSuccess(): void {
    this.successCount++;
    if (this.errorCount === 0) {
      this.status = "healthy";
    }
  }

  /**
   * Record error
   */
  recordError(message: string): void {
    this.errorCount++;
    this.lastError = message;

    if (this.errorCount > 5) {
      this.status = "unhealthy";
    } else if (this.errorCount > 2) {
      this.status = "degraded";
    }
  }

  /**
   * Get status
   */
  getStatus(): HealthStatus {
    return this.status;
  }

  /**
   * Get stats
   */
  getStats(): Record<string, any> {
    return {
      status: this.status,
      successCount: this.successCount,
      errorCount: this.errorCount,
      successRate: this.successCount + this.errorCount > 0 
        ? ((this.successCount / (this.successCount + this.errorCount)) * 100).toFixed(2) + "%"
        : "N/A",
      lastError: this.lastError,
    };
  }

  /**
   * Reset stats
   */
  reset(): void {
    this.errorCount = 0;
    this.successCount = 0;
    this.lastError = undefined;
    this.status = "healthy";
  }
}
