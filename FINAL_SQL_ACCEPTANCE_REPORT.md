# PETSHOP MIO — FINAL CORRECTION & PRE-PUSH VERIFICATION

Ngày kiểm tra: 08/11/2026. Phạm vi: chuẩn bị, **không commit/push và không chạy seed/migration trên database thật**.

## A. Lỗi đã phát hiện và xử lý

| Mức độ | Lỗi | Cách xử lý | Kết quả |
|---|---|---|---|
| Critical | Báo cáo/form công khai hoặc điền sẵn mật khẩu demo | Xóa mật khẩu khỏi báo cáo và các form; tài khoản demo phụ bị khóa mặc định | PASS tĩnh |
| Critical | README và `AUDIT_REPORT.md` cũ còn công khai tài khoản/mật khẩu mẫu | Xóa thông tin đăng nhập dùng chung; hướng dẫn bootstrap và cấp mật khẩu riêng | PASS quét secret |
| Critical | Tạo/reset mật khẩu dùng SHA-256 hoặc fallback `123456` | `DangNhapBus`, `NhanVienBus`, `NhanVienDAO` chỉ ghi PBKDF2 và từ chối mật khẩu yếu/trống | PASS compile/unit |
| Critical | Bootstrap ghi đè NV001; reset CLI thiếu xác thực | Bootstrap từ chối tài khoản tồn tại; reset yêu cầu xác thực NVQL hoạt động | PASS compile; DB NOT_VERIFIABLE |
| Critical | SQL tự CREATE/USE database, DEMO chỉ dựa tên DB và guard nằm sau DDL | Bỏ CREATE/USE; preflight trước DDL; bắt xác nhận đúng `DB_NAME`, đủ 30 bảng, tên Test/Dev chính xác, token fresh-only và DB trống | PASS tĩnh; runtime NOT_VERIFIABLE |
| Critical | Seed DELETE rồi insert không transaction | Bỏ DELETE; từ chối DB có dữ liệu; thêm XACT_ABORT, TRY/CATCH, TRANSACTION/ROLLBACK | PASS tĩnh |
| High | Java cho `maQuyDoi=null`, SQL bắt NOT NULL | Cho phép `ChiTietXuatLo.maQuyDoi` nullable | PASS tĩnh |
| High | SQL `KHONG_HOAN` khác Java | Chuẩn hóa `KHONG_HOAN_TIEN` | PASS tĩnh |
| High | Sinh mã fallback SELECT MAX | `MaTuSinhHelper` fail-closed nếu BangCapSo không cấp số nguyên tử | PASS compile |
| High | Giao ca biến lỗi DB thành doanh thu 0 | `GiaoCaBus` ném lỗi và dừng chốt ca | PASS compile |
| High | Test có thể ghi database cấu hình hiện tại | Bắt `PETSHOP_TEST_DATABASE=QuanLyCuaHangThuCung_Test` và kiểm tra catalog trước live test | PASS chốt an toàn |

## B. Bảng đối chiếu dữ liệu

Không có `COUNT(*)` hợp lệ trong đợt này vì chưa được cấp database Test cô lập. Không tái sử dụng số liệu báo cáo cũ.

| Nhóm | Mục tiêu | Thực tế xác minh | Trạng thái |
|---|---:|---|---|
| SanPham, KhachHang, ThuCung, LichHen | 50/bảng | Chưa chạy SQL Test | NOT_VERIFIABLE |
| HoaDon | Khoảng 50 | Chưa chạy SQL Test | NOT_VERIFIABLE |
| ChiTietHoaDon | 90–140 | Kiểm tra tĩnh có 109 mã duy nhất; chưa COUNT DB | NOT_VERIFIABLE |
| PhieuDoiTra | 2–3 hợp lý | Script khai báo 2 | NOT_VERIFIABLE |
| FEFO/Spa Pass/Pet Hotel/BangGiaoCa | Đồng bộ | Chưa chạy DB | NOT_VERIFIABLE |

Kiểm tra tĩnh trong script ghi nhận 50 mã `SP`, 50 `KH`, 50 `TC`, 50 `LH`, 50 `HD`, 109 `CTHD`, 2 `DT` và 24 `GC`. Đây không thay thế `COUNT(*)` sau khi chạy trên database Test.

## C. Kết quả kiểm thử

| Bộ kiểm thử | PASS | FAIL | NOT_VERIFIABLE |
|---|---:|---:|---:|
| Java 17 compile `-Xlint` | 95 file | 0 | 0 |
| Phase 1 | 20 | 0 | 3 |
| Phase 2 tài chính | 14 | 0 | 4 |
| PBKDF2 thuần | 4 | 0 | 1 nhóm DB |

Một lần chạy trước khi bổ sung guard đã kết nối database cấu hình hiện tại và gọi `sinhMaKhachHang()`, làm tăng cấp số `KH` trong `BangCapSo` một lần. Không tự ý hoàn tác vì chưa xác định database đích và giảm sequence có thể tạo trùng mã. Kết quả lần chạy đó bị loại khỏi bằng chứng.

## D. Bảo mật

- Không còn mật khẩu demo plaintext trong báo cáo hiện tại hoặc giá trị mặc định trên form.
- Mật khẩu mới chỉ ghi PBKDF2; SHA-256 cũ chỉ được đọc để nâng cấp.
- Bootstrap nhập ẩn, xác nhận hai lần, kiểm tra độ mạnh, xóa `char[]`; reset CLI xác thực NVQL.
- `config/*.local.properties` được `.gitignore` bảo vệ; các config tracked chỉ chứa placeholder/trống.

## E. SQL Safety

- File gộp không còn CREATE/USE/DROP database hay DELETE seed; preflight chạy trước DDL/DML.
- Phải xác nhận `XacNhanDatabase=DB_NAME()` cùng session.
- SCHEMA_ONLY chỉ chạy trên database chưa có bảng người dùng. DEMO yêu cầu schema chuẩn đủ 30 bảng, đúng DB Test/Dev quy định, có token, NV001 bootstrap và dữ liệu nghiệp vụ trống.
- Seed có transaction/rollback. Cú pháp/runtime SQL Server Test vẫn NOT_VERIFIABLE.

## F. Tương thích Java

Đã đối chiếu các lớp đăng nhập/nhân viên, `HoaDonDAO`, `LoHangDAO`, `BangGiaoCaDAO`, `TheSpaPassDAO`, `GoiSpaPassDAO`, các BUS tài chính/đổi trả/giao ca và schema liên quan. Java 17 biên dịch thành công 95 file. GUI end-to-end, SQL transaction/concurrency, FEFO thật, RBAC thực tế, Spa Pass và Pet Hotel chưa được chạy trên DB Test.

## G. Git

Working tree còn nhiều file Phase 1–2 modified/untracked; không commit/push. `git diff --check` không phát hiện lỗi whitespace. Script cũ giữ trong `sql/archive/`. `.gitignore` bảo vệ config local và output export; file local, log, backup DB và output test không được đưa vào commit. Quét source/config/tài liệu hiện hành không còn mật khẩu demo plaintext; OTP `123456` trong `VNPayDialog` chỉ là dữ liệu sandbox của cổng thử nghiệm, không phải tài khoản hệ thống.

## Kết luận

**NOT_READY_FOR_PUSH**

Blocker: chưa có `QuanLyCuaHangThuCung_Test` được xác nhận để chạy SQL, lấy COUNT(*), kiểm tra đăng nhập/RBAC và 6 bài transaction/concurrency. Phải hoàn tất các kiểm thử này rồi mới được đổi thành READY_FOR_PUSH.
