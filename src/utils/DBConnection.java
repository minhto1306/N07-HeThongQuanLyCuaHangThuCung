package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * =============================================================================
 * LỚP QUẢN LÝ KẾT NỐI CƠ SỞ DỮ LIỆU MICROSOFT SQL SERVER (JDBC CONNECTION FACTORY)
 * Dự án: Hệ Thống Quản Lý Cửa Hàng Thú Cưng Mio (Pet Shop Management System)
 * Đơn vị: Khoa CNTT - Đại học Công nghiệp TP.HCM (IUH) - Nhóm 07
 * =============================================================================
 * Đặc điểm kỹ thuật và kiến trúc:
 * 1. Đọc động tập tin cấu hình bên ngoài: `config/database.properties`.
 * 2. Cơ chế kết nối kép thông minh (Dual-strategy Fallback):
 *    - Chiến lược A: Thử kết nối TCP/IP chuẩn qua Port 1433.
 *    - Chiến lược B: Tự động chuyển đổi sang Named Instance SQLEXPRESS nếu Port 1433 bị đóng.
 * 3. Hỗ trợ độc lập phiên giao dịch JDBC ACID: Mỗi lần gọi getNewConnection() sẽ trả về một
 *    Connection riêng biệt, cho phép quản lý đóng mở an toàn qua cú pháp try-with-resources.
 * 4. Tích hợp cờ bảo mật SSL/TLS: encrypt=true, trustServerCertificate=true chống lỗi chứng chỉ.
 */
public class DBConnection {

    // -------------------------------------------------------------------------
    // CÁC THÔNG SỐ CẤU HÌNH MẶC ĐỊNH (DEFAULT CONNECTION CONFIGURATION)
    // -------------------------------------------------------------------------
    private static String host = "localhost";                     // Địa chỉ máy chủ CSDL (mặc định máy cục bộ)
    private static String port = "1433";                          // Cổng TCP/IP mặc định của SQL Server
    private static String databaseName = "QuanLyCuaHangThuCung";  // Tên cơ sở dữ liệu chính của dự án
    private static String user = "sa";                            // Tài khoản xác thực SQL Server Authentication
    private static String password = "sapassword";                // Mật khẩu tài khoản sa
    private static String instance = "SQLEXPRESS";                // Tên Named Instance mặc định trên Windows
    private static int loginTimeout = 3;                          // Thời gian chờ kết nối tối đa (3 giây) tránh đơ ứng dụng

    private static Connection connection = null;
    private static boolean checkedConnection = false;
    private static boolean dbAvailable = false;

    // Khối tĩnh (static block): Tự động nạp file cấu hình ngay khi class được nạp vào máy ảo JVM
    static {
        loadConfig();
    }

    // Khóa hàm khởi tạo private để ngăn chặn việc khởi tạo đối tượng trực tiếp bên ngoài
    private DBConnection() {}

    /**
     * Nạp các thông số kết nối từ file `config/database.properties`.
     * Việc dùng file cấu hình giúp linh hoạt đổi sang máy chủ khác mà không cần biên dịch lại mã nguồn Java.
     */
    private static void loadConfig() {
        File configFile = new File("config/database.properties");
        if (configFile.exists()) {
            // Sử dụng try-with-resources để tự động đóng luồng đọc file sau khi hoàn tất
            try (InputStream input = new FileInputStream(configFile)) {
                Properties prop = new Properties();
                // Đọc toàn bộ các cặp Key=Value trong file properties
                prop.load(input);

                if (prop.getProperty("db.host") != null)
                    host = prop.getProperty("db.host").trim();
                if (prop.getProperty("db.port") != null)
                    port = prop.getProperty("db.port").trim();
                if (prop.getProperty("db.name") != null)
                    databaseName = prop.getProperty("db.name").trim();
                if (prop.getProperty("db.user") != null)
                    user = prop.getProperty("db.user").trim();
                if (prop.getProperty("db.password") != null)
                    password = prop.getProperty("db.password").trim();
                if (prop.getProperty("db.instance") != null)
                    instance = prop.getProperty("db.instance").trim();
            } catch (Exception e) {
                System.err.println("[DBConnection] Không thể đọc file config/database.properties: " + e.getMessage());
            }
        }
    }

