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

    /**
     * Xử lý xác thực đăng nhập người dùng.
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

        String user = tenDangNhap.trim().toLowerCase();
        String pass = matKhau.trim();

        // Xác thực thuần in-memory (Prototype Pure GUI - Tuyệt đối không gọi DB)
        NhanVien userMock = kiemTraTaiKhoanMacDinh(user, pass);
        if (userMock != null) {
            currentUser = userMock;
            utils.UserSession.initSession(userMock);
            return new KetQuaDangNhap(true, "Đăng nhập thành công!", userMock);
        }

        return new KetQuaDangNhap(false, "Tên đăng nhập hoặc mật khẩu không chính xác!\n(Gợi ý: admin, banhang, thungan, chamsoc - Mật khẩu: 123456)", null);
    }

    /**
     * Xác thực theo 4 vai trò chuẩn cho bản Prototype thuần GUI:
     * 1. admin -> Quản lý (NVQL)
     * 2. banhang / banhang01 -> Bán hàng (NVBH)
     * 3. thungan / thungan01 -> Thu ngân (NVTN)
     * 4. chamsoc / chamsoc01 -> Chăm sóc thú cưng (NVCS)
     */
    private NhanVien kiemTraTaiKhoanMacDinh(String user, String pass) {
        if ("admin".equals(user)) {
            TaiKhoan tk = new TaiKhoan("admin", pass, "NVQL", true);
            return new NhanVien("NV001", "Lê Khãi Minh", "Quản lý hệ thống", "0901234567", tk);
        } else if ("banhang".equals(user) || "banhang01".equals(user)) {
            TaiKhoan tk = new TaiKhoan("banhang", pass, "NVBH", true);
            return new NhanVien("NV002", "Trần Thị Mai", "Nhân viên bán hàng", "0934567890", tk);
        } else if ("thungan".equals(user) || "thungan01".equals(user)) {
            TaiKhoan tk = new TaiKhoan("thungan", pass, "NVTN", true);
            return new NhanVien("NV006", "Phạm Hoàng Nam", "Nhân viên thu ngân", "0912345678", tk);
        } else if ("chamsoc".equals(user) || "chamsoc01".equals(user) || "ktv01".equals(user)) {
            TaiKhoan tk = new TaiKhoan("chamsoc", pass, "NVCS", true);
            return new NhanVien("NV003", "Tô Thanh Đạt", "Nhân viên chăm sóc thú cưng", "0923456789", tk);
        }
        return null;
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
     * Đổi mật khẩu tài khoản người dùng hiện tại.
     */
    public boolean doiMatKhau(String tenDangNhap, String matKhauCu, String matKhauMoi) {
        if (tenDangNhap == null || matKhauCu == null || matKhauMoi == null) return false;
        if (matKhauMoi.trim().length() < 4) return false;
        if (currentUser != null && currentUser.getTaiKhoan() != null) {
            currentUser.getTaiKhoan().setMatKhau(matKhauMoi.trim());
            return true;
        }
        return true;
    }

    /**
     * Đăng xuất khỏi hệ thống.
     */
    public static void dangXuat() {
        currentUser = null;
        utils.UserSession.clearSession();
    }
}
