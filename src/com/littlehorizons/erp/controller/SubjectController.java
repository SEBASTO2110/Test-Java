package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GradeDaoImpl;
import com.littlehorizons.erp.dao.IGradeDAO;
import com.littlehorizons.erp.dao.ISubjectDAO;
import com.littlehorizons.erp.dao.SubjectDaoImpl;
import com.littlehorizons.erp.model.Subject;

import java.util.List;

/**
 * Business rules of the subjects catalog.
 */
public class SubjectController {
    public static final int MAX_WEEKLY_HOURS = 10;

    private ISubjectDAO subjectDAO;
    private IGradeDAO gradeDAO;

    public SubjectController() {
        subjectDAO = new SubjectDaoImpl();
        gradeDAO = new GradeDaoImpl();
    }

    public void registerSubject(Subject subject) {
        validateSubject(subject);
        subject.setCode(subject.getCode().toUpperCase());
        if (subjectDAO.findSubjectByCode(subject.getCode()) != null) {
            throw new RuntimeException("Ya existe una materia con el código " + subject.getCode() + ".");
        }
        subjectDAO.addSubject(subject);
    }

    public void updateSubject(Subject subject) {
        validateSubject(subject);
        subject.setCode(subject.getCode().toUpperCase());
        Subject existing = subjectDAO.findSubjectByCode(subject.getCode());
        if (existing != null && existing.getId() != subject.getId()) {
            throw new RuntimeException("El código " + subject.getCode() + " ya lo usa otra materia.");
        }
        subjectDAO.updateSubject(subject);
    }

    public Subject findSubject(int subjectId) {
        Subject subject = subjectDAO.findSubjectById(subjectId);
        if (subject == null) {
            throw new RuntimeException("No existe una materia con ID " + subjectId + ".");
        }
        return subject;
    }

    public List<Subject> listSubjects() {
        return subjectDAO.listAllSubjects();
    }

    public List<Subject> listByGrade(int gradeId) {
        return subjectDAO.findSubjectsByGrade(gradeId);
    }

    public void deleteSubject(int subjectId) {
        findSubject(subjectId);
        subjectDAO.deleteSubject(subjectId);
    }

    private void validateSubject(Subject subject) {
        if (subject == null) {
            throw new RuntimeException("Error, la materia está vacía.");
        }
        if (gradeDAO.findGradeById(subject.getGradeId()) == null) {
            throw new RuntimeException("El grado seleccionado no existe.");
        }
        if (subject.getWeeklyHours() < 1 || subject.getWeeklyHours() > MAX_WEEKLY_HOURS) {
            throw new RuntimeException("Las horas semanales deben estar entre 1 y " + MAX_WEEKLY_HOURS + ".");
        }
    }
}
