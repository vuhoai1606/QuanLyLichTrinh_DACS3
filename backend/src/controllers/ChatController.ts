import { AppDataSource } from "@config/database";
import { ChatMessage } from "@models/ChatMessage";
import { successResponse, errorResponse } from "@utils/errors";
import { logger } from "@utils/logger";
import webSocketService from "@services/WebSocketService";

export class ChatController {
  async getMessages(groupId: string) {
    try {
      const chatRepo = AppDataSource.getRepository("ChatMessage");
      const messages = await chatRepo.find({
        where: { groupId },
        relations: ["sender"],
        order: { created_at: "ASC" },
        take: 50
      });
      return successResponse(messages, "Messages retrieved");
    } catch (error) {
      logger.error("getMessages error", error);
      return errorResponse(500, "Internal server error");
    }
  }

  async sendMessage(userId: string, body: any) {
    const { groupId, message, type, metadata } = body;
    if (!groupId || !message) return errorResponse(400, "Missing fields");

    try {
      const chatRepo = AppDataSource.getRepository("ChatMessage");
      const chatMsg = chatRepo.create({
        groupId,
        senderId: userId,
        message,
        type: type || "TEXT",
        metadata
      });
      const savedMsg = await chatRepo.save(chatMsg);
      
      // Notify group via WebSocket
      const groupMemberRepo = AppDataSource.getRepository("GroupMember");
      const members = await groupMemberRepo.find({ where: { group_id: groupId } });
      const userIds = members.map(m => m.user_id);
      
      webSocketService.broadcastToGroup(userIds, {
        type: "NEW_CHAT_MESSAGE",
        data: savedMsg
      });

      return successResponse(savedMsg, "Message sent", 201);
    } catch (error) {
      logger.error("sendMessage error", error);
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new ChatController();
