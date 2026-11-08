package utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Hệ thống màu sắc chuẩn (Design System) của Ứng dụng Quản lý Cửa hàng Thú cưng Mio (Nhóm 07).
 * Tông màu chủ đạo: Luxury Emerald & Mint Sage (#059669 / #10B981).
 * Bo góc mượt mà (Curved Modern - 16px ~ 20px) đem lại trải nghiệm cao cấp, sạch sẽ, dịu mắt và thư giãn.
 */
public final class ThemeColors {

    private ThemeColors() {}

    // 1. PRIMARY ACCENT (LUXURY EMERALD & MINT SAGE)
    /** Nút bấm chính / Điểm nhấn chủ đạo (Xanh Ngọc Lục Bảo sang trọng #059669) */
    public static final Color PRIMARY = new Color(0x05, 0x96, 0x69);

    /** Màu hover nút chính / Secondary (Xanh Bạc Hà tươi sáng #10B981) */
    public static final Color PRIMARY_HOVER = new Color(0x10, 0xB9, 0x81);

    /** Màu khi click/active nút (Xanh Rừng trầm đậm #047857) */
    public static final Color PRIMARY_ACTIVE = new Color(0x04, 0x78, 0x57);

    /** Mint pastel nhạt (#D1FAE5) - Điểm nhấn phụ dịu mắt */
    public static final Color PRIMARY_LIGHT = new Color(0xD1, 0xFA, 0xE5);

    /** Sage kem dịu (#E6, 0xF4, 0xEA) - Tone hữu cơ thiên nhiên */
    public static final Color PRIMARY_NUDE = new Color(0xE6, 0xF4, 0xEA);

    /** Nền mint cực mờ (#F0FDF4) - Cho selection bảng, hover menu tinh tế */
    public static final Color PRIMARY_SOFT = new Color(0xF0, 0xFD, 0xF4);

    /** Nền tint mint thanh khiết (#ECFDF5) - Cho header bảng, pill tag */
    public static final Color PRIMARY_TINT = new Color(0xEC, 0xFD, 0xF5);

    // 2. BACKGROUND & SURFACES (MODERN CLEAN RETINA)
    /** Nền ứng dụng chính (Trắng xám Slate siêu êm mắt #F8FAFC - chống mỏi mắt) */
    public static final Color BACKGROUND = new Color(0xF8, 0xFA, 0xFC);

    /** Nền thay thế dịu mắt (Soft Slate #F1F5F9) */
    public static final Color BACKGROUND_ALT = new Color(0xF1, 0xF5, 0xF9);

    /** Nền Card / Khung chứa (Trắng sứ tinh khiết #FFFFFF) */
    public static final Color SURFACE = new Color(0xFF, 0xFF, 0xFF);

    /** Nền Sidebar thanh bên (Trắng ánh ngọc dịu #FAFCFB) */
    public static final Color SIDEBAR_BG = new Color(0xFA, 0xFC, 0xFB);

    /** Nền Top Header & Top Navigation (Trắng sứ #FFFFFF) */
    public static final Color HEADER_BG = new Color(0xFF, 0xFF, 0xFF);

    // 3. VIỀN & PHÂN CÁCH (BORDERS & DIVIDERS)
    /** Viền card mỏng siêu nét tone Slate thanh lịch (#E2E8F0) */
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);

    /** Viền mờ rất nhẹ (#F1F5F9) */
    public static final Color BORDER_LIGHT = new Color(0xF1, 0xF5, 0xF9);

    /** Viền nhấn ngọc lục bảo (#059669) */
    public static final Color BORDER_ACCENT = new Color(0x05, 0x96, 0x69);

    // 4. MÀU CHỮ (TYPOGRAPHY - HIGH CONTRAST SLATE NAVY)
    /** Chữ chính / Tiêu đề / Nút bấm (Đen than Slate sang trọng #0F172A - tương phản WCAG AAA) */
    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A);

    /** Chữ đoạn văn / nhãn mô tả phụ (Xám Slate #475569) */
    public static final Color TEXT_SECONDARY = new Color(0x47, 0x55, 0x69);

    /** Chữ mờ / placeholder / timestamp (#94A3B8) */
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);

    /** Chữ điểm nhấn ngọc lục bảo (#059669) */
    public static final Color TEXT_ACCENT = new Color(0x05, 0x96, 0x69);

    // 5. MÀU TRẠNG THÁI (STATUS COLORS)
    /** Thành công / Hoàn tất (Xanh mint tươi #10B981) */
    public static final Color SUCCESS = new Color(0x10, 0xB9, 0x81);
    public static final Color SUCCESS_BG = new Color(0xEC, 0xFD, 0xF5);
    public static final Color SUCCESS_LIGHT = new Color(0xEC, 0xFD, 0xF5);
    public static final Color SUCCESS_TEXT = new Color(0x06, 0x5F, 0x46);

    /** Nguy hiểm / Cảnh báo lỗi / Đăng xuất (Đỏ hồng thanh lịch #EF4444) */
    public static final Color DANGER = new Color(0xEF, 0x44, 0x44);
    public static final Color DANGER_BG = new Color(0xFE, 0xF2, 0xF2);
    public static final Color DANGER_LIGHT = new Color(0xFE, 0xF2, 0xF2);
    public static final Color DANGER_TEXT = new Color(0x99, 0x1B, 0x1B);

    /** Cảnh báo / Hạn dùng FEFO (Hổ phách ấm #F59E0B) */
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);
    public static final Color WARNING_BG = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_LIGHT = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_TEXT = new Color(0x92, 0x40, 0x0E);

    /** Thông tin / Dịch vụ (Xanh Indigo hiện đại #6366F1) */
    public static final Color INFO = new Color(0x63, 0x66, 0xF1);
    public static final Color INFO_BG = new Color(0xEE, 0xF2, 0xFF);
    public static final Color INFO_TEXT = new Color(0x37, 0x30, 0xA3);

    // Trạng thái Lịch hẹn
    public static final Color APPOINTMENT_WAITING_BG = new Color(0xE0, 0xF2, 0xFE);
    public static final Color APPOINTMENT_WAITING_TEXT = new Color(0x03, 0x69, 0xA1);

    public static final Color APPOINTMENT_PROGRESS_BG = new Color(0xFE, 0xF3, 0xC7);
    public static final Color APPOINTMENT_PROGRESS_TEXT = new Color(0x92, 0x40, 0x0E);
    public static final Color APPOINTMENT_DOING_BG = APPOINTMENT_PROGRESS_BG;
    public static final Color APPOINTMENT_DOING_TEXT = APPOINTMENT_PROGRESS_TEXT;

    public static final Color APPOINTMENT_DONE_BG = new Color(0xEC, 0xFD, 0xF5);
    public static final Color APPOINTMENT_DONE_TEXT = new Color(0x06, 0x5F, 0x46);

    public static final Color APPOINTMENT_CANCEL_BG = new Color(0xFE, 0xF2, 0xF2);
    public static final Color APPOINTMENT_CANCEL_TEXT = new Color(0x99, 0x1B, 0x1B);

    // 6. TIỆN ÍCH TẠO BORDER & COMPONENT STYLING (BO GÓC MỀM MẠI 16PX - 20PX)

    /**
     * Lớp Border bo tròn chống răng cưa (Anti-Aliased Curved Border).
     */
    public static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        public RoundedBorder(Color color, int radius) {
            this(color, radius, 1);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color != null ? color : BORDER);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    /**
     * Tạo viền Card bo góc mềm mại thanh lịch (radius 18px) kèm padding tùy chọn.
     */
    public static Border createCardBorder(int top, int left, int bottom, int right) {
        return new CompoundBorder(
                new RoundedBorder(BORDER, 18, 1),
                new EmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Tạo viền Card bo góc mặc định (radius 18px, padding 14px).
     */
    public static Border createCardBorder() {
        return createCardBorder(14, 14, 14, 14);
    }

    /**
     * Tạo viền Input bo góc mềm mại hiện đại (radius 14px).
     */
    public static Border createInputBorder() {
        return new CompoundBorder(
                new RoundedBorder(BORDER, 14, 1),
                new EmptyBorder(6, 12, 6, 12)
        );
    }

    /**
     * Tạo nút bấm chính tông Luxury Emerald (Primary Button).
     * Chữ trắng tinh #FFFFFF trên nền Emerald #059669, hover Mint #10B981, bo góc 16px.
     */
    public static JButton createPrimaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new RoundedBorder(PRIMARY_ACTIVE, 16, 1),
                new EmptyBorder(8, 18, 8, 18)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
                btn.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY);
                btn.setForeground(Color.WHITE);
            }
        });
        return btn;
    }

    /**
     * Tạo nút bấm phụ tông trắng sứ viền bo góc mềm mại (Secondary Button, radius 16px).
     */
    public static JButton createSecondaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(SURFACE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 16, 1),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_TINT);
                btn.setForeground(TEXT_PRIMARY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(SURFACE);
                btn.setForeground(TEXT_PRIMARY);
            }
        });

        return btn;
    }

    /**
     * Tạo Badge trạng thái bo tròn mềm mại với màu sắc pastel (radius 14px).
     */
    public static JLabel createBadge(String text, Color bgColor, Color textColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(textColor);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        return badge;
    }
}
