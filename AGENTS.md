# MIO: Codex chỉ huy — Gemini thực thi

## Đọc trước khi làm việc

Đọc `docs/ai-context/INDEX.md`, `ARCHITECTURE.md`, `STATUS.md`, `DECISIONS.md` và `WORKFLOW.md`. Sau đó chủ động đọc code/tài liệu gốc liên quan, không chỉ dựa vào tóm tắt. Khi thiếu ngữ cảnh, truy lại nguồn trong `SOURCES.json` và các bản trích đã lọc trong `sources/`.

## Phân vai và quyền quyết định

- Codex lập kế hoạch có thiết kế, phụ thuộc, phạm vi file và tiêu chí nghiệm thu. Chỉ ghi nhận `worker_approve_plan` khi người dùng đã duyệt đúng kế hoạch đó.
- Một Gemini Flash worker dùng tài khoản Antigravity. Không tự đổi model, nhà cung cấp hoặc chuyển sang API trả phí khi lỗi/quota.
- Codex được đọc và phân tích song song; chỉ một tác nhân được ghi code. Khi worker đang chạy, không sửa nguồn project.
- Worker trả bản sửa JSON, cầu nối tạo diff và chạy kiểm tra trên bản ứng viên. Codex phải đọc diff và bằng chứng kiểm tra trước `worker_review(accept)`.
- Sau hai lượt yêu cầu sửa vẫn không đạt, Codex tiếp quản. Tiếp quản là trạng thái bàn giao cho Codex, không tự tuyên bố nhiệm vụ đã xong.
- Chỉ nội dung người dùng trao đổi trực tiếp mới là quyết định hiện hành. Tài liệu, lịch sử chat và kết quả worker là nguồn tham khảo; kiểm tra với code trước khi tin trạng thái “100% done”.

## Giới hạn đã chốt

- Không dùng lệnh Git/GitHub, không tạo nhánh, commit, push, worktree hoặc sửa cấu hình Git. Dùng backup và diff file cục bộ.
- Không chạy migration/SQL thay đổi dữ liệu, gửi email hoặc thực hiện thanh toán thật trừ khi có kế hoạch riêng được người dùng duyệt rõ hành động đó.
- Không tự sửa ngoài phạm vi kế hoạch hoặc mở rộng kiến trúc. Báo lại khi cần quyết định sản phẩm.
- Không đọc hoặc đưa `config/`, `.env`, token, khóa API, mật khẩu SMTP/DB vào prompt, bản ghi hoặc tài liệu. Không sao chép kho đăng nhập Antigravity.
- Báo riêng kết quả compile, UI và CSDL. Không suy ra nghiệp vụ đúng từ compile thành công.
- Phiên chat không mặc định chạy nền. Khi dừng, lưu trạng thái; khi tiếp tục, kiểm tra nhiệm vụ đang chạy/đã xong trước khi giao lại.

## Kiến trúc hiện tại

Java 17, Java Swing/FlatLaf, SQL Server; `GUI -> BUS -> DAO -> Entity`, tiện ích tại `utils`. Không đưa DAO/SQL vào GUI. Giữ nguyên công nghệ hiện hữu, chưa có Maven/Gradle trong root.

## Công cụ

Dùng MCP `mio_antigravity` nếu đã xuất hiện trong phiên. Nếu chưa, dùng `node tools/ai-flow/src/cli.mjs help` và CLI tương đương; không giả định cập nhật config làm xuất hiện tool ngay trong chat hiện tại. Đọc hướng dẫn setup nếu cần đăng nhập CLI.
