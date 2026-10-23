package models.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Thực thể Lô hàng (Quản lý hạn sử dụng & xuất kho theo FEFO)
 */
public class LoHang {
    private String soLo;
    private SanPham sanPham;
    private NhaCungCap nhaCungCap;
    private LocalDate ngaySanXuat;
    private LocalDate hanSuDung;
    private BigDecimal donGiaNhapCoSo;
    private int soLuongNhap;
    private int soLuongTon;

    public LoHang() {
        this.donGiaNhapCoSo = BigDecimal.ZERO;
        this.soLuongNhap = 0;
        this.soLuongTon = 0;
    }

    public LoHang(String soLo, SanPham sanPham, NhaCungCap nhaCungCap, LocalDate ngaySanXuat,
                  LocalDate hanSuDung, BigDecimal donGiaNhapCoSo, int soLuongNhap, int soLuongTon) {
        this.soLo = soLo;
        this.sanPham = sanPham;
        this.nhaCungCap = nhaCungCap;
        this.ngaySanXuat = ngaySanXuat;
        this.hanSuDung = hanSuDung;
        this.donGiaNhapCoSo = donGiaNhapCoSo != null ? donGiaNhapCoSo : BigDecimal.ZERO;
        this.soLuongNhap = soLuongNhap;
        this.soLuongTon = soLuongTon;
    }

    public String getSoLo() {
        return soLo;
    }

    public void setSoLo(String soLo) {
        this.soLo = soLo;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public NhaCungCap getNhaCungCap() {
        return nhaCungCap;
    }

    public void setNhaCungCap(NhaCungCap nhaCungCap) {
        this.nhaCungCap = nhaCungCap;
    }

    public LocalDate getNgaySanXuat() {
        return ngaySanXuat;
    }

    public void setNgaySanXuat(LocalDate ngaySanXuat) {
        this.ngaySanXuat = ngaySanXuat;
    }

    public LocalDate getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDate hanSuDung) {
        this.hanSuDung = hanSuDung;
    }

    public BigDecimal getDonGiaNhapCoSo() {
        return donGiaNhapCoSo;
    }

    public void setDonGiaNhapCoSo(BigDecimal donGiaNhapCoSo) {
        this.donGiaNhapCoSo = donGiaNhapCoSo;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public int getSoLuongTon() {
        return soLuongTon;
    }

    public void setSoLuongTon(int soLuongTon) {
        this.soLuongTon = soLuongTon;
    }

    /**
     * Kiểm tra lô hàng đã quá hạn sử dụng hay chưa
     */
    public boolean isHetHan() {
        return hanSuDung != null && hanSuDung.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "LoHang{" +
                "soLo='" + soLo + '\'' +
                ", sanPham=" + (sanPham != null ? sanPham.getTenSanPham() : "null") +
                ", hanSuDung=" + hanSuDung +
                ", soLuongTon=" + soLuongTon +
                '}';
    }
}
