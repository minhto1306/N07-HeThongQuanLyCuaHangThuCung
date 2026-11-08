package entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Thực thể Phòng Lưu Trú Khách Sạn Thú Cưng (Pet Hotel).
 * Tuân thủ nghiêm ngặt nguyên lý Đóng gói (Encapsulation) của OOP:
 * Toàn bộ thuộc tính ở mức truy cập private và kiểm soát toàn vẹn dữ liệu qua getter/setter.
 */
public class PhongLuuTru implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maPhong;
    private String loaiPhong;       // Phòng Tiêu chuẩn, VIP Suite, Condo Mèo
    private long donGiaNgay;        // Giá thuê theo ngày (>= 0)
    private String trangThai;       // TRỐNG, ĐANG CÓ KHÁCH, ĐANG DỌN DẸP
    private String tenThuCung;      // Tên thú cưng gửi
    private String giongLoai;
    private String tenChu;
    private String sdtChu;
    private LocalDateTime thoiGianCheckIn;
    private LocalDateTime duKienCheckOut;
    private String ghiChuChamSoc;
    private boolean daChoAnSang;
    private boolean daChoAnToi;
    private boolean daDiDaoVeSinh;
    private long phiDichVuPhatSinh;

    public PhongLuuTru() {
        this.maPhong = "";
        this.loaiPhong = "Phòng Tiêu chuẩn";
        this.donGiaNgay = 0L;
        this.trangThai = "TRỐNG";
        this.tenThuCung = "";
        this.giongLoai = "";
        this.tenChu = "";
        this.sdtChu = "";
        this.ghiChuChamSoc = "";
        this.daChoAnSang = false;
        this.daChoAnToi = false;
        this.daDiDaoVeSinh = false;
        this.phiDichVuPhatSinh = 0L;
    }

    public PhongLuuTru(String maPhong, String loaiPhong, long donGiaNgay, String trangThai) {
        this();
        if (maPhong != null && !maPhong.trim().isEmpty()) {
            this.maPhong = maPhong.trim();
        }
        this.loaiPhong = loaiPhong != null ? loaiPhong : "Phòng Tiêu chuẩn";
        setDonGiaNgay(donGiaNgay);
        setTrangThai(trangThai);
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) {
        if (maPhong != null && !maPhong.trim().isEmpty()) {
            this.maPhong = maPhong.trim();
        }
    }

    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong != null ? loaiPhong : "Phòng Tiêu chuẩn"; }

    public long getDonGiaNgay() { return donGiaNgay; }
    public void setDonGiaNgay(long donGiaNgay) {
        if (donGiaNgay < 0) {
            throw new IllegalArgumentException("Đơn giá ngày lưu trú không được âm: " + donGiaNgay);
        }
        this.donGiaNgay = donGiaNgay;
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) {
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            this.trangThai = trangThai.trim();
        }
    }

    public String getTenThuCung() { return tenThuCung; }
    public void setTenThuCung(String tenThuCung) { this.tenThuCung = tenThuCung != null ? tenThuCung : ""; }

    public String getGiongLoai() { return giongLoai; }
    public void setGiongLoai(String giongLoai) { this.giongLoai = giongLoai != null ? giongLoai : ""; }

    public String getTenChu() { return tenChu; }
    public void setTenChu(String tenChu) { this.tenChu = tenChu != null ? tenChu : ""; }

    public String getSdtChu() { return sdtChu; }
    public void setSdtChu(String sdtChu) { this.sdtChu = sdtChu != null ? sdtChu : ""; }

    public LocalDateTime getThoiGianCheckIn() { return thoiGianCheckIn; }
    public void setThoiGianCheckIn(LocalDateTime thoiGianCheckIn) { this.thoiGianCheckIn = thoiGianCheckIn; }

    public LocalDateTime getDuKienCheckOut() { return duKienCheckOut; }
    public void setDuKienCheckOut(LocalDateTime duKienCheckOut) { this.duKienCheckOut = duKienCheckOut; }

    public String getGhiChuChamSoc() { return ghiChuChamSoc; }
    public void setGhiChuChamSoc(String ghiChuChamSoc) { this.ghiChuChamSoc = ghiChuChamSoc != null ? ghiChuChamSoc : ""; }

    public boolean isDaChoAnSang() { return daChoAnSang; }
    public void setDaChoAnSang(boolean daChoAnSang) { this.daChoAnSang = daChoAnSang; }

    public boolean isDaChoAnToi() { return daChoAnToi; }
    public void setDaChoAnToi(boolean daChoAnToi) { this.daChoAnToi = daChoAnToi; }

    public boolean isDaDiDaoVeSinh() { return daDiDaoVeSinh; }
    public void setDaDiDaoVeSinh(boolean daDiDaoVeSinh) { this.daDiDaoVeSinh = daDiDaoVeSinh; }

    public long getPhiDichVuPhatSinh() { return phiDichVuPhatSinh; }
    public void setPhiDichVuPhatSinh(long phiDichVuPhatSinh) {
        if (phiDichVuPhatSinh < 0) {
            throw new IllegalArgumentException("Phí dịch vụ phát sinh không được âm: " + phiDichVuPhatSinh);
        }
        this.phiDichVuPhatSinh = phiDichVuPhatSinh;
    }

    @Override
    public String toString() {
        return "PhongLuuTru{" +
                "maPhong='" + maPhong + '\'' +
                ", loaiPhong='" + loaiPhong + '\'' +
                ", donGiaNgay=" + donGiaNgay +
                ", trangThai='" + trangThai + '\'' +
                ", tenThuCung='" + tenThuCung + '\'' +
                '}';
    }
}
