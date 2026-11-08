# BẢN BÀN GIAO NGỮ CẢNH DỰ ÁN AGY & MA TRẬN KIỂM TOÁN HỆ THỐNG MIO
**Ngày lập:** 08/11/2026  
**Dự án:** N07 - Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (`N07-HeThongQuanLyCuaHangThuCungMio`)  
**Mục đích:** Bàn giao hiện trạng mã nguồn, cơ sở dữ liệu, các phát hiện đã xác minh thực tế và phân định phạm vi kiểm toán toàn diện phục vụ phân rã nhiệm vụ cho đợt sửa lỗi kế tiếp.

---

## 1. Tuyên Bố Giới Hạn Truy Cập Hội Thoại (Conversation Access Limitation)

- **Hiện trạng truy cập:** Worker thực thi hiện hành xác nhận **không có quyền truy cập toàn bộ lịch sử hội thoại (full chat transcript)** của các phiên làm việc Antigravity hay Project Eclipse trước đây ngoài các trích đoạn văn bản được chọn lọc tại `docs/ai-context/sources/2c6fd9a7-message-excerpts.json` và bảng chỉ mục `docs/ai-context/SOURCES.json`.
- **Nguyên tắc đối soát ngữ cảnh:** Mọi thông tin trong tài liệu này được phân định rõ ràng giữa:
  1. *Trích đoạn hội thoại (Conversation Excerpts):* Các ghi nhận trao đổi cục bộ đã được xuất lọc.
  2. *Artifact lưu trữ (Saved AGY Artifacts):* Các tài liệu tổng kết trước đây như `docs/ai-context/sources/2c6fd9a7-handover_state.md`, `docs/ai-context/sources/2c6fd9a7-PROJECT_CHECKPOINT_MIO_PETSHOP.md`, `docs/ai-context/sources/2c6fd9a7-implementation_plan.md`.
  3. *Quan sát đã kiểm chứng (Verified Observations):* Bằng chứng thực nghiệm thu được từ audit mã nguồn Java và CSDL thực tế tại `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` và `docs/test_plan/RE_AUDIT_2026-11-08.md`.
  4. *Suy luận kỹ thuật (Inference):* Các đánh giá rủi ro và khuyến nghị kiến trúc mở chờ quyết định nghiệp vụ.
- **Cảnh báo tính xác thực:** Không giả định các tuyên bố "100% hoàn thành" trong tài liệu cũ là sự thật hiển nhiên nếu chưa đối chiếu trực tiếp với mã nguồn đang hiện hữu.

---

## 2. Mục Tiêu Dự Án & Các Quy Tắc Đã Thống Nhất (Goals & Accepted Rules)

- **Mục tiêu sản phẩm:** Xây dựng phần mềm quản lý máy trạm (desktop) phục vụ vận hành chuỗi cửa hàng thú cưng Mio Pet Shop, bao gồm: Điểm bán lẻ POS (sản phẩm, dịch vụ, quét mã vạch), Đổi trả hàng, Quản lý kho hàng theo lô và hạn dùng (FEFO), Dịch vụ chăm sóc thú cưng (Spa, Grooming, Thẻ Spa Pass), Lưu trú thú cưng (Pet Hotel), Quản lý ca trực nhân sự và Báo cáo thống kê tài chính.
- **Quy tắc phối hợp hai tác nhân (Codex – Gemini):**
  - *Phân vai:* Codex đảm nhiệm vị trí chỉ huy, lập kế hoạch chi tiết, review diff và nghiệm thu; Gemini Flash đóng vai trò worker thực thi từng bước nguyên tử trong phạm vi tệp cho phép (`allowedFiles`).
  - *Quyền quyết định hiện hành:* Chỉ những chỉ thị trực tiếp từ người dùng và bản kế hoạch đã được phê duyệt mới có hiệu lực áp dụng (`docs/ai-context/DECISIONS.md`, `AGENTS.md`).
  - *Kỷ luật Git:* Tuyệt đối không can thiệp Git, không tạo nhánh, không commit/push từ phía Gemini worker. Chỉ thị ngày 08/11/2026 cho phép Codex thực hiện các thao tác Git cần thiết theo phân công nhóm (`docs/ai-context/REPAIR_AUTHORIZATION_20261108.md`).
  - *An toàn môi trường:* Không chạy migration/DML làm thay đổi cơ sở dữ liệu đang vận hành khi chưa có kế hoạch kiểm thử độc lập; không gửi email SMTP thật, không gọi cổng thanh toán thật, không đưa thông tin nhạy cảm (credentials, connection string) vào ngữ cảnh AI.

