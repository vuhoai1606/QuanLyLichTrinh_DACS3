import authService from "@services/AuthService";
import { successResponse, errorResponse, AppError } from "@utils/errors";
import { AuthContext } from "@middleware/auth";

export class AuthController {
  // Register new user
  async register(body: any) {
    const { email, password, full_name } = body;

    if (!email || !password || !full_name) {
      return errorResponse(400, "Email, password, and full name required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.register(email, password, full_name);

      return new Response(
        JSON.stringify({
          status: 201,
          success: true,
          message: "User registered successfully",
          data: {
            user: result.user,
            token: result.token,
          },
        }),
        { status: 201, headers: { "Content-Type": "application/json" } }
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }

  // Login user
  async login(body: any) {
    const { email, password } = body;

    if (!email || !password) {
      return errorResponse(400, "Email and password required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.login(email, password);

      return successResponse(
        {
          user: result.user,
          token: result.token,
        },
        "Login successful"
      );
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Get current user profile
  async getProfile(ctx: AuthContext) {
    if (!ctx.user) {
      return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    }

    try {
      const user = await authService.getUserById(ctx.user.userId);
      return successResponse(user, "User retrieved successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Update profile
  async updateProfile(ctx: AuthContext, body: any) {
    if (!ctx.user) {
      return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    }

    try {
      const user = await authService.updateProfile(ctx.user.userId, body);
      return successResponse(user, "Profile updated successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Change password
  async changePassword(ctx: AuthContext, body: any) {
    if (!ctx.user) {
      return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
    }

    const { oldPassword, newPassword } = body;

    if (!oldPassword || !newPassword) {
      return errorResponse(400, "Old password and new password required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.changePassword(ctx.user.userId, oldPassword, newPassword);
      return successResponse(result, "Password changed successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Forgot password
  async forgotPassword(body: any) {
    const { email } = body;

    if (!email) {
      return errorResponse(400, "Email is required", "MISSING_EMAIL");
    }

    try {
      const result = await authService.forgotPassword(email);
      return successResponse(result, "Password reset instructions sent");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Reset password
  async resetPassword(body: any) {
    const { resetToken, newPassword } = body;

    if (!resetToken || !newPassword) {
      return errorResponse(400, "Reset token and new password required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.resetPassword(resetToken, newPassword);
      return successResponse(result, "Password reset successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Refresh token
  async refreshToken(body: any) {
    const { token } = body;

    if (!token) {
      return errorResponse(400, "Token is required", "MISSING_TOKEN");
    }

    try {
      const result = await authService.refreshToken(token);
      return successResponse(result, "Token refreshed successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Verify token
  async verifyToken(body: any) {
    const { token } = body;

    if (!token) {
      return errorResponse(400, "Token is required", "MISSING_TOKEN");
    }

    try {
      const result = await authService.verifyToken(token);
      return successResponse(result, "Token verification completed");
    } catch (error) {
      return errorResponse(500, "Internal server error");
    }
  }
}

export default new AuthController();
