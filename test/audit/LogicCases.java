package audit;

import bus.BanHangBus;
import bus.DoiTraBus;
import bus.GiaoCaBus;
import bus.KhoLoHangBus;
import bus.TaiChinhHoaDonBus;
import dao.HoaDonDAO;
import dao.NhapKhoDAO;
import entity.*;
import utils.PasswordHasher;
import utils.UserSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LogicCases {
    private LogicCases() {}

    static void add(List<AuditRunner.Case> cases) {
        cases.add(new AuditRunner.Case("L01", "logic", "PBKDF2, sai mật khẩu và tài khoản khóa", () -> {
            String hash = PasswordHasher.hashPBKDF2("TestPass#2026");
            AuditRunner.require(hash.startsWith("PBKDF2$"), "Sai định dạng hash");
            AuditRunner.require(PasswordHasher.verifyPassword("TestPass#2026", hash), "Mật khẩu đúng bị từ chối");
            AuditRunner.require(!PasswordHasher.verifyPassword("wrong", hash), "Mật khẩu sai được chấp nhận");
        }));
        cases.add(new AuditRunner.Case("L02", "logic", "RBAC bốn vai trò, phiên hết hạn", () -> {
            try {
                for (String role : List.of("NVQL", "NVBH", "NVTN", "NVCS")) {
                    TaiKhoan account = new TaiKhoan();
                    account.setVaiTro(role);
                    NhanVien staff = new NhanVien();
                    staff.setMaNhanVien("TEST_" + role);
                    staff.setTaiKhoan(account);
                    UserSession.initSession(staff);
                    AuditRunner.require(role.equals(UserSession.getRoleCode()), "Sai role " + role);
                    AuditRunner.require(UserSession.canAccessNhom3CapNhatNhapKho() == role.equals("NVQL"),
                            "Quyền nhập kho sai cho " + role);
                }
                UserSession.clearSession();
                AuditRunner.require(!UserSession.canAccessNhom3CapNhatNhapKho(), "Phiên hết hạn còn quyền nhập kho");
            } finally {
                UserSession.clearSession();
            }
        }));
        cases.add(new AuditRunner.Case("L03", "logic", "POS từ chối giá âm, số lượng sai và tiền tràn", () -> {
            AuditRunner.require(money("38,000 đ") == 38000, "Đọc giá định dạng sai");
            AuditRunner.rejects(IllegalArgumentException.class, () -> money("-1 đ"));
            AuditRunner.rejects(IllegalArgumentException.class, () -> money("1,2"));
            AuditRunner.rejects(IllegalArgumentException.class, () -> money("999999999999999999999"));
            AuditRunner.require(quantity("2") == 2, "Đọc số lượng sai");
            AuditRunner.rejects(IllegalArgumentException.class, () -> quantity("-2"));
            AuditRunner.rejects(IllegalArgumentException.class, () -> quantity("0"));
        }));
        cases.add(new AuditRunner.Case("L04", "logic", "Tách giảm giá, điểm, bù trừ và tiền hoàn", () -> {
            var result = TaiChinhHoaDonBus.tinhThanhToan(
                    new BigDecimal("100000"), new BigDecimal("10000"),
                    new BigDecimal("20000"), new BigDecimal("90000"));
            AuditRunner.require(result.getTienPhaiThanhToan().signum() == 0, "Tổng âm");
            AuditRunner.require(result.getTienHoanDoiTra().compareTo(new BigDecimal("20000")) == 0,
                    "Tiền hoàn dư sai");
            HoaDon bill = new HoaDon();
            ChiTietDichVu line = new ChiTietDichVu();
            line.setThanhTien(new BigDecimal("100000"));
            bill.addChiTiet(line);
            bill.setGiamGia(new BigDecimal("10000"));
            bill.setTienDoiDiem(new BigDecimal("20000"));
            bill.setTienBuTruDoiTra(new BigDecimal("30000"));
            BanHangBus.tinhTongTienHoaDon(bill);
            AuditRunner.require(bill.getThanhTien().compareTo(new BigDecimal("40000")) == 0,
                    "Tổng hóa đơn sai");
        }));
        cases.add(new AuditRunner.Case("L05", "logic", "DAO chặn dòng hóa đơn giá âm trước khi mở DB", () -> {
            HoaDon bill = new HoaDon();
            NhanVien staff = new NhanVien();
            staff.setMaNhanVien("NV_TEST");
            bill.setNhanVien(staff);
            ChiTietDichVu line = new ChiTietDichVu();
            line.setDonGiaLucBan(new BigDecimal("-1"));
            bill.addChiTiet(line);
            AuditRunner.rejects(IllegalArgumentException.class, () -> new HoaDonDAO().thanhToanHoaDon(bill));
        }));
        cases.add(new AuditRunner.Case("L06", "logic", "Nhập kho chặn bản ghi thiếu trước khi mở DB", () ->
                AuditRunner.rejects(IllegalArgumentException.class, () ->
                        new NhapKhoDAO().nhapSanPhamVaLoHang(new SanPham(), new QuyDoiDonVi(), new LoHang()))));
        cases.add(new AuditRunner.Case("L07", "logic", "FEFO bỏ lô hết hạn; quy đổi không tràn", () -> {
            LoHang fresh = new LoHang(); fresh.setHanSuDung(LocalDate.now().plusDays(10)); fresh.setSoLuongTon(5);
            LoHang expired = new LoHang(); expired.setHanSuDung(LocalDate.now().minusDays(1)); expired.setSoLuongTon(9);
            AuditRunner.require(KhoLoHangBus.tinhTongTonKhaDung(new SanPham(), List.of(fresh, expired)) == 5,
                    "Tồn khả dụng tính cả lô hết hạn");
            AuditRunner.require(BanHangBus.tinhSoLuongXuatCoSo(2, 12) == 24, "Quy đổi sai");
        }));
        cases.add(new AuditRunner.Case("L08", "logic", "Đổi trả và bàn giao tiền mặt", () -> {
            PhieuDoiTra returned = new PhieuDoiTra();
            ChiTietDoiTra line = new ChiTietDoiTra();
            line.setTienHoanLai(new BigDecimal("30000"));
            returned.addChiTietDoiTra(line);
            AuditRunner.require(DoiTraBus.tinhTongTienHoan(returned).compareTo(new BigDecimal("30000")) == 0,
                    "Tổng hoàn sai");
            AuditRunner.require(GiaoCaBus.tinhTienMatLyThuyet(
                    new BigDecimal("100000"), new BigDecimal("50000"), new BigDecimal("20000"))
                    .compareTo(new BigDecimal("130000")) == 0, "Tiền cuối ca sai");
        }));
        cases.add(new AuditRunner.Case("L09", "logic", "Script SQL có 30 bảng và filtered index barcode", () -> {
            String sql = Files.readString(Path.of("sql/HeThongQuanLyCuaHangThuCung.sql"));
            Matcher matcher = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:dbo\\.)?(\\w+)").matcher(sql);
            java.util.Set<String> tables = new java.util.HashSet<>();
            while (matcher.find()) tables.add(matcher.group(1).toLowerCase());
            AuditRunner.require(tables.size() == 30, "Số bảng script=" + tables.size());
            AuditRunner.require(sql.contains("WHERE maVach IS NOT NULL")
                    && !sql.contains("maVach NVARCHAR(30) NULL UNIQUE"), "Barcode unique sai");
        }));
    }

    private static Object call(String method, Object raw) throws Exception {
        Method m = Class.forName("gui.BanHangPOSPanel").getDeclaredMethod(method,
                method.equals("docSoLuongGioHang") ? new Class<?>[]{Object.class}
                        : new Class<?>[]{Object.class, String.class});
        m.setAccessible(true);
        try { return method.equals("docSoLuongGioHang") ? m.invoke(null, raw) : m.invoke(null, raw, "Giá"); }
        catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw e;
        }
    }
    private static long money(Object raw) throws Exception { return (Long) call("docSoTienGioHang", raw); }
    private static int quantity(Object raw) throws Exception { return (Integer) call("docSoLuongGioHang", raw); }
}
