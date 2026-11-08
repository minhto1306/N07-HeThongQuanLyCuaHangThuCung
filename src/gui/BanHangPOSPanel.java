package gui;

import bus.BanHangBus;
import bus.SpaPassBus;
import entity.DoiTraTransferData;
import entity.KhachHang;
import entity.TheSpaPass;
import utils.IconHelper;
import utils.ThemeColors;
import utils.UserSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Panel Bán hàng POS (Point of Sale) tại quầy chuẩn phong cách Cửa hàng Thú cưng hiện đại.
 * Tích hợp DUAL-VIEW CHUYÊN NGHIỆP:
 * 1. Tab 1: Màn hình Bán hàng POS (Catalog Grid Cards + Giỏ hàng 1 chạm).
 * 2. Tab 2: Hóa đơn chờ / Đang thao tác trong ngày (Lưới thẻ Grid Cards 1 chạm khôi phục).
 * 3. Tab 3: Lịch sử Hóa đơn đã thanh toán (Bảng JTable đối soát, bộ lọc ngày, In lại HĐ, Xuất Excel).
 */
public class BanHangPOSPanel extends JPanel {

    private final DecimalFormat df = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final BanHangBus controller = new BanHangBus();

    public static final String[] CAC_BUOC_TIEN_DO = {
            "1. Tiếp nhận bé",
            "2. Đang thực hiện",
            "3. Đã xong - Chờ đón",
            "4. Hoàn tất & TT"
    };

    private CardLayout posCardLayout;
    private JPanel pnlCardContainer;
    private JButton btnQuickDonCho;

    public static final String VIEW_POS = "VIEW_POS";
    public static final String VIEW_DON_CHO = "VIEW_DON_CHO";
    public static final String VIEW_LICH_SU = "VIEW_LICH_SU";

    // ==========================================
    // DATA MODELS CHO ĐƠN CHỜ & LỊCH SỬ HÓA ĐƠN
    // ==========================================
    public static class DonHangTam implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public String maHoaDon;
        public String tenKhach;
        public String sdt;
        public String diemTichLuy;
        public String thoiGianTao;
        public List<Object[]> dsMon = new ArrayList<>();
        public long tongTien;
        public long giamGia;
        public long tienDoiDiem;
        public long tienBuTruDoiTra;
        public long khachCanTra;
        public String phuongThuc;
        public List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel = new ArrayList<>();
        public entity.DoiTraTransferData thongTinDoiTra;
        public String maTheSpaPass;
    }

    public static class HoaDonDaThanhToan {
        public String maHoaDon;
        public String thoiGian;
        public String tenKhach;
        public String sdt;
        public String thuNgan;
        public long tongTienHang;
        public long giamGia;
        public long tienDoiDiem;
        public long tienBuTruDoiTra;
        public long tongThanhToan;
        public String phuongThuc;
        public String trangThai;
        public List<Object[]> dsMon = new ArrayList<>();
    }

    private final List<DonHangTam> dsDonTam = new ArrayList<>();
    private static final List<HoaDonDaThanhToan> dsLichSuHoaDon = new ArrayList<>();
    private static final DateTimeFormatter STATIC_DTF = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    static {
        dongBoLichSuHoaDonTuDatabase();
    }

    public static void dongBoLichSuHoaDonTuDatabase() {
        try {
            List<entity.HoaDon> dbList = new bus.BanHangBus().layTatCaHoaDon();
            if (dbList != null && !dbList.isEmpty()) {
                dsLichSuHoaDon.clear();
                DecimalFormat dfLocal = new DecimalFormat("#,### đ");
                for (entity.HoaDon hdDb : dbList) {
                    HoaDonDaThanhToan item = new HoaDonDaThanhToan();
                    item.maHoaDon = hdDb.getMaHoaDon();
                    LocalDateTime tg = hdDb.getThoiGianThanhToan() != null ? hdDb.getThoiGianThanhToan() : hdDb.getNgayLap();
                    item.thoiGian = tg != null ? tg.format(STATIC_DTF) : LocalDateTime.now().format(STATIC_DTF);
                    item.tenKhach = (hdDb.getKhachHang() != null && hdDb.getKhachHang().getHoTen() != null)
                            ? hdDb.getKhachHang().getHoTen() : "Khách vãng lai";
                    item.sdt = (hdDb.getKhachHang() != null && hdDb.getKhachHang().getSoDienThoai() != null)
                            ? hdDb.getKhachHang().getSoDienThoai() : "";
                    item.thuNgan = (hdDb.getNhanVien() != null && hdDb.getNhanVien().getHoTen() != null)
                            ? hdDb.getNhanVien().getHoTen() : "Lê Anh Minh";
                    long giam = hdDb.getGiamGia() != null ? hdDb.getGiamGia().longValue() : 0L;
                    long doiDiem = hdDb.getTienDoiDiem() != null ? hdDb.getTienDoiDiem().longValue() : 0L;
                    long buTru = hdDb.getTienBuTruDoiTra() != null ? hdDb.getTienBuTruDoiTra().longValue() : 0L;
                    long thanh = hdDb.getThanhTien() != null ? hdDb.getThanhTien().longValue() : 0L;
                    item.giamGia = giam + doiDiem;
                    item.tienDoiDiem = doiDiem;
                    item.tienBuTruDoiTra = buTru;
                    item.tongThanhToan = thanh;
                    item.tongTienHang = 0L;
                    item.phuongThuc = hdDb.getPhuongThucThanhToan() != null ? hdDb.getPhuongThucThanhToan() : "Tiền mặt";
                    item.trangThai = hdDb.getTrangThaiHoaDon() != null ? hdDb.getTrangThaiHoaDon() : "Đã thanh toán";

                    int stt = 1;
                    if (hdDb.getDsChiTiet() != null) {
                        for (entity.ChiTietHoaDon ct : hdDb.getDsChiTiet()) {
                            long dg = ct.getDonGiaLucBan() != null ? ct.getDonGiaLucBan().longValue() : 0L;
                            int sl = ct.getSoLuong();
                            long tt = ct.getThanhTien() != null ? ct.getThanhTien().longValue() : (dg * sl);
                            item.tongTienHang = Math.addExact(item.tongTienHang, tt);
                            if (ct instanceof entity.ChiTietXuatLo) {
                                entity.ChiTietXuatLo ctx = (entity.ChiTietXuatLo) ct;
                                String maSP = (ctx.getLoHang() != null && ctx.getLoHang().getSanPham() != null)
                                        ? ctx.getLoHang().getSanPham().getMaSanPham() : "SP001";
                                String tenSP = (ctx.getLoHang() != null && ctx.getLoHang().getSanPham() != null)
                                        ? ctx.getLoHang().getSanPham().getTenSanPham() : "Sản phẩm";
                                String dvt = (ctx.getQuyDoiDonVi() != null && ctx.getQuyDoiDonVi().getDonViTinh() != null)
                                        ? ctx.getQuyDoiDonVi().getDonViTinh().getTenDonVi() : "Đơn vị";
                                item.dsMon.add(new Object[]{stt++, maSP, tenSP, dvt, sl, dfLocal.format(dg), dfLocal.format(tt),
                                        ctx.getLoHang() != null ? ctx.getLoHang().getSoLo() : null,
                                        ctx.getSoLuongXuatCoSo()});
                            } else if (ct instanceof entity.ChiTietDichVu) {
                                entity.ChiTietDichVu ctd = (entity.ChiTietDichVu) ct;
                                String maDV = ctd.getDichVu() != null ? ctd.getDichVu().getMaDichVu() : "DV001";
                                String tenDV = ctd.getDichVu() != null ? ctd.getDichVu().getTenDichVu() : "Dịch vụ";
                                String dvt = (ctd.getDichVu() != null && ctd.getDichVu().getThoiLuongPhut() > 0)
                                        ? (ctd.getDichVu().getThoiLuongPhut() + " phút") : "Gói";
                                item.dsMon.add(new Object[]{stt++, maDV, tenDV, dvt, sl, dfLocal.format(dg), dfLocal.format(tt), CAC_BUOC_TIEN_DO[3]});
                            }
                        }
                    }
                    dsLichSuHoaDon.add(item);
                }
                return;
            }
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi đồng bộ lịch sử HĐ từ CSDL: " + e.getMessage());
        }
    }

    private static void dongBoLichSuHoaDonNeuCan() {
        if (dsLichSuHoaDon.isEmpty()) {
            dongBoLichSuHoaDonTuDatabase();
        }
    }

    public static List<HoaDonDaThanhToan> layDanhSachHoaDonDaThanhToan() {
        dongBoLichSuHoaDonNeuCan();
        return dsLichSuHoaDon;
    }

    public static boolean laHoaDonHomNay(HoaDonDaThanhToan hd) {
        if (hd == null || hd.thoiGian == null) return false;
        String todayStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        return hd.thoiGian.contains(todayStr);
    }

    public static boolean coSanPhamBanLe(HoaDonDaThanhToan hd) {
        if (hd == null || hd.dsMon == null) return false;
        for (Object[] mon : hd.dsMon) {
            String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
            if (ma.startsWith("SP")) return true;
        }
        return false;
    }

    public static boolean coDichVuSpa(HoaDonDaThanhToan hd) {
        if (hd == null || hd.dsMon == null) return false;
        for (Object[] mon : hd.dsMon) {
            String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
            if (ma.startsWith("DV")) return true;
        }
        return false;
    }

    public static boolean laHoaDonHopLe(HoaDonDaThanhToan hd) {
        if (hd == null) return false;
        if (hd.trangThai != null && (hd.trangThai.toLowerCase().contains("hủy") || hd.trangThai.toLowerCase().contains("huy"))) {
            return false;
        }
        return true;
    }

    public static long tinhTongDoanhThuHomNay() {
        dongBoLichSuHoaDonNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            sum += hd.tongThanhToan;
        }
        return sum;
    }

    public static int demTongSoHoaDonHomNay() {
        dongBoLichSuHoaDonNeuCan();
        int count = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (laHoaDonHomNay(hd) && laHoaDonHopLe(hd)) count++;
        }
        return count;
    }

    public static int demSoDonBanLeHomNay() {
        dongBoLichSuHoaDonNeuCan();
        int count = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            if (coSanPhamBanLe(hd)) count++;
        }
        return count;
    }

    public static int demSoDonDichVuHomNay() {
        dongBoLichSuHoaDonNeuCan();
        int count = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            if (coDichVuSpa(hd)) count++;
        }
        return count;
    }

    private static long parseTienMon(Object val) {
        if (val == null) return 0L;
        String num = val.toString().replaceAll("[^0-9]", "").trim();
        return num.isEmpty() ? 0L : Long.parseLong(num);
    }

    public static long tinhDoanhThuDichVuSpaHomNay() {
        dongBoLichSuHoaDonNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (ma.startsWith("DV") && mon.length > 6) {
                    sum += parseTienMon(mon[6]);
                }
            }
        }
        return sum;
    }

    public static long tinhDoanhThuHangHoaHomNay() {
        dongBoLichSuHoaDonNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (ma.startsWith("SP") && mon.length > 6) {
                    sum += parseTienMon(mon[6]);
                }
            }
        }
        return sum;
    }

    public static long tinhDoanhThuKhacHomNay() {
        dongBoLichSuHoaDonNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            if (!laHoaDonHomNay(hd) || !laHoaDonHopLe(hd)) continue;
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (!ma.startsWith("SP") && !ma.startsWith("DV") && mon.length > 6) {
                    sum += parseTienMon(mon[6]);
                }
            }
        }
        return sum;
    }

    private static final List<BanHangPOSPanel> INSTANCES = new ArrayList<>();

    public static class SanPhamDoiTra {
        public String maSP;
        public String tenSP;
        public String dvt;
        public long donGia;
        public int tonKho;

        public SanPhamDoiTra(String maSP, String tenSP, String dvt, long donGia, int tonKho) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.dvt = dvt;
            this.donGia = donGia;
            this.tonKho = tonKho;
        }

        @Override
        public String toString() {
            return maSP + " - " + tenSP + " (" + new DecimalFormat("#,### đ").format(donGia) + ")";
        }
    }

    public static List<SanPhamDoiTra> layDanhSachSanPhamDoi() {
        List<SanPhamDoiTra> list = new ArrayList<>();
        if (!INSTANCES.isEmpty()) {
            BanHangPOSPanel inst = INSTANCES.get(0);
            for (CatalogItem it : inst.allItems) {
                if (it.ma.startsWith("SP")) {
                    list.add(new SanPhamDoiTra(it.ma, it.ten, it.dvt, it.donGia, it.tonKho));
                }
            }
        }
        if (list.isEmpty()) {
            list.add(new SanPhamDoiTra("SP001", "Hạt Royal Canin Kitten 2kg", "Gói", 380000, 45));
            list.add(new SanPhamDoiTra("SP002", "Pate Whiskas Vị Cá Thu 85g", "Lon", 20000, 180));
            list.add(new SanPhamDoiTra("SP003", "Pate Whiskas (Lốc 6 lon)", "Lốc", 115000, 30));
            list.add(new SanPhamDoiTra("SP004", "Vòng Cổ Chuông Nơ Thú Cưng", "Chiếc", 45000, 60));
            list.add(new SanPhamDoiTra("SP005", "Sữa Tắm Dưỡng Lông Bio-Care", "Chai", 85000, 35));
            list.add(new SanPhamDoiTra("SP006", "Cát Vệ Sinh Mèo Hương Táo 5kg", "Túi", 65000, 50));
        }
        return list;
    }

    public static void congTonKhoCatalog(String maSP, int soLuong) {
        if (maSP == null || soLuong <= 0) return;
        for (BanHangPOSPanel inst : INSTANCES) {
            for (CatalogItem item : inst.allItems) {
                if (item.ma.equalsIgnoreCase(maSP) || item.ten.equalsIgnoreCase(maSP)) {
                    item.tonKho += soLuong;
                    break;
                }
            }
            inst.locVaHienThiCards();
        }
    }

    public static void truTonKhoCatalog(String maSP, int soLuong) {
        if (maSP == null || soLuong <= 0) return;
        for (BanHangPOSPanel inst : INSTANCES) {
            for (CatalogItem item : inst.allItems) {
                if (item.ma.equalsIgnoreCase(maSP) || item.ten.equalsIgnoreCase(maSP)) {
                    item.tonKho = Math.max(0, item.tonKho - soLuong);
                    break;
                }
            }
            inst.locVaHienThiCards();
        }
    }

    public static void dongBoToanBoCatalogVaGiaoDien() {
        for (BanHangPOSPanel inst : INSTANCES) {
            inst.dongBoTonKhoCatalogTuDatabase();
            inst.locVaHienThiCards();
        }
    }

    // Catalog items
    private static class CatalogItem {
        String ma;
        String ten;
        String loai;
        String dvt;
        String soLoHoacKtv;
        int tonKho;
        long donGia;
        String iconName;
        String moTaNgan;
        String maVach;

        CatalogItem(String ma, String ten, String loai, String dvt, String soLoHoacKtv, int tonKho, long donGia, String iconName, String moTaNgan, String maVach) {
            this.ma = ma;
            this.ten = ten;
            this.loai = loai;
            this.dvt = dvt;
            this.soLoHoacKtv = soLoHoacKtv;
            this.tonKho = tonKho;
            this.donGia = donGia;
            this.iconName = iconName;
            this.moTaNgan = moTaNgan;
            this.maVach = (maVach != null) ? maVach : "";
        }

        CatalogItem(String ma, String ten, String loai, String dvt, String soLoHoacKtv, int tonKho, long donGia, String iconName, String moTaNgan) {
            this(ma, ten, loai, dvt, soLoHoacKtv, tonKho, donGia, iconName, moTaNgan, "");
        }
    }

    private final List<CatalogItem> allItems = new ArrayList<>();
    private final List<CatalogItem> filteredItems = new ArrayList<>();
    private String currentCategory = "TẤT CẢ";
    private String currentSearchKeyword = "";

    // UI Tab 1 (POS) - Nâng cấp 2 Tab & Barcode quét mã
    private JTabbedPane tabPosCatalog;
    private JTextField txtQuetBarcode;
    private JTextField txtTimKiemHangHoa;
    private JTable tblDanhMucHangHoa;
    private DefaultTableModel modelHangHoa;
    private TableRowSorter<DefaultTableModel> sorterHangHoa;

    private JPanel pnlGridCards;
    private JTextField txtTimKiem;
    private final List<JButton> filterButtons = new ArrayList<>();

    private JLabel lblMaHoaDon;
    private JTable tblGioHang;
    private DefaultTableModel modelGioHang;
    private JTextField txtSdtKhachHang;
    private JTextField txtTenKhachHang;
    private JLabel lblDiemTichLuy;
    private JSpinner spnDiemDung;
    private JLabel lblTienDoiDiemInfo;
    private JCheckBox chkDungDiem;
    private JLabel lblTongTienHang;
    private JLabel lblGiamGia;
    private JLabel lblKhachCanTra;
    private JTextField txtTienKhachDua;
    private JLabel lblTienThoiLai;
    private JComboBox<String> cboPhuongThuc;

    private int diemHienCo = 0;
    private long tienDoiDiem = 0;
    private boolean tenKhachTuDongDien = false;
    private boolean dangCapNhatKhach = false;
    private boolean dangCapNhatSoLuongBang = false;

    private long tongTienHang = 0;
    private long tienGiamVip = 0;
    private long tienGiamGia = 0;

    // Mô hình 1: Thẻ Spa Pass (Punch Card)
    private final SpaPassBus SpaPassBus = new SpaPassBus();
    private TheSpaPass theSpaPassHienTai = null;
    private JLabel lblSpaPassStatus;
    private JButton btnDungSpaPass;

    // Mô hình 3: Allergy Guard & Smart Cross-Selling
    private JLabel lblThuCungDangChon;
    private JPanel pnlCrossSell;
    private JLabel lblCrossSellText;
    private JButton btnThemCrossSell;
    private CatalogItem crossSellItemHienTai = null;

    // Luồng Đổi trả chuyển sang POS thanh toán (Milestone 2)
    private DoiTraTransferData thongTinDoiTraHienTai = null;
    private final java.util.Map<String, entity.HotelCheckoutSnapshot> mapHotelSnapshots = new java.util.HashMap<>();
    private JPanel pnlThongBaoDoiTra;
    private JLabel lblThongBaoDoiTra;
    private JButton btnHuyDoiTra;

    // UI Tab 2 (Đơn chờ)
    private JPanel pnlCardsDonCho;
    private JLabel lblSoDonChoHeader;
    private JTextField txtTimDonCho;

    // UI Tab 3 (Lịch sử)
    private DefaultTableModel modelLichSu;
    private JTable tblLichSu;
    private TableRowSorter<DefaultTableModel> sorterLichSu;
    private JTextField txtTimKiemLichSu;
    private JComboBox<String> cboLocNgayLichSu;
    private JComboBox<String> cboLocLoaiLichSu;
    private final List<HoaDonDaThanhToan> dsLichSuHienThi = new ArrayList<>();

    public BanHangPOSPanel() {
        if (!INSTANCES.contains(this)) {
            INSTANCES.add(this);
        }
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BACKGROUND);

        khoiTaoDuLieuCatalog();
        dongBoLichSuHoaDon();

        posCardLayout = new CardLayout();
        pnlCardContainer = new JPanel(posCardLayout);
        pnlCardContainer.setBackground(ThemeColors.BACKGROUND);

        pnlCardContainer.add(taoTabPosBanHang(), VIEW_POS);
        pnlCardContainer.add(taoTabDonChoGrid(), VIEW_DON_CHO);
        pnlCardContainer.add(taoTabLichSuHoaDon(), VIEW_LICH_SU);

        add(pnlCardContainer, BorderLayout.CENTER);

        taiDsDonTamTuDisk();
        capNhatGiaoDienDonCho();
        locVaHienThiCards();
        capNhatTongTien();
    }

    public void chonTabPOS() {
        if (posCardLayout != null && pnlCardContainer != null) {
            posCardLayout.show(pnlCardContainer, VIEW_POS);
        }
    }

    public void chonTabDonCho() {
        if (posCardLayout != null && pnlCardContainer != null) {
            capNhatGiaoDienDonCho();
            posCardLayout.show(pnlCardContainer, VIEW_DON_CHO);
        }
    }

    public void chonTabLichSuHoaDon() {
        chonTabLichSuHoaDonTheoBoLoc("Hôm nay", "Tất cả loại đơn");
    }

    public void chonTabLichSuHoaDonTheoBoLoc(String khoangNgay, String loaiDon) {
        if (cboLocNgayLichSu != null && khoangNgay != null) {
            cboLocNgayLichSu.setSelectedItem(khoangNgay);
        }
        if (cboLocLoaiLichSu != null && loaiDon != null) {
            cboLocLoaiLichSu.setSelectedItem(loaiDon);
        }
        if (posCardLayout != null && pnlCardContainer != null) {
            capNhatBangLichSu();
            posCardLayout.show(pnlCardContainer, VIEW_LICH_SU);
        }
    }

    public void chonTabLichSu() {
        if (cboLocNgayLichSu != null) {
            cboLocNgayLichSu.setSelectedItem("Tất cả lịch sử");
        }
        if (cboLocLoaiLichSu != null) {
            cboLocLoaiLichSu.setSelectedItem("Tất cả loại đơn");
        }
        if (posCardLayout != null && pnlCardContainer != null) {
            capNhatBangLichSu();
            posCardLayout.show(pnlCardContainer, VIEW_LICH_SU);
        }
    }

    public void xuLyLapHoaDon() {
        chonTabPOS();
    }

    public void traCuuKhachHangVaVIP(String sdt) {
        chonTabPOS();
        if (txtSdtKhachHang != null && sdt != null) {
            txtSdtKhachHang.setText(sdt);
        }
    }

    public void traCuuHoaDonCu(String maHD) {
        chonTabLichSu();
        if (txtTimKiemLichSu != null && maHD != null) {
            txtTimKiemLichSu.setText(maHD);
        }
    }

    public void xuLyDoiTraHang() {
        chonTabLichSu();
        JOptionPane.showMessageDialog(this,
                "Để xử lý đổi trả hàng hoặc hoàn tiền:\n1. Chọn hóa đơn cần đổi trả trong bảng Lịch sử\n2. Bấm 'Chi tiết / In lại HĐ' và thực hiện hoàn tiền theo quy định.",
                "Hướng dẫn đổi trả hàng", JOptionPane.INFORMATION_MESSAGE);
    }

    private void khoiTaoDuLieuCatalog() {
        dongBoTonKhoCatalogTuDatabase();
    }

    public void dongBoTonKhoCatalogTuDatabase() {
        allItems.clear();
        try {
            bus.BanHangBus busPos = new bus.BanHangBus();
            java.util.List<bus.BanHangBus.CatalogItem> busItems = busPos.layDanhSachMatHangCatalog();
            for (bus.BanHangBus.CatalogItem bi : busItems) {
                if (!bi.laDichVu) {
                    allItems.add(new CatalogItem(
                            bi.ma, bi.tenHienThi, "Hàng hóa", bi.dvt, "Kho chính",
                            bi.tonTheoQuyCach, bi.gia, IconHelper.BOXES, (bi.moTa != null && !bi.moTa.isEmpty()) ? bi.moTa : "Sản phẩm chính hãng",
                            bi.maVach
                    ));
                } else {
                    String nhom = "Grooming cơ bản";
                    String icon = IconHelper.SCISSORS;
                    String dvt = bi.thoiLuongPhut > 0 ? (bi.thoiLuongPhut + " phút") : "Buổi";
                    if (bi.ten.contains("Spa") || bi.ten.contains("Ozone") || bi.ten.contains("Thảo Dược") || bi.ten.contains("Khử Mùi")) {
                        nhom = "Spa & Trị liệu";
                        icon = IconHelper.SPARKLES;
                    } else if (bi.ten.contains("Nhuộm")) {
                        nhom = "Tạo kiểu & Nhuộm";
                        icon = IconHelper.SPARKLES;
                    } else if (bi.ten.contains("Hotel") || bi.ten.contains("Lưu Chuồng")) {
                        nhom = "Khách sạn thú cưng";
                        icon = IconHelper.HOME;
                        dvt = "24 giờ";
                    }
                    allItems.add(new CatalogItem(
                            bi.ma, bi.ten, nhom, dvt, "KTV chuyên nghiệp",
                            99, bi.gia, icon, bi.ten
                    ));
                }
            }
            napDuLieuBangHangHoa();
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi nạp Catalog từ CSDL: " + e.getMessage());
        }
    }
