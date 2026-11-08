# KẾ HOẠCH KIỂM THỬ PHÂN HỆ BÁN HÀNG POS & ĐỔI TRẢ HÀNG
## (POS SALES, RETURN & EXCHANGE MANAGEMENT TEST SPECIFICATION)

---

- **Dự án:** Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)
- **Tài liệu:** Đặc tả Kịch bản Kiểm thử Chi tiết (Detailed Test Case Specification)
- **Mã tài liệu:** `TC_POS_BAN_HANG_DOI_TRA`
- **Phiên bản:** 1.0 (Production-Grade Test Suite)
- **Ngày ban hành:** 2026-11-07
- **Phân hệ phụ trách:**
  1. Bán hàng tại quầy POS (`gui.BanHangPOSPanel`, `gui.VietQRDialog`, `bus.BanHangBus`, `dao.HoaDonDAO`, `dao.SanPhamDAO`, `dao.LoHangDAO`, `dao.KhachHangDAO`)
  2. Quản lý Đổi trả hàng hóa (`gui.DoiTraHangPanel`, `bus.DoiTraBus`, `entity.DoiTraTransferData`, `dao.HoaDonDAO`)
- **Hệ quản trị CSDL:** Microsoft SQL Server 2019+ (`QuanLyCuaHangThuCung`)

---

## 1. PHẠM VI & MỤC TIÊU KIỂM THỬ (TEST SCOPE & OBJECTIVES)

### 1.1 Phạm vi kiểm thử (Scope)
Tài liệu này bao phủ toàn diện các thành phần giao diện người dùng (UI), tầng logic nghiệp vụ (BUS), tầng truy xuất dữ liệu (DAO), và các ràng buộc toàn vẹn cơ sở dữ liệu (T-SQL UDF, Stored Procedures, ACID Transactions) liên quan đến chu trình bán hàng và đổi trả hàng tại quầy:
1. **Phương thức thanh toán POS:**
   - Dropdown `cboPhuongThuc` chỉ gồm đúng 2 phương thức chuẩn: "Tiền mặt" và "Chuyển khoản".
   - Luồng thanh toán Tiền mặt: Kiểm tra tiền khách đưa (`txtTienKhachDua`), tính tiền thối lại theo thời gian thực (`lblTienThoiLai`), phím mệnh giá nhanh ("Đủ tiền", "100k", "200k", "500k"), chặn thanh toán khi thiếu tiền.
   - Luồng thanh toán Chuyển khoản VietQR: Kích hoạt modal `VietQRDialog`, khóa cứng số tiền `canTra`, tạo nội dung chuyển khoản chứa `maHD`, xử lý xác nhận và hủy bỏ giao dịch.
2. **Thao tác Giỏ hàng & Phím tắt POS:**
   - Thêm món từ danh mục Catalog, nhấp đúp sửa số lượng trực tiếp tại cột SL, nút Tăng/Giảm/Xóa món, phím tắt F1-F12, lưu tạm đơn hàng và khôi phục đơn chờ phục vụ.
3. **Chương trình Khách hàng Thân thiết & Giảm giá VIP:**
   - Tra cứu khách hàng qua SĐT, tích lũy điểm thưởng mới (100.000đ = 1 điểm), khấu trừ điểm thưởng để giảm giá (`spnDiemDung`, 1 điểm = 1.000đ), giảm giá tự động 5% cho khách hàng VIP (`diemHienCo >= 100`).
4. **Đổi trả hàng hóa (2 Kịch bản):**
   - Kịch bản A: Trả hàng - Hoàn tiền trực tiếp tại quầy bằng phiếu in K80, kiểm tra thời hạn 7 ngày.
   - Kịch bản B: Đổi sang sản phẩm khác / Khách lấy thêm hàng phát sinh chênh lệch (`chenhLech = 0`, `chenhLech > 0`, `chenhLech < 0`), đóng gói `DoiTraTransferData` chuyển tiếp sang `BanHangPOSPanel` để thanh toán phần bù.
5. **Rà soát & Kiểm chứng Danh mục Lỗi Kỹ thuật (Defect Verification):**
   - DEF-02 / DEF-CRI-02 (Không khấu trừ điểm thưởng trong CSDL khi thanh toán).
   - DEF-04 / DEF-BLK-01 (Tràn số và xung đột khóa chính `fn_SinhMaHoaDon` khi vượt quá 99 hóa đơn/ngày).
   - DEF-05 / DEF-CRI-12 (Hàng đổi trả nguyên tem chỉ cộng kho ảo trên RAM, không cập nhật CSDL).
   - DEF-08 / DEF-MAJ-04 (Giao dịch không nguyên tử khi thanh toán đổi hàng tại POS).
   - DEF-09 / DEF-MAJ-05 (Đụng độ mã hóa đơn khi nhiều thu ngân cùng mở quầy bán hàng).
   - DEF-12 / DEF-MIN-02 (Không khóa tạm hóa đơn gốc khi chuyển luồng đổi trả sang POS).
   - DEF-BLK-02 (Xuất bán lô hàng đã hết hạn sử dụng cho khách hàng).
   - DEF-CRI-06 (Sai lệch số lượng và đơn giá dòng hàng khi xuất kho phân tách nhiều lô tại POS).
   - DEF-CRI-11 (Khối finally đóng Statement che giấu conn.close trong HoaDonDAO).
   - DEF-MAJ-03 (Không thể xuất bán đơn hàng lớn nếu vượt quá tồn của một lô đơn lẻ - Lỗi FEFO đơn lô).

---

## 2. MA TRẬN TRUY XUẤT KIỂM THỬ (TRACEABILITY & COVERAGE MATRIX)

| Mã Test Case | Tên Kịch Bản Kiểm Thử | Thành Phần Mục Tiêu | Cấp Độ | Mức Độ | Trạng Thái Dự Kiến |
|---|---|---|---|---|---|
| **TC-POS-001** | Thanh toán Tiền mặt: Khách đưa đủ tiền (`tienDua == canTra`) | BanHangPOSPanel, HoaDonDAO | UI / Integration | High | PASS |
| **TC-POS-002** | Thanh toán Tiền mặt: Khách đưa thừa tiền (`tienDua > canTra`) | BanHangPOSPanel, DocumentListener | UI / Integration | High | PASS |
| **TC-POS-003** | Thanh toán Tiền mặt: Chặn thanh toán khi thiếu tiền (`tienDua < canTra`) | BanHangPOSPanel, JOptionPane | UI / Validation | Critical | PASS |
| **TC-POS-004** | Thanh toán Tiền mặt: Kiểm tra giá trị biên & dữ liệu dị thường ô Tiền khách đưa | BanHangPOSPanel (txtTienKhachDua) | UI / Boundary | Medium | PASS |
| **TC-POS-005** | Thanh toán Tiền mặt: Phím chọn mệnh giá nhanh (Đủ tiền, 100k, 200k, 500k) | BanHangPOSPanel (taoNutGoiYTien) | UI / Functional | Medium | PASS |
| **TC-POS-006** | Tự động chuyển về "Tiền mặt" khi bấm nút mệnh giá từ chế độ Chuyển khoản | BanHangPOSPanel (cboPhuongThuc) | UI / State Transition | Medium | PASS |
| **TC-POS-007** | Thanh toán Chuyển khoản VietQR: Khởi tạo mã QR động và hoàn tất thanh toán | VietQRDialog, BanHangPOSPanel | UI / Integration | Critical | PASS |
| **TC-POS-008** | Thanh toán Chuyển khoản VietQR: Hủy giao dịch QR giữ nguyên giỏ hàng | VietQRDialog, BanHangPOSPanel | UI / Alternative | High | PASS |
| **TC-POS-009** | Thanh toán Chuyển khoản VietQR: Kiểm tra chế độ Offline Fallback Canvas | VietQRDialog | Integration / Fallback | Medium | PASS |
| **TC-POS-010** | Thao tác Giỏ hàng: Thêm, sửa số lượng trực tiếp, tăng/giảm và xóa dòng | BanHangPOSPanel (tblGioHang) | UI / Functional | High | PASS |
| **TC-POS-011** | Thao tác Phím tắt quầy thu ngân (F9, F1-F12, Enter, Esc) | BanHangPOSPanel (KeyBindings) | UI / Usability | Medium | PASS |
| **TC-POS-012** | Lưu tạm đơn hàng và khôi phục đơn chờ phục vụ | BanHangPOSPanel (DonHangTam) | UI / Integration | High | PASS |
| **TC-POS-013** | Chuyển đổi qua lại giữa Tiền mặt và Chuyển khoản trong giỏ hàng có món | BanHangPOSPanel (cboPhuongThuc) | UI / State Transition | Medium | PASS |
| **TC-POS-014** | Tích lũy điểm thưởng thành viên khi thanh toán hóa đơn mới | BanHangBus, KhachHangDAO | Integration / Business | High | PASS |
| **TC-POS-015** | Rà soát lỗi DEF-02: Dùng điểm tích lũy nhưng CSDL không khấu trừ điểm | BanHangPOSPanel, HoaDonDAO | Integration / Defect | Critical | FAIL (DEF-02 / DEF-CRI-02) |
| **TC-POS-016** | Giảm giá tự động 5% cho khách hàng VIP (`diemHienCo >= 100`) | BanHangBus (tinhGiamGiaVIP) | Unit / Integration | High | PASS |
| **TC-POS-017** | Rà soát lỗi DEF-04: Tràn số `fn_SinhMaHoaDon` khi đạt 100 hóa đơn trong ngày | sql (fn_SinhMaHoaDon), HoaDonDAO | Unit / Database | Critical | FAIL (DEF-04 / DEF-BLK-01) |
| **TC-POS-018** | Rà soát lỗi DEF-09: Xung đột mã hóa đơn khi 2 quầy cùng mở phiên bán hàng | BanHangPOSPanel, HoaDonDAO | Integration / Concurrency | Major | FAIL (DEF-09 / DEF-MAJ-05) |
| **TC-POS-019** | Chặn thanh toán giỏ hàng rỗng & Cảnh báo dịch vụ spa/hotel chưa hoàn thành | BanHangPOSPanel, modelGioHang | UI / Validation | High | PASS |
| **TC-DT-001** | Tra cứu hóa đơn cũ trong hạn 7 ngày tại màn hình Đổi trả | DoiTraHangPanel, HoaDonDAO | UI / Integration | High | PASS |
| **TC-DT-002** | Tra cứu hóa đơn quá hạn 7 ngày: Hiển thị cảnh báo và xác nhận ngoại lệ | DoiTraHangPanel (ChronoUnit) | UI / Validation | High | PASS |
| **TC-DT-003** | Đổi trả Kịch bản A: Trả hàng hoàn tiền K80 trực tiếp tại quầy thu ngân | DoiTraHangPanel, DoiTraBus | UI / Integration | Critical | PASS |
| **TC-DT-004** | Đổi trả Kịch bản B: Đổi sang sản phẩm ngang giá (`chenhLech = 0`) sang POS | DoiTraHangPanel -> BanHangPOSPanel | UI / Integration | Critical | PASS |
| **TC-DT-005** | Đổi trả Kịch bản B: Khách lấy thêm hàng bù tiền (`chenhLech > 0`) sang POS | DoiTraHangPanel -> BanHangPOSPanel | UI / Integration | Critical | PASS |
| **TC-DT-006** | Đổi trả Kịch bản B: Đổi sang sản phẩm rẻ hơn (`chenhLech < 0`, hoàn thừa) | DoiTraHangPanel -> BanHangPOSPanel | UI / Integration | Critical | PASS |
| **TC-DT-007** | Hủy bỏ tiếp nhận đổi trả tại POS (`btnHuyDoiTra`) và khôi phục trạng thái | BanHangPOSPanel (huyDoiTraHang) | UI / Alternative | Medium | PASS |
| **TC-DT-008** | Rà soát lỗi DEF-05: Hàng nguyên tem đổi trả chỉ cộng RAM, không nhập CSDL | DoiTraHangPanel, HoaDonDAO | Integration / Defect | Critical | FAIL (DEF-05 / DEF-CRI-12) |
| **TC-DT-009** | Rà soát lỗi DEF-08: Giao dịch không nguyên tử khi thanh toán đổi hàng tại POS | BanHangPOSPanel (thucHienTT) | Integration / Defect | Major | FAIL (DEF-08 / DEF-MAJ-04) |
| **TC-DT-010** | Rà soát lỗi DEF-12: Không khóa hóa đơn gốc khi chuyển dữ liệu sang POS | DoiTraHangPanel, DoiTraTransfer | Integration / Defect | Minor | FAIL (DEF-12 / DEF-MIN-02) |

