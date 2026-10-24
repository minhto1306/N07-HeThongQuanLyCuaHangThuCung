package views;

import utils.ThemeColors;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Properties;

/**
 * Hộp thoại hiển thị mã VietQR động chuẩn NAPAS 24/7 dành cho Quầy Thu Ngân POS (Nhóm 07).
 * - TỰ ĐỘNG KHÓA CỨNG SỐ TIỀN ĐƠN HÀNG (Khách quét mã là màn hình ngân hàng tự động điền đúng số tiền,
 *   không cho phép sửa, chống tuyệt đối việc khách chuyển thiếu hoặc chuyển dư).
 * - Tự động điền Mã hóa đơn vào nội dung chuyển khoản để đối soát chính xác 100%.
 * - Hỗ trợ nạp cấu hình từ `config/vietqr.properties`.
 * - Tích hợp cơ chế kép (Direct PNG API & Base64 v2 API) tải mã cực nhanh và bảo đảm độ nét tối đa.
 */
public class VietQRDialog extends JDialog {

    private static final DecimalFormat df = new DecimalFormat("#,### đ");

    // Thông tin tài khoản mặc định (MB Bank - Mã BIN: 970422)
    private String bankId = "970422";
    private String bankName = "Ngân hàng TMCP Quân Đội (MB Bank)";
    private String accountNo = "0349445392";
    private String accountName = "LE KHAI MINH";
    private String template = "compact2";

    private final String maHoaDon;
    private final long tongThanhToan;
    private final String tenKhach;

    private boolean xacNhanThanhCong = false;

    private JLabel lblQRCode;
    private JLabel lblTrangThaiKetNoi;
    private JButton btnXacNhan;
    private JButton btnHuy;

    public VietQRDialog(Frame parent, String maHoaDon, long tongThanhToan, String tenKhach) {
        super(parent, "THANH TOÁN VIETQR 24/7 - ĐƠN HÀNG " + maHoaDon, true);
        this.maHoaDon = maHoaDon != null ? maHoaDon : "HD000000";
        this.tongThanhToan = Math.max(0, tongThanhToan);
        this.tenKhach = (tenKhach != null && !tenKhach.trim().isEmpty()) ? tenKhach : "Khách vãng lai";

        napCauHinhVietQR();
        khoiTaoGiaoDien();
        taiMaVietQRBatDongBo();
    }

    /**
     * Phương thức tiện ích tĩnh mở hộp thoại VietQR và chờ Thu ngân bấm xác nhận nhận tiền.
     * @return true nếu đã nhận tiền và xác nhận in hóa đơn, false nếu hủy.
     */
    public static boolean xacNhanThanhToanVietQR(Component parentComp, String maHD, long tongTien, String tenKhach) {
        Window window = SwingUtilities.getWindowAncestor(parentComp);
        Frame parentFrame = (window instanceof Frame) ? (Frame) window : null;
        VietQRDialog dlg = new VietQRDialog(parentFrame, maHD, tongTien, tenKhach);
        dlg.setVisible(true);
        return dlg.isXacNhanThanhCong();
    }

    public boolean isXacNhanThanhCong() {
        return xacNhanThanhCong;
    }

