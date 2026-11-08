# KẾ HOẠCH KIỂM THỬ CHI TIẾT: PHÂN HỆ NHÂN SỰ, PHÂN CA & BÀN GIAO CA THU NGÂN
**Mã tài liệu:** `TC-STP-NS-PC-GC`  
**Dự án:** N07 - Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)  
**Tác nhân thực hiện:** Writer 3 (teamwork_test_writer)  
**Phạm vi:** Quản lý Nhân sự & Phân quyền RBAC, Ma trận Phân ca làm việc tuần (Timetable Matrix), Quy trình Chốt ca & Bàn giao két tiền mặt, Đối soát chênh lệch, Phân quyền Quản lý xét duyệt ca và In biên bản K80.  
**Ngày ban hành:** 07/11/2026  
**Trạng thái:** Sẵn sàng kiểm thử (Test Ready)  

---

## I. TỔNG QUAN PHÂN HỆ & MA TRẬN TRUY XUẤT (TRACEABILITY MATRIX)

### 1.1 Phạm vi kiểm thử & Các thành phần liên quan
*   **Giao diện (GUI):**
    *   `QuanLyNhanVienPanel.java`: Quản lý danh sách nhân viên dạng thẻ (Staff Cards), thêm mới, sửa thông tin, đổi vai trò, khóa/mở khóa tài khoản, cấp lại mật khẩu SHA-256.
    *   `LichPhanCaPanel.java`: Thời khóa biểu ma trận tuần (4 ca x 7 ngày), thẻ phân ca bo tròn, menu chuột phải điều phối nhanh (xếp ca, đổi ca, sửa ghi chú, xóa ca), xuất file Excel.
    *   `BanGiaoCaPanel.java`: Biên bản đối soát két tiền mặt (Panel trái), Lịch sử bàn giao ca và Hộp thoại Quản lý duyệt két đối soát (Panel phải), xem và in biên bản K80.
*   **Nghiệp vụ (BUS):**
    *   `bus.NhanVienBus.java`: Điều phối thêm, cập nhật, đổi vai trò, khóa/mở khóa, reset mật khẩu, lấy danh sách KTV.
    *   `bus.PhanCaBus.java`: Điều phối ma trận phân ca, kiểm tra trùng ca trong ngày, quy tắc 2 ca liên tiếp, sinh mã phân công.
    *   `bus.GiaoCaBus.java`: Điều phối mở ca, tính doanh thu tiền mặt, chuyển khoản, hoàn trả, lưu biên bản và duyệt ca.
*   **Truy cập dữ liệu (DAO):**
    *   `dao.NhanVienDAO.java`, `dao.PhanCaDAO.java`, `dao.BangGiaoCaDAO.java`, `dao.TaiKhoanDAO.java`.
*   **Cơ sở dữ liệu (SQL Server):**
    *   Bảng: `NhanVien`, `TaiKhoan`, `CaLamViec`, `BangPhanCong`, `BangGiaoCa`, `HoaDon`, `PhieuDoiTra`.
    *   Thủ tục & Hàm: `sp_DangNhap`, `sp_DoiMatKhau`, `fn_HashPassword`, `fn_SinhMaNhanVien`, `fn_SinhMaGiaoCa`.

### 1.2 Ma trận Kiểm thử Phân hệ (Test Traceability Matrix)

| Nhóm Chức Năng | Mã Test Case | Cấp Độ | Màn Hình / Lớp Mục Tiêu | Mục Tiêu Chính & Khiếm Khuyết Liên Quan |
| :--- | :--- | :--- | :--- | :--- |
| **Quản lý Nhân sự & Phân quyền** | `TC-NV-001` | Integration | `NhanVienBus.themNhanVien` | Thêm nhân viên mới đầy đủ thông tin, mã tự sinh, mật khẩu băm SHA-256, phân vai trò RBAC (Happy Path) |
| | `TC-NV-002` | Unit/DAO | `NhanVienDAO` (insert, update, delete) | Kiểm tra rò rỉ kết nối JDBC thiếu `conn.close()` trong khối finally (**DEF-03 / DEF-BLK-05**) |
| | `TC-NV-003` | Integration | `NhanVienDAO.updateNhanVien` | Cập nhật thông tin nhân viên (Họ tên, chức vụ, SĐT, Email) đồng bộ giữa `NhanVien` và `TaiKhoan` |
| | `TC-NV-004` | Integration | `NhanVienBus.doiVaiTro` | Điều chỉnh vai trò tài khoản nhân viên (`NVBH` -> `NVTN` -> `NVQL` -> `NVCS`) |
| | `TC-NV-005` | Integration | `NhanVienBus.toggleTrangThai` | Khóa và Mở khóa tài khoản nhân viên (Đổi cờ `trangThaiHoatDong`) |
| | `TC-NV-006` | Security/Bug | `sp_DangNhap` / `DangNhapBus` | Lỗ hổng tài khoản nhân viên bị khóa nhưng vẫn đăng nhập thành công (**DEF-01 / DEF-07 / DEF-CRI-01**) |
| | `TC-NV-007` | Integration | `NhanVienBus.resetMatKhau` | Cấp lại mật khẩu mặc định tự động băm SHA-256 an toàn trong CSDL |
| | `TC-NV-008` | Integration | `QuanLyNhanVienPanel` -> `NhanVienDAO` | Xóa nhân viên vướng khóa ngoại FK và kiểm tra lỗi im lặng không thông báo (**DEF-20 / DEF-MAJ-19**) |
| **Phân ca làm việc** | `TC-PC-001` | UI/Integration| `LichPhanCaPanel` -> `PhanCaBus` | Xếp ca trực mới cho nhân viên theo mô hình Thời khóa biểu ma trận tuần (Happy Path) |
| | `TC-PC-002` | Unit/Logic | `PhanCaBus.xepCaLamViec` | Chặn xếp trùng ca làm việc của cùng một nhân viên trong cùng một ngày (Negative Test) |
| | `TC-PC-003` | Unit/Logic | `PhanCaBus.laHaiCaLienTiep` | Giới hạn tối đa 2 ca liên tiếp trong ngày khi có sự phê duyệt của Quản lý |
| | `TC-PC-004` | Integration | `PhanCaBus.doiCaPhanCong` | Đổi ca làm việc và cập nhật nhiệm vụ phân công (Mở quầy, grooming, bán lẻ, kiểm kho) |
| | `TC-PC-005` | Integration | `PhanCaBus.xoaPhanCong` | Xóa phân công ca làm việc và đồng bộ cập nhật trên CSDL |
| | `TC-PC-006` | Unit/Bug | `PhanCaBus.xepCaLamViec` | Xung đột sinh mã phân công (`maPC = "PC" + (size() + 1)`) sau khi xóa phân công ca (**DEF-19 / DEF-MAJ-18**) |
| | `TC-PC-007` | UI/Manual | `LichPhanCaPanel` (Filters) | Kiểm tra bộ lọc cá nhân hóa "Chỉ xem lịch của tôi" vs Lọc theo nhóm vai trò |
| | `TC-PC-008` | Integration | `LichPhanCaExcelExporter` | Xuất bảng ma trận lịch làm việc tuần ra tệp Excel (.xlsx) |
| **Bàn giao ca & Đối soát két** | `TC-GC-001` | Integration | `GiaoCaBus.layCaBanGiaoGanNhat` | Mở ca làm việc & Tự động kế thừa tiền đầu ca từ ca liền trước (`tienDauCa = caGanNhat.tienCuoiCaThucTe`) |
| | `TC-GC-002` | Integration | `BangGiaoCaDAO` (tinhDoanhThu) | Tự động tính toán doanh thu tiền mặt, chuyển khoản và tiền hoàn trả theo thời gian thực từ CSDL |
| | `TC-GC-003` | UI/Logic | `BanGiaoCaPanel.tinhChenhLech` | Đối soát két tiền - Kịch bản Khớp két 100% (Chênh lệch = 0 đ, nhãn xanh lá) (Happy Path) |
| | `TC-GC-004` | UI/Logic | `BanGiaoCaPanel.tinhChenhLech` | Đối soát két tiền - Kịch bản Thừa tiền mặt (Chênh lệch > 0 đ, nhãn cam, bắt buộc giải trình) |
| | `TC-GC-005` | UI/Edge | `BanGiaoCaPanel.xacNhanBanGiaoCa`| Đối soát két tiền - Kịch bản Thiếu tiền mặt (Chênh lệch < 0 đ, chặn chốt ca nếu để trống giải trình) |
| | `TC-GC-006` | UI/Boundary | `BanGiaoCaPanel.xacNhanBanGiaoCa`| Kiểm tra nhập liệu tiền âm `-50,000` bị regex lọc mất dấu trừ thành `+50,000`, ký tự chữ, để trống |
| | `TC-GC-007` | Integration | `BanGiaoCaPanel.xacNhanBanGiaoCa`| Thu ngân chốt ca lưu biên bản trạng thái `"⏳ Chờ Quản lý duyệt"` vào CSDL và đăng xuất đổi ca |
| | `TC-GC-008` | Security/Adv | `BanGiaoCaPanel`, `UserSession` | Kiểm soát phân quyền RBAC & Kiểm thử đối kháng leo thang đặc quyền sau khi đăng xuất chốt ca (**DEF-06 / DEF-MAJ-02**) |
| | `TC-GC-009` | Integration | `BanGiaoCaPanel.duyetBienBanGiaoCa` | Quản lý (`NVQL`/`admin`) đăng nhập, kiểm tra số liệu két và bấm phê duyệt ca thành công |
| | `TC-GC-010` | UI/Integration| `BanGiaoCaPanel.hienThiBienBanGiaoCaK80`| Xuất và hiển thị Biên bản Đối soát & Bàn giao ca chuẩn khổ giấy nhiệt K80 |

