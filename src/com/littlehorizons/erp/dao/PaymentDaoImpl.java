package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.PaymentMethod;
import com.littlehorizons.erp.model.PaymentPeriod;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.RecurringPaymentStatus;
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

public class PaymentDaoImpl implements IPaymentDAO {
    private static final String SELECT = "SELECT p.*, cp.codigo_concepto, cp.nombre_concepto,"
            + " CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante,"
            + " CONCAT_WS(' ', a.primer_nombre, a.primer_apellido) AS nombre_acudiente"
            + " FROM pagos p"
            + " JOIN estudiantes e ON e.id_estudiante = p.id_estudiante"
            + " JOIN conceptos_pago cp ON cp.id_concepto = p.id_concepto"
            + " LEFT JOIN acudientes a ON a.id_acudiente = p.id_acudiente";
    private static final String ORDER = " ORDER BY p.fecha_pago DESC, p.id_pago DESC";

    /** Subquery that returns data of the main guardian of the student "e". */
    private static final String MAIN_GUARDIAN = " FROM estudiante_acudiente ea"
            + " JOIN acudientes a ON a.id_acudiente = ea.id_acudiente"
            + " WHERE ea.id_estudiante = e.id_estudiante ORDER BY ea.es_principal DESC LIMIT 1";

    private Connection con;

    public PaymentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addPayment(Payment payment) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, "
                + "metodo_pago, numero_recibo, `año_academico`, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, payment.getStudentId());
            SqlUtils.setInteger(pst, 2, payment.getGuardianId());
            pst.setInt(3, payment.getConceptId());
            pst.setBigDecimal(4, payment.getAmountPaid());
            SqlUtils.setDate(pst, 5, payment.getPaymentDate());
            pst.setString(6, payment.getMethod().getDbValue());
            pst.setString(7, payment.getReceiptNumber());
            SqlUtils.setInteger(pst, 8, payment.getAcademicYear());
            SqlUtils.setText(pst, 9, payment.getPeriod() == null ? null : payment.getPeriod().getDbValue());
            SqlUtils.setText(pst, 10, payment.getNotes());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el pago", e);
        }
    }

    @Override
    public Payment findPaymentById(int id) {
        List<Payment> payments = listPayments(SELECT + " WHERE p.id_pago = ?", id);
        return payments.isEmpty() ? null : payments.get(0);
    }

    @Override
    public List<Payment> findPaymentsByStudent(int studentId) {
        return listPayments(SELECT + " WHERE p.id_estudiante = ?" + ORDER, studentId);
    }

    @Override
    public List<Payment> findPaymentsByGuardianStudents(int guardianId) {
        return listPayments(SELECT + " WHERE p.id_estudiante IN"
                + " (SELECT ea.id_estudiante FROM estudiante_acudiente ea WHERE ea.id_acudiente = ?)" + ORDER, guardianId);
    }

    @Override
    public List<Payment> listAllPayments() {
        return listPayments(SELECT + ORDER, null);
    }

    @Override
    public int nextReceiptSequence(int year) {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(numero_recibo, '-', -1) AS UNSIGNED)), 0) + 1"
                + " FROM pagos WHERE numero_recibo LIKE ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "REC-" + year + "-%");
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("calcular el número de recibo", e);
        }
    }

    @Override
    public List<RecurringPaymentStatus> findRecurringPaymentStatus() {
        String sql = "SELECT e.id_estudiante,"
                + " CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante,"
                + " cp.codigo_concepto, cp.nombre_concepto,"
                + " COALESCE(pce.monto_personalizado, cp.monto_base) AS valor_mensual,"
                + " (SELECT MAX(p.fecha_pago) FROM pagos p WHERE p.id_estudiante = e.id_estudiante"
                + " AND p.id_concepto = cp.id_concepto) AS ultimo_pago,"
                + " (SELECT CONCAT_WS(' ', a.primer_nombre, a.primer_apellido)" + MAIN_GUARDIAN + ") AS nombre_acudiente,"
                + " (SELECT a.telefono_principal" + MAIN_GUARDIAN + ") AS telefono_acudiente"
                + " FROM estudiantes e"
                + " CROSS JOIN conceptos_pago cp"
                + " LEFT JOIN precios_concepto_estudiante pce ON pce.id_estudiante = e.id_estudiante"
                + " AND pce.id_concepto = cp.id_concepto"
                + " WHERE e.estado = 'Activo' AND cp.es_recurrente = TRUE AND cp.esta_activo = TRUE"
                + " ORDER BY e.primer_apellido, e.primer_nombre";
        List<RecurringPaymentStatus> rows = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                rows.add(new RecurringPaymentStatus(
                        rs.getInt("id_estudiante"),
                        rs.getString("nombre_estudiante"),
                        rs.getString("codigo_concepto"),
                        rs.getString("nombre_concepto"),
                        rs.getBigDecimal("valor_mensual"),
                        SqlUtils.getDate(rs, "ultimo_pago"),
                        rs.getString("nombre_acudiente"),
                        rs.getString("telefono_acudiente")));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los pagos recurrentes", e);
        }
        return rows;
    }

    private List<Payment> listPayments(String sql, Integer param) {
        List<Payment> payments = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (param != null) {
                pst.setInt(1, param);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    payments.add(createPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los pagos", e);
        }
        return payments;
    }

    private Payment createPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id_pago"));
        payment.setStudentId(rs.getInt("id_estudiante"));
        payment.setStudentName(rs.getString("nombre_estudiante"));
        payment.setGuardianId(SqlUtils.getInteger(rs, "id_acudiente"));
        payment.setGuardianName(rs.getString("nombre_acudiente"));
        payment.setConceptId(rs.getInt("id_concepto"));
        payment.setConceptCode(rs.getString("codigo_concepto"));
        payment.setConceptName(rs.getString("nombre_concepto"));
        payment.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        payment.setPaymentDate(SqlUtils.getDate(rs, "fecha_pago"));
        payment.setMethod(PaymentMethod.fromDbValue(rs.getString("metodo_pago")));
        payment.setReceiptNumber(rs.getString("numero_recibo"));
        payment.setAcademicYear(SqlUtils.getInteger(rs, "año_academico"));
        payment.setPeriod(PaymentPeriod.fromDbValue(rs.getString("periodo_academico")));
        payment.setNotes(rs.getString("notas"));
        return payment;
    }
}
