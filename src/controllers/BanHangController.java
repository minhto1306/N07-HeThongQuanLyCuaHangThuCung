package controllers;

import models.dao.HoaDonDAO;
import models.dao.KhachHangDAO;
import models.dao.LoHangDAO;
import models.entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller điều phối toàn bộ nghiệp vụ Bán hàng POS và Khách hàng Thành viên:
 * 1. Đồng bộ 100% danh sách khách hàng và điểm tích lũy với SQL Server qua KhachHangDAO.
 * 2. Thêm sản phẩm xuất từ lô hàng theo FEFO (First Expired, First Out).
 * 3. Gọi HoaDonDAO thực thi ACID Transaction.
 */
public class BanHangController {

    private static final List<KhachHang> DANH_SACH_KHACH_HANG = new ArrayList<>();
    private static final KhachHangDAO STATIC_KH_DAO = new KhachHangDAO();

    static {
        dongBoKhachHangTuDatabase();
    }

    public static synchronized void dongBoKhachHangTuDatabase() {
        try {
            List<KhachHang> dbList = STATIC_KH_DAO.getAllKhachHang();
            if (dbList != null && !dbList.isEmpty()) {
                DANH_SACH_KHACH_HANG.clear();
                DANH_SACH_KHACH_HANG.addAll(dbList);
                return;
            }
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi đọc KhachHang từ CSDL: " + e.getMessage());
        }
        if (DANH_SACH_KHACH_HANG.isEmpty()) {
            DANH_SACH_KHACH_HANG.add(new KhachHang("KH001", "Nguyễn Văn An", "0988111222", 120));
            DANH_SACH_KHACH_HANG.add(new KhachHang("KH002", "Trần Thị Mai", "0977333444", 50));
            DANH_SACH_KHACH_HANG.add(new KhachHang("KH003", "Lê Hoàng Yến", "0912345678", 15));
            DANH_SACH_KHACH_HANG.add(new KhachHang("KH004", "Phạm Hoàng Long", "0918776655", 210));
            DANH_SACH_KHACH_HANG.add(new KhachHang("KH005", "Đỗ Quỳnh Chi", "0905123456", 35));
        }
    }

    private final HoaDonDAO hoaDonDAO;
    private final LoHangDAO loHangDAO;
    private final KhachHangDAO khachHangDAO;
    private HoaDon hoaDonHienTai;

    public BanHangController() {
        this.hoaDonDAO = new HoaDonDAO();
        this.loHangDAO = new LoHangDAO();
        this.khachHangDAO = new KhachHangDAO();
        khoiTaoHoaDonMoi(null, null);
    }

