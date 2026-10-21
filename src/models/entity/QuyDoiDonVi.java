package models.entity;

import java.math.BigDecimal;

/**
 * Thực thể Quy đổi đơn vị tính & Giá bán theo từng quy cách
 */
public class QuyDoiDonVi {
    private String maQuyDoi;
    private SanPham sanPham;
    private DonViTinh donViTinh;
    private int tyLeQuyDoi; // Tỷ lệ quy về đơn vị cơ sở (VD: Thùng 24 lon -> 24)
    private boolean laDonViCoSo;
    private BigDecimal giaBan;

    public QuyDoiDonVi() {
        this.tyLeQuyDoi = 1;
        this.laDonViCoSo = false;
        this.giaBan = BigDecimal.ZERO;
    }

    public QuyDoiDonVi(String maQuyDoi, SanPham sanPham, DonViTinh donViTinh, int tyLeQuyDoi, boolean laDonViCoSo, BigDecimal giaBan) {
        this.maQuyDoi = maQuyDoi;
        this.sanPham = sanPham;
        this.donViTinh = donViTinh;
        this.tyLeQuyDoi = tyLeQuyDoi;
        this.laDonViCoSo = laDonViCoSo;
        this.giaBan = giaBan != null ? giaBan : BigDecimal.ZERO;
    }

    public String getMaQuyDoi() {
        return maQuyDoi;
    }

    public void setMaQuyDoi(String maQuyDoi) {
        this.maQuyDoi = maQuyDoi;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public DonViTinh getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(DonViTinh donViTinh) {
        this.donViTinh = donViTinh;
    }

    public int getTyLeQuyDoi() {
        return tyLeQuyDoi;
    }

    public void setTyLeQuyDoi(int tyLeQuyDoi) {
        this.tyLeQuyDoi = tyLeQuyDoi;
    }

    public boolean isLaDonViCoSo() {
        return laDonViCoSo;
    }

    public void setLaDonViCoSo(boolean laDonViCoSo) {
        this.laDonViCoSo = laDonViCoSo;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    @Override
    public String toString() {
        return "QuyDoiDonVi{" +
                "maQuyDoi='" + maQuyDoi + '\'' +
                ", donViTinh=" + (donViTinh != null ? donViTinh.getTenDonVi() : "null") +
                ", tyLeQuyDoi=" + tyLeQuyDoi +
                ", giaBan=" + giaBan +
                '}';
    }
}
