package models.entity;

import java.math.BigDecimal;

/**
 * Lớp con đại diện cho Dòng dịch vụ chăm sóc thú cưng trên Hóa đơn.
 * Kế thừa từ ChiTietHoaDon. Ghi nhận kỹ thuật viên thực hiện & tính hoa hồng.
 */
public class ChiTietDichVu extends ChiTietHoaDon {
    private DichVu dichVu;
    private ThuCung thuCung;
    private NhanVien ktvThucHien;
    private BigDecimal tienHoaHong;

    public ChiTietDichVu() {
        super();
        this.tienHoaHong = BigDecimal.ZERO;
    }

    public ChiTietDichVu(String maChiTiet, HoaDon hoaDon, int soLuong, BigDecimal donGiaLucBan,
                         DichVu dichVu, ThuCung thuCung, NhanVien ktvThucHien, BigDecimal tienHoaHong) {
        super(maChiTiet, hoaDon, soLuong, donGiaLucBan);
        this.dichVu = dichVu;
        this.thuCung = thuCung;
        this.ktvThucHien = ktvThucHien;
        this.tienHoaHong = tienHoaHong != null ? tienHoaHong : BigDecimal.ZERO;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public NhanVien getKtvThucHien() {
        return ktvThucHien;
    }

    public void setKtvThucHien(NhanVien ktvThucHien) {
        this.ktvThucHien = ktvThucHien;
    }

    public BigDecimal getTienHoaHong() {
        return tienHoaHong;
    }

    public void setTienHoaHong(BigDecimal tienHoaHong) {
        this.tienHoaHong = tienHoaHong;
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
        String tenDv = dichVu != null ? dichVu.getTenDichVu() : "Dịch vụ";
        String tenTc = thuCung != null ? thuCung.getTenThuCung() : "Chưa rõ thú cưng";
        String tenKtv = ktvThucHien != null ? " - KTV: " + ktvThucHien.getHoTen() : "";
        return "[Dịch vụ] " + tenDv + " (" + tenTc + ")" + tenKtv;
    }

    @Override
    public String getLoaiChiTiet() {
        return "DICH_VU";
    }

    @Override
    public String toString() {
        return "ChiTietDichVu{" +
                "maChiTiet='" + maChiTiet + '\'' +
                ", tenKhoanMuc='" + getTenKhoanMuc() + '\'' +
                ", soLuong=" + soLuong +
                ", donGia=" + donGiaLucBan +
                ", thanhTien=" + getThanhTien() +
                ", hoaHong=" + tienHoaHong +
                '}';
    }
}
