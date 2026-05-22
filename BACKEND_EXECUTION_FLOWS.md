# Backend Execution Flows - BFY Project

## 📋 Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    CLIENT REQUEST                               │
│            (Frontend / Mobile / External API)                    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   ELYSIA SERVER (Port 3000)                      │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         GLOBAL MIDDLEWARE (Applied to all requests)      │  │
│  │  • Rate Limit (100 req/min)                             │  │
│  │  • CORS Middleware                                      │  │
│  │  • Request ID Tracking & Logging                        │  │
│  │  • Security Headers                                     │  │
│  │  • Compression                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
│                       │                                          │
│                       ▼                                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              ROUTE HANDLER (/api/...)                    │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │ 1. Check Route Params & Query Strings              │ │  │
│  │  │ 2. Route-specific Middleware (e.g., authMiddleware) │ │  │
│  │  │ 3. Request Validation                              │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  │                       │                                  │  │
│  │                       ▼                                  │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │         CONTROLLER LAYER                            │ │  │
│  │  │  (e.g., AuthController, ScheduleController)        │ │  │
│  │  │  • Parse & Validate Input (DTOs)                   │ │  │
│  │  │  • Prepare Data                                    │ │  │
│  │  │  • Call Service Layer                              │ │  │
│  │  │  • Format Response                                 │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  │                       │                                  │  │
│  │                       ▼                                  │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │         SERVICE LAYER                              │ │  │
│  │  │  (e.g., AuthService, ScheduleService)              │ │  │
│  │  │  • Business Logic Processing                        │ │  │
│  │  │  • Data Transformations                             │ │  │
│  │  │  • Interact with Database via ORM                   │ │  │
│  │  │  • External Service Integration (APIs, etc.)        │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  │                       │                                  │  │
│  │                       ▼                                  │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │       TypeORM Repository / Models                  │ │  │
│  │  │  (e.g., User, Schedule, Goal, FocusSession)        │ │  │
│  │  │  • Query Database                                  │ │  │
│  │  │  • Create/Update/Delete Records                    │ │  │
│  │  │  • Validate Business Rules                         │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  │                       │                                  │  │
│  │                       ▼                                  │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │      PostgreSQL Database (TypeORM)                │ │  │
│  │  │  • Execute SQL Queries                             │ │  │
│  │  │  • Return Data                                     │ │  │
│  │  └─────────────────────────────────────────────────────┘ │  │
│  │                       │                                  │  │
│  └───────────────────────┬──────────────────────────────────┘  │
│                          │                                      │
│                          ▼                                      │
│           ┌──────────────────────────────┐                     │
│           │  Format Response (Success/Error)                  │
│           │  • successResponse()                              │
│           │  • errorResponse()                                │
│           └──────────────────────────────┘                     │
│                          │                                      │
└──────────────────────────┼──────────────────────────────────────┘
                           │
                           ▼
                    CLIENT RESPONSE (JSON)
```

---

## 🔄 Chi tiết các luồng chính

### 1. **AUTHENTICATION & USER MANAGEMENT FLOW**

#### Register / Login Flow
```
POST /api/auth/register
├─ Request: { email, password, full_name, gender, dob }
├─ AuthController.register(body)
│  ├─ Validate input using DTOs
│  │  └─ validateRegister() [app.constants.ts]
│  └─ Call AuthService.register()
│     ├─ Check valid email format
│     ├─ Validate password strength
│     ├─ Check if email exists in DB
│     ├─ Hash password (bcryptjs)
│     ├─ Create User record (TypeORM)
│     ├─ Create UserSettings record (Default: language=en, theme=SYSTEM)
│     ├─ Generate JWT tokens
│     │  ├─ accessToken (short-lived)
│     │  └─ refreshToken (long-lived)
│     └─ Return { user, accessToken, refreshToken }
└─ Response: { user, token, message, status }

POST /api/auth/login
├─ Request: { email, password }
├─ AuthController.login(body)
│  ├─ Validate input
│  └─ Call AuthService.login()
│     ├─ Find user by email (case-insensitive)
│     ├─ Verify password hash
│     ├─ Generate new tokens
│     └─ Return { user, tokens }
└─ Response: { user, token }

GET /api/auth/me (Protected)
├─ authMiddleware checks Authorization header
│  ├─ Extract JWT token
│  ├─ Verify token signature
│  ├─ Decode and get user ID
│  └─ Attach user info to request context
├─ AuthController.getProfile(ctx)
│  └─ Fetch user details from DB
└─ Response: { user profile data }
```

---

### 2. **SCHEDULE & TASK MANAGEMENT FLOW**

#### Create Schedule Flow
```
POST /api/schedule/create
├─ Request: { title, description, start_time, end_time, category_id, ... }
├─ authMiddleware (verify JWT)
├─ ScheduleController.createSchedule(body, ctx)
│  ├─ Validate input (DTOs)
│  ├─ Call ScheduleService.createSchedule()
│  │  ├─ Get user ID from context
│  │  ├─ Validate category exists
│  │  ├─ Check for time conflicts
│  │  ├─ Create Schedule entity
│  │  │  ├─ Generate unique ID
│  │  │  ├─ Set timestamps (created_at, updated_at)
│  │  │  └─ Store in DB via TypeORM
│  │  ├─ If assignment type = "GROUP": Create GroupAssignment
│  │  ├─ If reminder set: Create Reminder record
│  │  └─ Return created schedule
│  └─ Format response
└─ Response: { schedule, message }

