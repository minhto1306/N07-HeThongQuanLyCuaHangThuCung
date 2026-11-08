# KẾ HOẠCH KIỂM THỬ PHÂN HỆ QUẢN LÝ KHO, SẢN PHẨM & LÔ HÀNG
## (INVENTORY, PRODUCTS, BATCHES & UNIT CONVERSION TEST SPECIFICATION)

---

- **Dự án:** Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)
- **Tài liệu:** Đặc tả Kịch bản Kiểm thử Chi tiết (Detailed Test Case Specification)
- **Mã tài liệu:** `TC_KHO_SAN_PHAM_LO_HANG`
- **Phiên bản:** 1.0 (Production-Grade Test Suite)
- **Ngày ban hành:** 2026-11-07
- **Phân hệ phụ trách:**
  1. Quản lý Kho, Sản phẩm & Lô hàng (`gui.KhoLoHangPanel`, `bus.KhoLoHangBus`, `dao.SanPhamDAO`, `dao.LoHangDAO`)
  2. Nhập sản phẩm & Lô hàng mới chuẩn FEFO (`gui.NhapSanPhamVaLoHangDialog`)
  3. Thiết lập Đơn vị tính Quy đổi & Đa quy cách (`gui.ThietLapQuyDoiDialog`, `entity.QuyDoiDonVi`, `dao.SanPhamDAO`)
- **Hệ quản trị CSDL:** Microsoft SQL Server 2019+ (`QuanLyCuaHangThuCung`)

---

## 1. PHẠM VI & MỤC TIÊU KIỂM THỬ (TEST SCOPE & OBJECTIVES)

### 1.1 Phạm vi kiểm thử (Scope)
Tài liệu này bao phủ toàn diện các chức năng quản trị kho, danh mục sản phẩm, theo dõi hạn sử dụng theo chiến lược FEFO (First Expired, First Out), đơn vị tính quy đổi đa cấp, và các ràng buộc toàn vẹn dữ liệu trong CSDL:
1. **Quản lý Sản phẩm & Nhập Lô hàng Mới:**
   - Tự động sinh mã sản phẩm (`fn_SinhMaSanPham`) và mã lô hàng (`fn_SinhMaLoHang`).
   - Ràng buộc logic ngày sản xuất (NSX) và hạn sử dụng (HSD): HSD phải diễn ra sau NSX, chặn NSX = HSD, chặn NSX > HSD, xử lý năm nhuận (29/02).
   - Ràng buộc tài chính nhập kho: Giá bán lẻ cơ sở không được thấp hơn giá nhập cơ sở (`giaBan < giaNhap`).
2. **Theo dõi Hạn sử dụng & Chiến lược Xuất kho FEFO:**
   - Đánh giá hạn dùng theo 4 cấp độ: An toàn (> 60 ngày), Cận date cảnh báo (31 - 60 ngày), Cận date nguy cấp (<= 30 ngày), Đã hết hạn (HSD < Hôm nay).
   - Kiểm tra bộ lọc hạn dùng trên giao diện `KhoLoHangPanel` và tính điểm đặt hàng tự động theo công thức ROP `= (d * L) + SS`.
   - Rà soát lỗi chí mạng **DEF-KHO-01 / DEF-BLK-02**: Bán nhầm lô hàng đã hết hạn cho khách tại quầy POS do hàm `getAllLoHang()` không lọc hạn dùng.
3. **Thiết lập Quy đổi Đơn vị tính & Đa quy cách (Multi-tier Unit Conversion):**
   - Thiết lập quy đổi đa cấp (ví dụ: Lon lẻ cơ sở -> Lốc 6 lon -> Thùng 24 lon; Bao 20kg -> Kg lẻ cơ sở).
   - Tự động gợi ý bảng giá sỉ chiết khấu 5% theo công thức: `GiaGoiY = round(GiaCoSo * TyLe * 0.95)`.
   - Ràng buộc tỷ lệ quy đổi (`tyLeQuyDoi > 0`), giá bán không âm.
   - Trừ tồn kho theo quy cách quy đổi: Bán 1 Thùng 24 lon -> Trừ chính xác 24 đơn vị cơ sở trong bảng `LoHang` qua `ChiTietXuatLo`.
4. **Xuất kho Phân tán nhiều lô & Chặn tồn kho âm:**
   - Rà soát lỗi **DEF-07 / DEF-KHO-06 / DEF-MAJ-03**: Đơn hàng lớn cần số lượng vượt quá tồn của một lô đơn lẻ bị từ chối bán hoặc nhảy cóc lô vi phạm FEFO.
   - Rà soát lỗi **DEF-10 / DEF-KHO-02 / DEF-CRI-06**: Dòng hàng quy cách bị tách nhiều lô gán sai số lượng cơ sở với đơn giá sỉ trong `ChiTietXuatLo`.
   - Chặn xuất bán vượt quá tồn kho khả dụng và bảo đảm ràng buộc `CHECK (soLuongTon >= 0)` trong SQL Server.
5. **Rà soát & Kiểm chứng Danh mục Lỗi Kỹ thuật (Defect Verification):**
   - DEF-KHO-01 / DEF-BLK-02 (Blocker: Tự động bán lô hàng hết hạn).
   - DEF-10 / DEF-KHO-02 / DEF-CRI-06 (Critical: Sai lệch số lượng và đơn giá khi tách lô).
   - DEF-KHO-03 / DEF-MAJ-09 (Major: Hardcode mã danh mục `DM01`, mã ĐVT `DVT01`, mã NCC `NCC01` và nuốt ngoại lệ CSDL).
   - DEF-KHO-04 / DEF-MAJ-10 (Major: Sinh mã ĐVT `DVT + (size + 1)` gây trùng khóa chính và nuốt ngoại lệ CSDL).
   - DEF-KHO-05 / DEF-MAJ-11 (Major: Thiếu hoàn toàn thực thể chứng từ `PhieuNhap` và `ChiTietPhieuNhap`).
   - DEF-KHO-07 / DEF-MIN-05 (Minor: Hàm `LoHangDAO.congTonKho` không chặn số âm).
   - DEF-BLK-06 (Blocker: Rò rỉ kết nối JDBC trong `SanPhamDAO.deleteSanPham`).

---

## 2. MA TRẬN TRUY XUẤT KIỂM THỬ (TRACEABILITY & COVERAGE MATRIX)

