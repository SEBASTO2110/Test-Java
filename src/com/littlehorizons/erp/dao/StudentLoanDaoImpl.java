package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.StudentLoan;

/**
 * Loans of resources to students (table prestamos_estudiante).
 */
public class StudentLoanDaoImpl extends LoanDaoImpl {

    @Override
    protected String getLoanTable() {
        return "prestamos_estudiante";
    }

    @Override
    protected String getBorrowerTable() {
        return "estudiantes";
    }

    @Override
    protected String getBorrowerIdColumn() {
        return "id_estudiante";
    }

    @Override
    protected Loan createEmptyLoan() {
        return new StudentLoan();
    }
}