---

## 3. Kiến Trúc Hệ Thống & Cơ Sở Dữ Liệu (Architecture & Database)

- **Môi trường kỹ thuật:** Java 17 (JavaSE-17), FlatLaf 3.5.4, Microsoft SQL Server qua thư viện JDBC Driver 12.8.1 (`docs/ai-context/ARCHITECTURE.md`). Cấu hình dự án Eclipse chuẩn tại thư mục gốc, không sử dụng Maven hay Gradle.
- **Kiến trúc phân tầng N-Tier:**
  - `src/entity/`: Chứa các POJO ánh xạ thực thể dữ liệu (`SanPham`, `LoHang`, `QuyDoiDonVi`, `HoaDon`, `ChiTietHoaDon`, `LichHen`, `TheSpaPass`,...).
  - `src/dao/`: Quản lý 100% các câu truy vấn SQL, thao tác `PreparedStatement`, giao dịch JDBC và ánh xạ bản ghi. Tuyệt đối không chứa logic tính toán tiền tệ hay định dạng giao diện.
  - `src/bus/`: Điều phối luồng nghiệp vụ trung gian, tính toán tiền tệ, tích lũy điểm thưởng và kiểm tra quyền hạn trước khi gọi DAO.
  - `src/gui/` & `src/gui/charts/`: Giao diện người dùng FlatLaf Swing. Tuân thủ nguyên tắc cách ly: GUI chỉ giao tiếp qua BUS, không gọi trực tiếp DAO hay `DBConnection`.
  - `src/utils/`: Các lớp tiện ích chung như `DBConnection`, `PasswordHasher`, `MaTuSinhHelper`, `UserSession`, `EmailThongBaoHelper`.
- **Kiến trúc Cơ sở dữ liệu:**
  - Cơ sở dữ liệu Microsoft SQL Server bao gồm 28 bảng theo thiết kế chuẩn (`sql/HeThongQuanLyCuaHangThuCung.sql` và `sql/CapNhatRangBuocNghiepVu.sql`).
  - *Hiện trạng lệch pha lược đồ (Schema Mismatch):* DB thực tế đang dùng thiếu 1 bảng (`LichSuSuDungSpaPass`) và 7 cột tại các bảng hiện hữu (`gioBatDauCa`, `ngayGiaoCa` tại `BangGiaoCa`; `soLo`, `soLuongCoSoHoan` tại `ChiTietDoiTra`; `maHoaDon`, `trangThaiThanhToan` tại `LichSuLuuTru`; `maHoaDonKichHoat` tại `TheSpaPass`) (`docs/test_plan/RE_AUDIT_2026-11-08.md`).

---

## 4. Lịch Sử Phát Triển, Các Phát Hiện Đã Xác Minh & Hiện Trạng Sửa Lỗi

### 4.1. Tuyên bố lịch sử so với thực tế mã nguồn
- Trong các artifact cũ (`docs/ai-context/sources/2c6fd9a7-handover_state.md`), hệ thống được tuyên bố đã hoàn thành 100% qua 6 phase và vượt qua 13/13 bài test. Tuy nhiên, đợt tái kiểm tra ngày 08/11/2026 sau khi pull mã nguồn `224a140` xác nhận nhiều điểm chưa hoàn thiện hoặc chỉ đạt ở mức cục bộ:
  - File `build.bat` không tồn tại ở root dự án hiện hữu (`docs/ai-context/STATUS.md`).
  - Báo cáo kiểm thử ứng dụng trên môi trường thử nghiệm `MioAudit_224a140_20261108` ghi nhận **11 điều kiện ĐẠT / 14 điều kiện KHÔNG ĐẠT** trên tổng số 25 kịch bản (`docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md`).

