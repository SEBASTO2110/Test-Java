package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.GuardianController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

public class GuardianView {

    private static final List<String> RELATIONSHIPS = List.of("Padre", "Madre", "Abuelo", "Abuela", "Tío", "Tía",
            "Hermano(a)", "Tutor Legal");

    private GuardianController guardianController;
    private AcademicPickers pickers;

    public GuardianView() {
        guardianController = new GuardianController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Acudientes");
            System.out.println("""
                1. Registrar acudiente
                2. Listar acudientes
                3. Actualizar datos de un acudiente
                4. Vincular acudiente a un estudiante
                5. Desvincular acudiente de un estudiante
                6. Ver estudiantes a cargo de un acudiente
                7. Eliminar acudiente
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar acudiente");
                        registerGuardian();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar acudientes");
                        listGuardians();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Actualizar datos de un acudiente");
                        updateGuardian();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Vincular acudiente a un estudiante");
                        linkGuardian();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Desvincular acudiente de un estudiante");
                        unlinkGuardian();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Ver estudiantes a cargo de un acudiente");
                        showStudentsOfGuardian();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Eliminar acudiente");
                        deleteGuardian();
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

    private void registerGuardian() {
        Guardian guardian = new Guardian();
        guardian.setDocumentType(InputReader.readOption("Tipo de documento", DocumentType.ADULT_TYPES, DocumentType::getLabel));
        guardian.setDocumentNumber(InputReader.readDocumentNumber("Número de documento"));
        readPersonalData(guardian);
        guardianController.registerGuardian(guardian);
        int id = guardian.getId();
        ConsoleUtils.printSuccess("Acudiente registrado con ID " + id + ": " + guardian.getFullName());

        if (InputReader.confirm("¿Desea vincularlo a un estudiante ahora?")) {
            Student student = pickers.pickStudent();
            boolean primary = InputReader.confirm("¿Es el acudiente principal?");
            guardianController.linkGuardian(student.getId(), id, primary, true);
            ConsoleUtils.printSuccess("Acudiente vinculado a " + student.getFullName() + ".");
        }
    }

    private void listGuardians() {
        printGuardians(guardianController.listGuardians());
    }

    private void updateGuardian() {
        Guardian guardian = guardianController.findGuardian(InputReader.readId("ID del acudiente"));
        ConsoleUtils.printInfo("» " + guardian + " - Ingrese de nuevo los datos de contacto:");
        readPersonalData(guardian);
        guardianController.updateGuardian(guardian);
        ConsoleUtils.printSuccess("Acudiente actualizado.");
    }

    private void linkGuardian() {
        Student student = pickers.pickStudent();
        Guardian guardian = guardianController.findGuardian(InputReader.readId("ID del acudiente"));
        boolean primary = InputReader.confirm("¿Es el acudiente principal para comunicaciones?");
        boolean emergency = InputReader.confirm("¿Es contacto de emergencia?");
        guardianController.linkGuardian(student.getId(), guardian.getId(), primary, emergency);
        ConsoleUtils.printSuccess(guardian.getFullName() + " ahora es acudiente de " + student.getFullName() + ".");
    }

    private void unlinkGuardian() {
        Student student = pickers.pickStudent();
        int guardianId = InputReader.readId("ID del acudiente a desvincular");
        guardianController.unlinkGuardian(student.getId(), guardianId);
        ConsoleUtils.printSuccess("Acudiente desvinculado.");
    }

    private void showStudentsOfGuardian() {
        Guardian guardian = guardianController.findGuardian(InputReader.readId("ID del acudiente"));
        ConsoleUtils.printInfo("Estudiantes a cargo de " + guardian.getFullName() + ":");
        ConsoleUtils.printTable(new String[]{"ID", "Documento", "Nombre", "Estado"},
                guardianController.getStudentsOf(guardian.getId()).stream()
                        .map(student -> new String[]{String.valueOf(student.getId()), student.getDocument(),
                                student.getFullName(), student.getStatus().getLabel()})
                        .collect(Collectors.toList()));
    }

    private void deleteGuardian() {
        Guardian guardian = guardianController.findGuardian(InputReader.readId("ID del acudiente"));
        if (InputReader.confirm("¿Seguro que desea eliminar a " + guardian.getFullName() + "?")) {
            guardianController.deleteGuardian(guardian.getId());
            ConsoleUtils.printSuccess("Acudiente eliminado.");
        }
    }

    private void readPersonalData(Guardian guardian) {
        guardian.setFirstName(InputReader.readText("Primer nombre", 100));
        guardian.setMiddleName(InputReader.readOptionalText("Segundo nombre", 100));
        guardian.setLastName(InputReader.readText("Primer apellido", 100));
        guardian.setSecondLastName(InputReader.readOptionalText("Segundo apellido", 100));
        guardian.setRelationship(InputReader.readFromList("Parentesco", RELATIONSHIPS, value -> value));
        guardian.setPrimaryPhone(InputReader.readPhone("Teléfono principal", true));
        guardian.setSecondaryPhone(InputReader.readPhone("Teléfono secundario", false));
        guardian.setEmail(InputReader.readEmail("Correo electrónico", false));
        guardian.setAddress(InputReader.readOptionalText("Dirección", 255));
        guardian.setOccupation(InputReader.readOptionalText("Ocupación", 100));
        guardian.setFinanciallyResponsible(InputReader.confirm("¿Es responsable de los pagos?"));
    }

    private void printGuardians(List<Guardian> guardians) {
        ConsoleUtils.printTable(new String[]{"ID", "Documento", "Nombre", "Parentesco", "Teléfono", "Resp. pagos"},
                guardians.stream()
                        .map(guardian -> new String[]{String.valueOf(guardian.getId()), guardian.getDocument(),
                                guardian.getFullName(), guardian.getRelationship(), guardian.getPrimaryPhone(),
                                Formats.yesNo(guardian.isFinanciallyResponsible())})
                        .collect(Collectors.toList()));
    }
}
