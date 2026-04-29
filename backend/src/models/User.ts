import { Entity, PrimaryColumn, Column, CreateDateColumn, UpdateDateColumn, OneToOne, OneToMany, ManyToMany } from "typeorm";
import { UserSettings } from "./UserSettings";
import { Category } from "./Category";
import { Schedule } from "./Schedule";
import { Group } from "./Group";
import { GroupMember } from "./GroupMember";
import { FocusSession } from "./FocusSession";

@Entity("users")
export class User {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", unique: true })
  email: string;

  @Column({ type: "varchar" })
  password_hash: string;

  @Column({ type: "varchar", length: 100 })
  full_name: string;

  @Column({ type: "varchar", nullable: true })
  avatar_url: string;

  @Column({ type: "varchar", length: 280, nullable: true })
  bio: string;

  @Column({ type: "varchar", default: "UTC" })
  timezone: string;

  @Column({ type: "integer", default: 0 })
  total_exp: number;

  @Column({ type: "varchar", default: "Rookie" })
  current_rank: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @OneToOne(() => UserSettings, (settings) => settings.user, { cascade: true })
  settings: UserSettings;

  @OneToMany(() => Category, (category) => category.user, { cascade: true })
  categories: Category[];

  @OneToMany(() => Schedule, (schedule) => schedule.creator, { cascade: true })
  schedules: Schedule[];

  @OneToMany(() => Group, (group) => group.leader, { cascade: true })
  groups: Group[];

  @OneToMany(() => GroupMember, (member) => member.user, { cascade: true })
  groupMembers: GroupMember[];

  @OneToMany(() => FocusSession, (session) => session.user, { cascade: true })
  focusSessions: FocusSession[];
}
