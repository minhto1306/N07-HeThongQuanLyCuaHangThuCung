# Kế Hoạch Cải Tổ Toàn Diện (V2) — Khắc Phục 18 Vấn Đề Vấn Đáp & Tối Ưu Dữ Liệu Lớn Cho Gemini Flash 3.8

## 1. Mục tiêu Cải tổ & Tổng hợp Bối cảnh
Bản kế hoạch này tích hợp toàn bộ đề xuất kiến trúc của bạn cùng **18 vấn đề giảng viên chỉ ra trong buổi vấn đáp** và **các kịch bản chịu tải dữ liệu lớn thực tế**:

1. **Quy chuẩn Khóa chính & Mã tự sinh tự động tăng (`NV001` $\rightarrow$ `NVn`):**
   - **Tuyệt đối không dùng Số điện thoại làm khóa chính** vì nhân viên/khách hàng có thể thay đổi SĐT.
   - Bảng `TaiKhoan` dùng khóa chính **`maTaiKhoan` (`NV001`, `NV002`, ..., `NVn`)** tự động tăng, liên kết 1–1 với `maNhanVien` (`NV001`...`NVn`).
   - Bảng `TaiKhoan` lưu `soDienThoai` (`UNIQUE NOT NULL`), `email` (`UNIQUE NOT NULL`), `matKhau` (**băm SHA-256**), `vaiTro`, `trangThaiHoatDong`, **bỏ cột `tenDangNhap`**. Nhân viên đăng nhập bằng **Số điện thoại + Mật khẩu**.
   - **Toàn bộ mã số trong hệ thống (`NVxxx`, `SPxxx`, `DVxxx`, `QDxxx`, `KHxxxx`, `TCxxxx`, `HSxxxx`, `SKxxxx`, `GCxxx`, `LO-yyMMdd-xxx`, `HD...`, `LH...`, `DT...`)** đều **tự động sinh & tự động tăng** qua `MaTuSinhHelper`. Mọi ô hiển thị mã trên giao diện đều khóa `setEditable(false)` (Read-Only), không cho phép người dùng tự gõ hay chỉnh sửa.
2. **Kiến trúc Đa tầng Desktop (N-Tier: `entity`, `dao`, `bus`, `gui`, `utils`) & Tách biệt Nghiệp vụ:**
   - Giải quyết triệt để **Vấn đề 2 & 3**: Không đặt bất kỳ câu lệnh SQL, lời gọi `DAO` hay phép tính tiền nào trong tầng `gui`.
3. **Chuẩn hóa `Entity`, Liên kết Sản phẩm – Lô hàng & Cơ chế Định giá bán trước (Giải quyết Vấn đề 4, 5, 6, 7, 8):**
   - Bổ sung `giaBanCoSo`, `donViCoSo`, `maVach`, `dsLoHang`, `dsQuyDoi` vào `entity/SanPham.java` và bảng `SanPham`.
   - Bổ sung thuộc tính `SanPham sanPham` và `DichVu dichVu` trực tiếp vào `entity/ChiTietHoaDon.java`.
   - Khẳng định rõ quy tắc nghiệp vụ: **Giá nhập (`donGiaNhapCoSo`)** quản lý theo từng **`LoHang`**; **Giá bán niêm yết (`giaBanCoSo`)** quản lý tại **`SanPham`** (và `QuyDoiDonVi.giaBan` theo tỷ lệ quy đổi) do Quản lý thiết lập trước. Khi lập hóa đơn, `ChiTietHoaDon` tự động nạp giá bán niêm yết từ `SanPham`/`QuyDoiDonVi` vào `donGiaLucBan` (nhân viên thu ngân không thể tự đặt hay sửa giá).
4. **Đồng bộ 100% Số liệu Thực tế & Liên kết Drill-Down trên Dashboard (Giải quyết Vấn đề 9, 10, 11, 12):**
   - Đổi tên thẻ KPI rõ nghĩa: **`"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`**.
   - Bấm vào bất kỳ thẻ KPI nào trên Dashboard (`Doanh thu`, `Hóa đơn bán lẻ/dịch vụ`, `Thú cưng làm dịch vụ`, `Lô hàng sắp hết hạn`) đều **chuyển thẳng sang màn hình chi tiết và tự động lọc đúng danh sách tương ứng**, khớp số lượng 100%.
5. **Tối ưu Thao tác POS, Tra cứu Khách theo SĐT, Điểm Tích Lũy Linh Hoạt & Tìm Đơn Tạm (Giải quyết Vấn đề 13, 14, 15, 16):**
   - Tách khu vực bên trái POS thành **2 Tab** (`Tab 1: Hàng hóa bán lẻ` dùng **Ô quét mã vạch Barcode + Bảng `JTable`**; `Tab 2: Dịch vụ Spa & Chăm sóc`), giữ nguyên **Giỏ hàng chung** và **Đơn hàng tạm (Đơn chờ)**.
   - Tra cứu khách hàng bằng **Số điện thoại** trực tiếp từ CSDL.
   - Cho phép khách **nhập số điểm tích lũy muốn sử dụng** (`JSpinner` / `JTextField` từ `0` đến số điểm tối đa khả dụng) thay vì ép dùng hết 1 lần.
   - Bổ sung **Ô tìm kiếm nhanh Đơn hàng tạm theo Số điện thoại khách hàng** trên màn hình Đơn chờ.
6. **Xử lý Lưu lượng Dữ liệu Lớn & Điều phối Tiến độ Dịch vụ (Giải quyết Vấn đề 17, 18 & Bài toán tải lớn):**
   - Giải quyết 6 khó khăn thực tế khi có hàng trăm ca dịch vụ chạy song song (xem Mục 2 bên dưới).

