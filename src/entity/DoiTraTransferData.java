package entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) cho luồng đổi trả hàng chuyển tiếp sang POS thanh toán.
 * Định nghĩa hợp đồng dữ liệu giữa DoiTraHangPanel, MainDashboard và BanHangPOSPanel.
 */
public class DoiTraTransferData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Thông tin chi tiết một mặt hàng khách trả lại cửa hàng
     */
    public static class MonTraLai implements Serializable {
        private static final long serialVersionUID = 1L;

        private String maSP;
        private String tenSP;
        private String dvt;
        private int soLuong;
        private long donGia;
        private long thanhTien;
        private String lyDo;
        private boolean nguyenTem;
        private String soLo;
        private int soLuongCoSoHoan;

        public MonTraLai() {}

        public MonTraLai(String maSP, String tenSP, String dvt, int soLuong,
                         long donGia, long thanhTien, String lyDo, boolean nguyenTem) {
            this(maSP, tenSP, dvt, soLuong, donGia, thanhTien, lyDo, nguyenTem, null, 0);
        }

        public MonTraLai(String maSP, String tenSP, String dvt, int soLuong,
                         long donGia, long thanhTien, String lyDo, boolean nguyenTem,
                         String soLo, int soLuongCoSoHoan) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.dvt = dvt;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
            this.lyDo = lyDo;
            this.nguyenTem = nguyenTem;
            this.soLo = soLo;
            this.soLuongCoSoHoan = soLuongCoSoHoan;
        }

        public String getMaSP() { return maSP; }
        public void setMaSP(String maSP) { this.maSP = maSP; }

        public String getTenSP() { return tenSP; }
        public void setTenSP(String tenSP) { this.tenSP = tenSP; }

        public String getDvt() { return dvt; }
        public void setDvt(String dvt) { this.dvt = dvt; }

        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

        public long getDonGia() { return donGia; }
        public void setDonGia(long donGia) { this.donGia = donGia; }

        public long getThanhTien() { return thanhTien; }
        public void setThanhTien(long thanhTien) { this.thanhTien = thanhTien; }

        public String getLyDo() { return lyDo; }
        public void setLyDo(String lyDo) { this.lyDo = lyDo; }

        public boolean isNguyenTem() { return nguyenTem; }
        public void setNguyenTem(boolean nguyenTem) { this.nguyenTem = nguyenTem; }

        public String getSoLo() { return soLo; }
        public void setSoLo(String soLo) { this.soLo = soLo; }

        public int getSoLuongCoSoHoan() { return soLuongCoSoHoan; }
        public void setSoLuongCoSoHoan(int soLuongCoSoHoan) { this.soLuongCoSoHoan = soLuongCoSoHoan; }
    }

    /**
     * Thông tin chi tiết một mặt hàng mới đổi sang được chọn từ DoiTraHangPanel
     */
    public static class MonDoiMoi implements Serializable {
        private static final long serialVersionUID = 1L;

        private String maSP;
        private String tenSP;
        private String dvt;
        private int soLuong;
        private long donGia;

        public MonDoiMoi() {}

        public MonDoiMoi(String maSP, String tenSP, String dvt, int soLuong, long donGia) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.dvt = dvt;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        public String getMaSP() { return maSP; }
        public void setMaSP(String maSP) { this.maSP = maSP; }

        public String getTenSP() { return tenSP; }
        public void setTenSP(String tenSP) { this.tenSP = tenSP; }

        public String getDvt() { return dvt; }
        public void setDvt(String dvt) { this.dvt = dvt; }

        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

        public long getDonGia() { return donGia; }
        public void setDonGia(long donGia) { this.donGia = donGia; }
    }

    private String maHoaDonGoc;
    private String thoiGianGoc;
    private String tenKhach;
    private String sdtKhach;
    private long tongTienTra;
    private int diemKhauTru;
    private long giaTriHangTra;
    private long giaTriHangDoi;
    private long tienHoanThucTe;
    private long tienKhachTraThem;
    private String phuongThucHoanTien= "CHUA_XAC_DINH";
    private List<MonTraLai> dsMonTra= new ArrayList<>();
    private List<MonDoiMoi> dsMonMoi= new ArrayList<>();

    public DoiTraTransferData() {}

    public DoiTraTransferData(String maHoaDonGoc, String tenKhach, String sdtKhach, long tongTienTra) {
        this.maHoaDonGoc = maHoaDonGoc;
        this.tenKhach = tenKhach;
        this.sdtKhach = sdtKhach;
        this.tongTienTra = tongTienTra;
    }

    public DoiTraTransferData(String maHoaDonGoc, String thoiGianGoc, String tenKhach, String sdtKhach, long tongTienTra, int diemKhauTru) {
        this.maHoaDonGoc = maHoaDonGoc;
        this.thoiGianGoc = thoiGianGoc;
        this.tenKhach = tenKhach;
        this.sdtKhach = sdtKhach;
        this.tongTienTra = tongTienTra;
        this.diemKhauTru = diemKhauTru;
    }

    public String getMaHoaDonGoc() { return maHoaDonGoc; }
    public void setMaHoaDonGoc(String maHoaDonGoc) { this.maHoaDonGoc = maHoaDonGoc; }

    public String getThoiGianGoc() { return thoiGianGoc; }
    public void setThoiGianGoc(String thoiGianGoc) { this.thoiGianGoc = thoiGianGoc; }

    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }

    public String getSdtKhach() { return sdtKhach; }
    public void setSdtKhach(String sdtKhach) { this.sdtKhach = sdtKhach; }

    public long getTongTienTra() { return tongTienTra; }
    public void setTongTienTra(long tongTienTra) { this.tongTienTra = tongTienTra; }

    public int getDiemKhauTru() { return diemKhauTru; }
    public void setDiemKhauTru(int diemKhauTru) { this.diemKhauTru = diemKhauTru; }

    public long getGiaTriHangTra() { return giaTriHangTra; }
    public void setGiaTriHangTra(long giaTriHangTra) { this.giaTriHangTra = giaTriHangTra; }

    public long getGiaTriHangDoi() { return giaTriHangDoi; }
    public void setGiaTriHangDoi(long giaTriHangDoi) { this.giaTriHangDoi = giaTriHangDoi; }

    public long getTienHoanThucTe() { return tienHoanThucTe; }
    public void setTienHoanThucTe(long tienHoanThucTe) { this.tienHoanThucTe = tienHoanThucTe; }

    public long getTienKhachTraThem() { return tienKhachTraThem; }
    public void setTienKhachTraThem(long tienKhachTraThem) { this.tienKhachTraThem = tienKhachTraThem; }

    public String getPhuongThucHoanTien() { return phuongThucHoanTien; }
    public void setPhuongThucHoanTien(String phuongThucHoanTien) { this.phuongThucHoanTien = phuongThucHoanTien; }

    public List<MonTraLai> getDsMonTra() { return dsMonTra; }
    public void setDsMonTra(List<MonTraLai> dsMonTra) { this.dsMonTra = dsMonTra; }

    public List<MonDoiMoi> getDsMonMoi() { return dsMonMoi; }
    public void setDsMonMoi(List<MonDoiMoi> dsMonMoi) { this.dsMonMoi = dsMonMoi; }

    public void addMonTra(MonTraLai mon) {
        if (this.dsMonTra == null) this.dsMonTra = new ArrayList<>();
        this.dsMonTra.add(mon);
    }

    public void addMonMoi(MonDoiMoi mon) {
        if (this.dsMonMoi == null) this.dsMonMoi = new ArrayList<>();
        this.dsMonMoi.add(mon);
    }
}
