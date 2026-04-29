# 🏗️ BFY Backend - Architecture & Code Flow

## 📐 Overall Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT (Mobile/Web)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP Request
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    ELYSIA SERVER (Port 3000)                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         MIDDLEWARE LAYER (Auth, CORS, etc)            │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│          ROUTES LAYER (/api/auth, /api/schedule, etc)       │
│  • Defines HTTP endpoints                                   │
│  • Receives request parameters (body, params, query)        │
│  • Delegates to Controllers                                 │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               CONTROLLERS LAYER                              │
│  • Validates input data                                     │
│  • Checks authorization (if needed)                         │
│  • Calls Service methods                                    │
│  • Handles errors & formats HTTP responses                  │
│  • Returns Response with correct status codes               │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               SERVICES LAYER                                │
│  • Pure business logic                                      │
│  • Database operations via TypeORM                          │
│  • Throws AppError for error cases                          │
│  • Returns business domain objects                          │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│               MODELS LAYER (TypeORM)                        │
│  • Entity definitions with @Entity, @Column decorators      │
│  • Relationships: @ManyToOne, @OneToMany, etc              │
│  • Database constraints                                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL DATABASE                            │
│  • Persistent data storage                                  │
│  • Tables: users, schedules, focus_sessions, etc           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔄 Request/Response Flow - Step by Step

### **Example: User Login**

```
1. CLIENT SENDS REQUEST
   ┌─────────────────────────────────────────┐
   │ POST /api/auth/login                    │
   │ Content-Type: application/json          │
   │ {                                       │
   │   "email": "john@example.com",          │
   │   "password": "MyPassword123"           │
   │ }                                       │
   └─────────────────────────────────────────┘
                    ▼

2. MIDDLEWARE PROCESSING
   ┌─────────────────────────────────────────┐
   │ CORS Check       → ✅ Pass              │
   │ Body Parsing     → ✅ Parse JSON        │
   └─────────────────────────────────────────┘
                    ▼

3. ROUTES LAYER (src/routes/auth.ts)
   ┌──────────────────────────────────────────────────┐
   │ .post("/login",                                  │
   │   async ({ body }) => {                          │
   │     return authController.login(body);  ◄── DELEGATE
   │   }                                              │
   │ )                                                │
   └──────────────────────────────────────────────────┘
                    ▼

4. CONTROLLERS LAYER (src/controllers/AuthController.ts)
   ┌──────────────────────────────────────────────────────────┐
   │ async login(body) {                                      │
   │   1. VALIDATE INPUT                                      │
   │      if (!email || !password)                            │
   │        return errorResponse(400, "Missing fields")       │
   │                                                          │
   │   2. CALL SERVICE                                        │
   │      const result = await authService.login(email, pwd) │
   │                                                          │
   │   3. ERROR HANDLING                                      │
   │      if (error instanceof AppError)                      │
   │        return errorResponse(error.status, ...)          │
   │                                                          │
   │   4. FORMAT RESPONSE                                     │
   │      return new Response(JSON.stringify({               │
   │        status: 200,                                      │
   │        success: true,                                    │
   │        data: result                                      │
   │      }), { status: 200 })                               │
   │ }                                                        │
   └──────────────────────────────────────────────────────────┘
                    ▼

5. SERVICES LAYER (src/services/AuthService.ts)
   ┌──────────────────────────────────────────────────────────┐
   │ async login(email, password) {                           │
   │   1. GET USER FROM DATABASE                              │
   │      const user = await userRepository.findOne({         │
   │        where: { email: email.toLowerCase() }             │
   │      })                                                  │
   │                                                          │
   │   2. VALIDATE PASSWORD                                   │
   │      if (!user || !comparePassword(password, hash))      │
   │        throw new AppError(401, "Invalid credentials")    │
   │                                                          │
   │   3. GENERATE TOKEN                                      │
   │      const token = generateJWT(user.id)                  │
   │                                                          │
   │   4. RETURN BUSINESS OBJECT                              │
   │      return { user, token }  ◄── NOT HTTP RESPONSE      │
   │ }                                                        │
   └──────────────────────────────────────────────────────────┘
                    ▼

6. MODELS LAYER (src/models/User.ts)
   ┌──────────────────────────────────────────────────────────┐
   │ @Entity("users")                                         │
   │ export class User {                                      │
   │   @PrimaryColumn("uuid")                                 │
   │   id: string;                                            │
   │                                                          │
   │   @Column({ type: "varchar", unique: true })            │
   │   email: string;                                         │
   │                                                          │
   │   @Column({ type: "varchar" })                           │
   │   password_hash: string;                                 │
   │   ...                                                    │
   │ }                                                        │
   └──────────────────────────────────────────────────────────┘
                    ▼

7. DATABASE QUERY
   ┌──────────────────────────────────────────┐
   │ SELECT * FROM users WHERE email = ?      │
   │ → Returns: { id, email, password_hash }  │
   └──────────────────────────────────────────┘
                    ▼

8. RESPONSE FLOW (Back Up)
   ┌────────────────────────────────────┐
   │ Service returns { user, token }    │
   │          ▼                         │
   │ Controller wraps in Response       │
   │          ▼                         │
   │ Route passes to client             │
   │          ▼                         │
   │ CLIENT RECEIVES                    │
   │ {                                  │
   │   "status": 200,                   │
   │   "success": true,                 │
   │   "message": "Login successful",   │
   │   "data": {                        │
   │     "user": { ... },               │
   │     "token": "eyJhbGci..."         │
   │   }                                │
   │ }                                  │
   └────────────────────────────────────┘
```