    /**
     * Xây dựng chuỗi URL kết nối chuẩn JDBC của Microsoft SQL Server Driver.
     * @param usePort true: kết nối qua cổng TCP/IP 1433; false: kết nối qua Named Instance (SQLEXPRESS).
     * @return Chuỗi JDBC URL hoàn chỉnh kèm các cờ tham số bảo mật.
     */
    private static String buildUrl(boolean usePort) {
        if (usePort && port != null && !port.isEmpty()) {
            // Định dạng URL qua TCP Port: jdbc:sqlserver://localhost:1433;databaseName=...
            // encrypt=true: Kích hoạt mã hóa SSL/TLS cho kết nối
            // trustServerCertificate=true: Tin cậy chứng chỉ tự ký (Self-signed) của SQL Server
            // loginTimeout=3: Quá 3 giây không kết nối được sẽ dừng ngay, kích hoạt cơ chế fallback
            return String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, port, databaseName, user, password, loginTimeout);
        } else {
            // Định dạng URL qua Named Instance: jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=...
            return String.format(
                    "jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, instance, databaseName, user, password, loginTimeout);
        }
    }

    /**
     * Lấy một kết nối đến CSDL (mỗi lần gọi sinh một Connection mới độc lập).
     * Điều này bảo đảm không bị lỗi đóng nhầm kết nối dùng chung giữa các tác vụ đa luồng.
     * @return Đối tượng java.sql.Connection đang mở
     * @throws SQLException Ném ra lỗi nếu cả 2 phương thức kết nối đều không thành công
     */
    public static Connection getConnection() throws SQLException {
        return getNewConnection();
    }

    /**
     * Tạo một kết nối mới độc lập phục vụ giao dịch ACID (JDBC Transactions).
     * Áp dụng kỹ thuật Fallback kép (Dual Connection Strategy).
     */
    public static Connection getNewConnection() throws SQLException {
        SQLException lastEx = null;

        // ---------------------------------------------------------------------
        // CHIẾN LƯỢC 1: Thử kết nối TCP/IP qua cổng mặc định 1433
        // ---------------------------------------------------------------------
        try {
            String url1 = buildUrl(true);
            Connection conn = DriverManager.getConnection(url1);
            dbAvailable = true;
            return conn; // Kết nối thành công -> Trả về đối tượng Connection ngay
        } catch (SQLException e1) {
            lastEx = e1;
        }

        // ---------------------------------------------------------------------
        // CHIẾN LƯỢC 2: Fallback sang kết nối Named Instance (SQLEXPRESS)
        // ---------------------------------------------------------------------
        // Thường gặp khi cài đặt SQL Server Express bản mặc định sử dụng dynamic port
        try {
            String url2 = buildUrl(false);
            Connection conn = DriverManager.getConnection(url2);
            dbAvailable = true;
            return conn;
        } catch (SQLException e2) {
            lastEx = e2;
        }

        // Nếu cả 2 chiến lược đều thất bại, đánh dấu CSDL Offline và ném ngoại lệ kèm thông báo chi tiết
        dbAvailable = false;
        throw new SQLException("Không thể kết nối đến Microsoft SQL Server (" + databaseName
                + "). Vui lòng kiểm tra lại dịch vụ SQL Server (MSSQLSERVER/SQLEXPRESS) và tài khoản sa.", lastEx);
    }

    /**
     * Kiểm tra trạng thái máy chủ CSDL có đang sẵn sàng hoạt động (ONLINE) hay không.
     * @return true nếu kết nối thành công; false nếu máy chủ CSDL chưa bật
     */
    public static boolean isDatabaseAvailable() {
        if (!checkedConnection) {
            try (Connection conn = getNewConnection()) {
                dbAvailable = (conn != null && !conn.isClosed());
            } catch (Exception e) {
                dbAvailable = false;
            }
            checkedConnection = true;
        }
        return dbAvailable;
    }

    /**
     * Kiểm tra xem ứng dụng có đang buộc phải chạy ở chế độ ngoại tuyến (Offline Mode) hay không.
     */
    public static boolean isOfflineMode() {
        return !isDatabaseAvailable();
    }

    /**
     * Thực hiện đọc lại cấu hình và thử kết nối lại đến CSDL (sau khi người dùng khởi động lại SQL Server).
     */
    public static void retryConnection() {
        loadConfig();
        closeConnection();
        checkedConnection = false;
        isDatabaseAvailable();
    }

    /**
     * Đóng kết nối tĩnh an toàn nếu có.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Phương thức main độc lập dùng để kiểm tra nhanh (Test Connectivity) kết nối CSDL từ dòng lệnh.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   KIỂM TRA KẾT NỐI MICROSOFT SQL SERVER JDBC    ");
        System.out.println("=================================================");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(">>> KẾT NỐI THÀNH CÔNG ĐẾN CSDL: " + databaseName);
                System.out.println(">>> Trạng thái máy chủ: ONLINE");
                System.out.println(">>> Driver: " + conn.getMetaData().getDriverName() + " v" + conn.getMetaData().getDriverVersion());
            }
        } catch (SQLException e) {
            System.err.println(">>> KẾT NỐI THẤT BẠI: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(">>> Nguyên nhân gốc: " + e.getCause().getMessage());
            }
        }
    }
}
