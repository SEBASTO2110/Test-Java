package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Loan;

import java.time.LocalDate;
import java.util.List;

public interface ILoanDAO {
    /**
     * Saves the loan and changes the resource to "Prestado" in one transaction.
     * Returns false if the resource was no longer available.
     */
    boolean addLoan(Loan loan);

    /**
     * Saves the return date and changes the resource back to "Disponible" in one transaction.
     */
    boolean registerReturn(Loan loan, LocalDate returnDate);

    int markOverdueLoans(LocalDate today);

    Loan findLoanById(int id);

    List<Loan> findLoansByBorrower(int borrowerId);

    List<Loan> findLoansByResource(int resourceId);

    List<Loan> findOpenLoans();
}