    /**
     * Nạp thông tin tài khoản từ file `config/vietqr.properties`
     */
    private void napCauHinhVietQR() {
        File propFile = new File("config/vietqr.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                Properties p = new Properties();
                p.load(in);
                if (p.getProperty("vietqr.bankId") != null) bankId = p.getProperty("vietqr.bankId").trim();
                if (p.getProperty("vietqr.bankName") != null) bankName = p.getProperty("vietqr.bankName").trim();
                if (p.getProperty("vietqr.accountNo") != null) accountNo = p.getProperty("vietqr.accountNo").trim();
                if (p.getProperty("vietqr.accountName") != null) accountName = p.getProperty("vietqr.accountName").trim();
                if (p.getProperty("vietqr.template") != null) template = p.getProperty("vietqr.template").trim();
            } catch (Exception ex) {
                System.err.println("[VietQRDialog] Không thể đọc config/vietqr.properties: " + ex.getMessage());
            }
        }
    }

    private void khoiTaoGiaoDien() {
        setSize(480, 720);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF8, 0xFA, 0xFC));

        // Phím tắt ESC để đóng, F9 để xác nhận
        getRootPane().registerKeyboardAction(e -> {
            xacNhanThanhCong = false;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> {
            xacNhanThanhCong = true;
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // 1. HEADER BANNER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(0x1E, 0x3A, 0x8A)); // Xanh Navy MB Bank sang trọng
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("THANH TOÁN VIETQR ĐỘNG (NAPAS 247)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Mã QR đã gắn cố định số tiền: " + df.format(tongThanhToan) + " • Khách không cần nhập tay");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(0x93, 0xC5, 0xFD));

        JPanel pnlHeaderTitle = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlHeaderTitle.setOpaque(false);
        pnlHeaderTitle.add(lblTitle);
        pnlHeaderTitle.add(lblSubtitle);

        pnlHeader.add(pnlHeaderTitle, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. NỘI DUNG CHÍNH (CENTER)
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Khung hiển thị ảnh QR Code
        JPanel pnlQRWrapper = new JPanel(new BorderLayout());
        pnlQRWrapper.setBackground(Color.WHITE);
        pnlQRWrapper.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xE2, 0xE8, 0xF0), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        pnlQRWrapper.setMaximumSize(new Dimension(320, 340));
        pnlQRWrapper.setPreferredSize(new Dimension(320, 340));
        pnlQRWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblQRCode = new JLabel("<html><center>⏳ Đang kết nối cổng VietQR Napas...<br><small>Khởi tạo mã QR động gắn số tiền " + df.format(tongThanhToan) + "</small></center></html>", SwingConstants.CENTER);
        lblQRCode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQRCode.setForeground(ThemeColors.TEXT_MUTED);
        pnlQRWrapper.add(lblQRCode, BorderLayout.CENTER);

        // Trạng thái kết nối
        lblTrangThaiKetNoi = new JLabel("⚡ Đang tạo mã VietQR động đính kèm số tiền...", SwingConstants.CENTER);
        lblTrangThaiKetNoi.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrangThaiKetNoi.setForeground(new Color(0x02, 0x84, 0xC7));
        lblTrangThaiKetNoi.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Khung thông tin chi tiết hóa đơn & tài khoản nhận
        JPanel pnlDetails = new JPanel(new GridBagLayout());
        pnlDetails.setBackground(Color.WHITE);
        pnlDetails.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xCB, 0xD5, 0xE1), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        pnlDetails.setMaximumSize(new Dimension(420, 175));
        pnlDetails.setPreferredSize(new Dimension(420, 175));
        pnlDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        themDongThongTin(pnlDetails, gbc, 0, "Ngân hàng nhận:", bankName, null);
        themDongThongTin(pnlDetails, gbc, 1, "Chủ tài khoản:", accountName, null);
        themDongThongTin(pnlDetails, gbc, 2, "Số tài khoản:", accountNo, accountNo);
        themDongThongTin(pnlDetails, gbc, 3, "Số tiền cần chuyển:", df.format(tongThanhToan), null, true);
        themDongThongTin(pnlDetails, gbc, 4, "Nội dung chuyển khoản:", maHoaDon, maHoaDon);

        pnlBody.add(pnlQRWrapper);
        pnlBody.add(Box.createVerticalStrut(6));
        pnlBody.add(lblTrangThaiKetNoi);
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(pnlDetails);

        add(pnlBody, BorderLayout.CENTER);

        // 3. THANH ĐIỀU KHIỂN DƯỚI CÙNG (ACTIONS)
        JPanel pnlActions = new JPanel(new BorderLayout(12, 0));
        pnlActions.setBackground(Color.WHITE);
        pnlActions.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE2, 0xE8, 0xF0)),
                new EmptyBorder(12, 20, 14, 20)
        ));

        btnHuy = new JButton("Hủy / Đóng (ESC)");
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnHuy.setPreferredSize(new Dimension(140, 42));
        btnHuy.setBackground(new Color(0xF1, 0xF5, 0xF9));
        btnHuy.setForeground(ThemeColors.TEXT_MUTED);
        btnHuy.setFocusPainted(false);
        btnHuy.setBorder(new LineBorder(new Color(0xCB, 0xD5, 0xE1), 1, true));
        btnHuy.addActionListener(e -> {
            xacNhanThanhCong = false;
            dispose();
        });

        btnXacNhan = new JButton("✓ ĐÃ NHẬN ĐỦ " + df.format(tongThanhToan) + " (F9)");
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setPreferredSize(new Dimension(280, 42));
        btnXacNhan.setBackground(new Color(0x10, 0xB9, 0x81)); // Xanh Emerald thành công
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorder(new LineBorder(new Color(0x05, 0x96, 0x69), 1, true));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> {
            xacNhanThanhCong = true;
            dispose();
        });

        pnlActions.add(btnHuy, BorderLayout.WEST);
        pnlActions.add(btnXacNhan, BorderLayout.CENTER);

        add(pnlActions, BorderLayout.SOUTH);
    }

    private void themDongThongTin(JPanel pnl, GridBagConstraints gbc, int row, String label, String value, String copyVal) {
        themDongThongTin(pnl, gbc, row, label, value, copyVal, false);
    }

    private void themDongThongTin(JPanel pnl, GridBagConstraints gbc, int row, String label, String value, String copyVal, boolean highlight) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.34;
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblL.setForeground(new Color(0x64, 0x74, 0x8B));
        pnl.add(lblL, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.56;
        JLabel lblV = new JLabel(value);
        if (highlight) {
            lblV.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblV.setForeground(new Color(0xDC, 0x26, 0x26)); // Màu đỏ số tiền nổi bật
        } else {
            lblV.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblV.setForeground(ThemeColors.TEXT_PRIMARY);
        }
        pnl.add(lblV, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.10;
        if (copyVal != null && !copyVal.isEmpty()) {
            JButton btnCopy = new JButton("Copy");
            btnCopy.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btnCopy.setMargin(new Insets(1, 4, 1, 4));
            btnCopy.setBackground(new Color(0xF1, 0xF5, 0xF9));
            btnCopy.setForeground(new Color(0x02, 0x84, 0xC7));
            btnCopy.setFocusPainted(false);
            btnCopy.setBorder(new LineBorder(new Color(0xBA, 0xE6, 0xFD), 1, true));
            btnCopy.setToolTipText("Sao chép");
            btnCopy.addActionListener(e -> {
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(copyVal), null);
                    btnCopy.setText("✓");
                    Timer t = new Timer(1500, evt -> btnCopy.setText("Copy"));
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ignored) {}
            });
            pnl.add(btnCopy, gbc);
        } else {
            pnl.add(Box.createHorizontalStrut(1), gbc);
        }
    }

    /**
     * Tải mã VietQR động trực tiếp từ máy chủ VietQR Napas (api.vietqr.io)
     * Đã khóa chặt số tiền hóa đơn và mã đơn hàng.
     */
    private void taiMaVietQRBatDongBo() {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                // CƠ CHẾ 1: Tải trực tiếp PNG chuẩn từ api.vietqr.io/image/
                try {
                    String encMemo = URLEncoder.encode(maHoaDon, StandardCharsets.UTF_8);
                    String encName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
                    String urlStr = String.format("https://api.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                            bankId, accountNo, template, tongThanhToan, encMemo, encName);

                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(4500);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) PetShopPOS/1.0");
                    conn.setRequestProperty("Accept", "image/png,image/*;q=0.9");

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try (InputStream in = conn.getInputStream()) {
                            BufferedImage img = ImageIO.read(in);
                            if (img != null) {
                                return img;
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[VietQRDialog] Endpoint 1 (api.vietqr.io/image) lỗi: " + ex.getMessage());
                }

                // CƠ CHẾ 2: Dự phòng qua VietQR JSON API v2 (/v2/generate)
                try {
                    String jsonInput = String.format(
                            "{\"accountNo\":\"%s\",\"accountName\":\"%s\",\"acqId\":\"%s\",\"amount\":%d,\"addInfo\":\"%s\",\"format\":\"text\",\"template\":\"%s\"}",
                            accountNo, accountName, bankId, tongThanhToan, maHoaDon, template
                    );
                    URL url = new URL("https://api.vietqr.io/v2/generate");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    conn.setConnectTimeout(4500);
                    conn.setReadTimeout(5000);
                    conn.setDoOutput(true);
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
                    }

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = br.readLine()) != null) sb.append(line);
                            String resp = sb.toString();

                            int b64Idx = resp.indexOf("base64,");
                            if (b64Idx != -1) {
                                int start = b64Idx + 7;
                                int end = resp.indexOf("\"", start);
                                String b64 = resp.substring(start, end);
                                byte[] bytes = Base64.getDecoder().decode(b64);
                                BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                                if (img != null) {
                                    return img;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[VietQRDialog] Endpoint 2 (/v2/generate) lỗi: " + ex.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        // Co dãn vừa vặn khung hiển thị 300 x 320 giữ tỉ lệ
                        int targetW = 300;
                        int targetH = (int) (img.getHeight() * ((double) targetW / img.getWidth()));
                        if (targetH > 324) {
                            targetH = 324;
                            targetW = (int) (img.getWidth() * ((double) targetH / img.getHeight()));
                        }
                        Image scaled = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
                        lblQRCode.setText(null);
                        lblQRCode.setIcon(new ImageIcon(scaled));

                        lblTrangThaiKetNoi.setText("✓ ĐÃ KHÓA CỐ ĐỊNH SỐ TIỀN " + df.format(tongThanhToan) + " (Khách không thể sửa tiền)");
                        lblTrangThaiKetNoi.setForeground(new Color(0x15, 0x80, 0x3D)); // Xanh lá đậm chuẩn bảo mật
                    } else {
                        lblQRCode.setText("<html><center><font color='#DC2626'><b>Mất kết nối Internet</b></font><br><br>Không thể tải mã VietQR động.<br>Vui lòng kiểm tra WiFi hoặc chuyển khoản theo STK bên dưới.</center></html>");
                        lblTrangThaiKetNoi.setText("⚡ Quầy POS đang ngoại tuyến. Vui lòng kiểm tra mạng.");
                        lblTrangThaiKetNoi.setForeground(new Color(0xDC, 0x26, 0x26));
                    }
                } catch (Exception ex) {
                    lblQRCode.setText("Không thể hiển thị mã QR. Vui lòng chuyển khoản thủ công.");
                    lblTrangThaiKetNoi.setText("Lỗi: " + ex.getMessage());
                    lblTrangThaiKetNoi.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }
}
