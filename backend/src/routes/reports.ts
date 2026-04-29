import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import reportController from "@controllers/ReportController";

export const reportRoutes = new Elysia({ prefix: "/reports" })
  .get("/productivity", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getProductivityReport(ctx.user?.userId); }, { tags: ["Reports"] })
  .get("/focus-analysis", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getFocusAnalysis(ctx.user?.userId); }, { tags: ["Reports"] })
  .get("/task-completion", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getTaskCompletionRate(ctx.user?.userId); }, { tags: ["Reports"] })
  .get("/exp-history", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getExpHistory(ctx.user?.userId); }, { tags: ["Reports"] })
  .post("/export", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return reportController.exportReport(ctx.user?.userId, body); }, { tags: ["Reports"] })
  .get("/compare", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.compareWithPreviousPeriod(ctx.user?.userId); }, { tags: ["Reports"] })
  .get("/health-score", async (ctx: AuthContext) => { authMiddleware(ctx); return reportController.getHealthScore(ctx.user?.userId); }, { tags: ["Reports"] });
