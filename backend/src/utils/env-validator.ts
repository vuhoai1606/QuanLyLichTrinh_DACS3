import { config } from "@config/env";
import { logger } from "@utils/logger";

/**
 * Validate required environment variables on startup
 */
export function validateEnvironment(): void {
  logger.info("Validating environment configuration...");

  const requiredVars = [
    { name: "PORT", value: config.port },
    { name: "NODE_ENV", value: config.nodeEnv },
    { name: "DATABASE_HOST", value: config.database.host },
    { name: "DATABASE_NAME", value: config.database.name },
    { name: "DATABASE_USER", value: config.database.user },
    { name: "DATABASE_PASSWORD", value: config.database.password },
    { name: "JWT_SECRET", value: config.jwt.secret },
  ];

  const missing: string[] = [];

  for (const envVar of requiredVars) {
    if (!envVar.value) {
      missing.push(envVar.name);
    }
  }

  if (missing.length > 0) {
    logger.error(
      `Missing required environment variables: ${missing.join(", ")}`,
      new Error("Environment validation failed")
    );
    process.exit(1);
  }

  // Validate values
  const validNodeEnvs = ["development", "staging", "production", "test"];
  if (!validNodeEnvs.includes(config.nodeEnv)) {
    logger.error(
      `Invalid NODE_ENV. Must be one of: ${validNodeEnvs.join(", ")}`,
      new Error("Invalid NODE_ENV")
    );
    process.exit(1);
  }

  if (config.port < 1 || config.port > 65535) {
    logger.error(
      `Invalid PORT. Must be between 1 and 65535`,
      new Error("Invalid PORT")
    );
    process.exit(1);
  }

  if (config.jwt.secret.length < 32 && config.nodeEnv === "production") {
    logger.warn("JWT_SECRET is too short for production (< 32 characters)");
  }

  logger.info("✅ Environment validation passed");
  logger.info(`Configuration: NODE_ENV=${config.nodeEnv}, PORT=${config.port}`);
}

/**
 * Print startup configuration
 */
export function printStartupConfig(): void {
  const config_display = {
    "Server Port": config.port,
    "Environment": config.nodeEnv,
    "Database": `${config.database.user}@${config.database.host}:${config.database.port}/${config.database.name}`,
    "JWT Expiry": config.jwt.expiresIn,
    "API Prefix": config.api.prefix,
    "CORS Origin": config.cors.origin,
  };

  logger.info("Server Configuration:", config_display);
}
