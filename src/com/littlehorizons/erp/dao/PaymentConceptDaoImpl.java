package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.PaymentConcept;
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

public class PaymentConceptDaoImpl implements IPaymentConceptDAO {
    private Connection con;

    public PaymentConceptDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addConcept(PaymentConcept concept) {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, "
                + "es_recurrente, esta_activo) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, concept);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    concept.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el concepto de pago", e);
        }
    }

    @Override
    public List<PaymentConcept> listAllConcepts() {
        String sql = "SELECT * FROM conceptos_pago ORDER BY nombre_concepto";
        List<PaymentConcept> concepts = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                concepts.add(createConcept(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los conceptos de pago", e);
        }
        return concepts;
    }

    @Override
    public PaymentConcept findConceptById(int id) {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        PaymentConcept concept = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    concept = createConcept(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el concepto de pago", e);
        }
        return concept;
    }

    @Override
    public boolean existsConceptCode(String code, int excludedId) {
        String sql = "SELECT COUNT(*) FROM conceptos_pago WHERE UPPER(codigo_concepto) = UPPER(?) AND id_concepto <> ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code);
            pst.setInt(2, excludedId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar el código del concepto", e);
        }
    }

    @Override
    public boolean conceptHasPayments(int conceptId) {
        String sql = "SELECT COUNT(*) FROM pagos WHERE id_concepto = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, conceptId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar los pagos del concepto", e);
        }
    }

    @Override
    public void updateConcept(PaymentConcept concept) {
        String sql = "UPDATE conceptos_pago SET codigo_concepto = ?, nombre_concepto = ?, descripcion = ?, "
                + "monto_base = ?, es_recurrente = ?, esta_activo = ? WHERE id_concepto = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, concept);
            pst.setInt(7, concept.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el concepto de pago", e);
        }
    }

    @Override
    public void deleteConcept(int id) {
        String sql = "DELETE FROM conceptos_pago WHERE id_concepto = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el concepto de pago", e);
        }
    }

    private void fillStatement(PreparedStatement pst, PaymentConcept concept) throws SQLException {
        pst.setString(1, concept.getCode());
        pst.setString(2, concept.getName());
        SqlUtils.setText(pst, 3, concept.getDescription());
        pst.setBigDecimal(4, concept.getBaseAmount());
        pst.setBoolean(5, concept.isRecurring());
        pst.setBoolean(6, concept.isActive());
    }

    private PaymentConcept createConcept(ResultSet rs) throws SQLException {
        PaymentConcept concept = new PaymentConcept(rs.getString("codigo_concepto"), rs.getString("nombre_concepto"),
                rs.getString("descripcion"), rs.getBigDecimal("monto_base"), rs.getBoolean("es_recurrente"));
        concept.setId(rs.getInt("id_concepto"));
        concept.setActive(rs.getBoolean("esta_activo"));
        return concept;
    }
}
