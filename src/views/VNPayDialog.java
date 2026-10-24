package views;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import utils.ThemeColors;
import utils.VNPayHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Hộp thoại tích hợp Cổng thanh toán VNPay Sandbox v2.1.0 dành cho Quầy Thu Ngân POS (Nhóm 07).
 * - Tự động tạo URL thanh toán chuẩn bảo mật HMAC-SHA512.
 * - Hiển thị mã QR thanh toán và hỗ trợ mở thẳng trình duyệt Web (Chrome/Edge).
 * - Tích hợp Mini-Server cục bộ (Local Loopback Server) tự động đón nhận kết quả thanh toán
 *   khi khách trả tiền thành công trên cổng VNPay (tự động chốt đơn mà không cần bấm tay).
 * - Cung cấp sẵn thông tin Thẻ ngân hàng test NCB (Số thẻ, Tên, OTP) có nút Copy nhanh để demo.
 */
public class VNPayDialog extends JDialog {

    private static final DecimalFormat df = new DecimalFormat("#,### đ");

    private final String maHoaDon;
    private final long tongThanhToan;
    private final String tenKhach;

    private boolean xacNhanThanhCong = false;
    private String paymentUrl;
    private HttpServer localServer;

    private JLabel lblQRCode;
    private JLabel lblTrangThai;
    private JButton btnMoWeb;
    private JButton btnXacNhan;
    private JButton btnHuy;

    public VNPayDialog(Frame parent, String maHoaDon, long tongThanhToan, String tenKhach) {
        super(parent, "CỔNG THANH TOÁN VNPAY SANDBOX - ĐƠN HÀNG " + maHoaDon, true);
        this.maHoaDon = maHoaDon != null ? maHoaDon : "HD000000";
        this.tongThanhToan = Math.max(0, tongThanhToan);
        this.tenKhach = (tenKhach != null && !tenKhach.trim().isEmpty()) ? tenKhach : "Khách vãng lai";

        khoiTaoServerLangNghe();
        khoiTaoGiaoDien();
        taiMaQRBatDongBo();
    }

    public static boolean xacNhanThanhToanVNPay(Component parentComp, String maHD, long tongTien, String tenKhach) {
        Window window = SwingUtilities.getWindowAncestor(parentComp);
        Frame parentFrame = (window instanceof Frame) ? (Frame) window : null;
        VNPayDialog dlg = new VNPayDialog(parentFrame, maHD, tongTien, tenKhach);
        dlg.setVisible(true);
        return dlg.isXacNhanThanhCong();
    }

    public boolean isXacNhanThanhCong() {
        return xacNhanThanhCong;
    }

