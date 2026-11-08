package dao;

import entity.BangGiaoCa;
import entity.CaLamViec;
import entity.NhanVien;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Quản lý Ca làm việc & Biên bản Bàn giao ca (BangGiaoCa).
 * Cung cấp đầy đủ các thao tác CRUD và kiểm soát đối soát quỹ tiền mặt.
 */
public class BangGiaoCaDAO {

    /**
     * Mở ca làm việc mới khi nhân viên bắt đầu ca trực
     */
    public boolean moCaLamViec(BangGiaoCa giaoCa) throws SQLException {
        return insertBangGiaoCa(giaoCa);
    }

    public boolean insertBangGiaoCa(BangGiaoCa giaoCa) throws SQLException {
        if (giaoCa == null || giaoCa.getNhanVien() == null
                || giaoCa.getNhanVien().getMaNhanVien() == null
                || giaoCa.getCaLamViec() == null
                || giaoCa.getCaLamViec().getMaCa() == null) {
            throw new IllegalArgumentException("Bàn giao ca phải xác định nhân viên và ca làm việc");
        }
        String sql = "INSERT INTO BangGiaoCa (maGiaoCa, maNhanVien, maCa, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe, tienChenhLech, " +
                     "tienThuTienMat, tienChuyenKhoan, tienHoanTra, lyDoChenhLech, gioBatDauCa, gioKetThucThucTe, trangThai, maQuanLyDuyet, thoiGianDuyet) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giaoCa.getMaGiaoCa());
            ps.setString(2, giaoCa.getNhanVien().getMaNhanVien());
            ps.setString(3, giaoCa.getCaLamViec().getMaCa());
            ps.setBigDecimal(4, giaoCa.getTienDauCa());
            ps.setBigDecimal(5, giaoCa.getTienCuoiCaHeThong());
            ps.setBigDecimal(6, giaoCa.getTienCuoiCaThucTe());
            ps.setBigDecimal(7, giaoCa.getTienChenhLech());
            ps.setBigDecimal(8, giaoCa.getTienThuTienMat());
            ps.setBigDecimal(9, giaoCa.getTienChuyenKhoan());
            ps.setBigDecimal(10, giaoCa.getTienHoanTra());
            ps.setString(11, giaoCa.getLyDoChenhLech());
            if (giaoCa.getGioBatDauCa() != null) ps.setTimestamp(12, Timestamp.valueOf(giaoCa.getGioBatDauCa()));
            else ps.setNull(12, Types.TIMESTAMP);

            if (giaoCa.getGioKetThucThucTe() != null) {
                ps.setTimestamp(13, Timestamp.valueOf(giaoCa.getGioKetThucThucTe()));
            } else {
                ps.setNull(13, Types.TIMESTAMP);
            }
            ps.setString(14, giaoCa.getTrangThai() != null ? giaoCa.getTrangThai() : "CHO_DUYET");
            if (giaoCa.getQuanLyDuyet() != null) ps.setString(15, giaoCa.getQuanLyDuyet().getMaNhanVien());
            else ps.setNull(15, Types.NVARCHAR);
            if (giaoCa.getThoiGianDuyet() != null) ps.setTimestamp(16, Timestamp.valueOf(giaoCa.getThoiGianDuyet()));
            else ps.setNull(16, Types.TIMESTAMP);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Chốt kết ca và ghi nhận đối soát chênh lệch tiền mặt
     */
    public boolean chotKetCa(BangGiaoCa giaoCa) throws SQLException {
        String sql = "UPDATE BangGiaoCa SET tienCuoiCaHeThong=?, tienCuoiCaThucTe=?, tienChenhLech=?, tienThuTienMat=?, " +
                     "tienChuyenKhoan=?, tienHoanTra=?, lyDoChenhLech=?, gioKetThucThucTe=?, trangThai=? " +
                     "WHERE maGiaoCa=? AND trangThai='DANG_MO' AND gioKetThucThucTe IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, giaoCa.getTienCuoiCaHeThong());
            ps.setBigDecimal(2, giaoCa.getTienCuoiCaThucTe());
            ps.setBigDecimal(3, giaoCa.getTienChenhLech());
            ps.setBigDecimal(4, giaoCa.getTienThuTienMat());
            ps.setBigDecimal(5, giaoCa.getTienChuyenKhoan());
            ps.setBigDecimal(6, giaoCa.getTienHoanTra());
            ps.setString(7, giaoCa.getLyDoChenhLech());
            ps.setTimestamp(8, Timestamp.valueOf(giaoCa.getGioKetThucThucTe() != null ? giaoCa.getGioKetThucThucTe() : LocalDateTime.now()));
            ps.setString(9, giaoCa.getTrangThai() != null ? giaoCa.getTrangThai() : "CHO_DUYET");
            ps.setString(10, giaoCa.getMaGiaoCa());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Lấy toàn bộ danh sách biên bản bàn giao ca
     */
    public List<BangGiaoCa> getAllBangGiaoCa() throws SQLException {
        List<BangGiaoCa> list = new ArrayList<>();
        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +
                     "ORDER BY bgc.gioKetThucThucTe DESC, bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("tenNhanVien"));

                CaLamViec ca = new CaLamViec();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));

                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");
                LocalDateTime gioKT = (kt != null) ? kt.toLocalDateTime() : null;

                BangGiaoCa bgc = new BangGiaoCa(
                        rs.getString("maGiaoCa"),
                        nv,
                        ca,
                        rs.getBigDecimal("tienDauCa"),
                        rs.getBigDecimal("tienCuoiCaHeThong"),
                        rs.getBigDecimal("tienCuoiCaThucTe"),
                        rs.getBigDecimal("tienChenhLech"),
                        rs.getString("lyDoChenhLech"),
                        gioKT
                );
                boSungThongTinK80(bgc, rs);
                list.add(bgc);
            }
        }
        return list;
    }

    /**
     * Lấy số tiền mặt thực tế của ca vừa bàn giao gần nhất để làm tiền đầu ca
     * kế tiếp. Chỉ các ca đã chốt (có giờ kết thúc) mới được sử dụng.
     *
     * @return số tiền bàn giao gần nhất; null nếu chưa từng có ca nào được chốt
     */
    public Long getTienThucTeCaBanGiaoGanNhat() throws SQLException {
        BangGiaoCa caGanNhat = getCaBanGiaoGanNhat();
        return caGanNhat != null && caGanNhat.getTienCuoiCaThucTe() != null
                ? caGanNhat.getTienCuoiCaThucTe().longValue()
                : null;
    }

    /**
     * Lấy ca đã chốt gần nhất để xác định cả số tiền bàn giao và mốc bắt đầu
     * tính doanh thu cho ca kế tiếp.
     */
    public BangGiaoCa getCaBanGiaoGanNhat() throws SQLException {
        String sql = "SELECT TOP 1 bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +
                     "FROM BangGiaoCa " +
                     "bgc LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +
                     "WHERE bgc.gioKetThucThucTe IS NOT NULL " +
                     "ORDER BY bgc.gioKetThucThucTe DESC, bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("tenNhanVien"));

                CaLamViec ca = new CaLamViec();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));

                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");
                BangGiaoCa bgc = new BangGiaoCa(
                        rs.getString("maGiaoCa"), nv, ca,
                        rs.getBigDecimal("tienDauCa"),
                        rs.getBigDecimal("tienCuoiCaHeThong"),
                        rs.getBigDecimal("tienCuoiCaThucTe"),
                        rs.getBigDecimal("tienChenhLech"),
                        rs.getString("lyDoChenhLech"),
                        kt != null ? kt.toLocalDateTime() : null
                );
                boSungThongTinK80(bgc, rs);
                return bgc;
            }
        }
        return null;
    }

    /** Tổng hóa đơn tiền mặt phát sinh sau lần bàn giao gần nhất. */
    public long tinhDoanhThuTienMatTuThoiDiem(LocalDateTime tuThoiDiem) {
        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongTienMat FROM HoaDon " +
                     "WHERE thoiGianThanhToan > ? " +
                     "AND trangThaiHoaDon = N'Đã thanh toán' " +
                     "AND phuongThucThanhToan = N'Tiền mặt'";
        return tinhTongTheoThoiDiem(sql, "tongTienMat", tuThoiDiem, "doanh thu tiền mặt theo ca");
    }

    /** Tổng hóa đơn chuyển khoản phát sinh sau lần bàn giao gần nhất (chỉ tham khảo). */
    public long tinhDoanhThuChuyenKhoanTuThoiDiem(LocalDateTime tuThoiDiem) {
        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongCK FROM HoaDon " +
                     "WHERE thoiGianThanhToan > ? " +
                     "AND trangThaiHoaDon = N'Đã thanh toán' " +
                     "AND phuongThucThanhToan IS NOT NULL " +
                     "AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%'";
        return tinhTongTheoThoiDiem(sql, "tongCK", tuThoiDiem, "doanh thu chuyển khoản theo ca");
    }

    /** Tổng tiền đổi/trả phát sinh sau lần bàn giao gần nhất. */
    public long tinhTongTienHoanTraTuThoiDiem(LocalDateTime tuThoiDiem) {
        if (!hasPhuongThucHoanColumn()) {
            throw new IllegalStateException("CSDL chưa có dữ liệu tiền hoàn thực tế Phase 1/2");
        }
        String sql = "SELECT ISNULL(SUM(tienHoanThucTe), 0) AS tongHoan FROM PhieuDoiTra " +
                     "WHERE ngayDoiTra > ?";
        return tinhTongTheoThoiDiem(sql, "tongHoan", tuThoiDiem, "tiền hoàn trả theo ca");
    }

    public long tinhDoanhThuTrongCa(LocalDateTime batDau, LocalDateTime ketThuc,
                                    String maNhanVien, boolean tienMat) {
        String dieuKienThanhToan = tienMat
                ? "(phuongThucThanhToan LIKE N'%Tiền mặt%' OR phuongThucThanhToan IS NULL)"
                : "phuongThucThanhToan IS NOT NULL AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%'";
        String sql = "SELECT ISNULL(SUM(thanhTien),0) tong FROM HoaDon "
                + "WHERE thoiGianThanhToan>=? AND thoiGianThanhToan<=? AND maNhanVien=? "
                + "AND trangThaiHoaDon=N'Đã thanh toán' AND " + dieuKienThanhToan;
        return tinhTongTrongKhoang(sql, batDau, ketThuc, maNhanVien);
    }

    private static Boolean hasNewDoiTraCols = null;

    private static synchronized boolean hasPhuongThucHoanColumn() {
        if (hasNewDoiTraCols != null) return hasNewDoiTraCols;
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='PhieuDoiTra' AND COLUMN_NAME='phuongThucHoanTien'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            hasNewDoiTraCols = rs.next();
        } catch (Exception e) {
            hasNewDoiTraCols = false;
        }
        return hasNewDoiTraCols != null && hasNewDoiTraCols;
    }

    public long tinhHoanTraTrongCa(LocalDateTime batDau, LocalDateTime ketThuc, String maNhanVien) {
        if (!hasPhuongThucHoanColumn()) {
            throw new IllegalStateException("CSDL chưa có cột phương thức hoàn tiền Phase 1/2; "
                    + "không thể suy đoán toàn bộ tiền hoàn là tiền mặt. Hãy dùng sql/HeThongQuanLyThuCung.sql.");
        }
        String sql = "SELECT ISNULL(SUM(tienHoanThucTe), 0) tong FROM PhieuDoiTra "
                + "WHERE ngayDoiTra>=? AND ngayDoiTra<=? AND maNhanVienTiepNhan=? "
                + "AND phuongThucHoanTien=N'Tiền mặt'";
        return tinhTongTrongKhoang(sql, batDau, ketThuc, maNhanVien);
    }

    private long tinhTongTrongKhoang(String sql, LocalDateTime batDau, LocalDateTime ketThuc,
                                     String maNhanVien) {
        if (batDau == null || ketThuc == null || maNhanVien == null) return 0L;
        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(batDau));
            ps.setTimestamp(2, Timestamp.valueOf(ketThuc));
            ps.setString(3, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong("tong") : 0L; }
        } catch (SQLException e) {
            System.err.println("[BangGiaoCaDAO] Lỗi tính doanh thu đúng ca/thu ngân: " + e.getMessage());
            throw new RuntimeException("Lỗi CSDL khi tính toán dữ liệu ca trực: " + e.getMessage(), e);
        }
    }

    private long tinhTongTheoThoiDiem(String sql, String columnName, LocalDateTime tuThoiDiem, String logName) {
        LocalDateTime mocTinh = tuThoiDiem != null
                ? tuThoiDiem
                : java.time.LocalDate.now().atStartOfDay();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(mocTinh));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(columnName) : 0L;
            }
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi tính " + logName + ": " + e.getMessage());
            throw new IllegalStateException("Không thể đối soát " + logName + "; dừng chốt ca để tránh sai tiền", e);
        }
    }

    /**
     * Lấy ca làm việc đang mở (chưa kết ca) của một nhân viên
     */
    public BangGiaoCa getCaHienTai(String maNhanVien) throws SQLException {
        String sql = "SELECT TOP 1 bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +
                     "WHERE bgc.maNhanVien = ? AND bgc.gioKetThucThucTe IS NULL " +
                     "ORDER BY bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNhanVien"));
                    nv.setHoTen(rs.getString("tenNhanVien"));

                    CaLamViec ca = new CaLamViec();
                    ca.setMaCa(rs.getString("maCa"));
                    ca.setTenCa(rs.getString("tenCa"));

                    BangGiaoCa bgc = new BangGiaoCa(
                            rs.getString("maGiaoCa"),
                            nv,
                            ca,
                            rs.getBigDecimal("tienDauCa"),
                            rs.getBigDecimal("tienCuoiCaHeThong"),
                            rs.getBigDecimal("tienCuoiCaThucTe"),
                            rs.getBigDecimal("tienChenhLech"),
                            rs.getString("lyDoChenhLech"),
                            null
                    );
                    boSungThongTinK80(bgc, rs);
                    return bgc;
                }
            }
        }
        return null;
    }

    /**
     * Tính tổng doanh thu Tiền mặt thực thu trong ngày hôm nay từ bảng HoaDon
     */
    public long tinhDoanhThuTienMatTrongNgay() {
        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongTienMat " +
                     "FROM HoaDon " +
                     "WHERE CAST(thoiGianThanhToan AS DATE) = CAST(GETDATE() AS DATE) " +
                     "  AND trangThaiHoaDon = N'Đã thanh toán' " +
                     "  AND phuongThucThanhToan = N'Tiền mặt'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("tongTienMat");
            }
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi tính doanh thu tiền mặt: " + e.getMessage());
            throw new IllegalStateException("Không thể đối soát doanh thu tiền mặt", e);
        }
        throw new IllegalStateException("Truy vấn doanh thu tiền mặt không trả về dòng kết quả");
    }

    /**
     * Tính tổng doanh thu Chuyển khoản / QR / VNPay / Thẻ trong ngày hôm nay từ bảng HoaDon
     */
    public long tinhDoanhThuChuyenKhoanTrongNgay() {
        String sql = "SELECT ISNULL(SUM(thanhTien), 0) AS tongCK " +
                     "FROM HoaDon " +
                     "WHERE CAST(thoiGianThanhToan AS DATE) = CAST(GETDATE() AS DATE) " +
                     "  AND trangThaiHoaDon = N'Đã thanh toán' " +
                     "  AND phuongThucThanhToan IS NOT NULL " +
                     "  AND phuongThucThanhToan NOT LIKE N'%Tiền mặt%'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("tongCK");
            }
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi tính doanh thu chuyển khoản: " + e.getMessage());
            throw new IllegalStateException("Không thể đối soát doanh thu chuyển khoản", e);
        }
        throw new IllegalStateException("Truy vấn doanh thu chuyển khoản không trả về dòng kết quả");
    }

    /**
     * Tính tổng tiền hoàn trả khách (Đổi / Trả hàng) trong ngày hôm nay từ bảng PhieuDoiTra
     */
    public long tinhTongTienHoanTraTrongNgay() {
        if (!hasPhuongThucHoanColumn()) {
            throw new IllegalStateException("CSDL chưa có dữ liệu phương thức hoàn tiền; không thể tính két chính xác");
        }
        String sql = "SELECT ISNULL(SUM(tienHoanThucTe), 0) AS tongHoan " +
                     "FROM PhieuDoiTra " +
                     "WHERE CAST(ngayDoiTra AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("tongHoan");
            }
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi tính tiền hoàn trả: " + e.getMessage());
            throw new IllegalStateException("Không thể đối soát tiền hoàn trả", e);
        }
        throw new IllegalStateException("Truy vấn tiền hoàn trả không trả về dòng kết quả");
    }

    /**
     * Cập nhật trạng thái Quản lý duyệt đối soát ca vào cột lyDoChenhLech của BangGiaoCa
     */
    public boolean duyetGiaoCa(String maGiaoCa, String maQuanLy, LocalDateTime thoiGianDuyet) {
        String sql = "UPDATE BangGiaoCa SET trangThai='DA_DUYET', maQuanLyDuyet=?, thoiGianDuyet=? " +
                     "WHERE maGiaoCa=? AND trangThai='CHO_DUYET'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maQuanLy);
            ps.setTimestamp(2, Timestamp.valueOf(thoiGianDuyet != null ? thoiGianDuyet : LocalDateTime.now()));
            ps.setString(3, maGiaoCa);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi cập nhật duyệt ca: " + e.getMessage());
            return false;
        }
    }

    // tương thích ngược cho các unit test kiểm thử duyệt ca cũ
    public boolean capNhatGhiChuDuyetCa(String maGiaoCa, String ghiChuMoi) throws SQLException {
        String maQuanLy = utils.UserSession.getUserId();
        if (maQuanLy == null || !utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý đang đăng nhập mới được duyệt ca");
        }
        if (!duyetGiaoCa(maGiaoCa, maQuanLy, LocalDateTime.now())) {
            return false;
        }
        String sql = "UPDATE BangGiaoCa SET lyDoChenhLech = ? WHERE maGiaoCa = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ghiChuMoi);
            ps.setString(2, maGiaoCa);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy danh sách các biên bản giao ca đang ở trạng thái chờ Quản lý duyệt
     */
    public List<BangGiaoCa> getDanhSachCaChoDuyet() {
        List<BangGiaoCa> list = new ArrayList<>();
        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                      "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +
                     "WHERE bgc.trangThai = 'CHO_DUYET' " +
                     "ORDER BY bgc.maGiaoCa DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("tenNhanVien"));

                CaLamViec ca = new CaLamViec();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));

                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");
                LocalDateTime gioKT = (kt != null) ? kt.toLocalDateTime() : null;

                BangGiaoCa bgc = new BangGiaoCa(
                        rs.getString("maGiaoCa"),
                        nv,
                        ca,
                        rs.getBigDecimal("tienDauCa"),
                        rs.getBigDecimal("tienCuoiCaHeThong"),
                        rs.getBigDecimal("tienCuoiCaThucTe"),
                        rs.getBigDecimal("tienChenhLech"),
                        rs.getString("lyDoChenhLech"),
                        gioKT
                );
                boSungThongTinK80(bgc, rs);
                list.add(bgc);
            }
        } catch (Exception e) {
            System.err.println("[BangGiaoCaDAO] Lỗi lấy danh sách ca chờ duyệt: " + e.getMessage());
        }
        return list;
    }

    /** Lấy đầy đủ dữ liệu lịch sử của một biên bản để xem/in lại K80. */
    public BangGiaoCa getBangGiaoCaTheoMa(String maGiaoCa) throws SQLException {
        String sql = "SELECT bgc.*, nv.hoTen AS tenNhanVien, c.tenCa, ql.hoTen AS tenQuanLyDuyet " +
                     "FROM BangGiaoCa bgc " +
                     "LEFT JOIN NhanVien nv ON bgc.maNhanVien = nv.maNhanVien " +
                     "LEFT JOIN CaLamViec c ON bgc.maCa = c.maCa " +
                     "LEFT JOIN NhanVien ql ON bgc.maQuanLyDuyet = ql.maNhanVien " +
                     "WHERE bgc.maGiaoCa = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGiaoCa);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                NhanVien nv = new NhanVien();
                nv.setMaNhanVien(rs.getString("maNhanVien"));
                nv.setHoTen(rs.getString("tenNhanVien"));
                CaLamViec ca = new CaLamViec();
                ca.setMaCa(rs.getString("maCa"));
                ca.setTenCa(rs.getString("tenCa"));
                Timestamp kt = rs.getTimestamp("gioKetThucThucTe");
                BangGiaoCa bgc = new BangGiaoCa(
                        rs.getString("maGiaoCa"), nv, ca,
                        rs.getBigDecimal("tienDauCa"), rs.getBigDecimal("tienCuoiCaHeThong"),
                        rs.getBigDecimal("tienCuoiCaThucTe"), rs.getBigDecimal("tienChenhLech"),
                        rs.getString("lyDoChenhLech"), kt != null ? kt.toLocalDateTime() : null);
                boSungThongTinK80(bgc, rs);
                return bgc;
            }
        }
    }

    private void boSungThongTinK80(BangGiaoCa bgc, ResultSet rs) throws SQLException {
        BigDecimal tienMat = rs.getBigDecimal("tienThuTienMat");
        BigDecimal chuyenKhoan = rs.getBigDecimal("tienChuyenKhoan");
        BigDecimal hoanTra = rs.getBigDecimal("tienHoanTra");
        bgc.setTienThuTienMat(tienMat != null ? tienMat : BigDecimal.ZERO);
        bgc.setTienChuyenKhoan(chuyenKhoan != null ? chuyenKhoan : BigDecimal.ZERO);
        bgc.setTienHoanTra(hoanTra != null ? hoanTra : BigDecimal.ZERO);
        bgc.setTrangThai(rs.getString("trangThai"));

        String maQuanLy = rs.getString("maQuanLyDuyet");
        if (maQuanLy != null && !maQuanLy.trim().isEmpty()) {
            NhanVien quanLy = new NhanVien();
            quanLy.setMaNhanVien(maQuanLy);
            quanLy.setHoTen(rs.getString("tenQuanLyDuyet"));
            bgc.setQuanLyDuyet(quanLy);
        }
        Timestamp thoiGianDuyet = rs.getTimestamp("thoiGianDuyet");
        bgc.setThoiGianDuyet(thoiGianDuyet != null ? thoiGianDuyet.toLocalDateTime() : null);
    }

    /**
     * Tự động phát sinh mã giao ca tiếp theo dạng GCxxx
     */
    public String phatSinhMaGiaoCa() {
        String sql = "SELECT TOP 1 maGiaoCa FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC%' AND maGiaoCa NOT LIKE 'GC_TEST_%' ORDER BY LEN(maGiaoCa) DESC, maGiaoCa DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String ma = rs.getString("maGiaoCa");
                String numStr = ma.replaceAll("[^0-9]", "");
                if (!numStr.isEmpty()) {
                    int num = Integer.parseInt(numStr) + 1;
                    return String.format("GC%03d", num);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể phát sinh mã giao ca vì CSDL không khả dụng", e);
        }
        return "GC001";
    }
}
