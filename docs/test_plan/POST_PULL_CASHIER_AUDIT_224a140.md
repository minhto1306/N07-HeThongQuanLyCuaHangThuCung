# Kiểm thử sau pull và trải nghiệm thu ngân — 08/11/2026

## Kết luận thực tế

Đã pull fast-forward `main` từ `6d17532` đến `224a140bb6d60667a6b15699db37c28f66119f41`. Không commit/push, không ghi đè thay đổi cục bộ. Nguồn ứng dụng được kiểm tra là bản vừa pull, không tự sửa Java/SQL sản phẩm.

Luồng bán tiền mặt đơn giản có thể sử dụng: thêm hàng, kiểm tra thiếu tiền, nút Đủ tiền, thanh toán, xem hóa đơn, trừ tồn, lưu tạm và khôi phục đơn đều thao tác được. Tuy nhiên chưa nên coi hệ thống đã nghiệm thu: còn lỗi xác thực khôi phục mật khẩu, mapping DB chính, bán tách lô, trả hàng đồng thời, số ngày Hotel, lượt Spa và báo cáo quy đổi/kho.

## Hai môi trường được phân biệt

| Môi trường | Hành động | Kết quả |
|---|---|---|
| DB hiện hữu lấy từ cấu hình project | Chỉ đọc metadata/SELECT/DAO; không migration, seed, cleanup, cập nhật cấu hình | Có 27 bảng; thiếu 7 cột trên bảng hiện hữu và toàn bộ bảng lịch sử Spa |
| `MioAudit_224a140_20261108` | DB mới riêng được người dùng yêu cầu tạo; schema, migration, seed, dữ liệu lớn và các giao dịch lỗi đều ở đây | Đủ 28 bảng; dữ liệu giả; các bản ghi lỗi được giữ làm bằng chứng |

CWD của ứng dụng thử: `.ai-work/audit-224a140/sandbox-app`. Cấu hình DB được tạo riêng tại đó; không sao chép cấu hình SMTP/VNPay/VietQR thật. CashierLauncher kiểm tra `DB_NAME()` trước khi khởi động và xác thực tài khoản thu ngân mẫu bằng BUS. Không tự động thao tác màn hình đăng nhập của người dùng, không gửi email, không chuyển tiền, không in máy in thật.

Script khởi tạo nguyên bản gặp lỗi SQL **10609** ở filtered index dùng computed column `ngayGiaoCa`. Migration vừa pull sửa bộ lọc sang `gioKetThucThucTe` và tạo được index trong DB thử. Không tự áp dụng sửa này lên DB hiện hữu. DB mới tạo từ script hiện tại không chứa các UDF/procedure sinh mã cũ có trên DB hiện hữu; ứng dụng hiện dùng helper Java. Vì vậy lỗi thiếu UDF khi probe DB thử không phải lỗi gọi API của luồng POS đã chạy.

## Kết quả kiểm tra có bằng chứng

| Nhóm | Kết quả | Phạm vi diễn giải |
|---|---|---|
| Compile Java 17 | 87/87 file, exit 0 | Không tương đương nghiệp vụ đúng |
| Đọc DAO trên DB hiện hữu | 19/20 chạy được | XNT lỗi 207; các báo cáo trả rỗng không chứng minh đúng số tiền |
| Kiểm thử ứng dụng trên DB riêng | **25 điều kiện: 11 đạt, 14 không đạt** | Tổng hợp scenario-results, scenario-more, cashier-edge-results, package-mapping-results; gồm API, concurrency, đối chiếu SQL; không phải 25 thao tác GUI |
| Tải đồng thời | **400/400 thanh toán, 8 luồng, 54,16 giây** | Stock giảm đúng 400; bài tải dùng ID định trước, không chứng minh helper tự sinh ID an toàn |
| Hai quầy tranh tồn | 1 thành công, 1 bị chặn; tồn 10 còn 2 | Hai quầy cùng mua 8, không bán vượt kho |
| Contract probe không ghi DB | 10 đạt, 4 không đạt | JDBC recorder, chỉ là bằng chứng payload; đã kiểm chứng các vấn đề chính bằng DB thử |
| Cầu nối AI | 27/27 test đạt | Worker mock/fixture; không phải 27 nghiệp vụ bán hàng |

