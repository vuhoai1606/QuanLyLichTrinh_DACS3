# BFY - Screen Specification (Mobile)

Project: Better For Yourself (BFY)
Platform phase: Android-first with KMP core architecture
UI language: 100% English

---

# Màn hình: Splash Screen

**Mã màn hình:** UI-SCR-001
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Splash Screen Mockup](assets/mockups/splash-screen.png)

## 2. Danh sách các thành phần (Components)

| Thành phần          | Loại             | Mô tả/Logic                                             |
| :------------------ | :--------------- | :------------------------------------------------------ |
| BFY Logo            | Image            | Hiển thị logo trung tâm màn hình.                       |
| Loading animation   | Lottie/Progress  | Hiệu ứng loading ngắn khi khởi chạy app.                |
| Token check service | Background logic | Kiểm tra access token còn hợp lệ để điều hướng tự động. |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Hiển thị logo + animation trong khi kiểm tra token.
- **Empty:** Không áp dụng.
- **Error:** Token check lỗi do local/session corruption thì chuyển về Authentication và ghi log.

## 4. Logic chuyển trang (Navigation)

- **Token hợp lệ:** Chuyển đến Home Dashboard (UI-SCR-003).
- **Token không hợp lệ/không tồn tại:** Chuyển đến Authentication (UI-SCR-002).

---

# Màn hình: Authentication (Login/Register)

**Mã màn hình:** UI-SCR-002
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Authentication Mockup](assets/mockups/auth-screen.png)

## 2. Danh sách các thành phần (Components)

| Thành phần                 | Loại              | Mô tả/Logic                                              |
| :------------------------- | :---------------- | :------------------------------------------------------- |
| Tab switch Login/Register  | Segmented control | Chuyển giữa form Sign In và Sign Up trong cùng màn hình. |
| Email input                | TextField         | Validate định dạng email theo thời gian thực.            |
| Password input             | PasswordField     | Ẩn/hiện mật khẩu, kiểm tra độ dài tối thiểu.             |
| Full Name input (Register) | TextField         | Chỉ hiển thị khi ở tab Register.                         |
| Sign In button             | Button            | Gửi request đăng nhập.                                   |
| Sign Up button             | Button            | Gửi request tạo tài khoản.                               |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Disable form, hiển thị spinner trên nút submit.
- **Empty:** Form mặc định với placeholder.
- **Error:** Sai thông tin, email trùng, lỗi mạng; hiển thị lỗi ngay dưới field hoặc toast tổng quát.

## 4. Logic chuyển trang (Navigation)

- **Sign In thành công:** Chuyển đến Home Dashboard (UI-SCR-003).
- **Sign Up thành công:** Chuyển sang Login state hoặc auto-login vào Home Dashboard (UI-SCR-003).
- **Nhấn Back trên Android ở màn này:** Thoát app theo chuẩn hệ thống.

---

# Màn hình: Home Dashboard (Tab 1)

**Mã màn hình:** UI-SCR-003
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Home Dashboard Mockup](assets/mockups/home-dashboard.png)

## 2. Danh sách các thành phần (Components)

| Thành phần                 | Loại                    | Mô tả/Logic                                                  |
| :------------------------- | :---------------------- | :----------------------------------------------------------- |
| Greeting header            | Text                    | Hiển thị "Hello, [Name]" theo profile.                       |
| EXP progress + level       | Progress bar + badge    | Cập nhật theo gamification realtime hoặc sau sync.           |
| Filter button              | IconButton              | Lọc timeline theo category/color/tag.                        |
| Unified timeline list      | RecyclerView/LazyColumn | Hiển thị gộp To-do, Task, Event theo thứ tự thời gian.       |
| Item type label [E]/[T]    | Label chip              | Event có [E], Task có [T], To-do không có nhãn.              |
| Category color border      | Visual indicator        | Viền label đổi theo màu category.                            |
| To-do checkbox             | Checkbox                | Tick để mark done.                                           |
| Row swipe action           | Gesture + action        | Vuốt trái để hiện nút Delete màu đỏ.                         |
| Long press multi-select    | Gesture mode            | Nhấn giữ để vào Select Mode, cho phép bulk action.           |
| Bulk action bar            | Top action bar          | Xóa hàng loạt hoặc Mark done hàng loạt.                      |
| Floating Action Button (+) | FAB                     | Mở Bottom Sheet tạo mới (UI-SCR-008), không chuyển màn hình. |
| Confetti mini effect       | Animation               | Bắn hiệu ứng khi hoàn thành To-do.                           |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Skeleton list + placeholder header.
- **Empty:** Hiển thị thông điệp "No plans for today" và nút "Create first item" (mở UI-SCR-008).
- **Error:** Hiển thị "Unable to load timeline" + nút "Retry".
- **Select Mode:** Hiển thị check indicator trên từng item và thanh công cụ bulk action.

