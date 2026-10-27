package com.littlehorizons.erp.utils;

import java.util.Scanner;

/**
 * Singleton so the whole application reads the keyboard with the same Scanner.
 */
public class ScannerSingleton {
    private static Scanner scanner = null;

    private ScannerSingleton() {
    }

    public static Scanner getInstance() {
        if (scanner == null) {
            scanner = new Scanner(System.in);
        }
        return scanner;
    }

    public static void closeScanner() {
        if (scanner != null) {
            scanner.close();
            scanner = null;
        }
    }
}
