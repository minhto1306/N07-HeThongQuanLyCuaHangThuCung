# Kế hoạch Tối giản hóa Toàn diện Dự án — Chuẩn bị Báo cáo Đồ án Eclipse

> **Mục tiêu:** Rà soát toàn bộ source code tại `D:\HOCTAP\HK1_2026-2027\PhatTrienUngDung (AD)\BAITAPNHOM\Repo_Group07-HeThongQuanLyCuaHangThuCung`, thực hiện đợt tối giản hóa toàn diện theo phong cách "gọn gàng, thực tế, đúng sức sinh viên", đảm bảo 0 lỗi biên dịch, không in chữ đỏ ra Console Eclipse, giao diện sạch đẹp như phần mềm thực tế và phân quyền 4 vai trò chạy mượt mà 100%.

---

## 1. Mục tiêu & Phạm vi Thay đổi

```mermaid
flowchart TD
    A[Rà soát Source Code] --> B[1. Dọn dẹp UI & Chữ thừa]
    A --> C[2. Khóa an toàn chống Crash]
    A --> D[3. Chuẩn hóa 4 Vai trò Phân quyền]
    A --> E[4. Tương thích 100% Eclipse IDE]

    B --> B1[Xóa thông tin nhóm ở LoginForm]
    B --> B2[Xóa cảnh báo Mock Data / Cổng 1433 & Version Sidebar]
    B --> B3[Sửa lỗi đè chữ BarChartPanel]

    C --> C1[Khóa/thông báo nhẹ tính năng Khách sạn thú cưng & Nâng cao]
    C --> C2[Thêm Row Selection Guards chống NullPointer]
    C --> C3[Loại bỏ System.err để Eclipse Console không hiện chữ đỏ]

    D --> D1[admin / [REDACTED_SAMPLE_PASSWORD] -> NVQL: Toàn quyền Dashboard & Quản lý]
    D --> D2[banhang01 / [REDACTED_SAMPLE_PASSWORD] -> NVBH: Chỉ mở POS]
    D --> D3[thungan01 / [REDACTED_SAMPLE_PASSWORD] -> NVTN: Thu ngân POS & Giao ca]
    D --> D4[chamsoc01 / [REDACTED_SAMPLE_PASSWORD] -> NVCS: Lịch hẹn & Hồ sơ thú cưng]
    D --> D5[Header chuẩn: Tên nhân viên - Tên vai trò]

    E --> E1[Tạo file .project và .classpath chuẩn Eclipse trong repo]
    E --> E2[Khắc phục triệt để warning deprecation HoaDonDAO]
    E --> E3[Đồng bộ sang eclipse-workspace]
```

---

## 2. User Review Required

> [!IMPORTANT]
> **Tài khoản Đăng nhập & Mật khẩu chuẩn:**
> Hệ thống sẽ chuẩn hóa chính thức 4 tài khoản với mật khẩu chung `[REDACTED_SAMPLE_PASSWORD]`:
> 1. `admin` / `[REDACTED_SAMPLE_PASSWORD]` $\rightarrow$ **Quản lý (`NVQL`)**: Xem toàn bộ phân hệ, doanh thu, biểu đồ KPI, nhân sự.
> 2. `banhang01` / `[REDACTED_SAMPLE_PASSWORD]` $\rightarrow$ **Bán hàng (`NVBH`)**: Chỉ mở duy nhất màn hình POS bán hàng (Sidebar chỉ có 1 nút POS).
> 3. `thungan01` / `[REDACTED_SAMPLE_PASSWORD]` $\rightarrow$ **Thu ngân (`NVTN`)**: Mở màn hình POS bán hàng & Bàn giao ca làm việc.
> 4. `chamsoc01` / `[REDACTED_SAMPLE_PASSWORD]` $\rightarrow$ **Chăm sóc (`NVCS`)**: Chỉ mở Lịch hẹn dịch vụ và Hồ sơ thú cưng (vẫn hỗ trợ alias `ktv01` nếu kiểm thử viên gõ nhầm).

> [!NOTE]
> **Trạng thái Database ở thanh dưới đáy (Status Bar):**
> Thanh cam cảnh báo `[Chế độ Demo Offline - CSDL Cục bộ] Cổng 1433 chưa mở, đang dùng Mock Data an toàn` sẽ được thay thế bằng dòng trạng thái trung tính, chuyên nghiệp:
> `Trạng thái: Sẵn sàng làm việc` (kèm icon tick xanh). Không để lộ thông tin thiếu kết nối DB hay cổng 1433 trước mặt giảng viên.

