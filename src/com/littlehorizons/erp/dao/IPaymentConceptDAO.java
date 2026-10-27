package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.PaymentConcept;

import java.util.List;

public interface IPaymentConceptDAO {
    void addConcept(PaymentConcept concept);

    List<PaymentConcept> listAllConcepts();

    PaymentConcept findConceptById(int id);

    boolean existsConceptCode(String code, int excludedId);

    boolean conceptHasPayments(int conceptId);

    void updateConcept(PaymentConcept concept);

    void deleteConcept(int id);
}