---

## 2. Phân Tích 6 Tình Huống Khó Khăn Khi Quản Lý Lượng Dữ Liệu Lớn & Giải Pháp Trong Kế Hoạch

| STT | Tình huống thực tế khi dữ liệu lớn (Pain Point) | Hậu quả nếu giữ thiết kế cũ | Giải pháp thiết kế trong bản cải tổ |
| :--- | :--- | :--- | :--- |
| **1** | **Ùn tắc khi khách quay lại đón thú cưng & thanh toán:** Có 50–100 đơn tạm/lịch hẹn đang chờ, khách đọc SĐT nhưng thu ngân phải dò mắt qua hàng chục thẻ. | Mất 3–5 phút/khách để tìm đơn, gây ùn tắc quầy thu ngân. | Bổ sung **Ô tìm nhanh theo SĐT (`txtTimNhanhSDT`)** tại cả màn hình `Đơn chờ POS` và `Quản lý Lịch hẹn`. Gõ SĐT + `Enter` là lọc đúng đơn của khách ngay lập tức. |
| **2** | **Quá tải hoặc trùng lịch Kỹ thuật viên (KTV):** Có hàng chục ca Spa cùng lúc, người điều phối không biết KTV nào đang rảnh hay đang kẹt nhiều bé. | Giao dồn việc cho 1 KTV trong khi KTV khác rảnh; khách chờ lâu. | Trên `QuanLyLichHenPanel` & `DatLichHenDialog`: Hiển thị **số ca đang phục vụ của từng KTV**, có ComboBox lọc lịch hẹn theo từng KTV và chặn phân công trùng khung giờ. |
| **3** | **Trôi đơn / Quên cập nhật các ca dịch vụ bị lố giờ:** Hàng trăm dòng lịch hẹn cũ (đã xong, đã hủy) trộn lẫn với ca đang làm khiến ca bị trễ giờ bị khuất. | Thú cưng làm xong không ai báo khách, hoặc ca tắm kéo dài quá thời lượng mà quản lý không phát hiện. | 1. Mặc định ưu tiên đẩy các ca `Đang phục vụ` & `Đã đặt` trong ngày lên đầu.<br>2. Tự động gắn nhãn cảnh báo **`⚠️ Quá giờ`** nếu thời gian phục vụ vượt `thoiLuongPhut`.<br>3. Bổ sung **Phân trang (20 dòng/trang)** + lọc theo khung giờ. |
| **4** | **Đơ giao diện (Swing EDT Freeze) khi danh mục > 1.000 mặt hàng:** Dùng `GridLayout` vẽ hàng trăm `JPanel` có ảnh cho từng sản phẩm. | Tràn bộ nhớ, cuộn giật lag, thu ngân không thể bán hàng nhanh. | Thay lưới thẻ ảnh sản phẩm trên POS bằng **`JTable` ảo hóa + Thanh quét mã Barcode (`F2`)** và bộ lọc tức thời, chịu tải mượt hàng chục nghìn dòng. |
| **5** | **Đứt gãy thông tin giữa bộ phận Spa và Thu ngân POS:** KTV bấm "Hoàn thành" bên màn hình Lịch hẹn nhưng thu ngân POS phải hỏi lại khách làm dịch vụ gì. | Dễ tính thiếu dịch vụ hoặc mất thời gian nhập lại từ đầu. | Nút **"Chuyển sang POS thanh toán"** trên `QuanLyLichHenPanel` tự động đẩy toàn bộ dịch vụ của lịch hẹn đó vào **Giỏ hàng / Đơn chờ POS** gắn sẵn SĐT khách hàng. |
| **6** | **Sai lệch khóa chính & giá bán khi nhân viên thao tác vội:** Nhân viên tự gõ mã trùng hoặc sửa nhầm đơn giá bán trên bảng. | Lỗi khóa ngoại SQL, thất thoát doanh thu của chủ cửa hàng. | Khóa `setEditable(false)` 100% các ô Mã số (tự động tăng qua `MaTuSinhHelper`) và khóa cột Đơn giá trên POS (nạp cứng từ `SanPham.giaBanCoSo`). |

---

## 3. Bảng Đối Chiếu 18 Vấn Đề Cô Giáo Chỉ Ra $\leftrightarrow$ Giải Pháp Kỹ Thuật Cụ Thể

