package com.littlehorizons.erp.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;

/**
 * Generates a simulated CUFE (Código Único de Factura Electrónica).
 * The real DIAN CUFE is a SHA-384 over several invoice fields; here it is the
 * SHA-384 of invoice number + NIT + issue date, as the project requires.
 */
public class CufeGenerator {

    private static final String ALGORITHM = "SHA-384";

    private CufeGenerator() {
    }

    /** Returns a 96-character lowercase hexadecimal hash. */
    public static String generate(String invoiceNumber, String nit, LocalDate issueDate) {
        String source = invoiceNumber + nit + issueDate.toString();
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-384 is mandatory in every Java runtime, so this should never happen
            ErrorLogger.log("CufeGenerator.generate", e);
            throw new IllegalStateException("SHA-384 not available", e);
        }
    }
}
