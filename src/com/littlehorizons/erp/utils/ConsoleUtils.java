package com.littlehorizons.erp.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Shared console output helpers so every screen of the system looks the same.
 */
public class ConsoleUtils {

    private static final int BOX_WIDTH = 50;
    private static final int MAX_CELL_WIDTH = 40;

    /**
     * ANSI colors only work in a real terminal. IDE output windows (NetBeans, IntelliJ) do not
     * have a System.console(), so colors are disabled there. Can be forced with -Derp.colors=true.
     */
    private static final boolean USE_COLORS =
            Boolean.parseBoolean(System.getProperty("erp.colors", String.valueOf(System.console() != null)));

    private static final String RESET = color("\u001B[0m");
    private static final String GREEN = color("\u001B[32m");
    private static final String RED = color("\u001B[31m");
    private static final String YELLOW = color("\u001B[33m");
    private static final String CYAN = color("\u001B[36m");

    private ConsoleUtils() {
    }

    private static String color(String ansiCode) {
        return USE_COLORS ? ansiCode : "";
    }

    public static void printBanner(String title, String subtitle) {
        String border = "═".repeat(BOX_WIDTH);
        System.out.println();
        System.out.println(CYAN + "╔" + border + "╗");
        System.out.println("║" + padRight("   " + title, BOX_WIDTH) + "║");
        System.out.println("║" + padRight("   " + subtitle, BOX_WIDTH) + "║");
        System.out.println("╚" + border + "╝" + RESET);
    }

    public static void printHeader(String title) {
        System.out.println();
        System.out.println(CYAN + "── " + title + " " + "─".repeat(Math.max(3, BOX_WIDTH - title.length() - 4)) + RESET);
    }

    /**
     * Header shown before an option of a menu, with the reminder of how to cancel.
     */
    public static void printActionHeader(String title) {
        printHeader(title);
        printInfo("(Escriba " + InputReader.CANCEL_TOKEN + " en cualquier campo para cancelar)");
    }

    /**
     * Shows the message of an error caught in a view. Unexpected errors (without message)
     * are written in error_log.txt.
     */
    public static void printException(RuntimeException e) {
        if (InputReader.CANCEL_MESSAGE.equals(e.getMessage())) {
            printWarning("Operación cancelada. Regresando al menú.");
        } else if (e.getMessage() == null || e.getMessage().isBlank()) {
            ErrorLogger.log("Unexpected error", e);
            printError("Ocurrió un error inesperado. El detalle quedó en error_log.txt.");
        } else {
            printError(e.getMessage());
        }
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[OK] " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + "[ERROR] " + message + RESET);
    }

    public static void printWarning(String message) {
        System.out.println(YELLOW + "[!] " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println("  " + message);
    }

    /**
     * Prints rows as an aligned text table. Long values are truncated.
     */
    public static void printTable(String[] headers, List<String[]> rows) {
        if (rows.isEmpty()) {
            printWarning("No se encontraron registros.");
            return;
        }
        int[] widths = IntStream.range(0, headers.length)
                .map(column -> Math.min(MAX_CELL_WIDTH, rows.stream()
                        .mapToInt(row -> safe(row[column]).length())
                        .reduce(headers[column].length(), Math::max)))
                .toArray();

        String separator = Arrays.stream(widths)
                .mapToObj(width -> "-".repeat(width + 2))
                .collect(Collectors.joining("+", "+", "+"));

        System.out.println(separator);
        System.out.println(formatRow(headers, widths));
        System.out.println(separator);
        rows.forEach(row -> System.out.println(formatRow(row, widths)));
        System.out.println(separator);
        System.out.println("  Total: " + rows.size() + " registro(s)");
    }

    private static String formatRow(String[] values, int[] widths) {
        return IntStream.range(0, widths.length)
                .mapToObj(column -> " " + padRight(truncate(safe(values[column]), widths[column]), widths[column]) + " ")
                .collect(Collectors.joining("|", "|", "|"));
    }

    private static String truncate(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width - 3) + "...";
    }

    private static String padRight(String value, int width) {
        return value.length() >= width ? value : value + " ".repeat(width - value.length());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public static void pause() {
        InputReader.waitForEnter();
    }
}