//    -----

    private void dongBoLichSuHoaDon() {
        dongBoLichSuHoaDonNeuCan();
    }

    private void ganBoLocSoNguyenChoSpinner(JSpinner spinner) {
        if (spinner == null) return;
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            javax.swing.text.Document doc = tf.getDocument();
            if (doc instanceof javax.swing.text.AbstractDocument) {
                ((javax.swing.text.AbstractDocument) doc).setDocumentFilter(new javax.swing.text.DocumentFilter() {
                    @Override
                    public void insertString(FilterBypass fb, int offset, String string, javax.swing.text.AttributeSet attr)
                            throws javax.swing.text.BadLocationException {
                        if (string != null && string.matches("\\d+")) {
                            super.insertString(fb, offset, string, attr);
                        }
                    }

                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text, javax.swing.text.AttributeSet attrs)
                            throws javax.swing.text.BadLocationException {
                        if (text != null && text.matches("\\d*")) {
                            super.replace(fb, offset, length, text, attrs);
                        }
                    }
                });
            }
        }
    }

    private void capNhatTienDoiDiemTuSpinner() {
        if (spnDiemDung == null) return;
        try {
            spnDiemDung.commitEdit();
        } catch (Exception ignored) {}
        int soDiemChon = ((Number) spnDiemDung.getValue()).intValue();
        if (soDiemChon < 0) {
            soDiemChon = 0;
            spnDiemDung.setValue(0);
        } else if (diemHienCo > 0 && soDiemChon > diemHienCo) {
            soDiemChon = diemHienCo;
            spnDiemDung.setValue(diemHienCo);
        }
        tienDoiDiem = soDiemChon * 1000L;
        if (lblTienDoiDiemInfo != null) {
            lblTienDoiDiemInfo.setText("(-" + df.format(tienDoiDiem) + ")");
        }
    }

    private void capNhatDiemSpinner() {
        if (spnDiemDung == null) return;
        if (diemHienCo > 0) {
            long tongSauVip = Math.max(0, tongTienHang - (diemHienCo >= 100 ? (long)(tongTienHang * 0.05) : 0));
            int maxDiemTheoDon = (int) (tongSauVip / 1000L);
            int maxDiem = Math.min(diemHienCo, maxDiemTheoDon);
            int curVal = ((Number) spnDiemDung.getValue()).intValue();
            if (curVal > maxDiem) curVal = maxDiem;
            if (curVal < 0) curVal = 0;
            spnDiemDung.setModel(new SpinnerNumberModel(curVal, 0, Math.max(0, maxDiem), 1));
            ganBoLocSoNguyenChoSpinner(spnDiemDung);
            spnDiemDung.setEnabled(maxDiem > 0);
            capNhatTienDoiDiemTuSpinner();
        } else {
            spnDiemDung.setModel(new SpinnerNumberModel(0, 0, 0, 1));
            ganBoLocSoNguyenChoSpinner(spnDiemDung);
            spnDiemDung.setEnabled(false);
            tienDoiDiem = 0;
            if (lblTienDoiDiemInfo != null) {
                lblTienDoiDiemInfo.setText("(-0 đ)");
            }
        }
    }

    // ==========================================
    // 1. TAB BÁN HÀNG POS CHÍNH (CATALOG + CART)
    // ==========================================
    private JPanel taoTabPosBanHang() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBorder(new EmptyBorder(10, 12, 10, 12));
        pnl.setOpaque(false);

        JPanel pnlCatalog = taoPanelCatalogGrid();
        JPanel pnlHoaDon = taoPanelHoaDonVaThanhToan();

        GridBagConstraints gbcCatalog = new GridBagConstraints();
        gbcCatalog.gridx = 0;
        gbcCatalog.gridy = 0;
        gbcCatalog.weightx = 0.56;
        gbcCatalog.weighty = 1.0;
        gbcCatalog.fill = GridBagConstraints.BOTH;
        gbcCatalog.insets = new Insets(0, 0, 0, 12);

        GridBagConstraints gbcHoaDon = new GridBagConstraints();
        gbcHoaDon.gridx = 1;
        gbcHoaDon.gridy = 0;
        gbcHoaDon.weightx = 0.44;
        gbcHoaDon.weighty = 1.0;
        gbcHoaDon.fill = GridBagConstraints.BOTH;

        pnl.add(pnlCatalog, gbcCatalog);
        pnl.add(pnlHoaDon, gbcHoaDon);
        return pnl;
    }

    private JPanel taoPanelCatalogGrid() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);

        // Thanh thao tác Barcode & Điều hướng nhanh
        JPanel pnlTopAction = new JPanel(new BorderLayout(8, 0));
        pnlTopAction.setOpaque(false);

        JPanel pnlBarcode = new JPanel(new BorderLayout(6, 0));
        pnlBarcode.setOpaque(false);

        txtQuetBarcode = new JTextField();
        txtQuetBarcode.setPreferredSize(new Dimension(0, 38));
        txtQuetBarcode.putClientProperty("JTextField.placeholderText", "Quét mã vạch Barcode hoặc nhập mã SP rồi nhấn Enter [F2]...");
        txtQuetBarcode.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.BOXES, 16, ThemeColors.PRIMARY_ACTIVE));
        txtQuetBarcode.setBorder(ThemeColors.createInputBorder());
        txtQuetBarcode.addActionListener(e -> xuLyQuetBarcode(txtQuetBarcode.getText().trim()));

        // Đăng ký phím tắt F2 để nhảy vào ô Barcode
        registerKeyboardAction(e -> {
            if (txtQuetBarcode != null) {
                txtQuetBarcode.requestFocusInWindow();
                txtQuetBarcode.selectAll();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        pnlBarcode.add(txtQuetBarcode, BorderLayout.CENTER);

        JPanel pnlQuickNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlQuickNav.setOpaque(false);

        btnQuickDonCho = ThemeColors.createSecondaryButton("Đơn chờ (" + dsDonTam.size() + ")", IconHelper.getIcon(IconHelper.CLOCK, 15, ThemeColors.WARNING));
        btnQuickDonCho.setPreferredSize(new Dimension(126, 38));
        btnQuickDonCho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickDonCho.addActionListener(e -> chonTabDonCho());

        JButton btnQuickLichSu = ThemeColors.createSecondaryButton("Lịch sử HĐ", IconHelper.getIcon(IconHelper.RECEIPT, 15, ThemeColors.PRIMARY_ACTIVE));
        btnQuickLichSu.setPreferredSize(new Dimension(118, 38));
        btnQuickLichSu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickLichSu.addActionListener(e -> chonTabLichSu());

        pnlQuickNav.add(btnQuickDonCho);
        pnlQuickNav.add(btnQuickLichSu);

        pnlTopAction.add(pnlBarcode, BorderLayout.CENTER);
        pnlTopAction.add(pnlQuickNav, BorderLayout.EAST);

        // JTabbedPane tách riêng Hàng hóa bán lẻ và Dịch vụ Spa
        tabPosCatalog = new JTabbedPane();
        tabPosCatalog.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabPosCatalog.addTab("📦 Hàng Hóa Bán Lẻ", taoTabHangHoaBanLe());
        tabPosCatalog.addTab("✂️ Dịch Vụ Spa & Chăm Sóc", taoTabDichVuSpa());

        pnl.add(pnlTopAction, BorderLayout.NORTH);
        pnl.add(tabPosCatalog, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel taoTabHangHoaBanLe() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        txtTimKiemHangHoa = new JTextField();
        txtTimKiemHangHoa.setPreferredSize(new Dimension(0, 36));
        txtTimKiemHangHoa.putClientProperty("JTextField.placeholderText", "Tìm nhanh hàng hóa theo tên, quy cách, mã SP, mã vạch...");
        txtTimKiemHangHoa.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimKiemHangHoa.setBorder(ThemeColors.createInputBorder());

        txtTimKiemHangHoa.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { locBangHangHoa(); }
            @Override
            public void removeUpdate(DocumentEvent e) { locBangHangHoa(); }
            @Override
            public void changedUpdate(DocumentEvent e) { locBangHangHoa(); }
        });

        pnlSearch.add(txtTimKiemHangHoa, BorderLayout.CENTER);

        String[] cols = {"Mã SP", "Mã vạch", "Tên hàng hóa & Quy cách", "ĐVT", "Đơn giá", "Tồn kho", "Thao tác"};
        modelHangHoa = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblDanhMucHangHoa = new JTable(modelHangHoa);
        tblDanhMucHangHoa.setRowHeight(34);
        tblDanhMucHangHoa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDanhMucHangHoa.setShowHorizontalLines(true);
        tblDanhMucHangHoa.setShowVerticalLines(false);
        tblDanhMucHangHoa.setGridColor(ThemeColors.BORDER_LIGHT);
        tblDanhMucHangHoa.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblDanhMucHangHoa.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tblDanhMucHangHoa.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDanhMucHangHoa.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblDanhMucHangHoa.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tblDanhMucHangHoa.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tblDanhMucHangHoa.getColumnModel().getColumn(0).setPreferredWidth(65);
        tblDanhMucHangHoa.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        tblDanhMucHangHoa.getColumnModel().getColumn(1).setPreferredWidth(95);
        tblDanhMucHangHoa.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        tblDanhMucHangHoa.getColumnModel().getColumn(2).setPreferredWidth(200);

        tblDanhMucHangHoa.getColumnModel().getColumn(3).setPreferredWidth(55);
        tblDanhMucHangHoa.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tblDanhMucHangHoa.getColumnModel().getColumn(4).setPreferredWidth(85);
        tblDanhMucHangHoa.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        tblDanhMucHangHoa.getColumnModel().getColumn(5).setPreferredWidth(60);
        tblDanhMucHangHoa.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        tblDanhMucHangHoa.getColumnModel().getColumn(6).setPreferredWidth(70);
        tblDanhMucHangHoa.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setForeground(ThemeColors.PRIMARY_ACTIVE);
                lbl.setBackground(isSelected ? ThemeColors.PRIMARY_SOFT : ThemeColors.SURFACE);
                return lbl;
            }
        });

        sorterHangHoa = new TableRowSorter<>(modelHangHoa);
        tblDanhMucHangHoa.setRowSorter(sorterHangHoa);

        tblDanhMucHangHoa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || tblDanhMucHangHoa.getSelectedColumn() == 6) {
                    int row = tblDanhMucHangHoa.getSelectedRow();
                    if (row >= 0) {
                        int modelRow = tblDanhMucHangHoa.convertRowIndexToModel(row);
                        String ma = (String) modelHangHoa.getValueAt(modelRow, 0);
                        String dvt = (String) modelHangHoa.getValueAt(modelRow, 3);
                        themHangHoaTheoMaVaDvtVaoGio(ma, dvt);
                    }
                }
            }
        });

        tblDanhMucHangHoa.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "addSelectedProductToCart");
        tblDanhMucHangHoa.getActionMap().put("addSelectedProductToCart", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = tblDanhMucHangHoa.getSelectedRow();
                if (row >= 0) {
                    int modelRow = tblDanhMucHangHoa.convertRowIndexToModel(row);
                    String ma = (String) modelHangHoa.getValueAt(modelRow, 0);
                    String dvt = (String) modelHangHoa.getValueAt(modelRow, 3);
                    themHangHoaTheoMaVaDvtVaoGio(ma, dvt);
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblDanhMucHangHoa);
        scrollTable.setBorder(ThemeColors.createInputBorder());
        scrollTable.getViewport().setBackground(ThemeColors.SURFACE);

        pnl.add(pnlSearch, BorderLayout.NORTH);
        pnl.add(scrollTable, BorderLayout.CENTER);

        napDuLieuBangHangHoa();
        return pnl;
    }

    private JPanel taoTabDichVuSpa() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 6));
        pnlHeader.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(0, 36));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm kiếm dịch vụ Spa, Grooming theo tên, mã...");
        txtTimKiem.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimKiem.setBorder(ThemeColors.createInputBorder());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { capNhatTimKiem(); }
            @Override
            public void removeUpdate(DocumentEvent e) { capNhatTimKiem(); }
            @Override
            public void changedUpdate(DocumentEvent e) { capNhatTimKiem(); }
        });

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilters.setOpaque(false);

        String[] categories = {
                "TẤT CẢ",
                "Grooming cơ bản",
                "Spa & Trị liệu",
                "Tạo kiểu & Nhuộm",
                "Khách sạn thú cưng"
        };

        filterButtons.clear();
        for (String cat : categories) {
            JButton btnFilter = taoFilterButton(cat);
            filterButtons.add(btnFilter);
            pnlFilters.add(btnFilter);
        }
        if (!filterButtons.isEmpty()) {
            kichHoatFilterButton(filterButtons.get(0));
        }

        pnlHeader.add(txtTimKiem, BorderLayout.NORTH);
        pnlHeader.add(pnlFilters, BorderLayout.SOUTH);

        pnlGridCards = new JPanel();
        pnlGridCards.setLayout(new BoxLayout(pnlGridCards, BoxLayout.Y_AXIS));
        pnlGridCards.setOpaque(false);
        pnlGridCards.setBorder(new EmptyBorder(0, 0, 8, 4));

        JPanel pnlCardsContainer = new JPanel(new BorderLayout());
        pnlCardsContainer.setOpaque(false);
        pnlCardsContainer.add(pnlGridCards, BorderLayout.NORTH);

        JScrollPane scrollCards = new JScrollPane(pnlCardsContainer);
        scrollCards.setBorder(null);
        scrollCards.setOpaque(false);
        scrollCards.getViewport().setOpaque(false);
        scrollCards.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollCards.getVerticalScrollBar().setUnitIncrement(18);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(scrollCards, BorderLayout.CENTER);
        return pnl;
    }

    private void xuLyQuetBarcode(String barcodeOrMa) {
        if (barcodeOrMa == null || barcodeOrMa.trim().isEmpty()) return;
        String code = barcodeOrMa.trim();
        CatalogItem found = null;
        for (CatalogItem it : allItems) {
            if ("Hàng hóa".equalsIgnoreCase(it.loai)) {
                if ((it.maVach != null && it.maVach.equalsIgnoreCase(code)) || it.ma.equalsIgnoreCase(code)) {
                    found = it;
                    break;
                }
            }
        }
        if (found != null) {
            themSanPhamVaoGio(found);
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ignored) {}
            if (txtQuetBarcode != null) {
                txtQuetBarcode.setText("");
                txtQuetBarcode.requestFocusInWindow();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy sản phẩm có mã vạch hoặc mã SP: [" + code + "]",
                    "Không tìm thấy sản phẩm", JOptionPane.WARNING_MESSAGE);
            if (txtQuetBarcode != null) {
                txtQuetBarcode.selectAll();
                txtQuetBarcode.requestFocusInWindow();
            }
        }
    }

    private void themHangHoaTheoMaVaoGio(String ma) {
        themHangHoaTheoMaVaDvtVaoGio(ma, null);
    }

    private void themHangHoaTheoMaVaDvtVaoGio(String ma, String dvt) {
        if (ma == null) return;
        for (CatalogItem it : allItems) {
            if (it.ma.equalsIgnoreCase(ma) && (dvt == null || dvt.isEmpty() || it.dvt.equalsIgnoreCase(dvt))) {
                themSanPhamVaoGio(it);
                return;
            }
        }
    }

    private void locBangHangHoa() {
        if (sorterHangHoa == null || txtTimKiemHangHoa == null) return;
        String kw = txtTimKiemHangHoa.getText().trim();
        if (kw.isEmpty()) {
            sorterHangHoa.setRowFilter(null);
        } else {
            sorterHangHoa.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(kw)));
        }
    }

    private void napDuLieuBangHangHoa() {
        if (modelHangHoa == null) return;
        modelHangHoa.setRowCount(0);
        for (CatalogItem item : allItems) {
            if ("Hàng hóa".equalsIgnoreCase(item.loai)) {
                modelHangHoa.addRow(new Object[]{
                        item.ma,
                        item.maVach != null ? item.maVach : "",
                        item.ten,
                        item.dvt,
                        df.format(item.donGia),
                        item.tonKho,
                        "+ Thêm"
                });
            }
        }
    }

    private JButton taoFilterButton(String category) {
        JButton btn = new JButton(category);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(ThemeColors.SURFACE);
        btn.setForeground(ThemeColors.TEXT_SECONDARY);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(new CompoundBorder(
                new ThemeColors.RoundedBorder(ThemeColors.BORDER, 16, 1),
                new EmptyBorder(5, 14, 5, 14)
        ));

        btn.addActionListener(e -> {
            for (JButton b : filterButtons) {
                b.setBackground(ThemeColors.SURFACE);
                b.setForeground(ThemeColors.TEXT_SECONDARY);
                b.setBorder(new CompoundBorder(
                        new ThemeColors.RoundedBorder(ThemeColors.BORDER, 16, 1),
                        new EmptyBorder(5, 14, 5, 14)
                ));
            }
            kichHoatFilterButton(btn);
            currentCategory = category;
            locVaHienThiCards();
        });
        return btn;
    }

    private void kichHoatFilterButton(JButton btn) {
        btn.setBackground(ThemeColors.PRIMARY_SOFT);
        btn.setForeground(ThemeColors.PRIMARY_ACTIVE);
        btn.setBorder(new CompoundBorder(
                new ThemeColors.RoundedBorder(ThemeColors.BORDER_ACCENT, 16, 1),
                new EmptyBorder(5, 14, 5, 14)
        ));
    }

    private void capNhatTimKiem() {
        currentSearchKeyword = txtTimKiem.getText().trim().toLowerCase();
        locVaHienThiCards();
    }

    private void locVaHienThiCards() {
        if (pnlGridCards == null) return;
        filteredItems.clear();
        List<CatalogItem> dsDichVu = new ArrayList<>();

        for (CatalogItem item : allItems) {
            if ("Hàng hóa".equalsIgnoreCase(item.loai)) continue;

            boolean matchCategory = "TẤT CẢ".equalsIgnoreCase(currentCategory) ||
                    item.loai.equalsIgnoreCase(currentCategory);
            boolean matchKeyword = currentSearchKeyword.isEmpty() ||
                    item.ten.toLowerCase().contains(currentSearchKeyword) ||
                    item.ma.toLowerCase().contains(currentSearchKeyword) ||
                    (item.moTaNgan != null && item.moTaNgan.toLowerCase().contains(currentSearchKeyword));
            if (matchCategory && matchKeyword) {
                filteredItems.add(item);
                dsDichVu.add(item);
            }
        }

        pnlGridCards.removeAll();

        if (!dsDichVu.isEmpty()) {
            pnlGridCards.add(taoBannerPhanCachDanhMuc(
                    "DANH MỤC DỊCH VỤ SPA, GROOMING & CHĂM SÓC (" + dsDichVu.size() + " dịch vụ)",
                    IconHelper.SCISSORS,
                    ThemeColors.PRIMARY_TINT,
                    ThemeColors.PRIMARY_ACTIVE,
                    "Nhấn vào dịch vụ để chỉ định Kỹ thuật viên & Thú cưng phục vụ"
            ));
            pnlGridCards.add(Box.createVerticalStrut(8));

            JPanel pnlGridDV = new JPanel(new GridLayout(0, 3, 10, 10));
            pnlGridDV.setOpaque(false);
            pnlGridDV.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (CatalogItem dv : dsDichVu) {
                pnlGridDV.add(taoTheCard(dv));
            }
            pnlGridCards.add(pnlGridDV);
        } else {
            JPanel pnlEmpty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
            pnlEmpty.setOpaque(false);
            JLabel lblEmpty = new JLabel("Không tìm thấy dịch vụ phù hợp từ khóa.");
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblEmpty.setForeground(ThemeColors.TEXT_MUTED);
            pnlEmpty.add(lblEmpty);
            pnlGridCards.add(pnlEmpty);
        }

        pnlGridCards.revalidate();
        pnlGridCards.repaint();
    }

    private JPanel taoBannerPhanCachDanhMuc(String title, String iconName, Color bgColor, Color fgColor, String subHint) {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setBackground(ThemeColors.SURFACE);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        banner.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblTitle = new JLabel(title, IconHelper.getIcon(iconName, 16, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);

        JLabel lblHint = new JLabel(subHint, SwingConstants.RIGHT);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(ThemeColors.TEXT_SECONDARY);

        banner.add(lblTitle, BorderLayout.WEST);
        banner.add(lblHint, BorderLayout.EAST);
        return banner;
    }

    private JPanel taoTheCard(CatalogItem item) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setPreferredSize(new Dimension(175, 218));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(8, 10, 8, 10));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlTop = new JPanel(new BorderLayout(6, 0));
        pnlTop.setOpaque(false);

        boolean isDichVu = !"Hàng hóa".equalsIgnoreCase(item.loai);
        String subFolder = isDichVu ? "services" : "products";
        String imagePath = "images/" + subFolder + "/" + item.ma.toLowerCase() + ".png";

        // Ảnh thực tế bo góc (68x68) kèm Vector Fallback an toàn
        JLabel lblThumb = new JLabel(IconHelper.loadImage(imagePath, 68, 68, item.iconName));
        lblThumb.setPreferredSize(new Dimension(68, 68));

        JPanel pnlTopRight = new JPanel(new BorderLayout(0, 4));
        pnlTopRight.setOpaque(false);

        JLabel lblBadge;
        if (isDichVu) {
            lblBadge = ThemeColors.createBadge(item.dvt, ThemeColors.PRIMARY_SOFT, ThemeColors.PRIMARY);
        } else {
            lblBadge = ThemeColors.createBadge("Tồn: " + item.tonKho,
                    item.tonKho > 10 ? ThemeColors.PRIMARY_SOFT : ThemeColors.DANGER_LIGHT,
                    item.tonKho > 10 ? ThemeColors.PRIMARY : ThemeColors.DANGER_TEXT);
        }
        JLabel lblMaNho = new JLabel(item.ma + " · " + item.dvt, SwingConstants.RIGHT);
        lblMaNho.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblMaNho.setForeground(ThemeColors.TEXT_MUTED);

        pnlTopRight.add(lblBadge, BorderLayout.NORTH);
        pnlTopRight.add(lblMaNho, BorderLayout.SOUTH);

        pnlTop.add(lblThumb, BorderLayout.WEST);
        pnlTop.add(pnlTopRight, BorderLayout.EAST);

        // Giữa: Tên sản phẩm/dịch vụ + Mô tả ngắn trực quan
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 3));
        pnlCenter.setOpaque(false);

        JLabel lblTen = new JLabel("<html><b style='color:#264653; font-size:11.5px;'>" + item.ten + "</b></html>");
        lblTen.setVerticalAlignment(SwingConstants.TOP);

        String moTa = item.moTaNgan != null ? item.moTaNgan : (item.ma + " · " + item.loai);
        JLabel lblMoTa = new JLabel("<html><span style='color:#576871; font-size:9.5px;'>" + moTa + "</span></html>");
        lblMoTa.setVerticalAlignment(SwingConstants.TOP);

        pnlCenter.add(lblTen, BorderLayout.NORTH);
        pnlCenter.add(lblMoTa, BorderLayout.CENTER);

        // Đáy: Giá tiền + Nút thêm nhanh
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER_LIGHT));

        JLabel lblGia = new JLabel(df.format(item.donGia));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGia.setForeground(ThemeColors.PRIMARY);

        JLabel lblHint = new JLabel(isDichVu ? "Chọn DV >" : "+ Thêm giỏ");
        lblHint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHint.setForeground(ThemeColors.PRIMARY);

        pnlBottom.add(lblGia, BorderLayout.WEST);
        pnlBottom.add(lblHint, BorderLayout.EAST);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlCenter, BorderLayout.CENTER);
        card.add(pnlBottom, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isDichVu) {
                    moDialogChonDichVu(item);
                } else {
                    themSanPhamVaoGio(item);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(ThemeColors.PRIMARY_SOFT);
                card.setBorder(new CompoundBorder(
                        new ThemeColors.RoundedBorder(ThemeColors.PRIMARY, 18, 1),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(ThemeColors.SURFACE);
                card.setBorder(ThemeColors.createCardBorder(8, 10, 8, 10));
            }
        });

        return card;
    }

    private void moDialogChonDichVu(CatalogItem item) {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ChonDichVuDialog dialog = new ChonDichVuDialog(
                topFrame,
                item.ma,
                item.ten,
                item.donGia,
                item.dvt,
                (maDV, moTaChiTiet, thoiLuong, tongGiaDichVu, tenKtv) -> {
                    themDichVuDaCauHinhVaoGio(maDV, moTaChiTiet, thoiLuong, tongGiaDichVu);
                }
        );
        dialog.setVisible(true);
    }

    private void themDichVuDaCauHinhVaoGio(String maDV, String moTaChiTiet, String thoiLuong, long giaDichVu) {
        int stt = modelGioHang.getRowCount() + 1;
        modelGioHang.addRow(new Object[]{
                stt, maDV, moTaChiTiet, thoiLuong, 1, df.format(giaDichVu), df.format(giaDichVu), CAC_BUOC_TIEN_DO[0]
        });
        capNhatTongTien();
    }

