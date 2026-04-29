# 🏗️ BFY Backend - Kiến Trúc & Luồng Code (Tiếng Việt)

## 🔍 Chi Tiết Luồng Chạy Code - Từng Dòng

### **Ví Dụ Thực Tế 1: Tạo Schedule (POST /api/schedule)**

#### **BƯỚC 1: CLIENT GỬI REQUEST**
```bash
POST http://localhost:3000/api/schedule
Content-Type: application/json

{
  "creator_id": "user-123",
  "title": "Mua sữa",
  "type": "TODO"
}
```

---

#### **BƯỚC 2: ELYSIA SERVER NHẬN REQUEST**
```typescript
// File: src/index.ts
const app = new Elysia()
  .group("/api", (app) =>
    app.use(scheduleRoutes)  // ← Route được đăng ký tại đây
  )
```

Elysia phân tích request:
- ✅ Method: `POST`
- ✅ URL: `/api/schedule`
- ✅ Headers: `Content-Type: application/json`
- ✅ Body: `{creator_id, title, type}`

---

#### **BƯỚC 3: XỬ LÝ ROUTE**
```typescript
// File: src/routes/schedule.ts

export const scheduleRoutes = new Elysia({ prefix: "/schedule" })
  .post(
    "/:type",  // ← Định nghĩa route
    async ({ params, body }: { params: any; body: any }) => {
      // Bước 3a: Trích xuất params và body
      console.log("📍 Route Handler được gọi");
      console.log("  params.type:", params.type);  // "TODO"
      console.log("  body:", body);  // {creator_id, title, type}

      // Bước 3b: Chuyển tiếp đến controller
      return scheduleController.createSchedule({ 
        ...body, 
        type: params.type 
      });  // ← GỬI ĐẾN CONTROLLER
    },
    { tags: ["Schedule"] }
  );

// Dữ liệu được gửi đến controller:
// {
//   creator_id: "user-123",
//   title: "Mua sữa",
//   type: "TODO"
// }
```

**Tóm tắt Bước 3:**
- ✅ Route matched: `POST /api/schedule/:type`
- ✅ Body được trích xuất từ request
- ✅ Chuyển tiếp đến `scheduleController.createSchedule()`

---

#### **BƯỚC 4: CONTROLLER KIỂM TRA INPUT**
```typescript
// File: src/controllers/ScheduleController.ts

export class ScheduleController {
  async createSchedule(body: any) {
    // Bước 4a: Phân rã input
    console.log("🎮 Controller chạy");
    const type = body.type || "TODO";
    const { creator_id, title, ...rest } = body;

    console.log("  Input nhận được:", { creator_id, title, type });

    // Bước 4b: KIỂM TRA - Các trường bắt buộc có không?
    if (!creator_id || !title) {
      console.log("  ❌ Kiểm tra không hợp lệ: Thiếu creator_id hoặc title");
      return errorResponse(
        400, 
        "creator_id và title là bắt buộc", 
        "MISSING_FIELDS"
      );
      // Trả về ngay: {status: 400, success: false, error: {...}}
    }

    console.log("  ✅ Kiểm tra hợp lệ");

    // Bước 4c: Cố gắng thực thi business logic
    try {
      // Gọi service
      console.log("  📞 Gọi scheduleService.createSchedule()");
      const schedule = await scheduleService.createSchedule({
        ...rest,
        creator_id,
        title,
        type,
      });

      console.log("  ✅ Service trả về:", schedule);

      // Bước 4d: Định dạng và trả về HTTP response
      const response = new Response(
        JSON.stringify({
          status: 201,  // Trạng thái được tạo
          success: true,
          message: "Schedule được tạo thành công",
          data: schedule,  // ← Bao gồm schedule vừa tạo
        }),
        { 
          status: 201, 
          headers: { "Content-Type": "application/json" } 
        }
      );

      console.log("  📤 Gửi response với status 201");
      return response;
    } catch (error) {
      // Bước 4e: Xử lý lỗi
      console.log("  ❌ Lỗi được bắt:", error.message);

      if (error instanceof AppError) {
        console.log("  → AppError được phát hiện, trả về error response");
        return errorResponse(error.status, error.message, error.code);
        // Ví dụ: {status: 400, success: false, error: {message: "...", code: "..."}}
      }

      // Lỗi mặc định
      return errorResponse(500, error instanceof Error ? error.message : "Lỗi máy chủ nội bộ");
    }
  }
}
```

**Tóm tắt Bước 4:**
- ✅ Input được kiểm tra (creator_id và title bắt buộc)
- ✅ Nếu kiểm tra không hợp lệ → trả về lỗi (400)
- ✅ Nếu hợp lệ → gọi service
- ✅ Try-catch để xử lý lỗi

