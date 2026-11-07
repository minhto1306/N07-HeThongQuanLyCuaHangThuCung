package dao;

import utils.DBConnection;
import gui.PetHotelPanel.PhongLuuTru;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý Phòng lưu trú Khách sạn thú cưng (PhongLuuTru) trên SQL Server.
 */
public class PhongLuuTruDAO {

    public List<PhongLuuTru> getAllPhong() throws SQLException {
        List<PhongLuuTru> list = new ArrayList<>();
        String sql = "SELECT * FROM PhongLuuTru ORDER BY maPhong ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PhongLuuTru p = new PhongLuuTru(
                        rs.getString("maPhong"),
                        rs.getString("loaiPhong"),
                        rs.getLong("donGiaNgay"),
                        rs.getString("trangThai")
                );
                p.tenThuCung = rs.getString("tenThuCung") != null ? rs.getString("tenThuCung") : "";
                p.giongLoai = rs.getString("giongLoai") != null ? rs.getString("giongLoai") : "";
                p.tenChu = rs.getString("tenChu") != null ? rs.getString("tenChu") : "";
                p.sdtChu = rs.getString("sdtChu") != null ? rs.getString("sdtChu") : "";
                Timestamp ci = rs.getTimestamp("thoiGianCheckIn");
                Timestamp co = rs.getTimestamp("duKienCheckOut");
                p.thoiGianCheckIn = ci != null ? ci.toLocalDateTime() : null;
                p.duKienCheckOut = co != null ? co.toLocalDateTime() : null;
                p.ghiChuChamSoc = rs.getString("ghiChuChamSoc") != null ? rs.getString("ghiChuChamSoc") : "";
                p.daChoAnSang = rs.getBoolean("daChoAnSang");
                p.daChoAnToi = rs.getBoolean("daChoAnToi");
                p.daDiDaoVeSinh = rs.getBoolean("daDiDaoVeSinh");
                p.phiDichVuPhatSinh = rs.getLong("phiDichVuPhatSinh");
                list.add(p);
            }
        }
        return list;
    }

    public boolean updatePhong(PhongLuuTru p) throws SQLException {
        String sql = "UPDATE PhongLuuTru SET trangThai = ?, tenThuCung = ?, giongLoai = ?, tenChu = ?, sdtChu = ?, " +
                     "thoiGianCheckIn = ?, duKienCheckOut = ?, ghiChuChamSoc = ?, daChoAnSang = ?, daChoAnToi = ?, " +
                     "daDiDaoVeSinh = ?, phiDichVuPhatSinh = ? WHERE maPhong = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.trangThai);
            ps.setString(2, p.tenThuCung != null ? p.tenThuCung : "");
            ps.setString(3, p.giongLoai != null ? p.giongLoai : "");
            ps.setString(4, p.tenChu != null ? p.tenChu : "");
            ps.setString(5, p.sdtChu != null ? p.sdtChu : "");
            if (p.thoiGianCheckIn != null) ps.setTimestamp(6, Timestamp.valueOf(p.thoiGianCheckIn));
            else ps.setNull(6, Types.TIMESTAMP);
            if (p.duKienCheckOut != null) ps.setTimestamp(7, Timestamp.valueOf(p.duKienCheckOut));
            else ps.setNull(7, Types.TIMESTAMP);
            ps.setString(8, p.ghiChuChamSoc != null ? p.ghiChuChamSoc : "");
            ps.setBoolean(9, p.daChoAnSang);
            ps.setBoolean(10, p.daChoAnToi);
            ps.setBoolean(11, p.daDiDaoVeSinh);
            ps.setLong(12, p.phiDichVuPhatSinh);
            ps.setString(13, p.maPhong);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean insertLichSuLuuTru(String maPhong, String tenThuCung, String tenChu, String sdtChu,
                                      java.time.LocalDateTime checkIn, java.time.LocalDateTime checkOut,
                                      long soNgay, long donGia, long phiPhatSinh, long tongTien, String ghiChu) throws SQLException {
        String sql = "INSERT INTO LichSuLuuTru (maPhong, tenThuCung, tenChu, sdtChu, thoiGianCheckIn, thoiGianCheckOut, " +
                     "soNgayLuuTru, donGiaNgay, phiPhatSinh, tongTien, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setString(2, tenThuCung != null ? tenThuCung : "");
            ps.setString(3, tenChu != null ? tenChu : "");
            ps.setString(4, sdtChu != null ? sdtChu : "");
            if (checkIn != null) ps.setTimestamp(5, Timestamp.valueOf(checkIn));
            else ps.setNull(5, Types.TIMESTAMP);
            if (checkOut != null) ps.setTimestamp(6, Timestamp.valueOf(checkOut));
            else ps.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(7, (int) soNgay);
            ps.setBigDecimal(8, java.math.BigDecimal.valueOf(donGia));
            ps.setBigDecimal(9, java.math.BigDecimal.valueOf(phiPhatSinh));
            ps.setBigDecimal(10, java.math.BigDecimal.valueOf(tongTien));
            ps.setString(11, ghiChu != null ? ghiChu : "");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean hoanTatCheckOutDaThanhToan(PhongLuuTru p, long soNgay, long tongTien,
                                               String maHoaDon) throws SQLException {
        String sqlLichSu = "INSERT INTO LichSuLuuTru (maPhong, tenThuCung, tenChu, sdtChu, thoiGianCheckIn, "
                + "thoiGianCheckOut, soNgayLuuTru, donGiaNgay, phiPhatSinh, tongTien, ghiChu, maHoaDon, trangThaiThanhToan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, N'DA_THANH_TOAN')";
        String sqlPhong = "UPDATE PhongLuuTru SET trangThai=N'ĐANG DỌN DẸP' "
                + "WHERE maPhong=? AND trangThai=N'ĐANG CÓ KHÁCH'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlLichSu)) {
                ps.setString(1, p.maPhong);
                ps.setString(2, p.tenThuCung);
                ps.setString(3, p.tenChu);
                ps.setString(4, p.sdtChu);
                ps.setTimestamp(5, p.thoiGianCheckIn != null ? Timestamp.valueOf(p.thoiGianCheckIn) : null);
                ps.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setLong(7, soNgay);
                ps.setLong(8, p.donGiaNgay);
                ps.setLong(9, p.phiDichVuPhatSinh);
                ps.setLong(10, tongTien);
                ps.setString(11, p.ghiChuChamSoc);
                ps.setString(12, maHoaDon);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlPhong)) {
                ps.setString(1, p.maPhong);
                if (ps.executeUpdate() != 1) throw new SQLException("Phòng không còn ở trạng thái đang có khách");
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
}