---

## 3. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (DETAILED TEST CASES)

### 3.1 Nhóm Kiểm Thử Thanh Toán Tiền Mặt & Tiền Thối (Cash Payment & Change)

---

#### TC-POS-001: Thanh toán Tiền mặt - Khách đưa đủ tiền (`tienKhachDua == canTra`)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`txtTienKhachDua`, `lblTienThoiLai`, `btnThanhToan`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh khi khách đưa đúng số tiền cần thanh toán cho phương thức "Tiền mặt", hệ thống tính tiền thối bằng 0 đ, hoàn tất lưu hóa đơn vào CSDL và trừ kho chính xác.
- **Điều kiện tiên quyết:**
  - Tài khoản Thu ngân `NV001` đang đăng nhập vào phiên làm việc.
  - Sản phẩm `SP001` (Hạt Royal Canin) có lô hàng `LO261101-01` còn tồn kho `>= 2` gói, đơn giá 150,000 đ.
- **Các bước thực hiện:**
  1. Mở màn hình POS (`BanHangPOSPanel`).
  2. Nhấp chọn sản phẩm `SP001` với số lượng 2 gói vào giỏ hàng `tblGioHang`.
  3. Quan sát mục "TỔNG THANH TOÁN" hiển thị `300,000 đ`.
  4. Đảm bảo dropdown `cboPhuongThuc` đang chọn "Tiền mặt".
  5. Nhập vào ô `txtTienKhachDua` chuỗi `"300000"`.
  6. Quan sát nhãn `lblTienThoiLai`.
  7. Nhấn nút "THANH TOÁN (F9)" hoặc gõ phím `F9`.
  8. Tại hộp thoại xác nhận thanh toán, nhấn "Có" / "OK".
- **Dữ liệu kiểm thử:**
  - Sản phẩm: `SP001`, Số lượng: 2, Đơn giá: 150,000 đ.
  - Tổng cần trả: 300,000 đ.
  - Tiền khách đưa: 300,000 đ.
- **Kết quả mong đợi:**
  - Ngay khi gõ xong `"300000"`, `lblTienThoiLai` hiển thị `"0 đ"` với màu xanh lá (`ThemeColors.SUCCESS`).
  - Hộp thoại thanh toán thành công hiển thị thông báo: `ĐÃ THANH TOÁN HÓA ĐƠN [HD{yyMMdd}-xx] THÀNH CÔNG!`.
  - Hóa đơn được ghi vào bảng `HoaDon` với `thanhTien = 300000.00`, `phuongThucThanhToan = N'Tiền mặt'`, `trangThaiHoaDon = N'Đã thanh toán'`.
  - Bảng `ChiTietHoaDon` ghi nhận 1 dòng sản phẩm `SP001` với số lượng 2.
  - Lô hàng `LO261101-01` trong bảng `LoHang` bị trừ đúng 2 đơn vị cơ sở.
  - Khung giỏ hàng POS tự động làm sạch và sinh mã hóa đơn mới cho lượt khách tiếp theo.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tiền thối = 0 đ, hóa đơn lưu CSDL đầy đủ, tồn kho trừ đúng 2, không văng ngoại lệ.
  - Không đạt: Tiền thối tính sai, bị chặn thanh toán, hoặc không ghi nhận CSDL.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra tồn kho trước và sau giao dịch
  SELECT soLo, maSanPham, soLuongTon FROM LoHang WHERE maSanPham = 'SP001';

  -- Kiểm tra hóa đơn vừa sinh
  SELECT TOP 1 maHoaDon, maKhachHang, maNhanVien, thanhTien, giamGia, phuongThucThanhToan, trangThaiHoaDon
  FROM HoaDon ORDER BY ngayLap DESC;

  -- Kiểm tra chi tiết hóa đơn và chi tiết xuất lô
  SELECT ct.maChiTiet, ct.maHoaDon, ct.maSanPham, ct.soLuong, ct.thanhTien, xl.soLo, xl.soLuongXuatCoSo
  FROM ChiTietHoaDon ct
  LEFT JOIN ChiTietXuatLo xl ON ct.maChiTiet = xl.maChiTiet
  WHERE ct.maHoaDon = (SELECT TOP 1 maHoaDon FROM HoaDon ORDER BY ngayLap DESC);
  ```

---

#### TC-POS-002: Thanh toán Tiền mặt - Khách đưa thừa tiền (`tienKhachDua > canTra`)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`txtTienKhachDua`, `lblTienThoiLai`, `DocumentListener`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh tính năng tính tiền thối lại theo thời gian thực khi khách đưa số tiền lớn hơn tổng cần trả, định dạng tiền tệ sắc nét chuẩn `#,# đ`.
- **Điều kiện tiên quyết:**
  - POS có giỏ hàng với tổng tiền cần thanh toán là 230,000 đ.
- **Các bước thực hiện:**
  1. Thêm sản phẩm vào giỏ để `lblKhachCanTra` đạt `230,000 đ`.
  2. Chọn phương thức "Tiền mặt".
  3. Focus vào ô `txtTienKhachDua`, gõ lần lượt từng ký tự: `"5"`, `"0"`, `"0"`, `"0"`, `"0"`, `"0"` (500,000 đ).
  4. Quan sát sự thay đổi của nhãn `lblTienThoiLai` sau mỗi ký tự gõ.
  5. Nhấn "THANH TOÁN (F9)".
- **Dữ liệu kiểm thử:**
  - Cần trả: 230,000 đ.
  - Tiền khách đưa: 500,000 đ.
  - Giá trị mong đợi tiền thối: 270,000 đ (`500,000 - 230,000`).
- **Kết quả mong đợi:**
  - Khi gõ `"500000"`, `lblTienThoiLai` hiển thị chính xác `"270,000 đ"`, màu chữ xanh lá (`ThemeColors.SUCCESS`).
  - Khi bấm thanh toán, hộp thoại xem trước hóa đơn hoặc thông báo thành công ghi rõ `Tiền khách đưa: 500,000 đ`, `Tiền thối lại: 270,000 đ`.
  - CSDL lưu hóa đơn với `thanhTien = 230000.00` (không lưu 500,000 đ vào doanh thu).
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tiền thối hiển thị `270,000 đ` ngay lập tức không cần bấm nút phụ; doanh thu hóa đơn ghi đúng 230,000 đ.
  - Không đạt: Tiền thối không cập nhật, hiển thị sai số, hoặc doanh thu ghi nhận nhầm 500,000 đ.
- **Verification SQL Queries:**
  ```sql
  SELECT TOP 1 maHoaDon, thanhTien, phuongThucThanhToan 
  FROM HoaDon ORDER BY ngayLap DESC;
  -- thanhTien phải bằng 230000.00
  ```

---

#### TC-POS-003: Thanh toán Tiền mặt - Chặn thanh toán khi thiếu tiền (`tienKhachDua < canTra`)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (dòng 2733-2749)
- **Cấp độ kiểm thử:** UI / Validation Test
- **Mục tiêu kiểm thử:** Xác minh hệ thống chặn thanh toán, hiển thị popup cảnh báo chi tiết số tiền còn thiếu, tự động select toàn bộ ô nhập liệu để thu ngân nhập lại, và tuyệt đối không ghi dữ liệu rác vào CSDL.
- **Điều kiện tiên quyết:**
  - Giỏ hàng POS có tổng tiền cần thanh toán là 350,000 đ.
- **Các bước thực hiện:**
  1. Giỏ hàng có tổng tiền `canTra = 350,000 đ`.
  2. Chọn phương thức "Tiền mặt".
  3. Nhập vào `txtTienKhachDua` số tiền `200000` (thiếu 150,000 đ).
  4. Quan sát nhãn `lblTienThoiLai`.
  5. Nhấn nút "THANH TOÁN (F9)".
- **Dữ liệu kiểm thử:**
  - Cần trả: 350,000 đ.
  - Tiền khách đưa: 200,000 đ.
  - Số tiền thiếu: 150,000 đ.
- **Kết quả mong đợi:**
  - Tại nhãn `lblTienThoiLai`, hiển thị chữ màu đỏ: `"Chưa đủ (thiếu 150,000 đ)"` (`ThemeColors.DANGER`).
  - Khi bấm thanh toán, hệ thống hiển thị `JOptionPane.WARNING_MESSAGE` với tiêu đề `"Chưa đủ tiền thanh toán"` và nội dung:
    ```
    Tiền khách đưa chưa đủ để hoàn tất thanh toán!

    • Tổng tiền cần thanh toán: 350,000 đ
    • Số tiền khách đã đưa: 200,000 đ
    • Số tiền còn thiếu: 150,000 đ

    Vui lòng nhập đủ tiền trước khi hoàn tất hóa đơn.
    ```
  - Con trỏ tự động focus vào `txtTienKhachDua` và bôi đen toàn bộ số `200000`.
  - Giao dịch dừng lại hoàn toàn, không tạo hóa đơn trong SQL Server, không trừ kho `LoHang`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Xuất hiện đúng popup cảnh báo, con trỏ focus vào ô tiền, CSDL không phát sinh bản ghi mới.
  - Không đạt: Cho phép thanh toán khi thiếu tiền, hoặc không hiển thị thông báo rõ ràng.
- **Verification SQL Queries:**
  ```sql
  -- Xác minh không có hóa đơn nào được tạo trong vòng 1 phút qua
  SELECT COUNT(*) AS SoHoaDonTaoSai
  FROM HoaDon
  WHERE ngayLap >= DATEADD(minute, -1, GETDATE()) AND thanhTien = 350000;
  -- Kết quả mong đợi: 0
  ```

---