| Nhóm | Vấn đề cô giáo nhận xét | File & Hạng mục sửa trong Kế hoạch |
| :--- | :--- | :--- |
| **I. Đăng nhập & Kiến trúc** | **1.** Chưa có chức năng quên mật khẩu | `LoginForm.java`, `DangNhapBus.java`, `EmailThongBaoHelper.java` (Phase 2): Xác thực SĐT + Email $\rightarrow$ Gửi OTP qua `EmailPreviewDialog` $\rightarrow$ Đặt mật khẩu mới (băm SHA-256). |
| | **2.** Chưa giải thích thuyết phục kiến trúc ứng dụng Desktop | Chuyển toàn bộ sang **N-Tier (`gui` $\rightarrow$ `bus` $\rightarrow$ `dao` $\rightarrow$ `entity`)** (Phase 1 & 3). |
| | **3.** Xử lý tính toán đặt ở giao diện hoặc không chỉ ra được chỗ tính | Đưa toàn bộ công thức tính tiền về `ChiTietHoaDon.tinhThanhTien()`, `HoaDon.tinhTongThanhToan()` (tầng `entity`) và `BanHangBus.tinhToanDonHang()` (tầng `bus`). |
| **II. Entity, Sản phẩm & Giá bán** | **4.** `ChiTietHoaDon` không thể hiện rõ sản phẩm | Thêm thuộc tính `protected SanPham sanPham;` và `protected DichVu dichVu;` trực tiếp vào `entity/ChiTietHoaDon.java` (Phase 2). |
| | **5.** `Entity SanPham` thiếu dữ liệu quan trọng | Thêm `giaBanCoSo`, `donViCoSo`, `maVach`, `List<LoHang> dsLoHang`, `List<QuyDoiDonVi> dsQuyDoi` vào `entity/SanPham.java` và cột `giaBanCoSo`, `donViCoSo`, `maVach` vào bảng `SanPham` (Phase 2). |
| | **6.** Quan hệ Sản phẩm – Lô hàng chưa chặt chẽ | `SanPham` (1) $\leftrightarrow$ (N) `LoHang`: `SanPham` quản lý thông tin định danh & giá bán chuẩn; mỗi `LoHang` quản lý số lượng nhập/tồn, `ngaySanXuat`, `hanSuDung` và `donGiaNhapCoSo` của từng đợt nhập. |
| | **7.** Chưa xác định rõ nơi quản lý giá bán | Giá nhập (`donGiaNhapCoSo`) nằm ở `LoHang`; Giá bán niêm yết (`giaBanCoSo`) nằm ở `SanPham` (và `QuyDoiDonVi.giaBan`). |
| | **8.** Giá trong `ChiTietHoaDon` thiếu cơ chế định giá trước | Khi thêm SP vào hóa đơn, `BanHangBus` tự động lấy `sanPham.getGiaBanCoSo()` (hoặc `quyDoi.getGiaBan()`) gán vào `ChiTietHoaDon.donGiaLucBan`. Khóa không cho thu ngân sửa cột Đơn giá trên POS. |
| **III. Dashboard & Báo cáo** | **9.** Tên chỉ số cảnh báo không rõ nghĩa ("Cảnh báo theo lô") | Đổi thành **`"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`** trên `TongQuanPanel.java` (Phase 4). |
| | **10.** Chỉ số Dashboard không liên kết đến danh sách chi tiết | Gắn sự kiện Click (Drill-Down) vào thẻ `"LÔ HÀNG SẮP HẾT HẠN"` $\rightarrow$ Mở thẳng `KhoLoHangPanel` (Tab FEFO) và lọc đúng danh sách các lô cảnh báo đó (Phase 4). |
| | **11.** Số liệu Dashboard và màn hình chi tiết chưa nhất quán | Dùng chung duy nhất hàm `KhoLoHangBus.layDanhSachLoCanhBaoHanDung()` (`soLuongTon > 0` và `hanSuDung <= today + 60 ngày`) cho cả Dashboard và Kho (Phase 3 & 4). |
| | **12.** Thống kê hóa đơn không giúp truy cập đúng dữ liệu | Gắn sự kiện Click vào thẻ `"HÓA ĐƠN ĐÃ LẬP"` trên Dashboard $\rightarrow$ Mở `BanHangPOSPanel` (Tab Lịch sử HĐ) và lọc đúng các đơn bán lẻ / dịch vụ trong ngày (Phase 4). |
| **IV. POS, Khách hàng & Đơn tạm** | **13.** Cách chọn hàng ở POS chưa phù hợp thao tác tại quầy | Thay lưới ảnh hàng hóa bằng **Ô quét mã vạch Barcode (`F2`) + Bảng `JTable` Hàng hóa** trong `Tab 1: Hàng hóa bán lẻ`, tách riêng `Tab 2: Dịch vụ Spa` (Phase 5). |
| | **14.** Tra cứu khách hàng chưa tối ưu (phải ưu tiên SĐT) | Ô `txtSdtKhachHang` trên POS tra cứu trực tiếp theo **Số điện thoại** từ `BanHangBus.timKhachHangTheoSDT(sdt)` (Phase 5). |
| | **15.** Sử dụng điểm tích lũy thiếu linh hoạt (bắt dùng hết 1 lần) | Thêm `JSpinner spnDiemSuDung` cho phép khách chọn số điểm muốn dùng (`0` $\rightarrow$ `diemToiDaKhaDung`), mỗi điểm = `1.000 đ` (Phase 5). |
| | **16.** Khôi phục đơn hàng tạm không có cách tìm nhanh | Thêm **Ô tìm kiếm nhanh Đơn tạm theo SĐT / Mã đơn / Tên khách (`txtTimDonCho`)** ngay đầu Tab Đơn chờ POS (Phase 5). |
| **V. Kiểm thử & Quy trình** | **17 & 18.** Chưa kiểm thử đủ tình huống thực tế & cần hiểu rõ kiến trúc | Bổ sung kiểm thử tự động và tài liệu giải thích luồng gọi hàm `gui` $\rightarrow$ `bus` $\rightarrow$ `dao` $\rightarrow$ `entity` ngay trong code comment chuẩn (Phase 6). |

---

## 4. Kế Hoạch Thực Thi Nguyên Tử (6 Phases Chi Tiết Cho Gemini Flash 3.8)

### Phase 1: Chuyển Đổi Kiến Trúc N-Tier (`entity`, `dao`, `bus`, `gui`, `utils`)
- Di chuyển và cập nhật `package` + `import` toàn bộ dự án:
  - `src/models/entity/*.java` $\rightarrow$ `src/entity/*.java` (`package entity;`)
  - `src/models/dao/*.java` $\rightarrow$ `src/dao/*.java` (`package dao;`)
  - `src/controllers/*Controller.java` $\rightarrow$ `src/bus/*Bus.java` (`package bus;`):
    - `BanHangController` $\rightarrow$ `BanHangBus`
    - `DangNhapController` $\rightarrow$ `DangNhapBus`
    - `GiaoCaController` $\rightarrow$ `GiaoCaBus`
    - `NhanVienController` $\rightarrow$ `NhanVienBus`
    - `PhanCaController` $\rightarrow$ `PhanCaBus`
    - `SpaPassController` $\rightarrow$ `SpaPassBus`
    - `ThuCungController` $\rightarrow$ `ThuCungBus`
  - `src/views/*.java` $\rightarrow$ `src/gui/*.java` (`package gui;`) và `src/views/charts/*.java` $\rightarrow$ `src/gui/charts/*.java` (`package gui.charts;`).
  - Xóa các thư mục cũ `src/models`, `src/controllers`, `src/views`.
  - Chạy `cmd /c build.bat` để xác nhận biên dịch thành công.

