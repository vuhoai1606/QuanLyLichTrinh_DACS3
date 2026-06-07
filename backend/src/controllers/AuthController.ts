import authService from "@services/AuthService";
import { OAuth2Client } from "google-auth-library";
import otpService from "@services/OTPService";
import { successResponse, errorResponse, AppError } from "@utils/errors";
import { AuthContext } from "@middleware/auth";
import { validateRegister, validateLogin, validateChangePassword, validateUpdateProfile, RegisterDTO, LoginDTO } from "@dtos/auth.dto";
import { logger } from "@utils/logger";
import { APP_CONSTANTS } from "@constants/app.constants";


const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID || "731620527212-10kqcai1ib22t3be0rimj085poa4h7ra.apps.googleusercontent.com");

export class AuthController {
  
  async forgotPassword(body: any): Promise<Response> {
    try {
      const { email } = body;
      if (!email) return errorResponse(400, "Missing email");
      
      await otpService.requestOTP(email, "FORGOT_PASSWORD");
      return successResponse(null, "OTP sent successfully");
    } catch (error) {
      logger.error("Forgot Password error", error);
      return errorResponse(500, "Internal server error");
    }
  }

  async requestOTP(body: any): Promise<Response> {
    try {
      const { email, purpose } = body;
      if (!email || !purpose) return errorResponse(400, "Missing email or purpose");
      
      await otpService.requestOTP(email, purpose);
      return successResponse(null, "OTP sent successfully");
    } catch (error) {
      logger.error("Request OTP error", error);
      return errorResponse(500, "Internal server error");
    }
  }

  async verifyOTP(body: any): Promise<Response> {
    try {
      const { email, otp, purpose = "FORGOT_PASSWORD" } = body;
      if (!email || !otp) return errorResponse(400, "Missing email or otp");
      
      const isValid = await otpService.verifyOTP(email, otp, purpose, false);
      if (!isValid) return errorResponse(400, "Invalid or expired OTP");
      
      return successResponse({ verified: true }, "OTP verified successfully");
    } catch (error) {
      logger.error("Verify OTP error", error);
      return errorResponse(500, "Internal server error");
    }
  }

  async resetPassword(body: any): Promise<Response> {
    try {
      const { email, otp, newPassword } = body;
      if (!email || !otp || !newPassword) return errorResponse(400, "Missing email, otp or newPassword");
      
      const isValid = await otpService.verifyOTP(email, otp, "FORGOT_PASSWORD");
      if (!isValid) return errorResponse(400, "Invalid or expired OTP");
      
      await authService.changePasswordWithEmail(email, newPassword);
      return successResponse(null, "Password reset successfully");
    } catch (error) {
      logger.error("Reset Password error", error);
      return errorResponse(500, "Internal server error");
    }
  }

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

      const result = await authService.register(body.email, body.password, body.full_name, body.gender, body.dob);
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
      const { idToken } = body;
      
      if (!idToken) {
        return errorResponse(400, "Missing idToken", "INVALID_INPUT");
      }

      // Verify token
      const ticket = await client.verifyIdToken({
        idToken: idToken,
        audience: process.env.GOOGLE_CLIENT_ID || "731620527212-10kqcai1ib22t3be0rimj085poa4h7ra.apps.googleusercontent.com",
      });
      
      const payload = ticket.getPayload();
      if (!payload) {
        return errorResponse(401, "Invalid Google token", "UNAUTHORIZED");
      }

      const googleId = payload.sub;
      const email = payload.email || "";
      const fullName = payload.name || "Google User";
      const avatarUrl = payload.picture;

      const result = await authService.googleLogin(googleId, email, fullName, avatarUrl);
      
      return successResponse(
        {
          user: result.user,
          token: result.token,
        },
        "Login with Google successful",
        APP_CONSTANTS.HTTP.OK
      );
    } catch (error) {
      logger.error("Google Login error", error instanceof Error ? error : new Error(String(error)));
      return errorResponse(500, "Internal server error", "INTERNAL_ERROR");
    }
  }
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
