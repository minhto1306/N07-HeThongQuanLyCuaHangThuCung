# KỊCH BẢN KIỂM THỬ: BẢO MẬT, ĐĂNG NHẬP & PHÂN QUYỀN TRUY CẬP
## (SECURITY, AUTHENTICATION & ACCESS CONTROL TEST SPECIFICATION)

- **Mã tài liệu:** `TC_BAO_MAT_DANG_NHAP`
- **Dự án:** Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)
- **Phân hệ mục tiêu:** Phân hệ 5 - Bảo mật, Đăng nhập, Đổi mật khẩu & Phân quyền RBAC
- **Phạm vi thành phần (Scope):**
  - **Tầng Giao diện (GUI):** `gui.LoginForm`, `gui.MainDashboard` (Hộp thoại Đổi mật khẩu), Modal Quên mật khẩu đa bước (`moHopThoaiQuenMatKhau`).
  - **Tầng Nghiệp vụ (BUS):** `bus.DangNhapBus`, `utils.UserSession` (Quản lý phiên, phân quyền RBAC).
  - **Tầng Truy cập Dữ liệu (DAO):** `dao.TaiKhoanDAO`, `dao.NhanVienDAO`.
  - **Tầng Cơ sở Dữ liệu (T-SQL):** Bảng `TaiKhoan`, `NhanVien`, Hàm `dbo.fn_HashPassword`, Thủ tục `dbo.sp_DangNhap`, `dbo.sp_DoiMatKhau`, `dbo.sp_TaoTaiKhoan`.
- **Phiên bản tài liệu:** 1.0 (Chính thức)
- **Ngày ban hành:** 07/11/2026
- **Quy chuẩn kiểm thử:** Độc lập, có thể kiểm chứng, đối chiếu 100% với mã nguồn và CSDL thực tế.

---

## 1. MA TRẬN TRUY XUẤT NGUỒN GỐC & ĐỘ BAO PHỦ (TRACEABILITY MATRIX)

| Mã Test Case | Tên Kịch Bản Kiểm Thử | Thành Phần Mục Tiêu | Cấp Độ | Phân Loại Luồng | Khiếm Khuyết Liên Quan |
|---|---|---|---|---|---|
| **TC-AUTH-001** | Đăng nhập thành công với vai trò Quản lý hệ thống (NVQL - Lê Khãi Minh) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Happy Path | - |
| **TC-AUTH-002** | Đăng nhập thành công với vai trò Quản lý kho (NVQL - Tô Thành Đạt) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Happy Path | - |
| **TC-AUTH-003** | Đăng nhập thành công với vai trò Thu ngân (NVTN - Lê Anh Minh) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Happy Path | - |
| **TC-AUTH-004** | Đăng nhập thành công với vai trò Chuyên viên Spa & Pet Hotel (NVCS - Võ Thị Hồng Ngọc) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Happy Path | - |
| **TC-AUTH-005** | Đăng nhập thành công với vai trò Kỹ thuật viên chăm sóc (NVCS - Trần Thị Thu Hà) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Happy Path | - |
| **TC-AUTH-006** | Đăng nhập thành công bằng Mã nhân viên (thay vì Số điện thoại) | `LoginForm`, `DangNhapBus`, `TaiKhoanDAO` | Integration / UI | Alternative Path | - |
| **TC-AUTH-007** | Đăng nhập thất bại do nhập sai mật khẩu | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Negative Path | - |
| **TC-AUTH-008** | Đăng nhập thất bại do Số điện thoại / Mã NV chưa đăng ký trong hệ thống | `LoginForm`, `DangNhapBus`, `TaiKhoanDAO` | Integration / UI | Negative Path | - |
| **TC-AUTH-009** | Đăng nhập thất bại do để trống ô Số điện thoại hoặc Mật khẩu | `LoginForm`, `DangNhapBus` | Unit / UI | Boundary / Validation | - |
| **TC-AUTH-010** | Đăng nhập với ký tự đặc biệt, khoảng trắng và tấn công SQL Injection | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Security / Adversarial | - |
| **TC-AUTH-011** | Chặn đăng nhập đối với tài khoản nhân viên bị khóa (`trangThaiHoatDong = 0`) | `LoginForm`, `DangNhapBus`, `sp_DangNhap` | Integration / UI | Security / Defect | **DEF-01 / DEF-07 (DEF-CRI-01)** |
| **TC-SEC-001** | Xác minh tính toàn vẹn của hàm băm mật khẩu `fn_HashPassword` (SHA-256) | `fn_HashPassword`, T-SQL | Unit (DB) | Security / Integrity | - |
| **TC-SEC-002** | Kiểm tra lưu trữ mật khẩu an toàn trong CSDL (Không lưu Plaintext Password) | `TaiKhoan`, `sp_TaoTaiKhoan`, `sp_DoiMatKhau` | Unit / Integration | Security / Compliance | - |
| **TC-SEC-003** | Giới hạn số lần thử sai liên tiếp & Phòng chống tấn công vét cạn (Brute-Force) | `LoginForm`, `DangNhapBus`, `TaiKhoan` | UI / Security | Vulnerability Scan | **DEF-03 (DEF-MAJ-01)** |
| **TC-PWD-001** | Quên mật khẩu: Xác thực thông tin SĐT và Email khớp nhau trong CSDL (Bước 1) | `LoginForm`, `DangNhapBus`, `TaiKhoanDAO` | Integration / UI | Happy Path | - |
| **TC-PWD-002** | Quên mật khẩu: Chặn xác thực khi SĐT hoặc Email không khớp hoặc không tồn tại | `LoginForm`, `DangNhapBus`, `TaiKhoanDAO` | Integration / UI | Negative Path | - |
| **TC-PWD-003** | Quên mật khẩu: Nhập mã OTP 6 chữ số hợp lệ trong thời hạn 5 phút (Bước 2) | `LoginForm`, `DangNhapBus` | Unit / UI | Happy Path | - |
| **TC-PWD-004** | Quên mật khẩu: Nhập mã OTP sai, để trống, hoặc OTP đã quá hạn 5 phút | `LoginForm`, `DangNhapBus` | Unit / UI | Edge / Timeout | - |
| **TC-PWD-005** | Quên mật khẩu: Đặt lại mật khẩu mới thành công và đăng nhập lại (Bước 3) | `LoginForm`, `DangNhapBus`, `sp_DoiMatKhau` | Integration / UI | Happy Path | - |
| **TC-PWD-006** | Đổi mật khẩu trong Dashboard: Đổi thành công khi nhập đúng mật khẩu cũ | `MainDashboard`, `DangNhapBus`, `sp_DoiMatKhau` | Integration / UI | Happy Path | - |
| **TC-PWD-007** | Đổi mật khẩu trong Dashboard: Thất bại do nhập sai mật khẩu cũ hiện tại | `MainDashboard`, `DangNhapBus` | Integration / UI | Negative Path | - |
| **TC-PWD-008** | Đổi mật khẩu trong Dashboard: Thất bại do mật khẩu xác nhận không khớp | `MainDashboard` | Unit / UI | Validation | - |
| **TC-PWD-009** | Kiểm tra bất nhất quy định độ dài mật khẩu tối thiểu (4 ký tự vs 6 ký tự) | `LoginForm`, `MainDashboard`, `DangNhapBus` | Integration / UI | Defect / Boundary | **DEF-11 (DEF-MIN-01)** |
| **TC-RBAC-001** | Phân quyền truy cập chức năng cho vai trò Thu ngân (`NVTN`) | `UserSession`, `MainDashboard` | Integration / UI | RBAC Verification | - |
| **TC-RBAC-002** | Phân quyền truy cập chức năng cho vai trò Chuyên viên / KTV chăm sóc (`NVCS`) | `UserSession`, `MainDashboard` | Integration / UI | RBAC Verification | - |
| **TC-RBAC-003** | Phân quyền truy cập chức năng cho vai trò Bán hàng (`NVBH`) | `UserSession`, `MainDashboard` | Integration / UI | RBAC Verification | - |
| **TC-RBAC-004** | Phân quyền truy cập toàn bộ chức năng cho vai trò Quản lý hệ thống (`NVQL`) | `UserSession`, `MainDashboard` | Integration / UI | RBAC Verification | - |
| **TC-RBAC-005** | Kiểm tra lỗ hổng leo thang đặc quyền Quản lý sau khi Đăng xuất (Logout) | `UserSession`, `MainDashboard` | Integration / Security | Critical Vulnerability | **DEF-06 (DEF-MAJ-02)** |