---

## 📂 Directory Structure & Responsibilities

```
src/
├── index.ts
│   └─ Main server entry point, imports all routes
│
├── bootstrap.ts
│   └─ Entry file that imports index.ts
│
├── routes/                    ◄── HTTP ENDPOINT DEFINITIONS
│   ├── auth.ts               (9 endpoints: register, login, profile, etc)
│   ├── schedule.ts           (14 endpoints: create, update, filter, etc)
│   ├── focus.ts              (9 endpoints: sessions, stats, etc)
│   ├── collaboration.ts      (15 endpoints: groups, sharing, etc)
│   ├── users.ts              (7 endpoints: search, profile, follow, etc)
│   ├── gamification.ts       (6 endpoints: ranks, badges, etc)
│   ├── notifications.ts      (15 endpoints: FCM, preferences, etc)
│   ├── reports.ts            (7 endpoints: productivity, analysis, etc)
│   ├── settings.ts           (10 endpoints: user settings, privacy, etc)
│   └── admin.ts              (7 endpoints: user management, logs, etc)
│
├── controllers/              ◄── HTTP LOGIC & VALIDATION
│   ├── AuthController.ts     (Handle auth requests)
│   ├── ScheduleController.ts (Handle schedule requests)
│   ├── FocusController.ts    (Handle focus session requests)
│   ├── CollaborationController.ts
│   ├── UserController.ts
│   ├── GamificationController.ts
│   ├── NotificationController.ts
│   ├── ReportController.ts
│   ├── SettingsController.ts
│   ├── AdminController.ts
│   └── index.ts              (Export all controllers)
│
├── services/                 ◄── BUSINESS LOGIC & DB OPERATIONS
│   ├── AuthService.ts        (Passwordhashing, token generation)
│   ├── ScheduleService.ts    (Schedule CRUD & filtering)
│   ├── FocusService.ts       (Focus session tracking)
│   ├── CollaborationService.ts
│   ├── UserService.ts
│   ├── GamificationService.ts
│   ├── NotificationService.ts
│   ├── ReportService.ts
│   ├── SettingsService.ts
│   ├── AdminService.ts
│   └── DashboardService.ts
│
├── models/                   ◄── DATABASE ENTITY DEFINITIONS
│   ├── User.ts               (@Entity, relationships)
│   ├── Schedule.ts
│   ├── FocusSession.ts
│   ├── Category.ts
│   ├── Group.ts
│   ├── GroupMember.ts
│   ├── Reminder.ts
│   ├── ScheduleAssignment.ts
│   ├── UserSettings.ts
│   └── User.schema.ts        (Index definitions)
│
├── middleware/               ◄── REQUEST PROCESSING
│   ├── auth.ts               (JWT verification, user context)
│   └── cors.ts               (CORS headers)
│
├── config/                   ◄── CONFIGURATION
│   ├── database.ts           (TypeORM setup)
│   └── env.ts                (Environment variables)
│
└── utils/                    ◄── HELPER FUNCTIONS
    ├── errors.ts             (AppError class, error responses)
    ├── jwt.ts                (JWT generation, verification)
    ├── password.ts           (Bcrypt hashing, comparison)
    └── validation.ts         (Email, UUID, color validation)
```

