package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích khởi tạo mã tự sinh theo quy luật nghiệp vụ chuẩn cho hệ thống Mio Pet Shop.
 * Tất cả các mã đều tự động tăng dần từ CSDL, không cho phép chỉnh sửa thủ công:
 * - Mã Nhân Viên / Tài Khoản: NV{3 số tự tăng} (Ví dụ: NV001, NV002, ...)
 * - Mã Sản Phẩm: SP{3 số tự tăng} (Ví dụ: SP001, SP002, ...)
 * - Mã Dịch Vụ: DV{3 số tự tăng} (Ví dụ: DV001, DV002, ...)
 * - Mã Quy Đổi: QD{3 số tự tăng} (Ví dụ: QD001, QD002, ...)
 * - Mã Lô Hàng: LO-{yyMMdd}-{3 số tự tăng} (Ví dụ: LO-261107-001)
 * - Mã Giao Ca: GC{3 số tự tăng} (Ví dụ: GC001, GC002, ...)
 * - Mã Hóa Đơn: HD{yyMMdd}-{Mã NV 2 số}-{Số thứ tự 3 số} (Ví dụ: HD261107-01-001)
 * - Mã Chi Tiết HĐ: {Mã HĐ}_CT{seq} (Ví dụ: HD261107-01-001_CT1)
 * - Mã Phiếu Đổi Trả: DT{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: DT261107-001)
 * - Mã Lịch Hẹn: LH{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: LH261107-001)
 * - Mã Khách Hàng: KH{4 chữ số tự tăng} (Ví dụ: KH0006)
 * - Mã Thú Cưng: TC{4 chữ số tự tăng} (Ví dụ: TC0005)
 * - Mã Thẻ Spa Pass: PASS-{SĐT}-{Số thứ tự 2 số} (Ví dụ: PASS-0988111222-01)
 */
public final class MaTuSinhHelper {

    private MaTuSinhHelper() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");

    /**
     * Sinh mã Nhân Viên & Tài Khoản tự tăng dạng NV001, NV002,...
     */
    public static synchronized String sinhMaNhanVien() {
        int max = 0;
        String sql = "SELECT maNhanVien FROM NhanVien WHERE maNhanVien LIKE 'NV%'";
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
        return String.format("NV%03d", max + 1);
    }

    /**
     * Sinh mã Sản Phẩm tự tăng dạng SP001, SP002,...
     */
    public static synchronized String sinhMaSanPham() {
        int max = 0;
        String sql = "SELECT maSanPham FROM SanPham WHERE maSanPham LIKE 'SP%'";
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
        return String.format("SP%03d", max + 1);
    }

    /**
     * Sinh mã Dịch Vụ tự tăng dạng DV001, DV002,...
     */
    public static synchronized String sinhMaDichVu() {
        int max = 0;
        String sql = "SELECT maDichVu FROM DichVu WHERE maDichVu LIKE 'DV%'";
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
        return String.format("DV%03d", max + 1);
    }

    /**
     * Sinh mã Quy Đổi Đơn Vị Tính tự tăng dạng QD001, QD002,...
     */
    public static synchronized String sinhMaQuyDoi() {
        int max = 0;
        String sql = "SELECT maQuyDoi FROM QuyDoiDonVi WHERE maQuyDoi LIKE 'QD%'";
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
        return String.format("QD%03d", max + 1);
    }

    /**
     * Sinh số lô hàng tự tăng theo ngày dạng LO-yyMMdd-001,...
     */
    public static synchronized String sinhSoLo() {
        return sinhSoLo("SP001");
    }

