package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lớp tiện ích quản lý kết nối cơ sở dữ liệu Microsoft SQL Server (Singleton
 * Pattern).
 * Đảm bảo tối ưu hóa và an toàn khi kết nối JDBC trong toàn bộ ứng dụng.
 * Hỗ trợ nạp cấu hình từ `config/database.properties` và tự động fallback giữa
 * TCP Port 1433 và Named Instance SQLEXPRESS.
 */
public class DBConnection {

    private static String host = "localhost";
    private static String port = "1433";
    private static String databaseName = "QuanLyCuaHangThuCung";
    private static String user = "sa";
    private static String password = "sapassword";
    private static String instance = "SQLEXPRESS";
    private static int loginTimeout = 3;

    private static Connection connection = null;
    private static boolean checkedConnection = false;
    private static boolean dbAvailable = false;

    static {
        loadConfig();
    }

    private DBConnection() {
    }

    /**
     * Nạp cấu hình từ tập tin config/database.properties nếu có.
     */
    private static void loadConfig() {
        File configFile = new File("config/database.properties");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties prop = new Properties();
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
                System.err.println("[DBConnection] Không thể đọc config/database.properties: " + e.getMessage());
            }
        }
    }

    /**
     * Tạo chuỗi URL kết nối SQL Server
     */
    private static String buildUrl(boolean usePort) {
        if (usePort && port != null && !port.isEmpty()) {
            return String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, port, databaseName, user, password, loginTimeout);
        } else {
            return String.format(
                    "jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, instance, databaseName, user, password, loginTimeout);
        }
    }

    /**
     * Lấy kết nối đến SQL Server (mỗi try-with-resources nhận một Connection độc lập để tránh đóng nhầm kết nối lồng nhau).
     */
    public static Connection getConnection() throws SQLException {
        return getNewConnection();
    }

    /**
     * Tạo một kết nối mới độc lập (dành cho JDBC Transactions độc lập).
     */
    public static Connection getNewConnection() throws SQLException {
        SQLException lastEx = null;

        // Thử kết nối qua Port 1433
        try {
            String url1 = buildUrl(true);
            Connection conn = DriverManager.getConnection(url1);
            dbAvailable = true;
            return conn;
        } catch (SQLException e1) {
            lastEx = e1;
        }

        // Thử kết nối qua Named Instance SQLEXPRESS
        try {
            String url2 = buildUrl(false);
            Connection conn = DriverManager.getConnection(url2);
            dbAvailable = true;
            return conn;
        } catch (SQLException e2) {
            lastEx = e2;
        }

        dbAvailable = false;
        throw new SQLException("Không thể kết nối đến Microsoft SQL Server (" + databaseName
                + "). Vui lòng kiểm tra lại dịch vụ SQL Server và tài khoản đăng nhập.", lastEx);
    }

    /**
     * Kiểm tra trạng thái máy chủ CSDL.
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
     * Kiểm tra xem hệ thống có đang chạy chế độ Offline hay không.
     */
    public static boolean isOfflineMode() {
        return !isDatabaseAvailable();
    }

    /**
     * Thử kết nối lại.
     */
    public static void retryConnection() {
        loadConfig();
        closeConnection();
        checkedConnection = false;
        isDatabaseAvailable();
    }

    /**
     * Đóng kết nối an toàn.
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

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   KIỂM TRA KẾT NỐI MICROSOFT SQL SERVER");
        System.out.println("=================================================");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println(">>> KẾT NỐI THÀNH CÔNG ĐẾN CSDL: " + databaseName);
                System.out.println(">>> Trạng thái máy chủ: ONLINE");
            }
        } catch (SQLException e) {
            System.err.println(">>> KẾT NỐI THẤT BẠI: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(">>> Chi tiết lỗi: " + e.getCause().getMessage());
            }
        }
    }
}
