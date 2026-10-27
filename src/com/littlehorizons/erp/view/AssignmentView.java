package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.ClassSchedule;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeachingAssignment;
import com.littlehorizons.erp.model.WeekDay;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

public class AssignmentView {

    private static final String[] ASSIGNMENT_HEADERS = {"ID", "Materia", "Grado", "Sección", "Año", "Profesor"};

    private TeacherController teacherController;
    private AcademicPickers pickers;

    public AssignmentView() {
        teacherController = new TeacherController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Carga académica y horarios");
            System.out.println("""
                1. Asignar materia a un profesor
                2. Ver materias asignadas a un profesor
                3. Ver todas las asignaciones de un año
                4. Quitar una asignación
                5. Agregar bloque de horario a una asignación
                6. Ver horario semanal de un profesor
                7. Eliminar un bloque de horario
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Asignar materia a un profesor");
                        assignSubject();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Ver materias asignadas a un profesor");
                        showTeacherAssignments();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Ver todas las asignaciones de un año");
                        showYearAssignments();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Quitar una asignación");
                        removeAssignment();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Agregar bloque de horario a una asignación");
                        addSchedule();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Ver horario semanal de un profesor");
                        showTeacherSchedule();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Eliminar un bloque de horario");
                        removeSchedule();
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

    private void assignSubject() {
        Teacher teacher = pickers.pickTeacher();
        Subject subject = pickers.pickSubject();
        int year = pickers.readAcademicYear();
        String section = InputReader.readText("Sección o grupo (ej: A)", 10);
        TeachingAssignment assignment = teacherController.assignSubject(teacher.getId(), subject.getId(), year, section);
        int id = assignment.getId();
        ConsoleUtils.printSuccess("Asignación " + id + " creada: " + subject.getName() + " para "
                + teacher.getFullName() + ".");
        if (InputReader.confirm("¿Desea agregar el horario ahora?")) {
            addScheduleTo(id);
        }
    }

    private void showTeacherAssignments() {
        Teacher teacher = pickers.pickTeacher();
        int year = pickers.readAcademicYear();
        printAssignments(teacherController.assignmentsOf(teacher.getId(), year));
    }

    private void showYearAssignments() {
        printAssignments(teacherController.assignmentsOfYear(pickers.readAcademicYear()));
    }

    private void removeAssignment() {
        TeachingAssignment assignment = teacherController.findAssignment(InputReader.readId("ID de la asignación"));
        ConsoleUtils.printInfo("» " + assignment.getDescription());
        if (InputReader.confirm("¿Quitar esta asignación y sus horarios?")) {
            teacherController.removeAssignment(assignment.getId());
            ConsoleUtils.printSuccess("Asignación eliminada.");
        }
    }

    private void addSchedule() {
        addScheduleTo(InputReader.readId("ID de la asignación"));
    }

    private void addScheduleTo(int assignmentId) {
        TeachingAssignment assignment = teacherController.findAssignment(assignmentId);
        ConsoleUtils.printInfo("» " + assignment.getDescription());
        do {
            ClassSchedule schedule = new ClassSchedule();
            schedule.setAssignmentId(assignmentId);
            schedule.setDay(InputReader.readOption("Día", WeekDay.values(), WeekDay::getLabel));
            schedule.setStartTime(InputReader.readTime("Hora de inicio"));
            schedule.setEndTime(InputReader.readTime("Hora de fin"));
            teacherController.addSchedule(schedule);
            ConsoleUtils.printSuccess("Bloque agregado: " + schedule.getDay().getLabel() + " "
                    + schedule.getStartTime() + " - " + schedule.getEndTime());
        } while (InputReader.confirm("¿Agregar otro bloque a esta asignación?"));
    }

    private void showTeacherSchedule() {
        Teacher teacher = pickers.pickTeacher();
        int year = pickers.readAcademicYear();
        List<ClassSchedule> schedule = teacherController.scheduleOf(teacher.getId(), year);
        ConsoleUtils.printTable(new String[]{"ID", "Día", "Inicio", "Fin", "Clase"}, schedule.stream()
                .map(block -> new String[]{String.valueOf(block.getId()), block.getDay().getLabel(),
                        Formats.time(block.getStartTime()), Formats.time(block.getEndTime()),
                        block.getAssignmentDescription()})
                .collect(Collectors.toList()));
    }

    private void removeSchedule() {
        int id = InputReader.readId("ID del bloque de horario (ver opción 6)");
        if (InputReader.confirm("¿Eliminar este bloque de horario?")) {
            teacherController.removeSchedule(id);
            ConsoleUtils.printSuccess("Bloque eliminado.");
        }
    }

    private void printAssignments(List<TeachingAssignment> assignments) {
        ConsoleUtils.printTable(ASSIGNMENT_HEADERS, assignments.stream()
                .map(assignment -> new String[]{String.valueOf(assignment.getId()), assignment.getSubjectName(),
                        assignment.getGradeName(), Formats.text(assignment.getSection()),
                        String.valueOf(assignment.getAcademicYear()), assignment.getTeacherName()})
                .collect(Collectors.toList()));
    }
}
