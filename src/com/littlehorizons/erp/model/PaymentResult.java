package com.littlehorizons.erp.model;

/**
 * Result of registering a payment: the payment is always saved, but the invoice
 * can fail (for example if the PDF cannot be written).
 */
public class PaymentResult {
    private Payment payment;
    private Invoice invoice;
    private String invoiceError;

    public PaymentResult(Payment payment, Invoice invoice, String invoiceError) {
        this.payment = payment;
        this.invoice = invoice;
        this.invoiceError = invoiceError;
    }

    public Payment getPayment() {
        return payment;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getInvoiceError() {
        return invoiceError;
    }

    public boolean hasInvoice() {
        return invoice != null;
    }
}
