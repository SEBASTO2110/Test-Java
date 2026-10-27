package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;

import java.util.List;

public interface IInvoiceDAO {
    void addInvoice(Invoice invoice);

    Invoice findInvoiceByPaymentId(int paymentId);

    List<Invoice> listAllInvoices();

    int nextInvoiceConsecutive();

    void updateInvoiceFile(int invoiceId, String fileName, String filePath);
}