---

## 3. Chi tiết Đề xuất Thay đổi theo Từng Phân hệ

### Phân hệ 1: Dọn dẹp Giao diện & Chữ thừa (UI Simplification)

#### [MODIFY] `src/views/LoginForm.java`
- **Xóa bỏ khối chữ nhóm ở banner cam:** Xóa hoàn toàn component `lblTeam` chứa nội dung `Nhóm 07 - HK1 2026-2027 / Khoa CNTT - Đại học Công Nghiệp TP.HCM` ở banner bên trái.
- **Cập nhật khung gợi ý tài khoản Demo:** Đổi `ktv01` thành `chamsoc01` để hiển thị đúng 4 vai trò:
  - `admin` (NVQL) | `thungan01` (NVTN)
  - `banhang01` (NVBH) | `chamsoc01` (NVCS)
  - Mật khẩu: `[REDACTED_SAMPLE_PASSWORD]`
- **Default form inputs:** Giữ mặc định `admin` / `[REDACTED_SAMPLE_PASSWORD]` để người dùng có thể nhấn Đăng nhập hoặc Enter ngay lập tức.

#### [MODIFY] `src/views/MainDashboard.java`
- **Tiêu đề cửa sổ (Title):** Thay `setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG - NHÓM 07...")` bằng:
  `setTitle("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG - " + UserSession.getFullName() + " [" + UserSession.getRoleCode() + "]");`
- **Top Header:**
  - Xóa bỏ huy hiệu `"GROUP 07"` ở góc trái logo.
  - Cập nhật thông tin người dùng góc phải hiển thị rõ ràng: `[Tên nhân viên] - [Tên vai trò]` (Ví dụ: `Lê Khãi Minh - Quản lý (NVQL)`, `Trần Thị Thu Hà - Bán hàng (NVBH)`).
- **Sidebar Footer:**
  - Xóa bỏ hoàn toàn panel footer chứa chữ `Phiên bản 1.0 (FlatLaf Light) / Cửa hàng Thú cưng N07`.
- **Bottom Status Bar:**
  - Xóa bỏ nhãn cảnh báo cam `lblDbStatus` liên quan đến Offline Mode/Cổng 1433/Mock data.
  - Thay bằng nhãn màu xanh ngọc nhẹ nhàng: `Trạng thái: Sẵn sàng làm việc` (Icon check tròn xanh).

#### [MODIFY] `src/views/charts/BarChartPanel.java`
- **Sửa lỗi đè chữ ở biểu đồ 7 ngày:**
  - Xóa bỏ `lblSub` chứa chuỗi `Thứ 2 – Chủ Nhật • Cột cam nổi bật đỉnh tuần` đang đè lên tiêu đề `XU HƯỚNG DOANH THU 7 NGÀY GẦN NHẤT`.
  - Bổ sung padding đáy cho `pnlHeader` để tiêu đề đứng độc lập, thoáng đãng, không bị chồng lấn.

---

### Phân hệ 2: Khóa An Toàn các Tính Năng Chưa Hoàn Thiện (Safe Guard)

#### [MODIFY] `src/views/BanHangPOSPanel.java`
- **Khóa tính năng Khách sạn thú cưng:**
  - Khi người dùng click vào tab danh mục `"Khách sạn thú cưng"` hoặc món `"Lưu Chuồng Pet Hotel (VIP)"`: Hiển thị thông báo nhẹ nhàng:
    ```java
    JOptionPane.showMessageDialog(this, 
        "Chức năng Khách sạn thú cưng đang được hoàn thiện trong giai đoạn tiếp theo!", 
        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    ```
- **Rà soát các thao tác hóa đơn:** Kiểm tra tất cả nút bấm thao tác giỏ hàng, in ấn, xem chi tiết để đảm bảo không ném ra ngoại lệ khi chưa chọn mục.

#### [MODIFY] `src/views/DatLichHenDialog.java`
- Khi khách hàng hoặc nhân viên chọn mục `"DV006 - Khách sạn thú cưng lưu trú 24h"` trong danh sách dịch vụ: Hiển thị thông báo tính năng đang hoàn thiện và giữ chọn gói dịch vụ cơ bản (DV001/DV002).

