package views;

import controllers.ThuCungController;
import models.entity.ChiTietLichHen;
import models.entity.LichHen;
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

    private final ThuCungController controller;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    // KPI Summary Labels
    private JLabel lblCountCho;
    private JLabel lblCountDangLam;
    private JLabel lblCountXong;
    private JLabel lblCountHuy;

    // Filter controls
    private JTextField txtTimKiem;
    private String currentStatusFilter = "TẤT CẢ";
    private final List<JButton> statusFilterButtons = new ArrayList<>();

    // Table
    private JTable tblLichHen;
    private DefaultTableModel modelLichHen;
    private List<LichHen> currentList = new ArrayList<>();

    public QuanLyLichHenPanel() {
        this(new ThuCungController());
    }

    public QuanLyLichHenPanel(ThuCungController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setBackground(ThemeColors.BACKGROUND);

        initUI();
        taiDuLieuLichHen();
    }

    private void initUI() {
        // 1. Phía trên: 4 Thẻ KPI Tóm tắt trạng thái + Thanh tìm kiếm & Bộ lọc
        JPanel pnlTop = new JPanel(new BorderLayout(0, 10));
        pnlTop.setOpaque(false);

        pnlTop.add(taoPanelKpiSummary(), BorderLayout.NORTH);
        pnlTop.add(taoPanelToolbar(), BorderLayout.SOUTH);

        add(pnlTop, BorderLayout.NORTH);

        // 2. Ở giữa: Bảng danh sách Lịch hẹn dịch vụ
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
        pnl.add(taoKpiCard("Đã hủy lịch", lblCountHuy, IconHelper.TRASH, new Color(0xF1, 0xF5, 0xF9), ThemeColors.TEXT_MUTED));

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
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(10, 14, 10, 14));

        // Bên trái: Ô tìm kiếm & Bộ lọc trạng thái
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        pnlLeft.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(220, 34));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo khách, SĐT, mã LH...");
        txtTimKiem.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 14, ThemeColors.TEXT_MUTED));
        txtTimKiem.setBorder(ThemeColors.createInputBorder());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override
            public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override
            public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });

        pnlLeft.add(txtTimKiem);

        String[] statuses = {"TẤT CẢ", "Đã đặt", "Đang phục vụ", "Hoàn thành", "Hủy"};
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

        // Phân quyền: NVCS không cần nút đặt lịch mới, chỉ xem và làm việc
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 20, 32));
        btn.addActionListener(e -> {
            currentStatusFilter = st;
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
                        new EmptyBorder(4, 12, 4, 12)
                ));
            } else {
                btn.setBackground(ThemeColors.SURFACE);
                btn.setForeground(ThemeColors.TEXT_SECONDARY);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(4, 12, 4, 12)
                ));
            }
        }
    }

    private JPanel taoPanelTable() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 14, 12, 14));

        String[] cols = {"Mã LH", "Thời gian hẹn", "Khách hàng", "Số điện thoại", "Thú cưng", "Dịch vụ yêu cầu", "KTV Phụ trách", "Trạng thái", "Ghi chú"};
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

        // Căn giữa cột Mã LH và Thời gian
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblLichHen.getColumnModel().getColumn(0).setPreferredWidth(70);
        tblLichHen.getColumnModel().getColumn(0).setCellRenderer(center);
        tblLichHen.getColumnModel().getColumn(1).setPreferredWidth(130);
        tblLichHen.getColumnModel().getColumn(1).setCellRenderer(center);

        // Renderer Badge màu sắc cho cột Trạng thái
        tblLichHen.getColumnModel().getColumn(7).setPreferredWidth(110);
        tblLichHen.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String st = value != null ? value.toString() : "";
                if ("Đang phục vụ".equalsIgnoreCase(st)) {
                    lbl.setBackground(ThemeColors.APPOINTMENT_DOING_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_DOING_TEXT);
                } else if ("Hoàn thành".equalsIgnoreCase(st)) {
                    lbl.setBackground(ThemeColors.APPOINTMENT_DONE_BG);
                    lbl.setForeground(ThemeColors.APPOINTMENT_DONE_TEXT);
                } else if ("Hủy".equalsIgnoreCase(st) || "Đã hủy".equalsIgnoreCase(st)) {
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
        return pnl;
    }

    private JPanel taoPanelThaoTacTheoVaiTro() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);

        // Bên trái: Hướng dẫn theo vai trò
        String roleDesc = UserSession.getRoleDescription();
        JLabel lblRoleHint = new JLabel("Vai trò hiện tại: " + roleDesc,
                IconHelper.getPrimaryIcon(IconHelper.USER_CHECK, 16), SwingConstants.LEFT);
        lblRoleHint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblRoleHint.setForeground(ThemeColors.TEXT_SECONDARY);

        // Bên phải: Các nút hành động nghiệp vụ
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        // Nhóm nút dành cho Kỹ thuật viên (NVCS)
        JButton btnNhanViec = ThemeColors.createSecondaryButton("Bắt đầu làm (Đang phục vụ)",
                IconHelper.getIcon(IconHelper.SPARKLES, 13, ThemeColors.PRIMARY_ACTIVE));
        JButton btnHoanThanh = ThemeColors.createSecondaryButton("Hoàn thành dịch vụ",
                IconHelper.getIcon(IconHelper.CHECK, 13, ThemeColors.SUCCESS));

        // Nhóm nút dành cho Thu ngân (NVTN) & Quản lý (NVQL)
        JButton btnChuyenHoaDon = ThemeColors.createPrimaryButton("Chuyển thành Hóa đơn POS",
                IconHelper.getIcon(IconHelper.CREDIT_CARD, 14, Color.WHITE));
        JButton btnHuyLich = ThemeColors.createSecondaryButton("Hủy lịch hẹn",
                IconHelper.getIcon(IconHelper.TRASH, 13, ThemeColors.DANGER));

        btnNhanViec.addActionListener(e -> capNhatTrangThaiChon("Đang phục vụ"));
        btnHoanThanh.addActionListener(e -> capNhatTrangThaiChon("Hoàn thành"));
        btnHuyLich.addActionListener(e -> capNhatTrangThaiChon("Hủy"));
        btnChuyenHoaDon.addActionListener(e -> chuyenSangHoaDonPOS());

        // Phân quyền hiển thị theo UserSession
        pnlActions.add(btnNhanViec);
        pnlActions.add(btnHoanThanh);

        if (UserSession.isQuanLy() || UserSession.isThuNgan()) {
            pnlActions.add(btnHuyLich);
            pnlActions.add(btnChuyenHoaDon);
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
        capNhatSoLuongKpi();
        apDungBoLoc();
    }

    private void capNhatSoLuongKpi() {
        int cho = 0, dangLam = 0, xong = 0, huy = 0;
        for (LichHen lh : currentList) {
            String st = lh.getTrangThaiLichHen();
            if ("Đang phục vụ".equalsIgnoreCase(st)) dangLam++;
            else if ("Hoàn thành".equalsIgnoreCase(st)) xong++;
            else if ("Hủy".equalsIgnoreCase(st) || "Đã hủy".equalsIgnoreCase(st)) huy++;
            else cho++;
        }
        lblCountCho.setText(String.valueOf(cho));
        lblCountDangLam.setText(String.valueOf(dangLam));
        lblCountXong.setText(String.valueOf(xong));
        lblCountHuy.setText(String.valueOf(huy));
    }

    private void apDungBoLoc() {
        modelLichHen.setRowCount(0);
        String kw = txtTimKiem != null ? txtTimKiem.getText().trim().toLowerCase() : "";

        for (LichHen lh : currentList) {
            boolean matchSt = "TẤT CẢ".equalsIgnoreCase(currentStatusFilter) ||
                    lh.getTrangThaiLichHen().equalsIgnoreCase(currentStatusFilter);

            String khTen = lh.getKhachHang() != null ? lh.getKhachHang().getHoTen() : "";
            String sdt = lh.getKhachHang() != null ? lh.getKhachHang().getSoDienThoai() : "";
            boolean matchKw = kw.isEmpty() ||
                    lh.getMaLichHen().toLowerCase().contains(kw) ||
                    khTen.toLowerCase().contains(kw) ||
                    sdt.contains(kw);

            if (matchSt && matchKw) {
                String tenThuCung = "Thú cưng";
                String tenDichVu = "Dịch vụ chăm sóc";
                String tenKtv = lh.getNhanVienTiepNhan() != null ? lh.getNhanVienTiepNhan().getHoTen() : "Chưa chỉ định";

                if (lh.getDsChiTiet() != null && !lh.getDsChiTiet().isEmpty()) {
                    ChiTietLichHen ct = lh.getDsChiTiet().get(0);
                    if (ct.getThuCung() != null) tenThuCung = ct.getThuCung().getTenThuCung();
                    if (ct.getDichVu() != null) tenDichVu = ct.getDichVu().getTenDichVu();
                    if (ct.getKtvChiDinh() != null) tenKtv = ct.getKtvChiDinh().getHoTen();
                }

                modelLichHen.addRow(new Object[]{
                        lh.getMaLichHen(),
                        lh.getNgayHen() != null ? lh.getNgayHen().format(dtf) : "",
                        khTen,
                        sdt,
                        tenThuCung,
                        tenDichVu,
                        tenKtv,
                        lh.getTrangThaiLichHen(),
                        lh.getGhiChu()
                });
            }
        }
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
        String dvTen = (String) modelLichHen.getValueAt(row, 5);
        String trangThai = (String) modelLichHen.getValueAt(row, 7);

        if (!"Hoàn thành".equalsIgnoreCase(trangThai)) {
            int opt = JOptionPane.showConfirmDialog(this,
                    "Lịch hẹn này chưa ở trạng thái 'Hoàn thành'. Bạn có chắc muốn chuyển thẳng sang thanh toán ngay?",
                    "Xác nhận thanh toán trước", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
        }

        String msg = String.format(
                "ĐÃ CHUYỂN LỊCH HẸN SANG MÀN HÌNH BÁN HÀNG POS!\n\n" +
                        "• Mã lịch hẹn: %s\n" +
                        "• Khách hàng: %s\n" +
                        "• Dịch vụ: %s\n\n" +
                        "Thu ngân có thể kiểm tra giỏ hàng tại tab 'Bán hàng POS' để chốt thanh toán.",
                maLH, khTen, dvTen
        );

        JOptionPane.showMessageDialog(this, msg, "Chuyển Hóa đơn thành công", JOptionPane.INFORMATION_MESSAGE);
    }
}