---

## II. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (TEST CASES SPECIFICATION)

---

### PHẦN 1: QUẢN LÝ NHÂN SỰ, PHÂN QUYỀN RBAC & BẢO MẬT

#### TC-NV-001: Thêm mới nhân viên và tài khoản người dùng thành công (Happy Path)
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `QuanLyNhanVienPanel.moDialogThemNhanVien`, `NhanVienBus.themNhanVien`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chức năng thêm mới nhân sự:
    *   Hệ thống tự động phát sinh mã nhân viên mới `NVxxx` qua hàm `fn_SinhMaNhanVien()`.
    *   Mật khẩu khởi tạo được tự động băm SHA-256 (64 ký tự hex) trước khi lưu vào bảng `TaiKhoan`.
    *   Dữ liệu được lưu đồng thời vào cả 2 bảng `NhanVien` và `TaiKhoan` trong 1 Transaction an toàn.
*   **Điều kiện tiên quyết:**
    *   Số điện thoại `0934567890` và email `phuong.tt@miopet.vn` chưa tồn tại trong hệ thống.
*   **Các bước thực hiện:**
    1.  Mở màn hình `QuanLyNhanVienPanel`.
    2.  Bấm nút "+ Thêm nhân viên".
    3.  Nhập thông tin:
        *   Họ và tên: `Trần Thu Phương`
        *   Chức vụ: `Nhân viên Thu ngân chính`
        *   Số điện thoại: `0934567890`
        *   Email: `phuong.tt@miopet.vn`
        *   Mật khẩu khởi tạo: `Pass@123`
        *   Phân quyền vai trò: `NVTN` (Thu ngân)
    4.  Bấm "OK".
*   **Dữ liệu kiểm thử:**
    *   `hoTen = "Trần Thu Phương"`, `chucVu = "Nhân viên Thu ngân chính"`, `sdt = "0934567890"`, `email = "phuong.tt@miopet.vn"`, `matKhau = "Pass@123"`, `vaiTro = "NVTN"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại hiển thị thông báo thêm thành công kèm mã nhân viên mới (ví dụ `NV006`).
    *   Bảng `NhanVien` có 1 bản ghi mới với `maNhanVien = 'NV006'`.
    *   Bảng `TaiKhoan` có 1 bản ghi với `maTaiKhoan = 'NV006'`, `soDienThoai = '0934567890'`, `vaiTro = 'NVTN'`, `trangThaiHoatDong = 1`.
    *   Mật khẩu trong `TaiKhoan` được băm SHA-256 độ dài đúng 64 ký tự (không lưu văn bản thuần `Pass@123`).
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Cả 2 bảng được ghi nhận đồng bộ, mật khẩu được băm bảo mật.
    *   *Không đạt:* Bị lỗi khóa ngoại, mật khẩu để lộ chuỗi gốc, hoặc thiếu bản ghi ở 1 trong 2 bảng.
*   **Verification SQL Queries:**
```sql
SELECT nv.maNhanVien, nv.hoTen, nv.chucVu, nv.soDienThoai, tk.vaiTro, tk.matKhau, tk.trangThaiHoatDong
FROM NhanVien nv
JOIN TaiKhoan tk ON nv.maTaiKhoan = tk.maTaiKhoan
WHERE nv.soDienThoai = '0934567890';
-- Kỳ vọng: vaiTro = 'NVTN', trangThaiHoatDong = 1, LEN(matKhau) = 64.
```

---

#### TC-NV-002: Kiểm tra rò rỉ kết nối JDBC trong NhanVienDAO (DEF-03 / DEF-BLK-05 Verification)
*   **Phân hệ & Màn hình:** Tầng DAO / `dao.NhanVienDAO.java` (`insertNhanVien`, `updateNhanVien`, `deleteNhanVien`).
*   **Cấp độ kiểm thử:** Unit / Connection Leak Audit.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-03 / DEF-BLK-05**:
    *   Trong `insertNhanVien` (dòng 161-165):
        ```java
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
        ```
    *   Tương tự tại `updateNhanVien` (dòng 203-207) và `deleteNhanVien` (dòng 239-243).
    *   Cả 3 phương thức quản lý nhân sự quan trọng này đều hoàn toàn thiếu lệnh `conn.close()` trong khối `finally`. Mỗi thao tác nhân sự sẽ bỏ quên một socket kết nối TCP vật lý trên SQL Server.
*   **Điều kiện tiên quyết:** Quyền truy vấn DMV `sys.dm_exec_sessions`.
*   **Các bước thực hiện:**
    1.  Chạy truy vấn SQL đếm số phiên JDBC hiện tại.
    2.  Thực hiện 10 thao tác cập nhật nhân viên thông qua `nhanVienDAO.updateNhanVien()`.
    3.  Chạy lại truy vấn SQL đếm số phiên JDBC.
*   **Dữ liệu kiểm thử:** Cập nhật lặp lại thông tin cho `NV002`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại (Bug Trigger):* Số phiên kết nối tăng thêm 10 và giữ nguyên trạng thái mở -> **Xác nhận khiếm khuyết DEF-03 / DEF-BLK-05 (Blocker)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Phát hiện thiếu `conn.close()`.
*   **Verification SQL Queries:**
```sql
SELECT session_id, host_name, program_name, client_interface_name, status, login_time
FROM sys.dm_exec_sessions
WHERE program_name LIKE '%JDBC%' OR client_interface_name LIKE '%JDBC%';
```

---

#### TC-NV-003: Cập nhật thông tin nhân viên đồng bộ hai bảng NhanVien và TaiKhoan
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `NhanVienDAO.updateNhanVien`.
*   **Cấp độ kiểm thử:** Integration Test (DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra tính toàn vẹn khi sửa đổi thông tin: khi nhân viên đổi số điện thoại hoặc email, hệ thống phải cập nhật đồng thời ở cả bảng `NhanVien` và bảng `TaiKhoan` (vì SĐT trong `TaiKhoan` là định danh đăng nhập).
*   **Điều kiện tiên quyết:** Nhân viên `NV002` (`Lê Anh Minh`) có SĐT cũ `0901234567`.
*   **Các bước thực hiện:**
    1.  Khởi tạo đối tượng `NhanVien` với mã `NV002`.
    2.  Đổi SĐT mới: `0909888999`, Email mới: `minh.la.new@miopet.vn`.
    3.  Gọi `nhanVienDAO.updateNhanVien(nv)`.
*   **Dữ liệu kiểm thử:** `maNhanVien = "NV002"`, `sdtMoi = "0909888999"`, `emailMoi = "minh.la.new@miopet.vn"`.
*   **Kết quả mong đợi:**
    *   Phương thức trả về `true`.
    *   Cả 2 bảng `NhanVien` và `TaiKhoan` đều ghi nhận SĐT mới `0909888999`.
    *   Nhân viên có thể đăng nhập bằng số điện thoại mới.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Đồng bộ dữ liệu 100% giữa hai bảng.
*   **Verification SQL Queries:**
```sql
SELECT nv.maNhanVien, nv.soDienThoai AS sdt_NV, tk.soDienThoai AS sdt_TK, nv.email AS mail_NV, tk.email AS mail_TK
FROM NhanVien nv
JOIN TaiKhoan tk ON nv.maTaiKhoan = tk.maTaiKhoan
WHERE nv.maNhanVien = 'NV002';
-- Kỳ vọng: sdt_NV = sdt_TK = '0909888999'.
```

---

#### TC-NV-004: Điều chỉnh phân quyền vai trò tài khoản nhân viên (RBAC)
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `QuanLyNhanVienPanel.moDialogDoiVaiTro`, `NhanVienBus.doiVaiTro`.
*   **Cấp độ kiểm thử:** Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra chức năng nâng cấp/chuyển đổi vai trò nhân sự: chuyển từ `NVBH` (Bán hàng) sang `NVTN` (Thu ngân) hoặc `NVQL` (Quản lý). Hệ thống cập nhật cột `vaiTro` trong bảng `TaiKhoan` với ràng buộc `CHECK (vaiTro IN ('NVQL', 'NVTN', 'NVBH', 'NVCS'))`.
*   **Điều kiện tiên quyết:** Nhân viên `NV004` đang có vai trò `NVBH`.
*   **Các bước thực hiện:**
    1.  Trên giao diện nhân viên `NV004`, chọn thao tác "Đổi vai trò".
    2.  Chọn vai trò mới: `NVTN` (Thu ngân).
    3.  Xác nhận cập nhật.
*   **Dữ liệu kiểm thử:** `maNhanVien = "NV004"`, `vaiTroMoi = "NVTN"`.
*   **Kết quả mong đợi:**
    *   Cột `vaiTro` trong `TaiKhoan` của `NV004` đổi thành `'NVTN'`.
    *   Thẻ nhân viên trên giao diện đổi màu badge sang xanh dương (Thu ngân).
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Quyền hạn được cập nhật chính xác.
*   **Verification SQL Queries:**
```sql
SELECT maTaiKhoan, vaiTro FROM TaiKhoan WHERE maTaiKhoan = 'NV004';
-- Kỳ vọng: vaiTro = 'NVTN'.
```

---

#### TC-NV-005: Khóa và Mở khóa tài khoản nhân viên
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `QuanLyNhanVienPanel.xuLyKhoaTaiKhoan`, `NhanVienBus.toggleTrangThai`.
*   **Cấp độ kiểm thử:** Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra chức năng tạm đình chỉ hoặc kích hoạt lại tài khoản:
    *   Khi bấm Khóa: `trangThaiHoatDong` đổi từ `1` -> `0`.
    *   Khi bấm Mở khóa: `trangThaiHoatDong` đổi từ `0` -> `1`.
*   **Điều kiện tiên quyết:** Nhân viên `NV005` đang có `trangThaiHoatDong = 1`.
*   **Các bước thực hiện:**
    1.  Chọn nhân viên `NV005`, bấm "Khóa tài khoản".
    2.  Kiểm tra CSDL.
    3.  Bấm "Mở khóa tài khoản".
    4.  Kiểm tra CSDL.
*   **Dữ liệu kiểm thử:** `maNhanVien = "NV005"`.
*   **Kết quả mong đợi:** Cột `trangThaiHoatDong` lật trạng thái chuẩn xác (`0` -> `1`).
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Trạng thái lưu bền vững.
*   **Verification SQL Queries:**
```sql
SELECT maTaiKhoan, trangThaiHoatDong FROM TaiKhoan WHERE maTaiKhoan = 'NV005';
```

---

#### TC-NV-006: Lỗ hổng tài khoản nhân viên bị khóa nhưng vẫn đăng nhập thành công (DEF-01 / DEF-07 / DEF-CRI-01 Verification)
*   **Phân hệ & Màn hình:** Bảo mật & Đăng nhập / `sp_DangNhap`, `bus.DangNhapBus.xuLyDangNhap`.
*   **Cấp độ kiểm thử:** Security / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh lỗ hổng bảo mật nghiêm trọng **DEF-01 / DEF-07 / DEF-CRI-01**:
    *   Tại thủ tục `dbo.sp_DangNhap`:
        ```sql
        WHERE (tk.soDienThoai = @identifier OR tk.email = @identifier OR tk.maTaiKhoan = @identifier)
          AND tk.matKhau = @hashed;
        ```
        Thủ tục SQL không lọc điều kiện `trangThaiHoatDong = 1`.
    *   Tại `DangNhapBus.java` (dòng 81-96):
        ```java
        TaiKhoan tk = taiKhoanDAO.kiemTraDangNhap(inputUser, matKhau.trim());
        if (tk != null) {
            NhanVien nv = nhanVienDAO.findByMaNhanVien(tk.getMaTaiKhoan());
            // ...
            UserSession.initSession(nv);
            return new KetQuaDangNhap(true, "Đăng nhập thành công!", nv);
        }
        ```
        Code kiểm tra tài khoản bị khóa lại nằm ở nhánh `else` (chỉ chạy khi sai mật khẩu). Nếu nhân viên bị khóa nhập đúng mật khẩu, hệ thống khởi tạo phiên `UserSession` và cho phép đăng nhập vào trang chủ bình thường!
*   **Điều kiện tiên quyết:** Khóa tài khoản nhân viên `NV002` (`trangThaiHoatDong = 0`). Mật khẩu là `123456`.
*   **Các bước thực hiện:**
    1.  Mở màn hình đăng nhập `LoginForm`.
    2.  Nhập SĐT `0901234567` (`NV002`), Mật khẩu `123456`.
    3.  Bấm "Đăng nhập".
*   **Dữ liệu kiểm thử:** `identifier = "0901234567"`, `password = "123456"`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng bảo mật chuẩn:* Bắt buộc chặn đăng nhập, hiển thị thông báo: "Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động. Vui lòng liên hệ Quản lý!".
    *   *Thực tế hiện tại (Bug Trigger):* Đăng nhập thành công, mở ra giao diện chính `MainDashboard` -> **Xác nhận phát hiện lỗ hổng DEF-01 / DEF-07 / DEF-CRI-01 (Critical)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗ hổng):* Chứng minh tài khoản bị khóa vẫn đăng nhập được.
*   **Verification SQL Queries:**
```sql
-- Khóa tài khoản NV002:
UPDATE TaiKhoan SET trangThaiHoatDong = 0 WHERE maTaiKhoan = 'NV002';

