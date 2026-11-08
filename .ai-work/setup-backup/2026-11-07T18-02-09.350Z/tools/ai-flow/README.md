# MIO AI Flow — Codex chỉ huy, Gemini thực thi

Bộ điều phối chạy local bằng Node.js 22+, không cần npm install, API key hoặc Git. Codex đọc project gốc và điều phối một Gemini Flash worker qua tài khoản Antigravity CLI.

## Cài đặt

Đã chuẩn bị installer dành riêng cho project MIO. Installer chỉ thêm công cụ, tài liệu và cấu hình MCP theo project; lưu backup khi cần cập nhật file setup. Không di chuyển hoặc sửa code Java, SQL, config ứng dụng hay Git.

```powershell
node .\install.mjs 'C:\Users\lekha\eclipse-workspace\N07-HeThongQuanLyCuaHangThuCungMio'
```

Sau cài đặt, mở thư mục project MIO trong Codex, cho phép project đáng tin cậy nếu ứng dụng hỏi và dùng phiên đã tải MCP `mio_antigravity`. Không giả định chat cũ tự có tool mới. Cấu hình nằm ở `.codex/config.toml`; không sửa cấu hình Codex toàn cục.

## Đăng nhập và kiểm tra model

Trong terminal project, chạy:

```powershell
node tools/ai-flow/src/cli.mjs doctor
```

Nếu CLI báo chưa đăng nhập, mở terminal của bạn và chạy `agy`, hoàn thành đăng nhập tài khoản Google ngay trong luồng Antigravity, rồi thoát CLI tương tác. Không gửi mật khẩu hoặc token vào chat.

Chạy lại doctor; lấy **exact Gemini Flash model ID** được liệt kê:

```powershell
node tools/ai-flow/src/cli.mjs set-model EXACT_FLASH_ID_FROM_DOCTOR
```

Model mặc định là `null` vì không được tự đoán ID hoặc chuyển provider. Lỗi quota/auth/model sẽ dừng worker, không gọi API trả phí. API-key/ADC/Vertex environment variables phổ biến bị loại khỏi tiến trình worker; không thay đổi cấu hình đăng nhập của bạn.

## Bài smoke thật bắt buộc

Nếu `agy models` dùng được trong CMD của bạn nhưng phiên công cụ báo chưa đăng nhập, mở `tools/ai-flow/Mo-Worker-Host.cmd` hoặc chạy `node tools/ai-flow/src/cli.mjs host` trong CMD đó. Giữ cửa sổ mở. Đây là host foreground dùng đăng nhập trong môi trường của bạn, không sao chép credential hoặc chạy dịch vụ nền. MCP tự dùng host khi có heartbeat. Ctrl+C dừng host và hủy worker.

Chế độ host dùng hàng đợi file cục bộ dưới `.ai-work/host/`, không mở cổng mạng và không lưu token nhà cung cấp. Chỉ tám thao tác MCP cố định được nhận, không nhận lệnh shell tùy ý. Request hết hạn/thuộc phiên host cũ không được phát lại. Không chỉnh sửa hoặc tạo request thủ công khi không hiểu phạm vi đã duyệt. CLI tương đương: `node tools/ai-flow/src/cli.mjs host-call worker_status`.

```powershell
node tools/ai-flow/src/cli.mjs approve tools/ai-flow/examples/setup-smoke-plan.json
node tools/ai-flow/src/cli.mjs dispatch setup-smoke-v1 setup-context-probe
node tools/ai-flow/src/cli.mjs result setup-context-probe
```

Codex phải review quan sát của probe, rồi gọi MCP `worker_review` với `decision=accept`, `evidence` thật và `proposalDigest` vừa đọc. CLI tương đương: lưu object review dưới `.ai-work/review.json`, chạy `node tools/ai-flow/src/cli.mjs review .ai-work/review.json`.

Sau đó dispatch `setup-document-smoke`, đọc diff và checks, review accept tương tự. Marker tài liệu là thay đổi duy nhất trong smoke. Cuối cùng:

```powershell
node tools/ai-flow/src/cli.mjs finish-smoke setup-context-probe setup-document-smoke
```

Không thực hiện nhiệm vụ sản phẩm trước khi preflight có `liveValidated=true`.

## Giao việc thật

