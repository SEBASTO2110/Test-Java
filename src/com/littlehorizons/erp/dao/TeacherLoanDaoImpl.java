package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.TeacherLoan;

/**
 * Loans of resources to teachers (table prestamos_profesor).
 */
public class TeacherLoanDaoImpl extends LoanDaoImpl {

    @Override
    protected String getLoanTable() {
        return "prestamos_profesor";
    }

    @Override
    protected String getBorrowerTable() {
        return "profesores";
    }

    @Override
    protected String getBorrowerIdColumn() {
        return "id_profesor";
    }

    @Override
    protected Loan createEmptyLoan() {
        return new TeacherLoan();
    }
}
