package bus;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.PasswordHasher;

import java.sql.SQLException;
import java.security.SecureRandom;

/**
 * Controller xử lý logic nghiệp vụ Đăng nhập, Xác thực bảo mật & Quên mật khẩu.
 * Đăng nhập bằng Số điện thoại nhân viên + Mật khẩu băm SHA-256.
 */
public class DangNhapBus {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

        try {
            // 1. Tìm tài khoản theo SĐT hoặc mã nhân viên
            TaiKhoan tk = taiKhoanDAO.findBySoDienThoai(sdt);
            if (tk == null && inputUser.toUpperCase().startsWith("NV")) {
                tk = taiKhoanDAO.findByMaTaiKhoan(inputUser.toUpperCase());
            }

            if (tk == null) {
                return new KetQuaDangNhap(false, "Số điện thoại / Mã nhân viên hoặc mật khẩu không chính xác!", null);
            }

            if (!tk.isTrangThaiHoatDong()) {
                return new KetQuaDangNhap(false, "Tài khoản của nhân viên này đang bị khóa hoặc ngừng hoạt động!", null);
            }

            // 2. Xác thực mật khẩu bằng constant-time verify (hỗ trợ cả PBKDF2 và SHA-256 cũ)
            boolean hopLe = PasswordHasher.verifyPassword(matKhau.trim(), tk.getMatKhau());
            if (!hopLe) {
                return new KetQuaDangNhap(false, "Mật khẩu không chính xác! Vui lòng thử lại hoặc sử dụng 'Quên mật khẩu'.", null);
            }

            // 3. Nếu mật khẩu khớp và còn là hash cũ (SHA-256), tự động nâng cấp sang PBKDF2 (khi CSDL cho phép)
            if (PasswordHasher.needsUpgrade(tk.getMatKhau())) {
                try {
                    String newPbkdf2 = PasswordHasher.hashPBKDF2(matKhau.trim());
                    boolean updated = taiKhoanDAO.doiMatKhau(tk.getMaTaiKhoan(), newPbkdf2);
                    if (updated) {
                        tk.setMatKhau(newPbkdf2);
                    }
                } catch (Exception ex) {
                    // Nếu cột matKhau trong CSDL chưa chạy migration mở rộng độ dài (<= 64 ký tự),
                    // ghi nhận log cảnh báo và không làm gián đoạn đăng nhập của người dùng
                    System.err.println("[DangNhapBus] Chưa thể tự nâng cấp hash lên PBKDF2 (cần chạy migration SQL mở rộng cột matKhau): " + ex.getMessage());
                }
            }

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
            // Bảo mật nghiêm ngặt: Phải khớp đồng thời cả Số điện thoại và Email chính thức trong CSDL
            // Tuyệt đối không cho phép fallback gửi OTP về email lạ khi chỉ biết SĐT
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
        int otp = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Đặt lại mật khẩu mới sau khi xác thực OTP thành công.
     */
    public boolean datLaiMatKhau(String soDienThoai, String matKhauMoi) {
        if (soDienThoai == null || !matKhauDuManh(matKhauMoi)) {
            return false;
        }
        String sdt = chuanHoaSoDienThoai(soDienThoai);
        String hash = PasswordHasher.hashPBKDF2(matKhauMoi.trim());
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
        if (!matKhauDuManh(matKhauMoi)) return false;

        try {
            TaiKhoan tk = taiKhoanDAO.findBySoDienThoai(maHoacSdt);
            if (tk == null) {
                tk = taiKhoanDAO.findByMaTaiKhoan(maHoacSdt);
            }
            if (tk != null && PasswordHasher.verifyPassword(matKhauCu.trim(), tk.getMatKhau())) {
                String newHash = PasswordHasher.hashPBKDF2(matKhauMoi.trim());
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

    private boolean matKhauDuManh(String value) {
        if (value == null || value.length() < 8) return false;
        boolean hoa = false, thuong = false, so = false;
        for (char c : value.toCharArray()) {
            hoa |= Character.isUpperCase(c);
            thuong |= Character.isLowerCase(c);
            so |= Character.isDigit(c);
        }
        return hoa && thuong && so;
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
