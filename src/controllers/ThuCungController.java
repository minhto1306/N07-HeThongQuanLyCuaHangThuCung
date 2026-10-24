package controllers;

import models.dao.LichHenDAO;
import models.dao.ThuCungDAO;
import models.entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller điều phối quản lý Thú cưng, Hồ sơ Microchip, Kiểm tra thể trạng & Lịch hẹn dịch vụ.
 */
public class ThuCungController {

    private final ThuCungDAO thuCungDAO;
    private final LichHenDAO lichHenDAO;

    // Cache dữ liệu mẫu dự phòng khi database chưa được bật
    private final List<HoSoThuCung> mockHoSoThuCung = new ArrayList<>();
    private final List<HoSoSucKhoe> mockHoSoSucKhoe = new ArrayList<>();
    private final List<LichHen> mockLichHen = new ArrayList<>();

    public ThuCungController() {
        this.thuCungDAO = new ThuCungDAO();
        this.lichHenDAO = new LichHenDAO();
        khoiTaoDuLieuMau();
    }

    private void khoiTaoDuLieuMau() {
        KhachHang kh1 = new KhachHang("KH001", "Nguyễn Văn An", "0988111222", 120);
        KhachHang kh2 = new KhachHang("KH002", "Trần Thị Mai", "0977333444", 50);
        KhachHang kh3 = new KhachHang("KH003", "Phạm Hoàng Long", "0918776655", 0);
        KhachHang kh4 = new KhachHang("KH004", "Đỗ Quỳnh Chi", "0909445566", 20);

        ThuCung tc1 = new ThuCung("TC001", "Miu Miu", "Mèo Anh Lông Ngắn", LocalDate.of(2023, 5, 10), "Cái");
        ThuCung tc2 = new ThuCung("TC002", "Lu Lu", "Chó Poodle", LocalDate.of(2022, 11, 20), "Đực");
        ThuCung tc3 = new ThuCung("TC003", "Bông", "Chó Pomeranian", LocalDate.of(2024, 2, 15), "Cái");
        ThuCung tc4 = new ThuCung("TC004", "Moka", "Chó Corgi", LocalDate.of(2023, 9, 8), "Đực");

        mockHoSoThuCung.add(new HoSoThuCung("HS001", kh1, tc1, "MC982001", LocalDate.of(2023, 6, 1), "Đang nuôi"));
        mockHoSoThuCung.add(new HoSoThuCung("HS002", kh2, tc2, "MC982002", LocalDate.of(2023, 1, 15), "Đang nuôi"));
        mockHoSoThuCung.add(new HoSoThuCung("HS003", kh3, tc3, "MC982003", LocalDate.of(2024, 3, 1), "Đang nuôi"));
        mockHoSoThuCung.add(new HoSoThuCung("HS004", kh4, tc4, "MC982004", LocalDate.of(2023, 10, 10), "Đang nuôi"));

        NhanVien ktv1 = new NhanVien("NV003", "Tô Thanh Đạt", "Kỹ thuật viên Grooming", "0923456789", null);
        NhanVien ktv2 = new NhanVien("NV005", "Võ Thị Hồng Ngọc", "Chuyên viên Spa thú cưng", "0945678901", null);

        mockHoSoSucKhoe.add(new HoSoSucKhoe("SK001", tc1, ktv1, new BigDecimal("4.2"), new BigDecimal("38.5"), "Không có", true, "Thể trạng tốt, lông sạch sẽ", LocalDateTime.now().minusDays(2)));
        mockHoSoSucKhoe.add(new HoSoSucKhoe("SK002", tc2, ktv1, new BigDecimal("5.8"), new BigDecimal("38.8"), "Có ít vùng tai", true, "Cần xịt vệ sinh tai trước khi cắt lông", LocalDateTime.now().minusDays(1)));
        mockHoSoSucKhoe.add(new HoSoSucKhoe("SK003", tc3, ktv2, new BigDecimal("3.1"), new BigDecimal("38.6"), "Không có", true, "Lông rối nhẹ vùng cổ, cần gỡ rối trước khi tắm", LocalDateTime.now()));

        DichVu dv1 = new DichVu("DV001", "Tắm sấy khử mùi & Vệ sinh tai", new BigDecimal("150000"), 45);
        DichVu dv2 = new DichVu("DV002", "Cắt tỉa lông tạo kiểu thẩm mỹ", new BigDecimal("300000"), 90);
        DichVu dv3 = new DichVu("DV003", "Tắm dưỡng lông mượt & Trị ve rận", new BigDecimal("220000"), 60);

        LichHen lh1 = new LichHen("LH001", kh1, ktv1, LocalDateTime.now().withHour(9).withMinute(0), "Hoàn thành", "Bé ngoan, hoàn thành tốt");
        lh1.addChiTiet(new ChiTietLichHen("CT001", lh1, tc1, dv1, ktv1));

        LichHen lh2 = new LichHen("LH002", kh2, ktv1, LocalDateTime.now().withHour(10).withMinute(30), "Đang phục vụ", "Cắt kiểu mặt gấu teddy");
        lh2.addChiTiet(new ChiTietLichHen("CT002", lh2, tc2, dv2, ktv1));

        LichHen lh3 = new LichHen("LH003", kh3, ktv2, LocalDateTime.now().plusHours(2), "Đã đặt", "Tắm dưỡng thảo dược phục hồi lông");
        lh3.addChiTiet(new ChiTietLichHen("CT003", lh3, tc3, dv3, ktv2));

        LichHen lh4 = new LichHen("LH004", kh4, ktv1, LocalDateTime.now().plusHours(4), "Đã đặt", "Chủ yêu cầu sấy khô kỹ");
        lh4.addChiTiet(new ChiTietLichHen("CT004", lh4, tc4, dv1, ktv1));

        mockLichHen.add(lh1);
        mockLichHen.add(lh2);
        mockLichHen.add(lh3);
        mockLichHen.add(lh4);
    }

