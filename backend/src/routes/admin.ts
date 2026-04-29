import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import adminController from "@controllers/AdminController";

export const adminRoutes = new Elysia({ prefix: "/admin" })
  .get("/users", async (ctx: AuthContext) => { authMiddleware(ctx); const query = (ctx.request?.url?.split("?")[1] || "") as any; return adminController.getAllUsers(query); }, { tags: ["Admin"] })
  .get("/users/:userId", async (ctx: AuthContext) => { authMiddleware(ctx); return adminController.getUserDetails((ctx.params as any)?.userId); }, { tags: ["Admin"] })
  .put("/users/:userId/status", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return adminController.changeUserStatus((ctx.params as any)?.userId, body?.status); }, { tags: ["Admin"] })
  .get("/statistics", async (ctx: AuthContext) => { authMiddleware(ctx); return adminController.getSystemStatistics(); }, { tags: ["Admin"] })
  .post("/maintenance", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return adminController.triggerMaintenance(body?.action); }, { tags: ["Admin"] })
  .get("/logs", async (ctx: AuthContext) => { authMiddleware(ctx); const query = (ctx.request?.url?.split("?")[1] || "") as any; return adminController.getSystemLogs(query); }, { tags: ["Admin"] })
  .get("/users/search/:query", async (ctx: AuthContext) => { authMiddleware(ctx); return adminController.searchUsers((ctx.params as any)?.query); }, { tags: ["Admin"] });
