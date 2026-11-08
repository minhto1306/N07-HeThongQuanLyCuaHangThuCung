package dao;

import entity.*;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Lịch hẹn và Chi tiết Lịch hẹn dịch vụ.
 */
public class LichHenDAO {

    /**
     * Lấy toàn bộ danh sách lịch hẹn kèm khách hàng và chi tiết dịch vụ
     */
    public List<LichHen> getAllLichHen() throws SQLException {
        List<LichHen> list = new ArrayList<>();
        String sql = "SELECT lh.maLichHen, lh.ngayHen, lh.trangThaiLichHen, lh.ghiChu, " +
                "kh.maKhachHang, kh.hoTen AS tenKhachHang, kh.soDienThoai, kh.diemTichLuy, " +
                "nv.maNhanVien AS maNvTiepNhan, nv.hoTen AS tenNvTiepNhan " +
                "FROM LichHen lh " +
                "JOIN KhachHang kh ON lh.maKhachHang = kh.maKhachHang " +
                "LEFT JOIN NhanVien nv ON lh.maNhanVienTiepNhan = nv.maNhanVien " +
                "ORDER BY lh.ngayHen DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhachHang kh = new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("tenKhachHang"),
                        rs.getString("soDienThoai"),
                        rs.getInt("diemTichLuy")
                );

                NhanVien nv = null;
                if (rs.getString("maNvTiepNhan") != null) {
                    nv = new NhanVien();
                    nv.setMaNhanVien(rs.getString("maNvTiepNhan"));
                    nv.setHoTen(rs.getString("tenNvTiepNhan"));
                }

                Timestamp ts = rs.getTimestamp("ngayHen");
                LocalDateTime ngayHen = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

                LichHen lh = new LichHen(
                        rs.getString("maLichHen"),
                        kh,
                        nv,
                        ngayHen,
                        rs.getString("trangThaiLichHen"),
                        rs.getString("ghiChu")
                );

                // Nạp danh sách chi tiết
                lh.setDsChiTiet(getChiTietByLichHen(conn, lh.getMaLichHen(), lh));
                list.add(lh);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách chi tiết lịch hẹn của 1 mã lịch hẹn
     */
    private List<ChiTietLichHen> getChiTietByLichHen(Connection conn, String maLichHen, LichHen lh) throws SQLException {
        List<ChiTietLichHen> ds = new ArrayList<>();
        String sql = "SELECT ctlh.maChiTietLichHen, " +
                "tc.maThuCung, tc.tenThuCung, tc.giongLoai, " +
                "dv.maDichVu, dv.tenDichVu, dv.giaDichVu, dv.thoiLuongPhut, " +
                "ktv.maNhanVien AS maKtv, ktv.hoTen AS tenKtv " +
                "FROM ChiTietLichHen ctlh " +
                "JOIN ThuCung tc ON ctlh.maThuCung = tc.maThuCung " +
                "JOIN DichVu dv ON ctlh.maDichVu = dv.maDichVu " +
                "LEFT JOIN NhanVien ktv ON ctlh.maKtvChiDinh = ktv.maNhanVien " +
                "WHERE ctlh.maLichHen = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLichHen);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThuCung tc = new ThuCung();
                    tc.setMaThuCung(rs.getString("maThuCung"));
                    tc.setTenThuCung(rs.getString("tenThuCung"));
                    tc.setGiongLoai(rs.getString("giongLoai"));

                    DichVu dv = new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getBigDecimal("giaDichVu"),
                            rs.getInt("thoiLuongPhut")
                    );

                    NhanVien ktv = null;
                    if (rs.getString("maKtv") != null) {
                        ktv = new NhanVien();
                        ktv.setMaNhanVien(rs.getString("maKtv"));
                        ktv.setHoTen(rs.getString("tenKtv"));
                    }

