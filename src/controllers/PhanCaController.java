package controllers;

import models.entity.BangPhanCong;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import utils.UserSession;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller quản lý Phân công ca làm việc & Ma trận lịch tuần.
 * Chuẩn hóa 4 ca làm việc trong ngày và lưu vết phân công kèm ghi chú ca.
 */
public class PhanCaController {

    private final NhanVienController nhanVienController;
    private final models.dao.PhanCaDAO phanCaDAO = new models.dao.PhanCaDAO();
    private final List<CaLamViec> danhSachCa = new ArrayList<>();
    private final List<BangPhanCong> danhSachPhanCong = new ArrayList<>();

    public PhanCaController() {
        this.nhanVienController = new NhanVienController();
        khoiTaoCaLamViec();
        dongBoPhanCongTuDatabase();
    }

    private void khoiTaoCaLamViec() {
        try {
            List<CaLamViec> dbCa = phanCaDAO.getAllCaLamViec();
            if (dbCa != null && !dbCa.isEmpty()) {
                danhSachCa.addAll(dbCa);
                return;
            }
        } catch (Exception ignored) {}
        danhSachCa.add(new CaLamViec("CA01", "Ca 1 (07:00 - 11:30)", LocalTime.of(7, 0), LocalTime.of(11, 30)));
        danhSachCa.add(new CaLamViec("CA02", "Ca 2 (11:30 - 15:30)", LocalTime.of(11, 30), LocalTime.of(15, 30)));
        danhSachCa.add(new CaLamViec("CA03", "Ca 3 (15:30 - 19:30)", LocalTime.of(15, 30), LocalTime.of(19, 30)));
        danhSachCa.add(new CaLamViec("CA04", "Ca 4 (19:30 - 22:00)", LocalTime.of(19, 30), LocalTime.of(22, 0)));
    }