---

#### **BƯỚC 5: SERVICE XỬ LÝ BUSINESS LOGIC**
```typescript
// File: src/services/ScheduleService.ts

export class ScheduleService {
  async createSchedule(data: any): Promise<any> {
    console.log("⚙️ Service chạy");
    console.log("  Dữ liệu input:", data);

    // Bước 5a: Lấy TypeORM repository
    const scheduleRepository = AppDataSource.getRepository("Schedule");
    console.log("  📦 Repository được lấy");

    // Bước 5b: Business logic - Kiểm tra dựa trên type
    if (data.type === "TODO" && !data.title) {
      console.log("  ❌ Kiểm tra business không hợp lệ: TODO cần title");
      throw new AppError(400, "Title là bắt buộc", "MISSING_TITLE");
      // ← Service ném lỗi, controller sẽ bắt nó
    }

    if (data.type === "TASK" && !data.deadline) {
      console.log("  ❌ Kiểm tra business không hợp lệ: TASK cần deadline");
      throw new AppError(400, "Deadline là bắt buộc cho TASK", "MISSING_DEADLINE");
    }

    if (data.type === "EVENT") {
      if (!data.start_date || !data.end_date) {
        throw new AppError(400, "Ngày bắt đầu và kết thúc bắt buộc", "MISSING_TIME");
      }
      if (data.end_date <= data.start_date) {
        throw new AppError(400, "Ngày kết thúc phải sau ngày bắt đầu", "INVALID_TIME");
      }
    }

    console.log("  ✅ Kiểm tra business hợp lệ");

    // Bước 5c: Tạo UUID
    const scheduleId = generateUUID();
    console.log("  🔑 UUID được tạo:", scheduleId);

    // Bước 5d: Tạo entity object (CHƯA lưu)
    const schedule = scheduleRepository.create({
      id: scheduleId,
      ...data,
      created_at: new Date(),
      is_archived: false,
    });

    console.log("  📝 Entity được tạo (chưa lưu):", {
      id: schedule.id,
      title: schedule.title,
      type: schedule.type,
      created_at: schedule.created_at,
    });

    // Bước 5e: Lưu vào database
    console.log("  💾 Đang lưu vào database...");
    const savedSchedule = await scheduleRepository.save(schedule);
    // ← Thực thi: INSERT INTO schedules VALUES (...)
    console.log("  ✅ Đã lưu vào database");

    // Bước 5f: Trả về business object (KHÔNG phải HTTP response)
    console.log("  📦 Trả về business object cho controller");
    return savedSchedule;
    // Trả về: {id, creator_id, title, type, created_at, ...}
  }
}
```

**Tóm tắt Bước 5:**
- ✅ Lấy repository
- ✅ Kiểm tra business rules (cụ thể theo type)
- ✅ Tạo UUID
- ✅ Tạo entity
- ✅ Lưu vào database
- ✅ Trả về business object (KHÔNG phải HTTP response)

---

#### **BƯỚC 6: THỰC THI DATABASE**
```sql
-- TypeORM SQL Query
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
  'Mua sữa',
  'TODO',
  '2026-04-21 10:30:00',
  false
);

-- Database trả về:
-- {
--   id: '550e8400-e29b-41d4-a716-446655440000',
--   creator_id: 'user-123',
--   title: 'Mua sữa',
--   type: 'TODO',
--   created_at: 2026-04-21T10:30:00.000Z,
--   is_archived: false,
--   updated_at: 2026-04-21T10:30:00.000Z
-- }
```

---

#### **BƯỚC 7: LUỒNG RESPONSE TRỞ VỀ**

```
SERVICE trả về business object
  ↓
CONTROLLER nhận: {id, creator_id, title, type, created_at, ...}
  ↓
CONTROLLER bọc trong HTTP response
  ↓
JSON.stringify chuyển đổi thành JSON string
  ↓
new Response() tạo HTTP response
  ↓
Elysia gửi đến client
```

---

#### **BƯỚC 8: CLIENT NHẬN HTTP RESPONSE**
```bash
HTTP/1.1 201 Created
Content-Type: application/json

{
  "status": 201,
  "success": true,
  "message": "Schedule được tạo thành công",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "creator_id": "user-123",
    "title": "Mua sữa",
    "type": "TODO",
    "created_at": "2026-04-21T10:30:00.000Z"
  }
}
```

**Giải thích Status Code:**
- `201` = Tạo thành công (resource được tạo thành công)
- `success: true` = Thao tác thành công
- `data` = Đối tượng schedule vừa tạo

---

### **VÍ DỤ 2: Tình Huống Lỗi - Input Không Hợp Lệ**

