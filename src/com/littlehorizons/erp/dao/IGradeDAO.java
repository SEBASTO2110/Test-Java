package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Grade;

import java.util.List;

public interface IGradeDAO {
    List<Grade> listAllGrades();

    Grade findGradeById(int id);
}
