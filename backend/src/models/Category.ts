import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";

@Entity("categories")
export class Category {
  @PrimaryColumn("uuid")
  id: string;

  @Column("uuid")
  user_id: string;

  @Column({ type: "varchar", length: 50 })
  name: string;

  @Column({ type: "varchar" })
  hex_color: string;

  @ManyToOne(() => User, (user) => user.categories, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;
}
