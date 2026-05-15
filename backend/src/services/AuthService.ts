import { AppDataSource } from "@config/database";
import { config } from "@config/env";
import { hashPassword, comparePassword, validatePassword } from "@utils/password";
import { generateUUID, isValidEmail } from "@utils/validation";
import { AppError } from "@utils/errors";
import jwt from "jsonwebtoken";

export class AuthService {
  async register(email: string, password: string, full_name: string): Promise<any> {
    console.log("🔐 Service: Register started for", email);
    const userRepository = AppDataSource.getRepository("User");
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    if (!isValidEmail(email)) {
      console.log("❌ Invalid email format:", email);
      throw new AppError(400, "Invalid email format", "INVALID_EMAIL");
    }

    const passwordValidation = validatePassword(password);
    console.log("🔑 Password validation:", passwordValidation);
    if (!passwordValidation.valid) {
      throw new AppError(400, passwordValidation.message || "Invalid password", "INVALID_PASSWORD");
    }

    const existingUser = await userRepository.findOne({ where: { email: email.toLowerCase() } });
    if (existingUser) {
      console.log("❌ Email already exists");
      throw new AppError(409, "Email already exists", "EMAIL_EXISTS");
    }

    console.log("✅ Creating new user...");
    const user = userRepository.create({
      id: generateUUID(),
      email: email.toLowerCase(),
      password_hash: await hashPassword(password),
      full_name,
    });

    const savedUser = await userRepository.save(user);
    console.log("✅ User saved:", savedUser.id);

    const settings = userSettingsRepository.create({
      user_id: savedUser.id,
      language: "en",
      theme: "SYSTEM",
      default_focus_minutes: 25,
      notifications_enabled: true,
    });

    await userSettingsRepository.save(settings);
    console.log("✅ User settings saved");
    
    const token = await this.generateToken(savedUser.id);
    console.log("✅ Token generated");

    return { user: savedUser, token };
  }

  async login(email: string, password: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({ where: { email: email.toLowerCase() } });

    if (!user) {
      throw new AppError(401, "Invalid credentials", "INVALID_CREDENTIALS");
    }

    if (!user.password_hash) {
      throw new AppError(400, "This account uses Google login. Please use Continue with Google.", "USE_SOCIAL_LOGIN");
    }

    const validPassword = await comparePassword(password, user.password_hash);
    if (!validPassword) {
      throw new AppError(401, "Invalid credentials", "INVALID_CREDENTIALS");
    }

    const token = await this.generateToken(user.id);
    return { user, token };
  }

  async googleLogin(googleId: string, email: string, fullName: string, avatarUrl?: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const userSettingsRepository = AppDataSource.getRepository("UserSettings");

    let user = await userRepository.findOne({ 
      where: [{ google_id: googleId }, { email: email.toLowerCase() }] 
    }) as any;

    if (!user) {
      // Create new user for first-time Google sign-in
      user = userRepository.create({
        id: generateUUID(),
        email: email.toLowerCase(),
        google_id: googleId,
        full_name: fullName,
        avatar_url: avatarUrl,
        is_active: true
      });
      user = await userRepository.save(user);

      // Initialize settings
      const settings = userSettingsRepository.create({
        user_id: user.id,
        language: "en",
        theme: "SYSTEM",
        default_focus_minutes: 25,
        notifications_enabled: true,
      });
      await userSettingsRepository.save(settings);
    } else {
      // Link Google ID if not already linked
      if (!user.google_id) {
        user.google_id = googleId;
        if (avatarUrl && !user.avatar_url) user.avatar_url = avatarUrl;
        await userRepository.save(user);
      }
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
      // For security, don't reveal user existence
      return { success: true, message: "If email exists, an OTP will be sent" };
    }

    // Generate 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();

    user.password_reset_token = otp; // Re-using token field for OTP
    user.password_reset_expires = new Date(Date.now() + 600000); // 10 minutes expiry
    await userRepository.save(user);

    console.log(`📨 OTP for ${email}: ${otp}`); // In real app, send via email

    return {
      success: true,
      message: "OTP sent to your email",
      otp, // Sending OTP in response for demo purposes (In production, only via email)
    };
  }

  async verifyOtp(email: string, otp: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({
      where: { email: email.toLowerCase(), password_reset_token: otp },
    });

    if (!user || !user.password_reset_expires || user.password_reset_expires < new Date()) {
      throw new AppError(401, "Invalid or expired OTP", "INVALID_OTP");
    }

    return { success: true, message: "OTP verified successfully" };
  }

  async resetPassword(email: string, otp: string, newPassword: string): Promise<any> {
    const userRepository = AppDataSource.getRepository("User");
    const user = await userRepository.findOne({
      where: { email: email.toLowerCase(), password_reset_token: otp },
    });

    if (!user || !user.password_reset_expires || user.password_reset_expires < new Date()) {
      throw new AppError(401, "Invalid or expired OTP", "INVALID_OTP");
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
    try {
      const decoded = jwt.verify(token, config.jwt.secret, { ignoreExpiration: true }) as any;
      
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
    try {
      const decoded = jwt.verify(token, config.jwt.secret) as any;
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
    return jwt.sign({ userId }, config.jwt.secret as any, {
      expiresIn: config.jwt.expiresIn as any,
    } as any);
  }
}

export default new AuthService();
