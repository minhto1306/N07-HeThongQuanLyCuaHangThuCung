package models.entity;

/**
 * Thực thể Chi tiết lịch hẹn
 */
public class ChiTietLichHen {
    private String maChiTietLichHen;
    private LichHen lichHen;
    private ThuCung thuCung;
    private DichVu dichVu;
    private NhanVien ktvChiDinh;

    public ChiTietLichHen() {}

    public ChiTietLichHen(String maChiTietLichHen, LichHen lichHen, ThuCung thuCung, DichVu dichVu, NhanVien ktvChiDinh) {
        this.maChiTietLichHen = maChiTietLichHen;
        this.lichHen = lichHen;
        this.thuCung = thuCung;
        this.dichVu = dichVu;
        this.ktvChiDinh = ktvChiDinh;
    }

    public String getMaChiTietLichHen() {
        return maChiTietLichHen;
    }

    public void setMaChiTietLichHen(String maChiTietLichHen) {
        this.maChiTietLichHen = maChiTietLichHen;
    }

    public LichHen getLichHen() {
        return lichHen;
    }

    public void setLichHen(LichHen lichHen) {
        this.lichHen = lichHen;
    }

    public ThuCung getThuCung() {
        return thuCung;
    }

    public void setThuCung(ThuCung thuCung) {
        this.thuCung = thuCung;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public NhanVien getKtvChiDinh() {
        return ktvChiDinh;
    }

    public void setKtvChiDinh(NhanVien ktvChiDinh) {
        this.ktvChiDinh = ktvChiDinh;
    }

    @Override
    public String toString() {
        return "ChiTietLichHen{" +
                "maChiTietLichHen='" + maChiTietLichHen + '\'' +
                ", thuCung=" + (thuCung != null ? thuCung.getTenThuCung() : "null") +
                ", dichVu=" + (dichVu != null ? dichVu.getTenDichVu() : "null") +
                ", ktvChiDinh=" + (ktvChiDinh != null ? ktvChiDinh.getHoTen() : "null") +
                '}';
    }
}
