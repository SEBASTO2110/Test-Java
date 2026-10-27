package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.TeachingAssignment;

import java.util.List;

public interface ITeachingAssignmentDAO {
    void addAssignment(TeachingAssignment assignment);

    TeachingAssignment findAssignmentById(int id);

    List<TeachingAssignment> findAssignmentsByTeacher(int teacherId, int academicYear);

    List<TeachingAssignment> findAssignmentsBySubject(int subjectId, int academicYear);

    List<TeachingAssignment> findAssignmentsByYear(int academicYear);

    void deleteAssignment(int id);
}
