package models.dao;

import models.entity.NhanVien;
import models.entity.TaiKhoan;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Nhân viên (NhanVien) và Tài khoản (TaiKhoan).
 * Cung cấp đầy đủ các thao tác CRUD và quản lý phân quyền người dùng.
 */
public class NhanVienDAO {

    /**
     * Tìm nhân viên theo tên tài khoản đăng nhập (liên kết 1-1).
     */
    public NhanVien findByTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT nv.*, tk.matKhau, tk.vaiTro, tk.trangThaiHoatDong " +
                     "FROM NhanVien nv " +
                     "LEFT JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
                     "WHERE nv.tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNhanVien(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm nhân viên theo mã nhân viên.
     */
    public NhanVien findByMaNhanVien(String maNhanVien) throws SQLException {
        String sql = "SELECT nv.*, tk.matKhau, tk.vaiTro, tk.trangThaiHoatDong " +
                     "FROM NhanVien nv " +
                     "LEFT JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap " +
                     "WHERE nv.maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNhanVien(rs);
                }
            }
        }
        return null;
    }

    /**
     * Lấy danh sách toàn bộ nhân viên trong hệ thống.
     */
    public List<NhanVien> getAllNhanVien() throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT nv.*, tk.matKhau, tk.vaiTro, tk.trangThaiHoatDong " +
                     "FROM NhanVien nv " +
                     "LEFT JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap ORDER BY nv.maNhanVien ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        }
        return list;
    }

    /**
     * Thêm nhân viên mới kèm tài khoản (Transaction JDBC an toàn).
     */
    public boolean insertNhanVien(NhanVien nv, String matKhau, String vaiTro) throws SQLException {
        String sqlTK = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiHoatDong) VALUES (?, ?, ?, 1)";
        String sqlNV = "INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, tenDangNhap) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String tenDN = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getTenDangNhap() : nv.getMaNhanVien().toLowerCase();

            try (PreparedStatement psTK = conn.prepareStatement(sqlTK)) {
                psTK.setString(1, tenDN);
                psTK.setString(2, matKhau != null ? matKhau : "123456");
                psTK.setString(3, vaiTro != null ? vaiTro : "NVTN");
                psTK.executeUpdate();
            }

            try (PreparedStatement psNV = conn.prepareStatement(sqlNV)) {
                psNV.setString(1, nv.getMaNhanVien());
                psNV.setString(2, nv.getHoTen());
                psNV.setString(3, nv.getChucVu());
                psNV.setString(4, nv.getSoDienThoai());
                psNV.setString(5, tenDN);
                psNV.executeUpdate();
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
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật thông tin nhân viên (họ tên, chức vụ, số điện thoại)
     */
    public boolean updateNhanVien(NhanVien nv) throws SQLException {
        String sql = "UPDATE NhanVien SET hoTen = ?, chucVu = ?, soDienThoai = ? WHERE maNhanVien = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nv.getHoTen());
            ps.setString(2, nv.getChucVu());
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getMaNhanVien());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa nhân viên và tài khoản liên kết (Transaction an toàn)
     */
    public boolean deleteNhanVien(String maNhanVien) throws SQLException {
        NhanVien nv = findByMaNhanVien(maNhanVien);
        if (nv == null) return false;

        String sqlDelNV = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        String sqlDelTK = "DELETE FROM TaiKhoan WHERE tenDangNhap = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlDelNV)) {
                ps1.setString(1, maNhanVien);
                ps1.executeUpdate();
            }

            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getTenDangNhap() != null) {
                try (PreparedStatement ps2 = conn.prepareStatement(sqlDelTK)) {
                    ps2.setString(1, nv.getTaiKhoan().getTenDangNhap());
                    ps2.executeUpdate();
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
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật vai trò tài khoản nhân viên.
     */
    public boolean updateVaiTro(String tenDangNhap, String vaiTroMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET vaiTro = ? WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTroMoi);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đặt lại mật khẩu tài khoản nhân viên.
     */
    public boolean resetMatKhau(String tenDangNhap, String matKhauMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoi);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động của tài khoản (Khóa/Mở khóa).
     */
    public boolean toggleTrangThai(String tenDangNhap, boolean trangThaiMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET trangThaiHoatDong = ? WHERE tenDangNhap = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setString(2, tenDangNhap);
            return ps.executeUpdate() > 0;
        }
    }

    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        TaiKhoan tk = null;
        if (rs.getString("tenDangNhap") != null) {
            tk = new TaiKhoan(
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro"),
                    rs.getBoolean("trangThaiHoatDong")
            );
        }
        return new NhanVien(
                rs.getString("maNhanVien"),
                rs.getString("hoTen"),
                rs.getString("chucVu"),
                rs.getString("soDienThoai"),
                tk
        );
    }
}