                    ChiTietLichHen ct = new ChiTietLichHen(
                            rs.getString("maChiTietLichHen"),
                            lh,
                            tc,
                            dv,
                            ktv
                    );
                    ds.add(ct);
                }
            }
        }
        return ds;
    }

    private static final java.time.format.DateTimeFormatter DTF_LOG = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private int layThoiLuongDichVu(Connection conn, String maDichVu) {
        if (maDichVu == null || maDichVu.isBlank()) return 45;
        String sql = "SELECT thoiLuongPhut FROM DichVu WHERE maDichVu = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int tl = rs.getInt(1);
                    return tl > 0 ? tl : 45;
                }
            }
        } catch (SQLException ignored) {}
        return 45;
    }

    private void kiemTraXungDotKtv(Connection conn, String maLichHenHienTai, String maKtv,
                                   LocalDateTime newStart, LocalDateTime newEnd) throws SQLException {
        if (maKtv == null || maKtv.isBlank()) return;
        String sql = "SELECT TOP 1 l.maLichHen, l.ngayHen, "
                + "DATEADD(MINUTE, ISNULL(dv.thoiLuongPhut, 45), l.ngayHen) AS ketThuc, "
                + "ISNULL(dv.tenDichVu, N'Dịch vụ') AS tenDichVu, "
                + "ISNULL(nv.hoTen, ct.maKtvChiDinh) AS tenKtv "
                + "FROM ChiTietLichHen ct "
                + "JOIN LichHen l ON ct.maLichHen = l.maLichHen "
                + "LEFT JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
                + "LEFT JOIN NhanVien nv ON ct.maKtvChiDinh = nv.maNhanVien "
                + "WHERE ct.maKtvChiDinh = ? "
                + "AND l.trangThaiLichHen NOT IN (N'Đã hủy', N'Hủy', N'HỦY', N'DA_HUY') "
                + "AND (? IS NULL OR l.maLichHen <> ?) "
                + "AND l.ngayHen < ? AND DATEADD(MINUTE, ISNULL(dv.thoiLuongPhut, 45), l.ngayHen) > ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKtv.trim());
            ps.setString(2, maLichHenHienTai);
            ps.setString(3, maLichHenHienTai);
            ps.setTimestamp(4, Timestamp.valueOf(newEnd));
            ps.setTimestamp(5, Timestamp.valueOf(newStart));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maLhCu = rs.getString("maLichHen");
                    LocalDateTime exStart = rs.getTimestamp("ngayHen").toLocalDateTime();
                    LocalDateTime exEnd = rs.getTimestamp("ketThuc").toLocalDateTime();
                    String tenDichVu = rs.getString("tenDichVu");
                    String tenKtv = rs.getString("tenKtv");
                    throw new SQLException(String.format(
                            "Kỹ thuật viên [%s] đã có lịch hẹn [%s] (%s) từ %s đến %s! Không thể xếp lịch trùng trong khoảng %s đến %s (DV08).",
                            tenKtv, maLhCu, tenDichVu, exStart.format(DTF_LOG), exEnd.format(DTF_LOG),
                            newStart.format(DTF_LOG), newEnd.format(DTF_LOG)
                    ));
                }
            }
        }
    }

    private void kiemTraXungDotThuCung(Connection conn, String maLichHenHienTai, String maThuCung,
                                       LocalDateTime newStart, LocalDateTime newEnd) throws SQLException {
        if (maThuCung == null || maThuCung.isBlank()) return;
        String sql = "SELECT TOP 1 l.maLichHen, l.ngayHen, "
                + "DATEADD(MINUTE, ISNULL(dv.thoiLuongPhut, 45), l.ngayHen) AS ketThuc, "
                + "ISNULL(dv.tenDichVu, N'Dịch vụ') AS tenDichVu, "
                + "ISNULL(tc.tenThuCung, ct.maThuCung) AS tenThuCung "
                + "FROM ChiTietLichHen ct "
                + "JOIN LichHen l ON ct.maLichHen = l.maLichHen "
                + "LEFT JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
                + "LEFT JOIN ThuCung tc ON ct.maThuCung = tc.maThuCung "
                + "WHERE ct.maThuCung = ? "
                + "AND l.trangThaiLichHen NOT IN (N'Đã hủy', N'Hủy', N'HỦY', N'DA_HUY') "
                + "AND (? IS NULL OR l.maLichHen <> ?) "
                + "AND l.ngayHen < ? AND DATEADD(MINUTE, ISNULL(dv.thoiLuongPhut, 45), l.ngayHen) > ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maThuCung.trim());
            ps.setString(2, maLichHenHienTai);
            ps.setString(3, maLichHenHienTai);
            ps.setTimestamp(4, Timestamp.valueOf(newEnd));
            ps.setTimestamp(5, Timestamp.valueOf(newStart));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maLhCu = rs.getString("maLichHen");
                    LocalDateTime exStart = rs.getTimestamp("ngayHen").toLocalDateTime();
                    LocalDateTime exEnd = rs.getTimestamp("ketThuc").toLocalDateTime();
                    String tenDichVu = rs.getString("tenDichVu");
                    String tenThuCung = rs.getString("tenThuCung");
                    throw new SQLException(String.format(
                            "Thú cưng [%s] đã có lịch hẹn [%s] (%s) từ %s đến %s! Một thú cưng không thể thực hiện hai dịch vụ trong khoảng giờ giao nhau %s đến %s (DV08).",
                            tenThuCung, maLhCu, tenDichVu, exStart.format(DTF_LOG), exEnd.format(DTF_LOG),
                            newStart.format(DTF_LOG), newEnd.format(DTF_LOG)
                    ));
                }
            }
        }
    }

    /**
     * Thêm lịch hẹn mới kèm chi tiết (Transaction JDBC an toàn)
     */
    public boolean insertLichHen(LichHen lh) throws SQLException {
        String sqlLH = "INSERT INTO LichHen (maLichHen, maKhachHang, maNhanVienTiepNhan, ngayHen, trangThaiLichHen, ghiChu) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietLichHen (maChiTietLichHen, maLichHen, maThuCung, maDichVu, maKtvChiDinh) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            LocalDateTime newStart = lh.getNgayHen() != null ? lh.getNgayHen() : LocalDateTime.now();

            // Kiểm tra trùng lịch kỹ thuật viên và thú cưng theo khoảng giao thoa thời gian chuẩn (DV08)
            if (lh.getDsChiTiet() != null) {
                for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                    int thoiLuong = 0;
                    if (ct.getDichVu() != null) {
                        thoiLuong = ct.getDichVu().getThoiLuongPhut();
                        if (thoiLuong <= 0 && ct.getDichVu().getMaDichVu() != null) {
                            thoiLuong = layThoiLuongDichVu(conn, ct.getDichVu().getMaDichVu());
                        }
                    }
                    if (thoiLuong <= 0) thoiLuong = 45;
                    LocalDateTime newEnd = newStart.plusMinutes(thoiLuong);

                    // 1. Kiểm tra trùng lịch KTV
                    if (ct.getKtvChiDinh() != null && ct.getKtvChiDinh().getMaNhanVien() != null) {
                        kiemTraXungDotKtv(conn, lh.getMaLichHen(), ct.getKtvChiDinh().getMaNhanVien(), newStart, newEnd);
                    }

                    // 2. Kiểm tra trùng lịch Thú cưng
                    if (ct.getThuCung() != null && ct.getThuCung().getMaThuCung() != null) {
                        kiemTraXungDotThuCung(conn, lh.getMaLichHen(), ct.getThuCung().getMaThuCung(), newStart, newEnd);
                    }
                }
            }

            try (PreparedStatement psLH = conn.prepareStatement(sqlLH)) {
                psLH.setString(1, lh.getMaLichHen());
                psLH.setString(2, lh.getKhachHang().getMaKhachHang());
                psLH.setString(3, lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getMaNhanVien() : null);
                psLH.setTimestamp(4, Timestamp.valueOf(lh.getNgayHen() != null ? lh.getNgayHen() : LocalDateTime.now()));
                psLH.setString(5, lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt");
                psLH.setString(6, lh.getGhiChu());
                psLH.executeUpdate();
            }

            for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                try (PreparedStatement psCT = conn.prepareStatement(sqlCT)) {
                    psCT.setString(1, ct.getMaChiTietLichHen());
                    psCT.setString(2, lh.getMaLichHen());
                    psCT.setString(3, ct.getThuCung().getMaThuCung());
                    psCT.setString(4, ct.getDichVu().getMaDichVu());
                    psCT.setString(5, ct.getKtvChiDinh() != null ? ct.getKtvChiDinh().getMaNhanVien() : null);
                    psCT.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật trạng thái lịch hẹn (Đã đặt, Đang phục vụ, Hoàn thành, Hủy)
     */
    public boolean capNhatTrangThai(String maLichHen, String trangThaiMoi) throws SQLException {
        String sql = "UPDATE LichHen SET trangThaiLichHen = ? WHERE maLichHen = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setString(2, maLichHen);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Phân công KTV chỉ định cho chi tiết lịch hẹn (có kiểm tra chống trùng lịch)
     */
    public boolean capNhatKtvChiDinh(String maChiTietLichHen, String maKtv) throws SQLException {
        if (maChiTietLichHen == null || maChiTietLichHen.isBlank() || maKtv == null || maKtv.isBlank()) {
            return false;
        }
        String sqlQuery = "SELECT l.maLichHen, l.ngayHen, ct.maDichVu, ISNULL(dv.thoiLuongPhut, 45) AS thoiLuong "
                + "FROM ChiTietLichHen ct "
                + "JOIN LichHen l ON ct.maLichHen = l.maLichHen "
                + "LEFT JOIN DichVu dv ON ct.maDichVu = dv.maDichVu "
                + "WHERE ct.maChiTietLichHen = ?";
        try (Connection conn = DBConnection.getConnection()) {
            LocalDateTime start = null;
            int thoiLuong = 45;
            String maLichHen = null;
            try (PreparedStatement psQ = conn.prepareStatement(sqlQuery)) {
                psQ.setString(1, maChiTietLichHen);
                try (ResultSet rs = psQ.executeQuery()) {
                    if (rs.next()) {
                        maLichHen = rs.getString("maLichHen");
                        Timestamp ts = rs.getTimestamp("ngayHen");
                        if (ts != null) start = ts.toLocalDateTime();
                        thoiLuong = rs.getInt("thoiLuong");
                    }
                }
            }
            if (start != null) {
                LocalDateTime end = start.plusMinutes(thoiLuong > 0 ? thoiLuong : 45);
                kiemTraXungDotKtv(conn, maLichHen, maKtv, start, end);
            }
            String sql = "UPDATE ChiTietLichHen SET maKtvChiDinh = ? WHERE maChiTietLichHen = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maKtv);
                ps.setString(2, maChiTietLichHen);
                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * Xóa lịch hẹn kèm toàn bộ chi tiết (Transaction)
     */
    public boolean deleteLichHen(String maLichHen) throws SQLException {
        String sqlCT = "DELETE FROM ChiTietLichHen WHERE maLichHen = ?";
        String sqlLH = "DELETE FROM LichHen WHERE maLichHen = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlCT)) {
                ps1.setString(1, maLichHen);
                ps1.executeUpdate();
            }

            int rows;
            try (PreparedStatement ps2 = conn.prepareStatement(sqlLH)) {
                ps2.setString(1, maLichHen);
                rows = ps2.executeUpdate();
            }

            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