---

### Phase 2: Cải Tổ CSDL, Khóa Chính Tự Tăng `NVxxx`, Băm SHA-256, Định Giá `SanPham` & `ChiTietHoaDon`

#### [MODIFY] [HeThongQuanLyCuaHangThuCung.sql](file:///c:/Users/lekha/eclipse-workspace/N07-HeThongQuanLyCuaHangThuCungMio/sql/HeThongQuanLyCuaHangThuCung.sql)
1. **Bảng `TaiKhoan` & `NhanVien`** (Khóa chính `maTaiKhoan` = `NV001`..`NVn`, bỏ `tenDangNhap`, đăng nhập bằng `soDienThoai`, mật khẩu băm SHA-256):
```sql
CREATE TABLE TaiKhoan (
    maTaiKhoan NVARCHAR(20) PRIMARY KEY,       -- Tự động tăng: NV001, NV002, ...
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,  -- Dùng để nhân viên đăng nhập
    email NVARCHAR(100) NOT NULL UNIQUE,       -- Dùng để xác thực quên mật khẩu
    matKhau NVARCHAR(64) NOT NULL,             -- Chuỗi băm SHA-256 (64 ký tự hex)
    vaiTro NVARCHAR(20) NOT NULL,              -- NVQL, NVTN, NVKHO, NVCS, NVBH
    trangThaiHoatDong BIT NOT NULL DEFAULT 1
);

CREATE TABLE NhanVien (
    maNhanVien NVARCHAR(20) PRIMARY KEY,       -- Tự động tăng: NV001, NV002, ...
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50) NOT NULL,
    soDienThoai NVARCHAR(15) NOT NULL UNIQUE,
    email NVARCHAR(100) NOT NULL,
    maTaiKhoan NVARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTaiKhoan) REFERENCES TaiKhoan(maTaiKhoan) ON DELETE CASCADE
);
```
2. **Bảng `SanPham` & `ChiTietHoaDon`** (Khắc phục Vấn đề 4, 5, 6, 7, 8 của cô giáo):
```sql
CREATE TABLE SanPham (
    maSanPham NVARCHAR(20) PRIMARY KEY,        -- Tự động tăng: SP001, SP002, ...
    maDanhMuc NVARCHAR(20) NOT NULL,
    tenSanPham NVARCHAR(100) NOT NULL,
    maVach NVARCHAR(30) NULL UNIQUE,           -- Mã Barcode quét tại quầy (VD: 893600000001)
    donViCoSo NVARCHAR(30) NOT NULL DEFAULT N'Đơn vị', -- Đơn vị tính gốc (Gói, Lon, Chai...)
    giaBanCoSo DECIMAL(18,2) NOT NULL DEFAULT 0,       -- Giá bán niêm yết chuẩn do Quản lý quy định
    moTa NVARCHAR(255),
    CONSTRAINT FK_SanPham_DanhMuc FOREIGN KEY (maDanhMuc) REFERENCES DanhMucHang(maDanhMuc)
);
```
- Trong bảng `ChiTietHoaDon`: Thêm cột `maSanPham NVARCHAR(20) NULL` và `maDichVu NVARCHAR(20) NULL` (kèm khóa ngoại tới `SanPham` và `DichVu`) để ngay tại bảng `ChiTietHoaDon` cũng truy vết trực tiếp được sản phẩm/dịch vụ!

#### [NEW] `src/utils/PasswordHasher.java`
- Hàm `public static String hashSHA256(String rawPassword)` băm mật khẩu sang chuỗi Hex 64 ký tự bằng `MessageDigest.getInstance("SHA-256")`.

#### [MODIFY] `src/utils/MaTuSinhHelper.java`
- Bổ sung đầy đủ các hàm sinh mã tự động tăng (đọc mã lớn nhất trong CSDL rồi `+ 1`):
  - `sinhMaNhanVien()`: Sinh mã `NV001`, `NV002`, ..., `NVxxx` (dùng chung cho `maNhanVien` và `maTaiKhoan`).
  - `sinhMaSanPham()`: Sinh mã `SP001`, `SP002`, ..., `SPxxx`.
  - `sinhMaDichVu()`: Sinh mã `DV001`, `DV002`, ..., `DVxxx`.
  - `sinhMaQuyDoi()`: Sinh mã `QD001`, `QD002`, ..., `QDxxx`.
  - `sinhSoLo(String maSP)`: Sinh mã `LO-yyMMdd-xxx`.
  - `sinhMaGiaoCa()`: Sinh mã `GC001`, `GC002`, ..., `GCxxx`.
  - Cùng các hàm sẵn có: `sinhMaHoaDon(String maNV)`, `sinhMaPhieuDoiTra()`, `sinhMaLichHen()`, `sinhMaKhachHang()` (`KH0001`..), `sinhMaThuCung()` (`TC0001`..).
- **Ràng buộc giao diện:** Tất cả các trường mã trên mọi Dialog/Panel (`QuanLyNhanVienPanel`, `NhapSanPhamVaLoHangDialog`, `ThietLapQuyDoiDialog`, `DatLichHenDialog`, `ThuCungDichVuPanel`, `BanGiaoCaPanel`) đều gọi `MaTuSinhHelper` và đặt `setEditable(false)`.