-- Kiểm tra kết quả trả về của sp_DangNhap:
EXEC dbo.sp_DangNhap @identifier = '0901234567', @matKhauRaw = '123456';
-- KẾT QUẢ QUAN SÁT: Trả về bản ghi dù trangThaiHoatDong = 0!
```

---

#### TC-NV-007: Cấp lại mật khẩu (Reset password) tự động băm SHA-256
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `QuanLyNhanVienPanel.moDialogResetMatKhau`, `NhanVienBus.resetMatKhau`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chức năng đặt lại mật khẩu cho nhân viên quên mật khẩu: Quản lý nhập mật khẩu mới, hệ thống băm SHA-256 qua hàm `dbo.fn_HashPassword()` và cập nhật vào `TaiKhoan`.
*   **Điều kiện tiên quyết:** Nhân viên `NV002` đang có mật khẩu cũ.
*   **Các bước thực hiện:**
    1.  Chọn nhân viên `NV002`, chọn "Cấp lại mật khẩu".
    2.  Nhập mật khẩu mới: `Mio@2026`.
    3.  Bấm "OK".
*   **Dữ liệu kiểm thử:** `maNhanVien = "NV002"`, `matKhauMoi = "Mio@2026"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại hiển thị thông báo cấp lại mật khẩu thành công.
    *   Cột `matKhau` trong bảng `TaiKhoan` bằng đúng chuỗi băm `dbo.fn_HashPassword('Mio@2026')`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Mật khẩu mới được băm và cập nhật chính xác.
*   **Verification SQL Queries:**
```sql
SELECT maTaiKhoan, matKhau, dbo.fn_HashPassword('Mio@2026') AS HashKiemChung
FROM TaiKhoan
WHERE maTaiKhoan = 'NV002';
-- Kỳ vọng: matKhau = HashKiemChung.
```

---

#### TC-NV-008: Xóa nhân viên vướng ràng buộc khóa ngoại FK và kiểm tra lỗi im lặng (DEF-20 / DEF-MAJ-19 Verification)
*   **Phân hệ & Màn hình:** Quản lý Nhân sự / `QuanLyNhanVienPanel`, `dao.NhanVienDAO.deleteNhanVien`.
*   **Cấp độ kiểm thử:** Integration / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-20 / DEF-MAJ-19**:
    *   Khi nhân viên đã có dữ liệu ràng buộc khóa ngoại trong `HoaDon`, `PhieuDoiTra`, `BangGiaoCa`, câu lệnh `DELETE FROM NhanVien` sẽ bị SQL Server chặn lại bằng lỗi Foreign Key Constraint.
    *   Tầng DAO bắt lỗi, rollback và trả về `false`.
    *   Tại tầng GUI (`QuanLyNhanVienPanel.java`): Code chỉ có `if (ok) { ... }`, không hề có khối `else` để bật hộp thoại báo lỗi. Người dùng bấm xóa nhưng không thấy bất kỳ hiện tượng gì xảy ra, gây hiểu lầm là phần mềm bị đơ hoặc lỗi hệ thống.
*   **Điều kiện tiên quyết:** Nhân viên `NV002` đã lập hóa đơn trong bảng `HoaDon`.
*   **Các bước thực hiện:**
    1.  Gọi `nhanVienDAO.deleteNhanVien("NV002")`.
    2.  Quan sát phản hồi trên giao diện `QuanLyNhanVienPanel`.
*   **Dữ liệu kiểm thử:** `maNhanVien = "NV002"`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng chuẩn:* Phải hiển thị thông báo: "Không thể xóa nhân viên này vì đã phát sinh hóa đơn và biên bản bàn giao ca trong hệ thống!".
    *   *Thực tế hiện tại (Bug Trigger):* Thao tác thất bại trong im lặng, không có bất kỳ thông báo nào cho người dùng -> **Xác nhận khiếm khuyết DEF-20 / DEF-MAJ-19 (Major)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh thao tác thất bại không hiển thị thông báo.

