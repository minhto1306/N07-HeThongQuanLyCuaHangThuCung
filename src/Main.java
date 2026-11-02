import java.awt.Font;
import java.awt.Insets;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import views.LoginForm;
import views.MainDashboard;

/**
 * =============================================================================
 * LỚP KHỞI CHẠY CHÍNH CỦA HỆ THỐNG (APPLICATION ENTRY POINT)
 * Dự án: Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (Pet Shop Management System)
 * Đơn vị: Khoa CNTT - Đại học Công nghiệp TP.HCM (IUH) - Nhóm 07
 * =============================================================================
 * Nhiệm vụ cốt lõi:
 * 1. Kích hoạt khử răng cưa đồ họa và làm mượt phông chữ (Anti-Aliasing).
 * 2. Thiết lập Look and Feel hiện đại: FlatMacLightLaf (phong cách phẳng macOS).
 * 3. Chuẩn hóa hệ thống Design Token (Màu Luxury Emerald, Bo góc, Bảng JTable).
 * 4. Khởi chạy giao diện an toàn đa luồng trên Event Dispatch Thread (EDT).
 */
public class Main {

    /**
     * Hàm main: Điểm bắt đầu thực thi của toàn bộ chương trình Java
     * @param args Tham số dòng lệnh (Command-line arguments)
     */
    public static void main(String[] args) {
        // ---------------------------------------------------------------------
        // BƯỚC 1: TỐI ƯU HÓA HIỂN THỊ ĐỒ HỌA & LÀM MƯỢT FONT CHỮ TRÊN MÀN HÌNH
        // ---------------------------------------------------------------------
        // Kích hoạt công nghệ làm mượt font sub-pixel (LCD/ClearType) của hệ điều hành
        System.setProperty("awt.useSystemAAFontSettings", "on");
        // Bật tính năng khử răng cưa text cho toàn bộ các thành phần Java Swing
        System.setProperty("swing.aatext", "true");

        // ---------------------------------------------------------------------
        // BƯỚC 2: CÀI ĐẶT LOOK AND FEEL FLATLAF VÀ DESIGN SYSTEM
        // ---------------------------------------------------------------------
        caiDatFlatLaf();

        // ---------------------------------------------------------------------
        // BƯỚC 3: KHỞI TẠO VÀ HIỂN THỊ GIAO DIỆN TRÊN EVENT DISPATCH THREAD (EDT)
        // ---------------------------------------------------------------------
        // Quy tắc vàng của Swing: Mọi thao tác tạo và cập nhật UI bắt buộc phải chạy
        // trên luồng giao diện EDT thông qua invokeLater để tránh Deadlock và Race Condition.
        SwingUtilities.invokeLater(() -> {
            // Kiểm tra tham số dòng lệnh khởi động nhanh (Bypass mode) dành cho Developer / Tester
            if (args != null && args.length > 0 && "--dashboard".equalsIgnoreCase(args[0])) {
                System.out.println("[Main] Khởi chạy trực tiếp MainDashboard (Chế độ bypass đăng nhập để kiểm thử)...");
                new MainDashboard().setVisible(true);
            } else {
                // Mặc định: Luôn mở màn hình Đăng nhập (LoginForm) để kiểm soát phiên và xác thực người dùng
                System.out.println("[Main] Đang khởi chạy màn hình Đăng nhập (LoginForm)...");
                new LoginForm().setVisible(true);
            }
        });
    }

    /**
     * Cài đặt bộ giao diện phẳng FlatLaf, cấu hình tỷ lệ bo góc, bảng biểu và đồng bộ font Segoe UI.
     */
    private static void caiDatFlatLaf() {
        try {
            // Áp dụng chủ đề FlatMacLightLaf: Phong cách thiết kế giao diện Apple macOS hiện đại, phẳng và sạch sẽ
            com.formdev.flatlaf.themes.FlatMacLightLaf.setup();

            // Thiết lập font chữ Segoe UI 13pt mặc định cho toàn bộ ứng dụng
            UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 13));

