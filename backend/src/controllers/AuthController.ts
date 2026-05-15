import authService from "@services/AuthService";
import { successResponse, errorResponse, AppError } from "@utils/errors";
import { AuthContext } from "@middleware/auth";
import { validateRegister, validateLogin, validateChangePassword, validateUpdateProfile, RegisterDTO, LoginDTO } from "@dtos/auth.dto";
import { logger } from "@utils/logger";
import { APP_CONSTANTS } from "@constants/app.constants";

export class AuthController {
  /**
   * Register new user
   * POST /api/auth/register
   */
  async register(body: any): Promise<Response> {
    try {
      logger.debug("Register request received", { email: body.email });

      // Validate input
      const validation = validateRegister(body);
      if (!validation.valid) {
        logger.warn("Register validation failed", { errors: validation.errors });
        return errorResponse(
          APP_CONSTANTS.HTTP.BAD_REQUEST,
          validation.errors.join(", "),
          APP_CONSTANTS.ERROR_CODES.VALIDATION_ERROR
        );
      }

      const result = await authService.register(body.email, body.password, body.full_name);
      logger.info("User registered successfully", { userId: result.user.id, email: body.email });

      return successResponse(
        {
          user: result.user,
          token: result.token,
        },
        "User registered successfully",
        APP_CONSTANTS.HTTP.CREATED
      );
    } catch (error) {
      logger.error("Register error", error instanceof Error ? error : new Error(String(error)));
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(
        APP_CONSTANTS.HTTP.INTERNAL_ERROR,
        "Internal server error",
        APP_CONSTANTS.ERROR_CODES.INTERNAL_ERROR
      );
    }
  }

  /**
   * Login user
   * POST /api/auth/login
   */
  async login(body: any): Promise<Response> {
    try {
      logger.debug("Login request received", { email: body.email });

      // Validate input
      const validation = validateLogin(body);
      if (!validation.valid) {
        logger.warn("Login validation failed", { errors: validation.errors });
        return errorResponse(
          APP_CONSTANTS.HTTP.BAD_REQUEST,
          validation.errors.join(", "),
          APP_CONSTANTS.ERROR_CODES.VALIDATION_ERROR
        );
      }

      const result = await authService.login(body.email, body.password);
      logger.info("User logged in successfully", { userId: result.user.id, email: body.email });

      return successResponse(
        {
          user: result.user,
          token: result.token,
        },
        "Login successful"
      );
    } catch (error) {
      logger.error("Login error", error instanceof Error ? error : new Error(String(error)));
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(
        APP_CONSTANTS.HTTP.INTERNAL_ERROR,
        "Internal server error",
        APP_CONSTANTS.ERROR_CODES.INTERNAL_ERROR
      );
    }
  }

  /**
   * Google Login
   * POST /api/auth/google-login
   */
  async googleLogin(body: any): Promise<Response> {
    try {
      const { googleId, email, fullName, avatarUrl } = body;

      if (!googleId || !email) {
        return errorResponse(400, "Google ID and email are required", "MISSING_FIELDS");
      }

      const result = await authService.googleLogin(googleId, email, fullName, avatarUrl);
      logger.info("User logged in via Google", { userId: result.user.id, email: email });

      return successResponse(
        {
          user: result.user,
          token: result.token,
        },
        "Google login successful"
      );
    } catch (error) {
      logger.error("Google login error", error instanceof Error ? error : new Error(String(error)));
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
    const { email, otp, newPassword } = body;

    if (!email || !otp || !newPassword) {
      return errorResponse(400, "Email, OTP and new password required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.resetPassword(email, otp, newPassword);
      return successResponse(result, "Password reset successfully");
    } catch (error) {
      if (error instanceof AppError) {
        return errorResponse(error.status, error.message, error.code);
      }
      return errorResponse(500, "Internal server error");
    }
  }

  // Verify OTP
  async verifyOtp(body: any) {
    const { email, otp } = body;

    if (!email || !otp) {
      return errorResponse(400, "Email and OTP required", "MISSING_FIELDS");
    }

    try {
      const result = await authService.verifyOtp(email, otp);
      return successResponse(result, "OTP verified successfully");
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
