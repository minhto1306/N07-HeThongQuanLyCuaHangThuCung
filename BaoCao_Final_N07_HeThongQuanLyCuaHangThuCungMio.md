# TRƯỜNG ĐẠI HỌC CÔNG NGHIỆP TP. HỒ CHÍ MINH
## KHOA CÔNG NGHỆ THÔNG TIN - BỘ MÔN KỸ THUẬT PHẦN MỀM
***

# BÁO CÁO TỔNG KẾT ĐỒ ÁN MÔN HỌC
## PHÁT TRIỂN ỨNG DỤNG (APPLICATION DEVELOPMENT)

# HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO
### (MIO PET SHOP MANAGEMENT SYSTEM)

**Giảng viên hướng dẫn:** ThS. Phạm Thanh Hùng  
**Nhóm thực hiện:** Nhóm 07 (Lớp DHKTPM - Học kỳ 1, Năm học 2026 - 2027)  

**Bảng thông tin thành viên Nhóm 07:**

| STT | Họ và Tên | Mã Sinh Viên (MSSV) | Vai trò trong nhóm | Phân hệ kỹ thuật phụ trách | Vai trò Khởi nghiệp |
|:---:|---|:---:|:---:|---|---|
| **1** | **Lê Khải Minh** *(Trưởng nhóm)* | **24667791** | Leader | Bán hàng POS, In hóa đơn K80, Đổi trả hàng sang POS, Tích hợp VietQR Napas 24/7 & VNPay | Giám đốc điều hành (CEO) |
| **2** | **Tô Thành Đạt** | **24012345** | Member | Quản lý Kho hàng, Danh mục sản phẩm, Quản lý Lô hàng cận date (FEFO), Bảng quy đổi ĐVT | Thiết kế UI/UX & Frontend |
| **3** | **Võ Thị Hồng Ngọc** | **24023456** | Member | Quản lý Hồ sơ thú cưng, Sổ sức khỏe điện tử & Tiêm chủng, Đặt lịch hẹn Spa/Grooming, Pet Hotel | Trưởng bộ phận QA & Marketing |
| **4** | **Lê Anh Minh** | **24034567** | Member | Quản lý Nhân sự, Đăng nhập & RBAC 4 vai trò, Ma trận xếp ca trực tuần, Bàn giao ca & Duyệt két | Giám đốc công nghệ (CTO) |

*Thành phố Hồ Chí Minh, Tháng 10 Năm 2026*

---

## LỜI CẢM ƠN

Lời đầu tiên, nhóm chúng em xin gửi lời tri ân sâu sắc nhất đến **Thầy Phạm Thanh Hùng** – giảng viên trực tiếp giảng dạy, định hướng và đồng hành cùng chúng em trong suốt học phần Phát triển ứng dụng. 

Trong suốt 15 tuần thực hiện đề tài *"Hệ thống Quản lý Cửa hàng Thú cưng Mio"*, chính những bài giảng thực tiễn, sự chỉ dẫn tận tình và những góp ý chuyên môn sâu sắc của Thầy về tư duy kiến trúc hướng đối tượng, quy chuẩn cơ sở dữ liệu quan hệ và trải nghiệm người dùng công nghiệp đã giúp chúng em tháo gỡ nhiều rào cản phức tạp. Thầy không chỉ truyền đạt kiến thức chuyên môn vững chắc mà còn rèn luyện cho chúng em tác phong làm việc nghiêm túc, kỷ luật và tinh thần tự chủ trong giải quyết vấn đề kỹ thuật.

Nhóm cũng xin chân thành cảm ơn Ban Giám hiệu **Trường Đại học Công nghiệp TP. Hồ Chí Minh (IUH)** cùng quý Thầy Cô trong **Khoa Công nghệ Thông tin**. Nhà trường và Khoa đã luôn tạo điều kiện học tập thuận lợi nhất về môi trường nghiên cứu, hệ thống phòng thực hành máy tính và khung chương trình đào tạo bám sát nhu cầu thực tế của ngành công nghiệp phần mềm.

Mặc dù toàn đội ngũ đã nỗ lực hết mình để phân tích nghiệp vụ, thiết kế kiến trúc và hiện thực mã nguồn bám sát 100% thực tế, song do hạn chế về kinh nghiệm thực chiến, đề tài khó tránh khỏi những điểm có thể hoàn thiện hơn. Chúng em rất mong nhận được những đánh giá, góp ý quý báu từ Thầy để hệ thống ngày càng tối ưu và mang giá trị ứng dụng cao hơn trong đời sống.

*Chúng em xin trân trọng cảm ơn Thầy!*

---

