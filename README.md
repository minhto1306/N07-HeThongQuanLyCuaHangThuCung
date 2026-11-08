# 🐾 HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (MIO PET SHOP MANAGEMENT SYSTEM)

> **Đồ án học phần:** Phát triển Ứng dụng (Application Development - AD)  
> **Đơn vị đào tạo:** Khoa Công nghệ Thông tin - Trường Đại học Công nghiệp TP.HCM (IUH)  
> **Nhóm thực hiện:** Nhóm 07 (N07)  
> **Nền tảng & Kiến trúc:** Kiến trúc Đa tầng N-Tier Desktop (Entity - DAO - BUS - GUI - Utils) • Microsoft SQL Server • FlatLaf Modern UI (Luxury Emerald & Mint Sage Edition)

---

## 👥 THÀNH VIÊN NHÓM VÀ PHÂN CÔNG NHIỆM VỤ

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center; width: 50px;">STT</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Họ và Tên</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Mã Sinh Viên</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Vai trò</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Phân hệ nghiệp vụ phụ trách</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Nhánh GitHub (Branch)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>1</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lê Khãi Minh</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><i>(Trưởng nhóm)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Leader / Fullstack</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Bán hàng POS, In hóa đơn K80, Tích hợp VietQR Napas 247 &amp; Cổng VNPay Sandbox</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/pos-ban-hang</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>2</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Tô Thanh Đạt</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><i>(Thành viên)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Kho hàng, Danh mục sản phẩm, Quản lý Lô hàng cận date (FEFO), Đổi trả hàng hóa &amp; Hoàn kho, Thiết lập bảng quy đổi đơn vị tính đa cấp</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/kho-san-pham-lo-hang</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>3</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Võ Thị Hồng Ngọc</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><i>(Thành viên)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Hồ sơ thú cưng, Sổ theo dõi sức khỏe &amp; tiêm phòng, Đặt lịch hẹn Spa &amp; Grooming, Khách sạn lưu trú (Pet Hotel)</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/thu-cung-dich-vu-hotel</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>4</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lê Anh Minh</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><i>(Thành viên)</i></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;"><b>Developer</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;">Quản lý Nhân sự, Đăng nhập &amp; Phân quyền RBAC 4 vai trò, Ma trận xếp ca trực tuần, Bàn giao ca thu ngân &amp; Két tiền, Dashboard báo biểu</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>feature/nhan-su-giao-ca-dashboard</code></td>
    </tr>
  </tbody>
</table>

---

## 📖 BÀI TOÁN NGHIỆP VỤ & CƠ CHẾ HOẠT ĐỘNG KHỚP VỚI GIAO DIỆN

Hệ thống được thiết kế chuyên biệt cho mô hình chuỗi siêu thị thú cưng kết hợp dịch vụ chăm sóc cao cấp (Bán lẻ đồ thú cưng, Thức ăn, Spa Grooming, Pet Hotel). Dưới đây là mô tả chi tiết từng quy trình nghiệp vụ tương ứng trực tiếp với các thao tác trên màn hình:

### 1. Phân hệ Bán hàng POS & Điều phối Thu ngân (`BanHangPOSPanel`)
* **Bố cục màn hình 2 vùng chuyên nghiệp:**
  - **Nửa trái (Catalog trực quan):** Chia danh mục bằng các nút lọc (`TẤT CẢ`, `Hàng hóa`, `Dịch vụ`, `Thức ăn`, `Phụ kiện`, `Thuốc thú y`). Thẻ sản phẩm hiển thị ảnh, tên, giá bán và số lượng tồn kho khả dụng. Thẻ dịch vụ có banner tím và hướng dẫn chọn thú cưng / kỹ thuật viên.
  - **Nửa phải (Giỏ hàng & Khách hàng & Thanh toán):** Thông tin khách hàng VIP, giỏ hàng, bảng tổng tiền và các nút tác vụ.
