package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Last payment of a recurring concept (e.g. monthly tuition) of an active student.
 */
public class RecurringPaymentStatus {
    private int studentId;
    private String studentName;
    private String conceptCode;
    private String conceptName;
    private BigDecimal monthlyAmount;
    private LocalDate lastPaymentDate;
    private String guardianName;
    private String guardianPhone;

    public RecurringPaymentStatus(int studentId, String studentName, String conceptCode, String conceptName, BigDecimal monthlyAmount, LocalDate lastPaymentDate, String guardianName, String guardianPhone) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.conceptCode = conceptCode;
        this.conceptName = conceptName;
        this.monthlyAmount = monthlyAmount;
        this.lastPaymentDate = lastPaymentDate;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public String getConceptName() {
        return conceptName;
    }

    public BigDecimal getMonthlyAmount() {
        return monthlyAmount;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }
}
