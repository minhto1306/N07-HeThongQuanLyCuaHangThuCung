# KẾ HOẠCH KIỂM THỬ CHI TIẾT: PHÂN HỆ KHÁCH HÀNG, THÚ CƯNG & DỊCH VỤ SPA / PET HOTEL
**Mã tài liệu:** `TC-STP-KH-TC-DV`  
**Dự án:** N07 - Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)  
**Tác nhân thực hiện:** Writer 3 (teamwork_test_writer)  
**Phạm vi:** Quản lý Khách hàng, Hồ sơ Thú cưng (Microchip), Sức khỏe thể trạng, Đặt lịch hẹn Spa & Grooming, Thẻ Hội viên Spa Pass, và Khách sạn Thú cưng (Pet Hotel).  
**Ngày ban hành:** 07/11/2026  
**Trạng thái:** Sẵn sàng kiểm thử (Test Ready)  

---

## I. TỔNG QUAN PHÂN HỆ & MA TRẬN TRUY XUẤT (TRACEABILITY MATRIX)

### 1.1 Phạm vi kiểm thử & Các thành phần liên quan
*   **Giao diện (GUI):**
    *   `ThuCungDichVuPanel.java`: Quản lý 5 tabs (Hồ sơ Thú cưng & Microchip, Hồ sơ Sức khỏe & Thể trạng, Lịch hẹn & Phân công KTV, Khách hàng Thành viên & Điểm VIP, Thẻ Hội viên & Gói Spa Pass).
    *   `PetHotelPanel.java`: Ma trận phòng lưu trú (Grid/Card), Check-in, Theo dõi dinh dưỡng, Check-out tính tiền và đẩy sang POS.
    *   `DatLichHenDialog.java`: Hộp thoại tiếp nhận đặt lịch hẹn Spa 2 cột (Balanced 2-Column), Time Slot Badges, Date Picker, tính Size thú cưng, chọn KTV.
    *   `ChonDichVuDialog.java`: Hộp thoại chọn nhanh dịch vụ chăm sóc thú cưng bổ sung tại quầy.
*   **Nghiệp vụ (BUS):**
    *   `bus.ThuCungBus.java`: Điều phối thú cưng, khách hàng, hồ sơ sức khỏe, lịch hẹn và phân công KTV.
    *   `bus.SpaPassBus.java`: Điều phối thẻ Spa Pass, tra cứu thẻ khả dụng, quẹt trừ buổi, kích hoạt thẻ mới.
    *   `bus.PetHotelBus.java`: Điều phối phòng lưu trú, cập nhật trạng thái phòng, lưu lịch sử check-out.
*   **Truy cập dữ liệu (DAO):**
    *   `dao.ThuCungDAO.java`, `dao.LichHenDAO.java`, `dao.TheSpaPassDAO.java`, `dao.PhongLuuTruDAO.java`, `dao.DichVuDAO.java`, `dao.KhachHangDAO.java`.
*   **Cơ sở dữ liệu (SQL Server):**
    *   Bảng: `KhachHang`, `ThuCung`, `HoSoThuCung`, `HoSoSucKhoe`, `DichVu`, `LichHen`, `ChiTietLichHen`, `TheSpaPass`, `PhongLuuTru`, `LichSuLuuTru`.
    *   Hàm UDF: `dbo.fn_SinhMaThuCung()`, `dbo.fn_SinhMaKhachHang()`, `dbo.fn_SinhMaLichHen()`.

### 1.2 Ma trận Kiểm thử Phân hệ (Test Traceability Matrix)

| Nhóm Chức Năng | Mã Test Case | Cấp Độ | Màn Hình / Lớp Mục Tiêu | Mục Tiêu Chính & Khiếm Khuyết Liên Quan |
| :--- | :--- | :--- | :--- | :--- |
| **Quản lý Thú cưng & Khách hàng** | `TC-PET-001` | Integration | `ThuCungBus` -> `ThuCungDAO` | Thêm hồ sơ thú cưng mới kèm mã Microchip và tạo tự động Khách hàng (Happy Path) |
| | `TC-PET-002` | Unit/DAO | `ThuCungDAO.insertThuCung` | Kiểm tra rò rỉ kết nối JDBC khi thêm thú cưng (Phát hiện & Chặn **DEF-02 / DEF-BLK-04**) |
| | `TC-PET-003` | UI/Manual | `ThuCungDichVuPanel` (Tab 1) | Tìm kiếm & lọc hồ sơ thú cưng theo từ khóa (Tên bé, SĐT chủ, mã Microchip) |
| | `TC-PET-004` | Integration | `ThuCungDAO.updateThuCung` | Cập nhật thông tin nhận dạng thú cưng (Tên, giống loài, ngày sinh, giới tính) |
| | `TC-PET-005` | Unit/DAO | `ThuCungDAO.deleteThuCung` | Xóa thú cưng và kiểm tra rò rỉ kết nối JDBC trong khối finally (**DEF-02 / DEF-BLK-04**) |
| | `TC-PET-006` | Integration | `ThuCungDAO.deleteThuCung` | Xóa thú cưng đang vướng ràng buộc khóa ngoại FK trong `LichHen` / `LichSuLuuTru` |
| | `TC-PET-007` | Integration | `ThuCungBus` -> `ThuCungDAO` | Ghi nhận sổ theo dõi thể trạng sức khỏe (Cân nặng, nhiệt độ, tiêm phòng dại) |
| | `TC-PET-008` | Unit/Logic | `ThuCungBus.themHoSoSucKhoe` | Kiểm tra cơ chế gán cứng mã khách hàng fallback `"KH001"` (**DEF-25 / DEF-MIN-06**) |
| **Lịch hẹn Spa & Grooming** | `TC-SPA-001` | UI/Integration| `DatLichHenDialog` -> `ThuCungBus` | Đặt lịch hẹn Spa thành công với đầy đủ thông tin Khách, Bé, KTV, Khung giờ |
| | `TC-SPA-002` | UI/Logic | `DatLichHenDialog.capNhatSizeBadge`| Tự động phân loại Size thú cưng theo cân nặng (<5kg: Nhỏ, 5-10kg: Vừa, >10kg: Lớn) |
| | `TC-SPA-003` | UI/Integration| `DatLichHenDialog.capNhatSlotAvailability` | Kiểm tra phát hiện xung đột / trùng lịch KTV trong cùng khung giờ (Khóa nút slot) |
| | `TC-SPA-004` | UI/Edge | `DatLichHenDialog.capNhatSlotAvailability` | Bỏ qua kiểm tra va chạm khi chọn "Chờ phân công ca trực sau" (**DEF-13 / DEF-MAJ-12**) |
| | `TC-SPA-005` | Unit/UDF | `dbo.fn_SinhMaLichHen` | Kiểm tra lỗi runtime ép kiểu chuỗi sang INT khi sang ngày mới (**DEF-05 / DEF-CRI-03**) |
| | `TC-SPA-006` | UI/Bug Check | `DatLichHenDialog.traCuuKhachHangNhanh` | Kiểm tra tra cứu khách hàng cũ bằng Mock Data tĩnh 2 số điện thoại (**DEF-15 / DEF-MAJ-14**) |
| | `TC-SPA-007` | UI/Bug Check | `DatLichHenDialog.xuLyLuuLichHen` | Kiểm tra đơn giá dịch vụ Spa bị fix cứng 200.000 đ (**DEF-14 / DEF-MAJ-13**) |
| | `TC-SPA-008` | Integration | `DatLichHenDialog.xuLyLuuLichHen` | Đặt lịch hẹn luôn tạo mới thú cưng gây nhân bản bản ghi (**DEF-16 / DEF-MAJ-15**) |
| | `TC-SPA-009` | Integration | `ThuCungBus.capNhatTrangThaiLichHen`| Điều phối chuyển trạng thái lịch hẹn (Chờ tiếp nhận -> Đang thực hiện -> Hoàn thành) |
| | `TC-SPA-010` | Unit/DAO | `LichHenDAO.deleteLichHen` | Xóa lịch hẹn và kiểm tra rò rỉ kết nối JDBC thiếu `conn.close()` (**DEF-BLK-07**) |
| | `TC-SPA-011` | Concurrency | `DatLichHenDialog` -> `ThuCungBus` | Kiểm thử tranh chấp đồng thời (Race Condition) khi 2 thu ngân cùng đặt lịch cho 1 KTV tại cùng 1 khung giờ (**DEF-MAJ-12**) |
| **Thẻ Hội viên & Spa Pass** | `TC-PASS-001` | Integration | `SpaPassBus.kichHoatTheMoi` | Kích hoạt Thẻ Spa Pass mới (5 buổi / 10 buổi) khi khách mua tại POS |
| | `TC-PASS-002` | Unit/Logic | `TheSpaPass.dungMotBuoi` | Quẹt thẻ trừ 1 buổi khi thú cưng làm dịch vụ (Số buổi còn lại giảm 1) |
| | `TC-PASS-003` | Integration | `SpaPassBus.suDungMotBuoi` | Kiểm tra bất đồng bộ RAM vs DB khi DB update thất bại mà RAM vẫn trừ buổi (**DEF-10 / DEF-CRI-09**) |
| | `TC-PASS-004` | Unit/Edge | `TheSpaPass.coTheSuDung` | Chặn sử dụng khi số buổi còn lại = 0 (Trạng thái chuyển `"ĐÃ HẾT BUỔI"`) |
| | `TC-PASS-005` | Unit/Edge | `TheSpaPass.coTheSuDung` | Chặn sử dụng khi thẻ đã quá hạn sử dụng (Trạng thái chuyển `"HẾT HẠN SỬ DỤNG"`) |
| | `TC-PASS-006` | Integration | `SpaPassBus.napThemBuoiChoThe` | Nạp thêm số buổi và gia hạn thêm thời gian sử dụng cho Thẻ Spa Pass cũ |
| | `TC-PASS-007` | Integration | `BanHangPOSPanel` (Spa Pass Flow) | Kiểm tra lỗ hổng POS chỉ trừ duy nhất 1 buổi dù giỏ hàng có nhiều lượt spa (**DEF-17 / DEF-MAJ-16**) |
| | `TC-PASS-008` | UI/Manual | `ThuCungDichVuPanel` (Tab 5) | Đối soát danh sách Thẻ Hội viên, lọc theo trạng thái và tìm kiếm theo SĐT |
| **Khách sạn Thú cưng Pet Hotel** | `TC-HTL-001` | UI/Integration| `PetHotelPanel.moDialogCheckIn` | Check-in lưu trú thú cưng (Chọn phòng trống, nhập SĐT, số ngày dự kiến) |
| | `TC-HTL-002` | Integration | `PetHotelBus.capNhatPhong` | Kiểm tra tính nguyên tử (Atomicity) khi đổi trạng thái phòng TRỐNG -> ĐANG CÓ KHÁCH |
| | `TC-HTL-003` | UI/Integration| `PetHotelPanel.thucHienCheckOut`| Check-out đúng hạn: Tính tiền phòng chuẩn xác theo đơn giá ngày và đẩy sang POS |
| | `TC-HTL-004` | UI/Bug Check | `PetHotelPanel.thucHienCheckOut`| Bỏ qua phụ phí trả trễ khi khách đón thú cưng trễ so với dự kiến (**DEF-08 / DEF-CRI-07**) |
| | `TC-HTL-005` | Integration | `PetHotelBus.luuLichSuLuuTru` | Thiếu Transaction phân tán giữa ghi `LichSuLuuTru` và cập nhật `PhongLuuTru` (**DEF-09 / DEF-CRI-08**) |
| | `TC-HTL-006` | UI/Manual | `PetHotelPanel` (Care Card) | Đánh dấu nhật ký chăm sóc (Ăn sáng, ăn tối, đi dạo) và cộng dồn phụ phí phát sinh |
| | `TC-HTL-007` | Architecture| `bus.PetHotelBus` / `dao.PhongLuuTruDAO`| Kiểm tra vi phạm Clean Architecture: BUS và DAO import trực tiếp Inner Class của GUI (**DEF-21 / DEF-MAJ-20**) |
| | `TC-HTL-008` | Unit/DAO | `PhongLuuTruDAO.getAllPhong` | Kiểm tra nạp danh sách phòng lưu trú và ánh xạ đúng các trường trạng thái |

