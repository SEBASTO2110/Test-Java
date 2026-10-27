package com.littlehorizons.erp.model;

/**
 * Attendance status of a student in a class. The dbValue is the text saved in MySQL.
 */
public enum AttendanceStatus {
    PRESENT("Presente", "P"),
    ABSENT("Ausente", "A"),
    LATE("Tardanza", "T"),
    MEDICAL_EXCUSE("Excusa Médica", "E");

    private final String dbValue;
    private final String shortCode;

    AttendanceStatus(String dbValue, String shortCode) {
        this.dbValue = dbValue;
        this.shortCode = shortCode;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getLabel() {
        return dbValue;
    }

    public String getShortCode() {
        return shortCode;
    }

    public static AttendanceStatus fromShortCode(String code) {
        for (AttendanceStatus item : values()) {
            if (item.shortCode.equalsIgnoreCase(code)) {
                return item;
            }
        }
        return PRESENT;
    }

    public static AttendanceStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (AttendanceStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para AttendanceStatus: " + value);
    }
}
