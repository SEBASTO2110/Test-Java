package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.StudentConceptPrice;

import java.util.List;

public interface IStudentConceptPriceDAO {
    /**
     * Saves the custom price of a student for a concept (replaces it if it already exists).
     */
    void saveCustomPrice(StudentConceptPrice price);

    StudentConceptPrice findCustomPrice(int studentId, int conceptId);

    List<StudentConceptPrice> listAllCustomPrices();

    boolean deleteCustomPrice(int id);
}
