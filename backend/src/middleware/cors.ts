import { Context } from "elysia";

export const corsMiddleware = (ctx: Context) => {
  const origin = ctx.request.headers.get("origin");
  const allowedOrigins = [
    "http://localhost:3001",
    "http://localhost:3000",
    process.env.CORS_ORIGIN || "",
  ].filter(Boolean);

  if (!origin || allowedOrigins.includes(origin)) {
    ctx.set.headers = {
      "Access-Control-Allow-Origin": origin || "*",
      "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS, PATCH",
      "Access-Control-Allow-Headers": "Content-Type, Authorization",
      "Access-Control-Allow-Credentials": "true",
    };
  }

  if (ctx.request.method === "OPTIONS") {
    return new Response(null, { status: 204 });
  }
};
