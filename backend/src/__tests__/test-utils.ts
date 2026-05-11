import { describe, it, expect, beforeAll, afterAll, beforeEach } from "bun:test";
import { DataSource } from "typeorm";

/**
 * Test database setup
 */
export async function setupTestDatabase(): Promise<DataSource> {
  // In-memory SQLite for testing (or test PostgreSQL)
  const dataSource = new DataSource({
    type: "better-sqlite3",
    database: ":memory:",
    entities: [],
    synchronize: true,
    logging: false,
  });

  await dataSource.initialize();
  return dataSource;
}

/**
 * Cleanup test database
 */
export async function cleanupTestDatabase(dataSource: DataSource): Promise<void> {
  if (dataSource.isInitialized) {
    await dataSource.destroy();
  }
}

/**
 * Mock request context
 */
export function createMockContext(): Record<string, any> {
  return {
    request: {
      method: "GET",
      url: "http://localhost:3000/api/test",
      headers: {
        get: (name: string) => {
          const headers: Record<string, string> = {
            "content-type": "application/json",
          };
          return headers[name.toLowerCase()];
        },
      },
    },
    state: {},
    body: {},
    response: new Response("{}"),
  };
}

/**
 * Mock user
 */
export function createMockUser(overrides?: Record<string, any>) {
  return {
    id: "user_123",
    email: "test@example.com",
    password: "HashedPassword123",
    full_name: "Test User",
    bio: "Test bio",
    avatar_url: "https://example.com/avatar.jpg",
    created_at: new Date(),
    updated_at: new Date(),
    ...overrides,
  };
}

/**
 * Mock schedule
 */
export function createMockSchedule(overrides?: Record<string, any>) {
  return {
    id: "schedule_123",
    user_id: "user_123",
    title: "Test Schedule",
    description: "Test description",
    type: "TASK",
    status: "PENDING",
    priority: "HIGH",
    due_date: new Date(),
    created_at: new Date(),
    updated_at: new Date(),
    ...overrides,
  };
}

/**
 * Assert error response
 */
export function expectErrorResponse(response: Response, expectedStatus: number, expectedCode?: string) {
  expect(response.status).toBe(expectedStatus);

  if (expectedCode) {
    // Parse and check error code if needed
  }
}

/**
 * Assert success response
 */
export async function expectSuccessResponse(response: Response, expectedStatus: number = 200) {
  expect(response.status).toBe(expectedStatus);

  const data = await response.json();
  expect(data.success).toBe(true);
  expect(data.status).toBe(expectedStatus);
}

/**
 * Test suite wrapper
 */
export function testSuite(name: string, tests: () => void) {
  describe(name, tests);
}

/**
 * Performance benchmark
 */
export async function benchmark(
  name: string,
  fn: () => Promise<void>,
  iterations: number = 100
) {
  const start = performance.now();

  for (let i = 0; i < iterations; i++) {
    await fn();
  }

  const duration = performance.now() - start;
  const avgTime = duration / iterations;

  console.log(`📊 Benchmark: ${name}`);
  console.log(`   Total: ${duration.toFixed(2)}ms`);
  console.log(`   Average: ${avgTime.toFixed(2)}ms`);
  console.log(`   Iterations: ${iterations}`);

  return { total: duration, average: avgTime, iterations };
}

/**
 * Wait helper for async tests
 */
export function wait(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/**
 * Retry helper
 */
export async function retry<T>(
  fn: () => Promise<T>,
  maxAttempts: number = 3,
  delayMs: number = 100
): Promise<T> {
  let lastError: Error | undefined;

  for (let i = 0; i < maxAttempts; i++) {
    try {
      return await fn();
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));
      if (i < maxAttempts - 1) {
        await wait(delayMs);
      }
    }
  }

  throw lastError || new Error("Retry failed");
}
