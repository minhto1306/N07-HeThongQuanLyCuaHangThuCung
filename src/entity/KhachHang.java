package entity;

// Entity khách hàng, mới bổ sung thêm trường email với địa chỉ
// để phục vụ bên gửi email xác nhận đặt lịch hẹn với thông báo chăm sóc thú cưng ở pet hotel
public class KhachHang {
    private String maKhachHang;
    private String hoTen;
    private String soDienThoai;
    private int diemTichLuy;
    private String email;   // thêm để gửi mail thông báo lịch hẹn và tiêm phòng
    private String diaChi;  // lưu địa chỉ nhà khách khi cần đón thú cưng

    public KhachHang() {
        this.diemTichLuy = 0;
        this.email = "";
        this.diaChi = "";
    }

    // constructor cũ 4 tham số giữ nguyên để mấy chỗ bên POS với Hóa Đơn gọi không bị lỗi
    public KhachHang(String maKhachHang, String hoTen, String soDienThoai, int diemTichLuy) {
        this(maKhachHang, hoTen, soDienThoai, diemTichLuy, "", "");
    }

    // constructor đầy đủ 6 tham số dùng cho phần quản lý khách hàng và gửi mail
    public KhachHang(String maKhachHang, String hoTen, String soDienThoai, int diemTichLuy, String email, String diaChi) {
        this.maKhachHang = maKhachHang;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.diemTichLuy = diemTichLuy;
        this.email = email != null ? email : "";
        this.diaChi = diaChi != null ? diaChi : "";
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public int getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(int diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email : "";
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi != null ? diaChi : "";
    }

    @Override
    public String toString() {
        return "KhachHang{" +
                "maKhachHang='" + maKhachHang + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", soDienThoai='" + soDienThoai + '\'' +
                ", diemTichLuy=" + diemTichLuy +
                ", email='" + email + '\'' +
                ", diaChi='" + diaChi + '\'' +
                '}';
    }
}
