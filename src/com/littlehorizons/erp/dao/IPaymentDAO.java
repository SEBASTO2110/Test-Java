package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.RecurringPaymentStatus;

import java.util.List;

public interface IPaymentDAO {
    void addPayment(Payment payment);

    Payment findPaymentById(int id);

    List<Payment> findPaymentsByStudent(int studentId);

    /**
     * Payments of every student linked to the guardian (siblings included).
     */
    List<Payment> findPaymentsByGuardianStudents(int guardianId);

    List<Payment> listAllPayments();

    int nextReceiptSequence(int year);

    List<RecurringPaymentStatus> findRecurringPaymentStatus();
}
