package entity;

import java.math.BigDecimal;

/**
 * Lớp trừu tượng đại diện cho một dòng chi tiết trên Hóa đơn.
 * Áp dụng tính ĐA HÌNH (Polymorphism) trong lập trình hướng đối tượng OOP:
 * - ChiTietXuatLo: Dòng xuất bán sản phẩm hàng hóa từ lô kho.
 * - ChiTietDichVu: Dòng thực hiện dịch vụ Spa/Grooming.
 *
 * Khắc phục Vấn đề 4 & 8:
 * - Liên kết trực tiếp SanPham và DichVu để xác định rõ mặt hàng/dịch vụ được bán.
 * - Cơ chế định giá trước: Tự động nạp giá bán niêm yết từ SanPham/QuyDoiDonVi/DichVu vào donGiaLucBan,
 *   chống nhân viên tự ý sửa giá bán tại quầy.
 */
public abstract class ChiTietHoaDon {
    protected String maChiTiet;
    protected HoaDon hoaDon;
    protected int soLuong;
    protected BigDecimal donGiaLucBan = BigDecimal.ZERO;
    protected BigDecimal thanhTien = BigDecimal.ZERO;

    // Liên kết trực tiếp sản phẩm hoặc dịch vụ
    protected SanPham sanPham;
    protected DichVu dichVu;

    public ChiTietHoaDon() {
        this.soLuong = 1;
        this.donGiaLucBan = BigDecimal.ZERO;
        this.thanhTien = BigDecimal.ZERO;
    }

    public ChiTietHoaDon(String maChiTiet, HoaDon hoaDon, int soLuong, BigDecimal donGiaLucBan) {
        this.maChiTiet = maChiTiet;
        this.hoaDon = hoaDon;
        this.soLuong = Math.max(1, soLuong);
        this.donGiaLucBan = donGiaLucBan != null && donGiaLucBan.compareTo(BigDecimal.ZERO) >= 0 ? donGiaLucBan : BigDecimal.ZERO;
        this.thanhTien = this.donGiaLucBan.multiply(BigDecimal.valueOf(this.soLuong));
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
        this.soLuong = Math.max(1, soLuong);
    }

    public BigDecimal getDonGiaLucBan() {
        return donGiaLucBan;
    }

    public void setDonGiaLucBan(BigDecimal donGiaLucBan) {
        this.donGiaLucBan = donGiaLucBan != null ? donGiaLucBan : BigDecimal.ZERO;
    }

    public BigDecimal getThanhTien() {
        if (this.thanhTien == null) {
            BigDecimal dg = this.donGiaLucBan != null ? this.donGiaLucBan : BigDecimal.ZERO;
            this.thanhTien = dg.multiply(BigDecimal.valueOf(Math.max(1, this.soLuong)));
        }
        return this.thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien != null && thanhTien.compareTo(BigDecimal.ZERO) >= 0 ? thanhTien : BigDecimal.ZERO;
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
// note: donGia niem yet do BanHangBus ap dung khi dua vao gio hang
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
// note: donGia niem yet do BanHangBus ap dung khi dua vao gio hang
    }

    // note lai: apDungGiaNiemYet da chuyen sang BanHangBus

    /**
     * Phương thức trừu tượng tính toán thành tiền cho từng loại chi tiết hóa đơn.
     * Được định nghĩa ở lớp con và được gọi ở tầng Business / Entity, không tính ở View.
     */
    // note lai: entity chi thuan chua thuoc tinh thanhTien va getter/setter, tinh toan da chuyen ve BanHangBus.tinhThanhTien()

    /**
     * Tên khoản mục hiển thị trên giao diện hoặc bản in hóa đơn.
     */
    public abstract String getTenKhoanMuc();

    /**
     * Phân loại chi tiết: "SAN_PHAM" hoặc "DICH_VU".
     */
    public abstract String getLoaiChiTiet();
}

