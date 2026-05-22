import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";

@Entity("notifications")
export class Notification {
  @PrimaryGeneratedColumn("uuid")
  id: string;

  @Column({ type: "uuid" })
  user_id: string;

  @Column({ type: "uuid", nullable: true })
  sender_id: string;

  @Column({ type: "varchar", enum: ["SHARE", "COLLAB_INVITE", "TASK_ASSIGNED", "GROUP_TASK", "SYSTEM"] })
  type: "SHARE" | "COLLAB_INVITE" | "TASK_ASSIGNED" | "GROUP_TASK" | "SYSTEM";

  @Column({ type: "varchar" })
  title: string;

  @Column({ type: "text" })
  message: string;

  @Column({ type: "uuid", nullable: true })
  related_id: string;

  @Column({ type: "boolean", default: false })
  ia_read: boolean;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;

  @ManyToOne(() => User, { nullable: true, onDelete: "SET NULL" })
  @JoinColumn({ name: "sender_id" })
  sender: User;
}
