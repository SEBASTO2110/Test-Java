package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.SubjectController;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

public class SubjectView {

    private SubjectController subjectController;
    private AcademicPickers pickers;

    public SubjectView() {
        subjectController = new SubjectController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Materias");
            System.out.println("""
                1. Registrar materia
                2. Listar materias de un grado
                3. Listar todas las materias
                4. Actualizar materia
                5. Eliminar materia
                0. Volver""");
            option = InputReader.readMenuOption(5);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar materia");
                        registerSubject();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar materias de un grado");
                        listByGrade();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Listar todas las materias");
                        printSubjects(subjectController.listSubjects());
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Actualizar materia");
                        updateSubject();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Eliminar materia");
                        deleteSubject();
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

    private void registerSubject() {
        Subject subject = new Subject();
        readSubjectData(subject);
        subjectController.registerSubject(subject);
        int id = subject.getId();
        ConsoleUtils.printSuccess("Materia registrada con ID " + id + ".");
    }

    private void listByGrade() {
        printSubjects(subjectController.listByGrade(pickers.pickGrade().getId()));
    }

    private void updateSubject() {
        Subject subject = pickers.pickSubject();
        ConsoleUtils.printInfo("Ingrese los nuevos datos de la materia:");
        readSubjectData(subject);
        subject.setActive(InputReader.confirm("¿La materia está activa?"));
        subjectController.updateSubject(subject);
        ConsoleUtils.printSuccess("Materia actualizada.");
    }

    private void deleteSubject() {
        Subject subject = pickers.pickSubject();
        ConsoleUtils.printWarning("Se eliminarán también las notas, inscripciones y asistencias de esta materia.");
        if (InputReader.confirm("¿Seguro que desea eliminar " + subject.getName() + "?")) {
            subjectController.deleteSubject(subject.getId());
            ConsoleUtils.printSuccess("Materia eliminada.");
        }
    }

    private void readSubjectData(Subject subject) {
        subject.setCode(InputReader.readText("Código (ej: MAT-5)", 20));
        subject.setName(InputReader.readText("Nombre de la materia", 100));
        subject.setDescription(InputReader.readOptionalText("Descripción", 500));
        subject.setGradeId(pickers.pickGrade().getId());
        subject.setWeeklyHours(InputReader.readInt("Horas semanales", 1, SubjectController.MAX_WEEKLY_HOURS));
    }

    private void printSubjects(List<Subject> subjects) {
        ConsoleUtils.printTable(new String[]{"ID", "Código", "Materia", "Grado", "Horas/sem", "Activa"},
                subjects.stream()
                        .map(subject -> new String[]{String.valueOf(subject.getId()), subject.getCode(),
                                subject.getName(), subject.getGradeName(), String.valueOf(subject.getWeeklyHours()),
                                Formats.yesNo(subject.isActive())})
                        .collect(Collectors.toList()));
    }
}