Một lỗi fixture ban đầu dùng P011/P012 không có trong seed làm lượt thử đầu dừng sau 10 điều kiện. Không tính lỗi fixture là lỗi sản phẩm. Đã tiếp tục 8 điều kiện còn lại bằng phòng ATROOM1/ATROOM2 được tạo riêng; log chia làm hai file. Nguồn SandboxScenarios đã bổ sung phòng thử để lần chạy trên DB mới không gặp lỗi fixture đó. Các bộ scenario chỉ chạy một lần trên DB mới, không được chạy lại mù vì ID fixture đã tồn tại.

## 1. Các lỗi đã tái hiện

| Mã | Ưu tiên | Tình huống và kết quả | Bằng chứng nguồn chính |
|---|---|---|---|
| A01 | P0 | Email sai nhưng SĐT đúng vẫn được BUS chấp nhận để khôi phục mật khẩu; GUI gửi OTP tới email người nhập. Chỉ kiểm tra bước xác thực, không gửi OTP thật | DangNhapBus.java:132–154; LoginForm.java:375–396; live-checks/sandbox-validation có `RESET_WRONG_EMAIL_ACCEPTED=true` |
| A02 | P1 | Bán tách lô không tròn hộp/thùng đặt qdXuat=null; SQL bắt buộc maQuyDoi nên giao dịch lỗi **515**. Rollback giữ nguyên tồn và hóa đơn | BanHangPOSPanel.java nhánh qdXuat=null; HoaDonDAO setString maQuyDoi; ChiTietXuatLo.maQuyDoi NOT NULL |
| A03 | P1 | Hai phiếu trả cùng hóa đơn một món đều được ghi khi chạy đồng thời. Không tái kiểm tra tổng số đã trả trong transaction | HoaDonDAO.luuPhieuDoiTraTrongTransaction; scenario-results: accepted=2 |
| A04 | P1 | Hóa đơn Hotel cho lưu trú 2 ngày, 300.000/ngày, 600.000 tổng: lịch sử lưu **1 ngày** | POS đưa số ngày vào chuỗi ĐVT nhưng quantity=1; HoaDonDAO.hoanTatHotelTrongTransaction lấy quantity làm số ngày |
| A05 | P1 | Một hóa đơn hai phòng lỗi **2601** vì UNIQUE LichSuLuuTru.maHoaDon; rollback cả hóa đơn/phòng | HoaDonDAO duyệt danh sách phòng; script/migration tạo UQ_LichSuLuuTru_HoaDon |
| A06 | P1 | Chi tiết dịch vụ số lượng 2 dùng Spa Pass chỉ tăng soBuoiDaDung **1** | HoaDonDAO.xuLySpaPassTrongTransaction luôn +1 |
| A07 | P1 | DAO nhận lô hết hạn và vẫn thanh toán thành công. GUI đã có lọc HSD mới, nhưng không có kiểm tra lại khi ghi SQL | HoaDonDAO.sqlTruKho không lọc hanSuDung; ATEXPIREDSALE được ghi trong DB thử |
| A08 | P1 | Truyền lượng xuất -2 qua DAO vẫn ghi được, tồn tăng **2**. Đây là lỗ hổng API/tầng bảo vệ, chưa chứng minh nhập số âm từ GUI hiện tại | HoaDonDAO chỉ kiểm tra hóa đơn có chi tiết/nhân viên; câu UPDATE trừ số âm |
| A09 | P1 | Header thanhTien=1, chi tiết=10.000 vẫn được DAO ghi. Không đối chiếu tổng tiền trước commit | ATMISMATCH trong DB thử; scenario-more |
| A10 | P1 | Tài khoản thu ngân gọi hàm lưu hồ sơ khách có thể ghi đè điểm từ **80 lên 100**; BUS/DAO không bảo vệ role và cập nhật điểm tuyệt đối | BanHangBus.luuHoacCapNhatKhachHang; KhachHangDAO.update; cashier-edge-results |
| A11 | P1 | Hai tiến trình Java độc lập cùng gọi helper cho cùng nhân viên, trước khi ghi hóa đơn, nhận **cùng ID** | MaTuSinhHelper MAX + cache/synchronized chỉ trong một JVM; CodeProbe x2 |
| A12 | P1 | Bán một hộp có 10 đơn vị: Top bán chạy hiển thị **1** với nhãn đơn vị cơ sở, trong khi xuất kho **10** | BaoCaoThongKeDAO.getTopBanChay dùng SUM(ct.soLuong), nhãn sp.donViCoSo; package-mapping-results |
| A13 | P1 | XNT tính cả hàng hỏng không nhập lại kho: báo hoàn **3**, nhưng lượng thực hoàn kho chỉ **2** | COALESCE(NULLIF(soLuongCoSoHoan,0),soLuongDoiTra) biến 0 hợp lệ thành số món trả |
| A14 | P1 | Cập nhật nhân viên 20 lần làm tăng **19 session còn mở** trong khi tiến trình test còn sống | NhanVienDAO finally không close; scenario-more. Tiến trình test kết thúc thì OS giải phóng các connection đó |
| A15 | P2 | Đọc lại hóa đơn sai header đã cố ý tạo: SQL lưu 1, entity trả **10.000**. DAO nạp chi tiết rồi addChiTiet tính lại total; không giữ nguyên số liệu được lưu | HoaDonDAO.getAllHoaDon/findById + HoaDon.addChiTiet/setDsChiTiet |

