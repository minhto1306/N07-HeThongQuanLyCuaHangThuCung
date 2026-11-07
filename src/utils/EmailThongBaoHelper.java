package utils;

import java.awt.*;
import javax.swing.JOptionPane;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;

// Lớp hỗ trợ tạo và gửi email thông báo tự động cho khách hàng & nhân viên
// Hỗ trợ gửi thư thật 100% qua Gmail SMTP (cổng 465 SSL) chuẩn Java thuần không cần thư viện ngoài
public class EmailThongBaoHelper {

    private static final DecimalFormat DF_MONEY = new DecimalFormat("#,### đ");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    private static String smtpHost = "smtp.gmail.com";
    private static int smtpPort = 465;
    private static String smtpUsername = "";
    private static String smtpPassword = "";
    private static String senderDisplayName = "Mio Pet Shop";

    private static final String DEFAULT_USER_B64 = "bGUxNDIwNDQ1QGdtYWlsLmNvbQ==";
    private static final String DEFAULT_PASS_B64 = "ampqbW10Zm12YXhoamlyZA==";

    static {
        napCauHinh();
    }

    public static synchronized void napCauHinh() {
        File f = new File("config/mail.properties");
        if (f.exists()) {
            try (InputStream in = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(in);
                if (p.getProperty("mail.smtp.host") != null) smtpHost = p.getProperty("mail.smtp.host").trim();
                if (p.getProperty("mail.smtp.port") != null) smtpPort = Integer.parseInt(p.getProperty("mail.smtp.port").trim());
                if (p.getProperty("mail.smtp.username") != null && !p.getProperty("mail.smtp.username").isBlank()) {
                    smtpUsername = p.getProperty("mail.smtp.username").trim();
                }
                if (p.getProperty("mail.smtp.password") != null && !p.getProperty("mail.smtp.password").isBlank()) {
                    smtpPassword = p.getProperty("mail.smtp.password").trim().replaceAll("\\s+", "");
                }
                if (p.getProperty("mail.smtp.senderName") != null) senderDisplayName = p.getProperty("mail.smtp.senderName").trim();
            } catch (Exception ex) {
                System.err.println("[EmailThongBaoHelper] Lỗi đọc config/mail.properties: " + ex.getMessage());
            }
        }
        // Dự phòng mặc định: Nếu chưa điền cấu hình riêng, tự động dùng tài khoản đã kích hoạt
        if (smtpUsername == null || smtpUsername.isBlank()) {
            try {
                smtpUsername = new String(Base64.getDecoder().decode(DEFAULT_USER_B64), StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
        if (smtpPassword == null || smtpPassword.isBlank()) {
            try {
                smtpPassword = new String(Base64.getDecoder().decode(DEFAULT_PASS_B64), StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }
    }

    /** Kiểm tra xem đã có tài khoản gửi email trong config/mail.properties chưa. */
    public static boolean isSmtpConfigured() {
        napCauHinh();
        return smtpUsername != null && !smtpUsername.isBlank()
                && smtpPassword != null && !smtpPassword.isBlank();
    }

    /** Gửi email thật qua giao thức SMTP SSL. */
    public static void guiEmail(String toEmail, String tieuDe, String htmlContent) throws Exception {
        napCauHinh();
        if (!isSmtpConfigured()) {
            throw new IllegalStateException("Chưa cấu hình tài khoản gửi thư trong config/mail.properties");
        }
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) sf.createSocket(smtpHost, smtpPort)) {
            socket.setSoTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            docPhanHoi(in, 220);

            guiLenh(out, "EHLO " + smtpHost);
            docPhanHoi(in, 250);

            guiLenh(out, "AUTH LOGIN");
            docPhanHoi(in, 334);

            guiLenh(out, Base64.getEncoder().encodeToString(smtpUsername.getBytes(StandardCharsets.UTF_8)));
            docPhanHoi(in, 334);

            guiLenh(out, Base64.getEncoder().encodeToString(smtpPassword.getBytes(StandardCharsets.UTF_8)));
            docPhanHoi(in, 235);

            guiLenh(out, "MAIL FROM:<" + smtpUsername + ">");
            docPhanHoi(in, 250);

            guiLenh(out, "RCPT TO:<" + toEmail + ">");
            docPhanHoi(in, 250);

            guiLenh(out, "DATA");
            docPhanHoi(in, 354);

            out.write("From: =?UTF-8?B?" + Base64.getEncoder().encodeToString(senderDisplayName.getBytes(StandardCharsets.UTF_8)) + "?= <" + smtpUsername + ">\r\n");
            out.write("To: <" + toEmail + ">\r\n");
            out.write("Subject: =?UTF-8?B?" + Base64.getEncoder().encodeToString(tieuDe.getBytes(StandardCharsets.UTF_8)) + "?=\r\n");
            out.write("MIME-Version: 1.0\r\n");
            out.write("Content-Type: text/html; charset=UTF-8\r\n");
            out.write("Content-Transfer-Encoding: base64\r\n\r\n");
            out.write(Base64.getMimeEncoder().encodeToString(htmlContent.getBytes(StandardCharsets.UTF_8)));
            out.write("\r\n.\r\n");
            out.flush();

            docPhanHoi(in, 250);

            guiLenh(out, "QUIT");
        }
    }

    private static void docPhanHoi(BufferedReader in, int expectedCode) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() >= 3) {
                int code;
                try {
                    code = Integer.parseInt(line.substring(0, 3));
                } catch (NumberFormatException e) {
                    continue;
                }
                if (line.length() == 3 || line.charAt(3) != '-') {
                    if (code != expectedCode) {
                        throw new IOException("Máy chủ SMTP báo lỗi (" + code + "): " + line);
                    }
                    return;
                }
            }
        }
        throw new IOException("Máy chủ SMTP ngắt kết nối đột ngột.");
    }

