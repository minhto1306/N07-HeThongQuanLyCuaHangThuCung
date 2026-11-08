package utils;

import java.awt.Color;
import java.time.LocalDateTime;

/**
 * Chuẩn hóa trạng thái cuộc hẹn dịch vụ Spa / Grooming.
 * Đảm bảo đồng bộ giữa CSDL SQL Server và giao diện người dùng Swing.
 */
public enum TrangThaiCuocHen {
    HOAN_THANH("Đã hoàn thành", new Color(0xDC, 0xFC, 0xE7), new Color(0x15, 0x80, 0x3D)),
    DANG_THUC_HIEN("Đang phục vụ", new Color(0xDB, 0xEA, 0xFE), new Color(0x1D, 0x4E, 0xD8)),
    CHO_TIEP_NHAN("Chờ tiếp nhận", new Color(0xFE, 0xF3, 0xC7), new Color(0x92, 0x40, 0x0E)),
    HUY("Đã hủy", new Color(0xF3, 0xF4, 0xF6), new Color(0x6B, 0x72, 0x80));

    private final String moTa;
    private final Color bgColor;
    private final Color fgColor;

    TrangThaiCuocHen(String moTa, Color bgColor, Color fgColor) {
        this.moTa = moTa;
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    public String moTa() {
        return moTa;
    }

    public Color getBgColor() {
        return bgColor;
    }

    public Color getFgColor() {
        return fgColor;
    }

    /**
     * Chuyển đổi an toàn từ chuỗi DB / văn bản tự do sang Enum chuẩn.
     * Tự động loại bỏ bất kỳ hậu tố ngoặc phụ nào như "(Quá giờ)", "(Trễ hẹn)", "(⚠️ Quá giờ)".
     */
    public static TrangThaiCuocHen fromString(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String s = raw.replaceAll("\\s*\\(.*\\)$", "").trim().toLowerCase();
        if (s.contains("hoàn thành") || s.contains("hoan thanh") || s.contains("xong")) {
            return HOAN_THANH;
        }
        if (s.contains("phục vụ") || s.contains("phuc vu") || s.contains("thực hiện") || s.contains("thuc hien")) {
            return DANG_THUC_HIEN;
        }
        if (s.contains("chờ") || s.contains("cho") || s.contains("tiếp nhận") || s.contains("tiep nhan") || s.contains("đặt") || s.contains("dat")) {
            return CHO_TIEP_NHAN;
        }
        if (s.contains("hủy") || s.contains("huy")) {
            return HUY;
        }
        return null;
    }

    /**
     * Kiểm tra ca có bị quá giờ hay không.
     * QUY TẮC BẮT BUỘC:
     * - Null an toàn: thoiGianHen null hoặc st null -> false.
     * - HOAN_THANH và HUY: KHÔNG BAO GIỜ bị coi là quá giờ.
     * - Lịch hẹn tương lai: Chưa đến giờ nên KHÔNG quá giờ.
     * - DANG_THUC_HIEN: Quá thời lượng dịch vụ đăng ký (mặc định 60 phút).
     * - CHO_TIEP_NHAN: Đã quá 15 phút sau giờ hẹn mà chưa tiếp nhận.
     */
    public static boolean isCaQuaGio(LocalDateTime thoiGianHen, TrangThaiCuocHen st, int thoiLuongPhut) {
        if (thoiGianHen == null || st == null) {
            return false;
        }
        if (st == HOAN_THANH || st == HUY) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (thoiGianHen.isAfter(now)) {
            return false;
        }
        long elapsedMinutes = java.time.Duration.between(thoiGianHen, now).toMinutes();
        if (st == DANG_THUC_HIEN) {
            int duration = thoiLuongPhut > 0 ? thoiLuongPhut : 60;
            return elapsedMinutes > duration;
        }
        // Chờ tiếp nhận: khách trễ hẹn quá 15 phút
        return elapsedMinutes > 15;
    }
}
