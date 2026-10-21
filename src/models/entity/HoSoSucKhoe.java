package models.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Hồ sơ sức khỏe và kiểm tra thể trạng thú cưng
 */
public class HoSoSucKhoe {
    private String maSucKhoe;
    private ThuCung thuCung;
    private NhanVien nhanVienKiemTra;
    private BigDecimal canNang;
    private BigDecimal nhietDo;
    private String tinhTrangVeRan;
    private boolean tiemPhongDai;
    private String ghiChuTheTrang;
    private LocalDateTime ngayKiemTra;

    public HoSoSucKhoe() {
        this.ngayKiemTra = LocalDateTime.now();
    }

    public HoSoSucKhoe(String maSucKhoe, ThuCung thuCung, NhanVien nhanVienKiemTra, BigDecimal canNang,
                       BigDecimal nhietDo, String tinhTrangVeRan, boolean tiemPhongDai, String ghiChuTheTrang, LocalDateTime ngayKiemTra) {
        this.maSucKhoe = maSucKhoe;
        this.thuCung = thuCung;
        this.nhanVienKiemTra = nhanVienKiemTra;
        this.canNang = canNang;
        this.nhietDo = nhietDo;
        this.tinhTrangVeRan = tinhTrangVeRan;
        this.tiemPhongDai = tiemPhongDai;
        this.ghiChuTheTrang = ghiChuTheTrang;
        this.ngayKiemTra = ngayKiemTra;
    }

    public String getMaSucKhoe() {
        return maSucKhoe;
    }

    public void setMaSucKhoe(String maSucKhoe) {
        this.maSucKhoe = maSucKhoe;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public NhanVien getNhanVienKiemTra() {
        return nhanVienKiemTra;
    }

    public void setNhanVienKiemTra(NhanVien nhanVienKiemTra) {
        this.nhanVienKiemTra = nhanVienKiemTra;
    }

    public BigDecimal getCanNang() {
        return canNang;
    }

    public void setCanNang(BigDecimal canNang) {
        this.canNang = canNang;
    }

    public BigDecimal getNhietDo() {
        return nhietDo;
    }

    public void setNhietDo(BigDecimal nhietDo) {
        this.nhietDo = nhietDo;
    }

    public String getTinhTrangVeRan() {
        return tinhTrangVeRan;
    }

    public void setTinhTrangVeRan(String tinhTrangVeRan) {
        this.tinhTrangVeRan = tinhTrangVeRan;
    }

    public boolean isTiemPhongDai() {
        return tiemPhongDai;
    }

    public void setTiemPhongDai(boolean tiemPhongDai) {
        this.tiemPhongDai = tiemPhongDai;
    }

    public String getGhiChuTheTrang() {
        return ghiChuTheTrang;
    }

    public void setGhiChuTheTrang(String ghiChuTheTrang) {
        this.ghiChuTheTrang = ghiChuTheTrang;
    }

    public LocalDateTime getNgayKiemTra() {
        return ngayKiemTra;
    }

    public void setNgayKiemTra(LocalDateTime ngayKiemTra) {
        this.ngayKiemTra = ngayKiemTra;
    }

    @Override
    public String toString() {
        return "HoSoSucKhoe{" +
                "maSucKhoe='" + maSucKhoe + '\'' +
                ", thuCung=" + (thuCung != null ? thuCung.getTenThuCung() : "null") +
                ", canNang=" + canNang +
                ", nhietDo=" + nhietDo +
                ", tiemPhongDai=" + tiemPhongDai +
                ", ngayKiemTra=" + ngayKiemTra +
                '}';
    }
}
