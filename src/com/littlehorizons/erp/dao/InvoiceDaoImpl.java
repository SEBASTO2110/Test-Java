package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
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

public class InvoiceDaoImpl implements IInvoiceDAO {
    private Connection con;

    public InvoiceDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addInvoice(Invoice invoice) {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, "
                + "ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, invoice.getInvoiceNumber());
            pst.setInt(2, invoice.getPaymentId());
            SqlUtils.setDate(pst, 3, invoice.getIssueDate());
            pst.setString(4, invoice.getCufe());
            SqlUtils.setText(pst, 5, invoice.getPdfFileName());
            SqlUtils.setText(pst, 6, invoice.getFilePath());
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    invoice.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar la factura", e);
        }
    }

    @Override
    public Invoice findInvoiceByPaymentId(int paymentId) {
        String sql = "SELECT * FROM facturas WHERE id_pago = ? ORDER BY id_factura LIMIT 1";
        Invoice invoice = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, paymentId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    invoice = createInvoice(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar la factura del pago", e);
        }
        return invoice;
    }

    @Override
    public List<Invoice> listAllInvoices() {
        String sql = "SELECT * FROM facturas ORDER BY id_factura DESC";
        List<Invoice> invoices = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                invoices.add(createInvoice(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar las facturas", e);
        }
        return invoices;
    }

    @Override
    public int nextInvoiceConsecutive() {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(numero_factura, '-', -1) AS UNSIGNED)), 0) + 1"
                + " FROM facturas";

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 1;
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("calcular el consecutivo de la factura", e);
        }
    }

    @Override
    public void updateInvoiceFile(int invoiceId, String fileName, String filePath) {
        String sql = "UPDATE facturas SET nombre_archivo_pdf = ?, ruta_archivo = ? WHERE id_factura = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, fileName);
            pst.setString(2, filePath);
            pst.setInt(3, invoiceId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("guardar la ruta del PDF de la factura", e);
        }
    }

    private Invoice createInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice(rs.getString("numero_factura"), rs.getInt("id_pago"),
                SqlUtils.getDate(rs, "fecha_emision"), rs.getString("cufe"));
        invoice.setId(rs.getInt("id_factura"));
        invoice.setPdfFileName(rs.getString("nombre_archivo_pdf"));
        invoice.setFilePath(rs.getString("ruta_archivo"));
        return invoice;
    }
}
