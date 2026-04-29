# BFY API Examples - cURL Commands

Complete collection of API endpoints with examples. Copy & paste these commands to test!

## 🔐 Authentication API

### 1. Register New User

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "MyPassword123",
    "full_name": "John Doe"
  }'
```

**Response:**
```json
{
  "status": 201,
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "_id": "uuid-here",
      "email": "john@example.com",
      "full_name": "John Doe",
      "total_exp": 0,
      "current_rank": "Rookie"
    },
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

### 2. Login

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "MyPassword123"
  }'
```

### 3. Get Current User Profile

```bash
curl -X GET http://localhost:3000/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Update Profile

```bash
curl -X PUT http://localhost:3000/api/auth/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "full_name": "John Updated",
    "avatar_url": "https://example.com/avatar.jpg",
    "bio": "I love productivity",
    "timezone": "Asia/Ho_Chi_Minh"
  }'
```

---

## 📋 Schedule API

### 5. Create Category

```bash
curl -X POST http://localhost:3000/api/schedules/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Work",
    "hex_color": "#FF5733"
  }'
```

### 6. Create TODO

```bash
curl -X POST http://localhost:3000/api/schedules/todo \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Buy groceries",
    "category_id": "uuid-of-category"
  }'
```

### 7. Create TASK (with deadline)

```bash
curl -X POST http://localhost:3000/api/schedules/task \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Finish project report",
    "description": "Complete Q2 financial report",
    "deadline": "2026-05-20T17:00:00Z",
    "category_id": "uuid-of-category",
    "rrule": "FREQ=WEEKLY;BYDAY=FR"
  }'
```

### 8. Create EVENT (with time range)

```bash
curl -X POST http://localhost:3000/api/schedules/event \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Team Meeting",
    "description": "Weekly sync with team",
    "start_time": "2026-04-17T10:00:00Z",
    "end_time": "2026-04-17T11:00:00Z",
    "category_id": "uuid-of-category",
    "is_all_day": false,
    "is_countdown_enabled": false
  }'
```

### 9. Get Daily Timeline

```bash
curl -X GET "http://localhost:3000/api/schedules/timeline?startDate=2026-04-17T00:00:00Z&endDate=2026-04-17T23:59:59Z" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "status": 200,
  "success": true,
  "message": "Timeline retrieved successfully",
  "data": [
    {
      "_id": "uuid",
      "title": "Team Meeting",
      "type": "EVENT",
      "start_time": "2026-04-17T10:00:00Z",
      "status": "PENDING"
    }
  ]
}
```

### 10. Update Schedule

```bash
curl -X PUT http://localhost:3000/api/schedules/:schedule_id \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "title": "Updated Title",
    "status": "DONE",
    "description": "New description"
  }'
```

### 11. Delete Schedule

```bash
curl -X DELETE http://localhost:3000/api/schedules/:schedule_id \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## ⏱️ Focus API

### 12. Create Focus Session (Completed)

```bash
curl -X POST http://localhost:3000/api/focus/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "duration_minutes": 25,
    "status": "COMPLETED"
  }'
```

**Response:**
```json
{
  "status": 201,
  "success": true,
  "message": "Focus session created successfully",
  "data": {
    "_id": "uuid",
    "user_id": "user-uuid",
    "duration_minutes": 25,
    "status": "COMPLETED",
    "exp_earned": 250
  }
}
```

### 13. Create Focus Session (Failed)

```bash
curl -X POST http://localhost:3000/api/focus/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "duration_minutes": 25,
    "status": "FAILED"
  }'
```

### 14. Get Focus History

```bash
curl -X GET "http://localhost:3000/api/focus/history?limit=50" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 15. Get Focus Statistics

```bash
curl -X GET http://localhost:3000/api/focus/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "status": 200,
  "success": true,
  "message": "Focus stats retrieved successfully",
  "data": {
    "total_minutes": 250,
    "total_sessions": 10,
    "completed": 9
  }
}
```

---

## 👥 Collaboration API

### 16. Create Group

```bash
curl -X POST http://localhost:3000/api/collaboration/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Startup Team"
  }'
```

### 17. Get User Groups

```bash
curl -X GET http://localhost:3000/api/collaboration/groups \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 18. Add Member to Group

```bash
curl -X POST http://localhost:3000/api/collaboration/groups/:group_id/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "user_id": "member-uuid"
  }'
```

### 19. Get Group Members

```bash
curl -X GET http://localhost:3000/api/collaboration/groups/:group_id/members \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 20. Assign Schedule to User

```bash
curl -X POST http://localhost:3000/api/collaboration/assignments/:schedule_id \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "assignee_id": "team-member-uuid"
  }'
```

### 21. Get User Assignments

```bash
curl -X GET http://localhost:3000/api/collaboration/assignments \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 22. Accept Assignment

```bash
curl -X PATCH http://localhost:3000/api/collaboration/assignments/:schedule_id/ACCEPTED \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 23. Decline Assignment

```bash
curl -X PATCH http://localhost:3000/api/collaboration/assignments/:schedule_id/DECLINED \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🧪 Testing Workflow

### Complete User Journey

```bash
# 1. Register
TOKEN=$(curl -s -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123456",
    "full_name": "Test User"
  }' | jq -r '.data.token')

echo "Token: $TOKEN"

# 2. Create Category
CATEGORY=$(curl -s -X POST http://localhost:3000/api/schedules/categories \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Personal",
    "hex_color": "#FF5733"
  }' | jq -r '.data._id')

echo "Category: $CATEGORY"

# 3. Create Task
curl -s -X POST http://localhost:3000/api/schedules/task \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"title\": \"Complete documentation\",
    \"deadline\": \"2026-05-20T17:00:00Z\",
    \"category_id\": \"$CATEGORY\"
  }" | jq

# 4. Create Focus Session
curl -s -X POST http://localhost:3000/api/focus/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "duration_minutes": 25,
    "status": "COMPLETED"
  }' | jq

# 5. Check stats
curl -s -X GET http://localhost:3000/api/focus/stats \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

## 📌 Notes

- Replace `YOUR_JWT_TOKEN` with actual token from login/register
- Replace `:schedule_id` and `:group_id` with actual UUIDs
- All timestamps use ISO 8601 format (UTC): `2026-04-17T10:00:00Z`
- Use `jq` for pretty-printing JSON (install with `bun install -g jq`)

---

**Created:** April 2026  
**BFY Backend API v1.0**
