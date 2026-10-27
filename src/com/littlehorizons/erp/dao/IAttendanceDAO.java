package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;

import java.time.LocalDate;
import java.util.List;

public interface IAttendanceDAO {
    /**
     * Saves the attendance of a whole class. If a student already has attendance for
     * that subject and date, it is replaced.
     */
    int saveAttendance(List<Attendance> records);

    List<Attendance> findAttendanceBySubjectAndDate(int subjectId, LocalDate date);

    List<Attendance> findAttendanceByStudent(int studentId, LocalDate from, LocalDate to);

    List<Attendance> findAttendanceByDateRange(LocalDate from, LocalDate to);
}
