package com.littlehorizons.erp.model;

/**
 * Academic period of the school year. The dbValue is the text saved in MySQL.
 */
public enum AcademicPeriod {
    I("I", "Período I"),
    II("II", "Período II"),
    III("III", "Período III"),
    IV("IV", "Período IV"),
    FINAL("Final", "Final");

    /** Periods in which a student can be enrolled in a subject (Final is only a grade). */
    public static final AcademicPeriod[] TERMS = {I, II, III, IV};

    private final String dbValue;
    private final String label;

    AcademicPeriod(String dbValue, String label) {
        this.dbValue = dbValue;
        this.label = label;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Converts the text saved in the database into the enum value.
     */
    public static AcademicPeriod fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (AcademicPeriod item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para AcademicPeriod: " + value);
    }
}
