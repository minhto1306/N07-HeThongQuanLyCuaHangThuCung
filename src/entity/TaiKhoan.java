package entity;

/**
 * Thực thể Tài khoản người dùng (TaiKhoan).
 * - maTaiKhoan: Khóa chính tự động tăng (NV001, NV002, ...)
 * - soDienThoai: Số điện thoại nhân viên (dùng để đăng nhập hệ thống, duy nhất)
 * - email: Hòm thư công việc (dùng để gửi OTP xác thực quên mật khẩu)
 * - matKhau: Mật khẩu bảo mật được băm bằng thuật toán SHA-256 (64 ký tự hex)
 * - vaiTro: NVQL (Quản lý), NVTN (Thu ngân), NVKHO (Thủ kho), NVCS (Chăm sóc/Spa), NVBH (Bán hàng)
 * - trangThaiHoatDong: true (Đang làm việc), false (Khóa tài khoản)
 */
public class TaiKhoan {
    private String maTaiKhoan;
    private String soDienThoai;
    private String email;
    private String matKhau;
    private String vaiTro;
    private boolean trangThaiHoatDong;

    public TaiKhoan() {
        this.trangThaiHoatDong = true;
    }

    public TaiKhoan(String maTaiKhoan, String soDienThoai, String email, String matKhau, String vaiTro, boolean trangThaiHoatDong) {
        this.maTaiKhoan = maTaiKhoan;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    // Constructor tiện ích cũ để tương thích
    public TaiKhoan(String maTaiKhoan, String matKhau, String vaiTro, boolean trangThaiHoatDong) {
        this(maTaiKhoan, "", "", matKhau, vaiTro, trangThaiHoatDong);
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public boolean isTrangThaiHoatDong() {
        return trangThaiHoatDong;
    }

    public void setTrangThaiHoatDong(boolean trangThaiHoatDong) {
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    // Getter/Setter tương thích ngược cho mã/tên tài khoản
    public String getTenDangNhap() {
        return (soDienThoai != null && !soDienThoai.isEmpty()) ? soDienThoai : maTaiKhoan;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.soDienThoai = tenDangNhap;
    }

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "maTaiKhoan='" + maTaiKhoan + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", email='" + email + '\'' +
                ", vaiTro='" + vaiTro + '\'' +
                ", trangThaiHoatDong=" + trangThaiHoatDong +
                '}';
    }
}
