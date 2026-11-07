package gui;

import bus.ThuCungBus;
import entity.ChiTietLichHen;
import entity.LichHen;
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
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel Quản lý Lịch hẹn & Tiến độ Chăm sóc Thú cưng (Appointment Booking & Tracking).
 * Hỗ trợ phân quyền chặt chẽ theo 4 vai trò:
 * - Kỹ thuật viên (NVCS): Xem danh sách ca phục vụ, nhận việc, cập nhật tiến độ ("Đang tắm", "Đang sấy", "Hoàn thành").
 * - Thu ngân (NVTN) & Quản lý (NVQL): Tiếp nhận lịch mới, điều phối chuyển ca, hủy lịch, chuyển sang Hóa đơn thanh toán POS.
 */
public class QuanLyLichHenPanel extends JPanel {

    private final ThuCungBus controller;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    // KPI Summary Labels
    private JLabel lblCountCho;
    private JLabel lblCountDangLam;
    private JLabel lblCountXong;
    private JLabel lblCountHuy;

    // Filter & Pagination controls
    private JTextField txtTimKiem;
    private JComboBox<String> cboLocKTV;
    private JCheckBox chkChiHomNay;
    private String currentStatusFilter = "TẤT CẢ";
    private final List<JButton> statusFilterButtons = new ArrayList<>();

    // Table & Pagination state
    private JTable tblLichHen;
    private DefaultTableModel modelLichHen;
    private List<LichHen> currentList = new ArrayList<>();
    private final List<Object[]> filteredRows = new ArrayList<>();
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private JLabel lblPageInfo;
    private JButton btnPrevPage;
    private JButton btnNextPage;

    public QuanLyLichHenPanel() {
        this(new ThuCungBus());
    }

    public QuanLyLichHenPanel(ThuCungBus controller) {
        this.controller = controller;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setBackground(ThemeColors.BACKGROUND);

        initUI();
        taiDuLieuLichHen();
    }

    private void initUI() {
        // 1. Phía trên: 4 Thẻ KPI Tóm tắt trạng thái + Thanh tìm kiếm & Bộ lọc đa tiêu chí
        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);