* **Quy trình bán hàng từng bước:**
  1. **Nhận diện khách hàng:** Thu ngân nhập Số điện thoại khách vào ô tìm kiếm (`txtSoDienThoai`). Hệ thống tự động tra cứu họ tên, cấp bậc thành viên (VIP/Thân thiết) và số điểm tích lũy hiện có (Quy đổi: `1 điểm = 1.000 VNĐ`). Nếu khách có thú cưng, ComboBox thú cưng tự động nạp danh sách bé cưng để phục vụ dịch vụ.
  2. **Thêm món vào giỏ hàng:**
     - *Hàng hóa:* Nhấn trực tiếp vào thẻ sản phẩm hoặc quét mã Barcode. Món hàng lập tức đưa vào bảng giỏ hàng. Cột `SL` hỗ trợ **nhấp đúp chuột để gõ số lượng trực tiếp từ bàn phím**.
     - *Dịch vụ Spa:* Nhấn vào thẻ dịch vụ $\rightarrow$ Màn hình hiển thị hộp thoại chọn cân nặng thú cưng (để áp giá chính xác theo size: `< 5kg`, `5-10kg`, `> 10kg`) và phân công Kỹ thuật viên chăm sóc.
  3. **Gợi ý bán kèm thông minh (Smart Cross-Sell):** Khi giỏ hàng có thức ăn hạt hoặc dịch vụ tắm, thanh banner vàng cam nổi lên đề xuất: *“Gợi ý: Mua kèm xịt ve rận Bio-Care giảm 10%”*. Thu ngân chỉ cần bấm `+ Thêm (-10%)` để bổ sung vào đơn.
  4. **Áp dụng Mã giảm giá (Voucher):** Nhập mã khuyến mãi vào ô `txtVoucher` (ví dụ: `PET50K` giảm 50.000đ hoặc `GIAM10` giảm 10% tổng đơn) và bấm `Áp dụng`.
  5. **Tính toán & Tiền khách đưa:**
     - Hệ thống hiển thị rõ ràng: `Tổng tiền hàng` $\rightarrow$ `Giảm giá` $\rightarrow$ `TỔNG THANH TOÁN (Lớn màu xanh)`.
     - Cung cấp hàng nút bấm gợi ý tiền mặt nhanh: `[Đủ tiền]`, `[50k]`, `[100k]`, `[200k]`, `[500k]`. Khi bấm, hệ thống tự động điền vào ô `Khách đưa` và tính ngay `Tiền thối lại` với màu xanh lá trực quan.
  6. **Đơn hàng chờ phục vụ song song (Holding Carts):** Nếu khách đang chờ người nhà chuyển tiền hoặc bé cưng đang tắm dở trong phòng Spa, thu ngân bấm nút `Lưu tạm (Đơn chờ)` $\rightarrow$ Toàn bộ giỏ hàng và thông tin khách được đưa vào danh sách đơn chờ. Thu ngân có thể bấm `Tạo đơn mới` để thanh toán ngay cho khách tiếp theo. Khi khách quay lại, bấm `Đơn chờ (x)` để khôi phục giỏ hàng trong 1 giây.
  7. **Thanh toán & Xuất hóa đơn (`F9`):**
     - Thu ngân nhấn phím tắt `F9` hoặc nút `THANH TOÁN (F9)`.
     - **Tiền mặt:** Hệ thống xác nhận và mở hộp thoại xem trước hóa đơn (`HoaDonPreviewDialog`) chuẩn mẫu in K80.
     - **Chuyển khoản VietQR:** Bật hộp thoại `VietQRDialog` tự động sinh mã QR động chuẩn Napas 247 nhúng sẵn số tiền đơn hàng cố định (chuẩn EMVCo). Khách quét app ngân hàng tự điền đúng số tiền, chống hoàn toàn rủi ro chuyển thiếu/thừa.
     - **Cổng thanh toán điện tử VNPay Sandbox:** Bật hộp thoại `VNPayDialog` kết nối VNPay API v2.1.0 qua mã băm HMAC-SHA512 bảo mật. Tích hợp máy chủ lắng nghe nội bộ (Local Loopback Server) tự động bắt tín hiệu thanh toán thành công từ trình duyệt để tự đóng hộp thoại và chốt đơn tự động.