---

## 2. TỪ ĐIỂN DỮ LIỆU KIỂM THỬ (TEST DATA DICTIONARY)

Dữ liệu kiểm thử chuẩn nạp sẵn trong tệp cơ sở dữ liệu `sql/HeThongQuanLyCuaHangThuCung.sql`:

| Mã NV | Số Điện Thoại | Email Đăng Ký | Mật Khẩu Thô | Vai Trò CSDL | Trạng Thái Hoạt Động | Chức Vụ / Ghi Chú |
|---|---|---|---|---|---|---|
| **NV001** | `0901234567` | `minh.quanly@miopet.vn` | `123456` | `NVQL` | 1 (Hoạt động) | Quản lý hệ thống (Toàn quyền quản trị) |
| **NV002** | `0923456789` | `dat.kho@miopet.vn` | `123456` | `NVQL` | 1 (Hoạt động) | Quản lý kho & Lô hàng (Quyền quản trị) |
| **NV003** | `0912345678` | `minh.thungan@miopet.vn` | `123456` | `NVTN` | 1 (Hoạt động) | Thu ngân trưởng ca (POS, Đổi trả, Giao ca) |
| **NV004** | `0945678901` | `ngoc.spa@miopet.vn` | `123456` | `NVCS` | 1 (Hoạt động) | Chuyên viên Spa & Pet Hotel (Dịch vụ) |
| **NV005** | `0934567890` | `ha.ktv@miopet.vn` | `123456` | `NVCS` | 1 (Hoạt động) | Kỹ thuật viên chăm sóc (Làm dịch vụ) |
| **NV006** | `0956789012` | `hung.banhang@miopet.vn` | `123456` | `NVBH` | **0 (ĐÃ BỊ KHÓA)** | Nhân viên bán hàng cũ (Dùng kiểm thử DEF-01 / DEF-CRI-01) |

*Mã băm SHA-256 của chuỗi `123456` trong SQL Server:*  
- **Băm SQL Server `dbo.fn_HashPassword` (`NVARCHAR(255)` UTF-16LE 2-byte):** `ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24`  
- **Băm tiêu chuẩn UTF-8 / ASCII (1-byte char):** `8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92`  
*(Lưu ý kỹ thuật: Do hàm `dbo.fn_HashPassword(@matKhau NVARCHAR(255))` nhận tham số kiểu `NVARCHAR`, SQL Server băm chuỗi ký tự theo chuẩn UTF-16LE (2 bytes/ký tự), sinh ra mã băm `ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24` được lưu thực tế trong cơ sở dữ liệu).*

---

## 3. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (TEST CASE SPECIFICATIONS)

### Nhóm 1: Xác Thực Đăng Nhập (Authentication - TC-AUTH)

#### TC-AUTH-001: Đăng nhập thành công với vai trò Quản lý hệ thống (NVQL)
- **Phân hệ & Màn hình:** Bảo mật / Giao diện `gui.LoginForm` -> `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh nhân viên có vai trò Quản lý (`NV001`) đăng nhập thành công bằng Số điện thoại và Mật khẩu hợp lệ, khởi tạo đầy đủ phiên làm việc và hiển thị toàn bộ thanh điều hướng quản trị.
- **Điều kiện tiên quyết:** CSDL đang hoạt động; tài khoản `NV001` có `trangThaiHoatDong = 1` và mật khẩu khớp với băm của `123456`.
- **Các bước thực hiện:**
  1. Khởi chạy ứng dụng bằng `run.bat` để hiển thị cửa sổ `LoginForm`.
  2. Tại ô `txtPhone`, nhập số điện thoại: `0901234567`.
  3. Tại ô `txtPassword`, nhập mật khẩu: `123456`.
  4. Nhấn phím `Enter` hoặc click nút "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0901234567"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị hộp thoại thông báo: "Đăng nhập thành công!".
  - Cửa sổ `LoginForm` đóng lại.
  - Cửa sổ `MainDashboard` hiển thị với tiêu đề và thanh trạng thái hiển thị đúng thông tin: "Lê Khãi Minh - Quản lý hệ thống (NVQL)".
  - `UserSession.getCurrentUser().getMaNhanVien()` trả về `"NV001"`.
  - `UserSession.getCurrentRole()` trả về `Role.NVQL`.
  - Toàn bộ các nút điều hướng sidebar (Tổng quan, POS, Đổi trả, Kho & Lô hàng, Khách hàng & Thú cưng, Lịch hẹn Spa, Khách sạn Pet Hotel, Quản lý Nhân sự, Phân ca làm việc, Bàn giao ca) đều sáng và bấm được.
- **Tiêu chí Đạt/Không đạt:**
  - *Đạt (Pass):* Đăng nhập thành công, phiên khởi tạo đúng vai trò `NVQL`, không có lỗi ngoại lệ `SQLException` hay `NullPointerException`.
  - *Không đạt (Fail):* Đăng nhập thất bại, sai vai trò hoặc văng ngoại lệ.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra trạng thái tài khoản trước khi thực hiện
  SELECT tk.maTaiKhoan, tk.soDienThoai, tk.vaiTro, tk.trangThaiHoatDong, nv.hoTen, nv.chucVu
  FROM TaiKhoan tk
  JOIN NhanVien nv ON tk.maTaiKhoan = nv.maTaiKhoan
  WHERE tk.maTaiKhoan = 'NV001';
  
  -- Xác minh thủ tục sp_DangNhap trả về đúng 1 bản ghi
  EXEC dbo.sp_DangNhap @identifier = '0901234567', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-002: Đăng nhập thành công với vai trò Quản lý kho (NVQL - Tô Thành Đạt)
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm` -> `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh tài khoản Quản lý kho `NV002` đăng nhập thành công với quyền `NVQL`.
- **Điều kiện tiên quyết:** Tài khoản `NV002` có `trangThaiHoatDong = 1`.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0923456789`.
  3. Nhập `txtPassword`: `123456`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0923456789"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:** Đăng nhập thành công, hiển thị dashboard với tên "Tô Thành Đạt - Quản lý kho & Lô hàng".
- **Tiêu chí Đạt/Không đạt:** Đạt nếu vào được Dashboard và `UserSession.isQuanLy() == true`.
- **Verification SQL Queries:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = '0923456789', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-003: Đăng nhập thành công với vai trò Thu ngân (NVTN - Lê Anh Minh)
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm` -> `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh tài khoản Thu ngân `NV003` đăng nhập thành công; kiểm tra phiên làm việc được gắn đúng vai trò `Role.NVTN`.
- **Điều kiện tiên quyết:** Tài khoản `NV003` có `vaiTro = 'NVTN'`, `trangThaiHoatDong = 1`.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0912345678`.
  3. Nhập `txtPassword`: `123456`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0912345678"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:**
  - Thông báo đăng nhập thành công.
  - Tiêu đề Dashboard hiển thị "Lê Anh Minh - Thu ngân trưởng ca (NVTN)".
  - `UserSession.isThuNgan()` trả về `true`, `UserSession.isQuanLy()` trả về `false`.
  - Các mục Quản lý Nhân sự, Phân ca bị ẩn hoặc vô hiệu hóa.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu phiên làm việc là `NVTN` và các màn hình bị giới hạn quyền truy cập theo RBAC.
- **Verification SQL Queries:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = '0912345678', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-004: Đăng nhập thành công với vai trò Chuyên viên Spa & Pet Hotel (NVCS)
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm` -> `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh tài khoản Chăm sóc thú cưng `NV004` (Võ Thị Hồng Ngọc) đăng nhập thành công với vai trò `NVCS`.
- **Điều kiện tiên quyết:** Tài khoản `NV004` tồn tại và đang hoạt động.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0945678901`.
  3. Nhập `txtPassword`: `123456`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0945678901"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:** Đăng nhập thành công, phiên làm việc gán vai trò `NVCS`. Người dùng truy cập được phân hệ Thú cưng, Lịch hẹn Spa, Khách sạn Pet Hotel; không có quyền truy cập Quản lý Nhân sự hay Phân ca.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu vào được hệ thống với quyền `Role.NVCS`.
