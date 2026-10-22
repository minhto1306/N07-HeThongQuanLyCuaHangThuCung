package models.entity;

import java.time.LocalDate;

/**
 * Thực thể Bảng phân công ca làm việc
 */
public class BangPhanCong {
    private String maPhanCong;
    private NhanVien nhanVien;
    private CaLamViec caLamViec;
    private LocalDate ngayLamViec;
    private String trangThaiCa; // Chưa bắt đầu, Đang làm việc, Đã hoàn thành, Vắng mặt
    private String ghiChu;      // Ghi chú ca trực: "Spa bồn sục", "Quầy thu ngân 1", v.v.

    public BangPhanCong() {}

    public BangPhanCong(String maPhanCong, NhanVien nhanVien, CaLamViec caLamViec, LocalDate ngayLamViec, String trangThaiCa) {
        this(maPhanCong, nhanVien, caLamViec, ngayLamViec, trangThaiCa, "");
    }

    public BangPhanCong(String maPhanCong, NhanVien nhanVien, CaLamViec caLamViec, LocalDate ngayLamViec, String trangThaiCa, String ghiChu) {
        this.maPhanCong = maPhanCong;
        this.nhanVien = nhanVien;
        this.caLamViec = caLamViec;
        this.ngayLamViec = ngayLamViec;
        this.trangThaiCa = trangThaiCa;
        this.ghiChu = ghiChu;
    }

    public String getMaPhanCong() {
        return maPhanCong;
    }

    public void setMaPhanCong(String maPhanCong) {
        this.maPhanCong = maPhanCong;
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

    public LocalDate getNgayLamViec() {
        return ngayLamViec;
    }

    public void setNgayLamViec(LocalDate ngayLamViec) {
        this.ngayLamViec = ngayLamViec;
    }

    public String getTrangThaiCa() {
        return trangThaiCa;
    }

    public void setTrangThaiCa(String trangThaiCa) {
        this.trangThaiCa = trangThaiCa;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "BangPhanCong{" +
                "maPhanCong='" + maPhanCong + '\'' +
                ", nhanVien=" + (nhanVien != null ? nhanVien.getHoTen() : "null") +
                ", caLamViec=" + (caLamViec != null ? caLamViec.getTenCa() : "null") +
                ", ngayLamViec=" + ngayLamViec +
                ", trangThaiCa='" + trangThaiCa + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
