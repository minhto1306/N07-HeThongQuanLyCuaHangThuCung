package gui;

import bus.ThuCungBus;
import entity.ChiTietLichHen;
import entity.DichVu;
import entity.LichHen;
import entity.NhanVien;
import entity.ThuCung;
import utils.IconHelper;
import utils.ThemeColors;
import gui.charts.BarChartPanel;
import gui.charts.DonutChartPanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel Tổng quan / Dashboard chính hiển thị số liệu thống kê KPI, biểu đồ doanh số trực quan và tác vụ trong ngày.
 * Thiết kế chuẩn phong cách SaaS hiện đại:
 * - Tông màu Warm Peach / Soft Apricot / Nude dịu mắt.
 * - Các Card KPI có icon Lucide trong vòng tròn pastel thanh lịch.
 * - Tích hợp 2 biểu đồ Graphics2D chuyên nghiệp: BarChartPanel (Doanh thu 7 ngày) và DonutChartPanel (Cơ cấu doanh thu & Tiến độ dịch vụ).
 * - Bảng JTable hiển thị rõ ràng với badge trạng thái màu sắc, tự động đồng bộ real-time với POS và Kho FEFO.
 */
public class TongQuanPanel extends JPanel {

    private static final List<TongQuanPanel> INSTANCES = new ArrayList<>();

    private final DecimalFormat dfMoney = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtfHour = DateTimeFormatter.ofPattern("HH:mm");

    // 4 Thẻ KPI động
    private JLabel lblDoanhThuGiaTri;
    private JLabel lblDoanhThuGhiChu;

    private JLabel lblHoaDonGiaTri;
    private JLabel lblHoaDonGhiChu;

    private JLabel lblThuCungGiaTri;
    private JLabel lblThuCungGhiChu;

    private JLabel lblCanhBaoGiaTri;
    private JLabel lblCanhBaoGhiChu;

    // 2 Biểu đồ trực quan
    private BarChartPanel barChartPanel;
    private DonutChartPanel donutChartPanel;

    // 2 Bảng dữ liệu thực tế
    private DefaultTableModel modelLichHen;
    private DefaultTableModel modelCanhBaoFEFO;

    // 3 Chế độ Dashboard: Vận hành, Chiến lược, Phân tích (Chuẩn Strategic / Operational / Analytical)
    public static final String MODE_OPERATIONAL = "OPERATIONAL";
    public static final String MODE_STRATEGIC = "STRATEGIC";
    public static final String MODE_ANALYTICAL = "ANALYTICAL";

    private CardLayout cardLayoutContent;
    private JPanel pnlCardsSwitch;
    private JLabel lblModeDescription;
    private JButton btnModeOperational;
    private JButton btnModeStrategic;
    private JButton btnModeAnalytical;

    // KPI Chế độ Chiến Lược (Strategic)
    private JLabel lblStratDoanhThu;
    private JLabel lblStratLoiNhuan;
    private JLabel lblBienLoiNhuan;
    private JLabel lblStratHangHoa;
    private JLabel lblStratDichVu;
    private DefaultTableModel modelTopSanPham;
    private DefaultTableModel modelHieuSuatKTV;

    // KPI Chế độ Phân Tích (Analytical)
    private JLabel lblAnalytTongMa;
    private JLabel lblAnalytFEFO;
    private JLabel lblAnalytHotel;
    private JLabel lblAnalytVIP;
    private DefaultTableModel modelAnalytROP;

    public TongQuanPanel() {
        INSTANCES.add(this);
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BACKGROUND);

        JPanel pnlMainScroll = new JPanel();
        pnlMainScroll.setLayout(new BoxLayout(pnlMainScroll, BoxLayout.Y_AXIS));
        pnlMainScroll.setOpaque(false);
        pnlMainScroll.setBorder(new EmptyBorder(14, 18, 18, 18));

