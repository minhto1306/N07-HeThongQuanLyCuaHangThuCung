package views.charts;

import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;

/**
 * Biểu đồ Tròn Donut (Donut Chart) vẽ trực tiếp bằng Graphics2D thuần Java.
 * - Hiển thị Cơ cấu Doanh thu theo 3 nhóm ngành: Spa/Grooming (45%), Thức ăn (35%), Pet Hotel (20%).
 * - Vòng tròn trung tâm đếm tiến độ hoàn thành dịch vụ: 24/30 Đơn đã xong (80%).
 * - Chú thích (Legend) màu sắc kèm số liệu và tỷ lệ % trực quan.
 */
public class DonutChartPanel extends JPanel {

    private final DecimalFormat dfMoney = new DecimalFormat("#,### đ");

    public static class SliceData {
        public String name;
        public double percentage;
        public long revenue;
        public Color color;

        public SliceData(String name, double percentage, long revenue, Color color) {
            this.name = name;
            this.percentage = percentage;
            this.revenue = revenue;
            this.color = color;
        }
    }

    private SliceData[] slices;
    private int completedServices = 24;
    private int totalServices = 30;
    private JLabel[] lblSliceValues;
    private JLabel lblGiaTriTong;

    public DonutChartPanel() {
        initDefaultData();
        setLayout(new BorderLayout(12, 10));
        setBackground(ThemeColors.SURFACE);
        setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        setPreferredSize(new Dimension(420, 290));

        // Header Panel
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("CƠ CẤU DOANH THU & TIẾN ĐỘ",
                IconHelper.getIcon(IconHelper.SPARKLES, 18, ThemeColors.PRIMARY_ACTIVE), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);

        JLabel lblSub = new JLabel("Tỷ trọng ngành • Tiến độ hoàn tất", SwingConstants.RIGHT);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(ThemeColors.TEXT_MUTED);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(lblSub, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Body: Donut Canvas (Trái) + Legend (Phải)
        JPanel pnlBody = new JPanel(new BorderLayout(10, 0));
        pnlBody.setOpaque(false);

        DonutCanvas canvas = new DonutCanvas();
        canvas.setPreferredSize(new Dimension(200, 210));
        pnlBody.add(canvas, BorderLayout.CENTER);

        JPanel pnlLegend = taoPanelLegend();
        pnlLegend.setPreferredSize(new Dimension(185, 210));
        pnlBody.add(pnlLegend, BorderLayout.EAST);

        add(pnlBody, BorderLayout.CENTER);
    }

    private void initDefaultData() {
        slices = new SliceData[]{
                new SliceData("Spa & Grooming", 45.0, 11_250_000L, new Color(0xFF, 0x8B, 0xA7)), // Hồng san hô #FF8BA7
                new SliceData("Thức ăn thú cưng", 35.0, 8_750_000L, new Color(0xC3, 0xF0, 0xCA)), // Mint pastel #C3F0CA
                new SliceData("Pet Hotel & Khác", 20.0, 5_000_000L, new Color(0xFF, 0xC6, 0xC7))  // Hồng phấn #FFC6C7
        };
    }

    /**
     * Cập nhật động cơ cấu doanh thu 3 nhóm ngành và tiến độ hoàn thành dịch vụ trong ngày.
     */
    public void capNhatCoCauDoanhThu(long dtSpa, long dtHangHoa, long dtKhac, int dichVuXong, int tongDichVu) {
        if (slices == null || slices.length < 3) {
            initDefaultData();
        }
        long total = dtSpa + dtHangHoa + dtKhac;
        double pctSpa;
        double pctHangHoa;
        double pctKhac;

        if (total > 0) {
            pctSpa = Math.round((double) dtSpa * 100.0 / total);
            pctHangHoa = Math.round((double) dtHangHoa * 100.0 / total);
            pctKhac = Math.max(0.0, 100.0 - pctSpa - pctHangHoa);
        } else {
            pctSpa = 45.0;
            pctHangHoa = 35.0;
            pctKhac = 20.0;
        }

        slices[0].percentage = pctSpa;
        slices[0].revenue = Math.max(0L, dtSpa);

        slices[1].percentage = pctHangHoa;
        slices[1].revenue = Math.max(0L, dtHangHoa);

        slices[2].percentage = pctKhac;
        slices[2].revenue = Math.max(0L, dtKhac);

        this.completedServices = Math.max(0, dichVuXong);
        this.totalServices = Math.max(1, tongDichVu);

        if (lblSliceValues != null) {
            for (int i = 0; i < slices.length && i < lblSliceValues.length; i++) {
                if (lblSliceValues[i] != null) {
                    lblSliceValues[i].setText((int) Math.round(slices[i].percentage) + "% • " + dfMoney.format(slices[i].revenue));
                }
            }
        }
        if (lblGiaTriTong != null) {
            lblGiaTriTong.setText(dfMoney.format(Math.max(0L, total)));
        }

        revalidate();
        repaint();
    }

    private JPanel taoPanelLegend() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 4, 0);

