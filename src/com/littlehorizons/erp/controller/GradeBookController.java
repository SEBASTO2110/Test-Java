package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDaoImpl;
import com.littlehorizons.erp.dao.IAcademicRecordDAO;
import com.littlehorizons.erp.dao.IStudentDAO;
import com.littlehorizons.erp.dao.ISubjectDAO;
import com.littlehorizons.erp.dao.ISubjectEnrollmentDAO;
import com.littlehorizons.erp.dao.ITeachingAssignmentDAO;
import com.littlehorizons.erp.dao.StudentDaoImpl;
import com.littlehorizons.erp.dao.SubjectDaoImpl;
import com.littlehorizons.erp.dao.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.dao.TeachingAssignmentDaoImpl;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.StudentAverage;
import com.littlehorizons.erp.model.SubjectAverage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Registration of grades and every average of the academic module.
 */
public class GradeBookController {
    private IAcademicRecordDAO recordDAO;
    private IStudentDAO studentDAO;
    private ISubjectDAO subjectDAO;
    private ISubjectEnrollmentDAO subjectEnrollmentDAO;
    private ITeachingAssignmentDAO assignmentDAO;

    public GradeBookController() {
        recordDAO = new AcademicRecordDaoImpl();
        studentDAO = new StudentDaoImpl();
        subjectDAO = new SubjectDaoImpl();
        subjectEnrollmentDAO = new SubjectEnrollmentDaoImpl();
        assignmentDAO = new TeachingAssignmentDaoImpl();
    }

    // ---------------------------------------------------------------- registration

    /**
     * Returns the grade already registered, or null if there is none.
     */
    public AcademicRecord findExisting(int studentId, int subjectId, int academicYear, AcademicPeriod period) {
        return recordDAO.findRecord(studentId, subjectId, academicYear, period);
    }

    /**
     * Rules checked before asking the user for the grade.
     */
    public void validateGradeContext(int studentId, int subjectId, int teacherId, int academicYear) {
        if (studentDAO.findStudentById(studentId) == null) {
            throw new RuntimeException("No existe un estudiante con ID " + studentId + ".");
        }
        if (subjectDAO.findSubjectById(subjectId) == null) {
            throw new RuntimeException("No existe una materia con ID " + subjectId + ".");
        }
        if (!subjectEnrollmentDAO.isEnrolled(studentId, subjectId, academicYear)) {
            throw new RuntimeException("El estudiante no está inscrito en esa materia en " + academicYear + ".");
        }
        boolean teacherAssigned = assignmentDAO.findAssignmentsBySubject(subjectId, academicYear).stream()
                .anyMatch(assignment -> assignment.getTeacherId() == teacherId);
        if (!teacherAssigned) {
            throw new RuntimeException("El profesor no tiene asignada esa materia en " + academicYear + ".");
        }
    }

    /**
     * Saves the grade. If the student already has a grade for that subject and period it is updated.
     * Returns true when an existing grade was updated.
     */
    public boolean saveGrade(AcademicRecord record) {
        validateGradeContext(record.getStudentId(), record.getSubjectId(), record.getTeacherId(),
                record.getAcademicYear());
        int decimals = Math.max(0, record.getScore().stripTrailingZeros().scale());
        if (record.getScore().compareTo(AcademicRecord.MIN_SCORE) < 0
                || record.getScore().compareTo(AcademicRecord.MAX_SCORE) > 0 || decimals > 1) {
            throw new RuntimeException("La nota debe estar entre 0.0 y 5.0 con un solo decimal.");
        }
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        AcademicRecord existing = recordDAO.findRecord(record.getStudentId(), record.getSubjectId(),
                record.getAcademicYear(), record.getPeriod());
        if (existing != null) {
            record.setId(existing.getId());
            recordDAO.updateRecord(record);
            return true;
        }
        recordDAO.addRecord(record);
        return false;
    }

    /**
     * Students enrolled in a subject in a period, used to type the grades of a whole class.
     */
    public List<SubjectEnrollment> classList(int subjectId, int academicYear, AcademicPeriod period) {
        List<SubjectEnrollment> students = subjectEnrollmentDAO.findSubjectEnrollmentsBySubject(subjectId,
                academicYear, period);
        if (students.isEmpty()) {
            throw new RuntimeException("No hay estudiantes inscritos en esa materia para el " + period.getLabel()
                    + " de " + academicYear + ".");
        }
        return students;
    }

    public void deleteGrade(int recordId) {
        if (!recordDAO.deleteRecord(recordId)) {
            throw new RuntimeException("No existe una nota con ID " + recordId + ".");
        }
    }

