package gui;

import bus.NhanVienBus;
import entity.NhanVien;
import entity.TaiKhoan;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Quản lý Nhân viên & Phân quyền Hệ thống (Dành riêng cho Quản lý NVQL).
 * Giao diện hiện đại dạng Lưới thẻ (Grid Cards):
 * - Thẻ nhân viên hiển thị Avatar tròn, Họ tên, Mã NV tự sinh, Email công việc, SĐT đăng nhập.
 * - Mã NV tự động tăng từ T-SQL fn_SinhMaNhanVien, không cho phép chỉnh sửa bằng tay.
 * - Thao tác nhanh: Đổi vai trò, Cấp lại mật khẩu (tự động băm SHA-256), Khóa / Mở khóa tài khoản.
 */
public class QuanLyNhanVienPanel extends JPanel {

    private final NhanVienBus controller = new NhanVienBus();
    private final List<NhanVien> danhSachNhanVien = new ArrayList<>();
    private final List<NhanVien> danhSachLoc = new ArrayList<>();

    private String currentRoleFilter = "TẤT CẢ";
    private String currentSearchKeyword = "";

    private JPanel pnlGridCards;
    private JTextField txtTimKiem;
    private final List<JButton> filterButtons = new ArrayList<>();

    public QuanLyNhanVienPanel() {
        setLayout(new BorderLayout(0, 14));
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setBackground(ThemeColors.BACKGROUND);

        initUI();
        taiDuLieuNhanVien();
    }

