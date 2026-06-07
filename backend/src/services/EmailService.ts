import nodemailer from "nodemailer";
import { logger } from "@utils/logger";

class EmailService {
  private transporter: nodemailer.Transporter | null = null;

  private getTransporter() {
    if (!this.transporter) {
      const host = process.env.SMTP_HOST || "smtp.ethereal.email";
      if (host.includes("gmail.com")) {
        this.transporter = nodemailer.createTransport({
          service: "gmail",
          auth: {
            user: process.env.SMTP_USER,
            pass: process.env.SMTP_PASS,
          },
        });
      } else {
        this.transporter = nodemailer.createTransport({
          host,
          port: Number(process.env.SMTP_PORT) || 587,
          auth: {
            user: process.env.SMTP_USER || "your_ethereal_user",
            pass: process.env.SMTP_PASS || "your_ethereal_pass",
          },
        });
      }
    }
    return this.transporter;
  }

  async sendOTP(email: string, otp: string, purpose: string) {
    const subject = purpose === "REGISTRATION" ? "BFY Registration OTP" : "BFY Password Reset OTP";
    const text = `Your verification code is: ${otp}. This code expires in 5 minutes.`;
    
    try {
      if (process.env.SMTP_HOST && process.env.SMTP_PASS) {
        logger.info(`[EMAIL] Attempting to send real email to ${email} using ${process.env.SMTP_USER}`);
        await this.getTransporter().sendMail({
          from: `"BFY App" <${process.env.SMTP_USER}>`,
          to: email,
          subject,
          text,
        });
        logger.info(`[EMAIL] OTP successfully sent to ${email} via real SMTP`);
      } else {
        logger.info(`[MOCK EMAIL] To: ${email} | Subject: ${subject} | Content: ${text}`);
      }
    } catch (error) {
      logger.error(`[EMAIL ERROR] Failed to send email to ${email}:`, error);
      // Fallback to mock email if real email fails
      logger.info(`[MOCK EMAIL FALLBACK] To: ${email} | Subject: ${subject} | Content: ${text}`);
    }
  }
}

export default new EmailService();
