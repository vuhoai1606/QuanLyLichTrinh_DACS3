import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import { errorResponse } from "@utils/errors";
import { extractToken, verifyToken } from "@utils/jwt";
import userController from "@controllers/UserController";

export const userRoutes = new Elysia({ prefix: "/users" })
  .get(
    "/me",
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
      return userController.getProfile(ctx);
    },
    { tags: ["Users"] }
  )
  .patch(
    "/profile",
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
      return userController.updateProfile(ctx);
    },
    { tags: ["Users"] }
  )
  .get("/search", async ({ query }: { query: any }) => userController.searchUsers(query), { tags: ["Users"] })
  .get("/:id/public", async ({ params }: { params: any }) => userController.getPublicProfile(params.id), { tags: ["Users"] })
  .get("/:id/stats", async ({ params }: { params: any }) => userController.getUserStats(params.id), { tags: ["Users"] })
  .post("/follow/:userId", async ({ params, query }: { params: any; query: any }) => userController.followUser(params.userId, query), { tags: ["Users"] })
  .delete("/follow/:userId", async ({ params, query }: { params: any; query: any }) => userController.unfollowUser(params.userId, query), { tags: ["Users"] })
  .get("/followers/:userId", async ({ params }: { params: any }) => userController.getFollowers(params.userId), { tags: ["Users"] })
  .get("/following/:userId", async ({ params }: { params: any }) => userController.getFollowing(params.userId), { tags: ["Users"] });
