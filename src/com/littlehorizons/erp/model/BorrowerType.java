package com.littlehorizons.erp.model;

/**
 * Who borrows a resource. Each type is stored in its own table
 * (prestamos_estudiante / prestamos_profesor), so it is not a database column.
 */
public enum BorrowerType {
    STUDENT("Estudiante"),
    TEACHER("Profesor");

    private final String label;

    BorrowerType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
