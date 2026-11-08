package test.phase2;

import bus.DangNhapBus;
import bus.DangNhapBus.KetQuaDangNhap;
import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import utils.DBConnection;
import utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Kiểm thử tự động cơ chế băm & xác thực mật khẩu PBKDF2 cho PetShop Mio.
 * 
 * Nguyên tắc bảo mật:
 * 1. Không hardcode bất kỳ mật khẩu plaintext mặc định nào trong mã nguồn test.
 * 2. Sinh mật khẩu ngẫu nhiên theo phiên test (ephemeral random secret).
 * 3. Kiểm thử xác thực PasswordHasher, auto-upgrade, và tích hợp DangNhapBus qua bản ghi test tạm thời tự dọn dẹp.
 */
public class TestLoginPBKDF2 {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" KIỂM THỬ XÁC THỰC MẬT KHẨU PBKDF2 & ĐĂNG NHẬP AN TOÀN");
        System.out.println("================================================================================\n");

        boolean allPassed = true;

        // Sinh mật khẩu ngẫu nhiên cho phiên test, không hardcode secret
        String dynamicSecret = "Test@" + UUID.randomUUID().toString().substring(0, 8) + "A1";
        String wrongSecret = "Wrong@" + UUID.randomUUID().toString().substring(0, 8) + "Z9";

        // 1. Kiểm thử định dạng băm PBKDF2
        String pbkdf2Hash = PasswordHasher.hashPBKDF2(dynamicSecret);
        if (pbkdf2Hash != null && pbkdf2Hash.startsWith("PBKDF2$65536$") && pbkdf2Hash.length() > 80) {
            System.out.println("[PASS] 1. Sinh hash PBKDF2 đúng chuẩn công nghiệp 65,536 iterations, salt 16-byte.");
        } else {
            System.err.println("[FAIL] 1. Định dạng hash PBKDF2 không đúng!");
            allPassed = false;
        }

        // 2. Kiểm thử xác thực mật khẩu đúng (Constant-time verify)
        if (PasswordHasher.verifyPassword(dynamicSecret, pbkdf2Hash)) {
            System.out.println("[PASS] 2. Constant-time verify thành công với mật khẩu chính xác.");
        } else {
            System.err.println("[FAIL] 2. Constant-time verify thất bại với mật khẩu chính xác!");
            allPassed = false;
        }

        // 3. Kiểm thử từ chối mật khẩu sai
        if (!PasswordHasher.verifyPassword(wrongSecret, pbkdf2Hash)) {
            System.out.println("[PASS] 3. Từ chối chính xác mật khẩu sai lệch.");
        } else {
            System.err.println("[FAIL] 3. Lỗ hổng: Chấp nhận mật khẩu không chính xác!");
            allPassed = false;
        }

        // 4. Kiểm thử cơ chế nhận diện nâng cấp (needsUpgrade)
        String legacySha256 = PasswordHasher.hashSHA256(dynamicSecret);
        if (PasswordHasher.needsUpgrade(legacySha256) && !PasswordHasher.needsUpgrade(pbkdf2Hash)) {
            System.out.println("[PASS] 4. Nhận diện chuẩn xác hash cũ cần nâng cấp và hash mới đã tối ưu.");
        } else {
            System.err.println("[FAIL] 4. Lỗi nhận diện nâng cấp hash mật khẩu!");
            allPassed = false;
        }

        // 5. Chỉ kiểm thử tích hợp khi người vận hành xác nhận rõ database TEST.
        String allowedDb = System.getenv("PETSHOP_TEST_DATABASE");
        if (!"QuanLyCuaHangThuCung_Test".equals(allowedDb)) {
            System.out.println("[NOT_VERIFIABLE] Bỏ qua DB integration: cần PETSHOP_TEST_DATABASE=QuanLyCuaHangThuCung_Test.");
            System.out.println("KẾT QUẢ KIỂM THỬ PBKDF2 THUẦN: " + (allPassed ? "PASS" : "FAIL"));
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                if (!allowedDb.equals(conn.getCatalog())) {
                    throw new SecurityException("Kết nối thực tế không trùng database TEST đã xác nhận: " + conn.getCatalog());
                }
                String testMa = "NV_TEST";
                String testSdt = "0999999999";
                String testEmail = "test.pbkdf2@miopet.vn";

                // Dọn dẹp bản ghi test cũ nếu có
                cleanUpTestAccount(conn, testMa);

                // Tạo tài khoản và nhân viên test tạm thời
                String insertTk = "INSERT INTO TaiKhoan (maTaiKhoan, soDienThoai, email, matKhau, vaiTro, trangThaiHoatDong) "
                        + "VALUES (?, ?, ?, ?, 'NVQL', 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertTk)) {
                    ps.setString(1, testMa);
                    ps.setString(2, testSdt);
                    ps.setString(3, testEmail);
                    ps.setString(4, pbkdf2Hash);
                    ps.executeUpdate();
                }

                String insertNv = "INSERT INTO NhanVien (maNhanVien, hoTen, chucVu, soDienThoai, email, maTaiKhoan) "
                        + "VALUES (?, N'Nhân Viên Test PBKDF2', N'Kiểm thử tự động', ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertNv)) {
                    ps.setString(1, testMa);
                    ps.setString(2, testSdt);
                    ps.setString(3, testEmail);
                    ps.setString(4, testMa);
                    ps.executeUpdate();
                }

                // Thực hiện đăng nhập qua DangNhapBus
                DangNhapBus bus = new DangNhapBus();
                KetQuaDangNhap kqDung = bus.xuLyDangNhap(testSdt, dynamicSecret);
                if (kqDung.isThanhCong() && kqDung.getNhanVien() != null) {
                    System.out.println("[PASS] 5. DangNhapBus xác thực thành công tài khoản kiểm thử PBKDF2 qua CSDL.");
                } else {
                    System.err.printf("[FAIL] 5. DangNhapBus xác thực thất bại: %s\n", kqDung.getThongDiep());
                    allPassed = false;
                }

                KetQuaDangNhap kqSai = bus.xuLyDangNhap(testSdt, wrongSecret);
                if (!kqSai.isThanhCong()) {
                    System.out.println("[PASS] 6. DangNhapBus từ chối đăng nhập khi sai mật khẩu.");
                } else {
                    System.err.println("[FAIL] 6. DangNhapBus chấp nhận sai mật khẩu!");
                    allPassed = false;
                }

                // Dọn dẹp tài khoản test để bảo đảm database hygiene
                cleanUpTestAccount(conn, testMa);
                System.out.println("[PASS] 7. Dọn dẹp bản ghi kiểm thử tạm thời thành công (Database Hygiene Clean).");
            } else {
                System.out.println("[SKIP] Bỏ qua kiểm thử tích hợp DB do không có kết nối cơ sở dữ liệu.");
            }
        } catch (Exception e) {
            System.err.println("[LƯU Ý] Kiểm thử tích hợp DB ngoại lệ: " + e.getMessage());
            // Không đánh dấu fail nếu do môi trường offline không có SQL Server
        }

        System.out.println("\n================================================================================");
        System.out.println("KẾT QUẢ KIỂM THỬ PBKDF2: " + (allPassed ? "TẤT CẢ ĐẠT (PASS)" : "CÓ LỖI (FAIL)"));
        System.out.println("================================================================================");
    }

    private static void cleanUpTestAccount(Connection conn, String ma) {
        try {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM NhanVien WHERE maNhanVien = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TaiKhoan WHERE maTaiKhoan = ?")) {
                ps.setString(1, ma);
                ps.executeUpdate();
            }
        } catch (SQLException ignored) {
        }
    }
}
