import { EntitySchema } from "typeorm";

export const UserSchema = new EntitySchema({
  name: "User",
  tableName: "users",
  columns: {
    id: { primary: true, type: "uuid", default: "uuid_generate_v4()" },
    email: { type: "varchar", unique: true },
    password: { type: "varchar" },
    full_name: { type: "varchar", nullable: true },
    avatar_url: { type: "varchar", nullable: true },
    total_exp: { type: "integer", default: 0 },
    rank: { type: "varchar", default: "Rookie" },
    created_at: { type: "timestamp", createDate: true },
    updated_at: { type: "timestamp", updateDate: true },
  },
  relations: {
    settings: {
      type: "one-to-one",
      target: "UserSettings",
      inverseSide: "user",
      onDelete: "CASCADE",
    },
    categories: {
      type: "one-to-many",
      target: "Category",
      inverseSide: "user",
      onDelete: "CASCADE",
    },
    schedules: {
      type: "one-to-many",
      target: "Schedule",
      inverseSide: "creator",
      onDelete: "CASCADE",
    },
    groups: {
      type: "one-to-many",
      target: "Group",
      inverseSide: "leader",
      onDelete: "CASCADE",
    },
    focus_sessions: {
      type: "one-to-many",
      target: "FocusSession",
      inverseSide: "user",
      onDelete: "CASCADE",
    },
  },
});
