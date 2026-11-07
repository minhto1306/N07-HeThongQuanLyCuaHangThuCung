package bus;

import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.LoHangDAO;
import entity.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// controller xử lý bán hàng và khách hàng
public class BanHangBus {

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

    public BanHangBus() {
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
        NhanVien nhanVienLap = nhanVien != null ? nhanVien : utils.UserSession.getCurrentUser();
        if (nhanVienLap == null || nhanVienLap.getMaNhanVien() == null) {
            throw new IllegalStateException("Phải đăng nhập trước khi mở phiên bán hàng");
        }
        // chưa có mã thì tự sinh mã theo ngày
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            maHoaDon = utils.MaTuSinhHelper.sinhMaHoaDon(nhanVienLap.getMaNhanVien());
        }
        // khởi tạo hóa đơn mới
        this.hoaDonHienTai = new HoaDon(
                maHoaDon,
                null,
                nhanVienLap,
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
     * DTO đại diện cho một mặt hàng hoặc dịch vụ trên danh mục POS.
     */
    public static class CatalogItem {
        public String ma;
        public String ten;
        public String moTa;
        public long gia;
        public String dvt;
        public int tyLe;
        public int tonCoSo;
        public int tonTheoQuyCach;
        public String tenHienThi;
        public boolean laDichVu;
        public int thoiLuongPhut;
        public String maVach;

        public CatalogItem() {}
    }

    /**
     * Lấy toàn bộ danh mục sản phẩm (kèm tỷ lệ quy đổi và tồn kho) cùng các dịch vụ Spa cho POS.
     * Tuân thủ kiến trúc N-Tier: Gọi qua SanPhamDAO và DichVuDAO, không truy vấn SQL trực tiếp tại tầng BUS.
     */
    public List<CatalogItem> layDanhSachMatHangCatalog() {
        List<CatalogItem> result = new ArrayList<>();
        try {
            dao.SanPhamDAO spDao = new dao.SanPhamDAO();
            for (Object[] row : spDao.layDanhSachSanPhamCatalogPOS()) {
                CatalogItem item = new CatalogItem();
                item.ma = (String) row[0];
                item.ten = (String) row[1];
                item.moTa = (String) row[2];
                item.maVach = (String) row[3];
                item.gia = (Long) row[5];
                item.dvt = (String) row[6];
                item.tyLe = (Integer) row[7];
                item.tonCoSo = (Integer) row[8];
                item.tonTheoQuyCach = item.tyLe > 0 ? (item.tonCoSo / item.tyLe) : item.tonCoSo;
                item.tenHienThi = item.tyLe > 1 ? (item.ten + " (" + item.dvt + ")") : item.ten;
                item.laDichVu = false;
                result.add(item);
            }

            dao.DichVuDAO dvDao = new dao.DichVuDAO();
            for (DichVu dv : dvDao.getAllDichVu()) {
                CatalogItem item = new CatalogItem();
                item.ma = dv.getMaDichVu();
                item.ten = dv.getTenDichVu();
                item.moTa = "Dịch vụ Grooming / Spa / Khách sạn thú cưng";
                item.gia = dv.getGiaDichVu() != null ? dv.getGiaDichVu().longValue() : 0L;
                item.dvt = "Lượt";
                item.tyLe = 1;
                item.tonCoSo = 999;
                item.tonTheoQuyCach = 999;
                item.tenHienThi = item.ten;
                item.laDichVu = true;
                item.thoiLuongPhut = dv.getThoiLuongPhut();
                result.add(item);
            }
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi đọc danh mục hàng hóa POS: " + e.getMessage());
        }
        return result;
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn từ CSDL.
     */
    public List<HoaDon> layTatCaHoaDon() {
        try {
            return hoaDonDAO.getAllHoaDon();
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi layTatCaHoaDon: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Thực hiện thanh toán và lưu hóa đơn POS cùng chi tiết xuất lô, trừ kho FEFO.
     */
    public boolean luuHoaDonPOS(HoaDon hd) {
        try {
            return hoaDonDAO.thanhToanHoaDon(hd);
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi luuHoaDonPOS: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lưu phiếu đổi trả liên kết với đơn POS.
     */
    public boolean luuPhieuDoiTra(PhieuDoiTra pdt) {
        try {
            return hoaDonDAO.luuPhieuDoiTra(pdt);
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi luuPhieuDoiTra: " + e.getMessage());
            return false;
        }
    }

    /** Lưu phiếu đổi hàng và hóa đơn mới trong cùng một transaction CSDL. */
    public boolean luuHoaDonKemDoiTra(HoaDon hd, PhieuDoiTra pdt) {
        try {
            return hoaDonDAO.thanhToanHoaDonKemDoiTra(hd, pdt);
        } catch (SQLException | RuntimeException e) {
            System.err.println("[BanHangBus] Lỗi luuHoaDonKemDoiTra: " + e.getMessage());
            return false;
        }
    }

    public boolean luuGiaoDichPosDayDu(HoaDon hd, PhieuDoiTra pdt,
                                      String maTheSuDung, entity.TheSpaPass theMoi,
                                      List<String> dsMaPhongHotel, int diemSuDung,
                                      int diemThuHoiDoTraHang) {
        try {
            return hoaDonDAO.thanhToanHoaDonDayDu(
                    hd, pdt, maTheSuDung, theMoi, dsMaPhongHotel,
                    diemSuDung, diemThuHoiDoTraHang);
        } catch (SQLException | RuntimeException e) {
            System.err.println("[BanHangBus] Lỗi luuGiaoDichPosDayDu: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy danh sách lô hàng FEFO cho một sản phẩm.
     */
    public List<LoHang> layDanhSachLoHangFEFO(String maSP) {
        try {
            return loHangDAO.getAvailableLotsFEFO(maSP);
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi layDanhSachLoHangFEFO: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Lấy toàn bộ danh sách lô hàng trong hệ thống.
     */
    public List<LoHang> layTatCaLoHang() {
        try {
            return loHangDAO.getAllLoHang();
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi layTatCaLoHang: " + e.getMessage());
            return Collections.emptyList();
        }
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
                    return kh;
                } catch (SQLException e) {
                    System.err.println("[BanHangBus] Lỗi update KhachHang CSDL: " + e.getMessage());
                    return null;
                }
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
            DANH_SACH_KHACH_HANG.add(khMoi);
            return khMoi;
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi insert KhachHang CSDL: " + e.getMessage());
            return null;
        }
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
                    System.err.println("[BanHangBus] Lỗi capNhatThongTinKhachHang CSDL: " + e.getMessage());
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
                    return kh;
                } catch (SQLException e) {
                    System.err.println("[BanHangBus] Lỗi update KhachHang CSDL: " + e.getMessage());
                    return null;
                }
            }
        }

        String tenMoi = (hoTen == null || hoTen.trim().isEmpty() || hoTen.trim().equalsIgnoreCase("Khách vãng lai"))
                ? "Khách hàng mới"
                : hoTen.trim();
        String maMoi = sinhMaKhachHangMoi();
        KhachHang khMoi = new KhachHang(maMoi, tenMoi, sdt, Math.max(0, diemMoi), email, diaChi);
        try {
            STATIC_KH_DAO.insert(khMoi);
            DANH_SACH_KHACH_HANG.add(khMoi);
            return khMoi;
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi insert KhachHang CSDL: " + e.getMessage());
            return null;
        }
    }

    // Xóa khách hàng khỏi CSDL và loại bỏ luôn khỏi danh sách tạm trên RAM để tránh sai lệch dữ liệu
    public static boolean xoaKhachHang(String maKH) {
        try {
            STATIC_KH_DAO.delete(maKH);
        } catch (SQLException e) {
            System.err.println("[BanHangBus] Lỗi xoaKhachHang CSDL: " + e.getMessage());
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

    /**
     * Tra cứu nhanh sản phẩm bán lẻ theo Mã vạch (Barcode EAN-13) hoặc Mã sản phẩm (VD: SP001).
     */
    public CatalogItem timSanPhamTheoMaVachHoacMaSP(String maInput) {
        if (maInput == null || maInput.trim().isEmpty()) {
            return null;
        }
        String kw = maInput.trim();
        List<CatalogItem> catalog = layDanhSachMatHangCatalog();
        for (CatalogItem item : catalog) {
            if (!item.laDichVu && item.tyLe == 1) {
                if ((item.maVach != null && item.maVach.equalsIgnoreCase(kw))
                        || (item.ma != null && item.ma.equalsIgnoreCase(kw))) {
                    return item;
                }
            }
        }
        for (CatalogItem item : catalog) {
            if (!item.laDichVu) {
                if ((item.maVach != null && item.maVach.equalsIgnoreCase(kw))
                        || (item.ma != null && item.ma.equalsIgnoreCase(kw))) {
                    return item;
                }
            }
        }
        String kwLower = kw.toLowerCase();
        for (CatalogItem item : catalog) {
            if (!item.laDichVu) {
                if ((item.maVach != null && item.maVach.toLowerCase().contains(kwLower))
                        || (item.ma != null && item.ma.toLowerCase().contains(kwLower))
                        || (item.ten != null && item.ten.toLowerCase().contains(kwLower))) {
                    return item;
                }
            }
        }
        return null;
    }

    public KhachHang timKhachHangTheoSDT(String sdt) {
        return timKhachHang(sdt);
    }

    public static long tinhThanhTienTungDong(int soLuong, long donGia) {
        if (soLuong <= 0 || donGia < 0) return 0L;
        return (long) soLuong * donGia;
    }

    public static long tinhGiamGiaVIP(long tongTienHang, boolean isVIP) {
        if (!isVIP || tongTienHang <= 0) return 0L;
        return Math.round(tongTienHang * 0.05);
    }

    public static long tinhTienGiamTuDiem(int soDiemMuonDung, int diemHienCo, long tongTienSauVIP) {
        if (soDiemMuonDung <= 0 || diemHienCo <= 0 || tongTienSauVIP <= 0) return 0L;
        int diemHopLe = Math.max(0, Math.min(soDiemMuonDung, diemHienCo));
        return Math.min(tongTienSauVIP, diemHopLe * 1000L);
    }

    public static long tinhTongThanhToan(long tongTienHang, long tienGiamVIP, long tienDoiDiem, long tienKhauTruDoiTra) {
        return Math.max(0L, tongTienHang - tienGiamVIP - tienDoiDiem - tienKhauTruDoiTra);
    }

    public static int tinhDiemThuongMoi(long tongThanhToan) {
        if (tongThanhToan <= 0) return 0;
        return (int) (tongThanhToan / 100000L);
    }

    private void unusedEndMarker() {
    }
}