#### **CLIENT GỬI REQUEST THIẾU TITLE**
```bash
POST http://localhost:3000/api/schedule
Content-Type: application/json

{
  "creator_id": "user-123",
  "type": "TODO"
  # ❌ THIẾU title!
}
```

---

#### **BƯỚC 1-3: Giống như trước → Route → Controller**

#### **BƯỚC 4: CONTROLLER KIỂM TRA KHÔNG HỢP LỆ**
```typescript
// src/controllers/ScheduleController.ts

async createSchedule(body: any) {
  const { creator_id, title, ...rest } = body;
  // title = undefined

  if (!creator_id || !title) {
    // ❌ Kiểm tra không hợp lệ tại đây!
    console.log("❌ Kiểm tra không hợp lệ: title bị thiếu");
    
    return errorResponse(
      400,                      // HTTP status
      "creator_id và title là bắt buộc",  // Thông báo
      "MISSING_FIELDS"          // Error code
    );
  }
  // ← Trả về ngay mà không gọi service
}
```

#### **CLIENT NHẬN ERROR RESPONSE**
```bash
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "status": 400,
  "success": false,
  "error": {
    "message": "creator_id và title là bắt buộc",
    "code": "MISSING_FIELDS"
  }
}
```

**✅ Kiểm tra bắt được lỗi TRƯỚC KHI gọi database!**

---

### **VÍ DỤ 3: Tình Huống Lỗi Database**

#### **CLIENT GỬI REQUEST TRÙNG LẶP**
```bash
POST http://localhost:3000/api/schedule
{
  "creator_id": "user-123",
  "title": "Mua sữa",
  "type": "TODO"
}
# Lưu ý: Schedule "Mua sữa" của user-123 đã tồn tại
```

#### **BƯỚC 4: Controller → BƯỚC 5: Service → Database**

```typescript
// Service cố gắng lưu
const savedSchedule = await scheduleRepository.save(schedule);

// PostgreSQL ném lỗi nếu vi phạm unique constraint
// Lỗi: Duplicate key value violates unique constraint
```

#### **BƯỚC 5: SERVICE NEM APPERROR**
```typescript
catch (databaseError) {
  console.log("❌ Database error:", databaseError.message);
  
  throw new AppError(
    409,                              // Conflict status
    "Schedule đã tồn tại",            // Thông báo thân thiện
    "DUPLICATE_SCHEDULE"              // Error code
  );
}
```

#### **BƯỚC 4: CONTROLLER BẮT VÀ ĐỊNH DẠNG LỖI**
```typescript
catch (error) {
  if (error instanceof AppError) {
    return errorResponse(error.status, error.message, error.code);
    // Trả về: {status: 409, success: false, error: {...}}
  }
}
```

#### **CLIENT NHẬN ERROR RESPONSE**
```bash
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "status": 409,
  "success": false,
  "error": {
    "message": "Schedule đã tồn tại",
    "code": "DUPLICATE_SCHEDULE"
  }
}
```

---

### **VÍ DỤ 4: Lỗi Authentication**

#### **CLIENT GỬI REQUEST KHÔNG CÓ TOKEN**
```bash
POST http://localhost:3000/api/reports/productivity
# ❌ KHÔNG CÓ Authorization header!
```

#### **BƯỚC 4: CONTROLLER KIỂM TRA XÁC THỰC**
```typescript
// src/controllers/ReportController.ts

async getProductivityReport(ctx: AuthContext) {
  // authMiddleware đã chạy rồi trong route
  // Kiểm tra xem user đã xác thực chưa
  
  if (!ctx.user) {
    console.log("❌ Chưa xác thực");
    return errorResponse(401, "Chưa xác thực", "UNAUTHORIZED");
  }
  
  // ← Trả về ngay nếu không có user
}
```

#### **CLIENT NHẬN 401 ERROR**
```bash
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "status": 401,
  "success": false,
  "error": {
    "message": "Chưa xác thực",
    "code": "UNAUTHORIZED"
  }
}
```

---

### **OUTPUT CONSOLE EXAMPLE**

Nếu chạy server có bật logging:

```
📍 Route Handler được gọi
  params.type: TODO
  body: {creator_id: "user-123", title: "Mua sữa", type: "TODO"}

🎮 Controller chạy
  Input nhận được: {creator_id: "user-123", title: "Mua sữa", type: "TODO"}
  ✅ Kiểm tra hợp lệ
  📞 Gọi scheduleService.createSchedule()

⚙️ Service chạy
  Dữ liệu input: {creator_id: "user-123", title: "Mua sữa", type: "TODO"}
  📦 Repository được lấy
  ✅ Kiểm tra business hợp lệ
  🔑 UUID được tạo: 550e8400-e29b-41d4-a716-446655440000
  📝 Entity được tạo (chưa lưu)
  💾 Đang lưu vào database...
  ✅ Đã lưu vào database
  📦 Trả về business object cho controller

🎮 Controller chạy (tiếp theo)
  ✅ Service trả về: {id: "550e8400...", creator_id: "user-123", ...}
  📤 Gửi response với status 201

✅ THÀNH CÔNG - Schedule được tạo với ID: 550e8400-e29b-41d4-a716-446655440000
```

---

### **TRẠNG THÁI MEMORY TẠI MỖI BƯỚC**

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
  validation: ✅ hợp lệ
  schedule (từ service): {id, creator_id, title, type, created_at, ...}

       ↓

SERVICE:
  data (input): {creator_id, title, type}
  scheduleRepository: TypeORM repository
  schedule (entity): {id, creator_id, title, type, ...}
  savedSchedule (từ DB): {id, creator_id, title, type, created_at, ...}

       ↓

DATABASE:
  INSERT query được thực thi
  Dòng mới được thêm vào bảng schedules
  Entity được trả về

       ↓

RESPONSE:
  status: 201
  success: true
  data: {id, creator_id, title, type, created_at, ...}

       ↓

CLIENT:
  HTTP 201 với JSON body được nhận
```

---

### **LUỒNG GỌI (CALL STACK)**

```
1. CLIENT.request()
   ↓
2. ELYSIA.listen()
   ↓
3. MIDDLEWARE.cors()
   ↓
4. ROUTE.post("/schedule")
   ↓
5. CONTROLLER.createSchedule(body)
   ├─ VALIDATE input
   ├─ SERVICE.createSchedule(data)
   │  ├─ VALIDATE business rules
   │  ├─ REPOSITORY.create(data)
   │  ├─ DATABASE.save(entity)
   │  └─ RETURN savedSchedule
   ├─ FORMAT response
   └─ RETURN Response
   ↓
6. ELYSIA.send(response)
   ↓
7. CLIENT.receive(response)
```

---

## 📊 Dữ Liệu Biến Đổi Tại Mỗi Tầng

```
REQUEST: {title: "Meeting", deadline: "2026-04-21", type: "TASK"}
                         │
                         ▼
ROUTE NHẬN ────────────► Nhận raw HTTP body
                         │
                         ▼
CONTROLLER KIỂM TRA ────► Kiểm tra các trường bắt buộc
                         │ Kiểm tra kiểu dữ liệu
                         │ Kiểm tra quyền truy cập
                         ▼
CONTROLLER GỌI SERVICE ──► Gửi dữ liệu đã làm sạch
                         │
                         ▼
SERVICE XỬ LÝ ────────► Business logic
                         │ Truy vấn database
                         │ Trả về business object
                         ▼
DATABASE ─────────────► Lưu trữ dữ liệu
                         │ Trả về entity
                         ▼
ĐỊNH DẠNG RESPONSE ───► { status, success, data }
                         │
                         ▼
CLIENT NHẬN ────────────► Structured JSON response
```

---

## 🎯 Best Practices Implemented

| Tầng | ✅ LÀM | ❌ KHÔNG LÀM |
|-------|-------|---------|
| **Routes** | Chuyển tiếp đến controller | Gọi service trực tiếp |
| **Routes** | Định nghĩa endpoints | Thêm business logic |
| **Controllers** | Kiểm tra input | Để service kiểm tra |
| **Controllers** | Xử lý HTTP responses | Trả về domain objects |
| **Controllers** | Bắt & định dạng lỗi | Ném HTTP errors |
| **Services** | Thao tác database | Định dạng HTTP response |
| **Services** | Business logic | HTTP status codes |
| **Services** | Ném AppError | Trả về error responses |
| **Models** | Định nghĩa entity | Business logic |
| **Models** | TypeORM decorators | Manual queries |

---

## 📝 Tóm Tắt

**Luồng đảm bảo:**
- ✅ **Tách rời trách nhiệm** - Mỗi tầng có trách nhiệm cụ thể
- ✅ **Xử lý lỗi** - Xử lý lỗi nhất quán trên tất cả tầng
- ✅ **Dễ kiểm tra** - Mỗi tầng có thể được kiểm tra độc lập
- ✅ **Dễ bảo trì** - Dễ thêm tính năng mới
- ✅ **Tái sử dụng code** - Services có thể được tái sử dụng bởi nhiều controllers
- ✅ **Bảo mật** - Kiểm tra ở nhiều tầng
- ✅ **Hiệu suất** - Truy vấn database hiệu quả qua TypeORM
