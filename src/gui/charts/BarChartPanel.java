package gui.charts;

import bus.ThongKeBus;
import utils.IconHelper;
import utils.ThemeColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Biểu đồ Cột (Bar Chart) vẽ trực tiếp bằng Graphics2D thuần Java.
 * - Hiển thị xu hướng Doanh thu 7 ngày trong tuần (6 ngày trước + Hôm nay) 100% từ CSDL SQL Server qua ThongKeBus.
 * - Cột cao nhất trong tuần được highlight bằng màu cam rực rỡ (ThemeColors.PRIMARY).
 * - Tương tác di chuột (Hover Tooltip) hiển thị doanh thu chính xác và số lượng hóa đơn.
 */
public class BarChartPanel extends JPanel {

    private final DecimalFormat dfMoney = new DecimalFormat("#,### đ");

    public static class DayData {
        public String dayLabel;
        public String dateLabel;
        public long revenue;
        public int orders;

        public DayData(String dayLabel, String dateLabel, long revenue, int orders) {
            this.dayLabel = dayLabel;
            this.dateLabel = dateLabel;
            this.revenue = revenue;
            this.orders = orders;
        }
    }

    private DayData[] days;
    private int hoverIndex = -1;
    private Point mousePoint = null;

    public BarChartPanel() {
        napDuLieuThucTe();
        setLayout(new BorderLayout());
        setBackground(ThemeColors.SURFACE);
        setBorder(new CompoundBorder(
                new LineBorder(ThemeColors.BORDER, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        setPreferredSize(new Dimension(500, 290));

        // Header Panel
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblTitle = new JLabel("XU HƯỚNG DOANH THU 7 NGÀY GẦN NHẤT (CSDL THỰC TẾ)",
                IconHelper.getIcon(IconHelper.BAR_CHART, 18, ThemeColors.PRIMARY_ACTIVE), SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        lblTitle.setIconTextGap(8);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // Canvas vẽ biểu đồ
        ChartCanvas canvas = new ChartCanvas();
        add(canvas, BorderLayout.CENTER);
    }

    /**
     * Nạp dữ liệu doanh thu thực tế 7 ngày gần nhất từ CSDL qua ThongKeBus (không hardcode dữ liệu ảo).
     */
    public void napDuLieuThucTe() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        days = new DayData[7];

        ThongKeBus thongKeBus = new ThongKeBus();
        long[] revs = thongKeBus.layDoanhThu7NgayGanNhat();
        int[] ords = thongKeBus.laySoDon7NgayGanNhat();

        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            String dayLbl = (i == 6) ? "Hôm nay" : layTenThu(d.getDayOfWeek());
            days[i] = new DayData(dayLbl, d.format(fmt), revs[i], ords[i]);
        }
        revalidate();
        repaint();
    }

    private String layTenThu(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "T2";
            case TUESDAY: return "T3";
            case WEDNESDAY: return "T4";
            case THURSDAY: return "T5";
            case FRIDAY: return "T6";
            case SATURDAY: return "T7";
            case SUNDAY:
            default: return "CN";
        }
    }

    /**
     * Cập nhật doanh thu và số lượng đơn hàng thực tế của ngày hôm nay (cột thứ 7 cuối cùng).
     */
    public void capNhatDoanhThuHomNay(long doanhThuHomNay, int soDonHomNay) {
        if (days == null || days.length < 7) {
            napDuLieuThucTe();
        }

        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 0; i < days.length; i++) {
            LocalDate d = today.minusDays(days.length - 1 - i);
            days[i].dayLabel = (i == days.length - 1) ? "Hôm nay" : layTenThu(d.getDayOfWeek());
            days[i].dateLabel = d.format(fmt);
        }

        DayData homNay = days[days.length - 1];
        homNay.revenue = Math.max(0L, doanhThuHomNay);
        homNay.orders = Math.max(0, soDonHomNay);

        revalidate();
        repaint();
    }

    private class ChartCanvas extends JPanel {
        private final Rectangle[] barRects = new Rectangle[7];

