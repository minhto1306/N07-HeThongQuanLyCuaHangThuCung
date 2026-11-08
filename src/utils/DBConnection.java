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

    // đọc file cấu hình database theo thứ tự ưu tiên:
    // 1. config/database.properties (mẫu cơ bản)
    // 2. config/database.local.properties (cấu hình máy cá nhân, không theo dõi Git)
    // 3. Biến môi trường (Environment Variables - mức ưu tiên cao nhất)
    private static void loadConfig() {
        // 1. Nạp tệp cấu hình mẫu
        File baseConfigFile = new File("config/database.properties");
        if (baseConfigFile.exists()) {
            applyPropertiesFile(baseConfigFile);
        }

        // 2. Nạp tệp cấu hình máy cá nhân (nếu có) để ghi đè
        File localConfigFile = new File("config/database.local.properties");
        if (localConfigFile.exists()) {
            applyPropertiesFile(localConfigFile);
        }

        // 3. Ưu tiên cao nhất từ biến môi trường (Environment Variables) nếu có
        String envHost = System.getenv("DB_HOST");
        String envPort = System.getenv("DB_PORT");
        String envName = System.getenv("DB_NAME");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");
        String envInst = System.getenv("DB_INSTANCE");

        if (envHost != null && !envHost.isBlank()) host = envHost.trim();
        if (envPort != null && !envPort.isBlank()) port = envPort.trim();
        if (envName != null && !envName.isBlank()) databaseName = envName.trim();
        if (envUser != null && !envUser.isBlank()) user = envUser.trim();
        if (envPass != null && !envPass.isBlank()) password = envPass.trim();
        if (envInst != null && !envInst.isBlank()) instance = envInst.trim();
    }

    private static void applyPropertiesFile(File file) {
        try (InputStream input = new FileInputStream(file)) {
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
            if (prop.getProperty("db.timeout") != null) {
                try { loginTimeout = Integer.parseInt(prop.getProperty("db.timeout").trim()); } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[DBConnection] Không đọc được file config db [" + file.getName() + "]: " + e.getMessage());
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

    // mở kết nối mới bằng thông tin đã được cấu hình minh bạch, không đoán mò mật khẩu
    public static Connection getNewConnection() throws SQLException {
        if (user == null || user.isBlank() || password == null || password.isBlank()
                || password.startsWith("YOUR_") || password.contains("PLACEHOLDER")) {
            dbAvailable = false;
            throw new SQLException("Cơ sở dữ liệu chưa được cấu hình thông tin xác thực! Vui lòng cấu hình tài khoản và mật khẩu trong 'config/database.local.properties' (sao chép từ 'config/database.properties.example') hoặc đặt biến môi trường DB_USER và DB_PASSWORD.");
        }
        SQLException lastEx = null;

        // 1. Thử kết nối qua Port (chuẩn SQL Server TCP/IP)
        try {
            String url1 = buildUrl(true, password != null ? password : "");
            Connection conn = DriverManager.getConnection(url1);
            dbAvailable = true;
            return conn;
        } catch (SQLException e1) {
            lastEx = e1;
        }

        // 2. Thử kết nối qua Named Instance (SQLEXPRESS)
        try {
            String url2 = buildUrl(false, password != null ? password : "");
            Connection conn = DriverManager.getConnection(url2);
            dbAvailable = true;
            return conn;
        } catch (SQLException e2) {
            lastEx = e2;
        }

        dbAvailable = false;
        throw new SQLException("Không thể kết nối CSDL SQL Server [" + databaseName + "] trên host [" + host
                + "]. Vui lòng kiểm tra lại cấu hình trong config/database.properties hoặc biến môi trường DB_PASSWORD.", lastEx);
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
