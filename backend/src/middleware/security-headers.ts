/**
 * Security Headers Middleware
 * Implements security best practices like HSTS, CSP, X-Frame-Options, etc.
 * Elysia-compatible version
 */

export interface SecurityHeadersOptions {
  enableHSTS?: boolean;
  enableCSP?: boolean;
  enableXFrame?: boolean;
  enableXContentType?: boolean;
  enableXXSS?: boolean;
  enableReferrerPolicy?: boolean;
  enablePermissions?: boolean;
}

const DEFAULT_OPTIONS: SecurityHeadersOptions = {
  enableHSTS: true,
  enableCSP: true,
  enableXFrame: true,
  enableXContentType: true,
  enableXXSS: true,
  enableReferrerPolicy: true,
  enablePermissions: true,
};

/**
 * Apply security headers to response - Elysia plugin style
 */
export const securityHeaders = (options: SecurityHeadersOptions = DEFAULT_OPTIONS) => {
  return (ctx: any) => {
    try {
      if (!ctx.set) ctx.set = {};
      if (!ctx.set.headers) ctx.set.headers = {};

      // HSTS - Force HTTPS for 1 year
      if (options.enableHSTS) {
        ctx.set.headers["strict-transport-security"] = "max-age=31536000; includeSubDomains; preload";
      }

      // CSP - Content Security Policy
      if (options.enableCSP) {
        ctx.set.headers["content-security-policy"] =
          "default-src 'self'; " +
          "script-src 'self' 'unsafe-inline'; " +
          "style-src 'self' 'unsafe-inline'; " +
          "img-src 'self' data: https:; " +
          "font-src 'self' data:; " +
          "connect-src 'self' https:; " +
          "frame-ancestors 'none'; " +
          "base-uri 'self'; " +
          "form-action 'self'";
      }

      // X-Frame-Options - Prevent clickjacking
      if (options.enableXFrame) {
        ctx.set.headers["x-frame-options"] = "DENY";
      }

      // X-Content-Type-Options - Prevent MIME sniffing
      if (options.enableXContentType) {
        ctx.set.headers["x-content-type-options"] = "nosniff";
      }

      // X-XSS-Protection - Enable browser XSS protection
      if (options.enableXXSS) {
        ctx.set.headers["x-xss-protection"] = "1; mode=block";
      }

      // Referrer-Policy - Control referrer information
      if (options.enableReferrerPolicy) {
        ctx.set.headers["referrer-policy"] = "strict-origin-when-cross-origin";
      }

      // Permissions Policy - Control browser features
      if (options.enablePermissions) {
        ctx.set.headers["permissions-policy"] =
          "camera=(), microphone=(), geolocation=(), payment=(), usb=(), magnetometer=(), gyroscope=(), accelerometer=()";
      }
    } catch (error) {
      // Silently fail if headers can't be set - route still processes
      console.warn("Security headers middleware error:", error);
    }
  };
};

/**
 * Security headers configuration object
 */
export const SECURITY_HEADERS = {
  // Strict-Transport-Security - Enforce HTTPS
  HSTS: {
    "Strict-Transport-Security": "max-age=31536000; includeSubDomains; preload",
  },

  // Content-Security-Policy - XSS prevention
  CSP: {
    "Content-Security-Policy":
      "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self'",
  },

  // X-Frame-Options - Clickjacking prevention
  XFrame: {
    "X-Frame-Options": "DENY",
  },

  // X-Content-Type-Options - MIME sniffing prevention
  XContentType: {
    "X-Content-Type-Options": "nosniff",
  },

  // X-XSS-Protection - XSS protection
  XSS: {
    "X-XSS-Protection": "1; mode=block",
  },

  // Referrer-Policy - Referrer information control
  Referrer: {
    "Referrer-Policy": "strict-origin-when-cross-origin",
  },

  // Permissions-Policy - Browser feature control
  Permissions: {
    "Permissions-Policy":
      "camera=(), microphone=(), geolocation=(), payment=(), usb=()",
  },
};

/**
 * Get all security headers
 */
export const getAllSecurityHeaders = (): Record<string, string> => {
  return {
    ...SECURITY_HEADERS.HSTS,
    ...SECURITY_HEADERS.CSP,
    ...SECURITY_HEADERS.XFrame,
    ...SECURITY_HEADERS.XContentType,
    ...SECURITY_HEADERS.XSS,
    ...SECURITY_HEADERS.Referrer,
    ...SECURITY_HEADERS.Permissions,
  };
};
