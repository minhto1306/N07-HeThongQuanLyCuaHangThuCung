# 🐾 HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (MIO PET SHOP MANAGEMENT SYSTEM)

> **Đồ án học phần:** Phát triển Ứng dụng (Application Development - AD)  
> **Đơn vị đào tạo:** Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM (IUH)  
> **Giảng viên hướng dẫn:** ThS. Phạm Thanh Hùng  
> **Nhóm thực hiện:** Nhóm 07 (N07)  
> **Nền tảng & Kiến trúc:** Pure Java MVC (Model - View - Controller) • Microsoft SQL Server • FlatLaf Modern UI (Luxury Emerald & Mint Sage Edition)

---

## 👥 THÀNH VIÊN NHÓM VÀ PHÂN CÔNG NHIỆM VỤ

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; width: 50px;">STT</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Họ và Tên</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Vai trò</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Phân hệ nghiệp vụ phụ trách</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Nhánh GitHub (Branch)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>1</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lê Khải Minh</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Leader / Fullstack</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Bán hàng POS, In hóa đơn K80, Đổi trả hàng &amp; Chuyển tiếp sang POS, Tích hợp thanh toán chuyển khoản VietQR Napas 24/7</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/pos-ban-hang-doi-tra</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>2</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Tô Thành Đạt</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Kho hàng, Danh mục sản phẩm, Quản lý Lô hàng cận date (FEFO), Thiết lập bảng quy đổi đơn vị tính đa cấp</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/kho-san-pham-lo-hang</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>3</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Võ Thị Hồng Ngọc</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Hồ sơ thú cưng, Sổ theo dõi sức khỏe &amp; tiêm phòng, Đặt lịch hẹn Spa &amp; Grooming, Khách sạn lưu trú (Pet Hotel)</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/thu-cung-dich-vu-hotel</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>4</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lê Anh Minh</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Nhân sự, Đăng nhập &amp; Phân quyền RBAC 4 vai trò, Ma trận xếp ca trực tuần, Bàn giao ca thu ngân &amp; Duyệt két tiền, Dashboard báo biểu</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/nhan-su-giao-ca-dashboard</code></td>
    </tr>
  </tbody>
</table>

---

## 📖 BÀI TOÁN NGHIỆP VỤ & CƠ CHẾ HOẠT ĐỘNG KHỚP VỚI CHƯƠNG TRÌNH

Hệ thống được thiết kế chuyên biệt cho mô hình siêu thị thú cưng kết hợp dịch vụ chăm sóc trọn gói (Bán lẻ sản phẩm thú cưng, Thức ăn dinh dưỡng, Spa Grooming, Pet Hotel). Dưới đây là mô tả chi tiết từng quy trình nghiệp vụ khớp trực tiếp với giao diện và mã nguồn của ứng dụng:

### 1. Phân hệ Bán hàng POS & Điều phối Thu ngân (`BanHangPOSPanel`)
* **Bố cục màn hình 2 vùng trực quan:**
  - **Nửa trái (Catalog trực quan):** Phân loại danh mục bằng các nút lọc (`TẤT CẢ`, `Hàng hóa`, `Dịch vụ`, `Thức ăn`, `Phụ kiện`, `Thuốc thú y`). Thẻ sản phẩm hiển thị ảnh, tên, đơn giá và số lượng tồn kho khả dụng. Thẻ dịch vụ hiển thị thông tin gói chăm sóc và chọn thú cưng / kỹ thuật viên.
  - **Nửa phải (Giỏ hàng, Khách hàng & Thanh toán):** Quản lý khách hàng thành viên, giỏ hàng đa đơn vị quy đổi, bảng tính tiền tự động và khu vực thanh toán.