---

## 🔀 Request Processing - Detail View

### **Routes Layer**
```typescript
// ❌ WRONG - Old way (REMOVED)
.post("/login", async ({ body }) => {
  try {
    // Service call mixed with HTTP logic
    const result = await authService.login(body.email, body.password);
    // Response formatting
    return new Response(JSON.stringify({...}), { status: 200 });
  } catch (error) {
    return errorResponse(500, error.message);
  }
})

// ✅ CORRECT - New way (CURRENT)
.post("/login", 
  async ({ body }) => authController.login(body),
  { tags: ["Auth"] }
)
```
**Role:** Only defines HTTP endpoint and delegates to controller

---

### **Controllers Layer**
```typescript
async login(body: any) {
  // 1️⃣ VALIDATE INPUT
  const { email, password } = body;
  if (!email || !password) {
    return errorResponse(400, "Email and password required", "MISSING_FIELDS");
  }

  try {
    // 2️⃣ CALL SERVICE (business logic)
    const result = await authService.login(email, password);

    // 3️⃣ FORMAT HTTP RESPONSE
    return new Response(
      JSON.stringify({
        status: 200,
        success: true,
        message: "Login successful",
        data: result,
      }),
      { status: 200, headers: { "Content-Type": "application/json" } }
    );
  } catch (error) {
    // 4️⃣ HANDLE ERRORS
    if (error instanceof AppError) {
      return errorResponse(error.status, error.message, error.code);
    }
    return errorResponse(500, "Internal server error");
  }
}
```
**Role:** Validates input, calls service, handles errors, formats response

---

### **Services Layer**
```typescript
async login(email: string, password: string): Promise<any> {
  const userRepository = AppDataSource.getRepository("User");
  
  // 1️⃣ DATABASE OPERATION
  const user = await userRepository.findOne({ 
    where: { email: email.toLowerCase() } 
  });
  
  // 2️⃣ BUSINESS LOGIC
  if (!user || !comparePassword(password, user.password_hash)) {
    // Throw error - let controller handle HTTP response
    throw new AppError(401, "Invalid credentials", "INVALID_CREDENTIALS");
  }
  
  // 3️⃣ GENERATE TOKEN (business operation)
  const token = await this.generateToken(user.id);
  
  // 4️⃣ RETURN BUSINESS OBJECT (NOT HTTP response)
  return { user, token };
}
```
**Role:** Pure business logic, database operations, throw AppError

---

### **Models Layer**
```typescript
@Entity("users")
export class User {
  @PrimaryColumn("uuid")
  id: string;

  @Column({ type: "varchar", unique: true })
  email: string;

  @Column({ type: "varchar" })
  password_hash: string;

  @CreateDateColumn({ type: "timestamp" })
  created_at: Date;

  // Relationships
  @OneToOne(() => UserSettings, settings => settings.user)
  settings: UserSettings;

  @OneToMany(() => Schedule, schedule => schedule.creator)
  schedules: Schedule[];
}
```
**Role:** Entity definition with TypeORM decorators

---

## 💾 How Data Flows Through Layers

