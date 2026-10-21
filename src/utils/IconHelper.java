package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Tiện ích hỗ trợ quản lý và kết xuất icon theo phong cách Lucide Icons (https://lucide.dev/).
 * - Đường nét mảnh hiện đại (stroke 1.75 - 2.0).
 * - Tự động khử răng cưa (Anti-Aliasing) với độ sắc nét tuyệt đối ở mọi độ phân giải màn hình.
 * - Hỗ trợ nạp tệp tài nguyên PNG từ thư mục `resources/icons/` và tự động fallback sang
 *   bộ vẽ vector Java2D nội tại chuẩn xác 100%.
 */
public final class IconHelper {

    private static final Map<String, Icon> iconCache = new HashMap<>();

    // Danh sách tên định danh Lucide chuẩn
    public static final String DASHBOARD = "layout-dashboard";
    public static final String HOME = "home";
    public static final String PAW = "paw-print";
    public static final String CART = "shopping-cart";
    public static final String RECEIPT = "receipt";
    public static final String USERS = "users";
    public static final String USER = "user";
    public static final String USER_CHECK = "user-check";
    public static final String BAR_CHART = "bar-chart-3";
    public static final String TRENDING_UP = "trending-up";
    public static final String LOG_IN = "log-in";
    public static final String LOG_OUT = "log-out";
    public static final String SETTINGS = "settings";
    public static final String SEARCH = "search";
    public static final String PLUS = "plus";
    public static final String MINUS = "minus";
    public static final String TRASH = "trash-2";
    public static final String BOXES = "boxes";
    public static final String CALENDAR = "calendar";
    public static final String STETHOSCOPE = "stethoscope";
    public static final String CLIPBOARD_CHECK = "clipboard-check";
    public static final String ALERT_TRIANGLE = "alert-triangle";
    public static final String ALERT_CIRCLE = "alert-circle";
    public static final String CLOCK = "clock";
    public static final String TAG = "tag";
    public static final String CREDIT_CARD = "credit-card";
    public static final String LOCK = "lock";
    public static final String REFRESH = "refresh-cw";
    public static final String SUN = "sun";
    public static final String MOON = "moon";
    public static final String CHECK = "check";
    public static final String PHONE = "phone";
    public static final String SHOPPING_BAG = "shopping-bag";
    public static final String ROLE_NVQL = "crown";
    public static final String ROLE_NVBH = "shopping-bag";
    public static final String ROLE_NVCS = "heart-handshake";
    public static final String ROLE_NVTN = "wallet";
    public static final String SCISSORS = "scissors";
    public static final String SPARKLES = "sparkles";
    public static final String CHEVRON_LEFT = "chevron-left";
    public static final String CHEVRON_RIGHT = "chevron-right";
    public static final String EDIT = "edit";
    public static final String SHIELD = "shield";
    public static final String KEY = "key";
    public static final String FILE_TEXT = "file-text";

    private IconHelper() {}

    /**
     * Lấy Icon chuẩn Lucide với tên, kích thước (pixel) và màu sắc mong muốn.
     */
    public static Icon getIcon(String name, int size, Color color) {
        String key = name + "_" + size + "_" + (color != null ? color.getRGB() : "null");
        if (iconCache.containsKey(key)) {
            return iconCache.get(key);
        }

        // Với icon kích thước lớn (>28px) hoặc khi cần độ nét cao, ưu tiên vẽ Vector trực tiếp
        if (size <= 28) {
            Icon loaded = loadFromResource(name, size, color);
            if (loaded != null) {
                iconCache.put(key, loaded);
                return loaded;
            }
        }

        // Kết xuất Vector Java2D chuẩn Lucide sắc nét 100% trên mọi độ phân giải
        BufferedImage img = renderVectorIcon(name, size, color);
        ImageIcon icon = new ImageIcon(img);
        iconCache.put(key, icon);
        return icon;
    }

    /**
     * Luôn kết xuất trực tiếp bằng vector Java2D (không dùng ảnh bitmap), đảm bảo sắc nét 100%.
     */
    public static Icon getVectorIcon(String name, int size, Color color) {
        BufferedImage img = renderVectorIcon(name, size, color);
        return new ImageIcon(img);
    }

    /**
     * Lấy Icon với màu mặc định là màu chữ chính (Slate 800).
     */
    public static Icon getIcon(String name, int size) {
        return getIcon(name, size, ThemeColors.TEXT_PRIMARY);
    }

    /**
     * Lấy Icon với kích thước mặc định 18x18.
     */
    public static Icon getIcon(String name) {
        return getIcon(name, 18, ThemeColors.TEXT_PRIMARY);
    }

    /**
     * Lấy Icon màu chính (ThemeColors.PRIMARY).
     */
    public static Icon getPrimaryIcon(String name, int size) {
        return getIcon(name, size, ThemeColors.PRIMARY);
    }

    /**
     * Nạp an toàn hình ảnh (Product / Service thumbnail) từ thư mục resources
     * với cơ chế Safe Resource Loading & Vector Fallback.
     * Tuyệt đối không bao giờ trả về null hoặc ném NullPointerException.
     */
    public static ImageIcon loadImage(String relativePath, int width, int height, String fallbackVectorIcon) {
        if (relativePath != null && !relativePath.trim().isEmpty()) {
            try {
                BufferedImage bimg = null;
                // 1. Thử load từ Classpath
                String path = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
                InputStream is = IconHelper.class.getResourceAsStream(path);
                if (is == null && !relativePath.startsWith("/")) {
                    is = IconHelper.class.getResourceAsStream("/resources/" + relativePath);
                }
                if (is != null) {
                    bimg = ImageIO.read(is);
                }

                // 2. Thử load từ File hệ thống trực tiếp (Workspace relative)
                if (bimg == null) {
                    File file = new File(relativePath);
                    if (!file.exists()) {
                        file = new File("resources/" + relativePath);
                    }
                    if (file.exists() && file.isFile()) {
                        bimg = ImageIO.read(file);
                    }
                }

                if (bimg != null) {
                    BufferedImage rounded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = rounded.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    RoundRectangle2D clip = new RoundRectangle2D.Float(0, 0, width, height, 14, 14);
                    g2.setClip(clip);
                    g2.drawImage(bimg, 0, 0, width, height, null);
                    g2.setClip(null);
                    g2.setColor(ThemeColors.BORDER);
                    g2.drawRoundRect(0, 0, width - 1, height - 1, 14, 14);
                    g2.dispose();
                    return new ImageIcon(rounded);
                }
            } catch (Exception ignored) {
                // Fallback xuống vector icon
            }
        }

        // 3. Fallback: Vẽ Icon Vector chuẩn sắc nét trên khung nền bo tròn mềm mại
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeColors.PRIMARY_SOFT);
        g2.fillRoundRect(0, 0, width, height, 14, 14);
        g2.setColor(ThemeColors.BORDER);
        g2.drawRoundRect(0, 0, width - 1, height - 1, 14, 14);

        int iconSize = Math.max(16, Math.min(width, height) * 55 / 100);
        BufferedImage vec = renderVectorIcon(fallbackVectorIcon != null ? fallbackVectorIcon : BOXES, iconSize, ThemeColors.PRIMARY);
        int ox = (width - iconSize) / 2;
        int oy = (height - iconSize) / 2;
        g2.drawImage(vec, ox, oy, null);
        g2.dispose();

        return new ImageIcon(canvas);
    }

    private static Icon loadFromResource(String name, int size, Color color) {
        String[] possiblePaths = {
                "/resources/icons/" + name + ".png",
                "/icons/" + name + ".png",
                "resources/icons/" + name + ".png"
        };

        for (String path : possiblePaths) {
            try {
                InputStream is = IconHelper.class.getResourceAsStream(path);
                if (is != null) {
                    BufferedImage raw = ImageIO.read(is);
                    if (raw != null) {
                        Image scaled = raw.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) {}

            try {
                File f = new File(path);
                if (f.exists() && f.isFile()) {
                    BufferedImage raw = ImageIO.read(f);
                    if (raw != null) {
                        Image scaled = raw.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Kết xuất vector Lucide Icon bằng Java2D Graphics với tọa độ chuẩn 24x24 scale sang size thực tế.
     */
    public static BufferedImage renderVectorIcon(String name, int size, Color color) {
        if (color == null) color = ThemeColors.TEXT_PRIMARY;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // Bật khử răng cưa và bo tròn nét vẽ cao cấp
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Chuẩn hóa hệ tọa độ từ 24x24 của Lucide sang kích thước thực
        double scale = (double) size / 24.0;
        g2.scale(scale, scale);

        g2.setColor(color);
        // Stroke chuẩn Lucide 1.85px, đầu bo tròn (ROUND CAP, ROUND JOIN)
        g2.setStroke(new BasicStroke(1.85f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        drawLucideShape(g2, name);

        g2.dispose();
        return img;
    }

    private static void drawLucideShape(Graphics2D g2, String name) {
        switch (name.toLowerCase()) {
            case "layout-dashboard":
            case "dashboard":
                // 4 ô vuông dashboard
                g2.draw(new RoundRectangle2D.Double(3, 3, 7, 9, 2, 2));
                g2.draw(new RoundRectangle2D.Double(14, 3, 7, 5, 2, 2));
                g2.draw(new RoundRectangle2D.Double(14, 12, 7, 9, 2, 2));
                g2.draw(new RoundRectangle2D.Double(3, 16, 7, 5, 2, 2));
                break;

            case "home":
                Path2D home = new Path2D.Double();
                home.moveTo(3, 10);
                home.lineTo(12, 3);
                home.lineTo(21, 10);
                home.lineTo(21, 20);
                home.curveTo(21, 20.5, 20.5, 21, 20, 21);
                home.lineTo(4, 21);
                home.curveTo(3.5, 21, 3, 20.5, 3, 20);
                home.closePath();
                g2.draw(home);
                // Cửa chính
                g2.draw(new Line2D.Double(9, 21, 9, 13));
                g2.draw(new Line2D.Double(9, 13, 15, 13));
                g2.draw(new Line2D.Double(15, 13, 15, 21));
                break;

            case "paw-print":
            case "paw":
                // Vết chân thú cưng: 4 ngón + 1 đệm chân chính
                g2.fill(new Ellipse2D.Double(5, 5, 3.2, 4.2));
                g2.fill(new Ellipse2D.Double(9.2, 2.5, 3.4, 4.5));
                g2.fill(new Ellipse2D.Double(13.8, 2.5, 3.4, 4.5));
                g2.fill(new Ellipse2D.Double(18, 5, 3.2, 4.2));
                // Đệm bàn chân chính
                Path2D pad = new Path2D.Double();
                pad.moveTo(6.5, 13.5);
                pad.curveTo(6, 17, 9, 21, 12.5, 21);
                pad.curveTo(16, 21, 19, 17, 18.5, 13.5);
                pad.curveTo(16.5, 11.5, 14, 12.5, 12.5, 13.5);
                pad.curveTo(11, 12.5, 8.5, 11.5, 6.5, 13.5);
                pad.closePath();
                g2.fill(pad);
                break;

            case "shopping-cart":
            case "cart":
                // Bánh xe
                g2.fill(new Ellipse2D.Double(8, 20, 2.2, 2.2));
                g2.fill(new Ellipse2D.Double(18, 20, 2.2, 2.2));
                // Giỏ hàng
                Path2D cart = new Path2D.Double();
                cart.moveTo(2, 3);
                cart.lineTo(5, 3);
                cart.lineTo(7.5, 16);
                cart.curveTo(7.6, 17, 8.5, 17.5, 9.5, 17.5);
                cart.lineTo(19.5, 17.5);
                cart.lineTo(21.5, 7);
                cart.lineTo(6, 7);
                g2.draw(cart);
                break;

            case "receipt":
                Path2D receipt = new Path2D.Double();
                receipt.moveTo(4, 3);
                receipt.lineTo(20, 3);
                receipt.lineTo(20, 21);
                receipt.lineTo(17.5, 19.5);
                receipt.lineTo(15, 21);
                receipt.lineTo(12.5, 19.5);
                receipt.lineTo(10, 21);
                receipt.lineTo(7.5, 19.5);
                receipt.lineTo(4, 21);
                receipt.closePath();
                g2.draw(receipt);
                // Các dòng nội dung trên biên lai
                g2.draw(new Line2D.Double(8, 8, 16, 8));
                g2.draw(new Line2D.Double(8, 12, 16, 12));
                g2.draw(new Line2D.Double(8, 16, 12, 16));
                break;

            case "users":
                // 2 người dùng
                g2.draw(new Ellipse2D.Double(7, 4, 6, 6)); // Đầu người trước
                Path2D b1 = new Path2D.Double();
                b1.moveTo(4, 19);
                b1.curveTo(4, 15, 7, 13.5, 10, 13.5);
                b1.curveTo(13, 13.5, 16, 15, 16, 19);
                g2.draw(b1);
                // Người phía sau
                Path2D b2 = new Path2D.Double();
                b2.moveTo(17, 11);
                b2.curveTo(19, 12, 20.5, 14, 20.5, 18);
                g2.draw(b2);
                g2.draw(new Arc2D.Double(14, 3, 5, 5, -80, 200, Arc2D.OPEN));
                break;

            case "user":
                // 1 người dùng
                g2.draw(new Ellipse2D.Double(8, 4, 8, 8));
                Path2D person = new Path2D.Double();
                person.moveTo(4, 20);
                person.curveTo(4, 15.5, 7.5, 14, 12, 14);
                person.curveTo(16.5, 14, 20, 15.5, 20, 20);
                g2.draw(person);
                break;

            case "user-check":
                g2.draw(new Ellipse2D.Double(7, 4, 7, 7));
                Path2D uc = new Path2D.Double();
                uc.moveTo(3, 20);
                uc.curveTo(3, 16, 6.5, 14.5, 10.5, 14.5);
                g2.draw(uc);
                // Checkmark
                Path2D chk = new Path2D.Double();
                chk.moveTo(16, 15);
                chk.lineTo(18, 17.5);
                chk.lineTo(22, 12);
                g2.draw(chk);
                break;

            case "bar-chart-3":
            case "chart":
                g2.draw(new Line2D.Double(3, 20, 21, 20));
                g2.draw(new Line2D.Double(6, 20, 6, 14));
                g2.draw(new Line2D.Double(12, 20, 12, 7));
                g2.draw(new Line2D.Double(18, 20, 18, 10));
                break;

            case "trending-up":
                Path2D trend = new Path2D.Double();
                trend.moveTo(22, 7);
                trend.lineTo(13.5, 15.5);
                trend.lineTo(8.5, 10.5);
                trend.lineTo(2, 17);
                g2.draw(trend);
                // Mũi tên góc phải trên
                g2.draw(new Line2D.Double(16, 7, 22, 7));
                g2.draw(new Line2D.Double(22, 7, 22, 13));
                break;

            case "log-in":
                Path2D loginBox = new Path2D.Double();
                loginBox.moveTo(15, 3);
                loginBox.lineTo(19, 3);
                loginBox.curveTo(20, 3, 21, 4, 21, 5);
                loginBox.lineTo(21, 19);
                loginBox.curveTo(21, 20, 20, 21, 19, 21);
                loginBox.lineTo(15, 21);
                g2.draw(loginBox);
                // Mũi tên đi vào
                g2.draw(new Line2D.Double(3, 12, 14, 12));
                g2.draw(new Line2D.Double(10, 8, 14, 12));
                g2.draw(new Line2D.Double(10, 16, 14, 12));
                break;

            case "log-out":
                Path2D logoutBox = new Path2D.Double();
                logoutBox.moveTo(9, 21);
                logoutBox.lineTo(5, 21);
                logoutBox.curveTo(4, 21, 3, 20, 3, 19);
                logoutBox.lineTo(3, 5);
                logoutBox.curveTo(3, 4, 4, 3, 5, 3);
                logoutBox.lineTo(9, 3);
                g2.draw(logoutBox);
                // Mũi tên đi ra
                g2.draw(new Line2D.Double(16, 17, 21, 12));
                g2.draw(new Line2D.Double(16, 7, 21, 12));
                g2.draw(new Line2D.Double(9, 12, 21, 12));
                break;

            case "settings":
                g2.draw(new Ellipse2D.Double(9, 9, 6, 6));
                Path2D cog = new Path2D.Double();
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4.0;
                    double cos = Math.cos(angle);
                    double sin = Math.sin(angle);
                    double x1 = 12 + cos * 7.5;
                    double y1 = 12 + sin * 7.5;
                    double x2 = 12 + cos * 10.5;
                    double y2 = 12 + sin * 10.5;
                    cog.moveTo(x1, y1);
                    cog.lineTo(x2, y2);
                }
                g2.draw(cog);
                break;

            case "search":
                g2.draw(new Ellipse2D.Double(4, 4, 12, 12));
                g2.draw(new Line2D.Double(14.5, 14.5, 20, 20));
                break;

            case "plus":
                g2.draw(new Line2D.Double(12, 5, 12, 19));
                g2.draw(new Line2D.Double(5, 12, 19, 12));
                break;

            case "minus":
                g2.draw(new Line2D.Double(5, 12, 19, 12));
                break;

            case "trash-2":
            case "trash":
                g2.draw(new Line2D.Double(3, 6, 21, 6));
                g2.draw(new Line2D.Double(8, 6, 8, 4));
                g2.draw(new Line2D.Double(8, 4, 16, 4));
                g2.draw(new Line2D.Double(16, 4, 16, 6));
                Path2D bin = new Path2D.Double();
                bin.moveTo(5, 6);
                bin.lineTo(6.5, 20);
                bin.curveTo(6.6, 20.8, 7.3, 21.5, 8.2, 21.5);
                bin.lineTo(15.8, 21.5);
                bin.curveTo(16.7, 21.5, 17.4, 20.8, 17.5, 20);
                bin.lineTo(19, 6);
                g2.draw(bin);
                g2.draw(new Line2D.Double(10, 10, 10, 17));
                g2.draw(new Line2D.Double(14, 10, 14, 17));
                break;

            case "boxes":
            case "package":
                // Hộp kho hàng
                Path2D box = new Path2D.Double();
                box.moveTo(12, 2.5);
                box.lineTo(21, 7);
                box.lineTo(21, 17);
                box.lineTo(12, 21.5);
                box.lineTo(3, 17);
                box.lineTo(3, 7);
                box.closePath();
                g2.draw(box);
                g2.draw(new Line2D.Double(12, 2.5, 12, 21.5));
                g2.draw(new Line2D.Double(12, 12, 21, 7));
                g2.draw(new Line2D.Double(12, 12, 3, 7));
                break;

            case "calendar":
                g2.draw(new RoundRectangle2D.Double(3, 4, 18, 17, 3, 3));
                g2.draw(new Line2D.Double(3, 9, 21, 9));
                g2.draw(new Line2D.Double(8, 2, 8, 5));
                g2.draw(new Line2D.Double(16, 2, 16, 5));
                g2.draw(new Line2D.Double(8, 13, 8.5, 13));
                g2.draw(new Line2D.Double(12, 13, 12.5, 13));
                g2.draw(new Line2D.Double(16, 13, 16.5, 13));
                g2.draw(new Line2D.Double(8, 17, 8.5, 17));
                g2.draw(new Line2D.Double(12, 17, 12.5, 17));
                break;

            case "stethoscope":
                Path2D steth = new Path2D.Double();
                steth.moveTo(4, 4);
                steth.lineTo(4, 9);
                steth.curveTo(4, 13, 8, 14, 11, 14);
                steth.lineTo(11, 17);
                steth.curveTo(11, 19.5, 13, 21, 15.5, 21);
                steth.curveTo(18, 21, 20, 19.5, 20, 17);
                steth.curveTo(20, 14.5, 18, 13, 15.5, 13);
                steth.lineTo(15.5, 15);
                g2.draw(steth);
                g2.draw(new Line2D.Double(18, 4, 18, 9));
                g2.draw(new Line2D.Double(4, 4, 6, 4));
                g2.draw(new Line2D.Double(16, 4, 18, 4));
                break;

            case "clipboard-check":
                g2.draw(new RoundRectangle2D.Double(4, 4, 16, 17, 3, 3));
                g2.draw(new RoundRectangle2D.Double(8.5, 2, 7, 3.5, 1.5, 1.5));
                Path2D cbCheck = new Path2D.Double();
                cbCheck.moveTo(9, 13);
                cbCheck.lineTo(11, 15.5);
                cbCheck.lineTo(15, 10.5);
                g2.draw(cbCheck);
                break;

            case "alert-triangle":
            case "alert":
            case "alert-circle":
                Path2D tri = new Path2D.Double();
                tri.moveTo(12, 3);
                tri.lineTo(21.5, 19.5);
                tri.curveTo(21.8, 20, 21.5, 21, 20.5, 21);
                tri.lineTo(3.5, 21);
                tri.curveTo(2.5, 21, 2.2, 20, 2.5, 19.5);
                tri.closePath();
                g2.draw(tri);
                g2.draw(new Line2D.Double(12, 9, 12, 13.5));
                g2.fill(new Ellipse2D.Double(11, 16.5, 2, 2));
                break;

            case "clock":
                g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
                g2.draw(new Line2D.Double(12, 7, 12, 12));
                g2.draw(new Line2D.Double(12, 12, 15.5, 13.5));
                break;

            case "tag":
                Path2D tag = new Path2D.Double();
                tag.moveTo(12.5, 3);
                tag.lineTo(20.5, 11);
                tag.lineTo(12, 19.5);
                tag.lineTo(4, 11.5);
                tag.lineTo(4, 3);
                tag.closePath();
                g2.draw(tag);
                g2.fill(new Ellipse2D.Double(6.5, 5.5, 2, 2));
                break;

            case "credit-card":
                g2.draw(new RoundRectangle2D.Double(2, 5, 20, 14, 3, 3));
                g2.draw(new Line2D.Double(2, 10, 22, 10));
                g2.draw(new Line2D.Double(6, 15, 10, 15));
                break;

            case "lock":
                g2.draw(new RoundRectangle2D.Double(4, 10, 16, 11, 3, 3));
                Path2D shackle = new Path2D.Double();
                shackle.moveTo(7, 10);
                shackle.lineTo(7, 6.5);
                shackle.curveTo(7, 4, 9, 2.5, 12, 2.5);
                shackle.curveTo(15, 2.5, 17, 4, 17, 6.5);
                shackle.lineTo(17, 10);
                g2.draw(shackle);
                g2.fill(new Ellipse2D.Double(11, 14, 2, 2));
                g2.draw(new Line2D.Double(12, 15, 12, 17.5));
                break;

            case "refresh-cw":
                g2.draw(new Arc2D.Double(3, 3, 18, 18, 30, 260, Arc2D.OPEN));
                Path2D arrow1 = new Path2D.Double();
                arrow1.moveTo(21, 4);
                arrow1.lineTo(21, 9);
                arrow1.lineTo(16, 9);
                g2.draw(arrow1);
                break;

            case "sun":
                g2.draw(new Ellipse2D.Double(8, 8, 8, 8));
                g2.draw(new Line2D.Double(12, 2, 12, 4.5));
                g2.draw(new Line2D.Double(12, 19.5, 12, 22));
                g2.draw(new Line2D.Double(2, 12, 4.5, 12));
                g2.draw(new Line2D.Double(19.5, 12, 22, 12));
                break;

            case "moon":
                Path2D moon = new Path2D.Double();
                moon.moveTo(21, 12.8);
                moon.curveTo(19.8, 17.8, 15.3, 21.5, 10, 21.5);
                moon.curveTo(4.8, 21.5, 0.5, 17.2, 0.5, 12);
                moon.curveTo(0.5, 6.7, 4.2, 2.2, 9.2, 1);
                moon.curveTo(8.5, 2.5, 8.1, 4.2, 8.1, 6);
                moon.curveTo(8.1, 12, 13.9, 16.9, 20, 16.9);
                moon.curveTo(20.3, 16.9, 20.7, 16.8, 21, 16.8);
                g2.draw(moon);
                break;

            case "check":
                Path2D c = new Path2D.Double();
                c.moveTo(4, 12.5);
                c.lineTo(9.5, 18);
                c.lineTo(20, 6);
                g2.draw(c);
                break;

            case "dollar-sign":
                g2.draw(new Line2D.Double(12, 2, 12, 22));
                Path2D s = new Path2D.Double();
                s.moveTo(17, 6);
                s.curveTo(13, 4, 7, 6, 7, 10);
                s.curveTo(7, 14, 17, 14, 17, 18);
                s.curveTo(17, 22, 11, 24, 7, 22);
                g2.draw(s);
                break;

            case "dog":
                // Tai chó và khuôn mặt
                Path2D dog = new Path2D.Double();
                dog.moveTo(5, 7);
                dog.lineTo(3.5, 12);
                dog.curveTo(3.5, 14, 5, 15, 6.5, 14);
                dog.lineTo(7, 12);
                g2.draw(dog);
                Path2D dogR = new Path2D.Double();
                dogR.moveTo(19, 7);
                dogR.lineTo(20.5, 12);
                dogR.curveTo(20.5, 14, 19, 15, 17.5, 14);
                dogR.lineTo(17, 12);
                g2.draw(dogR);
                g2.draw(new RoundRectangle2D.Double(6, 6, 12, 14, 5, 5));
                g2.fill(new Ellipse2D.Double(8.5, 10, 1.8, 1.8));
                g2.fill(new Ellipse2D.Double(13.7, 10, 1.8, 1.8));
                g2.fill(new Ellipse2D.Double(11, 13.5, 2, 1.5));
                break;

            case "cat":
                Path2D cat = new Path2D.Double();
                cat.moveTo(4, 9);
                cat.lineTo(6, 4);
                cat.lineTo(9.5, 7);
                cat.lineTo(14.5, 7);
                cat.lineTo(18, 4);
                cat.lineTo(20, 9);
                cat.curveTo(20.5, 15, 17, 20, 12, 20);
                cat.curveTo(7, 20, 3.5, 15, 4, 9);
                cat.closePath();
                g2.draw(cat);
                g2.fill(new Ellipse2D.Double(8, 11, 1.8, 1.8));
                g2.fill(new Ellipse2D.Double(14.2, 11, 1.8, 1.8));
                g2.draw(new Line2D.Double(12, 13.5, 12, 15));
                break;

            case "x":
                g2.draw(new Line2D.Double(5, 5, 19, 19));
                g2.draw(new Line2D.Double(19, 5, 5, 19));
                break;

            case "crown":
                Path2D crown = new Path2D.Double();
                crown.moveTo(3, 18);
                crown.lineTo(5, 8);
                crown.lineTo(9.5, 13);
                crown.lineTo(12, 6);
                crown.lineTo(14.5, 13);
                crown.lineTo(19, 8);
                crown.lineTo(21, 18);
                crown.closePath();
                g2.draw(crown);
                g2.draw(new Line2D.Double(3, 18, 21, 18));
                break;

            case "shopping-bag":
                Path2D bag = new Path2D.Double();
                bag.moveTo(6, 7);
                bag.lineTo(4, 20);
                bag.curveTo(4, 20.5, 4.5, 21, 5, 21);
                bag.lineTo(19, 21);
                bag.curveTo(19.5, 21, 20, 20.5, 20, 20);
                bag.lineTo(18, 7);
                bag.closePath();
                g2.draw(bag);
                // Quai xách
                Path2D handle = new Path2D.Double();
                handle.moveTo(9, 7);
                handle.curveTo(9, 3.5, 15, 3.5, 15, 7);
                g2.draw(handle);
                break;

            case "heart-handshake":
                // Vẽ trái tim chăm sóc
                Path2D heart = new Path2D.Double();
                heart.moveTo(12, 20);
                heart.curveTo(6, 14, 4, 11, 4, 8);
                heart.curveTo(4, 5, 6.5, 4, 9, 4);
                heart.curveTo(10.5, 4, 11.5, 5, 12, 6);
                heart.curveTo(12.5, 5, 13.5, 4, 15, 4);
                heart.curveTo(17.5, 4, 20, 5, 20, 8);
                heart.curveTo(20, 11, 18, 14, 12, 20);
                heart.closePath();
                g2.draw(heart);
                break;

            case "wallet":
                g2.draw(new RoundRectangle2D.Double(3, 6, 18, 13, 4, 4));
                g2.draw(new Line2D.Double(3, 10, 21, 10));
                g2.draw(new RoundRectangle2D.Double(14, 12, 7, 5, 2, 2));
                g2.fill(new Ellipse2D.Double(16, 13.5, 2, 2));
                break;

            case "scissors":
                // 2 lỗ kéo
                g2.draw(new Ellipse2D.Double(4, 5, 5, 5));
                g2.draw(new Ellipse2D.Double(4, 14, 5, 5));
                // 2 lưỡi kéo bắt chéo
                g2.draw(new Line2D.Double(8, 7.5, 20, 18));
                g2.draw(new Line2D.Double(8, 16.5, 20, 6));
                break;

            case "sparkles":
                // Ngôi sao lấp lánh trung tâm
                Path2D star = new Path2D.Double();
                star.moveTo(12, 3);
                star.curveTo(12, 7.5, 13.5, 9, 18, 9);
                star.curveTo(13.5, 9, 12, 10.5, 12, 15);
                star.curveTo(12, 10.5, 10.5, 9, 6, 9);
                star.curveTo(10.5, 9, 12, 7.5, 12, 3);
                star.closePath();
                g2.draw(star);
                // Điểm lấp lánh phụ
                g2.fill(new Ellipse2D.Double(18, 16, 2, 2));
                g2.fill(new Ellipse2D.Double(6, 17, 2, 2));
                break;

            case "phone":
                g2.draw(new RoundRectangle2D.Double(6, 3, 12, 18, 4, 4));
                g2.draw(new Line2D.Double(10, 6, 14, 6));
                g2.fill(new Ellipse2D.Double(11, 17, 2, 2));
                break;

            case "chevron-left":
                Path2D cl = new Path2D.Double();
                cl.moveTo(15, 18);
                cl.lineTo(9, 12);
                cl.lineTo(15, 6);
                g2.draw(cl);
                break;

            case "chevron-right":
                Path2D cr = new Path2D.Double();
                cr.moveTo(9, 18);
                cr.lineTo(15, 12);
                cr.lineTo(9, 6);
                g2.draw(cr);
                break;

            case "edit":
            case "pencil":
                Path2D pen = new Path2D.Double();
                pen.moveTo(17, 3);
                pen.lineTo(21, 7);
                pen.lineTo(7, 21);
                pen.lineTo(3, 21);
                pen.lineTo(3, 17);
                pen.closePath();
                g2.draw(pen);
                g2.draw(new Line2D.Double(14, 6, 18, 10));
                break;

            case "shield":
                Path2D shield = new Path2D.Double();
                shield.moveTo(12, 22);
                shield.curveTo(7, 20, 4, 15, 4, 9);
                shield.lineTo(4, 4);
                shield.lineTo(12, 2);
                shield.lineTo(20, 4);
                shield.lineTo(20, 9);
                shield.curveTo(20, 15, 17, 20, 12, 22);
                shield.closePath();
                g2.draw(shield);
                break;

            case "key":
                g2.draw(new Ellipse2D.Double(3, 7, 10, 10));
                g2.draw(new Line2D.Double(13, 12, 21, 12));
                g2.draw(new Line2D.Double(18, 12, 18, 16));
                g2.draw(new Line2D.Double(21, 12, 21, 15));
                break;

            case "file-text":
                g2.draw(new RoundRectangle2D.Double(4, 2, 16, 20, 2, 2));
                g2.draw(new Line2D.Double(8, 7, 16, 7));
                g2.draw(new Line2D.Double(8, 12, 16, 12));
                g2.draw(new Line2D.Double(8, 17, 13, 17));
                break;

            default:
                // Mặc định vẽ chấm tròn thanh lịch nếu chưa có icon
                g2.draw(new Ellipse2D.Double(6, 6, 12, 12));
                g2.draw(new Line2D.Double(12, 9, 12, 15));
                break;
        }
    }

    /**
     * Phương thức tiện ích xuất hàng loạt các icon Lucide ra thư mục resources/icons/ dưới dạng PNG.
     */
    public static void main(String[] args) {
        String[] icons = {
            "layout-dashboard", "home", "paw-print", "dog", "cat", "shopping-cart", "receipt",
            "users", "user", "user-check", "bar-chart-3", "trending-up", "log-in", "log-out",
            "settings", "search", "plus", "minus", "trash-2", "boxes", "package", "calendar",
            "stethoscope", "clipboard-check", "alert-triangle", "clock", "tag", "credit-card",
            "lock", "refresh-cw", "sun", "moon", "check", "dollar-sign", "x"
        };
        File dir = new File("resources/icons");
        if (!dir.exists()) dir.mkdirs();

        int count = 0;
        for (String name : icons) {
            try {
                BufferedImage img = renderVectorIcon(name, 24, new Color(30, 41, 59));
                ImageIO.write(img, "PNG", new File(dir, name + ".png"));
                count++;
            } catch (Exception e) {
                System.err.println("Lỗi xuất icon " + name + ": " + e.getMessage());
            }
        }
        System.out.println("Đã xuất thành công " + count + " Lucide Icons ra thư mục resources/icons/!");
    }
}