#### [MODIFY] `src/entity/TaiKhoan.java`, `src/entity/NhanVien.java`, `src/entity/SanPham.java`, `src/entity/ChiTietHoaDon.java`, `src/entity/ChiTietXuatLo.java`
- `TaiKhoan.java`: Thuộc tính `maTaiKhoan` (`NVxxx`), `soDienThoai`, `email`, `matKhau` (SHA-256), `vaiTro`, `trangThaiHoatDong`.
- `SanPham.java`: Bổ sung `maVach`, `donViCoSo`, `giaBanCoSo` (`BigDecimal`), `List<LoHang> dsLoHang`, `List<QuyDoiDonVi> dsQuyDoi`.
- `ChiTietHoaDon.java`: Bổ sung `protected SanPham sanPham;` và `protected DichVu dichVu;`, phương thức `apDungGiaNiemYet()` tự động lấy giá bán từ `SanPham.getGiaBanCoSo()` (hoặc `QuyDoiDonVi.getGiaBan()`) và `DichVu.getGiaDichVu()`.

#### [MODIFY] `src/dao/TaiKhoanDAO.java`, `src/dao/NhanVienDAO.java`, `src/bus/DangNhapBus.java`, `src/gui/LoginForm.java`
- Đăng nhập bằng **Số điện thoại (`soDienThoai`)** + **Mật khẩu** (băm SHA-256).
- Bổ sung chức năng **Quên mật khẩu** trên `LoginForm`: Nhập `Số điện thoại` + `Email` $\rightarrow$ Kiểm tra khớp trong `TaiKhoanDAO` $\rightarrow$ Gửi mã OTP 6 số qua `EmailPreviewDialog` $\rightarrow$ Nhập OTP và đặt mật khẩu mới (băm SHA-256 lưu xuống CSDL).

---

### Phase 3: Module Hóa Triệt Để — Tách 100% DAO & Tính Toán Về Tầng `bus`
- Tạo mới `src/bus/KhoLoHangBus.java`, `src/bus/DoiTraBus.java`, `src/bus/PetHotelBus.java`, `src/bus/ThongKeBus.java` và nâng cấp `src/bus/BanHangBus.java`.
- Chuyển toàn bộ các đoạn gọi `new dao.*` hoặc `DBConnection.getConnection()` từ `BanHangPOSPanel`, `KhoLoHangPanel`, `TongQuanPanel`, `DoiTraHangPanel`, `PetHotelPanel`, `NhapSanPhamVaLoHangDialog`, `ThietLapQuyDoiDialog` sang tầng `bus`.
- Đảm bảo trong `BanHangBus` có các hàm tính toán rõ ràng để chứng minh trước hội đồng:
  - `tinhThanhTienTungDong(BigDecimal donGiaNiemYet, int soLuong)`
  - `tinhTienGiamTuDiem(int diemMuonDung, int diemHienCo, long tongTienHang)`
  - `tinhTongThanhToan(long tongTienHang, long tienGiamVoucher, long tienGiamDiem, long tienKhauTruDoiTra)`

---

### Phase 4: Đồng Bộ Số Liệu Thực Tế 100% & Liên Kết Drill-Down Trên Dashboard (`TongQuanPanel`)
- **Xóa toàn bộ dữ liệu ảo/hardcode** trong `BarChartPanel.java` và `TongQuanPanel.java`.
- **Sửa nhãn thẻ KPI thứ 4 (Vấn đề 9):** Đổi thành **`"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`**.
- **Đồng bộ 100% danh sách lô cảnh báo (Vấn đề 11):** Cả `TongQuanPanel` và `KhoLoHangPanel` đều gọi chung `KhoLoHangBus.layDanhSachLoCanhBaoHanDung()` (lọc các lô có `soLuongTon > 0` và `soNgayConLai <= 60`).
- **Liên kết Click-to-Drill-Down từ 4 thẻ KPI trên Dashboard (Vấn đề 10 & 12):**
  - Click thẻ **"DOANH THU HÔM NAY"** $\rightarrow$ Mở `BaoCaoThongKeDialog` (Tab 1: Doanh thu hôm nay).
  - Click thẻ **"HÓA ĐƠN ĐÃ LẬP"** $\rightarrow$ Gọi `MainDashboard.dieuHuongSangLichSuHoaDonHomNay()` chuyển thẳng sang `BanHangPOSPanel` (Tab Lịch sử HĐ) lọc đúng các đơn bán lẻ & dịch vụ trong ngày.
  - Click thẻ **"THÚ CƯNG LÀM DỊCH VỤ"** $\rightarrow$ Chuyển thẳng sang `QuanLyLichHenPanel` lọc các ca dịch vụ hôm nay.
  - Click thẻ **"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"** $\rightarrow$ Chuyển thẳng sang `KhoLoHangPanel` (Tab Quản lý Lô hàng FEFO) và **tự động bật chế độ lọc chỉ hiển thị đúng các lô sắp hết hạn/đã hết hạn** khớp với con số trên Dashboard.

---