    public List<AcademicRecord> gradesOfSubject(int subjectId, int academicYear, AcademicPeriod period) {
        return recordDAO.findRecordsBySubject(subjectId, academicYear).stream()
                .filter(record -> record.getPeriod() == period)
                .sorted(Comparator.comparing(AcademicRecord::getStudentName))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------- averages

    /**
     * Average of each subject of a student in a year (periods I to IV, the Final grade is not averaged).
     */
    public List<SubjectAverage> subjectAveragesOfStudent(int studentId, int academicYear) {
        List<AcademicRecord> records = recordDAO.findRecordsByStudent(studentId).stream()
                .filter(record -> record.getAcademicYear() == academicYear)
                .filter(record -> record.getPeriod() != AcademicPeriod.FINAL)
                .collect(Collectors.toList());
        return toSubjectAverages(records);
    }

    /**
     * General average of a student in one period (all subjects).
     */
    public OptionalDouble periodAverageOfStudent(int studentId, int academicYear, AcademicPeriod period) {
        return recordDAO.findRecordsByStudent(studentId).stream()
                .filter(record -> record.getAcademicYear() == academicYear && record.getPeriod() == period)
                .mapToDouble(record -> record.getScore().doubleValue())
                .average();
    }

    /**
     * Average of the whole course (grade) in each subject. If period is null, periods I to IV are used.
     */
    public List<SubjectAverage> courseSubjectAverages(int gradeId, int academicYear, AcademicPeriod period) {
        return toSubjectAverages(recordsOfCourse(gradeId, academicYear, period));
    }

    /**
     * Average of every student of a course, from the best to the lowest.
     */
    public List<StudentAverage> studentAveragesOfCourse(int gradeId, int academicYear, AcademicPeriod period) {
        Map<Integer, List<AcademicRecord>> byStudent = recordsOfCourse(gradeId, academicYear, period).stream()
                .collect(Collectors.groupingBy(AcademicRecord::getStudentId));

        List<StudentAverage> averages = new ArrayList<>();
        for (List<AcademicRecord> records : byStudent.values()) {
            DoubleSummaryStatistics stats = records.stream()
                    .mapToDouble(record -> record.getScore().doubleValue())
                    .summaryStatistics();
            AcademicRecord first = records.get(0);
            averages.add(new StudentAverage(first.getStudentId(), first.getStudentName(), stats.getAverage(),
                    stats.getCount()));
        }
        averages.sort(Comparator.comparingDouble(StudentAverage::getAverage).reversed());
        return averages;
    }

    /**
     * Students of a course whose average is lower than the passing score (they need extra support).
     */
    public List<StudentAverage> studentsAtRisk(int gradeId, int academicYear, AcademicPeriod period) {
        double passingScore = AcademicRecord.PASSING_SCORE.doubleValue();
        return studentAveragesOfCourse(gradeId, academicYear, period).stream()
                .filter(average -> average.getAverage() < passingScore)
                .sorted(Comparator.comparingDouble(StudentAverage::getAverage))
                .collect(Collectors.toList());
    }

    /**
     * Full academic history of a student grouped by year.
     */
    public Map<Integer, List<AcademicRecord>> academicHistory(int studentId) {
        if (studentDAO.findStudentById(studentId) == null) {
            throw new RuntimeException("No existe un estudiante con ID " + studentId + ".");
        }
        return recordDAO.findRecordsByStudent(studentId).stream()
                .collect(Collectors.groupingBy(AcademicRecord::getAcademicYear, TreeMap::new, Collectors.toList()));
    }

    private List<AcademicRecord> recordsOfCourse(int gradeId, int academicYear, AcademicPeriod period) {
        return recordDAO.findRecordsByGradeAndYear(gradeId, academicYear).stream()
                .filter(record -> period == null ? record.getPeriod() != AcademicPeriod.FINAL
                        : record.getPeriod() == period)
                .collect(Collectors.toList());
    }

    private List<SubjectAverage> toSubjectAverages(List<AcademicRecord> records) {
        Map<String, DoubleSummaryStatistics> statsBySubject = records.stream()
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectName, TreeMap::new,
                        Collectors.summarizingDouble(record -> record.getScore().doubleValue())));

        List<SubjectAverage> averages = new ArrayList<>();
        for (Map.Entry<String, DoubleSummaryStatistics> entry : statsBySubject.entrySet()) {
            averages.add(new SubjectAverage(entry.getKey(), entry.getValue().getAverage(), entry.getValue().getCount()));
        }
        return averages;
    }
}
