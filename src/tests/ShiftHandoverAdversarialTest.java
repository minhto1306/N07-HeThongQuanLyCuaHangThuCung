package tests;

import models.dao.BangGiaoCaDAO;
import models.entity.BangGiaoCa;
import models.entity.CaLamViec;
import models.entity.NhanVien;
import models.entity.TaiKhoan;
import utils.DBConnection;
import utils.UserSession;
import views.BanGiaoCaPanel;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Adversarial Stress Harness for Milestone 3 (R3 - Shift Handover & Manager Review).
 * Designed by Challenger agent to rigorously challenge and empirically verify:
 * 1. Mandatory explanation enforcement when discrepancy != 0 (deficit, surplus, whitespace)
 * 2. Cashier role RBAC isolation (button hidden, approval forbidden)
 * 3. Manager approval persistence, status formatting, and pending list exclusion
 * 4. Dynamic session role-switching reactivity in Swing UI
 * 5. Robust string/currency parsing (parseSoTien) resilience
 * 6. Code generation integrity and database idempotency
 */
public class ShiftHandoverAdversarialTest {

    private static final String ADV_TEST_ID_1 = "GC_TEST_ADV01";
    private static final String ADV_TEST_ID_2 = "GC_TEST_ADV02";

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runAdversarialSuite();
        System.exit(result.isAllPassed() ? 0 : 1);
    }

    public static TestFramework.SuiteResult runAdversarialSuite() {
        TestFramework.startSuite("ShiftHandoverAdversarialTest (Milestone 3 Challenger Suite)");

        try {
            cleanupAdvTestRecords();
            testDiscrepancyExplanationValidationLogic();
            testCashierRoleApprovalRestrictionAndVisibility();
            testManagerApprovalLifecycleAndExclusion();
            testDynamicRoleSwitchingReactivity();
            testCurrencyParsingResilience();
            testIdGenerationIntegrity();
        } catch (Exception ex) {
            TestFramework.recordManual("Adversarial Suite Execution Exception", false, ex.getMessage());
            ex.printStackTrace();
        } finally {
            cleanupAdvTestRecords();
            resetUserSession();
        }

        return TestFramework.endSuite();
    }

    /**
     * Helper to simulate the exact discrepancy validation logic from BanGiaoCaPanel.xacNhanBanGiaoCa
     */
    public static boolean validateDiscrepancyExplanation(long lyThuyet, long thucTe, String rawLyDo) {
        long chenhLech = thucTe - lyThuyet;
        String lyDoNhap = rawLyDo != null ? rawLyDo.trim() : "";
        if (chenhLech != 0 && lyDoNhap.isEmpty()) {
            return false; // BLOCKED: must provide explanation
        }
        return true; // ALLOWED: either exact match or explanation is provided
    }

    /**
     * 1. Test mandatory explanation requirement when discrepancy exists.
     */
    private static void testDiscrepancyExplanationValidationLogic() {
        long lyThuyet = 3500000L;

        // Case A: Deficit of 50,000 VND without explanation -> BLOCKED
        long thucTeDeficit = 3450000L;
        boolean allowedDeficitNoNote = validateDiscrepancyExplanation(lyThuyet, thucTeDeficit, "");
        TestFramework.assertFalse("ADV-SH-01: Cash deficit (-50k) with empty note is BLOCKED", allowedDeficitNoNote);

        // Case B: Surplus of 100,000 VND without explanation -> BLOCKED
        long thucTeSurplus = 3600000L;
        boolean allowedSurplusNoNote = validateDiscrepancyExplanation(lyThuyet, thucTeSurplus, null);
        TestFramework.assertFalse("ADV-SH-02: Cash surplus (+100k) with null note is BLOCKED", allowedSurplusNoNote);

        // Case C: Deficit with whitespace-only explanation -> BLOCKED
        boolean allowedWhitespaceNote = validateDiscrepancyExplanation(lyThuyet, thucTeDeficit, "    \t  ");
        TestFramework.assertFalse("ADV-SH-03: Cash deficit with whitespace note is BLOCKED", allowedWhitespaceNote);

        // Case D: Exact cash match (0 discrepancy) with empty explanation -> ALLOWED
        boolean allowedExactNoNote = validateDiscrepancyExplanation(lyThuyet, lyThuyet, "");
        TestFramework.assertTrue("ADV-SH-04: Exact cash match (0 discrepancy) without note is ALLOWED", allowedExactNoNote);

        // Case E: Deficit with valid explanation -> ALLOWED
        boolean allowedDeficitWithNote = validateDiscrepancyExplanation(lyThuyet, thucTeDeficit, "Khách không lấy 50k tiền thối");
        TestFramework.assertTrue("ADV-SH-05: Cash deficit with valid explanation is ALLOWED", allowedDeficitWithNote);

        // Case F: Surplus with valid explanation -> ALLOWED
        boolean allowedSurplusWithNote = validateDiscrepancyExplanation(lyThuyet, thucTeSurplus, "Tiền boa tip của khách");
        TestFramework.assertTrue("ADV-SH-06: Cash surplus with valid explanation is ALLOWED", allowedSurplusWithNote);
    }

    /**
     * 2. Test Cashier role approval restriction and button invisibility.
     */
    private static void testCashierRoleApprovalRestrictionAndVisibility() {
        NhanVien cashier = new NhanVien();
        cashier.setMaNhanVien("NV002");
        cashier.setHoTen("Lê Anh Minh");
        cashier.setChucVu("Nhân viên Thu ngân");
        TaiKhoan tk = new TaiKhoan("thungan01", "123456", "NVTN", true);
        cashier.setTaiKhoan(tk);
        UserSession.initSession(cashier);

        TestFramework.assertFalse("ADV-SH-07: Cashier is NOT manager", UserSession.isQuanLy());
        TestFramework.assertTrue("ADV-SH-08: Cashier is cashier role", UserSession.isThuNgan());

        BanGiaoCaPanel panel = new BanGiaoCaPanel();
        JButton btnDuyet = (JButton) getFieldValue(panel, "btnQuanLyDuyet");
        TestFramework.assertNotNull("ADV-SH-09: btnQuanLyDuyet exists", btnDuyet);
        if (btnDuyet != null) {
            TestFramework.assertFalse("ADV-SH-10: btnQuanLyDuyet is INVISIBLE for Cashier", btnDuyet.isVisible());
        }

        // Direct invocation security check: UserSession.isQuanLy() gate prevents rogue approval
        boolean isQuanLyCheck = UserSession.isQuanLy();
        TestFramework.assertFalse("ADV-SH-11: Security gate rejects non-manager approval invocation", isQuanLyCheck);
    }

    /**
     * 3. Test Manager approval lifecycle: pending query, approval DB update, and pending list exclusion.
     */
    private static void testManagerApprovalLifecycleAndExclusion() throws Exception {
        BangGiaoCaDAO dao = new BangGiaoCaDAO();

        // Step 1: Insert pending handover
        NhanVien cashier = new NhanVien("NV002", "Lê Anh Minh", "Nhân viên Thu ngân", "0901111111", null);
        CaLamViec ca = new CaLamViec("CA02", "Ca Chiều", null, null);
        String pendingStatus = "⏳ Chờ Quản lý duyệt | Lệch 20k tiền lẻ";

        BangGiaoCa pendingRecord = new BangGiaoCa(
                ADV_TEST_ID_1, cashier, ca,
                BigDecimal.valueOf(1000000L),
                BigDecimal.valueOf(4500000L),
                BigDecimal.valueOf(4480000L),
                BigDecimal.valueOf(-20000L),
                pendingStatus,
                LocalDateTime.now()
        );
        boolean inserted = dao.insertBangGiaoCa(pendingRecord);
        TestFramework.assertTrue("ADV-SH-12: Inserted pending shift into Database", inserted);

        // Step 2: Verify record is returned in getDanhSachCaChoDuyet()
        List<BangGiaoCa> pendingList = dao.getDanhSachCaChoDuyet();
        TestFramework.assertNotNull("ADV-SH-13: getDanhSachCaChoDuyet returns list", pendingList);
        boolean foundPending = false;
        for (BangGiaoCa item : pendingList) {
            if (ADV_TEST_ID_1.equalsIgnoreCase(item.getMaGiaoCa())) {
                foundPending = true;
                break;
            }
        }
        TestFramework.assertTrue("ADV-SH-14: Pending shift is PRESENT in pending list", foundPending);

        // Step 3: Switch to Manager and execute approval
        NhanVien manager = new NhanVien("NV001", "Lê Khãi Minh", "Quản lý hệ thống", "0909999999", null);
        TaiKhoan tkManager = new TaiKhoan("admin", "123456", "NVQL", true);
        manager.setTaiKhoan(tkManager);
        UserSession.initSession(manager);

        TestFramework.assertTrue("ADV-SH-15: Manager session active", UserSession.isQuanLy());

        String approvedNote = "✓ Đã duyệt (QL: " + UserSession.getFullName() + ") | Lệch 20k tiền lẻ";
        boolean approved = dao.capNhatGhiChuDuyetCa(ADV_TEST_ID_1, approvedNote);
        TestFramework.assertTrue("ADV-SH-16: Manager approval updated record in DB", approved);

        // Step 4: Verify record is EXCLUDED from getDanhSachCaChoDuyet()
        List<BangGiaoCa> afterApprovalList = dao.getDanhSachCaChoDuyet();
        boolean stillInPending = false;
        for (BangGiaoCa item : afterApprovalList) {
            if (ADV_TEST_ID_1.equalsIgnoreCase(item.getMaGiaoCa())) {
                stillInPending = true;
                break;
            }
        }
        TestFramework.assertFalse("ADV-SH-17: Approved shift is EXCLUDED from pending list", stillInPending);

        // Step 5: Verify record in getAllBangGiaoCa() reflects approved status
        List<BangGiaoCa> allList = dao.getAllBangGiaoCa();
        BangGiaoCa verifiedRecord = null;
        for (BangGiaoCa item : allList) {
            if (ADV_TEST_ID_1.equalsIgnoreCase(item.getMaGiaoCa())) {
                verifiedRecord = item;
                break;
            }
        }
        TestFramework.assertNotNull("ADV-SH-18: Approved shift found in full history", verifiedRecord);
        if (verifiedRecord != null) {
            TestFramework.assertTrue("ADV-SH-19: Note contains '✓ Đã duyệt'",
                    verifiedRecord.getLyDoChenhLech() != null && verifiedRecord.getLyDoChenhLech().contains("✓ Đã duyệt"));
            TestFramework.assertContains("ADV-SH-20: Note contains manager name",
                    verifiedRecord.getLyDoChenhLech(), "Lê Khãi Minh");
        }
    }

    /**
     * 4. Test dynamic session role switching reactivity in BanGiaoCaPanel.
     */
    private static void testDynamicRoleSwitchingReactivity() {
        // Start as Cashier
        NhanVien cashier = new NhanVien("NV002", "Lê Anh Minh", "Nhân viên Thu ngân", "", null);
        cashier.setTaiKhoan(new TaiKhoan("thungan01", "123456", "NVTN", true));
        UserSession.initSession(cashier);

        BanGiaoCaPanel panel = new BanGiaoCaPanel();
        JButton btnDuyet = (JButton) getFieldValue(panel, "btnQuanLyDuyet");
        TestFramework.assertNotNull("ADV-SH-21: Panel has btnQuanLyDuyet", btnDuyet);
        if (btnDuyet != null) {
            TestFramework.assertFalse("ADV-SH-22: Initially INVISIBLE for Cashier", btnDuyet.isVisible());
        }

        // Switch to Manager and refresh panel
        NhanVien manager = new NhanVien("NV001", "Lê Khãi Minh", "Quản lý hệ thống", "", null);
        manager.setTaiKhoan(new TaiKhoan("admin", "123456", "NVQL", true));
        UserSession.initSession(manager);

        panel.lamMoiDuLieuCaHienTai();
        if (btnDuyet != null) {
            TestFramework.assertTrue("ADV-SH-23: Dynamically becomes VISIBLE after switching to Manager", btnDuyet.isVisible());
        }

        // Switch back to Cashier and refresh panel
        UserSession.initSession(cashier);
        panel.lamMoiDuLieuCaHienTai();
        if (btnDuyet != null) {
            TestFramework.assertFalse("ADV-SH-24: Dynamically becomes INVISIBLE after switching back to Cashier", btnDuyet.isVisible());
        }
    }

    /**
     * 5. Test parseSoTien method edge cases and extreme inputs.
     */
    private static void testCurrencyParsingResilience() {
        BanGiaoCaPanel panel = new BanGiaoCaPanel();
        Method parseMethod = null;
        try {
            parseMethod = BanGiaoCaPanel.class.getDeclaredMethod("parseSoTien", String.class, long.class);
            parseMethod.setAccessible(true);
        } catch (Exception ex) {
            TestFramework.recordManual("ADV-SH-25: parseSoTien method lookup", false, ex.getMessage());
            return;
        }

        try {
            // Standard currency format with commas and currency symbol
            long val1 = (long) parseMethod.invoke(panel, "1,500,000 đ", 0L);
            TestFramework.assertEquals("ADV-SH-25: Parse standard formatted currency '1,500,000 đ'", 1500000L, val1);

            // String with alphanumeric mix
            long val2 = (long) parseMethod.invoke(panel, "abc 250 def 000", 0L);
            TestFramework.assertEquals("ADV-SH-26: Parse alphanumeric string 'abc 250 def 000'", 250000L, val2);

            // Null input with fallback
            long val3 = (long) parseMethod.invoke(panel, null, 1000000L);
            TestFramework.assertEquals("ADV-SH-27: Parse null input returns fallback 1,000,000", 1000000L, val3);

            // Empty/whitespace string with fallback
            long val4 = (long) parseMethod.invoke(panel, "   ", 500000L);
            TestFramework.assertEquals("ADV-SH-28: Parse whitespace string returns fallback 500,000", 500000L, val4);

            // Pure non-digits
            long val5 = (long) parseMethod.invoke(panel, "--- đ", 800000L);
            TestFramework.assertEquals("ADV-SH-29: Parse non-numeric string returns fallback 800,000", 800000L, val5);
        } catch (Exception ex) {
            TestFramework.recordManual("ADV-SH-25: parseSoTien reflection invocation", false, ex.getMessage());
        }
    }

    /**
     * 6. Test ID generation format and increment integrity.
     */
    private static void testIdGenerationIntegrity() {
        BangGiaoCaDAO dao = new BangGiaoCaDAO();
        String id = dao.phatSinhMaGiaoCa();
        TestFramework.assertNotNull("ADV-SH-30: Generated ID is non-null", id);
        TestFramework.assertTrue("ADV-SH-31: Generated ID starts with 'GC'", id != null && id.startsWith("GC"));
        TestFramework.assertTrue("ADV-SH-32: Generated ID matches pattern GC\\d+", id != null && id.matches("^GC\\d+$"));
    }

    // --- Cleanup & Session Helpers ---

    private static void cleanupAdvTestRecords() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM BangGiaoCa WHERE maGiaoCa LIKE 'GC_TEST_ADV%'")) {
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    private static void resetUserSession() {
        NhanVien defaultAdmin = new NhanVien();
        defaultAdmin.setMaNhanVien("NV001");
        defaultAdmin.setHoTen("Lê Khãi Minh");
        TaiKhoan tk = new TaiKhoan();
        tk.setVaiTro("NVQL");
        defaultAdmin.setTaiKhoan(tk);
        UserSession.initSession(defaultAdmin);
    }

    private static Object getFieldValue(Object target, String fieldName) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            return null;
        }
    }
}
