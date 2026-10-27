package com.littlehorizons.erp.model;

/**
 * Loan of a resource to a teacher (table prestamos_profesor).
 */
public class TeacherLoan extends Loan {

    @Override
    public BorrowerType getBorrowerType() {
        return BorrowerType.TEACHER;
    }
}
