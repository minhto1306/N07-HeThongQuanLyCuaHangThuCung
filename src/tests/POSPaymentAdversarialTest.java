package tests;

import views.BanHangPOSPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Adversarial Stress Harness for Milestone 1 (R1 - POS Payment & Change Logic).
 * Designed by Challenger agent to rigorously probe edge cases, boundary conditions,
 * input variations, and state transitions.
 */
public class POSPaymentAdversarialTest {

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runAdversarialSuite();
        System.exit(result.isAllPassed() ? 0 : 1);
    }

    public static TestFramework.SuiteResult runAdversarialSuite() {
        TestFramework.startSuite("POSPaymentAdversarialTest (Milestone 1 Challenger Suite)");

        try {
            testBoundaryDeficitConditions();
            testExtremeValuesAndOverflow();
            testRealTimeUIStateTransitions();
            testDropdownSwitchingAndButtonOverrides();
            testPreservationOfCashInputDuringCartUpdates();
            testEmptyAndMalformedInputs();
        } catch (Exception ex) {
            TestFramework.recordManual("Adversarial Suite Execution Exception", false, ex.getMessage());
            ex.printStackTrace();
        }

        return TestFramework.endSuite();
    }

    /**
     * 1. Test extreme boundary conditions (exact, deficit -1, surplus +1).
     */
    private static void testBoundaryDeficitConditions() {
        long due = 100000L;

        // Boundary -1 VND
        TestFramework.assertFalse("ADV-01: Cash deficit by 1 VND (99,999 < 100,000) must be BLOCKED",
                POSPaymentTest.validateCashPayment("Tiền mặt", due, 99999L));

        // Exact match
        TestFramework.assertTrue("ADV-02: Exact cash match (100,000 == 100,000) must be ALLOWED",
                POSPaymentTest.validateCashPayment("Tiền mặt", due, 100000L));
        TestFramework.assertEquals("ADV-03: Change for exact match must be 0",
                0L, POSPaymentTest.calculateChange(due, 100000L));

        // Boundary +1 VND
        TestFramework.assertTrue("ADV-04: Surplus by 1 VND (100,001 > 100,000) must be ALLOWED",
                POSPaymentTest.validateCashPayment("Tiền mặt", due, 100001L));
        TestFramework.assertEquals("ADV-05: Change for +1 VND must be 1",
                1L, POSPaymentTest.calculateChange(due, 100001L));

        // Zero total due
        TestFramework.assertTrue("ADV-06: Zero due with 0 cash must be ALLOWED",
                POSPaymentTest.validateCashPayment("Tiền mặt", 0L, 0L));
        TestFramework.assertEquals("ADV-07: Change for zero due is 0",
                0L, POSPaymentTest.calculateChange(0L, 0L));
    }

    /**
     * 2. Test large values and non-overflowing arithmetic.
     */
    private static void testExtremeValuesAndOverflow() {
        long hugeDue = 500000000L; // 500 million VND
        long hugeCash = 1000000000L; // 1 billion VND

        TestFramework.assertTrue("ADV-08: 1 Billion VND cash for 500M VND order is ALLOWED",
                POSPaymentTest.validateCashPayment("Tiền mặt", hugeDue, hugeCash));
        TestFramework.assertEquals("ADV-09: Change for 1 Billion on 500M is exactly 500 Million",
                500000000L, POSPaymentTest.calculateChange(hugeDue, hugeCash));

        long hugeDeficitCash = 499999999L;
        TestFramework.assertFalse("ADV-10: 499,999,999 VND for 500M VND order is BLOCKED",
                POSPaymentTest.validateCashPayment("Tiền mặt", hugeDue, hugeDeficitCash));
    }

    /**
     * 3. Test real-time UI state transitions on BanHangPOSPanel.
     */
    private static void testRealTimeUIStateTransitions() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) {
            TestFramework.recordManual("ADV-11: Instantiate BanHangPOSPanel", false, "Failed to create panel");
            return;
        }

        setFieldValue(panel, "tongTienHang", 450000L);
        setFieldValue(panel, "tienGiamGia", 50000L); // canTra = 400,000 đ

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        // Case: Customer pays 300,000 đ for 400,000 đ order
        txtTienDua.setText("300000");
        invokeMethod(panel, "tinhTienThoi");
        String shortageText = lblThoi.getText();
        TestFramework.assertTrue("ADV-11: Shortage text indicates missing amount",
                shortageText.contains("Chưa đủ") && shortageText.contains("100.000"));

        // Case: Click 500k button
        JButton btn500k = findButtonByText(panel, "500k");
        TestFramework.assertNotNull("ADV-12: Button 500k exists", btn500k);
        btn500k.doClick();

        TestFramework.assertEquals("ADV-13: txtTienKhachDua is 500000 after clicking 500k",
                "500000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        String surplusText = lblThoi.getText().replaceAll("[^0-9]", "");
        TestFramework.assertEquals("ADV-14: Change is 100,000 đ when paying 500k for 400k order",
                100000L, Long.parseLong(surplusText));

        // Case: Click "Đủ tiền" button
        JButton btnTienDu = findButtonByText(panel, "Đủ tiền");
        TestFramework.assertNotNull("ADV-15: Button 'Đủ tiền' exists", btnTienDu);
        btnTienDu.doClick();

        TestFramework.assertEquals("ADV-16: txtTienKhachDua is 400000 after clicking 'Đủ tiền'",
                "400000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        TestFramework.assertEquals("ADV-17: Change is 0 đ after clicking 'Đủ tiền'",
                "0 đ", lblThoi.getText().trim());
    }

    /**
     * 4. Test dropdown switching and button overrides.
     */
    private static void testDropdownSwitchingAndButtonOverrides() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        setFieldValue(panel, "tongTienHang", 250000L);
        setFieldValue(panel, "tienGiamGia", 0L);

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JComboBox<?> cboPhuongThuc = (JComboBox<?>) getFieldValue(panel, "cboPhuongThuc");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        // Switch to "Chuyển khoản"
        cboPhuongThuc.setSelectedItem("Chuyển khoản");
        TestFramework.assertFalse("ADV-18: txtTienKhachDua is disabled during Chuyển khoản", txtTienDua.isEnabled());
        TestFramework.assertEquals("ADV-19: txtTienKhachDua matches total due (250000) for Chuyển khoản",
                "250000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        TestFramework.assertEquals("ADV-20: Change is 0 đ for Chuyển khoản", "0 đ", lblThoi.getText().trim());

        // Cashier clicks "500k" while on "Chuyển khoản"
        JButton btn500k = findButtonByText(panel, "500k");
        btn500k.doClick();

        TestFramework.assertEquals("ADV-21: Clicking denomination button automatically reverts method to 'Tiền mặt'",
                "Tiền mặt", cboPhuongThuc.getSelectedItem());
        TestFramework.assertTrue("ADV-22: txtTienKhachDua is re-enabled upon denomination click", txtTienDua.isEnabled());
        TestFramework.assertEquals("ADV-23: txtTienKhachDua is set to 500000", "500000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        TestFramework.assertEquals("ADV-24: Change is recalculated to 250,000 đ",
                250000L, Long.parseLong(lblThoi.getText().replaceAll("[^0-9]", "")));
    }

    /**
     * 5. Test that manual cash input is NOT wiped out when cart is recalculated.
     */
    private static void testPreservationOfCashInputDuringCartUpdates() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JComboBox<?> cboPhuongThuc = (JComboBox<?>) getFieldValue(panel, "cboPhuongThuc");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) getFieldValue(panel, "modelGioHang");

        cboPhuongThuc.setSelectedItem("Tiền mặt");
        txtTienDua.setText("500000");

        // Add an item row with thanhTien = 200,000 đ
        model.addRow(new Object[]{1, "SP001", "Pate Whiskas", "Gói", 2, "100.000", "200.000", "Hoàn tất"});

        // Invoke capNhatTongTien()
        invokeMethod(panel, "capNhatTongTien");

        TestFramework.assertEquals("ADV-25: Manual cash input of 500,000 is PRESERVED after cart total update",
                "500000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        TestFramework.assertEquals("ADV-26: Change dynamically updates to 300,000 đ (500k - 200k)",
                300000L, Long.parseLong(lblThoi.getText().replaceAll("[^0-9]", "")));
    }

    /**
     * 6. Test empty, spaces, and malformed non-numeric cash input resilience.
     */
    private static void testEmptyAndMalformedInputs() throws Exception {
        BanHangPOSPanel panel = createPanelInstance();
        if (panel == null) return;

        setFieldValue(panel, "tongTienHang", 100000L);
        setFieldValue(panel, "tienGiamGia", 0L);

        JTextField txtTienDua = (JTextField) getFieldValue(panel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(panel, "lblTienThoiLai");

        // Test empty string
        txtTienDua.setText("");
        invokeMethod(panel, "tinhTienThoi");
        String emptyText = lblThoi.getText();
        TestFramework.assertTrue("ADV-27: Empty cash input shows deficit without crashing",
                emptyText.contains("Chưa đủ") && emptyText.contains("100.000"));

        // Test non-numeric input "abc!@#"
        txtTienDua.setText("abc!@#");
        invokeMethod(panel, "tinhTienThoi");
        String malformedText = lblThoi.getText();
        TestFramework.assertTrue("ADV-28: Non-numeric input safely treated as 0 cash deficit",
                malformedText.contains("Chưa đủ") && malformedText.contains("100.000"));

        // Test formatted currency string "200.000 đ"
        txtTienDua.setText("200.000 đ");
        invokeMethod(panel, "tinhTienThoi");
        String formattedChangeText = lblThoi.getText().replaceAll("[^0-9]", "");
        TestFramework.assertEquals("ADV-29: Formatted string '200.000 đ' is parsed as 200000 giving 100,000 đ change",
                100000L, Long.parseLong(formattedChangeText));
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
