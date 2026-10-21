package models.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Hóa đơn thanh toán tại quầy.
 * Chứa danh sách đa hình các chi tiết (Bán hàng và Làm dịch vụ).
 */
public class HoaDon {
    private String maHoaDon;
    private KhachHang khachHang;
    private NhanVien nhanVien;
    private LocalDateTime ngayLap;
    private LocalDateTime thoiGianThanhToan;
    private BigDecimal giamGia;
    private BigDecimal thanhTien;
    private String phuongThucThanhToan; // Tiền mặt, Chuyển khoản, Thẻ
    private String trangThaiHoaDon;     // Chờ thanh toán, Đã thanh toán, Đã hủy
    private List<ChiTietHoaDon> dsChiTiet;

    public HoaDon() {
        this.ngayLap = LocalDateTime.now();
        this.giamGia = BigDecimal.ZERO;
        this.thanhTien = BigDecimal.ZERO;
        this.trangThaiHoaDon = "Chờ thanh toán";
        this.dsChiTiet = new ArrayList<>();
    }

    public HoaDon(String maHoaDon, KhachHang khachHang, NhanVien nhanVien, LocalDateTime ngayLap,
                  BigDecimal giamGia, String phuongThucThanhToan, String trangThaiHoaDon) {
        this.maHoaDon = maHoaDon;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
        this.ngayLap = ngayLap != null ? ngayLap : LocalDateTime.now();
        this.giamGia = giamGia != null ? giamGia : BigDecimal.ZERO;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThaiHoaDon = trangThaiHoaDon != null ? trangThaiHoaDon : "Chờ thanh toán";
        this.dsChiTiet = new ArrayList<>();
        this.thanhTien = BigDecimal.ZERO;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return thoiGianThanhToan;
    }

    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) {
        this.thoiGianThanhToan = thoiGianThanhToan;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = giamGia != null ? giamGia : BigDecimal.ZERO;
        capNhatTongTien();
    }

    public BigDecimal getThanhTien() {
        if (this.thanhTien == null || this.thanhTien.compareTo(BigDecimal.ZERO) == 0) {
            capNhatTongTien();
        }
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getTrangThaiHoaDon() {
        return trangThaiHoaDon;
    }

    public void setTrangThaiHoaDon(String trangThaiHoaDon) {
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

    public List<ChiTietHoaDon> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
        capNhatTongTien();
    }

    public void addChiTiet(ChiTietHoaDon chiTiet) {
        this.dsChiTiet.add(chiTiet);
        capNhatTongTien();
    }

    public void removeChiTiet(ChiTietHoaDon chiTiet) {
        this.dsChiTiet.remove(chiTiet);
        capNhatTongTien();
    }

    /**
     * Tự động tính toán tổng thành tiền của hóa đơn:
     * Tổng tiền = (Tổng thành tiền các dòng chi tiết) - Giảm giá.
     */
    public void capNhatTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        if (this.dsChiTiet != null) {
            for (ChiTietHoaDon ct : this.dsChiTiet) {
                if (ct.getThanhTien() != null) {
                    tong = tong.add(ct.getThanhTien());
                }
            }
        }
        if (this.giamGia != null) {
            tong = tong.subtract(this.giamGia);
        }
        // Đảm bảo không âm
        this.thanhTien = tong.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : tong;
    }

    @Override
    public String toString() {
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", khachHang=" + (khachHang != null ? khachHang.getHoTen() : "Khách vãng lai") +
                ", nhanVien=" + (nhanVien != null ? nhanVien.getHoTen() : "null") +
                ", ngayLap=" + ngayLap +
                ", soDong=" + (dsChiTiet != null ? dsChiTiet.size() : 0) +
                ", thanhTien=" + thanhTien +
                ", trangThai='" + trangThaiHoaDon + '\'' +
                '}';
    }
}
