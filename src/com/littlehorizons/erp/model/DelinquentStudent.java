package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Row of the delinquency report: a recurring concept unpaid for more than a month.
 */
public class DelinquentStudent {
    private int studentId;
    private String studentName;
    private String conceptName;
    private LocalDate lastPaymentDate;
    private long pendingMonths;
    private BigDecimal estimatedDebt;
    private String guardianName;
    private String guardianPhone;

    public DelinquentStudent(int studentId, String studentName, String conceptName, LocalDate lastPaymentDate, long pendingMonths, BigDecimal estimatedDebt, String guardianName, String guardianPhone) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.conceptName = conceptName;
        this.lastPaymentDate = lastPaymentDate;
        this.pendingMonths = pendingMonths;
        this.estimatedDebt = estimatedDebt;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public long getPendingMonths() {
        return pendingMonths;
    }

    public BigDecimal getEstimatedDebt() {
        return estimatedDebt;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }
}