### 4.2. Các phát hiện trọng yếu đã xác minh thực nghiệm (Verified Findings)
1. **Khôi phục mật khẩu bỏ qua kiểm tra Email (A01 - P0):** `src/bus/DangNhapBus.java` cho phép người dùng chỉ cần nhập đúng SĐT là được chấp nhận dù Email nhập sai; `src/gui/LoginForm.java` lấy email người nhập để gửi OTP, tạo lỗ hổng chiếm đoạt tài khoản khi SMTP hoạt động.
2. **Lỗi khóa ngoại khi bán tách lô quy đổi (A02 - P1):** Khi tách lô không chẵn kiện/thùng, nhánh `qdXuat = null` truyền giá trị null vào cột bắt buộc `ChiTietXuatLo.maQuyDoi`, gây lỗi SQL 515 và rollback hóa đơn (`src/gui/BanHangPOSPanel.java`, `src/dao/HoaDonDAO.java`).
3. **Đua giao dịch trả hàng đồng thời (A03 - P1):** `HoaDonDAO.luuPhieuDoiTraTrongTransaction` không tái kiểm tra tổng số lượng đã trả trước đó trong transaction, cho phép hai quầy cùng trả hàng vượt quá số lượng mua gốc.
4. **Sai lệch số ngày lưu trú Pet Hotel (A04 - P1):** POS lưu số ngày vào chuỗi ĐVT nhưng số lượng đặt là 1; DAO lấy số lượng làm số ngày khiến hóa đơn 2 ngày bị lưu thành 1 ngày trong `LichSuLuuTru`.
5. **Xung đột Unique khóa ngoại nhiều phòng một hóa đơn (A05 - P1):** Ràng buộc `UQ_LichSuLuuTru_HoaDon` gây lỗi SQL 2601 khi thanh toán từ 2 phòng trở lên trên cùng một hóa đơn.
6. **Hụt trừ số buổi thẻ Spa Pass (A06 - P1):** Chi tiết dịch vụ có số lượng từ 2 trở lên nhưng `HoaDonDAO.xuLySpaPassTrongTransaction` chỉ tăng `soBuoiDaDung` thêm 1.
7. **Xuất bán lô hết hạn ở tầng DAO (A07 - P1):** GUI đã bổ sung lọc FEFO, nhưng câu lệnh UPDATE trừ kho tại tầng DAO không kiểm tra hạn dùng, dẫn đến API vẫn ghi nhận xuất hàng hết hạn.
8. **Bảo vệ tham số số lượng và tổng tiền ở DAO (A08, A09 - P1):** DAO thiếu lớp phòng thủ (defensive check), cho phép truyền số lượng âm làm tăng tồn kho bất thường hoặc tổng tiền hóa đơn không khớp tổng chi tiết.
9. **Ghi đè điểm tích lũy ngoài giao dịch (A10 - P1):** Tầng BUS/DAO cập nhật điểm tuyệt đối qua hàm lưu hồ sơ khách hàng mà không kiểm tra quyền, dẫn đến nguy cơ ghi đè điểm tích lũy.
10. **Trùng mã tự sinh giữa hai tiến trình JVM (A11 - P1):** `src/utils/MaTuSinhHelper.java` dùng cơ chế synchronized trong một tiến trình Java; khi chạy hai quầy độc lập, cả hai cùng đọc giá trị MAX và sinh trùng mã.
11. **Rò rỉ kết nối JDBC (A14 - P1):** Các hàm ghi trong `src/dao/NhanVienDAO.java`, `src/dao/ThuCungDAO.java`, `src/dao/SanPhamDAO.java`, `src/dao/LichHenDAO.java` thiếu `conn.close()` trong khối `finally`.
12. **Báo cáo Top bán chạy và Xuất-Nhập-Tồn (A12, A13 - P1):** Báo cáo Top bán chạy cộng dồn số lượng mà không nhân tỷ lệ quy đổi về đơn vị cơ sở; báo cáo XNT tính nhầm cả số lượng hoàn hàng hỏng vào lượng nhập lại kho.

---

## 5. Khối Lượng Công Việc Còn Lại (Remaining Work)