| Mã Test Case | Tên Kịch Bản Kiểm Thử | Thành Phần Mục Tiêu | Cấp Độ | Mức Độ | Trạng Thái Dự Kiến |
|---|---|---|---|---|---|
| **TC-KHO-001** | Nhập sản phẩm mới và lô hàng mới hợp lệ chuẩn FEFO | NhapSanPhamVaLoHangDialog, SanPhamDAO, LoHangDAO | UI / Integration | High | PASS |
| **TC-KHO-002** | Ràng buộc logic ngày tháng trong NhapSanPhamVaLoHangDialog (NSX, HSD, Năm nhuận) | NhapSanPhamVaLoHangDialog (xuLyLuuKho) | UI / Validation | Critical | PASS |
| **TC-KHO-003** | Ràng buộc giá: Chặn giá bán lẻ cơ sở nhỏ hơn giá nhập cơ sở | NhapSanPhamVaLoHangDialog | UI / Validation | High | PASS |
| **TC-KHO-004** | Rà soát lỗi DEF-KHO-03: Hardcode mã DM01, DVT01, NCC01 khi nhập hàng | KhoLoHangPanel (moDialogNhapHang) | Integration / Defect | Major | FAIL (DEF-KHO-03 / DEF-MAJ-09) |
| **TC-KHO-005** | Rà soát lỗi DEF-KHO-05: Thiếu thực thể chứng từ Phiếu Nhập Hàng | SQL Schema, LoHangDAO | Database / Architecture | Major | FAIL (DEF-KHO-05 / DEF-MAJ-11) |
| **TC-LO-001** | Phân loại và hiển thị cảnh báo hạn sử dụng FEFO 4 cấp độ | KhoLoHangPanel (Tab FEFO) | UI / Visual Badge | High | PASS |
| **TC-LO-002** | Rà soát lỗi BLOCKER DEF-KHO-01: POS tự động bán lô hàng ĐÃ HẾT HẠN | BanHangPOSPanel, LoHangDAO (getAllLoHang) | Integration / Defect | Blocker | FAIL (DEF-KHO-01 / DEF-BLK-02) |
| **TC-LO-003** | Kiểm thử tầng DAO: LoHangDAO.getAvailableLotsFEFO lọc chuẩn HSD | LoHangDAO (getAvailableLotsFEFO) | Unit / DAO | High | PASS |
| **TC-QD-001** | Thiết lập đơn vị tính quy đổi đa cấp (Lon -> Lốc 6 -> Thùng 24) và chiết khấu 5% | ThietLapQuyDoiDialog, SanPhamDAO | UI / Integration | High | PASS |
| **TC-QD-002** | Kiểm tra ràng buộc tỷ lệ quy đổi (tyLeQuyDoi <= 0, giá bán âm, trùng tên ĐVT) | ThietLapQuyDoiDialog | UI / Validation | Medium | PASS |
| **TC-QD-003** | Rà soát lỗi DEF-KHO-04: Xung đột khóa chính DVT và nuốt ngoại lệ CSDL | ThietLapQuyDoiDialog (dòng 365-376) | Integration / Defect | Major | FAIL (DEF-KHO-04 / DEF-MAJ-10) |
| **TC-QD-004** | Trừ tồn kho theo quy cách quy đổi: Bán 1 Thùng 24 lon trừ đúng 24 đơn vị cơ sở | BanHangPOSPanel, ChiTietXuatLo, LoHang | Integration / Business | Critical | PASS |
| **TC-LO-004** | Rà soát lỗi DEF-07/DEF-KHO-06: Đơn hàng lớn vượt tồn 1 lô bị từ chối bán | BanHangBus (themSanPhamXuatLo) | Unit / Business Logic | Major | FAIL (DEF-07 / DEF-KHO-06 / DEF-MAJ-03) |
| **TC-LO-005** | Rà soát lỗi DEF-10/DEF-KHO-02: Sai lệch số lượng và đơn giá khi tách lô | BanHangPOSPanel (dòng 2854-2861) | Integration / Defect | Critical | FAIL (DEF-10 / DEF-KHO-02 / DEF-CRI-06) |
| **TC-LO-006** | Chặn xuất bán vượt quá tồn kho khả dụng và kiểm tra ràng buộc CHECK soLuongTon >= 0 | sql (LoHang CHECK), BanHangPOSPanel | Integration / DB Constraint | Critical | PASS |
| **TC-LO-007** | Kiểm thử tầng DAO: LoHangDAO.congTonKho và truTonKho với số âm | LoHangDAO (congTonKho, truTonKho) | Unit / Robustness | Minor | FAIL (DEF-KHO-07 / DEF-MIN-05) |
| **TC-KHO-006** | Xóa sản phẩm và kiểm tra rò rỉ kết nối JDBC trong SanPhamDAO | SanPhamDAO (deleteSanPham) | Integration / Defect | Blocker | FAIL (DEF-BLK-06) |

---

## 3. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (DETAILED TEST CASES)

### 3.1 Nhóm Kiểm Thử Nhập Sản Phẩm & Lô Hàng Mới (Product & Batch Ingestion)

---

#### TC-KHO-001: Nhập sản phẩm mới và lô hàng mới hợp lệ chuẩn FEFO
- **Phân hệ & Component:** Quản lý Kho | `gui.NhapSanPhamVaLoHangDialog`, `dao.SanPhamDAO`, `dao.LoHangDAO`
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh quy trình tạo sản phẩm mới đồng thời nhập lô hàng đầu tiên: Tự động gọi hàm UDF sinh mã `fn_SinhMaSanPham` và `fn_SinhMaLoHang`, kiểm tra ghi nhận vào bảng `SanPham`, `QuyDoiDonVi` (đơn vị cơ sở), và bảng `LoHang`.
- **Điều kiện tiên quyết:**
  - Tài khoản Quản lý `NV001` đang mở màn hình `KhoLoHangPanel`.
  - Danh mục `DM001` (Thức ăn thú cưng) và Nhà cung cấp `NCC001` (Royal Canin VN) tồn tại trong CSDL.
- **Các bước thực hiện:**
  1. Tại `KhoLoHangPanel`, bấm nút `"Nhập hàng mới"` để mở `NhapSanPhamVaLoHangDialog`.
  2. Quan sát ô `txtMaSP` và `txtSoLo` được tự động sinh mã sẵn.
  3. Nhập Tên sản phẩm: `"Pate Mèo Whiskas Vị Cá Hồi 85g"`.
  4. Chọn Danh mục: `"Thức ăn thú cưng"`, ĐVT cơ sở: `"Gói"`, Nhà cung cấp: `"Royal Canin VN"`.
  5. Nhập Đơn giá nhập cơ sở: `"12000"`, Giá bán lẻ cơ sở: `"18000"`.
  6. Nhập Ngày sản xuất: `"01/10/2026"`, Hạn sử dụng: `"01/10/2027"` (thời hạn 12 tháng).
  7. Nhập Số lượng nhập: `50` gói.
  8. Quan sát nhãn `lblFefoBadge` hiển thị: `AN TOÀN (> 60 ngày)`.
  9. Nhấn nút `"Lưu & Nhập kho"`.
- **Dữ liệu kiểm thử:**
  - Tên SP: Pate Mèo Whiskas Vị Cá Hồi 85g.
  - Giá nhập: 12,000 đ; Giá bán: 18,000 đ.
  - NSX: 01/10/2026, HSD: 01/10/2027.
  - Số lượng nhập: 50 gói.
