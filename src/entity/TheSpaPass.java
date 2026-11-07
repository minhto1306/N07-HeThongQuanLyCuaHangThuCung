package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Thực thể Thẻ Gói Dịch Vụ Định Kỳ (Spa Pass / Punch Card).
 * Cho phép khách hàng mua gói 5 hoặc 10 buổi với giá ưu đãi,
 * trừ dần số buổi khi đưa thú cưng đến làm dịch vụ tại quầy POS.
 * Hỗ trợ dùng chung cho các bé trong cùng một gia đình (Family Sharing qua SĐT).
 */
public class TheSpaPass {

    private String maThe;
    private String sdtKhachHang;
    private String tenKhachHang;
    private String tenGoi;
    private int tongSoBuoi;
    private int soBuoiDaDung;
    private int soBuoiConLai;
    private long donGiaGoi;
    private LocalDate ngayKichHoat;
    private LocalDate ngayHetHan;
    private String trangThai; // "ĐANG HOẠT ĐỘNG", "ĐÃ HẾT BUỔI", "HẾT HẠN SỬ DỤNG"

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public TheSpaPass() {
        this.ngayKichHoat = LocalDate.now();
        this.ngayHetHan = LocalDate.now().plusDays(90);
        this.trangThai = "ĐANG HOẠT ĐỘNG";
    }

    public TheSpaPass(String maThe, String sdtKhachHang, String tenKhachHang, String tenGoi,
                      int tongSoBuoi, int soBuoiDaDung, long donGiaGoi, LocalDate ngayKichHoat, LocalDate ngayHetHan) {
        this.maThe = maThe;
        this.sdtKhachHang = sdtKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.tenGoi = tenGoi;
        this.tongSoBuoi = tongSoBuoi;
        this.soBuoiDaDung = soBuoiDaDung;
        this.soBuoiConLai = Math.max(0, tongSoBuoi - soBuoiDaDung);
        this.donGiaGoi = donGiaGoi;
        this.ngayKichHoat = ngayKichHoat != null ? ngayKichHoat : LocalDate.now();
        this.ngayHetHan = ngayHetHan != null ? ngayHetHan : LocalDate.now().plusDays(90);
        capNhatTrangThai();
    }

    public void capNhatTrangThai() {
        if (soBuoiConLai <= 0) {
            this.trangThai = "ĐÃ HẾT BUỔI";
        } else if (LocalDate.now().isAfter(ngayHetHan)) {
            this.trangThai = "HẾT HẠN SỬ DỤNG";
        } else {
            this.trangThai = "ĐANG HOẠT ĐỘNG";
        }
    }

    public boolean coTheSuDung() {
        capNhatTrangThai();
        return "ĐANG HOẠT ĐỘNG".equals(this.trangThai) && soBuoiConLai > 0;
    }

    public boolean dungMotBuoi() {
        if (!coTheSuDung()) return false;
        this.soBuoiDaDung++;
        this.soBuoiConLai = Math.max(0, tongSoBuoi - soBuoiDaDung);
        capNhatTrangThai();
        return true;
    }

    public void giaHanThem(int soNgay) {
        if (this.ngayHetHan != null) {
            this.ngayHetHan = this.ngayHetHan.plusDays(soNgay);
            capNhatTrangThai();
        }
    }

    public void napThemBuoi(int soBuoiNapThem, int soNgayGiaHan) {
        this.tongSoBuoi += Math.max(0, soBuoiNapThem);
        this.soBuoiConLai += Math.max(0, soBuoiNapThem);
        if (soNgayGiaHan > 0 && this.ngayHetHan != null) {
            this.ngayHetHan = this.ngayHetHan.plusDays(soNgayGiaHan);
        }
        capNhatTrangThai();
    }

    public String getMaThe() { return maThe; }
    public void setMaThe(String maThe) { this.maThe = maThe; }

    public String getSdtKhachHang() { return sdtKhachHang; }
    public void setSdtKhachHang(String sdtKhachHang) { this.sdtKhachHang = sdtKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getTenGoi() { return tenGoi; }
    public void setTenGoi(String tenGoi) { this.tenGoi = tenGoi; }

    public int getTongSoBuoi() { return tongSoBuoi; }
    public void setTongSoBuoi(int tongSoBuoi) { this.tongSoBuoi = tongSoBuoi; }

    public int getSoBuoiDaDung() { return soBuoiDaDung; }
    public void setSoBuoiDaDung(int soBuoiDaDung) { this.soBuoiDaDung = soBuoiDaDung; }

    public int getSoBuoiConLai() { return soBuoiConLai; }
    public void setSoBuoiConLai(int soBuoiConLai) { this.soBuoiConLai = soBuoiConLai; }

    public long getDonGiaGoi() { return donGiaGoi; }
    public void setDonGiaGoi(long donGiaGoi) { this.donGiaGoi = donGiaGoi; }

    public LocalDate getNgayKichHoat() { return ngayKichHoat; }
    public void setNgayKichHoat(LocalDate ngayKichHoat) { this.ngayKichHoat = ngayKichHoat; }

    public LocalDate getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDate ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public boolean isKhaDung() { return coTheSuDung(); }
    public String getTenThe() { return tenGoi != null ? tenGoi : "Thẻ Spa Pass"; }

    public String getNgayHetHanStr() {
        return ngayHetHan != null ? ngayHetHan.format(DF) : "—";
    }

    @Override
    public String toString() {
        return "TheSpaPass{" +
                "maThe='" + maThe + '\'' +
                ", sdtKhachHang='" + sdtKhachHang + '\'' +
                ", tenGoi='" + tenGoi + '\'' +
                ", soBuoiConLai=" + soBuoiConLai + "/" + tongSoBuoi +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
