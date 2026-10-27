package com.littlehorizons.erp.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Reads and validates what the user types. If a value is invalid it shows a message and asks again.
 * Typing CANCEL_TOKEN in any field cancels the current operation.
 */
public class InputReader {
    public static final String CANCEL_TOKEN = "*";
    public static final String CANCEL_MESSAGE = "Operación cancelada.";

    private static final String EMAIL_REGEX = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    private static final String PHONE_REGEX = "^\\+?\\d{7,15}$";
    private static final String DOCUMENT_REGEX = "^[A-Za-z0-9]{5,20}$";

    private InputReader() {
    }

    private static String readLine(String prompt) {
        Scanner sc = ScannerSingleton.getInstance();
        System.out.print(prompt);
        if (!sc.hasNextLine()) {
            throw new RuntimeException(CANCEL_MESSAGE);
        }
        String value = sc.nextLine().trim();
        if (value.equals(CANCEL_TOKEN)) {
            throw new RuntimeException(CANCEL_MESSAGE);
        }
        return value;
    }

    // ---------------------------------------------------------------- text

    public static String readText(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt + ": ");
            if (!value.isEmpty() && value.length() <= maxLength) {
                return value;
            }
            ConsoleUtils.printError("Este campo es obligatorio y admite máximo " + maxLength + " caracteres.");
        }
    }

    /**
     * Returns null when the user leaves the field empty.
     */
    public static String readOptionalText(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt + " (opcional): ");
            if (value.isEmpty()) {
                return null;
            }
            if (value.length() <= maxLength) {
                return value;
            }
            ConsoleUtils.printError("Máximo " + maxLength + " caracteres.");
        }
    }

    /**
     * Used when updating: if the user presses Enter the current value is kept.
     */
    public static String readTextOrKeep(String prompt, String current, int maxLength) {
        while (true) {
            String value = readLine(prompt + " [" + (current == null ? "" : current) + "]: ");
            if (value.isEmpty()) {
                return current;
            }
            if (value.length() <= maxLength) {
                return value;
            }
            ConsoleUtils.printError("Máximo " + maxLength + " caracteres.");
        }
    }

    public static String readDocumentNumber(String prompt) {
        while (true) {
            String value = readLine(prompt + ": ");
            if (value.matches(DOCUMENT_REGEX)) {
                return value;
            }
            ConsoleUtils.printError("El documento debe tener entre 5 y 20 letras o números, sin espacios ni puntos.");
        }
    }

    public static String readEmail(String prompt, boolean required) {
        while (true) {
            String value = readLine(prompt + (required ? ": " : " (opcional): "));
            if (value.isEmpty() && !required) {
                return null;
            }
            if (value.matches(EMAIL_REGEX) && value.length() <= 100) {
                return value.toLowerCase();
            }
            ConsoleUtils.printError("Correo inválido. Ejemplo: nombre@correo.com");
        }
    }

    public static String readPhone(String prompt, boolean required) {
        while (true) {
            String value = readLine(prompt + (required ? ": " : " (opcional): ")).replace(" ", "");
            if (value.isEmpty() && !required) {
                return null;
            }
            if (value.matches(PHONE_REGEX)) {
                return value;
            }
            ConsoleUtils.printError("Teléfono inválido. Use solo números (7 a 15 dígitos).");
        }
    }

    // ---------------------------------------------------------------- numbers

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt + ": ");
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // not a number, the message is shown below
            }
            ConsoleUtils.printError("Ingrese un número entero entre " + min + " y " + max + ".");
        }
    }

    public static int readId(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads the option of a menu. If the user cancels, 0 (go back) is returned.
     */
    public static int readMenuOption(int maxOption) {
        try {
            return readInt("\nSeleccione una opción", 0, maxOption);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public static BigDecimal readDecimal(String prompt, BigDecimal min, BigDecimal max, int maxDecimals) {
        while (true) {
            BigDecimal number = parseDecimal(readLine(prompt + ": "), min, max, maxDecimals);
            if (number != null) {
                return number;
            }
            ConsoleUtils.printError("Ingrese un número entre " + min + " y " + max + " con máximo " + maxDecimals
                    + " decimal(es).");
        }
    }

    /**
     * Same as readDecimal, but returns null if the user leaves it empty (to skip a value).
     */
    public static BigDecimal readOptionalDecimal(String prompt, BigDecimal min, BigDecimal max, int maxDecimals) {
        while (true) {
            String value = readLine(prompt + " (Enter = omitir): ");
            if (value.isEmpty()) {
                return null;
            }
            BigDecimal number = parseDecimal(value, min, max, maxDecimals);
            if (number != null) {
                return number;
            }
            ConsoleUtils.printError("Ingrese un número entre " + min + " y " + max + " con máximo " + maxDecimals
                    + " decimal(es).");
        }
    }

    private static BigDecimal parseDecimal(String value, BigDecimal min, BigDecimal max, int maxDecimals) {
        try {
            BigDecimal number = new BigDecimal(value.replace(',', '.'));
            int decimals = Math.max(0, number.stripTrailingZeros().scale());
            if (number.compareTo(min) >= 0 && number.compareTo(max) <= 0 && decimals <= maxDecimals) {
                return number;
            }
        } catch (NumberFormatException e) {
            // invalid number
        }
        return null;
    }

    // ---------------------------------------------------------------- dates

    public static LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt + " (AAAA-MM-DD): ");
            try {
                return LocalDate.parse(value);
            } catch (RuntimeException e) {
                ConsoleUtils.printError("Fecha inválida. Use el formato AAAA-MM-DD, por ejemplo 2026-03-15.");
            }
        }
    }

    public static LocalDate readDate(String prompt, LocalDate min, LocalDate max) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (!date.isBefore(min) && !date.isAfter(max)) {
                return date;
            }
            ConsoleUtils.printError("Ingrese una fecha entre " + min + " y " + max + ".");
        }
    }

    public static LocalDate readDateOrToday(String prompt) {
        while (true) {
            String value = readLine(prompt + " (AAAA-MM-DD, Enter = hoy): ");
            if (value.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(value);
            } catch (RuntimeException e) {
                ConsoleUtils.printError("Fecha inválida. Use el formato AAAA-MM-DD.");
            }
        }
    }

    public static LocalTime readTime(String prompt) {
        while (true) {
            String value = readLine(prompt + " (HH:MM, 24 horas): ");
            try {
                return LocalTime.parse(value);
            } catch (RuntimeException e) {
                ConsoleUtils.printError("Hora inválida. Ejemplo: 07:30 o 14:00.");
            }
        }
    }

    // ---------------------------------------------------------------- choices

    public static boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (S/N): ").toUpperCase();
            if (value.equals("S")) {
                return true;
            }
            if (value.equals("N")) {
                return false;
            }
            ConsoleUtils.printError("Responda S o N.");
        }
    }

    /**
     * Reads a short code from a list (for example P/A/T/E). Enter returns the default code.
     */
    public static String readCode(String prompt, List<String> validCodes, String defaultCode) {
        while (true) {
            String value = readLine(prompt + " " + validCodes + " (Enter = " + defaultCode + "): ").toUpperCase();
            if (value.isEmpty()) {
                return defaultCode;
            }
            if (validCodes.contains(value)) {
                return value;
            }
            ConsoleUtils.printError("Opción inválida. Use una de: " + String.join(", ", validCodes));
        }
    }

    /**
     * Shows a numbered list and returns the element chosen by the user.
     * The labeler lambda says how each element is shown.
     */
    public static <T> T readFromList(String prompt, List<T> options, Function<T, String> labeler) {
        System.out.println(prompt + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + labeler.apply(options.get(i)));
        }
        int option = readInt("Opción", 1, options.size());
        return options.get(option - 1);
    }

    /**
     * Same as readFromList but for an array, for example the values of an enum:
     * readOption("Género", Gender.values(), Gender::getLabel)
     */
    public static <T> T readOption(String prompt, T[] options, Function<T, String> labeler) {
        return readFromList(prompt, List.of(options), labeler);
    }

    public static void waitForEnter() {
        System.out.print("\nPresione Enter para continuar...");
        Scanner sc = ScannerSingleton.getInstance();
        if (sc.hasNextLine()) {
            sc.nextLine();
        }
    }
}