---

### 2. Quản lý Kho, Xuất kho Cận Date (FEFO) & Quy đổi Đơn vị (`KhoLoHangPanel`)
* **Thuật toán trừ kho FEFO (First-Expired, First-Out):**
  - Khi một sản phẩm được bán ra, hệ thống **không trừ ngẫu nhiên** mà tự động truy vấn các lô hàng (`LoHang`) còn hạn sử dụng của sản phẩm đó, sắp xếp tăng dần theo `NgayHetHan`.
  - Hệ thống ưu tiên xuất hết số lượng của lô cận date nhất trước, nếu chưa đủ số lượng đơn hàng sẽ tự động trừ tiếp sang lô tiếp theo, đảm bảo giảm thiểu tối đa hàng quá hạn hỏng hóc trong kho.
* **Cảnh báo hạn sử dụng 3 cấp độ màu:**
  - 🟢 **Xanh lá (An toàn):** Hạn sử dụng còn trên 60 ngày.
  - 🟡 **Vàng cam (Cận date):** Hạn sử dụng còn từ 15 đến 60 ngày (Cần kích hoạt chương trình xả hàng khuyến mãi).
  - 🔴 **Đỏ nguy cấp:** Hạn sử dụng dưới 15 ngày hoặc đã hết hạn (Cảnh báo thu hồi tiêu hủy).
* **Quy đổi Đơn vị tính Đa cấp (`ThietLapQuyDoiDialog`):**
  - Khai báo linh hoạt tỷ lệ quy đổi: `1 Thùng = 12 Khay = 48 Lon`.
  - Khi khách mua lẻ 2 Lon hoặc mua 1 Khay, hệ thống tự động quy đổi chính xác về đơn vị cơ sở để trừ tồn kho thực tế của lô hàng tương ứng.

---

### 3. Nghiệp vụ Đổi / Trả Hàng Hóa & Hoàn Tiền (`DoiTraHangPanel`)
* **Quy định chính sách:** Hỗ trợ đổi trả trong vòng 07 ngày kể từ ngày in hóa đơn.
* **Thao tác nghiệp vụ chuẩn:**
  1. **Tra cứu hóa đơn gốc:** Nhập Mã hóa đơn (vd: `HD261029-NV01-001`) hoặc Số điện thoại khách hàng vào ô tra cứu $\rightarrow$ Bấm `Tra cứu HĐ` (hoặc bấm `Chọn từ Lịch sử`).
  2. **Chọn sản phẩm và khai báo lý do:** Tích chọn checkbox sản phẩm khách mang đến trả. Nhập số lượng trả, chọn lý do (Hàng lỗi, thú cưng không hợp vị, mua nhầm size) và tình trạng hàng hóa (`Nguyên tem, vỏ hộp mới`, `Đã bóc seal`, `Lỗi do nhà sản xuất`).
  3. **Lựa chọn 1 trong 2 kịch bản:**
     - **Kịch bản A (Trả hàng - Hoàn tiền):** Hệ thống tính số tiền hoàn trả cho khách, tự động khấu trừ điểm tích lũy VIP đã cộng trước đó. Nếu sản phẩm còn nguyên tem, hệ thống tự động nhập hoàn tồn kho lại cho lô hàng ban đầu.
     - **Kịch bản B (Đổi sang sản phẩm khác):** Thu ngân chọn sản phẩm mới từ danh sách và số lượng đổi mới. Hệ thống tự động đối trừ chênh lệch giá:
       - *Món mới đắt hơn:* Hiển thị rõ số tiền khách cần thanh toán thêm.
       - *Món mới rẻ hơn:* Hiển thị số tiền cửa hàng cần hoàn trả lại cho khách.
  4. Nhấn `XÁC NHẬN ĐỔI TRẢ & HOÀN TIỀN` $\rightarrow$ Tự động sinh phiếu đổi trả (`DTyyMMdd-xxx`), cập nhật lại tồn kho và xuất phiếu in xác nhận.

