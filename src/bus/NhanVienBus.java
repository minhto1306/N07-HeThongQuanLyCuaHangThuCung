package bus;

import dao.NhanVienDAO;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.MaTuSinhHelper;
import utils.PasswordHasher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối quản lý Nhân viên & Phân quyền.
 * Kết nối trực tiếp 100% CSDL SQL Server qua NhanVienDAO.
 */
public class NhanVienBus {

    private final NhanVienDAO nhanVienDAO;

    public NhanVienBus() {
        this.nhanVienDAO = new NhanVienDAO();
    }

    /**
     * Lấy toàn bộ danh sách nhân viên kèm thông tin tài khoản từ CSDL SQL Server.
     */
    public List<NhanVien> layDanhSachNhanVien() {
        try {
            return nhanVienDAO.getAllNhanVien();
        } catch (SQLException e) {
            System.err.println("[NhanVienBus] Lỗi đọc Nhân viên từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thêm nhân viên mới đầy đủ thông tin: Mã tự sinh NVxxx, mật khẩu băm PBKDF2.
     */
    public boolean themNhanVien(String hoTen, String chucVu, String soDienThoai, String email,
                                String matKhau, String vaiTro) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thêm nhân viên mới!");
        }
        if (matKhau == null || BootstrapPasswordPolicy.invalid(matKhau)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và chữ số");
        }
        String maNV = MaTuSinhHelper.sinhMaNhanVien();
        String passHash = PasswordHasher.hashPBKDF2(matKhau.trim());
        String role = (vaiTro != null && !vaiTro.trim().isEmpty()) ? vaiTro.trim() : "NVTN";
        String sdt = (soDienThoai != null) ? soDienThoai.trim() : "";
        String mail = (email != null && !email.trim().isEmpty()) ? email.trim() : maNV.toLowerCase() + "@mio.vn";

        TaiKhoan tk = new TaiKhoan(maNV, sdt, mail, passHash, role, true);
        NhanVien nv = new NhanVien(maNV, hoTen, chucVu, sdt, mail, tk);

        try {
            return nhanVienDAO.insertNhanVien(nv, passHash, role);
        } catch (SQLException e) {
            System.err.println("[NhanVienBus] Lỗi insert CSDL: " + e.getMessage());
            return false;
        }
    }


    /**
     * Đổi vai trò tài khoản nhân viên.
     */
    public boolean doiVaiTro(String maNVHoacSdt, String vaiTroMoi) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền thay đổi vai trò tài khoản!");
        }
        try {
            return nhanVienDAO.updateVaiTro(maNVHoacSdt, vaiTroMoi);
        } catch (SQLException e) {
            System.err.println("[NhanVienBus] Lỗi update vai trò CSDL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cấp lại mật khẩu (mật khẩu mới được tự động băm PBKDF2 trước khi lưu).
     */
    public boolean resetMatKhau(String maNVHoacSdt, String matKhauMoi) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền cấp lại mật khẩu tài khoản!");
        }
        if (matKhauMoi == null || BootstrapPasswordPolicy.invalid(matKhauMoi)) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và chữ số");
        }
        String hash = PasswordHasher.hashPBKDF2(matKhauMoi.trim());
        try {
            return nhanVienDAO.resetMatKhau(maNVHoacSdt, hash);
        } catch (SQLException e) {
            System.err.println("[NhanVienBus] Lỗi reset mật khẩu CSDL: " + e.getMessage());
            return false;
        }
    }

    private static final class BootstrapPasswordPolicy {
        private static boolean invalid(String value) {
            if (value == null || value.length() < 8) return true;
            boolean hoa = false, thuong = false, so = false;
            for (char c : value.toCharArray()) {
                hoa |= Character.isUpperCase(c);
                thuong |= Character.isLowerCase(c);
                so |= Character.isDigit(c);
            }
            return !(hoa && thuong && so);
        }
    }

    /**
     * Bật/tắt trạng thái hoạt động (Khóa hoặc Kích hoạt).
     */
    public boolean toggleTrangThai(String maNVHoacSdt) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền khóa/kích hoạt tài khoản!");
        }
        for (NhanVien nv : layDanhSachNhanVien()) {
            boolean match = nv.getMaNhanVien().equalsIgnoreCase(maNVHoacSdt) ||
                            nv.getSoDienThoai().equalsIgnoreCase(maNVHoacSdt) ||
                            (nv.getTaiKhoan() != null && nv.getTaiKhoan().getMaTaiKhoan().equalsIgnoreCase(maNVHoacSdt));
            if (match && nv.getTaiKhoan() != null) {
                boolean trangThaiMoi = !nv.getTaiKhoan().isTrangThaiHoatDong();
                try {
                    boolean ok = nhanVienDAO.toggleTrangThai(nv.getMaNhanVien(), trangThaiMoi);
                    if (ok) {
                        nv.getTaiKhoan().setTrangThaiHoatDong(trangThaiMoi);
                        return true;
                    }
                } catch (SQLException e) {
                    System.err.println("[NhanVienBus] Lỗi toggle trạng thái CSDL: " + e.getMessage());
                    return false;
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Lấy danh sách kỹ thuật viên chăm sóc đang hoạt động (cho ComboBox chỉ định KTV).
     */
    public List<NhanVien> layDanhSachKtvHoatDong() {
        List<NhanVien> list = new ArrayList<>();
        for (NhanVien nv : layDanhSachNhanVien()) {
            if (nv.getTaiKhoan() != null && "NVCS".equalsIgnoreCase(nv.getTaiKhoan().getVaiTro())
                    && nv.getTaiKhoan().isTrangThaiHoatDong()) {
                list.add(nv);
            }
        }
        return list;
    }
}