Không cộng cứng số dòng trên thành “toàn bộ số lỗi hệ thống”: một nhóm có thể trùng căn nguyên, còn điều kiện test fail có thể cùng phản ánh một lỗi. A08/A09 là kiểm thử bảo vệ ở API, không đồng nghĩa cashier đã nhập được giá trị đó bằng GUI.

## 2. Mapping DB hiện hữu sau pull

| Bảng | Thiếu trong DB đang dùng | Hậu quả |
|---|---|---|
| BangGiaoCa | gioBatDauCa, ngayGiaoCa | Ghi giao ca dùng gioBatDauCa lỗi; index ngày còn thiếu |
| ChiTietDoiTra | soLo, soLuongCoSoHoan | Ghi hoàn theo lô và báo cáo XNT lỗi 207 |
| LichSuLuuTru | maHoaDon, trangThaiThanhToan | Checkout Hotel trong POS không tương thích |
| TheSpaPass | maHoaDonKichHoat | Phát hành thẻ qua hóa đơn không tương thích |
| LichSuSuDungSpaPass | Thiếu bảng | Sử dụng thẻ lỗi 208 |

Đã xác nhận bằng metadata và SELECT TOP(0), không chỉ so file SQL. Pull code không tự chạy migration hay đưa dữ liệu ở máy khác về DB trên máy này. Những báo cáo 25/25, 7/7 trong AUDIT_PROGRESS.md nói về môi trường/đường dẫn khác, không phải bằng chứng đã chạy thành công tại đây.

## 3. Kiểm tra script validation và seed

Trên DB hiện hữu: 25 quy tắc chạy riêng từng block: 19 PASS, 3 FAIL, 3 không kiểm chứng được do cột/bảng thiếu. R01 FAIL vì không có bộ seed lớn, không phải lỗi runtime; R04 báo 6 sản phẩm vì so tên đơn vị với mã; R22 xác nhận 2 biên bản sai công thức tiền két.

Trên DB thử sau các ca cố ý thử lỗi và dữ liệu lớn, sau MAXDOP=1: **19 PASS, 6 FAIL**. Đây là dữ liệu đã thử phá, không phải nghiệm thu bộ seed sạch. Có các điểm bộ validation bỏ lọt:

- **R10 luôn INSERT PASS,0**, bất kể @ErrCount đã tính ra. Truy vấn còn JOIN mọi quy đổi của sản phẩm, không dùng đúng quy đổi đã xuất.
- **R14 SUM(ctdt.soLuong)**, trong khi Java ghi soLuongDoiTra. Cột soLuong có thể NULL: ca trả vượt đã tái hiện vẫn PASS; đối chiếu độc lập phát hiện 1 nhóm trả vượt.
- **R21 đếm TongDaDung nhưng không so với soBuoiDaDung**. PASS dù đối chiếu độc lập thấy 10 thẻ seed có counter 2 nhưng chỉ 1 lịch sử mỗi thẻ.
- **R04 so SanPham.donViCoSo với DonViTinh.maDonVi**. App lưu tên Gói/Hộp; seed mới lại lưu DVT01/DVT06. Tạo false positive và không thống nhất hợp đồng dữ liệu. 5.004 sản phẩm bị báo lỗi ở bộ dữ liệu trộn, không phải 5.004 lỗi riêng.
- **R07 chỉ tính trả hàng trạng thái DA_XU_LY**, còn Java mặc định Hoàn thành; bỏ sót hoàn kho của giao dịch ứng dụng.
- **R09 không xét hàng hoàn bán lại**: tổng xuất vượt lượng nhập ban đầu có thể hợp lệ sau khi nhập lại hàng trả.
- **R18 chỉ kiểm tra hồ sơ đã tồn tại**, không phát hiện thú cưng chưa có bất kỳ hồ sơ chủ nào.
- **R24 không kiểm tra vaiTro**, dù nhãn kết quả tuyên bố người duyệt là quản lý.
- **R25 chỉ so tổng hai nguồn SQL**, không chạy Dashboard/BUS/GUI; không thể dùng nó để chứng minh Dashboard đúng.

CleanupDuLieuMau.sql DELETE toàn bộ nhiều bảng, không lọc tiền tố seed. Không chạy script cleanup này trên DB hiện hữu. Bản thân seed dùng ID cố định, không phải idempotent chỉ nhờ có transaction; chạy lại sẽ trùng khóa nếu không có quy trình reset riêng.

## 4. Trải nghiệm trực tiếp của thu ngân

Các quan sát sau thực sự thao tác qua giao diện Windows của bản thử, không suy ra từ compile:

| Công việc | Quan sát | Đánh giá cho người dùng |
|---|---|---|
| Thêm hàng bán lẻ | Nhấn Thêm đưa đúng món/đơn giá vào giỏ | Dễ thao tác; bảng phân biệt Gói/Thùng rõ hơn nếu cửa sổ rộng |
| Khách đưa thiếu tiền | Nút thanh toán chặn và cho biết số tiền thiếu | Hữu ích, không tạo hóa đơn giả |
| Khách đưa đủ tiền | Nút Đủ tiền điền đúng 350.000; thanh toán mở preview | Giảm nhập tay; hóa đơn ghi được và tồn giảm 99 → 98 |
| Điểm khách vãng lai | Preview vẫn ghi “+3 điểm, tổng 3 điểm” dù khách vãng lai không có tài khoản KH | Dễ làm thu ngân hứa nhầm quyền lợi; nên ẩn hoặc ghi chưa tích vì chưa có KH |
| F9 | Nhấn F9 không mở thanh toán; nhấn nút bằng chuột thì hoạt động | Nhãn THÀNH TOÁN (F9) gây kỳ vọng sai; source chỉ có ActionListener, chưa bind F9 |
| Lưu tạm/khôi phục | Đơn 38.000 lưu được, thẻ đơn hiện đúng món/giờ/tổng và khôi phục được | Có ích; mỗi lần lưu/khôi phục có thêm modal OK, cản nhịp phục vụ đông khách |
| Tìm hàng ở 5.054 sản phẩm | Tìm “AUDIT Sản phẩm 4999” ra đúng Gói/Hộp; thao tác type + chụp trạng thái khoảng 311ms | Tra cứu tại quầy tương đối nhanh trong lần thử này; số đo gồm tool/capture, không phải benchmark latency chuẩn |
| Lịch sử 20.459 hóa đơn | Mở được toàn bộ bảng sau tuning; không phân trang, mặc định Tất cả lịch sử | Dễ ngợp; nên mặc định Hôm nay/ca hiện tại, phân trang và tìm phía server |
| Chuyển màn hình | Timer giám sát event dispatch thread ghi một khoảng tắc **3.212ms** | Có cảm giác đứng hình khoảng 3 giây, không phải crash |
| Bàn giao ca | Form cho nhập/đếm tiền rồi đến lúc bấm Chốt mới báo không được phân công CA01 | Bảo vệ quyền là đúng; trải nghiệm chưa tốt vì kiểm tra quá muộn. Nên cảnh báo/khóa ngay khi mở form |
| Tiền đầu ca/doanh thu | Seed có ca GC013 kết thúc 11:30 cùng ngày trong tương lai, trong khi thao tác khoảng 09:38; form lấy mốc đó và hiện thu trong ca 0 | Cần seed theo thời gian hiện tại và loại biên bản tương lai khỏi mốc kế thừa. Đây là lỗi dữ liệu giả phối hợp với giả định code, không chứng minh doanh thu thật bị mất |

