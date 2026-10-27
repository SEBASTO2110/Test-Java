package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.AttendanceStatus;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDaoImpl implements IAttendanceDAO {
    private static final String SELECT = "SELECT a.*, s.nombre_materia, " + SqlUtils.fullName("e")
            + " AS nombre_estudiante FROM asistencias a"
            + " JOIN materias s ON s.id_materia = a.id_materia"
            + " JOIN estudiantes e ON e.id_estudiante = a.id_estudiante";

    private Connection con;

    public AttendanceDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public int saveAttendance(List<Attendance> records) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, "
                + "observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), "
                + "observaciones = VALUES(observaciones), registrado_por = VALUES(registrado_por)";
        int saved = 0;

        try {
            con.setAutoCommit(false);
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                for (Attendance attendance : records) {
                    pst.setInt(1, attendance.getStudentId());
                    pst.setInt(2, attendance.getSubjectId());
                    SqlUtils.setDate(pst, 3, attendance.getDate());
                    pst.setString(4, attendance.getStatus().getDbValue());
                    SqlUtils.setText(pst, 5, attendance.getObservations());
                    SqlUtils.setInteger(pst, 6, attendance.getRecordedBy());
                    pst.executeUpdate();
                    saved++;
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("guardar la asistencia", e);
        }
        return saved;
    }

    @Override
    public List<Attendance> findAttendanceBySubjectAndDate(int subjectId, LocalDate date) {
        String sql = SELECT + " WHERE a.id_materia = ? AND a.fecha_asistencia = ?"
                + " ORDER BY e.primer_apellido, e.primer_nombre";
        List<Attendance> records = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, subjectId);
            SqlUtils.setDate(pst, 2, date);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(createAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar la asistencia de la clase", e);
        }
        return records;
    }

    @Override
    public List<Attendance> findAttendanceByStudent(int studentId, LocalDate from, LocalDate to) {
        String sql = SELECT + " WHERE a.id_estudiante = ? AND a.fecha_asistencia BETWEEN ? AND ?"
                + " ORDER BY a.fecha_asistencia, s.nombre_materia";
        List<Attendance> records = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            SqlUtils.setDate(pst, 2, from);
            SqlUtils.setDate(pst, 3, to);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(createAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar la asistencia del estudiante", e);
        }
        return records;
    }

    @Override
    public List<Attendance> findAttendanceByDateRange(LocalDate from, LocalDate to) {
        String sql = SELECT + " WHERE a.fecha_asistencia BETWEEN ? AND ? ORDER BY a.fecha_asistencia";
        List<Attendance> records = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            SqlUtils.setDate(pst, 1, from);
            SqlUtils.setDate(pst, 2, to);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    records.add(createAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar la asistencia", e);
        }
        return records;
    }

    private Attendance createAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();
        attendance.setId(rs.getInt("id_asistencia"));
        attendance.setStudentId(rs.getInt("id_estudiante"));
        attendance.setSubjectId(rs.getInt("id_materia"));
        attendance.setDate(SqlUtils.getDate(rs, "fecha_asistencia"));
        attendance.setStatus(AttendanceStatus.fromDbValue(rs.getString("estado_asistencia")));
        attendance.setObservations(rs.getString("observaciones"));
        attendance.setRecordedBy(SqlUtils.getInteger(rs, "registrado_por"));
        attendance.setSubjectName(rs.getString("nombre_materia"));
        attendance.setStudentName(rs.getString("nombre_estudiante"));
        return attendance;
    }
}
