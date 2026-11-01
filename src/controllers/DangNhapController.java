package controllers;

import models.dao.NhanVienDAO;
import models.dao.TaiKhoanDAO;
import models.entity.NhanVien;
import models.entity.TaiKhoan;

import java.sql.SQLException;

/**
 * Controller xử lý logic nghiệp vụ Đăng nhập & Quản lý phiên làm việc của người dùng.
 */
public class DangNhapController {

    private final TaiKhoanDAO taiKhoanDAO;
    private final NhanVienDAO nhanVienDAO;
    private static NhanVien currentUser = null;

    public DangNhapController() {
        this.taiKhoanDAO = new TaiKhoanDAO();
        this.nhanVienDAO = new NhanVienDAO();
    }

    /**
     * Kết quả trả về sau khi xác thực đăng nhập.
     */
    public static class KetQuaDangNhap {
        private final boolean thanhCong;
        private final String thongDiep;
        private final NhanVien nhanVien;

        public KetQuaDangNhap(boolean thanhCong, String thongDiep, NhanVien nhanVien) {
            this.thanhCong = thanhCong;
            this.thongDiep = thongDiep;
            this.nhanVien = nhanVien;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getThongDiep() {
            return thongDiep;
        }

        public NhanVien getNhanVien() {
            return nhanVien;
        }
    }

    private String chuanHoaTenDangNhap(String u) {
        if (u == null) return "";
        String s = u.trim().toLowerCase();
        if ("banhang".equals(s)) return "banhang01";
        if ("thungan".equals(s)) return "thungan01";
        if ("chamsoc".equals(s) || "chamsoc01".equals(s)) return "ktv01";
        return s;
    }

    /**
     * Xử lý xác thực đăng nhập người dùng 100% từ CSDL SQL Server.
     * @param tenDangNhap Tên đăng nhập
     * @param matKhau Mật khẩu
     * @return KetQuaDangNhap chi tiết
     */
    public KetQuaDangNhap xuLyDangNhap(String tenDangNhap, String matKhau) {
        // 1. Kiểm tra dữ liệu đầu vào
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập tên đăng nhập!", null);
        }
        if (matKhau == null || matKhau.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập mật khẩu!", null);
        }

        String rawUser = tenDangNhap.trim();
        String cleanUser = chuanHoaTenDangNhap(rawUser);
        String pass = matKhau.trim();

        try {
            // Xác thực 100% qua CSDL SQL Server (TaiKhoanDAO & NhanVienDAO)
            TaiKhoan tk = taiKhoanDAO.kiemTraDangNhap(cleanUser, pass);
            if (tk != null) {
                NhanVien nv = nhanVienDAO.findByTenDangNhap(tk.getTenDangNhap());
                if (nv != null) {
                    currentUser = nv;
                    utils.UserSession.initSession(nv);
                    return new KetQuaDangNhap(true, "Đăng nhập thành công!", nv);
                } else {
                    return new KetQuaDangNhap(false, "Tài khoản [" + cleanUser + "] chưa được gán thông tin Nhân viên trong CSDL!", null);
                }
            } else {
                // Kiểm tra lý do thất bại chi tiết để hỗ trợ người dùng
                TaiKhoan tonTai = taiKhoanDAO.findByTenDangNhap(cleanUser);
                if (tonTai == null) {
                    return new KetQuaDangNhap(false, "Tên đăng nhập không tồn tại trong hệ thống!\n(Gợi ý: admin, banhang, thungan, chamsoc - Mật khẩu: 123456)", null);
                } else if (!tonTai.isTrangThaiHoatDong()) {
                    return new KetQuaDangNhap(false, "Tài khoản [" + cleanUser + "] đang bị khóa hoặc ngừng hoạt động!", null);
                } else {
                    return new KetQuaDangNhap(false, "Mật khẩu không chính xác! Vui lòng thử lại.", null);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DangNhapController] Lỗi SQL khi xác thực: " + e.getMessage());
            return new KetQuaDangNhap(false, "Không thể kết nối đến máy chủ CSDL SQL Server:\n" + e.getMessage() + "\nVui lòng kiểm tra lại dịch vụ SQL Server!", null);
        }
    }

    /**
     * Lấy người dùng đang đăng nhập trong phiên làm việc hiện tại.
     */
    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    /**
     * Gán người dùng hiện tại (dùng khi kiểm thử hoặc chuyển phiên).
     */
    public static void setCurrentUser(NhanVien nv) {
        currentUser = nv;
        utils.UserSession.initSession(nv);
    }

    /**
     * Đổi mật khẩu tài khoản người dùng hiện tại (ghi trực tiếp xuống CSDL SQL Server).
     */
    public boolean doiMatKhau(String tenDangNhap, String matKhauCu, String matKhauMoi) {
        if (tenDangNhap == null || matKhauCu == null || matKhauMoi == null) return false;
        if (matKhauMoi.trim().length() < 4) return false;
        String cleanUser = chuanHoaTenDangNhap(tenDangNhap);
        try {
            boolean ok = taiKhoanDAO.doiMatKhau(cleanUser, matKhauMoi.trim());
            if (ok && currentUser != null && currentUser.getTaiKhoan() != null) {
                currentUser.getTaiKhoan().setMatKhau(matKhauMoi.trim());
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("[DangNhapController] Lỗi đổi mật khẩu SQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đăng xuất khỏi hệ thống.
     */
    public static void dangXuat() {
        currentUser = null;
        utils.UserSession.clearSession();
    }
}
