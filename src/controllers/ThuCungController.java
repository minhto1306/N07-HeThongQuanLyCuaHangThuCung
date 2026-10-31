package controllers;

import models.dao.LichHenDAO;
import models.dao.ThuCungDAO;
import models.entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối quản lý Thú cưng, Hồ sơ Microchip, Kiểm tra thể trạng & Lịch hẹn dịch vụ.
 * Nạp và lưu trữ 100% dữ liệu thực từ CSDL Microsoft SQL Server qua DAO.
 */
public class ThuCungController {

    private final ThuCungDAO thuCungDAO;
    private final LichHenDAO lichHenDAO;

    public ThuCungController() {
        this.thuCungDAO = new ThuCungDAO();
        this.lichHenDAO = new LichHenDAO();
    }

    public List<HoSoThuCung> layDanhSachHoSoThuCung() {
        try {
            List<HoSoThuCung> dbList = thuCungDAO.getAllHoSoThuCung();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy HoSoThuCung từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean themThuCung(ThuCung tc, String maKhachHang, String soMicrochip) {
        try {
            models.dao.KhachHangDAO khDao = new models.dao.KhachHangDAO();
            KhachHang kh = khDao.findByMaKhachHang(maKhachHang);
            if (kh == null) {
                kh = new KhachHang(maKhachHang, "Khách hàng " + maKhachHang, "09" + (System.currentTimeMillis() % 100000000L), 0);
                khDao.insert(kh);
            }
            return thuCungDAO.insertThuCung(tc, kh.getMaKhachHang(), soMicrochip);
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi thêm thú cưng CSDL: " + e.getMessage());
            return false;
        }
    }

    public List<HoSoSucKhoe> layDanhSachHoSoSucKhoe() {
        try {
            List<HoSoSucKhoe> dbList = thuCungDAO.getAllHoSoSucKhoe();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy HoSoSucKhoe từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean themHoSoSucKhoe(HoSoSucKhoe sk) {
        try {
            if (sk.getThuCung() != null && thuCungDAO.getThuCungById(sk.getThuCung().getMaThuCung()) == null) {
                thuCungDAO.insertThuCung(sk.getThuCung(), "KH001", "MC" + (System.currentTimeMillis() % 100000));
            }
            return thuCungDAO.insertHoSoSucKhoe(sk);
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi thêm HoSoSucKhoe CSDL: " + e.getMessage());
            return false;
        }
    }

    public List<LichHen> layDanhSachLichHen() {
        try {
            List<LichHen> dbList = lichHenDAO.getAllLichHen();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi lấy LichHen từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
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
                        thuCungDAO.insertThuCung(tc, maKH, "MC" + (System.currentTimeMillis() % 100000));
                    }
                }
            }
            return lichHenDAO.insertLichHen(lh);
        } catch (SQLException e) {
            System.err.println("[ThuCungController] Lỗi đặt lịch hẹn CSDL: " + e.getMessage());
            return false;
        }
    }

    public boolean capNhatTrangThaiLichHen(String maLichHen, String trangThaiMoi) {
        try {
            return lichHenDAO.capNhatTrangThai(maLichHen, trangThaiMoi);
        } catch (Exception e) {
            System.err.println("[ThuCungController] Lỗi cập nhật trạng thái lịch hẹn CSDL: " + e.getMessage());
            return false;
        }
    }

    public boolean phanCongKtv(String maChiTietLichHen, String maKtv, String tenKtv) {
        try {
            return lichHenDAO.capNhatKtvChiDinh(maChiTietLichHen, maKtv);
        } catch (Exception e) {
            System.err.println("[ThuCungController] Lỗi phân công KTV CSDL: " + e.getMessage());
            return false;
        }
    }
}
