# Chỉ mục ngữ cảnh MIO

Project nguồn: `C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio`.

Mục tiêu của bộ tài liệu này là cho planner chủ động truy xuất nguồn, không giới hạn planner ở bản tóm tắt của worker.

| Cần hiểu | Đọc |
|---|---|
| Kiến trúc và điểm vào code | `ARCHITECTURE.md`, `.classpath`, `src/gui/LoginForm.java`, BUS/DAO tương ứng |
| Nghiệp vụ mong muốn | README, `sources/*implementation_plan*`, `docs/test_plan/` |
| Trạng thái và mâu thuẫn | `STATUS.md`, `AUDIT_FIX_PROGRESS.md`, nguồn handover/checkpoint |
| Quyết định đang áp dụng | `DECISIONS.md` và yêu cầu hiện hành của người dùng |
| Vận hành hai model | `WORKFLOW.md`, `tools/ai-flow/README.md` |
| Truy nguyên thông tin | `SOURCES.json`, `sources/` |

Mỗi lần nhận yêu cầu, chọn và đọc đầy đủ tài liệu/module liên quan, ghi điều chưa biết. Đừng gửi toàn bộ project vào một prompt. Cầu nối từ chối khi vượt ngân sách ngữ cảnh, không cắt file ngầm. Worker được nhận nguyên nội dung những file được chọn (sau lọc bí mật); nếu thiếu, phải trả `blockers` để Codex đọc thêm và bổ sung nhiệm vụ.

`sources/` chứa bản trích tài liệu và thông điệp trao đổi liên quan đã lọc, không phải bản xuất đầy đủ mọi cuộc chat. Nguồn có thể cũ hoặc mô tả một bản sao project khác; luôn kiểm tra đường dẫn và code thực tế.
