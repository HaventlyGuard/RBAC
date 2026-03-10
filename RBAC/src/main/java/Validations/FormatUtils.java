package Validations;

import java.util.List;

public class FormatUtils {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private FormatUtils() {
    }

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                if (row[i] != null && row[i].length() > colWidths[i]) {
                    colWidths[i] = row[i].length();
                }
            }
        }

        for (int i = 0; i < colWidths.length; i++) {
            colWidths[i] = Math.min(colWidths[i] + 2, 40);
        }

        StringBuilder sb = new StringBuilder();

        sb.append(buildSeparator(colWidths, "┌", "┬", "┐"));
        sb.append("\n");

        sb.append("│");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], colWidths[i] - 2)).append(" │");
        }
        sb.append("\n");

        sb.append(buildSeparator(colWidths, "├", "┼", "┤"));
        sb.append("\n");

        for (String[] row : rows) {
            sb.append("│");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length && row[i] != null) ? row[i] : "";
                sb.append(" ").append(padRight(cell, colWidths[i] - 2)).append(" │");
            }
            sb.append("\n");
        }

        sb.append(buildSeparator(colWidths, "└", "┴", "┘"));

        return sb.toString();
    }

    private static String buildSeparator(int[] widths, String left, String middle, String right) {
        StringBuilder sb = new StringBuilder(left);
        for (int i = 0; i < widths.length; i++) {
            for (int j = 0; j < widths[i]; j++) {
                sb.append("─");
            }
            if (i < widths.length - 1) {
                sb.append(middle);
            }
        }
        sb.append(right);
        return sb.toString();
    }

    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxLength = 0;
        for (String line : lines) {
            maxLength = Math.max(maxLength, line.length());
        }

        StringBuilder sb = new StringBuilder();

        sb.append("┌─");
        for (int i = 0; i < maxLength; i++) {
            sb.append("─");
        }
        sb.append("─┐\n");

        for (String line : lines) {
            sb.append("│ ").append(padRight(line, maxLength)).append(" │\n");
        }

        sb.append("└─");
        for (int i = 0; i < maxLength; i++) {
            sb.append("─");
        }
        sb.append("─┘");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        return ANSI_BOLD + ANSI_CYAN + "\n" + formatBox(text) + ANSI_RESET;
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;

        StringBuilder sb = new StringBuilder(text);
        for (int i = text.length(); i < length; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;

        StringBuilder sb = new StringBuilder();
        for (int i = text.length(); i < length; i++) {
            sb.append(" ");
        }
        sb.append(text);
        return sb.toString();
    }
}