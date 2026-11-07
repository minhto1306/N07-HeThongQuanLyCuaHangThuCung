package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thực thể Bảng giao ca & đối soát tiền mặt cuối ca
 */
public class BangGiaoCa {
    private String maGiaoCa;
    private NhanVien nhanVien;
    private CaLamViec caLamViec;
    private BigDecimal tienDauCa;
    private BigDecimal tienCuoiCaHeThong;
    private BigDecimal tienCuoiCaThucTe;
    private BigDecimal tienChenhLech;
    private BigDecimal tienThuTienMat;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienHoanTra;
    private String lyDoChenhLech;
    private LocalDateTime gioKetThucThucTe;
    private String trangThai;
    private NhanVien quanLyDuyet;
    private LocalDateTime thoiGianDuyet;

    public BangGiaoCa() {
        this.tienDauCa = BigDecimal.ZERO;
        this.tienCuoiCaHeThong = BigDecimal.ZERO;
        this.tienCuoiCaThucTe = BigDecimal.ZERO;
        this.tienChenhLech = BigDecimal.ZERO;
        this.tienThuTienMat = BigDecimal.ZERO;
        this.tienChuyenKhoan = BigDecimal.ZERO;
        this.tienHoanTra = BigDecimal.ZERO;
        this.trangThai = "DANG_MO";
    }

    public BangGiaoCa(String maGiaoCa, NhanVien nhanVien, CaLamViec caLamViec, BigDecimal tienDauCa,
                      BigDecimal tienCuoiCaHeThong, BigDecimal tienCuoiCaThucTe, BigDecimal tienChenhLech,
                      String lyDoChenhLech, LocalDateTime gioKetThucThucTe) {
        this.maGiaoCa = maGiaoCa;
        this.nhanVien = nhanVien;
        this.caLamViec = caLamViec;
        this.tienDauCa = tienDauCa != null ? tienDauCa : BigDecimal.ZERO;
        this.tienCuoiCaHeThong = tienCuoiCaHeThong != null ? tienCuoiCaHeThong : BigDecimal.ZERO;
        this.tienCuoiCaThucTe = tienCuoiCaThucTe != null ? tienCuoiCaThucTe : BigDecimal.ZERO;
        this.tienChenhLech = tienChenhLech != null ? tienChenhLech : BigDecimal.ZERO;
        this.lyDoChenhLech = lyDoChenhLech;
        this.gioKetThucThucTe = gioKetThucThucTe;
        this.tienThuTienMat = BigDecimal.ZERO;
        this.tienChuyenKhoan = BigDecimal.ZERO;
        this.tienHoanTra = BigDecimal.ZERO;
        this.trangThai = gioKetThucThucTe == null ? "DANG_MO" : "CHO_DUYET";
    }

    public BangGiaoCa(String maGiaoCa, NhanVien nhanVien, CaLamViec caLamViec, BigDecimal tienDauCa,
                      BigDecimal tienCuoiCaHeThong, BigDecimal tienCuoiCaThucTe, BigDecimal tienChenhLech,
                      BigDecimal tienThuTienMat, BigDecimal tienChuyenKhoan, BigDecimal tienHoanTra,
                      String lyDoChenhLech, LocalDateTime gioKetThucThucTe, String trangThai,
                      NhanVien quanLyDuyet, LocalDateTime thoiGianDuyet) {
        this(maGiaoCa, nhanVien, caLamViec, tienDauCa, tienCuoiCaHeThong, tienCuoiCaThucTe,
                tienChenhLech, lyDoChenhLech, gioKetThucThucTe);
        this.tienThuTienMat = tienThuTienMat != null ? tienThuTienMat : BigDecimal.ZERO;
        this.tienChuyenKhoan = tienChuyenKhoan != null ? tienChuyenKhoan : BigDecimal.ZERO;
        this.tienHoanTra = tienHoanTra != null ? tienHoanTra : BigDecimal.ZERO;
        this.trangThai = trangThai != null ? trangThai : this.trangThai;
        this.quanLyDuyet = quanLyDuyet;
        this.thoiGianDuyet = thoiGianDuyet;
    }

