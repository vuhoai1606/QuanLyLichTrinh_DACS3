const { Client } = require('pg');
const bcrypt = require('bcryptjs');
require('dotenv').config();

// Test database configuration from src/__tests__/setup.ts
const dbConfig = {
  host: process.env.DATABASE_HOST || 'localhost',
  port: process.env.DATABASE_PORT || 5432,
  user: 'test_user',
  password: 'test_password',
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
    console.log(`Granting privileges to user "test_user" on database "${testDbName}"...`);
    await client.query(`GRANT ALL PRIVILEGES ON DATABASE "${testDbName}" TO test_user;`);
    console.log('Privileges granted.');

    // 4. Seed test user
    await client.end();
    
    const testDbClient = new Client({
      ...dbConfig,
      database: testDbName
    });
    
    await testDbClient.connect();
    console.log('Connected to test database to seed data...');
    
    // Create users table (minimal for seeding)
    await testDbClient.query(`
      CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        email VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        full_name VARCHAR(100) NOT NULL,
        avatar_url VARCHAR(255),
        bio VARCHAR(280),
        timezone VARCHAR(50) DEFAULT 'UTC',
        total_exp INTEGER DEFAULT 0,
        current_rank VARCHAR(50) DEFAULT 'Rookie',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    
    const hashedPass = await bcrypt.hash('SecurePass123', 10);
    const userId = 'user_123_seeded_00000000000000000000'; // Must be valid UUID format or we just use a real one
    
    // Use a fixed UUID for testing
    const fixedUserId = '00000000-0000-0000-0000-000000000001';
    
    console.log(`Seeding test user: test@example.com (${fixedUserId})`);
    await testDbClient.query(`
      INSERT INTO users (id, email, password_hash, full_name)
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (email) DO NOTHING
    `, [fixedUserId, 'test@example.com', hashedPass, 'Test User']);
    
    await testDbClient.end();
    console.log('Test database setup complete! ✅');

  } catch (error) {
    console.error('❌ Error setting up the test database:', error.stack);
    process.exit(1); // Exit with error
  }
}

setupTestDatabase();
