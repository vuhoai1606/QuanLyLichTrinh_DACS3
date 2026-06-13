import { Entity, PrimaryColumn, Column, CreateDateColumn, UpdateDateColumn, OneToOne, OneToMany } from "typeorm";
import { UserSettings } from "./UserSettings";
import { Category } from "./Category";
import type { Schedule } from "./Schedule";
import type { Group } from "./Group";
import { GroupMember } from "./GroupMember";
import { FocusSession } from "./FocusSession";
import { Notification } from "./Notification";
import { UserBadge } from "./UserBadge";
import { FCMToken } from "./FCMToken";
import { TaskCollaborator } from "./TaskCollaborator";

@Entity("users")
export class User {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", unique: true })
  email: string;

  @Column({ type: "varchar", unique: true, nullable: true })
  google_id: string;

  @Column({ type: "varchar", nullable: true })
  password_hash: string;

  @Column({ type: "varchar", length: 100 })
  full_name: string;

  @Column({ type: "varchar", nullable: true })
  avatar_url: string;

  @Column({ type: "varchar", nullable: true })
  bio: string;

  @Column({ type: "varchar", nullable: true })
  gender: string;

  @Column({ type: "varchar", nullable: true })
  dob: string;

  @Column({ type: "varchar", default: "UTC" })
  timezone: string;

  @Column({ type: "integer", default: 0 })
  total_exp: number;

  @Column({ type: "varchar", default: "Rookie" })
  current_rank: string;

  @Column({ type: "integer", default: 0 })
  current_streak: number;

  @Column({ type: "integer", default: 0 })
  best_streak: number;

  @Column({ type: "timestamp", nullable: true })
  last_active_date: Date;

  @Column({ type: "boolean", default: true })
  is_active: boolean;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @OneToOne("User"Settings, (settings) => settings.user, { cascade: true })
  settings: UserSettings;

  @OneToMany(() => Category, (category) => category.user, { cascade: true })
  categories: Category[];

  @OneToMany("Schedule", (schedule) => schedule.creator, { cascade: true })
  schedules: Schedule[];

  @OneToMany("Group", (group) => group.leader, { cascade: true })
  groups: Group[];

  @OneToMany("Group"Member, (member) => member.user, { cascade: true })
  groupMembers: GroupMember[];

  @OneToMany(() => FocusSession, (session) => session.user, { cascade: true })
  focusSessions: FocusSession[];

  @OneToMany(() => Notification, (notification) => notification.user, { cascade: true })
  notifications: Notification[];

  @OneToMany("User"Badge, (userBadge) => userBadge.user, { cascade: true })
  badges: UserBadge[];

  @OneToMany(() => FCMToken, (token) => token.user, { cascade: true })
  fcmTokens: FCMToken[];

  @OneToMany(() => TaskCollaborator, (collab) => collab.user)
  collaborations: TaskCollaborator[];
}