* **Quy trình bán hàng từng bước:**
  1. **Nhận diện khách hàng & Tích điểm:** Thu ngân nhập Số điện thoại khách vào ô tìm kiếm (`txtSoDienThoai`). Hệ thống tự động gợi ý và tra cứu họ tên, hạng thành viên cùng điểm tích lũy hiện có (Quy đổi giảm giá: `1 điểm = 1.000 VNĐ`). Nếu khách có thú cưng, danh sách thú cưng tự động nạp vào ComboBox để phục vụ dịch vụ Spa.
  2. **Thêm món vào giỏ hàng & Quy đổi đơn vị tính:**
     - *Hàng hóa:* Nhấn vào thẻ sản phẩm hoặc quét mã Barcode để đưa vào giỏ hàng. Hỗ trợ chọn đơn vị bán linh hoạt (`Túi`, `Lon`, `Khay`, `Hộp`, `Thùng`) với giá bán tự động tính theo hệ số quy đổi và chiết khấu mua sỉ. Cột `SL` hỗ trợ kiểm tra chặt chẽ số lượng tồn kho thực tế.
     - *Dịch vụ Spa:* Nhấn vào thẻ dịch vụ $\rightarrow$ Chọn thú cưng (áp giá tự động theo cân nặng) và phân công Kỹ thuật viên phụ trách. Hỗ trợ kích hoạt hoặc sử dụng gói thẻ **Spa Pass** trọn gói.
  3. **Gợi ý bán kèm thông minh (Smart Cross-Sell):** Khi giỏ hàng có thức ăn hạt hoặc dịch vụ tắm, thanh gợi ý đề xuất sản phẩm mua kèm giảm giá 10% giúp thu ngân dễ dàng tư vấn thêm cho khách.
  4. **Đơn hàng chờ phục vụ song song (Holding Carts):** Nếu khách đang chờ lấy thêm đồ hoặc thú cưng đang làm dịch vụ, thu ngân bấm `Lưu tạm (Đơn chờ)` để lưu giỏ hàng hiện tại và phục vụ khách tiếp theo, sau đó khôi phục lại bất cứ lúc nào.
  5. **Hai phương thức thanh toán chuẩn hóa & Tính tiền thối thực tế (`F9`):**
     - Hệ thống hỗ trợ **2 phương thức thanh toán thực tế**: **`Tiền mặt`** và **`Chuyển khoản`**.
     - **Thanh toán Tiền mặt:**
       - Ô `Khách đưa` không tự động điền cứng 100% tổng tiền mà để thu ngân nhập đúng số tiền thực tế khách đưa hoặc bấm nhanh 4 nút mệnh giá: **`[100.000đ]`**, **`[200.000đ]`**, **`[500.000đ]`**, **`[Đủ tiền]`**.
       - Hệ thống tự động tính **`Tiền thối lại`** theo thời gian thực: hiển thị màu xanh lá khi khách đưa đủ/dư tiền, hoặc cảnh báo **`Thiếu: ... đ`** màu đỏ và **chặn hoàn tất thanh toán** nếu khách đưa chưa đủ tiền.
     - **Thanh toán Chuyển khoản (VietQR Napas 24/7):**
       - Khi chọn `Chuyển khoản` và bấm `THANH TOÁN (F9)`, hệ thống tự động điền đủ số tiền cần thu và mở hộp thoại `VietQRDialog` hiển thị mã QR động chuẩn EMVCo Napas 24/7 kèm sẵn số tiền và nội dung chuyển khoản của hóa đơn.
     - Sau khi xác nhận thanh toán, hệ thống ghi nhận giao dịch ACID vào CSDL, trừ tồn kho theo lô FEFO và hiển thị phiếu in hóa đơn nhiệt K80 (`HoaDonPreviewDialog`).

---

### 2. Quản lý Kho, Xuất kho Cận Date (FEFO) & Quy đổi Đơn vị (`KhoLoHangPanel`)
* **Thuật toán trừ kho FEFO (First-Expired, First-Out):**
  - Khi sản phẩm được xuất bán tại POS, hệ thống tự động truy vấn các lô hàng (`LoHang`) còn tồn kho của sản phẩm đó và sắp xếp tăng dần theo `NgayHetHan`.
  - Hệ thống ưu tiên xuất từ lô cận date nhất trước; nếu lô đó không đủ số lượng sẽ tự động lấy tiếp ở lô kế tiếp và lưu vết chi tiết xuất lô (`ChiTietXuatLo`).
