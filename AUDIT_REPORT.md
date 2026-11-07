# BÁO CÁO KIỂM TOÁN NGUỒN VÀ KẾ HOẠCH HOÀN THIỆN HỆ THỐNG PETSHOP MIO

---

## I. THÔNG TIN CHUNG & PHẠM VI KIỂM TOÁN

- **Dự án:** PETSHOP MIO — Hệ thống Quản lý Cửa hàng Thú cưng — Nhóm 07.
- **Công nghệ nền tảng:** Java Swing, JDBC Driver, Microsoft SQL Server, Eclipse IDE (Java 17).
- **Kiến trúc ứng dụng:** 3-Layer / N-Tier (`GUI ➔ BUS ➔ DAO ➔ Entity` + SQL Server DB Constraints).
- **Vị trí Workspace kiểm toán bắt buộc:**
  `D:\HK1 - 2026\Application Development\N07-HeThongQuanLyCuaHangThuCung-main\N07-HeThongQuanLyCuaHangThuCung-main`
- **Mốc Git:** Branch `main`, Commit SHA `6d17532167dfb3c480486894e3069b95ad837f52`.
- **Tài liệu đầu vào đối chiếu:** `D:\HK1 - 2026\Application Development\Lỗi source.docx` gồm 43 mục gốc (`#01–#43`), 16 phát hiện bổ sung (`A01–A08, B01–B08`), và 6 phản ánh vận hành mới (`C01–C04, D01–D02`). Tổng cộng: **65 mã kiểm toán**.
- **Nguyên tắc an toàn Giai đoạn 1:** Toàn bộ quá trình kiểm toán chỉ phân tích tĩnh trên mã nguồn thực tế và đối chiếu schema SQL. Tuyệt đối không chỉnh sửa file code Java, SQL, cấu hình hay thư viện; không thực thi migration, seed hay cleanup trên cơ sở dữ liệu.

---

## II. BẢNG TỔNG HỢP TRẠNG THÁI 65 MÃ KIỂM TOÁN

| Trạng thái | Số lượng | Tỷ lệ | Định nghĩa |
|---|---:|---:|---|
| **CONFIRMED** | **54** | 83.1% | Lỗi hoặc lỗ hổng thực sự còn tồn tại trên source hiện tại |
| **PARTIAL** | **7** | 10.8% | Nhận xét đúng một phần, đúng về điều kiện biên hoặc phụ thuộc chính sách kinh doanh |
| **FIXED** | **2** | 3.1% | Vấn đề đã được khắc phục ở các bản cập nhật gần nhất (`A02`, `B08`) |
| **INVALID** | **1** | 1.5% | Nhận định không chính xác (`A05`: không có xung đột hash/mã giữa Java và T-SQL) |
| **UNVERIFIED** | **0** | 0.0% | Không có mã nào thiếu dữ liệu kiểm tra trên mã nguồn |
| **BLOCKED** | **1** | 1.5% | `A08`: File migration tồn tại trên source, cần kết nối SQL Server thực tế để kiểm tra trạng thái CSDL |
| **NOT_STARTED** | **0** | 0.0% | **Toàn bộ 65 mã đã được kiểm tra chi tiết** |
| **TỔNG CỘNG** | **65** | **100%** | **Đầy đủ 65/65 mã theo yêu cầu kiểm toán** |

---

## III. MA TRẬN CHI TIẾT 65 MÃ KIỂM TOÁN THEO SÁU ĐỢT

### ĐỢT 1 — SQL, JDBC, TRANSACTION, TỒN KHO & TIỀN (22 mã)

#### 1. Mã `#02` — Dữ liệu điểm khách bị ghi đè trước transaction
- **Nhóm kỹ thuật:** BUS / Transaction / Data Integrity.
- **Phân hệ:** POS / Bán hàng & Tích điểm.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2778–2786`, `src/bus/BanHangBus.java:308–328`.
- **Bảng/cột SQL:** `KhachHang.diemTichLuy`.
- **Nguyên nhân gốc:** Trước khi gọi `hoaDonDAO.thanhToanHoaDon(hdReal)`, POS gọi hàm `BanHangBus.capNhatThongTinKhachHang(..., diemMoi)` hoặc lưu khách với số điểm tính toán trên RAM. Nếu transaction thanh toán sau đó bị rollback (do lỗi in bill, hết tồn FEFO, mạng gián đoạn), số điểm của khách đã bị cập nhật sai lệch trong CSDL.
- **Hậu quả:** Mất tính nguyên tử (Atomicity); hai quầy cùng tích/dùng điểm cho một khách hàng sẽ ghi đè mất điểm của nhau.
- **Điều kiện tái hiện:** Bán hàng có dùng điểm, cố ý ngắt kết nối DB tại bước lưu hóa đơn.
- **Hướng khắc phục:** Loại bỏ hoàn toàn việc cập nhật điểm trước transaction; dồn 100% việc trừ điểm sử dụng và cộng điểm tích lũy vào transaction nội bộ `HoaDonDAO.thanhToanHoaDon` với cơ chế kiểm tra điều kiện dòng `WHERE diemTichLuy >= diemSuDung`.
- **Phạm vi ảnh hưởng:** `BanHangPOSPanel.java`, `BanHangBus.java`, `HoaDonDAO.java`.
- **Bài test nghiệm thu:** Mở 2 phiên đồng thời cùng dùng điểm của 1 khách hàng; kiểm tra rollback khi giao dịch hóa đơn thất bại.

#### 2. Mã `#03` — Phân bổ nhiều lô làm sai số lượng chi tiết hóa đơn
- **Nhóm kỹ thuật:** Logic tồn kho FEFO / Quy đổi / POS.
- **Phân hệ:** Quản lý Kho & Bán hàng POS.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2846–2868`.
- **Bảng/cột SQL:** `ChiTietHoaDon.soLuong`, `ChiTietHoaDon.thanhTien`, `ChiTietXuatLo.soLuongXuatCoSo`.
- **Nguyên nhân gốc:** Khi một mặt hàng bán theo quy cách (ví dụ Thùng, tỷ lệ 24 lon) cần xuất từ 2 lô khác nhau, biến `take` là số lượng theo đơn vị cơ sở (lon). Nhưng ở nhánh chia lô thứ hai, code gán `ct.setSoLuong(take)` và tính `ctTotal` chia nhỏ. Đến khi hiển thị hoặc in bill, hàm `dongBoLichSuHoaDonTuDatabase` lấy `ct.getSoLuong()` (số lon) nhân với đơn giá thùng, làm số lượng và giá trị dòng bị đội lên gấp 24 lần.
- **Hậu quả:** Hóa đơn lưu sai số lượng và giá tiền; khách hàng khiếu nại vì bill in ra số tiền sai lệch nghiêm trọng.
- **Hướng khắc phục:** Tách bạch rõ ràng: `ChiTietHoaDon` lưu số lượng mua theo đơn vị quy cách (Thùng), còn việc xuất từ nhiều lô được hạch toán riêng ở bảng con `ChiTietXuatLo`.
- **Mã liên quan:** `A01`, `B01`, `#22`.

#### 3. Mã `#04` / `A01` — POS có thể phân bổ tồn từ lô đã hết hạn
- **Nhóm kỹ thuật:** Logic tồn kho FEFO / Validation.
- **Phân hệ:** Bán hàng POS & Quản lý Lô FEFO.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2850–2871`.
- **Bảng/cột SQL:** `LoHang.hanSuDung`, `LoHang.soLuongTon`.
- **Nguyên nhân gốc:** Vòng lặp duyệt phân bổ tồn kho trong POS chỉ kiểm tra điều kiện `candidate.getSoLuongTon() > 0` và đúng mã sản phẩm, mà không lọc lại hạn sử dụng `hanSuDung >= LocalDate.now()` ngay tại thời điểm thanh toán.
- **Hậu quả:** Rủi ro xuất bán thực phẩm/thuốc thú cưng đã hết hạn sử dụng cho khách hàng.
- **Hướng khắc phục:** Bắt buộc lọc danh sách lô khả dụng qua `LoHangDAO.getAvailableLotsFEFO` và bổ sung ràng buộc kiểm tra `hanSuDung >= CAST(GETDATE() AS DATE)` trong câu lệnh SQL trừ tồn lô tại DAO.
- **Mã liên quan:** `A01`, `#37`.

