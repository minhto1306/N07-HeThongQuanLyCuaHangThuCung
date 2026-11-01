package tests;

import java.util.ArrayList;
import java.util.List;

/**
 * Master E2E Automated Test Runner for Mio Pet Shop System.
 * Sequentially executes all test suites, aggregates results across Tiers 1-4,
 * and outputs a comprehensive pass/fail summary report.
 * Returns process exit code 0 if all tests pass, non-zero if any test fails.
 */
public class ComprehensiveE2ETestRunner {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        System.out.println("################################################################################");
        System.out.println("             HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG MIO (NHÓM 07)                   ");
        System.out.println("              MASTER AUTOMATED E2E TEST SUITE RUNNER                           ");
        System.out.println("################################################################################");
        System.out.println("Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("JDK Version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        System.out.println();

        List<TestFramework.SuiteResult> suiteResults = new ArrayList<>();

        // 1. Suite 1: POS Payment & Change Logic
        suiteResults.add(POSPaymentTest.runTests());
        suiteResults.add(POSPaymentAdversarialTest.runAdversarialSuite());

        // 2. Suite 2: Exchange Flow to POS & Net Due Calculation
        suiteResults.add(ExchangeFlowTest.runTests());
        suiteResults.add(ExchangeFlowAdversarialTest.runAdversarialSuite());

        // 3. Suite 3: Shift Handover & Manager Review Workflow
        suiteResults.add(ShiftHandoverTest.runTests());

        long totalDuration = System.currentTimeMillis() - startTime;

        // Print Aggregated Summary Table
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                      COMPREHENSIVE TEST EXECUTION SUMMARY                      ");
        System.out.println("================================================================================");
        System.out.printf("| %-38s | %6s | %5s | %5s | %8s | %8s |%n",
                "Test Suite", "Total", "PASS", "FAIL", "Time", "Status");
        System.out.println("+----------------------------------------+--------+-------+-------+----------+----------+");

        int totalAll = 0;
        int passedAll = 0;
        int failedAll = 0;

        for (TestFramework.SuiteResult r : suiteResults) {
            int tCount = r.tests.size();
            int pCount = r.getPassedCount();
            int fCount = r.getFailedCount();
            totalAll += tCount;
            passedAll += pCount;
            failedAll += fCount;

            String statusStr = (fCount == 0) ? "[PASS]" : "[FAIL]";
            System.out.printf("| %-38s | %6d | %5d | %5d | %6d ms | %8s |%n",
                    r.suiteName.length() > 38 ? r.suiteName.substring(0, 38) : r.suiteName,
                    tCount, pCount, fCount, r.getTotalDurationMs(), statusStr);
        }

        System.out.println("+----------------------------------------+--------+-------+-------+----------+----------+");
        System.out.printf("| %-38s | %6d | %5d | %5d | %6d ms | %8s |%n",
                "TOTAL AGGREGATE", totalAll, passedAll, failedAll, totalDuration,
                (failedAll == 0 ? "[ALL OK]" : "[FAILED]"));
        System.out.println("================================================================================");

        if (failedAll > 0) {
            System.err.println();
            System.err.println(">>> FAILED TESTS DETAILS:");
            for (TestFramework.SuiteResult r : suiteResults) {
                for (TestFramework.TestCaseResult tc : r.tests) {
                    if (!tc.passed) {
                        System.err.printf("  * [%s] -> %s: %s%n", r.suiteName, tc.name, tc.message);
                    }
                }
            }
            System.err.println();
            System.err.println("❌ OVERALL STATUS: SOME TESTS FAILED (Exit Code 1)");
            System.exit(1);
        } else {
            System.out.println();
            System.out.println("✅ OVERALL STATUS: ALL 100% OF TESTS PASSED CLEANLY! (Exit Code 0)");
            System.exit(0);
        }
    }
}
