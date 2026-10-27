package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClassSchedule;
import com.littlehorizons.erp.model.WeekDay;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ClassScheduleDaoImpl implements IClassScheduleDAO {
    private Connection con;

    public ClassScheduleDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addSchedule(ClassSchedule schedule) {
        String sql = "INSERT INTO horarios_clase (id_asignacion, dia_semana, hora_inicio, hora_fin) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, schedule.getAssignmentId());
            pst.setString(2, schedule.getDay().getDbValue());
            pst.setTime(3, Time.valueOf(schedule.getStartTime()));
            pst.setTime(4, Time.valueOf(schedule.getEndTime()));
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    schedule.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el horario", e);
        }
    }

    @Override
    public List<ClassSchedule> findSchedulesByTeacher(int teacherId, int academicYear) {
        String sql = "SELECT h.*, a.id_profesor, CONCAT(s.nombre_materia, ' - ', g.nombre_grado, ' ', "
                + "COALESCE(a.seccion, '')) AS descripcion FROM horarios_clase h"
                + " JOIN asignaciones_profesor a ON a.id_asignacion = h.id_asignacion"
                + " JOIN materias s ON s.id_materia = a.id_materia"
                + " JOIN grados g ON g.id_grado = s.id_grado"
                + " WHERE a.id_profesor = ? AND a.`año_academico` = ?"
                + " ORDER BY FIELD(h.dia_semana, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'),"
                + " h.hora_inicio";
        List<ClassSchedule> schedules = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, teacherId);
            pst.setInt(2, academicYear);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ClassSchedule schedule = new ClassSchedule();
                    schedule.setId(rs.getInt("id_horario"));
                    schedule.setAssignmentId(rs.getInt("id_asignacion"));
                    schedule.setDay(WeekDay.fromDbValue(rs.getString("dia_semana")));
                    schedule.setStartTime(rs.getTime("hora_inicio").toLocalTime());
                    schedule.setEndTime(rs.getTime("hora_fin").toLocalTime());
                    schedule.setTeacherId(rs.getInt("id_profesor"));
                    schedule.setAssignmentDescription(rs.getString("descripcion"));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar el horario del profesor", e);
        }
        return schedules;
    }

    @Override
    public boolean deleteSchedule(int id) {
        String sql = "DELETE FROM horarios_clase WHERE id_horario = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el horario", e);
        }
    }
}
