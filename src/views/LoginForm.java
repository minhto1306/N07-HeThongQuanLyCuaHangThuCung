package views;

import controllers.DangNhapController;
import models.entity.NhanVien;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Giao diện Đăng nhập hệ thống Quản lý Cửa hàng Thú cưng (LoginForm).
 * Thiết kế theo phong cách Card nổi hiện đại:
 * - Tông màu Warm Peach / Soft Apricot / Nude ấm áp và thân thiện.
 * - Tích hợp hệ thống Icon Lucide cho các trường nhập liệu và nút điều khiển.
 * - Bảo toàn 100% logic xác thực DangNhapController, phím Enter và điều hướng phân quyền.
 */
public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private final DangNhapController dangNhapController;

    public LoginForm() {
        this.dangNhapController = new DangNhapController();
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        setTitle("ĐĂNG NHẬP HỆ THỐNG - CỬA HÀNG THÚ CƯNG (GROUP 07)");
        setSize(860, 550);
        setMinimumSize(new Dimension(860, 550));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // 1. Banner thương hiệu bên trái (WEST) - Tông Peach / Apricot Gradient
        JPanel pnlBanner = taoBannerBenTrai();
        pnlBanner.setPreferredSize(new Dimension(340, 0));

        // 2. Khung nhập liệu đăng nhập bên phải (CENTER)
        JPanel pnlForm = taoKhungDangNhap();

        add(pnlBanner, BorderLayout.WEST);
        add(pnlForm, BorderLayout.CENTER);

        // Bắt sự kiện phím Enter cho cả hai trường nhập liệu
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    thucHienDangNhap();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private JPanel taoBannerBenTrai() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Dải màu gradient hồng phấn sang hồng san hô theo Happy Hues Palette 15
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0xFF, 0xC6, 0xC7),
                        0, getHeight(), new Color(0xFF, 0x8B, 0xA7)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Họa tiết vòng tròn trang trí mờ nghệ thuật
                g2d.setColor(new Color(255, 255, 255, 28));
                g2d.fillOval(-40, -40, 200, 200);
                g2d.fillOval(getWidth() - 100, getHeight() - 120, 180, 180);
            }
        };
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(50, 25, 35, 25));

        // Icon Lucide Paw Print kết xuất Vector Java2D nét căng 100% (không vỡ hạt)
        JLabel lblIcon = new JLabel(IconHelper.getVectorIcon(IconHelper.PAW, 68, Color.WHITE), SwingConstants.CENTER);
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBrand = new JLabel("PET SHOP SYSTEM", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Chăm sóc tận tâm - Phục vụ tiện lợi", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblSub.setForeground(new Color(255, 247, 237));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        banner.add(lblIcon);
        banner.add(Box.createVerticalStrut(16));
        banner.add(lblBrand);
        banner.add(Box.createVerticalStrut(8));
        banner.add(lblSub);
        banner.add(Box.createVerticalGlue());

        return banner;
    }

    private JPanel taoKhungDangNhap() {
        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(26, 36, 22, 36));

        // 1. Tiêu đề form
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Vui lòng điền thông tin xác thực để bắt đầu ca làm việc");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblDesc);

        // 2. Vùng nhập liệu (GridBagLayout)
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Tên đăng nhập (kèm icon Lucide User)
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("Tên đăng nhập:", IconHelper.getPrimaryIcon(IconHelper.USER, 16), SwingConstants.LEFT);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(ThemeColors.TEXT_PRIMARY);
        lblUser.setIconTextGap(8);
        pnlFields.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField("admin");
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setPreferredSize(new Dimension(340, 40));
        txtUsername.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập...");
        txtUsername.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.USER, 16, ThemeColors.TEXT_MUTED));
        txtUsername.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        pnlFields.add(txtUsername, gbc);

        // Mật khẩu (kèm icon Lucide Lock)
        gbc.gridy = 2;
        gbc.insets = new Insets(12, 0, 4, 0);
        JLabel lblPass = new JLabel("Mật khẩu:", IconHelper.getPrimaryIcon(IconHelper.LOCK, 16), SwingConstants.LEFT);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(ThemeColors.TEXT_PRIMARY);
        lblPass.setIconTextGap(8);
        pnlFields.add(lblPass, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 4, 0);
        txtPassword = new JPasswordField("123456");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(340, 40));
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        txtPassword.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.LOCK, 16, ThemeColors.TEXT_MUTED));
        txtPassword.putClientProperty("JPasswordField.showRevealButton", true);
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        pnlFields.add(txtPassword, gbc);

        // Khung gợi ý tài khoản demo (Chip Card tông Peach/Nude nhẹ nhàng, không bị tràn viền)
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 10, 0);
        JPanel pnlHint = new JPanel(new BorderLayout(10, 0));
        pnlHint.setBackground(ThemeColors.PRIMARY_TINT);
        pnlHint.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_ACCENT, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lblHintIcon = new JLabel(IconHelper.getIcon(IconHelper.ALERT_TRIANGLE, 16, ThemeColors.PRIMARY));
        JLabel lblHintText = new JLabel("<html><div style='color:#9A3412; font-size:11px; line-height:1.45;'>"
                + "<b>Tài khoản Demo (4 vai trò):</b><br/>"
                + "• <b>admin</b> (Quản lý) &nbsp;&nbsp;|&nbsp;&nbsp; <b>thungan</b> (Thu ngân)<br/>"
                + "• <b>banhang</b> (Bán hàng) &nbsp;&nbsp;|&nbsp;&nbsp; <b>chamsoc</b> (Chăm sóc)<br/>"
                + "Mật khẩu chung: <b style='color:#C2410C;'>123456</b>"
                + "</div></html>");
        pnlHint.add(lblHintIcon, BorderLayout.WEST);
        pnlHint.add(lblHintText, BorderLayout.CENTER);
        pnlFields.add(pnlHint, gbc);

        // Nút bấm Đăng nhập & Thoát
        gbc.gridy = 5;
        gbc.insets = new Insets(14, 0, 0, 0);
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlButtons.setOpaque(false);

        btnLogin = new JButton("Đăng nhập", IconHelper.getIcon(IconHelper.LOG_IN, 18, ThemeColors.TEXT_PRIMARY));
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(ThemeColors.PRIMARY);
        btnLogin.setForeground(ThemeColors.TEXT_PRIMARY);
        btnLogin.setPreferredSize(new Dimension(0, 42));
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setIconTextGap(8);
        btnLogin.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.PRIMARY_HOVER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btnLogin.addActionListener(e -> thucHienDangNhap());

        btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExit.setBackground(ThemeColors.BACKGROUND);
        btnExit.setForeground(ThemeColors.TEXT_SECONDARY);
        btnExit.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> System.exit(0));

        pnlButtons.add(btnLogin);
        pnlButtons.add(btnExit);
        pnlFields.add(pnlButtons, gbc);

        form.add(pnlHeader, BorderLayout.NORTH);
        form.add(pnlFields, BorderLayout.CENTER);
        return form;
    }

    private void thucHienDangNhap() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        DangNhapController.KetQuaDangNhap ketQua = dangNhapController.xuLyDangNhap(username, password);

        if (ketQua.isThanhCong()) {
            NhanVien nv = ketQua.getNhanVien();
            JOptionPane.showMessageDialog(this,
                    "Xin chào: " + nv.getHoTen() + "\nChức vụ: " + nv.getChucVu() + "\n" + ketQua.getThongDiep(),
                    "Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);

            // Mở MainDashboard với thông tin nhân viên
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                MainDashboard dashboard = new MainDashboard(nv);
                dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
                dashboard.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    ketQua.getThongDiep(),
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    public static void main(String[] args) {
        // Áp dụng Look and Feel qua Main
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
