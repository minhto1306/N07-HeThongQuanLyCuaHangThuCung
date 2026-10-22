package models.entity;

/**
 * Thực thể Tài khoản người dùng
 */
public class TaiKhoan {
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro; // Admin, NhanVienThuNgan, KyThuatVien
    private boolean trangThaiHoatDong;

    public TaiKhoan() {
        this.trangThaiHoatDong = true;
    }

    public TaiKhoan(String tenDangNhap, String matKhau, String vaiTro, boolean trangThaiHoatDong) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
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

    @Override
    public String toString() {
        return "TaiKhoan{" +
                "tenDangNhap='" + tenDangNhap + '\'' +
                ", vaiTro='" + vaiTro + '\'' +
                ", trangThaiHoatDong=" + trangThaiHoatDong +
                '}';
    }
}