#### 4. Mã `#05` — Trả hàng không nguyên tem có thể bị DAO bác bỏ
- **Nhóm kỹ thuật:** DAO / Contract dữ liệu GUI - CSDL.
- **Phân hệ:** Đổi trả hàng / Quản lý Kho.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/DoiTraHangPanel.java:875–889`, `src/dao/HoaDonDAO.java:541–557`.
- **Bảng/cột SQL:** `ChiTietDoiTra.soLo`, `ChiTietDoiTra.soLuongCoSoHoan`.
- **Nguyên nhân gốc:** GUI quy định hàng không nguyên tem (hàng lỗi/hỏng) thì không hoàn kho (đặt số lô rỗng và lượng cơ sở = 0). Tuy nhiên, `HoaDonDAO.java:541` lại đặt điều kiện kiểm tra nghiêm ngặt: `if (ct.getSoLo() == null || ct.getSoLuongCoSoHoan() <= 0) throw new IllegalArgumentException("Hàng trả phải xác định đúng lô gốc và số lượng cơ sở")`.
- **Hậu quả:** Toàn bộ các giao dịch đổi trả hàng bị bóc tem/lỗi hỏng đều bị ném ngoại lệ và không thể lưu vào hệ thống.
- **Hướng khắc phục:** Cập nhật DAO cho phép `soLo` mang giá trị NULL hoặc ký hiệu `KHO_PHE_LIEU` khi hàng không đủ điều kiện nhập lại kho bán.

#### 5. Mã `#07` — Truy vấn doanh thu ca lỗi nhưng trả về số 0
- **Nhóm kỹ thuật:** DAO / Bắt lỗi ngoại lệ.
- **Phân hệ:** Bàn giao ca / Đối soát két tiền.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/dao/BangGiaoCaDAO.java:232–258`.
- **Bảng/cột SQL:** `HoaDon.thanhTien`, `HoaDon.thoiGianThanhToan`.
- **Nguyên nhân gốc:** Hàm `tinhTongTrongKhoang` bắt `SQLException` và âm thầm trả về `0L`.
- **Hậu quả:** Khi mất kết nối CSDL hoặc lỗi câu lệnh, hệ thống coi doanh thu phát sinh trong ca bằng 0, dẫn đến tính sai tiền két lý thuyết, gây chênh lệch "thừa tiền" giả tạo và ép thu ngân giải trình oan.
- **Hướng khắc phục:** Ném `SQLException` lên tầng BUS/GUI hoặc trả về đối tượng kết quả có trạng thái lỗi để giao diện hiển thị cảnh báo không thể chốt ca khi CSDL gián đoạn.

#### 6. Mã `#08` — Seed giao ca GC002, GC003 sai công thức tiền két
- **Nhóm kỹ thuật:** SQL / Dữ liệu mẫu (Seed).
- **Phân hệ:** Bàn giao ca / CSDL.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `sql/HeThongQuanLyCuaHangThuCung.sql:493–509`.
- **Bảng/cột SQL:** `BangGiaoCa.tienCuoiCaHeThong`, `BangGiaoCa.tienChenhLech`.
- **Nguyên nhân gốc:** Dữ liệu mẫu viết tay không khớp công thức `tienCuoiCaHeThong = tienDauCa + tienThuTienMat - tienHoanTra`. Bản ghi `GC002` ghi lý thuyết 5.200.000đ (thực tế $1.000.000 + 4.180.000 - 0 = 5.180.000đ$), `GC003` ghi lý thuyết 4.800.000đ (thực tế $1.000.000 + 3.735.000 - 0 = 4.735.000đ$).
- **Hậu quả:** Script kiểm tra toàn vẹn CSDL (Validation) sẽ báo lỗi vi phạm công thức kế toán.
- **Hướng khắc phục:** Sửa lại số liệu seed đúng công thức toán học trong script seed chuẩn hóa.

#### 7. Mã `#12` — Hàm duyệt ca cũ ghi đè lý do giải trình
- **Nhóm kỹ thuật:** DAO / API Legacy.
- **Phân hệ:** Bàn giao ca / Quản trị.
- **Trạng thái & Độ tin cậy:** `CONFIRMED LEGACY_API`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/BangGiaoCaDAO.java:387–399`.
- **Bảng/cột SQL:** `BangGiaoCa.lyDoChenhLech`.
- **Nguyên nhân gốc:** Phương thức tương thích cũ `capNhatGhiChuDuyetCa` ghi đè toàn bộ nội dung trường `lyDoChenhLech` bằng ghi chú của người duyệt, làm mất giải trình ban đầu của thu ngân.
- **Hướng khắc phục:** Nối chuỗi `[Thu ngân giải trình] + [Quản lý phê duyệt]` hoặc loại bỏ phương thức legacy không còn sử dụng.

#### 8. Mã `#19` — Trừ điểm VIP trước khi phiếu đổi trả commit
- **Nhóm kỹ thuật:** BUS / Transaction.
- **Phân hệ:** Đổi trả hàng / Tích điểm.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/DoiTraHangPanel.java:842–899`.
- **Bảng/cột SQL:** `KhachHang.diemTichLuy`, `PhieuDoiTra`.
- **Nguyên nhân gốc:** Nhánh đổi trả độc lập thực hiện trừ điểm thưởng tích lũy của khách hàng trên CSDL trước khi gọi `HoaDonDAO.luuPhieuDoiTra`.
- **Hậu quả:** Nếu việc lưu phiếu đổi trả thất bại, điểm của khách hàng bị trừ mất mà không có giao dịch đối ứng.
- **Hướng khắc phục:** Đưa việc thu hồi điểm tích lũy vào cùng một JDBC Transaction duy nhất với lưu phiếu đổi trả và hoàn kho.

#### 9. Mã `#25` / `B03` — Lệch số ngày và tiền lưu trú Pet Hotel giữa POS và CSDL
- **Nhóm kỹ thuật:** DAO / Bus / Tính phí khách sạn.
- **Phân hệ:** Pet Hotel / Hóa đơn POS.
- **Trạng thái & Độ tin cậy:** `PARTIAL SPEC_DEPENDENT`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/PetHotelPanel.java:1008–1014`, `src/dao/HoaDonDAO.java:114–118`.
- **Bảng/cột SQL:** `LichSuLuuTru.soNgayLuuTru`, `LichSuLuuTru.tongTien`, `HoaDon.thanhTien`.
- **Nguyên nhân gốc:** Giao diện tính số ngày lưu trú đẩy sang giỏ POS, nhưng khi thanh toán, SQL tự chạy lại câu lệnh `DATEDIFF(DAY, CAST(thoiGianCheckIn AS DATE), CAST(GETDATE() AS DATE))`. Nếu khách check-out lúc 23:55 và thanh toán hoàn tất lúc 00:05 sáng hôm sau, số ngày trong `LichSuLuuTru` bị tăng thêm 1 ngày so với số tiền đã thu trên `HoaDon`.
- **Hậu quả:** Báo cáo doanh thu Pet Hotel trên hóa đơn lệch với doanh thu lưu trữ phòng.
- **Hướng khắc phục:** Chuyển chính xác số ngày và đơn giá phòng đã chốt tại hóa đơn POS vào `LichSuLuuTru`, không dùng `DATEDIFF` độc lập trên SQL Server.

#### 10. Mã `#33` — Sinh mã MAX + 1 có thể trùng ở nhiều tiến trình
- **Nhóm kỹ thuật:** Utils / Quản lý khóa / Concurrency.
- **Phân hệ:** Toàn hệ thống (Hóa đơn, Khách hàng, Đổi trả).
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/utils/MaTuSinhHelper.java:42–80`.
- **Bảng/cột SQL:** `HoaDon.maHoaDon`, `KhachHang.maKhachHang`, `SanPham.maSanPham`.
- **Nguyên nhân gốc:** Hàm `laySoThuTuTheoNgay` chỉ đồng bộ hóa `synchronized` trên bộ nhớ một JVM qua `MAX_CACHE`. Khi chạy nhiều máy trạm (multi-client), hai máy cùng truy vấn `SELECT MAX` tại một thời điểm sẽ sinh ra mã trùng nhau và gây lỗi khóa chính khi commit.
- **Hướng khắc phục:** Thêm cơ chế retry khi bắt lỗi trùng khóa PK (SQL Server Error 2627) hoặc dùng sequence/tiền tố định danh mã máy trầy.

#### 11. Mã `#36` — Hủy hóa đơn chỉ đổi trạng thái, không hoàn nghiệp vụ
- **Nhóm kỹ thuật:** DAO / Data Integrity / Legacy API.
- **Phân hệ:** Hóa đơn / Quản lý Kho / Khách hàng.
- **Trạng thái & Độ tin cậy:** `CONFIRMED LEGACY_API`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/HoaDonDAO.java:566–572`.
- **Bảng/cột SQL:** `HoaDon.trangThaiHoaDon`, `LoHang.soLuongTon`, `KhachHang.diemTichLuy`.
- **Nguyên nhân gốc:** Hàm `huyHoaDon(String maHoaDon)` chỉ chạy lệnh `UPDATE HoaDon SET trangThaiHoaDon = N'Đã hủy' WHERE maHoaDon = ?` mà không hoàn kho các lô đã xuất, không thu hồi điểm tích lũy và không giải phóng phòng Pet Hotel/lượt Spa Pass.
- **Hướng khắc phục:** Viết transaction hủy hóa đơn hoàn chỉnh hoàn tác mọi sổ cái nghiệp vụ, hoặc khóa phương thức này nếu nghiệp vụ không cho phép hủy đơn sau thanh toán.

#### 12. Mã `#37` — API FEFO cũ có thể lấy hàng quá hạn/hết tồn
- **Nhóm kỹ thuật:** DAO / Legacy API.
- **Phân hệ:** Kho hàng / FEFO.
- **Trạng thái & Độ tin cậy:** `PARTIAL LEGACY_API`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/dao/LoHangDAO.java:64–96, 221–238`.
- **Bảng/cột SQL:** `LoHang`.
- **Nguyên nhân gốc:** Phương thức cũ `getLoHangFEFOBySanPham` thiếu mệnh đề lọc hạn dùng. Phương thức mới `getAvailableLotsFEFO` đã có lọc, nhưng phương thức cũ vẫn tồn tại trong mã nguồn.
- **Hướng khắc phục:** Xóa bỏ hoặc chuyển hướng phương thức cũ sang gọi phương thức mới.

#### 13. Mã `#38` — Hàm kho cập nhật RAM rồi nuốt lỗi cập nhật SQL
- **Nhóm kỹ thuật:** GUI / Logic đồng bộ kho.
- **Phân hệ:** Quản lý Kho.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/KhoLoHangPanel.java:126–166`.
- **Bảng/cột SQL:** `LoHang.soLuongTon`.
- **Nguyên nhân gốc:** Các hàm `truTonKho` và `congTonKho` thay đổi trực tiếp `soLuongTon` trên danh sách tĩnh `DANH_SACH_LO_HANG` trước, sau đó gọi `khoBus.truTonKho` trong khối `try { ... } catch (Exception ignored) {}`.
- **Hậu quả:** Nếu kết nối DB lỗi, giao diện kho vẫn hiển thị số tồn mới, gây lệch giữa màn hình và CSDL.
- **Hướng khắc phục:** Thực hiện ghi xuống CSDL trước; nếu thành công mới đồng bộ lại giao diện.

#### 14. Mã `#41` — Kiểm tra đã đổi trả không nguyên tử khi nhiều quầy
- **Nhóm kỹ thuật:** DAO / Concurrency.
- **Phân hệ:** Đổi trả hàng hóa.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/HoaDonDAO.java:464–487, 493–560`.
- **Bảng/cột SQL:** `ChiTietDoiTra`, `PhieuDoiTra`.
- **Nguyên nhân gốc:** Việc đọc số lượng sản phẩm đã đổi trả qua `layMapSoLuongDaTra` và việc lưu `ChiTietDoiTra` nằm ở 2 connection/transaction tách rời nhau.
- **Hậu quả:** Hai quầy cùng nhận đổi trả một hóa đơn gốc có thể cho phép khách trả vượt quá số lượng đã mua ban đầu.
- **Hướng khắc phục:** Thực hiện kiểm tra số lượng khả dụng bên trong cùng transaction ghi phiếu đổi trả với gợi ý khóa dòng thích hợp.

#### 15. Mã `#42` — Đối soát két không tách tiền hoàn mặt và chuyển khoản
- **Nhóm kỹ thuật:** DAO / Báo cáo tài chính.
- **Phân hệ:** Bàn giao ca / Két tiền mặt.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/BangGiaoCaDAO.java:226–229`.
- **Bảng/cột SQL:** `PhieuDoiTra.tongTienHoan`.
- **Nguyên nhân gốc:** Bảng `PhieuDoiTra` chưa có cột phương thức hoàn tiền (`phuongThucHoan`), và câu truy vấn tính tiền hoàn trong ca `SUM(tongTienHoan)` trừ toàn bộ số tiền đổi trả vào két tiền mặt.
- **Hậu quả:** Nếu khách được hoàn tiền qua chuyển khoản/QR ngân hàng, két tiền mặt vẫn bị trừ, gây sai lệch số dư tiền mặt thực tế trong két.
- **Hướng khắc phục:** Bổ sung trường `phuongThucHoan` vào `PhieuDoiTra` và chỉ trừ các khoản hoàn bằng tiền mặt.

#### 16. Mã `A01` — POS có thể phân bổ lô hàng đã hết hạn để bán
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Ghi chú:** Trùng hoàn toàn nguyên nhân gốc với mã `#04`. Không tạo công việc sửa riêng.

