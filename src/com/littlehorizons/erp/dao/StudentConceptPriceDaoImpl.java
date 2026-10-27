package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.StudentConceptPrice;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentConceptPriceDaoImpl implements IStudentConceptPriceDAO {
    private static final String SELECT = "SELECT pce.*, cp.nombre_concepto, cp.monto_base,"
            + " CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante"
            + " FROM precios_concepto_estudiante pce"
            + " JOIN conceptos_pago cp ON cp.id_concepto = pce.id_concepto"
            + " JOIN estudiantes e ON e.id_estudiante = pce.id_estudiante";

    private Connection con;

    public StudentConceptPriceDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void saveCustomPrice(StudentConceptPrice price) {
        String sql = "INSERT INTO precios_concepto_estudiante (id_estudiante, id_concepto, monto_personalizado, motivo)"
                + " VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE monto_personalizado = VALUES(monto_personalizado),"
                + " motivo = VALUES(motivo)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, price.getStudentId());
            pst.setInt(2, price.getConceptId());
            pst.setBigDecimal(3, price.getCustomAmount());
            pst.setString(4, price.getReason());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("guardar el valor personalizado", e);
        }
    }

    @Override
    public StudentConceptPrice findCustomPrice(int studentId, int conceptId) {
        String sql = SELECT + " WHERE pce.id_estudiante = ? AND pce.id_concepto = ?";
        StudentConceptPrice price = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, studentId);
            pst.setInt(2, conceptId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    price = createPrice(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el valor personalizado", e);
        }
        return price;
    }

    @Override
    public List<StudentConceptPrice> listAllCustomPrices() {
        String sql = SELECT + " ORDER BY e.primer_apellido, cp.nombre_concepto";
        List<StudentConceptPrice> prices = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                prices.add(createPrice(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los valores personalizados", e);
        }
        return prices;
    }

    @Override
    public boolean deleteCustomPrice(int id) {
        String sql = "DELETE FROM precios_concepto_estudiante WHERE id_precio = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el valor personalizado", e);
        }
    }

    private StudentConceptPrice createPrice(ResultSet rs) throws SQLException {
        StudentConceptPrice price = new StudentConceptPrice(rs.getInt("id_estudiante"), rs.getInt("id_concepto"),
                rs.getBigDecimal("monto_personalizado"), rs.getString("motivo"));
        price.setId(rs.getInt("id_precio"));
        price.setStudentName(rs.getString("nombre_estudiante"));
        price.setConceptName(rs.getString("nombre_concepto"));
        price.setBaseAmount(rs.getBigDecimal("monto_base"));
        return price;
    }
}