* **Cảnh báo hạn sử dụng 3 cấp độ màu:**
  - 🟢 **Xanh lá (An toàn):** Hạn sử dụng còn trên 60 ngày.
  - 🟡 **Vàng cam (Cận date):** Hạn sử dụng còn từ 15 đến 60 ngày.
  - 🔴 **Đỏ nguy cấp:** Hạn sử dụng dưới 15 ngày hoặc đã hết hạn.
* **Quy đổi Đơn vị tính Đa cấp (`ThietLapQuyDoiDialog`):**
  - Khai báo linh hoạt tỷ lệ quy đổi (ví dụ: `1 Thùng = 4 Hộp = 24 Gói`). Khi bán theo đơn vị lớn hoặc nhỏ, hệ thống đều tự động quy đổi về đơn vị cơ sở để trừ tồn kho chính xác.

---

### 3. Nghiệp vụ Đổi / Trả Hàng Hóa & Chuyển Tiếp Sang POS (`DoiTraHangPanel`)
* **Chính sách đổi trả:** Hỗ trợ kiểm tra và đổi trả đối với các mặt hàng vật lý thuộc hóa đơn hợp lệ trong vòng 07 ngày.
* **Quy trình xử lý 2 kịch bản:**
  1. **Tra cứu hóa đơn gốc:** Nhập Mã hóa đơn hoặc chọn từ danh sách lịch sử hóa đơn để tải các mặt hàng đã mua.
  2. **Chọn sản phẩm trả & tình trạng:** Tích chọn sản phẩm khách muốn đổi/trả, nhập số lượng trả, lý do và tình trạng hàng (`Còn nguyên tem` sẽ được tự động hoàn lại vào tồn kho lô hàng; `Hàng lỗi / Đã bóc` ghi nhận xử lý riêng).
  3. **Hai kịch bản xử lý rõ ràng:**
     - **Kịch bản A (Chỉ trả hàng & Hoàn tiền tại quầy):** Khi khách chỉ trả lại hàng và không lấy thêm sản phẩm mới, nút tác vụ hiển thị **`HOÀN TIỀN & LƯU PHIẾU TRẢ`**. Hệ thống lập phiếu đổi trả, hoàn lại tồn kho (nếu nguyên tem), khấu trừ điểm tích lũy tương ứng và xuất phiếu xác nhận hoàn tiền K80.
     - **Kịch bản B (Đổi sang sản phẩm mới $\rightarrow$ Chuyển tiếp sang POS):** Khi khách chọn thêm sản phẩm mới muốn đổi sang, nút tác vụ tự động chuyển thành **`CHUYỂN SANG POS ĐỂ ĐỔI HÀNG & THANH TOÁN`**. Hệ thống đóng gói dữ liệu đổi trả (`DoiTraTransferData`) và tự động chuyển sang màn hình **Bán hàng POS (`BanHangPOSPanel`)**:
       - Tự động điền thông tin khách hàng và nạp các sản phẩm đổi mới vào giỏ hàng POS (thu ngân có thể thoải mái chọn thêm sản phẩm khác nếu khách mua thêm).
       - Hiển thị rõ dòng **`Khấu trừ tiền hàng đổi trả: -xxx đ`** trên bảng tính tiền và tự động cấn trừ vào `TỔNG THANH TOÁN`.
       - Nếu giá trị đơn mới cao hơn tiền hàng trả, thu ngân thu phần tiền chênh lệch bằng Tiền mặt hoặc Chuyển khoản VietQR ngay trên POS. Nếu giá trị đơn mới thấp hơn, hệ thống hiển thị rõ số tiền mặt cần hoàn thừa lại cho khách.

---

### 4. Hồ Sơ Thú Cưng, Dịch Vụ Spa & Khách Sạn Pet Hotel
* **Quản lý Hồ sơ & Sổ sức khỏe (`ThuCungDichVuPanel`):**
  - Lưu trữ hồ sơ chi tiết từng thú cưng (Tên, giống loài Chó/Mèo, giới tính, cân nặng, ngày sinh, chủ sở hữu) và lịch sử khám sức khỏe, tiêm chủng định kỳ (`HoSoSucKhoe`).