Khối lượng công việc cần triển khai được phân rã theo thứ tự ưu tiên kỹ thuật:
1. **Khắc phục lỗ hổng xác thực:** Sửa quy trình khôi phục mật khẩu tại `src/bus/DangNhapBus.java` và `src/gui/LoginForm.java`, bắt buộc đối soát đồng thời SĐT và Email từ CSDL.
2. **Đồng bộ Lược đồ CSDL:** Chuẩn bị kịch bản migration có kiểm thử trên môi trường staging; bổ sung bảng `LichSuSuDungSpaPass` và 7 cột thiếu; điều chỉnh ràng buộc `UNIQUE` trên `LichSuLuuTru.maHoaDon` thành chỉ mục phù hợp cho phép một hóa đơn chứa nhiều phòng.
3. **Củng cố toàn vẹn giao dịch POS & Trả hàng:** Đóng gói nguyên tử việc tạo phiếu đổi trả và hóa đơn mua mới; kiểm tra số lượng còn lại được phép trả ngay trong transaction có khóa dòng; sửa logic tách lô gán đúng mã quy đổi cơ sở.
4. **Sửa đổi nghiệp vụ Dịch vụ & Khách sạn:** Tính đúng số ngày lưu trú Pet Hotel theo ngày thực tế hoặc cấu hình thỏa thuận; trừ đúng số lượt Spa Pass theo số lượng dịch vụ thực tế trong giỏ hàng.
5. **Đóng tài nguyên JDBC:** Rà soát toàn bộ các lớp DAO, chuyển đổi sang cú pháp `try-with-resources` hoặc bảo đảm `conn.close()` được thực thi trong khối `finally`.
6. **Chuẩn hóa báo cáo thống kê:** Sửa logic XNT loại trừ hàng hỏng không nhập kho; chuẩn hóa đơn vị tính về đơn vị cơ sở trong Top bán chạy; xử lý mốc thời gian cuối ngày `< denNgay.plusDays(1)`.
7. **Tối ưu trải nghiệm thu ngân & Hiệu năng Swing:** Đưa các truy vấn tải dữ liệu lớn ra khỏi luồng xử lý giao diện (Swing Event Dispatch Thread); bổ sung phân trang máy chủ cho lịch sử hóa đơn.

---

## 6. Phân Công 4 Chủ Module & Nhánh Làm Việc (4 Owners & Branches)

Căn cứ theo kế hoạch phân chia trách nhiệm nhóm (`docs/ai-context/REPAIR_AUTHORIZATION_20261108.md`, `docs/ai-context/sources/agy-KE_HOACH_PHAN_CHIA_GITHUB_NHOM07.md`), 4 nhánh nghiệp vụ được phân công cụ thể như sau:

| STT | Thành viên phụ trách | Nhánh Git quy định | Phân hệ & Phạm vi mã nguồn chịu trách nhiệm |
|:---:|---|---|---|
| 1 | **Lê Khãi Minh** | `feature/pos-ban-hang-doi-tra` | Điểm bán lẻ POS, Giỏ hàng, Quét mã vạch, Thanh toán, Lưu tạm đơn, Hóa đơn, Đổi trả hàng (`BanHangPOSPanel`, `DoiTraHangPanel`, `HoaDonDAO`, `BanHangBus`, `DoiTraBus`). |
| 2 | **Tô Thanh Đạt** | `feature/kho-san-pham-lo-hang` | Danh mục sản phẩm, Đơn vị tính, Quy đổi đơn vị, Nhập kho, Quản lý lô hàng theo FEFO, Kiểm kê (`KhoLoHangPanel`, `LoHangDAO`, `SanPhamDAO`, `QuyDoiDonViDAO`, `KhoLoHangBus`). |
| 3 | **Võ Thị Hồng Ngọc** | `feature/thu-cung-dich-vu-hotel` | Hồ sơ khách hàng, Hồ sơ thú cưng, Tiếp nhận & Điều phối lịch hẹn Spa, Thẻ Spa Pass, Khách sạn thú cưng Pet Hotel (`ThuCungDichVuPanel`, `QuanLyLichHenPanel`, `PetHotelPanel`, `TheSpaPassDAO`, `LichHenDAO`, `PetHotelBus`). |
| 4 | **Lê Anh Minh** | `feature/nhan-su-giao-ca-dashboard` | Đăng nhập, Xác thực, Khôi phục mật khẩu, Phân quyền RBAC, Quản lý nhân viên, Phân ca trực, Bàn giao ca thu ngân, Dashboard KPI và Báo cáo thống kê (`LoginForm`, `QuanLyNhanVienPanel`, `BanGiaoCaPanel`, `TongQuanPanel`, `BaoCaoThongKeDAO`, `DangNhapBus`, `ThongKeBus`). |

*Lưu ý phối hợp:* Các sửa đổi tại `src/dao/HoaDonDAO.java` liên quan xuyên suốt giữa POS, Spa Pass và Pet Hotel phải được ghi chú rõ ràng trong mô tả Pull Request tích hợp vào nhánh `develop`.

