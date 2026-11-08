# KỊCH BẢN KIỂM THỬ: BÁO CÁO THỐNG KÊ, DOANH THU & XUẤT - NHẬP - TỒN
## (REPORTS, REVENUE ANALYTICS & INVENTORY AUDIT TEST SPECIFICATION)

- **Mã tài liệu:** `TC_BAO_CAO_THONG_KE`
- **Dự án:** Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)
- **Phân hệ mục tiêu:** Phân hệ 6 - Báo cáo & Thống kê Doanh thu, Tồn kho, Hiệu suất và Dashboard Điều hành
- **Phạm vi thành phần (Scope):**
  - **Tầng Giao diện (GUI):**
    * `gui.TongQuanPanel` (Dashboard điều hành 3 chế độ: Vận hành, Chiến lược, Phân tích).
    * `gui.BaoCaoThongKeDialog` (Trung tâm 7 tab báo biểu quản trị đa chiều).
    * `gui.BarChartPanel` (Biểu đồ cột Graphics2D hiển thị doanh thu 7 ngày).
    * `gui.DonutChartPanel` (Biểu đồ tròn Graphics2D hiển thị cơ cấu ngành hàng và công suất phòng Hotel).
  - **Tầng Nghiệp vụ (BUS):** `bus.ThongKeBus` (Điều phối dữ liệu thống kê, khoảng thời gian kỳ báo cáo).
  - **Tầng Truy cập Dữ liệu (DAO):** `dao.BaoCaoThongKeDAO` (Trực tiếp thực thi các câu truy vấn phân tích dữ liệu chuyên sâu).
  - **Tầng Cơ sở Dữ liệu (T-SQL):** Bảng `HoaDon`, `ChiTietHoaDon`, `ChiTietXuatLo`, `ChiTietDichVu`, `LoHang`, `SanPham`, `QuyDoiDonVi`, `DonViTinh`, `BangPhanCong`, `PhieuDoiTra`, `ChiTietDoiTra`, `PhongLuuTru`.
- **Phiên bản tài liệu:** 1.0 (Chính thức)
- **Ngày ban hành:** 07/11/2026
- **Quy chuẩn kiểm thử:** Độc lập, có thể kiểm chứng, đối chiếu 100% với mã nguồn và CSDL thực tế.

---

## 1. MA TRẬN TRUY XUẤT NGUỒN GỐC & ĐỘ BAO PHỦ (TRACEABILITY MATRIX)

| Mã Test Case | Tên Kịch Bản Kiểm Thử | Thành Phần Mục Tiêu | Cấp Độ | Phân Loại Luồng | Khiếm Khuyết Liên Quan |
|---|---|---|---|---|---|
| **TC-DASH-001** | Hiển thị 4 thẻ chỉ số KPI hôm nay (Doanh thu, Đơn hàng, Lịch hẹn, Phòng Hotel) | `TongQuanPanel`, `BanHangPOSPanel` | UI / Integration | Happy Path | - |
| **TC-DASH-002** | Hiển thị biểu đồ cột `BarChartPanel` - Doanh thu 7 ngày gần nhất | `TongQuanPanel`, `BarChartPanel`, `ThongKeBus` | UI / Integration | Happy Path | - |
| **TC-DASH-003** | Hiển thị biểu đồ tròn `DonutChartPanel` - Cơ cấu ngành hàng & Công suất Pet Hotel | `TongQuanPanel`, `DonutChartPanel` | UI / Integration | Happy Path | - |
| **TC-DASH-004** | Chuyển đổi linh hoạt giữa 3 chế độ Dashboard (Vận hành, Chiến lược, Phân tích) | `TongQuanPanel` | UI Test | Functionality | - |
| **TC-DASH-005** | Kiểm tra dữ liệu Dashboard khi khởi động lại ứng dụng (Đọc RAM vs CSDL SQL Server) | `TongQuanPanel`, `BanHangPOSPanel`, `HoaDonDAO` | Integration / UI | Defect / Data Loss | **DEF-BC-07 / DEF-MIN-04** |
| **TC-DASH-006** | Kiểm tra tính toán lợi nhuận Dashboard Chiến lược (Phát hiện gán cứng tỷ suất 42%) | `TongQuanPanel.java:363, 365` | Unit / UI | Code Quality / Defect | **DEF-BC-05 / DEF-MAJ-08** |
| **TC-BC-001** | Báo cáo Doanh thu & Lợi nhuận - Lọc theo kỳ "Hôm nay" | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / UI | Happy Path | - |
| **TC-BC-002** | Báo cáo Doanh thu & Lợi nhuận - Lọc "7 ngày gần nhất" và "Tháng hiện tại" | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / UI | Happy Path | - |
| **TC-BC-003** | Phân loại doanh thu theo phương thức thanh toán (Tiền mặt vs Chuyển khoản VietQR) | `BaoCaoThongKeDAO.getDoanhThu` | Integration / Unit | Financial Integrity | - |
| **TC-BC-004** | Kiểm tra tính cân đối tài chính: Tiền hàng - Giảm giá = Doanh thu thuần = Tiền hàng + Tiền DV | `BaoCaoThongKeDAO.getDoanhThu` | Unit / Integration | Financial Audit | - |
| **TC-BC-005** | Kiểm tra bỏ sót giao dịch thanh toán ở giây cuối ngày 23:59:59 | `ThongKeBus.java:36`, `BaoCaoThongKeDAO` | Integration / Boundary | Critical Defect | **DEF-BC-06 / DEF-MIN-03** |
| **TC-BC-006** | Báo cáo Doanh thu trong kỳ không phát sinh giao dịch nào (Tập dữ liệu rỗng) | `BaoCaoThongKeDAO`, `BaoCaoThongKeDialog` | Integration / UI | Edge Case / Empty Set | - |
| **TC-BC-007** | Báo cáo Top Bán Chạy - Xếp hạng theo Doanh thu và Số lượng bán | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / UI | Happy Path | - |
| **TC-BC-008** | Báo cáo Top Bán Chạy - Bỏ qua tỷ lệ quy đổi ĐVT khi cộng sản lượng bán | `BaoCaoThongKeDAO.java:165, 177` | Unit / Integration | Defect / Business Logic | **DEF-BC-02 / DEF-CRI-05** |
| **TC-BC-009** | Phân loại rõ ràng cơ cấu mặt hàng: Hàng hóa (sản phẩm vật lý) vs Dịch vụ (lượt spa) | `BaoCaoThongKeDAO.getTopBanChay` | Integration / UI | Categorization | - |
| **TC-BC-010** | Báo cáo Hiệu suất nhân viên - Thống kê 7 ngày / 30 ngày (Bị nuốt ca trực ngày cuối) | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / Boundary | Defect / Boundary | **DEF-MAJ-06** |
| **TC-BC-011** | Báo cáo Hiệu suất nhân viên - Bị hiển thị 0 ca trực khi lọc theo 1 ngày duy nhất | `BaoCaoThongKeDAO.java:203` | Unit / Integration | Defect / Boundary | **DEF-BC-03 / DEF-MAJ-06** |
| **TC-BC-012** | Báo cáo Hiệu suất nhân viên - Lỗi tính trùng doanh thu giữa Thu ngân và KTV | `BaoCaoThongKeDAO.java:204-214` | Unit / Integration | Critical Defect | **DEF-BC-04 / DEF-MAJ-07** |
| **TC-BC-013** | Kiểm tra tính toán hoa hồng thực tế cho Kỹ thuật viên làm dịch vụ Spa | `BaoCaoThongKeDAO.getHieuSuatNhanSu` | Integration / Financial | Commission Audit | - |
| **TC-BC-014** | Báo cáo Cảnh báo FEFO - Danh sách sắp xếp theo Hạn sử dụng tăng dần | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / UI | Happy Path | - |
| **TC-BC-015** | Nhận diện và gắn badge cảnh báo: Cận date nguy cấp (<= 30 ngày), Cận date (31 - 60 ngày) | `BaoCaoThongKeDialog.taoTabCanhBaoFEFO` | UI Test | Visual Warning | - |
| **TC-BC-016** | Nhận diện các lô hàng ĐÃ HẾT HẠN SỬ DỤNG còn tồn trong kho | `BaoCaoThongKeDAO.getLoHangFefo` | Integration / UI | Safety Audit | - |
| **TC-BC-017** | Kiểm tra nguy cơ sập giao diện NullPointerException khi Ngày sản xuất bị NULL | `BaoCaoThongKeDAO.java:314` | Unit / Integration | Critical Defect | **DEF-11 / DEF-CRI-10** |
| **TC-BC-018** | Báo cáo Xuất - Nhập - Tồn - Đối soát độc lập tồn kho thực tế LoHang (Thay thế Tautology) | `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO` | Integration / Audit | Reconcile / Audit | - |
| **TC-BC-019** | Báo cáo Xuất - Nhập - Tồn - Lỗi lấy tồn thời gian thực làm sai lệch tồn lịch sử | `BaoCaoThongKeDAO.java:327, 357` | Unit / Integration | Blocker Defect | **DEF-BC-01 / DEF-BLK-03** |
| **TC-BC-020** | Kiểm tra tính toán Giá trị tồn kho theo giá vốn nhập cơ sở | `BaoCaoThongKeDAO.getXuatNhapTon` | Integration / Financial | Valuation Audit | - |
| **TC-BC-021** | Báo cáo Khách hàng VIP và thống kê thẻ dịch vụ Spa Pass | `BaoCaoThongKeDialog` (Tab 3) | Integration / UI | Happy Path | - |
| **TC-BC-022** | Báo cáo Khách sạn Pet Hotel lưu trú và tổng tiền hoàn trả đổi trả hàng | `BaoCaoThongKeDialog` (Tab 4) | Integration / UI | Happy Path | - |
| **TC-BC-023** | Tính năng xuất báo cáo đang xem ra tệp CSV hỗ trợ mở bằng Microsoft Excel | `BaoCaoThongKeDialog.xuatBaoCaoRaCsv` | UI / File I/O | Export Feature | - |
| **TC-BC-024** | Tính năng in ấn trực tiếp bảng báo cáo đang xem (Java Print Service) | `BaoCaoThongKeDialog.inBaoCaoDangXem` | UI Test | Print Feature | - |

---

## 2. TỪ ĐIỂN DỮ LIỆU KIỂM THỬ BÁO CÁO (REPORT TEST DATA DICTIONARY)

