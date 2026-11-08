package entity;

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
    private BigDecimal giaTriHangTra;
    private BigDecimal giaTriHangDoi;
    private BigDecimal tienHoanThucTe;
    private BigDecimal tienKhachTraThem;
    private String phuongThucHoanTien;
    private List<ChiTietDoiTra> dsChiTietDoiTra;

    public PhieuDoiTra() {
        this.ngayDoiTra = LocalDateTime.now();
        this.tongTienHoan = BigDecimal.ZERO;
        this.giaTriHangTra = BigDecimal.ZERO;
        this.giaTriHangDoi = BigDecimal.ZERO;
        this.tienHoanThucTe = BigDecimal.ZERO;
        this.tienKhachTraThem = BigDecimal.ZERO;
        this.phuongThucHoanTien = "CHUA_XAC_DINH";
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

    public BigDecimal getGiaTriHangTra() {
        return giaTriHangTra;
    }

    public void setGiaTriHangTra(BigDecimal giaTriHangTra) {
        this.giaTriHangTra = giaTriHangTra != null ? giaTriHangTra : BigDecimal.ZERO;
    }

    public BigDecimal getGiaTriHangDoi() {
        return giaTriHangDoi;
    }

    public void setGiaTriHangDoi(BigDecimal giaTriHangDoi) {
        this.giaTriHangDoi = giaTriHangDoi != null ? giaTriHangDoi : BigDecimal.ZERO;
    }

    public BigDecimal getTienHoanThucTe() {
        return tienHoanThucTe;
    }

    public void setTienHoanThucTe(BigDecimal tienHoanThucTe) {
        this.tienHoanThucTe = tienHoanThucTe != null ? tienHoanThucTe : BigDecimal.ZERO;
    }

    public BigDecimal getTienKhachTraThem() {
        return tienKhachTraThem;
    }

    public void setTienKhachTraThem(BigDecimal tienKhachTraThem) {
        this.tienKhachTraThem = tienKhachTraThem != null ? tienKhachTraThem : BigDecimal.ZERO;
    }

    public String getPhuongThucHoanTien() {
        return phuongThucHoanTien;
    }

    public void setPhuongThucHoanTien(String phuongThucHoanTien) {
        this.phuongThucHoanTien = phuongThucHoanTien != null ? phuongThucHoanTien : "CHUA_XAC_DINH";
    }

    public List<ChiTietDoiTra> getDsChiTietDoiTra() {
        return dsChiTietDoiTra;
    }

    public void setDsChiTietDoiTra(List<ChiTietDoiTra> dsChiTietDoiTra) {
        this.dsChiTietDoiTra = dsChiTietDoiTra;
    }

    public void addChiTietDoiTra(ChiTietDoiTra chiTiet) {
        this.dsChiTietDoiTra.add(chiTiet);
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