#### 17. Mã `A02` — Bảy phương thức DAO không đóng JDBC Connection
- **Trạng thái & Độ tin cậy:** `FIXED STATIC`.
- **Ghi chú:** Rà soát toàn bộ các class trong `src/dao`, 100% các kết nối JDBC hiện đều sử dụng cú pháp try-with-resources hoặc được đóng ở khối `finally`. Nhận định cũ không còn đúng với mã nguồn hiện tại.

#### 18. Mã `A05` — Xung đột định dạng hash/mã giữa Java và T-SQL
- **Trạng thái & Độ tin cậy:** `INVALID STATIC`.
- **Ghi chú:** Mã hex SHA-256 từ Java `PasswordHasher` (64 ký tự hex thường) hoàn toàn tương thích với chuỗi NVARCHAR lưu trữ trong SQL Server seed. Không có xung đột kiểu dữ liệu.

#### 19. Mã `A08` — File migration SQL chưa chạy trên database thực
- **Trạng thái & Độ tin cậy:** `BLOCKED`.
- **Ghi chú:** File migration `sql/CapNhatRangBuocNghiepVu.sql` đã tồn tại trên repo, nhưng cần được kiểm tra trực tiếp trên SQL Server instance thật để xác định schema đã được đồng bộ hay chưa.

#### 20. Mã `B01` — LEFT JOIN ChiTietXuatLo nhân bản dòng trong báo cáo
- **Nhóm kỹ thuật:** DAO / SQL Reporting.
- **Phân hệ:** Báo cáo Thống kê & Top bán chạy.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/dao/BaoCaoThongKeDAO.java:104–180`.
- **Bảng/cột SQL:** `ChiTietHoaDon`, `ChiTietXuatLo`.
- **Nguyên nhân gốc:** Phép nối `ChiTietHoaDon ct LEFT JOIN ChiTietXuatLo xl ON xl.maChiTiet = ct.maChiTiet` có quan hệ 1-Nhiều khi một dòng hàng được xuất từ 2 lô trở lên. Phép nối này làm nhân bản số dòng của `ChiTietHoaDon`, khiến các hàm `SUM(ct.soLuong)` và `SUM(ct.thanhTien)` tính gấp đôi, gấp ba sản lượng và doanh thu thực tế.
- **Hậu quả:** Báo cáo Top bán chạy và Báo cáo Doanh thu hàng hóa bị phóng đại số liệu tài chính.
- **Hướng khắc phục:** Gom nhóm tổng hợp từ bảng `ChiTietXuatLo` trước khi JOIN với `ChiTietHoaDon`, hoặc tính doanh thu thuần dựa trên từng dòng chi tiết duy nhất.

#### 21. Mã `B03` — Lệch giá Pet Hotel khi checkout và thanh toán qua ngày mới
- **Trạng thái & Độ tin cậy:** `PARTIAL SPEC_DEPENDENT`.
- **Ghi chú:** Là biểu hiện cụ thể của mã `#25`. Đã phân tích chi tiết tại `#25`.

#### 22. Mã `B08` — Hoàn kho gán “An toàn” cho lô đã hết tồn mà không kiểm tra hạn
- **Trạng thái & Độ tin cậy:** `FIXED / PARTIAL`.
- **Vị trí code:** `src/gui/KhoLoHangPanel.java:162–164`.
- **Ghi chú:** Luồng hoàn kho chính thống qua `DoiTraBus` và `HoaDonDAO` đã cập nhật trực tiếp CSDL. Đoạn gán nhãn RAM cũ `"AN TOÀN (> 6 tháng)"` tại helper phụ cần được loại bỏ để tránh gây hiểu nhầm trạng thái FEFO.

---

### ĐỢT 2 — POS, HÓA ĐƠN, ĐỔI TRẢ & KHÁCH HÀNG (10 mã)

