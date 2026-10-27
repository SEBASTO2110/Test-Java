package com.littlehorizons.erp.model;

import java.util.Set;

/**
 * Student whose absences in a week exceed the allowed limit.
 */
public class AbsenceAlert {
    private int studentId;
    private String studentName;
    private long absences;
    private Set<String> subjects;

    public AbsenceAlert(int studentId, String studentName, long absences, Set<String> subjects) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.absences = absences;
        this.subjects = subjects;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public long getAbsences() {
        return absences;
    }

    public Set<String> getSubjects() {
        return subjects;
    }
}
