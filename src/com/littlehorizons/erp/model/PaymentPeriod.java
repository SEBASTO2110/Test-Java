package com.littlehorizons.erp.model;

/**
 * Academic period a payment applies to. The dbValue is the text saved in MySQL.
 */
public enum PaymentPeriod {
    FIRST("I", "I"),
    SECOND("II", "II"),
    THIRD("III", "III"),
    FOURTH("IV", "IV"),
    ANNUAL("Anual", "Anual");

    private final String dbValue;
    private final String label;

    PaymentPeriod(String dbValue, String label) {
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
    public static PaymentPeriod fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentPeriod item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para PaymentPeriod: " + value);
    }
}
