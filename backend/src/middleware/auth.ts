import { Context } from "elysia";
import { extractToken, verifyToken, JWTPayload } from "@utils/jwt";
import { errorResponse } from "@utils/errors";

export interface AuthContext extends Context {
  user?: JWTPayload;
}

export const authMiddleware = (ctx: AuthContext) => {
  const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
  
  if (!token) {
    return errorResponse(401, "Missing authorization token", "MISSING_TOKEN");
  }

  const payload = verifyToken(token);
  if (!payload) {
    return errorResponse(401, "Invalid or expired token", "INVALID_TOKEN");
  }

  ctx.user = payload;
};

export const optionalAuthMiddleware = (ctx: AuthContext) => {
  const token = extractToken(ctx.request.headers.get("authorization") ?? undefined);
  
  if (token) {
    const payload = verifyToken(token);
    if (payload) {
      ctx.user = payload;
    }
  }
};