Dữ liệu hóa đơn, xuất lô và phân ca trong CSDL phục vụ đối soát số liệu thống kê:

| Đối Tượng | Mã / Định Danh | Thông Tin Nghiệp Vụ / Chi Tiết | Giá Trị Tài Chính | Ghi Chú Kiểm Thử |
|---|---|---|---|---|
| **Hóa đơn 1** | `HD261103-01` | Mua Thức ăn Hạt Royal Canin (SP001), thanh toán Tiền mặt | Thanh tiền: 350.000 đ (Giảm giá: 0 đ) | Doanh thu Hàng hóa thuần |
| **Hóa đơn 2** | `HD261103-02` | Khách VIP An mua Pate Whiskas (SP002) + Cắt tỉa lông (DV001) | Hàng: 240.000 đ, DV: 300.000 đ, Giảm VIP: 27.000 đ | Hóa đơn hỗn hợp Hàng + Dịch vụ |
| **Hóa đơn 3** | `HD261104-01` | Khách Mai thanh toán Chuyển khoản VietQR | Thanh tiền: 1.200.000 đ (Chuyển khoản) | Kiểm tra doanh thu VietQR |
| **Phiếu Đổi trả**| `DT261104-01` | Hoàn trả 1 túi Cát vệ sinh cho khách Mai | Tiền hoàn lại: 65.000 đ | Kiểm tra trừ doanh thu / hoàn trả |
| **Lô hàng FEFO**| `LO001` (SP001) | HSD: 15 ngày tới (Cận date nguy cấp) | Tồn kho: 5 gói | Cảnh báo badge ĐỎ |
| **Lô hàng FEFO**| `LO002` (SP002) | HSD: 45 ngày tới (Cận date cảnh báo) | Tồn kho: 20 lon | Cảnh báo badge VÀNG |
| **Lô hàng hết hạn**| `LO_HETHAN` | HSD: Hôm qua (Quá hạn) | Tồn kho: 2 hộp | Cảnh báo badge HẾT HẠN |
| **Phân ca** | `CA01`, `CA02` | Phân công NV001, NV003, NV004 | Ca trực cố định trong ngày | Kiểm tra đếm số ca trực |

---

## 3. ĐẶC TẢ CHI TIẾT CÁC KỊCH BẢN KIỂM THỬ (TEST CASE SPECIFICATIONS)

### Nhóm 1: Dashboard Tổng Quan Điều Hành (TC-DASH)

#### TC-DASH-001: Hiển thị 4 thẻ chỉ số KPI hôm nay (Doanh thu, Đơn hàng, Lịch hẹn, Phòng Hotel)
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel` (Chế độ Vận hành - Operational View)
- **Cấp độ kiểm thử:** UI Test / Integration Test
- **Mục tiêu kiểm thử:** Xác minh 4 thẻ KPI đầu trang hiển thị chính xác các chỉ số tổng hợp trong ngày hiện tại: Doanh thu hôm nay (kèm phân tích Spa vs Hàng hóa), Tổng số đơn hàng (phân tích Bán lẻ vs Dịch vụ), Lịch hẹn hôm nay (chờ phục vụ / đã hoàn thành), và Tỷ lệ lấp đầy phòng Pet Hotel.
- **Điều kiện tiên quyết:** Đăng nhập tài khoản Quản lý `NV001`; hệ thống đã phát sinh hóa đơn bán hàng trong ngày.
- **Các bước thực hiện:**
  1. Mở màn hình `TongQuanPanel` từ menu sidebar.
  2. Quan sát 4 thẻ KPI nổi bật trên hàng đầu tiên.
  3. Đối chiếu số liệu hiển thị trên Thẻ 1 (`lblDoanhThuGiaTri`) với tổng tiền các hóa đơn trong ngày.
  4. Đối chiếu số đơn trên Thẻ 2 (`lblHoaDonGiaTri`) với số lượng hóa đơn phát sinh.
  5. Đối chiếu Thẻ 3 (Lịch hẹn) và Thẻ 4 (Phòng Hotel).
- **Dữ liệu kiểm thử:** Ngày hiện tại (`LocalDate.now()`).
- **Kết quả mong đợi:**
  - Thẻ 1: Hiển thị định dạng tiền tệ Việt Nam (ví dụ `1.850.000 đ`), dòng chú thích hiển thị rõ `Spa: xxx • Hàng hóa: yyy`.
  - Thẻ 2: Hiển thị định dạng `X đơn`, dòng chú thích `A đơn bán lẻ | B đơn dịch vụ`.
  - Thẻ 3: Hiển thị số lịch hẹn hôm nay.
  - Thẻ 4: Hiển thị số phòng đang có khách / tổng số phòng (ví dụ `3/10 phòng`).
- **Tiêu chí Đạt/Không đạt:** Đạt nếu 4 thẻ KPI hiển thị đầy đủ, số liệu khớp với giao dịch và định dạng đẹp mắt không vỡ giao diện.
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra doanh thu thực tế trong ngày từ CSDL
  SELECT 
      COUNT(*) AS TongDonHomNay,
      ISNULL(SUM(thanhTien), 0) AS DoanhThuHomNay
  FROM HoaDon
  WHERE trangThaiHoaDon = N'Đã thanh toán'
    AND CAST(COALESCE(thoiGianThanhToan, ngayLap) AS DATE) = CAST(GETDATE() AS DATE);
  ```

---

#### TC-DASH-002: Hiển thị biểu đồ cột BarChartPanel - Doanh thu 7 ngày gần nhất
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel`, `gui.BarChartPanel`
- **Cấp độ kiểm thử:** UI Test / Integration Test
- **Mục tiêu kiểm thử:** Xác minh component đồ họa tùy biến `BarChartPanel` vẽ biểu đồ cột doanh thu 7 ngày liên tục chính xác bằng Graphics2D; có đầy đủ nhãn các ngày, giá trị trên đỉnh cột, và đường trục tọa độ.
- **Điều kiện tiên quyết:** CSDL có dữ liệu bán hàng trong 7 ngày qua.
- **Các bước thực hiện:**
  1. Tại `TongQuanPanel`, cuộn đến khu vực "Biểu đồ Doanh thu 7 ngày gần nhất".
  2. Kiểm tra số lượng cột hiển thị (phải đủ đúng 7 cột đại diện cho 7 ngày từ `T-6` đến `Hôm nay`).
  3. Rê chuột hoặc quan sát nhãn ngày tháng dưới chân mỗi cột (`dd/MM`).
  4. Kiểm tra độ cao của các cột phản ánh tỷ lệ doanh thu tương ứng.
- **Dữ liệu kiểm thử:** Khoảng ngày: `[LocalDate.now().minusDays(6), LocalDate.now()]`.
- **Kết quả mong đợi:**
  - Biểu đồ hiển thị sắc nét, khử răng cưa (Anti-aliasing).
  - Cột có doanh thu cao nhất đạt gần đỉnh biểu đồ; ngày không có doanh thu hiển thị cột rỗng hoặc mức 0.
  - Giá trị doanh thu trên đỉnh cột hiển thị rút gọn (ví dụ `1.2M`, `850k`).
- **Tiêu chí Đạt/Không đạt:** Đạt nếu biểu đồ vẽ đúng 7 cột, không bị méo mó, không văng ngoại lệ đồ họa.

---

#### TC-DASH-003: Hiển thị biểu đồ tròn DonutChartPanel - Cơ cấu ngành hàng & Công suất Pet Hotel
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel`, `gui.DonutChartPanel`
- **Cấp độ kiểm thử:** UI Test / Integration Test
- **Mục tiêu kiểm thử:** Xác minh component `DonutChartPanel` vẽ biểu đồ tròn khuyết tâm (donut) hiển thị chính xác tỷ trọng phần trăm cơ cấu ngành hàng (Spa, Thức ăn, Phụ kiện, Thuốc thú y) và tỷ lệ lấp đầy phòng Pet Hotel.
- **Điều kiện tiên quyết:** Đang mở màn hình `TongQuanPanel`.
- **Các bước thực hiện:**
  1. Quan sát khu vực Biểu đồ tròn Cơ cấu doanh thu ngành hàng.
  2. Kiểm tra các cung tròn tương ứng từng ngành hàng (màu sắc phân biệt, chú giải bên cạnh).
  3. Tổng phần trăm các phần tử trong biểu đồ donut phải cộng lại bằng đúng 100%.
- **Kết quả mong đợi:**
  - Biểu đồ donut hiển thị mượt mà với hiệu ứng gradient bo góc.
  - Khi một ngành hàng chiếm 0% thì không hiển thị cung màu đó.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu biểu đồ donut hiển thị đúng tỷ trọng cơ cấu.

---

#### TC-DASH-004: Chuyển đổi linh hoạt giữa 3 chế độ Dashboard (Vận hành, Chiến lược, Phân tích)
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel`
- **Cấp độ kiểm thử:** UI Test
- **Mục tiêu kiểm thử:** Kiểm tra chức năng chuyển đổi giữa 3 chế độ xem Dashboard bằng các nút chuyển view: Vận hành (Operational), Chiến lược (Strategic) và Phân tích (Analytical).
- **Điều kiện tiên quyết:** Đang mở `TongQuanPanel`.
- **Các bước thực hiện:**
  1. Click vào nút "Chiến lược" -> Quan sát giao diện chuyển sang hiển thị các chỉ số tài chính, lợi nhuận gộp, tỷ trọng dịch vụ vs hàng hóa.
  2. Click vào nút "Phân tích" -> Quan sát giao diện hiển thị biểu đồ phân tích xu hướng và so sánh tăng trưởng tuần trước.
  3. Click lại nút "Vận hành" -> Trở về giao diện điều hành ca trực và bảng lịch hẹn hôm nay.
- **Kết quả mong đợi:** CardLayout hoặc panel chuyển đổi mượt mà, không giật lag, giữ nguyên trạng thái dữ liệu.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu chuyển đổi chế độ xem trơn tru.

---

#### TC-DASH-005: Kiểm tra dữ liệu Dashboard khi mở lại ứng dụng (DEF-BC-07 / DEF-MIN-04)
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel`, `gui.BanHangPOSPanel`
- **Cấp độ kiểm thử:** Integration Test / Defect Verification (**DEF-BC-07 / DEF-MIN-04**)
- **Mục tiêu kiểm thử:** Kiểm tra việc mất dữ liệu trên Dashboard Tổng quan khi người dùng khởi động lại ứng dụng do hệ thống đọc số liệu doanh thu hôm nay từ biến bộ nhớ RAM của `BanHangPOSPanel` thay vì truy vấn CSDL SQL Server.
- **Điều kiện tiên quyết:** Trong ngày hôm nay đã có 2 hóa đơn được thanh toán thành công và lưu vào SQL Server (tổng tiền 1.500.000 đ).
- **Các bước thực hiện:**
  1. Tắt hoàn toàn ứng dụng (Đóng Java process).
  2. Khởi chạy lại ứng dụng bằng `run.bat`.
  3. Đăng nhập tài khoản Quản lý `NV001`.
  4. Mở ngay màn hình Dashboard `TongQuanPanel` (chưa mở và chưa bán hàng tại màn hình POS).
  5. Quan sát giá trị hiển thị tại thẻ "Doanh thu hôm nay" và "Số hóa đơn hôm nay".
