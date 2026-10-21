package models.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Phiếu đổi trả hàng hóa
 */
public class PhieuDoiTra {
    private String maPhieuDoiTra;
    private HoaDon hoaDonGoc;
    private NhanVien nhanVien;
    private LocalDateTime ngayDoiTra;
    private String lyDoDoiTra;
    private BigDecimal tongTienHoan;
    private String trangThaiDoiTra;
    private List<ChiTietDoiTra> dsChiTietDoiTra;

    public PhieuDoiTra() {
        this.ngayDoiTra = LocalDateTime.now();
        this.tongTienHoan = BigDecimal.ZERO;
        this.trangThaiDoiTra = "Hoàn thành";
        this.dsChiTietDoiTra = new ArrayList<>();
    }

    public PhieuDoiTra(String maPhieuDoiTra, HoaDon hoaDonGoc, NhanVien nhanVien, LocalDateTime ngayDoiTra,
                       String lyDoDoiTra, BigDecimal tongTienHoan, String trangThaiDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
        this.hoaDonGoc = hoaDonGoc;
        this.nhanVien = nhanVien;
        this.ngayDoiTra = ngayDoiTra != null ? ngayDoiTra : LocalDateTime.now();
        this.lyDoDoiTra = lyDoDoiTra;
        this.tongTienHoan = tongTienHoan != null ? tongTienHoan : BigDecimal.ZERO;
        this.trangThaiDoiTra = trangThaiDoiTra != null ? trangThaiDoiTra : "Hoàn thành";
        this.dsChiTietDoiTra = new ArrayList<>();
    }

    public String getMaPhieuDoiTra() {
        return maPhieuDoiTra;
    }

    public void setMaPhieuDoiTra(String maPhieuDoiTra) {
        this.maPhieuDoiTra = maPhieuDoiTra;
    }

    public HoaDon getHoaDonGoc() {
        return hoaDonGoc;
    }

    public void setHoaDonGoc(HoaDon hoaDonGoc) {
        this.hoaDonGoc = hoaDonGoc;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public LocalDateTime getNgayDoiTra() {
        return ngayDoiTra;
    }

    public void setNgayDoiTra(LocalDateTime ngayDoiTra) {
        this.ngayDoiTra = ngayDoiTra;
    }

    public String getLyDoDoiTra() {
        return lyDoDoiTra;
    }

    public void setLyDoDoiTra(String lyDoDoiTra) {
        this.lyDoDoiTra = lyDoDoiTra;
    }

    public BigDecimal getTongTienHoan() {
        return tongTienHoan;
    }

    public void setTongTienHoan(BigDecimal tongTienHoan) {
        this.tongTienHoan = tongTienHoan;
    }

    public String getTrangThaiDoiTra() {
        return trangThaiDoiTra;
    }

    public void setTrangThaiDoiTra(String trangThaiDoiTra) {
        this.trangThaiDoiTra = trangThaiDoiTra;
    }

    public List<ChiTietDoiTra> getDsChiTietDoiTra() {
        return dsChiTietDoiTra;
    }

    public void setDsChiTietDoiTra(List<ChiTietDoiTra> dsChiTietDoiTra) {
        this.dsChiTietDoiTra = dsChiTietDoiTra;
        capNhatTongTienHoan();
    }

    public void addChiTietDoiTra(ChiTietDoiTra chiTiet) {
        this.dsChiTietDoiTra.add(chiTiet);
        capNhatTongTienHoan();
    }

    public void capNhatTongTienHoan() {
        BigDecimal tong = BigDecimal.ZERO;
        if (this.dsChiTietDoiTra != null) {
            for (ChiTietDoiTra ct : this.dsChiTietDoiTra) {
                if (ct.getTienHoanLai() != null) {
                    tong = tong.add(ct.getTienHoanLai());
                }
            }
        }
        this.tongTienHoan = tong;
    }

    @Override
    public String toString() {
        return "PhieuDoiTra{" +
                "maPhieuDoiTra='" + maPhieuDoiTra + '\'' +
                ", hoaDonGoc=" + (hoaDonGoc != null ? hoaDonGoc.getMaHoaDon() : "null") +
                ", tongTienHoan=" + tongTienHoan +
                ", lyDo='" + lyDoDoiTra + '\'' +
                '}';
    }
}
