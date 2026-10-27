package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.GuardianLink;
import com.littlehorizons.erp.model.DocumentType;
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

public class GuardianDaoImpl implements IGuardianDAO {
    private Connection con;

    public GuardianDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addGuardian(Guardian guardian) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, "
                + "correo_electronico, direccion, ocupacion, es_responsable_financiero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, guardian);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    guardian.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el acudiente", e);
        }
    }

    @Override
    public List<Guardian> listAllGuardians() {
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, segundo_apellido, primer_nombre";
        List<Guardian> guardians = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                guardians.add(createGuardian(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los acudientes", e);
        }
        return guardians;
    }

    @Override
    public Guardian findGuardianById(int id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        Guardian guardian = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    guardian = createGuardian(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el acudiente", e);
        }
        return guardian;
    }

    @Override
    public Guardian findGuardianByDocument(String documentNumber) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        Guardian guardian = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, documentNumber);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    guardian = createGuardian(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el acudiente por documento", e);
        }
        return guardian;
    }

    @Override
    public List<Guardian> findGuardiansByStudent(int studentId) {
        String sql = "SELECT a.* FROM acudientes a"
                + " JOIN estudiante_acudiente ea ON ea.id_acudiente = a.id_acudiente"
                + " WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC";
        List<Guardian> guardians = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    guardians.add(createGuardian(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los acudientes del estudiante", e);
        }
        return guardians;
    }

    @Override
    public void updateGuardian(Guardian guardian) {
        String sql = "UPDATE acudientes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, "
                + "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, parentesco = ?, "
                + "telefono_principal = ?, telefono_secundario = ?, correo_electronico = ?, direccion = ?, "
                + "ocupacion = ?, es_responsable_financiero = ? WHERE id_acudiente = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, guardian);
            pst.setInt(14, guardian.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el acudiente", e);
        }
    }

    @Override
    public void deleteGuardian(int id) {
        String sql = "DELETE FROM acudientes WHERE id_acudiente = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el acudiente", e);
        }
    }

    @Override
    public void linkGuardian(GuardianLink link) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, "
                + "es_contacto_emergencia) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, link.getStudentId());
            pst.setInt(2, link.getGuardianId());
            pst.setBoolean(3, link.isPrimaryContact());
            pst.setBoolean(4, link.isEmergencyContact());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("vincular el acudiente", e);
        }
    }

    @Override
    public void unlinkGuardian(int studentId, int guardianId) {
        String sql = "DELETE FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, guardianId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("desvincular el acudiente", e);
        }
    }

    @Override
    public boolean isLinked(int studentId, int guardianId) {
        String sql = "SELECT COUNT(*) FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, guardianId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar el vínculo del acudiente", e);
        }
    }

    @Override
    public int countGuardiansOf(int studentId) {
        String sql = "SELECT COUNT(*) FROM estudiante_acudiente WHERE id_estudiante = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("contar los acudientes del estudiante", e);
        }
    }

    private void fillStatement(PreparedStatement pst, Guardian guardian) throws SQLException {
        pst.setString(1, guardian.getDocumentType().getDbValue());
        pst.setString(2, guardian.getDocumentNumber());
        pst.setString(3, guardian.getFirstName());
        SqlUtils.setText(pst, 4, guardian.getMiddleName());
        pst.setString(5, guardian.getLastName());
        SqlUtils.setText(pst, 6, guardian.getSecondLastName());
        pst.setString(7, guardian.getRelationship());
        pst.setString(8, guardian.getPrimaryPhone());
        SqlUtils.setText(pst, 9, guardian.getSecondaryPhone());
        SqlUtils.setText(pst, 10, guardian.getEmail());
        SqlUtils.setText(pst, 11, guardian.getAddress());
        SqlUtils.setText(pst, 12, guardian.getOccupation());
        pst.setBoolean(13, guardian.isFinanciallyResponsible());
    }

    private Guardian createGuardian(ResultSet rs) throws SQLException {
        Guardian guardian = new Guardian();
        guardian.setId(rs.getInt("id_acudiente"));
        guardian.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        guardian.setDocumentNumber(rs.getString("numero_documento"));
        guardian.setFirstName(rs.getString("primer_nombre"));
        guardian.setMiddleName(rs.getString("segundo_nombre"));
        guardian.setLastName(rs.getString("primer_apellido"));
        guardian.setSecondLastName(rs.getString("segundo_apellido"));
        guardian.setEmail(rs.getString("correo_electronico"));
        guardian.setAddress(rs.getString("direccion"));
        guardian.setRelationship(rs.getString("parentesco"));
        guardian.setPrimaryPhone(rs.getString("telefono_principal"));
        guardian.setSecondaryPhone(rs.getString("telefono_secundario"));
        guardian.setOccupation(rs.getString("ocupacion"));
        guardian.setFinanciallyResponsible(rs.getBoolean("es_responsable_financiero"));
        return guardian;
    }
}