### Phase 5: Thiết Kế Lại POS (`BanHangPOSPanel`) — 2 Tab Hàng Hóa (Barcode + JTable) & Dịch Vụ, Chọn Điểm Linh Hoạt, Tìm Đơn Tạm Theo SĐT
1. **Nửa trái POS tách thành `JTabbedPane` gồm 2 Tab rõ ràng (Vấn đề 13):**
   - **Tab 1 (`📦 Hàng Hóa Bán Lẻ`):**
     - Trên cùng: Ô **`txtQuetMaVach`** (nhập/quét mã vạch `893...` hoặc mã `SPxxx` + nhấn `Enter` là tự động thêm ngay vào giỏ hàng với giá bán niêm yết từ `SanPham`/`QuyDoiDonVi`) + Ô tìm kiếm nhanh + ComboBox lọc Danh mục.
     - Ở giữa: **Bảng `JTable` Danh sách Hàng hóa** (`Mã SP`, `Mã vạch`, `Tên sản phẩm`, `ĐVT`, `Giá niêm yết`, `Tồn khả dụng`). Nhấp đúp chuột hoặc nhấn `Enter` để thêm vào giỏ.
   - **Tab 2 (`✂️ Dịch Vụ Spa & Chăm Sóc`):**
     - Hiển thị bảng/danh sách Dịch vụ Spa, Grooming, Pet Hotel + nút **"Nạp từ Lịch hẹn đã xong"** để đưa dịch vụ vào chung giỏ hàng hoặc lưu vào Đơn tạm.
2. **Tra cứu Khách hàng ưu tiên bằng Số điện thoại (Vấn đề 14):**
   - Ô `txtSdtKhachHang` tra cứu trực tiếp theo SĐT qua `BanHangBus.timKhachHangTheoSDT(sdt)` (hỗ trợ gõ SĐT nhấn `Enter` hoặc tự động tra cứu khi đủ 10 chữ số).
3. **Sử dụng Điểm tích lũy linh hoạt (Vấn đề 15):**
   - Thay vì ép dùng toàn bộ điểm, thêm `JSpinner spnSoDiemDung` ngay cạnh `chkDungDiem` cho phép khách chọn số điểm muốn dùng (từ `0` đến `diemHienCo`, tối đa không vượt quá tổng tiền đơn hàng), tự động quy đổi `1 điểm = 1.000 đ` qua `BanHangBus`.
4. **Khôi phục Đơn hàng tạm có Tìm kiếm nhanh theo SĐT (Vấn đề 16):**
   - Trên màn hình **Đơn chờ (`VIEW_DON_CHO`)**: Thêm thanh công cụ đầu trang gồm ô **`txtTimDonChoTheoSDT`** (`"Nhập SĐT khách hàng, tên khách hoặc mã đơn tạm để tìm nhanh..."`). Khi thu ngân gõ SĐT của 1 trong nhiều khách đang gửi đơn tạm, màn hình lập tức lọc chỉ hiển thị đúng đơn tạm của khách đó (nhấn `Enter` là nạp thẳng lại vào giỏ hàng POS).
5. **Khóa cột Đơn giá trên Giỏ hàng POS (Vấn đề 8):**
   - Trong `modelGioHang`, chỉ cho phép sửa cột `Số lượng` (cột 4), tuyệt đối khóa cột `Mã`, `Tên`, `ĐVT`, `Đơn giá`, `Thành tiền`.

---

### Phase 6: Tối Ưu Quản Lý Tiến Độ Dịch Vụ Lưu Lượng Lớn (`QuanLyLichHenPanel`)
- Giải quyết bài toán quản lý hàng trăm ca dịch vụ cùng lúc:
  1. **Thanh bộ lọc đa chiều:**
     - Ô tìm kiếm nhanh theo **SĐT khách / Mã LH / Tên thú cưng**.
     - ComboBox lọc theo **Kỹ thuật viên phụ trách** (`Tất cả KTV` hoặc từng KTV kèm số lượng ca đang làm).
     - Các nút Chip lọc trạng thái (`TẤT CẢ`, `Đã đặt`, `Đang phục vụ`, `Hoàn thành`, `Hủy`) + Checkbox `Chỉ hiện lịch hôm nay`.
  2. **Cảnh báo quá giờ & Sắp xếp thông minh:**
     - Tự động ưu tiên sắp xếp các ca `Đang phục vụ` và `Đã đặt` lên trước.
     - Hiển thị cột **Tiến độ & Thời lượng** (`[Bước 2/3] Đang phục vụ (45 phút)` hoặc cảnh báo đỏ `⚠️ Quá giờ` nếu đã vượt thời lượng dự kiến).
  3. **Thanh Phân trang (Pagination Bar):**
     - Hiển thị `20 dòng / trang` với nút chuyển trang `[« Đầu] [‹ Trước] Trang X/Y (Tổng N lịch hẹn) [Sau ›] [Cuối »]` giúp giao diện luôn mượt mà dù có hàng nghìn bản ghi.
  4. **Liên kết 1 chạm sang Đơn tạm POS:**
     - Khi bấm `"Chuyển sang POS thanh toán"`, hệ thống tự động nạp khách hàng (theo SĐT) và toàn bộ dịch vụ trong phiếu hẹn sang màn hình POS (nếu POS đang bận phục vụ khách khác thì tự động đưa vào **Đơn chờ POS** để không làm gián đoạn!).

---

## 5. Prompt Chuẩn Hóa Dành Riêng Cho Model Gemini Flash 3.8

> [!IMPORTANT]
> Bạn có thể chuyển model sang **Gemini Flash 3.8** và gõ **"Duyệt"** (hoặc copy nguyên khối Prompt dưới đây gửi cho Flash 3.8) để bắt đầu thực thi tuần tự từng Phase.

