package entity;

import java.math.BigDecimal;

/**
 * Lớp con đại diện cho Dòng xuất bán sản phẩm từ một Lô hàng cụ thể.
 * Kế thừa từ ChiTietHoaDon.
 */
public class ChiTietXuatLo extends ChiTietHoaDon {
    private LoHang loHang;
    private QuyDoiDonVi quyDoiDonVi;
    private int soLuongXuatCoSo; // Số lượng quy đổi về đơn vị gốc để trừ kho

    public ChiTietXuatLo() {
        super();
        this.soLuongXuatCoSo = 0;
    }

    public ChiTietXuatLo(String maChiTiet, HoaDon hoaDon, int soLuong, BigDecimal donGiaLucBan,
                         LoHang loHang, QuyDoiDonVi quyDoiDonVi) {
        super(maChiTiet, hoaDon, soLuong, donGiaLucBan);
        this.loHang = loHang;
        this.quyDoiDonVi = quyDoiDonVi;
        if (quyDoiDonVi != null && quyDoiDonVi.getSanPham() != null) {
            this.sanPham = quyDoiDonVi.getSanPham();
        } else if (loHang != null && loHang.getSanPham() != null) {
            this.sanPham = loHang.getSanPham();
        }
        tinhSoLuongXuatCoSo();
    }

    public LoHang getLoHang() {
        return loHang;
    }

    public void setLoHang(LoHang loHang) {
        this.loHang = loHang;
        if (this.sanPham == null && loHang != null && loHang.getSanPham() != null) {
            this.sanPham = loHang.getSanPham();
        }
    }

    public QuyDoiDonVi getQuyDoiDonVi() {
        return quyDoiDonVi;
    }

    public void setQuyDoiDonVi(QuyDoiDonVi quyDoiDonVi) {
        this.quyDoiDonVi = quyDoiDonVi;
        if (quyDoiDonVi != null && quyDoiDonVi.getSanPham() != null) {
            this.sanPham = quyDoiDonVi.getSanPham();
        }
        if (this.donGiaLucBan == null || this.donGiaLucBan.compareTo(BigDecimal.ZERO) <= 0) {
            apDungGiaNiemYet();
        }
        tinhSoLuongXuatCoSo();
    }

    @Override
    public void setSoLuong(int soLuong) {
        super.setSoLuong(soLuong);
        tinhSoLuongXuatCoSo();
    }

    @Override
    public void apDungGiaNiemYet() {
        if (quyDoiDonVi != null && quyDoiDonVi.getGiaBan() != null && quyDoiDonVi.getGiaBan().compareTo(BigDecimal.ZERO) > 0) {
            this.donGiaLucBan = quyDoiDonVi.getGiaBan();
            this.thanhTien = tinhThanhTien();
        } else {
            super.apDungGiaNiemYet();
        }
    }

    public int getSoLuongXuatCoSo() {
        return soLuongXuatCoSo;
    }

    public void setSoLuongXuatCoSo(int soLuongXuatCoSo) {
        this.soLuongXuatCoSo = soLuongXuatCoSo;
    }

    /**
     * Tự động tính số lượng đơn vị cơ sở cần trừ trong kho lô hàng
     */
    public void tinhSoLuongXuatCoSo() {
        int tyLe = (quyDoiDonVi != null) ? quyDoiDonVi.getTyLeQuyDoi() : 1;
        this.soLuongXuatCoSo = this.soLuong * tyLe;
    }

    @Override
    public BigDecimal tinhThanhTien() {
        if (this.donGiaLucBan == null) {
            return BigDecimal.ZERO;
        }
        return this.donGiaLucBan.multiply(BigDecimal.valueOf(this.soLuong));
    }

    @Override
    public String getTenKhoanMuc() {
        if (quyDoiDonVi != null && quyDoiDonVi.getSanPham() != null) {
            String donVi = quyDoiDonVi.getDonViTinh() != null ? quyDoiDonVi.getDonViTinh().getTenDonVi() : "";
            return quyDoiDonVi.getSanPham().getTenSanPham() + " [" + donVi + " - Lô: " + (loHang != null ? loHang.getSoLo() : "") + "]";
        }
        if (sanPham != null) {
            return sanPham.getTenSanPham();
        }
        return "Sản phẩm xuất kho";
    }

    @Override
    public String getLoaiChiTiet() {
        return "SAN_PHAM";
    }

    @Override
    public String toString() {
        return "ChiTietXuatLo{" +
                "maChiTiet='" + maChiTiet + '\'' +
                ", tenKhoanMuc='" + getTenKhoanMuc() + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGiaLucBan +
                ", thanhTien=" + getThanhTien() +
                '}';
    }
}