Ở cửa sổ nhỏ hơn (~1346px), tên hàng dài bị cắt và dòng giỏ hàng hẹp; ở màn hình rộng dễ đọc hơn. Nên có tooltip/tên đầy đủ ở vùng chi tiết. Không nên coi mọi bản ghi seed trùng tên khách/thú cưng là thực tế sử dụng tốt: bộ seed hiện chưa bao phủ đầy đủ phân biệt thú cưng cùng tên và hồ sơ ID xuyên suốt lịch hẹn → POS.

Ảnh bằng chứng: `.ai-work/audit-224a140/evidence/handover-blocked.png`.

## 5. Tải lớn và chẩn đoán hiệu năng

Bulk thêm 5.000 sản phẩm, 10.000 quy đổi, 15.000 lô, 5.000 khách và 20.000 hóa đơn/chi tiết/xuất lô, giữ cân đối tồn cho các lô bulk. Cùng seed và fixture trước đó: 5.054 sản phẩm, 15.055 lô, 5.051 khách, 20.459 hóa đơn tại mốc benchmark. Bài kiểm tra hộp sau đó thêm 1 hóa đơn nữa. Dữ liệu là giả; các ca lỗi cố ý vẫn nằm trong DB thử, không dùng DB này để chứng minh dữ liệu nghiệp vụ sạch.

Ban đầu truy vấn load chi tiết toàn bộ hóa đơn chờ **RESOURCE_SEMAPHORE ít nhất 134.436ms**, yêu cầu 44.368KB, tối thiểu 8.192KB, chưa được cấp. Helper benchmark được dừng sau khi lưu bằng chứng, không KILL truy vấn người dùng. Không kết luận máy nào cũng gặp cùng độ trễ; đây là ảnh hưởng của plan và tài nguyên SQL Server tại máy thử. Cấu hình server global được đọc, không đổi.

Sau khi đặt **MAXDOP=1 chỉ cho DB thử**, cùng nguồn DAO và dữ liệu:

| Thao tác | Thời gian quan sát |
|---|---:|
| Đọc 5.054 sản phẩm | 363ms |
| Đọc 15.055 lô | 457ms |
| Đọc 5.051 khách | 280ms |
| Đọc 20.459 hóa đơn kèm chi tiết | 1.047ms |
| Báo cáo doanh thu | 587ms |
| Top bán chạy | 378ms |
| XNT | 573ms |
| Nạp lịch sử static POS sau xác thực | 1.570ms |
| Tạo/hiện Dashboard thu ngân | 4.086ms |

Số đo là một lượt trên máy này, có cache và lúc khởi động UI/benchmark cùng hoạt động; không phải p95/p99 hay SLA. Không đưa MAXDOP=1 lên DB chính như một sửa chữa mặc định. Cần xem plan, index, tài nguyên SQL; đồng thời đưa truy vấn khỏi Swing EDT, thêm timeout/cancel và phân trang để quầy không treo khi SQL chậm.

