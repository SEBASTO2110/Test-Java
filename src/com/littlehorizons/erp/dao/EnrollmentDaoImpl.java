package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.EnrollmentStatus;
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

public class EnrollmentDaoImpl implements IEnrollmentDAO {
    private static final String SELECT = "SELECT m.*, g.nombre_grado, " + SqlUtils.fullName("e")
            + " AS nombre_estudiante FROM matriculas m"
            + " JOIN grados g ON g.id_grado = m.id_grado"
            + " JOIN estudiantes e ON e.id_estudiante = m.id_estudiante";

    private Connection con;

    public EnrollmentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addEnrollment(Enrollment enrollment) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, `año_academico`, estado_matricula, "
                + "fecha_matricula, fecha_retiro, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, enrollment);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    enrollment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar la matrícula", e);
        }
    }

    @Override
    public Enrollment findEnrollmentById(int id) {
        List<Enrollment> enrollments = listEnrollments(SELECT + " WHERE m.id_matricula = ?", id, null);
        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    public Enrollment findEnrollmentByStudentAndYear(int studentId, int academicYear) {
        List<Enrollment> enrollments = listEnrollments(SELECT + " WHERE m.id_estudiante = ? AND m.`año_academico` = ?",
                studentId, academicYear);
        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    public List<Enrollment> findEnrollmentsByStudent(int studentId) {
        return listEnrollments(SELECT + " WHERE m.id_estudiante = ? ORDER BY m.`año_academico`", studentId, null);
    }

    @Override
    public List<Enrollment> findEnrollmentsByGradeAndYear(int gradeId, int academicYear) {
        return listEnrollments(SELECT + " WHERE m.id_grado = ? AND m.`año_academico` = ?"
                + " ORDER BY e.primer_apellido, e.primer_nombre", gradeId, academicYear);
    }

    @Override
    public void updateEnrollment(Enrollment enrollment) {
        String sql = "UPDATE matriculas SET id_estudiante = ?, id_grado = ?, `año_academico` = ?, estado_matricula = ?, "
                + "fecha_matricula = ?, fecha_retiro = ?, observaciones = ? WHERE id_matricula = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, enrollment);
            pst.setInt(8, enrollment.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar la matrícula", e);
        }
    }

    private List<Enrollment> listEnrollments(String sql, Integer firstParam, Integer secondParam) {
        List<Enrollment> enrollments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, firstParam);
            if (secondParam != null) {
                pst.setInt(2, secondParam);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(createEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar las matrículas", e);
        }
        return enrollments;
    }

    private void fillStatement(PreparedStatement pst, Enrollment enrollment) throws SQLException {
        pst.setInt(1, enrollment.getStudentId());
        pst.setInt(2, enrollment.getGradeId());
        pst.setInt(3, enrollment.getAcademicYear());
        pst.setString(4, enrollment.getStatus().getDbValue());
        SqlUtils.setDate(pst, 5, enrollment.getEnrollmentDate());
        SqlUtils.setDate(pst, 6, enrollment.getWithdrawalDate());
        SqlUtils.setText(pst, 7, enrollment.getNotes());
    }

    private Enrollment createEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id_matricula"));
        enrollment.setStudentId(rs.getInt("id_estudiante"));
        enrollment.setGradeId(rs.getInt("id_grado"));
        enrollment.setAcademicYear(rs.getInt("año_academico"));
        enrollment.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado_matricula")));
        enrollment.setEnrollmentDate(SqlUtils.getDate(rs, "fecha_matricula"));
        enrollment.setWithdrawalDate(SqlUtils.getDate(rs, "fecha_retiro"));
        enrollment.setNotes(rs.getString("observaciones"));
        enrollment.setGradeName(rs.getString("nombre_grado"));
        enrollment.setStudentName(rs.getString("nombre_estudiante"));
        return enrollment;
    }
}