#### TC-POS-004: Thanh toán Tiền mặt - Kiểm tra giá trị biên & dữ liệu dị thường ô Tiền khách đưa
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`txtTienKhachDua`, regex parser dòng 2713-2719)
- **Cấp độ kiểm thử:** UI / Boundary & Robustness Test
- **Mục tiêu kiểm thử:** Kiểm tra khả năng xử lý ngoại lệ khi người dùng nhập các chuỗi dữ liệu bất thường: bỏ trống, nhập chữ, số âm, ký tự đặc biệt, số cực lớn vượt ngưỡng `Long.MAX_VALUE`.
- **Điều kiện tiên quyết:**
  - Giỏ hàng có 1 sản phẩm trị giá 100,000 đ.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  1. *Trường hợp 4.1 (Bỏ trống):* Xóa sạch ô `txtTienKhachDua` để trống `""` -> Bấm F9.
  2. *Trường hợp 4.2 (Ký tự chữ):* Gõ `"abcxyz"` vào ô tiền khách đưa -> Quan sát regex lọc và nhãn thối lại.
  3. *Trường hợp 4.3 (Ký tự đặc biệt):* Gõ `"!@#$%^&*()"` -> Quan sát hệ thống.
  4. *Trường hợp 4.4 (Số âm):* Cố tình paste `"-50000"` vào ô nhập -> Bấm F9.
  5. *Trường hợp 4.5 (Số cực đại):* Gõ `"999999999999999999999999"` (vượt kiểu `Long`) -> Bấm F9.
  6. *Trường hợp 4.6 (Biên sát nút):* Khách đưa `99,999 đ` (thiếu đúng 1 đ) -> Bấm F9.
  7. *Trường hợp 4.7 (Biên thừa sát nút):* Khách đưa `100,001 đ` (thừa đúng 1 đ) -> Bấm F9.
- **Kết quả mong đợi:**
  - Case 4.1: Regex xử lý chuỗi rỗng thành giá trị `0 đ`, chặn thanh toán báo thiếu 100,000 đ.
  - Case 4.2 & 4.3: Hàm `replaceAll("[^0-9]", "")` loại bỏ hoàn toàn các ký tự chữ và ký tự đặc biệt, không ném `NumberFormatException` ra console.
  - Case 4.4: Dấu trừ bị loại bỏ hoặc số âm bị chặn ở mức thiếu tiền.
  - Case 4.5: Khối `try-catch (NumberFormatException)` bắt lỗi an toàn và gán `tienKhachDuaVal = 0`, hiển thị nhãn thối lại `"Số tiền không hợp lệ"`, không làm sập ứng dụng.
  - Case 4.6: Báo thiếu đúng `1 đ`.
  - Case 4.7: Báo thối đúng `1 đ`, cho phép thanh toán.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Ứng dụng an toàn 100% trước mọi input dị thường, không crash giao diện.
  - Không đạt: Ứng dụng bị văng ngoại lệ unhandled exception hoặc tính tiền thối ra số âm.

---

#### TC-POS-005: Phím chọn mệnh giá nhanh ("Đủ tiền", "100k", "200k", "500k")
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`taoNutGoiYTien`, dòng 1789-1800, 1945-1974)
- **Cấp độ kiểm thử:** UI / Functional Test
- **Mục tiêu kiểm thử:** Xác minh thao tác 1 chạm của 4 nút mệnh giá nhanh: tự động điền giá trị tiền khách đưa tương ứng và tính lại tiền thối theo thời gian thực.
- **Điều kiện tiên quyết:**
  - Giỏ hàng có tổng tiền cần thanh toán là 160,000 đ.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  1. *Bước 1:* Bấm nút `"100k"`.
     - Kiểm tra: `txtTienKhachDua` hiển thị `"100000"`, `lblTienThoiLai` báo `"Chưa đủ (thiếu 60,000 đ)"`.
  2. *Bước 2:* Bấm nút `"200k"`.
     - Kiểm tra: `txtTienKhachDua` hiển thị `"200000"`, `lblTienThoiLai` báo `"40,000 đ"`.
  3. *Bước 3:* Bấm nút `"500k"`.
     - Kiểm tra: `txtTienKhachDua` hiển thị `"500000"`, `lblTienThoiLai` báo `"340,000 đ"`.
  4. *Bước 4:* Bấm nút `"Đủ tiền"`.
     - Kiểm tra: `txtTienKhachDua` tự động gán bằng đúng `canTra` (`"160000"`), `lblTienThoiLai` báo `"0 đ"`.
- **Kết quả mong đợi:**
  - Các nút phản hồi tức thì với 1 cú click chuột.
  - Số tiền điền vào ô nhập liệu chính xác 100% không bị thừa/thiếu số 0.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Đúng số tiền, tiền thối cập nhật chuẩn xác.
  - Không đạt: Nút không phản hồi hoặc điền sai mệnh giá.

---

#### TC-POS-006: Tự động chuyển về "Tiền mặt" khi bấm nút mệnh giá từ chế độ Chuyển khoản
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (dòng 1957-1960)
- **Cấp độ kiểm thử:** UI / State Transition Test
- **Mục tiêu kiểm thử:** Xác minh khi thu ngân đang ở phương thức "Chuyển khoản" mà bấm vào bất kỳ nút mệnh giá nào (100k, 200k, 500k, Đủ tiền), hệ thống tự động đổi `cboPhuongThuc` về "Tiền mặt" và mở khóa `txtTienKhachDua`.
- **Điều kiện tiên quyết:**
  - Giỏ hàng có tổng tiền 250,000 đ.
  - Thu ngân đang chọn phương thức "Chuyển khoản" (ô `txtTienKhachDua` đang bị khóa mờ `setEnabled(false)`).
- **Các bước thực hiện:**
  1. Quan sát `cboPhuongThuc` hiển thị "Chuyển khoản", ô nhập tiền bị disabled.
  2. Nhấp chuột vào nút `"500k"`.
  3. Quan sát trạng thái của `cboPhuongThuc`, `txtTienKhachDua`, và `lblTienThoiLai`.
- **Kết quả mong đợi:**
  - `cboPhuongThuc` tự động đổi lựa chọn sang `"Tiền mặt"`.
  - `txtTienKhachDua` được mở khóa (`setEnabled(true)`), hiển thị `"500000"`.
  - `lblTienThoiLai` chuyển từ `"0 đ"` sang `"250,000 đ"`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Chuyển mượt mà giữa 2 trạng thái thanh toán, mở khóa ô nhập liệu.
  - Không đạt: Ô nhập liệu vẫn bị khóa hoặc dropdown không tự chuyển về Tiền mặt.

---

### 3.2 Nhóm Kiểm Thử Thanh Toán Chuyển Khoản VietQR (VietQR Dynamic Payment)

---

#### TC-POS-007: Thanh toán Chuyển khoản VietQR - Khởi tạo mã QR động và hoàn tất thanh toán
- **Phân hệ & Component:** POS & Bán Hàng | `gui.VietQRDialog`, `gui.BanHangPOSPanel` (dòng 2751-2766)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh quy trình tạo mã VietQR động chuẩn NAPAS 24/7, khóa cứng số tiền đơn hàng, hiển thị mã QR kèm thông tin tài khoản, và khi thu ngân bấm "Xác nhận đã nhận tiền (Enter)" thì ghi nhận hóa đơn hợp lệ.
- **Điều kiện tiên quyết:**
  - File cấu hình `config/vietqr.properties` tồn tại với thông tin STB MB Bank `0349445392`, Chủ TK `LE KHAI MINH`.
  - Giỏ hàng có tổng tiền cần thanh toán là 450,000 đ.
- **Các bước thực hiện:**
  1. Chọn phương thức "Chuyển khoản" tại `cboPhuongThuc`.
  2. Quan sát ô `txtTienKhachDua` tự động điền `"450000"` và bị khóa không cho sửa; `lblTienThoiLai` hiển thị `"0 đ"`.
  3. Nhấn "THANH TOÁN (F9)".
  4. Hộp thoại `VietQRDialog` xuất hiện.
  5. Kiểm tra thông tin hiển thị trên dialog:
     - Tiêu đề dialog: `THANH TOÁN VIETQR 24/7 - ĐƠN HÀNG HD...`
     - Số tiền: `450,000 đ` (nổi bật font to).
     - Nội dung chuyển khoản: Chứa chính xác mã hóa đơn `HD{yyMMdd}-xx`.
     - Hình ảnh mã QR: Được tải thành công từ máy chủ VietQR API (hoặc fallback canvas).
  6. Thu ngân bấm nút `"XÁC NHẬN ĐÃ NHẬN TIỀN (Enter)"` hoặc gõ phím `Enter`.
- **Dữ liệu kiểm thử:**
  - Khách hàng: `Nguyễn Văn A` (0988111222).
  - Cần trả: 450,000 đ.
  - Phương thức: "Chuyển khoản".
- **Kết quả mong đợi:**
  - Modal `VietQRDialog` đóng lại, hàm `isXacNhanThanhCong()` trả về `true`.
  - Hệ thống tiến hành lưu Hóa đơn vào SQL Server với:
    * `thanhTien = 450000.00`
    * `phuongThucThanhToan = N'Chuyển khoản'`
    * `trangThaiHoaDon = N'Đã thanh toán'`
  - Hàng hóa trong kho được trừ chuẩn xác theo FEFO.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Mã QR hiển thị đầy đủ thông tin, thanh toán thành công, CSDL ghi nhận phương thức "Chuyển khoản".
  - Không đạt: Không hiển thị được dialog, hoặc thanh toán thất bại.
- **Verification SQL Queries:**
  ```sql
  SELECT TOP 1 maHoaDon, thanhTien, phuongThucThanhToan, trangThaiHoaDon
  FROM HoaDon ORDER BY ngayLap DESC;
  -- Phải có phuongThucThanhToan = N'Chuyển khoản'
  ```

---

#### TC-POS-008: Thanh toán Chuyển khoản VietQR - Hủy giao dịch QR giữ nguyên giỏ hàng
- **Phân hệ & Component:** POS & Bán Hàng | `gui.VietQRDialog` (`btnHuy`, dòng 2754-2756)
- **Cấp độ kiểm thử:** UI / Alternative Flow Test
- **Mục tiêu kiểm thử:** Xác minh khi khách đổi ý không chuyển khoản hoặc giao dịch ngân hàng gián đoạn, thu ngân bấm "HỦY GIAO DỊCH (ESC)", hệ thống dừng thanh toán an toàn, giữ nguyên toàn bộ giỏ hàng trên màn hình POS, và không ghi dữ liệu rác vào CSDL.
- **Điều kiện tiên quyết:**
  - Hộp thoại `VietQRDialog` đang mở cho đơn hàng 300,000 đ.
- **Các bước thực hiện:**
  1. Trên hộp thoại `VietQRDialog`, nhấn nút `"HỦY GIAO DỊCH (ESC)"` hoặc gõ phím `Escape`.
  2. Quan sát hộp thoại đóng lại và màn hình POS.
- **Kết quả mong đợi:**
  - Hộp thoại đóng lại, trả về `false`.
  - Màn hình POS giữ nguyên vẹn toàn bộ sản phẩm trong giỏ hàng, thông tin khách hàng, số tiền cần trả.
  - Thu ngân có thể chuyển sang phương thức "Tiền mặt" hoặc thêm bớt món mà không phải nhập lại từ đầu.
  - CSDL SQL Server không có bất kỳ hóa đơn nào được tạo.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Hủy giao dịch an toàn, giỏ hàng bảo toàn 100%, không sinh hóa đơn rác trong CSDL.
  - Không đạt: Giỏ hàng bị xóa trắng hoặc CSDL vẫn ghi nhận hóa đơn.

---

