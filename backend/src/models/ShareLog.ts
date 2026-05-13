import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";
import { Schedule } from "./Schedule";

@Entity("share_logs")
export class ShareLog {
  @PrimaryGeneratedColumn("uuid")
  id: string;

  @Column({ type: "uuid" })
  schedule_id: string;

  @Column({ type: "uuid" })
  sender_id: string;

  @Column({ type: "uuid" })
  recipient_id: string;

  @Column({ type: "varchar", enum: ["COPY", "COLLAB"] })
  share_type: "COPY" | "COLLAB";

  @Column({ type: "uuid", nullable: true })
  new_schedule_id: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @ManyToOne(() => Schedule, { onDelete: "CASCADE" })
  @JoinColumn({ name: "schedule_id" })
  schedule: Schedule;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "sender_id" })
  sender: User;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "recipient_id" })
  recipient: User;
}