            // -----------------------------------------------------------------
            // 1. CẤU HÌNH BÁN KÍNH BO GÓC (CORNER RADIUS) MỀM MẠI
            // -----------------------------------------------------------------
            UIManager.put("Button.arc", 16);             // Bo góc 16px cho các nút bấm (JButton)
            UIManager.put("Component.arc", 18);          // Bo góc 18px cho các khung chứa tổng thể
            UIManager.put("ProgressBar.arc", 16);        // Bo góc 16px cho thanh tiến độ
            UIManager.put("TextComponent.arc", 14);       // Bo góc 14px cho ô nhập liệu (JTextField, JPasswordField)
            UIManager.put("ScrollBar.thumbArc", 999);    // Bo tròn hình viên thuốc cho thanh cuộn (JScrollBar)
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2)); // Khoảng đệm thanh cuộn

            // -----------------------------------------------------------------
            // 2. MÀU SẮC ĐIỂM NHẤN (ACCENT COLORS) - TÔNG MÀU LUXURY EMERALD
            // -----------------------------------------------------------------
            UIManager.put("Component.accentColor", utils.ThemeColors.PRIMARY);         // Màu chủ đạo Emerald #059669
            UIManager.put("Component.focusColor", utils.ThemeColors.PRIMARY_HOVER);     // Màu viền khi được chọn (Focus)
            UIManager.put("Component.focusedBorderColor", utils.ThemeColors.PRIMARY);  // Viền nét cao khi focus

            // -----------------------------------------------------------------
            // 3. CHUẨN HÓA BẢNG DỮ LIỆU JTABLE & HIỆU ỨNG RÊ CHUỘT (HOVER)
            // -----------------------------------------------------------------
            UIManager.put("Table.rowHeight", 36);                                     // Chiều cao dòng 36px giúp bấm chuột dễ dàng
            UIManager.put("Table.showHorizontalLines", true);                          // Hiển thị đường kẻ ngang mỏng
            UIManager.put("Table.showVerticalLines", false);                           // Ẩn đường kẻ dọc để giao diện thanh thoát
            UIManager.put("Table.gridColor", utils.ThemeColors.BORDER_LIGHT);          // Màu đường kẻ bảng xám nhẹ
            UIManager.put("Table.hoverBackground", utils.ThemeColors.PRIMARY_SOFT);    // Màu nền khi rê chuột: Mint dịu mắt #F0FDF4
            UIManager.put("Table.hoverForeground", utils.ThemeColors.TEXT_PRIMARY);    // Màu chữ khi rê chuột: Đen than #0F172A
            UIManager.put("Table.selectionBackground", utils.ThemeColors.PRIMARY_LIGHT); // Màu nền khi click chọn dòng: Mint pastel #D1FAE5
            UIManager.put("Table.selectionForeground", utils.ThemeColors.TEXT_PRIMARY); // Màu chữ dòng được chọn
            UIManager.put("Table.selectionInactiveBackground", utils.ThemeColors.PRIMARY_SOFT);
            UIManager.put("Table.selectionInactiveForeground", utils.ThemeColors.TEXT_PRIMARY);

            // Cấu hình tiêu đề bảng (TableHeader)
            UIManager.put("TableHeader.height", 38);                                   // Chiều cao tiêu đề 38px
            UIManager.put("TableHeader.background", utils.ThemeColors.PRIMARY_TINT);   // Nền tiêu đề Slate Mint #ECFDF5
            UIManager.put("TableHeader.foreground", utils.ThemeColors.TEXT_PRIMARY);   // Chữ tiêu đề đậm nét
            UIManager.put("TableHeader.separatorColor", utils.ThemeColors.BORDER);     // Đường phân cách giữa các cột

            // -----------------------------------------------------------------
            // 4. CẤU HÌNH THẺ ĐIỀU HƯỚNG TABBEDPANE
            // -----------------------------------------------------------------
            UIManager.put("TabbedPane.showTabSeparators", true);                       // Hiển thị vạch phân tách tab
            UIManager.put("TabbedPane.selectedBackground", utils.ThemeColors.SURFACE); // Nền tab đang chọn: Trắng sứ #FFFFFF
            UIManager.put("TabbedPane.selectedForeground", utils.ThemeColors.TEXT_PRIMARY); // Chữ tab đang chọn
            UIManager.put("TabbedPane.underlineColor", utils.ThemeColors.PRIMARY);     // Đường gạch chân tab màu Emerald
            UIManager.put("TabbedPane.tabInsets", new Insets(8, 16, 8, 16));           // Khoảng đệm trong tab

            // -----------------------------------------------------------------
            // 5. CẤU HÌNH NÚT BẤM (BUTTON) MẶC ĐỊNH
            // -----------------------------------------------------------------
            UIManager.put("Button.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);   // Nền nút khi rê chuột
            UIManager.put("Button.hoverForeground", java.awt.Color.WHITE);             // Chữ nút màu trắng tinh
            UIManager.put("Button.default.background", utils.ThemeColors.PRIMARY);     // Nút Enter/Mặc định: Xanh Emerald
            UIManager.put("Button.default.hoverBackground", utils.ThemeColors.PRIMARY_HOVER);
            UIManager.put("Button.default.pressedBackground", utils.ThemeColors.PRIMARY_ACTIVE);
            UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
            UIManager.put("Button.default.hoverForeground", java.awt.Color.WHITE);

            System.out.println("[Main] Đã áp dụng giao diện chính thức: FlatMacLightLaf (Luxury Emerald Edition).");
        } catch (Throwable t) {
            // Cơ chế dự phòng an toàn (Fallback): Nếu thư viện FlatLaf không nạp được, chuyển về LookAndFeel của hệ điều hành
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                System.out.println("[Main] Đã kích hoạt Fallback System LookAndFeel: " + UIManager.getLookAndFeel().getName());
            } catch (Exception e) {
                System.err.println("[Main] Lỗi nghiêm trọng khi cài đặt LookAndFeel: " + e.getMessage());
            }
        }

        // ---------------------------------------------------------------------
        // BƯỚC 4: ĐỒNG BỘ FONT SEGOE UI 13PT XUYÊN SUỐT TOÀN BỘ CÁC WIDGET SWING
        // ---------------------------------------------------------------------
        // Quét toàn bộ bảng mã tài nguyên của UIManager để ghi đè font chữ đồng nhất cho mọi thành phần
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