GET /api/schedule/list?date=2026-05-20 (Protected)
├─ authMiddleware
├─ ScheduleController.getSchedules()
│  └─ Call ScheduleService.getSchedules()
│     ├─ Query Schedule table filtered by:
│     │  ├─ user_id = current_user.id
│     │  ├─ start_time >= date (start of day)
│     │  └─ end_time <= date (end of day)
│     ├─ Join with Category info
│     ├─ Join with FocusSession (if exists)
│     └─ Return sorted by start_time
└─ Response: [ { schedule }, { schedule }, ... ]

PUT /api/schedule/:id (Protected)
├─ ScheduleController.updateSchedule(id, body)
│  └─ ScheduleService.updateSchedule(id, data)
│     ├─ Find schedule by ID
│     ├─ Verify user ownership
│     ├─ Update fields
│     ├─ Validate new times (no conflicts)
│     ├─ Update DB record
│     └─ Trigger notifications if time changed
└─ Response: { updated schedule }
```

---

### 3. **FOCUS SESSION FLOW** ⏱️

#### Start Focus Session
```
POST /api/focus/start
├─ Request: { schedule_id, duration_minutes, focus_mode }
├─ authMiddleware
├─ FocusController.startSession()
│  └─ Call FocusService.startSession()
│     ├─ Find Schedule by ID
│     ├─ Check if already has active focus session
│     ├─ Create FocusSession entity
│     │  ├─ status = "ACTIVE"
│     │  ├─ start_time = NOW
│     │  ├─ target_duration = duration_minutes
│     │  └─ focus_mode = ["POMODORO", "DEEP_FOCUS", "CUSTOM"]
│     ├─ Store in DB
│     ├─ Trigger WebSocket event → Frontend (show timer)
│     └─ Return session with countdown
└─ Response: { focusSession, remainingTime }

POST /api/focus/end/:sessionId
├─ FocusController.endSession()
│  └─ FocusService.endSession()
│     ├─ Find active FocusSession
│     ├─ Calculate actual_duration
│     ├─ Calculate focus_score (based on duration)
│     ├─ Update status = "COMPLETED"
│     ├─ Update user focus stats
│     ├─ Check if eligible for new badge/rank
│     │  └─ Trigger GamificationService
│     │     ├─ Award badges if milestones reached
│     │     ├─ Update rank/XP
│     │     └─ Send notification
│     ├─ Emit WebSocket event → Frontend (session complete)
│     └─ Return focus_score & rewards
└─ Response: { session, score, badges_earned }

GET /api/focus/stats (Protected)
├─ FocusService.getUserFocusStats()
│  ├─ Query FocusSession table
│  │  ├─ GROUP BY date
│  │  ├─ SUM(actual_duration)
│  │  ├─ AVG(focus_score)
│  │  └─ COUNT by focus_mode
│  └─ Return aggregated stats
└─ Response: { totalHours, avgScore, byMode, dailyTrend }
```

---

### 4. **GAMIFICATION FLOW** 🏆

#### Badge & Rank System
```
Background: Focus Session Completes
│
├─ GamificationService.checkAchievements(userId)
│  ├─ Get user's total focus hours
│  ├─ Get user's consistency (daily streak)
│  ├─ Check predefined badge criteria:
│  │  ├─ "First Focus" → 1 session
│  │  ├─ "Focus Master" → 50+ hours
│  │  ├─ "Consistency" → 7-day streak
│  │  ├─ "Night Owl" → focused at night
│  │  └─ "Week Warrior" → all 7 days focused
│  │
│  ├─ Award badges via UserBadgeService
│  │  └─ Create UserBadge record (if not already earned)
│  │
│  ├─ Update user rank:
│  │  ├─ Bronze (0-100 XP)
│  │  ├─ Silver (100-500 XP)
│  │  ├─ Gold (500-1000 XP)
│  │  └─ Platinum (1000+ XP)
│  │
│  └─ Trigger NotificationService
│     └─ Send in-app notification to user
│
└─ WebSocket: Update Frontend leaderboard (if enabled)

GET /api/gamification/profile (Protected)
├─ GamificationService.getUserProfile()
│  ├─ Get total XP, current rank
│  ├─ Get list of earned badges
│  ├─ Get achievement progress
│  └─ Get leaderboard position
└─ Response: { rank, xp, badges, achievements, leaderboardRank }
```

---

### 5. **COLLABORATION FLOW** 👥

#### Share Schedule / Create Goal Group
```
POST /api/collaboration/share
├─ Request: { schedule_id, shared_with_user_ids, permission }
├─ authMiddleware
├─ CollaborationController.shareSchedule()
│  └─ CollaborationService.shareSchedule()
│     ├─ Verify owner of schedule
│     ├─ For each shared_with_user_id:
│     │  ├─ Create ShareLog record
│     │  ├─ Add permission (VIEW, EDIT, MANAGE)
│     │  └─ Send notification to recipient
│     │     └─ NotificationService.notify()
│     │        ├─ Create Notification record
│     │        ├─ Send FCM push (if mobile)
│     │        └─ WebSocket update (if online)
│     └─ Return share confirmation
└─ Response: { sharedWith, permissions }

