import gamificationService from "@services/GamificationService";
import { successResponse, errorResponse } from "@utils/errors";

export class GamificationController {
  // Get ranks
  async getRanks() {
    try {
      const ranks = await gamificationService.getRanks();
      return successResponse(ranks, "Ranks retrieved");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }

  // Get leaderboard
  async getLeaderboard(query: any) {
    const { user_id, type = "global", limit = 20 } = query;

    try {
      const leaderboard = await gamificationService.getLeaderboard(
        user_id,
        type,
        parseInt(limit)
      );
      return successResponse(leaderboard, "Leaderboard retrieved");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }

  // Get rank info
  async getRankInfo(userId: string) {
    try {
      const rankInfo = await gamificationService.getUserRankInfo(userId);
      return successResponse(rankInfo, "Rank info retrieved");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }

  // Get badges
  async getBadges(query: any) {
    const { user_id } = query;

    if (!user_id) {
      return errorResponse(400, "user_id required", "MISSING_FIELDS");
    }

    try {
      const unlockedBadges = await gamificationService.getUserBadges(user_id);
      const lockedBadges = await gamificationService.getLockedBadges(user_id);

      return successResponse(
        {
          unlocked: unlockedBadges,
          locked: lockedBadges,
        },
        "Badges retrieved"
      );
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }

  // Get all badges
  async getAllBadges() {
    try {
      const badges = await gamificationService.getAllBadges();
      return successResponse(badges, "All badges retrieved");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }

  // Unlock badge
  async unlockBadge(body: any) {
    const { user_id, badge_id } = body;

    if (!user_id || !badge_id) {
      return errorResponse(400, "user_id and badge_id required", "MISSING_FIELDS");
    }

    try {
      const result = await gamificationService.unlockBadge(user_id, badge_id);
      return successResponse(result, "Badge unlocked");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new GamificationController();