---

### 4. Hồ Sơ Thú Cưng, Dịch Vụ Spa & Khách Sạn Pet Hotel
* **Quản lý Hồ sơ & Sổ sức khỏe (`ThuCungDichVuPanel`):**
  - Quản lý định danh từng thú cưng (Tên bé, giống loài Chó/Mèo, màu lông, ngày sinh, số microchip định danh, thông tin chủ nuôi).
  - Sổ theo dõi sức khỏe: Ghi nhận cân nặng, nhiệt độ, tình trạng ve rận, da lông và lịch sử tiêm phòng vắc-xin dại định kỳ.
* **Đặt lịch hẹn Spa & Grooming (`DatLichHenDialog` / `QuanLyLichHenPanel`):**
  - Đặt lịch hẹn trực quan theo các khung giờ cố định (Time Slots): Ca sáng (`08:30`, `09:30`, `10:30`, `11:00`) và Ca chiều (`13:30`, `14:30`, `15:30`, `16:30`, `17:30`, `18:30`).
  - Chọn kỹ thuật viên chăm sóc, tự động phát hiện trùng lịch để tránh quá tải ca làm việc.
* **Gói Thẻ Dịch Vụ Trọn Gói (Spa Pass):**
  - Khách hàng có thể mua thẻ gói 5 buổi hoặc 10 buổi (`PASS-0988111222-01`).
  - Khi đến quầy POS làm dịch vụ, thu ngân chỉ cần bấm nút `Dùng Spa Pass` để trừ 1 lượt sử dụng mà không cần thanh toán tiền mặt.
* **Khách sạn Lưu Trú Thú Cưng (`PetHotelPanel`):**
  - **Ma trận 12 phòng lưu trú tiêu chuẩn/VIP:** Màu sắc thẻ phòng thay đổi trực quan theo thời gian thực (Xanh lá: *Phòng trống sẵn sàng*, Đỏ hồng: *Đang có khách lưu trú*, Vàng cam: *Đang dọn dẹp khử trùng tia cực tím*).
  - **Quy trình Check-in:** Nhấn vào phòng trống $\rightarrow$ Chọn thú cưng, ngày vào, ngày dự kiến đón, gói dinh dưỡng (Pate tươi, hạt dinh dưỡng ngày 2 cữ).
  - **Quy trình Check-out:** Nhấn vào phòng đang lưu trú $\rightarrow$ Bấm `Check-out & Tính tiền`. Hệ thống tự động tính số ngày lưu trú thực tế nhân đơn giá phòng, kết xuất chi phí đưa sang hóa đơn và chuyển trạng thái phòng sang `Đang dọn dẹp`.

---

### 5. Phân Ca Trực & Bàn Giao Ca Thu Ngân (`BanGiaoCaPanel` / `LichPhanCaPanel`)
* **Ma trận phân ca tuần:** Chuẩn hóa 4 ca làm việc/ngày, xếp lịch trực linh hoạt cho nhân viên theo các ca: Sáng (`07:30 - 12:00`), Chiều (`12:00 - 17:00`), Tối (`17:00 - 21:30`) và Ca gãy (`08:00 - 17:00`).
* **Quy trình bàn giao ca & đối soát két tiền:**
  1. Đầu ca: Ghi nhận số tiền mặt ban đầu để trong két (`Tiền đầu ca`).
  2. Cuối ca: Hệ thống tự động tổng hợp toàn bộ doanh thu tiền mặt phát sinh trong ca (`Doanh thu tiền mặt hệ thống`).
  3. Hệ thống tính `Tổng tiền cuối ca lý thuyết = Tiền đầu ca + Doanh thu mặt`.
  4. Thu ngân đếm tiền mặt thực tế trong két và nhập vào ô `Tiền mặt thực tế đếm được`.
  5. Hệ thống tự động so khớp và tính `Chênh lệch`:
     - Nếu chênh lệch bằng 0: Hiển thị nhãn xanh `0 đ (Khớp két 100%)`.
     - Nếu thừa hoặc thiếu: Bắt buộc thu ngân phải nhập vào khung `Lý do chênh lệch` (ví dụ: Khách gửi tiền tip, làm rách tiền lẻ...).
  6. Nhấn `XÁC NHẬN BÀN GIAO & KẾT CA` để chốt sổ và in biên bản giao ca.

