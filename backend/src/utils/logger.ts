import { config } from "@config/env";

export enum LogLevel {
  DEBUG = "DEBUG",
  INFO = "INFO",
  WARN = "WARN",
  ERROR = "ERROR",
}

interface LogContext {
  timestamp: string;
  level: LogLevel;
  message: string;
  data?: any;
  error?: Error;
}

class Logger {
  private isDevelopment = config.nodeEnv === "development";

  private format(context: LogContext): string {
    const { timestamp, level, message, data, error } = context;
    let log = `[${timestamp}] [${level}] ${message}`;
    
    if (data) {
      log += ` | ${JSON.stringify(data)}`;
    }
    
    if (error) {
      log += ` | ${error.message}\n${error.stack}`;
    }
    
    return log;
  }

  private log(level: LogLevel, message: string, data?: any, error?: Error): void {
    const context: LogContext = {
      timestamp: new Date().toISOString(),
      level,
      message,
      data,
      error,
    };

    const formatted = this.format(context);

    switch (level) {
      case LogLevel.DEBUG:
        if (this.isDevelopment) console.log(`🔵 ${formatted}`);
        break;
      case LogLevel.INFO:
        console.log(`🟢 ${formatted}`);
        break;
      case LogLevel.WARN:
        console.warn(`🟡 ${formatted}`);
        break;
      case LogLevel.ERROR:
        console.error(`🔴 ${formatted}`);
        break;
    }

    // Send to external logging service (Sentry, DataDog, etc.) if configured
    if (process.env.EXTERNAL_LOGGING_ENABLED === "true") {
      // Mock external call
    }
  }

  debug(message: string, data?: any): void {
    this.log(LogLevel.DEBUG, message, data);
  }

  info(message: string, data?: any): void {
    this.log(LogLevel.INFO, message, data);
  }

  warn(message: string, data?: any): void {
    this.log(LogLevel.WARN, message, data);
  }

  error(message: string, error?: Error, data?: any): void {
    this.log(LogLevel.ERROR, message, data, error);
  }
}

export const logger = new Logger();
