# Nhiệm vụ thật: kiểm toán và sửa MIO với AGY + Codex song song

Bạn là **AGY Main, người điều phối thực thi**. Codex đã đặt kiến trúc công việc bên dưới. Hãy bắt đầu trên project `C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio`. Người dùng cần tìm và sửa sớm lỗi Java, SQL, giao diện và nghiệp vụ, kể cả lỗi họ chưa nêu; ưu tiên trải nghiệm thu ngân và vận hành thật. Đây là nhiệm vụ MIO, không phải bài thử `order_service.py`.

## Nguồn sự thật và hiện trạng

Đọc `AGENTS.md`, `docs/ai-context/AGY_HANDOVER_20261108.md`, `docs/ai-context/REPAIR_AUTHORIZATION_20261108.md`, `docs/test_plan/RE_AUDIT_2026-11-08.md`, `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md`, rồi đối chiếu mã nguồn và SQL hiện tại. Báo cáo cũ ở baseline `224a140` không đồng nghĩa lỗi vẫn còn trong working tree. Working tree đang có bản sửa chưa commit; giữ nguyên, không reset hoặc ghi đè. Bộ regression gần nhất 25/25 trên DB thử chỉ chứng minh các ca đã kiểm, chưa phải kiểm toán toàn hệ thống. AGY không có toàn bộ lịch sử chat Eclipse; đừng tuyên bố đã đọc được.

## Kiến trúc phối hợp bắt buộc

1. AGY Main dùng `invoke_subagent` tạo các worker rà soát **song song**. Worker được tự dùng công cụ/skill phù hợp trong AGY. Phân mảng: (A) POS, hóa đơn, giá, mã vạch, đổi trả; (B) kho, sản phẩm, lô, SQL mapping; (C) lịch hẹn, thú cưng, Spa, Hotel, hàng đợi dịch vụ; (D) nhân sự, phân quyền, ca, dashboard/báo cáo. Gửi task đủ ngữ cảnh, đường dẫn và tiêu chí; không bắt worker đoán chính sách nghiệp vụ.
2. Trong giai đoạn rà soát, worker **chỉ đọc**. Mỗi worker gửi checkpoint sớm: những đường dữ liệu đã truy, lỗi có bằng chứng, giả thuyết cần thử, file có thể sửa. AGY Main tổng hợp thành danh sách P0/P1/P2 và đối chiếu chéo Java -> DAO -> entity -> SQL. Đặt câu hỏi “có quá nhiều dữ liệu/thao tác” theo vai: thu ngân mới, thu ngân quét mã liên tục, thu ngân hay thao tác nhầm, hai quầy đồng thời, lễ tân lịch hẹn, quản lý dịch vụ và quản lý báo cáo.
3. **Ưu tiên hạn mức:** AGY/Gemini tự đọc toàn bộ nguồn liên quan, lập danh sách lỗi, đối chiếu SQL, chạy thử, viết bản sửa, tự phản biện giữa các worker và sửa lại. Dùng khả năng Teamwork/Boost/skill của AGY nếu thật sự khả dụng. Không gọi Codex để đọc tài liệu, tóm tắt, viết test, sửa lỗi đơn giản, kiểm tra từng file hoặc nhắc tiến độ. AGY Main chỉ gọi **Codex CLI read-only** cho (a) một quyết định kiến trúc hoặc xung đột nghiệp vụ có ảnh hưởng nhiều phân hệ, (b) một review giữa chừng của gói P0/P1 đầu tiên để chứng minh vòng trao đổi tự động, và (c) một review tích hợp cuối cùng. Ngoài các điểm này, chỉ gọi Codex khi Gemini đã tự kiểm tra mà vẫn còn bằng chứng mâu thuẫn/rủi ro nghiêm trọng. Gom nhiều câu hỏi liên quan vào một lời gọi ngắn; tránh lặp lại toàn bộ source hoặc lịch sử chat. Dùng `codex exec` đã thử hoạt động; kiểm tra `--help` cho cú pháp chính xác. Không dùng `codex review --uncommitted` trước khi tự xác minh.
4. AGY Main **tự dùng `send_message`** chuyển nhận xét Codex vào đúng worker khi worker đang `running` hoặc `idle` chờ feedback; xác nhận worker đã nhận. Không bắt người dùng copy/paste nhận xét. Cho worker sửa tiếp; Codex review lại các thay đổi quan trọng trong khi worker khác tiếp tục việc độc lập.
5. Chỉ cho phép **một writer trên mỗi file**. Rà soát có thể chạy song song; trước khi sửa, AGY Main phân file/owner rõ. File dùng chung như `HoaDonDAO.java`, schema và các entity hợp đồng phải chốt giao diện/điều kiện trước rồi mới giao một worker sửa. Không ghi đè thay đổi của người khác, không sửa `config/` hoặc credential.