- **Kết quả mong đợi theo ĐẶC TẢ ĐÚNG (Expected Specification):**
  - Thẻ Doanh thu hôm nay phải hiển thị `1.500.000 đ` (đọc trực tiếp từ CSDL `HoaDon`).
  - Thẻ Số hóa đơn phải hiển thị `2 đơn`.
- **Hành vi thực tế phát hiện lỗi (DEF-BC-07 / DEF-MIN-04 Evidence):**
  - Xem mã nguồn `TongQuanPanel.java:244-245`:
    ```java
    long doanhThuHomNay = BanHangPOSPanel.tinhTongDoanhThuHomNay();
    int tongDon = BanHangPOSPanel.demTongSoHoaDonHomNay();
    ```
  - Biến trong `BanHangPOSPanel` là biến RAM tĩnh mới khởi tạo nên có giá trị `0`.
  - Kết quả lỗi thực tế: Thẻ Doanh thu hiển thị `0 đ` và `0 đơn`, dù CSDL có đầy đủ hóa đơn hôm nay!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Xác nhận Dashboard hiển thị 0 đ sau khi restart ứng dụng (Xác nhận khiếm khuyết DEF-BC-07 / DEF-MIN-04).
- **Verification SQL Queries:**
  ```sql
  -- Kiểm tra doanh thu thực tế trong CSDL khác 0
  SELECT COUNT(*) AS SoDonThuc, ISNULL(SUM(thanhTien), 0) AS DoanhThuThuc
  FROM HoaDon
  WHERE trangThaiHoaDon = N'Đã thanh toán'
    AND CAST(COALESCE(thoiGianThanhToan, ngayLap) AS DATE) = CAST(GETDATE() AS DATE);
  ```

---

#### TC-DASH-006: Kiểm tra tính toán lợi nhuận Dashboard Chiến lược (DEF-BC-05 / DEF-MAJ-08)
- **Phân hệ & Màn hình:** Báo cáo / `gui.TongQuanPanel.java:363, 365`
- **Cấp độ kiểm thử:** Unit Test / Code Audit & Defect Verification (**DEF-BC-05 / DEF-MAJ-08**)
- **Mục tiêu kiểm thử:** Kiểm tra hiện tượng gán cứng tỷ suất lợi nhuận giả lập 42% (`Math.round(doanhThuHomNay * 0.42)`) trong Chế độ Chiến lược của Dashboard khi dữ liệu giá vốn chưa phát sinh hoặc gặp lỗi.
- **Điều kiện tiên quyết:** Xem xét mã nguồn và kịch bản thực thi của `TongQuanPanel`.
- **Các bước thực hiện:**
  1. Tạo 1 hóa đơn bán hàng hôm nay chỉ gồm tiền dịch vụ (Spa) 100.000 đ (chưa có giá vốn hàng hóa).
  2. Mở `TongQuanPanel`, chuyển sang tab "Chiến lược" (Strategic View).
  3. Quan sát nhãn Lợi nhuận ước tính (`lblStratLoiNhuan`).
- **Kết quả mong đợi theo ĐẶC TẢ CHUẨN:**
  - Lợi nhuận gộp phải được tính đúng bằng `Doanh thu - Giá vốn` dựa trên dữ liệu kế toán thực tế của bảng `ChiTietXuatLo` và `ChiTietDichVu`.
- **Hành vi thực tế phát hiện lỗi (DEF-BC-05 / DEF-MAJ-08 Evidence):**
  - Tại `TongQuanPanel.java:363`:
    ```java
    loiNhuan = tongDT > 0 ? (tongDT - tongGV) : Math.round(doanhThuHomNay * 0.42);
    ```
  - Và tại dòng 365 trong khối `catch`:
    ```java
    loiNhuan = Math.round(doanhThuHomNay * 0.42);
    ```
  - Kết quả lỗi thực tế: Số liệu lợi nhuận bị nhân cứng với hệ số giả định 42%, không phản ánh chính xác kết quả kinh doanh thực tế.
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Xác nhận sự tồn tại của phép gán cứng `0.42` trong code (Xác nhận lỗi DEF-BC-05 / DEF-MAJ-08).

---

### Nhóm 2: Báo Cáo Doanh Thu & Lợi Nhuận (Tab 0 - TC-BC)

#### TC-BC-001: Báo cáo Doanh thu & Lợi nhuận - Lọc theo kỳ "Hôm nay"
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 0: "Doanh thu & Lợi nhuận")
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh bảng báo biểu chi tiết Doanh thu & Lợi nhuận nạp đầy đủ danh sách các hóa đơn đã thanh toán trong ngày hôm nay; tính đúng các cột: Mã HĐ, Thời gian, Khách hàng, Thu ngân, Tiền hàng, Giảm giá, Doanh thu thuần, Tiền hàng, Tiền dịch vụ, Giá vốn, và Phương thức thanh toán.
- **Điều kiện tiên quyết:** CSDL có ít nhất 2 hóa đơn phát sinh trong ngày hôm nay.
- **Các bước thực hiện:**
  1. Trên `MainDashboard`, click nút "Trung tâm Báo cáo" (hoặc phím tắt).
  2. Hộp thoại `BaoCaoThongKeDialog` xuất hiện.
  3. Tại combobox `cboKyBaoCao`, chọn: `"Hôm nay (dd/MM/yyyy)"`.
  4. Tab 0 ("Doanh thu & Lợi nhuận") được hiển thị mặc định.
  5. Kiểm tra bảng dữ liệu và dòng tổng cộng ở cuối bảng.
- **Dữ liệu kiểm thử:** `tuNgay = LocalDate.now()`, `denNgay = LocalDate.now()`.
- **Kết quả mong đợi:**
  - Hiển thị danh sách các hóa đơn có `trangThaiHoaDon = N'Đã thanh toán'` trong ngày.
  - Các hóa đơn đã hủy hoặc đang mở không xuất hiện trong danh sách.
  - Dòng tổng cộng ở chân bảng tính đúng `SUM(DoanhThuThuan)`, `SUM(GiaVon)`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu dữ liệu khớp 100% với truy vấn CSDL.
- **Verification SQL Queries:**
  ```sql
  DECLARE @tu DATETIME2 = CAST(CAST(GETDATE() AS DATE) AS DATETIME2);
  DECLARE @den DATETIME2 = DATEADD(day, 1, @tu);
  
  SELECT h.maHoaDon, COALESCE(h.thoiGianThanhToan, h.ngayLap) AS thoiGian,
         COALESCE(kh.hoTen, N'Khách vãng lai') AS khachHang,
         COALESCE(nv.hoTen, N'Không xác định') AS thuNgan,
         h.thanhTien AS doanhThuThuan,
         h.phuongThucThanhToan
  FROM HoaDon h
  LEFT JOIN KhachHang kh ON kh.maKhachHang = h.maKhachHang
  LEFT JOIN NhanVien nv ON nv.maNhanVien = h.maNhanVien
  WHERE h.trangThaiHoaDon = N'Đã thanh toán'
    AND COALESCE(h.thoiGianThanhToan, h.ngayLap) >= @tu
    AND COALESCE(h.thoiGianThanhToan, h.ngayLap) < @den
  ORDER BY thoiGian DESC;
  ```

---

#### TC-BC-002: Báo cáo Doanh thu & Lợi nhuận - Lọc "7 ngày gần nhất" và "Tháng hiện tại"
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 0)
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh việc chuyển đổi các mốc thời gian báo cáo ("7 ngày gần nhất", "Tháng hiện tại", "Quý hiện tại") cập nhật chính xác danh sách hóa đơn trong kỳ.
- **Điều kiện tiên quyết:** CSDL có dữ liệu bán hàng rải rác trong tháng.
- **Các bước thực hiện:**
  1. Tại `cboKyBaoCao`, chọn `"7 ngày gần nhất"`.
  2. Quan sát bảng tự động làm mới và kiểm tra cột `Thời gian` nằm trong khoảng `[Today - 6, Today]`.
  3. Tiếp tục chọn `"Tháng X/yyyy"`.
  4. Kiểm tra toàn bộ hóa đơn từ ngày mùng 1 đầu tháng đến hiện tại được nạp lên bảng.
- **Kết quả mong đợi:** Bảng làm mới mượt mà, số dòng tăng lên tương ứng với số hóa đơn trong kỳ mở rộng.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu lọc đúng khoảng ngày.

---

