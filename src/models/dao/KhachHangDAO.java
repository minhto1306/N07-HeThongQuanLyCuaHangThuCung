package models.dao;

import models.entity.KhachHang;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Khách hàng (KhachHang).
 * Cung cấp đầy đủ các thao tác CRUD và quản lý điểm tích lũy thành viên.
 */
public class KhachHangDAO {

    /**
     * Lấy toàn bộ danh sách khách hàng trong hệ thống
     */
    public List<KhachHang> getAllKhachHang() throws SQLException {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang ORDER BY hoTen ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhachHang(
                        rs.getString("maKhachHang"),
                        rs.getString("hoTen"),
                        rs.getString("soDienThoai"),
                        rs.getInt("diemTichLuy")
                ));
            }
        }
        return list;
    }

    /**
     * Tìm khách hàng theo số điện thoại
     */
    public KhachHang findBySoDienThoai(String soDienThoai) throws SQLException {
        String sql = "SELECT * FROM KhachHang WHERE soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("maKhachHang"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getInt("diemTichLuy")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Tìm khách hàng theo mã khách hàng
     */
    public KhachHang findByMaKhachHang(String maKhachHang) throws SQLException {
        String sql = "SELECT * FROM KhachHang WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new KhachHang(
                            rs.getString("maKhachHang"),
                            rs.getString("hoTen"),
                            rs.getString("soDienThoai"),
                            rs.getInt("diemTichLuy")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Thêm khách hàng mới
     */
    public boolean insert(KhachHang kh) throws SQLException {
        String sql = "INSERT INTO KhachHang (maKhachHang, hoTen, soDienThoai, diemTichLuy) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKhachHang());
            ps.setString(2, kh.getHoTen());
            ps.setString(3, kh.getSoDienThoai());
            ps.setInt(4, kh.getDiemTichLuy());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    public boolean update(KhachHang kh) throws SQLException {
        String sql = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ?, diemTichLuy = ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getHoTen());
            ps.setString(2, kh.getSoDienThoai());
            ps.setInt(3, kh.getDiemTichLuy());
            ps.setString(4, kh.getMaKhachHang());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa khách hàng theo mã
     */
    public boolean delete(String maKhachHang) throws SQLException {
        String sql = "DELETE FROM KhachHang WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maKhachHang);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cộng dồn hoặc trừ điểm tích lũy của khách hàng
     */
    public boolean capNhatDiemTichLuy(String maKhachHang, int diemThayDoi) throws SQLException {
        String sql = "UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKhachHang = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diemThayDoi);
            ps.setString(2, maKhachHang);
            return ps.executeUpdate() > 0;
        }
    }
}
