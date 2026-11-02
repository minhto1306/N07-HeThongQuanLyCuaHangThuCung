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

// controller xử lý bán hàng và khách hàng
public class BanHangController {

    // list tạm lưu khách trên ram để tìm cho nhanh đỡ lag
    private static final List<KhachHang> DANH_SACH_KHACH_HANG = new ArrayList<>();
    private static final KhachHangDAO STATIC_KH_DAO = new KhachHangDAO();

    // kéo danh sách khách từ db lên
    public static synchronized void dongBoKhachHangTuDatabase() {
        try {
            List<KhachHang> dbList = STATIC_KH_DAO.getAllKhachHang();
            if (dbList != null) {
                DANH_SACH_KHACH_HANG.clear();
                DANH_SACH_KHACH_HANG.addAll(dbList);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đọc khách hàng: " + e.getMessage());
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
        // Mặc định khởi tạo ngay một phiên hóa đơn trắng khi mở màn hình POS
        khoiTaoHoaDonMoi(null, null);
    }

    /**
     * Khởi tạo một phiên hóa đơn mới tại quầy
     */
    // Khởi tạo phiên hóa đơn mới tại quầy: gán mã chuẩn, thời gian tạo và nhân viên thu ngân trực ca
    public void khoiTaoHoaDonMoi(String maHoaDon, NhanVien nhanVien) {
        // chưa có mã thì tự sinh mã theo ngày
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            maHoaDon = utils.MaTuSinhHelper.sinhMaHoaDon(nhanVien != null ? nhanVien.getMaNhanVien() : utils.UserSession.getUserId());
        }
        // khởi tạo hóa đơn mới
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
    // Tra cứu nhanh khách hàng khi thu ngân nhập thông tin vào ô tìm kiếm trên quầy POS
    public KhachHang timKhachHang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return null;
        }
        // Luôn đảm bảo nạp dữ liệu khách hàng mới nhất
        dongBoKhachHangTuDatabase();
        String kw = tuKhoa.trim();
        String kwLower = kw.toLowerCase();

        // tìm theo sdt hoặc mã trước
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().equals(kw) || kh.getMaKhachHang().equalsIgnoreCase(kw)) {
                if (this.hoaDonHienTai != null) {
                    this.hoaDonHienTai.setKhachHang(kh);
                }
                return kh;
            }
        }

        // gõ trên 3 ký tự thì tìm mờ theo tên
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

    // Lọc danh sách khách hàng gợi ý (Autocomplete) theo từ khóa SĐT, Họ tên hoặc Mã KH
    public static List<KhachHang> timKiemGoiYKhachHang(String tuKhoa) {
        // Đảm bảo dữ liệu bộ nhớ đệm đã được tải
        if (DANH_SACH_KHACH_HANG.isEmpty()) {
            dongBoKhachHangTuDatabase();
        }
        List<KhachHang> kq = new ArrayList<>();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            kq.addAll(DANH_SACH_KHACH_HANG);
            return kq;
        }
        String kw = tuKhoa.trim().toLowerCase();
        // Quét nhanh qua danh sách tạm trên RAM để không làm đơ giao diện Swing
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().toLowerCase().contains(kw)
                    || kh.getHoTen().toLowerCase().contains(kw)
                    || kh.getMaKhachHang().toLowerCase().contains(kw)) {
                kq.add(kh);
            }
        }
        return kq;
    }

    // Lưu mới hoặc cập nhật thông tin khách hàng và điểm tích lũy xuống SQL Server
    public static KhachHang luuHoacCapNhatKhachHang(String soDienThoai, String hoTen, int diemMoi) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
            return null;
        }
        dongBoKhachHangTuDatabase();
        String sdt = soDienThoai.trim();
        // Cắt chuỗi an toàn nếu quá 15 ký tự để tránh lỗi tràn cột CSDL (Data Truncation)
        if (sdt.length() > 15) {
            sdt = sdt.substring(0, 15);
        }

        // Trường hợp 1: Nếu khách đã tồn tại trong hệ thống -> Cập nhật tên và điểm tích lũy
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().equalsIgnoreCase(sdt)) {
                if (hoTen != null && !hoTen.trim().isEmpty() && !hoTen.trim().equalsIgnoreCase("Khách vãng lai")) {
                    kh.setHoTen(hoTen.trim());
                }
                // Đảm bảo điểm tích lũy không bao giờ bị âm
                kh.setDiemTichLuy(Math.max(0, diemMoi));
                try {
                    STATIC_KH_DAO.update(kh);
                } catch (SQLException e) {
                    System.err.println("[BanHangController] Lỗi update KhachHang CSDL: " + e.getMessage());
                }
                return kh;
            }
        }

        // Trường hợp 2: Khách hàng mới toanh -> Tự động sinh mã KH mới và thêm vào CSDL
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
        // Thêm ngay vào bộ nhớ đệm RAM để đồng bộ dữ liệu tức thì
        DANH_SACH_KHACH_HANG.add(khMoi);
        return khMoi;
    }

    // Gọi MaTuSinhHelper để sinh mã khách hàng tự tăng dạng KH0001 dựa trên mã lớn nhất trong CSDL
    public static String sinhMaKhachHangMoi() {
        return utils.MaTuSinhHelper.sinhMaKhachHang();
    }

    // Cập nhật thông tin chi tiết khách hàng: chuẩn hóa dữ liệu đầu vào và lưu xuống CSDL
    public static boolean capNhatThongTinKhachHang(String maKH, String hoTenMoi, String sdtMoi, int diemMoi) {
        return capNhatThongTinKhachHang(maKH, hoTenMoi, sdtMoi, diemMoi, "", "");
    }

    // overload cập nhật thông tin khách hàng có cả email và địa chỉ để gửi mail thông báo
    public static boolean capNhatThongTinKhachHang(String maKH, String hoTenMoi, String sdtMoi, int diemMoi, String emailMoi, String diaChiMoi) {
        dongBoKhachHangTuDatabase();
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getMaKhachHang().equalsIgnoreCase(maKH)) {
                kh.setHoTen(hoTenMoi.trim());
                kh.setSoDienThoai(sdtMoi.trim());
                kh.setDiemTichLuy(Math.max(0, diemMoi));
                kh.setEmail(emailMoi != null ? emailMoi.trim() : "");
                kh.setDiaChi(diaChiMoi != null ? diaChiMoi.trim() : "");
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

    // thêm khách hàng mới hoặc cập nhật kèm cả email và địa chỉ nhà
    public static KhachHang luuHoacCapNhatKhachHang(String soDienThoai, String hoTen, int diemMoi, String email, String diaChi) {
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
                if (email != null && !email.trim().isEmpty()) kh.setEmail(email.trim());
                if (diaChi != null && !diaChi.trim().isEmpty()) kh.setDiaChi(diaChi.trim());
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
        KhachHang khMoi = new KhachHang(maMoi, tenMoi, sdt, Math.max(0, diemMoi), email, diaChi);
        try {
            STATIC_KH_DAO.insert(khMoi);
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi insert KhachHang CSDL: " + e.getMessage());
        }
        DANH_SACH_KHACH_HANG.add(khMoi);
        return khMoi;
    }

    // Xóa khách hàng khỏi CSDL và loại bỏ luôn khỏi danh sách tạm trên RAM để tránh sai lệch dữ liệu
    public static boolean xoaKhachHang(String maKH) {
        try {
            STATIC_KH_DAO.delete(maKH);
        } catch (SQLException e) {
            System.err.println("[BanHangController] Lỗi xoaKhachHang CSDL: " + e.getMessage());
            return false;
        }
        return DANH_SACH_KHACH_HANG.removeIf(kh -> kh.getMaKhachHang().equalsIgnoreCase(maKH));
    }

    // Xếp hạng thành viên tự động dựa trên tổng điểm tích lũy
    public static String xepHangThanhVien(int diem) {
        if (diem >= 100) return "VIP Kim Cương";
        if (diem >= 50) return "Hạng Vàng";
        if (diem >= 20) return "Hạng Bạc";
        return "Thành viên Mới";
    }

    // Lấy toàn bộ danh sách khách hàng đang có để đổ lên bảng hiển thị trên giao diện
    public static List<KhachHang> getDanhSachKhachHang() {
        dongBoKhachHangTuDatabase();
        return DANH_SACH_KHACH_HANG;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng: Tự động tìm Lô hàng có hạn sử dụng gần nhất (FEFO)
     */
    // Xuất kho theo chiến lược FEFO (First Expired, First Out): kiểm tra tồn kho và ưu tiên lô có hạn dùng gần nhất
    public boolean themSanPhamXuatLo(SanPham sp, QuyDoiDonVi qd, int soLuong) throws Exception {
        // kiểm tra dữ liệu truyền vào
        if (sp == null || qd == null || soLuong <= 0) {
            throw new IllegalArgumentException("Thông tin sản phẩm hoặc số lượng không hợp lệ!");
        }

        // đổi ra số lượng cơ sở
        int soLuongCoSoCanXuat = soLuong * qd.getTyLeQuyDoi();

        // lấy mấy lô còn hạn theo thứ tự fefo
        List<LoHang> dsLoHang = loHangDAO.getAvailableLotsFEFO(sp.getMaSanPham());
        if (dsLoHang.isEmpty()) {
            throw new IllegalStateException("Sản phẩm [" + sp.getTenSanPham() + "] hết hàng hoặc hết date rồi");
        }

        // chọn lô cận date nhất mà còn đủ hàng
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
                throw new IllegalStateException("Lô [" + loChon.getSoLo() + "] còn có " + loChon.getSoLuongTon() + ", ko đủ xuất");
            }
        }

        // tạo dòng chi tiết xuất lô
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
    // Thêm dịch vụ chăm sóc thú cưng vào hóa đơn và tự động trích hoa hồng 10% cho kỹ thuật viên
    public boolean themDichVu(DichVu dv, ThuCung tc, NhanVien ktv) {
        if (dv == null || tc == null) {
            throw new IllegalArgumentException("Vui lòng chọn dịch vụ và thú cưng hợp lệ!");
        }

        String maCT = hoaDonHienTai.getMaHoaDon() + "_DV" + (hoaDonHienTai.getDsChiTiet().size() + 1);

        // trích 10% hoa hồng cho bạn làm spa
        BigDecimal hoaHong = dv.getGiaDichVu().multiply(BigDecimal.valueOf(0.10));

        // thêm vào bill
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

    // Áp dụng số tiền giảm giá (từ Voucher khuyến mãi hoặc chiết khấu điểm tích lũy)
    public void apDungGiamGia(BigDecimal giamGia) {
        this.hoaDonHienTai.setGiamGia(giamGia);
    }

    /**
     * Xác nhận thanh toán hóa đơn
     */
    // Xác nhận thanh toán: ghi nhận phương thức, cập nhật thời gian và kích hoạt ACID transaction lưu CSDL
    public boolean thanhToan(String phuongThucThanhToan) throws SQLException {
        // lưu phương thức thanh toán với giờ
        this.hoaDonHienTai.setPhuongThucThanhToan(phuongThucThanhToan);
        this.hoaDonHienTai.setThoiGianThanhToan(LocalDateTime.now());

        // gọi dao lưu xuống db
        boolean ok = hoaDonDAO.thanhToanHoaDon(this.hoaDonHienTai);

        if (ok) {
            dongBoKhachHangTuDatabase();
        }
        return ok;
    }
}