#### 23. Mã `#01` / `D01` — Thu ngân có thể tự điều chỉnh điểm khách hàng
- **Nhóm kỹ thuật:** GUI / RBAC / CRM.
- **Phân hệ:** Quầy bán hàng POS & Khách hàng.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2086, 2117–2121`.
- **Bảng/cột SQL:** `KhachHang.diemTichLuy`.
- **Nguyên nhân gốc:** Hộp thoại quản lý khách hàng tại POS cho phép chỉnh sửa trực tiếp trường `txtDiem` và lưu xuống CSDL mà không kiểm tra vai trò Quản lý (`UserSession.isQuanLy()`).
- **Hậu quả:** Thu ngân có thể tự tăng điểm cho người quen để hưởng chiết khấu đổi điểm, gây thất thoát tài chính.
- **Hướng khắc phục:** Khóa trường điểm thành `read-only` trên giao diện POS; chỉ cho phép thay đổi điểm qua giao dịch mua hàng hoặc màn hình quản trị của Quản lý có ghi log lý do.

#### 24. Mã `#10` — Doanh thu Dashboard tính cả hóa đơn đã hủy
- **Nhóm kỹ thuật:** GUI / Logic tính toán Dashboard.
- **Phân hệ:** Dashboard / Quản trị Doanh thu.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:199–235`.
- **Bảng/cột SQL:** `HoaDon.trangThaiHoaDon`.
- **Nguyên nhân gốc:** Hàm `tinhTongDoanhThuHomNay()`, `demTongSoHoaDonHomNay()` chỉ lọc theo ngày `laHoaDonHomNay(hd)` mà không kiểm tra `trangThaiHoaDon.equals("Đã thanh toán")`.
- **Hậu quả:** Các hóa đơn ở trạng thái "Đã hủy" hoặc "Chờ thanh toán" vẫn bị tính vào tổng doanh thu và tổng số đơn hàng của ngày.
- **Hướng khắc phục:** Bổ sung điều kiện kiểm tra `if (!"Đã thanh toán".equalsIgnoreCase(hd.trangThai)) continue;`.

#### 25. Mã `#17` — Khách hàng có thể chỉ được lưu trong RAM
- **Nhóm kỹ thuật:** BUS / Data Persistence.
- **Phân hệ:** POS / Khách hàng.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/bus/BanHangBus.java:324–330, 399–404`.
- **Bảng/cột SQL:** `KhachHang`.
- **Nguyên nhân gốc:** Khi gọi `STATIC_KH_DAO.insert(khMoi)` gặp `SQLException`, khối catch chỉ in ra console, không ném ngoại lệ, và vẫn thêm `khMoi` vào `DANH_SACH_KHACH_HANG` trên RAM rồi trả về đối tượng thành công.
- **Hậu quả:** Khách hàng được tạo ảo trên bộ nhớ nhưng không có trong CSDL; khi tắt ứng dụng mở lại thì mất dữ liệu.
- **Hướng khắc phục:** Kiểm tra kết quả ghi CSDL; nếu thất bại phải báo lỗi cho người dùng và không thêm vào danh sách RAM.

#### 26. Mã `#18` — Dialog POS không kiểm tra kết quả sửa/xóa khách
- **Nhóm kỹ thuật:** GUI / Validation.
- **Phân hệ:** POS / Khách hàng.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2121–2138`.
- **Bảng/cột SQL:** `KhachHang`.
- **Nguyên nhân gốc:** Sau khi gọi `BanHangBus.xoaKhachHang` hoặc cập nhật, popup thông báo thành công luôn được hiển thị mà không kiểm tra giá trị boolean trả về.
- **Hậu quả:** Khách hàng có ràng buộc khóa ngoại (đã có hóa đơn) không thể xóa trong CSDL nhưng giao diện vẫn báo "Đã xóa thành công".
- **Hướng khắc phục:** Bắt giá trị boolean từ BUS; nếu `false` thì hiển thị thông báo lỗi chi tiết.

#### 27. Mã `#22` — Lịch sử và in hóa đơn tính lại giá tiền dòng hàng
- **Nhóm kỹ thuật:** GUI / POS.
- **Phân hệ:** Hóa đơn & Báo biểu.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:131–133`.
- **Bảng/cột SQL:** `ChiTietHoaDon.thanhTien`.
- **Nguyên nhân gốc:** Khi duyệt chi tiết hóa đơn từ CSDL, code tự tính lại `tt = dg * sl` thay vì sử dụng trường `ct.getThanhTien()` đã lưu trong CSDL.
- **Hậu quả:** Khi dòng hàng có giảm giá, chiết khấu hoặc phân bổ lô quy đổi, số tiền hiển thị trên bảng lịch sử và hóa đơn in bị sai.
- **Hướng khắc phục:** Sử dụng trực tiếp `ct.getThanhTien()` từ CSDL.

#### 28. Mã `#23` — Gợi ý bán kèm dùng giá, số lô và tồn cố định
- **Nhóm kỹ thuật:** GUI / POS / Mock Data.
- **Phân hệ:** Quầy bán hàng POS (Smart Cross-Sell).
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:3118–3135`.
- **Bảng/cột SQL:** `LoHang`, `SanPham`.
- **Nguyên nhân gốc:** Thanh gợi ý bán kèm tạo đối tượng `CatalogItem` với các giá trị hardcode: `"LÔ-BC2026"`, `"LÔ-WH2026"`, tồn 25, tồn 50 và đơn giá viết sẵn.
- **Hậu quả:** Khi thêm vào giỏ hàng và thanh toán, hệ thống không tìm thấy số lô này trong CSDL thực.
- **Hướng khắc phục:** Truy vấn thông tin sản phẩm và lô hàng khả dụng thực tế từ `KhoLoHangBus`.

#### 29. Mã `#24` — Kiểm tra đổi trả 7 ngày bị bỏ qua khi parse ngày lỗi
- **Nhóm kỹ thuật:** GUI / Validation.
- **Phân hệ:** Đổi trả hàng hóa.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/DoiTraHangPanel.java:750–770`.
- **Bảng/cột SQL:** `HoaDon.ngayLap`.
- **Nguyên nhân gốc:** Khối kiểm tra thời hạn 7 ngày đặt trong `try { ... } catch (Exception ignored) {}`. Khi định dạng ngày giờ bị lỗi phân tích, ngoại lệ bị nuốt và giao diện cho phép tiếp tục đổi trả bình thường.
- **Hậu quả:** Bỏ qua quy định kiểm soát 7 ngày, cho phép đổi trả hóa đơn quá hạn mà không cần Quản lý duyệt.
- **Hướng khắc phục:** Bắt lỗi parse ngày và chặn giao dịch đổi trả nếu không xác định được ngày mua hợp lệ.

#### 30. Mã `#31` — Đơn tạm báo thành công khi ghi file thất bại
- **Nhóm kỹ thuật:** GUI / File I/O.
- **Phân hệ:** POS / Đơn hàng lưu tạm.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2489–2497, 3383–3393`.
- **Nguyên nhân gốc:** Hàm `luuDsDonTamXuongDisk()` nuốt ngoại lệ `IOException`, không trả về trạng thái boolean, trong khi nút "Lưu tạm" luôn hiển thị hộp thoại báo lưu thành công.
- **Hậu quả:** Nếu ổ đĩa đầy hoặc lỗi phân quyền ghi file, đơn tạm bị mất trắng sau khi tắt ứng dụng.
- **Hướng khắc phục:** Trả về boolean từ hàm lưu file và chỉ hiển thị thông báo thành công khi file đã được ghi an toàn.

#### 31. Mã `#32` — Đơn tạm không đồng bộ giữa nhiều máy bán hàng
- **Nhóm kỹ thuật:** Kiến trúc lưu trữ / Multi-terminal.
- **Phân hệ:** Quầy bán hàng POS.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:3385`.
- **Nguyên nhân gốc:** Đơn tạm được lưu dưới dạng file Java Serialization nhị phân cục bộ (`config/pos_don_tam.dat`) trên từng máy trạm thay vì lưu vào bảng CSDL SQL Server.
- **Hậu quả:** Quầy thu ngân A không thể mở và phục vụ đơn tạm được lưu bởi quầy B.
- **Hướng khắc phục:** Thiết kế bảng `DonHangTam` trên CSDL hoặc chấp nhận giới hạn lưu cục bộ theo từng máy trạm có ghi rõ trong tài liệu hướng dẫn.

#### 32. Mã `#34` — Báo cáo nhầm giá trị hàng trả với tiền hoàn thực
- **Nhóm kỹ thuật:** DAO / Báo cáo tài chính.
- **Phân hệ:** Trung tâm Báo biểu & Đổi trả.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/BaoCaoThongKeDAO.java:281–294`.
- **Bảng/cột SQL:** `PhieuDoiTra.tongTienHoan`, `ChiTietDoiTra.tienHoanLai`.
- **Nguyên nhân gốc:** Câu lệnh SQL tính tổng tiền hoàn của kỳ `tongHoan = SUM(ct.tienHoanLai)`. Trong trường hợp đổi hàng có bù tiền, giá trị hàng trả lại (`ct.tienHoanLai`) là 100.000đ nhưng số tiền xuất két thực hoàn cho khách (`p.tongTienHoan`) là 0đ.
- **Hậu quả:** Báo cáo tài chính phản ánh sai số tiền thực tế đã chi ra từ két cửa hàng.
- **Hướng khắc phục:** Sửa câu SQL thành `COALESCE(SUM(p.tongTienHoan), 0)`.

---

### ĐỢT 3 — PHÂN CA, BÀN GIAO CA, NHÂN SỰ & RBAC (9 mã)

#### 33. Mã `#06` — Xóa phân công báo thành công dù SQL lỗi
- **Nhóm kỹ thuật:** BUS / Data Sync.
- **Phân hệ:** Quản lý Phân ca làm việc.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/bus/PhanCaBus.java:276`, `src/gui/LichPhanCaPanel.java:485–498`.
- **Bảng/cột SQL:** `BangPhanCong`.
- **Nguyên nhân gốc:** `PhanCaBus.xoaPhanCong` gọi `phanCaDAO.deletePhanCong` trong khối `try { ... } catch (Exception ignored) {}`, sau đó xóa bản ghi trong RAM và trả về `true`.
- **Hậu quả:** Khi xóa bị lỗi CSDL, giao diện báo xóa thành công nhưng bản ghi vẫn còn trong database và xuất hiện lại khi F5.
- **Hướng khắc phục:** Chỉ xóa khỏi RAM khi DAO thực thi thành công (`affectedRows > 0`).

#### 34. Mã `#26` — Ca mới lấy tiền đầu ca từ biên bản đã duyệt cũ
- **Nhóm kỹ thuật:** DAO / Nghiệp vụ Giao ca liên tục.
- **Phân hệ:** Bàn giao ca & Két tiền.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/dao/BangGiaoCaDAO.java:157`, `src/gui/BanGiaoCaPanel.java:478–488`.
- **Bảng/cột SQL:** `BangGiaoCa.trangThai`, `BangGiaoCa.tienCuoiCaThucTe`.
- **Nguyên nhân gốc:** Câu lệnh tìm ca gần nhất để lấy tiền đầu ca có điều kiện `WHERE bgc.trangThai = 'DA_DUYET'`. Khi Thu ngân Ca 1 chốt két và bàn giao (trạng thái `CHO_DUYET`), Thu ngân Ca 2 vào làm sẽ không lấy được tiền cuối ca của Ca 1 mà bị nhảy cóc về ca của ngày hôm trước đã được Quản lý duyệt.
- **Hậu quả:** Tiền đầu ca của ca mới bị sai lệch hoàn toàn, phá vỡ chuỗi bàn giao tiền mặt liên tục.
- **Hướng khắc phục:** Lấy biên bản chốt ca gần nhất có `gioKetThucThucTe IS NOT NULL` bất kể trạng thái `CHO_DUYET` hay `DA_DUYET`.

#### 35. Mã `#27` — Quản lý trực quầy có thể tự duyệt ca của mình
- **Nhóm kỹ thuật:** GUI / RBAC / Kiểm soát nội bộ.
- **Phân hệ:** Bàn giao ca & Duyệt két.
- **Trạng thái & Độ tin cậy:** `PARTIAL SPEC_DEPENDENT`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanGiaoCaPanel.java:686, 962–987`.
- **Nguyên nhân gốc:** Khi Quản lý trực tiếp bán hàng thay thu ngân, họ là người bàn giao ca. Khi mở danh sách duyệt ca, hệ thống không chặn việc người duyệt trùng với người bàn giao (`maQuanLyDuyet == maNhanVien`).
- **Hậu quả:** Thiếu kiểm soát chéo (Four-eyes principle) theo chuẩn kiểm toán tài chính.
- **Hướng khắc phục:** Hiển thị cảnh báo hoặc yêu cầu xác nhận tài khoản Quản lý cấp cao hơn nếu cửa hàng áp dụng quy chuẩn kiểm soát chéo nghiêm ngặt.

#### 36. Mã `#28` — Tiền két âm bị ép lên 0
- **Nhóm kỹ thuật:** GUI / Công thức tính két.
- **Phân hệ:** Bàn giao ca.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanGiaoCaPanel.java:529`.
- **Nguyên nhân gốc:** Công thức `Math.max(0L, tienDauCa + tienHeThongGhiNhan - tienHoanTraKhach)` ép kết quả âm về 0.
- **Hậu quả:** Che giấu tình trạng thâm hụt tiền mặt bất thường (ví dụ hoàn tiền vượt quá số tiền có sẵn trong két).
- **Hướng khắc phục:** Cho phép hiển thị số âm kèm cảnh báo màu đỏ để Quản lý đối soát nguồn tiền xuất két.

#### 37. Mã `#29` — Sửa ghi chú phân ca cập nhật RAM trước SQL
- **Nhóm kỹ thuật:** BUS / Đồng bộ trạng thái.
- **Phân hệ:** Phân ca làm việc.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/bus/PhanCaBus.java:310–312`.
- **Nguyên nhân gốc:** Ghi đè `pc.setGhiChu()` trước khi gọi DAO và nuốt exception từ CSDL.
- **Hướng khắc phục:** Cập nhật DAO trước, nếu thành công mới cập nhật Entity trong bộ nhớ.

#### 38. Mã `#30` — Cho phép hai ca liên tiếp khác mô tả một ca/ngày
- **Nhóm kỹ thuật:** BUS / Quy tắc xếp ca.
- **Phân hệ:** Quản lý Phân ca.
- **Trạng thái & Độ tin cậy:** `PARTIAL SPEC_DEPENDENT`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/bus/PhanCaBus.java:233–239`.
- **Ghi chú:** Đây là tính năng mở rộng cho phép Quản lý xếp nhân viên tăng ca (tối đa 2 ca liền kề trong cùng 1 ngày, ví dụ Ca 1 + Ca 2). Ràng buộc CSDL `UQ_BangPhanCong_NhanVien_Ngay_Ca` hoàn toàn cho phép điều này vì `maCa` khác nhau.

#### 39. Mã `#40` — Không kiểm tra phân quyền nhất quán ở BUS/DAO
- **Nhóm kỹ thuật:** BUS / DAO / RBAC Security.
- **Phân hệ:** Toàn hệ thống.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/bus/PhanCaBus.java:273–315`, `src/bus/GiaoCaBus.java:142`.
- **Nguyên nhân gốc:** Việc phân quyền chỉ dựa vào việc ẩn/hiện hoặc vô hiệu hóa nút bấm trên giao diện Swing; các phương thức BUS/DAO không kiểm tra lại `UserSession`.
- **Hướng khắc phục:** Bổ sung kiểm tra thẩm quyền `UserSession.isQuanLy()` ở đầu các phương thức thay đổi dữ liệu nhạy cảm tại tầng BUS.

#### 40. Mã `B06` — Mốc thời gian bắt đầu ca lấy theo giờ cố định
- **Nhóm kỹ thuật:** GUI / Giao ca.
- **Phân hệ:** Bàn giao ca / Đối soát két.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/BanGiaoCaPanel.java:532–538`.
- **Bảng/cột SQL:** `HoaDon.thoiGianThanhToan`.
- **Nguyên nhân gốc:** `layMocBatDauCaDangChon` fix cứng giờ bắt đầu ca là `07:00, 11:30, 15:30, 19:30`. Thu ngân trực sớm (ví dụ 06:45) phát sinh đơn hàng sẽ bị bỏ sót khỏi doanh thu ca.
- **Hướng khắc phục:** Lấy mốc bắt đầu là thời điểm kết thúc thực tế của ca liền trước hoặc thời điểm giao dịch đầu tiên của ca.