    /**
     * Khởi tạo một phiên hóa đơn mới tại quầy
     */
    public void khoiTaoHoaDonMoi(String maHoaDon, NhanVien nhanVien) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            maHoaDon = "HD" + System.currentTimeMillis() % 1000000;
        }
        this.hoaDonHienTai = new HoaDon(
                maHoaDon,
                null,
                nhanVien != null ? nhanVien : new NhanVien("NV001", "Nhân viên thu ngân", "Thu ngân", "", null),
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "Tiền mặt",
                "Chờ thanh toán"
        );
    }

    public HoaDon getHoaDonHienTai() {
        return hoaDonHienTai;
    }

    /**
     * Tra cứu khách hàng thành viên theo số điện thoại, mã KH hoặc tên trực tiếp từ CSDL
     */
    public KhachHang timKhachHang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return null;
        }
        dongBoKhachHangTuDatabase();
        String kw = tuKhoa.trim();
        String kwLower = kw.toLowerCase();
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().equals(kw) || kh.getMaKhachHang().equalsIgnoreCase(kw)) {
                if (this.hoaDonHienTai != null) {
                    this.hoaDonHienTai.setKhachHang(kh);
                }
                return kh;
            }
        }
        if (kw.length() >= 3) {
            for (KhachHang kh : DANH_SACH_KHACH_HANG) {
                if (kh.getSoDienThoai().contains(kw) || kh.getHoTen().toLowerCase().contains(kwLower)) {
                    if (this.hoaDonHienTai != null) {
                        this.hoaDonHienTai.setKhachHang(kh);
                    }
                    return kh;
                }
            }
        }
        return null;
    }

    /**
     * Tìm danh sách khách hàng gợi ý khi gõ SĐT, Mã KH hoặc Họ tên
     */
    public static List<KhachHang> timKiemGoiYKhachHang(String tuKhoa) {
        dongBoKhachHangTuDatabase();
        List<KhachHang> kq = new ArrayList<>();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            kq.addAll(DANH_SACH_KHACH_HANG);
            return kq;
        }
        String kw = tuKhoa.trim().toLowerCase();
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().toLowerCase().contains(kw)
                    || kh.getHoTen().toLowerCase().contains(kw)
                    || kh.getMaKhachHang().toLowerCase().contains(kw)) {
                kq.add(kh);
            }
        }
        return kq;
    }

    /**
     * Lưu hoặc cập nhật khách hàng và điểm tích lũy xuống SQL Server
     */
    public static KhachHang luuHoacCapNhatKhachHang(String soDienThoai, String hoTen, int diemMoi) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return null;
        }
        dongBoKhachHangTuDatabase();
        String sdt = soDienThoai.trim();
        if (sdt.length() > 15) {
            sdt = sdt.substring(0, 15);
        }

        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().equalsIgnoreCase(sdt)) {
                if (hoTen != null && !hoTen.trim().isEmpty() && !hoTen.trim().equalsIgnoreCase("Khách vãng lai")) {
                    kh.setHoTen(hoTen.trim());
                }
                kh.setDiemTichLuy(Math.max(0, diemMoi));
                try {
                    STATIC_KH_DAO.update(kh);
                } catch (SQLException e) {
                    System.err.println("[BanHangController] Lỗi update KhachHang CSDL: " + e.getMessage());
                }
                return kh;
            }
        }

        String tenMoi = (hoTen == null || hoTen.trim().isEmpty() || hoTen.trim().equalsIgnoreCase("Khách vãng lai"))
                ? "Khách hàng mới"
                : hoTen.trim();
        String maMoi = sinhMaKhachHangMoi();
        KhachHang khMoi = new KhachHang(maMoi, tenMoi, sdt, Math.max(0, diemMoi));
        try {
            STATIC_KH_DAO.insert(khMoi);
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi insert KhachHang CSDL: " + e.getMessage());
        }
        DANH_SACH_KHACH_HANG.add(khMoi);
        return khMoi;
    }

    public static String sinhMaKhachHangMoi() {
        dongBoKhachHangTuDatabase();
        int max = DANH_SACH_KHACH_HANG.size() + 1;
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            try {
                int num = Integer.parseInt(kh.getMaKhachHang().replaceAll("\\D+", ""));
                if (num >= max) max = num + 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return String.format("KH%03d", max);
    }

    public static boolean capNhatThongTinKhachHang(String maKH, String hoTenMoi, String sdtMoi, int diemMoi) {
        dongBoKhachHangTuDatabase();
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getMaKhachHang().equalsIgnoreCase(maKH)) {
                kh.setHoTen(hoTenMoi.trim());
                kh.setSoDienThoai(sdtMoi.trim());
                kh.setDiemTichLuy(Math.max(0, diemMoi));
                try {
                    STATIC_KH_DAO.update(kh);
                    return true;
                } catch (SQLException e) {
                    System.err.println("[BanHangController] Lỗi capNhatThongTinKhachHang CSDL: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean xoaKhachHang(String maKH) {
        try {
            STATIC_KH_DAO.delete(maKH);
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi xoaKhachHang CSDL: " + e.getMessage());
            return false;
        }
        return DANH_SACH_KHACH_HANG.removeIf(kh -> kh.getMaKhachHang().equalsIgnoreCase(maKH));
    }

    public static String xepHangThanhVien(int diem) {
        if (diem >= 100) return "VIP Kim Cương";
        if (diem >= 50) return "Hạng Vàng";
        if (diem >= 20) return "Hạng Bạc";
        return "Thành viên Mới";
    }

    public static List<KhachHang> getDanhSachKhachHang() {
        dongBoKhachHangTuDatabase();
        return DANH_SACH_KHACH_HANG;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng: Tự động tìm Lô hàng có hạn sử dụng gần nhất (FEFO)
     */
    public boolean themSanPhamXuatLo(SanPham sp, QuyDoiDonVi qd, int soLuong) throws Exception {
        if (sp == null || qd == null || soLuong <= 0) {
            throw new IllegalArgumentException("Thông tin sản phẩm hoặc số lượng không hợp lệ!");
        }

        int soLuongCoSoCanXuat = soLuong * qd.getTyLeQuyDoi();

        List<LoHang> dsLoHang = loHangDAO.getAvailableLotsFEFO(sp.getMaSanPham());
        if (dsLoHang.isEmpty()) {
            throw new IllegalStateException("Sản phẩm [" + sp.getTenSanPham() + "] hiện đã hết hàng hoặc các lô hàng đều đã quá hạn sử dụng!");
        }

        LoHang loChon = null;
        for (LoHang lo : dsLoHang) {
            if (lo.getSoLuongTon() >= soLuongCoSoCanXuat) {
                loChon = lo;
                break;
            }
        }

        if (loChon == null) {
            loChon = dsLoHang.get(0);
            if (loChon.getSoLuongTon() < soLuongCoSoCanXuat) {
                throw new IllegalStateException("Lô hàng [" + loChon.getSoLo() + "] chỉ còn tồn " + loChon.getSoLuongTon() + " đơn vị, không đủ để xuất!");
            }
        }

        String maCT = hoaDonHienTai.getMaHoaDon() + "_SP" + (hoaDonHienTai.getDsChiTiet().size() + 1);
        ChiTietXuatLo ctXuatLo = new ChiTietXuatLo(
                maCT,
                hoaDonHienTai,
                soLuong,
                qd.getGiaBan(),
                loChon,
                qd
        );

        hoaDonHienTai.addChiTiet(ctXuatLo);
        return true;
    }

    /**
     * Thêm dịch vụ thú cưng vào hóa đơn
     */
    public boolean themDichVu(DichVu dv, ThuCung tc, NhanVien ktv) {
        if (dv == null || tc == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ và thú cưng hợp lệ!");
        }

        String maCT = hoaDonHienTai.getMaHoaDon() + "_DV" + (hoaDonHienTai.getDsChiTiet().size() + 1);
        BigDecimal hoaHong = dv.getGiaDichVu().multiply(BigDecimal.valueOf(0.10));

        ChiTietDichVu ctDv = new ChiTietDichVu(
                maCT,
                hoaDonHienTai,
                1,
                dv.getGiaDichVu(),
                dv,
                tc,
                ktv,
                hoaHong
        );

        hoaDonHienTai.addChiTiet(ctDv);
        return true;
    }

    /**
     * Áp dụng giảm giá (Voucher hoặc chiết khấu điểm)
     */
    public void apDungGiamGia(BigDecimal giamGia) {
        this.hoaDonHienTai.setGiamGia(giamGia);
    }

    /**
     * Xác nhận thanh toán hóa đơn
     */
    public boolean thanhToan(String phuongThucThanhToan) throws SQLException {
        this.hoaDonHienTai.setPhuongThucThanhToan(phuongThucThanhToan);
        this.hoaDonHienTai.setThoiGianThanhToan(LocalDateTime.now());
        boolean ok = hoaDonDAO.thanhToanHoaDon(this.hoaDonHienTai);
        if (ok) {
            dongBoKhachHangTuDatabase();
        }
        return ok;
    }
}
