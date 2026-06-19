import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, OneToMany, CreateDateColumn } from "typeorm";
import type { User } from "./User";
import { GroupMember } from "./GroupMember";
import type { Schedule } from "./Schedule";

@Entity("groups")
export class Group {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", length: 120 })
  name: string;

  @Column({ type: "varchar", nullable: true })
  avatar_url: string;

  @Column("uuid")
  leader_id: string;

  @Column({ type: "text", nullable: true })
  description: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @Column({ type: "timestamp" })
  updated_at: Date;

  @ManyToOne("User", (user) => user.groups, { onDelete: "CASCADE" })
  @JoinColumn({ name: "leader_id" })
  leader: User;

  @OneToMany(() => GroupMember, (member) => member.group, { cascade: true })
  members: GroupMember[];

  @OneToMany("Schedule", (schedule) => schedule.group, { cascade: true })
  schedules: Schedule[];
}
