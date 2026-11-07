package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm mật khẩu bảo mật chuẩn SHA-256.
 * Không cần phụ thuộc thư viện ngoài, dùng MessageDigest chuẩn của JDK.
 */
public final class PasswordHasher {

    private PasswordHasher() {}

    /**
     * Băm chuỗi mật khẩu đầu vào sang mã băm SHA-256 (64 ký tự hex viết thường).
     * @param input Mật khẩu dạng plain text
     * @return Chuỗi hex 64 ký tự, hoặc chuỗi rỗng nếu input null
     */
    public static String hashSHA256(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        // Nếu đã là chuỗi hex SHA-256 64 ký tự thì tránh băm kép
        if (input.matches("^[0-9a-fA-F]{64}$")) {
            return input.toLowerCase();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán SHA-256 không khả dụng trên môi trường JVM này", e);
        }
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập vào có khớp với mã băm SHA-256 lưu trong CSDL hay không.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        byte[] hashedInput = hashSHA256(rawPassword).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = storedHash.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(hashedInput, expected);
    }
}

