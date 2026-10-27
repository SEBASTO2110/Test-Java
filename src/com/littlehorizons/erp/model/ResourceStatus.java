package com.littlehorizons.erp.model;

/**
 * Status of a school resource. The dbValue is the text saved in MySQL.
 */
public enum ResourceStatus {
    AVAILABLE("Disponible", "Disponible"),
    LOANED("Prestado", "Prestado"),
    MAINTENANCE("En Mantenimiento", "En Mantenimiento"),
    RETIRED("Dado de Baja", "Dado de Baja");

    private final String dbValue;
    private final String label;

    ResourceStatus(String dbValue, String label) {
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
    public static ResourceStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (ResourceStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para ResourceStatus: " + value);
    }
}