## 6. Các điểm còn trong code cần sửa/kiểm chứng thêm

- 7 hàm ghi ở ThuCungDAO (2), NhanVienDAO (3), SanPhamDAO.delete, LichHenDAO.delete chưa close connection. Nhóm nhân viên đã đo runtime.
- ThongKeBus còn dùng 23:59:59 với `< den`, bỏ cuối ngày; getHieuSuatNhanSu so DATE(tu) ≤ ngayLamViec < DATE(den) nên báo cáo một ngày không có ca.
- Báo cáo nhân sự cộng doanh thu thu ngân và dịch vụ KTV; không được cộng các dòng như tổng doanh thu cửa hàng khi một dịch vụ được tính cho hai người.
- KTV dịch vụ hiện được suy từ chuỗi tên dòng, rồi chọn nhân viên Spa đầu tiên nếu không tìm thấy; không giữ ID phân công xuyên suốt. ThuCung vẫn null trong chi tiết POS. FK khớp không có nghĩa nghiệp vụ đúng.
- Lịch hẹn chưa được liên kết ID vào transaction POS để cập nhật trạng thái hoàn thành; đặt lịch chưa chống race bằng transaction/ràng buộc server.
- Helper sinh thẻ còn COUNT để sinh số; xóa thẻ hoặc hai JVM có thể trùng ID; cần ràng buộc/reservation ở DB.
- ThietLapQuyDoiDialog bỏ qua boolean false của BUS sau khi BUS bắt SQLException; vẫn có thể báo thành công giả.
- Kiểm kê ghi đè tồn chưa có sổ biến động; XNT lịch sử không thể khôi phục các điều chỉnh đó. Math.max(0,...) có thể che sai lệch.
- Hoàn tiền đổi hàng đang trộn giá trị hàng trả với khoản tiền thực chi dư, trong khi bàn giao lọc phương thức theo hóa đơn gốc. Cần test đủ tổ hợp đổi hàng thấp/cao hơn và hoàn qua kênh khác, định nghĩa rõ ledger tiền thực chi.
- Chưa có giới hạn thử sai đăng nhập/OTP, độ dài mật khẩu 4 và 6 chưa thống nhất. Chưa kiểm thử gửi email/OTP end-to-end vì tắt kênh thật.
- RBAC chủ yếu ở GUI; không coi ma trận nút bấm là bằng chứng BUS/DAO độc lập bảo vệ mọi mutation.
- PetHotelBus/PhongLuuTruDAO vẫn phụ thuộc inner class GUI; đây là nợ kiến trúc, không phải lỗi compile.

## 7. Đối chiếu lại 49 mục cũ sau pull

Không dùng bảng này để tuyên bố mọi mục “đã được chạy UI”. “Code” là xác minh tĩnh; “DB thử” là có chạy và kiểm tra kết quả SQL.

