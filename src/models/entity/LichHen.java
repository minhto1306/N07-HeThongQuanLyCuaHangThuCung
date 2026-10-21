package models.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thực thể Lịch hẹn dịch vụ
 */
public class LichHen {
    private String maLichHen;
    private KhachHang khachHang;
    private NhanVien nhanVienTiepNhan;
    private LocalDateTime ngayHen;
    private String trangThaiLichHen; // Đã đặt, Đang phục vụ, Hoàn thành, Hủy
    private String ghiChu;
    private List<ChiTietLichHen> dsChiTiet;

    public LichHen() {
        this.trangThaiLichHen = "Đã đặt";
        this.dsChiTiet = new ArrayList<>();
    }

    public LichHen(String maLichHen, KhachHang khachHang, NhanVien nhanVienTiepNhan, LocalDateTime ngayHen, String trangThaiLichHen, String ghiChu) {
        this.maLichHen = maLichHen;
        this.khachHang = khachHang;
        this.nhanVienTiepNhan = nhanVienTiepNhan;
        this.ngayHen = ngayHen;
        this.trangThaiLichHen = trangThaiLichHen;
        this.ghiChu = ghiChu;
        this.dsChiTiet = new ArrayList<>();
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        this.maLichHen = maLichHen;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVienTiepNhan() {
        return nhanVienTiepNhan;
    }

    public void setNhanVienTiepNhan(NhanVien nhanVienTiepNhan) {
        this.nhanVienTiepNhan = nhanVienTiepNhan;
    }

    public LocalDateTime getNgayHen() {
        return ngayHen;
    }

    public void setNgayHen(LocalDateTime ngayHen) {
        this.ngayHen = ngayHen;
    }

    public String getTrangThaiLichHen() {
        return trangThaiLichHen;
    }

    public void setTrangThaiLichHen(String trangThaiLichHen) {
        this.trangThaiLichHen = trangThaiLichHen;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public List<ChiTietLichHen> getDsChiTiet() {
        return dsChiTiet;
    }

    public void setDsChiTiet(List<ChiTietLichHen> dsChiTiet) {
        this.dsChiTiet = dsChiTiet;
    }

    public void addChiTiet(ChiTietLichHen chiTiet) {
        if (this.dsChiTiet.size() < 3) { // Tối đa 3 thú cưng/dịch vụ theo nghiệp vụ SRS
            this.dsChiTiet.add(chiTiet);
        } else {
            throw new IllegalStateException("Một lịch hẹn chỉ phục vụ tối đa 3 dịch vụ / thú cưng!");
        }
    }

    @Override
    public String toString() {
        return "LichHen{" +
                "maLichHen='" + maLichHen + '\'' +
                ", khachHang=" + (khachHang != null ? khachHang.getHoTen() : "null") +
                ", ngayHen=" + ngayHen +
                ", trangThaiLichHen='" + trangThaiLichHen + '\'' +
                '}';
    }
}
