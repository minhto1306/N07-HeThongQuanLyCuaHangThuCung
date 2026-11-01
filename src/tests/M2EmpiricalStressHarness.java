package tests;

import models.entity.DoiTraTransferData;
import utils.ThemeColors;
import views.BanHangPOSPanel;
import views.DoiTraHangPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Empirical Stress Harness for Milestone 2 (R2: Luồng đổi trả sang sản phẩm khác / thêm hàng sang POS).
 * Authored by challenger_m2_2.
 *
 * EMPIRICAL CHALLENGES:
 * 1. Rapid switching between Scenario A and Scenario B in DoiTraHangPanel (100 cycles).
 * 2. Forwarding to POS: live injection, banner visibility, and receipt state verification.
 * 3. Dynamic Cart Mutations: adding 5 products, removing 2, changing quantities.
 * 4. Cancellation handling: reject cancel vs confirm cancel [Hủy đổi trả] -> clean revert.
 * 5. Re-forwarding after cancellation: clean slate verification without cross-contamination.
 * 6. Cashier checkout: cash (deficit vs exact vs surplus) vs VietQR for net difference.
 * 7. Store refund scenario (Net Due == 0, Store owes refund).
 */
public class M2EmpiricalStressHarness {

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runStressHarness();
        System.exit(result.isAllPassed() ? 0 : 1);
    }

    public static TestFramework.SuiteResult runStressHarness() {
        TestFramework.startSuite("M2EmpiricalStressHarness (Milestone 2 Challenger Empirical Suite)");

        try {
            testRapidScenarioSwitching();
            testForwardingAndReceptionInPOS();
            testDynamicCartMutationsAtPOS();
            testCancellationRejectionAndConfirmation();
            testReForwardingAfterCancellation();
            testCashierCheckoutCashVsVietQR();
            testStoreRefundCheckout();
        } catch (Exception ex) {
            TestFramework.recordManual("M2 Stress Harness Exception", false, ex.getMessage());
            ex.printStackTrace();
        }

        return TestFramework.endSuite();
    }

    /**
     * CHALLENGE 1: Rapid switching between Scenario A and Scenario B in DoiTraHangPanel.
     * Simulates 100 rapid alternations to verify:
     * - Button text, color, icon, and dimension synchronization
     * - Enable/disable state of new product selection controls
     * - Dynamic calculation label correctness
     * - Zero exceptions or UI race conditions
     */
    private static void testRapidScenarioSwitching() throws Exception {
        DoiTraHangPanel panel = new DoiTraHangPanel();
        TestFramework.assertNotNull("CHAL-M2-01: DoiTraHangPanel instance created", panel);
        if (panel == null) return;

        JRadioButton radA = (JRadioButton) getFieldValue(panel, "radKichBanA");
        JRadioButton radB = (JRadioButton) getFieldValue(panel, "radKichBanB");
        JButton btnAction = (JButton) getFieldValue(panel, "btnXacNhanInPhieu");
        JComboBox<?> cboMoi = (JComboBox<?>) getFieldValue(panel, "cboSanPhamMoi");
        JSpinner spnSlMoi = (JSpinner) getFieldValue(panel, "spnSoLuongMoi");
        JLabel lblChenhLech = (JLabel) getFieldValue(panel, "lblKetQuaChenhLech");

        TestFramework.assertNotNull("CHAL-M2-02: radKichBanA found", radA);
        TestFramework.assertNotNull("CHAL-M2-03: radKichBanB found", radB);
        TestFramework.assertNotNull("CHAL-M2-04: btnXacNhanInPhieu found", btnAction);
        TestFramework.assertNotNull("CHAL-M2-05: cboSanPhamMoi found", cboMoi);
        TestFramework.assertNotNull("CHAL-M2-06: spnSoLuongMoi found", spnSlMoi);
        TestFramework.assertNotNull("CHAL-M2-07: lblKetQuaChenhLech found", lblChenhLech);

        Method chuyenDoiMethod = DoiTraHangPanel.class.getDeclaredMethod("chuyenDoiKichBan", boolean.class);
        chuyenDoiMethod.setAccessible(true);

        boolean allCyclesPassed = true;
        String failureReason = "";

        // Rapid 100-cycle loop
        for (int i = 0; i < 100; i++) {
            // 1. Switch to Scenario B (Exchange -> Forward to POS)
            radB.setSelected(true);
            radA.setSelected(false);
            chuyenDoiMethod.invoke(panel, false);

            if (!cboMoi.isEnabled() || !spnSlMoi.isEnabled()) {
                allCyclesPassed = false;
                failureReason = "Controls not enabled in Scenario B at iteration " + i;
                break;
            }
            if (!btnAction.getText().contains("CHUYỂN SANG BÁN HÀNG POS ĐỂ THANH TOÁN")) {
                allCyclesPassed = false;
                failureReason = "Button text incorrect for Scenario B at iteration " + i;
                break;
            }
            if (!new Color(16, 185, 129).equals(btnAction.getBackground())) {
                allCyclesPassed = false;
                failureReason = "Button background color is not green (#10B981) at iteration " + i;
                break;
            }

            // 2. Switch back to Scenario A (Return -> Refund at counter)
            radA.setSelected(true);
            radB.setSelected(false);
            chuyenDoiMethod.invoke(panel, true);

            if (cboMoi.isEnabled() || spnSlMoi.isEnabled()) {
                allCyclesPassed = false;
                failureReason = "Controls not disabled in Scenario A at iteration " + i;
                break;
            }
            if (!btnAction.getText().contains("XÁC NHẬN & IN PHIẾU HOÀN TIỀN")) {
                allCyclesPassed = false;
                failureReason = "Button text incorrect for Scenario A at iteration " + i;
                break;
            }
            if (!ThemeColors.PRIMARY.equals(btnAction.getBackground())) {
                allCyclesPassed = false;
                failureReason = "Button background is not PRIMARY at iteration " + i;
                break;
            }
            if (!lblChenhLech.getText().contains("Kịch bản A: Hoàn 100% tiền hàng đã chọn")) {
                allCyclesPassed = false;
                failureReason = "lblKetQuaChenhLech does not match Scenario A description at iteration " + i;
                break;
            }
        }

        TestFramework.assertTrue("CHAL-M2-08: 100 rapid alternations between Scenario A and B succeeded without state corruption: " + failureReason, allCyclesPassed);
    }

    /**
     * CHALLENGE 2: Forwarding to POS: live injection, banner visibility, and receipt state verification.
     */
    private static void testForwardingAndReceptionInPOS() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();
        TestFramework.assertNotNull("CHAL-M2-09: BanHangPOSPanel created for reception", posPanel);

        DoiTraTransferData data = new DoiTraTransferData(
                "HD_TEST_001", "2026-10-31 10:15:00", "Nguyễn Văn Thử Nghiệm", "0987654321", 300000L, 3
        );
        data.addMonTra(new DoiTraTransferData.MonTraLai("SP001", "Hạt Me-O Cá Ngừ 1.2kg", "Gói", 1, 120000L, 120000L, "Đổi vị", true));
        data.addMonTra(new DoiTraTransferData.MonTraLai("SP002", "Sữa tắm SOS 530ml", "Chai", 1, 180000L, 180000L, "Đổi loại", true));
        data.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP003", "Hạt Royal Canin Fit32 2kg", "Bao", 1, 220000L));

        // Start auto-dismiss helper for the information dialog
        AutoDismissThread dismisser = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data);
        dismisser.finish();

        DoiTraTransferData liveData = (DoiTraTransferData) getFieldValue(posPanel, "thongTinDoiTraHienTai");
        TestFramework.assertNotNull("CHAL-M2-10: thongTinDoiTraHienTai successfully set in POS", liveData);
        TestFramework.assertEquals("CHAL-M2-11: Received maHoaDonGoc matches HD_TEST_001", "HD_TEST_001", liveData.getMaHoaDonGoc());
        TestFramework.assertEquals("CHAL-M2-12: Received tongTienTra matches 300,000 đ", 300000L, liveData.getTongTienTra());

        JPanel pnlBanner = (JPanel) getFieldValue(posPanel, "pnlThongBaoDoiTra");
        JLabel lblBanner = (JLabel) getFieldValue(posPanel, "lblThongBaoDoiTra");
        TestFramework.assertNotNull("CHAL-M2-13: pnlThongBaoDoiTra banner exists", pnlBanner);
        TestFramework.assertTrue("CHAL-M2-14: pnlThongBaoDoiTra is visible", pnlBanner.isVisible());
        TestFramework.assertTrue("CHAL-M2-15: Banner text contains invoice HD_TEST_001 and -300.000",
                lblBanner.getText().contains("HD_TEST_001") && lblBanner.getText().contains("300.000"));

        JTextField txtSdt = (JTextField) getFieldValue(posPanel, "txtSdtKhachHang");
        JTextField txtTen = (JTextField) getFieldValue(posPanel, "txtTenKhachHang");
        TestFramework.assertEquals("CHAL-M2-16: Customer phone populated", "0987654321", txtSdt.getText().trim());
        TestFramework.assertEquals("CHAL-M2-17: Customer name populated", "Nguyễn Văn Thử Nghiệm", txtTen.getText().trim());

        DefaultTableModel modelGio = (DefaultTableModel) getFieldValue(posPanel, "modelGioHang");
        TestFramework.assertEquals("CHAL-M2-18: Cart populated with initial replacement item (count=1)", 1, modelGio.getRowCount());
        TestFramework.assertEquals("CHAL-M2-19: Cart item is SP003", "SP003", modelGio.getValueAt(0, 1));

        JLabel lblKhachCanTra = (JLabel) getFieldValue(posPanel, "lblKhachCanTra");
        JLabel lblTienThoi = (JLabel) getFieldValue(posPanel, "lblTienThoiLai");
        TestFramework.assertEquals("CHAL-M2-20: Initial replacement (220k < 300k deduction) -> Net due canTra is 0 đ",
                "0 đ", lblKhachCanTra.getText().trim());
        TestFramework.assertTrue("CHAL-M2-21: Initial replacement (220k < 300k) -> lblTienThoiLai displays 'Hoàn thừa: 80.000 đ'",
                lblTienThoi.getText().contains("Hoàn thừa") && lblTienThoi.getText().contains("80.000"));
    }

    /**
     * CHALLENGE 3: Dynamic Cart Mutations: adding 5 products, removing 2, changing quantities.
     */
    private static void testDynamicCartMutationsAtPOS() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();

        DoiTraTransferData data = new DoiTraTransferData(
                "HD_TEST_002", "2026-10-31 10:20:00", "Khách Hàng Đổi Trả", "0912345678", 300000L, 3
        );
        data.addMonTra(new DoiTraTransferData.MonTraLai("SP001", "Sản phẩm trả", "Gói", 1, 300000L, 300000L, "Lỗi", true));
        data.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP003", "Hạt Royal Canin", "Bao", 1, 220000L));

        AutoDismissThread dismisser = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data);
        dismisser.finish();

        DefaultTableModel modelGio = (DefaultTableModel) getFieldValue(posPanel, "modelGioHang");
        JTable tblGioHang = (JTable) getFieldValue(posPanel, "tblGioHang");
        JLabel lblTongTienHang = (JLabel) getFieldValue(posPanel, "lblTongTienHang");
        JLabel lblGiamGia = (JLabel) getFieldValue(posPanel, "lblGiamGia");
        JLabel lblKhachCanTra = (JLabel) getFieldValue(posPanel, "lblKhachCanTra");
        JLabel lblTienThoi = (JLabel) getFieldValue(posPanel, "lblTienThoiLai");

        // 1. ADD 5 DIFFERENT PRODUCTS TO CART
        // Item 1: SP010 (Pate RC 45k x 2 = 90k) -> Cart: 220k + 90k = 310k -> Net due: 10k
        addCartRow(modelGio, "SP010", "Pate Royal Canin", "Túi", 2, 45000L);
        invokeMethod(posPanel, "capNhatTongTien");
        TestFramework.assertEquals("CHAL-M2-22: After adding Item 1, Cart Total is 310,000 đ", "310.000 đ", lblTongTienHang.getText().trim());
        TestFramework.assertEquals("CHAL-M2-23: After adding Item 1, Net Due canTra is 10,000 đ", "10.000 đ", lblKhachCanTra.getText().trim());

        // Item 2: SP011 (Xịt khử mùi 150k x 1 = 150k) -> Cart: 310k + 150k = 460k -> Net due: 160k
        addCartRow(modelGio, "SP011", "Xịt khử mùi Envy", "Chai", 1, 150000L);
        // Item 3: SP012 (Cào móng 280k x 1 = 280k) -> Cart: 460k + 280k = 740k -> Net due: 440k
        addCartRow(modelGio, "SP012", "Cào móng cây xương rồng", "Cái", 1, 280000L);
        // Item 4: SP013 (Vòng cổ 20k x 3 = 60k) -> Cart: 740k + 60k = 800k -> Net due: 500k
        addCartRow(modelGio, "SP013", "Vòng cổ chuông", "Cái", 3, 20000L);
        // Item 5: SP014 (Bát ăn đôi 75k x 2 = 150k) -> Cart: 800k + 150k = 950k -> Net due: 650k
        addCartRow(modelGio, "SP014", "Bát ăn đôi inox", "Cái", 2, 75000L);

        invokeMethod(posPanel, "capNhatTongTien");
        TestFramework.assertEquals("CHAL-M2-24: Cart row count is 6 (1 initial + 5 added)", 6, modelGio.getRowCount());
        TestFramework.assertEquals("CHAL-M2-25: Cart Total with 5 added items is 950,000 đ", "950.000 đ", lblTongTienHang.getText().trim());
        TestFramework.assertEquals("CHAL-M2-26: Net Due canTra is 650,000 đ (950k - 300k)", "650.000 đ", lblKhachCanTra.getText().trim());
        TestFramework.assertTrue("CHAL-M2-27: Deduction label displays exchange deduction -300,000",
                lblGiamGia.getText().contains("300.000"));

        // 2. REMOVE 2 PRODUCTS (Remove Row 2 [SP011, 150k] and Row 3 [SP013, 60k])
        tblGioHang.setRowSelectionInterval(2, 2); // SP011
        invokeMethod(posPanel, "xoaMonDangChon");
        // After removing SP011: Total = 950k - 150k = 800k
        TestFramework.assertEquals("CHAL-M2-28: After removing SP011, cart has 5 items", 5, modelGio.getRowCount());

        tblGioHang.setRowSelectionInterval(3, 3); // SP013
        invokeMethod(posPanel, "xoaMonDangChon");
        // After removing SP013: Total = 800k - 60k = 740k
        TestFramework.assertEquals("CHAL-M2-29: After removing SP013, cart has 4 items", 4, modelGio.getRowCount());
        TestFramework.assertEquals("CHAL-M2-30: Cart Total after removing 2 items is 740,000 đ", "740.000 đ", lblTongTienHang.getText().trim());
        TestFramework.assertEquals("CHAL-M2-31: Net Due after removing 2 items is 440,000 đ (740k - 300k)", "440.000 đ", lblKhachCanTra.getText().trim());

        // 3. CHANGE QUANTITIES
        // Row 1 (SP010, 45k): increase quantity by +2 -> from 2 to 4 (line total +90k -> total = 830k, canTra = 530k)
        tblGioHang.setRowSelectionInterval(1, 1);
        invokeMethod(posPanel, "thayDoiSoLuong", 1);
        invokeMethod(posPanel, "thayDoiSoLuong", 1);
        TestFramework.assertEquals("CHAL-M2-32: Row 1 quantity increased to 4", 4, (int) modelGio.getValueAt(1, 4));

        // Row 3 (SP014, 75k): decrease quantity by -1 -> from 2 to 1 (line total -75k -> total = 755k, canTra = 455k)
        tblGioHang.setRowSelectionInterval(3, 3);
        invokeMethod(posPanel, "thayDoiSoLuong", -1);
        TestFramework.assertEquals("CHAL-M2-33: Row 3 quantity decreased to 1", 1, (int) modelGio.getValueAt(3, 4));

        invokeMethod(posPanel, "capNhatTongTien");
        TestFramework.assertEquals("CHAL-M2-34: Cart Total after quantity modifications is 755,000 đ", "755.000 đ", lblTongTienHang.getText().trim());
        TestFramework.assertEquals("CHAL-M2-35: Final Net Due after quantity modifications is 455,000 đ", "455.000 đ", lblKhachCanTra.getText().trim());
    }

    /**
     * CHALLENGE 4: Cancellation handling: reject cancel vs confirm cancel [Hủy đổi trả] -> clean revert.
     */
    private static void testCancellationRejectionAndConfirmation() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();

        DoiTraTransferData data = new DoiTraTransferData(
                "HD_CANCEL_001", "2026-10-31 10:30:00", "Khách Hủy", "0933333333", 200000L, 2
        );
        data.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP005", "Đồ ăn vặt", "Gói", 2, 80000L));

        AutoDismissThread dismisser1 = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data);
        dismisser1.finish();

        JPanel pnlBanner = (JPanel) getFieldValue(posPanel, "pnlThongBaoDoiTra");
        DefaultTableModel modelGio = (DefaultTableModel) getFieldValue(posPanel, "modelGioHang");

        // 4.1 SUBCASE: User clicks [Hủy đổi trả] but chooses NO in confirm dialog
        AutoDismissThread dismisserNo = startAutoDismiss(1500, JOptionPane.NO_OPTION);
        posPanel.huyDoiTraHang();
        dismisserNo.finish();

        TestFramework.assertNotNull("CHAL-M2-36: Rejecting cancellation preserves thongTinDoiTraHienTai",
                getFieldValue(posPanel, "thongTinDoiTraHienTai"));
        TestFramework.assertTrue("CHAL-M2-37: Rejecting cancellation keeps banner visible", pnlBanner.isVisible());
        TestFramework.assertEquals("CHAL-M2-38: Rejecting cancellation preserves cart row count (1)", 1, modelGio.getRowCount());

        // 4.2 SUBCASE: User clicks [Hủy đổi trả] and confirms YES
        AutoDismissThread dismisserYes = startAutoDismiss(2500, JOptionPane.YES_OPTION);
        posPanel.huyDoiTraHang();
        dismisserYes.finish();

        TestFramework.assertNull("CHAL-M2-39: Confirming cancellation resets thongTinDoiTraHienTai to null",
                getFieldValue(posPanel, "thongTinDoiTraHienTai"));
        TestFramework.assertFalse("CHAL-M2-40: Banner pnlThongBaoDoiTra becomes hidden", pnlBanner.isVisible());
        TestFramework.assertEquals("CHAL-M2-41: Cart is completely emptied (count=0)", 0, modelGio.getRowCount());

        JLabel lblKhachCanTra = (JLabel) getFieldValue(posPanel, "lblKhachCanTra");
        JLabel lblGiamGia = (JLabel) getFieldValue(posPanel, "lblGiamGia");
        TestFramework.assertEquals("CHAL-M2-42: lblKhachCanTra reverts to 0 đ", "0 đ", lblKhachCanTra.getText().trim());
        TestFramework.assertEquals("CHAL-M2-43: lblGiamGia reverts to 0 đ", "0 đ", lblGiamGia.getText().trim());
    }

    /**
     * CHALLENGE 5: Re-forwarding after cancellation: clean slate verification without cross-contamination.
     */
    private static void testReForwardingAfterCancellation() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();

        // 1. First transaction: cancelled
        DoiTraTransferData data1 = new DoiTraTransferData("HD_FIRST_001", "2026-10-31 08:00:00", "Khách Cũ", "0911111111", 100000L, 1);
        data1.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP001", "Món cũ", "Gói", 1, 100000L));
        AutoDismissThread d1 = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data1);
        d1.finish();

        AutoDismissThread dCancel = startAutoDismiss(2500, JOptionPane.YES_OPTION);
        posPanel.huyDoiTraHang();
        dCancel.finish();

        // 2. Second transaction: Forward new exchange data
        DoiTraTransferData data2 = new DoiTraTransferData(
                "HD_NEW_002", "2026-10-31 10:45:00", "Trần Thị Hoàng Yến", "0911223344", 500000L, 5
        );
        data2.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP088", "Nệm nằm cao cấp lông cừu", "Cái", 1, 420000L));

        AutoDismissThread d2 = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data2);
        d2.finish();

        DoiTraTransferData liveData2 = (DoiTraTransferData) getFieldValue(posPanel, "thongTinDoiTraHienTai");
        TestFramework.assertNotNull("CHAL-M2-44: New exchange data received after prior cancellation", liveData2);
        TestFramework.assertEquals("CHAL-M2-45: Invoice ID is HD_NEW_002 (no residual from HD_FIRST_001)",
                "HD_NEW_002", liveData2.getMaHoaDonGoc());
        TestFramework.assertEquals("CHAL-M2-46: Deduction is 500,000 đ", 500000L, liveData2.getTongTienTra());

        JLabel lblBanner = (JLabel) getFieldValue(posPanel, "lblThongBaoDoiTra");
        TestFramework.assertTrue("CHAL-M2-47: Banner text displays HD_NEW_002 and -500.000",
                lblBanner.getText().contains("HD_NEW_002") && lblBanner.getText().contains("500.000"));

        JTextField txtSdt = (JTextField) getFieldValue(posPanel, "txtSdtKhachHang");
        JTextField txtTen = (JTextField) getFieldValue(posPanel, "txtTenKhachHang");
        TestFramework.assertEquals("CHAL-M2-48: Sdt matches 0911223344", "0911223344", txtSdt.getText().trim());
        TestFramework.assertEquals("CHAL-M2-49: Customer name matches Trần Thị Hoàng Yến", "Trần Thị Hoàng Yến", txtTen.getText().trim());

        DefaultTableModel modelGio = (DefaultTableModel) getFieldValue(posPanel, "modelGioHang");
        TestFramework.assertEquals("CHAL-M2-50: Cart contains exactly 1 new item (count=1)", 1, modelGio.getRowCount());
        TestFramework.assertEquals("CHAL-M2-51: New item is SP088", "SP088", modelGio.getValueAt(0, 1));

        JLabel lblTienThoi = (JLabel) getFieldValue(posPanel, "lblTienThoiLai");
        TestFramework.assertTrue("CHAL-M2-52: Net refund displays 'Hoàn thừa: 80.000 đ' (420k - 500k = -80k)",
                lblTienThoi.getText().contains("Hoàn thừa") && lblTienThoi.getText().contains("80.000"));
    }

    /**
     * CHALLENGE 6: Cashier checkout: cash (deficit vs exact vs surplus) vs VietQR for net difference.
     */
    private static void testCashierCheckoutCashVsVietQR() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();

        DoiTraTransferData data = new DoiTraTransferData(
                "HD_PAY_003", "2026-10-31 11:00:00", "Lê Khải Minh", "0901234567", 500000L, 5
        );
        data.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP088", "Nệm nằm lông cừu", "Cái", 1, 420000L));

        AutoDismissThread d1 = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data);
        d1.finish();

        // Add additional product to create a Net Due of 170,000 đ
        // Cart total: 420k + 250k = 670k -> Deduction 500k -> Net due = 170k
        DefaultTableModel modelGio = (DefaultTableModel) getFieldValue(posPanel, "modelGioHang");
        addCartRow(modelGio, "SP090", "Balo phi hành gia", "Cái", 1, 250000L);
        invokeMethod(posPanel, "capNhatTongTien");

        JLabel lblKhachCanTra = (JLabel) getFieldValue(posPanel, "lblKhachCanTra");
        TestFramework.assertEquals("CHAL-M2-53: Net Due canTra is 170,000 đ", "170.000 đ", lblKhachCanTra.getText().trim());

        JComboBox<?> cbo = (JComboBox<?>) getFieldValue(posPanel, "cboPhuongThuc");
        JTextField txtTienDua = (JTextField) getFieldValue(posPanel, "txtTienKhachDua");
        JLabel lblThoi = (JLabel) getFieldValue(posPanel, "lblTienThoiLai");

        // 6.1 Cash Deficit (100k < 170k) -> Must display shortage and be blocked
        cbo.setSelectedItem("Tiền mặt");
        txtTienDua.setText("100000");
        invokeMethod(posPanel, "tinhTienThoi");
        TestFramework.assertTrue("CHAL-M2-54: Shortage warning 'Chưa đủ (thiếu 70.000 đ)' shown",
                lblThoi.getText().contains("Chưa đủ") && lblThoi.getText().contains("70.000"));
        TestFramework.assertEquals("CHAL-M2-55: Shortage label foreground is DANGER (red)",
                ThemeColors.DANGER, lblThoi.getForeground());
        boolean deficitBlocked = POSPaymentTest.validateCashPayment("Tiền mặt", 170000L, 100000L);
        TestFramework.assertFalse("CHAL-M2-56: Cash deficit (100k < 170k) is BLOCKED", deficitBlocked);

        // 6.2 Exact Cash (170k == 170k) -> Must display 0 đ change and be allowed
        txtTienDua.setText("170000");
        invokeMethod(posPanel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-M2-57: Exact cash change is 0 đ", "0 đ", lblThoi.getText().trim());
        TestFramework.assertEquals("CHAL-M2-58: Exact cash label foreground is SUCCESS (green)",
                ThemeColors.SUCCESS, lblThoi.getForeground());
        boolean exactAllowed = POSPaymentTest.validateCashPayment("Tiền mặt", 170000L, 170000L);
        TestFramework.assertTrue("CHAL-M2-59: Exact cash payment is ALLOWED", exactAllowed);

        // 6.3 Surplus Cash (200k > 170k) -> Must display 30,000 đ change and be allowed
        txtTienDua.setText("200000");
        invokeMethod(posPanel, "tinhTienThoi");
        TestFramework.assertEquals("CHAL-M2-60: Surplus cash change is 30,000 đ", "30.000 đ", lblThoi.getText().trim());
        boolean surplusAllowed = POSPaymentTest.validateCashPayment("Tiền mặt", 170000L, 200000L);
        TestFramework.assertTrue("CHAL-M2-61: Surplus cash payment is ALLOWED", surplusAllowed);

        // 6.4 VietQR Payment
        cbo.setSelectedItem("Chuyển khoản");
        TestFramework.assertFalse("CHAL-M2-62: txtTienKhachDua is disabled during VietQR transfer", txtTienDua.isEnabled());
        TestFramework.assertEquals("CHAL-M2-63: txtTienKhachDua auto-populated with exact net due 170,000 đ",
                "170000", txtTienDua.getText().replaceAll("[^0-9]", ""));
        TestFramework.assertEquals("CHAL-M2-64: lblTienThoiLai is '0 đ' during VietQR transfer", "0 đ", lblThoi.getText().trim());
        boolean qrAllowed = POSPaymentTest.validateCashPayment("Chuyển khoản", 170000L, 0L);
        TestFramework.assertTrue("CHAL-M2-65: VietQR payment does not require physical cash and is ALLOWED", qrAllowed);
    }

    /**
     * CHALLENGE 7: Store refund scenario (Net Due == 0, Store owes refund).
     * Rule: When tongTienHang < tienKhauTruDoiTra, customer owes 0 đ, store refunds difference.
     * Cashier checkout with 0 cash given MUST be allowed.
     */
    private static void testStoreRefundCheckout() throws Exception {
        BanHangPOSPanel posPanel = new BanHangPOSPanel();

        DoiTraTransferData data = new DoiTraTransferData(
                "HD_REFUND_004", "2026-10-31 11:30:00", "Khách Nhận Hoàn", "0944444444", 400000L, 4
        );
        data.addMonMoi(new DoiTraTransferData.MonDoiMoi("SP001", "Món rẻ hơn hàng trả", "Gói", 1, 150000L));

        AutoDismissThread d1 = startAutoDismiss(1500, JOptionPane.OK_OPTION);
        posPanel.tiepNhanDoiTraHang(data);
        d1.finish();

        JLabel lblKhachCanTra = (JLabel) getFieldValue(posPanel, "lblKhachCanTra");
        JLabel lblThoi = (JLabel) getFieldValue(posPanel, "lblTienThoiLai");
        JTextField txtTienDua = (JTextField) getFieldValue(posPanel, "txtTienKhachDua");

        TestFramework.assertEquals("CHAL-M2-66: Refund case -> Net Due canTra is 0 đ", "0 đ", lblKhachCanTra.getText().trim());
        TestFramework.assertTrue("CHAL-M2-67: lblTienThoiLai displays 'Hoàn thừa: 250.000 đ' (400k - 150k)",
                lblThoi.getText().contains("Hoàn thừa") && lblThoi.getText().contains("250.000"));
        TestFramework.assertEquals("CHAL-M2-68: lblTienThoiLai foreground is SUCCESS (green)",
                ThemeColors.SUCCESS, lblThoi.getForeground());

        // Zero physical cash given from customer is valid because canTra == 0
        boolean refundCheckoutAllowed = POSPaymentTest.validateCashPayment("Tiền mặt", 0L, 0L);
        TestFramework.assertTrue("CHAL-M2-69: Checkout with store refund (0 cash given) is ALLOWED", refundCheckoutAllowed);
    }

    // --- Helper Methods ---

    private static void addCartRow(DefaultTableModel model, String ma, String ten, String dvt, int sl, long donGia) {
        int stt = model.getRowCount() + 1;
        long thanhTien = donGia * sl;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
        model.addRow(new Object[]{
                stt, ma, ten, dvt, sl, df.format(donGia), df.format(thanhTien), "Đang chọn"
        });
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

    public static class AutoDismissThread extends Thread {
        private final int option;
        private final AtomicBoolean running = new AtomicBoolean(true);

        public AutoDismissThread(long maxDurationMs, int option) {
            this.option = option;
            setDaemon(true);
        }

        @Override
        public void run() {
            long deadline = System.currentTimeMillis() + 60000L; // 60s safety timeout
            while (running.get() && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {
                    break;
                }
                SwingUtilities.invokeLater(() -> {
                    for (Window w : Window.getWindows()) {
                        if (w instanceof JDialog && (w.isVisible() || w.isShowing())) {
                            JDialog d = (JDialog) w;
                            findAndSetOption(d, option);
                            d.setVisible(false);
                            d.dispose();
                        }
                    }
                });
            }
        }

        private boolean findAndSetOption(Component c, int opt) {
            if (c instanceof JOptionPane) {
                ((JOptionPane) c).setValue(opt);
                return true;
            }
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    if (findAndSetOption(child, opt)) return true;
                }
            }
            return false;
        }

        public void finish() {
            running.set(false);
            interrupt();
        }
    }

    private static AutoDismissThread startAutoDismiss(long maxDurationMs, int option) {
        AutoDismissThread t = new AutoDismissThread(maxDurationMs, option);
        t.start();
        return t;
    }
}
