package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDaoImpl;
import com.littlehorizons.erp.dao.IGuardianDAO;
import com.littlehorizons.erp.dao.IStudentDAO;
import com.littlehorizons.erp.dao.StudentDaoImpl;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Business rules of the students.
 */
public class StudentController {
    private static final int MIN_AGE = 3;
    private static final int MAX_AGE = 25;

    private IStudentDAO studentDAO;
    private IGuardianDAO guardianDAO;

    public StudentController() {
        studentDAO = new StudentDaoImpl();
        guardianDAO = new GuardianDaoImpl();
    }

    public void registerStudent(Student student) {
        validateStudent(student);
        Student existing = studentDAO.findStudentByDocument(student.getDocumentNumber());
        if (existing != null) {
            throw new RuntimeException("Ya existe un estudiante con el documento " + student.getDocumentNumber()
                    + ": " + existing.getFullName());
        }
        student.setStatus(StudentStatus.ACTIVE);
        studentDAO.addStudent(student);
    }

    public void updateStudent(Student student) {
        validateStudent(student);
        Student existing = studentDAO.findStudentByDocument(student.getDocumentNumber());
        if (existing != null && existing.getId() != student.getId()) {
            throw new RuntimeException("El documento " + student.getDocumentNumber() + " ya pertenece a otro estudiante.");
        }
        studentDAO.updateStudent(student);
    }

    public Student findStudent(int studentId) {
        Student student = studentDAO.findStudentById(studentId);
        if (student == null) {
            throw new RuntimeException("No existe un estudiante con ID " + studentId + ".");
        }
        return student;
    }

    /**
     * Used by the other modules: clubs, payments and loans only accept active students.
     */
    public Student findActiveStudent(int studentId) {
        Student student = findStudent(studentId);
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new RuntimeException("El estudiante " + student.getFullName() + " no está activo (estado: "
                    + student.getStatus().getLabel() + ").");
        }
        return student;
    }

    public List<Student> listStudents() {
        return studentDAO.listAllStudents();
    }

    public List<Student> listByStatus(StudentStatus status) {
        return studentDAO.listAllStudents().stream()
                .filter(student -> student.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Student> searchByName(String text) {
        return studentDAO.findStudentsByName(text);
    }

    public List<Student> searchByDocument(String text) {
        return studentDAO.listAllStudents().stream()
                .filter(student -> student.getDocumentNumber().contains(text))
                .collect(Collectors.toList());
    }

    public List<Guardian> getGuardians(int studentId) {
        findStudent(studentId);
        return guardianDAO.findGuardiansByStudent(studentId);
    }

    public void changeStatus(int studentId, StudentStatus status) {
        Student student = findStudent(studentId);
        if (student.getStatus() == status) {
            throw new RuntimeException("El estudiante ya se encuentra en estado " + status.getLabel() + ".");
        }
        studentDAO.updateStudentStatus(studentId, status);
    }

    public void deleteStudent(int studentId) {
        findStudent(studentId);
        studentDAO.deleteStudent(studentId);
    }

    /**
     * Number of students per status, ordered alphabetically.
     */
    public Map<String, Long> countByStatus() {
        return studentDAO.listAllStudents().stream()
                .collect(Collectors.groupingBy(student -> student.getStatus().getLabel(), TreeMap::new,
                        Collectors.counting()));
    }

    /**
     * Active students that still do not have a guardian.
     */
    public List<Student> studentsWithoutGuardian() {
        return studentDAO.listAllStudents().stream()
                .filter(student -> student.getStatus() == StudentStatus.ACTIVE)
                .filter(student -> guardianDAO.countGuardiansOf(student.getId()) == 0)
                .collect(Collectors.toList());
    }

    private void validateStudent(Student student) {
        if (student == null) {
            throw new RuntimeException("Error, el estudiante está vacío.");
        }
        LocalDate today = LocalDate.now();
        if (student.getBirthDate().isAfter(today.minusYears(MIN_AGE))
                || student.getBirthDate().isBefore(today.minusYears(MAX_AGE))) {
            throw new RuntimeException("La edad del estudiante debe estar entre " + MIN_AGE + " y " + MAX_AGE + " años.");
        }
        if (student.getAdmissionDate().isAfter(today)) {
            throw new RuntimeException("La fecha de ingreso no puede ser futura.");
        }
        if (student.getAdmissionDate().isBefore(student.getBirthDate().plusYears(MIN_AGE - 1))) {
            throw new RuntimeException("La fecha de ingreso no es coherente con la fecha de nacimiento.");
        }
    }
}
