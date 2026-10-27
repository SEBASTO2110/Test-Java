package com.littlehorizons.erp.model;

/**
 * Day of the week of a class. The dbValue is the text saved in MySQL.
 */
public enum WeekDay {
    MONDAY("Lunes", "Lunes"),
    TUESDAY("Martes", "Martes"),
    WEDNESDAY("Miércoles", "Miércoles"),
    THURSDAY("Jueves", "Jueves"),
    FRIDAY("Viernes", "Viernes"),
    SATURDAY("Sábado", "Sábado");

    private final String dbValue;
    private final String label;

    WeekDay(String dbValue, String label) {
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
    public static WeekDay fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (WeekDay item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para WeekDay: " + value);
    }
}
