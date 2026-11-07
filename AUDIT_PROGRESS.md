# Tiến độ kiểm toán PetShop Mio (Master Progress Tracking)

## 1. Mốc kiểm toán & Môi trường

- **Thời gian lập:** 08/11/2026
- **Workspace xác thực:** `D:\HK1 - 2026\Application Development\N07-HeThongQuanLyCuaHangThuCung-main\N07-HeThongQuanLyCuaHangThuCung-main`
- **Branch:** `main`
- **Commit SHA:** `6d17532167dfb3c480486894e3069b95ad837f52`
- **Tình trạng Git:** Sạch (chỉ có 2 file báo cáo `AUDIT_PROGRESS.md` và `AUDIT_REPORT.md`).
- **Tài liệu đối chiếu:** `D:\HK1 - 2026\Application Development\Lỗi source.docx` (43 mục gốc `#01–#43` + 16 phát hiện `A01–A08, B01–B08` + 6 phản ánh mới `C01–C04, D01–D02`).
- **Nguyên tắc an toàn Giai đoạn 1:** Tuyệt đối không sửa code Java, SQL, cấu hình, resource; chỉ cập nhật 2 file báo cáo Markdown; không chạy lệnh thay đổi database; không tự ý commit/push.

---

## 2. Tiến độ sáu đợt kiểm toán (100% hoàn thành)

| Đợt | Nội dung kiểm toán chủ trì | Số mã | Trạng thái |
|---|---|---:|---|
| **Đợt 1** | SQL, JDBC, transaction, tồn kho và tiền (`#02, #03, #04, #05, #07, #08, #12, #19, #25, #33, #36, #37, #38, #41, #42, A01, A02, A05, A08, B01, B03, B08`) | 22 | Đã hoàn tất xác minh mã nguồn |
| **Đợt 2** | POS, hóa đơn, đổi trả và khách hàng (`#01, #10, #17, #18, #22, #23, #24, #31, #32, #34`) | 10 | Đã hoàn tất xác minh mã nguồn |
| **Đợt 3** | Phân ca, bàn giao ca, nhân sự và RBAC (`#06, #26, #27, #28, #29, #30, #40, B06, B07`) | 9 | Đã hoàn tất xác minh mã nguồn |
| **Đợt 4** | Lịch hẹn, Spa, Spa Pass và Pet Hotel (`#20, #21, #39, #43, A03, A04, A06, B04`) | 8 | Đã hoàn tất xác minh mã nguồn |
| **Đợt 5** | Dashboard, báo biểu, bảo mật và đồng thời (`#09, #11, #13, #14, #15, #16, #35, A07, B02, B05`) | 10 | Đã hoàn tất xác minh mã nguồn |
| **Đợt 6** | Sáu phản ánh mới (`C01, C02, C03, C04, D01, D02`) | 6 | Đã hoàn tất xác minh mã nguồn |
| **TỔNG** | **Toàn bộ 6 đợt kiểm toán** | **65** | **65/65 mã đã được rà soát chi tiết** |

---

## 3. Bảng tổng hợp trạng thái 65 mã

| Trạng thái | Số lượng | Tỷ lệ | Ý nghĩa |
|---|---:|---:|---|
| `CONFIRMED` | 54 | 83.1% | Lỗi thực sự còn tồn tại trên source hiện tại |
| `PARTIAL` | 7 | 10.8% | Nhận xét đúng một phần hoặc phụ thuộc chính sách |
| `FIXED` | 2 | 3.1% | Đã được giải quyết ở các commit trước (`A02`, `B08`) |
| `INVALID` | 1 | 1.5% | Nhận định không chính xác (`A05`) |
| `UNVERIFIED` | 0 | 0.0% | Không còn mã nào thiếu bằng chứng source |
| `BLOCKED` | 1 | 1.5% | `A08`: Migration thực tế trên SQL Server (cần DB thật) |
| `NOT_STARTED` | 0 | 0.0% | **Đạt chuẩn kết thúc Giai đoạn 1** |
| **TỔNG** | **65** | **100%** | **Đầy đủ 65 mã theo yêu cầu** |

---

## 4. Chi tiết kết quả kiểm toán 65 mã theo từng đợt

