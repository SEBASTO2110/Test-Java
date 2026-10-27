package com.littlehorizons.erp.model;

/**
 * Status of a club membership. The dbValue is the text saved in MySQL.
 */
public enum MembershipStatus {
    ACTIVE("Activa", "Activa"),
    WITHDRAWN("Retirada", "Retirada");

    private final String dbValue;
    private final String label;

    MembershipStatus(String dbValue, String label) {
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
    public static MembershipStatus fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (MembershipStatus item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para MembershipStatus: " + value);
    }
}
