# Checkpoint sửa lỗi Pet Shop Mio

Ngày cập nhật: 07/11/2026 (Asia/Saigon)

## Phạm vi và nguyên tắc

- Source đang sửa trực tiếp: `N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main`.
- Kiến trúc giữ theo 3 lớp GUI -> BUS -> DAO -> Entity.
- Chưa push Git, chưa commit, không yêu cầu import lại Eclipse.
- CSDL SQL Server trên máy hiện chưa kết nối được, nên mới xác minh compile; chưa chạy migration lên dữ liệu thật.

## Đã sửa và đã compile Java 17 thành công

1. Xác thực/RBAC:
   - Bỏ `--dashboard` và tài khoản Quản lý mặc định.
   - `UserSession` không còn mặc định NVQL khi chưa đăng nhập.
   - Bỏ so sánh mật khẩu thô với hash; OTP dùng `SecureRandom`.
   - Khóa quên mật khẩu giả vì dự án chưa có SMTP thật.
   - Khóa nút cập nhật Kho với tài khoản chỉ có quyền xem; giới hạn cập nhật hồ sơ sức khỏe/khách hàng theo vai trò.

2. VNPay/VietQR:
   - VNPay chỉ thành công khi chữ ký, mã hóa đơn, số tiền và response code đều đúng.
   - Bỏ F9/nút xác nhận VNPay thủ công.
   - Sửa múi giờ VNPay sang `Asia/Ho_Chi_Minh`, so sánh chữ ký constant-time.
   - Bỏ QR offline giả; VietQR thủ công phải xác nhận đã đối chiếu app ngân hàng.
   - Đồng bộ số tài khoản QR trên hóa đơn.

3. Phân ca/bàn giao ca:
   - Đồng bộ đủ CA01..CA04 và đúng khung giờ.
   - Không tự seed phân công giả khi DB lỗi/rỗng.
   - Thu ngân chỉ chốt ca được phân công; NVQL phải xác nhận khi trực quầy thay.
   - Tiền đầu ca chỉ kế thừa từ biên bản `DA_DUYET`; bỏ lịch sử giao ca giả.
   - Lưu `gioBatDauCa`; doanh thu/hoàn trả lọc đúng khoảng ca và đúng mã thu ngân.
   - Thêm migration chống trùng phân công và giao ca theo nhân viên/ngày/ca.

4. Sinh mã/nhập kho:
   - Bỏ full-table scan và lỗi tràn `Integer`; SQL lấy `MAX` phần số.
   - Không sinh mã giả khi DB mất kết nối.
   - Danh mục, ĐVT, NCC map theo ID thật thay vì `DM01/DVT01/NCC01`.
   - Nhập sản phẩm + quy đổi + lô hàng chạy trong một transaction; lỗi thì rollback và không báo thành công giả.

5. Hồ sơ thú cưng/lịch hẹn:
   - Tra cứu khách/thú cưng bằng dữ liệu DB thay vì SĐT hardcode.
   - Khi tên bé đã thuộc đúng khách, lịch mới dùng lại mã thú cưng cũ để giữ bệnh án/dị ứng.

6. Đổi trả:
   - Giá hoàn phân bổ theo tiền thực trả sau giảm giá.
   - Quá 7 ngày bắt buộc đúng tài khoản NVQL.
   - Lưu số lô và số lượng cơ sở hoàn; hoàn đúng lô trong cùng transaction lưu phiếu.
   - Không tự đổi lô hết tồn thành trạng thái hạn dùng “an toàn”.

7. Pet Hotel/Spa Pass:
   - Phòng Hotel chỉ giải phóng sau khi hóa đơn POS commit; lịch sử gắn `maHoaDon`.
   - Spa Pass không tự seed/phát hành/nạp miễn phí; phải bán qua POS.
   - Trừ lượt và ghi `LichSuSuDungSpaPass` trong một transaction.

8. Báo cáo XNT:
   - Tồn cuối kỳ quá khứ được quay ngược theo nhập/xuất/hoàn sau ngày kết thúc, không lấy thẳng tồn hôm nay.

