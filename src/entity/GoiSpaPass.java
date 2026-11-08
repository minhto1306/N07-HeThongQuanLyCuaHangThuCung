package entity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Thực thể Cấu hình Gói Dịch vụ Spa Pass (DV14).
 * Quản lý số buổi được hưởng, đơn giá và thời hạn hiệu lực chính thức từ CSDL,
 * loại bỏ việc phân tích số buổi bằng Regex từ tên gói.
 */
public class GoiSpaPass implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maGoi;
    private String tenGoi;
    private int soBuoi;
    private BigDecimal donGiaGoi;
    private int thoiHanNgay;
    private boolean trangThaiHoatDong;

    public GoiSpaPass() {
        this.donGiaGoi = BigDecimal.ZERO;
        this.trangThaiHoatDong = true;
    }

    public GoiSpaPass(String maGoi, String tenGoi, int soBuoi, BigDecimal donGiaGoi, int thoiHanNgay, boolean trangThaiHoatDong) {
        this.maGoi = maGoi;
        this.tenGoi = tenGoi;
        this.soBuoi = soBuoi;
        this.donGiaGoi = donGiaGoi != null ? donGiaGoi : BigDecimal.ZERO;
        this.thoiHanNgay = thoiHanNgay;
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    public String getMaGoi() {
        return maGoi;
    }

    public void setMaGoi(String maGoi) {
        this.maGoi = maGoi;
    }

    public String getTenGoi() {
        return tenGoi;
    }

    public void setTenGoi(String tenGoi) {
        this.tenGoi = tenGoi;
    }

    public int getSoBuoi() {
        return soBuoi;
    }

    public void setSoBuoi(int soBuoi) {
        this.soBuoi = soBuoi;
    }

    public BigDecimal getDonGiaGoi() {
        return donGiaGoi;
    }

    public void setDonGiaGoi(BigDecimal donGiaGoi) {
        this.donGiaGoi = donGiaGoi;
    }

    public int getThoiHanNgay() {
        return thoiHanNgay;
    }

    public void setThoiHanNgay(int thoiHanNgay) {
        this.thoiHanNgay = thoiHanNgay;
    }

    public boolean isTrangThaiHoatDong() {
        return trangThaiHoatDong;
    }

    public void setTrangThaiHoatDong(boolean trangThaiHoatDong) {
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    @Override
    public String toString() {
        return tenGoi != null ? tenGoi : maGoi;
    }
}
