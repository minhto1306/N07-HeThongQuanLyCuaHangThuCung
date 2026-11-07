package bus;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.PasswordHasher;

import java.sql.SQLException;
import java.util.Random;

/**
 * Controller xử lý logic nghiệp vụ Đăng nhập, Xác thực bảo mật & Quên mật khẩu.
 * Đăng nhập bằng Số điện thoại nhân viên + Mật khẩu băm SHA-256.
 */
public class DangNhapBus {

    private final TaiKhoanDAO taiKhoanDAO;
    private final NhanVienDAO nhanVienDAO;
    private static NhanVien currentUser = null;

    public DangNhapBus() {
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
     * Chuẩn hóa số điện thoại nhập vào (xóa dấu cách, gạch nối, định dạng quốc tế +84 -> 0).
     */
    public String chuanHoaSoDienThoai(String sdt) {
        if (sdt == null) return "";
        String s = sdt.trim().replaceAll("[^0-9+]", "");
        if (s.startsWith("+84")) {
            s = "0" + s.substring(3);
        }
        return s;
    }

    /**
     * Xử lý xác thực đăng nhập nhân viên bằng Số điện thoại và Mật khẩu.
     * Mật khẩu được tự động băm SHA-256 trước khi đối chiếu trong CSDL.
     */
    public KetQuaDangNhap xuLyDangNhap(String soDienThoaiHoacMa, String matKhau) {
        if (soDienThoaiHoacMa == null || soDienThoaiHoacMa.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập Số điện thoại nhân viên!", null);
        }
        if (matKhau == null || matKhau.trim().isEmpty()) {
            return new KetQuaDangNhap(false, "Vui lòng nhập mật khẩu!", null);
        }

        String inputUser = soDienThoaiHoacMa.trim();
        String sdt = chuanHoaSoDienThoai(inputUser);
        String passHash = PasswordHasher.hashSHA256(matKhau.trim());

        try {
            // 1. Thử xác thực theo số điện thoại
            TaiKhoan tk = taiKhoanDAO.kiemTraDangNhap(sdt, passHash);

            // 2. Nếu không tìm thấy bằng SĐT, thử xác thực bằng mã tài khoản (NVxxx) cho tương thích
            if (tk == null && inputUser.toUpperCase().startsWith("NV")) {
                TaiKhoan tkMa = taiKhoanDAO.findByMaTaiKhoan(inputUser.toUpperCase());
                if (tkMa != null && tkMa.isTrangThaiHoatDong() && passHash.equalsIgnoreCase(tkMa.getMatKhau())) {
                    tk = tkMa;
                }
            }

            if (tk != null) {
                NhanVien nv = nhanVienDAO.findByMaNhanVien(tk.getMaTaiKhoan());
                if (nv == null) {
                    nv = nhanVienDAO.findBySoDienThoai(tk.getSoDienThoai());
                }
                if (nv != null) {
                    nv.setTaiKhoan(tk);
                    currentUser = nv;
                    utils.UserSession.initSession(nv);
                    return new KetQuaDangNhap(true, "Đăng nhập thành công!", nv);
                } else {
                    return new KetQuaDangNhap(false, "Tài khoản [" + tk.getMaTaiKhoan() + "] chưa được liên kết thông tin Nhân viên!", null);
                }
            } else {
                // Kiểm tra lý do chi tiết để hỗ trợ người dùng
                TaiKhoan tonTai = taiKhoanDAO.findBySoDienThoai(sdt);
                if (tonTai == null && inputUser.toUpperCase().startsWith("NV")) {
                    tonTai = taiKhoanDAO.findByMaTaiKhoan(inputUser.toUpperCase());
                }

                if (tonTai == null) {
                    return new KetQuaDangNhap(false, "Số điện thoại / Mã nhân viên không tồn tại trong hệ thống!\n(Gợi ý SĐT mẫu: 0901234567, 0912345678 - Mật khẩu: 123456)", null);
                } else if (!tonTai.isTrangThaiHoatDong()) {
                    return new KetQuaDangNhap(false, "Tài khoản của nhân viên này đang bị khóa hoặc ngừng hoạt động!", null);
                } else {
                    return new KetQuaDangNhap(false, "Mật khẩu không chính xác! Vui lòng thử lại hoặc sử dụng 'Quên mật khẩu'.", null);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DangNhapBus] Lỗi SQL khi xác thực: " + e.getMessage());
            return new KetQuaDangNhap(false, "Không thể kết nối đến máy chủ CSDL SQL Server:\n" + e.getMessage() + "\nVui lòng kiểm tra lại dịch vụ SQL Server!", null);
        }
    }

    /**
     * Xác thực thông tin Quên mật khẩu (kiểm tra SĐT và Email có khớp trong CSDL không).
     */
    public NhanVien xacThucQuenMatKhau(String soDienThoai, String email) {
        if (soDienThoai == null || email == null) return null;
        String sdt = chuanHoaSoDienThoai(soDienThoai);
        String mail = email.trim();

        try {
            TaiKhoan tk = taiKhoanDAO.findBySoDienThoaiVaEmail(sdt, mail);
            if (tk != null && tk.isTrangThaiHoatDong()) {
                NhanVien nv = nhanVienDAO.findByMaNhanVien(tk.getMaTaiKhoan());
                if (nv != null) {
                    nv.setTaiKhoan(tk);
                    return nv;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DangNhapBus] Lỗi xacThucQuenMatKhau: " + e.getMessage());
        }
        return null;
    }

    /**
     * Sinh mã OTP ngẫu nhiên 6 chữ số phục vụ xác thực quên mật khẩu.
     */
    public String sinhMaOTP() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Đặt lại mật khẩu mới sau khi xác thực OTP thành công.
     */
    public boolean datLaiMatKhau(String soDienThoai, String matKhauMoi) {
        if (soDienThoai == null || matKhauMoi == null || matKhauMoi.trim().length() < 4) {
            return false;
        }
        String sdt = chuanHoaSoDienThoai(soDienThoai);
        String hash = PasswordHasher.hashSHA256(matKhauMoi.trim());
        try {
            return taiKhoanDAO.doiMatKhauTheoSoDienThoai(sdt, hash);
        } catch (SQLException e) {
            System.err.println("[DangNhapBus] Lỗi datLaiMatKhau: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đổi mật khẩu tài khoản người dùng hiện tại (băm SHA-256).
     */
    public boolean doiMatKhau(String maHoacSdt, String matKhauCu, String matKhauMoi) {
        if (maHoacSdt == null || matKhauCu == null || matKhauMoi == null) return false;
        if (matKhauMoi.trim().length() < 4) return false;

        String oldHash = PasswordHasher.hashSHA256(matKhauCu.trim());
        String newHash = PasswordHasher.hashSHA256(matKhauMoi.trim());

        try {
            TaiKhoan tk = taiKhoanDAO.findBySoDienThoai(maHoacSdt);
            if (tk == null) {
                tk = taiKhoanDAO.findByMaTaiKhoan(maHoacSdt);
            }
            if (tk != null && oldHash.equalsIgnoreCase(tk.getMatKhau())) {
                boolean ok = taiKhoanDAO.doiMatKhau(tk.getMaTaiKhoan(), newHash);
                if (ok && currentUser != null && currentUser.getTaiKhoan() != null) {
                    currentUser.getTaiKhoan().setMatKhau(newHash);
                }
                return ok;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("[DangNhapBus] Lỗi đổi mật khẩu SQL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy người dùng đang đăng nhập trong phiên làm việc hiện tại.
     */
    public static NhanVien getCurrentUser() {
        return currentUser;
    }

    /**
     * Gán người dùng hiện tại.
     */
    public static void setCurrentUser(NhanVien nv) {
        currentUser = nv;
        utils.UserSession.initSession(nv);
    }

    /**
     * Đăng xuất khỏi hệ thống.
     */
    public static void dangXuat() {
        currentUser = null;
        utils.UserSession.clearSession();
    }
}
