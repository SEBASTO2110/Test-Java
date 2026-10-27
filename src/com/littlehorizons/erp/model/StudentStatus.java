package com.littlehorizons.erp.model;

/**
 * Status of a student. The dbValue is the text saved in MySQL.
 */
public enum StudentStatus {
    ACTIVE("Activo", "Activo"),
    GRADUATED("Graduado", "Graduado"),
    WITHDRAWN("Retirado", "Retirado"),
    SUSPENDED("Suspendido", "Suspendido");

    private final String dbValue;
    private final String label;

    StudentStatus(String dbValue, String label) {
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
    public static StudentStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (StudentStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para StudentStatus: " + value);
    }
}
