package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AcademicRecordDaoImpl implements IAcademicRecordDAO {
    private static final String SELECT = "SELECT r.*, s.nombre_materia, "
            + SqlUtils.fullName("e") + " AS nombre_estudiante, "
            + SqlUtils.fullName("p") + " AS nombre_profesor FROM registros_academicos r"
            + " JOIN materias s ON s.id_materia = r.id_materia"
            + " JOIN estudiantes e ON e.id_estudiante = r.id_estudiante"
            + " JOIN profesores p ON p.id_profesor = r.id_profesor";
    private static final String ORDER = " ORDER BY r.`año_academico`, s.nombre_materia,"
            + " FIELD(r.periodo_academico, 'I', 'II', 'III', 'IV', 'Final')";

    private Connection con;

    public AcademicRecordDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addRecord(AcademicRecord record) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, `año_academico`, "
                + "periodo_academico, valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, record.getStudentId());
            pst.setInt(2, record.getSubjectId());
            pst.setInt(3, record.getTeacherId());
            pst.setInt(4, record.getAcademicYear());
            pst.setString(5, record.getPeriod().getDbValue());
            pst.setBigDecimal(6, record.getScore());
            SqlUtils.setText(pst, 7, record.getObservations());
            SqlUtils.setDate(pst, 8, record.getRecordDate());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar la nota", e);
        }
    }

    @Override
    public void updateRecord(AcademicRecord record) {
        String sql = "UPDATE registros_academicos SET id_profesor = ?, valor_nota = ?, observaciones = ?,"
                + " fecha_registro = ? WHERE id_registro = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, record.getTeacherId());
            pst.setBigDecimal(2, record.getScore());
            SqlUtils.setText(pst, 3, record.getObservations());
            SqlUtils.setDate(pst, 4, record.getRecordDate());
            pst.setInt(5, record.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar la nota", e);
        }
    }

    @Override
    public boolean deleteRecord(int id) {
        String sql = "DELETE FROM registros_academicos WHERE id_registro = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar la nota", e);
        }
    }

    @Override
    public AcademicRecord findRecord(int studentId, int subjectId, int academicYear, AcademicPeriod period) {
        String sql = SELECT + " WHERE r.id_estudiante = ? AND r.id_materia = ? AND r.`año_academico` = ?"
                + " AND r.periodo_academico = ?";
        AcademicRecord record = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, subjectId);
            pst.setInt(3, academicYear);
            pst.setString(4, period.getDbValue());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    record = createRecord(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar la nota", e);
        }
        return record;
    }

    @Override
    public List<AcademicRecord> findRecordsByStudent(int studentId) {
        return listRecords(SELECT + " WHERE r.id_estudiante = ?" + ORDER, studentId, null);
    }

    @Override
    public List<AcademicRecord> findRecordsBySubject(int subjectId, int academicYear) {
        return listRecords(SELECT + " WHERE r.id_materia = ? AND r.`año_academico` = ?" + ORDER,
                subjectId, academicYear);
    }

    @Override
    public List<AcademicRecord> findRecordsByGradeAndYear(int gradeId, int academicYear) {
        return listRecords(SELECT + " JOIN matriculas m ON m.id_estudiante = r.id_estudiante"
                + " AND m.`año_academico` = r.`año_academico`"
                + " WHERE m.id_grado = ? AND r.`año_academico` = ?" + ORDER, gradeId, academicYear);
    }

    private List<AcademicRecord> listRecords(String sql, Integer firstParam, Integer secondParam) {
        List<AcademicRecord> records = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, firstParam);
            if (secondParam != null) {
                pst.setInt(2, secondParam);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(createRecord(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar las notas", e);
        }
        return records;
    }

    private AcademicRecord createRecord(ResultSet rs) throws SQLException {
        AcademicRecord record = new AcademicRecord();
        record.setId(rs.getInt("id_registro"));
        record.setStudentId(rs.getInt("id_estudiante"));
        record.setSubjectId(rs.getInt("id_materia"));
        record.setTeacherId(rs.getInt("id_profesor"));
        record.setAcademicYear(rs.getInt("año_academico"));
        record.setPeriod(AcademicPeriod.fromDbValue(rs.getString("periodo_academico")));
        record.setScore(rs.getBigDecimal("valor_nota"));
        record.setObservations(rs.getString("observaciones"));
        record.setRecordDate(SqlUtils.getDate(rs, "fecha_registro"));
        record.setSubjectName(rs.getString("nombre_materia"));
        record.setStudentName(rs.getString("nombre_estudiante"));
        record.setTeacherName(rs.getString("nombre_profesor"));
        return record;
    }
}
