package utils;

/** Escape CSV và vô hiệu hóa công thức do dữ liệu người dùng chèn vào Excel. */
public final class CsvHelper {
    private CsvHelper() {}

    public static String cell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        String stripped = text.stripLeading();
        if (!stripped.isEmpty() && "=+-@".indexOf(stripped.charAt(0)) >= 0) {
            text = "'" + text;
        }
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
