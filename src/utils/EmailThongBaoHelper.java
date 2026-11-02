package utils;

import views.EmailPreviewDialog;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Lớp hỗ trợ tạo và gửi email thông báo tự động cho khách hàng
// Phục vụ riêng cho phân hệ Pet Hotel (chuồng trại), Lịch hẹn Spa Grooming và Sổ sức khỏe tiêm phòng của bạn Ngọc
// Code theo phong cách HTML inline CSS để tương thích tốt với mọi hòm thư như Gmail, Outlook
public class EmailThongBaoHelper {

    private static final DecimalFormat DF_MONEY = new DecimalFormat("#,### đ");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    // Hàm gọi hiển thị dialog xem trước email và thực hiện gửi
    public static boolean moHopThoaiGuiEmail(Window owner, String toEmail, String tieuDe, String htmlContent) {
        EmailPreviewDialog dlg = new EmailPreviewDialog(owner, toEmail, tieuDe, htmlContent);
        dlg.setVisible(true);
        return dlg.isDaGuiThanhCong();
    }

    // 1. Mẫu Email Xác nhận Check-in nhận phòng thú cưng tại Pet Hotel
    public static String taoEmailCheckInHotel(String tenKhach, String sdtKhach, String maPhong, String loaiPhong,
                                             long donGiaNgay, String tenThuCung, String giongLoai,
                                             String checkInStr, String checkOutDuKien, String ghiChu) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ffd8be; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background-color: #f25042; color: #ffffff; padding: 18px 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 20px;'>🐾 MIO PET HOTEL - XÁC NHẬN TIẾP NHẬN THÚ CƯNG</h2>"
                + "  <p style='margin: 5px 0 0; font-size: 13px;'>Khách sạn thú cưng chuẩn 5 sao - Chăm sóc tận tâm như tại nhà</p>"
                + "</div>"
                + "<div style='padding: 20px; background-color: #fffffe; color: #2b2c34;'>"
                + "  <p>Xin chào quý khách <b>" + tenKhach + "</b> (" + sdtKhach + "),</p>"
                + "  <p>Cửa hàng thú cưng Mio xin gửi lời cảm ơn quý khách đã tin tưởng gửi bé <b>" + tenThuCung + "</b> (" + giongLoai + ") lưu trú tại khách sạn của chúng tôi.</p>"
                + "  <table style='width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 13px;'>"
                + "    <tr style='background-color: #fff3ec; border-bottom: 1px solid #ffd8be;'><td style='padding: 8px; font-weight: bold;'>Mã chuồng / Phòng:</td><td style='padding: 8px; color: #e16162; font-weight: bold;'>" + maPhong + " (" + loaiPhong + ")</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Thời gian Check-in:</td><td style='padding: 8px;'>" + checkInStr + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Dự kiến Check-out:</td><td style='padding: 8px;'>" + checkOutDuKien + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Đơn giá lưu trú:</td><td style='padding: 8px; font-weight: bold; color: #e16162;'>" + DF_MONEY.format(donGiaNgay) + "/ngày</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Dặn dò chăm sóc:</td><td style='padding: 8px; color: #555;'>" + (ghiChu != null && !ghiChu.isEmpty() ? ghiChu : "Chăm sóc theo khẩu phần tiêu chuẩn của shop") + "</td></tr>"
                + "  </table>"
                + "  <div style='background-color: #fefae0; border-left: 4px solid #dda15e; padding: 10px 14px; margin: 15px 0; font-size: 12px;'>"
                + "    ℹ️ <b>Cam kết tại Mio:</b> Bé sẽ được camera quan sát 24/7, phòng luôn bật máy lọc không khí và khử khuẩn tia cực tím hàng ngày. Nhân viên sẽ gửi nhật ký ăn uống cho bạn mỗi buổi chiều."
                + "  </div>"
                + "  <p style='font-size: 13px;'>Khi cần cập nhật thêm tình hình hoặc gửi video trực tiếp của bé, bạn chỉ cần liên hệ Zalo Hotline: <b>0988.111.222</b>.</p>"
                + "</div>"
                + "<div style='background-color: #f9f9fb; padding: 12px; text-align: center; font-size: 11px; color: #888; border-top: 1px solid #eee;'>"
                + "  Mio Pet Care & Spa • 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM • Hotline: 0988.111.222"
                + "</div>"
                + "</div>";
    }

    // 2. Mẫu Email Báo cáo Chăm sóc & Dinh dưỡng hàng ngày tại Pet Hotel (Daily Care Report Card)
    public static String taoEmailBaoCaoDinhDuong(String tenKhach, String maPhong, String tenThuCung,
                                                boolean daAnSang, boolean daAnToi, boolean daDiDao,
                                                long phiPhatSinh, String ghiChu) {
        String statusSang = daAnSang ? "<span style='color: #2e7d32; font-weight: bold;'>Đã ăn hết khẩu phần hạt & pate</span>" : "<span style='color: #c62828;'>Chưa đến giờ hoặc bé kén ăn</span>";
        String statusToi = daAnToi ? "<span style='color: #2e7d32; font-weight: bold;'>Đã ăn xong bữa tối ngon lành</span>" : "<span style='color: #888;'>Đang chuẩn bị phần ăn tối</span>";
        String statusDiDao = daDiDao ? "<span style='color: #2e7d32; font-weight: bold;'>Đã đi dạo sân cỏ & vệ sinh sạch sẽ</span>" : "<span style='color: #888;'>Đang nghỉ ngơi trong chuồng</span>";

        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #c8e6c9; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background-color: #2e7d32; color: #ffffff; padding: 18px 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 19px;'>📝 NHẬT KÝ CHĂM SÓC HÀNG NGÀY CỦA BÉ " + tenThuCung.toUpperCase() + "</h2>"
                + "  <p style='margin: 5px 0 0; font-size: 12px;'>Báo cáo lưu trú phòng " + maPhong + " - Cập nhật lúc " + DTF.format(LocalDateTime.now()) + "</p>"
                + "</div>"
                + "<div style='padding: 20px; background-color: #ffffff; color: #2b2c34;'>"
                + "  <p>Chào bạn <b>" + tenKhach + "</b>,</p>"
                + "  <p>Hôm nay bé <b>" + tenThuCung + "</b> tại khách sạn Pet Hotel sinh hoạt rất ngoan và vui vẻ. Đội ngũ nhân viên xin gửi bạn báo cáo sinh hoạt trong ngày của bé:</p>"
                + "  <table style='width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 13px;'>"
                + "    <tr style='border-bottom: 1px solid #e0e0e0;'><td style='padding: 10px; font-weight: bold; width: 35%;'>🥣 Khẩu phần ăn sáng:</td><td style='padding: 10px;'>" + statusSang + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #e0e0e0;'><td style='padding: 10px; font-weight: bold;'>🍲 Khẩu phần ăn tối:</td><td style='padding: 10px;'>" + statusToi + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #e0e0e0;'><td style='padding: 10px; font-weight: bold;'>🌳 Đi dạo & Vệ sinh:</td><td style='padding: 10px;'>" + statusDiDao + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #e0e0e0;'><td style='padding: 10px; font-weight: bold;'>💊 Dịch vụ / Thuốc thêm:</td><td style='padding: 10px; color: #d84315; font-weight: bold;'>" + DF_MONEY.format(phiPhatSinh) + "</td></tr>"
                + "    <tr><td style='padding: 10px; font-weight: bold;'>📋 Ghi chú thể trạng:</td><td style='padding: 10px; color: #424242;'>" + (ghiChu != null && !ghiChu.isEmpty() ? ghiChu : "Bé lanh lợi, thân nhiệt bình thường, hòa đồng với bạn bè xung quanh") + "</td></tr>"
                + "  </table>"
                + "  <p style='font-size: 13px;'>Bạn hoàn toàn yên tâm tận hưởng chuyến đi nhé, các bảo mẫu tại Mio đang chăm sóc bé rất chu đáo!</p>"
                + "</div>"
                + "<div style='background-color: #f1f8e9; padding: 12px; text-align: center; font-size: 11px; color: #558b2f; border-top: 1px solid #dcedc8;'>"
                + "  Mio Pet Care & Boarding • Tổng đài hỗ trợ 24/7: 0988.111.222"
                + "</div>"
                + "</div>";
    }

    // 3. Mẫu Email Xác nhận Lịch hẹn Spa & Grooming
    public static String taoEmailXacNhanLichHen(String tenKhach, String sdtKhach, String maLichHen,
                                               String tenDichVu, String tenThuCung, String tenKtv,
                                               String ngayHenStr, String ghiChu) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #b3e5fc; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background-color: #0288d1; color: #ffffff; padding: 18px 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 20px;'>✂️ MIO SPA & GROOMING - XÁC NHẬN LỊCH HẸN</h2>"
                + "  <p style='margin: 5px 0 0; font-size: 13px;'>Mã phiếu hẹn: <b>" + maLichHen + "</b></p>"
                + "</div>"
                + "<div style='padding: 20px; background-color: #ffffff; color: #2b2c34;'>"
                + "  <p>Kính chào quý khách <b>" + tenKhach + "</b>,</p>"
                + "  <p>Cửa hàng Mio đã tiếp nhận lịch hẹn chăm sóc làm đẹp cho bé <b>" + tenThuCung + "</b>. Chi tiết lịch hẹn như sau:</p>"
                + "  <table style='width: 100%; border-collapse: collapse; margin: 15px 0; font-size: 13px;'>"
                + "    <tr style='background-color: #e1f5fe; border-bottom: 1px solid #b3e5fc;'><td style='padding: 8px; font-weight: bold;'>Dịch vụ đăng ký:</td><td style='padding: 8px; color: #0277bd; font-weight: bold;'>" + tenDichVu + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Thời gian hẹn đến:</td><td style='padding: 8px; font-weight: bold; color: #d32f2f;'>" + ngayHenStr + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Kỹ thuật viên chỉ định:</td><td style='padding: 8px;'>" + (tenKtv != null && !tenKtv.isEmpty() ? tenKtv : "Shop sẽ sắp xếp KTV tay nghề cao nhất") + "</td></tr>"
                + "    <tr style='border-bottom: 1px solid #eee;'><td style='padding: 8px; font-weight: bold;'>Lưu ý của chủ nuôi:</td><td style='padding: 8px; color: #555;'>" + (ghiChu != null && !ghiChu.isEmpty() ? ghiChu : "Không có") + "</td></tr>"
                + "  </table>"
                + "  <p style='font-size: 12px; color: #666;'>* Quý khách vui lòng đưa bé đến trước giờ hẹn 10 phút để bác sĩ kiểm tra da lông sơ bộ trước khi tắm cắt. Nếu có thay đổi lịch, vui lòng gọi trước cho shop qua số 0988.111.222.</p>"
                + "</div>"
                + "<div style='background-color: #f9f9fb; padding: 12px; text-align: center; font-size: 11px; color: #888; border-top: 1px solid #eee;'>"
                + "  Hệ Thống Chăm Sóc Thú Cưng Mio • Đặt lịch trực tuyến • Tư vấn tạo kiểu chuyên nghiệp"
                + "</div>"
                + "</div>";
    }

    // 4. Mẫu Email Cảnh báo Tái chủng Vaccine & Sổ giun (kèm Mã Giảm Giá 10%)
    public static String taoEmailNhacTiemPhong(String tenKhach, String tenThuCung, String tenMuiTiem,
                                              String ngayHetHan, String maVoucher) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ffcc80; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background-color: #ef6c00; color: #ffffff; padding: 18px 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 19px;'>💉 THÔNG BÁO TÁI CHỦNG VACCINE ĐỊNH KỲ</h2>"
                + "  <p style='margin: 5px 0 0; font-size: 13px;'>Bảo vệ sức khỏe bé <b>" + tenThuCung + "</b> cùng Mio Pet Clinic</p>"
                + "</div>"
                + "<div style='padding: 20px; background-color: #ffffff; color: #2b2c34;'>"
                + "  <p>Thân gửi bạn <b>" + tenKhach + "</b>,</p>"
                + "  <p>Hệ thống theo dõi sức khỏe của Mio ghi nhận bé <b>" + tenThuCung + "</b> sắp đến hạn tiêm phòng định kỳ theo phác đồ thú y:</p>"
                + "  <div style='background-color: #fff8e1; border: 1px dashed #ffa000; border-radius: 6px; padding: 14px; margin: 15px 0;'>"
                + "    <p style='margin: 0 0 6px 0; font-size: 14px;'>🛡️ <b>Mũi tiêm cần tái chủng:</b> <span style='color: #c62828; font-weight: bold;'>" + tenMuiTiem + "</span></p>"
                + "    <p style='margin: 0; font-size: 14px;'>📅 <b>Hạn tái chủng đề xuất:</b> <span style='font-weight: bold;'>" + ngayHetHan + "</span></p>"
                + "  </div>"
                + "  <p style='font-size: 13px;'>Việc tiêm phòng đúng hạn giúp bé duy trì kháng thể bảo vệ tối ưu trước các bệnh truyền nhiễm nguy hiểm (như Care, Parvo, Dại). Đừng để trễ hạn bạn nhé!</p>"
                + "  <div style='background-color: #e8f5e9; border: 1px solid #a5d6a7; border-radius: 6px; padding: 12px; text-align: center; margin: 15px 0;'>"
                + "    <span style='font-size: 12px; color: #2e7d32; font-weight: bold;'>🎁 TẶNG BẠN VOUCHER ƯU ĐÃI 10% DỊCH VỤ VACCINE & SPA:</span><br/>"
                + "    <span style='display: inline-block; margin-top: 6px; padding: 6px 14px; background-color: #2e7d32; color: #fff; font-weight: bold; font-size: 15px; border-radius: 4px; letter-spacing: 1px;'>" + maVoucher + "</span>"
                + "  </div>"
                + "  <p style='font-size: 13px;'>Đưa bé ghé Mio ngay hoặc gọi <b>0988.111.222</b> để đặt lịch trước và giữ phòng tiêm cho bé nhé!</p>"
                + "</div>"
                + "<div style='background-color: #fafafa; padding: 12px; text-align: center; font-size: 11px; color: #888; border-top: 1px solid #eee;'>"
                + "  Phòng Tiêm Chủng Thú Y & Spa Mio • Chăm sóc sức khỏe toàn diện cho thú cưng"
                + "</div>"
                + "</div>";
    }
}
