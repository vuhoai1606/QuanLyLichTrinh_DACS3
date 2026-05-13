import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn } from "typeorm";
import { Schedule } from "./Schedule";
import { User } from "./User";

@Entity("schedule_assignments")
export class ScheduleAssignment {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  schedule_id: string;

  @Column("uuid")
  assignee_id: string;

  @Column({ type: "varchar", enum: ["PENDING", "ACCEPTED", "DECLINED"], default: "PENDING" })
  assign_status: "PENDING" | "ACCEPTED" | "DECLINED";

  @ManyToOne(() => Schedule, (schedule) => schedule.assignments, { onDelete: "CASCADE" })
  @JoinColumn({ name: "schedule_id" })
  schedule: Schedule;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "assignee_id" })
  assignee: User;
}
