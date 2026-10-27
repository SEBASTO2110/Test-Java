package com.littlehorizons.erp.model;

/**
 * Gender of a student. The dbValue is the text saved in MySQL.
 */
public enum Gender {
    MALE("M", "Masculino"),
    FEMALE("F", "Femenino"),
    OTHER("Otro", "Otro");

    private final String dbValue;
    private final String label;

    Gender(String dbValue, String label) {
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
    public static Gender fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (Gender item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para Gender: " + value);
    }
}