    public static synchronized String sinhSoLo(String maSP) {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "LO-" + today + "-";
        int max = 0;
        String sql = "SELECT soLo FROM LoHang WHERE soLo LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    if (code != null && code.startsWith(prefix)) {
                        String tail = code.substring(prefix.length()).replaceAll("[^0-9]", "");
                        if (!tail.isEmpty()) {
                            int val = Integer.parseInt(tail);
                            if (val > max) max = val;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.format("%s%03d", prefix, max + 1);
    }

    /**
     * Sinh mã Bàn Giao Ca tự tăng dạng GC001, GC002,...
     */
    public static synchronized String sinhMaGiaoCa() {
        int max = 0;
        String sql = "SELECT maGiaoCa FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC%'";
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
        return String.format("GC%03d", max + 1);
    }

    /**
     * Sinh mã Phân Công Ca tự tăng dạng PC01, PC02,...
     */
    public static synchronized String sinhMaPhanCong() {
        int max = 0;
        String sql = "SELECT maPhanCong FROM BangPhanCong";
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
        return String.format("PC%02d", max + 1);
    }

    /**
     * Sinh mã Hóa Đơn theo quy luật: HD + yyMMdd + "-" + MãNV(2 số) + "-" + STT(3 số)
     * Đảm bảo tổng độ dài luôn <= 15 ký tự (phù hợp với bảng HoaDon và ChiTietHoaDon NVARCHAR(20)).
     */
    public static synchronized String sinhMaHoaDon(String maNhanVien) {
        String today = LocalDate.now().format(DATE_FMT);

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
        int max = 0;

        String sql = "SELECT maHoaDon FROM HoaDon WHERE maHoaDon LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    if (code != null && code.startsWith(prefix)) {
                        String tail = code.substring(prefix.length()).replaceAll("[^0-9]", "");
                        if (!tail.isEmpty()) {
                            int val = Integer.parseInt(tail);
                            if (val > max) max = val;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return String.format("%s%03d", prefix, max + 1);
    }

    /**
     * Sinh mã Phiếu Đổi Trả theo quy luật: DT + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaPhieuDoiTra() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "DT" + today + "-";
        int max = 0;
        String sql = "SELECT maPhieuDoiTra FROM PhieuDoiTra WHERE maPhieuDoiTra LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    if (code != null && code.startsWith(prefix)) {
                        String tail = code.substring(prefix.length()).replaceAll("[^0-9]", "");
                        if (!tail.isEmpty()) {
                            int val = Integer.parseInt(tail);
                            if (val > max) max = val;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.format("%s%03d", prefix, max + 1);
    }

    /**
     * Sinh mã Lịch Hẹn theo quy luật: LH + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaLichHen() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "LH" + today + "-";
        int max = 0;
        String sql = "SELECT maLichHen FROM LichHen WHERE maLichHen LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    if (code != null && code.startsWith(prefix)) {
                        String tail = code.substring(prefix.length()).replaceAll("[^0-9]", "");
                        if (!tail.isEmpty()) {
                            int val = Integer.parseInt(tail);
                            if (val > max) max = val;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return String.format("%s%03d", prefix, max + 1);
    }

    /**
     * Sinh mã Khách Hàng mới theo quy luật tự tăng: KH + 3 chữ số (ví dụ KH006)
     */
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
        return String.format("KH%03d", max + 1);
    }

    /**
     * Sinh mã Thú Cưng mới theo quy luật tự tăng: TC + 3 chữ số (ví dụ TC006)
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
        return String.format("TC%03d", max + 1);
    }

    /**
     * Sinh mã Hồ Sơ Thú Cưng tự tăng dạng HS001, HS002,...
     */
    public static synchronized String sinhMaHoSo() {
        int max = 0;
        String sql = "SELECT maHoSo FROM HoSoThuCung WHERE maHoSo LIKE 'HS%'";
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
        return String.format("HS%03d", max + 1);
    }

    /**
     * Sinh mã Hồ Sơ Sức Khỏe tự tăng dạng SK001, SK002,...
     */
    public static synchronized String sinhMaSucKhoe() {
        int max = 0;
        String sql = "SELECT maSucKhoe FROM HoSoSucKhoe WHERE maSucKhoe LIKE 'SK%'";
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
        return String.format("SK%03d", max + 1);
    }

    /**
     * Sinh mã Đơn Vị Tính tự tăng dạng DVT01, DVT02,...
     */
    public static synchronized String sinhMaDonViTinh() {
        int max = 0;
        String sql = "SELECT maDonVi FROM DonViTinh WHERE maDonVi LIKE 'DV%'";
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
        return String.format("DVT%02d", max + 1);
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

