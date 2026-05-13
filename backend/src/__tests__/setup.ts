/**
 * Jest/Bun test setup
 * Runs before all tests
 */

// Setup environment variables for testing
process.env.NODE_ENV = "test";
process.env.DATABASE_HOST = "localhost";
process.env.DATABASE_PORT = "5432";
process.env.DATABASE_USER = "test_user";
process.env.DATABASE_PASSWORD = "test_password";
process.env.DATABASE_NAME = "bfy_test";
process.env.JWT_SECRET = "test_secret_key_for_testing_only_123456";

// Suppress console logs during tests (optional)
// global.console = {
//   ...console,
//   log: jest.fn(),
//   debug: jest.fn(),
//   info: jest.fn(),
//   warn: jest.fn(),
//   error: jest.fn(),
// };

// Add custom matchers if needed
expect.extend({
  toBeWithinRange(received: number, floor: number, ceiling: number) {
    const pass = received >= floor && received <= ceiling;
    if (pass) {
      return {
        message: () =>
          `expected ${received} not to be within range ${floor} - ${ceiling}`,
        pass: true,
      };
    } else {
      return {
        message: () =>
          `expected ${received} to be within range ${floor} - ${ceiling}`,
        pass: false,
      };
    }
  },
});

// Global test timeout
jest.setTimeout(30000);

// Cleanup after all tests
afterAll(async () => {
  // Close database connections
  // Close any open servers
  // Clear timers
  jest.clearAllTimers();
});
