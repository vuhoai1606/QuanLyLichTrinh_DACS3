import userService from "@services/UserService";
import { successResponse, errorResponse } from "@utils/errors";

export class UserController {
  // Search users
  async searchUsers(query: any) {
    const { q, user_id, limit = 10 } = query;

    if (!q || !user_id) {
      return errorResponse(400, "q (search query) and user_id required", "MISSING_FIELDS");
    }

    try {
      const users = await userService.searchUsers(q, user_id, parseInt(limit));
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "Users found",
          data: users,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get public profile
  async getPublicProfile(userId: string) {
    try {
      const profile = await userService.getUserPublicProfile(userId);

      if (!profile) {
        return errorResponse(404, "User not found", "USER_NOT_FOUND");
      }

      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: profile,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get user stats
  async getUserStats(userId: string) {
    try {
      const stats = await userService.getUserStats(userId);

      if (!stats) {
        return errorResponse(404, "User not found", "USER_NOT_FOUND");
      }

      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: stats,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Follow user
  async followUser(userId: string, query: any) {
    const { follower_id } = query;

    if (!follower_id) {
      return errorResponse(400, "follower_id query param required", "MISSING_FIELDS");
    }

    try {
      const result = await userService.followUser(follower_id, userId);
      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "Followed successfully",
          data: result,
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Unfollow user
  async unfollowUser(userId: string, query: any) {
    const { follower_id } = query;

    if (!follower_id) {
      return errorResponse(400, "follower_id query param required", "MISSING_FIELDS");
    }

    try {
      const result = await userService.unfollowUser(follower_id, userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          message: "Unfollowed successfully",
          data: result,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get followers
  async getFollowers(userId: string) {
    try {
      const followers = await userService.getFollowers(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: followers,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Get following
  async getFollowing(userId: string) {
    try {
      const following = await userService.getFollowing(userId);
      return new Response(
        JSON.stringify({
          status: 200,
          success: true,
          data: following,
        }),
        { status: 200, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}

export default new UserController();
