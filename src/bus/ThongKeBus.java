package bus;

import dao.BaoCaoThongKeDAO;
import dao.BaoCaoThongKeDAO.*;
import dao.HoaDonDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller điều phối toàn bộ nghiệp vụ Thống kê, Báo cáo Doanh thu, Hiệu suất và Dữ liệu Dashboard.
 * Tách biệt hoàn toàn tầng GUI khỏi BaoCaoThongKeDAO.
 */
public class ThongKeBus {

    private final BaoCaoThongKeDAO baoCaoDAO;
    private final HoaDonDAO hoaDonDAO;

    public ThongKeBus() {
        this.baoCaoDAO = new BaoCaoThongKeDAO();
        this.hoaDonDAO = new HoaDonDAO();
    }

    public List<DoanhThuRow> layBaoCaoDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        try {
            return baoCaoDAO.getDoanhThu(tuNgay, denNgay);
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layBaoCaoDoanhThu: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<DoanhThuRow> layBaoCaoDoanhThu(LocalDate tuNgay, LocalDate denNgay) {
        return layBaoCaoDoanhThu(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    public List<TopBanChayRow> layTopBanChay(LocalDateTime tuNgay, LocalDateTime denNgay) {
        try {
            return baoCaoDAO.getTopBanChay(tuNgay, denNgay);
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layTopBanChay: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<TopBanChayRow> layTopBanChay(LocalDate tuNgay, LocalDate denNgay) {
        return layTopBanChay(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    public List<HieuSuatNhanSuRow> layHieuSuatNhanSu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        try {
            return baoCaoDAO.getHieuSuatNhanSu(tuNgay, denNgay);
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layHieuSuatNhanSu: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<HieuSuatNhanSuRow> layHieuSuatNhanSu(LocalDate tuNgay, LocalDate denNgay) {
        return layHieuSuatNhanSu(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    public List<KhachHangRow> layThongKeKhachHang() {
        try {
            return baoCaoDAO.getKhachHang();
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layThongKeKhachHang: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<HotelSummaryRow> layHotelSummary() {
        try {
            return baoCaoDAO.getHotelSummary();
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layHotelSummary: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public DoiTraSummary layDoiTraSummary(LocalDateTime tuNgay, LocalDateTime denNgay) {
        try {
            return baoCaoDAO.getDoiTraSummary(tuNgay, denNgay);
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layDoiTraSummary: " + e.getMessage());
            return new DoiTraSummary();
        }
    }

    public DoiTraSummary layDoiTraSummary(LocalDate tuNgay, LocalDate denNgay) {
        return layDoiTraSummary(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    public List<LoHangFefoRow> layLoHangFefo() {
        try {
            return baoCaoDAO.getLoHangFefo();
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layLoHangFefo: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<XuatNhapTonRow> layXuatNhapTon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        try {
            return baoCaoDAO.getXuatNhapTon(tuNgay, denNgay);
        } catch (SQLException e) {
            System.err.println("[ThongKeBus] Lỗi layXuatNhapTon: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<XuatNhapTonRow> layXuatNhapTon(LocalDate tuNgay, LocalDate denNgay) {
        return layXuatNhapTon(tuNgay.atStartOfDay(), denNgay.plusDays(1).atStartOfDay());
    }

    /**
     * Lấy doanh thu thực tế 7 ngày gần nhất từ CSDL SQL Server (phục vụ BarChart và Dashboard).
     */
    public long[] layDoanhThu7NgayGanNhat() {
        long[] result = new long[7];
        LocalDate today = LocalDate.now();
        LocalDateTime tuNgay = today.minusDays(6).atStartOfDay();
        LocalDateTime denNgay = today.plusDays(1).atStartOfDay();
        List<DoanhThuRow> rows = layBaoCaoDoanhThu(tuNgay, denNgay);

        Map<LocalDate, Long> mapNgayDoanhThu = new HashMap<>();
        for (DoanhThuRow r : rows) {
            if (r.thoiGian != null) {
                LocalDate d = r.thoiGian.toLocalDate();
                mapNgayDoanhThu.put(d, mapNgayDoanhThu.getOrDefault(d, 0L) + r.doanhThuThuan);
            }
        }

        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            result[i] = mapNgayDoanhThu.getOrDefault(d, 0L);
        }
        return result;
    }

    /**
     * Lấy số lượng đơn hàng thực tế 7 ngày gần nhất từ CSDL SQL Server.
     */
    public int[] laySoDon7NgayGanNhat() {
        int[] result = new int[7];
        LocalDate today = LocalDate.now();
        LocalDateTime tuNgay = today.minusDays(6).atStartOfDay();
        LocalDateTime denNgay = today.plusDays(1).atStartOfDay();
        List<DoanhThuRow> rows = layBaoCaoDoanhThu(tuNgay, denNgay);

        Map<LocalDate, Integer> mapNgayDon = new HashMap<>();
        for (DoanhThuRow r : rows) {
            if (r.thoiGian != null) {
                LocalDate d = r.thoiGian.toLocalDate();
                mapNgayDon.put(d, mapNgayDon.getOrDefault(d, 0) + 1);
            }
        }

        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            result[i] = mapNgayDon.getOrDefault(d, 0);
        }
        return result;
    }

    /**
     * Lấy danh sách nhãn thứ/ngày của 7 ngày gần nhất.
     */
    public String[] layNhan7NgayGanNhat() {
        String[] labels = new String[7];
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(6 - i);
            int dayOfWeek = d.getDayOfWeek().getValue();
            String thu = (dayOfWeek == 7) ? "CN" : ("T" + (dayOfWeek + 1));
            labels[i] = thu + " (" + String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue()) + ")";
        }
        return labels;
    }

    /**
     * Lấy tổng doanh thu thuần trong ngày hôm nay.
     */
    public long layDoanhThuHomNay() {
        LocalDate today = LocalDate.now();
        List<DoanhThuRow> rows = layBaoCaoDoanhThu(today, today);
        long sum = 0;
        for (DoanhThuRow r : rows) {
            sum += r.doanhThuThuan;
        }
        return sum;
    }

    /**
     * Lấy tổng số hóa đơn thanh toán trong ngày hôm nay.
     */
    public int laySoDonHangHomNay() {
        LocalDate today = LocalDate.now();
        List<DoanhThuRow> rows = layBaoCaoDoanhThu(today, today);
        return rows.size();
    }
}
