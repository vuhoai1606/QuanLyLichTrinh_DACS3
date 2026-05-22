import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import aiService from "@services/AIService";
import scheduleService from "@services/ScheduleService";
import { successResponse } from "@utils/errors";

export const aiRoutes = new Elysia({ prefix: "/ai" })
  .onBeforeHandle(authMiddleware)
  .post("/breakdown", async (ctx: AuthContext) => {
    const { title, description } = await ctx.request.json() as any;
    const subTasks = await aiService.breakdownTask(title, description);
    return successResponse(subTasks, "Task broken down");
  })
  .get("/suggest-schedule", async (ctx: AuthContext) => {
    const result = await scheduleService.getSchedulesForUser(ctx.user!.userId, 20, 0);
    const suggestion = await aiService.suggestSchedule(ctx.user!.userId, result.data);
    return successResponse(suggestion, "Schedule suggestion generated");
  });
