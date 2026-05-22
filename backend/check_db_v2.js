import pg from "pg";
const { Client } = pg;

const client = new Client({
  host: "localhost",
  port: 5432,
  database: "Quan_Li_Lich_Trinh",
  user: "postgres",
  password: "v01215335600",
});

async function main() {
  await client.connect();

  const tables = await client.query(`
    SELECT table_name 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    ORDER BY table_name
  `);

  console.log("=== TABLES IN DATABASE ===");
  tables.rows.forEach(r => console.log(" -", r.table_name));

  console.log("\n=== COLUMNS PER TABLE ===");
  for (const row of tables.rows) {
    const tbl = row.table_name;
    const cols = await client.query(`
      SELECT column_name, data_type, is_nullable, column_default
      FROM information_schema.columns
      WHERE table_schema = 'public' AND table_name = $1
      ORDER BY ordinal_position
    `, [tbl]);
    console.log(`\n[${tbl}]`);
    cols.rows.forEach(c =>
      console.log(`  ${c.column_name} | ${c.data_type} | nullable:${c.is_nullable} | default:${c.column_default}`)
    );
  }

  await client.end();
}

main().catch(e => { console.error(e); process.exit(1); });
