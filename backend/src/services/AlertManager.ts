import { logger } from "@utils/logger";

/**
 * Alert severity levels
 */
export type AlertSeverity = "critical" | "high" | "medium" | "low" | "info";

/**
 * Alert status
 */
export type AlertStatus = "active" | "resolved" | "acknowledged";

/**
 * Alert notification
 */
export interface Alert {
  id: string;
  title: string;
  message: string;
  severity: AlertSeverity;
  status: AlertStatus;
  component: string;
  createdAt: Date;
  resolvedAt?: Date;
  acknowledgedAt?: Date;
  acknowledgedBy?: string;
  metadata?: Record<string, any>;
  retryCount?: number;
}

/**
 * Alert condition
 */
export interface AlertCondition {
  name: string;
  metric: string;
  operator: ">" | "<" | "=" | "!=";
  threshold: number;
  duration: number; // milliseconds
  enabled: boolean;
}

/**
 * Alert manager for monitoring and alerting
 */
export class AlertManager {
  private static alerts = new Map<string, Alert>();
  private static conditions = new Map<string, AlertCondition>();
  private static alertHandlers: ((alert: Alert) => void)[] = [];
  private static alertCounter = 0;

  /**
   * Register an alert condition
   */
  static registerCondition(condition: AlertCondition): void {
    this.conditions.set(condition.name, condition);
    logger.info("Alert condition registered", { condition: condition.name });
  }

  /**
   * Create and register an alert
   */
  static createAlert(
    title: string,
    message: string,
    component: string,
    severity: AlertSeverity = "high"
  ): Alert {
    const alert: Alert = {
      id: `alert_${++this.alertCounter}_${Date.now()}`,
      title,
      message,
      severity,
      status: "active",
      component,
      createdAt: new Date(),
    };

    this.alerts.set(alert.id, alert);

    // Trigger handlers
    this.notifyHandlers(alert);

    // Log based on severity
    if (severity === "critical") {
      logger.error(`🚨 Alert [CRITICAL]: ${title}`, undefined, { alertId: alert.id, message, component });
    } else if (severity === "high") {
      logger.warn(`🚨 Alert [HIGH]: ${title}`, { alertId: alert.id, message, component });
    } else {
      logger.info(`🚨 Alert [${severity.toUpperCase()}]: ${title}`, { alertId: alert.id, message, component });
    }

    return alert;
  }

  /**
   * Acknowledge an alert
   */
  static acknowledgeAlert(alertId: string, acknowledgedBy: string): boolean {
    const alert = this.alerts.get(alertId);
    if (!alert) return false;

    alert.status = "acknowledged";
    alert.acknowledgedAt = new Date();
    alert.acknowledgedBy = acknowledgedBy;

    logger.info("Alert acknowledged", { alertId, acknowledgedBy });
    return true;
  }

  /**
   * Resolve an alert
   */
  static resolveAlert(alertId: string): boolean {
    const alert = this.alerts.get(alertId);
    if (!alert) return false;

    alert.status = "resolved";
    alert.resolvedAt = new Date();

    logger.info("Alert resolved", { alertId, component: alert.component });
    return true;
  }

  /**
   * Get alert by ID
   */
  static getAlert(alertId: string): Alert | undefined {
    return this.alerts.get(alertId);
  }

  /**
   * Get all active alerts
   */
  static getActiveAlerts(): Alert[] {
    return Array.from(this.alerts.values()).filter(
      (a) => a.status === "active" || a.status === "acknowledged"
    );
  }

  /**
   * Get alerts by component
   */
  static getAlertsByComponent(component: string): Alert[] {
    return Array.from(this.alerts.values()).filter((a) => a.component === component);
  }

  /**
   * Get alerts by severity
   */
  static getAlertsBySeverity(severity: AlertSeverity): Alert[] {
    return Array.from(this.alerts.values()).filter((a) => a.severity === severity);
  }

  /**
   * Get alert summary
   */
  static getSummary(): Record<string, any> {
    const all = Array.from(this.alerts.values());
    const active = this.getActiveAlerts();
    const critical = this.getAlertsBySeverity("critical");
    const high = this.getAlertsBySeverity("high");

    return {
      totalAlerts: all.length,
      activeAlerts: active.length,
      criticalAlerts: critical.length,
      highAlerts: high.length,
      byComponent: this.groupAlertsByComponent(),
      recent: all.slice(-10),
    };
  }

  /**
   * Group alerts by component
   */
  private static groupAlertsByComponent(): Record<string, number> {
    const grouped: Record<string, number> = {};

    this.alerts.forEach((alert) => {
      grouped[alert.component] = (grouped[alert.component] || 0) + 1;
    });

    return grouped;
  }

  /**
   * Register alert handler
   */
  static onAlert(handler: (alert: Alert) => void): void {
    this.alertHandlers.push(handler);
  }

  /**
   * Notify all handlers
   */
  private static notifyHandlers(alert: Alert): void {
    this.alertHandlers.forEach((handler) => {
      try {
        handler(alert);
      } catch (error) {
        logger.error("Error in alert handler", error instanceof Error ? error : new Error(String(error)));
      }
    });
  }

  /**
   * Clear resolved alerts older than duration
   */
  static clearOldAlerts(durationMs: number = 24 * 60 * 60 * 1000): number {
    // 24 hours default
    let cleared = 0;
    const now = Date.now();

    this.alerts.forEach((alert, id) => {
      if (alert.status === "resolved" && alert.resolvedAt) {
        if (now - alert.resolvedAt.getTime() > durationMs) {
          this.alerts.delete(id);
          cleared++;
        }
      }
    });

    if (cleared > 0) {
      logger.debug(`Cleared ${cleared} old resolved alerts`);
    }

    return cleared;
  }

  /**
   * Clear all alerts
   */
  static clearAll(): void {
    this.alerts.clear();
    logger.info("All alerts cleared");
  }
}

/**
 * Pre-configured alert conditions
 */
export const ALERT_CONDITIONS = {
  // Performance alerts
  HighResponseTime: {
    name: "HighResponseTime",
    metric: "http_request_duration_ms",
    operator: ">",
    threshold: 5000, // 5 seconds
    duration: 60000, // 1 minute
    enabled: true,
  } as AlertCondition,

  HighErrorRate: {
    name: "HighErrorRate",
    metric: "error_rate",
    operator: ">",
    threshold: 10, // 10%
    duration: 300000, // 5 minutes
    enabled: true,
  } as AlertCondition,

  DatabaseConnectionFailed: {
    name: "DatabaseConnectionFailed",
    metric: "db_connection_errors",
    operator: ">",
    threshold: 0,
    duration: 30000, // 30 seconds
    enabled: true,
  } as AlertCondition,

  HighMemoryUsage: {
    name: "HighMemoryUsage",
    metric: "memory_usage_percent",
    operator: ">",
    threshold: 85,
    duration: 120000, // 2 minutes
    enabled: true,
  } as AlertCondition,

  ServiceDown: {
    name: "ServiceDown",
    metric: "service_health",
    operator: "=",
    threshold: 0,
    duration: 10000, // 10 seconds
    enabled: true,
  } as AlertCondition,
};

/**
 * Setup common alerts
 */
export function setupDefaultAlerts(): void {
  Object.values(ALERT_CONDITIONS).forEach((condition) => {
    AlertManager.registerCondition(condition);
  });

  logger.info("✅ Default alert conditions registered");
}