    /**
     * Khởi động Mini Server cục bộ để nhận kết quả chuyển hướng tự động từ VNPay
     */
    private void khoiTaoServerLangNghe() {
        try {
            int port = VNPayHelper.getReturnPort();
            String path = VNPayHelper.getReturnPath();
            String returnUrl = "http://localhost:" + port + path;

            // Tạo URL thanh toán VNPay
            this.paymentUrl = VNPayHelper.taoUrlThanhToan(maHoaDon, tongThanhToan, returnUrl);

            localServer = HttpServer.create(new InetSocketAddress(port), 0);
            localServer.createContext(path, new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String query = exchange.getRequestURI().getQuery();
                    Map<String, String> params = phanTichQuery(query);

                    String responseCode = params.get("vnp_ResponseCode");
                    String txnRef = params.get("vnp_TxnRef");
                    boolean hopLe = VNPayHelper.xacThucChuKy(params);
                    boolean thanhCong = "00".equals(responseCode);

                    // Phản hồi HTML cho Trình duyệt
                    String html;
                    if (thanhCong) {
                        html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Thanh toán thành công</title></head>"
                                + "<body style='font-family:Segoe UI,sans-serif;text-align:center;padding:50px;background:#F8FAFC;'>"
                                + "<div style='display:inline-block;background:white;padding:40px;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.08);'>"
                                + "<h1 style='color:#10B981;font-size:32px;margin-bottom:10px;'>✓ THANH TOÁN THÀNH CÔNG!</h1>"
                                + "<p style='font-size:18px;color:#334155;'>Đơn hàng <b>" + maHoaDon + "</b> đã được Cổng VNPay xác nhận.</p>"
                                + "<p style='font-size:22px;color:#0284C7;font-weight:bold;margin:16px 0;'>" + df.format(tongThanhToan) + "</p>"
                                + "<p style='color:#64748B;'>Bạn có thể đóng tab này và quay lại ứng dụng Pet Shop POS.</p>"
                                + "</div></body></html>";
                    } else {
                        html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Giao dịch thất bại</title></head>"
                                + "<body style='font-family:Segoe UI,sans-serif;text-align:center;padding:50px;background:#F8FAFC;'>"
                                + "<div style='display:inline-block;background:white;padding:40px;border-radius:12px;box-shadow:0 4px 12px rgba(0,0,0,0.08);'>"
                                + "<h1 style='color:#EF4444;font-size:32px;margin-bottom:10px;'>✕ GIAO DỊCH KHÔNG THÀNH CÔNG</h1>"
                                + "<p style='font-size:16px;color:#64748B;'>Mã phản hồi VNPay: <b>" + responseCode + "</b></p>"
                                + "</div></body></html>";
                    }

                    byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }

                    // Đóng server sau khi phản hồi
                    new Thread(() -> {
                        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                        dungServer();
                    }).start();

                    // Cập nhật giao diện Swing
                    SwingUtilities.invokeLater(() -> {
                        if (thanhCong) {
                            xacNhanThanhCong = true;
                            if (lblTrangThai != null) {
                                lblTrangThai.setText("✓ Cổng VNPay đã xác nhận thanh toán thành công!");
                                lblTrangThai.setForeground(new Color(0x15, 0x80, 0x3D));
                            }
                            Timer t = new Timer(800, evt -> dispose());
                            t.setRepeats(false);
                            t.start();
                        } else {
                            if (lblTrangThai != null) {
                                lblTrangThai.setText("✕ Giao dịch VNPay thất bại hoặc bị hủy (Mã: " + responseCode + ")");
                                lblTrangThai.setForeground(Color.RED);
                            }
                        }
                    });
                }
            });

            localServer.setExecutor(null);
            localServer.start();
        } catch (Exception ex) {
            System.err.println("[VNPayDialog] Không thể khởi động Local Server: " + ex.getMessage());
        }
    }

    private void dungServer() {
        if (localServer != null) {
            try {
                localServer.stop(0);
                localServer = null;
            } catch (Exception ignored) {}
        }
    }

    private void khoiTaoGiaoDien() {
        setSize(520, 750);
        setResizable(false);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0xF8, 0xFA, 0xFC));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dungServer();
            }
        });

        getRootPane().registerKeyboardAction(e -> {
            xacNhanThanhCong = false;
            dungServer();
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> {
            xacNhanThanhCong = true;
            dungServer();
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // 1. HEADER VNPAY (Xanh dương & Đỏ thương hiệu VNPay)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(0x00, 0x5B, 0xA8)); // Màu xanh thương hiệu VNPay
        pnlHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel("CỔNG THANH TOÁN VNPAY (SANDBOX)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Hỗ trợ quét VNPay-QR, Thẻ ATM Nội Địa (NCB) và Thẻ Quốc Tế");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(0xBA, 0xE6, 0xFD));

        JPanel pnlHeaderTitle = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlHeaderTitle.setOpaque(false);
        pnlHeaderTitle.add(lblTitle);
        pnlHeaderTitle.add(lblSubtitle);

        pnlHeader.add(pnlHeaderTitle, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. BODY
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Khung QR Code
        JPanel pnlQRWrapper = new JPanel(new BorderLayout());
        pnlQRWrapper.setBackground(Color.WHITE);
        pnlQRWrapper.setBorder(new CompoundBorder(
                new LineBorder(new Color(0x00, 0x5B, 0xA8), 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        pnlQRWrapper.setMaximumSize(new Dimension(280, 280));
        pnlQRWrapper.setPreferredSize(new Dimension(280, 280));
        pnlQRWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblQRCode = new JLabel("<html><center>⏳ Đang tạo mã VNPay-QR...<br><small>Số tiền: " + df.format(tongThanhToan) + "</small></center></html>", SwingConstants.CENTER);
        lblQRCode.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQRCode.setForeground(ThemeColors.TEXT_MUTED);
        pnlQRWrapper.add(lblQRCode, BorderLayout.CENTER);

        // Nút mở Web Browser trực tiếp
        btnMoWeb = new JButton("🌐 MỞ TRANG THANH TOÁN VNPAY (ATM / QR)");
        btnMoWeb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnMoWeb.setBackground(new Color(0xED, 0x1C, 0x24)); // Đỏ nổi bật VNPay
        btnMoWeb.setForeground(Color.WHITE);
        btnMoWeb.setFocusPainted(false);
        btnMoWeb.setBorder(new EmptyBorder(10, 16, 10, 16));
        btnMoWeb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMoWeb.setMaximumSize(new Dimension(460, 40));
        btnMoWeb.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMoWeb.addActionListener(e -> moTrinhDuyetVNPay());

        lblTrangThai = new JLabel("⚡ Đang chờ khách thanh toán trên cổng VNPay...", SwingConstants.CENTER);
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTrangThai.setForeground(new Color(0x02, 0x84, 0xC7));
        lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Card thông tin thẻ test NCB Sandbox
        JPanel pnlCardTest = new JPanel(new GridBagLayout());
        pnlCardTest.setBackground(Color.WHITE);
        pnlCardTest.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xBA, 0xE6, 0xFD), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        pnlCardTest.setMaximumSize(new Dimension(460, 180));
        pnlCardTest.setPreferredSize(new Dimension(460, 180));
        pnlCardTest.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        themDong(pnlCardTest, gbc, 0, "Số tiền thanh toán:", df.format(tongThanhToan), null, true);
        themDong(pnlCardTest, gbc, 1, "Ngân hàng test:", "NCB (Ngân hàng Quốc Dân)", null, false);
        themDong(pnlCardTest, gbc, 2, "Số thẻ ATM test:", "9704198526191432198", "9704198526191432198", false);
        themDong(pnlCardTest, gbc, 3, "Tên chủ thẻ:", "NGUYEN VAN A", "NGUYEN VAN A", false);
        themDong(pnlCardTest, gbc, 4, "Ngày phát hành:", "07/15", "07/15", false);
        themDong(pnlCardTest, gbc, 5, "Mật khẩu OTP:", "123456", "123456", false);

        pnlBody.add(pnlQRWrapper);
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(btnMoWeb);
        pnlBody.add(Box.createVerticalStrut(6));
        pnlBody.add(lblTrangThai);
        pnlBody.add(Box.createVerticalStrut(8));
        pnlBody.add(pnlCardTest);

        add(pnlBody, BorderLayout.CENTER);

        // 3. ACTIONS
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
            dungServer();
            dispose();
        });

        btnXacNhan = new JButton("✓ XÁC NHẬN ĐÃ THANH TOÁN (F9)");
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setPreferredSize(new Dimension(280, 42));
        btnXacNhan.setBackground(new Color(0x10, 0xB9, 0x81));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorder(new LineBorder(new Color(0x05, 0x96, 0x69), 1, true));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> {
            xacNhanThanhCong = true;
            dungServer();
            dispose();
        });

        pnlActions.add(btnHuy, BorderLayout.WEST);
        pnlActions.add(btnXacNhan, BorderLayout.CENTER);

        add(pnlActions, BorderLayout.SOUTH);
    }

    private void moTrinhDuyetVNPay() {
        if (paymentUrl == null || paymentUrl.isEmpty()) return;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(paymentUrl));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + paymentUrl);
            }
            lblTrangThai.setText("Đã mở trình duyệt! Khách vui lòng thanh toán trên trang web VNPay...");
            lblTrangThai.setForeground(new Color(0x02, 0x84, 0xC7));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở trình duyệt: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void themDong(JPanel pnl, GridBagConstraints gbc, int row, String label, String value, String copyVal, boolean highlight) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.36;
        JLabel lblL = new JLabel(label);
        lblL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblL.setForeground(new Color(0x64, 0x74, 0x8B));
        pnl.add(lblL, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.54;
        JLabel lblV = new JLabel(value);
        if (highlight) {
            lblV.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblV.setForeground(new Color(0xDC, 0x26, 0x26));
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
     * Tải mã QR dẫn thẳng tới liên kết thanh toán VNPay
     */
    private void taiMaQRBatDongBo() {
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                try {
                    // Tạo mã QR dẫn đến paymentUrl bằng API sinh QR nhanh
                    String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=260x260&data="
                            + URLEncoder.encode(paymentUrl, StandardCharsets.UTF_8);

                    URL url = new URL(qrApiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(4000);
                    conn.setReadTimeout(5000);
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                    if (conn.getResponseCode() == 200) {
                        try (InputStream in = conn.getInputStream()) {
                            return ImageIO.read(in);
                        }
                    }
                } catch (Exception ignored) {}

                // Fallback nếu mất mạng: Tự vẽ một khung icon VNPay
                return taoHinhAnhFallback(260, 260);
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    if (img != null) {
                        Image scaled = img.getScaledInstance(260, 260, Image.SCALE_SMOOTH);
                        lblQRCode.setText(null);
                        lblQRCode.setIcon(new ImageIcon(scaled));
                    }
                } catch (Exception ignored) {}
            }
        };

        worker.execute();
    }

    private BufferedImage taoHinhAnhFallback(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        g.setColor(new Color(0x00, 0x5B, 0xA8));
        g.fillRect(0, 0, w, 45);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.drawString("VNPAY SANDBOX 2.1.0", 50, 28);

        g.setColor(new Color(0x64, 0x74, 0x8B));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.drawString("Bấm nút đỏ bên dưới để mở", 50, 110);
        g.drawString("Trang thanh toán VNPay trên Web", 35, 135);
        g.drawString("Số tiền: " + df.format(tongThanhToan), 65, 165);

        g.dispose();
        return bi;
    }

    private static Map<String, String> phanTichQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0 && idx < pair.length() - 1) {
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    result.put(key, value);
                } catch (Exception ignored) {}
            }
        }
        return result;
    }
}