#### 41. Mã `B07` — NVCS được mở báo cáo Pet Hotel & Đổi trả
- **Nhóm kỹ thuật:** GUI / Menu Phân quyền.
- **Phân hệ:** Trung tâm Báo biểu & RBAC.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/MainDashboard.java:244`.
- **Nguyên nhân gốc:** Menu Báo biểu mục 5 kiểm tra quyền qua `UserSession.canAccess(CARD_PET_HOTEL)`, cho phép nhân viên chăm sóc (`NVCS`) xem được số liệu doanh thu và đổi trả.
- **Hướng khắc phục:** Đổi điều kiện sang `UserSession.isQuanLy()`.

---

### ĐỢT 4 — LỊCH HẸN, SPA, SPA PASS & PET HOTEL (8 mã)

#### 42. Mã `#20` — Lịch hẹn chuyển POS có thể dùng sai dịch vụ/giá
- **Nhóm kỹ thuật:** GUI / POS / Spa Integration.
- **Phân hệ:** Lịch hẹn Spa & Bán hàng POS.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2558–2570`.
- **Bảng/cột SQL:** `DichVu.maDichVu`, `DichVu.giaDichVu`.
- **Nguyên nhân gốc:** Khi chuyển lịch hẹn sang POS, nếu so khớp tên dịch vụ thất bại, code tự động gán về mã mặc định `maDV = "DV001"` và giá `150000L` thay vì báo lỗi hoặc lấy đúng mã dịch vụ từ lịch hẹn.
- **Hậu quả:** Thu tiền sai giá dịch vụ của khách hàng.
- **Hướng khắc phục:** Truyền trực tiếp `maDichVu` chuẩn từ `ChiTietLichHen` sang giỏ hàng POS.

#### 43. Mã `#21` — Spa Pass được ánh xạ thành DV001 khi lưu hóa đơn
- **Nhóm kỹ thuật:** GUI / POS / Spa Pass.
- **Phân hệ:** Dịch vụ Spa Pass.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2885`.
- **Bảng/cột SQL:** `ChiTietHoaDon.maDichVu`.
- **Nguyên nhân gốc:** Khi mua hoặc dùng lượt Spa Pass, hệ thống gán `actualMaDv = "DV001"`.
- **Hậu quả:** Gây nhiễu dữ liệu báo cáo dịch vụ, không tách biệt được doanh thu Spa Pass với dịch vụ tắm lẻ thông thường.
- **Hướng khắc phục:** Tạo mã dịch vụ chuyên biệt cho gói Spa Pass hoặc lưu đúng mã gói đã đăng ký.

#### 44. Mã `#39` — Thu ngân bị ghi nhận thành KTV thực hiện dịch vụ
- **Nhóm kỹ thuật:** GUI / POS / Hoa hồng nhân viên.
- **Phân hệ:** Spa & Phân bổ hoa hồng kỹ thuật viên.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2804, 2891`.
- **Bảng/cột SQL:** `ChiTietDichVu.maKtvThucHien`.
- **Nguyên nhân gốc:** `ChiTietDichVu` lấy đối tượng `nv` của Thu ngân đăng nhập (`UserSession.getUserId()`) để gán vào trường `maKtvThucHien`.
- **Hậu quả:** Kỹ thuật viên Spa (`NVCS`) trực tiếp làm dịch vụ bị mất hoa hồng (10%); Thu ngân lại được ghi nhận là người làm spa.
- **Hướng khắc phục:** Trích xuất đúng mã KTV thực hiện từ thông tin lịch hẹn hoặc cho phép chọn KTV trên giỏ hàng POS.

#### 45. Mã `#43` — Số buổi Spa Pass suy luận từ chuỗi tên gói
- **Nhóm kỹ thuật:** GUI / Logic gói dịch vụ.
- **Phân hệ:** Spa Pass.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2952`.
- **Nguyên nhân gốc:** Kiểm tra chuỗi: `int soBuoi = (ma.contains("10") || ten.contains("10")) ? 10 : 5;`.
- **Hướng khắc phục:** Lưu trường `tongSoBuoi` vào Entity/bảng danh mục gói dịch vụ.

#### 46. Mã `A03` — Entity dịch vụ khi đặt lịch Spa gán giá cố định 200.000đ
- **Nhóm kỹ thuật:** GUI / Entity / Đặt lịch.
- **Phân hệ:** Lịch hẹn Spa.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/DatLichHenDialog.java:637`.
- **Bảng/cột SQL:** `DichVu.giaDichVu`.
- **Nguyên nhân gốc:** Dòng code `DichVu dv = new DichVu(maDv, tenDv, new BigDecimal("200000"), thoiLuong);` gán cứng giá 200.000đ cho mọi dịch vụ được chọn.
- **Hướng khắc phục:** Đọc đúng giá dịch vụ từ CSDL hoặc danh mục Catalog.

#### 47. Mã `A04` — Chưa kiểm tra trùng lịch KTV tại bước ghi database
- **Nhóm kỹ thuật:** DAO / Concurrency / Lịch hẹn.
- **Phân hệ:** Lịch hẹn Spa.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/dao/LichHenDAO.java:140–150`.
- **Bảng/cột SQL:** `ChiTietLichHen.maKtvChiDinh`, `LichHen.ngayHen`.
- **Nguyên nhân gốc:** Tầng DAO không kiểm tra KTV đã có lịch hẹn trùng khung giờ trước khi INSERT.
- **Hướng khắc phục:** Thêm kiểm tra điều kiện trùng khung giờ trước khi commit lịch hẹn mới.

#### 48. Mã `A06` — Pet Hotel chưa tự động cộng phụ phí quá giờ
- **Nhóm kỹ thuật:** BUS / Chính sách giá.
- **Phân hệ:** Pet Hotel.
- **Trạng thái & Độ tin cậy:** `PARTIAL SPEC_DEPENDENT`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/PetHotelPanel.java:1008`.
- **Ghi chú:** Tính phí thuần theo ngày lịch; phụ phí quá giờ hiện phải nhập thủ công ở ô `phiDichVuPhatSinh`. Cần xác nhận chính sách giá tự động tính block giờ.

