package gui;

import bus.DangNhapBus;
import entity.NhanVien;
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
 * - Đăng nhập bằng Số điện thoại nhân viên (soDienThoai) và Mật khẩu (bảo mật SHA-256).
 * - Tích hợp tính năng "Quên mật khẩu?" xác thực qua Số điện thoại + Email và mã OTP.
 * - Thiết kế Card nổi hiện đại theo tông màu chuẩn FlatLaf sang trọng.
 */
public class LoginForm extends JFrame {

    private JTextField txtPhone;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnExit;
    private JButton btnForgotPassword;
    private final DangNhapBus dangNhapBus;

    public LoginForm() {
        this.dangNhapBus = new DangNhapBus();
        khoiTaoGiaoDien();
    }

    private void khoiTaoGiaoDien() {
        // Đảm bảo nút con mắt xem mật khẩu luôn được kích hoạt
        UIManager.put("PasswordField.showRevealButton", true);

        setTitle("ĐĂNG NHẬP HỆ THỐNG - CỬA HÀNG THÚ CƯNG MIO");
        setSize(880, 560);
        setMinimumSize(new Dimension(880, 560));
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.BACKGROUND);

        // 1. Banner thương hiệu bên trái (WEST) - Tông Emerald & Forest Gradient
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
        txtPhone.addKeyListener(enterListener);
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
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x10, 0xB9, 0x81),
                        0, getHeight(), new Color(0x06, 0x4E, 0x3B)
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

        JLabel lblDesc = new JLabel("Đăng nhập bằng Số điện thoại nhân viên đã được cấp");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(ThemeColors.TEXT_SECONDARY);

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblDesc);

        // 2. Vùng nhập liệu (GridBagLayout)
        JPanel pnlFields = new JPanel(new GridBagLayout());
        pnlFields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Số điện thoại nhân viên
        gbc.gridy = 0;
        JLabel lblPhone = new JLabel("Số điện thoại nhân viên:", IconHelper.getPrimaryIcon(IconHelper.PHONE, 16), SwingConstants.LEFT);
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPhone.setForeground(ThemeColors.TEXT_PRIMARY);
        lblPhone.setIconTextGap(8);
        pnlFields.add(lblPhone, gbc);

        gbc.gridy = 1;
        txtPhone = new JTextField("0901234567");
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setPreferredSize(new Dimension(360, 40));
        txtPhone.putClientProperty("JTextField.placeholderText", "Nhập SĐT nhân viên (VD: 0901234567)...");
        txtPhone.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.PHONE, 16, ThemeColors.TEXT_MUTED));
        txtPhone.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        pnlFields.add(txtPhone, gbc);

        // Mật khẩu
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 4, 0);
        JLabel lblPass = new JLabel("Mật khẩu bảo mật:", IconHelper.getPrimaryIcon(IconHelper.LOCK, 16), SwingConstants.LEFT);
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(ThemeColors.TEXT_PRIMARY);
        lblPass.setIconTextGap(8);
        pnlFields.add(lblPass, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 4, 0);
        txtPassword = new JPasswordField("123456");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(360, 40));
        txtPassword.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        txtPassword.putClientProperty("JTextField.leadingIcon", IconHelper.getIcon(IconHelper.LOCK, 16, ThemeColors.TEXT_MUTED));
        txtPassword.putClientProperty("FlatLaf.style", "showRevealButton: true");
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        pnlFields.add(txtPassword, gbc);

        // Dòng phụ trợ: Quên mật khẩu? (Được tùy biến màu hover/click không bị trùng nền trắng)
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 0, 10, 0);
        JPanel pnlForgot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlForgot.setOpaque(false);

        btnForgotPassword = new JButton("Quên mật khẩu?") {
            @Override
            public Color getForeground() {
                if (getModel() == null) return ThemeColors.PRIMARY_ACTIVE;
                if (getModel().isPressed()) return ThemeColors.PRIMARY_ACTIVE;
                if (getModel().isRollover()) return ThemeColors.PRIMARY_HOVER;
                return ThemeColors.PRIMARY_ACTIVE;
            }
        };
        btnForgotPassword.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        btnForgotPassword.setForeground(ThemeColors.PRIMARY_ACTIVE);
        btnForgotPassword.setBorder(null);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setFocusPainted(false);
        btnForgotPassword.setOpaque(false);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.putClientProperty("JButton.buttonType", "borderless");
        btnForgotPassword.putClientProperty("FlatLaf.style", "hoverForeground: #10B981; pressedForeground: #047857; focusedForeground: #047857; hoverBackground: #FFFFFF; pressedBackground: #FFFFFF;");
        btnForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnForgotPassword.setText("<html><u>Quên mật khẩu?</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnForgotPassword.setText("Quên mật khẩu?");
            }
        });
        btnForgotPassword.addActionListener(e -> moHopThoaiQuenMatKhau());
        pnlForgot.add(btnForgotPassword);
        pnlFields.add(pnlForgot, gbc);

        // Nút bấm Đăng nhập & Thoát
        gbc.gridy = 5;
        gbc.insets = new Insets(6, 0, 0, 0);
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
        String phone = txtPhone.getText();
        String password = new String(txtPassword.getPassword());

        DangNhapBus.KetQuaDangNhap ketQua = dangNhapBus.xuLyDangNhap(phone, password);

        if (ketQua.isThanhCong()) {
            NhanVien nv = ketQua.getNhanVien();
            JOptionPane.showMessageDialog(this,
                    "Xin chào: " + nv.getHoTen() + "\nChức vụ: " + nv.getChucVu() + "\nSĐT: " + nv.getSoDienThoai() + "\n" + ketQua.getThongDiep(),
                    "Đăng nhập thành công", JOptionPane.INFORMATION_MESSAGE);

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

    /**
     * Mở hộp thoại Quên mật khẩu đa bước (Xác thực SĐT + Email -> OTP 6 số -> Đặt lại mật khẩu mới).
     */
    private void moHopThoaiQuenMatKhau() {
        JDialog dlg = new JDialog(this, "KHÔI PHỤC MẬT KHẨU NHÂN VIÊN", true);
        dlg.setSize(500, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);
        dlg.setLayout(new BorderLayout());

        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setBorder(new EmptyBorder(20, 24, 20, 24));
        pnlContent.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Lấy lại mật khẩu qua OTP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Nhập Số điện thoại và Email công việc đã đăng ký với cửa hàng:");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(ThemeColors.TEXT_SECONDARY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtResetPhone = new JTextField(txtPhone.getText().trim());
        txtResetPhone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtResetPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextField txtResetEmail = new JTextField();
        txtResetEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtResetEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtResetEmail.putClientProperty("JTextField.placeholderText", "Ví dụ: minh.quanly@miopet.vn hoặc minh.thungan@miopet.vn");

        JTextField txtOtp = new JTextField();
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtOtp.setHorizontalAlignment(JTextField.CENTER);
        txtOtp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtOtp.setEnabled(false);

        JPasswordField txtNewPass = new JPasswordField();
        txtNewPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNewPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtNewPass.putClientProperty("FlatLaf.style", "showRevealButton: true");
        txtNewPass.setEnabled(false);

        JPasswordField txtConfirmPass = new JPasswordField();
        txtConfirmPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtConfirmPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtConfirmPass.putClientProperty("FlatLaf.style", "showRevealButton: true");
        txtConfirmPass.setEnabled(false);

        final String[] maOtpDaGui = {""};
        final long[] thoiGianGuiOtp = {0L};

        JButton btnGuiOtp = ThemeColors.createSecondaryButton("1. Gửi mã OTP xác nhận", null);
        btnGuiOtp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnXacNhanDoiMK = ThemeColors.createPrimaryButton("2. Đặt lại mật khẩu mới", null);
        btnXacNhanDoiMK.setEnabled(false);
        btnXacNhanDoiMK.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnGuiOtp.addActionListener(e -> {
            if (!utils.EmailThongBaoHelper.isSmtpConfigured()) {
                JOptionPane.showMessageDialog(dlg,
                        "Hệ thống chưa cấu hình tài khoản gửi email trong config/mail.properties.\n"
                        + "Vui lòng nhập địa chỉ Gmail và Mật khẩu ứng dụng (App Password) vào file config/mail.properties!",
                        "Chưa cấu hình Email SMTP", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String sdt = txtResetPhone.getText().trim();
            String mail = txtResetEmail.getText().trim();

            if (sdt.isEmpty() || mail.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập cả Số điện thoại và Email công việc!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            NhanVien nv = dangNhapBus.xacThucQuenMatKhau(sdt, mail);
            if (nv == null) {
                JOptionPane.showMessageDialog(dlg,
                        "Không thể xác thực thông tin tài khoản.",
                        "Thông tin không khớp", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String otp = dangNhapBus.sinhMaOTP();
            maOtpDaGui[0] = otp;
            thoiGianGuiOtp[0] = System.currentTimeMillis();

            // Hiển thị hộp thoại EmailPreviewDialog gửi mã xác thực OTP tới email nhân viên
            String maTK = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getMaTaiKhoan() : nv.getMaNhanVien();
            String htmlOtp = utils.EmailThongBaoHelper.taoEmailKhoiPhucMatKhau(nv.getHoTen(), sdt, maTK, otp);
            boolean guiThanhCong = utils.EmailThongBaoHelper.moHopThoaiGuiEmail(
                    dlg,
                    nv.getTaiKhoan().getEmail(),
                    "[Mio Pet Shop] Mã OTP khôi phục mật khẩu tài khoản " + maTK,
                    htmlOtp
            );
            if (!guiThanhCong) {
                return;
            }

            txtOtp.setEnabled(true);
            txtOtp.setText("");
            txtNewPass.setEnabled(true);
            txtConfirmPass.setEnabled(true);
            btnXacNhanDoiMK.setEnabled(true);
            btnGuiOtp.setText("1. Gửi lại mã OTP xác nhận");
            txtResetPhone.setEditable(false);
            txtResetEmail.setEditable(false);
            txtOtp.requestFocus();
        });

        btnXacNhanDoiMK.addActionListener(e -> {
            if (thoiGianGuiOtp[0] == 0L || System.currentTimeMillis() - thoiGianGuiOtp[0] > 5 * 60 * 1000L) {
                JOptionPane.showMessageDialog(dlg, "Mã OTP đã hết hiệu lực (quá thời hạn 5 phút)! Vui lòng nhấn 'Gửi lại mã OTP xác nhận'.", "OTP hết hạn", JOptionPane.WARNING_MESSAGE);
                txtOtp.setText("");
                txtOtp.requestFocus();
                return;
            }

            String otpNhap = txtOtp.getText().trim();
            String mkMoi = new String(txtNewPass.getPassword()).trim();
            String mkXacNhan = new String(txtConfirmPass.getPassword()).trim();

            if (!otpNhap.equals(maOtpDaGui[0])) {
                JOptionPane.showMessageDialog(dlg, "Mã OTP không chính xác! Vui lòng kiểm tra lại.", "Sai mã OTP", JOptionPane.ERROR_MESSAGE);
                txtOtp.requestFocus();
                return;
            }

            if (mkMoi.length() < 6) {
                JOptionPane.showMessageDialog(dlg, "Mật khẩu mới phải có ít nhất 6 ký tự để đảm bảo an toàn!", "Mật khẩu yếu", JOptionPane.WARNING_MESSAGE);
                txtNewPass.requestFocus();
                return;
            }

            if (!mkMoi.equals(mkXacNhan)) {
                JOptionPane.showMessageDialog(dlg, "Mật khẩu xác nhận không trùng khớp!", "Lỗi xác nhận", JOptionPane.ERROR_MESSAGE);
                txtConfirmPass.requestFocus();
                return;
            }

            boolean ok = dangNhapBus.datLaiMatKhau(txtResetPhone.getText().trim(), mkMoi);
            if (ok) {
                JOptionPane.showMessageDialog(dlg,
                        "Đặt lại mật khẩu thành công!\nMật khẩu mới đã được băm SHA-256 an toàn vào CSDL.\nBạn có thể đăng nhập ngay bây giờ.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                txtPhone.setText(txtResetPhone.getText().trim());
                txtPassword.setText("");
                dlg.dispose();
                txtPassword.requestFocus();
            } else {
                JOptionPane.showMessageDialog(dlg, "Có lỗi xảy ra khi lưu mật khẩu mới xuống CSDL!", "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlContent.add(lblTitle);
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(lblSub);
        pnlContent.add(Box.createVerticalStrut(12));

        pnlContent.add(new JLabel("Số điện thoại nhân viên:"));
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(txtResetPhone);
        pnlContent.add(Box.createVerticalStrut(8));

        pnlContent.add(new JLabel("Email công việc (đã lưu trong hồ sơ):"));
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(txtResetEmail);
        pnlContent.add(Box.createVerticalStrut(10));

        pnlContent.add(btnGuiOtp);
        pnlContent.add(Box.createVerticalStrut(14));

        pnlContent.add(new JLabel("Mã xác thực OTP (6 chữ số):"));
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(txtOtp);
        pnlContent.add(Box.createVerticalStrut(8));

        pnlContent.add(new JLabel("Mật khẩu mới:"));
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(txtNewPass);
        pnlContent.add(Box.createVerticalStrut(8));

        pnlContent.add(new JLabel("Xác nhận mật khẩu mới:"));
        pnlContent.add(Box.createVerticalStrut(4));
        pnlContent.add(txtConfirmPass);
        pnlContent.add(Box.createVerticalStrut(14));

        pnlContent.add(btnXacNhanDoiMK);

        JScrollPane sp = new JScrollPane(pnlContent);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
            UIManager.put("PasswordField.showRevealButton", true);
        } catch (Exception ignored) {
            try {
                com.formdev.flatlaf.FlatLightLaf.setup();
                UIManager.put("PasswordField.showRevealButton", true);
            } catch (Exception ignored2) {}
        }

        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
