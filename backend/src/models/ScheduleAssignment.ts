import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn } from "typeorm";
import type { Schedule } from "./Schedule";
import type { User } from "./User";

@Entity("schedule_assignments")
export class ScheduleAssignment {
  @PrimaryColumn("uuid")
  schedule_id: string;

  @PrimaryColumn("uuid")
  assignee_id: string;

  @Column({ type: "varchar", enum: ["PENDING", "ACCEPTED", "DECLINED"], default: "PENDING" })
  assign_status: "PENDING" | "ACCEPTED" | "DECLINED";

  @Column({ type: "timestamp" })
  created_at: Date;

  @Column({ type: "timestamp" })
  updated_at: Date;

  @ManyToOne("Schedule", (schedule) => schedule.assignments, { onDelete: "CASCADE" })
  @JoinColumn({ name: "schedule_id" })
  schedule: Schedule;

  @ManyToOne("User", { onDelete: "CASCADE" })
  @JoinColumn({ name: "assignee_id" })
  assignee: User;
}