GET /api/collaboration/shared-with-me (Protected)
├─ CollaborationService.getSharedItems()
│  ├─ Query Schedule table
│  │  ├─ JOIN ShareLog where user_id = requester
│  │  └─ FILTER by permission level
│  └─ Return shared schedules/goals
└─ Response: [ { schedule, sharedBy, permission } ]

POST /api/collaboration/group/create
├─ Request: { group_name, members, goal_id }
├─ CollaborationService.createGroup()
│  ├─ Create Group entity
│  ├─ Create GroupMember records (one per member)
│  │  ├─ member_id, group_id, role (OWNER/MEMBER)
│  │  └─ joined_at = NOW
│  ├─ If goal: Assign to all members
│  │  └─ Create GoalAssignment records
│  └─ Send invite notifications to members
└─ Response: { group, members }
```

---

### 6. **NOTIFICATION SYSTEM FLOW** 📬

#### Notification Trigger & Delivery
```
Various Events Trigger Notifications:
├─ Focus session about to start (reminder)
├─ Schedule upcoming (15 min before)
├─ Goal deadline approaching
├─ Shared item (someone shared with you)
├─ Badge earned (gamification)
├─ Collaboration invite (group, shared goal)
└─ Admin alerts (system notifications)

NotificationService Flow:
│
├─ Create Notification record:
│  ├─ type: "REMINDER" | "SHARE" | "BADGE" | "DEADLINE" | ...
│  ├─ title, message, data
│  ├─ user_id (recipient)
│  ├─ read = false
│  └─ created_at = NOW
│
├─ Check user notification settings:
│  └─ NotificationService.getUserSettings()
│     └─ Query UserSettings.notifications_enabled
│
├─ Send via multiple channels:
│  │
│  ├─ 📱 FCM Push Notification (Mobile):
│  │  ├─ Get user's FCMToken records
│  │  └─ firebase-admin.messaging.send()
│  │
│  ├─ 🌐 WebSocket (In-App, Real-time):
│  │  ├─ WebSocketService.broadcast()
│  │  ├─ Check if user online
│  │  └─ Send event: { type: "notification", data }
│  │
│  └─ 📊 Database (In-App Notification Center):
│     └─ Already stored in Notification table
│
└─ Mark sent, log delivery status

GET /api/notifications (Protected)
├─ NotificationService.getUserNotifications()
│  ├─ Query Notification table
│  │  ├─ WHERE user_id = currentUser.id
│  │  ├─ ORDER BY created_at DESC
│  │  └─ LIMIT to unread or last 30
│  └─ Return with pagination
└─ Response: [ { notification }, ... ]

POST /api/notifications/:id/read (Protected)
├─ Mark notification as read
└─ Update: read = true, read_at = NOW
```

---

### 7. **ANALYTICS & REPORTING FLOW** 📊

#### Generate Reports
```
POST /api/reports/generate
├─ Request: { start_date, end_date, report_type }
├─ ReportService.generateReport()
│  ├─ Query multiple data sources:
│  │  ├─ FocusSession stats
│  │  ├─ Schedule completion rate
│  │  ├─ Goal progress
│  │  └─ Focus duration trends
│  │
│  ├─ Aggregate & calculate metrics:
│  │  ├─ Total focus hours
│  │  ├─ Average daily focus
│  │  ├─ Most productive hours
│  │  ├─ Focus consistency score
│  │  ├─ Most used focus mode
│  │  └─ Category breakdown
│  │
│  ├─ Generate visualizations data:
│  │  ├─ Daily trend graph data
│  │  ├─ Category pie chart data
│  │  ├─ Hourly distribution
│  │  └─ Weekly comparison
│  │
│  ├─ Create Report record (optional - cache)
│  └─ Return formatted report
│
└─ Response: { metrics, trends, charts }

GET /api/analytics/dashboard (Protected)
├─ AnalyticsService.getDashboardData()
│  ├─ Get overview metrics
│  │  ├─ Total focus today
│  │  ├─ Streaks (current day/week)
│  │  ├─ This week vs last week
│  │  └─ Achievement progress
│  │
│  ├─ Get quick stats
│  │  ├─ Schedules today (completed/pending)
│  │  ├─ Active goals progress
│  │  ├─ New badges earned
│  │  └─ Rank change
│  │
│  └─ Get recommendations
│     └─ Based on user patterns
│
└─ Response: { overview, stats, recommendations }
```

---

### 8. **ADMIN PANEL FLOW** ⚙️

#### System Monitoring & User Management
```
GET /api/admin/users (Admin only)
├─ adminMiddleware (check role = ADMIN)
├─ AdminService.getAllUsers()
│  ├─ Query User table (all users)
│  ├─ Include aggregated stats
│  └─ Pagination
└─ Response: [ { user, stats }, ... ]

POST /api/admin/users/:id/suspend
├─ AdminService.suspendUser(userId)
│  ├─ Update User.status = "SUSPENDED"
│  ├─ Invalidate user sessions/tokens
│  ├─ Log action to audit trail
│  └─ Notify user (optional)
└─ Response: { success, message }

