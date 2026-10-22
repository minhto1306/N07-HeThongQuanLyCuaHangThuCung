package models.dao;

import models.entity.TaiKhoan;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Tài khoản người dùng (TaiKhoan).
 * Cung cấp đầy đủ các thao tác CRUD và bảo mật xác thực người dùng.
 */
public class TaiKhoanDAO {

    /**
     * Tìm tài khoản theo tên đăng nhập.
     */
    public TaiKhoan findByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TaiKhoan(
                            rs.getString("tenDangNhap"),
                            rs.getString("matKhau"),
                            rs.getString("vaiTro"),
                            rs.getBoolean("trangThaiHoatDong")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Xác thực thông tin đăng nhập.
     * @param tenDangNhap Tên đăng nhập
     * @param matKhau Mật khẩu
     * @return TaiKhoan nếu hợp lệ và đang hoạt động, null nếu sai thông tin
     */
    public TaiKhoan kiemTraDangNhap(String tenDangNhap, String matKhau) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ? AND trangThaiHoatDong = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TaiKhoan(
                            rs.getString("tenDangNhap"),
                            rs.getString("matKhau"),
                            rs.getString("vaiTro"),
                            rs.getBoolean("trangThaiHoatDong")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Đổi mật khẩu tài khoản.
     */
    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy toàn bộ danh sách tài khoản
     */
    public List<TaiKhoan> getAllTaiKhoan() throws SQLException {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT * FROM TaiKhoan ORDER BY tenDangNhap ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        rs.getString("matKhau"),
                        rs.getString("vaiTro"),
                        rs.getBoolean("trangThaiHoatDong")
                ));
            }
        }
        return list;
    }

    /**
     * Thêm tài khoản mới
     */
    public boolean insertTaiKhoan(TaiKhoan tk) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tk.getTenDangNhap());
            ps.setString(2, tk.getMatKhau());
            ps.setString(3, tk.getVaiTro());
            ps.setBoolean(4, tk.isTrangThaiHoatDong());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa tài khoản
     */
    public boolean deleteTaiKhoan(String tenDangNhap) throws SQLException {
        String sql = "DELETE FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }
}
