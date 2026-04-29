import { Elysia } from "elysia";
import focusController from "@controllers/FocusController";

export const focusRoutes = new Elysia({ prefix: "/focus" })
  .post("/sessions", async ({ body }: { body: any }) => focusController.createSession(body), { tags: ["Focus"] })
  .get("/history", async ({ query }: { query: any }) => focusController.getHistory(query.user_id, query.limit), { tags: ["Focus"] })
  .get("/stats", async ({ query }: { query: any }) => focusController.getStats(query.user_id), { tags: ["Focus"] })
  .get("/today", async ({ query }: { query: any }) => focusController.getTodaysSummary(query.user_id), { tags: ["Focus"] })
  .get("/streak", async ({ query }: { query: any }) => focusController.getStreak(query.user_id), { tags: ["Focus"] })
  .post("/sessions/:sessionId/pause", async ({ params }: { params: any }) => focusController.pauseSession(params.sessionId), { tags: ["Focus"] })
  .post("/sessions/:sessionId/resume", async ({ params }: { params: any }) => focusController.resumeSession(params.sessionId), { tags: ["Focus"] })
  .post("/sessions/bulk", async ({ body }: { body: any }) => focusController.bulkCreateSessions(body), { tags: ["Focus"] })
  .get("/comparison", async ({ query }: { query: any }) => focusController.compareWithOtherUsers(query.user_id, query.metric), { tags: ["Focus"] });
