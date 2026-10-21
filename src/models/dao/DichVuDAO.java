package models.dao;

import models.entity.DichVu;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Dịch vụ chăm sóc thú cưng (Spa, Grooming, Khám).
 * Cung cấp đầy đủ các thao tác CRUD.
 */
public class DichVuDAO {

    /**
     * Lấy toàn bộ danh sách dịch vụ trong hệ thống
     */
    public List<DichVu> getAllDichVu() throws SQLException {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT * FROM DichVu ORDER BY maDichVu ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DichVu(
                        rs.getString("maDichVu"),
                        rs.getString("tenDichVu"),
                        rs.getBigDecimal("giaDichVu"),
                        rs.getInt("thoiLuongPhut")
                ));
            }
        }
        return list;
    }

    /**
     * Tìm dịch vụ theo mã ID
     */
    public DichVu getDichVuById(String maDichVu) throws SQLException {
        String sql = "SELECT * FROM DichVu WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DichVu(
                            rs.getString("maDichVu"),
                            rs.getString("tenDichVu"),
                            rs.getBigDecimal("giaDichVu"),
                            rs.getInt("thoiLuongPhut")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Thêm dịch vụ mới
     */
    public boolean insertDichVu(DichVu dv) throws SQLException {
        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, giaDichVu, thoiLuongPhut) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getMaDichVu());
            ps.setString(2, dv.getTenDichVu());
            ps.setBigDecimal(3, dv.getGiaDichVu());
            ps.setInt(4, dv.getThoiLuongPhut());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin dịch vụ (tên, giá tiền, thời lượng phút)
     */
    public boolean updateDichVu(DichVu dv) throws SQLException {
        String sql = "UPDATE DichVu SET tenDichVu = ?, giaDichVu = ?, thoiLuongPhut = ? WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dv.getTenDichVu());
            ps.setBigDecimal(2, dv.getGiaDichVu());
            ps.setInt(3, dv.getThoiLuongPhut());
            ps.setString(4, dv.getMaDichVu());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa dịch vụ theo mã
     */
    public boolean deleteDichVu(String maDichVu) throws SQLException {
        String sql = "DELETE FROM DichVu WHERE maDichVu = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDichVu);
            return ps.executeUpdate() > 0;
        }
    }
}
