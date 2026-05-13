import { Elysia } from "elysia";
import { TelemetryService } from "@services/TelemetryService";
import { HealthCheckService } from "@services/HealthCheckService";
import { MetricsCollector } from "@services/MetricsCollector";
import { AlertManager } from "@services/AlertManager";
import { successResponse } from "@utils/errors";

/**
 * Monitoring routes for observability
 */
export const monitoringRoutes = (app: Elysia) =>
  app
    // Telemetry endpoints
    .get("/telemetry", () => {
      const summary = TelemetryService.getSummary();
      return successResponse(summary, "Telemetry data retrieved");
    })

    .get("/telemetry/errors", () => {
      const summary = TelemetryService.getErrorSummary();
      return successResponse(summary, "Error summary retrieved");
    })

    .get("/telemetry/performance", () => {
      const metrics = TelemetryService.getPerformanceMetrics();
      return successResponse(metrics, "Performance metrics retrieved");
    })

    .get("/telemetry/events", (ctx) => {
      const limit = parseInt((ctx.query as any)?.limit || "100");
      const type = (ctx.query as any)?.type;
      const severity = (ctx.query as any)?.severity;

      const events = TelemetryService.getEvents({ type, severity, limit });
      return successResponse(events, "Events retrieved");
    })

    .delete("/telemetry", () => {
      TelemetryService.clear();
      return successResponse({ cleared: true }, "Telemetry data cleared");
    })

    // Health check endpoints
    .get("/health/full", async () => {
      const report = await HealthCheckService.getHealthReport();
      return successResponse(report, "Full health report");
    })

    .get("/health/status", async () => {
      const status = await HealthCheckService.getHealthStatus();
      return successResponse(status, "Health status");
    })

    // Metrics endpoints
    .get("/metrics", () => {
      const metrics = MetricsCollector.getSummary();
      return successResponse(metrics, "Metrics summary");
    })

    .get("/metrics/prometheus", () => {
      const prometheus = MetricsCollector.exportPrometheus();
      return new Response(prometheus, {
        headers: { "Content-Type": "text/plain" },
      });
    })

    .get("/metrics/all", () => {
      const metrics = MetricsCollector.getAllMetrics();
      return successResponse(metrics, "All metrics");
    })

    .get("/metrics/:metricName", (ctx) => {
      const metricName = (ctx.params as any).metricName;
      const agg = MetricsCollector.getMetricAggregation(metricName);

      if (!agg) {
        return successResponse(
          null,
          `Metric '${metricName}' not found or has no data`
        );
      }

      return successResponse(agg, `Metric '${metricName}' aggregation`);
    })

    .delete("/metrics", () => {
      MetricsCollector.reset();
      return successResponse({ cleared: true }, "All metrics cleared");
    })

    // Alert endpoints
    .get("/alerts", () => {
      const summary = AlertManager.getSummary();
      return successResponse(summary, "Alert summary");
    })

    .get("/alerts/active", () => {
      const alerts = AlertManager.getActiveAlerts();
      return successResponse(alerts, "Active alerts");
    })

    .get("/alerts/:component", (ctx) => {
      const component = (ctx.params as any).component;
      const alerts = AlertManager.getAlertsByComponent(component);
      return successResponse(alerts, `Alerts for component: ${component}`);
    })

    .get("/alerts/severity/:severity", (ctx) => {
      const severity = (ctx.params as any).severity as any;
      const alerts = AlertManager.getAlertsBySeverity(severity);
      return successResponse(alerts, `${severity} alerts`);
    })

    .post("/alerts/:alertId/acknowledge", (ctx) => {
      const alertId = (ctx.params as any).alertId;
      const body = ctx.body as any;
      const acknowledgedBy = body?.acknowledgedBy || "system";

      const success = AlertManager.acknowledgeAlert(alertId, acknowledgedBy);

      if (success) {
        return successResponse({ acknowledged: true }, "Alert acknowledged");
      }

      return successResponse(
        { acknowledged: false },
        "Alert not found",
        404
      );
    })

    .post("/alerts/:alertId/resolve", (ctx) => {
      const alertId = (ctx.params as any).alertId;

      const success = AlertManager.resolveAlert(alertId);

      if (success) {
        return successResponse({ resolved: true }, "Alert resolved");
      }

      return successResponse(
        { resolved: false },
        "Alert not found",
        404
      );
    })

    // Dashboard data endpoint
    .get("/dashboard", async () => {
      const health = await HealthCheckService.getHealthStatus();
      const telemetry = TelemetryService.getSummary();
      const metrics = MetricsCollector.getSummary();
      const alerts = AlertManager.getSummary();

      return successResponse(
        {
          health,
          telemetry,
          metrics,
          alerts,
          timestamp: new Date().toISOString(),
        },
        "Dashboard data"
      );
    });
