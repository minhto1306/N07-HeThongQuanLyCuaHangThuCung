# 📘 HƯỚNG DẪN PUSH CODE VÀ TẠO PULL REQUEST (DÀNH CHO TỪNG THÀNH VIÊN)
> **Dành cho:** Thành viên Nhóm 07 - Đề tài Hệ Thống Quản Lý Cửa Hàng Thú Cưng  
> **Repository:** `https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung.git`  
> **Mục tiêu:** Cầm tay chỉ việc từ cài đặt cấu hình ban đầu, tạo nhánh, copy code, băm commit theo chuẩn, đẩy lên GitHub và Review chéo.

---

## 🎯 NGUYÊN TẮC BẮT BUỘC CỦA CẢ NHÓM
1. **Tuyệt đối không đụng vào nhánh `main`:** Nhánh `main` chỉ để Leader nộp bài cuối kỳ cho giảng viên.
2. **Mọi người chỉ làm việc trên nhánh riêng của mình**, sau đó tạo **Pull Request (PR)** để xin gộp vào nhánh **`develop`**.
3. **Mỗi người chia làm 3 commit nhỏ** (Entity $\rightarrow$ DAO $\rightarrow$ View/Controller) để biểu đồ đóng góp (Contributors Graph) của bạn trên GitHub dày dặn và đẹp mắt.

---

## BƯỚC 1: CẤU HÌNH ĐỊNH DANH GIT TRÊN MÁY BẠN (LÀM 1 LẦN DUY NHẤT)

Mở cửa sổ **Git Bash** (hoặc Terminal / PowerShell) trên máy tính của bạn và gõ 2 lệnh sau:

```bash
git config --global user.name "Họ và Tên Của Bạn"
git config --global user.email "email_ban_dung_dang_ky_github@gmail.com"
```

> [!IMPORTANT]
> **Email phải trùng khớp 100%** với email bạn dùng đăng ký tài khoản GitHub. Nếu gõ sai email, commit của bạn sẽ bị tính là "người lạ" và biểu đồ đóng góp của bạn trên GitHub sẽ bị trắng tinh!

---

## BƯỚC 2: TẢI DỰ ÁN VỀ MÁY (CLONE)

Mở Terminal tại thư mục bạn muốn lưu bài học (ví dụ `D:\HocTap`) và gõ:

```bash
# 1. Đi vào thư mục học tập
cd D:\HocTap

# 2. Tải repository về máy
git clone https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung.git

# 3. Đi vào thư mục dự án vừa tải về
cd N07-HeThongQuanLyCuaHangThuCung
```

---

## BƯỚC 3: TẠO VÀ CHUYỂN SANG NHÁNH RIÊNG CỦA BẠN

Cú pháp chuẩn: `git checkout -b feature/<tên-nhánh-của-bạn> origin/develop`

Mỗi bạn tìm đúng tên mình và copy lệnh tương ứng:

- **Bạn Tô Thanh Đạt:**
  ```bash
  git checkout -b feature/kho-san-pham-lo-hang origin/develop
  ```
- **Bạn Võ Thị Hồng Ngọc:**
  ```bash
  git checkout -b feature/thu-cung-dich-vu-hotel origin/develop
  ```
- **Bạn Lê Anh Minh:**
  ```bash
  git checkout -b feature/nhan-su-giao-ca-dashboard origin/develop
  ```
- **Bạn Lê Khãi Minh (Leader):**
  ```bash
  git checkout -b feature/pos-ban-hang-doi-tra origin/develop
  ```

---

## BƯỚC 4: COPY CODE TỪ GÓI ĐƯỢC PHÂN VÀO DỰ ÁN

Leader (Khãi Minh) sẽ gửi cho bạn thư mục mã nguồn được phân công (hoặc bạn lấy trong thư mục `phan_chia_code_4_thanh_vien/`):
- Bạn copy các file Java của mình vào đúng các thư mục tương ứng trong `src/`:
  - Các file Entity $\rightarrow$ bỏ vào `src/models/entity/`
  - Các file DAO $\rightarrow$ bỏ vào `src/models/dao/`
  - Các file Controller $\rightarrow$ bỏ vào `src/controllers/`
  - Các file View / Dialog $\rightarrow$ bỏ vào `src/views/` (hoặc `src/views/charts/`)

---

## BƯỚC 5: HƯỚNG DẪN LỆNH COMMIT THEO TỪNG BẠN (CHIA LÀM 3 ĐỢT COMMIT)

Mỗi bạn chỉ cần copy-paste lần lượt 3 khối lệnh dưới đây trên Terminal của máy mình:

---

### 👨‍💻 DÀNH CHO BẠN: TÔ THANH ĐẠT (`feature/kho-san-pham-lo-hang`)

