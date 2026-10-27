package com.littlehorizons.erp.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Common data of a resource loan. {@link StudentLoan} and {@link TeacherLoan}
 * only differ in who the borrower is.
 */
public abstract class Loan {

    private int id;
    private int resourceId;
    private String resourceCode;
    private String resourceName;
    private int borrowerId;
    private String borrowerName;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private LoanStatus status = LoanStatus.ACTIVE;
    private String notes;

    public abstract BorrowerType getBorrowerType();

    /** A loan is open while the resource has not been returned (Activo or Vencido). */
    public boolean isOpen() {
        return status != LoanStatus.RETURNED;
    }

    public boolean isOverdueOn(LocalDate date) {
        return isOpen() && expectedReturnDate.isBefore(date);
    }

    public long getDaysOverdue(LocalDate date) {
        return isOverdueOn(date) ? ChronoUnit.DAYS.between(expectedReturnDate, date) : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(int borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getBorrowerName() {
        return borrowerName;
    }

    public void setBorrowerName(String borrowerName) {
        this.borrowerName = borrowerName;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
