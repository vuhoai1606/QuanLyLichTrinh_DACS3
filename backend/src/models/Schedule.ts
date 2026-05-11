import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, OneToMany, CreateDateColumn, UpdateDateColumn } from "typeorm";
import { User } from "./User";
import { Group } from "./Group";
import { Category } from "./Category";
import { Reminder } from "./Reminder";
import { ScheduleAssignment } from "./ScheduleAssignment";
import { ShareLog } from "./ShareLog";
import { TaskCollaborator } from "./TaskCollaborator";

@Entity("schedules")
export class Schedule {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  creator_id: string;

  @Column("uuid", { nullable: true })
  group_id: string;

  @Column("uuid", { nullable: true })
  category_id: string;

  @Column({ type: "varchar", length: 100 })
  title: string;

  @Column({ type: "text", nullable: true })
  description: string;

  @Column({ type: "varchar", nullable: true })
  location: string;

  @Column({ type: "varchar", enum: ["TODO", "TASK", "EVENT"] })
  type: "TODO" | "TASK" | "EVENT";

  @Column({ type: "varchar", enum: ["LOW", "MEDIUM", "HIGH"], default: "MEDIUM" })
  priority: "LOW" | "MEDIUM" | "HIGH";

  @Column({ type: "timestamp", nullable: true })
  start_time: Date;

  @Column({ type: "timestamp", nullable: true })
  end_time: Date;

  @Column({ type: "timestamp", nullable: true })
  deadline: Date;

  @Column({ type: "boolean", default: false })
  is_all_day: boolean;

  @Column({ type: "text", nullable: true })
  rrule: string;

  @Column({ type: "varchar", enum: ["PENDING", "DOING", "DONE"], default: "PENDING" })
  status: "PENDING" | "DOING" | "DONE";

  @Column({ type: "boolean", default: false })
  is_countdown_enabled: boolean;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @ManyToOne(() => User, (user) => user.schedules, { onDelete: "CASCADE" })
  @JoinColumn({ name: "creator_id" })
  creator: User;

  @ManyToOne(() => Group, (group) => group.schedules, { onDelete: "CASCADE" })
  @JoinColumn({ name: "group_id" })
  group: Group;

  @ManyToOne(() => Category)
  @JoinColumn({ name: "category_id" })
  category: Category;

  @OneToMany(() => Reminder, (reminder) => reminder.schedule, { cascade: true })
  reminders: Reminder[];

  @OneToMany(() => ScheduleAssignment, (assignment) => assignment.schedule, { cascade: true })
  assignments: ScheduleAssignment[];

  @OneToMany(() => ShareLog, (shareLog) => shareLog.schedule)
  shareLogs: ShareLog[];

  @OneToMany(() => TaskCollaborator, (collaborator) => collaborator.schedule)
  collaborators: TaskCollaborator[];
}