```bash
# === COMMIT 1: Thêm các Entity mô hình Kho & Sản phẩm ===
git add src/models/entity/DanhMucHang.java src/models/entity/DonViTinh.java src/models/entity/NhaCungCap.java src/models/entity/SanPham.java src/models/entity/QuyDoiDonVi.java src/models/entity/LoHang.java
git commit -m "feat(entity): create SanPham, LoHang, QuyDoiDonVi, DanhMucHang, NhaCungCap models"

# === COMMIT 2: Thêm các DAO truy vấn kho & lô hàng FEFO ===
git add src/models/dao/SanPhamDAO.java src/models/dao/LoHangDAO.java
git commit -m "feat(dao): implement SanPhamDAO and LoHangDAO for FEFO stock management"

# === COMMIT 3: Thêm giao diện Quản lý Kho & Quy đổi đơn vị ===
git add src/views/KhoLoHangPanel.java src/views/ThietLapQuyDoiDialog.java src/views/NhapSanPhamVaLoHangDialog.java
git commit -m "feat(view): add KhoLoHangPanel, ThietLapQuyDoiDialog and NhapSanPhamVaLoHangDialog"

# === ĐẨY LÊN GITHUB ===
git push -u origin feature/kho-san-pham-lo-hang
```

---

### 👩‍💻 DÀNH CHO BẠN: VÕ THỊ HỒNG NGỌC (`feature/thu-cung-dich-vu-hotel`)

```bash
# === COMMIT 1: Thêm các Entity Thú cưng, Sức khỏe & Dịch vụ ===
git add src/models/entity/ThuCung.java src/models/entity/HoSoThuCung.java src/models/entity/HoSoSucKhoe.java src/models/entity/DichVu.java src/models/entity/LichHen.java src/models/entity/ChiTietLichHen.java
git commit -m "feat(entity): add ThuCung, HoSoThuCung, HoSoSucKhoe, DichVu, LichHen models"

# === COMMIT 2: Thêm các DAO Dịch vụ, Lịch hẹn & Khách sạn ===
git add src/models/dao/ThuCungDAO.java src/models/dao/DichVuDAO.java src/models/dao/LichHenDAO.java src/models/dao/PhongLuuTruDAO.java
git commit -m "feat(dao): implement ThuCungDAO, DichVuDAO, LichHenDAO and PhongLuuTruDAO"

# === COMMIT 3: Thêm Controller & Giao diện Thú cưng / Spa / Hotel ===
git add src/controllers/ThuCungController.java src/views/ThuCungDichVuPanel.java src/views/PetHotelPanel.java src/views/QuanLyLichHenPanel.java src/views/DatLichHenDialog.java src/views/ChonDichVuDialog.java
git commit -m "feat(view): add ThuCungDichVuPanel, PetHotelPanel, QuanLyLichHenPanel and dialogs"

# === ĐẨY LÊN GITHUB ===
git push -u origin feature/thu-cung-dich-vu-hotel
```

---

### 👨‍💻 DÀNH CHO BẠN: LÊ ANH MINH (`feature/nhan-su-giao-ca-dashboard`)

```bash
# === COMMIT 1: Thêm các Entity Nhân sự, Phân quyền & Ca làm việc ===
git add src/models/entity/TaiKhoan.java src/models/entity/NhanVien.java src/models/entity/CaLamViec.java src/models/entity/BangPhanCong.java src/models/entity/BangGiaoCa.java
git commit -m "feat(entity): add TaiKhoan, NhanVien, CaLamViec, BangPhanCong, BangGiaoCa models"

# === COMMIT 2: Thêm các DAO & Controller xử lý đăng nhập, giao ca, phân ca ===
git add src/models/dao/TaiKhoanDAO.java src/models/dao/NhanVienDAO.java src/models/dao/PhanCaDAO.java src/models/dao/BangGiaoCaDAO.java src/controllers/DangNhapController.java src/controllers/NhanVienController.java src/controllers/PhanCaController.java src/controllers/GiaoCaController.java
git commit -m "feat(dao): implement TaiKhoanDAO, NhanVienDAO, PhanCaDAO, BangGiaoCaDAO and controllers"

# === COMMIT 3: Thêm giao diện Dashboard, Đăng nhập, Bàn giao ca và Charts ===
git add src/views/LoginForm.java src/views/MainDashboard.java src/views/QuanLyNhanVienPanel.java src/views/LichPhanCaPanel.java src/views/BanGiaoCaPanel.java src/views/TongQuanPanel.java src/views/BaoCaoThongKeDialog.java src/views/charts/BarChartPanel.java src/views/charts/DonutChartPanel.java
git commit -m "feat(view): implement MainDashboard, LoginForm, QuanLyNhanVienPanel, BanGiaoCaPanel and Charts"

# === ĐẨY LÊN GITHUB ===
git push -u origin feature/nhan-su-giao-ca-dashboard
```

---

### 👨‍💻 DÀNH CHO BẠN: LÊ KHÃI MINH (`feature/pos-ban-hang-doi-tra`)

