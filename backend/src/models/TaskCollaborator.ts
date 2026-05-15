import { Entity, PrimaryColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";
import { Schedule } from "./Schedule";

@Entity("task_collaborators")
export class TaskCollaborator {
  @PrimaryColumn("uuid")
  schedule_id: string;

  @PrimaryColumn("uuid")
  user_id: string;

  @Column({ type: "varchar", enum: ["VIEW", "EDIT"], default: "VIEW" })
  permission_level: "VIEW" | "EDIT";

  @CreateDateColumn({ type: "timestamp" })
  added_at: Date;

  @ManyToOne(() => Schedule, (schedule) => schedule.collaborators, { onDelete: "CASCADE" })
  @JoinColumn({ name: "schedule_id" })
  schedule: Schedule;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
