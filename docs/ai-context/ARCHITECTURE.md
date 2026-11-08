# Kiến trúc xác minh qua file

- Java 17: `.classpath` dùng JavaSE-17. Project Eclipse, mã nguồn `src/`, đầu ra IDE `bin/`.
- Các package đang tồn tại: `entity`, `dao`, `bus`, `gui`, `utils`.
- `.classpath` liệt kê FlatLaf 3.5.4 và Microsoft SQL Server JDBC 12.8.1; lệnh kiểm tra mới dùng toàn bộ `lib/*`.
- Giao diện Swing, điểm vào có `src/gui/LoginForm.java`; đăng nhập qua `src/bus/DangNhapBus.java`, phiên/quyền trong `src/utils/UserSession.java`.
- JDBC trong `src/utils/DBConnection.java`, lấy cấu hình từ `config/database.properties`. Không sao chép nội dung cấu hình vào ngữ cảnh AI.
- Email qua `src/utils/EmailThongBaoHelper.java`; code có đường gửi SMTP thật. Không gọi thử vì có thể gửi thư.
- Các nhóm nghiệp vụ: POS/đổi trả, kho/lô/FEFO, khách hàng/thú cưng/Spa/Hotel, nhân sự/giao ca, báo cáo và xác thực/RBAC.
- SQL gồm script hệ thống và `sql/CapNhatRangBuocNghiepVu.sql`. Chưa thực thi trong quá trình setup flow.
- Không tìm thấy `pom.xml`, Gradle, `build.bat` hoặc `run.bat` tại root lúc khảo sát. Dùng lệnh compile của cầu nối; không tin lệnh cũ chỉ vì xuất hiện trong handover.

Quy tắc thiết kế từ kế hoạch Antigravity: GUI gọi BUS, BUS điều phối nghiệp vụ/transaction, DAO chứa SQL, Entity mô tả dữ liệu. Khi thay đổi một chức năng, đọc cả chuỗi liên quan và schema; không giả định mọi code đã hoàn toàn tuân thủ kiến trúc.