* **Đặt lịch hẹn Spa & Grooming (`DatLichHenDialog` / `QuanLyLichHenPanel`):**
  - Đặt lịch theo các khung giờ trong ngày, phân công kỹ thuật viên chăm sóc, kiểm soát trạng thái lịch hẹn (`Chờ xác nhận`, `Đang thực hiện`, `Hoàn thành`, `Đã hủy`) và chuyển sang POS thanh toán.
* **Khách sạn Lưu Trú Thú Cưng (`PetHotelPanel`):**
  - Sơ đồ trực quan hệ thống chuồng/phòng lưu trú Standard & VIP theo trạng thái thời gian thực (`Trống`, `Đang lưu trú`, `Đang dọn dẹp`), hỗ trợ Check-in nhận phòng và Check-out tính tiền theo số ngày lưu trú.

---

### 5. Phân Ca Trực & Đối Soát Bàn Giao Ca Chờ Duyệt (`BanGiaoCaPanel` / `LichPhanCaPanel`)
* **Ma trận phân ca tuần (`LichPhanCaPanel`):** Xếp lịch trực cho nhân viên theo các ca làm việc trong tuần.
* **Quy trình bàn giao ca & đối soát két tiền bất đồng bộ (`BanGiaoCaPanel`):**
  1. **Thu ngân kết ca & kiểm đếm tiền mặt:** Hệ thống tự động tổng hợp `Tiền đầu ca`, `Doanh thu tiền mặt hệ thống`, `Tiền mặt hoàn trả khách` để tính ra `Tổng tiền lý thuyết trong két`.
  2. Thu ngân đếm tiền thực tế trong két và nhập vào ô `Tiền mặt thực tế đếm được`. Hệ thống tự động tính độ lệch (`Khớp két`, `Thừa` hoặc `Thiếu`). Nếu có chênh lệch, bắt buộc thu ngân phải nhập `Lý do giải trình`.
  3. **Gửi bàn giao chờ xét duyệt:** Khi thu ngân bấm **`GỬI BÀN GIAO CA (CHỜ QUẢN LÝ DUYỆT)`**, biên bản giao ca được lưu vào CSDL với trạng thái **`⏳ Chờ Quản lý duyệt`** (không yêu cầu Quản lý phải đứng tại quầy nhập mật khẩu ngay lúc đó), đồng thời gợi ý thu ngân đăng xuất kết thúc ca.
  4. **Quản lý đăng nhập & ký duyệt ca:** Khi Quản lý (`NVQL`) đăng nhập vào hệ thống, Quản lý mở mục **Duyệt giao ca**, kiểm tra các ca đang có trạng thái `⏳ Chờ Quản lý duyệt` và bấm nút **`QUẢN LÝ XÁC NHẬN & KÝ DUYỆT CA`** để cập nhật trạng thái thành **`✓ Đã duyệt`** và in biên bản đối soát K80.

---

## 🏗️ CẤU TRÚC THƯ MỤC DỰ ÁN (MVC PURE JAVA)