---

### PHẦN 2: MA TRẬN PHÂN CÔNG CA LÀM VIỆC TUẦN (TIMETABLE MATRIX)

#### TC-PC-001: Xếp ca làm việc mới theo ma trận tuần (Happy Path)
*   **Phân hệ & Màn hình:** Phân ca làm việc / `LichPhanCaPanel`, `PhanCaBus.xepCaLamViec`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra quy trình phân công ca trực:
    *   Chọn nhân viên: `NV002 - Lê Anh Minh` (Thu ngân).
    *   Chọn ngày: Thứ Ba tuần tới.
    *   Chọn ca: `CA01` (Ca 1 Sáng: 07:00 - 11:30).
    *   Nhiệm vụ: `Mở quầy thu ngân 1, kiểm đếm két đầu ca`.
    *   Lưu vào bảng `BangPhanCong` trong CSDL.
*   **Điều kiện tiên quyết:** Nhân viên `NV002` chưa có lịch trực trong ngày Thứ Ba tuần tới.
*   **Các bước thực hiện:**
    1.  Mở màn hình `LichPhanCaPanel`.
    2.  Bấm nút "+ Xếp ca mới".
    3.  Điền thông tin nhân viên, ngày, ca làm việc và ghi chú nhiệm vụ.
    4.  Bấm "Lưu phân công".
*   **Dữ liệu kiểm thử:**
    *   `maNV = "NV002"`, `maCa = "CA01"`, `ngay = next Tuesday`, `ghiChu = "Mở quầy thu ngân 1"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại đóng lại, ô ma trận giao giữa `Ca 1` và `Thứ Ba` xuất hiện thẻ nhân viên `Lê Anh Minh` màu xanh dương (Thu ngân).
    *   Bảng `BangPhanCong` trong CSDL có bản ghi mới.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Thẻ ca xuất hiện đúng ô ma trận và lưu bền vững vào CSDL.
*   **Verification SQL Queries:**
```sql
SELECT pc.maPhanCong, nv.hoTen, c.tenCa, pc.ngayLamViec, pc.ghiChu
FROM BangPhanCong pc
JOIN NhanVien nv ON pc.maNhanVien = nv.maNhanVien
JOIN CaLamViec c ON pc.maCa = c.maCa
WHERE pc.maNhanVien = 'NV002' AND pc.ngayLamViec = CAST(DATEADD(day, 2, GETDATE()) AS DATE);
```

---

#### TC-PC-002: Chặn xếp trùng ca làm việc của cùng một nhân viên trong ngày (Negative Test)
*   **Phân hệ & Màn hình:** Phân ca làm việc / `PhanCaBus.xepCaLamViec`, ràng buộc `UQ_BangPhanCong_NhanVien_Ngay_Ca`.
*   **Cấp độ kiểm thử:** Unit & Boundary Test.
*   **Mô tả mục tiêu:** Một nhân viên không thể bị xếp trùng 2 lần vào cùng 1 ca làm việc trong cùng 1 ngày (ví dụ `NV002` đã trực `CA01` ngày `2026-11-10`, không thể tiếp tục xếp thêm `CA01` vào ngày đó).
*   **Điều kiện tiên quyết:** Đã có phân công cho `NV002` vào `CA01` ngày `2026-11-10`.
*   **Các bước thực hiện:**
    1.  Cố gắng gọi `phanCaBus.xepCaLamViec(nv, ca1, ngay, "Nhiệm vụ trùng")`.
*   **Dữ liệu kiểm thử:** `nv = NV002`, `ca = CA01`, `ngay = 2026-11-10`.
*   **Kết quả mong đợi:**
    *   Hàm phát hiện đã có ca trong ngày, kiểm tra nếu cùng ca thì chỉ cập nhật ghi chú thay vì tạo bản ghi trùng lặp.
    *   Ràng buộc UNIQUE trong CSDL `UQ_BangPhanCong_NhanVien_Ngay_Ca` bảo vệ toàn vẹn dữ liệu.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Không sinh ra 2 bản ghi trùng nhau.
*   **Verification SQL Queries:**
```sql
SELECT maNhanVien, ngayLamViec, maCa, COUNT(*) AS SoLuong
FROM BangPhanCong
GROUP BY maNhanVien, ngayLamViec, maCa
HAVING COUNT(*) > 1;
-- Kỳ vọng: Trả về 0 dòng (Không có bất kỳ bản ghi trùng lặp nào).
```

---

#### TC-PC-003: Giới hạn tối đa 2 ca liên tiếp trong ngày cho nhân viên
*   **Phân hệ & Màn hình:** Phân ca làm việc / `PhanCaBus.laHaiCaLienTiep`, dòng 237-243.
*   **Cấp độ kiểm thử:** Unit / Business Rule Test.
*   **Mô tả mục tiêu:** Kiểm tra quy định lao động:
    *   Một nhân viên chỉ được trực tối đa 2 ca trong 1 ngày, và 2 ca đó bắt buộc phải liền kề nhau (ví dụ: `CA01` + `CA02`, hoặc `CA02` + `CA03`, hoặc `CA03` + `CA04`).
    *   Nếu xếp 2 ca rời rạc (ví dụ `CA01` sáng và `CA04` tối) hoặc xếp ca thứ 3 trong ngày -> Hệ thống từ chối (`return false`).
*   **Điều kiện tiên quyết:** Nhân viên `NV003` đã có lịch trực `CA01` ngày hôm nay.
*   **Các bước thực hiện:**
    1.  Thử xếp thêm `CA02` cho `NV003` -> Kiểm tra kết quả (Hợp lệ: 2 ca liên tiếp).
    2.  Thử xếp thêm `CA04` cho `NV003` -> Kiểm tra kết quả (Không hợp lệ: ca thứ 3).
*   **Dữ liệu kiểm thử:** `CA01` (đã có), thêm `CA02`, thêm `CA04`.
*   **Kết quả mong đợi:**
    *   Bước 1: Chấp nhận xếp `CA02`.
    *   Bước 2: Từ chối xếp `CA04` (`size >= 2`).
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Tuân thủ nghiêm ngặt quy tắc sức khỏe lao động.

---

#### TC-PC-004: Đổi ca làm việc và cập nhật nhiệm vụ phân công
*   **Phân hệ & Màn hình:** Phân ca làm việc / `PhanCaBus.doiCaPhanCong`, `PhanCaBus.capNhatGhiChuPhanCong`.
*   **Cấp độ kiểm thử:** Integration Test (BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chức năng điều phối nhân sự nhanh: đổi ca trực của nhân viên từ `CA01` sang `CA02` và cập nhật lại dặn dò công việc.
*   **Điều kiện tiên quyết:** Phân công `PC1001` đang ở `CA01`.
*   **Các bước thực hiện:**
    1.  Gọi `phanCaBus.doiCaPhanCong("PC1001", ca2)`.
    2.  Gọi `phanCaBus.capNhatGhiChuPhanCong("PC1001", "Hỗ trợ cắt tỉa ca trưa")`.
*   **Dữ liệu kiểm thử:** `maPC = "PC1001"`, `caMoi = CA02`, `ghiChuMoi = "Hỗ trợ cắt tỉa ca trưa"`.
*   **Kết quả mong đợi:** CSDL cập nhật `maCa = 'CA02'` và `ghiChu = 'Hỗ trợ cắt tỉa ca trưa'`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Thao tác thành công, không phát sinh lỗi khóa ngoại.
*   **Verification SQL Queries:**
```sql
SELECT maPhanCong, maCa, ghiChu FROM BangPhanCong WHERE maPhanCong = 1001;
```

---

#### TC-PC-005: Xóa phân công ca làm việc
*   **Phân hệ & Màn hình:** Phân ca làm việc / `PhanCaBus.xoaPhanCong`, `PhanCaDAO.deletePhanCong`.
*   **Cấp độ kiểm thử:** Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra chức năng hủy phân công ca: xóa bản ghi phân công khỏi CSDL và loại bỏ thẻ nhân viên khỏi ma trận giao diện tuần.
*   **Điều kiện tiên quyết:** Phân công `PC9999` tồn tại trong hệ thống.
*   **Các bước thực hiện:**
    1.  Gọi `phanCaBus.xoaPhanCong("PC9999")`.
    2.  Kiểm tra CSDL.
*   **Dữ liệu kiểm thử:** `maPC = "PC9999"`.
*   **Kết quả mong đợi:** Bản ghi bị xóa khỏi `BangPhanCong`, hàm trả về `true`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Bản ghi không còn tồn tại trong CSDL.
*   **Verification SQL Queries:**
```sql
SELECT COUNT(*) FROM BangPhanCong WHERE maPhanCong = 9999; -- Kỳ vọng: 0
```

---

#### TC-PC-006: Kiểm tra xung đột sinh mã phân công maPC = size() + 1 (DEF-19 / DEF-MAJ-18 Verification)
*   **Phân hệ & Màn hình:** Tầng Nghiệp vụ / `bus.PhanCaBus.xepCaLamViec`, dòng 245.
*   **Cấp độ kiểm thử:** Unit / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-19 / DEF-MAJ-18**:
    *   Dòng 245: `String maPC = "PC" + (danhSachPhanCong.size() + 1);`
    *   Hệ thống sinh mã phân công dựa trên kích thước danh sách trong bộ nhớ RAM (`size() + 1`).
    *   Nếu trong ca làm việc trước đó có thao tác xóa 1 phân công (ví dụ danh sách đang có 10 phần tử, xóa 1 phần tử còn 9; khi thêm mới, `size() + 1 = 10`), mã `PC10` sinh ra sẽ bị trùng lặp với mã đã từng tồn tại trong CSDL hoặc gây lỗi khóa chính.
*   **Điều kiện tiên quyết:** Đã có 10 phân công trong CSDL.
*   **Các bước thực hiện:**
    1.  Xóa phân công số 5.
    2.  Tiến hành thêm một phân công mới.
    3.  Quan sát mã `maPC` được sinh ra.
*   **Dữ liệu kiểm thử:** Kích thước danh sách biến động sau khi xóa.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại (Bug Trigger):* Mã sinh ra bị trùng lặp với bản ghi đã có -> **Xác nhận khiếm khuyết DEF-19 / DEF-MAJ-18 (Major)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh cơ chế sinh mã bằng `size() + 1` không an toàn.

---

#### TC-PC-007: Kiểm tra bộ lọc cá nhân hóa "Chỉ xem lịch của tôi"
*   **Phân hệ & Màn hình:** Phân ca làm việc / `LichPhanCaPanel.taiDuLieuMaTran`, `chkChiLichCuaToi`.
*   **Cấp độ kiểm thử:** UI / Manual Functional Test.
*   **Mô tả mục tiêu:** Kiểm tra tính năng lọc cá nhân:
    *   Khi nhân viên đăng nhập, tích chọn "Chỉ xem lịch của tôi" -> Ma trận chỉ hiển thị các ca của chính nhân viên đó, các ô khác để trống.
    *   Khi bỏ tích chọn -> Hiển thị toàn bộ nhân sự theo tuần.
*   **Điều kiện tiên quyết:** Phiên đăng nhập hiện tại là `NV002` (`Lê Anh Minh`).
*   **Các bước thực hiện:**
    1.  Tích chọn checkbox `chkChiLichCuaToi`.
    2.  Quan sát các thẻ ca trên bảng ma trận.
*   **Dữ liệu kiểm thử:** `UserSession.getUserId() == "NV002"`.
*   **Kết quả mong đợi:** Tất cả các thẻ hiển thị trên bảng đều có tên `Lê Anh Minh`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Lọc chính xác theo mã nhân viên đăng nhập.

---

#### TC-PC-008: Xuất bảng ma trận lịch phân công ca tuần ra tệp Excel K80
*   **Phân hệ & Màn hình:** Phân ca làm việc / `utils.LichPhanCaExcelExporter.xuatFileExcel`.
*   **Cấp độ kiểm thử:** Integration Test (GUI -> POI/File System).
*   **Mô tả mục tiêu:** Đảm bảo tính năng xuất lịch làm việc ra file Excel hoạt động ổn định:
    *   Tạo file `.xlsx` đầy đủ tiêu đề, ngày xuất, bảng ma trận 4 ca x 7 ngày.
    *   Định dạng viền ô, màu sắc header, tên nhân viên trong từng ca.
*   **Điều kiện tiên quyết:** Ma trận tuần có dữ liệu phân công.
*   **Các bước thực hiện:**
    1.  Bấm nút "Xuất Excel".
    2.  Chọn đường dẫn lưu file: `export/LichPhanCa_Tuan_45.xlsx`.
    3.  Bấm Save.
*   **Dữ liệu kiểm thử:** File path hợp lệ trên ổ cứng.
*   **Kết quả mong đợi:** File Excel được tạo thành công, kích thước > 0 bytes, mở được bằng Excel.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Xuất file không bị văng Exception.

---

### PHẦN 3: BÀN GIAO CA THU NGÂN & ĐỐI SOÁT KÉT TIỀN MẶT

#### TC-GC-001: Mở ca làm việc & Tự động kế thừa tiền đầu ca từ ca liền kề (Happy Path)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.lamMoiDuLieuCaHienTai`, `GiaoCaBus.layCaBanGiaoGanNhat`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chuỗi kế thừa liên tục giữa các ca:
    *   Ca trước (ví dụ `GC002`) chốt két thực tế bàn giao là `4.580.000 đ`.
    *   Khi thu ngân ca sau mở màn hình Bàn giao ca, ô `(1) Tiền đầu ca kế thừa` (`txtTienDauCa`) phải tự động nạp đúng số tiền `4.580.000 đ`, không cho phép thu ngân tự ý sửa (`setEditable(false)`).
