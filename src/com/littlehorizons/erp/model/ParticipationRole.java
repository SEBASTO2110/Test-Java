package com.littlehorizons.erp.model;

/**
 * Role of a student in an event. The dbValue is the text saved in MySQL.
 */
public enum ParticipationRole {
    PARTICIPANT("Participante", "Participante"),
    COMPETITOR("Competidor", "Competidor"),
    ORGANIZER("Organizador", "Organizador"),
    ATTENDEE("Asistente", "Asistente");

    private final String dbValue;
    private final String label;

    ParticipationRole(String dbValue, String label) {
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
    public static ParticipationRole fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (ParticipationRole item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para ParticipationRole: " + value);
    }
}
