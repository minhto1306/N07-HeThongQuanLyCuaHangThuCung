package utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Hệ thống màu sắc chuẩn (Design System) của Ứng dụng Quản lý Cửa hàng Thú cưng (Nhóm 07).
 * Tông màu chủ đạo: Cam nhạt / Nude ấm (Peach / Warm Nude / Soft Apricot).
 * Mang lại cảm giác ấm áp, thân thiện, cao cấp và chuyên nghiệp cho khách hàng và nhân viên.
 */
public final class ThemeColors {

    private ThemeColors() {}

    // ==========================================
    // 1. PRIMARY ACCENT (HAPPY HUES 15: #ff8ba7)
    // ==========================================
    /** Nút bấm chính / Điểm nhấn chủ đạo (Hồng san hô #FF8BA7) */
    public static final Color PRIMARY = new Color(0xFF, 0x8B, 0xA7);

    /** Màu hover nút chính / Secondary (Hồng phấn nhẹ nhàng #FFC6C7) */
    public static final Color PRIMARY_HOVER = new Color(0xFF, 0xC6, 0xC7);

    /** Màu khi click/active nút (Hồng san hô sẫm #E07A93) */
    public static final Color PRIMARY_ACTIVE = new Color(0xE0, 0x7A, 0x93);

    /** Hồng pastel nhạt (#FFC6C7) - Điểm nhấn phụ */
    public static final Color PRIMARY_LIGHT = new Color(0xFF, 0xC6, 0xC7);

    /** Nude hồng ấm (#F8D7DA) */
    public static final Color PRIMARY_NUDE = new Color(0xF8, 0xD7, 0xDA);

    /** Nền hồng phấn rất mờ (#FDEDEF) - Cho selection bảng, active menu */
    public static final Color PRIMARY_SOFT = new Color(0xFD, 0xED, 0xEF);

    /** Nền tint kem ấm (#FAEEE7) - Trùng tông nền chính */
    public static final Color PRIMARY_TINT = new Color(0xFA, 0xEE, 0xE7);

    // ==========================================
    // 2. BACKGROUND & SURFACES (HAPPY HUES 15)
    // ==========================================
    /** Nền ứng dụng chính (Kem ấm #FAEEE7) */
    public static final Color BACKGROUND = new Color(0xFA, 0xEE, 0xE7);

    /** Nền thay thế dịu mắt (Kem sáng #FFF5F0) */
    public static final Color BACKGROUND_ALT = new Color(0xFF, 0xF5, 0xF0);

    /** Nền Card / Khung chứa (Trắng tinh khiết #FFFFFE) */
    public static final Color SURFACE = new Color(0xFF, 0xFF, 0xFE);

    /** Nền Sidebar thanh bên (Trắng sáng #FFFFFE) */
    public static final Color SIDEBAR_BG = new Color(0xFF, 0xFF, 0xFE);

    /** Nền Top Header & Top Navigation (Trắng sáng #FFFFFE) */
    public static final Color HEADER_BG = new Color(0xFF, 0xFF, 0xFE);

    // ==========================================
    // 3. VIỀN & PHÂN CÁCH (BORDERS & DIVIDERS)
    // ==========================================
    /** Viền card mỏng ấm áp (#EDDCD2) */
    public static final Color BORDER = new Color(0xED, 0xDC, 0xD2);

    /** Viền mờ rất nhẹ (#F4EAE4) */
    public static final Color BORDER_LIGHT = new Color(0xF4, 0xEA, 0xE4);

    /** Viền nhấn hồng phấn (#FFC6C7) */
    public static final Color BORDER_ACCENT = new Color(0xFF, 0xC6, 0xC7);

    // ==========================================
    // 4. MÀU CHỮ (TYPOGRAPHY - HAPPY HUES 15)
    // ==========================================
    /** Chữ chính / Tiêu đề / Nút bấm (Nâu espresso sẫm đậm #33272A) */
    public static final Color TEXT_PRIMARY = new Color(0x33, 0x27, 0x2A);

    /** Chữ đoạn văn / nhãn mô tả phụ (Nâu đất trầm #594A4E) */
    public static final Color TEXT_SECONDARY = new Color(0x59, 0x4A, 0x4E);

    /** Chữ mờ / placeholder / timestamp (#8C7B80) */
    public static final Color TEXT_MUTED = new Color(0x8C, 0x7B, 0x80);

    /** Chữ điểm nhấn (#33272A) */
    public static final Color TEXT_ACCENT = new Color(0x33, 0x27, 0x2A);

    // ==========================================
    // 5. MÀU TRẠNG THÁI (STATUS COLORS)
    // ==========================================
    /** Thành công / Hoàn tất (Xanh mint pastel #C3F0CA) */
    public static final Color SUCCESS = new Color(0xC3, 0xF0, 0xCA);
    public static final Color SUCCESS_BG = new Color(0xED, 0xFA, 0xEE);
    public static final Color SUCCESS_LIGHT = new Color(0xED, 0xFA, 0xEE);
    public static final Color SUCCESS_TEXT = new Color(0x23, 0x4E, 0x27);

