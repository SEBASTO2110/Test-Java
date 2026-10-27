package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * One row of the invoice detail.
 */
public class InvoiceLine {
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public InvoiceLine(String description, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