---

### 6. Quy Luật Sinh Mã Nghiệp Vụ Tự Động (`MaTuSinhHelper`)
Toàn bộ mã nghiệp vụ được sinh tự động theo quy luật chặt chẽ, đảm bảo tính duy nhất, có ý nghĩa thời gian và **tuyệt đối không vượt quá giới hạn 20 ký tự của CSDL**:

<table border="1" style="border-collapse: collapse; width: 100%; border: 1px solid #cbd5e1;">
  <thead>
    <tr style="background-color: #f1f5f9;">
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Loại đối tượng</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Tiền tố &amp; Quy luật</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">Chiều dài</th>
      <th style="padding: 8px; border: 1px solid #cbd5e1;">Ví dụ thực tế</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Hóa đơn bán hàng</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>HDyyMMdd-NVxx-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">15 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>HD261029-NV01-001</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Chi tiết hóa đơn</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>&lt;MãHĐ&gt;_CTx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">19 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>HD261029-NV01-001_CT1</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Phiếu đổi trả</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DTyyMMdd-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">12 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>DT261029-001</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Lịch hẹn Spa</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>LHyMMdd-xxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">12 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>LH61029-001</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Khách hàng mới</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>KHxxxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">6 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>KH0015</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Thú cưng mới</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>TCxxxx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">6 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>TC0024</code></td>
    </tr>
    <tr>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><b>Thẻ Spa Pass</b></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>PASS-&lt;4 số cuối SĐT&gt;-xx</code></td>
      <td style="padding: 8px; border: 1px solid #cbd5e1; text-align: center;">14 ký tự</td>
      <td style="padding: 8px; border: 1px solid #cbd5e1;"><code>PASS-1222-01</code></td>
    </tr>
  </tbody>
</table>

---

## 🏗️ CẤU TRÚC THƯ MỤC DỰ ÁN (MVC PURE JAVA)

