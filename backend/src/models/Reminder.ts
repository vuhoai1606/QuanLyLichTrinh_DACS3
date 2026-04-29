import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn } from "typeorm";
import { Schedule } from "./Schedule";

@Entity("reminders")
export class Reminder {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  schedule_id: string;

  @Column({ type: "varchar", enum: ["WHEN_STARTS", "MIN_5", "MIN_10", "MIN_30", "HOUR_1", "DAY_1", "WEEK_1", "CUSTOM"] })
  trigger_type: "WHEN_STARTS" | "MIN_5" | "MIN_10" | "MIN_30" | "HOUR_1" | "DAY_1" | "WEEK_1" | "CUSTOM";

  @Column({ type: "timestamp", nullable: true })
  custom_time: Date;

  @Column({ type: "boolean", default: false })
  is_alarm: boolean;

  @Column({ type: "varchar", nullable: true })
  sound_uri: string;

  @Column({ type: "timestamp", nullable: true })
  last_triggered_at: Date;

  @ManyToOne(() => Schedule, (schedule) => schedule.reminders, { onDelete: "CASCADE" })
  @JoinColumn({ name: "schedule_id" })
  schedule: Schedule;
}