```
┌─────────────────────────────────────────────────────────────┐
│ CLIENT REQUEST                                              │
│ POST /api/schedule {title: "Buy milk", type: "TODO"}        │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ ROUTE HANDLER (routes/schedule.ts)                          │
│ Receives: body = {title: "Buy milk", type: "TODO"}          │
│ Action: scheduleController.createSchedule(body)             │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ CONTROLLER (controllers/ScheduleController.ts)              │
│ 1. Validate: title exists? ✅ type valid? ✅                │
│ 2. Call: scheduleService.createSchedule(body)               │
│ 3. Receive: Schedule object with id, title, type, etc       │
│ 4. Format: { status: 201, success: true, data: {...} }      │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ SERVICE (services/ScheduleService.ts)                       │
│ 1. Generate UUID for schedule                               │
│ 2. Validate business rules (type specific validation)       │
│ 3. Create entity: scheduleRepository.create({...})          │
│ 4. Save to DB: await scheduleRepository.save(schedule)      │
│ 5. Return: Schedule object (domain model)                   │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ MODEL/TypeORM (models/Schedule.ts)                          │
│ Executes: INSERT INTO schedules VALUES (...)                │
│ Returns: Persisted schedule with all fields populated       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ DATABASE (PostgreSQL)                                       │
│ Table: schedules                                            │
│ New row: id|creator_id|title|type|created_at|...           │
└────────────────────────┬────────────────────────────────────┘
                         │
      (Flow reverses - Response travels back up)
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│ CLIENT RECEIVES                                             │
│ {                                                           │
│   "status": 201,                                            │
│   "success": true,                                          │
│   "message": "Schedule created",                            │
│   "data": {                                                 │
│     "id": "550e8400-e29b-41d4-a716-446655440000",          │
│     "title": "Buy milk",                                    │
│     "type": "TODO",                                         │
│     "created_at": "2026-04-21T10:30:00Z"                   │
│   }                                                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Error Handling Flow

```
┌─────────────────────────────────────────────────────┐
│ SERVICE DETECTS ERROR                               │
│ throw new AppError(                                 │
│   400,                    ◄── HTTP status code      │
│   "Invalid deadline",     ◄── Error message         │
│   "INVALID_DEADLINE"      ◄── Error code            │
│ )                                                   │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│ CONTROLLER CATCHES ERROR                            │
│ catch (error) {                                     │
│   if (error instanceof AppError) {                  │
│     return errorResponse(                           │
│       error.status,      ◄── 400                    │
│       error.message,     ◄── "Invalid deadline"     │
│       error.code         ◄── "INVALID_DEADLINE"     │
│     )                                               │
│   }                                                 │
│ }                                                   │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│ CLIENT RECEIVES ERROR RESPONSE                      │
│ {                                                   │
│   "status": 400,                                    │
│   "success": false,                                 │
│   "error": {                                        │
│     "message": "Invalid deadline",                  │
│     "code": "INVALID_DEADLINE"                      │
│   }                                                 │
│ }                                                   │
└─────────────────────────────────────────────────────┘
```

---

## � Code Execution Trace - Detailed Line-by-Line

### **Real Example: Create Schedule (POST /api/schedule)**

#### **STEP 1: CLIENT SENDS REQUEST**
```bash
POST http://localhost:3000/api/schedule
Content-Type: application/json

{
  "creator_id": "user-123",
  "title": "Buy milk",
  "type": "TODO"
}
```

---

#### **STEP 2: ELYSIA SERVER RECEIVES REQUEST**
```typescript
// File: src/index.ts
const app = new Elysia()
  .group("/api", (app) =>
    app.use(scheduleRoutes)  // ← Route is registered here
  )
```

Elysia parses the request:
- ✅ Method: `POST`
- ✅ URL: `/api/schedule`
- ✅ Headers: `Content-Type: application/json`
- ✅ Body: `{creator_id, title, type}`

---

#### **STEP 3: ROUTE HANDLER EXECUTION**
```typescript
// File: src/routes/schedule.ts