---

## 7. Bằng Chứng Kiểm Thử & Hiện Trạng Môi Trường (Test Evidence & Environments)

### 7.1. Phân biệt hai môi trường CSDL
- **Cơ sở dữ liệu hiện hữu (Production/Local Config):** Được đọc từ `config/database.properties`. Hiện có 27 bảng, thiếu 1 bảng và 7 cột nghiệp vụ mới. Kiểm tra chỉ đọc (20 hàm DAO) ghi nhận 19 hàm chạy được, 1 hàm báo lỗi SQL 207 tại báo cáo XNT (`docs/test_plan/RE_AUDIT_2026-11-08.md`).
- **Cơ sở dữ liệu thử nghiệm (`MioAudit_224a140_20261108`):** Được thiết lập riêng biệt theo yêu cầu người dùng tại `.ai-work/audit-224a140/`. Đã chạy migration đầy đủ 28 bảng, nạp hơn 5.000 sản phẩm, 15.000 lô hàng, 20.000 hóa đơn mẫu và các bộ dữ liệu biên để kiểm tra lỗi (`docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md`).

### 7.2. Kết quả kiểm thử hiệu năng & Tải đồng thời
- **Tải thanh toán đồng thời:** Đạt 400 giao dịch thanh toán trên 8 luồng song song trong 54,16 giây; số lượng tồn kho giảm chính xác 400 đơn vị.
- **Tranh chấp tồn kho:** Mô phỏng 2 quầy cùng mua 8 sản phẩm trên lô hàng chỉ còn tồn 10; kết quả 1 quầy thành công, 1 quầy bị chặn an toàn, tồn kho còn đúng 2 sản phẩm.
- **Tắc nghẽn giao diện (EDT Stall):** Khi nạp dữ liệu lớn không phân trang, ghi nhận luồng giao diện Swing bị đơ trong khoảng thời gian lên tới **3.212 ms** (`.ai-work/audit-224a140/sandbox-app/ui-large-output.txt`).
- **Điều chỉnh SQL Server:** Trên DB thử nghiệm, việc áp dụng `MAXDOP=1` giúp giảm hiện tượng chờ cấp phát bộ nhớ (`RESOURCE_SEMAPHORE`). Tuy nhiên đây là kết quả đo lường cục bộ, không tự ý áp dụng lên DB chính mà cần theo dõi plan truy vấn thực tế.

---

## 8. Mâu Thuẫn Tài Liệu & Các Quyết Định Nghiệp Vụ Mở (Contradictions & Open Decisions)

### 8.1. Các điểm mâu thuẫn tài liệu cần lưu ý
- *Trạng thái hoàn thành:* Báo cáo cũ ghi nhận hệ thống đạt 100% không lỗi; đối chiếu thực tế mã nguồn cho thấy tồn tại lỗi logic và thiếu sót schema như đã phân tích.
- *Cơ chế sinh mã:* UDF trong SQL sử dụng cơ chế sinh mã 2 chữ số (dễ tràn sau 99 đơn), trong khi ứng dụng đã chuyển một phần sang dùng `MaTuSinhHelper` trong Java; tuy nhiên Java helper lại chưa chống được va chạm giữa nhiều máy trạm độc lập.
- *Công thức lợi nhuận:* Tài liệu nghiệp vụ yêu cầu tính lợi nhuận thực tế theo giá vốn lô hàng, nhưng trên giao diện `TongQuanPanel` vẫn còn dòng code ước tính cố định 42% doanh thu.

### 8.2. Các bài toán thiết kế nghiệp vụ mở cần xem xét (Open Design Questions)
1. **Quy trình xử lý khách vắng mặt / trễ hẹn (No-Show Workflow):**
   - *Bối cảnh:* Người dùng yêu cầu khảo sát luồng xử lý khi khách đặt lịch dịch vụ hoặc phòng khách sạn nhưng không đến đúng giờ.
   - *Các phương án thiết kế cần thẩm định:* Cần đánh giá xem nên bổ sung các trạng thái mới (chẳng hạn như trạng thái chờ xác nhận, trễ hẹn, hoặc hủy do vắng mặt) hay áp dụng thời gian gia hạn (grace period 15-30 phút). Việc tự động giải phóng chỗ hoặc hủy ca phải được đặt dưới quyền xác nhận của Quản lý hay cho phép Thu ngân thực hiện; chính sách này **chưa được chốt chính thức** và cần trao đổi thêm với giảng viên/chủ cửa hàng.
