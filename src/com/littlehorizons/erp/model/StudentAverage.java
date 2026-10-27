package com.littlehorizons.erp.model;

/**
 * Average grade of one student (rankings and academic risk report).
 */
public class StudentAverage {
    private int studentId;
    private String studentName;
    private double average;
    private long gradesCount;

    public StudentAverage(int studentId, String studentName, double average, long gradesCount) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.average = average;
        this.gradesCount = gradesCount;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public double getAverage() {
        return average;
    }

    public long getGradesCount() {
        return gradesCount;
    }
}