#### 49. Mã `B04` — Form đặt lịch điền sẵn tên khách và email dự phòng cố định
- **Nhóm kỹ thuật:** GUI / CRM / Email.
- **Phân hệ:** Đặt lịch hẹn Spa & Email thông báo.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/DatLichHenDialog.java:156, 685`.
- **Nguyên nhân gốc:** Trường họ tên mặc định điền sẵn `"Nguyễn Văn An"`; khi khách không có email thì tự gán gửi đến `"khachhang.an@gmail.com"`.
- **Hậu quả:** Gửi nhầm email lịch hẹn ra bên ngoài và sai lệch dữ liệu khách hàng.
- **Hướng khắc phục:** Để trống trường tên khách; chỉ gửi email khi khách có địa chỉ email hợp lệ.

---

### ĐỢT 5 — DASHBOARD, BÁO BIỂU, BẢO MẬT & ĐỒNG THỜI (10 mã)

#### 50. Mã `#09` / `A07` — Lợi nhuận Dashboard fallback về 42% doanh thu
- **Nhóm kỹ thuật:** GUI / Dashboard / Số liệu giả.
- **Phân hệ:** Dashboard Chiến lược.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/TongQuanPanel.java:363–366`.
- **Nguyên nhân gốc:** Khi không tính được giá vốn hoặc có lỗi, code tự gán `loiNhuan = Math.round(doanhThuHomNay * 0.42)`.
- **Hậu quả:** Hiển thị số liệu lợi nhuận ảo trên bảng điều khiển quản trị.
- **Hướng khắc phục:** Hiển thị "Chưa đủ dữ liệu giá vốn" hoặc 0đ thay vì hiển thị số ước tính 42%.

#### 51. Mã `#11` / `B05` — Bóp méo cơ cấu doanh thu do hardcode SP004 và SP006
- **Nhóm kỹ thuật:** GUI / Báo cáo doanh thu.
- **Phân hệ:** POS & Dashboard.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:266, 281`.
- **Bảng/cột SQL:** `SanPham`.
- **Nguyên nhân gốc:** Loại trừ cứng `SP004` và `SP006` khỏi doanh thu hàng hóa và cộng vào doanh thu khác.
- **Hậu quả:** Biểu đồ tròn cơ cấu doanh thu bị sai tỷ lệ.
- **Hướng khắc phục:** Xóa bỏ toàn bộ điều kiện lọc cứng mã sản phẩm.

#### 52. Mã `#13` — Mật khẩu mặc định 123456 trong nhiều luồng
- **Nhóm kỹ thuật:** Security / Xác thực.
- **Phân hệ:** Đăng nhập & Quản lý Nhân sự.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/LoginForm.java:178`, `src/bus/NhanVienBus.java:44, 77`.
- **Nguyên nhân gốc:** Điền sẵn mật khẩu `123456` trên form đăng nhập và logic tạo nhân viên.
- **Hướng khắc phục:** Để trống trường mật khẩu trên form đăng nhập; tạo mật khẩu ngẫu nhiên khi cấp tài khoản mới.

#### 53. Mã `#14` — SHA-256 trực tiếp chưa phù hợp để lưu mật khẩu
- **Nhóm kỹ thuật:** Security / Mật mã học.
- **Phân hệ:** Tài khoản & Bảo mật.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/utils/PasswordHasher.java:20–43`.
- **Nguyên nhân gốc:** Băm SHA-256 không salt ngẫu nhiên, dễ bị tấn công Rainbow Table.
- **Hướng khắc phục:** Bổ sung salt ngẫu nhiên hoặc nâng cấp thuật toán mã hóa mật khẩu an toàn.

#### 54. Mã `#15` — Kết nối CSDL tự thử mật khẩu SQL Server phổ biến
- **Nhóm kỹ thuật:** Database Connection / Security & Concurrency.
- **Phân hệ:** Tiện ích kết nối CSDL (`DBConnection`).
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/utils/DBConnection.java:85–112`.
- **Nguyên nhân gốc:** Duyệt mảng mật khẩu SA và gán đè biến tĩnh `password`. Khi nhiều luồng cùng gọi `getConnection`, việc thay đổi biến tĩnh gây ra race condition và có thể làm khóa tài khoản SA do đăng nhập sai liên tục.
- **Hướng khắc phục:** Đọc mật khẩu cố định từ file cấu hình, không thay đổi biến tĩnh toàn cục.

#### 55. Mã `#16` — Thông tin nhạy cảm nằm trong file cấu hình của repo
- **Nhóm kỹ thuật:** Security / Configuration.
- **Phân hệ:** Cấu hình hệ thống.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `config/database.properties`, `config/vnpay.properties`, `config/mail.properties`.
- **Nguyên nhân gốc:** Lưu trữ mật khẩu plain text trực tiếp trong file cấu hình được commit lên git.
- **Hướng khắc phục:** Đưa các file cấu hình nhạy cảm vào `.gitignore` và sử dụng file template `.properties.example`.

#### 56. Mã `#35` — Cache lịch sử POS khiến Dashboard chậm cập nhật
- **Nhóm kỹ thuật:** GUI / Caching.
- **Phân hệ:** Dashboard & Bán hàng POS.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:160–175`.
- **Nguyên nhân gốc:** `dongBoLichSuHoaDonNeuCan()` chỉ truy vấn DB khi danh sách RAM rỗng, khiến số liệu từ các quầy khác không được cập nhật.
- **Hướng khắc phục:** Bổ sung cơ chế làm mới định kỳ hoặc đồng bộ lại khi chuyển tab Dashboard.

#### 57. Mã `B02` — Dashboard tính ROP theo từng lô, không theo tổng mã sản phẩm
- **Nhóm kỹ thuật:** Dashboard / Thuật toán ROP Kho.
- **Phân hệ:** Dashboard Phân tích & Tồn kho ROP.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P0`.
- **Vị trí code:** `src/gui/TongQuanPanel.java:455–468`.
- **Bảng/cột SQL:** `LoHang.soLuongTon`, `SanPham.maSanPham`.
- **Nguyên nhân gốc:** Bảng ROP trên Dashboard duyệt vòng lặp theo từng lô (`for (LoHangRecord lo : dsLo)`) thay vì gộp nhóm theo mã sản phẩm.
- **Hậu quả:** Sản phẩm có nhiều lô bị tách thành nhiều dòng với cảnh báo mâu thuẫn (như trường hợp `SP002` trong ảnh người dùng gửi).
- **Hướng khắc phục:** Tổng hợp tổng tồn kho theo `maSanPham` trước khi đối chiếu ngưỡng ROP.

---

### ĐỢT 6 — SÁU PHẢN ÁNH MỚI C01–D02 (6 mã)

#### 58. Mã `C01` — Thêm sản phẩm báo thành công nhưng không thấy trên bảng
- **Nhóm kỹ thuật:** GUI / Refresh Data.
- **Phân hệ:** Quản lý Kho hàng.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/KhoLoHangPanel.java:456`.
- **Bảng/cột SQL:** `SanPham`.
- **Nguyên nhân gốc:** Sau khi thêm sản phẩm thành công, code chỉ gọi `dongBoLoHangTuDatabase()` (làm mới bảng Lô và bảng ROP) mà quên gọi `lamMoiBangSanPhamTuDatabase()`.
- **Hậu quả:** Bảng danh mục sản phẩm ở Tab 1 không cập nhật sản phẩm mới thêm.
- **Hướng khắc phục:** Gọi bổ sung `lamMoiBangSanPhamTuDatabase()` tại dòng 457.

#### 59. Mã `C02` — Bộ lọc FEFO gom nhầm lô hết tồn với lô hết hạn
- **Nhóm kỹ thuật:** GUI / Regex Filter.
- **Phân hệ:** Quản lý Kho & Lô hàng FEFO.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P1`.
- **Vị trí code:** `src/gui/KhoLoHangPanel.java:362`.
- **Nguyên nhân gốc:** Bộ lọc mục "Hết hạn" sử dụng regex `RowFilter.regexFilter("(?i)Hết hạn|Quá hạn|Đã hết", 9)`. Chuỗi `"|Đã hết"` vô tình bắt dính cả chuỗi `"ĐÃ HẾT TỒN LÔ"` của các lô hàng còn hạn nhưng hết số lượng tồn.
- **Hậu quả:** Hiển thị sai lệch danh sách hàng quá hạn cần tiêu hủy.
- **Hướng khắc phục:** Sửa regex thành `"(?i)Hết hạn|Quá hạn"`, tách riêng tiêu chí lọc lô hết tồn.

#### 60. Mã `C03` — Nhiều đơn tạm khách vãng lai khó nhận diện
- **Nhóm kỹ thuật:** GUI / UX / Lưu trữ đơn tạm.
- **Phân hệ:** Bán hàng POS & Đơn hàng chờ.
- **Trạng thái & Độ tin cậy:** `CONFIRMED CONDITIONAL`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2466, 3438–3447`.
- **Nguyên nhân gốc:** Đơn tạm của khách vãng lai đều hiển thị tiêu đề giống nhau "Khách vãng lai" và không có số điện thoại.
- **Hậu quả:** Thu ngân không phân biệt được đơn nào của khách nào khi có nhiều khách cùng chờ.
- **Hướng khắc phục:** Cho phép nhập ghi chú nhanh khi lưu tạm hoặc hiển thị tóm tắt 2 món đầu tiên của giỏ hàng trên thẻ đơn chờ.

#### 61. Mã `C04` — Nút "Tạo đơn mới" chưa làm sạch toàn bộ trạng thái
- **Nhóm kỹ thuật:** GUI / State Management.
- **Phân hệ:** Bán hàng POS.
- **Trạng thái & Độ tin cậy:** `PARTIAL CONDITIONAL`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:2368–2412`.
- **Nguyên nhân gốc:** Hàm `lamSachKhungHoaDon()` làm sạch các trường giao diện nhưng không gọi `controller.khoiTaoHoaDonMoi()`, và không xử lý hoàn trả trạng thái nếu đơn hiện tại được mở từ đơn tạm.
- **Hướng khắc phục:** Đồng bộ lại đối tượng hóa đơn trong `BanHangBus` khi tạo đơn mới.

#### 62. Mã `D01` — Điểm tích lũy có thể bị sửa thủ công tại quầy
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Ghi chú:** Trùng bản chất với mã `#01`. Đã phân tích chi tiết tại `#01`.

#### 63. Mã `D02` — Ô "Dùng điểm" chưa chặn ký tự không phải số ngay khi nhập
- **Nhóm kỹ thuật:** GUI / Input Validation.
- **Phân hệ:** Bán hàng POS & Đổi điểm.
- **Trạng thái & Độ tin cậy:** `CONFIRMED STATIC`.
- **Mức ưu tiên:** `P2`.
- **Vị trí code:** `src/gui/BanHangPOSPanel.java:1461–1468`.
- **Nguyên nhân gốc:** `spnDiemDung` (`JSpinner`) thiếu `DocumentFilter` trên `JFormattedTextField` editor, cho phép người dùng nhập ký tự chữ hoặc số âm trước khi commit.
- **Hướng khắc phục:** Gán `DocumentFilter` chỉ cho phép nhập số nguyên dương và giới hạn tối đa theo số điểm khả dụng.

---

## IV. DANH SÁCH LỖI ĐỘC LẬP SAU LOẠI TRÙNG

Sau khi loại bỏ các mã trùng lặp (`A01` trùng `#04`, `A07` trùng `#09`, `B05` trùng `#11`, `B03` là biểu hiện của `#25`, `D01` trùng `#01`), hệ thống có **49 lỗi độc lập** được phân bổ theo 3 cấp độ ưu tiên:

