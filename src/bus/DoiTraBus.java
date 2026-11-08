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
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt, String sdtKhachHang, int diemKhauTru) {
        if (!utils.UserSession.canAccessDoiTraHang()) {
            throw new SecurityException("Người dùng hiện tại không có quyền thực hiện đổi trả hàng!");
        }
        if (pdt == null) return false;
        try {
            return hoaDonDAO.luuPhieuDoiTra(pdt, sdtKhachHang, diemKhauTru);
        } catch (SQLException e) {
            System.err.println("[DoiTraBus] Lỗi luuPhieuDoiTra: " + e.getMessage());
            return false;
        }
    }

    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) {
        return luuPhieuDoiTra(pdt, null, 0);
    }
    /**
     * Tra cứu danh sách các số lô đã xuất bán của sản phẩm trong hóa đơn gốc.
     */
    public java.util.List<String> layDanhSachSoLoXuat(String maHoaDon, String maSanPham) {
        return hoaDonDAO.layDanhSachSoLoXuat(maHoaDon, maSanPham);
    }
}