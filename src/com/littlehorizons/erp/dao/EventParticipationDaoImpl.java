package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.CompetitionPosition;
import com.littlehorizons.erp.model.ParticipationRole;
import com.littlehorizons.erp.model.EventParticipation;
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

public class EventParticipationDaoImpl implements IEventParticipationDAO {
    private static final String SELECT = "SELECT pe.*, ev.nombre_evento,"
            + " CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante"
            + " FROM participantes_evento pe"
            + " JOIN eventos ev ON ev.id_evento = pe.id_evento"
            + " JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante";

    private Connection con;

    public EventParticipationDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addParticipation(EventParticipation participation) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, "
                + "observaciones) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, participation.getEventId());
            pst.setInt(2, participation.getStudentId());
            pst.setString(3, participation.getRole().getDbValue());
            SqlUtils.setText(pst, 4, participation.getPosition() == null ? null
                    : participation.getPosition().getDbValue());
            SqlUtils.setText(pst, 5, participation.getNotes());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    participation.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el participante", e);
        }
    }

    @Override
    public EventParticipation findParticipationById(int id) {
        List<EventParticipation> participations = listParticipations(SELECT + " WHERE pe.id_participacion = ?", id);
        return participations.isEmpty() ? null : participations.get(0);
    }

    @Override
    public List<EventParticipation> findParticipationsByEvent(int eventId) {
        return listParticipations(SELECT + " WHERE pe.id_evento = ?"
                + " ORDER BY pe.posicion_obtenida IS NULL, pe.posicion_obtenida, e.primer_apellido", eventId);
    }

    @Override
    public List<EventParticipation> findParticipationsByStudent(int studentId) {
        return listParticipations(SELECT + " WHERE pe.id_estudiante = ? ORDER BY ev.fecha_evento DESC", studentId);
    }

    @Override
    public List<EventParticipation> listAllParticipations() {
        return listParticipations(SELECT + " ORDER BY ev.fecha_evento DESC", null);
    }

    @Override
    public boolean existsParticipation(int eventId, int studentId) {
        String sql = "SELECT COUNT(*) FROM participantes_evento WHERE id_evento = ? AND id_estudiante = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, eventId);
            pst.setInt(2, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar la participación", e);
        }
    }

    @Override
    public boolean isPositionTaken(int eventId, CompetitionPosition position, int excludedParticipationId) {
        String sql = "SELECT COUNT(*) FROM participantes_evento WHERE id_evento = ? AND posicion_obtenida = ?"
                + " AND id_participacion <> ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, eventId);
            pst.setString(2, position.getDbValue());
            pst.setInt(3, excludedParticipationId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar la posición", e);
        }
    }

    @Override
    public void updatePosition(int participationId, CompetitionPosition position) {
        String sql = "UPDATE participantes_evento SET posicion_obtenida = ? WHERE id_participacion = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, position.getDbValue());
            pst.setInt(2, participationId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("asignar la posición", e);
        }
    }

    @Override
    public boolean deleteParticipation(int id) {
        String sql = "DELETE FROM participantes_evento WHERE id_participacion = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el participante", e);
        }
    }

    private List<EventParticipation> listParticipations(String sql, Integer param) {
        List<EventParticipation> participations = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (param != null) {
                pst.setInt(1, param);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    EventParticipation participation = new EventParticipation();
                    participation.setId(rs.getInt("id_participacion"));
                    participation.setEventId(rs.getInt("id_evento"));
                    participation.setEventName(rs.getString("nombre_evento"));
                    participation.setStudentId(rs.getInt("id_estudiante"));
                    participation.setStudentName(rs.getString("nombre_estudiante"));
                    participation.setRole(ParticipationRole.fromDbValue(rs.getString("rol_participacion")));
                    participation.setPosition(CompetitionPosition.fromDbValue(rs.getString("posicion_obtenida")));
                    participation.setNotes(rs.getString("observaciones"));
                    participations.add(participation);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los participantes", e);
        }
        return participations;
    }
}
