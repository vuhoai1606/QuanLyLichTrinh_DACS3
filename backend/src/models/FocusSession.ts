import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, CreateDateColumn } from "typeorm";
import { User } from "./User";

@Entity("focus_sessions")
export class FocusSession {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  user_id: string;

  @Column({ type: "integer" })
  duration_minutes: number;

  @Column({ type: "varchar", enum: ["COMPLETED", "FAILED"] })
  status: "COMPLETED" | "FAILED";

  @Column({ type: "integer", default: 0 })
  exp_earned: number;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @ManyToOne(() => User, (user) => user.focusSessions, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
