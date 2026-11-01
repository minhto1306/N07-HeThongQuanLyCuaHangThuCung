package tests;

import models.entity.DoiTraTransferData;
import views.BanHangPOSPanel;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Adversarial Stress Harness for Milestone 2 (R2 - Exchange Flow Forward to POS).
 * Designed by Challenger agent to rigorously stress-test:
 * - Data contract serialization/deserialization & null resiliency
 * - Exact boundary arithmetic (exact, -1 VND refund, +1 VND net due)
 * - Massive transaction values & non-overflow behavior
 * - Multi-tier discount stacking (VIP 5% + point redemption + exchange deduction)
 * - Live UI state transitions (labels, banner visibility, and cancellation reset)
 */
public class ExchangeFlowAdversarialTest {

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runAdversarialSuite();
        System.exit(result.isAllPassed() ? 0 : 1);
    }

    public static TestFramework.SuiteResult runAdversarialSuite() {
        TestFramework.startSuite("ExchangeFlowAdversarialTest (Milestone 2 Challenger Suite)");

        try {
            testContractSerializationAndDeepCopy();
            testNullSafetyAndMalformedTransferData();
            testBoundaryArithmeticAndExtremeValues();
            testComplexDiscountStackingWithExchange();
            testDynamicCartModificationInvariants();
            testLivePOSPanelStateTransitions();
        } catch (Exception ex) {
            TestFramework.recordManual("Adversarial Suite Execution Exception", false, ex.getMessage());
            ex.printStackTrace();
        }

        return TestFramework.endSuite();
    }

    /**
     * 1. Test DTO serialization and deserialization integrity.
     */
    private static void testContractSerializationAndDeepCopy() throws Exception {
        DoiTraTransferData original = new DoiTraTransferData(
                "HD_20261031_001", "2026-10-31 10:30:00", "Lê Văn Thử Nghiệm", "0987654321", 350000L, 3
        );
        original.addMonTra(new DoiTraTransferData.MonTraLai(
                "SP001", "Hạt Royal Canin Maxi Puppy 4kg", "Bao", 1, 350000L, 350000L, "Thú cưng chê hạt", true
        ));
        original.addMonMoi(new DoiTraTransferData.MonDoiMoi(
                "SP002", "Pate Monge Vị Vịt & Táo 400g", "Hộp", 7, 50000L
        ));

        // Serialize to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = baos.toByteArray();

        TestFramework.assertTrue("ADV-EX-01: Serialized byte array is non-empty", bytes.length > 0);

        // Deserialize from byte array
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        TestFramework.assertTrue("ADV-EX-02: Deserialized object is instance of DoiTraTransferData",
                deserialized instanceof DoiTraTransferData);

        DoiTraTransferData copy = (DoiTraTransferData) deserialized;
        TestFramework.assertEquals("ADV-EX-03: maHoaDonGoc preserved after serialization",
                original.getMaHoaDonGoc(), copy.getMaHoaDonGoc());
        TestFramework.assertEquals("ADV-EX-04: tongTienTra preserved after serialization",
                original.getTongTienTra(), copy.getTongTienTra());
        TestFramework.assertEquals("ADV-EX-05: diemKhauTru preserved after serialization",
                original.getDiemKhauTru(), copy.getDiemKhauTru());
        TestFramework.assertEquals("ADV-EX-06: dsMonTra item count preserved",
                original.getDsMonTra().size(), copy.getDsMonTra().size());
        TestFramework.assertEquals("ADV-EX-07: dsMonMoi item count preserved",
                original.getDsMonMoi().size(), copy.getDsMonMoi().size());
        TestFramework.assertEquals("ADV-EX-08: MonTraLai maSP and nguyenTem preserved",
                "SP001", copy.getDsMonTra().get(0).getMaSP());
        TestFramework.assertTrue("ADV-EX-09: MonTraLai nguyenTem is true",
                copy.getDsMonTra().get(0).isNguyenTem());
        TestFramework.assertEquals("ADV-EX-10: MonDoiMoi soLuong preserved",
                7, copy.getDsMonMoi().get(0).getSoLuong());
    }

    /**
     * 2. Test null-safety and robust initialization of DoiTraTransferData.
     */
    private static void testNullSafetyAndMalformedTransferData() {
        DoiTraTransferData emptyData = new DoiTraTransferData();
        TestFramework.assertNotNull("ADV-EX-11: Default constructor initializes non-null dsMonTra", emptyData.getDsMonTra());
        TestFramework.assertNotNull("ADV-EX-12: Default constructor initializes non-null dsMonMoi", emptyData.getDsMonMoi());

        // Null list reset then addMonTra / addMonMoi auto-recovers
        emptyData.setDsMonTra(null);
        emptyData.addMonTra(new DoiTraTransferData.MonTraLai());
        TestFramework.assertEquals("ADV-EX-13: addMonTra safely initializes null dsMonTra list", 1, emptyData.getDsMonTra().size());

        emptyData.setDsMonMoi(null);
        emptyData.addMonMoi(new DoiTraTransferData.MonDoiMoi());
        TestFramework.assertEquals("ADV-EX-14: addMonMoi safely initializes null dsMonMoi list", 1, emptyData.getDsMonMoi().size());
    }

    /**
     * 3. Test exact boundary conditions, off-by-one VND, and huge transaction values.
     */
    private static void testBoundaryArithmeticAndExtremeValues() {
        long exactReturn = 500000L;

        // Exact match (500k == 500k)
        TestFramework.assertEquals("ADV-EX-15: Exact match -> canTra is 0",
                0L, ExchangeFlowTest.calculateCanTra(500000L, 0L, exactReturn));
        TestFramework.assertEquals("ADV-EX-16: Exact match -> tienHoanThua is 0",
                0L, ExchangeFlowTest.calculateTienHoanThua(500000L, 0L, exactReturn));

        // Off-by-one: 1 VND deficit (499,999 VND < 500,000 VND) -> Store refunds 1 VND
        TestFramework.assertEquals("ADV-EX-17: 1 VND less than return -> canTra is 0",
                0L, ExchangeFlowTest.calculateCanTra(499999L, 0L, exactReturn));
        TestFramework.assertEquals("ADV-EX-18: 1 VND less than return -> refund is 1 VND",
                1L, ExchangeFlowTest.calculateTienHoanThua(499999L, 0L, exactReturn));

        // Off-by-one: 1 VND surplus (500,001 VND > 500,000 VND) -> Customer pays 1 VND
        TestFramework.assertEquals("ADV-EX-19: 1 VND more than return -> canTra is 1 VND",
                1L, ExchangeFlowTest.calculateCanTra(500001L, 0L, exactReturn));
        TestFramework.assertEquals("ADV-EX-20: 1 VND more than return -> refund is 0",
                0L, ExchangeFlowTest.calculateTienHoanThua(500001L, 0L, exactReturn));

        // Massive values (100 Million VND and 1 Billion VND)
        long hugeNew = 1000000000L; // 1 billion VND
        long hugeReturn = 200000000L; // 200 million VND
        TestFramework.assertEquals("ADV-EX-21: Massive order (1B - 200M) -> canTra is 800,000,000 đ",
                800000000L, ExchangeFlowTest.calculateCanTra(hugeNew, 0L, hugeReturn));
        TestFramework.assertEquals("ADV-EX-22: Massive order -> refund is 0 đ",
                0L, ExchangeFlowTest.calculateTienHoanThua(hugeNew, 0L, hugeReturn));

        // Massive refund (50k new vs 500M return)
        TestFramework.assertEquals("ADV-EX-23: Massive refund (50k vs 500M) -> canTra is 0 đ",
                0L, ExchangeFlowTest.calculateCanTra(50000L, 0L, 500000000L));
        TestFramework.assertEquals("ADV-EX-24: Massive refund -> refund is 499,950,000 đ",
                499950000L, ExchangeFlowTest.calculateTienHoanThua(50000L, 0L, 500000000L));
    }

    /**
     * 4. Multi-tier discount stacking (VIP discount + Point redemption + Exchange deduction).
     */
    private static void testComplexDiscountStackingWithExchange() {
        // Scenario: Order 1,000,000 đ; VIP 5% (50,000 đ); 200 loyalty points (200,000 đ)
        // Total discount = 250,000 đ; Exchange credit = 350,000 đ
        long tongTien = 1000000L;
        long vipDiscount = 50000L;
        long pointsDiscount = 200000L;
        long tongGiamGia = vipDiscount + pointsDiscount; // 250,000 đ
        long tienKhauTru = 350000L;

        long canTra = ExchangeFlowTest.calculateCanTra(tongTien, tongGiamGia, tienKhauTru);
        long refund = ExchangeFlowTest.calculateTienHoanThua(tongTien, tongGiamGia, tienKhauTru);

        TestFramework.assertEquals("ADV-EX-25: Stacking VIP + Points + Exchange -> canTra is 400,000 đ",
                400000L, canTra);
        TestFramework.assertEquals("ADV-EX-26: Stacking VIP + Points + Exchange -> refund is 0 đ",
                0L, refund);

        // Scenario: Full discount coverage where points/discounts pay for whole purchase
        // tongTien = 200,000 đ; discount = 200,000 đ; exchange credit = 150,000 đ
        long canTraFullDiscount = ExchangeFlowTest.calculateCanTra(200000L, 200000L, 150000L);
        long refundFullDiscount = ExchangeFlowTest.calculateTienHoanThua(200000L, 200000L, 150000L);

        TestFramework.assertEquals("ADV-EX-27: Points pay entire order -> canTra is 0 đ",
                0L, canTraFullDiscount);
        TestFramework.assertEquals("ADV-EX-28: Points pay entire order -> entire 150k exchange is refunded",
                150000L, refundFullDiscount);
    }

    /**
     * 5. Dynamic cart item modification invariant check.
     * Invariant: canTra + (tienKhauTru - tienHoanThua) == max(0, tongTienHang - tienGiamGia)
     */
    private static void testDynamicCartModificationInvariants() {
        long tienKhauTru = 300000L;
        long[] cartTotalSteps = {
                100000L, // Item 1 (100k < 300k) -> refund 200k, canTra 0
                250000L, // Add Item 2 (250k < 300k) -> refund 50k, canTra 0
                300000L, // Add Item 3 (300k == 300k) -> refund 0, canTra 0
                420000L, // Add Item 4 (420k > 300k) -> refund 0, canTra 120k
                200000L  // Remove Item 4 & 3 (200k < 300k) -> refund 100k, canTra 0
        };

        for (int i = 0; i < cartTotalSteps.length; i++) {
            long total = cartTotalSteps[i];
            long canTra = ExchangeFlowTest.calculateCanTra(total, 0L, tienKhauTru);
            long refund = ExchangeFlowTest.calculateTienHoanThua(total, 0L, tienKhauTru);

            long actualEffectiveCredit = tienKhauTru - refund;
            long expectedEffectiveTotal = canTra + actualEffectiveCredit;

            TestFramework.assertEquals("ADV-EX-29[" + i + "]: Mathematical accounting balance invariant holds",
                    total, expectedEffectiveTotal);
        }
    }

    /**
     * 6. Live GUI BanHangPOSPanel state transitions with DoiTraTransferData.
     */
    private static void testLivePOSPanelStateTransitions() {
        try {
            BanHangPOSPanel panel = new BanHangPOSPanel();
            javax.swing.table.DefaultTableModel modelGioHang =
                    (javax.swing.table.DefaultTableModel) getFieldValue(panel, "modelGioHang");

            DoiTraTransferData data = new DoiTraTransferData(
                    "HD00999", "2026-10-31 11:00", "Nguyễn Văn Thử UI", "0909999888", 200000L, 2
            );

            // Set transfer data into panel via reflection and populate cart with 320,000 đ
            setFieldValue(panel, "thongTinDoiTraHienTai", data);
            modelGioHang.setRowCount(0);
            modelGioHang.addRow(new Object[]{1, "SP001", "Hạt Maxi Puppy", "Gói", 1, "320.000 đ", "320.000 đ", "Đang bán"});

            // Invoke capNhatTongTien
            invokeMethod(panel, "capNhatTongTien");

            JLabel lblGiamGia = (JLabel) getFieldValue(panel, "lblGiamGia");
            JLabel lblKhachCanTra = (JLabel) getFieldValue(panel, "lblKhachCanTra");
            JLabel lblTienThoiLai = (JLabel) getFieldValue(panel, "lblTienThoiLai");

            TestFramework.assertNotNull("ADV-EX-30: lblGiamGia exists", lblGiamGia);
            TestFramework.assertNotNull("ADV-EX-31: lblKhachCanTra exists", lblKhachCanTra);

            // Verify label text reflects deduction
            String giamGiaText = lblGiamGia.getText();
            TestFramework.assertTrue("ADV-EX-32: lblGiamGia displays exchange deduction prefix",
                    giamGiaText.contains("Đổi") && giamGiaText.contains("200.000"));

            String canTraText = lblKhachCanTra.getText().replaceAll("[^0-9]", "");
            TestFramework.assertEquals("ADV-EX-33: lblKhachCanTra displays net due 120,000 đ",
                    120000L, Long.parseLong(canTraText));

            // Test scenario when New < Return: New 150k < Return 200k -> Refund 50k
            modelGioHang.setRowCount(0);
            modelGioHang.addRow(new Object[]{1, "SP002", "Pate Monge", "Hộp", 3, "50.000 đ", "150.000 đ", "Đang bán"});
            invokeMethod(panel, "capNhatTongTien");

            String canTraRefundText = lblKhachCanTra.getText().replaceAll("[^0-9]", "");
            TestFramework.assertEquals("ADV-EX-34: lblKhachCanTra displays 0 đ for refund case",
                    0L, Long.parseLong(canTraRefundText));

            String thoiLaiText = lblTienThoiLai.getText();
            TestFramework.assertTrue("ADV-EX-35: lblTienThoiLai displays 'Hoàn thừa: 50.000'",
                    thoiLaiText.contains("Hoàn thừa") && thoiLaiText.contains("50.000"));

            // Test reset / cancel exchange state
            setFieldValue(panel, "thongTinDoiTraHienTai", null);
            modelGioHang.setRowCount(0);
            invokeMethod(panel, "capNhatTongTien");
            TestFramework.assertNull("ADV-EX-36: thongTinDoiTraHienTai successfully reset to null",
                    getFieldValue(panel, "thongTinDoiTraHienTai"));
            TestFramework.assertEquals("ADV-EX-37: lblGiamGia resets to 0 đ after clearing exchange data",
                    "0 đ", lblGiamGia.getText().trim());
        } catch (Exception ex) {
            TestFramework.recordManual("ADV-EX-30: Live POS GUI State Transitions Exception", false, ex.getMessage());
            ex.printStackTrace();
        }
    }

    // --- Reflection Helpers ---

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
}
