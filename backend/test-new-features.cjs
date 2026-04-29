const BASE_URL = "http://localhost:3000/api";

let testUserId = "";
let testUserId2 = "";
let testScheduleId = "";
let testGroupId = "";
let testCategoryId = "";

async function makeRequest(method, endpoint, body = null) {
  const options = {
    method,
    headers: { "Content-Type": "application/json" },
  };

  if (body) options.body = JSON.stringify(body);

  const response = await fetch(`${BASE_URL}${endpoint}`, options);
  const data = await response.json();

  return { status: response.status, ...data };
}

async function runTests() {
  console.log("\n🧪 Testing New Endpoints\n");

  try {
    // 1. Register two test users
    console.log("=== 📝 USER REGISTRATION ===\n");
    
    const reg1 = await makeRequest("POST", "/auth/register", {
      email: `test_new_1_${Date.now()}@example.com`,
      password: "password123",
      full_name: "Test User 1",
    });
    testUserId = reg1.data.user.id;
    console.log(`✓ User 1 registered: ${testUserId}\n`);

    const reg2 = await makeRequest("POST", "/auth/register", {
      email: `test_new_2_${Date.now()}@example.com`,
      password: "password123",
      full_name: "Test User 2",
    });
    testUserId2 = reg2.data.user.id;
    console.log(`✓ User 2 registered: ${testUserId2}\n`);

    // 2. Create a schedule for sharing
    console.log("=== 📅 CREATE SCHEDULE ===\n");
    
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const schedRes = await makeRequest("POST", "/schedule/todo", {
      creator_id: testUserId,
      title: "Test Task for Sharing",
      type: "TASK",
      description: "This task will be shared",
      deadline: tomorrow.toISOString(),
    });
    testScheduleId = schedRes.data.id;
    console.log(`✓ Schedule created: ${testScheduleId}\n`);

    // 3. Test User Search
    console.log("=== 🔍 USER SEARCH ===\n");
    
    const searchRes = await makeRequest("GET", `/users/search?q=Test&user_id=${testUserId}&limit=5`);
    console.log(`✓ Found ${searchRes.data.length} users\n`);

    // 4. Test Share Copy (Mechanism 1)
    console.log("=== 📋 SHARE COPY (Mechanism 1) ===\n");
    
    const shareCopyRes = await makeRequest("POST", "/collaboration/share-copy", {
      schedule_id: testScheduleId,
      sender_id: testUserId,
      recipient_id: testUserId2,
    });
    console.log(`✓ Schedule copied to user 2`);
    console.log(`  Original: ${shareCopyRes.data.original_schedule_id}`);
    console.log(`  Copy: ${shareCopyRes.data.new_schedule_id}\n`);

    // 5. Test Share Collab (Mechanism 2)
    console.log("=== 🤝 SHARE COLLAB (Mechanism 2) ===\n");
    
    const shareCollabRes = await makeRequest("POST", "/collaboration/share-collab", {
      schedule_id: testScheduleId,
      sender_id: testUserId,
      recipient_id: testUserId2,
      permission_level: "EDIT",
    });
    console.log(`✓ User 2 is now a collaborator`);
    console.log(`  Permission: ${shareCollabRes.data.permission_level}\n`);

    // 6. Get Collaborators
    console.log("=== 👥 GET COLLABORATORS ===\n");
    
    const collabRes = await makeRequest("GET", `/collaboration/schedule/${testScheduleId}/collaborators`);
    console.log(`✓ Got ${collabRes.data.length} collaborators\n`);

    // 7. Test Leaderboard
    console.log("=== 🏆 LEADERBOARD ===\n");
    
    const leaderboardRes = await makeRequest(
      "GET",
      `/gamification/leaderboard?user_id=${testUserId}&type=global&limit=10`
    );
    console.log(`✓ Global leaderboard: ${leaderboardRes.data.length} entries\n`);

    // 8. Get Ranks
    console.log("=== 🎖️  RANKS ===\n");
    
    const ranksRes = await makeRequest("GET", `/gamification/ranks`);
    console.log(`✓ Available ranks: ${ranksRes.data.length}`);
    ranksRes.data.forEach((rank, i) => {
      console.log(`  ${i + 1}. ${rank.rank_name} (${rank.min_exp} EXP)`);
    });
    console.log();

    // 9. Get User Rank Info
    console.log("=== 📊 USER RANK INFO ===\n");
    
    const rankInfoRes = await makeRequest("GET", `/gamification/user/${testUserId}/rank-info`);
    console.log(`✓ User Rank Information:`);
    console.log(`  Current Rank: ${rankInfoRes.data.current_rank}`);
    console.log(`  Total EXP: ${rankInfoRes.data.total_exp}`);
    console.log(`  EXP to Next: ${rankInfoRes.data.exp_to_next_level}\n`);

    // 10. Get Badges
    console.log("=== 🏅 BADGES ===\n");
    
    const badgesRes = await makeRequest("GET", `/gamification/badges?user_id=${testUserId}`);
    if (badgesRes.data) {
      console.log(`✓ Unlocked Badges: ${badgesRes.data.unlocked?.length || 0}`);
      console.log(`✓ Locked Badges: ${badgesRes.data.locked?.length || 0}\n`);
    } else {
      console.log(`❌ Error: ${badgesRes.error?.message}\n`);
    }

    // 11. Save FCM Token
    console.log("=== 📱 FCM TOKEN ===\n");
    
    const fcmRes = await makeRequest("POST", "/notifications/fcm-token", {
      user_id: testUserId,
      token: "test_fcm_token_" + Date.now(),
      platform: "ANDROID",
      device_name: "Samsung Galaxy S21",
    });
    console.log(`✓ FCM token saved`);
    console.log(`  Token: ${fcmRes.data.token.substring(0, 20)}...`);
    console.log(`  Platform: ${fcmRes.data.platform}\n`);

    // 12. Get Notifications
    console.log("=== 📬 NOTIFICATIONS ===\n");
    
    const notifRes = await makeRequest("GET", `/notifications?user_id=${testUserId}&limit=10`);
    console.log(`✓ Got ${notifRes.data.length} notifications\n`);

    // 13. Dashboard Summary
    console.log("=== 📈 DASHBOARD SUMMARY ===\n");
    
    const dashboardRes = await makeRequest("GET", `/schedule/dashboard/summary?user_id=${testUserId}`);
    console.log(`✓ Dashboard Summary for Today:`);
    console.log(`  Events: ${dashboardRes.data.today.events}`);
    console.log(`  Tasks: ${dashboardRes.data.today.tasks}`);
    console.log(`  TODOs: ${dashboardRes.data.today.todos}`);
    console.log(`  Focus Minutes: ${dashboardRes.data.today.focus_minutes}`);
    console.log(`  EXP Earned Today: ${dashboardRes.data.today.exp_earned}\n`);

    // 14. Weekly Stats
    console.log("=== 📊 WEEKLY STATS ===\n");
    
    const weeklyRes = await makeRequest("GET", `/schedule/dashboard/weekly-stats?user_id=${testUserId}`);
    console.log(`✓ Weekly Statistics:`);
    weeklyRes.data.forEach((day) => {
      console.log(`  ${day.day}: ${day.focus_minutes} min, ${day.exp_earned} EXP`);
    });
    console.log();

    console.log("=".repeat(50));
    console.log("✅ ALL NEW ENDPOINT TESTS COMPLETED!");
    console.log("=".repeat(50));
  } catch (error) {
    console.error("❌ Test Error:", error.message);
  }
}

runTests();