---

## II. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (TEST CASES SPECIFICATION)

---

### PHẦN 1: QUẢN LÝ THÚ CƯNG, KHÁCH HÀNG & SỔ THEO DÕI SỨC KHỎE

#### TC-PET-001: Thêm hồ sơ thú cưng mới kèm tạo mã Microchip và tạo tự động Khách hàng
*   **Phân hệ & Màn hình:** Quản lý Thú cưng / `ThuCungDichVuPanel` (Tab 1), `ThuCungBus.themThuCungTheoSdtChu`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra khả năng tiếp nhận thú cưng mới của một khách hàng chưa từng có trong hệ thống; hệ thống phải tự động sinh mã khách hàng mới, sinh mã thú cưng mới, gắn mã Microchip định danh chuẩn `MC-982...-VN`, và lưu vào CSDL qua Transaction an toàn.
*   **Điều kiện tiên quyết:**
    *   Hệ thống đang kết nối SQL Server bình thường.
    *   Số điện thoại khách hàng `0911223344` chưa tồn tại trong bảng `KhachHang`.
*   **Các bước thực hiện:**
    1.  Mở màn hình `ThuCungDichVuPanel`, chọn Tab 1: "Hồ sơ Thú cưng & Microchip".
    2.  Bấm nút "+ Tiếp nhận bé cưng mới".
    3.  Nhập thông tin:
        *   Tên chủ nuôi: `Hoàng Văn Nam`
        *   SĐT chủ nuôi: `0911223344`
        *   Tên bé cưng: `Bơ Vàng`
        *   Giống loài: `Chó Golden Retriever`
        *   Ngày sinh: `15/05/2023`
        *   Giới tính: `Đực`
        *   Số Microchip: (Để trống để hệ thống tự sinh mã chuẩn)
    4.  Bấm "Lưu hồ sơ bé".
*   **Dữ liệu kiểm thử:**
    *   `tenChu = "Hoàng Văn Nam"`, `sdtChu = "0911223344"`.
    *   `tenThuCung = "Bơ Vàng"`, `giongLoai = "Chó Golden Retriever"`, `ngaySinh = 2023-05-15`, `gioiTinh = "Đực"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại hiển thị thông báo "Tiếp nhận hồ sơ bé cưng thành công!".
    *   Bảng `KhachHang` xuất hiện 1 bản ghi mới với SĐT `0911223344` và tên `Hoàng Văn Nam`.
    *   Bảng `ThuCung` xuất hiện bản ghi tên `Bơ Vàng`.
    *   Bảng `HoSoThuCung` xuất hiện bản ghi liên kết giữa `maKhachHang` và `maThuCung` vừa tạo, mã Microchip có định dạng `MC-982...-VN`, trạng thái nuôi là `Đang nuôi`.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Cả 3 bảng `KhachHang`, `ThuCung`, `HoSoThuCung` đều được ghi nhận đồng bộ dữ liệu.
    *   *Không đạt:* Bị văng lỗi `NullPointerException`, thiếu một trong các bản ghi, hoặc mã Microchip bị NULL.
*   **Verification SQL Queries:**
```sql
-- Kiểm tra trước khi thực thi:
SELECT COUNT(*) AS TonTaiKH FROM KhachHang WHERE soDienThoai = '0911223344'; -- Kỳ vọng: 0