GET /api/admin/analytics (Admin only)
├─ AdminService.getSystemAnalytics()
│  ├─ Total users, active users
│  ├─ Total focus sessions
│  ├─ System load & performance
│  ├─ Storage usage
│  ├─ Error rates
│  └─ API performance metrics
└─ Response: { systemStats, performance }

GET /api/admin/logs (Admin only)
├─ AdminService.getSystemLogs()
│  ├─ Error logs
│  ├─ Audit logs (user actions)
│  ├─ API call logs
│  └─ Performance logs
└─ Response: [ { log }, ... ]
```

---

### 9. **WEBSOCKET REAL-TIME FLOW** 🔗

#### WebSocket Communication (Active Connections)
```
CLIENT CONNECTS: ws://server/ws
├─ WebSocketService.handleConnection(socket)
│  ├─ Verify JWT token
│  ├─ Get user ID from token
│  ├─ Add to active connections map
│  │  └─ Map<userId, socket>
│  ├─ Subscribe to user-specific rooms
│  │  └─ Room: "user:{userId}"
│  └─ Send welcome message
│
├─ Listen for Events:
│  ├─ "focus:update" → broadcast focus progress
│  ├─ "notification" → send real-time notification
│  ├─ "schedule:change" → sync schedule updates
│  ├─ "collaboration:update" → group activity
│  ├─ "typing" → live collaboration
│  └─ "heartbeat" → keep-alive
│
└─ On Disconnect:
   └─ Remove from active connections

Real-time Event Examples:

1️⃣ Focus Session Progress:
   Client: socket.emit("focus:start", { sessionId })
   Server: Periodically broadcast focus remaining time
   Client: Receive & update timer UI

2️⃣ Live Notifications:
   Server Event: Badge earned
   WebSocket broadcast to user socket
   Client: Pop notification immediately

3️⃣ Collaboration Real-time:
   User A: Edits shared schedule
   Server: Broadcast change to User B (if online)
   User B: UI updates instantly (no refresh needed)

4️⃣ Typing Indicator:
   User A: Starts typing goal description
   Broadcast: "User A is typing..."
   User B: Sees indicator (if collaborative editing)
```

---

### 10. **EXTERNAL SERVICE INTEGRATIONS** 🔌

#### AI Service Flow
```
POST /api/ai/generate-goals
├─ Request: { prompt, preferences }
├─ AIService.generateGoals()
│  ├─ Send prompt to Google Generative AI (Gemini)
│  │  └─ API: @google/generative-ai
│  ├─ Parse AI response
│  ├─ Format goals with:
│  │  ├─ Title, description
│  │  ├─ Estimated duration
│  │  ├─ Category suggestion
│  │  └─ Sub-goals breakdown
│  └─ Return structured goals
└─ Response: [ { goal }, ... ]

POST /api/ai/get-suggestions
├─ AIService.getSuggestions(userId)
│  ├─ Gather user data:
│  │  ├─ Recent focus patterns
│  │  ├─ Goal history
│  │  ├─ Time preferences
│  │  └─ Performance metrics
│  ├─ Send to Gemini API
│  ├─ Get personalized recommendations
│  └─ Store suggestions cache
└─ Response: { suggestions, reasoning }
```

#### Firebase Integration
```
Post-Focus Session:
├─ Check if FCM tokens registered
├─ firebase-admin.messaging.send()
│  ├─ Send push notification to device
│  ├─ Data: { notificationId, title, message }
│  └─ Handle delivery status
└─ Update FCMToken.last_sent

Fallback:
├─ If push fails, store in DB
└─ User sees in-app notification next login
```

#### Calendar Sync Flow
```
POST /api/calendar/sync
├─ CalendarSyncService.syncWithCalendar()
│  ├─ Get user's calendar service auth tokens
│  ├─ Fetch events from external calendar:
│  │  ├─ Google Calendar API
│  │  ├─ Outlook Calendar API
│  │  └─ iCal format
│  ├─ Map external events to Schedule entities
│  ├─ Insert/update in BFY database
│  └─ Set up bi-directional sync
└─ Response: { syncedCount, conflicts }

Bi-directional Sync:
├─ When user creates Schedule in BFY:
│  └─ Push to synced external calendar
├─ When user creates event in Google Calendar:
│  └─ Pull and create Schedule in BFY (scheduled job)
└─ Conflict resolution: User's BFY action takes precedence
```

---

## 🔐 **SECURITY FLOW**

### JWT Token Flow
```
1. LOGIN Success
   ├─ Generate accessToken (15 min expiry)
   ├─ Generate refreshToken (7 day expiry)
   └─ Store refreshToken in RefreshToken table

2. Protected API Request
   ├─ Client sends: Authorization: Bearer <accessToken>
   ├─ authMiddleware checks:
   │  ├─ Token exists
   │  ├─ Signature valid (JWT secret)
   │  ├─ Not expired
   │  └─ Extract user_id
   ├─ Attach user to request context
   └─ Proceed to controller

3. Access Token Expired
   ├─ Client catches 401 error
   ├─ Client sends refreshToken
   ├─ Server validates refreshToken
   │  ├─ Check in DB (RefreshToken table)
   │  ├─ Verify signature
   │  └─ Not revoked
   ├─ Issue new accessToken
   └─ Return new token to client