```
N07-HeThongQuanLyCuaHangThuCungMio/
├── config/                             # File cấu hình kết nối và API
│   ├── database.properties             # Cấu hình Microsoft SQL Server (Host, Port, User, Pass)
│   ├── vietqr.properties               # Cấu hình tài khoản thụ hưởng VietQR Napas 247
│   └── vnpay.properties                # Cấu hình Merchant Terminal Cổng thanh toán VNPay Sandbox
│
├── sql/                                # Cơ sở dữ liệu quan hệ
│   ├── HeThongQuanLyThuCung.sql        # Kịch bản DDL hợp nhất tạo mới CSDL 30 Bảng & Cấu hình chuẩn
│   └── archive/                        # Lưu trữ kịch bản gốc, migrations, seed, validate và cleanup
│
├── lib/                                # Các thư viện phụ thuộc (Dependencies)
│   ├── flatlaf-3.5.4.jar               # Giao diện phẳng FlatLaf hiện đại
│   └── mssql-jdbc-12.8.1.jre11.jar     # Trình điều khiển JDBC kết nối Microsoft SQL Server
│
├── resources/                          # Tài nguyên đồ họa & âm thanh
│   ├── icons/                          # Bộ icon phẳng chuẩn hóa (Lucide / Material UI)
│   └── images/                         # Ảnh chụp sản phẩm, dịch vụ và thú cưng minh họa
│
├── src/                                # Toàn bộ mã nguồn chương trình (Pure Java MVC)
│   ├── Main.java                       # Điểm khởi chạy ứng dụng (Bootstrap & Look and Feel)
│   │
│   ├── controllers/                    # Tầng điều khiển nghiệp vụ (Business Logic Controllers)
│   │   ├── BanHangController.java      # Xử lý tính tiền, voucher, trừ kho FEFO, xuất hóa đơn
│   │   ├── DangNhapController.java     # Kiểm tra đăng nhập, mã hóa và phân quyền người dùng
│   │   ├── GiaoCaController.java       # Quản lý mở ca, chốt ca và biên bản kiểm kê két tiền
│   │   ├── NhanVienController.java     # Thêm, sửa, xóa, tìm kiếm nhân sự cửa hàng
│   │   ├── PhanCaController.java       # Điều phối lịch trực tuần theo ma trận 4 ca
│   │   ├── SpaPassController.java      # Kích hoạt, tra cứu và trừ lượt sử dụng Thẻ Spa Pass
│   │   └── ThuCungController.java      # Quản lý hồ sơ bé cưng, tiêm phòng và lịch hẹn Spa
│   │
│   ├── models/
│   │   ├── entity/                     # Các lớp thực thể đối tượng (Domain Entities)
│   │   │   ├── HoaDon.java, ChiTietHoaDon.java, ChiTietXuatLo.java, ChiTietDichVu.java...
│   │   │   ├── SanPham.java, LoHang.java, DonViTinh.java, QuyDoiDonVi.java...
│   │   │   ├── KhachHang.java, ThuCung.java, HoSoSucKhoe.java, TheSpaPass.java...
│   │   │   └── NhanVien.java, TaiKhoan.java, BangGiaoCa.java, CaLamViec.java...
│   │   │
│   │   └── dao/                        # Tầng truy vấn cơ sở dữ liệu (Data Access Objects)
│   │       ├── HoaDonDAO.java, SanPhamDAO.java, LoHangDAO.java, DichVuDAO.java...
│   │       ├── KhachHangDAO.java, ThuCungDAO.java, LichHenDAO.java, PhongLuuTruDAO.java...
│   │       └── NhanVienDAO.java, TaiKhoanDAO.java, BangGiaoCaDAO.java, PhanCaDAO.java...
│   │
│   ├── utils/                          # Các lớp tiện ích hỗ trợ toàn hệ thống
│   │   ├── DBConnection.java           # Quản lý kết nối SQL Server Connection Pool
│   │   ├── ThemeColors.java            # Bảng màu Luxury Emerald & Mint Sage, bo góc viền cong
│   │   ├── MaTuSinhHelper.java         # Thuật toán tự sinh mã nghiệp vụ (<= 20 ký tự)
│   │   ├── UserSession.java            # Lưu phiên làm việc của nhân viên đang đăng nhập
│   │   ├── IconHelper.java             # Bộ nạp icon vector và màu sắc theo chủ đề
│   │   └── VNPayHelper.java            # Thuật toán băm HMAC-SHA512 kết nối VNPay Gateway
│   │
│   └── views/                          # Giao diện người dùng Swing (UI Panels & Dialogs)
│       ├── LoginForm.java              # Màn hình đăng nhập hiện đại với xác thực nhanh
│       ├── MainDashboard.java          # Khung ứng dụng chính với thanh Sidebar điều hướng
│       ├── TongQuanPanel.java          # Màn hình tổng quan với thống kê KPI thời gian thực
│       ├── BanHangPOSPanel.java        # Màn hình bán hàng POS cảm ứng & Quầy thu ngân
│       ├── DoiTraHangPanel.java        # Màn hình đổi trả hàng, hoàn tiền và hoàn tồn kho
│       ├── KhoLoHangPanel.java         # Màn hình quản lý kho, lô hàng cận date & xuất nhập
│       ├── ThuCungDichVuPanel.java     # Màn hình hồ sơ thú cưng, sổ tiêm phòng & sức khỏe
│       ├── QuanLyLichHenPanel.java     # Màn hình theo dõi tiến độ lịch hẹn Spa & Grooming
│       ├── PetHotelPanel.java          # Màn hình quản lý sơ đồ phòng khách sạn lưu trú
│       ├── BanGiaoCaPanel.java         # Màn hình bàn giao ca và đối soát tiền mặt cuối ngày
│       ├── LichPhanCaPanel.java        # Màn hình xếp lịch trực và phân ca tuần nhân viên
│       ├── QuanLyNhanVienPanel.java    # Màn hình quản lý hồ sơ nhân viên và tài khoản
│       ├── VietQRDialog.java           # Hộp thoại hiển thị mã QR động Napas 247
│       ├── VNPayDialog.java            # Hộp thoại cổng thanh toán VNPay Sandbox
│       ├── HoaDonPreviewDialog.java    # Hộp thoại xem trước và in phiếu hóa đơn K80
│       └── charts/                     # Biểu đồ thống kê kết xuất bằng Java Graphics2D
│           ├── BarChartPanel.java      # Biểu đồ cột doanh thu 7 ngày gần nhất
│           └── DonutChartPanel.java    # Biểu đồ Donut cơ cấu doanh thu sản phẩm / dịch vụ
│
├── build.bat                           # Script tự động biên dịch toàn bộ dự án
└── run.bat                             # Script khởi chạy ứng dụng trực tiếp bằng Java
```

