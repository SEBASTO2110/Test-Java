package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;

import java.util.List;

public interface ISubjectDAO {
    void addSubject(Subject subject);

    List<Subject> listAllSubjects();

    Subject findSubjectById(int id);

    Subject findSubjectByCode(String code);

    List<Subject> findSubjectsByGrade(int gradeId);

    void updateSubject(Subject subject);

    void deleteSubject(int id);
}