4. LOGOUT / Token Revocation
   ├─ Delete refreshToken from DB
   └─ Access becomes invalid on next request
```

### Request Signing (API Key)
```
POST /api/secure-endpoint
├─ Header: X-API-Key: <api_key>
├─ apiKeyMiddleware checks:
│  ├─ Key exists in apiKeyStore
│  ├─ Key not expired
│  └─ Key not revoked
├─ Proceed if valid
└─ Reject if invalid (401)
```

---

## 📦 **ERROR HANDLING FLOW**

```
Any Error in Request → Exception Caught
│
├─ Check error type:
│  ├─ Validation Error (BAD_REQUEST 400)
│  ├─ Authentication Error (UNAUTHORIZED 401)
│  ├─ Authorization Error (FORBIDDEN 403)
│  ├─ Not Found Error (NOT_FOUND 404)
│  ├─ Conflict Error (CONFLICT 409)
│  └─ Server Error (INTERNAL_ERROR 500)
│
├─ Log error details:
│  ├─ Error message
│  ├─ Stack trace
│  ├─ Request ID (for tracking)
│  ├─ User ID (if authenticated)
│  └─ Timestamp
│
├─ Return formatted error response:
│  ├─ status: HTTP code
│  ├─ success: false
│  ├─ message: user-friendly message
│  ├─ code: error code (e.g., "EMAIL_EXISTS")
│  └─ requestId: for support reference
│
└─ Alert if critical error
   └─ AlertManager.triggerAlert()
```

---

## 🔄 **DATA FLOW SUMMARY**

```
Input Validation & Transformation:
  Raw Request Data → DTO Validation → Type-checked Object

Database Operations:
  Service → TypeORM Repository → SQL Query → PostgreSQL → Result

Response Formatting:
  Raw Data → Format with timestamps → Serialize to JSON → HTTP Response

Async Operations:
  Request → Service (Promise) ─┐
                                ├─→ (background processing)
                                └─→ Log result
  Client receives initial response immediately

Error Propagation:
  Try/Catch in Controller → Check error type → Call errorResponse() → Client
```

---

## 📊 **Database Entities & Relationships**

```
User (Core)
├─ id (PK)
├─ email, password_hash
├─ full_name, gender, dob
└─ created_at, updated_at

├─ → UserSettings (1:1)
│   ├─ language, theme
│   ├─ notifications_enabled
│   └─ default_focus_minutes
│
├─ → Schedule (1:Many)
│   ├─ title, description
│   ├─ start_time, end_time
│   ├─ category_id (FK)
│   └─ status
│   │
│   ├─ → FocusSession (1:Many)
│   │   ├─ start_time, actual_duration
│   │   ├─ focus_score, focus_mode
│   │   └─ status
│   │
│   ├─ → Reminder (1:Many)
│   │   ├─ type, notification_time
│   │   └─ is_completed
│   │
│   └─ → ScheduleAssignment (1:Many)
│       └─ assigned_to_group_id (FK)
│
├─ → Goal (1:Many)
│   ├─ title, description
│   ├─ target_date, priority
│   ├─ status, progress_percentage
│   └─ category_id
│
├─ → Group (1:Many) [Groups created by user]
│   ├─ name, description
│   └─ → GroupMember (1:Many)
│       └─ member_id (FK User)
│
├─ → UserBadge (1:Many)
│   ├─ badge_id (FK)
│   └─ earned_at
│
├─ → Notification (1:Many)
│   ├─ type, message
│   ├─ read, read_at
│   └─ data (JSON)
│
├─ → ChatMessage (1:Many)
│   ├─ content, sender_id
│   ├─ group_id (FK)
│   └─ created_at
│
├─ → ShareLog (1:Many) [Things shared with others]
│   ├─ shared_item_id, shared_item_type
│   ├─ shared_with_user_id (FK User)
│   └─ permission
│
└─ → FCMToken (1:Many)
    ├─ token (device token)
    ├─ device_type
    └─ last_used_at
```

---

## 🚀 **Startup Sequence**

```
1. npm run dev (or bun run src/bootstrap.ts)
   └─ Load environment variables from .env

2. index.ts
   ├─ Initialize Elysia app
   ├─ Setup middleware (CORS, rate limit, security, etc.)
   ├─ Connect to PostgreSQL via TypeORM
   ├─ Setup routes
   ├─ Setup WebSocket
   ├─ Initialize services (Telemetry, HealthCheck, etc.)
   └─ Sync database schema (in development)

3. database.ts connectDB()
   ├─ Create DataSource with all entities
   ├─ Connect to PostgreSQL
   ├─ Synchronize schema (dev only)
   └─ Initialize repositories

4. Start Server
   ├─ Listen on PORT 3000
   ├─ Log startup config
   ├─ Initialize test API keys (development)
   └─ Ready to accept requests

5. Client can start making requests
   ├─ First request creates session
   ├─ Auth endpoints don't require token
   ├─ Other endpoints require valid JWT
   └─ WebSocket can connect with auth token
