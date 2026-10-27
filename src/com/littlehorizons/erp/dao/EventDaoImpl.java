package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.EventType;
import com.littlehorizons.erp.model.Event;
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

public class EventDaoImpl implements IEventDAO {
    private static final String SELECT = "SELECT ev.*, CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS nombre_organizador,"
            + " (SELECT COUNT(*) FROM participantes_evento pe WHERE pe.id_evento = ev.id_evento) AS participantes"
            + " FROM eventos ev LEFT JOIN profesores p ON p.id_profesor = ev.id_organizador";

    private Connection con;

    public EventDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, "
                + "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, event);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    event.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el evento", e);
        }
    }

    @Override
    public List<Event> listAllEvents() {
        String sql = SELECT + " ORDER BY ev.fecha_evento DESC";
        List<Event> events = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                events.add(createEvent(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los eventos", e);
        }
        return events;
    }

    @Override
    public Event findEventById(int id) {
        String sql = SELECT + " WHERE ev.id_evento = ?";
        Event event = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    event = createEvent(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el evento", e);
        }
        return event;
    }

    @Override
    public void updateEvent(Event event) {
        String sql = "UPDATE eventos SET nombre_evento = ?, descripcion = ?, tipo_evento = ?, fecha_evento = ?, "
                + "lugar = ?, tiene_competencia = ?, id_organizador = ?, esta_activo = ? WHERE id_evento = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, event);
            pst.setInt(9, event.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el evento", e);
        }
    }

    @Override
    public void deleteEvent(int id) {
        String sql = "DELETE FROM eventos WHERE id_evento = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el evento", e);
        }
    }

    private void fillStatement(PreparedStatement pst, Event event) throws SQLException {
        pst.setString(1, event.getName());
        SqlUtils.setText(pst, 2, event.getDescription());
        pst.setString(3, event.getType().getDbValue());
        SqlUtils.setDate(pst, 4, event.getEventDate());
        SqlUtils.setText(pst, 5, event.getLocation());
        pst.setBoolean(6, event.hasCompetition());
        SqlUtils.setInteger(pst, 7, event.getOrganizerTeacherId());
        pst.setBoolean(8, event.isActive());
    }

    private Event createEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setId(rs.getInt("id_evento"));
        event.setName(rs.getString("nombre_evento"));
        event.setDescription(rs.getString("descripcion"));
        event.setType(EventType.fromDbValue(rs.getString("tipo_evento")));
        event.setEventDate(SqlUtils.getDate(rs, "fecha_evento"));
        event.setLocation(rs.getString("lugar"));
        event.setHasCompetition(rs.getBoolean("tiene_competencia"));
        event.setOrganizerTeacherId(SqlUtils.getInteger(rs, "id_organizador"));
        event.setOrganizerName(rs.getString("nombre_organizador"));
        event.setActive(rs.getBoolean("esta_activo"));
        event.setParticipantCount(rs.getInt("participantes"));
        return event;
    }
}