- **Verification SQL Queries:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = '0945678901', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-005: Đăng nhập thành công với vai trò Kỹ thuật viên chăm sóc (NVCS - Trần Thị Thu Hà)
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm` -> `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh tài khoản KTV `NV005` đăng nhập thành công với vai trò `NVCS`.
- **Điều kiện tiên quyết:** Tài khoản `NV005` có `trangThaiHoatDong = 1`.
- **Các bước thực hiện:**
  1. Nhập `txtPhone`: `0934567890`.
  2. Nhập `txtPassword`: `123456`.
  3. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0934567890"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:** Đăng nhập thành công, hệ thống chào mừng "Trần Thị Thu Hà".
- **Tiêu chí Đạt/Không đạt:** Đạt nếu xác thực thành công.
- **Verification SQL Queries:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = '0934567890', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-006: Đăng nhập thành công bằng Mã nhân viên (thay vì Số điện thoại)
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm`, `bus.DangNhapBus`, `sp_DangNhap`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh hệ thống hỗ trợ đăng nhập linh hoạt bằng Mã nhân viên (`maTaiKhoan` / `maNhanVien`) thay vì chỉ nhận diện Số điện thoại.
- **Điều kiện tiên quyết:** Tài khoản `NV001` tồn tại trong bảng `TaiKhoan`.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Tại ô `txtPhone`, nhập: `NV001` (hoặc `nv001` chữ thường).
  3. Tại ô `txtPassword`, nhập: `123456`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "NV001"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:**
  - Thủ tục `sp_DangNhap` so khớp điều kiện `tk.maTaiKhoan = @identifier`.
  - Đăng nhập thành công vào Dashboard của Quản lý Lê Khãi Minh.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu đăng nhập thành công bằng mã nhân viên dạng chữ.
- **Verification SQL Queries:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = 'NV001', @matKhauRaw = '123456';
  ```

---

#### TC-AUTH-007: Đăng nhập thất bại do nhập sai mật khẩu
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm`, `bus.DangNhapBus`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh khi nhập đúng Số điện thoại nhưng nhập sai Mật khẩu, hệ thống chặn đăng nhập và hiển thị thông báo lỗi rõ ràng, gợi ý dùng tính năng Quên mật khẩu.
- **Điều kiện tiên quyết:** Tài khoản `0901234567` tồn tại trong hệ thống.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0901234567`.
  3. Nhập `txtPassword`: `SaiMatKhau123`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0901234567"`, `txtPassword = "SaiMatKhau123"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị hộp thoại cảnh báo: "Mật khẩu không chính xác! Vui lòng thử lại hoặc sử dụng 'Quên mật khẩu'.".
  - Cửa sổ `LoginForm` vẫn giữ nguyên, con trỏ chuột focus lại vào ô Mật khẩu và bôi đen chuỗi mật khẩu sai để người dùng nhập lại.
  - Không mở cửa sổ `MainDashboard`.
  - Phiên làm việc `UserSession` không được khởi tạo.
- **Tiêu chí Đạt/Không đạt:**
  - *Đạt (Pass):* Chặn đăng nhập đúng thông báo lỗi, không khởi tạo phiên.
  - *Không đạt (Fail):* Cho phép đăng nhập hoặc văng lỗi crash màn hình.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra sp_DangNhap trả về rỗng (0 bản ghi)
  EXEC dbo.sp_DangNhap @identifier = '0901234567', @matKhauRaw = 'SaiMatKhau123';
  ```

---

#### TC-AUTH-008: Đăng nhập thất bại do Số điện thoại chưa đăng ký
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm`, `bus.DangNhapBus`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh khi nhập Số điện thoại hoặc Mã nhân viên không tồn tại trong cơ sở dữ liệu, hệ thống thông báo lỗi không tìm thấy tài khoản.
- **Điều kiện tiên quyết:** Số điện thoại `0999888777` và mã `NV999` hoàn toàn không có trong bảng `TaiKhoan`.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0999888777`.
  3. Nhập `txtPassword`: `123456`.
  4. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0999888777"`, `txtPassword = "123456"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị thông báo lỗi: "Số điện thoại / Mã nhân viên không tồn tại trong hệ thống!\n(Gợi ý SĐT mẫu: 0901234567, 0912345678 - Mật khẩu: 123456)".
  - Chặn đăng nhập thành công.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu hiển thị đúng thông báo tài khoản không tồn tại.
- **Verification SQL Queries:**
  ```sql
  SELECT COUNT(*) AS SoLuong FROM TaiKhoan WHERE soDienThoai = '0999888777';
  -- Kết quả: 0
  ```

---

#### TC-AUTH-009: Đăng nhập thất bại do để trống ô Số điện thoại hoặc Mật khẩu
- **Phân hệ & Màn hình:** Bảo mật / `gui.LoginForm`, `bus.DangNhapBus`
- **Cấp độ kiểm thử:** Unit Test / UI Test
- **Mục tiêu kiểm thử:** Kiểm tra tính năng validation dữ liệu đầu vào tại tầng GUI và BUS khi người dùng để trống các ô nhập liệu.
- **Điều kiện tiên quyết:** Ứng dụng đang ở màn hình đăng nhập.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  - *Ca 1 (Trống cả hai):* `txtPhone = ""`, `txtPassword = ""` -> Bấm Đăng nhập.
  - *Ca 2 (Trống SĐT):* `txtPhone = "   "`, `txtPassword = "123456"` -> Bấm Đăng nhập.
  - *Ca 3 (Trống Mật khẩu):* `txtPhone = "0901234567"`, `txtPassword = ""` -> Bấm Đăng nhập.
- **Kết quả mong đợi:**
  - *Ca 1 & Ca 2:* Hiển thị thông báo "Vui lòng nhập Số điện thoại nhân viên!", focus vào `txtPhone`.
  - *Ca 3:* Hiển thị thông báo "Vui lòng nhập mật khẩu!", focus vào `txtPassword`.
  - Không gửi truy vấn SQL xuống máy chủ CSDL.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu validation chặn ngay từ tầng ứng dụng trước khi gọi DAO.

---

#### TC-AUTH-010: Đăng nhập với ký tự đặc biệt và tấn công SQL Injection
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm`, `DangNhapBus`, `TaiKhoanDAO`, `sp_DangNhap`
- **Cấp độ kiểm thử:** Integration Test / Security Test
- **Mục tiêu kiểm thử:** Xác minh hệ thống sử dụng tham số hóa truy vấn (Parameterized PreparedStatement / Stored Procedure) an toàn, miễn nhiễm hoàn toàn với các mẫu tấn công chèn mã SQL Injection.
- **Điều kiện tiên quyết:** Bảng `TaiKhoan` có dữ liệu chuẩn.
- **Các bước thực hiện:**
  1. Tại `txtPhone`, nhập chuỗi SQL Injection bypass kinh điển: `' OR '1'='1` hoặc `admin' --`.
  2. Tại `txtPassword`, nhập chuỗi: `' OR '1'='1`.
  3. Bấm "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:**
  - Payload 1: `txtPhone = "' OR '1'='1"`, `txtPassword = "' OR '1'='1"`
  - Payload 2: `txtPhone = "0901234567' UNION SELECT NULL, NULL, NULL --"`, `txtPassword = "123"`
  - Payload 3: `txtPhone = "<script>alert(1)</script>"`, `txtPassword = "abc"`
