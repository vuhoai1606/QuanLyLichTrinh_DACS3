import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import analyticsService from "@services/AnalyticsService";
import { successResponse } from "@utils/errors";

export const analyticsRoutes = new Elysia({ prefix: "/analytics" })
  .onBeforeHandle(authMiddleware)
  .get("/productivity", async (ctx: AuthContext) => {
    const stats = await analyticsService.getUserProductivityStats(ctx.user!.userId);
    return successResponse(stats, "Stats retrieved");
  });
