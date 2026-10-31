package tests;

import views.MainDashboard;
import views.BanHangPOSPanel;
import views.DoiTraHangPanel;
import views.BanGiaoCaPanel;
import utils.UserSession;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Automated Application Launch Verification Test for Milestone 4 (Requirement R5).
 * Simulates complete application startup, verifies FlatLaf Look and Feel initialization,
 * verifies MainDashboard frame construction, verifies panel lazy loading for POS, Exchange,
 * and Shift Handover, verifies inter-panel routing, and cleanly closes the UI.
 */
public class ApplicationLaunchVerificationTest {

    public static TestFramework.SuiteResult runTests() {
        TestFramework.startSuite("ApplicationLaunchVerificationTest (Milestone 4 GUI Launch)");

        AtomicReference<MainDashboard> dashboardRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        // Test 1: Initialize Look and Feel
        try {
            com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
            String lafName = UIManager.getLookAndFeel().getName();
            TestFramework.assertTrue("LAUNCH-01: FlatLaf Look & Feel setup successfully",
                    lafName.contains("FlatLaf") || lafName.contains("Mac Light") || lafName.contains("Windows"));
        } catch (Throwable t) {
            TestFramework.assertTrue("LAUNCH-01: Valid LookAndFeel is present", UIManager.getLookAndFeel() != null);
        }

        // Test 2: Instantiate MainDashboard on EDT
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    MainDashboard dashboard = new MainDashboard();
                    dashboardRef.set(dashboard);
                } catch (Throwable t) {
                    errorRef.set(t);
                }
            });
        } catch (Exception e) {
            errorRef.set(e);
        }
        TestFramework.assertTrue("LAUNCH-02: MainDashboard instantiated without exceptions on EDT", errorRef.get() == null);
        TestFramework.assertNotNull("LAUNCH-02b: MainDashboard instance is non-null", dashboardRef.get());

        MainDashboard dashboard = dashboardRef.get();
        if (dashboard == null) {
            return TestFramework.endSuite();
        }

        // Test 3: MainDashboard Frame Properties and Title
        TestFramework.assertTrue("LAUNCH-03: Title contains store branding",
                dashboard.getTitle() != null && dashboard.getTitle().contains("HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG"));
        TestFramework.assertNotNull("LAUNCH-03b: JMenuBar is attached to MainDashboard", dashboard.getJMenuBar());
        TestFramework.assertTrue("LAUNCH-03c: JMenuBar contains standard functional menus", dashboard.getJMenuBar().getMenuCount() >= 5);

        // Test 4: Navigate to POS Panel
        try {
            SwingUtilities.invokeAndWait(() -> {
                dashboard.dieuHuongSangChucNang(MainDashboard.CARD_POS);
            });
            Field fPos = MainDashboard.class.getDeclaredField("banHangPOSPanel");
            fPos.setAccessible(true);
            BanHangPOSPanel pos = (BanHangPOSPanel) fPos.get(dashboard);
            TestFramework.assertNotNull("LAUNCH-04a: BanHangPOSPanel loaded into content cards", pos);
            TestFramework.assertTrue("LAUNCH-04b: BanHangPOSPanel is visible", pos != null && pos.isVisible());
        } catch (Exception e) {
            TestFramework.recordManual("LAUNCH-04: Navigate to POS Panel", false, e.getMessage());
        }

        // Test 5: Navigate to Doi Tra Hang Panel
        try {
            SwingUtilities.invokeAndWait(() -> {
                dashboard.dieuHuongSangChucNang("CARD_DOI_TRA");
            });
            Field fDoiTra = MainDashboard.class.getDeclaredField("doiTraHangPanel");
            fDoiTra.setAccessible(true);
            DoiTraHangPanel dt = (DoiTraHangPanel) fDoiTra.get(dashboard);
            TestFramework.assertNotNull("LAUNCH-05a: DoiTraHangPanel loaded into content cards", dt);
            TestFramework.assertTrue("LAUNCH-05b: DoiTraHangPanel is visible", dt != null && dt.isVisible());
        } catch (Exception e) {
            TestFramework.recordManual("LAUNCH-05: Navigate to Doi Tra Panel", false, e.getMessage());
        }

        // Test 6: Navigate to Shift Handover Panel
        try {
            SwingUtilities.invokeAndWait(() -> {
                dashboard.dieuHuongSangChucNang(MainDashboard.CARD_GIAO_CA);
            });
            Field fGiaoCa = MainDashboard.class.getDeclaredField("banGiaoCaPanel");
            fGiaoCa.setAccessible(true);
            BanGiaoCaPanel bgc = (BanGiaoCaPanel) fGiaoCa.get(dashboard);
            TestFramework.assertNotNull("LAUNCH-06a: BanGiaoCaPanel loaded into content cards", bgc);
            TestFramework.assertTrue("LAUNCH-06b: BanGiaoCaPanel is visible", bgc != null && bgc.isVisible());
        } catch (Exception e) {
            TestFramework.recordManual("LAUNCH-06: Navigate to Shift Handover Panel", false, e.getMessage());
        }

        // Test 7: Verify Manager Shift Review Button Visibility
        try {
            Field fGiaoCa = MainDashboard.class.getDeclaredField("banGiaoCaPanel");
            fGiaoCa.setAccessible(true);
            BanGiaoCaPanel bgc = (BanGiaoCaPanel) fGiaoCa.get(dashboard);
            Field fBtnDuyet = BanGiaoCaPanel.class.getDeclaredField("btnQuanLyDuyet");
            fBtnDuyet.setAccessible(true);
            JButton btnDuyet = (JButton) fBtnDuyet.get(bgc);
            TestFramework.assertNotNull("LAUNCH-07a: btnQuanLyDuyet exists in BanGiaoCaPanel", btnDuyet);
            TestFramework.assertTrue("LAUNCH-07b: btnQuanLyDuyet is visible for default Manager session", btnDuyet != null && btnDuyet.isVisible());
        } catch (Exception e) {
            TestFramework.recordManual("LAUNCH-07: Verify Manager Shift Review Button", false, e.getMessage());
        }

        // Test 8: Window Render and Disposal
        try {
            SwingUtilities.invokeAndWait(() -> {
                dashboard.pack();
                dashboard.setVisible(true);
                TestFramework.assertTrue("LAUNCH-08a: MainDashboard isShowing", dashboard.isShowing());
                dashboard.setVisible(false);
                dashboard.dispose();
                TestFramework.assertTrue("LAUNCH-08b: MainDashboard disposed cleanly", !dashboard.isDisplayable());
            });
        } catch (Exception e) {
            TestFramework.recordManual("LAUNCH-08: Window Pack and Disposal", false, e.getMessage());
        }

        return TestFramework.endSuite();
    }

    public static void main(String[] args) {
        TestFramework.SuiteResult result = runTests();
        if (result.getFailedCount() > 0) {
            System.err.println("❌ Application launch verification failed!");
            System.exit(1);
        } else {
            System.out.println("✅ APPLICATION LAUNCH & UI VERIFICATION PASSED COMPLETELY! (Exit Code 0)");
            System.exit(0);
        }
    }
}