Codex khảo sát code/tài liệu, lập kế hoạch và nhận duyệt của bạn. Dùng `examples/task-plan.template.json` làm cấu trúc. Gói task phải có `goal`, `design`, `askWhen`, `steps`, `acceptance`, `allowedFiles`, `contextFiles`, `checks`; dependencies tham chiếu task trong cùng plan, không có chu trình.

`worker_approve_plan` lưu revision bất biến và dấu xác nhận của cuộc trao đổi. Đây là nhật ký quy trình, không phải xác thực người dùng riêng biệt. Không ghi “đã duyệt” khi chưa được duyệt. Mỗi kế hoạch/task có ID duy nhất; không tái sử dụng ID cho phạm vi khác.

Các công cụ MCP:

| Công cụ | Chức năng |
|---|---|
| `worker_preflight` | Kiểm tra tài khoản/model hoặc chọn exact model được xác nhận |
| `worker_approve_plan` | Ghi kế hoạch đã duyệt và task bất biến |
| `worker_dispatch` | Giao nhiệm vụ, chụp baseline và backup; MCP trả ngay |
| `worker_status` | Xem trạng thái, không tự chạy lại |
| `worker_result` | Lấy đề xuất, diff, checks, proposalDigest |
| `worker_cancel` | Hủy worker trong đúng phiên sở hữu, kết thúc process tree |
| `worker_review` | Accept, retry với phản hồi, hoặc takeover |
| `worker_finish_smoke` | Mở khóa production sau hai probe đã accept |

CLI có thao tác tương đương, xem `node tools/ai-flow/src/cli.mjs help`. CLI dispatch đợi kết thúc; MCP dispatch chạy bất đồng bộ trong vòng đời server. Tắt phiên không tạo dịch vụ chạy nền.

## Kiểm tra và dữ liệu

- Check tự động: `java-compile` (Java 17, `lib/*`), `contains`, `json-parse`.
- `manual`: đòi Codex kiểm tra thực tế và ghi bằng chứng trong review; không tự đánh dấu passed.
- Cầu nối chạy checks trên bản candidate riêng; không chạy app, kết nối DB hay gửi SMTP.
- Lệnh kiểm tra compile code gốc: `node tools/ai-flow/src/cli.mjs compile`.
- Kiểm thử cầu nối: `node --test tools/ai-flow/test/*.test.mjs`.
- `.ai-work/tasks/ID/attempt-N/` lưu backup, hash, prompt đã lọc, đề xuất, diff, check log và journal apply. Không đưa thư mục này lên dịch vụ chia sẻ hoặc Git.
- Thư mục `config/`, credential stores, `.env` không được gửi cho worker. Không giao worker sửa file chứa bí mật; xử lý việc đó trực tiếp và có phạm vi riêng.
- Worker chỉ tạo JSON. File tools, shell, MCP, web và subagent bị deny bằng settings từng phiên; bất kỳ tool call được phát hiện đều làm run thất bại.
- Diff được tạo bằng so sánh văn bản, không chạy Git. Toàn bộ nội dung file thay thế được thể hiện để review rõ ràng.

## Lỗi và phục hồi

Task đã accept không chạy lại khi dispatch cùng ID. Blocked/cancelled/interrupted chỉ chạy lại khi dùng `resume PLAN_ID TASK_ID`. Resume lỗi hạ tầng không tính là lượt sửa nghiệp vụ. Phản hồi review retry có tối đa hai lượt sửa; sau đó trạng thái takeover yêu cầu Codex tiếp quản.

Khi source thay đổi trong lúc worker chạy hoặc trước apply, dừng với conflict. Khi apply lỗi giữa chừng, giữ journal và backup, không rollback mù. Phiên crash lúc `applying` cần đối chiếu journal trước khi tiếp tục. Kiểm tra hash không thay thế khóa bắt buộc của hệ điều hành: tránh sửa source từ Eclipse/Antigravity trong lúc apply.

## Nguồn kỹ thuật

- [OpenAI: MCP trong Codex](https://learn.chatgpt.com/docs/extend/mcp?surface=cli)
- [Antigravity: Headless mode](https://antigravity.google/docs/headless-mode/)
- [Antigravity: Permissions](https://antigravity.google/docs/permissions/)
- [Antigravity: Authenticate](https://antigravity.google/docs/install/)

Kết quả setup thực tế được ghi riêng trong `INSTALL_REPORT.json` và `VERIFICATION_REPORT.md`; không coi kiểm thử mock là bằng chứng worker thật đã chạy.
