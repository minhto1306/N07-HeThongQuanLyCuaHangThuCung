package bus;

import dao.LichHenDAO;
import dao.ThuCungDAO;
import entity.*;

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
public class ThuCungBus {

    private final ThuCungDAO thuCungDAO;
    private final LichHenDAO lichHenDAO;

    public ThuCungBus() {
        this.thuCungDAO = new ThuCungDAO();
        this.lichHenDAO = new LichHenDAO();
    }

    // Nạp toàn bộ hồ sơ thú cưng từ CSDL lên, nếu lỗi thì trả về danh sách rỗng để UI không bị NullPointerException
    public List<HoSoThuCung> layDanhSachHoSoThuCung() {
        try {
            List<HoSoThuCung> dbList = thuCungDAO.getAllHoSoThuCung();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi lấy HoSoThuCung từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Tiếp nhận thú cưng mới: kiểm tra chủ nuôi trước, nếu là khách mới toanh chưa có hồ sơ thì tự động tạo nhanh khách hàng rồi mới gắn bé vào
    public boolean themThuCung(ThuCung tc, String maKhachHang, String soMicrochip) {
        try {
            dao.KhachHangDAO khDao = new dao.KhachHangDAO();
            KhachHang kh = khDao.findByMaKhachHang(maKhachHang);
            if (kh == null) {
                String maKhMoi = (maKhachHang != null && !maKhachHang.trim().isEmpty())
                        ? maKhachHang.trim()
                        : utils.MaTuSinhHelper.sinhMaKhachHang();
                kh = new KhachHang(maKhMoi, "Khách hàng " + maKhMoi, "0900000" + maKhMoi.replaceAll("\\D+", ""), 0);
                khDao.insert(kh);
            }
            return thuCungDAO.insertThuCung(tc, kh.getMaKhachHang(), soMicrochip);
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi thêm thú cưng CSDL: " + e.getMessage());
            return false;
        }
    }

    // Tiếp nhận thú cưng mới kèm tra cứu/tạo khách hàng tự động theo SĐT & tên chủ nuôi
    public boolean themThuCungTheoSdtChu(ThuCung tc, String tenChu, String sdtChu, String soMicrochip) {
        try {
            dao.KhachHangDAO khDao = new dao.KhachHangDAO();
            KhachHang kh = null;
            if (sdtChu != null && !sdtChu.trim().isEmpty()) {
                kh = khDao.findBySoDienThoai(sdtChu.trim());
            }
            if (kh == null) {
                String maKhMoi = utils.MaTuSinhHelper.sinhMaKhachHang();
                String hoTen = (tenChu != null && !tenChu.trim().isEmpty()) ? tenChu.trim() : ("Khách hàng " + maKhMoi);
                String sdt = (sdtChu != null && !sdtChu.trim().isEmpty()) ? sdtChu.trim() : ("0900000" + maKhMoi.replaceAll("\\D+", ""));
                kh = new KhachHang(maKhMoi, hoTen, sdt, 0);
                khDao.insert(kh);
            }
            return thuCungDAO.insertThuCung(tc, kh.getMaKhachHang(), soMicrochip);
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi thêm thú cưng theo SĐT chủ: " + e.getMessage());
            return false;
        }
    }

    // Lấy lịch sử khám, tiêm chủng và theo dõi thể trạng định kỳ của các bé cưng
    public List<HoSoSucKhoe> layDanhSachHoSoSucKhoe() {
        try {
            List<HoSoSucKhoe> dbList = thuCungDAO.getAllHoSoSucKhoe();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi lấy HoSoSucKhoe từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Ghi sổ theo dõi sức khỏe: cân nặng, nhiệt độ, lịch tiêm vaccine, triệu chứng bệnh
    public boolean themHoSoSucKhoe(HoSoSucKhoe sk) {
        try {
            // Nếu bé cưng chưa được đăng ký trong CSDL thì tạo trước một mã thú cưng dự phòng
            if (sk.getThuCung() != null && thuCungDAO.getThuCungById(sk.getThuCung().getMaThuCung()) == null) {
                thuCungDAO.insertThuCung(sk.getThuCung(), "KH001", null);
            }
            return thuCungDAO.insertHoSoSucKhoe(sk);
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi thêm HoSoSucKhoe CSDL: " + e.getMessage());
            return false;
        }
    }

    // Lấy toàn bộ danh sách lịch hẹn Spa, Grooming và khám bệnh để hiển thị lên bảng quản lý lịch
    public List<LichHen> layDanhSachLichHen() {
        try {
            List<LichHen> dbList = lichHenDAO.getAllLichHen();
            return dbList != null ? dbList : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi lấy LichHen từ CSDL: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Đặt lịch hẹn mới: kiểm tra số điện thoại của chủ, nếu số đã có trong hệ thống thì liên kết ngay để tích điểm
    public boolean datLichHenMoi(LichHen lh) {
        try {
            dao.KhachHangDAO khDao = new dao.KhachHangDAO();
            KhachHang kh = lh.getKhachHang();
            if (kh != null) {
                // Ưu tiên tìm khách hàng theo số điện thoại trước
                KhachHang bySdt = khDao.findBySoDienThoai(kh.getSoDienThoai());
                if (bySdt != null) {
                    lh.setKhachHang(bySdt);
                    kh = bySdt;
                } else if (khDao.findByMaKhachHang(kh.getMaKhachHang()) == null) {
                    // Nếu là khách mới thì lưu thông tin liên hệ vào danh mục khách hàng
                    khDao.insert(kh);
                }
            }
            if (lh.getDsChiTiet() != null) {
                // Kiểm tra từng bé cưng trong phiếu hẹn, đảm bảo bé nào cũng có thông tin định danh
                for (ChiTietLichHen ct : lh.getDsChiTiet()) {
                    ThuCung tc = ct.getThuCung();
                    if (tc != null && thuCungDAO.getThuCungById(tc.getMaThuCung()) == null) {
                        String maKH = kh != null ? kh.getMaKhachHang() : "KH001";
                        thuCungDAO.insertThuCung(tc, maKH, null);
                    }
                }
            }
            return lichHenDAO.insertLichHen(lh);
        } catch (SQLException e) {
            System.err.println("[ThuCungBus] Lỗi đặt lịch hẹn CSDL: " + e.getMessage());
            return false;
        }
    }

    // Cập nhật tiến độ lịch hẹn (Chờ xác nhận -> Đang thực hiện -> Hoàn thành) để bên quầy POS tiện theo dõi thanh toán
    public boolean capNhatTrangThaiLichHen(String maLichHen, String trangThaiMoi) {
        try {
            return lichHenDAO.capNhatTrangThai(maLichHen, trangThaiMoi);
        } catch (Exception e) {
            System.err.println("[ThuCungBus] Lỗi cập nhật trạng thái lịch hẹn CSDL: " + e.getMessage());
            return false;
        }
    }

    // Chỉ định kỹ thuật viên chăm sóc cho dịch vụ của bé, để lát tính đúng 10% hoa hồng trên hóa đơn
    public boolean phanCongKtv(String maChiTietLichHen, String maKtv, String tenKtv) {
        try {
            return lichHenDAO.capNhatKtvChiDinh(maChiTietLichHen, maKtv);
        } catch (Exception e) {
            System.err.println("[ThuCungBus] Lỗi phân công KTV CSDL: " + e.getMessage());
            return false;
        }
    }
}
