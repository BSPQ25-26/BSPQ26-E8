const TOTAL_USERS = Number.parseInt(process.env.COUNT || "50", 10);
const BASE_URL = process.env.BASE_URL || "http://localhost:10000";
const REGISTER_URL = `${BASE_URL.replace(/\/$/, "")}/api/auth/register`;

if (!Number.isFinite(TOTAL_USERS) || TOTAL_USERS <= 0) {
  throw new Error("COUNT must be a positive integer");
}

async function createTestUsers() {
  for (let i = 1; i <= TOTAL_USERS; i += 1) {
    const payload = {
      email: `testuser${i}@example.com`,
      username: `testuser${i}`,
      password: "TestPassword!"
    };

    const response = await fetch(REGISTER_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    if (!(response.status === 201 || response.status === 409 || response.status === 400)) {
      throw new Error(`Unexpected status ${response.status}`);
    }
  }
}

createTestUsers();