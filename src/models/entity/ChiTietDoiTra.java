package models.entity;

import java.math.BigDecimal;

/**
 * Thực thể Chi tiết đổi trả mặt hàng
 */
public class ChiTietDoiTra {
    private String maChiTietDoiTra;
    private PhieuDoiTra phieuDoiTra;
    private SanPham sanPham;
    private int soLuongDoiTra;
    private BigDecimal tienHoanLai;

    public ChiTietDoiTra() {
        this.soLuongDoiTra = 1;
        this.tienHoanLai = BigDecimal.ZERO;
    }

    public ChiTietDoiTra(String maChiTietDoiTra, PhieuDoiTra phieuDoiTra, SanPham sanPham, int soLuongDoiTra, BigDecimal tienHoanLai) {
        this.maChiTietDoiTra = maChiTietDoiTra;
        this.phieuDoiTra = phieuDoiTra;
        this.sanPham = sanPham;
        this.soLuongDoiTra = soLuongDoiTra;
        this.tienHoanLai = tienHoanLai != null ? tienHoanLai : BigDecimal.ZERO;
    }

    public String getMaChiTietDoiTra() {
        return maChiTietDoiTra;
    }

    public void setMaChiTietDoiTra(String maChiTietDoiTra) {
        this.maChiTietDoiTra = maChiTietDoiTra;
    }

    public PhieuDoiTra getPhieuDoiTra() {
        return phieuDoiTra;
    }

    public void setPhieuDoiTra(PhieuDoiTra phieuDoiTra) {
        this.phieuDoiTra = phieuDoiTra;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public int getSoLuongDoiTra() {
        return soLuongDoiTra;
    }

    public void setSoLuongDoiTra(int soLuongDoiTra) {
        this.soLuongDoiTra = soLuongDoiTra;
    }

    public BigDecimal getTienHoanLai() {
        return tienHoanLai;
    }

    public void setTienHoanLai(BigDecimal tienHoanLai) {
        this.tienHoanLai = tienHoanLai;
    }

    @Override
    public String toString() {
        return "ChiTietDoiTra{" +
                "maChiTietDoiTra='" + maChiTietDoiTra + '\'' +
                ", sanPham=" + (sanPham != null ? sanPham.getTenSanPham() : "null") +
                ", soLuongDoiTra=" + soLuongDoiTra +
                ", tienHoanLai=" + tienHoanLai +
                '}';
    }
}