-- Kiểm tra sau khi thực thi:
SELECT kh.maKhachHang, kh.hoTen, kh.soDienThoai, tc.maThuCung, tc.tenThuCung, tc.giongLoai, hs.soMicrochip, hs.trangThaiNuoi
FROM KhachHang kh
JOIN HoSoThuCung hs ON kh.maKhachHang = hs.maKhachHang
JOIN ThuCung tc ON hs.maThuCung = tc.maThuCung
WHERE kh.soDienThoai = '0911223344';
-- Kỳ vọng: Trả về đúng 1 dòng dữ liệu với đầy đủ quan hệ Khách Hàng - Thú Cưng - Microchip.
```

---

#### TC-PET-002: Kiểm tra rò rỉ kết nối JDBC khi thêm thú cưng (Defect DEF-02 / DEF-BLK-04 Verification)
*   **Phân hệ & Màn hình:** Tầng DAO / `dao.ThuCungDAO.insertThuCung`.
*   **Cấp độ kiểm thử:** Unit / Integration Test (Database Session Audit).
*   **Mô tả mục tiêu:** Kiểm tra và xác minh khiếm khuyết **DEF-02 / DEF-BLK-04**: trong phương thức `ThuCungDAO.insertThuCung(ThuCung, String, String)`, khối `finally` chỉ gọi `conn.setAutoCommit(true)` mà hoàn toàn thiếu `conn.close()`, dẫn đến việc để hở kết nối vật lý trên SQL Server.
*   **Điều kiện tiên quyết:**
    *   Quyền truy cập vào bảng hệ thống `sys.dm_exec_sessions` của SQL Server Master.
*   **Các bước thực hiện:**
    1.  Chạy câu lệnh SQL kiểm tra số lượng phiên kết nối JDBC hiện hữu từ ứng dụng.
    2.  Gọi phương thức `ThuCungDAO.insertThuCung` liên tục 15 lần thông qua vòng lặp kiểm thử tự động (hoặc thao tác trên GUI).
    3.  Chạy lại câu lệnh SQL đếm số lượng phiên `ESTABLISHED` trên SQL Server.
*   **Dữ liệu kiểm thử:**
    *   15 đối tượng `ThuCung` giả lập (`TC_LEAK_01` -> `TC_LEAK_15`) gắn với khách hàng `KH001`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng chuẩn phần mềm:* Sau khi thực hiện xong, toàn bộ kết nối JDBC phải được đóng trả về hồ bơi (`conn.close()`), số phiên kết nối không được tăng đột biến không kiểm soát.
    *   *Thực tế hiện tại (Bug Trigger):* Số lượng phiên `session_id` tăng thêm 15 phiên và giữ nguyên trạng thái mở trên SQL Server -> **Xác nhận phát hiện lỗi DEF-02 / DEF-BLK-04 (Blocker)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Test PASS nếu tái hiện thành công bug):* Phát hiện số phiên kết nối không giảm về 0 sau khi hoàn thành.
*   **Verification SQL Queries:**
```sql
-- Đếm số phiên kết nối JDBC đang treo mở trước và sau khi thêm thú cưng:
SELECT session_id, login_time, host_name, program_name, status, cpu_time, memory_usage
FROM sys.dm_exec_sessions
WHERE program_name LIKE '%JDBC%' OR client_interface_name LIKE '%JDBC%';
```

---

#### TC-PET-003: Tìm kiếm & Lọc hồ sơ thú cưng theo từ khóa đa năng
*   **Phân hệ & Màn hình:** Quản lý Thú cưng / `ThuCungDichVuPanel` (Tab 1: `txtSearch`, `tblHoSo`).
*   **Cấp độ kiểm thử:** UI / Manual Functional Test.
*   **Mô tả mục tiêu:** Đảm bảo thanh tìm kiếm lọc theo thời gian thực (Real-time DocumentListener) lọc chính xác theo: Tên thú cưng, Tên chủ nuôi, SĐT chủ nuôi, và Mã số Microchip.
*   **Điều kiện tiên quyết:**
    *   Bảng `HoSoThuCung` đã nạp các bản ghi mẫu:
        *   Bé `Miu Miu`, chủ `Nguyễn Văn An`, SĐT `0988111222`, Microchip `MC-982001-VN`.
        *   Bé `Lu Lu`, chủ `Trần Thị Mai`, SĐT `0977333444`, Microchip `MC-982002-VN`.
*   **Các bước thực hiện:**
    1.  Nhập `"Miu Miu"` vào ô `txtSearch` -> Quan sát bảng.
    2.  Xóa ô tìm kiếm, nhập `"0977333444"` -> Quan sát bảng.
    3.  Xóa ô tìm kiếm, nhập `"MC-982001"` -> Quan sát bảng.
    4.  Nhập chuỗi không tồn tại `"XYZ_NOT_FOUND_999"` -> Quan sát bảng.
*   **Dữ liệu kiểm thử:**
    *   Tên: `"Miu Miu"`, SĐT: `"0977333444"`, Microchip: `"MC-982001"`, Chuỗi lạ: `"XYZ_NOT_FOUND_999"`.
*   **Kết quả mong đợi:**
    *   Bước 1: Bảng chỉ hiển thị dòng chứa bé `Miu Miu`.
    *   Bước 2: Bảng chỉ hiển thị dòng chứa bé `Lu Lu` (chủ `Trần Thị Mai`).
    *   Bước 3: Bảng chỉ hiển thị dòng chứa Microchip `MC-982001-VN`.
    *   Bước 4: Bảng hiển thị 0 dòng (trống), không văng Exception.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Lọc tức thì, khớp đúng 100% dữ liệu, không giật lag.
*   **Verification SQL Queries:**
```sql
SELECT hs.maHoSo, tc.tenThuCung, kh.hoTen, kh.soDienThoai, hs.soMicrochip
FROM HoSoThuCung hs
JOIN ThuCung tc ON hs.maThuCung = tc.maThuCung
JOIN KhachHang kh ON hs.maKhachHang = kh.maKhachHang
WHERE tc.tenThuCung LIKE N'%Miu Miu%' OR kh.soDienThoai LIKE '%0977333444%' OR hs.soMicrochip LIKE '%MC-982001%';
```

---

#### TC-PET-004: Cập nhật thông tin nhận dạng thú cưng
*   **Phân hệ & Màn hình:** Quản lý Thú cưng / `ThuCungDAO.updateThuCung`.
*   **Cấp độ kiểm thử:** Integration Test (DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chức năng cập nhật thông tin thú cưng (đổi tên, giống loài, giới tính, ngày sinh) đã lưu bền vững xuống CSDL.
*   **Điều kiện tiên quyết:**
    *   Đã có bản ghi thú cưng `TC001` (Tên cũ: `Miu Miu`, Giống: `Mèo Anh Lông Ngắn`).
*   **Các bước thực hiện:**
    1.  Khởi tạo đối tượng `ThuCung` với mã `TC001`.
    2.  Thay đổi thông tin:
        *   Tên mới: `Miu Miu Xám Khói`
        *   Giống mới: `Mèo Anh Lông Ngắn Classic`
        *   Ngày sinh: `2022-01-10`
        *   Giới tính: `Cái`
    3.  Gọi phương thức `thuCungDAO.updateThuCung(tc)`.
*   **Dữ liệu kiểm thử:**
    *   `maThuCung = "TC001"`, `tenThuCung = "Miu Miu Xám Khói"`, `giongLoai = "Mèo Anh Lông Ngắn Classic"`, `gioiTinh = "Cái"`.
*   **Kết quả mong đợi:**
    *   Phương thức trả về `true`.
    *   Truy vấn SQL bảng `ThuCung` với `maThuCung = 'TC001'` phản ánh chính xác các thông tin mới.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Giá trị trong CSDL được cập nhật chuẩn xác.
*   **Verification SQL Queries:**
```sql
-- Kiểm tra dữ liệu sau cập nhật:
SELECT maThuCung, tenThuCung, giongLoai, ngaySinh, gioiTinh
FROM ThuCung
WHERE maThuCung = 'TC001';
-- Kỳ vọng: tenThuCung = N'Miu Miu Xám Khói', gioiTinh = N'Cái'.
```

---

#### TC-PET-005: Xóa thú cưng và kiểm tra rò rỉ kết nối JDBC trong khối finally (DEF-02 / DEF-BLK-04 Verification)
*   **Phân hệ & Màn hình:** Tầng DAO / `dao.ThuCungDAO.deleteThuCung`.
*   **Cấp độ kiểm thử:** Unit / Database Leak Test.
*   **Mô tả mục tiêu:** Xác minh lỗi rò rỉ kết nối trong phương thức `deleteThuCung`:
    ```java
    finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
    ```
    Khối `finally` thiếu hoàn toàn lệnh `conn.close()`.
*   **Điều kiện tiên quyết:**
    *   Tạo bản ghi thú cưng tạm thời `TC_TEMP_DEL` kèm hồ sơ nuôi và sức khỏe.
*   **Các bước thực hiện:**
    1.  Chèn bản ghi `TC_TEMP_DEL`.
    2.  Thực hiện lệnh xóa `thuCungDAO.deleteThuCung("TC_TEMP_DEL")`.
    3.  Kiểm tra trạng thái kết nối trên SQL Server.
*   **Dữ liệu kiểm thử:** `maThuCung = "TC_TEMP_DEL"`.
*   **Kết quả mong đợi:**
    *   Bản ghi bị xóa thành công khỏi `ThuCung`, `HoSoThuCung`, `HoSoSucKhoe`.
    *   *Lỗ hổng kỹ thuật:* Kết nối TCP vẫn duy trì trạng thái `ESTABLISHED` do không được giải phóng -> Xác nhận **DEF-02 / DEF-BLK-04**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Xác nhận khiếm khuyết):* Phát hiện thiếu `conn.close()`.
*   **Verification SQL Queries:**
```sql
SELECT session_id, status, command FROM sys.dm_exec_requests WHERE command LIKE '%DELETE%';
```

---

#### TC-PET-006: Xóa thú cưng đang vướng ràng buộc khóa ngoại FK trong Lịch hẹn / Lưu trú
*   **Phân hệ & Màn hình:** Quản lý Thú cưng / `ThuCungDAO.deleteThuCung`, `ThuCungDichVuPanel`.
*   **Cấp độ kiểm thử:** Integration / Negative Test.
*   **Mô tả mục tiêu:** Kiểm tra xử lý ràng buộc toàn vẹn cơ sở dữ liệu khi người dùng cố gắng xóa một thú cưng đã có lịch hẹn phục vụ trong `ChiTietLichHen` hoặc có lịch sử lưu trú trong `LichSuLuuTru`.
*   **Điều kiện tiên quyết:**
    *   Thú cưng `TC001` đang có bản ghi liên kết trong bảng `ChiTietLichHen` (`FK_CTLichHen_ThuCung`).
*   **Các bước thực hiện:**
    1.  Trên giao diện `ThuCungDichVuPanel`, chọn bé cưng `TC001`.
    2.  Bấm nút "Xóa hồ sơ bé cưng".
    3.  Xác nhận "Đồng ý xóa" trên hộp thoại cảnh báo.
*   **Dữ liệu kiểm thử:** `maThuCung = "TC001"`.
*   **Kết quả mong đợi:**
    *   Hệ thống SQL Server kích hoạt lỗi vi phạm ràng buộc khóa ngoại `The DELETE statement conflicted with the REFERENCE constraint "FK_CTLichHen_ThuCung"`.
    *   Giao diện ứng dụng phải bắt lỗi an toàn, hiển thị thông báo: "Không thể xóa bé cưng này vì đang có lịch hẹn hoặc lịch sử dịch vụ trong hệ thống!", không làm crash ứng dụng.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Giao diện bắt Exception và thông báo rõ ràng cho người dùng; dữ liệu không bị xóa dở dang.
    *   *Không đạt:* Chương trình bị văng Exception `SQLException` ra màn hình hoặc đơ không phản hồi (**DEF-26 / DEF-MIN-07**).
*   **Verification SQL Queries:**
```sql
SELECT COUNT(*) FROM ChiTietLichHen WHERE maThuCung = 'TC001'; -- Kỳ vọng > 0 (Khóa ngoại ngăn chặn DELETE)
```

---

#### TC-PET-007: Ghi sổ theo dõi thể trạng sức khỏe định kỳ (Cân nặng, Nhiệt độ, Tiêm chủng)
*   **Phân hệ & Màn hình:** Quản lý Thú cưng / Tab 2: "Hồ sơ Sức khỏe & Thể trạng", `ThuCungBus.themHoSoSucKhoe`.
*   **Cấp độ kiểm thử:** Integration Test (UI -> BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra chức năng ghi nhận lịch sử khám và theo dõi thể trạng: cân nặng (kg), thân nhiệt (°C), tình trạng ve rận, trạng thái tiêm phòng dại và ghi chú bác sĩ/KTV.
*   **Điều kiện tiên quyết:**
    *   Đã có thú cưng `TC001` (`Miu Miu`) trong CSDL.
    *   Nhân viên kiểm tra `NV003` (`Tô Thanh Đạt`).
*   **Các bước thực hiện:**
    1.  Chọn Tab 2: "Hồ sơ Sức khỏe & Thể trạng".
    2.  Bấm "+ Thêm phiếu khám / kiểm tra".
    3.  Nhập thông tin thể trạng:
        *   Bé cưng: `TC001 - Miu Miu`
        *   Cân nặng: `4.5` (kg)
        *   Nhiệt độ: `38.5` (°C)
        *   Tình trạng ve rận: `Không có`
        *   Tiêm phòng dại: `Đã tiêm` (Tích chọn)
        *   Ghi chú: `Thể trạng tốt, lông mượt, tai sạch`
    4.  Bấm "Lưu kết quả khám".
*   **Dữ liệu kiểm thử:**
    *   `maThuCung = "TC001"`, `canNang = 4.5`, `nhietDo = 38.5`, `tinhTrangVeRan = "Không có"`, `tiemPhongDai = 1`.
*   **Kết quả mong đợi:**
    *   Lưu thành công vào bảng `HoSoSucKhoe`.
    *   Bảng hiển thị trên giao diện nạp thêm dòng mới với đầy đủ các chỉ số.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Dữ liệu lưu đúng kiểu dữ liệu `DECIMAL(6,2)` và `DECIMAL(4,2)` trong SQL Server.
*   **Verification SQL Queries:**
```sql
SELECT TOP 1 maSucKhoe, maThuCung, canNang, nhietDo, tinhTrangVeRan, tiemPhongDai, ngayKiemTra
FROM HoSoSucKhoe
WHERE maThuCung = 'TC001'
ORDER BY ngayKiemTra DESC;
-- Kỳ vọng: canNang = 4.50, nhietDo = 38.50, tiemPhongDai = 1.
```

---

#### TC-PET-008: Kiểm tra cơ chế gán cứng mã khách hàng fallback "KH001" (DEF-25 / DEF-MIN-06 Verification)
*   **Phân hệ & Màn hình:** Tầng Nghiệp vụ / `bus.ThuCungBus.themHoSoSucKhoe`.
*   **Cấp độ kiểm thử:** Unit / Logic Audit.
*   **Mô tả mục tiêu:** Kiểm tra đoạn code dòng 108-110 trong `ThuCungBus.java`:
    ```java
    if (sk.getThuCung() != null && thuCungDAO.getThuCungById(sk.getThuCung().getMaThuCung()) == null) {
        thuCungDAO.insertThuCung(sk.getThuCung(), "KH001", null);
    }
    ```
    Nếu bé cưng chưa có trong CSDL, hệ thống tự động gán cứng mã khách hàng là `"KH001"`. Nếu mã `"KH001"` bị xóa khỏi hệ thống, thao tác sẽ phát sinh lỗi vi phạm khóa ngoại FK.
*   **Điều kiện tiên quyết:**
    *   Giả lập xóa tạm thời hoặc đổi mã khách hàng `KH001` trong môi trường staging.
*   **Các bước thực hiện:**
    1.  Khởi tạo `HoSoSucKhoe` cho một bé cưng mới toanh `TC_NEW_999`.
    2.  Gọi `thuCungBus.themHoSoSucKhoe(sk)`.
*   **Dữ liệu kiểm thử:** `maThuCung = "TC_NEW_999"`, `maKhachHang = "KH001"`.
*   **Kết quả mong đợi:**
    *   Nếu `KH001` không tồn tại, lệnh `insertThuCung` văng `SQLException: FK_HoSo_KhachHang` -> Xác nhận **DEF-25 / DEF-MIN-06 (Minor)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Phát hiện sự phụ thuộc cứng vào mã `KH001`.
*   **Verification SQL Queries:**
```sql
SELECT COUNT(*) FROM KhachHang WHERE maKhachHang = 'KH001';
```

---

### PHẦN 2: ĐẶT LỊCH HẸN SPA, GROOMING & PHÂN CÔNG KỸ THUẬT VIÊN

#### TC-SPA-001: Tiếp nhận đặt lịch hẹn Spa & Grooming thành công (Happy Path)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog`, `ThuCungBus.datLichHenMoi`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra toàn bộ quy trình đặt lịch hẹn dịch vụ chăm sóc: chọn ngày, chọn khung giờ (Time slot badge), nhập khách hàng, tên bé cưng, chọn KTV phụ trách và dịch vụ yêu cầu.
*   **Điều kiện tiên quyết:**
    *   KTV `NV004` (`Võ Thị Hồng Ngọc`) còn trống lịch khung giờ `09:30` ngày mai.
    *   Dịch vụ `DV001` (Tắm Spa Bồn Sục Ozone) có trong CSDL.
*   **Các bước thực hiện:**
    1.  Trên thanh điều khiển, mở hộp thoại Đặt Lịch Hẹn Spa (`DatLichHenDialog`).
    2.  Nhập thông tin khách: Họ tên `Lê Minh Khôi`, SĐT `0966778899`.
    3.  Nhập thông tin bé: Tên `Bông Xù`, Giống `Chó Poodle`, Cân nặng `4.2` kg, Giới tính `Cái`.
    4.  Cột phải:
        *   Chọn ngày: Ngày mai (`LocalDate.now().plusDays(1)`).
        *   Khung giờ: Nhấp chọn badge `09:30`.
        *   Dịch vụ: Chọn `DV001 - Tắm Spa Bồn Sục Ozone (45 phút)`.
        *   KTV: Chọn `NV004 - Võ Thị Hồng Ngọc (Kỹ thuật viên)`.
        *   Dịch vụ cộng thêm: Tích chọn `+ Cạo vôi răng (+50k)`.
    5.  Bấm "Xác nhận Đặt lịch".
*   **Dữ liệu kiểm thử:**
    *   `hoTen = "Lê Minh Khôi"`, `sdt = "0966778899"`, `tenBe = "Bông Xù"`, `canNang = 4.2`.
    *   `ngayHen = tomorrow 09:30`, `maDv = "DV001"`, `maKtv = "NV004"`, `phuPhi = "+ Cạo vôi răng (+50k)"`.
*   **Kết quả mong đợi:**
    *   Hộp thoại hiển thị thông báo "TIẾP NHẬN ĐẶT LỊCH HẸN THÀNH CÔNG!".
    *   Trong bảng `LichHen` xuất hiện bản ghi mới có trạng thái `"Đã đặt"`.
    *   Trong bảng `ChiTietLichHen` có liên kết dịch vụ `DV001`, bé `Bông Xù`, KTV `NV004`.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Bản ghi lịch hẹn được tạo hoàn chỉnh trong CSDL với mã lịch hẹn chuẩn.
*   **Verification SQL Queries:**
```sql
SELECT lh.maLichHen, kh.hoTen, kh.soDienThoai, lh.ngayHen, lh.trangThaiLichHen, ctlh.maDichVu, ctlh.maKtvChiDinh, lh.ghiChu
FROM LichHen lh
JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang
JOIN ChiTietLichHen ctlh ON lh.maLichHen = ctlh.maLichHen
WHERE kh.soDienThoai = '0966778899';
-- Kỳ vọng: trangThaiLichHen = N'Đã đặt', maKtvChiDinh = 'NV004', ghiChu chứa 'Cạo vôi răng'.
```

---