- **Kết quả mong đợi:**
  - Chuỗi payload được xử lý thuần túy dưới dạng chuỗi literal (`NVARCHAR`).
  - Hệ thống không bị vượt qua xác thực; thông báo lỗi "Số điện thoại / Mã nhân viên không tồn tại trong hệ thống!".
  - Không phát sinh lỗi cú pháp SQL Server (`SqlException: Unclosed quotation mark after the character string`).
- **Tiêu chí Đạt/Không đạt:** Đạt nếu SQL Injection bị vô hiệu hóa 100%.

---

#### TC-AUTH-011: Chặn đăng nhập tài khoản nhân viên bị khóa (Kiểm tra DEF-01 / DEF-07 / DEF-CRI-01)
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm`, `DangNhapBus`, `TaiKhoanDAO`, `sp_DangNhap`
- **Cấp độ kiểm thử:** Integration Test / Security Defect Verification
- **Mục tiêu kiểm thử:** Kiểm tra nghiêm ngặt việc ngăn chặn truy cập đối với tài khoản nhân viên đã bị vô hiệu hóa hoặc cho nghỉ việc (`trangThaiHoatDong = 0`), dù người đó nhập **ĐÚNG 100% MẬT KHẨU**.
- **Điều kiện tiên quyết:** Tài khoản `NV006` (Nguyễn Văn Hùng - Nhân viên bán hàng cũ) có `soDienThoai = '0956789012'`, `matKhau` băm của `'123456'`, và `trangThaiHoatDong = 0`.
- **Các bước thực hiện:**
  1. Mở màn hình `LoginForm`.
  2. Nhập `txtPhone`: `0956789012`.
  3. Nhập `txtPassword`: `123456` (Mật khẩu chính xác).
  4. Bấm nút "ĐĂNG NHẬP".
- **Dữ liệu kiểm thử:** `txtPhone = "0956789012"`, `txtPassword = "123456"`.
- **Kết quả mong đợi theo ĐẶC TẢ AN TOÀN (Security Specification):**
  - Hệ thống **BẮT BUỘC PHẢI CHẶN ĐĂNG NHẬP**.
  - Hiển thị hộp thoại cảnh báo: "Tài khoản của nhân viên này đang bị khóa hoặc ngừng hoạt động!".
  - Không khởi tạo phiên `UserSession` và không mở `MainDashboard`.
- **Hành vi thực tế phát hiện lỗi (DEF-01 / DEF-07 / DEF-CRI-01 Evidence):**
  - Trong `sp_DangNhap` thiếu `AND tk.trangThaiHoatDong = 1`.
  - Trong `DangNhapBus.xuLyDangNhap`, khi `tk != null`, hệ thống cấp quyền đăng nhập ngay mà không kiểm tra `tk.isTrangThaiHoatDong()`.
  - Kết quả lỗi thực tế: Hệ thống báo "Đăng nhập thành công!" và mở Dashboard.
- **Tiêu chí Đạt/Không đạt:**
  - *Đạt (Pass):* Chặn đăng nhập thành công với thông báo tài khoản bị khóa.
  - *Không đạt (Fail / Defect Confirmed):* Đăng nhập thành công vào Dashboard (Xác nhận lỗi DEF-01 / DEF-CRI-01 tồn tại).
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra trạng thái hiện tại của tài khoản NV006
  SELECT maTaiKhoan, soDienThoai, vaiTro, trangThaiHoatDong
  FROM TaiKhoan
  WHERE maTaiKhoan = 'NV006';
  -- Kỳ vọng quan sát: trangThaiHoatDong = 0
  
  -- Kiểm tra kết quả trả về của sp_DangNhap
  EXEC dbo.sp_DangNhap @identifier = '0956789012', @matKhauRaw = '123456';
  -- Quan sát lỗi: Stored procedure vẫn trả về 1 dòng bản ghi thay vì tập rỗng!
  ```

---

### Nhóm 2: Mật Mã Học & Lưu Trữ Mật Khẩu (Security & Cryptography - TC-SEC)

#### TC-SEC-001: Xác minh tính toàn vẹn của hàm băm mật khẩu `fn_HashPassword` (SHA-256)
- **Phân hệ & Màn hình:** Cơ sở dữ liệu / Hàm T-SQL `dbo.fn_HashPassword`
- **Cấp độ kiểm thử:** Unit Test (Database Function)
- **Mục tiêu kiểm thử:** Xác minh hàm `fn_HashPassword` băm chuỗi thô bằng thuật toán SHA-256 trong SQL Server, trả về chuỗi 64 ký tự hex chữ thường (lowercase) và xử lý đúng giá trị NULL. Phân định rõ băm UTF-16LE 2-byte của kiểu `NVARCHAR` trong SQL Server so với băm ASCII/UTF-8 1-byte.
- **Điều kiện tiên quyết:** Hàm `dbo.fn_HashPassword` đã được biên dịch trên database `QuanLyCuaHangThuCung`.
- **Các bước thực hiện:**
  1. Thực thi truy vấn kiểm tra giá trị băm của chuỗi mẫu `'123456'`.
  2. Đối chiếu với giá trị băm thực tế trong SQL Server: `ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24` (do hàm `dbo.fn_HashPassword(@matKhau NVARCHAR(255))` nhận tham số `NVARCHAR(255)`, SQL Server truyền mảng byte UTF-16LE 2-byte vào `HASHBYTES('SHA2_256', N'123456')`, khác với chuẩn UTF-8/ASCII 1-byte cho ra `8d969...`).
  3. Kiểm tra độ dài chuỗi trả về (`LEN` và `DATALENGTH`).
  4. Kiểm tra các ký tự có hoàn toàn là chữ thường (lowercase) hay không.
  5. Kiểm tra trường hợp truyền tham số `NULL` và chuỗi rỗng `''`.
- **Dữ liệu kiểm thử:**
  - Input 1: `'123456'`
  - Input 2: `'admin@2026'`
  - Input 3: `NULL`
  - Input 4: `N'MậtKhẩuTiếngViệtCóDấu'`
- **Kết quả mong đợi:**
  - Input 1 trong SQL Server trả về chính xác: `'ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24'`.
    *(Giải thích kỹ thuật: `dbo.fn_HashPassword(@matKhau NVARCHAR(255))` nhận kiểu `NVARCHAR` 2-byte UTF-16LE, nên `HASHBYTES('SHA2_256', N'123456')` tạo chuỗi hex `ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24`. Trong khi đó, chuỗi băm `8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92` là kết quả của UTF-8/ASCII 1-byte `HASHBYTES('SHA2_256', '123456')` khi không có tiền tố N hoặc kiểu VARCHAR)*.
  - Độ dài chuỗi luôn luôn đúng bằng 64 ký tự hex (`VARCHAR(64)`).
  - Không có ký tự viết hoa (A-F).
  - Input 3 (`NULL`) trả về `NULL`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu kết quả băm trong SQL Server khớp chính xác `ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24` và độ dài đúng 64 hex lowercase.
- **Verification SQL Queries:**
  ```sql
  SELECT 
      dbo.fn_HashPassword('123456') AS HashActual,
      'ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24' AS HashExpected_SQLServer_NVARCHAR,
      '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92' AS HashExpected_Standard_ASCII,
      CASE WHEN dbo.fn_HashPassword('123456') = 'ec278a38901287b2771a13739520384d43e4b078f78affe702def108774cce24' 
           THEN N'PASSED (Khớp UTF-16LE NVARCHAR)' ELSE N'FAILED' END AS TestResult,
      LEN(dbo.fn_HashPassword('123456')) AS HashLength;
  ```

---

