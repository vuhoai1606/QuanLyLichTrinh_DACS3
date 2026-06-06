import nodemailer from "nodemailer";
import { logger } from "@utils/logger";

class EmailService {
  private transporter: nodemailer.Transporter;

  constructor() {
    this.transporter = nodemailer.createTransport({
      host: process.env.SMTP_HOST || "smtp.ethereal.email",
      port: Number(process.env.SMTP_PORT) || 587,
      auth: {
        user: process.env.SMTP_USER || "your_ethereal_user",
        pass: process.env.SMTP_PASS || "your_ethereal_pass",
      },
    });
  }

  async sendOTP(email: string, otp: string, purpose: string) {
    const subject = purpose === "REGISTRATION" ? "BFY Registration OTP" : "BFY Password Reset OTP";
    const text = `Your verification code is: ${otp}. This code expires in 5 minutes.`;
    
    try {
      if (process.env.SMTP_HOST) {
        await this.transporter.sendMail({
          from: '"BFY App" <noreply@bfy.com>',
          to: email,
          subject,
          text,
        });
        logger.info(`OTP sent to ${email}`);
      } else {
        logger.info(`[MOCK EMAIL] To: ${email} | Subject: ${subject} | Content: ${text}`);
      }
    } catch (error) {
      logger.error(`Error sending email to ${email}`, error);
      throw new Error("Failed to send email");
    }
  }
}

export default new EmailService();
