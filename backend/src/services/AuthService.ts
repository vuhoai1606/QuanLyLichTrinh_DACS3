import { AppDataSource } from "@config/database";
import { hashPassword, comparePassword, validatePassword } from "@utils/password";
import { generateUUID, isValidEmail } from "@utils/validation";
import { AppError } from "@utils/errors";

export class AuthService {
  async register(email: string, password: string, full_name: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    if (!isValidEmail(email)) {
      throw new AppError(400, "Invalid email format", "INVALID_EMAIL");
    }

    const passwordValidation = validatePassword(password);
    if (!passwordValidation.valid) {
      throw new AppError(400, passwordValidation.message || "Invalid password", "INVALID_PASSWORD");
    }

    const existingUser = await userRepository.findOne({ where: { email: email.toLowerCase() } });
    if (existingUser) {
      throw new AppError(409, "Email already exists", "EMAIL_EXISTS");
    }

    const user = userRepository.create({
      id: generateUUID(),
      email: email.toLowerCase(),
      password_hash: await hashPassword(password),
      full_name,
    });

    const savedUser = await userRepository.save(user);

    const settings = userSettingsRepository.create({
      user_id: savedUser.id,
      language: "en",
      theme: "SYSTEM",
      default_focus_minutes: 25,
      notifications_enabled: true,
    });

    await userSettingsRepository.save(settings);
    const token = await this.generateToken(savedUser.id);

    return { user: savedUser, token };
  }

  async login(email: string, password: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { email: email.toLowerCase() } });

    if (!user) {
      throw new AppError(401, "Invalid credentials", "INVALID_CREDENTIALS");
    }

    const validPassword = await comparePassword(password, user.password_hash);
    if (!validPassword) {
      throw new AppError(401, "Invalid credentials", "INVALID_CREDENTIALS");
    }

    const token = await this.generateToken(user.id);
    return { user, token };
  }

  async getUserById(userId: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { id: userId } });

    if (!user) {
      throw new AppError(404, "User not found", "USER_NOT_FOUND");
    }

    return user;
  }

  async updateProfile(userId: string, data: any): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { id: userId } });

    if (!user) {
      throw new AppError(404, "User not found", "USER_NOT_FOUND");
    }

    if (data.full_name) user.full_name = data.full_name;
    if (data.avatar_url) user.avatar_url = data.avatar_url;

    return await userRepository.save(user);
  }

  async changePassword(userId: string, oldPassword: string, newPassword: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { id: userId } });

    if (!user) {
      throw new AppError(404, "User not found", "USER_NOT_FOUND");
    }

    const validOldPassword = await comparePassword(oldPassword, user.password_hash);
    if (!validOldPassword) {
      throw new AppError(401, "Current password is incorrect", "INVALID_PASSWORD");
    }

    const passwordValidation = validatePassword(newPassword);
    if (!passwordValidation.valid) {
      throw new AppError(400, passwordValidation.message || "Invalid password", "INVALID_PASSWORD");
    }

    user.password_hash = await hashPassword(newPassword);
    await userRepository.save(user);

    return { success: true, message: "Password changed successfully" };
  }

  async forgotPassword(email: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { email: email.toLowerCase() } });

    if (!user) {
      // Don't reveal if email exists for security
      return { success: true, message: "If email exists, reset link will be sent" };
    }

    const resetToken = require("jsonwebtoken").sign(
      { userId: user.id, type: "password_reset" },
      process.env.JWT_SECRET || "secret",
      { expiresIn: "1h" }
    );

    user.password_reset_token = resetToken;
    user.password_reset_expires = new Date(Date.now() + 3600000);
    await userRepository.save(user);

    return {
      success: true,
      message: "Password reset email sent",
      resetToken, // In real app, send via email
    };
  }

  async resetPassword(resetToken: string, newPassword: string): Promise<any> {
    const jwt = require("jsonwebtoken");
    
    try {
      const decoded = jwt.verify(resetToken, process.env.JWT_SECRET || "secret");
      if (decoded.type !== "password_reset") {
        throw new AppError(401, "Invalid reset token", "INVALID_TOKEN");
      }
    } catch (error) {
      throw new AppError(401, "Reset token expired", "TOKEN_EXPIRED");
    }

    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({
      where: { password_reset_token: resetToken },
    });

    if (!user || !user.password_reset_expires || user.password_reset_expires < new Date()) {
      throw new AppError(401, "Reset token expired or invalid", "INVALID_TOKEN");
    }

    const passwordValidation = validatePassword(newPassword);
    if (!passwordValidation.valid) {
      throw new AppError(400, passwordValidation.message || "Invalid password", "INVALID_PASSWORD");
    }

    user.password_hash = await hashPassword(newPassword);
    user.password_reset_token = null;
    user.password_reset_expires = null;
    await userRepository.save(user);

    return { success: true, message: "Password reset successfully" };
  }

  async refreshToken(token: string): Promise<any> {
    const jwt = require("jsonwebtoken");
    
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET || "secret", { ignoreExpiration: true });
      
      if (Date.now() >= decoded.exp * 1000) {
        const userRepository = AppDataSource.getRepository("User");
        const user = await userRepository.findOne({ where: { id: decoded.userId } });

        if (!user || !user.is_active) {
          throw new AppError(401, "User not found or inactive", "INVALID_USER");
        }

        const newToken = await this.generateToken(decoded.userId);
        return { token: newToken, expiresIn: "7d" };
      }

      return { token, message: "Token still valid" };
    } catch (error) {
      throw new AppError(401, "Invalid token", "INVALID_TOKEN");
    }
  }

  async verifyToken(token: string): Promise<any> {
    const jwt = require("jsonwebtoken");
    
    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET || "secret");
      const userRepository = AppDataSource.getRepository("User");
      const user = await userRepository.findOne({ where: { id: decoded.userId } });

      return {
        valid: true,
        userId: decoded.userId,
        expiresAt: new Date(decoded.exp * 1000),
        userExists: !!user,
      };
    } catch (error) {
      return {
        valid: false,
        message: "Token is invalid or expired",
      };
    }
  }

  private async generateToken(userId: string): Promise<string> {
    const jwt = require("jsonwebtoken");
    return jwt.sign({ userId }, process.env.JWT_SECRET || "secret", {
      expiresIn: process.env.JWT_EXPIRES_IN || "7d",
    });
  }
}

export default new AuthService();
