# BFY - Database Specification (PostgreSQL, Android-first, KMP-ready)

## I. TONG QUAN DU AN (PROJECT OVERVIEW)

- Ten san pham: BFY (Better For Yourself)
- Nen tang phat trien: Android (Giai doan 1 cua DACS3), kien truc loi theo Kotlin Multiplatform (KMP) de mo rong iOS.
- Ngon ngu UI: 100% English.
- Deadline: 20/05/2026.

## II. PHAN TICH VAN DE & MUC TIEU (PROBLEM & OBJECTIVE)

- Doi tuong muc tieu: Hoc sinh, Sinh vien.
- Pain points:
  - De quen viec do thieu he thong nhac nho manh.
  - De mat tap trung do xao nhang dien thoai.
  - Lam viec nhom kho theo doi tien do va nhac viec thu cong.
- Muc tieu: He sinh thai nang suat 3-trong-1 (Schedule, Focus, Collaboration) + Gamification.

## III. USP (UNIQUE SELLING PROPOSITION)

- Unified Timeline: Gop TODO/TASK/EVENT vao 1 luong hien thi.
- Peer Pressure Notification: Tu dong nhac thanh vien tre deadline.
- Gamification for Discipline: EXP, Rank, Badge de thuong tinh ky luat.

## IV. FUNCTIONAL REQUIREMENTS MAPPING

- FR1 Authentication: users.
- FR2 Personal Schedule: schedules, categories, reminders.
- FR3 Focus Mode: focus_sessions.
- FR4 Collaboration: groups, group_members, schedule_assignments.
- FR5 Notification & Alarm: reminders + is_countdown_enabled trong schedules.

## V. NON-FUNCTIONAL REQUIREMENTS MAPPING

- NFR1 (KMP + Android native):
  - Shared KMP: business rules, validation, timeline aggregation.
  - Android native: foreground service, alarm, screen tracking.
- NFR2 (Performance):
  - Countdown notification su dung native Chronometer.
  - Query timeline toi uu boi index tren schedule time fields.
- NFR3 (Backend + DB):
  - RESTful API.
  - PostgreSQL voi quan he chat giua user, group, schedule, assignment, reminder.

---

## 1. Nguyen tac thiet ke DB

- Database model: PostgreSQL tables + reference theo UUID.
- ID strategy: UUID cho tat ca row id (khong dung serial/bigserial de dong nhat da nen tang).
- Timezone: Luu tat ca moc thoi gian duoi UTC (TIMESTAMPTZ); convert local o client.
- Soft/hard delete:
  - Mac dinh hard delete cho category, schedule, group membership theo cascade nghiep vu.
  - Co the mo rong soft delete sau (deleted_at).

---

## 2. Enum chuan hoa

- schedules.type: TODO | TASK | EVENT
- schedules.status: PENDING | DOING | DONE
- schedule_assignments.assign_status: PENDING | ACCEPTED | DECLINED
- focus_sessions.status: COMPLETED | FAILED
- reminders.trigger_type: WHEN_STARTS | MIN_5 | MIN_10 | MIN_30 | HOUR_1 | DAY_1 | WEEK_1 | CUSTOM

---

## 3. Bang du lieu chi tiet

### 3.1 users

Muc dich: Luu tai khoan, profile co ban, thong tin gamification tong.

| Field         | Type        | Required | Constraint/Default      | Mo ta                               |
| :------------ | :---------- | :------: | :---------------------- | :---------------------------------- |
| id            | UUID        |   Yes    | Primary key             | Dinh danh user                      |
| email         | TEXT        |   Yes    | Unique, lower-case      | Email dang nhap                     |
| password_hash | TEXT        |   Yes    | Not null                | Mat khau da hash                    |
| full_name     | TEXT        |   Yes    | Max 100 chars           | Ten hien thi                        |
| avatar_url    | TEXT        |    No    | Nullable                | Anh dai dien user                   |
| bio           | TEXT        |    No    | Nullable, max 280 chars | Mo ta ngan profile                  |
| timezone      | TEXT        |    No    | Default 'UTC'           | Mui gio user (vd: Asia/Ho_Chi_Minh) |
| total_exp     | INTEGER     |   Yes    | Default 0, >= 0         | Tong EXP                            |
| current_rank  | TEXT        |   Yes    | Default 'Rookie'        | Rank hien tai                       |
| created_at    | TIMESTAMPTZ |   Yes    | Default now()           | Ngay tao                            |
| updated_at    | TIMESTAMPTZ |   Yes    | Default now()           | Ngay cap nhat profile               |

