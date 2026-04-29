import { Entity, PrimaryColumn, Column, OneToOne, JoinColumn, UpdateDateColumn } from "typeorm";
import { User } from "./User";

@Entity("user_settings")
export class UserSettings {
  @PrimaryColumn("uuid")
  user_id: string;

  @Column({ type: "varchar", default: "en" })
  language: string;

  @Column({ type: "varchar", enum: ["LIGHT", "DARK", "SYSTEM"], default: "SYSTEM" })
  theme: "LIGHT" | "DARK" | "SYSTEM";

  @Column({ type: "integer", default: 25 })
  default_focus_minutes: number;

  @Column({ type: "boolean", default: true })
  notifications_enabled: boolean;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @OneToOne(() => User, (user) => user.settings, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
