# 🧪 TÀI LIỆU HƯỚNG DẪN KIỂM THỬ TOÀN DIỆN HỆ THỐNG (TESTING GUIDE)
## HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (MIO PET SHOP)

> **Môn học:** Phát triển Ứng dụng (AD) - Khoa CNTT, Đại học Công nghiệp TP.HCM (IUH)  
> **Nhóm thực hiện:** Nhóm 07 (N07)  
> **Phiên bản:** v2.4 (Luxury Emerald Edition)  
> **Mục đích:** Cung cấp tài liệu hướng dẫn kiểm thử chi tiết, kịch bản thao tác thực tế từng bước (Step-by-Step UI Actions) nhằm kiểm tra toàn diện tính đúng đắn của logic nghiệp vụ, tính toàn vẹn dữ liệu trong CSDL và phản hồi của giao diện người dùng.

---

## 📋 MỤC LỤC
1. [Mục Tiêu & Tiêu Chuẩn Kiểm Thử](#1-mục-tiêu--tiêu-chuẩn-kiểm-thử)
2. [Ma Trận Phủ Kiểm Thử (Test Coverage Matrix)](#2-ma-trận-phủ-kiểm-thử-test-coverage-matrix)
3. [Dữ Liệu Kiểm Thử Chuẩn Bị Trước (Pre-requisite Test Data)](#3-dữ-liệu-kiểm-thử-chuẩn-bị-trước-pre-requisite-test-data)
4. [Chi Tiết 10 Kịch Bản Kiểm Thử Nghiệp Vụ Cốt Lõi](#4-chi-tiết-10-kịch-bản-kiểm-thử-nghiệp-vụ-cốt-lõi)
   - [TC-01: Đăng Nhập & Phân Quyền RBAC 4 Vai Trò](#tc-01-đăng-nhập--phân-quyền-rbac-4-vai-trò)
   - [TC-02: Bán Hàng POS & Trừ Kho Tự Động Theo Thuật Toán FEFO](#tc-02-bán-hàng-pos--trừ-kho-tự-động-theo-thuật-toán-fefo)
   - [TC-03: Bán Hàng Với Đơn Vị Tính Quy Đổi Đa Cấp](#tc-03-bán-hàng-với-đơn-vị-tính-quy-đổi-đa-cấp)
   - [TC-04: Thanh Toán Số VietQR Động & Cổng VNPay Sandbox](#tc-04-thanh-toán-số-vietqr-động--cổng-vnpay-sandbox)
   - [TC-05: Lưu & Phục Hồi Đơn Hàng Chờ (Holding Carts)](#tc-05-lưu--phục-hồi-đơn-hàng-chờ-holding-carts)
   - [TC-06: Đổi / Trả Hàng Hóa & Hoàn Tiền (2 Kịch Bản A & B)](#tc-06-đổi--trả-hàng-hóa--hoàn-tiền-2-kịch-bản-a--b)
   - [TC-07: Tiếp Nhận Lịch Hẹn Spa & Trừ Lượt Thẻ Spa Pass](#tc-07-tiếp-nhận-lịch-hẹn-spa--trừ-lượt-thẻ-spa-pass)
   - [TC-08: Check-in / Check-out Khách Sạn Pet Hotel & Tính Phí](#tc-08-check-in--check-out-khách-sạn-pet-hotel--tính-phí)
   - [TC-09: Kiểm Thử Quy Luật Sinh Mã Tự Động (<= 20 Ký Tự)](#tc-09-kiểm-thử-quy-luật-sinh-mã-tự-động--20-ký-tự)
   - [TC-10: Mở Ca, Chốt Ca Thu Ngân & Biên Bản Đối Soát Tiền Mặt](#tc-10-mở-ca-chốt-ca-thu-ngân--biên-bản-đối-soát-tiền-mặt)
5. [Quy Trình Ghi Nhận Lỗi (Bug Reporting Template)](#5-quy-trình-ghi-nhận-lỗi-bug-reporting-template)
6. [Tiêu Chí Nghiệm Thu (Acceptance Criteria)](#6-tiêu-chí-nghiệm-thu-acceptance-criteria)

---

## 1. MỤC TIÊU & TIÊU CHUẨN KIỂM THỬ
* **Mục tiêu:**
  - Đảm bảo các luồng thao tác trên giao diện người dùng (Swing FlatLaf) khớp 100% với các bảng dữ liệu trong SQL Server (`QuanLyCuaHangThuCung`).
  - Đảm bảo tính toán tài chính (Tổng tiền hàng, Giảm giá voucher, Khách đưa, Tiền thối, Đối soát tiền mặt) chính xác tuyệt đối không bị sai lệch số học.
  - Đảm bảo trừ kho chính xác theo nguyên tắc **FEFO (First-Expired, First-Out)**, không xảy ra tình trạng âm kho hoặc xuất sai lô hàng cận hạn.
  - Đảm bảo quy tắc bảo mật và phân quyền vai trò (RBAC) không cho phép nhân viên truy cập trái phép vào các phân hệ ngoài phạm vi quyền hạn.
* **Môi trường thử nghiệm:**
  - Hệ điều hành: Windows 10 / Windows 11.
  - JDK: Java 17 hoặc Java 21 LTS.
  - CSDL: Microsoft SQL Server 2019/2022 (Port 1433).
  - Look and Feel: FlatMacLightLaf (Luxury Emerald Theme).

---

## 2. MA TRẬN PHỦ KIỂM THỬ (TEST COVERAGE MATRIX)

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Phân hệ nghiệp vụ</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Mã kiểm thử</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Tên chức năng chính</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Màn hình View</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Lớp Controller liên quan</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Xác thực &amp; Bảo mật</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-01</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Đăng nhập &amp; Phân quyền RBAC 4 vai trò</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>LoginForm</code>, <code>MainDashboard</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DangNhapController</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Bán hàng &amp; Kho</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-02</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">POS Thanh toán &amp; Trừ kho FEFO theo lô</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangPOSPanel</code>, <code>HoaDonPreviewDialog</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangController</code>, <code>LoHangDAO</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Kho &amp; Quy đổi</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-03</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Bán hàng đơn vị quy đổi đa cấp</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangPOSPanel</code>, <code>ThietLapQuyDoiDialog</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangController</code>, <code>SanPhamDAO</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Thanh toán số</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-04</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Thanh toán VietQR Napas &amp; VNPay Sandbox</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>VietQRDialog</code>, <code>VNPayDialog</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>VNPayHelper</code>, <code>BanHangController</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Bán hàng song song</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-05</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Lưu đơn chờ &amp; Khôi phục giỏ hàng</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangPOSPanel</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangController</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Hậu mãi &amp; Đổi trả</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-06</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Trả hàng hoàn tiền &amp; Đổi sản phẩm bù trừ</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DoiTraHangPanel</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanHangController</code>, <code>HoaDonDAO</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Spa &amp; Chăm sóc</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-07</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Đặt lịch hẹn Spa &amp; Sử dụng Thẻ Spa Pass</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DatLichHenDialog</code>, <code>ThuCungDichVuPanel</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>ThuCungController</code>, <code>SpaPassController</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lưu trú thú cưng</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-08</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Check-in, theo dõi &amp; Check-out Pet Hotel</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>PetHotelPanel</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>PhongLuuTruDAO</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Cơ chế Sinh mã</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-09</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Sinh mã hóa đơn, đổi trả, hẹn, thẻ</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>MaTuSinhHelper</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>MaTuSinhHelper</code>, toàn bộ DAOs</td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Quản lý Tài chính</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><code>TC-10</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Bàn giao ca thu ngân &amp; Khớp két tiền mặt</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>BanGiaoCaPanel</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>GiaoCaController</code>, <code>BangGiaoCaDAO</code></td>
    </tr>
  </tbody>
</table>

---

## 3. DỮ LIỆU KIỂM THỬ CHUẨN BỊ TRƯỚC (PRE-REQUISITE TEST DATA)

Dữ liệu mẫu đã được nạp sẵn từ tệp `sql/HeThongQuanLyCuaHangThuCung.sql`:

1. **Tài khoản người dùng:**
   - Quản lý: `NVQL001` / `admin123` (Trần Quản Lý)
   - Thu ngân: `NVTN001` / `tn123` (Lê Anh Minh)
   - Bán hàng: `NVBH001` / `bh123` (Nguyễn Văn Bán)
   - Chăm sóc: `NVCS001` / `cs123` (Võ Thị Chăm Sóc)
2. **Khách hàng mẫu:**
   - Khách VIP: `0988111222` (Chị Mai Anh - 1.250 điểm tích lũy - Thú cưng: Bé Bông, Giống Poodle)
   - Khách thường: `0912345678` (Anh Hoàng Long - 150 điểm tích lũy - Thú cưng: Miu Miu, Mèo Anh Lông Ngắn)
3. **Sản phẩm & Lô hàng mẫu (FEFO):**
   - Sản phẩm `SP001` (Pate Mèo Ciao 80g):
     - Lô `LH001-A`: Hạn dùng `2026-11-15` (Cận date nhất), Tồn kho: `5 lon`.
     - Lô `LH001-B`: Hạn dùng `2027-06-30` (Hạn xa), Tồn kho: `50 lon`.
4. **Mã giảm giá mẫu:**
   - `PET50K`: Giảm trực tiếp 50.000 VNĐ cho đơn từ 200.000 VNĐ.
   - `GIAM10`: Giảm 10% tổng giá trị đơn hàng (Tối đa 100.000 VNĐ).

---

## 4. CHI TIẾT 10 KỊCH BẢN KIỂM THỬ NGHIỆP VỤ CỐT LÕI

---

### TC-01: ĐĂNG NHẬP & PHÂN QUYỀN RBAC 4 VAI TRÒ
* **Mục đích:** Kiểm tra cơ chế đăng nhập, bảo vệ mật khẩu và phân quyền hiển thị menu điều hướng theo vai trò người dùng.
* **Màn hình kiểm thử:** `LoginForm` $\rightarrow$ `MainDashboard`.
* **Tiền điều kiện:** Ứng dụng đã khởi chạy và hiển thị màn hình đăng nhập.
* **Các bước thực hiện:**
  1. Nhập Tên đăng nhập `NVQL001`, Mật khẩu `admin123` $\rightarrow$ Nhấn nút **ĐĂNG NHẬP**.
  2. Quan sát thanh Sidebar bên trái của màn hình `MainDashboard`.
  3. Nhấn nút **Đăng xuất** ở góc dưới Sidebar.
  4. Lần lượt đăng nhập bằng các tài khoản: `NVTN001` (tn123), `NVBH001` (bh123), `NVCS001` (cs123).
* **Kết quả mong đợi (UI):**
  - Đăng nhập thành công, xuất hiện thông báo chào mừng kèm họ tên và chức vụ của nhân viên.
  - **Với Quản lý (`NVQL001`):** Sidebar hiển thị đầy đủ 10 mục: `Tổng quan`, `Bán hàng POS`, `Kho & Lô hàng`, `Đổi trả hàng`, `Hồ sơ thú cưng`, `Lịch hẹn Spa`, `Khách sạn Pet Hotel`, `Bàn giao ca`, `Lịch phân ca`, `Quản lý nhân viên`.
  - **Với Thu ngân (`NVTN001`):** Chỉ hiển thị các mục: `Bán hàng POS`, `Đổi trả hàng`, `Bàn giao ca`, `Lịch trực cá nhân`. Các nút quản lý kho và quản lý nhân viên bị ẩn hoàn toàn.
  - **Với Kỹ thuật viên Spa (`NVCS001`):** Mặc định chuyển thẳng vào màn hình `Hồ sơ thú cưng`, hiển thị `Lịch hẹn Spa` và `Khách sạn Pet Hotel`. Nút `Bán hàng POS` và `Bàn giao ca` không xuất hiện.
* **Kiểm tra logic ngầm:**
  - Đối tượng `UserSession.getCurrentUser()` lưu đúng thông tin nhân viên đăng nhập.
  - Mật khẩu hiển thị dạng ẩn `JPasswordField`.

---

### TC-02: BÁN HÀNG POS & TRỪ KHO TỰ ĐỘNG THEO THUẬT TOÁN FEFO
* **Mục đích:** Kiểm tra quy trình bán hàng lẻ tại quầy, xuất hóa đơn K80 và kiểm chứng thuật toán trừ kho ưu tiên lô cận date nhất (`First-Expired, First-Out`).
* **Màn hình kiểm thử:** `BanHangPOSPanel` $\rightarrow$ `HoaDonPreviewDialog`.
* **Tiền điều kiện:** Đang đăng nhập tài khoản thu ngân `NVTN001`. Lô `LH001-A` của sản phẩm `SP001` còn `5` lon (HSD: 15/11/2026), Lô `LH001-B` còn `50` lon (HSD: 30/06/2027).
* **Các bước thực hiện:**
  1. Tại ô `Số điện thoại khách`, nhập `0988111222` $\rightarrow$ Nhấn Enter.
  2. Trên khu vực Catalog bên trái, nhấn vào thẻ sản phẩm **Pate Mèo Ciao 80g (`SP001`)**.
  3. Tại bảng giỏ hàng, nhấp đúp vào cột **SL** của dòng sản phẩm `SP001`, gõ số lượng: `7` $\rightarrow$ Nhấn Enter.
  4. Nhập mã voucher `GIAM10` vào ô `txtVoucher` $\rightarrow$ Nhấn nút **Áp dụng**.
  5. Bấm vào nút gợi ý tiền nhanh **[500k]**.
  6. Nhấn phím **F9** hoặc click nút **THANH TOÁN (F9)**.
  7. Chọn hình thức **Tiền mặt** và nhấn **Xác nhận thanh toán**.
* **Kết quả mong đợi (UI):**
  - Thông tin khách hàng hiển thị: *Chị Mai Anh - Hạng VIP (1.250 điểm)*.
  - Giỏ hàng cập nhật số lượng `7`, đơn giá `35.000đ`, thành tiền hàng `245.000đ`.
  - Giảm giá `10%` hiển thị: `-24.500đ`. Tổng thanh toán: `220.500đ`.
  - Khách đưa `500.000đ`, nhãn `Thối lại` hiển thị số tiền xanh lá: `279.500đ`.
  - Hộp thoại `HoaDonPreviewDialog` mở ra hiển thị mã hóa đơn dạng `HDyyMMdd-NVxx-xxx` chuẩn mẫu K80.
* **Kiểm tra CSDL & Logic ngầm:**
  - Truy vấn bảng `LoHang` trong SQL Server:
    - Lô `LH001-A` bị trừ toàn bộ `5` lon $\rightarrow$ Tồn kho còn lại: `0`.
    - Lô `LH001-B` bị trừ tiếp `2` lon $\rightarrow$ Tồn kho còn lại: `48`.
  - Bảng `ChiTietXuatLo` ghi nhận 2 bản ghi xuất kho tương ứng với 2 lô khác nhau cho cùng 1 hóa đơn.
  - Điểm tích lũy của khách hàng tăng thêm `2 điểm` (Tỷ lệ 100.000đ = 1 điểm).

---

### TC-03: BÁN HÀNG VỚI ĐƠN VỊ TÍNH QUY ĐỔI ĐA CẤP
* **Mục đích:** Kiểm tra khả năng bán hàng theo đơn vị quy đổi (Lon $\rightarrow$ Khay $\rightarrow$ Thùng) và quy đổi chính xác về đơn vị cơ sở khi xuất kho.
* **Màn hình kiểm thử:** `BanHangPOSPanel`.
* **Tiền điều kiện:** Sản phẩm `SP001` có quy đổi: `1 Khay = 12 Lon`. Tồn kho ban đầu của lô hàng tương ứng là `48 Lon`.
* **Các bước thực hiện:**
  1. Thêm sản phẩm `SP001` vào giỏ hàng.
  2. Tại cột `ĐVT`, chọn đơn vị: **Khay**.
  3. Nhập số lượng: `2`.
  4. Thực hiện thanh toán hóa đơn bằng tiền mặt.
* **Kết quả mong đợi (UI):**
  - Đơn giá tự động nhân theo hệ số quy đổi: `35.000đ x 12 = 420.000đ/Khay`.
  - Thành tiền: `840.000đ`.
* **Kiểm tra CSDL:**
  - Lô hàng tương ứng trong CSDL bị trừ chính xác `24 Lon` (`2 Khay x 12`).
  - Tồn kho còn lại: `24 Lon`. Không xảy ra lỗi lệch tỷ lệ.

---

### TC-04: THANH TOÁN SỐ VIETQR ĐỘNG & CỔNG VNPAY SANDBOX
* **Mục đích:** Kiểm tra tích hợp cổng thanh toán trực tuyến, mã QR động và máy chủ đón nhận kết quả thanh toán tự động (Loopback Server).
* **Màn hình kiểm thử:** `BanHangPOSPanel` $\rightarrow$ `VietQRDialog` / `VNPayDialog`.
* **Tiền điều kiện:** Máy tính có kết nối Internet để gọi API tạo mã VietQR và cổng VNPay Sandbox.
* **Các bước thực hiện:**
  1. Tạo giỏ hàng có tổng thanh toán `150.000đ`.
  2. Tại ô ComboBox `Phương thức`, chọn **Cổng VNPay**.
  3. Nhấn **THANH TOÁN (F9)**.
  4. Hộp thoại `VNPayDialog` xuất hiện, nhấn nút **Mở trang thanh toán VNPay**.
  5. Trình duyệt mở trang cổng Sandbox $\rightarrow$ Chọn ngân hàng demo (NCB), nhập số thẻ test và OTP `123456`.
  6. Sau khi thanh toán thành công trên web, quay lại ứng dụng.
* **Kết quả mong đợi (UI):**
  - Hộp thoại `VNPayDialog` tự động phát hiện kết quả thanh toán từ Local Loopback Server (Port 8888), hiển thị biểu tượng tích xanh: *“Thanh toán thành công qua VNPay!”*.
  - Hộp thoại thanh toán tự động đóng lại, chuyển ngay sang màn hình in hóa đơn `HoaDonPreviewDialog`.
* **Kiểm tra logic ngầm:**
  - Hóa đơn trong CSDL ghi nhận phương thức thanh toán là `VNPay Gateway`, trạng thái `Đã thanh toán`.

---

### TC-05: LƯU & PHỤC HỒI ĐƠN HÀNG CHỜ (HOLDING CARTS)
* **Mục đích:** Kiểm tra tính năng giữ đơn hàng tạm thời khi khách chưa thanh toán ngay để phục vụ khách hàng khác mà không làm mất giỏ hàng.
* **Màn hình kiểm thử:** `BanHangPOSPanel`.
* **Tiền điều kiện:** Quầy thu ngân đang có nhiều khách xếp hàng.
* **Các bước thực hiện:**
  1. Nhập khách hàng `0912345678` (Anh Hoàng Long), chọn 3 sản phẩm vào giỏ hàng (Tổng tiền: `380.000đ`).
  2. Khách báo cần đi chọn thêm thức ăn cho mèo, nhấn nút **Lưu tạm (Đơn chờ)**.
  3. Quan sát nút tác vụ nhanh `Đơn chờ (x)` trên thanh công cụ.
  4. Nhấn **Tạo đơn mới** $\rightarrow$ Thực hiện thanh toán 1 đơn hàng khác cho khách vãng lai (Tổng tiền: `50.000đ`).
  5. Sau khi thanh toán xong, nhấn nút **Đơn chờ (1)** $\rightarrow$ Chọn đơn hàng của khách `Anh Hoàng Long`.
* **Kết quả mong đợi (UI):**
  - Khi bấm `Lưu tạm`, giỏ hàng được dọn sạch về trạng thái rỗng, số đếm trên nút `Đơn chờ` tăng lên thành `Đơn chờ (1)`.
  - Đơn của khách vãng lai thanh toán bình thường mà không bị lẫn dữ liệu của đơn trước.
  - Khi khôi phục đơn chờ, toàn bộ thông tin khách hàng `0912345678`, danh sách 3 sản phẩm và số lượng được nạp lại nguyên vẹn vào giỏ hàng.

---

### TC-06: ĐỔI / TRẢ HÀNG HÓA & HOÀN TIỀN (2 KỊCH BẢN A & B)
* **Mục đích:** Kiểm tra tính đúng đắn của chính sách đổi trả hàng trong vòng 07 ngày, tính toán bù trừ tiền và hoàn nhập tồn kho.
* **Màn hình kiểm thử:** `DoiTraHangPanel`.
* **Tiền điều kiện:** Đã có hóa đơn `HD261029-NV01-001` thanh toán sản phẩm `SP001` (Giá 35.000đ, SL: 2).
* **Các bước thực hiện Kịch bản A (Trả hàng - Hoàn tiền):**
  1. Mở màn hình `Đổi trả hàng`, nhập mã hóa đơn `HD261029-NV01-001` $\rightarrow$ Nhấn **Tra cứu HĐ**.
  2. Tích chọn sản phẩm `SP001`, nhập `SL Trả`: `1`.
  3. Chọn tình trạng hàng: **Nguyên tem, vỏ hộp mới**. Chọn lý do: **Thú cưng không dùng**.
  4. Chọn RadioButton **Kịch bản A: Trả hàng - Hoàn tiền**.
  5. Nhấn nút **XÁC NHẬN ĐỔI TRẢ & HOÀN TIỀN**.
* **Các bước thực hiện Kịch bản B (Đổi sang món khác):**
  1. Thực hiện lại bước 1 và 2.
  2. Chọn RadioButton **Kịch bản B: Đổi sang sản phẩm khác**.
  3. Tại ComboBox `Chọn món mới`, chọn sản phẩm có giá `50.000đ`, nhập `SL Mới`: `1`.
  4. Quan sát số tiền chênh lệch hiển thị và bấm xác nhận.
* **Kết quả mong đợi (UI):**
  - **Kịch bản A:** Hiển thị tiền hoàn lại: `35.000đ`, khấu trừ điểm VIP tương ứng. Nhập hoàn kho `+1` sản phẩm.
  - **Kịch bản B:** Hiển thị rõ: *“Khách thanh toán thêm: 15.000đ (50.000đ - 35.000đ)”*.
  - Sinh phiếu đổi trả có định dạng `DTyyMMdd-xxx`.
* **Kiểm tra CSDL:**
  - Tồn kho của lô hàng ban đầu trong bảng `LoHang` được cộng hoàn trả thêm `1`.

---

### TC-07: TIẾP NHẬN LỊCH HẸN SPA & TRỪ LƯỢT THẺ SPA PASS
* **Mục đích:** Kiểm tra quy trình tiếp nhận lịch hẹn, chọn khung giờ không bị trùng và tính năng khấu trừ lượt gói dịch vụ Spa Pass.
* **Màn hình kiểm thử:** `DatLichHenDialog` $\rightarrow$ `BanHangPOSPanel`.
* **Tiền điều kiện:** Khách hàng `0988111222` sở hữu Thẻ Spa Pass còn `3` lượt tắm sấy.
* **Các bước thực hiện:**
  1. Mở màn hình `Lịch hẹn Spa`, nhấn **Tiếp nhận đặt lịch mới**.
  2. Nhập SĐT `0988111222`, chọn thú cưng `Bé Bông (Poodle)`.
  3. Chọn dịch vụ: **Gói Tắm Sấy Vệ Sinh Toàn Diện**. Chọn Kỹ thuật viên: **Võ Thị Lan**.
  4. Bấm chọn khung giờ chiều: **[14:30]** $\rightarrow$ Nhấn **Xác nhận đặt lịch**.
  5. Mở màn hình `Bán hàng POS`, nhập SĐT `0988111222`.
  6. Tại dòng thông tin thẻ Spa Pass, bấm nút **Dùng Spa Pass**.
  7. Nhấn **THANH TOÁN (F9)**.
* **Kết quả mong đợi (UI):**
  - Khung giờ `14:30` đổi sang màu đỏ kèm tên bé cưng, không thể bị đặt trùng lần thứ 2.
  - Tại quầy POS, dịch vụ được đưa vào giỏ hàng với giá thanh toán `0 đ (Đã trừ lượt Spa Pass)`.
  - Thẻ Spa Pass của khách cập nhật số lượt còn lại: `2 lượt`.
* **Kiểm tra CSDL:**
  - Bảng `LichHen` tạo mới bản ghi với mã dạng `LHyMMdd-xxx`.
  - Bảng `TheSpaPass` cập nhật trường `SoLuotConLai = SoLuotConLai - 1`.

---

### TC-08: CHECK-IN / CHECK-OUT KHÁCH SẠN PET HOTEL & TÍNH PHÍ
* **Mục đích:** Kiểm tra quy trình quản lý phòng lưu trú, cập nhật trạng thái phòng theo thời gian thực và tự động tính tiền phòng theo ngày thực tế.
* **Màn hình kiểm thử:** `PetHotelPanel`.
* **Tiền điều kiện:** Phòng `VIP-01` đang ở trạng thái **Phòng trống** (Màu xanh lá, Đơn giá: 250.000đ/ngày).
* **Các bước thực hiện:**
  1. Nhấn vào ô phòng **VIP-01** trên ma trận phòng.
  2. Tại khung thao tác bên phải, chọn thú cưng `Bé Bông`, chọn ngày vào `Hôm nay`, chế độ ăn `Pate tươi 2 bữa/ngày`.
  3. Nhấn nút **Xác nhận Check-in**.
  4. Sau khi check-in, nhấn lại vào phòng `VIP-01` và bấm nút **Check-out & Tính tiền**.
* **Kết quả mong đợi (UI):**
  - Khi check-in: Ô phòng `VIP-01` lập tức đổi màu sang **Đỏ hồng (Đang có khách)**, thẻ KPI `Đang có khách` tăng lên 1, `Phòng trống` giảm đi 1.
  - Khi check-out: Hệ thống tự động tính chi phí: `Số ngày x 250.000đ`, kết xuất hóa đơn sang quầy POS và chuyển phòng sang màu **Vàng cam (Đang dọn dẹp khử trùng)**.
* **Kiểm tra CSDL:**
  - Bảng `PhongLuuTru` cập nhật đúng `TrangThai` tương ứng (`DANG_O`, `DON_DEP`, `TRONG`).

---

### TC-09: KIỂM THỬ QUY LUẬT SINH MÃ TỰ ĐỘNG (<= 20 KÝ TỰ)
* **Mục đích:** Đảm bảo toàn bộ các mã khóa chính tự sinh tuân thủ chặt chẽ độ dài trường `NVARCHAR(20)` của SQL Server, không gây lỗi tràn chuỗi (String Truncation Exception).
* **Lớp kiểm thử:** `src/utils/MaTuSinhHelper.java`.
* **Các bước thực hiện:**
  1. Kích hoạt liên tục các hành động: Tạo mới 10 Hóa đơn, 5 Phiếu đổi trả, 5 Lịch hẹn, 3 Khách hàng mới, 2 Thẻ Spa Pass.
  2. Đo độ dài chuỗi ký tự (String Length) của từng mã được sinh ra.
* **Bảng đối soát tiêu chuẩn:**

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Phương thức sinh mã</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Định dạng sinh ra</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Độ dài thực tế</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Giới hạn CSDL</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Đánh giá</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaHoaDon()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>HDyyMMdd-NVxx-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>15 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Mã chi tiết hóa đơn</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>&lt;MaHD&gt;_CTx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>19 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaDoiTra()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DTyyMMdd-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>12 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaLichHen()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>LHyMMdd-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>12 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaKhachHang()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>KHxxxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>6 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaThuCung()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>TCxxxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>6 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>sinhMaSpaPass()</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>PASS-sdt-xx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>14 ký tự</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">20 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; color: #059669;"><b>ĐẠT</b></td>
    </tr>
  </tbody>
</table>

---

### TC-10: MỞ CA, CHỐT CA THU NGÂN & BIÊN BẢN ĐỐI SOÁT TIỀN MẶT
* **Mục đích:** Kiểm tra quy trình bàn giao ca thu ngân cuối ngày, tự động đối soát doanh thu tiền mặt thực tế vs hệ thống và xử lý chênh lệch két.
* **Màn hình kiểm thử:** `BanGiaoCaPanel`.
* **Tiền điều kiện:** Thu ngân `NVTN001` đã thực hiện các hóa đơn bán hàng tiền mặt trong ca. Tiền đầu ca định mức trong két là `1.500.000đ`.
* **Các bước thực hiện:**
  1. Mở màn hình `Bàn giao ca`.
  2. Quan sát ô `Doanh thu tiền mặt hệ thống` và `Tổng tiền cuối ca lý thuyết`.
  3. **Kịch bản 10.1 (Khớp két 100%):** Nhập ô `Tiền mặt thực tế đếm được` đúng bằng số tiền lý thuyết $\rightarrow$ Nhấn Enter.
  4. **Kịch bản 10.2 (Lệch két thiếu tiền):** Giả định trong két bị thiếu 50.000đ, sửa lại số tiền thực tế ít hơn lý thuyết 50.000đ $\rightarrow$ Nhấn Enter.
  5. Nhập vào ô `Lý do chênh lệch`: *“Khách thanh toán nhầm tiền rách phải thu hồi”*.
  6. Nhấn nút **XÁC NHẬN BÀN GIAO & KẾT CA**.
* **Kết quả mong đợi (UI):**
  - Kịch bản 10.1: Nhãn chênh lệch hiển thị màu xanh lá: `0 đ (Khớp két 100%)`.
  - Kịch bản 10.2: Nhãn chênh lệch hiển thị màu đỏ: `-50.000 đ (Thiếu tiền két)`. Nếu chưa nhập lý do chênh lệch, nút xác nhận sẽ cảnh báo yêu cầu giải trình.
  - Xuất hiện biên bản bàn giao ca chốt số liệu thành công.
* **Kiểm tra CSDL:**
  - Bảng `BangGiaoCa` ghi nhận đúng `TienDauCa`, `TienHeThongGhiNhan`, `TienThucTeKiemDem`, `TienChenhLech` và `LyDoChenhLech`.

---

## 5. QUY TRÌNH GHI NHẬN LỖI (BUG REPORTING TEMPLATE)

Khi thực hiện kiểm thử phát hiện lỗi sai khác giữa kết quả thực tế và kết quả mong đợi, người kiểm thử ghi nhận theo mẫu chuẩn sau:

```markdown
### [BUG-ID] Tiêu đề tóm tắt lỗi
- **Mã kịch bản liên quan:** (Ví dụ: TC-02 Bán hàng POS)
- **Mức độ nghiêm trọng:** [Blocker / Critical / Major / Minor]
- **Tài khoản kiểm thử:** (Ví dụ: NVTN001)
- **Các bước tái hiện (Steps to Reproduce):**
  1. Bước 1...
  2. Bước 2...
- **Kết quả thực tế (Actual Result):** (Mô tả giao diện hoặc lỗi xuất hiện trên console/database)
- **Kết quả mong đợi (Expected Result):** (Mô tả đúng theo tài liệu đặc tả nghiệp vụ)
- **Ảnh chụp màn hình / Log Exception đính kèm:** (Nếu có)
```

---

## 6. TIÊU CHÍ NGHIỆM THU (ACCEPTANCE CRITERIA)

Dự án được đánh giá **ĐẠT YÊU CẦU NGHIỆM THU** khi thỏa mãn đầy đủ các tiêu chí:
1. ✅ **10/10 Kịch bản kiểm thử (TC-01 đến TC-10)** đạt kết quả **PASS**.
2. ✅ Không xảy ra bất kỳ ngoại lệ nào (`NullPointerException`, `SQLException`, `UnknownStyleException`) trong quá trình vận hành liên tục.
3. ✅ Thuật toán trừ kho FEFO bảo toàn đúng số lượng tồn theo từng lô và hạn dùng.
4. ✅ Mã nghiệp vụ tự sinh 100% $\le 20$ ký tự, tương thích hoàn toàn với schema SQL Server.
5. ✅ Giao diện FlatLaf đồng bộ nhận diện thương hiệu Luxury Emerald & Mint Sage, bo góc mượt mà 16px - 20px, phản hồi tức thì dưới 0.5 giây.

---
*Tài liệu được biên soạn bởi Nhóm 07 (N07) - Đề tài môn học Phát Triển Ứng Dụng (AD) - Khoa CNTT, IUH.*
