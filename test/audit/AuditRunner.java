package audit;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** No dependency on JUnit or a new build system. Use -ea for application assertions. */
public final class AuditRunner {
    @FunctionalInterface interface Check { void run() throws Exception; }
    static final class Case {
        final String id, group, title;
        final Check check;
        Case(String id, String group, String title, Check check) {
            this.id = id; this.group = group; this.title = title; this.check = check;
        }
    }
    static final class Skip extends Exception {
        Skip(String reason) { super(reason); }
    }
    private AuditRunner() {}

    static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void rejects(Class<? extends Throwable> type, Check check) throws Exception {
        try { check.run(); }
        catch (Throwable e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("Sai loại lỗi: " + e.getClass().getSimpleName(), e);
        }
        throw new AssertionError("Đầu vào sai đã được chấp nhận");
    }

    public static void main(String[] args) throws Exception {
        String mode = args.length == 0 ? "all" : args[0].toLowerCase();
        if ("setup".equals(mode)) {
            TestDatabase.setup();
            return;
        }
        if (!List.of("all", "logic", "db", "external", "ui").contains(mode)) {
            System.err.println("Dùng: AuditRunner [all|logic|db|external|ui|setup]");
            System.exit(2);
        }
        List<Case> cases = new ArrayList<>();
        if (mode.equals("all") || mode.equals("logic")) LogicCases.add(cases);
        if (mode.equals("all") || mode.equals("db")) DatabaseCases.add(cases);
        if (mode.equals("all") || mode.equals("external")) ExternalCases.add(cases);
        if (mode.equals("all") || mode.equals("ui")) UiCases.add(cases);

        String dbUnavailable = null;
        if (mode.equals("all") || mode.equals("db")) {
            try (var ignored = TestDatabase.open()) {
                // This read-only preflight also verifies DB_NAME() before any test writes.
            } catch (Exception e) {
                dbUnavailable = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
        }
        int passed = 0, failed = 0, skipped = 0;
        List<String> lines = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        lines.add("PETSHOP AUDIT " + now);
        lines.add("Java=" + System.getProperty("java.version") + " mode=" + mode);
        lines.add("DB=" + (dbUnavailable == null ? "connected to " + TestDatabase.NAME : "unavailable"));
        for (Case test : cases) {
            String outcome, detail = "";
            try {
                if ("db".equals(test.group) && dbUnavailable != null) {
                    throw new Skip(dbUnavailable);
                }
                test.check.run();
                outcome = "PASS"; passed++;
            } catch (Skip e) {
                outcome = "SKIP"; skipped++; detail = e.getMessage();
            } catch (Throwable e) {
                outcome = "FAIL"; failed++;
                detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            String line = outcome + " " + test.id + " [" + test.group + "] " + test.title
                    + (detail.isBlank() ? "" : " :: " + detail.replace('\n', ' '));
            lines.add(line);
            System.out.println(line);
        }
        lines.add("SUMMARY PASS=" + passed + " FAIL=" + failed + " SKIP=" + skipped);
        Path output = Path.of("export", "audit-results-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".txt");
        Files.createDirectories(output.getParent());
        Files.write(output, lines, StandardCharsets.UTF_8);
        System.out.println(lines.get(lines.size() - 1));
        System.out.println("REPORT " + output.toAbsolutePath());
        if (failed > 0) System.exit(1);
        if (skipped > 0) System.exit(2);
    }
}