    public List<HoSoThuCung> layDanhSachHoSoThuCung() {
        try {
            List<HoSoThuCung> dbList = thuCungDAO.getAllHoSoThuCung();
            if (dbList != null && !dbList.isEmpty()) {
                mockHoSoThuCung.clear();
                mockHoSoThuCung.addAll(dbList);
                return mockHoSoThuCung;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy HoSoThuCung: " + e.getMessage());
        }
        return mockHoSoThuCung;
    }

    public boolean themThuCung(ThuCung tc, String maKhachHang, String soMicrochip) {
        try {
            models.dao.KhachHangDAO khDao = new models.dao.KhachHangDAO();
            KhachHang kh = khDao.findByMaKhachHang(maKhachHang);
            if (kh == null) {
                kh = new KhachHang(maKhachHang, "Khách hàng " + maKhachHang, "09" + (System.currentTimeMillis() % 100000000L), 0);
                khDao.insert(kh);
            }
            if (thuCungDAO.insertThuCung(tc, kh.getMaKhachHang(), soMicrochip)) {
                layDanhSachHoSoThuCung();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi thêm thú cưng CSDL: " + e.getMessage());
        }
        KhachHang kh = new KhachHang(maKhachHang, "Khách hàng " + maKhachHang, "0900000000", 0);
        mockHoSoThuCung.add(0, new HoSoThuCung("HS" + System.currentTimeMillis() % 100000, kh, tc, soMicrochip, LocalDate.now(), "Đang nuôi"));
        return true;
    }

    public List<HoSoSucKhoe> layDanhSachHoSoSucKhoe() {
        try {
            List<HoSoSucKhoe> dbList = thuCungDAO.getAllHoSoSucKhoe();
            if (dbList != null && !dbList.isEmpty()) {
                mockHoSoSucKhoe.clear();
                mockHoSoSucKhoe.addAll(dbList);
                return mockHoSoSucKhoe;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy HoSoSucKhoe: " + e.getMessage());
        }
        return mockHoSoSucKhoe;
    }

    public boolean themHoSoSucKhoe(HoSoSucKhoe sk) {
        try {
            if (sk.getThuCung() != null && thuCungDAO.getThuCungById(sk.getThuCung().getMaThuCung()) == null) {
                thuCungDAO.insertThuCung(sk.getThuCung(), "KH001", "MC" + System.currentTimeMillis() % 100000);
            }
            if (thuCungDAO.insertHoSoSucKhoe(sk)) {
                layDanhSachHoSoSucKhoe();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi thêm HoSoSucKhoe CSDL: " + e.getMessage());
        }
        mockHoSoSucKhoe.add(0, sk);
        return true;
    }

    public List<LichHen> layDanhSachLichHen() {
        try {
            List<LichHen> dbList = lichHenDAO.getAllLichHen();
            if (dbList != null && !dbList.isEmpty()) {
                mockLichHen.clear();
                mockLichHen.addAll(dbList);
                return mockLichHen;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy LichHen: " + e.getMessage());
        }
        return mockLichHen;
    }

    public boolean datLichHenMoi(LichHen lh) {
        try {
            models.dao.KhachHangDAO khDao = new models.dao.KhachHangDAO();
            KhachHang kh = lh.getKhachHang();
            if (kh != null) {
                KhachHang bySdt = khDao.findBySoDienThoai(kh.getSoDienThoai());
                if (bySdt != null) {
                    lh.setKhachHang(bySdt);
                    kh = bySdt;
                } else if (khDao.findByMaKhachHang(kh.getMaKhachHang()) == null) {
                    khDao.insert(kh);
                }
            }
            if (lh.getDsChiTiet() != null) {
                for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                    ThuCung tc = ct.getThuCung();
                    if (tc != null && thuCungDAO.getThuCungById(tc.getMaThuCung()) == null) {
                        String maKH = kh != null ? kh.getMaKhachHang() : "KH001";
                        thuCungDAO.insertThuCung(tc, maKH, "MC" + System.currentTimeMillis() % 100000);
                    }
                }
            }
            if (lichHenDAO.insertLichHen(lh)) {
                layDanhSachLichHen();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi đặt lịch hẹn CSDL: " + e.getMessage());
        }
        mockLichHen.add(0, lh);
        return true;
    }

    public boolean capNhatTrangThaiLichHen(String maLichHen, String trangThaiMoi) {
        try {
            boolean ok = lichHenDAO.capNhatTrangThai(maLichHen, trangThaiMoi);
            if (ok) {
                layDanhSachLichHen();
                return true;
            }
        } catch (Exception ignored) {
        }
        for (LichHen lh : mockLichHen) {
            if (lh.getMaLichHen().equals(maLichHen)) {
                lh.setTrangThaiLichHen(trangThaiMoi);
                return true;
            }
        }
        return false;
    }

    public boolean phanCongKtv(String maChiTietLichHen, String maKtv, String tenKtv) {
        try {
            boolean ok = lichHenDAO.capNhatKtvChiDinh(maChiTietLichHen, maKtv);
            if (ok) {
                layDanhSachLichHen();
                return true;
            }
        } catch (Exception ignored) {
        }
        for (LichHen lh : mockLichHen) {
            for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                if (ct.getMaChiTietLichHen().equals(maChiTietLichHen)) {
                    ct.setKtvChiDinh(new NhanVien(maKtv, tenKtv, "Kỹ thuật viên", "", null));
                    return true;
                }
            }
        }
        return false;
    }
}