## Hợp đồng nghiệp vụ phải kiểm tra

- Giá bán và chi tiết hóa đơn: giá nguồn ở đâu, snapshot đơn giá/thành tiền lúc bán, hóa đơn tính và lưu tổng thế nào, hàng thiếu giá xử lý thế nào. Quy tắc cho hàng thiếu giá cần đề xuất và xin quyết định, không tự bịa.
- POS: quét mã vạch là đường nhanh, mã không tồn tại/mã trùng, nhiều sản phẩm, thao tác bàn phím, phản hồi UI khi truy vấn chậm, giỏ hàng lớn và thanh toán lỗi giữa chừng.
- Kho/đổi trả: FEFO, hạn dùng, lô/quy đổi, chống âm kho, hai quầy cùng trả/xuất, rollback, hoàn tiền và điểm khách.
- Lịch hẹn/dịch vụ: trễ giờ, vắng mặt, hủy/dời lịch, tranh chấp tài nguyên, quá nhiều đơn đang chờ, ai phụ trách và khả năng lọc/sắp xếp. Chính sách no-show là quyết định mở; đưa phương án và tác động.
- Dashboard/báo cáo: lấy số liệu thật từ DB, đồng nhất công thức doanh thu/chi phí/lợi nhuận với hóa đơn và trả hàng, ngày cuối kỳ, dữ liệu lớn.
- SQL: bảng/cột/khóa/ràng buộc đúng với tất cả DAO; thứ tự migration, dữ liệu cũ, transaction và đóng connection. Không áp migration lên DB đang dùng; chỉ DB thử cô lập.

## Trình tự thực hiện

**Pha 1, kiểm toán nhanh:** Gemini workers trả từng phát hiện với mã, mức độ, file:method/dòng, bước tái hiện, ảnh hưởng thật, bằng chứng code/DB/UI, phân loại `đã chứng minh` / `cần thử` / `cần quyết định nghiệp vụ`. AGY Main cho các Gemini worker phản biện chéo, loại lỗi đã sửa, xếp P0/P1 trước và tự chốt việc độc lập. Chỉ chuyển Codex câu hỏi kiến trúc hoặc xung đột nhiều phân hệ thật sự còn mở.

**Pha 2, sửa có kiểm soát:** Giao các lỗi độc lập cho Gemini workers theo chủ module đã nêu trong `REPAIR_AUTHORIZATION_20261108.md`; mỗi gói có file được sửa, invariant, điều kiện nghiệm thu. Worker gửi checkpoint sau thiết kế và sau diff đầu tiên cho AGY Main; Gemini tự review thường xuyên. Với gói P0/P1 đầu tiên, AGY Main gọi Codex một lần khi worker còn chạy, dùng `send_message` chuyển nhận xét vào đúng worker và tiếp tục. Các gói độc lập còn lại chạy song song bằng AGY, không gọi Codex theo chu kỳ. Các ca giao nhau POS/Spa/Hotel/SQL phải tích hợp tuần tự.

**Pha 3, xác minh:** Biên dịch Java 17; chạy regression liên quan trên DB thử riêng; thử lỗi rollback và hai quầy đồng thời; thử thao tác UI như thu ngân; đối chiếu dashboard với truy vấn DB. Báo rõ test nào chạy thật, test nào chưa. Không chạm DB sản xuất, không gửi email/thanh toán thật.

**Pha 4, bàn giao:** Trả bảng lỗi đã sửa/chưa sửa, diff, test evidence, quyết định nghiệp vụ còn mở và thứ tự xem lại cho người dùng. Giữ riêng thay đổi theo module và chủ nhánh đã định; không mạo danh thành viên khi commit. Git/push chỉ sau khi Codex review tích hợp và có bằng chứng kiểm tra.

## Điều kiện thành công của cơ chế phối hợp

Trong **một task P0/P1 MIO thật**, phải có bằng chứng: AGY worker đang chạy; Codex CLI được gọi khi worker chưa kết thúc; output Codex được AGY Main gửi bằng `send_message` vào đúng worker; worker xác nhận nhận và cập nhật diff. Ghi timestamp, worker ID và đường dẫn log/checkpoint. AGY/Gemini tự xử lý các vòng còn lại; Codex review tích hợp cuối cùng. Nếu bất kỳ bước nào không tự động được, báo chính xác bước hỏng, dừng cơ chế đó và tiếp tục việc rà soát độc lập; không tuyên bố hai AI đã giao tiếp tự động.

Bắt đầu ngay bằng Pha 1, tạo worker thật và gửi checkpoint đầu tiên; đừng mở thêm bài demo.