- **Kết quả mong đợi:**
  - Hộp thoại hiển thị thông báo thành công:
    `Nhập kho sản phẩm & lô hàng thành công! • Sản phẩm: Pate Mèo Whiskas... • Lô: LO... (50 Gói)`.
  - Bảng `SanPham` có 1 bản ghi mới với `donViCoSo = N'Gói'`, `giaBanCoSo = 18000.00`.
  - Bảng `QuyDoiDonVi` tự động tạo 1 bản ghi đơn vị cơ sở với `laDonViCoSo = 1`, `tyLeQuyDoi = 1`, `giaBan = 18000.00`.
  - Bảng `LoHang` có 1 bản ghi với `soLuongNhap = 50`, `soLuongTon = 50`, `hanSuDung = '2027-10-01'`.
  - Bảng danh sách sản phẩm và bảng Lô hàng trên `KhoLoHangPanel` tự động cập nhật dòng mới.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Sinh mã chuẩn, lưu đủ 3 bảng, số lượng tồn ban đầu bằng số lượng nhập.
  - Không đạt: Thiếu bản ghi ở bất kỳ bảng nào hoặc tính sai tồn kho.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra sản phẩm vừa tạo
  SELECT TOP 1 maSanPham, tenSanPham, donViCoSo, giaBanCoSo 
  FROM SanPham ORDER BY maSanPham DESC;

  -- Kiểm tra đơn vị cơ sở trong bảng QuyDoiDonVi
  SELECT TOP 1 maQuyDoi, maSanPham, tyLeQuyDoi, laDonViCoSo, giaBan
  FROM QuyDoiDonVi ORDER BY maQuyDoi DESC;

  -- Kiểm tra lô hàng vừa nhập
  SELECT TOP 1 soLo, maSanPham, soLuongNhap, soLuongTon, hanSuDung, donGiaNhapCoSo
  FROM LoHang ORDER BY ngayNhap DESC;
  ```

---

#### TC-KHO-002: Ràng buộc logic ngày tháng trong NhapSanPhamVaLoHangDialog
- **Phân hệ & Component:** Quản lý Kho | `gui.NhapSanPhamVaLoHangDialog` (dòng 372-380, 416-534)
- **Cấp độ kiểm thử:** UI / Validation & Boundary Test
- **Mục tiêu kiểm thử:** Xác minh các ràng buộc kiểm tra tính hợp lệ của Ngày sản xuất (NSX) và Hạn sử dụng (HSD):
  - HSD phải sau NSX (`dHanSD.isAfter(dNgaySX)`).
  - Chặn trường hợp HSD bằng đúng NSX.
  - Chặn trường hợp HSD trước NSX (lỗi logic thời gian).
  - Kiểm tra tính toán ngày hợp lệ trong năm nhuận (29/02/2024, 28/02/2025).
- **Điều kiện tiên quyết:**
  - Mở `NhapSanPhamVaLoHangDialog`.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  1. *Test Case 2.1 (HSD trước NSX):* Nhập NSX = `"20/11/2026"`, HSD = `"10/11/2026"` -> Bấm "Lưu & Nhập kho".
  2. *Test Case 2.2 (HSD bằng NSX):* Nhập NSX = `"15/11/2026"`, HSD = `"15/11/2026"` -> Bấm "Lưu & Nhập kho".
  3. *Test Case 2.3 (Năm nhuận hợp lệ):* Nhập/chọn NSX = `"29/02/2024"`, HSD = `"28/02/2025"` -> Bấm "Lưu & Nhập kho".
  4. *Test Case 2.4 (Năm không nhuận kiểm tra ngày 29/02):*
     - *Đặc tính giao diện (UI Note):* Trường nhập ngày tháng trên `NhapSanPhamVaLoHangDialog` là trường không cho gõ trực tiếp (`non-editable`) và người dùng chọn ngày thông qua hộp thoại lịch popup `JDatePicker`. Trên giao diện `JDatePicker`, năm không nhuận (như 2025) tự động không hiển thị ngày 29/02 để click chọn.
     - *Kịch bản kiểm thử logic phân tích cú pháp (Date Parsing Validation):* Để kiểm tra độ bền vững khi tiếp nhận dữ liệu ngày dị thường (ví dụ truyền từ file Excel, máy quét mã vạch hoặc gọi API nghiệp vụ), kiểm thử trực tiếp hàm logic kiểm tra ngày `LocalDate.parse(txt, DateTimeFormatter.ofPattern("dd/MM/yyyy"))` với chuỗi giả lập NSX = `"29/02/2025"` (năm 2025 không nhuận).
- **Kết quả mong đợi:**
  - Case 2.1 & Case 2.2: Hệ thống chặn lại ngay lập tức, hiển thị `JOptionPane.WARNING_MESSAGE` với tiêu đề `"Lỗi ngày tháng"` và nội dung:
    `Lỗi logic thời gian: Hạn sử dụng (...) phải diễn ra SAU Ngày sản xuất (...)!`. Dữ liệu không được lưu.
  - Case 2.3: `LocalDate.parse` phân tích thành công ngày 29/02/2024 hợp lệ, cho phép lưu vào CSDL.
  - Case 2.4: Bộ phân tích cú pháp `LocalDate.parse` bắt `DateTimeParseException` an toàn, hiển thị popup cảnh báo: `Lỗi định dạng ngày tháng (dd/MM/yyyy)!` (hoặc thông báo ngày không hợp lệ), hoàn toàn không làm sập ứng dụng Java Swing.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Chặn triệt để ngày vô lý, xử lý chuẩn xác năm nhuận.
  - Không đạt: Cho phép lưu lô hàng có HSD trước NSX hoặc crash chương trình.

---

#### TC-KHO-003: Ràng buộc giá - Chặn giá bán lẻ cơ sở nhỏ hơn giá nhập cơ sở
- **Phân hệ & Component:** Quản lý Kho | `gui.NhapSanPhamVaLoHangDialog` (dòng 382-389)
- **Cấp độ kiểm thử:** UI / Business Rule Validation Test
- **Mục tiêu kiểm thử:** Xác minh quy tắc nghiệp vụ kinh doanh: Giá bán lẻ niêm yết không được thấp hơn giá vốn nhập kho (`giaBan < giaNhap`), tránh thất thoát tài chính do sơ suất của nhân viên nhập liệu.
- **Điều kiện tiên quyết:**
  - Mở `NhapSanPhamVaLoHangDialog`.
- **Các bước thực hiện:**
  1. Nhập Giá nhập cơ sở `txtGiaNhap = "50000"`.
  2. Nhập Giá bán lẻ cơ sở `txtGiaBan = "40000"` (bán lỗ 10,000 đ/sản phẩm).
  3. Điền các trường thông tin hợp lệ khác.
  4. Bấm nút `"Lưu & Nhập kho"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị hộp thoại cảnh báo:
    `Giá bán lẻ cơ sở không được nhỏ hơn giá nhập cơ sở!`.
  - Tiêu đề popup: `"Lỗi nghiệp vụ giá"`.
  - Con trỏ tự động focus vào ô `txtGiaBan`.
  - Dữ liệu bị chặn lại, không lưu vào bảng `SanPham` hay `LoHang`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Chặn đúng quy tắc giá, con trỏ focus vào ô giá bán.
  - Không đạt: Cho phép lưu giá bán thấp hơn giá nhập.

---

#### TC-KHO-004: RÀ SOÁT LỖI DEF-KHO-03 / DEF-MAJ-09 - Hardcode mã DM01, DVT01, NCC01 khi nhập hàng & Nuốt ngoại lệ CSDL
- **Phân hệ & Component:** Quản lý Kho | `gui.KhoLoHangPanel` (dòng 426-443)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-KHO-03 / DEF-MAJ-09 - MAJOR DEFECT**)
- **Mục tiêu kiểm thử:**
  1. **Lỗi gán cứng mã ngoại khóa (Hardcoded Foreign Keys):** Trong phương thức `moDialogNhapHang()` của `KhoLoHangPanel`, khi tiếp nhận sự kiện từ dialog để ghi vào CSDL, mã danh mục bị ép cứng thành `"DM01"`, mã đơn vị tính thành `"DVT01"`, mã nhà cung cấp thành `"NCC01"`, bất kể người dùng chọn danh mục nào trên giao diện!
  2. **Lỗi nuốt ngoại lệ CSDL âm thầm (Silent Exception Swallowing):** Tại các dòng `KhoLoHangPanel.java:441-443`:
     ```java
     try {
         // Thực thi thêm sản phẩm và lô hàng vào CSDL...
     } catch (Exception ex) {
         // NUỐT LỖI ÂM THẦM: Không có JOptionPane.showMessageDialog, không log, không rethrow!
     }
     ```
     Nếu câu lệnh INSERT vào CSDL gặp lỗi (như vi phạm khóa chính, mất kết nối SQL Server, hoặc lỗi khóa ngoại), khối `catch` nuốt trọn ngoại lệ. Giao diện không hiển thị bất kỳ thông báo lỗi nào cho người dùng, khiến nhân viên quản lý kho lầm tưởng rằng hàng hóa đã được nhập kho thành công trong khi CSDL hoàn toàn không có dữ liệu!
- **Điều kiện tiên quyết:**
  - CSDL có nhiều danh mục: `DM01` (Thức ăn), `DM02` (Cát vệ sinh), `DM03` (Thuốc thú y).
- **Các bước thực hiện:**
  - **Kịch bản A (Kiểm tra gán cứng mã ngoại khóa):**
    1. Mở `NhapSanPhamVaLoHangDialog`.
    2. Chọn Danh mục: `"Thuốc thú y & Trị ve rận"`, ĐVT cơ sở: `"Chai"`, Nhà cung cấp: `"Bayer Animal Health"`.
    3. Bấm "Lưu & Nhập kho".
    4. Mở SSMS, truy vấn bảng `SanPham` và `LoHang` để kiểm tra mã ngoại khóa được lưu thực tế.
  - **Kịch bản B (Kiểm tra nuốt ngoại lệ khi CSDL lỗi):**
    1. Tạm thời ngắt dịch vụ SQL Server (hoặc cố tình tạo mã sản phẩm trùng lặp để gây `SQLException`).
    2. Điền thông tin nhập hàng và bấm "Lưu & Nhập kho".
    3. Quan sát giao diện người dùng có bật popup cảnh báo lỗi hay im lặng đóng dialog.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Kịch bản A: Sản phẩm vừa tạo bị lưu vào CSDL với `maDanhMuc = 'DM01'` (bị ép thành Thức ăn), `maDonVi = 'DVT01'` (bị ép thành Gói), và `maNhaCungCap = 'NCC01'`.
  - Kịch bản B: Khi CSDL văng ngoại lệ, khối catch tại dòng 441-443 nuốt lỗi hoàn toàn. Giao diện đóng lại bình thường, không có hộp thoại thông báo lỗi nào xuất hiện, nhân viên không hề biết dữ liệu bị thất thoát!
  - **XÁC NHẬN LỖI DEF-KHO-03 / DEF-MAJ-09 TỒN TẠI 100%.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.
