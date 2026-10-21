package models.entity;

import java.time.LocalDate;

/**
 * Thực thể Thú cưng
 */
public class ThuCung {
    private String maThuCung;
    private String tenThuCung;
    private String giongLoai;
    private LocalDate ngaySinh;
    private String gioiTinh; // Đực, Cái

    public ThuCung() {}

    public ThuCung(String maThuCung, String tenThuCung, String giongLoai, LocalDate ngaySinh, String gioiTinh) {
        this.maThuCung = maThuCung;
        this.tenThuCung = tenThuCung;
        this.giongLoai = giongLoai;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
    }

    public String getMaThuCung() {
        return maThuCung;
    }

    public void setMaThuCung(String maThuCung) {
        this.maThuCung = maThuCung;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getGiongLoai() {
        return giongLoai;
    }

    public void setGiongLoai(String giongLoai) {
        this.giongLoai = giongLoai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    @Override
    public String toString() {
        return "ThuCung{" +
                "maThuCung='" + maThuCung + '\'' +
                ", tenThuCung='" + tenThuCung + '\'' +
                ", giongLoai='" + giongLoai + '\'' +
                ", gioiTinh='" + gioiTinh + '\'' +
                '}';
    }
}
