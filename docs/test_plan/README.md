# KẾ HOẠCH KIỂM THỬ TOÀN DIỆN (COMPREHENSIVE TEST PLAN)
## HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG (N07 - PET STORE MANAGEMENT SYSTEM)

**Mã dự án:** `N07-HeThongQuanLyCuaHangThuCungMio`  
**Phiên bản tài liệu:** 1.0 (Chính thức)  
**Ngày phát hành:** 07/11/2026  
**Chế độ kiểm thử:** Comprehensive System & Regression Testing  
**Thư mục lưu trữ:** `docs/test_plan/`  

---

## MỤC LỤC
1. [Tổng Quan & Mục Tiêu Kiểm Thử](#1-tổng-quan--mục-tiêu-kiểm-thử)
2. [Phạm Vi Kiểm Thử (Test Scope)](#2-phạm-vi-kiểm-thử-test-scope)
3. [Chiến Lược Kiểm Thử (Test Strategy)](#3-chiến-lược-kiểm-thử-test-strategy)
4. [Môi Trường & Hạ Tầng Kiểm Thử (Test Environment)](#4-môi-trường--hạ-tầng-kiểm-thử-test-environment)
5. [Ma Trận Truy Xuất Nguồn Gốc & Độ Bao Phủ (Traceability & Coverage Matrix)](#5-ma-trận-truy-xuất-nguồn-gốc--độ-bao-phủ-traceability--coverage-matrix)
6. [Tóm Tắt Điều Hành Danh Mục Lỗi (Executive Defect Summary)](#6-tóm-tắt-điều-hành-danh-mục-lỗi-executive-defect-summary)
7. [Quy Trình Thực Thi & Tiêu Chuẩn Nghiệm Thu (Execution & Acceptance Criteria)](#7-quy-trình-thực-thi--tiêu-chuẩn-nghiệm-thu-execution--acceptance-criteria)
8. [Phân Công Tài Liệu Đặc Tả Test Case](#8-phân-công-tài-liệu-đặc-tả-test-case)

---

## 1. TỔNG QUAN & MỤC TIÊU KIỂM THỬ

### 1.1 Bối Cảnh Hệ Thống
Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`) là giải pháp phần mềm quản trị toàn diện dành cho chuỗi bán lẻ vật phẩm thú cưng, phòng khám - chăm sóc dịch vụ Spa/Grooming và lưu trú khách sạn thú cưng (Pet Hotel). Ứng dụng được xây dựng trên nền tảng Java Swing hiện đại kết hợp giao diện phẳng FlatLaf và hệ quản trị cơ sở dữ liệu quan hệ Microsoft SQL Server.

Nhằm bảo đảm chất lượng phần mềm, tính toàn vẹn dữ liệu kế toán - kho vận, độ an toàn bảo mật và sự ổn định tuyệt đối trước khi bàn giao và đưa vào vận hành thực tế tại các điểm bán, tài liệu này thiết lập **Kế hoạch kiểm thử toàn diện (Comprehensive Test Plan)** cho toàn bộ hệ thống.

### 1.2 Mục Tiêu Kiểm Thử Cốt Lõi
1. **Xác minh tính đúng đắn của các yêu cầu chức năng cốt lõi (Functional Correctness):**
   - Đảm bảo 100% các luồng nghiệp vụ POS (bán hàng 2 phương thức Tiền mặt / Chuyển khoản VietQR, tính tiền thối thời gian thực, nút mệnh giá nhanh).
   - Đảm bảo luồng đổi trả hàng chuyển tiếp sang POS thanh toán chênh lệch hoạt động trơn tru.
   - Đảm bảo quy trình chốt ca bàn giao lưu trạng thái "Chờ Quản lý duyệt" và phân quyền Quản lý xét duyệt chính xác.
   - Đảm bảo cơ chế quản lý kho đa quy cách, trừ kho theo nguyên tắc hạn sử dụng FEFO (First-Expired, First-Out), cảnh báo cận date và đặt hàng ROP.
   - Đảm bảo vận hành dịch vụ Spa, Thẻ Spa Pass, điều phối phòng Pet Hotel và báo cáo thống kê doanh thu.
2. **Bảo toàn tính toàn vẹn dữ liệu và giao dịch ACID (Data Integrity & ACID Transactions):**
   - Đảm bảo không xảy ra hiện tượng mất dữ liệu (data loss), lệch số liệu kế toán dòng hàng, xuất bán kho âm, hoặc partial commit giữa hóa đơn và chi tiết xuất lô.
3. **Phát hiện và cô lập toàn bộ các lỗi tiềm ẩn (Defect Detection & Isolation):**
   - Rà soát và nhận diện các lỗi nghiêm trọng (Blocker/Critical) như rò rỉ kết nối JDBC (Connection Leaks), tràn số UDF sinh mã, lỗi ép kiểu T-SQL sang ngày mới, và lỗ hổng bỏ qua trạng thái tài khoản bị khóa.
4. **Đo lường độ ổn định và trải nghiệm người dùng (Usability & Robustness):**
   - Kiểm tra khả năng xử lý ngoại lệ, thông báo lỗi thân thiện trên giao diện UI, và phản hồi tức thời dưới 1 giây cho các thao tác quầy POS.

---

## 2. PHẠM VI KIỂM THỬ (TEST SCOPE)

### 2.1 Phạm Vi Trong Kế Hoạch (In-Scope)
Kế hoạch kiểm thử bao phủ toàn bộ 6 phân hệ nghiệp vụ chính từ tầng Giao diện (UI/Swing), tầng Nghiệp vụ (BUS), tầng Truy cập Dữ liệu (DAO) đến tầng Cơ sở dữ liệu (Microsoft SQL Server):

1. **Phân hệ 1: Điểm Bán Hàng (POS), Bán Hàng Đa Phương Thức & Đổi Trả Hàng Hóa:**
   - Quản lý giỏ hàng, bảng danh mục sản phẩm/dịch vụ, tìm kiếm mã vạch EAN-13.
   - Phương thức thanh toán: "Tiền mặt" (nhập tiền khách đưa, tính tiền thối tự động, các nút gợi ý mệnh giá 100k, 200k, 500k, Đủ tiền; chặn khi thiếu tiền) và "Chuyển khoản" (modal VietQR NAPAS 24/7).
   - Tích điểm thành viên (100k = 1 điểm), tiêu điểm (1 điểm = 1,000đ), giảm giá thành viên VIP 5%.
   - Đổi trả hàng: Tra cứu hóa đơn cũ trong 7 ngày; Kịch bản A (trả hàng hoàn tiền tại chỗ); Kịch bản B (đổi sang sản phẩm khác / thêm hàng, chuyển dữ liệu và khấu trừ tiền sang POS).
2. **Phân hệ 2: Quản Lý Kho Hàng, Sản Phẩm, Lô Hàng & Hạn Sử Dụng (FEFO):**
   - Danh mục sản phẩm, đơn vị tính cơ sở và thiết lập bảng quy đổi đơn vị tính đa cấp (Lon -> Lốc -> Thùng).
   - Quản lý lô hàng nhập kho, số lô, hạn sử dụng, nhà cung cấp, giá nhập, giá bán.
   - Chiến lược xuất kho FEFO (ưu tiên lô cận hạn nhất), phân tách xuất nhiều lô (Split Lot).
   - Bộ lọc và cảnh báo hạn dùng (Hết hạn, Cận date nguy cấp <= 30 ngày, Cận date <= 60 ngày, An toàn).
   - Quản lý điểm đặt hàng lại ROP `= (d * L) + SS` và đề xuất nhập hàng.
3. **Phân hệ 3: Quản Lý Khách Hàng, Hồ Sơ Thú Cưng, Dịch Vụ Spa & Khách Sạn Pet Hotel:**
   - CRUD thông tin khách hàng, hạng thẻ, lịch sử mua sắm.
   - Quản lý hồ sơ thú cưng, số microchip, giống loài, cân nặng, tiền sử bệnh án.
   - Đặt lịch hẹn Spa/Grooming, phân công kỹ thuật viên (KTV), kiểm tra xung đột khung giờ phục vụ.
   - Quản lý thẻ Spa Pass trả trước (10 buổi), trừ lượt sử dụng khi thanh toán.
   - Quản lý phòng lưu trú Pet Hotel, sơ đồ phòng trực quan, Check-in, Check-out, phụ phí quá giờ.
4. **Phân hệ 4: Quản Lý Nhân Sự, Phân Ca Làm Việc & Bàn Giao Ca:**
   - CRUD hồ sơ nhân viên, phân quyền vai trò (RBAC: NVQL, NVBH, NVCS, NVTN), liên kết tài khoản.
   - Lập lịch phân ca làm việc (Ca Sáng, Ca Chiều, Ca Tối), xếp ca KTV dịch vụ.
   - Bàn giao ca: Chốt két tiền mặt, tính toán doanh thu hệ thống, ghi nhận tiền thực tế bàn giao, phát hiện chênh lệch thừa/thiếu, lưu trạng thái "Chờ Quản lý duyệt", Quản lý đăng nhập xét duyệt ca.
5. **Phân hệ 5: Bảo Mật Hệ Thống, Xác Thực Đăng Nhập & Đổi Mật Khẩu:**
   - Đăng nhập hệ thống bằng SĐT/Mã tài khoản và Mật khẩu; mã hóa mật khẩu SHA-256 qua UDF `fn_HashPassword`.
   - Kiểm tra trạng thái hoạt động tài khoản (`trangThaiHoatDong = 1`), chặn tài khoản đã bị khóa.
   - Quy trình Quên mật khẩu đa bước qua OTP (6 số, thời hạn 5 phút).
   - Đổi mật khẩu trong phiên đăng nhập Dashboard.
6. **Phân hệ 6: Báo Cáo & Thống Kê Doanh Thu, Tồn Kho:**
   - Tổng quan Dashboard: 3 chế độ (Vận hành, Chiến lược, Phân tích), biểu đồ Graphics2D doanh thu 7 ngày và cơ cấu ngành hàng.
   - 7 Báo biểu chuyên sâu: Doanh thu & Lợi nhuận, Top Sản phẩm bán chạy, Hiệu suất nhân viên & KTV, Khách hàng & Điểm VIP, Pet Hotel & Đổi trả, Cảnh báo hạn dùng FEFO, Báo cáo Xuất - Nhập - Tồn kho.

### 2.2 Phạm Vi Ngoài Kế Hoạch (Out-of-Scope)
Các hạng mục sau không thuộc phạm vi kiểm thử tự động hoặc nghiệm thu của dự án này:
- **Tích hợp phần cứng máy in nhiệt vật lý:** Kiểm thử lệnh in hóa đơn K80 và in phiếu đổi trả được thực hiện thông qua giao diện đồ họa xem trước `HoaDonPreviewDialog` và máy in ảo (Microsoft Print to PDF / Java Print Service), không kết nối thiết bị in POS vật lý qua cổng COM/USB.
- **Cổng thanh toán trực tuyến thực tế:** Kiểm thử VietQR sử dụng giao thức chuẩn Napas 24/7 tải mã QR hiển thị trên `VietQRDialog` và cơ chế fallback offline nội bộ, không thực hiện trừ tiền thật từ tài khoản ngân hàng thực tế.
- **Cổng gửi tin nhắn SMS Brandname/OTP viễn thông:** Kiểm thử mã OTP trong quy trình Quên mật khẩu được xác minh thông qua log hệ thống và console giả lập nhà mạng.

---

## 3. CHIẾN LƯỢC KIỂM THỬ (TEST STRATEGY)

Hệ thống áp dụng mô hình kiểm thử đa tầng (Multi-Level Testing Strategy) kết hợp kiểm thử hộp đen (Black-Box Testing) và kiểm thử hộp trắng (White-Box Testing):

```
┌─────────────────────────────────────────────────────────────┐
│               UI / SYSTEM ACCEPTANCE TESTING               │
│      (Swing Components, User Flows, Short-cuts, Look&Feel)   │
├─────────────────────────────────────────────────────────────┤
│                 INTEGRATION TESTING (ACID)                  │
│       (BUS -> DAO -> DB Transactions, Multi-table Updates)   │
├─────────────────────────────────────────────────────────────┤
│                        UNIT TESTING                         │
│  (DAO CRUD, BUS Business Calculations, SQL UDFs & SPs)      │
└─────────────────────────────────────────────────────────────┘
```

### 3.1 Cấp Độ 1: Kiểm Thử Đơn Vị (Unit Testing)
- **Tầng Cơ sở dữ liệu (T-SQL UDF & Stored Procedures):**
  * Kiểm thử các hàm băm và sinh mã tự động: `fn_HashPassword`, `fn_SinhMaHoaDon`, `fn_SinhMaPhieuDoiTra`, `fn_SinhMaKhachHang`, `fn_SinhMaSanPham`, `fn_SinhMaLoHang`, `fn_SinhMaQuyDoi`, `fn_SinhMaLichHen`.
  * Kiểm thử thủ tục xác thực: `sp_DangNhap`, `sp_DoiMatKhau`, `sp_TaoTaiKhoan`.
- **Tầng Truy cập Dữ liệu (DAO Layer):**
  * Kiểm thử các phương thức CRUD độc lập của `HoaDonDAO`, `SanPhamDAO`, `LoHangDAO`, `KhachHangDAO`, `ThuCungDAO`, `DichVuDAO`, `TheSpaPassDAO`, `NhanVienDAO`, `TaiKhoanDAO`, `BangGiaoCaDAO`, `PhanCaDAO`, `BaoCaoThongKeDAO`.
  * Đảm bảo mọi kết nối `Connection`, `PreparedStatement`, `ResultSet` đều được đóng giải phóng tài nguyên trong khối `finally` hoặc cú pháp `try-with-resources`.
- **Tầng Nghiệp vụ (BUS Layer):**
  * Kiểm thử các thuật toán tính toán độc lập: tính chiết khấu VIP (`tinhGiamGiaVIP`), tính khấu trừ điểm thưởng (`tinhTienGiamTuDiem`), tính bù trừ tiền chênh lệch đổi hàng (`tinhTongThanhToan`), thuật toán bốc lô FEFO, tính điểm đặt hàng lại ROP.

### 3.2 Cấp Độ 2: Kiểm Thử Tích Hợp (Integration Testing)
- **Kiểm thử giao dịch ACID liên tầng (Multi-Table Transactions):**
  * Quy trình thanh toán hóa đơn: Ghi đồng thời vào bảng `HoaDon`, `ChiTietHoaDon`, `ChiTietXuatLo`, trừ tồn kho `LoHang`, ghi `ChiTietDichVu`, và cập nhật điểm tích lũy `KhachHang` trong cùng 1 Transaction (`conn.setAutoCommit(false)`). Nếu bất kỳ bước nào thất bại, toàn bộ giao dịch phải được rollback 100%.
  * Quy trình đổi trả thêm hàng: Kiểm tra tính nguyên tử giữa việc lưu phiếu đổi trả và tạo hóa đơn bán mới trên POS.
  * Quy trình chốt ca & duyệt ca: Ghi nhận bàn giao ca và cập nhật phân quyền duyệt ca của Quản lý.
- **Kiểm thử rò rỉ tài nguyên mạng & CSDL (Connection Leak Auditing):**
  * Giám sát số lượng phiên kết nối vật lý trên SQL Server (`sys.dm_exec_sessions`) qua các chu kỳ thao tác nghiệp vụ lặp lại để phát hiện và ngăn chặn triệt để hiện tượng connection leak.

### 3.3 Cấp Độ 3: Kiểm Thử Hệ Thống & Giao Diện Người Dùng (UI / System Testing)
- **Kiểm thử tương tác giao diện Swing:**
  * Kiểm tra tính phản hồi thời gian thực của các sự kiện `DocumentListener`, phím bấm tắt (F1 tìm kiếm, F2 quét mã, F4 làm mới giỏ, F9 thanh toán, ESC hủy đơn).
  * Kiểm tra việc kích hoạt/vô hiệu hóa các nút chức năng (bật/tắt ô nhập tiền khi đổi phương thức thanh toán, nút gợi ý mệnh giá tiền mặt).
- **Kiểm thử phân quyền giao diện (RBAC UI Enforcement):**
  * Xác minh người dùng với từng vai trò (`NVBH`, `NVCS`, `NVTN`, `NVQL`) chỉ có thể xem và thao tác trên các Menu/Card tương ứng với thẩm quyền.

### 3.4 Kỹ Thuật Thiết Kế Ca Kiểm Thử (Test Design Techniques)
- **Phân tích giá trị biên (Boundary Value Analysis - BVA):**
  * Tiền khách đưa: `canTra - 1` (chặn), `canTra` (vừa đủ), `canTra + 1` (thối 1đ).
  * Hạn sử dụng lô hàng: Hôm qua (đã hết hạn), Hôm nay (hết hạn ngày cuối), Ngày mai (cận hạn nguy cấp), Ngày 30, Ngày 60.
  * Giới hạn hóa đơn trong ngày: Hóa đơn 98, 99 (hợp lệ), 100, 101 (kiểm tra tràn mã).
  * Thời gian giao dịch: `23:59:59.000` đến `23:59:59.999`.
- **Phân vùng tương đương (Equivalence Partitioning - EP):**
  * Phân vùng phương thức thanh toán: {Tiền mặt, Chuyển khoản} (Hợp lệ) vs {Khác, Null} (Không hợp lệ).
  * Phân vùng tài khoản: {Hoạt động, Mật khẩu đúng} (Hợp lệ) vs {Bị khóa, Mật khẩu đúng} (Phải chặn) vs {Sai mật khẩu} (Chặn).
  * Phân vùng tỷ lệ quy đổi ĐVT: {Số nguyên dương > 0} (Hợp lệ) vs {0, Số âm, Số thực} (Chặn).

---

## 4. MÔI TRƯỜNG & HẠ TẦNG KIỂM THỬ (TEST ENVIRONMENT)

Toàn bộ các ca kiểm thử phải được thực thi và thẩm định trên môi trường chuẩn hóa sau:

| Thành Phần | Cấu Hình Chuẩn Nghiệm Thu | Ghi Chú Kỹ Thuật |
|---|---|---|
| **Hệ Điều Hành** | Windows 11 Pro 64-bit / Windows 10 Pro 64-bit | Môi trường máy trạm POS chuẩn |
| **Java Platform** | OpenJDK 17 LTS (Eclipse Temurin / Oracle JDK 17) | Runtime yêu cầu: Java SE 17 (LTS) |
| **Hệ Quản Trị CSDL** | Microsoft SQL Server 2019 / 2022 (Standard/Express) | Tên CSDL: `QuanLyCuaHangThuCung` |
| **Giao Diện Đồ Họa** | FlatLaf 3.5.4 (FlatLaf IntelliJ / macOS Light theme) | Bật khử răng cưa text & vector graphics |
| **JDBC Driver** | `mssql-jdbc-12.8.1.jre11.jar` | Đặt tại thư mục `lib/` dự án |
| **Tập Lệnh Cơ Sở Dữ Liệu**| `sql/HeThongQuanLyCuaHangThuCung.sql` | Chứa toàn bộ Schema, DDL, DML, UDFs, SPs |
| **Tập Tin Cấu Hình** | `config/database.properties` | Chứa `db.url`, `db.user`, `db.password` |
| **Công Cụ Biên Dịch** | Tập lệnh `build.bat` (sử dụng `javac -encoding UTF-8`) | Biên dịch từ `src/` sang `bin/` |
| **Công Cụ Khởi Chạy** | Tập lệnh `run.bat` (khởi chạy class `gui.LoginForm`) | Thư viện trong classpath: `lib/*;bin` |

---

## 5. MA TRẬN TRUY XUẤT NGUỒN GỐC & ĐỘ BAO PHỦ (TRACEABILITY & COVERAGE MATRIX)

Dưới đây là ma trận truy xuất nguồn gốc tổng thể kết nối từ Yêu cầu Nghiệp vụ (Requirements) -> Thành phần Hệ thống (Architecture Components) -> Mức độ Kiểm thử -> Tài liệu Đặc tả Test Cases:

| Phân Hệ & Phạm Vi | Mã Yêu Cầu | Thành Phần UI (Presentation) | Thành Phần BUS (Business) | Thành Phần DAO & Database | Cấp Độ Kiểm Thử | Tệp Đặc Tả Kịch Bản |
|---|---|---|---|---|---|---|
| **1. POS, Bán Hàng & Đổi Trả** | R1, R2, R5 | `BanHangPOSPanel`<br>`DoiTraHangPanel`<br>`VietQRDialog`<br>`HoaDonPreviewDialog` | `BanHangBus`<br>`DoiTraBus` | `HoaDonDAO`<br>`SanPhamDAO`<br>`LoHangDAO`<br>`fn_SinhMaHoaDon`<br>`fn_SinhMaPhieuDoiTra` | Unit, Integration, UI | `docs/test_plan/TC_POS_BAN_HANG_DOI_TRA.md` |
| **2. Quản Lý Kho & Lô Hàng FEFO** | R4, R5 | `KhoLoHangPanel`<br>`NhapSanPhamVaLoHangDialog`<br>`ThietLapQuyDoiDialog` | `KhoLoHangBus` | `SanPhamDAO`<br>`LoHangDAO`<br>`fn_SinhMaSanPham`<br>`fn_SinhMaLoHang`<br>`fn_SinhMaQuyDoi` | Unit, Integration, UI | `docs/test_plan/TC_KHO_SAN_PHAM_LO_HANG.md` |
| **3. Khách Hàng, Spa & Pet Hotel** | R4, R5 | `ThuCungDichVuPanel`<br>`PetHotelPanel`<br>`DatLichHenDialog`<br>`ChonDichVuDialog` | `ThuCungBus`<br>`SpaPassBus`<br>`PetHotelBus` | `KhachHangDAO`<br>`ThuCungDAO`<br>`DichVuDAO`<br>`TheSpaPassDAO`<br>`LichHenDAO`<br>`fn_SinhMaLichHen` | Unit, Integration, UI | `docs/test_plan/TC_KHACH_HANG_THU_CUNG_DICH_VU.md` |
| **4. Quản Lý Nhân Sự & Giao Ca** | R3, R5 | `QuanLyNhanVienPanel`<br>`BanGiaoCaPanel`<br>`DuyetBanGiaoCaDialog` | `NhanVienBus`<br>`GiaoCaBus`<br>`PhanCaBus` | `NhanVienDAO`<br>`BangGiaoCaDAO`<br>`PhanCaDAO`<br>`fn_SinhMaNhanVien` | Unit, Integration, UI | `docs/test_plan/TC_NHAN_SU_GIAO_CA.md` |
| **5. Bảo Mật, Đăng Nhập & Đổi MK** | R4, R5 | `LoginForm`<br>`MainDashboard`<br>`QuenMatKhauDialog` | `DangNhapBus`<br>`UserSession` | `TaiKhoanDAO`<br>`sp_DangNhap`<br>`sp_DoiMatKhau`<br>`sp_TaoTaiKhoan`<br>`fn_HashPassword` | Unit, Integration, UI | `docs/test_plan/TC_BAO_MAT_DANG_NHAP.md` |
| **6. Báo Cáo & Thống Kê** | R4, R5 | `TongQuanPanel`<br>`BaoCaoThongKeDialog`<br>`BarChartPanel`<br>`DonutChartPanel` | `ThongKeBus` | `BaoCaoThongKeDAO`<br>Bảng: `HoaDon`, `ChiTietXuatLo`, `BangPhanCong` | Unit, Integration, UI | `docs/test_plan/TC_BAO_CAO_THONG_KE.md` |

---

## 6. TÓM TẮT ĐIỀU HÀNH DANH MỤC LỖI (EXECUTIVE DEFECT SUMMARY)

Qua quá trình rà soát độc lập và kiểm tra chuyên sâu mã nguồn Java Swing, tầng BUS, DAO và CSDL SQL Server của 3 đoàn khảo sát (Explorer Survey 1, 2, 3), hệ thống đã phát hiện và ghi nhận tổng cộng **49 lỗi kỹ thuật và lỗ hổng hệ thống**.

### 6.1 Phân Bố Lỗi Theo Mức Độ Nghiêm Trọng (By Severity)
- **Blocker (Lỗi chặn đứng hệ thống):** **7 lỗi** (14.3%) — Gây sập ứng dụng, tê liệt thanh toán POS, rò rỉ socket kết nối CSDL, hoặc vi phạm nghiêm trọng an toàn thú y.
- **Critical (Lỗi nghiêm trọng):** **12 lỗi** (24.5%) — Lỗ hổng bỏ qua tài khoản bị khóa, trục lợi điểm thưởng vô tận, lỗi ép kiểu T-SQL sang ngày mới, bất đồng bộ giao dịch và thất thu tài chính.
- **Major (Lỗi mức độ lớn):** **23 lỗi** (46.9%) — Sai lệch số liệu kế toán dòng hàng, tính trùng doanh thu, thiếu chứng từ nhập kho, rủi ro tấn công brute-force, và vi phạm kiến trúc.
- **Minor (Lỗi nhỏ / Cải thiện):** **7 lỗi** (14.3%) — Sai lệch biên mili-giây, bất nhất độ dài mật khẩu, thiếu thông báo lỗi ngoại lệ.

```
┌─────────────────────────────────────────────────────────────┐
│                 BIỂU ĐỒ PHÂN BỐ LỖI HỆ THỐNG                │
├───────────────────┬──────────┬──────────┬───────────────────┤
│ Mức độ            │ Số lượng │ Tỷ lệ    │ Trạng thái xử lý  │
├───────────────────┼──────────┼──────────┼───────────────────┤
│ 🔴 Blocker        │    7     │  14.3%   │ Bắt buộc sửa ngay │
│ 🟠 Critical       │   12     │  24.5%   │ Bắt buộc sửa ngay │
│ 🟡 Major          │   23     │  46.9%   │ Khắc phục trước go-live│
│ 🟢 Minor          │    7     │  14.3%   │ Tối ưu hóa        │
├───────────────────┼──────────┼──────────┼───────────────────┤
│ TỔNG CỘNG         │   49     │ 100.0%   │ Đã lập danh mục   │
└───────────────────┴──────────┴──────────┴───────────────────┘
```

### 6.2 Phân Bố Lỗi Theo Phân Hệ Nghiệp Vụ
1. **POS & Bán Hàng, Đổi Trả:** 9 lỗi (gồm DEF-BLK-01, DEF-CRI-02, DEF-CRI-04, DEF-CRI-06, DEF-CRI-11, DEF-CRI-12, DEF-MAJ-04, DEF-MAJ-05, DEF-MIN-02).
2. **Quản Lý Kho & Lô Hàng FEFO:** 8 lỗi (gồm DEF-BLK-02, DEF-BLK-06, DEF-MAJ-04, DEF-MAJ-10, DEF-MAJ-11, DEF-MAJ-12, DEF-MIN-05, DEF-MAJ-23).
3. **Khách Hàng, Thú Cưng, Spa & Hotel:** 12 lỗi (gồm DEF-BLK-04, DEF-BLK-07, DEF-CRI-03, DEF-CRI-07, DEF-CRI-08, DEF-CRI-09, DEF-MAJ-13, DEF-MAJ-14, DEF-MAJ-15, DEF-MAJ-16, DEF-MAJ-17, DEF-MAJ-18, DEF-MIN-06, DEF-MIN-07).
4. **Nhân Sự, Phân Ca & Bàn Giao Ca:** 4 lỗi (gồm DEF-BLK-05, DEF-MAJ-19, DEF-MAJ-20, DEF-MAJ-21).
5. **Bảo Mật, Đăng Nhập & Phân Quyền:** 5 lỗi (gồm DEF-CRI-01, DEF-MAJ-01, DEF-MAJ-03, DEF-MIN-01, DEF-MAJ-24).
6. **Báo Cáo & Thống Kê:** 8 lỗi (gồm DEF-BLK-03, DEF-CRI-05, DEF-CRI-10, DEF-MAJ-07, DEF-MAJ-08, DEF-MAJ-09, DEF-MIN-03, DEF-MIN-04).
7. **Toàn Cục Cơ Sở Dữ Liệu & Hạ Tầng:** 3 lỗi (gồm DEF-MAJ-22, DEF-MAJ-23, DEF-MAJ-24).

*(Chi tiết từng lỗi, nguyên nhân kỹ thuật, kịch bản tái hiện, câu truy vấn SQL kiểm chứng và giải pháp khắc phục được đặc tả đầy đủ trong tệp `docs/test_plan/DEFECT_CATALOG.md`)*.

---

## 7. QUY TRÌNH THỰC THI & TIÊU CHUẨN NGHIỆM THU (EXECUTION & ACCEPTANCE CRITERIA)

### 7.1 Tiêu Chuẩn Bắt Đầu Kiểm Thử (Entry Criteria)
1. Mã nguồn dự án `N07-HeThongQuanLyCuaHangThuCungMio` biên dịch thành công qua `build.bat` không có lỗi cú pháp (`exit 0`).
2. Cơ sở dữ liệu SQL Server đã nạp đầy đủ schema và dữ liệu mẫu chuẩn từ `sql/HeThongQuanLyCuaHangThuCung.sql`.
3. Tệp cấu hình `config/database.properties` trỏ đúng kết nối máy chủ CSDL kiểm thử.
4. Toàn bộ tài liệu đặc tả kịch bản kiểm thử (`docs/test_plan/TC_*.md`) đã được phê duyệt.

### 7.2 Tiêu Chuẩn Tạm Dừng & Tiếp Tục (Suspension & Resumption Criteria)
- **Tạm dừng (Suspension):** Quá trình kiểm thử bị tạm dừng ngay lập tức nếu phát sinh lỗi Blocker làm sập cơ sở dữ liệu, rò rỉ hết kết nối TCP, hoặc không thể khởi động giao diện đăng nhập `LoginForm`.
- **Tiếp tục (Resumption):** Chỉ được tiếp tục sau khi lỗi Blocker được cô lập, sửa chữa mã nguồn, biên dịch lại thành công và kiểm tra hồi quy xác nhận đã giải quyết xong.

### 7.3 Tiêu Chuẩn Nghiệm Thu & Đạt/Không Đạt (Pass/Fail Criteria)
Một ca kiểm thử (Test Case) được đánh giá là **ĐẠT (PASS)** khi thỏa mãn đồng thời các điều kiện:
1. **Kết quả quan sát trên giao diện (Observed UI Result):** Khớp 100% với Kết quả mong đợi (Expected Result) trong tài liệu đặc tả test case.
2. **Tính toàn vẹn dữ liệu CSDL (Database State):** Câu truy vấn SQL kiểm chứng (Verification Query) trả về dữ liệu chính xác, các ràng buộc khóa ngoại, tồn kho, điểm thưởng, và tiền tệ được cập nhật đồng nhất.
3. **Không phát sinh ngoại lệ ngầm (No Unhandled Exceptions):** Không có lỗi runtime (`NullPointerException`, `SQLException`, `ArithmeticException`) xuất hiện trong console log hoặc command line.

### 7.4 Tiêu Chuẩn Kết Thúc Kiểm Thử (Exit / Acceptance Criteria)
Hệ thống được chứng nhận sẵn sàng đưa vào vận hành thực tế khi thỏa mãn các điều kiện:
- [ ] 100% các ca kiểm thử thuộc phạm vi In-Scope đã được thực thi.
- [ ] **0 lỗi mức Blocker** tồn tại trong hệ thống.
- [ ] **0 lỗi mức Critical** tồn tại trong hệ thống.
- [ ] Tối thiểu **95% các lỗi mức Major** đã được giải quyết hoặc có giải pháp xử lý tạm thời được quản lý phê duyệt.
- [ ] Toàn bộ các ca kiểm thử hồi quy (Regression Test) cho các chức năng cốt lõi (POS, Đổi trả, Chốt ca, FEFO) chạy đạt 100%.

---

## 8. PHÂN CÔNG TÀI LIỆU ĐẶC TẢ TEST CASE

Chi tiết từng ca kiểm thử, dữ liệu biên, các bước thực hiện và truy vấn SQL kiểm chứng được phân rã thành các tệp tài liệu chuyên biệt dưới đây:

| Tên Tệp Tài Liệu | Nội Dung Chuyên Sâu | Phân Hệ |
|---|---|---|
| `docs/test_plan/DEFECT_CATALOG.md` | Danh mục tổng hợp 49 lỗi kỹ thuật & lỗ hổng hệ thống chi tiết | Toàn hệ thống |
| `docs/test_plan/TC_POS_BAN_HANG_DOI_TRA.md` | Kịch bản kiểm thử Bán hàng POS, Thanh toán Tiền mặt/VietQR, Đổi trả hàng | Phân hệ 1 |
| `docs/test_plan/TC_KHO_SAN_PHAM_LO_HANG.md` | Kịch bản kiểm thử Quản lý Kho, ĐVT quy đổi, Lô hàng, Hạn dùng FEFO, ROP | Phân hệ 2 |
| `docs/test_plan/TC_KHACH_HANG_THU_CUNG_DICH_VU.md` | Kịch bản kiểm thử Khách hàng, Thú cưng, Lịch hẹn Spa, Thẻ Spa Pass, Pet Hotel | Phân hệ 3 |
| `docs/test_plan/TC_NHAN_SU_GIAO_CA.md` | Kịch bản kiểm thử Quản lý Nhân sự, Phân ca làm việc, Bàn giao ca & Duyệt ca | Phân hệ 4 |
| `docs/test_plan/TC_BAO_MAT_DANG_NHAP.md` | Kịch bản kiểm thử Xác thực Đăng nhập, Băm SHA-256, Quên mật khẩu OTP, Đổi MK | Phân hệ 5 |
| `docs/test_plan/TC_BAO_CAO_THONG_KE.md` | Kịch bản kiểm thử Dashboard Tổng quan, 7 Báo biểu Quản trị, Xuất-Nhập-Tồn | Phân hệ 6 |

---
*Tài liệu Kế hoạch kiểm thử được ban hành bởi Nhóm Kiểm thử Phần mềm N07.*
