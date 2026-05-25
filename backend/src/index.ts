import { Elysia } from "elysia";
import { connectDB, AppDataSource } from "@config/database";
import { config } from "@config/env";
import { corsMiddleware } from "@middleware/cors";
import { securityHeaders, getAllSecurityHeaders } from "@middleware/security-headers";
import { requestIdMiddleware, requestSummaryMiddleware, RequestTracer } from "@middleware/request-tracking";
import { compressionMiddleware } from "@middleware/compression";
import { apiKeyStore, initializeTestKeys } from "@middleware/api-key";
import { authRoutes } from "@routes/auth";
import { scheduleRoutes } from "@routes/schedule";
import { focusRoutes } from "@routes/focus";
import { collaborationRoutes } from "@routes/collaboration";
import { userRoutes } from "@routes/users";
import { gamificationRoutes } from "@routes/gamification";
import { notificationRoutes } from "@routes/notifications";
import { reportRoutes } from "@routes/reports";
import { settingsRoutes } from "@routes/settings";
import { adminRoutes } from "@routes/admin";
import { monitoringRoutes } from "@routes/monitoring";
import { chatRoutes } from "@routes/chat";
import { analyticsRoutes } from "@routes/analytics";
import { aiRoutes } from "@routes/ai";
import { logger } from "@utils/logger";
import { validateEnvironment, printStartupConfig } from "@utils/env-validator";
import { APP_CONSTANTS } from "@constants/app.constants";
import { TelemetryService, PerformanceTimer } from "@services/TelemetryService";
import { HealthCheckService } from "@services/HealthCheckService";
import { MetricsCollector, RequestMetrics } from "@services/MetricsCollector";
import { AlertManager, setupDefaultAlerts } from "@services/AlertManager";
import gamificationService from "@services/GamificationService";
// import { websocket } from "@elysiajs/websocket"; // Temporarily disabled due to compatibility issues
import { rateLimit } from "elysia-rate-limit";
import webSocketService from "@services/WebSocketService";
import { verifyToken } from "@utils/jwt";

/**
 * Initialize and configure Elysia server
 */
const app = new Elysia()
  // .use(websocket()) // Temporarily disabled due to compatibility issues
  .use(rateLimit({
    max: 100,
    duration: 60000, // 1 minute
    skip: () => process.env.NODE_ENV === "test",
    errorResponse: {
      status: 429,
      success: false,
      message: "Too many requests",
      code: "RATE_LIMIT_EXCEEDED"
    }
  }))
  .onBeforeHandle((ctx: any) => {
    corsMiddleware(ctx);
    securityHeaders()(ctx);
    const requestContext = requestIdMiddleware(ctx); // Add request ID tracking
    ctx.state = ctx.state || {};
    ctx.state.requestId = requestContext.requestId;
  })
  .onAfterHandle((ctx: any) => {
    // Log request summary after handling
    if (ctx.state?.requestId) {
      requestSummaryMiddleware(ctx, ctx.state.requestId);
    }
  })
  .get("/", () => ({
    name: "BFY Backend API",
    version: "1.0.0",
    description: "Better For Yourself - Personal Productivity Management API",
    database: "PostgreSQL + TypeORM",
    status: "running",
    timestamp: new Date().toISOString(),
    documentation: "http://localhost:3000/api-docs",
  }))
  .get("/health", () => ({
    status: "healthy",
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    environment: config.nodeEnv,
    memoryUsage: process.memoryUsage(),
  }))
  .get("/api/keys", () => ({
    keys: apiKeyStore.getAllKeys(),
    total: apiKeyStore.getAllKeys().length,
  }))
  .group("/api", (app) =>
    app
      .use(authRoutes)
      .use(scheduleRoutes)
      .use(focusRoutes)
      .use(collaborationRoutes)
      .use(userRoutes)
      .use(gamificationRoutes)
      .use(notificationRoutes)
      .use(reportRoutes)
      .use(settingsRoutes)
      .use(adminRoutes)
      .use(chatRoutes)
      .use(analyticsRoutes)
      .use(aiRoutes)
  )
  // WebSocket handler disabled - WebSocket plugin temporarily disabled due to compatibility issues
  // .ws("/ws", {...})
  .group("/monitoring", (app) => app.use(monitoringRoutes))
  .onError(({ error, code, request }) => {
    logger.error(`HTTP Error [${code}]:`, error instanceof Error ? error : new Error(String(error)));

    if (code === "NOT_FOUND") {
      const url = new URL(request.url);
      logger.warn(`Route not found: ${request.method} ${url.pathname}`);
      return {
        status: APP_CONSTANTS.HTTP.NOT_FOUND,
        success: false,
        message: `Route not found: ${request.method} ${url.pathname}`,
        code: APP_CONSTANTS.ERROR_CODES.NOT_FOUND,
        data: null,
      };
    }

    return {
      status: APP_CONSTANTS.HTTP.INTERNAL_ERROR,
      success: false,
      message: "Internal server error",
      code: APP_CONSTANTS.ERROR_CODES.INTERNAL_ERROR,
      data: null,
    };
  });

