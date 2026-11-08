package bus;

import dao.BangGiaoCaDAO;
import entity.BangGiaoCa;
import entity.CaLamViec;
import entity.NhanVien;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Controller điều phối nghiệp vụ Mở ca, Chốt ca, Bàn giao két tiền và Đối soát doanh thu ca làm việc.
 * Tách 100% tầng GUI khỏi BangGiaoCaDAO.
 */
public class GiaoCaBus {

    private final BangGiaoCaDAO bangGiaoCaDAO;
    private BangGiaoCa caHienTai;

    public GiaoCaBus() {
        this.bangGiaoCaDAO = new BangGiaoCaDAO();
    }

    /**
     * Lấy ca bàn giao gần nhất trong hệ thống.
     */
    public BangGiaoCa layCaBanGiaoGanNhat() {
        try {
            return bangGiaoCaDAO.getCaBanGiaoGanNhat();
        } catch (SQLException e) {
            System.err.println("[GiaoCaBus] Lỗi layCaBanGiaoGanNhat: " + e.getMessage());
            return null;
        }
    }

    /**
     * Tính tổng doanh thu tiền mặt thu được từ mốc thời gian chỉ định.
     */
    public long tinhDoanhThuTienMatTuThoiDiem(LocalDateTime mocThoiGian) {
        try {
            return bangGiaoCaDAO.tinhDoanhThuTienMatTuThoiDiem(mocThoiGian);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi tinhDoanhThuTienMat: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Tính tổng doanh thu chuyển khoản từ mốc thời gian chỉ định.
     */
    public long tinhDoanhThuChuyenKhoanTuThoiDiem(LocalDateTime mocThoiGian) {
        try {
            return bangGiaoCaDAO.tinhDoanhThuChuyenKhoanTuThoiDiem(mocThoiGian);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi tinhDoanhThuChuyenKhoan: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Tính tổng số tiền hoàn trả cho khách do đổi trả hàng từ mốc thời gian chỉ định.
     */
    public long tinhTongTienHoanTraTuThoiDiem(LocalDateTime mocThoiGian) {
        try {
            return bangGiaoCaDAO.tinhTongTienHoanTraTuThoiDiem(mocThoiGian);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi tinhTongTienHoanTra: " + e.getMessage());
            return 0L;
        }
    }

    public long tinhDoanhThuTrongCa(LocalDateTime batDau, LocalDateTime ketThuc,
                                    String maNhanVien, boolean tienMat) {
        return bangGiaoCaDAO.tinhDoanhThuTrongCa(batDau, ketThuc, maNhanVien, tienMat);
    }

    public long tinhHoanTraTrongCa(LocalDateTime batDau, LocalDateTime ketThuc, String maNhanVien) {
        return bangGiaoCaDAO.tinhHoanTraTrongCa(batDau, ketThuc, maNhanVien);
    }

    /**
     * Lấy toàn bộ lịch sử các ca bàn giao.
     */
    public List<BangGiaoCa> layToanBoLichSuGiaoCa() {
        try {
            return bangGiaoCaDAO.getAllBangGiaoCa();
        } catch (SQLException e) {
            System.err.println("[GiaoCaBus] Lỗi layToanBoLichSuGiaoCa: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Phát sinh mã giao ca tự động tăng (GCxxx).
     */
    public String phatSinhMaGiaoCa() {
        return bangGiaoCaDAO.phatSinhMaGiaoCa();
    }

    /**
     * Thêm mới bản ghi bàn giao ca vào CSDL.
     */
    public boolean luuBangGiaoCa(BangGiaoCa bgc) {
        try {
            return bangGiaoCaDAO.insertBangGiaoCa(bgc);
        } catch (SQLException e) {
            System.err.println("[GiaoCaBus] Lỗi luuBangGiaoCa: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách các ca chờ Quản lý duyệt két.
     */
    public List<BangGiaoCa> layDanhSachCaChoDuyet() {
        try {
            return bangGiaoCaDAO.getDanhSachCaChoDuyet();
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi layDanhSachCaChoDuyet: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy chi tiết thông tin một ca theo mã giao ca.
     */
    public BangGiaoCa layBangGiaoCaTheoMa(String maGC) {
        try {
            return bangGiaoCaDAO.getBangGiaoCaTheoMa(maGC);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi layBangGiaoCaTheoMa: " + e.getMessage());
            return null;
        }
    }

    /**
     * Duyệt ca bàn giao két tiền mặt kèm mốc thời gian duyệt.
     */
    public boolean duyetGiaoCa(String maGC, String maQuanLy, LocalDateTime thoiGianDuyet) {
        if (!utils.UserSession.isQuanLy()) {
            throw new SecurityException("Chỉ Quản lý (NVQL) mới có quyền duyệt bàn giao ca!");
        }
        return bangGiaoCaDAO.duyetGiaoCa(maGC, maQuanLy, thoiGianDuyet);
    }

    /**
     * Duyệt ca bàn giao két tiền mặt.
     */
    public boolean duyetGiaoCa(String maGC, String maNguoiDuyet, String ghiChuDuyet) {
        return duyetGiaoCa(maGC, maNguoiDuyet, LocalDateTime.now());
    }

    /**
     * Bắt đầu một ca làm việc mới: Nhập số tiền mặt đầu ca
     */
    public boolean moCa(String maGiaoCa, NhanVien nv, CaLamViec ca, BigDecimal tienDauCa) throws SQLException {
        if (!utils.UserSession.canAccessNhom1GiaoCa()) {
            throw new SecurityException("Người dùng hiện tại không có quyền thực hiện mở ca làm việc!");
        }
        if (tienDauCa == null || tienDauCa.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số tiền đầu ca không được âm!");
        }

        this.caHienTai = new BangGiaoCa(
                maGiaoCa,
                nv,
                ca,
                tienDauCa,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "",
                null
        );

        return bangGiaoCaDAO.moCaLamViec(this.caHienTai);
    }

    /**
     * Chốt ca làm việc cuối ngày: Đối soát tiền mặt lý thuyết vs thực tế
     */
    public boolean chotCa(BigDecimal tienHeThong, BigDecimal tienThucTe, String lyDoChenhLech) throws SQLException {
        if (!utils.UserSession.canAccessNhom1GiaoCa()) {
            throw new SecurityException("Người dùng hiện tại không có quyền thực hiện chốt ca làm việc!");
        }
        if (caHienTai == null) {
            throw new IllegalStateException("Chưa có ca làm việc nào đang mở!");
        }

        caHienTai.setTienCuoiCaHeThong(tienHeThong);
        caHienTai.setTienCuoiCaThucTe(tienThucTe);
        caHienTai.capNhatTienChenhLech();
        caHienTai.setGioKetThucThucTe(LocalDateTime.now());

        if (caHienTai.getTienChenhLech().compareTo(BigDecimal.ZERO) != 0) {
            if (lyDoChenhLech == null || lyDoChenhLech.trim().isEmpty()) {
                throw new IllegalArgumentException("Phát hiện chênh lệch két tiền mặt! Bắt buộc phải nhập lý do giải trình.");
            }
            caHienTai.setLyDoChenhLech(lyDoChenhLech);
        }

        return bangGiaoCaDAO.chotKetCa(this.caHienTai);
    }

    public BangGiaoCa getCaHienTai() {
        return caHienTai;
    }
}