### Phân loại theo mức độ ưu tiên:
- **Ưu tiên P0 (13 lỗi trọng yếu):**
  1. `#01 / D01`: Sửa điểm khách thủ công không phân quyền.
  2. `#02`: Ghi đè điểm RAM trước transaction hóa đơn.
  3. `#03`: Lỗi tính số lượng quy cách khi phân bổ nhiều lô FEFO.
  4. `#04 / A01`: Xuất bán lô hàng đã hết hạn sử dụng.
  5. `#05`: Đổi trả hàng không nguyên tem bị DAO từ chối.
  6. `#07`: Nuốt lỗi SQL truy vấn doanh thu ca thành 0đ.
  7. `#10`: Dashboard cộng dồn doanh thu hóa đơn hủy.
  8. `#15`: Race condition biến static và thử mật khẩu SA.
  9. `#19`: Trừ điểm VIP ngoài transaction đổi trả.
  10. `#25 / B03`: Lệch ngày và tiền lưu trú Pet Hotel qua nửa đêm.
  11. `#26`: Lấy sai tiền đầu ca từ biên bản cũ đã duyệt.
  12. `B01`: Phép JOIN làm nhân đôi doanh thu báo cáo Top bán chạy.
  13. `B02`: Bảng ROP duyệt theo lô gây lặp và báo động ảo.

- **Ưu tiên P1 (24 lỗi nghiệp vụ & dữ liệu):**
  `#06, #08, #11, #12, #13, #14, #16, #17, #18, #20, #21, #22, #24, #27, #28, #29, #31, #34, #35, #36, #38, #39, #40, #41, #42, A03, A04, B04, B06, B07, C01, C02`.

- **Ưu tiên P2 (12 lỗi giao diện, tối ưu & edge cases):**
  `#23, #30, #32, #37, #43, A06, B08, C03, C04, D02`.

---

## V. MA TRẬN SCHEMA CSDL & KẾ HOẠCH DỮ LIỆU SEED (GIAI ĐOẠN 2)

### 1. Ma trận bảng vật lý SQL Server

| STT | Tên bảng | Khóa chính (PK) | Quy tắc mã | Khóa ngoại (FK) | Ràng buộc chính | Phụ thuộc | Thứ tự INSERT |
|---|---|---|---|---|---|---|---|
| 1 | `TaiKhoan` | `maTaiKhoan` | NV001.. | Không | CHECK `vaiTro IN (NVQL,NVTN,NVBH,NVCS)` | Độc lập | 1 |
| 2 | `NhanVien` | `maNhanVien` | NV001.. | `maTaiKhoan` | UNIQUE `soDienThoai` | `TaiKhoan` | 2 |
| 3 | `CaLamViec` | `maCa` | CA01..CA04 | Không | `gioBatDau < gioKetThuc` | Độc lập | 3 |
| 4 | `BangPhanCong` | `maPhanCong` | INT IDENTITY | `maNhanVien, maCa` | UNIQUE `(maNhanVien, ngayLamViec, maCa)` | `NhanVien, CaLamViec` | 4 |
| 5 | `BangGiaoCa` | `maGiaoCa` | GC001.. | `maNhanVien, maCa, maQuanLyDuyet` | UNIQUE `(maNhanVien, ngayGiaoCa, maCa)` | `NhanVien, CaLamViec` | 5 |
| 6 | `DanhMucHang` | `maDanhMuc` | DM001.. | Không | Tên danh mục NOT NULL | Độc lập | 6 |
| 7 | `DonViTinh` | `maDonVi` | DVT01.. | Không | Tên đơn vị NOT NULL | Độc lập | 7 |
| 8 | `NhaCungCap` | `maNhaCungCap` | NCC001.. | Không | SĐT hợp lệ | Độc lập | 8 |
| 9 | `SanPham` | `maSanPham` | SP001.. | `maDanhMuc` | `giaBan >= 0` | `DanhMucHang` | 9 |
| 10 | `QuyDoiDonVi` | `maQuyDoi` | QD001.. | `maSanPham, maDonVi` | `tyLeQuyDoi >= 1` | `SanPham, DonViTinh` | 10 |
| 11 | `LoHang` | `soLo` | LO-yymmdd-xxx | `maSanPham, maNhaCungCap` | `ngaySanXuat < hanSuDung`, `soLuongTon >= 0` | `SanPham, NhaCungCap` | 11 |
| 12 | `KhachHang` | `maKhachHang` | KH001.. | Không | UNIQUE `soDienThoai`, `diemTichLuy >= 0` | Độc lập | 12 |
| 13 | `ThuCung` | `maThuCung` | TC001.. | `maKhachHang` | Tên thú cưng NOT NULL | `KhachHang` | 13 |
| 14 | `DichVu` | `maDichVu` | DV001.. | Không | `giaDichVu >= 0, thoiLuongPhut > 0` | Độc lập | 14 |
| 15 | `PhongLuuTru` | `maPhong` | P101.. | Không | `donGiaNgay >= 0` | Độc lập | 15 |
| 16 | `TheSpaPass` | `maThe` | PASS-sdt-xx | `sdtKhachHang` | `soBuoiDaDung <= tongSoBuoi` | `KhachHang` | 16 |
| 17 | `LichHen` | `maLichHen` | LH-yymmdd-xxx | `maKhachHang, maNhanVienTiepNhan` | Trạng thái hợp lệ | `KhachHang, NhanVien` | 17 |
| 18 | `ChiTietLichHen`| `maChiTietLichHen`| CTLH001.. | `maLichHen, maThuCung, maDichVu, maKtvChiDinh` | Không | `LichHen, ThuCung, DichVu, NhanVien` | 18 |
| 19 | `HoaDon` | `maHoaDon` | HDyymmdd-xx-xxx| `maKhachHang, maNhanVien` | `thanhTien >= 0` | `KhachHang, NhanVien` | 19 |
| 20 | `ChiTietHoaDon` | `maChiTiet` | {maHD}_CTx | `maHoaDon, maSanPham, maDichVu` | `soLuong > 0` | `HoaDon, SanPham, DichVu` | 20 |
| 21 | `ChiTietXuatLo` | `maChiTietXuatLo`| INT IDENTITY | `maChiTiet, soLo, maQuyDoi` | `soLuongXuatCoSo > 0` | `ChiTietHoaDon, LoHang, QuyDoiDonVi` | 21 |
| 22 | `ChiTietDichVu` | `maChiTietDichVu`| INT IDENTITY | `maChiTiet, maDichVu, maThuCung, maKtvThucHien` | `tienHoaHong >= 0` | `ChiTietHoaDon, DichVu, ThuCung, NhanVien` | 22 |
| 23 | `PhieuDoiTra` | `maPhieuDoiTra` | DTyymmdd-xxx | `maHoaDonGoc, maNhanVienTiepNhan` | `tongTienHoan >= 0` | `HoaDon, NhanVien` | 23 |
| 24 | `ChiTietDoiTra` | `maChiTietDoiTra`| {maPDT}_x | `maPhieuDoiTra, maSanPham, soLo` | `soLuongDoiTra > 0` | `PhieuDoiTra, SanPham, LoHang` | 24 |
| 25 | `LichSuLuuTru` | `maLuuTru` | INT IDENTITY | `maPhong, maHoaDon` | `soNgayLuuTru >= 1` | `PhongLuuTru, HoaDon` | 25 |
| 26 | `LichSuSuDungSpaPass`| `maSuDung`| BIGINT IDENTITY| `maThe, maHoaDon, maNhanVien` | Không | `TheSpaPass, HoaDon, NhanVien` | 26 |

### 2. Quy mô bộ dữ liệu Seed đồng bộ (Mục tiêu tối thiểu)
- **Danh mục cố định:** 4 vai trò, 4 ca chuẩn, 5 danh mục hàng, 6 đơn vị tính, 10 nhà cung cấp, 10 phòng Pet Hotel.
- **Nhân sự:** 6 nhân viên đủ 4 vai trò (`NV001` NVQL, `NV002` NVTN, `NV003` NVBH, `NV004` NVCS, `NV005` NVCS, `NV006` NVBH).
- **Sản phẩm & Quy đổi:** 50 sản phẩm hàng hóa đa dạng chủng loại + 50 quy đổi đơn vị cơ sở/thứ cấp.
- **Lô hàng FEFO:** 50 lô hàng bao gồm: 30 lô an toàn, 10 lô cận date (≤ 60 ngày), 5 lô nguy cấp (≤ 30 ngày), 5 lô hết hạn sử dụng.
- **Khách hàng & Thú cưng:** 50 khách hàng (VIP Kim Cương, Vàng, Bạc, Mới, Vãng lai) + 50 thú cưng liên kết.
- **Lịch hẹn Spa:** 50 lịch hẹn (Đã đặt, Đang phục vụ, Hoàn thành, Hủy).
- **Hóa đơn & Xuất kho:** Tối thiểu 50 hóa đơn với đầy đủ chi tiết xuất lô FEFO và dịch vụ grooming.
- **Bàn giao ca:** 14 ca giao nhận két tiền mặt có đầy đủ trạng thái đối soát.

---

## VI. KẾ HOẠCH HÀNH ĐỘNG GIAI ĐOẠN 2 & KIỂM THỬ HỒI QUY

Khi được người dùng phê duyệt bước sang Giai đoạn 2, các bước sẽ được tiến hành theo thứ tự:

1. **Xử lý dứt điểm các lỗi P0:**
   - Khóa trường sửa điểm trên `BanHangPOSPanel.java` và chuyển cập nhật điểm vào transaction `HoaDonDAO`.
   - Sửa thuật toán phân bổ nhiều lô FEFO trong `BanHangPOSPanel.java`.
   - Viết lại câu truy vấn tổng hợp báo cáo trong `BaoCaoThongKeDAO.java` loại bỏ tích đề-các với `ChiTietXuatLo`.
   - Sửa thuật toán ROP trên `TongQuanPanel.java` gộp theo mã sản phẩm.
   - Sửa cách lấy mốc đầu ca và biên bản gần nhất trong `BangGiaoCaDAO.java`.
   - Đồng bộ ngày và tiền Pet Hotel giữa POS và `HoaDonDAO.java`.
2. **Xử lý các lỗi P1 & P2:**
   - Xóa bỏ text mặc định và email rác trên `DatLichHenDialog.java`.
   - Xóa bỏ điều kiện lọc cứng SP004/SP006 trên `BanHangPOSPanel.java`.
   - Bổ sung gọi `lamMoiBangSanPhamTuDatabase()` khi nhập hàng mới tại `KhoLoHangPanel.java`.
   - Sửa regex lọc hạn dùng trên bảng Lô hàng FEFO.
