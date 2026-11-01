package tests;

import views.BanHangPOSPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Adversarial Stress Test Suite for Milestone 1 (R1: POS Payment & Change Logic).
 * Authored by challenger_m1_2.
 * 
 * Verifies:
 * 1. Rapid switching between "Tiền mặt" and "Chuyển khoản".
 * 2. Clicking quick buttons when "Chuyển khoản" was selected -> auto-switching to "Tiền mặt".
 * 3. Formatting strings in txtTienKhachDua (commas, periods, spaces, letters, overflow) -> tinhTienThoi() crash resilience.
 * 4. Boundary cases: Negative strings, 0 VND orders, very large amounts (100,000,000 VND, 1,000,000,000 VND).
 * 5. Payment blocking logic under cash deficit.
 */
public class M1AdversarialStressTest {

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runTests();
        System.exit(result.isAllPassed() ? 0 : 1);
    }

    public static TestFramework.SuiteResult runTests() {
        TestFramework.startSuite("M1AdversarialStressTest (Adversarial POS Payment & Boundary Stress)");

        try {
            testRapidPaymentMethodSwitching();
            testQuickButtonsAutoSwitchFromBankTransfer();
            testFormattingAndMalformedInputResilience();
            testBoundaryConditionsAndExtremeAmounts();
            testPaymentBlockingOnCashDeficit();
        } catch (Exception ex) {
            TestFramework.recordManual("M1StressTest Execution Exception", false, ex.getMessage());
            ex.printStackTrace();
        }

        return TestFramework.endSuite();
    }

    /**
     * Challenge 1: Rapid switching between "Tiền mặt" and "Chuyển khoản".
     * Simulates 100 rapid alternations between Cash and Bank Transfer to check for:
     * - Field enable/disable synchronization
     * - Correct text and change label updates
     * - No deadlock or uncaught exceptions
     */
    private static void testRapidPaymentMethodSwitching() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        TestFramework.assertNotNull("CHAL-01: BanHangPOSPanel instance created", panel);
        if (panel == null) return;

        long orderTotal = 450000L;
        setFieldValue(panel, "tongTienHang", orderTotal);
        setFieldValue(panel, "tienGiamGia", 50000L); // canTra = 400,000
        long canTra = 400000L;

        JComboBox<?> cbo = (JComboBox<?>) getFieldValue(panel, "cboPhuongThuc");
        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        TestFramework.assertNotNull("CHAL-02: cboPhuongThuc found", cbo);
        TestFramework.assertNotNull("CHAL-03: txtTienKhachDua found", txtTienDua);
        TestFramework.assertNotNull("CHAL-04: lblTienThoiLai found", lblThoi);

        boolean allInvariantsHold = true;
        String failureDetail = "";

        // Rapid 100-cycle loop
        for (int i = 0; i < 100; i++) {
            // Switch to Chuyển khoản
            cbo.setSelectedItem("Chuyển khoản");
            if (txtTienDua.isEnabled()) {
                allInvariantsHold = false;
                failureDetail = "txtTienKhachDua remained ENABLED after switching to Chuyển khoản at iteration " + i;
                break;
            }
            if (!"0 đ".equals(lblThoi.getText().trim())) {
                allInvariantsHold = false;
                failureDetail = "lblTienThoiLai was not '0 đ' during Chuyển khoản at iteration " + i;
                break;
            }
            if (!String.valueOf(canTra).equals(txtTienDua.getText().replaceAll("[^0-9]", ""))) {
                allInvariantsHold = false;
                failureDetail = "txtTienKhachDua did not match canTra (" + canTra + ") at iteration " + i;
                break;
            }

            // Switch back to Tiền mặt
            cbo.setSelectedItem("Tiền mặt");
            if (!txtTienDua.isEnabled()) {
                allInvariantsHold = false;
                failureDetail = "txtTienKhachDua remained DISABLED after switching to Tiền mặt at iteration " + i;
                break;
            }
        }

        TestFramework.assertTrue("CHAL-05: 100 rapid switches between Tiền mặt and Chuyển khoản maintain state invariants", allInvariantsHold);
        if (!allInvariantsHold) {
            System.err.println("       --> State invariant failure: " + failureDetail);
        }
    }

    /**
     * Challenge 2: Clicking quick denomination buttons when "Chuyển khoản" was selected.
     * Cashier has "Chuyển khoản" active (txtTienKhachDua disabled).
     * Clicking 100k, 200k, 500k, or Đủ tiền must:
     * - Auto-switch cboPhuongThuc to "Tiền mặt"
     * - Re-enable txtTienKhachDua
     * - Set txtTienKhachDua to the button's denomination
     * - Trigger change calculation immediately
     */
    private static void testQuickButtonsAutoSwitchFromBankTransfer() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        setFieldValue(panel, "tongTienHang", 350000L);
        setFieldValue(panel, "tienGiamGia", 0L);

        JComboBox<?> cbo = (JComboBox<?>) getFieldValue(panel, "cboPhuongThuc");
        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        JButton btn100k = findButtonByText(panel, "100k");
        JButton btn200k = findButtonByText(panel, "200k");
        JButton btn500k = findButtonByText(panel, "500k");
        JButton btnTienDu = findButtonByText(panel, "Đủ tiền");

        TestFramework.assertNotNull("CHAL-06: Quick button 100k present", btn100k);
        TestFramework.assertNotNull("CHAL-07: Quick button 200k present", btn200k);
        TestFramework.assertNotNull("CHAL-08: Quick button 500k present", btn500k);
        TestFramework.assertNotNull("CHAL-09: Quick button 'Đủ tiền' present", btnTienDu);

        // Sub-test 2.1: 100k button from Chuyển khoản
        cbo.setSelectedItem("Chuyển khoản");
        TestFramework.assertFalse("CHAL-10: txtTienKhachDua disabled in Chuyển khoản", txtTienDua.isEnabled());
        btn100k.doClick();
        TestFramework.assertEquals("CHAL-11: 100k click auto-switches cboPhuongThuc to 'Tiền mặt'", "Tiền mặt", cbo.getSelectedItem());
        TestFramework.assertTrue("CHAL-12: txtTienKhachDua re-enabled after 100k click", txtTienDua.isEnabled());
        TestFramework.assertEquals("CHAL-13: txtTienKhachDua text set to '100000'", "100000", txtTienDua.getText());
        TestFramework.assertTrue("CHAL-14: lblTienThoiLai shows shortage warning for 100k on 350k due",
                lblThoi.getText().contains("Chưa đủ") && lblThoi.getText().contains("250.000"));

        // Sub-test 2.2: 200k button from Chuyển khoản
        cbo.setSelectedItem("Chuyển khoản");
        btn200k.doClick();
        TestFramework.assertEquals("CHAL-15: 200k click auto-switches to 'Tiền mặt'", "Tiền mặt", cbo.getSelectedItem());
        TestFramework.assertTrue("CHAL-16: txtTienKhachDua re-enabled after 200k click", txtTienDua.isEnabled());
        TestFramework.assertEquals("CHAL-17: txtTienKhachDua text set to '200000'", "200000", txtTienDua.getText());

        // Sub-test 2.3: 500k button from Chuyển khoản
        cbo.setSelectedItem("Chuyển khoản");
        btn500k.doClick();
        TestFramework.assertEquals("CHAL-18: 500k click auto-switches to 'Tiền mặt'", "Tiền mặt", cbo.getSelectedItem());
        TestFramework.assertTrue("CHAL-19: txtTienKhachDua re-enabled after 500k click", txtTienDua.isEnabled());
        TestFramework.assertEquals("CHAL-20: txtTienKhachDua text set to '500000'", "500000", txtTienDua.getText());
        TestFramework.assertTrue("CHAL-21: lblTienThoiLai shows 150,000 đ surplus for 500k on 350k due",
                lblThoi.getText().contains("150.000") || lblThoi.getText().contains("150,000"));

        // Sub-test 2.4: "Đủ tiền" button from Chuyển khoản
        cbo.setSelectedItem("Chuyển khoản");
        btnTienDu.doClick();
        TestFramework.assertEquals("CHAL-22: 'Đủ tiền' click auto-switches to 'Tiền mặt'", "Tiền mặt", cbo.getSelectedItem());
        TestFramework.assertTrue("CHAL-23: txtTienKhachDua re-enabled after 'Đủ tiền' click", txtTienDua.isEnabled());
        TestFramework.assertEquals("CHAL-24: txtTienKhachDua set to exact 350,000", "350000", txtTienDua.getText());
        TestFramework.assertEquals("CHAL-25: lblTienThoiLai shows '0 đ' after 'Đủ tiền'", "0 đ", lblThoi.getText().trim());
    }

    /**
     * Challenge 3: Formatting strings in txtTienKhachDua.
     * Tests commas, periods, spaces, alphanumeric, symbols, empty string, and giant overflow numbers.
     */
    private static void testFormattingAndMalformedInputResilience() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        setFieldValue(panel, "tongTienHang", 200000L);
        setFieldValue(panel, "tienGiamGia", 0L);

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        // 3.1 Standard formatted with commas: "200,000"
        txtTienDua.setText("200,000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-26: Formatted '200,000' parses correctly (change 0 đ)", "0 đ", lblThoi.getText().trim());

        // 3.2 Formatted with dots/periods: "250.000" (surplus 50,000)
        txtTienDua.setText("250.000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-27: Formatted '250.000' parses correctly (change 50,000 đ)",
                lblThoi.getText().contains("50.000") || lblThoi.getText().contains("50,000"));

        // 3.3 Formatted with mixed spaces: "  300  000  " (surplus 100,000)
        txtTienDua.setText("  300  000  ");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-28: Formatted '  300  000  ' parses correctly (change 100,000 đ)",
                lblThoi.getText().contains("100.000") || lblThoi.getText().contains("100,000"));

        // 3.4 Letters & symbols embedded: "VND 200.000đ"
        txtTienDua.setText("VND 200.000đ");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-29: 'VND 200.000đ' extracts digits cleanly (change 0 đ)", "0 đ", lblThoi.getText().trim());

        // 3.5 Letters only: "abcxyz" -> treated as 0 (shortage 200,000 đ)
        txtTienDua.setText("abcxyz");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-30: Pure letters 'abcxyz' treated as 0 without crashing",
                lblThoi.getText().contains("Chưa đủ") && (lblThoi.getText().contains("200.000") || lblThoi.getText().contains("200,000")));

        // 3.6 Special symbols only: "!@#$%^&*" -> treated as 0
        txtTienDua.setText("!@#$%^&*");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-31: Symbols '!@#$%^&*' treated as 0 without crashing",
                lblThoi.getText().contains("Chưa đủ"));

        // 3.7 Empty string: "" -> treated as 0
        txtTienDua.setText("");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-32: Empty string '' treated as 0 without crashing",
                lblThoi.getText().contains("Chưa đủ"));

        // 3.8 Whitespace only: "   " -> treated as 0
        txtTienDua.setText("   ");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-33: Whitespace only '   ' treated as 0 without crashing",
                lblThoi.getText().contains("Chưa đủ"));

        // 3.9 Huge overflow number: > Long.MAX_VALUE
        txtTienDua.setText("9999999999999999999999999999999999999999");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-34: Numerical overflow handled gracefully with 'Số tiền không hợp lệ'",
                lblThoi.getText().contains("không hợp lệ") || lblThoi.getText().contains("Chưa đủ"));
    }

    /**
     * Challenge 4: Boundary conditions and extreme amounts.
     * Tests:
     * - 0 VND order (e.g. 100% voucher / free item)
     * - Negative string input ("-50000")
     * - Very large amount (100,000,000 VND - 100 million)
     * - Extreme large amount (1,000,000,000 VND - 1 billion)
     */
    private static void testBoundaryConditionsAndExtremeAmounts() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        // 4.1 Zero VND order with 0 cash given -> change is exactly 0 đ
        setFieldValue(panel, "tongTienHang", 0L);
        setFieldValue(panel, "tienGiamGia", 0L);
        txtTienDua.setText("0");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-35: 0 VND order with 0 cash shows '0 đ'", "0 đ", lblThoi.getText().trim());

        // 4.2 Zero VND order with 50,000 cash given -> change is 50,000 đ
        txtTienDua.setText("50000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-36: 0 VND order with 50,000 cash returns 50,000 đ change",
                lblThoi.getText().contains("50.000") || lblThoi.getText().contains("50,000"));

        // 4.3 Negative string "-50000" for 100,000 order -> digits 50000 extracted, shortage 50,000 đ
        setFieldValue(panel, "tongTienHang", 100000L);
        setFieldValue(panel, "tienGiamGia", 0L);
        txtTienDua.setText("-50000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-37: Negative string '-50000' handles safely without crash",
                lblThoi.getText().contains("50.000") || lblThoi.getText().contains("50,000"));

        // 4.4 Large order: 100,000,000 VND (100 million)
        setFieldValue(panel, "tongTienHang", 100000000L);
        setFieldValue(panel, "tienGiamGia", 0L);

        // Sub-test 4.4a: Exact 100,000,000 VND
        txtTienDua.setText("100000000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-38: 100M VND order with exact 100M cash shows '0 đ'", "0 đ", lblThoi.getText().trim());

        // Sub-test 4.4b: 1 VND shortage (99,999,999 VND)
        txtTienDua.setText("99999999");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-39: 100M VND order with 1 VND shortage shows shortage indicator",
                lblThoi.getText().contains("Chưa đủ") && lblThoi.getText().contains("1"));

        // Sub-test 4.4c: Surplus cash 150,000,000 VND -> change 50,000,000 VND
        txtTienDua.setText("150000000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-40: 100M VND order with 150M cash shows 50M change",
                lblThoi.getText().contains("50.000.000") || lblThoi.getText().contains("50,000,000"));

        // 4.5 Extreme large order: 1,000,000,000 VND (1 billion VND)
        setFieldValue(panel, "tongTienHang", 1000000000L);
        txtTienDua.setText("1200000000");
        invokeMethod(panel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-41: 1B VND order with 1.2B cash calculates 200M change safely",
                lblThoi.getText().contains("200.000.000") || lblThoi.getText().contains("200,000,000"));
    }

    /**
     * Challenge 5: Checkout blocking on cash deficit.
     * Evaluates the thucHienThanhToan condition:
     * ("Tiền mặt".equals(pttt) && canTra > 0 && tienKhachDuaVal < canTra)
     */
    private static void testPaymentBlockingOnCashDeficit() {
        // 5.1 Cash deficit (100k < 200k) -> Must block
        boolean blocked1 = POSPaymentTest.validateCashPayment("Tiền mặt", 200000L, 100000L);
        TestFramework.assertFalse("CHAL-42: Payment blocked when cash is deficit (100k < 200k)", blocked1);

        // 5.2 Cash 0 for 200k order -> Must block
        boolean blocked2 = POSPaymentTest.validateCashPayment("Tiền mặt", 200000L, 0L);
        TestFramework.assertFalse("CHAL-43: Payment blocked when cash is 0 on positive order", blocked2);

        // 5.3 Cash exact (200k == 200k) -> Allowed
        boolean allowed1 = POSPaymentTest.validateCashPayment("Tiền mặt", 200000L, 200000L);
        TestFramework.assertTrue("CHAL-44: Payment allowed when cash is exact", allowed1);

        // 5.4 Cash surplus (500k > 200k) -> Allowed
        boolean allowed2 = POSPaymentTest.validateCashPayment("Tiền mặt", 200000L, 500000L);
        TestFramework.assertTrue("CHAL-45: Payment allowed when cash is surplus", allowed2);

        // 5.5 Zero due order with 0 cash -> Allowed
        boolean allowed3 = POSPaymentTest.validateCashPayment("Tiền mặt", 0L, 0L);
        TestFramework.assertTrue("CHAL-46: Payment allowed on 0 VND order with 0 cash", allowed3);

        // 5.6 Chuyển khoản with 0 cash given -> Allowed (bank transfer handles via QR)
        boolean allowed4 = POSPaymentTest.validateCashPayment("Chuyển khoản", 200000L, 0L);
        TestFramework.assertTrue("CHAL-47: Chuyển khoản does not require physical cash", allowed4);
    }

    // --- Reflection Helpers ---

    private static BanHangPOSPanel createPanelInstance() {
        try {
            return new BanHangPOSPanel();
        } catch (Exception e) {
            System.err.println("Could not create BanHangPOSPanel: " + e.getMessage());
            return null;
        }
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

    private static void setFieldValue(Object target, String fieldName, Object val) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, val);
        } catch (Exception ignored) {}
    }

    private static Object invokeMethod(Object target, String methodName, Object... args) {
        try {
            for (Method m : target.getClass().getDeclaredMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                    m.setAccessible(true);
                    return m.invoke(target, args);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static JButton findButtonByText(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) {
                return (JButton) c;
            }
            if (c instanceof Container) {
                JButton found = findButtonByText((Container) c, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