```
N07-HeThongQuanLyCuaHangThuCung/
├── config/                             # File cấu hình kết nối CSDL và cổng thanh toán
│   ├── database.properties             # Cấu hình kết nối Microsoft SQL Server
│   ├── vietqr.properties               # Cấu hình tài khoản thụ hưởng VietQR Napas 24/7
│   └── vnpay.properties                # Cấu hình thông số VNPay
│
├── sql/                                # Kịch bản Cơ sở dữ liệu SQL Server
│   ├── HeThongQuanLyCuaHangThuCung.sql # Kịch bản tạo Database, 18 bảng, Ràng buộc & Dữ liệu mẫu
│   └── DB_HeThongQuanLyThuCung.sql     # Kịch bản CSDL dự phòng đầy đủ
│
├── lib/                                # Các thư viện phụ thuộc (Dependencies)
│   ├── flatlaf-3.5.4.jar               # Thư viện giao diện phẳng FlatLaf
│   └── mssql-jdbc-12.8.1.jre11.jar     # Trình điều khiển JDBC kết nối Microsoft SQL Server
│
├── resources/                          # Tài nguyên hình ảnh & biểu tượng
│   ├── icon/ & icons/                  # Bộ biểu tượng giao diện
│   └── images/                         # Hình ảnh sản phẩm và dịch vụ minh họa
│
└── src/                                # Toàn bộ mã nguồn chương trình (Pure Java MVC)
    ├── Main.java                       # Điểm khởi chạy ứng dụng (Khởi tạo FlatLaf & mở LoginForm)
    ├── controllers/                    # Tầng điều khiển nghiệp vụ (Business Logic Controllers)
    │   ├── BanHangController.java      # Điều phối giỏ hàng, khách hàng, xuất hóa đơn
    │   ├── DangNhapController.java     # Xác thực tài khoản và phiên đăng nhập RBAC
    │   ├── GiaoCaController.java       # Nghiệp vụ bàn giao ca và đối soát két tiền
    │   ├── NhanVienController.java     # Quản lý hồ sơ nhân sự cửa hàng
    │   ├── PhanCaController.java       # Điều phối lịch phân công ca làm việc
    │   ├── SpaPassController.java      # Quản lý gói thẻ dịch vụ Spa Pass
    │   └── ThuCungController.java      # Quản lý hồ sơ thú cưng và sổ sức khỏe
    │
    ├── models/
    │   ├── entity/                     # Các lớp thực thể & DTO (26 lớp)
    │   │   ├── HoaDon.java, ChiTietHoaDon.java, ChiTietXuatLo.java, ChiTietDichVu.java
    │   │   ├── PhieuDoiTra.java, ChiTietDoiTra.java, DoiTraTransferData.java, TheSpaPass.java
    │   │   ├── SanPham.java, LoHang.java, DonViTinh.java, QuyDoiDonVi.java, DanhMucHang.java, NhaCungCap.java
    │   │   ├── KhachHang.java, ThuCung.java, HoSoThuCung.java, HoSoSucKhoe.java, DichVu.java, LichHen.java, ChiTietLichHen.java
    │   │   └── NhanVien.java, TaiKhoan.java, BangGiaoCa.java, CaLamViec.java, BangPhanCong.java
    │   │
    │   └── dao/                        # Tầng truy cập cơ sở dữ liệu JDBC (12 lớp DAO)
    │       ├── HoaDonDAO.java, KhachHangDAO.java, SanPhamDAO.java, LoHangDAO.java
    │       ├── ThuCungDAO.java, DichVuDAO.java, LichHenDAO.java, PhongLuuTruDAO.java
    │       └── NhanVienDAO.java, TaiKhoanDAO.java, BangGiaoCaDAO.java, PhanCaDAO.java
    │
    ├── utils/                          # Các lớp tiện ích hệ thống
    │   ├── DBConnection.java           # Quản lý kết nối SQL Server JDBC
    │   ├── ThemeColors.java            # Hệ màu giao diện Luxury Emerald & Mint Sage
    │   ├── MaTuSinhHelper.java         # Bộ sinh mã nghiệp vụ tự động chuẩn <= 20 ký tự
    │   ├── UserSession.java            # Quản lý phiên đăng nhập và kiểm tra phân quyền RBAC
    │   ├── IconHelper.java             # Kết xuất biểu tượng vector Java2D
    │   └── VNPayHelper.java            # Tiện ích tạo URL và chữ ký số HMAC-SHA512
    │
    └── views/                          # Tầng giao diện người dùng Java Swing (Panels & Dialogs)
        ├── LoginForm.java              # Màn hình đăng nhập hệ thống
        ├── MainDashboard.java          # Khung điều khiển chính với thanh điều hướng Pill Tabs
        ├── TongQuanPanel.java          # Màn hình Dashboard tổng quan & KPI
        ├── BanHangPOSPanel.java        # Màn hình Bán hàng POS & Thanh toán
        ├── DoiTraHangPanel.java        # Màn hình Đổi trả hàng hóa
        ├── KhoLoHangPanel.java         # Màn hình Quản lý kho & Lô hàng FEFO
        ├── ThuCungDichVuPanel.java     # Màn hình Hồ sơ thú cưng & Sổ sức khỏe
        ├── QuanLyLichHenPanel.java     # Màn hình Quản lý lịch hẹn Spa & Grooming
        ├── PetHotelPanel.java          # Màn hình Khách sạn lưu trú thú cưng (Pet Hotel)
        ├── BanGiaoCaPanel.java         # Màn hình Bàn giao ca & Quản lý xét duyệt két tiền
        ├── LichPhanCaPanel.java        # Màn hình Lịch phân ca làm việc tuần
        ├── QuanLyNhanVienPanel.java    # Màn hình Quản lý nhân sự & Tài khoản
        ├── VietQRDialog.java           # Hộp thoại thanh toán chuyển khoản VietQR Napas 24/7
        ├── HoaDonPreviewDialog.java    # Hộp thoại xem trước và in hóa đơn nhiệt K80
        └── charts/                     # Các thành phần biểu đồ thống kê Graphics2D
            ├── BarChartPanel.java      # Biểu đồ cột thống kê doanh thu
            └── DonutChartPanel.java    # Biểu đồ tròn tỷ trọng doanh thu
```

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY ỨNG DỤNG

