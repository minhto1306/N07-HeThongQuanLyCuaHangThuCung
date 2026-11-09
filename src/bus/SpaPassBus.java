package bus;

import dao.TheSpaPassDAO;
import entity.TheSpaPass;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller Quản lý Thẻ Gói Spa Pass (Punch Card).
 * Quản lý tra cứu theo SĐT khách, kích hoạt thẻ mới khi mua tại POS, và trừ số buổi khi sử dụng.
 * Lưu trữ bền vững xuống CSDL SQL Server qua TheSpaPassDAO.
 */
public class SpaPassBus {

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

        // Không tự tạo thẻ miễn phí khi CSDL rỗng/mất kết nối.
        DANH_SACH_THE.clear();
    }

    /**
     * Tra cứu Thẻ Spa Pass còn hiệu lực của khách hàng theo Số điện thoại.
     */
    public TheSpaPass timTheKhaDung(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) return null;
        String cleanSdt = sdt.trim();
        for (TheSpaPass the : DANH_SACH_THE) {
            if (the.getSdtKhachHang().equals(cleanSdt) && coTheSuDung(the)) {
                return the;
            }
        }
        try {
            TheSpaPass fromDb = dao.getTheSpaPassBySdt(cleanSdt);
            if (fromDb != null && coTheSuDung(fromDb)) {
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
        System.err.println("[SpaPassBus] Từ chối trừ lượt ngoài giao dịch hóa đơn POS");
        return false;
    }

    public boolean suDungMotBuoi(String maTheOrSdt, String maHD, String ghiChu) {
        if (maTheOrSdt == null) return false;
        for (TheSpaPass the : DANH_SACH_THE) {
            if ((maTheOrSdt.equalsIgnoreCase(the.getMaThe()) || maTheOrSdt.equalsIgnoreCase(the.getSdtKhachHang())) && coTheSuDung(the)) {
                try {
                    boolean ok = dao.suDungMotBuoi(the.getMaThe(), maHD,
                            utils.UserSession.getUserId(), ghiChu);
                    if (ok) {
                        dongBoDuLieuTuDatabase();
                        return true;
                    }
                } catch (Exception e) {
                    System.err.println("[SpaPassBus] Lỗi trừ lượt/ghi lịch sử: " + e.getMessage());
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Kích hoạt Thẻ Spa Pass mới khi khách hàng mua gói tại quầy POS.
     */
    public static synchronized TheSpaPass kichHoatTheMoi(String sdt, String tenKhach, String tenGoi, int soBuoi, long donGia) {
        if (sdt == null || !sdt.trim().matches("^0\\d{9}$")) {
            System.err.println("[SpaPassBus] Số điện thoại không hợp lệ: " + sdt);
            return null;
        }
        if (soBuoi <= 0 || donGia <= 0) {
            System.err.println("[SpaPassBus] Số buổi hoặc đơn giá không hợp lệ");
            return null;
        }
        String cleanSdt = sdt.trim();
        int seq = 1;
        for (TheSpaPass t : DANH_SACH_THE) {
            if (cleanSdt.equals(t.getSdtKhachHang())) {
                seq++;
            }
        }
        String maThe = String.format("PASS-%s-%02d", cleanSdt, seq);
        // Kiểm tra tránh trùng khóa trong DB
        try {
            while (dao.findByMaThe(maThe) != null) {
                seq++;
                maThe = String.format("PASS-%s-%02d", cleanSdt, seq);
            }
        } catch (Exception ignored) {}

        TheSpaPass the = new TheSpaPass(
                maThe,
                cleanSdt,
                tenKhach != null ? tenKhach.trim() : "",
                tenGoi,
                soBuoi,
                0,
                donGia,
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusMonths(6)
        );
        the.setSoBuoiConLai(soBuoi);
        the.setSoBuoiDaDung(0);
        the.setTrangThai("ĐANG HOẠT ĐỘNG");
        try {
            boolean ok = dao.insertTheSpaPass(the);
            if (ok) {
                DANH_SACH_THE.add(0, the);
                return the;
            }
        } catch (Exception e) {
            System.err.println("[SpaPassBus] Lỗi lưu TheSpaPass xuống CSDL: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy toàn bộ danh sách thẻ Spa Pass (phục vụ đối soát và báo cáo).
     */
    public static synchronized List<TheSpaPass> layTatCaThe() {
        return new ArrayList<>(DANH_SACH_THE);
    }

    public static synchronized TheSpaPass timTheTheoMa(String maThe) {
        if (maThe == null || maThe.trim().isEmpty()) return null;
        for (TheSpaPass the : DANH_SACH_THE) {
            if (maThe.trim().equalsIgnoreCase(the.getMaThe())) {
                return the;
            }
        }
        return null;
    }

    public static synchronized boolean napThemBuoiChoThe(String maThe, int soBuoi, int soNgayGiaHan) {
        if (maThe == null || maThe.trim().isEmpty()) return false;
        if (soBuoi <= 0 && soNgayGiaHan <= 0) return false;
        TheSpaPass the = timTheTheoMa(maThe);
        if (the == null) return false;

        int oldTongBuoi = the.getTongSoBuoi();
        int oldConLai = the.getSoBuoiConLai();
        java.time.LocalDate oldNgayHetHan = the.getNgayHetHan();
        String oldTrangThai = the.getTrangThai();

        if (soBuoi > 0) {
            the.setTongSoBuoi(the.getTongSoBuoi() + soBuoi);
            the.setSoBuoiConLai(the.getSoBuoiConLai() + soBuoi);
        }
        if (soNgayGiaHan > 0) {
            the.setNgayHetHan(the.getNgayHetHan() != null ? the.getNgayHetHan().plusDays(soNgayGiaHan) : java.time.LocalDate.now().plusDays(soNgayGiaHan));
        }
        capNhatTrangThai(the);

        try {
            boolean ok = dao.updateTheSpaPass(the);
            if (ok) {
                dongBoDuLieuTuDatabase();
                return true;
            }
        } catch (Exception e) {
            System.err.println("[SpaPassBus] Lỗi cập nhật TheSpaPass: " + e.getMessage());
        }

        // Rollback memory if DB update fails
        the.setTongSoBuoi(oldTongBuoi);
        the.setSoBuoiConLai(oldConLai);
        the.setNgayHetHan(oldNgayHetHan);
        the.setTrangThai(oldTrangThai);
        return false;
    }

    // note lai: logic trang thai va kiem tra the con dung duoc khong chuyen tu entity sang BUS
    // entity TheSpaPass chi thuan POJO chua data thoi
    public static void capNhatTrangThai(TheSpaPass the) {
        if (the == null) return;
        if (the.getTongSoBuoi() > 0 && the.getSoBuoiDaDung() > 0) {
            the.setSoBuoiConLai(Math.max(0, the.getTongSoBuoi() - the.getSoBuoiDaDung()));
        }
        if (the.getSoBuoiConLai() <= 0) {
            the.setTrangThai("ĐÃ HẾT BUỔI");
        } else if (the.getNgayHetHan() != null && java.time.LocalDate.now().isAfter(the.getNgayHetHan())) {
            the.setTrangThai("HẾT HẠN SỬ DỤNG");
        } else {
            the.setTrangThai("ĐANG HOẠT ĐỘNG");
        }
    }

    public static boolean isKhaDung(TheSpaPass the) {
        return coTheSuDung(the);
    }

    public static boolean coTheSuDung(TheSpaPass the) {
        if (the == null) return false;
        capNhatTrangThai(the);
        return "ĐANG HOẠT ĐỘNG".equalsIgnoreCase(the.getTrangThai()) && the.getSoBuoiConLai() > 0;
    }
}
