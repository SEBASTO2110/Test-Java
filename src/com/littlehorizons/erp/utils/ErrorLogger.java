package com.littlehorizons.erp.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Writes every error of the application in the error_log.txt file.
 */
public class ErrorLogger {
    private static final String LOG_FILE = "error_log.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ErrorLogger() {
    }

    public static void log(String context, Exception error) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("[" + LocalDateTime.now().format(DATE_FORMAT) + "] " + context);
            writer.println("    " + error.getClass().getSimpleName() + ": " + error.getMessage());
            for (StackTraceElement line : error.getStackTrace()) {
                writer.println("        at " + line);
            }
            writer.println("-".repeat(80));
        } catch (IOException e) {
            System.out.println("No se pudo escribir en " + LOG_FILE + ": " + e.getMessage());
        }
    }

    /**
     * Logs a database error and returns an exception with a message the user can understand.
     */
    public static RuntimeException databaseError(String action, SQLException e) {
        log("Error al " + action, e);
        String reason;
        switch (e.getErrorCode()) {
            case 1062:
                reason = "ya existe un registro con esos datos.";
                break;
            case 1451:
                reason = "hay otros registros que dependen de este.";
                break;
            case 1452:
                reason = "el registro relacionado no existe (revise los ID).";
                break;
            case 3819:
                reason = "un valor está fuera del rango permitido.";
                break;
            default:
                reason = "ocurrió un problema con la base de datos (ver error_log.txt).";
        }
        return new RuntimeException("Error al " + action + ": " + reason);
    }
}
