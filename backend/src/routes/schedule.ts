import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import { errorResponse } from "@utils/errors";
import { extractToken, verifyToken } from "@utils/jwt";
import scheduleController from "@controllers/ScheduleController";

export const scheduleRoutes = new Elysia({ prefix: "/schedule" })
  .get(
    "/",
    async (ctx: AuthContext) => {
      const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
      if (!token) {
        return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
      }
      const payload = verifyToken(token);
      if (!payload) {
        return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
      }
      ctx.user = payload as any;
      const userId = (ctx as any).user?.userId;
      if (!userId) {
        return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
      }
      return scheduleController.getSchedules(userId);
    },
    { tags: ["Schedule"] }
  )
  .post(
    "/",
    async (ctx: AuthContext & { body: any }) => {
      const { body } = ctx;
      const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
      if (!token) {
        return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
      }
      const payload = verifyToken(token);
      if (!payload) {
        return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
      }
      ctx.user = payload as any;
      const userId = (ctx as any).user?.userId;
      if (!userId) {
        return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
      }
      return scheduleController.createSchedule({ ...body, creator_id: userId });
    },
    { tags: ["Schedule"] }
  )
  .post("/categories", async ({ body }: { body: any }) => scheduleController.createCategory(body), { tags: ["Schedule"] })
  .post("/recur", async ({ body }: { body: any }) => scheduleController.createRecurring(body), { tags: ["Schedule"] })
  .post("/bulk", async ({ body }: { body: any }) => scheduleController.bulkCreate(body), { tags: ["Schedules"] })
  .post("/sync-calendar", async ({ body }: { body: any }) => scheduleController.syncCalendar(body), { tags: ["Schedules"] })
  .delete("/bulk", async ({ body }: { body: any }) => scheduleController.bulkDelete(body), { tags: ["Schedules"] })
  .post("/archive", async ({ body }: { body: any }) => scheduleController.archiveSchedules(body), { tags: ["Schedule"] })
  .post("/bulk-delete", async ({ body }: { body: any }) => scheduleController.bulkDelete(body), { tags: ["Schedule"] })
  .get("/dashboard/summary", async ({ query }: { query: any }) => scheduleController.getDashboardSummary(query.user_id), { tags: ["Dashboard"] })
  .get("/dashboard/weekly", async ({ query }: { query: any }) => scheduleController.getWeeklyStats(query.user_id), { tags: ["Dashboard"] })
  .get("/dashboard/monthly", async ({ query }: { query: any }) => scheduleController.getMonthlyStats(query.user_id), { tags: ["Dashboard"] })
  .get("/dashboard/weekly-goal", async ({ query }: { query: any }) => scheduleController.getWeeklyGoalProgress(query.user_id), { tags: ["Dashboard"] })
  .get("/search/:searchQuery", async ({ params, query }: { params: any; query: any }) => scheduleController.searchSchedules(params.searchQuery, query.user_id), { tags: ["Schedule"] })
  .get("/export/:format", async ({ params, query }: { params: any; query: any }) => scheduleController.exportSchedules(query.user_id, params.format), { tags: ["Schedule"] })
  .post("/:id/clone", async ({ params, query }: { params: any; query: any }) => scheduleController.cloneSchedule(params.id, query.user_id), { tags: ["Schedule"] })
  .put("/:id/status", async ({ params, body }: { params: any; body: any }) => scheduleController.updateStatus(params.id, body), { tags: ["Schedule"] })
  .post("/create/:type", async ({ params, body }: { params: any; body: any }) => scheduleController.createSchedule({ ...body, type: params.type }), { tags: ["Schedule"] })
  .get(
    "/categories",
    async (ctx: AuthContext) => {
      const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
      if (!token) {
        return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
      }
      const payload = verifyToken(token);
      if (!payload) {
        return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
      }
      ctx.user = payload as any;
      const userId = (ctx as any).user?.userId;
      if (!userId) {
        return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
      }
      return scheduleController.getCategories(userId);
    },
    { tags: ["Schedule"] }
  )
  .get("/:id", async ({ params }: { params: any }) => scheduleController.getScheduleById(params.id), { tags: ["Schedule"] })
  .delete("/:id", async (ctx: AuthContext & { params: any }) => {
    const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
    if (!token) return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
    const payload = verifyToken(token);
    if (!payload) return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
    const userId = (payload as any).userId;
    if (!userId) return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    return scheduleController.deleteSchedule(ctx.params.id, userId);
  }, { tags: ["Schedule"] })
  .put("/:id", async (ctx: AuthContext & { params: any; body: any }) => {
    const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
    if (!token) return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
    const payload = verifyToken(token);
    if (!payload) return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
    const userId = (payload as any).userId;
    if (!userId) return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    return scheduleController.updateSchedule(ctx.params.id, userId, ctx.body);
  }, { tags: ["Schedule"] });
