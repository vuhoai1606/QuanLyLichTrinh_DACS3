import { DataSource } from "typeorm";
import { config } from "@config/env";
import {
  UserSchema,
  UserSettingsSchema,
  CategorySchema,
  GroupSchema,
  GroupMemberSchema,
  ScheduleSchema,
  ReminderSchema,
  ScheduleAssignmentSchema,
  FocusSessionSchema,
  RankSchema,
  BadgeSchema,
  UserBadgeSchema,
  FCMTokenSchema,
  NotificationSchema,
  ShareLogSchema,
  TaskCollaboratorSchema,
  ChatMessageSchema,
  RefreshTokenSchema,
} from "@config/schemas";

export let AppDataSource: DataSource;

export async function connectDB() {
  try {
    AppDataSource = new DataSource({
      type: "postgres",
      host: config.database.host,
      port: config.database.port,
      username: config.database.user,
      password: config.database.password,
      database: config.database.name,
      ssl: config.database.ssl ? { rejectUnauthorized: false } : false,
      synchronize: config.nodeEnv === "development" || config.nodeEnv === "test",
      logging: config.nodeEnv === "development",
      entities: [
        UserSchema,
        UserSettingsSchema,
        CategorySchema,
        GroupSchema,
        GroupMemberSchema,
        ScheduleSchema,
        ReminderSchema,
        ScheduleAssignmentSchema,
        FocusSessionSchema,
        RankSchema,
        BadgeSchema,
        UserBadgeSchema,
        FCMTokenSchema,
        NotificationSchema,
        ShareLogSchema,
        TaskCollaboratorSchema,
        ChatMessageSchema,
        RefreshTokenSchema,
      ],
      migrations: ["src/migrations/*.ts"],
      subscribers: [],
    });

    if (!AppDataSource.isInitialized) {
      await AppDataSource.initialize();
      console.log("✅ PostgreSQL connected successfully");
    }
    return AppDataSource;
  } catch (error) {
    console.error("❌ PostgreSQL connection failed:", error);
    process.exit(1);
  }
};

export default { AppDataSource, connectDB };