*   **Điều kiện tiên quyết:** Bản ghi bàn giao ca gần nhất `GC002` có `tienCuoiCaThucTe = 4580000.00`.
*   **Các bước thực hiện:**
    1.  Mở màn hình `BanGiaoCaPanel`.
    2.  Quan sát giá trị hiển thị tại ô `txtTienDauCa`.
*   **Dữ liệu kiểm thử:** `tienCuoiCaThucTe_CaTruoc = 4580000.00`.
*   **Kết quả mong đợi:**
    *   `txtTienDauCa.getText()` hiển thị `4580000`.
    *   Ô nhập liệu bị khóa không cho sửa tay.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Kế thừa chuẩn xác số tiền từ ca trước.
*   **Verification SQL Queries:**
```sql
SELECT TOP 1 maGiaoCa, tienCuoiCaThucTe, gioKetThucThucTe
FROM BangGiaoCa
ORDER BY gioKetThucThucTe DESC, maGiaoCa DESC;
-- Kỳ vọng: Giá trị tienCuoiCaThucTe này khớp với txtTienDauCa trên giao diện.
```

---

#### TC-GC-002: Tự động tính toán doanh thu tiền mặt, chuyển khoản và tiền hoàn trả theo ca
*   **Phân hệ & Màn hình:** Bàn giao ca / `BangGiaoCaDAO` (`tinhDoanhThuTienMatTuThoiDiem`, `tinhDoanhThuChuyenKhoanTuThoiDiem`, `tinhTongTienHoanTraTuThoiDiem`).
*   **Cấp độ kiểm thử:** Integration Test (Database Aggregation).
*   **Mô tả mục tiêu:** Kiểm tra cơ chế tự động quét bảng `HoaDon` và `PhieuDoiTra` kể từ mốc thời gian chốt ca gần nhất:
    *   `tienHeThongGhiNhan` = Tổng tiền các hóa đơn Tiền mặt.
    *   `tienChuyenKhoanGhiNhan` = Tổng tiền các hóa đơn Chuyển khoản VietQR / Thẻ.
    *   `tienHoanTraKhach` = Tổng tiền các phiếu đổi trả hàng hoàn tiền cho khách.
    *   `tienDuKien` (Lý thuyết két) = `tienDauCa + tienHeThongGhiNhan - tienHoanTraKhach`.
*   **Điều kiện tiên quyết:**
    *   `tienDauCa = 1.000.000 đ`.
    *   Trong ca phát sinh: 2 hóa đơn tiền mặt (500k + 300k = 800k), 1 hóa đơn VietQR (400k), 1 phiếu hoàn trả tiền mặt (100k).
*   **Các bước thực hiện:**
    1.  Bấm nút "Làm mới doanh thu ca".
    2.  Quan sát các nhãn: `lblTienHeThong`, `lblTienChuyenKhoan`, `lblTienHoanTra`, `lblTienDuKien`.
*   **Dữ liệu kiểm thử:**
    *   Đầu ca: `1.000.000 đ`. Thu tiền mặt: `800.000 đ`. Chuyển khoản: `400.000 đ`. Hoàn trả: `100.000 đ`.
*   **Kết quả mong đợi:**
    *   `lblTienHeThong` hiển thị `+800,000 đ`.
    *   `lblTienChuyenKhoan` hiển thị `400,000 đ (Vào TK Ngân hàng)`.
    *   `lblTienHoanTra` hiển thị `-100,000 đ`.
    *   `lblTienDuKien` hiển thị chính xác: `1.000.000 + 800.000 - 100.000 = 1,700,000 đ`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Tính toán chính xác 100% theo công thức đối soát két.
*   **Verification SQL Queries:**
```sql
DECLARE @moc DATETIME2 = (SELECT ISNULL(MAX(gioKetThucThucTe), CAST(GETDATE() AS DATE)) FROM BangGiaoCa);

SELECT 
    (SELECT ISNULL(SUM(thanhTien), 0) FROM HoaDon WHERE thoiGianThanhToan > @moc AND (phuongThucThanhToan LIKE N'%Tiền mặt%' OR phuongThucThanhToan IS NULL)) AS DoanhThuTienMat,
    (SELECT ISNULL(SUM(thanhTien), 0) FROM HoaDon WHERE thoiGianThanhToan > @moc AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%') AS DoanhThuChuyenKhoan,
    (SELECT ISNULL(SUM(tongTienHoan), 0) FROM PhieuDoiTra WHERE ngayDoiTra > @moc) AS TienHoanTra;
```