Indexes:

- unique: email
- index: created_at desc

Business rules:

- Email duoc normalize lower-case truoc khi luu.
- Khong luu plain text password.
- timezone dung IANA timezone format de convert lich chinh xac tren mobile.

---

### 3.2 user_settings

Muc dich: Luu cai dat ca nhan de mapping tab Profile & Settings.

| Field                 | Type        | Required | Constraint/Default                | Mo ta                       |
| :-------------------- | :---------- | :------: | :-------------------------------- | :-------------------------- |
| user_id               | UUID        |   Yes    | Primary key, Ref users.id         | 1-1 voi user                |
| language              | TEXT        |   Yes    | Default 'en'                      | Ngon ngu UI                 |
| theme                 | ENUM        |   Yes    | LIGHT/DARK/SYSTEM, default SYSTEM | Giao dien sang/toi/he thong |
| default_focus_minutes | INTEGER     |   Yes    | Default 25, > 0                   | Pomodoro mac dinh           |
| notifications_enabled | BOOLEAN     |   Yes    | Default true                      | Bat/tat thong bao ung dung  |
| updated_at            | TIMESTAMPTZ |   Yes    | Default now()                     | Lan cap nhat setting cuoi   |

Indexes:

- unique: user_id
- index: theme

Business rules:

- Tao user moi thi auto tao user_settings mac dinh.
- language phai map danh sach locale app ho tro.

---

### 3.3 categories

Muc dich: Danh muc do user tu tao, kem mau de render timeline.

| Field     | Type | Required | Constraint/Default      | Mo ta               |
| :-------- | :--- | :------: | :---------------------- | :------------------ |
| id        | UUID |   Yes    | Primary key             | Dinh danh category  |
| user_id   | UUID |   Yes    | Ref users.id            | Chu so huu danh muc |
| name      | TEXT |   Yes    | Not null, max 50 chars  | Ten danh muc        |
| hex_color | TEXT |   Yes    | Regex ^#[0-9A-Fa-f]{6}$ | Mau category        |

Indexes:

- index: user_id
- unique compound: (user_id, name)

Business rules:

- Xoa user thi xoa category cua user do (cascade qua service).
- Category chi thuoc ve 1 user.

---

### 3.4 groups

Muc dich: Nhom hoc tap/lam viec.

| Field      | Type        | Required | Constraint/Default      | Mo ta           |
| :--------- | :---------- | :------: | :---------------------- | :-------------- |
| id         | UUID        |   Yes    | Primary key             | Dinh danh group |
| name       | TEXT        |   Yes    | Not null, max 120 chars | Ten nhom        |
| leader_id  | UUID        |   Yes    | Ref users.id            | Truong nhom     |
| created_at | TIMESTAMPTZ |   Yes    | Default now()           | Ngay tao        |

Indexes:

- index: leader_id
- text or index: name

Business rules:

- Leader bat buoc la thanh vien cua nhom (enforce khi tao membership).

---

### 3.5 group_members

Muc dich: Quan he n-n giua user va group.

| Field     | Type        | Required | Constraint/Default | Mo ta         |
| :-------- | :---------- | :------: | :----------------- | :------------ |
| group_id  | UUID        |   Yes    | Ref groups.id      | Nhom          |
| user_id   | UUID        |   Yes    | Ref users.id       | Thanh vien    |
| joined_at | TIMESTAMPTZ |   Yes    | Default now()      | Ngay tham gia |

Primary key logic:

- unique compound: (group_id, user_id)

Indexes:

- index: user_id
- index: group_id

Business rules:

- Xoa group hoac user thi xoa membership lien quan.

---

### 3.6 schedules (Bang trung tam Unified Timeline)

Muc dich: Luu TODO/TASK/EVENT trong 1 bang de query timeline 1 lan.

