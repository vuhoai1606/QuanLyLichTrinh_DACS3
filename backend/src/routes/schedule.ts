import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import scheduleController from "@controllers/ScheduleController";

export const scheduleRoutes = new Elysia({ prefix: "/schedule" })
  .post("/categories", async ({ body }: { body: any }) => scheduleController.createCategory(body), { tags: ["Schedule"] })
  .post("/recur", async ({ body }: { body: any }) => scheduleController.createRecurring(body), { tags: ["Schedule"] })
  .post("/bulk-create", async ({ body }: { body: any }) => scheduleController.bulkCreate(body), { tags: ["Schedule"] })
  .post("/filter", async ({ body }: { body: any }) => scheduleController.filterSchedules(body), { tags: ["Schedule"] })
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
  .get("/:id", async ({ params }: { params: any }) => scheduleController.getScheduleById(params.id), { tags: ["Schedule"] });
