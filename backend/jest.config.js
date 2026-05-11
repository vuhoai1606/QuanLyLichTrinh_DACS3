/**
 * Jest/Bun test configuration
 * Place at: bunfig.toml or jest.config.js
 */

module.exports = {
  displayName: "Backend Tests",
  testEnvironment: "node",
  testMatch: [
    "**/__tests__/**/*.test.ts",
    "**/?(*.)+(spec|test).ts",
  ],
  collectCoverageFrom: [
    "src/**/*.ts",
    "!src/**/*.d.ts",
    "!src/__tests__/**",
    "!src/index.ts",
    "!src/bootstrap.ts",
  ],
  coveragePathIgnorePatterns: [
    "/node_modules/",
    "dist/",
  ],
  coverageThreshold: {
    global: {
      branches: 60,
      functions: 60,
      lines: 60,
      statements: 60,
    },
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^@config/(.*)$": "<rootDir>/src/config/$1",
    "^@controllers/(.*)$": "<rootDir>/src/controllers/$1",
    "^@middleware/(.*)$": "<rootDir>/src/middleware/$1",
    "^@models/(.*)$": "<rootDir>/src/models/$1",
    "^@routes/(.*)$": "<rootDir>/src/routes/$1",
    "^@services/(.*)$": "<rootDir>/src/services/$1",
    "^@utils/(.*)$": "<rootDir>/src/utils/$1",
    "^@dtos/(.*)$": "<rootDir>/src/dtos/$1",
    "^@constants/(.*)$": "<rootDir>/src/constants/$1",
  },
  setupFilesAfterEnv: ["<rootDir>/src/__tests__/setup.ts"],
  testTimeout: 30000,
  verbose: true,
  bail: false,
  // Coverage reporters
  coverageReporters: ["text", "text-summary", "html", "lcov", "json"],
  coverageDirectory: "coverage/",
  // Test reporters
  reporters: [
    "default",
    [
      "jest-junit",
      {
        outputDirectory: "test-results/",
        outputName: "junit.xml",
        classNameTemplate: "{classname}",
        titleTemplate: "{title}",
        ancestorSeparator: " › ",
        usePathAsClassName: true,
      },
    ],
  ],
};
