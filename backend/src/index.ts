import { Elysia } from "elysia";
import { connectDB } from "@config/database";
import { config } from "@config/env";
import { corsMiddleware } from "@middleware/cors";
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

// Initialize Elysia server
const app = new Elysia()
  .onBeforeHandle((ctx) => {
    corsMiddleware(ctx);
  })
  .get("/", () => ({
    name: "BFY Backend API",
    version: "1.0.0",
    database: "PostgreSQL + TypeORM",
    status: "running",
    timestamp: new Date().toISOString(),
  }))
  .get("/health", () => ({
    status: "healthy",
    timestamp: new Date().toISOString(),
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
  )
  .onError(({ error, code }) => {
    console.error(`[${code}]`, error);

    if (code === "NOT_FOUND") {
      return {
        status: 404,
        error: {
          message: "Route not found",
          code: "NOT_FOUND",
        },
      };
    }

    return {
      status: 500,
      error: {
        message: "Internal server error",
        code: code || "INTERNAL_ERROR",
      },
    };
  });

// Start server
const start = async () => {
  try {
    console.log("🔌 Connecting to PostgreSQL...");
    await connectDB();

    const port = config.port;
    app.listen(port, ({ hostname, port }) => {
      console.log(`✅ Server running at http://${hostname}:${port}`);
      console.log(`📍 API Prefix: ${config.api.prefix}`);
      console.log(`🌍 Node Env: ${config.nodeEnv}`);
      console.log(`🗄️  Database: PostgreSQL`);
    });
  } catch (error) {
    console.error("❌ Failed to start server:", error);
    process.exit(1);
  }
};

start();

export default app;
