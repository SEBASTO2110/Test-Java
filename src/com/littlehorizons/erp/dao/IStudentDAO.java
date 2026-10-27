package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentStatus;

import java.util.List;

public interface IStudentDAO {
    void addStudent(Student student);

    List<Student> listAllStudents();

    Student findStudentById(int id);

    Student findStudentByDocument(String documentNumber);

    List<Student> findStudentsByName(String text);

    List<Student> findStudentsByGuardian(int guardianId);

    List<Student> findStudentsByGradeAndYear(int gradeId, int academicYear);

    void updateStudent(Student student);

    void updateStudentStatus(int id, StudentStatus status);

    void deleteStudent(int id);
}