    private void initUI() {
        // 1. Header Toolbar: Bộ lọc vai trò + Tìm kiếm + Nút Thêm mới
        JPanel pnlToolbar = taoPanelToolbar();
        add(pnlToolbar, BorderLayout.NORTH);

        // 2. Vùng Lưới thẻ (Grid Cards) đặt trong JScrollPane
        pnlGridCards = new JPanel();
        pnlGridCards.setOpaque(false);
        pnlGridCards.setLayout(new GridLayout(0, 3, 14, 14));

        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.add(pnlGridCards, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(pnlWrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel taoPanelToolbar() {
        JPanel pnl = new JPanel(new BorderLayout(12, 10));
        pnl.setBackground(ThemeColors.SURFACE);
        pnl.setBorder(ThemeColors.createCardBorder(12, 16, 12, 16));

        // Hàng trên: Tiêu đề phân hệ & Nút Thêm nhân viên mới
        JPanel pnlTopRow = new JPanel(new BorderLayout());
        pnlTopRow.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN & PHÂN QUYỀN HỆ THỐNG",
                IconHelper.getPrimaryIcon(IconHelper.USERS, 20), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JButton btnThemNV = ThemeColors.createPrimaryButton("Thêm nhân viên mới",
                IconHelper.getIcon(IconHelper.PLUS, 14, Color.WHITE));
        btnThemNV.addActionListener(e -> moDialogThemNhanVien());

        pnlTopRow.add(lblTitle, BorderLayout.WEST);
        pnlTopRow.add(btnThemNV, BorderLayout.EAST);

        // Hàng dưới: Thanh tìm kiếm & Bộ lọc vai trò
        JPanel pnlBottomRow = new JPanel(new BorderLayout(10, 0));
        pnlBottomRow.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setPreferredSize(new Dimension(260, 36));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Tìm theo tên, mã NV, SĐT, email...");
        txtTimKiem.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.SEARCH, 15, ThemeColors.TEXT_MUTED));
        txtTimKiem.putClientProperty("JTextField.showClearButton", true);
        txtTimKiem.setBorder(ThemeColors.createInputBorder());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                currentSearchKeyword = txtTimKiem.getText().trim().toLowerCase();
                apDungBoLocVaHienThi();
            }
        });

        // Filter chips vai trò
        JPanel pnlChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlChips.setOpaque(false);

        String[] roles = {"TẤT CẢ", "Quản lý (NVQL)", "Thu ngân (NVTN)", "Bán hàng (NVBH)", "Chăm sóc (NVCS)"};
        for (String r : roles) {
            JButton btnChip = taoFilterChip(r);
            filterButtons.add(btnChip);
            pnlChips.add(btnChip);
        }
        capNhatFilterChipStyle("TẤT CẢ");

        pnlBottomRow.add(txtTimKiem, BorderLayout.WEST);
        pnlBottomRow.add(pnlChips, BorderLayout.CENTER);

        pnl.add(pnlTopRow, BorderLayout.NORTH);
        pnl.add(pnlBottomRow, BorderLayout.CENTER);

        return pnl;
    }

    private JButton taoFilterChip(String label) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + 18, 34));
        btn.addActionListener(e -> {
            currentRoleFilter = label;
            capNhatFilterChipStyle(label);
            apDungBoLocVaHienThi();
        });
        return btn;
    }

    private void capNhatFilterChipStyle(String activeLabel) {
        for (JButton btn : filterButtons) {
            if (btn.getText().equalsIgnoreCase(activeLabel)) {
                btn.setBackground(ThemeColors.PRIMARY);
                btn.setForeground(Color.WHITE);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                        new EmptyBorder(5, 12, 5, 12)
                ));
            } else {
                btn.setBackground(ThemeColors.SURFACE);
                btn.setForeground(ThemeColors.TEXT_SECONDARY);
                btn.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.BORDER, 1, true),
                        new EmptyBorder(5, 12, 5, 12)
                ));
            }
        }
    }

    public void taiDuLieuNhanVien() {
        danhSachNhanVien.clear();
        danhSachNhanVien.addAll(controller.layDanhSachNhanVien());
        apDungBoLocVaHienThi();
    }

    private void apDungBoLocVaHienThi() {
        danhSachLoc.clear();
        for (NhanVien nv : danhSachNhanVien) {
            TaiKhoan tk = nv.getTaiKhoan();
            String vaiTro = tk != null ? tk.getVaiTro() : "";

            boolean matchRole = true;
            if ("Quản lý (NVQL)".equalsIgnoreCase(currentRoleFilter)) matchRole = "NVQL".equalsIgnoreCase(vaiTro);
            else if ("Thu ngân (NVTN)".equalsIgnoreCase(currentRoleFilter)) matchRole = "NVTN".equalsIgnoreCase(vaiTro);
            else if ("Bán hàng (NVBH)".equalsIgnoreCase(currentRoleFilter)) matchRole = "NVBH".equalsIgnoreCase(vaiTro);
            else if ("Chăm sóc (NVCS)".equalsIgnoreCase(currentRoleFilter)) matchRole = "NVCS".equalsIgnoreCase(vaiTro);

            boolean matchKw = currentSearchKeyword.isEmpty() ||
                    nv.getHoTen().toLowerCase().contains(currentSearchKeyword) ||
                    nv.getMaNhanVien().toLowerCase().contains(currentSearchKeyword) ||
                    nv.getSoDienThoai().contains(currentSearchKeyword) ||
                    nv.getEmail().toLowerCase().contains(currentSearchKeyword);

            if (matchRole && matchKw) {
                danhSachLoc.add(nv);
            }
        }

        pnlGridCards.removeAll();
        for (NhanVien nv : danhSachLoc) {
            pnlGridCards.add(taoTheNhanVienCard(nv));
        }
        pnlGridCards.revalidate();
        pnlGridCards.repaint();
    }

    private JPanel taoTheNhanVienCard(NhanVien nv) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(ThemeColors.SURFACE);
        card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));

        TaiKhoan tk = nv.getTaiKhoan();
        String vaiTro = tk != null ? tk.getVaiTro() : "NV";
        boolean hoatDong = tk != null && tk.isTrangThaiHoatDong();

        // 1. Phía trên thẻ: Avatar tròn + Thông tin tên + Badge vai trò
        JPanel pnlHeader = new JPanel(new BorderLayout(12, 0));
        pnlHeader.setOpaque(false);

        // Avatar tròn với 2 chữ cái đầu
        String initials = layChuCaiDau(nv.getHoTen());
        JPanel pnlAvatar = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(layMauAvatarTheoVaiTro(vaiTro));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        pnlAvatar.setPreferredSize(new Dimension(46, 46));
        pnlAvatar.setOpaque(false);
        JLabel lblInitials = new JLabel(initials);
        lblInitials.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblInitials.setForeground(Color.WHITE);
        pnlAvatar.add(lblInitials);

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlInfo.setOpaque(false);

        JLabel lblName = new JLabel(nv.getHoTen());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblMa = new JLabel(nv.getMaNhanVien() + " · " + nv.getChucVu());
        lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMa.setForeground(ThemeColors.TEXT_MUTED);

        pnlInfo.add(lblName);
        pnlInfo.add(lblMa);

        pnlHeader.add(pnlAvatar, BorderLayout.WEST);
        pnlHeader.add(pnlInfo, BorderLayout.CENTER);

        // Badge vai trò ở góc phải
        JLabel lblRoleBadge = taoBadgeVaiTro(vaiTro);
        pnlHeader.add(lblRoleBadge, BorderLayout.EAST);

        // 2. Ở giữa thẻ: Số điện thoại đăng nhập, Email & Trạng thái hoạt động
        JPanel pnlBody = new JPanel(new GridLayout(3, 1, 0, 4));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(6, 0, 6, 0));

        JLabel lblPhone = new JLabel("Đăng nhập (SĐT): " + nv.getSoDienThoai(),
                IconHelper.getIcon(IconHelper.PHONE, 13, ThemeColors.TEXT_MUTED), SwingConstants.LEFT);
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhone.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel lblEmail = new JLabel("Email: " + (nv.getEmail().isEmpty() ? "Chưa thiết lập" : nv.getEmail()),
                IconHelper.getIcon(IconHelper.MAIL, 13, ThemeColors.TEXT_MUTED), SwingConstants.LEFT);
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEmail.setForeground(ThemeColors.TEXT_SECONDARY);

        JLabel lblStatus;
        if (hoatDong) {
            lblStatus = ThemeColors.createBadge("Đang làm việc", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS);
        } else {
            lblStatus = ThemeColors.createBadge("Tạm khóa / Nghỉ ca", new Color(0xF1, 0xF5, 0xF9), ThemeColors.TEXT_MUTED);
        }

        JPanel pnlStatusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlStatusRow.setOpaque(false);
        pnlStatusRow.add(lblStatus);

        pnlBody.add(lblPhone);
        pnlBody.add(lblEmail);
        pnlBody.add(pnlStatusRow);

        // 3. Phía dưới thẻ: Bộ nút thao tác nhanh
        JPanel pnlActions = new JPanel(new GridLayout(1, 3, 6, 0));
        pnlActions.setOpaque(false);

        JButton btnDoiVaiTro = ThemeColors.createSecondaryButton("Đổi vai trò", null);
        JButton btnResetPass = ThemeColors.createSecondaryButton("Cấp lại MK", null);
        JButton btnKhoa = ThemeColors.createSecondaryButton(hoatDong ? "Khóa TK" : "Mở khóa", null);

        btnDoiVaiTro.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnResetPass.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnKhoa.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        if (!hoatDong) {
            btnKhoa.setForeground(ThemeColors.SUCCESS);
        } else {
            btnKhoa.setForeground(ThemeColors.DANGER);
        }

        btnDoiVaiTro.addActionListener(e -> moDialogDoiVaiTro(nv));
        btnResetPass.addActionListener(e -> moDialogResetMatKhau(nv));
        btnKhoa.addActionListener(e -> xuLyKhoaTaiKhoan(nv));

        pnlActions.add(btnDoiVaiTro);
        pnlActions.add(btnResetPass);
        pnlActions.add(btnKhoa);

        card.add(pnlHeader, BorderLayout.NORTH);
        card.add(pnlBody, BorderLayout.CENTER);
        card.add(pnlActions, BorderLayout.SOUTH);

        // Hiệu ứng hover nổi bật
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(0xFF, 0xFA, 0xF5));
                card.setBorder(new CompoundBorder(
                        new LineBorder(ThemeColors.PRIMARY, 1, true),
                        new EmptyBorder(14, 16, 14, 16)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(ThemeColors.SURFACE);
                card.setBorder(ThemeColors.createCardBorder(14, 16, 14, 16));
            }
        });

        return card;
    }

    private JLabel taoBadgeVaiTro(String vaiTro) {
        switch (vaiTro.toUpperCase()) {
            case "NVQL":
                return ThemeColors.createBadge("Quản lý", ThemeColors.PRIMARY_SOFT, ThemeColors.PRIMARY_ACTIVE);
            case "NVKHO":
                return ThemeColors.createBadge("Thủ kho", new Color(0xFE, 0xF3, 0xC7), new Color(0xB4, 0x53, 0x09));
            case "NVTN":
                return ThemeColors.createBadge("Thu ngân", new Color(0xDB, 0xEA, 0xFE), new Color(0x1E, 0x40, 0xAF));
            case "NVBH":
                return ThemeColors.createBadge("Bán hàng", ThemeColors.SUCCESS_LIGHT, ThemeColors.SUCCESS);
            case "NVCS":
                return ThemeColors.createBadge("Chăm sóc", new Color(0xF3, 0xE8, 0xFF), new Color(0x7E, 0x22, 0xCE));
            default:
                return ThemeColors.createBadge(vaiTro, ThemeColors.PRIMARY_TINT, ThemeColors.TEXT_SECONDARY);
        }
    }

    private Color layMauAvatarTheoVaiTro(String vaiTro) {
        switch (vaiTro.toUpperCase()) {
            case "NVQL": return ThemeColors.PRIMARY;
            case "NVKHO": return new Color(0xD9, 0x77, 0x06);
            case "NVTN": return new Color(0x25, 0x63, 0xEB);
            case "NVBH": return new Color(0x10, 0xB9, 0x81);
            case "NVCS": return new Color(0x8B, 0x5C, 0xF6);
            default: return ThemeColors.TEXT_MUTED;
        }
    }

    private String layChuCaiDau(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) return "NV";
        String[] parts = hoTen.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[parts.length - 2].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    public void capNhatNhanSu() {
        moDialogThemNhanVien();
    }

    public void traCuuNhanVien(String kw) {
        if (txtTimKiem != null) {
            txtTimKiem.setText(kw != null ? kw : "");
        }
    }

    public void moDialogThemNhanVien() {
        String maTuDong = controller.sinhMaNhanVienMoi();
        JTextField txtMaNV = new JTextField(maTuDong);
        txtMaNV.setEditable(false);
        txtMaNV.setBackground(new Color(0xF1, 0xF5, 0xF9));

        JTextField txtHoTen = new JTextField();
        JTextField txtChucVu = new JTextField("Kỹ thuật viên Grooming");
        JTextField txtPhone = new JTextField();
        txtPhone.putClientProperty("JTextField.placeholderText", "Số điện thoại dùng để đăng nhập...");

        JTextField txtEmail = new JTextField(maTuDong.toLowerCase() + "@miopet.vn");
        txtEmail.putClientProperty("JTextField.placeholderText", "Hòm thư nhận OTP khôi phục mật khẩu...");

        JPasswordField txtPass = new JPasswordField("123456");
        txtPass.putClientProperty("FlatLaf.style", "showRevealButton: true");
        JComboBox<String> cboRole = new JComboBox<>(new String[]{"NVCS", "NVBH", "NVTN", "NVQL"});

        Object[] fields = {
                "Mã nhân viên (Hệ thống tự sinh):", txtMaNV,
                "Họ và tên nhân viên:", txtHoTen,
                "Chức vụ công việc:", txtChucVu,
                "Số điện thoại (dùng để đăng nhập):", txtPhone,
                "Email công việc (để nhận OTP):", txtEmail,
                "Mật khẩu khởi tạo (mặc định 123456):", txtPass,
                "Phân quyền vai trò:", cboRole
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "THÊM MỚI NHÂN VIÊN & TÀI KHOẢN HỆ THỐNG", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String ten = txtHoTen.getText().trim();
            String cv = txtChucVu.getText().trim();
            String sdt = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String r = (String) cboRole.getSelectedItem();

            if (ten.isEmpty() || sdt.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên, Số điện thoại và Mật khẩu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = controller.themNhanVien(ten, cv, sdt, email, p, r);
            if (ok) {
                taiDuLieuNhanVien();
                JOptionPane.showMessageDialog(this,
                        "Đã thêm mới nhân viên [" + ten + "] thành công!\nMã nhân viên: " + maTuDong + "\nĐăng nhập bằng SĐT: " + sdt,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void moDialogDoiVaiTro(NhanVien nv) {
        TaiKhoan tk = nv.getTaiKhoan();
        if (tk == null) return;

        String[] roles = {"NVQL", "NVTN", "NVBH", "NVCS"};
        JComboBox<String> cbo = new JComboBox<>(roles);
        cbo.setSelectedItem(tk.getVaiTro());

        int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"Chọn vai trò mới cho nhân viên " + nv.getHoTen() + " (" + nv.getMaNhanVien() + "):", cbo},
                "ĐỔI VAI TRÒ TÀI KHOẢN", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            String roleMoi = (String) cbo.getSelectedItem();
            boolean ok = controller.doiVaiTro(nv.getMaNhanVien(), roleMoi);
            if (ok) {
                taiDuLieuNhanVien();
                JOptionPane.showMessageDialog(this, "Đã cập nhật vai trò mới [" + roleMoi + "] thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void moDialogResetMatKhau(NhanVien nv) {
        JPasswordField txtNewPass = new JPasswordField("123456");
        txtNewPass.putClientProperty("FlatLaf.style", "showRevealButton: true");
        int opt = JOptionPane.showConfirmDialog(this,
                new Object[]{"Cấp lại mật khẩu cho nhân viên [" + nv.getHoTen() + " - " + nv.getSoDienThoai() + "]:", txtNewPass},
                "CẤP LẠI MẬT KHẨU (BĂM SHA-256)", JOptionPane.OK_CANCEL_OPTION);

        if (opt == JOptionPane.OK_OPTION) {
            String passMoi = new String(txtNewPass.getPassword()).trim();
            if (passMoi.isEmpty()) passMoi = "123456";

            boolean ok = controller.resetMatKhau(nv.getMaNhanVien(), passMoi);
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Đã cấp lại mật khẩu thành công cho nhân viên [" + nv.getHoTen() + "]!\nMật khẩu mới đã được băm SHA-256 bảo mật.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void xuLyKhoaTaiKhoan(NhanVien nv) {
        TaiKhoan tk = nv.getTaiKhoan();
        if (tk == null) return;

        String hanhDong = tk.isTrangThaiHoatDong() ? "Khóa" : "Mở khóa";
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn " + hanhDong + " tài khoản của nhân viên " + nv.getHoTen() + " (" + nv.getMaNhanVien() + ")?",
                "Xác nhận " + hanhDong, JOptionPane.YES_NO_OPTION);

        if (opt == JOptionPane.YES_OPTION) {
            boolean ok = controller.toggleTrangThai(nv.getMaNhanVien());
            if (ok) {
                taiDuLieuNhanVien();
                JOptionPane.showMessageDialog(this, "Đã " + hanhDong + " tài khoản thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
