package com.littlehorizons.erp.model;

/**
 * Position obtained in an event with competition. The dbValue is the text saved in MySQL.
 */
public enum CompetitionPosition {
    FIRST_PLACE("1er Lugar", "1er Lugar"),
    SECOND_PLACE("2do Lugar", "2do Lugar"),
    THIRD_PLACE("3er Lugar", "3er Lugar"),
    PARTICIPANT("Participante", "Participante");

    private final String dbValue;
    private final String label;

    CompetitionPosition(String dbValue, String label) {
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
    public static CompetitionPosition fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (CompetitionPosition item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para CompetitionPosition: " + value);
    }
}
