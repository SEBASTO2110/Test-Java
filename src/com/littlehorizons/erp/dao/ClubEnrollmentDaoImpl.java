package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.MembershipStatus;
import com.littlehorizons.erp.model.ClubEnrollment;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClubEnrollmentDaoImpl implements IClubEnrollmentDAO {
    private static final String SELECT = "SELECT m.*, c.nombre_club,"
            + " CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante"
            + " FROM membresias_club m"
            + " JOIN clubes c ON c.id_club = m.id_club"
            + " JOIN estudiantes e ON e.id_estudiante = m.id_estudiante";

    private Connection con;

    public ClubEnrollmentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addClubEnrollment(ClubEnrollment enrollment) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia)"
                + " VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, enrollment.getStudentId());
            pst.setInt(2, enrollment.getClubId());
            SqlUtils.setDate(pst, 3, enrollment.getJoinDate());
            pst.setString(4, enrollment.getStatus().getDbValue());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    enrollment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("inscribir al estudiante en el club", e);
        }
    }

    @Override
    public ClubEnrollment findClubEnrollmentById(int id) {
        List<ClubEnrollment> enrollments = listEnrollments(SELECT + " WHERE m.id_membresia = ?", id, null);
        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    public List<ClubEnrollment> findClubEnrollmentsByClub(int clubId) {
        return listEnrollments(SELECT + " WHERE m.id_club = ? ORDER BY m.estado_membresia, e.primer_apellido",
                clubId, null);
    }

    @Override
    public List<ClubEnrollment> findClubEnrollmentsByStudent(int studentId) {
        return listEnrollments(SELECT + " WHERE m.id_estudiante = ? ORDER BY m.fecha_ingreso DESC", studentId, null);
    }

    @Override
    public ClubEnrollment findActiveClubEnrollment(int studentId, int clubId) {
        List<ClubEnrollment> enrollments = listEnrollments(SELECT + " WHERE m.id_estudiante = ? AND m.id_club = ?"
                + " AND m.estado_membresia = 'Activa'", studentId, clubId);
        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    public int countActiveMembers(int clubId) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, clubId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("contar los miembros del club", e);
        }
    }

    @Override
    public void withdrawFromClub(int enrollmentId, LocalDate withdrawalDate, String reason) {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = ?, motivo_retiro = ?"
                + " WHERE id_membresia = ? AND estado_membresia = 'Activa'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            SqlUtils.setDate(pst, 1, withdrawalDate);
            SqlUtils.setText(pst, 2, reason);
            pst.setInt(3, enrollmentId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("retirar al estudiante del club", e);
        }
    }

    private List<ClubEnrollment> listEnrollments(String sql, Integer firstParam, Integer secondParam) {
        List<ClubEnrollment> enrollments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, firstParam);
            if (secondParam != null) {
                pst.setInt(2, secondParam);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ClubEnrollment enrollment = new ClubEnrollment();
                    enrollment.setId(rs.getInt("id_membresia"));
                    enrollment.setStudentId(rs.getInt("id_estudiante"));
                    enrollment.setStudentName(rs.getString("nombre_estudiante"));
                    enrollment.setClubId(rs.getInt("id_club"));
                    enrollment.setClubName(rs.getString("nombre_club"));
                    enrollment.setJoinDate(SqlUtils.getDate(rs, "fecha_ingreso"));
                    enrollment.setStatus(MembershipStatus.fromDbValue(rs.getString("estado_membresia")));
                    enrollment.setWithdrawalDate(SqlUtils.getDate(rs, "fecha_retiro"));
                    enrollment.setWithdrawalReason(rs.getString("motivo_retiro"));
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar las membresías", e);
        }
        return enrollments;
    }
}
