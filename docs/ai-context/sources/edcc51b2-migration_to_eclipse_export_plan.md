# Kế Hoạch Đồng Bộ & Chuyển Đổi Mã Nguồn Sang Thư Mục `eclipse_export/`

## 1. Mục Tiêu Dự Án
Chuyển đổi toàn bộ mã nguồn, thư viện, tài nguyên và kịch bản cơ sở dữ liệu vào một thư mục riêng biệt mang tên [`eclipse_export/`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/eclipse_export), đồng thời tự động chuẩn hóa mã nguồn để tương thích 100% với cấu trúc dự án Eclipse IDE của bạn (`N07-HeThongQuanLyCuaHangThuCungMio`).

> [!IMPORTANT]
> **Nguyên tắc bảo vệ dữ liệu:**
> - Toàn bộ mã nguồn gốc trong thư mục `src/`, `database/`, `lib/` hiện tại được **giữ nguyên vẹn 100%**, không bị chỉnh sửa hay ghi đè.
> - Mọi thao tác sao chép, chuẩn hóa package và tái cấu trúc chỉ thực hiện bên trong thư mục độc lập [`eclipse_export/`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/eclipse_export).

---

## 2. Xác Nhận Với Người Dùng (User Review Required)

Dựa trên cấu trúc dự án Eclipse thực tế trong ảnh bạn cung cấp:
- Eclipse Package Explorer:
  - `src/controller` *(số ít, không có `s`)*
  - `src/model/dao` *(số ít, không có `s`)*
  - `src/model/entity` *(số ít, không có `s`)*
  - `src/utils`
  - `src/views`
  - `lib/` (FlatLaf 3.5.4, MSSQL JDBC 12.8.1)
  - `resources/icon/` *(số ít `icon`)*

> [!WARNING]
> Nếu chuyển mã nguồn trực tiếp mà không đổi tên package, Eclipse sẽ báo lỗi đỏ toàn bộ 62 tập tin Java do xung đột `package controllers;` và `package models...`.
> Chúng tôi sẽ tự động xử lý toàn bộ các câu lệnh `package` và `import` cho khớp chính xác với Eclipse.

---

## 3. Chi Tiết Kế Hoạch Thực Hiện (Proposed Changes)

### Bước 1: Sao chép và tái cấu trúc Mã nguồn Java (`eclipse_export/src/`)
Đọc từng file Java từ `src/` gốc và ghi sang `eclipse_export/src/` với các điều chỉnh:

1. **Chuẩn hóa khai báo Package:**
   - `package controllers;` $\rightarrow$ `package controller;` (6 files: `BanHangController`, `DangNhapController`, `GiaoCaController`, `NhanVienController`, `PhanCaController`, `ThuCungController`)
   - `package models.dao;` $\rightarrow$ `package model.dao;` (10 files DAO)
   - `package models.entity;` $\rightarrow$ `package model.entity;` (24 files Entity)
   - `package utils;` $\rightarrow$ giữ nguyên
   - `package views;` và `package views.charts;` $\rightarrow$ giữ nguyên
   - `Main.java` $\rightarrow$ giữ ở default package trong `eclipse_export/src/Main.java` (hoặc chuyển vào package nếu bạn yêu cầu).

2. **Cập nhật đồng bộ các câu lệnh Import:**
   - Thay thế mọi `import controllers.*` $\rightarrow$ `import controller.*`
   - Thay thế mọi `import models.dao.*` $\rightarrow$ `import model.dao.*`
   - Thay thế mọi `import models.entity.*` $\rightarrow$ `import model.entity.*`
   - Thay thế mọi `import models.*` $\rightarrow$ `import model.*`

3. **Cập nhật nạp Icon trong `IconHelper.java`:**
   - Bổ sung đường dẫn `/resources/icon/` và `resources/icon/` vào danh sách `possiblePaths` để khớp với thư mục `resources/icon` trên Eclipse của bạn.

---

### Bước 2: Tổng hợp Kịch bản SQL hoàn chỉnh (`eclipse_export/sql/`)
- Tạo tập tin kịch bản duy nhất: [`eclipse_export/sql/HeThongQuanLyCuaHangThuCung.sql`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/eclipse_export/sql/HeThongQuanLyCuaHangThuCung.sql).
- Kịch bản bao gồm:
  1. `CREATE DATABASE QuanLyCuaHangThuCung` & `USE QuanLyCuaHangThuCung`
  2. Tạo 24 bảng dữ liệu với đầy đủ khóa chính (PK) và ràng buộc khóa ngoại (FK) từ [`database/scripts/01_schema.sql`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/database/scripts/01_schema.sql).
  3. Đổ toàn bộ dữ liệu mẫu (Seeding) từ [`database/seeds/02_sample_data.sql`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/database/seeds/02_sample_data.sql) cho cả 4 vai trò (Quản lý, Bán hàng, Thu ngân, Chăm sóc) và các nghiệp vụ bán hàng, kho lô hàng FEFO, lịch hẹn, giao ca.

---

### Bước 3: Sao chép Thư viện (`lib/`) & Tài nguyên (`resources/`)
- Sao chép [`lib/flatlaf-3.5.4.jar`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/lib/flatlaf-3.5.4.jar) và [`lib/mssql-jdbc-12.8.1.jre11.jar`](file:///d:/HOCTAP/HK1_2026-2027/PhatTrienUngDung%20%28AD%29/BAITAPNHOM/Repo_Group07-HeThongQuanLyCuaHangThuCung/lib/mssql-jdbc-12.8.1.jre11.jar) vào `eclipse_export/lib/`.
- Sao chép toàn bộ 35 file icon PNG từ `resources/icons/` vào `eclipse_export/resources/icon/`.

---

## 4. Kế Hoạch Kiểm Thử & Xác Minh (Verification Plan)

### Kiểm thử tự động (Automated Test)
1. **Biên dịch thử nghiệm toàn bộ mã nguồn xuất:**
   Sử dụng trình biên dịch `javac` với classpath từ thư mục `lib/` vừa sao chép:
   ```powershell
   javac -encoding UTF-8 -cp "eclipse_export/lib/*" -d "eclipse_export/bin" (Get-ChildItem -Path "eclipse_export/src" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
   ```
   *Tiêu chí thành công:* Biên dịch thành công 100% với 0 lỗi cú pháp / 0 lỗi thiếu package.

2. **Chạy thử nghiệm giao diện từ bản xuất:**
   Khởi chạy class `Main` trong `eclipse_export/bin` để xác nhận form Đăng nhập và Dashboard nạp trơn tru không lỗi:
   ```powershell
   java -cp "eclipse_export/bin;eclipse_export/lib/*" Main
   ```

---

## 5. Cấu Trúc Bàn Giao Cuối Cùng

```text
eclipse_export/
├── src/
│   ├── controller/             # 6 Controller files (package controller;)
│   ├── model/
│   │   ├── dao/                # 10 DAO files (package model.dao;)
│   │   └── entity/             # 24 Entity files (package model.entity;)
│   ├── utils/                  # 4 Utility files (DBConnection, IconHelper, ThemeColors, UserSession)
│   ├── views/                  # 14 UI Panels/Dialogs (package views;)
│   │   └── charts/             # 2 Chart Panels (package views.charts;)
│   └── Main.java               # File khởi chạy ứng dụng
├── sql/
│   └── HeThongQuanLyCuaHangThuCung.sql   # Kịch bản DDL + DML đầy đủ
├── lib/
│   ├── flatlaf-3.5.4.jar
│   └── mssql-jdbc-12.8.1.jre11.jar
└── resources/
    └── icon/                   # 35 icons định dạng PNG
```
