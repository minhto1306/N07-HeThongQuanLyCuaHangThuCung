# 📘 HƯỚNG DẪN CLONE REPO, PUSH CODE VÀ TẠO PULL REQUEST TRÊN ECLIPSE IDE
> **Dành cho:** Thành viên Nhóm 07 - Đề tài Hệ Thống Quản Lý Cửa Hàng Thú Cưng  
> **Repository:** `https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung.git`  
> **Mục tiêu:** Hướng dẫn trực quan 100% bằng giao diện Eclipse IDE cho các bạn chưa quen dùng Git, từ bước Clone dự án vào Eclipse, tạo nhánh, copy code, commit 3 đợt, push lên GitHub và Review chéo.

---

## 🎯 NGUYÊN TẮC BẮT BUỘC CỦA CẢ NHÓM
1. **Tuyệt đối không đụng vào nhánh `main`:** Nhánh `main` là nhánh phát hành chính thức để Leader nộp bài cuối kỳ.
2. **Clone trực tiếp vào Eclipse IDE:** Không cần tạo thư mục ngoài hay gõ lệnh phức tạp.
3. **Mỗi người làm việc trên nhánh riêng:** Tạo nhánh từ `develop`, sau khi code xong thì tạo **Pull Request (PR)** để xin gộp vào nhánh **`develop`**.
4. **Mỗi bạn chia làm 3 đợt commit nhỏ** (Entity $\rightarrow$ DAO $\rightarrow$ View/Controller) để biểu đồ đóng góp (Contributors Graph) trên GitHub của bạn dày dặn và được chấm điểm cao.

---

## BƯỚC 1: CẤU HÌNH ĐỊNH DANH GIT TRONG ECLIPSE (LÀM 1 LẦN DUY NHẤT)

Để GitHub ghi nhận đúng tên và avatar của bạn khi commit:
1. Mở **Eclipse IDE**.
2. Trên thanh menu, chọn: **Window** $\rightarrow$ **Preferences**.
3. Ở ô tìm kiếm phía trên bên trái, gõ chữ `Git` $\rightarrow$ Chọn mục **Configuration** (dưới nhánh *Version Control (Team) / Git*).
4. Bấm vào thẻ **User Settings**:
   - Nếu đã có 2 dòng `user.name` và `user.email`: Kiểm tra xem email có đúng với email tài khoản GitHub của bạn chưa.
   - Nếu chưa có: Bấm nút **Add Entry...**:
     - Key: `user.name` | Value: `Họ và Tên Của Bạn` (Bấm OK).
     - Bấm tiếp **Add Entry...**: Key: `user.email` | Value: `email_dang_ky_github@gmail.com` (Bấm OK).
5. Bấm **Apply and Close**.

> [!IMPORTANT]
> **Email phải trùng khớp 100%** với email tài khoản GitHub của bạn. Nếu sai email, biểu đồ đóng góp của bạn trên GitHub sẽ bị trắng tinh!

---

## BƯỚC 2: CLONE REPOSITORY TRỰC TIẾP VÀO ECLIPSE IDE

Không cần mở Terminal, bạn clone thẳng dự án vào Workspace của Eclipse:

1. Mở góc nhìn **Git Repositories** trong Eclipse:
   - Trên menu chọn: **Window** $\rightarrow$ **Show View** $\rightarrow$ **Other...**
   - Gõ tìm `Git Repositories` $\rightarrow$ Nhấp chọn và bấm **Open**.
   *(Cửa sổ Git Repositories sẽ xuất hiện ở góc dưới hoặc bên cạnh màn hình)*.

2. Bấm vào dòng chữ màu xanh: **Clone a Git repository** (hoặc biểu tượng icon Clone Repository có hình thư mục và mũi tên).

3. Cửa sổ **Source Git Repository** hiện ra:
   - Dán URL dự án vào ô **URI**:
     ```
     https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung.git
     ```
   - Eclipse sẽ tự động điền Host (`github.com`) và Repository path.
   - Ở khung **Authentication** phía dưới:
     - **User:** Nhập username (hoặc email) GitHub của bạn.
     - **Password:** Nhập mật khẩu tài khoản GitHub hoặc **Personal Access Token (classic)** của bạn.
     - Tick chọn ô **Store in Secure Store** để lần sau không cần nhập lại mật khẩu.
   - Bấm **Next**.

4. Cửa sổ **Branch Selection**:
   - Tích chọn nhánh **`develop`** (nhánh làm việc chung của nhóm).
   - Bấm **Next**.