| ID | Kết luận ở 224a140 |
|---|---|
| BLK-01 | UDF cũ vẫn có trong DB hiện hữu; POS dùng Java helper nên không kết luận UI dừng ở đơn 100 |
| BLK-02 | GUI đã lọc HSD; DAO còn cho xuất hết hạn, đã tái hiện DB thử |
| BLK-03 | DB hiện hữu lỗi schema; DB thử XNT còn tính nhầm hàng hỏng là hoàn kho |
| BLK-04 | Còn 2 hàm ThuCungDAO thiếu close (code) |
| BLK-05 | Còn 3 hàm NhanVienDAO thiếu close; 20 update tăng 19 session |
| BLK-06 | deleteSanPham thiếu close (code) |
| BLK-07 | deleteLichHen thiếu close (code) |
| CRI-01 | DAO login Java đã lọc active; procedure cũ trên DB hiện hữu chưa lọc, không thấy Java gọi nó |
| CRI-02 | Trừ điểm transaction đạt; nhưng lưu hồ sơ trước/ngoài transaction vẫn ghi đè điểm |
| CRI-03 | UDF lịch hẹn cũ còn rủi ro; Java UI dùng helper khác; probe DB hiện hữu hiện chạy được |
| CRI-04 | Tương tự UDF phiếu trả; chưa tái hiện lỗi ngày mới bằng sửa dữ liệu DB hiện hữu |
| CRI-05 | Bản mới gộp đơn vị nhưng không quy đổi: 1 hộp xuất 10 vẫn báo 1 đơn vị cơ sở |
| CRI-06 | Đã đổi cách tách lô, nhưng nhánh không tròn hộp tạo maQuyDoi null, SQL 515 |
| CRI-07 | Chưa có logic phụ thu theo duKienCheckOut; cần chốt chính sách |
| CRI-08 | Transaction Hotel đã gộp; rollback đạt, còn lỗi ngày/unique nhiều phòng |
| CRI-09 | Transaction Spa đã gộp; không còn sửa RAM trước DB theo mô tả cũ; số lượt còn sai |
| CRI-10 | NSX/HSD NOT NULL; đọc FEFO đạt; không tái hiện NPE như mô tả cũ |
| CRI-11 | Đã dùng try-with-resources ở thanh toán |
| CRI-12 | Code hoàn kho theo lô đã có; DB hiện hữu thiếu cột; DB thử hoàn hàng hỏng không tăng kho đạt |
| MAJ-01 | Chưa thấy giới hạn thử sai đăng nhập (code) |
| MAJ-02 | UserSession guest không có quyền; contract 8 card guest-denied đạt |
| MAJ-03 | API BUS vẫn đơn lô, GUI có nhánh đa lô riêng chưa chạy được nhánh pack lẻ |
| MAJ-04 | Hóa đơn/phiếu trả đã chung transaction; rollback đạt trong các ca đã chạy |
| MAJ-05 | Hai tiến trình sinh cùng ID, đã tái hiện |
| MAJ-06 | Báo cáo ca một ngày còn sai biên DATE (code) |
| MAJ-07 | Còn cần tách định nghĩa doanh thu phụ trách/KTV, không cộng trùng như tổng cửa hàng |
| MAJ-08 | Đã bỏ fallback 42% trong bản pull mới |
| MAJ-09 | Nhập kho đã dùng mapping ID và transaction; chưa thao tác ghi toàn bộ UI nhập kho |
| MAJ-10 | Mã mới đã đổi; dialog vẫn bỏ qua boolean false (code) |
| MAJ-11 | Phiếu nhập/chứng từ chưa có; là thiếu tính năng cần yêu cầu nghiệp vụ |
| MAJ-12 | UI lọc slot, server chưa khóa va chạm đồng thời (code) |
| MAJ-13 | Dialog đặt lịch đã đọc giá DichVuDAO; POS fallback khi không map được vẫn cứng DV001/300000 |
| MAJ-14 | Đã tra khách từ DB |
| MAJ-15 | Dùng lại thú theo tên/chủ; còn nguy cơ gộp nhầm hai bé cùng tên |
| MAJ-16 | Dịch vụ qty2 chỉ trừ Spa 1, đã tái hiện |
| MAJ-17 | Chưa giữ maLichHen trong transaction thanh toán (code) |
| MAJ-18 | DAO ghi IDENTITY và lấy generated key; lỗi PC theo size không còn là khóa chính DB |
| MAJ-19 | Nhiều dialog nhân sự thiếu nhánh lỗi khi BUS false (code) |
| MAJ-20 | Còn import inner class GUI (nợ kiến trúc) |
| MAJ-21 | UDF DB hiện hữu chưa đồng bộ; helper Java TRY_CONVERT không sửa các UDF đó |
| MAJ-22 | Thiếu trigger không tự là lỗi: đã có CHECK và Java transaction; không thêm trigger cộng/trừ mù |
| MAJ-23 | Chưa pooling; tách rủi ro hiệu năng khỏi connection leak đã đo |
| MIN-01 | Độ dài password 4/6 chưa thống nhất |
| MIN-02 | Trả trùng hai quầy đã tái hiện; cần kiểm tra số dư trả trong transaction |
| MIN-03 | Biên ngày 23:59:59 còn thiếu |
| MIN-04 | POS đã nạp lịch sử từ DB; nhận định mở lại app luôn 0 không còn đúng chung |
| MIN-05 | congTonKho không chặn tham số âm (code) |
| MIN-06 | ThuCungBus còn fallback KH001 ở một số nhánh (code) |
| MIN-07 | POS dialog mới có báo FK khi xóa; panel CRM cũ còn thiếu nhánh báo lỗi |

