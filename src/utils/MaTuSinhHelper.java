package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiện ích khởi tạo mã tự sinh theo quy luật nghiệp vụ chuẩn cho hệ thống Mio Pet Shop.
 * Tất cả các mã đều tự động tăng dần từ CSDL, không cho phép chỉnh sửa thủ công:
 * - Mã Nhân Viên / Tài Khoản: NV{3 số tự tăng} (Ví dụ: NV001, NV002, ...)
 * - Mã Sản Phẩm: SP{3 số tự tăng} (Ví dụ: SP001, SP002, ...)
 * - Mã Dịch Vụ: DV{3 số tự tăng} (Ví dụ: DV001, DV002, ...)
 * - Mã Quy Đổi: QD{3 số tự tăng} (Ví dụ: QD001, QD002, ...)
 * - Mã Lô Hàng: LO-{yyMMdd}-{3 số tự tăng} (Ví dụ: LO-261107-001)
 * - Mã Giao Ca: GC{3 số tự tăng} (Ví dụ: GC001, GC002, ...)
 * - Mã Phân Công: PC{2 số tự tăng} (Ví dụ: PC01, PC02, ...)
 * - Mã Hóa Đơn: HD{yyMMdd}-{Mã NV 2 số}-{Số thứ tự 3 số} (Ví dụ: HD261107-01-001)
 * - Mã Chi Tiết HĐ: {Mã HĐ}_CT{seq} (Ví dụ: HD261107-01-001_CT1)
 * - Mã Phiếu Đổi Trả: DT{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: DT261107-001)
 * - Mã Lịch Hẹn: LH{yyMMdd}-{Số thứ tự 3 số} (Ví dụ: LH261107-001)
 * - Mã Khách Hàng: KH{3 chữ số tự tăng} (Ví dụ: KH006)
 * - Mã Thú Cưng: TC{3 chữ số tự tăng} (Ví dụ: TC006)
 * - Mã Hồ Sơ Thú Cưng: HS{3 chữ số tự tăng} (Ví dụ: HS006)
 * - Mã Hồ Sơ Sức Khỏe: SK{3 chữ số tự tăng} (Ví dụ: SK006)
 * - Mã Đơn Vị Tính: DVT{2 chữ số tự tăng} (Ví dụ: DVT03)
 * - Mã Thẻ Spa Pass: PASS-{SĐT}-{Số thứ tự 2 số} (Ví dụ: PASS-0988111222-01)
 *
 * Tích hợp bộ đệm an toàn (ConcurrentHashMap) và log ngoại lệ minh bạch,
 * chống phát sinh mã trùng lặp (NV001, SP001,...) khi đường truyền CSDL bị gián đoạn.
 */
public final class MaTuSinhHelper {

    private MaTuSinhHelper() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Map<String, Integer> MAX_CACHE = new ConcurrentHashMap<>();

    private static synchronized int laySoThuTuTiepTheo(String keyPrefix, String sql, int fallbackBase) {
        int max = MAX_CACHE.getOrDefault(keyPrefix, 0);
        boolean dbOk = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            dbOk = true;
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
        } catch (Exception e) {
            System.err.println("[MaTuSinhHelper] Cảnh báo đọc mã tự sinh [" + keyPrefix + "] từ CSDL: " + e.getMessage());
        }

        if (!dbOk && max == 0) {
            max = fallbackBase;
        }

