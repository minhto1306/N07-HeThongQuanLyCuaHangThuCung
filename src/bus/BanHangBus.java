package bus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.LoHangDAO;
import entity.ChiTietDichVu;
import entity.ChiTietHoaDon;
import entity.ChiTietXuatLo;
import entity.DichVu;
import entity.HoaDon;
import entity.KhachHang;
import entity.LoHang;
import entity.NhanVien;
import entity.PhieuDoiTra;
import entity.QuyDoiDonVi;
import entity.SanPham;
import entity.ThuCung;

//  xử lý bán hàng và khách hàng
public class BanHangBus {

	// Cache khách hàng với Double-Checked Locking và TTL 30 giây (tránh race condition và giảm tải DB)
	private static final long KHACH_HANG_CACHE_TTL_MS = 30_000L;
	private static volatile List<KhachHang> DANH_SACH_KHACH_HANG = Collections.emptyList();
	private static volatile long lastSyncKhachHangTime = 0L;
	private static final KhachHangDAO STATIC_KH_DAO = new KhachHangDAO();

	public static List<KhachHang> getCachedKhachHang() {
		long now = System.currentTimeMillis();
		if (DANH_SACH_KHACH_HANG.isEmpty() || now - lastSyncKhachHangTime > KHACH_HANG_CACHE_TTL_MS) {
			synchronized (BanHangBus.class) {
				if (DANH_SACH_KHACH_HANG.isEmpty() || now - lastSyncKhachHangTime > KHACH_HANG_CACHE_TTL_MS) {
					try {
						List<KhachHang> dbList = STATIC_KH_DAO.getAllKhachHang();
						if (dbList != null) {
							DANH_SACH_KHACH_HANG = Collections.unmodifiableList(new ArrayList<>(dbList));
						} else {
							DANH_SACH_KHACH_HANG = Collections.emptyList();
						}
						lastSyncKhachHangTime = System.currentTimeMillis();
					} catch (SQLException e) {
						lastSyncKhachHangTime = System.currentTimeMillis(); // Tránh dồn dập query khi DB lỗi
						System.err.println("Lỗi nạp cache khách hàng: " + e.getMessage());
					}
				}
			}
		}
		return DANH_SACH_KHACH_HANG;
	}

	public static void dongBoKhachHangTuDatabase() {
		dongBoKhachHangTuDatabase(true);
	}

	public static synchronized void dongBoKhachHangTuDatabase(boolean force) {
		if (force) {
			lastSyncKhachHangTime = 0L;
		}
		getCachedKhachHang();
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
	// Khởi tạo phiên hóa đơn mới tại quầy: gán mã chuẩn, thời gian tạo và nhân viên
	// thu ngân trực ca
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
		this.hoaDonHienTai = new HoaDon(maHoaDon, null, nhanVienLap, LocalDateTime.now(), BigDecimal.ZERO, "Tiền mặt",
				"Chờ thanh toán");
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

		public CatalogItem() {
		}
	}

