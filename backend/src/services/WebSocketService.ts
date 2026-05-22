import { logger } from "@utils/logger";

interface ConnectedUser {
  userId: string;
  ws: any;
}

export class WebSocketService {
  private static instance: WebSocketService;
  private connections: Map<string, ConnectedUser[]> = new Map(); // userId -> connections

  private constructor() {}

  public static getInstance(): WebSocketService {
    if (!WebSocketService.instance) {
      WebSocketService.instance = new WebSocketService();
    }
    return WebSocketService.instance;
  }

  public addConnection(userId: string, ws: any) {
    const userConnections = this.connections.get(userId) || [];
    userConnections.push({ userId, ws });
    this.connections.set(userId, userConnections);
    logger.debug(`User ${userId} connected via WebSocket`);
  }

  public removeConnection(userId: string, ws: any) {
    const userConnections = this.connections.get(userId) || [];
    const filtered = userConnections.filter(conn => conn.ws !== ws);
    if (filtered.length > 0) {
      this.connections.set(userId, filtered);
    } else {
      this.connections.delete(userId);
    }
    logger.debug(`User ${userId} disconnected from WebSocket`);
  }

  public sendToUser(userId: string, data: any) {
    const userConnections = this.connections.get(userId) || [];
    userConnections.forEach(conn => {
      try {
        conn.ws.send(JSON.stringify(data));
      } catch (e) {
        logger.error(`Failed to send message to user ${userId}`, e);
      }
    });
  }

  public broadcastToGroup(userIds: string[], data: any) {
    userIds.forEach(userId => this.sendToUser(userId, data));
  }
}

export default WebSocketService.getInstance();