#### TC-SPA-002: Tự động phân loại Size thú cưng theo cân nặng (Size Badges)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.capNhatSizeBadge`, `spnCanNang`, `lblSizeBadge`.
*   **Cấp độ kiểm thử:** UI / Unit Component Test.
*   **Mô tả mục tiêu:** Kiểm tra nhãn phân loại kích thước thú cưng tự động đổi màu và nội dung theo giá trị Spinner cân nặng để nhân viên tư vấn đúng gói dịch vụ:
    *   Cân nặng < 5.0 kg: `Size Nhỏ (< 5kg)` (Màu xanh da trời).
    *   Cân nặng từ 5.0 đến 10.0 kg: `Size Vừa (5 - 10kg)` (Màu cam Warm Peach).
    *   Cân nặng > 10.0 kg: `Size Lớn (> 10kg)` (Màu tím Soft Purple).
*   **Điều kiện tiên quyết:** Mở `DatLichHenDialog`.
*   **Các bước thực hiện:**
    1.  Chỉnh Spinner `spnCanNang` về các giá trị biên: `4.9` kg -> Quan sát nhãn `lblSizeBadge`.
    2.  Chỉnh Spinner về `5.0` kg -> Quan sát nhãn.
    3.  Chỉnh Spinner về `10.0` kg -> Quan sát nhãn.
    4.  Chỉnh Spinner về `10.1` kg -> Quan sát nhãn.
*   **Dữ liệu kiểm thử:** `4.9`, `5.0`, `10.0`, `10.1`.
*   **Kết quả mong đợi:**
    *   Tại `4.9 kg`: Hiển thị `"Size Nhỏ (< 5kg)"`.
    *   Tại `5.0 kg`: Hiển thị `"Size Vừa (5 - 10kg)"`.
    *   Tại `10.0 kg`: Hiển thị `"Size Vừa (5 - 10kg)"`.
    *   Tại `10.1 kg`: Hiển thị `"Size Lớn (> 10kg)"`.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Chuyển đổi chính xác 100% tại các điểm biên giá trị.

---

#### TC-SPA-003: Phát hiện xung đột / trùng lịch KTV trong cùng khung giờ (Conflict Detection)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.capNhatSlotAvailability`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra logic phát hiện va chạm giờ hẹn: khi KTV A đã có lịch hẹn phục vụ lúc `10:30` (thời lượng 45 phút), nếu nhân viên mở đặt lịch mới cho cùng KTV A vào ngày đó, nút slot `10:30` phải tự động bị vô hiệu hóa (disabled), hiển thị chữ `10:30 (Kín)`.
*   **Điều kiện tiên quyết:**
    *   Đã có lịch hẹn cho KTV `NV004` vào ngày `2026-11-08` lúc `10:30`.
*   **Các bước thực hiện:**
    1.  Mở `DatLichHenDialog`.
    2.  Chọn ngày `2026-11-08`.
    3.  Tại combobox KTV `cboKtv`, chọn `NV004 - Võ Thị Hồng Ngọc`.
    4.  Quan sát trạng thái nút slot `10:30`.
*   **Dữ liệu kiểm thử:** `selectedDate = 2026-11-08`, `ktv = "NV004"`, slot kiểm tra: `"10:30"`.
*   **Kết quả mong đợi:**
    *   Nút slot `10:30` bị disable (`isEnabled() == false`).
    *   Text trên nút đổi thành `"10:30 (Kín)"`.
    *   Người dùng không thể nhấp chọn khung giờ này.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt:* Hệ thống phát hiện xung đột chính xác và ngăn ngừa đặt đè lịch.
*   **Verification SQL Queries:**
```sql
SELECT lh.maLichHen, ctlh.maKtvChiDinh, lh.ngayHen, lh.trangThaiLichHen
FROM LichHen lh
JOIN ChiTietLichHen ctlh ON lh.maLichHen = ctlh.maLichHen
WHERE ctlh.maKtvChiDinh = 'NV004'
  AND CAST(lh.ngayHen AS DATE) = '2026-11-08'
  AND lh.trangThaiLichHen NOT IN (N'Hủy', N'Đã hủy');
```

---

#### TC-SPA-004: Kiểm tra lỗ hổng bỏ qua kiểm tra va chạm khi chọn "Chờ phân công ca trực sau" (DEF-13 / DEF-MAJ-12 Verification)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.capNhatSlotAvailability`, `ThuCungBus.java`.
*   **Cấp độ kiểm thử:** Integration / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-13 / DEF-MAJ-12**:
    *   Khi nhân viên chọn `"Chờ phân công ca trực sau"` trong `cboKtv`, biến `maKtv` rỗng `""`. Vòng lặp kiểm tra va chạm `if (allHen != null && !maKtv.isEmpty())` bị bỏ qua hoàn toàn.
    *   Đồng thời, hàm `ThuCungBus.datLichHenMoi()` không hề có kiểm tra trùng giờ ở tầng BUS/DAO.
    *   Hậu quả: Có thể đặt vô số lịch hẹn trùng cùng 1 khung giờ mà hệ thống không đưa ra bất kỳ cảnh báo nào.
*   **Điều kiện tiên quyết:** Đã có 10 lịch hẹn lúc `14:30` ngày hôm nay.
*   **Các bước thực hiện:**
    1.  Mở `DatLichHenDialog`, chọn KTV là `"Chờ phân công ca trực sau"`.
    2.  Quan sát các nút slot buổi chiều (đặc biệt là slot `14:30`).
    3.  Tiến hành bấm đặt lịch mới lúc `14:30`.
*   **Dữ liệu kiểm thử:** `cboKtv = "Chờ phân công ca trực sau"`, `slot = "14:30"`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại (Bug Trigger):* Nút `14:30` vẫn sáng xanh (enabled), bấm xác nhận vẫn lưu thành công vào CSDL -> **Xác nhận phát hiện khiếm khuyết DEF-13 / DEF-MAJ-12 (Major)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Test PASS nếu tái hiện thành công bug):* Chứng minh được hệ thống cho phép đặt trùng không giới hạn khi không chỉ định KTV.

---

#### TC-SPA-005: Kiểm tra lỗi runtime ép kiểu UDF fn_SinhMaLichHen khi sang ngày mới (DEF-05 / DEF-CRI-03 Verification)
*   **Phân hệ & Màn hình:** Cơ sở dữ liệu SQL Server / `dbo.fn_SinhMaLichHen()`, `LichHenDAO.sinhMaLichHenMoi`.
*   **Cấp độ kiểm thử:** Unit Test (T-SQL Database Engine).
*   **Mô tả mục tiêu:** Xác minh lỗi runtime nghiêm trọng trong hàm UDF `dbo.fn_SinhMaLichHen`:
    *   Khi sang ngày mới (chưa có lịch hẹn nào của ngày hôm đó), `@max = 0`.
    *   Khối `IF @max = 0` được kích hoạt và chạy lệnh:
        `SELECT @max = ISNULL(MAX(CAST(SUBSTRING(maLichHen, 3, LEN(maLichHen)) AS INT)), 0) FROM LichHen WHERE maLichHen LIKE 'LH[0-9]%';`
    *   Nếu trong bảng có mã lịch hẹn ngày trước dạng `LH261107-001`, phép `SUBSTRING(..., 3, ...)` cắt ra chuỗi `'261107-001'`.
    *   Lệnh `CAST('261107-001' AS INT)` lập tức văng lỗi: `Conversion failed when converting the varchar value '261107-001' to data type int.`
    *   DAO bắt Exception và fallback về `"LH001"`, gây trùng lặp khóa chính với dữ liệu có sẵn.
*   **Điều kiện tiên quyết:** Bảng `LichHen` có bản ghi `LH261107-001`.
*   **Các bước thực hiện:**
    1.  Chạy lệnh SQL gọi `dbo.fn_SinhMaLichHen()` vào một ngày mới chưa có dữ liệu.
*   **Dữ liệu kiểm thử:** `LichHen` chứa mã định dạng ngày tháng `LH261107-001`.
*   **Kết quả mong đợi:**
    *   SQL Server văng lỗi `Msg 245, Level 16, State 1: Conversion failed` -> **Xác nhận khiếm khuyết DEF-05 / DEF-CRI-03 (Critical)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Bug Repro Confirmed):* Lỗi ép kiểu được tái hiện thành công.
*   **Verification SQL Queries:**
```sql
-- Kịch bản tái hiện lỗi DEF-05 / DEF-CRI-03:
INSERT INTO LichHen (maLichHen, maKhachHang, ngayHen, trangThaiLichHen)
VALUES ('LH261107-001', 'KH001', GETDATE(), N'Đã đặt');

-- Gọi hàm sinh mã:
SELECT dbo.fn_SinhMaLichHen();
-- KẾT QUẢ QUAN SÁT: Văng lỗi Conversion failed!
```

---

#### TC-SPA-006: Kiểm tra tra cứu nhanh khách hàng cũ bằng Mock Data tĩnh (DEF-15 / DEF-MAJ-14 Verification)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.traCuuKhachHangNhanh`.
*   **Cấp độ kiểm thử:** UI / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-15 / DEF-MAJ-14**: Nút "Tra cứu nhanh" trong hộp thoại đặt lịch hẹn không hề truy vấn CSDL hay gọi tầng BUS mà chỉ kiểm tra cứng 2 số điện thoại `"0988111222"` và `"0977333444"`.
*   **Điều kiện tiên quyết:** Bảng `KhachHang` có khách hàng thực tế với SĐT `0901234567` (`Lê Anh Minh`).
*   **Các bước thực hiện:**
    1.  Mở `DatLichHenDialog`.
    2.  Nhập SĐT `0901234567` vào ô `txtSdt`.
    3.  Bấm nút "Tra cứu".
*   **Dữ liệu kiểm thử:** `sdt = "0901234567"`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng chuẩn:* Hệ thống phải tìm thấy thông tin khách hàng `Lê Anh Minh` từ CSDL và điền vào ô họ tên.
    *   *Thực tế hiện tại (Bug Trigger):* Hệ thống bật popup: "Chưa có dữ liệu cho SĐT này. Tiếp tục nhập thông tin mới." do code bị gán cứng `if (sdt.equals("0988111222")) ... else if ...` -> **Xác nhận khiếm khuyết DEF-15 / DEF-MAJ-14 (Major)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Xác nhận lỗi):* Khách hàng có trong CSDL nhưng bị báo không tìm thấy.

---

#### TC-SPA-007: Kiểm tra đơn giá Dịch vụ Spa bị fix cứng 200.000 đ (DEF-14 / DEF-MAJ-13 Verification)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.xuLyLuuLichHen`, dòng 634.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-14 / DEF-MAJ-13**: Khi người dùng chọn bất kỳ dịch vụ nào (kể cả DV001 giá 300.000 đ, hay DV003 giá 90.000 đ), đối tượng `DichVu` được tạo ra trong `xuLyLuuLichHen` đều bị ép giá cứng:
    `DichVu dv = new DichVu(maDv, tenDv, new BigDecimal("200000"), thoiLuong);`
*   **Điều kiện tiên quyết:** Dịch vụ `DV001` trong CSDL có giá là `300.000 đ`.
*   **Các bước thực hiện:**
    1.  Mở `DatLichHenDialog`.
    2.  Chọn dịch vụ `DV001 - Tắm Spa Bồn Sục Ozone (300.000 đ)`.
    3.  Bấm xác nhận đặt lịch.
*   **Dữ liệu kiểm thử:** `cboDichVu = "DV001 - Tắm Spa Bồn Sục Ozone (300.000 đ)"`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại:* Giá dịch vụ trong đối tượng `ChiTietLichHen` bị ghi nhận là `200.000 đ` thay vì `300.000 đ` -> **Xác nhận khiếm khuyết DEF-14 / DEF-MAJ-13 (Major)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Xác nhận lỗi):* Đơn giá lưu trữ bị sai lệch so với bảng danh mục `DichVu`.
*   **Verification SQL Queries:**
```sql
SELECT giaDichVu FROM DichVu WHERE maDichVu = 'DV001'; -- CSDL là 300000.00
```

---