#### TC-BC-003: Phân loại doanh thu theo phương thức thanh toán (Tiền mặt vs VietQR)
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.getDoanhThu`
- **Cấp độ kiểm thử:** Integration Test / Financial Audit
- **Mục tiêu kiểm thử:** Xác minh báo cáo phân định rạch ròi giữa doanh thu thu bằng "Tiền mặt" và doanh thu chuyển khoản "Chuyển khoản" (VietQR), hỗ trợ thu ngân đối soát két tiền cuối ca.
- **Điều kiện tiên quyết:** Có hóa đơn thanh toán Tiền mặt và hóa đơn thanh toán VietQR.
- **Các bước thực hiện:**
  1. Kiểm tra cột `Phương thức` trên bảng báo cáo Doanh thu.
  2. Lọc tổng số tiền thanh toán bằng Tiền mặt.
  3. Lọc tổng số tiền thanh toán bằng Chuyển khoản VietQR.
- **Kết quả mong đợi:**
  - Hóa đơn Tiền mặt hiển thị đúng nhãn `Tiền mặt`.
  - Hóa đơn VietQR hiển thị đúng nhãn `Chuyển khoản`.
  - Không có giá trị `NULL` hoặc `Không xác định`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu phân loại phương thức chính xác 100%.
- **Verification SQL Queries:**
  ```sql
  SELECT phuongThucThanhToan, COUNT(*) AS SoLuongDon, SUM(thanhTien) AS TongTien
  FROM HoaDon
  WHERE trangThaiHoaDon = N'Đã thanh toán'
  GROUP BY phuongThucThanhToan;
  ```

---

#### TC-BC-004: Kiểm tra tính cân đối tài chính trên từng dòng hóa đơn
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.java:120-150`
- **Cấp độ kiểm thử:** Unit Test / Integration Test
- **Mục tiêu kiểm thử:** Xác minh tính cân bằng kế toán trên từng dòng bản ghi báo cáo:
  1. `Tiền hàng - Giảm giá = Doanh thu thuần`.
  2. `Doanh thu hàng hóa + Doanh thu dịch vụ = Doanh thu thuần`.
  3. `Lợi nhuận gộp = Doanh thu thuần - Giá vốn`.
- **Điều kiện tiên quyết:** Báo cáo Doanh thu đã tải các dòng dữ liệu.
- **Các bước thực hiện:**
  1. Lấy danh sách `List<DoanhThuRow>` từ `ThongKeBus.layBaoCaoDoanhThu()`.
  2. Duyệt qua từng dòng `DoanhThuRow` và thực hiện các phép assert kiểm tra logic.
- **Dữ liệu kiểm thử:** Mọi dòng hóa đơn trong kỳ.
- **Kết quả mong đợi:**
  - `r.tienHang - r.giamGia == r.doanhThuThuan`.
  - `r.doanhThuHangHoa + r.doanhThuDichVu == r.doanhThuThuan`.
  - Sai số phân bổ tỷ trọng làm tròn không vượt quá 1 đồng do phép làm tròn `Math.round()`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu 100% các dòng đều thỏa mãn phương trình kế toán.

---

#### TC-BC-005: Kiểm tra bỏ sót giao dịch ở giây cuối ngày 23:59:59 (DEF-BC-06 / DEF-MIN-03)
- **Phân hệ & Màn hình:** Báo cáo / `bus.ThongKeBus.java:36`, `dao.BaoCaoThongKeDAO.java:130`
- **Cấp độ kiểm thử:** Integration Test / Boundary Defect Verification (**DEF-BC-06 / DEF-MIN-03**)
- **Mục tiêu kiểm thử:** Kiểm tra hiện tượng thất thoát giao dịch phát sinh ở giây cuối cùng của ngày báo cáo (`23:59:59.001` đến `23:59:59.999`) do `ThongKeBus` ép mốc `denNgay.atTime(23, 59, 59)` kết hợp với toán tử SQL `< ?`.
- **Điều kiện tiên quyết:** Có 1 hóa đơn thanh toán lúc `23:59:59` của ngày `07/11/2026`.
- **Các bước thực hiện:**
  1. Chèn một hóa đơn mẫu có thời gian thanh toán `2026-11-07 23:59:59.500`.
  2. Mở báo cáo Doanh thu cho ngày `07/11/2026` qua `ThongKeBus.layBaoCaoDoanhThu(LocalDate.of(2026, 11, 7), LocalDate.of(2026, 11, 7))`.
  3. Kiểm tra xem hóa đơn trên có xuất hiện trong danh sách kết quả trả về không.
- **Kết quả mong đợi theo ĐẶC TẢ ĐÚNG:**
  - Hóa đơn phát sinh trong ngày 07/11 phải được tính đầy đủ vào báo cáo của ngày 07/11. Mốc thời gian kết thúc chuẩn phải là đầu ngày tiếp theo (`2026-11-08 00:00:00.000`).
- **Hành vi thực tế phát hiện lỗi (DEF-BC-06 / DEF-MIN-03 Evidence):**
  - Tại `ThongKeBus.java:36`: `denNgay.atTime(23, 59, 59)`.
  - Tham số truyền vào PreparedStatement là `2026-11-07 23:59:59.000`.
  - Câu lệnh SQL: `WHERE thoiGian < '2026-11-07 23:59:59.000'`.
  - Kết quả lỗi thực tế: Hóa đơn lúc `23:59:59` bị loại bỏ hoàn toàn khỏi báo cáo doanh thu của ngày đó!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Hóa đơn giây cuối ngày bị mất khỏi báo cáo (Xác nhận lỗi DEF-BC-06 / DEF-MIN-03).
- **Verification SQL Queries:**
  ```sql
  -- Giả lập kiểm tra với tham số của ThongKeBus
  DECLARE @tu DATETIME2 = '2026-11-07 00:00:00';
  DECLARE @den_loi DATETIME2 = '2026-11-07 23:59:59'; -- Code hiện tại
  DECLARE @den_dung DATETIME2 = '2026-11-08 00:00:00'; -- Chuẩn kỹ thuật
  
  -- Quan sát sự chênh lệch bản ghi giữa @den_loi và @den_dung
  SELECT maHoaDon, thoiGianThanhToan
  FROM HoaDon
  WHERE thoiGianThanhToan >= @tu AND thoiGianThanhToan < @den_dung
    AND thoiGianThanhToan >= @den_loi;
  ```

---

#### TC-BC-006: Báo cáo Doanh thu trong kỳ không phát sinh giao dịch nào (Empty Set)
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDialog`, `BaoCaoThongKeDAO`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh khi chọn khoảng thời gian không có hóa đơn nào (ví dụ một ngày trong quá khứ chưa mở cửa hàng), hệ thống hiển thị bảng rỗng mượt mà, không văng lỗi `NullPointerException` hay lỗi chia cho 0.
- **Điều kiện tiên quyết:** Ngày `01/01/2020` không có dữ liệu bán hàng.
- **Các bước thực hiện:**
  1. Truy vấn báo cáo với `tuNgay = 01/01/2020`, `denNgay = 01/01/2020`.
- **Kết quả mong đợi:**
  - Danh sách trả về rỗng (`rows.size() == 0`).
  - Giao diện hiển thị bảng không có dòng nào, dòng tổng cộng hiển thị `0 đ`.
  - Ứng dụng không bị treo hay văng crash exception.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu xử lý ngoại lệ biên rỗng an toàn.

---

### Nhóm 3: Báo Cáo Top Bán Chạy (Tab 1 - TC-BC)

#### TC-BC-007: Báo cáo Top Bán Chạy - Xếp hạng theo Doanh thu và Số lượng
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 1: "Top Bán chạy")
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh Tab 1 thống kê danh sách các mặt hàng và dịch vụ bán chạy nhất, sắp xếp giảm dần theo doanh thu (`doanhThu DESC, soLuong DESC`); hiển thị đầy đủ: Mã, Tên mặt hàng/dịch vụ, Phân nhóm, Đơn vị tính, Số lượng bán, và Doanh thu thu được.
- **Điều kiện tiên quyết:** CSDL có dữ liệu bán hàng và dịch vụ.
- **Các bước thực hiện:**
  1. Mở `BaoCaoThongKeDialog`, click chuyển sang Tab 1: "Top Bán chạy".
  2. Quan sát thứ tự các dòng trong bảng.
  3. Kiểm tra dòng đầu tiên phải là mặt hàng có doanh thu cao nhất.
- **Kết quả mong đợi:** Dữ liệu sắp xếp đúng theo doanh thu giảm dần; phân nhóm rõ ràng "Hàng hóa" hoặc "Dịch vụ".
- **Tiêu chí Đạt/Không đạt:** Đạt nếu xếp hạng đúng.
- **Verification SQL Queries:**
  ```sql
  SELECT TOP 5 
      CASE WHEN ct.loaiChiTiet LIKE '%DICH_VU%' THEN dv.maDichVu ELSE l.maSanPham END AS Ma,
      SUM(ct.soLuong) AS SoLuong,
      SUM(ct.thanhTien) AS DoanhThu
  FROM ChiTietHoaDon ct
  LEFT JOIN ChiTietXuatLo xl ON xl.maChiTiet = ct.maChiTiet
  LEFT JOIN LoHang l ON l.soLo = xl.soLo
  LEFT JOIN ChiTietDichVu dv ON dv.maChiTiet = ct.maChiTiet
  GROUP BY CASE WHEN ct.loaiChiTiet LIKE '%DICH_VU%' THEN dv.maDichVu ELSE l.maSanPham END
  ORDER BY DoanhThu DESC;
  ```

---

#### TC-BC-008: Báo cáo Top Bán Chạy - Bỏ qua tỷ lệ quy đổi khi cộng sản lượng (DEF-BC-02 / DEF-CRI-05)
- **Phân hệ & Màn hình:** Báo cáo / `dao.BaoCaoThongKeDAO.java:165, 177-181`
- **Cấp độ kiểm thử:** Unit Test / Business Logic Defect Verification (**DEF-BC-02 / DEF-CRI-05**)
- **Mục tiêu kiểm thử:** Kiểm tra khiếm khuyết nghiêm trọng trong báo cáo Top Bán Chạy: Câu lệnh SQL cộng gộp trực tiếp `SUM(ct.soLuong)` mà **KHÔNG nhân với `tyLeQuyDoi`**, dẫn đến việc khách mua 1 Thùng (24 lon) chỉ được đếm là số lượng 1, khiến sản phẩm bán sỉ theo thùng/lốc bị tụt hạng sản lượng nghiêm trọng.
- **Điều kiện tiên quyết:** Sản phẩm `SP002` (Pate Whiskas) có đơn vị cơ sở là Lon, quy đổi Thùng 24 lon (`tyLeQuyDoi = 24`).
- **Các bước thực hiện:**
  1. Tạo Hóa đơn A: Khách mua 1 Thùng Pate 24 lon (`soLuong = 1`, đơn vị Thùng).
  2. Tạo Hóa đơn B: Khách mua 3 lon Pate lẻ (`soLuong = 3`, đơn vị Lon).
  3. Chạy báo cáo Top Bán Chạy qua `BaoCaoThongKeDAO.getTopBanChay()`.
  4. Quan sát giá trị cột `Số lượng bán` của sản phẩm `SP002`.
- **Kết quả mong đợi theo ĐẶC TẢ KHO & BÁN HÀNG:**
  - Tổng sản lượng tiêu thụ phải được quy về đơn vị cơ sở: `1 * 24 + 3 = 27 lon`.
- **Hành vi thực tế phát hiện lỗi (DEF-BC-02 / DEF-CRI-05 Evidence):**
  - Tại `BaoCaoThongKeDAO.java:165`: `SUM(ct.soLuong) soLuong`.
  - Phép tính thực tế: `1 + 3 = 4`! (Cộng 1 thùng với 3 lon ra 4).
  - Kết quả lỗi thực tế: Sản phẩm tiêu thụ 27 lon thực tế nhưng chỉ được báo cáo là 4 đơn vị, làm sai lệch hoàn toàn thống kê nhu cầu thị trường.
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Xác nhận sản lượng bị tính thành 4 thay vì 27 (Xác nhận lỗi DEF-BC-02 / DEF-CRI-05).

---

#### TC-BC-009: Phân loại cơ cấu mặt hàng: Hàng hóa vs Dịch vụ
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.getTopBanChay`
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh cột `Phân nhóm` phân định đúng giữa `Hàng hóa` và `Dịch vụ`, cột `Đơn vị tính` hiển thị đúng tên ĐVT cho hàng hóa và chữ `Lượt` cho dịch vụ.
- **Điều kiện tiên quyết:** CSDL có phát sinh cả bán sản phẩm và làm dịch vụ cắt tỉa/tắm sấy.
- **Các bước thực hiện:** Kiểm tra các dòng trên Tab 1 của `BaoCaoThongKeDialog`.
- **Kết quả mong đợi:** Dịch vụ (DV001, DV002) có phân nhóm là "Dịch vụ" và ĐVT là "Lượt"; Hàng hóa (SP001, SP002) có phân nhóm là "Hàng hóa" và ĐVT là "Gói", "Lon", "Hộp".
- **Tiêu chí Đạt/Không đạt:** Đạt nếu phân nhóm và ĐVT hiển thị chính xác.