| Field                | Type        | Required | Constraint/Default                  | Mo ta                                  |
| :------------------- | :---------- | :------: | :---------------------------------- | :------------------------------------- |
| id                   | UUID        |   Yes    | Primary key                         | Dinh danh item                         |
| creator_id           | UUID        |   Yes    | Ref users.id                        | Nguoi tao                              |
| group_id             | UUID        |    No    | Nullable, Ref groups.id             | Null = ca nhan, co gia tri = item nhom |
| category_id          | UUID        |    No    | Nullable, Ref categories.id         | Danh muc                               |
| title                | TEXT        |   Yes    | Not null, max 100 chars             | Tieu de                                |
| description          | TEXT        |    No    | Nullable, max 1000 chars            | Mo ta cho TASK/EVENT                   |
| type                 | ENUM        |   Yes    | TODO/TASK/EVENT                     | Loai item                              |
| start_time           | TIMESTAMPTZ |    No    | Nullable                            | Danh cho EVENT                         |
| end_time             | TIMESTAMPTZ |    No    | Nullable                            | Danh cho EVENT                         |
| deadline             | TIMESTAMPTZ |    No    | Nullable                            | Danh cho TASK                          |
| is_all_day           | BOOLEAN     |   Yes    | Default false                       | Event all day                          |
| rrule                | TEXT        |    No    | Nullable                            | Quy tac lap lai                        |
| status               | ENUM        |   Yes    | PENDING/DOING/DONE, default PENDING | Trang thai TODO/TASK                   |
| is_countdown_enabled | BOOLEAN     |   Yes    | Default false                       | Bat countdown notification             |
| created_at           | TIMESTAMPTZ |   Yes    | Default now()                       | Ngay tao                               |
| updated_at           | TIMESTAMPTZ |   Yes    | Default now()                       | Ngay cap nhat item                     |

Indexes (quan trong cho hieu nang timeline):

- index: creator_id + created_at desc
- index: group_id + created_at desc
- index: type + deadline
- index: type + start_time
- index: type + end_time
- index: status
- index: category_id

Validation rules theo type:

- TODO:
  - title required.
  - start_time, end_time, deadline deu nullable.
- TASK:
  - deadline required.
  - deadline >= created_at khi tao moi.
- EVENT:
  - start_time va end_time required.
  - end_time > start_time.

Business rules:

- Item group phai co group_id va creator la thanh vien group do.
- Item type EVENT co the bat reminder + alarm + countdown.

---

### 3.7 reminders

Muc dich: 1 schedule co the co nhieu moc nhac nho/alarm.

| Field             | Type        | Required | Constraint/Default                                         | Mo ta                     |
| :---------------- | :---------- | :------: | :--------------------------------------------------------- | :------------------------ |
| id                | UUID        |   Yes    | Primary key                                                | Dinh danh reminder        |
| schedule_id       | UUID        |   Yes    | Ref schedules.id                                           | Item duoc nhac            |
| trigger_type      | ENUM        |   Yes    | WHEN_STARTS/MIN_5/MIN_10/MIN_30/HOUR_1/DAY_1/WEEK_1/CUSTOM | Kieu kich hoat            |
| custom_time       | TIMESTAMPTZ |    No    | Required khi trigger_type = CUSTOM                         | Gio custom                |
| is_alarm          | BOOLEAN     |   Yes    | Default false                                              | true = bao thuc am thanh  |
| sound_uri         | TEXT        |    No    | Nullable                                                   | Duong dan file am thanh   |
| last_triggered_at | TIMESTAMPTZ |    No    | Nullable                                                   | Lan reminder vua duoc ban |

Indexes:

- index: schedule_id
- index: trigger_type

Business rules:

- 1 schedule co nhieu reminders.
- Neu is_alarm = true va sound_uri null, he thong dung default sound.
- Xoa schedule thi xoa reminders lien quan.

---

### 3.8 schedule_assignments

Muc dich: Luu thong tin giao viec/chia se viec cho user trong collaboration.

