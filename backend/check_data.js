import pg from "pg";
const { Client } = pg;

const client = new Client({
  host: "localhost",
  port: 5432,
  database: "Quan_li_Lich_Trinh",
  user: "postgres",
  password: "v01215335600",
});

async function main() {
  await client.connect();

  console.log("=== USERS ===");
  const users = await client.query("SELECT id, email, full_name FROM users LIMIT 5");
  users.rows.forEach(r => console.log(r));

  console.log("\n=== GROUPS ===");
  const groups = await client.query("SELECT * FROM groups LIMIT 5");
  groups.rows.forEach(r => console.log(r));

  console.log("\n=== GROUP MEMBERS ===");
  const members = await client.query("SELECT * FROM group_members LIMIT 5");
  members.rows.forEach(r => console.log(r));

  console.log("\n=== GROUP TASKS / SCHEDULES ===");
  const schedules = await client.query("SELECT id, title, creator_id, group_id, type FROM schedules WHERE group_id IS NOT NULL LIMIT 5");
  schedules.rows.forEach(r => console.log(r));

  await client.end();
}

main().catch(e => { console.error(e); process.exit(1); });