---

### Nhóm 4: Báo Cáo Hiệu Suất KTV & Nhân Sự (Tab 2 - TC-BC)

#### TC-BC-010: Báo cáo Hiệu suất nhân viên trong khoảng thời gian 7 ngày hoặc 1 tháng (Phát hiện lỗi nuốt ca trực ngày cuối - DEF-MAJ-06)
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 2: "Hiệu suất KTV & Nhân sự"), `dao.BaoCaoThongKeDAO.java:203`
- **Cấp độ kiểm thử:** Integration Test / Boundary Defect Analysis (**DEF-MAJ-06**)
- **Mục tiêu kiểm thử:** Xác minh Tab 2 hiển thị đầy đủ danh sách nhân viên trong hệ thống kèm các chỉ số: Mã NV, Họ tên, Chức vụ, Trạng thái hoạt động, Số ca trực, Số hóa đơn đã lập, Số lượt dịch vụ thực hiện, Doanh thu đóng góp, và Tiền hoa hồng tích lũy. Đồng thời kiểm chứng khiếm khuyết ngầm nguy hiểm: toán tử `< CAST(? AS date)` khiến các truy vấn đa ngày (ví dụ 7 ngày gần nhất, 30 ngày, tháng hiện tại) **âm thầm nuốt chửng toàn bộ số ca trực phát sinh trong ngày cuối cùng của kỳ báo cáo** (`denNgay`).
- **Phân tích bản chất khiếm khuyết (DEF-MAJ-06 trong truy vấn đa ngày):**
  - Trong `BaoCaoThongKeDAO.java:203`:
    ```sql
    WHERE ngayLamViec >= CAST(? AS date) AND ngayLamViec < CAST(? AS date)
    ```
  - Khi người dùng chọn khoảng thời gian 7 ngày (ví dụ từ `2026-11-01` đến `2026-11-07`):
    - `CAST('2026-11-07' AS date)` trả về giá trị ngày `'2026-11-07'`.
    - Điều kiện lọc trở thành: `ngayLamViec >= '2026-11-01' AND ngayLamViec < '2026-11-07'`.
    - Kết quả: Toàn bộ các ca trực diễn ra trong ngày `2026-11-07` đều có `ngayLamViec = '2026-11-07'`, không thỏa mãn `< '2026-11-07'`, do đó **100% ca trực của ngày cuối cùng bị bỏ sót hoàn toàn**!
  - *Mức độ nguy hại:* Khác với `TC-BC-011` (xem 1 ngày hiển thị toàn bộ số 0 dễ nhận biết), khi xem 7 ngày hay 30 ngày, cột "Số ca trực" vẫn hiển thị các con số dương (của ngày 1 đến ngày 6). Quản lý rất dễ bị đánh lừa rằng số liệu đã chính xác, trong khi thực tế số ca trực của ngày Chủ nhật/ngày cuối kỳ đã bị thất thoát khỏi báo cáo đánh giá KPI!
- **Điều kiện tiên quyết:** CSDL có bảng phân công ca trực từ ngày `01/11/2026` đến ngày `07/11/2026`, trong đó ngày `07/11/2026` có ít nhất 4 ca trực của các nhân viên.
- **Các bước thực hiện:**
  1. Mở `BaoCaoThongKeDialog`, chọn kỳ "7 ngày gần nhất" (ví dụ `01/11/2026` - `07/11/2026`).
  2. Chuyển sang Tab 2: "Hiệu suất KTV & Nhân sự".
  3. Ghi nhận số ca trực hiển thị trên giao diện của nhân viên `NV003` và `NV004`.
  4. Chạy câu truy vấn kiểm chứng độc lập trên CSDL đếm số ca thực tế cả 7 ngày vs 6 ngày đầu.
- **Kết quả mong đợi theo ĐẶC TẢ ĐÚNG:**
  - Báo cáo phải thống kê đầy đủ toàn bộ 7 ngày (từ 00:00 ngày đầu đến hết 23:59:59 ngày cuối). Số ca trực phải bao gồm cả các ca trực ngày `07/11/2026`.
- **Hành vi thực tế phát hiện lỗi (Defect Confirmation):**
  - Số ca trực hiển thị trên giao diện bị hụt đúng bằng số ca mà nhân viên đó đã trực trong ngày cuối cùng (`07/11/2026`).
- **Tiêu chí Đạt/Không đạt:** *Đạt:* Xác nhận và ghi nhận chính xác lỗi bỏ sót ca trực ngày cuối **DEF-MAJ-06** trong báo cáo kiểm thử.
- **Verification SQL Queries:**
  ```sql
  DECLARE @tuDate DATE = '2026-11-01';
  DECLARE @denDate DATE = '2026-11-07';

  -- [1] Câu lệnh hiện tại trong DAO (Dùng toán tử < CAST(? AS date)) -> BỊ THIẾU NGÀY CUỐI
  SELECT pc.maNhanVien, COUNT(pc.maPhanCong) AS SoCa_BiThieuNgayCuoi
  FROM BangPhanCong pc
  WHERE pc.ngayLamViec >= @tuDate AND pc.ngayLamViec < @denDate
  GROUP BY pc.maNhanVien;

  -- [2] Câu lệnh chuẩn kỹ thuật (Dùng toán tử <= CAST(? AS date)) -> ĐẦY ĐỦ 7 NGÀY
  SELECT pc.maNhanVien, COUNT(pc.maPhanCong) AS SoCa_ChuanDu7Ngay
  FROM BangPhanCong pc
  WHERE pc.ngayLamViec >= @tuDate AND pc.ngayLamViec <= @denDate
  GROUP BY pc.maNhanVien;

  -- [3] Chỉ ra chính xác các ca trực ngày cuối bị nuốt chửng
  SELECT pc.maPhanCong, pc.maNhanVien, pc.maCa, pc.ngayLamViec
  FROM BangPhanCong pc
  WHERE pc.ngayLamViec = @denDate;
  ```

---

#### TC-BC-011: Báo cáo Hiệu suất nhân viên - Hiển thị 0 ca trực khi lọc 1 ngày (DEF-BC-03 / DEF-MAJ-06)
- **Phân hệ & Màn hình:** Báo cáo / `dao.BaoCaoThongKeDAO.java:203`, `bus.ThongKeBus.java:61-63`
- **Cấp độ kiểm thử:** Unit Test / Integration Test / Defect Verification (**DEF-BC-03 / DEF-MAJ-06**)
- **Mục tiêu kiểm thử:** Kiểm tra lỗi logic trong câu lệnh SQL tính số ca trực: Mệnh đề `WHERE ngayLamViec >= CAST(? AS date) AND ngayLamViec < CAST(? AS date)` khi xem báo cáo trong 1 ngày (Hôm nay) khiến cả hai mốc thời gian sau khi ép kiểu đều bằng cùng một ngày, dẫn đến **Cột "Số ca trực" của toàn bộ nhân viên luôn bằng 0**.
- **Điều kiện tiên quyết:** Bảng `BangPhanCong` có ít nhất 4 ca trực được phân công cho ngày hôm nay (`CAST(GETDATE() AS DATE)`).
- **Các bước thực hiện:**
  1. Mở `BaoCaoThongKeDialog`, chọn kỳ báo cáo: `"Hôm nay (dd/MM/yyyy)"`.
  2. Chuyển sang Tab 2: "Hiệu suất KTV & Nhân sự".
  3. Quan sát cột "Số ca trực" của nhân viên Lê Anh Minh (`NV003`) và Võ Thị Hồng Ngọc (`NV004`).
- **Kết quả mong đợi theo ĐẶC TẢ ĐÚNG:**
  - Cột Số ca trực phải hiển thị số lượng ca trực thực tế trong ngày hôm nay (ví dụ: NV003 có 2 ca trực sáng và tối -> phải hiển thị `2 ca`).
- **Hành vi thực tế phát hiện lỗi (DEF-BC-03 / DEF-MAJ-06 Evidence):**
  - Tại `BaoCaoThongKeDAO.java:203`:
    ```sql
    WITH PC AS (SELECT maNhanVien,COUNT(*) soCa FROM BangPhanCong WHERE ngayLamViec>=CAST(? AS date) AND ngayLamViec<CAST(? AS date) GROUP BY maNhanVien)
    ```
  - Khi xem ngày `07/11/2026`: `CAST('2026-11-07 00:00:00' AS date) = '2026-11-07'` và `CAST('2026-11-07 23:59:59' AS date) = '2026-11-07'`.
  - Mệnh đề `WHERE ngayLamViec >= '2026-11-07' AND ngayLamViec < '2026-11-07'` luôn vô nghiệm.
  - Kết quả lỗi thực tế: Cột "Số ca trực" của tất cả nhân viên đều hiển thị bằng `0`!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Số ca trực hiển thị bằng 0 dù CSDL có phân ca hôm nay (Xác nhận lỗi DEF-BC-03 / DEF-MAJ-06).
