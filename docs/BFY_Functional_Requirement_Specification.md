# BFY - Functional Requirement Specification

Project: Better For Yourself (BFY)
Platform phase: Android-first, core architecture aligned with Kotlin Multiplatform (KMP)
Deadline: 20/05/2026

---

# Chức năng: User Registration

**Mã chức năng:** F-REQ-001
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép người dùng mới tạo tài khoản BFY để sử dụng hệ sinh thái quản lý lịch trình, tập trung và cộng tác nhóm.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng chưa có phiên đăng nhập hợp lệ.
- Thiết bị có kết nối mạng để gọi REST API.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng mở màn hình Sign Up.
2. Hệ thống hiển thị form gồm: Full Name, Email, Password, Confirm Password.
3. Người dùng nhập thông tin và nhấn "Create Account".
4. Hệ thống kiểm tra định dạng và tính hợp lệ.
5. Hệ thống gửi yêu cầu tạo tài khoản lên backend.
6. Backend tạo user mới trong MongoDB.
7. Hệ thống thông báo đăng ký thành công và chuyển sang màn hình đăng nhập hoặc tự đăng nhập.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Thiếu thông tin bắt buộc:** Hệ thống highlight trường trống và không cho submit.
- **E2: Email đã tồn tại:** Hệ thống báo lỗi và yêu cầu dùng email khác.
- **E3: Mật khẩu không đạt chính sách:** Hệ thống hiển thị yêu cầu độ mạnh mật khẩu.
- **E4: Mất kết nối mạng:** Hệ thống báo lỗi mạng và cho phép thử lại.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Email phải đúng định dạng.
- Password tối thiểu 8 ký tự, có chữ và số.
- Một email chỉ được liên kết với một tài khoản BFY.

---

# Chức năng: User Login

**Mã chức năng:** F-REQ-002
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép người dùng xác thực để truy cập dữ liệu cá nhân, nhóm và các tính năng cốt lõi.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã có tài khoản hợp lệ.
- Tài khoản chưa bị khóa.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng mở màn hình Sign In.
2. Người dùng nhập Email và Password.
3. Người dùng nhấn "Login".
4. Hệ thống gửi yêu cầu xác thực tới backend.
5. Backend xác thực thông tin và trả access token.
6. Hệ thống lưu phiên đăng nhập an toàn và điều hướng vào trang chính.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Sai thông tin đăng nhập:** Hệ thống báo "Invalid credentials".
- **E2: Tài khoản bị vô hiệu hóa:** Hệ thống chặn đăng nhập và hướng dẫn liên hệ hỗ trợ.
- **E3: Token lỗi/hết hạn trong phiên cũ:** Hệ thống yêu cầu đăng nhập lại.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Không được tiết lộ chi tiết nguyên nhân sai đăng nhập gây lộ thông tin tài khoản.
- Phiên đăng nhập cần hết hạn theo chính sách bảo mật.

---

# Chức năng: Profile Management

**Mã chức năng:** F-REQ-003
**Mức độ ưu tiên:** Trung bình (Should-have)

## 1. Mô tả tóm tắt

Cho phép người dùng cập nhật hồ sơ cá nhân để cá nhân hóa trải nghiệm và hiển thị danh tính trong nhóm.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng vào trang Profile.
2. Hệ thống tải thông tin hồ sơ hiện tại.
3. Người dùng chỉnh sửa thông tin (display name, avatar, bio, timezone).
4. Người dùng nhấn "Save".
5. Hệ thống xác thực dữ liệu và cập nhật backend.
6. Hệ thống hiển thị thông báo cập nhật thành công.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Dữ liệu không hợp lệ:** Hệ thống báo lỗi tại trường tương ứng.
- **E2: Upload avatar thất bại:** Hệ thống báo lỗi và giữ dữ liệu cũ.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Display name tối đa 50 ký tự.
- Avatar phải đúng định dạng ảnh cho phép.