        public ChartCanvas() {
            setOpaque(false);
            for (int i = 0; i < barRects.length; i++) {
                barRects[i] = new Rectangle();
            }

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePoint = e.getPoint();
                    int newHover = -1;
                    for (int i = 0; i < barRects.length; i++) {
                        if (barRects[i] != null && barRects[i].contains(mousePoint)) {
                            newHover = i;
                            break;
                        }
                    }
                    if (newHover != hoverIndex) {
                        hoverIndex = newHover;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (days == null || days.length == 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            int width = getWidth();
            int height = getHeight();

            int padLeft = 46;
            int padRight = 14;
            int padTop = 18;
            int padBottom = 34;

            int plotW = width - padLeft - padRight;
            int plotH = height - padTop - padBottom;
            if (plotW <= 0 || plotH <= 0) {
                g2.dispose();
                return;
            }

            // Tìm giá trị max doanh thu
            long maxRev = 1_000_000L;
            int maxIdx = 0;
            for (int i = 0; i < days.length; i++) {
                if (days[i].revenue > maxRev) {
                    maxRev = days[i].revenue;
                    maxIdx = i;
                }
            }
            long maxGrid = ((maxRev / 5_000_000L) + 1) * 5_000_000L;
            if (maxGrid < 1_000_000L) maxGrid = 1_000_000L;

            // 1. Vẽ các đường lưới ngang và nhãn trục Y
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fmY = g2.getFontMetrics();
            int gridSteps = 4;
            for (int i = 0; i <= gridSteps; i++) {
                int y = padTop + plotH - (i * plotH / gridSteps);
                long val = i * maxGrid / gridSteps;

                g2.setColor(ThemeColors.BORDER_LIGHT);
                g2.drawLine(padLeft, y, width - padRight, y);

                String yLabel = (val >= 1_000_000L) ? (val / 1_000_000L) + "Tr" : (val / 1000L) + "k";
                if (val == 0) yLabel = "0";
                g2.setColor(ThemeColors.TEXT_MUTED);
                g2.drawString(yLabel, padLeft - fmY.stringWidth(yLabel) - 6, y + 4);
            }

            // 2. Vẽ 7 Cột doanh thu
            int n = days.length;
            int slotW = plotW / n;
            int barW = Math.min(32, Math.max(18, slotW - 14));

            for (int i = 0; i < n; i++) {
                DayData d = days[i];
                int x = padLeft + i * slotW + (slotW - barW) / 2;
                int barH = (int) ((double) d.revenue / maxGrid * plotH);
                barH = Math.max(d.revenue > 0 ? 4 : 0, Math.min(plotH, barH));
                int y = padTop + plotH - barH;

                barRects[i].setBounds(x - 4, padTop, barW + 8, plotH + padBottom);

                // Màu sắc cột
                Color barColor;
                if (i == hoverIndex) {
                    barColor = ThemeColors.PRIMARY_ACTIVE;
                } else if (i == maxIdx && d.revenue > 0) {
                    barColor = ThemeColors.PRIMARY;
                } else if (i == n - 1) {
                    barColor = new Color(0x38, 0xBD, 0xF8);
                } else {
                    barColor = new Color(0xBA, 0xE6, 0xFD);
                }

                // Vẽ cột bo tròn góc trên
                RoundRectangle2D barShape = new RoundRectangle2D.Double(x, y, barW, barH + 6, 6, 6);
                g2.setColor(barColor);
                g2.setClip(padLeft, padTop, plotW, plotH);
                g2.fill(barShape);
                g2.setClip(null);

                // Nhãn ngày và thứ ở dưới trục X
                g2.setFont(new Font("Segoe UI", i == n - 1 ? Font.BOLD : Font.PLAIN, 11));
                g2.setColor(i == n - 1 ? ThemeColors.PRIMARY_ACTIVE : ThemeColors.TEXT_SECONDARY);
                FontMetrics fmX = g2.getFontMetrics();
                int textX = x + (barW - fmX.stringWidth(d.dayLabel)) / 2;
                g2.drawString(d.dayLabel, textX, height - 16);

                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.setColor(ThemeColors.TEXT_MUTED);
                FontMetrics fmDate = g2.getFontMetrics();
                int dateX = x + (barW - fmDate.stringWidth(d.dateLabel)) / 2;
                g2.drawString(d.dateLabel, dateX, height - 4);
            }

            // 3. Hiển thị Tooltip nổi khi Hover chuột
            if (hoverIndex >= 0 && hoverIndex < n && mousePoint != null) {
                DayData d = days[hoverIndex];
                String line1 = d.dayLabel + " (" + d.dateLabel + ")";
                String line2 = "Doanh thu: " + dfMoney.format(d.revenue);
                String line3 = "Đơn hàng: " + d.orders + " đơn";

                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tipW = Math.max(fm.stringWidth(line1), Math.max(fm.stringWidth(line2), fm.stringWidth(line3))) + 18;
                int tipH = 56;

                int tipX = mousePoint.x + 12;
                int tipY = mousePoint.y - tipH - 8;
                if (tipX + tipW > width - 6) tipX = mousePoint.x - tipW - 8;
                if (tipY < 4) tipY = mousePoint.y + 14;

                // Nền tooltip đổ bóng nhẹ
                g2.setColor(new Color(0x0F, 0x17, 0x2A, 230));
                g2.fillRoundRect(tipX, tipY, tipW, tipH, 8, 8);

                g2.setColor(Color.WHITE);
                g2.drawString(line1, tipX + 9, tipY + 16);
                g2.setColor(new Color(0x38, 0xBD, 0xF8));
                g2.drawString(line2, tipX + 9, tipY + 32);
                g2.setColor(new Color(0xE2, 0xE8, 0xF0));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(line3, tipX + 9, tipY + 48);
            }

            g2.dispose();
        }
    }
}
