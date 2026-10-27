package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Custom amount of a payment concept for a specific student, used for
 * scholarships and discounts (table precios_concepto_estudiante).
 */
public class StudentConceptPrice {

    private int id;
    private int studentId;
    private String studentName;
    private int conceptId;
    private String conceptName;
    private BigDecimal baseAmount;
    private BigDecimal customAmount;
    private String reason;

    public StudentConceptPrice() {
    }

    public StudentConceptPrice(int studentId, int conceptId, BigDecimal customAmount, String reason) {
        this.studentId = studentId;
        this.conceptId = conceptId;
        this.customAmount = customAmount;
        this.reason = reason;
    }

    /** Discount over the base amount, e.g. 25.0 for a 25 % scholarship. */
    public BigDecimal getDiscountPercentage() {
        if (baseAmount == null || baseAmount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return baseAmount.subtract(customAmount)
                .multiply(BigDecimal.valueOf(100))
                .divide(baseAmount, 1, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getCustomAmount() {
        return customAmount;
    }

    public void setCustomAmount(BigDecimal customAmount) {
        this.customAmount = customAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