	/**
	 * Lấy toàn bộ danh mục sản phẩm (kèm tỷ lệ quy đổi và tồn kho) cùng các dịch vụ
	 * Spa cho POS. Tuân thủ kiến trúc N-Tier: Gọi qua SanPhamDAO và DichVuDAO,
	 * không truy vấn SQL trực tiếp tại tầng BUS.
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

	public boolean luuGiaoDichPosDayDu(HoaDon hd, PhieuDoiTra pdt, String maTheSuDung, entity.TheSpaPass theMoi,
			List<String> dsMaPhongHotel, List<entity.HotelCheckoutSnapshot> dsSnapshotsHotel, int diemSuDung,
			int diemThuHoiDoTraHang) {
		if (diemSuDung < 0) {
			throw new IllegalArgumentException("Điểm sử dụng không được là số âm: " + diemSuDung);
		}
		if (hd != null && hd.getDsChiTiet() != null) {
			for (ChiTietHoaDon ct : hd.getDsChiTiet()) {
				if (ct == null)
					continue;
				if (ct.getDonGiaLucBan() == null)
					ct.setDonGiaLucBan(BigDecimal.ZERO);
				if (ct.getThanhTien() == null) {
					ct.setThanhTien(ct.getDonGiaLucBan().multiply(BigDecimal.valueOf(Math.max(1, ct.getSoLuong()))));
				}
			}
		}
		if (diemThuHoiDoTraHang < 0) {
			throw new IllegalArgumentException("Điểm thu hồi do trả hàng không được là số âm: " + diemThuHoiDoTraHang);
		}
		if (hd != null && hd.getKhachHang() != null && diemSuDung > 0) {
			KhachHang kh = timKhachHang(hd.getKhachHang().getMaKhachHang());
			if (kh == null)
				kh = timKhachHang(hd.getKhachHang().getSoDienThoai());
			if (kh != null && diemSuDung > kh.getDiemTichLuy()) {
				throw new IllegalArgumentException("Điểm sử dụng (" + diemSuDung + ") vượt quá điểm tích lũy hiện có ("
						+ kh.getDiemTichLuy() + ")");
			}
		}
		try {
			return hoaDonDAO.thanhToanHoaDonDayDu(hd, pdt, maTheSuDung, theMoi, dsMaPhongHotel, dsSnapshotsHotel,
					diemSuDung, diemThuHoiDoTraHang);
		} catch (SQLException | RuntimeException e) {
			System.err.println("[BanHangBus] Lỗi luuGiaoDichPosDayDu: " + e.getMessage());
			return false;
		}
	}

	public boolean luuGiaoDichPosDayDu(HoaDon hd, PhieuDoiTra pdt, String maTheSuDung, entity.TheSpaPass theMoi,
			List<String> dsMaPhongHotel, int diemSuDung, int diemThuHoiDoTraHang) {
		return luuGiaoDichPosDayDu(hd, pdt, maTheSuDung, theMoi, dsMaPhongHotel, Collections.emptyList(), diemSuDung,
				diemThuHoiDoTraHang);
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
	 * Tra cứu khách hàng thành viên theo số điện thoại, mã KH hoặc tên trực tiếp từ
	 * CSDL
	 */
	// Tra cứu nhanh khách hàng khi thu ngân nhập thông tin vào ô tìm kiếm trên quầy POS (Null-safe 100%)
	public KhachHang timKhachHang(String tuKhoa) {
		if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
			return null;
		}
		String kw = tuKhoa.trim();
		String kwLower = kw.toLowerCase();
		List<KhachHang> list = getCachedKhachHang();

		// 1. Tìm chính xác theo SĐT hoặc Mã KH
		for (KhachHang kh : list) {
			if (kh == null) continue;
			if (Objects.equals(kh.getSoDienThoai(), kw)
					|| kw.equalsIgnoreCase(Objects.toString(kh.getMaKhachHang(), ""))) {
				if (this.hoaDonHienTai != null) {
					this.hoaDonHienTai.setKhachHang(kh);
				}
				return kh;
			}
		}

