import { AppDataSource } from "@config/database";

class GamificationService {
  // Initialize default ranks
  async initializeRanks() {
    try {
      const rankRepo = AppDataSource.getRepository("Rank");
      const existingRanks = await rankRepo.count();

      if (existingRanks === 0) {
        const ranks = [
          { id: 1, rank_name: "Rookie", min_exp: 0, max_exp: 100 },
          { id: 2, rank_name: "Novice", min_exp: 100, max_exp: 300 },
          { id: 3, rank_name: "Apprentice", min_exp: 300, max_exp: 600 },
          { id: 4, rank_name: "Adept", min_exp: 600, max_exp: 1000 },
          { id: 5, rank_name: "Expert", min_exp: 1000, max_exp: 1500 },
          { id: 6, rank_name: "Master", min_exp: 1500, max_exp: 2500 },
          { id: 7, rank_name: "Legend", min_exp: 2500, max_exp: null },
        ];

        await rankRepo.save(ranks);
      }
    } catch (error) {
      console.error("❌ Initialize ranks error:", error);
    }
  }

  async getRanks() {
    try {
      const rankRepo = AppDataSource.getRepository("Rank");
      const ranks = await rankRepo.find({
        order: { id: "ASC" },
      });
      return ranks;
    } catch (error) {
      console.error("❌ Get ranks error:", error);
      throw error;
    }
  }

  async getLeaderboard(
    userId: string,
    type: "friends" | "global" = "global",
    limit: number = 20
  ) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      let query = userRepo
        .createQueryBuilder("user")
        .select([
          "user.id",
          "user.full_name",
          "user.avatar_url",
          "user.total_exp",
          "user.current_rank",
        ])
        .orderBy("user.total_exp", "DESC")
        .take(limit);

      if (type === "friends") {
        // Get friend list from groups
        const groupMemberRepo = AppDataSource.getRepository("GroupMember");
        const friendsInGroups = await groupMemberRepo
          .createQueryBuilder("gm")
          .select("DISTINCT gm.user_id")
          .where(
            "gm.group_id IN (SELECT gm2.group_id FROM group_members gm2 WHERE gm2.user_id = :userId)",
            { userId }
          )
          .getMany();

        const friendIds = friendsInGroups.map((f) => f.user_id);
        if (friendIds.length > 0) {
          query = query.where("user.id IN (:...friendIds)", { friendIds });
        } else {
          return [];
        }
      }

      const leaderboard = await query.getMany();

      return leaderboard.map((user, index) => ({
        rank: index + 1,
        ...user,
      }));
    } catch (error) {
      console.error("❌ Get leaderboard error:", error);
      throw error;
    }
  }

  async getUserRankInfo(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      const rankRepo = AppDataSource.getRepository("Rank");

      const user = await userRepo.findOne({
        where: { id: userId },
        select: ["id", "total_exp", "current_rank"],
      });

      if (!user) return null;

      const currentRank = await rankRepo.findOne({
        where: { rank_name: user.current_rank },
      });

      const nextRank = await rankRepo.findOne({
        where: { id: (currentRank?.id || 0) + 1 },
      });

      const expToNextLevel = nextRank
        ? nextRank.min_exp - user.total_exp
        : null;

      return {
        user_id: userId,
        current_rank: user.current_rank,
        total_exp: user.total_exp,
        next_rank: nextRank?.rank_name || null,
        exp_to_next_level: expToNextLevel,
        rank_info: currentRank,
      };
    } catch (error) {
      console.error("❌ Get user rank info error:", error);
      throw error;
    }
  }

  async updateUserRank(userId: string, newExp: number) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      const rankRepo = AppDataSource.getRepository("Rank");

      const user = await userRepo.findOne({ where: { id: userId } });
      if (!user) return null;

      const newTotalExp = user.total_exp + newExp;

      // Find appropriate rank based on EXP
      const newRank = await rankRepo
        .createQueryBuilder("rank")
        .where("rank.min_exp <= :exp AND (rank.max_exp IS NULL OR rank.max_exp > :exp)", {
          exp: newTotalExp,
        })
        .orderBy("rank.id", "DESC")
        .getOne();

      if (newRank) {
        await userRepo.update(
          { id: userId },
          {
            total_exp: newTotalExp,
            current_rank: newRank.rank_name,
          }
        );
      }

      return { total_exp: newTotalExp, current_rank: newRank?.rank_name };
    } catch (error) {
      console.error("❌ Update user rank error:", error);
      throw error;
    }
  }

  // Badge Management
  async getAllBadges() {
    try {
      const badgeRepo = AppDataSource.getRepository("Badge");
      return await badgeRepo.find();
    } catch (error) {
      console.error("❌ Get all badges error:", error);
      throw error;
    }
  }

  async getUserBadges(userId: string) {
    try {
      const userBadgeRepo = AppDataSource.getRepository("UserBadge");

      const userBadges = await userBadgeRepo
        .createQueryBuilder("ub")
        .leftJoinAndSelect("ub.badge", "badge")
        .where("ub.user_id = :userId", { userId })
        .select([
          "badge.id",
          "badge.name",
          "badge.description",
          "badge.icon_url",
          "ub.unlocked_at",
        ])
        .getMany();

      return userBadges.map((ub) => ({
        ...ub.badge,
        unlocked_at: ub.unlocked_at,
      }));
    } catch (error) {
      console.error("❌ Get user badges error:", error);
      throw error;
    }
  }

  async getLockedBadges(userId: string) {
    try {
      const badgeRepo = AppDataSource.getRepository("Badge");
      const userBadgeRepo = AppDataSource.getRepository("UserBadge");

      const unlockedBadges = await userBadgeRepo
        .createQueryBuilder("ub")
        .select("ub.badge_id")
        .where("ub.user_id = :userId", { userId })
        .getMany();

      const unlockedIds = unlockedBadges.map((ub) => ub.badge_id);

      let query = badgeRepo.createQueryBuilder("badge");

      if (unlockedIds.length > 0) {
        query = query.where("badge.id NOT IN (:...ids)", { ids: unlockedIds });
      }

      const lockedBadges = await query.getMany();
      return lockedBadges;
    } catch (error) {
      console.error("❌ Get locked badges error:", error);
      throw error;
    }
  }

  async unlockBadge(userId: string, badgeId: string) {
    try {
      const userBadgeRepo = AppDataSource.getRepository("UserBadge");

      // Check if already unlocked
      const existing = await userBadgeRepo.findOne({
        where: { user_id: userId, badge_id: badgeId },
      });

      if (existing) return existing;

      const userBadge = await userBadgeRepo.save({
        user_id: userId,
        badge_id: badgeId,
      });

      return userBadge;
    } catch (error) {
      console.error("❌ Unlock badge error:", error);
      throw error;
    }
  }
}

export default new GamificationService();
