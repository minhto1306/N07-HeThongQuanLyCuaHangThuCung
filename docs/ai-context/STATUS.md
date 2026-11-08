# Trạng thái và độ tin cậy

Khảo sát setup: 08/11/2026, Asia/Bangkok. Mọi báo cáo cũ được coi là tuyên bố lịch sử cần xác minh.

| Chủ đề | Nguồn cũ | Bằng chứng hiện tại / cách xử lý |
|---|---|---|
| Hoàn thành, compile | Handover ghi “100% done”, có build.bat | Root chưa có build.bat; kết quả compile của phiên setup lưu riêng tại `.ai-work/baseline-compile/result.json` |
| CSDL | Handover nói 13/13 test đạt; audit nói DB offline | Chưa xác minh DB trong setup; không tự chạy JDBC hoặc migration |
| Khôi phục mật khẩu | Audit nói khóa quên mật khẩu giả; handover nói OTP/email preview | `LoginForm.java` có bước kiểm tra cấu hình SMTP; `EmailThongBaoHelper.java` có gửi SMTP thật; `DangNhapBus.java` dùng SecureRandom. Chưa kiểm thử gửi thư |
| Source gốc | Audit chỉ một đường dẫn nested khác | Người dùng xác nhận thư mục Eclipse MIO là source hiện hành; các phát biểu lịch sử không tự chuyển sang bản này |
| Git | Lịch sử có cả yêu cầu giữ Git và báo cáo Git | Chỉ thị hiện tại: không dùng Git/GitHub, không thao tác kho |
| Test plan | Có 49 lỗi trong danh mục kiểm thử | Đây là danh mục/kịch bản, không phải kết quả chạy mới và cũng không chứng minh mọi lỗi vẫn còn |
| Kết nối worker | Có agy CLI và chế độ JSON không tương tác | Cần `doctor` xác minh tài khoản và exact model; không suy ra từ giao diện đã đăng nhập |

Không thay đổi nghiệp vụ để “hòa giải” tài liệu trong lúc setup. Khi làm nhiệm vụ cụ thể, Codex phân biệt hành vi code đang có với hành vi người dùng muốn. Nếu lựa chọn nghiệp vụ còn mâu thuẫn, hỏi người dùng trước khi code.

Trạng thái vận hành trực tiếp: `.ai-work/preflight.json`. Lịch sử nhiệm vụ: `.ai-work/tasks/*/state.json`. Production dispatch bị khóa đến khi hai bài smoke thật được Codex review và chấp nhận.

## Audit sau pull 08/11/2026

Người dùng đã yêu cầu pull, sau đó cho phép tự tạo môi trường thử riêng và kiểm thử theo vai trò thu ngân. Đã pull main đến `224a140`, không commit/push. Xem `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` và bằng chứng `.ai-work/audit-224a140/`.

DB hiện hữu chỉ đọc: vẫn 27 bảng, thiếu mapping, DAO read 19/20 đạt. DB thử mới `MioAudit_224a140_20261108`: 28 bảng, hơn 5.000 sản phẩm/15.000 lô/20.000 hóa đơn giả, có dữ liệu cố ý thử lỗi. 25 điều kiện ứng dụng: 11 đạt/14 không đạt; stress 400 thanh toán 8 luồng đạt; bridge 27/27 đạt. Chưa sửa nguồn ứng dụng hay migration DB hiện hữu. Không dùng dữ liệu cố ý thử phá trong DB thử để kết luận DB hiện hữu có cùng lỗi dữ liệu. MAXDOP=1 chỉ thay trong DB thử để khảo sát chờ bộ nhớ; cấu hình SQL global/DB hiện hữu không đổi.