        int next = max + 1;
        MAX_CACHE.put(keyPrefix, next);
        return next;
    }

    private static synchronized int laySoThuTuTheoNgay(String fullPrefix, String sql) {
        int max = MAX_CACHE.getOrDefault(fullPrefix, 0);
        boolean dbOk = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullPrefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                dbOk = true;
                while (rs.next()) {
                    String code = rs.getString(1);
                    if (code != null && code.startsWith(fullPrefix)) {
                        String tail = code.substring(fullPrefix.length()).replaceAll("[^0-9]", "");
                        if (!tail.isEmpty()) {
                            try {
                                int val = Integer.parseInt(tail);
                                if (val > max) max = val;
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[MaTuSinhHelper] Cảnh báo đọc mã tự sinh [" + fullPrefix + "] từ CSDL: " + e.getMessage());
        }

        if (!dbOk && max == 0) {
            max = 900;
        }

        int next = max + 1;
        MAX_CACHE.put(fullPrefix, next);
        return next;
    }

    /**
     * Sinh mã Nhân Viên & Tài Khoản tự tăng dạng NV001, NV002,...
     */
    public static synchronized String sinhMaNhanVien() {
        return String.format("NV%03d", laySoThuTuTiepTheo("NV", "SELECT maNhanVien FROM NhanVien WHERE maNhanVien LIKE 'NV%'", 900));
    }

    /**
     * Sinh mã Sản Phẩm tự tăng dạng SP001, SP002,...
     */
    public static synchronized String sinhMaSanPham() {
        return String.format("SP%03d", laySoThuTuTiepTheo("SP", "SELECT maSanPham FROM SanPham WHERE maSanPham LIKE 'SP%'", 900));
    }

    /**
     * Sinh mã Dịch Vụ tự tăng dạng DV001, DV002,...
     */
    public static synchronized String sinhMaDichVu() {
        return String.format("DV%03d", laySoThuTuTiepTheo("DV", "SELECT maDichVu FROM DichVu WHERE maDichVu LIKE 'DV%'", 900));
    }

    /**
     * Sinh mã Quy Đổi Đơn Vị Tính tự tăng dạng QD001, QD002,...
     */
    public static synchronized String sinhMaQuyDoi() {
        return String.format("QD%03d", laySoThuTuTiepTheo("QD", "SELECT maQuyDoi FROM QuyDoiDonVi WHERE maQuyDoi LIKE 'QD%'", 900));
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
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "SELECT soLo FROM LoHang WHERE soLo LIKE ?"));
    }

    /**
     * Sinh mã Bàn Giao Ca tự tăng dạng GC001, GC002,...
     */
    public static synchronized String sinhMaGiaoCa() {
        return String.format("GC%03d", laySoThuTuTiepTheo("GC", "SELECT maGiaoCa FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC%'", 900));
    }

    /**
     * Sinh mã Phân Công Ca tự tăng dạng PC01, PC02,...
     */
    public static synchronized String sinhMaPhanCong() {
        return String.format("PC%02d", laySoThuTuTiepTheo("PC", "SELECT maPhanCong FROM BangPhanCong", 900));
    }

    /**
     * Sinh mã Hóa Đơn theo quy luật: HD + yyMMdd + "-" + MãNV(2 số) + "-" + STT(3 số)
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
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "SELECT maHoaDon FROM HoaDon WHERE maHoaDon LIKE ?"));
    }

    /**
     * Sinh mã Phiếu Đổi Trả theo quy luật: DT + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaPhieuDoiTra() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "DT" + today + "-";
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "SELECT maPhieuDoiTra FROM PhieuDoiTra WHERE maPhieuDoiTra LIKE ?"));
    }

    /**
     * Sinh mã Lịch Hẹn theo quy luật: LH + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaLichHen() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "LH" + today + "-";
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "SELECT maLichHen FROM LichHen WHERE maLichHen LIKE ?"));
    }

    /**
     * Sinh mã Khách Hàng mới theo quy luật tự tăng: KH + 3 chữ số (ví dụ KH006)
     */
    public static synchronized String sinhMaKhachHang() {
        return String.format("KH%03d", laySoThuTuTiepTheo("KH", "SELECT maKhachHang FROM KhachHang WHERE maKhachHang LIKE 'KH%'", 900));
    }

    /**
     * Sinh mã Thú Cưng mới theo quy luật tự tăng: TC + 3 chữ số (ví dụ TC006)
     */
    public static synchronized String sinhMaThuCung() {
        return String.format("TC%03d", laySoThuTuTiepTheo("TC", "SELECT maThuCung FROM ThuCung WHERE maThuCung LIKE 'TC%'", 900));
    }

    /**
     * Sinh mã Hồ Sơ Thú Cưng tự tăng dạng HS001, HS002,...
     */
    public static synchronized String sinhMaHoSo() {
        return String.format("HS%03d", laySoThuTuTiepTheo("HS", "SELECT maHoSo FROM HoSoThuCung WHERE maHoSo LIKE 'HS%'", 900));
    }

    /**
     * Sinh mã Hồ Sơ Sức Khỏe tự tăng dạng SK001, SK002,...
     */
    public static synchronized String sinhMaSucKhoe() {
        return String.format("SK%03d", laySoThuTuTiepTheo("SK", "SELECT maSucKhoe FROM HoSoSucKhoe WHERE maSucKhoe LIKE 'SK%'", 900));
    }

    /**
     * Sinh mã Đơn Vị Tính tự tăng dạng DVT01, DVT02,...
     */
    public static synchronized String sinhMaDonViTinh() {
        return String.format("DVT%02d", laySoThuTuTiepTheo("DVT", "SELECT maDonVi FROM DonViTinh WHERE maDonVi LIKE 'DV%'", 900));
    }

    /**
     * Sinh mã Thẻ Spa Pass: PASS-{SĐT}-{2 số thứ tự thẻ của khách}
     */
    public static synchronized String sinhMaTheSpaPass(String sdt) {
        String cleanSdt = (sdt != null && !sdt.trim().isEmpty()) ? sdt.trim() : "0988111222";
        String key = "PASS_" + cleanSdt;
        int seq = MAX_CACHE.getOrDefault(key, 0);
        String sql = "SELECT COUNT(*) FROM TheSpaPass WHERE sdtKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanSdt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seq = Math.max(seq, rs.getInt(1));
                }
            }
        } catch (Exception e) {
            System.err.println("[MaTuSinhHelper] Cảnh báo đọc mã Thẻ Spa Pass CSDL: " + e.getMessage());
        }
        seq++;
        MAX_CACHE.put(key, seq);
        return String.format("PASS-%s-%02d", cleanSdt, seq);
    }
}
