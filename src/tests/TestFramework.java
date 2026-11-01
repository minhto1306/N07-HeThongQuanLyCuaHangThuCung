package tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lightweight assertion and test harness framework for Java Swing & POS tests.
 * Zero external dependencies (runs on pure JDK 17).
 */
public class TestFramework {

    public static class TestCaseResult {
        public final String name;
        public final boolean passed;
        public final String message;
        public final long durationMs;

        public TestCaseResult(String name, boolean passed, String message, long durationMs) {
            this.name = name;
            this.passed = passed;
            this.message = message;
            this.durationMs = durationMs;
        }
    }

    public static class SuiteResult {
        public final String suiteName;
        public final List<TestCaseResult> tests = new ArrayList<>();
        public long startTimeMs;
        public long endTimeMs;

        public SuiteResult(String suiteName) {
            this.suiteName = suiteName;
            this.startTimeMs = System.currentTimeMillis();
        }

        public void finish() {
            this.endTimeMs = System.currentTimeMillis();
        }

        public int getPassedCount() {
            int c = 0;
            for (TestCaseResult r : tests) if (r.passed) c++;
            return c;
        }

        public int getFailedCount() {
            int c = 0;
            for (TestCaseResult r : tests) if (!r.passed) c++;
            return c;
        }

        public long getTotalDurationMs() {
            return endTimeMs > 0 ? (endTimeMs - startTimeMs) : (System.currentTimeMillis() - startTimeMs);
        }

        public boolean isAllPassed() {
            return getFailedCount() == 0;
        }
    }

    private static SuiteResult currentSuite = null;

    public static void startSuite(String suiteName) {
        currentSuite = new SuiteResult(suiteName);
        System.out.println();
        System.out.println("================================================================================");
        System.out.println(">>> RUNNING TEST SUITE: " + suiteName);
        System.out.println("================================================================================");
    }

    public static SuiteResult endSuite() {
        if (currentSuite != null) {
            currentSuite.finish();
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("  SUITE RESULT: %s | Total: %d | PASS: %d | FAIL: %d | Time: %d ms%n",
                    currentSuite.suiteName, currentSuite.tests.size(),
                    currentSuite.getPassedCount(), currentSuite.getFailedCount(),
                    currentSuite.getTotalDurationMs());
            System.out.println("================================================================================");
        }
        return currentSuite;
    }

    public static void assertEquals(String testName, Object expected, Object actual) {
        long start = System.currentTimeMillis();
        boolean ok = Objects.equals(expected, actual);
        recordTest(testName, ok, ok ? null : String.format("Expected [%s] but got [%s]", expected, actual), start);
    }

    public static void assertEquals(String testName, long expected, long actual) {
        long start = System.currentTimeMillis();
        boolean ok = (expected == actual);
        recordTest(testName, ok, ok ? null : String.format("Expected [%d] but got [%d]", expected, actual), start);
    }

    public static void assertEquals(String testName, double expected, double actual, double delta) {
        long start = System.currentTimeMillis();
        boolean ok = Math.abs(expected - actual) <= delta;
        recordTest(testName, ok, ok ? null : String.format("Expected [%f] but got [%f] (delta %f)", expected, actual, delta), start);
    }

    public static void assertTrue(String testName, boolean condition) {
        long start = System.currentTimeMillis();
        recordTest(testName, condition, condition ? null : "Expected condition to be TRUE but was FALSE", start);
    }

    public static void assertFalse(String testName, boolean condition) {
        long start = System.currentTimeMillis();
        recordTest(testName, !condition, !condition ? null : "Expected condition to be FALSE but was TRUE", start);
    }

    public static void assertNotNull(String testName, Object obj) {
        long start = System.currentTimeMillis();
        boolean ok = (obj != null);
        recordTest(testName, ok, ok ? null : "Expected non-null object but was NULL", start);
    }

    public static void assertNull(String testName, Object obj) {
        long start = System.currentTimeMillis();
        boolean ok = (obj == null);
        recordTest(testName, ok, ok ? null : "Expected NULL object but got " + obj, start);
    }

    public static void assertContains(String testName, String container, String expectedSubstring) {
        long start = System.currentTimeMillis();
        boolean ok = container != null && expectedSubstring != null && container.contains(expectedSubstring);
        recordTest(testName, ok, ok ? null : String.format("Expected text to contain [%s], but was [%s]", expectedSubstring, container), start);
    }

    public static void recordManual(String testName, boolean passed, String message) {
        recordTest(testName, passed, message, System.currentTimeMillis());
    }

    private static void recordTest(String testName, boolean passed, String errorMsg, long startTime) {
        long dur = System.currentTimeMillis() - startTime;
        TestCaseResult result = new TestCaseResult(testName, passed, errorMsg, dur);
        if (currentSuite != null) {
            currentSuite.tests.add(result);
        }

        if (passed) {
            System.out.printf("  [PASS] %-62s (%d ms)%n", testName, dur);
        } else {
            System.out.printf("  [FAIL] %-62s (%d ms)%n", testName, dur);
            System.err.printf("         --> ERROR: %s%n", errorMsg);
        }
    }
}