        // 1. Header trên cùng: Tiêu đề bên trái + Ngày hiện tại & Nút Làm mới bên phải
        JPanel pnlHeader = new JPanel(new BorderLayout(16, 8));
        pnlHeader.setBackground(ThemeColors.SURFACE);
        pnlHeader.setBorder(ThemeColors.createCardBorder(14, 20, 14, 20));
        pnlHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));

        JPanel pnlHeaderLeft = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlHeaderLeft.setOpaque(false);

        JLabel lblWelcomeTitle = new JLabel("TỔNG QUAN HOẠT ĐỘNG CỬA HÀNG",
                IconHelper.getPrimaryIcon(IconHelper.DASHBOARD, 22), SwingConstants.LEFT);
        lblWelcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblWelcomeTitle.setForeground(ThemeColors.PRIMARY);
        lblWelcomeTitle.setIconTextGap(10);

        JLabel lblWelcomeSub = new JLabel("Theo dõi thời gian thực doanh thu POS, tiến độ dịch vụ Spa và tồn kho lô hàng FEFO",
                SwingConstants.LEFT);
        lblWelcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblWelcomeSub.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlHeaderLeft.add(lblWelcomeTitle);
        pnlHeaderLeft.add(lblWelcomeSub);

        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        pnlHeaderRight.setOpaque(false);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel lblDate = new JLabel("Hôm nay: " + dateStr,
                IconHelper.getIcon(IconHelper.CALENDAR, 15, ThemeColors.TEXT_SECONDARY), SwingConstants.RIGHT);
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(ThemeColors.TEXT_SECONDARY);
        lblDate.setIconTextGap(6);

        JButton btnTrungTamBaoBieu = ThemeColors.createPrimaryButton("Trung tâm 7 Báo biểu",
                IconHelper.getIcon(IconHelper.BAR_CHART, 14, Color.WHITE));
        btnTrungTamBaoBieu.setToolTipText("Mở Trung tâm Báo biểu & Thống kê chuyên sâu (7 loại báo cáo)");
        btnTrungTamBaoBieu.addActionListener(e -> BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 0));

        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới số liệu",
                IconHelper.getIcon(IconHelper.REFRESH, 14, ThemeColors.PRIMARY_ACTIVE));
        btnLamMoi.addActionListener(e -> lamMoiDuLieuDashboard());

        pnlHeaderRight.add(lblDate);
        pnlHeaderRight.add(btnTrungTamBaoBieu);
        pnlHeaderRight.add(btnLamMoi);

        pnlHeader.add(pnlHeaderLeft, BorderLayout.WEST);
        pnlHeader.add(pnlHeaderRight, BorderLayout.EAST);
        pnlMainScroll.add(pnlHeader);
        pnlMainScroll.add(Box.createVerticalStrut(12));

        // 2. Toolbar Chuyển chế độ Dashboard (3 Phím Segmented Pill: Operational, Strategic, Analytical)
        JPanel pnlToolbar = new JPanel(new BorderLayout(14, 0));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel pnlPillBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlPillBar.setOpaque(false);

        btnModeOperational = taoNutChonMode("⚡ VẬN HÀNH (HẰNG NGÀY)");
        btnModeStrategic = taoNutChonMode("🎯 CHIẾN LƯỢC (QUẢN TRỊ)");
        btnModeAnalytical = taoNutChonMode("🔬 PHÂN TÍCH (CHUỖI CUNG ỨNG)");

        btnModeOperational.addActionListener(e -> switchMode(MODE_OPERATIONAL));
        btnModeStrategic.addActionListener(e -> switchMode(MODE_STRATEGIC));
        btnModeAnalytical.addActionListener(e -> switchMode(MODE_ANALYTICAL));

        pnlPillBar.add(btnModeOperational);
        pnlPillBar.add(btnModeStrategic);
        pnlPillBar.add(btnModeAnalytical);

        lblModeDescription = new JLabel("⚡ Vận hành: Giám sát đơn hàng, doanh số quầy và lịch hẹn thời gian thực");
        lblModeDescription.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblModeDescription.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlToolbar.add(pnlPillBar, BorderLayout.WEST);
        pnlToolbar.add(lblModeDescription, BorderLayout.EAST);

        pnlMainScroll.add(pnlToolbar);
        pnlMainScroll.add(Box.createVerticalStrut(12));

        // 3. Khung CardLayout chứa 3 Chế độ Dashboard độc lập
        cardLayoutContent = new CardLayout();
        pnlCardsSwitch = new JPanel(cardLayoutContent);
        pnlCardsSwitch.setOpaque(false);

        pnlCardsSwitch.add(taoViewOperational(), MODE_OPERATIONAL);
        pnlCardsSwitch.add(taoViewStrategic(), MODE_STRATEGIC);
        pnlCardsSwitch.add(taoViewAnalytical(), MODE_ANALYTICAL);

        switchMode(MODE_OPERATIONAL);

        pnlMainScroll.add(pnlCardsSwitch);

        // Cuộn toàn màn hình mượt mà
        JScrollPane scrMain = new JScrollPane(pnlMainScroll);
        scrMain.setBorder(BorderFactory.createEmptyBorder());
        scrMain.getViewport().setBackground(ThemeColors.BACKGROUND);
        scrMain.getVerticalScrollBar().setUnitIncrement(16);

        add(scrMain, BorderLayout.CENTER);

        // Tự động làm mới khi chuyển sang tab Tổng quan
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                lamMoiDuLieuDashboard();
            }
        });

        // Khởi tạo dữ liệu thực tế ngay khi mở
        lamMoiDuLieuDashboard();
    }

    /**
     * Làm mới đồng loạt tất cả các instance của TongQuanPanel đang hoạt động.
     */
    public static void lamMoiDuLieuDashboardAll() {
        for (TongQuanPanel p : INSTANCES) {
            p.lamMoiDuLieuDashboard();
        }
    }

    /**
     * Nạp lại toàn bộ KPI, Biểu đồ và 2 Bảng dữ liệu từ nguồn thực tế (BanHangPOSPanel, ThuCungBus, KhoLoHangPanel).
     */
    public void lamMoiDuLieuDashboard() {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(this::thucHienLamMoiDuLieuDashboard);
            } catch (Exception ex) {
                thucHienLamMoiDuLieuDashboard();
            }
            return;
        }
        thucHienLamMoiDuLieuDashboard();
    }

    private synchronized void thucHienLamMoiDuLieuDashboard() {
        // 1. Lấy số liệu thực tế từ BanHangPOSPanel
        long doanhThuHomNay = BanHangPOSPanel.tinhTongDoanhThuHomNay();
        int tongDon = BanHangPOSPanel.demTongSoHoaDonHomNay();
        int donBanLe = BanHangPOSPanel.demSoDonBanLeHomNay();
        int donDichVu = BanHangPOSPanel.demSoDonDichVuHomNay();
        long dtSpa = BanHangPOSPanel.tinhDoanhThuDichVuSpaHomNay();
        long dtHangHoa = BanHangPOSPanel.tinhDoanhThuHangHoaHomNay();
        long dtKhac = BanHangPOSPanel.tinhDoanhThuKhacHomNay();

        lblDoanhThuGiaTri.setText(dfMoney.format(doanhThuHomNay));
        lblDoanhThuGhiChu.setText("Spa: " + rutGonTien(dtSpa) + " • Hàng hóa: " + rutGonTien(dtHangHoa));

        lblHoaDonGiaTri.setText(tongDon + " đơn");
        lblHoaDonGhiChu.setText(donBanLe + " đơn bán lẻ | " + donDichVu + " đơn dịch vụ");

        // 2. Lấy dữ liệu lịch hẹn thực tế từ ThuCungBus (đồng bộ 100% với QuanLyLichHenPanel)
        List<LichHen> dsLichHen = new ThuCungBus().layDanhSachLichHen();
        modelLichHen.setRowCount(0);

        int tongThuCungDichVu = 0;
        int soDichVuXong = 0;
        int soDangThucHien = 0;
        int soChoTiepNhan = 0;

        for (LichHen lh : dsLichHen) {
            String gioHen = lh.getNgayHen() != null ? lh.getNgayHen().format(dtfHour) : "09:00";
            String tenKhach = lh.getKhachHang() != null ? lh.getKhachHang().getHoTen() : "Khách lẻ";
            String rawStatus = lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt";
            String statusHienThi = chuanHoaTrangThaiLichHen(rawStatus);
            boolean daHuy = "Hủy".equalsIgnoreCase(rawStatus) || "Đã hủy".equalsIgnoreCase(rawStatus);

            List<ChiTietLichHen> dsCT = lh.getDsChiTiet();
            if (dsCT != null && !dsCT.isEmpty()) {
                for (ChiTietLichHen ct : dsCT) {
                    if (!daHuy) {
                        tongThuCungDichVu++;
                        if (statusHienThi.contains("hoàn thành")) {
                            soDichVuXong++;
                        } else if (statusHienThi.contains("thực hiện")) {
                            soDangThucHien++;
                        } else {
                            soChoTiepNhan++;
                        }
                    }

                    ThuCung tc = ct.getThuCung();
                    String tenTC = tc != null
                            ? tc.getTenThuCung() + (tc.getGiongLoai() != null ? " (" + rutGonGiong(tc.getGiongLoai()) + ")" : "")
                            : "Thú cưng";
                    DichVu dv = ct.getDichVu();
                    String tenDV = dv != null ? dv.getTenDichVu() : "Dịch vụ Spa";
                    NhanVien ktv = ct.getKtvChiDinh() != null ? ct.getKtvChiDinh() : lh.getNhanVienTiepNhan();
                    String tenKTV = ktv != null ? ktv.getHoTen() : "Chưa phân công";

                    modelLichHen.addRow(new Object[]{gioHen, tenKhach, tenTC, tenDV, tenKTV, statusHienThi});
                }
            } else {
                if (!daHuy) {
                    tongThuCungDichVu++;
                    if (statusHienThi.contains("hoàn thành")) {
                        soDichVuXong++;
                    } else if (statusHienThi.contains("thực hiện")) {
                        soDangThucHien++;
                    } else {
                        soChoTiepNhan++;
                    }
                }
                String tenKTV = lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getHoTen() : "Chưa phân công";
                modelLichHen.addRow(new Object[]{gioHen, tenKhach, "Thú cưng", "Dịch vụ chăm sóc", tenKTV, statusHienThi});
            }
        }

        lblThuCungGiaTri.setText(tongThuCungDichVu + " bé");
        lblThuCungGhiChu.setText(soDichVuXong + " hoàn thành, " + soDangThucHien + " đang làm, " + soChoTiepNhan + " chờ");

        // 3. Lấy dữ liệu lô hàng thực tế từ KhoLoHangPanel & KhoLoHangBus (Thống nhất quy tắc ≤ 60 ngày)
        List<KhoLoHangPanel.LoHangRecord> dsLo = KhoLoHangPanel.layDanhSachLoHangFEFO();
        int soLoCanhBao = KhoLoHangPanel.demSoLoCanhBaoFEFO();
        modelCanhBaoFEFO.setRowCount(0);

        int tongTonCanhBao = 0;
        for (KhoLoHangPanel.LoHangRecord lo : dsLo) {
            if (lo.soLuongTon <= 0) continue;
            String st = lo.trangThaiFEFO != null ? lo.trangThaiFEFO.toUpperCase() : "";
            if (st.contains("CẬN DATE") || st.contains("NGUY CẤP") || st.contains("HẾT HẠN")) {
                tongTonCanhBao += lo.soLuongTon;
                String mucCanhBao = xacDinhMucCanhBaoHienThi(lo);
                modelCanhBaoFEFO.addRow(new Object[]{
                        lo.soLo,
                        lo.tenSP,
                        lo.hanSD,
                        String.valueOf(lo.soLuongTon),
                        lo.donVi,
                        mucCanhBao
                });
            }
        }

        lblCanhBaoGiaTri.setText(soLoCanhBao + " lô cảnh báo");
        lblCanhBaoGhiChu.setText("Hạn dùng ≤ 60 ngày • Tồn: " + tongTonCanhBao + " đơn vị");

        // 4. Cập nhật 2 biểu đồ trực quan
        if (barChartPanel != null) {
            barChartPanel.capNhatDoanhThuHomNay(doanhThuHomNay, tongDon);
        }
        if (donutChartPanel != null) {
            donutChartPanel.capNhatCoCauDoanhThu(dtSpa, dtHangHoa, dtKhac, soDichVuXong, Math.max(1, tongThuCungDichVu));
        }

        // 5. Cập nhật số liệu Chế độ Chiến lược (Strategic View - từ ThongKeBus thực tế)
        bus.ThongKeBus tkBus = new bus.ThongKeBus();
        if (lblStratDoanhThu != null) lblStratDoanhThu.setText(dfMoney.format(doanhThuHomNay));
        long loiNhuan = 0L;
        try {
            var rowsDT = tkBus.layBaoCaoDoanhThu(LocalDate.now(), LocalDate.now());
            long tongDT = 0L, tongGV = 0L;
            for (var r : rowsDT) {
                tongDT += r.doanhThuThuan;
                tongGV += r.giaVon;
            }
            loiNhuan = (tongDT - tongGV);
        } catch (Exception ignored) {
            loiNhuan = 0L;
        }
        if (lblStratLoiNhuan != null) lblStratLoiNhuan.setText(dfMoney.format(loiNhuan));
        if (lblBienLoiNhuan != null) { double bien = doanhThuHomNay > 0 ? (loiNhuan * 100.0 / doanhThuHomNay) : 0.0; lblBienLoiNhuan.setText(String.format("Biên lợi nhuận ~%.1f%%", bien)); }
        if (lblStratHangHoa != null) lblStratHangHoa.setText(dfMoney.format(dtHangHoa));
        if (lblStratDichVu != null) lblStratDichVu.setText(dfMoney.format(dtSpa));

        if (modelTopSanPham != null) {
            modelTopSanPham.setRowCount(0);
            List<BanHangPOSPanel.HoaDonDaThanhToan> dsHD = BanHangPOSPanel.layDanhSachHoaDonDaThanhToan();
            Map<String, Object[]> spAgg = new LinkedHashMap<>();
            for (BanHangPOSPanel.HoaDonDaThanhToan hd : dsHD) {
                if (hd.dsMon != null) {
                    for (Object[] mon : hd.dsMon) {
                        if (mon != null && mon.length > 6) {
                            String ma = mon[1] != null ? mon[1].toString() : "";
                            String ten = mon[2] != null ? mon[2].toString() : "";
                            String loai = ma.startsWith("SP") ? "Hàng hóa" : "Spa Grooming";
                            int sl = mon[4] instanceof Number ? ((Number) mon[4]).intValue() : 1;
                            long tt = 0L;
                            try {
                                String num = mon[6].toString().replaceAll("[^0-9]", "").trim();
                                if (!num.isEmpty()) tt = Long.parseLong(num);
                            } catch (Exception ignored) {}
                            if (!spAgg.containsKey(ma)) {
                                spAgg.put(ma, new Object[]{ma, ten, loai, sl, tt});
                            } else {
                                Object[] cur = spAgg.get(ma);
                                cur[3] = (int) cur[3] + sl;
                                cur[4] = (long) cur[4] + tt;
                            }
                        }
                    }
                }
            }
            if (!spAgg.isEmpty()) {
                List<Object[]> sorted = new ArrayList<>(spAgg.values());
                sorted.sort((a, b) -> Long.compare((long) b[4], (long) a[4]));
                for (int i = 0; i < Math.min(5, sorted.size()); i++) {
                    Object[] r = sorted.get(i);
                    modelTopSanPham.addRow(new Object[]{r[0], r[1], r[2], r[3] + " lượt/đơn vị", dfMoney.format((long) r[4])});
                }
            }
        }

        if (modelHieuSuatKTV != null) {
            modelHieuSuatKTV.setRowCount(0);
            try {
                var rowsNS = tkBus.layHieuSuatNhanSu(LocalDate.now().minusDays(30), LocalDate.now());
                for (var r : rowsNS) {
                    if (r.dangHoatDong && !"NV001".equalsIgnoreCase(r.maNhanVien)) {
                        int soLuot = r.soDichVu > 0 ? r.soDichVu : r.soHoaDon;
                        long tienThucTe = r.hoaHong > 0 ? r.hoaHong : r.doanhThuPhuTrach;
                        modelHieuSuatKTV.addRow(new Object[]{
                                r.hoTen,
                                r.chucVu != null ? r.chucVu : "Nhân viên",
                                soLuot + " ca / hóa đơn",
                                soLuot > 0 ? "Hoàn thành 100%" : "Đang trực ca",
                                dfMoney.format(tienThucTe)
                        });
                    }
                }
            } catch (Exception ignored) {}
        }

        // 6. Cập nhật số liệu Chế độ Phân tích (Analytical View - từ CSDL thực tế)
        int tongSoLo = dsLo.size();
        if (lblAnalytTongMa != null) lblAnalytTongMa.setText(tongSoLo + " lô kho");
        try {
            List<gui.PetHotelPanel.PhongLuuTru> dsPhong = new bus.PetHotelBus().layDanhSachPhong();
            int phongCoKhach = 0;
            for (gui.PetHotelPanel.PhongLuuTru p : dsPhong) {
                if (p.trangThai != null && p.trangThai.contains("CÓ KHÁCH")) phongCoKhach++;
            }
            int pct = dsPhong.isEmpty() ? 0 : (phongCoKhach * 100 / dsPhong.size());
            if (lblAnalytHotel != null) lblAnalytHotel.setText(phongCoKhach + " / " + dsPhong.size() + " phòng (" + pct + "%)");
        } catch (Exception e) {
            if (lblAnalytHotel != null) lblAnalytHotel.setText("Khách sạn hoạt động");
        }
        if (lblAnalytFEFO != null) lblAnalytFEFO.setText(soLoCanhBao + " lô cận date");
        if (lblAnalytVIP != null) {
            var rowsKH = tkBus.layThongKeKhachHang();
            int tongKH = Math.max(1, rowsKH.size());
            int khThanThiet = 0;
            for (var r : rowsKH) {
                if (r.diemTichLuy >= 10) khThanThiet++;
            }
            int pctThanThiet = (khThanThiet * 100) / tongKH;
            lblAnalytVIP.setText(pctThanThiet + "% (" + khThanThiet + "/" + rowsKH.size() + " thẻ thành viên)");
        }

        if (modelAnalytROP != null) {
            modelAnalytROP.setRowCount(0);
            Map<String, Object[]> spMap = new LinkedHashMap<>();
            for (KhoLoHangPanel.LoHangRecord lo : dsLo) {
                if (lo.maSP == null || lo.maSP.trim().isEmpty()) continue;
                if (!spMap.containsKey(lo.maSP)) {
                    spMap.put(lo.maSP, new Object[]{lo.maSP, lo.tenSP, lo.soLuongTon, lo.donVi});
                } else {
                    Object[] cur = spMap.get(lo.maSP);
                    cur[2] = (int) cur[2] + lo.soLuongTon;
                }
            }
            for (Object[] r : spMap.values()) {
                String maSP = (String) r[0];
                String tenSP = (String) r[1];
                int tongTon = (int) r[2];
                String donVi = (String) r[3];
                int ropDiem = 20;
                String danhGia = tongTon <= 10 ? "Cần nhập gấp" : (tongTon <= ropDiem ? "Chạm ngưỡng ROP" : "An toàn");
                String deXuat = tongTon < ropDiem ? ("+" + (ropDiem - tongTon)) : "0 (Đủ tồn)";
                modelAnalytROP.addRow(new Object[]{
                        maSP,
                        tenSP,
                        tongTon + " " + donVi,
                        ropDiem + " " + donVi,
                        deXuat,
                        danhGia
                });
            }
        }

        revalidate();
        repaint();
    }

    // ==========================================
    // 3 CHẾ ĐỘ DASHBOARD (STRATEGIC / OPERATIONAL / ANALYTICAL)
    // ==========================================

    private JPanel taoViewOperational() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);

        // Hàng 1: 4 Thẻ KPI Động (GridLayout 1x4)
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 110));
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblDoanhThuGiaTri = new JLabel("0 đ");
        lblDoanhThuGhiChu = new JLabel("Đang tải dữ liệu...");
        pnlCards.add(taoTheKpiDong("DOANH THU HÔM NAY", lblDoanhThuGiaTri, lblDoanhThuGhiChu,
                IconHelper.TRENDING_UP, ThemeColors.PRIMARY, ThemeColors.PRIMARY_SOFT,
                () -> {
                    Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    new BaoCaoThongKeDialog(topFrame, 0).setVisible(true);
                }));

        lblHoaDonGiaTri = new JLabel("0 đơn");
        lblHoaDonGhiChu = new JLabel("0 đơn bán lẻ | 0 đơn dịch vụ");
        lblHoaDonGhiChu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblHoaDonGhiChu.setToolTipText("Nhấn nửa trái để lọc Đơn bán lẻ hôm nay, nửa phải để lọc Đơn dịch vụ hôm nay");
        lblHoaDonGhiChu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getX() < lblHoaDonGhiChu.getWidth() / 2) {
                    MainDashboard.dieuHuongCard("ACTION_LICH_SU_HD_BAN_LE");
                } else {
                    MainDashboard.dieuHuongCard("ACTION_LICH_SU_HD_DICH_VU");
                }
                e.consume();
            }
        });
        pnlCards.add(taoTheKpiDong("HÓA ĐƠN ĐÃ LẬP", lblHoaDonGiaTri, lblHoaDonGhiChu,
                IconHelper.RECEIPT, ThemeColors.SUCCESS, ThemeColors.SUCCESS_BG,
                () -> MainDashboard.dieuHuongCard("ACTION_LICH_SU_HD")));

        lblThuCungGiaTri = new JLabel("0 bé");
        lblThuCungGhiChu = new JLabel("0 hoàn thành, 0 đang phục vụ");
        pnlCards.add(taoTheKpiDong("THÚ CƯNG LÀM DỊCH VỤ", lblThuCungGiaTri, lblThuCungGhiChu,
                IconHelper.PAW, ThemeColors.INFO, ThemeColors.INFO_BG,
                () -> MainDashboard.dieuHuongCard(MainDashboard.CARD_LICH_HEN)));

        lblCanhBaoGiaTri = new JLabel("0 lô hàng");
        lblCanhBaoGhiChu = new JLabel("Hạn dùng ≤ 60 ngày - Cần ưu tiên FEFO");
        pnlCards.add(taoTheKpiDong("LÔ HÀNG SẮP HẾT HẠN (≤ 60 NGÀY)", lblCanhBaoGiaTri, lblCanhBaoGhiChu,
                IconHelper.ALERT_TRIANGLE, ThemeColors.DANGER, ThemeColors.DANGER_BG,
                () -> MainDashboard.dieuHuongCard("ACTION_CANH_BAO_FEFO")));

        view.add(pnlCards);
        view.add(Box.createVerticalStrut(16));

        // Hàng 2: 2 Biểu đồ trực quan (GridLayout 1x2)
        JPanel pnlCharts = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCharts.setOpaque(false);
        pnlCharts.setPreferredSize(new Dimension(0, 290));
        pnlCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        barChartPanel = new BarChartPanel();
        donutChartPanel = new DonutChartPanel();

        pnlCharts.add(barChartPanel);
        pnlCharts.add(donutChartPanel);

        view.add(pnlCharts);
        view.add(Box.createVerticalStrut(16));

        // Hàng 3: 2 Bảng dữ liệu thực tế (GridLayout 1x2)
        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlTables.setOpaque(false);
        pnlTables.setPreferredSize(new Dimension(0, 265));
        pnlTables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Bảng trái: Lịch hẹn
        JPanel pnlLichHen = new JPanel(new BorderLayout(8, 10));
        pnlLichHen.setBackground(ThemeColors.SURFACE);
        pnlLichHen.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderLichHen = new JLabel("Lịch hẹn dịch vụ trong ngày",
                IconHelper.getIcon(IconHelper.CALENDAR, 18, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblHeaderLichHen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderLichHen.setForeground(ThemeColors.TEXT_PRIMARY);
        lblHeaderLichHen.setIconTextGap(8);
        pnlLichHen.add(lblHeaderLichHen, BorderLayout.NORTH);

        String[] colsLichHen = {"Giờ", "Khách hàng", "Thú cưng", "Dịch vụ", "KTV phụ trách", "Trạng thái"};
        modelLichHen = new DefaultTableModel(colsLichHen, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblLichHen = taoBangDuLieu(modelLichHen, 5);
        tblLichHen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tblLichHen.setToolTipText("Nhấp đúp chuột để mở Quản lý Lịch hẹn dịch vụ");
        tblLichHen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblLichHen.getSelectedRow() != -1) {
                    MainDashboard.dieuHuongCard(MainDashboard.CARD_LICH_HEN);
                }
            }
        });
        pnlLichHen.add(new JScrollPane(tblLichHen), BorderLayout.CENTER);

        // Bảng phải: Cảnh báo FEFO
        JPanel pnlCanhBao = new JPanel(new BorderLayout(8, 10));
        pnlCanhBao.setBackground(ThemeColors.SURFACE);
        pnlCanhBao.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderCanhBao = new JLabel("Cảnh báo hạn dùng lô hàng (Ưu tiên FEFO)",
                IconHelper.getIcon(IconHelper.ALERT_TRIANGLE, 18, ThemeColors.DANGER), SwingConstants.LEFT);
        lblHeaderCanhBao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderCanhBao.setForeground(ThemeColors.DANGER_TEXT);
        lblHeaderCanhBao.setIconTextGap(8);
        pnlCanhBao.add(lblHeaderCanhBao, BorderLayout.NORTH);

        String[] colsCanhBao = {"Số lô", "Tên sản phẩm", "Hạn sử dụng", "Tồn kho", "Đơn vị", "Mức cảnh báo"};
        modelCanhBaoFEFO = new DefaultTableModel(colsCanhBao, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblCanhBao = taoBangDuLieu(modelCanhBaoFEFO, 5);
        tblCanhBao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tblCanhBao.setToolTipText("Nhấp đúp chuột để xem chi tiết lô hàng sắp hết hạn (Kho & Lô hàng)");
        tblCanhBao.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tblCanhBao.getSelectedRow() != -1) {
                    MainDashboard.dieuHuongCard("ACTION_CANH_BAO_FEFO");
                }
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblCanhBao.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblCanhBao.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblCanhBao.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        pnlCanhBao.add(new JScrollPane(tblCanhBao), BorderLayout.CENTER);

        pnlTables.add(pnlLichHen);
        pnlTables.add(pnlCanhBao);

        view.add(pnlTables);
        return view;
    }

    private JPanel taoViewStrategic() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);

        // 4 KPI Chiến lược (Tương ứng slide Strategic Dashboard - Image 3)
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 110));
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblStratDoanhThu = new JLabel("0 đ");
        pnlCards.add(taoTheKpiDong("TỔNG DOANH THU THỰC TẾ", lblStratDoanhThu, new JLabel("+12.5% so với kỳ trước"),
                IconHelper.TRENDING_UP, ThemeColors.PRIMARY, ThemeColors.PRIMARY_SOFT,
                () -> {
                    Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    new BaoCaoThongKeDialog(topFrame, 0).setVisible(true);
                }));

        lblStratLoiNhuan = new JLabel("0 đ");
        lblBienLoiNhuan = new JLabel("Biên lợi nhuận 0.0%");
        pnlCards.add(taoTheKpiDong("LỢI NHUẬN GỘP ƯỚC TÍNH", lblStratLoiNhuan, lblBienLoiNhuan,
                IconHelper.CHECK, ThemeColors.SUCCESS, ThemeColors.SUCCESS_BG,
                () -> {
                    Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
                    new BaoCaoThongKeDialog(topFrame, 1).setVisible(true);
                }));

        lblStratHangHoa = new JLabel("0 đ");
        pnlCards.add(taoTheKpiDong("DOANH THU HÀNG HÓA", lblStratHangHoa, new JLabel("Bán lẻ tại quầy POS"),
                IconHelper.BOXES, ThemeColors.INFO, ThemeColors.INFO_BG,
                () -> MainDashboard.dieuHuongCard("ACTION_LICH_SU_HD_BAN_LE")));

        lblStratDichVu = new JLabel("0 đ");
        pnlCards.add(taoTheKpiDong("DỊCH VỤ SPA & PET HOTEL", lblStratDichVu, new JLabel("Grooming & Khách sạn"),
                IconHelper.SCISSORS, ThemeColors.WARNING, ThemeColors.WARNING_BG,
                () -> MainDashboard.dieuHuongCard("ACTION_LICH_SU_HD_DICH_VU")));

        view.add(pnlCards);
        view.add(Box.createVerticalStrut(16));

        // Bảng Báo cáo Chiến lược: Top Sản phẩm bán chạy & Bảng xếp hạng KTV
        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlTables.setOpaque(false);
        pnlTables.setPreferredSize(new Dimension(0, 480));
        pnlTables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        // Bảng trái: Top 5 Bán chạy
        JPanel pnlTopSP = new JPanel(new BorderLayout(8, 10));
        pnlTopSP.setBackground(ThemeColors.SURFACE);
        pnlTopSP.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderTop = new JLabel("Top 5 Sản phẩm & Dịch vụ bán chạy nhất",
                IconHelper.getIcon(IconHelper.TRENDING_UP, 18, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblHeaderTop.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTop.setForeground(ThemeColors.TEXT_PRIMARY);
        lblHeaderTop.setIconTextGap(8);
        pnlTopSP.add(lblHeaderTop, BorderLayout.NORTH);

        String[] colsTop = {"Mã SP", "Tên sản phẩm / Dịch vụ", "Phân loại", "Đã bán", "Doanh thu"};
        modelTopSanPham = new DefaultTableModel(colsTop, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblTop = taoBangDuLieu(modelTopSanPham, -1);
        pnlTopSP.add(new JScrollPane(tblTop), BorderLayout.CENTER);

        // Bảng phải: Hiệu suất KTV & Đánh giá CSAT
        JPanel pnlKTV = new JPanel(new BorderLayout(8, 10));
        pnlKTV.setBackground(ThemeColors.SURFACE);
        pnlKTV.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderKTV = new JLabel("Hiệu suất Nhân sự & Đánh giá CSAT",
                IconHelper.getIcon(IconHelper.USERS, 18, ThemeColors.SUCCESS_TEXT), SwingConstants.LEFT);
        lblHeaderKTV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderKTV.setForeground(ThemeColors.TEXT_PRIMARY);
        lblHeaderKTV.setIconTextGap(8);
        pnlKTV.add(lblHeaderKTV, BorderLayout.NORTH);

        String[] colsKTV = {"Nhân viên", "Vai trò", "Số ca xong", "Đánh giá CSAT", "Hoa hồng"};
        modelHieuSuatKTV = new DefaultTableModel(colsKTV, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblKTV = taoBangDuLieu(modelHieuSuatKTV, 3);
        pnlKTV.add(new JScrollPane(tblKTV), BorderLayout.CENTER);

        pnlTables.add(pnlTopSP);
        pnlTables.add(pnlKTV);

        view.add(pnlTables);
        return view;
    }

    private JPanel taoViewAnalytical() {
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);

        // 4 KPI Phân tích Chuỗi cung ứng & Kho (Tương ứng slide Analytical Dashboard - Image 5)
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 110));
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblAnalytTongMa = new JLabel("48 mã");
        pnlCards.add(taoTheKpiDong("TỔNG MÃ HÀNG TỒN KHO", lblAnalytTongMa, new JLabel("Tồn an toàn & Chu kỳ ROP"),
                IconHelper.BOXES, ThemeColors.PRIMARY, ThemeColors.PRIMARY_SOFT));

        lblAnalytHotel = new JLabel("6 / 8 phòng");
        pnlCards.add(taoTheKpiDong("LƯU TRÚ PET HOTEL", lblAnalytHotel, new JLabel("Tỷ lệ lấp đầy 75.0%"),
                IconHelper.HOME, ThemeColors.INFO, ThemeColors.INFO_BG));

        lblAnalytFEFO = new JLabel("0 lô");
        pnlCards.add(taoTheKpiDong("LÔ HÀNG CẬN DATE (FEFO)", lblAnalytFEFO, new JLabel("Ưu tiên xả hàng trước"),
                IconHelper.ALERT_TRIANGLE, ThemeColors.DANGER, ThemeColors.DANGER_BG));

        lblAnalytVIP = new JLabel("68.5%");
        pnlCards.add(taoTheKpiDong("TỶ LỆ KHÁCH HÀNG VIP", lblAnalytVIP, new JLabel("Khách hàng thân thiết"),
                IconHelper.USER_CHECK, ThemeColors.SUCCESS, ThemeColors.SUCCESS_BG));

        view.add(pnlCards);
        view.add(Box.createVerticalStrut(16));

        // 2 Bảng: Cảnh báo FEFO chi tiết + Điểm ROP
        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlTables.setOpaque(false);
        pnlTables.setPreferredSize(new Dimension(0, 480));
        pnlTables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        // Bảng trái: Bảng FEFO
        JPanel pnlFEFO = new JPanel(new BorderLayout(8, 10));
        pnlFEFO.setBackground(ThemeColors.SURFACE);
        pnlFEFO.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderFEFO = new JLabel("Phân tích Lô hàng & Trạng thái FEFO",
                IconHelper.getIcon(IconHelper.ALERT_TRIANGLE, 18, ThemeColors.DANGER), SwingConstants.LEFT);
        lblHeaderFEFO.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderFEFO.setForeground(ThemeColors.DANGER_TEXT);
        lblHeaderFEFO.setIconTextGap(8);
        pnlFEFO.add(lblHeaderFEFO, BorderLayout.NORTH);

        JTable tblFEFODup = taoBangDuLieu(modelCanhBaoFEFO, 5);
        pnlFEFO.add(new JScrollPane(tblFEFODup), BorderLayout.CENTER);

        // Bảng phải: Điểm tái đặt hàng (ROP)
        JPanel pnlROP = new JPanel(new BorderLayout(8, 10));
        pnlROP.setBackground(ThemeColors.SURFACE);
        pnlROP.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        JLabel lblHeaderROP = new JLabel("Điểm Tái Đặt Hàng (ROP) & Nhu cầu bù kho",
                IconHelper.getIcon(IconHelper.BOXES, 18, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblHeaderROP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderROP.setForeground(ThemeColors.TEXT_PRIMARY);
        lblHeaderROP.setIconTextGap(8);
        pnlROP.add(lblHeaderROP, BorderLayout.NORTH);

        String[] colsROP = {"Mã SP", "Tên sản phẩm", "Tồn hiện tại", "Điểm ROP", "Đề xuất đặt", "Khuyến nghị"};
        modelAnalytROP = new DefaultTableModel(colsROP, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblROP = taoBangDuLieu(modelAnalytROP, 5);
        pnlROP.add(new JScrollPane(tblROP), BorderLayout.CENTER);

        pnlTables.add(pnlFEFO);
        pnlTables.add(pnlROP);

        view.add(pnlTables);
        return view;
    }

    private JButton taoNutChonMode(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private void switchMode(String mode) {
        if (cardLayoutContent != null && pnlCardsSwitch != null) {
            cardLayoutContent.show(pnlCardsSwitch, mode);
        }
        capNhatTrangThaiNutMode(mode);
        if (lblModeDescription != null) {
            if (MODE_OPERATIONAL.equals(mode)) {
                lblModeDescription.setText("⚡ Vận hành: Giám sát đơn hàng, doanh số quầy và lịch hẹn thời gian thực");
            } else if (MODE_STRATEGIC.equals(mode)) {
                lblModeDescription.setText("🎯 Chiến lược: Cung cấp bức tranh toàn cảnh doanh thu, lợi nhuận gộp cho cấp quản lý");
            } else if (MODE_ANALYTICAL.equals(mode)) {
                lblModeDescription.setText("🔬 Phân tích: Đào sâu tồn kho FEFO, chu kỳ tái đặt hàng ROP và chuỗi cung ứng");
            }
        }
    }

    private void capNhatTrangThaiNutMode(String activeMode) {
        setButtonActive(btnModeOperational, MODE_OPERATIONAL.equals(activeMode));
        setButtonActive(btnModeStrategic, MODE_STRATEGIC.equals(activeMode));
        setButtonActive(btnModeAnalytical, MODE_ANALYTICAL.equals(activeMode));
    }

    private void setButtonActive(JButton btn, boolean active) {
        if (btn == null) return;
        btn.putClientProperty("JButton.buttonType", "roundRect");
        if (active) {
            btn.setBackground(ThemeColors.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new CompoundBorder(
                    new ThemeColors.RoundedBorder(ThemeColors.PRIMARY_ACTIVE, 16, 1),
                    new EmptyBorder(6, 14, 6, 14)
            ));
        } else {
            btn.setBackground(ThemeColors.SURFACE);
            btn.setForeground(ThemeColors.TEXT_SECONDARY);
            btn.setBorder(new CompoundBorder(
                    new ThemeColors.RoundedBorder(ThemeColors.BORDER, 16, 1),
                    new EmptyBorder(6, 14, 6, 14)
            ));
        }
    }

    private String chuanHoaTrangThaiLichHen(String raw) {
        if (raw == null) return "Chờ tiếp nhận";
        String lower = raw.toLowerCase();
        if (lower.contains("hoàn thành") || lower.contains("xong")) {
            return "Đã hoàn thành";
        } else if (lower.contains("đang") || lower.contains("phục vụ") || lower.contains("thực hiện")) {
            return "Đang thực hiện";
        } else if (lower.contains("hủy")) {
            return "Đã hủy";
        }
        return "Chờ tiếp nhận";
    }

    private String rutGonGiong(String giong) {
        if (giong == null) return "";
        if (giong.contains("Mèo")) return "Mèo";
        if (giong.startsWith("Chó ")) return giong.substring(4).trim();
        return giong;
    }

    private String rutGonTien(long amount) {
        if (amount >= 1_000_000L) {
            return String.format("%.2f Tr", amount / 1_000_000.0);
        } else if (amount >= 1_000L) {
            return (amount / 1_000L) + "k";
        }
        return amount + " đ";
    }

    private String xacDinhMucCanhBaoHienThi(KhoLoHangPanel.LoHangRecord lo) {
        if (lo.soLuongTon <= 0) {
            return "Hết hạn / Hết tồn";
        }
        String st = lo.trangThaiFEFO != null ? lo.trangThaiFEFO.toUpperCase() : "";
        if (st.contains("HẾT HẠN") || st.contains("NGUY CẤP")) {
            return "Sắp hết hạn";
        }
        if (st.contains("CẬN DATE (1 THÁNG)")) {
            return lo.soLuongTon <= 15 ? "Sắp hết hạn" : "Ưu tiên xuất bán";
        }
        if (st.contains("CẬN DATE") || st.contains("ƯU TIÊN")) {
            return "Ưu tiên xuất bán";
        }
        if (lo.soLuongTon <= 10) {
            return "Cảnh báo tồn thấp";
        }
        return "Theo dõi";
    }

    private JPanel taoTheKpiDong(String tieuDe, JLabel lblGiaTri, JLabel lblGhiChu, String iconName, Color iconColor, Color iconBgColor) {
        return taoTheKpiDong(tieuDe, lblGiaTri, lblGhiChu, iconName, iconColor, iconBgColor, null);
    }

    private JPanel taoTheKpiDong(String tieuDe, JLabel lblGiaTri, JLabel lblGhiChu, String iconName, Color iconColor, Color iconBgColor, Runnable onClickAction) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        if (onClickAction != null) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.setToolTipText("Bấm để xem chi tiết danh sách...");
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onClickAction.run();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(new Color(0xFF, 0xFA, 0xF5));
                    card.setBorder(new CompoundBorder(
                            new LineBorder(ThemeColors.PRIMARY, 1, true),
                            new EmptyBorder(14, 16, 14, 16)
                    ));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(ThemeColors.SURFACE);
                    card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));
                }
            });
        }

        // Vòng tròn pastel chứa Icon Lucide bên trái
        JPanel pnlIcon = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlIcon.setOpaque(false);
        pnlIcon.setPreferredSize(new Dimension(50, 50));
        pnlIcon.add(new JLabel(IconHelper.getIcon(iconName, 24, iconColor)));

        // Thông tin số liệu bên phải
        JPanel pnlInfo = new JPanel(new GridLayout(3, 1, 0, 2));
        pnlInfo.setOpaque(false);

        JLabel lblTieuDe = new JLabel(tieuDe.toUpperCase());
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTieuDe.setForeground(ThemeColors.TEXT_MUTED);

        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblGiaTri.setForeground(ThemeColors.TEXT_PRIMARY);

        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblGhiChu.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlInfo.add(lblTieuDe);
        pnlInfo.add(lblGiaTri);
        pnlInfo.add(lblGhiChu);

        card.add(pnlIcon, BorderLayout.WEST);
        card.add(pnlInfo, BorderLayout.CENTER);
        return card;
    }

    private JTable taoBangDuLieu(DefaultTableModel model, int statusColIndex) {
        JTable tbl = new JTable(model);
        tbl.setRowHeight(36);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setShowHorizontalLines(true);
        tbl.setShowVerticalLines(false);
        tbl.setGridColor(ThemeColors.BORDER_LIGHT);
        tbl.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tbl.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tbl.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Căn giữa cột đầu tiên (Giờ / Số lô)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(65);
        tbl.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Renderer cho cột trạng thái (Badge phong cách modern chip)
        if (statusColIndex >= 0 && statusColIndex < model.getColumnCount()) {
            tbl.getColumnModel().getColumn(statusColIndex).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    if (value != null) {
                        String st = value.toString();
                        if (st.contains("hoàn thành") || st.contains("Theo dõi")) {
                            lbl.setForeground(ThemeColors.SUCCESS);
                            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        } else if (st.contains("thực hiện") || st.contains("Ưu tiên")) {
                            lbl.setForeground(ThemeColors.PRIMARY);
                            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        } else if (st.contains("hết hạn") || st.contains("Hết") || st.contains("Cảnh báo")) {
                            lbl.setForeground(ThemeColors.DANGER);
                            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                        } else {
                            lbl.setForeground(ThemeColors.TEXT_SECONDARY);
                            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                        }
                    }
                    return lbl;
                }
            });
        }

        return tbl;
    }

    public void xuatBaoCaoDoanhThu() {
        BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 0);
    }

    public void xuatThongKeDichVu() {
        BaoCaoThongKeDialog.moHopThoaiBaoCao(this, 1);
    }
}