## 4. Logic chuyển trang (Navigation)

- **Tap vào item Task/Event:** Chuyển đến màn hình chi tiết tương ứng (UI-SCR-009 nếu là Group Task/Event, hoặc detail local nếu cá nhân).
- **Tap FAB (+):** Mở Bottom Sheet Creation (UI-SCR-008).
- **Switch bottom navigation:** Chuyển sang Focus (UI-SCR-004), Collaboration (UI-SCR-005), Profile (UI-SCR-007).

---

# Màn hình: Focus Mode (Tab 2)

**Mã màn hình:** UI-SCR-004
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Focus Mode Mockup](assets/mockups/focus-mode.png)

## 2. Danh sách các thành phần (Components)

| Thành phần               | Loại          | Mô tả/Logic                                                    |
| :----------------------- | :------------ | :------------------------------------------------------------- |
| Circular countdown timer | Custom view   | Hiển thị thời gian còn lại dạng vòng tròn lớn.                 |
| Time text (MM:SS)        | Text          | Luôn rõ ràng, cỡ lớn, lấy từ setting mặc định hoặc custom.     |
| Start Focus button       | Button        | Bắt đầu phiên focus.                                           |
| Give Up button           | Danger button | Xuất hiện thay cho Start khi timer đang chạy.                  |
| Focus warning dialog     | Dialog        | Cảnh báo khi người dùng thoát app/chuyển tab trong phiên chạy. |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Khởi tạo session/timer state.
- **Empty:** Trạng thái chờ, chưa bắt đầu phiên.
- **Error:** Không khởi chạy được foreground/notification channel thì hiển thị lỗi và hướng dẫn cấp quyền.
- **Running:** Timer chạy, nút chuyển thành Give Up.
- **Completed:** Hiển thị thông báo hoàn thành và EXP nhận được.
- **Abandoned:** Hiển thị trạng thái Give Up, không cộng điểm.

## 4. Logic chuyển trang (Navigation)

- **Tap Start Focus:** Bắt đầu session, giữ người dùng ở màn hiện tại.
- **Tap Give Up:** Kết thúc session thất bại, ở lại màn và reset timer.
- **Rời app/tab khi đang chạy:** Hiển thị warning dialog hoặc system notification cảnh báo mất tập trung.

---

# Màn hình: Collaboration (Tab 3)

**Mã màn hình:** UI-SCR-005
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Collaboration Mockup](assets/mockups/collaboration-tab.png)

## 2. Danh sách các thành phần (Components)

| Thành phần               | Loại              | Mô tả/Logic                                       |
| :----------------------- | :---------------- | :------------------------------------------------ |
| Header segmented buttons | Segmented control | Chuyển giữa My Groups và Assigned to me.          |
| Group list               | List              | Danh sách nhóm đã tham gia.                       |
| Create Group (+)         | Button/FAB nhỏ    | Tạo nhóm mới.                                     |
| Assigned items list      | List              | Danh sách Task/Event được giao cho user hiện tại. |
| Accept button            | Button            | Chấp nhận và thêm vào lịch cá nhân.               |
| Decline button           | Outlined button   | Từ chối assignment và gửi phản hồi trạng thái.    |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Skeleton cho group list hoặc assigned list.
- **Empty:**
  - My Groups: "You have no groups yet" + nút "Create group".
  - Assigned to me: "No assignments".
- **Error:** "Cannot load collaboration data" + Retry.

## 4. Logic chuyển trang (Navigation)

