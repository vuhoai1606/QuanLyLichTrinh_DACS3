import { Entity, PrimaryColumn, Column, ManyToOne, JoinColumn } from "typeorm";
import type { User } from "./User";

@Entity("otp_codes")
export class OTPCode {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", length: 255 })
  email: string;

  @Column({ type: "varchar", length: 10 })
  code: string;

  @Column({ type: "varchar", length: 50 })
  purpose: string; // "REGISTRATION", "FORGOT_PASSWORD"

  @Column({ type: "timestamp" })
  expires_at: Date;

  @Column({ type: "timestamp" })
  created_at: Date;
}