#### TC-POS-009: Thanh toán Chuyển khoản VietQR - Kiểm tra cơ chế Offline Fallback Canvas
- **Phân hệ & Component:** POS & Bán Hàng | `gui.VietQRDialog` (`taoAnhQRFallback`, dòng 360-420)
- **Cấp độ kiểm thử:** Integration / Fallback & Resilience Test
- **Mục tiêu kiểm thử:** Xác minh khi máy tính thu ngân bị ngắt kết nối Internet (mất mạng LAN/Wifi), hệ thống tự động phát hiện lỗi HTTP và chuyển sang vẽ vector đồ họa nội bộ (Offline Mock QR) để giao diện không bị treo đơ, vẫn hiển thị đầy đủ STK và Cú pháp cho khách chuyển khoản thủ công.
- **Điều kiện tiên quyết:**
  - Ngắt kết nối mạng Internet hoặc giả lập URL API không phản hồi.
- **Các bước thực hiện:**
  1. Thêm sản phẩm vào giỏ, chọn "Chuyển khoản", bấm "THANH TOÁN (F9)".
  2. Quan sát quá trình tải mã QR trong `VietQRDialog`.
- **Kết quả mong đợi:**
  - Sau timeout ngắn, dialog không bị crash. Nhãn trạng thái hiển thị: `Chế độ Offline: Quét mã hoặc chuyển khoản thủ công theo thông tin bên dưới`.
  - Khung ảnh hiển thị hình ảnh QR mô phỏng nội bộ kèm biểu tượng ngân hàng.
  - Khách hàng có thể nhìn số tài khoản và nội dung hiển thị bên dưới để chuyển khoản bằng App ngân hàng.
  - Thu ngân vẫn có thể bấm nút "Xác nhận đã nhận tiền" sau khi kiểm tra biến động số dư trên điện thoại.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Xử lý ngoại lệ mạng mượt mà, fallback hoạt động ổn định.
  - Không đạt: Chương trình bị treo (freeze UI) hoặc ném lỗi đỏ ra màn hình người dùng.

---

### 3.3 Nhóm Kiểm Thử Thao Tác Giỏ Hàng & Phím Tắt POS (Cart & Shortcuts)

---

#### TC-POS-010: Thao tác Giỏ hàng - Thêm món, sửa số lượng trực tiếp, tăng/giảm và xóa dòng
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`tblGioHang`, `xuLySuaSoLuongTuBanPhim`, `thayDoiSoLuong`, `xoaMonDangChon`)
- **Cấp độ kiểm thử:** UI / Functional Test
- **Mục tiêu kiểm thử:** Xác minh toàn bộ các thao tác điều chỉnh giỏ hàng: nhấp đúp cột SL gõ số lượng, nút Tăng (+1), nút Giảm (-1, khi về 0 tự xóa dòng), nút Xóa món, và tự động kiểm tra chặn vượt tồn kho.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP002` (Pate Whiskas) có tồn kho khả dụng là 10 lon.
- **Các bước thực hiện & Dữ liệu kiểm thử:**
  1. *Thêm món:* Click vào thẻ sản phẩm `SP002` trên Catalog -> Giỏ hàng thêm 1 dòng, số lượng = 1, thành tiền = 20,000 đ.
  2. *Nút Tăng:* Bấm nút `[+ Tăng]` -> Số lượng tăng lên 2, thành tiền = 40,000 đ.
  3. *Nhập bàn phím hợp lệ:* Nhấp đúp vào cột SL (cột số 4), gõ số `5`, nhấn Enter -> Số lượng = 5, thành tiền = 100,000 đ.
  4. *Nhập vượt tồn kho:* Nhấp đúp cột SL, gõ số `15` (vượt quá tồn 10) -> Nhấn Enter.
     - Quan sát: Popup cảnh báo tồn kho xuất hiện, hệ thống tự động hạ số lượng về mức tồn tối đa `10`.
  5. *Nút Giảm:* Bấm nút `[- Giảm]` liên tục cho đến khi số lượng về 0.
     - Quan sát: Khi số lượng giảm từ 1 xuống 0, dòng sản phẩm tự động bị xóa khỏi giỏ hàng.
  6. *Nút Xóa:* Thêm lại món, bấm nút `[Xóa]` -> Dòng sản phẩm bị xóa ngay lập tức, danh số thứ tự (STT) được đánh lại từ 1.
- **Kết quả mong đợi:**
  - Mọi thao tác đều phản ánh tức thì lên `lblTongTienHang` và `lblKhachCanTra`.
  - Cột STT luôn liên tục không bị nhảy cóc số.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tính toán chính xác, tự động giới hạn tồn kho, cập nhật tổng tiền chuẩn xác.
  - Không đạt: Cho phép bán vượt tồn kho trên giỏ hàng, hoặc STT bị sai lệch.

---

#### TC-POS-011: Thao tác Phím tắt quầy thu ngân (F9, F1-F12, Enter, Esc)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (KeyBindings)
- **Cấp độ kiểm thử:** UI / Usability & Accessibility Test
- **Mục tiêu kiểm thử:** Xác minh các phím tắt quầy thu ngân hoạt động chuẩn xác giúp tăng tốc độ phục vụ khách hàng vào giờ cao điểm:
  - `F9`: Kích hoạt nút THANH TOÁN.
  - `F1`: Tạo mới hóa đơn.
  - `F2`: Lưu tạm hóa đơn.
  - `F3`: Xem trước / In hóa đơn K80.
  - `Enter`: Xác nhận tìm kiếm / Xác nhận dialog.
  - `Esc`: Đóng các dialog popup.
- **Điều kiện tiên quyết:**
  - Mở giao diện POS, giỏ hàng có món.
- **Các bước thực hiện:**
  1. Nhấn phím `F9` từ bàn phím (không dùng chuột).
  2. Quan sát hành vi hệ thống: Dialog xác nhận thanh toán hoặc VietQRDialog xuất hiện.
  3. Nhấn `Esc` để đóng dialog.
  4. Nhấn `F2` -> Đơn hàng được lưu tạm vào Tab 2.
- **Kết quả mong đợi:**
  - Hệ thống bắt phím tắt nhạy bén, không bị xung đột với các trường nhập liệu văn bản.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Toàn bộ phím tắt hoạt động đúng chức năng gán.
  - Không đạt: Phím tắt không có tác dụng.

---

#### TC-POS-012: Lưu tạm đơn hàng và khôi phục đơn chờ phục vụ
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`luuTamDonHang`, `capNhatGiaoDienDonCho`, `khoiPhucDonHangTam`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh tính năng lưu tạm giỏ hàng hiện tại khi khách hàng cần lựa chọn thêm đồ, cho phép thu ngân phục vụ khách tiếp theo và sau đó khôi phục lại nguyên vẹn giỏ hàng cũ chỉ bằng 1 chạm.
- **Điều kiện tiên quyết:**
  - Khách `Nguyễn Văn B` (0912345678) đang có giỏ hàng 2 món trị giá 280,000 đ.
- **Các bước thực hiện:**
  1. Nhấn nút `"Lưu tạm"` (hoặc `F2`).
  2. Quan sát thông báo: `ĐÃ LƯU TẠM ĐƠN HÀNG [HD...] THÀNH CÔNG!`.
  3. Màn hình POS tự động dọn sạch giỏ hàng, sẵn sàng bán đơn mới cho khách khác.
  4. Chuyển sang Tab 2: `"Hóa đơn chờ phục vụ"`.
  5. Thấy thẻ đơn hàng của khách `Nguyễn Văn B` hiển thị với đầy đủ thông tin: Mã HĐ, Tên khách, Số món, Số tiền `280,000 đ`, Thời gian tạo.
  6. Nhấn nút `"Khôi phục đơn"` trên thẻ đó.
- **Kết quả mong đợi:**
  - Hệ thống tự động chuyển về Tab 1 Bán hàng POS.
  - Toàn bộ 2 món hàng, thông tin khách hàng, số tiền cần trả được khôi phục chính xác 100%.
  - Thẻ đơn chờ tương ứng bị xóa khỏi danh sách Tab 2.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Lưu và khôi phục đầy đủ thông tin, không làm mất dữ liệu giỏ hàng.
  - Không đạt: Khôi phục thiếu món hoặc mất thông tin khách.

---

#### TC-POS-013: Chuyển đổi qua lại giữa Tiền mặt và Chuyển khoản trong giỏ hàng có món
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`cboPhuongThuc`, `txtTienKhachDua`, `lblTienThoiLai`)
- **Cấp độ kiểm thử:** UI / State Transition Test
- **Mục tiêu kiểm thử:** Xác minh khi giỏ hàng đã có sản phẩm, việc chuyển đổi qua lại giữa phương thức "Tiền mặt" và "Chuyển khoản" bảo toàn nguyên vẹn giỏ hàng, số tiền cần trả, đồng thời cập nhật đúng trạng thái của ô nhập tiền khách đưa (`txtTienKhachDua` enabled/disabled) và nhãn tiền thối lại (`lblTienThoiLai`).
- **Điều kiện tiên quyết:**
  - Giỏ hàng POS đang có sản phẩm với tổng tiền cần thanh toán là 300,000 đ.
- **Các bước thực hiện:**
  1. Mặc định `cboPhuongThuc` là "Tiền mặt", ô `txtTienKhachDua` đang mở, nhập `500000` -> `lblTienThoiLai` hiển thị `200,000 đ`.
  2. Đổi `cboPhuongThuc` sang "Chuyển khoản".
  3. Quan sát: `txtTienKhachDua` tự động điền `300,000 đ` và bị khóa (`setEnabled(false)`); `lblTienThoiLai` hiển thị `0 đ`. Giỏ hàng giữ nguyên các món hàng.
  4. Đổi lại `cboPhuongThuc` về "Tiền mặt".
  5. Quan sát: `txtTienKhachDua` được mở khóa lại (`setEnabled(true)`), cho phép sửa tiền khách đưa; `lblTienThoiLai` tính toán lại theo thời gian thực.
  6. Nhấn "THANH TOÁN (F9)" để hoàn tất.
- **Dữ liệu kiểm thử:** Cần trả: 300,000 đ; Chuỗi chuyển đổi: Tiền mặt -> Chuyển khoản -> Tiền mặt.
- **Kết quả mong đợi:** Giỏ hàng không bị xóa; trạng thái UI chuyển đổi chính xác; không phát sinh ngoại lệ runtime.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Trạng thái UI chuyển đổi chính xác, giỏ hàng bảo toàn 100%.
  - Không đạt: Giỏ hàng bị reset hoặc ô nhập tiền bị khóa sai trạng thái.
- **Verification SQL Queries:**
  ```sql
  SELECT TOP 1 maHoaDon, thanhTien, phuongThucThanhToan FROM HoaDon ORDER BY ngayLap DESC;
  ```

---

### 3.4 Nhóm Kiểm Thử Điểm Thưởng & Khách Hàng VIP (Loyalty & VIP Defect Verification)

---

#### TC-POS-014: Tích lũy điểm thưởng thành viên khi thanh toán hóa đơn mới
- **Phân hệ & Component:** POS & Bán Hàng | `bus.BanHangBus`, `dao.HoaDonDAO` (dòng 181-190), `dao.KhachHangDAO`
- **Cấp độ kiểm thử:** Integration / Business Logic Test
- **Mục tiêu kiểm thử:** Xác minh công thức tích điểm thành viên chuẩn của hệ thống: Cứ mỗi 100,000 đ thanh toán thực tế (`canTra`) được cộng 1 điểm thưởng vào tài khoản khách hàng trong CSDL SQL Server.
- **Điều kiện tiên quyết:**
  - Khách hàng `Trần Thị Mai` (SĐT `0977888999`) có điểm tích lũy ban đầu là `10` điểm.
- **Các bước thực hiện:**
  1. Tại ô `txtSdtKhachHang`, nhập `"0977888999"`, hệ thống tự động tìm thấy khách `Trần Thị Mai - 10 điểm (Thành viên Đồng)`.
  2. Thêm sản phẩm vào giỏ hàng với tổng tiền cần thanh toán là `350,000 đ`.
  3. Không sử dụng điểm giảm giá (`spnDiemDung = 0`).
  4. Thanh toán Tiền mặt với số tiền đưa `350,000 đ`.
  5. Sau khi thanh toán thành công, kiểm tra lại số điểm của khách trong CSDL SQL Server.
- **Dữ liệu kiểm thử:**
  - Điểm ban đầu: 10 điểm.
  - Tiền thanh toán: 350,000 đ -> Điểm thưởng mới = `350,000 / 100,000 = 3` điểm (phép chia nguyên).
  - Điểm mong đợi sau thanh toán: `10 + 3 = 13` điểm.
- **Kết quả mong đợi:**
  - Bảng `KhachHang` được cập nhật cột `diemTichLuy = 13`.
  - Hóa đơn in ra thể hiện: `Điểm hiện có: 13`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Điểm tích lũy trong CSDL tăng chính xác 3 điểm.
  - Không đạt: Điểm không tăng hoặc tăng sai tỷ lệ.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra điểm của khách hàng trước và sau giao dịch
  SELECT maKhachHang, hoTen, soDienThoai, diemTichLuy
  FROM KhachHang
  WHERE soDienThoai = '0977888999';
  ```

