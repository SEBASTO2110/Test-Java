package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.controller.GuardianController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.Gender;
import com.littlehorizons.erp.model.StudentStatus;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

public class StudentView {

    private static final List<String> BLOOD_TYPES = List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-",
            "No registra");
    private static final String[] TABLE_HEADERS = {"ID", "Documento", "Nombre completo", "Edad", "Estado"};

    private StudentController studentController;
    private GuardianController guardianController;
    private EnrollmentController enrollmentController;
    private AcademicPickers pickers;

    public StudentView() {
        studentController = new StudentController();
        guardianController = new GuardianController();
        enrollmentController = new EnrollmentController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Estudiantes");
            System.out.println("""
                1. Registrar estudiante
                2. Listar estudiantes
                3. Buscar estudiante por nombre
                4. Ver ficha completa de un estudiante
                5. Actualizar datos de un estudiante
                6. Cambiar estado (Activo, Graduado, Retirado, Suspendido)
                7. Eliminar estudiante
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar estudiante");
                        registerStudent();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar estudiantes");
                        listStudents();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Buscar estudiante por nombre");
                        searchStudents();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Ver ficha completa de un estudiante");
                        showStudentFile();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Actualizar datos de un estudiante");
                        updateStudent();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Cambiar estado (Activo, Graduado, Retirado, Suspendido)");
                        changeStatus();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Eliminar estudiante");
                        deleteStudent();
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

    private void registerStudent() {
        Student student = new Student();
        student.setDocumentType(InputReader.readOption("Tipo de documento", DocumentType.values(), DocumentType::getLabel));
        student.setDocumentNumber(InputReader.readDocumentNumber("Número de documento"));
        readNames(student);
        student.setBirthDate(InputReader.readDate("Fecha de nacimiento"));
        student.setGender(InputReader.readOption("Género", Gender.values(), Gender::getLabel));
        student.setAddress(InputReader.readOptionalText("Dirección", 255));
        student.setPhone(InputReader.readPhone("Teléfono", false));
        student.setEmail(InputReader.readEmail("Correo electrónico", false));
        student.setBloodType(readBloodType());
        student.setAllergies(InputReader.readOptionalText("Alergias", 500));
        student.setMedicalConditions(InputReader.readOptionalText("Condiciones médicas", 500));
        student.setAdmissionDate(InputReader.readDateOrToday("Fecha de ingreso al colegio"));

        studentController.registerStudent(student);
        int id = student.getId();
        ConsoleUtils.printSuccess("Estudiante registrado con ID " + id + ": " + student.getFullName());

        if (InputReader.confirm("¿Desea vincular un acudiente ahora?")) {
            int guardianId = InputReader.readId("ID del acudiente (debe estar registrado)");
            guardianController.linkGuardian(id, guardianId, true, true);
            ConsoleUtils.printSuccess("Acudiente vinculado como contacto principal.");
        } else {
            ConsoleUtils.printWarning("Recuerde vincular un acudiente antes de matricular al estudiante.");
        }
    }

    private void listStudents() {
        List<Student> students = InputReader.confirm("¿Filtrar por estado?")
                ? studentController.listByStatus(InputReader.readOption("Estado", StudentStatus.values(), StudentStatus::getLabel))
                : studentController.listStudents();
        printStudents(students);
    }

    private void searchStudents() {
        String text = InputReader.readText("Texto a buscar en el nombre", 100);
        printStudents(studentController.searchByName(text));
    }

    private void showStudentFile() {
        Student student = pickers.pickStudent();
        ConsoleUtils.printHeader("Ficha del estudiante");
        System.out.println("  ID:                 " + student.getId());
        System.out.println("  Nombre:             " + student.getFullName());
        System.out.println("  Documento:          " + student.getDocumentType().getLabel() + " "
                + student.getDocumentNumber());
        System.out.println("  Nacimiento / Edad:  " + student.getBirthDate() + " (" + student.getAge() + " años)");
        System.out.println("  Género:             " + student.getGender().getLabel());
        System.out.println("  Dirección:          " + Formats.text(student.getAddress()));
        System.out.println("  Teléfono / Correo:  " + Formats.text(student.getPhone()) + " / "
                + Formats.text(student.getEmail()));
        System.out.println("  Tipo de sangre:     " + Formats.text(student.getBloodType()));
        System.out.println("  Alergias:           " + Formats.text(student.getAllergies()));
        System.out.println("  Cond. médicas:      " + Formats.text(student.getMedicalConditions()));
        System.out.println("  Fecha de ingreso:   " + student.getAdmissionDate());
        System.out.println("  Estado:             " + student.getStatus().getLabel());

        ConsoleUtils.printHeader("Acudientes y contactos de emergencia");
        List<Guardian> guardians = studentController.getGuardians(student.getId());
        ConsoleUtils.printTable(new String[]{"ID", "Nombre", "Parentesco", "Teléfono", "Correo"}, guardians.stream()
                .map(guardian -> new String[]{String.valueOf(guardian.getId()), guardian.getFullName(),
                        guardian.getRelationship(), guardian.getPrimaryPhone(), Formats.text(guardian.getEmail())})
                .collect(Collectors.toList()));

        ConsoleUtils.printHeader("Matrículas");
        List<Enrollment> enrollments = enrollmentController.enrollmentHistory(student.getId());
        ConsoleUtils.printTable(new String[]{"Año", "Grado", "Estado", "Fecha"}, enrollments.stream()
                .map(enrollment -> new String[]{String.valueOf(enrollment.getAcademicYear()),
                        enrollment.getGradeName(), enrollment.getStatus().getLabel(),
                        Formats.date(enrollment.getEnrollmentDate())})
                .collect(Collectors.toList()));
    }

    private void updateStudent() {
        Student student = pickers.pickStudent();
        int option;
        do {
            System.out.println("""

                    Campo a modificar:
                      1. Nombres y apellidos
                      2. Documento
                      3. Fecha de nacimiento
                      4. Género
                      5. Dirección
                      6. Teléfono
                      7. Correo electrónico
                      8. Tipo de sangre
                      9. Alergias
                      10. Condiciones médicas
                      11. Fecha de ingreso
                      0. GUARDAR CAMBIOS""");
            option = InputReader.readInt("Opción", 0, 11);
            switch (option) {
                case 1:
                    readNames(student);
                    break;
                case 2:
                    student.setDocumentType(InputReader.readOption("Tipo de documento", DocumentType.values(),
                            DocumentType::getLabel));
                    student.setDocumentNumber(InputReader.readDocumentNumber("Número de documento"));
                    break;
                case 3:
                    student.setBirthDate(InputReader.readDate("Fecha de nacimiento"));
                    break;
                case 4:
                    student.setGender(InputReader.readOption("Género", Gender.values(), Gender::getLabel));
                    break;
                case 5:
                    student.setAddress(InputReader.readOptionalText("Dirección", 255));
                    break;
                case 6:
                    student.setPhone(InputReader.readPhone("Teléfono", false));
                    break;
                case 7:
                    student.setEmail(InputReader.readEmail("Correo electrónico", false));
                    break;
                case 8:
                    student.setBloodType(readBloodType());
                    break;
                case 9:
                    student.setAllergies(InputReader.readOptionalText("Alergias", 500));
                    break;
                case 10:
                    student.setMedicalConditions(InputReader.readOptionalText("Condiciones médicas", 500));
                    break;
                case 11:
                    student.setAdmissionDate(InputReader.readDate("Fecha de ingreso"));
                    break;
                default:
                    break;
            }
        } while (option != 0);

        studentController.updateStudent(student);
        ConsoleUtils.printSuccess("Datos del estudiante actualizados.");
    }

    private void changeStatus() {
        Student student = pickers.pickStudent();
        ConsoleUtils.printInfo("Estado actual: " + student.getStatus().getLabel());
        StudentStatus status = InputReader.readOption("Nuevo estado", StudentStatus.values(), StudentStatus::getLabel);
        studentController.changeStatus(student.getId(), status);
        ConsoleUtils.printSuccess("Estado actualizado a " + status.getLabel() + ".");
    }

    private void deleteStudent() {
        Student student = pickers.pickStudent();
        ConsoleUtils.printWarning("Se eliminarán también sus notas, asistencias, matrículas y demás registros.");
        if (InputReader.confirm("¿Seguro que desea eliminar a " + student.getFullName() + "?")) {
            studentController.deleteStudent(student.getId());
            ConsoleUtils.printSuccess("Estudiante eliminado.");
        } else {
            ConsoleUtils.printInfo("No se eliminó ningún registro.");
        }
    }

    private void readNames(Student student) {
        student.setFirstName(InputReader.readText("Primer nombre", 100));
        student.setMiddleName(InputReader.readOptionalText("Segundo nombre", 100));
        student.setLastName(InputReader.readText("Primer apellido", 100));
        student.setSecondLastName(InputReader.readOptionalText("Segundo apellido", 100));
    }

    private String readBloodType() {
        String bloodType = InputReader.readFromList("Tipo de sangre", BLOOD_TYPES, value -> value);
        return bloodType.equals("No registra") ? null : bloodType;
    }

    private void printStudents(List<Student> students) {
        ConsoleUtils.printTable(TABLE_HEADERS, students.stream()
                .map(student -> new String[]{String.valueOf(student.getId()), student.getDocument(),
                        student.getFullName(), String.valueOf(student.getAge()), student.getStatus().getLabel()})
                .collect(Collectors.toList()));
    }
}