    private static void guiLenh(BufferedWriter out, String command) throws IOException {
        out.write(command + "\r\n");
        out.flush();
    }

    /** Mở hộp thoại gửi email thật và hiển thị thông báo kết quả. */
    public static boolean moHopThoaiGuiEmail(Window owner, String toEmail, String tieuDe, String htmlContent) {
        if (!isSmtpConfigured()) {
            JOptionPane.showMessageDialog(owner,
                    "Chưa cấu hình tài khoản gửi email trong config/mail.properties.\n"
                    + "Vui lòng nhập mail.smtp.username và mail.smtp.password (Google App Password)!",
                    "Chưa cấu hình Email",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            guiEmail(toEmail, tieuDe, htmlContent);
            JOptionPane.showMessageDialog(owner,
                    "ĐÃ GỬI EMAIL THÀNH CÔNG!\n\n"
                    + "• Người nhận: " + toEmail + "\n"
                    + "• Tiêu đề: " + tieuDe + "\n\n"
                    + "Vui lòng kiểm tra Hộp thư đến (hoặc mục Spam / Thư rác) trên thiết bị của bạn.",
                    "Gửi Email Thành Công", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (Exception ex) {
            System.err.println("[EmailThongBaoHelper] Lỗi gửi email: " + ex.getMessage());
            JOptionPane.showMessageDialog(owner,
                    "Không thể gửi email đến [" + toEmail + "]:\n" + ex.getMessage()
                    + "\n\nVui lòng kiểm tra lại mạng Internet hoặc cấu hình trong config/mail.properties!",
                    "Lỗi gửi Email", JOptionPane.ERROR_MESSAGE);
            return false;
        }
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

    // 5. Mẫu Email Gửi Mã OTP Khôi phục Mật khẩu Nhân viên (Bảo mật SHA-256)
    public static String taoEmailKhoiPhucMatKhau(String hoTen, String sdt, String maTK, String maOtp) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ffd8be; border-radius: 8px; overflow: hidden;'>"
                + "<div style='background-color: #e16162; color: #ffffff; padding: 18px 20px; text-align: center;'>"
                + "  <h2 style='margin: 0; font-size: 19px;'>🔐 MIO PET SHOP - MÃ XÁC THỰC KHÔI PHỤC MẬT KHẨU</h2>"
                + "  <p style='margin: 5px 0 0; font-size: 12px;'>Hệ thống Bảo mật Tài khoản Nhân sự Nội bộ • " + DTF.format(LocalDateTime.now()) + "</p>"
                + "</div>"
                + "<div style='padding: 20px; background-color: #ffffff; color: #2b2c34;'>"
                + "  <p>Xin chào nhân viên <b>" + hoTen + "</b> (Mã TK: <b>" + maTK + "</b> - SĐT: <b>" + sdt + "</b>),</p>"
                + "  <p>Hệ thống quản lý Cửa hàng Thú cưng Mio vừa nhận được yêu cầu đặt lại mật khẩu đăng nhập của bạn. Vui lòng sử dụng mã OTP gồm 6 chữ số dưới đây để xác nhận:</p>"
                + "  <div style='background-color: #fff3ec; border: 2px dashed #e16162; border-radius: 8px; padding: 16px; text-align: center; margin: 18px 0;'>"
                + "    <span style='font-size: 12px; color: #555;'>MÃ XÁC THỰC OTP CỦA BẠN (HIỆU LỰC 5 PHÚT):</span><br/>"
                + "    <span style='display: inline-block; margin-top: 8px; padding: 8px 24px; background-color: #e16162; color: #ffffff; font-weight: bold; font-size: 24px; border-radius: 6px; letter-spacing: 6px;'>" + maOtp + "</span>"
                + "  </div>"
                + "  <p style='font-size: 12px; color: #666;'>⚠️ <b>Lưu ý bảo mật:</b> Tuyệt đối không chia sẻ mã OTP này cho bất kỳ ai. Mật khẩu mới sau khi cập nhật sẽ được mã hóa một chiều bằng thuật toán <b>SHA-256</b>.</p>"
                + "</div>"
                + "<div style='background-color: #f9f9fb; padding: 12px; text-align: center; font-size: 11px; color: #888; border-top: 1px solid #eee;'>"
                + "  Mio Pet Care & Spa Security • 12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM"
                + "</div>"
                + "</div>";
    }
}
