package models.dao;

import utils.DBConnection;
import views.PetHotelPanel.PhongLuuTru;

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
}
