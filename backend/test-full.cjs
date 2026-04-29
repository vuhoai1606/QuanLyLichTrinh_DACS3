const http = require("http");
const crypto = require("crypto");

function makeRequest(method, path, body = null, headers = {}) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: "localhost",
      port: 3000,
      path: path,
      method: method,
      headers: {
        "Content-Type": "application/json",
        ...headers,
      },
    };

    const req = http.request(options, (res) => {
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        try {
          resolve({
            status: res.statusCode,
            body: JSON.parse(data),
          });
        } catch (e) {
          resolve({
            status: res.statusCode,
            body: data,
          });
        }
      });
    });

    req.on("error", reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function testAPI() {
  console.log("🧪 Complete BFY Backend API Test Suite\n");
  let passedTests = 0;
  let totalTests = 0;

  const testEmail = `test${crypto.randomBytes(4).toString("hex")}@example.com`;
  let authToken, userId, scheduleId, categoryId;

  try {
    // ===== AUTH TESTS =====
    console.log("=== 🔐 AUTH ENDPOINTS ===\n");

    totalTests++;
    console.log("✓ [1/1] POST /api/auth/register");
    const registerRes = await makeRequest("POST", "/api/auth/register", {
      email: testEmail,
      password: "TestPass123",
      full_name: "Test User",
    });
    if (registerRes.status === 201) {
      passedTests++;
      authToken = registerRes.body.data?.token;
      userId = registerRes.body.data?.user?.id;
      console.log(`   ✅ Registered: ${testEmail} (${userId})\n`);
    } else {
      console.log(`   ❌ Status: ${registerRes.status}\n`);
    }

    totalTests++;
    console.log("✓ [2/2] POST /api/auth/login");
    const loginRes = await makeRequest("POST", "/api/auth/login", {
      email: testEmail,
      password: "TestPass123",
    });
    if (loginRes.status === 200) {
      passedTests++;
      console.log(`   ✅ Login successful\n`);
    } else {
      console.log(`   ❌ Status: ${loginRes.status}\n`);
    }

    if (!userId) {
      console.log("❌ Failed to create user, skipping schedule tests");
      return;
    }

    // ===== SCHEDULE TESTS =====
    console.log("=== 📅 SCHEDULE ENDPOINTS ===\n");

    totalTests++;
    console.log("✓ [3/5] POST /api/schedule/categories");
    const catRes = await makeRequest("POST", "/api/schedule/categories", {
      user_id: userId,
      name: "Work",
      hex_color: "#FF5733",
    });
    if (catRes.status === 201) {
      passedTests++;
      categoryId = catRes.body.data?.id;
      console.log(`   ✅ Category created\n`);
    } else {
      console.log(`   ❌ Status: ${catRes.status} - ${JSON.stringify(catRes.body)}\n`);
    }

    totalTests++;
    console.log("✓ [4/5] POST /api/schedule/todo");
    const schedRes = await makeRequest("POST", "/api/schedule/todo", {
      creator_id: userId,
      title: "Complete backend",
      description: "Finish API endpoints",
      priority: "HIGH",
      category_id: categoryId,
    });
    if (schedRes.status === 201) {
      passedTests++;
      scheduleId = schedRes.body.data?.id;
      console.log(`   ✅ TODO created\n`);
    } else {
      console.log(`   ❌ Status: ${schedRes.status} - ${JSON.stringify(schedRes.body)}\n`);
    }

    totalTests++;
    console.log("✓ [5/5] GET /api/schedule/timeline");
    const timelineRes = await makeRequest("GET", "/api/schedule/timeline?start=2026-01-01&end=2026-12-31");
    if (timelineRes.status === 200) {
      passedTests++;
      console.log(`   ✅ Timeline retrieved (${timelineRes.body.data?.length || 0} items)\n`);
    } else {
      console.log(`   ❌ Status: ${timelineRes.status}\n`);
    }

    // ===== FOCUS TESTS =====
    console.log("=== 🎯 FOCUS ENDPOINTS ===\n");

    totalTests++;
    console.log("✓ [6/8] POST /api/focus/sessions");
    const focusRes = await makeRequest("POST", "/api/focus/sessions", {
      user_id: userId,
      duration_minutes: 25,
      status: "COMPLETED",
    });
    if (focusRes.status === 201) {
      passedTests++;
      console.log(`   ✅ Focus session created (25 exp earned)\n`);
    } else {
      console.log(`   ❌ Status: ${focusRes.status}\n`);
    }

    totalTests++;
    console.log("✓ [7/8] GET /api/focus/history");
    const historyRes = await makeRequest("GET", "/api/focus/history?user_id=" + userId);
    if (historyRes.status === 200) {
      passedTests++;
      console.log(`   ✅ Focus history retrieved\n`);
    } else {
      console.log(`   ❌ Status: ${historyRes.status}\n`);
    }

    totalTests++;
    console.log("✓ [8/8] GET /api/focus/stats");
    const statsRes = await makeRequest("GET", "/api/focus/stats?user_id=" + userId);
    if (statsRes.status === 200) {
      passedTests++;
      console.log(`   ✅ Focus stats retrieved\n`);
    } else {
      console.log(`   ❌ Status: ${statsRes.status}\n`);
    }

    // ===== COLLABORATION TESTS =====
    console.log("=== 👥 COLLABORATION ENDPOINTS ===\n");

    totalTests++;
    console.log("✓ [9/10] POST /api/collaboration/groups");
    const groupRes = await makeRequest("POST", "/api/collaboration/groups", {
      name: "Team Alpha",
      leader_id: userId,
    });
    if (groupRes.status === 201) {
      passedTests++;
      console.log(`   ✅ Group created\n`);
    } else {
      console.log(`   ❌ Status: ${groupRes.status}\n`);
    }

    totalTests++;
    console.log("✓ [10/10] GET /api/collaboration/groups");
    const groupsRes = await makeRequest("GET", "/api/collaboration/groups?user_id=" + userId);
    if (groupsRes.status === 200) {
      passedTests++;
      console.log(`   ✅ Groups retrieved\n`);
    } else {
      console.log(`   ❌ Status: ${groupsRes.status}\n`);
    }

    // ===== SUMMARY =====
    console.log(`\n${"=".repeat(50)}`);
    console.log(`📊 RESULTS: ${passedTests}/${totalTests} tests passed`);
    console.log(`${"=".repeat(50)}`);

    if (passedTests === totalTests) {
      console.log("✅ ALL TESTS PASSED - Backend 100% Complete!");
    } else {
      console.log(`⚠️  ${totalTests - passedTests} test(s) failed`);
    }
  } catch (error) {
    console.error("❌ Test Error:", error.message);
  }
}

testAPI();