- **Tap vào một group ở My Groups:** Chuyển tới Group Detail (UI-SCR-006).
- **Tap Accept:** Confirm dialog, thêm item vào timeline cá nhân, refresh Home.
- **Tap Decline:** Confirm dialog, cập nhật trạng thái Declined và thông báo leader.

---

# Màn hình: Group Detail

**Mã màn hình:** UI-SCR-006
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Group Detail Mockup](assets/mockups/group-detail.png)

## 2. Danh sách các thành phần (Components)

| Thành phần                       | Loại                     | Mô tả/Logic                                         |
| :------------------------------- | :----------------------- | :-------------------------------------------------- |
| Group header                     | App bar                  | Hiển thị tên nhóm, số thành viên, role hiện tại.    |
| Member list                      | Horizontal/Vertical list | Hiển thị thành viên và trạng thái hoạt động cơ bản. |
| Shared task list                 | List                     | Danh sách task/event chung của nhóm.                |
| Assign Task button (Leader only) | Primary button           | Chỉ hiển thị nếu role là leader.                    |
| Task progress chips              | Chip                     | Hiển thị status: To-do/In progress/Done/Overdue.    |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Skeleton cho member và shared list.
- **Empty:** Chưa có task chung, hiển thị CTA "Assign first task" (leader).
- **Error:** Không tải được dữ liệu nhóm, hiển thị Retry.

## 4. Logic chuyển trang (Navigation)

- **Tap Assign Task:** Mở flow giao việc (modal hoặc screen phụ).
- **Tap một task/event:** Mở chi tiết assignment (UI-SCR-009).
- **Back:** Quay lại Collaboration (UI-SCR-005).

---

# Màn hình: Profile & Settings (Tab 4)

**Mã màn hình:** UI-SCR-007
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Profile Settings Mockup](assets/mockups/profile-settings.png)

## 2. Danh sách các thành phần (Components)

| Thành phần             | Loại           | Mô tả/Logic                                    |
| :--------------------- | :------------- | :--------------------------------------------- |
| Current rank card      | Card           | Hiển thị tên rank, icon, EXP hiện tại.         |
| Badge board            | Grid           | Badge đã đạt sáng màu, chưa đạt grayscale.     |
| Focus stats            | Stat row       | Tổng thời gian focus (phút/giờ).               |
| Task stats             | Stat row       | Tổng task hoàn thành.                          |
| Language setting       | Selector       | Mặc định English, hỗ trợ mở rộng ngôn ngữ sau. |
| Theme setting          | Selector       | Light / Dark / System.                         |
| Focus duration setting | Selector/input | Chọn preset 25p, 30p hoặc custom hợp lệ.       |
| Change Password        | Navigation row | Chuyển đến flow đổi mật khẩu.                  |
| Logout                 | Danger button  | Xóa phiên hiện tại và về Authentication.       |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Placeholder cho card, badge, settings list.
- **Empty:** Nếu chưa có dữ liệu gamification, hiển thị thông điệp khuyến khích bắt đầu focus/task.
- **Error:** Lỗi tải profile/settings, hiển thị Retry.

## 4. Logic chuyển trang (Navigation)

- **Tap Change Password:** Chuyển đến màn hình đổi mật khẩu (screen con).
- **Tap Logout:** Hiển thị confirm, sau đó về Authentication (UI-SCR-002).

---

# Màn hình: Bottom Sheet Creation (To-do | Task | Event)

**Mã màn hình:** UI-SCR-008
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Bottom Sheet Creation Mockup](assets/mockups/bottom-sheet-creation.png)

## 2. Danh sách các thành phần (Components)

