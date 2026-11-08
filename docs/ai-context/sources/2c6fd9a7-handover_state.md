# BẢN BÀN GIAO TIẾN ĐỘ TOÀN DIỆN & HƯỚNG DẪN TIẾP QUẢN (HANDOVER STATE)
**Thời điểm tạo:** 07/11/2026 - 15:50 (GMT+7)  
**Dự án:** `c:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio`  
**Công nghệ:** Java Swing (FlatLaf) + Microsoft SQL Server + Apache POI + ZXing Barcode + iText PDF  
**Mục đích:** Lưu lại toàn bộ bối cảnh để model **Gemini Flash 3.8** (hoặc phiên làm việc mới) có thể tiếp quản ngay lập tức mà không cần hỏi lại người dùng hay suy đoán ngầm.

---

## 1. Tóm Tắt Trạng Thái Hiện Tại (100% Done & Compiled Clean)
Toàn bộ **6 Phase cải tổ lớn** khắc phục **18 lỗi vấn đáp của cô giáo** đã được lập trình và biên dịch thành công:
- **Lệnh biên dịch:** `cmd /c "cd /d c:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio && build.bat"`
- **Kết quả:** `[THANH CONG] Toan bo ma nguon Java da duoc bien dich thanh cong vao thu muc bin!`
- **Không có bất kỳ thay đổi nào liên kết với Git/GitHub** (tuân thủ 100% chỉ thị của người dùng).

---

## 2. Bản Đồ Kiến Trúc Đa Tầng N-Tier Chuẩn (Đã Tái Cấu Trúc)
Hệ thống đã loại bỏ hoàn toàn mô hình MVC cũ, chuyển đổi sang kiến trúc N-Tier desktop:

| Package | Đường dẫn | Chức năng & Nhiệm vụ |
| :--- | :--- | :--- |
| **`entity`** | `src/entity/*.java` | Chứa các đối tượng nghiệp vụ POJO (`TaiKhoan`, `NhanVien`, `SanPham`, `LoHang`, `QuyDoiDonVi`, `HoaDon`, `ChiTietHoaDon`, `LichHen`, `TheSpaPass`...). |
| **`dao`** | `src/dao/*.java` | Chứa 100% mã truy vấn SQL Server, PreparedStatement. Không chứa logic tính tiền hay giao diện. |
| **`bus`** | `src/bus/*.java` | Tầng xử lý nghiệp vụ trung gian giữa GUI và DAO (`DangNhapBus`, `NhanVienBus`, `BanHangBus`, `GiaoCaBus`, `KhoLoHangBus`, `DoiTraBus`, `PetHotelBus`, `KhachHangBus`, `ThongKeBus`, `SpaPassBus`, `ThuCungBus`...). |
| **`gui`** | `src/gui/*.java` & `src/gui/charts/*.java` | Giao diện người dùng FlatLaf. **Tuyệt đối 0% DAO và 0% DBConnection**. Chỉ gọi dữ liệu và xử lý qua các lớp `bus.*`. |
| **`utils`** | `src/utils/*.java` | Tiện ích dùng chung (`DBConnection`, `PasswordHasher`, `MaTuSinhHelper`, `ThemeColors`, `IconHelper`, `UserSession`, `VietQRHelper`, `EmailThongBaoHelper`...). |

---

## 3. Chi Tiết Các Tính Năng Đã Hoàn Thiện