- **Verification SQL Queries:**
  ```sql
  SELECT TOP 1 maSanPham, tenSanPham, maDanhMuc, maNhaCungCap 
  FROM SanPham 
  ORDER BY maSanPham DESC;
  -- Nếu maDanhMuc = 'DM01' dù người dùng chọn Thuốc thú y -> DEFECT DEF-KHO-03 / DEF-MAJ-09 CONFIRMED!
  ```

---

#### TC-KHO-005: RÀ SOÁT LỖI DEF-KHO-05 / DEF-MAJ-11 - Thiếu thực thể chứng từ Phiếu Nhập Hàng
- **Phân hệ & Component:** Cơ Sở Dữ Liệu & Kho | `sql/HeThongQuanLyCuaHangThuCung.sql`, `dao.LoHangDAO`
- **Cấp độ kiểm thử:** Database / Architectural Gap Verification (**DEF-KHO-05 / DEF-MAJ-11 - MAJOR GAP**)
- **Mục tiêu kiểm thử:** Xác minh khiếm khuyết thiết kế CSDL (DEF-KHO-05 / DEF-MAJ-11): Hệ thống thiếu hoàn toàn hai thực thể cốt lõi `PhieuNhap` và `ChiTietPhieuNhap`. Khi nhập hàng, các lô hàng được chèn trực tiếp vào bảng `LoHang` mà không gắn với chứng từ hóa đơn nhập nào.
- **Điều kiện tiên quyết:**
  - Kiểm tra lược đồ CSDL `QuanLyCuaHangThuCung`.
- **Các bước thực hiện:**
  1. Chạy câu truy vấn kiểm tra sự tồn tại của bảng `PhieuNhap` trong SQL Server:
     ```sql
     SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_NAME IN ('PhieuNhap', 'ChiTietPhieuNhap', 'DonNhapHang');
     ```
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Kết quả trả về: `0 rows`.
  - Không có bảng nào lưu thông tin: Người giao hàng, Nhân viên nhận hàng, Số hóa đơn đỏ VAT của Nhà cung cấp, Tổng tiền đơn nhập, Trạng thái thanh toán công nợ.
  - **XÁC NHẬN KHIẾM KHUYẾT DEF-KHO-05 / DEF-MAJ-11 TỒN TẠI.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Architectural Gap Detected)**.

---

### 3.2 Nhóm Kiểm Thử Theo Dõi Hạn Dùng & Chiến Lược FEFO (FEFO Expiration Tracking)

---

#### TC-LO-001: Phân loại và hiển thị cảnh báo hạn sử dụng FEFO 4 cấp độ
- **Phân hệ & Component:** Quản lý Kho | `gui.KhoLoHangPanel` (Tab Lô hàng FEFO), `bus.KhoLoHangBus` (`layDanhSachLoCanhBaoHanDung`)
- **Cấp độ kiểm thử:** UI / Visual Badge & Filter Test
- **Mục tiêu kiểm thử:** Xác minh hệ thống phân loại chính xác tình trạng hạn dùng của từng lô hàng theo 4 cấp độ và hiển thị huy hiệu (Badge) màu sắc trực quan:
  1. `AN TOÀN (> 60 ngày)` - Màu xanh lá (`#10B981`).
  2. `CẬN DATE (31 - 60 ngày) - ƯU TIÊN XUẤT SỚM` - Màu vàng cam (`#F59E0B`).
  3. `CẬN DATE NGUY CẤP (<= 30 ngày) - XẢ HÀNG GẤP` - Màu đỏ nguy cấp (`#EF4444`).
  4. `ĐÃ HẾT HẠN SỬ DỤNG - NGƯNG BÁN` - Màu xám đen (`#374151`).
- **Điều kiện tiên quyết:**
  - Trong bảng `LoHang` có 4 lô hàng tương ứng 4 mốc thời gian so với ngày hiện tại (`GETDATE()`):
    * Lô 1: HSD = Hôm nay + 90 ngày.
    * Lô 2: HSD = Hôm nay + 45 ngày.
    * Lô 3: HSD = Hôm nay + 15 ngày.
    * Lô 4: HSD = Hôm nay - 5 ngày.
- **Các bước thực hiện:**
  1. Mở `KhoLoHangPanel`, chọn Tab 2: `"Quản lý Lô hàng & Chiến lược FEFO"`.
  2. Quan sát bảng danh sách lô hàng và các huy hiệu tại cột "Trạng thái FEFO".
  3. Sử dụng combobox lọc `cboLocHSD`:
     - Chọn `"Tất cả lô hàng"` -> Hiển thị cả 4 lô.
     - Chọn `"Lô cận hạn (<= 60 ngày)"` -> Chỉ hiển thị Lô 2, Lô 3, Lô 4.
     - Chọn `"Cận hạn nguy cấp (<= 30 ngày)"` -> Chỉ hiển thị Lô 3 và Lô 4.
     - Chọn `"Đã hết hạn sử dụng"` -> Chỉ hiển thị Lô 4.
- **Kết quả mong đợi:**
  - Màu sắc và nội dung Badge hiển thị chính xác theo số ngày còn hạn (`DATEDIFF(day, GETDATE(), hanSuDung)`).
  - Bộ lọc hoạt động tức thì, không bị sót lô hàng.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Phân loại đúng 4 cấp độ, bộ lọc lọc chính xác.
  - Không đạt: Phân loại sai màu hoặc lọc thiếu lô hàng.

---

#### TC-LO-002: RÀ SOÁT LỖI BLOCKER DEF-KHO-01 / DEF-BLK-02 - POS tự động bán lô hàng ĐÃ HẾT HẠN
- **Phân hệ & Component:** POS & Kho | `gui.BanHangPOSPanel` (dòng 2791, 2845-2850), `dao.LoHangDAO` (dòng 31-47)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-KHO-01 / DEF-BLK-02 - BLOCKER DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng và tái hiện lỗi nghiêm trọng bậc nhất DEF-KHO-01 / DEF-BLK-02: Hệ thống tự động bốc lô hàng ĐÃ HẾT HẠN để xuất bán trước cho khách hàng tại quầy POS, do phương thức `LoHangDAO.getAllLoHang()` chỉ `ORDER BY hanSuDung ASC` mà không lọc `hanSuDung >= GETDATE()`.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` (Pate Whiskas) có 2 lô hàng trong kho:
    * Lô A (`LO_HETHAN`): HSD = `20/10/2026` (ĐÃ QUÁ HẠN), tồn kho = 5 lon.
    * Lô B (`LO_CONHAN`): HSD = `20/12/2026` (CÒN HẠN 43 ngày), tồn kho = 20 lon.
- **Các bước thực hiện:**
  1. Mở màn hình Bán hàng POS (`BanHangPOSPanel`).
  2. Khách hàng mua 2 lon sản phẩm `SP002`.
  3. Bấm "THANH TOÁN (F9)", chọn Tiền mặt và hoàn tất thanh toán.
  4. Mở SSMS, truy vấn bảng `ChiTietXuatLo` để xem hệ thống đã bốc hàng từ số lô nào.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Truy vấn CSDL cho thấy: `ChiTietXuatLo` liên kết với số lô `soLo = 'LO_HETHAN'`!
  - Lô `LO_HETHAN` bị trừ tồn kho từ 5 xuống 3. Lô `LO_CONHAN` hoàn toàn không được đụng đến!
  - **NGUYÊN NHÂN GỐC RỄ:** `BanHangPOSPanel:2791` gọi `busThanhToan.layTatCaLoHang()` (gọi `getAllLoHang()`). Thuật toán duyệt từ đầu danh sách và chỉ kiểm tra `candidate.getSoLuongTon() > 0`. Do Lô hết hạn có HSD cũ nhất nên luôn đứng đầu danh sách `ORDER BY hanSuDung ASC`.
  - **HẬU QUẢ VÔ CÙNG NGUY HIỂM:** Cửa hàng tự động bán đồ ăn hết hạn cho thú cưng, gây nguy cơ ngộ độc, vi phạm pháp luật và đạo đức kinh doanh nghiêm trọng.
  - **XÁC NHẬN LỖI DEF-KHO-01 / DEF-BLK-02 TỒN TẠI 100%.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Blocker Defect Detected)**.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra chi tiết xuất lô vừa thanh toán
  SELECT TOP 1 xl.maChiTietXuatLo, xl.soLo, l.hanSuDung, l.soLuongTon,
         DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) AS SoNgayConHan
  FROM ChiTietXuatLo xl
  JOIN LoHang l ON xl.soLo = l.soLo
  ORDER BY xl.maChiTietXuatLo DESC;
  -- Nếu hanSuDung < GETDATE() -> BLOCKER DEF-KHO-01 / DEF-BLK-02 CONFIRMED!
  ```

