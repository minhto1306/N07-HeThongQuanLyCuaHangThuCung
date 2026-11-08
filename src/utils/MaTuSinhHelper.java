package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
 * Cấp phát số thứ tự qua transaction SQL dùng chung giữa các tiến trình.
 * Mã đã cấp không tái sử dụng; giao dịch bị hủy có thể để lại khoảng trống.
 */
public final class MaTuSinhHelper {

    private MaTuSinhHelper() {}

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Map<String, Integer> MAX_CACHE = new ConcurrentHashMap<>();

    private static int reserveNextSequence(String prefixKey, String tableName, String columnName, String codePrefix) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            Connection conn = null;
            try {
                conn = DBConnection.getConnection();
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

                String selectSeqSql = "SELECT giaTri FROM dbo.MaTuSinhSequence WITH (UPDLOCK, HOLDLOCK) WHERE prefix = ?";
                Long currentVal = null;
                try (PreparedStatement psSeq = conn.prepareStatement(selectSeqSql)) {
                    psSeq.setString(1, prefixKey);
                    try (ResultSet rsSeq = psSeq.executeQuery()) {
                        if (rsSeq.next()) {
                            currentVal = rsSeq.getLong(1);
                        }
                    }
                }

                long next;
                if (currentVal != null) {
                    if (currentVal >= Integer.MAX_VALUE) throw new IllegalStateException("Đã hết miền số thứ tự mã " + prefixKey);
                    next = currentVal + 1;
                    String updateSql = "UPDATE dbo.MaTuSinhSequence SET giaTri = ? WHERE prefix = ?";
                    try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                        psUpd.setLong(1, next);
                        psUpd.setString(2, prefixKey);
                        psUpd.executeUpdate();
                    }
                } else {
                    long initMax = 0;
                    String maxSql = "SELECT MAX(TRY_CONVERT(BIGINT, SUBSTRING(" + columnName + ", ?, 20))) "
                            + "FROM " + tableName + " WHERE " + columnName + " LIKE ?";
                    try (PreparedStatement psMax = conn.prepareStatement(maxSql)) {
                        psMax.setInt(1, codePrefix.length() + 1);
                        psMax.setString(2, codePrefix + "%");
                        try (ResultSet rsMax = psMax.executeQuery()) {
                            if (rsMax.next()) {
                                initMax = Math.max(0, rsMax.getLong(1));
                            }
                        }
                    }
                    if (initMax >= Integer.MAX_VALUE) throw new IllegalStateException("Đã hết miền số thứ tự mã " + prefixKey);
                    next = initMax + 1;
                    String insertSql = "INSERT INTO dbo.MaTuSinhSequence (prefix, giaTri) VALUES (?, ?)";
                    try (PreparedStatement psIns = conn.prepareStatement(insertSql)) {
                        psIns.setString(1, prefixKey);
                        psIns.setLong(2, next);
                        psIns.executeUpdate();
                    }
                }

                if (next > Integer.MAX_VALUE) {
                    throw new IllegalStateException("Tràn số thứ tự mã tự sinh vượt quá Integer.MAX_VALUE: " + next);
                }