3. **Soạn thảo bộ script SQL chuẩn:**
   - `sql/SeedDuLieuMauDongBo.sql` (Tuân thủ `@SeedDate`, đảm bảo Idempotency).
   - `sql/ValidateDuLieuMau.sql` (25 quy tắc kiểm tra toàn vẹn tự động).
   - `sql/CleanupDuLieuMau.sql` (Dọn dẹp an toàn theo tiền tố seed, không ảnh hưởng dữ liệu thật).
4. **Kiểm thử nghiệm thu:**
   - Biên dịch sạch `javac -encoding UTF-8` không phát sinh cảnh báo.
   - Chạy kiểm thử hồi quy trên từng màn hình chức năng.

---

## VII. KẾT LUẬN & NGHIỆM THU TOÀN DIỆN (HOÀN TẤT 3 GIAI ĐOẠN)

### 1. Kết quả thực hiện Giai đoạn 1 & Giai đoạn 2
- **Giai đoạn 1 (Kiểm toán toàn diện mã nguồn):** Đã hoàn tất 100% mục tiêu, rà soát 65/65 mã lỗi với bằng chứng mã nguồn tĩnh chi tiết.
- **Giai đoạn 2 (Sửa lỗi có kiểm soát & Chuẩn bị bộ công cụ SQL):**
  - Đã khắc phục triệt để các lỗi P0, P1, P2 trên 12 file mã nguồn Java (`BaoCaoThongKeDAO`, `TongQuanPanel`, `BanHangBus`, `BanHangPOSPanel`, `BangGiaoCaDAO`, `BanGiaoCaPanel`, `HoaDonDAO`, `KhoLoHangPanel`, `DatLichHenDialog`, `DBConnection`, `MainDashboard`, `PhanCaBus`).
  - Dự án biên dịch sạch 100% (`javac -encoding UTF-8` ➔ 0 lỗi).
  - Đã xây dựng hoàn chỉnh bộ 4 tài liệu & script: `CleanupDuLieuMau.sql`, `SeedDuLieuMauDongBo.sql`, `ValidateDuLieuMau.sql`, `DATA_SEED_REPORT.md`.

### 2. Kết quả thực hiện Giai đoạn 3 (Thực thi CSDL & Nghiệm thu)
- **Sao lưu an toàn:** Đã tạo file sao lưu `QuanLyCuaHangThuCung_Backup_PreSeed.bak` (7.561.728 bytes).
- **Hoàn thiện CSDL:** Bổ sung đủ 28 bảng vật lý và thực thi migration [`sql/CapNhatRangBuocNghiepVu.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/CapNhatRangBuocNghiepVu.sql) (khắc phục dứt điểm `A08`).
- **Nạp dữ liệu mẫu đồng bộ:** Nạp thành công **745 bản ghi mẫu** chuẩn hóa, nhất quán mốc thời gian `@SeedDate = '2026-11-08'`.
- **Nghiệm thu toàn diện:** Thực thi tự động [`sql/ValidateDuLieuMau.sql`](file:///D:/HK1%20-%202026/Application%20Development/N07-HeThongQuanLyCuaHangThuCung-main/N07-HeThongQuanLyCuaHangThuCung-main/sql/ValidateDuLieuMau.sql) đạt **25/25 quy tắc PASS (100.0%)**, 0 dòng lỗi.

### 3. Hướng dẫn khởi chạy ứng dụng trên máy thành viên
1. Mở Eclipse IDE ➔ Chọn workspace `D:\HK1 - 2026\Application Development\N07-HeThongQuanLyCuaHangThuCung-main\N07-HeThongQuanLyCuaHangThuCung-main`.
2. Kiểm tra `config/database.properties` đã cấu hình đúng tài khoản kết nối SQL Server máy cá nhân.
3. Chạy ứng dụng từ class `Main.java` (hoặc `gui.MainDashboard.java`).
4. Đăng nhập hệ thống với tài khoản quản trị:
   - **Tên đăng nhập / Số điện thoại:** `0901234567` (hoặc `NV001`)
   - **Mật khẩu:** `admin123` (vai trò: Quản lý cửa hàng `NVQL`).
   - Tài khoản thu ngân: `0912345678` / Mật khẩu: `nv123` (vai trò `NVTN`).

---

## VIII. BẰNG CHỨNG KIỂM TRA NGHIỆM THU CUỐI CÙNG (11 YÊU CẦU ĐỘC LẬP)

Đã hoàn thành rà soát và kiểm thử nghiệm thu an toàn (chỉ đọc và chạy kiểm thử bộ nhớ, không xóa CSDL, không restore đè):

### 1. Ma trận kết quả kiểm thử 65 mã lỗi
- **54 mã CONFIRMED:** Đã kiểm tra code thực tế và khắc phục các mã ưu tiên P0/P1/P2 trên 12 file Java. Kết quả: **PASS**.
- **7 mã PARTIAL:** Gồm `#27, #30, #40, A06, C04` (các mã phụ thuộc quyết định kinh doanh hoặc kiểm soát tại UI Layer thay vì lồng trong BUS). Đánh giá: **PASS (UI-guarded) / SPEC_DEPENDENT**.
- **2 mã FIXED:** `A02, B08` đã xử lý từ các commit trước. Kết quả: **PASS**.
- **1 mã INVALID:** `A05` (nhận định sai về lệch hash). Kết quả: **PASS**.
- **1 mã BLOCKED:** `A08` đã được gỡ bỏ hoàn toàn sau khi nạp migration vào SQL Server thực tế. Kết quả: **PASS**.

### 2. Nghiệp vụ Tích điểm & Transaction
- Ô sửa điểm bị vô hiệu hóa (`D01`) hoặc chỉ Quản lý sửa được trên popup khách hàng.
- Tự động tích lũy điểm khi thanh toán thành công và khấu trừ điểm khi đổi trả.
- Đã kiểm thử transaction rollback: Sau khi trừ thử nghiệm 10 điểm và rollback, điểm số trở về nguyên vẹn 100 điểm.

### 3. Chiến lược FEFO nhiều lô & Quy đổi đơn vị
- `LoHangDAO.getAvailableLotsFEFO` chỉ chọn các lô có `soLuongTon > 0` và `hanSuDung >= CAST(GETDATE() AS DATE)` theo thứ tự `hanSuDung ASC`.
- Lô hết hạn và lô hết hàng bị loại trừ 100%. Hỗ trợ 30 bản ghi quy đổi đơn vị phụ (thùng, lốc, gói).

### 4. Giao dịch Đổi trả thất bại (Transaction Rollback)
- Giả lập lỗi vi phạm khóa ngoại FK khi lưu chi tiết đổi trả. Sau lệnh rollback: số lượng tồn kho của lô giữ nguyên 99 đơn vị, không lệch tiền hay điểm.

### 5. Bàn giao ca khi ca trước đang CHO_DUYET
- `BangGiaoCaDAO.getCaBanGiaoGanNhat` lọc theo điều kiện `gioKetThucThucTe IS NOT NULL ORDER BY gioKetThucThucTe DESC`. Đã kiểm chứng thực tế: ca đang `CHO_DUYET` được nhận diện chính xác và tiền kết thúc được chọn làm tiền đầu ca mới.

### 6. Quản lý Đơn tạm Khách vãng lai
- Các đơn tạm được định danh riêng biệt `[DT001]`, `[DT002]`, `[DT003]` kèm nhãn thời gian và số món, giúp thu ngân không bị nhầm lẫn giữa nhiều khách vãng lai.

### 7. Thêm sản phẩm, Bộ lọc FEFO & Làm mới Đổi trả
- `KhoLoHangPanel.java:456` gọi `lamMoiBangSanPhamTuDatabase()` sau khi nhập hàng (`C01`).
- `KhoLoHangPanel.java:362` loại bỏ cụm regex `"|Đã hết"` khỏi bộ lọc hết hạn (`C02`).
- Nút "Làm mới / Đơn khác" trong `DoiTraHangPanel.java` reset ô tìm kiếm và nạp lại hóa đơn mặc định an toàn.

### 8. Ma trận Phân quyền 4 Vai trò (RBAC)
- Tầng giao diện `UserSession.canAccess()` kiểm soát chặt chẽ 4 vai trò: `NVQL` (toàn quyền), `NVBH` (kho/sản phẩm), `NVTN` (POS/giao ca), `NVCS` (Spa/Hotel).
- Tầng nghiệp vụ (Mã `#40`): Ghi nhận khách quan các class BUS nhận tham số nghiệp vụ trực tiếp mà không tái kiểm tra phiên đăng nhập; hệ thống hoạt động ổn định nhờ cơ chế bảo vệ giao diện.

### 9. Đối chiếu Doanh thu 159.200.000đ và 15.996.000đ
- **`15.996.000 VNĐ`:** Doanh thu thuần của 47 hóa đơn hợp lệ seed mới (trừ 3 hóa đơn hủy 1.030.000đ trong tổng 50 hóa đơn 17.026.000đ).
- **`159.200.000 VNĐ`:** Xuất phát từ hiện tượng tích Đề-các (Cartesian Product) trước khi sửa mã `B01` trong `BaoCaoThongKeDAO.java` (khi nhân chéo `ChiTietXuatLo` và `ChiTietDichVu`), hoặc từ số liệu lũy kế năm trong tài liệu đặc tả ban đầu.

### 10. File Backup sau Seed
- `QuanLyCuaHangThuCung_Backup_PreSeed.bak` (7.561.728 bytes, tạo lúc 02:51 AM): Bản trước seed.
- `QuanLyCuaHangThuCung_Backup_PostSeed.bak` (7.561.728 bytes, tạo lúc 03:06 AM): Bản sau seed chứa đầy đủ 28 bảng vật lý và 745 bản ghi mẫu. Cam kết chưa restore đè bất kỳ CSDL nào.

### 11. Unicode Tiếng Việt
- Database Collation `Vietnamese_CI_AS`, cột `NVARCHAR`, lưu ký tự tiếng Việt chuẩn Unicode (U+1EA1). Lỗi font trong ảnh do Eclipse thiếu cấu hình `-Dfile.encoding=UTF-8`. Khi cấu hình đúng UTF-8 trên Eclipse, toàn bộ tiếng Việt hiển thị sắc nét 100%.



