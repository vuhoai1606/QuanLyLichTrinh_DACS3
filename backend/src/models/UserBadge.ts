import { Entity, PrimaryColumn, CreateDateColumn, ManyToOne, JoinColumn } from "typeorm";
import { User } from "./User";
import { Badge } from "./Badge";

@Entity("user_badges")
export class UserBadge {
  @PrimaryColumn("uuid")
  user_id: string;

  @PrimaryColumn("uuid")
  badge_id: string;

  @CreateDateColumn({ type: "timestamp" })
  unlocked_at: Date;

  @ManyToOne(() => User, { onDelete: "CASCADE" })
  @JoinColumn({ name: "user_id" })
  user: User;

  @ManyToOne(() => Badge, (badge) => badge.userBadges, { onDelete: "CASCADE" })
  @JoinColumn({ name: "badge_id" })
  badge: Badge;
}
