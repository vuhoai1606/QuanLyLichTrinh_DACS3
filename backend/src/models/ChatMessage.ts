import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import type { User } from "./User";
import type { Group } from "./Group";

@Entity("chat_messages")
export class ChatMessage {
  @PrimaryGeneratedColumn("uuid")
  id: string;

  @Column({ name: "group_id" })
  groupId: string;

  @ManyToOne("Group")
  @JoinColumn({ name: "group_id" })
  group: Group;

  @Column({ name: "sender_id" })
  senderId: string;

  @ManyToOne("User")
  @JoinColumn({ name: "sender_id" })
  sender: User;

  @Column({ type: "text" })
  message: string;

  @Column({ default: "TEXT" })
  type: string; // TEXT, IMAGE, FILE

  @Column({ type: "json", nullable: true })
  metadata: any; // For sharing files, etc.

  @CreateDateColumn()
  created_at: Date;
}

