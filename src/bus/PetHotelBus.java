package bus;

import dao.PhongLuuTruDAO;
import gui.PetHotelPanel.PhongLuuTru;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối quản lý Khách sạn Thú cưng (Pet Hotel), đặt phòng lưu trú và cập nhật trạng thái phòng.
 * Tách biệt hoàn toàn tầng GUI khỏi PhongLuuTruDAO.
 */
public class PetHotelBus {

    private final PhongLuuTruDAO phongLuuTruDAO;

    public PetHotelBus() {
        this.phongLuuTruDAO = new PhongLuuTruDAO();
    }

    /**
     * Lấy toàn bộ danh sách phòng lưu trú từ CSDL.
     */
    public List<PhongLuuTru> layDanhSachPhong() {
        try {
            return phongLuuTruDAO.getAllPhong();
        } catch (SQLException e) {
            System.err.println("[PetHotelBus] Lỗi layDanhSachPhong: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Cập nhật thông tin và trạng thái phòng lưu trú (check-in / check-out / dọn dẹp).
     */
    public boolean capNhatPhong(PhongLuuTru phong) {
        if (phong == null) return false;
        try {
            return phongLuuTruDAO.updatePhong(phong);
        } catch (SQLException e) {
            System.err.println("[PetHotelBus] Lỗi capNhatPhong: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lưu lịch sử lưu trú vào CSDL khi khách làm thủ tục Check-out.
     */
    public boolean luuLichSuLuuTru(PhongLuuTru p, long soNgay, long tongTien) {
        if (p == null) return false;
        try {
            return phongLuuTruDAO.insertLichSuLuuTru(
                    p.maPhong, p.tenThuCung, p.tenChu, p.sdtChu,
                    p.thoiGianCheckIn, java.time.LocalDateTime.now(),
                    soNgay, p.donGiaNgay, p.phiDichVuPhatSinh, tongTien, p.ghiChuChamSoc
            );
        } catch (SQLException e) {
            System.err.println("[PetHotelBus] Lỗi luuLichSuLuuTru CSDL: " + e.getMessage());
            return false;
        }
    }

    public boolean hoanTatCheckOutDaThanhToan(PhongLuuTru p, long soNgay, long tongTien, String maHoaDon) {
        if (p == null || maHoaDon == null || maHoaDon.isBlank()) return false;
        try {
            return phongLuuTruDAO.hoanTatCheckOutDaThanhToan(p, soNgay, tongTien, maHoaDon);
        } catch (SQLException e) {
            System.err.println("[PetHotelBus] Lỗi hoàn tất checkout: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đếm số phòng đang có thú cưng lưu trú (phục vụ KPI và thống kê).
     */
    public int demSoPhongDangLuuTru() {
        int count = 0;
        for (PhongLuuTru p : layDanhSachPhong()) {
            if (p != null && "ĐANG LƯU TRÚ".equalsIgnoreCase(p.trangThai)) {
                count++;
            }
        }
        return count;
    }
}