        pnlTop.add(taoPanelKpiSummary(), BorderLayout.NORTH);
        pnlTop.add(taoPanelToolbar(), BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        // 2. Ở giữa: Bảng danh sách Lịch hẹn dịch vụ kèm thanh phân trang
        JPanel pnlCenter = taoPanelTable();
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Phía dưới: Thanh thao tác nghiệp vụ theo vai trò
        JPanel pnlBottom = taoPanelThaoTacTheoVaiTro();
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JPanel taoPanelKpiSummary() {
        JPanel pnl = new JPanel(new GridLayout(1, 4, 12, 0));
        pnl.setOpaque(false);

        lblCountCho = new JLabel("0", SwingConstants.CENTER);
        lblCountDangLam = new JLabel("0", SwingConstants.CENTER);
        lblCountXong = new JLabel("0", SwingConstants.CENTER);
        lblCountHuy = new JLabel("0", SwingConstants.CENTER);

        pnl.add(taoKpiCard("Chờ tiếp nhận", lblCountCho, IconHelper.CLOCK, ThemeColors.WARNING_LIGHT, ThemeColors.WARNING));
        pnl.add(taoKpiCard("Đang phục vụ", lblCountDangLam, IconHelper.SPARKLES, ThemeColors.PRIMARY_SOFT, ThemeColors.PRIMARY_ACTIVE));
        pnl.add(taoKpiCard("Đã hoàn thành", lblCountXong, IconHelper.CHECK, ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS));
        pnl.add(taoKpiCard("Đã hủy / Quá giờ", lblCountHuy, IconHelper.ALERT_TRIANGLE, new Color(0xF1, 0xF5, 0xF9), ThemeColors.DANGER_TEXT));

        return pnl;
    }

    private JPanel taoKpiCard(String title, JLabel lblValue, String iconName, Color bgColor, Color textColor) {
        JPanel card = new JPanel(new BorderLayout(10, 4));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(10, 14, 10, 14));

        JPanel iconBox = new JPanel(new GridBagLayout());
        iconBox.setPreferredSize(new Dimension(36, 36));
        iconBox.setBackground(bgColor);
        iconBox.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));
        iconBox.add(new JLabel(IconHelper.getIcon(iconName, 17, textColor)));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(ThemeColors.TEXT_SECONDARY);

        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblValue.setForeground(textColor);
        lblValue.setHorizontalAlignment(SwingConstants.LEFT);

        textPanel.add(lblTitle);
        textPanel.add(lblValue);

        card.add(iconBox, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel taoPanelToolbar() {
        JPanel pnl = new JPanel(new BorderLayout(10, 6));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(10, 14, 10, 14));

        // Bên trái: Ô tìm kiếm SĐT/Mã LH/Thú cưng + Lọc KTV + Bộ lọc trạng thái
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pnlLeft.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(205, 34));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm SĐT, Mã LH, Tên khách/bé...");
        txtTimKiem.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 14, ThemeColors.TEXT_MUTED));
        txtTimKiem.setBorder(ThemeColors.createInputBorder());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { currentPage = 1; apDungBoLoc(); }
            @Override
            public void removeUpdate(DocumentEvent e) { currentPage = 1; apDungBoLoc(); }
            @Override
            public void changedUpdate(DocumentEvent e) { currentPage = 1; apDungBoLoc(); }
        });

        cboLocKTV = new JComboBox<>(new String[]{"Tất cả KTV"});
        cboLocKTV.setPreferredSize(new Dimension(175, 34));
        cboLocKTV.setToolTipText("Lọc tiến độ dịch vụ theo Kỹ thuật viên phụ trách và tải công việc");
        cboLocKTV.addActionListener(e -> {
            currentPage = 1;
            apDungBoLoc();
        });

        chkChiHomNay = new JCheckBox("Trong ngày");
        chkChiHomNay.setOpaque(false);
        chkChiHomNay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        chkChiHomNay.setForeground(ThemeColors.TEXT_SECONDARY);
        chkChiHomNay.addActionListener(e -> {
            currentPage = 1;
            apDungBoLoc();
        });

        pnlLeft.add(txtTimKiem);
        pnlLeft.add(cboLocKTV);
        pnlLeft.add(chkChiHomNay);

        String[] statuses = {"TẤT CẢ", "Đã đặt", "Đang phục vụ", "Hoàn thành", "⚠️ Quá giờ", "Hủy"};
        for (String st : statuses) {
            JButton btnFilter = taoFilterChip(st);
            statusFilterButtons.add(btnFilter);
            pnlLeft.add(btnFilter);
        }
        capNhatFilterChipStyle("TẤT CẢ");

        // Bên phải: Nút Tiếp nhận Lịch hẹn mới (Dành cho Thu ngân & Quản lý)
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
        pnlRight.setOpaque(false);

        JButton btnThemMoi = ThemeColors.createPrimaryButton("Tiếp nhận Lịch hẹn mới", IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        btnThemMoi.addActionListener(e -> moDialogDatLich());

        if (UserSession.isChamSoc()) {
            btnThemMoi.setEnabled(false);
            btnThemMoi.setToolTipText("Chức năng dành cho Thu ngân hoặc Quản lý");
        }

        pnlRight.add(btnThemMoi);

        pnl.add(pnlLeft, BorderLayout.CENTER);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    private JButton taoFilterChip(String st) {
        JButton btn = new JButton(st);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 14, 32));
        btn.addActionListener(e -> {
            currentStatusFilter = st;
            currentPage = 1;
            capNhatFilterChipStyle(st);
            apDungBoLoc();
        });
        return btn;
    }

    private void capNhatFilterChipStyle(String activeSt) {
        for (JButton btn : statusFilterButtons) {
            if (btn.getText().equalsIgnoreCase(activeSt)) {
                btn.setBackground(ThemeColors.PRIMARY);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                        new EmptyBorder(4, 10, 4, 10)
                ));
            } else {
                btn.setBackground(ThemeColors.SURFACE);
                btn.setForeground(ThemeColors.TEXT_SECONDARY);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(4, 10, 4, 10)
                ));
            }
        }
    }

    private JPanel taoPanelTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        String[] cols = {"Mã LH", "Thời gian hẹn", "Khách hàng", "Số điện thoại", "Thú cưng", "Dịch vụ yêu cầu", "KTV Phụ trách", "Trạng thái", "Tiến độ & Ghi chú"};
        modelLichHen = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblLichHen = new JTable(modelLichHen);
        tblLichHen.setRowHeight(36);
        tblLichHen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblLichHen.setShowHorizontalLines(true);
        tblLichHen.setShowVerticalLines(false);
        tblLichHen.setGridColor(ThemeColors.BORDER_LIGHT);
        tblLichHen.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblLichHen.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        tblLichHen.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLichHen.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblLichHen.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tblLichHen.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblLichHen.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblLichHen.getColumnModel().getColumn(0).setCellRenderer(center);
        tblLichHen.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblLichHen.getColumnModel().getColumn(1).setCellRenderer(center);

        // Renderer Badge màu sắc cho cột Trạng thái (kèm cảnh báo Quá giờ)
        tblLichHen.getColumnModel().getColumn(7).setPreferredWidth(135);
        tblLichHen.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String st = value != null ? value.toString() : "";
                if (st.contains("Quá giờ")) {
                    lbl.setBackground(ThemeColors.DANGER_LIGHT);
                    lbl.setForeground(ThemeColors.DANGER_TEXT);
                } else if (st.contains("Đang phục vụ")) {
                    lbl.setBackground(ThemeColors.APPOINTMENT_DOING_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_DOING_TEXT);
                } else if (st.contains("Hoàn thành")) {
                    lbl.setBackground(ThemeColors.APPOINTMENT_DONE_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_DONE_TEXT);
                } else if (st.contains("Hủy")) {
                    lbl.setBackground(ThemeColors.APPOINTMENT_CANCEL_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_CANCEL_TEXT);
                } else {
                    lbl.setBackground(ThemeColors.APPOINTMENT_WAITING_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_WAITING_TEXT);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });

        pnl.add(new JScrollPane(tblLichHen), BorderLayout.CENTER);

        // Thanh phân trang (20 dòng/trang) giúp xử lý lượng lớn lịch hẹn không bị đơ UI
        JPanel pnlPagination = new JPanel(new BorderLayout());
        pnlPagination.setOpaque(false);
        pnlPagination.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel lblSortHint = new JLabel("⚡ Tự động ưu tiên hiển thị: Đang phục vụ ➔ Đã đặt (Sắp xếp giờ hẹn gần nhất & cảnh báo ca quá giờ)");
        lblSortHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSortHint.setForeground(ThemeColors.TEXT_SECONDARY);

        JPanel pnlPageBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlPageBtns.setOpaque(false);

        btnPrevPage = ThemeColors.createSecondaryButton("« Trang trước", null);
        btnPrevPage.setPreferredSize(new Dimension(110, 28));
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                hienThiTrangHienTai();
            }
        });

        lblPageInfo = new JLabel("Trang 1 / 1 (0 ca)", SwingConstants.CENTER);
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPageInfo.setForeground(ThemeColors.TEXT_PRIMARY);

        btnNextPage = ThemeColors.createSecondaryButton("Trang sau »", null);
        btnNextPage.setPreferredSize(new Dimension(110, 28));
        btnNextPage.addActionListener(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) filteredRows.size() / PAGE_SIZE));
            if (currentPage < totalPages) {
                currentPage++;
                hienThiTrangHienTai();
            }
        });

        pnlPageBtns.add(btnPrevPage);
        pnlPageBtns.add(lblPageInfo);
        pnlPageBtns.add(btnNextPage);

        pnlPagination.add(lblSortHint, BorderLayout.WEST);
        pnlPagination.add(pnlPageBtns, BorderLayout.EAST);
        pnl.add(pnlPagination, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel taoPanelThaoTacTheoVaiTro() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        String roleDesc = UserSession.getRoleDescription();
        JLabel lblRoleHint = new JLabel("Vai trò hiện tại: " + roleDesc,
                IconHelper.getPrimaryIcon(IconHelper.USER_CHECK, 16), SwingConstants.LEFT);
        lblRoleHint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRoleHint.setForeground(ThemeColors.TEXT_SECONDARY);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        JButton btnNhanViec = ThemeColors.createSecondaryButton("Bắt đầu làm (Đang phục vụ)",
                IconHelper.getIcon(IconHelper.SPARKLES, 13, ThemeColors.PRIMARY_ACTIVE));
        JButton btnHoanThanh = ThemeColors.createSecondaryButton("Hoàn thành dịch vụ",
                IconHelper.getIcon(IconHelper.CHECK, 13, ThemeColors.SUCCESS));

        JButton btnChuyenHoaDon = ThemeColors.createPrimaryButton("Chuyển sang Hóa đơn POS / Đơn tạm",
                IconHelper.getIcon(IconHelper.CREDIT_CARD, 14, Color.WHITE));
        JButton btnHuyLich = ThemeColors.createSecondaryButton("Hủy lịch hẹn",
                IconHelper.getIcon(IconHelper.TRASH, 13, ThemeColors.DANGER));

        btnNhanViec.addActionListener(e -> capNhatTrangThaiChon("Đang phục vụ"));
        btnHoanThanh.addActionListener(e -> capNhatTrangThaiChon("Hoàn thành"));
        btnHuyLich.addActionListener(e -> capNhatTrangThaiChon("Hủy"));
        btnChuyenHoaDon.addActionListener(e -> chuyenSangHoaDonPOS());

        pnlActions.add(btnNhanViec);
        pnlActions.add(btnHoanThanh);

        if (UserSession.isQuanLy() || UserSession.isThuNgan()) {
            pnlActions.add(btnHuyLich);
            pnlActions.add(btnChuyenHoaDon);
            JButton btnGuiEmailNhac = ThemeColors.createSecondaryButton("Gửi Email Nhắc Lịch",
                    IconHelper.getIcon(IconHelper.CLOCK, 13, ThemeColors.PRIMARY_ACTIVE));
            btnGuiEmailNhac.addActionListener(e -> guiEmailNhacLichHen());
            pnlActions.add(btnGuiEmailNhac);
        }

        pnl.add(lblRoleHint, BorderLayout.WEST);
        pnl.add(pnlActions, BorderLayout.EAST);
        return pnl;
    }

    public void moDialogDatLich() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DatLichHenDialog dialog = new DatLichHenDialog(topFrame, controller, this::taiDuLieuLichHen);
        dialog.setVisible(true);
    }

    public void tiepNhanLichHen() {
        moDialogDatLich();
    }

    public void traCuuLichHen(String keyword) {
        if (txtTimKiem != null) {
            txtTimKiem.setText(keyword != null ? keyword : "");
        }
    }

    public void capNhatTienDoDichVu(String trangThai) {
        capNhatTrangThaiChon(trangThai);
    }

    public void taiDuLieuLichHen() {
        currentList = controller.layDanhSachLichHen();
        capNhatDanhSachKtvVaKpi();
        apDungBoLoc();
    }

    private boolean isCaQuaGio(LichHen lh) {
        if (lh == null || lh.getNgayHen() == null) return false;
        String st = lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "";
        if ("Hoàn thành".equalsIgnoreCase(st) || "Hủy".equalsIgnoreCase(st) || "Đã hủy".equalsIgnoreCase(st)) {
            return false;
        }
        int thoiLuong = 60;
        if (lh.getDsChiTiet() != null && !lh.getDsChiTiet().isEmpty()) {
            ChiTietLichHen ct = lh.getDsChiTiet().get(0);
            if (ct.getDichVu() != null && ct.getDichVu().getThoiLuongPhut() > 0) {
                thoiLuong = ct.getDichVu().getThoiLuongPhut();
            }
        }
        long elapsedMinutes = java.time.Duration.between(lh.getNgayHen(), java.time.LocalDateTime.now()).toMinutes();
        return elapsedMinutes > thoiLuong;
    }

    private void capNhatDanhSachKtvVaKpi() {
        int cho = 0, dangLam = 0, xong = 0, huyVaQuaGio = 0;
        java.util.Map<String, Integer> ktvActiveCount = new java.util.LinkedHashMap<>();

        for (LichHen lh : currentList) {
            String st = lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt";
            boolean quaGio = isCaQuaGio(lh);
            if ("Đang phục vụ".equalsIgnoreCase(st)) {
                dangLam++;
                if (quaGio) huyVaQuaGio++;
            } else if ("Hoàn thành".equalsIgnoreCase(st)) {
                xong++;
            } else if ("Hủy".equalsIgnoreCase(st) || "Đã hủy".equalsIgnoreCase(st)) {
                huyVaQuaGio++;
            } else {
                cho++;
                if (quaGio) huyVaQuaGio++;
            }

            String tenKtv = layTenKtv(lh);
            if (!"Chưa chỉ định".equalsIgnoreCase(tenKtv)) {
                ktvActiveCount.putIfAbsent(tenKtv, 0);
                if ("Đang phục vụ".equalsIgnoreCase(st) || "Đã đặt".equalsIgnoreCase(st)) {
                    ktvActiveCount.put(tenKtv, ktvActiveCount.get(tenKtv) + 1);
                }
            }
        }

        lblCountCho.setText(String.valueOf(cho));
        lblCountDangLam.setText(String.valueOf(dangLam));
        lblCountXong.setText(String.valueOf(xong));
        lblCountHuy.setText(String.valueOf(huyVaQuaGio));

        if (cboLocKTV != null) {
            String prev = (String) cboLocKTV.getSelectedItem();
            java.awt.event.ActionListener[] listeners = cboLocKTV.getActionListeners();
            for (java.awt.event.ActionListener l : listeners) cboLocKTV.removeActionListener(l);

            cboLocKTV.removeAllItems();
            cboLocKTV.addItem("Tất cả KTV");
            for (java.util.Map.Entry<String, Integer> entry : ktvActiveCount.entrySet()) {
                cboLocKTV.addItem(entry.getKey() + " (" + entry.getValue() + " ca)");
            }
            if (prev != null) {
                for (int i = 0; i < cboLocKTV.getItemCount(); i++) {
                    String item = cboLocKTV.getItemAt(i);
                    String basePrev = prev.contains(" (") ? prev.substring(0, prev.indexOf(" (")) : prev;
                    if (item.startsWith(basePrev)) {
                        cboLocKTV.setSelectedIndex(i);
                        break;
                    }
                }
            }
            for (java.awt.event.ActionListener l : listeners) cboLocKTV.addActionListener(l);
        }
    }

    private String layTenKtv(LichHen lh) {
        String tenKtv = lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getHoTen() : "Chưa chỉ định";
        if (lh.getDsChiTiet() != null && !lh.getDsChiTiet().isEmpty()) {
            ChiTietLichHen ct = lh.getDsChiTiet().get(0);
            if (ct.getKtvChiDinh() != null && ct.getKtvChiDinh().getHoTen() != null) {
                tenKtv = ct.getKtvChiDinh().getHoTen();
            }
        }
        return tenKtv;
    }

    private int xepHangUuTienTrangThai(LichHen lh) {
        String st = lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt";
        if ("Đang phục vụ".equalsIgnoreCase(st)) return 1;
        if ("Đã đặt".equalsIgnoreCase(st)) return 2;
        if ("Hoàn thành".equalsIgnoreCase(st)) return 3;
        return 4;
    }

    private void apDungBoLoc() {
        filteredRows.clear();
        String kw = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";
        String ktvFilter = cboLocKTV != null && cboLocKTV.getSelectedItem() != null
                ? cboLocKTV.getSelectedItem().toString() : "Tất cả KTV";
        if (ktvFilter.contains(" (")) {
            ktvFilter = ktvFilter.substring(0, ktvFilter.indexOf(" (")).trim();
        }
        boolean onlyToday = chkChiHomNay != null && chkChiHomNay.isSelected();
        java.time.LocalDate today = java.time.LocalDate.now();

        List<LichHen> sorted = new ArrayList<>(currentList);
        sorted.sort((a, b) -> {
            int rA = xepHangUuTienTrangThai(a);
            int rB = xepHangUuTienTrangThai(b);
            if (rA != rB) return Integer.compare(rA, rB);
            if (a.getNgayHen() != null && b.getNgayHen() != null) {
                return a.getNgayHen().compareTo(b.getNgayHen());
            }
            return 0;
        });

        for (LichHen lh : sorted) {
            String rawSt = lh.getTrangThaiLichHen() != null ? lh.getTrangThaiLichHen() : "Đã đặt";
            boolean quaGio = isCaQuaGio(lh);

            boolean matchSt;
            if ("TẤT CẢ".equalsIgnoreCase(currentStatusFilter)) {
                matchSt = true;
            } else if ("⚠️ Quá giờ".equalsIgnoreCase(currentStatusFilter)) {
                matchSt = quaGio;
            } else {
                matchSt = rawSt.equalsIgnoreCase(currentStatusFilter);
            }

            if (onlyToday && (lh.getNgayHen() == null || !lh.getNgayHen().toLocalDate().equals(today))) {
                continue;
            }

            String tenKtv = layTenKtv(lh);
            boolean matchKtv = "Tất cả KTV".equalsIgnoreCase(ktvFilter) || tenKtv.equalsIgnoreCase(ktvFilter);

            String khTen = lh.getKhachHang() != null ? lh.getKhachHang().getHoTen() : "";
            String sdt = lh.getKhachHang() != null ? lh.getKhachHang().getSoDienThoai() : "";
            String tenThuCung = "Thú cưng";
            String tenDichVu = "Dịch vụ chăm sóc";
            int thoiLuongPhut = 60;

            if (lh.getDsChiTiet() != null && !lh.getDsChiTiet().isEmpty()) {
                ChiTietLichHen ct = lh.getDsChiTiet().get(0);
                if (ct.getThuCung() != null && ct.getThuCung().getTenThuCung() != null) {
                    tenThuCung = ct.getThuCung().getTenThuCung();
                }
                if (ct.getDichVu() != null) {
                    if (ct.getDichVu().getTenDichVu() != null) tenDichVu = ct.getDichVu().getTenDichVu();
                    if (ct.getDichVu().getThoiLuongPhut() > 0) thoiLuongPhut = ct.getDichVu().getThoiLuongPhut();
                }
            }

            boolean matchKw = kw.isEmpty() ||
                    lh.getMaLichHen().toLowerCase().contains(kw) ||
                    khTen.toLowerCase().contains(kw) ||
                    sdt.contains(kw) ||
                    tenThuCung.toLowerCase().contains(kw) ||
                    tenDichVu.toLowerCase().contains(kw);

            if (matchSt && matchKtv && matchKw) {
                String stHienThi = quaGio ? (rawSt + " (⚠️ Quá giờ)") : rawSt;
                String ghiChuGoc = lh.getGhiChu() != null ? lh.getGhiChu() : "";
                String tienDoGhiChu = quaGio
                        ? ("⚠️ Đã vượt định mức " + thoiLuongPhut + "p • " + ghiChuGoc)
                        : ("Định mức " + thoiLuongPhut + "p" + (ghiChuGoc.isEmpty() ? "" : " • " + ghiChuGoc));

                filteredRows.add(new Object[]{
                        lh.getMaLichHen(),
                        lh.getNgayHen() != null ? lh.getNgayHen().format(dtf) : "",
                        khTen,
                        sdt,
                        tenThuCung,
                        tenDichVu,
                        tenKtv,
                        stHienThi,
                        tienDoGhiChu
                });
            }
        }

        hienThiTrangHienTai();
    }

    private void hienThiTrangHienTai() {
        modelLichHen.setRowCount(0);
        int totalRows = filteredRows.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRows / PAGE_SIZE));
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startIdx = (currentPage - 1) * PAGE_SIZE;
        int endIdx = Math.min(startIdx + PAGE_SIZE, totalRows);
        for (int i = startIdx; i < endIdx; i++) {
            modelLichHen.addRow(filteredRows.get(i));
        }

        if (lblPageInfo != null) {
            lblPageInfo.setText("Trang " + currentPage + " / " + totalPages + " (" + totalRows + " ca)");
        }
        if (btnPrevPage != null) btnPrevPage.setEnabled(currentPage > 1);
        if (btnNextPage != null) btnNextPage.setEnabled(currentPage < totalPages);
    }

    private void capNhatTrangThaiChon(String trangThaiMoi) {
        int row = tblLichHen.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch hẹn từ bảng để thực hiện!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maLH = (String) modelLichHen.getValueAt(row, 0);
        boolean ok = controller.capNhatTrangThaiLichHen(maLH, trangThaiMoi);
        if (ok) {
            taiDuLieuLichHen();
            TongQuanPanel.lamMoiDuLieuDashboardAll();
            JOptionPane.showMessageDialog(this, "Đã cập nhật trạng thái lịch hẹn [" + maLH + "] sang [" + trangThaiMoi + "]!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void chuyenSangHoaDonPOS() {
        int row = tblLichHen.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lịch hẹn cần lập hóa đơn thanh toán!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maLH = (String) modelLichHen.getValueAt(row, 0);
        String khTen = (String) modelLichHen.getValueAt(row, 2);
        String sdt = (String) modelLichHen.getValueAt(row, 3);
        String thuCung = (String) modelLichHen.getValueAt(row, 4);
        String dvTen = (String) modelLichHen.getValueAt(row, 5);
        String ktv = (String) modelLichHen.getValueAt(row, 6);
        String trangThai = (String) modelLichHen.getValueAt(row, 7);

        if (!"Hoàn thành".equalsIgnoreCase(trangThai)) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Lịch hẹn này chưa ở trạng thái 'Hoàn thành'. Bạn có chắc muốn chuyển thẳng sang thanh toán ngay?",
                    "Xác nhận thanh toán trước", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        Container top = getTopLevelAncestor();
        if (top instanceof MainDashboard) {
            MainDashboard md = (MainDashboard) top;
            md.dieuHuongSangChucNang(MainDashboard.CARD_POS);
            BanHangPOSPanel pos = md.getBanHangPOSPanel();
            if (pos != null) {
                pos.tiepNhanDichVuLichHen(maLH, khTen, sdt, thuCung, dvTen, ktv);
            }
        } else {
            String msg = String.format(
                    "ĐÃ CHUYỂN LỊCH HẸN SANG MÀN HÌNH BÁN HÀNG POS!\n\n" +
                            "• Mã lịch hẹn: %s\n" +
                            "• Khách hàng: %s (%s)\n" +
                            "• Dịch vụ: %s\n\n" +
                            "Thu ngân có thể kiểm tra giỏ hàng tại tab 'Bán hàng POS' để chốt thanh toán.",
                    maLH, khTen, sdt, dvTen
            );
            JOptionPane.showMessageDialog(this, msg, "Chuyển Hóa đơn thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // gửi email nhắc khách đưa thú cưng đến đúng giờ hẹn để tránh bị quên lịch
    private void guiEmailNhacLichHen() {
        int row = tblLichHen.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 lịch hẹn trên bảng để gửi email nhắc!", "Nhắc nhở", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maLH = (String) modelLichHen.getValueAt(row, 0);
        String ngayHenStr = (String) modelLichHen.getValueAt(row, 1);
        String tenKhach = (String) modelLichHen.getValueAt(row, 2);
        String sdt = (String) modelLichHen.getValueAt(row, 3);
        String tenThuCung = (String) modelLichHen.getValueAt(row, 4);
        String tenDichVu = (String) modelLichHen.getValueAt(row, 5);
        String tenKtv = (String) modelLichHen.getValueAt(row, 6);
        String ghiChu = (String) modelLichHen.getValueAt(row, 8);

        String email = "";
        try {
            entity.KhachHang kh = new bus.KhachHangBus().timKhachHangTheoSoDienThoai(sdt);
            if (kh != null && kh.getEmail() != null && !kh.getEmail().isEmpty()) {
                email = kh.getEmail();
            } else {
                email = "khachhang.an@gmail.com";
            }
        } catch (Exception ignored) {
            email = "khachhang.an@gmail.com";
        }

        String tieuDe = "[Mio Spa] Nhắc Hẹn Chăm Sóc Bé " + tenThuCung + " (" + ngayHenStr + ")";
        String html = utils.EmailThongBaoHelper.taoEmailXacNhanLichHen(
                tenKhach, sdt, maLH, tenDichVu, tenThuCung, tenKtv, ngayHenStr, ghiChu);

        utils.EmailThongBaoHelper.moHopThoaiGuiEmail(SwingUtilities.getWindowAncestor(this), email, tieuDe, html);
    }

}
