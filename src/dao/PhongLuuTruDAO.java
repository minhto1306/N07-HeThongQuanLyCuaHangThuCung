package dao;

import utils.DBConnection;
import entity.PhongLuuTru;

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
                p.setTenThuCung(rs.getString("tenThuCung"));
                p.setGiongLoai(rs.getString("giongLoai"));
                p.setTenChu(rs.getString("tenChu"));
                p.setSdtChu(rs.getString("sdtChu"));
                Timestamp ci = rs.getTimestamp("thoiGianCheckIn");
                Timestamp co = rs.getTimestamp("duKienCheckOut");
                p.setThoiGianCheckIn(ci != null ? ci.toLocalDateTime() : null);
                p.setDuKienCheckOut(co != null ? co.toLocalDateTime() : null);
                p.setGhiChuChamSoc(rs.getString("ghiChuChamSoc"));
                p.setDaChoAnSang(rs.getBoolean("daChoAnSang"));
                p.setDaChoAnToi(rs.getBoolean("daChoAnToi"));
                p.setDaDiDaoVeSinh(rs.getBoolean("daDiDaoVeSinh"));
                p.setPhiDichVuPhatSinh(rs.getLong("phiDichVuPhatSinh"));
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
            ps.setString(1, p.getTrangThai());
            ps.setString(2, p.getTenThuCung() != null ? p.getTenThuCung() : "");
            ps.setString(3, p.getGiongLoai() != null ? p.getGiongLoai() : "");
            ps.setString(4, p.getTenChu() != null ? p.getTenChu() : "");
            ps.setString(5, p.getSdtChu() != null ? p.getSdtChu() : "");
            if (p.getThoiGianCheckIn() != null) ps.setTimestamp(6, Timestamp.valueOf(p.getThoiGianCheckIn()));
            else ps.setNull(6, Types.TIMESTAMP);
            if (p.getDuKienCheckOut() != null) ps.setTimestamp(7, Timestamp.valueOf(p.getDuKienCheckOut()));
            else ps.setNull(7, Types.TIMESTAMP);
            ps.setString(8, p.getGhiChuChamSoc() != null ? p.getGhiChuChamSoc() : "");
            ps.setBoolean(9, p.isDaChoAnSang());
            ps.setBoolean(10, p.isDaChoAnToi());
            ps.setBoolean(11, p.isDaDiDaoVeSinh());
            ps.setLong(12, p.getPhiDichVuPhatSinh());
            ps.setString(13, p.getMaPhong());
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
                ps.setString(1, p.getMaPhong());
                ps.setString(2, p.getTenThuCung());
                ps.setString(3, p.getTenChu());
                ps.setString(4, p.getSdtChu());
                ps.setTimestamp(5, p.getThoiGianCheckIn() != null ? Timestamp.valueOf(p.getThoiGianCheckIn()) : null);
                ps.setTimestamp(6, Timestamp.valueOf(java.time.LocalDateTime.now()));
                ps.setLong(7, soNgay);
                ps.setLong(8, p.getDonGiaNgay());
                ps.setLong(9, p.getPhiDichVuPhatSinh());
                ps.setLong(10, tongTien);
                ps.setString(11, p.getGhiChuChamSoc());
                ps.setString(12, maHoaDon);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlPhong)) {
                ps.setString(1, p.getMaPhong());
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
