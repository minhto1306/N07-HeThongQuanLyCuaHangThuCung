package bus;

import dao.TheSpaPassDAO;
import entity.TheSpaPass;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Quản lý Thẻ Gói Spa Pass (Punch Card).
 * Quản lý tra cứu theo SĐT khách, kích hoạt thẻ mới khi mua tại POS, và trừ số buổi khi sử dụng.
 * Lưu trữ bền vững xuống CSDL SQL Server qua TheSpaPassDAO.
 */
public class SpaPassBus {
    private final dao.DichVuDAO dichVuDAO = new dao.DichVuDAO();

    private static final List<TheSpaPass> DANH_SACH_THE = new ArrayList<>();
    private static final TheSpaPassDAO dao = new TheSpaPassDAO();

    static {
        dongBoDuLieuTuDatabase();
    }

    public static synchronized void dongBoDuLieuTuDatabase() {
        try {
            List<TheSpaPass> dbList = dao.getAllTheSpaPass();
            if (dbList != null && !dbList.isEmpty()) {
                DANH_SACH_THE.clear();
                DANH_SACH_THE.addAll(dbList);
                return;
            }
        } catch (Exception e) {
            System.err.println("[SpaPassBus] Cảnh báo kết nối CSDL TheSpaPass: " + e.getMessage());
        }

        if (DANH_SACH_THE.isEmpty()) {
            khoiTaoDuLieuMacDinh();
        }
    }

    private static void khoiTaoDuLieuMacDinh() {
        // Khách quen Nguyễn Văn An (0988111222): Có gói Spa 5 buổi, đã dùng 2, còn 3 buổi
        TheSpaPass the1 = new TheSpaPass(
                "PASS_0988111222_01",
                "0988111222",
                "Nguyễn Văn An",
                "Gói Spa Chăm Sóc Thư Giãn (5 Buổi)",
                5,
                2,
                900000L,
                LocalDate.now().minusDays(20),
                LocalDate.now().plusDays(70)
        );
        DANH_SACH_THE.add(the1);
        try { dao.insertTheSpaPass(the1); } catch (Exception ignored) {}

        // Khách Trần Thị Mai (0977333444): Có gói Spa 10 buổi, đã dùng 8, còn 2 buổi
        TheSpaPass the2 = new TheSpaPass(
                "PASS_0977333444_01",
                "0977333444",
                "Trần Thị Mai",
                "Gói Spa Toàn Diện & Cắt Tỉa (10 Buổi)",
                10,
                8,
                1600000L,
                LocalDate.now().minusDays(45),
                LocalDate.now().plusDays(45)
        );
        DANH_SACH_THE.add(the2);
        try { dao.insertTheSpaPass(the2); } catch (Exception ignored) {}
    }

    /**
     * Tra cứu Thẻ Spa Pass còn hiệu lực của khách hàng theo Số điện thoại.
     */
    public TheSpaPass timTheKhaDung(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String cleanSdt = sdt.trim();
        for (TheSpaPass the : DANH_SACH_THE) {
            if (the.getSdtKhachHang().equals(cleanSdt) && the.coTheSuDung()) {
                return the;
            }
        }
        try {
            TheSpaPass fromDb = dao.getTheSpaPassBySdt(cleanSdt);
            if (fromDb != null && fromDb.coTheSuDung()) {
                DANH_SACH_THE.removeIf(t -> t.getMaThe().equalsIgnoreCase(fromDb.getMaThe()));
                DANH_SACH_THE.add(0, fromDb);
                return fromDb;
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Sử dụng 1 buổi trong Thẻ Spa Pass của khách hàng.
     */
    public boolean suDungMotBuoi(String sdt) {
        TheSpaPass the = timTheKhaDung(sdt);
        if (the != null) {
            boolean ok = the.dungMotBuoi();
            if (ok) {
                try { dao.updateTheSpaPass(the); } catch (Exception e) {
                    System.err.println("[SpaPassBus] Lỗi cập nhật TheSpaPass CSDL: " + e.getMessage());
                }
            }
            return ok;
        }
        return false;
    }

    public boolean suDungMotBuoi(String maTheOrSdt, String maHD, String ghiChu) {
        if (maTheOrSdt == null) return false;
        for (TheSpaPass the : DANH_SACH_THE) {
            if ((maTheOrSdt.equalsIgnoreCase(the.getMaThe()) || maTheOrSdt.equalsIgnoreCase(the.getSdtKhachHang())) && the.coTheSuDung()) {
                boolean ok = the.dungMotBuoi();
                if (ok) {
                    try { dao.updateTheSpaPass(the); } catch (Exception e) {
                        System.err.println("[SpaPassBus] Lỗi cập nhật TheSpaPass CSDL: " + e.getMessage());
                    }
                }
                return ok;
            }
        }
        return false;
    }

    /**
     * Kích hoạt Thẻ Spa Pass mới khi khách hàng mua gói tại quầy POS.
     */
    public TheSpaPass kichHoatTheMoi(String sdt, String tenKhach, String maGoi, int soBuoi, long donGia) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String tenGoi = "Gói Spa Chăm Sóc (" + soBuoi + " Buổi)";
        String maThe = dao.sinhMaTheSpaPass(sdt);
        TheSpaPass theMoi = new TheSpaPass(
                maThe,
                sdt.trim(),
                tenKhach != null ? tenKhach : "Khách hàng",
                tenGoi,
                soBuoi,
                0,
                donGia,
                LocalDate.now(),
                LocalDate.now().plusDays(soBuoi > 5 ? 120 : 90)
        );
        DANH_SACH_THE.add(0, theMoi);
        try {
            dao.insertTheSpaPass(theMoi);
        } catch (Exception e) {
            System.err.println("[SpaPassBus] Lỗi lưu TheSpaPass CSDL: " + e.getMessage());
        }
        return theMoi;
    }

    /**
     * Lấy toàn bộ danh sách thẻ Spa Pass (phục vụ đối soát và báo cáo).
     */
    public List<TheSpaPass> layTatCaThe() {
        return new ArrayList<>(DANH_SACH_THE);
    }

    public TheSpaPass timTheTheoMa(String maThe) {
        if (maThe == null || maThe.trim().isEmpty()) return null;
        for (TheSpaPass the : DANH_SACH_THE) {
            if (maThe.trim().equalsIgnoreCase(the.getMaThe())) {
                return the;
            }
        }
        return null;
    }

    public boolean napThemBuoiChoThe(String maThe, int soBuoi, int soNgayGiaHan) {
        TheSpaPass the = timTheTheoMa(maThe);
        if (the != null) {
            the.napThemBuoi(soBuoi, soNgayGiaHan);
            try {
                dao.updateTheSpaPass(the);
            } catch (Exception e) {
                System.err.println("[SpaPassBus] Lỗi cập nhật nạp thêm TheSpaPass CSDL: " + e.getMessage());
            }
            return true;
        }
        return false;
    }
}
