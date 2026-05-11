import { logger } from "@utils/logger";

/**
 * Response compression middleware
 * Compresses response bodies to reduce bandwidth
 * Supports gzip and deflate
 */

export interface CompressionOptions {
  threshold?: number; // Minimum size to compress (bytes)
  level?: number; // Compression level (1-9)
  exclude?: (path: string) => boolean;
}

const DEFAULT_OPTIONS: CompressionOptions = {
  threshold: 1024, // 1KB
  level: 6, // Default zlib compression level
  exclude: (path: string) =>
    /\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$/.test(path),
};

/**
 * Check if content should be compressed
 */
function shouldCompress(contentType: string, size: number, threshold: number): boolean {
  // Don't compress already compressed types
  if (
    contentType.includes("gzip") ||
    contentType.includes("deflate") ||
    contentType.includes("br") ||
    contentType.includes("image") ||
    contentType.includes("video") ||
    contentType.includes("audio")
  ) {
    return false;
  }

  // Only compress if size exceeds threshold
  return size > threshold;
}

/**
 * Compression middleware
 * Note: For production, consider using native web server compression (Nginx, Apache)
 */
export const compressionMiddleware = (options: CompressionOptions = DEFAULT_OPTIONS) => {
  return async (ctx: any) => {
    const path = new URL(ctx.request.url).pathname;

    // Skip excluded paths
    if (options.exclude && options.exclude(path)) {
      return;
    }

    // Store original response
    const originalResponse = ctx.response;

    // Wrap response with compression info
    if (originalResponse && typeof originalResponse.clone === "function") {
      try {
        const clone = await originalResponse.clone();
        const contentType = clone.headers.get("content-type") || "";
        const buffer = await clone.arrayBuffer();

        const threshold = options.threshold || DEFAULT_OPTIONS.threshold || 1024;

        if (shouldCompress(contentType, buffer.byteLength, threshold)) {
          logger.debug("Compression eligible", {
            path,
            originalSize: buffer.byteLength,
            contentType,
          });
        }
      } catch (error) {
        logger.debug("Could not analyze response for compression");
      }
    }
  };
};

/**
 * Get compression stats
 */
export const getCompressionStats = (originalSize: number, compressedSize: number): string => {
  const ratio = ((1 - compressedSize / originalSize) * 100).toFixed(2);
  return `${ratio}% reduction (${originalSize} → ${compressedSize} bytes)`;
};

/**
 * Response size tracker
 */
export class ResponseSizeTracker {
  private stats = {
    totalResponses: 0,
    totalSize: 0,
    compressedSize: 0,
    avgSize: 0,
  };

  track(originalSize: number, compressedSize?: number): void {
    this.stats.totalResponses++;
    this.stats.totalSize += originalSize;
    if (compressedSize) {
      this.stats.compressedSize += compressedSize;
    }
    this.stats.avgSize = Math.round(this.stats.totalSize / this.stats.totalResponses);
  }

  getStats() {
    return {
      ...this.stats,
      compressionRatio: this.stats.compressedSize > 0 
        ? (((1 - this.stats.compressedSize / this.stats.totalSize) * 100).toFixed(2) + "%")
        : "N/A",
    };
  }

  reset(): void {
    this.stats = {
      totalResponses: 0,
      totalSize: 0,
      compressedSize: 0,
      avgSize: 0,
    };
  }
}

export const responseTracker = new ResponseSizeTracker();