export const scheduleRoutes = new Elysia({ prefix: "/schedule" })
  .post(
    "/:type",  // ← Route definition
    async ({ params, body }: { params: any; body: any }) => {
      // Step 3a: Extract params and body
      console.log("📍 Route Handler Called");
      console.log("  params.type:", params.type);  // "TODO"
      console.log("  body:", body);  // {creator_id, title, type}

      // Step 3b: Delegate to controller
      return scheduleController.createSchedule({ 
        ...body, 
        type: params.type 
      });  // ← PASS TO CONTROLLER
    },
    { tags: ["Schedule"] }
  );

// What gets passed to controller:
// {
//   creator_id: "user-123",
//   title: "Buy milk",
//   type: "TODO"
// }
```

**Summary of Step 3:**
- ✅ Route matched: `POST /api/schedule/:type`
- ✅ Body extracted from request
- ✅ Delegated to `scheduleController.createSchedule()`

---

#### **STEP 4: CONTROLLER INPUT VALIDATION**
```typescript
// File: src/controllers/ScheduleController.ts

export class ScheduleController {
  async createSchedule(body: any) {
    // Step 4a: Destructure input
    console.log("🎮 Controller Execution");
    const type = body.type || "TODO";
    const { creator_id, title, ...rest } = body;

    console.log("  Input received:", { creator_id, title, type });

    // Step 4b: VALIDATION - Check required fields
    if (!creator_id || !title) {
      console.log("  ❌ Validation failed: Missing creator_id or title");
      return errorResponse(
        400, 
        "creator_id and title required", 
        "MISSING_FIELDS"
      );
      // Returns immediately: {status: 400, success: false, error: {...}}
    }

    console.log("  ✅ Validation passed");

    // Step 4c: Try to execute business logic
    try {
      // Call service
      console.log("  📞 Calling scheduleService.createSchedule()");
      const schedule = await scheduleService.createSchedule({
        ...rest,
        creator_id,
        title,
        type,
      });

      console.log("  ✅ Service returned:", schedule);

      // Step 4d: Format and return HTTP response
      const response = new Response(
        JSON.stringify({
          status: 201,  // Created status
          success: true,
          message: "Schedule created successfully",
          data: schedule,  // ← Include the created schedule
        }),
        { 
          status: 201, 
          headers: { "Content-Type": "application/json" } 
        }
      );

      console.log("  📤 Sending response with status 201");
      return response;
    } catch (error) {
      // Step 4e: Error handling
      console.log("  ❌ Error caught:", error.message);

      if (error instanceof AppError) {
        console.log("  → AppError detected, returning error response");
        return errorResponse(error.status, error.message, error.code);
        // Example: {status: 400, success: false, error: {message: "...", code: "..."}}
      }

      // Fallback error
      return errorResponse(500, error instanceof Error ? error.message : "Internal server error");
    }
  }
}
```

**Summary of Step 4:**
- ✅ Input validated (creator_id and title required)
- ✅ If validation fails → return error (400)
- ✅ If valid → call service
- ✅ Try-catch for error handling

---

#### **STEP 5: SERVICE BUSINESS LOGIC**
```typescript
// File: src/services/ScheduleService.ts

