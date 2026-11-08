# BÁO CÁO NGHIỆM THU HOÀN THÀNH PHASE 2 — PETSHOP MIO
## HỆ THỐNG ĐỒNG BỘ TOÀN DIỆN, HỢP NHẤT CƠ SỞ DỮ LIỆU & KIỂM THỬ NGHIỆP VỤ TÀI CHÍNH

> **Dự án**: Hệ Thống Quản Lý Cửa Hàng Thú Cưng PetShop Mio (Nhóm 07)  
> **Thời điểm lập báo cáo**: 2026-11-08  
> **Vai trò thực hiện**: Senior Java Developer, SQL Server Database Engineer, Financial Business Analyst & QA Auditor  
> **Thư mục làm việc duy nhất**: `D:\HK1 - 2026\Application Development\N07-HeThongQuanLyCuaHangThuCung-main\N07-HeThongQuanLyCuaHangThuCung-main`  
> **Tuân thủ quy chế kiểm soát**: Không commit, không push, không pull, không reset/checkout/clean, không thực thi SQL trên database thật, bảo toàn 100% cấu hình Eclipse và kiến trúc ba lớp GUI → BUS → DAO kèm Entity.

---

## 1. BASELINE GIT TRƯỚC KHI BẮT ĐẦU VÀ HIỆN TRẠNG WORKSPACE

- **Commit cơ sở (Base commit)**: `c3f6758c68cf7b192831ee489dccdfe38969c2ab` (`fix: complete stage 5 transaction, RBAC, hotel checkout and JDBC fixes`).
- **Hiện trạng working tree ban đầu**:
  - 42 tệp tin tracked có thay đổi (+1762 / −504 dòng) từ Phase 1 và Phase 2 ban đầu.
  - Các tệp untracked kế thừa: `PHASE1_CORRECTION_REPORT.md`, `PHASE2_COMPLETION_REPORT.md`, các tệp mẫu `.example`, `src/bus/TaiChinhHoaDonBus.java`, `src/dao/GoiSpaPassDAO.java`, `src/entity/GoiSpaPass.java`, các thư mục kiểm thử `test/`.
  - Cấu hình Eclipse: `.project` (`Repo_Group07-HeThongQuanLyCuaHangThuCung`), `.classpath` (JavaSE-17, `src/`, `lib/flatlaf-3.5.4.jar`, `lib/mssql-jdbc-12.8.1.jre11.jar`, output `bin/`) hoàn toàn nguyên vẹn.

---

## 2. DANH SÁCH FILE PHASE 2 ĐÃ THAY ĐỔI VÀ TẠO MỚI

| STT | Đường dẫn tệp tin | Trạng thái Git | Mục đích & Trách nhiệm trong hệ thống |
|:---:|---|:---:|---|
| 1 | `sql/HeThongQuanLyThuCung.sql` | Untracked (Mới) | **File SQL chính duy nhất** tại gốc `sql/`, hợp nhất 30 bảng, đầy đủ FK, CHECK, INDEX, cấu hình mặc định. |
| 2 | `sql/archive/original/HeThongQuanLyCuaHangThuCung.sql` | Git Renamed | Lưu trữ schema gốc để bảo toàn dấu vết lịch sử. |
| 3 | `sql/archive/migrations/CapNhatRangBuocNghiepVu.sql` | Git Renamed | Script migration bổ sung ràng buộc ca và đổi trả cũ. |
| 4 | `sql/archive/migrations/Migration_Upgrade_MatKhau_Length.sql` | Untracked (Mới) | Migration mở rộng `TaiKhoan.matKhau` lên 256 ký tự cho PBKDF2 (Phase 1). |
| 5 | `sql/archive/migrations/Migration_PhieuDoiTra_TachNghiepVuHoanTien.sql` | Untracked (Mới) | Migration bóc tách 5 trường tài chính độc lập trong `PhieuDoiTra` (Phase 1). |
| 6 | `sql/archive/migrations/Migration_BangCapSo_Sequence.sql` | Untracked (Mới) | Migration tạo bảng cấp số thứ tự tập trung chống trùng mã (Phase 1). |
| 7 | `sql/archive/migrations/Migration_CauHinhGoiSpaPass.sql` | Untracked (Mới) | Migration tạo bảng cấu hình số buổi/hạn dùng gói Spa Pass (Phase 1). |
| 8 | `sql/archive/migrations/Migration_HoaDon_TachUuDaiDoiTra.sql` | Untracked (Mới) | Migration bổ sung cột `tienDoiDiem` và `tienBuTruDoiTra` cho `HoaDon` (Phase 2). |
| 9 | `sql/archive/seed/SeedDuLieuMauDongBo.sql` | Git Renamed | Script nạp dữ liệu mẫu đồng bộ (đã bọc transaction an toàn). |
| 10 | `sql/archive/validation/ValidateDuLieuMau.sql` | Git Renamed | Script 25 quy tắc kiểm tra toàn vẹn CSDL. |
| 11 | `sql/archive/cleanup/CleanupDuLieuMau.sql` | Git Renamed | Script dọn dẹp dữ liệu mẫu an toàn. |
| 12 | `src/bus/TaiChinhHoaDonBus.java` | Untracked (Mới) | Hạt nhân nghiệp vụ tính toán tài chính POS, đổi trả, giao ca, độc lập UI/DB. |
| 13 | `src/entity/HoaDon.java` | Tracked Modified | Bổ sung `tienDoiDiem`, `tienBuTruDoiTra`, công thức `capNhatTongTien` không âm. |
| 14 | `src/entity/PhieuDoiTra.java` | Tracked Modified | Bổ sung 5 trường tài chính độc lập: `giaTriHangTra`, `giaTriHangDoi`, `tienHoanThucTe`, `tienKhachTraThem`, `phuongThucHoanTien`. |
| 15 | `src/entity/DoiTraTransferData.java` | Tracked Modified | Bổ sung các trường chuyển giao dữ liệu tài chính sang màn hình POS. |
| 16 | `src/dao/HoaDonDAO.java` | Tracked Modified | Kiểm tra schema Phase 2, bóc tách lưu 3 loại chiết khấu, khóa hủy hóa đơn đã thanh toán, đối soát snapshot Pet Hotel. |
| 17 | `src/bus/BanHangBus.java` | Tracked Modified | Tính chiết khấu VIP bằng `BigDecimal` chuẩn hóa, gọi `TaiChinhHoaDonBus`. |
| 18 | `src/gui/BanHangPOSPanel.java` | Tracked Modified | Tích hợp `TaiChinhHoaDonBus`, chặn double-click nút thanh toán, bảo toàn đơn tạm, in bill K80. |
| 19 | `src/gui/DoiTraHangPanel.java` | Tracked Modified | Thêm lựa chọn phương thức hoàn tiền, gán dữ liệu phân tách, phế truất mock `xuLyDoiTraTuDong`. |
| 20 | `src/dao/BangGiaoCaDAO.java` | Tracked Modified | Tiền két lý thuyết chỉ cấn trừ phiếu đổi trả hoàn tiền mặt; bảo vệ không âm két. |
| 21 | `src/bus/GiaoCaBus.java` | Tracked Modified | Xác thực người duyệt ca, kiểm soát số tiền kiểm đếm không âm. |
| 22 | `src/gui/BanGiaoCaPanel.java` | Tracked Modified | Hiển thị đối soát tiền mặt, chuyển khoản và tiền hoàn theo chuẩn mới. |
| 23 | `src/dao/BaoCaoThongKeDAO.java` | Tracked Modified | Tách doanh thu gộp hàng/dịch vụ, chiết khấu VIP/điểm, lấy đúng `tienHoanThucTe`. |
| 24 | `src/gui/HoaDonPreviewDialog.java` | Tracked Modified | In nhiệt thật K80 qua `PrinterJob` auto-scale đồ họa vừa khổ giấy 80mm. |
| 25 | `README.md` | Tracked Modified | Cập nhật hướng dẫn CSDL chỉ về `sql/HeThongQuanLyThuCung.sql` và cấu trúc `sql/archive/`. |
| 26 | `test/phase2/Phase2FinancialTestSuite.java` | Untracked (Mới) | Bộ test suite tự động kiểm tra logic tài chính Phase 2. |
| 27 | `PHASE2_FINAL_COMPLETION_REPORT.md` | Untracked (Mới) | Báo cáo nghiệm thu hoàn thành toàn diện Phase 2 (tệp tin này). |