---

# Chức năng: Manage To-do Items (CRUD)

**Mã chức năng:** F-REQ-004
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép người dùng tạo và quản lý các công việc đơn giản dạng To-do để tránh quên việc hằng ngày.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng mở module Personal Schedule.
2. Người dùng chọn "New To-do".
3. Hệ thống hiển thị form: title, category, color, repeat (optional RRULE).
4. Người dùng nhập dữ liệu và nhấn "Save".
5. Hệ thống lưu dữ liệu và đồng bộ lên Unified Timeline.
6. Người dùng có thể Edit/Delete/Mark Done cho To-do.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Thiếu title:** Hệ thống không cho lưu.
- **E2: RRULE sai cú pháp:** Hệ thống báo lỗi và yêu cầu sửa.
- **E3: Đồng bộ thất bại:** Hệ thống giữ local state và thử đồng bộ lại.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Title tối đa 100 ký tự.
- Dữ liệu To-do phải được map vào Unified Timeline theo timestamp.
- Màu và category phải lấy từ danh mục hợp lệ.

---

# Chức năng: Manage Task with Deadline (CRUD)

**Mã chức năng:** F-REQ-005
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép tạo và theo dõi Task có deadline rõ ràng để kiểm soát tiến độ cá nhân và làm cơ sở chấm điểm kỷ luật.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng chọn "New Task".
2. Hệ thống hiển thị form: title, description, start date, deadline, category/color, repeat.
3. Người dùng nhập thông tin và nhấn lưu.
4. Hệ thống kiểm tra hợp lệ và ghi nhận vào cơ sở dữ liệu.
5. Task được hiển thị trong Unified Timeline và danh sách Task.
6. Người dùng cập nhật trạng thái (To-do/In-progress/Done).

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Deadline nhỏ hơn start date:** Hệ thống không cho lưu.
- **E2: Task trùng lặp gần như hoàn toàn:** Hệ thống cảnh báo tạo trùng.
- **E3: Chưa đủ quyền sửa task (trong nhóm):** Hệ thống từ chối cập nhật.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Deadline không được ở quá khứ khi tạo mới.
- Task hoàn thành đúng hạn mới được cộng đầy đủ EXP.
- Chỉ owner/leader hoặc người được phân quyền mới sửa nội dung task.

---

# Chức năng: Manage Event with Duration (CRUD)

**Mã chức năng:** F-REQ-006
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép tạo sự kiện có thời lượng (start-end) để quản lý lịch học/lịch họp có khung giờ cụ thể.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng chọn "New Event".
2. Hệ thống hiển thị form: title, description, start time, end time, repeat, category/color.
3. Người dùng nhập thông tin và lưu.
4. Hệ thống xác thực thời gian và lưu vào database.
5. Event xuất hiện trong Unified Timeline theo đúng time block.
6. Người dùng có thể chỉnh sửa hoặc hủy event.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: End time <= start time:** Hệ thống báo lỗi logic thời gian.
- **E2: Trùng giờ với event quan trọng khác:** Hệ thống cảnh báo conflict.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Event phải có start time và end time hợp lệ.
- Với event lặp, phải hỗ trợ chuẩn RRULE.

---

# Chức năng: Pomodoro Focus Mode & App Leave Warning

