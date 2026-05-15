import { EntitySchema } from "typeorm";

export const UserSchema = new EntitySchema({
  name: "User",
  tableName: "users",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    email: { type: "varchar", unique: true },
    password_hash: { type: "varchar", nullable: true },
    google_id: { type: "varchar", unique: true, nullable: true },
    full_name: { type: "varchar", length: 100 },
    avatar_url: { type: "varchar", nullable: true },
    bio: { type: "varchar", length: 280, nullable: true },
    timezone: { type: "varchar", default: "UTC" },
    total_exp: { type: "integer", default: 0 },
    current_rank: { type: "varchar", default: "Rookie" },
    is_active: { type: "boolean", default: true },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
});

export const UserSettingsSchema = new EntitySchema({
  name: "UserSettings",
  tableName: "user_settings",
  columns: {
    user_id: { primary: true, type: "uuid" },
    language: { type: "varchar", default: "en" },
    theme: { type: "varchar", enum: ["LIGHT", "DARK", "SYSTEM"], default: "SYSTEM" },
    default_focus_minutes: { type: "integer", default: 25 },
    notifications_enabled: { type: "boolean", default: true },
    daily_focus_goal_minutes: { type: "integer", default: 120 },
    weekly_task_goal: { type: "integer", default: 10 },
    updated_at: { type: "timestamp", updateDate: true },
  },
});

export const CategorySchema = new EntitySchema({
  name: "Category",
  tableName: "categories",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    user_id: { type: "uuid" },
    name: { type: "varchar" },
    hex_color: { type: "varchar", length: 7 },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    user: {
      type: "many-to-one",
      target: "User",
      inverseSide: "categories",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
  },
});

export const GroupSchema = new EntitySchema({
  name: "Group",
  tableName: "groups",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    leader_id: { type: "uuid" },
    name: { type: "varchar" },
    description: { type: "text", nullable: true },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    leader: {
      type: "many-to-one",
      target: "User",
      inverseSide: "groups",
      joinColumn: { name: "leader_id" },
    },
  },
});

export const GroupMemberSchema = new EntitySchema({
  name: "GroupMember",
  tableName: "group_members",
  columns: {
    group_id: { primary: true, type: "uuid" },
    user_id: { primary: true, type: "uuid" },
    created_at: { type: "timestamp", createDate: true },
  },
  relations: {
    group: {
      type: "many-to-one",
      target: "Group",
      joinColumn: { name: "group_id" },
      onDelete: "CASCADE",
    },
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
  },
});

export const ScheduleSchema = new EntitySchema({
  name: "Schedule",
  tableName: "schedules",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    creator_id: { type: "uuid" },
    group_id: { type: "uuid", nullable: true },
    category_id: { type: "uuid", nullable: true },
    title: { type: "varchar" },
    description: { type: "text", nullable: true },
    location: { type: "varchar", nullable: true },
    type: { type: "varchar", enum: ["TODO", "TASK", "EVENT"] },
    status: { type: "varchar", enum: ["PENDING", "DOING", "DONE"], default: "PENDING" },
    priority: { type: "varchar", enum: ["LOW", "MEDIUM", "HIGH"], default: "MEDIUM" },
    start_time: { type: "timestamp", nullable: true },
    end_time: { type: "timestamp", nullable: true },
    deadline: { type: "timestamp", nullable: true },
    is_all_day: { type: "boolean", default: false },
    rrule: { type: "text", nullable: true },
    is_countdown_enabled: { type: "boolean", default: false },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    creator: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "creator_id" },
    },
    group: {
      type: "many-to-one",
      target: "Group",
      joinColumn: { name: "group_id" },
      nullable: true,
    },
    category: {
      type: "many-to-one",
      target: "Category",
      joinColumn: { name: "category_id" },
      nullable: true,
    },
    reminders: {
      type: "one-to-many",
      target: "Reminder",
      inverseSide: "schedule",
    },
    assignments: {
      type: "one-to-many",
      target: "ScheduleAssignment",
      inverseSide: "schedule",
    },
    collaborators: {
      type: "one-to-many",
      target: "TaskCollaborator",
      inverseSide: "schedule",
    },
  },
});

export const ReminderSchema = new EntitySchema({
  name: "Reminder",
  tableName: "reminders",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    schedule_id: { type: "uuid" },
    trigger_type: { type: "varchar" },
    custom_minutes: { type: "integer", nullable: true },
    is_notified: { type: "boolean", default: false },
    created_at: { type: "timestamp", createDate: true },
  },
  relations: {
    schedule: {
      type: "many-to-one",
      target: "Schedule",
      joinColumn: { name: "schedule_id" },
      onDelete: "CASCADE",
    },
  },
});

