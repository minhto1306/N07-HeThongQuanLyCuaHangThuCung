package dao;

import entity.NhanVien;
import entity.TaiKhoan;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Nhân viên (NhanVien) và Tài khoản (TaiKhoan).
 * Quản lý liên kết 1-1 giữa NhanVien.maTaiKhoan và TaiKhoan.maTaiKhoan.
 * Đăng nhập bằng số điện thoại, mật khẩu băm SHA-256.
 */
public class NhanVienDAO {

    public String sinhMaNhanVienMoi() {
        String sql = "SELECT dbo.fn_SinhMaNhanVien()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
        } catch (SQLException ignored) {}
        return "NV001";
    }

    public String taoMaNhanVienMoi() {
        return sinhMaNhanVienMoi();
    }

    private static final String SELECT_BASE =
            "SELECT nv.maNhanVien, nv.hoTen, nv.chucVu, nv.soDienThoai, nv.email AS nvEmail, nv.maTaiKhoan, " +
            "       tk.soDienThoai AS tkSdt, tk.email AS tkEmail, tk.matKhau, tk.vaiTro, tk.trangThaiHoatDong " +
            "FROM NhanVien nv " +
            "LEFT JOIN TaiKhoan tk ON nv.maTaiKhoan = tk.maTaiKhoan ";

    /**
     * Tìm nhân viên theo số điện thoại (dùng để xác thực đăng nhập hoặc tra cứu).
     */
    public NhanVien findBySoDienThoai(String soDienThoai) throws SQLException {
        String sql = SELECT_BASE + "WHERE nv.soDienThoai = ? OR tk.soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            ps.setString(2, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNhanVien(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm nhân viên theo tên tài khoản đăng nhập cũ (hỗ trợ tương thích ngược: tìm theo SĐT hoặc mã NV).
     */
    public NhanVien findByTenDangNhap(String tenDangNhap) throws SQLException {
        NhanVien nv = findBySoDienThoai(tenDangNhap);
        if (nv == null) {
            nv = findByMaNhanVien(tenDangNhap);
        }
        return nv;
    }

    /**
     * Tìm nhân viên theo mã nhân viên (NV001, NV002, ...).
     */
    public NhanVien findByMaNhanVien(String maNhanVien) throws SQLException {
        String sql = SELECT_BASE + "WHERE nv.maNhanVien = ?";
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
     * Lấy danh sách toàn bộ nhân viên trong hệ thống sắp xếp theo mã tăng dần.
     */
    public List<NhanVien> getAllNhanVien() throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY nv.maNhanVien ASC";
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
     * Tự động sinh mã NVxxx qua T-SQL Function fn_SinhMaNhanVien nếu chưa có.
     */
    public boolean insertNhanVien(NhanVien nv, String matKhauHash, String vaiTro) throws SQLException {
        String sqlTK = "INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) " +
                       "VALUES (?, ?, ?, dbo.fn_HashPassword(?), ?, 1)";
        String sqlNV = "INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, email, maTaiKhoan) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String maNV = nv.getMaNhanVien();
            if (maNV == null || maNV.trim().isEmpty()) {
                maNV = sinhMaNhanVienMoi();
                nv.setMaNhanVien(maNV);
            }

            String email = (nv.getEmail() != null && !nv.getEmail().trim().isEmpty())
                    ? nv.getEmail().trim()
                    : maNV.toLowerCase() + "@mio.vn";
            String sdt = nv.getSoDienThoai() != null ? nv.getSoDienThoai().trim() : "";
            String passHash = (matKhauHash != null && !matKhauHash.trim().isEmpty())
                    ? matKhauHash.trim()
                    : "123456";
            String role = (vaiTro != null && !vaiTro.trim().isEmpty()) ? vaiTro.trim() : "NVTN";

            // 1. Thêm bản ghi TaiKhoan
            try (PreparedStatement psTK = conn.prepareStatement(sqlTK)) {
                psTK.setString(1, maNV);
                psTK.setString(2, sdt);
                psTK.setString(3, email);
                psTK.setString(4, passHash);
                psTK.setString(5, role);
                psTK.executeUpdate();
            }

            // 2. Thêm bản ghi NhanVien
            try (PreparedStatement psNV = conn.prepareStatement(sqlNV)) {
                psNV.setString(1, maNV);
                psNV.setString(2, nv.getHoTen());
                psNV.setString(3, nv.getChucVu());
                psNV.setString(4, sdt);
                psNV.setString(5, email);
                psNV.setString(6, maNV);
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
     * Cập nhật thông tin nhân viên (họ tên, chức vụ, số điện thoại, email)
     */
    public boolean updateNhanVien(NhanVien nv) throws SQLException {
        String sqlNV = "UPDATE NhanVien SET hoTen = ?, chucVu = ?, soDienThoai = ?, email = ? WHERE maNhanVien = ?";
        String sqlTK = "UPDATE TaiKhoan SET soDienThoai = ?, email = ? WHERE maTaiKhoan = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {
                ps.setString(1, nv.getHoTen());
                ps.setString(2, nv.getChucVu());
                ps.setString(3, nv.getSoDienThoai());
                ps.setString(4, nv.getEmail());
                ps.setString(5, nv.getMaNhanVien());
                ps.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlTK)) {
                ps2.setString(1, nv.getSoDienThoai());
                ps2.setString(2, nv.getEmail());
                ps2.setString(3, nv.getMaNhanVien());
                ps2.executeUpdate();
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
     * Xóa nhân viên và tài khoản liên kết (Transaction an toàn)
     */
    public boolean deleteNhanVien(String maNhanVien) throws SQLException {
        String sqlDelNV = "DELETE FROM NhanVien WHERE maNhanVien = ?";
        String sqlDelTK = "DELETE FROM TaiKhoan WHERE maTaiKhoan = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps1 = conn.prepareStatement(sqlDelNV)) {
                ps1.setString(1, maNhanVien);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlDelTK)) {
                ps2.setString(1, maNhanVien);
                ps2.executeUpdate();
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
    public boolean updateVaiTro(String maNVHoacSdt, String vaiTroMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET vaiTro = ? WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTroMoi);
            ps.setString(2, maNVHoacSdt);
            ps.setString(3, maNVHoacSdt);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đặt lại mật khẩu tài khoản nhân viên (nhận mã hash SHA-256).
     */
    public boolean resetMatKhau(String maNVHoacSdt, String matKhauMoiRaw) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = dbo.fn_HashPassword(?) WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoiRaw);
            ps.setString(2, maNVHoacSdt);
            ps.setString(3, maNVHoacSdt);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động của tài khoản (Khóa/Mở khóa).
     */
    public boolean toggleTrangThai(String maNVHoacSdt, boolean trangThaiMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET trangThaiHoatDong = ? WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setString(2, maNVHoacSdt);
            ps.setString(3, maNVHoacSdt);
            return ps.executeUpdate() > 0;
        }
    }

    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        String maTK = rs.getString("maTaiKhoan");
        String sdt = rs.getString("soDienThoai");
        String email = rs.getString("nvEmail");
        if (email == null) email = rs.getString("tkEmail");

        TaiKhoan tk = null;
        if (maTK != null) {
            tk = new TaiKhoan(
                    maTK,
                    rs.getString("tkSdt") != null ? rs.getString("tkSdt") : sdt,
                    email,
                    rs.getString("matKhau"),
                    rs.getString("vaiTro"),
                    rs.getBoolean("trangThaiHoatDong")
            );
        }
        return new NhanVien(
                rs.getString("maNhanVien"),
                rs.getString("hoTen"),
                rs.getString("chucVu"),
                sdt,
                email,
                tk
        );
    }
}
