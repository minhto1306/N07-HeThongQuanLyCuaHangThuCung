package utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Lớp tiện ích tích hợp Cổng thanh toán VNPay chuẩn phiên bản 2.1.0 (Nhóm 07).
 * - Sinh mã băm HMAC-SHA512 bảo mật toàn vẹn giao dịch.
 * - Sắp xếp tham số theo bảng chữ cái và sinh Payment URL chuyển hướng sang Sandbox.
 * - Kiểm tra chữ ký số (Checksum) khi nhận dữ liệu phản hồi từ VNPay.
 */
public final class VNPayHelper {

    private static String tmnCode = "2QXUI4J4";
    private static String hashSecret = "RA3U8AXPMRF3F8W3L6M8X6E8Z9R5H4Y8";
    private static String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static int returnPort = 8888;
    private static String returnPath = "/vnpay_return";

    static {
        napCauHinh();
    }

    private VNPayHelper() {}

    public static void napCauHinh() {
        File f = new File("config/vnpay.properties");
        if (f.exists()) {
            try (InputStream in = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(in);
                if (p.getProperty("vnpay.tmnCode") != null) tmnCode = p.getProperty("vnpay.tmnCode").trim();
                if (p.getProperty("vnpay.hashSecret") != null) hashSecret = p.getProperty("vnpay.hashSecret").trim();
                if (p.getProperty("vnpay.payUrl") != null) payUrl = p.getProperty("vnpay.payUrl").trim();
                if (p.getProperty("vnpay.returnPort") != null) returnPort = Integer.parseInt(p.getProperty("vnpay.returnPort").trim());
                if (p.getProperty("vnpay.returnPath") != null) returnPath = p.getProperty("vnpay.returnPath").trim();
            } catch (Exception ex) {
                System.err.println("[VNPayHelper] Lỗi đọc config/vnpay.properties: " + ex.getMessage());
            }
        }
    }

    public static String getTmnCode() { return tmnCode; }
    public static String getHashSecret() { return hashSecret; }
    public static String getPayUrl() { return payUrl; }
    public static int getReturnPort() { return returnPort; }
    public static String getReturnPath() { return returnPath; }

    /**
     * Băm HMAC-SHA512 với khóa bí mật
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) return "";
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * bytes.length);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            System.err.println("[VNPayHelper] Lỗi HMAC-SHA512: " + ex.getMessage());
            return "";
        }
    }

    /**
     * Tạo URL thanh toán VNPay Sandbox v2.1.0
     */
    public static String taoUrlThanhToan(String maHD, long soTienVnd, String returnUrl) {
        try {
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(soTienVnd * 100L)); // VNPay quy định nhân 100
            vnp_Params.put("vnp_CurrCode", "VND");
            
            // Mã giao dịch duy nhất
            String txnRef = maHD + "_" + (System.currentTimeMillis() % 10000000L);
            vnp_Params.put("vnp_TxnRef", txnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + maHD);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1");

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Sắp xếp tham số theo alphabet (A-Z)
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();

            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(hashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

            return payUrl + "?" + queryUrl;
        } catch (UnsupportedEncodingException ex) {
            System.err.println("[VNPayHelper] Lỗi mã hóa URL: " + ex.getMessage());
            return payUrl;
        }
    }

    /**
     * Xác thực chữ ký số từ dữ liệu Callback của VNPay
     */
    public static boolean xacThucChuKy(Map<String, String> fields) {
        try {
            String vnp_SecureHash = fields.get("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) return false;

            Map<String, String> vnp_Fields = new HashMap<>(fields);
            vnp_Fields.remove("vnp_SecureHash");
            vnp_Fields.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(vnp_Fields.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();

            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Fields.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }

            String signValue = hmacSHA512(hashSecret, hashData.toString());
            return signValue.equalsIgnoreCase(vnp_SecureHash);
        } catch (Exception ex) {
            return false;
        }
    }
}
