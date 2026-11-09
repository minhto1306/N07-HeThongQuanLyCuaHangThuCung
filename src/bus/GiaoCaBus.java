package bus;

import dao.BangGiaoCaDAO;
import entity.BangGiaoCa;
import entity.CaLamViec;
import entity.NhanVien;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
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
            throw new IllegalStateException("Không thể đối soát doanh thu tiền mặt; dừng chốt ca", e);
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
            throw new IllegalStateException("Không thể đối soát doanh thu chuyển khoản; dừng chốt ca", e);
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
            throw new IllegalStateException("Không thể đối soát tiền hoàn trả; dừng chốt ca", e);
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
    /**
     * Thêm mới hoặc cập nhật bản ghi bàn giao ca vào CSDL.
     * Chống lỗi duplicate key UQ_BangGiaoCa_NhanVien_Ngay_Ca nếu ca đang ở trạng thái CHO_DUYET.
     */
    public boolean luuBangGiaoCa(BangGiaoCa bgc) {
        try {
            if (bgc == null || bgc.getNhanVien() == null || bgc.getCaLamViec() == null) {
                return false;
            }
            LocalDate ngay = bgc.getGioKetThucThucTe() != null
                    ? bgc.getGioKetThucThucTe().toLocalDate()
                    : LocalDate.now();
            BangGiaoCa daCo = bangGiaoCaDAO.timGiaoCaTrongNgay(
                    bgc.getNhanVien().getMaNhanVien(),
                    ngay,
                    bgc.getCaLamViec().getMaCa()
            );
            if (daCo != null) {
                if ("DA_DUYET".equalsIgnoreCase(daCo.getTrangThai())) {
                    throw new IllegalStateException("Ca " + bgc.getCaLamViec().getMaCa()
                            + " ngày hôm nay đã được Quản lý duyệt két (Mã: "
                            + daCo.getMaGiaoCa() + "). Không thể chốt lại!");
                }
                // Ghi đè cập nhật vào bản ghi đang chờ duyệt
                bgc.setMaGiaoCa(daCo.getMaGiaoCa());
                return bangGiaoCaDAO.updateBangGiaoCaToanDien(bgc);
            }
            return bangGiaoCaDAO.insertBangGiaoCa(bgc);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi luuBangGiaoCa: " + e.getMessage());
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            return false;
        }
    }

    public BangGiaoCa timGiaoCaTrongNgay(String maNV, LocalDate ngay, String maCa) {
        try {
            return bangGiaoCaDAO.timGiaoCaTrongNgay(maNV, ngay, maCa);
        } catch (Exception e) {
            System.err.println("[GiaoCaBus] Lỗi timGiaoCaTrongNgay: " + e.getMessage());
            return null;
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
        String nguoiDangNhap = utils.UserSession.getUserId();
        if (nguoiDangNhap == null || nguoiDangNhap.isBlank()
                || maQuanLy == null || !nguoiDangNhap.equalsIgnoreCase(maQuanLy.trim())) {
            throw new SecurityException("Người duyệt phải đúng tài khoản Quản lý đang đăng nhập");
        }
        return bangGiaoCaDAO.duyetGiaoCa(maGC, nguoiDangNhap, thoiGianDuyet);
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
        this.caHienTai.setGioBatDauCa(LocalDateTime.now());
        this.caHienTai.setTrangThai("DANG_MO");

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
        if (tienHeThong == null || tienHeThong.compareTo(BigDecimal.ZERO) < 0
                || tienThucTe == null || tienThucTe.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền hệ thống và tiền kiểm đếm không được âm");
        }

        caHienTai.setTienCuoiCaHeThong(tienHeThong);
        caHienTai.setTienCuoiCaThucTe(tienThucTe);
        // note lai: tinh toan tien chenh lech chuyen ve dung tang BUS (null-safe)
        BigDecimal chenhLech = (tienThucTe != null && tienHeThong != null)
                ? tienThucTe.subtract(tienHeThong)
                : BigDecimal.ZERO;
        caHienTai.setTienChenhLech(chenhLech);
        caHienTai.setGioKetThucThucTe(LocalDateTime.now());
        caHienTai.setTrangThai("CHO_DUYET");

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

    public static BigDecimal tinhTienMatLyThuyet(BigDecimal tienDauCa,
                                                  BigDecimal thuTienMat,
                                                  BigDecimal hoanTienMat) {
        return TaiChinhHoaDonBus.tinhTienCuoiCa(tienDauCa, thuTienMat, hoanTienMat);
    }
}