    public void dongBoPhanCongTuDatabase() {
        try {
            List<BangPhanCong> dbList = phanCaDAO.getAllPhanCong();
            if (dbList != null && !dbList.isEmpty()) {
                danhSachPhanCong.clear();
                danhSachPhanCong.addAll(dbList);
                return;
            }
            khoiTaoDuLieuPhanCongMau();
            for (BangPhanCong pc : danhSachPhanCong) {
                try { phanCaDAO.insertPhanCong(pc); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            if (danhSachPhanCong.isEmpty()) {
                khoiTaoDuLieuPhanCongMau();
            }
        }
    }

    private void khoiTaoDuLieuPhanCongMau() {
        List<NhanVien> dsNV = nhanVienController.layDanhSachNhanVien();
        if (dsNV.isEmpty()) return;

        LocalDate today = LocalDate.now();
        LocalDate t2 = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        CaLamViec ca1 = danhSachCa.get(0);
        CaLamViec ca2 = danhSachCa.get(1);
        CaLamViec ca3 = danhSachCa.get(2);
        CaLamViec ca4 = danhSachCa.get(3);

        int id = 1000;
        for (int w = -1; w <= 1; w++) {
            LocalDate mondayOfWeek = t2.plusWeeks(w);
            for (NhanVien nv : dsNV) {
                String role = nv.getTaiKhoan() != null ? nv.getTaiKhoan().getVaiTro() : "NVBH";

                for (int i = 0; i < 7; i++) {
                    LocalDate date = mondayOfWeek.plusDays(i);

                    if ("NVQL".equals(role)) {
                        if (i < 6) { // T2 -> T7
                            danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca1, date, "Đang làm việc", "Điều hành chung & Duyệt chốt ca"));
                        }
                    } else if ("NVTN".equals(role)) {
                        if (i % 2 == 0) {
                            danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca1, date, "Chưa bắt đầu", "Quầy thu ngân 1 & Nhận bàn giao"));
                        } else if (i != 6) {
                            danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca2, date, "Chưa bắt đầu", "Quầy thu ngân 1 & Đối soát"));
                        }
                    } else if ("NVCS".equals(role)) {
                        if (nv.getMaNhanVien().equals("NV003")) { // Tô Thanh Đạt
                            if (i != 2) { // Nghỉ thứ 4
                                danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca2, date, "Chưa bắt đầu", "Nhận 4 bé Cắt tỉa tạo kiểu"));
                            }
                        } else { // Võ Thị Hồng Ngọc
                            if (i != 4) { // Nghỉ thứ 6
                                danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca1, date, "Chưa bắt đầu", "Spa bồn sục Ozone & Tắm dưỡng"));
                            }
                        }
                    } else { // NVBH
                        if (nv.getMaNhanVien().equals("NV004")) {
                            if (i != 0) { // Nghỉ thứ 2
                                danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca3, date, "Chưa bắt đầu", "Tư vấn thức ăn & phụ kiện"));
                            }
                        } else {
                            if (i != 3) { // Nghỉ thứ 5
                                danhSachPhanCong.add(new BangPhanCong("PC" + (++id), nv, ca4, date, "Chưa bắt đầu", "Trực ca tối & Sắp xếp kệ hàng"));
                            }
                        }
                    }
                }
            }
        }
    }

    public List<CaLamViec> layDanhSachCaLamViec() {
        return new ArrayList<>(danhSachCa);
    }

    public CaLamViec timCaTheoMa(String maCa) {
        for (CaLamViec ca : danhSachCa) {
            if (ca.getMaCa().equalsIgnoreCase(maCa)) return ca;
        }
        return null;
    }

    public List<BangPhanCong> layPhanCongTrongTuan(LocalDate monday) {
        LocalDate sunday = monday.plusDays(6);
        List<BangPhanCong> res = new ArrayList<>();
        for (BangPhanCong pc : danhSachPhanCong) {
            if (!pc.getNgayLamViec().isBefore(monday) && !pc.getNgayLamViec().isAfter(sunday)) {
                res.add(pc);
            }
        }
        return res;
    }

    public List<BangPhanCong> layPhanCongTheoNhanVien(String maNV, LocalDate monday) {
        LocalDate sunday = monday.plusDays(6);
        List<BangPhanCong> res = new ArrayList<>();
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getNhanVien() != null && pc.getNhanVien().getMaNhanVien().equalsIgnoreCase(maNV)) {
                if (!pc.getNgayLamViec().isBefore(monday) && !pc.getNgayLamViec().isAfter(sunday)) {
                    res.add(pc);
                }
            }
        }
        return res;
    }

    public BangPhanCong timPhanCong(String maNV, LocalDate ngay) {
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getNhanVien() != null && pc.getNhanVien().getMaNhanVien().equalsIgnoreCase(maNV) &&
                    pc.getNgayLamViec().equals(ngay)) {
                return pc;
            }
        }
        return null;
    }

    /** Tìm đúng phân công của một nhân viên trong một ngày và một ca cụ thể. */
    public BangPhanCong timPhanCong(String maNV, LocalDate ngay, String maCa) {
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getNhanVien() != null && pc.getCaLamViec() != null
                    && pc.getNhanVien().getMaNhanVien().equalsIgnoreCase(maNV)
                    && pc.getNgayLamViec().equals(ngay)
                    && pc.getCaLamViec().getMaCa().equalsIgnoreCase(maCa)) {
                return pc;
            }
        }
        return null;
    }

    /** Lấy toàn bộ ca của một nhân viên trong một ngày (tối đa 2 khi tăng ca). */
    public List<BangPhanCong> layPhanCongTheoNhanVienVaNgay(String maNV, LocalDate ngay) {
        List<BangPhanCong> result = new ArrayList<>();
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getNhanVien() != null
                    && pc.getNhanVien().getMaNhanVien().equalsIgnoreCase(maNV)
                    && pc.getNgayLamViec().equals(ngay)) {
                result.add(pc);
            }
        }
        return result;
    }

    /** Chỉ cho phép tăng ca khi hai ca nằm liền nhau trong danh mục CA01..CA04. */
    public boolean laHaiCaLienTiep(CaLamViec caA, CaLamViec caB) {
        if (caA == null || caB == null) return false;
        int idxA = -1;
        int idxB = -1;
        for (int i = 0; i < danhSachCa.size(); i++) {
            String maCa = danhSachCa.get(i).getMaCa();
            if (maCa.equalsIgnoreCase(caA.getMaCa())) idxA = i;
            if (maCa.equalsIgnoreCase(caB.getMaCa())) idxB = i;
        }
        return idxA >= 0 && idxB >= 0 && Math.abs(idxA - idxB) == 1;
    }

    /**
     * Xếp ca mới hoặc đổi ca cho nhân viên tại một ngày cụ thể
     */
    public boolean xepCaLamViec(NhanVien nv, CaLamViec ca, LocalDate ngay, String ghiChu) {
        if (!UserSession.isQuanLy() || nv == null || ngay == null) return false;

        if (ca == null) {
            BangPhanCong tonTai = timPhanCong(nv.getMaNhanVien(), ngay);
            if (tonTai == null) return false;
            try {
                if (!phanCaDAO.deletePhanCong(tonTai.getMaPhanCong())) return false;
                return danhSachPhanCong.remove(tonTai);
            } catch (Exception e) {
                System.err.println("[PhanCaController] Lỗi xóa phân công: " + e.getMessage());
                return false;
            }
        }

        BangPhanCong trungCa = timPhanCong(nv.getMaNhanVien(), ngay, ca.getMaCa());
        if (trungCa != null) {
            String ghiChuMoi = ghiChu != null ? ghiChu : "";
            try {
                if (!phanCaDAO.updateGhiChu(trungCa.getMaPhanCong(), ghiChuMoi)) return false;
                trungCa.setGhiChu(ghiChuMoi);
                return true;
            } catch (Exception e) {
                System.err.println("[PhanCaController] Lỗi cập nhật nhiệm vụ: " + e.getMessage());
                return false;
            }
        }

        List<BangPhanCong> cacCaTrongNgay = layPhanCongTheoNhanVienVaNgay(nv.getMaNhanVien(), ngay);
        if (!cacCaTrongNgay.isEmpty()) {
            if (!UserSession.isQuanLy() || cacCaTrongNgay.size() >= 2
                    || !laHaiCaLienTiep(cacCaTrongNgay.get(0).getCaLamViec(), ca)) {
                return false;
            }
        }

        String maPC = "PC" + System.currentTimeMillis();
        BangPhanCong pcMoi = new BangPhanCong(maPC, nv, ca, ngay, "Chưa bắt đầu", ghiChu != null ? ghiChu : "");
        try {
            if (!phanCaDAO.insertPhanCong(pcMoi)) return false;
            danhSachPhanCong.add(pcMoi);
            return true;
        } catch (Exception e) {
            System.err.println("[PhanCaController] Lỗi thêm phân công: " + e.getMessage());
            return false;
        }
    }

    public List<BangPhanCong> layPhanCongTheoCaVaNgay(String maCa, LocalDate ngay) {
        List<BangPhanCong> list = new ArrayList<>();
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getCaLamViec() != null && pc.getCaLamViec().getMaCa().equalsIgnoreCase(maCa) &&
                    pc.getNgayLamViec().equals(ngay)) {
                list.add(pc);
            }
        }
        return list;
    }

    public BangPhanCong timPhanCongTheoMa(String maPC) {
        for (BangPhanCong pc : danhSachPhanCong) {
            if (pc.getMaPhanCong().equalsIgnoreCase(maPC)) {
                return pc;
            }
        }
        return null;
    }

    public boolean xoaPhanCong(String maPC) {
        BangPhanCong pc = timPhanCongTheoMa(maPC);
        if (pc != null) {
            try { phanCaDAO.deletePhanCong(maPC); } catch (Exception ignored) {}
            return danhSachPhanCong.remove(pc);
        }
        return false;
    }

    public boolean doiCaPhanCong(String maPC, CaLamViec caMoi) {
        BangPhanCong pc = timPhanCongTheoMa(maPC);
        if (pc != null && caMoi != null) {
            List<BangPhanCong> cacCaTrongNgay = layPhanCongTheoNhanVienVaNgay(
                    pc.getNhanVien().getMaNhanVien(), pc.getNgayLamViec());
            for (BangPhanCong caKhac : cacCaTrongNgay) {
                if (caKhac.getMaPhanCong().equalsIgnoreCase(maPC)) continue;
                if (caKhac.getCaLamViec() != null
                        && (caKhac.getCaLamViec().getMaCa().equalsIgnoreCase(caMoi.getMaCa())
                        || !laHaiCaLienTiep(caKhac.getCaLamViec(), caMoi))) {
                    return false;
                }
            }
            try {
                if (!phanCaDAO.updateCaPhanCong(maPC, caMoi.getMaCa())) return false;
                pc.setCaLamViec(caMoi);
                return true;
            } catch (Exception e) {
                System.err.println("[PhanCaController] Lỗi đổi ca: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean capNhatGhiChuPhanCong(String maPC, String ghiChu) {
        BangPhanCong pc = timPhanCongTheoMa(maPC);
        if (pc != null) {
            pc.setGhiChu(ghiChu != null ? ghiChu : "");
            try { phanCaDAO.updateGhiChu(maPC, pc.getGhiChu()); } catch (Exception ignored) {}
            return true;
        }
        return false;
    }

    public NhanVienController getNhanVienController() {
        return nhanVienController;
    }
}