```

---

## 🧪 **Testing Flow**

```
npm run test
├─ setup-test-db.js (Create test database)
├─ Jest runner finds .test.ts files
│
├─ Unit Tests (__tests__/unit.test.ts)
│  ├─ Test individual functions
│  ├─ Test DTOs & validation
│  ├─ Test utilities
│  └─ No DB required
│
├─ Integration Tests (__tests__/integration.test.ts)
│  ├─ Create test DB schema
│  ├─ Test API endpoints end-to-end
│  ├─ Create test data
│  ├─ Make HTTP requests
│  ├─ Verify responses
│  └─ Cleanup
│
└─ Cleanup test database
```

---

## 📝 **Middleware Execution Order**

```
Client Request Arrives
    ↓
1. Rate Limiter (elysia-rate-limit)
   └─ Check if within 100 req/min limit
    ↓
2. CORS Middleware
   └─ Check origin, add CORS headers
    ↓
3. Request ID Middleware
   └─ Add unique request ID to context
    ↓
4. Security Headers Middleware
   └─ Add security headers to response
    ↓
5. Route-Specific Middleware (if needed)
   └─ authMiddleware (check JWT token)
    ↓
6. Controller Handler
   └─ Process request
    ↓
7. Response Formatting
   └─ successResponse() or errorResponse()
    ↓
8. Compression Middleware
   └─ Compress response body (if enabled)
    ↓
Client Receives Response
```

---

## 🎯 **Key Concepts**

| Concept | Role |
|---------|------|
| **Elysia** | Web framework (similar to Express but for Bun) |
| **TypeORM** | ORM for database operations with entity relationships |
| **PostgreSQL** | Primary data store (relational database) |
| **JWT** | Token-based authentication (stateless) |
| **WebSocket** | Real-time bidirectional communication |
| **DTOs** | Data Transfer Objects for input validation |
| **Services** | Business logic layer, reusable across controllers |
| **Controllers** | Request handlers, format responses |
| **Middleware** | Functions that process requests before reaching handlers |
| **Repository Pattern** | Abstract database operations through TypeORM |

---

## 📍 **File Structure Reference**

```
src/
├─ index.ts              → Main app setup & middleware config
├─ bootstrap.ts          → Entry point
├─ config/
│  ├─ database.ts       → PostgreSQL + TypeORM setup
│  ├─ env.ts            → Environment variables
│  └─ schemas.ts        → Entity definitions
├─ controllers/          → Handle HTTP requests, call services
├─ services/             → Business logic, database operations
├─ models/               → TypeORM entity definitions
├─ routes/               → API endpoints definition
├─ middleware/           → Request processing (auth, logging, etc.)
├─ dtos/                 → Data validation schemas
├─ types/                → TypeScript type definitions
├─ utils/                → Helper functions
└─ constants/            → App constants & HTTP codes
```

---

## ✅ **Summary of Main Flows**

1. **Auth Flow**: Register/Login → JWT Token → Protected endpoints
2. **Schedule Flow**: Create → Set reminder → Due → Focus session → Complete
3. **Focus Flow**: Start session → Running → End → Calculate score → Award badges
4. **Gamification**: Actions → Check achievements → Award badges/XP → Update rank
5. **Collaboration**: Share schedule → Invite members → Sync updates → Real-time sync via WS
6. **Notifications**: Event triggers → Check settings → Send via FCM/WS/DB
7. **Analytics**: Gather data → Aggregate metrics → Generate report/dashboard
8. **Real-time**: WebSocket connect → Subscribe to events → Receive broadcasts
9. **Admin**: Dashboard → View system stats → Manage users → Monitor alerts
10. **External**: AI suggestions → Calendar sync → FCM push

---

# 💬 **GIẢI THÍCH CHO THẦY CÔ - Cách Trình Bày**

## **Version 1: Giải Thích Ngắn Gọn (2-3 phút)**

### "Thầy ơi, em xin trình bày về kiến trúc backend của dự án BFY:"

"**BFY (Better For Yourself)** là một ứng dụng quản lý thời gian và tập trung. Backend của chúng em được xây dựng bằng **Elysia.js** (framework web hiện đại) chạy trên **Bun runtime**, kết nối với **PostgreSQL** làm database.

**Kiến trúc theo 5 tầng:**

1. **Route Layer**: Định nghĩa các endpoint API (ví dụ: POST /api/auth/login, GET /api/schedule/list)

2. **Controller Layer**: Nhận request, validate dữ liệu input (dùng DTOs), gọi Service để xử lý

3. **Service Layer**: Xử lý business logic - ví dụ khi user start focus session, service sẽ:
   - Kiểm tra user có schedule không
   - Tạo record FocusSession mới
   - Tính focus_score sau khi kết thúc
   - Kiểm tra điều kiện để trao badge

4. **Repository Layer (TypeORM)**: Trừu tượng hóa các thao tác database - Create, Read, Update, Delete

5. **Database (PostgreSQL)**: Lưu trữ dữ liệu (User, Schedule, Goal, FocusSession, Notification, v.v.)

**Luồng hoạt động chính:**
- Người dùng gửi request → Middleware kiểm tra (JWT token, CORS, rate limit) → Controller validate → Service xử lý logic → Lưu database → Response JSON

**Các tính năng chính:**
- 🔐 **Authentication**: JWT token (15 phút) + Refresh token (7 ngày)
- 📅 **Schedule Management**: Tạo lịch, set reminder, notification
- ⏱️ **Focus Sessions**: Bắt đầu/kết thúc phiên tập trung, tính điểm
- 🏆 **Gamification**: Trao badge, rank, XP dựa vào hoạt động
- 👥 **Collaboration**: Chia sẻ schedule, tạo group, real-time sync
- 📬 **Notifications**: Gửi qua FCM (mobile), WebSocket (real-time), hoặc in-app
- 📊 **Analytics**: Thống kê focus time, trends, reports

**Real-time technology:**
- WebSocket để sync real-time khi có thay đổi
- Firebase Cloud Messaging (FCM) cho push notification

Em xin kết thúc trình bày!"

---

## **Version 2: Giải Thích Chi Tiết (5-7 phút)**

### "Em xin được trình bày chi tiết hơn về 3 luồng chính:"

**1️⃣ Authentication Flow (Luồng Xác Thực):**
```
User nhập email + password
    ↓
