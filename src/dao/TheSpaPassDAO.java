package dao;

import entity.TheSpaPass;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý dữ liệu Thẻ Spa Pass trên cơ sở dữ liệu SQL Server.
 */
public class TheSpaPassDAO {

    public List<TheSpaPass> getAllTheSpaPass() throws SQLException {
        List<TheSpaPass> list = new ArrayList<>();
        String sql = "SELECT * FROM TheSpaPass ORDER BY ngayKichHoat DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date dKh = rs.getDate("ngayKichHoat");
                Date dHh = rs.getDate("ngayHetHan");
                TheSpaPass the = new TheSpaPass(
                        rs.getString("maThe"),
                        rs.getString("sdtKhachHang"),
                        rs.getString("tenKhachHang"),
                        rs.getString("tenGoi"),
                        rs.getInt("tongSoBuoi"),
                        rs.getInt("soBuoiDaDung"),
                        rs.getBigDecimal("donGiaGoi").longValue(),
                        dKh != null ? dKh.toLocalDate() : LocalDate.now(),
                        dHh != null ? dHh.toLocalDate() : LocalDate.now().plusDays(90)
                );
                the.setTrangThai(rs.getString("trangThai"));
                list.add(the);
            }
        }
        return list;
    }

    public TheSpaPass getTheSpaPassBySdt(String sdt) throws SQLException {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String sql = "SELECT TOP 1 * FROM TheSpaPass WHERE sdtKhachHang = ? AND soBuoiDaDung < tongSoBuoi AND ngayHetHan >= CAST(GETDATE() AS DATE) ORDER BY ngayKichHoat DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sdt.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date dKh = rs.getDate("ngayKichHoat");
                    Date dHh = rs.getDate("ngayHetHan");
                    TheSpaPass the = new TheSpaPass(
                            rs.getString("maThe"),
                            rs.getString("sdtKhachHang"),
                            rs.getString("tenKhachHang"),
                            rs.getString("tenGoi"),
                            rs.getInt("tongSoBuoi"),
                            rs.getInt("soBuoiDaDung"),
                            rs.getBigDecimal("donGiaGoi").longValue(),
                            dKh != null ? dKh.toLocalDate() : LocalDate.now(),
                            dHh != null ? dHh.toLocalDate() : LocalDate.now().plusDays(90)
                    );
                    the.setTrangThai(rs.getString("trangThai"));
                    return the;
                }
            }
        }
        return null;
    }

    public boolean insertTheSpaPass(TheSpaPass the) throws SQLException {
        String sql = "INSERT INTO TheSpaPass (maThe, sdtKhachHang, tenKhachHang, tenGoi, tongSoBuoi, soBuoiDaDung, donGiaGoi, ngayKichHoat, ngayHetHan, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, the.getMaThe());
            ps.setString(2, the.getSdtKhachHang());
            ps.setString(3, the.getTenKhachHang());
            ps.setString(4, the.getTenGoi());
            ps.setInt(5, the.getTongSoBuoi());
            ps.setInt(6, the.getSoBuoiDaDung());
            ps.setBigDecimal(7, BigDecimal.valueOf(the.getDonGiaGoi()));
            ps.setDate(8, Date.valueOf(the.getNgayKichHoat() != null ? the.getNgayKichHoat() : LocalDate.now()));
            ps.setDate(9, Date.valueOf(the.getNgayHetHan() != null ? the.getNgayHetHan() : LocalDate.now().plusDays(90)));
            ps.setString(10, the.getTrangThai() != null ? the.getTrangThai() : "ĐANG HOẠT ĐỘNG");
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateTheSpaPass(TheSpaPass the) throws SQLException {
        String sql = "UPDATE TheSpaPass SET tongSoBuoi = ?, soBuoiDaDung = ?, ngayHetHan = ?, trangThai = ? WHERE maThe = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, the.getTongSoBuoi());
            ps.setInt(2, the.getSoBuoiDaDung());
            ps.setDate(3, Date.valueOf(the.getNgayHetHan() != null ? the.getNgayHetHan() : LocalDate.now().plusDays(90)));
            ps.setString(4, the.getTrangThai());
            ps.setString(5, the.getMaThe());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean suDungMotBuoi(String maThe, String maHoaDon, String maNhanVien, String ghiChu)
            throws SQLException {
        String sqlUpdate = "UPDATE TheSpaPass SET soBuoiDaDung=soBuoiDaDung+1 "
                + "WHERE maThe=? AND trangThai=N'ĐANG HOẠT ĐỘNG' AND ngayHetHan>=CAST(GETDATE() AS DATE) "
                + "AND soBuoiDaDung<tongSoBuoi";
        String sqlHistory = "INSERT INTO LichSuSuDungSpaPass(maThe,maHoaDon,maNhanVien,thoiGianSuDung,ghiChu) "
                + "VALUES(?,?,?,SYSDATETIME(),?)";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, maThe);
                if (ps.executeUpdate() != 1) throw new SQLException("Thẻ không còn lượt hoặc đã hết hạn");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlHistory)) {
                ps.setString(1, maThe);
                ps.setString(2, maHoaDon);
                ps.setString(3, maNhanVien);
                ps.setString(4, ghiChu);
                ps.executeUpdate();
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
