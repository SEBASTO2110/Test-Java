package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
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

public class ClubDaoImpl implements IClubDAO {
    private static final String SELECT = "SELECT c.*, CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS nombre_supervisor,"
            + " (SELECT COUNT(*) FROM membresias_club m WHERE m.id_club = c.id_club"
            + " AND m.estado_membresia = 'Activa') AS miembros_activos"
            + " FROM clubes c LEFT JOIN profesores p ON p.id_profesor = c.id_profesor_supervisor";

    private Connection con;

    public ClubDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addClub(Club club) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, "
                + "horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, club);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    club.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el club", e);
        }
    }

    @Override
    public List<Club> listAllClubs() {
        String sql = SELECT + " ORDER BY c.nombre_club";
        List<Club> clubs = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                clubs.add(createClub(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los clubes", e);
        }
        return clubs;
    }

    @Override
    public Club findClubById(int id) {
        String sql = SELECT + " WHERE c.id_club = ?";
        Club club = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    club = createClub(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el club", e);
        }
        return club;
    }

    @Override
    public boolean existsClubName(String name, int excludedId) {
        String sql = "SELECT COUNT(*) FROM clubes WHERE LOWER(nombre_club) = LOWER(?) AND id_club <> ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.setInt(2, excludedId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar el nombre del club", e);
        }
    }

    @Override
    public void updateClub(Club club) {
        String sql = "UPDATE clubes SET nombre_club = ?, descripcion = ?, id_profesor_supervisor = ?, "
                + "capacidad_maxima = ?, horario_reunion = ?, esta_activo = ? WHERE id_club = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, club);
            pst.setInt(7, club.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el club", e);
        }
    }

    @Override
    public void deleteClub(int id) {
        String sql = "DELETE FROM clubes WHERE id_club = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el club", e);
        }
    }

    private void fillStatement(PreparedStatement pst, Club club) throws SQLException {
        pst.setString(1, club.getName());
        SqlUtils.setText(pst, 2, club.getDescription());
        SqlUtils.setInteger(pst, 3, club.getSupervisorTeacherId());
        pst.setInt(4, club.getMaxCapacity());
        SqlUtils.setText(pst, 5, club.getMeetingSchedule());
        pst.setBoolean(6, club.isActive());
    }

    private Club createClub(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setId(rs.getInt("id_club"));
        club.setName(rs.getString("nombre_club"));
        club.setDescription(rs.getString("descripcion"));
        club.setSupervisorTeacherId(SqlUtils.getInteger(rs, "id_profesor_supervisor"));
        club.setSupervisorName(rs.getString("nombre_supervisor"));
        club.setMaxCapacity(rs.getInt("capacidad_maxima"));
        club.setMeetingSchedule(rs.getString("horario_reunion"));
        club.setActive(rs.getBoolean("esta_activo"));
        club.setActiveMembers(rs.getInt("miembros_activos"));
        return club;
    }
}
