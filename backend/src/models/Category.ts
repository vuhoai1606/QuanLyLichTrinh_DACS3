import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn, CreateDateColumn, UpdateDateColumn } from "typeorm";
import type { User } from "./User";

@Entity("categories")
export class Category {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "uuid" })
  user_id: string;

  @Column({ type: "varchar" })
  name: string;

  @Column({ type: "varchar", length: 7 })
  hex_color: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  @UpdateDateColumn({ type: "timestamp" })
  updated_at: Date;

  @ManyToOne("User", (user: any) => user.categories, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
