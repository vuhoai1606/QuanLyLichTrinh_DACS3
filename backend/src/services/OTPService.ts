import { AppDataSource } from "@config/database";
import { OTPCode } from "@models/OTPCode";
import emailService from "@services/EmailService";
import { generateUUID } from "@utils/validation";

class OTPService {
  async requestOTP(email: string, purpose: string) {
    const otpRepository = AppDataSource.getRepository("OTPCode");
    
    // Clean up old OTPs
    await otpRepository.delete({ email, purpose });

    const code = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = new Date(Date.now() + 5 * 60000); // 5 mins

    const otp = otpRepository.create({
      id: generateUUID(),
      email,
      code,
      purpose,
      expires_at: expiresAt,
      created_at: new Date(),
    });

    await otpRepository.save(otp);
    await emailService.sendOTP(email, code, purpose);
    
    return true;
  }

  async verifyOTP(email: string, code: string, purpose: string, consume: boolean = true) {
    const otpRepository = AppDataSource.getRepository("OTPCode");
    
    const otp = await otpRepository.findOne({ where: { email, code, purpose } });
    if (!otp) {
      return false;
    }
    
    if (new Date() > otp.expires_at) {
      await otpRepository.remove(otp);
      return false;
    }

    // OTP valid, remove it if consume is true
    if (consume) {
      await otpRepository.remove(otp);
    }
    return true;
  }
}

export default new OTPService();