9. POS đổi hàng và email:
   - DTO đổi trả giữ `soLo` và `soLuongCoSoHoan`, không làm rơi thông tin lô khi chuyển sang POS.
   - Phiếu trả, hoàn đúng lô cũ, hóa đơn mới và trừ lô mới được commit/rollback trong cùng một transaction.
   - Bỏ cộng tồn RAM thủ công sau đổi hàng để tránh hoàn hai lần hoặc hoàn sai lô; giao diện đồng bộ lại từ DB.
   - Không còn mặc định nhân viên `NV001` khi phiên đăng nhập POS hết hạn.
   - Email chưa có SMTP sẽ báo rõ chưa gửi, không mở preview rồi ghi nhận gửi thành công giả.

10. Spa Pass và dữ liệu phiên:
   - Trừ lượt/kích hoạt Spa Pass đã gộp vào transaction hóa đơn; hóa đơn lỗi thì thẻ không đổi và ngược lại.
   - Thẻ mới bắt buộc có SĐT thật, lấy đúng giá gói trên giỏ và lưu `maHoaDonKichHoat`; bỏ khách mẫu `0988111222`.
   - Chặn phát hành, nạp hoặc trừ lượt Spa Pass trực tiếp ngoài luồng thanh toán POS.
   - Bỏ các fallback ngầm `NV001/NV002`, số điện thoại mẫu và vai trò Thu ngân khi phiên/vai trò không hợp lệ.
   - Dọn định dạng bất thường của `BangGiaoCaDAO.java`; `git diff --check` hiện sạch.

11. Pet Hotel:
   - Hóa đơn Hotel, lịch sử lưu trú và chuyển phòng sang `ĐANG DỌN DẸP` đã nằm trong cùng transaction POS.
   - Bỏ transaction checkout chạy sau hóa đơn; sau commit chỉ tải lại phòng từ DB để cập nhật giao diện.

12. Điểm khách hàng và xuất báo cáo:
   - Cộng điểm hóa đơn, trừ điểm đã dùng và thu hồi điểm do trả hàng chạy trong cùng transaction POS; có kiểm tra xung đột số dư.
   - Bỏ cập nhật đè tổng điểm ở transaction sau hóa đơn.
   - CSV báo cáo/ROP escape dấu nháy và chặn công thức Excel từ dữ liệu bắt đầu bằng `=`, `+`, `-`, `@`.
   - Tên sheet XLSX được lọc ký tự cấm và giới hạn 31 ký tự.

## File SQL nâng cấp

- `sql/CapNhatRangBuocNghiepVu.sql`: chạy một lần trên DB hiện hữu sau khi sao lưu.
- Script cố ý dừng nếu phát hiện dữ liệu trùng; không tự xóa lịch sử.
- Cần test trên bản sao DB trước khi chạy DB chính.

## Trạng thái kiểm thử

- Lần compile gần nhất: thành công, toàn bộ `src/**/*.java`, Java 17, classpath `lib/*`.
- `DBConnection.main`: thất bại do SQL Server `QuanLyCuaHangThuCung` hiện không kết nối được.
- Chưa thể chạy integration test/migration vì DB offline.

## Việc tiếp tục ngay sau checkpoint

1. Rà và sửa các mutation còn thiếu RBAC trong Pet Hotel, Đổi trả, CRM.
2. Rà SQL XNT với dữ liệu thật sau migration, đặc biệt kiểm kê điều chỉnh kho.
3. Rà soft-delete, báo cáo/CSV, hóa đơn/K80, race condition sinh mã.
4. Khi SQL hoạt động: sao lưu DB -> chạy migration trên bản sao -> test đăng nhập, POS, đổi trả, giao ca, Hotel, Spa Pass, XNT.

## Ghi chú Git

- Không push.
- Trước commit cần dùng `git status`, `git diff --check`, compile lại và liệt kê toàn bộ file thay đổi.
