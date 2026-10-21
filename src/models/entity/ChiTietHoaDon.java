package models.entity;

import java.math.BigDecimal;

/**
 * Lớp trừu tượng đại diện cho một khoản mục thu tiền trên Hóa đơn.
 * Áp dụng tính ĐA HÌNH (Polymorphism) trong lập trình hướng đối tượng OOP.
 */
public abstract class ChiTietHoaDon {
    protected String maChiTiet;
    protected HoaDon hoaDon;
    protected int soLuong;
    protected BigDecimal donGiaLucBan;
    protected BigDecimal thanhTien;

    public ChiTietHoaDon() {
        this.soLuong = 1;
        this.donGiaLucBan = BigDecimal.ZERO;
        this.thanhTien = BigDecimal.ZERO;
    }

    public ChiTietHoaDon(String maChiTiet, HoaDon hoaDon, int soLuong, BigDecimal donGiaLucBan) {
        this.maChiTiet = maChiTiet;
        this.hoaDon = hoaDon;
        this.soLuong = soLuong;
        this.donGiaLucBan = donGiaLucBan != null ? donGiaLucBan : BigDecimal.ZERO;
        this.thanhTien = tinhThanhTien();
    }

    public String getMaChiTiet() {
        return maChiTiet;
    }

    public void setMaChiTiet(String maChiTiet) {
        this.maChiTiet = maChiTiet;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
        this.thanhTien = tinhThanhTien();
    }

    public BigDecimal getDonGiaLucBan() {
        return donGiaLucBan;
    }

    public void setDonGiaLucBan(BigDecimal donGiaLucBan) {
        this.donGiaLucBan = donGiaLucBan;
        this.thanhTien = tinhThanhTien();
    }

    public BigDecimal getThanhTien() {
        if (this.thanhTien == null || this.thanhTien.compareTo(BigDecimal.ZERO) == 0) {
            this.thanhTien = tinhThanhTien();
        }
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    /**
     * Phương thức trừu tượng tính toán thành tiền cho từng loại chi tiết hóa đơn.
     */
    public abstract BigDecimal tinhThanhTien();

    /**
     * Tên khoản mục hiển thị trên giao diện hoặc bản in hóa đơn.
     */
    public abstract String getTenKhoanMuc();

    /**
     * Phân loại chi tiết: "XUAT_LO" hoặc "DICH_VU".
     */
    public abstract String getLoaiChiTiet();
}
