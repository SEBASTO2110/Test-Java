package com.littlehorizons.erp.model;

/**
 * Type of identity document. The dbValue is the text saved in MySQL.
 */
public enum DocumentType {
    TI("TI", "Tarjeta de Identidad"),
    CC("CC", "Cédula de Ciudadanía"),
    CE("CE", "Cédula de Extranjería"),
    PASSPORT("Pasaporte", "Pasaporte");

    /** Adults (guardians and teachers) cannot use a TI. */
    public static final DocumentType[] ADULT_TYPES = {CC, CE, PASSPORT};

    private final String dbValue;
    private final String label;

    DocumentType(String dbValue, String label) {
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
    public static DocumentType fromDbValue(String value) {
        if (value == null) {
            return null;
        }
        for (DocumentType item : values()) {
            if (item.dbValue.equalsIgnoreCase(value)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Valor desconocido para DocumentType: " + value);
    }
}