export class ScheduleService {
  async createSchedule(data: any): Promise<any> {
    console.log("⚙️ Service Execution");
    console.log("  Input data:", data);

    // Step 5a: Get TypeORM repository
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    console.log("  📦 Repository obtained");

    // Step 5b: Business logic - Validate based on type
    if (data.type === "TODO" && !data.title) {
      console.log("  ❌ Business validation failed: TODO requires title");
      throw new AppError(400, "Title is required", "MISSING_TITLE");
      // ← Service throws error, controller will catch it
    }

    if (data.type === "TASK" && !data.deadline) {
      console.log("  ❌ Business validation failed: TASK requires deadline");
      throw new AppError(400, "Deadline is required for TASK", "MISSING_DEADLINE");
    }

    if (data.type === "EVENT") {
      if (!data.start_date || !data.end_date) {
        throw new AppError(400, "Start and end date required", "MISSING_TIME");
      }
      if (data.end_date <= data.start_date) {
        throw new AppError(400, "End date must be after start date", "INVALID_TIME");
      }
    }

    console.log("  ✅ Business validation passed");

    // Step 5c: Generate UUID
    const scheduleId = generateUUID();
    console.log("  🔑 Generated UUID:", scheduleId);

    // Step 5d: Create entity object (NOT saved yet)
    const schedule = scheduleRepository.create({
      id: scheduleId,
      ...data,
      created_at: new Date(),
      is_archived: false,
    });

    console.log("  📝 Entity created (not saved yet):", {
      id: schedule.id,
      title: schedule.title,
      type: schedule.type,
      created_at: schedule.created_at,
    });

    // Step 5e: Save to database
    console.log("  💾 Saving to database...");
    const savedSchedule = await scheduleRepository.save(schedule);
    // ← This executes: INSERT INTO schedules VALUES (...)
    console.log("  ✅ Saved to database");

    // Step 5f: Return business object (NOT HTTP response)
    console.log("  📦 Returning business object to controller");
    return savedSchedule;
    // Returns: {id, creator_id, title, type, created_at, ...}
  }
}
```

**Summary of Step 5:**
- ✅ Get repository
- ✅ Validate business rules (type-specific)
- ✅ Generate UUID
- ✅ Create entity
- ✅ Save to database
- ✅ Return business object (NOT HTTP response)

---

#### **STEP 6: DATABASE OPERATION**
```typescript
// TypeORM SQL Query
INSERT INTO schedules (
  id, 
  creator_id, 
  title, 
  type, 
  created_at, 
  is_archived
) VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  'user-123',
  'Buy milk',
  'TODO',
  '2026-04-21 10:30:00',
  false
);

// Database returns:
{
  id: '550e8400-e29b-41d4-a716-446655440000',
  creator_id: 'user-123',
  title: 'Buy milk',
  type: 'TODO',
  created_at: 2026-04-21T10:30:00.000Z,
  is_archived: false,
  updated_at: 2026-04-21T10:30:00.000Z
}
```

---

#### **STEP 7: RESPONSE FLOW BACK UP**

```
SERVICE returns business object
  ↓
CONTROLLER receives: {id, creator_id, title, type, created_at, ...}
  ↓
CONTROLLER wraps in HTTP response
  ↓
JSON.stringify converts to JSON string
  ↓
new Response() creates HTTP response
  ↓
Elysia sends to client
```

---

#### **STEP 8: CLIENT RECEIVES HTTP RESPONSE**
```bash
HTTP/1.1 201 Created
Content-Type: application/json

{
  "status": 201,
  "success": true,
  "message": "Schedule created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "creator_id": "user-123",
    "title": "Buy milk",
    "type": "TODO",
    "created_at": "2026-04-21T10:30:00.000Z"
  }
}
```

**Status Code Breakdown:**
- `201` = Created (resource successfully created)
- `success: true` = Operation successful
- `data` = The created schedule object

---

### **EXAMPLE 2: Error Scenario - Invalid Input**

#### **CLIENT SENDS REQUEST WITH MISSING TITLE**
```bash
POST http://localhost:3000/api/schedule
Content-Type: application/json

{
  "creator_id": "user-123",
  "type": "TODO"
  // ❌ MISSING title!
}
```

---

#### **STEP 1-3: Same as before → Route → Controller**

#### **STEP 4: CONTROLLER VALIDATION FAILS**
```typescript
// src/controllers/ScheduleController.ts

