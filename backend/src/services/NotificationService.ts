import * as admin from "firebase-admin";
import { logger } from "@utils/logger";

export class NotificationService {
  private static instance: NotificationService;

  private constructor() {
    try {
      // In production, initialize with service account
      // admin.initializeApp({
      //   credential: admin.credential.cert(require("./firebase-service-account.json"))
      // });
      logger.info("Firebase Admin initialized (Placeholder)");
    } catch (e) {
      logger.error("Firebase Admin initialization failed", e);
    }
  }

  public static getInstance(): NotificationService {
    if (!NotificationService.instance) {
      NotificationService.instance = new NotificationService();
    }
    return NotificationService.instance;
  }

  async sendPushNotification(token: string, title: string, body: string, data?: any) {
    try {
      const message = {
        notification: { title, body },
        token,
        data: data || {}
      };
      
      // await admin.messaging().send(message);
      logger.debug(`Push sent to ${token}: ${title}`);
    } catch (error) {
      logger.error("sendPushNotification error", error);
    }
  }
}

export default NotificationService.getInstance();
