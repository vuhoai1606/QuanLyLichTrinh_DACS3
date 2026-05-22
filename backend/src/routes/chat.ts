import { Elysia } from "elysia";
import { AuthContext, authMiddleware } from "@middleware/auth";
import chatController from "@controllers/ChatController";

export const chatRoutes = new Elysia({ prefix: "/chat" })
  .onBeforeHandle(authMiddleware)
  .get("/:groupId", (ctx: AuthContext & { params: any }) => chatController.getMessages(ctx.params.groupId))
  .post("/send", async (ctx: AuthContext) => chatController.sendMessage(ctx.user!.userId, await ctx.request.json()));