---

## 🚀 HƯỚNG DẪN CÀI ĐẶT & CHẠY ỨNG DỤNG

### 1. Yêu cầu môi trường tối thiểu
- **Java Development Kit (JDK):** Phiên bản **Java 17** hoặc **Java 21 LTS** (Khuyên dùng OpenJDK hoặc Oracle JDK).
- **Hệ quản trị CSDL:** **Microsoft SQL Server** (2019, 2022 hoặc bản Express) mở cổng mặc định `1433`.
- **Môi trường phát triển (IDE):** **Eclipse IDE for Java Developers**, IntelliJ IDEA hoặc Visual Studio Code.

### 2. Khởi tạo Cơ sở dữ liệu SQL Server
1. Mở công cụ **SQL Server Management Studio (SSMS)** hoặc **Azure Data Studio**.
2. Mở tệp kịch bản: [`sql/HeThongQuanLyThuCung.sql`](sql/HeThongQuanLyThuCung.sql).
3. Tự tạo và chọn đúng database đích. Script **không tự tạo, đổi hoặc xóa database**.
4. Trong cùng cửa sổ truy vấn/session, xác nhận database và chạy chế độ chỉ tạo schema:
   ```sql
   USE QuanLyCuaHangThuCung;
   EXEC sp_set_session_context N'XacNhanDatabase', N'QuanLyCuaHangThuCung';
   EXEC sp_set_session_context N'CheDo', N'SCHEMA_ONLY';
   -- Sau đó chạy toàn bộ sql/HeThongQuanLyThuCung.sql trong chính session này.
   ```
5. Tạo tài khoản quản lý đầu tiên bằng `utils.BootstrapAdminAccount`; không có mật khẩu mặc định trong source.
6. Chỉ khi cần dữ liệu mẫu, dùng database cô lập tên chính xác `QuanLyCuaHangThuCung_Test` hoặc `QuanLyCuaHangThuCung_Dev`, bootstrap NV001 trước, rồi xác nhận trong cùng session:
   ```sql
   EXEC sp_set_session_context N'XacNhanDatabase', DB_NAME();
   EXEC sp_set_session_context N'CheDo', N'DEMO_DATA';
   EXEC sp_set_session_context N'XacNhanDemo', N'PETSHOP_MIO_DEMO_FRESH_ONLY';
   -- Sau đó chạy lại toàn bộ file. Script sẽ từ chối nếu database không còn trống.
   ```
   Các tài khoản mẫu phụ được tạo ở trạng thái khóa; quản lý phải đặt mật khẩu riêng trước khi kích hoạt.
