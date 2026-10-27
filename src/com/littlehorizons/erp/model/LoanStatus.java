package com.littlehorizons.erp.model;

/**
 * Status of a resource loan. The dbValue is the text saved in MySQL.
 */
public enum LoanStatus {
    ACTIVE("Activo", "Activo"),
    RETURNED("Devuelto", "Devuelto"),
    OVERDUE("Vencido", "Vencido");

    private final String dbValue;
    private final String label;

    LoanStatus(String dbValue, String label) {
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
    public static LoanStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (LoanStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para LoanStatus: " + value);
    }
}