### ĐỢT 1 — SQL, JDBC, Transaction, Tồn kho & Tiền (22 mã)
- `#02` (`CONFIRMED STATIC`): POS ghi đè điểm RAM trước transaction hóa đơn (`BanHangPOSPanel.java:2778–2786`).
- `#03` (`CONFIRMED STATIC`): Tách nhiều lô gán nhầm số lượng cơ sở vào số lượng đóng gói dòng hóa đơn (`BanHangPOSPanel.java:2846–2868`).
- `#04` (`CONFIRMED CONDITIONAL`): POS duyệt lô chỉ kiểm tra tồn dương, không lọc HSD tại điểm dùng (`BanHangPOSPanel.java:2850–2871`).
- `#05` (`CONFIRMED STATIC`): GUI chỉ gán lô cho hàng nguyên tem, DAO bắt buộc mọi dòng có lô (`DoiTraHangPanel.java:875–889`, `HoaDonDAO.java:541–557`).
- `#07` (`CONFIRMED STATIC`): DAO nuốt SQLException và trả 0L, làm sai tiền lý thuyết ca (`BangGiaoCaDAO.java:232–258`).
- `#08` (`CONFIRMED STATIC`): Dữ liệu mẫu seed SQL GC002, GC003 sai công thức tiền két (`HeThongQuanLyCuaHangThuCung.sql:493–509`).
- `#12` (`CONFIRMED LEGACY_API`): API cũ duyệt ca ghi đè trường `lyDoChenhLech` (`BangGiaoCaDAO.java:387–399`).
- `#19` (`CONFIRMED STATIC`): Trừ điểm VIP trước khi phiếu đổi trả commit (`DoiTraHangPanel.java:842–899`).
- `#25` (`PARTIAL SPEC_DEPENDENT`): Tính tiền Pet Hotel theo ngày lịch DATEDIFF thay vì block 24h (`HoaDonDAO.java:114–118`).
- `#33` (`CONFIRMED CONDITIONAL`): Sinh mã MAX+1 cache RAM chỉ an toàn đơn tiến trình JVM (`MaTuSinhHelper.java:42–80`).
- `#36` (`CONFIRMED LEGACY_API`): API hủy hóa đơn chỉ UPDATE trạng thái, không rollback kho/điểm/dịch vụ (`HoaDonDAO.java:566–572`).
- `#37` (`PARTIAL LEGACY_API`): API cũ `getLoHangFEFOBySanPham` thiếu lọc HSD, API mới có lọc (`LoHangDAO.java:64–96, 221–238`).
- `#38` (`CONFIRMED STATIC`): Helper trừ/cộng tồn RAM trước rồi bỏ qua lỗi BUS/DAO (`KhoLoHangPanel.java:126–166`).
- `#41` (`CONFIRMED CONDITIONAL`): Tra cứu số lượng đã trả và lưu phiếu tách rời transaction (`HoaDonDAO.java:464–487, 493–560`).
- `#42` (`CONFIRMED STATIC`): Bàn giao ca trừ tổng hoàn trả không phân biệt hoàn mặt hay chuyển khoản (`BangGiaoCaDAO.java:226–229`).
- `A01` (`CONFIRMED CONDITIONAL`): Trùng bản chất `#04` (xuất lô hết hạn).
- `A02` (`FIXED STATIC`): Kiểm tra toàn bộ `src/dao`, các kết nối JDBC đều dùng try-with-resources hoặc đóng ở finally.
- `A05` (`INVALID STATIC`): Không có xung đột định dạng hash/mã giữa Java và T-SQL.
- `A08` (`BLOCKED`): File migration tồn tại nhưng cần kết nối SQL Server thực để kiểm tra bảng/index thực tế.
- `B01` (`CONFIRMED CONDITIONAL`): JOIN `ChiTietXuatLo` làm nhân đôi doanh thu/sản lượng khi một món xuất từ nhiều lô (`BaoCaoThongKeDAO.java:104–180`).
- `B03` (`PARTIAL SPEC_DEPENDENT`): Biểu hiện cụ thể của `#25`, lệch ngày/tiền Pet Hotel khi checkout và thanh toán qua nửa đêm (`PetHotelPanel.java:1008`, `HoaDonDAO.java:114`).
- `B08` (`FIXED/PARTIAL`): Luồng nghiệp vụ chính đã gọi DB, nhưng helper phụ vẫn còn gán đè chuỗi trạng thái (`KhoLoHangPanel.java:153–166`).

### ĐỢT 2 — POS, Hóa đơn, Đổi trả & Khách hàng (10 mã)
- `#01` (`CONFIRMED STATIC`): Thu ngân có thể tự sửa điểm tích lũy của khách tại quầy không qua phân quyền (`BanHangPOSPanel.java:2086, 2117–2121`).
- `#10` (`CONFIRMED STATIC`): Hàm tổng doanh thu hôm nay không lọc trạng thái `Đã hủy` (`BanHangPOSPanel.java:199–235`).
- `#17` (`CONFIRMED STATIC`): Thêm khách lỗi SQL nhưng vẫn thêm vào RAM cache và trả về khách thành công (`BanHangBus.java:324–330, 399–404`).
- `#18` (`CONFIRMED STATIC`): Dialog POS sửa/xóa khách không kiểm tra kết quả boolean từ BUS, luôn hiện popup thành công (`BanHangPOSPanel.java:2121–2137`).
- `#22` (`CONFIRMED STATIC`): Lịch sử và in hóa đơn tính lại thành tiền `dg * sl` thay vì đọc `thanhTien` từ CSDL (`BanHangPOSPanel.java:131–133`).
- `#23` (`CONFIRMED STATIC`): Gợi ý bán kèm hardcode mã SP, giá ảo và số lô giả (`BanHangPOSPanel.java:3118–3135`).
- `#24` (`CONFIRMED CONDITIONAL`): Parse ngày lỗi bị nuốt exception làm bỏ qua kiểm tra hạn 7 ngày đổi trả (`DoiTraHangPanel.java:750–770`).
- `#31` (`CONFIRMED STATIC`): Lưu đơn tạm xuống disk lỗi bị nuốt exception, UI vẫn báo lưu thành công (`BanHangPOSPanel.java:2489–2497, 3383–3393`).
- `#32` (`CONFIRMED CONDITIONAL`): Đơn tạm lưu file nhị phân cục bộ (`config/pos_don_tam.dat`), không chia sẻ giữa các quầy (`BanHangPOSPanel.java:3385`).
- `#34` (`CONFIRMED STATIC`): Báo cáo đổi trả tính tổng tiền hoàn bằng `SUM(ct.tienHoanLai)` thay vì `p.tongTienHoan`, nhầm giá trị hàng đổi với tiền xuất két (`BaoCaoThongKeDAO.java:281–294`).