5. Cửa sổ **Local Destination**:
   - **Directory:** Giữ nguyên đường dẫn mặc định trong thư mục git của Eclipse.
   - **Initial branch:** Chọn **`develop`**.
   - **CỰC KỲ QUAN TRỌNG:** Ở phần *Wizard for project import*, đảm bảo đã tick chọn:
     👉 **Import all existing Eclipse projects after clone finishes**  
     *(Vì dự án đã có sẵn cấu hình `.project` và `.classpath`, Eclipse sẽ tự động nạp thành Java Project hoàn chỉnh ngay lập tức!)*
   - Bấm **Finish**.

🎉 **Kết quả:** Ngay trong cửa sổ **Package Explorer** của Eclipse sẽ xuất hiện dự án:
`N07-HeThongQuanLyCuaHangThuCung [develop]` kèm biểu tượng kho Git!

---

## BƯỚC 3: TẠO VÀ CHUYỂN SANG NHÁNH RIÊNG CỦA BẠN TRÊN ECLIPSE

Trước khi thêm code, bạn phải tạo nhánh riêng của mình từ nhánh `develop`:

1. Trong cửa sổ **Package Explorer**, nhấp chuột phải vào dự án `N07-HeThongQuanLyCuaHangThuCung`.
2. Chọn: **Team** $\rightarrow$ **Switch To** $\rightarrow$ **New Branch...**
3. Cửa sổ *Create Branch* hiện lên:
   - Ở ô **Branch name**, bạn nhập đúng tên nhánh được phân công của mình:
     - **Bạn Tô Thanh Đạt:** `feature/kho-san-pham-lo-hang`
     - **Bạn Võ Thị Hồng Ngọc:** `feature/thu-cung-dich-vu-hotel`
     - **Bạn Lê Anh Minh:** `feature/nhan-su-giao-ca-dashboard`
     - **Bạn Lê Khãi Minh:** `feature/pos-ban-hang-doi-tra`
   - Đảm bảo ô **Configure upstream for push and pull** đã được tick.
   - Tick chọn ô **Check out new branch**.
4. Bấm **Finish**.

👉 Nhìn lại Package Explorer, tên dự án sẽ đổi thành:  
`N07-HeThongQuanLyCuaHangThuCung [feature/<tên-nhánh-của-bạn>]` $\rightarrow$ Đã sẵn sàng làm việc!

---

## BƯỚC 4: COPY CÁC TỆP MÃ NGUỒN VÀO DỰ ÁN TRÊN ECLIPSE

Leader (Khãi Minh) sẽ gửi cho bạn thư mục gói code được phân công:
- Bạn mở thư mục đó trên máy tính và mở Eclipse song song:
  - Các file Entity $\rightarrow$ Kéo thả hoặc Copy & Paste vào gói `src/models/entity/` trong Eclipse.
  - Các file DAO $\rightarrow$ Bỏ vào gói `src/models/dao/` trong Eclipse.
  - Các file Controller $\rightarrow$ Bỏ vào gói `src/controllers/` trong Eclipse.
  - Các file View / Dialog $\rightarrow$ Bỏ vào gói `src/views/` (hoặc `src/views/charts/`) trong Eclipse.
- Nhấp chuột phải vào tên dự án trong Eclipse $\rightarrow$ bấm **Refresh** (`F5`).

---

## BƯỚC 5: THỰC HIỆN 3 ĐỢT COMMIT BẰNG CỬA SỔ "GIT STAGING"

Để biểu đồ đóng góp của bạn dày dặn, hãy chia làm **3 đợt commit**:

1. Mở cửa sổ **Git Staging**:
   - Chuột phải vào dự án $\rightarrow$ **Team** $\rightarrow$ **Commit...**
   - Cửa sổ Git Staging sẽ mở ra, gồm 2 ô chính:
     - **Unstaged Changes:** Các file bạn mới thêm vào (đang có dấu `?` hoặc `>`).
     - **Staged Changes:** Các file được chọn để lưu vào commit này.
     - **Commit Message:** Ô gõ lời nhắn mô tả công việc.

---

### 👨‍💻 CHI TIẾT 3 ĐỢT COMMIT CHO BẠN: TÔ THANH ĐẠT (`feature/kho-san-pham-lo-hang`)

