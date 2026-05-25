const { Client } = require('pg');
const bcrypt = require('bcryptjs');
require('dotenv').config();

// Test database configuration from src/__tests__/setup.ts
const dbConfig = {
  host: process.env.DATABASE_HOST || 'localhost',
  port: parseInt(process.env.DATABASE_PORT) || 5432,
  user: process.env.DATABASE_USER || 'postgres',
  password: process.env.DATABASE_PASSWORD || 'v01215335600',
  database: 'postgres', // Connect to default 'postgres' db to create the new one
};

const testDbName = 'bfy_test';

async function setupTestDatabase() {
  const client = new Client(dbConfig);

  try {
    await client.connect();
    console.log('Connected to PostgreSQL to set up the test database...');

    // 1. Drop the existing test database if it exists
    console.log(`Checking for and dropping existing database: ${testDbName}...`);
    const checkDbQuery = `SELECT 1 FROM pg_database WHERE datname = '${testDbName}'`;
    const { rows } = await client.query(checkDbQuery);

    if (rows.length > 0) {
      console.log(`Database "${testDbName}" exists. Dropping it...`);
      // Terminate all connections to the test database before dropping it
      await client.query(`
        SELECT pg_terminate_backend(pg_stat_activity.pid)
        FROM pg_stat_activity
        WHERE pg_stat_activity.datname = '${testDbName}'
          AND pid <> pg_backend_pid();
      `);
      await client.query(`DROP DATABASE "${testDbName}"`);
      console.log(`Database "${testDbName}" dropped.`);
    } else {
      console.log(`Database "${testDbName}" does not exist. Skipping drop.`);
    }

    // 2. Create the test database
    console.log(`Creating new database: ${testDbName}...`);
    await client.query(`CREATE DATABASE "${testDbName}"`);
    console.log(`Database "${testDbName}" created successfully.`);

    // 3. Grant privileges to the test user
    console.log(`Granting privileges to user "${dbConfig.user}" on database "${testDbName}"...`);
    try {
      await client.query(`GRANT ALL PRIVILEGES ON DATABASE "${testDbName}" TO "${dbConfig.user}";`);
      console.log('Privileges granted.');
    } catch (e) {
      console.log('Privileges grant skipped (user might be superuser already):', e.message);
    }

    await client.end();
    console.log('Test database setup complete! ✅');

  } catch (error) {
    console.error('❌ Error setting up the test database:', error.stack);
    process.exit(1); // Exit with error
  }
}

setupTestDatabase();