---

#### TC-LO-003: Kiểm thử tầng DAO - LoHangDAO.getAvailableLotsFEFO lọc chuẩn HSD
- **Phân hệ & Component:** Tầng Truy Xuất Dữ Liệu | `dao.LoHangDAO` (`getAvailableLotsFEFO`, dòng 231-250)
- **Cấp độ kiểm thử:** Unit Test (DAO Layer)
- **Mục tiêu kiểm thử:** Xác minh phương thức chuẩn `getAvailableLotsFEFO(maSP)` trong `LoHangDAO` có lọc chuẩn điều kiện `l.soLuongTon > 0 AND l.hanSuDung >= CAST(GETDATE() AS DATE)` và sắp xếp tăng dần theo HSD (`ORDER BY l.hanSuDung ASC`).
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` có 3 lô trong CSDL:
    * Lô 1: HSD = Hôm nay - 2 ngày (hết hạn), tồn = 5.
    * Lô 2: HSD = Hôm nay + 60 ngày, tồn = 10.
    * Lô 3: HSD = Hôm nay + 30 ngày, tồn = 8.
- **Các bước thực hiện:**
  1. Viết unit test gọi `LoHangDAO.getAvailableLotsFEFO("SP002")`.
  2. Đếm số lượng phần tử trong danh sách trả về.
  3. Kiểm tra thứ tự các phần tử.
- **Kết quả mong đợi:**
  - Danh sách trả về đúng 2 phần tử (Lô 1 đã hết hạn bị loại bỏ hoàn toàn).
  - Phần tử đầu tiên (index 0) là Lô 3 (HSD cận hơn, +30 ngày).
  - Phần tử thứ hai (index 1) là Lô 2 (HSD xa hơn, +60 ngày).
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Danh sách chỉ gồm lô còn hạn, sắp xếp đúng thứ tự FEFO.
  - Không đạt: Lọt lô hết hạn hoặc sai thứ tự.

---

### 3.3 Nhóm Kiểm Thử Quy Đổi Đơn Vị Tính & Đa Quy Cách (Unit Conversion & Multi-Packaging)

---

#### TC-QD-001: Thiết lập đơn vị tính quy đổi đa cấp (Lon -> Lốc 6 -> Thùng 24) và chiết khấu 5%
- **Phân hệ & Component:** Quản lý Kho | `gui.ThietLapQuyDoiDialog`, `entity.QuyDoiDonVi`, `dao.SanPhamDAO`
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh tính năng thiết lập bảng giá quy cách đóng gói: Chọn sản phẩm cơ sở (Lon), thêm quy cách "Lốc 6 lon" (tỷ lệ 6) và "Thùng 24 lon" (tỷ lệ 24). Kiểm tra công thức tự động tính giá gợi ý chiết khấu 5%: `giaGoiY = round(coSo.giaBan * tyLe * 0.95)`.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` (Pate Whiskas) có ĐVT cơ sở là `"Lon"`, giá bán cơ sở `20,000 đ`.
- **Các bước thực hiện:**
  1. Mở `ThietLapQuyDoiDialog` từ nút "Thiết lập quy đổi ĐVT" trên `KhoLoHangPanel`.
  2. Chọn sản phẩm: `"SP002 - Pate Whiskas Vị Cá Hồi"`.
  3. Thẻ ĐVT gốc hiển thị: `Lon`, Giá cơ sở: `20,000 đ`.
  4. *Thiết lập Quy cách 1:*
     - Nhập Đơn vị quy đổi mới: `"Lốc 6 lon"`.
     - Tỷ lệ quy đổi (`spnTyLe`): `6`.
     - Quan sát ô `txtGiaBanMoi`: Tự động điền giá gợi ý: `20,000 * 6 * 0.95 = 114,000 đ`.
     - Bấm nút `"Thêm quy cách quy đổi"`.
  5. *Thiết lập Quy cách 2:*
     - Nhập Đơn vị quy đổi mới: `"Thùng 24 lon"`.
     - Tỷ lệ quy đổi (`spnTyLe`): `24`.
     - Quan sát ô `txtGiaBanMoi`: Tự động điền giá gợi ý: `20,000 * 24 * 0.95 = 456,000 đ`.
     - Bấm nút `"Thêm quy cách quy đổi"`.
- **Kết quả mong đợi:**
  - Bảng quy cách hiển thị đầy đủ 3 đơn vị: Lon (cơ sở, tỷ lệ 1), Lốc 6 lon (tỷ lệ 6), Thùng 24 lon (tỷ lệ 24).
  - Bảng `QuyDoiDonVi` trong SQL Server được thêm 2 bản ghi mới với `laDonViCoSo = 0`.
  - Màn hình Bán hàng POS nạp được 3 quy cách này vào Catalog để thu ngân bán hàng linh hoạt.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tính đúng giá gợi ý chiết khấu 5%, lưu đầy đủ vào CSDL.
  - Không đạt: Tính sai giá gợi ý hoặc không lưu CSDL.
- **Verification SQL Queries:**
  ```sql
  SELECT qd.maQuyDoi, sp.tenSanPham, dv.tenDonVi, qd.tyLeQuyDoi, qd.laDonViCoSo, qd.giaBan
  FROM QuyDoiDonVi qd
  JOIN SanPham sp ON qd.maSanPham = sp.maSanPham
  JOIN DonViTinh dv ON qd.maDonVi = dv.maDonVi
  WHERE qd.maSanPham = 'SP002'
  ORDER BY qd.tyLeQuyDoi ASC;
  ```

---

#### TC-QD-002: Kiểm tra ràng buộc tỷ lệ quy đổi (tyLeQuyDoi <= 0, giá bán âm)
- **Phân hệ & Component:** Quản lý Kho | `gui.ThietLapQuyDoiDialog`, `sql` (CHECK constraints)
- **Cấp độ kiểm thử:** UI / Validation & DB Constraint Test
- **Mục tiêu kiểm thử:** Xác minh các ràng buộc toàn vẹn của bảng `QuyDoiDonVi`: Chặn tỷ lệ quy đổi nhỏ hơn hoặc bằng 0 (`CHECK (tyLeQuyDoi > 0)`), chặn giá bán quy cách âm, chặn tên quy cách bỏ trống.
- **Điều kiện tiên quyết:**
  - Mở `ThietLapQuyDoiDialog`.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  1. *Test 2.1 (Tên bỏ trống):* Để trống `txtDonViMoi` -> Bấm "Thêm quy cách" -> Báo lỗi: `Vui lòng nhập tên quy cách quy đổi!`.
  2. *Test 2.2 (Tỷ lệ = 0):* Chỉnh `spnTyLe = 0` hoặc âm `-5`. JSpinner bị chặn ở mức tối thiểu 1.
  3. *Test 2.3 (Giá bán âm):* Nhập `txtGiaBanMoi = "-50000"` -> Bấm "Thêm quy cách" -> Báo lỗi: `Giá bán quy cách phải là số nguyên dương hợp lệ!`.
  4. *Test 2.4 (Trực tiếp CSDL):* Chạy script SQL cố tình vi phạm CHECK:
     ```sql
     INSERT INTO QuyDoiDonVi (maQuyDoi, maSanPham, maDonVi, tyLeQuyDoi, laDonViCoSo, giaBan)
     VALUES ('QD_TEST_ERR', 'SP001', 'DVT01', 0, 0, 10000);
     ```