                conn.commit();
                return (int) next;
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (Exception ignored) {}
                }
                if (e.getErrorCode() == 208 || "42S02".equals(e.getSQLState())) {
                    throw new IllegalStateException("Bảng dbo.MaTuSinhSequence chưa tồn tại. Vui lòng thực thi migration sql/20261108_MaTuSinhSequence.sql", e);
                }
                boolean isDeadlock = (e.getErrorCode() == 1205 || "40001".equals(e.getSQLState()));
                if (isDeadlock && attempt < maxRetries) {
                    try {
                        Thread.sleep(20L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Đã hủy chờ cấp mã", ie);
                    }
                    continue;
                }
                throw new IllegalStateException("Không thể sinh mã " + prefixKey + " qua CSDL: " + e.getMessage(), e);
            } catch (RuntimeException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException rollbackEx) { e.addSuppressed(rollbackEx); }
                }
                throw e;
            } finally {
                if (conn != null) {
                    try { conn.close(); } catch (Exception ignored) {}
                }
            }
        }
        throw new IllegalStateException("Không thể sinh mã " + prefixKey + " sau " + maxRetries + " lần thử do xung đột");
    }

    private static synchronized int laySoThuTuTiepTheo(String keyPrefix, String tableName,
                                                        String columnName, String codePrefix) {
        return reserveNextSequence(keyPrefix, tableName, columnName, codePrefix);
    }

    private static synchronized int laySoThuTuTheoNgay(String fullPrefix, String tableName, String columnName) {
        return reserveNextSequence(fullPrefix, tableName, columnName, fullPrefix);
    }

    /**
     * Sinh mã Nhân Viên & Tài Khoản tự tăng dạng NV001, NV002,...
     */
    public static synchronized String sinhMaNhanVien() {
        return String.format("NV%03d", laySoThuTuTiepTheo("NV", "NhanVien", "maNhanVien", "NV"));
    }

    /**
     * Sinh mã Sản Phẩm tự tăng dạng SP001, SP002,...
     */
    public static synchronized String sinhMaSanPham() {
        return String.format("SP%03d", laySoThuTuTiepTheo("SP", "SanPham", "maSanPham", "SP"));
    }

    /**
     * Sinh mã Dịch Vụ tự tăng dạng DV001, DV002,...
     */
    public static synchronized String sinhMaDichVu() {
        return String.format("DV%03d", laySoThuTuTiepTheo("DV", "DichVu", "maDichVu", "DV"));
    }

    /**
     * Sinh mã Quy Đổi Đơn Vị Tính tự tăng dạng QD001, QD002,...
     */
    public static synchronized String sinhMaQuyDoi() {
        return String.format("QD%03d", laySoThuTuTiepTheo("QD", "QuyDoiDonVi", "maQuyDoi", "QD"));
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
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "LoHang", "soLo"));
    }

    /**
     * Sinh mã Bàn Giao Ca tự tăng dạng GC001, GC002,...
     */
    public static synchronized String sinhMaGiaoCa() {
        return String.format("GC%03d", laySoThuTuTiepTheo("GC", "BangGiaoCa", "maGiaoCa", "GC"));
    }

    /**
     * Sinh mã Phân Công Ca tự tăng dạng PC01, PC02,...
     */
    public static synchronized String sinhMaPhanCong() {
        return String.format("PC%02d", MAX_CACHE.merge("PC", 1, Integer::sum));
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
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "HoaDon", "maHoaDon"));
    }

    /**
     * Sinh mã Phiếu Đổi Trả theo quy luật: DT + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaPhieuDoiTra() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "DT" + today + "-";
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "PhieuDoiTra", "maPhieuDoiTra"));
    }

    /**
     * Sinh mã Lịch Hẹn theo quy luật: LH + yyMMdd + "-" + STT(3 số)
     */
    public static synchronized String sinhMaLichHen() {
        String today = LocalDate.now().format(DATE_FMT);
        String prefix = "LH" + today + "-";
        return String.format("%s%03d", prefix, laySoThuTuTheoNgay(prefix, "LichHen", "maLichHen"));
    }

    /**
     * Sinh mã Khách Hàng mới theo quy luật tự tăng: KH + 3 chữ số (ví dụ KH006)
     */
    public static synchronized String sinhMaKhachHang() {
        return String.format("KH%03d", laySoThuTuTiepTheo("KH", "KhachHang", "maKhachHang", "KH"));
    }

    /**
     * Sinh mã Thú Cưng mới theo quy luật tự tăng: TC + 3 chữ số (ví dụ TC006)
     */
    public static synchronized String sinhMaThuCung() {
        return String.format("TC%03d", laySoThuTuTiepTheo("TC", "ThuCung", "maThuCung", "TC"));
    }

    /**
     * Sinh mã Hồ Sơ Thú Cưng tự tăng dạng HS001, HS002,...
     */
    public static synchronized String sinhMaHoSo() {
        return String.format("HS%03d", laySoThuTuTiepTheo("HS", "HoSoThuCung", "maHoSo", "HS"));
    }

    /**
     * Sinh mã Hồ Sơ Sức Khỏe tự tăng dạng SK001, SK002,...
     */
    public static synchronized String sinhMaSucKhoe() {
        return String.format("SK%03d", laySoThuTuTiepTheo("SK", "HoSoSucKhoe", "maSucKhoe", "SK"));
    }

    /**
     * Sinh mã Đơn Vị Tính tự tăng dạng DVT01, DVT02,...
     */
    public static synchronized String sinhMaDonViTinh() {
        return String.format("DVT%02d", laySoThuTuTiepTheo("DVT", "DonViTinh", "maDonVi", "DVT"));
    }

    /**
     * Sinh mã Thẻ Spa Pass: PASS-{SĐT}-{2 số thứ tự thẻ của khách}
     */
    public static synchronized String sinhMaTheSpaPass(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại khách hàng là bắt buộc để sinh mã Spa Pass");
        }
        String cleanSdt = sdt.trim();
        if (!cleanSdt.matches("[0-9]{8,15}")) throw new IllegalArgumentException("Số điện thoại Spa Pass phải gồm 8 đến 15 chữ số");
        String prefix = "PASS-" + cleanSdt + "-";
        int seq = reserveNextSequence(prefix, "TheSpaPass", "maThe", prefix);
        return String.format("%s%02d", prefix, seq);
    }
}
