package views;

import controllers.ThuCungController;
import models.entity.ChiTietLichHen;
import models.entity.DichVu;
import models.entity.LichHen;
import models.entity.NhanVien;
import models.entity.ThuCung;
import utils.IconHelper;
import utils.ThemeColors;
import views.charts.BarChartPanel;
import views.charts.DonutChartPanel;

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
import java.util.List;

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
        pnlMainScroll.add(Box.createVerticalStrut(14));

        // 2. Hàng 1 — 4 Thẻ KPI Động (GridLayout 1x4)
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 110));
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        lblDoanhThuGiaTri = new JLabel("0 đ");
        lblDoanhThuGhiChu = new JLabel("Đang tải dữ liệu...");
        pnlCards.add(taoTheKpiDong("DOANH THU HÔM NAY", lblDoanhThuGiaTri, lblDoanhThuGhiChu,
                IconHelper.TRENDING_UP, ThemeColors.PRIMARY, ThemeColors.PRIMARY_SOFT));

        lblHoaDonGiaTri = new JLabel("0 đơn");
        lblHoaDonGhiChu = new JLabel("0 đơn bán lẻ, 0 đơn dịch vụ");
        pnlCards.add(taoTheKpiDong("HÓA ĐƠN ĐÃ LẬP", lblHoaDonGiaTri, lblHoaDonGhiChu,
                IconHelper.RECEIPT, ThemeColors.SUCCESS, ThemeColors.SUCCESS_BG));

        lblThuCungGiaTri = new JLabel("0 bé");
        lblThuCungGhiChu = new JLabel("0 hoàn thành, 0 đang phục vụ");
        pnlCards.add(taoTheKpiDong("THÚ CƯNG LÀM DỊCH VỤ", lblThuCungGiaTri, lblThuCungGhiChu,
                IconHelper.PAW, ThemeColors.INFO, ThemeColors.INFO_BG));

        lblCanhBaoGiaTri = new JLabel("0 lô hàng");
        lblCanhBaoGhiChu = new JLabel("Ưu tiên xuất theo chuẩn FEFO");
        pnlCards.add(taoTheKpiDong("CẢNH BÁO LÔ HÀNG FEFO", lblCanhBaoGiaTri, lblCanhBaoGhiChu,
                IconHelper.ALERT_TRIANGLE, ThemeColors.DANGER, ThemeColors.DANGER_BG));

        pnlMainScroll.add(pnlCards);
        pnlMainScroll.add(Box.createVerticalStrut(16));

        // 3. Hàng 2 — 2 Biểu đồ trực quan (GridLayout 1x2)
        JPanel pnlCharts = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlCharts.setOpaque(false);
        pnlCharts.setPreferredSize(new Dimension(0, 290));
        pnlCharts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        barChartPanel = new BarChartPanel();
        donutChartPanel = new DonutChartPanel();

        pnlCharts.add(barChartPanel);
        pnlCharts.add(donutChartPanel);

        pnlMainScroll.add(pnlCharts);
        pnlMainScroll.add(Box.createVerticalStrut(16));

        // 4. Hàng 3 — 2 Bảng dữ liệu thực tế (GridLayout 1x2)
        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlTables.setOpaque(false);
        pnlTables.setPreferredSize(new Dimension(0, 265));
        pnlTables.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // Bảng trái: Lịch hẹn dịch vụ trong ngày
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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblLichHen = taoBangDuLieu(modelLichHen, 5);
        pnlLichHen.add(new JScrollPane(tblLichHen), BorderLayout.CENTER);

        // Bảng phải: Cảnh báo hạn dùng lô hàng (Ưu tiên FEFO)
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
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblCanhBao = taoBangDuLieu(modelCanhBaoFEFO, 5);
        // Căn giữa thêm cột Tồn kho và Đơn vị cho bảng FEFO
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblCanhBao.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tblCanhBao.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tblCanhBao.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        pnlCanhBao.add(new JScrollPane(tblCanhBao), BorderLayout.CENTER);

        pnlTables.add(pnlLichHen);
        pnlTables.add(pnlCanhBao);

        pnlMainScroll.add(pnlTables);

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
     * Nạp lại toàn bộ KPI, Biểu đồ và 2 Bảng dữ liệu từ nguồn thực tế (BanHangPOSPanel, ThuCungController, KhoLoHangPanel).
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
        lblHoaDonGhiChu.setText(donBanLe + " đơn bán lẻ, " + donDichVu + " đơn dịch vụ");

        // 2. Lấy dữ liệu lịch hẹn thực tế từ ThuCungController
        List<LichHen> dsLichHen = new ThuCungController().layDanhSachLichHen();
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

            List<ChiTietLichHen> dsCT = lh.getDsChiTiet();
            if (dsCT != null && !dsCT.isEmpty()) {
                for (ChiTietLichHen ct : dsCT) {
                    tongThuCungDichVu++;
                    if (statusHienThi.contains("hoàn thành")) {
                        soDichVuXong++;
                    } else if (statusHienThi.contains("thực hiện")) {
                        soDangThucHien++;
                    } else {
                        soChoTiepNhan++;
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
                tongThuCungDichVu++;
                if (statusHienThi.contains("hoàn thành")) {
                    soDichVuXong++;
                } else if (statusHienThi.contains("thực hiện")) {
                    soDangThucHien++;
                } else {
                    soChoTiepNhan++;
                }
                String tenKTV = lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getHoTen() : "Chưa phân công";
                modelLichHen.addRow(new Object[]{gioHen, tenKhach, "Thú cưng", "Dịch vụ chăm sóc", tenKTV, statusHienThi});
            }
        }

        // Cộng thêm các đơn dịch vụ phát sinh trực tiếp tại quầy POS (vượt ngoài đơn mẫu ban đầu)
        int extraPosServiceOrders = Math.max(0, donDichVu - 1);
        tongThuCungDichVu += extraPosServiceOrders;
        soDichVuXong += extraPosServiceOrders;

        lblThuCungGiaTri.setText(tongThuCungDichVu + " bé");
        lblThuCungGhiChu.setText(soDichVuXong + " hoàn thành, " + soDangThucHien + " đang làm, " + soChoTiepNhan + " chờ");

        // 3. Lấy dữ liệu lô hàng thực tế từ KhoLoHangPanel
        List<KhoLoHangPanel.LoHangRecord> dsLo = KhoLoHangPanel.layDanhSachLoHangFEFO();
        int soLoCanhBao = KhoLoHangPanel.demSoLoCanhBaoFEFO();
        modelCanhBaoFEFO.setRowCount(0);

        int tongTonCanhBao = 0;
        for (KhoLoHangPanel.LoHangRecord lo : dsLo) {
            String mucCanhBao = xacDinhMucCanhBaoHienThi(lo);
            if (lo.trangThaiFEFO != null && (lo.trangThaiFEFO.toUpperCase().contains("CẬN DATE")
                    || lo.trangThaiFEFO.toUpperCase().contains("ƯU TIÊN")
                    || lo.soLuongTon <= 15)) {
                tongTonCanhBao += lo.soLuongTon;
            }
            modelCanhBaoFEFO.addRow(new Object[]{
                    lo.soLo,
                    lo.tenSP,
                    lo.hanSD,
                    String.valueOf(lo.soLuongTon),
                    lo.donVi,
                    mucCanhBao
            });
        }

        lblCanhBaoGiaTri.setText(soLoCanhBao + " lô cảnh báo");
        lblCanhBaoGhiChu.setText("Tồn nhóm ưu tiên FEFO: " + tongTonCanhBao + " đơn vị");

        // 4. Cập nhật 2 biểu đồ trực quan
        if (barChartPanel != null) {
            barChartPanel.capNhatDoanhThuHomNay(doanhThuHomNay, tongDon);
        }
        if (donutChartPanel != null) {
            donutChartPanel.capNhatCoCauDoanhThu(dtSpa, dtHangHoa, dtKhac, soDichVuXong, Math.max(1, tongThuCungDichVu));
        }

        revalidate();
        repaint();
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
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

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
