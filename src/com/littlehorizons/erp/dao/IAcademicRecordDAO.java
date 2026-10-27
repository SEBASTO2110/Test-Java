package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.AcademicPeriod;

import java.util.List;

public interface IAcademicRecordDAO {
    void addRecord(AcademicRecord record);

    void updateRecord(AcademicRecord record);

    boolean deleteRecord(int id);

    AcademicRecord findRecord(int studentId, int subjectId, int academicYear, AcademicPeriod period);

    List<AcademicRecord> findRecordsByStudent(int studentId);

    List<AcademicRecord> findRecordsBySubject(int subjectId, int academicYear);

    List<AcademicRecord> findRecordsByGradeAndYear(int gradeId, int academicYear);
}
