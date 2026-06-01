import { AppDataSource } from "@config/database";

class UserService {
  async searchUsers(query: string, currentUserId: string, limit: number = 10) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const users = await userRepo
        .createQueryBuilder("user")
        .where("(user.email ILIKE :query OR user.full_name ILIKE :query)", {
          query: `%${query}%`,
        })
        .andWhere("user.id != :currentUserId", { currentUserId })
        .select([
          "user.id",
          "user.email",
          "user.full_name",
          "user.avatar_url",
          "user.total_exp",
          "user.current_rank",
        ])
        .take(limit)
        .getMany();

      return users;
    } catch (error) {
      console.error("❌ Search users error:", error);
      throw error;
    }
  }

  async getUserById(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const user = await userRepo.findOne({
        where: { id: userId },
        select: [
          "id",
          "email",
          "full_name",
          "avatar_url",
          "bio",
          "total_exp",
          "current_rank",
          "created_at",
        ],
      });

      return user || null;
    } catch (error) {
      console.error("❌ Get user by ID error:", error);
      throw error;
    }
  }

  async getUserPublicProfile(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const user = await userRepo.findOne({
        where: { id: userId },
        select: [
          "id",
          "email",
          "full_name",
          "avatar_url",
          "bio",
          "total_exp",
          "current_rank",
          "is_active",
          "gender",
          "dob",
          "timezone",
        ],
      });

      if (!user) return null;

      // Get user badges
      const badgeRepo = AppDataSource.getRepository("UserBadge");
      const badges = await badgeRepo
        .createQueryBuilder("ub")
        .leftJoinAndSelect("ub.badge", "badge")
        .where("ub.user_id = :userId", { userId })
        .select(["badge.id", "badge.name", "badge.icon_url", "ub.unlocked_at"])
        .getMany();

      return {
        ...user,
        badges: badges.map((ub) => ub.badge),
      };
    } catch (error) {
      console.error("❌ Get public profile error:", error);
      throw error;
    }
  }

  async getUserStats(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      const scheduleRepo = AppDataSource.getRepository("Schedule");
      const sessionRepo = AppDataSource.getRepository("FocusSession");

      const user = await userRepo.findOne({ where: { id: userId } });
      if (!user) return null;

      const totalSchedules = await scheduleRepo.count({
        where: { creator_id: userId },
      });

      const completedSchedules = await scheduleRepo.count({
        where: { creator_id: userId, status: "DONE" },
      });

      const totalSessions = await sessionRepo.count({
        where: { user_id: userId },
      });

      const totalFocusMinutes = await sessionRepo
        .createQueryBuilder("fs")
        .select("SUM(fs.duration_minutes)", "total")
        .where("fs.user_id = :userId", { userId })
        .getRawOne();

      return {
        userId,
        totalSchedules,
        completedSchedules,
        completionRate: totalSchedules > 0 ? ((completedSchedules / totalSchedules) * 100).toFixed(2) : 0,
        totalSessions,
        totalFocusMinutes: parseInt(totalFocusMinutes?.total) || 0,
        totalExp: user.total_exp,
        currentRank: user.current_rank,
        accountCreatedAt: user.created_at,
      };
    } catch (error) {
      console.error("❌ Get user stats error:", error);
      throw error;
    }
  }

  async followUser(followerId: string, followeeId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const follower = await userRepo.findOne({ where: { id: followerId } });
      const followee = await userRepo.findOne({ where: { id: followeeId } });

      if (!follower || !followee) {
        throw new Error("User not found");
      }

      if (!follower.following) {
        follower.following = [];
      }

      if (!follower.following.includes(followeeId)) {
        follower.following.push(followeeId);
        await userRepo.save(follower);
      }

      return { success: true, message: "Followed successfully" };
    } catch (error) {
      console.error("❌ Follow user error:", error);
      throw error;
    }
  }

  async unfollowUser(followerId: string, followeeId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const follower = await userRepo.findOne({ where: { id: followerId } });
      if (!follower) throw new Error("Follower not found");

      if (follower.following && Array.isArray(follower.following)) {
        follower.following = follower.following.filter(id => id !== followeeId);
        await userRepo.save(follower);
      }

      return { success: true, message: "Unfollowed successfully" };
    } catch (error) {
      console.error("❌ Unfollow user error:", error);
      throw error;
    }
  }

  async getFollowers(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      // Get all users who follow this user
      const followers = await userRepo
        .createQueryBuilder("user")
        .where(":userId = ANY(user.following)", { userId })
        .select(["user.id", "user.full_name", "user.avatar_url", "user.current_rank"])
        .getMany();

      return followers;
    } catch (error) {
      console.error("❌ Get followers error:", error);
      throw error;
    }
  }

  async getFollowing(userId: string) {
    try {
      const userRepo = AppDataSource.getRepository("User");

      const user = await userRepo.findOne({
        where: { id: userId },
        select: ["id", "following"],
      });

      if (!user || !user.following) return [];

      const following = await userRepo
        .createQueryBuilder("user")
        .where("user.id = ANY(:ids)", { ids: user.following })
        .select(["user.id", "user.full_name", "user.avatar_url", "user.current_rank"])
        .getMany();

      return following;
    } catch (error) {
      console.error("❌ Get following error:", error);
      throw error;
    }
  }

  async updateProfile(userId: string, data: { full_name?: string; bio?: string; avatar_url?: string; gender?: string; dob?: string }) {
    try {
      const userRepo = AppDataSource.getRepository("User");
      const user = await userRepo.findOne({ where: { id: userId } });

      if (!user) throw new Error("User not found");

      if (data.full_name !== undefined) user.full_name = data.full_name;
      if (data.bio !== undefined) user.bio = data.bio;
      if (data.avatar_url !== undefined) user.avatar_url = data.avatar_url;
      if (data.gender !== undefined) user.gender = data.gender;
      if (data.dob !== undefined) user.dob = data.dob;

      const savedUser = await userRepo.save(user);
      
      // Return updated profile without sensitive data
      const { password_hash, password_reset_token, password_reset_expires, ...profile } = savedUser as any;
      return profile;
    } catch (error) {
      console.error("❌ Update profile error:", error);
      throw error;
    }
  }
}

export default new UserService();
