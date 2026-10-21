package models.entity;

import java.time.LocalDate;

/**
 * Thực thể Hồ sơ thú cưng (Liên kết sở hữu giữa Khách hàng và Thú cưng)
 */
public class HoSoThuCung {
    private String maHoSo;
    private KhachHang khachHang;
    private ThuCung thuCung;
    private String soMicrochip;
    private LocalDate ngayDangKy;
    private String trangThaiNuoi; // Đang nuôi, Đã chuyển chủ, Đã mất

    public HoSoThuCung() {
        this.trangThaiNuoi = "Đang nuôi";
    }

    public HoSoThuCung(String maHoSo, KhachHang khachHang, ThuCung thuCung, String soMicrochip, LocalDate ngayDangKy, String trangThaiNuoi) {
        this.maHoSo = maHoSo;
        this.khachHang = khachHang;
        this.thuCung = thuCung;
        this.soMicrochip = soMicrochip;
        this.ngayDangKy = ngayDangKy;
        this.trangThaiNuoi = trangThaiNuoi;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = maHoSo;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public String getSoMicrochip() {
        return soMicrochip;
    }

    public void setSoMicrochip(String soMicrochip) {
        this.soMicrochip = soMicrochip;
    }

    public LocalDate getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDate ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public String getTrangThaiNuoi() {
        return trangThaiNuoi;
    }

    public void setTrangThaiNuoi(String trangThaiNuoi) {
        this.trangThaiNuoi = trangThaiNuoi;
    }

    @Override
    public String toString() {
        return "HoSoThuCung{" +
                "maHoSo='" + maHoSo + '\'' +
                ", khachHang=" + (khachHang != null ? khachHang.getHoTen() : "null") +
                ", thuCung=" + (thuCung != null ? thuCung.getTenThuCung() : "null") +
                ", soMicrochip='" + soMicrochip + '\'' +
                ", trangThaiNuoi='" + trangThaiNuoi + '\'' +
                '}';
    }
}
