package com.littlehorizons.erp.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

/**
 * Text formats used when showing data on screen.
 */
public class Formats {

    private Formats() {
    }

    public static String text(Object value) {
        return value == null ? "-" : value.toString();
    }

    public static String date(LocalDate date) {
        return date == null ? "-" : date.toString();
    }

    public static String time(LocalTime time) {
        return time == null ? "-" : time.toString();
    }

    public static String score(BigDecimal score) {
        return score == null ? "-" : String.format(Locale.US, "%.1f", score);
    }

    public static String average(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    /**
     * Colombian pesos, e.g. 350000 -> "$ 350.000" (cents only when they are not zero).
     */
    public static String money(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        boolean hasCents = amount.setScale(2, RoundingMode.HALF_UP).remainder(BigDecimal.ONE).signum() != 0;
        return new DecimalFormat(hasCents ? "$ #,##0.00" : "$ #,##0", symbols).format(amount);
    }

    public static String percentage(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    public static String yesNo(boolean value) {
        return value ? "Sí" : "No";
    }
}
