package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.IInvoiceDAO;
import com.littlehorizons.erp.dao.IPaymentDAO;
import com.littlehorizons.erp.dao.InvoiceDaoImpl;
import com.littlehorizons.erp.dao.PaymentDaoImpl;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.InvoiceDocument;
import com.littlehorizons.erp.model.InvoiceLine;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.utils.CufeGenerator;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.InvoicePdfGenerator;
import com.littlehorizons.erp.utils.SchoolInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Issues the electronic invoice of a payment: consecutive number, CUFE, VAT and PDF file.
 */
public class InvoiceController {
    /** Colombian VAT rate. */
    public static final BigDecimal VAT_RATE = new BigDecimal("0.19");
    /**
     * Educational services are excluded from VAT (Art. 476 E.T.); only goods such as the
     * uniform are taxed. The amount paid already includes the VAT.
     */
    public static final List<String> TAXABLE_CONCEPT_CODES = List.of("UNIFORME");

    private IInvoiceDAO invoiceDAO;
    private IPaymentDAO paymentDAO;
    private StudentController studentController;
    private GuardianController guardianController;

    public InvoiceController() {
        invoiceDAO = new InvoiceDaoImpl();
        paymentDAO = new PaymentDaoImpl();
        studentController = new StudentController();
        guardianController = new GuardianController();
    }

    public List<Invoice> listInvoices() {
        return invoiceDAO.listAllInvoices();
    }

    /**
     * Creates the invoice of a payment and writes its PDF. If the payment already has an
     * invoice, the same number and CUFE are kept and only the PDF is generated again.
     */
    public Invoice issueInvoice(int paymentId) {
        Payment payment = paymentDAO.findPaymentById(paymentId);
        if (payment == null) {
            throw new RuntimeException("No existe un pago con ID " + paymentId + ".");
        }
        Guardian customer = findCustomer(payment);
        Invoice invoice = invoiceDAO.findInvoiceByPaymentId(paymentId);
        if (invoice == null) {
            invoice = createInvoiceRecord(payment);
        }
        String fileName = InvoicePdfGenerator.fileNameFor(consecutiveOf(invoice.getInvoiceNumber()));
        try {
            Path file = InvoicePdfGenerator.generate(buildDocument(invoice, payment, customer), fileName);
            invoice.setPdfFileName(fileName);
            invoice.setFilePath(file.toString());
            invoiceDAO.updateInvoiceFile(invoice.getId(), fileName, file.toString());
            return invoice;
        } catch (IOException e) {
            ErrorLogger.log("Invoice PDF generation failed for payment " + paymentId, e);
            throw new RuntimeException("La factura " + invoice.getInvoiceNumber()
                    + " quedó registrada pero no se pudo generar el PDF: " + e.getMessage());
        }
    }

    private Invoice createInvoiceRecord(Payment payment) {
        String number = String.format("%s-%06d", SchoolInfo.INVOICE_PREFIX, invoiceDAO.nextInvoiceConsecutive());
        LocalDate issueDate = LocalDate.now();
        Invoice invoice = new Invoice(number, payment.getId(), issueDate,
                CufeGenerator.generate(number, SchoolInfo.NIT_NUMBER, issueDate));
        invoiceDAO.addInvoice(invoice);
        return invoice;
    }

    private int consecutiveOf(String invoiceNumber) {
        return Integer.parseInt(invoiceNumber.substring(invoiceNumber.lastIndexOf('-') + 1));
    }

    /**
     * The guardian who paid; if it was not saved, the financially responsible guardian of the student.
     */
    private Guardian findCustomer(Payment payment) {
        if (payment.getGuardianId() != null) {
            return guardianController.findGuardian(payment.getGuardianId());
        }
        List<Guardian> guardians = studentController.getGuardians(payment.getStudentId());
        if (guardians.isEmpty()) {
            throw new RuntimeException("El estudiante no tiene acudiente registrado para facturar.");
        }
        for (Guardian guardian : guardians) {
            if (guardian.isFinanciallyResponsible()) {
                return guardian;
            }
        }
        return guardians.get(0);
    }

    private InvoiceDocument buildDocument(Invoice invoice, Payment payment, Guardian customer) {
        BigDecimal total = payment.getAmountPaid().setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxRate = TAXABLE_CONCEPT_CODES.contains(payment.getConceptCode()) ? VAT_RATE : BigDecimal.ZERO;
        BigDecimal subtotal = total.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP);
        BigDecimal tax = total.subtract(subtotal);

        String description = payment.getConceptName();
        if (payment.getPeriod() != null) {
            description += " - Período " + payment.getPeriod().getLabel();
        }
        if (payment.getAcademicYear() != null) {
            description += " " + payment.getAcademicYear();
        }
        String documentType = customer.getDocumentType() == null ? "" : customer.getDocumentType().getDbValue();

        return new InvoiceDocument(
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getCufe(),
                customer.getFullName(),
                documentType,
                customer.getDocumentNumber(),
                customer.getAddress(),
                customer.getPrimaryPhone(),
                customer.getEmail(),
                payment.getStudentName(),
                payment.getMethod().getLabel(),
                payment.getReceiptNumber(),
                List.of(new InvoiceLine(description, 1, subtotal, subtotal)),
                subtotal,
                taxRate,
                tax,
                total);
    }
}
