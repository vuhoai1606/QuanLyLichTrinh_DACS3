import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";

@Entity("fcm_tokens")
export class FCMToken {
  @PrimaryGeneratedColumn("uuid")
  id: string;

  @Column({ type: "uuid" })
  user_id: string;

  @Column({ type: "text", unique: true })
  token: string;

  @Column({ type: "varchar", nullable: true })
  device_name: string;

  @Column({ type: "varchar", enum: ["ANDROID", "IOS", "WEB"] })
  platform: "ANDROID" | "IOS" | "WEB";

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
