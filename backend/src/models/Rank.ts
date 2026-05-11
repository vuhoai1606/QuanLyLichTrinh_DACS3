import { Entity, PrimaryColumn, Column, CreateDateColumn } from "typeorm";

@Entity("ranks")
export class Rank {
  @PrimaryColumn("integer")
  id: number;

  @Column({ type: "varchar", unique: true })
  rank_name: string;

  @Column({ type: "integer" })
  min_exp: number;

  @Column({ type: "integer", nullable: true })
  max_exp: number;

  @Column({ type: "varchar", nullable: true })
  icon_url: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;
}
