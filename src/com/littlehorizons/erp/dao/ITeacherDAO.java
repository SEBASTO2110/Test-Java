package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherStatus;

import java.util.List;

public interface ITeacherDAO {
    void addTeacher(Teacher teacher);

    List<Teacher> listAllTeachers();

    Teacher findTeacherById(int id);

    Teacher findTeacherByDocument(String documentNumber);

    Teacher findTeacherByEmail(String email);

    List<Teacher> findTeachersByNameOrSpecialization(String text);

    void updateTeacher(Teacher teacher);

    void updateTeacherStatus(int id, TeacherStatus status);

    void deleteTeacher(int id);
}