## 8. Tài liệu và bằng chứng để tiếp tục

Trong `.ai-work/audit-224a140/`:

- `sql-evidence.txt`, `dao-evidence.txt`, `live-checks.txt`: DB hiện hữu chỉ đọc sau pull.
- `sandbox-setup.txt`: tạo DB riêng; lỗi khởi tạo index 10609 và migration sau đó thành công.
- `sandbox-app/scenario-results.txt`: 10 điều kiện đầu; không có dòng TOTAL do lỗi fixture đã giải thích.
- `sandbox-app/scenario-more.txt`: 8 điều kiện tiếp và tải 400 giao dịch.
- `sandbox-app/cashier-edge-results.txt`: 5 điều kiện, gồm 2 tiến trình sinh ID.
- `sandbox-app/package-mapping-results.txt`: 2 điều kiện hộp/XNT.
- `sandbox-app/bulk-baseline.txt`, `sql-waits.txt`, `bulk-benchmark.txt`, `sql-diagnostics.txt`: số liệu tải trước/sau thử MAXDOP.
- `sandbox-app/sandbox-validation.txt`: 25 rule và đối chiếu độc lập sau thử phá; `validation-before-maxdop.txt` ghi các timeout trước tuning.
- `sandbox-app/ui-large-output.txt`: UI_READY và EDT_STALL. `ui-large-error.txt`: lỗi console của app thử.
- `bridge-tests.txt`: 27 test cầu nối đạt ngoài sandbox; lỗi EPERM của lượt chạy trong sandbox không phải lỗi sản phẩm.
- `Mo-Quay-Thu-Ngan-Thu.cmd`: mở lại ứng dụng thử, dùng lớp nguồn đã compile và config riêng. Tên cửa sổ có MIO AUDIT ONLY. Không dùng launcher này cho DB thật.

Chưa chạy thanh toán ngân hàng thật, gửi SMTP thật, in máy in vật lý, hàng triệu hóa đơn, soak test nhiều giờ hoặc mọi tổ hợp nghiệp vụ. Không có bộ kiểm thử hữu hạn nào bảo đảm phát hiện mọi lỗi có thể tồn tại. Phạm vi trên đủ tái hiện các lỗi trọng yếu và chỉ ra bước sửa/nghiệm thu tiếp theo.

## Thứ tự sửa đề xuất

1. Khôi phục mật khẩu; đồng bộ schema DB hiện hữu bằng migration được kiểm thử; chặn ID trùng và trả hàng trùng ở transaction.
2. Sửa maQuyDoi của tách lô, số lượt Spa, số ngày/phòng Hotel, chống quantity/total sai ở DAO; đóng connection.
3. Sửa XNT hàng hỏng, đơn vị Top bán chạy, nạp lại total từ DB và ghi đè điểm hồ sơ; giữ ID KTV/thú cưng/lịch hẹn xuyên suốt.
4. Đưa SQL ra khỏi EDT, thêm timeout/cancel/phân trang; xem plan/tài nguyên SQL trước khi quyết định cấu hình sản xuất.
5. Sửa validation để fail đúng; thêm F9 thật, giảm modal đơn chờ, cảnh báo phân ca sớm và nhãn điểm khách vãng lai.

Chưa tự sửa nguồn ứng dụng hoặc nâng cấp DB hiện hữu trong audit này.