```markdown
### 📋 PROMPT CHUẨN AGY (DÀNH CHO WORKER GEMINI FLASH 3.8)
- **[ROLE & CONTEXT]**: Bạn là Worker thực thi code Java Swing & SQL Server cho dự án `N07-HeThongQuanLyCuaHangThuCungMio` theo đúng bản kế hoạch `implementation_plan.md` (V2) đã được Architect (Gemini 4 Argon) thiết kế để giải quyết toàn bộ 18 vấn đề vấn đáp và tối ưu kiến trúc N-Tier.
- **[ATOMIC EXECUTION PHASES]**: Thực hiện tuần tự từng Phase, kết thúc mỗi Phase BẮT BUỘC chạy `cmd /c build.bat` để kiểm tra biên dịch 0 lỗi trước khi sang Phase tiếp theo:
  1. **Phase 1 (N-Tier Package Refactoring):**
     - Di chuyển toàn bộ code sang 5 package chuẩn: `entity` (từ `models.entity`), `dao` (từ `models.dao`), `bus` (từ `controllers`, đổi hậu tố `*Controller` thành `*Bus`), `gui` và `gui.charts` (từ `views` và `views.charts`), `utils`. Xóa thư mục cũ `models`, `controllers`, `views`.
  2. **Phase 2 (Database, Auto-Increment IDs `NV001..NVn`, SHA-256, Entity Pricing & Forgot Password):**
     - Cập nhật `sql/HeThongQuanLyCuaHangThuCung.sql`:
       + Bảng `TaiKhoan`: Khóa chính `maTaiKhoan NVARCHAR(20) PRIMARY KEY` (`NV001`..`NVn`), `soDienThoai NVARCHAR(15) NOT NULL UNIQUE`, `email NVARCHAR(100) NOT NULL UNIQUE`, `matKhau NVARCHAR(64) NOT NULL` (lưu mã băm SHA-256 của `'[REDACTED_SAMPLE_PASSWORD]'`), `vaiTro`, `trangThaiHoatDong` (bỏ cột `tenDangNhap`).
       + Bảng `NhanVien`: `maNhanVien` (`NV001`..`NVn`), `hoTen`, `chucVu`, `soDienThoai`, `email`, `maTaiKhoan` FK tới `TaiKhoan(maTaiKhoan)`.
       + Bảng `SanPham`: Bổ sung `maVach NVARCHAR(30) UNIQUE`, `donViCoSo NVARCHAR(30)`, `giaBanCoSo DECIMAL(18,2) NOT NULL`.
       + Bảng `ChiTietHoaDon`: Bổ sung `maSanPham NVARCHAR(20) NULL` và `maDichVu NVARCHAR(20) NULL`.
     - Tạo `src/utils/PasswordHasher.java` (băm SHA-256).
     - Nâng cấp `src/utils/MaTuSinhHelper.java`: Tự động sinh và tự động tăng tất cả mã (`NV001..NVn`, `SP001..SPn`, `DV001..DVn`, `QD001..QDn`, `GC001..GCn`, `LO-yyMMdd-xxx`, `KH0001..KHn`, `TC0001..TCn`, `HD...`, `LH...`, `DT...`). Đặt `setEditable(false)` cho mọi ô Mã trên toàn bộ giao diện.
     - Cập nhật `entity/SanPham.java` (thêm `maVach`, `donViCoSo`, `giaBanCoSo`, `dsLoHang`, `dsQuyDoi`) và `entity/ChiTietHoaDon.java` (thêm `SanPham sanPham`, `DichVu dichVu` và cơ chế lấy giá bán niêm yết tự động từ `SanPham`/`QuyDoiDonVi`).
     - Cập nhật `LoginForm.java` & `DangNhapBus.java`: Đăng nhập bằng `soDienThoai` + `matKhau` (SHA-256) và thêm chức năng **Quên mật khẩu** (xác thực SĐT + Email -> gửi OTP qua `EmailPreviewDialog` -> đặt mật khẩu mới).
  3. **Phase 3 (Strict Layer Separation - 100% Logic in `bus`):**
     - Tạo `KhoLoHangBus`, `DoiTraBus`, `PetHotelBus`, `ThongKeBus` và cập nhật `BanHangBus`.
     - Xóa toàn bộ lời gọi `dao.*` và `DBConnection` ra khỏi các lớp trong `src/gui/`.
  4. **Phase 4 (Real Dashboard Data & Drill-Down Links):**
     - Xóa toàn bộ dữ liệu ảo trong `BarChartPanel` và `TongQuanPanel`.
     - Đổi tên thẻ KPI thứ 4 thành `"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`, dùng chung hàm `KhoLoHangBus.layDanhSachLoCanhBaoHanDung()` để đồng bộ 100% số liệu giữa Dashboard và màn hình Kho.
     - Gắn sự kiện Click (Drill-Down) vào 4 thẻ KPI trên `TongQuanPanel` để mở thẳng màn hình danh sách chi tiết tương ứng (Báo cáo doanh thu, Lịch sử hóa đơn bán lẻ/dịch vụ hôm nay, Lịch hẹn hôm nay, Danh sách lô sắp hết hạn).
  5. **Phase 5 (POS Redesign: 2 Tabs Product JTable+Barcode & Services, Flexible Points, Fast Hold-Order Search):**
     - Tách nửa trái `BanHangPOSPanel` thành `JTabbedPane` 2 Tab: Tab 1 (`Hàng Hóa Bán Lẻ` gồm ô Quét mã vạch Barcode tự động + Bảng `JTable` hàng hóa khóa sửa giá) và Tab 2 (`Dịch Vụ Spa & Chăm Sóc`), giữ nguyên Giỏ hàng chung và Đơn chờ.
     - Tra cứu khách hàng theo `soDienThoai`.
     - Thêm `JSpinner spnSoDiemDung` cho phép khách chọn số điểm tích lũy muốn dùng linh hoạt (thay vì bắt dùng hết).
     - Trên màn hình Đơn chờ (`VIEW_DON_CHO`), thêm ô tìm kiếm nhanh `txtTimDonCho` lọc tức thời theo SĐT khách hàng / Mã đơn / Tên khách.
  6. **Phase 6 (High-Volume Service Progress Management):**
     - Nâng cấp `QuanLyLichHenPanel`: Thêm bộ lọc theo KTV phụ trách (kèm đếm tải công việc), tìm nhanh theo SĐT, cảnh báo ca quá giờ, thanh Phân trang (`20 dòng/trang`), và chuyển lịch hẹn hoàn thành vào thẳng Giỏ hàng / Đơn chờ POS.