        lblSliceValues = new JLabel[slices.length];
        int row = 0;
        for (int i = 0; i < slices.length; i++) {
            gbc.gridy = row++;
            pnl.add(taoItemLegend(slices[i], i), gbc);
        }

        // Card tổng kết bên dưới legend
        gbc.gridy = row;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel pnlTotal = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTotal.setBackground(new Color(0xF8, 0xFA, 0xFC));
        pnlTotal.setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblTieuDeTong = new JLabel("TỔNG DOANH THU");
        lblTieuDeTong.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTieuDeTong.setForeground(ThemeColors.TEXT_MUTED);

        long initialTotal = 0L;
        for (SliceData s : slices) initialTotal += s.revenue;
        lblGiaTriTong = new JLabel(dfMoney.format(initialTotal));
        lblGiaTriTong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiaTriTong.setForeground(ThemeColors.PRIMARY_ACTIVE);

        pnlTotal.add(lblTieuDeTong);
        pnlTotal.add(lblGiaTriTong);
        pnl.add(pnlTotal, gbc);

        return pnl;
    }

    private JPanel taoItemLegend(SliceData s, int index) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setOpaque(false);

        // Chip màu vuông bo góc
        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(s.color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                g2.dispose();
            }
        };
        chip.setPreferredSize(new Dimension(12, 12));
        chip.setOpaque(false);

        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 1));
        pnlText.setOpaque(false);

        JLabel lblName = new JLabel(s.name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblName.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel lblVal = new JLabel((int) Math.round(s.percentage) + "% • " + dfMoney.format(s.revenue));
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblVal.setForeground(ThemeColors.TEXT_MUTED);
        if (lblSliceValues != null && index >= 0 && index < lblSliceValues.length) {
            lblSliceValues[index] = lblVal;
        }

        pnlText.add(lblName);
        pnlText.add(lblVal);

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        pnlLeft.setOpaque(false);
        pnlLeft.add(chip);

        item.add(pnlLeft, BorderLayout.WEST);
        item.add(pnlText, BorderLayout.CENTER);
        return item;
    }

    private class DonutCanvas extends JPanel {
        public DonutCanvas() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            // Khử răng cưa tuyệt đối
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int width = getWidth();
            int height = getHeight();

            int size = Math.min(width, height) - 20;
            if (size <= 0) {
                g2.dispose();
                return;
            }

            int x = (width - size) / 2;
            int y = (height - size) / 2;

            // 1. Vẽ các lát cắt hình tròn (Pie Slices)
            double startAngle = 90.0; // Bắt đầu từ góc 12 giờ
            for (SliceData s : slices) {
                double extent = -s.percentage * 3.6; // Đổi % sang độ góc
                Arc2D.Double arc = new Arc2D.Double(x, y, size, size, startAngle, extent, Arc2D.PIE);
                g2.setColor(s.color);
                g2.fill(arc);

                // Viền trắng phân cách giữa các lát
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(arc);

                startAngle += extent;
            }

            // 2. Vẽ hình tròn rỗng ở tâm để tạo hình Donut
            int holeSize = (int) (size * 0.62);
            int holeX = x + (size - holeSize) / 2;
            int holeY = y + (size - holeSize) / 2;

            Ellipse2D.Double hole = new Ellipse2D.Double(holeX, holeY, holeSize, holeSize);
            g2.setColor(ThemeColors.SURFACE);
            g2.fill(hole);

            g2.setColor(ThemeColors.BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(hole);

            // 3. Hiển thị thông số trung tâm (Tiến độ dịch vụ)
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.setColor(ThemeColors.PRIMARY_ACTIVE);
            String line1 = completedServices + "/" + totalServices;
            FontMetrics fm1 = g2.getFontMetrics();
            int l1X = holeX + (holeSize - fm1.stringWidth(line1)) / 2;
            int l1Y = holeY + (holeSize / 2) - 4;
            g2.drawString(line1, l1X, l1Y);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.setColor(ThemeColors.SUCCESS);
            int pct = (int) Math.round((double) completedServices / totalServices * 100);
            String line2 = "Đã xong (" + pct + "%)";
            FontMetrics fm2 = g2.getFontMetrics();
            int l2X = holeX + (holeSize - fm2.stringWidth(line2)) / 2;
            int l2Y = l1Y + 15;
            g2.drawString(line2, l2X, l2Y);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2.setColor(ThemeColors.TEXT_MUTED);
            String line3 = "Dịch vụ hôm nay";
            FontMetrics fm3 = g2.getFontMetrics();
            int l3X = holeX + (holeSize - fm3.stringWidth(line3)) / 2;
            int l3Y = l2Y + 12;
            g2.drawString(line3, l3X, l3Y);

            g2.dispose();
        }
    }
}