---

#### TC-GC-003: Đối soát két tiền - Kịch bản Khớp két 100% (Happy Path)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.tinhChenhLech`, `txtTienThucTe`, `lblChenhLech`.
*   **Cấp độ kiểm thử:** UI & Logic Test.
*   **Mô tả mục tiêu:** Khi thu ngân kiểm đếm két và nhập số tiền thực tế đúng bằng số tiền lý thuyết:
    *   Chênh lệch = `0 đ`.
    *   Nhãn `lblChenhLech` hiển thị chữ màu xanh lá: `"0 đ (Khớp két 100%)"`.
    *   Thu ngân không bắt buộc phải nhập lý do giải trình.
    *   Cho phép bấm nút "CHỐT CA & BÀN GIAO KÉT TIỀN".
*   **Điều kiện tiên quyết:** Tiền mặt lý thuyết đang là `1.700.000 đ`.
*   **Các bước thực hiện:**
    1.  Nhập `"1700000"` vào ô `txtTienThucTe`.
    2.  Quan sát nhãn `lblChenhLech`.
    3.  Bấm nút "CHỐT CA & BÀN GIAO KÉT TIỀN".
*   **Dữ liệu kiểm thử:** `txtTienThucTe = "1700000"`.
*   **Kết quả mong đợi:**
    *   `lblChenhLech` đổi thành `"0 đ (Khớp két 100%)"`, màu xanh lá `#059669`.
    *   Hệ thống không yêu cầu nhập giải trình, lưu biên bản thành công.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Trạng thái khớp két chuẩn xác.

---

#### TC-GC-004: Đối soát két tiền - Kịch bản Thừa tiền mặt trong két (Chênh lệch > 0)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.tinhChenhLech`, `txtLyDoChenhLech`.
*   **Cấp độ kiểm thử:** UI & Boundary Test.
*   **Mô tả mục tiêu:** Kiểm tra xử lý trường hợp két bị thừa tiền:
    *   Lý thuyết: `1.700.000 đ`. Thực tế kiểm đếm: `1.750.000 đ` (Thừa `50.000 đ`).
    *   Nhãn `lblChenhLech` hiển thị màu vàng/cam: `"+50,000 đ (Thừa tiền mặt)"`.
    *   Thu ngân BẮT BUỘC nhập lý do giải trình thừa tiền (ví dụ: `Khách bo thêm tiền thối`, `Nhầm lẫn tiền lẻ lẻ`).
    *   Nếu để trống ô giải trình và bấm Chốt ca -> Hệ thống bật cảnh báo chặn chốt ca.
*   **Điều kiện tiên quyết:** Tiền lý thuyết = `1.700.000 đ`.
*   **Các bước thực hiện:**
    1.  Nhập `"1750000"` vào ô `txtTienThucTe`.
    2.  Để trống ô `txtLyDoChenhLech` và bấm Chốt ca -> Quan sát cảnh báo.
    3.  Nhập lý do: `"Khách mua thức ăn mèo không lấy 50k tiền thối"`.
    4.  Bấm Chốt ca -> Quan sát kết quả.
*   **Dữ liệu kiểm thử:** `thucTe = 1750000`, `lyDo = "Khách không lấy 50k tiền thối"`.
*   **Kết quả mong đợi:**
    *   Bước 2: Hệ thống bật cảnh báo: "Phát hiện chênh lệch tiền mặt trong két (+50,000 đ)! BẮT BUỘC nhập lý do giải trình trước khi bàn giao ca." và focus vào ô giải trình.
    *   Bước 4: Lưu biên bản thành công với ghi chú giải trình.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Chặn thành công khi thiếu giải trình; cho phép chốt ca khi đã điền lý do.

---

#### TC-GC-005: Đối soát két tiền - Kịch bản Thiếu tiền mặt trong két (Chênh lệch < 0)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.tinhChenhLech`, `BanGiaoCaPanel.xacNhanBanGiaoCa`.
*   **Cấp độ kiểm thử:** UI & Negative/Edge Test.
*   **Mô tả mục tiêu:** Kiểm tra xử lý trường hợp hụt két:
    *   Lý thuyết: `1.700.000 đ`. Thực tế kiểm đếm: `1.680.000 đ` (Thiếu `20.000 đ`).
    *   Nhãn `lblChenhLech` hiển thị màu đỏ: `"-20,000 đ (Thiếu tiền mặt)"`.
    *   Bắt buộc thu ngân giải trình lý do hụt tiền (ví dụ: `Thối nhầm tờ 20k cho khách mua xúc xích`).
    *   Nếu không giải trình -> Chặn chốt ca tuyệt đối.
*   **Điều kiện tiên quyết:** Tiền lý thuyết = `1.700.000 đ`.
*   **Các bước thực hiện:**
    1.  Nhập `"1680000"` vào `txtTienThucTe`.
    2.  Để trống giải trình -> Bấm Chốt ca -> Xác nhận bị chặn.
    3.  Điền giải trình: `"Thối nhầm 20.000 đ cho khách mua xúc xích đơn HD261107-12"`.
    4.  Bấm Chốt ca.
*   **Dữ liệu kiểm thử:** `thucTe = 1680000`, `chenhLech = -20000`.
*   **Kết quả mong đợi:**
    *   Hệ thống kiểm soát chặt chẽ, ghi nhận số tiền âm `-20000.00` vào cột `tienChenhLech` của CSDL.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Không cho phép bỏ qua giải trình khi hụt tiền két.
*   **Verification SQL Queries:**
```sql
SELECT TOP 1 maGiaoCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, lyDoChenhLech, trangThai
FROM BangGiaoCa
ORDER BY gioKetThucThucTe DESC;
-- Kỳ vọng: tienChenhLech = -20000.00, lyDoChenhLech chứa 'Thối nhầm 20.000 đ'.
```

---

#### TC-GC-006: Kiểm tra nhập liệu tiền thực tế không hợp lệ & Khiếm khuyết regex nuốt dấu trừ (Negative / Boundary Defect)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.xacNhanBanGiaoCa`, `txtTienThucTe`.
*   **Cấp độ kiểm thử:** UI / Input Validation & Regex Flaw Test.
*   **Mô tả mục tiêu:** Kiểm tra tính năng làm sạch dữ liệu và bắt lỗi khi thu ngân nhập sai định dạng, đồng thời xác minh khiếm khuyết xử lý chuỗi số âm:
    *   Để trống hoàn toàn ô tiền thực tế -> Thông báo: "Vui lòng kiểm đếm và nhập tổng tiền mặt thực tế trong két trước khi chốt ca!".
    *   Nhập chữ cái `"abcxyz"` -> Nhãn hiển thị "Chưa kiểm đếm tiền mặt" hoặc "Số tiền không hợp lệ".
    *   Nhập số có dấu phẩy/chấm `"1,700,000"` -> Hệ thống tự động lọc bỏ dấu và parse thành số `1700000`.
    *   **Phát hiện khiếm khuyết nhập số âm (`-50,000`):**
        *   Khi thu ngân vô tình hoặc cố ý nhập số tiền âm `-50,000` hoặc `-50000` vào ô `txtTienThucTe`.
        *   Mã nguồn `BanGiaoCaPanel.java` sử dụng biểu thức chính quy:
            ```java
            String clean = raw.replaceAll("[^0-9]", "");
            long tienThucTe = clean.isEmpty() ? 0 : Long.parseLong(clean);
            ```
        *   Biểu thức `[^0-9]` xóa sạch toàn bộ ký tự không phải chữ số, bao gồm **dấu trừ (`-`)**.
        *   Hệ quả: Chuỗi `"-50,000"` bị lọc sạch dấu trừ và biến thành `"50000"` (`+50,000 đ`). Hệ thống ghi nhận két có dương 50.000 đ thay vì phát hiện và chặn số tiền âm vô lý!
*   **Điều kiện tiên quyết:** Mở `BanGiaoCaPanel`, hệ thống tính doanh thu ca đang chạy.
*   **Các bước thực hiện:**
    1.  Để trống ô `txtTienThucTe` -> Bấm Chốt ca -> Quan sát thông báo.
    2.  Nhập `"mười triệu đồng"` -> Quan sát nhãn chênh lệch.
    3.  Nhập `"2.500.000"` -> Quan sát hệ thống tự động định dạng `2,500,000 đ`.
    4.  **Kiểm tra số âm (Negative Cash Input):** Nhập `"-50,000"` vào ô `txtTienThucTe`.
    5.  Quan sát nhãn hiển thị tiền thực tế và nhãn chênh lệch tính toán.
    6.  Bấm nút "Chốt ca & Bàn giao" và quan sát phản ứng của hệ thống.
