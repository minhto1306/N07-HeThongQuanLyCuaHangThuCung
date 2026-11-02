package views;

import controllers.DangNhapController;
import models.dao.BangGiaoCaDAO;
import models.dao.NhanVienDAO;
import models.dao.TaiKhoanDAO;
import models.entity.BangGiaoCa;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import models.entity.TaiKhoan;
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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel Bàn giao ca làm việc & Đối soát két tiền mặt giữa Thu ngân và Quản lý.
 * Hỗ trợ đầy đủ 2 kịch bản thực tế:
 * 1. Xác thực Kép tại quầy (Dual-Control): Thu ngân chốt ca -> Quản lý (admin) hoặc ca sau nhập mật khẩu xác nhận ngay tại chỗ -> In biên bản K80.
 * 2. Bàn giao chờ duyệt (Async Handover): Thu ngân gửi phiếu "⏳ Chờ QL duyệt" rồi đăng xuất -> Quản lý (admin) đăng nhập vào sau và bấm "✅ Quản lý Duyệt & Xác nhận Két".
 */
public class BanGiaoCaPanel extends JPanel {

    private static final long TIEN_BAN_GIAO_MAU = 6_500_000L;

    private final DecimalFormat df = new DecimalFormat("#,### đ");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JComboBox<String> cboCaLamViec;
    private JLabel lblThuNganHienTai;
    private JLabel lblVaiTroHienTai;
    private JTextField txtTienDauCa;
    private JLabel lblTienHeThong;
    private JLabel lblTienChuyenKhoan;
    private JLabel lblTienHoanTra;
    private JLabel lblTienDuKien;
    private JTextField txtTienThucTe;
    private JLabel lblChenhLech;
    private JTextArea txtLyDoChenhLech;

    private JTable tblLichSu;
    private DefaultTableModel modelLichSu;
    private JButton btnQuanLyDuyet;
    private JButton btnChotCa;

    private long tienDauCa = TIEN_BAN_GIAO_MAU;
    private long tienHeThongGhiNhan = 0L;
    private long tienChuyenKhoanGhiNhan = 0L;
    private long tienHoanTraKhach = 0L;
    private boolean dangCapNhatTuDong = false;

    public BanGiaoCaPanel() {
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(14, 18, 14, 18));
        setBackground(ThemeColors.BACKGROUND);

        JPanel pnlLeft = taoPanelBienBanKetCa();
        pnlLeft.setPreferredSize(new Dimension(500, 0));

        JPanel pnlRight = taoPanelLichSuGiaoCa();

        add(pnlLeft, BorderLayout.WEST);
        add(pnlRight, BorderLayout.CENTER);