---

#### TC-POS-015: RÀ SOÁT LỖI DEF-02 / DEF-CRI-02 - Dùng điểm tích lũy nhưng CSDL không khấu trừ điểm
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (dòng 2771-2785), `bus.BanHangBus`, `dao.HoaDonDAO` (dòng 181-190)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-02 / DEF-CRI-02 - CRITICAL DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng và tái hiện lỗi chí mạng DEF-02 / DEF-CRI-02: Khách hàng sử dụng điểm tích lũy để giảm tiền trên hóa đơn (1 điểm = 1,000 đ), số tiền phải trả giảm đi thật, nhưng câu lệnh UPDATE trong CSDL không hề trừ điểm đã dùng mà chỉ cộng thêm điểm thưởng mới!
- **Điều kiện tiên quyết:**
  - Khách hàng `Lê Hoàng Long` (SĐT `0988111222`) đang có sẵn `50` điểm tích lũy trong bảng `KhachHang`.
- **Các bước thực hiện:**
  1. Nhập SĐT `0988111222` trên màn hình POS. Hệ thống hiển thị: `Khách cũ: 50 điểm`.
  2. Thêm sản phẩm vào giỏ hàng với tổng tiền hàng `tongTienHang = 200,000 đ`.
  3. Tại ô `Dùng điểm (spnDiemDung)`, chọn dùng `20` điểm.
  4. Quan sát: Hệ thống giảm trừ `20,000 đ` (`20 * 1,000 đ`), tổng tiền cần thanh toán giảm còn `180,000 đ`.
  5. Chọn phương thức "Tiền mặt", nhập tiền khách đưa `180,000 đ`.
  6. Bấm "THANH TOÁN (F9)" và xác nhận hoàn tất giao dịch.
  7. Truy vấn trực tiếp CSDL SQL Server để kiểm tra `diemTichLuy` của khách hàng `0988111222`.
- **Dữ liệu kiểm thử & Phân tích logic:**
  - Điểm ban đầu: 50 điểm.
  - Điểm dùng để trừ tiền: 20 điểm.
  - Tiền khách trả: 180,000 đ -> Điểm thưởng mới: `180,000 / 100,000 = 1` điểm.
  - **KẾT QUẢ KỲ VỌNG CHUẨN:** Điểm mới = `50 - 20 + 1 = 31` điểm.
  - **KẾT QUẢ THỰC TẾ TRONG CODE (LỖI DEF-02):**
    * Dòng 2774 tính `tongDiemMoi = 31` nhưng bỏ quên, không truyền vào DAO.
    * Dòng 2780 truyền `diemHienCo` (50 điểm).
    * `HoaDonDAO` dòng 185 thực thi: `UPDATE KhachHang SET diemTichLuy = diemTichLuy + 1`.
    * Điểm thực tế trong CSDL trở thành: `50 + 1 = 51` điểm!
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Kết quả truy vấn SQL trả về `diemTichLuy = 51`.
  - **XÁC NHẬN LỖI DEF-02 TỒN TẠI 100%:** Điểm dùng không bị trừ, khách hàng có thể dùng đi dùng lại 50 điểm này vô hạn lần để bòn rút tiền của cửa hàng.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)** nhằm xác nhận lỗi kỹ thuật để chuyển giao đội ngũ phát triển sửa lỗi.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra điểm thực tế trong CSDL sau khi thanh toán đơn dùng 20 điểm
  SELECT maKhachHang, hoTen, soDienThoai, diemTichLuy
  FROM KhachHang
  WHERE soDienThoai = '0988111222';
  -- Nếu diemTichLuy = 51 -> DEFECT DEF-02 CONFIRMED!
  -- Nếu diemTichLuy = 31 -> Đã sửa lỗi thành công.
  ```

---

#### TC-POS-016: Giảm giá tự động 5% cho khách hàng VIP (`diemHienCo >= 100`)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (dòng 2294-2296), `bus.BanHangBus` (`tinhGiamGiaVIP`)
- **Cấp độ kiểm thử:** Unit / Integration Test
- **Mục tiêu kiểm thử:** Xác minh chính sách ưu đãi khách hàng VIP: Khách có điểm tích lũy từ 100 điểm trở lên được tự động chiết khấu 5% trên tổng tiền hàng. Kiểm tra các giá trị biên 99 điểm (không giảm) và 100 điểm (bắt đầu giảm).
- **Điều kiện tiên quyết:**
  - Khách A có 99 điểm (Thành viên Bạc).
  - Khách B có 100 điểm (Thành viên Vàng - VIP).
- **Các bước thực hiện:**
  1. *Test Biên 99 điểm:* Nhập SĐT Khách A -> Thêm món 1,000,000 đ -> Quan sát ô `lblGiamGia`.
     - Kết quả: `lblGiamGia = 0 đ`, tổng trả = 1,000,000 đ.
  2. *Test Biên 100 điểm:* Nhập SĐT Khách B -> Thêm món 1,000,000 đ -> Quan sát ô `lblGiamGia`.
     - Kết quả: `lblGiamGia = -50,000 đ` (5% của 1,000,000 đ), tổng trả = 950,000 đ.
  3. Thanh toán hóa đơn cho Khách B -> Hóa đơn lưu vào CSDL với `giamGia = 50000.00`, `thanhTien = 950000.00`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Đúng chính sách chiết khấu 5% tại mốc 100 điểm, lưu CSDL chính xác.
  - Không đạt: 99 điểm bị giảm nhầm, hoặc 100 điểm không được giảm.

---

### 3.5 Nhóm Kiểm Thử Đồng Thời & Tràn Số Mã Hóa Đơn (Concurrency & Rollover Verification)

---

#### TC-POS-017: RÀ SOÁT LỖI DEF-04 / DEF-BLK-01 - Tràn số `fn_SinhMaHoaDon` khi đạt 100 hóa đơn trong ngày
- **Phân hệ & Component:** Cơ Sở Dữ Liệu & POS | `sql/HeThongQuanLyCuaHangThuCung.sql` (dòng 618-629), `dao.HoaDonDAO`
- **Cấp độ kiểm thử:** Unit / Database Defect Verification (**DEF-04 / DEF-BLK-01 - BLOCKER DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng lỗi tràn số của hàm UDF T-SQL `fn_SinhMaHoaDon`. Do định dạng cắt 2 chữ số (`RIGHT('00' + CAST(@max + 1 AS VARCHAR(10)), 2)`), khi số lượng hóa đơn trong ngày đạt từ 100 trở lên, hàm bị quay vòng về số 00, gây lỗi vi phạm khóa chính trùng lặp `PRIMARY KEY Violation`.
- **Điều kiện tiên quyết:**
  - Kết nối tới CSDL `QuanLyCuaHangThuCung`.
- **Các bước thực hiện:**
  1. Kiểm tra mã nguồn T-SQL của hàm `fn_SinhMaHoaDon`:
     ```sql
     SELECT OBJECT_DEFINITION(OBJECT_ID('dbo.fn_SinhMaHoaDon'));
     ```
  2. Chèn dữ liệu giả lập hóa đơn thứ 99 trong ngày hôm nay:
     ```sql
     DECLARE @todayPrefix VARCHAR(10) = 'HD' + CONVERT(VARCHAR(6), GETDATE(), 12) + '-';
     INSERT INTO HoaDon (maHoaDon, maNhanVien, ngayLap, thanhTien, trangThaiHoaDon)
     VALUES (@todayPrefix + '99', 'NV001', GETDATE(), 100000, N'Đã thanh toán');
     ```
  3. Gọi hàm `fn_SinhMaHoaDon` để sinh mã cho hóa đơn thứ 100:
     ```sql
     SELECT dbo.fn_SinhMaHoaDon('NV001') AS MaHoaDonThu100;
     ```
  4. Tiếp tục gọi hàm sinh mã cho hóa đơn thứ 101:
     ```sql
     SELECT dbo.fn_SinhMaHoaDon('NV001') AS MaHoaDonThu101;
     ```
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Tại Bước 3: `@max = 99`, `@max + 1 = 100`. Hàm `RIGHT('00100', 2)` trả về `'00'`. Mã sinh ra là `HD{yyMMdd}-00`.
  - Tại Bước 4: UDF tìm MAX của chuỗi số 2 ký tự thì `'99'` vẫn lớn hơn `'00'`, nên `@max` vẫn là 99. Kết quả tiếp tục trả về `HD{yyMMdd}-00`!
  - Khi cố tình INSERT hóa đơn thứ 101, SQL Server ném lỗi:
    `Violation of PRIMARY KEY constraint 'PK__HoaDon'. Cannot insert duplicate key in object 'dbo.HoaDon'. The duplicate key value is (HD{yyMMdd}-00).`
  - **XÁC NHẬN LỖI DEF-04 TỒN TẠI 100%:** Toàn bộ quầy POS sẽ bị sụp đổ chức năng thanh toán khi cửa hàng bán được từ hóa đơn thứ 100 trở đi trong ngày.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra lỗi sinh mã trùng lặp
  SELECT dbo.fn_SinhMaHoaDon('NV001');
  -- Xóa bản ghi giả lập sau kiểm thử để làm sạch CSDL
  DELETE FROM HoaDon WHERE maHoaDon LIKE 'HD%' AND maHoaDon LIKE '%-99';
  ```

---

