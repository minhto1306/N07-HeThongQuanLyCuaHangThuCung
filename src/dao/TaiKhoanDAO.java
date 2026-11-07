package dao;

import entity.TaiKhoan;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho Tài khoản người dùng (TaiKhoan).
 * Quản lý khóa chính tự động tăng maTaiKhoan (NV001, NV002, ...),
 * đăng nhập bằng số điện thoại (soDienThoai) và mật khẩu băm SHA-256.
 */
public class TaiKhoanDAO {

    /**
     * Tìm tài khoản theo mã tài khoản (khóa chính NVxxx).
     */
    public TaiKhoan findByMaTaiKhoan(String maTaiKhoan) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE maTaiKhoan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTaiKhoan(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm tài khoản theo số điện thoại (dùng để đăng nhập hoặc kiểm tra trùng lặp).
     */
    public TaiKhoan findBySoDienThoai(String soDienThoai) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTaiKhoan(rs);
                }
            }
        }
        return null;
    }

    /**
     * Tìm tài khoản theo Số điện thoại và Email (dùng cho chức năng Quên mật khẩu).
     */
    public TaiKhoan findBySoDienThoaiVaEmail(String soDienThoai, String email) throws SQLException {
        String sql = "SELECT * FROM TaiKhoan WHERE soDienThoai = ? AND email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTaiKhoan(rs);
                }
            }
        }
        return null;
    }

    /**
     * Phương thức tương thích ngược tìm theo tên đăng nhập (hỗ trợ cả mã tài khoản hoặc SĐT).
     */
    public TaiKhoan findByTenDangNhap(String tenDangNhap) throws SQLException {
        TaiKhoan tk = findBySoDienThoai(tenDangNhap);
        if (tk == null) {
            tk = findByMaTaiKhoan(tenDangNhap);
        }
        return tk;
    }

    /**
     * Xác thực thông tin đăng nhập bằng Số điện thoại và mật khẩu đã băm SHA-256.
     * @param soDienThoai Số điện thoại nhân viên
     * @param matKhauHash Mật khẩu băm SHA-256
     * @return TaiKhoan nếu hợp lệ và đang hoạt động, null nếu sai thông tin
     */
    public TaiKhoan kiemTraDangNhap(String identifier, String matKhauRaw) throws SQLException {
        String sql = "{CALL dbo.sp_DangNhap(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, matKhauRaw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan tk = new TaiKhoan();
                    tk.setMaTaiKhoan(rs.getString("maTaiKhoan"));
                    tk.setSoDienThoai(rs.getString("soDienThoai"));
                    tk.setEmail(rs.getString("email"));
                    tk.setVaiTro(rs.getString("vaiTro"));
                    tk.setTrangThaiHoatDong(rs.getBoolean("trangThaiHoatDong"));
                    return tk;
                }
            }
        }
        return null;
    }

    /**
     * Đổi mật khẩu tài khoản theo mã tài khoản (maTaiKhoan).
     */
    public boolean doiMatKhau(String maTaiKhoan, String matKhauMoiRaw) throws SQLException {
        String sql = "{CALL dbo.sp_DoiMatKhau(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTaiKhoan);
            ps.setString(2, matKhauMoiRaw);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đổi mật khẩu tài khoản theo Số điện thoại (dùng cho Quên mật khẩu).
     */
    public boolean doiMatKhauTheoSoDienThoai(String soDienThoai, String matKhauMoiRaw) throws SQLException {
        String sql = "UPDATE TaiKhoan SET matKhau = dbo.fn_HashPassword(?) WHERE soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matKhauMoiRaw);
            ps.setString(2, soDienThoai);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật trạng thái hoạt động (Khóa / Mở khóa).
     */
    public boolean toggleTrangThai(String maTaiKhoan, boolean trangThaiMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET trangThaiHoatDong = ? WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setString(2, maTaiKhoan);
            ps.setString(3, maTaiKhoan);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cập nhật vai trò phân quyền.
     */
    public boolean updateVaiTro(String maTaiKhoan, String vaiTroMoi) throws SQLException {
        String sql = "UPDATE TaiKhoan SET vaiTro = ? WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTroMoi);
            ps.setString(2, maTaiKhoan);
            ps.setString(3, maTaiKhoan);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Lấy toàn bộ danh sách tài khoản
     */
    public List<TaiKhoan> getAllTaiKhoan() throws SQLException {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT * FROM TaiKhoan ORDER BY maTaiKhoan ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTaiKhoan(rs));
            }
        }
        return list;
    }

    /**
     * Thêm tài khoản mới
     */
    public boolean insertTaiKhoan(TaiKhoan tk) throws SQLException {
        String sql = "INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) " +
                     "VALUES (?, ?, ?, dbo.fn_HashPassword(?), ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tk.getMaTaiKhoan());
            ps.setString(2, tk.getSoDienThoai());
            ps.setString(3, tk.getEmail());
            ps.setString(4, tk.getMatKhau());
            ps.setString(5, tk.getVaiTro());
            ps.setBoolean(6, tk.isTrangThaiHoatDong());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa tài khoản
     */
    public boolean deleteTaiKhoan(String maTaiKhoan) throws SQLException {
        String sql = "DELETE FROM TaiKhoan WHERE maTaiKhoan = ? OR soDienThoai = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTaiKhoan);
            ps.setString(2, maTaiKhoan);
            return ps.executeUpdate() > 0;
        }
    }

    private TaiKhoan mapResultSetToTaiKhoan(ResultSet rs) throws SQLException {
        return new TaiKhoan(
                rs.getString("maTaiKhoan"),
                rs.getString("soDienThoai"),
                rs.getString("email"),
                rs.getString("matKhau"),
                rs.getString("vaiTro"),
                rs.getBoolean("trangThaiHoatDong")
        );
    }
}
