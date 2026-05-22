import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import { errorResponse } from "@utils/errors";
import { extractToken, verifyToken } from "@utils/jwt";
import focusController from "@controllers/FocusController";

export const focusRoutes = new Elysia({ prefix: "/focus" })
  .get(
    "/stats",
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
      return focusController.getStats(userId);
    },
    { tags: ["Focus"] }
  )
  .post("/sessions", async ({ body }: { body: any }) => focusController.createSession(body), { tags: ["Focus"] })
  .get("/history", async ({ query }: { query: any }) => focusController.getHistory(query.user_id, query.limit), { tags: ["Focus"] })
  .get("/today", async ({ query }: { query: any }) => focusController.getTodaysSummary(query.user_id), { tags: ["Focus"] })
  .get("/streak", async ({ query }: { query: any }) => focusController.getStreak(query.user_id), { tags: ["Focus"] })
  .post("/sessions/:sessionId/pause", async ({ params }: { params: any }) => focusController.pauseSession(params.sessionId), { tags: ["Focus"] })
  .post("/sessions/:sessionId/resume", async ({ params }: { params: any }) => focusController.resumeSession(params.sessionId), { tags: ["Focus"] })
  .post("/sessions/bulk", async ({ body }: { body: any }) => focusController.bulkCreateSessions(body), { tags: ["Focus"] })
  .get("/comparison", async ({ query }: { query: any }) => focusController.compareWithOtherUsers(query.user_id, query.metric), { tags: ["Focus"] })
  .get("/ambient-sounds", () => {
    return [
      { id: "rain", name: "Rain", url: "https://example.com/sounds/rain.mp3", category: "Nature" },
      { id: "white-noise", name: "White Noise", url: "https://example.com/sounds/white.mp3", category: "Noise" },
      { id: "lofi", name: "Lofi Study", url: "https://example.com/sounds/lofi.mp3", category: "Music" },
      { id: "forest", name: "Forest", url: "https://example.com/sounds/forest.mp3", category: "Nature" }
    ];
  }, { tags: ["Focus"] });