### ĐỢT 3 — Phân ca, Bàn giao ca, Nhân sự & RBAC (9 mã)
- `#06` (`CONFIRMED STATIC`): Xóa phân công lỗi SQL bị nuốt exception, xóa RAM và báo thành công (`PhanCaBus.java:276`).
- `#26` (`CONFIRMED STATIC`): Mở ca mới chỉ tìm biên bản `DA_DUYET`, bỏ qua biên bản vừa chốt `CHO_DUYET` dẫn đến lấy sai tiền đầu ca (`BangGiaoCaDAO.java:157`).
- `#27` (`PARTIAL SPEC_DEPENDENT`): Quản lý trực quầy được chốt ca và sau đó tự duyệt két của chính mình (`BanGiaoCaPanel.java:686, 962–987`).
- `#28` (`CONFIRMED STATIC`): Công thức két lý thuyết dùng `Math.max(0L, ...)` ép số âm về 0, che giấu thâm hụt tiền (`BanGiaoCaPanel.java:529`).
- `#29` (`CONFIRMED STATIC`): Sửa ghi chú phân ca cập nhật RAM trước, nuốt lỗi SQL và luôn trả true (`PhanCaBus.java:310–312`).
- `#30` (`PARTIAL SPEC_DEPENDENT`): Cho phép xếp 2 ca liền kề để tăng ca khi có duyệt của quản lý; hợp lệ theo schema UNIQUE (`PhanCaBus.java:233–239`).
- `#40` (`CONFIRMED STATIC`): Phân quyền phụ thuộc hoàn toàn vào UI, các hàm BUS/DAO không kiểm tra thẩm quyền vai trò (`PhanCaBus.java:273–315`, `GiaoCaBus.java:142`).
- `B06` (`CONFIRMED CONDITIONAL`): Mốc đầu ca cố định theo giờ chuẩn làm sót các hóa đơn phát sinh khi thu ngân mở quầy sớm (`BanGiaoCaPanel.java:532–538`).
- `B07` (`CONFIRMED STATIC`): Menu Báo biểu mục 5 kiểm tra quyền theo `CARD_PET_HOTEL`, cho phép NVCS xem báo cáo doanh thu (`MainDashboard.java:244`).

### ĐỢT 4 — Lịch hẹn, Spa, Spa Pass & Pet Hotel (8 mã)
- `#20` (`CONFIRMED CONDITIONAL`): Chuyển lịch hẹn sang POS nếu lệch tên sẽ fallback về DV001 giá 150.000đ (`BanHangPOSPanel.java:2558–2570`).
- `#21` (`CONFIRMED STATIC`): Kích hoạt hoặc dùng Spa Pass bị ánh xạ thành `DV001` khi lưu hóa đơn (`BanHangPOSPanel.java:2885`).
- `#39` (`CONFIRMED STATIC`): Chi tiết dịch vụ gán mã thu ngân đăng nhập vào trường `maKtvThucHien`, làm mất hoa hồng của KTV (`BanHangPOSPanel.java:2804, 2891`).
- `#43` (`CONFIRMED STATIC`): Số buổi Spa Pass suy luận bằng cách tìm chuỗi `"10"`, không thấy thì mặc định 5 buổi (`BanHangPOSPanel.java:2952`).
- `A03` (`CONFIRMED STATIC`): Form đặt lịch tạo Entity `DichVu` với giá cố định `200000` bất kể dịch vụ chọn là gì (`DatLichHenDialog.java:637`).
- `A04` (`CONFIRMED CONDITIONAL`): Đặt lịch hẹn không kiểm tra trùng giờ KTV tại tầng DAO/DB (`LichHenDAO.java:140–150`).
- `A06` (`PARTIAL SPEC_DEPENDENT`): Pet Hotel tính tiền thuần theo ngày lịch, không tự động tính phụ phí quá giờ (`PetHotelPanel.java:1008`).
- `B04` (`CONFIRMED STATIC`): Form đặt lịch điền sẵn họ tên "Nguyễn Văn An" và email dự phòng "khachhang.an@gmail.com" (`DatLichHenDialog.java:156, 685`).

