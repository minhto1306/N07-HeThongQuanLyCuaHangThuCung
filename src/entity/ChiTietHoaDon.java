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
    protected BigDecimal donGiaLucBan;
    protected BigDecimal thanhTien;

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
        this.soLuong = Math.max(1, soLuong);
        this.thanhTien = tinhThanhTien();
    }

    public BigDecimal getDonGiaLucBan() {
        return donGiaLucBan;
    }

    public void setDonGiaLucBan(BigDecimal donGiaLucBan) {
        this.donGiaLucBan = donGiaLucBan != null ? donGiaLucBan : BigDecimal.ZERO;
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

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
        if ((this.donGiaLucBan == null || this.donGiaLucBan.compareTo(BigDecimal.ZERO) <= 0) && sanPham != null) {
            apDungGiaNiemYet();
        }
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
        if ((this.donGiaLucBan == null || this.donGiaLucBan.compareTo(BigDecimal.ZERO) <= 0) && dichVu != null) {
            apDungGiaNiemYet();
        }
    }

    /**
     * Tự động áp dụng giá bán niêm yết từ SanPham / QuyDoiDonVi hoặc DichVu vào donGiaLucBan,
     * đảm bảo cơ chế định giá trước (tránh nhân viên tự ý đặt giá bán tại quầy).
     */
    public void apDungGiaNiemYet() {
        if (dichVu != null && dichVu.getGiaDichVu() != null && dichVu.getGiaDichVu().compareTo(BigDecimal.ZERO) > 0) {
            this.donGiaLucBan = dichVu.getGiaDichVu();
        } else if (sanPham != null && sanPham.getGiaBanCoSo() != null && sanPham.getGiaBanCoSo().compareTo(BigDecimal.ZERO) > 0) {
            this.donGiaLucBan = sanPham.getGiaBanCoSo();
        }
        this.thanhTien = tinhThanhTien();
    }

    /**
     * Phương thức trừu tượng tính toán thành tiền cho từng loại chi tiết hóa đơn.
     * Được định nghĩa ở lớp con và được gọi ở tầng Business / Entity, không tính ở View.
     */
    public abstract BigDecimal tinhThanhTien();

    /**
     * Tên khoản mục hiển thị trên giao diện hoặc bản in hóa đơn.
     */
    public abstract String getTenKhoanMuc();

    /**
     * Phân loại chi tiết: "SAN_PHAM" hoặc "DICH_VU".
     */
    public abstract String getLoaiChiTiet();
}