- **Verification SQL Queries:**
  ```sql
  -- Truy vấn kiểm chứng mệnh đề lỗi
  SELECT COUNT(*) AS SoCaBiLoi
  FROM BangPhanCong
  WHERE ngayLamViec >= CAST(GETDATE() AS DATE) AND ngayLamViec < CAST(GETDATE() AS DATE);
  -- Kết quả luôn là 0!
  
  -- Truy vấn chuẩn xác
  SELECT maNhanVien, COUNT(*) AS SoCaThucTe
  FROM BangPhanCong
  WHERE ngayLamViec = CAST(GETDATE() AS DATE)
  GROUP BY maNhanVien;
  ```

---

#### TC-BC-012: Kiểm tra lỗi tính trùng doanh thu giữa Thu ngân và KTV (DEF-BC-04 / DEF-MAJ-07)
- **Phân hệ & Màn hình:** Báo cáo / `dao.BaoCaoThongKeDAO.java:204-214`
- **Cấp độ kiểm thử:** Unit Test / Financial Defect Verification (**DEF-BC-04 / DEF-MAJ-07**)
- **Mục tiêu kiểm thử:** Kiểm tra lỗi tính trùng doanh thu dịch vụ 2 lần: Doanh thu của nhân viên được tính bằng `hd.doanhThu + dv.doanhThuDV`. Trong đó `hd.doanhThu` tính theo Thu ngân lập phiếu (đã bao gồm tiền dịch vụ), còn `dv.doanhThuDV` tính theo KTV làm dịch vụ. Dẫn đến việc tổng doanh thu các nhân viên cộng lại vượt xa tổng doanh thu thực tế của cửa hàng.
- **Điều kiện tiên quyết:** Có 1 hóa đơn thanh toán dịch vụ Spa 300.000 đ do Thu ngân `NV003` lập và KTV `NV004` thực hiện.
- **Các bước thực hiện:**
  1. Chạy báo cáo Hiệu suất nhân viên cho kỳ chứa hóa đơn trên.
  2. Lấy doanh thu của Thu ngân `NV003`.
  3. Lấy doanh thu của KTV `NV004`.
  4. Tính tổng doanh thu của tất cả nhân viên và so sánh với tổng doanh thu thực của cửa hàng.
- **Kết quả mong đợi theo ĐẶC TẢ TÀI CHÍNH:**
  - Tiền dịch vụ 300.000 đ chỉ được tính doanh số dịch vụ cho KTV `NV004` (hoặc tính hoa hồng), không được cộng gộp trùng vào doanh số bán hàng của Thu ngân, hoặc nếu phân tách thì tổng doanh số nhân viên phải bằng tổng doanh thu cửa hàng.
- **Hành vi thực tế phát hiện lỗi (DEF-BC-04 / DEF-MAJ-07 Evidence):**
  - CTE `HD`: Thu ngân `NV003` được cộng `300.000 đ` vào cột `doanhThu`.
  - CTE `DV`: KTV `NV004` được cộng `300.000 đ` vào cột `doanhThuDV`.
  - Phép cộng dòng 211: `COALESCE(hd.doanhThu,0) + COALESCE(dv.doanhThuDV,0)`.
  - Kết quả lỗi thực tế: Cửa hàng chỉ thu được 300.000 đ từ khách, nhưng tổng doanh thu nhân viên bị đội lên thành `600.000 đ`!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Tổng doanh thu nhân sự bị đội gấp đôi tiền dịch vụ (Xác nhận lỗi DEF-BC-04 / DEF-MAJ-07).

---

#### TC-BC-013: Kiểm tra tính toán hoa hồng thực tế cho Kỹ thuật viên Spa
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.getHieuSuatNhanSu`
- **Cấp độ kiểm thử:** Integration Test / Commission Audit
- **Mục tiêu kiểm thử:** Xác minh cột `Hoa hồng` tính đúng tổng số tiền hoa hồng thực tế được hưởng từ các dịch vụ spa mà KTV trực tiếp thực hiện (`SUM(dv.tienHoaHong)`).
- **Điều kiện tiên quyết:** Bảng `ChiTietDichVu` có ghi nhận trường `tienHoaHong` (ví dụ: 10% giá trị dịch vụ).
- **Các bước thực hiện:** Đối chiếu cột `Hoa hồng` của KTV `NV004` với bảng `ChiTietDichVu`.
- **Kết quả mong đợi:** Cột Hoa hồng bằng chính xác tổng tiền hoa hồng trong kỳ, không bị tính sai sang cho nhân viên khác.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu hoa hồng khớp số liệu kế toán.

---

### Nhóm 5: Báo Cáo Cảnh Báo Hạn Dùng FEFO (Tab 5 - TC-BC)

#### TC-BC-014: Báo cáo Cảnh báo FEFO - Danh sách sắp xếp theo Hạn sử dụng tăng dần
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 5: "Cảnh báo FEFO")
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh Tab 5 nạp danh sách toàn bộ các lô hàng còn tồn kho (`soLuongTon > 0`), tự động sắp xếp theo thứ tự hạn sử dụng tăng dần (`hanSuDung ASC, soLo ASC`) để phục vụ chiến lược xuất kho ưu tiên hàng cận hạn trước.
- **Điều kiện tiên quyết:** Bảng `LoHang` có nhiều lô với các mốc HSD khác nhau.
- **Các bước thực hiện:**
  1. Mở `BaoCaoThongKeDialog`, click chọn Tab 5: "Cảnh báo FEFO".
  2. Quan sát thứ tự các lô hàng hiển thị trên bảng.
- **Kết quả mong đợi:**
  - Lô có hạn sử dụng gần nhất (hoặc đã hết hạn) nằm ở các dòng trên cùng.
  - Các lô có hạn dùng xa hơn nằm ở phía dưới.
  - Cột `Số ngày còn lại` tính đúng `DATEDIFF(day, GETDATE(), hanSuDung)`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu sắp xếp chuẩn xác theo nguyên tắc FEFO.
- **Verification SQL Queries:**
  ```sql
  SELECT l.soLo, l.maSanPham, sp.tenSanPham, l.hanSuDung, l.soLuongTon,
         DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) AS soNgayConLai
  FROM LoHang l
  JOIN SanPham sp ON sp.maSanPham = l.maSanPham
  WHERE l.soLuongTon > 0
  ORDER BY l.hanSuDung ASC, l.soLo ASC;
  ```

---

#### TC-BC-015: Nhận diện và gắn badge cảnh báo FEFO trực quan
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog.taoTabCanhBaoFEFO`
- **Cấp độ kiểm thử:** UI Test / Visual Warning
- **Mục tiêu kiểm thử:** Xác minh cơ chế hiển thị badge cảnh báo màu sắc trực quan:
  - Lô cận hạn nguy cấp (<= 30 ngày): Nền ĐỎ cảnh báo (`CẬN DATE NGUY CẤP`).
  - Lô cận hạn cảnh báo (31 - 60 ngày): Nền VÀNG CAM (`CẬN DATE - ƯU TIÊN XUẤT`).
  - Lô bình thường (> 60 ngày): Nền XANH AN TOÀN.
- **Điều kiện tiên quyết:** Có các lô hàng mẫu thuộc cả 3 mức thời hạn trên.
- **Các bước thực hiện:** Quan sát cột "Trạng thái hạn dùng" trên Tab 5.
- **Kết quả mong đợi:** Màu sắc và nhãn cảnh báo hiển thị đúng phân loại số ngày còn lại.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu phân loại màu sắc chính xác.

---

