package controllers;

import models.entity.BangPhanCong;
import models.entity.CaLamViec;
import models.entity.NhanVien;

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

    /**
     * Xếp ca mới hoặc đổi ca cho nhân viên tại một ngày cụ thể
     */
    public boolean xepCaLamViec(NhanVien nv, CaLamViec ca, LocalDate ngay, String ghiChu) {
        BangPhanCong tonTai = timPhanCong(nv.getMaNhanVien(), ngay);
        if (tonTai != null) {
            if (ca == null) {
                try { phanCaDAO.deletePhanCong(tonTai.getMaPhanCong()); } catch (Exception ignored) {}
                danhSachPhanCong.remove(tonTai);
                return true;
            }
            tonTai.setCaLamViec(ca);
            tonTai.setGhiChu(ghiChu != null ? ghiChu : "");
            try {
                phanCaDAO.updateCaPhanCong(tonTai.getMaPhanCong(), ca.getMaCa());
                phanCaDAO.updateGhiChu(tonTai.getMaPhanCong(), tonTai.getGhiChu());
            } catch (Exception ignored) {}
            return true;
        }

        if (ca != null) {
            String maPC = "PC" + (System.currentTimeMillis() % 1000000);
            BangPhanCong pcMoi = new BangPhanCong(maPC, nv, ca, ngay, "Chưa bắt đầu", ghiChu != null ? ghiChu : "");
            try { phanCaDAO.insertPhanCong(pcMoi); } catch (Exception ignored) {}
            danhSachPhanCong.add(pcMoi);
            return true;
        }
        return false;
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
            pc.setCaLamViec(caMoi);
            try { phanCaDAO.updateCaPhanCong(maPC, caMoi.getMaCa()); } catch (Exception ignored) {}
            return true;
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