		// 2. Gõ từ 3 ký tự trở lên thì tìm mờ theo SĐT hoặc Họ tên
		if (kw.length() >= 3) {
			for (KhachHang kh : list) {
			if (kh == null) continue;
				String sdt = Objects.toString(kh.getSoDienThoai(), "");
				String hoTen = Objects.toString(kh.getHoTen(), "").toLowerCase();
				if (sdt.contains(kw) || hoTen.contains(kwLower)) {
					if (this.hoaDonHienTai != null) {
						this.hoaDonHienTai.setKhachHang(kh);
					}
					return kh;
				}
			}
		}
		return null;
	}

	// Lọc danh sách khách hàng gợi ý (Autocomplete) theo từ khóa SĐT, Họ tên hoặc Mã KH (Null-safe 100%)
	public static List<KhachHang> timKiemGoiYKhachHang(String tuKhoa) {
		List<KhachHang> kq = new ArrayList<>();
		List<KhachHang> list = getCachedKhachHang();
		if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
			kq.addAll(list);
			return kq;
		}
		String kw = tuKhoa.trim().toLowerCase();
		for (KhachHang kh : list) {
			if (kh == null) continue;
			String sdt = Objects.toString(kh.getSoDienThoai(), "").toLowerCase();
			String hoTen = Objects.toString(kh.getHoTen(), "").toLowerCase();
			String maKH = Objects.toString(kh.getMaKhachHang(), "").toLowerCase();
			if (sdt.contains(kw) || hoTen.contains(kw) || maKH.contains(kw)) {
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
		dongBoKhachHangTuDatabase(true);
		String sdt = soDienThoai.trim();
		// Cắt chuỗi an toàn nếu quá 15 ký tự để tránh lỗi tràn cột CSDL (Data
		// Truncation)
		if (sdt.length() > 15) {
			sdt = sdt.substring(0, 15);
		}

		// Trường hợp 1: Nếu khách đã tồn tại trong hệ thống -> Cập nhật tên và điểm
		// tích lũy
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

	// Gọi MaTuSinhHelper để sinh mã khách hàng tự tăng dạng KH0001 dựa trên mã lớn
	// nhất trong CSDL
	public static String sinhMaKhachHangMoi() {
		return utils.MaTuSinhHelper.sinhMaKhachHang();
	}

	// Cập nhật thông tin chi tiết khách hàng: chuẩn hóa dữ liệu đầu vào và lưu
	// xuống CSDL
	public static boolean capNhatThongTinKhachHang(String maKH, String hoTenMoi, String sdtMoi, int diemMoi) {
		return capNhatThongTinKhachHang(maKH, hoTenMoi, sdtMoi, diemMoi, "", "");
	}

	// overload cập nhật thông tin khách hàng có cả email và địa chỉ để gửi mail
	// thông báo
	public static boolean capNhatThongTinKhachHang(String maKH, String hoTenMoi, String sdtMoi, int diemMoi,
			String emailMoi, String diaChiMoi) {
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
	public static KhachHang luuHoacCapNhatKhachHang(String soDienThoai, String hoTen, int diemMoi, String email,
			String diaChi) {
		if (soDienThoai == null || soDienThoai.trim().isEmpty()) {
			return null;
		}
		dongBoKhachHangTuDatabase(true);
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
				if (email != null && !email.trim().isEmpty())
					kh.setEmail(email.trim());
				if (diaChi != null && !diaChi.trim().isEmpty())
					kh.setDiaChi(diaChi.trim());
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

	// Xóa khách hàng khỏi CSDL và loại bỏ luôn khỏi danh sách tạm trên RAM để tránh
	// sai lệch dữ liệu
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
		if (diem >= 100)
			return "VIP Kim Cương";
		if (diem >= 50)
			return "Hạng Vàng";
		if (diem >= 20)
			return "Hạng Bạc";
		return "Thành viên Mới";
	}

	// Lấy toàn bộ danh sách khách hàng đang có để đổ lên bảng hiển thị trên giao
	// diện
	public static List<KhachHang> getDanhSachKhachHang() {
		dongBoKhachHangTuDatabase();
		return DANH_SACH_KHACH_HANG;
	}

	/**
	 * Thêm sản phẩm vào giỏ hàng: Tự động tìm Lô hàng có hạn sử dụng gần nhất
	 * (FEFO)
	 */
	// Xuất kho theo chiến lược FEFO (First Expired, First Out): kiểm tra tồn kho và
	// phân bổ đa lô tự động (BH01)
	public boolean themSanPhamXuatLo(SanPham sp, QuyDoiDonVi qd, int soLuong) throws Exception {
		// kiểm tra dữ liệu truyền vào
		if (sp == null || qd == null || soLuong <= 0) {
			throw new IllegalArgumentException("Thông tin sản phẩm hoặc số lượng không hợp lệ!");
		}

		// kiểm tra tỷ lệ quy đổi (fail fast: không chấp nhận <= 0)
		if (qd.getTyLeQuyDoi() <= 0) {
			throw new IllegalArgumentException("Tỷ lệ quy đổi không hợp lệ!");
		}
		int tyLe = qd.getTyLeQuyDoi();

		// đổi ra số lượng cơ sở (kiểm tra chống tràn số nguyên)
		long soLuongCoSoCanXuatLong = (long) soLuong * tyLe;
		if (soLuongCoSoCanXuatLong > Integer.MAX_VALUE || soLuongCoSoCanXuatLong <= 0) {
			throw new IllegalArgumentException("Số lượng xuất kho vượt quá giới hạn cho phép của hệ thống!");
		}
		int soLuongCoSoCanXuat = (int) soLuongCoSoCanXuatLong;

		// lấy các lô còn hạn theo thứ tự FEFO (cận date nhất lên trước)
		List<LoHang> dsLoHang = loHangDAO.getAvailableLotsFEFO(sp.getMaSanPham());
		if (dsLoHang.isEmpty()) {
			throw new IllegalStateException("Sản phẩm [" + sp.getTenSanPham() + "] đã hết hàng hoặc hết hạn sử dụng!");
		}

		long tongTonKhaDungLong = dsLoHang.stream().mapToLong(LoHang::getSoLuongTon).sum();
		if (tongTonKhaDungLong < soLuongCoSoCanXuatLong) {
			throw new IllegalStateException(
					"Sản phẩm [" + sp.getTenSanPham() + "] chỉ còn tổng tồn " + tongTonKhaDungLong
							+ " đơn vị cơ sở, không đủ xuất số lượng yêu cầu (" + soLuongCoSoCanXuat + ")!");
		}

		// Phân bổ xuất hàng qua nhiều lô FEFO nếu một lô không đủ số lượng (BH01)
		int conLaiCoSo = soLuongCoSoCanXuat;
		for (LoHang lo : dsLoHang) {
			if (lo.getSoLuongTon() <= 0)
				continue;
			int xuatLoNayCoSo = Math.min(conLaiCoSo, lo.getSoLuongTon());

			int slXuat;
			BigDecimal donGiaXuat = qd.getGiaBan();
			QuyDoiDonVi qdXuat = qd;

			if (xuatLoNayCoSo == soLuongCoSoCanXuat) {
				slXuat = soLuong;
			} else if (tyLe > 1 && xuatLoNayCoSo % tyLe == 0) {
				slXuat = xuatLoNayCoSo / tyLe;
			} else if (tyLe > 1) {
				// Xuất lẻ theo đơn vị cơ sở vì lô không đủ nguyên thùng/hộp (giữ chuẩn scale 2
				// và tránh NPE)
				slXuat = xuatLoNayCoSo;
				if (sp.getGiaBanCoSo() != null && sp.getGiaBanCoSo().compareTo(BigDecimal.ZERO) > 0) {
					donGiaXuat = sp.getGiaBanCoSo();
				} else {
					donGiaXuat = qd.getGiaBan().divide(BigDecimal.valueOf(tyLe), 2, java.math.RoundingMode.HALF_UP);
				}
				// Tạo đối tượng QuyDoiDonVi cơ sở để không bao giờ bị NPE khi format/in hóa đơn
				String tenDvCoSo = (sp.getDonViCoSo() != null && !sp.getDonViCoSo().isEmpty()) ? sp.getDonViCoSo()
						: "Đơn vị";
				qdXuat = new QuyDoiDonVi("QD_CS_" + sp.getMaSanPham(), sp, new entity.DonViTinh("DVT_CS", tenDvCoSo), 1,
						true, donGiaXuat);
			} else {
				slXuat = xuatLoNayCoSo;
			}

			String maCT = hoaDonHienTai.getMaHoaDon() + "_SP" + (hoaDonHienTai.getDsChiTiet().size() + 1);
			ChiTietXuatLo ctXuatLo = new ChiTietXuatLo(maCT, hoaDonHienTai, slXuat, donGiaXuat, lo, qdXuat);
			ctXuatLo.setSoLuongXuatCoSo(xuatLoNayCoSo);
			ctXuatLo.setThanhTien(donGiaXuat.multiply(BigDecimal.valueOf(slXuat)));
			hoaDonHienTai.addChiTiet(ctXuatLo);

			conLaiCoSo -= xuatLoNayCoSo;
			if (conLaiCoSo <= 0) {
				break; // FEFO: stop when done
			}
		}

		return true;
	}

	/**
	 * Thêm dịch vụ thú cưng vào hóa đơn
	 */
	// Thêm dịch vụ chăm sóc thú cưng vào hóa đơn và tự động trích hoa hồng 10% cho
	// kỹ thuật viên
	public boolean themDichVu(DichVu dv, ThuCung tc, NhanVien ktv) {
		if (dv == null || tc == null) {
			throw new IllegalArgumentException("Vui lòng chọn dịch vụ và thú cưng hợp lệ!");
		}

		String maCT = hoaDonHienTai.getMaHoaDon() + "_DV" + (hoaDonHienTai.getDsChiTiet().size() + 1);

		// trích 10% hoa hồng cho bạn làm spa
		BigDecimal hoaHong = dv.getGiaDichVu().multiply(BigDecimal.valueOf(0.10));

		// thêm vào bill
		ChiTietDichVu ctDv = new ChiTietDichVu(maCT, hoaDonHienTai, 1, dv.getGiaDichVu(), dv, tc, ktv, hoaHong);

		hoaDonHienTai.addChiTiet(ctDv);
		return true;
	}

	// Áp dụng số tiền giảm giá (từ Voucher khuyến mãi hoặc chiết khấu điểm tích
	// lũy)
	public void apDungGiamGia(BigDecimal giamGia) {
		this.hoaDonHienTai.setGiamGia(giamGia);
	}

	/**
	 * Xác nhận thanh toán hóa đơn
	 */
	// Xác nhận thanh toán: ghi nhận phương thức, cập nhật thời gian và kích hoạt
	// ACID transaction lưu CSDL
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
	 * Tra cứu nhanh sản phẩm bán lẻ theo Mã vạch (Barcode EAN-13) hoặc Mã sản phẩm
	 * (VD: SP001).
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
		if (soLuong <= 0 || donGia < 0)
			return 0L;
		return (long) soLuong * donGia;
	}

	public static long tinhGiamGiaVIP(long tongTienHang, boolean isVIP) {
		if (!isVIP || tongTienHang <= 0)
			return 0L;
		return BigDecimal.valueOf(tongTienHang).multiply(new BigDecimal("0.05"))
				.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
	}

	public static long tinhTienGiamTuDiem(int soDiemMuonDung, int diemHienCo, long tongTienSauVIP) {
		if (soDiemMuonDung <= 0 || diemHienCo <= 0 || tongTienSauVIP <= 0)
			return 0L;
		int diemHopLe = Math.max(0, Math.min(soDiemMuonDung, diemHienCo));
		return Math.min(tongTienSauVIP, diemHopLe * 1000L);
	}

	public static long tinhTongThanhToan(long tongTienHang, long tienGiamVIP, long tienDoiDiem,
			long tienKhauTruDoiTra) {
		return TaiChinhHoaDonBus
				.tinhThanhToan(BigDecimal.valueOf(tongTienHang), BigDecimal.valueOf(tienGiamVIP),
						BigDecimal.valueOf(tienDoiDiem), BigDecimal.valueOf(tienKhauTruDoiTra))
				.getTienPhaiThanhToan().longValueExact();
	}

	public static int tinhDiemThuongMoi(long tongThanhToan) {
		if (tongThanhToan <= 0)
			return 0;
		return (int) (tongThanhToan / 100000L);
	}

	private void unusedEndMarker() {
	}

	// note lại: tính thành tiền chi tiết (đơn giá x số lượng), tách khỏi entity
	// ChiTietDichVu và ChiTietHoaDon
	public static java.math.BigDecimal tinhThanhTien(java.math.BigDecimal donGia, int soLuong) {
		if (donGia == null || soLuong <= 0)
			return java.math.BigDecimal.ZERO;
		return donGia.multiply(java.math.BigDecimal.valueOf(soLuong));
	}

	// note lại: tính số lượng quy đổi ra đơn vị cơ sở để trừ tồn kho, tách khỏi
	// entity ChiTietXuatLo
	public static int tinhSoLuongXuatCoSo(int soLuong, int tyLeQuyDoi) {
		return Math.max(0, soLuong) * Math.max(1, tyLeQuyDoi);
	}

	// note lại: tính tổng tiền hóa đơn sau khi trừ giảm giá, đổi điểm và bù trừ đổi
	// trả
	public static void tinhTongTienHoaDon(entity.HoaDon hd) {
		if (hd == null)
			return;
		java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
		if (hd.getDsChiTiet() != null) {
			for (entity.ChiTietHoaDon ct : hd.getDsChiTiet()) {
				if (ct != null && ct.getThanhTien() != null) {
					tong = tong.add(ct.getThanhTien());
				}
			}
		}
		java.math.BigDecimal giam = hd.getGiamGia() != null ? hd.getGiamGia() : java.math.BigDecimal.ZERO;
		java.math.BigDecimal doiDiem = hd.getTienDoiDiem() != null ? hd.getTienDoiDiem() : java.math.BigDecimal.ZERO;
		java.math.BigDecimal buTru = hd.getTienBuTruDoiTra() != null ? hd.getTienBuTruDoiTra()
				: java.math.BigDecimal.ZERO;
		java.math.BigDecimal thanhTien = tong.subtract(giam).subtract(doiDiem).subtract(buTru);
		if (thanhTien.compareTo(java.math.BigDecimal.ZERO) < 0) {
			thanhTien = java.math.BigDecimal.ZERO;
		}
		hd.setThanhTien(thanhTien);
	}
}