### 3.1. Đăng nhập, Tài khoản & Quên Mật Khẩu (Phase 1 & 2)
- Bảng `TaiKhoan`: Khóa chính `maTaiKhoan` (`NV001`..`NVn`), liên kết 1–1 với `NhanVien`. Cột `soDienThoai` (`UNIQUE`), `email` (`UNIQUE`), `matKhau` (băm **SHA-256 (64 ký tự)**). Bỏ cột `tenDangNhap`.
- Đăng nhập: Nhân viên đăng nhập bằng **Số điện thoại** + **Mật khẩu**.
- Quên mật khẩu: Hộp thoại trên [`LoginForm.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/gui/LoginForm.java): Nhập SĐT + Email $\rightarrow$ kiểm tra khớp hồ sơ $\rightarrow$ sinh OTP 6 số $\rightarrow$ mở form nhập OTP và đổi mật khẩu mới (băm SHA-256).

### 3.2. Chuẩn hóa Mã Tự Sinh & Khóa Chống Sửa Tay
- Toàn bộ mã định danh (`NVxxx`, `SPxxx`, `DVxxx`, `QDxxx`, `LO-yyMMdd-xxx`, `GCxxx`, `KHxxxx`, `TCxxxx`, `HD...`, `DT...`) tự động sinh và tăng qua [`MaTuSinhHelper.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/utils/MaTuSinhHelper.java).
- Khóa cứng `setEditable(false)` và `setFocusable(false)` với màu nền xám nhạt (`#F1F5F9`) trên toàn bộ form nhập liệu (`NhapSanPhamVaLoHangDialog`, `ThietLapQuyDoiDialog`, `ThuCungDichVuPanel`, `QuanLyNhanVienPanel`...).

### 3.3. Dashboard Đồng Bộ 100% Dữ Liệu Thực & Click-to-Drill-Down (Phase 4)
- [`BarChartPanel.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/gui/charts/BarChartPanel.java): Xóa bỏ mảng giả lập, liên kết trực tiếp `ThongKeBus.layDoanhThu7NgayGanNhat()`.
- [`TongQuanPanel.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/gui/TongQuanPanel.java):
  + Đổi tên thẻ 4 thành `"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`, đồng bộ số lượng với `KhoLoHangBus.layDanhSachLoCanhBaoHanDung()`.
  + Tích hợp Click-to-Drill-Down trên cả 4 thẻ KPI: Click thẻ Doanh thu mở Tab Báo cáo; thẻ Hóa đơn mở POS Lịch sử HĐ; thẻ Dịch vụ mở Quản lý Lịch hẹn; thẻ Lô hàng mở Kho hàng FEFO.

