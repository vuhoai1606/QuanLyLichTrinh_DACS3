import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import calendarController from "@controllers/CalendarController";

export const calendarRoutes = new Elysia({ prefix: "/calendar" })
  .get(
    "/auth",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      return calendarController.getAuthUrl(ctx);
    },
    { tags: ["Calendar"] }
  )
  .get(
    "/callback",
    async ({ query }: { query: any }) => calendarController.handleCallback(query),
    { tags: ["Calendar"] }
  )
  .post(
    "/sync",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      return calendarController.sync(ctx);
    },
    { tags: ["Calendar"] }
  );