async createSchedule(body: any) {
  const { creator_id, title, ...rest } = body;
  // title = undefined

  if (!creator_id || !title) {
    // ❌ Validation fails here!
    console.log("❌ Validation failed: title is missing");
    
    return errorResponse(
      400,                      // HTTP status
      "creator_id and title required",  // Message
      "MISSING_FIELDS"          // Error code
    );
  }
  // ← Returns immediately without calling service
}
```

#### **CLIENT RECEIVES ERROR RESPONSE**
```bash
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "status": 400,
  "success": false,
  "error": {
    "message": "creator_id and title required",
    "code": "MISSING_FIELDS"
  }
}
```

**✅ Validation caught error BEFORE database operation!**

---

### **EXAMPLE 3: Database Error Scenario**

#### **CLIENT SENDS DUPLICATE REQUEST**
```bash
POST http://localhost:3000/api/schedule
{
  "creator_id": "user-123",
  "title": "Buy milk",
  "type": "TODO"
}
```

#### **STEP 4: Controller → Step 5: Service → Database**

```typescript
// Service tries to save
const savedSchedule = await scheduleRepository.save(schedule);

// PostgreSQL throws constraint error if duplicate unique key
// Error: Duplicate key value violates unique constraint
```

#### **STEP 5: SERVICE THROWS APPERROR**
```typescript
catch (databaseError) {
  console.log("❌ Database error:", databaseError.message);
  
  throw new AppError(
    409,                              // Conflict status
    "Schedule already exists",        // User-friendly message
    "DUPLICATE_SCHEDULE"              // Error code
  );
}
```

#### **STEP 4: CONTROLLER CATCHES AND FORMATS ERROR**
```typescript
catch (error) {
  if (error instanceof AppError) {
    return errorResponse(error.status, error.message, error.code);
    // Returns: {status: 409, success: false, error: {...}}
  }
}
```

#### **CLIENT RECEIVES ERROR RESPONSE**
```bash
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "status": 409,
  "success": false,
  "error": {
    "message": "Schedule already exists",
    "code": "DUPLICATE_SCHEDULE"
  }
}
```

---

### **EXAMPLE 4: Authentication Error**

#### **CLIENT SENDS REQUEST WITHOUT AUTH TOKEN**
```bash
POST http://localhost:3000/api/reports/productivity
// ❌ NO Authorization header!
```

#### **STEP 4: CONTROLLER CHECKS AUTH**
```typescript
// src/controllers/ReportController.ts

async getProductivityReport(ctx: AuthContext) {
  // authMiddleware already ran in route
  // Check if user is authenticated
  
  if (!ctx.user) {
    console.log("❌ Not authenticated");
    return errorResponse(401, "Unauthorized", "UNAUTHORIZED");
  }
  
  // ← Returns immediately if no user
}
```

#### **CLIENT RECEIVES 401 ERROR**
```bash
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "status": 401,
  "success": false,
  "error": {
    "message": "Unauthorized",
    "code": "UNAUTHORIZED"
  }
}
```

---

### **CONSOLE OUTPUT EXAMPLE**

If you run the server with logging enabled:

```
📍 Route Handler Called
  params.type: TODO
  body: {creator_id: "user-123", title: "Buy milk", type: "TODO"}

🎮 Controller Execution
  Input received: {creator_id: "user-123", title: "Buy milk", type: "TODO"}
  ✅ Validation passed
  📞 Calling scheduleService.createSchedule()

⚙️ Service Execution
  Input data: {creator_id: "user-123", title: "Buy milk", type: "TODO"}
  📦 Repository obtained
  ✅ Business validation passed
  🔑 Generated UUID: 550e8400-e29b-41d4-a716-446655440000
  📝 Entity created (not saved yet)
  💾 Saving to database...
  ✅ Saved to database
  📦 Returning business object to controller

🎮 Controller Execution (continued)
  ✅ Service returned: {id: "550e8400...", creator_id: "user-123", ...}
  📤 Sending response with status 201

✅ SUCCESS - Schedule created with ID: 550e8400-e29b-41d4-a716-446655440000
```

---

### **MEMORY STATE AT EACH STEP**

```
CLIENT REQUEST:
  method: "POST"
  url: "/api/schedule"
  body: {creator_id, title, type}
  headers: {Content-Type: application/json}

       ↓

ROUTE HANDLER:
  params: {type: "TODO"}
  body: {creator_id, title, type}

       ↓

