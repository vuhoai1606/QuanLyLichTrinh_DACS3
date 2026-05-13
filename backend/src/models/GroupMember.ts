import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, CreateDateColumn } from "typeorm";
import { Group } from "./Group";
import { User } from "./User";

@Entity("group_members")
export class GroupMember {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  group_id: string;

  @Column("uuid")
  user_id: string;

  @CreateDateColumn({ type: "timestamp" })
  joined_at: Date;

  @ManyToOne(() => Group, (group) => group.members, { onDelete: "CASCADE" })
  @JoinColumn({ name: "group_id" })
  group: Group;

  @ManyToOne(() => User, (user) => user.groupMembers, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
