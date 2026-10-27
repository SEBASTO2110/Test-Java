package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.LoanController;
import com.littlehorizons.erp.model.BorrowerType;
import com.littlehorizons.erp.model.Loan;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loans and returns of resources to students and teachers.
 */
public class LoanView {

    private LoanController loanController;
    private ResourcePickers resourcePickers;
    private AcademicPickers pickers;

    public LoanView() {
        loanController = new LoanController();
        resourcePickers = new ResourcePickers();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Préstamos y devoluciones");
            System.out.println("""
                1. Prestar recurso a un estudiante
                2. Prestar recurso a un profesor
                3. Registrar devolución
                4. Ver préstamos activos
                5. Reporte de préstamos vencidos
                6. Historial de préstamos de un estudiante
                7. Historial de préstamos de un profesor
                8. Historial de préstamos de un recurso
                0. Volver""");
            option = InputReader.readMenuOption(8);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Prestar recurso a un estudiante");
                        lend(BorrowerType.STUDENT);
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Prestar recurso a un profesor");
                        lend(BorrowerType.TEACHER);
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Registrar devolución");
                        registerReturn();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Ver préstamos activos");
                        printLoans(loanController.openLoans(), LocalDate.now());
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Reporte de préstamos vencidos");
                        showOverdueReport();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Historial de préstamos de un estudiante");
                        showStudentHistory();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Historial de préstamos de un profesor");
                        showTeacherHistory();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Historial de préstamos de un recurso");
                        showResourceHistory();
                        break;
                    case 0:
                        break;
                    default:
                        ConsoleUtils.printError("Opción inválida.");
                }
            } catch (RuntimeException e) {
                ConsoleUtils.printException(e);
            }
            if (option != 0) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private void lend(BorrowerType type) {
        int borrowerId = type == BorrowerType.STUDENT ? pickers.pickStudent().getId() : pickers.pickTeacher().getId();
        Resource resource = resourcePickers.pickResource(true);
        LocalDate loanDate = InputReader.readDateOrToday("Fecha del préstamo");
        int maxDays = LoanController.maxLoanDays(type);
        LocalDate suggested = loanDate.plusDays(Math.min(LoanController.DEFAULT_LOAN_DAYS, maxDays));
        LocalDate expected = InputReader.confirm("¿Devolución programada para el " + suggested + "?")
                ? suggested
                : InputReader.readDate("Fecha de devolución programada (máx. " + maxDays + " días)",
                loanDate.plusDays(1), loanDate.plusDays(maxDays));
        String notes = InputReader.readOptionalText("Observaciones", 500);
        Loan loan = loanController.lendResource(type, borrowerId, resource.getId(), loanDate, expected, notes);
        ConsoleUtils.printSuccess("Préstamo #" + loan.getId() + ": " + loan.getResourceCode() + " - "
                + loan.getResourceName() + " a " + loan.getBorrowerName() + ". Devolver antes del "
                + loan.getExpectedReturnDate() + ".");
    }

    private void registerReturn() {
        BorrowerType type = InputReader.readFromList("¿Quién devuelve el recurso?", List.of(BorrowerType.values()),
                BorrowerType::getLabel);
        List<Loan> openLoans = loanController.openLoans(type);
        printLoans(openLoans, LocalDate.now());
        if (openLoans.isEmpty()) {
            return;
        }
        int loanId = InputReader.readId("ID del préstamo");
        LocalDate returnDate = InputReader.readDateOrToday("Fecha real de devolución");
        Loan loan = loanController.returnResource(type, loanId, returnDate);
        ConsoleUtils.printSuccess("Devolución registrada. " + loan.getResourceName() + " vuelve a estar Disponible.");
        if (loan.getExpectedReturnDate().isBefore(returnDate)) {
            ConsoleUtils.printWarning("Se devolvió con " + ChronoUnit.DAYS.between(loan.getExpectedReturnDate(), returnDate)
                    + " día(s) de retraso.");
        }
    }

    private void printLoans(List<Loan> loans, LocalDate today) {
        ConsoleUtils.printTable(new String[]{"ID", "Tipo", "Prestatario", "Recurso", "Préstamo", "Devolver", "Devuelto",
                        "Estado", "Días retraso"},
                loans.stream()
                        .map(loan -> new String[]{String.valueOf(loan.getId()), loan.getBorrowerType().getLabel(),
                                loan.getBorrowerName(), loan.getResourceCode() + " " + loan.getResourceName(),
                                Formats.date(loan.getLoanDate()), Formats.date(loan.getExpectedReturnDate()),
                                Formats.date(loan.getActualReturnDate()), loan.getStatus().getLabel(),
                                String.valueOf(loan.getDaysOverdue(today))})
                        .collect(Collectors.toList()));
    }

    /**
     * Also available from the reports menu.
     */
    public void showOverdueReport() {
        LocalDate today = LocalDate.now();
        List<Loan> overdue = loanController.overdueLoans(today);
        printLoans(overdue, today);
        long students = overdue.stream().filter(loan -> loan.getBorrowerType() == BorrowerType.STUDENT).count();
        double averageDelay = overdue.stream().mapToLong(loan -> loan.getDaysOverdue(today)).average().orElse(0);
        ConsoleUtils.printInfo("Vencidos de estudiantes: " + students + " | de profesores: " + (overdue.size() - students)
                + " | Retraso promedio: " + Formats.average(averageDelay) + " días");
    }

    private void showStudentHistory() {
        int studentId = pickers.pickStudent().getId();
        printLoans(loanController.borrowerHistory(BorrowerType.STUDENT, studentId), LocalDate.now());
    }

    private void showTeacherHistory() {
        int teacherId = pickers.pickTeacher().getId();
        printLoans(loanController.borrowerHistory(BorrowerType.TEACHER, teacherId), LocalDate.now());
    }

    private void showResourceHistory() {
        int resourceId = resourcePickers.pickResource(false).getId();
        printLoans(loanController.resourceHistory(resourceId), LocalDate.now());
    }
}
