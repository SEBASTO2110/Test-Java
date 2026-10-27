package com.littlehorizons.erp.model;

/**
 * Type of school event. The dbValue is the text saved in MySQL.
 */
public enum EventType {
    ACADEMIC("Académico", "Académico"),
    CULTURAL("Cultural", "Cultural"),
    SPORTS("Deportivo", "Deportivo"),
    SOCIAL("Social", "Social"),
    SCIENTIFIC("Científico", "Científico");

    private final String dbValue;
    private final String label;

    EventType(String dbValue, String label) {
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
    public static EventType fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (EventType item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para EventType: " + value);
    }
}
