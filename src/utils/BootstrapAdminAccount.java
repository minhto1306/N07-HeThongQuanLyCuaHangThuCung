package utils;

import entity.TaiKhoan;

import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Tiện ích Quản trị Bảo mật (CLI) - Khởi tạo tài khoản Quản lý đầu tiên hoặc Đặt lại mật khẩu nhân viên.
 * 
 * Các nguyên tắc bảo mật tuân thủ (TC01, TC02, Audit Pre-Push):
 * 1. Nhập mật khẩu tương tác ẩn qua System.console().readPassword() (không truyền mật khẩu qua tham số dòng lệnh CLI).
 * 2. Xác thực độ phức tạp mật khẩu: tối thiểu 8 ký tự, ít nhất 1 chữ hoa, 1 chữ thường và 1 chữ số.
 * 3. Nhập xác nhận mật khẩu 2 lần (Password Confirmation) để tránh gõ sai.
 * 4. Xóa trắng bộ nhớ đệm char[] (Arrays.fill(..., '\0')) ngay sau khi băm PBKDF2.
 * 5. Thực hiện trong Giao dịch nguyên tử (Atomic Transaction) với rollback khi có sự cố.
 * 6. Tách biệt rõ 2 luồng nghiệp vụ:
 *    - bootstrapAdmin: Khởi tạo/Kích hoạt tài khoản Quản lý hệ thống gốc (NV001).
 *    - resetPasswordWithAuth: Đặt lại mật khẩu cho tài khoản nhân viên theo yêu cầu có xác thực.
 * 7. Tuyệt đối không log, in hoặc xuất mật khẩu plaintext ra màn hình/file log.
 */
public final class BootstrapAdminAccount {

