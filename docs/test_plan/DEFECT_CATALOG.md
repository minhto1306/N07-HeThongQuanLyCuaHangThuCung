# DANH MỤC LỖI KỸ THUẬT & LỖ HỔNG HỆ THỐNG
## (DEFECT & GAP CATALOG - COMPREHENSIVE AUDIT)

**Dự án:** N07 - Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)  
**Phiên bản tài liệu:** 1.0 (Tổng hợp toàn diện)  
**Ngày phát hành:** 07/11/2026  
**Nguồn dữ liệu:** Tổng hợp từ 3 báo cáo khảo sát độc lập (`explorer_survey_1`, `explorer_survey_2`, `explorer_survey_3`)  
**Phạm vi:** 6 phân hệ nghiệp vụ, Kiến trúc N-Tier, Giao dịch JDBC & CSDL Microsoft SQL Server  

---

## 1. BẢNG TỔNG HỢP SỐ LƯỢNG LỖI THEO PHÂN HỆ & MỨC ĐỘ

Hệ thống đã nhận diện và chuẩn hóa tổng cộng **49 lỗi kỹ thuật & lỗ hổng hệ thống**, phân bố theo mức độ nghiêm trọng và phân hệ như sau:

| STT | Phân Hệ Nghiệp Vụ / Lĩnh Vực | Blocker | Critical | Major | Minor | Tổng Số Lỗi |
|:---:|---|:---:|:---:|:---:|:---:|:---:|
| 1 | **POS & Bán Hàng, Đổi Trả Hàng** | 1 | 4 | 3 | 1 | **9** |
| 2 | **Quản Lý Kho, Sản Phẩm & Lô Hàng (FEFO)** | 2 | 1 | 4 | 1 | **8** |
| 3 | **Khách Hàng, Thú Cưng & Spa / Pet Hotel** | 2 | 4 | 5 | 2 | **13** |
| 4 | **Quản Lý Nhân Sự, Phân Ca & Bàn Giao Ca** | 1 | 0 | 3 | 0 | **4** |
| 5 | **Bảo Mật, Đăng Nhập & Phân Quyền (RBAC)** | 0 | 1 | 2 | 1 | **4** |
| 6 | **Báo Cáo & Thống Kê Quản Trị** | 1 | 2 | 3 | 2 | **8** |
| 7 | **Toàn Cục Cơ Sở Dữ Liệu & Hạ Tầng JDBC** | 0 | 0 | 3 | 0 | **3** |
| **TỔNG** | **Toàn Bộ Hệ Thống** | **7** | **12** | **23** | **7** | **49** |

---

## 2. DANH MỤC CHI TIẾT CÁC LỖI MỨC BLOCKER (7 LỖI)

---

