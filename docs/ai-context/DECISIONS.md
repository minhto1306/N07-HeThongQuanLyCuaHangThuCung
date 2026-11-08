# Quyết định hiện hành

1. Codex là planner/reviewer chính, mở cùng thư mục project hiện tại; không di chuyển code.
2. Gemini Flash worker sử dụng tài khoản Antigravity, một worker tại một thời điểm. Exact model ID phải được CLI xác nhận, không đoán từ tên hiển thị.
3. Người dùng duyệt kế hoạch tổng thể, sau đó thực hiện tự động trong phạm vi đó. Không cần hỏi lại từng nhiệm vụ đã thuộc kế hoạch.
4. Codex đọc các bản bàn giao/checkpoint và code để tổng hợp; không chọn mù một chat làm nguồn sự thật.
5. Sau hai lượt sửa không đạt, chuyển việc cho Codex. Các lỗi kết nối/quota không tự chuyển sang API hoặc model khác.
6. Không Git/GitHub; không commit/branch/worktree/push hay thay đổi cấu hình Git.
7. Không migration trên DB hiện hữu mặc định. Compile, UI, DB là ba kết quả riêng.
8. Bản đầu dùng worker tạo đề xuất JSON có phạm vi file; công cụ áp dụng sau review và kiểm tra dấu file. Worker không được gọi shell, file tools, MCP hay web. Việc đọc/kiểm tra file và chạy compile do cầu nối làm.
9. Một phần ngữ cảnh Antigravity được trích vào tài liệu chung; không hứa tự chuyển toàn bộ lịch sử hoặc bộ nhớ của sản phẩm.
10. Quyền phê duyệt kế hoạch là quy trình giữa người dùng và Codex; trường `approval` là dấu vết trao đổi, không phải cơ chế xác thực độc lập. Không dùng cầu nối như dịch vụ nhiều người dùng.