#### TC-BC-016: Nhận diện các lô hàng ĐÃ HẾT HẠN SỬ DỤNG còn tồn trong kho
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.getLoHangFefo`
- **Cấp độ kiểm thử:** Integration Test / Safety Audit
- **Mục tiêu kiểm thử:** Xác minh hệ thống phát hiện chính xác các lô hàng có `hanSuDung < GETDATE()` nhưng vẫn còn tồn kho (`soLuongTon > 0`), hiển thị số ngày âm (`soNgayConLai < 0`) và gắn nhãn "ĐÃ HẾT HẠN SỬ DỤNG - CẤM BÁN".
- **Điều kiện tiên quyết:** Có lô hàng mẫu `LO_TEST_HETHAN` có HSD trước ngày hôm nay.
- **Các bước thực hiện:** Kiểm tra các dòng đầu tiên trên Tab 5.
- **Kết quả mong đợi:** Lô hết hạn được gắn nhãn cảnh báo nổi bật màu đỏ đậm để thủ kho thực hiện thủ tục tiêu hủy hoặc trả lại nhà cung cấp.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu nhận diện đầy đủ hàng hết hạn.

---

#### TC-BC-017: Kiểm tra lỗi crash NullPointerException khi Ngày sản xuất bị NULL (DEF-11 / DEF-CRI-10)
- **Phân hệ & Màn hình:** Báo cáo / `dao.BaoCaoThongKeDAO.java:314`
- **Cấp độ kiểm thử:** Unit Test / Integration Test / Defect Verification (**DEF-11 / DEF-CRI-10**)
- **Mục tiêu kiểm thử:** Kiểm tra lỗi crash giao diện do `NullPointerException`: Trong CSDL bảng `LoHang`, cột `ngaySanXuat` cho phép nhận giá trị `NULL`. Khi DAO đọc `rs.getDate("ngaySanXuat").toLocalDate()`, do đối tượng `Date` bị `null` nên việc gọi `.toLocalDate()` lập tức làm sập luồng nạp dữ liệu của Tab FEFO.
- **Điều kiện tiên quyết:** Chèn một lô hàng mẫu `LO_TEST_NULL_NSX` có `ngaySanXuat = NULL` và `hanSuDung = '2026-12-31'`.
- **Các bước thực hiện:**
  1. Mở CSDL và chèn lô hàng:
     ```sql
     INSERT INTO LoHang (soLo, maSanPham, ngaySanXuat, hanSuDung, soLuongNhap, soLuongTon)
     VALUES ('LO_TEST_NULL_NSX', 'SP001', NULL, '2026-12-31', 10, 10);
     ```
  2. Mở ứng dụng, vào `BaoCaoThongKeDialog`, click chuyển sang Tab 5: "Cảnh báo FEFO".
  3. Quan sát giao diện và kiểm tra log console/terminal.
- **Kết quả mong đợi theo ĐẶC TẢ AN TOÀN:**
  - Nếu `ngaySanXuat == null`, hệ thống phải hiển thị chuỗi rỗng `""` hoặc `"Không xác định"`, không được văng ngoại lệ.
- **Hành vi thực tế phát hiện lỗi (DEF-11 / DEF-CRI-10 Evidence):**
  - Tại `BaoCaoThongKeDAO.java:314`:
    ```java
    r.ngaySanXuat = rs.getDate("ngaySanXuat").toLocalDate();
    ```
  - Khi gặp `NULL`, Java ném: `java.lang.NullPointerException: Cannot invoke "java.sql.Date.toLocalDate()" because return value of "java.sql.ResultSet.getDate(String)" is null`.
  - Kết quả lỗi thực tế: Toàn bộ bảng Cảnh báo FEFO không thể hiển thị được dữ liệu, tab bị trắng xóa hoặc văng dialog lỗi!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Ngoại lệ NPE xuất hiện khi gặp `ngaySanXuat = NULL` (Xác nhận lỗi DEF-11 / DEF-CRI-10).
- **Cleanup Query:**
  ```sql
  DELETE FROM LoHang WHERE soLo = 'LO_TEST_NULL_NSX';
  ```

---

### Nhóm 6: Báo Cáo Xuất - Nhập - Tồn Kho (Tab 6 - TC-BC)

#### TC-BC-018: Báo cáo Xuất - Nhập - Tồn - Đối soát độc lập tồn kho thực tế LoHang (Thay thế khẳng định Tautology)
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 6: "Xuất - Nhập - Tồn"), `dao.BaoCaoThongKeDAO.java:327-360`
- **Cấp độ kiểm thử:** Integration Test / Financial & Inventory Audit
- **Mục tiêu kiểm thử:** Thay thế khẳng định luẩn quẩn (Tautological Assertion) bằng kịch bản đối soát độc lập (Independent Stock Reconciliation):
  - *Vấn đề với khẳng định cũ:* Phương trình `Tồn Đầu + Nhập - Xuất + Trả = Tồn Cuối` là một phép đồng nhất thức hình thức (Tautology), bởi vì trong mã nguồn `BaoCaoThongKeDAO.java:357`, hệ thống gán cứng: `tonDauKy = tonCuoiKy - nhap + xuat - tra`. Do đó, kiểm tra `tonDauKy + nhap - xuat + tra == tonCuoiKy` luôn luôn trả về `true` một cách vô nghĩa, ngay cả khi số liệu tồn kho bị sai lệch trầm trọng!
  - *Mục tiêu đối soát thực tế:*
    1. Giá trị `Tồn cuối kỳ` hiển thị trên bảng báo cáo của ngày hiện tại PHẢI khớp 100% với tổng số lượng tồn kho vật lý đang lưu trữ trong bảng `LoHang` (`SUM(soLuongTon)`).
    2. Kiểm tra tính toàn vẹn độc lập: Tổng lượng tồn kho vật lý phải được đối chiếu với toàn bộ lịch sử xuất nhập kho từ trước tới nay (`Tổng Nhập - Tổng Xuất + Tổng Trả == Tồn Vật Lý`).
- **Điều kiện tiên quyết:** CSDL có các sản phẩm đang có lô hàng trong bảng `LoHang` với số lượng tồn thực tế.
- **Các bước thực hiện:**
  1. Mở `BaoCaoThongKeDialog`, chọn kỳ báo cáo "Hôm nay" (hoặc kỳ kết thúc bằng ngày hôm nay).
  2. Click chuyển sang Tab 6: "Xuất - Nhập - Tồn".
  3. Ghi nhận các giá trị: `Mã sản phẩm`, `Tồn đầu kỳ`, `Nhập trong kỳ`, `Xuất trong kỳ`, `Hoàn trả trong kỳ`, và `Tồn cuối kỳ`.
  4. Thực thi câu lệnh SQL đối soát độc lập trực tiếp trên CSDL để kiểm tra `Tồn cuối kỳ` và kiểm toán phương trình kho thực tế độc lập với công thức gán trong code Java.
- **Kết quả mong đợi:**
  - `Tồn cuối kỳ` trên UI của từng sản phẩm phải bằng đúng `TonKhoVatLyThucTe` (`SUM(soLuongTon)`) trong bảng `LoHang`.
  - Không có bất kỳ sản phẩm nào có `TonKhoVatLyThucTe < 0`.
  - Số lượng `Xuất bán trong kỳ` trên UI phải khớp với `SUM(ChiTietXuatLo.soLuongXuatCoSo)` trong khoảng thời gian tương ứng.
- **Tiêu chí Đạt/Không đạt:** *Đạt:* Tồn cuối kỳ trên báo cáo khớp 100% với tồn vật lý thực tế trong CSDL `LoHang`, phát hiện và từ chối chấp nhận phép kiểm tra đồng nhất thức luẩn quẩn.
- **Verification SQL Queries (Tập lệnh đối soát độc lập không luẩn quẩn):**
  ```sql
  -- [1] Đối soát độc lập Tồn cuối kỳ trên báo cáo so với tồn vật lý thực tế trong LoHang
  SELECT 
      sp.maSanPham,
      sp.tenSanPham,
      ISNULL(SUM(lh.soLuongTon), 0) AS TonKhoVatLyThucTe_LoHang,
      ISNULL(nhapLuyKe.TongNhap, 0) AS TongNhapLuyKe,
      ISNULL(xuatLuyKe.TongXuat, 0) AS TongXuatLuyKe,
      ISNULL(traLuyKe.TongTra, 0) AS TongHoanTraLuyKe,
      (ISNULL(nhapLuyKe.TongNhap, 0) - ISNULL(xuatLuyKe.TongXuat, 0) + ISNULL(traLuyKe.TongTra, 0)) AS TonCanDoiLuyKe,
      -- Cờ kiểm toán: Nếu tồn vật lý khác tồn cân đối -> Cảnh báo sai lệch thẻ kho
      CASE 
          WHEN ISNULL(SUM(lh.soLuongTon), 0) = (ISNULL(nhapLuyKe.TongNhap, 0) - ISNULL(xuatLuyKe.TongXuat, 0) + ISNULL(traLuyKe.TongTra, 0))
          THEN N'✓ KHỚP 100%' 
          ELSE N'❌ LỆCH TỒN VẬT LÝ' 
      END AS TrangThaiDoiSoat
  FROM SanPham sp
  LEFT JOIN LoHang lh ON sp.maSanPham = lh.maSanPham
  LEFT JOIN (
      SELECT maSanPham, SUM(soLuongNhap) AS TongNhap 
      FROM LoHang GROUP BY maSanPham
  ) nhapLuyKe ON sp.maSanPham = nhapLuyKe.maSanPham
  LEFT JOIN (
      SELECT l.maSanPham, SUM(xl.soLuongXuatCoSo) AS TongXuat 
      FROM ChiTietXuatLo xl 
      JOIN LoHang l ON xl.soLo = l.soLo 
      GROUP BY l.maSanPham
  ) xuatLuyKe ON sp.maSanPham = xuatLuyKe.maSanPham
  LEFT JOIN (
      SELECT ctdt.maSanPham, SUM(ctdt.soLuong) AS TongTra 
      FROM ChiTietDoiTra ctdt 
      WHERE ctdt.tinhTrangHang = N'NGUYEN_TEM'
      GROUP BY ctdt.maSanPham
  ) traLuyKe ON sp.maSanPham = traLuyKe.maSanPham
  GROUP BY sp.maSanPham, sp.tenSanPham, nhapLuyKe.TongNhap, xuatLuyKe.TongXuat, traLuyKe.TongTra;

  -- [2] Kiểm tra tồn kho âm trên toàn hệ thống
  SELECT soLo, maSanPham, soLuongTon 
  FROM LoHang 
  WHERE soLuongTon < 0;
  -- Kỳ vọng: 0 dòng.
  ```

---

#### TC-BC-019: Báo cáo Xuất - Nhập - Tồn - Lỗi lấy tồn thời gian thực làm sai tồn lịch sử (DEF-BC-01 / DEF-BLK-03)
- **Phân hệ & Màn hình:** Báo cáo / `dao.BaoCaoThongKeDAO.java:327, 357`
- **Cấp độ kiểm thử:** Unit Test / Integration Test / Blocker Defect Verification (**DEF-BC-01 / DEF-BLK-03**)
- **Mục tiêu kiểm thử:** Kiểm tra khiếm khuyết nghiêm trọng (Blocker) trong thuật toán báo cáo Xuất-Nhập-Tồn: CTE `L` truy vấn `SELECT SUM(soLuongTon) AS tonCuoi FROM LoHang` mà **không lọc theo mốc thời gian kết thúc kỳ `den`**, sau đó tính ngược `tonDauKy = tonCuoiKy - nhapKy + xuatKy - traKy`. Khi xem báo cáo cho một kỳ trong quá khứ, `tonCuoiKy` bị lấy theo tồn kho của ngày hôm nay, khiến **tồn đầu kỳ và biến động tồn kho lịch sử bị sai lệch hoàn toàn, thậm chí ra số âm**.
- **Điều kiện tiên quyết:** Sản phẩm `SP001` có tồn kho hôm nay là 10 hộp. Tuần trước (từ 01/11 đến 03/11) có nhập 20 hộp và bán 5 hộp. Sau đó (từ 04/11 đến hôm nay) bán thêm 5 hộp nữa.
- **Các bước thực hiện:**
  1. Mở báo cáo Xuất-Nhập-Tồn cho kỳ lịch sử: từ `01/11/2026` đến `03/11/2026`.
  2. Quan sát giá trị `Tồn cuối kỳ` và `Tồn đầu kỳ` được tính toán cho `SP001`.
- **Kết quả mong đợi theo ĐẶC TẢ SỔ CÁI KHO:**
  - Tồn cuối kỳ ngày 03/11 phải là `20 - 5 = 15 hộp`.
  - Tồn đầu kỳ ngày 01/11 phải là `0 hộp`.
- **Hành vi thực tế phát hiện lỗi (DEF-BC-01 / DEF-BLK-03 Evidence):**
  - Tại `BaoCaoThongKeDAO.java:327`:
    ```sql
    WITH L AS (SELECT maSanPham,SUM(soLuongTon) tonCuoi ... FROM LoHang GROUP BY maSanPham)
    ```
  - Biến `tonCuoi` bị lấy bằng `10` (tồn kho của ngày hôm nay 07/11).
  - Dòng 357: `r.tonDauKy = r.tonCuoiKy - r.nhapTrongKy + r.xuatBanTrongKy - r.hoanTraTrongKy;`  
    -> `tonDauKy = 10 - 20 + 5 - 0 = -5 hộp` (TỒN ĐẦU KỲ BỊ ÂM!).
  - Kết quả lỗi thực tế: Toàn bộ báo cáo Xuất-Nhập-Tồn cho các kỳ trong quá khứ bị sai số liệu nghiêm trọng, xuất hiện tồn kho âm vô lý!
- **Tiêu chí Đạt/Không đạt:**
  - *Phát hiện lỗi thành công:* Tồn đầu kỳ bị tính sai hoặc ra số âm khi xem kỳ quá khứ (Xác nhận lỗi Blocker DEF-BC-01 / DEF-BLK-03).

---

#### TC-BC-020: Kiểm tra tính toán Giá trị tồn kho theo giá vốn nhập cơ sở
- **Phân hệ & Màn hình:** Báo cáo / `BaoCaoThongKeDAO.java:329`
- **Cấp độ kiểm thử:** Integration Test / Financial Audit
- **Mục tiêu kiểm thử:** Xác minh cột `Giá trị tồn (Giá nhập)` tính đúng bằng `SUM(soLuongTon * donGiaNhapCoSo)` trên toàn bộ các lô của từng sản phẩm.
- **Điều kiện tiên quyết:** CSDL có các lô hàng với đơn giá nhập khác nhau.
- **Các bước thực hiện:** Đối chiếu cột Giá trị tồn với bảng `LoHang`.
- **Kết quả mong đợi:** Giá trị tồn kho khớp chính xác với tổng tích số lượng tồn nhân giá nhập từng lô.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu số liệu tài chính khớp 100%.

---

### Nhóm 7: Báo Cáo Khách VIP, Pet Hotel & Xuất Dữ Liệu (TC-BC)

#### TC-BC-021: Báo cáo Khách hàng VIP và thống kê thẻ Spa Pass (Tab 3)
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 3: "Khách VIP & Điểm")
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh Tab 3 hiển thị danh sách khách hàng thành viên có điểm tích lũy, phân hạng thẻ VIP (Kim Cương >= 100đ, Vàng 50-99đ, Bạc 20-49đ, Mới < 20đ), tổng chi tiêu tích lũy, và danh sách các thẻ Spa Pass đang kích hoạt còn hạn.
- **Điều kiện tiên quyết:** Bảng `KhachHang` có dữ liệu khách VIP.
- **Các bước thực hiện:** Chuyển sang Tab 3 và kiểm tra thông tin hiển thị.
- **Kết quả mong đợi:** Khách hàng được gắn đúng hạng thẻ VIP và số điểm tích lũy.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu dữ liệu khách hàng chính xác.

---

#### TC-BC-022: Báo cáo Khách sạn Pet Hotel & Hoàn trả đổi trả hàng (Tab 4)
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog` (Tab 4: "Pet Hotel & Đổi trả")
- **Cấp độ kiểm thử:** Integration Test / UI Test
- **Mục tiêu kiểm thử:** Xác minh Tab 4 tổng hợp các chỉ số: Doanh thu lưu trú khách sạn thú cưng, tổng lượt gửi, số phòng đang có khách, và tổng số tiền đã hoàn trả từ các phiếu đổi trả hàng trong kỳ.
- **Điều kiện tiên quyết:** Có phát sinh lượt gửi phòng Pet Hotel và phiếu đổi trả hoàn tiền.
- **Các bước thực hiện:** Chuyển sang Tab 4 và đối chiếu số liệu.
- **Kết quả mong đợi:** Tổng tiền hoàn trả khớp với `SUM(tongTienHoan)` của bảng `PhieuDoiTra`.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu số liệu khớp CSDL.