- **Kết quả mong đợi:**
  - Trên giao diện: Mọi giá trị sai đều bị popup chặn lại.
  - Tầng CSDL: SQL Server ném lỗi vi phạm ràng buộc CHECK:
    `The INSERT statement conflicted with the CHECK constraint "CK__QuyDoiDon__tyLeQ__..."`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Chặn thành công cả ở tầng UI và CSDL.
  - Không đạt: Cho phép lưu tỷ lệ <= 0 hoặc giá âm.

---

#### TC-QD-003: RÀ SOÁT LỖI DEF-KHO-04 / DEF-MAJ-10 - Xung đột khóa chính DVT và nuốt ngoại lệ CSDL
- **Phân hệ & Component:** Quản lý Kho | `gui.ThietLapQuyDoiDialog` (dòng 365-376, 387)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-KHO-04 / DEF-MAJ-10 - MAJOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng và tái hiện lỗi DEF-KHO-04 / DEF-MAJ-10: Trong `ThietLapQuyDoiDialog.xuLyThemQuyDoi()`, mã đơn vị tính mới được sinh bằng chuỗi `"DVT" + (size + 1)` không chuẩn định dạng (`DVT01`, `DVT02`). Khi CSDL ném lỗi trùng khóa chính hoặc vi phạm khóa ngoại, khối `try-catch` nuốt ngoại lệ (chỉ in `System.err`), và dòng 387 vẫn thản nhiên hiển thị popup thông báo thành công cho người dùng!
- **Điều kiện tiên quyết:**
  - Bảng `DonViTinh` đã có các mã `DVT01`, `DVT02`, `DVT03` (tổng 3 bản ghi).
- **Các bước thực hiện:**
  1. Thêm một đơn vị tính mới tại `ThietLapQuyDoiDialog`.
  2. Mã sinh ra là `"DVT" + (3 + 1) = "DVT4"` (trong khi quy chuẩn CSDL là 5 ký tự `DVT04`).
  3. Giả lập tình huống CSDL đã có sẵn mã đó hoặc lỗi kết nối.
  4. Bấm "Thêm quy cách quy đổi".
  5. Quan sát thông báo trên màn hình và đối chiếu dữ liệu thực tế trong SQL Server.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Giao diện Swing hiển thị hộp thoại màu xanh: `Đã thêm quy cách đóng gói thành công!`.
  - Nhưng trong SQL Server: Hoàn toàn không có bản ghi nào được thêm vào bảng `DonViTinh` và `QuyDoiDonVi`.
  - Giao diện và RAM hiển thị có hàng, nhưng khi khởi động lại ứng dụng thì quy cách đó biến mất.
  - **XÁC NHẬN LỖI DEF-KHO-04 / DEF-MAJ-10 TỒN TẠI 100%:** Nuốt ngoại lệ CSDL, gây lệch pha nghiêm trọng giữa RAM và Database.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

---

#### TC-QD-004: Trừ tồn kho theo quy cách quy đổi - Bán 1 Thùng 24 lon trừ đúng 24 đơn vị cơ sở
- **Phân hệ & Component:** POS & Kho | `gui.BanHangPOSPanel` (dòng 2840-2865), `entity.ChiTietXuatLo`, `dao.LoHangDAO`
- **Cấp độ kiểm thử:** Integration / Business Logic Test
- **Mục tiêu kiểm thử:** Xác minh nghiệp vụ xuất kho theo quy đổi: Khi khách mua 1 Thùng Pate 24 lon trên màn hình POS, hệ thống phải nhân tỷ lệ quy đổi (`soLuongCoSoCanXuat = 1 * 24 = 24`) và trừ chính xác 24 đơn vị cơ sở trong bảng `LoHang` theo thứ tự FEFO.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` có quy cách `"Thùng 24 lon"` (`tyLeQuyDoi = 24`).
  - Lô `LO261101-02` có số lượng tồn là `50` lon.
- **Các bước thực hiện:**
  1. Mở màn hình POS, chọn bán sản phẩm `SP002` với đơn vị tính là `"Thùng 24 lon"`, số lượng: 1 thùng.
  2. Tổng tiền thanh toán: 450,000 đ.
  3. Hoàn tất thanh toán hóa đơn.
  4. Mở SSMS kiểm tra bảng `ChiTietHoaDon`, `ChiTietXuatLo`, và `LoHang`.
- **Kết quả mong đợi:**
  - Bảng `ChiTietHoaDon` ghi nhận: `soLuong = 1`, đơn giá = 450,000 đ, thành tiền = 450,000 đ.
  - Bảng `ChiTietXuatLo` ghi nhận: `soLuongXuatCoSo = 24`, liên kết với mã quy đổi của Thùng 24 lon.
  - Tồn kho của Lô `LO261101-02` trong bảng `LoHang` giảm chính xác 24 đơn vị: từ 50 xuống còn 26 lon.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Trừ đúng 24 đơn vị cơ sở, chi tiết xuất lô lưu đúng tỷ lệ.
  - Không đạt: Chỉ trừ 1 đơn vị cơ sở hoặc trừ sai số lượng.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra chi tiết hóa đơn và xuất lô
  SELECT ct.maHoaDon, ct.soLuong AS SlBan, qd.tenDonVi, xl.soLo, xl.soLuongXuatCoSo, l.soLuongTon
  FROM ChiTietHoaDon ct
  JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet
  JOIN LoHang l ON xl.soLo = l.soLo
  LEFT JOIN (
      SELECT q.maQuyDoi, dv.tenDonVi FROM QuyDoiDonVi q JOIN DonViTinh dv ON q.maDonVi = dv.maDonVi
  ) qd ON xl.maQuyDoi = qd.maQuyDoi
  WHERE ct.maHoaDon = (SELECT TOP 1 maHoaDon FROM HoaDon ORDER BY ngayLap DESC);
  -- soLuongXuatCoSo phải bằng 24
  ```

---

### 3.4 Nhóm Kiểm Thử Xuất Kho Phân Tán & Chặn Tồn Kho Âm (Split Lot & Stock Floor)

---

