package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentView {

    private static final String[] TABLE_HEADERS = {"ID matrícula", "Estudiante", "Grado", "Año", "Estado", "Fecha"};

    private EnrollmentController enrollmentController;
    private AcademicPickers pickers;

    public EnrollmentView() {
        enrollmentController = new EnrollmentController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Matrículas por grado");
            System.out.println("""
                1. Matricular estudiante en un grado
                2. Listar matriculados por grado y año
                3. Historial de matrículas de un estudiante
                4. Finalizar año escolar de una matrícula
                5. Retirar matrícula
                0. Volver""");
            option = InputReader.readMenuOption(5);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Matricular estudiante en un grado");
                        enrollStudent();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar matriculados por grado y año");
                        listByGrade();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Historial de matrículas de un estudiante");
                        showHistory();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Finalizar año escolar de una matrícula");
                        completeEnrollment();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Retirar matrícula");
                        withdrawEnrollment();
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

    private void enrollStudent() {
        Student student = pickers.pickStudent();
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        LocalDate date = InputReader.readDateOrToday("Fecha de matrícula");
        String notes = InputReader.readOptionalText("Observaciones", 500);
        Enrollment enrollment = enrollmentController.enrollStudent(student.getId(), grade.getId(), year, date, notes);
        int id = enrollment.getId();
        ConsoleUtils.printSuccess("Matrícula " + id + " creada: " + student.getFullName() + " en " + grade.getName()
                + " (" + year + ").");
        ConsoleUtils.printInfo("Siguiente paso: Gestión Académica > Inscripción en materias.");
    }

    private void listByGrade() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        printEnrollments(enrollmentController.listByGradeAndYear(grade.getId(), year));
    }

    private void showHistory() {
        Student student = pickers.pickStudent();
        printEnrollments(enrollmentController.enrollmentHistory(student.getId()));
    }

    private void completeEnrollment() {
        Enrollment enrollment = enrollmentController.findEnrollment(InputReader.readId("ID de la matrícula"));
        ConsoleUtils.printInfo("» " + enrollment.getStudentName() + " - " + enrollment.getGradeName() + " "
                + enrollment.getAcademicYear());
        if (InputReader.confirm("¿Marcar la matrícula como completada?")) {
            boolean graduated = enrollmentController.completeEnrollment(enrollment.getId());
            ConsoleUtils.printSuccess("Matrícula completada.");
            if (graduated) {
                ConsoleUtils.printSuccess("¡El estudiante terminó grado Once y quedó en estado Graduado!");
            }
        }
    }

    private void withdrawEnrollment() {
        Enrollment enrollment = enrollmentController.findEnrollment(InputReader.readId("ID de la matrícula"));
        ConsoleUtils.printInfo("» " + enrollment.getStudentName() + " - " + enrollment.getGradeName() + " "
                + enrollment.getAcademicYear());
        LocalDate date = InputReader.readDateOrToday("Fecha de retiro");
        String reason = InputReader.readText("Motivo del retiro", 500);
        boolean withdrawStudent = InputReader.confirm("¿Cambiar también el estado del estudiante a Retirado?");
        enrollmentController.withdrawEnrollment(enrollment.getId(), date, reason, withdrawStudent);
        ConsoleUtils.printSuccess("Matrícula retirada.");
    }

    private void printEnrollments(List<Enrollment> enrollments) {
        ConsoleUtils.printTable(TABLE_HEADERS, enrollments.stream()
                .map(enrollment -> new String[]{String.valueOf(enrollment.getId()), enrollment.getStudentName(),
                        enrollment.getGradeName(), String.valueOf(enrollment.getAcademicYear()),
                        enrollment.getStatus().getLabel(), Formats.date(enrollment.getEnrollmentDate())})
                .collect(Collectors.toList()));
    }
}