**Mã chức năng:** F-REQ-007
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Giúp người dùng tập trung học/làm việc bằng bộ đếm Pomodoro và cảnh báo khi rời app/tab để giảm xao nhãng.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)
- Android Native Layer (Foreground/App State Tracking)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.
- Người dùng cấp quyền thông báo cần thiết.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng vào Focus Mode.
2. Người dùng chọn preset (ví dụ 25/5) hoặc custom thời lượng.
3. Người dùng nhấn "Start Focus".
4. Hệ thống khởi chạy bộ đếm và bật giám sát trạng thái app.
5. Nếu người dùng rời app trong phiên focus, hệ thống hiển thị cảnh báo.
6. Khi hết phiên, hệ thống phát tín hiệu hoàn thành và ghi nhận kết quả.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: App bị kill bởi hệ điều hành:** Hệ thống khôi phục phiên gần nhất khi mở lại (nếu khả thi).
- **E2: Thiếu quyền thông báo foreground:** Hệ thống yêu cầu cấp quyền trước khi chạy đầy đủ.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Focus timer phải ưu tiên cơ chế native để tiết kiệm pin.
- Chỉ phiên hoàn thành hợp lệ mới được cộng EXP đầy đủ.
- Cảnh báo rời app phải được hiển thị ngay khi phát hiện mất tập trung.

---

# Chức năng: Group Management & Assignment Workflow

**Mã chức năng:** F-REQ-008
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cho phép tạo nhóm học tập/làm việc, phân quyền leader-member và giao nhiệm vụ nội bộ để theo dõi tiến độ minh bạch.

## 2. Tác nhân (Actors)

- Leader
- Member
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người tạo nhóm đã đăng nhập.
- Người dùng được mời tồn tại trong hệ thống.

## 4. Quy trình thực hiện (Main Flow)

1. Leader tạo nhóm mới.
2. Leader thêm thành viên vào nhóm.
3. Leader tạo task/event nội bộ và gán cho member.
4. Member nhận thông báo task được giao.
5. Member chọn "Accept" hoặc "Decline".
6. Hệ thống cập nhật trạng thái assignment và tiến độ nhóm theo thời gian thực.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Thành viên từ chối task:** Hệ thống cập nhật trạng thái Declined và báo cho leader.
- **E2: Leader giao task cho người không thuộc nhóm:** Hệ thống chặn thao tác.
- **E3: Xung đột khi nhiều người cùng sửa task:** Hệ thống áp dụng cơ chế conflict handling.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Leader có quyền quản trị phân công cao nhất trong nhóm.
- Member chỉ cập nhật task được giao cho mình (trừ khi có quyền đặc biệt).
- Mọi thay đổi assignment phải cập nhật trạng thái theo thời gian thực cho các thành viên liên quan.

---

# Chức năng: Peer Pressure Notification (Auto Reminder for Late Members)

**Mã chức năng:** F-REQ-009
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Hệ thống tự động phát hiện thành viên trễ deadline trong nhóm và gửi nhắc nhở để giảm việc leader phải đôn đốc thủ công.

## 2. Tác nhân (Actors)

- Hệ thống (System)
- Leader
- Member

## 3. Điều kiện tiên quyết (Pre-conditions)

- Task đã được assign cho member.
- Có cấu hình ngưỡng xác định trễ hạn.

## 4. Quy trình thực hiện (Main Flow)

1. Hệ thống định kỳ quét trạng thái task nhóm.
2. Hệ thống phát hiện task quá hạn hoặc có nguy cơ quá hạn.
3. Hệ thống gửi thông báo nhắc nhở cho member liên quan.
4. Hệ thống gửi bản tóm tắt tiến độ/chậm trễ cho leader.
5. Hệ thống cập nhật lại trạng thái nhắc nhở hiện tại để tránh gửi trùng.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Thành viên tắt notification:** Hệ thống lưu trạng thái không nhận được push/local alert.
- **E2: Quá nhiều nhắc nhở:** Hệ thống kích hoạt anti-spam cooldown.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Mỗi task trễ chỉ gửi tối đa N thông báo trong 24 giờ (cấu hình được).
- Không gửi nhắc nếu task đã chuyển trạng thái Done trước thời điểm quét.

---

# Chức năng: Local Notification, Custom Alarm & Countdown Reminder

**Mã chức năng:** F-REQ-010
**Mức độ ưu tiên:** Cao (Must-have)

## 1. Mô tả tóm tắt

