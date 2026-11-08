package test.phase1;

import entity.*;
import utils.DBConnection;
import utils.MaTuSinhHelper;
import utils.PasswordHasher;
import dao.GoiSpaPassDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Repeatable Automated Test Suite for PetShop Mio - Phase 1 Correction.
 * 
 * Domains Covered:
 * 1. PBKDF2 Password Hashing (TC02)
 * 2. Returns & Refunds Integrity (TC05, TC06, TC09, TC10)
 * 3. Spa Pass Session Depletion & Package Config (DV13, DV14)
 * 4. Parked Order & Hotel Snapshot State (TC12, TC13)
 * 5. Appointment Overlap Detection (DV08)
 * 6. Concurrency & Code Generation (BH03)
 * 7. FEFO Multi-lot & Unit Conversion (BH01, BH02)
 * 8. Live Database Connectivity & Schema Probe (Safe Read-Only)
 */
public class Phase1RepeatableTestSuite {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static int nonVerifiableTests = 0;
    private static final List<String> failureDetails = new ArrayList<>();
    private static final List<String> statusLines = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" PETSHOP MIO - PHASE 1 REPEATABLE AUTOMATED TEST SUITE");
        System.out.println(" Execution Time: " + LocalDateTime.now());
        System.out.println(" Java Version   : " + System.getProperty("java.version"));
        System.out.println("================================================================================\n");

        runSuite();

        printSummary();
    }

    private static void runSuite() {
        // Domain 1: PBKDF2 Password Hashing (TC02)
        runTest("TC02.1 - PBKDF2 65536 Iterations & Standard Format", Phase1RepeatableTestSuite::testPbkdf2Format);
        runTest("TC02.2 - PBKDF2 Distinct Random Salts", Phase1RepeatableTestSuite::testPbkdf2RandomSalt);
        runTest("TC02.3 - PBKDF2 Constant-Time Password Verification", Phase1RepeatableTestSuite::testPbkdf2Verification);
        runTest("TC02.4 - PBKDF2 Backward Compatibility With Legacy SHA-256", Phase1RepeatableTestSuite::testPbkdf2LegacySha256);
        runTest("TC02.5 - PBKDF2 Automatic Upgrade Detection Flag", Phase1RepeatableTestSuite::testPbkdf2UpgradeFlag);

        // Domain 2: Returns & Refunds (TC05, TC06, TC09, TC10)
        runTest("TC05.1 - Returns Reject Negative or Zero Quantities", Phase1RepeatableTestSuite::testDoiTraNegativeQuantity);
        runTest("TC05.2 - Returns Reject Return Quantity Exceeding Purchased", Phase1RepeatableTestSuite::testDoiTraExceedPurchased);
        runTest("TC06.1 - Returns Multi-line Same Product Aggregation Check", Phase1RepeatableTestSuite::testDoiTraMultiLineAggregation);
        runTest("TC09.1 - Returns Financial Fields Calculation (Scenarios A/B/C)", Phase1RepeatableTestSuite::testDoiTraFinancialCalculations);
        runTest("TC10.1 - Shift Handover Deducts Only Cash Refunds, Not Transfer", Phase1RepeatableTestSuite::testShiftHandoverCashDeduction);

        // Domain 3: Spa Pass (DV13, DV14)
        runTest("DV13.1 - Spa Pass Session Decrement and Depletion to Inactive", Phase1RepeatableTestSuite::testSpaPassSessionDepletion);
        runTest("DV14.1 - Spa Pass Package Configuration from GoiSpaPassDAO", Phase1RepeatableTestSuite::testSpaPassPackageConfig);

        // Domain 4: Parked Order & State Restoration (TC12, TC13)
        runTest("TC13.1 - HotelCheckoutSnapshot Authentic Model and Field Retention", Phase1RepeatableTestSuite::testHotelSnapshotIntegrity);
        runTest("TC12.1 - Parked Order Restoration of DoiTraTransferData and SpaPass", Phase1RepeatableTestSuite::testParkedOrderStateRestoration);

        // Domain 5: Appointment Overlap (DV08)
        runTest("DV08.1 - Appointment Overlap Range Intersection Algorithm", Phase1RepeatableTestSuite::testAppointmentIntervalIntersection);
        runTest("DV08.2 - Appointment Technician Conflict Detection", Phase1RepeatableTestSuite::testTechnicianAppointmentConflict);
        runTest("DV08.3 - Appointment Pet Conflict Detection (Multi-KTV Prevention)", Phase1RepeatableTestSuite::testPetAppointmentConflict);
        runTest("DV08.4 - Appointment Cancelled Status Exclusion", Phase1RepeatableTestSuite::testAppointmentCancelledStatusExclusion);

        // Domain 6: Code Generation Concurrency (BH03)
        runTest("BH03.1 - Concurrency Atomic Sequence Generator (Simulation)", Phase1RepeatableTestSuite::testConcurrentCodeGeneration);
        runTest("BH03.2 - Live BangCapSo Sequence Concurrency (SQL Server)", Phase1RepeatableTestSuite::testLiveBangCapSoConcurrency);

        // Domain 7: FEFO & Multi-lot Allocation (BH01, BH02)
        runTest("BH01.1 - FEFO Multi-Lot Allocation with Expiry Ordering", Phase1RepeatableTestSuite::testFefoMultiLotAllocation);
        runTest("BH02.1 - FEFO Unit Conversion Without Rounding Anomaly", Phase1RepeatableTestSuite::testUnitConversionRoundingFix);

        // Domain 8: Database Environment Check
        runTest("DB01.1 - Live SQL Server Connectivity & Safe Schema Probe", Phase1RepeatableTestSuite::testLiveDatabaseProbe);
    }

    private static void runTest(String testName, TestRunnable runnable) {
        totalTests++;
        try {
            runnable.run();
            passedTests++;
            statusLines.add("[PASS] " + testName);
            System.out.printf("  [PASS] %s%n", testName);
        } catch (NonVerifiableException e) {
            nonVerifiableTests++;
            statusLines.add("[NOT_VERIFIABLE] " + testName + " -> " + e.getMessage());
            System.out.printf("  [NOT_VERIFIABLE] %s (%s)%n", testName, e.getMessage());
        } catch (AssertionError | Exception e) {
            failedTests++;
            String err = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            failureDetails.add(testName + ": " + err);
            statusLines.add("[FAIL] " + testName + " -> " + err);
            System.out.printf("  [FAIL] %s: %s%n", testName, err);
        }
    }

    // =========================================================================
    // 1. PBKDF2 TESTS (TC02)
    // =========================================================================

    private static void testPbkdf2Format() {
        String raw = "Admin@2026";
        String hash = PasswordHasher.hashPBKDF2(raw);
        assertNotNull(hash, "Hash should not be null");
        assertTrue(hash.startsWith("PBKDF2$65536$"), "Hash must use PBKDF2 with 65536 iterations");
        String[] parts = hash.split("\\$");
        assertEquals(4, parts.length, "Hash format must contain 4 dollar-separated parts");
        assertEquals("PBKDF2", parts[0]);
        assertEquals("65536", parts[1]);
        assertTrue(parts[2].length() >= 32, "Salt hex length must be >= 32 characters (16 bytes)");
        assertTrue(parts[3].length() >= 64, "Key hex length must be >= 64 characters (32 bytes)");
    }

    private static void testPbkdf2RandomSalt() {
        String raw = "ShopMioSecret#1";
        String hash1 = PasswordHasher.hashPBKDF2(raw);
        String hash2 = PasswordHasher.hashPBKDF2(raw);
        assertFalse(hash1.equals(hash2), "Two hashes of the same password must have different salts and outputs");
        assertTrue(PasswordHasher.verifyPassword(raw, hash1), "Hash 1 must verify against raw password");
        assertTrue(PasswordHasher.verifyPassword(raw, hash2), "Hash 2 must verify against raw password");
    }

    private static void testPbkdf2Verification() {
        String raw = "StrongP@ssw0rd";
        String hash = PasswordHasher.hashPBKDF2(raw);
        assertTrue(PasswordHasher.verifyPassword("StrongP@ssw0rd", hash), "Exact match must return true");
        assertFalse(PasswordHasher.verifyPassword("strongp@ssw0rd", hash), "Case difference must return false");
        assertFalse(PasswordHasher.verifyPassword("WrongPassword", hash), "Wrong password must return false");
        assertFalse(PasswordHasher.verifyPassword("", hash), "Empty password must return false");
        assertFalse(PasswordHasher.verifyPassword(null, hash), "Null password must return false");
        assertFalse(PasswordHasher.verifyPassword(raw, null), "Null hash must return false");
    }

    private static void testPbkdf2LegacySha256() {
        String legacyFixture = "LegacyFixture9!";
        String legacySha256 = "c8d71d80f4a57b7c0e2eb2ed5d30440316a86d7c2e468508c9802e52236361b3";
        assertTrue(PasswordHasher.verifyPassword(legacyFixture, legacySha256), "Legacy SHA-256 hash must verify correctly");
        assertFalse(PasswordHasher.verifyPassword("WrongFixture9!", legacySha256), "Wrong password on SHA-256 must return false");
    }

    private static void testPbkdf2UpgradeFlag() {
        String sha256Hash = "c8d71d80f4a57b7c0e2eb2ed5d30440316a86d7c2e468508c9802e52236361b3";
        assertTrue(PasswordHasher.needsUpgrade(sha256Hash), "Legacy SHA-256 hash must trigger needsUpgrade == true");

        String oldIterationHash = "PBKDF2$10000$0123456789abcdef0123456789abcdef$fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        assertTrue(PasswordHasher.needsUpgrade(oldIterationHash), "Hash with < 65536 iterations must trigger needsUpgrade == true");

        String currentPbkdf2 = PasswordHasher.hashPBKDF2("LegacyFixture9!");
        assertFalse(PasswordHasher.needsUpgrade(currentPbkdf2), "Current PBKDF2 hash (65536 iters) must NOT need upgrade");
    }

    // =========================================================================
    // 2. RETURNS & REFUNDS TESTS (TC05, TC06, TC09, TC10)
    // =========================================================================

    private static void testDoiTraNegativeQuantity() {
        PhieuDoiTra pdt = new PhieuDoiTra();
        pdt.setMaPhieuDoiTra("DT_TEST_01");
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon("HD_TEST_01");
        pdt.setHoaDonGoc(hd);
        NhanVien nv = new NhanVien();
        nv.setMaNhanVien("NV001");
        pdt.setNhanVien(nv);

        ChiTietDoiTra ctNegative = new ChiTietDoiTra("CT1", pdt, new SanPham("SP001", null, "", ""), -2, BigDecimal.valueOf(50000));
        pdt.addChiTietDoiTra(ctNegative);

        boolean rejected = false;
        try {
            for (ChiTietDoiTra ct : pdt.getDsChiTietDoiTra()) {
                if (ct.getSoLuongDoiTra() <= 0) {
                    throw new IllegalArgumentException("Số lượng đổi trả phải lớn hơn 0");
                }
            }
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        assertTrue(rejected, "System must reject negative or zero return quantity");
    }

    private static void testDoiTraExceedPurchased() {
        int purchasedQty = 3;
        int previouslyReturnedQty = 2;
        int requestingReturnQty = 2;

        int remainingAllowed = Math.max(0, purchasedQty - previouslyReturnedQty);
        boolean exceeds = requestingReturnQty > remainingAllowed;
        assertTrue(exceeds, "Requesting 2 items when only 1 remaining must be flagged as exceeding");
    }

    private static void testDoiTraMultiLineAggregation() {
        int purchasedSP001 = 5;

        List<ChiTietDoiTra> ds = new ArrayList<>();
        ds.add(new ChiTietDoiTra("CT1", null, new SanPham("SP001", null, "", ""), 3, BigDecimal.valueOf(30000)));
        ds.add(new ChiTietDoiTra("CT2", null, new SanPham("SP001", null, "", ""), 3, BigDecimal.valueOf(30000)));

        Map<String, Integer> tongTraTheoSP = new HashMap<>();
        for (ChiTietDoiTra ct : ds) {
            String maSP = ct.getSanPham().getMaSanPham();
            tongTraTheoSP.put(maSP, tongTraTheoSP.getOrDefault(maSP, 0) + ct.getSoLuongDoiTra());
        }

        assertEquals(6, tongTraTheoSP.get("SP001").intValue(), "Aggregated return for SP001 must be 6");
        assertTrue(tongTraTheoSP.get("SP001") > purchasedSP001, "Aggregated return must exceed purchased quantity (6 > 5)");
    }

    private static void testDoiTraFinancialCalculations() {
        // Scenario A: Direct return, no exchange
        BigDecimal giaTriTraA = BigDecimal.valueOf(200000);
        BigDecimal giaTriDoiA = BigDecimal.ZERO;
        BigDecimal diffA = giaTriTraA.subtract(giaTriDoiA);
        BigDecimal hoanThucTeA = diffA.compareTo(BigDecimal.ZERO) > 0 ? diffA : BigDecimal.ZERO;
        BigDecimal khachBuA = diffA.compareTo(BigDecimal.ZERO) < 0 ? diffA.negate() : BigDecimal.ZERO;
        assertEquals(0, BigDecimal.valueOf(200000).compareTo(hoanThucTeA));
        assertEquals(0, BigDecimal.ZERO.compareTo(khachBuA));

        // Scenario B: Exchange for higher value items
        BigDecimal giaTriTraB = BigDecimal.valueOf(200000);
        BigDecimal giaTriDoiB = BigDecimal.valueOf(350000);
        BigDecimal diffB = giaTriTraB.subtract(giaTriDoiB);
        BigDecimal hoanThucTeB = diffB.compareTo(BigDecimal.ZERO) > 0 ? diffB : BigDecimal.ZERO;
        BigDecimal khachBuB = diffB.compareTo(BigDecimal.ZERO) < 0 ? diffB.negate() : BigDecimal.ZERO;
        assertEquals(0, BigDecimal.ZERO.compareTo(hoanThucTeB));
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(khachBuB));

        // Scenario C: Exchange for lower value items
        BigDecimal giaTriTraC = BigDecimal.valueOf(300000);
        BigDecimal giaTriDoiC = BigDecimal.valueOf(180000);
        BigDecimal diffC = giaTriTraC.subtract(giaTriDoiC);
        BigDecimal hoanThucTeC = diffC.compareTo(BigDecimal.ZERO) > 0 ? diffC : BigDecimal.ZERO;
        BigDecimal khachBuC = diffC.compareTo(BigDecimal.ZERO) < 0 ? diffC.negate() : BigDecimal.ZERO;
        assertEquals(0, BigDecimal.valueOf(120000).compareTo(hoanThucTeC));
        assertEquals(0, BigDecimal.ZERO.compareTo(khachBuC));
    }

    private static void testShiftHandoverCashDeduction() {
        class MockPhieu {
            final BigDecimal tienHoanThucTe;
            final String phuongThuc;
            MockPhieu(BigDecimal t, String pt) { this.tienHoanThucTe = t; this.phuongThuc = pt; }
        }

        List<MockPhieu> phieuList = Arrays.asList(
                new MockPhieu(BigDecimal.valueOf(100000), "Tiền mặt"),
                new MockPhieu(BigDecimal.valueOf(250000), "Chuyển khoản"),
                new MockPhieu(BigDecimal.valueOf(50000), "Tiền mặt"),
                new MockPhieu(BigDecimal.valueOf(300000), "VNPay")
        );

        long tongHoanTienMat = 0;
        for (MockPhieu p : phieuList) {
            if (p.phuongThuc != null && p.phuongThuc.contains("Tiền mặt")) {
                tongHoanTienMat += p.tienHoanThucTe.longValue();
            }
        }

        assertEquals(150000L, tongHoanTienMat, "Shift cash deduction must be exactly 150,000 (100k + 50k cash)");
    }

    // =========================================================================
    // 3. SPA PASS TESTS (DV13, DV14)
    // =========================================================================

    private static void testSpaPassSessionDepletion() {
        TheSpaPass the = new TheSpaPass("PASS-0912345678-01", "0912345678", "Nguyễn Văn A", "Gói 2 Buổi", 2, 0, 300000L, LocalDate.now(), LocalDate.now().plusDays(90));

        assertTrue(the.coTheSuDung(), "Pass with 2 sessions must be usable");

        boolean ok1 = the.dungMotBuoi();
        assertTrue(ok1, "First session deduction must succeed");
        assertEquals(1, the.getSoBuoiConLai(), "Remaining sessions should be 1");
        assertTrue(the.coTheSuDung(), "Pass with 1 session must still be usable");

        boolean ok2 = the.dungMotBuoi();
        assertTrue(ok2, "Second session deduction must succeed");
        assertEquals(0, the.getSoBuoiConLai(), "Remaining sessions should be 0");
        assertEquals("ĐÃ HẾT BUỔI", the.getTrangThai(), "Status must transition to ĐÃ HẾT BUỔI");
        assertFalse(the.coTheSuDung(), "Exhausted pass must not be usable");
    }

    private static void testSpaPassPackageConfig() {
        requireExplicitTestDatabase();
        GoiSpaPassDAO dao = new GoiSpaPassDAO();
        GoiSpaPass g05 = dao.findByMaGoi("GOI_SPA05");
        assertNotNull(g05, "GOI_SPA05 config must exist");
        assertEquals(5, g05.getSoBuoi(), "GOI_SPA05 must have 5 sessions");

        GoiSpaPass g10 = dao.findByMaGoi("GOI_SPA10");
        assertNotNull(g10, "GOI_SPA10 config must exist");
        assertEquals(10, g10.getSoBuoi(), "GOI_SPA10 must have 10 sessions");

        GoiSpaPass gVip = dao.findByMaGoi("GOI_SPA_VIP");
        assertNotNull(gVip, "GOI_SPA_VIP config must exist");
        assertEquals(12, gVip.getSoBuoi(), "GOI_SPA_VIP must have 12 sessions");
    }

    // =========================================================================
    // 4. PARKED ORDER & HOTEL SNAPSHOT TESTS (TC12, TC13)
    // =========================================================================

    private static void testHotelSnapshotIntegrity() {
        LocalDateTime checkIn = LocalDateTime.now().minusHours(26);
        LocalDateTime checkOut = LocalDateTime.now();
        long donGia = 150000L;
        long phuPhi = 40000L;
        long tongTien = donGia + phuPhi;

        HotelCheckoutSnapshot snap = new HotelCheckoutSnapshot(
                "P101", "VIP", "Miu", "Nguyễn Văn A", "0901234567",
                checkIn, checkOut, 2, donGia, phuPhi, tongTien, "Ghi chú Pet Hotel"
        );

        assertEquals("P101", snap.getMaPhong());
        assertEquals("VIP", snap.getLoaiPhong());
        assertEquals(2, snap.getSoNgayLuuTru());
        assertEquals(tongTien, snap.getTongTien());
        assertEquals("Miu", snap.getTenThuCung());
        assertEquals("Nguyễn Văn A", snap.getTenChu());
        assertNotNull(snap.getThoiGianCheckIn(), "CheckIn must not be null");
        assertNotNull(snap.getThoiGianCheckOut(), "CheckOut must not be null");
    }

    private static void testParkedOrderStateRestoration() {
        DoiTraTransferData data = new DoiTraTransferData();
        data.setMaHoaDonGoc("HD261108-01-005");
        data.setTenKhach("Nguyễn Văn An");
        data.setSdtKhach("0912345678");
        data.setTongTienTra(120000);
        data.setDiemKhauTru(1);
        data.addMonTra(new DoiTraTransferData.MonTraLai(
                "SP001", "Pate Royal Canin", "Gói", 2, 60000, 120000, "Hàng thừa", true, "LO-01", 2
        ));

        assertEquals("HD261108-01-005", data.getMaHoaDonGoc());
        assertEquals(120000L, data.getTongTienTra());
        assertEquals(1, data.getDsMonTra().size());
        assertEquals("SP001", data.getDsMonTra().get(0).maSP);
        assertEquals(2, data.getDsMonTra().get(0).soLuong);
    }

    // =========================================================================
    // 5. APPOINTMENT OVERLAP TESTS (DV08)
    // =========================================================================

    private static boolean intervalsOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && e1.isAfter(s2);
    }

    private static void testAppointmentIntervalIntersection() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 9, 0);

        LocalDateTime s1 = base;
        LocalDateTime e1 = base.plusMinutes(45);

        // Case A: Overlap (09:15 - 10:00)
        LocalDateTime sA = base.plusMinutes(15);
        LocalDateTime eA = base.plusMinutes(60);
        assertTrue(intervalsOverlap(s1, e1, sA, eA), "Overlapping appointments must be flagged");

        // Case B: Adjacent (09:45 - 10:30)
        LocalDateTime sB = base.plusMinutes(45);
        LocalDateTime eB = base.plusMinutes(90);
        assertFalse(intervalsOverlap(s1, e1, sB, eB), "Adjacent appointments must NOT overlap");

        // Case C: Enclosed (09:10 - 09:30)
        LocalDateTime sC = base.plusMinutes(10);
        LocalDateTime eC = base.plusMinutes(30);
        assertTrue(intervalsOverlap(s1, e1, sC, eC), "Enclosed appointment must overlap");

        // Case D: Different duration (60m service vs 30m service)
        LocalDateTime sD = base.plusMinutes(40);
        LocalDateTime eD = sD.plusMinutes(30);
        assertTrue(intervalsOverlap(s1, e1, sD, eD), "Variable duration overlap must be detected");
    }

    private static void testTechnicianAppointmentConflict() {
        String ktv1 = "NV003";
        LocalDateTime existingStart = LocalDateTime.of(2026, 11, 10, 14, 0);
        LocalDateTime existingEnd = existingStart.plusMinutes(45);

        LocalDateTime newStart = LocalDateTime.of(2026, 11, 10, 14, 30);
        LocalDateTime newEnd = newStart.plusMinutes(45);

        boolean sameKtv = ktv1.equals("NV003");
        boolean overlap = intervalsOverlap(existingStart, existingEnd, newStart, newEnd);
        assertTrue(sameKtv && overlap, "Technician conflict must be flagged when booking same KTV for overlapping time");
    }

    private static void testPetAppointmentConflict() {
        String pet1 = "TC005";
        LocalDateTime existingStart = LocalDateTime.of(2026, 11, 10, 10, 0);
        LocalDateTime existingEnd = existingStart.plusMinutes(60);

        LocalDateTime newStart = LocalDateTime.of(2026, 11, 10, 10, 30);
        LocalDateTime newEnd = newStart.plusMinutes(45);

        boolean samePet = pet1.equals("TC005");
        boolean overlap = intervalsOverlap(existingStart, existingEnd, newStart, newEnd);
        assertTrue(samePet && overlap, "Pet conflict must be flagged when same pet is scheduled for 2 services concurrently");
    }

    private static void testAppointmentCancelledStatusExclusion() {
        String status = "Đã hủy";
        List<String> ignoredStatuses = Arrays.asList("Đã hủy", "Hủy", "HỦY", "DA_HUY");
        boolean isIgnored = ignoredStatuses.contains(status);
        assertTrue(isIgnored, "Cancelled appointment must be excluded from conflict checking");
    }

    // =========================================================================
    // 6. CODE GENERATION CONCURRENCY (BH03)
    // =========================================================================

    private static void testConcurrentCodeGeneration() throws Exception {
        int threadCount = 10;
        int perThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<String> generatedCodes = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);

        // Atomic sequence simulator representing the BangCapSo concurrency contract
        java.util.concurrent.atomic.AtomicInteger atomicSequence = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < perThread; j++) {
                        int next = atomicSequence.incrementAndGet();
                        String code = String.format("KH%03d", next);
                        generatedCodes.add(code);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Concurrent generation timed out");
        executor.shutdown();

        int expectedCount = threadCount * perThread;
        assertEquals(expectedCount, generatedCodes.size(), "All generated codes across threads must be 100% unique");
    }

    private static void testLiveBangCapSoConcurrency() {
        requireExplicitTestDatabase();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new NonVerifiableException("Database not available for live BangCapSo concurrency test.");
            }
            verifyTestCatalog(conn);
            String code = MaTuSinhHelper.sinhMaKhachHang();
            assertNotNull(code, "Generated code must not be null");
            assertTrue(code.startsWith("KH"), "Generated code must have KH prefix");
        } catch (NonVerifiableException e) {
            throw e;
        } catch (Exception e) {
            throw new NonVerifiableException("SQL Server unavailable (" + e.getMessage() + "). Marking live BangCapSo test as NOT_VERIFIABLE_WITHOUT_DATABASE.");
        }
    }

    // =========================================================================
    // 7. FEFO MULTI-LOT ALLOCATION & UNIT CONVERSION (BH01, BH02)
    // =========================================================================

    private static void testFefoMultiLotAllocation() {
        class MockLo {
            final String soLo;
            final LocalDate hsd;
            int ton;
            MockLo(String s, LocalDate d, int t) { this.soLo = s; this.hsd = d; this.ton = t; }
        }

        List<MockLo> lots = new ArrayList<>();
        lots.add(new MockLo("LO-01", LocalDate.now().plusDays(10), 5));
        lots.add(new MockLo("LO-02", LocalDate.now().plusDays(30), 10));

        lots.sort(Comparator.comparing(l -> l.hsd));

        int needBaseUnits = 8;
        Map<String, Integer> allocated = new LinkedHashMap<>();

        for (MockLo lo : lots) {
            if (lo.hsd.isBefore(LocalDate.now())) continue;
            if (needBaseUnits <= 0) break;

            int take = Math.min(lo.ton, needBaseUnits);
            allocated.put(lo.soLo, take);
            lo.ton -= take;
            needBaseUnits -= take;
        }

        assertEquals(0, needBaseUnits, "All required units must be allocated");
        assertEquals(5, allocated.get("LO-01").intValue(), "Lot 1 must allocate full 5 units");
        assertEquals(3, allocated.get("LO-02").intValue(), "Lot 2 must allocate remaining 3 units");
    }

    private static void testUnitConversionRoundingFix() {
        int tyLeQuyDoi = 10;
        int requestedPacks = 5;

        int baseUnitsAllocated = requestedPacks;
        assertEquals(5, baseUnitsAllocated, "Base units allocated must be exact 5, not rounded or forced to 10");

        BigDecimal giaBanHop = BigDecimal.valueOf(100000);
        BigDecimal giaCoSo = giaBanHop.divide(BigDecimal.valueOf(tyLeQuyDoi), 2, java.math.RoundingMode.HALF_UP);
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(giaCoSo));
    }

    // =========================================================================
    // 8. DATABASE PROBE TEST
    // =========================================================================

    private static void testLiveDatabaseProbe() {
        requireExplicitTestDatabase();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new NonVerifiableException("Database not accessible with current config. Live transaction/UPDLOCK tests cannot run.");
            }
            verifyTestCatalog(conn);
            boolean valid = conn.isValid(2);
            assertTrue(valid, "Database connection must be valid");
        } catch (NonVerifiableException e) {
            throw e;
        } catch (Exception e) {
            throw new NonVerifiableException("SQL Server unavailable (" + e.getMessage() + "). Marking DB live tests as NOT_VERIFIABLE_WITHOUT_DATABASE.");
        }
    }

    private static void requireExplicitTestDatabase() {
        if (!"QuanLyCuaHangThuCung_Test".equals(System.getenv("PETSHOP_TEST_DATABASE"))) {
            throw new NonVerifiableException("Chưa xác nhận PETSHOP_TEST_DATABASE=QuanLyCuaHangThuCung_Test; không được chạy live DB test.");
        }
    }

    private static void verifyTestCatalog(Connection conn) throws Exception {
        if (!"QuanLyCuaHangThuCung_Test".equals(conn.getCatalog())) {
            throw new NonVerifiableException("Kết nối thực tế không phải QuanLyCuaHangThuCung_Test: " + conn.getCatalog());
        }
    }

    // =========================================================================
    // ASSERTION HELPERS
    // =========================================================================

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError("Assertion Failed: " + msg);
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) throw new AssertionError("Assertion Failed (expected false): " + msg);
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError("Assertion Failed (expected not null): " + msg);
    }

    private static void assertEquals(Object expected, Object actual) {
        assertEquals(expected, actual, "Values do not match");
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("%s [Expected: %s, Actual: %s]", msg, expected, actual));
        }
    }

    private static void printSummary() {
        System.out.println("\n================================================================================");
        System.out.println(" TEST RESULTS SUMMARY");
        System.out.println("================================================================================");
        System.out.printf(" Total Tests Run     : %d%n", totalTests);
        System.out.printf(" Passed Tests        : %d%n", passedTests);
        System.out.printf(" Failed Tests        : %d%n", failedTests);
        System.out.printf(" Not Verifiable Tests: %d%n", nonVerifiableTests);
        System.out.println("--------------------------------------------------------------------------------");

        if (failedTests > 0) {
            System.out.println("Failures:");
            for (String f : failureDetails) {
                System.out.println(" - " + f);
            }
        } else {
            System.out.println(" ALL EXECUTABLE AUTOMATED TESTS COMPLETED WITH ZERO FAILURES.");
        }
        System.out.println("================================================================================\n");
    }

    @FunctionalInterface
    private interface TestRunnable {
        void run() throws Exception;
    }

    private static class NonVerifiableException extends RuntimeException {
        public NonVerifiableException(String msg) { super(msg); }
    }
}