| Field         | Type | Required | Constraint/Default                         | Mo ta                |
| :------------ | :--- | :------: | :----------------------------------------- | :------------------- |
| schedule_id   | UUID |   Yes    | Ref schedules.id                           | Item duoc giao       |
| assignee_id   | UUID |   Yes    | Ref users.id                               | Nguoi nhan           |
| assign_status | ENUM |   Yes    | PENDING/ACCEPTED/DECLINED, default PENDING | Trang thai chap nhan |

Primary key logic:

- unique compound: (schedule_id, assignee_id)

Indexes:

- index: assignee_id + assign_status
- index: schedule_id

Business rules:

- Chi assign item group cho thanh vien trong group.
- Neu assignee decline, item khong duoc dua vao timeline ca nhan.

---

### 3.9 focus_sessions

Muc dich: Luu lich su Pomodoro + EXP tu focus.

| Field            | Type        | Required | Constraint/Default | Mo ta             |
| :--------------- | :---------- | :------: | :----------------- | :---------------- |
| id               | UUID        |   Yes    | Primary key        | Dinh danh session |
| user_id          | UUID        |   Yes    | Ref users.id       | Nguoi focus       |
| duration_minutes | INTEGER     |   Yes    | > 0                | So phut setup     |
| status           | ENUM        |   Yes    | COMPLETED/FAILED   | Ket qua session   |
| exp_earned       | INTEGER     |   Yes    | Default 0, >= 0    | EXP nhan duoc     |
| created_at       | TIMESTAMPTZ |   Yes    | Default now()      | Thoi diem tao     |

Indexes:

- index: user_id + created_at desc
- index: status

Business rules:

- COMPLETED moi duoc cong EXP (tuy quy tac game).
- FAILED thi exp_earned = 0 (mac dinh).

---

## 4. Quan he du lieu (Reference Model)

- users (1) - (n) categories
- users (1) - (1) user_settings
- users (1) - (n) schedules qua creator_id
- users (1) - (n) focus_sessions
- users (n) - (n) groups qua group_members
- groups (1) - (n) schedules qua group_id
- schedules (1) - (n) reminders
- schedules (n) - (n) users qua schedule_assignments

---

## 5. Query mau cho Unified Timeline (PostgreSQL query)

Muc tieu: Query 1 lan de lay timeline hom nay cho 1 user gom item ca nhan + item duoc giao ACCEPTED + item nhom lien quan.

Input:

- userId
- startOfDayUTC
- endOfDayUTC

Dieu kien loc co ban:

- creator_id = userId (item ca nhan), hoac
- assignee_id = userId va assign_status = ACCEPTED, hoac
- group_id thuoc danh sach group user tham gia.

Sort:

- uu tien start_time, neu null thi deadline, neu null thi created_at.

Goi y:

- can bo index khop voi dieu kien group_id, assignee_id, deadline/start_time.

---

## 6. Rang buoc nghiep vu quan trong

- TODO/TASK/EVENT dung chung schedules de toi uu timeline query.
- Reminder countdown tren Android dung Foreground Service + Chronometer (notification), khong render lai moi giay trong UI layer.
- RRULE duoc validate truoc khi ghi DB.
- Moi thay doi assignment phai ghi log su kien o service layer (de phuc vu peer pressure notification va audit).

---

## 7. Kiem tra tinh toan ven (Data Integrity Checklist)

- [ ] unique email duoc enforce.
- [ ] user_settings tao tu dong khi user tao moi.
- [ ] unique (group_id, user_id) duoc enforce.
- [ ] unique (schedule_id, assignee_id) duoc enforce.
- [ ] EVENT co start_time/end_time hop le.
- [ ] TASK co deadline hop le.
- [ ] CUSTOM reminder co custom_time.
- [ ] Xoa schedule phai xoa reminders va assignments lien quan.
- [ ] Xoa user phai xu ly cascade dung nghiep vu (category, membership, focus sessions).

---

## 8. Mapping nhanh sang API modules

- Auth API: users.
- Settings API: user_settings.
- Schedule API: schedules, categories, reminders.
- Collaboration API: groups, group_members, schedule_assignments.
- Focus API: focus_sessions.
- Gamification API: update users.total_exp, users.current_rank dua tren task/focus hop le.
