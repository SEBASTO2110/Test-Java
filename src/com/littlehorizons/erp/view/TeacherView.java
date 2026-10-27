package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.TeacherStatus;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

public class TeacherView {

    private TeacherController teacherController;
    private AcademicPickers pickers;

    public TeacherView() {
        teacherController = new TeacherController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Directorio de profesores");
            System.out.println("""
                1. Registrar profesor
                2. Listar profesores
                3. Buscar por nombre o especialización
                4. Actualizar datos de un profesor
                5. Cambiar estado (Activo, Inactivo, Licencia)
                6. Eliminar profesor
                7. exportador de materias
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar profesor");
                        registerTeacher();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar profesores");
                        printTeachers(teacherController.listTeachers());
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Buscar por nombre o especialización");
                        searchTeachers();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Actualizar datos de un profesor");
                        updateTeacher();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Cambiar estado (Activo, Inactivo, Licencia)");
                        changeStatus();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Eliminar profesor");
                        deleteTeacher();
                        break;
                    
                    case 7:
                        new ExportMateriasView().showMenu();
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

    private void registerTeacher() {
        Teacher teacher = new Teacher();
        readTeacherData(teacher);
        teacherController.registerTeacher(teacher);
        int id = teacher.getId();
        ConsoleUtils.printSuccess("Profesor registrado con ID " + id + ": " + teacher.getFullName());
    }

    private void searchTeachers() {
        printTeachers(teacherController.search(InputReader.readText("Texto a buscar", 100)));
    }

    private void updateTeacher() {
        Teacher teacher = pickers.pickTeacher();
        ConsoleUtils.printInfo("Ingrese los nuevos datos del profesor:");
        readTeacherData(teacher);
        teacherController.updateTeacher(teacher);
        ConsoleUtils.printSuccess("Profesor actualizado.");
    }

    private void changeStatus() {
        Teacher teacher = pickers.pickTeacher();
        ConsoleUtils.printInfo("Estado actual: " + teacher.getStatus().getLabel());
        TeacherStatus status = InputReader.readOption("Nuevo estado", TeacherStatus.values(), TeacherStatus::getLabel);
        teacherController.changeStatus(teacher.getId(), status);
        ConsoleUtils.printSuccess("Estado actualizado a " + status.getLabel() + ".");
    }

    private void deleteTeacher() {
        Teacher teacher = pickers.pickTeacher();
        ConsoleUtils.printWarning("Si el profesor tiene notas registradas no se podrá eliminar; en ese caso "
                + "cámbielo a Inactivo.");
        if (InputReader.confirm("¿Seguro que desea eliminar a " + teacher.getFullName() + "?")) {
            teacherController.deleteTeacher(teacher.getId());
            ConsoleUtils.printSuccess("Profesor eliminado.");
        }
    }

    private void readTeacherData(Teacher teacher) {
        teacher.setDocumentType(InputReader.readOption("Tipo de documento", DocumentType.ADULT_TYPES, DocumentType::getLabel));
        teacher.setDocumentNumber(InputReader.readDocumentNumber("Número de documento"));
        teacher.setFirstName(InputReader.readText("Primer nombre", 100));
        teacher.setMiddleName(InputReader.readOptionalText("Segundo nombre", 100));
        teacher.setLastName(InputReader.readText("Primer apellido", 100));
        teacher.setSecondLastName(InputReader.readOptionalText("Segundo apellido", 100));
        teacher.setEmail(InputReader.readEmail("Correo electrónico", true));
        teacher.setPhone(InputReader.readPhone("Teléfono", false));
        teacher.setSpecialization(InputReader.readText("Especialización (ej: Matemáticas)", 100));
        teacher.setHireDate(InputReader.readDate("Fecha de contratación"));
    }

    private void printTeachers(List<Teacher> teachers) {
        ConsoleUtils.printTable(new String[]{"ID", "Nombre", "Especialización", "Correo", "Teléfono", "Estado"},
                teachers.stream()
                        .map(teacher -> new String[]{String.valueOf(teacher.getId()), teacher.getFullName(),
                                Formats.text(teacher.getSpecialization()), teacher.getEmail(),
                                Formats.text(teacher.getPhone()), teacher.getStatus().getLabel()})
                        .collect(Collectors.toList()));
    }
}
