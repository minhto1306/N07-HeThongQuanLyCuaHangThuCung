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

    // Bộ nhớ đệm danh sách khách hàng trên RAM để phục vụ tìm kiếm / gợi ý tức thì
    private static final List<KhachHang> DANH_SACH_KHACH_HANG = new ArrayList<>();
    private static final KhachHangDAO STATIC_KH_DAO = new KhachHangDAO();

    // Đồng bộ danh bạ khách hàng từ SQL Server lên bộ nhớ đệm (RAM)
    // Dùng synchronized để đảm bảo an toàn đa luồng (Thread-safe) khi nhiều phiên POS cùng truy cập
    public static synchronized void dongBoKhachHangTuDatabase() {
        try {
            List<KhachHang> dbList = STATIC_KH_DAO.getAllKhachHang();
            if (dbList != null) {
                // Xóa danh sách cũ và nạp mới dữ liệu mới nhất từ CSDL
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
        // Mặc định khởi tạo ngay một phiên hóa đơn trắng khi mở màn hình POS
        khoiTaoHoaDonMoi(null, null);
    }

    /**
     * Khởi tạo một phiên hóa đơn mới tại quầy
     */
    // Khởi tạo phiên hóa đơn mới tại quầy: gán mã chuẩn, thời gian tạo và nhân viên thu ngân trực ca
    public void khoiTaoHoaDonMoi(String maHoaDon, NhanVien nhanVien) {
        // Bước 1: Nếu chưa có mã HĐ thì gọi hàm sinh mã tự động theo quy chuẩn HDyyMMdd-NV-xxx
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            maHoaDon = utils.MaTuSinhHelper.sinhMaHoaDon(nhanVien != null ? nhanVien.getMaNhanVien() : utils.UserSession.getUserId());
        }
        // Bước 2: Thiết lập thông tin ban đầu của hóa đơn ở trạng thái "Chờ thanh toán"
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

        // Bước 1: Ưu tiên tìm kiếm chính xác tuyệt đối theo Số điện thoại hoặc Mã khách hàng
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getSoDienThoai().equals(kw) || kh.getMaKhachHang().equalsIgnoreCase(kw)) {
                if (this.hoaDonHienTai != null) {
                    this.hoaDonHienTai.setKhachHang(kh);
                }
                return kh;
            }
        }

        // Bước 2: Nếu nhập từ 3 ký tự trở lên thì mở rộng tìm kiếm tương đối (chứa chuỗi SĐT hoặc Tên)
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
        dongBoKhachHangTuDatabase();
        for (KhachHang kh : DANH_SACH_KHACH_HANG) {
            if (kh.getMaKhachHang().equalsIgnoreCase(maKH)) {
                // Trim khoảng trắng thừa và chặn điểm tích lũy âm
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
        // Bước 1: Kiểm tra tính hợp lệ của tham số đầu vào
        if (sp == null || qd == null || soLuong <= 0) {
            throw new IllegalArgumentException("Thông tin sản phẩm hoặc số lượng không hợp lệ!");
        }

        // Bước 2: Quy đổi số lượng bán ra số lượng cơ sở (ví dụ: bán 2 hộp x tỷ lệ 10 = 20 gói cơ sở)
        int soLuongCoSoCanXuat = soLuong * qd.getTyLeQuyDoi();

        // Bước 3: Truy vấn danh sách các lô hàng còn tồn kho của sản phẩm, sắp xếp tăng dần theo Hạn sử dụng (FEFO)
        List<LoHang> dsLoHang = loHangDAO.getAvailableLotsFEFO(sp.getMaSanPham());
        if (dsLoHang.isEmpty()) {
            throw new IllegalStateException("Sản phẩm [" + sp.getTenSanPham() + "] hiện đã hết hàng hoặc các lô hàng đều đã quá hạn sử dụng!");
        }

        // Bước 4: Tìm kiếm lô hàng ưu tiên có hạn sử dụng gần nhất và đủ số lượng tồn khả dụng
        LoHang loChon = null;
        for (LoHang lo : dsLoHang) {
            if (lo.getSoLuongTon() >= soLuongCoSoCanXuat) {
                loChon = lo;
                break;
            }
        }

        // Bước 5: Nếu không có lô đơn lẻ nào đủ, chọn lô cận date nhất và kiểm tra tồn kho
        if (loChon == null) {
            loChon = dsLoHang.get(0);
            if (loChon.getSoLuongTon() < soLuongCoSoCanXuat) {
                throw new IllegalStateException("Lô hàng [" + loChon.getSoLo() + "] chỉ còn tồn " + loChon.getSoLuongTon() + " đơn vị, không đủ để xuất!");
            }
        }

        // Bước 6: Khởi tạo chi tiết xuất lô và gán vào giỏ hàng của hóa đơn hiện tại
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

        // Bước 1: Sinh mã chi tiết dịch vụ duy nhất trong hóa đơn
        String maCT = hoaDonHienTai.getMaHoaDon() + "_DV" + (hoaDonHienTai.getDsChiTiet().size() + 1);

        // Bước 2: Tự động tính 10% hoa hồng trên đơn giá dịch vụ để sau này phục vụ tính KPI/lương nhân sự
        BigDecimal hoaHong = dv.getGiaDichVu().multiply(BigDecimal.valueOf(0.10));

        // Bước 3: Đóng gói đối tượng ChiTietDichVu và lưu vào danh sách chi tiết hóa đơn
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
        // Bước 1: Cập nhật phương thức thanh toán (Tiền mặt, Chuyển khoản VietQR, VNPay) và mốc thời gian chốt đơn
        this.hoaDonHienTai.setPhuongThucThanhToan(phuongThucThanhToan);
        this.hoaDonHienTai.setThoiGianThanhToan(LocalDateTime.now());

        // Bước 2: Kích hoạt giao dịch thanh toán ACID trong HoaDonDAO (lưu HĐ, trừ tồn kho lô hàng, cộng điểm khách)
        boolean ok = hoaDonDAO.thanhToanHoaDon(this.hoaDonHienTai);

        // Bước 3: Sau khi lưu thành công, đồng bộ lại danh sách khách hàng để cập nhật điểm tích lũy mới
        if (ok) {
            dongBoKhachHangTuDatabase();
        }
        return ok;
    }
}
