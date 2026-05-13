const { Client } = require("pg");

async function createDatabase() {
  const client = new Client({
    user: "postgres",
    password: "v01215335600",
    host: "localhost",
    port: 5432,
    database: "postgres", // Connect to default 'postgres' DB first
  });

  try {
    await client.connect();
    console.log("🔌 Connected to PostgreSQL");

    // Check if database exists
    const result = await client.query(
      "SELECT 1 FROM pg_database WHERE datname = $1",
      ["Quan_Li_Lich_Trinh"]
    );

    if (result.rows.length > 0) {
      console.log("✅ Database Quan_Li_Lich_Trinh already exists");
    } else {
      await client.query('CREATE DATABASE "Quan_Li_Lich_Trinh"');
      console.log("✅ Database Quan_Li_Lich_Trinh created successfully");
    }

    await client.end();
  } catch (error) {
    console.error("❌ Error:", error.message);
    process.exit(1);
  }
}

createDatabase();