### 1. Yêu cầu môi trường
- **Java Development Kit (JDK):** Phiên bản **Java 17 LTS** trở lên (Java 17 / Java 21).
- **Hệ quản trị Cơ sở dữ liệu:** **Microsoft SQL Server** (2019 / 2022 / Express) đang chạy ở cổng mặc định `1433` và bật xác thực **SQL Server Authentication**.
- **Môi trường phát triển (IDE):** **Eclipse IDE for Java Developers** (hoặc IntelliJ IDEA / VS Code).

### 2. Khởi tạo Cơ sở dữ liệu SQL Server
1. Mở **SQL Server Management Studio (SSMS)**.
2. Mở tệp kịch bản SQL trong thư mục dự án: [`sql/HeThongQuanLyCuaHangThuCung.sql`](sql/HeThongQuanLyCuaHangThuCung.sql).
3. Nhấn **Execute (F5)** để tạo cơ sở dữ liệu `QuanLyCuaHangThuCung`, khởi tạo toàn bộ 18 bảng dữ liệu, khóa chính, khóa ngoại cùng dữ liệu mẫu.
4. Kiểm tra thông tin tài khoản kết nối SQL Server trong tệp [`config/database.properties`](config/database.properties) (mặc định sử dụng tài khoản `sa`):
   ```properties
   db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyCuaHangThuCung;encrypt=true;trustServerCertificate=true;
   db.user=sa
   db.password=sapassword
   ```
   *(Lưu ý: Thay `sapassword` bằng mật khẩu `sa` thực tế trên máy của bạn).*

### 3. Hướng dẫn mở và chạy dự án trên Eclipse IDE (Khuyên dùng)
Dự án đã được cấu hình sẵn tệp `.project`, `.classpath` (JavaSE-17) và thư viện trong `lib/`, bạn chỉ cần Import vào Eclipse là chạy ngay:
1. Mở **Eclipse IDE** $\rightarrow$ Trên thanh menu chọn **File** $\rightarrow$ **Import...**
2. Mở mục **General** $\rightarrow$ Chọn **Existing Projects into Workspace** $\rightarrow$ Nhấn **Next**.
3. Tại mục **Select root directory**, nhấn **Browse...** và trỏ đến thư mục `N07-HeThongQuanLyCuaHangThuCung` vừa tải về.
4. Tích chọn dự án `N07-HeThongQuanLyCuaHangThuCung` trong khung *Projects* $\rightarrow$ Nhấn **Finish**.
5. Trong cửa sổ *Package Explorer*, mở thư mục `src` $\rightarrow$ Nhấp chuột phải vào tệp **[`src/Main.java`](src/Main.java)** $\rightarrow$ Chọn **Run As** $\rightarrow$ **Java Application**.

