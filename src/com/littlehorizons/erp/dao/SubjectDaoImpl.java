package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
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

public class SubjectDaoImpl implements ISubjectDAO {
    private static final String SELECT = "SELECT s.*, g.nombre_grado FROM materias s"
            + " JOIN grados g ON g.id_grado = s.id_grado";
    private static final String ORDER = " ORDER BY g.nivel_numerico, s.nombre_materia";

    private Connection con;

    public SubjectDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addSubject(Subject subject) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, "
                + "esta_activa) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, subject);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    subject.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar la materia", e);
        }
    }

    @Override
    public List<Subject> listAllSubjects() {
        String sql = SELECT + ORDER;
        List<Subject> subjects = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                subjects.add(createSubject(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar las materias", e);
        }
        return subjects;
    }

    @Override
    public Subject findSubjectById(int id) {
        String sql = SELECT + " WHERE s.id_materia = ?";
        Subject subject = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    subject = createSubject(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar la materia", e);
        }
        return subject;
    }

    @Override
    public Subject findSubjectByCode(String code) {
        String sql = SELECT + " WHERE s.codigo_materia = ?";
        Subject subject = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    subject = createSubject(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar la materia por código", e);
        }
        return subject;
    }

    @Override
    public List<Subject> findSubjectsByGrade(int gradeId) {
        String sql = SELECT + " WHERE s.id_grado = ?" + ORDER;
        List<Subject> subjects = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, gradeId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    subjects.add(createSubject(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar las materias del grado", e);
        }
        return subjects;
    }

    @Override
    public void updateSubject(Subject subject) {
        String sql = "UPDATE materias SET codigo_materia = ?, nombre_materia = ?, descripcion = ?, id_grado = ?, "
                + "horas_semanales = ?, esta_activa = ? WHERE id_materia = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, subject);
            pst.setInt(7, subject.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar la materia", e);
        }
    }

    @Override
    public void deleteSubject(int id) {
        String sql = "DELETE FROM materias WHERE id_materia = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar la materia", e);
        }
    }

    private void fillStatement(PreparedStatement pst, Subject subject) throws SQLException {
        pst.setString(1, subject.getCode());
        pst.setString(2, subject.getName());
        SqlUtils.setText(pst, 3, subject.getDescription());
        pst.setInt(4, subject.getGradeId());
        pst.setInt(5, subject.getWeeklyHours());
        pst.setBoolean(6, subject.isActive());
    }

    private Subject createSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject();
        subject.setId(rs.getInt("id_materia"));
        subject.setCode(rs.getString("codigo_materia"));
        subject.setName(rs.getString("nombre_materia"));
        subject.setDescription(rs.getString("descripcion"));
        subject.setGradeId(rs.getInt("id_grado"));
        subject.setWeeklyHours(rs.getInt("horas_semanales"));
        subject.setActive(rs.getBoolean("esta_activa"));
        subject.setGradeName(rs.getString("nombre_grado"));
        return subject;
    }
}
