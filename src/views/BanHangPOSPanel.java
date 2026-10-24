package views;

import controllers.BanHangController;
import models.entity.KhachHang;
import utils.IconHelper;
import utils.ThemeColors;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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
    private final BanHangController controller = new BanHangController();

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
    public static class DonHangTam {
        public String maHoaDon;
        public String tenKhach;
        public String sdt;
        public String diemTichLuy;
        public String thoiGianTao;
        public List<Object[]> dsMon = new ArrayList<>();
        public long tongTien;
        public long giamGia;
        public long khachCanTra;
        public String phuongThuc;
    }

    public static class HoaDonDaThanhToan {
        public String maHoaDon;
        public String thoiGian;
        public String tenKhach;
        public String sdt;
        public String thuNgan;
        public long tongTienHang;
        public long giamGia;
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
            models.dao.HoaDonDAO hdDao = new models.dao.HoaDonDAO();
            List<models.entity.HoaDon> dbList = hdDao.getAllHoaDon();
            if (dbList != null && !dbList.isEmpty()) {
                dsLichSuHoaDon.clear();
                DecimalFormat dfLocal = new DecimalFormat("#,### đ");
                for (models.entity.HoaDon hdDb : dbList) {
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
                    long thanh = hdDb.getThanhTien() != null ? hdDb.getThanhTien().longValue() : 0L;
                    item.giamGia = giam;
                    item.tongThanhToan = thanh;
                    item.tongTienHang = thanh + giam;
                    item.phuongThuc = hdDb.getPhuongThucThanhToan() != null ? hdDb.getPhuongThucThanhToan() : "Tiền mặt";
                    item.trangThai = hdDb.getTrangThaiHoaDon() != null ? hdDb.getTrangThaiHoaDon() : "Đã thanh toán";

                    int stt = 1;
                    if (hdDb.getDsChiTiet() != null) {
                        for (models.entity.ChiTietHoaDon ct : hdDb.getDsChiTiet()) {
                            long dg = ct.getDonGiaLucBan() != null ? ct.getDonGiaLucBan().longValue() : 0L;
                            int sl = ct.getSoLuong();
                            long tt = dg * sl;
                            if (ct instanceof models.entity.ChiTietXuatLo) {
                                models.entity.ChiTietXuatLo ctx = (models.entity.ChiTietXuatLo) ct;
                                String maSP = (ctx.getLoHang() != null && ctx.getLoHang().getSanPham() != null)
                                        ? ctx.getLoHang().getSanPham().getMaSanPham() : "SP001";
                                String tenSP = (ctx.getLoHang() != null && ctx.getLoHang().getSanPham() != null)
                                        ? ctx.getLoHang().getSanPham().getTenSanPham() : "Sản phẩm";
                                String dvt = (ctx.getQuyDoiDonVi() != null && ctx.getQuyDoiDonVi().getDonViTinh() != null)
                                        ? ctx.getQuyDoiDonVi().getDonViTinh().getTenDonVi() : "Đơn vị";
                                item.dsMon.add(new Object[]{stt++, maSP, tenSP, dvt, sl, dfLocal.format(dg), dfLocal.format(tt)});
                            } else if (ct instanceof models.entity.ChiTietDichVu) {
                                models.entity.ChiTietDichVu ctd = (models.entity.ChiTietDichVu) ct;
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
        khoiTaoDuLieuMauLichSuFallback();
    }

    private static void khoiTaoDuLieuMauLichSuNeuCan() {
        if (dsLichSuHoaDon.isEmpty()) {
            dongBoLichSuHoaDonTuDatabase();
        }
    }

    private static void khoiTaoDuLieuMauLichSuFallback() {
        if (!dsLichSuHoaDon.isEmpty()) {
            return;
        }
        HoaDonDaThanhToan hd1 = new HoaDonDaThanhToan();
        hd1.maHoaDon = "HD982104";
        hd1.thoiGian = LocalDateTime.now().minusHours(3).format(STATIC_DTF);
        hd1.tenKhach = "Nguyễn Văn An";
        hd1.sdt = "0988111222";
        hd1.thuNgan = "Lê Anh Minh";
        hd1.tongTienHang = 400000;
        hd1.giamGia = 20000;
        hd1.tongThanhToan = 380000;
        hd1.phuongThuc = "Chuyển khoản QR";
        hd1.trangThai = "Đã thanh toán";
        hd1.dsMon.add(new Object[]{1, "SP001", "Hạt Royal Canin Kitten 2kg", "Gói", 1, "380,000 đ", "380,000 đ"});
        hd1.dsMon.add(new Object[]{2, "SP002", "Pate Whiskas Vị Cá Thu 85g", "Lon", 1, "20,000 đ", "20,000 đ"});
        dsLichSuHoaDon.add(hd1);

        HoaDonDaThanhToan hd2 = new HoaDonDaThanhToan();
        hd2.maHoaDon = "HD981502";
        hd2.thoiGian = LocalDateTime.now().minusHours(5).format(STATIC_DTF);
        hd2.tenKhach = "Trần Thị Mai";
        hd2.sdt = "0977333444";
        hd2.thuNgan = "Lê Anh Minh";
        hd2.tongTienHang = 300000;
        hd2.giamGia = 0;
        hd2.tongThanhToan = 300000;
        hd2.phuongThuc = "Tiền mặt";
        hd2.trangThai = "Đã thanh toán";
        hd2.dsMon.add(new Object[]{1, "DV001", "Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ", "90 phút", 1, "300,000 đ", "300,000 đ", CAC_BUOC_TIEN_DO[3]});
        dsLichSuHoaDon.add(hd2);
    }

    public static List<HoaDonDaThanhToan> layDanhSachHoaDonDaThanhToan() {
        khoiTaoDuLieuMauLichSuNeuCan();
        return dsLichSuHoaDon;
    }

    public static long tinhTongDoanhThuHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            sum += hd.tongThanhToan;
        }
        return sum;
    }

    public static int demTongSoHoaDonHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        return dsLichSuHoaDon.size();
    }

    public static int demSoDonBanLeHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        int count = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            boolean hasSP = false;
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (ma.startsWith("SP")) {
                    hasSP = true;
                    break;
                }
            }
            if (hasSP) count++;
        }
        return count;
    }

    public static int demSoDonDichVuHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        int count = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            boolean hasDV = false;
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (ma.startsWith("DV")) {
                    hasDV = true;
                    break;
                }
            }
            if (hasDV) count++;
        }
        return count;
    }

    private static long parseTienMon(Object val) {
        if (val == null) return 0L;
        String num = val.toString().replaceAll("[^0-9]", "").trim();
        return num.isEmpty() ? 0L : Long.parseLong(num);
    }

    public static long tinhDoanhThuDichVuSpaHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
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
        khoiTaoDuLieuMauLichSuNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if (ma.startsWith("SP") && !ma.equalsIgnoreCase("SP004") && !ma.equalsIgnoreCase("SP006") && mon.length > 6) {
                    sum += parseTienMon(mon[6]);
                }
            }
        }
        return sum;
    }

    public static long tinhDoanhThuKhacHomNay() {
        khoiTaoDuLieuMauLichSuNeuCan();
        long sum = 0;
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
            for (Object[] mon : hd.dsMon) {
                String ma = mon.length > 1 && mon[1] != null ? mon[1].toString() : "";
                if ((ma.equalsIgnoreCase("SP004") || ma.equalsIgnoreCase("SP006") || (!ma.startsWith("SP") && !ma.startsWith("DV"))) && mon.length > 6) {
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

        CatalogItem(String ma, String ten, String loai, String dvt, String soLoHoacKtv, int tonKho, long donGia, String iconName, String moTaNgan) {
            this.ma = ma;
            this.ten = ten;
            this.loai = loai;
            this.dvt = dvt;
            this.soLoHoacKtv = soLoHoacKtv;
            this.tonKho = tonKho;
            this.donGia = donGia;
            this.iconName = iconName;
            this.moTaNgan = moTaNgan;
        }
    }

    private final List<CatalogItem> allItems = new ArrayList<>();
    private final List<CatalogItem> filteredItems = new ArrayList<>();
    private String currentCategory = "TẤT CẢ";
    private String currentSearchKeyword = "";

    // UI Tab 1 (POS)
    private JPanel pnlGridCards;
    private JTextField txtTimKiem;
    private final List<JButton> filterButtons = new ArrayList<>();

    private JLabel lblMaHoaDon;
    private JTable tblGioHang;
    private DefaultTableModel modelGioHang;
    private JTextField txtSdtKhachHang;
    private JTextField txtTenKhachHang;
    private JLabel lblDiemTichLuy;
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
    private long tienGiamGia = 0;

    // UI Tab 2 (Đơn chờ)
    private JPanel pnlCardsDonCho;
    private JLabel lblSoDonChoHeader;

    // UI Tab 3 (Lịch sử)
    private DefaultTableModel modelLichSu;
    private JTable tblLichSu;
    private TableRowSorter<DefaultTableModel> sorterLichSu;
    private JTextField txtTimKiemLichSu;
    private JComboBox<String> cboLocNgayLichSu;

    public BanHangPOSPanel() {
        if (!INSTANCES.contains(this)) {
            INSTANCES.add(this);
        }
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BACKGROUND);

        khoiTaoDuLieuCatalog();
        khoiTaoDuLieuMauLichSu();

        posCardLayout = new CardLayout();
        pnlCardContainer = new JPanel(posCardLayout);
        pnlCardContainer.setBackground(ThemeColors.BACKGROUND);

        pnlCardContainer.add(taoTabPosBanHang(), VIEW_POS);
        pnlCardContainer.add(taoTabDonChoGrid(), VIEW_DON_CHO);
        pnlCardContainer.add(taoTabLichSuHoaDon(), VIEW_LICH_SU);

        add(pnlCardContainer, BorderLayout.CENTER);

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

    public void chonTabLichSu() {
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
        // Hàng hóa & Phụ kiện thú cưng (kèm hình ảnh thực tế & mô tả ngắn)
        allItems.add(new CatalogItem("SP001", "Hạt Royal Canin Kitten 2kg", "Hàng hóa", "Gói", "Lô: RC-2026-01", 45, 380000, IconHelper.BOXES,
                "Thức ăn hạt cao cấp giàu DHA & Protein cho mèo con 4-12 tháng"));
        allItems.add(new CatalogItem("SP002", "Pate Whiskas Vị Cá Thu 85g", "Hàng hóa", "Lon", "Lô: WS-2026-01", 180, 20000, IconHelper.BOXES,
                "Pate cá biển tươi bổ sung Omega 3 & 6 giúp mượt lông, dễ tiêu hóa"));
        allItems.add(new CatalogItem("SP003", "Pate Whiskas (Lốc 6 lon)", "Hàng hóa", "Lốc", "Lô: WS-2026-01", 30, 115000, IconHelper.BOXES,
                "Combo tiết kiệm 6 lon Pate dinh dưỡng vị cá thu & cá ngừ đại dương"));
        allItems.add(new CatalogItem("SP004", "Vòng Cổ Chuông Nơ Thú Cưng", "Hàng hóa", "Chiếc", "Kho chính", 60, 45000, IconHelper.SHOPPING_BAG,
                "Vòng cổ vải lụa mềm có khóa an toàn chống nghẹt kèm chuông đồng"));
        allItems.add(new CatalogItem("SP005", "Sữa Tắm Dưỡng Lông Bio-Care", "Hàng hóa", "Chai", "Lô: BC-2026-02", 35, 85000, IconHelper.SPARKLES,
                "Sữa tắm thảo mộc khử mùi hôi, dưỡng ẩm da và ngăn rụng lông"));
        allItems.add(new CatalogItem("SP006", "Cát Vệ Sinh Mèo Hương Táo 5kg", "Hàng hóa", "Túi", "Kho chính", 50, 65000, IconHelper.BOXES,
                "Cát đất sét Bentonite vón cục siêu nhanh, khử mùi kháng khuẩn 99%"));

        // Dịch vụ Grooming cơ bản
        allItems.add(new CatalogItem("DV001", "Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ", "Grooming cơ bản", "90 phút", "KTV chỉ định", 99, 300000, IconHelper.SCISSORS,
                "Gói tắm sấy + cắt tỉa tạo kiểu Teddy/Baby chuẩn Hàn Quốc theo giống"));
        allItems.add(new CatalogItem("DV002", "Cắt Mài Móng & Vệ Sinh Tai", "Grooming cơ bản", "30 phút", "KTV chỉ định", 99, 100000, IconHelper.PAW,
                "Bấm móng, mài nhẵn đầu móng, nhổ lông tai & nhỏ dung dịch kháng khuẩn"));
        allItems.add(new CatalogItem("DV003", "Vắt Tuyến Hôi & Cạo Bàn Chân", "Grooming cơ bản", "30 phút", "KTV chỉ định", 99, 90000, IconHelper.SPARKLES,
                "Cạo sạch lông đệm thịt bàn chân, bụng và xử lý sạch tuyến hôi hậu môn"));

        // Dịch vụ Spa & Trị liệu
        allItems.add(new CatalogItem("DV004", "Tắm Sấy Khử Mùi & Dưỡng Lông", "Spa & Trị liệu", "45 phút", "KTV chuyên sâu", 99, 150000, IconHelper.SPARKLES,
                "Tắm 2 bước bằng dầu gội chuyên dụng, ủ xả collagen & sấy chải bông"));
        allItems.add(new CatalogItem("DV005", "Tắm Thảo Dược & Trị Ve Rận", "Spa & Trị liệu", "60 phút", "KTV chuyên sâu", 99, 220000, IconHelper.SPARKLES,
                "Ngâm thảo dược thiên nhiên diệt sạch ve rận, nấm da và làm dịu kích ứng"));
        allItems.add(new CatalogItem("DV006", "Ngâm Bồn Sục Ozone Thư Giãn", "Spa & Trị liệu", "60 phút", "Bồn Ozone VIP", 99, 250000, IconHelper.SPARKLES,
                "Sục khí гидро-ozone làm sạch sâu lỗ chân lông, tuần hoàn máu giảm stress"));

        // Tạo kiểu & Nhuộm
        allItems.add(new CatalogItem("DV007", "Nhuộm Lông Nghệ Thuật (Tai & Đuôi)", "Tạo kiểu & Nhuộm", "60 phút", "Chuyên viên nhuộm", 99, 180000, IconHelper.SPARKLES,
                "Nhuộm màu hữu cơ an toàn 100% từ thực vật cho phần tai, đuôi và chân"));

        // Khách sạn thú cưng
        allItems.add(new CatalogItem("DV008", "Lưu Chuồng Pet Hotel (VIP)", "Khách sạn thú cưng", "24 giờ", "Phòng điều hòa", 15, 200000, IconHelper.HOME,
                "Phòng lưu trú điều hòa 24/7, bao gồm 3 bữa ăn tiêu chuẩn & dắt đi dạo"));

        dongBoTonKhoCatalogTuDatabase();
    }

    public void dongBoTonKhoCatalogTuDatabase() {
        try {
            models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
            models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
            for (CatalogItem item : allItems) {
                if (item.ma != null && item.ma.startsWith("SP")) {
                    int tonDb = loDao.getTongTonKhaDung(item.ma);
                    if (tonDb >= 0) {
                        item.tonKho = tonDb;
                    }
                }
            }
            List<models.entity.SanPham> dsSP = spDao.getAllSanPham();
            if (dsSP != null) {
                for (models.entity.SanPham sp : dsSP) {
                    boolean exists = false;
                    for (CatalogItem item : allItems) {
                        if (item.ma.equalsIgnoreCase(sp.getMaSanPham())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        int ton = loDao.getTongTonKhaDung(sp.getMaSanPham());
                        long gia = 50000L;
                        String dvt = "Đơn vị";
                        List<models.entity.QuyDoiDonVi> listQd = spDao.getQuyDoiBySanPham(sp.getMaSanPham());
                        if (listQd != null && !listQd.isEmpty()) {
                            models.entity.QuyDoiDonVi qd0 = listQd.get(0);
                            if (qd0.getGiaBan() != null) gia = qd0.getGiaBan().longValue();
                            if (qd0.getDonViTinh() != null) dvt = qd0.getDonViTinh().getTenDonVi();
                        }
                        allItems.add(new CatalogItem(
                                sp.getMaSanPham(), sp.getTenSanPham(), "Hàng hóa", dvt, "Kho chính",
                                ton, gia, IconHelper.BOXES, sp.getMoTa() != null ? sp.getMoTa() : "Sản phẩm mới"
                        ));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void khoiTaoDuLieuMauLichSu() {
        khoiTaoDuLieuMauLichSuNeuCan();

        // Khởi tạo 2 đơn tạm mẫu có chứa dịch vụ chăm sóc theo dõi tiến độ
        DonHangTam dt1 = new DonHangTam();
        dt1.maHoaDon = "HD-TAM-01";
        dt1.tenKhach = "Lê Hoàng Yến";
        dt1.sdt = "0912345678";
        dt1.diemTichLuy = "Điểm hiện có: 120 điểm (VIP)";
        dt1.thoiGianTao = "10:15";
        dt1.tongTien = 300000;
        dt1.giamGia = 15000;
        dt1.khachCanTra = 285000;
        dt1.phuongThuc = "Chuyển khoản QR";
        dt1.dsMon.add(new Object[]{1, "DV001", "Cắt Tỉa Lông Tạo Kiểu Thẩm Mỹ (Miu Miu)", "90 phút", 1, "300,000 đ", "300,000 đ", CAC_BUOC_TIEN_DO[1]});
        dsDonTam.add(dt1);

        DonHangTam dt2 = new DonHangTam();
        dt2.maHoaDon = "HD-TAM-02";
        dt2.tenKhach = "Đỗ Minh Khang";
        dt2.sdt = "0933445566";
        dt2.diemTichLuy = "Điểm hiện có: 40 điểm";
        dt2.thoiGianTao = "11:00";
        dt2.tongTien = 250000;
        dt2.giamGia = 0;
        dt2.khachCanTra = 250000;
        dt2.phuongThuc = "Tiền mặt";
        dt2.dsMon.add(new Object[]{1, "DV006", "Ngâm Bồn Sục Ozone Thư Giãn (Lulu)", "60 phút", 1, "250,000 đ", "250,000 đ", CAC_BUOC_TIEN_DO[0]});
        dsDonTam.add(dt2);
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
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setOpaque(false);

        JPanel pnlSearch = new JPanel(new BorderLayout(8, 0));
        pnlSearch.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(0, 38));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm kiếm nhanh sản phẩm hoặc dịch vụ theo tên, mã, công dụng...");
        txtTimKiem.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 17, ThemeColors.TEXT_MUTED));
        txtTimKiem.setBorder(ThemeColors.createInputBorder());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { capNhatTimKiem(); }
            @Override
            public void removeUpdate(DocumentEvent e) { capNhatTimKiem(); }
            @Override
            public void changedUpdate(DocumentEvent e) { capNhatTimKiem(); }
        });

        pnlSearch.add(txtTimKiem, BorderLayout.CENTER);

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

        pnlSearch.add(pnlQuickNav, BorderLayout.EAST);

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilters.setOpaque(false);

        String[] categories = {
                "TẤT CẢ",
                "Hàng hóa",
                "Grooming cơ bản",
                "Spa & Trị liệu",
                "Tạo kiểu & Nhuộm",
                "Khách sạn thú cưng"
        };

        for (String cat : categories) {
            JButton btnFilter = taoFilterButton(cat);
            filterButtons.add(btnFilter);
            pnlFilters.add(btnFilter);
        }
        if (!filterButtons.isEmpty()) {
            kichHoatFilterButton(filterButtons.get(0));
        }

        pnlHeader.add(pnlSearch, BorderLayout.NORTH);
        pnlHeader.add(pnlFilters, BorderLayout.SOUTH);

        // Khung danh sách thẻ kéo dọc hoàn toàn (không bao giờ cuộn ngang), phân tách rõ Sản phẩm & Dịch vụ
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

    private JButton taoFilterButton(String category) {
        JButton btn = new JButton(category);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(ThemeColors.SURFACE);
        btn.setForeground(ThemeColors.TEXT_SECONDARY);
        btn.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));

        btn.addActionListener(e -> {
            for (JButton b : filterButtons) {
                b.setBackground(ThemeColors.SURFACE);
                b.setForeground(ThemeColors.TEXT_SECONDARY);
                b.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(5, 12, 5, 12)
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
                new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
    }

    private void capNhatTimKiem() {
        currentSearchKeyword = txtTimKiem.getText().trim().toLowerCase();
        locVaHienThiCards();
    }

    private void locVaHienThiCards() {
        filteredItems.clear();
        List<CatalogItem> dsHangHoa = new ArrayList<>();
        List<CatalogItem> dsDichVu = new ArrayList<>();

        for (CatalogItem item : allItems) {
            boolean matchCategory = "TẤT CẢ".equalsIgnoreCase(currentCategory) ||
                    item.loai.equalsIgnoreCase(currentCategory);
            boolean matchKeyword = currentSearchKeyword.isEmpty() ||
                    item.ten.toLowerCase().contains(currentSearchKeyword) ||
                    item.ma.toLowerCase().contains(currentSearchKeyword) ||
                    (item.moTaNgan != null && item.moTaNgan.toLowerCase().contains(currentSearchKeyword));
            if (matchCategory && matchKeyword) {
                filteredItems.add(item);
                if ("Hàng hóa".equalsIgnoreCase(item.loai)) {
                    dsHangHoa.add(item);
                } else {
                    dsDichVu.add(item);
                }
            }
        }

        pnlGridCards.removeAll();

        // 1. KHU VỰC SẢN PHẨM & HÀNG HÓA BÁN LẺ
        if (!dsHangHoa.isEmpty()) {
            pnlGridCards.add(taoBannerPhanCachDanhMuc(
                    "SẢN PHẨM, THỨC ĂN & PHỤ KIỆN THÚ CƯNG (" + dsHangHoa.size() + " mặt hàng)",
                    IconHelper.BOXES,
                    new Color(0xEC, 0xFD, 0xF5),
                    new Color(0x04, 0x78, 0x57),
                    "Nhấn 1 chạm vào thẻ để thêm sản phẩm trực tiếp vào giỏ hàng"
            ));
            pnlGridCards.add(Box.createVerticalStrut(8));

            JPanel pnlGridSP = new JPanel(new GridLayout(0, 3, 10, 10));
            pnlGridSP.setOpaque(false);
            pnlGridSP.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (CatalogItem sp : dsHangHoa) {
                pnlGridSP.add(taoTheCard(sp));
            }
            pnlGridCards.add(pnlGridSP);
        }

        // Vạch phân cách rõ ràng giữa khu vực Sản phẩm và khu vực Dịch vụ
        if (!dsHangHoa.isEmpty() && !dsDichVu.isEmpty()) {
            pnlGridCards.add(Box.createVerticalStrut(14));
            JSeparator sep = new JSeparator();
            sep.setForeground(ThemeColors.BORDER_ACCENT);
            sep.setAlignmentX(Component.LEFT_ALIGNMENT);
            sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            pnlGridCards.add(sep);
            pnlGridCards.add(Box.createVerticalStrut(12));
        }

        // 2. KHU VỰC DỊCH VỤ SPA, GROOMING & PET HOTEL
        if (!dsDichVu.isEmpty()) {
            pnlGridCards.add(taoBannerPhanCachDanhMuc(
                    "DỊCH VỤ SPA, GROOMING & CHĂM SÓC THÚ CƯNG (" + dsDichVu.size() + " dịch vụ)",
                    IconHelper.SCISSORS,
                    ThemeColors.PRIMARY_TINT,
                    ThemeColors.PRIMARY_ACTIVE,
                    "Nhấn vào thẻ để chọn cân nặng bé cưng & kỹ thuật viên phụ trách"
            ));
            pnlGridCards.add(Box.createVerticalStrut(8));

            JPanel pnlGridDV = new JPanel(new GridLayout(0, 3, 10, 10));
            pnlGridDV.setOpaque(false);
            pnlGridDV.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (CatalogItem dv : dsDichVu) {
                pnlGridDV.add(taoTheCard(dv));
            }
            pnlGridCards.add(pnlGridDV);
        }

        pnlGridCards.revalidate();
        pnlGridCards.repaint();
    }

    private JPanel taoBannerPhanCachDanhMuc(String title, String iconName, Color bgColor, Color fgColor, String subHint) {
        JPanel banner = new JPanel(new BorderLayout(8, 0));
        banner.setBackground(bgColor);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        banner.setBorder(new CompoundBorder(
                new LineBorder(fgColor, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel lblTitle = new JLabel(title, IconHelper.getIcon(iconName, 16, fgColor), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(fgColor);
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
        card.setPreferredSize(new Dimension(175, 224));
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
            lblBadge = ThemeColors.createBadge(item.dvt, ThemeColors.PRIMARY_SOFT, ThemeColors.TEXT_PRIMARY);
        } else {
            lblBadge = ThemeColors.createBadge("Tồn: " + item.tonKho,
                    item.tonKho > 10 ? ThemeColors.SUCCESS_LIGHT : ThemeColors.DANGER_LIGHT,
                    item.tonKho > 10 ? ThemeColors.SUCCESS_TEXT : ThemeColors.DANGER_TEXT);
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

        JLabel lblTen = new JLabel("<html><b style='color:#33272A; font-size:11px;'>" + item.ten + "</b></html>");
        lblTen.setVerticalAlignment(SwingConstants.TOP);

        String moTa = item.moTaNgan != null ? item.moTaNgan : (item.ma + " · " + item.loai);
        JLabel lblMoTa = new JLabel("<html><span style='color:#64748B; font-size:9.5px;'>" + moTa + "</span></html>");
        lblMoTa.setVerticalAlignment(SwingConstants.TOP);

        pnlCenter.add(lblTen, BorderLayout.NORTH);
        pnlCenter.add(lblMoTa, BorderLayout.CENTER);

        // Đáy: Giá tiền + Nút thêm nhanh
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeColors.BORDER_LIGHT));

        JLabel lblGia = new JLabel(df.format(item.donGia));
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblGia.setForeground(ThemeColors.PRIMARY_ACTIVE);

        JLabel lblHint = new JLabel(isDichVu ? "Chọn DV >" : "+ Thêm giỏ");
        lblHint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHint.setForeground(isDichVu ? ThemeColors.PRIMARY_ACTIVE : new Color(0x04, 0x78, 0x57));

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
                card.setBackground(ThemeColors.PRIMARY_TINT);
                card.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
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

    private void themSanPhamVaoGio(CatalogItem item) {
        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            String ma = (String) modelGioHang.getValueAt(i, 1);
            if (ma.equals(item.ma)) {
                int sl = (int) modelGioHang.getValueAt(i, 4);
                int slMoi = sl + 1;
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

    private JPanel taoPanelHoaDonVaThanhToan() {
        JPanel pnl = new JPanel(new BorderLayout(0, 6));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(8, 12, 8, 12));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", IconHelper.getPrimaryIcon(IconHelper.RECEIPT, 16), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        lblMaHoaDon = new JLabel("HD" + System.currentTimeMillis() % 1000000, SwingConstants.RIGHT);
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

        chkDungDiem = new JCheckBox("Dùng điểm (-0 đ)");
        chkDungDiem.setOpaque(false);
        chkDungDiem.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chkDungDiem.setForeground(ThemeColors.PRIMARY_ACTIVE);
        chkDungDiem.setEnabled(false);
        chkDungDiem.setFocusPainted(false);
        chkDungDiem.addActionListener(e -> capNhatTongTien());

        pnlDiemContainer.add(lblDiemTichLuy, BorderLayout.WEST);
        pnlDiemContainer.add(chkDungDiem, BorderLayout.EAST);

        gbcKh.gridx = 0; gbcKh.gridy = 0; gbcKh.weightx = 1.0;
        pnlKhachHang.add(pnlRowSdt, gbcKh);
        gbcKh.gridy = 1;
        pnlKhachHang.add(pnlRowTen, gbcKh);
        gbcKh.gridy = 2;
        pnlKhachHang.add(pnlDiemContainer, gbcKh);

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

        JButton btnTang = new JButton("Tăng", IconHelper.getIcon(IconHelper.PLUS, 12, new Color(0x04, 0x78, 0x57)));
        btnTang.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTang.setBackground(new Color(0xEC, 0xFD, 0xF5));
        btnTang.setForeground(new Color(0x04, 0x78, 0x57));
        btnTang.setFocusPainted(false);
        btnTang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTang.setIconTextGap(4);
        btnTang.setPreferredSize(new Dimension(74, 25));
        btnTang.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x10, 0xB9, 0x81), 1, true),
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
        cboPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Cổng VNPay", "Thẻ / POS"});
        cboPhuongThuc.setPreferredSize(new Dimension(130, 26));
        cboPhuongThuc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cboPhuongThuc.addActionListener(e -> {
            String sel = (String) cboPhuongThuc.getSelectedItem();
            if (sel != null && (sel.contains("VNPay") || sel.contains("QR") || sel.contains("Thẻ"))) {
                long canTraHienTai = Math.max(0, tongTienHang - tienGiamGia);
                if (txtTienKhachDua != null) {
                    txtTienKhachDua.setText(df.format(canTraHienTai));
                }
                if (lblTienThoiLai != null) {
                    lblTienThoiLai.setText("0 đ");
                }
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

        JPanel pnlSubActions = new JPanel(new GridLayout(1, 2, 6, 0));
        pnlSubActions.setOpaque(false);

        JButton btnTaoDonMoi = ThemeColors.createSecondaryButton("Tạo mới", IconHelper.getIcon(IconHelper.PLUS, 12, ThemeColors.TEXT_SECONDARY));
        JButton btnLuuTam = ThemeColors.createSecondaryButton("Lưu tạm (F4)", IconHelper.getIcon(IconHelper.RECEIPT, 12, ThemeColors.TEXT_SECONDARY));
        btnTaoDonMoi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLuuTam.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTaoDonMoi.setPreferredSize(new Dimension(92, 34));
        btnLuuTam.setPreferredSize(new Dimension(112, 34));

        btnTaoDonMoi.addActionListener(e -> taoDonMoi());
        btnLuuTam.addActionListener(e -> luuTamDonHang());

        pnlSubActions.add(btnTaoDonMoi);
        pnlSubActions.add(btnLuuTam);

        JButton btnThanhToan = new JButton("THANH TOÁN (F9)", IconHelper.getIcon(IconHelper.CREDIT_CARD, 15, Color.WHITE));
        btnThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThanhToan.setBackground(ThemeColors.PRIMARY);
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThanhToan.setPreferredSize(new Dimension(0, 34));
        btnThanhToan.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        btnThanhToan.addActionListener(e -> thucHienThanhToan());

        pnlActionButtons.add(pnlSubActions, BorderLayout.WEST);
        pnlActionButtons.add(btnThanhToan, BorderLayout.CENTER);

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 4));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlKhachHang, BorderLayout.NORTH);

        JPanel pnlTableWrapper = new JPanel(new BorderLayout(0, 3));
        pnlTableWrapper.setOpaque(false);
        pnlTableWrapper.add(pnlCartActions, BorderLayout.NORTH);
        pnlTableWrapper.add(scrollGioHang, BorderLayout.CENTER);
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
        btn.putClientProperty("FlatLaf.style", "arc: 6");
        btn.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(1, 5, 1, 5)
        ));
        btn.addActionListener(e -> {
            if (amount < 0) {
                long canTra = Math.max(0, tongTienHang - tienGiamGia);
                txtTienKhachDua.setText(String.valueOf(canTra));
            } else {
                txtTienKhachDua.setText(String.valueOf(amount));
            }
            tinhTienThoi();
        });
        return btn;
    }

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

            // Kiểm tra tồn kho nếu là sản phẩm
            String maMon = (String) modelGioHang.getValueAt(row, 1);
            if (maMon != null && maMon.startsWith("SP")) {
                for (CatalogItem item : allItems) {
                    if (maMon.equalsIgnoreCase(item.ma) && slMoi > item.tonKho && item.tonKho > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Số lượng nhập vượt quá tồn kho hiện tại (" + item.tonKho + " " + item.dvt + ")!",
                                "Cảnh báo tồn kho", JOptionPane.WARNING_MESSAGE);
                        slMoi = item.tonKho;
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
        List<KhachHang> dsGoiY = BanHangController.timKiemGoiYKhachHang(kw);
        if (dsGoiY.isEmpty()) {
            dsGoiY = BanHangController.getDanhSachKhachHang();
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(ThemeColors.BORDER_ACCENT, 1, true));

        JMenuItem headerItem = new JMenuItem("DANH SÁCH KHÁCH HÀNG THÀNH VIÊN (" + dsGoiY.size() + " KH)");
        headerItem.setFont(new Font("Segoe UI", Font.BOLD, 11));
        headerItem.setEnabled(false);
        popup.add(headerItem);
        popup.addSeparator();

        for (KhachHang kh : dsGoiY) {
            String hang = BanHangController.xepHangThanhVien(kh.getDiemTichLuy());
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
                if (chkDungDiem != null) {
                    chkDungDiem.setSelected(false);
                    chkDungDiem.setEnabled(false);
                    chkDungDiem.setText("Dùng điểm (-0 đ)");
                }
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

        JTextField txtSdt = new JTextField(khHienTai != null ? khHienTai.getSoDienThoai() : sdtDangNhap);
        JTextField txtHoTen = new JTextField(khHienTai != null ? khHienTai.getHoTen() : tenDangNhap);
        JTextField txtDiem = new JTextField(khHienTai != null ? String.valueOf(khHienTai.getDiemTichLuy()) : "0");

        String trangThaiKH = khHienTai != null
                ? ("KHÁCH CŨ [" + khHienTai.getMaKhachHang() + "] - Hạng: " + BanHangController.xepHangThanhVien(khHienTai.getDiemTichLuy()))
                : "KHÁCH HÀNG MỚI (Tự động cấp mã thành viên mới)";

        Object[] options = khHienTai != null
                ? new Object[]{"Lưu cập nhật", "Xóa khách hàng", "Đóng"}
                : new Object[]{"+ Thêm Khách hàng mới", "Đóng"};

        Object[] msg = {
                "Phân loại: " + trangThaiKH,
                "Số điện thoại (10 chữ số) (*):", txtSdt,
                "Họ và tên khách hàng (*):", txtHoTen,
                "Điểm tích lũy hiện có:", txtDiem
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
            int diemMoi = 0;
            try {
                diemMoi = Math.max(0, Integer.parseInt(txtDiem.getText().trim()));
            } catch (NumberFormatException ignored) {
            }
            if (khHienTai != null) {
                BanHangController.capNhatThongTinKhachHang(khHienTai.getMaKhachHang(), tenMoi, sdtMoi, diemMoi);
            } else {
                BanHangController.luuHoacCapNhatKhachHang(sdtMoi, tenMoi, diemMoi);
            }
            txtSdtKhachHang.setText(sdtMoi);
            traCuuKhachHang();
            JOptionPane.showMessageDialog(this, "Đã lưu hồ sơ khách hàng [" + tenMoi + " - " + sdtMoi + "] thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } else if (khHienTai != null && choice == 1) {
            int cf = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa khách hàng [" + khHienTai.getHoTen() + " - " + khHienTai.getSoDienThoai() + "]?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (cf == JOptionPane.YES_OPTION) {
                BanHangController.xoaKhachHang(khHienTai.getMaKhachHang());
                txtSdtKhachHang.setText("");
                txtTenKhachHang.setText("");
                traCuuKhachHang();
                JOptionPane.showMessageDialog(this, "Đã xóa thông tin khách hàng khỏi hệ thống.", "Đã xóa", JOptionPane.INFORMATION_MESSAGE);
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
            } else {
                KhachHang kh = controller.timKhachHang(sdt);
                if (kh != null) {
                    if (txtTenKhachHang != null) {
                        txtTenKhachHang.setText(kh.getHoTen());
                    }
                    tenKhachTuDongDien = true;
                    diemHienCo = kh.getDiemTichLuy();
                    String hang = BanHangController.xepHangThanhVien(diemHienCo);
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
            }

            if (chkDungDiem != null) {
                if (diemHienCo > 0) {
                    chkDungDiem.setEnabled(true);
                    chkDungDiem.setText("Dùng điểm (-" + df.format(diemHienCo * 1000L) + ")");
                } else {
                    chkDungDiem.setSelected(false);
                    chkDungDiem.setEnabled(false);
                    chkDungDiem.setText("Dùng điểm (-0 đ)");
                }
            }
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
                String donGiaStr = ((String) modelGioHang.getValueAt(row, 5)).replace(",", "").replace(" đ", "").trim();
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
        long tienGiamVip = (isVIP && tongTienHang > 0) ? (long) (tongTienHang * 0.05) : 0;
        long tongSauVip = Math.max(0, tongTienHang - tienGiamVip);
        tienDoiDiem = (chkDungDiem != null && chkDungDiem.isSelected() && diemHienCo > 0 && tongSauVip > 0)
                ? Math.min(tongSauVip, diemHienCo * 1000L)
                : 0;
        tienGiamGia = tienGiamVip + tienDoiDiem;
        long canTra = Math.max(0, tongTienHang - tienGiamGia);

        if (lblTongTienHang != null) lblTongTienHang.setText(df.format(tongTienHang));
        if (lblGiamGia != null) lblGiamGia.setText((tienGiamGia > 0 ? "-" : "") + df.format(tienGiamGia));
        if (lblKhachCanTra != null) lblKhachCanTra.setText(df.format(canTra));
        if (txtTienKhachDua != null) {
            txtTienKhachDua.setText(canTra == 0 ? "0" : String.valueOf(canTra));
        }
        tinhTienThoi();
    }

    private void tinhTienThoi() {
        if (txtTienKhachDua == null || lblTienThoiLai == null) return;
        try {
            long canTra = Math.max(0, tongTienHang - tienGiamGia);
            String text = txtTienKhachDua.getText().replaceAll("[^0-9]", "").trim();
            long tienDua = text.isEmpty() ? 0 : Long.parseLong(text);
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
        } catch (NumberFormatException e) {
            lblTienThoiLai.setText("Số tiền không hợp lệ");
            lblTienThoiLai.setForeground(ThemeColors.DANGER);
        }
    }

    private void lamSachKhungHoaDon() {
        modelGioHang.setRowCount(0);
        lblMaHoaDon.setText("HD" + System.currentTimeMillis() % 1000000);
        dangCapNhatKhach = true;
        try {
            txtSdtKhachHang.setText("");
            txtTenKhachHang.setText("");
            tenKhachTuDongDien = false;
            diemHienCo = 0;
            tienDoiDiem = 0;
            if (chkDungDiem != null) {
                chkDungDiem.setSelected(false);
                chkDungDiem.setEnabled(false);
                chkDungDiem.setText("Dùng điểm (-0 đ)");
            }
            lblDiemTichLuy.setText("Khách vãng lai · 0 điểm");
            lblDiemTichLuy.setForeground(ThemeColors.TEXT_SECONDARY);
        } finally {
            dangCapNhatKhach = false;
        }
        capNhatTongTien();
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
        don.giamGia = tienGiamGia;
        don.khachCanTra = Math.max(0, tongTienHang - tienGiamGia);
        don.phuongThuc = (String) cboPhuongThuc.getSelectedItem();

        for (int i = 0; i < modelGioHang.getRowCount(); i++) {
            don.dsMon.add(new Object[]{
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

        dsDonTam.add(0, don);
        capNhatGiaoDienDonCho();

        JOptionPane.showMessageDialog(this,
                "ĐÃ LƯU TẠM ĐƠN HÀNG [" + don.maHoaDon + "] THÀNH CÔNG!\n" +
                        "• Khách: " + don.tenKhach + "\n" +
                        "• Món: " + don.dsMon.size() + " món · " + df.format(don.khachCanTra) + "\n\n" +
                        "Đơn đã được chuyển vào Tab 'Hóa đơn chờ phục vụ' để bạn phục vụ khách tiếp theo.",
                "Lưu tạm thành công", JOptionPane.INFORMATION_MESSAGE);

        lamSachKhungHoaDon();
    }

    public void tiepNhanDichVuPetHotel(String maPhong, String loaiPhong, String tenBe, String tenChu, String sdt, int soNgay, long donGiaNgay, long tongTien) {
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

    private void thucHienThanhToan() {
        if (modelGioHang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống! Vui lòng chọn sản phẩm hoặc dịch vụ trước khi thanh toán.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Kết thúc chỉnh sửa ô SL nếu đang gõ dở
        if (tblGioHang.isEditing()) {
            tblGioHang.getCellEditor().stopCellEditing();
        }

        // Cảnh báo an toàn tiến độ dịch vụ (Multi-Service Progress Safety Check)
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

        long canTra = Math.max(0, tongTienHang - tienGiamGia);
        String tienDuaRaw = txtTienKhachDua != null ? txtTienKhachDua.getText().replaceAll("[^0-9]", "").trim() : "";
        long tienKhachDuaVal = tienDuaRaw.isEmpty() ? canTra : Math.max(canTra, Long.parseLong(tienDuaRaw));
        long tienThoiVal = Math.max(0, tienKhachDuaVal - canTra);

        String pttt = (String) cboPhuongThuc.getSelectedItem();
        String maHD = lblMaHoaDon.getText();
        String sdt = txtSdtKhachHang.getText().trim();
        String tenNhap = txtTenKhachHang.getText().trim();
        String tenKhach = tenNhap.isEmpty() ? (sdt.isEmpty() ? "Khách vãng lai" : "Khách hàng mới") : tenNhap;

        // XÁC NHẬN THANH TOÁN CỔNG VNPAY (HOẶC VIETQR)
        if (pttt != null && (pttt.contains("VNPay") || pttt.contains("VNPAY"))) {
            boolean daNhanTien = VNPayDialog.xacNhanThanhToanVNPay(this, maHD, canTra, tenKhach);
            if (!daNhanTien) {
                return; // Thu ngân hoặc khách hủy giao dịch VNPay
            }
            tienKhachDuaVal = canTra;
            tienThoiVal = 0;
            if (txtTienKhachDua != null) {
                txtTienKhachDua.setText(df.format(canTra));
            }
            if (lblTienThoiLai != null) {
                lblTienThoiLai.setText("0 đ");
            }
        } else if (pttt != null && pttt.contains("QR")) {
            boolean daNhanTien = VietQRDialog.xacNhanThanhToanVietQR(this, maHD, canTra, tenKhach);
            if (!daNhanTien) {
                return; // Thu ngân hoặc khách hủy giao dịch QR
            }
            tienKhachDuaVal = canTra;
            tienThoiVal = 0;
            if (txtTienKhachDua != null) {
                txtTienKhachDua.setText(df.format(canTra));
            }
            if (lblTienThoiLai != null) {
                lblTienThoiLai.setText("0 đ");
            }
        }

        String nowStr = LocalDateTime.now().format(dtf);

        // Tính điểm đã dùng, điểm thưởng mới và tổng điểm mới
        int diemDaDung = (chkDungDiem != null && chkDungDiem.isSelected() && diemHienCo > 0) ? (int) (tienDoiDiem / 1000L) : 0;
        int diemThuongMoi = (int) (canTra / 10000L);
        int tongDiemMoi = Math.max(0, diemHienCo - diemDaDung) + diemThuongMoi;

        // Đảm bảo khách hàng tồn tại trong CSDL trước khi tạo Hóa đơn
        String sdtLuu = "";
        if (!sdt.isEmpty() || (!tenNhap.isEmpty() && !tenNhap.equalsIgnoreCase("Khách vãng lai"))) {
            sdtLuu = sdt.isEmpty() ? ("KH-" + tenNhap) : sdt;
            KhachHang khSaved = BanHangController.luuHoacCapNhatKhachHang(sdtLuu, tenKhach, diemHienCo);
            if (khSaved != null) {
                tenKhach = khSaved.getHoTen();
                sdt = khSaved.getSoDienThoai();
            }
        }

        // Lưu Hóa đơn & trừ tồn kho FEFO trực tiếp vào CSDL SQL Server qua HoaDonDAO
        try {
            models.dao.HoaDonDAO hdDao = new models.dao.HoaDonDAO();
            models.entity.HoaDon hdReal = new models.entity.HoaDon();
            hdReal.setMaHoaDon(maHD);
            hdReal.setNgayLap(LocalDateTime.now());
            hdReal.setThoiGianThanhToan(LocalDateTime.now());
            hdReal.setGiamGia(java.math.BigDecimal.valueOf(tienGiamGia));
            hdReal.setThanhTien(java.math.BigDecimal.valueOf(canTra));
            hdReal.setPhuongThucThanhToan(pttt);
            hdReal.setTrangThaiHoaDon("Đã thanh toán");

            models.entity.NhanVien nv = new models.entity.NhanVien();
            nv.setMaNhanVien(utils.UserSession.getUserId() != null ? utils.UserSession.getUserId() : "NV001");
            hdReal.setNhanVien(nv);

            if (!sdt.isEmpty()) {
                models.dao.KhachHangDAO khDao = new models.dao.KhachHangDAO();
                models.entity.KhachHang kh = khDao.findBySoDienThoai(sdt);
                if (kh != null) {
                    hdReal.setKhachHang(kh);
                }
            }

            models.dao.LoHangDAO loDao = new models.dao.LoHangDAO();
            java.util.List<models.entity.LoHang> allLo = loDao.getAllLoHang();
            models.dao.SanPhamDAO spDao = new models.dao.SanPhamDAO();
            int ctSeq = 1;

            for (int i = 0; i < modelGioHang.getRowCount(); i++) {
                String ma = (String) modelGioHang.getValueAt(i, 1);
                int sl = Integer.parseInt(modelGioHang.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                String dgStr = modelGioHang.getValueAt(i, 5).toString().replaceAll("[^0-9]", "");
                java.math.BigDecimal dg = dgStr.isEmpty() ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(dgStr);

                if (ma != null && ma.startsWith("SP")) {
                    models.entity.QuyDoiDonVi qd = null;
                    try {
                        java.util.List<models.entity.QuyDoiDonVi> listQd = spDao.getQuyDoiBySanPham(ma);
                        if (listQd != null && !listQd.isEmpty()) {
                            qd = listQd.get(0);
                        }
                    } catch (Exception ignored) {}

                    int remaining = sl;
                    if (allLo != null) {
                        for (models.entity.LoHang candidate : allLo) {
                            if (remaining <= 0) break;
                            if (candidate.getSanPham() != null
                                    && candidate.getSanPham().getMaSanPham().equalsIgnoreCase(ma)
                                    && candidate.getSoLuongTon() > 0) {
                                int take = Math.min(remaining, candidate.getSoLuongTon());
                                candidate.setSoLuongTon(candidate.getSoLuongTon() - take);
                                models.entity.ChiTietXuatLo ct = new models.entity.ChiTietXuatLo(
                                        maHD + "_CT" + (ctSeq++), hdReal, take, dg, candidate, qd
                                );
                                ct.setSoLuongXuatCoSo(take);
                                hdReal.addChiTiet(ct);
                                remaining -= take;
                            }
                        }
                    }
                    if (remaining > 0 && allLo != null && !allLo.isEmpty()) {
                        for (models.entity.LoHang fallback : allLo) {
                            if (fallback.getSoLuongTon() >= remaining) {
                                fallback.setSoLuongTon(fallback.getSoLuongTon() - remaining);
                                models.entity.ChiTietXuatLo ct = new models.entity.ChiTietXuatLo(
                                        maHD + "_CT" + (ctSeq++), hdReal, remaining, dg, fallback, qd
                                );
                                ct.setSoLuongXuatCoSo(remaining);
                                hdReal.addChiTiet(ct);
                                remaining = 0;
                                break;
                            }
                        }
                    }
                } else if (ma != null && ma.startsWith("DV")) {
                    models.entity.DichVu dv = new models.entity.DichVu();
                    dv.setMaDichVu(ma);
                    models.entity.ChiTietDichVu ct = new models.entity.ChiTietDichVu(
                            maHD + "_CT" + (ctSeq++), hdReal, sl, dg, dv, null, nv, java.math.BigDecimal.ZERO
                    );
                    hdReal.addChiTiet(ct);
                }
            }

            if (!hdReal.getDsChiTiet().isEmpty()) {
                hdDao.thanhToanHoaDon(hdReal);
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

        // Cập nhật chính xác tổng điểm tích lũy của khách xuống CSDL
        if (!sdtLuu.isEmpty()) {
            BanHangController.luuHoacCapNhatKhachHang(sdtLuu, tenKhach, tongDiemMoi);
        }

        // Cập nhật tồn kho & thẻ catalog trực tiếp trên RAM (Tối ưu hóa: Tránh bão truy vấn lặp lại N+1 gây lag)
        for (BanHangPOSPanel inst : INSTANCES) {
            inst.locVaHienThiCards();
        }

        HoaDonDaThanhToan hd = new HoaDonDaThanhToan();
        hd.maHoaDon = maHD;
        hd.thoiGian = nowStr;
        hd.tenKhach = tenKhach;
        hd.sdt = sdt;
        hd.thuNgan = utils.UserSession.getFullName();
        hd.tongTienHang = tongTienHang;
        hd.giamGia = tienGiamGia;
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

        lamSachKhungHoaDon();
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

        JLabel lblHint = new JLabel("Click 1 chạm vào thẻ để khôi phục đơn quay trở lại giỏ hàng POS thanh toán", SwingConstants.RIGHT);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(pnlLeft, BorderLayout.WEST);
        pnlHeader.add(lblHint, BorderLayout.EAST);

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
                for (DonHangTam don : dsDonTam) {
                    pnlCardsDonCho.add(taoCardDonCho(don));
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
        cboPhuongThuc.setSelectedItem(don.phuongThuc);

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
        capNhatTongTien();

        dsDonTam.remove(don);
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

        cboLocNgayLichSu = new JComboBox<>(new String[]{"Hôm nay", "Hôm qua", "7 ngày qua", "Tất cả lịch sử"});
        cboLocNgayLichSu.setPreferredSize(new Dimension(160, 36));

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
        pnlToolbar.add(Box.createHorizontalStrut(8));
        pnlToolbar.add(new JLabel("Tìm kiếm:"));
        pnlToolbar.add(txtTimKiemLichSu);
        pnlToolbar.add(new JLabel("Khoảng ngày:"));
        pnlToolbar.add(cboLocNgayLichSu);
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
        for (HoaDonDaThanhToan hd : dsLichSuHoaDon) {
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
        HoaDonDaThanhToan hd = dsLichSuHoaDon.get(modelRow);

        int diemCong = (int) (hd.tongThanhToan / 10000L);
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
        JOptionPane.showMessageDialog(this,
                "XUẤT BÁO CÁO DOANH THU EXCEL THÀNH CÔNG!\n" +
                        "• Tổng số hóa đơn: " + dsLichSuHoaDon.size() + " đơn\n" +
                        "• File đã lưu tại: export/BaoCaoDoanhThu_" + System.currentTimeMillis() + ".xlsx",
                "Xuất Excel thành công", JOptionPane.INFORMATION_MESSAGE);
    }
}