*   **Dữ liệu kiểm thử:**
    *   Case 1 (Trống): `txtTienThucTe = ""`
    *   Case 2 (Chữ): `txtTienThucTe = "mười triệu đồng"`
    *   Case 3 (Định dạng): `txtTienThucTe = "2.500.000"`
    *   Case 4 (Số âm): `txtTienThucTe = "-50,000"` (hoặc `"-50000"`)
*   **Kết quả mong đợi theo TIÊU CHUẨN AN TOÀN:**
    *   Case 1 & 2: Chặn chốt ca, hiển thị thông báo yêu cầu kiểm đếm và nhập số tiền hợp lệ.
    *   Case 3: Parse đúng `2500000` đ.
    *   Case 4: Hệ thống **BẮT BUỘC PHẢI CHẶN** và hiển thị thông báo lỗi: "Số tiền mặt thực tế kiểm đếm không thể là số âm!".
*   **Hành vi thực tế phát hiện lỗi (Defect Evidence):**
    *   Tại Case 4: Do `raw.replaceAll("[^0-9]", "")` xóa mất dấu trừ, ô tiền thực tế nhận giá trị `+50000`. Hệ thống tự động tính chênh lệch dựa trên số dương `+50,000 đ` và cho phép gửi biên bản chốt ca lên CSDL mà không hề báo lỗi số âm!
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Xác nhận lỗi):* Chứng minh việc nhập `"-50,000"` bị biến thành `"+50,000"` và không bị chặn.

---

#### TC-GC-007: Thu ngân chốt ca lưu biên bản trạng thái "⏳ Chờ Quản lý duyệt" vào CSDL (Happy Path)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.xacNhanBanGiaoCa`, `BangGiaoCaDAO.insertBangGiaoCa`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra quy trình chốt ca của Thu ngân (loại bỏ cửa sổ đăng nhập kép xác thực tại quầy theo yêu cầu R3):
    *   Thu ngân bấm "CHỐT CA & BÀN GIAO KÉT TIỀN".
    *   Hệ thống sinh mã giao ca mới (ví dụ `GC004`).
    *   Lưu bản ghi vào bảng `BangGiaoCa` với trạng thái `'CHO_DUYET'`.
    *   Nút "Chốt ca" bị vô hiệu hóa (`setEnabled(false)`), khóa các ô nhập liệu.
    *   Bảng lịch sử bên phải nạp thêm dòng mới với mô tả `"⏳ Chờ Quản lý duyệt | Khớp két 100%"`.
    *   Hệ thống hiển thị popup: "ĐÃ GỬI BIÊN BẢN KẾT CA [GC004] LÊN HỆ THỐNG! Trạng thái: ⏳ Chờ Quản lý duyệt... Bạn có muốn ĐĂNG XUẤT ngay bây giờ không?".
*   **Điều kiện tiên quyết:** Thu ngân `NV002` đang trực ca.
*   **Các bước thực hiện:**
    1.  Nhập tiền thực tế hợp lệ.
    2.  Bấm "CHỐT CA & BÀN GIAO KÉT TIỀN".
    3.  Quan sát trạng thái các nút bấm và hộp thoại đăng xuất.
*   **Dữ liệu kiểm thử:** `maNV = "NV002"`, `maCa = "CA01"`, `tienThucTe = 1700000`.
*   **Kết quả mong đợi:**
    *   Bản ghi lưu thành công vào CSDL với `trangThai = 'CHO_DUYET'`.
    *   Cột `maQuanLyDuyet` và `thoiGianDuyet` mang giá trị `NULL`.
    *   Hệ thống cho phép đăng xuất an toàn để đổi ca.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Tuân thủ 100% quy trình bất đồng bộ (Async Handover).
*   **Verification SQL Queries:**
```sql
SELECT maGiaoCa, maNhanVien, maCa, tienCuoiCaThucTe, tienChenhLech, trangThai, maQuanLyDuyet, thoiGianDuyet
FROM BangGiaoCa
WHERE maGiaoCa = 'GC004';
-- Kỳ vọng: trangThai = 'CHO_DUYET', maQuanLyDuyet IS NULL, thoiGianDuyet IS NULL.
```

---

#### TC-GC-008: Kiểm soát phân quyền RBAC & Kiểm thử đối kháng leo thang đặc quyền sau khi đăng xuất chốt ca (DEF-06 / DEF-MAJ-02)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.lamMoiDuLieuCaHienTai`, `BanGiaoCaPanel.duyetBienBanGiaoCaDangChon`, `UserSession.clearSession()`, `UserSession.isQuanLy()`.
*   **Cấp độ kiểm thử:** Security & Adversarial Authorization Test (**DEF-06 / DEF-MAJ-02**).
*   **Mô tả mục tiêu:** Đảm bảo nguyên tắc kiểm soát nội bộ (Separation of Duties / Dual-Control) và phát hiện lỗ hổng leo thang đặc quyền (Privilege Escalation):
    *   **Kịch bản cơ bản (RBAC chuẩn):** Khi tài khoản Thu ngân (`NVTN` hoặc `NVBH`) đăng nhập: nút "✅ Quản lý Duyệt & Xác nhận Két" bên bảng phải bị ẩn hoàn toàn (`btnQuanLyDuyet.setVisible(false)`). Nếu cố tình gọi `duyetBienBanGiaoCaDangChon()`, hệ thống phải chặn lại.
    *   **Kịch bản đối kháng (Adversarial Exploit - DEF-06 / DEF-MAJ-02):** Khi thu ngân thực hiện chốt ca ở `TC-GC-007`, hệ thống hỏi popup "Bạn có muốn ĐĂNG XUẤT ngay bây giờ không?". Nếu bấm "Có", hệ thống gọi `UserSession.clearSession()`. Tuy nhiên, trong mã nguồn `UserSession.java`:
        ```java
        public static void clearSession() {
            currentUser = null;
            currentRole = Role.NVQL; // LỖI NGHIÊM TRỌNG: Gán nhầm vai trò Quản lý khi logout!
        }
        ```
        Nếu thu ngân chưa đóng giao diện hoặc giữ tham chiếu UI frame / gọi phương thức duyệt mà không đăng nhập lại, hàm `UserSession.isQuanLy()` sẽ trả về `true` (do `currentRole == Role.NVQL`). Thu ngân có thể lợi dụng lỗi này để tự duyệt biên bản bàn giao ca của chính mình, phá vỡ hoàn toàn nguyên tắc Dual-Control!
*   **Điều kiện tiên quyết:** Tài khoản `NV002` (Vai trò `NVTN`) vừa chốt ca `GC004` trạng thái `CHO_DUYET`.
*   **Các bước thực hiện:**
    1.  *Bước 1 (Kiểm tra RBAC khi đang đăng nhập NVTN):* Đăng nhập tài khoản `NV002`, vào màn hình `BanGiaoCaPanel`. Quan sát nút "✅ Quản lý Duyệt & Xác nhận Két" bị ẩn (`btnQuanLyDuyet.isVisible() == false`).
    2.  *Bước 2 (Chốt ca & Kích hoạt đăng xuất):* Thực hiện chốt ca thành công, popup xuất hiện hỏi đăng xuất -> Chọn "Có" (Hệ thống gọi `UserSession.clearSession()`).
    3.  *Bước 3 (Thử nghiệm tấn công đối kháng):* Thay vì thoát hẳn ứng dụng, giữ tham chiếu đối tượng `BanGiaoCaPanel` hoặc kích hoạt action duyệt ca `duyetBienBanGiaoCaDangChon()`.
    4.  *Bước 4 (Quan sát phản ứng hệ thống):* Kiểm tra xem hàm `UserSession.isQuanLy()` trả về giá trị gì và hệ thống có cho phép mở dialog duyệt ca hay không.
*   **Dữ liệu kiểm thử:**
    *   Trước logout: `UserSession.getCurrentRole() == UserSession.Role.NVTN`.
    *   Sau logout: `UserSession.getCurrentUser() == null`, nhưng `UserSession.getCurrentRole() == UserSession.Role.NVQL` (**Lỗi DEF-06 / DEF-MAJ-02**).
*   **Kết quả mong đợi (Theo chuẩn an toàn):**
    *   Khi logout, `UserSession.clearSession()` phải gán `currentRole = null` (hoặc `Role.NONE`).
    *   Bất kỳ hành vi gọi duyệt nào khi chưa xác thực lại bằng tài khoản Quản lý hợp lệ đều bị chặn đứng và buộc chuyển hướng về `LoginForm`.
*   **Hành vi thực tế phát hiện lỗi (Defect Confirmation):**
    *   Hệ thống cho phép vượt qua rào chắn `UserSession.isQuanLy() == true`, mở dialog duyệt ca và thu ngân có thể tự duyệt ca của mình mà không cần quản lý can thiệp.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Phát hiện và ghi nhận chính xác lỗ hổng bảo mật **DEF-06 / DEF-MAJ-02** trong báo cáo kiểm thử.

---

