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

    // Kéo hết danh bạ khách hàng từ SQL Server lên RAM sẵn, để lát thu ngân gõ SĐT hay tên là gợi ý tức thì, không bị khựng giao diện Swing
    public static synchronized void dongBoKhachHangTuDatabase() {
        try {
            List<KhachHang> dbList = STATIC_KH_DAO.getAllKhachHang();
            if (dbList != null) {
                DANH_SACH_KHACH_HANG.clear();
                DANH_SACH_KHACH_HANG.addAll(dbList);
            }
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi đọc KhachHang từ CSDL: " + e.getMessage());
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
    // Tạo phiên hóa đơn mới tại quầy, nếu chưa có mã thì tự sinh mã theo ngày với ca trực cho chuẩn
    public void khoiTaoHoaDonMoi(String maHoaDon, NhanVien nhanVien) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            maHoaDon = utils.MaTuSinhHelper.sinhMaHoaDon(nhanVien != null ? nhanVien.getMaNhanVien() : utils.UserSession.getUserId());
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
    // Tìm nhanh thông tin khách khi nhập SĐT hoặc mã KH, từ 3 ký tự trở lên thì cho tìm mờ theo tên luôn cho tiện
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

    // Lọc danh sách gợi ý tức thì khi thu ngân gõ vào ô tìm kiếm, gõ tới đâu lọc mờ tới đó theo SĐT hoặc tên
    public static List<KhachHang> timKiemGoiYKhachHang(String tuKhoa) {
        if (DANH_SACH_KHACH_HANG.isEmpty()) {
            dongBoKhachHangTuDatabase();
        }
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

    // Khách quen thì cập nhật lại tên với điểm, còn số mới toanh thì tự sinh mã KH rồi lưu thẳng vào CSDL luôn
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

    // Gọi qua MaTuSinhHelper để lấy mã khách hàng tự tăng dạng KH0001, khỏi lo trùng mã
    public static String sinhMaKhachHangMoi() {
        return utils.MaTuSinhHelper.sinhMaKhachHang();
    }

    // Cập nhật lại thông tin khách, nhớ trim khoảng trắng với chặn điểm âm trước khi bắn xuống DB
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

    // Xóa khách dưới CSDL xong thì nhớ xóa luôn trong danh sách tạm trên RAM để không bị lệch dữ liệu
    public static boolean xoaKhachHang(String maKH) {
        try {
            STATIC_KH_DAO.delete(maKH);
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi xoaKhachHang CSDL: " + e.getMessage());
            return false;
        }
        return DANH_SACH_KHACH_HANG.removeIf(kh -> kh.getMaKhachHang().equalsIgnoreCase(maKH));
    }

    // Tự động xếp hạng thành viên theo điểm tích lũy: mốc 100 điểm là Kim Cương, 50 điểm Vàng, 20 điểm Bạc
    public static String xepHangThanhVien(int diem) {
        if (diem >= 100) return "VIP Kim Cương";
        if (diem >= 50) return "Hạng Vàng";
        if (diem >= 20) return "Hạng Bạc";
        return "Thành viên Mới";
    }

    // Lấy toàn bộ danh sách khách hàng (đã đồng bộ với CSDL) để đổ lên bảng quản lý hoặc combobox
    public static List<KhachHang> getDanhSachKhachHang() {
        dongBoKhachHangTuDatabase();
        return DANH_SACH_KHACH_HANG;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng: Tự động tìm Lô hàng có hạn sử dụng gần nhất (FEFO)
     */
    // Xuất kho theo chuẩn FEFO: tự động bốc lô nào cận date nhất trước, quy đổi ra đơn vị cơ sở rồi mới trừ tồn kho
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
    // Thêm dịch vụ spa / chăm sóc bé cưng vào hóa đơn, tự tính sẵn 10% hoa hồng cho kỹ thuật viên
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

    // Áp voucher giảm giá hoặc chiết khấu điểm tích lũy trực tiếp vào tổng tiền hóa đơn
    public void apDungGiamGia(BigDecimal giamGia) {
        this.hoaDonHienTai.setGiamGia(giamGia);
    }

    /**
     * Xác nhận thanh toán hóa đơn
     */
    // Chốt đơn: cập nhật giờ thanh toán rồi gọi HoaDonDAO chạy giao dịch ACID lưu an toàn tuyệt đối vào CSDL
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