#### TC-SPA-008: Đặt lịch hẹn luôn tạo mới thú cưng gây nhân bản bản ghi (DEF-16 / DEF-MAJ-15 Verification)
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog.xuLyLuuLichHen`, dòng 628.
*   **Cấp độ kiểm thử:** Integration Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-16 / DEF-MAJ-15**:
    *   Dòng 628: `ThuCung tc = new ThuCung(new dao.ThuCungDAO().sinhMaThuCungMoi(), tenTc, ...);`
    *   Khi khách hàng đặt lịch hẹn cho bé cưng đã có sẵn trong hệ thống (ví dụ bé `Miu Miu`), hệ thống không tái sử dụng mã `TC001` mà luôn tự sinh một mã mới (`TC002`, `TC003`...), khiến một bé cưng bị nhân bản thành nhiều bản ghi khác nhau trong bảng `ThuCung`.
*   **Điều kiện tiên quyết:** Bé cưng `Miu Miu` (`TC001`) đã có trong hệ thống.
*   **Các bước thực hiện:**
    1.  Đặt lịch hẹn mới cho khách `Nguyễn Văn An`, nhập tên bé cưng là `Miu Miu`.
    2.  Bấm xác nhận lưu lịch hẹn.
    3.  Kiểm tra số lượng bản ghi tên `Miu Miu` trong bảng `ThuCung`.
*   **Dữ liệu kiểm thử:** `tenThuCung = "Miu Miu"`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại (Bug Trigger):* Xuất hiện thêm mã thú cưng mới (ví dụ `TC008`) cùng tên `Miu Miu` -> **Xác nhận khiếm khuyết DEF-16 / DEF-MAJ-15 (Major)**.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Xác nhận lỗi):* Phát hiện sinh mã thú cưng thừa thãi.
*   **Verification SQL Queries:**
```sql
SELECT maThuCung, tenThuCung, giongLoai FROM ThuCung WHERE tenThuCung = N'Miu Miu';
-- Kỳ vọng nếu đúng: chỉ có 1 bản ghi TC001.
-- Thực tế bug: xuất hiện nhiều bản ghi cùng tên bé.
```

---

#### TC-SPA-009: Điều phối chuyển trạng thái lịch hẹn Spa
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `QuanLyLichHenPanel`, `ThuCungBus.capNhatTrangThaiLichHen`.
*   **Cấp độ kiểm thử:** Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra vòng đời trạng thái lịch hẹn: `Chờ tiếp nhận` -> `Đang phục vụ` -> `Hoàn thành` (hoặc `Hủy`).
*   **Điều kiện tiên quyết:** Đã có lịch hẹn `LH001` trạng thái `"Đã đặt"`.
*   **Các bước thực hiện:**
    1.  Gọi `controller.capNhatTrangThaiLichHen("LH001", "Đang phục vụ")`.
    2.  Kiểm tra CSDL.
    3.  Gọi `controller.capNhatTrangThaiLichHen("LH001", "Hoàn thành")`.
    4.  Kiểm tra CSDL.
*   **Dữ liệu kiểm thử:** `maLichHen = "LH001"`, trạng thái: `"Đang phục vụ"`, `"Hoàn thành"`.
*   **Kết quả mong đợi:** Cột `trangThaiLichHen` trong bảng `LichHen` cập nhật tương ứng theo từng bước.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Cập nhật thành công không có lỗi SQL.
*   **Verification SQL Queries:**
```sql
SELECT maLichHen, trangThaiLichHen FROM LichHen WHERE maLichHen = 'LH001';
```

---

#### TC-SPA-010: Xóa lịch hẹn và kiểm tra rò rỉ kết nối JDBC thiếu conn.close()
*   **Phân hệ & Màn hình:** Tầng DAO / `dao.LichHenDAO.deleteLichHen`.
*   **Cấp độ kiểm thử:** Unit / Connection Leak Test.
*   **Mô tả mục tiêu:** Xác minh dòng 233-236 trong `LichHenDAO.java`:
    ```java
    } finally {
        if (conn != null) {
            try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }
    ```
    Khối `finally` thiếu `conn.close()`, gây rò rỉ socket kết nối vĩnh viễn mỗi khi xóa lịch hẹn.
*   **Điều kiện tiên quyết:** Tạo lịch hẹn tạm `LH_DEL_TEST`.
*   **Các bước thực hiện:**
    1.  Gọi `lichHenDAO.deleteLichHen("LH_DEL_TEST")`.
    2.  Kiểm tra kết nối trên SQL Server.
*   **Dữ liệu kiểm thử:** `maLichHen = "LH_DEL_TEST"`.
*   **Kết quả mong đợi:** Lịch hẹn bị xóa nhưng kết nối JDBC bị rò rỉ mở.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Xác nhận tồn tại lỗi thiếu `conn.close()` (DEF-BLK-07).

---

#### TC-SPA-011: Kiểm thử tranh chấp đồng thời (Race Condition) khi 2 thu ngân cùng đặt lịch cho 1 KTV tại cùng 1 khung giờ
*   **Phân hệ & Màn hình:** Dịch vụ Spa / `DatLichHenDialog`, `ThuCungBus.datLichHen`, `LichHenDAO.insertLichHen`, CSDL `LichHen`.
*   **Cấp độ kiểm thử:** Concurrency / Integration Test (Multi-Threaded / Multi-User Simulation).
*   **Mục tiêu kiểm thử:** Kiểm chứng lỗ hổng tranh chấp đồng thời (Race Condition) và đặt trùng lịch (Double-Booking) khi 2 thu ngân tại 2 máy/quầy khác nhau cùng thao tác đặt lịch cho cùng 1 Kỹ thuật viên (ví dụ KTV `NV005 - Trần Thị Thu Hà`) tại cùng một ngày và cùng một khung giờ (ví dụ `09:00 - 10:00` ngày mai).
*   **Phân tích bản chất kỹ thuật (Root Cause & Concurrency Gap):**
    *   Tại tầng Giao diện (`DatLichHenDialog.java`), phương thức `capNhatSlotAvailability()` chỉ kiểm tra tình trạng trống của slot tại thời điểm nạp giao diện (Point-in-Time Check). Khi cả 2 quầy cùng mở dialog trước khi lưu, cả 2 bên đều thấy slot `09:00 - 10:00` hiển thị màu xanh (Khả dụng).
    *   Tại tầng Nghiệp vụ (`ThuCungBus.java`) và tầng DAO (`LichHenDAO.java`), hệ thống hoàn toàn thiếu cơ chế khóa giao dịch mức dòng (`SELECT ... WITH (UPDLOCK, HOLDLOCK)`) hoặc cô lập giao dịch mức `SERIALIZABLE`.
    *   Tại tầng Cơ sở dữ liệu (`sql/HeThongQuanLyCuaHangThuCung.sql`), bảng `LichHen` **không có ràng buộc toàn vẹn duy nhất (UNIQUE Constraint)** trên bộ 3 thuộc tính `(maKtv, ngayHen, khungGio)`.
    *   Hệ quả: Cả 2 luồng đồng thời gọi `insertLichHen` đều commit thành công 100%, dẫn đến 1 KTV bị phân công phục vụ 2 thú cưng khác nhau tại cùng 1 thời điểm thực tế (Double-booking defect).
*   **Điều kiện tiên quyết:**
    *   KTV `NV005` (Trần Thị Thu Hà) có lịch trực ngày `2026-11-08`.
    *   Khung giờ `09:00 - 10:00` ngày `2026-11-08` chưa có lịch hẹn nào trước đó.
*   **Các bước thực hiện (Kịch bản đa luồng mô phỏng):**
    1.  **Quầy 1 (Thu ngân A):** Mở `DatLichHenDialog`, chọn ngày mai (`2026-11-08`), chọn KTV `NV005`, chọn slot `09:00 - 10:00` cho thú cưng `Bé Bơ` (Khách A).
    2.  **Quầy 2 (Thu ngân B):** Mở `DatLichHenDialog` cùng thời điểm, chọn cùng ngày mai (`2026-11-08`), chọn cùng KTV `NV005`, chọn slot `09:00 - 10:00` cho thú cưng `Bé Miu` (Khách B). Cả 2 quầy đều thấy slot sáng xanh khả dụng.
    3.  **Kích hoạt đồng thời (Concurrent Execution):** Cả 2 quầy cùng nhấn nút "Xác nhận đặt lịch" gần như tức thời (mô phỏng chênh lệch < 100ms bằng 2 Thread song song).
    4.  Kiểm tra số bản ghi được ghi vào bảng `LichHen` và thông báo trên cả 2 màn hình.
*   **Dữ liệu kiểm thử:**
    *   Thread 1: `ktv = "NV005"`, `ngayHen = "2026-11-08"`, `gioHen = "09:00"`, `khachHang = "Nguyễn Văn A"`, `thuCung = "Bé Bơ"`.
    *   Thread 2: `ktv = "NV005"`, `ngayHen = "2026-11-08"`, `gioHen = "09:00"`, `khachHang = "Trần Thị B"`, `thuCung = "Bé Miu"`.
*   **Kết quả mong đợi theo TIÊU CHUẨN AN TOÀN (Concurrency Safe):**
    *   Hệ thống chỉ cho phép 1 giao dịch thành công (Thread đến trước miligiây).
    *   Giao dịch thứ 2 (Thread đến sau) phải bị chặn lại với thông báo lỗi: "Khung giờ 09:00 - 10:00 của KTV Trần Thị Thu Hà vừa được đặt bởi quầy khác! Vui lòng chọn khung giờ hoặc KTV khác.".
    *   Bảng `LichHen` chỉ có đúng 1 bản ghi duy nhất cho slot đó.
*   **Hành vi thực tế phát hiện lỗi (Defect / Concurrency Vulnerability Evidence - DEF-MAJ-12):**
    *   Cả 2 dialog đều báo "Đặt lịch hẹn thành công!".
    *   CSDL SQL Server ghi nhận cả 2 bản ghi lịch hẹn với cùng `maKtv = 'NV005'`, cùng `ngayHen = '2026-11-08'`, cùng `gioHen = '09:00'`.
    *   Xác nhận lỗi Double-Booking do thiếu cơ chế khóa đồng thời và thiếu UNIQUE constraint ở tầng DB.
*   **Tiêu chí Đạt/Không đạt:**
    *   *Đạt (Phát hiện lỗi thành công):* Ghi nhận 2 bản ghi trùng slot của cùng 1 KTV trong CSDL (Xác nhận lỗi Race Condition / DEF-MAJ-12).
*   **Verification SQL Queries:**
```sql
-- Kiểm tra các lịch hẹn bị trùng lặp KTV trong cùng ngày và giờ
SELECT ngayHen, gioHen, maKtv, COUNT(*) AS SoLuongLichTrung
FROM LichHen
WHERE maKtv IS NOT NULL AND trangThaiLichHen NOT IN (N'Đã hủy')
GROUP BY ngayHen, gioHen, maKtv
HAVING COUNT(*) > 1;
-- KỲ VỌNG AN TOÀN: 0 bản ghi (Không được trùng)
-- THỰC TẾ LỖI: Xuất hiện bản ghi SoLuongLichTrung = 2
```

---

### PHẦN 3: THẺ HỘI VIÊN & GÓI DỊCH VỤ ĐỊNH KỲ SPA PASS

#### TC-PASS-001: Kích hoạt Thẻ Spa Pass mới khi khách mua gói tại quầy POS (Happy Path)
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `SpaPassBus.kichHoatTheMoi`, `TheSpaPassDAO.insertTheSpaPass`.
*   **Cấp độ kiểm thử:** Integration Test (BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra quy trình kích hoạt thẻ gói Spa Pass: khi khách hàng mua gói 5 buổi hoặc 10 buổi tại quầy POS, hệ thống sinh mã thẻ `PASS_<SDT>_xx`, gán số buổi, hạn sử dụng (90 ngày cho gói 5 buổi, 120 ngày cho gói 10 buổi), trạng thái `"ĐANG HOẠT ĐỘNG"`, và lưu bền vững vào CSDL.
*   **Điều kiện tiên quyết:** Khách hàng `Đỗ Quốc Bảo` có SĐT `0933557799`.
*   **Các bước thực hiện:**
    1.  Gọi phương thức:
        `spaPassBus.kichHoatTheMoi("0933557799", "Đỗ Quốc Bảo", "GOI_SPA_05", 5, 900000L);`
*   **Dữ liệu kiểm thử:**
    *   `sdt = "0933557799"`, `tenKhach = "Đỗ Quốc Bảo"`, `soBuoi = 5`, `donGia = 900000L`.
*   **Kết quả mong đợi:**
    *   Trả về đối tượng `TheSpaPass` không null.
    *   Mã thẻ có định dạng `PASS_0933557799_01`.
    *   `tongSoBuoi = 5`, `soBuoiDaDung = 0`, `soBuoiConLai = 5`.
    *   `ngayHetHan = ngayKichHoat + 90 ngày`.
    *   Bảng `TheSpaPass` trong CSDL có bản ghi mới khớp 100% thông tin.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Thẻ được lưu bền vững vào CSDL và nạp vào danh sách RAM.
*   **Verification SQL Queries:**
```sql
SELECT maThe, sdtKhachHang, tenKhachHang, tenGoi, tongSoBuoi, soBuoiDaDung, donGiaGoi, ngayKichHoat, ngayHetHan, trangThai
FROM TheSpaPass
WHERE sdtKhachHang = '0933557799';
-- Kỳ vọng: tongSoBuoi = 5, soBuoiDaDung = 0, trangThai = N'ĐANG HOẠT ĐỘNG'.
```

---

#### TC-PASS-002: Quẹt thẻ trừ 1 buổi khi sử dụng dịch vụ Spa (Happy Path)
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `TheSpaPass.dungMotBuoi`, `SpaPassBus.suDungMotBuoi`.
*   **Cấp độ kiểm thử:** Unit & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra logic trừ buổi của thẻ Spa Pass:
    *   Trước khi quẹt: `soBuoiDaDung = 2`, `soBuoiConLai = 3`.
    *   Sau khi quẹt 1 buổi: `soBuoiDaDung = 3`, `soBuoiConLai = 2`.
    *   Cập nhật CSDL và đối tượng thực thể.
*   **Điều kiện tiên quyết:** Thẻ `PASS_0988111222_01` của khách `Nguyễn Văn An` có 5 buổi, đã dùng 2 buổi, còn 3 buổi.
*   **Các bước thực hiện:**
    1.  Gọi `spaPassBus.suDungMotBuoi("0988111222")`.
    2.  Kiểm tra số buổi còn lại của thẻ trên RAM.
    3.  Kiểm tra số buổi còn lại trong CSDL SQL Server.
*   **Dữ liệu kiểm thử:** `sdt = "0988111222"`.
*   **Kết quả mong đợi:**
    *   Phương thức trả về `true`.
    *   `the.getSoBuoiDaDung() == 3`.
    *   `the.getSoBuoiConLai() == 2`.
    *   CSDL cập nhật `soBuoiDaDung = 3`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Số buổi trừ chính xác 1 đơn vị trên cả RAM và CSDL.
*   **Verification SQL Queries:**
```sql
SELECT maThe, soBuoiDaDung, (tongSoBuoi - soBuoiDaDung) AS soBuoiConLai, trangThai
FROM TheSpaPass
WHERE maThe = 'PASS_0988111222_01';
-- Kỳ vọng: soBuoiDaDung = 3, soBuoiConLai = 2.
```

---

#### TC-PASS-003: Kiểm tra bất đồng bộ RAM vs DB khi cập nhật TheSpaPass thất bại (DEF-10 / DEF-CRI-09 Verification)
*   **Phân hệ & Màn hình:** Tầng Nghiệp vụ / `bus.SpaPassBus.suDungMotBuoi`, dòng 102-108.
*   **Cấp độ kiểm thử:** Integration / Fault Injection Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-10 / DEF-CRI-09**:
    ```java
    boolean ok = the.dungMotBuoi();
    if (ok) {
        try { dao.updateTheSpaPass(the); } catch (Exception e) {
            System.err.println("[SpaPassBus] Lỗi cập nhật TheSpaPass CSDL: " + e.getMessage());
        }
    }
    return ok;
    ```
    Nếu lệnh gọi `dao.updateTheSpaPass(the)` bị lỗi kết nối hoặc văng Exception, biến `the` trên RAM đã bị trừ buổi, hàm vẫn trả về `true` báo thành công cho UI. Khi khởi động lại ứng dụng, CSDL vẫn giữ số buổi cũ, gây lệch pha nghiêm trọng giữa RAM và DB.
*   **Điều kiện tiên quyết:** Giả lập ngắt kết nối mạng hoặc đóng kết nối DB khi quẹt thẻ.
*   **Các bước thực hiện:**
    1.  Gọi `suDungMotBuoi("0988111222")` trong điều kiện DB bị lỗi.
    2.  Quan sát giá trị trả về của hàm và giá trị `soBuoiDaDung` trên RAM.
*   **Dữ liệu kiểm thử:** `sdt = "0988111222"`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại (Bug Trigger):* Hàm vẫn trả về `true` báo trừ thẻ thành công dù CSDL chưa hề được cập nhật -> **Xác nhận khiếm khuyết DEF-10 / DEF-CRI-09 (Critical)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh được sự bất đồng bộ giữa bộ nhớ RAM và CSDL.

---

#### TC-PASS-004: Chặn sử dụng khi Thẻ Spa Pass đã hết số buổi (Edge Case)
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `entity.TheSpaPass.coTheSuDung`, `SpaPassBus.suDungMotBuoi`.
*   **Cấp độ kiểm thử:** Unit & Boundary Test.
*   **Mô tả mục tiêu:** Khi thẻ đã sử dụng hết số buổi (`soBuoiDaDung == tongSoBuoi`, `soBuoiConLai == 0`):
    *   Hệ thống tự động chuyển trạng thái thẻ thành `"ĐÃ HẾT BUỔI"`.
    *   Hàm `coTheSuDung()` trả về `false`.
    *   Hàm `suDungMotBuoi()` trả về `false` và chặn trừ buổi.
    *   Quầy POS yêu cầu khách thanh toán tiền mặt/chuyển khoản bình thường.
*   **Điều kiện tiên quyết:** Thẻ `PASS_HET_BUOI` có `tongSoBuoi = 5`, `soBuoiDaDung = 5`.
*   **Các bước thực hiện:**
    1.  Gọi `the.coTheSuDung()`.
    2.  Gọi `spaPassBus.suDungMotBuoi("0999000111")`.
*   **Dữ liệu kiểm thử:** `tongSoBuoi = 5`, `soBuoiDaDung = 5`.
*   **Kết quả mong đợi:**
    *   `the.getTrangThai()` chuyển thành `"ĐÃ HẾT BUỔI"`.
    *   `the.coTheSuDung() == false`.
    *   `spaPassBus.suDungMotBuoi()` trả về `false`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Tuyệt đối không cho phép quẹt thẻ khi số buổi còn lại <= 0.
*   **Verification SQL Queries:**
```sql
SELECT maThe, tongSoBuoi, soBuoiDaDung, trangThai FROM TheSpaPass WHERE maThe = 'PASS_HET_BUOI';
-- Kỳ vọng: trangThai = N'ĐÃ HẾT BUỔI'.
```

---

#### TC-PASS-005: Chặn sử dụng khi Thẻ Spa Pass đã quá hạn sử dụng (Expired Pass)
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `entity.TheSpaPass.capNhatTrangThai`, `coTheSuDung`.
*   **Cấp độ kiểm thử:** Unit & Boundary Test.
*   **Mô tả mục tiêu:** Thẻ dù còn buổi (`soBuoiConLai = 2`) nhưng ngày hiện tại đã vượt quá `ngayHetHan`:
    *   Trạng thái thẻ tự động đổi thành `"HẾT HẠN SỬ DỤNG"`.
    *   Chặn quẹt thẻ trừ buổi.
*   **Điều kiện tiên quyết:** Thẻ `PASS_EXPIRED` có `ngayHetHan = LocalDate.now().minusDays(1)`.
*   **Các bước thực hiện:**
    1.  Khởi tạo `TheSpaPass` với `ngayHetHan` là hôm qua.
    2.  Gọi `the.coTheSuDung()`.
*   **Dữ liệu kiểm thử:** `ngayHetHan = yesterday`, `soBuoiConLai = 2`.
*   **Kết quả mong đợi:**
    *   `the.getTrangThai()` bằng `"HẾT HẠN SỬ DỤNG"`.
    *   `the.coTheSuDung()` trả về `false`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Chặn thành công thẻ hết hạn.

---

#### TC-PASS-006: Nạp thêm số buổi và gia hạn thẻ Spa Pass cũ
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `SpaPassBus.napThemBuoiChoThe`, `TheSpaPass.napThemBuoi`.
*   **Cấp độ kiểm thử:** Integration Test (BUS -> DAO -> DB).
*   **Mô tả mục tiêu:** Kiểm tra nghiệp vụ nạp thêm buổi cho thẻ cũ: nạp thêm 5 buổi và gia hạn thêm 60 ngày.
    *   Tổng số buổi tăng thêm 5.
    *   Số buổi còn lại tăng thêm 5.
    *   Hạn sử dụng lùi thêm 60 ngày.
    *   Trạng thái chuyển lại thành `"ĐANG HOẠT ĐỘNG"`.
*   **Điều kiện tiên quyết:** Thẻ `PASS_0988111222_01` đang có 5 buổi (còn 2 buổi), hạn cũ là `D`.
*   **Các bước thực hiện:**
    1.  Gọi `spaPassBus.napThemBuoiChoThe("PASS_0988111222_01", 5, 60)`.
*   **Dữ liệu kiểm thử:** `soBuoiNap = 5`, `soNgayGiaHan = 60`.
*   **Kết quả mong đợi:**
    *   `tongSoBuoi` mới = 10.
    *   `soBuoiConLai` mới = 7.
    *   `ngayHetHan` mới = `D + 60 ngày`.
    *   CSDL cập nhật đồng bộ.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Cập nhật chuẩn xác số buổi và hạn dùng mới.
*   **Verification SQL Queries:**
```sql
SELECT maThe, tongSoBuoi, soBuoiDaDung, ngayHetHan, trangThai FROM TheSpaPass WHERE maThe = 'PASS_0988111222_01';
```

---

#### TC-PASS-007: Kiểm tra lỗ hổng POS chỉ trừ duy nhất 1 buổi dù giỏ hàng có nhiều lượt Spa (DEF-17 / DEF-MAJ-16 Verification)
*   **Phân hệ & Màn hình:** Bán hàng POS & Spa Pass / `BanHangPOSPanel.java`, dòng 2958-2967.
*   **Cấp độ kiểm thử:** Integration / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-17 / DEF-MAJ-16**:
    *   Khi khách hàng đưa 2 bé cưng đi làm dịch vụ Spa trong cùng 1 đơn hàng (giỏ hàng có 2 dòng dịch vụ `DV_SPAPASS`).
    *   Tại `BanHangPOSPanel.java`:
        ```java
        for (GioHangItem item : dsGioHang) {
            if ("DV_SPAPASS".equals(item.getMaSanPham())) {
                spaPassBus.suDungMotBuoi(sdtKhach);
                break; // <-- LỖI: Dừng vòng lặp ngay khi gặp item đầu tiên!
            }
        }
        ```
    *   Lệnh `break;` làm cho hệ thống chỉ trừ đúng 1 buổi trên thẻ, bỏ qua lượt spa thứ hai của khách.
*   **Điều kiện tiên quyết:** Thẻ Spa Pass có 5 buổi; giỏ hàng POS thêm 2 dịch vụ Spa Pass.
*   **Các bước thực hiện:**
    1.  Thanh toán đơn hàng chứa 2 dịch vụ Spa Pass.
    2.  Kiểm tra số buổi bị trừ trên thẻ của khách.
*   **Dữ liệu kiểm thử:** Giỏ hàng: 2 item `DV_SPAPASS`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng chuẩn:* Phải trừ 2 buổi trên thẻ.
    *   *Thực tế hiện tại (Bug Trigger):* Thẻ chỉ bị trừ đúng 1 buổi -> **Xác nhận khiếm khuyết DEF-17 / DEF-MAJ-16 (Major)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh được thất thoát lượt trừ buổi do lệnh `break;`.

---

#### TC-PASS-008: Đối soát danh sách Thẻ Hội viên trên giao diện quản trị
*   **Phân hệ & Màn hình:** Thẻ Hội viên / `ThuCungDichVuPanel` (Tab 5), `modelTheHoiVien`, `tblTheHoiVien`.
*   **Cấp độ kiểm thử:** UI / Manual Functional Test.
*   **Mô tả mục tiêu:** Kiểm tra hiển thị danh sách thẻ hội viên, bộ lọc theo trạng thái (`Tất cả`, `Đang hoạt động`, `Đã hết buổi`, `Hết hạn`), và ô tìm kiếm theo tên/SĐT khách hàng.
*   **Điều kiện tiên quyết:** CSDL có ít nhất 3 thẻ với 3 trạng thái khác nhau.
*   **Các bước thực hiện:**
    1.  Chọn Tab 5: "Thẻ Hội Viên & Gói Spa Pass".
    2.  Chọn combobox lọc: `"ĐANG HOẠT ĐỘNG"` -> Kiểm tra bảng chỉ hiển thị thẻ đang hoạt động.
    3.  Nhập SĐT `"0988111222"` vào ô tìm kiếm -> Kiểm tra kết quả lọc.
*   **Dữ liệu kiểm thử:** Bộ lọc: `"ĐANG HOẠT ĐỘNG"`, Từ khóa: `"0988111222"`.
*   **Kết quả mong đợi:** Bảng lọc chính xác theo tiêu chí chọn.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Dữ liệu hiển thị trực quan, đúng tiến độ buổi (ví dụ `3/5 buổi`).

---

### PHẦN 4: KHÁCH SẠN THÚ CƯNG (PET HOTEL & BOARDING MATRIX)

#### TC-HTL-001: Tiếp nhận lưu trú thú cưng (Check-in Pet Hotel thành công)
*   **Phân hệ & Màn hình:** Pet Hotel / `PetHotelPanel.moDialogCheckIn`, `PetHotelBus.capNhatPhong`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra quy trình tiếp nhận thú cưng vào khách sạn lưu trú:
    *   Chọn phòng trống (ví dụ `P101 - Phòng VIP Suite`).
    *   Nhập thông tin chủ nuôi, bé cưng, số ngày dự kiến lưu trú (ví dụ 3 ngày), dặn dò dinh dưỡng/thuốc men.
    *   Phòng chuyển từ trạng thái `"TRỐNG"` sang `"ĐANG CÓ KHÁCH"`.
    *   Ghi nhận thời điểm check-in thực tế và dự kiến check-out (`now + 3 days`).
*   **Điều kiện tiên quyết:** Phòng `P101` đang ở trạng thái `"TRỐNG"`.
*   **Các bước thực hiện:**
    1.  Mở màn hình `PetHotelPanel`.
    2.  Nhấp vào thẻ phòng `P101` hoặc bấm nút "Check-in lưu trú".
    3.  Điền thông tin:
        *   SĐT chủ nuôi: `0988554433`
        *   Tên chủ nuôi: `Phạm Hải Đăng`
        *   Tên bé cưng: `Lucky`
        *   Giống loài: `Chó Corgi 8kg`
        *   Phòng: `P101 - Phòng VIP Suite (350.000 đ/ngày)`
        *   Số ngày dự kiến: `3`
        *   Dặn dò dinh dưỡng: `Ăn hạt ngâm nước ấm 2 bữa/ngày, không ăn đồ ngọt`
    4.  Bấm "Xác nhận Check-in".
*   **Dữ liệu kiểm thử:**
    *   `maPhong = "P101"`, `tenBe = "Lucky"`, `tenChu = "Phạm Hải Đăng"`, `soNgay = 3`, `donGia = 350000L`.
*   **Kết quả mong đợi:**
    *   Hộp thoại đóng lại, thẻ phòng `P101` đổi màu sang cam/đỏ báo `"ĐANG CÓ KHÁCH"`.
    *   Bảng `PhongLuuTru` trong CSDL được cập nhật:
        *   `trangThai = 'ĐANG CÓ KHÁCH'`.
        *   `tenThuCung = 'Lucky'`, `tenChu = 'Phạm Hải Đăng'`, `sdtChu = '0988554433'`.
        *   `thoiGianCheckIn` = thời điểm hiện tại.
        *   `duKienCheckOut` = thời điểm hiện tại + 3 ngày.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Trạng thái phòng và thông tin lưu trú cập nhật đúng 100%.
*   **Verification SQL Queries:**
```sql
SELECT maPhong, loaiPhong, donGiaNgay, trangThai, tenThuCung, tenChu, sdtChu, thoiGianCheckIn, duKienCheckOut, ghiChuChamSoc
FROM PhongLuuTru
WHERE maPhong = 'P101';
-- Kỳ vọng: trangThai = N'ĐANG CÓ KHÁCH', tenThuCung = N'Lucky', DATEDIFF(day, thoiGianCheckIn, duKienCheckOut) = 3.
```

---

#### TC-HTL-002: Kiểm tra tính nguyên tử khi cập nhật trạng thái phòng lưu trú
*   **Phân hệ & Màn hình:** Pet Hotel / `dao.PhongLuuTruDAO.updatePhong`.
*   **Cấp độ kiểm thử:** Unit & Integration Test.
*   **Mô tả mục tiêu:** Đảm bảo câu lệnh SQL `UPDATE PhongLuuTru` cập nhật đồng thời toàn bộ 13 trường dữ liệu của phòng (trạng thái, tên bé, chủ, thời gian, dặn dò, phụ phí) trong 1 thao tác duy nhất.
*   **Điều kiện tiên quyết:** Phòng `P102` tồn tại trong CSDL.
*   **Các bước thực hiện:**
    1.  Khởi tạo đối tượng `PhongLuuTru` với trạng thái mới.
    2.  Gọi `phongLuuTruDAO.updatePhong(p)`.
*   **Dữ liệu kiểm thử:** `maPhong = "P102"`, `trangThai = "ĐANG DỌN DẸP"`.
*   **Kết quả mong đợi:** Phương thức trả về `true`, số dòng tác động `rowsUpdated == 1`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Cập nhật thành công 1 phòng duy nhất.
*   **Verification SQL Queries:**
```sql
SELECT maPhong, trangThai FROM PhongLuuTru WHERE maPhong = 'P102';
```

---

#### TC-HTL-003: Check-out Pet Hotel đúng hạn và tính tiền phòng chuẩn xác (Happy Path)
*   **Phân hệ & Màn hình:** Pet Hotel / `PetHotelPanel.thucHienCheckOut`, `PetHotelBus.luuLichSuLuuTru`.
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra quy trình thanh toán trả phòng đúng hạn:
    *   Thời gian check-in: 2 ngày trước.
    *   Thời gian check-out: hôm nay (đúng hạn).
    *   Tiền phòng = `2 ngày * 350.000 đ = 700.000 đ`.
    *   Phụ phí phát sinh = `50.000 đ`.
    *   Tổng thanh toán = `750.000 đ`.
    *   Lưu lịch sử vào `LichSuLuuTru`.
    *   Phòng chuyển sang trạng thái `"ĐANG DỌN DẸP"`.
    *   Đẩy thông tin sang giỏ hàng POS để thu tiền.
*   **Điều kiện tiên quyết:** Phòng `P101` đang có khách, `donGiaNgay = 350000`, `phiDichVuPhatSinh = 50000`, `thoiGianCheckIn = now - 2 days`.
*   **Các bước thực hiện:**
    1.  Trên giao diện `PetHotelPanel`, chọn phòng `P101`.
    2.  Bấm nút "Check-out & Thanh toán".
    3.  Quan sát hộp thoại xác nhận tính tiền.
    4.  Bấm "Yes" xác nhận đẩy sang POS.
*   **Dữ liệu kiểm thử:** `soNgay = 2`, `donGia = 350000`, `phiPhatSinh = 50000`.
*   **Kết quả mong đợi:**
    *   Hộp thoại hiển thị: Tiền phòng = `700.000 đ`, Phụ phí = `50.000 đ`, Tổng = `750.000 đ`.
    *   Bảng `LichSuLuuTru` có 1 bản ghi mới ghi nhận `soNgayLuuTru = 2`, `tongTien = 750000`.
    *   Phòng `P101` đổi trạng thái thành `"ĐANG DỌN DẸP"`.
    *   Màn hình POS mở ra với giỏ hàng chứa tiền phòng Pet Hotel `750.000 đ`.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Tính đúng 100% số tiền, lưu lịch sử thành công và chuyển tiếp sang POS.
*   **Verification SQL Queries:**
```sql
-- Kiểm tra lịch sử lưu trú vừa ghi:
SELECT TOP 1 maLuuTru, maPhong, tenThuCung, soNgayLuuTru, donGiaNgay, phiPhatSinh, tongTien, thoiGianCheckOut
FROM LichSuLuuTru
WHERE maPhong = 'P101'
ORDER BY maLuuTru DESC;
-- Kỳ vọng: soNgayLuuTru = 2, tongTien = 750000.00.

-- Kiểm tra trạng thái phòng:
SELECT maPhong, trangThai FROM PhongLuuTru WHERE maPhong = 'P101';
-- Kỳ vọng: trangThai = N'ĐANG DỌN DẸP'.
```

---

#### TC-HTL-004: Kiểm tra bỏ qua phụ phí trả trễ khi khách đón thú cưng quá giờ (DEF-08 / DEF-CRI-07 Verification)
*   **Phân hệ & Màn hình:** Pet Hotel / `PetHotelPanel.thucHienCheckOut`, dòng 991-996.
*   **Cấp độ kiểm thử:** UI & Integration / Bug Verification Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-08 / DEF-CRI-07**:
    *   Đoạn code tính tiền check-out:
        ```java
        long days = 1;
        if (p.thoiGianCheckIn != null) {
            days = ChronoUnit.DAYS.between(p.thoiGianCheckIn.toLocalDate(), LocalDateTime.now().toLocalDate());
            if (days <= 0) days = 1;
        }
        long tienPhong = days * p.donGiaNgay;
        long tongTien = tienPhong + p.phiDichVuPhatSinh;
        ```
    *   Hệ thống chỉ tính số ngày giữa ngày check-in và ngày hiện tại mà hoàn toàn bỏ qua trường `duKienCheckOut`.
    *   Ví dụ: Khách đăng ký gửi 1 ngày (dự kiến đón lúc 10:00 sáng), nhưng đến 21:00 tối khách mới đến đón (trễ 11 tiếng). Hệ thống vẫn chỉ tính đúng 1 ngày tiền phòng, gây thất thoát phụ phí lưu chuồng quá giờ.
*   **Điều kiện tiên quyết:** Phòng `P103` check-in lúc 08:00 hôm qua, dự kiến check-out lúc 10:00 hôm nay. Hiện tại là 20:00 tối hôm nay (trễ 10 tiếng).
*   **Các bước thực hiện:**
    1.  Bấm check-out phòng `P103`.
    2.  Quan sát số tiền được tính trong hộp thoại xác nhận.
*   **Dữ liệu kiểm thử:** `checkIn = yesterday 08:00`, `duKienCheckOut = today 10:00`, `now = today 20:00`.
*   **Kết quả mong đợi:**
    *   *Kỳ vọng nghiệp vụ chuẩn:* Phải tính thêm phụ phí quá giờ (ví dụ tính thêm 50% hoặc 1 ngày đơn giá).
    *   *Thực tế hiện tại (Bug Trigger):* Chỉ tính đúng 1 ngày tròn, không hề có phụ phí trễ giờ -> **Xác nhận khiếm khuyết DEF-08 / DEF-CRI-07 (Critical)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh được logic bỏ quên `duKienCheckOut`.

---

#### TC-HTL-005: Kiểm tra thiếu Transaction giữa ghi lịch sử lưu trú và cập nhật trạng thái phòng (DEF-09 / DEF-CRI-08 Verification)
*   **Phân hệ & Màn hình:** Tầng Nghiệp vụ / `bus.PetHotelBus.luuLichSuLuuTru`, `PetHotelPanel.thucHienCheckOut`.
*   **Cấp độ kiểm thử:** Integration / Fault Injection Test.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết **DEF-09 / DEF-CRI-08**:
    *   Tại `PetHotelPanel.thucHienCheckOut()` (dòng 1019-1026):
        1. Gọi `luuLichSuLuuTru(p, days, tongTien)` (mở kết nối 1, thực hiện INSERT vào `LichSuLuuTru`).
        2. Sau đó mới gọi `luuPhongXuongDB(p)` (mở kết nối 2, thực hiện UPDATE `PhongLuuTru`).
    *   Hai thao tác này không nằm trong cùng một Transaction JDBC (`conn.setAutoCommit(false)`). Nếu bước 1 thành công nhưng bước 2 bị lỗi mạng/sập nguồn, bản ghi lịch sử đã ghi nhận check-out nhưng phòng trên thực tế vẫn mang trạng thái `"ĐANG CÓ KHÁCH"`, gây sai lệch số liệu phòng trống và thất thoát quản trị.
*   **Điều kiện tiên quyết:** Giả lập lỗi ở bước cập nhật trạng thái phòng sau khi ghi lịch sử.
*   **Các bước thực hiện:**
    1.  Chạy kịch bản check-out với lỗi giả lập tại bước `updatePhong`.
    2.  Kiểm tra dữ liệu 2 bảng `LichSuLuuTru` và `PhongLuuTru`.
*   **Dữ liệu kiểm thử:** `maPhong = "P101"`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại:* `LichSuLuuTru` đã có bản ghi mới nhưng `PhongLuuTru` vẫn giữ trạng thái cũ, không thể rollback -> **Xác nhận khiếm khuyết DEF-09 / DEF-CRI-08 (Critical)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi):* Chứng minh được sự thiếu tính nguyên tử giữa 2 bảng.

---

#### TC-HTL-006: Ghi nhận nhật ký chăm sóc thú cưng nội trú (Feeding & Walking Log)
*   **Phân hệ & Màn hình:** Pet Hotel / `PetHotelPanel` (Khung chi tiết phòng bên phải).
*   **Cấp độ kiểm thử:** UI & Integration Test.
*   **Mô tả mục tiêu:** Kiểm tra các nút tích chọn chăm sóc hàng ngày cho thú cưng đang lưu trú:
    *   Tích chọn "Đã cho ăn sáng" (`daChoAnSang = true`).
    *   Tích chọn "Đã cho ăn tối" (`daChoAnToi = true`).
    *   Tích chọn "Đã đi dạo vệ sinh" (`daDiDaoVeSinh = true`).
    *   Thêm phụ phí phát sinh (ví dụ mua thêm pate thưởng +45.000 đ).
    *   Lưu cập nhật xuống CSDL.
*   **Điều kiện tiên quyết:** Phòng `P101` đang có khách lưu trú.
*   **Các bước thực hiện:**
    1.  Chọn phòng `P101` trên ma trận phòng.
    2.  Tại khung chi tiết bên phải, tích chọn: "Đã cho ăn sáng" và "Đã đi dạo".
    3.  Nhập phụ phí dịch vụ: cộng thêm `45000` đ (Pate cá hồi).
    4.  Bấm "Lưu nhật ký chăm sóc".
*   **Dữ liệu kiểm thử:** `daChoAnSang = true`, `daDiDaoVeSinh = true`, `phiPhatSinh = 45000`.
*   **Kết quả mong đợi:**
    *   CSDL cập nhật `daChoAnSang = 1`, `daDiDaoVeSinh = 1`, `phiDichVuPhatSinh = 45000`.
    *   Khi nhân viên khác mở lại phòng, các checkbox vẫn giữ đúng trạng thái đã lưu.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Trạng thái chăm sóc lưu bền vững vào CSDL.
*   **Verification SQL Queries:**
```sql
SELECT maPhong, daChoAnSang, daChoAnToi, daDiDaoVeSinh, phiDichVuPhatSinh
FROM PhongLuuTru
WHERE maPhong = 'P101';
-- Kỳ vọng: daChoAnSang = 1, daDiDaoVeSinh = 1, phiDichVuPhatSinh = 45000.
```

---

#### TC-HTL-007: Kiểm tra vi phạm kiến trúc phân tầng Clean Architecture (DEF-21 / DEF-MAJ-20 Verification)
*   **Phân hệ & Màn hình:** Mã nguồn hệ thống / `bus.PetHotelBus`, `dao.PhongLuuTruDAO`.
*   **Cấp độ kiểm thử:** Source Code Architecture Inspection.
*   **Mô tả mục tiêu:** Xác minh khiếm khuyết kiến trúc **DEF-21 / DEF-MAJ-20**:
    *   Tầng Nghiệp vụ (`bus/PetHotelBus.java`, dòng 4): `import gui.PetHotelPanel.PhongLuuTru;`
    *   Tầng Truy cập dữ liệu (`dao/PhongLuuTruDAO.java`, dòng 4): `import gui.PetHotelPanel.PhongLuuTru;`
    *   Cả BUS và DAO đều phụ thuộc trực tiếp vào một Inner Class tĩnh nằm sâu trong tầng Giao diện (GUI). Nếu tầng GUI thay đổi hoặc chạy trên môi trường không có thư viện Swing (Headless/CLI/Unit Test), toàn bộ tầng BUS và DAO sẽ không thể biên dịch độc lập.
*   **Các bước thực hiện:**
    1.  Mở mã nguồn `PetHotelBus.java` và `PhongLuuTruDAO.java`.
    2.  Kiểm tra các lệnh `import`.
*   **Kết quả mong đợi:**
    *   *Thực tế hiện tại:* Tồn tại lệnh import phụ thuộc ngược từ BUS/DAO sang GUI -> **Xác nhận khiếm khuyết DEF-21 / DEF-MAJ-20 (Major)**.
*   **Tiêu chí Đạt/Không đạt:** *Đạt (Xác nhận lỗi kiến trúc):* Phát hiện vi phạm nguyên tắc Clean Architecture.

---

#### TC-HTL-008: Nạp danh sách phòng lưu trú và kiểm tra toàn vẹn dữ liệu
*   **Phân hệ & Màn hình:** Tầng DAO / `dao.PhongLuuTruDAO.getAllPhong`.
*   **Cấp độ kiểm thử:** Unit / DAO Test.
*   **Mô tả mục tiêu:** Đảm bảo phương thức `getAllPhong()` đọc đầy đủ danh sách phòng lưu trú từ SQL Server, ánh xạ chính xác các trường `Timestamp` sang `LocalDateTime` mà không gây lỗi `NullPointerException` khi phòng đang trống (`thoiGianCheckIn == null`).
*   **Điều kiện tiên quyết:** CSDL có 6 phòng mẫu (4 phòng trống, 2 phòng đang có khách).
*   **Các bước thực hiện:**
    1.  Khởi tạo `PhongLuuTruDAO`.
    2.  Gọi `getAllPhong()`.
*   **Kết quả mong đợi:**
    *   Trả về danh sách 6 phòng.
    *   Với các phòng trống: `thoiGianCheckIn` và `duKienCheckOut` nhận giá trị `null` an toàn, không bị văng lỗi.
    *   Với các phòng có khách: `thoiGianCheckIn` được parse chuẩn xác.
*   **Tiêu chí Đạt/Không đạt:** *Đạt:* Trả về danh sách nguyên vẹn, không crash khi gặp giá trị NULL.
*   **Verification SQL Queries:**
```sql
SELECT COUNT(*) AS TongSoPhong,
       SUM(CASE WHEN trangThai = N'TRỐNG' THEN 1 ELSE 0 END) AS SoPhongTrong,
       SUM(CASE WHEN trangThai = N'ĐANG CÓ KHÁCH' THEN 1 ELSE 0 END) AS SoPhongCoKhach
FROM PhongLuuTru;
```

---

## III. TẬP LỆNH SQL KIỂM CHỨNG TRẠNG THÁI CƠ SỞ DỮ LIỆU TOÀN DIỆN (VERIFICATION SUITE)

Để hỗ trợ kiểm thử viên tự động hóa kiểm tra dữ liệu trước và sau mỗi ca kiểm thử, tập lệnh SQL chuẩn hóa dưới đây được thiết lập:

```sql
USE QuanLyCuaHangThuCung;
GO

-- ======================================================================================
-- 1. BÁO CÁO TỔNG QUAN TÌNH TRẠNG DỮ LIỆU KHÁCH HÀNG, THÚ CƯNG & DỊCH VỤ
-- ======================================================================================
PRINT '=== THỐNG KÊ TỔNG QUAN DỮ LIỆU DỊCH VỤ & THÚ CƯNG ===';

SELECT 'KhachHang' AS Bang, COUNT(*) AS TongSoBanGhi FROM KhachHang
UNION ALL
SELECT 'ThuCung', COUNT(*) FROM ThuCung
UNION ALL
SELECT 'HoSoThuCung', COUNT(*) FROM HoSoThuCung
UNION ALL
SELECT 'HoSoSucKhoe', COUNT(*) FROM HoSoSucKhoe
UNION ALL
SELECT 'LichHen', COUNT(*) FROM LichHen
UNION ALL
SELECT 'ChiTietLichHen', COUNT(*) FROM ChiTietLichHen
UNION ALL
SELECT 'TheSpaPass', COUNT(*) FROM TheSpaPass
UNION ALL
SELECT 'PhongLuuTru', COUNT(*) FROM PhongLuuTru
UNION ALL
SELECT 'LichSuLuuTru', COUNT(*) FROM LichSuLuuTru;
GO

-- ======================================================================================
-- 2. TRUY VẤN KIỂM TRA ĐỒNG BỘ THẺ SPA PASS (PHÁT HIỆN LỖI DEF-10)
-- ======================================================================================
SELECT maThe, sdtKhachHang, tenKhachHang, tongSoBuoi, soBuoiDaDung,
       (tongSoBuoi - soBuoiDaDung) AS soBuoiConLaiTinhToan,
       trangThai,
       CASE 
           WHEN (tongSoBuoi - soBuoiDaDung) <= 0 AND trangThai <> N'ĐÃ HẾT BUỔI' THEN N'LỖI: Sai trạng thái hết buổi'
           WHEN GETDATE() > ngayHetHan AND trangThai <> N'HẾT HẠN SỬ DỤNG' THEN N'LỖI: Sai trạng thái quá hạn'
           ELSE N'HỢP LỆ'
       END AS KiemTraTrangThai
FROM TheSpaPass;
GO

-- ======================================================================================
-- 3. TRUY VẤN KIỂM TRA ĐỐI SOÁT CHECK-OUT PET HOTEL (PHÁT HIỆN LỖI DEF-08)
-- ======================================================================================
SELECT p.maPhong, p.tenThuCung, p.thoiGianCheckIn, p.duKienCheckOut, GETDATE() AS ThoiDiemHienTai,
       DATEDIFF(hour, p.duKienCheckOut, GETDATE()) AS SoGioTraTre,
       p.donGiaNgay, p.phiDichVuPhatSinh
FROM PhongLuuTru p
WHERE p.trangThai = N'ĐANG CÓ KHÁCH'
  AND GETDATE() > p.duKienCheckOut;
-- Nếu truy vấn này trả về dòng dữ liệu, những phòng này đang bị trả trễ giờ và cần tính phụ phí!
GO
```