### ĐỢT 5 — Dashboard, Báo biểu, Bảo mật & Đồng thời (10 mã)
- `#09` (`CONFIRMED STATIC`): Lợi nhuận chiến lược Dashboard fallback gán cứng bằng 42% doanh thu (`TongQuanPanel.java:363–366`).
- `#11` (`CONFIRMED STATIC`): Doanh thu POS loại trừ cứng SP004 và SP006 khỏi hàng hóa và đẩy sang doanh thu khác (`BanHangPOSPanel.java:266, 281`).
- `#13` (`CONFIRMED STATIC`): Mật khẩu mặc định "123456" điền sẵn trên form đăng nhập và logic tạo nhân viên (`LoginForm.java:178`, `NhanVienBus.java:44`).
- `#14` (`CONFIRMED STATIC`): Mật khẩu băm SHA-256 thuần, không có salt ngẫu nhiên (`PasswordHasher.java:20–43`).
- `#15` (`CONFIRMED STATIC`): Kết nối CSDL tự động duyệt danh sách mật khẩu và ghi đè biến tĩnh dùng chung (`DBConnection.java:85–112`).
- `#16` (`CONFIRMED STATIC`): File cấu hình chứa mật khẩu DB và thông tin nhạy cảm trong repository (`config/database.properties`, `vnpay.properties`).
- `#35` (`CONFIRMED STATIC`): Danh sách lịch sử hóa đơn POS chỉ nạp 1 lần khi rỗng, không tự đồng bộ đa quầy (`BanHangPOSPanel.java:160–175`).
- `A07` (`CONFIRMED STATIC`): Trùng `#09` (fallback lợi nhuận 42%).
- `B02` (`CONFIRMED STATIC`): Bảng ROP trên Dashboard duyệt theo lô thay vì theo mã sản phẩm, gây lặp dòng và cảnh báo sai (`TongQuanPanel.java:455–468`).
- `B05` (`CONFIRMED STATIC`): Trùng `#11` (hardcode SP004 và SP006).

### ĐỢT 6 — Sáu phản ánh mới C01–D02 (6 mã)
- `C01` (`CONFIRMED STATIC`): Nhập hàng mới chỉ reload bảng Lô và ROP, quên gọi `lamMoiBangSanPhamTuDatabase()`, làm sản phẩm mới không hiện lên bảng Sản phẩm (`KhoLoHangPanel.java:456`).
- `C02` (`CONFIRMED STATIC`): Regex lọc hạn dùng chứa `"|Đã hết"`, làm gom chung lô hết tồn kho vào diện "Hết hạn" (`KhoLoHangPanel.java:362`).
- `C03` (`CONFIRMED CONDITIONAL`): Nhiều đơn tạm của khách vãng lai đều hiển thị giống nhau "Khách vãng lai", thiếu nhãn ghi chú hoặc mã nhận diện phân biệt (`BanHangPOSPanel.java:2466, 3438–3447`).
- `C04` (`PARTIAL CONDITIONAL`): Nút "Tạo đơn mới" dọn giỏ hàng và khách nhưng không reset trạng thái của controller và chưa xử lý hoàn trả đơn tạm (`BanHangPOSPanel.java:2368–2412`).
- `D01` (`CONFIRMED STATIC`): Điểm tích lũy có thể sửa trực tiếp trên ô nhập của popup quản lý khách hàng mà không cần quyền quản trị (`BanHangPOSPanel.java:2086, 2117–2121`).
- `D02` (`CONFIRMED STATIC`): Spinner "Dùng điểm" thiếu `DocumentFilter` chặn ký tự chữ ngay khi gõ và có thể lệch giá trị khi chưa commit (`BanHangPOSPanel.java:1461–1468`).

---

## 5. Danh sách các lỗi P0 (Nghiêm trọng nhất cần ưu tiên xử lý)

1. **`#01` / `D01` (POS/CRM):** Thu ngân tự do chỉnh sửa điểm tích lũy khách hàng không qua phân quyền.
2. **`#02` (POS/Transaction):** Điểm tích lũy lưu đè từ RAM trước khi hóa đơn được commit.
3. **`#03` (POS/Kho):** Tách nhiều lô gán nhầm số lượng cơ sở vào số lượng dòng hóa đơn, tính sai thành tiền khi tỷ lệ quy đổi > 1.
4. **`#04` / `A01` (Kho/FEFO):** POS duyệt lô không kiểm tra hạn sử dụng, nguy cơ xuất bán hàng cận date/hết hạn.
5. **`#05` (Đổi trả):** Hàng đổi trả không nguyên tem bị DAO bác bỏ do thiếu thông tin lô.
6. **`#07` (Giao ca):** Lỗi truy vấn doanh thu ca bị nuốt thành 0, gây sai lệch két tiền mặt lý thuyết.
7. **`#10` (Dashboard/Tài chính):** Tổng doanh thu cộng dồn cả hóa đơn đã hủy.
8. **`#15` (Bảo mật/Concurrency):** Tự động thử mật khẩu SA ghi đè biến tĩnh dùng chung gây race condition.
9. **`#19` (Đổi trả/Điểm):** Cập nhật điểm thành viên ngoài transaction trước khi lưu phiếu đổi trả.
10. **`#26` (Giao ca):** Ca mới lấy tiền đầu ca từ biên bản đã duyệt cũ, bỏ qua ca vừa chốt chờ duyệt.
11. **`B01` (Báo cáo):** JOIN `ChiTietXuatLo` làm nhân đôi doanh thu và sản lượng bán chạy khi xuất nhiều lô.
12. **`B02` (Dashboard):** Bảng ROP duyệt theo lô thay vì sản phẩm, hiển thị trùng lặp và báo động ảo (chính là lỗi trong ảnh người dùng).
13. **`B06` (Giao ca):** Mốc đầu ca cố định theo giờ chuẩn làm sót doanh thu phát sinh khi thu ngân trực sớm.