#### TC-POS-018: RÀ SOÁT LỖI DEF-09 / DEF-MAJ-05 - Xung đột mã hóa đơn khi 2 quầy thu ngân cùng mở bán
- **Phân hệ & Component:** POS & Bán Hàng | `sql/HeThongQuanLyCuaHangThuCung.sql`, `gui.BanHangPOSPanel` (dòng 2372, 2724, 2794)
- **Cấp độ kiểm thử:** Integration / Concurrency Race Condition Test (**DEF-09 / DEF-MAJ-05 - MAJOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng hiện tượng race condition khi 2 thu ngân tại 2 máy POS khác nhau cùng mở ca bán hàng. Mã hóa đơn được sinh ngay khi mở màn hình thay vì sinh ngay thời điểm COMMIT thanh toán, dẫn đến việc cả 2 quầy cùng giữ chung một mã hóa đơn.
- **Điều kiện tiên quyết:**
  - Hai phiên làm việc Swing của ứng dụng cùng chạy song song (Máy A và Máy B).
- **Các bước thực hiện:**
  1. Máy A mở tab Bán hàng POS -> Nhận mã hiển thị trên `lblMaHoaDon` là `HD261107-05`.
  2. Máy B mở tab Bán hàng POS ngay sau đó (khi Máy A chưa bấm thanh toán) -> Cũng nhận mã `HD261107-05`.
  3. Máy A thêm hàng, bấm THANH TOÁN thành công -> Hóa đơn `HD261107-05` được ghi vào CSDL.
  4. Máy B sau đó bấm THANH TOÁN đơn hàng của mình.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Máy B bị ném ngoại lệ `SQLException: Violation of PRIMARY KEY constraint 'PK__HoaDon'`.
  - Màn hình Máy B hiện popup đỏ: `KHÔNG THỂ LƯU HÓA ĐƠN VÀO CƠ SỞ DỮ LIỆU SQL SERVER!`.
  - Thu ngân tại Máy B bị kẹt giỏ hàng và phải hủy đơn tạo lại.
  - **XÁC NHẬN LỖI DEF-09 TỒN TẠI 100%:** Kiến trúc sinh mã hóa đơn trước khi thanh toán không an toàn cho môi trường đa quầy thu ngân.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

#### TC-POS-019: Chặn thanh toán giỏ hàng rỗng và cảnh báo dịch vụ spa/hotel chưa hoàn thành
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`btnThanhToan`, `tblGioHang`, `modelGioHang`, `thucHienThanhToan`)
- **Cấp độ kiểm thử:** UI / Negative & Business Workflow Validation Test
- **Mục tiêu kiểm thử:**
  1. **Chặn thanh toán khi giỏ hàng rỗng (Empty Cart Validation):** Đảm bảo hệ thống kiểm tra điều kiện `modelGioHang.getRowCount() == 0`. Nếu thu ngân bấm "THANH TOÁN (F9)" hoặc kích hoạt phím nóng `F9` khi chưa chọn bất kỳ sản phẩm hoặc dịch vụ nào, hệ thống phải lập tức chặn lại và hiển thị thông báo nhắc nhở, tuyệt đối không được phát sinh hóa đơn rỗng (`0 đ`) vào CSDL.
  2. **Cảnh báo dịch vụ Spa/Pet Hotel chưa hoàn thành (`conDichVuChuaXong`):** Khi giỏ hàng có dịch vụ Spa hoặc dịch vụ lưu trú Pet Hotel mà trạng thái dịch vụ/lịch hẹn chưa chuyển sang `"Đã hoàn thành"` (`conDichVuChuaXong == true`), hệ thống phải hiển thị hộp thoại cảnh báo (Confirmation / Warning Dialog) để thu ngân xác nhận xem có thanh toán sớm trước khi dịch vụ kết thúc hay không, ngăn ngừa sai lệch trạng thái vận hành.
- **Điều kiện tiên quyết:**
  - Thu ngân `NV001` đang mở phiên bán hàng tại màn hình `BanHangPOSPanel`.
- **Các bước thực hiện:**
  - **Kịch bản 1: Giỏ hàng rỗng (`modelGioHang.getRowCount() == 0`)**
    1. Đảm bảo bảng giỏ hàng `tblGioHang` hoàn toàn trống (chưa thêm món).
    2. Bấm nút "THANH TOÁN (F9)" (hoặc ấn phím tắt `F9`).
    3. Quan sát phản ứng của giao diện và thông báo popup.
  - **Kịch bản 2: Đơn hàng chứa dịch vụ chưa hoàn thành (`conDichVuChuaXong == true`)**
    1. Thêm một dịch vụ Spa `DV001` (Cắt tỉa lông) từ lịch hẹn đang phục vụ (trạng thái: `"Đang thực hiện"`).
    2. Bấm nút "THANH TOÁN (F9)".
    3. Quan sát hộp thoại cảnh báo `JOptionPane.showConfirmDialog`.
- **Dữ liệu kiểm thử:**
  - Kịch bản 1: `modelGioHang.getRowCount() == 0`.
  - Kịch bản 2: `modelGioHang.getRowCount() > 0`, cờ `conDichVuChuaXong = true` (tồn tại dòng dịch vụ có trạng thái khác `"Đã hoàn thành"`).
- **Kết quả mong đợi:**
  - **Kịch bản 1:** Hệ thống hiển thị hộp thoại cảnh báo: `"Giỏ hàng đang trống! Vui lòng chọn sản phẩm hoặc dịch vụ trước khi thanh toán."`. Luồng thanh toán bị hủy ngay lập tức; không mở `VietQRDialog`, không sinh hóa đơn trong CSDL SQL Server.
  - **Kịch bản 2:** Hệ thống hiển thị hộp thoại cảnh báo xác nhận: `"Cảnh báo: Có dịch vụ Spa/Hotel trong giỏ hàng chưa hoàn thành phục vụ! Bạn có chắc chắn muốn thanh toán trước không?"`. Nếu bấm "Hủy / Không", luồng thanh toán dừng lại để KTV hoàn tất dịch vụ. Nếu bấm "Đồng ý", hệ thống mới tiếp tục luồng thanh toán.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - *Đạt:* Xử lý validation giỏ hàng rỗng nghiêm ngặt, chặn đứng việc lưu hóa đơn rác 0 đ và có cơ chế cảnh báo nghiệp vụ khi dịch vụ chưa hoàn thành.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra không có bất kỳ hóa đơn rác nào có tổng tiền = 0 hoặc không có chi tiết
  SELECT h.maHoaDon, h.ngayLap, h.thanhTien, COUNT(ct.maChiTiet) AS SoLuongDongHang
  FROM HoaDon h
  LEFT JOIN ChiTietHoaDon ct ON h.maHoaDon = ct.maHoaDon
  WHERE h.thanhTien <= 0
  GROUP BY h.maHoaDon, h.ngayLap, h.thanhTien
  HAVING COUNT(ct.maChiTiet) = 0;
  -- Kỳ vọng: 0 bản ghi vi phạm.
  ```

---

### 3.6 Nhóm Kiểm Thử Đổi Trả Hàng Hóa - Kịch Bản A & B (Returns & Exchange Scenarios)

---

#### TC-DT-001: Tra cứu hóa đơn cũ trong hạn 7 ngày tại màn hình Đổi trả
- **Phân hệ & Component:** Đổi Trả Hàng | `gui.DoiTraHangPanel` (`txtTraCuu`, `napHoaDonLenGiaoDien`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh tính năng tra cứu hóa đơn đã thanh toán theo mã hóa đơn hoặc số điện thoại khách hàng trong vòng 7 ngày; hiển thị đầy đủ thông tin hóa đơn và nạp danh sách sản phẩm lên bảng `tblSanPham`.
- **Điều kiện tiên quyết:**
  - Hóa đơn `HD261105-01` mua cách đây 2 ngày chứa 2 sản phẩm: `SP001` (SL: 2) và `SP002` (SL: 5).
- **Các bước thực hiện:**
  1. Mở màn hình Đổi trả hàng (`DoiTraHangPanel`).
  2. Nhập mã `"HD261105-01"` vào ô tìm kiếm `txtTraCuu`, nhấn Enter hoặc nút "Tra cứu".
  3. Quan sát thông tin tại thẻ `pnlInfoCard` và bảng `tblSanPham`.
- **Kết quả mong đợi:**
  - Thẻ thông tin hiển thị chính xác: Mã HĐ, Tên khách, SĐT, Thu ngân lập, Thời gian mua, Tổng tiền.
  - Bảng sản phẩm hiển thị đủ 2 dòng sản phẩm với cột "Đã mua" tương ứng (2 và 5).
  - Cột "Chọn" mặc định bỏ tick, cột "SL Trả" mặc định bằng 1, các cột Lý do và Tình trạng hàng có sẵn combobox lựa chọn.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Dữ liệu tải nhanh, đầy đủ, chính xác.
  - Không đạt: Không tìm thấy hóa đơn hoặc hiển thị sai sản phẩm.

---

#### TC-DT-002: Tra cứu hóa đơn quá hạn 7 ngày - Hiển thị cảnh báo và xác nhận ngoại lệ
- **Phân hệ & Component:** Đổi Trả Hàng | `gui.DoiTraHangPanel` (dòng 724-742)
- **Cấp độ kiểm thử:** UI / Business Rule Validation Test
- **Mục tiêu kiểm thử:** Xác minh chính sách đổi trả trong vòng 7 ngày: Nếu hóa đơn mua quá 7 ngày kể từ ngày thanh toán, hệ thống hiển thị cảnh báo quá hạn rõ ràng, yêu cầu xác nhận ngoại lệ Quản lý mới cho phép tiếp tục.
- **Điều kiện tiên quyết:**
  - Hóa đơn `HD261020-01` mua từ ngày 20/10/2026 (đã quá 18 ngày).
- **Các bước thực hiện:**
  1. Tra cứu hóa đơn `HD261020-01`.
  2. Tích chọn 1 sản phẩm cần trả.
  3. Bấm nút `"XÁC NHẬN IN PHIẾU ĐỔI TRẢ"`.
- **Kết quả mong đợi:**
  - Hệ thống hiển thị `JOptionPane.showConfirmDialog` với tiêu đề `"Cảnh báo quá hạn 7 ngày"` và nội dung:
    ```
    ⚠️ CẢNH BÁO QUÁ HẠN ĐỔI TRẢ (QUY ĐỊNH 07 NGÀY):

    • Hóa đơn [HD261020-01] mua lúc: ... (Đã qua 18 ngày).
    • Theo quy định cửa hàng, hóa đơn chỉ được đổi/trả trong vòng 07 ngày.

    Bạn có xác nhận ngoại lệ từ Quản lý cửa hàng để tiếp tục xử lý không?
    ```
  - Nếu bấm "Không" (NO): Quy trình dừng lại ngay lập tức.
  - Nếu bấm "Có" (YES): Cho phép xử lý tiếp theo quyền ngoại lệ của Quản lý.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tính toán số ngày chuẩn xác bằng `ChronoUnit.DAYS`, hiển thị đúng dialog cảnh báo.
  - Không đạt: Cho phép đổi trả tự do mà không cảnh báo.

---

#### TC-DT-003: Đổi trả Kịch bản A - Trả hàng hoàn tiền K80 trực tiếp tại quầy thu ngân
- **Phân hệ & Component:** Đổi Trả Hàng | `gui.DoiTraHangPanel` (Kịch bản A, dòng 803-870), `dao.HoaDonDAO` (`luuPhieuDoiTra`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh Kịch bản A (Khách trả lại hàng và nhận lại tiền mặt tại quầy): Tạo phiếu đổi trả trong CSDL, khấu trừ điểm tích lũy tương ứng của khách hàng, in phiếu đổi trả K80.
- **Điều kiện tiên quyết:**
  - Khách hàng trả lại 1 gói `SP001` trị giá 150,000 đ từ hóa đơn cũ.
  - Khách hàng ban đầu có 20 điểm tích lũy.
- **Các bước thực hiện:**
  1. Tra cứu hóa đơn cũ.
  2. Tích chọn sản phẩm `SP001`, nhập `SL Trả = 1`.
  3. Chọn Lý do: `"Sản phẩm lỗi/hỏng"`, Tình trạng: `"Hàng lỗi/rách (chờ hủy)"`.
  4. Chọn radio button: `"Kịch bản A: Trả hàng - Hoàn tiền"`.
  5. Quan sát các nhãn kết quả tính toán:
     - `lblKetQuaHoanTien`: `150,000 đ`.
     - `lblKetQuaDiemVIP`: `-1 điểm` (150,000 / 100,000).
  6. Bấm nút `"XÁC NHẬN IN PHIẾU ĐỔI TRẢ"`.
- **Kết quả mong đợi:**
  - Phiếu đổi trả được lưu vào bảng `PhieuDoiTra` với mã dạng `DT{yyMMdd}-xx`, `tongTienHoan = 150000.00`, `trangThaiDoiTra = N'Hoàn thành'`.
  - Bảng `ChiTietDoiTra` lưu mã sản phẩm `SP001`, số lượng trả 1, tiền hoàn 150,000 đ.
  - Điểm tích lũy của khách hàng trong bảng `KhachHang` bị trừ 1 điểm (còn 19 điểm).
  - Xuất hiện cửa sổ xem trước phiếu in nhiệt 80mm với đầy đủ thông tin trả hàng.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tạo phiếu đổi trả CSDL thành công, trừ điểm tích lũy đúng, hoàn tiền đúng 150k.
  - Không đạt: Không lưu được phiếu hoặc tính sai tiền hoàn.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra phiếu đổi trả vừa tạo
  SELECT TOP 1 maPhieuDoiTra, maHoaDonGoc, maNhanVienTiepNhan, tongTienHoan, lyDoDoiTra, trangThaiDoiTra
  FROM PhieuDoiTra ORDER BY ngayDoiTra DESC;

  -- Kiểm tra chi tiết phiếu đổi trả
  SELECT maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai
  FROM ChiTietDoiTra WHERE maPhieuDoiTra = (SELECT TOP 1 maPhieuDoiTra FROM PhieuDoiTra ORDER BY ngayDoiTra DESC);

  -- Kiểm tra điểm khách hàng
  SELECT maKhachHang, diemTichLuy FROM KhachHang WHERE soDienThoai = '0988111222';
  ```