    private static final String DEFAULT_ADMIN_ID = "NV001";
    private static final String DEFAULT_ADMIN_PHONE = "0901234567";
    private static final String DEFAULT_ADMIN_EMAIL = "minh.quanly@miopet.vn";
    private static final String DEFAULT_ADMIN_NAME = "Lê Khãi Minh";
    private static final String DEFAULT_ADMIN_ROLE = "NVQL";

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" PETSHOP MIO - TIỆN ÍCH QUẢN TRỊ BẢO MẬT & KHỞI TẠO TÀI KHOẢN (PBKDF2)");
        System.out.println("================================================================================\n");

        if (args.length == 0 || args[0].equalsIgnoreCase("--help") || args[0].equalsIgnoreCase("-h")) {
            printUsage();
            return;
        }

        String command = args[0].trim().toLowerCase();

        try {
            switch (command) {
                case "bootstrap":
                case "init":
                    handleBootstrapAdmin();
                    break;
                case "reset":
                    if (args.length < 2) {
                        System.err.println("LỖI: Thiếu mã tài khoản cần reset!");
                        System.err.println("Cú pháp: java -cp \"bin;lib/*\" utils.BootstrapAdminAccount reset <maTaiKhoan>");
                        return;
                    }
                    handleResetPassword(args[1].trim());
                    break;
                default:
                    System.err.println("LỆNH KHÔNG HỢP LỆ. Phải dùng rõ 'bootstrap' hoặc 'reset <maTaiKhoan>'.");
                    printUsage();
                    break;
            }
        } catch (Exception e) {
            System.err.println("\n[LỖI THỰC THI] " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("HƯỚNG DẪN SỬ DỤNG:");
        System.out.println("1. Khởi tạo / Kích hoạt tài khoản Quản trị viên (NV001):");
        System.out.println("   java -cp \"bin;lib/*\" utils.BootstrapAdminAccount bootstrap\n");
        System.out.println("2. Đặt lại mật khẩu tài khoản nhân viên:");
        System.out.println("   java -cp \"bin;lib/*\" utils.BootstrapAdminAccount reset <maTaiKhoan>\n");
        System.out.println("LƯU Ý BẢO MẬT: Mật khẩu sẽ được yêu cầu nhập tương tác trực tiếp, KHÔNG truyền qua tham số dòng lệnh!");
    }

    /**
     * Khởi tạo an toàn tài khoản Quản lý đầu tiên (NV001) trong giao dịch nguyên tử.
     */
    public static boolean handleBootstrapAdmin() {
        System.out.printf(">>> BẮT ĐẦU KHỞI TẠO TÀI KHOẢN QUẢN LÝ HỆ THỐNG [%s]...\n", DEFAULT_ADMIN_ID);
        char[] password = promptPasswordWithConfirmation("Nhập mật khẩu mới cho Quản lý NV001");
        if (password == null) {
            return false;
        }

        String pbkdf2Hash;
        try {
            pbkdf2Hash = PasswordHasher.hashPBKDF2(password);
        } finally {
            Arrays.fill(password, '\0'); // Xóa trắng bộ nhớ char[] ngay sau khi băm
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Kiểm tra tài khoản TaiKhoan
                boolean taiKhoanExists;
                String checkTkSql = "SELECT maTaiKhoan FROM TaiKhoan WHERE maTaiKhoan = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkTkSql)) {
                    ps.setString(1, DEFAULT_ADMIN_ID);
                    try (ResultSet rs = ps.executeQuery()) {
                        taiKhoanExists = rs.next();
                    }
                }

                if (taiKhoanExists) {
                    conn.rollback();
                    System.err.println("[TỪ CHỐI] NV001 đã tồn tại. Bootstrap không được ghi đè; hãy dùng lệnh reset có xác thực Quản lý.");
                    return false;
                } else {
                    String insertTkSql = "INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) "
                            + "VALUES (?, ?, ?, ?, ?, 1)";
                    try (PreparedStatement ps = conn.prepareStatement(insertTkSql)) {
                        ps.setString(1, DEFAULT_ADMIN_ID);
                        ps.setString(2, DEFAULT_ADMIN_PHONE);
                        ps.setString(3, DEFAULT_ADMIN_EMAIL);
                        ps.setString(4, pbkdf2Hash);
                        ps.setString(5, DEFAULT_ADMIN_ROLE);
                        ps.executeUpdate();
                    }
                }

                // 2. Kiểm tra hồ sơ nhân viên NhanVien
                boolean nhanVienExists;
                String checkNvSql = "SELECT maNhanVien FROM NhanVien WHERE maNhanVien = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkNvSql)) {
                    ps.setString(1, DEFAULT_ADMIN_ID);
                    try (ResultSet rs = ps.executeQuery()) {
                        nhanVienExists = rs.next();
                    }
                }

                if (!nhanVienExists) {
                    String insertNvSql = "INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, email, maTaiKhoan) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertNvSql)) {
                        ps.setString(1, DEFAULT_ADMIN_ID);
                        ps.setString(2, DEFAULT_ADMIN_NAME);
                        ps.setString(3, "Quản lý hệ thống");
                        ps.setString(4, DEFAULT_ADMIN_PHONE);
                        ps.setString(5, DEFAULT_ADMIN_EMAIL);
                        ps.setString(6, DEFAULT_ADMIN_ID);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("\n[THÀNH CÔNG] Đã khởi tạo / kích hoạt mật khẩu PBKDF2 an toàn cho Quản lý NV001!");
                System.out.printf("  - Mã nhân viên : %s\n", DEFAULT_ADMIN_ID);
                System.out.printf("  - Họ và tên    : %s\n", DEFAULT_ADMIN_NAME);
                System.out.printf("  - Số điện thoại: %s (Dùng để đăng nhập POS / GUI)\n", DEFAULT_ADMIN_PHONE);
                System.out.printf("  - Vai trò      : %s\n", DEFAULT_ADMIN_ROLE);
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("\n[THẤT BẠI] Giao dịch bị hủy bỏ (Rollback): " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("\n[LỖI KẾT NỐI CSDL] " + e.getMessage());
            return false;
        }
    }

    /**
     * Đặt lại mật khẩu có xác thực cho nhân viên bất kỳ trong giao dịch nguyên tử.
     */
    public static boolean handleResetPassword(String maTaiKhoan) {
        System.out.printf(">>> ĐẶT LẠI MẬT KHẨU CHO TÀI KHOẢN [%s]...\n", maTaiKhoan);

        // Kiểm tra tài khoản tồn tại trước khi hỏi mật khẩu
        try (Connection conn = DBConnection.getConnection()) {
            if (!xacThucQuanLyChoReset(conn)) {
                System.err.println("[TỪ CHỐI] Không xác thực được một tài khoản NVQL đang hoạt động.");
                return false;
            }
            String checkSql = "SELECT tk.maTaiKhoan, tk.soDienThoai, tk.email, tk.vaiTro, nv.hoTen "
                    + "FROM TaiKhoan tk "
                    + "LEFT JOIN NhanVien nv ON tk.maTaiKhoan = nv.maTaiKhoan "
                    + "WHERE tk.maTaiKhoan = ?";
            String hoTen = "N/A";
            String sdt = "N/A";
            String vaiTro = "N/A";

            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, maTaiKhoan);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.err.printf("\n[LỖI] Không tìm thấy tài khoản [%s] trong hệ thống CSDL!\n", maTaiKhoan);
                        return false;
                    }
                    sdt = rs.getString("soDienThoai");
                    vaiTro = rs.getString("vaiTro");
                    if (rs.getString("hoTen") != null) {
                        hoTen = rs.getString("hoTen");
                    }
                }
            }

            System.out.printf("  - Tài khoản  : %s (Họ tên: %s, SĐT: %s, Vai trò: %s)\n", maTaiKhoan, hoTen, sdt, vaiTro);
            char[] password = promptPasswordWithConfirmation("Nhập mật khẩu mới cho " + maTaiKhoan);
            if (password == null) {
                return false;
            }

            String pbkdf2Hash;
            try {
                pbkdf2Hash = PasswordHasher.hashPBKDF2(password);
            } finally {
                Arrays.fill(password, '\0');
            }

            conn.setAutoCommit(false);
            try {
                String updateSql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTaiKhoan = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, pbkdf2Hash);
                    ps.setString(2, maTaiKhoan);
                    int updated = ps.executeUpdate();
                    if (updated > 0) {
                        conn.commit();
                        System.out.printf("\n[THÀNH CÔNG] Đã cập nhật mật khẩu PBKDF2 mới cho tài khoản [%s] thành công!\n", maTaiKhoan);
                        return true;
                    } else {
                        conn.rollback();
                        System.err.printf("\n[THẤT BẠI] Không thể cập nhật mật khẩu cho [%s]!\n", maTaiKhoan);
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("\n[THẤT BẠI] Rollback giao dịch: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("\n[LỖI KẾT NỐI CSDL] " + e.getMessage());
            return false;
        }
    }

    private static boolean xacThucQuanLyChoReset(Connection conn) throws SQLException {
        Console console = System.console();
        if (console == null) {
            System.err.println("Không có terminal tương tác an toàn; không thể xác thực Quản lý để reset mật khẩu.");
            return false;
        }
        String maQuanLy = console.readLine("Mã tài khoản Quản lý phê duyệt: ");
        char[] matKhau = console.readPassword("Mật khẩu Quản lý: ");
        if (maQuanLy == null || maQuanLy.isBlank() || matKhau == null) return false;
        try {
            String sql = "SELECT matKhau FROM TaiKhoan WHERE maTaiKhoan=? AND vaiTro='NVQL' AND trangThaiHoatDong=1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, maQuanLy.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() && PasswordHasher.verifyPassword(new String(matKhau), rs.getString(1));
                }
            }
        } finally {
            Arrays.fill(matKhau, '\0');
        }
    }

    /**
     * Nhập mật khẩu tương tác ẩn và xác nhận lại, kiểm tra độ phức tạp nghiêm ngặt.
     */
    private static char[] promptPasswordWithConfirmation(String promptMessage) {
        Console console = System.console();
        char[] pwd1;
        char[] pwd2;

        if (console != null) {
            pwd1 = console.readPassword("%s: ", promptMessage);
            if (pwd1 == null || pwd1.length == 0) {
                System.err.println("LỖI: Mật khẩu không được để trống!");
                return null;
            }
            pwd2 = console.readPassword("Xác nhận lại mật khẩu: ");
        } else {
            System.out.printf("%s (Lưu ý: Môi trường không có Console ẩn, đang dùng luồng chuẩn): ", promptMessage);
            Scanner scanner = new Scanner(System.in);
            if (!scanner.hasNextLine()) {
                System.err.println("LỖI: Không đọc được dữ liệu từ bàn phím!");
                return null;
            }
            pwd1 = scanner.nextLine().toCharArray();
            System.out.print("Xác nhận lại mật khẩu: ");
            pwd2 = scanner.hasNextLine() ? scanner.nextLine().toCharArray() : new char[0];
        }

        // 1. Kiểm tra 2 mật khẩu trùng khớp
        if (!Arrays.equals(pwd1, pwd2)) {
            System.err.println("LỖI BẢO MẬT: Mật khẩu xác nhận không trùng khớp! Vui lòng thử lại.");
            Arrays.fill(pwd1, '\0');
            Arrays.fill(pwd2, '\0');
            return null;
        }
        Arrays.fill(pwd2, '\0');

        // 2. Kiểm tra độ phức tạp mật khẩu
        String validationError = validatePasswordComplexity(pwd1);
        if (validationError != null) {
            System.err.println("LỖI ĐỘ PHỨC TẠP MẬT KHẨU: " + validationError);
            Arrays.fill(pwd1, '\0');
            return null;
        }

        return pwd1;
    }

    /**
     * Kiểm tra tiêu chuẩn độ an toàn mật khẩu:
     * - Độ dài >= 8
     * - Chứa ít nhất 1 chữ hoa
     * - Chứa ít nhất 1 chữ thường
     * - Chứa ít nhất 1 chữ số
     */
    public static String validatePasswordComplexity(char[] password) {
        if (password == null || password.length < 8) {
            return "Mật khẩu phải có độ dài tối thiểu 8 ký tự.";
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        if (!hasUpper) return "Mật khẩu phải chứa ít nhất 1 chữ cái in hoa (A-Z).";
        if (!hasLower) return "Mật khẩu phải chứa ít nhất 1 chữ cái in thường (a-z).";
        if (!hasDigit) return "Mật khẩu phải chứa ít nhất 1 chữ số (0-9).";

        return null; // Hợp lệ
    }
}
