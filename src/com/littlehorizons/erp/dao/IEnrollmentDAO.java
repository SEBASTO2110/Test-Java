package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;

import java.util.List;

public interface IEnrollmentDAO {
    void addEnrollment(Enrollment enrollment);

    Enrollment findEnrollmentById(int id);

    Enrollment findEnrollmentByStudentAndYear(int studentId, int academicYear);

    List<Enrollment> findEnrollmentsByStudent(int studentId);

    List<Enrollment> findEnrollmentsByGradeAndYear(int gradeId, int academicYear);

    void updateEnrollment(Enrollment enrollment);
}