#### [MODIFY] `src/views/KhoLoHangPanel.java`
- Giữ nút "Thêm Sản phẩm & Lô mới" và "Kiểm kê tồn kho" hoạt động bình thường trên Mock Data; đối với nút "Thiết lập Quy đổi ĐVT" nâng cao, đảm bảo dialog mở mượt mà hoặc thông báo hoàn thiện an toàn nếu dữ liệu trống.

#### [MODIFY] `src/utils/DBConnection.java` & `src/controllers/DangNhapController.java` & `src/controllers/ThuCungController.java`
- Thay thế toàn bộ các lệnh `System.err.println(...)` liên quan đến cảnh báo không kết nối được SQL Server 1433 bằng xử lý yên lặng (silent fallback) hoặc log thông thường. Đảm bảo tab Console trong Eclipse **hoàn toàn sạch bóng chữ đỏ**.

---

### Phân hệ 3: Chuẩn Hóa Đăng Nhập & Phân Quyền (Authentication & Authorization)

#### [MODIFY] `src/controllers/DangNhapController.java`
- Bổ sung tài khoản `chamsoc01` (và giữ alias `ktv01`) với mật khẩu `[REDACTED_SAMPLE_PASSWORD]`, map trực tiếp tới nhân viên Tô Thanh Đạt (`NVCS`).
- Đồng bộ danh sách 4 tài khoản mặc định kiểm tra:
  1. `admin` $\rightarrow$ `NV001`, Lê Khãi Minh, `NVQL`
  2. `banhang01` $\rightarrow$ `NV004`, Trần Thị Thu Hà, `NVBH`
  3. `thungan01` $\rightarrow$ `NV002`, Lê Anh Minh, `NVTN`
  4. `chamsoc01` / `ktv01` $\rightarrow$ `NV003`, Tô Thanh Đạt, `NVCS`

#### [MODIFY] `src/controllers/NhanVienController.java`
- Cập nhật danh sách Mock NhanVien: đổi username của Tô Thanh Đạt thành `chamsoc01` (vai trò `NVCS`) để khi Quản lý vào màn hình "Quản lý Nhân sự & TK" sẽ thấy đúng username `chamsoc01`.

#### [MODIFY] `src/utils/UserSession.java`
- Cập nhật logic thẩm quyền `canAccess(String cardName)`:
  - `NVQL`: Có quyền truy cập toàn bộ các phân hệ (`CARD_TONG_QUAN`, `CARD_POS`, `CARD_LICH_HEN`, `CARD_THU_CUNG`, `CARD_KHO`, `CARD_NHAN_VIEN`, `CARD_PHAN_CA`, `CARD_GIAO_CA`).
  - `NVBH`: Chỉ cho phép `CARD_POS`. Tất cả các card khác trả về `false`.
  - `NVCS`: Chỉ cho phép `CARD_LICH_HEN` và `CARD_THU_CUNG`.
  - `NVTN`: Cho phép `CARD_POS` và `CARD_GIAO_CA`.

#### [MODIFY] `src/views/MainDashboard.java`
- Tinh chỉnh danh sách Sidebar theo đúng yêu cầu:
  - `NVQL`: 8 nút đầy đủ (Tổng quan hệ thống, Bán hàng POS, Quản lý Lịch hẹn, Hồ sơ Thú cưng, Kho & Lô hàng, Quản lý Nhân sự & TK, Lịch Phân ca Tuần, Bàn giao ca làm việc).
  - `NVBH`: 1 nút duy nhất ("Bán hàng POS"). Mặc định mở ngay màn hình POS.
  - `NVCS`: 2 nút ("Lịch hẹn dịch vụ", "Hồ sơ Thú cưng"). Mặc định mở ngay Lịch hẹn.
  - `NVTN`: 2 nút ("Thu ngân & Bán hàng POS", "Bàn giao ca làm việc"). Mặc định mở ngay POS.

---

### Phân hệ 4: Tương Thích Hoàn Toàn với Eclipse IDE

