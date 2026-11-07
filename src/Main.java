import java.awt.Font;
import java.awt.Insets;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import gui.LoginForm;
import gui.MainDashboard;

public class Main {

    public static void main(String[] args) {
        // bật khử răng cưa text cho đỡ vỡ hạt font chữ
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // cài theme giao diện
        caiDatFlatLaf();

        // chạy giao diện trên luồng swing cho mượt
        SwingUtilities.invokeLater(() -> {
            // nếu truyền cờ --dashboard thì mở thẳng vô xem thử giao diện luôn
            if (args != null && args.length > 0 && "--dashboard".equalsIgnoreCase(args[0])) {
                new MainDashboard().setVisible(true);
            } else {
                // bth thì mở login
                new LoginForm().setVisible(true);
            }
        });
    }

    private static void caiDatFlatLaf() {
        try {
            // xài theme mac cho bo góc đẹp xíu
            com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

            // chỉnh bo góc mấy cái nút với ô nhập
            UIManager.put("Button.arc", 16);
            UIManager.put("Component.arc", 18);
            UIManager.put("ProgressBar.arc", 16);
            UIManager.put("TextComponent.arc", 14);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

            // màu xanh chủ đạo
            UIManager.put("Component.accentColor", utils.ThemeColors.PRIMARY);
            UIManager.put("Component.focusColor", utils.ThemeColors.PRIMARY_HOVER);
            UIManager.put("Component.focusedBorderColor", utils.ThemeColors.PRIMARY);

            // chỉnh lại mấy dòng trong bảng jtable cho thoáng, cao 36 dễ bấm
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.gridColor", utils.ThemeColors.BORDER_LIGHT);
            UIManager.put("Table.hoverBackground", utils.ThemeColors.PRIMARY_SOFT);
            UIManager.put("Table.hoverForeground", utils.ThemeColors.TEXT_PRIMARY);
            UIManager.put("Table.selectionBackground", utils.ThemeColors.PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", utils.ThemeColors.TEXT_PRIMARY);
            UIManager.put("Table.selectionInactiveBackground", utils.ThemeColors.PRIMARY_SOFT);
            UIManager.put("Table.selectionInactiveForeground", utils.ThemeColors.TEXT_PRIMARY);

            // tiêu đề bảng
            UIManager.put("TableHeader.height", 38);
            UIManager.put("TableHeader.background", utils.ThemeColors.PRIMARY_TINT);
            UIManager.put("TableHeader.foreground", utils.ThemeColors.TEXT_PRIMARY);
            UIManager.put("TableHeader.separatorColor", utils.ThemeColors.BORDER);

            // tab
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.selectedBackground", utils.ThemeColors.SURFACE);
            UIManager.put("TabbedPane.selectedForeground", utils.ThemeColors.TEXT_PRIMARY);
            UIManager.put("TabbedPane.underlineColor", utils.ThemeColors.PRIMARY);
            UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));

            // nút bấm
            UIManager.put("Button.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);
            UIManager.put("Button.hoverForeground", java.awt.Color.WHITE);
            UIManager.put("Button.default.background", utils.ThemeColors.PRIMARY);
            UIManager.put("Button.default.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);
            UIManager.put("Button.default.pressedBackground", utils.ThemeColors.PRIMARY_ACTIVE);
            UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.default.hoverForeground", java.awt.Color.WHITE);

            // Bật nút con mắt xem mật khẩu (FlatLaf reveal button) cho toàn bộ hệ thống
            UIManager.put("PasswordField.showRevealButton", true);

        } catch (Throwable t) {
            // lỡ máy ko có thư viện flatlaf thì dùng tạm giao diện mặc định của máy
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Lỗi load theme: " + e.getMessage());
            }
        }

        // set font segoe ui cho đồng bộ
        Font segoeFont = new Font("Segoe UI", Font.PLAIN, 13);
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, segoeFont);
            }
        }
    }
}