- **Đợt 1 (Entity):**
  - Trong ô *Unstaged Changes*, chỉ chọn 6 file Entity: `DanhMucHang.java`, `DonViTinh.java`, `NhaCungCap.java`, `SanPham.java`, `QuyDoiDonVi.java`, `LoHang.java` $\rightarrow$ Kéo xuống ô *Staged Changes* (hoặc bấm dấu `+`).
  - Ô *Commit Message*, dán nội dung:
    ```
    feat(entity): create SanPham, LoHang, QuyDoiDonVi, DanhMucHang, NhaCungCap models
    ```
  - Bấm nút **Commit** (ở góc dưới bên phải).

- **Đợt 2 (DAO):**
  - Kéo 2 file DAO: `SanPhamDAO.java`, `LoHangDAO.java` xuống ô *Staged Changes*.
  - Ô *Commit Message*, dán nội dung:
    ```
    feat(dao): implement SanPhamDAO and LoHangDAO for FEFO stock management
    ```
  - Bấm nút **Commit**.

- **Đợt 3 (Giao diện View):**
  - Kéo các file View còn lại: `KhoLoHangPanel.java`, `ThietLapQuyDoiDialog.java`, `NhapSanPhamVaLoHangDialog.java` xuống ô *Staged Changes*.
  - Ô *Commit Message*, dán nội dung:
    ```
    feat(view): add KhoLoHangPanel, ThietLapQuyDoiDialog and NhapSanPhamVaLoHangDialog
    ```
  - Bấm nút **Commit and Push...** để đẩy luôn lên GitHub!

---

### 👩‍💻 CHI TIẾT 3 ĐỢT COMMIT CHO BẠN: VÕ THỊ HỒNG NGỌC (`feature/thu-cung-dich-vu-hotel`)

- **Đợt 1 (Entity):**
  - Kéo 6 file Entity: `ThuCung.java`, `HoSoThuCung.java`, `HoSoSucKhoe.java`, `DichVu.java`, `LichHen.java`, `ChiTietLichHen.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(entity): add ThuCung, HoSoThuCung, HoSoSucKhoe, DichVu, LichHen models
    ```
  - Bấm nút **Commit**.

- **Đợt 2 (DAO):**
  - Kéo 4 file DAO: `ThuCungDAO.java`, `DichVuDAO.java`, `LichHenDAO.java`, `PhongLuuTruDAO.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(dao): implement ThuCungDAO, DichVuDAO, LichHenDAO and PhongLuuTruDAO
    ```
  - Bấm nút **Commit**.

- **Đợt 3 (Controller & Giao diện View):**
  - Kéo các file còn lại: `ThuCungController.java`, `ThuCungDichVuPanel.java`, `PetHotelPanel.java`, `QuanLyLichHenPanel.java`, `DatLichHenDialog.java`, `ChonDichVuDialog.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(view): add ThuCungDichVuPanel, PetHotelPanel, QuanLyLichHenPanel and dialogs
    ```
  - Bấm nút **Commit and Push...** để đẩy luôn lên GitHub!

---

### 👨‍💻 CHI TIẾT 3 ĐỢT COMMIT CHO BẠN: LÊ ANH MINH (`feature/nhan-su-giao-ca-dashboard`)

- **Đợt 1 (Entity):**
  - Kéo 5 file Entity: `TaiKhoan.java`, `NhanVien.java`, `CaLamViec.java`, `BangPhanCong.java`, `BangGiaoCa.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(entity): add TaiKhoan, NhanVien, CaLamViec, BangPhanCong, BangGiaoCa models
    ```
  - Bấm nút **Commit**.

- **Đợt 2 (DAO & Controllers):**
  - Kéo 4 DAO & 4 Controller: `TaiKhoanDAO.java`, `NhanVienDAO.java`, `PhanCaDAO.java`, `BangGiaoCaDAO.java`, `DangNhapController.java`, `NhanVienController.java`, `PhanCaController.java`, `GiaoCaController.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(dao): implement TaiKhoanDAO, NhanVienDAO, PhanCaDAO, BangGiaoCaDAO and controllers
    ```
  - Bấm nút **Commit**.

- **Đợt 3 (Giao diện View & Dashboard Graphics2D):**
  - Kéo các file View còn lại: `LoginForm.java`, `MainDashboard.java`, `QuanLyNhanVienPanel.java`, `LichPhanCaPanel.java`, `BanGiaoCaPanel.java`, `TongQuanPanel.java`, `BaoCaoThongKeDialog.java`, `BarChartPanel.java`, `DonutChartPanel.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(view): implement MainDashboard, LoginForm, QuanLyNhanVienPanel, BanGiaoCaPanel and Charts
    ```
  - Bấm nút **Commit and Push...** để đẩy luôn lên GitHub!

---

