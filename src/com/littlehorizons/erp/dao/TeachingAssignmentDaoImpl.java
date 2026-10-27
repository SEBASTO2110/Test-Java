package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.TeachingAssignment;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TeachingAssignmentDaoImpl implements ITeachingAssignmentDAO {
    private static final String SELECT = "SELECT a.*, s.nombre_materia, g.nombre_grado, "
            + SqlUtils.fullName("p") + " AS nombre_profesor FROM asignaciones_profesor a"
            + " JOIN materias s ON s.id_materia = a.id_materia"
            + " JOIN grados g ON g.id_grado = s.id_grado"
            + " JOIN profesores p ON p.id_profesor = a.id_profesor";
    private static final String ORDER = " ORDER BY a.`año_academico` DESC, g.nivel_numerico, s.nombre_materia";

    private Connection con;

    public TeachingAssignmentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addAssignment(TeachingAssignment assignment) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, `año_academico`, seccion)"
                + " VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, assignment.getTeacherId());
            pst.setInt(2, assignment.getSubjectId());
            pst.setInt(3, assignment.getAcademicYear());
            SqlUtils.setText(pst, 4, assignment.getSection());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    assignment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("asignar la materia", e);
        }
    }

    @Override
    public TeachingAssignment findAssignmentById(int id) {
        List<TeachingAssignment> assignments = listAssignments(SELECT + " WHERE a.id_asignacion = ?", id, null);
        return assignments.isEmpty() ? null : assignments.get(0);
    }

    @Override
    public List<TeachingAssignment> findAssignmentsByTeacher(int teacherId, int academicYear) {
        return listAssignments(SELECT + " WHERE a.id_profesor = ? AND a.`año_academico` = ?" + ORDER,
                teacherId, academicYear);
    }

    @Override
    public List<TeachingAssignment> findAssignmentsBySubject(int subjectId, int academicYear) {
        return listAssignments(SELECT + " WHERE a.id_materia = ? AND a.`año_academico` = ?" + ORDER,
                subjectId, academicYear);
    }

    @Override
    public List<TeachingAssignment> findAssignmentsByYear(int academicYear) {
        return listAssignments(SELECT + " WHERE a.`año_academico` = ?" + ORDER, academicYear, null);
    }

    @Override
    public void deleteAssignment(int id) {
        String sql = "DELETE FROM asignaciones_profesor WHERE id_asignacion = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar la asignación", e);
        }
    }

    private List<TeachingAssignment> listAssignments(String sql, Integer firstParam, Integer secondParam) {
        List<TeachingAssignment> assignments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, firstParam);
            if (secondParam != null) {
                pst.setInt(2, secondParam);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    TeachingAssignment assignment = new TeachingAssignment();
                    assignment.setId(rs.getInt("id_asignacion"));
                    assignment.setTeacherId(rs.getInt("id_profesor"));
                    assignment.setSubjectId(rs.getInt("id_materia"));
                    assignment.setAcademicYear(rs.getInt("año_academico"));
                    assignment.setSection(rs.getString("seccion"));
                    assignment.setSubjectName(rs.getString("nombre_materia"));
                    assignment.setGradeName(rs.getString("nombre_grado"));
                    assignment.setTeacherName(rs.getString("nombre_profesor"));
                    assignments.add(assignment);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar las asignaciones", e);
        }
        return assignments;
    }
}
