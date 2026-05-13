const http = require("http");

function makeRequest(method, path, body = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: "localhost",
      port: 3000,
      path: path,
      method: method,
      headers: {
        "Content-Type": "application/json",
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
  console.log("🧪 Testing BFY Backend API\n");

  try {
    // Test 1: Register
    console.log("📝 Test 1: Register");
    const registerRes = await makeRequest("POST", "/api/auth/register", {
      email: "test@example.com",
      password: "TestPass123",
      full_name: "Test User",
    });
    console.log(`Status: ${registerRes.status}`);
    console.log(`Response:`, registerRes.body);

    if (registerRes.status === 201 && registerRes.body.data?.token) {
      const token = registerRes.body.data.token;
      const userId = registerRes.body.data.user?.id;

      // Test 2: Login
      console.log("\n🔐 Test 2: Login");
      const loginRes = await makeRequest("POST", "/api/auth/login", {
        email: "test@example.com",
        password: "TestPass123",
      });
      console.log(`Status: ${loginRes.status}`);
      console.log(`Response:`, loginRes.body);

      // Test 3: Create Category
      console.log("\n📂 Test 3: Create Category");
      const catRes = await makeRequest("POST", "/api/schedule/categories", {
        user_id: userId,
        name: "Work",
        hex_color: "#FF5733",
      });
      console.log(`Status: ${catRes.status}`);
      console.log(`Response:`, catRes.body);
    }

    // Test 4: Get health
    console.log("\n❤️ Test 4: Health Check");
    const healthRes = await makeRequest("GET", "/health");
    console.log(`Status: ${healthRes.status}`);
    console.log(`Response:`, healthRes.body);

    console.log("\n✅ Testing complete!");
  } catch (error) {
    console.error("❌ Error:", error.message);
  }
}

testAPI();