### DEF-BLK-01: Tràn số và xung đột khóa chính khi tạo trên 99 hóa đơn trong một ngày
- **Phân hệ & Module:** POS / Bán Hàng & Cơ Sở Dữ Liệu
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: Dòng 618-629 (`dbo.fn_SinhMaHoaDon`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Quầy POS thực hiện thanh toán các đơn hàng trong ngày hôm nay.
  2. Khi cửa hàng bán đến đơn thứ 99, mã hóa đơn sinh ra là `HD{yyMMdd}-99`.
  3. Khi thực hiện thanh toán đơn thứ 100, hàm `fn_SinhMaHoaDon` sinh mã `HD{yyMMdd}-00`.
  4. Khi thanh toán đơn thứ 101, hàm tiếp tục sinh mã `HD{yyMMdd}-01` (trùng với hóa đơn số 1 buổi sáng).
  5. CSDL văng ngoại lệ `Violation of PRIMARY KEY constraint 'PK__HoaDon...'`. Toàn bộ quầy thu ngân bị tê liệt hoàn toàn từ đơn hàng thứ 100 trở đi trong ngày.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Trong UDF `fn_SinhMaHoaDon`:
    ```sql
    RETURN @prefix + RIGHT('00' + CAST(@max + 1 AS VARCHAR(10)), 2);
    ```
  - Biểu thức dùng `RIGHT('00' + ..., 2)` để định dạng số thứ tự gồm 2 chữ số. Khi `@max = 99`, `@max + 1 = 100`. Biểu thức `RIGHT('00100', 2)` cắt lấy 2 ký tự cuối là `'00'`. Đến đơn 101, `@max` vẫn tìm thấy 99 là số lớn nhất (vì chuỗi `'99' > '00'`), dẫn đến `@max + 1 = 100` tiếp tục sinh `'00'` hoặc `'01'`, gây trùng lặp khóa chính.
- **Mức độ ảnh hưởng:** Tê liệt vận hành kinh doanh quầy POS, chặn đứng hoạt động bán hàng của cửa hàng khi đạt doanh số cao.
- **Đề xuất khắc phục (Remediation):**
  - Mở rộng độ dài định dạng số thứ tự từ 2 chữ số lên tối thiểu 4 chữ số:
    ```sql
    RETURN @prefix + RIGHT('0000' + CAST(@max + 1 AS VARCHAR(10)), 4);
    ```
- **Kịch bản SQL kiểm chứng:**
  ```sql
  DECLARE @prefix VARCHAR(10) = 'HD' + CONVERT(VARCHAR(6), GETDATE(), 12) + '-';
  DECLARE @max INT = 99;
  SELECT @prefix + RIGHT('00' + CAST(@max + 1 AS VARCHAR(10)), 2) AS [MaSinhRa_Don100];
  -- Kết quả: 'HDyyMMdd-00' (Lỗi cắt cụt số 1 của 100)
  ```

---

### DEF-BLK-02: Tự động xuất bán lô hàng ĐÃ HẾT HẠN cho khách hàng trước tiên
- **Phân hệ & Module:** Quản Lý Kho & Điểm Bán Hàng POS (FEFO)
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2791, 2845-2850; `src/dao/LoHangDAO.java`: Dòng 33-38 (`getAllLoHang`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Trong kho có một lô hàng thực phẩm thú cưng đã hết hạn sử dụng từ hôm qua nhưng vẫn còn tồn 5 gói.
  2. Thu ngân mở màn hình POS và thêm sản phẩm đó vào giỏ hàng để bán cho khách.
  3. Bấm "Thanh toán (F9)". Hệ thống tự động bốc lô hết hạn nói trên và trừ kho thành công, in hóa đơn xuất bán hàng hết hạn cho khách.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Tại `BanHangPOSPanel.java:2791`:
    ```java
    List<entity.LoHang> allLo = busThanhToan.layTatCaLoHang(); // Gọi LoHangDAO.getAllLoHang()
    ```
  - Trong `LoHangDAO.getAllLoHang()`, câu lệnh SQL là `SELECT l.* ... ORDER BY l.hanSuDung ASC` nhưng **hoàn toàn không có điều kiện `hanSuDung >= CAST(GETDATE() AS DATE)`**.
  - Các lô hết hạn có hạn dùng cũ nhất nên luôn nằm ở vị trí đầu tiên của danh sách. POS duyệt vòng lặp chỉ kiểm tra `candidate.getSoLuongTon() > 0`, do đó tự động chọn lô hết hạn xuất cho khách trước tiên!
- **Mức độ ảnh hưởng:** Vi phạm pháp luật an toàn thú y nghiêm trọng, gây nguy hiểm đến tính mạng vật nuôi, hủy hoại uy tín thương hiệu cửa hàng.
- **Đề xuất khắc phục (Remediation):**
  - Sửa `BanHangPOSPanel` gọi phương thức chuẩn `LoHangDAO.getAvailableLotsFEFO(maSP)` (đã có sẵn điều kiện `hanSuDung >= CAST(GETDATE() AS DATE)`). Đồng thời bổ sung điều kiện lọc hạn dùng vào `getAllLoHang()`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  -- Kiểm tra các lô hàng hết hạn có tồn kho
  SELECT soLo, maSanPham, hanSuDung, soLuongTon 
  FROM LoHang 
  WHERE hanSuDung < CAST(GETDATE() AS DATE) AND soLuongTon > 0;
  ```

---

### DEF-BLK-03: Báo cáo Xuất-Nhập-Tồn lịch sử bị sai lệch hoàn toàn do lấy tồn kho thời gian thực ở hiện tại
- **Phân hệ & Module:** Báo Cáo & Thống Kê Quản Trị
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/dao/BaoCaoThongKeDAO.java`: Dòng 327-334, 357 (`getXuatNhapTon`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Quản lý mở Báo cáo Xuất - Nhập - Tồn cho khoảng thời gian của tháng trước (ví dụ 01/10/2026 đến 31/10/2026).
  2. Bấm "Tra cứu báo cáo".
  3. Quan sát thấy cột "Tồn đầu kỳ" và "Tồn cuối kỳ" bị lệch hoàn toàn so với thực tế, nhiều sản phẩm hiển thị tồn đầu kỳ là số âm!
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Trong `BaoCaoThongKeDAO.getXuatNhapTon()`:
    ```sql
    WITH L AS (SELECT maSanPham, SUM(soLuongTon) tonCuoi FROM LoHang GROUP BY maSanPham)
    ```
    và dòng 357:
    ```java
    r.tonDauKy = r.tonCuoiKy - r.nhapTrongKy + r.xuatBanTrongKy - r.hoanTraTrongKy;
    ```
  - Biến `tonCuoiKy` bị lấy trực tiếp từ `SUM(soLuongTon)` của bảng `LoHang` ở thời điểm hiện tại (hôm nay). Sau đó lại dùng công thức tính ngược để suy ra `tonDauKy`. Khi xem báo cáo quá khứ, các giao dịch xuất/nhập phát sinh từ ngày 01/11 đến nay làm công thức tính ngược bị sai hoàn toàn.
- **Mức độ ảnh hưởng:** Báo cáo kiểm toán kho và báo cáo tài chính bị vô hiệu hóa; sai lệch số liệu quản trị doanh nghiệp.
- **Đề xuất khắc phục (Remediation):**
  - Xây dựng bảng sổ cái ghi nhận biến động thẻ kho lịch sử (`SoKhoLichSu`) hoặc tính toán tồn kho lịch sử dựa trên tổng nhập tích lũy trừ tổng xuất tích lũy tính đến mốc thời gian `denNgay`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  -- Tồn cuối hiện tại hoàn toàn không phụ thuộc vào tham số thời gian truyền vào
  SELECT maSanPham, SUM(soLuongTon) AS TonHienTai FROM LoHang GROUP BY maSanPham;
  ```

---

### DEF-BLK-04: Rò rỉ kết nối JDBC (Connection Leak) trong `ThuCungDAO`
- **Phân hệ & Module:** Khách Hàng, Thú Cưng & CSDL
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/dao/ThuCungDAO.java`: Dòng 189-194 (`insertThuCung`), dòng 248-252 (`deleteThuCung`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Nhân viên thực hiện tiếp nhận thú cưng hoặc xóa hồ sơ thú cưng trên giao diện `ThuCungDichVuPanel`.
  2. Thao tác lặp lại 15-20 lần.
  3. Toàn bộ ứng dụng bị đơ cứng, không thể mở bất kỳ cửa sổ nào khác; kết nối tới SQL Server bị timeout.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Trong khối `finally` của `insertThuCung`:
    ```java
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
    ```
  - Hoàn toàn thiếu câu lệnh `conn.close();`. Mỗi thao tác thêm/xóa thú cưng để lại 1 kết nối TCP vật lý ở trạng thái mở vĩnh viễn trên SQL Server.
- **Mức độ ảnh hưởng:** Làm cạn kiệt tài nguyên kết nối của SQL Server, khiến toàn bộ hệ thống bị treo cứng.
- **Đề xuất khắc phục (Remediation):**
  - Bổ sung `conn.close()` trong khối `finally` hoặc chuyển toàn bộ sang cú pháp `try (Connection conn = ...)`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  SELECT session_id, host_name, program_name, status 
  FROM sys.dm_exec_sessions 
  WHERE program_name LIKE '%JDBC%';
  ```

---

### DEF-BLK-05: Rò rỉ kết nối JDBC (Connection Leak) trong `NhanVienDAO`
- **Phân hệ & Module:** Quản Lý Nhân Sự & CSDL
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/dao/NhanVienDAO.java`: Dòng 161-165 (`insertNhanVien`), dòng 203-207 (`updateNhanVien`), dòng 239-243 (`deleteNhanVien`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Quản lý thực hiện thêm mới, cập nhật hoặc xóa nhân viên trong `QuanLyNhanVienPanel`.
  2. Thao tác xong, kiểm tra số lượng connection trên SQL Server thấy tăng dần và không giải phóng.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Cả 3 phương thức `insertNhanVien`, `updateNhanVien`, `deleteNhanVien` chỉ gọi `conn.setAutoCommit(true);` trong `finally` mà không hề gọi `conn.close();`.
- **Mức độ ảnh hưởng:** Cạn kiệt connection pool của máy chủ CSDL, gây sập kết nối các phân hệ khác.
- **Đề xuất khắc phục (Remediation):** Bổ sung `conn.close();` trong khối `finally`.
- **Kịch bản SQL kiểm chứng:** Tương tự kịch bản kiểm tra `sys.dm_exec_sessions` của DEF-BLK-04.

---

### DEF-BLK-06: Rò rỉ kết nối JDBC trong `SanPhamDAO` khi xóa sản phẩm
- **Phân hệ & Module:** Quản Lý Kho & CSDL
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/dao/SanPhamDAO.java`: Dòng 207-211 (`deleteSanPham`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Nhân viên quản lý kho thực hiện xóa một sản phẩm trong danh mục.
  2. Khối `finally` của `deleteSanPham` chạy nhưng không đóng kết nối.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Khối `finally` chỉ có `conn.setAutoCommit(true);` và thiếu lệnh `conn.close()`.
- **Mức độ ảnh hưởng:** Rò rỉ tài nguyên mạng và socket kết nối của SQL Server.
- **Đề xuất khắc phục (Remediation):** Thêm `conn.close();` vào khối `finally`.
- **Kịch bản SQL kiểm chứng:** Kiểm tra số phiên `sys.dm_exec_sessions`.

---

### DEF-BLK-07: Rò rỉ kết nối JDBC trong `LichHenDAO` khi xóa lịch hẹn
- **Phân hệ & Module:** Spa & Dịch Vụ / CSDL
- **Mức độ nghiêm trọng:** **Blocker**
- **Vị trí mã nguồn:** `src/dao/LichHenDAO.java`: Dòng 233-236 (`deleteLichHen`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Nhân viên thực hiện hủy/xóa một lịch hẹn chăm sóc thú cưng.
  2. Phương thức thực thi xong nhưng kết nối CSDL bị treo vĩnh viễn.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Khối `finally` chỉ phục hồi auto-commit mà không gọi `conn.close()`.
- **Mức độ ảnh hưởng:** Gây tràn connection pool khi lượng khách hủy/đổi lịch hẹn cao.
- **Đề xuất khắc phục (Remediation):** Bổ sung `conn.close();` trong khối `finally`.
- **Kịch bản SQL kiểm chứng:** Kiểm tra số phiên `sys.dm_exec_sessions`.

---

## 3. DANH MỤC CHI TIẾT CÁC LỖI MỨC CRITICAL (12 LỖI)

---

### DEF-CRI-01: Tài khoản nhân viên bị khóa (`trangThaiHoatDong = 0`) vẫn đăng nhập thành công
- **Phân hệ & Module:** Bảo Mật, Đăng Nhập & Phân Quyền
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: Dòng 440-441; `src/dao/TaiKhoanDAO.java`: Dòng 89-108; `src/bus/DangNhapBus.java`: Dòng 83-93
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Tài khoản nhân viên `NV006` (nhân viên đã nghỉ việc) có `trangThaiHoatDong = 0` trong CSDL.
  2. Mở màn hình đăng nhập `LoginForm`.
  3. Nhập SĐT `0956789012` và Mật khẩu `123456`.
  4. Hệ thống thông báo "Đăng nhập thành công!" và mở thẳng vào `MainDashboard` với đầy đủ quyền hạn của nhân viên đó!
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Trong thủ tục SQL `sp_DangNhap`:
    ```sql
    WHERE (tk.soDienThoai = @identifier OR tk.email = @identifier OR tk.maTaiKhoan = @identifier)
      AND tk.matKhau = @hashed;
    ```
    Hoàn toàn không có điều kiện `AND tk.trangThaiHoatDong = 1`.
  - Trong `DangNhapBus.xuLyDangNhap()`:
    ```java
    if (tk != null) {
        // Lập tức khởi tạo UserSession và trả về thành công!
        utils.UserSession.initSession(nv);
        return new KetQuaDangNhap(true, "Đăng nhập thành công!", nv);
    }
    ```
    Đoạn kiểm tra `!tonTai.isTrangThaiHoatDong()` nằm ở khối `else` (chỉ chạy khi sai mật khẩu). Nếu nhập đúng mật khẩu thì bypass hoàn toàn!
- **Mức độ ảnh hưởng:** Lỗ hổng bảo mật nghiêm trọng. Nhân viên bị sa thải, đình chỉ vẫn đăng nhập vào sửa đổi dữ liệu và bán hàng.
- **Đề xuất khắc phục (Remediation):**
  - Thêm điều kiện `AND tk.trangThaiHoatDong = 1` vào `sp_DangNhap`.
  - Trong `DangNhapBus.xuLyDangNhap`, kiểm tra `if (!tk.isTrangThaiHoatDong()) return new KetQuaDangNhap(false, "Tài khoản đã bị khóa!");` ngay trước khi cấp quyền.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  EXEC dbo.sp_DangNhap @identifier = '0956789012', @matKhauRaw = '123456';
  -- Quan sát: Vẫn trả về 1 dòng bản ghi dù trangThaiHoatDong = 0!
  ```

---

### DEF-CRI-02: Lợi dụng điểm tích lũy vô tận: Trừ tiền trên hóa đơn nhưng không khấu trừ điểm trong CSDL
- **Phân hệ & Module:** POS / Bán Hàng & Quản Lý Khách Hàng
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2771-2785; `src/bus/BanHangBus.java`: Dòng 265-291; `src/dao/HoaDonDAO.java`: Dòng 181-190
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách hàng thân thiết có 50 điểm tích lũy. Mua đơn hàng 200,000đ.
  2. Tại ô "Dùng điểm", thu ngân chọn 20 điểm (giảm 20,000đ, khách trả 180,000đ).
  3. Thanh toán thành công.
  4. Kiểm tra lại CSDL thấy điểm của khách không những không bị trừ 20 điểm (về 30) mà còn tăng lên 51 điểm (được cộng thêm 1 điểm từ 180k)!
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Tại `BanHangPOSPanel.java:2774`, code tính `tongDiemMoi = Math.max(0, diemHienCo - diemDaDung) + diemThuongMoi;`. Nhưng biến này bị bỏ quên, dòng 2780 truyền `diemHienCo` (số điểm cũ) vào lưu khách hàng.
  - Trong `HoaDonDAO.thanhToanHoaDon:181-190`, câu lệnh SQL duy nhất cập nhật điểm là: `UPDATE KhachHang SET diemTichLuy = diemTichLuy + ?` (cộng thêm điểm thưởng). Điểm đã dùng không bao giờ bị trừ trong SQL Server.
- **Mức độ ảnh hưởng:** Thất thoát tài chính nghiêm trọng; khách hàng có thể dùng số điểm tích lũy vô số lần để lấy tiền của cửa hàng.
- **Đề xuất khắc phục (Remediation):**
  - Trong `HoaDonDAO.thanhToanHoaDon`, thực thi câu lệnh trừ số điểm đã dùng: `UPDATE KhachHang SET diemTichLuy = diemTichLuy - ? + ? WHERE maKhachHang = ?`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  SELECT maKhachHang, hoTen, soDienThoai, diemTichLuy FROM KhachHang WHERE soDienThoai = '0988111222';
  ```

---

### DEF-CRI-03: Hàm UDF `fn_SinhMaLichHen` văng lỗi ép kiểu cú pháp khi sang ngày mới
- **Phân hệ & Module:** Spa, Dịch Vụ & Cơ Sở Dữ Liệu
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: Dòng 641-648; `src/dao/LichHenDAO.java`: Dòng 22-23
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Ngày hôm trước hệ thống đã tạo các lịch hẹn dạng `LH261107-001`.
  2. Sang ngày hôm sau (chưa có lịch hẹn nào của ngày mới), nhân viên bấm đặt lịch hẹn cho khách.
  3. Quá trình lưu thất bại; CSDL văng lỗi hoặc mã sinh ra bị trùng lặp `LH001` với dữ liệu ban đầu.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Trong `fn_SinhMaLichHen`:
    ```sql
    IF @max = 0
    BEGIN
        SELECT @max = ISNULL(MAX(CAST(SUBSTRING(maLichHen, 3, LEN(maLichHen)) AS INT)), 0)
        FROM LichHen WHERE maLichHen LIKE 'LH[0-9]%';
    ```
  - Khi sang ngày mới, `@max` theo tiền tố ngày bằng 0. Khối fallback chạy và quét các mã có dạng `LH261107-001` (khớp `LIKE 'LH[0-9]%'`). Đoạn `SUBSTRING('LH261107-001', 3, ...)` lấy ra chuỗi `'261107-001'`. Lệnh `CAST('261107-001' AS INT)` văng lỗi cú pháp: `Conversion failed when converting the varchar value '261107-001' to data type int.`
  - `LichHenDAO` bắt lỗi âm thầm và fallback về `"LH001"`, gây trùng khóa chính với dữ liệu hạt giống.
- **Mức độ ảnh hưởng:** Chặn đứng hoàn toàn chức năng đặt lịch hẹn dịch vụ của ngày mới.
- **Đề xuất khắc phục (Remediation):** Bỏ nhánh fallback ép kiểu lỗi; khi `@max = 0` của ngày mới thì trả về `@prefix + '001'`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  SELECT CAST(SUBSTRING('LH261107-001', 3, 10) AS INT); -- Văng Msg 245 Conversion failed
  ```

---

### DEF-CRI-04: Hàm UDF `fn_SinhMaPhieuDoiTra` văng lỗi ép kiểu cú pháp khi sang ngày mới
- **Phân hệ & Module:** Đổi Trả Hàng & Cơ Sở Dữ Liệu
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: Dòng 665-672; `src/dao/HoaDonDAO.java`: Dòng 38-42
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  - Tương tự DEF-CRI-03, khi sang ngày mới chưa có phiếu đổi trả nào, hàm `fn_SinhMaPhieuDoiTra` chạy nhánh fallback `LIKE 'DT[0-9]%'` và cố ép kiểu chuỗi `'261107-001'` sang INT gây sập hàm T-SQL.
- **Phân tích nguyên nhân gốc rễ (Root Cause):** Lỗi logic tương tự trong nhánh fallback của hàm sinh mã đổi trả.
- **Mức độ ảnh hưởng:** Chặn đứng quy trình đổi trả hàng khi sang ngày mới.
- **Đề xuất khắc phục (Remediation):** Chuẩn hóa định dạng mã đổi trả theo ngày `@prefix + RIGHT('000' + (@max + 1), 3)` và xóa bỏ khối fallback ép kiểu sai.
- **Kịch bản SQL kiểm chứng:** Chạy thử `SELECT dbo.fn_SinhMaPhieuDoiTra()` khi có bản ghi ngày cũ.

---

### DEF-CRI-05: Báo cáo Top Bán Chạy cộng gộp sản lượng sai bản chất do bỏ qua tỷ lệ quy đổi ĐVT
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/dao/BaoCaoThongKeDAO.java`: Dòng 161-165, 177-181 (`getTopBanChay`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách hàng A mua 1 Thùng Pate (quy cách 24 lon). Khách hàng B mua 2 Lon Pate lẻ.
  2. Mở Báo cáo Top Bán Chạy.
  3. Sản lượng bán của mặt hàng bán theo Thùng được tính là 1, trong khi bán lẻ được tính là 2.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Câu lệnh SQL dùng `SUM(ct.soLuong)` trực tiếp trên `ChiTietHoaDon` mà không `JOIN` với `QuyDoiDonVi` để nhân với `tyLeQuyDoi`. Do đó 1 Thùng (24 lon) chỉ được tính số lượng là 1.
- **Mức độ ảnh hưởng:** Báo cáo sản lượng bị méo mó, dẫn đến quyết định nhập hàng sai lầm của ban quản trị.
- **Đề xuất khắc phục (Remediation):** Đưa số lượng về đơn vị cơ sở: `SUM(ct.soLuong * ISNULL(qd.tyLeQuyDoi, 1)) AS soLuongCoSo`.
- **Kịch bản SQL kiểm chứng:** So sánh `SUM(soLuong)` với `SUM(soLuong * tyLeQuyDoi)`.

---

### DEF-CRI-06: Sai lệch số lượng và đơn giá dòng hàng khi xuất kho phân tách nhiều lô tại POS
- **Phân hệ & Module:** Quản Lý Kho & Điểm Bán Hàng POS
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2854-2861
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách mua 1 Thùng Pate (24 lon) với giá 450,000đ.
  2. Kho phải bốc từ 2 lô: Lô 1 lấy 10 lon, Lô 2 lấy 14 lon.
  3. Hóa đơn ghi nhận 2 dòng `ChiTietXuatLo`. Dòng 1 ghi số lượng 10 nhưng đơn giá vẫn là 450,000đ. Tích `10 * 450,000 = 4,500,000đ` lệch hoàn toàn với `thanhTien` thực tế là 187,500đ.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Biến `soLuong` của `ChiTietXuatLo` bị gán bằng số lượng lon lẻ cơ sở `take`, trong khi đơn giá `dg` vẫn giữ nguyên là đơn giá của cả Thùng lớn.
- **Mức độ ảnh hưởng:** Phá vỡ tính toàn vẹn kế toán dòng hàng; báo cáo doanh thu chi tiết bị sai lệch số liệu.
- **Đề xuất khắc phục (Remediation):** Đơn giá dòng xuất lô phải được quy đổi về giá đơn vị cơ sở `dg / qd.tyLeQuyDoi`.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  SELECT c.soLuong, c.donGiaLucBan, c.thanhTien, xl.soLuongXuatCoSo 
  FROM ChiTietHoaDon c JOIN ChiTietXuatLo xl ON c.maChiTiet = xl.maChiTiet;
  ```

---

### DEF-CRI-07: Check-out phòng Pet Hotel không tính phụ phí quá giờ (Overtime Fee)
- **Phân hệ & Module:** Khách Sạn Thú Cưng (Pet Hotel)
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/gui/PetHotelPanel.java`: Dòng 991-996 (`thucHienCheckOut`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách gửi thú cưng check-in lúc 08:00 sáng ngày 05/11, hẹn đón lúc 10:00 sáng ngày 07/11 (2 ngày).
  2. Thực tế khách đón trễ vào 20:00 tối ngày 07/11 (quá giờ 10 tiếng).
  3. Bấm Check-out: Hệ thống chỉ tính tiền đúng 2 ngày, không hề tính phụ phí trễ giờ.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Code chỉ tính `ChronoUnit.DAYS.between(checkIn, now)`, hoàn toàn bỏ quên trường `duKienCheckOut` và không có logic phụ thu quá giờ.
- **Mức độ ảnh hưởng:** Thất thu doanh thu phòng khách sạn thú cưng, thiếu công bằng trong kinh doanh.
- **Đề xuất khắc phục (Remediation):** So sánh `now` với `duKienCheckOut`; nếu trễ > 2 tiếng thì phụ thu 50% đơn giá ngày, nếu trễ > 6 tiếng thì phụ thu 100% đơn giá ngày.
- **Kịch bản SQL kiểm chứng:** Kiểm tra các bản ghi trong `LichSuLuuTru`.

---

### DEF-CRI-08: Không có JDBC Transaction giữa ghi lịch sử lưu trú và cập nhật trạng thái phòng trống
- **Phân hệ & Module:** Pet Hotel & Cơ Sở Dữ Liệu
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/bus/PetHotelBus.java`: Dòng 40, 53
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Thu ngân thực hiện Check-out trả phòng.
  2. Bước 1: Ghi lịch sử lưu trú thành công.
  3. Bước 2: Cập nhật phòng về trạng thái "Trống" bị lỗi mạng hoặc crash.
  4. Lịch sử đã ghi nhận khách đã về nhưng phòng trên sơ đồ vẫn báo "Đang có khách", phòng bị treo không thể đón khách mới.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Gọi 2 phương thức DAO độc lập không được bao bọc trong cùng một Transaction `conn.setAutoCommit(false)`.
- **Mức độ ảnh hưởng:** Gây bất đồng bộ trạng thái phòng Pet Hotel.
- **Đề xuất khắc phục (Remediation):** Gom 2 thao tác vào một giao dịch ACID duy nhất trong DAO.
- **Kịch bản SQL kiểm chứng:** `SELECT trangThaiPhong FROM PhongLuuTru WHERE maPhong = 'P101';`

---

### DEF-CRI-09: Bất đồng bộ dữ liệu Thẻ Spa Pass giữa RAM và CSDL khi trừ số buổi
- **Phân hệ & Module:** Spa & Dịch Vụ
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/bus/SpaPassBus.java`: Dòng 102-108 (`suDungMotBuoi`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách dùng 1 buổi Spa Pass.
  2. RAM trừ thành công 1 buổi (từ 10 về 9).
  3. Hàm gọi `dao.updateTheSpaPass(the)` gặp lỗi CSDL.
  4. Khối catch nuốt lỗi, hàm vẫn trả về `true` báo thành công. Khi tắt ứng dụng mở lại, số buổi trên CSDL vẫn là 10.
- **Phân tích nguyên nhân gốc rễ (Root Cause):** Trừ trên RAM trước khi ghi CSDL; nuốt ngoại lệ CSDL và trả về kết quả thành công sai sự thật.
- **Mức độ ảnh hưởng:** Mất đồng bộ dữ liệu giữa phiên làm việc và CSDL, sai lệch quyền lợi khách hàng.
- **Đề xuất khắc phục (Remediation):** Cập nhật CSDL thành công trước rồi mới cập nhật đối tượng RAM; nếu CSDL lỗi phải ném ngoại lệ rollback.
- **Kịch bản SQL kiểm chứng:** `SELECT soBuoiDaDung, soBuoiConLai FROM TheSpaPass WHERE maThe = 'SP001';`

---

### DEF-CRI-10: Nguy cơ `NullPointerException` làm sập giao diện khi nạp báo cáo tồn kho FEFO
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/dao/BaoCaoThongKeDAO.java`: Dòng 314-315
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Trong CSDL có một lô hàng có trường `ngaySanXuat` bị NULL (hoặc hàng phụ kiện không có NSX).
  2. Quản lý mở Tab 6 "Cảnh báo hạn dùng FEFO".
  3. Giao diện bị đơ và văng `NullPointerException` trên console.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Gọi trực tiếp `rs.getDate("ngaySanXuat").toLocalDate()`. Khi `rs.getDate()` trả về `null`, phương thức `.toLocalDate()` ném ngay ngoại lệ NPE.
- **Mức độ ảnh hưởng:** Sập giao diện xem báo cáo hạn dùng FEFO.
- **Đề xuất khắc phục (Remediation):** Kiểm tra null an toàn: `java.sql.Date d = rs.getDate("ngaySanXuat"); r.ngaySanXuat = (d != null) ? d.toLocalDate() : null;`.
- **Kịch bản SQL kiểm chứng:** `SELECT soLo, ngaySanXuat, hanSuDung FROM LoHang WHERE ngaySanXuat IS NULL;`

---

### DEF-CRI-11: Khối `finally` đóng Statement dễ gây ngoại lệ che giấu `conn.close()` trong `HoaDonDAO`
- **Phân hệ & Module:** POS / Bán Hàng & Cơ Sở Dữ Liệu
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/dao/HoaDonDAO.java`: Dòng 208-219 (`luuHoaDon`)
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  - Trong khối `finally`, 6 câu lệnh `ps.close()` được viết tuần tự không có try-catch riêng. Nếu `psHoaDon.close()` phát sinh lỗi, các lệnh đóng `conn.close()` phía dưới bị bỏ qua.
- **Phân tích nguyên nhân gốc rễ (Root Cause):** Khối đóng tài nguyên JDBC thiếu tính phòng thủ (defensive programming).
- **Mức độ ảnh hưởng:** Gây rò rỉ kết nối ngầm tại luồng thanh toán cốt lõi của POS.
- **Đề xuất khắc phục (Remediation):** Dùng try-with-resources cho toàn bộ `Connection` và `PreparedStatement`.
- **Kịch bản SQL kiểm chứng:** Kiểm tra rò rỉ session sau khi giả lập lỗi đóng Statement.

---

### DEF-CRI-12: Hàng đổi trả nguyên tem chỉ cộng kho ảo trên RAM, không nhập lại kho trong CSDL SQL Server
- **Phân hệ & Module:** Đổi Trả Hàng & Quản Lý Kho
- **Mức độ nghiêm trọng:** **Critical**
- **Vị trí mã nguồn:** `src/gui/DoiTraHangPanel.java`: Dòng 803-823; `src/gui/BanHangPOSPanel.java`: Dòng 2932-2940; `src/dao/HoaDonDAO.java`: Dòng 429-463
- **Mô tả hiện tượng & Kịch bản tái hiện:**
  1. Khách hàng trả lại 2 gói hạt khô còn nguyên tem.
  2. Hệ thống in phiếu đổi trả thành công.
  3. Mở CSDL SQL Server kiểm tra bảng `LoHang`: Số lượng tồn kho hoàn toàn không tăng thêm 2 đơn vị.
- **Phân tích nguyên nhân gốc rễ (Root Cause):**
  - Hệ thống chỉ gọi hàm sửa biến trên RAM. Trong `HoaDonDAO.luuPhieuDoiTra`, chỉ có lệnh `INSERT INTO PhieuDoiTra` và `ChiTietDoiTra`, hoàn toàn thiếu lệnh `UPDATE LoHang SET soLuongTon = soLuongTon + ?`. Đồng thời `ChiTietDoiTra` không lưu số lô (`soLo`).
- **Mức độ ảnh hưởng:** Hao hụt tồn kho thực tế trong CSDL; sai lệch số liệu quản trị kho vĩnh viễn.
- **Đề xuất khắc phục (Remediation):** Bổ sung cột `soLo` vào `ChiTietDoiTra` và thực thi câu lệnh UPDATE cộng lại tồn kho lô trong giao dịch lưu phiếu đổi trả.
- **Kịch bản SQL kiểm chứng:**
  ```sql
  SELECT soLo, maSanPham, soLuongTon FROM LoHang WHERE maSanPham = 'SP001';
  ```

---

## 4. DANH MỤC CHI TIẾT CÁC LỖI MỨC MAJOR (23 LỖI)

---

### DEF-MAJ-01: Không giới hạn số lần đăng nhập sai & không có cơ chế khóa tạm thời (Lỗ hổng Brute-Force)
- **Phân hệ & Module:** Bảo Mật & Đăng Nhập
- **Vị trí mã nguồn:** `src/gui/LoginForm.java`: 270-295; `src/bus/DangNhapBus.java`: 69-115; `sql/...sql`: 33-41
- **Nguyên nhân & Hiện tượng:** Bảng `TaiKhoan` và tầng BUS không lưu số lần thử sai, không có lockout thời gian, không có CAPTCHA. Kẻ xấu có thể chạy script tấn công vét cạn mật khẩu.
- **Tác động:** Nguy cơ bị chiếm đoạt tài khoản Quản lý và nhân viên.
- **Khắc phục:** Bổ sung cột `soLanDangNhapSai`, `thoiGianKhoaTamThoi` trong `TaiKhoan` và khóa tài khoản 15 phút nếu nhập sai quá 5 lần.

### DEF-MAJ-02: Phiên làm việc mặc định phân quyền Quản lý (NVQL) khi chưa đăng nhập hoặc sau khi đăng xuất
- **Phân hệ & Module:** Bảo Mật & Phân Quyền
- **Vị trí mã nguồn:** `src/utils/UserSession.java`: Dòng 50, 64, 110, 256
- **Nguyên nhân & Hiện tượng:** `currentRole` khởi tạo là `Role.NVQL` và `clearSession()` cũng gán về `Role.NVQL`. Bất kỳ luồng kiểm tra quyền nào khi chưa đăng nhập đều trả về `true` (toàn quyền quản trị).
- **Tác động:** Lỗ hổng kiểm soát truy cập (Access Control Bypass).
- **Khắc phục:** Khởi tạo `currentRole = null` hoặc `Role.GUEST`; hàm `canAccess` trả về `false` nếu phiên chưa được xác thực.

### DEF-MAJ-03: Không thể xuất bán đơn hàng lớn nếu số lượng vượt quá tồn của một lô đơn lẻ (Lỗi FEFO đơn lô)
- **Phân hệ & Module:** POS / Bán Hàng & Quản Lý Kho
- **Vị trí mã nguồn:** `src/bus/BanHangBus.java`: Dòng 428-441 (`themSanPhamXuatLo`)
- **Nguyên nhân & Hiện tượng:** Thuật toán chỉ tìm 1 lô duy nhất có tồn `>=` số lượng cần mua. Nếu Lô 1 có 5 sp, Lô 2 có 10 sp, khách mua 12 sp (tổng kho 15 sp), hệ thống báo lỗi không đủ hàng. Nếu khách mua 8 sp thì nhảy sang bốc Lô 2, bỏ qua Lô 1 cận hạn hơn.
- **Tác động:** Phá vỡ nguyên tắc FEFO và từ chối bán hàng sai thực tế.
- **Khắc phục:** Viết lại thuật toán phân bổ xuất kho đa lô theo thứ tự HSD tăng dần cho đến khi đủ số lượng yêu cầu.

### DEF-MAJ-04: Giao dịch không nguyên tử khi thanh toán đổi hàng tại POS (Nguy cơ Partial Commit)
- **Phân hệ & Module:** POS & Đổi Trả Hàng
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2918-2930
- **Nguyên nhân & Hiện tượng:** POS gọi `luuPhieuDoiTra` (kết nối 1, commit) rồi mới gọi `luuHoaDonPOS` (kết nối 2, commit). Nếu bước 2 lỗi, phiếu đổi trả đã lưu nhưng hóa đơn mua mới không có.
- **Tác động:** Bất nhất dữ liệu kế toán tài chính.
- **Khắc phục:** Gộp việc lưu phiếu đổi trả và hóa đơn POS vào cùng một JDBC Transaction duy nhất.

### DEF-MAJ-05: Xung đột mã hóa đơn khi nhiều thu ngân mở quầy bán hàng cùng lúc (Concurrency Race Condition)
- **Phân hệ & Module:** POS / Bán Hàng
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: 618-629; `src/gui/BanHangPOSPanel.java`: 2724, 2794
- **Nguyên nhân & Hiện tượng:** `fn_SinhMaHoaDon` được gọi lúc mở màn hình trước khi thanh toán. Hai quầy mở cùng lúc sẽ nhận cùng một mã; quầy thanh toán sau bị lỗi Primary Key Duplicate.
- **Tác động:** Thu ngân bị chặn thanh toán đột ngột và mất giỏ hàng đang tạo.
- **Khắc phục:** Chỉ sinh mã hóa đơn ngay tại thời điểm bấm nút xác nhận thanh toán bên trong Transaction khóa dòng CSDL.

### DEF-MAJ-06: Mệnh đề SQL so sánh số ca trực nhân sự bị vô nghiệm khi xem báo cáo trong ngày
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Vị trí mã nguồn:** `src/dao/BaoCaoThongKeDAO.java`: Dòng 203, 217-218; `src/bus/ThongKeBus.java`: Dòng 61-63
- **Nguyên nhân & Hiện tượng:** `WHERE ngayLamViec >= CAST(? AS date) AND ngayLamViec < CAST(? AS date)`. Khi xem theo ngày, cả 2 mốc cùng ngày nên tập kết quả luôn rỗng, số ca trực của nhân viên luôn bằng 0.
- **Tác động:** Báo cáo hiệu suất nhân sự hằng ngày bị sai lệch hoàn toàn.
- **Khắc phục:** Điều chỉnh điều kiện thành: `ngayLamViec >= CAST(? AS date) AND ngayLamViec <= CAST(? AS date)`.

### DEF-MAJ-07: Doanh thu nhân sự bị tính trùng 2 lần giữa Thu ngân và Kỹ thuật viên Spa
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Vị trí mã nguồn:** `src/dao/BaoCaoThongKeDAO.java`: Dòng 204-214
- **Nguyên nhân & Hiện tượng:** Doanh thu = `hd.doanhThu` (của thu ngân lập phiếu, gồm cả dịch vụ) + `dv.doanhThuDV` (của KTV làm dịch vụ). Doanh thu dịch vụ bị tính 2 lần.
- **Tác động:** Tổng doanh thu nhân viên cộng lại vượt xa doanh thu thực tế của cửa hàng.
- **Khắc phục:** Tách bạch doanh thu bán hàng hóa thuần cho thu ngân và doanh thu dịch vụ thuần cho KTV.

### DEF-MAJ-08: Số liệu lợi nhuận trên Dashboard chế độ Chiến lược bị hardcode 42%
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Vị trí mã nguồn:** `src/gui/TongQuanPanel.java`: Dòng 363, 365
- **Nguyên nhân & Hiện tượng:** Khi chưa có giá vốn, hệ thống tự động gán `loiNhuan = Math.round(doanhThuHomNay * 0.42)`.
- **Tác động:** Số liệu lợi nhuận bị làm giả, vi phạm tính trung thực của phần mềm quản trị.
- **Khắc phục:** Tính đúng giá vốn từ `ChiTietXuatLo.soLuongXuatCoSo * LoHang.donGiaNhapCoSo`; nếu chưa có giá vốn thì hiển thị `0 đ` hoặc "Chưa có dữ liệu giá vốn".

### DEF-MAJ-09: Nhập sản phẩm và lô mới bị ép cứng mã Danh mục `DM01`, ĐVT `DVT01`, NCC `NCC01`
- **Phân hệ & Module:** Quản Lý Kho
- **Vị trí mã nguồn:** `src/gui/KhoLoHangPanel.java`: Dòng 427-440
- **Nguyên nhân & Hiện tượng:** Mọi sản phẩm mới tạo đều bị gán cứng vào danh mục Thức ăn (`DM01`), đơn vị Gói (`DVT01`), NCC (`NCC01`) bất kể người dùng chọn Cát vệ sinh hay Dụng cụ y tế.
- **Tác động:** Sai lệch danh mục sản phẩm và nhà cung cấp.
- **Khắc phục:** Đọc đúng mã danh mục, mã ĐVT và mã NCC đã chọn từ JComboBox.

### DEF-MAJ-10: Sinh mã ĐVT sai định dạng và nuốt ngoại lệ CSDL trong Thiết lập quy đổi
- **Phân hệ & Module:** Quản Lý Kho
- **Vị trí mã nguồn:** `src/gui/ThietLapQuyDoiDialog.java`: Dòng 365-376, 387
- **Nguyên nhân & Hiện tượng:** Sinh mã bằng `"DVT" + (size + 1)` không chuẩn; bắt lỗi CSDL nuốt im lặng và vẫn thông báo thành công cho người dùng dù CSDL không lưu được.
- **Tác động:** Lệch pha giữa bộ nhớ giao diện và CSDL SQL Server.
- **Khắc phục:** Sử dụng UDF sinh mã chuẩn hoặc UUID; hiển thị thông báo lỗi nếu chèn CSDL thất bại.

### DEF-MAJ-11: Toàn bộ hệ thống thiếu thực thể Phiếu Nhập Hàng (`PhieuNhap`) và Chi Tiết Phiếu Nhập
- **Phân hệ & Module:** Quản Lý Kho & CSDL
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`; `src/dao/LoHangDAO.java`: Dòng 115-129
- **Nguyên nhân & Hiện tượng:** Hệ thống chèn trực tiếp vào `LoHang`, không lưu chứng từ phiếu nhập, số hóa đơn đỏ NCC, người giao, người nhận.
- **Tác động:** Đứt gãy quy trình kiểm toán kế toán kho và công nợ nhà cung cấp.
- **Khắc phục:** Bổ sung bảng `PhieuNhap` và `ChiTietPhieuNhap` liên kết với `LoHang`.

### DEF-MAJ-12: Bỏ qua kiểm tra va chạm / trùng khung giờ khi đặt lịch hẹn KTV
- **Phân hệ & Module:** Spa & Dịch Vụ
- **Vị trí mã nguồn:** `src/gui/DatLichHenDialog.java`: Dòng 483; `src/bus/ThuCungBus.java`: Dòng 130
- **Nguyên nhân & Hiện tượng:** Nếu chọn "Chờ phân công ca sau", biến `maKtv` rỗng nên bỏ qua kiểm tra va chạm. Tầng BUS/DAO không kiểm tra trùng giờ trước khi INSERT.
- **Tác động:** Trùng giờ phục vụ của KTV hoặc của thú cưng, gây ùn tắc tại phòng spa.
- **Khắc phục:** Bổ sung kiểm tra trùng giờ ở cả tầng BUS và ràng buộc CSDL.

### DEF-MAJ-13: Đơn giá Dịch vụ Spa bị gán cứng 200.000 đ trong dialog đặt lịch hẹn
- **Phân hệ & Module:** Spa & Dịch Vụ
- **Vị trí mã nguồn:** `src/gui/DatLichHenDialog.java`: Dòng 634
- **Nguyên nhân & Hiện tượng:** Giá dịch vụ bị fix cứng `new BigDecimal("200000")` cho mọi dịch vụ dù giá thực tế là 300k, 100k, 90k.
- **Tác động:** Sai lệch số tiền dịch vụ khi tiếp nhận lịch hẹn.
- **Khắc phục:** Lấy đơn giá thực tế từ đối tượng `DichVu` trong CSDL.

### DEF-MAJ-14: Tra cứu khách hàng cũ bằng dữ liệu tĩnh (Mock Data 2 số điện thoại)
- **Phân hệ & Module:** Spa & Khách Hàng
- **Vị trí mã nguồn:** `src/gui/DatLichHenDialog.java`: Dòng 572-590
- **Nguyên nhân & Hiện tượng:** Code chỉ kiểm tra 2 SĐT cứng `0988111222` và `0977333444`, không gọi CSDL. Khách hàng thực tế bị báo chưa có dữ liệu.
- **Tác động:** Không tận dụng được dữ liệu khách hàng cũ khi đặt lịch.
- **Khắc phục:** Gọi `KhachHangDAO.findBySoDienThoai(sdt)`.

### DEF-MAJ-15: Đặt lịch hẹn luôn tạo mới thú cưng, gây nhân bản bản ghi trùng lặp
- **Phân hệ & Module:** Spa & Thú Cưng
- **Vị trí mã nguồn:** `src/gui/DatLichHenDialog.java`: Dòng 628
- **Nguyên nhân & Hiện tượng:** Mỗi lần đặt lịch cho bé cưng cũ, hệ thống lại gọi `sinhMaThuCungMoi` và INSERT thú cưng mới.
- **Tác động:** Rác CSDL, nhân bản hồ sơ thú cưng trùng tên.
- **Khắc phục:** Cho phép chọn thú cưng sẵn có của khách hàng thay vì luôn tạo mới.

### DEF-MAJ-16: Thanh toán POS chỉ trừ 1 buổi Spa Pass bất kể số lượng dịch vụ trong giỏ hàng
- **Phân hệ & Module:** Spa Pass & POS
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2958-2967
- **Nguyên nhân & Hiện tượng:** Vòng lặp gặp `DV_SPAPASS` đầu tiên là `break;` ngay. Khách đi 2 bé dùng thẻ nhưng chỉ trừ 1 buổi.
- **Tác động:** Thất thoát lượt dịch vụ của cửa hàng.
- **Khắc phục:** Trừ số buổi tương ứng với tổng số lượng dịch vụ spa sử dụng thẻ trong giỏ.

### DEF-MAJ-17: POS thanh toán xong không cập nhật trạng thái "Hoàn thành" cho Lịch hẹn
- **Phân hệ & Module:** Spa & POS
- **Vị trí mã nguồn:** `src/gui/BanHangPOSPanel.java`: Dòng 2540-2580
- **Nguyên nhân & Hiện tượng:** Thanh toán tiền xong nhưng không cập nhật `trangThaiLichHen = N'Đã hoàn thành'` trong CSDL.
- **Tác động:** Lịch hẹn trên bảng điều phối vẫn báo chưa xong dù khách đã thanh toán về.
- **Khắc phục:** Bổ sung câu lệnh UPDATE trạng thái lịch hẹn sau khi thanh toán hóa đơn.

### DEF-MAJ-18: Xung đột sinh mã phân công ca (`maPC = "PC" + (size + 1)`)
- **Phân hệ & Module:** Quản Lý Nhân Sự & Phân Ca
- **Vị trí mã nguồn:** `src/bus/PhanCaBus.java`: Dòng 245
- **Nguyên nhân & Hiện tượng:** Dựa vào `size() + 1` của RAM; nếu xóa ca cũ rồi thêm mới sẽ bị trùng mã phân công đã có trong CSDL.
- **Tác động:** Lỗi trùng khóa chính khi phân công lịch làm việc.
- **Khắc phục:** Xây dựng UDF `fn_SinhMaPhanCong` hoặc tự tăng trong CSDL.

### DEF-MAJ-19: Giao diện Quản lý Nhân sự không hiển thị thông báo lỗi khi thao tác thất bại
- **Phân hệ & Module:** Quản Lý Nhân Sự
- **Vị trí mã nguồn:** `src/gui/QuanLyNhanVienPanel.java`: Dòng 432, 456, 475, 494
- **Nguyên nhân & Hiện tượng:** Thao tác thêm/sửa/khóa nhân viên nếu trả về `false` thì nuốt im lặng, không có thông báo cho người dùng.
- **Tác động:** Người dùng lầm tưởng thao tác đã xong hoặc hệ thống bị đơ.
- **Khắc phục:** Bổ sung `JOptionPane.showMessageDialog` thông báo nguyên nhân thất bại.

### DEF-MAJ-20: Tầng BUS và DAO import trực tiếp Inner Class của tầng GUI
- **Phân hệ & Module:** Kiến Trúc Phần Mềm Toàn Cục
- **Vị trí mã nguồn:** `src/bus/PetHotelBus.java`: Dòng 4; `src/dao/PhongLuuTruDAO.java`: Dòng 4
- **Nguyên nhân & Hiện tượng:** Cả BUS và DAO đều `import gui.PetHotelPanel.PhongLuuTru;`.
- **Tác động:** Vi phạm kiến trúc phân tầng sạch, gây liên kết phụ thuộc ngược (Tight Coupling).
- **Khắc phục:** Chuyển `PhongLuuTru` thành entity độc lập trong gói `entity`.

### DEF-MAJ-21: Các hàm UDF sinh mã tự tăng crash khi gặp mã chứa ký tự chữ cái phía sau
- **Phân hệ & Module:** Cơ Sở Dữ Liệu
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`: Dòng 494, 510, 526, 542, 558
- **Nguyên nhân & Hiện tượng:** Dùng `CAST(SUBSTRING(...) AS INT)` với điều kiện `LIKE 'prefix[0-9]%'`. Nếu có mã test chứa chữ cái (ví dụ `KH001A`), hàm CAST sập runtime.
- **Tác động:** Crash các hàm sinh mã tự tăng của toàn bộ hệ thống CSDL.
- **Khắc phục:** Dùng `TRY_CAST` thay cho `CAST` để trả về NULL thay vì văng exception.

### DEF-MAJ-22: Hệ thống thiếu Trigger bảo toàn ràng buộc tồn kho & tích lũy điểm ở tầng CSDL
- **Phân hệ & Module:** Cơ Sở Dữ Liệu
- **Vị trí mã nguồn:** `sql/HeThongQuanLyCuaHangThuCung.sql`
- **Nguyên nhân & Hiện tượng:** Toàn bộ việc trừ kho và tích điểm phụ thuộc vào mã nguồn Java; CSDL không có trigger ràng buộc tự động.
- **Tác động:** Rủi ro sai lệch dữ liệu khi có truy cập đồng thời hoặc từ ứng dụng bên ngoài.
- **Khắc phục:** Bổ sung trigger kiểm tra tồn kho không âm và trigger bảo toàn điểm thưởng.

### DEF-MAJ-23: Không có Connection Pool, mở mới kết nối TCP mỗi truy vấn gây cạn kiệt tài nguyên
- **Phân hệ & Module:** Hạ Tầng & Kết Nối CSDL
- **Vị trí mã nguồn:** `src/utils/DBConnection.java`: Dòng 73-103
- **Nguyên nhân & Hiện tượng:** Mỗi lần gọi `getConnection()` lại mở mới 1 kết nối TCP vật lý qua `DriverManager`.
- **Tác động:** Hao tổn tài nguyên mạng, độ trễ cao, dễ bị nghẽn socket kết nối.
- **Khắc phục:** Tích hợp thư viện Connection Pool chuyên dụng (HikariCP / Apache DBCP).

---

## 5. DANH MỤC CHI TIẾT CÁC LỖI MỨC MINOR (7 LỖI)

---

### DEF-MIN-01: Bất nhất quy định độ dài mật khẩu tối thiểu giữa giao diện và tầng nghiệp vụ
- **Phân hệ & Module:** Bảo Mật & Đổi Mật Khẩu
- **Vị trí mã nguồn:** `src/gui/LoginForm.java`: 419 (>= 6 ký tự); `src/gui/MainDashboard.java`: 751 (>= 4 ký tự); `src/bus/DangNhapBus.java`: 153, 170 (>= 4 ký tự)
- **Mô tả & Tác động:** Tiêu chuẩn mật khẩu không đồng nhất; cho phép đặt mật khẩu yếu "1234" khi đổi mật khẩu qua Dashboard.
- **Khắc phục:** Thống nhất quy định tối thiểu 6 ký tự trên toàn bộ hệ thống.

### DEF-MIN-02: Không khóa tạm thời hóa đơn gốc khi chuyển luồng đổi trả sang POS
- **Phân hệ & Module:** Đổi Trả & POS
- **Vị trí mã nguồn:** `src/gui/DoiTraHangPanel.java`: Dòng 785-801
- **Mô tả & Tác động:** Chuyển dữ liệu sang POS nhưng chưa đánh dấu khóa hóa đơn gốc, quầy khác có thể mở hóa đơn đó ra đổi trả lần 2.
- **Khắc phục:** Cập nhật cờ tạm thời `dangXuLyDoiTra = 1` trên hóa đơn gốc.

### DEF-MIN-03: Mất mát giao dịch ở giây cuối cùng của ngày (`23:59:59.001` đến `.999`)
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Vị trí mã nguồn:** `src/bus/ThongKeBus.java`: 36, 49, 62; `src/dao/BaoCaoThongKeDAO.java`: 130, 176
- **Mô tả & Tác động:** Chuyển ngày thành `23:59:59` kết hợp `< ?` làm sót hóa đơn phát sinh trong các mili-giây cuối ngày.
- **Khắc phục:** Dùng `denNgay.plusDays(1).atStartOfDay()` kết hợp `< ?`.

### DEF-MIN-04: Dashboard `TongQuanPanel` lấy số liệu từ biến tĩnh RAM của POS thay vì CSDL
- **Phân hệ & Module:** Báo Cáo & Thống Kê
- **Vị trí mã nguồn:** `src/gui/TongQuanPanel.java`: Dòng 244-250, 373-375
- **Mô tả & Tác động:** Mở lại app trong ngày thì Dashboard hiển thị doanh thu 0 đ vì biến RAM bị reset.
- **Khắc phục:** Truy vấn trực tiếp `HoaDonDAO.layDoanhThuHomNay()`.

### DEF-MIN-05: `LoHangDAO.congTonKho` không chặn giá trị âm
- **Phân hệ & Module:** Quản Lý Kho
- **Vị trí mã nguồn:** `src/dao/LoHangDAO.java`: Dòng 181-188
- **Mô tả & Tác động:** Không kiểm tra `soLuongCong > 0`, nếu truyền số âm sẽ biến thành lệnh trừ kho bất thường.
- **Khắc phục:** Thêm kiểm tra `if (soLuongCong <= 0) return false;`.

### DEF-MIN-06: Gán cứng mã khách hàng fallback `"KH001"`
- **Phân hệ & Module:** Quản Lý Khách Hàng
- **Vị trí mã nguồn:** `src/bus/ThuCungBus.java`: Dòng 110, 151
- **Mô tả & Tác động:** Nếu mã KH001 bị xóa khỏi CSDL, thao tác thêm thú cưng sẽ văng lỗi khóa ngoại FK.
- **Khắc phục:** Yêu cầu chọn khách hàng hợp lệ hoặc bắt buộc nhập thông tin khách hàng mới.

### DEF-MIN-07: Nút xóa khách hàng không thông báo nguyên nhân khi vướng khóa ngoại FK
- **Phân hệ & Module:** Khách Hàng / UI
- **Vị trí mã nguồn:** `src/gui/ThuCungDichVuPanel.java`: Dòng 600-605
- **Mô tả & Tác động:** Xóa khách hàng đã có hóa đơn bị chặn nhưng không có thông báo giải thích rõ ràng.
- **Khắc phục:** Bắt `SQLException` mã lỗi foreign key và hiển thị thông báo "Không thể xóa khách hàng đã có lịch sử hóa đơn!".

---

## 6. MA TRẬN KỊCH BẢN SQL KIỂM CHỨNG TOÀN DIỆN (SQL VERIFICATION SUITE)

Dưới đây là tập hợp các câu lệnh T-SQL chuẩn hóa dùng để kiểm chứng toàn bộ các trạng thái dữ liệu trước và sau khi thực thi sửa lỗi:

```sql
USE QuanLyCuaHangThuCung;
GO

-- ====================================================================
-- 1. KIỂM TRA LỖ HỔNG BẢO MẬT DEF-CRI-01 (Tài khoản bị khóa)
-- ====================================================================
PRINT N'--- [1] KIỂM TRA SP_DANGNHAP VỚI TÀI KHOẢN BỊ KHÓA NV006 ---';
EXEC dbo.sp_DangNhap @identifier = '0956789012', @matKhauRaw = '123456';
-- KỲ VỌNG SAU KHI SỬA: Không trả về bản ghi nào (hoặc trả về mã lỗi tài khoản bị khóa).

-- ====================================================================
-- 2. KIỂM TRA LỖI TRÀN MÃ HÓA ĐƠN DEF-BLK-01 (Đơn thứ 100 trong ngày)
-- ====================================================================
PRINT N'--- [2] KIỂM TRA HÀM SINH MÃ HÓA ĐƠN KHI ĐẠT 99 ĐƠN ---';
DECLARE @prefix VARCHAR(10) = 'HD' + CONVERT(VARCHAR(6), GETDATE(), 12) + '-';
DECLARE @maxTest INT = 99;
SELECT @prefix + RIGHT('00' + CAST(@maxTest + 1 AS VARCHAR(10)), 2) AS [MaLoiCu_00],
       @prefix + RIGHT('0000' + CAST(@maxTest + 1 AS VARCHAR(10)), 4) AS [MaKyVongMoi_0100];

-- ====================================================================
-- 3. KIỂM TRA LỖI XUẤT BÁN HÀNG HẾT HẠN DEF-BLK-02
-- ====================================================================
PRINT N'--- [3] DANH SÁCH LÔ HÀNG ĐÃ HẾT HẠN CÒN TỒN KHO ---';
SELECT l.soLo, l.maSanPham, s.tenSanPham, l.hanSuDung, l.soLuongTon
FROM LoHang l
JOIN SanPham s ON l.maSanPham = s.maSanPham
WHERE l.hanSuDung < CAST(GETDATE() AS DATE) AND l.soLuongTon > 0;
-- KỲ VỌNG: POS không bao giờ được xuất các lô này.

-- ====================================================================
-- 4. KIỂM TRA TOÀN VẸN CHI TIẾT HÓA ĐƠN & XUẤT LÔ DEF-CRI-06
-- ====================================================================
PRINT N'--- [4] ĐỐI SOÁT ĐƠN GIÁ VÀ THÀNH TIỀN DÒNG HÀNG ---';
SELECT ct.maChiTiet, ct.maHoaDon, ct.soLuong, ct.donGiaLucBan, ct.thanhTien,
       xl.soLo, xl.soLuongXuatCoSo
FROM ChiTietHoaDon ct
LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet
WHERE ct.soLuong * ct.donGiaLucBan <> ct.thanhTien;
-- KỲ VỌNG: 0 bản ghi lệch phép nhân soLuong * donGia.

-- ====================================================================
-- 5. KIỂM TRA TỒN KHO ÂM HOẶC LỖI QUY ĐỔI ĐƠN VỊ
-- ====================================================================
PRINT N'--- [5] KIỂM TRA TỒN KHO ÂM VÀ TỶ LỆ QUY ĐỔI ---';
SELECT soLo, maSanPham, soLuongTon FROM LoHang WHERE soLuongTon < 0;
SELECT maQuyDoi, maSanPham, tyLeQuyDoi, giaBan FROM QuyDoiDonVi WHERE tyLeQuyDoi <= 0 OR giaBan < 0;
-- KỲ VỌNG: 0 bản ghi vi phạm.
```

---
*Tài liệu Danh mục lỗi kỹ thuật được tổng hợp độc lập bởi Đội ngũ Kiểm thử Phần mềm N07.*