- **[FLASH-PROOF IRON RULES (QUY TẮC THÉP CHỐNG ẨU)]**:
  - ❌ CẤM RÚT GỌN CODE: Tuyệt đối không dùng `// TODO`, `// ...` hoặc làm mất các tính năng sẵn có (In K80, VietQR, VNPay, Spa Pass, Xuất Excel).
  - ❌ CẤM GỌI `dao.*` TRONG `gui.*`: Mọi truy xuất dữ liệu từ giao diện phải đi qua tầng `bus.*`.
  - ⚡ KIỂM TRA COMPILER: Chạy `cmd /c build.bat` sau mỗi Phase và sửa sạch mọi lỗi biên dịch trước khi đi tiếp.
```

---

## 7. Trạng Thái Hoàn Thành Toàn Diện (All 6 Phases Completed 100%)

> [!IMPORTANT]
> **Toàn bộ 6 Phase cải tổ đã được thực thi và kiểm chứng thành công 100%**.
> Lệnh `build.bat` biên dịch toàn bộ mã nguồn Java vào thư mục `bin` với kết quả: `[THANH CONG] Toan bo ma nguon Java da duoc bien dich thanh cong vao thu muc bin!` (0 lỗi cú pháp, 0 lỗi liên kết).

### Bảng Tổng Hợp Nghiệm Thu Kỹ Thuật:
1. **Phase 1 (Kiến trúc N-Tier chuẩn):** Đã phân chia sạch sẽ 5 package: `entity`, `dao`, `bus`, `gui` (kèm `gui.charts`), `utils`.
2. **Phase 2 (Đăng nhập & Mã tự sinh & Quên mật khẩu):** 
   - Đăng nhập bằng `soDienThoai` + mật khẩu băm `SHA-256`.
   - Bảng `TaiKhoan` dùng khóa chính `maTaiKhoan` (`NV001`..`NVn`), liên kết 1-1 với `NhanVien`.
   - Tính năng **Quên mật khẩu** 2 bước: SĐT + Email $\rightarrow$ sinh OTP 6 số $\rightarrow$ đổi mật khẩu mới băm SHA-256.
   - Toàn bộ mã định danh (`NVxxx`, `SPxxx`, `DVxxx`, `QDxxx`, `LO-yyMMdd-xxx`, `GCxxx`, `KHxxxx`, `TCxxxx`, `HD...`, `DT...`) tự động tăng qua `MaTuSinhHelper` và khóa cứng `setEditable(false)`, `setFocusable(false)`.
3. **Phase 3 (Tách biệt triệt để Logic — 0% DAO trong GUI):**
   - Đã tạo các lớp Bus chuyên biệt: `GiaoCaBus`, `KhoLoHangBus`, `DoiTraBus`, `PetHotelBus`, `KhachHangBus`, `ThongKeBus`, và nâng cấp `BanHangBus`.
   - Kết quả quét kiểm tra: `ALL CLEAN 100%! Zero DAO / DBConnection in gui package!`.
4. **Phase 4 (Dashboard liên kết dữ liệu thực 100% & Drill-Down):**
   - Xóa bỏ toàn bộ các mảng dữ liệu giả lập trong `BarChartPanel` và `TongQuanPanel`. Đọc 100% dữ liệu thực từ `ThongKeBus`.
   - Thẻ 4 đổi tên chuẩn thành: `"LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)"`, đồng bộ số lượng với `KhoLoHangBus`.
   - Tích hợp sự kiện Click-to-Drill-Down trên cả 4 thẻ KPI chuyển nhanh sang các màn hình chi tiết tương ứng.
5. **Phase 5 (Nâng cấp POS 2 Tab, Quét Barcode & Điểm linh hoạt):**
   - Tách nửa trái `BanHangPOSPanel` thành `JTabbedPane`:
     + `Tab 1: 📦 Hàng Hóa Bán Lẻ`: Ô quét Barcode siêu tốc (hỗ trợ phím tắt `F2` focus, Enter tự động thêm vào giỏ hàng) + Bảng `JTable` danh mục hàng hóa (khóa cứng sửa đơn giá).
     + `Tab 2: ✂️ Dịch Vụ Spa & Chăm Sóc`: Bộ lọc danh mục dịch vụ + Thẻ card chọn KTV và bé cưng.
   - Điểm tích lũy chuyển sang `JSpinner spnDiemDung`, cho phép khách chọn dùng số điểm linh hoạt (`0` $\rightarrow$ max điểm).
   - Ô tìm kiếm nhanh Đơn tạm `txtTimDonCho` trong Tab Đơn chờ lọc tức thời theo SĐT khách hàng/Mã đơn.
6. **Phase 6 (Quản lý Tiến độ Dịch vụ Lưu lượng Lớn & Khóa Mã Tự Sinh):**
   - `QuanLyLichHenPanel`: Bộ lọc theo KTV (hiển thị số ca đang làm), cảnh báo ca quá giờ `⚠️ Quá giờ`, thanh phân trang (`20 dòng/trang`), chuyển 1 chạm sang Đơn tạm POS.
   - Khóa `setEditable(false)` và `setFocusable(false)` toàn bộ các trường textfield mã trên các form nhập liệu.
   - Tuyệt đối không can thiệp hay liên kết Git / GitHub theo yêu cầu của người dùng.
