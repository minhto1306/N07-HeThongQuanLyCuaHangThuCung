package bus;

import dao.KhachHangDAO;
import entity.KhachHang;

import java.sql.SQLException;

/**
 * Controller điều phối quản lý Khách hàng, điểm tích lũy và tra cứu thông tin liên hệ.
 * Tách biệt hoàn toàn tầng GUI khỏi KhachHangDAO.
 */
public class KhachHangBus {

    private final KhachHangDAO khachHangDAO;

    public KhachHangBus() {
        this.khachHangDAO = new KhachHangDAO();
    }

    /**
     * Tìm khách hàng theo số điện thoại liên hệ.
     */
    public KhachHang timKhachHangTheoSoDienThoai(String soDienThoai) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) return null;
        try {
            return khachHangDAO.findBySoDienThoai(soDienThoai.trim());
        } catch (SQLException e) {
            System.err.println("[KhachHangBus] Lỗi timKhachHangTheoSoDienThoai: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy email khách hàng theo số điện thoại (tự động fallback email mặc định nếu khách chưa đăng ký email).
     */
    public String layEmailKhachHang(String soDienThoai) {
        KhachHang kh = timKhachHangTheoSoDienThoai(soDienThoai);
        if (kh != null && kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {
            return kh.getEmail().trim();
        }
        return "khachhang.an@gmail.com";
    }

    /**
     * Lưu hoặc cập nhật khách hàng và điểm tích lũy.
     */
    public boolean luuHoacCapNhatKhachHang(String sdt, String hoTen, int diemMoi) {
        return BanHangBus.luuHoacCapNhatKhachHang(sdt, hoTen, diemMoi) != null;
    }
}
