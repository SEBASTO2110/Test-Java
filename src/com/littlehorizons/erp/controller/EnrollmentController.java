package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EnrollmentDaoImpl;
import com.littlehorizons.erp.dao.GradeDaoImpl;
import com.littlehorizons.erp.dao.GuardianDaoImpl;
import com.littlehorizons.erp.dao.IEnrollmentDAO;
import com.littlehorizons.erp.dao.IGradeDAO;
import com.littlehorizons.erp.dao.IGuardianDAO;
import com.littlehorizons.erp.dao.IStudentDAO;
import com.littlehorizons.erp.dao.ISubjectDAO;
import com.littlehorizons.erp.dao.ISubjectEnrollmentDAO;
import com.littlehorizons.erp.dao.StudentDaoImpl;
import com.littlehorizons.erp.dao.SubjectDaoImpl;
import com.littlehorizons.erp.dao.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.EnrollmentStatus;
import com.littlehorizons.erp.model.StudentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Business rules of the grade enrollments (matrículas) and the subject enrollments (inscripciones).
 */
public class EnrollmentController {
    public static final int FIRST_ACADEMIC_YEAR = 2000;
    private static final int LAST_GRADE_LEVEL = 11;

    private IEnrollmentDAO enrollmentDAO;
    private ISubjectEnrollmentDAO subjectEnrollmentDAO;
    private IStudentDAO studentDAO;
    private IGuardianDAO guardianDAO;
    private IGradeDAO gradeDAO;
    private ISubjectDAO subjectDAO;

    public EnrollmentController() {
        enrollmentDAO = new EnrollmentDaoImpl();
        subjectEnrollmentDAO = new SubjectEnrollmentDaoImpl();
        studentDAO = new StudentDaoImpl();
        guardianDAO = new GuardianDaoImpl();
        gradeDAO = new GradeDaoImpl();
        subjectDAO = new SubjectDaoImpl();
    }

    public static int currentYear() {
        return LocalDate.now().getYear();
    }

    public List<Grade> listGrades() {
        return gradeDAO.listAllGrades();
    }

    // ---------------------------------------------------------------- grade enrollment

    public Enrollment enrollStudent(int studentId, int gradeId, int academicYear, LocalDate date, String notes) {
        Student student = studentDAO.findStudentById(studentId);
        if (student == null) {
            throw new RuntimeException("No existe un estudiante con ID " + studentId + ".");
        }
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new RuntimeException("Solo se pueden matricular estudiantes activos (estado actual: "
                    + student.getStatus().getLabel() + ").");
        }
        if (guardianDAO.countGuardiansOf(studentId) == 0) {
            throw new RuntimeException("El estudiante debe tener al menos un acudiente antes de matricularse.");
        }
        if (gradeDAO.findGradeById(gradeId) == null) {
            throw new RuntimeException("El grado seleccionado no existe.");
        }
        Enrollment existing = enrollmentDAO.findEnrollmentByStudentAndYear(studentId, academicYear);
        if (existing != null) {
            throw new RuntimeException("El estudiante ya tiene matrícula en " + academicYear + " ("
                    + existing.getGradeName() + ", " + existing.getStatus().getLabel() + ").");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeId(gradeId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setEnrollmentDate(date);
        enrollment.setNotes(notes);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentDAO.addEnrollment(enrollment);
        return enrollment;
    }

    public Enrollment findEnrollment(int enrollmentId) {
        Enrollment enrollment = enrollmentDAO.findEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new RuntimeException("No existe una matrícula con ID " + enrollmentId + ".");
        }
        return enrollment;
    }

    public List<Enrollment> listByGradeAndYear(int gradeId, int academicYear) {
        return enrollmentDAO.findEnrollmentsByGradeAndYear(gradeId, academicYear);
    }

    public List<Enrollment> enrollmentHistory(int studentId) {
        return enrollmentDAO.findEnrollmentsByStudent(studentId);
    }

