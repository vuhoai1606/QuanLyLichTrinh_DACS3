const http = require('http');

function registerUser(user) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(user);
    const req = http.request(
      'http://localhost:3000/api/auth/register',
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Content-Length': data.length,
        },
      },
      (res) => {
        let body = '';
        res.on('data', (chunk) => (body += chunk));
        res.on('end', () => resolve(JSON.parse(body)));
      }
    );
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

async function run() {
  const user1 = {
    full_name: "Test User 1",
    email: "testuser1@example.com",
    password: "Password123!",
  };

  const user2 = {
    full_name: "Test User 2",
    email: "testuser2@example.com",
    password: "Password123!",
  };

  try {
    console.log("Registering User 1...");
    const res1 = await registerUser(user1);
    console.log("Response 1:", res1);

    console.log("Registering User 2...");
    const res2 = await registerUser(user2);
    console.log("Response 2:", res2);
  } catch (error) {
    console.error("Error:", error);
  }
}

run();
