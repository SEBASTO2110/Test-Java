package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeDaoImpl implements IGradeDAO {
    private Connection con;

    public GradeDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public List<Grade> listAllGrades() {
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico";
        List<Grade> grades = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                grades.add(new Grade(rs.getInt("id_grado"), rs.getString("nombre_grado"), rs.getInt("nivel_numerico")));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los grados", e);
        }
        return grades;
    }

    @Override
    public Grade findGradeById(int id) {
        String sql = "SELECT * FROM grados WHERE id_grado = ?";
        Grade grade = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    grade = new Grade(rs.getInt("id_grado"), rs.getString("nombre_grado"), rs.getInt("nivel_numerico"));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el grado", e);
        }
        return grade;
    }
}
