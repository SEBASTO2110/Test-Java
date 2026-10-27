package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ILoanDAO;
import com.littlehorizons.erp.dao.StudentLoanDaoImpl;
import com.littlehorizons.erp.dao.TeacherLoanDaoImpl;
import com.littlehorizons.erp.model.BorrowerType;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business rules for lending resources to students and teachers.
 */
public class LoanController {
    public static final int MAX_STUDENT_LOAN_DAYS = 30;
    public static final int MAX_TEACHER_LOAN_DAYS = 90;
    public static final int DEFAULT_LOAN_DAYS = 15;

    private ILoanDAO studentLoanDAO;
    private ILoanDAO teacherLoanDAO;
    private ResourceController resourceController;
    private StudentController studentController;
    private TeacherController teacherController;

    public LoanController() {
        studentLoanDAO = new StudentLoanDaoImpl();
        teacherLoanDAO = new TeacherLoanDaoImpl();
        resourceController = new ResourceController();
        studentController = new StudentController();
        teacherController = new TeacherController();
    }

    public static int maxLoanDays(BorrowerType type) {
        return type == BorrowerType.STUDENT ? MAX_STUDENT_LOAN_DAYS : MAX_TEACHER_LOAN_DAYS;
    }

    private ILoanDAO daoFor(BorrowerType type) {
        return type == BorrowerType.STUDENT ? studentLoanDAO : teacherLoanDAO;
    }

    /**
     * Lends a resource to a student or a teacher. Only "Disponible" resources can be lent;
     * the loan and the new status of the resource are saved in one transaction.
     */
    public Loan lendResource(BorrowerType type, int borrowerId, int resourceId, LocalDate loanDate,
                             LocalDate expectedReturnDate, String notes) {
        String borrowerName;
        Loan loan;
        if (type == BorrowerType.STUDENT) {
            borrowerName = studentController.findActiveStudent(borrowerId).getFullName();
            loan = new StudentLoan();
        } else {
            borrowerName = teacherController.findActiveTeacher(borrowerId).getFullName();
            loan = new TeacherLoan();
        }
        Resource resource = resourceController.findResource(resourceId);
        if (!resource.isAvailable()) {
            throw new RuntimeException("El recurso '" + resource.getName() + "' no está disponible (estado: "
                    + resource.getStatus().getLabel() + ").");
        }
        validateDates(type, loanDate, expectedReturnDate);

        loan.setResourceId(resourceId);
        loan.setBorrowerId(borrowerId);
        loan.setLoanDate(loanDate);
        loan.setExpectedReturnDate(expectedReturnDate);
        loan.setNotes(notes);
        if (!daoFor(type).addLoan(loan)) {
            throw new RuntimeException("El recurso acaba de ser prestado; ya no está disponible.");
        }
        loan.setBorrowerName(borrowerName);
        loan.setResourceCode(resource.getCode());
        loan.setResourceName(resource.getName());
        return loan;
    }

    private void validateDates(BorrowerType type, LocalDate loanDate, LocalDate expectedReturnDate) {
        if (loanDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha del préstamo no puede ser futura.");
        }
        if (!expectedReturnDate.isAfter(loanDate)) {
            throw new RuntimeException("La fecha de devolución debe ser posterior a la fecha del préstamo.");
        }
        if (ChronoUnit.DAYS.between(loanDate, expectedReturnDate) > maxLoanDays(type)) {
            throw new RuntimeException("El préstamo a un " + type.getLabel().toLowerCase() + " no puede superar "
                    + maxLoanDays(type) + " días.");
        }
    }

    /**
     * Registers the return: real return date, loan "Devuelto" and resource "Disponible".
     */
    public Loan returnResource(BorrowerType type, int loanId, LocalDate returnDate) {
        Loan loan = findLoan(type, loanId);
        if (!loan.isOpen()) {
            throw new RuntimeException("El préstamo ya fue devuelto el " + loan.getActualReturnDate() + ".");
        }
        if (returnDate.isBefore(loan.getLoanDate()) || returnDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de devolución debe estar entre " + loan.getLoanDate() + " y hoy.");
        }
        if (!daoFor(type).registerReturn(loan, returnDate)) {
            throw new RuntimeException("No se pudo registrar la devolución; el préstamo ya estaba cerrado.");
        }
        loan.setActualReturnDate(returnDate);
        return loan;
    }

    public Loan findLoan(BorrowerType type, int loanId) {
        Loan loan = daoFor(type).findLoanById(loanId);
        if (loan == null) {
            throw new RuntimeException("No existe un préstamo a " + type.getLabel().toLowerCase() + " con ID "
                    + loanId + ".");
        }
        return loan;
    }

    /**
     * Changes the "Activo" loans past their due date to "Vencido". Returns how many changed.
     */
    public int refreshOverdueStatus(LocalDate today) {
        return studentLoanDAO.markOverdueLoans(today) + teacherLoanDAO.markOverdueLoans(today);
    }

    /**
     * Open loans of both types, the closest due date first.
     */
    public List<Loan> openLoans() {
        refreshOverdueStatus(LocalDate.now());
        List<Loan> loans = new ArrayList<>(studentLoanDAO.findOpenLoans());
        loans.addAll(teacherLoanDAO.findOpenLoans());
        loans.sort(Comparator.comparing(Loan::getExpectedReturnDate));
        return loans;
    }

    public List<Loan> openLoans(BorrowerType type) {
        return openLoans().stream()
                .filter(loan -> loan.getBorrowerType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Loans not returned on the expected date, the most delayed first.
     */
    public List<Loan> overdueLoans(LocalDate today) {
        return openLoans().stream()
                .filter(loan -> loan.isOverdueOn(today))
                .sorted(Comparator.comparingLong((Loan loan) -> loan.getDaysOverdue(today)).reversed())
                .collect(Collectors.toList());
    }

    public List<Loan> borrowerHistory(BorrowerType type, int borrowerId) {
        if (type == BorrowerType.STUDENT) {
            studentController.findStudent(borrowerId);
        } else {
            teacherController.findTeacher(borrowerId);
        }
        return daoFor(type).findLoansByBorrower(borrowerId);
    }

    public List<Loan> resourceHistory(int resourceId) {
        resourceController.findResource(resourceId);
        List<Loan> loans = new ArrayList<>(studentLoanDAO.findLoansByResource(resourceId));
        loans.addAll(teacherLoanDAO.findLoansByResource(resourceId));
        loans.sort(Comparator.comparing(Loan::getLoanDate).reversed());
        return loans;
    }
}
