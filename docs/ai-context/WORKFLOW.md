# Quy trình vận hành

## A. Chuẩn bị một lần

1. Mở project MIO trong Codex. Đọc chỉ mục và đối chiếu nguồn Antigravity với code.
2. Kiểm tra `worker_preflight` hoặc CLI `doctor`. Nếu cần, người dùng đăng nhập Antigravity CLI tương tác. Không yêu cầu gửi mật khẩu/token qua chat.
3. Chọn exact Gemini Flash ID từ danh sách CLI; cấu hình qua `set-model`/`worker_preflight(model=...)`. Không tự thay model khi model không khả dụng.
4. Ghi nhận kế hoạch setup smoke trong `tools/ai-flow/examples/setup-smoke-plan.json`. Người dùng đã yêu cầu thực hiện flow bao gồm kiểm thử này.
5. Giao `setup-context-probe`, đọc quan sát, review với bằng chứng thực tế. Chỉ chấp nhận khi kiến trúc/ngữ cảnh đúng.
6. Giao `setup-document-smoke`, xem diff và check marker, chấp nhận. Chỉ file marker tài liệu được ghi.
7. `worker_finish_smoke` với hai ID để mở khóa giao việc thật. Nếu thất bại, giữ khóa và sửa nguyên nhân.

## B. Mỗi yêu cầu sản phẩm

1. Codex đọc code/schema/test plan liên quan. Ghi yêu cầu, ràng buộc, chỗ chưa rõ; hỏi các quyết định sản phẩm còn thiếu.
2. Lập kế hoạch: mục tiêu, phạm vi, thiết kế, phụ thuộc, nhiệm vụ, file được sửa, tiêu chí và loại kiểm tra. Bạn duyệt kế hoạch.
3. `worker_approve_plan`: lưu các task cụ thể bất biến. Sửa phạm vi cần kế hoạch revision mới và được duyệt.
4. `worker_dispatch`: chụp hash và backup file; gửi ngữ cảnh được chọn cho Gemini. Công cụ không cắt ngữ cảnh ngầm.
5. Trong lúc Gemini chạy, Codex đọc/phân tích việc tiếp theo, không ghi nguồn. Dùng status để xem tiến độ; hủy qua đúng phiên MCP sở hữu task.
6. `worker_result`: đọc đề xuất và diff. Kiểm tra tự động chạy trên candidate; kết quả worker tự khai không phải bằng chứng chạy test.
7. Codex review nghiệp vụ, đọc lại file gốc và tự kiểm tra các điểm cần thiết. Gửi `worker_review` với exact proposalDigest.
8. `accept` chỉ áp dụng khi không có thay đổi đồng thời và check tự động đạt; manual check phải có bằng chứng trong review. `retry` cần lỗi và cách tái hiện cụ thể; sau hai repair chuyển `takeover`.
9. Codex tiếp quản bằng cách đọc đề xuất/backup, tự sửa phạm vi đã duyệt, tự kiểm tra; trạng thái takeover không đồng nghĩa done.
10. Sau từng task đã accept, giao task phụ thuộc tiếp theo. Cuối cùng kiểm tra tích hợp, cập nhật trạng thái và bàn giao.

## C. Ngắt phiên và lỗi

- Mất mạng/quota/auth: lưu blocked và nguyên nhân, vô hiệu readiness khi phù hợp; không tự gọi lại hoặc trả phí API.
- Phiên điều phối đóng: hủy worker sở hữu tiến trình. Crash để lại running được trình bày thành interrupted khi owner không còn; chỉ resume rõ ràng mới chạy lại.
- `applying` còn dở: kiểm tra `apply-journal.json` và backup. Không tự rollback nếu có thể ghi đè thay đổi của người dùng.
- Xung đột file: dừng accept, đọc thay đổi mới, tạo lại nhiệm vụ trên baseline mới. Không ghi đè mù.
- Nhiệm vụ đã accepted: dispatch lại cùng ID trả trạng thái, không chạy model lại.
- Không chạy nền sau khi Codex dừng. CLI dispatch chạy chờ hoàn tất; MCP dispatch trả ngay và tiếp tục trong vòng đời phiên MCP.

## D. Giới hạn kỹ thuật

Cầu nối kiểm tra hash trước mỗi lần thay file, lưu backup/diff và nhật ký ghi. Một trình soạn thảo bên ngoài vẫn có thể ghi vào thời điểm giữa kiểm tra và rename; đây không phải khóa filesystem bắt buộc cho mọi ứng dụng. Tránh sửa nguồn đồng thời trong Eclipse/Antigravity khi đang áp dụng task.

Worker bị giới hạn bằng settings quyền Antigravity, không phải sandbox hệ điều hành do cầu nối tự cung cấp. Các tool call bất ngờ làm run bị từ chối. Phải kiểm chứng chế độ quyền trong bài live smoke trước khi dùng thật.
