import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, CreateDateColumn } from "typeorm";
import { Group } from "./Group";
import { User } from "./User";

@Entity("group_members")
export class GroupMember {
  @PrimaryColumn("uuid")
  group_id: string;

  @PrimaryColumn("uuid")
  user_id: string;

  @Column({ type: "varchar", default: "MEMBER" })
  role: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @ManyToOne(() => Group, (group) => group.members, { onDelete: "CASCADE" })
  @JoinColumn({ name: "group_id" })
  group: Group;

  @ManyToOne(() => User, (user) => user.groupMembers, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
