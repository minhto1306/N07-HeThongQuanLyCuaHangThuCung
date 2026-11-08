package entity;

import java.time.LocalDateTime;

/**
 * Snapshot thông tin chốt thanh toán Pet Hotel tại thời điểm check-out.
 * Giữ nguyên số ngày và giá trị thanh toán, chống tính lại số ngày (DATEDIFF / GETDATE)
 * khi hóa đơn POS thanh toán qua ngày mới (Lỗi B03).
 */
public class HotelCheckoutSnapshot {
    private String maPhong;
    private String loaiPhong;
    private String tenThuCung;
    private String tenChu;
    private String sdtChu;
    private LocalDateTime thoiGianCheckIn;
    private LocalDateTime thoiGianCheckOut;
    private int soNgayLuuTru;
    private long donGiaNgay;
    private long phiPhatSinh;
    private long tongTien;
    private String ghiChu;

    public HotelCheckoutSnapshot() {
    }

    public HotelCheckoutSnapshot(String maPhong, String loaiPhong, String tenThuCung, String tenChu, String sdtChu,
                                 LocalDateTime thoiGianCheckIn, LocalDateTime thoiGianCheckOut,
                                 int soNgayLuuTru, long donGiaNgay, long phiPhatSinh, long tongTien, String ghiChu) {
        this.maPhong = maPhong;
        this.loaiPhong = loaiPhong;
        this.tenThuCung = tenThuCung;
        this.tenChu = tenChu;
        this.sdtChu = sdtChu;
        this.thoiGianCheckIn = thoiGianCheckIn;
        this.thoiGianCheckOut = thoiGianCheckOut;
        this.soNgayLuuTru = soNgayLuuTru;
        this.donGiaNgay = donGiaNgay;
        this.phiPhatSinh = phiPhatSinh;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getLoaiPhong() {
        return loaiPhong;
    }

    public void setLoaiPhong(String loaiPhong) {
        this.loaiPhong = loaiPhong;
    }

    public String getTenThuCung() {
        return tenThuCung;
    }

    public void setTenThuCung(String tenThuCung) {
        this.tenThuCung = tenThuCung;
    }

    public String getTenChu() {
        return tenChu;
    }

    public void setTenChu(String tenChu) {
        this.tenChu = tenChu;
    }

    public String getSdtChu() {
        return sdtChu;
    }

    public void setSdtChu(String sdtChu) {
        this.sdtChu = sdtChu;
    }

    public LocalDateTime getThoiGianCheckIn() {
        return thoiGianCheckIn;
    }

    public void setThoiGianCheckIn(LocalDateTime thoiGianCheckIn) {
        this.thoiGianCheckIn = thoiGianCheckIn;
    }

    public LocalDateTime getThoiGianCheckOut() {
        return thoiGianCheckOut;
    }

    public void setThoiGianCheckOut(LocalDateTime thoiGianCheckOut) {
        this.thoiGianCheckOut = thoiGianCheckOut;
    }

    public int getSoNgayLuuTru() {
        return soNgayLuuTru;
    }

    public void setSoNgayLuuTru(int soNgayLuuTru) {
        this.soNgayLuuTru = soNgayLuuTru;
    }

    public long getDonGiaNgay() {
        return donGiaNgay;
    }

    public void setDonGiaNgay(long donGiaNgay) {
        this.donGiaNgay = donGiaNgay;
    }

    public long getPhiPhatSinh() {
        return phiPhatSinh;
    }

    public void setPhiPhatSinh(long phiPhatSinh) {
        this.phiPhatSinh = phiPhatSinh;
    }

    public long getTongTien() {
        return tongTien;
    }

    public void setTongTien(long tongTien) {
        this.tongTien = tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