```bash
# === COMMIT 1: Thêm các Entity Hóa đơn, Khách hàng & Đổi trả ===
git add src/models/entity/KhachHang.java src/models/entity/HoaDon.java src/models/entity/ChiTietHoaDon.java src/models/entity/ChiTietXuatLo.java src/models/entity/ChiTietDichVu.java src/models/entity/PhieuDoiTra.java src/models/entity/ChiTietDoiTra.java
git commit -m "feat(entity): add KhachHang, HoaDon, ChiTietHoaDon, ChiTietXuatLo, ChiTietDichVu, PhieuDoiTra models"

# === COMMIT 2: Thêm DAO Hóa đơn, Khách hàng & Controller Bán hàng ===
git add src/models/dao/KhachHangDAO.java src/models/dao/HoaDonDAO.java src/controllers/BanHangController.java
git commit -m "feat(dao): implement HoaDonDAO with atomic transaction, KhachHangDAO and BanHangController"

# === COMMIT 3: Thêm giao diện POS, Thanh toán VietQR, Cổng VNPay & Đổi trả ===
git add src/views/BanHangPOSPanel.java src/views/HoaDonPreviewDialog.java src/views/VietQRDialog.java src/views/VNPayDialog.java src/views/DoiTraHangPanel.java
git commit -m "feat(view): build BanHangPOSPanel, VietQRDialog, VNPayDialog, DoiTraHangPanel and invoice print preview"

# === ĐẨY LÊN GITHUB ===
git push -u origin feature/pos-ban-hang-doi-tra
```

---

## BƯỚC 6: TẠO PULL REQUEST (PR) TRÊN GIAO DIỆN GITHUB

Sau khi gõ lệnh `git push` xong:
1. Bạn mở trình duyệt vào link repository: [https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung](https://github.com/minhto1306/N07-HeThongQuanLyCuaHangThuCung).
2. Ngay trên đầu trang sẽ xuất hiện thanh màu vàng có tên nhánh của bạn $\rightarrow$ Bấm nút xanh lá **`Compare & pull request`**.
3. **Cực kỳ quan trọng ở phần chọn nhánh:**
   - **base:** chọn **`develop`** (KHÔNG CHỌN `main`).
   - **compare:** nhánh của bạn (ví dụ `feature/kho-san-pham-lo-hang`).
4. **Tiêu đề PR:** Viết rõ ràng (Ví dụ: `feat(kho): Tích hợp quản lý kho, lô hàng FEFO và quy đổi đơn vị`).
5. **Reviewers:** Nhìn sang cột bên phải mục **Reviewers**, bấm vào và tick chọn tên tài khoản các bạn còn lại trong nhóm để mời vào xem code.
6. Bấm nút **`Create pull request`**.

---

## BƯỚC 7: CÁC THÀNH VIÊN VÀO REVIEW & APPROVE (CHẤM ĐIỂM NHÓM CAO)

Khi thấy bạn mình gửi link PR:
1. Bạn mở link Pull Request đó ra.
2. Bấm vào thẻ **`Files changed`** ở phía trên để lướt xem các file bạn mình đã viết.
3. Bấm vào nút xanh lá **`Review changes`** ở góc trên bên phải:
   - Chọn mục **`Approve`** (Đồng ý).
   - Gõ một câu nhận xét ngắn (Ví dụ: *"Code chuẩn cấu trúc MVC, đã kiểm tra logic FEFO chạy tốt!"*).
   - Bấm nút **`Submit review`**.

---

## BƯỚC 8: LEADER BẤM MERGE CODE VÀO DEVELOP

Sau khi thấy các bạn đã vào Approve đầy đủ:
1. Trưởng nhóm (Khãi Minh) mở PR đó ra.
2. Cuộn xuống dưới cùng bấm nút **`Merge pull request`** $\rightarrow$ bấm tiếp **`Confirm merge`**.
3. Khi hiện chữ **Merged (màu tím)** là hoàn thành gộp code thành công vào nhánh chung `develop`!

---

## 🖥️ HƯỚNG DẪN THAO TÁC TRÊN ECLIPSE IDE (NẾU KHÔNG DÙNG DÒNG LỆNH)

Nếu bạn nào không quen gõ Terminal đen, có thể thao tác hoàn toàn bằng chuột trong Eclipse:

1. **Tạo nhánh:** Nhấp chuột phải vào tên dự án trong Eclipse $\rightarrow$ **Team** $\rightarrow$ **Switch To** $\rightarrow$ **New Branch...** $\rightarrow$ Đặt tên nhánh của bạn $\rightarrow$ Bấm **Finish**.
2. **Commit:** Nhấp chuột phải dự án $\rightarrow$ **Team** $\rightarrow$ **Commit...**
   - Cửa sổ **Git Staging** sẽ hiện ra.
   - Kéo các file muốn commit từ ô *Unstaged Changes* xuống ô *Staged Changes*.
   - Gõ lời nhắn commit vào ô *Commit Message*.
   - Bấm nút **Commit**.
3. **Push lên GitHub:** Nhấp chuột phải dự án $\rightarrow$ **Team** $\rightarrow$ **Push Branch 'feature/...'** $\rightarrow$ Bấm **Next** $\rightarrow$ Bấm **Finish**.

---
*Chúc cả nhóm phối hợp nhịp nhàng và đạt điểm tối đa môn Phát triển Ứng dụng!*