## MỤC LỤC
- [LỜI CẢM ƠN](#lời-cảm-ơn)
- [KHẢO SÁT THỰC TRẠNG KINH DOANH VÀ THU THẬP YÊU CẦU](#khảo-sát-thực-trạng-kinh-doanh-và-thu-thập-yêu-cầu)
- [CHƯƠNG 1: GIỚI THIỆU](#chương-1-giới-thiệu)
  - [1.1. Tổng quan đề tài](#11-tổng-quan-đề-tài)
  - [1.2. Mục tiêu đề tài](#12-mục-tiêu-đề-tài)
  - [1.3. Phạm vi đề tài](#13-phạm-vi-đề-tài)
  - [1.4. Yêu cầu chức năng của hệ thống](#14-yêu-cầu-chức-năng-của-hệ-thống)
  - [1.5. Yêu cầu phi chức năng](#15-yêu-cầu-phi-chức-năng)
- [CHƯƠNG 2: PHÂN TÍCH HỆ THỐNG](#chương-2-phân-tích-hệ-thống)
  - [2.1. Mô hình Use Case tổng quát](#21-mô-hình-use-case-tổng-quát)
  - [2.2. Danh sách các tác nhân hệ thống](#22-danh-sách-các-tác-nhân-hệ-thống)
  - [2.3. Danh mục Use Case và Ma trận phân quyền (RBAC)](#23-danh-mục-use-case-và-ma-trận-phân-quyền-rbac)
  - [2.4. Đặc tả chi tiết 18 Use Cases hệ thống](#24-đặc-tả-chi-tiết-18-use-cases-hệ-thống)
- [CHƯƠNG 3: THIẾT KẾ VÀ HIỆN THỰC HỆ THỐNG](#chương-3-thiết-kế-và-hiện-thực-hệ-thống)
  - [3.1. Mô hình lớp (Class Diagram) và Kiến trúc Tầng](#31-mô-hình-lớp-class-diagram-và-kiến-trúc-tầng)
  - [3.2. Thiết kế Cơ sở dữ liệu Quan hệ](#32-thiết-kế-cơ-sở-dữ-liệu-quan-hệ)
  - [3.3. Phân luồng Màn hình và Cơ chế Điều hướng (Screen Flow)](#33-phân-luồng-màn-hình-và-cơ-chế-điều-hướng-screen-flow)
  - [3.4. Chi tiết Màn hình Ứng dụng & Ba Luồng Nghiệp Vụ Cốt Lõi](#34-chi-tiết-màn-hình-ứng-dụng--ba-luồng-nghiệp-vụ-cốt-lõi)
  - [3.5. Kiểm thử Chức năng Ứng dụng (Test Cases)](#35-kiểm-thử-chức-năng-ứng-dụng-test-cases)
- [CHƯƠNG 4: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN](#chương-4-kết-luận-và-hướng-phát-triển)
  - [4.1. Kết quả đạt được](#41-kết-quả-đạt-được)
  - [4.2. Hạn chế của đồ án](#42-hạn-chế-của-đồ-án)
  - [4.3. Hướng phát triển trong tương lai](#43-hướng-phát-triển-trong-tương-lai)
- [CHƯƠNG 5: KHỞI NGHIỆP (STARTUP PLAN)](#chương-5-khởi-nghiệp-startup-plan)
  - [5.1. Kế hoạch phát triển dự án khởi nghiệp](#51-kế-hoạch-phát-triển-dự-án-khởi-nghiệp)
  - [5.2. Dự án của nhóm và kế hoạch triển khai](#52-dự-án-của-nhóm-và-kế-hoạch-triển-khai)
  - [5.3. Điểm khác biệt và sáng tạo của dự án](#53-điểm-khác-biệt-và-sáng-tạo-của-dự-án)
- [TÀI LIỆU THAM KHẢO](#tài-liệu-tham-khảo)
- [PHỤ LỤC](#phụ-lục)

---

## DANH MỤC CÁC HÌNH VẼ
- Hình 2.1: Sơ đồ Use Case tổng quát Hệ thống Quản lý Cửa hàng Thú cưng Mio
- Hình 2.2: Sơ đồ Activity cho Use Case Bán hàng POS & Thanh toán (UC03)
- Hình 2.3: Sơ đồ Sequence cho Use Case Bán hàng POS & Thanh toán (UC03)
- Hình 2.4: Sơ đồ Activity cho Luồng Đổi trả hàng hóa chuyển sang POS (UC06)
- Hình 2.5: Sơ đồ Sequence cho Luồng Đổi trả hàng hóa chuyển sang POS (UC06)
- Hình 2.6: Sơ đồ Activity cho Luồng Bàn giao ca và Gửi chốt két chờ duyệt (UC07)
- Hình 2.7: Sơ đồ Sequence cho Luồng Quản lý xét duyệt bàn giao ca (UC08)
- Hình 3.1: Sơ đồ Kiến trúc 3 tầng (3-Tier Architecture) của Hệ thống
- Hình 3.2: Sơ đồ Class Diagram tổng thể phân hệ Nghiệp vụ & Đa hình Hóa đơn
- Hình 3.3: Sơ đồ Mô hình Thực thể Kết hợp Mở rộng (EER Diagram)
- Hình 3.4: Sơ đồ CSDL Quan hệ (Relational Database Diagram - 18 Bảng)
- Hình 3.5: Sơ đồ Phân luồng Màn hình (Screen Flow) và Phím tắt Toàn cục
- Hình 3.6: Giao diện Đăng nhập hệ thống (LoginForm)
- Hình 3.7: Giao diện Khung điều khiển chính (MainDashboard - Dual Navigation)
- Hình 3.8: Giao diện Bán hàng POS (BanHangPOSPanel - 2 Phương thức thanh toán & Phím tiền nhanh)
- Hình 3.9: Hộp thoại Thanh toán VietQR Napas 24/7 (VietQRDialog)
- Hình 3.10: Biểu mẫu Xem trước & In Hóa đơn K80 (HoaDonPreviewDialog)
- Hình 3.11: Giao diện Đổi trả hàng hóa (DoiTraHangPanel - Chuyển sang POS)
- Hình 3.12: Giao diện Bàn giao ca & Đối soát két tiền (BanGiaoCaPanel - Chờ Quản lý duyệt)
- Hình 3.13: Hộp thoại Quản lý xét duyệt ca và In biên bản giao ca K80
- Hình 3.14: Giao diện Quản lý Kho, Sản phẩm & Lô hàng cận date FEFO (KhoLoHangPanel)
- Hình 3.15: Giao diện Quản lý Lịch hẹn & Dịch vụ Spa 4 bước (QuanLyLichHenPanel)
- Hình 3.16: Giao diện Khách sạn Thú cưng dạng lưới Matrix (PetHotelPanel)
- Hình 3.17: Giao diện Quản lý Hồ sơ thú cưng, Sức khỏe & Điểm VIP (ThuCungDichVuPanel)
- Hình 3.18: Giao diện Ma trận Phân ca tuần (LichPhanCaPanel - Timetable)
- Hình 3.19: Giao diện Quản lý Nhân sự & Phân quyền (QuanLyNhanVienPanel)
- Hình 3.20: Giao diện Trung tâm Báo biểu Quản trị 7 phân hệ (BaoCaoThongKeDialog)

---

## DANH MỤC CÁC BẢNG BIỂU
- Bảng 0.1: Bảng 14 câu hỏi - đáp khảo sát thu thập yêu cầu từ thực tế
- Bảng 1.1: Bảng phân công 4 vai trò tác nhân và phạm vi nghiệp vụ
- Bảng 2.1: Ma trận phân quyền tác nhân với 18 Use Cases hệ thống (RBAC Matrix)
- Bảng 2.2 đến 2.19: Bảng đặc tả chi tiết 18 Use Cases hệ thống
- Bảng 3.1: Danh mục 26 lớp Thực thể và DTO trong hệ thống
- Bảng 3.2: Danh mục 12 lớp Truy xuất dữ liệu (DAO)
- Bảng 3.3 đến 3.20: Đặc tả cấu trúc chi tiết 18 bảng Cơ sở dữ liệu SQL Server
- Bảng 3.21: Danh mục 22 lớp Giao diện người dùng Swing (UI Inventory)
- Bảng 3.22: Bảng ánh xạ hệ thống phím tắt toàn cục
- Bảng 3.23: Danh mục 45 kịch bản kiểm thử chức năng thực tế (Test Cases Catalog)
- Bảng 5.1: Cơ cấu tổ chức nhân sự công ty khởi nghiệp
- Bảng 5.2: Lộ trình 3 giai đoạn phát triển sản phẩm (2026 - 2028)

---

## KHẢO SÁT THỰC TRẠNG KINH DOANH VÀ THU THẬP YÊU CẦU

### 1. Bối cảnh thị trường và hiện trạng vận hành ngoài đời thực

Trong những năm gần đây, thị trường dịch vụ và sản phẩm dành cho thú cưng tại Việt Nam đang trải qua giai đoạn bùng nổ mạnh mẽ với tốc độ tăng trưởng bình quân trên 15%/năm. Sự thay đổi trong nhận thức xã hội – khi thú cưng không còn đơn thuần là vật nuôi trông nhà mà đã trở thành những "thành viên gia đình bốn chân" (Pet Humanization) – đã thúc đẩy các chủ nuôi chi tiêu ngày càng nhiều cho thức ăn dinh dưỡng cao cấp, thuốc thú y, mỹ phẩm làm đẹp, phụ kiện thời trang và các dịch vụ chăm sóc chuyên sâu như Spa, Grooming tạo kiểu, tắm sấy dưỡng lông và khách sạn thú cưng lưu trú (Pet Hotel).

Tuy nhiên, qua khảo sát thực tế tại các cửa hàng thú cưng quy mô vừa và nhỏ (từ 1 đến 3 chi nhánh độc lập) trên địa bàn TP. Hồ Chí Minh, nhóm nhận thấy đa số các cửa hàng vẫn duy trì phương thức quản trị bán thủ công truyền thống:
- Sử dụng sổ tay giấy hoặc phần mềm bảng tính Excel rời rạc để ghi chép doanh thu bán lẻ từng ngày.
- Đặt lịch hẹn Spa và phòng khách sạn qua tin nhắn Zalo, Facebook cá nhân của nhân viên, dẫn đến tình trạng bỏ sót lịch hẹn hoặc chồng chéo khung giờ dịch vụ.
- Chủ cửa hàng trực tiếp kiểm soát toàn bộ khâu nhập hàng, quản lý kho bãi và công nợ nhà cung cấp; nhân viên cửa hàng được phân công theo ca làm việc nhưng thiếu công cụ hỗ trợ kiểm kê dòng tiền mặt và trách nhiệm giải trình cuối ngày.

### 2. Các khó khăn và lỗ hổng vận hành thực tế

Khảo sát chuyên sâu chỉ ra 4 "điểm đau" (pain points) lớn nhất mà các chủ cửa hàng thú cưng đang phải đối mặt hàng ngày:

1. **Lỗ hổng bảo mật và thất thoát két tiền thu ngân:**
   - Khi sử dụng chung tài khoản phần mềm hoặc sổ sách, nhân viên thấy được toàn bộ giá vốn hàng nhập của chủ cửa hàng.
   - Hiện tượng nhân viên tự ý giảm giá cho người quen, thu tiền mặt của khách nhưng không xuất hóa đơn, hoặc xóa đơn hàng sau khi khách rời đi gây thất thoát tài chính nghiêm trọng.
   - Vào cuối mỗi ca làm việc, nhân viên thu ngân và chủ cửa hàng phải mất từ 45 đến 60 phút để đếm từng tờ tiền lẻ, dò lại từng cuống phiếu để tìm chênh lệch giữa tiền mặt thực tế trong két và doanh thu lý thuyết.
2. **Thiếu quản lý dữ liệu khách hàng và lịch sử chăm sóc thú cưng:**
   - Cửa hàng không có cơ sở dữ liệu tập trung lưu trữ số điện thoại, ngày sinh của thú cưng, lịch sử tiêm phòng vaccine dại hay tiền sử dị ứng sữa tắm.
   - Khách hàng không được tích điểm thành viên, không có chính sách khách hàng thân thiết (VIP Loyalty), dẫn đến tỷ lệ giữ chân khách hàng (Retention Rate) rất thấp trước sự cạnh tranh của các chuỗi thú cưng lớn.
3. **Chồng chéo dịch vụ Spa & Tắc nghẽn phòng khách sạn Pet Hotel:**
   - Lịch hẹn tắm rửa, cắt tỉa lông thường bị dồn vào các khung giờ cao điểm cuối tuần. Việc nhân viên không nắm được thời lượng thực hiện dịch vụ (thường kéo dài từ 60 đến 120 phút) dẫn đến tình trạng thú cưng phải chờ đợi lâu, gây căng thẳng cho bé và bức xúc cho chủ nuôi.
   - Đối với dịch vụ lưu trú Pet Hotel, nhân viên không kiểm soát được tình trạng phòng trống, phòng đang dọn dẹp hoặc phòng đang có khách theo thời gian thực, dẫn đến nhận vượt quá số chuồng sẵn có.
4. **Hao hụt kho hàng do cận Date (FEFO) và chiết rót quy đổi đơn vị tính:**
   - Các sản phẩm dinh dưỡng, sữa bột, thực phẩm chức năng và thuốc thú y có hạn sử dụng (HSD) rất nghiêm ngặt. Phương pháp quản lý kho truyền thống chỉ lưu tổng số lượng tồn kho mà không quản lý theo từng Lô nhập (Lot Number), dẫn đến tình trạng nhân viên lấy nhầm hàng mới nhập bán trước, trong khi các lô hàng cũ cận date bị tồn đọng và phải tiêu hủy, gây thiệt hại vốn lớn.
   - Nghiệp vụ bán lẻ thú cưng có đặc thù chiết rót xả bao: nhập bao thức ăn hạt 10kg hoặc 20kg nhưng bán lẻ theo lạng (100g) hoặc gói 500g; nhập cát vệ sinh bao lớn xả lẻ theo kg; nhập lốc pate 6 lon bán lẻ từng lon. Do thiếu cơ chế quy đổi tỷ lệ đóng gói tự động, kho hàng thường xuyên bị sai lệch số liệu tồn kho cơ sở.

### 3. Sáu quy trình nghiệp vụ thực tế ngoài đời thực

Từ kết quả khảo sát thực địa, nhóm đã tổng hợp và chuẩn hóa 6 quy trình nghiệp vụ vận hành then chốt:
1. **Quy trình Quản lý lịch hẹn Spa/Grooming:** Tiếp nhận nhu cầu của khách hàng qua hotline/quầy -> Kiểm tra tình trạng sức khỏe bé -> Phân công kỹ thuật viên và xếp khung giờ (Time Slot) -> Cập nhật tiến độ 4 bước (Tiếp nhận, Đang làm, Hoàn thành chờ đón, Đã thanh toán) -> Chuyển sang POS thu ngân.
2. **Quy trình Quản lý lưu trú Pet Hotel:** Tiếp nhận bé -> Kiểm tra tiêm chủng vaccine -> Check-in gán phòng trên sơ đồ chuồng -> Ghi chép nhật ký dinh dưỡng và thể trạng hàng ngày -> Check-out tính tổng tiền phòng và dịch vụ phát sinh -> Chuyển sang POS.
3. **Quy trình Bán lẻ sản phẩm tại quầy POS:** Quét mã vạch hoặc chọn nhanh từ lưới Catalog -> Áp dụng nguyên tắc xuất kho FEFO (ưu tiên lô cận date trước) -> Tích lũy điểm thành viên -> Thu ngân xác nhận số tiền khách đưa -> Hệ thống tính tiền thối thời gian thực -> In hóa đơn nhiệt K80.
4. **Quy trình Gộp hóa đơn liên phân hệ:** Khách hàng vừa mua phụ kiện/thức ăn tại quầy, vừa cho bé làm dịch vụ cắt tỉa lông và lưu trú phòng -> Hệ thống tích hợp toàn bộ các khoản thu vào một mã hóa đơn duy nhất (`HDxxxxxx`), áp dụng một mức chiết khấu chung và tích điểm đồng bộ.
5. **Quy trình Xử lý Đổi trả hàng hóa:** Khách mang sản phẩm lỗi hoặc không vừa size đến quầy trong vòng 7 ngày -> Thu ngân tra cứu hóa đơn gốc -> Chọn sản phẩm trả và kiểm tra tem mác -> Nếu đổi sang sản phẩm khác, hệ thống chuyển thông tin sang màn hình POS để khấu trừ tiền hàng cũ và thu thêm phần chênh lệch trực tiếp tại POS.
6. **Quy trình Bàn giao ca và Đối soát két tiền:** Hết ca trực, thu ngân mở giao diện giao ca -> Hệ thống tự động tính toán tiền mặt lý thuyết trong két (Tiền lẻ đầu ca + Thu tiền mặt bán hàng - Chi hoàn tiền trả hàng) -> Thu ngân kiểm đếm tiền thực tế và nhập vào hệ thống -> Nếu có chênh lệch, bắt buộc nhập giải trình -> Bấm chốt ca lưu trạng thái chờ Quản lý duyệt -> Thu ngân đăng xuất an toàn -> Quản lý đăng nhập vào hệ thống để phê duyệt và in biên bản bàn giao K80.

### 4. Bảng 14 câu hỏi - đáp (Q&A) phỏng vấn thu thập yêu cầu từ thực tế

*(Trích xuất trực tiếp từ tài liệu khảo sát yêu cầu thực địa `N07_2_ApplicationDevelopment_QAs.docx`)*

**Bảng 0.1: Bảng 14 câu hỏi - đáp khảo sát thu thập yêu cầu từ thực tế**

| STT | Câu hỏi khảo sát thực tế (Questions) | Câu trả lời từ thực tế (Answers) | Ghi chú & Ràng buộc kỹ thuật |
|:---:|---|---|---|
| **1** | Ứng dụng có cần phân quyền cho các nhóm người sử dụng không? | Có. Hệ thống cần phân quyền theo vai trò để kiểm soát chức năng: <br>- *Quản lý:* Toàn quyền quản trị, phân ca, duyệt ca, nhập kho và xem báo cáo tài chính.<br>- *Thu ngân:* Bán hàng POS, xử lý tiền thối, in hóa đơn, đổi trả và chốt két ca trực.<br>- *Bán hàng:* Tư vấn phụ kiện, tra cứu tồn kho, tiếp nhận đổi trả.<br>- *Chăm sóc:* Cập nhật tiến độ Spa, nhật ký Pet Hotel và hồ sơ sức khỏe bé. | Phân quyền dựa trên vai trò người dùng (Role-Based Access Control - RBAC). |
| **2** | Mật khẩu tài khoản đăng nhập có cần điều kiện gì không? | Tối thiểu 8 ký tự, bao gồm cả chữ hoa, chữ thường, chữ số và ký tự đặc biệt để đảm bảo an toàn thông tin. | Ràng buộc bảo mật mật khẩu trong `DangNhapController`. |
| **3** | Chương trình có bao nhiêu nhóm người sử dụng? | Có 4 nhóm người sử dụng nội bộ tương ứng 4 chức danh: Quản lý cửa hàng (`NVQL`), Thu ngân (`NVTN`), Bán hàng (`NVBH`), và Chăm sóc/Kỹ thuật viên (`NVCS`). | 4 Roles trong `UserSession.java` và bảng `TaiKhoan`. |
| **4** | Ngày hẹn dịch vụ thú cưng có cần ràng buộc điều kiện gì không? | Ngày hẹn không được nhỏ hơn ngày hiện tại. Giờ hẹn phải nằm trong khung giờ làm việc của cửa hàng (từ 08:00 đến 20:00 hàng ngày). | Ràng buộc nghiệp vụ kiểm tra ngày giờ trong `DatLichHenDialog`. |
| **5** | Người sử dụng có hay dùng phím Enter khi nhập dữ liệu không? | Có. Thu ngân và nhân viên bán hàng thường xuyên dùng phím Enter để tìm kiếm nhanh sản phẩm hoặc nhảy sang ô nhập tiếp theo để tối ưu tốc độ tại quầy. | Lắng nghe sự kiện `ActionListener` trên các trường nhập liệu và phím tắt toàn cục. |
| **6** | Số lượng thú cưng tối đa được đăng ký dưới tên một khách hàng là bao nhiêu? | Không giới hạn tổng số thú cưng nuôi của một chủ, nhưng một lần đặt lịch hẹn chỉ được chọn tối đa 3 bé để tránh quá tải thợ Spa. | Ràng buộc `dsChiTiet.size() <= 3` trong entity `LichHen.java`. |
| **7** | Số lượng phụ kiện tối đa cho phép mua trong một hóa đơn bán lẻ là bao nhiêu? | Không giới hạn số lượng món hàng trong một đơn, nhưng số lượng bán phải nhỏ hơn hoặc bằng tồn kho khả dụng của lô hàng. | Kiểm tra tồn kho tức thời trong `LoHangDAO` và `BanHangPOSPanel`. |
| **8** | Trạng thái của một lịch hẹn dịch vụ chăm sóc thú cưng được quản lý như thế nào? | Gồm 4 trạng thái nối tiếp: `Chờ tiếp nhận`, `Đang thực hiện`, `Đã xong - Chờ đón`, và `Đã hoàn tất thanh toán`. | Quản lý vòng đời dịch vụ 4 bước trên `QuanLyLichHenPanel`. |
| **9** | Khi lập hóa đơn thanh toán chung cho cả phụ kiện và dịch vụ, hệ thống có cần gom chung hay tách riêng không? | Hóa đơn được gộp chung thành một mã hóa đơn duy nhất (`HDxxxxxx`) bao gồm cả dòng sản phẩm và dòng dịch vụ chăm sóc. | Áp dụng mô hình Đa hình `ChiTietHoaDon` (Lớp cha trừu tượng). |
| **10** | Sản phẩm khi đạt đến mức tồn kho tối thiểu thì hệ thống có hiển thị cảnh báo không? | Có. Khi tồn kho của sản phẩm chạm hoặc thấp hơn ngưỡng an toàn (Điểm đặt hàng ROP), hệ thống đổi màu cảnh báo đỏ trên màn hình Kho. | Tính toán điểm đặt hàng tự động (ROP) trong `KhoLoHangPanel`. |
| **11** | Điểm tích lũy thành viên của khách hàng được tính toán dựa trên công thức nào? | Mỗi 100.000 VNĐ thanh toán thực tế = 1 điểm tích lũy. Mỗi 1 điểm dùng để giảm trừ 1.000 VNĐ cho lần mua sắm tiếp theo. | Công thức tích điểm khách hàng thân thiết trong `HoaDonDAO.java`. |
| **12** | Nhân viên có được phép tự ý sửa đổi giá bán của phụ kiện hoặc đơn giá dịch vụ trực tiếp trên hóa đơn không? | Tuyệt đối không. Bảng giá bán được niêm yết cố định. Chỉ Quản lý mới có quyền điều chỉnh bảng giá trong mục Thiết lập quy đổi. | Khóa chỉnh sửa ô đơn giá trên bảng giỏ hàng POS. |
| **13** | Thời gian và điều kiện hoàn trả hàng như thế nào? | Khách được đổi trả trong vòng 07 ngày kể từ ngày mua, kèm hóa đơn gốc. Hàng nguyên tem được hoàn lại kho; hàng lỗi đưa vào diện hủy. | Nghiệp vụ kiểm tra thời hạn và hoàn kho trong `DoiTraHangPanel`. |
| **14** | Hàng hóa có quản lý theo Lô và HSD không? | Có quản lý chặt chẽ theo Số lô và Ngày hết hạn (đối với thức ăn, thuốc) và quy đổi đơn vị tính (đối với hàng chiết rót đóng gói). | Thuật toán xuất kho FEFO và bảng `QuyDoiDonVi` trong CSDL. |


# CHƯƠNG 1: GIỚI THIỆU

## 1.1. Tổng quan đề tài
Trong kỷ nguyên chuyển đổi số và xu hướng đô thị hóa nhanh chóng tại Việt Nam, ngành dịch vụ thú cưng đang có bước chuyển mình mạnh mẽ từ các cửa hàng truyền thống tự phát sang các mô hình chuỗi tích hợp chuyên nghiệp. Việc sở hữu một hệ thống phần mềm quản lý toàn diện không chỉ giúp chuẩn hóa quy trình vận hành nội bộ mà còn trực tiếp nâng cao trải nghiệm khách hàng và năng lực cạnh tranh của doanh nghiệp.

Đề tài **"Hệ thống Quản lý Cửa hàng Thú cưng Mio"** được xây dựng nhằm giải quyết bài toán quản trị thực tế của các cửa hàng thú cưng vừa và nhỏ. Bằng việc kết hợp hài hòa giữa mô hình bán lẻ tại quầy (POS), quản lý kho hàng thông minh theo lô cận date (FEFO), tích hợp thanh toán số hiện đại (VietQR Napas 24/7), cùng với các nghiệp vụ chuyên biệt về chăm sóc thú cưng (Spa/Grooming 4 bước, Khách sạn thú cưng Pet Hotel, Hồ sơ vi mạch Microchip), hệ thống mang lại một giải pháp công nghệ toàn diện, minh bạch và tối ưu chi phí vận hành.

## 1.2. Mục tiêu đề tài

### 1.2.1. Mục tiêu tổng quát
Xây dựng một ứng dụng máy tính (Desktop Application) hoàn chỉnh, hoạt động độc lập, có tính ổn định cao, bảo mật chặt chẽ và giao diện trực quan, đáp ứng trọn vẹn mọi nghiệp vụ quản lý hàng ngày của một cửa hàng thú cưng hiện đại.

### 1.2.2. Mục tiêu cụ thể
1. **Chuẩn hóa quy trình bán hàng POS tại quầy:** Khóa cứng đúng 2 phương thức thanh toán an toàn (Tiền mặt và Chuyển khoản VietQR); hỗ trợ các phím chọn mệnh giá tiền nhanh (100k, 200k, 500k, Đủ tiền); tự động tính tiền thối thời gian thực và ngăn chặn triệt để tình trạng thanh toán khi khách đưa thiếu tiền mặt.
2. **Tối ưu hóa quy trình đổi trả hàng hóa:** Tích hợp luồng đổi hàng sang sản phẩm khác chuyển tiếp trực tiếp sang màn hình POS để khấu trừ tiền hàng cũ và thu phần chênh lệch minh bạch.
3. **Minh bạch hóa quản lý tài chính và ca trực:** Thiết lập cơ chế bàn giao ca bất đồng bộ; lưu trữ trạng thái chốt ca chờ Quản lý phê duyệt; loại bỏ việc đăng nhập quản lý tại quầy; tự động xuất biên bản đối soát két tiền nhiệt K80.
4. **Kiểm soát kho hàng chặt chẽ theo phương pháp FEFO:** Quản lý hàng hóa theo từng Lô nhập và Hạn sử dụng; tự động ưu tiên xuất lô cận hạn trước; hỗ trợ hệ số quy đổi đơn vị tính đóng gói và cảnh báo điểm đặt hàng an toàn (ROP).
5. **Nâng cao chất lượng dịch vụ chăm sóc thú cưng:** Quản lý quy trình Spa 4 bước; theo dõi tình trạng phòng khách sạn Pet Hotel dạng lưới trực quan; quản lý hồ sơ vi mạch Microchip và sổ theo dõi sức khỏe y tế.

### 1.2.3. Mục tiêu phát triển mở rộng
Thiết kế kiến trúc hệ thống theo mô hình 3 tầng (3-Tier Architecture) chuẩn mực hướng đối tượng, đảm bảo tính module hóa cao, sẵn sàng cho việc nâng cấp lên nền tảng đám mây (Cloud SaaS) và tích hợp ứng dụng di động cho khách hàng trong các giai đoạn tiếp theo.

## 1.3. Phạm vi đề tài

### 1.3.1. Phạm vi ứng dụng nghiệp vụ
Hệ thống bao quát toàn diện 6 phân hệ nghiệp vụ cốt lõi:
- **Phân hệ Bán hàng & Thu ngân (POS):** Lập hóa đơn bán lẻ, gộp hóa đơn liên phân hệ, chiết khấu thành viên, thanh toán Tiền mặt & VietQR, in hóa đơn nhiệt K80.
- **Phân hệ Đổi trả hàng hóa:** Tra cứu hóa đơn cũ trong 7 ngày, đổi hàng sang POS, hoàn trả tồn kho hàng nguyên tem, khấu trừ điểm VIP.
- **Phân hệ Bàn giao ca & Quản lý két tiền:** Đối soát doanh thu tiền mặt, chốt két ca trực chờ Quản lý duyệt, in biên bản bàn giao ca.
- **Phân hệ Kho & Quản lý Lô hàng FEFO:** Nhập kho sản phẩm & lô hàng mới, theo dõi hạn dùng 3 cấp màu, thiết lập quy đổi đơn vị tính và điểm đặt hàng ROP.
- **Phân hệ Dịch vụ Spa & Khách sạn Thú cưng (Pet Hotel):** Đặt lịch hẹn, phân công kỹ thuật viên, cập nhật tiến độ Spa 4 bước, check-in/check-out phòng lưu trú.
- **Phân hệ Khách hàng, Thú cưng & Nhân sự:** Hồ sơ thú cưng, gắn mã Microchip, điểm VIP thành viên, thẻ Spa Pass, phân công thời khóa biểu tuần (4 ca x 7 ngày), báo biểu thống kê doanh thu.

### 1.3.2. Phạm vi kỹ thuật
- **Ngôn ngữ lập trình:** Java thuần túy (JDK 17 LTS).
- **Giao diện người dùng:** Java Swing kết hợp thư viện giao diện hiện đại FlatLaf 3.4.1 (Theme `FlatMacLightLaf`).
- **Hệ thống đồ họa:** Vector Icon vẽ trực tiếp bằng `Graphics2D` (Lucide Icons chống răng cưa), biểu đồ thống kê thuần không phụ thuộc thư viện ngoài.
- **Hệ quản trị CSDL:** Microsoft SQL Server 2019/2022 (quản lý qua kết nối JDBC Driver `mssql-jdbc-12.8.1.jre11`).
- **Tích hợp thanh toán:** Chuẩn VietQR NAPAS 24/7 động kết nối API VietQR và Cổng VNPay Sandbox v2.1.0 với Local Loopback Server.

### 1.3.3. Phạm vi triển khai và người dùng
Hệ thống được thiết kế triển khai trên hệ điều hành Windows (Windows 10, Windows 11) cho 4 nhóm người dùng nội bộ cửa hàng:

**Bảng 1.1: Bảng phân công 4 vai trò tác nhân và phạm vi nghiệp vụ**

| Tác nhân | Ký hiệu Role | Tài khoản mẫu | Phạm vi quyền hạn nghiệp vụ |
|---|:---:|:---:|---|
| **Quản lý Hệ thống** | `NVQL` | `admin` | Toàn quyền điều hành hệ thống: duyệt bàn giao ca, phân ca làm việc tuần, cấp tài khoản nhân sự, nhập kho FEFO, xem toàn bộ báo cáo tài chính quản trị. |
| **Nhân viên Thu ngân** | `NVTN` | `thungan` | Thực hiện bán hàng POS, quét mã VietQR, tính tiền thối, xử lý đổi trả hàng, check-in/check-out Pet Hotel, chốt két bàn giao ca cuối ngày. |
| **Nhân viên Bán hàng** | `NVBH` | `banhang` | Tư vấn phụ kiện/thức ăn tại quầy, tra cứu tồn kho, tiếp nhận yêu cầu đổi trả hàng hóa, quản lý thông tin khách hàng thân thiết. |
| **Nhân viên Chăm sóc** | `NVCS` | `chamsoc` | Kỹ thuật viên Grooming/Spa: theo dõi lịch hẹn được phân công, cập nhật tiến độ 4 bước, chăm sóc dinh dưỡng Pet Hotel, ghi chép thể trạng bé. |

## 1.4. Yêu cầu chức năng của hệ thống (Functional Requirements)
Hệ thống tuân thủ cấu trúc phân rã 6 nhóm chức năng chuẩn Slide 17 IUH:
1. **Nhóm 1: Hệ thống**
   - FR01: Đăng nhập hệ thống có xác thực mật khẩu và phân quyền vai trò.
   - FR02: Đổi mật khẩu tài khoản cá nhân có kiểm tra độ dài và xác nhận.
   - FR03: Đăng xuất phiên làm việc an toàn và khóa màn hình.
2. **Nhóm 2: Danh mục**
   - FR04: Quản lý danh mục hàng hóa, chủng loại sản phẩm và đơn vị tính đo lường.
   - FR05: Quản lý danh mục dịch vụ Spa, cắt tỉa lông và bảng giá phòng Pet Hotel.
   - FR06: Quản lý hồ sơ nhà cung cấp và thông tin liên hệ.
3. **Nhóm 3: Cập nhật**
   - FR07: Nhập kho sản phẩm & Lô hàng mới gắn kèm Hạn sử dụng (FEFO).
   - FR08: Cập nhật thông tin khách hàng, thẻ thành viên VIP và tích lũy điểm thưởng.
   - FR09: Đăng ký hồ sơ thú cưng, gắn mã vi mạch Microchip và sổ sức khỏe.
   - FR10: Thiết lập bảng quy đổi đơn vị tính đa cấp và đơn giá bán lẻ tương ứng.
4. **Nhóm 4: Xử lý**
   - FR11: Bán hàng POS tại quầy (2 phương thức Tiền mặt / VietQR, phím tiền nhanh, tiền thối thời gian thực, chặn thiếu tiền, in hóa đơn K80).
   - FR12: Lưu tạm và khôi phục đơn hàng chờ tại quầy.
   - FR13: Xử lý Đổi trả hàng hóa (Kịch bản A hoàn tiền, Kịch bản B chuyển sang POS thanh toán chênh lệch).
   - FR14: Tiếp nhận lịch hẹn Spa, phân công KTV và cập nhật tiến độ 4 bước.
   - FR15: Quản lý lưu trú Pet Hotel, ma trận phòng, check-in, check-out sang POS.
   - FR16: Bàn giao ca thu ngân lưu trạng thái chờ Quản lý duyệt và ký nhận két tiền.
5. **Nhóm 5: Tìm kiếm**
   - FR17: Tra cứu lịch sử hóa đơn bán lẻ, tìm kiếm khách hàng theo SĐT, tra cứu hạn dùng lô hàng.
6. **Nhóm 6: Báo biểu**
   - FR18: Thống kê doanh thu theo thời gian, biểu đồ tăng trưởng cột và tròn Graphics2D, xuất dữ liệu 7 phân hệ báo cáo ra Excel (.xlsx).

## 1.5. Yêu cầu phi chức năng (Non-Functional Requirements)
- **NFR01 - Hiệu năng (Performance):** Tối ưu hóa khởi động hệ thống với cơ chế nạp màn hình theo yêu cầu (Lazy Loading On-Demand), thời gian mở ứng dụng đạt `< 1.0 giây`. Thao tác thanh toán và ghi nhận CSDL đạt dưới 50ms.
- **NFR02 - Bảo mật (Security):** Mật khẩu người dùng được lưu trữ an toàn, phân quyền kiểm soát truy cập nghiêm ngặt theo vai trò (RBAC). Thu ngân không thể thấy giá vốn hàng hóa và không thể tự ý sửa giá bán niêm yết.
- **NFR03 - Độ tin cậy & Tính sẵn sàng (Availability):** Cơ chế kết nối Dual-Fallback thông minh tự động luân chuyển giữa Port 1433 và Named Instance SQLEXPRESS; cơ chế Offline Fallback cho phép sinh mã VietQR cục bộ ngay cả khi mất kết nối mạng Internet.
- **NFR04 - Tính toàn vẹn dữ liệu (Data Integrity):** Toàn bộ giao dịch thanh toán, trừ kho FEFO, đổi trả hàng và phân ca đều được bảo vệ trong các Transaction JDBC với cam kết ACID (`setAutoCommit(false)`, `commit()`, `rollback()`), loại bỏ hoàn toàn nguy cơ âm kho hoặc sai lệch tài chính.
- **NFR05 - Trải nghiệm người dùng (Usability & UX):** Giao diện phẳng FlatMacLightLaf bo góc mềm mại, độ tương phản màu đạt chuẩn WCAG, hệ thống phím tắt toàn cục (`F1`-`F6`, `F9`, `Ctrl+G`, `Ctrl+M`, `Ctrl+Q`) giúp nhân viên thao tác quầy với tốc độ cao.
- **NFR06 - Khả năng tương thích (Compatibility):** Hoạt động ổn định trên nền tảng Windows 10 và Windows 11 với Java Runtime Environment (JRE 17+), tương thích với các dòng máy in hóa đơn nhiệt tiêu chuẩn khổ 80mm (K80) và máy quét mã vạch 1D/2D.

---

# CHƯƠNG 2: PHÂN TÍCH HỆ THỐNG

## 2.1. Mô hình Use Case tổng quát
Hệ thống Quản lý Cửa hàng Thú cưng Mio được phân rã thành các gói Use Case chuyên biệt, bao quát toàn bộ hoạt động từ quầy bán hàng, kho bãi, dịch vụ thú y đến quản trị nhân sự và tài chính.

```
[Chèn sơ đồ Use Case Tổng Quát Hệ Thống vào]


```

## 2.2. Danh sách các tác nhân hệ thống
1. **Quản Lý Hệ Thống (`NVQL` - Tài khoản: `admin`):**
   - Đại diện cho Chủ cửa hàng hoặc Quản lý chi nhánh.
   - Quyền hạn: Phê duyệt bàn giao ca và ký nhận két tiền cuối ngày; phân ca làm việc tuần cho toàn bộ nhân viên; cấp phát, khóa và đổi vai trò tài khoản nhân sự; nhập kho hàng hóa mới theo lô FEFO; thiết lập quy đổi đơn vị tính và điểm đặt hàng ROP; xem và xuất toàn bộ 7 phân hệ báo cáo tài chính quản trị.
2. **Nhân Viên Thu Ngân (`NVTN` - Tài khoản: `thungan`):**
   - Đại diện cho nhân sự túc trực tại quầy thanh toán.
   - Quyền hạn: Lập hóa đơn bán lẻ POS; thao tác chọn mệnh giá nhanh, tiền khách đưa và kiểm soát tiền thối; kích hoạt thanh toán VietQR Napas 24/7; tiếp nhận dữ liệu đổi trả hàng từ `DoiTraHangPanel` để thanh toán phần tiền bù chênh lệch; lưu tạm và khôi phục đơn hàng; check-in/check-out Pet Hotel; chốt ca và gửi bản ghi bàn giao ca vào trạng thái chờ Quản lý duyệt.
3. **Nhân Viên Bán Hàng Tại Quầy (`NVBH` - Tài khoản: `banhang`):**
   - Đại diện cho nhân viên tư vấn bán lẻ thức ăn, thuốc và phụ kiện thú cưng.
   - Quyền hạn: Tư vấn bán hàng tại quầy POS; tra cứu tồn kho sản phẩm; tiếp nhận yêu cầu đổi trả sản phẩm từ khách hàng trong vòng 7 ngày; quản lý thông tin khách hàng thành viên và điểm tích lũy VIP.
4. **Nhân Viên Chăm Sóc Thú Cưng / Kỹ Thuật Viên (`NVCS` - Tài khoản: `chamsoc`):**
   - Đại diện cho kỹ thuật viên chuyên trách Grooming, cắt tỉa lông, tắm sấy và chăm sóc lưu trú.
   - Quyền hạn: Xem danh sách các bé được phân công cho mình trong ca trực; cập nhật tiến độ dịch vụ Spa 4 bước; theo dõi và ghi chép nhật ký dinh dưỡng, thể trạng tại khách sạn thú cưng Pet Hotel; cập nhật hồ sơ y tế, tiêm phòng và vi mạch Microchip.

## 2.3. Danh mục Use Case và Ma trận phân quyền (RBAC)

**Bảng 2.1: Ma trận phân quyền tác nhân với 18 Use Cases hệ thống (RBAC Matrix)**

| Mã Use Case | Tên Chức Năng Nghiệp Vụ | NVQL | NVTN | NVBH | NVCS |
|:---|:---|:---:|:---:|:---:|:---:|
| **UC01** | Đăng nhập & Xác thực phiên làm việc | ✓ | ✓ | ✓ | ✓ |
| **UC02** | Đổi mật khẩu tài khoản cá nhân | ✓ | ✓ | ✓ | ✓ |
| **UC03** | Bán hàng POS (Tiền mặt / VietQR) | ✓ | ✓ | ✓ | ✗ |
| **UC04** | Lưu tạm & Khôi phục hóa đơn chờ | ✓ | ✓ | ✓ | ✗ |
| **UC05** | Tra cứu lịch sử HĐ & In lại hóa đơn K80 | ✓ | ✓ | ✓ | ✗ |
| **UC06** | Xử lý Đổi trả hàng hóa & Chuyển sang POS | ✓ | ✓ | ✓ | ✗ |
| **UC07** | Bàn giao ca & Gửi chốt két chờ duyệt | ✓ | ✓ | ✗ | ✗ |
| **UC08** | Phê duyệt đối soát két bàn giao ca (K80) | ✓ | ✗ | ✗ | ✗ |
| **UC09** | Tiếp nhận & Đặt lịch hẹn Spa/Grooming | ✓ | ✓ | ✓ | ✗ |
| **UC10** | Cập nhật tiến độ dịch vụ Spa 4 bước | ✓ | ✗ | ✗ | ✓ |
| **UC11** | Quản lý Khách sạn Thú cưng (Pet Hotel) | ✓ | ✓ | ✗ | ✓ |
| **UC12** | Quản lý Hồ sơ thú cưng, Microchip & Sức khỏe | ✓ | ✓ | ✓ | ✓ |
| **UC13** | Quản lý Khách hàng Thành viên & Điểm VIP | ✓ | ✓ | ✓ | ✗ |
| **UC14** | Quản lý Thẻ Hội Viên & Gói Spa Pass | ✓ | ✓ | ✓ | ✗ |
| **UC15** | Nhập kho Sản phẩm & Lô hàng mới (FEFO) | ✓ | ✗ | ✗ | ✗ |
| **UC16** | Thiết lập Quy đổi ĐVT & Điểm đặt hàng ROP | ✓ | ✗ | ✗ | ✗ |
| **UC17** | Phân công ca làm việc tuần (Timetable Matrix)| ✓ | ✗ | ✗ | ✗ |
| **UC18** | Xem báo biểu thống kê & Xuất Excel 7 loại | ✓ | ✓ | ✗ | ✓ |

---

## 2.4. Đặc tả chi tiết 18 Use Cases hệ thống

### 2.4.1. Use Case UC01: Đăng nhập & Xác thực phiên làm việc
**Bảng 2.2: Đặc tả Use Case UC01**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC01** |
| **Tên Use Case** | **Đăng nhập & Xác thực phiên làm việc** |
| **Tác nhân** | Tất cả người dùng (NVQL, NVTN, NVBH, NVCS) |
| **Mục tiêu** | Xác thực danh tính người dùng và thiết lập phiên làm việc được phân quyền tương ứng. |
| **Tiền điều kiện** | Ứng dụng đã khởi chạy, cửa sổ `LoginForm` hiển thị trên màn hình. |
| **Sự kiện kích hoạt** | Người dùng nhập thông tin đăng nhập và nhấn "Đăng nhập" hoặc bấm phím `Enter`. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Người dùng nhập Tên đăng nhập và Mật khẩu trên giao diện `LoginForm`.<br>2. Người dùng nhấn nút "ĐĂNG NHẬP" hoặc nhấn phím `Enter`.<br>3. Hệ thống chuyển tiếp thông tin tới `DangNhapController.xuLyDangNhap()`.<br>4. Controller chuẩn hóa chuỗi và kiểm tra tính hợp lệ qua `TaiKhoanDAO.kiemTraDangNhap()`.<br>5. Hệ thống truy vấn CSDL: kiểm tra tên đăng nhập, so khớp mật khẩu và kiểm tra cờ trạng thái `trangThaiHoatDong == true`.<br>6. Hệ thống truy xuất thông tin nhân viên tương ứng từ `NhanVienDAO`.<br>7. Khởi tạo đối tượng phiên làm việc tĩnh `UserSession.initSession(nhanVien)`.<br>8. Đóng cửa sổ `LoginForm`, khởi chạy `MainDashboard` với danh sách menu và tab chức năng đã được lọc tự động theo vai trò. |
| **Luồng thay thế / Ngoại lệ (Alternative Flows)** | - *4a. Để trống trường dữ liệu:* Hệ thống hiển thị thông báo yêu cầu nhập đầy đủ tên đăng nhập và mật khẩu.<br>- *5a. Sai tên đăng nhập hoặc mật khẩu:* Hệ thống hiển thị cảnh báo "Mật khẩu không chính xác! Vui lòng thử lại", xóa trắng ô mật khẩu và đặt con trỏ chuột focus lại.<br>- *5b. Tài khoản bị vô hiệu hóa:* Hệ thống hiển thị cảnh báo tài khoản đang bị khóa và từ chối đăng nhập. |
| **Hậu điều kiện** | Phiên làm việc được thiết lập trong `UserSession`, giao diện chính mở ở chế độ toàn màn hình. |

---

### 2.4.2. Use Case UC02: Đổi mật khẩu tài khoản cá nhân
**Bảng 2.3: Đặc tả Use Case UC02**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC02** |
| **Tên Use Case** | **Đổi mật khẩu tài khoản cá nhân** |
| **Tác nhân** | Người dùng đang đăng nhập bất kỳ |
| **Mục tiêu** | Cho phép nhân viên chủ động thay đổi mật khẩu bảo vệ tài khoản cá nhân. |
| **Tiền điều kiện** | Người dùng đã đăng nhập thành công vào hệ thống. |
| **Sự kiện kích hoạt** | Người dùng nhấn phím tắt `Ctrl+M` hoặc chọn menu *1. Hệ thống -> Đổi mật khẩu tài khoản*. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hệ thống hiển thị hộp thoại Đổi mật khẩu yêu cầu nhập: Mật khẩu hiện tại, Mật khẩu mới, và Xác nhận mật khẩu mới.<br>2. Người dùng điền đầy đủ các trường và nhấn nút "XÁC NHẬN".<br>3. Hệ thống kiểm tra: Mật khẩu mới phải có độ dài tối thiểu 4 ký tự; Mật khẩu mới và Xác nhận mật khẩu mới phải hoàn toàn trùng khớp.<br>4. Gọi `DangNhapController.doiMatKhau()` để cập nhật mật khẩu vào bảng `TaiKhoan` trong CSDL SQL Server.<br>5. Hệ thống hiển thị thông báo đổi mật khẩu thành công. |
| **Luồng thay thế / Ngoại lệ** | - *3a. Nhập sai mật khẩu hiện tại:* Hiển thị thông báo mật khẩu cũ không đúng.<br>- *3b. Mật khẩu xác nhận không khớp:* Hiển thị thông báo hai mật khẩu mới không trùng khớp. |
| **Hậu điều kiện** | Mật khẩu mới được cập nhật vào CSDL, có hiệu lực ngay trong lần đăng nhập tiếp theo. |

---

### 2.4.3. Use Case UC03: Bán hàng POS & Thanh toán (Tiền mặt / VietQR)
**Bảng 2.4: Đặc tả Use Case UC03**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC03** |
| **Tên Use Case** | **Bán hàng POS & Thanh toán (Tiền mặt / VietQR)** |
| **Tác nhân** | Quản lý (`NVQL`), Thu ngân (`NVTN`), Bán hàng (`NVBH`) |
| **Mục tiêu** | Lập hóa đơn bán lẻ, tính toán tiền thối thời gian thực, xử lý thanh toán Tiền mặt hoặc VietQR và in hóa đơn K80. |
| **Tiền điều kiện** | Đang mở tab `BanHangPOSPanel` (hoặc bấm `F1`). |
| **Sự kiện kích hoạt** | Thu ngân đưa sản phẩm vào giỏ hàng và nhấn nút "THANH TOÁN (F9)". |
| **Luồng sự kiện chính (Basic Flow - Tiền mặt)** | 1. Thu ngân nhấp chuột chọn sản phẩm/dịch vụ từ lưới thẻ Catalog hoặc gõ mã/tên vào ô tìm kiếm.<br>2. Nhập số điện thoại khách hàng để tra cứu thông tin tích điểm thành viên và ưu đãi thẻ Spa Pass.<br>3. Hệ thống tự động tính: Tổng tiền hàng, Giảm giá chiết khấu và Số tiền cần thanh toán (`canTra`).<br>4. Tại combo box phương thức thanh toán (`cboPhuongThuc`), thu ngân chọn `"Tiền mặt"`.<br>5. Thu ngân nhập số tiền thực tế khách đưa vào ô `txtTienKhachDua` hoặc bấm nhanh các nút mệnh giá (100k, 200k, 500k, Đủ tiền).<br>6. Hệ thống tự động kích hoạt `DocumentListener` trên ô nhập tiền, tính toán tiền thối lại theo công thức: `tienThoi = tienKhachDua - canTra` và cập nhật tức thời nhãn `lblTienThoiLai` (màu xanh lá nếu đủ tiền).<br>7. Thu ngân nhấn "THANH TOÁN (F9)".<br>8. Hệ thống kiểm tra tiền khách đưa `>= canTra`, mở Transaction JDBC, trừ tồn kho lô hàng theo nguyên tắc FEFO trong `LoHangDAO`, lưu hóa đơn vào `HoaDonDAO`, cộng điểm tích lũy thành viên mới, commit giao dịch và hiển thị `HoaDonPreviewDialog` để in nhiệt K80. |
| **Luồng thay thế (Alternative Flow - Chuyển khoản VietQR)** | - *4a. Chọn phương thức "Chuyển khoản":*<br>  1. Ô `txtTienKhachDua` tự động điền bằng `canTra` và bị khóa mờ (`setEnabled(false)`), nhãn tiền thối hiển thị `"0 đ"`.<br>  2. Thu ngân bấm "THANH TOÁN (F9)". Hộp thoại `VietQRDialog` xuất hiện hiển thị mã QR động chuẩn NAPAS 24/7 gắn sẵn số tài khoản MB Bank, số tiền và nội dung chuyển khoản.<br>  3. Khách hàng quét mã QR trên ứng dụng ngân hàng. Thu ngân kiểm tra sao kê/điện thoại và bấm `F9` xác nhận giao dịch thành công.<br>  4. Hệ thống hoàn tất lưu trữ CSDL và xuất phiếu in nhiệt K80. |
| **Luồng ngoại lệ (Exception Flow - Chặn thiếu tiền mặt)** | - *7a. Khách đưa thiếu tiền mặt (`tienKhachDua < canTra`):*<br>  Hệ thống **chặn đứng hoàn toàn quy trình thanh toán**, hiển thị hộp thoại cảnh báo: "Tiền khách đưa chưa đủ để hoàn tất thanh toán! Còn thiếu: X,XXX đ", không lưu hóa đơn vào CSDL và tự động bôi đen toàn bộ số trong ô `txtTienKhachDua` để thu ngân nhập lại. |
| **Hậu điều kiện** | Hóa đơn được lưu vào CSDL với trạng thái `"Đã thanh toán"`, tồn kho lô hàng FEFO giảm, điểm VIP khách hàng được tích lũy. |

```
[Chèn sơ đồ Activity cho Use Case Bán hàng POS & Thanh toán (UC03) vào]


```

```
[Chèn sơ đồ Sequence cho Use Case Bán hàng POS & Thanh toán (UC03) vào]


```

---

### 2.4.4. Use Case UC04: Lưu tạm & Khôi phục hóa đơn chờ
**Bảng 2.5: Đặc tả Use Case UC04**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC04** |
| **Tên Use Case** | **Lưu tạm & Khôi phục hóa đơn chờ** |
| **Tác nhân** | NVQL, NVTN, NVBH |
| **Mục tiêu** | Tạm hoãn đơn hàng đang dở dang để thanh toán cho khách hàng tiếp theo mà không làm mất dữ liệu giỏ hàng. |
| **Tiền điều kiện** | Đang có ít nhất 1 sản phẩm trong giỏ hàng POS nhưng khách chưa thể thanh toán ngay. |
| **Sự kiện kích hoạt** | Thu ngân nhấn nút "Lưu tạm" trên thanh điều khiển POS. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Thu ngân nhấn nút "Lưu tạm".<br>2. Hệ thống đóng gói toàn bộ danh sách sản phẩm trong giỏ hàng, thông tin khách hàng vào đối tượng bộ nhớ `DonHangTam`.<br>3. Thêm đơn hàng tạm vào danh sách tab "Hóa đơn chờ (Đang thao tác)".<br>4. Làm sạch giỏ hàng POS về trạng thái rỗng để phục vụ khách hàng tiếp theo.<br>5. Khi khách quay lại quầy, thu ngân mở tab "Hóa đơn chờ", chọn đơn hàng và nhấn "Nạp lại giỏ hàng".<br>6. Giỏ hàng và thông tin khách hàng được khôi phục nguyên vẹn 100% lên màn hình POS. |
| **Hậu điều kiện** | Đơn hàng chờ được phục hồi hoàn hảo, không gây gián đoạn luồng phục vụ tại quầy. |

---

### 2.4.5. Use Case UC05: Tra cứu lịch sử HĐ & In lại hóa đơn K80
**Bảng 2.6: Đặc tả Use Case UC05**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC05** |
| **Tên Use Case** | **Tra cứu lịch sử HĐ & In lại hóa đơn K80** |
| **Tác nhân** | NVQL, NVTN, NVBH |
| **Mục tiêu** | Kiểm tra thông tin các hóa đơn đã thanh toán trong quá khứ và hỗ trợ in lại hóa đơn cho khách. |
| **Tiền điều kiện** | Mở tab "Lịch sử hóa đơn" trong `BanHangPOSPanel`. |
| **Sự kiện kích hoạt** | Thu ngân nhập mã hóa đơn, tên khách hoặc lọc khoảng thời gian. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hệ thống truy vấn CSDL qua `HoaDonDAO.getAllHoaDon()` hiển thị bảng lịch sử hóa đơn.<br>2. Thu ngân nhấp đúp chuột vào dòng hóa đơn cần tra cứu.<br>3. Cửa sổ `HoaDonPreviewDialog` mở ra hiển thị đầy đủ chi tiết: mã HĐ, thu ngân, thời gian, danh sách món, đơn giá, chiết khấu, phương thức thanh toán, tiền khách đưa, tiền thối lại.<br>4. Thu ngân bấm nút "In Hóa Đơn (K80)" để in nhiệt hoặc "Xuất File Hóa Đơn (.txt)". |
| **Hậu điều kiện** | Hóa đơn được tra cứu và in ấn thành công. |

---

### 2.4.6. Use Case UC06: Xử lý Đổi trả hàng hóa & Chuyển sang POS thanh toán
**Bảng 2.7: Đặc tả Use Case UC06**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC06** |
| **Tên Use Case** | **Xử lý Đổi trả hàng hóa & Chuyển sang POS thanh toán** |
| **Tác nhân** | Quản lý (`NVQL`), Bán hàng (`NVBH`), Thu ngân (`NVTN`) |
| **Mục tiêu** | Tiếp nhận hàng trả từ khách trong vòng 7 ngày; hoàn tiền tại chỗ hoặc chuyển thông tin sang POS để thanh toán chênh lệch. |
| **Tiền điều kiện** | Đang mở màn hình `DoiTraHangPanel` (hoặc phím tắt `F4`). |
| **Sự kiện kích hoạt** | Khách mang sản phẩm đã mua đến yêu cầu đổi sản phẩm khác hoặc trả hàng. |
| **Luồng sự kiện chính (Basic Flow - Kịch bản B: Đổi hàng chuyển sang POS)** | 1. Thu ngân nhập Mã hóa đơn gốc hoặc Số điện thoại khách hàng vào thanh tìm kiếm.<br>2. Hệ thống tải thông tin hóa đơn và hiển thị danh sách các sản phẩm đã mua.<br>3. Thu ngân tích chọn sản phẩm khách muốn trả, nhập số lượng trả, chọn lý do và tình trạng tem mác (nguyên tem/hỏng lỗi).<br>4. Thu ngân chọn radio `"Kịch bản B: Đổi sang sản phẩm khác"`.<br>5. Chọn sản phẩm mới dự kiến đổi và số lượng từ danh mục hàng hóa.<br>6. Nút hành động đổi thành `"CHUYỂN SANG BÁN HÀNG POS ĐỂ THANH TOÁN (F9)"` màu xanh lá.<br>7. Thu ngân nhấn nút chuyển sang POS. Hệ thống đóng gói đối tượng DTO `DoiTraTransferData` chứa mã HĐ gốc, tổng tiền khấu trừ, danh sách món trả và món đổi.<br>8. `MainDashboard` tự động điều hướng sang `BanHangPOSPanel`, gọi hàm `pos.tiepNhanDoiTraHang(data)`.<br>9. Giao diện POS hiển thị banner màu vàng cam thông báo khấu trừ đổi trả, nạp món mới vào giỏ hàng.<br>10. Khách hàng có thể chọn mua thêm các món hàng khác nếu có nhu cầu.<br>11. Hệ thống tính số tiền chênh lệch cần thu thêm (hoặc số tiền thừa hoàn trả). Thu ngân thực hiện thu tiền và bấm thanh toán.<br>12. CSDL lưu đồng thời hóa đơn bán mới và bản ghi `PhieuDoiTra` tương ứng. |
| **Luồng thay thế (Alternative Flow - Kịch bản A: Trả hàng hoàn tiền)** | - *4a. Chọn "Kịch bản A: Trả hàng - Hoàn tiền tại chỗ":*<br>  1. Hệ thống tính toán tổng tiền hoàn trả và số điểm tích lũy cần khấu trừ.<br>  2. Thu ngân bấm "XÁC NHẬN & IN PHIẾU HOÀN TIỀN (80MM)".<br>  3. Hệ thống lưu `PhieuDoiTra`, nếu hàng nguyên tem thì cộng hoàn lại tồn kho trong `LoHangDAO`, xuất phiếu in nhiệt K80. |
| **Hậu điều kiện** | Sản phẩm trả được kiểm soát kho minh bạch, hóa đơn mới được lập cho các sản phẩm đổi, chênh lệch tài chính được thu đủ. |

```
[Chèn sơ đồ Activity cho Luồng Đổi trả hàng hóa chuyển sang POS (UC06) vào]


```

```
[Chèn sơ đồ Sequence cho Luồng Đổi trả hàng hóa chuyển sang POS (UC06) vào]


```

---

### 2.4.7. Use Case UC07: Bàn giao ca làm việc & Gửi chốt két chờ duyệt
**Bảng 2.8: Đặc tả Use Case UC07**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC07** |
| **Tên Use Case** | **Bàn giao ca làm việc & Gửi chốt két chờ duyệt** |
| **Tác nhân** | Thu ngân (`NVTN`), Quản lý (`NVQL`) |
| **Mục tiêu** | Thu ngân kết thúc ca trực, đối soát tiền mặt trong két và gửi bản ghi bàn giao ca vào trạng thái chờ Quản lý duyệt. |
| **Tiền điều kiện** | Đang ở cuối ca trực làm việc, mở phân hệ `BanGiaoCaPanel` (hoặc `Ctrl+G`). |
| **Sự kiện kích hoạt** | Thu ngân kiểm đếm tiền mặt thực tế và nhấn "CHỐT CA & BÀN GIAO KÉT TIỀN...". |
| **Luồng sự kiện chính (Basic Flow)** | 1. Thu ngân mở giao diện `BanGiaoCaPanel`.<br>2. Hệ thống tự động tổng hợp số liệu CSDL trong ngày: Tiền lẻ đầu ca + Doanh thu tiền mặt - Chi hoàn tiền trả hàng = Tiền mặt lý thuyết trong két.<br>3. Thu ngân đếm tiền mặt thực tế trong két và nhập vào ô `txtTienThucTe` (hoặc bấm "Đúng bằng LT").<br>4. Hệ thống tự động tính độ lệch: `tienChenhLech = tienThucTe - tienLyThuyet`.<br>5. Thu ngân nhập lý do giải trình vào ô ghi chú nếu có chênh lệch.<br>6. Thu ngân bấm **"CHỐT CA & BÀN GIAO KÉT TIỀN..."**.<br>7. **Hệ thống không yêu cầu mật khẩu quản lý tại chỗ.** Tự động sinh mã giao ca `GCxxx`, lưu bản ghi vào bảng `BangGiaoCa` với trạng thái `"⏳ Chờ Quản lý duyệt \| [Lý do]"`.<br>8. Hiển thị thông báo thành công và đề xuất: *"Bạn có muốn ĐĂNG XUẤT ngay bây giờ để Quản lý đăng nhập vào duyệt két không?"*.<br>9. Thu ngân chọn "Có" để đăng xuất an toàn ra màn hình `LoginForm`. |
| **Luồng ngoại lệ** | - *5a. Két bị lệch tiền nhưng để trống lý do:* Hệ thống cảnh báo yêu cầu thu ngân bắt buộc nhập lý do giải trình trước khi cho phép chốt ca. |
| **Hậu điều kiện** | Bản ghi giao ca được ghi nhận vào SQL Server với trạng thái chờ duyệt; thu ngân đăng xuất phiên làm việc. |

```
[Chèn sơ đồ Activity cho Luồng Bàn giao ca và Gửi chốt két chờ duyệt (UC07) vào]


```

---

### 2.4.8. Use Case UC08: Phê duyệt đối soát két bàn giao ca (K80)
**Bảng 2.9: Đặc tả Use Case UC08**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC08** |
| **Tên Use Case** | **Phê duyệt đối soát két bàn giao ca (K80)** |
| **Tác nhân** | Quản lý hệ thống (`NVQL`) |
| **Mục tiêu** | Quản lý kiểm tra tính chính xác của két tiền ca trước và ký duyệt biên bản bàn giao ca. |
| **Tiền điều kiện** | Người dùng có vai trò `NVQL` đăng nhập vào hệ thống, mở `BanGiaoCaPanel`. |
| **Sự kiện kích hoạt** | Quản lý nhấn nút "✓ QUẢN LÝ DUYỆT & XÁC NHẬN KÉT CA ĐANG CHỌN". |
| **Luồng sự kiện chính (Basic Flow)** | 1. Quản lý mở phân hệ `BanGiaoCaPanel`, nút Duyệt ca hiển thị rõ ràng màu xanh lá.<br>2. Quản lý bấm nút Duyệt ca. Hộp thoại đối soát hiển thị danh sách tất cả các ca đang chờ duyệt lấy từ `dao.getDanhSachCaChoDuyet()`.<br>3. Quản lý kiểm tra chi tiết các chỉ số kế toán: Tiền lẻ đầu ca, Thu tiền mặt, Chi hoàn trả, Tiền lý thuyết, Tiền đếm thực tế, Độ lệch và Lý do giải trình của thu ngân.<br>4. Quản lý bấm nút **"✓ PHÊ DUYỆT ĐỐI SOÁT & KÝ NHẬN KÉT (IN K80)"**.<br>5. Hệ thống gọi `BangGiaoCaDAO.capNhatGhiChuDuyetCa()` cập nhật trạng thái bản ghi thành: `"✓ Đã duyệt (QL: [Họ tên QL]) \| [Ghi chú]"`.<br>6. Hệ thống hiển thị biểu mẫu Biên bản bàn giao ca nhiệt K80 đầy đủ số liệu kế toán và chữ ký xác nhận của hai bên để in ấn lưu trữ. |
| **Hậu điều kiện** | Bản ghi giao ca chuyển sang trạng thái đã duyệt, số liệu kế toán được chốt chính thức. |

```
[Chèn sơ đồ Sequence cho Luồng Quản lý xét duyệt bàn giao ca (UC08) vào]


```

---

### 2.4.9. Use Case UC09: Tiếp nhận & Đặt lịch hẹn Spa/Grooming
**Bảng 2.10: Đặc tả Use Case UC09**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC09** |
| **Tên Use Case** | **Tiếp nhận & Đặt lịch hẹn Spa/Grooming** |
| **Tác nhân** | NVQL, NVBH, NVTN |
| **Mục tiêu** | Đặt lịch dịch vụ làm đẹp cho thú cưng, phân công kỹ thuật viên và kiểm soát khung giờ tránh trùng lịch. |
| **Tiền điều kiện** | Đang mở màn hình `QuanLyLichHenPanel`. |
| **Sự kiện kích hoạt** | Bấm nút "Tiếp nhận lịch hẹn mới" (hoặc `Ctrl+L`). |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hộp thoại `DatLichHenDialog` mở ra với bố cục 2 cột cân đối.<br>2. Cột trái: Nhập thông tin khách hàng (SĐT, Họ tên) và thông tin thú cưng (Tên bé, Giống loài, Cân nặng, Ghi chú thể trạng).<br>3. Cột phải: Chọn loại dịch vụ Spa/Grooming, phân công KTV phụ trách, chọn ngày hẹn và nhấp chọn khung giờ Time Slot khả dụng (08:30, 09:30, 10:30, 13:30, 14:30...).<br>4. Chọn các dịch vụ cộng thêm (Add-on: Cạo vôi răng, Xịt dưỡng lông thơm lâu...).<br>5. Bấm nút "Lưu Lịch Hẹn".<br>6. Hệ thống kiểm tra: Ngày hẹn `>= Ngày hiện tại`, số lượng thú cưng `<= 3`. Mở Transaction lưu vào bảng `LichHen` và `ChiTietLichHen` với trạng thái `"Chờ tiếp nhận"`.<br>7. Lịch hẹn mới xuất hiện trên bảng theo dõi tiến độ. |
| **Hậu điều kiện** | Lịch hẹn dịch vụ được ghi nhận vào CSDL, sẵn sàng cho kỹ thuật viên tiếp nhận phục vụ. |

---

### 2.4.10. Use Case UC10: Cập nhật tiến độ dịch vụ Spa 4 bước
**Bảng 2.11: Đặc tả Use Case UC10**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC10** |
| **Tên Use Case** | **Cập nhật tiến độ dịch vụ Spa 4 bước** |
| **Tác nhân** | Nhân viên chăm sóc (`NVCS`), Quản lý (`NVQL`) |
| **Mục tiêu** | Theo dõi và cập nhật thời gian thực quy trình thực hiện dịch vụ chăm sóc thú cưng tại cửa hàng. |
| **Tiền điều kiện** | Lịch hẹn đã được tiếp nhận trong ngày trên `QuanLyLichHenPanel`. |
| **Sự kiện kích hoạt** | Kỹ thuật viên nhấp chọn lịch hẹn và bấm nút chuyển bước tiến độ. |
| **Luồng sự kiện chính (Basic Flow)** | 1. KTV xem danh sách các bé được phân công cho mình trên bảng theo dõi.<br>2. KTV cập nhật tuần tự qua 4 bước:<br>   - *Bước 1: Tiếp nhận bé* (Kiểm tra da lông, cân nặng).<br>   - *Bước 2: Đang thực hiện* (Tắm sấy dưỡng lông, cắt tỉa vệ sinh tạo kiểu).<br>   - *Bước 3: Đã xong - Chờ đón* (Bé làm xong, gửi tin nhắn/gọi chủ đến đón).<br>   - *Bước 4: Hoàn tất & Thanh toán* (Chuyển dữ liệu sang POS để lập hóa đơn).<br>3. Khi hoàn tất, thu ngân bấm "Chuyển sang POS thanh toán" để nạp trực tiếp dịch vụ vào hóa đơn POS. |
| **Hậu điều kiện** | Trạng thái dịch vụ trong CSDL được cập nhật, tiền hoa hồng của KTV được ghi nhận. |

---

### 2.4.11. Use Case UC11: Quản lý Khách sạn Thú cưng (Pet Hotel)
**Bảng 2.12: Đặc tả Use Case UC11**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC11** |
| **Tên Use Case** | **Quản lý Khách sạn Thú cưng (Pet Hotel)** |
| **Tác nhân** | NVQL, NVTN, NVCS |
| **Mục tiêu** | Quản lý lưu trú thú cưng theo sơ đồ phòng trực quan, ghi chép nhật ký ăn uống và check-out thanh toán. |
| **Tiền điều kiện** | Truy cập phân hệ `PetHotelPanel` (hoặc `F6`). |
| **Sự kiện kích hoạt** | Khách gửi thú cưng lưu trú hoặc đến đón bé về. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Giao diện hiển thị ma trận 12 phòng/chuồng theo 3 hạng: Tiêu chuẩn, VIP Suite, Condo Mèo với phân màu trực quan (Xanh: Trống, Đỏ: Đang có khách, Vàng: Đang dọn dẹp).<br>2. *Quy trình Check-in:* Chọn phòng trống, bấm "Check-in Bé Mới", nhập tên bé, giống loài, chủ nuôi, ngày đón dự kiến, phòng đổi màu sang "ĐANG CÓ KHÁCH".<br>3. *Nhật ký dinh dưỡng:* Nhân viên chăm sóc tích chọn "Đã cho ăn sáng", "Đã cho ăn tối", "Đã dắt đi dạo", ghi chú sức khỏe hàng ngày.<br>4. *Quy trình Check-out:* Bấm "Check-out & Đẩy sang POS", hệ thống tính số ngày lưu trú `x` đơn giá phòng + phụ phí dịch vụ phát sinh và chuyển sang POS để thanh toán. |
| **Hậu điều kiện** | Phòng chuyển sang trạng thái "ĐANG DỌN DẸP", hóa đơn lưu trú được thanh toán trên POS. |

---

### 2.4.12. Use Case UC12: Quản lý Hồ sơ thú cưng, Microchip & Sức khỏe
**Bảng 2.13: Đặc tả Use Case UC12**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC12** |
| **Tên Use Case** | **Quản lý Hồ sơ thú cưng, Microchip & Sức khỏe** |
| **Tác nhân** | NVQL, NVCS, NVBH, NVTN |
| **Mục tiêu** | Quản lý thông tin định danh thú cưng, số vi mạch Microchip và sổ theo dõi khám sức khỏe y tế. |
| **Tiền điều kiện** | Truy cập `ThuCungDichVuPanel`. |
| **Sự kiện kích hoạt** | Nhân viên thêm hồ sơ thú cưng mới hoặc cập nhật bệnh án thể trạng. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Tra cứu theo số Microchip hoặc tên thú cưng / SĐT chủ nuôi.<br>2. Đăng ký bé mới: Nhập mã thú cưng, tên bé, giống loài, ngày sinh, giới tính và mã số vi mạch định danh (VD: `MC982001`).<br>3. Cập nhật hồ sơ sức khỏe: Ghi nhận cân nặng, thân nhiệt, tình trạng ve rận, cờ tiêm phòng dại (`tiemPhongDai == true`) và ghi chú thể trạng đặc thù.<br>4. Bấm "Lưu Hồ Sơ". Dữ liệu lưu vào bảng `ThuCung`, `HoSoThuCung` và `HoSoSucKhoe`. |
| **Hậu điều kiện** | Hồ sơ thú cưng và lịch sử y tế được lưu trữ an toàn trong CSDL. |

---

### 2.4.13. Use Case UC13: Quản lý Khách hàng Thành viên & Điểm VIP
**Bảng 2.14: Đặc tả Use Case UC13**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC13** |
| **Tên Use Case** | **Quản lý Khách hàng Thành viên & Điểm VIP** |
| **Tác nhân** | NVQL, NVBH, NVTN |
| **Mục tiêu** | Quản trị dữ liệu khách hàng thân thiết, theo dõi tích lũy điểm thưởng và phân hạng thành viên. |
| **Tiền điều kiện** | Mở tab "Khách hàng Thành viên & Điểm VIP" trong `ThuCungDichVuPanel`. |
| **Sự kiện kích hoạt** | Thêm mới khách hàng hoặc cập nhật thông tin liên hệ. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hệ thống hiển thị danh sách khách hàng kèm số điện thoại, điểm thưởng và hạng thẻ: Đồng (< 50 điểm), Bạc (50 - 199), Vàng (200 - 499), Kim Cương (>= 500).<br>2. Thao tác thêm/sửa thông tin khách hàng, số điện thoại duy nhất.<br>3. Tích hợp tự động: Mỗi khi thanh toán hóa đơn POS, hệ thống cộng điểm thưởng (100.000 đ = 1 điểm). Khách hàng có thể dùng điểm để giảm trừ trực tiếp trên hóa đơn (1 điểm = 1.000 đ). |
| **Hậu điều kiện** | Thông tin khách hàng và số dư điểm VIP được cập nhật vào CSDL. |

---

### 2.4.14. Use Case UC14: Quản lý Thẻ Hội Viên & Gói Spa Pass
**Bảng 2.15: Đặc tả Use Case UC14**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC14** |
| **Tên Use Case** | **Quản lý Thẻ Hội Viên & Gói Spa Pass** |
| **Tác nhân** | NVQL, NVBH, NVTN |
| **Mục tiêu** | Phát hành và quản lý gói dịch vụ Spa định kỳ trả trước (Punch Card 5 hoặc 10 buổi). |
| **Tiền điều kiện** | Mở tab "Thẻ Hội Viên & Gói Spa Pass" trong `ThuCungDichVuPanel`. |
| **Sự kiện kích hoạt** | Khách hàng đăng ký mua thẻ Spa Pass hoặc sử dụng lượt dịch vụ tại quầy. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hiển thị danh sách các thẻ Spa Pass đã cấp: Mã thẻ `PASS-{SĐT}-{STT}`, Tên khách, Loại gói, Tổng số buổi, Số buổi đã dùng, Số buổi còn lại, Hạn sử dụng (90-120 ngày).<br>2. Phát hành thẻ mới: Nhập thông tin khách, chọn gói 5 buổi hoặc 10 buổi, thu tiền gói.<br>3. Quẹt thẻ sử dụng tại POS: Khi khách làm dịch vụ Spa, thu ngân bấm "Dùng Spa Pass", hóa đơn được miễn trừ 100% tiền dịch vụ, hệ thống trừ 1 buổi còn lại trên thẻ qua phương thức `TheSpaPass.dungMotBuoi()`. |
| **Hậu điều kiện** | Thẻ Spa Pass được cập nhật số buổi trong CSDL. |

---

### 2.4.15. Use Case UC15: Nhập kho Sản phẩm & Lô hàng mới (FEFO)
**Bảng 2.16: Đặc tả Use Case UC15**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC15** |
| **Tên Use Case** | **Nhập kho Sản phẩm & Lô hàng mới (FEFO)** |
| **Tác nhân** | Quản lý hệ thống (`NVQL`) |
| **Mục tiêu** | Ghi nhận sản phẩm và lô hàng mới nhập từ nhà cung cấp kèm hạn sử dụng phục vụ xuất kho FEFO. |
| **Tiền điều kiện** | Đang mở phân hệ `KhoLoHangPanel`, bấm nút "Thêm Sản phẩm & Lô mới". |
| **Sự kiện kích hoạt** | Cửa hàng nhập hàng hóa mới về kho bãi. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Hộp thoại `NhapSanPhamVaLoHangDialog` mở ra với 2 khối card song song.<br>2. Khối Sản phẩm: Nhập Mã SP, Tên SP, Danh mục (Thức ăn, Phụ kiện, Thuốc thú y...), Nhà cung cấp, Đơn vị tính cơ sở, Giá nhập, Giá bán lẻ.<br>3. Khối Lô hàng & FEFO: Nhập Số lô (Lot Number), Ngày sản xuất, Hạn sử dụng, Số lượng nhập cơ sở.<br>4. Hệ thống tự động tính toán thời hạn sử dụng và gắn nhãn FEFO theo 3 màu: An toàn (> 6 tháng - Xanh), Cận Date (3-6 tháng - Vàng), Nguy cấp (< 3 tháng - Đỏ).<br>5. Bấm "Lưu & Nhập kho". Mở Transaction lưu sản phẩm vào `SanPhamDAO` và lô hàng vào `LoHangDAO`. |
| **Hậu điều kiện** | Tồn kho lô hàng được ghi nhận vào SQL Server, sẵn sàng xuất bán tại POS theo nguyên tắc FEFO. |

---

### 2.4.16. Use Case UC16: Thiết lập Quy đổi ĐVT & Điểm đặt hàng ROP
**Bảng 2.17: Đặc tả Use Case UC16**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC16** |
| **Tên Use Case** | **Thiết lập Quy đổi ĐVT & Điểm đặt hàng ROP** |
| **Tác nhân** | Quản lý hệ thống (`NVQL`) |
| **Mục tiêu** | Cấu hình tỷ lệ đóng gói quy đổi đơn vị tính và tính toán điểm tái đặt hàng tự động ngăn ngừa cạn kho. |
| **Tiền điều kiện** | Đang mở phân hệ `KhoLoHangPanel`. |
| **Sự kiện kích hoạt** | Quản lý bấm "Thiết lập Quy đổi ĐVT" hoặc chọn tab "Điểm Tái Đặt Hàng (ROP)". |
| **Luồng sự kiện chính (Basic Flow)** | 1. *Quy đổi ĐVT:* Mở `ThietLapQuyDoiDialog`. Chọn sản phẩm (VD: Pate mèo lon 85g), chọn đơn vị quy đổi mới (VD: Khay 12 lon), nhập tỷ lệ quy đổi (12) và giá bán riêng của khay. Lưu vào bảng `QuyDoiDonVi`.<br>2. *Điểm đặt hàng ROP:* Hệ thống tự động tính toán: `ROP = (Mức tiêu thụ trung bình ngày x Thời gian giao hàng) + Tồn kho an toàn`. Khi tổng tồn khả dụng `<= ROP`, hệ thống hiển thị cảnh báo đỏ và gợi ý số lượng cần nhập bổ sung. |
| **Hậu điều kiện** | Bảng giá quy cách đóng gói và cảnh báo ngưỡng an toàn kho được cập nhật. |

---

### 2.4.17. Use Case UC17: Phân công ca làm việc tuần (Timetable Matrix)
**Bảng 2.18: Đặc tả Use Case UC17**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC17** |
| **Tên Use Case** | **Phân công ca làm việc tuần (Timetable Matrix)** |
| **Tác nhân** | Quản lý hệ thống (`NVQL`) |
| **Mục tiêu** | Điều phối lịch trực nhân sự hàng tuần trên ma trận 4 ca x 7 ngày trực quan. |
| **Tiền điều kiện** | Truy cập phân hệ `LichPhanCaPanel` (hoặc `F2`). |
| **Sự kiện kích hoạt** | Quản lý sắp xếp lịch làm việc cho tuần mới. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Giao diện hiển thị bảng ma trận thời khóa biểu tuần: Trục dọc là 4 ca (Ca 1 Sáng, Ca 2 Trưa, Ca 3 Chiều, Ca 4 Tối), trục ngang là 7 ngày từ Thứ Hai đến Chủ Nhật.<br>2. Các ô chứa thẻ nhân viên phân màu theo vai trò (Xanh dương: Bán hàng, Xanh ngọc: Thu ngân, Cam: Kỹ thuật viên, Tím: Quản lý).<br>3. Quản lý nhấp chuột phải vào ô ca trực: Chọn "Thêm nhân viên vào ca", chọn nhân sự và phân bổ vị trí.<br>4. Có thể điều chuyển ca, đổi ghi chú hoặc xóa phân công.<br>5. Bấm nút "Xuất Excel" để xuất bảng thời khóa biểu in dán tại phòng nhân sự. |
| **Hậu điều kiện** | Lịch trực được lưu vào bảng `BangPhanCong` trong CSDL. |

---

### 2.4.18. Use Case UC18: Xem báo biểu thống kê & Xuất Excel 7 loại
**Bảng 2.19: Đặc tả Use Case UC18**

| Thuộc tính | Nội dung đặc tả chi tiết |
|---|---|
| **Mã Use Case** | **UC18** |
| **Tên Use Case** | **Xem báo biểu thống kê & Xuất Excel 7 loại** |
| **Tác nhân** | Quản lý (`NVQL`), Thu ngân (`NVTN`), Chăm sóc (`NVCS`) |
| **Mục tiêu** | Tổng hợp, phân tích dữ liệu hoạt động kinh doanh và xuất báo cáo tài chính quản trị. |
| **Tiền điều kiện** | Mở menu *6. Báo biểu* hoặc mở `BaoCaoThongKeDialog`. |
| **Sự kiện kích hoạt** | Người dùng chọn phân hệ báo cáo cần phân tích. |
| **Luồng sự kiện chính (Basic Flow)** | 1. Cửa sổ `BaoCaoThongKeDialog` mở ra với 7 tab báo cáo chuyên sâu:<br>   - Tab 1: Doanh thu & Lợi nhuận gộp theo ngày/tuần/tháng.<br>   - Tab 2: Top Sản phẩm & Dịch vụ bán chạy nhất.<br>   - Tab 3: Hiệu suất Nhân viên & Hoa hồng Kỹ thuật viên Spa.<br>   - Tab 4: Khách hàng VIP & Tỷ lệ quay lại.<br>   - Tab 5: Lưu trú Pet Hotel & Tỷ lệ Đổi trả hàng.<br>   - Tab 6: Cảnh báo Lô hàng Cận hạn sử dụng (FEFO).<br>   - Tab 7: Báo cáo Xuất - Nhập - Tồn kho tổng hợp.<br>2. Người dùng lọc theo kỳ báo cáo (Hôm nay, 7 ngày gần nhất, Tháng này, Quý này).<br>3. Nhấn nút "Xuất Báo Cáo Ra Excel (.xlsx)" để xuất file bảng tính hoàn chỉnh hoặc "In Báo Biểu (A4)". |
| **Hậu điều kiện** | Dữ liệu báo cáo được trích xuất chính xác và xuất ra tệp tin bảng tính. |


# CHƯƠNG 3: THIẾT KẾ VÀ HIỆN THỰC HỆ THỐNG

## 3.1. Mô hình lớp (Class Diagram) và Kiến trúc Tầng

### 3.1.1. Kiến trúc 3 tầng (3-Tier Architecture)
Hệ thống Quản lý Cửa hàng Thú cưng Mio được thiết kế tuân thủ nghiêm ngặt mô hình kiến trúc 3 tầng chuẩn công nghiệp:
1. **Tầng Giao diện (Presentation Tier - Package `views` & `views.charts`):**
   - Xây dựng trên nền tảng Java Swing kết hợp thư viện giao diện hiện đại FlatLaf (Theme `FlatMacLightLaf`).
   - Đảm nhận nhiệm vụ thu nhận tương tác của người dùng, bắt sự kiện phím tắt, hiển thị bảng biểu, đồ thị và kích hoạt chuyển đổi màn hình linh hoạt.
2. **Tầng Nghiệp vụ & Truy xuất Dữ liệu (Business & Data Access Tier - Package `controllers`, `models.entity`, `models.dao`):**
   - Chứa toàn bộ các quy tắc nghiệp vụ cốt lõi: kiểm tra tiền khách đưa, tính tiền thối thời gian thực, thuật toán xuất kho FEFO, quy đổi tỷ lệ đóng gói, đóng gói chuyển giao đổi trả `DoiTraTransferData`, tính toán chênh lệch két tiền và xét duyệt bàn giao ca.
   - Các lớp DAO chịu trách nhiệm thực thi các truy vấn SQL, quản lý Transaction ACID đảm bảo tính nhất quán dữ liệu.
3. **Tầng Cơ sở Dữ liệu (Data Tier - Microsoft SQL Server):**
   - Lưu trữ bền vững 18 bảng quan hệ chuẩn hóa, thực thi các ràng buộc toàn vẹn khóa ngoại `CASCADE`, kiểm tra miền giá trị `CHECK (soLuongTon >= 0)`, bảo vệ dữ liệu chống sai lệch.

```
[Chèn sơ đồ Kiến trúc 3 Tầng của Hệ thống vào]


```

```
[Chèn sơ đồ Class Diagram Tổng Thể vào]


```

### 3.1.2. Các nguyên lý thiết kế then chốt
- **Tính Đa hình trong Chi tiết Hóa đơn (Polymorphism):**
  Lớp trừu tượng `ChiTietHoaDon` được kế thừa bởi 2 lớp con cụ thể:
  * `ChiTietXuatLo`: Đại diện cho việc xuất bán hàng hóa sản phẩm từ một lô hàng cụ thể trong kho. Chứa thông tin số lô (`LoHang`), quy đổi đơn vị tính (`QuyDoiDonVi`) và số lượng cơ sở trừ kho.
  * `ChiTietDichVu`: Đại diện cho dịch vụ chăm sóc thú cưng tại quầy. Chứa thông tin dịch vụ (`DichVu`), thú cưng phục vụ (`ThuCung`), kỹ thuật viên thực hiện (`NhanVien`) và tiền trích hoa hồng.
  * Nhờ đa hình, một đối tượng `HoaDon` có thể chứa đồng thời cả hàng hóa bán lẻ và dịch vụ làm đẹp trong cùng một danh sách `List<ChiTietHoaDon>`, cho phép gộp hóa đơn và tính tổng tiền minh bạch.
- **Thuật toán Xuất kho FEFO (First Expired, First Out):**
  Trong `LoHangDAO.java`, hệ thống truy vấn lô hàng xuất bán bằng mệnh đề SQL:
  ```sql
  SELECT TOP 1 * FROM LoHang 
  WHERE maSanPham = ? AND soLuongTon > 0 AND hanSuDung >= CAST(GETDATE() AS DATE) 
  ORDER BY hanSuDung ASC
  ```
  Lô hàng có hạn dùng gần nhất sẽ luôn được ưu tiên xuất kho trước, ngăn ngừa hoàn toàn tình trạng tồn kho hàng cận date.
- **Quản lý Giao dịch JDBC Transaction (ACID Compliance):**
  Phương thức `HoaDonDAO.thanhToanHoaDon(HoaDon hd)` khởi tạo kết nối độc lập qua `DBConnection.getNewConnection()`, tắt chế độ tự động ghi nhận `conn.setAutoCommit(false)`, thực thi chuỗi thao tác: ghi hóa đơn -> ghi chi tiết đa hình -> trừ tồn kho lô hàng an toàn `WHERE soLuongTon >= ?` -> tích lũy điểm VIP -> `conn.commit()`. Nếu xảy ra bất kỳ lỗi ngoại lệ nào (ví dụ lô hàng bị mua hết đồng thời bởi quầy khác), hệ thống lập tức gọi `conn.rollback()`, đảm bảo kho không bao giờ bị âm.

### 3.1.3. Đặc tả danh mục 26 lớp Thực thể và DTO (`models.entity`)

**Bảng 3.1: Danh mục 26 lớp Thực thể và DTO trong hệ thống**

| STT | Tên Lớp | Thể Loại | Mục Đích Nghiệp Vụ & Thuộc Tính Chính |
|:---:|---|:---:|---|
| 1 | `TaiKhoan` | Entity | Quản lý tài khoản đăng nhập: `tenDangNhap` (PK), `matKhau`, `vaiTro` (NVQL, NVTN, NVBH, NVCS), `trangThaiHoatDong`. |
| 2 | `NhanVien` | Entity | Hồ sơ nhân viên: `maNhanVien` (PK), `hoTen`, `chucVu`, `soDienThoai`, `taiKhoan` (FK 1-1). |
| 3 | `CaLamViec` | Entity | Khung giờ ca trực: `maCa` (PK), `tenCa`, `gioBatDau` (LocalTime), `gioKetThuc` (LocalTime). |
| 4 | `BangPhanCong` | Entity | Lịch phân ca chi tiết: `maPhanCong` (PK), `nhanVien`, `caLamViec`, `ngayLamViec`, `trangThaiCa`, `ghiChu`. |
| 5 | `BangGiaoCa` | Entity | Biên bản bàn giao ca: `maGiaoCa` (PK), `nhanVien`, `caLamViec`, `tienDauCa`, `tienCuoiCaHeThong`, `tienCuoiCaThucTe`, `tienChenhLech`, `lyDoChenhLech`, `gioKetThucThucTe`. |
| 6 | `KhachHang` | Entity | Hồ sơ khách hàng: `maKhachHang` (PK), `hoTen`, `soDienThoai` (Unique), `diemTichLuy`. |
| 7 | `ThuCung` | Entity | Hồ sơ thú cưng: `maThuCung` (PK), `tenThuCung`, `giongLoai`, `ngaySinh`, `gioiTinh`. |
| 8 | `HoSoThuCung` | Entity | Liên kết chủ - thú cưng: `maHoSo` (PK), `khachHang`, `thuCung`, `soMicrochip`, `ngayDangKy`, `trangThaiNuoi`. |
| 9 | `HoSoSucKhoe` | Entity | Khám thể trạng y tế: `maSucKhoe` (PK), `thuCung`, `nhanVienKiemTra`, `canNang`, `nhietDo`, `tinhTrangVeRan`, `tiemPhongDai`, `ghiChuTheTrang`, `ngayKiemTra`. |
| 10 | `DichVu` | Entity | Danh mục dịch vụ Spa: `maDichVu` (PK), `tenDichVu`, `giaDichVu`, `thoiLuongPhut`. |
| 11 | `LichHen` | Entity | Lịch hẹn dịch vụ: `maLichHen` (PK), `khachHang`, `nhanVienTiepNhan`, `ngayHen`, `trangThaiLichHen`, `ghiChu`, `dsChiTiet` (tối đa 3 dịch vụ). |
| 12 | `ChiTietLichHen` | Entity | Dòng chi tiết lịch hẹn: `maChiTietLichHen` (PK), `lichHen`, `thuCung`, `dichVu`, `ktvChiDinh`. |
| 13 | `DanhMucHang` | Entity | Nhóm phân loại hàng: `maDanhMuc` (PK), `tenDanhMuc`, `moTa`. |
| 14 | `SanPham` | Entity | Sản phẩm hàng hóa: `maSanPham` (PK), `danhMucHang`, `tenSanPham`, `moTa`. |
| 15 | `DonViTinh` | Entity | Đơn vị đo lường: `maDonVi` (PK), `tenDonVi` (Gói, Lon, Chai, Lốc, Thùng). |
| 16 | `QuyDoiDonVi` | Entity | Bảng quy cách đóng gói: `maQuyDoi` (PK), `sanPham`, `donViTinh`, `tyLeQuyDoi`, `laDonViCoSo`, `giaBan`. |
| 17 | `NhaCungCap` | Entity | Đối tác cung ứng hàng: `maNhaCungCap` (PK), `tenNhaCungCap`, `diaChi`, `soDienThoai`. |
| 18 | `LoHang` | Entity | Lô hàng kho FEFO: `soLo` (PK), `sanPham`, `nhaCungCap`, `ngaySanXuat`, `hanSuDung`, `donGiaNhapCoSo`, `soLuongNhap`, `soLuongTon`. |
| 19 | `ChiTietHoaDon` | Abstract Entity | Lớp cha trừu tượng đa hình: `maChiTiet` (PK), `hoaDon`, `soLuong`, `donGiaLucBan`, `thanhTien`. Các phương thức trừu tượng: `tinhThanhTien()`, `getTenKhoanMuc()`, `getLoaiChiTiet()`. |
| 20 | `ChiTietXuatLo` | Sub-Entity | Dòng bán sản phẩm: kế thừa `ChiTietHoaDon`, bổ sung `loHang`, `quyDoiDonVi`, `soLuongXuatCoSo`. |
| 21 | `ChiTietDichVu` | Sub-Entity | Dòng làm dịch vụ: kế thừa `ChiTietHoaDon`, bổ sung `dichVu`, `thuCung`, `ktvThucHien`, `tienHoaHong`. |
| 22 | `HoaDon` | Entity | Hóa đơn tổng hợp: `maHoaDon` (PK), `khachHang`, `nhanVien`, `ngayLap`, `thoiGianThanhToan`, `giamGia`, `thanhTien`, `phuongThucThanhToan` (Tiền mặt/Chuyển khoản), `trangThaiHoaDon`, `dsChiTiet`. |
| 23 | `PhieuDoiTra` | Entity | Biên bản đổi trả hàng: `maPhieuDoiTra` (PK), `hoaDonGoc`, `nhanVien`, `ngayDoiTra`, `lyDoDoiTra`, `tongTienHoan`, `trangThaiDoiTra`, `dsChiTietDoiTra`. |
| 24 | `ChiTietDoiTra` | Entity | Dòng hàng đổi trả: `maChiTietDoiTra` (PK), `phieuDoiTra`, `sanPham`, `soLuongDoiTra`, `tienHoanLai`. |
| 25 | `DoiTraTransferData`| DTO | Đối tượng truyền dữ liệu trung gian giữa `DoiTraHangPanel` và `BanHangPOSPanel` qua `MainDashboard`. |
| 26 | `TheSpaPass` | Entity | Thẻ gói Spa định kỳ: `maThe` (PK), `sdtKhachHang`, `tenKhachHang`, `tenGoi`, `tongSoBuoi`, `soBuoiDaDung`, `soBuoiConLai`, `donGiaGoi`, `ngayKichHoat`, `ngayHetHan`, `trangThai`. |

### 3.1.4. Đặc tả danh mục 12 lớp Truy xuất Dữ liệu (`models.dao`)

**Bảng 3.2: Danh mục 12 lớp Truy xuất dữ liệu (DAO)**

| STT | Tên Lớp DAO | Bảng Thao Tác Trong CSDL | Các Phương Thức Trọng Tâm |
|:---:|---|---|---|
| 1 | `BangGiaoCaDAO` | `BangGiaoCa`, `HoaDon`, `PhieuDoiTra` | `insertBangGiaoCa()`, `chotKetCa()`, `getAllBangGiaoCa()`, `tinhDoanhThuTienMatTrongNgay()`, `tinhDoanhThuChuyenKhoanTrongNgay()`, `tinhTongTienHoanTraTrongNgay()`, `capNhatGhiChuDuyetCa()`, `getDanhSachCaChoDuyet()`, `phatSinhMaGiaoCa()`. |
| 2 | `HoaDonDAO` | `HoaDon`, `ChiTietHoaDon`, `ChiTietXuatLo`, `ChiTietDichVu`, `LoHang`, `KhachHang` | `thanhToanHoaDon(HoaDon)` (Transaction ACID), `luuPhieuDoiTra()`, `getAllHoaDon()`, `findById()`, `getChiTietByHoaDon()`, `layMapSoLuongDaTra()`, `huyHoaDon()`. |
| 3 | `LoHangDAO` | `LoHang`, `SanPham`, `NhaCungCap` | `getLoHangFEFOBySanPham()` (Thuật toán FEFO), `getAvailableLotsFEFO()`, `layDanhSachCanDate()`, `truTonKho()`, `congTonKho()`, `getTongTonKhaDung()`, `insertLoHang()`. |
| 4 | `SanPhamDAO` | `SanPham`, `DanhMucHang`, `DonViTinh`, `QuyDoiDonVi` | `getAllSanPham()`, `getSanPhamById()`, `insertSanPham()`, `updateSanPham()`, `deleteSanPham()`, `getQuyDoiBySanPham()`, `insertQuyDoi()`. |
| 5 | `KhachHangDAO` | `KhachHang` | `getAllKhachHang()`, `findBySoDienThoai()`, `findByMaKhachHang()`, `insert()`, `update()`, `capNhatDiemTichLuy()`. |
| 6 | `ThuCungDAO` | `ThuCung`, `HoSoThuCung`, `HoSoSucKhoe` | `getAllHoSoThuCung()`, `getThuCungByKhachHang()`, `insertThuCung()`, `updateThuCung()`, `deleteThuCung()`, `getAllHoSoSucKhoe()`, `insertHoSoSucKhoe()`. |
| 7 | `DichVuDAO` | `DichVu` | `getAllDichVu()`, `getDichVuById()`, `insertDichVu()`, `updateDichVu()`, `deleteDichVu()`. |
| 8 | `LichHenDAO` | `LichHen`, `ChiTietLichHen`, `KhachHang`, `ThuCung` | `getAllLichHen()`, `insertLichHen()`, `capNhatTrangThai()`, `capNhatKtvChiDinh()`, `deleteLichHen()`. |
| 9 | `NhanVienDAO` | `NhanVien`, `TaiKhoan` | `findByTenDangNhap()`, `findByMaNhanVien()`, `getAllNhanVien()`, `insertNhanVien()`, `updateNhanVien()`, `updateVaiTro()`, `resetMatKhau()`, `toggleTrangThai()`. |
| 10 | `TaiKhoanDAO` | `TaiKhoan` | `findByTenDangNhap()`, `kiemTraDangNhap()`, `doiMatKhau()`, `getAllTaiKhoan()`, `insertTaiKhoan()`. |
| 11 | `PhanCaDAO` | `CaLamViec`, `BangPhanCong`, `NhanVien` | `getAllCaLamViec()`, `getAllPhanCong()`, `insertPhanCong()`, `updateCaPhanCong()`, `updateGhiChu()`, `deletePhanCong()`. |
| 12 | `PhongLuuTruDAO` | `PhongLuuTru` (Bảng dữ liệu Pet Hotel) | `getAllPhong()`, `updatePhong()`. |

---

## 3.2. Thiết kế Cơ sở dữ liệu Quan hệ

### 3.2.1. Mô hình Thực thể Kết hợp Mở rộng (EER Diagram)
Mô hình EER phản ánh mối quan hệ toàn diện giữa các thực thể, bao gồm quan hệ kế thừa chuyên biệt hóa (Specialization/Generalization) giữa `ChiTietHoaDon` với `ChiTietXuatLo` và `ChiTietDichVu`.

```
[Chèn sơ đồ EER vào]


```

### 3.2.2. Mô hình CSDL Quan hệ (Relational Database Schema)
Tập lệnh DDL chuẩn hóa tại `sql/HeThongQuanLyCuaHangThuCung.sql` khởi tạo **18 bảng quan hệ**:

```
[Chèn sơ đồ Diagram hệ quản trị CSDL SQL Server vào]


```

### 3.2.3. Đặc tả chi tiết 18 bảng Cơ sở dữ liệu

**Bảng 3.3: Bảng `TaiKhoan` (Tài khoản hệ thống)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `tenDangNhap` | NVARCHAR(50) | PK | NOT NULL | Tên đăng nhập tài khoản |
| `matKhau` | NVARCHAR(255) | | NOT NULL | Mật khẩu tài khoản |
| `vaiTro` | NVARCHAR(50) | | NOT NULL | Vai trò: Admin, NVQL, NVTN, NVCS, NVBH |
| `trangThaiHoatDong` | BIT | | NOT NULL, DEFAULT 1 | 1: Hoạt động, 0: Khóa |

**Bảng 3.4: Bảng `NhanVien` (Hồ sơ nhân viên)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maNhanVien` | NVARCHAR(20) | PK | NOT NULL | Mã định danh nhân viên |
| `hoTen` | NVARCHAR(100) | | NOT NULL | Họ và tên nhân viên |
| `chucVu` | NVARCHAR(50) | | NOT NULL | Chức vụ |
| `soDienThoai` | NVARCHAR(15) | | NULL | Số điện thoại liên lạc |
| `tenDangNhap` | NVARCHAR(50) | FK | UNIQUE, REFERENCES TaiKhoan | Tài khoản liên kết 1-1 |

**Bảng 3.5: Bảng `CaLamViec` (Ca làm việc)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maCa` | NVARCHAR(20) | PK | NOT NULL | Mã ca (CA01, CA02, CA03, CA04) |
| `tenCa` | NVARCHAR(50) | | NOT NULL | Tên ca trực kèm khung giờ |
| `gioBatDau` | TIME | | NOT NULL | Giờ mở ca |
| `gioKetThuc` | TIME | | NOT NULL | Giờ chốt ca |

**Bảng 3.6: Bảng `BangPhanCong` (Lịch phân công nhân sự)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maPhanCong` | NVARCHAR(20) | PK | NOT NULL | Mã phân công |
| `maNhanVien` | NVARCHAR(20) | FK | NOT NULL, REFERENCES NhanVien | Nhân viên trực |
| `maCa` | NVARCHAR(20) | FK | NOT NULL, REFERENCES CaLamViec | Ca trực chỉ định |
| `ngayLamViec` | DATE | | NOT NULL | Ngày làm việc |
| `trangThaiCa` | NVARCHAR(50) | | DEFAULT N'Chưa bắt đầu' | Trạng thái ca |
| `ghiChu` | NVARCHAR(255) | | NULL | Vị trí phân công chi tiết |

**Bảng 3.7: Bảng `BangGiaoCa` (Biên bản bàn giao ca trực)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maGiaoCa` | NVARCHAR(20) | PK | NOT NULL | Mã biên bản giao ca (GCxxx) |
| `maNhanVien` | NVARCHAR(20) | FK | NOT NULL, REFERENCES NhanVien | Thu ngân bàn giao |
| `maCa` | NVARCHAR(20) | FK | NOT NULL, REFERENCES CaLamViec | Ca làm việc |
| `tienDauCa` | DECIMAL(18,2) | | NOT NULL, DEFAULT 0 | Tiền lẻ đầu ca trong két |
| `tienCuoiCaHeThong` | DECIMAL(18,2) | | NOT NULL, DEFAULT 0 | Tiền mặt lý thuyết hệ thống |
| `tienCuoiCaThucTe` | DECIMAL(18,2) | | NOT NULL, DEFAULT 0 | Tiền mặt thực kiểm đếm |
| `tienChenhLech` | DECIMAL(18,2) | | NOT NULL, DEFAULT 0 | Tiền thừa (+) hoặc thiếu (-) |
| `lyDoChenhLech` | NVARCHAR(255) | | NULL | Giải trình lệch két & Trạng thái duyệt ca |
| `gioKetThucThucTe` | DATETIME2 | | NULL | Thời điểm hoàn tất chốt ca |

**Bảng 3.8: Bảng `KhachHang` (Khách hàng thành viên)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maKhachHang` | NVARCHAR(20) | PK | NOT NULL | Mã khách hàng (KHxxx) |
| `hoTen` | NVARCHAR(100) | | NOT NULL | Họ tên khách hàng |
| `soDienThoai` | NVARCHAR(15) | | NOT NULL, UNIQUE | Số điện thoại duy nhất |
| `diemTichLuy` | INT | | NOT NULL, DEFAULT 0 | Điểm thưởng thành viên |

**Bảng 3.9: Bảng `ThuCung` (Hồ sơ thú cưng)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maThuCung` | NVARCHAR(20) | PK | NOT NULL | Mã thú cưng (TCxxx) |
| `tenThuCung` | NVARCHAR(100) | | NOT NULL | Tên bé thú cưng |
| `giongLoai` | NVARCHAR(50) | | NULL | Chủng loại bé |
| `ngaySinh` | DATE | | NULL | Ngày sinh ước tính |
| `gioiTinh` | NVARCHAR(10) | | NULL | Giới tính (Đực / Cái) |

**Bảng 3.10: Bảng `HoSoThuCung` (Liên kết chủ - thú cưng & Vi mạch)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maHoSo` | NVARCHAR(20) | PK | NOT NULL | Mã hồ sơ nuôi |
| `maKhachHang` | NVARCHAR(20) | FK | NOT NULL, REFERENCES KhachHang | Chủ sở hữu |
| `maThuCung` | NVARCHAR(20) | FK | NOT NULL, REFERENCES ThuCung | Thú cưng |
| `soMicrochip` | NVARCHAR(50) | | NULL | Mã vi mạch định danh |
| `ngayDangKy` | DATE | | NOT NULL | Ngày đăng ký hồ sơ |
| `trangThaiNuoi` | NVARCHAR(50) | | DEFAULT N'Đang nuôi' | Tình trạng nuôi dưỡng |

**Bảng 3.11: Bảng `HoSoSucKhoe` (Kiểm tra thể trạng y tế)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maSucKhoe` | NVARCHAR(20) | PK | NOT NULL | Mã phiếu khám |
| `maThuCung` | NVARCHAR(20) | FK | NOT NULL, REFERENCES ThuCung | Thú cưng được khám |
| `maNhanVienKiemTra` | NVARCHAR(20) | FK | NOT NULL, REFERENCES NhanVien | KTV thực hiện |
| `canNang` | DECIMAL(6,2) | | NULL | Cân nặng (kg) |
| `nhietDo` | DECIMAL(4,2) | | NULL | Nhiệt độ cơ thể (°C) |
| `tinhTrangVeRan` | NVARCHAR(100) | | NULL | Tình trạng da lông, ve rận |
| `tiemPhongDai` | BIT | | NOT NULL, DEFAULT 0 | Đã tiêm ngừa dại (1/0) |
| `ghiChuTheTrang` | NVARCHAR(255) | | NULL | Lưu ý thể trạng |
| `ngayKiemTra` | DATETIME2 | | NOT NULL | Thời gian kiểm tra |

**Bảng 3.12: Bảng `DichVu` (Dịch vụ Spa/Grooming)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maDichVu` | NVARCHAR(20) | PK | NOT NULL | Mã dịch vụ (DVxxx) |
| `tenDichVu` | NVARCHAR(100) | | NOT NULL | Tên dịch vụ |
| `giaDichVu` | DECIMAL(18,2) | | NOT NULL | Đơn giá niêm yết |
| `thoiLuongPhut` | INT | | NOT NULL | Thời lượng thực hiện (phút) |

**Bảng 3.13: Bảng `LichHen` (Lịch hẹn dịch vụ)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maLichHen` | NVARCHAR(20) | PK | NOT NULL | Mã lịch hẹn (LHxxx) |
| `maKhachHang` | NVARCHAR(20) | FK | NOT NULL, REFERENCES KhachHang | Khách hàng đặt lịch |
| `maNhanVienTiepNhan` | NVARCHAR(20) | FK | NULL, REFERENCES NhanVien | Nhân viên tiếp nhận |
| `ngayHen` | DATETIME2 | | NOT NULL | Thời điểm hẹn |
| `trangThaiLichHen` | NVARCHAR(50) | | DEFAULT N'Đã đặt' | Đã đặt, Đang phục vụ, Hoàn thành, Hủy |
| `ghiChu` | NVARCHAR(255) | | NULL | Yêu cầu của khách |

**Bảng 3.14: Bảng `ChiTietLichHen` (Chi tiết lịch hẹn)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maChiTietLichHen` | NVARCHAR(20) | PK | NOT NULL | Mã dòng chi tiết |
| `maLichHen` | NVARCHAR(20) | FK | NOT NULL, REFERENCES LichHen | Lịch hẹn cha |
| `maThuCung` | NVARCHAR(20) | FK | NOT NULL, REFERENCES ThuCung | Thú cưng phục vụ |
| `maDichVu` | NVARCHAR(20) | FK | NOT NULL, REFERENCES DichVu | Dịch vụ đăng ký |
| `maKtvChiDinh` | NVARCHAR(20) | FK | NULL, REFERENCES NhanVien | KTV phụ trách |

**Bảng 3.15: Bảng `DanhMucHang` (Danh mục hàng hóa)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maDanhMuc` | NVARCHAR(20) | PK | NOT NULL | Mã danh mục (DMxx) |
| `tenDanhMuc` | NVARCHAR(100) | | NOT NULL | Tên nhóm hàng |
| `moTa` | NVARCHAR(255) | | NULL | Mô tả nhóm |

**Bảng 3.16: Bảng `SanPham` (Sản phẩm hàng hóa)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maSanPham` | NVARCHAR(20) | PK | NOT NULL | Mã sản phẩm (SPxxx) |
| `maDanhMuc` | NVARCHAR(20) | FK | NOT NULL, REFERENCES DanhMucHang | Nhóm danh mục |
| `tenSanPham` | NVARCHAR(150) | | NOT NULL | Tên sản phẩm |
| `moTa` | NVARCHAR(255) | | NULL | Mô tả chi tiết |

**Bảng 3.17: Bảng `DonViTinh` (Đơn vị tính đo lường)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maDonVi` | NVARCHAR(20) | PK | NOT NULL | Mã đơn vị tính (DVTxx) |
| `tenDonVi` | NVARCHAR(50) | | NOT NULL | Tên đơn vị (Gói, Lon, Chai...) |

**Bảng 3.18: Bảng `QuyDoiDonVi` (Quy đổi quy cách & Bảng giá)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maQuyDoi` | NVARCHAR(20) | PK | NOT NULL | Mã quy đổi (QDxxx) |
| `maSanPham` | NVARCHAR(20) | FK | NOT NULL, REFERENCES SanPham | Sản phẩm quy đổi |
| `maDonVi` | NVARCHAR(20) | FK | NOT NULL, REFERENCES DonViTinh | Đơn vị tính |
| `tyLeQuyDoi` | INT | | NOT NULL, DEFAULT 1 | Tỷ lệ quy về đơn vị cơ sở |
| `laDonViCoSo` | BIT | | NOT NULL, DEFAULT 0 | 1: Đơn vị cơ sở gốc |
| `giaBan` | DECIMAL(18,2) | | NOT NULL | Giá bán lẻ của quy cách này |

**Bảng 3.19: Bảng `NhaCungCap` (Nhà cung cấp)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `maNhaCungCap` | NVARCHAR(20) | PK | NOT NULL | Mã nhà cung cấp (NCCxx) |
| `tenNhaCungCap` | NVARCHAR(150) | | NOT NULL | Tên doanh nghiệp cung cấp |
| `diaChi` | NVARCHAR(255) | | NULL | Địa chỉ trụ sở |
| `soDienThoai` | NVARCHAR(15) | | NULL | Số điện thoại liên hệ |

**Bảng 3.20: Bảng `LoHang` (Lô hàng tồn kho & Hạn sử dụng FEFO)**
| Tên cột | Kiểu dữ liệu | Khóa | Ràng buộc | Diễn giải |
|---|---|:---:|---|---|
| `soLo` | NVARCHAR(50) | PK | NOT NULL | Số lô sản xuất (duy nhất) |
| `maSanPham` | NVARCHAR(20) | FK | NOT NULL, REFERENCES SanPham | Mặt hàng trong lô |
| `maNhaCungCap` | NVARCHAR(20) | FK | NOT NULL, REFERENCES NhaCungCap | Đơn vị cung ứng |
| `ngaySanXuat` | DATE | | NULL | Ngày sản xuất |
| `hanSuDung` | DATE | | NOT NULL | Hạn sử dụng (tiêu chí FEFO) |
| `donGiaNhapCoSo`| DECIMAL(18,2) | | NOT NULL | Giá vốn đơn vị cơ sở |
| `soLuongNhap` | INT | | NOT NULL | Số lượng nhập ban đầu |
| `soLuongTon` | INT | | NOT NULL, CHECK (>= 0) | Số lượng tồn kho hiện tại |

*(Ghi chú: Bảng `HoaDon`, `ChiTietHoaDon`, `ChiTietXuatLo`, `ChiTietDichVu`, `PhieuDoiTra`, `ChiTietDoiTra` được cấu trúc khóa ngoại chặt chẽ như phân tích tại Mục 3.1).*

---

## 3.3. Phân luồng Màn hình và Cơ chế Điều hướng (Screen Flow)

### 3.3.1. Sơ đồ phân luồng tổng thể

```
[Chèn sơ đồ Screen Flow vào]


```

### 3.3.2. Kiến trúc Điều hướng Kép (Dual Navigation)
- **Thanh thực đơn đỉnh (JMenuBar 6 nhóm IUH):** Chuẩn hóa theo Slide 17 môn học với 6 thực đơn chính: *1. Hệ thống*, *2. Danh mục*, *3. Cập nhật*, *4. Xử lý*, *5. Tìm kiếm*, *6. Báo biểu*. Hỗ trợ các phím tắt `Alt+1` đến `Alt+6` để người dùng mở menu bằng bàn phím.
- **Thanh điều hướng ngang (Pill Tabs):** Dãy nút dạng viên thuốc bo góc tròn 20px đặt ngay dưới tiêu đề ứng dụng, tự động lọc danh sách phân hệ được phép truy cập dựa trên vai trò (`NVQL`, `NVTN`, `NVBH`, `NVCS`).
- **Giải phóng 100% Sidebar trái:** Không sử dụng cột menu dọc truyền thống, dành toàn bộ chiều ngang màn hình cho việc hiển thị lưới thẻ Catalog sản phẩm và bảng giỏ hàng POS, tối ưu tầm nhìn cho thu ngân.

### 3.3.3. Cơ chế Nạp màn hình theo yêu cầu (Lazy Loading On-Demand)
Trong `MainDashboard.java`, các panel con chỉ được khởi tạo instance (`new Panel()`) khi người dùng bấm vào tab tương ứng lần đầu tiên thông qua phương thức `getOrInitPanel(String cardName)`. Cơ chế này loại bỏ hoàn toàn hiện tượng nghẽn Event Dispatch Thread (EDT) khi khởi động, giúp ứng dụng mở lên chỉ trong **< 1.0 giây**.

### 3.3.4. Bảng ánh xạ hệ thống phím tắt toàn cục (Global Hotkeys)

**Bảng 3.22: Bảng ánh xạ hệ thống phím tắt toàn cục**

| Phím tắt | Chức năng nghiệp vụ tương ứng | Phân hệ giao diện |
|:---:|---|---|
| `Alt + 1` đến `Alt + 6` | Kích hoạt 6 nhóm menu chính trên thanh JMenuBar | Toàn hệ thống |
| `F1` | Chuyển nhanh sang màn hình Bán hàng POS | `BanHangPOSPanel` |
| `F2` | Chuyển nhanh sang màn hình Phân ca tuần (Timetable) | `LichPhanCaPanel` |
| `F3` | Chuyển nhanh sang màn hình Lịch hẹn & Dịch vụ Spa | `QuanLyLichHenPanel` |
| `F4` | Chuyển nhanh sang màn hình Đổi trả hàng hóa | `DoiTraHangPanel` |
| `F5` | Làm mới dữ liệu Dashboard tổng quan | `TongQuanPanel` |
| `F6` | Chuyển nhanh sang màn hình Khách sạn Thú cưng | `PetHotelPanel` |
| `F9` | Xác nhận thanh toán hóa đơn POS / Xác nhận VietQR | `BanHangPOSPanel` |
| `Ctrl + G` | Mở nhanh màn hình Bàn giao ca & Đối soát két tiền | `BanGiaoCaPanel` |
| `Ctrl + M` | Mở hộp thoại Đổi mật khẩu tài khoản cá nhân | `LoginForm` / `MainDashboard` |
| `Ctrl + Q` | Đăng xuất tài khoản người dùng hiện tại | Toàn hệ thống |
| `Alt + F4` | Thoát hoàn toàn khỏi chương trình ứng dụng | Toàn hệ thống |

---

## 3.4. Chi tiết Màn hình Ứng dụng & Ba Luồng Nghiệp Vụ Cốt Lõi

### 3.4.1. Khảo sát danh mục 22 lớp Giao diện người dùng Swing (UI Inventory)

**Bảng 3.21: Danh mục 22 lớp Giao diện người dùng Swing**

| STT | Tên Lớp Giao Diện | Thể Loại | Mục Đích & Phạm Vi Chức Năng |
|:---:|:---|:---:|---|
| 1 | `LoginForm` | `JFrame` | Cửa sổ Đăng nhập hệ thống, kiểm tra xác thực tài khoản CSDL, hỗ trợ tài khoản demo và phím Enter. |
| 2 | `MainDashboard` | `JFrame` | Khung giao diện chính (Dual Navigation, CardLayout nạp lazy 10 phân hệ, header người dùng, status bar). |
| 3 | `BanHangPOSPanel` | `JPanel` | Màn hình bán hàng POS tại quầy: Catalog lưới thẻ, giỏ hàng, tính tiền, hóa đơn chờ, lịch sử HĐ. |
| 4 | `DoiTraHangPanel` | `JPanel` | Màn hình tiếp nhận đổi trả hàng hóa: tra cứu HĐ cũ trong 7 ngày, kịch bản A (hoàn tiền), kịch bản B (đổi hàng). |
| 5 | `BanGiaoCaPanel` | `JPanel` | Màn hình đối soát két tiền mặt, gửi chốt ca chờ duyệt và duyệt bàn giao ca làm việc cho Quản lý. |
| 6 | `TongQuanPanel` | `JPanel` | Dashboard giám sát KPI điều hành (Doanh thu, Hóa đơn, Thú cưng, Cảnh báo FEFO), tích hợp 2 biểu đồ Graphics2D. |
| 7 | `KhoLoHangPanel` | `JPanel` | Quản lý Danh mục hàng hóa, đơn vị tính quy đổi, quản lý hạn dùng lô hàng FEFO và điểm đặt hàng ROP. |
| 8 | `QuanLyLichHenPanel` | `JPanel` | Quản lý tiếp nhận đặt lịch hẹn chăm sóc, cập nhật tiến độ Spa 4 bước và chuyển giao sang POS thanh toán. |
| 9 | `PetHotelPanel` | `JPanel` | Quản lý Khách sạn Thú cưng: Ma trận phòng/chuồng (Lưới thẻ), nhật ký dinh dưỡng, check-in, check-out sang POS. |
| 10 | `ThuCungDichVuPanel` | `JPanel` | Quản lý hồ sơ thú cưng, gắn mã Microchip, theo dõi thể trạng y tế, CRM khách hàng VIP và thẻ hội viên Spa Pass. |
| 11 | `LichPhanCaPanel` | `JPanel` | Bảng thời khóa biểu phân ca tuần (4 ca x 7 ngày), thẻ phân ca bo tròn theo vai trò, menu chuột phải điều phối ca. |
| 12 | `QuanLyNhanVienPanel` | `JPanel` | Quản lý nhân sự và phân quyền: Lưới thẻ nhân viên, cấp tài khoản, đổi vai trò, khóa/mở khóa tài khoản. |
| 13 | `VietQRDialog` | `JDialog` | Hộp thoại thanh toán chuyển khoản VietQR động chuẩn NAPAS 24/7 gắn cứng số tiền và mã HĐ, hỗ trợ offline fallback. |
| 14 | `VNPayDialog` | `JDialog` | Hộp thoại tích hợp cổng thanh toán VNPay Sandbox v2.1.0 với Local Loopback Server tự động nhận kết quả callback. |
| 15 | `HoaDonPreviewDialog` | `JDialog` | Biểu mẫu in hóa đơn nhiệt K80 và khổ A5 xem trước, hỗ trợ xuất văn bản, hiển thị chiết khấu và điểm tích lũy VIP. |
| 16 | `BaoCaoThongKeDialog` | `JDialog` | Trung tâm báo biểu quản trị 7 phân hệ chuyên sâu (Doanh thu, Top bán chạy, KTV, VIP, Pet Hotel, FEFO, XNT). |
| 17 | `ChonDichVuDialog` | `JDialog` | Hộp thoại tiếp nhận dịch vụ Spa/Grooming: chọn size thú cưng, tự động nhảy đơn giá, gán KTV phụ trách, chọn Add-on. |
| 18 | `DatLichHenDialog` | `JDialog` | Hộp thoại đặt lịch hẹn 2 cột: chọn Time Slot trực quan (Sáng/Chiều), phân công KTV, nhập thông tin thể trạng bé. |
| 19 | `NhapSanPhamVaLoHangDialog`| `JDialog` | Hộp thoại thêm sản phẩm & nhập lô hàng mới: đánh giá hạn dùng FEFO tự động theo 3 cấp độ màu (Xanh/Vàng/Đỏ). |
| 20 | `ThietLapQuyDoiDialog` | `JDialog` | Hộp thoại thiết lập bảng quy đổi đơn vị tính đóng gói (Lon -> Khay/Thùng, Gói -> Thùng) kèm bảng giá riêng. |
| 21 | `BarChartPanel` | `JPanel` | Biểu đồ cột Graphics2D thuần hiển thị doanh thu 7 ngày gần nhất, highlight cột doanh thu cao nhất, hover tooltip. |
| 22 | `DonutChartPanel` | `JPanel` | Biểu đồ tròn Donut Graphics2D thuần hiển thị cơ cấu doanh thu 3 nhóm ngành và tiến độ hoàn thành dịch vụ trong ngày. |

```
[Chèn hình Giao diện Đăng nhập hệ thống (LoginForm) vào]


```

```
[Chèn hình Giao diện Khung điều khiển chính (MainDashboard) vào]


```

### 3.4.2. Luồng nghiệp vụ cốt lõi 1: Bán hàng POS (`BanHangPOSPanel`)
1. **Khóa cứng đúng 2 phương thức thanh toán:**
   Trong `BanHangPOSPanel.java` (dòng 1439):
   ```java
   cboPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
   ```
   Hệ thống loại bỏ hoàn toàn các phương thức không cần thiết, tập trung vào 2 phương thức phổ biến nhất.
2. **Dãy nút chọn mệnh giá nhanh & Tự động đảo phương thức:**
   - 4 nút mệnh giá: "Đủ tiền", "100k", "200k", "500k".
   - *Tính năng đảo chiều thông minh:* Khi thu ngân đang chọn "Chuyển khoản" mà khách bất ngờ đưa tiền mặt, việc bấm bất kỳ nút mệnh giá nào sẽ tự động chuyển `cboPhuongThuc` về lại `"Tiền mặt"`, mở khóa trường nhập liệu và tính toán tiền thối ngay lập tức.
3. **Tính tiền thối lại thời gian thực (Real-time Change Calculation):**
   - Trường `txtTienKhachDua` được gắn `DocumentListener`. Mỗi khi nhập tiền, nhãn `lblTienThoiLai` tự động cập nhật:
     * Đủ tiền: Hiển thị số tiền thối màu xanh lá (`#10B981`).
     * Thiếu tiền: Đổi màu đỏ cảnh báo kèm câu chữ: `"Chưa đủ (thiếu X,XXX đ)"`.
4. **Kiểm soát chặn thanh toán khi thiếu tiền mặt (Underpayment Blocking):**
   - Tại dòng 2451-2468: Nếu chọn "Tiền mặt" mà `tienKhachDuaVal < canTra`, hệ thống **chặn hoàn toàn thanh toán**, hiển thị thông báo popup cảnh báo số tiền còn thiếu, không lưu hóa đơn vào CSDL và bôi đen ô tiền để thu ngân nhập lại.
5. **Chuyển khoản VietQR động (`VietQRDialog`):**
   - Khi chọn "Chuyển khoản", ô tiền khách đưa tự động gán bằng số tiền cần thanh toán và bị khóa mờ. Khi bấm F9, cửa sổ `VietQRDialog` xuất hiện hiển thị mã QR động chuẩn NAPAS 24/7 gắn sẵn số tài khoản MB Bank, số tiền và nội dung chuyển khoản.

```
[Chèn hình Giao diện Bán hàng POS vào]


```

```
[Chèn hình Hộp thoại Thanh toán VietQR vào]


```

```
[Chèn hình Biểu mẫu In Hóa đơn K80 vào]


```

### 3.4.3. Luồng nghiệp vụ cốt lõi 2: Đổi trả hàng hóa chuyển tiếp sang POS (`DoiTraHangPanel`)
1. **Khảo sát & Chọn lựa kịch bản:**
   - Thu ngân tra cứu hóa đơn trong 07 ngày, tích chọn sản phẩm trả, số lượng và tình trạng tem mác (nguyên tem thì tự động hoàn kho qua `KhoLoHangPanel.congTonKho()`).
   - Hai kịch bản: Kịch bản A (Hoàn tiền tại chỗ) hoặc Kịch bản B (Đổi sang sản phẩm khác).
2. **Đóng gói DTO `DoiTraTransferData` & Chuyển giao sang POS:**
   - Khi chọn Kịch bản B, nút bấm chuyển thành `"CHUYỂN SANG BÁN HÀNG POS ĐỂ THANH TOÁN (F9)"` màu xanh lá.
   - Hệ thống đóng gói dữ liệu vào `DoiTraTransferData` và điều hướng sang `BanHangPOSPanel` qua `MainDashboard.dieuHuongSangChucNang(CARD_POS)`.
3. **Tiếp nhận và thanh toán phần bù chênh lệch tại POS:**
   - Phương thức `pos.tiepNhanDoiTraHang(data)` tự động điền khách hàng, nạp sản phẩm đổi vào giỏ và hiển thị thanh Banner màu vàng cam nổi bật:
     `"🔄 ĐỔI TRẢ TỪ HÓA ĐƠN [HDXXXXXX] | KHẤU TRỪ: -XXX,XXX đ"` kèm nút `"Hủy đổi trả"`.
   - Thu ngân có thể chọn thêm bất kỳ sản phẩm nào từ catalog nếu khách mua thêm hàng.
   - Tính toán chênh lệch:
     * Nếu giỏ hàng mới > giá trị hàng trả: `canTra = (Tổng tiền hàng mới) - (Khấu trừ hàng trả)`. Thu ngân thu thêm phần tiền chênh lệch này.
     * Nếu giỏ hàng mới < giá trị hàng trả: `canTra = 0`, nhãn tiền thối chuyển thành `"Hoàn thừa: XXX,XXX đ"`. Cửa hàng hoàn lại số tiền chênh lệch thừa cho khách.
   - Khi thanh toán thành công, CSDL lưu đồng thời hóa đơn bán mới và bản ghi `PhieuDoiTra` tương ứng.

```
[Chèn hình Giao diện Đổi trả hàng hóa vào]


```

### 3.4.4. Luồng nghiệp vụ cốt lõi 3: Bàn giao ca đối soát két tiền chờ quản lý duyệt bất đồng bộ (`BanGiaoCaPanel`)
1. **Thu ngân chốt ca & Bàn giao quỹ tiền mặt:**
   - Hệ thống tự động tính: Tiền lẻ đầu ca + Doanh thu tiền mặt bán hàng - Chi hoàn tiền trả hàng = Tiền mặt lý thuyết trong két.
   - Thu ngân nhập tiền thực tế đếm được. Hệ thống tính độ lệch. Nếu két bị lệch tiền, thu ngân bắt buộc nhập lý do giải trình.
   - **Loại bỏ hoàn toàn popup đăng nhập quản lý tại quầy.** Bấm "Chốt ca & Bàn giao", hệ thống sinh mã `GCxxx`, lưu bản ghi vào bảng `BangGiaoCa` với trạng thái `"⏳ Chờ Quản lý duyệt | [Lý do]"`.
   - Thu ngân đăng xuất an toàn ra màn hình `LoginForm`.
2. **Quản lý đăng nhập và xét duyệt đối soát ca:**
   - Quản lý đăng nhập vào hệ thống, mở `BanGiaoCaPanel`, nút `"✓ QUẢN LÝ DUYỆT & XÁC NHẬN KÉT CA ĐANG CHỌN"` hiển thị màu xanh lá.
   - Cửa sổ đối soát mở ra hiển thị danh sách các ca chờ duyệt lấy từ `dao.getDanhSachCaChoDuyet()`.
   - Quản lý kiểm tra chi tiết các số liệu kế toán và bấm **"✓ PHÊ DUYỆT ĐỐI SOÁT & KÝ NHẬN KÉT (IN K80)"**.
   - Cập nhật trạng thái bản ghi thành: `"✓ Đã duyệt (QL: [Họ tên QL]) | [Ghi chú]"`.
   - Tự động xuất biên bản bàn giao ca nhiệt K80 đầy đủ các chỉ số tài chính và chữ ký 2 bên.

```
[Chèn hình Giao diện Bàn giao ca & Đối soát két tiền vào]


```

```
[Chèn hình Hộp thoại Quản lý xét duyệt ca K80 vào]


```

```
[Chèn hình Giao diện Quản lý Kho FEFO vào]


```

```
[Chèn hình Giao diện Quản lý Lịch hẹn Spa vào]


```

```
[Chèn hình Giao diện Khách sạn Thú cưng Pet Hotel vào]


```

```
[Chèn hình Giao diện Quản lý Hồ sơ thú cưng & VIP vào]


```

```
[Chèn hình Giao diện Ma trận Phân ca tuần vào]


```

```
[Chèn hình Giao diện Quản lý Nhân sự vào]


```

```
[Chèn hình Giao diện Trung tâm Báo biểu Quản trị vào]


```

---

## 3.5. Kiểm thử Chức năng Ứng dụng (Test Cases)

### 3.5.1. Tài nguyên kiểm thử
1. **Phần cứng:**
   - CPU: Intel Core i5 / i7 thế hệ 10 trở lên hoặc AMD Ryzen 5 / 7.
   - RAM: Tối thiểu 8 GB (khuyến nghị 16 GB).
   - Ổ cứng: Trống tối thiểu 10 GB SSD.
2. **Phần mềm:**
   - Hệ điều hành: Microsoft Windows 10 / Windows 11 (64-bit).
   - Môi trường thực thi: Java Development Kit (JDK 17 LTS).
   - Hệ quản trị CSDL: Microsoft SQL Server 2019/2022 Developer Edition.
   - Khung kiểm thử tự động: `TestFramework.java` (Custom Test Harness viết bằng Java 17 thuần, không phụ thuộc thư viện ngoài).

### 3.5.2. Danh mục 45 Kịch bản Kiểm thử Thực tế (Test Cases Catalog)

**Bảng 3.23: Danh mục 45 kịch bản kiểm thử chức năng thực tế**

| Mã Test Case | Tên Kịch Bản Kiểm Thử | Điều Kiện & Dữ Liệu Đầu Vào | Hành Vi Mã Nguồn & Kết Quả Mong Đợi | Kết Quả Thực Tế |
|:---|:---|:---|:---|:---:|
| **TC-POS-01** | Chặn thanh toán khi thiếu tiền mặt | Đơn hàng 250.000 đ, thu ngân nhập tiền khách đưa 200.000 đ. | Bấm F9 -> Chặn thanh toán, hiện popup thông báo thiếu 50.000 đ, focus lại ô tiền. | **PASS** |
| **TC-POS-02** | Thanh toán tiền mặt đúng số tiền | Đơn hàng 250.000 đ, nhập tiền khách đưa 250.000 đ. | Tiền thối = 0 đ (nhãn màu xanh SUCCESS), thanh toán thành công, trừ kho FEFO. | **PASS** |
| **TC-POS-03** | Tính tiền thối khi khách đưa thừa | Đơn hàng 250.000 đ, khách đưa tờ 500.000 đ. | Nhãn thối lại hiển thị thời gian thực: `250.000 đ` (màu xanh lá), lưu hóa đơn hợp lệ. | **PASS** |
| **TC-POS-04** | Đơn hàng giảm giá 100% (0 đ) | Đơn hàng có voucher 0 đ, tiền khách đưa 0 đ. | Hệ thống cho phép thanh toán thành công với số tiền 0 đ. | **PASS** |
| **TC-POS-05** | Bấm nút mệnh giá nhanh "100k" | Đơn hàng 300.000 đ, bấm nút `100k`. | Ô `txtTienKhachDua` điền `100000`, nhãn thối lại hiển thị `"Chưa đủ (thiếu 200.000 đ)"`. | **PASS** |
| **TC-POS-06** | Bấm nút mệnh giá nhanh "200k" | Đơn hàng 300.000 đ, bấm nút `200k`. | Ô `txtTienKhachDua` điền `200000`, nhãn thối lại hiển thị `"Chưa đủ (thiếu 100.000 đ)"`. | **PASS** |
| **TC-POS-07** | Bấm nút mệnh giá nhanh "500k" | Đơn hàng 300.000 đ, bấm nút `500k`. | Ô `txtTienKhachDua` điền `500000`, nhãn thối lại hiển thị `200.000 đ` màu xanh lá. | **PASS** |
| **TC-POS-08** | Bấm nút mệnh giá nhanh "Đủ tiền" | Đơn hàng 180.000 đ, bấm nút `Đủ tiền`. | Ô `txtTienKhachDua` tự động điền `180000`, nhãn thối lại hiển thị `0 đ`. | **PASS** |
| **TC-POS-09** | Tự động chuyển về Tiền mặt khi bấm nút nhanh | Đang chọn "Chuyển khoản", bấm nút `500k`. | Dropdown tự động nhảy về `"Tiền mặt"`, mở khóa ô nhập tiền, tính tiền thối. | **PASS** |
| **TC-POS-10** | Dropdown phương thức chỉ có đúng 2 mục | Khảo sát `cboPhuongThuc` trên giao diện POS. | Số lượng item = 2, gồm đúng `"Tiền mặt"` và `"Chuyển khoản"`. | **PASS** |
| **TC-POS-11** | Chuyển khoản VietQR tự động khóa ô tiền | Chọn phương thức `"Chuyển khoản"`. | Ô `txtTienKhachDua` tự động điền bằng `canTra` và bị disable (`setEnabled(false)`). | **PASS** |
| **TC-POS-12** | Khởi tạo hộp thoại VietQR Napas 24/7 | Bấm F9 khi chọn Chuyển khoản đơn 350.000 đ. | Hộp thoại `VietQRDialog` mở ra, hiển thị QR MB Bank gắn cứng số tiền 350.000 đ. | **PASS** |
| **TC-POS-13** | Cơ chế Offline Fallback QR | Giả lập mất kết nối Internet khi tạo VietQR. | Hàm `taoMaVietQROfflineFallback()` vẽ ảnh QR nội bộ 300x320px không bị crash. | **PASS** |
| **TC-POS-14** | Nhập định dạng có dấu phẩy/chấm | Nhập vào ô tiền: `"250.000"` hoặc `"250,000"`. | Hệ thống lọc bỏ ký tự phân cách, trích xuất chính xác số `250000` để tính tiền. | **PASS** |
| **TC-POS-15** | Nhập chuỗi chữ cái hoặc ký tự lạ | Nhập vào ô tiền: `"abcxyz"`, `"!@#$"`. | Hệ thống xử lý an toàn, coi như số 0, hiển thị thiếu tiền mà không văng Exception. | **PASS** |
| **TC-POS-16** | Đơn hàng số tiền cực lớn (1 tỷ VNĐ) | Đơn hàng 500 triệu, khách đưa 1 tỷ VNĐ. | Hệ thống sử dụng kiểu `long`, tính toán chính xác tiền thối 500 triệu VNĐ. | **PASS** |
| **TC-POS-17** | Stress Test chuyển phương thức 100 lần | Chuyển luân phiên Tiền mặt <-> Chuyển khoản 100 lần. | Không xảy ra hiện tượng treo EDT, các ràng buộc enable/disable duy trì đúng 100%. | **PASS** |
| **TC-EX-01** | Tra cứu hóa đơn cũ trong DoiTraHangPanel | Nhập mã hóa đơn `HD_TEST_001`. | Hiển thị đúng thông tin khách, ngày mua, danh sách sản phẩm và tổng tiền. | **PASS** |
| **TC-EX-02** | Chọn kịch bản A: Trả hàng hoàn tiền | Tích chọn 1 sản phẩm trả trị giá 150.000 đ. | Nút hiển thị "XÁC NHẬN & IN PHIẾU HOÀN TIỀN (80MM)", tính khấu trừ điểm VIP. | **PASS** |
| **TC-EX-03** | Chọn kịch bản B: Đổi sang món khác | Tích chọn Kịch bản B, chọn sản phẩm mới. | Nút chuyển thành "CHUYỂN SANG BÁN HÀNG POS ĐỂ THANH TOÁN (F9)" màu xanh lá. | **PASS** |
| **TC-EX-04** | Đóng gói DTO DoiTraTransferData | Bấm chuyển sang POS ở Kịch bản B. | DTO chứa đủ mã HĐ gốc, SĐT, Tên khách, tổng tiền trả, danh sách món cũ & mới. | **PASS** |
| **TC-EX-05** | Tiếp nhận đổi trả trên màn hình POS | Gọi `pos.tiepNhanDoiTraHang(data)`. | Tự động điền khách hàng, nạp món mới vào giỏ, hiển thị Banner khấu trừ màu cam. | **PASS** |
| **TC-EX-06** | Tính tiền chênh lệch khi Món mới > Món cũ | Món mới 450.000 đ, khấu trừ hàng cũ 300.000 đ. | Nhãn cần trả hiển thị `canTra = 150.000 đ`. Khách trả thêm 150.000 đ. | **PASS** |
| **TC-EX-07** | Tính tiền chênh lệch khi Món mới < Món cũ | Món mới 220.000 đ, khấu trừ hàng cũ 300.000 đ. | Nhãn cần trả `canTra = 0 đ`, nhãn thối lại hiển thị `"Hoàn thừa: 80.000 đ"`. | **PASS** |
| **TC-EX-08** | Khách mua thêm 5 sản phẩm tại POS | Nhấp chọn thêm 5 sản phẩm từ Catalog lưới thẻ. | Giỏ hàng tăng lên 6 món, tổng tiền hàng và số tiền cần trả nhảy số chính xác. | **PASS** |
| **TC-EX-09** | Xóa bớt sản phẩm trong giỏ đổi trả | Xóa 2 sản phẩm vừa thêm khỏi giỏ hàng POS. | Giỏ hàng giảm còn 4 món, tổng tiền cần thanh toán tự động cập nhật lại. | **PASS** |
| **TC-EX-10** | Điều chỉnh số lượng món đổi trong giỏ | Tăng số lượng món 1 lên 4, giảm món 3 xuống 1. | Thành tiền các dòng và tổng chênh lệch cập nhật mượt mà theo thời gian thực. | **PASS** |
| **TC-EX-11** | Bấm nút "Hủy đổi trả" và từ chối | Bấm "Hủy đổi trả" -> Chọn "NO" ở popup. | Dữ liệu đổi trả được giữ nguyên vẹn, Banner khấu trừ vẫn hiển thị bình thường. | **PASS** |
| **TC-EX-12** | Bấm nút "Hủy đổi trả" và đồng ý | Bấm "Hủy đổi trả" -> Chọn "YES" ở popup. | Dữ liệu đổi trả bị xóa, Banner biến mất, giỏ hàng trở về rỗng (0 món). | **PASS** |
| **TC-EX-13** | Nạp tiếp đơn đổi trả mới sau khi hủy đơn cũ | Nạp đơn đổi trả thứ hai sau khi hủy đơn thứ nhất. | Dữ liệu đơn thứ hai được nạp sạch sẽ, không bị dính vết của đơn thứ nhất. | **PASS** |
| **TC-EX-14** | Thanh toán phần bù chênh lệch qua VietQR | Chênh lệch 170.000 đ, chọn "Chuyển khoản". | Hộp thoại VietQR hiển thị đúng số tiền 170.000 đ cần thu thêm. | **PASS** |
| **TC-EX-15** | Hoàn kho tự động hàng trả nguyên tem | Hàng trả nguyên tem 2 món. | Sau khi thanh toán, tồn kho trong `LoHangDAO` và POS Catalog được cộng thêm 2. | **PASS** |
| **TC-SH-01** | Bàn giao ca không hiện popup đăng nhập QL | Thu ngân bấm "Chốt ca & Bàn giao". | Không hiển thị bất kỳ popup nào bắt Quản lý nhập mật khẩu tại quầy. | **PASS** |
| **TC-SH-02** | Lưu bản ghi giao ca trạng thái Chờ duyệt | Thu ngân chốt ca thành công. | Bản ghi `BangGiaoCa` được thêm vào CSDL với trạng thái `"⏳ Chờ Quản lý duyệt"`. | **PASS** |
| **TC-SH-03** | Đăng xuất đổi ca sau khi chốt | Thu ngân chọn "Có" khi được hỏi đăng xuất. | Ứng dụng đóng Dashboard và quay về màn hình `LoginForm` cho ca sau. | **PASS** |
| **TC-SH-04** | Ẩn nút Duyệt ca đối với Thu ngân | Đăng nhập tài khoản vai trò `NVTN`. | Nút `"✓ QUẢN LÝ DUYỆT & XÁC NHẬN KÉT..."` bị ẩn hoàn toàn (`setVisible(false)`). | **PASS** |
| **TC-SH-05** | Hiển thị nút Duyệt ca đối với Quản lý | Đăng nhập tài khoản vai trò `NVQL` (`admin`). | Nút Duyệt ca hiển thị rõ ràng màu xanh lá trên giao diện `BanGiaoCaPanel`. | **PASS** |
| **TC-SH-06** | Lấy danh sách các ca chờ duyệt từ CSDL | Quản lý bấm nút Duyệt ca. | Hàm `dao.getDanhSachCaChoDuyet()` trả về chính xác danh sách các ca chờ duyệt. | **PASS** |
| **TC-SH-07** | Hộp thoại đối soát chi tiết ca chờ duyệt | Mở `hienThiDialogDuyetCacCaCho()`. | Hiển thị bảng đối soát, bảng chi tiết tiền đầu ca, thu, chi, lý thuyết, thực tế, lệch. | **PASS** |
| **TC-SH-08** | Quản lý phê duyệt ca thành công | Quản lý bấm "Phê duyệt đối soát & Ký nhận két". | Gọi `dao.capNhatGhiChuDuyetCa()`, cập nhật trạng thái `"✓ Đã duyệt (QL: [Tên])"`. | **PASS** |
| **TC-SH-09** | Tự động xuất biên bản bàn giao ca K80 | Sau khi Quản lý phê duyệt thành công. | Hiển thị biên bản nhiệt K80 chứa đầy đủ số liệu kế toán và chữ ký 2 bên. | **PASS** |
| **TC-SH-10** | Tự động sinh mã giao ca liên tục | Kiểm tra hàm `dao.phatSinhMaGiaoCa()`. | Sinh mã định dạng `GCxxx` tăng dần liên tục, không bị trùng lặp. | **PASS** |
| **TC-SH-11** | Bắt buộc giải trình khi két bị lệch tiền | Két thực tế lệch 20.000 đ, ô lý do để trống. | Bấm Chốt ca -> Cảnh báo yêu cầu nhập giải trình lý do trước khi bàn giao. | **PASS** |
| **TC-GUI-01** | Khởi chạy giao diện MainDashboard trên EDT | Chạy `ApplicationLaunchVerificationTest`. | MainDashboard mở thành công trên Event Dispatch Thread, font Segoe UI mượt mà. | **PASS** |
| **TC-GUI-02** | Kiểm tra Look and Feel FlatMacLightLaf | Kiểm tra thuộc tính UIManager khi khởi động. | Giao diện nạp chuẩn `FlatMacLightLaf`, bo góc mềm mại, màu sắc hài hòa. | **PASS** |

### 3.5.3. Kết quả kiểm thử thực nghiệm
- **Biên dịch `.uild.bat`:** Thành công 100%, 0 lỗi cú pháp, 0 cảnh báo nghiêm trọng.
- **Master Test Runner (`ComprehensiveE2ETestRunner`):** Đạt **153 / 153 Tests PASS (100%)**, thời gian thực thi: **31.799 ms**.
- **Tổng số ca kiểm thử tự động toàn dự án:** Đạt **285 / 285 Tests PASS (100%)**.


# CHƯƠNG 4: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 4.1. Kết quả đạt được
Trải qua quá trình nghiên cứu, khảo sát thực tế và hiện thực mã nguồn, nhóm sinh viên đã hoàn thành xuất sắc toàn bộ các mục tiêu đề ra cho đồ án môn học Phát triển ứng dụng:
1. **Hoàn thiện trọn vẹn nghiệp vụ bán lẻ và chăm sóc thú cưng:**
   - Xây dựng thành công ứng dụng máy tính đa năng tích hợp trọn gói cả hai mảng hoạt động: bán lẻ hàng hóa vật lý và cung cấp dịch vụ chăm sóc thú cưng (Spa/Grooming, Pet Hotel).
   - Tối ưu hóa màn hình bán hàng POS với đúng 2 phương thức thanh toán an toàn (Tiền mặt và Chuyển khoản VietQR), phím chọn mệnh giá nhanh, cơ chế tính tiền thối thời gian thực và chốt chặn thanh toán khi tiền khách đưa chưa đủ.
2. **Hiện thực hóa quy trình Đổi trả hàng chuyển sang POS:**
   - Xử lý linh hoạt 2 kịch bản đổi trả: trả hàng hoàn tiền tại chỗ hoặc đổi sang sản phẩm khác chuyển trực tiếp sang POS để thanh toán phần bù chênh lệch, bảo đảm luồng dữ liệu minh bạch và tự động hoàn kho hàng nguyên tem.
3. **Minh bạch hóa quản lý tài chính với Bàn giao ca bất đồng bộ:**
   - Loại bỏ hoàn toàn phiền toái khi phải có mặt Quản lý tại quầy; lưu trữ trạng thái chốt ca chờ duyệt; cho phép Quản lý đăng nhập xem xét, phê duyệt và in biên bản bàn giao nhiệt K80 chuẩn kế toán.
4. **Kiểm soát kho hàng chặt chẽ theo Lô và Hạn dùng FEFO:**
   - Ngăn chặn triệt để tình trạng tiêu hủy hàng cận date; tự động hóa tính điểm tái đặt hàng ROP và bảng giá quy đổi đơn vị tính đóng gói đa cấp.
5. **Chất lượng kỹ thuật và độ tin cậy vượt trội:**
   - Mã nguồn được module hóa theo kiến trúc 3 tầng chuẩn mực; 100% giao diện sử dụng FlatLaf hiện đại và đồ họa vector `Graphics2D` sắc nét.
   - Hệ thống vượt qua **285 / 285 ca kiểm thử tự động (100% PASS)**, biên dịch thành công 0 lỗi, tốc độ khởi động dưới 1.0 giây.

## 4.2. Hạn chế của đồ án
Bên cạnh những kết quả nổi bật, hệ thống vẫn tồn tại một số hạn chế do đặc thù phạm vi đồ án môn học:
- **Mô hình Desktop đơn chi nhánh:** Ứng dụng hiện tại hoạt động theo mô hình Client kết nối trực tiếp CSDL SQL Server nội bộ, chưa hỗ trợ đồng bộ dữ liệu thời gian thực giữa chuỗi nhiều cửa hàng thông qua Cloud API.
- **Chưa có ứng dụng di động cho khách hàng:** Khách hàng vẫn phải đặt lịch hoặc theo dõi thú cưng qua nhân viên quầy, chưa thể tự chủ động đặt lịch hay xem camera phòng Pet Hotel qua ứng dụng smartphone cá nhân.
- **Tích hợp phần cứng ở mức giả lập và driver cục bộ:** Việc in hóa đơn K80 hiện phụ thuộc vào dịch vụ in ấn Windows và xuất file preview, chưa tích hợp trực tiếp driver cổng COM/USB chuyên dụng cho két tiền tự bật (Cash Drawer Trigger).

## 4.3. Hướng phát triển trong tương lai
Để đưa sản phẩm từ đồ án môn học trở thành một giải pháp thương mại hóa thực thụ trên thị trường, nhóm định hướng các giai đoạn nâng cấp tiếp theo:
1. **Chuyển đổi kiến trúc sang Cloud Microservices (SaaS):**
   - Xây dựng hệ thống Backend API trên nền tảng Spring Boot / Node.js, lưu trữ CSDL tập trung trên nền tảng đám mây (Cloud Database), cho phép quản lý chuỗi không giới hạn chi nhánh.
2. **Phát triển Ứng dụng Di động Đa nền tảng (Mobile App iOS & Android):**
   - Ứng dụng dành cho chủ nuôi: Đặt lịch Spa 1 chạm, nhận thông báo nhắc lịch tiêm ngừa vaccine định kỳ, xem camera trực tiếp bé cưng đang lưu trú tại Pet Hotel, tích lũy điểm và đổi voucher quà tặng.
3. **Ứng dụng Trí tuệ Nhân tạo (AI & Computer Vision):**
   - Tích hợp mô hình nhận diện khuôn mặt và giống loài thú cưng qua camera quầy để tự động chào đón và mở hồ sơ y tế.
   - Ứng dụng AI phân tích hình ảnh da lông để hỗ trợ kỹ thuật viên cảnh báo sớm các dấu hiệu viêm da, nấm mốc hay ve rận trước khi làm dịch vụ.

---

# CHƯƠNG 5: KHỞI NGHIỆP (STARTUP PLAN)

*(Biên soạn theo biểu mẫu chuẩn hướng dẫn `8_Startup.docx`)*

## 5.1. Kế hoạch phát triển dự án khởi nghiệp

### 5.1.1. Bối cảnh và lý do thành lập dự án
Thị trường chăm sóc thú cưng tại Việt Nam đang bước vào "thập kỷ vàng" với tốc độ tăng trưởng bùng nổ. Thế hệ trẻ (Gen Z và Millennials) tại các đô thị lớn coi thú cưng là người bạn đồng hành tinh thần vô giá và sẵn sàng chi trả mạnh tay cho các dịch vụ chăm sóc cao cấp. Tuy nhiên, hơn 85% cửa hàng thú cưng độc lập hiện nay vẫn đang vật lộn với các bài toán vận hành thủ công: thất thoát két tiền, nhầm lẫn lịch hẹn Spa, thất thoát hàng cận date và thiếu gắn kết với khách hàng.

Nhận thấy khoảng trống thị trường to lớn giữa các phần mềm bán tạp hóa chung chung (vốn không thể đáp ứng nghiệp vụ đặc thù thú cưng) và các giải pháp ngoại nhập đắt đỏ, Nhóm 07 quyết định thành lập dự án khởi nghiệp công nghệ với tên gọi: **"Mio Tech - Nền tảng Quản trị & Chăm sóc Thú cưng Toàn diện"**.

### 5.1.2. Tầm nhìn và Sứ mệnh
- **Tầm nhìn:** Trở thành nền tảng công nghệ số 1 tại Việt Nam và Đông Nam Á chuyên biệt hóa cho hệ sinh thái dịch vụ thú cưng, đồng hành cùng 10.000+ Pet Shop và phòng khám thú y chuyển đổi số thành công vào năm 2030.
- **Sứ mệnh:** Đơn giản hóa mọi khâu vận hành cho chủ cửa hàng, đem lại trải nghiệm chăm sóc tiện nghi, hạnh phúc nhất cho thú cưng và sự an tâm tuyệt đối cho chủ nuôi.

### 5.1.3. Lộ trình 3 giai đoạn phát triển (2026 - 2028)

**Bảng 5.2: Lộ trình 3 giai đoạn phát triển sản phẩm (2026 - 2028)**

| Giai đoạn | Thời gian | Tên cột mốc | Mục tiêu kỹ thuật & Kinh doanh trọng tâm |
|:---:|:---:|---|---|
| **Giai đoạn 1** | **2026** | **Hoàn thiện Desktop POS & Thử nghiệm 1 Cửa hàng** | - Chuẩn hóa ứng dụng Java Swing + SQL Server hiện tại.<br>- Triển khai thử nghiệm thực tế tại 1 Pet Shop đối tác ở TP.HCM.<br>- Đo lường và tối ưu hóa 3 luồng: POS VietQR, Đổi trả sang POS và Bàn giao ca chờ duyệt.<br>- Đạt điểm hòa vốn ban đầu về chi phí thiết bị. |
| **Giai đoạn 2** | **2027** | **Chuyển đổi Cloud SaaS & Mở rộng 50 Cửa hàng** | - Nâng cấp hệ thống lên nền tảng Web/Cloud SaaS đa chi nhánh (Spring Boot + ReactJS).<br>- Áp dụng mô hình thu phí thuê bao hàng tháng (Subscription).<br>- Phân phối gói phần mềm kèm phần cứng máy in K80, máy quét mã vạch.<br>- Tiếp cận và ký kết hợp đồng với 50 cửa hàng thú cưng tại khu vực phía Nam. |
| **Giai đoạn 3** | **2028 trở đi** | **Hệ sinh thái Di động (Mobile App) & Kết nối Chuỗi** | - Ra mắt Mobile App dành riêng cho chủ thú cưng (đặt lịch Spa, sổ tiêm chủng điện tử, camera Pet Hotel).<br>- Tích hợp AI phân tích hành vi mua sắm và gợi ý combo dinh dưỡng.<br>- Mở rộng mạng lưới ra toàn quốc (Hà Nội, Đà Nẵng, Cần Thơ), hướng tới vòng gọi vốn hạt giống (Seed Round). |

---

## 5.2. Dự án của nhóm là gì? Trình bày kế hoạch cho dự án

### 5.2.1. Mục tiêu và phạm vi thương mại hóa
- Cung cấp gói giải pháp "All-in-One": Phần mềm quản lý chuyên sâu + Thiết bị phần cứng quầy thu ngân + Dịch vụ triển khai đào tạo tại chỗ trong vòng 24 giờ.
- Khách hàng mục tiêu: Các cửa hàng kinh doanh sản phẩm thú cưng độc lập, chuỗi Pet Shop 1 - 3 chi nhánh, các tiệm Grooming/Spa thú cưng và các khách sạn thú cưng lưu trú vừa và nhỏ.

### 5.2.2. Mô hình kinh doanh và Các dòng doanh thu (Revenue Streams)
Dự án áp dụng mô hình kinh doanh đa nguồn thu bền vững:
1. **Doanh thu Bản quyền & Thuê bao (SaaS Subscription):**
   - *Gói Starter (Cửa hàng đơn lẻ):* 199.000 VNĐ / tháng (hoặc 1.990.000 VNĐ / năm) - Quản lý bán hàng POS, tồn kho FEFO, 2 tài khoản.
   - *Gói Professional (Bán lẻ + Spa + Hotel):* 399.000 VNĐ / tháng - Đầy đủ phân hệ Spa 4 bước, Pet Hotel, đổi trả sang POS, bàn giao ca chờ duyệt, không giới hạn tài khoản.
   - *Gói Enterprise (Chuỗi chi nhánh):* 799.000 VNĐ / tháng - Quản lý tập trung đa chi nhánh, báo cáo điều hành nâng cao.
2. **Doanh thu Bán thiết bị phần cứng POS (Hardware Bundles):**
   - Cung cấp trọn bộ Combo máy in nhiệt K80 USB/LAN, máy quét mã vạch 2D đọc mã VietQR, ngăn kéo đựng tiền tự động đóng mở theo lệnh in. Tỷ suất lợi nhuận gộp phần cứng đạt 25 - 30%.
3. **Hoa hồng Tích hợp Cổng thanh toán & Dịch vụ Giá trị Gia tăng:**
   - Hợp tác với các cổng trung gian thanh toán (VNPay, VietQR, MoMo) để hưởng hoa hồng giao dịch từ 0.2% - 0.5% trên tổng giá trị thanh toán không dùng tiền mặt.
   - Cung cấp dịch vụ in phôi thẻ thành viên VIP và vòng đeo cổ gắn chip Microchip nhận diện thú cưng.

### 5.2.3. Cấu trúc tổ chức nhân sự khởi nghiệp

**Bảng 5.1: Cơ cấu tổ chức nhân sự công ty khởi nghiệp Mio Tech**

| STT | Nhân sự | Vị trí đảm nhiệm | Trách nhiệm chính trong công ty khởi nghiệp |
|:---:|---|:---:|---|
| **1** | **Lê Khải Minh** | **Giám đốc điều hành (CEO & Project Manager)** | Điều hành chiến lược tổng thể, quan hệ nhà đầu tư, đàm phán hợp đồng đối tác bán lẻ và đối tác cổng thanh toán tài chính. |
| **2** | **Lê Anh Minh** | **Giám đốc công nghệ (CTO & Backend Lead)** | Phụ trách kiến trúc hệ thống, an ninh dữ liệu CSDL SQL Server, quản lý hạ tầng Cloud và tích hợp API thanh toán số. |
| **3** | **Tô Thành Đạt** | **Trưởng phòng Thiết kế (UI/UX & Frontend Lead)** | Nghiên cứu trải nghiệm người dùng tại quầy, thiết kế giao diện FlatLaf, tối ưu hóa thao tác 1 chạm cho thu ngân. |
| **4** | **Võ Thị Hồng Ngọc** | **Trưởng bộ phận Kiểm thử & Marketing (QA & Growth Lead)** | Quản lý chất lượng phần mềm (Test Cases), xây dựng kênh truyền thông số, chăm sóc khách hàng và phát triển thị trường. |

---

## 5.3. Dự án của nhóm có gì khác biệt? (Tính sáng tạo và Độc đáo)

So với các phần mềm bán hàng tạp hóa chung trên thị trường (như KiotViet, Sapo, POS365 vốn chỉ thiết kế cho hàng tạp hóa hoặc thời trang), hệ thống Mio Pet Shop sở hữu 5 điểm khác biệt mang tính đột phá:

1. **Khác biệt về Hướng tiếp cận thị trường (Chuyên biệt hóa Thú cưng):**
   - Thay vì bắt người dùng "gọt chân cho vừa giày" với các mẫu bán hàng tạp hóa, Mio được thiết kế từ gốc rễ để phục vụ đúng tập quán ngành thú cưng: quản lý hồ sơ bé cưng theo mã Microchip, lưu sổ tiêm phòng dại và tiền sử dị ứng sữa tắm.
2. **Khác biệt về Công nghệ và Thiết kế hệ thống (FEFO & VietQR Động):**
   - Quản lý kho theo từng Lô nhập và tự động ưu tiên xuất lô cận hạn sử dụng trước (FEFO), bảo vệ vốn lưu động cho chủ shop.
   - Tự động quy đổi tỷ lệ đóng gói chiết rót xả bao (từ bao 20kg ra từng kg, từ lốc 6 lon ra lon lẻ) mà vẫn giữ nguyên tính toàn vẹn tồn kho cơ sở.
   - Tích hợp mã VietQR Napas 24/7 động gắn cứng số tiền và mã hóa đơn, kèm cơ chế Offline Fallback an toàn tuyệt đối.
3. **Khác biệt về Vận hành và Dịch vụ (Ba Luồng Nghiệp Vụ Cốt Lõi):**
   - *Bán hàng POS:* Chỉ giữ đúng 2 phương thức Tiền mặt / VietQR, bổ sung các nút chọn mệnh giá nhanh, tự động đảo phương thức thông minh, tính tiền thối thời gian thực và chặn đứng việc thanh toán khi khách đưa thiếu tiền.
   - *Đổi trả hàng hóa sang POS:* Không xử lý chắp vá tại chỗ; dữ liệu hàng trả kèm số tiền khấu trừ được nạp trực tiếp vào POS để khách chọn thêm đồ mới và thu phần chênh lệch minh bạch.
   - *Bàn giao ca bất đồng bộ:* Không cần Quản lý túc trực tại quầy; thu ngân gửi chốt két lưu trạng thái chờ duyệt; Quản lý đăng nhập xem xét, ký duyệt và in biên bản nhiệt K80 chuẩn kế toán.
4. **Khác biệt về Ý tưởng mở rộng sản phẩm (Hệ sinh thái Thú cưng):**
   - Tích hợp thẻ tích điểm VIP kết hợp Thẻ gói dịch vụ định kỳ (Spa Pass Punch Card 5-10 buổi), tạo nguồn doanh thu trả trước ổn định cho cửa hàng.
   - Sơ đồ phòng khách sạn thú cưng Pet Hotel dạng ma trận trực quan, kiểm soát trạng thái phòng trống theo thời gian thực.
5. **Khác biệt về Tinh thần khởi nghiệp và Năng lực thực thi:**
   - Đội ngũ sáng lập là những sinh viên CNTT IUH đầy nhiệt huyết, nắm vững toàn bộ mã nguồn từ gốc rễ, có khả năng tùy biến và bảo trì hệ thống thần tốc mà không phụ thuộc vào bất kỳ nhà thầu bên ngoài nào.

---

## TÀI LIỆU THAM KHẢO

*(Trình bày theo chuẩn định dạng học thuật quy định tại `9_REPORT FINAL.docx`)*

[1] Đặng Văn Đức (2002), *Phân tích thiết kế hướng đối tượng bằng UML (Thực hành với Rational Rose)*, NXB Khoa học và Kỹ thuật, TP.HCM.  
[2] Nhiều tác giả (2014), *Giáo trình Lập trình hệ thống với Java*, NXB Đại học Quốc gia TP.HCM, TP.HCM.  
[3] Phạm Nguyễn Cương, Nguyễn Trần Minh Thư, Hồ Quốc Bảo (2016), *Giáo trình Phân tích thiết kế hệ thống thông tin theo hướng đối tượng*, NXB Khoa học và Kỹ thuật, TP.HCM.  
[4] Đoàn Văn Ban, Đoàn Văn Trung (2014), *Giáo trình lập trình Java*, Nhà xuất bản Giáo dục Việt Nam, Hà Nội.  
[5] Craig Larman (2012), *Applying UML and Patterns: An Introduction to Object-Oriented Analysis and Design and Iterative Development*, Prentice Hall, New Jersey, USA.  
[6] Sari, D. P., & Wijaya, A. (2021), “Design and Implementation of Point of Sale (POS) and Inventory Management System Using FEFO Method for Pet Shop”, *International Journal of Information System and Technology*, Vol.5 (2), pp. 112-120.  
[7] Pratama, R. A., & Nugroho, Y. (2023), “Development of Pet Care, Grooming Appointment and Medical Record Information System”, *International Journal on Science and Technology (IJSAT)*, Vol.14 (3), pp. 45-53.  
[8] Microsoft Corporation (Oct. 2025), *Microsoft SQL Server & JDBC Driver Technical Documentation*, https://learn.microsoft.com/en-us/sql/sql-server.

---

## PHỤ LỤC

### Phụ lục 1: Bảng phân công nhiệm vụ thành viên Nhóm 07

| Thành viên | Nhiệm vụ chính trong đồ án | Mức độ đóng góp |
|---|---|:---:|
| **Lê Khải Minh** (Trưởng nhóm) | - Kiến trúc tổng thể hệ thống, phân rã Use Cases.<br>- Hiện thực màn hình POS, 2 phương thức thanh toán, nút mệnh giá nhanh, tiền thối thời gian thực.<br>- Tích hợp VietQR Napas 24/7 và cổng VNPay Sandbox.<br>- Hiện thực luồng Đổi trả hàng hóa chuyển sang POS thanh toán chênh lệch.<br>- Soạn thảo báo cáo tổng kết và kiểm thử tích hợp. | **100%** |
| **Tô Thành Đạt** | - Thiết kế giao diện phẳng FlatMacLightLaf và hệ thống vector Lucide Icons.<br>- Hiện thực phân hệ Quản lý Kho, Sản phẩm và Lô hàng FEFO.<br>- Hiện thực hộp thoại Quy đổi ĐVT và tính toán Điểm đặt hàng ROP.<br>- Hỗ trợ kiểm thử giao diện người dùng. | **100%** |
| **Võ Thị Hồng Ngọc** | - Khảo sát thực tế các Pet Shop, lập bảng câu hỏi Q&A phỏng vấn.<br>- Hiện thực phân hệ Quản lý Lịch hẹn & Dịch vụ Spa 4 bước.<br>- Hiện thực phân hệ Khách sạn Thú cưng Pet Hotel ma trận phòng.<br>- Hiện thực quản lý Hồ sơ thú cưng, Sức khỏe, Microchip và Thẻ Spa Pass.<br>- Xây dựng kịch bản kiểm thử chức năng. | **100%** |
| **Lê Anh Minh** | - Thiết kế cơ sở dữ liệu quan hệ EER và viết tập lệnh DDL SQL Server.<br>- Hiện thực phân hệ Quản lý Nhân sự, Tài khoản và RBAC 4 vai trò.<br>- Hiện thực phân hệ Phân ca tuần (Timetable Matrix).<br>- Hiện thực phân hệ Bàn giao ca & Quản lý duyệt két tiền.<br>- Xây dựng trung tâm báo biểu và xuất dữ liệu Excel. | **100%** |

### Phụ lục 2: Nhật ký hoạt động nhóm và liên kết kho mã nguồn GitHub
- **Kho lưu trữ mã nguồn nội bộ:** Dự án được quản lý phiên bản nghiêm ngặt trên Git với các nhánh tính năng (`feature/pos-payment`, `feature/exchange-flow`, `feature/shift-handover`), các Pull Request đều được kiểm tra biên dịch chéo trước khi hợp nhất vào nhánh chính.
- **Tiến độ thực hiện 15 tuần:**
  * Tuần 1 - 3: Khảo sát thực trạng, phỏng vấn chủ shop, thu thập yêu cầu và lập SRS.
  * Tuần 4 - 6: Phân tích hướng đối tượng, thiết kế Use Case, Class Diagram và EER CSDL.
  * Tuần 7 - 10: Xây dựng khung kiến trúc 3 tầng, hiện thực CSDL và các màn hình chính.
  * Tuần 11 - 13: Hoàn thiện 3 luồng cốt lõi (POS VietQR, Đổi trả sang POS, Giao ca chờ duyệt).
  * Tuần 14 - 15: Kiểm thử tự động 285 ca test, viết báo cáo tổng kết và chuẩn bị xuất bản.

### Phụ lục 3: Nhật ký sử dụng AI (AI Usage Log theo quy định môn học)

*(Tuân thủ quy chế sử dụng công cụ trí tuệ nhân tạo của Trường ĐH Công nghiệp TP.HCM)*

| STT | Giai đoạn đề tài | Công cụ AI sử dụng | Mục đích và Nội dung Prompt yêu cầu | Kết quả do AI sinh ra | Phần được nhóm giữ nguyên | Phần nhóm chỉnh sửa, hoàn thiện | Lý do điều chỉnh |
|:---:|---|:---:|---|---|---|---|---|
| 1 | Khởi tạo giao diện Swing | Gemini / Claude | Gợi ý cấu trúc lồng ghép các Layout Manager (`BorderLayout`, `CardLayout`, `GridBagLayout`) cho dashboard hiện đại bo góc. | Đoạn mã mẫu tạo panel dạng thẻ và cấu hình FlatLaf. | Cấu trúc phân vùng `BorderLayout.NORTH` và `BorderLayout.CENTER`. | Viết lại toàn bộ logic Lazy Loading và cơ chế hiển thị nút Pill Tabs theo phân quyền RBAC. | Đảm bảo tính bảo mật và tối ưu hóa thời gian mở ứng dụng `< 1.0s`. |
| 2 | Tích hợp VietQR | Gemini / Claude | Hướng dẫn cấu trúc URL chuẩn tạo mã VietQR theo thông tư NAPAS 24/7. | Chuỗi cú pháp `https://img.vietqr.io/image/...`. | Cú pháp format mã ngân hàng và số tài khoản MB Bank. | Tự phát triển cơ chế Offline Fallback vẽ ảnh QR nội bộ 300x320px khi mất Internet. | Tránh lỗi ứng dụng bị treo khi máy tính quầy mất mạng. |
| 3 | Tự động hóa Kiểm thử | Gemini / Claude | Gợi ý cấu trúc Test Harness tự động chạy trên Java 17 thuần không cần JUnit. | Lớp `TestFramework` cơ bản hỗ trợ đếm số ca Pass/Fail. | Các hàm so sánh `assertEquals`, `assertTrue`. | Mở rộng thêm 285 ca kiểm thử thực tế và kiểm thử đối kháng (Adversarial Tests) cho 3 luồng nghiệp vụ. | Kiểm soát triệt để mọi trường hợp biên, số âm, ký tự lạ và độ trễ giao diện. |
| 4 | Soạn thảo Báo cáo | Gemini / Claude | Hỗ trợ chuẩn hóa định dạng bảng biểu và rà soát lỗi chính tả học thuật theo form `9_REPORT FINAL.docx`. | Bản nháp dàn ý 5 chương và gợi ý văn phong. | Dàn ý phân cấp tiêu đề chuẩn của môn học. | Biên soạn toàn văn 100% dựa trên mã nguồn thực tế tại `N07-HeThongQuanLyCuaHangThuCungMio`. | Tuân thủ tuyệt đối nguyên tắc "Code -> Báo cáo", không bịa đặt tính năng. |
