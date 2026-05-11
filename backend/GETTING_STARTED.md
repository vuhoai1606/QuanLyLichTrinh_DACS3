# Getting Started - Backend Setup Guide

Hướng dẫn chi tiết setup backend cho máy mới từ đầu đến khi chạy ứng dụng.

## 📋 Mục Lục
1. [Yêu cầu](#yêu-cầu-hệ-thống)
2. [Cài đặt](#cài-đặt)
3. [Cấu hình](#cấu-hình)
4. [Chạy Backend](#chạy-backend)
5. [Luồng Chạy Hệ Thống](#luồng-chạy-hệ-thống)
6. [API Endpoints](#api-endpoints)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)

---

## 🖥️ Yêu cầu Hệ Thống

### Bắt buộc
- **Node.js**: v18.0.0 hoặc cao hơn (hoặc Bun runtime)
- **Bun Runtime**: v1.0.0+ (khuyến nghị thay cho Node.js - nhanh hơn 3x)
- **PostgreSQL**: v13.0+ (database chính)
- **Git**: để clone repository
- **npm hoặc pnpm**: package manager

### Kiểm tra Phiên bản Hiện Tại
```bash
# Kiểm tra Node.js
node --version
npm --version

# Hoặc nếu dùng Bun
bun --version

# Kiểm tra PostgreSQL
psql --version
```

---

## 📦 Cài đặt

### Bước 1: Clone Repository
```bash
git clone <repository-url>
cd laptrinhdidong_DACS3\ -\ Copy\ -\ Copy/backend
```

### Bước 2: Cài đặt Bun Runtime (Khuyến nghị)
```bash
# Windows - dùng PowerShell
powershell -c "irm bun.sh/install.ps1|iex"

# macOS/Linux
curl -fsSL https://bun.sh/install | bash

# Xác nhận cài đặt
bun --version
```

### Bước 3: Cài đặt Dependencies
```bash
# Dùng Bun (nhanh hơn)
bun install

# Hoặc dùng npm
npm install

# Hoặc dùng pnpm
pnpm install
```

### Bước 4: Cài đặt PostgreSQL

**Windows:**
```bash
# Download từ https://www.postgresql.org/download/windows/
# Hoặc dùng Chocolatey
choco install postgresql

# Sau khi cài, khởi động service
net start postgresql-x64-13
```

**macOS:**
```bash
# Dùng Homebrew
brew install postgresql@15
brew services start postgresql@15
```

**Linux (Ubuntu):**
```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start
```

---

## ⚙️ Cấu hình

### Bước 1: Tạo File .env
Tại thư mục `backend`, tạo file `.env` với nội dung:

```env
# Database
DATABASE_URL=postgresql://postgres:your_password@localhost:5432/bfy_schedule
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=your_password
DB_NAME=bfy_schedule

# JWT Secret
JWT_SECRET=your_super_secret_key_at_least_32_characters_long
JWT_EXPIRY=24h

# Server
PORT=3000
NODE_ENV=development
RUNTIME=bun

# Firebase (optional - nếu dùng notifications)
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_PRIVATE_KEY=your_private_key
FIREBASE_CLIENT_EMAIL=your_client_email
```

### Bước 2: Tạo Database
```bash
# Kết nối vào PostgreSQL
psql -U postgres

# Trong psql shell
CREATE DATABASE bfy_schedule;
CREATE USER bfy_user WITH PASSWORD 'secure_password';
ALTER ROLE bfy_user SET client_encoding TO 'utf8';
ALTER ROLE bfy_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE bfy_user SET default_transaction_deferrable TO on;
ALTER ROLE bfy_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE bfy_schedule TO bfy_user;
\q
```

### Bước 3: Khởi tạo Database Schema
```bash
# Tự động tạo tables từ entities
bun run bootstrap

# Hoặc dùng npm
npm run bootstrap
```

**Output mong đợi:**
```
✅ Database connection established
✅ Schema created successfully
✅ 15 tables initialized: User, Schedule, FocusSession, Group, ...
```

---

## ▶️ Chạy Backend

### Phương thức 1: Dùng Bun (Khuyến nghị)

**Chế độ Development**
```bash
bun run dev
```

**Chế độ Production**
```bash
bun run start
```

### Phương thức 2: Dùng npm

**Chế độ Development**
```bash
npm run dev
```

**Chế độ Production**
```bash
npm run build
npm start
```

### Kiểm tra Server Đang Chạy
Mở browser đến: `http://localhost:3000`

Hoặc dùng curl:
```bash
curl http://localhost:3000/monitoring/health
```

**Response mong đợi:**
```json
{
  "status": "up",
  "timestamp": "2026-05-11T10:30:00Z",
  "database": "connected",
  "uptime": "45s"
}
```

---

## 🔄 Luồng Chạy Hệ Thống

### Kiến Trúc Backend

```
┌─────────────────────────────────────────┐
│         Client (Frontend)               │
└──────────────┬──────────────────────────┘
               │ HTTP/REST API
               ▼
┌─────────────────────────────────────────┐
│      Elysia Web Server (Port 3000)     │
│  - JWT Authentication Middleware       │
│  - CORS Middleware                     │
│  - Error Handling                      │
└──────────────┬──────────────────────────┘
               │
       ┌───────┴───────┬─────────┬───────────┬─────────┐
       ▼               ▼         ▼           ▼         ▼
   ┌────────┐  ┌──────────┐ ┌────────┐ ┌──────────┐ ┌────────┐
   │ Auth   │  │Schedule  │ │Focus   │ │Collab.  │ │Settings│
   │Route   │  │Route     │ │Route   │ │Route    │ │Route   │
   └────┬───┘  └────┬─────┘ └───┬────┘ └────┬────┘ └───┬────┘
        │            │           │           │          │
        ▼            ▼           ▼           ▼          ▼
   ┌────────┐  ┌──────────┐ ┌────────┐ ┌──────────┐ ┌────────┐
   │Auth    │  │Schedule  │ │Focus   │ │Collab.  │ │Settings│
   │Control │  │Control   │ │Control │ │Control  │ │Control │
   └────┬───┘  └────┬─────┘ └───┬────┘ └────┬────┘ └───┬────┘
        │            │           │           │          │
        ▼            ▼           ▼           ▼          ▼
   ┌────────┐  ┌──────────┐ ┌────────┐ ┌──────────┐ ┌────────┐
   │Auth    │  │Schedule  │ │Focus   │ │Collab.  │ │Settings│
   │Service │  │Service   │ │Service │ │Service  │ │Service │
   └────┬───┘  └────┬─────┘ └───┬────┘ └────┬────┘ └───┬────┘
        │            │           │           │          │
        └────────────┴───────────┴───────────┴──────────┘
                     │
                     ▼
        ┌────────────────────────────────┐
        │  TypeORM Repository Layer      │
        │  - User, Schedule, Group, etc. │
        └────────────┬───────────────────┘
                     │
                     ▼
        ┌────────────────────────────────┐
        │   PostgreSQL Database          │
        │  - 15 Tables                   │
        │  - Relations & Constraints     │
        └────────────────────────────────┘
```

### Luồng Request Cơ Bản

#### 1. Đăng nhập (Login)
```
POST /api/auth/login
↓
AuthController.login()
↓
AuthService.login()
  - Kiểm tra user tồn tại
  - Verify password (bcrypt)
  - Generate JWT token (24h expiry)
↓
Response: { token, user: { id, name, email } }
↓
Client lưu token vào localStorage
```

#### 2. Lấy Dashboard Data
```
GET /api/schedule/dashboard/summary
+ Header: Authorization: Bearer <token>
↓
JWT Middleware: Verify token + Extract userId
↓
ScheduleController.getDashboardSummary(userId)
↓
DashboardService.getDashboardSummary(userId)
  - Fetch user info
  - Calculate rank from total_exp
  - Get today's schedules
  - Calculate daily stats
  - Get 7-day streak
↓
Response: {
  user: { id, name, email, avatar_url },
  rank: { id, name, level, total_exp, exp_progress_percent },
  summary: [ { id, title, priority, status, due_date }, ... ],
  stats: { today: 5, recent_7_days: 32, streak: 8 },
  message: "Great work! You've completed 5 tasks today!"
}
```

#### 3. Tạo Focus Session
```
POST /api/focus/sessions
+ Body: { title, duration_minutes: 25, status: "STARTED" }
+ Header: Authorization: Bearer <token>
↓
FocusController.createSession()
↓
FocusService.createFocusSession()
  - Validate duration
  - Create session record
  - Start timer
↓
Response: { 
  id: "session-123",
  user_id: "user-456",
  duration_minutes: 25,
  started_at: "2026-05-11T10:30:00Z",
  status: "STARTED"
}
```

#### 4. Hoàn thành Focus Session
```
PUT /api/focus/sessions/:id
+ Body: { status: "COMPLETED", actual_duration_minutes: 25 }
↓
FocusService.updateFocusSession()
  - Mark session as COMPLETED
  - Calculate XP: 25 minutes × 10 = 250 XP
  - Add XP to user profile
  - Check for rank up
  - Update streak if applicable
↓
Update UserRank:
  - Old total_exp: 1200
  - New total_exp: 1450 (+250)
  - Old rank: "Novice" (100-300 exp)
  - New rank: "Apprentice" (300-600 exp) ✓ RANK UP!
  - Send achievement notification
↓
Response: {
  session: { id, status: "COMPLETED", exp_earned: 250 },
  user: { total_exp: 1450, rank: "Apprentice", level: 3 },
  achievement: { name: "Apprentice", unlocked: true }
}
```

#### 5. Lấy Leaderboard
```
GET /api/gamification/leaderboard?limit=10
↓
GamificationService.getLeaderboard()
  - Query top 10 users by total_exp
  - Rank: 1, 2, 3, ...
  - Include rank name, level, exp
↓
Response: [
  { rank: 1, user: "Alex", exp: 5200, rank_name: "Master" },
  { rank: 2, user: "Jane", exp: 4800, rank_name: "Master" },
  { rank: 3, user: "Bob", exp: 3200, rank_name: "Expert" },
  ...
]
```

---

## 🔌 API Endpoints

### Authentication (`/api/auth/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/register` | Đăng ký user mới |
| POST | `/auth/login` | Đăng nhập |
| GET | `/auth/profile` | Lấy profile |
| POST | `/auth/change-password` | Đổi mật khẩu |
| POST | `/auth/forgot-password` | Quên mật khẩu |
| POST | `/auth/reset-password` | Reset mật khẩu |

### Schedule Management (`/api/schedule/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/schedule/create/:type` | Tạo schedule (TODO/TASK/EVENT) |
| GET | `/schedule/:id` | Lấy chi tiết schedule |
| PUT | `/schedule/:id` | Cập nhật schedule |
| DELETE | `/schedule/:id` | Xoá schedule |
| GET | `/schedule/dashboard/summary` | Lấy home dashboard |
| GET | `/schedule/dashboard/weekly` | Lấy stats hàng tuần |
| GET | `/schedule/dashboard/monthly` | Lấy stats hàng tháng |

### Focus Sessions (`/api/focus/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/focus/sessions` | Tạo focus session |
| GET | `/focus/sessions` | Lấy danh sách sessions |
| PUT | `/focus/sessions/:id` | Cập nhật session |
| POST | `/focus/sessions/:id/pause` | Tạm dừng session |
| POST | `/focus/sessions/:id/resume` | Tiếp tục session |
| GET | `/focus/history` | Lấy lịch sử sessions |
| GET | `/focus/stats` | Lấy stats focus |
| GET | `/focus/streak` | Lấy streak hiện tại |

### Collaboration (`/api/collaboration/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/collaboration/groups` | Tạo group |
| GET | `/collaboration/groups/:id` | Lấy chi tiết group |
| POST | `/collaboration/groups/:id/members` | Thêm member vào group |
| POST | `/collaboration/schedules/share` | Chia sẻ schedule |
| GET | `/collaboration/activity` | Lấy activity log |

### Gamification (`/api/gamification/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/gamification/ranks` | Lấy danh sách ranks |
| GET | `/gamification/leaderboard` | Lấy top users |
| GET | `/gamification/badges` | Lấy badges |
| GET | `/gamification/user/:userId/rank-info` | Lấy rank info của user |

### Settings (`/api/settings/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/settings/app` | Lấy app settings |
| GET | `/settings/user` | Lấy user settings |
| PUT | `/settings/user` | Cập nhật user settings |
| POST | `/settings/theme` | Đổi theme (LIGHT/DARK/SYSTEM) |
| POST | `/settings/language` | Đổi ngôn ngữ |

---

## 🧪 Testing

### Chạy All Tests
```bash
bun test
# hoặc
npm test
```

### Chạy Tests Từng Service
```bash
# Auth tests
bun test test/auth.test.ts

# Schedule tests
bun test test/schedule.test.ts

# Focus tests
bun test test/focus.test.ts
```

### Integration Testing
```bash
# Chạy toàn bộ integration tests
npm run test:integration

# Chạy end-to-end flow
bun run test-full.cjs
```

### Manual API Testing
```bash
# Test health endpoint
curl http://localhost:3000/monitoring/health

# Test login
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Test dashboard (với token)
curl http://localhost:3000/api/schedule/dashboard/summary \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🆘 Troubleshooting

### Lỗi: "Cannot find module 'elysia'"
```bash
# Xoá node_modules và cài lại
rm -r node_modules
bun install
```

### Lỗi: "Error connecting to database"
```bash
# Kiểm tra PostgreSQL đang chạy
sudo service postgresql status

# Kiểm tra kết nối
psql -U postgres -d bfy_schedule

# Xem database credentials trong .env
cat .env | grep DATABASE
```

### Lỗi: "Port 3000 already in use"
```bash
# Windows - Tìm process dùng port 3000
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :3000
kill -9 <PID>

# Hoặc đổi port trong .env
PORT=3001
```

### Lỗi: "JWT token expired"
- Token hết hạn sau 24 giờ
- Frontend cần refresh token
- POST `/api/auth/refresh` để lấy token mới

### Lỗi: "User not found"
```bash
# Kiểm tra user tồn tại
psql -U postgres -d bfy_schedule
SELECT * FROM "User" WHERE email = 'test@example.com';

# Nếu chưa có, tạo user mới
INSERT INTO "User" (name, email, password_hash) 
VALUES ('Test User', 'test@example.com', 'hash_here');
```

### Performance Issues
```bash
# Xem database queries
EXPLAIN ANALYZE SELECT * FROM "Schedule" WHERE user_id = '123';

# Enable query logging
# Thêm vào .env:
DB_LOGGING=true

# Dùng Bun thay Node.js (3x nhanh hơn)
bun run dev
```

---

## 📞 Quick Reference Commands

```bash
# Cài đặt & Setup
bun install                    # Cài dependencies
npm run bootstrap              # Khởi tạo database
npm run seed                   # Seed test data

# Chạy
bun run dev                    # Dev mode
npm start                      # Production mode
npm run build                  # Build TypeScript

# Testing
npm test                       # All tests
npm run test:integration       # Integration tests
bun run test-full.cjs         # Full end-to-end

# Database
npm run db:create             # Tạo database
npm run db:drop               # Xoá database
npm run db:reset              # Reset database
npm run db:seed               # Seed data

# Maintenance
npm run lint                   # Check code style
npm run format                # Format code
npm run clean                 # Clean build files
```

---

## 🎯 Verification Checklist

Sau khi setup, kiểm tra:

- [ ] Bun/Node.js cài đặt: `bun --version` hoặc `node --version`
- [ ] PostgreSQL chạy: `psql -U postgres`
- [ ] Dependencies cài: `ls node_modules | grep elysia`
- [ ] .env file có đầy đủ variables
- [ ] Database tạo: `psql -l | grep bfy_schedule`
- [ ] Backend chạy: `curl http://localhost:3000/monitoring/health`
- [ ] Health check OK: `{"status":"up"}`
- [ ] Tests pass: `npm test` → 0 failures
- [ ] API respond: `curl http://localhost:3000/api/auth/profile` (nếu authenticate)

✅ Nếu tất cả đều OK → Backend sẵn sàng!

---

## 📚 Tài liệu Thêm

- **Elysia Docs**: https://elysiajs.com
- **TypeORM Docs**: https://typeorm.io
- **PostgreSQL Docs**: https://www.postgresql.org/docs/
- **JWT Guide**: https://jwt.io/introduction
- **Bun Runtime**: https://bun.sh

---

**Cần giúp gì khác? Liên hệ team development** 🚀
