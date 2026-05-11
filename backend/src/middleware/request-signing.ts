import { createHmac, randomBytes } from "crypto";
import { logger } from "@utils/logger";
import { APP_CONSTANTS } from "@constants/app.constants";
import { errorResponse } from "@utils/errors";

/**
 * Request signing for data integrity and authenticity
 * Implements HMAC-SHA256 signing
 */

export interface SignedRequest {
  timestamp: number;
  nonce: string;
  signature: string;
}

/**
 * Generate HMAC signature
 */
export const generateSignature = (
  method: string,
  path: string,
  body: string,
  timestamp: number,
  nonce: string,
  secret: string
): string => {
  const payload = `${method.toUpperCase()}\n${path}\n${body}\n${timestamp}\n${nonce}`;
  return createHmac("sha256", secret).update(payload).digest("hex");
};

/**
 * Generate request nonce (one-time use value)
 */
export const generateNonce = (): string => {
  return randomBytes(16).toString("hex");
};

/**
 * Request signing utility
 */
export class RequestSigner {
  /**
   * Sign request payload
   */
  static sign(
    method: string,
    path: string,
    body: any,
    secret: string
  ): SignedRequest {
    const timestamp = Math.floor(Date.now() / 1000);
    const nonce = generateNonce();
    const bodyStr = typeof body === "string" ? body : JSON.stringify(body);

    const signature = generateSignature(
      method,
      path,
      bodyStr,
      timestamp,
      nonce,
      secret
    );

    return {
      timestamp,
      nonce,
      signature,
    };
  }

  /**
   * Verify signature
   */
  static verify(
    method: string,
    path: string,
    body: any,
    timestamp: number,
    nonce: string,
    signature: string,
    secret: string,
    maxAge: number = 300 // 5 minutes default
  ): { valid: boolean; error?: string } {
    // Check timestamp freshness
    const now = Math.floor(Date.now() / 1000);
    if (Math.abs(now - timestamp) > maxAge) {
      return { valid: false, error: "Request timestamp is too old" };
    }

    // Verify signature
    const expectedSignature = generateSignature(
      method,
      path,
      typeof body === "string" ? body : JSON.stringify(body),
      timestamp,
      nonce,
      secret
    );

    if (signature !== expectedSignature) {
      return { valid: false, error: "Invalid signature" };
    }

    return { valid: true };
  }
}

/**
 * Nonce store - prevent replay attacks
 */
class NonceStore {
  private nonces = new Map<string, number>();
  private cleanupInterval: NodeJS.Timer | null = null;

  constructor(cleanupIntervalMs: number = 60000) {
    // Auto cleanup old nonces every minute
    this.cleanupInterval = setInterval(() => {
      this.cleanup();
    }, cleanupIntervalMs);
  }

  /**
   * Check if nonce was already used
   */
  isUsed(nonce: string): boolean {
    return this.nonces.has(nonce);
  }

  /**
   * Mark nonce as used
   */
  markUsed(nonce: string, expiresInMs: number = 3600000): void {
    this.nonces.set(nonce, Date.now() + expiresInMs);
  }

  /**
   * Cleanup expired nonces
   */
  private cleanup(): void {
    const now = Date.now();
    let removed = 0;

    this.nonces.forEach((expiry, nonce) => {
      if (now > expiry) {
        this.nonces.delete(nonce);
        removed++;
      }
    });

    if (removed > 0) {
      logger.debug(`Cleaned up ${removed} expired nonces`);
    }
  }

  /**
   * Get store size
   */
  getSize(): number {
    return this.nonces.size;
  }

  /**
   * Clear all nonces
   */
  clear(): void {
    this.nonces.clear();
    logger.debug("Nonce store cleared");
  }

  /**
   * Stop cleanup
   */
  stop(): void {
    if (this.cleanupInterval) {
      clearInterval(this.cleanupInterval);
    }
  }
}

export const nonceStore = new NonceStore();

/**
 * Request signature middleware
 * Validates signed requests for security
 */
export const requestSignatureMiddleware = (secret: string, maxAge?: number) => {
  return (ctx: any): Response | null => {
    // Only validate POST, PUT, PATCH requests
    const method = ctx.request.method;
    if (!["POST", "PUT", "PATCH"].includes(method)) {
      return null;
    }

    // Get signature headers
    const signature = ctx.request.headers.get("x-signature");
    const timestamp = ctx.request.headers.get("x-timestamp");
    const nonce = ctx.request.headers.get("x-nonce");

    if (!signature || !timestamp || !nonce) {
      logger.warn("Missing signature headers", { method, path: ctx.request.url });
      return errorResponse(
        APP_CONSTANTS.HTTP.BAD_REQUEST,
        "Missing signature headers (x-signature, x-timestamp, x-nonce)",
        APP_CONSTANTS.ERROR_CODES.VALIDATION_ERROR
      );
    }

    // Check if nonce was used (replay attack prevention)
    if (nonceStore.isUsed(nonce)) {
      logger.warn("Replay attack detected - nonce already used", { nonce });
      return errorResponse(
        APP_CONSTANTS.HTTP.BAD_REQUEST,
        "Nonce already used",
        APP_CONSTANTS.ERROR_CODES.VALIDATION_ERROR
      );
    }

    // Verify signature
    const path = new URL(ctx.request.url).pathname;
    const verification = RequestSigner.verify(
      method,
      path,
      ctx.body || "",
      parseInt(timestamp),
      nonce,
      signature,
      secret,
      maxAge
    );

    if (!verification.valid) {
      logger.warn("Invalid signature", { error: verification.error, method, path });
      return errorResponse(
        APP_CONSTANTS.HTTP.UNAUTHORIZED,
        verification.error || "Invalid signature",
        APP_CONSTANTS.ERROR_CODES.INVALID_TOKEN
      );
    }

    // Mark nonce as used
    nonceStore.markUsed(nonce);

    logger.debug("Request signature verified", { method, path });
    return null; // null means validation passed
  };
};

/**
 * Helper to generate signed request headers for client
 */
export const getSignedHeaders = (
  method: string,
  path: string,
  body: any,
  secret: string
): Record<string, string> => {
  const signed = RequestSigner.sign(method, path, body, secret);

  return {
    "x-timestamp": String(signed.timestamp),
    "x-nonce": signed.nonce,
    "x-signature": signed.signature,
  };
};
