import { Elysia } from "elysia";
import notificationController from "@controllers/NotificationController";

export const notificationRoutes = new Elysia({ prefix: "/notifications" })
  .post("/fcm-token", async ({ body }: { body: any }) => notificationController.saveFCMToken(body), { tags: ["Notifications"] })
  .delete("/fcm-token/:tokenId", async ({ params }: { params: any }) => notificationController.removeFCMToken(params.tokenId), { tags: ["Notifications"] })
  .get("/fcm-tokens/:userId", async ({ params }: { params: any }) => notificationController.getFCMTokens(params.userId), { tags: ["Notifications"] })
  .get("/", async ({ query }: { query: any }) => notificationController.getNotifications(query), { tags: ["Notifications"] })
  .get("/unread/:userId", async ({ params }: { params: any }) => notificationController.getUnreadNotifications(params.userId), { tags: ["Notifications"] })
  .patch("/:notificationId/read", async ({ params }: { params: any }) => notificationController.markAsRead(params.notificationId), { tags: ["Notifications"] })
  .patch("/user/:userId/read-all", async ({ params }: { params: any }) => notificationController.markAllAsRead(params.userId), { tags: ["Notifications"] })
  .delete("/:notificationId", async ({ params }: { params: any }) => notificationController.deleteNotification(params.notificationId), { tags: ["Notifications"] })
  .get("/stats/:userId", async ({ params }: { params: any }) => notificationController.getNotificationStats(params.userId), { tags: ["Notifications"] })
  .post("/test/:userId", async ({ params }: { params: any }) => notificationController.sendTestNotification(params.userId), { tags: ["Notifications"] })
  .put("/:notificationId", async ({ params, body }: { params: any; body: any }) => notificationController.updateNotification(params.notificationId, body), { tags: ["Notifications"] })
  .delete("/clear-all/:userId", async ({ params }: { params: any }) => notificationController.clearAllNotifications(params.userId), { tags: ["Notifications"] })
  .get("/preferences/:userId", async ({ params }: { params: any }) => notificationController.getNotificationPreferences(params.userId), { tags: ["Notifications"] })
  .post("/preferences/:userId", async ({ params, body }: { params: any; body: any }) => notificationController.setNotificationPreferences(params.userId, body), { tags: ["Notifications"] })
  .post("/bulk-send", async ({ body }: { body: any }) => notificationController.sendBulkNotifications(body), { tags: ["Notifications"] });