#### [NEW] `.project` & `.classpath` (Tại thư mục gốc repo)
- Thiết lập file `.project` chuẩn Eclipse: Tên project `Repo_Group07-HeThongQuanLyCuaHangThuCung` với Java Nature và Java Builder.
- Thiết lập file `.classpath` chuẩn Eclipse:
  - Source path: `src`
  - Library: `lib/flatlaf-3.5.4.jar`, `lib/mssql-jdbc-12.8.1.jre11.jar`
  - JRE Container: JavaSE-17 (hoặc compatible Standard VM)
  - Output path: `bin`

#### [MODIFY] `src/models/dao/HoaDonDAO.java`
- Sửa triệt để warning deprecation:
  - Thay `BigDecimal.ROUND_FLOOR` bằng `RoundingMode.FLOOR` tại dòng 136.
  - Giúp `javac` biên dịch đạt trạng thái **0 errors, 0 warnings**.

#### [SYNC] Đồng bộ sang `C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio`
- Đồng bộ các file mã nguồn đã tối giản sang thư mục Eclipse workspace để khi người dùng mở Eclipse và nhấn Run `Main.java` thì ứng dụng chạy ngay lập tức.

---

## 4. Kế hoạch Kiểm tra & Nghiệm thu (Verification Plan)

### Automated Tests
1. **Kiểm tra Biên dịch sạch (Clean Build):**
   ```cmd
   build.bat
   ```
   *Kỳ vọng:* `[THANH CONG] Toan bo ma nguon Java da duoc bien dich thanh cong vao thu muc bin!` (0 errors, 0 deprecation warnings).
2. **Kiểm tra Biên dịch với cờ khắt khe `-Xlint:deprecation`:**
   ```cmd
   javac -encoding UTF-8 -cp "lib/*;src" -Xlint:deprecation -d bin src/Main.java src/models/dao/*.java
   ```
   *Kỳ vọng:* Không có bất kỳ cảnh báo nào xuất hiện.
3. **Kiểm tra Suite Test Phân quyền:**
   Chạy script kiểm tra phân quyền để xác nhận cả 4 tài khoản và thẩm quyền các card hoạt động chính xác.

### Manual Verification
1. **Kiểm tra Màn hình Đăng nhập (LoginForm):**
   - Khởi chạy ứng dụng: Giao diện bật lên êm ru, không in chữ đỏ ra Console.
   - Banner cam bên trái: Đã xóa sạch dòng `Nhóm 07 - HK1 2026-2027 / Khoa CNTT - Đại học Công Nghiệp TP.HCM`.
   - Khung gợi ý: Hiển thị đúng 4 tài khoản (`admin`, `banhang01`, `thungan01`, `chamsoc01`).
2. **Đăng nhập lần lượt 4 tài khoản:**
   - **Tài khoản `admin`:** Header hiển thị `Lê Khãi Minh - Quản lý hệ thống [NVQL]`, Sidebar có đầy đủ 8 menu, xem được doanh thu và biểu đồ.
   - **Tài khoản `banhang01`:** Header hiển thị `Trần Thị Thu Hà - Nhân viên Bán hàng [NVBH]`, Sidebar chỉ có 1 nút "Bán hàng POS", mở ngay màn hình POS.
   - **Tài khoản `thungan01`:** Header hiển thị `Lê Anh Minh - Nhân viên Thu ngân [NVTN]`, Sidebar chỉ có 2 nút POS & Giao ca.
   - **Tài khoản `chamsoc01`:** Header hiển thị `Tô Thanh Đạt - Nhân viên Chăm sóc Thú cưng [NVCS]`, Sidebar chỉ có 2 nút Lịch hẹn & Hồ sơ thú cưng.
3. **Kiểm tra Biểu đồ & Status Bar:**
   - Dashboard: Tiêu đề `XU HƯỚNG DOANH THU 7 NGÀY GẦN NHẤT` sạch đẹp, không còn dòng chú thích bị đè chữ.
   - Góc dưới màn hình: Đã xóa bỏ thanh cam cảnh báo Cổng 1433/Mock data, hiển thị `Trạng thái: Sẵn sàng làm việc`.
   - Sidebar: Đã xóa khối version ở chân menu.
4. **Kiểm tra Khóa an toàn:**
   - Thử bấm vào "Khách sạn thú cưng" $\rightarrow$ Hiển thị thông báo nhẹ nhàng giai đoạn tiếp theo, không bị lỗi Exception.
