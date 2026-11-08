package bus;

import dao.HoaDonDAO;
import entity.PhieuDoiTra;

import java.sql.SQLException;
import java.math.BigDecimal;
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

        if (pdt.getDsChiTietDoiTra() == null || pdt.getDsChiTietDoiTra().isEmpty()) {
            throw new IllegalArgumentException("Phiếu đổi trả phải có ít nhất một mặt hàng");
        }

        // Chuẩn hóa và tính toán lại các trường tài chính đổi trả (TC09-TC10)
        BigDecimal giaTriTra = BigDecimal.ZERO;
        for (entity.ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
            if (ct == null || ct.getSanPham() == null
                    || ct.getSanPham().getMaSanPham() == null
                    || ct.getSanPham().getMaSanPham().isBlank()) {
                throw new IllegalArgumentException("Chi tiết đổi trả chưa xác định sản phẩm");
            }
            if (ct.getSoLuongDoiTra() <= 0) {
                throw new IllegalArgumentException("Số lượng đổi trả phải lớn hơn 0");
            }
            if (ct.getSoLuongCoSoHoan() < 0) {
                throw new IllegalArgumentException("Số lượng cơ sở hoàn kho không được âm");
            }
            if (ct.getTienHoanLai() == null || ct.getTienHoanLai().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Tiền hoàn của chi tiết không được âm");
            }
            giaTriTra = giaTriTra.add(ct.getTienHoanLai());
        }
        if (pdt.getGiaTriHangTra() != null
                && pdt.getGiaTriHangTra().compareTo(BigDecimal.ZERO) > 0
                && pdt.getGiaTriHangTra().compareTo(giaTriTra) != 0) {
            throw new IllegalArgumentException("Giá trị hàng trả không khớp tổng chi tiết đổi trả");
        }
        pdt.setGiaTriHangTra(giaTriTra);
        BigDecimal giaTriDoi = pdt.getGiaTriHangDoi() != null ? pdt.getGiaTriHangDoi() : BigDecimal.ZERO;
        if (giaTriDoi.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị hàng đổi không được âm");
        }
        pdt.setGiaTriHangDoi(giaTriDoi);

        BigDecimal chenhLech = pdt.getGiaTriHangTra().subtract(giaTriDoi);
        BigDecimal tienHoanThucTe = BigDecimal.ZERO;
        BigDecimal tienKhachTraThem = BigDecimal.ZERO;
        if (chenhLech.compareTo(BigDecimal.ZERO) > 0) {
            tienHoanThucTe = chenhLech;
        } else if (chenhLech.compareTo(BigDecimal.ZERO) < 0) {
            tienKhachTraThem = chenhLech.negate();
        }
        pdt.setTienHoanThucTe(tienHoanThucTe);
        pdt.setTienKhachTraThem(tienKhachTraThem);
        pdt.setTongTienHoan(tienHoanThucTe);

        pdt.setPhuongThucHoanTien(TaiChinhHoaDonBus.chuanHoaPhuongThucHoan(
                pdt.getPhuongThucHoanTien(), tienHoanThucTe.compareTo(BigDecimal.ZERO) > 0));

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

    // note lại: tính tổng tiền hoàn cho phiếu đổi trả, tách ra khỏi entity PhieuDoiTra
    public static java.math.BigDecimal tinhTongTienHoan(PhieuDoiTra pdt) {
        if (pdt == null || pdt.getDsChiTietDoiTra() == null) return java.math.BigDecimal.ZERO;
        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
        for (entity.ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
            if (ct != null && ct.getTienHoanLai() != null) {
                tong = tong.add(ct.getTienHoanLai());
            }
        }
        pdt.setTongTienHoan(tong);
        return tong;
    }
}