CONTROLLER:
  body (input): {creator_id, title, type}
  validation: ✅ passed
  schedule (from service): {id, creator_id, title, type, created_at, ...}

       ↓

SERVICE:
  data (input): {creator_id, title, type}
  scheduleRepository: TypeORM repository
  schedule (entity): {id, creator_id, title, type, ...}
  savedSchedule (from DB): {id, creator_id, title, type, created_at, ...}

       ↓

DATABASE:
  INSERT query executed
  New row added to schedules table
  Entity returned

       ↓

RESPONSE:
  status: 201
  success: true
  data: {id, creator_id, title, type, created_at, ...}

       ↓

CLIENT:
  HTTP 201 with JSON body received
```

---

## �📊 Data Transformation at Each Layer

```
REQUEST: {title: "Meeting", deadline: "2026-04-21", type: "TASK"}
                         │
                         ▼
ROUTE RECEIVES ─────────► Receives raw HTTP body
                         │
                         ▼
CONTROLLER VALIDATES ───► Checks required fields
                         │ Validates types
                         │ Checks authorization
                         ▼
CONTROLLER CALLS SERVICE ──► Passes cleaned data
                         │
                         ▼
SERVICE PROCESSES ──────► Business logic
                         │ Database queries
                         │ Return business object
                         ▼
DATABASE ──────────────► Persists entity
                         │ Returns saved entity
                         ▼
RESPONSE FORMAT ───────► { status, success, data }
                         │
                         ▼
CLIENT RECEIVES ───────► Structured JSON response
```

---

## 🎯 Best Practices Implemented

| Layer | ✅ DO | ❌ DON'T |
|-------|-------|---------|
| **Routes** | Delegate to controller | Call service directly |
| **Routes** | Define endpoints only | Add business logic |
| **Controllers** | Validate input | Let service validate |
| **Controllers** | Handle HTTP responses | Return domain objects |
| **Controllers** | Catch & format errors | Throw HTTP errors |
| **Services** | Database operations | HTTP response formatting |
| **Services** | Business logic | HTTP status codes |
| **Services** | Throw AppError | Return error responses |
| **Models** | Entity definitions | Business logic |
| **Models** | TypeORM decorators | Manual queries |

---

## 🚀 Complete Request Lifecycle

```
1. CLIENT SENDS HTTP REQUEST
   ├─ Method: POST
   ├─ URL: /api/auth/register
   ├─ Headers: Content-Type, Authorization
   └─ Body: {email, password, full_name}

2. MIDDLEWARE PROCESSING
   ├─ CORS Check ✅
   ├─ Body Parser ✅
   ├─ Request Logging ✅
   └─ Context Setup ✅

3. ROUTE MATCHING
   └─ matches /auth routes

4. CONTROLLER EXECUTION
   ├─ Input Validation
   ├─ Authorization Check
   ├─ Service Call
   └─ Response Formatting

5. SERVICE EXECUTION
   ├─ Business Logic
   ├─ Database Query
   ├─ Error Handling
   └─ Return Result

6. DATABASE OPERATION
   ├─ Insert/Update/Query
   ├─ Constraint Checking
   ├─ Transaction Handling
   └─ Return Entity

7. RESPONSE BUILDING
   ├─ Wrap in Response object
   ├─ Set HTTP status
   ├─ Set Headers
   └─ Return JSON

8. CLIENT RECEIVES
   ├─ HTTP Status (200, 201, 400, 401, etc)
   ├─ Headers
   └─ JSON Body {status, success, data/error}
```

---

## 📝 Summary

**The flow ensures:**
- ✅ **Separation of Concerns** - Each layer has specific responsibility
- ✅ **Error Handling** - Consistent error handling across all layers
- ✅ **Testability** - Each layer can be tested independently
- ✅ **Maintainability** - Easy to add new features
- ✅ **Code Reusability** - Services can be reused by multiple controllers
- ✅ **Security** - Validation at multiple layers
- ✅ **Performance** - Efficient database queries via TypeORM