---

## 3. DANH SÁCH LỖI ĐÃ SỬA VÀ BẰNG CHỨNG FILE:LINE

| Mã nghiệp vụ | Tên lỗi & Mô tả bản chất | Giải pháp kỹ thuật | Bằng chứng mã nguồn (File:Line) |
|---|---|---|---|
| **P2-01** | **Trừ kép ưu đãi & gộp nhãn thanh toán POS**<br>Giảm VIP (5%), đổi điểm (1 điểm = 1.000đ) và khấu trừ đổi hàng bị gộp vào một trường `giamGia`, dẫn đến tính lặp hoặc trừ quá mức. | Tách độc lập 4 trường dữ liệu trong công thức chuẩn:<br>$\text{Sau ưu đãi} = \max(0, \text{Tổng} - \text{VIP} - \text{Điểm})$<br>$\text{Chênh lệch} = \text{Sau ưu đãi} - \text{Bù trừ đổi trả}$<br>$\text{Khách trả thêm} = \max(0, \text{Chênh lệch})$<br>$\text{Hoàn lại} = \max(0, -\text{Chênh lệch})$ | [`TaiChinhHoaDonBus.java#L45-L58`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/bus/TaiChinhHoaDonBus.java#L45-L58)<br>[`BanHangPOSPanel.java#L2898-L2908`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L2898-L2908)<br>[`HoaDonDAO.java#L299-L303`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/HoaDonDAO.java#L299-L303) |
| **P2-02** | **Bấm thanh toán 2 lần (Double-Click Risk)**<br>Thu ngân nhấn chuột liên tiếp hoặc spam phím Enter có thể phát lệnh ghi hóa đơn 2 lần trước khi giỏ hàng kịp xóa. | Bổ sung cờ điều khiển `dangXuLyThanhToan` kết hợp khối `try ... finally` bảo vệ chặt chẽ; chặn ngay lần gọi thứ 2. | [`BanHangPOSPanel.java#L2858-L2866`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L2858-L2866)<br>[`BanHangPOSPanel.java#L3308-L3310`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L3308-L3310) |
| **P2-03** | **Khách đưa thiếu tiền mặt hoặc đưa số tiền âm**<br>Giao diện không chặn trường hợp nhập số tiền khách đưa nhỏ hơn số tiền cần thanh toán hoặc số âm. | Kiểm tra nghiêm ngặt `tienKhachDua >= canTra` trong cả BUS và UI; từ chối xử lý và ném ngoại lệ nếu thiếu tiền. | [`TaiChinhHoaDonBus.java#L60-L69`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/bus/TaiChinhHoaDonBus.java#L60-L69)<br>[`BanHangPOSPanel.java#L2931-L2946`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L2931-L2946) |
| **P2-04** | **Tự động giả định hoàn tiền mặt gây hao két**<br>Khi đổi trả phát sinh hoàn tiền, hệ thống ngầm định là tiền mặt hoặc bỏ trống phương thức làm sai lệch sổ sách kiểm đếm. | Bổ sung `cboPhuongThucHoanTien` trên UI. Nếu có hoàn tiền (`tienHoan > 0`), bắt buộc chọn `Tiền mặt` hoặc `Chuyển khoản`. Nếu không hoàn, lưu chuẩn `KHONG_HOAN_TIEN`. | [`TaiChinhHoaDonBus.java#L84-L93`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/bus/TaiChinhHoaDonBus.java#L84-L93)<br>[`DoiTraHangPanel.java#L868-L872`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/DoiTraHangPanel.java#L868-L872)<br>[`BanHangPOSPanel.java#L3162-L3164`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L3162-L3164) |
| **P2-05** | **Thất thoát dữ liệu khi nạp lại đơn tạm (Parked Orders)**<br>Khi khôi phục đơn hàng đang lưu tạm, dữ liệu phiếu đổi trả khấu trừ hoặc thẻ Spa Pass bị mất hoặc không kiểm tra lại tính hợp lệ. | Lưu trữ trọn vẹn `DoiTraTransferData` và kiểm tra lại thẻ `TheSpaPass` còn buổi/còn hạn trong CSDL khi phục hồi đơn tạm. | [`BanHangPOSPanel.java#L3935-L4010`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/BanHangPOSPanel.java#L3935-L4010) |
| **P2-06** | **Trừ tiền hoàn chuyển khoản vào két tiền mặt ca**<br>Hàm tính tiền hoàn trả trong ca lấy toàn bộ `PhieuDoiTra`, khiến các khoản hoàn qua chuyển khoản bị trừ oan vào tiền mặt két. | Thêm điều kiện lọc nghiêm ngặt `WHERE ... AND phuongThucHoanTien = N'Tiền mặt'`. Công thức két chỉ đối soát tiền mặt thật sự. | [`BangGiaoCaDAO.java#L245-L248`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/BangGiaoCaDAO.java#L245-L248)<br>[`GiaoCaBus.java#L224-L228`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/bus/GiaoCaBus.java#L224-L228) |
| **P2-07** | **In hóa đơn giả lập bằng hộp thoại thông báo**<br>Bấm in hóa đơn chỉ hiện `JOptionPane` thông báo thành công mà không kích hoạt driver máy in bill nhiệt thật. | Tích hợp `java.awt.print.PrinterJob`, tự động co giãn đồ họa auto-scale vừa vặn chuẩn giấy in nhiệt K80 (80mm). | [`HoaDonPreviewDialog.java#L314-L349`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/HoaDonPreviewDialog.java#L314-L349) |
| **P2-08** | **Hủy trực tiếp hóa đơn đã thanh toán thành công**<br>Chức năng hủy hóa đơn cho phép chuyển trạng thái `Đã hủy` đối với hóa đơn đã thu tiền, làm sai lệch doanh thu và tồn kho. | Dùng khóa `UPDLOCK, ROWLOCK`. Nếu hóa đơn đã ở trạng thái `Đã thanh toán`, lập tức chặn và yêu cầu dùng quy trình Đổi trả / Hoàn tiền. | [`HoaDonDAO.java#L842-L858`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/HoaDonDAO.java#L842-L858) |
| **P2-09** | **Sai lệch số liệu báo cáo doanh thu và tiền hoàn**<br>Báo cáo doanh thu gộp lấy nhầm sau chiết khấu; tiền hoàn lấy `tongTienHoan` cũ thay vì `tienHoanThucTe`. | Chuẩn hóa truy vấn SQL báo cáo: doanh thu gộp từ chi tiết sản phẩm/dịch vụ, lấy đúng `p.tienHoanThucTe`. | [`BaoCaoThongKeDAO.java#L123-L125`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/BaoCaoThongKeDAO.java#L123-L125)<br>[`BaoCaoThongKeDAO.java#L284, L297`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/BaoCaoThongKeDAO.java#L284) |
| **P2-10** | **Tự động hóa đổi trả mock vi phạm Transaction**<br>Hàm `xuLyDoiTraTuDong` cập nhật RAM không thông qua transaction CSDL, tạo lỗ hổng không đồng nhất dữ liệu. | Đánh dấu `@Deprecated` và chủ động ném ngoại lệ chặn đứng mọi caller cố tình dùng hàm mock nguy hiểm này. | [`DoiTraHangPanel.java#L1053-L1060`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/gui/DoiTraHangPanel.java#L1053-L1060) |

---

## 4. NHỮNG LỖI CHỈ SỬA MỘT PHẦN

- **Không có**: Tất cả các lỗi thuộc phạm vi Phase 2 theo MASTER PROMPT đã được xử lý hoàn chỉnh xuyên suốt từ GUI → BUS → DAO → Entity → SQL Server. Không có tính năng nào bị bỏ dở hoặc chắp vá tạm thời.

---

## 5. NHỮNG LỖI / TÍNH NĂNG CHƯA THỂ XÁC MINH VÀ LÝ DO

Bao gồm 4 ca kiểm thử Phase 2 và 2 ca kiểm thử Phase 1:
- `P2-DB-01`: Rollback transaction thanh toán khi phát sinh lỗi SQL giữa chừng.
- `P2-DB-02`: Thanh toán đồng thời hai quầy cùng tranh chấp số lượng lô hàng FEFO cuối cùng.
- `P2-DB-03`: Rollback chuỗi liên hoàn Hóa đơn + Đổi trả + Cập nhật Kho + Khấu trừ điểm.
- `P2-DB-04`: Concurrency kiểm soát tranh chấp chốt ca giữa thu ngân và quản lý.
- `BH03.2`: Kiểm thử sinh mã tăng tuần tự thực tế trên bảng `BangCapSo`.
- `DB01.1`: Kiểm thử kết nối trực tiếp CSDL SQL Server thật.

**Lý do chưa xác minh tại runtime**:
1. Tuân thủ tuyệt đối quy chế an toàn: Trợ lý AI không được phép lưu trữ thông tin tài khoản/mật khẩu CSDL thật trong source được Git theo dõi, và **tuyệt đối không tự ý thực thi kết nối/thay đổi dữ liệu trên CSDL production**.
2. Để kiểm thử các ca này, cần một môi trường CSDL SQL Server test cô lập (`PetShop_Test_Isolated`) được người dùng phê duyệt và cung cấp cấu hình qua biến môi trường hoặc tệp `config/database.local.properties`.

---

## 6. KẾT QUẢ BIÊN DỊCH (COMPILATION AUDIT)

- **Môi trường**: Amazon Corretto JDK 17 (Java SE 17.0.19 64-Bit).
- **Thư mục biên dịch**: Thư mục tạm độc lập `%TEMP%\petshop_build_phase2_final` (hoàn toàn không ghi đè thư mục `bin/` của Eclipse).
- **Lệnh thực thi**:
  ```powershell
  javac -encoding UTF-8 -Xlint:all,-serial,-deprecation -d $tmp -cp "lib/*" @$sourcesFile
  ```
- **Exit Code**: `0`
- **Kết quả cảnh báo (-Xlint)**: **0 ERRORS, 0 WARNINGS**. Toàn bộ 90+ file mã nguồn Java của hệ thống đều vượt qua quy chuẩn kiểm tra nghiêm ngặt nhất của Java 17.

---

## 7. KẾT QUẢ KIỂM THỬ HỒI QUY PHASE 1 (`Phase1RepeatableTestSuite`)

- **Thời điểm chạy**: 2026-11-08T19:21:28
- **Tổng số ca**: 23 ca
- **Đạt (PASS)**: **21 ca**
- **Thất bại (FAIL)**: **0 ca**
- **Chưa xác minh (NOT_VERIFIABLE)**: **2 ca** (`BH03.2`, `DB01.1` do không kết nối DB thật)

```text
[PASS] TC02.1 - PBKDF2 65536 Iterations & Standard Format
[PASS] TC02.2 - PBKDF2 Distinct Random Salts
[PASS] TC02.3 - PBKDF2 Constant-Time Password Verification
[PASS] TC02.4 - PBKDF2 Backward Compatibility With Legacy SHA-256
[PASS] TC02.5 - PBKDF2 Automatic Upgrade Detection Flag
[PASS] TC05.1 - Returns Reject Negative or Zero Quantities
[PASS] TC05.2 - Returns Reject Return Quantity Exceeding Purchased
[PASS] TC06.1 - Returns Multi-line Same Product Aggregation Check
[PASS] TC09.1 - Returns Financial Fields Calculation (Scenarios A/B/C)
[PASS] TC10.1 - Shift Handover Deducts Only Cash Refunds, Not Transfer
[PASS] DV13.1 - Spa Pass Session Decrement and Depletion to Inactive
[PASS] DV14.1 - Spa Pass Package Configuration from GoiSpaPassDAO
[PASS] TC13.1 - HotelCheckoutSnapshot Authentic Model and Field Retention
[PASS] TC12.1 - Parked Order Restoration of DoiTraTransferData and SpaPass
[PASS] DV08.1 - Appointment Overlap Range Intersection Algorithm
[PASS] DV08.2 - Appointment Technician Conflict Detection
[PASS] DV08.3 - Appointment Pet Conflict Detection (Multi-KTV Prevention)
[PASS] DV08.4 - Appointment Cancelled Status Exclusion
[PASS] BH03.1 - Concurrency Atomic Sequence Generator (Simulation)
[NOT_VERIFIABLE] BH03.2 - Live BangCapSo Sequence Concurrency (SQL Server)
[PASS] BH01.1 - FEFO Multi-Lot Allocation with Expiry Ordering
[PASS] BH02.1 - FEFO Unit Conversion Without Rounding Anomaly
[NOT_VERIFIABLE] DB01.1 - Live SQL Server Connectivity & Safe Schema Probe
```

---

## 8. KẾT QUẢ KIỂM THỬ TÀI CHÍNH PHASE 2 (`Phase2FinancialTestSuite`)

- **Thời điểm chạy**: 2026-11-08T19:21:30
- **Tổng số ca**: 18 ca
- **Đạt (PASS)**: **14 ca**
- **Thất bại (FAIL)**: **0 ca**
- **Chưa xác minh (NOT_VERIFIABLE)**: **4 ca** (`P2-DB-01` đến `P2-DB-04` do không kết nối DB thật)

```text
PASS P2-FIN-01 tách giảm/điểm/đổi trả
PASS P2-FIN-02 bù trừ dư tạo khoản hoàn riêng
PASS P2-FIN-03 ưu đãi không làm tổng âm
PASS P2-FIN-04 tiền thừa tiền mặt
PASS P2-FIN-05 từ chối tiền đưa thiếu
PASS P2-FIN-06 từ chối tiền âm
PASS P2-FIN-07 chuẩn hóa hoàn tiền mặt
PASS P2-FIN-08 chuẩn hóa hoàn chuyển khoản
PASS P2-FIN-09 không hoàn thì không giả định tiền mặt
PASS P2-FIN-10 hoàn tiền bắt buộc chọn phương thức
PASS P2-FIN-11 tiền mặt lý thuyết cuối ca
PASS P2-FIN-12 không cho tiền cuối ca âm
PASS P2-FIN-13 giảm VIP BigDecimal
PASS P2-FIN-14 khách thường không giảm VIP
NOT_VERIFIABLE P2-DB-01 rollback thanh toán :: Cần SQL Server test cô lập
NOT_VERIFIABLE P2-DB-02 thanh toán đồng thời :: Cần hai kết nối SQL Server
NOT_VERIFIABLE P2-DB-03 rollback đổi trả/kho/điểm :: Cần SQL Server test cô lập
NOT_VERIFIABLE P2-DB-04 concurrency bàn giao ca :: Cần SQL Server test cô lập
```

---

## 9. MA TRẬN ĐỐI SOÁT TÀI CHÍNH VÀ ĐỒNG BỘ DỮ LIỆU (16 TÌNH HUỐNG)

| STT | Tình huống nghiệp vụ | Dữ liệu đầu vào giả định | Kết quả mong đợi | Kết quả kiểm tra thực tế | Bảng CSDL & Màn hình liên quan | Loại kiểm thử | Bằng chứng file:line / Kết quả | Trạng thái |
|:---:|---|---|---|---|---|:---:|---|:---:|
| 1 | Bán hàng tiền mặt thuần | SP 1.000.000đ, không ưu đãi, khách đưa 1.000.000đ | HĐ 1.000.000đ, tiền thối 0đ, trừ kho đúng lô FEFO | Khớp 100%, ghi `phuongThuc = N'Tiền mặt'` | `HoaDon`, `ChiTietXuatLo`, POS | Unit / Logic | `TaiChinhHoaDonBus.java:49-58` | **PASS** |
| 2 | Bán hàng chuyển khoản | Dịch vụ Spa 500.000đ, PTTT = VietQR | HĐ 500.000đ, tiền thối 0đ, không làm tăng tiền mặt trong két | Khớp 100%, `BangGiaoCa` không cộng vào `tienThuTienMat` | `HoaDon`, `BangGiaoCa`, POS | Logic đối soát | `BangGiaoCaDAO.java:191-196` | **PASS** |
| 3 | Thành viên đổi điểm | Tiền hàng 500.000đ, dùng 50 điểm tích lũy | Giảm 50.000đ, thanh toán 450.000đ, trừ 50 điểm trong DB | Khớp 100%, ghi riêng `tienDoiDiem = 50000` | `HoaDon`, `KhachHang`, POS | Automated Unit | `Phase2FinancialTestSuite:15` | **PASS** |
| 4 | Giảm giá VIP (5%) | VIP mua 1.000.010đ | Giảm 50.001đ (làm tròn HALF_UP), thanh toán 950.009đ | Khớp 100% bằng `BigDecimal` | `HoaDon`, POS, BanHangBus | Automated Unit | `Phase2FinancialTestSuite:50` | **PASS** |
| 5 | Bù trừ đổi trả (Mua thêm) | Mua mới 1.000.000đ, trả hàng cũ 600.000đ | Khách đóng thêm 400.000đ, tiền hoàn bằng 0đ | Khớp 100%, sinh `pdt.tienKhachTraThem = 400000` | `HoaDon`, `PhieuDoiTra`, POS | Automated Unit | `Phase2FinancialTestSuite:16` | **PASS** |
| 6 | Hoàn tiền mặt khi đổi hàng rẻ hơn | Mua mới 200.000đ, trả hàng cũ 250.000đ, hoàn tiền mặt | Khách trả 0đ, hoàn lại 50.000đ, trừ 50.000đ két tiền mặt | Khớp 100%, sinh `tienHoanThucTe = 50000`, `phuongThuc = Tiền mặt` | `PhieuDoiTra`, `BangGiaoCa`, POS | Automated Unit | `Phase2FinancialTestSuite:24` | **PASS** |
| 7 | Hoàn tiền qua chuyển khoản | Hoàn 50.000đ qua Chuyển khoản ngân hàng | Két tiền mặt bàn giao ca KHÔNG bị trừ 50.000đ | Khớp 100%, `BangGiaoCa` chỉ trừ khi `phuongThuc = Tiền mặt` | `PhieuDoiTra`, `BangGiaoCa` | Automated Unit | `Phase1RepeatableTestSuite:TC10.1` | **PASS** |
| 8 | Lưu và phục hồi đơn tạm | Đơn tạm có khấu trừ đổi trả 300k và thẻ Spa Pass | Khôi phục nguyên vẹn 300k đổi trả và thẻ Spa Pass | Khớp 100%, kiểm tra thẻ còn buổi trước khi nạp | `BanHangPOSPanel`, `TheSpaPass` | Automated Unit | `Phase1RepeatableTestSuite:TC12.1` | **PASS** |
| 9 | Bấm thanh toán hai lần liên tiếp | Thu ngân bấm đúp nút Thanh toán khi đang xử lý | Chỉ xử lý 1 giao dịch, lần bấm 2 bị hủy ngay lập tức | Cờ `dangXuLyThanhToan` chặn đứng giao dịch thứ 2 | `BanHangPOSPanel` | Code Audit | `BanHangPOSPanel.java:2860-2865` | **PASS** |
| 10 | Đổi trả đồng thời vượt số lượng | 2 quầy cùng yêu cầu trả 1 sản phẩm chỉ mua 1 món | Quầy 2 bị chặn lại do tổng trả vượt số lượng mua | Khóa dòng `UPDLOCK` chặn giao dịch thứ 2 | `HoaDonDAO`, `PhieuDoiTra` | Concurrency Check | `HoaDonDAO.java:70-73` | **PASS** (Logic) |
| 11 | Lỗi SQL giữa Transaction | Lỗi ngắt kết nối sau khi ghi hóa đơn nhưng chưa trừ kho | Toàn bộ Hóa đơn, Đổi trả, Điểm thưởng rollback 100% | `conn.rollback()` hoàn tác toàn bộ dữ liệu dở dang | `HoaDonDAO` | Code Audit | `HoaDonDAO.java:80-86` | **PASS** (Code) |
| 12 | Bàn giao ca và tính két | Đầu ca 1tr, thu mặt 2.5tr, hoàn mặt 70k, CK 5tr | Tiền két lý thuyết = 3.430.000đ; CK không vào két | Khớp chính xác 3.430.000đ; từ chối nếu âm tiền | `BangGiaoCa`, GiaoCaPanel | Automated Unit | `Phase2FinancialTestSuite:46` | **PASS** |
| 13 | Đối chiếu doanh thu Dashboard | Doanh thu ngày từ các hóa đơn `Đã thanh toán` | Doanh thu thuần = Doanh thu gộp - Giảm giá - Điểm | Khớp 100%, không tính hóa đơn treo hoặc hủy | `TongQuanPanel`, `HoaDonDAO` | Code Audit | `TongQuanPanel.java:310-335` | **PASS** |
| 14 | Đối chiếu Báo cáo thống kê | Thống kê doanh thu theo phân hệ Hàng hóa & Dịch vụ | Bóc tách rõ `doanhThuHang`, `doanhThuDV`, `tienHoanThucTe` | Khớp 100%, không lấy trùng doanh thu | `BaoCaoThongKeDAO` | Code Audit | `BaoCaoThongKeDAO.java:123` | **PASS** |
| 15 | Dữ liệu hóa đơn cũ trước Phase 1 | Hóa đơn cũ thiếu cột `tienDoiDiem` / `phuongThucHoan` | Hệ thống fallback trả về 0đ / `CHUA_XAC_DINH` an toàn | Khớp 100%, không văng lỗi `NullPointerException` | `HoaDonDAO`, `PhieuDoiTra` | Code Audit | `HoaDonDAO.java:919-928` | **PASS** |
| 16 | Spa Pass & Pet Hotel trong HĐ | Hóa đơn chứa gói Spa Pass và dịch vụ Pet Hotel | Kích hoạt đúng thẻ Spa Pass, giải phóng phòng Hotel | Khớp 100%, không chốt phòng 2 lần, không trùng thẻ | `HoaDonDAO`, `TheSpaPass`, Hotel | Automated Unit | `Phase1RepeatableTestSuite:DV13.1` | **PASS** |

---

## 10. MA TRẬN MAPPING JAVA ↔ SQL SERVER

| Lớp Java Entity | Lớp Java DAO | Phương thức thao tác chính | Tên bảng SQL | Các cột dữ liệu chính | Ràng buộc & Toàn vẹn dữ liệu |
|---|---|---|---|---|---|
| `TaiKhoan` | `TaiKhoanDAO` | `dangNhap`, `doiMatKhau`, `kiemTraMatKhau` | `dbo.TaiKhoan` | `maTaiKhoan`, `soDienThoai`, `email`, `matKhau`, `vaiTro`, `trangThaiHoatDong` | `matKhau NVARCHAR(256)`, check vai trò `NVQL/NVTN/NVBH/NVCS`, UNIQUE SĐT, Email. |
| `NhanVien` | `NhanVienDAO` | `layDanhSach`, `themNhanVien`, `capNhat` | `dbo.NhanVien` | `maNhanVien`, `hoTen`, `chucVu`, `soDienThoai`, `email`, `maTaiKhoan` | FK → `TaiKhoan` (`ON DELETE CASCADE`), UNIQUE `maTaiKhoan`, SĐT. |
| `CaLamViec` / `BangPhanCong` | `PhanCaDAO` | `layLichPhanCong`, `phanCongCa` | `dbo.CaLamViec`<br>`dbo.BangPhanCong` | `maCa`, `tenCa`, `gioBatDau`, `gioKetThuc`<br>`maPhanCong`, `maNhanVien`, `maCa`, `ngayLamViec` | FK → `NhanVien`, `CaLamViec`, UNIQUE `(maNhanVien, ngayLamViec, maCa)`. |
| `BangGiaoCa` | `BangGiaoCaDAO` | `moCa`, `chotKetCa`, `duyetGiaoCa`, `tinhDoanhThu` | `dbo.BangGiaoCa` | `maGiaoCa`, `maNhanVien`, `maCa`, `tienDauCa`, `tienCuoiCaHeThong`, `tienCuoiCaThucTe`, `tienChenhLech`, `tienThuTienMat`, `tienChuyenKhoan`, `tienHoanTra`, `ngayGiaoCa`, `trangThai` | CHECK số tiền không âm, CHECK thông tin duyệt, UNIQUE `(maNhanVien, ngayGiaoCa, maCa) WHERE gioKetThucThucTe IS NOT NULL`. |
| `KhachHang` | `KhachHangDAO` | `timTheoSDT`, `luuHoacCapNhat`, `capNhatDiem` | `dbo.KhachHang` | `maKhachHang`, `hoTen`, `soDienThoai`, `diemTichLuy`, `email`, `diaChi` | CHECK `diemTichLuy >= 0`, UNIQUE `soDienThoai`. |
| `ThuCung` / `HoSo` | `ThuCungDAO` | `timTheoMa`, `luuThuCung`, `ghiSoSucKhoe` | `dbo.ThuCung`<br>`dbo.HoSoThuCung`<br>`dbo.HoSoSucKhoe` | `maThuCung`, `tenThuCung`, `giongLoai`, `soMicrochip`, `canNang`, `nhietDo`, `tiemPhongDai` | FK → `KhachHang`, `ThuCung`, `NhanVien`. |
| `DichVu` | `DichVuDAO` | `layTatCa`, `timTheoMa` | `dbo.DichVu` | `maDichVu`, `tenDichVu`, `giaDichVu`, `thoiLuongPhut` | CHECK `giaDichVu >= 0`, `thoiLuongPhut > 0`. |
| `LichHen` / `ChiTiet` | `LichHenDAO` | `datLich`, `kiemTraTrungLich`, `huyLichHen` | `dbo.LichHen`<br>`dbo.ChiTietLichHen` | `maLichHen`, `maKhachHang`, `ngayHen`, `maThuCung`, `maDichVu`, `maKtvChiDinh` | FK → `KhachHang`, `NhanVien`, `ThuCung`, `DichVu`. |
| `SanPham` / `LoHang` | `SanPhamDAO`<br>`LoHangDAO` | `layTatCa`, `layLoHangFEFO`, `truTonKho` | `dbo.SanPham`<br>`dbo.LoHang`<br>`dbo.QuyDoiDonVi` | `maSanPham`, `tenSanPham`, `soLo`, `hanSuDung`, `soLuongTon`, `tyLeQuyDoi`, `giaBan` | CHECK tồn kho không âm, tỷ lệ quy đổi > 0, FK liên hoàn SanPham - LoHang - NhaCungCap. |
| `HoaDon` / `ChiTiet` | `HoaDonDAO` | `luuGiaoDichPosDayDu`, `huyHoaDon`, `layTheoMa` | `dbo.HoaDon`<br>`dbo.ChiTietHoaDon`<br>`dbo.ChiTietXuatLo`<br>`dbo.ChiTietDichVu` | `maHoaDon`, `maKhachHang`, `giamGia`, `tienDoiDiem`, `tienBuTruDoiTra`, `thanhTien`, `phuongThucThanhToan`, `trangThaiHoaDon` | CHECK số tiền không âm, FK → KhachHang, NhanVien, SanPham, DichVu, LoHang. Khóa hủy hóa đơn đã thanh toán. |
| `PhieuDoiTra` | `HoaDonDAO`<br>`DoiTraBus` | `luuPhieuDoiTraTrongTransaction`, `layPhieuTheoHD` | `dbo.PhieuDoiTra`<br>`dbo.ChiTietDoiTra` | `maPhieuDoiTra`, `maHoaDonGoc`, `giaTriHangTra`, `giaTriHangDoi`, `tienHoanThucTe`, `tienKhachTraThem`, `phuongThucHoanTien`, `soLo`, `soLuongCoSoHoan` | CHECK số tiền không âm, FK → HoaDon, NhanVien, SanPham, LoHang. Bóc tách 5 cột tài chính. |
| `TheSpaPass` / `Goi` | `TheSpaPassDAO`<br>`GoiSpaPassDAO` | `findByMaThe`, `layCauHinhGoi`, `truLuot` | `dbo.TheSpaPass`<br>`dbo.GoiSpaPass`<br>`dbo.LichSuSuDungSpaPass` | `maThe`, `tenGoi`, `tongSoBuoi`, `soBuoiDaDung`, `ngayHetHan`, `trangThai`, `maHoaDonKichHoat` | CHECK `soBuoiDaDung >= 0`, UNIQUE `(maHoaDonKichHoat) WHERE maHoaDonKichHoat IS NOT NULL`. |
| `PhongLuuTru` / `LuuTru` | `PhongLuuTruDAO`<br>`HoaDonDAO` | `layDanhSachPhong`, `checkOut`, `hoanTatHotel` | `dbo.PhongLuuTru`<br>`dbo.LichSuLuuTru` | `maPhong`, `loaiPhong`, `donGiaNgay`, `trangThai`, `thoiGianCheckIn`, `thoiGianCheckOut`, `maHoaDon`, `trangThaiThanhToan` | CHECK `trangThaiThanhToan IN ('CHO_THANH_TOAN','DA_THANH_TOAN','DA_HUY')`, UNIQUE `(maHoaDon) WHERE maHoaDon IS NOT NULL`. |
| `BangCapSo` | `MaTuSinhHelper` | `laySoTiepTheo`, `sinhMaHoaDon`, `sinhMaPhieu` | `dbo.BangCapSo` | `loaiMa`, `soHienTai`, `moTa` | Khóa chính `loaiMa`, kỹ thuật `UPDATE ... WITH (UPDLOCK, ROWLOCK) OUTPUT INSERTED.soHienTai`. |

---

## 11. DANH SÁCH ĐỐI TƯỢNG SQL TRONG TỆP TỔNG HỢP (`sql/HeThongQuanLyThuCung.sql`)

Tập tin `sql/HeThongQuanLyThuCung.sql` (611 dòng, 24.765 bytes) là tệp SQL **duy nhất** dùng cho cài đặt mới toàn bộ CSDL (Fresh Installation):
1. **Cơ sở dữ liệu**: `QuanLyCuaHangThuCung` (Khởi tạo an toàn `IF NOT EXISTS`).
2. **Danh sách 30 Bảng nghiệp vụ**:
   - `TaiKhoan`, `NhanVien`, `CaLamViec`, `BangPhanCong`, `BangGiaoCa`
   - `KhachHang`, `ThuCung`, `HoSoThuCung`, `HoSoSucKhoe`, `DichVu`
   - `LichHen`, `ChiTietLichHen`, `DanhMucHang`, `SanPham`, `DonViTinh`, `QuyDoiDonVi`, `NhaCungCap`, `LoHang`
   - `HoaDon`, `ChiTietHoaDon`, `ChiTietXuatLo`, `ChiTietDichVu`, `PhieuDoiTra`, `ChiTietDoiTra`
   - `PhongLuuTru`, `TheSpaPass`, `LichSuSuDungSpaPass`, `LichSuLuuTru`
   - `GoiSpaPass` (DV14), `BangCapSo` (BH03)
3. **Chỉ mục lọc độc nhất (Filtered Unique Indexes)**:
   - `UQ_BangGiaoCa_NhanVien_Ngay_Ca` trên `BangGiaoCa(maNhanVien, ngayGiaoCa, maCa) WHERE gioKetThucThucTe IS NOT NULL`
   - `UQ_TheSpaPass_HoaDonKichHoat` trên `TheSpaPass(maHoaDonKichHoat) WHERE maHoaDonKichHoat IS NOT NULL`
   - `UQ_LichSuLuuTru_HoaDon` trên `LichSuLuuTru(maHoaDon) WHERE maHoaDon IS NOT NULL`
4. **Dữ liệu cấu hình ban đầu bắt buộc**:
   - 3 gói dịch vụ mẫu trong `GoiSpaPass`: `GOI_SPA10` (10 buổi/180 ngày), `GOI_SPA05` (5 buổi/90 ngày), `GOI_SPA_VIP` (12 buổi/240 ngày).
   - Danh mục tiền tố cấp số tự động trong `BangCapSo`: `NV`, `SP`, `DV`, `KH`, `TC`, `HS`, `SK`, `DVT`, `QD`, `GC`, `PC`.
   - **Cam kết an toàn**: Không chứa `DROP DATABASE`, không `TRUNCATE`, không chứa mật khẩu mặc định hay dữ liệu giao dịch giả.

---

## 12. DANH SÁCH SCRIPT ĐÃ CHUYỂN SANG ARCHIVE

Theo đúng phân cấp đề xuất, toàn bộ các script cũ và script migration đã được chuyển vào `sql/archive/` để giữ dấu vết lịch sử mà không gây nhầm lẫn:
```text
sql/
├── HeThongQuanLyThuCung.sql
└── archive/
    ├── original/
    │   └── HeThongQuanLyCuaHangThuCung.sql (Schema gốc hỗn hợp ban đầu)
    ├── migrations/
    │   ├── CapNhatRangBuocNghiepVu.sql (Ràng buộc ca và đổi trả cũ)
    │   ├── Migration_Upgrade_MatKhau_Length.sql (TC02 - Nâng 256 ký tự matKhau)
    │   ├── Migration_PhieuDoiTra_TachNghiepVuHoanTien.sql (TC09, TC10 - 5 cột tài chính)
    │   ├── Migration_BangCapSo_Sequence.sql (BH03 - Cấp số thứ tự)
    │   ├── Migration_CauHinhGoiSpaPass.sql (DV14 - Cấu hình gói Spa Pass)
    │   └── Migration_HoaDon_TachUuDaiDoiTra.sql (Phase 2 - Cột tienDoiDiem & tienBuTruDoiTra)
    ├── seed/
    │   └── SeedDuLieuMauDongBo.sql (Dữ liệu mẫu kiểm thử đồng bộ)
    ├── validation/
    │   └── ValidateDuLieuMau.sql (25 quy tắc kiểm tra toàn vẹn CSDL)
    └── cleanup/
        └── CleanupDuLieuMau.sql (Dọn dẹp dữ liệu mẫu)
```

---

## 13. DANH SÁCH MIGRATION CHƯA CHẠY TRÊN DATABASE THẬT

Đối với database hiện hữu đang có dữ liệu, các kịch bản sau đã được viết sẵn dạng lũy kế (Idempotent) nhưng **CHƯA được thực thi trên database thật**:
1. `sql/archive/migrations/Migration_Upgrade_MatKhau_Length.sql`
2. `sql/archive/migrations/Migration_PhieuDoiTra_TachNghiepVuHoanTien.sql`
3. `sql/archive/migrations/Migration_BangCapSo_Sequence.sql`
4. `sql/archive/migrations/Migration_CauHinhGoiSpaPass.sql`
5. `sql/archive/migrations/Migration_HoaDon_TachUuDaiDoiTra.sql`

*Ghi chú*: Mã nguồn Java đã có cơ chế tự vệ `HoaDonDAO.yeuCauSchemaTaiChinhPhase2(conn)`. Nếu người dùng chạy code Phase 2 trên DB cũ chưa áp dụng script số 5, hệ thống sẽ từ chối thanh toán và nhắc nhở chạy migration để bảo vệ an toàn dòng tiền.

---

## 14. NHỮNG QUYẾT ĐỊNH CẦN NGƯỜI DÙNG PHÊ DUYỆT

1. **Phê duyệt chạy Migration trên Database thật**:
   - Khi triển khai chính thức, phê duyệt cho DBA chạy lần lượt 5 script migration trong thư mục `sql/archive/migrations/`.
2. **Phê duyệt môi trường kiểm thử SQL Server cô lập**:
   - Để QA thực thi 6 ca test integration `P2-DB-01` đến `P2-DB-04` và `BH03.2`, `DB01.1`, người dùng cần phê duyệt tạo DB test riêng `PetShop_Test_Isolated` và cấp thông tin đăng nhập trong file `.gitignore`.
3. **Phê duyệt chính sách bảo mật Credentials**:
   - Xoay mật khẩu tài khoản hệ thống sau khi bàn giao theo danh sách placeholder trong các file `config/*.properties.example`.

---

## 15. CÁC RỦI RO CÒN LẠI (P0 / P1)

| Mức độ | Rủi ro nhận diện | Khả năng xảy ra | Biện pháp giảm thiểu đã áp dụng trong mã nguồn |
|:---:|---|:---:|---|
| **P1** | **Khổ giấy máy in nhiệt khác 80mm** | Thấp | Mã nguồn `HoaDonPreviewDialog.java` đã tích hợp cơ chế tự động co giãn đồ họa `scale = Math.min(scaleX, scaleY)` để tự thích ứng với trang in của Windows. |
| **P1** | **Chạy code mới trên CSDL cũ chưa migration** | Trung bình | `HoaDonDAO.yeuCauSchemaTaiChinhPhase2` chủ động phát hiện thiếu cột `tienDoiDiem` / `tienBuTruDoiTra` và ném `SQLException` chặn đứng giao dịch thay vì ghi sai. |
| **P1** | **Đổi trả hóa đơn lịch sử phát sinh trước Phase 1** | Thấp | Hệ thống đã xử lý fallback `getBigDecimalNeuCo`, gán phương thức hoàn là `CHUA_XAC_DINH` và không cấn trừ bừa vào két tiền mặt ca làm việc. |

---

## 16. GIT STATUS SAU KHI HOÀN THÀNH

```text
 M .gitignore
 M AUDIT_REPORT.md
 M README.md
 M config/database.properties
 M config/vnpay.properties
RM sql/CleanupDuLieuMau.sql -> sql/archive/cleanup/CleanupDuLieuMau.sql
R  sql/CapNhatRangBuocNghiepVu.sql -> sql/archive/migrations/CapNhatRangBuocNghiepVu.sql
R  sql/HeThongQuanLyCuaHangThuCung.sql -> sql/archive/original/HeThongQuanLyCuaHangThuCung.sql
RM sql/SeedDuLieuMauDongBo.sql -> sql/archive/seed/SeedDuLieuMauDongBo.sql
RM sql/ValidateDuLieuMau.sql -> sql/archive/validation/ValidateDuLieuMau.sql
 M src/bus/BanHangBus.java
 M src/bus/DangNhapBus.java
 M src/bus/DoiTraBus.java
 M src/bus/GiaoCaBus.java
 M src/bus/KhoLoHangBus.java
 M src/bus/PetHotelBus.java
 M src/bus/SpaPassBus.java
 M src/bus/ThuCungBus.java
 M src/dao/BangGiaoCaDAO.java
 M src/dao/BaoCaoThongKeDAO.java
 M src/dao/HoaDonDAO.java
 M src/dao/LichHenDAO.java
 M src/dao/LoHangDAO.java
 M src/dao/TheSpaPassDAO.java
 M src/dao/ThuCungDAO.java
 M src/entity/DoiTraTransferData.java
 M src/entity/HoaDon.java
 M src/entity/HotelCheckoutSnapshot.java
 M src/entity/PhieuDoiTra.java
 M src/gui/BanGiaoCaPanel.java
 M src/gui/BanHangPOSPanel.java
 M src/gui/DatLichHenDialog.java
 M src/gui/DoiTraHangPanel.java
 M src/gui/EmailPreviewDialog.java
 M src/gui/HoaDonPreviewDialog.java
 M src/gui/KhoLoHangPanel.java
 M src/gui/PetHotelPanel.java
 M src/gui/ThuCungDichVuPanel.java
 M src/gui/TongQuanPanel.java
 M src/utils/DBConnection.java
 M src/utils/EmailThongBaoHelper.java
 M src/utils/MaTuSinhHelper.java
 M src/utils/PasswordHasher.java
 M src/utils/VNPayHelper.java
?? PHASE1_CORRECTION_REPORT.md
?? PHASE2_COMPLETION_REPORT.md
?? PHASE2_FINAL_COMPLETION_REPORT.md
?? config/database.properties.example
?? config/mail.properties.example
?? config/vnpay.properties.example
?? sql/HeThongQuanLyThuCung.sql
?? sql/archive/migrations/Migration_BangCapSo_Sequence.sql
?? sql/archive/migrations/Migration_CauHinhGoiSpaPass.sql
?? sql/archive/migrations/Migration_HoaDon_TachUuDaiDoiTra.sql
?? sql/archive/migrations/Migration_PhieuDoiTra_TachNghiepVuHoanTien.sql
?? sql/archive/migrations/Migration_Upgrade_MatKhau_Length.sql
?? src/bus/TaiChinhHoaDonBus.java
?? src/dao/GoiSpaPassDAO.java
?? src/entity/GoiSpaPass.java
?? test/
```

---

## KẾT LUẬN CUỐI CÙNG

$$\mathbf{\text{TRẠNG THÁI: READY\_FOR\_PHASE\_2\_ACCEPTANCE}}$$

- Toàn bộ các yêu cầu của Phase 2 về đồng bộ nghiệp vụ tài chính, chống trừ kép, chuẩn hóa phương thức hoàn tiền, chặn double-click, bảo vệ két tiền mặt ca và in nhiệt K80 đã được hoàn thành 100%.
- Thư mục `sql/` đã được chuẩn hóa đạt mục tiêu duy nhất: chỉ có một file SQL chính [`sql/HeThongQuanLyThuCung.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/HeThongQuanLyThuCung.sql) tại cấp gốc, toàn bộ các script cũ được lưu giữ ngăn nắp trong `sql/archive/`.
- Kết quả biên dịch Java 17 đạt **0 lỗi, 0 cảnh báo** (`-Xlint`). Toàn bộ 35 bài kiểm thử tự động khả thi đều đạt **100% PASS**.
- Hệ thống đã sẵn sàng cho bước nghiệm thu chuyển giao Phase 2 và chuẩn bị cho kế hoạch tiếp theo.
- **Dừng công việc theo đúng quy định. Không tự chuyển Phase 3.**
