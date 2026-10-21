package models.entity;

import java.math.BigDecimal;

/**
 * Thực thể Dịch vụ chăm sóc thú cưng
 */
public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private BigDecimal giaDichVu;
    private int thoiLuongPhut;

    public DichVu() {
        this.giaDichVu = BigDecimal.ZERO;
    }

    public DichVu(String maDichVu, String tenDichVu, BigDecimal giaDichVu, int thoiLuongPhut) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.giaDichVu = giaDichVu != null ? giaDichVu : BigDecimal.ZERO;
        this.thoiLuongPhut = thoiLuongPhut;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String tenDichVu) {
        this.tenDichVu = tenDichVu;
    }

    public BigDecimal getGiaDichVu() {
        return giaDichVu;
    }

    public void setGiaDichVu(BigDecimal giaDichVu) {
        this.giaDichVu = giaDichVu;
    }

    public int getThoiLuongPhut() {
        return thoiLuongPhut;
    }

    public void setThoiLuongPhut(int thoiLuongPhut) {
        this.thoiLuongPhut = thoiLuongPhut;
    }

    @Override
    public String toString() {
        return "DichVu{" +
                "maDichVu='" + maDichVu + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", giaDichVu=" + giaDichVu +
                ", thoiLuongPhut=" + thoiLuongPhut +
                '}';
    }
}
