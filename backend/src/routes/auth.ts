import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import authController from "@controllers/AuthController";

export const authRoutes = new Elysia({ prefix: "/auth" })
  .post(
    "/register",
    async ({ body }: { body: any }) => authController.register(body),
    { tags: ["Auth"] }
  )
  .post(
    "/login",
    async ({ body }: { body: any }) => authController.login(body),
    { tags: ["Auth"] }
  )
  .post(
    "/google-login",
    async ({ body }: { body: any }) => authController.googleLogin(body),
    { tags: ["Auth"] }
  )
  .get(
    "/me",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      return authController.getProfile(ctx);
    },
    { tags: ["Auth"] }
  )
  .get(
    "/profile",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      return authController.getProfile(ctx);
    },
    { tags: ["Auth"] }
  )
  .put(
    "/profile",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      const body = await (ctx.request as any).json();
      return authController.updateProfile(ctx, body);
    },
    { tags: ["Auth"] }
  )
  .post(
    "/change-password",
    async (ctx: AuthContext) => {
      authMiddleware(ctx);
      const body = await (ctx.request as any).json();
      return authController.changePassword(ctx, body);
    },
    { tags: ["Auth"] }
  )
  .post(
    "/forgot-password",
    async ({ body }: { body: any }) => authController.forgotPassword(body),
    { tags: ["Auth"] }
  )
  .post(
    "/reset-password",
    async ({ body }: { body: any }) => authController.resetPassword(body),
    { tags: ["Auth"] }
  )
  .post(
    "/refresh",
    async ({ body }: { body: any }) => authController.refreshToken(body),
    { tags: ["Auth"] }
  )
  .post(
    "/verify",
    async ({ body }: { body: any }) => authController.verifyToken(body),
    { tags: ["Auth"] }
  );