//    private void themSanPhamVaoGio(CatalogItem item) {
//        if (!kiemTraVaCanhBaoDiUng(item)) {
//            return;
//        }
//
//        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
//            String ma = (String) modelGioHang.getValueAt(i, 1);
//            if (ma.equals(item.ma)) {
//                int sl = (int) modelGioHang.getValueAt(i, 4);
//                int slMoi = sl + 1;
//                modelGioHang.setValueAt(slMoi, i, 4);
//                modelGioHang.setValueAt(df.format(slMoi * item.donGia), i, 6);
//                capNhatTongTien();
//                return;
//            }
//        }
//
//        int stt = modelGioHang.getRowCount() + 1;
//        modelGioHang.addRow(new Object[]{
//                stt, item.ma, item.ten, item.dvt, 1, df.format(item.donGia), df.format(item.donGia), "—"
//        });
//        capNhatTongTien();
//    }
    
    private void themSanPhamVaoGio(CatalogItem item) {
        if (!kiemTraVaCanhBaoDiUng(item)) {
            return;
        }

        // --- BỔ SUNG: Kiểm tra tồn kho trước khi cho phép thêm vào giỏ hàng ---
        if ("Hàng hóa".equalsIgnoreCase(item.loai)) {
            int soLuongDangTrongGio = laySoLuongDaCoTrongGio(item.ma, item.dvt);
            if (soLuongDangTrongGio + 1 > item.tonKho) {
                JOptionPane.showMessageDialog(this,
                        "Không thể thêm sản phẩm [" + item.ten + "]!\n" +
                        "Số lượng trong giỏ (" + (soLuongDangTrongGio + 1) + ") đã vượt quá số lượng tồn kho hiện có (" + item.tonKho + " " + item.dvt + ").",
                        "Cảnh báo vượt tồn kho", JOptionPane.WARNING_MESSAGE);
                return; // Chặn lại, không cho phép thêm vào giỏ
            }
        }
        // -------------------------------------------------------------------

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String ma = (String) modelGioHang.getValueAt(i, 1);
            String dvtRow = modelGioHang.getValueAt(i, 3) != null ? modelGioHang.getValueAt(i, 3).toString() : "";
            if (ma != null && ma.equalsIgnoreCase(item.ma) && dvtRow.equalsIgnoreCase(item.dvt)) {
                int sl = (int) modelGioHang.getValueAt(i, 4);
                int slMoi = sl + 1;
                
                // Kiểm tra tiếp nếu tăng số lượng trực tiếp trong giỏ hàng
                if ("Hàng hóa".equalsIgnoreCase(item.loai) && slMoi > item.tonKho) {
                    JOptionPane.showMessageDialog(this,
                            "Số lượng sản phẩm trong giỏ không được vượt quá số tồn kho (" + item.tonKho + " " + item.dvt + ")!",
                            "Cảnh báo tồn kho", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                modelGioHang.setValueAt(slMoi, i, 4);
                modelGioHang.setValueAt(df.format(slMoi * item.donGia), i, 6);
                capNhatTongTien();
                return;
            }
        }

        int stt = modelGioHang.getRowCount() + 1;
        modelGioHang.addRow(new Object[]{
                stt, item.ma, item.ten, item.dvt, 1, df.format(item.donGia), df.format(item.donGia), "—"
        });
        capNhatTongTien();
    }

    // Hàm phụ trợ tính tổng số lượng sản phẩm đang có sẵn trong giỏ hàng theo (mã SP, ĐVT)
    private int laySoLuongDaCoTrongGio(String maSP, String dvt) {
        int count = 0;
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String maRow = (String) modelGioHang.getValueAt(i, 1);
            String dvtRow = modelGioHang.getValueAt(i, 3) != null ? modelGioHang.getValueAt(i, 3).toString() : "";
            if (maRow != null && maRow.equalsIgnoreCase(maSP) && (dvt == null || dvtRow.equalsIgnoreCase(dvt))) {
                Object slObj = modelGioHang.getValueAt(i, 4);
                try {
                    count += Integer.parseInt(slObj.toString().replaceAll("\\D+", ""));
                } catch (Exception ignored) {}
            }
        }
        return count;
    }
