package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClassSchedule;

import java.util.List;

public interface IClassScheduleDAO {
    void addSchedule(ClassSchedule schedule);

    List<ClassSchedule> findSchedulesByTeacher(int teacherId, int academicYear);

    boolean deleteSchedule(int id);
}