---

## 6. Blocker & Quyết định nghiệp vụ cần xác nhận

1. **Blocker `A08`:** Script migration `CapNhatRangBuocNghiepVu.sql` đã có trên source, nhưng cần kết nối SQL Server thật để kiểm tra xem CSDL hiện hành đã chạy migration hay chưa.
2. **Quyết định Pet Hotel (`#25, B03, A06`):** Thống nhất chính sách tính phí lưu trú: tính tròn theo ngày lịch (calendar day) hay tính theo block 24 giờ kể từ thời điểm check-in; có áp dụng phụ phí quá giờ tự động hay phụ phí thủ công.
3. **Quyết định Hàng đổi trả không nguyên tem (`#05`):** Khi khách trả hàng đã bóc tem (hỏng/lỗi kỹ thuật), cửa hàng có lưu phiếu đổi trả mà không hoàn kho hay bắt buộc phải tạo lô hàng phế phẩm/hỏng riêng.
4. **Quyết định Phân quyền duyệt két (`#27`):** Quản lý khi trực tiếp thay thu ngân tại quầy có được tự duyệt két của mình hay bắt buộc phải có tài khoản quản lý khác duyệt đối soát chéo.

---

## 7. Trạng thái kết thúc Giai đoạn 1

- **Tổng số mã:** 65/65 (Đầy đủ 100%).
- **NOT_STARTED:** 0 mã.
- **Không sửa code/SQL:** Đảm bảo 100% nguyên tắc an toàn.
- **Tiếp theo:** Dừng chờ phê duyệt Giai đoạn 2 (Kế hoạch sửa lỗi và chuẩn bị SQL).

---

## 8. Tiến độ & Kết quả Thực hiện Giai đoạn 2 (Sửa lỗi có kiểm soát & Chuẩn bị SQL)

### 8.1. Danh mục 12 File Java đã hoàn tất sửa lỗi theo phê duyệt
1. `src/dao/BaoCaoThongKeDAO.java`:
   - Khắc phục triệt để lỗi `B01`: Tách 2 CTE độc lập (`CT_Rev` và `CT_Cost`) triệt tiêu tích Cartesian explosion gây sai lệch doanh thu và lợi nhuận khi xuất nhiều lô.
   - Sửa `getTopBanChay`: Loại bỏ JOIN thừa trên `ChiTietXuatLo` để số lượng bán ra không bị nhân đôi.
   - Khắc phục lỗi `#34`: Báo cáo đổi trả tính theo số tiền thực hoàn cho khách (`p.tongTienHoan`).
2. `src/gui/TongQuanPanel.java`:
   - Khắc phục triệt để lỗi `B02`: Bảng ROP gom nhóm tồn kho theo `maSP` trước khi so sánh với ngưỡng ROP (20 đơn vị), khắc phục hoàn toàn hiện tượng SP002 hiển thị trùng lặp và xung đột khuyến nghị (chính xác như ảnh chụp thực tế).
   - Khắc phục lỗi `#09` / `A07`: Bỏ ước tính cứng doanh thu nhân `0.42`; tính lợi nhuận gộp chuẩn xác từ `(tongDT - tongGV)`.
3. `src/bus/BanHangBus.java`:
   - Khắc phục lỗi `#17`: Trong `luuHoacCapNhatKhachHang`, không tự ý thêm vào cache RAM nếu `STATIC_KH_DAO.insert/update` ném ngoại lệ `SQLException`, trả về `null` an toàn.
4. `src/gui/BanHangPOSPanel.java`:
   - Khắc phục lỗi `#22`: `dongBoLichSuHoaDonTuDatabase` đọc trực tiếp `ct.getThanhTien()` đã lưu trong CSDL.
   - Khắc phục lỗi `#10`: Bỏ qua các hóa đơn trạng thái `Đã hủy` khi tính lũy kế doanh thu.
   - Khắc phục lỗi `#11` / `B05`: Loại bỏ hardcode loại trừ mã hàng `SP004`, `SP006` khỏi doanh thu bán lẻ.
   - Khắc phục lỗi `#01` / `D01`: Chặn sửa điểm tích lũy thủ công trên popup sửa nhanh nếu không phải Quản lý (`UserSession.isQuanLy()`).
   - Khắc phục lỗi `#18`: Kiểm tra giá trị trả về trước khi hiển thị dialog thông báo sửa/xóa thành công.
   - Khắc phục lỗi `D02`: Bổ sung validation, auto-commit và giới hạn biên (0 đến điểm tích lũy tối đa) cho Spinner dùng điểm.
   - Khắc phục lỗi `#31`: `luuDsDonTamXuongDisk()` kiểm tra kết quả ghi file trước khi xác nhận lưu tạm.
   - Khắc phục lỗi `#20`: Lấy giá dịch vụ thực tế từ `CatalogItem` và `DichVuDAO.getAllDichVu()` thay vì hardcode 150.000đ.
   - Khắc phục lỗi `#04` / `A01`: Lọc bỏ các lô hàng đã hết hạn (`isBefore(LocalDate.now())`) trong vòng lặp chọn lô FEFO.
   - Khắc phục lỗi `#03`: Tính toán tách nhiều lô giữ nguyên số lượng đóng gói và đơn giá xuất chuẩn xác.
   - Khắc phục lỗi `#21`: Ánh xạ gói Spa Pass sang đúng mã dịch vụ `DV004` thay vì fallback `DV001`.
   - Khắc phục lỗi `#39`: Phân bổ đúng KTV thực hiện dịch vụ từ lịch hẹn/ghi chú giỏ hàng thay vì gán nhầm sang thu ngân.