/**
 * Start server with proper initialization
 */
const start = async () => {
  try {
    // Validate environment
    validateEnvironment();
    printStartupConfig();

    logger.info("🔌 Connecting to PostgreSQL...");
    await connectDB();
    logger.info("✅ PostgreSQL connected successfully");

    // Setup monitoring
    logger.info("📊 Setting up monitoring and observability...");
    setupDefaultAlerts();

    // Register database health check
    HealthCheckService.registerComponent("database", async () => {
      try {
        if (AppDataSource && AppDataSource.isInitialized) {
          await AppDataSource.query("SELECT 1");
          return true;
        }
        return false;
      } catch {
        return false;
      }
    });

    logger.info("✅ Monitoring initialized");

    // Initialize test API keys
    if (config.nodeEnv === "development") {
      initializeTestKeys();
    }

    // Initialize Gamification data
    logger.info("🎮 Initializing gamification data...");
    await gamificationService.initializeRanks();
    logger.info("✅ Gamification data initialized");

    const port = config.port;
    app.listen({ port, hostname: "0.0.0.0" }, ({ hostname, port }) => {
      logger.info(`✅ Server running at http://${hostname}:${port}`);
      logger.info(`📍 API Prefix: ${config.api.prefix}`);
      logger.info(`🌍 Node Environment: ${config.nodeEnv}`);
      logger.info(`🗄️  Database: PostgreSQL`);
      logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      logger.info("🔒 Security Features Enabled:");
      logger.info("   ✅ Security Headers (HSTS, CSP, X-Frame-Options)");
      logger.info("   ✅ Request ID Tracking");
      logger.info("   ✅ Request/Response Logging");
      logger.info("   ✅ Rate Limiting");
      logger.info("   ✅ API Key Management");
      logger.info("   ✅ CORS Protection");
      logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      logger.info("📊 Monitoring & Observability Enabled:");
      logger.info("   ✅ Telemetry Tracking");
      logger.info("   ✅ Health Checks");
      logger.info("   ✅ Metrics Collection");
      logger.info("   ✅ Alert Management");
      logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
      logger.info("Server is ready for requests!");
      logger.info("📍 Health Check: GET /health");
      logger.info("📊 Dashboard: GET /monitoring/dashboard");
      logger.info("🚨 Alerts: GET /monitoring/alerts");
      logger.info("📈 Metrics: GET /monitoring/metrics");
      logger.info("🔍 Telemetry: GET /monitoring/telemetry");
      logger.info("🔑 API Keys: GET /api/keys");
      logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    });
  } catch (error) {
    logger.error(
      "❌ Failed to start server",
      error instanceof Error ? error : new Error(String(error))
    );
    process.exit(1);
  }
};

// Handle uncaught exceptions
process.on("uncaughtException", (error) => {
  logger.error("Uncaught Exception:", error);
  process.exit(1);
});

// Handle unhandled promise rejections
process.on("unhandledRejection", (reason, promise) => {
  logger.error("Unhandled Rejection at:", reason instanceof Error ? reason : new Error(String(reason)));
});

if (process.env.NODE_ENV !== "test") {
  start();
}

export default app;