export const ScheduleAssignmentSchema = new EntitySchema({
  name: "ScheduleAssignment",
  tableName: "schedule_assignments",
  columns: {
    schedule_id: { primary: true, type: "uuid" },
    assignee_id: { primary: true, type: "uuid" },
    assign_status: { type: "varchar", enum: ["PENDING", "ACCEPTED", "DECLINED"], default: "PENDING" },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    schedule: {
      type: "many-to-one",
      target: "Schedule",
      joinColumn: { name: "schedule_id" },
      onDelete: "CASCADE",
    },
    assignee: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "assignee_id" },
      onDelete: "CASCADE",
    },
  },
});

export const FocusSessionSchema = new EntitySchema({
  name: "FocusSession",
  tableName: "focus_sessions",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    user_id: { type: "uuid" },
    duration_minutes: { type: "integer" },
    exp_earned: { type: "integer" },
    status: { type: "varchar", enum: ["COMPLETED", "FAILED"] },
    created_at: { type: "timestamp", createDate: true },
  },
  relations: {
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
  },
});

export const RankSchema = new EntitySchema({
  name: "Rank",
  tableName: "ranks",
  columns: {
    id: { primary: true, type: "integer" },
    rank_name: { type: "varchar", unique: true },
    min_exp: { type: "integer" },
    max_exp: { type: "integer", nullable: true },
    icon_url: { type: "varchar", nullable: true },
    created_at: { type: "timestamp", createDate: true },
  },
});

export const BadgeSchema = new EntitySchema({
  name: "Badge",
  tableName: "badges",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    name: { type: "varchar", unique: true },
    description: { type: "text" },
    icon_url: { type: "varchar" },
    criteria: { type: "varchar" },
    created_at: { type: "timestamp", createDate: true },
  },
});

export const UserBadgeSchema = new EntitySchema({
  name: "UserBadge",
  tableName: "user_badges",
  columns: {
    user_id: { primary: true, type: "uuid" },
    badge_id: { primary: true, type: "uuid" },
    unlocked_at: { type: "timestamp", createDate: true },
  },
  relations: {
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
    badge: {
      type: "many-to-one",
      target: "Badge",
      joinColumn: { name: "badge_id" },
      onDelete: "CASCADE",
    },
  },
});

export const FCMTokenSchema = new EntitySchema({
  name: "FCMToken",
  tableName: "fcm_tokens",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    user_id: { type: "uuid" },
    token: { type: "text", unique: true },
    device_name: { type: "varchar", nullable: true },
    platform: { type: "varchar", enum: ["ANDROID", "IOS", "WEB"] },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
  },
});

export const NotificationSchema = new EntitySchema({
  name: "Notification",
  tableName: "notifications",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    user_id: { type: "uuid" },
    sender_id: { type: "uuid", nullable: true },
    type: { type: "varchar", enum: ["SHARE", "COLLAB_INVITE", "TASK_ASSIGNED", "GROUP_TASK", "SYSTEM"] },
    title: { type: "varchar" },
    message: { type: "text" },
    related_id: { type: "uuid", nullable: true },
    is_read: { type: "boolean", default: false },
    created_at: { type: "timestamp", createDate: true },
  },
  relations: {
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
    sender: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "sender_id" },
      nullable: true,
      onDelete: "SET NULL",
    },
  },
});

export const ShareLogSchema = new EntitySchema({
  name: "ShareLog",
  tableName: "share_logs",
  columns: {
    id: { primary: true, type: "uuid", generated: "uuid" },
    schedule_id: { type: "uuid" },
    sender_id: { type: "uuid" },
    recipient_id: { type: "uuid" },
    share_type: { type: "varchar", enum: ["COPY", "COLLAB"] },
    new_schedule_id: { type: "uuid", nullable: true },
    created_at: { type: "timestamp", createDate: true },
  },
  relations: {
    schedule: {
      type: "many-to-one",
      target: "Schedule",
      joinColumn: { name: "schedule_id" },
      onDelete: "CASCADE",
    },
    sender: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "sender_id" },
      onDelete: "CASCADE",
    },
    recipient: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "recipient_id" },
      onDelete: "CASCADE",
    },
  },
});

export const TaskCollaboratorSchema = new EntitySchema({
  name: "TaskCollaborator",
  tableName: "task_collaborators",
  columns: {
    schedule_id: { primary: true, type: "uuid" },
    user_id: { primary: true, type: "uuid" },
    permission_level: { type: "varchar", enum: ["VIEW", "EDIT"], default: "VIEW" },
    added_at: { type: "timestamp", createDate: true },
  },
  relations: {
    schedule: {
      type: "many-to-one",
      target: "Schedule",
      inverseSide: "collaborators",
      joinColumn: { name: "schedule_id" },
      onDelete: "CASCADE",
    },
    user: {
      type: "many-to-one",
      target: "User",
      joinColumn: { name: "user_id" },
      onDelete: "CASCADE",
    },
  },
});
