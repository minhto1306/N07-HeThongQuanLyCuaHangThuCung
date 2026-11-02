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

    private DBConnection() {}

    // đọc file database.properties trong thư mục config
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
                System.err.println("ko doc dc file config db: " + e.getMessage());
            }
        }
    }

    // hàm tạo chuỗi kết nối
    private static String buildUrl(boolean usePort) {
        if (usePort && port != null && !port.isEmpty()) {
            // kết nối qua port 1433
            return String.format(
                    "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, port, databaseName, user, password, loginTimeout);
        } else {
            // kết nối qua sqlexpress
            return String.format(
                    "jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=true;loginTimeout=%d;",
                    host, instance, databaseName, user, password, loginTimeout);
        }
    }

    public static Connection getConnection() throws SQLException {
        return getNewConnection();
    }

    // mở kết nối mới
    public static Connection getNewConnection() throws SQLException {
        SQLException lastEx = null;

        // thử port 1433 trước
        try {
            String url1 = buildUrl(true);
            Connection conn = DriverManager.getConnection(url1);
            dbAvailable = true;
            return conn;
        } catch (SQLException e1) {
            lastEx = e1;
        }

        // nếu port tạch thì thử qua sqlexpress
        try {
            String url2 = buildUrl(false);
            Connection conn = DriverManager.getConnection(url2);
            dbAvailable = true;
            return conn;
        } catch (SQLException e2) {
            lastEx = e2;
        }

        dbAvailable = false;
        throw new SQLException("Không kết nối được SQL Server (" + databaseName + "), kiểm tra lại db giùm", lastEx);
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