#### TC-SEC-002: Kiểm tra lưu trữ mật khẩu an toàn trong CSDL (Không lưu Plaintext Password)
- **Phân hệ & Màn hình:** Cơ sở dữ liệu / Bảng `TaiKhoan`, `sp_TaoTaiKhoan`, `sp_DoiMatKhau`
- **Cấp độ kiểm thử:** Unit Test / Security Compliance
- **Mục tiêu kiểm thử:** Đảm bảo toàn bộ bảng `TaiKhoan` trong CSDL không chứa bất kỳ mật khẩu dạng bản rõ (plaintext) nào; mọi thao tác tạo mới hoặc đổi mật khẩu đều bắt buộc phải lưu băm 64 ký tự.
- **Điều kiện tiên quyết:** Bảng `TaiKhoan` đã nạp dữ liệu.
- **Các bước thực hiện:**
  1. Quét toàn bộ bảng `TaiKhoan` kiểm tra cột `matKhau`.
  2. Xác minh không có bản ghi nào có độ dài mật khẩu khác 64 ký tự.
  3. Thử tạo tài khoản mới bằng `sp_TaoTaiKhoan` và kiểm tra giá trị ghi vào cột `matKhau`.
- **Dữ liệu kiểm thử:** Tạo tài khoản thử nghiệm `NV_TEST_SEC` với mật khẩu thô `'MySecretPass999'`.
- **Kết quả mong đợi:**
  - 100% bản ghi trong bảng `TaiKhoan` có cột `matKhau` là chuỗi hex 64 ký tự.
  - Không tìm thấy bất kỳ chuỗi thô nào như `'123456'`, `'admin'`, `'password'`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu không có rò rỉ mật khẩu bản rõ trong CSDL.
- **Verification SQL Queries:**
  ```sql
  -- Phát hiện tài khoản có mật khẩu không phải dạng băm 64 ký tự
  SELECT maTaiKhoan, soDienThoai, matKhau, LEN(matKhau) AS DoDai
  FROM TaiKhoan
  WHERE LEN(matKhau) <> 64 OR matKhau LIKE '%[A-Z]%';
  -- KẾT QUẢ MONG ĐỢI: 0 bản ghi (Không có mật khẩu lỗi hoặc viết hoa)
  ```

---

#### TC-SEC-003: Giới hạn số lần thử sai liên tiếp & Phòng chống Brute-Force (DEF-03 / DEF-MAJ-01)
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm`, `DangNhapBus`, `TaiKhoan`
- **Cấp độ kiểm thử:** UI Test / Security Vulnerability Assessment
- **Mục tiêu kiểm thử:** Đánh giá khả năng phòng chống tấn công dò mật khẩu tự động (Dictionary / Brute-force Attack) thông qua cơ chế đếm số lần đăng nhập sai và khóa tạm thời tài khoản.
- **Điều kiện tiên quyết:** Tài khoản `NV001` đang hoạt động bình thường.
- **Các bước thực hiện:**
  1. Mở `LoginForm`.
  2. Nhập SĐT `0901234567`.
  3. Nhập mật khẩu sai liên tiếp 5 lần: `pass1`, `pass2`, `pass3`, `pass4`, `pass5`.
  4. Quan sát phản ứng của giao diện ở lần thử thứ 5 và các lần thử tiếp theo (thử đến lần 10).
- **Dữ liệu kiểm thử:** Thử sai liên tục 10 lần trên cùng 1 tài khoản trong vòng 30 giây.
- **Kết quả mong đợi theo TIÊU CHUẨN AN TOÀN (Security Standard):**
  - Sau 5 lần nhập sai liên tiếp, hệ thống phải:
    * Khóa tạm thời tài khoản trong 5 hoặc 15 phút.
    * Hoặc yêu cầu giải mã CAPTCHA.
    * Hoặc vô hiệu hóa nút Đăng nhập kèm bộ đếm lùi thời gian (Rate Limiting Delay).
- **Hành vi thực tế phát hiện lỗi (DEF-03 / DEF-MAJ-01 Evidence):**
  - Bảng `TaiKhoan` không có trường lưu `soLanSai` hoặc `thoiGianKhoaTam`.
  - `DangNhapBus` và `LoginForm` không có bộ đếm trong bộ nhớ RAM, không có CAPTCHA, không có cơ chế delay.
  - Người dùng có thể bấm thử sai hàng nghìn lần liên tiếp mà không hề bị hệ thống chặn lại hay làm chậm.
- **Tiêu chí Đạt/Không đạt:**
  - *Đạt (Pass):* Hệ thống có cơ chế khóa tạm thời hoặc delay sau 5 lần sai.
  - *Không đạt (Fail / Defect Confirmed):* Cho phép thử sai vô hạn (Xác nhận lỗ hổng DEF-03 / DEF-MAJ-01 tồn tại).

---

### Nhóm 3: Quy Trình Quên Mật Khẩu (Password Recovery - TC-PWD)

#### TC-PWD-001: Quên mật khẩu - Xác thực SĐT và Email khớp nhau trong CSDL (Bước 1)
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm` -> Modal `moHopThoaiQuenMatKhau` (Bước 1: Xác thực)
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh luồng Bước 1 của quy trình Quên mật khẩu: Kiểm tra số điện thoại và email khớp chính xác trong bảng `TaiKhoan` và nhân viên đang hoạt động.
- **Điều kiện tiên quyết:** Tài khoản `NV001` có SĐT `0901234567` và Email `minh.quanly@miopet.vn`.
- **Các bước thực hiện:**
  1. Tại `LoginForm`, nhấp vào liên kết "Quên mật khẩu?".
  2. Cửa sổ modal "Khôi phục mật khẩu - Bước 1/3" hiển thị.
  3. Nhập `txtPhoneForgot`: `0901234567`.
  4. Nhập `txtEmailForgot`: `minh.quanly@miopet.vn`.
  5. Nhấp nút "TIẾP TỤC" (hoặc "GỬI MÃ OTP").
- **Dữ liệu kiểm thử:** `sdt = "0901234567"`, `email = "minh.quanly@miopet.vn"`.
- **Kết quả mong đợi:**
  - `DangNhapBus.xacThucQuenMatKhau()` tìm thấy bản ghi `NhanVien` hợp lệ.
  - Hệ thống sinh mã OTP 6 số ngẫu nhiên qua `DangNhapBus.sinhMaOTP()`.
  - Hiển thị hộp thoại thông báo: "Đã tìm thấy tài khoản nhân viên Lê Khãi Minh! Mã xác thực OTP đã được gửi đến email minh.quanly@miopet.vn (Để kiểm thử/demo, mã OTP là: [XXXXXX])".
  - Chuyển giao diện sang Bước 2 (Nhập OTP).
- **Tiêu chí Đạt/Không đạt:** Đạt nếu xác thực thành công và sinh mã OTP 6 số.
- **Verification SQL Queries:**
  ```sql
  SELECT tk.maTaiKhoan, tk.soDienThoai, tk.email, tk.trangThaiHoatDong, nv.hoTen
  FROM TaiKhoan tk
  JOIN NhanVien nv ON tk.maTaiKhoan = nv.maTaiKhoan
  WHERE tk.soDienThoai = '0901234567' AND tk.email = 'minh.quanly@miopet.vn';
  ```

---

#### TC-PWD-002: Quên mật khẩu - Chặn xác thực khi SĐT hoặc Email không khớp
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm` (Modal Quên mật khẩu)
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh hệ thống từ chối cấp mã OTP khi thông tin Số điện thoại và Email không khớp nhau trong CSDL.
- **Điều kiện tiên quyết:** Không có tài khoản nào sở hữu đồng thời SĐT `0901234567` và Email `hacker@fake.com`.
- **Các bước thực hiện:**
  1. Mở modal Quên mật khẩu.
  2. Nhập SĐT: `0901234567` (SĐT của Lê Khãi Minh).
  3. Nhập Email: `hacker@fake.com` (Email sai lệch).
  4. Nhấn "TIẾP TỤC".
- **Dữ liệu kiểm thử:** `sdt = "0901234567"`, `email = "hacker@fake.com"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị cảnh báo: "Thông tin Số điện thoại và Email không khớp hoặc tài khoản đang bị khóa!\nVui lòng kiểm tra lại thông tin đã đăng ký với quản lý.".
  - Không sinh mã OTP và không chuyển sang Bước 2.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu chặn thành công và không tiết lộ OTP.

