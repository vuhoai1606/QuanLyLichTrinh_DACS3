import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import settingsController from "@controllers/SettingsController";

export const settingsRoutes = new Elysia({ prefix: "/settings" })
  .get("/app", async () => settingsController.getAppSettings(), { tags: ["Settings"] })
  .get("/user", async (ctx: AuthContext) => { authMiddleware(ctx); return settingsController.getUserSettings(ctx.user!.userId); }, { tags: ["Settings"] })
  .put("/user", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return settingsController.updateUserSettings(ctx.user!.userId, body); }, { tags: ["Settings"] })
  .post("/theme", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return settingsController.changeTheme(ctx.user!.userId, body?.theme); }, { tags: ["Settings"] })
  .post("/language", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return settingsController.changeLanguage(ctx.user!.userId, body?.language); }, { tags: ["Settings"] })
  .get("/notifications/preferences", async (ctx: AuthContext) => { authMiddleware(ctx); return settingsController.getNotificationPreferences(ctx.user!.userId); }, { tags: ["Settings"] })
  .post("/notifications/preferences", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return settingsController.setNotificationPreferences(ctx.user!.userId, body); }, { tags: ["Settings"] })
  .get("/privacy", async (ctx: AuthContext) => { authMiddleware(ctx); return settingsController.getPrivacySettings(ctx.user!.userId); }, { tags: ["Settings"] })
  .post("/privacy", async (ctx: AuthContext) => { authMiddleware(ctx); const body = (ctx as any).body; return settingsController.setPrivacySettings(ctx.user!.userId, body); }, { tags: ["Settings"] })
  .post("/data/export", async (ctx: AuthContext) => { authMiddleware(ctx); return settingsController.exportUserData(ctx.user!.userId); }, { tags: ["Settings"] });