        lamMoiDuLieuCaHienTai();
    }

    private JPanel taoPanelBienBanKetCa() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        // Xếp tiêu đề và badge theo chiều dọc để nội dung dài không đè lên nhau
        // khi panel bên trái bị thu hẹp hoặc Windows đang dùng display scaling.
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("BIÊN BẢN ĐỐI SOÁT & BÀN GIAO KÉT TIỀN",
                IconHelper.getPrimaryIcon(IconHelper.CLIPBOARD_CHECK, 18), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblVaiTroHienTai = ThemeColors.createBadge(UserSession.getRoleCode(), ThemeColors.PRIMARY_SOFT, ThemeColors.PRIMARY_ACTIVE);
        JPanel pnlRoleBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlRoleBadge.setOpaque(false);
        pnlRoleBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlRoleBadge.add(lblVaiTroHienTai);

        pnlHeader.add(lblTitle);
        pnlHeader.add(Box.createVerticalStrut(6));
        pnlHeader.add(pnlRoleBadge);
        pnl.add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dòng 0: Ca làm việc
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.42;
        pnlForm.add(taoLabelForm("Ca làm việc cần chốt:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.58;
        cboCaLamViec = new JComboBox<>(new String[]{
                "CA01 - Ca Sáng (07:30 - 12:00)",
                "CA02 - Ca Chiều (12:00 - 17:30)",
                "CA03 - Ca Tối (17:30 - 22:00)"
        });
        cboCaLamViec.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cboCaLamViec.setPreferredSize(new Dimension(210, 30));
        chonCaMacDinhTheoGio();
        pnlForm.add(cboCaLamViec, gbc);

        // Dòng 1: Nhân sự đang trực
        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(taoLabelForm("Nhân viên đang trực:"), gbc);

        gbc.gridx = 1;
        lblThuNganHienTai = new JLabel(layChuoiNhanVienHienTai());
        lblThuNganHienTai.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThuNganHienTai.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlForm.add(lblThuNganHienTai, gbc);

        // Dòng 2: Tiền đầu ca kế thừa từ ca vừa bàn giao
        gbc.gridx = 0; gbc.gridy = 2;
        pnlForm.add(taoLabelForm("(1) Tiền đầu ca kế thừa:"), gbc);

        gbc.gridx = 1;
        txtTienDauCa = new JTextField(String.valueOf(TIEN_BAN_GIAO_MAU));
        txtTienDauCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTienDauCa.setPreferredSize(new Dimension(160, 30));
        txtTienDauCa.setBorder(ThemeColors.createInputBorder());
        txtTienDauCa.setEditable(false);
        txtTienDauCa.setFocusable(false);
        txtTienDauCa.setBackground(ThemeColors.PRIMARY_TINT);
        txtTienDauCa.setToolTipText("Tự động lấy từ tiền thực tế của ca vừa bàn giao gần nhất");
        pnlForm.add(txtTienDauCa, gbc);

        // Dòng 3: Doanh thu tiền mặt hệ thống
        gbc.gridx = 0; gbc.gridy = 3;
        pnlForm.add(taoLabelForm("(2) (+) Thu Tiền mặt bán hàng:"), gbc);

        gbc.gridx = 1;
        lblTienHeThong = new JLabel("0 đ");
        lblTienHeThong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTienHeThong.setForeground(ThemeColors.PRIMARY_ACTIVE);
        pnlForm.add(lblTienHeThong, gbc);

        // Dòng 4: Doanh thu Chuyển khoản / QR / Thẻ (Tham khảo sao kê)
        gbc.gridx = 0; gbc.gridy = 4;
        pnlForm.add(taoLabelForm("(i) Doanh thu QR / VNPay / Thẻ:"), gbc);

        gbc.gridx = 1;
        lblTienChuyenKhoan = new JLabel("0 đ (Vào TK Ngân hàng)");
        lblTienChuyenKhoan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblTienChuyenKhoan.setForeground(new Color(0x02, 0x84, 0xC7));
        pnlForm.add(lblTienChuyenKhoan, gbc);

        // Dòng 5: Tiền hoàn trả khách (Đổi/Trả hàng)
        gbc.gridx = 0; gbc.gridy = 5;
        pnlForm.add(taoLabelForm("(3) (-) Chi hoàn tiền Đổi/Trả:"), gbc);

        gbc.gridx = 1;
        lblTienHoanTra = new JLabel("-0 đ");
        lblTienHoanTra.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTienHoanTra.setForeground(ThemeColors.DANGER);
        pnlForm.add(lblTienHoanTra, gbc);

        // Dòng 6: Tổng tiền mặt lý thuyết phải có trong két = (1) + (2) - (3)
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel lblLtTitle = new JLabel("(=) TIỀN MẶT LÝ THUYẾT KÉT:");
        lblLtTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLtTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        pnlForm.add(lblLtTitle, gbc);

        gbc.gridx = 1;
        lblTienDuKien = new JLabel("1,000,000 đ");
        lblTienDuKien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTienDuKien.setForeground(ThemeColors.SUCCESS_TEXT);
        pnlForm.add(lblTienDuKien, gbc);

        // Dòng 7: Tiền thực tế do thu ngân tự kiểm đếm
        gbc.gridx = 0; gbc.gridy = 7;
        JLabel lblTtTitle = new JLabel("Tiền mặt thực tế kiểm đếm (*):");
        lblTtTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTtTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTtTitle.setPreferredSize(new Dimension(210, 24));
        lblTtTitle.setMinimumSize(new Dimension(210, 24));
        pnlForm.add(lblTtTitle, gbc);

        gbc.gridx = 1;
        txtTienThucTe = new JTextField("");
        txtTienThucTe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTienThucTe.setPreferredSize(new Dimension(135, 32));
        txtTienThucTe.setBorder(ThemeColors.createInputBorder());
        txtTienThucTe.setToolTipText("Nhập tổng số tiền mặt thực tế vừa kiểm đếm trong két");
        txtTienThucTe.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { tinhChenhLech(); }
            @Override public void removeUpdate(DocumentEvent e) { tinhChenhLech(); }
            @Override public void changedUpdate(DocumentEvent e) { tinhChenhLech(); }
        });
        pnlForm.add(txtTienThucTe, gbc);

        // Dòng 8: Chênh lệch
        gbc.gridx = 0; gbc.gridy = 8;
        pnlForm.add(taoLabelForm("Chênh lệch (Thực tế - Lý thuyết):"), gbc);

        gbc.gridx = 1;
        lblChenhLech = new JLabel("Chưa kiểm đếm tiền mặt");
        lblChenhLech.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblChenhLech.setForeground(ThemeColors.TEXT_MUTED);
        pnlForm.add(lblChenhLech, gbc);

        // Dòng 9: Lý do giải trình / Ghi chú bàn giao
        gbc.gridx = 0; gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        pnlForm.add(taoLabelForm("Ghi chú / Giải trình lệch két:"), gbc);

        gbc.gridx = 1;
        txtLyDoChenhLech = new JTextArea(2, 20);
        txtLyDoChenhLech.setLineWrap(true);
        txtLyDoChenhLech.setWrapStyleWord(true);
        txtLyDoChenhLech.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLyDoChenhLech.setBorder(new EmptyBorder(4, 6, 4, 6));
        JScrollPane spLyDo = new JScrollPane(txtLyDoChenhLech);
        spLyDo.setBorder(ThemeColors.createInputBorder());
        pnlForm.add(spLyDo, gbc);

        // Khung hướng dẫn quy trình bàn giao ca & duyệt két
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        JLabel lblWorkflowGuide = new JLabel(
                "<html><div style='background:#FFFBEB; border:1px solid #FDE68A; padding:6px 8px; border-radius:6px; font-size:10px; color:#92400E;'>" +
                "<b>Quy trình chuẩn:</b> Thu ngân kiểm tiền & bấm <b>Chốt ca & Bàn giao</b>:<br>" +
                "• <b>Thu ngân:</b> Lưu biên bản trạng thái <i>⏳ Chờ Quản lý duyệt</i> và đăng xuất đổi ca.<br>" +
                "• <b>Quản lý:</b> Đăng nhập tài khoản Quản lý (NVQL) để đối soát số liệu và bấm <b>Duyệt Két</b> bên phải." +
                "</div></html>"
        );
        pnlForm.add(lblWorkflowGuide, gbc);

        pnl.add(pnlForm, BorderLayout.CENTER);

        // Cụm nút hành động dưới cùng
        JPanel pnlButtons = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlButtons.setOpaque(false);

        btnChotCa = new JButton("CHỐT CA & BÀN GIAO KÉT TIỀN",
                IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 16, Color.WHITE));
        btnChotCa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChotCa.setBackground(ThemeColors.PRIMARY);
        btnChotCa.setForeground(Color.WHITE);
        btnChotCa.setPreferredSize(new Dimension(0, 40));
        btnChotCa.setFocusPainted(false);
        btnChotCa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChotCa.setIconTextGap(8);
        btnChotCa.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));
        btnChotCa.addActionListener(e -> xacNhanBanGiaoCa());
        JButton btnXacNhan = btnChotCa;

        JPanel pnlSubBtns = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlSubBtns.setOpaque(false);

        JButton btnLamMoi = ThemeColors.createSecondaryButton("Làm mới doanh thu ca",
                IconHelper.getIcon(IconHelper.REFRESH, 13, ThemeColors.TEXT_SECONDARY));
        btnLamMoi.addActionListener(e -> {
            lamMoiDuLieuCaHienTai();
            JOptionPane.showMessageDialog(this, "Đã đồng bộ lại doanh thu Tiền mặt, Chuyển khoản và Hoàn trả mới nhất từ CSDL!", "Đã cập nhật", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnDangXuatNhanh = ThemeColors.createSecondaryButton("Đăng xuất đổi ca",
                IconHelper.getIcon(IconHelper.LOG_OUT, 13, ThemeColors.DANGER));
        btnDangXuatNhanh.addActionListener(e -> thucHienDangXuatChuyenCa());

        pnlSubBtns.add(btnLamMoi);
        pnlSubBtns.add(btnDangXuatNhanh);

        pnlButtons.add(btnXacNhan);
        pnlButtons.add(pnlSubBtns);
        pnl.add(pnlButtons, BorderLayout.SOUTH);

        return pnl;
    }

    private JLabel taoLabelForm(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(ThemeColors.TEXT_SECONDARY);
        // Giữ đủ chỗ cho các nhãn dài khi Windows dùng display scaling lớn.
        lbl.setPreferredSize(new Dimension(210, 24));
        lbl.setMinimumSize(new Dimension(210, 24));
        return lbl;
    }

    private void chonCaMacDinhTheoGio() {
        if (cboCaLamViec == null) return;
        int hour = LocalTime.now().getHour();
        if (hour < 12) {
            cboCaLamViec.setSelectedIndex(0);
        } else if (hour < 17) {
            cboCaLamViec.setSelectedIndex(1);
        } else {
            cboCaLamViec.setSelectedIndex(2);
        }
    }

    private String layChuoiNhanVienHienTai() {
        String ten = UserSession.getFullName();
        String ma = UserSession.getUserId();
        String role = UserSession.getRoleCode();
        return ten + " (" + ma + " - " + role + ")";
    }

    private JPanel taoPanelLichSuGiaoCa() {
        JPanel pnl = new JPanel(new BorderLayout(8, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        // Đặt dòng hướng dẫn dưới tiêu đề thay vì ép sang mép phải. Cách này
        // tránh hai label chồng nhau ở cửa sổ nhỏ hoặc khi font được scale lớn.
        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ BÀN GIAO & XÁC NHẬN ĐỐI SOÁT CA",
                IconHelper.getIcon(IconHelper.CLOCK, 18, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHint = new JLabel("Chọn một dòng bên dưới để Quản lý duyệt hoặc in biên bản K80", SwingConstants.LEFT);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(ThemeColors.TEXT_MUTED);
        lblHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlTop.add(lblTitle);
        pnlTop.add(Box.createVerticalStrut(4));
        pnlTop.add(lblHint);
        pnl.add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"Mã GC", "Ca", "Thu ngân giao", "Đầu ca", "Lý thuyết két", "Thực tế đếm", "Chênh lệch", "Thời điểm chốt", "Trạng thái & Ghi chú đối soát"};
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
        tblLichSu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Không ép co các cột đến mức mất nội dung. Khi cửa sổ hẹp, bảng sẽ
        // xuất hiện thanh cuộn ngang thay vì cắt tiêu đề và ghi chú đối soát.
        tblLichSu.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tblLichSu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblLichSu.getTableHeader().setBackground(ThemeColors.PRIMARY_TINT);
        tblLichSu.getTableHeader().setForeground(ThemeColors.TEXT_PRIMARY);
        tblLichSu.getTableHeader().setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        tblLichSu.getColumnModel().getColumn(0).setPreferredWidth(58);
        tblLichSu.getColumnModel().getColumn(0).setCellRenderer(center);
        tblLichSu.getColumnModel().getColumn(1).setPreferredWidth(65);
        tblLichSu.getColumnModel().getColumn(1).setCellRenderer(center);
        tblLichSu.getColumnModel().getColumn(2).setPreferredWidth(115);
        tblLichSu.getColumnModel().getColumn(3).setPreferredWidth(105);
        tblLichSu.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblLichSu.getColumnModel().getColumn(5).setPreferredWidth(115);
        tblLichSu.getColumnModel().getColumn(6).setPreferredWidth(105);
        tblLichSu.getColumnModel().getColumn(7).setPreferredWidth(135);
        tblLichSu.getColumnModel().getColumn(8).setPreferredWidth(310);
        tblLichSu.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String val = value != null ? value.toString() : "";
                if (val.contains("Chờ") || val.contains("⏳")) {
                    lbl.setForeground(new Color(0xD9, 0x77, 0x06));
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else if (val.contains("Đã duyệt") || val.contains("✓")) {
                    lbl.setForeground(ThemeColors.SUCCESS_TEXT);
                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                } else {
                    lbl.setForeground(ThemeColors.TEXT_PRIMARY);
                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                }
                return lbl;
            }
        });

        dongBoLichSuGiaoCaTuDatabase();

        pnl.add(new JScrollPane(tblLichSu), BorderLayout.CENTER);

        // Thanh tác vụ của Quản lý & In biên bản ở góc dưới bên phải
        JPanel pnlRightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightActions.setOpaque(false);

        btnQuanLyDuyet = new JButton("DUYỆT & XÁC NHẬN CA ĐANG CHỌN",
                IconHelper.getIcon(IconHelper.SHIELD, 15, Color.WHITE));
        btnQuanLyDuyet.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuanLyDuyet.setBackground(new Color(0x05, 0x96, 0x69));
        btnQuanLyDuyet.setForeground(Color.WHITE);
        btnQuanLyDuyet.setFocusPainted(false);
        btnQuanLyDuyet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuanLyDuyet.setPreferredSize(new Dimension(310, 38));
        btnQuanLyDuyet.setToolTipText("Quản lý duyệt và xác nhận két của ca đang chọn");
        btnQuanLyDuyet.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x04, 0x78, 0x57), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btnQuanLyDuyet.addActionListener(e -> duyetBienBanGiaoCaDangChon());
        btnQuanLyDuyet.setVisible(UserSession.isQuanLy());

        JButton btnInBienBan = ThemeColors.createSecondaryButton("Xem / In Biên Bản K80",
                IconHelper.getIcon(IconHelper.RECEIPT, 14, ThemeColors.PRIMARY_ACTIVE));
        btnInBienBan.setPreferredSize(new Dimension(190, 38));
        btnInBienBan.addActionListener(e -> xemVaInBienBanK80DangChon());

        pnlRightActions.add(btnInBienBan);
        pnlRightActions.add(btnQuanLyDuyet);
        pnl.add(pnlRightActions, BorderLayout.SOUTH);

        return pnl;
    }

    /**
     * Đồng bộ số liệu doanh thu thực tế trong ngày từ SQL Server và cập nhật thông tin người đang đăng nhập.
     */
    public void lamMoiDuLieuCaHienTai() {
        dangCapNhatTuDong = true;
        try {
            if (lblThuNganHienTai != null) {
                lblThuNganHienTai.setText(layChuoiNhanVienHienTai());
            }
            if (lblVaiTroHienTai != null) {
                boolean isQL = UserSession.getCurrentRole() == UserSession.Role.NVQL;
                lblVaiTroHienTai.setText(isQL ? "QUẢN LÝ (CÓ QUYỀN DUYỆT KÉT)" : "THU NGÂN TRỰC CA");
            }
            if (btnQuanLyDuyet != null) {
                btnQuanLyDuyet.setVisible(UserSession.isQuanLy());
            }

            BangGiaoCaDAO dao = new BangGiaoCaDAO();
            try {
                BangGiaoCa caBanGiaoGanNhat = dao.getCaBanGiaoGanNhat();
                if (caBanGiaoGanNhat != null) {
                    tienDauCa = caBanGiaoGanNhat.getTienCuoiCaThucTe() != null
                            ? caBanGiaoGanNhat.getTienCuoiCaThucTe().longValue()
                            : TIEN_BAN_GIAO_MAU;
                    LocalDateTime mocBatDauCa = caBanGiaoGanNhat.getGioKetThucThucTe();
                    tienHeThongGhiNhan = dao.tinhDoanhThuTienMatTuThoiDiem(mocBatDauCa);
                    tienChuyenKhoanGhiNhan = dao.tinhDoanhThuChuyenKhoanTuThoiDiem(mocBatDauCa);
                    tienHoanTraKhach = dao.tinhTongTienHoanTraTuThoiDiem(mocBatDauCa);
                } else {
                    tienDauCa = TIEN_BAN_GIAO_MAU;
                    LocalDateTime dauNgay = java.time.LocalDate.now().atStartOfDay();
                    tienHeThongGhiNhan = dao.tinhDoanhThuTienMatTuThoiDiem(dauNgay);
                    tienChuyenKhoanGhiNhan = dao.tinhDoanhThuChuyenKhoanTuThoiDiem(dauNgay);
                    tienHoanTraKhach = dao.tinhTongTienHoanTraTuThoiDiem(dauNgay);
                }
            } catch (Exception e) {
                // Dữ liệu mẫu giữ cho màn hình demo nhất quán khi CSDL chưa sẵn sàng.
                tienDauCa = TIEN_BAN_GIAO_MAU;
                tienHeThongGhiNhan = 0L;
                tienChuyenKhoanGhiNhan = 0L;
                tienHoanTraKhach = 0L;
                System.err.println("[BanGiaoCaPanel] Không lấy được tiền bàn giao ca gần nhất: " + e.getMessage());
            }
            long tongLyThuyet = tinhTongLyThuyetHienTai();

            if (txtTienDauCa != null) {
                txtTienDauCa.setText(String.valueOf(tienDauCa));
            }
            if (lblTienHeThong != null) {
                lblTienHeThong.setText("+" + df.format(tienHeThongGhiNhan));
            }
            if (lblTienChuyenKhoan != null) {
                lblTienChuyenKhoan.setText(df.format(tienChuyenKhoanGhiNhan) + " (Vào TK Ngân hàng)");
            }
            if (lblTienHoanTra != null) {
                lblTienHoanTra.setText("-" + df.format(tienHoanTraKhach));
            }
            if (lblTienDuKien != null) {
                lblTienDuKien.setText(df.format(tongLyThuyet));
            }

            dongBoLichSuGiaoCaTuDatabase();
        } finally {
            dangCapNhatTuDong = false;
        }
        tinhChenhLech();
    }

    private long tinhTongLyThuyetHienTai() {
        return Math.max(0L, tienDauCa + tienHeThongGhiNhan - tienHoanTraKhach);
    }

    private long parseSoTien(String raw, long defaultVal) {
        if (raw == null) return defaultVal;
        String clean = raw.replaceAll("[^0-9]", "").trim();
        if (clean.isEmpty()) return defaultVal;
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void tinhChenhLech() {
        if (lblChenhLech == null || txtTienThucTe == null) return;
        try {
            long lyThuyet = tinhTongLyThuyetHienTai();
            String text = txtTienThucTe.getText().replaceAll("[^0-9]", "").trim();
            if (text.isEmpty()) {
                lblChenhLech.setText("Chưa kiểm đếm tiền mặt");
                lblChenhLech.setForeground(ThemeColors.TEXT_MUTED);
                return;
            }
            long thucTe = Long.parseLong(text);
            long chenhLech = thucTe - lyThuyet;

            if (chenhLech == 0) {
                lblChenhLech.setText("0 đ (Khớp két 100%)");
                lblChenhLech.setForeground(ThemeColors.SUCCESS);
            } else if (chenhLech > 0) {
                lblChenhLech.setText("+" + df.format(chenhLech) + " (Thừa tiền mặt)");
                lblChenhLech.setForeground(ThemeColors.WARNING);
            } else {
                lblChenhLech.setText(df.format(chenhLech) + " (Thiếu tiền mặt)");
                lblChenhLech.setForeground(ThemeColors.DANGER);
            }
        } catch (NumberFormatException e) {
            lblChenhLech.setText("Số tiền không hợp lệ");
            lblChenhLech.setForeground(ThemeColors.DANGER);
        }
    }

    public void dongBoLichSuGiaoCaTuDatabase() {
        if (modelLichSu == null) return;
        modelLichSu.setRowCount(0);
        try {
            BangGiaoCaDAO dao = new BangGiaoCaDAO();
            List<BangGiaoCa> dbList = dao.getAllBangGiaoCa();
            if (dbList != null && !dbList.isEmpty()) {
                for (BangGiaoCa bgc : dbList) {
                    String maCa = bgc.getCaLamViec() != null && bgc.getCaLamViec().getMaCa() != null ? bgc.getCaLamViec().getMaCa() : "CA01";
                    String tenNV = bgc.getNhanVien() != null && bgc.getNhanVien().getHoTen() != null ? bgc.getNhanVien().getHoTen() : "Lê Anh Minh";
                    long dauCa = bgc.getTienDauCa() != null ? bgc.getTienDauCa().longValue() : 0L;
                    long ht = bgc.getTienCuoiCaHeThong() != null ? bgc.getTienCuoiCaHeThong().longValue() : 0L;
                    long tt = bgc.getTienCuoiCaThucTe() != null ? bgc.getTienCuoiCaThucTe().longValue() : 0L;
                    long cl = bgc.getTienChenhLech() != null ? bgc.getTienChenhLech().longValue() : 0L;
                    String gioKT = bgc.getGioKetThucThucTe() != null ? bgc.getGioKetThucThucTe().format(dtf) : "Đang mở";
                    String ghiChu = taoMoTaTrangThai(bgc);
                    modelLichSu.addRow(new Object[]{
                            bgc.getMaGiaoCa(), maCa, tenNV,
                            df.format(dauCa), df.format(ht), df.format(tt), df.format(cl),
                            gioKT, ghiChu
                    });
                }
                if (tblLichSu != null && modelLichSu.getRowCount() > 0) {
                    tblLichSu.setRowSelectionInterval(0, 0);
                }
                return;
            }
        } catch (Exception e) {
            System.err.println("[BanGiaoCaPanel] Lỗi tải lịch sử giao ca CSDL: " + e.getMessage());
        }
        // Dữ liệu demo theo chuỗi bàn giao liên tục, mới nhất luôn nằm trên cùng.
        modelLichSu.addRow(new Object[]{"GC003", "CA03", "Lê Khải Minh", "4,580,000 đ", "6,500,000 đ", "6,500,000 đ", "0 đ", "02/11/2026 22:00", "✓ Đã duyệt (QL: Trần Quản Lý) | Khớp két 100%"});
        modelLichSu.addRow(new Object[]{"GC002", "CA02", "Trần Thị Thu Hà", "3,500,000 đ", "4,600,000 đ", "4,580,000 đ", "-20,000 đ", "02/11/2026 17:30", "✓ Đã duyệt (QL: Trần Quản Lý) | Thối nhầm 20.000 đ"});
        modelLichSu.addRow(new Object[]{"GC001", "CA01", "Lê Anh Minh", "1,000,000 đ", "3,500,000 đ", "3,500,000 đ", "0 đ", "02/11/2026 12:00", "✓ Đã duyệt (QL: Trần Quản Lý) | Khớp két 100%"});
        if (tblLichSu != null) {
            tblLichSu.setRowSelectionInterval(0, 0);
        }
    }

    private String taoMoTaTrangThai(BangGiaoCa bgc) {
        String lyDo = bgc.getLyDoChenhLech() != null ? bgc.getLyDoChenhLech().trim() : "";
        if (lyDo.isEmpty()) lyDo = "Khớp két 100%";
        if ("DA_DUYET".equals(bgc.getTrangThai())) {
            String tenQuanLy = bgc.getQuanLyDuyet() != null && bgc.getQuanLyDuyet().getHoTen() != null
                    ? bgc.getQuanLyDuyet().getHoTen() : "Không rõ";
            return "✓ Đã duyệt (QL: " + tenQuanLy + ") | " + lyDo;
        }
        if ("CHO_DUYET".equals(bgc.getTrangThai())) return "⏳ Chờ Quản lý duyệt | " + lyDo;
        return "Đang mở | " + lyDo;
    }

    public void thucHienGiaoCa() {
        xacNhanBanGiaoCa();
    }

    /**
     * BƯỚC CHỐT CA & BÀN GIAO KÉT TIỀN:
     * Thu ngân chốt ca lưu biên bản trạng thái "⏳ Chờ Quản lý duyệt" vào CSDL và gợi ý đăng xuất.
     */
    private void xacNhanBanGiaoCa() {
        tinhChenhLech();
        long lyThuyet = tinhTongLyThuyetHienTai();
        String tienThucTeRaw = txtTienThucTe.getText() != null
                ? txtTienThucTe.getText().replaceAll("[^0-9]", "").trim()
                : "";
        if (tienThucTeRaw.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng kiểm đếm và nhập tổng tiền mặt thực tế trong két trước khi chốt ca!",
                    "Chưa nhập tiền thực tế", JOptionPane.WARNING_MESSAGE);
            txtTienThucTe.requestFocus();
            return;
        }

        long thucTe;
        try {
            thucTe = Long.parseLong(tienThucTeRaw);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Số tiền thực tế không hợp lệ. Vui lòng kiểm tra lại!",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtTienThucTe.requestFocus();
            return;
        }
        long chenhLech = thucTe - lyThuyet;

        String lyDoNhap = txtLyDoChenhLech.getText().trim();
        if (chenhLech != 0 && lyDoNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Phát hiện chênh lệch tiền mặt trong két (" + (chenhLech > 0 ? "+" : "") + df.format(chenhLech) + ")!\n" +
                    "Theo quy định kiểm soát nội bộ, Thu ngân BẮT BUỘC nhập lý do giải trình trước khi bàn giao ca.",
                    "Bắt buộc giải trình chênh lệch", JOptionPane.WARNING_MESSAGE);
            txtLyDoChenhLech.requestFocus();
            return;
        }

        String caSelected = cboCaLamViec != null && cboCaLamViec.getSelectedItem() != null
                ? cboCaLamViec.getSelectedItem().toString() : "CA01 - Ca Sáng";
        String maCa = caSelected.split("-")[0].trim();
        String maNV = UserSession.getUserId() != null ? UserSession.getUserId() : "NV002";
        String tenNV = UserSession.getFullName() != null ? UserSession.getFullName() : "Lê Anh Minh";
        String maGC = luuBienBanGiaoCaXuongCSDL(maNV, tenNV, maCa, tienDauCa, lyThuyet, thucTe, chenhLech, lyDoNhap);
        if (maGC == null) {
            return;
        }

        btnChotCa.setEnabled(false);
        txtTienThucTe.setEditable(false);
        txtLyDoChenhLech.setEditable(false);

        int askLogout = JOptionPane.showConfirmDialog(this,
                "ĐÃ GỬI BIÊN BẢN KẾT CA [" + maGC + "] LÊN HỆ THỐNG!\n" +
                "• Trạng thái hiện tại: ⏳ Chờ Quản lý duyệt\n" +
                "• Thu ngân bàn giao: " + tenNV + "\n" +
                "• Tiền mặt thực tế: " + df.format(thucTe) + " (Chênh lệch: " + df.format(chenhLech) + ")\n\n" +
                "Bạn có muốn ĐĂNG XUẤT ngay bây giờ để Quản lý (vai trò NVQL) đăng nhập vào duyệt két không?",
                "Đã gửi chờ Quản lý duyệt",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (askLogout == JOptionPane.YES_OPTION) {
            thucHienDangXuatNgay();
        }
    }

    private String luuBienBanGiaoCaXuongCSDL(String maNV, String tenNV, String maCa,
                                              long dauCa, long lyThuyet, long thucTe, long chenhLech, String ghiChu) {
        LocalDateTime nowDt = LocalDateTime.now();
        String nowStr = nowDt.format(dtf);
        
        BangGiaoCaDAO dao = new BangGiaoCaDAO();
        String maGC = dao.phatSinhMaGiaoCa();

        int maxNum = 0;
        try {
            maxNum = Integer.parseInt(maGC.substring(2));
        } catch (Exception ignored) {}

        for (int i = 0; i < modelLichSu.getRowCount(); i++) {
            String m = String.valueOf(modelLichSu.getValueAt(i, 0));
            if (m.startsWith("GC") && !m.startsWith("GC_TEST_")) {
                try {
                    int n = Integer.parseInt(m.substring(2).replaceAll("[^0-9]", ""));
                    if (n >= maxNum) maxNum = n + 1;
                } catch (Exception ignored) {}
            }
        }
        maGC = String.format("GC%03d", maxNum > 0 ? maxNum : 1);

        try {
            NhanVien nv = new NhanVien(maNV, tenNV, "", "", null);
            CaLamViec ca = new CaLamViec(maCa, maCa, null, null);
            BangGiaoCa bgc = new BangGiaoCa(
                    maGC, nv, ca,
                    BigDecimal.valueOf(dauCa),
                    BigDecimal.valueOf(lyThuyet),
                    BigDecimal.valueOf(thucTe),
                    BigDecimal.valueOf(chenhLech),
                    ghiChu, nowDt
            );
            bgc.setTienThuTienMat(BigDecimal.valueOf(tienHeThongGhiNhan));
            bgc.setTienChuyenKhoan(BigDecimal.valueOf(tienChuyenKhoanGhiNhan));
            bgc.setTienHoanTra(BigDecimal.valueOf(tienHoanTraKhach));
            bgc.setTrangThai("CHO_DUYET");
            if (!dao.insertBangGiaoCa(bgc)) {
                throw new SQLException("Không có bản ghi bàn giao ca nào được lưu.");
            }
        } catch (Exception e) {
            System.err.println("[BanGiaoCaPanel] Lỗi lưu giao ca CSDL: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Không thể lưu biên bản giao ca xuống CSDL. Dữ liệu chưa được chốt!\n" + e.getMessage(),
                    "Lỗi lưu bàn giao ca", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        modelLichSu.insertRow(0, new Object[]{
                maGC, maCa, tenNV,
                df.format(dauCa), df.format(lyThuyet),
                df.format(thucTe), df.format(chenhLech), nowStr,
                "⏳ Chờ Quản lý duyệt | " + (ghiChu.isEmpty() ? "Khớp két 100%" : ghiChu)
        });
        if (tblLichSu != null && modelLichSu.getRowCount() > 0) {
            tblLichSu.setRowSelectionInterval(0, 0);
        }
        txtLyDoChenhLech.setText("");
        return maGC;
    }

    /**
     * Nút dành cho Quản lý (NVQL) duyệt đối soát ca:
     * Hiển thị danh sách/hộp thoại các ca đang chờ duyệt, đối soát chênh lệch,
     * duyệt trực tiếp không cần nhập lại mật khẩu, cập nhật CSDL và in phiếu K80.
     */
    private void duyetBienBanGiaoCaDangChon() {
        if (!UserSession.isQuanLy()) {
            JOptionPane.showMessageDialog(this,
                    "Chức năng này chỉ dành cho vai trò Quản lý (NVQL)!",
                    "Từ chối quyền hạn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BangGiaoCaDAO dao = new BangGiaoCaDAO();
        List<BangGiaoCa> dsCho = dao.getDanhSachCaChoDuyet();

        int selectedMainRow = tblLichSu != null ? tblLichSu.getSelectedRow() : -1;
        if (selectedMainRow >= 0 && (dsCho == null || dsCho.isEmpty())) {
            String ghiChu = String.valueOf(modelLichSu.getValueAt(selectedMainRow, 8));
            if (ghiChu.contains("Đã duyệt") || ghiChu.startsWith("✓")) {
                int xemLai = JOptionPane.showConfirmDialog(this,
                        "Biên bản [" + modelLichSu.getValueAt(selectedMainRow, 0) + "] đã được duyệt đối soát trước đó.\n" +
                        "Bạn có muốn xem và in lại Phiếu Bàn Giao Ca K80 của biên bản này không?",
                        "Biên bản đã được duyệt", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (xemLai == JOptionPane.YES_OPTION) {
                    xemVaInBienBanK80DangChon();
                }
                return;
            }
        }

        if (dsCho == null || dsCho.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Hiện không có ca làm việc nào đang chờ Quản lý duyệt đối soát két!",
                    "Không có ca chờ duyệt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        hienThiDialogDuyetCacCaCho(dsCho);
    }

    /**
     * Hộp thoại đối soát và duyệt trực tiếp danh sách các ca chờ Quản lý duyệt
     */
    private void hienThiDialogDuyetCacCaCho(List<BangGiaoCa> dsCho) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "DANH SÁCH CA LÀM VIỆC CHỜ QUẢN LÝ DUYỆT ĐỐI SOÁT", true);
        dlg.setSize(760, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        ((JPanel) dlg.getContentPane()).setBorder(new EmptyBorder(14, 16, 14, 16));
        dlg.getContentPane().setBackground(ThemeColors.BACKGROUND);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("DANH SÁCH CA BÀN GIAO CHỜ XÉT DUYỆT (" + dsCho.size() + " ca)",
                IconHelper.getIcon(IconHelper.CLIPBOARD_CHECK, 20, ThemeColors.PRIMARY), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Quản lý kiểm tra số liệu két thực tế và xác nhận duyệt trực tiếp", SwingConstants.LEFT);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.SOUTH);
        dlg.add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"Mã GC", "Ca", "Thu ngân", "Đầu ca", "Lý thuyết", "Thực tế", "Chênh lệch", "Thời điểm chốt", "Lý do / Giải trình"};
        DefaultTableModel modelCho = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (BangGiaoCa bgc : dsCho) {
            String maCa = bgc.getCaLamViec() != null ? bgc.getCaLamViec().getMaCa() : "CA01";
            String tenNV = bgc.getNhanVien() != null ? bgc.getNhanVien().getHoTen() : "Thu ngân";
            long dauCa = bgc.getTienDauCa() != null ? bgc.getTienDauCa().longValue() : 0L;
            long ht = bgc.getTienCuoiCaHeThong() != null ? bgc.getTienCuoiCaHeThong().longValue() : 0L;
            long tt = bgc.getTienCuoiCaThucTe() != null ? bgc.getTienCuoiCaThucTe().longValue() : 0L;
            long cl = bgc.getTienChenhLech() != null ? bgc.getTienChenhLech().longValue() : 0L;
            String ktStr = bgc.getGioKetThucThucTe() != null ? bgc.getGioKetThucThucTe().format(dtf) : "";
            String note = bgc.getLyDoChenhLech() != null ? bgc.getLyDoChenhLech() : "";

            modelCho.addRow(new Object[]{
                    bgc.getMaGiaoCa(), maCa, tenNV,
                    df.format(dauCa), df.format(ht), df.format(tt), df.format(cl),
                    ktStr, note
            });
        }

        JTable tblCho = new JTable(modelCho);
        tblCho.setRowHeight(32);
        tblCho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCho.setSelectionBackground(ThemeColors.PRIMARY_SOFT);
        tblCho.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        // Pre-select first row or matching selection from main table
        int initIdx = 0;
        int mainSel = tblLichSu != null ? tblLichSu.getSelectedRow() : -1;
        if (mainSel >= 0 && mainSel < modelLichSu.getRowCount()) {
            String mainMaGC = String.valueOf(modelLichSu.getValueAt(mainSel, 0));
            for (int i = 0; i < dsCho.size(); i++) {
                if (mainMaGC.equalsIgnoreCase(dsCho.get(i).getMaGiaoCa())) {
                    initIdx = i;
                    break;
                }
            }
        }
        if (modelCho.getRowCount() > 0) {
            tblCho.setRowSelectionInterval(initIdx, initIdx);
        }

        // Details Panel
        JPanel pnlDetails = new JPanel(new GridLayout(3, 2, 8, 4));
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblDetailNV = new JLabel("Thu ngân: ");
        JLabel lblDetailCa = new JLabel("Ca trực: ");
        JLabel lblDetailLT = new JLabel("Lý thuyết két: ");
        JLabel lblDetailTT = new JLabel("Thực tế kiểm đếm: ");
        JLabel lblDetailCL = new JLabel("Chênh lệch: ");
        JLabel lblDetailNote = new JLabel("Giải trình: ");

        lblDetailNV.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailCa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailLT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailTT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetailCL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDetailNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlDetails.add(lblDetailNV);
        pnlDetails.add(lblDetailCa);
        pnlDetails.add(lblDetailLT);
        pnlDetails.add(lblDetailTT);
        pnlDetails.add(lblDetailCL);
        pnlDetails.add(lblDetailNote);

        Runnable capNhatChiTiet = () -> {
            int r = tblCho.getSelectedRow();
            if (r >= 0 && r < dsCho.size()) {
                BangGiaoCa b = dsCho.get(r);
                lblDetailNV.setText("<html>Thu ngân: <b>" + (b.getNhanVien() != null ? b.getNhanVien().getHoTen() : "") + "</b></html>");
                lblDetailCa.setText("<html>Ca trực: <b>" + (b.getCaLamViec() != null ? b.getCaLamViec().getMaCa() : "") + "</b></html>");
                lblDetailLT.setText("<html>Lý thuyết két: <b>" + df.format(b.getTienCuoiCaHeThong()) + "</b></html>");
                lblDetailTT.setText("<html>Thực tế kiểm đếm: <b>" + df.format(b.getTienCuoiCaThucTe()) + "</b></html>");
                long clVal = b.getTienChenhLech() != null ? b.getTienChenhLech().longValue() : 0L;
                lblDetailCL.setText("<html>Chênh lệch: <b>" + (clVal > 0 ? "+" : "") + df.format(clVal) + "</b></html>");
                lblDetailCL.setForeground(clVal == 0 ? ThemeColors.SUCCESS : (clVal < 0 ? ThemeColors.DANGER : ThemeColors.WARNING));
                lblDetailNote.setText("<html>Giải trình: <i>" + (b.getLyDoChenhLech() != null ? b.getLyDoChenhLech() : "") + "</i></html>");
            }
        };

        tblCho.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) capNhatChiTiet.run();
        });
        capNhatChiTiet.run();

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 8));
        pnlCenter.setOpaque(false);
        pnlCenter.add(new JScrollPane(tblCho), BorderLayout.CENTER);
        pnlCenter.add(pnlDetails, BorderLayout.SOUTH);
        dlg.add(pnlCenter, BorderLayout.CENTER);

        // Action Buttons
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        JButton btnDuyet = new JButton("PHÊ DUYỆT & KÝ NHẬN KÉT",
                IconHelper.getIcon(IconHelper.CHECK, 15, Color.WHITE));
        btnDuyet.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDuyet.setBackground(new Color(0x05, 0x96, 0x69));
        btnDuyet.setForeground(Color.WHITE);
        btnDuyet.setPreferredSize(new Dimension(250, 38));
        btnDuyet.setToolTipText("Phê duyệt đối soát, ký nhận két và in biên bản K80");
        btnDuyet.setFocusPainted(false);
        btnDuyet.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnDong = ThemeColors.createSecondaryButton("Đóng", null);
        btnDong.setPreferredSize(new Dimension(90, 38));
        btnDong.addActionListener(e -> dlg.dispose());

        btnDuyet.addActionListener(e -> {
            int selectedIdx = tblCho.getSelectedRow();
            if (selectedIdx < 0 || selectedIdx >= dsCho.size()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn ca cần duyệt trong danh sách!", "Chưa chọn ca", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BangGiaoCa caDangDuyet = dsCho.get(selectedIdx);
            String maGC = caDangDuyet.getMaGiaoCa();
            String maCa = caDangDuyet.getCaLamViec() != null ? caDangDuyet.getCaLamViec().getMaCa() : "CA01";
            String tenNV = caDangDuyet.getNhanVien() != null ? caDangDuyet.getNhanVien().getHoTen() : "Thu ngân";
            long dauCa = caDangDuyet.getTienDauCa() != null ? caDangDuyet.getTienDauCa().longValue() : 1000000L;
            long lyThuyet = caDangDuyet.getTienCuoiCaHeThong() != null ? caDangDuyet.getTienCuoiCaHeThong().longValue() : 0L;
            long thucTe = caDangDuyet.getTienCuoiCaThucTe() != null ? caDangDuyet.getTienCuoiCaThucTe().longValue() : 0L;
            long chenhLech = caDangDuyet.getTienChenhLech() != null ? caDangDuyet.getTienChenhLech().longValue() : 0L;
            String thoiDiem = caDangDuyet.getGioKetThucThucTe() != null ? caDangDuyet.getGioKetThucThucTe().format(dtf) : LocalDateTime.now().format(dtf);

            String lyDoGoc = caDangDuyet.getLyDoChenhLech() != null
                    ? caDangDuyet.getLyDoChenhLech().trim() : "";
            if (lyDoGoc.isEmpty()) lyDoGoc = "Khớp két 100%";

            String tenQuanLyDuyet = UserSession.getFullName();
            LocalDateTime thoiGianDuyet = LocalDateTime.now();
            String ghiChuMoi = "✓ Đã duyệt (QL: " + tenQuanLyDuyet + ") | " + lyDoGoc;

            BangGiaoCaDAO daoGiaoCa = new BangGiaoCaDAO();
            boolean ok = daoGiaoCa.duyetGiaoCa(maGC, UserSession.getUserId(), thoiGianDuyet);
            if (!ok) {
                JOptionPane.showMessageDialog(dlg, "Lỗi khi cập nhật cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dongBoLichSuGiaoCaTuDatabase();
            dlg.dispose();

            hienThiBienBanGiaoCaK80(maGC, maCa, tenNV, tenQuanLyDuyet, dauCa,
                    caDangDuyet.getTienThuTienMat().longValue(),
                    caDangDuyet.getTienChuyenKhoan().longValue(),
                    caDangDuyet.getTienHoanTra().longValue(),
                    lyThuyet, thucTe, chenhLech, ghiChuMoi, thoiDiem, thoiGianDuyet.format(dtf));
        });

        pnlActions.add(btnDong);
        pnlActions.add(btnDuyet);
        dlg.add(pnlActions, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void xemVaInBienBanK80DangChon() {
        if (tblLichSu == null || modelLichSu.getRowCount() == 0) return;
        int row = tblLichSu.getSelectedRow();
        if (row < 0) row = 0;

        String maGC = String.valueOf(modelLichSu.getValueAt(row, 0));
        try {
            BangGiaoCa bgc = new BangGiaoCaDAO().getBangGiaoCaTheoMa(maGC);
            if (bgc == null) throw new SQLException("Không tìm thấy biên bản " + maGC);
            String maCa = bgc.getCaLamViec() != null ? bgc.getCaLamViec().getMaCa() : "";
            String thuNganGiao = bgc.getNhanVien() != null ? bgc.getNhanVien().getHoTen() : "";
            String nguoiNhan = bgc.getQuanLyDuyet() != null && bgc.getQuanLyDuyet().getHoTen() != null
                    ? bgc.getQuanLyDuyet().getHoTen() : "Đang chờ Quản lý ký nhận";
            String thoiDiem = bgc.getGioKetThucThucTe() != null ? bgc.getGioKetThucThucTe().format(dtf) : "Chưa chốt";
            String thoiGianDuyet = bgc.getThoiGianDuyet() != null ? bgc.getThoiGianDuyet().format(dtf) : "Chưa duyệt";
            hienThiBienBanGiaoCaK80(maGC, maCa, thuNganGiao, nguoiNhan,
                    bgc.getTienDauCa().longValue(), bgc.getTienThuTienMat().longValue(),
                    bgc.getTienChuyenKhoan().longValue(), bgc.getTienHoanTra().longValue(),
                    bgc.getTienCuoiCaHeThong().longValue(), bgc.getTienCuoiCaThucTe().longValue(),
                    bgc.getTienChenhLech().longValue(), taoMoTaTrangThai(bgc), thoiDiem, thoiGianDuyet);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu lịch sử K80:\n" + ex.getMessage(),
                    "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hienThiBienBanGiaoCaK80(String maGC, String maCa, String nguoiGiao, String nguoiNhan,
                                         long dauCa, long thuTienMat, long thuChuyenKhoan, long chiHoanTra,
                                         long lyThuyet, long thucTe, long chenhLech, String ghiChu,
                                         String thoiDiem, String thoiGianDuyet) {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("        PET SHOP MANAGEMENT - HỆ THỐNG CỬA HÀNG      \n");
        sb.append("         BIÊN BẢN ĐỐI SOÁT & BÀN GIAO CA (K80)      \n");
        sb.append("====================================================\n");
        sb.append("Mã biên bản:        ").append(maGC).append("\n");
        sb.append("Ca làm việc:        ").append(maCa).append("\n");
        sb.append("Thời điểm chốt:     ").append(thoiDiem).append("\n");
        sb.append("Thời điểm duyệt:    ").append(thoiGianDuyet).append("\n");
        sb.append("Thu ngân giao ca:   ").append(nguoiGiao).append("\n");
        sb.append("Quản lý / Nhận ca:  ").append(nguoiNhan).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("1. ĐỐI SOÁT TIỀN MẶT TRONG KÉT:\n");
        sb.append(String.format("   (+) Tiền lẻ đầu ca:          %18s\n", df.format(dauCa)));
        sb.append(String.format("   (+) Thu tiền mặt bán hàng:   %18s\n", df.format(thuTienMat)));
        sb.append(String.format("   (-) Chi hoàn tiền đổi/trả:   %18s\n", df.format(chiHoanTra)));
        sb.append("   .................................................\n");
        sb.append(String.format("   (=) TIỀN MẶT LÝ THUYẾT KÉT:  %18s\n", df.format(lyThuyet)));
        sb.append(String.format("   (✓) TIỀN MẶT THỰC TẾ ĐẾM:    %18s\n", df.format(thucTe)));
        sb.append(String.format("   (Δ) CHÊNH LỆCH KÉT:          %18s\n", (chenhLech > 0 ? "+" : "") + df.format(chenhLech)));
        sb.append("----------------------------------------------------\n");
        sb.append("2. DOANH THU KHÔNG TIỀN MẶT (VIETQR / VNPAY / THẺ):\n");
        sb.append(String.format("   (i) Tổng chuyển khoản/Thẻ:   %18s\n", df.format(thuChuyenKhoan)));
        sb.append("----------------------------------------------------\n");
        sb.append("Trạng thái & Ghi chú:\n");
        sb.append(" -> ").append(ghiChu).append("\n");
        sb.append("----------------------------------------------------\n");
        sb.append("     CHỮ KÝ BÊN GIAO CA         CHỮ KÝ BÊN NHẬN CA  \n");
        sb.append("       (Đã ký xác nhận)           (Đã ký đối soát)  \n");
        sb.append("        ").append( String.format("%-24s", nguoiGiao) ).append(nguoiNhan).append("\n");
        sb.append("====================================================\n");

        JTextArea txtReceipt = new JTextArea(sb.toString());
        txtReceipt.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(254, 252, 248));
        txtReceipt.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setPreferredSize(new Dimension(500, 440));
        scroll.setBorder(new LineBorder(ThemeColors.BORDER_LIGHT, 1, true));

        JOptionPane.showMessageDialog(this, scroll, "BIÊN BẢN BÀN GIAO CA NHIỆT 80MM - " + maGC,
                JOptionPane.INFORMATION_MESSAGE, IconHelper.getIcon(IconHelper.RECEIPT, 24, ThemeColors.PRIMARY));
    }

    private void thucHienDangXuatChuyenCa() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn đăng xuất khỏi tài khoản [" + UserSession.getFullName() + "]\n" +
                "để Quản lý (admin) hoặc Thu ngân ca tiếp theo đăng nhập không?",
                "Đăng xuất chuyển ca",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            thucHienDangXuatNgay();
        }
    }

    private void thucHienDangXuatNgay() {
        Window win = SwingUtilities.getWindowAncestor(this);
        DangNhapController.dangXuat();
        if (win != null) {
            win.dispose();
        }
        new LoginForm().setVisible(true);
    }
}