---

#### TC-PWD-003: Quên mật khẩu - Nhập mã OTP 6 chữ số hợp lệ trong thời hạn 5 phút (Bước 2)
- **Phân hệ & Màn hình:** Bảo mật / Modal Quên mật khẩu (Bước 2: Nhập OTP)
- **Cấp độ kiểm thử:** Unit Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh khi người dùng nhập đúng mã OTP trong vòng 5 phút kể từ lúc sinh, hệ thống chấp nhận và chuyển sang Bước 3 (Đặt mật khẩu mới).
- **Điều kiện tiên quyết:** Đã hoàn thành Bước 1, mã OTP `123456` vừa được sinh ra (thời gian < 5 phút).
- **Các bước thực hiện:**
  1. Tại màn hình Bước 2, nhập mã OTP chính xác hiển thị trên thông báo demo vào ô `txtOtp`.
  2. Bấm nút "XÁC NHẬN OTP".
- **Dữ liệu kiểm thử:** `otpInput = otpGenerated` (Ví dụ `otp = "482915"`).
- **Kết quả mong đợi:**
  - Hệ thống xác nhận mã OTP chính xác và còn hiệu lực.
  - Chuyển giao diện sang Bước 3: Form nhập mật khẩu mới và xác nhận mật khẩu.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu chuyển bước thành công.

---

#### TC-PWD-004: Quên mật khẩu - Nhập mã OTP sai, để trống, hoặc OTP đã quá hạn 5 phút
- **Phân hệ & Màn hình:** Bảo mật / Modal Quên mật khẩu (Bước 2: Nhập OTP)
- **Cấp độ kiểm thử:** Unit Test / Edge Case Test
- **Mục tiêu kiểm thử:** Kiểm tra xử lý ngoại lệ khi nhập sai OTP hoặc nhập OTP sau khi đã hết hạn 5 phút.
- **Điều kiện tiên quyết:** Đang ở Bước 2 của quy trình quên mật khẩu.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  - *Kịch bản A (Sai 1 chữ số):* Nhập `otp = "000000"` (Khác mã đã sinh).
  - *Kịch bản B (Để trống OTP):* Nhập `otp = ""`.
  - *Kịch bản C (Hết hạn 5 phút):* Chờ quá 5 phút (300 giây) rồi mới bấm xác nhận.
- **Kết quả mong đợi:**
  - *Kịch bản A:* Báo lỗi "Mã xác thực OTP không chính xác! Vui lòng kiểm tra lại.".
  - *Kịch bản B:* Báo lỗi "Vui lòng nhập mã OTP 6 chữ số!".
  - *Kịch bản C:* Báo lỗi "Mã xác thực OTP đã hết hạn (quá 5 phút)! Vui lòng yêu cầu gửi lại mã mới.".
  - Không cho phép chuyển sang Bước 3.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu mọi trường hợp không hợp lệ đều bị chặn chính xác.

---

#### TC-PWD-005: Quên mật khẩu - Đặt lại mật khẩu mới thành công và đăng nhập lại (Bước 3)
- **Phân hệ & Màn hình:** Bảo mật / Modal Quên mật khẩu (Bước 3) -> `LoginForm`
- **Cấp độ kiểm thử:** Integration Test / End-to-End
- **Mục tiêu kiểm thử:** Xác minh người dùng đặt mật khẩu mới, CSDL cập nhật mật khẩu băm mới qua `sp_DoiMatKhau`, và người dùng đăng nhập lại thành công với mật khẩu mới.
- **Điều kiện tiên quyết:** Đã vượt qua Bước 2 xác thực OTP cho tài khoản `NV003` (`0912345678`).
- **Các bước thực hiện:**
  1. Tại ô `Mật khẩu mới`, nhập: `MatKhauMoi@2026`.
  2. Tại ô `Xác nhận mật khẩu mới`, nhập: `MatKhauMoi@2026`.
  3. Bấm "HOÀN TẤT ĐẶT LẠI MẬT KHẨU".
  4. Hệ thống thông báo thành công và đóng modal.
  5. Tại màn hình `LoginForm`, nhập SĐT `0912345678` và Mật khẩu cũ `123456` -> Bấm Đăng nhập (Kiểm tra mật khẩu cũ bị vô hiệu).
  6. Nhập lại SĐT `0912345678` và Mật khẩu mới `MatKhauMoi@2026` -> Bấm Đăng nhập.
- **Dữ liệu kiểm thử:** `newPass = "MatKhauMoi@2026"`, `confirmPass = "MatKhauMoi@2026"`.
- **Kết quả mong đợi:**
  - Bước 3 hoàn tất, CSDL cập nhật `matKhau = dbo.fn_HashPassword('MatKhauMoi@2026')`.
  - Bước 5 (Mật khẩu cũ): Báo "Mật khẩu không chính xác!".
  - Bước 6 (Mật khẩu mới): Đăng nhập thành công vào Dashboard của Lê Anh Minh.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu mật khẩu mới có hiệu lực ngay lập tức và mật khẩu cũ bị hủy.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra băm mật khẩu mới trong CSDL
  SELECT maTaiKhoan, soDienThoai, matKhau
  FROM TaiKhoan WHERE soDienThoai = '0912345678';
  
  -- Xác minh mật khẩu mới khớp với fn_HashPassword
  SELECT CASE WHEN matKhau = dbo.fn_HashPassword('MatKhauMoi@2026') 
              THEN N'KHỚP MẬT KHẨU MỚI' ELSE N'CHƯA ĐỔI' END AS KetQua
  FROM TaiKhoan WHERE soDienThoai = '0912345678';
  
  -- ROLLBACK lại mật khẩu cũ 123456 sau khi test xong
  EXEC dbo.sp_DoiMatKhau @maTaiKhoan = 'NV003', @matKhauMoiRaw = '123456';
  ```

---

### Nhóm 4: Đổi Mật Khẩu Trong Dashboard (Change Password - TC-PWD)

#### TC-PWD-006: Đổi mật khẩu trong Dashboard - Đổi thành công khi nhập đúng mật khẩu cũ
- **Phân hệ & Màn hình:** Bảo mật / `gui.MainDashboard.thucHienDoiMatKhau`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh nhân viên đang đăng nhập có thể đổi mật khẩu qua menu Dashboard khi cung cấp đúng mật khẩu hiện tại.
- **Điều kiện tiên quyết:** Đang đăng nhập tài khoản `NV001` (mật khẩu hiện tại `123456`).
- **Các bước thực hiện:**
  1. Trên thanh header hoặc avatar người dùng của `MainDashboard`, click chọn "Đổi mật khẩu".
  2. Hộp thoại Đổi mật khẩu xuất hiện gồm 3 trường nhập liệu.
  3. Nhập `Mật khẩu hiện tại`: `123456`.
  4. Nhập `Mật khẩu mới`: `QuanLy@999`.
  5. Nhập `Xác nhận mật khẩu mới`: `QuanLy@999`.
  6. Click nút "Xác nhận đổi" (hoặc "OK").
- **Dữ liệu kiểm thử:** `oldPass = "123456"`, `newPass = "QuanLy@999"`, `confirmPass = "QuanLy@999"`.
- **Kết quả mong đợi:**
  - Hệ thống thông báo: "Đổi mật khẩu thành công! Vui lòng ghi nhớ mật khẩu mới cho lần đăng nhập sau.".
  - CSDL cập nhật cột `matKhau` của `NV001`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu mật khẩu được đổi thành công trong CSDL.
- **Verification SQL Queries:**
  ```sql
  SELECT CASE WHEN matKhau = dbo.fn_HashPassword('QuanLy@999') 
              THEN N'ĐỔI THÀNH CÔNG' ELSE N'THẤT BẠI' END AS Status
  FROM TaiKhoan WHERE maTaiKhoan = 'NV001';
  
  -- Reset lại 123456 sau khi kiểm thử
  EXEC dbo.sp_DoiMatKhau @maTaiKhoan = 'NV001', @matKhauMoiRaw = '123456';
  ```

---

#### TC-PWD-007: Đổi mật khẩu trong Dashboard - Thất bại do nhập sai mật khẩu cũ
- **Phân hệ & Màn hình:** Bảo mật / `gui.MainDashboard.thucHienDoiMatKhau`, `bus.DangNhapBus.doiMatKhau`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh khi nhập sai mật khẩu hiện tại, hệ thống từ chối đổi mật khẩu và bảo toàn mật khẩu cũ.
- **Điều kiện tiên quyết:** Tài khoản `NV001` đang đăng nhập (mật khẩu đúng là `123456`).
- **Các bước thực hiện:**
  1. Mở hộp thoại Đổi mật khẩu trong Dashboard.
  2. Nhập `Mật khẩu hiện tại`: `SaiPass654`.
  3. Nhập `Mật khẩu mới`: `NewPass123`.
  4. Nhập `Xác nhận mật khẩu mới`: `NewPass123`.
  5. Bấm Xác nhận.
- **Dữ liệu kiểm thử:** `oldPass = "SaiPass654"`, `newPass = "NewPass123"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị cảnh báo: "Mật khẩu hiện tại không chính xác!".
  - Mật khẩu trong CSDL không bị thay đổi.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu từ chối đổi mật khẩu.