| Thành phần                     | Loại               | Mô tả/Logic                                                           |
| :----------------------------- | :----------------- | :-------------------------------------------------------------------- |
| Type segmented control         | Segmented control  | Chọn To-do, Task hoặc Event để đổi form động.                         |
| Title input                    | TextField          | Bắt buộc cho cả 3 loại.                                               |
| Description input (Task/Event) | TextArea           | Mo ta chi tiet cho Task va Event.                                     |
| Add more (To-do)               | Text button        | Thêm nhiều dòng To-do liên tiếp.                                      |
| Deadline picker (Task)         | DateTime picker    | Chọn deadline ngày giờ cho Task.                                      |
| Repeat selector (Task/Event)   | Dropdown           | Never, Daily, Mon-Fri, Weekly, Monthly, Yearly; lưu RRULE.            |
| Category dropdown              | Dropdown           | Chọn category đã có.                                                  |
| Add category (+)               | Icon button        | Tạo category mới và chọn màu đại diện.                                |
| Start/End time (Event)         | Time picker        | Chọn thời gian bắt đầu và kết thúc.                                   |
| All day toggle (Event)         | Switch             | Bật để chuyển Event thành cả ngày.                                    |
| Reminder multi-select (Event)  | Multi-select chips | Chọn nhiều mốc nhắc: starts, 5m, 10m, 30m, 1h, 1 day, 1 week, custom. |
| Custom reminder picker         | Picker             | Chọn chính xác trước bao nhiêu ngày/giờ/phút.                         |
| Alarm reminders toggle         | Switch             | Bật để dùng alarm âm thanh.                                           |
| Alarm sound selector           | Picker             | Chọn âm thanh mặc định hoặc file user tự thêm.                        |
| Countdown reminder toggle      | Switch             | Bật để chạy foreground countdown notification bằng Chronometer.       |
| Save button                    | Button             | Validate theo loại và lưu item.                                       |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Hiển thị loading ngắn khi load category/repeat preset.
- **Empty:**
  - Category empty: hiển thị gợi ý tạo category đầu tiên.
  - Form mới mở: các field mặc định theo type.
- **Error:**
  - Thiếu trường bắt buộc: báo lỗi inline.
  - End time <= Start time: chặn lưu Event.
  - RRULE không hợp lệ: báo lỗi và giữ form.

## 4. Logic chuyển trang (Navigation)

- **Mở từ FAB ở Home:** Present dạng bottom sheet trượt từ dưới lên.
- **Tap Save thành công:** Đóng bottom sheet, refresh Home timeline (UI-SCR-003).
- **Tap outside hoặc kéo xuống:** Đóng sheet; nếu có dữ liệu chưa lưu thì hỏi xác nhận discard.

---

# Màn hình: Assignment Detail / Decision

**Mã màn hình:** UI-SCR-009
**Link Figma:** [Dán link vào đây]

## 1. Hình ảnh thiết kế (Mockup)

![Assignment Detail Mockup](assets/mockups/assignment-detail.png)

## 2. Danh sách các thành phần (Components)

| Thành phần              | Loại            | Mô tả/Logic                                                   |
| :---------------------- | :-------------- | :------------------------------------------------------------ |
| Assignment summary card | Card            | Hiển thị title, loại item (Task/Event), deadline, người giao. |
| Description block       | Text area       | Nội dung mô tả chi tiết việc được giao.                       |
| Accept button           | Primary button  | Chấp nhận và tạo liên kết vào lịch cá nhân.                   |
| Decline button          | Outlined button | Từ chối và gửi trạng thái về leader.                          |

## 3. Các trạng thái giao diện (UI States)

- **Loading:** Skeleton summary + action buttons disabled.
- **Empty:** Không áp dụng (item bắt buộc tồn tại).
- **Error:** Không tải được assignment hoặc assignment đã bị thu hồi.

## 4. Logic chuyển trang (Navigation)

- **Tap Accept:** Cập nhật trạng thái, quay lại Collaboration (UI-SCR-005), đồng bộ Home (UI-SCR-003).
- **Tap Decline:** Cập nhật trạng thái từ chối, quay lại Collaboration (UI-SCR-005).

---

# Ghi chú triển khai UI (Android-first + KMP)

- Shared KMP module quản lý state, validation, RRULE mapping, assignment logic, gamification rules.
- Android native module xử lý Foreground Service, AlarmManager, Notification channel, Chronometer countdown.
- Toàn bộ text hiển thị trên UI phải dùng tiếng Anh theo yêu cầu dự án.
- Trên màn hình có timer/countdown, hạn chế recompose/re-render mỗi giây bằng logic thuần UI; ưu tiên native chronometer cho notification countdown để giảm pin drain.
