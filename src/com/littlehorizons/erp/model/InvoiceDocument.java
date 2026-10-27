package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything printed on the invoice PDF, already calculated.
 */
public class InvoiceDocument {
    private String invoiceNumber;
    private LocalDate issueDate;
    private String cufe;
    private String customerName;
    private String customerDocumentType;
    private String customerDocumentNumber;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;
    private String studentName;
    private String paymentMethod;
    private String receiptNumber;
    private List<InvoiceLine> lines;
    private BigDecimal subtotal;
    private BigDecimal taxRate;
    private BigDecimal tax;
    private BigDecimal total;

    public InvoiceDocument(String invoiceNumber, LocalDate issueDate, String cufe, String customerName, String customerDocumentType, String customerDocumentNumber, String customerAddress, String customerPhone, String customerEmail, String studentName, String paymentMethod, String receiptNumber, List<InvoiceLine> lines, BigDecimal subtotal, BigDecimal taxRate, BigDecimal tax, BigDecimal total) {
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.customerName = customerName;
        this.customerDocumentType = customerDocumentType;
        this.customerDocumentNumber = customerDocumentNumber;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.studentName = studentName;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
        this.lines = lines;
        this.subtotal = subtotal;
        this.taxRate = taxRate;
        this.tax = tax;
        this.total = total;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public String getCufe() {
        return cufe;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerDocumentType() {
        return customerDocumentType;
    }

    public String getCustomerDocumentNumber() {
        return customerDocumentNumber;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