---

#### TC-BC-023: Tính năng xuất báo cáo đang xem ra tệp CSV mở bằng Excel
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog.xuatBaoCaoRaCsv`
- **Cấp độ kiểm thử:** UI Test / File I/O Verification
- **Mục tiêu kiểm thử:** Xác minh khi bấm nút "Xuất CSV mở bằng Excel", hệ thống mở hộp thoại chọn thư mục lưu tệp `JFileChooser`, trích xuất toàn bộ dữ liệu bảng đang xem ra tệp `.csv` với bảng mã UTF-8 có BOM (Byte Order Mark) để Microsoft Excel mở trực tiếp không bị lỗi font tiếng Việt có dấu.
- **Điều kiện tiên quyết:** Đang xem một tab báo biểu có dữ liệu.
- **Các bước thực hiện:**
  1. Tại `BaoCaoThongKeDialog`, click nút "Xuất CSV mở bằng Excel".
  2. Chọn đường dẫn lưu tệp: `export/BaoCao_DoanhThu_Test.csv`.
  3. Bấm "Save" / "Lưu".
  4. Mở tệp `.csv` vừa xuất bằng Notepad hoặc Microsoft Excel.
- **Dữ liệu kiểm thử:** Đường dẫn tệp `.csv`.
- **Kết quả mong đợi:**
  - Xuất tệp thành công, hiển thị thông báo "Đã xuất báo cáo ra file CSV thành công!".
  - Nội dung tệp chứa dòng tiêu đề cột, các dòng dữ liệu ngăn cách bằng dấu phẩy `,`, tiếng Việt có dấu hiển thị chuẩn xác (UTF-8).
- **Tiêu chí Đạt/Không đạt:** Đạt nếu tệp CSV được tạo đúng cấu trúc và không lỗi font tiếng Việt.

---

#### TC-BC-024: Tính năng in ấn trực tiếp bảng báo cáo đang xem
- **Phân hệ & Màn hình:** Báo cáo / `gui.BaoCaoThongKeDialog.inBaoCaoDangXem`
- **Cấp độ kiểm thử:** UI Test / Printing Verification
- **Mục tiêu kiểm thử:** Xác minh nút "In bảng đang xem" kích hoạt hộp thoại máy in chuẩn của hệ điều hành qua `JTable.print()`, cho phép in ra máy in ảo (Microsoft Print to PDF) hoặc máy in vật lý.
- **Điều kiện tiên quyết:** Đang mở hộp thoại báo cáo.
- **Các bước thực hiện:**
  1. Click nút "In bảng đang xem".
  2. Hộp thoại cấu hình máy in Print Dialog xuất hiện.
  3. Chọn máy in ảo "Microsoft Print to PDF" và bấm "Print".
- **Kết quả mong đợi:**
  - Hộp thoại in hiển thị đúng tiêu đề báo cáo ở Header và số trang ở Footer.
  - Tệp PDF được xuất hoàn chỉnh, định dạng trang in vừa vặn không bị cắt cụt cột.
- **Tiêu chí Đạt/Không đạt:** Đạt nếu lệnh in Java Print Service hoạt động trơn tru.

---

## 4. KỊCH BẢN SQL KIỂM CHỨNG & TẠO DỮ LIỆU ĐỐI SOÁT BÁO CÁO (VERIFICATION & SEED SCRIPTS)

### 4.1 Tập lệnh đối soát dữ liệu Doanh thu & Tồn kho chuẩn:
```sql
USE QuanLyCuaHangThuCung;
GO

-- 1. Đối soát tổng doanh thu và tiền giảm giá toàn bộ hệ thống
SELECT 
    COUNT(*) AS TongSoHoaDon,
    SUM(thanhTien) AS TongDoanhThuThuan,
    SUM(ISNULL(giamGia, 0)) AS TongGiamGia,
    SUM(thanhTien + ISNULL(giamGia, 0)) AS TongTienHang
FROM HoaDon
WHERE trangThaiHoaDon = N'Đã thanh toán';

-- 2. Đối soát doanh thu theo phương thức thanh toán
SELECT 
    phuongThucThanhToan,
    COUNT(*) AS SoDon,
    SUM(thanhTien) AS TongTienThuan
FROM HoaDon
WHERE trangThaiHoaDon = N'Đã thanh toán'
GROUP BY phuongThucThanhToan;

-- 3. Kiểm tra danh sách lô hàng cận hạn nguy cấp (<= 30 ngày)
SELECT 
    l.soLo, l.maSanPham, sp.tenSanPham, l.hanSuDung, l.soLuongTon,
    DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) AS SoNgayConLai
FROM LoHang l
JOIN SanPham sp ON sp.maSanPham = l.maSanPham
WHERE l.soLuongTon > 0 
  AND DATEDIFF(day, CAST(GETDATE() AS DATE), l.hanSuDung) <= 30
ORDER BY l.hanSuDung ASC;

-- 4. Kiểm tra phương trình cân đối kho tổng thể:
SELECT 
    sp.maSanPham, sp.tenSanPham,
    ISNULL((SELECT SUM(soLuongNhap) FROM LoHang WHERE maSanPham = sp.maSanPham), 0) AS TongNhap,
    ISNULL((SELECT SUM(xl.soLuongXuatCoSo) FROM ChiTietXuatLo xl 
            JOIN LoHang l ON l.soLo = xl.soLo 
            JOIN ChiTietHoaDon ct ON ct.maChiTiet = xl.maChiTiet
            JOIN HoaDon h ON h.maHoaDon = ct.maHoaDon
            WHERE l.maSanPham = sp.maSanPham AND h.trangThaiHoaDon = N'Đã thanh toán'), 0) AS TongXuat,
    ISNULL((SELECT SUM(ct.soLuongDoiTra) FROM ChiTietDoiTra ct 
            JOIN PhieuDoiTra p ON p.maPhieuDoiTra = ct.maPhieuDoiTra
            WHERE ct.maSanPham = sp.maSanPham), 0) AS TongTra,
    ISNULL((SELECT SUM(soLuongTon) FROM LoHang WHERE maSanPham = sp.maSanPham), 0) AS TonHienTai
FROM SanPham sp;
GO
```