#### TC-LO-004: RÀ SOÁT LỖI DEF-07 / DEF-KHO-06 / DEF-MAJ-03 - Đơn hàng lớn vượt tồn 1 lô bị từ chối bán
- **Phân hệ & Component:** POS & Kho | `bus.BanHangBus` (`themSanPhamXuatLo`, dòng 427-441)
- **Cấp độ kiểm thử:** Unit / Business Logic Defect Verification (**DEF-07 / DEF-KHO-06 / DEF-MAJ-03 - MAJOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng và tái hiện lỗi DEF-07 / DEF-KHO-06 / DEF-MAJ-03 trong thuật toán FEFO của `BanHangBus`: Hàm `themSanPhamXuatLo` chỉ tìm 1 lô duy nhất thỏa mãn `soLuongTon >= soLuongCanXuat`. Nếu tổng kho đủ hàng nhưng phân tán ở 2 lô khác nhau, hệ thống nhảy cóc lô hoặc ném ngoại lệ dừng bán sai thực tế.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP001` có 2 lô hàng trong kho:
    * Lô 1 (cận hạn hơn): Tồn 5 gói.
    * Lô 2 (xa hạn hơn): Tồn 10 gói.
    * Tổng tồn kho toàn cửa hàng: 15 gói.
- **Các bước thực hiện:**
  1. *Kịch bản A (Khách mua 8 gói):* Khách cần mua 8 gói.
     - Lô 1 (5 gói) không đủ 8 gói -> Thuật toán bỏ qua Lô 1 và nhảy sang bốc Lô 2 (10 gói).
     - **Quan sát:** Lô 1 cận hạn hơn bị bỏ quên không xuất, vi phạm nguyên tắc FEFO!
  2. *Kịch bản B (Khách mua 12 gói):* Khách cần mua 12 gói (tổng kho 15 gói, thừa sức đáp ứng).
     - Thuật toán tìm không có lô đơn lẻ nào `>= 12`.
     - Hàm ném ngoại lệ `IllegalStateException("Không đủ số lượng tồn kho khả dụng!")`.
     - **Quan sát:** Cửa hàng từ chối bán cho khách dù kho còn đủ 15 gói!
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Hệ thống không hỗ trợ tách đơn hàng lớn qua nhiều lô ở tầng BUS.
  - **XÁC NHẬN LỖI DEF-07 / DEF-KHO-06 / DEF-MAJ-03 TỒN TẠI 100%.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

---

#### TC-LO-005: RÀ SOÁT LỖI DEF-10 / DEF-KHO-02 / DEF-CRI-06 - Sai lệch số lượng, đơn giá & Xử lý phần dư làm tròn khi tách lô
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (dòng 2854-2861), `entity.ChiTietXuatLo`
- **Cấp độ kiểm thử:** Integration / Financial Defect Verification Test (**DEF-10 / DEF-KHO-02 / DEF-CRI-06 - CRITICAL DEFECT**)
- **Mục tiêu kiểm thử:**
  1. **Kiểm chứng lỗi méo mó số liệu kế toán (DEF-10 / DEF-KHO-02 / DEF-CRI-06):** Khi POS tách xuất 1 quy cách đóng gói (ví dụ Thùng 24 lon lấy 10 lon Lô A và 14 lon Lô B), mã nguồn gán `soLuong` của `ChiTietXuatLo` bằng số lượng cơ sở `take`, trong khi đơn giá `dg` vẫn giữ nguyên giá sỉ của cả Thùng!
  2. **Kiểm tra xử lý phần dư làm tròn thập phân khi tách lô (Fractional Rounding Remainder Handling):**
     - Khi một gói/quy cách hàng có giá $P$ và quy cách $N$ đơn vị cơ sở có đơn giá cơ sở bị chia lẻ vô hạn tuần hoàn (ví dụ: Combo 3 gói giá 100,000 đ $\rightarrow$ đơn giá cơ sở = $100,000 / 3 = 33,333.333...$ đ).
     - Khi tách xuất qua 2 lô: Lô 1 lấy 1 gói ($1 \times 33,333 = 33,333$ đ), Lô 2 lấy 2 gói ($2 \times 33,333 = 66,666$ đ).
     - Tổng thành tiền tách lô: $33,333 + 66,666 = 99,999$ đ $\rightarrow$ Bị thiếu hụt 1 đồng so với dòng hóa đơn gốc 100,000 đ!
     - *Yêu cầu chuẩn tài chính:* Hệ thống phải có thuật toán phân bổ phần dư làm tròn (Rounding Remainder Allocation): các dòng tách đầu tiên tính theo `round(take * donGiaCoSo)`, và dòng tách cuối cùng phải hấp thụ toàn bộ phần dư còn lại:
       `thanhTienCuoi = thanhTienTong - sum(thanhTienCacDongDau)`
       nhằm đảm bảo tổng tiền các dòng xuất lô khớp chính xác 100% đến từng đồng với thành tiền hóa đơn.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` bán theo Thùng 24 lon giá 450,000 đ.
  - Sản phẩm `SP003` (Combo đồ chơi) bán theo Combo 3 món giá 100,000 đ.
  - Kho có nhiều lô hàng chia nhỏ số lượng.
- **Các bước thực hiện:**
  - **Kịch bản A (Kiểm tra sai lệch đơn giá khi tách lô):**
    1. Bán 1 Thùng 24 lon trên màn hình POS.
    2. Hệ thống tách xuất: Lô A lấy 10 lon, Lô B lấy 14 lon.
    3. Thanh toán thành công và mở SSMS kiểm tra bảng `ChiTietXuatLo`.
  - **Kịch bản B (Kiểm tra xử lý phần dư làm tròn):**
    1. Bán 1 Combo 3 món giá 100,000 đ.
    2. Hệ thống tách xuất: Lô 1 lấy 1 món, Lô 2 lấy 2 món.
    3. Kiểm tra tổng thành tiền của 2 dòng trong `ChiTietXuatLo` có bằng đúng 100,000 đ hay bị lệch 99,999 đ.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Kịch bản A: Bản ghi thứ nhất lưu `soLuong = 10`, nhưng `donGiaLucBan = 450,000 đ`, `thanhTien = 187,500 đ`. Phép toán `10 * 450,000 = 4,500,000 đ` lệch hoàn toàn với `thanhTien = 187,500 đ`.
  - Kịch bản B: Thiếu cơ chế hấp thụ phần dư làm tròn khiến tổng tiền các dòng xuất lô lệch so với dòng hóa đơn tổng.
  - **XÁC NHẬN LỖI DEF-10 / DEF-KHO-02 / DEF-CRI-06 TỒN TẠI 100%.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.
- **Verification SQL Queries:**
  ```sql
  -- [1] Đối soát đơn giá và thành tiền dòng hàng xuất lô
  SELECT ct.maChiTiet, ct.soLuong, ct.donGiaLucBan, ct.thanhTien,
         (ct.soLuong * ct.donGiaLucBan) AS TichSoLuongDonGia
  FROM ChiTietHoaDon ct
  WHERE ct.maHoaDon = (SELECT TOP 1 maHoaDon FROM HoaDon ORDER BY ngayLap DESC);

  -- [2] Kiểm tra cân đối tổng thành tiền xuất lô so với hóa đơn (chống lệch phần dư làm tròn)
  SELECT ct.maChiTiet, ct.thanhTien AS ThanhTienHoaDon,
         ISNULL(SUM(xl.soLuongXuatCoSo * (ct.donGiaLucBan / qd.tyLeQuyDoi)), 0) AS TongTienXuatLo
  FROM ChiTietHoaDon ct
  LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet
  LEFT JOIN QuyDoiDonVi qd ON xl.maQuyDoi = qd.maQuyDoi
  GROUP BY ct.maChiTiet, ct.thanhTien
  HAVING ct.thanhTien <> ISNULL(SUM(xl.soLuongXuatCoSo * (ct.donGiaLucBan / qd.tyLeQuyDoi)), 0);
  -- Kỳ vọng khi đã xử lý phần dư làm tròn: 0 dòng chênh lệch.
  ```

---

#### TC-LO-006: Chặn xuất bán vượt quá tồn kho khả dụng và kiểm tra ràng buộc CHECK soLuongTon >= 0
- **Phân hệ & Component:** Cơ Sở Dữ Liệu & POS | `sql/HeThongQuanLyCuaHangThuCung.sql` (bảng `LoHang`), `gui.BanHangPOSPanel` (dòng 2867-2874)
- **Cấp độ kiểm thử:** Integration / Database Constraint & Safety Test
- **Mục tiêu kiểm thử:** Xác minh cơ chế bảo vệ kép chống tồn kho âm:
  1. Tầng Giao diện/BUS: Phát hiện thiếu tồn kho và chặn ngay trước khi ghi CSDL, hiển thị popup thông báo số lượng còn thiếu.
  2. Tầng CSDL: Ràng buộc `CHECK (soLuongTon >= 0)` trong bảng `LoHang` từ chối mọi câu lệnh UPDATE làm âm tồn kho.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP001` chỉ còn tổng tồn kho khả dụng là `3` gói.
- **Các bước thực hiện:**
  1. *Thao tác trên POS:* Cố tình nhập bán `5` gói `SP001` (vượt quá tồn 2 gói).
  2. Bấm "THANH TOÁN (F9)".
  3. Quan sát thông báo từ POS.
  4. *Kiểm tra CSDL trực tiếp:* Chạy script SQL cố tình trừ quá số lượng tồn:
     ```sql
     UPDATE LoHang SET soLuongTon = soLuongTon - 10 WHERE maSanPham = 'SP001';
     ```
- **Kết quả mong đợi:**
  - Trên POS: Popup xuất hiện với tiêu đề `"Cảnh báo thiếu tồn kho FEFO"`:
    `Sản phẩm [SP001] không đủ số lượng tồn khả dụng trong các lô hàng còn hạn (còn thiếu 2 đơn vị cơ sở)! Hệ thống dừng thanh toán và chưa ghi nhận phiếu đổi trả hay hóa đơn.`.
  - Không có hóa đơn nào được tạo, giao dịch dừng an toàn.
  - Trên SQL Server: Câu lệnh UPDATE bị chặn đứng với lỗi:
    `The UPDATE statement conflicted with the CHECK constraint "CK__LoHang__soLuongT__...". The conflict occurred in database "QuanLyCuaHangThuCung", table "dbo.LoHang", column 'soLuongTon'.`
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tồn kho không bao giờ bị âm dưới bất kỳ tình huống nào.
  - Không đạt: Tồn kho bị âm hoặc CSDL cho phép ghi số âm.

---

#### TC-LO-007: Kiểm thử tầng DAO - LoHangDAO.congTonKho và truTonKho với số âm (DEF-KHO-07 / DEF-MIN-05)
- **Phân hệ & Component:** Tầng Truy Xuất Dữ Liệu | `dao.LoHangDAO` (dòng 166-188)
- **Cấp độ kiểm thử:** Unit / Defect Verification Test (**DEF-KHO-07 / DEF-MIN-05 - MINOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng phương thức `LoHangDAO.congTonKho(soLo, soLuongCong)` (DEF-KHO-07 / DEF-MIN-05) không có kiểm tra chặn số âm (`if (soLuongCong <= 0) return false;`). Nếu caller truyền số âm, câu lệnh `SET soLuongTon = soLuongTon + ?` sẽ biến thành trừ kho bất hợp pháp.
- **Điều kiện tiên quyết:**
  - Lô hàng `LO001` có tồn kho = 10.
- **Các bước thực hiện:**
  1. Gọi phương thức Java: `loHangDAO.congTonKho("LO001", -5);`.
  2. Kiểm tra tồn kho của `LO001`.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Hàm `congTonKho` thực thi câu lệnh SQL với tham số `-5`.
  - Tồn kho của `LO001` bị giảm từ 10 xuống 5 (hàm cộng kho lại làm giảm kho!).
  - **XÁC NHẬN LỖI DEF-KHO-07 / DEF-MIN-05 TỒN TẠI.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

---

### 3.5 Nhóm Kiểm Thử Xóa Sản Phẩm & Ràng Buộc Khóa Ngoại (Product Lifecycle & Foreign Keys)

---

#### TC-KHO-006: Xóa sản phẩm, xử lý ràng buộc toàn vẹn khóa ngoại & RÀ SOÁT LỖI BLOCKER DEF-BLK-06 (Connection Leak)
- **Phân hệ & Component:** Quản lý Kho | `dao.SanPhamDAO` (`deleteSanPham`, dòng 179-212), `sql` (Foreign Keys)
- **Cấp độ kiểm thử:** Integration / DB Integrity & Defect Verification Test (**DEF-BLK-06 - BLOCKER DEFECT**)
- **Mục tiêu kiểm thử:**
  1. Xác minh quy trình xóa sản phẩm và toàn vẹn dữ liệu:
     - Nếu sản phẩm mới tạo, chưa có lô hàng và chưa phát sinh hóa đơn: Phương thức `deleteSanPham` xóa các bản ghi trong `QuyDoiDonVi` trước rồi mới xóa `SanPham`.
     - Nếu sản phẩm ĐÃ CÓ LÔ HÀNG hoặc ĐÃ CÓ HÓA ĐƠN: Hệ thống phải chặn xóa và hiển thị thông báo lỗi ràng buộc toàn vẹn khóa ngoại, không cho phép xóa làm mất dữ liệu lịch sử.
  2. **Rà soát lỗi Blocker DEF-BLK-06 (Rò rỉ kết nối JDBC):** Trong `SanPhamDAO.java:207-211`, khối `finally` chỉ khôi phục `conn.setAutoCommit(true);` mà hoàn toàn thiếu lệnh `conn.close();`. Mỗi lần thực thi xóa sản phẩm (dù thành công hay văng ngoại lệ khóa ngoại) đều bỏ quên kết nối mở, gây cạn kiệt connection pool của SQL Server khi vận hành liên tục.
- **Điều kiện tiên quyết:**
  - Sản phẩm A (`SP_TEST_NEW`): Mới tạo, không có lô hàng, không có hóa đơn.
  - Sản phẩm B (`SP001`): Đã có lô hàng và hóa đơn bán hàng cũ.
- **Các bước thực hiện:**
  1. Ghi nhận số lượng kết nối đang mở tới CSDL bằng truy vấn `sys.dm_exec_sessions`.
  2. Thực hiện xóa sản phẩm A -> Kiểm tra kết quả trong CSDL.
  3. Cố tình xóa sản phẩm B -> Quan sát phản hồi lỗi ràng buộc toàn vẹn từ hệ thống.
  4. Chạy lại truy vấn kiểm tra số kết nối còn mở để xác định xem kết nối có được đóng giải phóng hay bị rò rỉ.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Sản phẩm A được xóa sạch thành công khỏi bảng `QuyDoiDonVi` và `SanPham`.
  - Sản phẩm B bị CSDL chặn lại với lỗi `REFERENCE constraint "FK__LoHang__maSanPha__..."`. Hệ thống hiển thị thông báo: `Không thể xóa sản phẩm đã phát sinh giao dịch hoặc đang có lô hàng tồn kho!`.
  - **XÁC NHẬN LỖI DEF-BLK-06 TỒN TẠI:** Số lượng sleeping session trong `sys.dm_exec_sessions` tăng dần sau mỗi thao tác gọi `deleteSanPham` do thiếu `conn.close()` trong khối `finally`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Blocker Defect DEF-BLK-06 Detected)**.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra số lượng kết nối JDBC đang mở từ ứng dụng Java
  SELECT session_id, login_time, host_name, program_name, status
  FROM sys.dm_exec_sessions
  WHERE program_name LIKE '%JDBC%' OR program_name LIKE '%Microsoft JDBC Driver%'
  ORDER BY login_time DESC;
  -- Nếu session vẫn ở trạng thái 'sleeping' và không giảm sau khi hàm kết thúc -> DEF-BLK-06 CONFIRMED!
  ```

---

## 4. TỔNG KẾT & KHUYẾN NGHỊ KHẮC PHỤC (SUMMARY & RECOMMENDATIONS)

1. **Về quản trị Lô hàng & FEFO:**
   - Cần khắc phục ngay lỗi Blocker **DEF-KHO-01 / DEF-BLK-02**: Trong `BanHangPOSPanel.java:2791`, thay thế việc gọi `busThanhToan.layTatCaLoHang()` (gọi `getAllLoHang()`) bằng việc gọi hàm có điều kiện lọc hạn dùng `busThanhToan.layDanhSachLoHangKhaDungFEFO()` (gọi `getAvailableLotsFEFO()`).
   - Cần nâng cấp thuật toán **DEF-07 / DEF-KHO-06 / DEF-MAJ-03** trong `BanHangBus` để hỗ trợ xuất hàng phân tán qua nhiều lô khi đơn hàng lớn hơn tồn của 1 lô đơn lẻ.
   - Bổ sung kiểm tra số dương trong `LoHangDAO.congTonKho` / `truTonKho` để loại trừ lỗi **DEF-KHO-07 / DEF-MIN-05**.
2. **Về quy đổi đơn vị tính & Tách lô:**
   - Cần sửa lỗi **DEF-KHO-04 / DEF-MAJ-10**: Sử dụng hàm UDF CSDL `fn_SinhMaQuyDoi()` và kiểm tra trùng lặp khóa chính trước khi INSERT `DonViTinh`, đồng thời không nuốt ngoại lệ mà phải rollback và báo lỗi cho người dùng.
   - Sửa lỗi tính toán **DEF-10 / DEF-KHO-02 / DEF-CRI-06**: Khi tách lô xuất hàng quy đổi, phải tính lại đơn giá dòng hàng tương ứng với số lượng cơ sở và triển khai thuật toán phân bổ phần dư làm tròn (rounding remainder) cho lô cuối cùng.
3. **Về nhập hàng & Quản lý tài nguyên CSDL:**
   - Cần khắc phục lỗi **DEF-KHO-03 / DEF-MAJ-09**: Lấy đúng mã danh mục (`cboDanhMuc`), mã ĐVT (`cboDvtCoSo`) và mã NCC (`cboNhaCungCap`) được chọn thay vì hardcode `DM01`, `DVT01`, `NCC01`, đồng thời xóa bỏ việc nuốt ngoại lệ tại `KhoLoHangPanel.java:441-443`.
   - Bổ sung bảng `PhieuNhap` và `ChiTietPhieuNhap` theo khuyến nghị của lỗi **DEF-KHO-05 / DEF-MAJ-11**.
   - Bổ sung `conn.close()` trong khối `finally` của `SanPhamDAO.deleteSanPham` để triệt tiêu lỗi rò rỉ kết nối nghiêm trọng **DEF-BLK-06**.