2. **Xử lý mặt hàng chưa có giá niêm yết (Absent Price Handling):**
   - *Bối cảnh:* Xuất hiện rủi ro khi sản phẩm trong kho chưa được thiết lập giá bán niêm yết chuẩn hoặc giá bằng 0 đ.
   - *Đề xuất kỹ thuật an toàn (Proposed Safe Rule):* Đề xuất áp dụng nguyên tắc chặn an toàn (fail-closed) – từ chối thêm vào giỏ hàng hoặc từ chối thanh toán nếu `giaBan <= 0`, đồng thời hiển thị cảnh báo yêu cầu Quản lý thiết lập giá. Đây là **đề xuất bảo vệ an toàn chờ xem xét**, chưa phải là yêu cầu nghiệp vụ đã được phê chuẩn.
3. **Chính sách phụ thu quá giờ Pet Hotel:** Cần làm rõ quy định làm tròn giờ và mức phí phạt khi khách đón thú cưng trễ so với dự kiến check-out ban đầu.

---

## 9. Ma Trận Kiểm Toán Toàn Diện Hệ Thống (Complete-System Audit Matrix)

Bảng ma trận tổng hợp các lĩnh vực kiểm toán toàn diện hệ thống, phân loại theo hiện trạng xác minh và định hướng cho các nhiệm vụ tiếp theo:

| Lĩnh vực kiểm toán | Trọng tâm kiểm tra kỹ thuật | Hiện trạng xác minh mã nguồn | Phân loại & Rủi ro chính | Đề xuất giải pháp kỹ thuật | Nguồn dẫn chứng |
|---|---|---|---|---|---|
| **1. UI (Giao diện người dùng)** | Hiện tượng treo đơ luồng giao diện Swing (EDT); phím tắt F2/F9; cắt cụt văn bản trên màn hình nhỏ. | Ghi nhận EDT stall 3.212 ms khi tải dữ liệu lớn; nhãn F9 trên nút thanh toán chưa bind phím thật. | **Xác minh thực nghiệm:** Trải nghiệm quầy chậm, gây khó khăn khi phục vụ giờ cao điểm. | Đưa truy vấn sang `SwingWorker`; bind KeyStroke F9; thêm tooltip hiển thị tên hàng đầy đủ. | `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` mục 4 |
| **2. Business Logic (Nghiệp vụ)** | Tính toán tiền hàng, chiết khấu, tích/dùng điểm, tính số buổi Spa Pass và số ngày lưu trú Pet Hotel. | Sai số ngày Hotel (lưu 1 thay vì 2); Spa Pass chỉ trừ 1 lượt dù mua số lượng 2; tính điểm vãng lai trên preview. | **Xác minh thực nghiệm:** Sai lệch quyền lợi khách hàng và doanh thu cửa hàng. | Chuẩn hóa `HoaDonDAO` nhận đúng số lượng làm số ngày Hotel và số buổi Spa Pass; ẩn điểm với khách vãng lai. | `src/dao/HoaDonDAO.java`, `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` mục 1 |
| **3. SQL Mapping & Data Integrity** | Ánh xạ cột/bảng giữa Java và SQL; ràng buộc NOT NULL, UNIQUE, CHECK và khóa ngoại. | DB hiện hữu thiếu 1 bảng và 7 cột; vi phạm UNIQUE khi thanh toán nhiều phòng một hóa đơn. | **Xác minh thực nghiệm:** Lỗi SQL 207, 208, 515, 2601 phát sinh khi chạy giao dịch. | Ban hành kịch bản migration đồng bộ schema; chuyển `UQ_LichSuLuuTru_HoaDon` sang quan hệ 1-N hợp lệ. | `docs/test_plan/RE_AUDIT_2026-11-08.md` mục 1, `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` mục 2 |
| **4. Concurrency (Đồng thời)** | Tranh chấp tồn kho giữa 2 quầy; sinh mã hóa đơn đồng thời; hoàn trả hàng trùng lặp trên cùng đơn. | Hai JVM sinh trùng mã tự sinh; 2 quầy cùng trả 1 món thành công; tranh chấp trừ tồn kho đạt an toàn. | **Xác minh thực nghiệm:** Nguy cơ trùng khóa chính hoặc ghi nhận hoàn trả vượt quá thực tế. | Sinh mã bằng sequence/transaction khóa tại CSDL; kiểm tra số dư còn được phép trả trong transaction. | `src/utils/MaTuSinhHelper.java`, `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` mục 1 |
| **5. Scale & Performance (Quy mô)** | Tải dữ liệu trên 20.000 hóa đơn, 5.000 sản phẩm; cấp phát bộ nhớ SQL Server và rò rỉ socket JDBC. | Chờ RESOURCE_SEMAPHORE khi chưa tinh chỉnh; 19 kết nối bị giữ mở sau 20 lần cập nhật nhân viên. | **Xác minh thực nghiệm:** Cạn kiệt connection pool, đơ ứng dụng khi vận hành dài ngày. | Đóng kết nối bằng `try-with-resources`; phân trang truy vấn SQL; tối ưu chỉ mục tìm kiếm. | `src/dao/NhanVienDAO.java`, `docs/test_plan/POST_PULL_CASHIER_AUDIT_224a140.md` mục 5 |
| **6. Cashier Operations (Vận hành quầy)** | Luồng thanh toán tiền mặt/chuyển khoản; tìm kiếm đơn tạm; kiểm tra ca trực khi mở quầy và chốt ca. | Thanh toán nhanh hoạt động tốt; đơn tạm lưu được; cảnh báo ca trực bị báo trễ lúc bấm Chốt ca. | **Xác minh thực nghiệm:** Thu ngân thao tác nhập liệu xong mới phát hiện không có quyền chốt ca. | Kiểm tra phân ca ngay khi mở form bàn giao; tối ưu ô tìm kiếm đơn tạm theo số điện thoại. | `src/gui/BanHangPOSPanel.java`, `src/gui/BanGiaoCaPanel.java` |
| **7. Appointments & Services** | Đặt lịch KTV; quản lý tiến độ làm dịch vụ; xử lý khách vắng mặt (no-show); liên kết thanh toán POS. | POS nạp dịch vụ chưa giữ ID lịch hẹn; chưa có cơ chế đánh dấu ca trễ hẹn/vắng mặt chính thức. | **Vấn đề thiết kế mở:** Nguy cơ ùn tắc KTV hoặc sai lệch trạng thái lịch hẹn khi khách không đến. | Thẩm định các phương án xử lý vắng mặt; liên kết cập nhật trạng thái `Đã hoàn thành` sau khi POS thanh toán. | `src/gui/QuanLyLichHenPanel.java`, `docs/ai-context/sources/2c6fd9a7-implementation_plan.md` |
| **8. Reports (Báo cáo & Thống kê)** | Độ chính xác báo cáo doanh thu, Top bán chạy, Xuất-Nhập-Tồn và đối soát tiền két ca trực. | XNT tính nhầm hàng hỏng vào hoàn kho; Top bán chạy bỏ qua quy đổi ĐVT; lọc ngày bỏ sót giây cuối. | **Xác minh thực nghiệm:** Số liệu kế toán và kiểm kê kho lịch sử bị sai lệch. | Sửa công thức XNT tách biệt hàng hoàn tái nhập; quy đổi số lượng bán về đơn vị cơ sở; dùng `< plusDays(1)`. | `src/dao/BaoCaoThongKeDAO.java`, `src/bus/ThongKeBus.java` |
| **9. Security & RBAC (Bảo mật)** | Xác thực đăng nhập; phân quyền vai trò (NVQL, NVTN, NVKHO, NVCS); quy trình khôi phục mật khẩu. | Bỏ qua kiểm tra email khi quên mật khẩu; thiếu cơ chế khóa tạm khi nhập sai mật khẩu nhiều lần. | **Lỗ hổng bảo mật nghiêm trọng:** Nguy cơ chiếm đoạt tài khoản nhân viên và quản lý. | Khớp bắt buộc cả SĐT và Email đã đăng ký trong CSDL; bổ sung giới hạn số lần thử sai mật khẩu/OTP. | `src/bus/DangNhapBus.java`, `src/gui/LoginForm.java`, `docs/test_plan/DEFECT_CATALOG.md` |

---
*Tài liệu bàn giao và ma trận kiểm toán được biên soạn trên cơ sở đối soát mã nguồn và dữ liệu thực tế tại repository.*