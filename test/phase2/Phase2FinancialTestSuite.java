package test.phase2;

import bus.BanHangBus;
import bus.GiaoCaBus;
import bus.TaiChinhHoaDonBus;
import java.math.BigDecimal;

/** Kiểm thử nghiệp vụ Phase 2 không kết nối hoặc thay đổi CSDL. */
public final class Phase2FinancialTestSuite {
    private static int passed, failed, notVerifiable;
    private Phase2FinancialTestSuite() {}

    public static void main(String[] args) {
        test("P2-FIN-01 tách giảm/điểm/đổi trả", () -> {
            TaiChinhHoaDonBus.KetQuaThanhToan k = TaiChinhHoaDonBus.tinhThanhToan(
                    bd("1000000"), bd("50000"), bd("20000"), bd("100000"));
            equal(bd("830000"), k.getTienPhaiThanhToan());
            equal(BigDecimal.ZERO, k.getTienHoanDoiTra());
        });
        test("P2-FIN-02 bù trừ dư tạo khoản hoàn riêng", () -> {
            TaiChinhHoaDonBus.KetQuaThanhToan k = TaiChinhHoaDonBus.tinhThanhToan(
                    bd("200000"), BigDecimal.ZERO, BigDecimal.ZERO, bd("250000"));
            equal(BigDecimal.ZERO, k.getTienPhaiThanhToan());
            equal(bd("50000"), k.getTienHoanDoiTra());
        });
        test("P2-FIN-03 ưu đãi không làm tổng âm", () -> {
            TaiChinhHoaDonBus.KetQuaThanhToan k = TaiChinhHoaDonBus.tinhThanhToan(
                    bd("100000"), bd("80000"), bd("50000"), BigDecimal.ZERO);
            equal(BigDecimal.ZERO, k.getTienPhaiThanhToan());
            equal(BigDecimal.ZERO, k.getTienHoanDoiTra());
        });
        test("P2-FIN-04 tiền thừa tiền mặt", () -> equal(bd("170000"),
                TaiChinhHoaDonBus.tinhTienThuaTienMat(bd("1000000"), bd("830000"))));
        testThrows("P2-FIN-05 từ chối tiền đưa thiếu", IllegalArgumentException.class, () ->
                TaiChinhHoaDonBus.tinhTienThuaTienMat(bd("800000"), bd("830000")));
        testThrows("P2-FIN-06 từ chối tiền âm", IllegalArgumentException.class, () ->
                TaiChinhHoaDonBus.tinhThanhToan(bd("-1"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
        test("P2-FIN-07 chuẩn hóa hoàn tiền mặt", () -> equal(TaiChinhHoaDonBus.TIEN_MAT,
                TaiChinhHoaDonBus.chuanHoaPhuongThucHoan(" tiền MẶT ", true)));
        test("P2-FIN-08 chuẩn hóa hoàn chuyển khoản", () -> equal(TaiChinhHoaDonBus.CHUYEN_KHOAN,
                TaiChinhHoaDonBus.chuanHoaPhuongThucHoan("Chuyển khoản", true)));
        test("P2-FIN-09 không hoàn thì không giả định tiền mặt", () -> equal(TaiChinhHoaDonBus.KHONG_HOAN_TIEN,
                TaiChinhHoaDonBus.chuanHoaPhuongThucHoan(null, false)));
        testThrows("P2-FIN-10 hoàn tiền bắt buộc chọn phương thức", IllegalArgumentException.class, () ->
                TaiChinhHoaDonBus.chuanHoaPhuongThucHoan(null, true));
        test("P2-FIN-11 tiền mặt lý thuyết cuối ca", () -> equal(bd("3430000"),
                GiaoCaBus.tinhTienMatLyThuyet(bd("1000000"), bd("2500000"), bd("70000"))));
        testThrows("P2-FIN-12 không cho tiền cuối ca âm", IllegalArgumentException.class, () ->
                GiaoCaBus.tinhTienMatLyThuyet(bd("100000"), BigDecimal.ZERO, bd("100001")));
        test("P2-FIN-13 giảm VIP BigDecimal", () -> equal(50001L,
                BanHangBus.tinhGiamGiaVIP(1000010L, true)));
        test("P2-FIN-14 khách thường không giảm VIP", () -> equal(0L,
                BanHangBus.tinhGiamGiaVIP(1000010L, false)));

        notVerifiable("P2-DB-01 rollback thanh toán", "Cần SQL Server test cô lập");
        notVerifiable("P2-DB-02 thanh toán đồng thời", "Cần hai kết nối SQL Server");
        notVerifiable("P2-DB-03 rollback đổi trả/kho/điểm", "Cần SQL Server test cô lập");
        notVerifiable("P2-DB-04 concurrency bàn giao ca", "Cần SQL Server test cô lập");

        System.out.printf("PHASE2_RESULT passed=%d failed=%d not_verifiable=%d%n", passed, failed, notVerifiable);
        if (failed > 0) throw new AssertionError("Phase 2 có " + failed + " test thất bại");
    }

    private static BigDecimal bd(String value) { return new BigDecimal(value); }
    private static void test(String name, CheckedRunnable action) {
        try { action.run(); passed++; System.out.println("PASS " + name); }
        catch (Throwable e) { failed++; System.out.println("FAIL " + name + " :: " + e.getMessage()); }
    }
    private static void testThrows(String name, Class<? extends Throwable> type, CheckedRunnable action) {
        test(name, () -> {
            try { action.run(); }
            catch (Throwable e) {
                if (type.isInstance(e)) return;
                throw new AssertionError("Sai loại ngoại lệ: " + e.getClass().getName(), e);
            }
            throw new AssertionError("Không phát sinh " + type.getSimpleName());
        });
    }
    private static void notVerifiable(String name, String reason) {
        notVerifiable++; System.out.println("NOT_VERIFIABLE " + name + " :: " + reason);
    }
    private static void equal(Object expected, Object actual) {
        if (expected instanceof BigDecimal && actual instanceof BigDecimal
                && ((BigDecimal) expected).compareTo((BigDecimal) actual) == 0) return;
        if (!(expected instanceof BigDecimal) && (expected == null ? actual == null : expected.equals(actual))) return;
        throw new AssertionError("expected=" + expected + ", actual=" + actual);
    }
    @FunctionalInterface private interface CheckedRunnable { void run() throws Exception; }
}