- **Verification SQL Queries:**
  ```sql
  SELECT CASE WHEN matKhau = dbo.fn_HashPassword('123456') 
              THEN N'MẬT KHẨU CŨ VẪN BẢO TOÀN' ELSE N'BỊ LỖI' END AS Status
  FROM TaiKhoan WHERE maTaiKhoan = 'NV001';
  ```

---

#### TC-PWD-008: Đổi mật khẩu trong Dashboard - Thất bại do mật khẩu xác nhận không khớp
- **Phân hệ & Màn hình:** Bảo mật / `gui.MainDashboard.thucHienDoiMatKhau`
- **Cấp độ kiểm thử:** Unit Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh validation chặn đổi mật khẩu khi người dùng gõ nhầm xác nhận mật khẩu mới.
- **Điều kiện tiên quyết:** Đang mở hộp thoại Đổi mật khẩu.
- **Các bước thực hiện:**
  1. Nhập `Mật khẩu hiện tại`: `123456`.
  2. Nhập `Mật khẩu mới`: `MatKhauMoi1`.
  3. Nhập `Xác nhận mật khẩu mới`: `MatKhauMoi2` (Khác ký tự cuối).
  4. Bấm Xác nhận.
- **Dữ liệu kiểm thử:** `newPass = "MatKhauMoi1"`, `confirmPass = "MatKhauMoi2"`.
- **Kết quả mong đợi:**
  - Thông báo lỗi: "Mật khẩu mới và xác nhận mật khẩu không khớp!".
  - Không gọi tầng BUS và không gửi lệnh xuống CSDL.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu chặn thành công tại UI.

---

#### TC-PWD-009: Kiểm tra bất nhất quy định độ dài mật khẩu tối thiểu (Kiểm tra DEF-11 / DEF-MIN-01)
- **Phân hệ & Màn hình:** Bảo mật / `LoginForm` vs `MainDashboard` vs `DangNhapBus`
- **Cấp độ kiểm thử:** Integration Test / Boundary Defect Verification
- **Mục tiêu kiểm thử:** Kiểm tra sự bất nhất về tiêu chuẩn độ dài mật khẩu tối thiểu giữa chức năng Quên mật khẩu tại `LoginForm` (yêu cầu >= 6 ký tự) và chức năng Đổi mật khẩu tại `MainDashboard` / `DangNhapBus` (chỉ yêu cầu >= 4 ký tự).
- **Điều kiện tiên quyết:** Tài khoản thử nghiệm đang hoạt động.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  - *Thử nghiệm 1 (Trên LoginForm - Quên mật khẩu):* Đặt mật khẩu mới có 4 ký tự: `"1234"` hoặc 5 ký tự `"12345"`.
  - *Thử nghiệm 2 (Trên MainDashboard - Đổi mật khẩu):* Đặt mật khẩu mới có 4 ký tự: `"1234"`.
- **Kết quả mong đợi theo TIÊU CHUẨN ĐỒNG NHẤT:**
  - Cả hai màn hình và tầng BUS phải thống nhất chung một quy chuẩn (khuyến nghị tối thiểu >= 6 hoặc >= 8 ký tự kèm chữ và số).
- **Hành vi thực tế phát hiện lỗi (DEF-11 / DEF-MIN-01 Evidence):**
  - Tại `LoginForm.java:419`: `if (pass.length() < 6)` -> Chặn và báo lỗi "Mật khẩu mới phải có ít nhất 6 ký tự!".
  - Tại `MainDashboard.java:751`: `if (matKhauMoi.length() < 4)` -> Cho phép đổi mật khẩu 4 ký tự `"1234"`.
  - Tại `DangNhapBus.java:153, 170`: `if (matKhauMoi.trim().length() < 4) return false;` -> Chấp nhận 4 ký tự.
  - Kết quả: Người dùng qua Dashboard có thể đặt mật khẩu cực kỳ yếu `"1234"`, nhưng khi quên mật khẩu lại bị bắt buộc >= 6 ký tự.
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Xác nhận sự lệch pha quy định 4 ký tự vs 6 ký tự (Xác nhận DEF-11 / DEF-MIN-01).

---

### Nhóm 5: Phân Quyền Truy Cập Dựa Trên Vai Trò (RBAC - TC-RBAC)

#### TC-RBAC-001: Phân quyền truy cập theo vai trò Thu ngân (NVTN)
- **Phân hệ & Màn hình:** Phân quyền / `utils.UserSession`, `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh nhân viên vai trò Thu ngân (`NVTN`) chỉ được phép truy cập các phân hệ được giao quyền (POS, Đổi trả, Lịch hẹn, Khách hàng, Bàn giao ca) và bị chặn tuyệt đối khỏi Quản lý Nhân sự, Phân ca làm việc và Tổng quan điều hành.
- **Điều kiện tiên quyết:** Đăng nhập bằng tài khoản `NV003` (`Role.NVTN`).
- **Các bước thực hiện:**
  1. Đăng nhập tài khoản `NV003`.
  2. Lần lượt click vào từng nút trên thanh menu sidebar:
     - Nhóm được phép: POS (`CARD_POS`), Đổi trả (`CARD_DOI_TRA`), Thú cưng (`CARD_THU_CUNG`), Lịch hẹn (`CARD_LICH_HEN`), Kho hàng (`CARD_KHO`), Bàn giao ca (`CARD_GIAO_CA`).
     - Nhóm bị cấm: Phân ca (`CARD_PHAN_CA`), Quản lý Nhân sự (`CARD_NHAN_VIEN`), Tổng quan Dashboard (`CARD_TONG_QUAN`).
- **Kết quả mong đợi:**
  - Các màn hình được phép: Chuyển CardLayout và hiển thị panel tương ứng bình thường.
  - Các màn hình bị cấm: Nút bấm trên sidebar bị ẩn, bị vô hiệu hóa (disabled), hoặc khi click hiển thị thông báo "Bạn không có quyền truy cập chức năng này! Vui lòng liên hệ Quản lý.".
  - `UserSession.canAccess("CARD_NHAN_VIEN")` trả về `false`.
  - `UserSession.canAccess("CARD_PHAN_CA")` trả về `false`.
  - `UserSession.canAccess("CARD_TONG_QUAN")` trả về `false`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu không có bất kỳ màn hình quản trị nào bị rò rỉ quyền truy cập cho Thu ngân.

---

#### TC-RBAC-002: Phân quyền truy cập theo vai trò Chuyên viên / KTV chăm sóc (NVCS)
- **Phân hệ & Màn hình:** Phân quyền / `utils.UserSession`, `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh nhân viên vai trò Chăm sóc (`NVCS`) chỉ truy cập hồ sơ Thú cưng, Lịch hẹn Spa, Khách sạn Pet Hotel; bị chặn vào POS bán hàng, Quản lý Nhân sự, Phân ca.
- **Điều kiện tiên quyết:** Đăng nhập bằng tài khoản `NV004` hoặc `NV005` (`Role.NVCS`).
- **Các bước thực hiện:**
  1. Đăng nhập tài khoản `NV004`.
  2. Thử truy cập `CARD_THU_CUNG`, `CARD_LICH_HEN`, `CARD_PET_HOTEL` -> Kỳ vọng được phép.
  3. Thử truy cập `CARD_POS`, `CARD_NHAN_VIEN`, `CARD_PHAN_CA` -> Kỳ vọng bị chặn.
