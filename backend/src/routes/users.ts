import { Elysia } from "elysia";
import userController from "@controllers/UserController";

export const userRoutes = new Elysia({ prefix: "/users" })
  .get("/search", async ({ query }: { query: any }) => userController.searchUsers(query), { tags: ["Users"] })
  .get("/:id/public", async ({ params }: { params: any }) => userController.getPublicProfile(params.id), { tags: ["Users"] })
  .get("/:id/stats", async ({ params }: { params: any }) => userController.getUserStats(params.id), { tags: ["Users"] })
  .post("/follow/:userId", async ({ params, query }: { params: any; query: any }) => userController.followUser(params.userId, query), { tags: ["Users"] })
  .delete("/follow/:userId", async ({ params, query }: { params: any; query: any }) => userController.unfollowUser(params.userId, query), { tags: ["Users"] })
  .get("/followers/:userId", async ({ params }: { params: any }) => userController.getFollowers(params.userId), { tags: ["Users"] })
  .get("/following/:userId", async ({ params }: { params: any }) => userController.getFollowing(params.userId), { tags: ["Users"] });