    public void withdrawEnrollment(int enrollmentId, LocalDate date, String reason, boolean withdrawStudent) {
        Enrollment enrollment = findActiveEnrollment(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setWithdrawalDate(date);
        enrollment.setNotes(reason);
        enrollmentDAO.updateEnrollment(enrollment);
        if (withdrawStudent) {
            studentDAO.updateStudentStatus(enrollment.getStudentId(), StudentStatus.WITHDRAWN);
        }
    }

    /**
     * Closes the school year of an enrollment. A student who finishes grade 11 becomes Graduated.
     * Returns true when the student graduated.
     */
    public boolean completeEnrollment(int enrollmentId) {
        Enrollment enrollment = findActiveEnrollment(enrollmentId);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollmentDAO.updateEnrollment(enrollment);

        Grade grade = gradeDAO.findGradeById(enrollment.getGradeId());
        boolean isLastGrade = grade != null && grade.getNumericLevel() == LAST_GRADE_LEVEL;
        if (isLastGrade) {
            studentDAO.updateStudentStatus(enrollment.getStudentId(), StudentStatus.GRADUATED);
        }
        return isLastGrade;
    }

    /**
     * Number of enrolled students per grade in a year, ordered by grade level.
     */
    public Map<String, Long> countStudentsByGrade(int academicYear) {
        Map<String, Long> result = new TreeMap<>();
        for (Grade grade : listGrades()) {
            long count = enrollmentDAO.findEnrollmentsByGradeAndYear(grade.getId(), academicYear).stream()
                    .filter(enrollment -> enrollment.getStatus() != EnrollmentStatus.WITHDRAWN)
                    .count();
            result.put(String.format("%02d %s", grade.getNumericLevel(), grade.getName()), count);
        }
        return result;
    }

    // ---------------------------------------------------------------- subject enrollment

    /**
     * Enrolls a student in every active subject of the grade where they are enrolled that year.
     * Returns how many new subject enrollments were created.
     */
    public int enrollInGradeSubjects(int studentId, int academicYear, AcademicPeriod period) {
        Enrollment enrollment = findEnrollmentForYear(studentId, academicYear);
        List<SubjectEnrollment> enrollments = new ArrayList<>();
        for (Subject subject : activeSubjectsOf(enrollment.getGradeId())) {
            enrollments.add(createSubjectEnrollment(studentId, subject.getId(), academicYear, period, null));
        }
        if (enrollments.isEmpty()) {
            throw new RuntimeException("El grado " + enrollment.getGradeName() + " no tiene materias activas.");
        }
        return subjectEnrollmentDAO.addSubjectEnrollments(enrollments);
    }

    /**
     * Enrolls all the students of a grade in all its subjects for a period.
     */
    public int enrollWholeGrade(int gradeId, int academicYear, AcademicPeriod period) {
        List<Subject> subjects = activeSubjectsOf(gradeId);
        List<SubjectEnrollment> enrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollmentDAO.findEnrollmentsByGradeAndYear(gradeId, academicYear)) {
            if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
                continue;
            }
            for (Subject subject : subjects) {
                enrollments.add(createSubjectEnrollment(enrollment.getStudentId(), subject.getId(), academicYear,
                        period, null));
            }
        }
        if (enrollments.isEmpty()) {
            throw new RuntimeException("No hay estudiantes con matrícula activa o materias activas en ese grado.");
        }
        return subjectEnrollmentDAO.addSubjectEnrollments(enrollments);
    }

    public SubjectEnrollment enrollInSubject(int studentId, int subjectId, int academicYear, AcademicPeriod period,
                                             String section) {
        Enrollment enrollment = findEnrollmentForYear(studentId, academicYear);
        Subject subject = subjectDAO.findSubjectById(subjectId);
        if (subject == null) {
            throw new RuntimeException("No existe una materia con ID " + subjectId + ".");
        }
        if (subject.getGradeId() != enrollment.getGradeId()) {
            throw new RuntimeException("La materia " + subject.getName() + " es de " + subject.getGradeName()
                    + " y el estudiante está matriculado en " + enrollment.getGradeName() + ".");
        }
        if (!subject.isActive()) {
            throw new RuntimeException("La materia está inactiva.");
        }
        SubjectEnrollment subjectEnrollment = createSubjectEnrollment(studentId, subjectId, academicYear, period,
                section);
        subjectEnrollmentDAO.addSubjectEnrollment(subjectEnrollment);
        return subjectEnrollment;
    }

    public List<SubjectEnrollment> subjectEnrollmentsOf(int studentId, int academicYear) {
        return subjectEnrollmentDAO.findSubjectEnrollmentsByStudent(studentId, academicYear);
    }

    public void withdrawFromSubject(int subjectEnrollmentId) {
        if (!subjectEnrollmentDAO.updateSubjectEnrollmentStatus(subjectEnrollmentId, EnrollmentStatus.WITHDRAWN)) {
            throw new RuntimeException("No existe una inscripción con ID " + subjectEnrollmentId + ".");
        }
    }

    private Enrollment findActiveEnrollment(int enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new RuntimeException("La matrícula no está activa (estado: " + enrollment.getStatus().getLabel()
                    + ").");
        }
        return enrollment;
    }

    private Enrollment findEnrollmentForYear(int studentId, int academicYear) {
        Enrollment enrollment = enrollmentDAO.findEnrollmentByStudentAndYear(studentId, academicYear);
        if (enrollment == null) {
            throw new RuntimeException("El estudiante no tiene matrícula en " + academicYear + ".");
        }
        if (enrollment.getStatus() == EnrollmentStatus.WITHDRAWN) {
            throw new RuntimeException("La matrícula de " + academicYear + " está retirada.");
        }
        return enrollment;
    }

    private List<Subject> activeSubjectsOf(int gradeId) {
        return subjectDAO.findSubjectsByGrade(gradeId).stream()
                .filter(Subject::isActive)
                .collect(Collectors.toList());
    }

    private SubjectEnrollment createSubjectEnrollment(int studentId, int subjectId, int academicYear,
                                                      AcademicPeriod period, String section) {
        SubjectEnrollment subjectEnrollment = new SubjectEnrollment();
        subjectEnrollment.setStudentId(studentId);
        subjectEnrollment.setSubjectId(subjectId);
        subjectEnrollment.setAcademicYear(academicYear);
        subjectEnrollment.setPeriod(period);
        subjectEnrollment.setSection(section == null ? "A" : section);
        subjectEnrollment.setEnrollmentDate(LocalDate.now());
        subjectEnrollment.setStatus(EnrollmentStatus.ACTIVE);
        return subjectEnrollment;
    }
}
