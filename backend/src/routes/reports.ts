import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import reportController from "@controllers/ReportController";

export const reportRoutes = new Elysia({ prefix: "/reports" })
  .get("/productivity", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getProductivityReport({ ...ctx.query, user_id: ctx.user!.userId }); }, { tags: ["Reports"] })
  .get("/focus-analysis", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getFocusAnalysis({ ...ctx.query, user_id: ctx.user!.userId }); }, { tags: ["Reports"] })
  .get("/task-completion", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getTaskCompletionRate({ ...ctx.query, user_id: ctx.user!.userId }); }, { tags: ["Reports"] })
  .get("/exp-history", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getExpHistory(ctx.user!.userId); }, { tags: ["Reports"] })
  .post("/export", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.exportReport({ ...(ctx.body as any), user_id: ctx.user!.userId }); }, { tags: ["Reports"] })
  .get("/compare", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.compareWithPreviousPeriod({ ...ctx.query, user_id: ctx.user!.userId }); }, { tags: ["Reports"] })
  .get("/health-score", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getHealthScore(ctx.user!.userId); }, { tags: ["Reports"] });
