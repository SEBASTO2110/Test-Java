package com.littlehorizons.erp.model;

/**
 * Payment method. The dbValue is the text saved in MySQL.
 */
public enum PaymentMethod {
    CASH("Efectivo", "Efectivo"),
    TRANSFER("Transferencia", "Transferencia"),
    DEBIT_CARD("Tarjeta Débito", "Tarjeta Débito"),
    CREDIT_CARD("Tarjeta Crédito", "Tarjeta Crédito"),
    CHECK("Cheque", "Cheque");

    private final String dbValue;
    private final String label;

    PaymentMethod(String dbValue, String label) {
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
    public static PaymentMethod fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (PaymentMethod item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para PaymentMethod: " + value);
    }
}
