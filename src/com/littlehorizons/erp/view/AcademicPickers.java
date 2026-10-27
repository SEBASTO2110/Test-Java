package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.SubjectController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Reusable prompts to choose students, teachers, subjects, grades, years and periods.
 * Users can type an ID directly or 0 to search first.
 */
public class AcademicPickers {

    private StudentController studentController;
    private TeacherController teacherController;
    private SubjectController subjectController;
    private EnrollmentController enrollmentController;

    public AcademicPickers() {
        studentController = new StudentController();
        teacherController = new TeacherController();
        subjectController = new SubjectController();
        enrollmentController = new EnrollmentController();
    }

    public Student pickStudent() {
        int id = InputReader.readInt("ID del estudiante (0 = buscar)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            String text = InputReader.readText("Nombre, apellido o documento a buscar", 100);
            List<Student> results = text.chars().allMatch(Character::isDigit)
                    ? studentController.listStudents().stream()
                    .filter(student -> student.getDocumentNumber().contains(text))
                    .collect(Collectors.toList())
                    : studentController.searchByName(text);
            if (results.isEmpty()) {
                throw new RuntimeException("No se encontraron estudiantes con \"" + text + "\".");
            }
            ConsoleUtils.printTable(new String[]{"ID", "Documento", "Nombre", "Estado"}, results.stream()
                    .map(student -> new String[]{String.valueOf(student.getId()), student.getDocument(),
                            student.getFullName(), student.getStatus().getLabel()})
                    .collect(Collectors.toList()));
            id = InputReader.readId("ID del estudiante");
        }
        Student student = studentController.findStudent(id);
        ConsoleUtils.printInfo("» " + student);
        return student;
    }

    public Teacher pickTeacher() {
        int id = InputReader.readInt("ID del profesor (0 = buscar)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            String text = InputReader.readText("Nombre o especialización a buscar", 100);
            List<Teacher> results = teacherController.search(text);
            if (results.isEmpty()) {
                throw new RuntimeException("No se encontraron profesores con \"" + text + "\".");
            }
            ConsoleUtils.printTable(new String[]{"ID", "Nombre", "Especialización", "Estado"}, results.stream()
                    .map(teacher -> new String[]{String.valueOf(teacher.getId()), teacher.getFullName(),
                            teacher.getSpecialization(), teacher.getStatus().getLabel()})
                    .collect(Collectors.toList()));
            id = InputReader.readId("ID del profesor");
        }
        Teacher teacher = teacherController.findTeacher(id);
        ConsoleUtils.printInfo("» " + teacher.getFullName() + " - " + teacher.getSpecialization());
        return teacher;
    }

    public Subject pickSubject() {
        int id = InputReader.readInt("ID de la materia (0 = ver materias de un grado)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            Grade grade = pickGrade();
            List<Subject> subjects = subjectController.listByGrade(grade.getId());
            if (subjects.isEmpty()) {
                throw new RuntimeException("El grado " + grade.getName() + " no tiene materias registradas.");
            }
            return InputReader.readFromList("Materias de " + grade.getName(), subjects, Subject::toString);
        }
        Subject subject = subjectController.findSubject(id);
        ConsoleUtils.printInfo("» " + subject);
        return subject;
    }

    public Grade pickGrade() {
        return InputReader.readFromList("Grados", enrollmentController.listGrades(), Grade::toString);
    }

    public int readAcademicYear() {
        int current = EnrollmentController.currentYear();
        return InputReader.readInt("Año académico (" + EnrollmentController.FIRST_ACADEMIC_YEAR + "-" + (current + 1)
                + ", actual " + current + ")", EnrollmentController.FIRST_ACADEMIC_YEAR, current + 1);
    }

    public AcademicPeriod readTerm() {
        return InputReader.readOption("Período académico", AcademicPeriod.TERMS, AcademicPeriod::getLabel);
    }

    public AcademicPeriod readPeriodIncludingFinal() {
        return InputReader.readOption("Período académico", AcademicPeriod.values(), AcademicPeriod::getLabel);
    }

    /**
     * Returns null when the user wants all the terms together.
     */
    public AcademicPeriod readOptionalTerm() {
        if (InputReader.confirm("¿Filtrar por un período específico?")) {
            return readPeriodIncludingFinal();
        }
        return null;
    }
}