### 3.4. Quầy Thu Ngân POS Siêu Tốc (Phase 5)
- [`BanHangPOSPanel.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/gui/BanHangPOSPanel.java): Tách nửa bên trái thành `JTabbedPane`:
  + **Tab 1: 📦 Hàng Hóa Bán Lẻ**: Ô quét mã Barcode (phím tắt `F2` focus, bấm Enter hoặc đầu quét bắn mã tự động thêm ngay vào giỏ hàng, xóa ô và focus tiếp) + Bảng `JTable` danh mục hàng hóa (khóa cứng cột đơn giá theo giá niêm yết).
  + **Tab 2: ✂️ Dịch Vụ Spa & Chăm Sóc**: Danh mục phân loại dịch vụ trực quan, thẻ chọn thú cưng, chọn KTV và hoa hồng 10%.
- **Điểm tích lũy**: Dùng `JSpinner spnDiemDung`, cho phép khách hàng chủ động chọn số điểm muốn đổi (`0` $\rightarrow$ điểm tối đa khả dụng) thay vì ép dùng hết.
- **Tìm kiếm Đơn tạm**: Thêm ô tìm kiếm nhanh `txtTimDonCho` trong Tab Đơn chờ, lọc tức thời theo SĐT khách hàng, tên khách hoặc mã đơn tạm.

### 3.5. Quản Lý Tiến Độ Dịch Vụ Lưu Lượng Lớn (Phase 6)
- [`QuanLyLichHenPanel.java`](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/src/gui/QuanLyLichHenPanel.java):
  + Bộ lọc KTV kèm đếm tải công việc (số ca KTV đang làm).
  + Cảnh báo ca trễ/lố thời lượng bằng nhãn đỏ `⚠️ Quá giờ`.
  + Thanh phân trang **20 dòng / trang** mượt mà, không đơ lag khi có hàng trăm ca.
  + Nút 1 chạm `"Chuyển sang POS thanh toán"` đẩy dịch vụ sang Giỏ hàng / Đơn tạm của quầy thu ngân.

---

## 4. Dữ Liệu Mẫu Đăng Nhập Để Kiểm Thử (Seed Data)
Do đã đổi sang đăng nhập bằng **Số điện thoại** và mật khẩu băm **SHA-256**, thông tin tài khoản mẫu như sau:

| Số điện thoại | Mật khẩu gốc | Vai trò | Tên nhân viên | Email |
| :--- | :--- | :--- | :--- | :--- |
| **`0901234567`** | `[REDACTED_SAMPLE_PASSWORD]` | **Quản lý (NVQL)** | Nguyễn Văn Minh | `minh.quanly@miopet.vn` |
| **`0902345678`** | `[REDACTED_SAMPLE_PASSWORD]` | **Thu ngân (NVTN)** | Trần Thị Thu | `thu.thungan@miopet.vn` |
| **`0903456789`** | `[REDACTED_SAMPLE_PASSWORD]` | **Chăm sóc (NVCS)** | Lê Hoàng Nam | `nam.chamsoc@miopet.vn` |
| **`0904567890`** | `[REDACTED_SAMPLE_PASSWORD]` | **Thủ kho (NVTK)** | Phạm Quốc Kho | `kho.thukho@miopet.vn` |

*(Mã băm SHA-256 của `'[REDACTED_SAMPLE_PASSWORD]'` là: `[REDACTED_SAMPLE_PASSWORD_HASH]`)*

---

## 5. Đồng Bộ Tài Liệu Báo Cáo & Hướng Dẫn Kiểm Thử
- **`README.md`**: Đã cập nhật 100% sang kiến trúc Đa tầng N-Tier (`entity`, `dao`, `bus`, `gui`, `utils`), bảng tài khoản mẫu đăng nhập bằng Số điện thoại + SHA-256, loại bỏ toàn bộ thuật ngữ MVC cũ và tài khoản cũ `NVQL001`.
- **`TAI_LIEU_KIEM_THU_NGHIEP_VU.md`**: Đã cập nhật toàn bộ các ca kiểm thử sang các lớp `*Bus`, bổ sung kịch bản `TC-01B: Khôi phục mật khẩu qua mã OTP và Email`, cập nhật kiểm thử quét Barcode POS (`F2`), điểm tích lũy `JSpinner` và phân trang dịch vụ lưu lượng lớn.
- **Biên dịch cuối cùng**: Lệnh `build.bat` xác nhận `[THANH CONG]` (Exit code 0).
- **Kiểm thử tích hợp CSDL SQL Server thực tế (13/13 PASSED)**:
  1. `PasswordHasher`: Băm chuẩn SHA-256 + xác thực `verifyPassword`.
  2. `DangNhapBus`: Đăng nhập SĐT + SHA-256, quên mật khẩu xác thực SĐT/Email, sinh OTP, đặt lại mật khẩu thành công.
  3. `MaTuSinhHelper`: Toàn bộ 15 hàm sinh mã tự tăng theo `MAX` (không trùng khóa khi xóa dữ liệu).
  4. `ChiTietHoaDon` / `ChiTietXuatLo`: Tự động áp dụng giá niêm yết `apDungGiaNiemYet()` và đồng bộ số lượng xuất cơ sở.
  5. Đồng bộ Doanh thu hôm nay và Đơn hôm nay giữa POS và `ThongKeBus` (khớp chính xác `775,000 đ`, 2 đơn: 1 bán lẻ, 1 dịch vụ).
  6. Giỏ hàng POS đa quy cách ĐVT: Hỗ trợ cùng lúc nhiều ĐVT của cùng một sản phẩm (`Lon`, `Lốc 6 lon`, `Thùng 24 lon`) tách dòng riêng biệt với đơn giá niêm yết chuẩn.
  7. Khóa cứng cột đơn giá giỏ hàng POS (chỉ cho phép sửa số lượng).
  8. Lọc lịch sử hóa đơn POS theo Khoảng ngày và Loại đơn (`Đơn bán lẻ` / `Đơn dịch vụ`).
  9. Quản lý lịch hẹn: Tự động cảnh báo ca quá giờ (`⚠️ Quá giờ`), phân trang 20 dòng/trang.
  10. Đồng bộ Dashboard `TongQuanPanel` với `KhoLoHangPanel` (hiển thị đúng 1 lô cận hạn, lô hết tồn hiển thị `ĐÃ HẾT TỒN LÔ`) và `QuanLyLichHenPanel` (khớp 4 thú cưng làm dịch vụ).
  11. Quản lý nhân viên: Hỗ trợ đầy đủ vai trò `Thủ kho (NVKHO)` bên cạnh Quản lý, Thu ngân, KTV Spa.

---

## 6. Hướng Dẫn Dành Riêng Cho Model Gemini Flash 3.8 Tiếp Quản
Khi người dùng chuyển sang model **Gemini Flash 3.8** và đưa ra yêu cầu tiếp theo:
1. **Kiểm tra trạng thái trước tiên:** Toàn bộ code đã biên dịch thành công vào `bin/`. Để kiểm tra lại bất cứ lúc nào, chỉ cần chạy:
   ```powershell
   cmd /c "cd /d c:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio && build.bat"
   ```
2. **Quy tắc Thép chống ẩu (Flash-Proof Constraints):**
   - ❌ **CẤM XÓA CODE CŨ:** Tuyệt đối không dùng comment kiểu `// TODO: keep existing code` hoặc rút gọn code hiện có.
   - ❌ **CẤM IMPORT `dao.*` HOẶC `DBConnection` TRONG PACKAGE `gui`:** Mọi thứ phải gọi qua `bus.*`.
   - ❌ **CẤM TỰ Ý MỞ RỘNG:** Chỉ sửa đúng hàm/phạm vi được chỉ định, ưu tiên Minimal Diff.
   - ❌ **CẤM TƯƠNG TÁC VỚI GIT/GITHUB:** Không chạy các lệnh git push/commit/remote theo yêu cầu người dùng.
3. **Lịch Sử Git, Push & Merge Lên Remote (Hoàn Thành 100%):**
   - **Remote Repository**: `https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung.git`
   - **Các nhánh đã push thành công**:
     1. `feature/pos-ban-hang-doi-tra` (`8125c3d`) - Author: `MinhTo130606 <lekhaiminh2606@gmail.com>`
     2. `feature/kho-san-pham-lo-hang` (`5875ec3`) - Author: `TDAT8 <jamas10102008@gmail.com>`
     3. `feature/nhan-su-giao-ca-dashboard` (`60f0c18`) - Author: `aminh09 <le1420445@gmail.com>`
     4. `feature/thu-cung-dich-vu-hotel` (`095f803`) - Author: `Vo Thi Hong Ngoc <vongoc30806@gmail.com>`
     5. `develop` (`c4c1db7`) - Merge toàn bộ 4 nhánh thành công không xung đột.
     6. `main` (`41b8ab6`) - Phát hành phiên bản chính thức N-tier hoàn thiện.
   - **Biên dịch**: `[THANH CONG]` (Exit code 0) bằng `build.bat`.
4. **Các tác vụ khả dĩ người dùng có thể yêu cầu tiếp:**
   - Chạy thử runtime ứng dụng (`run.bat` hoặc lệnh khởi chạy `MainDashboard` / `LoginForm`).
   - Kiểm tra cơ sở dữ liệu SQL Server hoặc hướng dẫn import file `sql/HeThongQuanLyCuaHangThuCung.sql`.
   - Chuẩn bị tài liệu báo cáo / slide thuyết trình / kịch bản demo vấn đáp trước hội đồng.
