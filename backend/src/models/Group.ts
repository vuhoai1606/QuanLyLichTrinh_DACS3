import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, OneToMany, CreateDateColumn } from "typeorm";
import { User } from "./User";
import { GroupMember } from "./GroupMember";
import { Schedule } from "./Schedule";

@Entity("groups")
export class Group {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", length: 120 })
  name: string;

  @Column("uuid")
  leader_id: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @ManyToOne(() => User, (user) => user.groups, { onDelete: "CASCADE" })
  @JoinColumn({ name: "leader_id" })
  leader: User;

  @OneToMany(() => GroupMember, (member) => member.group, { cascade: true })
  members: GroupMember[];

  @OneToMany(() => Schedule, (schedule) => schedule.group, { cascade: true })
  schedules: Schedule[];
}