7. Mở tệp [`config/database.properties`](config/database.properties) để kiểm tra lại tài khoản kết nối:
   ```properties
   db.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyCuaHangThuCung;encrypt=true;trustServerCertificate=true;
   db.user=sa
   db.password=YOUR_DATABASE_PASSWORD_HERE
   ```
   *(Lưu ý: Bạn có thể tạo file `config/database.local.properties` từ file `.example` để lưu mật khẩu cá nhân không bị Git theo dõi).*

### 3. Biên dịch và khởi chạy chương trình

#### Cách 1: Chạy nhanh qua tệp thực thi BAT (Khuyên dùng trên Windows)
```cmd
# Bước 1: Biên dịch toàn bộ mã nguồn vào thư mục bin/
build.bat

# Bước 2: Khởi chạy ứng dụng
run.bat
```

#### Cách 2: Khởi chạy từ Eclipse IDE
1. Mở Eclipse $\rightarrow$ **File** $\rightarrow$ **Import...** $\rightarrow$ **Existing Projects into Workspace**.
2. Chọn thư mục `N07-HeThongQuanLyCuaHangThuCungMio`.
3. Nhấp chuột phải vào dự án $\rightarrow$ **Build Path** $\rightarrow$ **Configure Build Path...** $\rightarrow$ Thẻ **Libraries** $\rightarrow$ Đảm bảo 2 tệp trong thư mục `lib/` (`flatlaf-3.5.4.jar` và `mssql-jdbc-12.8.1.jre11.jar`) đã nằm trong **Classpath**.
4. Mở tệp [`src/Main.java`](src/Main.java) $\rightarrow$ Nhấp chuột phải chọn **Run As** $\rightarrow$ **Java Application**.

---

## 🔑 TÀI KHOẢN VÀ PHÂN QUYỀN (RBAC)

Hệ thống hỗ trợ bốn vai trò được lưu trong CSDL: `NVQL`, `NVTN`, `NVBH` và `NVCS`. Repository không công khai tài khoản hoặc mật khẩu dùng chung.

- Tài khoản quản lý đầu tiên phải được tạo bằng `utils.BootstrapAdminAccount` trên đúng database đích.
- Các tài khoản demo phụ được tạo ở trạng thái khóa. Quản lý đặt mật khẩu riêng, đủ mạnh rồi mới kích hoạt.
- Mật khẩu mới được băm một chiều bằng PBKDF2 với salt ngẫu nhiên. SHA-256 cũ chỉ được đọc để nâng cấp khi đăng nhập thành công, không dùng để tạo/reset mật khẩu mới.
- Nhân viên quên mật khẩu sử dụng luồng OTP qua email đã cấu hình; không có mật khẩu dự phòng trong source.

---

## 📑 TÀI LIỆU HƯỚNG DẪN KIỂM THỬ NGHIỆP VỤ (TESTING GUIDE)

Để thực hiện kiểm thử chi tiết từng chức năng, quy trình kiểm tra logic ngầm và các kịch bản kiểm thử mẫu (Test Cases) khớp 100% với thao tác trên màn hình, vui lòng xem tài liệu:
👉 **[`TAI_LIEU_KIEM_THU_NGHIEP_VU.md`](TAI_LIEU_KIEM_THU_NGHIEP_VU.md)**

---
*© 2026 - Nhóm 07 - Đề tài môn học Phát Triển Ứng Dụng (AD) - Khoa Công nghệ Thông tin, Trường Đại học Công nghiệp TP.HCM (IUH).*
