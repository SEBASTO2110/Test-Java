package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.IPaymentConceptDAO;
import com.littlehorizons.erp.dao.IStudentConceptPriceDAO;
import com.littlehorizons.erp.dao.PaymentConceptDaoImpl;
import com.littlehorizons.erp.dao.StudentConceptPriceDaoImpl;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.StudentConceptPrice;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business rules of the payment concepts and the custom price of each student (scholarships and discounts).
 */
public class PaymentConceptController {
    /** Largest value allowed by DECIMAL(10,2). */
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("99999999.99");

    private IPaymentConceptDAO conceptDAO;
    private IStudentConceptPriceDAO priceDAO;
    private StudentController studentController;

    public PaymentConceptController() {
        conceptDAO = new PaymentConceptDaoImpl();
        priceDAO = new StudentConceptPriceDaoImpl();
        studentController = new StudentController();
    }

    public List<PaymentConcept> listConcepts() {
        return conceptDAO.listAllConcepts();
    }

    public List<PaymentConcept> listActiveConcepts() {
        return conceptDAO.listAllConcepts().stream()
                .filter(PaymentConcept::isActive)
                .collect(Collectors.toList());
    }

    public PaymentConcept findConcept(int conceptId) {
        PaymentConcept concept = conceptDAO.findConceptById(conceptId);
        if (concept == null) {
            throw new RuntimeException("No existe un concepto de pago con ID " + conceptId + ".");
        }
        return concept;
    }

    public void registerConcept(PaymentConcept concept) {
        validateConcept(concept, 0);
        conceptDAO.addConcept(concept);
    }

    public void updateConcept(PaymentConcept concept) {
        findConcept(concept.getId());
        validateConcept(concept, concept.getId());
        conceptDAO.updateConcept(concept);
    }

    public void changeActive(int conceptId, boolean active) {
        PaymentConcept concept = findConcept(conceptId);
        concept.setActive(active);
        conceptDAO.updateConcept(concept);
    }

    /**
     * A concept with payments cannot be deleted (the payments use it); it can be deactivated.
     */
    public void deleteConcept(int conceptId) {
        findConcept(conceptId);
        if (conceptDAO.conceptHasPayments(conceptId)) {
            throw new RuntimeException("El concepto tiene pagos registrados; desactívelo en lugar de eliminarlo.");
        }
        conceptDAO.deleteConcept(conceptId);
    }

    private void validateConcept(PaymentConcept concept, int excludedId) {
        if (concept.getCode() == null || !concept.getCode().matches("[A-Za-z0-9_-]{2,20}")) {
            throw new RuntimeException("El código debe tener de 2 a 20 caracteres (letras, números, - o _).");
        }
        concept.setCode(concept.getCode().toUpperCase());
        if (conceptDAO.existsConceptCode(concept.getCode(), excludedId)) {
            throw new RuntimeException("Ya existe un concepto con el código " + concept.getCode() + ".");
        }
        if (concept.getName() == null || concept.getName().isBlank()) {
            throw new RuntimeException("El nombre del concepto es obligatorio.");
        }
        if (concept.getBaseAmount() == null || concept.getBaseAmount().signum() <= 0
                || concept.getBaseAmount().compareTo(MAX_AMOUNT) > 0) {
            throw new RuntimeException("El valor base debe ser mayor que cero y menor a " + MAX_AMOUNT + ".");
        }
    }

    /**
     * Sets the amount a student pays for a concept. It cannot be higher than the base amount
     * because it is a scholarship or a discount ($ 0 = full scholarship).
     */
    public void setCustomPrice(int studentId, int conceptId, BigDecimal customAmount, String reason) {
        studentController.findActiveStudent(studentId);
        PaymentConcept concept = findConcept(conceptId);
        if (customAmount.signum() < 0 || customAmount.compareTo(concept.getBaseAmount()) > 0) {
            throw new RuntimeException("El valor personalizado debe estar entre $ 0 y el valor base del concepto.");
        }
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Indique el motivo (beca, descuento por hermanos, convenio...).");
        }
        priceDAO.saveCustomPrice(new StudentConceptPrice(studentId, conceptId, customAmount, reason));
    }

    public List<StudentConceptPrice> listCustomPrices() {
        return priceDAO.listAllCustomPrices();
    }

    public void removeCustomPrice(int priceId) {
        if (!priceDAO.deleteCustomPrice(priceId)) {
            throw new RuntimeException("No existe un valor personalizado con ID " + priceId + ".");
        }
    }

    /**
     * Amount the student must pay: the custom price if there is one, otherwise the base amount.
     */
    public BigDecimal applicableAmount(int studentId, int conceptId) {
        StudentConceptPrice price = priceDAO.findCustomPrice(studentId, conceptId);
        if (price != null) {
            return price.getCustomAmount();
        }
        return findConcept(conceptId).getBaseAmount();
    }
}