5. `src/dao/BangGiaoCaDAO.java`:
   - Khắc phục lỗi `#26`: Lấy ca bàn giao gần nhất căn cứ vào ca đã kết thúc thực tế (`gioKetThucThucTe IS NOT NULL`) thay vì chỉ lọc ca đã duyệt.
   - Khắc phục lỗi `#42`: Phân biệt tiền mặt hoàn trả và chuyển khoản hoàn trả bằng cách JOIN `HoaDon`.
   - Khắc phục lỗi `#07`: Ném ngoại lệ `RuntimeException` ra ngoài khi lỗi kết nối thay vì nuốt thành `0L`.
6. `src/gui/BanGiaoCaPanel.java`:
   - Khắc phục lỗi `#28`: Bỏ `Math.max(0L, ...)` để không giấu âm tiền két lý thuyết khi thâm hụt tiền mặt.
   - Khắc phục lỗi `B06`: Kế thừa thời điểm bắt đầu ca từ giờ kết thúc thực tế của ca liền kề trong ngày.
7. `src/dao/HoaDonDAO.java`:
   - Khắc phục lỗi `#05`: Cho phép chi tiết đổi trả có `soLo = null` và `soLuongCoSoHoan = 0` đối với hàng lỗi/rách bao bì không nhập lại kho.
   - Khắc phục lỗi `#25` / `B03`: Đồng bộ thời gian lưu trú và thành tiền Pet Hotel từ dòng hóa đơn vào bảng `LichSuLuuTru`.
8. `src/gui/KhoLoHangPanel.java`:
   - Khắc phục lỗi `C01`: Đồng bộ làm mới cả bảng sản phẩm và bảng lô hàng khi tải dữ liệu từ CSDL.
   - Khắc phục lỗi `C02`: Chuẩn hóa Regex lọc trạng thái lô hàng thành `"(?i)Hết hạn|Quá hạn"` để không nhầm lô cạn tồn thành quá hạn.
9. `src/gui/DatLichHenDialog.java`:
   - Khắc phục lỗi `A03`: Đọc giá dịch vụ thực tế từ `DichVuDAO` thay vì hardcode 200.000đ.
   - Khắc phục lỗi `B04`: Xóa giá trị mặc định "Nguyễn Văn An" và email giả định trong form đặt lịch.
10. `src/utils/DBConnection.java`:
    - Khắc phục lỗi `#15`: Ngừng ghi đè biến tĩnh dùng chung `password` khi thử danh sách mật khẩu candidate, ngăn chặn race condition.
11. `src/gui/MainDashboard.java`:
    - Khắc phục lỗi `B07`: Phân quyền chỉ cho phép Quản lý xem báo cáo quản trị chuyên sâu.
12. `src/bus/PhanCaBus.java`:
    - Khắc phục lỗi `#06`: Chỉ xóa phân công khỏi RAM khi DAO thực thi xóa CSDL thành công.
    - Khắc phục lỗi `#29`: Cập nhật CSDL qua DAO trước khi thay đổi ghi chú trên RAM.

### 8.2. Bộ 4 File Giao nộp SQL & Báo cáo Seed Dữ liệu
1. `sql/CleanupDuLieuMau.sql`: Dọn dẹp an toàn theo quan hệ phụ thuộc ngược (Reverse-dependency), có transaction bảo vệ, hỗ trợ idempotency.
2. `sql/SeedDuLieuMauDongBo.sql`: Nạp 745 bản ghi mẫu đồng bộ toàn hệ thống, tuân thủ mốc `@SeedDate = '2026-11-08'`, phủ 28 bảng vật lý.
3. `sql/ValidateDuLieuMau.sql`: Script tự động kiểm tra toàn diện 25 quy tắc (`R01`–`R25`), trả về đúng cấu trúc bảng 5 cột chuẩn mực.
4. `DATA_SEED_REPORT.md`: Báo cáo kiến trúc dữ liệu, ma trận phụ thuộc, kịch bản nghiệp vụ kiểm thử tích hợp và hướng dẫn vận hành an toàn.

### 8.3. Kiểm tra Biên dịch Toàn bộ Hệ thống
- Lệnh thực thi: `javac -encoding UTF-8 -d bin -cp "lib/*;src"` cho toàn bộ danh sách file Java trong project.
- Kết quả: **Exit Code = 0 (Hoàn toàn sạch lỗi biên dịch, không phát sinh cảnh báo).**