    public String getMaGiaoCa() {
        return maGiaoCa;
    }

    public void setMaGiaoCa(String maGiaoCa) {
        this.maGiaoCa = maGiaoCa;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public CaLamViec getCaLamViec() {
        return caLamViec;
    }

    public void setCaLamViec(CaLamViec caLamViec) {
        this.caLamViec = caLamViec;
    }

    public BigDecimal getTienDauCa() {
        return tienDauCa;
    }

    public void setTienDauCa(BigDecimal tienDauCa) {
        this.tienDauCa = tienDauCa;
    }

    public BigDecimal getTienCuoiCaHeThong() {
        return tienCuoiCaHeThong;
    }

    public void setTienCuoiCaHeThong(BigDecimal tienCuoiCaHeThong) {
        this.tienCuoiCaHeThong = tienCuoiCaHeThong;
    }

    public BigDecimal getTienCuoiCaThucTe() {
        return tienCuoiCaThucTe;
    }

    public void setTienCuoiCaThucTe(BigDecimal tienCuoiCaThucTe) {
        this.tienCuoiCaThucTe = tienCuoiCaThucTe;
    }

    public BigDecimal getTienChenhLech() {
        return tienChenhLech;
    }

    public void setTienChenhLech(BigDecimal tienChenhLech) {
        this.tienChenhLech = tienChenhLech;
    }

    public BigDecimal getTienThuTienMat() { return tienThuTienMat; }
    public void setTienThuTienMat(BigDecimal tienThuTienMat) { this.tienThuTienMat = tienThuTienMat; }
    public BigDecimal getTienChuyenKhoan() { return tienChuyenKhoan; }
    public void setTienChuyenKhoan(BigDecimal tienChuyenKhoan) { this.tienChuyenKhoan = tienChuyenKhoan; }
    public BigDecimal getTienHoanTra() { return tienHoanTra; }
    public void setTienHoanTra(BigDecimal tienHoanTra) { this.tienHoanTra = tienHoanTra; }

    public String getLyDoChenhLech() {
        return lyDoChenhLech;
    }

    public void setLyDoChenhLech(String lyDoChenhLech) {
        this.lyDoChenhLech = lyDoChenhLech;
    }

    public LocalDateTime getGioKetThucThucTe() {
        return gioKetThucThucTe;
    }

    public void setGioKetThucThucTe(LocalDateTime gioKetThucThucTe) {
        this.gioKetThucThucTe = gioKetThucThucTe;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public NhanVien getQuanLyDuyet() { return quanLyDuyet; }
    public void setQuanLyDuyet(NhanVien quanLyDuyet) { this.quanLyDuyet = quanLyDuyet; }
    public LocalDateTime getThoiGianDuyet() { return thoiGianDuyet; }
    public void setThoiGianDuyet(LocalDateTime thoiGianDuyet) { this.thoiGianDuyet = thoiGianDuyet; }

    /**
     * Tự động tính chênh lệch giữa tiền thực tế và tiền hệ thống
     */
    public void capNhatTienChenhLech() {
        if (this.tienCuoiCaThucTe != null && this.tienCuoiCaHeThong != null) {
            this.tienChenhLech = this.tienCuoiCaThucTe.subtract(this.tienCuoiCaHeThong);
        }
    }

    @Override
    public String toString() {
        return "BangGiaoCa{" +
                "maGiaoCa='" + maGiaoCa + '\'' +
                ", nhanVien=" + (nhanVien != null ? nhanVien.getHoTen() : "null") +
                ", caLamViec=" + (caLamViec != null ? caLamViec.getTenCa() : "null") +
                ", tienDauCa=" + tienDauCa +
                ", tienCuoiCaThucTe=" + tienCuoiCaThucTe +
                ", tienChenhLech=" + tienChenhLech +
                '}';
    }
}