- **Kết quả mong đợi:**
  - `UserSession.canAccess("CARD_THU_CUNG")` = `true`.
  - `UserSession.canAccess("CARD_PET_HOTEL")` = `true`.
  - `UserSession.canAccess("CARD_POS")` = `false`.
  - `UserSession.canAccess("CARD_NHAN_VIEN")` = `false`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu phân quyền `NVCS` hoạt động chính xác.

---

#### TC-RBAC-003: Phân quyền truy cập theo vai trò Bán hàng (NVBH)
- **Phân hệ & Màn hình:** Phân quyền / `utils.UserSession`, `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh nhân viên vai trò Bán hàng (`NVBH`) được phép vào POS, Đổi trả, Kho tra cứu tồn hàng, Thú cưng, Lịch hẹn; bị chặn vào Quản lý Nhân sự, Phân ca, Báo biểu doanh thu quản trị.
- **Điều kiện tiên quyết:** Khởi tạo phiên với vai trò `Role.NVBH`.
- **Các bước thực hiện:** Kiểm tra kết quả các hàm kiểm tra quyền của `UserSession`.
- **Kết quả mong đợi:**
  - `UserSession.canAccess("CARD_POS")` = `true`.
  - `UserSession.canAccess("CARD_DOI_TRA")` = `true`.
  - `UserSession.canAccess("CARD_KHO")` = `true`.
  - `UserSession.canAccess("CARD_PHAN_CA")` = `false`.
  - `UserSession.canAccess("CARD_NHAN_VIEN")` = `false`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu quyền hạn `NVBH` tuân thủ đúng ma trận phân quyền.

---

#### TC-RBAC-004: Phân quyền truy cập toàn bộ chức năng cho vai trò Quản lý (NVQL)
- **Phân hệ & Màn hình:** Phân quyền / `utils.UserSession`, `gui.MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh Quản lý hệ thống (`NVQL`) có toàn quyền truy cập 100% các màn hình và phân hệ trong hệ thống, bao gồm cả duyệt bàn giao ca, phân ca và quản lý tài khoản nhân sự.
- **Điều kiện tiên quyết:** Đăng nhập tài khoản `NV001` (`Role.NVQL`).
- **Các bước thực hiện:** Lần lượt click truy cập toàn bộ 10 phân hệ trên thanh sidebar.
- **Kết quả mong đợi:**
  - `UserSession.isQuanLy()` trả về `true`.
  - `UserSession.canAccess(cardName)` luôn trả về `true` cho mọi giá trị `cardName`.
  - Toàn bộ các phân hệ đều truy cập thành công, không có thông báo chặn quyền.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu Quản lý có toàn quyền hợp lệ.

---

#### TC-RBAC-005: Kiểm tra lỗ hổng leo thang đặc quyền Quản lý sau khi Đăng xuất (DEF-06 / DEF-MAJ-02)
- **Phân hệ & Màn hình:** Bảo mật / `utils.UserSession.clearSession`, `MainDashboard`
- **Cấp độ kiểm thử:** Integration Test / Critical Security Defect Verification
- **Mục tiêu kiểm thử:** Kiểm tra lỗ hổng bảo mật nghiêm trọng: Khi người dùng đăng xuất khỏi hệ thống (`clearSession`), vai trò mặc định bị gán thành `Role.NVQL` thay vì `null` hoặc `GUEST`, dẫn đến việc bất kỳ lời gọi kiểm tra quyền nào sau khi đăng xuất đều được cấp toàn quyền Quản trị tối cao.
- **Điều kiện tiên quyết:** Ứng dụng đang chạy; tài khoản `NV003` (Thu ngân) vừa đăng nhập.
- **Các bước thực hiện:**
  1. Đăng nhập tài khoản Thu ngân `NV003` -> Kiểm tra `UserSession.isQuanLy()` trả về `false`.
  2. Bấm nút "Đăng xuất" trên giao diện `MainDashboard`.
  3. Phương thức `UserSession.clearSession()` được kích hoạt để dọn dẹp phiên.
  4. Kiểm tra giá trị của `UserSession.getCurrentRole()` và kết quả gọi `UserSession.isQuanLy()`.
  5. Gọi thử phương thức `UserSession.canAccess("CARD_NHAN_VIEN")` và `UserSession.canAccess("CARD_TONG_QUAN")`.
- **Kết quả mong đợi theo TIÊU CHUẨN AN TOÀN (Security Specification):**
  - Sau khi đăng xuất, `currentUser` phải là `null`.
  - `currentRole` phải là `null` hoặc `Role.GUEST`.
  - `isQuanLy()` bắt buộc phải trả về `false`.
  - `canAccess(...)` bắt buộc phải trả về `false` cho tất cả các màn hình quản trị.
- **Hành vi thực tế phát hiện lỗi (DEF-06 / DEF-MAJ-02 Evidence):**
  - Xem mã nguồn `src/utils/UserSession.java`:
    * Dòng 50: `private static Role currentRole = Role.NVQL;`
    * Dòng 256: `public static void clearSession() { currentUser = null; currentRole = Role.NVQL; }`
    * Dòng 110: `if (isQuanLy()) return true;`
  - Hiện tượng lỗi thực tế: Sau khi đăng xuất, `currentRole` bị gán cứng thành `Role.NVQL`. Bất kỳ lời gọi `canAccess()` nào tiếp theo đều trả về `true`!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Xác nhận `UserSession.clearSession()` gán lại `Role.NVQL` và cấp quyền quản trị khi chưa đăng nhập (Xác nhận lỗ hổng DEF-06 / DEF-MAJ-02 tồn tại).

---

## 4. KỊCH BẢN SQL KIỂM CHỨNG & PHỤC HỒI DỮ LIỆU (SETUP / TEARDOWN SCRIPTS)

### 4.1 Tập lệnh khôi phục trạng thái chuẩn của phân hệ Tài khoản & Bảo mật:
```sql
USE QuanLyCuaHangThuCung;
GO

-- 1. Đảm bảo toàn bộ tài khoản chính đang ở trạng thái hoạt động với mật khẩu thô '123456'
UPDATE TaiKhoan 
SET matKhau = dbo.fn_HashPassword('123456'), trangThaiHoatDong = 1
WHERE maTaiKhoan IN ('NV001', 'NV002', 'NV003', 'NV004', 'NV005');

-- 2. Đảm bảo tài khoản NV006 ở trạng thái BỊ KHÓA để phục vụ kiểm thử DEF-01 / DEF-CRI-01
UPDATE TaiKhoan 
SET matKhau = dbo.fn_HashPassword('123456'), trangThaiHoatDong = 0
WHERE maTaiKhoan = 'NV006';

-- 3. Xóa các tài khoản rác sinh ra trong quá trình kiểm thử
DELETE FROM TaiKhoan WHERE maTaiKhoan LIKE 'NV_TEST%';

-- 4. Xác nhận tình trạng CSDL sẵn sàng kiểm thử
SELECT maTaiKhoan, soDienThoai, email, vaiTro, trangThaiHoatDong,
       CASE WHEN matKhau = dbo.fn_HashPassword('123456') THEN N'CHUẨN SHA-256' ELSE N'SAI MẬT KHẨU' END AS TinhTrangMatKhau
FROM TaiKhoan
ORDER BY maTaiKhoan;
GO
```
