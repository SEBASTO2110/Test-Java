package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.stream.Collectors;

public class SubjectEnrollmentView {

    private EnrollmentController enrollmentController;
    private AcademicPickers pickers;

    public SubjectEnrollmentView() {
        enrollmentController = new EnrollmentController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Inscripción en materias");
            System.out.println("""
                1. Inscribir estudiante en todas las materias de su grado
                2. Inscribir a todo un grado en sus materias
                3. Inscribir estudiante en una materia
                4. Ver materias inscritas de un estudiante
                5. Retirar estudiante de una materia
                0. Volver""");
            option = InputReader.readMenuOption(5);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Inscribir estudiante en todas las materias de su grado");
                        enrollStudentInGradeSubjects();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Inscribir a todo un grado en sus materias");
                        enrollWholeGrade();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Inscribir estudiante en una materia");
                        enrollInSubject();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Ver materias inscritas de un estudiante");
                        showStudentSubjects();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Retirar estudiante de una materia");
                        withdrawFromSubject();
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

    private void enrollStudentInGradeSubjects() {
        Student student = pickers.pickStudent();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readTerm();
        int created = enrollmentController.enrollInGradeSubjects(student.getId(), year, period);
        reportCreated(created);
    }

    private void enrollWholeGrade() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readTerm();
        int created = enrollmentController.enrollWholeGrade(grade.getId(), year, period);
        reportCreated(created);
    }

    private void enrollInSubject() {
        Student student = pickers.pickStudent();
        Subject subject = pickers.pickSubject();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readTerm();
        String section = InputReader.readText("Sección o grupo (ej: A)", 10).toUpperCase();
        SubjectEnrollment subjectEnrollment = enrollmentController.enrollInSubject(student.getId(), subject.getId(), year, period, section);
        int id = subjectEnrollment.getId();
        ConsoleUtils.printSuccess("Inscripción " + id + " creada.");
    }

    private void showStudentSubjects() {
        Student student = pickers.pickStudent();
        int year = pickers.readAcademicYear();
        ConsoleUtils.printTable(new String[]{"ID", "Materia", "Período", "Sección", "Estado", "Fecha"},
                enrollmentController.subjectEnrollmentsOf(student.getId(), year).stream()
                        .map(enrollment -> new String[]{String.valueOf(enrollment.getId()),
                                enrollment.getSubjectName(), enrollment.getPeriod().getLabel(),
                                Formats.text(enrollment.getSection()), enrollment.getStatus().getLabel(),
                                Formats.date(enrollment.getEnrollmentDate())})
                        .collect(Collectors.toList()));
    }

    private void withdrawFromSubject() {
        int id = InputReader.readId("ID de la inscripción (ver opción 4)");
        if (InputReader.confirm("¿Retirar al estudiante de esta materia?")) {
            enrollmentController.withdrawFromSubject(id);
            ConsoleUtils.printSuccess("Inscripción marcada como Retirada.");
        }
    }

    private void reportCreated(int created) {
        if (created == 0) {
            ConsoleUtils.printWarning("No se crearon inscripciones nuevas (ya existían).");
        } else {
            ConsoleUtils.printSuccess(created + " inscripción(es) creada(s).");
        }
    }
}