POST /api/auth/login
    ↓
AuthController.login()
    ├─ Validate: email format, password không rỗng
    └─ Call AuthService.login()
       ├─ Query User table by email
       ├─ Verify password hash (bcryptjs)
       ├─ Generate JWT accessToken (15 min)
       ├─ Generate refreshToken (7 day)
       └─ Return { user, token }
    ↓
Client nhận token, lưu vào localStorage
    ↓
Lần request tiếp theo:
├─ Client gửi: Authorization: Bearer <token>
├─ authMiddleware verify signature & expiry
├─ Nếu valid → Attach user info vào request context
├─ Nếu hết hạn → Client dùng refreshToken lấy token mới
└─ Nếu invalid → Return 401 Unauthorized
```

**2️⃣ Focus Session Flow (Luồng Phiên Tập Trung):**
```
User click "Start Focus" trên Schedule
    ↓
POST /api/focus/start { schedule_id, duration }
    ↓
authMiddleware + FocusController.startSession()
    ↓
FocusService.startSession()
├─ Find Schedule by ID
├─ Check không có focus session active khác
├─ Create FocusSession record:
│  ├─ start_time = NOW
│  ├─ target_duration = user input (e.g., 25 min)
│  ├─ status = "ACTIVE"
│  └─ focus_mode = "POMODORO" | "DEEP_FOCUS"
├─ Save to DB
├─ Broadcast via WebSocket → Frontend (show timer)
└─ Return { sessionId, remainingTime }
    ↓
Frontend nhận, show countdown timer
    ↓
User click "End Session" hoặc timer expires
    ↓
POST /api/focus/end/:sessionId
    ↓
FocusService.endSession()
├─ Find FocusSession
├─ Calculate actual_duration = end_time - start_time
├─ Calculate focus_score = (actual_duration / target_duration) * 100
├─ Update status = "COMPLETED"
├─ Call GamificationService.checkAchievements(userId)
│  ├─ Check if earned new badges
│  │  ├─ "First Focus" → completed 1 session
│  │  ├─ "Focus Master" → 50+ total hours
│  │  ├─ "Week Warrior" → focused all 7 days
│  │  └─ ... (10+ badges total)
│  ├─ Update user XP: user.xp += focus_score
│  ├─ Check rank upgrade:
│  │  ├─ Bronze (0-100 XP)
│  │  ├─ Silver (100-500 XP)
│  │  ├─ Gold (500-1000 XP)
│  │  └─ Platinum (1000+ XP)
│  └─ Call NotificationService.notify()
│     └─ Send in-app notification: "Earned badge: Focus Master!"
│
├─ Broadcast WebSocket → all connected users (if leaderboard enabled)
└─ Return { focus_score, badges_earned, new_rank }
    ↓
Frontend show reward popup
```

**3️⃣ Real-time Collaboration Flow (Luồng Cộng Tác Thời Gian Thực):**
```
User A share Schedule với User B
    ↓
POST /api/collaboration/share { schedule_id, shared_with_ids }
    ↓
CollaborationService.shareSchedule()
├─ Create ShareLog record
├─ Set permission level (VIEW | EDIT | MANAGE)
├─ Call NotificationService.notify(User B)
│  ├─ Create Notification record
│  ├─ Send FCM push (if User B offline)
│  └─ WebSocket broadcast (if User B online)
└─ Return { sharedWith, permissions }
    ↓
User B login / receive notification
    ↓
User B access shared Schedule
├─ GET /api/collaboration/shared-with-me
├─ Query ShareLog where user_id = User B
└─ Return [ { schedule, sharedBy, permission } ]
    ↓
User A edit Schedule title
    ↓
PUT /api/schedule/:id { title: "New Title" }
    ↓
ScheduleService.updateSchedule()
├─ Update DB record
├─ Broadcast WebSocket event: "schedule:updated"
│  └─ Send to all users who have access (User B included)
└─ Return updated schedule
    ↓
User B UI auto update (NO REFRESH NEEDED)
├─ Receive WebSocket message
├─ Update state/UI immediately
└─ Show "Schedule updated by User A"
```

**Middleware Execution Order:**
```
Request In:
    ↓
1. Rate Limiter (100 req/min limit)
    ↓
2. CORS Middleware (check origin)
    ↓
3. Request ID Middleware (add unique ID for tracking)
    ↓
4. authMiddleware (if protected endpoint)
    ├─ Extract JWT from Authorization header
    ├─ Verify signature & expiry
    └─ Attach user info to context
    ↓
