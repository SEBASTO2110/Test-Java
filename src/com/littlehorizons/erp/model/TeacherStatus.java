package com.littlehorizons.erp.model;

/**
 * Status of a teacher. The dbValue is the text saved in MySQL.
 */
public enum TeacherStatus {
    ACTIVE("Activo", "Activo"),
    INACTIVE("Inactivo", "Inactivo"),
    ON_LEAVE("Licencia", "Licencia");

    private final String dbValue;
    private final String label;

    TeacherStatus(String dbValue, String label) {
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
    public static TeacherStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (TeacherStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para TeacherStatus: " + value);
    }
}
