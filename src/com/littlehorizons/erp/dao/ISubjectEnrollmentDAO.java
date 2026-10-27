package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.EnrollmentStatus;

import java.util.List;

public interface ISubjectEnrollmentDAO {
    void addSubjectEnrollment(SubjectEnrollment enrollment);

    /**
     * Saves several enrollments in one transaction, skipping the ones that already exist.
     * Returns how many new enrollments were created.
     */
    int addSubjectEnrollments(List<SubjectEnrollment> enrollments);

    List<SubjectEnrollment> findSubjectEnrollmentsByStudent(int studentId, int academicYear);

    List<SubjectEnrollment> findSubjectEnrollmentsBySubject(int subjectId, int academicYear, AcademicPeriod period);

    boolean isEnrolled(int studentId, int subjectId, int academicYear);

    boolean updateSubjectEnrollmentStatus(int id, EnrollmentStatus status);
}
