package com.littlehorizons.erp.model;

/**
 * Loan of a resource to a student (table prestamos_estudiante).
 */
public class StudentLoan extends Loan {

    @Override
    public BorrowerType getBorrowerType() {
        return BorrowerType.STUDENT;
    }
}
