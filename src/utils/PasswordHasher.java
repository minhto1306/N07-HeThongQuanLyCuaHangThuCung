package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Tiện ích băm mật khẩu bảo mật chuẩn công nghiệp PBKDF2WithHmacSHA256 kết hợp salt ngẫu nhiên.
 * Hỗ trợ tương thích ngược với hash SHA-256 cũ và tự động nâng cấp sang PBKDF2 sau khi đăng nhập thành công.
 * So sánh mật khẩu bằng constant-time comparison chống Timing Attack.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    public static final int DEFAULT_ITERATIONS = 65536;
    private static final int SALT_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {}

    /**
     * Băm mật khẩu từ mảng char[] theo chuẩn PBKDF2WithHmacSHA256 kèm salt ngẫu nhiên 16 bytes.
     * Tránh lưu trữ mật khẩu plaintext dưới dạng String trong bộ nhớ JVM heap.
     */
    public static String hashPBKDF2(char[] password) {
        if (password == null || password.length == 0) {
            return "";
        }
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS);
        return "PBKDF2$" + DEFAULT_ITERATIONS + "$" + bytesToHex(salt) + "$" + bytesToHex(hash);
    }

    /**
     * Băm mật khẩu theo chuẩn PBKDF2WithHmacSHA256 kèm salt ngẫu nhiên 16 bytes.
     * Định dạng kết quả: PBKDF2$65536$<saltHex>$<derivedKeyHex>
     */
    public static String hashPBKDF2(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return hashPBKDF2(password.toCharArray());
    }

    /**
     * Băm chuỗi mật khẩu đầu vào sang mã băm SHA-256 (64 ký tự hex viết thường) phục vụ tương thích ngược.
     */
    public static String hashSHA256(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input.matches("^[0-9a-fA-F]{64}$")) {
            return input.toLowerCase();
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Thuật toán SHA-256 không khả dụng trên môi trường JVM này", e);
        }
    }

    /**
     * Kiểm tra mật khẩu người dùng nhập vào có khớp với mã băm lưu trong CSDL hay không.
     * Hỗ trợ cả định dạng mới (PBKDF2) và định dạng cũ (SHA-256 64 ký tự hex).
     * Dùng constant-time comparison chống Timing Attack.
     */
    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        String trimmedStored = storedHash.trim();

        // 1. Kiểm tra định dạng PBKDF2: PBKDF2$<iterations>$<saltHex>$<hashHex>
        if (trimmedStored.startsWith("PBKDF2$")) {
            String[] parts = trimmedStored.split("\\$");
            if (parts.length != 4) {
                return false;
            }
            try {
                int iterations = Integer.parseInt(parts[1]);
                byte[] salt = hexToBytes(parts[2]);
                byte[] expectedHash = hexToBytes(parts[3]);
                byte[] actualHash = pbkdf2(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
                return MessageDigest.isEqual(actualHash, expectedHash);
            } catch (Exception e) {
                System.err.println("[PasswordHasher] Lỗi kiểm tra hash PBKDF2: " + e.getMessage());
                return false;
            }
        }

        // 2. Tương thích ngược với hash SHA-256 cũ (chuỗi 64 ký tự hex)
        byte[] hashedInput = hashSHA256(rawPassword).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = trimmedStored.toLowerCase().getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(hashedInput, expected);
    }

    /**
     * Kiểm tra hash hiện tại trong CSDL có cần được nâng cấp lên PBKDF2 hay không.
     * Trả về true nếu hash đang là định dạng SHA-256 cũ hoặc không phải PBKDF2.
     */
    public static boolean needsUpgrade(String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return true;
        }
        String trimmed = storedHash.trim();
        if (!trimmed.startsWith("PBKDF2$")) {
            return true;
        }
        String[] parts = trimmed.split("\\$");
        if (parts.length != 4) {
            return true;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            return iterations < DEFAULT_ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Lỗi thuật toán PBKDF2: " + e.getMessage(), e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