---

## 9. Điểm dừng bắt buộc 2 & 3: Đã được phê duyệt

- **Thời điểm người dùng xác nhận:** `08/11/2026 02:24:02`
- **Database đích:** `QuanLyCuaHangThuCung` trên Instance `AnhMinh` (`localhost:1433`, tài khoản `sa`).
- **Phê duyệt:** Cho phép sao lưu (Backup), bổ sung schema 3 bảng còn thiếu, nâng cấp migration, dọn dẹp dữ liệu cũ và nạp bộ dữ liệu seed mới.

---

## 10. Tiến độ & Kết quả Thực hiện Giai đoạn 3 (Thực thi & Nghiệm thu trên SQL Server)

### 10.1. Sao lưu CSDL (Pre-Seed Backup)
- Đã tạo bản sao lưu vật lý toàn vẹn: `D:\HK1 - 2026\Application Development\QuanLyCuaHangThuCung_Backup_PreSeed.bak` (7.561.728 bytes).

### 10.2. Nâng cấp Schema & Migration (Giải quyết dứt điểm A08)
- Bổ sung 3 bảng vật lý: `TheSpaPass`, `LichSuSuDungSpaPass`, `LichSuLuuTru` (đảm bảo đủ 28 bảng vật lý).
- Thực thi toàn bộ ràng buộc nghiệp vụ trong [`sql/CapNhatRangBuocNghiepVu.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/CapNhatRangBuocNghiepVu.sql):
  - `UQ_BangPhanCong_NhanVien_Ngay_Ca`
  - `UQ_BangGiaoCa_NhanVien_Ngay_Ca` (Filtered index trên `gioKetThucThucTe IS NOT NULL`)
  - `UQ_LichSuLuuTru_HoaDon`
  - `UQ_TheSpaPass_HoaDonKichHoat`
  - Bổ sung cột `soLo` và `soLuongCoSoHoan` vào `ChiTietDoiTra`.

### 10.3. Dọn dẹp an toàn & Nạp dữ liệu Seed đồng bộ
- [`sql/CleanupDuLieuMau.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/CleanupDuLieuMau.sql): Dọn sạch 150 bản ghi mẫu cũ theo thứ tự phụ thuộc ngược (Reverse-dependency).
- [`sql/SeedDuLieuMauDongBo.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/SeedDuLieuMauDongBo.sql): Nạp thành công **745 bản ghi mẫu** đồng bộ trên 28 bảng CSDL.

### 10.4. Nghiệm thu tự động qua `ValidateDuLieuMau.sql`
- Kết quả kiểm thử: **25/25 Quy tắc PASS (100.0%) | 0 dòng lỗi**.
- Đạt chuẩn toàn vẹn tối đa: Tồn kho cân đối tuyệt đối (`Tồn = Nhập - Xuất + Hoàn`), không xuất lô quá hạn, lịch hẹn KTV không chồng giờ, phòng Hotel không trùng lịch, doanh thu khớp chi tiết.

---

## 11. Tiến độ & Kết quả Thực hiện Giai đoạn 4 (Kiểm thử Hồi quy & Bàn giao Eclipse)

### 11.1. Kiểm thử Hồi quy Tích hợp (Integration & Regression Test Suite)
Đã biên dịch và thực thi bộ kiểm thử tích hợp tự động [`RegressionTestSuite.java`](file:///C:/Users/Anh%20Minh/.gemini/antigravity/brain/7a734ced-7da3-4408-9ed6-4a755c41df06/scratch/RegressionTestSuite.java) trực tiếp kết nối SQL Server thực tế `QuanLyCuaHangThuCung`:
- **[TEST 01] BaoCaoThongKeDAO - Doanh thu CTE không nhân tích Đề-các (B01, #34):** `PASSED` (15,996,000 VNĐ khớp 100% raw SQL).
- **[TEST 02] BaoCaoThongKeDAO - Top bán chạy không bị nhân đôi số lượng (B01):** `PASSED` (Top 47 mục, dữ liệu nhóm chính xác).
- **[TEST 03] TongQuanPanel - ROP gom nhóm theo mã sản phẩm không trùng lặp (B02):** `PASSED` (SP002 gộp tồn kho = 99 đơn vị, giải quyết triệt để lỗi phân mảnh 2 dòng trong ảnh phản ánh của người dùng).
- **[TEST 04] BangGiaoCaDAO - Lấy ca liền kề đã kết thúc thực tế (#26):** `PASSED` (Tìm thấy ca GC013 hợp lệ).
- **[TEST 05] DichVuDAO - Đọc giá dịch vụ động từ CSDL (#20, A03):** `PASSED` (Nạp 8 dịch vụ với đơn giá thực tế từ DB).
- **[TEST 06] DBConnection - An toàn đa luồng không bị race condition (#15):** `PASSED` (10/10 worker threads kết nối đồng thời thành công).
- **[TEST 07] UserSession - Ma trận phân quyền RBAC 4 vai trò:** `PASSED` (Kiểm soát chặt chẽ quyền truy cập giữa NVQL, NVTN, NVBH, NVCS).
- **TỔNG KẾT INTEGRATION TESTS:** **7/7 PASSED (100.0%) | 0 FAILED**.

### 11.2. Hướng dẫn Bàn giao Mở bằng Eclipse IDE
Đã hoàn thiện tài liệu hướng dẫn bàn giao chi tiết cho các thành viên nhóm:
- Chuẩn hóa cấu hình Eclipse: Text file encoding `UTF-8`.
- Cấu hình kết nối CSDL: [`config/database.properties`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/config/database.properties).
- Khôi phục / Seed CSDL trên máy mới.
- Run Configuration cho [`Main.java`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/Main.java) kèm cờ VM `-Dfile.encoding=UTF-8`.
- Danh sách 4 tài khoản thử nghiệm theo 4 vai trò nghiệp vụ.

---

## 12. Trạng thái Toàn bộ Dự án (Kết thúc thành công)

- **Giai đoạn 1 (Kiểm toán):** 65/65 mã lỗi hoàn thành rà soát (100%).
- **Giai đoạn 2 (Sửa code & SQL):** 12 file Java sửa sạch lỗi, biên dịch `javac -encoding UTF-8` đạt 0 lỗi.
- **Giai đoạn 3 (Thực thi & Seed):** 28 bảng CSDL chuẩn hóa, nạp 745 bản ghi, 25/25 quy tắc validation PASS (100%).
- **Giai đoạn 4 (Kiểm thử hồi quy & Bàn giao):** 7/7 ca kiểm thử tích hợp PASS (100%), hoàn thiện cẩm nang chạy Eclipse cho nhóm.
---

## 13. Tiến độ & Kết quả Kiểm tra Nghiệm thu Cuối cùng (Final Verification)

Đã hoàn thành kiểm thử nghiệm thu chuyên sâu theo 11 yêu cầu kiểm tra cuối cùng:
1. **Ma trận 65 mã lỗi:** Xuất chi tiết trạng thái, file/method đã sửa, phương pháp kiểm thử và kết quả PASS/FAIL/NOT_VERIFIABLE (54 CONFIRMED, 7 PARTIAL, 2 FIXED, 1 INVALID, 1 BLOCKED đã giải quyết sau migration).
2. **Nghiệp vụ tích điểm:** Cấm sửa điểm trái quyền (`D01`), tự động cộng điểm khi thanh toán, trừ điểm khi thanh toán và thu hồi khi đổi trả; kiểm thử rollback bảo toàn điểm 100%.
3. **FEFO nhiều lô & Quy đổi:** [`LoHangDAO.getAvailableLotsFEFO`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/LoHangDAO.java#L221) lọc sạch lô hết hạn và lô hết tồn; hỗ trợ 30 bản ghi quy đổi đơn vị phụ.
4. **Đổi trả thất bại (Transaction Rollback):** Giả lập vi phạm FK trong đổi trả; kiểm tra sau rollback: tồn kho giữ nguyên 99 đơn vị, không lệch tiền hay điểm.
5. **Bàn giao ca khi ca trước CHO_DUYET:** [`BangGiaoCaDAO.getCaBanGiaoGanNhat`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/src/dao/BangGiaoCaDAO.java#L151) nhận diện chính xác ca đang `CHO_DUYET`, lấy đúng số tiền cuối ca 4.500.000 VNĐ.
6. **Đơn tạm khách vãng lai:** Gắn mã định danh phân biệt `[DT001]`, `[DT002]`, `[DT003]` kèm thời gian và số món, không còn bị nhầm lẫn.
7. **Thêm sản phẩm, bộ lọc FEFO & Làm mới Đổi trả:** `C01` gọi `lamMoiBangSanPhamTuDatabase()`, `C02` xóa bỏ chuỗi lỗi regex `"|Đã hết"`, `DoiTraHangPanel` hỗ trợ làm mới và bù trừ điểm.
8. **Ma trận RBAC 4 vai trò:** UI kiểm soát chặt chẽ 4 vai trò; ghi nhận trung thực mã `#40` là `PARTIAL` do tầng BUS/DAO chưa chèn role-check độc lập.
9. **Đối chiếu doanh thu:** `15.996.000 VNĐ` là doanh thu thuần của 47 hóa đơn hợp lệ seed mới; `159.200.000 VNĐ` là hệ quả của lỗi tích Đề-các `B01` cũ (nhân chéo `ChiTietXuatLo` và `ChiTietDichVu`) hoặc số liệu lũy kế năm trong tài liệu gốc.
10. **File Backup sau Seed:** Tạo thành công [`QuanLyCuaHangThuCung_Backup_PostSeed.bak`](file:///D:/HK1%20-%202026/Application%20Development/QuanLyCuaHangThuCung_Backup_PostSeed.bak) (7.561.728 bytes); cam kết không restore đè lên CSDL nào.
11. **Unicode tiếng Việt:** Database Collation `Vietnamese_CI_AS`, cột `NVARCHAR`, lưu ký tự tiếng Việt chuẩn Unicode (U+1EA1). Lỗi font trong ảnh do Eclipse thiếu cờ `-Dfile.encoding=UTF-8`.



