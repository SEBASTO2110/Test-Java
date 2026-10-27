package com.littlehorizons.erp.utils;

/**
 * Legal and contact data of the school printed on invoices.
 * The DIAN resolution is simulated for academic purposes.
 */
public class SchoolInfo {

    public static final String LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String NIT = "900.456.789-1";
    /** NIT without dots and check digit, used to calculate the CUFE. */
    public static final String NIT_NUMBER = "900456789";
    public static final String ADDRESS = "Calle 127 # 15-40, Bogotá D.C., Colombia";
    public static final String PHONE = "(601) 745 3210";
    public static final String EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String TAX_REGIME = "Responsable de IVA - Servicios educativos excluidos (Art. 476 E.T.)";
    public static final String INVOICE_PREFIX = "LH";
    public static final String DIAN_RESOLUTION = "Resolución DIAN de facturación electrónica No. 18764081234567 "
            + "del 2026-01-15, prefijo LH, rango 1 al 100000, vigencia 24 meses (SIMULADA)";

    private SchoolInfo() {
    }
}
