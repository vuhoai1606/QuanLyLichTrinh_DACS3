# BFY Backend API - ElysiaJS + PostgreSQL

Better For Yourself (BFY) - Backend API built with **ElysiaJS**, **PostgreSQL**, **TypeORM**, and **TypeScript** for the Android-first application ecosystem.

## 📋 Features

- ✅ **User Authentication** - Register, Login, Profile Management
- ✅ **Schedule Management** - Create TODO, TASK, EVENT items with timeline aggregation
- ✅ **Focus Mode** - Pomodoro sessions with EXP rewards
- ✅ **Collaboration** - Group management and task assignments
- ✅ **Gamification** - EXP system and rank progression
- ✅ **RESTful API** - Clean, standard endpoints
- ✅ **JWT Authentication** - Secure token-based auth
- ✅ **MongoDB Integration** - Normalized schema with proper indexing

## 🚀 Quick Start

### Prerequisites

- **Bun** 1.0+ ([install here](https://bun.sh))
- **PostgreSQL** 12+ (local or cloud via Heroku/Railway/Supabase)
- **Node** 18+ (for TypeScript support)

### Installation

1. **Clone/Navigate to project:**
   ```bash
   cd d:\laptrinhdidong_DACS3\backend
   ```

2. **Install dependencies:**
   ```bash
   bun install
   ```

3. **Configure environment:**
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` file:
   ```env
   PORT=3000
   NODE_ENV=development
   DATABASE_TYPE=postgres
   DATABASE_HOST=localhost
   DATABASE_PORT=5432
   DATABASE_NAME=bfy
   DATABASE_USER=postgres
   DATABASE_PASSWORD=postgres
   DATABASE_SSL=false
   JWT_SECRET=your_secret_key_here
   CORS_ORIGIN=http://localhost:3001
   ```

4. **Start PostgreSQL** (choose one):
   - **Local PostgreSQL**: `psql` or use pgAdmin/DBeaver
   - **Docker**: `docker run -d -p 5432:5432 --name postgres -e POSTGRES_PASSWORD=postgres postgres:16`
   - **Cloud (Supabase)**: Create account and get connection string from [supabase.com](https://supabase.com)

5. **Run development server:**
   ```bash
   bun run dev
   ```

   Server runs at: `http://localhost:3000`

## 📦 Project Structure

```
src/
├── config/           # Configuration files
│   ├── database.ts   # MongoDB connection
│   └── env.ts        # Environment variables
├── models/           # Mongoose schemas
│   ├── User.ts
│   ├── Schedule.ts
│   ├── Group.ts
│   ├── FocusSession.ts
│   └── ...
├── services/         # Business logic
│   ├── AuthService.ts
│   ├── ScheduleService.ts
│   ├── FocusService.ts
│   └── CollaborationService.ts
├── routes/           # API endpoints
│   ├── auth.ts
│   ├── schedule.ts
│   ├── focus.ts
│   └── collaboration.ts
├── middleware/       # Authentication & CORS
│   ├── auth.ts
│   └── cors.ts
├── utils/            # Helper functions
│   ├── jwt.ts
│   ├── password.ts
│   ├── validation.ts
│   └── errors.ts
└── index.ts          # Main server entry
```

## 🔌 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user profile
- `PUT /api/auth/profile` - Update profile

### Schedules
- `POST /api/schedules/categories` - Create category
- `POST /api/schedules/todo` - Create TODO
- `POST /api/schedules/task` - Create TASK
- `POST /api/schedules/event` - Create EVENT
- `GET /api/schedules/timeline?startDate=...&endDate=...` - Get timeline
- `PUT /api/schedules/:id` - Update schedule
- `DELETE /api/schedules/:id` - Delete schedule

### Focus
- `POST /api/focus/sessions` - Create focus session
- `GET /api/focus/history` - Get focus history
- `GET /api/focus/stats` - Get focus statistics

### Collaboration
- `POST /api/collaboration/groups` - Create group
- `GET /api/collaboration/groups` - Get user groups
- `POST /api/collaboration/groups/:group_id/members` - Add member
- `GET /api/collaboration/groups/:group_id/members` - Get group members
- `POST /api/collaboration/assignments/:schedule_id` - Assign schedule
- `GET /api/collaboration/assignments` - Get user assignments
- `PATCH /api/collaboration/assignments/:schedule_id/:status` - Update assignment status

## 🔐 Authentication

All protected endpoints require `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

**Example Register:**
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123",
    "full_name": "John Doe"
  }'
```

**Example Login:**
```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123"
  }'
```

## 💾 Database Schema

All models use **PostgreSQL** with **TypeORM**:

- **users**: User accounts, profiles, gamification stats
- **user_settings**: Per-user settings (language, theme, notifications)
- **categories**: Custom color-coded categories
- **schedules**: Unified TODO/TASK/EVENT items
- **reminders**: Reminder configurations for schedules
- **groups**: Collaboration groups
- **group_members**: Group membership tracking
- **schedule_assignments**: Task assignments in groups
- **focus_sessions**: Pomodoro session history with EXP

Auto-sync enabled: Tables are created automatically in development mode!

## 🛠️ Development

### Run Tests
```bash
bun test
```

### Build for Production
```bash
bun run build
```

### Start Production Server
```bash
bun run prod
```

## 📝 Code Examples

### Create a Schedule (TODO)

```bash
curl -X POST http://localhost:3000/api/schedules/todo \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "title": "Buy groceries",
    "category_id": "uuid-here"
  }'
```

### Create Focus Session

```bash
curl -X POST http://localhost:3000/api/focus/sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "duration_minutes": 25,
    "status": "COMPLETED"
  }'
```

### Get Daily Timeline

```bash
curl "http://localhost:3000/api/schedules/timeline?startDate=2026-04-16T00:00:00Z&endDate=2026-04-16T23:59:59Z" \
  -H "Authorization: Bearer <token>"
```

## 🎯 Gamification System

- **EXP Calculation**: 1 min focus = 10 EXP
- **Ranks**:
  - Rookie: 0 EXP
  - Novice: 100 EXP
  - Apprentice: 500 EXP
  - Expert: 1000 EXP
  - Master: 5000 EXP
  - Legend: 10000 EXP

## 🚀 Deployment

### Environment Variables for Deployment
```
DATABASE_TYPE=postgres
DATABASE_HOST=your-postgres-host.com
DATABASE_PORT=5432
DATABASE_NAME=bfy
DATABASE_USER=your_db_user
DATABASE_PASSWORD=your_secure_password
DATABASE_SSL=true
JWT_SECRET=<long_random_string>
NODE_ENV=production
CORS_ORIGIN=https://your-frontend.com
```

### Docker (Coming Soon)
```dockerfile
FROM oven/bun
COPY . /app
WORKDIR /app
RUN bun install
CMD ["bun", "src/index.ts"]
```

## 📚 Technologies

- **Framework**: ElysiaJS (Lightweight, fast TypeScript web framework)
- **Runtime**: Bun (Modern JavaScript runtime)
- **Database**: PostgreSQL with TypeORM ORM
- **Authentication**: JWT (jsonwebtoken)
- **Password Hashing**: bcryptjs
- **Language**: TypeScript 5.3

## 🤝 Contributing

1. Follow existing code structure
2. Use TypeScript strictly
3. Maintain consistent error handling
4. Add proper JSDoc comments
5. Test all endpoints before commit

## 📄 License

MIT License - BFY Project 2026

## 📞 Support

For issues or questions, contact the development team or check the documentation in the project.

---

**Built for the BFY ecosystem** 🎯  
Deadline: 20/05/2026
