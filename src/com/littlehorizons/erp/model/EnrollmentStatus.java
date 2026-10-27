package com.littlehorizons.erp.model;

/**
 * Status of a grade enrollment (matrícula) or a subject enrollment (inscripción). The dbValue is the text saved in MySQL.
 */
public enum EnrollmentStatus {
    ACTIVE("Activa", "Activa"),
    COMPLETED("Completada", "Completada"),
    WITHDRAWN("Retirada", "Retirada");

    private final String dbValue;
    private final String label;

    EnrollmentStatus(String dbValue, String label) {
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
    public static EnrollmentStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (EnrollmentStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para EnrollmentStatus: " + value);
    }
}
