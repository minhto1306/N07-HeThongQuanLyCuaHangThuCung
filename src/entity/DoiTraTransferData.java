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

        public String maSP;
        public String tenSP;
        public String dvt;
        public int soLuong;
        public long donGia;
        public long thanhTien;
        public String lyDo;
        public boolean nguyenTem;

        public MonTraLai() {}

        public MonTraLai(String maSP, String tenSP, String dvt, int soLuong,
                         long donGia, long thanhTien, String lyDo, boolean nguyenTem) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.dvt = dvt;
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
            this.lyDo = lyDo;
            this.nguyenTem = nguyenTem;
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
    }

    /**
     * Thông tin chi tiết một mặt hàng mới đổi sang được chọn từ DoiTraHangPanel
     */
    public static class MonDoiMoi implements Serializable {
        private static final long serialVersionUID = 1L;

        public String maSP;
        public String tenSP;
        public String dvt;
        public int soLuong;
        public long donGia;

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

    public String maHoaDonGoc;
    public String thoiGianGoc;
    public String tenKhach;
    public String sdtKhach;
    public long tongTienTra;
    public int diemKhauTru;
    public List<MonTraLai> dsMonTra = new ArrayList<>();
    public List<MonDoiMoi> dsMonMoi = new ArrayList<>();

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
