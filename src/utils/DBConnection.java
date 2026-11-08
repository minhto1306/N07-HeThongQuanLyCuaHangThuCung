package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    // mấy thông số kết nối mặc định, xíu nạp từ file database.properties đè lên
    private static String host = "localhost";
    private static String port = "1433";
    private static String databaseName = "QuanLyCuaHangThuCung";
    private static String user = "";
    private static String password = "";
    private static String instance = "SQLEXPRESS";
    private static int loginTimeout = 3;

    private static Connection connection = null;
    private static boolean checkedConnection = false;
    private static boolean dbAvailable = false;

    static {
        loadConfig();
    }

    private DBConnection() {}

    // đọc file database.properties trong thư mục config
    private static void loadConfig() {
        String configPath = System.getProperty("petshop.db.config", "config/database.properties");
        File configFile = new File(configPath);
        if (!configFile.isFile() && System.getProperty("petshop.db.config") != null) {
            throw new IllegalStateException("Không tìm thấy cấu hình CSDL: " + configFile.getAbsolutePath());
        }
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
                System.err.println("ko doc dc file config db: " + e.getMessage());
            }
        }
        // Đọc dự phòng biến môi trường hệ thống nếu chưa có trong file (null-safe check)
        if (user == null || user.isBlank()) {
            String envUser = System.getenv("DB_USER");
            if (envUser != null && !envUser.isBlank()) user = envUser.trim();
        }
        if (password == null || password.isBlank()) {
            String envPass = System.getenv("DB_PASSWORD");
            if (envPass != null && !envPass.isBlank()) password = envPass.trim();
        }
    }

    // hàm tạo chuỗi kết nối
    private static String buildUrl(boolean usePort) {
        return buildUrl(usePort, password);
    }

    private static String buildUrl(boolean usePort, String pass) {
        if (usePort && port != null && !port.isEmpty()) {
            // kết nối qua port 1433
            return String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, port, databaseName, user, pass, loginTimeout);
        } else {
            // kết nối qua sqlexpress
            return String.format(
                    "jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, instance, databaseName, user, pass, loginTimeout);
        }
    }

    public static Connection getConnection() throws SQLException {
        return getNewConnection();
    }

    // mở kết nối mới (chỉ dùng tài khoản và mật khẩu được cấu hình tường minh, cấm mật khẩu rỗng và cấm tự gán sa)
    public static Connection getNewConnection() throws SQLException {
        if (user == null || user.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình tài khoản (db.user) truy cập CSDL trong config/database.properties hoặc biến môi trường DB_USER!");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình mật khẩu (db.password) truy cập CSDL trong config/database.properties hoặc biến môi trường DB_PASSWORD!");
        }
        SQLException lastEx = null;

        // 1. Thử kết nối qua port mặc định 1433
        try {
            String url1 = buildUrl(true, password);
            Connection conn = DriverManager.getConnection(url1);
            dbAvailable = true;
            return conn;
        } catch (SQLException e1) {
            lastEx = e1;
        }

        // 2. Thử kết nối qua Named Instance SQLEXPRESS
        try {
            String url2 = buildUrl(false, password);
            Connection conn = DriverManager.getConnection(url2);
            dbAvailable = true;
            return conn;
        } catch (SQLException e2) {
            lastEx = e2;
        }

        dbAvailable = false;
        throw new SQLException("Không thể kết nối đến máy chủ CSDL SQL Server (" + databaseName + ") với tài khoản [" + user + "]. Vui lòng kiểm tra lại cấu hình trong " + System.getProperty("petshop.db.config", "config/database.properties") + "!", lastEx);
    }

    // check xem có đang online ko
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

    public static boolean isOfflineMode() {
        return !isDatabaseAvailable();
    }

    public static void retryConnection() {
        loadConfig();
        closeConnection();
        checkedConnection = false;
        isDatabaseAvailable();
    }

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

    // hàm main test thử kết nối
    public static void main(String[] args) {
        System.out.println("Dang test ket noi db...");
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Ket noi ok roi nha: " + databaseName);
            }
        } catch (SQLException e) {
            System.err.println("Ket noi that bai: " + e.getMessage());
        }
    }
}
