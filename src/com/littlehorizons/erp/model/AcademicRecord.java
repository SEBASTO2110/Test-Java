package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Grade (0.0 - 5.0) obtained by a student in a subject and period (table registros_academicos).
 */
public class AcademicRecord {

    public static final BigDecimal MIN_SCORE = new BigDecimal("0.0");
    public static final BigDecimal MAX_SCORE = new BigDecimal("5.0");
    public static final BigDecimal PASSING_SCORE = new BigDecimal("3.0");

    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private AcademicPeriod period;
    private BigDecimal score;
    private String observations;
    private LocalDate recordDate;

    // Read-only data loaded with joins
    private String studentName;
    private String subjectName;
    private String teacherName;

    public boolean isPassing() {
        return score != null && score.compareTo(PASSING_SCORE) >= 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public AcademicPeriod getPeriod() {
        return period;
    }

    public void setPeriod(AcademicPeriod period) {
        this.period = period;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
