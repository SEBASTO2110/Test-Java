package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
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

public class SubjectEnrollmentDaoImpl implements ISubjectEnrollmentDAO {
    private static final String SELECT = "SELECT i.*, s.nombre_materia, " + SqlUtils.fullName("e")
            + " AS nombre_estudiante FROM inscripciones_materia i"
            + " JOIN materias s ON s.id_materia = i.id_materia"
            + " JOIN estudiantes e ON e.id_estudiante = i.id_estudiante";
    private static final String COLUMNS = " (id_estudiante, id_materia, `año_academico`, periodo_academico, seccion, "
            + "fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private Connection con;

    public SubjectEnrollmentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addSubjectEnrollment(SubjectEnrollment enrollment) {
        String sql = "INSERT INTO inscripciones_materia" + COLUMNS;

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, enrollment);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    enrollment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("inscribir al estudiante en la materia", e);
        }
    }

    @Override
    public int addSubjectEnrollments(List<SubjectEnrollment> enrollments) {
        String sql = "INSERT IGNORE INTO inscripciones_materia" + COLUMNS;
        int created = 0;

        try {
            con.setAutoCommit(false);
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                for (SubjectEnrollment enrollment : enrollments) {
                    fillStatement(pst, enrollment);
                    created += pst.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("inscribir a los estudiantes en las materias", e);
        }
        return created;
    }

    @Override
    public List<SubjectEnrollment> findSubjectEnrollmentsByStudent(int studentId, int academicYear) {
        String sql = SELECT + " WHERE i.id_estudiante = ? AND i.`año_academico` = ?"
                + " ORDER BY i.periodo_academico, s.nombre_materia";
        List<SubjectEnrollment> enrollments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, academicYear);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(createSubjectEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar las materias del estudiante", e);
        }
        return enrollments;
    }

    @Override
    public List<SubjectEnrollment> findSubjectEnrollmentsBySubject(int subjectId, int academicYear,
                                                                   AcademicPeriod period) {
        String sql = SELECT + " WHERE i.id_materia = ? AND i.`año_academico` = ? AND i.periodo_academico = ?"
                + " AND i.estado <> 'Retirada' ORDER BY e.primer_apellido, e.primer_nombre";
        List<SubjectEnrollment> enrollments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, subjectId);
            pst.setInt(2, academicYear);
            pst.setString(3, period.getDbValue());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(createSubjectEnrollment(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los estudiantes de la materia", e);
        }
        return enrollments;
    }

    @Override
    public boolean isEnrolled(int studentId, int subjectId, int academicYear) {
        String sql = "SELECT COUNT(*) FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ?"
                + " AND `año_academico` = ? AND estado <> 'Retirada'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, subjectId);
            pst.setInt(3, academicYear);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar la inscripción", e);
        }
    }

    @Override
    public boolean updateSubjectEnrollmentStatus(int id, EnrollmentStatus status) {
        String sql = "UPDATE inscripciones_materia SET estado = ? WHERE id_inscripcion = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status.getDbValue());
            pst.setInt(2, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar la inscripción", e);
        }
    }

    private void fillStatement(PreparedStatement pst, SubjectEnrollment enrollment) throws SQLException {
        pst.setInt(1, enrollment.getStudentId());
        pst.setInt(2, enrollment.getSubjectId());
        pst.setInt(3, enrollment.getAcademicYear());
        pst.setString(4, enrollment.getPeriod().getDbValue());
        SqlUtils.setText(pst, 5, enrollment.getSection());
        SqlUtils.setDate(pst, 6, enrollment.getEnrollmentDate());
        pst.setString(7, enrollment.getStatus().getDbValue());
    }

    private SubjectEnrollment createSubjectEnrollment(ResultSet rs) throws SQLException {
        SubjectEnrollment enrollment = new SubjectEnrollment();
        enrollment.setId(rs.getInt("id_inscripcion"));
        enrollment.setStudentId(rs.getInt("id_estudiante"));
        enrollment.setSubjectId(rs.getInt("id_materia"));
        enrollment.setAcademicYear(rs.getInt("año_academico"));
        enrollment.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
        enrollment.setSection(rs.getString("seccion"));
        enrollment.setEnrollmentDate(SqlUtils.getDate(rs, "fecha_inscripcion"));
        enrollment.setStatus(EnrollmentStatus.fromDbValue(rs.getString("estado")));
        enrollment.setSubjectName(rs.getString("nombre_materia"));
        enrollment.setStudentName(rs.getString("nombre_estudiante"));
        return enrollment;
    }
}