### 4. Hướng dẫn biên dịch và chạy trực tiếp bằng dòng lệnh (Terminal / PowerShell)
Nếu bạn muốn biên dịch và chạy trực tiếp bằng lệnh `javac` và `java` mà không cần mở IDE, hãy mở **PowerShell** hoặc **Command Prompt** tại thư mục gốc của dự án và chạy:

- **Trên Windows PowerShell:**
  ```powershell
  # Bước 1: Tạo thư mục bin và biên dịch toàn bộ mã nguồn Java (UTF-8)
  New-Item -ItemType Directory -Force -Path bin | Out-Null
  $sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
  javac -encoding UTF-8 -cp "lib/*;src" -d bin $sources

  # Bước 2: Khởi chạy ứng dụng
  java -cp "bin;lib/*;resources" Main
  ```

- **Trên Command Prompt (CMD) / Git Bash:**
  ```bash
  # Bước 1: Biên dịch mã nguồn
  mkdir bin
  javac -encoding UTF-8 -cp "lib/*;src" -d bin src/Main.java src/controllers/*.java src/models/entity/*.java src/models/dao/*.java src/utils/*.java src/views/*.java src/views/charts/*.java

  # Bước 2: Khởi chạy ứng dụng
  java -cp "bin;lib/*;resources" Main
  ```

---

## 🔑 TÀI KHOẢN ĐĂNG NHẬP MẪU THEO PHÂN QUYỀN (RBAC)

Hệ thống phân quyền truy cập theo 4 vai trò nghiệp vụ (`UserSession`):

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Tên Đăng Nhập</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Mật Khẩu</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Vai Trò Hệ Thống</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Phạm Vi Quyền Hạn Trên Giao Diện</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>admin</code> <i>(hoặc <code>NVQL001</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>123456</code> <i>(hoặc <code>admin123</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Quản Lý Cửa Hàng (NVQL)</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Toàn quyền hệ thống:</b> Truy cập tất cả phân hệ (Tổng quan, Bán hàng POS, Kho lô hàng, Đổi trả, Thú cưng &amp; Dịch vụ, Lịch hẹn, Pet Hotel, Bàn giao ca &amp; <b>Xét duyệt giao ca</b>, Lịch phân ca, Quản lý nhân viên).</td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>thungan</code> <i>(hoặc <code>NVTN001</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>123456</code> <i>(hoặc <code>tn123</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Thu Ngân (NVTN)</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Quầy thu ngân:</b> Bán hàng POS, In hóa đơn K80, Đổi trả hàng hóa, Gửi bàn giao ca cuối ngày (trạng thái chờ duyệt), Xem lịch trực <i>(Bị khóa: Quản lý kho, Quản lý nhân sự, Ký duyệt giao ca)</i>.</td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>banhang</code> <i>(hoặc <code>NVBH001</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>123456</code> <i>(hoặc <code>bh123</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Nhân Viên Bán Hàng (NVBH)</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Tư vấn bán hàng:</b> Bán hàng POS, Tra cứu sản phẩm &amp; dịch vụ, Tiếp nhận khách hàng <i>(Bị khóa: Nhập kho, Duyệt giao ca, Quản lý nhân sự)</i>.</td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>chamsoc</code> <i>(hoặc <code>NVCS001</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>123456</code> <i>(hoặc <code>cs123</code>)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Kỹ Thuật Viên Spa (NVCS)</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Chăm sóc thú cưng:</b> Quản lý hồ sơ &amp; sức khỏe thú cưng, Điều phối lịch hẹn Spa Grooming, Quản lý phòng lưu trú Pet Hotel <i>(Bị khóa: Quầy thu ngân POS, Quản lý kho, Quản lý nhân sự)</i>.</td>
    </tr>
  </tbody>
</table>

---
*© 2026 - Nhóm 07 - Đề tài môn học Phát Triển Ứng Dụng (AD) - Khoa Công nghệ Thông tin, Trường Đại học Công nghiệp TP.HCM (IUH).*