    /** Nguy hiểm / Cảnh báo lỗi / Đăng xuất (Đỏ hồng #E05370) */
    public static final Color DANGER = new Color(0xE0, 0x53, 0x70);
    public static final Color DANGER_BG = new Color(0xFF, 0xEE, 0xF0);
    public static final Color DANGER_LIGHT = new Color(0xFF, 0xEE, 0xF0);
    public static final Color DANGER_TEXT = new Color(0x9E, 0x22, 0x38);

    /** Cảnh báo / Hạn dùng FEFO (Hổ phách ấm #F59E0B) */
    public static final Color WARNING = new Color(0xF5, 0x9E, 0x0B);
    public static final Color WARNING_BG = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_LIGHT = new Color(0xFF, 0xFB, 0xEB);
    public static final Color WARNING_TEXT = new Color(0x92, 0x40, 0x0E);

    /** Thông tin / Dịch vụ (Tím pastel dịu #8B5CF6) */
    public static final Color INFO = new Color(0x8B, 0x5C, 0xF6);
    public static final Color INFO_BG = new Color(0xF5, 0xF3, 0xFF);
    public static final Color INFO_TEXT = new Color(0x5B, 0x21, 0xB6);

    // Trạng thái Lịch hẹn
    public static final Color APPOINTMENT_WAITING_BG = new Color(0xDB, 0xEA, 0xFE);
    public static final Color APPOINTMENT_WAITING_TEXT = new Color(0x1E, 0x40, 0xAF);

    public static final Color APPOINTMENT_PROGRESS_BG = new Color(0xFD, 0xED, 0xEF);
    public static final Color APPOINTMENT_PROGRESS_TEXT = new Color(0x33, 0x27, 0x2A);
    public static final Color APPOINTMENT_DOING_BG = APPOINTMENT_PROGRESS_BG;
    public static final Color APPOINTMENT_DOING_TEXT = APPOINTMENT_PROGRESS_TEXT;

    public static final Color APPOINTMENT_DONE_BG = new Color(0xED, 0xFA, 0xEE);
    public static final Color APPOINTMENT_DONE_TEXT = new Color(0x23, 0x4E, 0x27);

    public static final Color APPOINTMENT_CANCEL_BG = new Color(0xFF, 0xEE, 0xF0);
    public static final Color APPOINTMENT_CANCEL_TEXT = new Color(0x9E, 0x22, 0x38);

    // ==========================================
    // 6. TIỆN ÍCH TẠO BORDER & COMPONENT STYLING
    // ==========================================

    /**
     * Tạo viền Card bo góc thanh lịch kèm padding chuẩn (bo góc 12px).
     */
    public static Border createCardBorder(int top, int left, int bottom, int right) {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Tạo viền Card bo góc mặc định (padding 14px).
     */
    public static Border createCardBorder() {
        return createCardBorder(14, 14, 14, 14);
    }

    /**
     * Tạo viền Input bo góc thanh lịch (bo góc 10px).
     */
    public static Border createInputBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        );
    }

    /**
     * Tạo nút bấm chính tông hồng san hô Happy Hues 15 (Primary Button).
     * Chữ nút bấm luôn là nâu espresso đậm #33272A, hover sang hồng phấn #FFC6C7.
     */
    public static JButton createPrimaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(PRIMARY);
        btn.setForeground(TEXT_PRIMARY); // Chữ sẫm #33272A theo Happy Hues 15
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_HOVER, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        // Đảm bảo tương phản tuyệt đối khi hover chuột
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER); // Hồng phấn #FFC6C7
                btn.setForeground(TEXT_PRIMARY); // Chữ sẫm #33272A - không bao giờ bị màu trắng!
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY); // Hồng san hô #FF8BA7
                btn.setForeground(TEXT_PRIMARY);
            }
        });
        return btn;
    }

    /**
     * Tạo nút bấm phụ tông kem ấm viền mỏng (Secondary Button).
     * Đảm bảo màu chữ khi hover luôn là màu sẫm đậm tương phản cao #33272A, không bao giờ bị màu trắng.
     */
    public static JButton createSecondaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(SURFACE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.putClientProperty("JButton.buttonType", "roundRect");

        // Đảm bảo tương phản tuyệt đối khi hover chuột
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER); // Hồng phấn #FFC6C7
                btn.setForeground(TEXT_PRIMARY); // Chữ sẫm #33272A
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
     * Tạo Badge trạng thái bo tròn với màu sắc pastel.
     */
    public static JLabel createBadge(String text, Color bgColor, Color textColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(textColor);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }
}
