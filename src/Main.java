import java.awt.Font;
import java.awt.Insets;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import views.LoginForm;
import views.MainDashboard;

/**
 * Điểm khởi chạy chính của ứng dụng Quản lý Cửa hàng Thú cưng (Pet Shop
 * Management System). - Cài đặt Look and Feel chính thức: FlatLaf Light UI với
 * component bo góc hiện đại.S - Khởi tạo font chữ và thiết lập chống răng cưa
 * (Anti-Aliasing). - Khởi chạy màn hình Đăng nhập (LoginForm) hoặc Màn hình
 * chính (MainDashboard).
 */
public class Main {

	public static void main(String[] args) {
		// 1. Tối ưu hóa hiển thị đồ họa và làm mượt font chữ (Anti-aliasing)
		System.setProperty("awt.useSystemAAFontSettings", "on");
		System.setProperty("swing.aatext", "true");

		// 2. Thiết lập Look and Feel chính thức bằng FlatLaf
		caiDatFlatLaf();

		// 3. Khởi chạy giao diện người dùng trên Event Dispatch Thread (EDT)
		SwingUtilities.invokeLater(() -> {
			// Cho phép tham số khởi động nhanh trực tiếp vào Dashboard nếu cần kiểm thử
			if (args != null && args.length > 0 && "--dashboard".equalsIgnoreCase(args[0])) {
				System.out.println("[Main] Đang khởi chạy trực tiếp MainDashboard (Chế độ Bypass/Dev)...");
				new MainDashboard().setVisible(true);
			} else {
				System.out.println("[Main] Đang khởi chạy màn hình Đăng nhập (LoginForm)...");
				new LoginForm().setVisible(true);
			}
		});
	}

	/**
	 * Cài đặt FlatLaf và cấu hình bo góc, font Segoe UI hiện đại.
	 */
	private static void caiDatFlatLaf() {
		try {
			// Thiết lập Look & Feel phong cách Apple macOS (FlatMacLightLaf)
			com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
			UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

			// 1. Tinh chỉnh phong cách phẳng bo góc mềm mại chuẩn macOS hiện đại
			UIManager.put("Button.arc", 16);
			UIManager.put("Component.arc", 18);
			UIManager.put("ProgressBar.arc", 16);
			UIManager.put("TextComponent.arc", 14);
			UIManager.put("ScrollBar.thumbArc", 999);
			UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

			// 2. Màu sắc chủ đạo (Luxury Emerald & Slate Obsidian)
			UIManager.put("Component.accentColor", utils.ThemeColors.PRIMARY);
			UIManager.put("Component.focusColor", utils.ThemeColors.PRIMARY_HOVER);
			UIManager.put("Component.focusedBorderColor", utils.ThemeColors.PRIMARY);

			// 3. Chuẩn hóa Bảng JTable & Sửa lỗi Hover
			UIManager.put("Table.rowHeight", 36);
			UIManager.put("Table.showHorizontalLines", true);
			UIManager.put("Table.showVerticalLines", false);
			UIManager.put("Table.gridColor", utils.ThemeColors.BORDER_LIGHT);
			UIManager.put("Table.hoverBackground", utils.ThemeColors.PRIMARY_SOFT); // Mint mờ #ECFDF5
			UIManager.put("Table.hoverForeground", utils.ThemeColors.TEXT_PRIMARY); // Slate 900
			UIManager.put("Table.selectionBackground", utils.ThemeColors.PRIMARY_LIGHT); // Mint #D1FAE5
			UIManager.put("Table.selectionForeground", utils.ThemeColors.TEXT_PRIMARY);
			UIManager.put("Table.selectionInactiveBackground", utils.ThemeColors.PRIMARY_SOFT);
			UIManager.put("Table.selectionInactiveForeground", utils.ThemeColors.TEXT_PRIMARY);

			// TableHeader
			UIManager.put("TableHeader.height", 38);
			UIManager.put("TableHeader.background", utils.ThemeColors.PRIMARY_TINT); // Slate 100
			UIManager.put("TableHeader.foreground", utils.ThemeColors.TEXT_PRIMARY);
			UIManager.put("TableHeader.separatorColor", utils.ThemeColors.BORDER);

			// 4. TabbedPane
			UIManager.put("TabbedPane.showTabSeparators", true);
			UIManager.put("TabbedPane.selectedBackground", utils.ThemeColors.SURFACE);
			UIManager.put("TabbedPane.selectedForeground", utils.ThemeColors.TEXT_PRIMARY);
			UIManager.put("TabbedPane.underlineColor", utils.ThemeColors.PRIMARY);
			UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));

			// 5. Nút bấm (Button) - Chuẩn màu trắng trên nền Emerald
			UIManager.put("Button.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);
			UIManager.put("Button.hoverForeground", java.awt.Color.WHITE);
			UIManager.put("Button.default.background", utils.ThemeColors.PRIMARY);
			UIManager.put("Button.default.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);
			UIManager.put("Button.default.pressedBackground", utils.ThemeColors.PRIMARY_ACTIVE);
			UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
			UIManager.put("Button.default.hoverForeground", java.awt.Color.WHITE);

			System.out.println("[Main] Đã áp dụng giao diện chính thức: FlatMacLightLaf (Luxury Emerald Edition).");
		} catch (Throwable t) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				System.out.println("[Main] Fallback System LookAndFeel: " + UIManager.getLookAndFeel().getName());
			} catch (Exception e) {
				System.err.println("[Main] Lỗi cài đặt LookAndFeel: " + e.getMessage());
			}
		}

		// Chuẩn hóa Font chữ mặc định hệ thống sang Segoe UI (chuẩn giao diện Windows
		// hiện đại)
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