//    -----

    private JPanel taoPanelHoaDonVaThanhToan() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(8, 12, 8, 12));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", IconHelper.getPrimaryIcon(IconHelper.RECEIPT, 16), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        lblMaHoaDon = new JLabel(utils.MaTuSinhHelper.sinhMaHoaDon(utils.UserSession.getUserId()), SwingConstants.RIGHT);
        lblMaHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMaHoaDon.setForeground(ThemeColors.PRIMARY_ACTIVE);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblMaHoaDon, BorderLayout.EAST);

        // Khu vực Khách hàng thu gọn: 2 dòng nhập gọn (26px) + thanh Điểm & Thao tác giỏ hàng
        JPanel pnlKhachHang = new JPanel(new GridBagLayout());
        pnlKhachHang.setOpaque(false);
        pnlKhachHang.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        GridBagConstraints gbcKh = new GridBagConstraints();
        gbcKh.fill = GridBagConstraints.HORIZONTAL;
        gbcKh.insets = new Insets(2, 2, 2, 2);

        // Ô 1: Số điện thoại / Tên khách + nút Chọn KH nhanh
        JPanel pnlRowSdt = new JPanel(new BorderLayout(4, 0));
        pnlRowSdt.setOpaque(false);
        JLabel lblSdtTitle = new JLabel("Tìm KH:");
        lblSdtTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSdtTitle.setPreferredSize(new Dimension(62, 26));

        txtSdtKhachHang = new JTextField();
        txtSdtKhachHang.setPreferredSize(new Dimension(0, 26));
        txtSdtKhachHang.putClientProperty("JTextField.placeholderText", "Gõ SĐT hoặc tên KH (vd: 0988 / An)...");
        txtSdtKhachHang.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.PHONE, 13, ThemeColors.TEXT_MUTED));
        txtSdtKhachHang.setBorder(ThemeColors.createInputBorder());

        JButton btnTraCuuKhach = new JButton("Chọn KH", IconHelper.getIcon(IconHelper.SEARCH, 12, ThemeColors.PRIMARY_ACTIVE));
        btnTraCuuKhach.setPreferredSize(new Dimension(82, 26));
        btnTraCuuKhach.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTraCuuKhach.setBackground(ThemeColors.PRIMARY_TINT);
        btnTraCuuKhach.setForeground(ThemeColors.PRIMARY_ACTIVE);
        btnTraCuuKhach.setFocusPainted(false);
        btnTraCuuKhach.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTraCuuKhach.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        btnTraCuuKhach.setToolTipText("Nhấn để mở danh sách gợi ý Khách cũ (VIP/Vàng) hoặc tìm theo từ khóa");
        btnTraCuuKhach.addActionListener(e -> hienThiPopupGoiYKhachHang(btnTraCuuKhach));

        txtSdtKhachHang.addActionListener(e -> traCuuKhachHang());
        txtSdtKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { traCuuKhachHang(); }
            @Override
            public void removeUpdate(DocumentEvent e) { traCuuKhachHang(); }
            @Override
            public void changedUpdate(DocumentEvent e) { traCuuKhachHang(); }
        });

        pnlRowSdt.add(lblSdtTitle, BorderLayout.WEST);
        pnlRowSdt.add(txtSdtKhachHang, BorderLayout.CENTER);
        pnlRowSdt.add(btnTraCuuKhach, BorderLayout.EAST);

        // Ô 2: Tên khách hàng + Nút Lưu/Sửa KH trực tiếp tại quầy
        JPanel pnlRowTen = new JPanel(new BorderLayout(4, 0));
        pnlRowTen.setOpaque(false);
        JLabel lblTenTitle = new JLabel("Tên khách:");
        lblTenTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTenTitle.setPreferredSize(new Dimension(62, 26));

        txtTenKhachHang = new JTextField();
        txtTenKhachHang.setPreferredSize(new Dimension(0, 26));
        txtTenKhachHang.putClientProperty("JTextField.placeholderText", "Khách vãng lai (nhập tên nếu khách mới)...");
        txtTenKhachHang.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.USERS, 13, ThemeColors.TEXT_MUTED));
        txtTenKhachHang.setBorder(ThemeColors.createInputBorder());
        txtTenKhachHang.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!dangCapNhatKhach) tenKhachTuDongDien = false;
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!dangCapNhatKhach) tenKhachTuDongDien = false;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!dangCapNhatKhach) tenKhachTuDongDien = false;
            }
        });

        JButton btnQuanLyKhachNhanh = new JButton("Lưu/Sửa", IconHelper.getIcon(IconHelper.EDIT, 11, ThemeColors.TEXT_PRIMARY));
        btnQuanLyKhachNhanh.setPreferredSize(new Dimension(82, 26));
        btnQuanLyKhachNhanh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnQuanLyKhachNhanh.setBackground(ThemeColors.SURFACE);
        btnQuanLyKhachNhanh.setForeground(ThemeColors.TEXT_PRIMARY);
        btnQuanLyKhachNhanh.setFocusPainted(false);
        btnQuanLyKhachNhanh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuanLyKhachNhanh.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        btnQuanLyKhachNhanh.setToolTipText("Thêm mới khách hàng hoặc Sửa/Xóa thông tin khách hàng ngay tại quầy");
        btnQuanLyKhachNhanh.addActionListener(e -> moHopThoaiQuanLyKhachNhanh());

        pnlRowTen.add(lblTenTitle, BorderLayout.WEST);
        pnlRowTen.add(txtTenKhachHang, BorderLayout.CENTER);
        pnlRowTen.add(btnQuanLyKhachNhanh, BorderLayout.EAST);

        // Dòng 3: Thông tin điểm tích lũy + Checkbox dùng điểm
        JPanel pnlDiemContainer = new JPanel(new BorderLayout(6, 0));
        pnlDiemContainer.setOpaque(false);

        lblDiemTichLuy = new JLabel("Khách vãng lai · 0 điểm");
        lblDiemTichLuy.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDiemTichLuy.setForeground(ThemeColors.TEXT_SECONDARY);

        JPanel pnlDiemSub = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlDiemSub.setOpaque(false);

        JLabel lblDungTitle = new JLabel("Dùng điểm:");
        lblDungTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDungTitle.setForeground(ThemeColors.TEXT_SECONDARY);

        spnDiemDung = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
        ganBoLocSoNguyenChoSpinner(spnDiemDung);
        spnDiemDung.setPreferredSize(new Dimension(65, 24));
        spnDiemDung.setFont(new Font("Segoe UI", Font.BOLD, 11));
        spnDiemDung.setEnabled(false);
        spnDiemDung.addChangeListener(e -> {
            capNhatTienDoiDiemTuSpinner();
            capNhatTongTien();
        });

        lblTienDoiDiemInfo = new JLabel("(-0 đ)");
        lblTienDoiDiemInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTienDoiDiemInfo.setForeground(ThemeColors.PRIMARY_ACTIVE);

        pnlDiemSub.add(lblDungTitle);
        pnlDiemSub.add(spnDiemDung);
        pnlDiemSub.add(lblTienDoiDiemInfo);

        pnlDiemContainer.add(lblDiemTichLuy, BorderLayout.WEST);
        pnlDiemContainer.add(pnlDiemSub, BorderLayout.EAST);

        // Dòng 3: Thú cưng đang chọn & Tag dị ứng (Mô hình 3: Allergy Guard)
        JPanel pnlThuCungRow = new JPanel(new BorderLayout(4, 0));
        pnlThuCungRow.setOpaque(false);
        JLabel lblTcTitle = new JLabel("Bé cưng:");
        lblTcTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTcTitle.setPreferredSize(new Dimension(62, 22));

        lblThuCungDangChon = new JLabel("Chưa chọn thú cưng (Khách vãng lai)");
        lblThuCungDangChon.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblThuCungDangChon.setForeground(ThemeColors.TEXT_MUTED);

        pnlThuCungRow.add(lblTcTitle, BorderLayout.WEST);
        pnlThuCungRow.add(lblThuCungDangChon, BorderLayout.CENTER);

        // Dòng 4: Thẻ Spa Pass (Mô hình 1: Punch Card)
        JPanel pnlSpaPassRow = new JPanel(new BorderLayout(6, 0));
        pnlSpaPassRow.setOpaque(false);
        lblSpaPassStatus = new JLabel("Chưa có thẻ Spa Pass");
        lblSpaPassStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSpaPassStatus.setForeground(ThemeColors.TEXT_MUTED);

        btnDungSpaPass = new JButton("Dùng 1 buổi Spa", IconHelper.getIcon(IconHelper.SPARKLES, 11, ThemeColors.PRIMARY_ACTIVE));
        btnDungSpaPass.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnDungSpaPass.setBackground(ThemeColors.PRIMARY_TINT);
        btnDungSpaPass.setForeground(ThemeColors.PRIMARY_ACTIVE);
        btnDungSpaPass.setPreferredSize(new Dimension(135, 22));
        btnDungSpaPass.setEnabled(false);
        btnDungSpaPass.setFocusPainted(false);
        btnDungSpaPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDungSpaPass.addActionListener(e -> suDungLuotSpaPass());

        JButton btnTraCuuSpaPass = new JButton("Tra cứu", IconHelper.getIcon(IconHelper.SEARCH, 11, ThemeColors.TEXT_SECONDARY));
        btnTraCuuSpaPass.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnTraCuuSpaPass.setBackground(ThemeColors.SURFACE);
        btnTraCuuSpaPass.setForeground(ThemeColors.TEXT_PRIMARY);
        btnTraCuuSpaPass.setPreferredSize(new Dimension(80, 22));
        btnTraCuuSpaPass.setFocusPainted(false);
        btnTraCuuSpaPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTraCuuSpaPass.addActionListener(e -> moDialogTraCuuSpaPass());

        JPanel pnlSpaPassActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlSpaPassActions.setOpaque(false);
        pnlSpaPassActions.add(btnTraCuuSpaPass);
        pnlSpaPassActions.add(btnDungSpaPass);

        pnlSpaPassRow.add(lblSpaPassStatus, BorderLayout.CENTER);
        pnlSpaPassRow.add(pnlSpaPassActions, BorderLayout.EAST);

        gbcKh.gridx = 0; gbcKh.gridy = 0; gbcKh.weightx = 1.0;
        pnlKhachHang.add(pnlRowSdt, gbcKh);
        gbcKh.gridy = 1;
        pnlKhachHang.add(pnlRowTen, gbcKh);
        gbcKh.gridy = 2;
        pnlKhachHang.add(pnlDiemContainer, gbcKh);
        gbcKh.gridy = 3;
        pnlKhachHang.add(pnlThuCungRow, gbcKh);
        gbcKh.gridy = 4;
        pnlKhachHang.add(pnlSpaPassRow, gbcKh);

        // Cho phép sửa trực tiếp cột SL (col == 4) từ bàn phím
        String[] cols = {"STT", "Mã", "Tên món / Dịch vụ", "ĐVT", "SL", "Đơn giá", "Thành tiền", "Tiến độ"};
        modelGioHang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4; // Cột SL cho phép nhấp đúp hoặc gõ phím chỉnh sửa trực tiếp
            }
        };

        modelGioHang.addTableModelListener(e -> {
            if (dangCapNhatSoLuongBang) return;
            if (e.getColumn() == 4 && e.getFirstRow() >= 0 && e.getFirstRow() < modelGioHang.getRowCount()) {
                xuLySuaSoLuongTuBanPhim(e.getFirstRow());
            }
        });

        tblGioHang = new JTable(modelGioHang);
        tblGioHang.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblGioHang.setRowHeight(32);
        tblGioHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblGioHang.setShowHorizontalLines(true);
        tblGioHang.setShowVerticalLines(false);
        tblGioHang.setGridColor(ThemeColors.BORDER_LIGHT);
        tblGioHang.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblGioHang.setSelectionForeground(ThemeColors.TEXT_PRIMARY);
        tblGioHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tblGioHang.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblGioHang.getTableHeader().setPreferredSize(new Dimension(0, 28));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        // Renderer nổi bật cho cột SL để người dùng biết có thể nhập trực tiếp từ bàn phím
        DefaultTableCellRenderer slRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(ThemeColors.PRIMARY_ACTIVE);
                lbl.setToolTipText("Nhấp đúp để gõ số lượng trực tiếp từ bàn phím");
                return lbl;
            }
        };

        tblGioHang.getColumnModel().getColumn(0).setPreferredWidth(26);
        tblGioHang.getColumnModel().getColumn(0).setCellRenderer(center);
        tblGioHang.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblGioHang.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblGioHang.getColumnModel().getColumn(3).setPreferredWidth(40);
        tblGioHang.getColumnModel().getColumn(3).setCellRenderer(center);
        tblGioHang.getColumnModel().getColumn(4).setPreferredWidth(36);
        tblGioHang.getColumnModel().getColumn(4).setCellRenderer(slRenderer);
        tblGioHang.getColumnModel().getColumn(5).setPreferredWidth(65);
        tblGioHang.getColumnModel().getColumn(5).setCellRenderer(right);
        tblGioHang.getColumnModel().getColumn(6).setPreferredWidth(72);
        tblGioHang.getColumnModel().getColumn(6).setCellRenderer(right);
        tblGioHang.getColumnModel().getColumn(7).setPreferredWidth(90);
        tblGioHang.getColumnModel().getColumn(7).setCellRenderer(new TienDoCellRenderer());

        // Ẩn cột 'Mã' (col 1) khỏi TableColumnModel để giao diện giỏ hàng thoáng mắt, không bị co chữ
        tblGioHang.removeColumn(tblGioHang.getColumnModel().getColumn(1));

        tblGioHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblGioHang.rowAtPoint(e.getPoint());
                int viewCol = tblGioHang.columnAtPoint(e.getPoint());
                if (row >= 0 && viewCol >= 0) {
                    int modelCol = tblGioHang.convertColumnIndexToModel(viewCol);
                    if (modelCol == 7) {
                        chuyenTienDoDichVuTrongGio(tblGioHang.convertRowIndexToModel(row));
                    }
                }
            }
        });

        JScrollPane scrollGioHang = new JScrollPane(tblGioHang);
        scrollGioHang.setPreferredSize(new Dimension(0, 260));
        scrollGioHang.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));

        // Thanh công cụ giỏ hàng thiết kế mới: Nút [- Giảm] / [+ Tăng] / [Xóa] sắc nét, không bao giờ bị cắt chữ "..."
        JPanel pnlCartActions = new JPanel(new BorderLayout());
        pnlCartActions.setOpaque(false);
        JLabel lblCartHint = new JLabel("Chi tiết đơn (Nhấp đúp cột SL để gõ số):");
        lblCartHint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCartHint.setForeground(ThemeColors.TEXT_SECONDARY);

        JPanel pnlCartBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlCartBtns.setOpaque(false);

        JButton btnGiam = new JButton("Giảm", IconHelper.getIcon(IconHelper.MINUS, 12, new Color(0x92, 0x40, 0x0E)));
        btnGiam.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnGiam.setBackground(new Color(0xFF, 0xFB, 0xEB));
        btnGiam.setForeground(new Color(0x92, 0x40, 0x0E));
        btnGiam.setFocusPainted(false);
        btnGiam.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiam.setIconTextGap(4);
        btnGiam.setPreferredSize(new Dimension(74, 25));
        btnGiam.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xF5, 0x9E, 0x0B), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        JButton btnTang = new JButton("Tăng", IconHelper.getIcon(IconHelper.PLUS, 12, ThemeColors.PRIMARY_ACTIVE));
        btnTang.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTang.setBackground(ThemeColors.PRIMARY_LIGHT);
        btnTang.setForeground(ThemeColors.PRIMARY_ACTIVE);
        btnTang.setFocusPainted(false);
        btnTang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTang.setIconTextGap(4);
        btnTang.setPreferredSize(new Dimension(74, 25));
        btnTang.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        JButton btnXoa = new JButton("Xóa", IconHelper.getIcon(IconHelper.TRASH, 12, ThemeColors.DANGER));
        btnXoa.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnXoa.setBackground(new Color(0xFF, 0xF1, 0xF2));
        btnXoa.setForeground(ThemeColors.DANGER);
        btnXoa.setFocusPainted(false);
        btnXoa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoa.setIconTextGap(4);
        btnXoa.setPreferredSize(new Dimension(66, 25));
        btnXoa.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xFB, 0x71, 0x85), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        btnTang.addActionListener(e -> thayDoiSoLuong(1));
        btnGiam.addActionListener(e -> thayDoiSoLuong(-1));
        btnXoa.addActionListener(e -> xoaMonDangChon());

        pnlCartBtns.add(btnGiam);
        pnlCartBtns.add(btnTang);
        pnlCartBtns.add(btnXoa);

        pnlCartActions.add(lblCartHint, BorderLayout.WEST);
        pnlCartActions.add(pnlCartBtns, BorderLayout.EAST);

        // Khung tổng kết thanh toán thu gọn tối đa (chỉ 4 dòng gọn gàng)
        JPanel pnlTongKet = new JPanel(new GridBagLayout());
        pnlTongKet.setOpaque(false);
        pnlTongKet.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);

        // Dòng 0: Tạm tính & Giảm giá gộp trên cùng 1 hàng nhỏ gọn
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        JPanel pnlTamTinhLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlTamTinhLeft.setOpaque(false);
        JLabel lblTamTinhTitle = new JLabel("Tạm tính:");
        lblTamTinhTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTongTienHang = new JLabel("0 đ");
        lblTongTienHang.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlTamTinhLeft.add(lblTamTinhTitle);
        pnlTamTinhLeft.add(lblTongTienHang);
        pnlTongKet.add(pnlTamTinhLeft, gbc);

        gbc.gridx = 1; gbc.weightx = 0.5;
        JPanel pnlGiamGiaRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlGiamGiaRight.setOpaque(false);
        JLabel lblGiamTitle = new JLabel("Giảm VIP/Điểm:");
        lblGiamTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblGiamGia = new JLabel("0 đ");
        lblGiamGia.setForeground(ThemeColors.DANGER);
        lblGiamGia.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlGiamGiaRight.add(lblGiamTitle);
        pnlGiamGiaRight.add(lblGiamGia);
        pnlTongKet.add(pnlGiamGiaRight, gbc);

        // Dòng 1: TỔNG THANH TOÁN
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.45;
        JLabel lblCanTraTitle = new JLabel("TỔNG THANH TOÁN:");
        lblCanTraTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCanTraTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlTongKet.add(lblCanTraTitle, gbc);
        gbc.gridx = 1; gbc.weightx = 0.55;
        lblKhachCanTra = new JLabel("0 đ", SwingConstants.RIGHT);
        lblKhachCanTra.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKhachCanTra.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnlTongKet.add(lblKhachCanTra, gbc);

        // Dòng 2: Phương thức & Tiền khách đưa nằm ngang gọn gàng
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.45;
        cboPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản"});
        cboPhuongThuc.setPreferredSize(new Dimension(130, 26));
        cboPhuongThuc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cboPhuongThuc.addActionListener(e -> {
            String sel = (String) cboPhuongThuc.getSelectedItem();
            long tienKhauTru = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
            long canTraHienTai = Math.max(0, tongTienHang - tienGiamGia - tienKhauTru);
            if ("Chuyển khoản".equals(sel)) {
                if (txtTienKhachDua != null) {
                    txtTienKhachDua.setText(canTraHienTai == 0 ? "0" : String.valueOf(canTraHienTai));
                    txtTienKhachDua.setEnabled(false);
                }
                if (lblTienThoiLai != null) {
                    lblTienThoiLai.setText("0 đ");
                    lblTienThoiLai.setForeground(ThemeColors.SUCCESS);
                }
            } else {
                if (txtTienKhachDua != null) {
                    txtTienKhachDua.setEnabled(true);
                    txtTienKhachDua.requestFocus();
                    txtTienKhachDua.selectAll();
                }
                tinhTienThoi();
            }
        });
        pnlTongKet.add(cboPhuongThuc, gbc);

        gbc.gridx = 1; gbc.weightx = 0.55;
        JPanel pnlTienDuaWrapper = new JPanel(new BorderLayout(4, 0));
        pnlTienDuaWrapper.setOpaque(false);
        JLabel lblKhachDua = new JLabel("Khách đưa:");
        lblKhachDua.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtTienKhachDua = new JTextField("0");
        txtTienKhachDua.setHorizontalAlignment(JTextField.RIGHT);
        txtTienKhachDua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTienKhachDua.setPreferredSize(new Dimension(95, 26));
        txtTienKhachDua.setBorder(ThemeColors.createInputBorder());
        JLabel lblDvtTienDua = new JLabel("VNĐ");
        lblDvtTienDua.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblDvtTienDua.setForeground(ThemeColors.TEXT_MUTED);
        pnlTienDuaWrapper.add(lblKhachDua, BorderLayout.WEST);
        pnlTienDuaWrapper.add(txtTienKhachDua, BorderLayout.CENTER);
        pnlTienDuaWrapper.add(lblDvtTienDua, BorderLayout.EAST);
        pnlTongKet.add(pnlTienDuaWrapper, gbc);

        // Dòng 3: Tiền thối lại bên trái + Gợi ý tiền nhanh bên phải trên cùng 1 hàng
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.42;
        JPanel pnlThoiLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlThoiLeft.setOpaque(false);
        JLabel lblThoiTitle = new JLabel("Thối lại:");
        lblThoiTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTienThoiLai = new JLabel("0 đ");
        lblTienThoiLai.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienThoiLai.setForeground(ThemeColors.SUCCESS);
        pnlThoiLeft.add(lblThoiTitle);
        pnlThoiLeft.add(lblTienThoiLai);
        pnlTongKet.add(pnlThoiLeft, gbc);

        gbc.gridx = 1; gbc.weightx = 0.58;
        JPanel pnlGoiY = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
        pnlGoiY.setOpaque(false);
        JButton btnTienDu = taoNutGoiYTien("Đủ tiền", -1);
        JButton btn100k = taoNutGoiYTien("100k", 100000);
        JButton btn200k = taoNutGoiYTien("200k", 200000);
        JButton btn500k = taoNutGoiYTien("500k", 500000);
        pnlGoiY.add(btnTienDu);
        pnlGoiY.add(btn100k);
        pnlGoiY.add(btn200k);
        pnlGoiY.add(btn500k);
        pnlTongKet.add(pnlGoiY, gbc);

        // DocumentListener cho txtTienKhachDua: tính tiền thối ngay khi gõ số theo thời gian thực
        txtTienKhachDua.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { tinhTienThoi(); }
            @Override
            public void removeUpdate(DocumentEvent e) { tinhTienThoi(); }
            @Override
            public void changedUpdate(DocumentEvent e) { tinhTienThoi(); }
        });
        txtTienKhachDua.addActionListener(e -> tinhTienThoi());

        // Hàng nút tác vụ dưới cùng gộp thành 1 dòng ngang 34px thay vì 2 tầng cao 96px
        JPanel pnlActionButtons = new JPanel(new BorderLayout(6, 0));
        pnlActionButtons.setOpaque(false);

        JPanel pnlSubActions = new JPanel(new GridLayout(1, 3, 4, 0));
        pnlSubActions.setOpaque(false);
        pnlSubActions.setPreferredSize(new Dimension(285, 34));

        JButton btnTaoDonMoi = ThemeColors.createSecondaryButton("Tạo mới", IconHelper.getIcon(IconHelper.PLUS, 12, ThemeColors.TEXT_SECONDARY));
        JButton btnLuuTam = ThemeColors.createSecondaryButton("Lưu tạm", IconHelper.getIcon(IconHelper.CLOCK, 12, ThemeColors.WARNING));
        JButton btnInK80 = ThemeColors.createSecondaryButton("In K80", IconHelper.getIcon(IconHelper.RECEIPT, 12, ThemeColors.PRIMARY_ACTIVE));

        btnTaoDonMoi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLuuTam.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnInK80.setFont(new Font("Segoe UI", Font.BOLD, 11));

        btnTaoDonMoi.addActionListener(e -> taoDonMoi());
        btnLuuTam.addActionListener(e -> luuTamDonHang());
        btnInK80.addActionListener(e -> xemTruocInHoaDonK80());

        pnlSubActions.add(btnTaoDonMoi);
        pnlSubActions.add(btnLuuTam);
        pnlSubActions.add(btnInK80);

        JButton btnThanhToan = new JButton("THANH TOÁN (F9)", IconHelper.getIcon(IconHelper.CREDIT_CARD, 15, Color.WHITE));
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThanhToan.setBackground(ThemeColors.PRIMARY);
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThanhToan.setPreferredSize(new Dimension(0, 36));
        btnThanhToan.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_ACTIVE, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnThanhToan.putClientProperty("JButton.buttonType", "roundRect");
        btnThanhToan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnThanhToan.setBackground(ThemeColors.PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnThanhToan.setBackground(ThemeColors.PRIMARY);
            }
        });
        btnThanhToan.addActionListener(e -> thucHienThanhToan());

        pnlActionButtons.add(pnlSubActions, BorderLayout.WEST);
        pnlActionButtons.add(btnThanhToan, BorderLayout.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 4));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlKhachHang, BorderLayout.NORTH);

        JPanel pnlTableWrapper = new JPanel(new BorderLayout(0, 3));
        pnlTableWrapper.setOpaque(false);

        // Thanh thông báo khấu trừ đổi trả hàng (Hiển thị khi tiếp nhận đổi trả từ DoiTraHangPanel)
        pnlThongBaoDoiTra = new JPanel(new BorderLayout(8, 0));
        pnlThongBaoDoiTra.setBackground(new Color(254, 243, 199));
        pnlThongBaoDoiTra.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 158, 11), 1, true),
                new EmptyBorder(5, 8, 5, 8)
        ));
        pnlThongBaoDoiTra.setVisible(false);

        lblThongBaoDoiTra = new JLabel("🔄 ĐỔI TRẢ TỪ HÓA ĐƠN | KHẤU TRỪ: -0 đ");
        lblThongBaoDoiTra.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblThongBaoDoiTra.setForeground(new Color(146, 64, 14));
        lblThongBaoDoiTra.setIcon(IconHelper.getIcon(IconHelper.REFRESH, 13, new Color(180, 83, 9)));

        btnHuyDoiTra = new JButton("Hủy đổi trả", IconHelper.getIcon(IconHelper.TRASH, 11, ThemeColors.DANGER));
        btnHuyDoiTra.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnHuyDoiTra.setBackground(new Color(254, 226, 226));
        btnHuyDoiTra.setForeground(ThemeColors.DANGER);
        btnHuyDoiTra.setFocusPainted(false);
        btnHuyDoiTra.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuyDoiTra.setBorder(new CompoundBorder(
                new LineBorder(new Color(239, 68, 68), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        btnHuyDoiTra.addActionListener(e -> huyDoiTraHang());

        pnlThongBaoDoiTra.add(lblThongBaoDoiTra, BorderLayout.CENTER);
        pnlThongBaoDoiTra.add(btnHuyDoiTra, BorderLayout.EAST);

        JPanel pnlCartTop = new JPanel(new BorderLayout(0, 3));
        pnlCartTop.setOpaque(false);
        pnlCartTop.add(pnlThongBaoDoiTra, BorderLayout.NORTH);
        pnlCartTop.add(pnlCartActions, BorderLayout.SOUTH);

        pnlTableWrapper.add(pnlCartTop, BorderLayout.NORTH);
        pnlTableWrapper.add(scrollGioHang, BorderLayout.CENTER);

        // Mô hình 3: Gợi ý bán kèm thông minh (Smart Cross-Sell banner)
        pnlCrossSell = new JPanel(new BorderLayout(6, 0));
        pnlCrossSell.setBackground(new Color(254, 243, 199));
        pnlCrossSell.setBorder(new CompoundBorder(
                new LineBorder(new Color(245, 158, 11), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        pnlCrossSell.setVisible(false);

        lblCrossSellText = new JLabel("Gợi ý: Mua kèm xịt ve rận Bio-Care giảm 10%");
        lblCrossSellText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblCrossSellText.setForeground(new Color(146, 64, 14));
        lblCrossSellText.setIcon(IconHelper.getIcon(IconHelper.TAG, 13, new Color(180, 83, 9)));

        btnThemCrossSell = new JButton("+ Thêm (-10%)");
        btnThemCrossSell.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnThemCrossSell.setBackground(new Color(245, 158, 11));
        btnThemCrossSell.setForeground(Color.WHITE);
        btnThemCrossSell.setFocusPainted(false);
        btnThemCrossSell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemCrossSell.putClientProperty("JButton.buttonType", "roundRect");
        btnThemCrossSell.addActionListener(e -> themSanPhamCrossSellHienTai());

        pnlCrossSell.add(lblCrossSellText, BorderLayout.CENTER);
        pnlCrossSell.add(btnThemCrossSell, BorderLayout.EAST);
        pnlTableWrapper.add(pnlCrossSell, BorderLayout.SOUTH);

        pnlCenter.add(pnlTableWrapper, BorderLayout.CENTER);

        pnlCenter.add(pnlTongKet, BorderLayout.SOUTH);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlCenter, BorderLayout.CENTER);
        pnl.add(pnlActionButtons, BorderLayout.SOUTH);

        return pnl;
    }

    private JButton taoNutGoiYTien(String label, long amount) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setBackground(Color.WHITE);
        btn.setForeground(ThemeColors.TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(1, 5, 1, 5)
        ));
        btn.addActionListener(e -> {
            if (cboPhuongThuc != null && !"Tiền mặt".equals(cboPhuongThuc.getSelectedItem())) {
                cboPhuongThuc.setSelectedItem("Tiền mặt");
            }
            if (txtTienKhachDua != null) {
                txtTienKhachDua.setEnabled(true);
                if (amount < 0) {
                    long tienKhauTru = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
                    long canTra = Math.max(0, tongTienHang - tienGiamGia - tienKhauTru);
                    txtTienKhachDua.setText(String.valueOf(canTra));
                } else {
                    txtTienKhachDua.setText(String.valueOf(amount));
                }
            }
            tinhTienThoi();
        });
        return btn;
    }

//    private void xuLySuaSoLuongTuBanPhim(int row) {
//        dangCapNhatSoLuongBang = true;
//        try {
//            Object rawVal = modelGioHang.getValueAt(row, 4);
//            String valStr = rawVal != null ? rawVal.toString().replaceAll("[^0-9-]", "").trim() : "1";
    private void xuLySuaSoLuongTuBanPhim(int row) {
        dangCapNhatSoLuongBang = true;
        try {
            Object rawVal = modelGioHang.getValueAt(row, 4);
            String valStr = rawVal != null ? rawVal.toString().replaceAll("[^0-9-]", "").trim() : "1";
            int slMoi;
            try {
                slMoi = Integer.parseInt(valStr);
            } catch (NumberFormatException ex) {
                slMoi = 1;
            }
            if (slMoi <= 0) {
                slMoi = 1;
            }

            // Kiểm tra tồn kho nghiêm ngặt nếu là sản phẩm hàng hóa
            String maMon = (String) modelGioHang.getValueAt(row, 1);
            String dvtMon = modelGioHang.getValueAt(row, 3) != null ? modelGioHang.getValueAt(row, 3).toString() : "";
            if (maMon != null && maMon.startsWith("SP")) {
                for (CatalogItem item : allItems) {
                    if (maMon.equalsIgnoreCase(item.ma) && (dvtMon.isEmpty() || dvtMon.equalsIgnoreCase(item.dvt))) {
                        if (slMoi > item.tonKho) {
                            JOptionPane.showMessageDialog(this,
                                    "Số lượng nhập (" + slMoi + ") vượt quá tồn kho hiện tại của sản phẩm [" + item.ten + "] (" + item.tonKho + " " + item.dvt + ")!\n" +
                                    "Hệ thống đã tự động điều chỉnh về mức tồn kho tối đa cho phép.",
                                    "Cảnh báo tồn kho", JOptionPane.WARNING_MESSAGE);
                            slMoi = item.tonKho; // Giới hạn tối đa bằng số lượng tồn kho
                        }
                        break;
                    }
                }
            }

            modelGioHang.setValueAt(slMoi, row, 4);
            String donGiaStr = ((String) modelGioHang.getValueAt(row, 5)).replaceAll("[^0-9]", "").trim();
            long donGia = donGiaStr.isEmpty() ? 0 : Long.parseLong(donGiaStr);
            modelGioHang.setValueAt(df.format(slMoi * donGia), row, 6);
            capNhatTongTien();
        } finally {
            dangCapNhatSoLuongBang = false;
        }
    }

    private void hienThiPopupGoiYKhachHang(Component invoker) {
        String kw = txtSdtKhachHang != null ? txtSdtKhachHang.getText().trim() : "";
        List<KhachHang> dsGoiY = BanHangBus.timKiemGoiYKhachHang(kw);
        if (dsGoiY.isEmpty()) {
            dsGoiY = BanHangBus.getDanhSachKhachHang();
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(ThemeColors.BORDER_ACCENT, 1, true));

        JMenuItem headerItem = new JMenuItem("DANH SÁCH KHÁCH HÀNG THÀNH VIÊN (" + dsGoiY.size() + " KH)");
        headerItem.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerItem.setEnabled(false);
        popup.add(headerItem);
        popup.addSeparator();

        for (KhachHang kh : dsGoiY) {
            String hang = BanHangBus.xepHangThanhVien(kh.getDiemTichLuy());
            String label = String.format("[%s] %s · %s (%d điểm - %s)",
                    kh.getMaKhachHang(), kh.getHoTen(), kh.getSoDienThoai(), kh.getDiemTichLuy(), hang);
            JMenuItem item = new JMenuItem(label, IconHelper.getIcon(IconHelper.USER, 13, ThemeColors.PRIMARY_ACTIVE));
            item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            item.addActionListener(e -> {
                txtSdtKhachHang.setText(kh.getSoDienThoai());
                traCuuKhachHang();
            });
            popup.add(item);
        }

        popup.addSeparator();
        JMenuItem itemKhachLe = new JMenuItem("Đặt lại: Khách vãng lai (Xóa thông tin đang chọn)",
                IconHelper.getIcon(IconHelper.REFRESH, 13, ThemeColors.TEXT_SECONDARY));
        itemKhachLe.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        itemKhachLe.addActionListener(e -> {
            dangCapNhatKhach = true;
            try {
                txtSdtKhachHang.setText("");
                txtTenKhachHang.setText("");
                tenKhachTuDongDien = false;
                diemHienCo = 0;
                lblDiemTichLuy.setText("Khách vãng lai · 0 điểm");
                lblDiemTichLuy.setForeground(ThemeColors.TEXT_SECONDARY);
                capNhatDiemSpinner();
            } finally {
                dangCapNhatKhach = false;
            }
            capNhatTongTien();
        });
        popup.add(itemKhachLe);

        popup.show(invoker, 0, invoker.getHeight() + 2);
    }

    private void moHopThoaiQuanLyKhachNhanh() {
        String sdtDangNhap = txtSdtKhachHang.getText().trim();
        String tenDangNhap = txtTenKhachHang.getText().trim();
        KhachHang khHienTai = controller.timKhachHang(sdtDangNhap);

        boolean isManager = utils.UserSession.isQuanLy();
        JTextField txtSdt = new JTextField(khHienTai != null ? khHienTai.getSoDienThoai() : sdtDangNhap);
        JTextField txtHoTen = new JTextField(khHienTai != null ? khHienTai.getHoTen() : tenDangNhap);
        JTextField txtDiem = new JTextField(khHienTai != null ? String.valueOf(khHienTai.getDiemTichLuy()) : "0");
        txtDiem.setEditable(isManager);
        if (!isManager) {
            txtDiem.setToolTipText("Chỉ Quản lý mới có quyền điều chỉnh điểm thủ công");
        }

        String trangThaiKH = khHienTai != null
                ? ("KHÁCH CŨ [" + khHienTai.getMaKhachHang() + "] - Hạng: " + BanHangBus.xepHangThanhVien(khHienTai.getDiemTichLuy()))
                : "KHÁCH HÀNG MỚI (Tự động cấp mã thành viên mới)";

        Object[] options = khHienTai != null
                ? new Object[]{"Lưu cập nhật", "Xóa khách hàng", "Đóng"}
                : new Object[]{"+ Thêm Khách hàng mới", "Đóng"};

        Object[] msg = {
                "Phân loại: " + trangThaiKH,
                "Số điện thoại (10 chữ số) (*):", txtSdt,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Điểm tích lũy hiện có" + (isManager ? ":" : " (Chỉ xem - Quyền Quản lý mới được sửa):"), txtDiem
        };

        int choice = JOptionPane.showOptionDialog(this, msg,
                "QUẢN LÝ THÔNG TIN KHÁCH HÀNG TẠI QUẦY POS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            String sdtMoi = txtSdt.getText().trim();
            String tenMoi = txtHoTen.getText().trim();
            if (sdtMoi.isEmpty() || tenMoi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Số điện thoại và Họ tên khách hàng!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!sdtMoi.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 chữ số bắt đầu bằng số 0.", "Lỗi định dạng SĐT", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int diemMoi = khHienTai != null ? khHienTai.getDiemTichLuy() : 0;
            if (isManager) {
                try {
                    diemMoi = Math.max(0, Integer.parseInt(txtDiem.getText().trim()));
                } catch (NumberFormatException ignored) {}
            }
            boolean ok = false;
            if (khHienTai != null) {
                ok = BanHangBus.capNhatThongTinKhachHang(khHienTai.getMaKhachHang(), tenMoi, sdtMoi, diemMoi);
            } else {
                ok = (BanHangBus.luuHoacCapNhatKhachHang(sdtMoi, tenMoi, diemMoi) != null);
            }
            if (ok) {
                txtSdtKhachHang.setText(sdtMoi);
                traCuuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ khách hàng [" + tenMoi + " - " + sdtMoi + "] thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể lưu hồ sơ khách hàng xuống CSDL. Vui lòng kiểm tra lại thông tin hoặc kết nối CSDL!", "Lỗi lưu dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        } else if (khHienTai != null && choice == 1) {
            int cf = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa khách hàng [" + khHienTai.getHoTen() + " - " + khHienTai.getSoDienThoai() + "]?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (cf == JOptionPane.YES_OPTION) {
                boolean xoaOk = BanHangBus.xoaKhachHang(khHienTai.getMaKhachHang());
                if (xoaOk) {
                    txtSdtKhachHang.setText("");
                    txtTenKhachHang.setText("");
                    traCuuKhachHang();
                    JOptionPane.showMessageDialog(this, "Đã xóa thông tin khách hàng khỏi hệ thống.", "Đã xóa", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa khách hàng này do đã phát sinh hóa đơn/giao dịch trong hệ thống (Ràng buộc toàn vẹn)!", "Không thể xóa", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void traCuuKhachHang() {
        if (dangCapNhatKhach) return;
        dangCapNhatKhach = true;
        try {
            String sdt = txtSdtKhachHang != null ? txtSdtKhachHang.getText().trim() : "";
            if (sdt.isEmpty()) {
                if (tenKhachTuDongDien && txtTenKhachHang != null) {
                    txtTenKhachHang.setText("");
                    tenKhachTuDongDien = false;
                }
                diemHienCo = 0;
                if (lblDiemTichLuy != null) {
                    lblDiemTichLuy.setText("Khách vãng lai · 0 điểm");
                    lblDiemTichLuy.setForeground(ThemeColors.TEXT_SECONDARY);
                }
                theSpaPassHienTai = null;
                if (lblSpaPassStatus != null) {
                    lblSpaPassStatus.setText("Chưa có thẻ Spa Pass");
                    lblSpaPassStatus.setForeground(ThemeColors.TEXT_MUTED);
                }
                if (btnDungSpaPass != null) {
                    btnDungSpaPass.setEnabled(false);
                }
                if (lblThuCungDangChon != null) {
                    lblThuCungDangChon.setText("Chưa chọn thú cưng (Khách vãng lai)");
                    lblThuCungDangChon.setForeground(ThemeColors.TEXT_MUTED);
                }
            } else {
                KhachHang kh = controller.timKhachHang(sdt);
                if (kh != null) {
                    if (txtTenKhachHang != null) {
                        txtTenKhachHang.setText(kh.getHoTen());
                    }
                    tenKhachTuDongDien = true;
                    diemHienCo = kh.getDiemTichLuy();
                    String hang = BanHangBus.xepHangThanhVien(diemHienCo);
                    if (lblDiemTichLuy != null) {
                        lblDiemTichLuy.setText("Khách cũ [" + kh.getMaKhachHang() + "]: " + diemHienCo + " điểm (" + hang + ")");
                        lblDiemTichLuy.setForeground(diemHienCo >= 100 ? ThemeColors.PRIMARY_ACTIVE : ThemeColors.SUCCESS_TEXT);
                    }
                } else {
                    if (tenKhachTuDongDien && txtTenKhachHang != null) {
                        txtTenKhachHang.setText("");
                        tenKhachTuDongDien = false;
                    }
                    diemHienCo = 0;
                    if (lblDiemTichLuy != null) {
                        lblDiemTichLuy.setText("Khách mới (chưa có thẻ): Sẽ tự động lưu khi TT");
                        lblDiemTichLuy.setForeground(ThemeColors.WARNING_TEXT);
                    }
                }

                // Cập nhật Thẻ Spa Pass (Mô hình 1: Punch Card)
                theSpaPassHienTai = SpaPassBus.timTheKhaDung(sdt);
                if (theSpaPassHienTai != null) {
                    if (lblSpaPassStatus != null) {
                        lblSpaPassStatus.setText(theSpaPassHienTai.getTenThe() + ": Còn " + theSpaPassHienTai.getSoBuoiConLai() + "/" + theSpaPassHienTai.getTongSoBuoi() + " buổi");
                        lblSpaPassStatus.setForeground(ThemeColors.PRIMARY_ACTIVE);
                    }
                    if (btnDungSpaPass != null) {
                        btnDungSpaPass.setEnabled(theSpaPassHienTai.isKhaDung());
                    }
                } else {
                    if (lblSpaPassStatus != null) {
                        lblSpaPassStatus.setText("Chưa đăng ký Spa Pass");
                        lblSpaPassStatus.setForeground(ThemeColors.TEXT_MUTED);
                    }
                    if (btnDungSpaPass != null) {
                        btnDungSpaPass.setEnabled(false);
                    }
                }

                // Cập nhật Thú cưng & Dị ứng (Mô hình 3: Allergy Guard - truy vấn động từ CSDL)
                if (lblThuCungDangChon != null) {
                    dao.ThuCungDAO tcDao = new dao.ThuCungDAO();
                    String thongTinDiUng = tcDao.getThongTinDiUngTheoSoDienThoai(sdt);
                    if (thongTinDiUng != null) {
                        lblThuCungDangChon.setText(thongTinDiUng);
                        String upper = thongTinDiUng.toUpperCase();
                        if (upper.contains("DỊ ỨNG") || upper.contains("DI UNG")) {
                            lblThuCungDangChon.setForeground(ThemeColors.DANGER);
                        } else {
                            lblThuCungDangChon.setForeground(ThemeColors.SUCCESS_TEXT);
                        }
                    } else if (kh != null) {
                        lblThuCungDangChon.setText("Bé cưng của " + kh.getHoTen() + " (Sức khỏe bình thường)");
                        lblThuCungDangChon.setForeground(ThemeColors.SUCCESS_TEXT);
                    } else {
                        lblThuCungDangChon.setText("Khách mới (Chưa có hồ sơ thú cưng)");
                        lblThuCungDangChon.setForeground(ThemeColors.TEXT_MUTED);
                    }
                }
            }

            capNhatDiemSpinner();
            capNhatTongTien();
        } finally {
            dangCapNhatKhach = false;
        }
    }

    private void thayDoiSoLuong(int delta) {
        int row = tblGioHang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng sản phẩm cần điều chỉnh số lượng!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        dangCapNhatSoLuongBang = true;
        try {
            int slHienTai = Integer.parseInt(modelGioHang.getValueAt(row, 4).toString().replaceAll("[^0-9]", ""));
            int slMoi = slHienTai + delta;
            if (slMoi <= 0) {
                modelGioHang.removeRow(row);
                danhLaiSTT();
            } else {
                modelGioHang.setValueAt(slMoi, row, 4);
                String donGiaStr = ((String) modelGioHang.getValueAt(row, 5)).replaceAll("[^0-9]", "").trim();
                long donGia = Long.parseLong(donGiaStr);
                modelGioHang.setValueAt(df.format(slMoi * donGia), row, 6);
            }
            capNhatTongTien();
        } finally {
            dangCapNhatSoLuongBang = false;
        }
    }

    private void xoaMonDangChon() {
        int row = tblGioHang.getSelectedRow();
        if (row >= 0) {
            modelGioHang.removeRow(row);
            danhLaiSTT();
            capNhatTongTien();
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn món cần xóa khỏi giỏ hàng!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void danhLaiSTT() {
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            modelGioHang.setValueAt(i + 1, i, 0);
        }
    }

    private void capNhatTongTien() {
        if (modelGioHang == null) return;
        tongTienHang = 0;
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String thanhTienStr = ((String) modelGioHang.getValueAt(i, 6)).replaceAll("[^0-9]", "").trim();
            if (!thanhTienStr.isEmpty()) {
                tongTienHang += Long.parseLong(thanhTienStr);
            }
        }

        boolean isVIP = diemHienCo >= 100 || (lblDiemTichLuy != null && lblDiemTichLuy.getText().contains("VIP"));
        tienGiamVip = bus.BanHangBus.tinhGiamGiaVIP(tongTienHang, isVIP);
        long tongSauVip = Math.max(0, tongTienHang - tienGiamVip);
        int soDiemChon = (spnDiemDung != null && diemHienCo > 0) ? ((Number) spnDiemDung.getValue()).intValue() : 0;
        tienDoiDiem = Math.min(tongSauVip, soDiemChon * 1000L);
        tienGiamGia = tienGiamVip + tienDoiDiem;

        long tienKhauTruDoiTra = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
        bus.TaiChinhHoaDonBus.KetQuaThanhToan ketQuaTaiChinh = bus.TaiChinhHoaDonBus.tinhThanhToan(
                java.math.BigDecimal.valueOf(tongTienHang),
                java.math.BigDecimal.valueOf(tienGiamVip),
                java.math.BigDecimal.valueOf(tienDoiDiem),
                java.math.BigDecimal.valueOf(tienKhauTruDoiTra));
        long tongGiamVaKhauTru = tienGiamGia + tienKhauTruDoiTra;
        long canTra = ketQuaTaiChinh.getTienPhaiThanhToan().longValueExact();

        if (lblTongTienHang != null) lblTongTienHang.setText(df.format(tongTienHang));
        if (lblGiamGia != null) {
            if (tongGiamVaKhauTru > 0) {
                if (tienKhauTruDoiTra > 0) {
                    lblGiamGia.setText("-" + df.format(tongGiamVaKhauTru) + " (Đổi: -" + df.format(tienKhauTruDoiTra) + ")");
                } else {
                    lblGiamGia.setText("-" + df.format(tongGiamVaKhauTru));
                }
            } else {
                lblGiamGia.setText("0 đ");
            }
        }
        if (lblKhachCanTra != null) lblKhachCanTra.setText(df.format(canTra));
        if (txtTienKhachDua != null) {
            String pttt = cboPhuongThuc != null ? (String) cboPhuongThuc.getSelectedItem() : "Tiền mặt";
            if ("Chuyển khoản".equals(pttt)) {
                txtTienKhachDua.setText(canTra == 0 ? "0" : String.valueOf(canTra));
            } else {
                String raw = txtTienKhachDua.getText().replaceAll("[^0-9]", "").trim();
                if (raw.isEmpty() || canTra == 0) {
                    txtTienKhachDua.setText("0");
                }
            }
        }
        tinhTienThoi();
        capNhatThanhCrossSell();
    }

    private void tinhTienThoi() {
        if (txtTienKhachDua == null || lblTienThoiLai == null) return;
        try {
            long tienKhauTruDoiTra = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
            bus.TaiChinhHoaDonBus.KetQuaThanhToan ketQuaTaiChinh = bus.TaiChinhHoaDonBus.tinhThanhToan(
                    java.math.BigDecimal.valueOf(tongTienHang),
                    java.math.BigDecimal.valueOf(tienGiamVip),
                    java.math.BigDecimal.valueOf(tienDoiDiem),
                    java.math.BigDecimal.valueOf(tienKhauTruDoiTra));
            long canTra = ketQuaTaiChinh.getTienPhaiThanhToan().longValueExact();
            long tienHoanThua = ketQuaTaiChinh.getTienHoanDoiTra().longValueExact();

            String text = txtTienKhachDua.getText().replaceAll("[^0-9]", "").trim();
            long tienDua = text.isEmpty() ? 0 : Long.parseLong(text);

            if (tienHoanThua > 0) {
                lblTienThoiLai.setText("Hoàn thừa: " + df.format(tienHoanThua));
                lblTienThoiLai.setForeground(ThemeColors.SUCCESS);
            } else {
                long tienThoi = tienDua - canTra;
                if (canTra == 0 && tienDua == 0) {
                    lblTienThoiLai.setText("0 đ");
                    lblTienThoiLai.setForeground(ThemeColors.SUCCESS);
                } else if (tienThoi >= 0) {
                    lblTienThoiLai.setText(df.format(tienThoi));
                    lblTienThoiLai.setForeground(ThemeColors.SUCCESS);
                } else {
                    lblTienThoiLai.setText("Chưa đủ (thiếu " + df.format(Math.abs(tienThoi)) + ")");
                    lblTienThoiLai.setForeground(ThemeColors.DANGER);
                }
            }
        } catch (NumberFormatException e) {
            lblTienThoiLai.setText("Số tiền không hợp lệ");
            lblTienThoiLai.setForeground(ThemeColors.DANGER);
        }
    }

    private void lamSachKhungHoaDon() {
        thongTinDoiTraHienTai = null;
        mapHotelSnapshots.clear();
        if (pnlThongBaoDoiTra != null) {
            pnlThongBaoDoiTra.setVisible(false);
        }
        modelGioHang.setRowCount(0);
        lblMaHoaDon.setText(utils.MaTuSinhHelper.sinhMaHoaDon(utils.UserSession.getUserId()));
        dangCapNhatKhach = true;
        try {
            txtSdtKhachHang.setText("");
            txtTenKhachHang.setText("");
            tenKhachTuDongDien = false;
            diemHienCo = 0;
            tienDoiDiem = 0;
            capNhatDiemSpinner();
            lblDiemTichLuy.setText("Khách vãng lai · 0 điểm");
            lblDiemTichLuy.setForeground(ThemeColors.TEXT_SECONDARY);
            theSpaPassHienTai = null;
            if (lblSpaPassStatus != null) {
                lblSpaPassStatus.setText("Chưa có thẻ Spa Pass");
                lblSpaPassStatus.setForeground(ThemeColors.TEXT_MUTED);
            }
            if (btnDungSpaPass != null) {
                btnDungSpaPass.setEnabled(false);
            }
            if (lblThuCungDangChon != null) {
                lblThuCungDangChon.setText("Chưa chọn thú cưng (Khách vãng lai)");
                lblThuCungDangChon.setForeground(ThemeColors.TEXT_MUTED);
            }
            if (pnlCrossSell != null) {
                pnlCrossSell.setVisible(false);
            }
            crossSellItemHienTai = null;
            if (txtTienKhachDua != null) {
                txtTienKhachDua.setText("0");
                txtTienKhachDua.setEnabled(true);
            }
            if (cboPhuongThuc != null) {
                cboPhuongThuc.setSelectedItem("Tiền mặt");
            }
        } finally {
            dangCapNhatKhach = false;
        }
        capNhatTongTien();
    }

    private void xemTruocInHoaDonK80() {
        if (modelGioHang == null || modelGioHang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng thêm sản phẩm/dịch vụ để xem trước hóa đơn.", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        HoaDonDaThanhToan hd = new HoaDonDaThanhToan();
        hd.maHoaDon = (lblMaHoaDon != null ? lblMaHoaDon.getText() : utils.MaTuSinhHelper.sinhMaHoaDon(utils.UserSession.getUserId())) + " (Tạm)";
        hd.thoiGian = LocalDateTime.now().format(STATIC_DTF);
        hd.tenKhach = (txtTenKhachHang != null && !txtTenKhachHang.getText().trim().isEmpty()) ? txtTenKhachHang.getText().trim() : "Khách vãng lai";
        hd.sdt = (txtSdtKhachHang != null) ? txtSdtKhachHang.getText().trim() : "";
        hd.thuNgan = UserSession.getFullName();
        long tienKhauTru = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
        hd.tongTienHang = tongTienHang;
        hd.giamGia = tienGiamVip;
        hd.tienDoiDiem = tienDoiDiem;
        hd.tienBuTruDoiTra = tienKhauTru;
        hd.tongThanhToan = bus.TaiChinhHoaDonBus.tinhThanhToan(
                java.math.BigDecimal.valueOf(tongTienHang),
                java.math.BigDecimal.valueOf(tienGiamVip),
                java.math.BigDecimal.valueOf(tienDoiDiem),
                java.math.BigDecimal.valueOf(tienKhauTru))
                .getTienPhaiThanhToan().longValueExact();
        hd.phuongThuc = cboPhuongThuc != null ? (String) cboPhuongThuc.getSelectedItem() : "Tiền mặt";
        hd.trangThai = "Phiếu tạm tính";
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            hd.dsMon.add(new Object[]{
                    modelGioHang.getValueAt(i, 0),
                    modelGioHang.getValueAt(i, 1),
                    modelGioHang.getValueAt(i, 2),
                    modelGioHang.getValueAt(i, 3),
                    modelGioHang.getValueAt(i, 4),
                    modelGioHang.getValueAt(i, 5),
                    modelGioHang.getValueAt(i, 6)
            });
        }
        long canTraVal = hd.tongThanhToan;
        HoaDonPreviewDialog.hienThiHoaDon(this, hd, canTraVal, 0, (int) (canTraVal / 100000), diemHienCo);
    }

    private void taoDonMoi() {
        if (modelGioHang.getRowCount() > 0) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn xóa đơn hiện tại để tạo hóa đơn mới?",
                    "Tạo đơn mới",
                    JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }
        lamSachKhungHoaDon();
    }

    private void luuTamDonHang() {
        if (modelGioHang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Hóa đơn đang trống, không thể lưu tạm!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tenNhap = txtTenKhachHang.getText().trim();
        DonHangTam don = new DonHangTam();
        don.maHoaDon = lblMaHoaDon.getText();
        don.tenKhach = tenNhap.isEmpty() ? "Khách vãng lai" : tenNhap;
        don.sdt = txtSdtKhachHang.getText().trim();
        don.diemTichLuy = lblDiemTichLuy.getText();
        don.thoiGianTao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        don.tongTien = tongTienHang;
        don.giamGia = tienGiamVip;
        don.tienDoiDiem = tienDoiDiem;
        don.tienBuTruDoiTra = thongTinDoiTraHienTai != null
                ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
        don.khachCanTra = bus.TaiChinhHoaDonBus.tinhThanhToan(
                java.math.BigDecimal.valueOf(tongTienHang),
                java.math.BigDecimal.valueOf(don.giamGia),
                java.math.BigDecimal.valueOf(don.tienDoiDiem),
                java.math.BigDecimal.valueOf(don.tienBuTruDoiTra))
                .getTienPhaiThanhToan().longValueExact();
        don.phuongThuc = (String) cboPhuongThuc.getSelectedItem();

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            Object[] r = new Object[]{
                    modelGioHang.getValueAt(i, 0),
                    modelGioHang.getValueAt(i, 1),
                    modelGioHang.getValueAt(i, 2),
                    modelGioHang.getValueAt(i, 3),
                    modelGioHang.getValueAt(i, 4),
                    modelGioHang.getValueAt(i, 5),
                    modelGioHang.getValueAt(i, 6),
                    modelGioHang.getValueAt(i, 7)
            };
            don.dsMon.add(r);

            // Lưu kèm snapshot Pet Hotel nếu có dòng dịch vụ lưu trú (TC12)
            Object valMa = modelGioHang.getValueAt(i, 1);
            if (valMa != null) {
                String ma = valMa.toString();
                if (ma.startsWith("DV-HOTEL-")) {
                    String mp = ma.substring("DV-HOTEL-".length()).trim().toUpperCase();
                    entity.HotelCheckoutSnapshot snap = mapHotelSnapshots.get(mp);
                    if (snap != null) {
                        don.dsSnapshotsHotel.add(snap);
                    }
                }
            }
        }

        don.thongTinDoiTra = this.thongTinDoiTraHienTai;
        if (theSpaPassHienTai != null) {
            don.maTheSpaPass = theSpaPassHienTai.getMaThe();
        }

        dsDonTam.add(0, don);
        boolean diskOk = luuDsDonTamXuongDisk();
        capNhatGiaoDienDonCho();

        if (diskOk) {
            JOptionPane.showMessageDialog(this,
                    "ĐÃ LƯU TẠM ĐƠN HÀNG [" + don.maHoaDon + "] THÀNH CÔNG!\n" +
                            "• Khách: " + don.tenKhach + "\n" +
                            "• Món: " + don.dsMon.size() + " món · " + df.format(don.khachCanTra) + "\n\n" +
                            "Đơn đã được chuyển vào Tab 'Hóa đơn chờ phục vụ' để bạn phục vụ khách tiếp theo.",
                    "Lưu tạm thành công", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Đơn hàng [" + don.maHoaDon + "] đã được lưu tạm trên RAM nhưng không thể ghi xuống file config/pos_don_tam.dat!\n" +
                            "Vui lòng lưu ý không tắt ứng dụng trước khi xử lý xong đơn này.",
                    "Cảnh báo lưu tạm", JOptionPane.WARNING_MESSAGE);
        }

        lamSachKhungHoaDon();
    }

    public void tiepNhanDichVuPetHotel(entity.HotelCheckoutSnapshot snapshot) {
        if (snapshot == null) return;
        mapHotelSnapshots.put(snapshot.getMaPhong().trim().toUpperCase(), snapshot);
        tiepNhanDichVuPetHotel(snapshot.getMaPhong(), snapshot.getLoaiPhong(),
                snapshot.getTenThuCung(), snapshot.getTenChu(), snapshot.getSdtChu(),
                snapshot.getSoNgayLuuTru(), snapshot.getDonGiaNgay(), snapshot.getTongTien());
    }

    @Deprecated
    public void tiepNhanDichVuPetHotel(String maPhong, String loaiPhong, String tenBe, String tenChu, String sdt, int soNgay, long donGiaNgay, long tongTien) {
        if (maPhong != null && !mapHotelSnapshots.containsKey(maPhong.trim().toUpperCase())) {
            gui.PetHotelPanel.PhongLuuTru phongReal = null;
            try {
                for (gui.PetHotelPanel.PhongLuuTru p : new bus.PetHotelBus().layDanhSachPhong()) {
                    if (p != null && p.maPhong != null && p.maPhong.equalsIgnoreCase(maPhong.trim())) {
                        phongReal = p;
                        break;
                    }
                }
            } catch (Exception ignored) {}

            if (phongReal != null && phongReal.thoiGianCheckIn != null) {
                mapHotelSnapshots.put(maPhong.trim().toUpperCase(), new entity.HotelCheckoutSnapshot(
                        maPhong, loaiPhong != null ? loaiPhong : phongReal.loaiPhong,
                        tenBe != null ? tenBe : phongReal.tenThuCung,
                        tenChu != null ? tenChu : phongReal.tenChu,
                        sdt != null ? sdt : phongReal.sdtChu,
                        phongReal.thoiGianCheckIn, LocalDateTime.now(),
                        soNgay, donGiaNgay > 0 ? donGiaNgay : phongReal.donGiaNgay,
                        phongReal.phiDichVuPhatSinh, tongTien, phongReal.ghiChuChamSoc
                ));
            } else {
                throw new IllegalStateException("Không tìm thấy thông tin lưu trú thực tế của phòng [" + maPhong + "] trong cơ sở dữ liệu! Thanh toán Pet Hotel bắt buộc phải xuất phát từ Check-out thực tế trên hệ thống (TC13).");
            }
        }
        chonTabPOS();
        dangCapNhatKhach = true;
        try {
            if (sdt != null && !sdt.trim().isEmpty()) {
                txtSdtKhachHang.setText(sdt.trim());
            }
            if (tenChu != null && !tenChu.trim().isEmpty()) {
                txtTenKhachHang.setText(tenChu.trim());
            }
            tenKhachTuDongDien = false;
        } finally {
            dangCapNhatKhach = false;
        }
        traCuuKhachHang();

        int stt = modelGioHang.getRowCount() + 1;
        String tenMon = "Lưu trú Hotel [" + maPhong + "] - " + loaiPhong + " (" + tenBe + ")";
        modelGioHang.addRow(new Object[]{
                stt,
                "DV-HOTEL-" + maPhong,
                tenMon,
                soNgay + " ngày",
                1,
                df.format(tongTien),
                df.format(tongTien),
                CAC_BUOC_TIEN_DO[3]
        });
        capNhatTongTien();

        JOptionPane.showMessageDialog(this,
                "ĐÃ TIẾP NHẬN DỊCH VỤ PET HOTEL VÀO GIỎ HÀNG POS!\n" +
                        "• Phòng: " + maPhong + " (" + loaiPhong + ")\n" +
                        "• Bé cưng: " + tenBe + " - Chủ: " + tenChu + "\n" +
                        "• Thời gian lưu trú: " + soNgay + " ngày\n" +
                        "• Tổng tiền: " + df.format(tongTien) + "\n\n" +
                        "Vui lòng kiểm tra giảm giá VIP / đổi điểm và bấm 'THANH TOÁN NGAY'.",
                "Tiếp nhận Pet Hotel thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void tiepNhanDichVuLichHen(String maLH, String khTen, String sdt, String thuCung, String tenDichVu, String ktv) {
        chonTabPOS();
        dangCapNhatKhach = true;
        try {
            if (sdt != null && !sdt.trim().isEmpty()) {
                txtSdtKhachHang.setText(sdt.trim());
            }
            if (khTen != null && !khTen.trim().isEmpty()) {
                txtTenKhachHang.setText(khTen.trim());
            }
            tenKhachTuDongDien = false;
        } finally {
            dangCapNhatKhach = false;
        }
        traCuuKhachHang();

        String maDV = null;
        long giaDV = 0L;
        String dvt = "45 phút";
        for (CatalogItem it : allItems) {
            if (it.loai.toLowerCase().contains("dịch vụ") || it.loai.toLowerCase().contains("spa") || it.loai.toLowerCase().contains("grooming")) {
                if (it.ten.equalsIgnoreCase(tenDichVu) || tenDichVu.contains(it.ten) || it.ten.contains(tenDichVu) || (it.ma != null && it.ma.equalsIgnoreCase(tenDichVu))) {
                    maDV = it.ma;
                    giaDV = it.donGia;
                    dvt = it.dvt;
                    break;
                }
            }
        }
        if (maDV == null) {
            try {
                dao.DichVuDAO dvDao = new dao.DichVuDAO();
                for (entity.DichVu dv : dvDao.getAllDichVu()) {
                    if (dv.getTenDichVu().equalsIgnoreCase(tenDichVu) || dv.getMaDichVu().equalsIgnoreCase(tenDichVu)
                            || tenDichVu.contains(dv.getTenDichVu()) || dv.getTenDichVu().contains(tenDichVu)) {
                        maDV = dv.getMaDichVu();
                        giaDV = dv.getGiaDichVu() != null ? dv.getGiaDichVu().longValue() : 0L;
                        dvt = dv.getThoiLuongPhut() > 0 ? (dv.getThoiLuongPhut() + " phút") : "45 phút";
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (maDV == null) {
            maDV = "DV001";
            giaDV = 300000L;
            dvt = "90 phút";
        }

        int stt = modelGioHang.getRowCount() + 1;
        String tenMon = tenDichVu + " (" + (thuCung != null && !thuCung.isEmpty() ? thuCung : "Bé cưng") + ") - KTV: " + (ktv != null && !ktv.isEmpty() ? ktv : "Mio Staff");
        modelGioHang.addRow(new Object[]{
                stt,
                maDV,
                tenMon,
                dvt,
                1,
                df.format(giaDV),
                df.format(giaDV),
                CAC_BUOC_TIEN_DO[0]
        });
        capNhatTongTien();

        JOptionPane.showMessageDialog(this,
                "ĐÃ TIẾP NHẬN LỊCH HẸN [" + maLH + "] VÀO GIỎ HÀNG POS!\n" +
                        "• Khách hàng: " + khTen + " (" + sdt + ")\n" +
                        "• Thú cưng: " + thuCung + "\n" +
                        "• Dịch vụ: " + tenDichVu + " - Đơn giá: " + df.format(giaDV) + "\n\n" +
                        "Vui lòng kiểm tra giỏ hàng và tiến hành thanh toán.",
                "Tiếp nhận Lịch hẹn thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void tiepNhanDoiTraHang(DoiTraTransferData data) {
        if (data == null) return;
        chonTabPOS();
        this.thongTinDoiTraHienTai = data;

        dangCapNhatKhach = true;
        try {
            if (data.getSdtKhach() != null && !data.getSdtKhach().trim().isEmpty()) {
                txtSdtKhachHang.setText(data.getSdtKhach().trim());
            } else {
                txtSdtKhachHang.setText("");
            }
            if (data.getTenKhach() != null && !data.getTenKhach().trim().isEmpty()) {
                txtTenKhachHang.setText(data.getTenKhach().trim());
            } else {
                txtTenKhachHang.setText("");
            }
            tenKhachTuDongDien = false;
        } finally {
            dangCapNhatKhach = false;
        }
        traCuuKhachHang();

        // Xóa giỏ hàng hiện tại và đưa các món đổi mới vào giỏ
        modelGioHang.setRowCount(0);
        if (data.getDsMonMoi() != null) {
            for (DoiTraTransferData.MonDoiMoi item : data.getDsMonMoi()) {
                int stt = modelGioHang.getRowCount() + 1;
                long thanhTien = item.donGia * item.soLuong;
                modelGioHang.addRow(new Object[]{
                        stt,
                        item.maSP,
                        item.tenSP,
                        item.dvt,
                        item.soLuong,
                        df.format(item.donGia),
                        df.format(thanhTien),
                        CAC_BUOC_TIEN_DO[0]
                });
            }
        }

        // Hiển thị thanh thông báo khấu trừ đổi trả
        if (pnlThongBaoDoiTra != null && lblThongBaoDoiTra != null) {
            lblThongBaoDoiTra.setText("🔄 ĐỔI TRẢ TỪ HÓA ĐƠN [" + data.getMaHoaDonGoc() + "] | KHẤU TRỪ: -" + df.format(data.getTongTienTra()));
            pnlThongBaoDoiTra.setVisible(true);
        }

        capNhatTongTien();

        JOptionPane.showMessageDialog(this,
                "ĐÃ TIẾP NHẬN YÊU CẦU ĐỔI TRẢ TỪ HÓA ĐƠN [" + data.getMaHoaDonGoc() + "]!\n" +
                "• Khách hàng: " + (data.getTenKhach() != null ? data.getTenKhach() : "Khách lẻ") + " (" + (data.getSdtKhach() != null ? data.getSdtKhach() : "") + ")\n" +
                "• Giá trị khấu trừ hàng trả: -" + df.format(data.getTongTienTra()) + "\n" +
                "• Món đổi mới: " + (data.getDsMonMoi() != null ? data.getDsMonMoi().size() : 0) + " món đã thêm vào giỏ hàng.\n\n" +
                "Thu ngân có thể chọn thêm sản phẩm từ Catalog nếu khách mua thêm hàng,\n" +
                "sau đó kiểm tra tổng chênh lệch và bấm 'THANH TOÁN'.",
                "Tiếp nhận Đổi trả thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void huyDoiTraHang() {
        int cf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn hủy giao dịch đổi trả này không?\n" +
                "Giỏ hàng sẽ được làm trống và hủy bỏ khấu trừ hàng trả.",
                "Xác nhận hủy đổi trả", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (cf == JOptionPane.YES_OPTION) {
            thongTinDoiTraHienTai = null;
            if (pnlThongBaoDoiTra != null) {
                pnlThongBaoDoiTra.setVisible(false);
            }
            modelGioHang.setRowCount(0);
            capNhatTongTien();
            JOptionPane.showMessageDialog(this, "Đã hủy bỏ trạng thái đổi trả thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private volatile boolean dangXuLyThanhToan = false;

    // xử lý bấm nút thanh toán
    private void thucHienThanhToan() {
        if (dangXuLyThanhToan) {
            return;
        }
        dangXuLyThanhToan = true;
        try {
        if (modelGioHang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn sản phẩm hoặc dịch vụ trước khi thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // dừng gõ trên ô bảng nếu đang sửa số lượng
        if (tblGioHang.isEditing()) {
            tblGioHang.getCellEditor().stopCellEditing();
        }

        // coi thử có bé nào đang làm spa chưa xong ko
        boolean conDichVuChuaXong = false;
        StringBuilder sbChuaXong = new StringBuilder();
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String ma = (String) modelGioHang.getValueAt(i, 1);
            if (ma != null && ma.startsWith("DV")) {
                String td = modelGioHang.getValueAt(i, 7) != null ? modelGioHang.getValueAt(i, 7).toString() : "";
                if (!td.startsWith("4.") && !td.contains("XONG") && !td.contains("Hoàn tất")) {
                    conDichVuChuaXong = true;
                    sbChuaXong.append(" • ").append(modelGioHang.getValueAt(i, 2)).append(" [").append(td).append("]\n");
                }
            }
        }
        if (conDichVuChuaXong) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "CẢNH BÁO AN TOÀN TIẾN ĐỘ DỊCH VỤ:\n" +
                    "Đơn hàng vẫn còn dịch vụ thú cưng chưa hoàn tất:\n" +
                    sbChuaXong.toString() + "\n" +
                    "Bạn có chắc chắn muốn tiến hành thanh toán ngay bây giờ không?",
                    "Cảnh báo thanh toán an toàn",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // tính lại tiền cần trả sau khi trừ giảm giá với tiền đổi hàng cũ
        long tienKhauTruDoiTra = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getTongTienTra() : 0L;
        bus.TaiChinhHoaDonBus.KetQuaThanhToan ketQuaTaiChinh = bus.TaiChinhHoaDonBus.tinhThanhToan(
                java.math.BigDecimal.valueOf(tongTienHang),
                java.math.BigDecimal.valueOf(tienGiamVip),
                java.math.BigDecimal.valueOf(tienDoiDiem),
                java.math.BigDecimal.valueOf(tienKhauTruDoiTra));
        long canTra = ketQuaTaiChinh.getTienPhaiThanhToan().longValueExact();
        long tongPhaiTraTruocKhauTru = java.math.BigDecimal.valueOf(tongTienHang)
                .subtract(java.math.BigDecimal.valueOf(tienGiamVip))
                .subtract(java.math.BigDecimal.valueOf(tienDoiDiem))
                .max(java.math.BigDecimal.ZERO).longValueExact();
        long tienHoanThua = ketQuaTaiChinh.getTienHoanDoiTra().longValueExact();

        String tienDuaRaw = txtTienKhachDua != null ? txtTienKhachDua.getText().replaceAll("[^0-9]", "").trim() : "";
        long tienKhachDuaVal;
        try {
            tienKhachDuaVal = tienDuaRaw.isEmpty() ? 0 : Long.parseLong(tienDuaRaw);
        } catch (NumberFormatException ex) {
            tienKhachDuaVal = 0;
        }

        String pttt = cboPhuongThuc != null ? (String) cboPhuongThuc.getSelectedItem() : "Tiền mặt";
        if (pttt == null) pttt = "Tiền mặt";

        String maHD = lblMaHoaDon.getText();
        String sdt = txtSdtKhachHang.getText().trim();
        String tenNhap = txtTenKhachHang.getText().trim();
        String tenKhach = tenNhap.isEmpty() ? (sdt.isEmpty() ? "Khách vãng lai" : "Khách hàng mới") : tenNhap;

        long tienThoiVal = 0;
        if (tienHoanThua > 0) {
            tienThoiVal = tienHoanThua;
        // tiền mặt thì kiểm tra xem khách đưa đủ chưa
        } else if ("Tiền mặt".equals(pttt)) {
            if (canTra > 0 && tienKhachDuaVal < canTra) {
                long thieu = canTra - tienKhachDuaVal;
                JOptionPane.showMessageDialog(this,
                        "Tiền khách đưa chưa đủ để hoàn tất thanh toán!\n\n"
                        + "• Tổng tiền cần thanh toán: " + df.format(canTra) + "\n"
                        + "• Số tiền khách đã đưa: " + df.format(tienKhachDuaVal) + "\n"
                        + "• Số tiền còn thiếu: " + df.format(thieu) + "\n\n"
                        + "Vui lòng nhập đủ tiền trước khi hoàn tất hóa đơn.",
                        "Chưa đủ tiền thanh toán",
                        JOptionPane.WARNING_MESSAGE);
                if (txtTienKhachDua != null) {
                    txtTienKhachDua.requestFocus();
                    txtTienKhachDua.selectAll();
                }
                return;
            }
            tienThoiVal = Math.max(0, tienKhachDuaVal - canTra);
        } else { // chuyển khoản thì hiện popup vietqr
            if (canTra > 0) {
                boolean daNhanTien = VietQRDialog.xacNhanThanhToanVietQR(this, maHD, canTra, tenKhach);
                if (!daNhanTien) {
                    return; // Thu ngân hoặc khách hủy giao dịch QR
                }
            }
            tienKhachDuaVal = canTra;
            tienThoiVal = 0;
            if (txtTienKhachDua != null) {
                txtTienKhachDua.setText(canTra == 0 ? "0" : String.valueOf(canTra));
            }
            if (lblTienThoiLai != null) {
                lblTienThoiLai.setText("0 đ");
            }
        }

        String nowStr = LocalDateTime.now().format(dtf);

        // Tính điểm đã dùng, điểm thưởng mới và khấu trừ điểm trả hàng (Chuẩn hệ thống: 100.000 đ = 1 điểm)
        try {
            if (spnDiemDung != null) spnDiemDung.commitEdit();
        } catch (Exception ignored) {}
        int diemDaDung = (spnDiemDung != null && diemHienCo > 0) ? ((Number) spnDiemDung.getValue()).intValue() : 0;
        if (diemDaDung < 0) diemDaDung = 0;
        if (diemDaDung > diemHienCo) diemDaDung = diemHienCo;
        int diemThuongMoi = (int) (canTra / 100000L);
        int diemKhauTruDT = (thongTinDoiTraHienTai != null) ? thongTinDoiTraHienTai.getDiemKhauTru() : 0;
        int tongDiemMoi = Math.max(0, diemHienCo - diemDaDung - diemKhauTruDT) + diemThuongMoi;

        // Đảm bảo khách hàng tồn tại trong CSDL trước khi tạo Hóa đơn
        String sdtLuu = "";
        if (!sdt.isEmpty() || (!tenNhap.isEmpty() && !tenNhap.equalsIgnoreCase("Khách vãng lai"))) {
            sdtLuu = sdt.isEmpty() ? ("KH-" + tenNhap) : sdt;
            KhachHang khSaved = BanHangBus.luuHoacCapNhatKhachHang(sdtLuu, tenKhach, diemHienCo);
            if (khSaved != null) {
                tenKhach = khSaved.getHoTen();
                sdt = khSaved.getSoDienThoai();
            }
        }

        // Lưu Hóa đơn & trừ tồn kho FEFO vào CSDL SQL Server qua BanHangBus
        try {
            bus.BanHangBus busThanhToan = new bus.BanHangBus();
            bus.KhoLoHangBus busKho = new bus.KhoLoHangBus();
            java.util.List<entity.LoHang> allLo = busThanhToan.layTatCaLoHang();

            entity.HoaDon hdReal = new entity.HoaDon();
            hdReal.setMaHoaDon(maHD);
            hdReal.setNgayLap(LocalDateTime.now());
            hdReal.setThoiGianThanhToan(LocalDateTime.now());
            hdReal.setGiamGia(java.math.BigDecimal.valueOf(tienGiamVip));
            hdReal.setTienDoiDiem(java.math.BigDecimal.valueOf(tienDoiDiem));
            hdReal.setTienBuTruDoiTra(java.math.BigDecimal.valueOf(tienKhauTruDoiTra));
            hdReal.setThanhTien(java.math.BigDecimal.valueOf(canTra));
            hdReal.setPhuongThucThanhToan(pttt);
            hdReal.setTrangThaiHoaDon("Đã thanh toán");

            entity.NhanVien nv = new entity.NhanVien();
            String maNhanVienDangNhap = utils.UserSession.getUserId();
            if (maNhanVienDangNhap == null || maNhanVienDangNhap.isBlank()) {
                throw new IllegalStateException("Phiên đăng nhập đã hết hạn; vui lòng đăng nhập lại trước khi thanh toán");
            }
            nv.setMaNhanVien(maNhanVienDangNhap);
            hdReal.setNhanVien(nv);

            if (!sdt.isEmpty()) {
                entity.KhachHang kh = new bus.KhachHangBus().timKhachHangTheoSoDienThoai(sdt);
                if (kh != null) {
                    hdReal.setKhachHang(kh);
                }
            }

            int ctSeq = 1;

            // 1. KIỂM TRA TỒN KHO KHẢ DỤNG FEFO VÀ KHỞI TẠO CHI TIẾT HÓA ĐƠN TRƯỚC
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String ma = (String) modelGioHang.getValueAt(i, 1);
                String dvt = modelGioHang.getValueAt(i, 3) != null ? modelGioHang.getValueAt(i, 3).toString() : "";
                int sl = Integer.parseInt(modelGioHang.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                String dgStr = modelGioHang.getValueAt(i, 5).toString().replaceAll("[^0-9]", "");
                java.math.BigDecimal dg = dgStr.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(dgStr);

                if (ma != null && ma.startsWith("SP")) {
                    entity.QuyDoiDonVi qd = null;
                    try {
                        java.util.List<entity.QuyDoiDonVi> listQd = busKho.layQuyDoiBySanPham(ma);
                        if (listQd != null && !listQd.isEmpty()) {
                            for (entity.QuyDoiDonVi q : listQd) {
                                if (q.getDonViTinh() != null && q.getDonViTinh().getTenDonVi().equalsIgnoreCase(dvt)) {
                                    qd = q;
                                    break;
                                }
                            }
                            if (qd == null) {
                                qd = listQd.get(0);
                            }
                        }
                    } catch (Exception ignored) {}

                    int tyLe = (qd != null && qd.getTyLeQuyDoi() > 0) ? qd.getTyLeQuyDoi() : 1;
                    int soLuongCoSoCanXuat = sl * tyLe;
                    int remainingCoSo = soLuongCoSoCanXuat;

                    if (allLo != null) {
                        for (entity.LoHang candidate : allLo) {
                            if (remainingCoSo <= 0) break;
                            if (candidate.getSanPham() != null
                                    && candidate.getSanPham().getMaSanPham().equalsIgnoreCase(ma)
                                    && candidate.getSoLuongTon() > 0) {
                                if (candidate.getHanSuDung() != null && candidate.getHanSuDung().isBefore(java.time.LocalDate.now())) {
                                    continue; // Bỏ qua lô đã hết hạn sử dụng theo chuẩn FEFO an toàn
                                }
                                int take = Math.min(remainingCoSo, candidate.getSoLuongTon());
                                candidate.setSoLuongTon(candidate.getSoLuongTon() - take);

                                java.math.BigDecimal itemTotal = dg.multiply(java.math.BigDecimal.valueOf(sl));
                                java.math.BigDecimal ctTotal = (take == soLuongCoSoCanXuat) ? itemTotal
                                        : itemTotal.multiply(java.math.BigDecimal.valueOf(take)).divide(java.math.BigDecimal.valueOf(soLuongCoSoCanXuat), 0, java.math.RoundingMode.HALF_UP);

                                int slXuat;
                                java.math.BigDecimal donGiaXuat = dg;
                                entity.QuyDoiDonVi qdXuat = qd;
                                if (take == soLuongCoSoCanXuat) {
                                    slXuat = sl;
                                } else if (tyLe > 1 && take % tyLe == 0) {
                                    slXuat = take / tyLe;
                                } else if (tyLe > 1) {
                                    // Bán lẻ theo đơn vị cơ sở vì lô chia không tròn quy cách hộp/thùng
                                    slXuat = take;
                                    donGiaXuat = dg.divide(java.math.BigDecimal.valueOf(tyLe), 0, java.math.RoundingMode.HALF_UP);
                                    qdXuat = null;
                                } else {
                                    slXuat = take;
                                }

                                entity.ChiTietXuatLo ct = new entity.ChiTietXuatLo(
                                        maHD + "_CT" + (ctSeq++), hdReal, slXuat, donGiaXuat, candidate, qdXuat
                                );
                                ct.setSoLuongXuatCoSo(take);
                                ct.setThanhTien(ctTotal);
                                hdReal.addChiTiet(ct);
                                remainingCoSo -= take;
                            }
                        }
                    }
                    if (remainingCoSo > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Sản phẩm [" + ma + "] không đủ số lượng tồn khả dụng trong các lô hàng còn hạn (còn thiếu " + remainingCoSo + " đơn vị cơ sở)!\n"
                                + "Hệ thống dừng thanh toán và chưa ghi nhận phiếu đổi trả hay hóa đơn.",
                                "Cảnh báo thiếu tồn kho FEFO",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else if (ma != null && (ma.startsWith("DV") || ma.startsWith("GOI_SPA"))) {
                    String actualMaDv = ma;
                    if (actualMaDv.startsWith("DV-HOTEL-")) {
                        actualMaDv = "DV008"; // Ánh xạ mã dịch vụ Pet Hotel vào mã chuẩn CSDL
                    } else if (actualMaDv.equals("DV_SPAPASS") || actualMaDv.startsWith("GOI_SPA")) {
                        actualMaDv = "DV004"; // Ánh xạ sang mã dịch vụ Tắm Sấy Chuẩn (DV004) thay vì DV001 Cắt tỉa
                    }
                    entity.DichVu dv = new entity.DichVu();
                    dv.setMaDichVu(actualMaDv);

                    // Trích xuất KTV thực hiện dịch vụ từ dòng giỏ hàng (chống ghi nhận thu ngân thành KTV)
                    entity.NhanVien ktvThucHien = nv;
                    String tenDong = String.valueOf(modelGioHang.getValueAt(i, 2));
                    try {
                        List<entity.NhanVien> dsAllNV = new bus.NhanVienBus().layDanhSachNhanVien();
                        for (entity.NhanVien n : dsAllNV) {
                            if (tenDong.contains(n.getHoTen()) || tenDong.contains(n.getMaNhanVien())) {
                                ktvThucHien = n;
                                break;
                            }
                        }
                        if (ktvThucHien == nv) {
                            // Nếu vẫn là thu ngân quầy, tìm nhân viên chuyên viên Spa (NVCS/KTV) để phân bổ
                            for (entity.NhanVien n : dsAllNV) {
                                if (n.getChucVu() != null && (n.getChucVu().contains("Spa") || n.getChucVu().contains("KTV") || n.getChucVu().contains("Chăm sóc"))) {
                                    ktvThucHien = n;
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {}

                    java.math.BigDecimal hoaHong = dg.multiply(java.math.BigDecimal.valueOf(sl))
                            .multiply(new java.math.BigDecimal("0.10")).setScale(0, java.math.RoundingMode.HALF_UP);

                    entity.ChiTietDichVu ct = new entity.ChiTietDichVu(
                            maHD + "_CT" + (ctSeq++), hdReal, sl, dg, dv, null, ktvThucHien, hoaHong
                    );
                    hdReal.addChiTiet(ct);
                }
            }

            // 2. Chuẩn bị phiếu đổi trả; việc lưu sẽ được gộp cùng transaction hóa đơn.
            entity.PhieuDoiTra pdt = null;
            if (thongTinDoiTraHienTai != null) {
                pdt = new entity.PhieuDoiTra();
                pdt.setMaPhieuDoiTra(utils.MaTuSinhHelper.sinhMaPhieuDoiTra());
                entity.HoaDon hdGoc = new entity.HoaDon();
                hdGoc.setMaHoaDon(thongTinDoiTraHienTai.getMaHoaDonGoc());
                pdt.setHoaDonGoc(hdGoc);
                entity.NhanVien nvDT = new entity.NhanVien();
                nvDT.setMaNhanVien(maNhanVienDangNhap);
                pdt.setNhanVien(nvDT);
                pdt.setNgayDoiTra(LocalDateTime.now());
                pdt.setLyDoDoiTra("Đổi sang sản phẩm khác tại quầy POS");
                pdt.setTongTienHoan(java.math.BigDecimal.valueOf(tienHoanThua));
                pdt.setGiaTriHangTra(java.math.BigDecimal.valueOf(tienKhauTruDoiTra));
                pdt.setGiaTriHangDoi(java.math.BigDecimal.valueOf(tongPhaiTraTruocKhauTru));
                pdt.setTienHoanThucTe(java.math.BigDecimal.valueOf(tienHoanThua));
                pdt.setTienKhachTraThem(java.math.BigDecimal.valueOf(canTra));
                pdt.setPhuongThucHoanTien(tienHoanThua > 0
                        ? thongTinDoiTraHienTai.getPhuongThucHoanTien()
                        : bus.TaiChinhHoaDonBus.KHONG_HOAN_TIEN);
                pdt.setTrangThaiDoiTra("Hoàn thành");

                int idx = 1;
                if (thongTinDoiTraHienTai.getDsMonTra() != null) {
                    for (DoiTraTransferData.MonTraLai mon : thongTinDoiTraHienTai.getDsMonTra()) {
                        entity.ChiTietDoiTra ctDoiTra = new entity.ChiTietDoiTra(
                                pdt.getMaPhieuDoiTra() + "_" + (idx++),
                                pdt,
                                new entity.SanPham(mon.maSP, null, "", ""),
                                mon.soLuong,
                                java.math.BigDecimal.valueOf(mon.thanhTien)
                        );
                        ctDoiTra.setSoLo(mon.soLo);
                        ctDoiTra.setSoLuongCoSoHoan(mon.soLuongCoSoHoan);
                        pdt.addChiTietDoiTra(ctDoiTra);
                    }
                }
            }

            String maTheSuDung = null;
            entity.TheSpaPass theMoi = null;
            List<String> dsMaPhongHotel = new ArrayList<>();
            List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel = new ArrayList<>();
            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String ma = String.valueOf(modelGioHang.getValueAt(i, 1));
                String ten = String.valueOf(modelGioHang.getValueAt(i, 2));
                if (ma.startsWith("DV-HOTEL-")) {
                    String mp = ma.substring("DV-HOTEL-".length()).trim().toUpperCase();
                    dsMaPhongHotel.add(mp);
                    entity.HotelCheckoutSnapshot snap = mapHotelSnapshots.get(mp);
                    if (snap == null) {
                        throw new IllegalStateException("Không tìm thấy snapshot thanh toán hợp lệ của phòng Pet Hotel [" + mp + "]! Vui lòng thực hiện Check-out từ giao diện Pet Hotel (TC13).");
                    }
                    dsSnapshotsHotel.add(snap);
                }
                if ("DV_SPAPASS".equals(ma)) {
                    if (theSpaPassHienTai == null || !theSpaPassHienTai.isKhaDung()) {
                        throw new IllegalStateException("Không tìm thấy Spa Pass còn hiệu lực để trừ lượt");
                    }
                    maTheSuDung = theSpaPassHienTai.getMaThe();
                }
                if (ma.startsWith("GOI_SPA") || ten.contains("Spa Pass") || ten.contains("Gói Spa")) {
                    if (theMoi != null) {
                        throw new IllegalStateException("Mỗi hóa đơn chỉ được kích hoạt một gói Spa Pass");
                    }
                    if (sdt == null || sdt.isBlank()) {
                        throw new IllegalStateException("Mua Spa Pass bắt buộc phải chọn khách hàng có số điện thoại");
                    }
                    // Lấy cấu hình gói Spa Pass chuẩn từ CSDL (DV14)
                    entity.GoiSpaPass cauHinhGoi = new bus.SpaPassBus().layCauHinhGoi(ma);
                    int soBuoi = cauHinhGoi.getSoBuoi();
                    int thoiHanNgay = cauHinhGoi.getThoiHanNgay();
                    long giaGoi = cauHinhGoi.getDonGiaGoi().longValue();

                    theMoi = new entity.TheSpaPass(
                            utils.MaTuSinhHelper.sinhMaTheSpaPass(sdt),
                            sdt.trim(), tenKhach, cauHinhGoi.getTenGoi(), soBuoi, 0, giaGoi,
                            LocalDate.now(), LocalDate.now().plusDays(thoiHanNgay)
                    );
                }
            }
            if (maTheSuDung != null && theMoi != null) {
                throw new IllegalStateException("Không thể vừa dùng lượt vừa kích hoạt Spa Pass mới trong cùng hóa đơn");
            }

            // 3. Hóa đơn, đổi trả, kho và Spa Pass cùng commit/rollback.
            boolean okHD = busThanhToan.luuGiaoDichPosDayDu(
                    hdReal, pdt, maTheSuDung, theMoi, dsMaPhongHotel, dsSnapshotsHotel,
                    diemDaDung, diemKhauTruDT);
            if (!okHD) {
                throw new SQLException("Lưu hóa đơn và cập nhật kho không thành công trong CSDL SQL Server!");
            }

            // Đồng bộ lại từ DB; không cộng RAM thủ công để tránh hoàn kho hai lần/sai lô.
            KhoLoHangPanel.dongBoLoHangTuDatabase();
            BanHangPOSPanel.dongBoToanBoCatalogVaGiaoDien();
            bus.SpaPassBus.dongBoDuLieuTuDatabase();
            if (!dsMaPhongHotel.isEmpty()) {
                PetHotelPanel.dongBoVaLamMoiGiaoDien();
            }
        } catch (Exception ex) {
            System.err.println("[BanHangPOSPanel] Lỗi ghi Hóa đơn CSDL: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                    "KHÔNG THỂ LƯU HÓA ĐƠN VÀO CƠ SỞ DỮ LIỆU SQL SERVER!\n\nChi tiết lỗi: " + ex.getMessage()
                    + "\n\n(Hệ thống đã dừng lại và không in hóa đơn ảo vì CSDL chưa ghi nhận thành công)",
                    "Lỗi Giao Dịch CSDL",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Đồng bộ tức thời tồn kho Catalog và Lô hàng FEFO từ Database xuống giao diện
        dongBoToanBoCatalogVaGiaoDien();
        gui.KhoLoHangPanel.dongBoLoHangTuDatabase();

        HoaDonDaThanhToan hd = new HoaDonDaThanhToan();
        hd.maHoaDon = maHD;
        hd.thoiGian = nowStr;
        hd.tenKhach = tenKhach;
        hd.sdt = sdt;
        hd.thuNgan = utils.UserSession.getFullName();
        hd.tongTienHang = tongTienHang;
        hd.giamGia = tienGiamGia;
        hd.tienDoiDiem = tienDoiDiem;
        hd.tienBuTruDoiTra = tienKhauTruDoiTra;
        hd.tongThanhToan = canTra;
        hd.phuongThuc = pttt;
        hd.trangThai = "Đã thanh toán";

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            hd.dsMon.add(new Object[]{
                    modelGioHang.getValueAt(i, 0),
                    modelGioHang.getValueAt(i, 1),
                    modelGioHang.getValueAt(i, 2),
                    modelGioHang.getValueAt(i, 3),
                    modelGioHang.getValueAt(i, 4),
                    modelGioHang.getValueAt(i, 5),
                    modelGioHang.getValueAt(i, 6),
                    modelGioHang.getValueAt(i, 7)
            });
        }

        dsLichSuHoaDon.add(0, hd);
        for (BanHangPOSPanel inst : INSTANCES) {
            inst.capNhatBangLichSu();
        }
        TongQuanPanel.lamMoiDuLieuDashboardAll();

        // Hiển thị Biểu mẫu Hóa đơn bán hàng (Supermarket / Pet Shop Receipt) ngay sau khi thanh toán
        HoaDonPreviewDialog.hienThiHoaDon(this, hd, tienKhachDuaVal, tienThoiVal, diemThuongMoi, tongDiemMoi);

        thongTinDoiTraHienTai = null;
        if (pnlThongBaoDoiTra != null) {
            pnlThongBaoDoiTra.setVisible(false);
        }

        lamSachKhungHoaDon();
        } finally {
            dangXuLyThanhToan = false;
        }
    }

    /**
     * Mô hình 3: Cảnh báo Dị ứng Thực phẩm (Allergy Guard)
     * Trả về true nếu an toàn hoặc khách xác nhận tự chọn mua.
     * Trả về false nếu thu ngân chọn "Đổi sang món khác".
     */
    public boolean kiemTraVaCanhBaoDiUng(CatalogItem item) {
        if (lblThuCungDangChon == null || item == null) return true;
        String tcText = lblThuCungDangChon.getText();
        if (tcText == null || tcText.contains("Chưa chọn") || tcText.contains("Khách vãng lai") || tcText.contains("bình thường")) {
            return true; // Khách vãng lai / chưa chọn thú cưng / sức khỏe bình thường -> Không cảnh báo
        }

        String itemNameLower = item.ten != null ? item.ten.toLowerCase() : "";
        boolean canhBaoGa = tcText.contains("DỊ ỨNG ĐẠM GÀ") && (itemNameLower.contains("gà") || itemNameLower.contains("chicken") || "SP001".equalsIgnoreCase(item.ma) || "SP002".equalsIgnoreCase(item.ma));
        boolean canhBaoLactose = tcText.contains("DỊ ỨNG LACTOSE") && (itemNameLower.contains("sữa") || itemNameLower.contains("milk") || itemNameLower.contains("bơ") || itemNameLower.contains("cheese") || itemNameLower.contains("lactose"));

        if (!canhBaoGa && !canhBaoLactose) {
            return true;
        }

        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }

        String lyDoDiUng = canhBaoGa ? "DỊ ỨNG ĐẠM GÀ" : "DỊ ỨNG LACTOSE (SỮA/BƠ)";
        String loiKhuyen = canhBaoGa ? "Khuyên dùng: Đổi sang hạt/pate vị Cá hồi hoặc Cừu để tránh kích ứng da & đường ruột."
                                     : "Khuyên dùng: Đổi sang các dòng sữa hạt chuyên dụng hoặc pate không chứa lactose.";

        Object[] options = {"Đổi sang món khác (Khuyên dùng)", "Khách xác nhận tự chọn mua"};
        int luaChon = JOptionPane.showOptionDialog(
                this,
                "⚠️ CẢNH BÁO AN TOÀN SỨC KHỎE (ALLERGY GUARD):\n\n"
                        + "Thú cưng đang chọn có tiền sử: [" + lyDoDiUng + "]\n"
                        + "Món đang thêm: " + item.ten + "\n\n"
                        + loiKhuyen + "\n\n"
                        + "Bạn muốn xử lý thế nào?",
                "Cảnh báo dị ứng thú cưng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        return luaChon == 1; // 1 = Khách xác nhận tự chọn mua -> Tiếp tục thêm vào giỏ; 0 hoặc đóng dialog = Hủy/Đổi món
    }

    /**
     * Mô hình 3: Gợi ý bán kèm thông minh (Smart Cross-Sell)
     */
    private void capNhatThanhCrossSell() {
        if (pnlCrossSell == null || modelGioHang == null) return;
        if (modelGioHang.getRowCount() == 0) {
            pnlCrossSell.setVisible(false);
            crossSellItemHienTai = null;
            return;
        }

        boolean coDichVu = false;
        boolean coSanPham = false;
        boolean daCoBioCare = false;
        boolean daCoPate = false;

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String ma = (String) modelGioHang.getValueAt(i, 1);
            String ten = (String) modelGioHang.getValueAt(i, 2);
            if (ma != null && (ma.startsWith("DV") || (ten != null && (ten.contains("Spa") || ten.contains("Tắm") || ten.contains("Khách sạn"))))) {
                coDichVu = true;
            }
            if (ma != null && (ma.startsWith("SP") || (ten != null && (ten.contains("Hạt") || ten.contains("Pate") || ten.contains("Xịt"))))) {
                coSanPham = true;
            }
            if ("SP004".equalsIgnoreCase(ma) || (ten != null && ten.contains("Bio-Care"))) {
                daCoBioCare = true;
            }
            if ("SP002".equalsIgnoreCase(ma) || "SP008".equalsIgnoreCase(ma) || (ten != null && ten.contains("Pate"))) {
                daCoPate = true;
            }
        }

        CatalogItem itemGoiY = null;
        String textGoiY = null;

        if (coDichVu && !daCoBioCare) {
            for (CatalogItem ci : allItems) {
                if ("SP004".equalsIgnoreCase(ci.ma) && ci.tonKho > 0) {
                    itemGoiY = ci;
                    textGoiY = "💡 Khuyên dùng sau Spa: " + ci.ten + " (Tồn: " + ci.tonKho + ") · " + df.format(ci.donGia);
                    break;
                }
            }
        } else if (coSanPham && !daCoPate) {
            String tcText = lblThuCungDangChon != null ? lblThuCungDangChon.getText() : "";
            String targetMa = (tcText != null && tcText.contains("DỊ ỨNG ĐẠM GÀ")) ? "SP008" : "SP002";
            for (CatalogItem ci : allItems) {
                if (targetMa.equalsIgnoreCase(ci.ma) && ci.tonKho > 0) {
                    itemGoiY = ci;
                    textGoiY = "💡 Mua kèm bữa ăn: " + ci.ten + " (Tồn: " + ci.tonKho + ") · " + df.format(ci.donGia);
                    break;
                }
            }
        }

        if (itemGoiY != null && textGoiY != null) {
            crossSellItemHienTai = itemGoiY;
            lblCrossSellText.setText(textGoiY);
            pnlCrossSell.setVisible(true);
        } else {
            pnlCrossSell.setVisible(false);
            crossSellItemHienTai = null;
        }
    }

    private void themSanPhamCrossSellHienTai() {
        if (crossSellItemHienTai != null) {
            themSanPhamVaoGio(crossSellItemHienTai);
            if (pnlCrossSell != null) {
                pnlCrossSell.setVisible(false);
            }
        }
    }

    /**
     * Mô hình 1: Thẻ Spa Pass Punch Card - Dùng 1 buổi
     */
    private void suDungLuotSpaPass() {
        if (theSpaPassHienTai == null || !theSpaPassHienTai.isKhaDung()) {
            JOptionPane.showMessageDialog(this, "Khách hàng không có thẻ Spa Pass hoặc thẻ đã hết số buổi khả dụng!", "Spa Pass", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String ma = (String) modelGioHang.getValueAt(i, 1);
            if ("DV_SPAPASS".equals(ma)) {
                JOptionPane.showMessageDialog(this, "Hóa đơn này đã có 1 lượt trừ buổi Spa Pass!", "Spa Pass", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        CatalogItem passItem = new CatalogItem(
                "DV_SPAPASS",
                "Dịch vụ Spa Grooming (Trừ Thẻ Pass " + theSpaPassHienTai.getMaThe() + ")",
                "Spa & Trị liệu",
                "Buổi",
                "KTV Chuyên nghiệp",
                999,
                0,
                IconHelper.SPARKLES,
                "Buổi Spa Grooming trọn gói từ Thẻ Pass gia đình"
        );
        themSanPhamVaoGio(passItem);
        if (btnDungSpaPass != null) {
            btnDungSpaPass.setEnabled(false);
        }
        JOptionPane.showMessageDialog(
                this,
                "Đã áp dụng 1 buổi Spa Grooming (0 đ) từ thẻ " + theSpaPassHienTai.getTenThe() + "!\n"
                        + "Số buổi còn lại sau đơn này: " + (theSpaPassHienTai.getSoBuoiConLai() - 1) + "/" + theSpaPassHienTai.getTongSoBuoi() + " buổi.",
                "Spa Pass Punch Card",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void moDialogTraCuuSpaPass() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "TRA CỨU & QUẢN LÝ THẺ SPA PASS TẠI QUẦY", true);
        dlg.setSize(820, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        ((JPanel) dlg.getContentPane()).setBorder(new EmptyBorder(14, 16, 14, 16));
        dlg.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTop.setOpaque(false);

        JTextField txtSearch = new JTextField(18);
        txtSearch.setPreferredSize(new Dimension(240, 36));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo SĐT, tên khách, mã thẻ...");
        txtSearch.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtSearch.setBorder(ThemeColors.createInputBorder());

        String sdtHienTai = txtSdtKhachHang != null ? txtSdtKhachHang.getText().trim() : "";
        if (!sdtHienTai.isEmpty()) {
            txtSearch.setText(sdtHienTai);
        }

        JButton btnPhatHanh = ThemeColors.createPrimaryButton("+ Bán thẻ mới", IconHelper.getIcon(IconHelper.PLUS, 13, Color.WHITE));
        pnlTop.add(new JLabel("Tìm kiếm:"));
        pnlTop.add(txtSearch);
        pnlTop.add(btnPhatHanh);

        String[] cols = {"Mã Thẻ", "Khách hàng", "Số điện thoại", "Tên gói", "Tổng", "Đã dùng", "Còn lại", "Hạn dùng", "Trạng thái"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tbl = new JTable(model);
        tbl.setRowHeight(32);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);

        Runnable loadData = () -> {
            model.setRowCount(0);
            String kw = txtSearch.getText().trim().toLowerCase();
            for (TheSpaPass the : SpaPassBus.layTatCaThe()) {
                the.capNhatTrangThai();
                if (!kw.isEmpty()) {
                    boolean match = (the.getMaThe() != null && the.getMaThe().toLowerCase().contains(kw))
                            || (the.getTenKhachHang() != null && the.getTenKhachHang().toLowerCase().contains(kw))
                            || (the.getSdtKhachHang() != null && the.getSdtKhachHang().contains(kw));
                    if (!match) continue;
                }
                model.addRow(new Object[]{
                        the.getMaThe(),
                        the.getTenKhachHang(),
                        the.getSdtKhachHang(),
                        the.getTenGoi(),
                        the.getTongSoBuoi(),
                        the.getSoBuoiDaDung(),
                        the.getSoBuoiConLai(),
                        the.getNgayHetHanStr(),
                        the.getTrangThai()
                });
            }
        };

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { loadData.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { loadData.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { loadData.run(); }
        });

        btnPhatHanh.addActionListener(e -> {
            JOptionPane.showMessageDialog(dlg,
                    "Không phát hành thẻ miễn phí từ màn tra cứu. Hãy thêm Gói Spa Pass vào giỏ hàng\n"
                    + "và thanh toán; thẻ chỉ được kích hoạt sau khi hóa đơn lưu thành công.",
                    "Bán gói qua POS", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setOpaque(false);

        JButton btnChonThe = ThemeColors.createPrimaryButton("Áp dụng thẻ này cho đơn hàng", IconHelper.getIcon(IconHelper.CHECK, 13, Color.WHITE));
        JButton btnDong = ThemeColors.createSecondaryButton("Đóng", null);

        btnChonThe.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn 1 thẻ trên bảng để áp dụng!", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String maThe = model.getValueAt(r, 0).toString();
            TheSpaPass the = SpaPassBus.timTheTheoMa(maThe);
            if (the != null) {
                theSpaPassHienTai = the;
                if (txtSdtKhachHang != null) txtSdtKhachHang.setText(the.getSdtKhachHang());
                if (txtTenKhachHang != null) txtTenKhachHang.setText(the.getTenKhachHang());
                if (lblSpaPassStatus != null) {
                    lblSpaPassStatus.setText(the.getTenThe() + ": Còn " + the.getSoBuoiConLai() + "/" + the.getTongSoBuoi() + " buổi");
                    lblSpaPassStatus.setForeground(ThemeColors.PRIMARY_ACTIVE);
                }
                if (btnDungSpaPass != null) {
                    btnDungSpaPass.setEnabled(the.isKhaDung());
                }
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Đã liên kết Thẻ Spa Pass của [" + the.getTenKhachHang() + "] vào đơn hiện tại!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnDong.addActionListener(e -> dlg.dispose());
        pnlBottom.add(btnChonThe);
        pnlBottom.add(btnDong);

        loadData.run();

        dlg.add(pnlTop, BorderLayout.NORTH);
        dlg.add(new JScrollPane(tbl), BorderLayout.CENTER);
        dlg.add(pnlBottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ==========================================
    // 2. TAB HÓA ĐƠN CHỜ PHỤC VỤ (GRID CARDS 1 CHẠM)
    // ==========================================
    private JPanel taoTabDonChoGrid() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBackground(ThemeColors.BACKGROUND);
        pnl.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setBackground(ThemeColors.SURFACE);
        pnlHeader.setBorder(ThemeColors.createCardBorder(12, 16, 12, 16));

        JButton btnBack = ThemeColors.createSecondaryButton("← Quay lại quầy POS", IconHelper.getIcon(IconHelper.CART, 14, ThemeColors.TEXT_SECONDARY));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> chonTabPOS());

        lblSoDonChoHeader = new JLabel("DANH SÁCH ĐƠN HÀNG LƯU TẠM ĐANG CHỜ PHỤC VỤ (0 ĐƠN)",
                IconHelper.getIcon(IconHelper.CLOCK, 18, ThemeColors.WARNING), SwingConstants.LEFT);
        lblSoDonChoHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSoDonChoHeader.setForeground(ThemeColors.TEXT_PRIMARY);

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlLeft.setOpaque(false);
        pnlLeft.add(btnBack);
        pnlLeft.add(lblSoDonChoHeader);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        txtTimDonCho = new JTextField(18);
        txtTimDonCho.setPreferredSize(new Dimension(240, 34));
        txtTimDonCho.putClientProperty("JTextField.placeholderText", "Tìm theo SĐT, tên khách, mã đơn...");
        txtTimDonCho.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimDonCho.setBorder(ThemeColors.createInputBorder());
        txtTimDonCho.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { capNhatGiaoDienDonCho(); }
            @Override
            public void removeUpdate(DocumentEvent e) { capNhatGiaoDienDonCho(); }
            @Override
            public void changedUpdate(DocumentEvent e) { capNhatGiaoDienDonCho(); }
        });

        JLabel lblHint = new JLabel("Click thẻ để khôi phục đơn", SwingConstants.RIGHT);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(ThemeColors.TEXT_MUTED);

        pnlRight.add(txtTimDonCho);
        pnlRight.add(lblHint);

        pnlHeader.add(pnlLeft, BorderLayout.WEST);
        pnlHeader.add(pnlRight, BorderLayout.EAST);

        pnlCardsDonCho = new JPanel(new GridLayout(0, 3, 14, 14));
        pnlCardsDonCho.setOpaque(false);

        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.add(pnlCardsDonCho, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(pnlWrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(scroll, BorderLayout.CENTER);

        capNhatGiaoDienDonCho();
        return pnl;
    }

    private boolean luuDsDonTamXuongDisk() {
        try {
            java.io.File file = new java.io.File("config/pos_don_tam.dat");
            if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
            try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
                oos.writeObject(new ArrayList<>(dsDonTam));
            }
            return true;
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi lưu đơn tạm disk: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void taiDsDonTamTuDisk() {
        try {
            java.io.File file = new java.io.File("config/pos_don_tam.dat");
            if (file.exists()) {
                try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(file))) {
                    List<DonHangTam> loaded = (List<DonHangTam>) ois.readObject();
                    if (loaded != null) {
                        dsDonTam.clear();
                        dsDonTam.addAll(loaded);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi đọc đơn tạm disk: " + e.getMessage());
        }
    }

    private void capNhatGiaoDienDonCho() {
        try {
            if (pnlCardsDonCho == null) return;
            pnlCardsDonCho.removeAll();

            if (lblSoDonChoHeader != null) {
                lblSoDonChoHeader.setText("DANH SÁCH ĐƠN HÀNG LƯU TẠM ĐANG CHỜ PHỤC VỤ (" + dsDonTam.size() + " ĐƠN)");
            }
            if (btnQuickDonCho != null) {
                btnQuickDonCho.setText("Đơn chờ (" + dsDonTam.size() + ")");
            }

            if (dsDonTam.isEmpty()) {
                JPanel pnlEmpty = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
                pnlEmpty.setOpaque(false);
                JLabel lblEmpty = new JLabel("Hiện không có hóa đơn nào đang lưu tạm. Bấm 'Lưu tạm' tại quầy POS để chuyển đơn vào đây.");
                lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                lblEmpty.setForeground(ThemeColors.TEXT_MUTED);
                pnlEmpty.add(lblEmpty);
                pnlCardsDonCho.setLayout(new BorderLayout());
                pnlCardsDonCho.add(pnlEmpty, BorderLayout.CENTER);
            } else {
                pnlCardsDonCho.setLayout(new GridLayout(0, 3, 14, 14));
                String kw = (txtTimDonCho != null) ? txtTimDonCho.getText().trim().toLowerCase() : "";
                int countHienThi = 0;
                for (DonHangTam don : dsDonTam) {
                    boolean match = kw.isEmpty()
                            || (don.maHoaDon != null && don.maHoaDon.toLowerCase().contains(kw))
                            || (don.sdt != null && don.sdt.toLowerCase().contains(kw))
                            || (don.tenKhach != null && don.tenKhach.toLowerCase().contains(kw));
                    if (match) {
                        pnlCardsDonCho.add(taoCardDonCho(don));
                        countHienThi++;
                    }
                }
                if (countHienThi == 0) {
                    JPanel pnlKhongThay = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
                    pnlKhongThay.setOpaque(false);
                    JLabel lblKt = new JLabel("Không tìm thấy đơn tạm nào khớp với từ khóa: '" + kw + "'");
                    lblKt.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    lblKt.setForeground(ThemeColors.TEXT_MUTED);
                    pnlKhongThay.add(lblKt);
                    pnlCardsDonCho.setLayout(new BorderLayout());
                    pnlCardsDonCho.add(pnlKhongThay, BorderLayout.CENTER);
                }
            }

            pnlCardsDonCho.revalidate();
            pnlCardsDonCho.repaint();
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi cập nhật giao diện đơn chờ: " + e.getMessage());
        }
    }

    private JPanel taoCardDonCho(DonHangTam don) {
        // Duyệt qua tất cả các dòng dịch vụ trong đơn tạm (Multi-Service Tracking)
        List<Integer> listDvIdx = new ArrayList<>();
        int daXongCount = 0;
        for (int i = 0; i < don.dsMon.size(); i++) {
            Object[] row = don.dsMon.get(i);
            String ma = row.length > 1 && row[1] != null ? row[1].toString() : "";
            if (ma.startsWith("DV")) {
                listDvIdx.add(i);
                String td = row.length > 7 && row[7] != null ? row[7].toString() : "";
                if (td.startsWith("4.") || td.contains("XONG") || td.contains("Hoàn tất")) {
                    daXongCount++;
                }
            }
        }
        int tongDv = listDvIdx.size();
        boolean hasDv = tongDv > 0;
        boolean allDvDone = hasDv && (daXongCount == tongDv);

        JPanel card = new JPanel(new BorderLayout(8, 8));
        int cardHeight = hasDv ? Math.min(270, 175 + tongDv * 28 + 25) : 165;
        card.setPreferredSize(new Dimension(270, cardHeight));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblMa = new JLabel(don.maHoaDon);
        lblMa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMa.setForeground(ThemeColors.PRIMARY_ACTIVE);

        JLabel lblBadge;
        if (hasDv && allDvDone) {
            lblBadge = ThemeColors.createBadge("Dịch vụ xong (" + daXongCount + "/" + tongDv + ")", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS_TEXT);
        } else if (hasDv) {
            lblBadge = ThemeColors.createBadge("Đang làm (" + daXongCount + "/" + tongDv + ")", ThemeColors.WARNING_LIGHT, ThemeColors.WARNING_TEXT);
        } else {
            lblBadge = ThemeColors.createBadge("Chờ thanh toán", ThemeColors.WARNING_LIGHT, ThemeColors.WARNING_TEXT);
        }

        JButton btnHuyDon = new JButton(IconHelper.getIcon(IconHelper.TRASH, 12, ThemeColors.DANGER));
        btnHuyDon.setPreferredSize(new Dimension(26, 26));
        btnHuyDon.setFocusPainted(false);
        btnHuyDon.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));
        btnHuyDon.setBackground(Color.WHITE);
        btnHuyDon.setToolTipText("Hủy đơn tạm này");
        btnHuyDon.addActionListener(e -> {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn hủy đơn tạm [" + don.maHoaDon + "]?",
                    "Hủy đơn tạm", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                dsDonTam.remove(don);
                luuDsDonTamXuongDisk();
                capNhatGiaoDienDonCho();
            }
        });

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(lblBadge);
        pnlRight.add(btnHuyDon);

        pnlTop.add(lblMa, BorderLayout.WEST);
        pnlTop.add(pnlRight, BorderLayout.EAST);

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);

        JLabel lblKhach = new JLabel("Khách: " + don.tenKhach + (don.sdt.isEmpty() ? "" : " (" + don.sdt + ")"));
        lblKhach.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKhach.setForeground(ThemeColors.TEXT_PRIMARY);
        lblKhach.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTime = new JLabel("Lưu lúc: " + don.thoiGianTao + " · " + don.dsMon.size() + " món");
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTime.setForeground(ThemeColors.TEXT_MUTED);
        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTongTien = new JLabel("Tổng tiền: " + df.format(don.khachCanTra));
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongTien.setForeground(ThemeColors.PRIMARY_ACTIVE);
        lblTongTien.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlCenter.add(lblKhach);
        pnlCenter.add(Box.createVerticalStrut(2));
        pnlCenter.add(lblTime);
        pnlCenter.add(Box.createVerticalStrut(2));
        pnlCenter.add(lblTongTien);

        if (hasDv) {
            pnlCenter.add(Box.createVerticalStrut(6));
            // Thanh tiến độ tổng thể (Overall Progress Bar)
            int pct = (daXongCount * 100) / tongDv;
            JProgressBar pgbTienDo = new JProgressBar(0, 100);
            pgbTienDo.setValue(pct);
            pgbTienDo.setStringPainted(true);
            pgbTienDo.setString(daXongCount + "/" + tongDv + " dịch vụ (" + pct + "%)");
            pgbTienDo.setFont(new Font("Segoe UI", Font.BOLD, 10));
            pgbTienDo.setForeground(allDvDone ? ThemeColors.SUCCESS : ThemeColors.PRIMARY_ACTIVE);
            pgbTienDo.setBackground(Color.decode("#FAEEE7"));
            pgbTienDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
            pgbTienDo.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlCenter.add(pgbTienDo);
            pnlCenter.add(Box.createVerticalStrut(4));

            // Danh sách từng dòng dịch vụ kèm nút Badge 1-chạm cập nhật tiến độ độc lập
            for (int sIdx : listDvIdx) {
                Object[] row = don.dsMon.get(sIdx);
                String tenMon = row.length > 2 && row[2] != null ? row[2].toString() : "Dịch vụ";
                if (tenMon.length() > 22) {
                    tenMon = tenMon.substring(0, 20) + "..";
                }
                String curTd = row.length > 7 && row[7] != null ? row[7].toString() : CAC_BUOC_TIEN_DO[0];

                JPanel pnlServiceRow = new JPanel(new BorderLayout(4, 0));
                pnlServiceRow.setOpaque(false);
                pnlServiceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
                pnlServiceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel lblTenDv = new JLabel("• " + tenMon);
                lblTenDv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                lblTenDv.setForeground(ThemeColors.TEXT_PRIMARY);

                JButton btnStep = taoButtonTienDo1Cham(don, sIdx, curTd);
                pnlServiceRow.add(lblTenDv, BorderLayout.CENTER);
                pnlServiceRow.add(btnStep, BorderLayout.EAST);

                pnlCenter.add(pnlServiceRow);
                pnlCenter.add(Box.createVerticalStrut(2));
            }
        }

        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        JLabel lblRestoreHint = new JLabel("Nhấn để nạp lại vào POS >");
        lblRestoreHint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRestoreHint.setForeground(ThemeColors.PRIMARY);
        pnlBottom.add(lblRestoreHint, BorderLayout.EAST);

        card.add(pnlTop, BorderLayout.NORTH);
        card.add(pnlCenter, BorderLayout.CENTER);
        card.add(pnlBottom, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                khoiPhucDonTamVaoPos(don);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(ThemeColors.PRIMARY_TINT);
                card.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                        new EmptyBorder(12, 14, 12, 14)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(ThemeColors.SURFACE);
                card.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));
            }
        });

        return card;
    }

    private void khoiPhucDonTamVaoPos(DonHangTam don) {
        if (modelGioHang.getRowCount() > 0) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Giỏ hàng POS đang có món chưa thanh toán!\nBạn có muốn thay thế giỏ hàng hiện tại bằng đơn tạm [" + don.maHoaDon + "]?",
                    "Xác nhận nạp đơn tạm", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        // Xóa sạch trạng thái nghiệp vụ của đơn trước (TC12)
        mapHotelSnapshots.clear();
        this.thongTinDoiTraHienTai = null;
        this.theSpaPassHienTai = null;
        if (pnlThongBaoDoiTra != null) pnlThongBaoDoiTra.setVisible(false);
        if (lblSpaPassStatus != null) lblSpaPassStatus.setText("Không có thẻ");
        if (btnDungSpaPass != null) btnDungSpaPass.setEnabled(false);

        lblMaHoaDon.setText(don.maHoaDon);
        dangCapNhatKhach = true;
        try {
            txtSdtKhachHang.setText(don.sdt != null ? don.sdt : "");
            txtTenKhachHang.setText(don.tenKhach != null && !don.tenKhach.equalsIgnoreCase("Khách vãng lai") ? don.tenKhach : "");
            tenKhachTuDongDien = false;
        } finally {
            dangCapNhatKhach = false;
        }
        traCuuKhachHang();
        if (spnDiemDung != null) {
            long soDiemDaLuu = Math.max(0L, don.tienDoiDiem / 1000L);
            spnDiemDung.setValue((int) Math.min(soDiemDaLuu, Math.max(0, diemHienCo)));
        }
        if (don.phuongThuc != null && don.phuongThuc.contains("Tiền mặt")) {
            cboPhuongThuc.setSelectedItem("Tiền mặt");
        } else {
            cboPhuongThuc.setSelectedItem("Chuyển khoản");
        }

        modelGioHang.setRowCount(0);
        for (Object[] row : don.dsMon) {
            if (row.length == 7) {
                String ma = (String) row[1];
                Object[] r8 = new Object[8];
                System.arraycopy(row, 0, r8, 0, 7);
                r8[7] = (ma != null && ma.startsWith("DV")) ? CAC_BUOC_TIEN_DO[0] : "—";
                modelGioHang.addRow(r8);
            } else {
                modelGioHang.addRow(row);
            }
        }
        // Khôi phục snapshots Pet Hotel và thông tin Đổi trả (TC12)
        if (don.dsSnapshotsHotel != null && !don.dsSnapshotsHotel.isEmpty()) {
            for (entity.HotelCheckoutSnapshot snap : don.dsSnapshotsHotel) {
                if (snap != null && snap.getMaPhong() != null) {
                    mapHotelSnapshots.put(snap.getMaPhong().trim().toUpperCase(), snap);
                }
            }
        }
        if (don.thongTinDoiTra != null) {
            this.thongTinDoiTraHienTai = don.thongTinDoiTra;
            pnlThongBaoDoiTra.setVisible(true);
            lblThongBaoDoiTra.setText("<html>Đang áp dụng đổi hàng từ HĐ <b>" + don.thongTinDoiTra.getMaHoaDonGoc()
                    + "</b> (Đã khấu trừ: " + df.format(don.thongTinDoiTra.getTongTienTra()) + ")</html>");
        }

        // Chỉ tính lại sau khi đã phục hồi đủ điểm và phiếu đổi trả.
        capNhatTongTien();

        // Khôi phục thẻ Spa Pass hợp lệ từ CSDL (TC12)
        if (don.maTheSpaPass != null && !don.maTheSpaPass.isBlank()) {
            try {
                dao.TheSpaPassDAO theDao = new dao.TheSpaPassDAO();
                entity.TheSpaPass the = theDao.findByMaThe(don.maTheSpaPass.trim());
                if (the != null) {
                    boolean conLuot = the.isKhaDung();
                    boolean dungKhach = (the.getSdtKhachHang() != null && don.sdt != null
                            && don.sdt.trim().equals(the.getSdtKhachHang().trim()));

                    if (conLuot && dungKhach) {
                        this.theSpaPassHienTai = the;
                        if (lblSpaPassStatus != null) {
                            lblSpaPassStatus.setText(the.getTenThe() + ": Còn " + the.getSoBuoiConLai() + "/" + the.getTongSoBuoi() + " buổi");
                        }
                        if (btnDungSpaPass != null) {
                            btnDungSpaPass.setEnabled(the.isKhaDung());
                        }
                    } else {
                        this.theSpaPassHienTai = null;
                        String lyDo = !conLuot ? "thẻ đã hết hạn hoặc hết lượt sử dụng" : "thẻ không thuộc về khách hàng trong đơn tạm";
                        JOptionPane.showMessageDialog(this,
                                "CẢNH BÁO THẺ SPA PASS:\nThẻ Spa Pass [" + don.maTheSpaPass + "] không thể áp dụng do: " + lyDo + ".\n"
                                + "Hệ thống đã gỡ bỏ thẻ khỏi đơn hàng để tránh thanh toán sai lệch.",
                                "Spa Pass không hợp lệ", JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    this.theSpaPassHienTai = null;
                }
            } catch (Exception ex) {
                System.err.println("[BanHangPOSPanel] Lỗi khôi phục thẻ Spa Pass: " + ex.getMessage());
                this.theSpaPassHienTai = null;
            }
        }

        dsDonTam.remove(don);
        luuDsDonTamXuongDisk();
        capNhatGiaoDienDonCho();

        chonTabPOS();

        JOptionPane.showMessageDialog(this,
                "ĐÃ KHÔI PHỤC ĐƠN HÀNG [" + don.maHoaDon + "] THÀNH CÔNG!\nBạn có thể quét thêm món hoặc bấm thanh toán ngay.",
                "Khôi phục thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void chuyenTienDoDichVuTrongGio(int row) {
        if (row < 0 || row >= modelGioHang.getRowCount()) return;
        String ma = (String) modelGioHang.getValueAt(row, 1);
        if (ma == null || !ma.startsWith("DV")) return;

        String currentTienDo = (String) modelGioHang.getValueAt(row, 7);
        int nextIdx = 0;
        for (int i = 0; i < CAC_BUOC_TIEN_DO.length; i++) {
            if (CAC_BUOC_TIEN_DO[i].equalsIgnoreCase(currentTienDo)) {
                nextIdx = (i + 1) % CAC_BUOC_TIEN_DO.length;
                break;
            }
        }
        String nextTienDo = CAC_BUOC_TIEN_DO[nextIdx];
        modelGioHang.setValueAt(nextTienDo, row, 7);

        String tenMon = (String) modelGioHang.getValueAt(row, 2);
        if (nextIdx == 3) {
            JOptionPane.showMessageDialog(this,
                    "Dịch vụ [" + tenMon + "] đã HOÀN TẤT & SẴN SÀNG THANH TOÁN!",
                    "Tiến độ dịch vụ", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton taoButtonTienDo1Cham(DonHangTam don, int serviceIdx, String curTienDo) {
        String bg = "#FEF3C7";
        String fg = "#B45309";
        String nextStep = CAC_BUOC_TIEN_DO[1];

        if (curTienDo.startsWith("2.")) {
            bg = "#DBEAFE";
            fg = "#1D4ED8";
            nextStep = CAC_BUOC_TIEN_DO[2];
        } else if (curTienDo.startsWith("3.")) {
            bg = "#F3E8FF";
            fg = "#7E22CE";
            nextStep = CAC_BUOC_TIEN_DO[3];
        } else if (curTienDo.startsWith("4.")) {
            bg = "#D1FAE5";
            fg = "#047857";
            nextStep = CAC_BUOC_TIEN_DO[0];
        }

        JButton btn = new JButton("<html><b style='color:" + fg + "; font-size:10px;'>" + curTienDo + " ></b></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btn.setBackground(Color.decode(bg));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(Color.decode(fg), 1, true),
                new EmptyBorder(1, 6, 1, 6)
        ));
        btn.setToolTipText("Nhấp 1 chạm để chuyển sang: " + nextStep);

        final String targetNext = nextStep;
        btn.addActionListener(e -> {
            don.dsMon.get(serviceIdx)[7] = targetNext;
            luuDsDonTamXuongDisk();
            capNhatGiaoDienDonCho();
            if (targetNext.startsWith("4.")) {
                JOptionPane.showMessageDialog(this,
                        "Dịch vụ cho bé cưng của khách [" + don.tenKhach + "] (Đơn " + don.maHoaDon + ")\nđã HOÀN TẤT & SẴN SÀNG THANH TOÁN!",
                        "Hoàn tất dịch vụ", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return btn;
    }

    private static class TienDoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            String val = value != null ? value.toString() : "—";

            if (val.startsWith("1.")) {
                lbl.setText("<html><span style='background:#FEF3C7; color:#B45309; font-weight:bold; font-size:10px; padding:2px 6px; border-radius:4px;'>" + val + "</span></html>");
                lbl.setToolTipText("1-Chạm: Nhấp để chuyển sang 'Đang thực hiện'");
            } else if (val.startsWith("2.")) {
                lbl.setText("<html><span style='background:#DBEAFE; color:#1D4ED8; font-weight:bold; font-size:10px; padding:2px 6px; border-radius:4px;'>" + val + "</span></html>");
                lbl.setToolTipText("1-Chạm: Nhấp để chuyển sang 'Đã xong - Chờ đón'");
            } else if (val.startsWith("3.")) {
                lbl.setText("<html><span style='background:#F3E8FF; color:#7E22CE; font-weight:bold; font-size:10px; padding:2px 6px; border-radius:4px;'>" + val + "</span></html>");
                lbl.setToolTipText("1-Chạm: Nhấp để chuyển sang 'Hoàn tất & TT'");
            } else if (val.startsWith("4.")) {
                lbl.setText("<html><span style='background:#D1FAE5; color:#047857; font-weight:bold; font-size:10px; padding:2px 6px; border-radius:4px;'>" + val + "</span></html>");
                lbl.setToolTipText("Dịch vụ đã hoàn tất và sẵn sàng thanh toán");
            } else {
                lbl.setText("<html><span style='color:#94A3B8; font-size:11px;'>—</span></html>");
                lbl.setToolTipText(null);
            }

            if (isSelected) {
                lbl.setBackground(ThemeColors.PRIMARY_SOFT);
            } else {
                lbl.setBackground(Color.WHITE);
            }
            return lbl;
        }
    }

    // ==========================================
    // 3. TAB LỊCH SỬ HÓA ĐƠN ĐÃ THANH TOÁN (JTABLE)
    // ==========================================
    private JPanel taoTabLichSuHoaDon() {
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBackground(ThemeColors.BACKGROUND);
        pnl.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlToolbar.setBackground(ThemeColors.SURFACE);
        pnlToolbar.setBorder(ThemeColors.createCardBorder(10, 14, 10, 14));

        txtTimKiemLichSu = new JTextField(16);
        txtTimKiemLichSu.setPreferredSize(new Dimension(200, 36));
        txtTimKiemLichSu.putClientProperty("JTextField.placeholderText", "Tìm theo mã HĐ, khách, SĐT...");
        txtTimKiemLichSu.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimKiemLichSu.setBorder(ThemeColors.createInputBorder());

        cboLocNgayLichSu = new JComboBox<>(new String[]{"Tất cả lịch sử", "Hôm nay", "Hôm qua", "7 ngày qua"});
        cboLocNgayLichSu.setPreferredSize(new Dimension(140, 36));
        cboLocNgayLichSu.addActionListener(e -> capNhatBangLichSu());

        cboLocLoaiLichSu = new JComboBox<>(new String[]{"Tất cả loại đơn", "Đơn bán lẻ (Hàng hóa)", "Đơn dịch vụ Spa"});
        cboLocLoaiLichSu.setPreferredSize(new Dimension(170, 36));
        cboLocLoaiLichSu.addActionListener(e -> capNhatBangLichSu());

        JButton btnXemChiTiet = ThemeColors.createSecondaryButton("Xem chi tiết đơn", IconHelper.getIcon(IconHelper.RECEIPT, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnInLai = ThemeColors.createSecondaryButton("In lại hóa đơn", IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 14, ThemeColors.TEXT_SECONDARY));
        JButton btnXuatExcel = ThemeColors.createPrimaryButton("Xuất Excel", IconHelper.getIcon(IconHelper.CHECK, 14, Color.WHITE));

        JButton btnBack = ThemeColors.createSecondaryButton("← Quay lại quầy POS", IconHelper.getIcon(IconHelper.CART, 14, ThemeColors.TEXT_SECONDARY));
        btnBack.setFocusPainted(false);
        btnBack.addActionListener(e -> chonTabPOS());

        btnXemChiTiet.addActionListener(e -> xuLyXemChiTietLichSu());
        btnInLai.addActionListener(e -> xuLyInLaiHoaDon());
        btnXuatExcel.addActionListener(e -> xuLyXuatExcel());

        pnlToolbar.add(btnBack);
        pnlToolbar.add(Box.createHorizontalStrut(6));
        pnlToolbar.add(new JLabel("Tìm kiếm:"));
        pnlToolbar.add(txtTimKiemLichSu);
        pnlToolbar.add(new JLabel("Khoảng ngày:"));
        pnlToolbar.add(cboLocNgayLichSu);
        pnlToolbar.add(new JLabel("Loại đơn:"));
        pnlToolbar.add(cboLocLoaiLichSu);
        pnlToolbar.add(btnXemChiTiet);
        pnlToolbar.add(btnInLai);
        pnlToolbar.add(btnXuatExcel);

        String[] cols = {"Mã HĐ", "Thời gian thanh toán", "Khách hàng", "Số điện thoại", "Thu ngân lập", "Tiền hàng", "Giảm VIP", "Tổng thanh toán", "Phương thức", "Trạng thái"};
        modelLichSu = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblLichSu = new JTable(modelLichSu);
        tblLichSu.setRowHeight(34);
        tblLichSu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLichSu.setShowHorizontalLines(true);
        tblLichSu.setShowVerticalLines(false);
        tblLichSu.setGridColor(ThemeColors.BORDER_LIGHT);
        tblLichSu.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblLichSu.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tblLichSu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLichSu.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblLichSu.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tblLichSu.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblLichSu.getColumnModel().getColumn(0).setPreferredWidth(85);
        tblLichSu.getColumnModel().getColumn(0).setCellRenderer(center);
        tblLichSu.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(ThemeColors.SUCCESS);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
        });

        sorterLichSu = new TableRowSorter<>(modelLichSu);
        tblLichSu.setRowSorter(sorterLichSu);

        tblLichSu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblLichSu.getSelectedRow() >= 0) {
                    xuLyXemChiTietLichSu();
                }
            }
        });

        txtTimKiemLichSu.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { locLichSu(txtTimKiemLichSu.getText().trim()); }
            @Override
            public void removeUpdate(DocumentEvent e) { locLichSu(txtTimKiemLichSu.getText().trim()); }
            @Override
            public void changedUpdate(DocumentEvent e) { locLichSu(txtTimKiemLichSu.getText().trim()); }
        });

        capNhatBangLichSu();

        pnl.add(pnlToolbar, BorderLayout.NORTH);
        pnl.add(new JScrollPane(tblLichSu), BorderLayout.CENTER);
        return pnl;
    }

    private void capNhatBangLichSu() {
        if (modelLichSu == null) return;
        modelLichSu.setRowCount(0);
        dsLichSuHienThi.clear();

        String locNgay = cboLocNgayLichSu != null && cboLocNgayLichSu.getSelectedItem() != null
                ? cboLocNgayLichSu.getSelectedItem().toString() : "Tất cả lịch sử";
        String locLoai = cboLocLoaiLichSu != null && cboLocLoaiLichSu.getSelectedItem() != null
                ? cboLocLoaiLichSu.getSelectedItem().toString() : "Tất cả loại đơn";

        java.time.LocalDate today = java.time.LocalDate.now();
        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            boolean matchNgay = true;
            if (!"Tất cả lịch sử".equals(locNgay)) {
                java.time.LocalDate hdDate = null;
                if (hd.thoiGian != null && hd.thoiGian.length() >= 10) {
                    try {
                        String s = hd.thoiGian.trim();
                        String datePart = s.contains(" - ") ? s.substring(s.lastIndexOf(" - ") + 3).trim() : s;
                        if (datePart.length() >= 10) {
                            hdDate = java.time.LocalDate.parse(datePart.substring(0, 10), dFmt);
                        }
                    } catch (Exception ignored) {}
                }
                if (hdDate == null) {
                    matchNgay = false;
                } else if ("Hôm nay".equals(locNgay)) {
                    matchNgay = hdDate.equals(today);
                } else if ("Hôm qua".equals(locNgay)) {
                    matchNgay = hdDate.equals(today.minusDays(1));
                } else if ("7 ngày qua".equals(locNgay)) {
                    matchNgay = !hdDate.isBefore(today.minusDays(6)) && !hdDate.isAfter(today);
                }
            }
            if (!matchNgay) continue;

            boolean matchLoai = true;
            if ("Đơn bán lẻ (Hàng hóa)".equals(locLoai)) {
                matchLoai = coSanPhamBanLe(hd);
            } else if ("Đơn dịch vụ Spa".equals(locLoai)) {
                matchLoai = coDichVuSpa(hd);
            }
            if (!matchLoai) continue;

            dsLichSuHienThi.add(hd);
            modelLichSu.addRow(new Object[]{
                    hd.maHoaDon,
                    hd.thoiGian,
                    hd.tenKhach,
                    hd.sdt,
                    hd.thuNgan,
                    df.format(hd.tongTienHang),
                    df.format(hd.giamGia),
                    df.format(hd.tongThanhToan),
                    hd.phuongThuc,
                    hd.trangThai
            });
        }
    }

    private void locLichSu(String kw) {
        if (kw.isEmpty()) {
            sorterLichSu.setRowFilter(null);
        } else {
            sorterLichSu.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(kw)));
        }
    }

    private void xuLyXemChiTietLichSu() {
        int row = tblLichSu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn từ bảng lịch sử để xem mẫu phiếu hóa đơn!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = tblLichSu.convertRowIndexToModel(row);
        if (modelRow < 0 || modelRow >= dsLichSuHienThi.size()) return;
        HoaDonDaThanhToan hd = dsLichSuHienThi.get(modelRow);

        int diemCong = (int) (hd.tongThanhToan / 100000L);
        int tongDiem = diemCong;
        if (hd.sdt != null && !hd.sdt.trim().isEmpty()) {
            KhachHang kh = controller.timKhachHang(hd.sdt.trim());
            if (kh != null) {
                tongDiem = kh.getDiemTichLuy();
            }
        }

        HoaDonPreviewDialog.hienThiHoaDon(this, hd, hd.tongThanhToan, 0, diemCong, tongDiem);
    }

    private void xuLyInLaiHoaDon() {
        int row = tblLichSu.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn cần xem và in lại!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        xuLyXemChiTietLichSu();
    }

    private void xuLyXuatExcel() {
        if (dsLichSuHoaDon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu hóa đơn để xuất Excel!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            java.util.List<java.util.List<String>> rows = new ArrayList<>();
            rows.add(java.util.Arrays.asList(
                    "Mã hóa đơn", "Thời gian", "Tên khách hàng", "Số điện thoại",
                    "Thu ngân", "Tổng tiền hàng", "Giảm giá", "Thực thu", "Phương thức", "Trạng thái"
            ));

            for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
                rows.add(java.util.Arrays.asList(
                        hd.maHoaDon != null ? hd.maHoaDon : "",
                        hd.thoiGian != null ? hd.thoiGian : "",
                        hd.tenKhach != null ? hd.tenKhach : "",
                        hd.sdt != null ? hd.sdt : "",
                        hd.thuNgan != null ? hd.thuNgan : "",
                        String.valueOf(hd.tongTienHang),
                        String.valueOf(hd.giamGia),
                        String.valueOf(hd.tongThanhToan),
                        hd.phuongThuc != null ? hd.phuongThuc : "",
                        hd.trangThai != null ? hd.trangThai : ""
                ));
            }

            String fileName = "BaoCaoDoanhThu_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + System.currentTimeMillis() + ".xlsx";
            java.nio.file.Path outputPath = java.nio.file.Paths.get("export", fileName);
            utils.LichPhanCaExcelExporter.export(outputPath, "Doanh Thu POS", rows);

            JOptionPane.showMessageDialog(this,
                    "XUẤT BÁO CÁO DOANH THU EXCEL THÀNH CÔNG!\n\n" +
                            "• Tổng số hóa đơn: " + dsLichSuHoaDon.size() + " đơn\n" +
                            "• Tệp đã lưu tại: " + outputPath.toAbsolutePath().toString(),
                    "Xuất Excel thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.err.println("[BanHangPOSPanel] Lỗi xuất Excel: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Không thể xuất file Excel: " + e.getMessage(), "Lỗi xuất file", JOptionPane.ERROR_MESSAGE);
        }
    }
}