---

#### TC-DT-004: Đổi trả Kịch bản B - Đổi sang sản phẩm ngang giá (`chenhLech = 0`) sang POS
- **Phân hệ & Component:** Đổi Trả & POS | `gui.DoiTraHangPanel` -> `gui.BanHangPOSPanel` (`tiepNhanDoiTraHang`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh Kịch bản B khi đổi sang sản phẩm khác có cùng giá trị (`chenhLech = 0`): Hệ thống đóng gói `DoiTraTransferData`, tự động chuyển màn hình sang POS, hiển thị thanh thông báo khấu trừ, giỏ hàng có món mới, tổng cần thanh toán = 0 đ, hoàn tất hóa đơn.
- **Điều kiện tiên quyết:**
  - Khách trả 1 gói hạt `SP001` (150,000 đ), muốn đổi sang 1 khay cát vệ sinh `SP003` (150,000 đ).
- **Các bước thực hiện:**
  1. Tại `DoiTraHangPanel`, chọn trả `SP001` (SL: 1, giá 150,000 đ).
  2. Chọn radio button: `"Kịch bản B: Đổi sang sản phẩm khác"`.
  3. Tại combobox `cboSanPhamMoi`, chọn `SP003 - Cát vệ sinh mèo (150,000 đ)`, `spnSoLuongMoi = 1`.
  4. Quan sát nhãn `lblKetQuaChenhLech` hiển thị: `0 đ (Đổi ngang giá)`.
  5. Bấm `"XÁC NHẬN IN PHIẾU ĐỔI TRẢ"`.
  6. Quan sát màn hình tự động chuyển sang `BanHangPOSPanel`.
- **Kết quả mong đợi:**
  - Màn hình chuyển sang tab Bán hàng POS.
  - Xuất hiện thanh banner màu vàng trên giỏ hàng:
    `🔄 ĐỔI TRẢ TỪ HÓA ĐƠN [HD...] | KHẤU TRỪ: -150,000 đ`.
  - Giỏ hàng POS có đúng 1 dòng sản phẩm `SP003` (SL: 1, Đơn giá: 150,000 đ).
  - Mục Tạm tính: `150,000 đ`. Mục Giảm VIP/Điểm: `-150,000 đ (Đổi: -150,000 đ)`.
  - Mục TỔNG THANH TOÁN (`lblKhachCanTra`): `0 đ`.
  - Thu ngân bấm "THANH TOÁN (F9)" -> Hóa đơn hoàn tất thành công với `thanhTien = 0.00`, ghi nhận phiếu đổi trả và hóa đơn mới vào CSDL.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Điều hướng mượt sang POS, khấu trừ 100%, khách không phải trả thêm tiền.
  - Không đạt: Không chuyển sang POS hoặc tính tiền sai lệch.
- **Verification SQL Queries:**
  ```sql
  -- 1. Kiểm tra phiếu đổi trả ghi nhận thông tin đổi hàng
  SELECT TOP 1 maPhieuDoiTra, maHoaDonGoc, tongTienHoan, lyDoDoiTra, trangThaiDoiTra
  FROM PhieuDoiTra ORDER BY ngayDoiTra DESC;

  -- 2. Kiểm tra chi tiết phiếu đổi trả ghi nhận sản phẩm trả lại (SP001)
  SELECT maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai
  FROM ChiTietDoiTra WHERE maPhieuDoiTra = (SELECT TOP 1 maPhieuDoiTra FROM PhieuDoiTra ORDER BY ngayDoiTra DESC);

  -- 3. Kiểm tra hóa đơn mới tại POS ghi nhận đổi ngang giá (thanhTien = 0.00)
  SELECT TOP 1 maHoaDon, tongTien, giamGia, thanhTien, phuongThucThanhToan
  FROM HoaDon ORDER BY ngayLap DESC;
  -- tongTien = 150000.00, giamGia = 150000.00, thanhTien = 0.00
  ```

---

#### TC-DT-005: Đổi trả Kịch bản B - Khách lấy thêm hàng bù tiền (`chenhLech > 0`) sang POS
- **Phân hệ & Component:** Đổi Trả & POS | `gui.DoiTraHangPanel` -> `gui.BanHangPOSPanel` (`tiepNhanDoiTraHang`, `thucHienThanhToan`)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh nghiệp vụ khách đổi hàng kèm mua thêm sản phẩm phát sinh bù tiền (`chenhLech > 0`): Dữ liệu chuyển sang POS, thu ngân chọn thêm hàng trên Catalog, thanh toán phần tiền chênh lệch bằng Tiền mặt hoặc Chuyển khoản VietQR.
- **Điều kiện tiên quyết:**
  - Khách trả 1 món trị giá 100,000 đ.
  - Khách đổi sang món mới trị giá 160,000 đ và nhặt thêm 1 món đồ chơi 50,000 đ tại quầy POS.
- **Các bước thực hiện:**
  1. Tại `DoiTraHangPanel`, chọn trả món 100,000 đ, chọn đổi món mới 160,000 đ -> Bấm xác nhận.
  2. Màn hình chuyển sang POS, giỏ hàng có món mới 160,000 đ, khấu trừ -100,000 đ. Cần trả hiện tại = 60,000 đ.
  3. Thu ngân click chọn thêm món đồ chơi `50,000 đ` từ lưới sản phẩm Catalog.
  4. Quan sát giỏ hàng cập nhật: Tổng tiền hàng = 210,000 đ (`160k + 50k`).
  5. Khấu trừ đổi trả = -100,000 đ.
  6. Tổng thanh toán cần thu: `110,000 đ` (`210k - 100k`).
  7. Chọn phương thức "Tiền mặt", nhập tiền khách đưa `200,000 đ` -> Tiền thối = `90,000 đ`.
  8. Bấm "THANH TOÁN (F9)".
- **Kết quả mong đợi:**
  - CSDL ghi nhận `PhieuDoiTra` với `tongTienHoan = 0.00` (vì đổi bù tiền).
  - CSDL ghi nhận `HoaDon` mới với `giamGia = 100000.00`, `thanhTien = 110000.00`.
  - Tồn kho của món mới và món đồ chơi được trừ đúng theo FEFO.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Dữ liệu chuyển tiếp trơn tru, cộng thêm món vào giỏ hàng bình thường, tính tiền chênh lệch chính xác 100%.
  - Không đạt: POS không cho thêm món mới hoặc tính sai tiền bù.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra hóa đơn bù tiền tại POS
  SELECT TOP 1 maHoaDon, giamGia, thanhTien, phuongThucThanhToan
  FROM HoaDon ORDER BY ngayLap DESC;
  -- giamGia = 100000.00, thanhTien = 110000.00
  ```

---

#### TC-DT-006: Đổi trả Kịch bản B - Đổi sang món rẻ hơn (`chenhLech < 0`, hoàn thừa)
- **Phân hệ & Component:** Đổi Trả & POS | `gui.DoiTraHangPanel` -> `gui.BanHangPOSPanel` (dòng 2711, 2730-2731, 2903)
- **Cấp độ kiểm thử:** UI / Integration Test
- **Mục tiêu kiểm thử:** Xác minh trường hợp khách đổi sang món rẻ hơn giá trị hàng trả lại: Màn hình POS tự động tính số tiền cửa hàng phải thối/hoàn thừa lại cho khách, không bắt khách trả thêm tiền.
- **Điều kiện tiên quyết:**
  - Khách trả sản phẩm trị giá 250,000 đ.
  - Khách đổi sang sản phẩm mới trị giá 180,000 đ (thừa 70,000 đ).
- **Các bước thực hiện:**
  1. Tại `DoiTraHangPanel`, chọn trả món 250,000 đ, đổi sang món mới 180,000 đ.
  2. Bấm xác nhận chuyển sang POS.
  3. Tại `BanHangPOSPanel`, quan sát các chỉ số thanh toán.
  4. Bấm "THANH TOÁN (F9)".
- **Kết quả mong đợi:**
  - `lblTongTienHang`: `180,000 đ`.
  - `lblKhachCanTra`: `0 đ`.
  - Nhãn `lblTienThoiLai` hiển thị: `"Hoàn thừa: 70,000 đ"` màu xanh lá.
  - CSDL ghi nhận `PhieuDoiTra` với `tongTienHoan = 70000.00`.
  - Hóa đơn mới ghi nhận `thanhTien = 0.00`, `giamGia = 180000.00`.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Tính đúng tiền hoàn thừa 70,000 đ, hoàn tất hóa đơn 0 đ mượt mà.
  - Không đạt: Bắt khách đưa thêm tiền hoặc tính sai tiền hoàn thừa.
- **Verification SQL Queries:**
  ```sql
  -- 1. Kiểm tra phiếu đổi trả ghi nhận số tiền hoàn thừa cho khách
  SELECT TOP 1 maPhieuDoiTra, maHoaDonGoc, tongTienHoan, lyDoDoiTra, trangThaiDoiTra
  FROM PhieuDoiTra ORDER BY ngayDoiTra DESC;
  -- tongTienHoan = 70000.00

  -- 2. Kiểm tra chi tiết phiếu đổi trả ghi nhận sản phẩm trả
  SELECT maChiTietDoiTra, maPhieuDoiTra, maSanPham, soLuongDoiTra, tienHoanLai
  FROM ChiTietDoiTra WHERE maPhieuDoiTra = (SELECT TOP 1 maPhieuDoiTra FROM PhieuDoiTra ORDER BY ngayDoiTra DESC);

  -- 3. Kiểm tra hóa đơn mới tại POS ghi nhận đổi sang món rẻ hơn (thanhTien = 0.00)
  SELECT TOP 1 maHoaDon, tongTien, giamGia, thanhTien, phuongThucThanhToan
  FROM HoaDon ORDER BY ngayLap DESC;
  -- tongTien = 180000.00, giamGia = 180000.00, thanhTien = 0.00
  ```

---

#### TC-DT-007: Hủy bỏ tiếp nhận đổi trả tại quầy POS (`btnHuyDoiTra`)
- **Phân hệ & Component:** POS & Bán Hàng | `gui.BanHangPOSPanel` (`huyDoiTraHang`, dòng 1884-1895)
- **Cấp độ kiểm thử:** UI / Alternative Flow Test
- **Mục tiêu kiểm thử:** Xác minh khi thu ngân hoặc khách hàng không muốn tiếp tục giao dịch đổi trả tại POS, bấm nút "Hủy đổi trả" trên thanh banner màu vàng thì hệ thống hủy bỏ phiên đổi trả, ẩn banner, và làm sạch giỏ hàng trở về trạng thái bán hàng thông thường.
- **Điều kiện tiên quyết:**
  - Màn hình POS đang hiển thị dữ liệu tiếp nhận đổi trả từ `DoiTraHangPanel`.
- **Các bước thực hiện:**
  1. Nhấn nút `"Hủy đổi trả"` trên thanh banner màu vàng (`btnHuyDoiTra`).
  2. Tại popup xác nhận: `Bạn có chắc chắn muốn hủy phiên đổi trả này?`, chọn "Có" (YES).
- **Kết quả mong đợi:**
  - Banner `pnlThongBaoDoiTra` ẩn đi (`setVisible(false)`).
  - Biến `thongTinDoiTraHienTai` được gán về `null`.
  - Giỏ hàng được làm sạch, tiền giảm giá và tiền khấu trừ đổi trả bị xóa về 0 đ.
  - Màn hình POS trở về trạng thái bán hàng bình thường.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Đạt: Xóa sạch dữ liệu đổi trả tạm thời, không để sót tiền khấu trừ sang đơn tiếp theo.
  - Không đạt: Tiền khấu trừ vẫn còn lưu lại ở hóa đơn sau.

---

### 3.7 Nhóm Kiểm Thử Toàn Vẹn Tồn Kho & Lỗi Giao Dịch Đổi Trả (Inventory & Transaction Integrity)

---

#### TC-DT-008: RÀ SOÁT LỖI DEF-05 / DEF-CRI-12 - Hàng đổi trả nguyên tem chỉ cộng RAM, không nhập CSDL
- **Phân hệ & Component:** Đổi Trả & Kho | `gui.DoiTraHangPanel` (dòng 803-823), `gui.BanHangPOSPanel` (dòng 2932-2940), `dao.HoaDonDAO` (dòng 429-475)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-05 / DEF-CRI-12 - CRITICAL DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng và tái hiện lỗi DEF-05 / DEF-CRI-12: Khi khách hàng trả lại hàng hóa còn nguyên tem bảo hành (tình trạng: `"Còn nguyên tem (nhập lại kho)"`), hệ thống chỉ gọi hàm tăng biến tồn kho trên bộ nhớ RAM (`KhoLoHangPanel.congTonKho` và `BanHangPOSPanel.congTonKhoCatalog`), hoàn toàn không thực thi câu lệnh SQL `UPDATE LoHang SET soLuongTon = soLuongTon + ?` trong SQL Server.
- **Điều kiện tiên quyết:**
  - Sản phẩm `SP001` trong bảng `LoHang` có số lượng tồn hiện tại là `10` đơn vị.
- **Các bước thực hiện:**
  1. Thực hiện đổi trả sản phẩm `SP001` với số lượng 2 gói, tình trạng chọn `"Còn nguyên tem (nhập lại kho)"`.
  2. Hoàn tất lưu phiếu đổi trả tại `DoiTraHangPanel` hoặc thanh toán tại `BanHangPOSPanel`.
  3. Quan sát giao diện: Thấy tồn kho hiển thị trên bảng quản lý kho tăng lên 12 (do cộng trên RAM).
  4. Mở SQL Server Management Studio (SSMS), chạy câu truy vấn kiểm tra tồn kho thực tế trong bảng `LoHang`.
  5. Tắt ứng dụng Java Swing và khởi động lại (`run.bat`).
  6. Mở lại màn hình Quản lý Kho hoặc POS để xem tồn kho của `SP001`.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Tại Bước 4: Tồn kho trong bảng `LoHang` vẫn là `10` (hoàn toàn không thay đổi).
  - Tại Bước 6: Sau khi khởi động lại ứng dụng, tồn kho của `SP001` bị tụt ngược từ 12 về 10!
  - Kiểm tra mã nguồn `HoaDonDAO.luuPhieuDoiTra`: Phương thức chỉ có 2 câu lệnh INSERT vào `PhieuDoiTra` và `ChiTietDoiTra`, không hề có câu lệnh UPDATE nào vào bảng `LoHang`. Đồng thời bảng `ChiTietDoiTra` không có cột lưu `soLo` để biết nhập trả vào lô nào.
  - **XÁC NHẬN LỖI DEF-05 / DEF-CRI-12 TỒN TẠI 100%:** Hàng trả nguyên tem bị thất thoát vĩnh viễn khỏi CSDL quản trị kho.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra tồn kho LoHang trước và sau khi đổi trả hàng nguyên tem
  SELECT soLo, maSanPham, soLuongNhap, soLuongTon 
  FROM LoHang 
  WHERE maSanPham = 'SP001';
  -- Nếu soLuongTon không tăng -> DEFECT DEF-05 / DEF-CRI-12 CONFIRMED!
  ```

---

#### TC-DT-009: RÀ SOÁT LỖI DEF-08 / DEF-MAJ-04 - Giao dịch không nguyên tử khi thanh toán đổi hàng tại POS
- **Phân hệ & Component:** POS & Đổi Trả | `gui.BanHangPOSPanel` (dòng 2918-2930)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-08 / DEF-MAJ-04 - MAJOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng nguy cơ Partial Commit (bất nhất dữ liệu tài chính): Khi thanh toán đơn hàng có đổi trả, POS gọi `busThanhToan.luuPhieuDoiTra` (kết nối 1, auto-commit) rồi mới gọi `busThanhToan.luuHoaDonPOS` (kết nối 2, auto-commit). Nếu bước 2 gặp lỗi (ví dụ đụng độ mã hóa đơn DEF-09 / DEF-MAJ-05 hoặc mất kết nối), bước 1 đã lưu vĩnh viễn vào CSDL mà không thể rollback.
- **Điều kiện tiên quyết:**
  - Tiếp nhận đơn đổi trả sang POS.
- **Các bước thực hiện:**
  1. Tiếp nhận đổi trả vào POS.
  2. Giả lập lỗi ở bước lưu hóa đơn (ví dụ cố tình chèn mã hóa đơn đã tồn tại vào CSDL trước khi bấm thanh toán).
  3. Bấm "THANH TOÁN (F9)".
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Hệ thống văng lỗi tại bước lưu hóa đơn và hiện popup lỗi CSDL.
  - Kiểm tra CSDL: Bảng `PhieuDoiTra` đã lưu thành công phiếu đổi trả, nhưng bảng `HoaDon` không có hóa đơn mua hàng mới tương ứng.
  - Cửa hàng bị mất tiền khấu trừ hàng trả mà không thu được tiền đơn hàng mới.
  - **XÁC NHẬN LỖI DEF-08 / DEF-MAJ-04 TỒN TẠI 100%:** Hai thao tác độc lập không nằm chung trong 1 Database Transaction duy nhất.
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

---

#### TC-DT-010: RÀ SOÁT LỖI DEF-12 / DEF-MIN-02 - Không khóa tạm hóa đơn gốc khi chuyển luồng đổi trả sang POS
- **Phân hệ & Component:** Đổi Trả Hàng | `gui.DoiTraHangPanel` (dòng 785-801)
- **Cấp độ kiểm thử:** Integration / Defect Verification Test (**DEF-12 / DEF-MIN-02 - MINOR DEFECT**)
- **Mục tiêu kiểm thử:** Kiểm chứng nguy cơ đổi trả trùng lặp: Khi chọn Kịch bản B chuyển dữ liệu sang POS, hóa đơn gốc chưa được đánh dấu khóa tạm (lock) trong CSDL. Nếu thu ngân khác mở lại `DoiTraHangPanel` trên máy khác cùng thời điểm, họ vẫn có thể chọn tiếp hóa đơn đó để trả hàng lần thứ 2.
- **Điều kiện tiên quyết:**
  - Hóa đơn `HD261107-01` vừa được gửi sang POS để thanh toán chênh lệch.
- **Các bước thực hiện:**
  1. Tại Máy 1: Chọn đổi hóa đơn `HD261107-01` sang POS (chưa bấm thanh toán trên POS).
  2. Tại Máy 2: Mở `DoiTraHangPanel`, tra cứu lại đúng mã `HD261107-01`.
- **Kết quả quan sát & Báo cáo lỗi (Defect Confirmation):**
  - Máy 2 vẫn hiển thị đầy đủ số lượng hàng nguyên vẹn, cho phép tiếp tục tạo đơn đổi trả lần 2.
  - **XÁC NHẬN LỖI DEF-12 / DEF-MIN-02 TỒN TẠI.**
- **Tiêu chí Đạt/Không đạt (Pass/Fail):**
  - Test Case đánh giá: **FAIL (Defect Detected)**.

---

## 4. TỔNG KẾT & KHUYẾN NGHỊ KHẮC PHỤC (SUMMARY & RECOMMENDATIONS)

1. **Về luồng Bán hàng POS:**
   - Các chức năng thanh toán Tiền mặt (tính tiền thối, nút mệnh giá nhanh) và Chuyển khoản VietQR hoạt động hoàn hảo, đáp ứng 100% yêu cầu nghiệp vụ R1 của dự án.
   - Cần khắc phục dứt điểm lỗi **DEF-02 / DEF-CRI-02**: Bổ sung cập nhật trừ điểm đã dùng vào câu lệnh SQL UPDATE trong `HoaDonDAO.thanhToanHoaDon`.
   - Cần khắc phục lỗi **DEF-04 / DEF-BLK-01**: Sửa hàm `fn_SinhMaHoaDon` tăng độ dài từ 2 số lên 4 số (`RIGHT('0000' + CAST(@max + 1 AS VARCHAR(10)), 4)`).
2. **Về luồng Đổi trả hàng:**
   - Luồng chuyển tiếp Kịch bản B sang POS hoạt động rất tiện lợi, giải quyết triệt để bài toán bù tiền tại quầy.
   - Cần khắc phục lỗi **DEF-05 / DEF-CRI-12**: Bổ sung cập nhật tăng tồn kho `LoHang` trong `HoaDonDAO.luuPhieuDoiTra` đối với hàng nguyên tem.
   - Cần gộp `luuPhieuDoiTra` và `luuHoaDonPOS` vào chung 1 JDBC Transaction duy nhất (giải quyết **DEF-08 / DEF-MAJ-04**).
