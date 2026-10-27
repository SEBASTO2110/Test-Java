package com.littlehorizons.erp.model;

/**
 * Average grade obtained in one subject.
 */
public class SubjectAverage {
    private String subjectName;
    private double average;
    private long gradesCount;

    public SubjectAverage(String subjectName, double average, long gradesCount) {
        this.subjectName = subjectName;
        this.average = average;
        this.gradesCount = gradesCount;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public double getAverage() {
        return average;
    }

    public long getGradesCount() {
        return gradesCount;
    }
}