### 👨‍💻 CHI TIẾT 3 ĐỢT COMMIT CHO BẠN: LÊ KHÃI MINH (`feature/pos-ban-hang-doi-tra`)

- **Đợt 1 (Entity):**
  - Đã hoàn thành (Commit `46d12c7`: `feat(model): them cac model ban hang hoa don va doi tra`).

- **Đợt 2 (DAO & Controller):**
  - Kéo `KhachHangDAO.java`, `HoaDonDAO.java`, `BanHangController.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(dao): implement HoaDonDAO with atomic transaction, KhachHangDAO and BanHangController
    ```
  - Bấm nút **Commit**.

- **Đợt 3 (Giao diện POS, VietQR, VNPay & Đổi trả):**
  - Kéo các file View: `BanHangPOSPanel.java`, `HoaDonPreviewDialog.java`, `VietQRDialog.java`, `VNPayDialog.java`, `DoiTraHangPanel.java` xuống ô *Staged Changes*.
  - Commit Message:
    ```
    feat(view): build BanHangPOSPanel, VietQRDialog, VNPayDialog, DoiTraHangPanel and invoice print preview
    ```
  - Bấm nút **Commit and Push...** để đẩy lên GitHub!

---

## BƯỚC 6: PUSH NHÁNH LÊN GITHUB (NẾU CHƯA BẤM NÚT COMMIT AND PUSH)

Nếu ở Đợt 3 bạn chỉ bấm nút **Commit**, thì đẩy code lên GitHub như sau:
1. Nhấp chuột phải vào dự án trong Eclipse $\rightarrow$ **Team** $\rightarrow$ **Push Branch 'feature/...'**
2. Cửa sổ Push hiện lên, kiểm tra Remote đã trỏ đúng vào repository của nhóm $\rightarrow$ Bấm **Next** (hoặc **Preview**).
3. Bấm nút **Push** (hoặc **Finish**).
4. Khi Eclipse báo *Pushed to ... [new branch]* là nhánh của bạn đã xuất hiện chính thức trên GitHub!

---

## BƯỚC 7: TẠO PULL REQUEST (PR) GỘP VÀO NHÁNH DEVELOP

Sau khi push thành công từ Eclipse:
1. Mở trình duyệt web và truy cập vào repository:  
   👉 [https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung](https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung)
2. Phía trên đầu trang sẽ hiện thanh màu vàng thông báo nhánh của bạn vừa đẩy $\rightarrow$ Bấm nút xanh **`Compare & pull request`**.
3. **Lưu ý cực kỳ quan trọng ở phần chọn nhánh:**
   - **base:** chọn **`develop`** (TUYỆT ĐỐI KHÔNG CHỌN `main`).
   - **compare:** chọn nhánh của bạn (ví dụ `feature/kho-san-pham-lo-hang`).
4. **Tiêu đề PR:** Viết rõ ràng (Ví dụ: `feat(kho): Hoàn thiện quản lý sản phẩm, lô hàng FEFO và quy đổi đơn vị`).
5. **Reviewers:** Ở cột bên phải mục **Reviewers**, tick chọn tên các bạn khác trong nhóm để mời vào xem code.
6. Bấm nút xanh **`Create pull request`**.

---

## BƯỚC 8: CÁC THÀNH VIÊN VÀO REVIEW & APPROVE (LẤY ĐIỂM NHÓM CAO)

Khi có bạn gửi link PR:
1. Bạn mở link Pull Request đó trên GitHub.
2. Bấm vào thẻ **`Files changed`** ở trên để xem qua các đoạn mã.
3. Bấm vào nút xanh lá **`Review changes`** (góc trên bên phải):
   - Chọn mục **`Approve`** (Đồng ý).
   - Viết lời nhận xét tích cực (Ví dụ: *"Đã kiểm tra code, tuân thủ đúng mô hình Pure Java MVC và chạy thử nghiệm tốt!"*).
   - Bấm nút **`Submit review`**.

---

## BƯỚC 9: LEADER BẤM MERGE CODE VÀO DEVELOP

Khi PR đã nhận đủ lượt Approve:
1. Trưởng nhóm (Khãi Minh) mở PR đó ra.
2. Cuộn xuống cuối trang bấm **`Merge pull request`** $\rightarrow$ bấm **`Confirm merge`**.
3. Nút chuyển sang màu tím với chữ **Merged** là hoàn tất tích hợp thành công!

---
*(Tài liệu lưu hành nội bộ - Nhóm 07 - Đề tài Hệ Thống Quản Lý Cửa Hàng Thú Cưng - IUH)*