5. Controller Handler (process business logic)
    ↓
6. Response Formatter (successResponse / errorResponse)
    ↓
Response Out
```

---

## **Version 3: High-Level Architecture (1 phút)**

Nếu thầy hỏi nhanh:

"Em xin trình bày kiến trúc: **Client ↔ API ↔ Business Logic ↔ Database**

**Cụ thể:**
- Frontend gửi request đến API endpoint
- Middleware check authentication & security
- Controller validate input
- Service xử lý business logic (ví dụ: tính focus score, trao badge)
- Repository lưu/lấy data từ PostgreSQL
- Return response JSON

**Key features:**
- JWT authentication (secure, stateless)
- WebSocket cho real-time collaboration
- Gamification (badge, rank, XP)
- Notifications (FCM + In-app)
- Analytics & reports

Kiến trúc này tuân theo **Clean Architecture** pattern, giúp code dễ maintain, test, và extend."

---

## **Version 4: Khi Thầy Hỏi Cụ Thể**

### ❓ "Xác thực người dùng như thế nào?"
**Trả lời:** "Em sử dụng JWT (JSON Web Token). Khi user login, server generate 2 tokens:
- **accessToken** (15 phút): dùng cho các API request
- **refreshToken** (7 ngày): dùng để lấy accessToken mới khi hết hạn

Client lưu tokens và gửi kèm mỗi request. Server verify signature + expiry time. Nếu hết hạn, client tự động refresh token mà không cần user đăng nhập lại."

### ❓ "Database như thế nào?"
**Trả lời:** "Em dùng PostgreSQL + TypeORM ORM. TypeORM giúp:
1. Define entities (ví dụ: User, Schedule, FocusSession)
2. Auto generate SQL queries
3. Handle relationships (User 1:Many Schedule)
4. Type-safe (TypeScript)

Database có ~15 tables: User, Schedule, FocusSession, Notification, Badge, v.v. Các bảng liên kết qua foreign keys."

### ❓ "Real-time như thế nào?"
**Trả lời:** "Em dùng WebSocket:
1. Client connect: `ws://server:3000`
2. Server verify JWT token
3. Subscribe to events (ví dụ: 'schedule:updated')
4. Khi có update, server broadcast to all connected clients
5. Client nhận event, update UI instantly (không cần refresh)

Ngoài ra, em còn dùng Firebase Cloud Messaging (FCM) để gửi push notification lên mobile devices."

### ❓ "Làm sao handle errors?"
**Trả lời:** "Em implement error handling ở từng layer:
1. **Controller**: validate input, catch errors
2. **Service**: check business rules, throw AppError
3. **Global handler**: catch all errors, return formatted response

Response format:
```json
{
  "success": false,
  "message": "Email already exists",
  "code": "EMAIL_EXISTS",
  "status": 409,
  "requestId": "req-12345"
}
```

RequestId giúp tracking log khi troubleshoot."

### ❓ "Bảo mật như thế nào?"
**Trả lời:** "Em implement multi-layer security:
1. **Password**: Hash bằng bcryptjs (salt rounds = 10)
2. **Authentication**: JWT token signing (secret key)
3. **API Key**: Optional API key authentication
4. **Rate limiting**: Max 100 requests/minute
5. **CORS**: Whitelist allowed origins
6. **Security headers**: CSP, X-Frame-Options, X-Content-Type-Options
7. **Input validation**: DTO validation before processing
8. **Authorization**: Check user ownership before update/delete"

---

## **Version 5: Demo Liveflow (Tương Tác)**

Nếu có thời gian, em có thể demo:

```bash
# 1. Đăng ký user
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Pass123!","full_name":"John Doe"}'

# Response: { user, token }

# 2. Tạo Schedule (với token)
curl -X POST http://localhost:3000/api/schedule/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Study Math","start_time":"2026-05-21T09:00:00Z","duration":60}'

# 3. Start focus session
curl -X POST http://localhost:3000/api/focus/start/schedule-id \
  -H "Authorization: Bearer <token>"

# 4. Get focus stats
curl -X GET http://localhost:3000/api/focus/stats \
  -H "Authorization: Bearer <token>"

# Response: { totalHours, avgScore, trends }
```

---

## **Tips Khi Trình Bày**

✅ **DO:**
- Vẽ diagram hoặc flow chart (request → response)
- Dùng ví dụ cụ thể (login, focus session)
- Nhấn mạnh design patterns (MVC, Repository, Service)
- Giải thích tại sao chọn công nghệ này (JWT vs Session, PostgreSQL vs MongoDB)
- Nói về trade-offs (real-time WebSocket vs polling)

❌ **DON'T:**
- Đọc code line by line (chán!)
- Dùng thuật ngữ quá phức tạp mà không giải thích
- Quên nói về tính năng/requirements
- Chỉ nói tech stack mà không nói architecture

✨ **Cách kết thúc ấn tượng:**
"Kiến trúc này tuân theo **Clean Architecture + Repository Pattern**, giúp code:
- **Maintainable**: dễ sửa, dễ thêm feature
- **Testable**: từng layer độc lập
- **Scalable**: easy to extend
- **Type-safe**: TypeScript giảm bugs

Đây là best practice trong industry."