#### TC-GC-009: Quản lý (NVQL/admin) đăng nhập và duyệt ca bàn giao thành công (Happy Path)
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.duyetBienBanGiaoCaDangChon`, `BangGiaoCaDAO.duyetGiaoCa`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra quy trình xét duyệt của Quản lý:
    *   Khi Quản lý (`NV001` - `Tô Thành Đạt`, vai trò `NVQL` hoặc `admin`) đăng nhập: nút "✅ Quản lý Duyệt & Xác nhận Két" hiển thị rõ ràng.
    *   Bấm nút duyệt: Hệ thống mở hộp thoại `JDialog` hiển thị danh sách các ca đang chờ duyệt (`CHO_DUYET`) kèm chi tiết thu ngân, ca trực, tiền lý thuyết, tiền thực tế, chênh lệch và giải trình.
    *   Quản lý chọn ca cần duyệt và bấm "PHÊ DUYỆT & KÝ NHẬN KÉT" (không cần nhập lại mật khẩu tại chỗ).
    *   Hệ thống cập nhật CSDL: `trangThai = 'DA_DUYET'`, `maQuanLyDuyet = 'NV001'`, `thoiGianDuyet = now`.
    *   Dòng lịch sử trên bảng đổi mô tả thành: `"✓ Đã duyệt (QL: Tô Thành Đạt) | Khớp két 100%"`.
    *   Tự động mở cửa sổ in biên bản K80 có tên Quản lý ký nhận.
*   **Điều kiện tiên quyết:**
    *   Đang có ca `GC004` trạng thái `'CHO_DUYET'`.
    *   Đăng nhập tài khoản Quản lý `NV001` (`Tô Thành Đạt`).
*   **Các bước thực hiện:**
    1.  Đăng nhập bằng tài khoản `NV001`.
    2.  Mở `BanGiaoCaPanel`.
    3.  Bấm nút "✅ Quản lý Duyệt & Xác nhận Két".
    4.  Trong hộp thoại danh sách ca chờ duyệt, chọn ca `GC004`.
    5.  Kiểm tra số liệu hiển thị trong khung chi tiết.
    6.  Bấm "PHÊ DUYỆT & KÝ NHẬN KÉT".
*   **Dữ liệu kiểm thử:** `maGiaoCa = "GC004"`, `maQuanLy = "NV001"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại đóng lại, ca `GC004` biến mất khỏi danh sách chờ duyệt.
    *   Bảng lịch sử chính hiển thị dòng `GC004` với chữ `"✓ Đã duyệt (QL: Tô Thành Đạt)"`.
    *   CSDL cập nhật `trangThai = 'DA_DUYET'`, `maQuanLyDuyet = 'NV001'`, `thoiGianDuyet` không null.
    *   Hiển thị biên bản in K80.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Hoàn tất 100% quy trình xét duyệt của Quản lý.
*   **Verification SQL Queries:**
```sql
SELECT maGiaoCa, trangThai, maQuanLyDuyet, thoiGianDuyet, lyDoChenhLech
FROM BangGiaoCa
WHERE maGiaoCa = 'GC004';
-- Kỳ vọng: trangThai = 'DA_DUYET', maQuanLyDuyet = 'NV001', thoiGianDuyet IS NOT NULL.
```

---

#### TC-GC-010: Xuất và hiển thị Biên bản Bàn giao ca chuẩn khổ giấy nhiệt K80
*   **Phân hệ & Màn hình:** Bàn giao ca / `BanGiaoCaPanel.hienThiBienBanGiaoCaK80`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra cấu trúc nội dung biên bản in K80 phục vụ lưu trữ kế toán và kiểm toán nội bộ:
    *   Đầy đủ tiêu đề: `PET SHOP MANAGEMENT - HỆ THỐNG CỬA HÀNG / BIÊN BẢN ĐỐI SOÁT & BÀN GIAO CA (K80)`.
    *   Mã biên bản, Ca trực, Thời điểm chốt, Thời điểm duyệt.
    *   Thu ngân giao ca, Người nhận bàn giao (Quản lý duyệt).
    *   Bảng bóc tách chi tiết:
        *   (1) Tiền đầu ca kế thừa
        *   (2) Doanh thu tiền mặt bán hàng (+)
        *   (3) Tiền hoàn trả khách đổi/trả (-)
        *   (=) Tiền mặt lý thuyết két
        *   (*) Tiền mặt thực tế kiểm đếm
        *   Chênh lệch két (Khớp / Thừa / Thiếu)
        *   (i) Doanh thu QR / Chuyển khoản (tham khảo sao kê ngân hàng)
    *   Chữ ký số/Chữ ký tay xác nhận của Thu ngân và Quản lý.
*   **Điều kiện tiên quyết:** Ca `GC004` đã được phê duyệt.
*   **Các bước thực hiện:**
    1.  Chọn ca `GC004` trên bảng lịch sử bàn giao ca.
    2.  Bấm xem và in lại biên bản K80.
*   **Dữ liệu kiểm thử:** `maGiaoCa = "GC004"`.
*   **Kết quả mong đợi:**
    *   Mở hộp thoại `HoaDonPreviewDialog` (hoặc dialog preview K80) hiển thị đầy đủ văn bản theo chuẩn định dạng in nhiệt K80.
    *   Số tiền có dấu phân cách nghìn (`#,### đ`).
    *   Có nút "In biên bản" gửi lệnh ra máy in nhiệt ESC/POS.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Biên bản trực quan, đầy đủ căn cứ pháp lý và đối soát tài chính.

---

## III. TẬP LỆNH SQL KIỂM CHỨNG TRẠNG THÁI CƠ SỞ DỮ LIỆU TOÀN DIỆN (VERIFICATION SUITE)

Dưới đây là tập lệnh SQL Server hỗ trợ kiểm tra và đối chiếu độc lập toàn bộ các bảng trong phân hệ Nhân sự & Bàn giao ca:

```sql
USE QuanLyCuaHangThuCung;
GO

-- ======================================================================================
-- 1. BÁO CÁO TỔNG QUAN TÌNH TRẠNG NHÂN SỰ & PHÂN QUYỀN RBAC
-- ======================================================================================
PRINT '=== THỐNG KÊ NHÂN SỰ & PHÂN QUYỀN ===';

SELECT tk.vaiTro, COUNT(*) AS SoLuongNhanVien,
       SUM(CASE WHEN tk.trangThaiHoatDong = 1 THEN 1 ELSE 0 END) AS DangHoatDong,
       SUM(CASE WHEN tk.trangThaiHoatDong = 0 THEN 1 ELSE 0 END) AS DaKhoa
FROM TaiKhoan tk
GROUP BY tk.vaiTro;
GO

-- ======================================================================================
-- 2. ĐỐI SOÁT TÌNH TRẠNG DUYỆT BÀN GIAO CA THU NGÂN (CHO_DUYET VS DA_DUYET)
-- ======================================================================================
PRINT '=== DANH SÁCH CÁC CA ĐANG CHỜ QUẢN LÝ DUYỆT ===';

SELECT bgc.maGiaoCa, bgc.maCa, nv.hoTen AS ThuNganGiao,
       bgc.tienDauCa, bgc.tienCuoiCaHeThong, bgc.tienCuoiCaThucTe, bgc.tienChenhLech,
       bgc.gioKetThucThucTe, bgc.trangThai, bgc.lyDoChenhLech
FROM BangGiaoCa bgc
JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien
WHERE bgc.trangThai = 'CHO_DUYET'
ORDER BY bgc.gioKetThucThucTe DESC;
GO

PRINT '=== DANH SÁCH CÁC CA ĐÃ ĐƯỢC PHÊ DUYỆT ===';

SELECT bgc.maGiaoCa, bgc.maCa, nv.hoTen AS ThuNganGiao, ql.hoTen AS QuanLyDuyet,
       bgc.tienChenhLech, bgc.thoiGianDuyet, bgc.trangThai
FROM BangGiaoCa bgc
JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien
LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien
WHERE bgc.trangThai = 'DA_DUYET'
ORDER BY bgc.thoiGianDuyet DESC;
GO

-- ======================================================================================
-- 3. KIỂM TRA RÀNG BUỘC PHÂN CÔNG CA TRỰC TRONG TUẦN
-- ======================================================================================
PRINT '=== PHÂN BỔ CA TRỰC THEO NHÂN VIÊN TRONG TUẦN HIỆN TẠI ===';

SELECT nv.maNhanVien, nv.hoTen, COUNT(pc.maPhanCong) AS TongSoCaTrongTuan
FROM NhanVien nv
LEFT JOIN BangPhanCong pc ON nv.maNhanVien = pc.maNhanVien
  AND pc.ngayLamViec >= DATEADD(day, 1 - DATEPART(weekday, GETDATE()), CAST(GETDATE() AS DATE))
  AND pc.ngayLamViec <= DATEADD(day, 7 - DATEPART(weekday, GETDATE()), CAST(GETDATE() AS DATE))
GROUP BY nv.maNhanVien, nv.hoTen;
GO
```
