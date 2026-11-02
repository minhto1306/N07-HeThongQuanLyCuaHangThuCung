package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích khởi tạo mã tự sinh theo quy luật nghiệp vụ chuẩn cho hệ thống Mio Pet Shop.
 * - Mã Hóa Đơn: HD{yyMMdd}-{Mã NV 2 số}-{Số thứ tự 3 số} (Ví dụ: HD261029-01-001, max 15 ký tự <= 20 ký tự)
 * - Mã Chi Tiết HĐ: {Mã HĐ}_CT{seq} (Ví dụ: HD261029-01-001_CT1 <= 19 ký tự <= 20 ký tự)
 * - Mã Phiếu Đổi Trả: DT{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: DT261029-001)
 * - Mã Lịch Hẹn: LH{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: LH261029-001)
 * - Mã Khách Hàng: KH{4 chữ số tự tăng} (Ví dụ: KH0006)
 * - Mã Thú Cưng: TC{4 chữ số tự tăng} (Ví dụ: TC0005)
 * - Mã Thẻ Spa Pass: PASS-{SĐT}-{Số thứ tự 2 số} (Ví dụ: PASS-0988111222-01)
 */
public final class MaTuSinhHelper {

    private MaTuSinhHelper() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * Sinh mã Hóa Đơn theo quy luật: HD + yyMMdd + "-" + MãNV(2 số) + "-" + STT(3 số)
     * Đảm bảo tổng độ dài luôn <= 15 ký tự (phù hợp với bảng HoaDon và ChiTietHoaDon NVARCHAR(20)).
     */
    // sinh mã hóa đơn kiểu: HD + ngày + mã nv + số thứ tự
    public static synchronized String sinhMaHoaDon(String maNhanVien) {
        String today = LocalDate.now().format(DATE_FMT);

        // lấy 2 số cuối của mã nv
        String nvPart = "00";
        if (maNhanVien != null && !maNhanVien.trim().isEmpty()) {
            String digits = maNhanVien.replaceAll("[^0-9]", "");
            if (digits.length() >= 2) {
                nvPart = digits.substring(digits.length() - 2);
            } else if (digits.length() == 1) {
                nvPart = "0" + digits;
            }
        }

        String prefix = "HD" + today + "-" + nvPart + "-";
        int seq = 1;

        // đếm xem hôm nay nv này bán dc mấy đơn rồi để tăng stt lên
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHoaDon LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seq = rs.getInt(1) + 1;
                }
            }
        } catch (Exception e) {
            // lỡ lỗi thì random đại 1 số tránh trùng
            seq = (int) (System.currentTimeMillis() % 900) + 100;
        }

        return String.format("%s%03d", prefix, seq);
    }

    /**
     * Sinh mã Phiếu Đổi Trả theo quy luật: DT + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaPhieuDoiTra() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "DT" + today + "-";
        int seq = 1;
        String sql = "SELECT COUNT(*) FROM PhieuDoiTra WHERE maPhieuDoiTra LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seq = rs.getInt(1) + 1;
                }
            }
        } catch (Exception e) {
            seq = (int) (System.currentTimeMillis() % 900) + 100;
        }
        return String.format("%s%03d", prefix, seq);
    }

    /**
     * Sinh mã Lịch Hẹn theo quy luật: LH + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaLichHen() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "LH" + today + "-";
        int seq = 1;
        String sql = "SELECT COUNT(*) FROM LichHen WHERE maLichHen LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seq = rs.getInt(1) + 1;
                }
            }
        } catch (Exception e) {
            seq = (int) (System.currentTimeMillis() % 900) + 100;
        }
        return String.format("%s%03d", prefix, seq);
    }

    /**
     * Sinh mã Khách Hàng mới theo quy luật tự tăng: KH + 4 chữ số (ví dụ KH0006)
     */
    // sinh mã khách hàng tự tăng KH0001
    public static synchronized String sinhMaKhachHang() {
        int max = 0;
        String sql = "SELECT maKhachHang FROM KhachHang WHERE maKhachHang LIKE 'KH%'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString(1);
                if (code != null) {
                    String num = code.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) {
                        try {
                            int val = Integer.parseInt(num);
                            if (val > max) max = val;
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.format("KH%04d", max + 1);
    }

    /**
     * Sinh mã Thú Cưng mới theo quy luật tự tăng: TC + 4 chữ số (ví dụ TC0005)
     */
    public static synchronized String sinhMaThuCung() {
        int max = 0;
        String sql = "SELECT maThuCung FROM ThuCung WHERE maThuCung LIKE 'TC%'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String code = rs.getString(1);
                if (code != null) {
                    String num = code.replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) {
                        try {
                            int val = Integer.parseInt(num);
                            if (val > max) max = val;
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.format("TC%04d", max + 1);
    }

    /**
     * Sinh mã Thẻ Spa Pass: PASS-{SĐT}-{2 số thứ tự thẻ của khách}
     */
    public static synchronized String sinhMaTheSpaPass(String sdt) {
        String cleanSdt = (sdt != null && !sdt.trim().isEmpty()) ? sdt.trim() : "0988111222";
        int seq = 1;
        String sql = "SELECT COUNT(*) FROM TheSpaPass WHERE sdtKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanSdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seq = rs.getInt(1) + 1;
                }
            }
        } catch (Exception ignored) {}
        return String.format("PASS-%s-%02d", cleanSdt, seq);
    }
}