Cung cấp cơ chế nhắc việc mạnh mẽ gồm thông báo thường, báo thức âm thanh tùy chỉnh và đồng hồ đếm ngược hiển thị liên tục để chống quên.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)
- Android Native Layer (Foreground Service, Alarm Manager, Notification)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã cấp quyền Notification/Alarm theo phiên bản Android.
- Có dữ liệu lịch hoặc focus session cần nhắc.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng cấu hình reminder cho task/event/focus.
2. Hệ thống lưu cấu hình reminder.
3. Đến thời điểm kích hoạt, hệ thống hiển thị Local Notification.
4. Nếu bật Custom Alarm, hệ thống phát âm thanh báo thức.
5. Với Countdown Reminder, hệ thống chạy Foreground Service và hiển thị đồng hồ đếm ngược trên notification/lock screen.
6. Người dùng có thể mở thông báo để vào app hoặc tắt thông báo hiện tại.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Không có quyền hiển thị thông báo:** Hệ thống điều hướng người dùng đến màn hình cấp quyền.
- **E2: Thiết bị vào chế độ tiết kiệm pin mạnh:** Hệ thống cảnh báo khả năng trễ thông báo.
- **E3: Foreground service bị dừng:** Hệ thống thử khởi động lại theo chính sách cho phép.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Countdown trong notification bắt buộc dùng native Chronometer, không tự re-render mỗi giây.
- Foreground Service phải có trạng thái rõ ràng và có thể dừng bởi người dùng.
- Alarm âm thanh phải tuân thủ chính sách âm lượng/quyền hệ thống.

---

# Chức năng: Gamification (EXP, Rank, Badge)

**Mã chức năng:** F-REQ-011
**Mức độ ưu tiên:** Trung bình (Should-have)

## 1. Mô tả tóm tắt

Biến hành vi kỷ luật thành trải nghiệm game hóa nhằm tăng động lực duy trì thói quen học tập/làm việc.

## 2. Tác nhân (Actors)

- Người dùng (User)
- Hệ thống (System)

## 3. Điều kiện tiên quyết (Pre-conditions)

- Người dùng đã đăng nhập.
- Có dữ liệu hoàn thành task/focus hợp lệ.

## 4. Quy trình thực hiện (Main Flow)

1. Người dùng hoàn thành task/focus session.
2. Hệ thống kiểm tra điều kiện hợp lệ (đúng hạn, đủ thời lượng, không gian lận).
3. Hệ thống cộng EXP theo công thức.
4. Nếu đạt ngưỡng, hệ thống nâng rank.
5. Nếu đạt mốc thành tích, hệ thống mở khóa badge.
6. Hệ thống hiển thị tiến trình và lịch sử thành tựu.

## 5. Các luồng phụ/Lỗi (Alternative/Error Flows)

- **E1: Đồng bộ thất bại:** Hệ thống queue dữ liệu và retry.
- **E2: Nghi ngờ gian lận thời gian focus:** Hệ thống không tính thưởng và gắn cờ kiểm tra.

## 6. Quy tắc nghiệp vụ (Business Rules)

- Chỉ tính EXP từ hoạt động hợp lệ theo quy định hệ thống.
- Rank tăng theo các mốc EXP định nghĩa trước.
- Mỗi badge có điều kiện mở khóa rõ ràng và không cấp trùng.

---

# Ghi chú kiến trúc & phi chức năng liên quan triển khai

- Kiến trúc client phải tách rõ shared module (KMP business logic/UI dùng chung) và androidApp module (Foreground Service, Alarm, app state tracking).
- Backend triển khai RESTful API, database MongoDB với reference chặt giữa user, group, task, event, assignment, notification log.
- Ưu tiên tối ưu pin cho Focus timer và Countdown reminder; tuân thủ Chronometer cho countdown notification.
- Tất cả chuỗi hiển thị UI trong app phải dùng tiếng Anh 100%.
