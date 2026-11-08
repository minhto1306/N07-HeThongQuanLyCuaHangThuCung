# Phạm vi sửa lỗi được yêu cầu ngày 08/11/2026

Người dùng yêu cầu dùng AGY thật cho đọc code/ngữ cảnh và công việc đơn giản để giảm tải Codex, rồi sửa lỗi và push theo kế hoạch phân công sẵn trong AGY. Đây là chỉ thị mới thay thế giới hạn không dùng Git trong AGENTS/DECISIONS cho các thao tác Git cần thiết của đợt sửa này. Gemini worker vẫn không chạy Git, shell hoặc DB; Codex review, kiểm thử và thực hiện Git.

Phạm vi: sửa các lỗi được xác minh trong POST_PULL_CASHIER_AUDIT_224a140.md và kiểm thử hồi quy trên DB thử. Không đổi công nghệ/kiến trúc, không tự áp dụng migration lên DB hiện hữu, không gửi email hoặc chuyển tiền thật. Không sửa hoặc commit config/credentials, .ai-work, tài liệu AGY riêng hoặc binary build. Không tự đổi model/provider khi quota/auth lỗi.

Kế hoạch AGY xác nhận bốn chủ module và nhánh đang tồn tại:
- Lê Khãi Minh: feature/pos-ban-hang-doi-tra; POS, hóa đơn, đổi trả.
- Tô Thanh Đạt: feature/kho-san-pham-lo-hang; kho, sản phẩm, lô, quy đổi.
- Võ Thị Hồng Ngọc: feature/thu-cung-dich-vu-hotel; hồ sơ thú cưng, lịch, Spa, Hotel.
- Lê Anh Minh: feature/nhan-su-giao-ca-dashboard; auth, nhân sự, ca, báo cáo.

Commit theo thay đổi thực và module, với danh tính Git hiện cấu hình; không tự gán tác giả thay các thành viên. Dùng nhánh tích hợp riêng cho kiểm thử các thay đổi xuyên module, đẩy từng nhánh module và PR vào develop theo kế hoạch; không tự merge main hoặc làm lại lịch sử. Các sửa ở HoaDonDAO có phụ thuộc xuyên POS/Spa/Hotel phải ghi rõ trong PR.

Worker chỉ đề xuất file UTF-8 đầy đủ trong allowedFiles. Mọi test/compile do cầu nối/Codex thực hiện. Không tuyên bố đã chạy test trong quan sát worker.
