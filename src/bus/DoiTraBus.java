package bus;

import dao.HoaDonDAO;
import entity.PhieuDoiTra;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * Controller điều phối nghiệp vụ Đổi trả hàng hóa, Hoàn tiền và Kiểm soát số lượng đã trả.
 * Tách biệt hoàn toàn tầng GUI khỏi HoaDonDAO.
 */
public class DoiTraBus {

    public String sinhMaPhieuDoiTraMoi() {
        return hoaDonDAO.sinhMaPhieuDoiTraMoi();
    }

    private final HoaDonDAO hoaDonDAO;

    public DoiTraBus() {
        this.hoaDonDAO = new HoaDonDAO();
    }

    /**
     * Tra cứu số lượng từng sản phẩm đã đổi trả trong các lần trước của hóa đơn để tránh trả vượt số lượng đã mua.
     */
    public Map<String, Integer> layMapSoLuongDaTra(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        return hoaDonDAO.layMapSoLuongDaTra(maHoaDon.trim());
    }

    /**
     * Lưu phiếu đổi trả hàng và chi tiết các mặt hàng hoàn trả vào CSDL.
     */
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) {
        if (pdt == null) return false;
        try {
            return hoaDonDAO.luuPhieuDoiTra(pdt);
        } catch (SQLException e) {
            System.err.println("[DoiTraBus] Lỗi luuPhieuDoiTra: " + e.getMessage());
            return false;
        }
    }
}
