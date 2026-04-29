import { Elysia } from "elysia";
import gamificationController from "@controllers/GamificationController";

export const gamificationRoutes = new Elysia({ prefix: "/gamification" })
  .get("/ranks", async () => gamificationController.getRanks(), { tags: ["Gamification"] })
  .get("/leaderboard", async ({ query }: { query: any }) => gamificationController.getLeaderboard(query), { tags: ["Gamification"] })
  .get("/user/:userId/rank-info", async ({ params }: { params: any }) => gamificationController.getRankInfo(params.userId), { tags: ["Gamification"] })
  .get("/badges", async ({ query }: { query: any }) => gamificationController.getBadges(query), { tags: ["Gamification"] })
  .get("/all-badges", async () => gamificationController.getAllBadges(), { tags: ["Gamification"] })
  .post("/badges/unlock", async ({ body }: { body: any }) => gamificationController.unlockBadge(body), { tags: ["Gamification"] });
