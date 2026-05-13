import { describe, it, expect, beforeEach } from "bun:test";
import { TelemetryService, PerformanceTimer } from "@services/TelemetryService";
import { AlertManager, ALERT_CONDITIONS } from "@services/AlertManager";
import { MetricsCollector } from "@services/MetricsCollector";
import { ErrorHandler } from "@utils/error-handler";
import { validateRegister } from "@dtos/auth.dto";

describe("Unit Tests - Services", () => {
  // ============= Telemetry Tests =============
  describe("TelemetryService", () => {
    beforeEach(() => {
      TelemetryService.clear();
    });

    it("should report errors with context", () => {
      const error = new Error("Test error");
      TelemetryService.reportError(
        "Test operation failed",
        error,
        { component: "auth" },
        "high"
      );

      const summary = TelemetryService.getSummary();
      expect(summary.errorCount).toBe(1);
      expect(summary.totalEvents).toBeGreaterThan(0);
    });

    it("should track error frequency", () => {
      TelemetryService.reportError("Error 1", new Error("test"), {}, "high");
      TelemetryService.reportError("Error 1", new Error("test"), {}, "high");
      TelemetryService.reportError("Error 2", new Error("test"), {}, "high");

      const summary = TelemetryService.getErrorSummary();
      expect(summary["Error 1"]).toBe(2);
      expect(summary["Error 2"]).toBe(1);
    });

    it("should report performance metrics", () => {
      TelemetryService.reportPerformance("operation", 150, { id: "123" }, 1000);
      TelemetryService.reportPerformance("operation", 250, { id: "124" }, 1000);

      const metrics = TelemetryService.getPerformanceMetrics();
      expect(metrics["operation"]).toBeDefined();
      expect(metrics["operation"].count).toBe(2);
      expect(metrics["operation"].avg).toBeGreaterThan(100);
    });

    it("should detect slow operations", () => {
      TelemetryService.reportPerformance("slow_op", 2500, {}, 1000); // above threshold
      TelemetryService.reportPerformance("fast_op", 150, {}, 1000); // below threshold

      const metrics = TelemetryService.getPerformanceMetrics();
      expect(metrics["slow_op"]).toBeDefined();
      expect(metrics["fast_op"]).toBeDefined();
    });

    it("should export Sentry format", () => {
      TelemetryService.reportError("test", new Error("err"), {}, "high");
      const sentry = TelemetryService.exportForSentry();

      expect(sentry.dsn).toBeDefined();
      expect(sentry.environment).toBeDefined();
    });
  });

  // ============= Performance Timer Tests =============
  describe("PerformanceTimer", () => {
    it("should measure elapsed time", () => {
      const timer = new PerformanceTimer("test");

      // Sleep 50ms
      const start = Date.now();
      while (Date.now() - start < 50) {}

      const elapsed = timer.elapsed();
      expect(elapsed).toBeGreaterThanOrEqual(40);
    });

    it("should report duration", () => {
      TelemetryService.clear();
      const timer = new PerformanceTimer("operation");

      const start = Date.now();
      while (Date.now() - start < 30) {}

      timer.end({}, 1000);

      const metrics = TelemetryService.getPerformanceMetrics();
      expect(metrics["operation"]).toBeDefined();
    });
  });

  // ============= Alert Manager Tests =============
  describe("AlertManager", () => {
    beforeEach(() => {
      AlertManager.clearAll();
    });

    it("should create alerts", () => {
      const alert = AlertManager.createAlert(
        "Test Alert",
        "Test message",
        "test_component",
        "high"
      );

      expect(alert.id).toBeDefined();
      expect(alert.title).toBe("Test Alert");
      expect(alert.severity).toBe("high");
      expect(alert.status).toBe("active");
    });

    it("should acknowledge alerts", () => {
      const alert = AlertManager.createAlert("Test", "msg", "component", "high");
      const success = AlertManager.acknowledgeAlert(alert.id, "admin");

      expect(success).toBe(true);
      const updated = AlertManager.getAlert(alert.id);
      expect(updated?.status).toBe("acknowledged");
      expect(updated?.acknowledgedBy).toBe("admin");
    });

    it("should resolve alerts", () => {
      const alert = AlertManager.createAlert("Test", "msg", "component", "high");
      const success = AlertManager.resolveAlert(alert.id);

      expect(success).toBe(true);
      const updated = AlertManager.getAlert(alert.id);
      expect(updated?.status).toBe("resolved");
    });

    it("should filter alerts by component", () => {
      AlertManager.createAlert("Alert 1", "msg", "database", "high");
      AlertManager.createAlert("Alert 2", "msg", "cache", "high");
      AlertManager.createAlert("Alert 3", "msg", "database", "low");

      const dbAlerts = AlertManager.getAlertsByComponent("database");
      expect(dbAlerts.length).toBe(2);

      const cacheAlerts = AlertManager.getAlertsByComponent("cache");
      expect(cacheAlerts.length).toBe(1);
    });

    it("should group alerts by severity", () => {
      AlertManager.createAlert("Alert 1", "msg", "comp", "critical");
      AlertManager.createAlert("Alert 2", "msg", "comp", "high");
      AlertManager.createAlert("Alert 3", "msg", "comp", "high");
      AlertManager.createAlert("Alert 4", "msg", "comp", "low");

      const critical = AlertManager.getAlertsBySeverity("critical");
      const high = AlertManager.getAlertsBySeverity("high");
      const low = AlertManager.getAlertsBySeverity("low");

      expect(critical.length).toBe(1);
      expect(high.length).toBe(2);
      expect(low.length).toBe(1);
    });

    it("should generate summary", () => {
      AlertManager.createAlert("A1", "msg", "db", "critical");
      AlertManager.createAlert("A2", "msg", "cache", "high");
      AlertManager.createAlert("A3", "msg", "db", "high");

      const summary = AlertManager.getSummary();
      expect(summary.totalAlerts).toBe(3);
      expect(summary.criticalAlerts).toBe(1);
      expect(summary.highAlerts).toBe(2);
      expect(summary.byComponent["db"]).toBe(2);
    });
  });

  // ============= Metrics Collector Tests =============
  describe("MetricsCollector", () => {
    beforeEach(() => {
      MetricsCollector.reset();
    });

    it("should record HTTP metrics", () => {
      MetricsCollector.recordHttpRequest("GET", "/api/users", 200, 50);
      MetricsCollector.recordHttpRequest("POST", "/api/users", 201, 100);

      const allMetrics = MetricsCollector.getAllMetrics();
      expect(allMetrics["http_request_duration_ms"]).toBeDefined();
      expect(allMetrics["http_requests_total"]).toBeDefined();
    });

    it("should record database metrics", () => {
      MetricsCollector.recordDatabaseOperation("SELECT users", 100, true);
      MetricsCollector.recordDatabaseOperation("INSERT user", 150, true);
      MetricsCollector.recordDatabaseOperation("DELETE user", 200, false);

      const agg = MetricsCollector.getMetricAggregation("db_operation_duration_ms");
      expect(agg?.count).toBe(3);
      expect(agg?.min).toBeLessThanOrEqual(100);
      expect(agg?.max).toBeGreaterThanOrEqual(200);
    });

    it("should calculate percentiles", () => {
      // Record 100 values from 1 to 100
      for (let i = 1; i <= 100; i++) {
        MetricsCollector.recordMetric("test_metric", i);
      }

      const agg = MetricsCollector.getMetricAggregation("test_metric");
      expect(agg?.p50).toBeLessThanOrEqual(60);
      expect(agg?.p95).toBeGreaterThan(90);
      expect(agg?.p99).toBeGreaterThan(95);
    });

    it("should export Prometheus format", () => {
      MetricsCollector.recordMetric("test_metric", 100);
      const prometheus = MetricsCollector.exportPrometheus();

      expect(prometheus).toContain("test_metric");
      expect(prometheus).toContain("TYPE");
    });
  });

  // ============= Error Handler Tests =============
  describe("ErrorHandler", () => {
    it("should handle service errors", () => {
      const error = new Error("Test error");
      const handled = ErrorHandler.handleServiceError(error, "TestService");

      expect(handled).toBeDefined();
      expect(handled.message).toContain("error");
    });

    it("should validate input", () => {
      const schema = {
        email: { required: true, pattern: /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/ },
        password: { required: true, minLength: 8 },
      };

      const valid = { email: "test@example.com", password: "SecurePass123" };
      const errors1 = ErrorHandler.validateInput(valid, schema);
      expect(errors1.length).toBe(0);

      const invalid = { email: "invalid-email", password: "123" };
      const errors2 = ErrorHandler.validateInput(invalid, schema);
      expect(errors2.length).toBeGreaterThan(0);
    });
  });

  // ============= Validation DTO Tests =============
  describe("Auth DTO Validation", () => {
    it("should validate register input", () => {
      const valid = {
        email: "user@example.com",
        password: "SecurePass123",
        full_name: "Test User",
      };

      const validation = validateRegister(valid);
      expect(validation.valid).toBe(true);
    });

    it("should reject invalid email", () => {
      const invalid = {
        email: "invalid-email",
        password: "SecurePass123",
        full_name: "Test User",
      };

      const validation = validateRegister(invalid);
      expect(validation.valid).toBe(false);
      expect(validation.errors?.length).toBeGreaterThan(0);
    });

    it("should reject weak password", () => {
      const invalid = {
        email: "user@example.com",
        password: "weak",
        full_name: "Test User",
      };

      const validation = validateRegister(invalid);
      expect(validation.valid).toBe(false);
    });

    it("should reject missing fields", () => {
      const incomplete = {
        email: "user@example.com",
        // missing password and full_name
      };

      const validation = validateRegister(incomplete);
      expect(validation.valid).toBe(false);
    });
  });
});
