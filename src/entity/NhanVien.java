package entity;

/**
 * Thực thể Nhân viên (NhanVien).
 * - maNhanVien: Tự động tăng (NV001, NV002, ...)
 * - hoTen: Họ và tên nhân viên
 * - chucVu: Chức vụ đảm nhiệm
 * - soDienThoai: Số điện thoại (dùng để liên lạc và đăng nhập)
 * - email: Hòm thư công việc (nhận thông báo và OTP)
 * - taiKhoan: Tài khoản liên kết 1-1
 */
public class NhanVien {
    private String maNhanVien;
    private String hoTen;
    private String chucVu;
    private String soDienThoai;
    private String email;
    private TaiKhoan taiKhoan;

    public NhanVien() {}

    public NhanVien(String maNhanVien, String hoTen, String chucVu, String soDienThoai, String email, TaiKhoan taiKhoan) {
        this.maNhanVien = maNhanVien;
        this.hoTen = hoTen;
        this.chucVu = chucVu;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.taiKhoan = taiKhoan;
    }

    public NhanVien(String maNhanVien, String hoTen, String chucVu, String soDienThoai, TaiKhoan taiKhoan) {
        this(maNhanVien, hoTen, chucVu, soDienThoai, "", taiKhoan);
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getChucVu() {
        return chucVu;
    }

    public void setChucVu(String chucVu) {
        this.chucVu = chucVu;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    @Override
    public String toString() {
        return "NhanVien{" +
                "maNhanVien='" + maNhanVien + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", chucVu='" + chucVu + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
