package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.GuardianController;
import com.littlehorizons.erp.controller.PaymentConceptController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.stream.Collectors;

/**
 * Prompts to choose a payment concept or a guardian. Users can type the ID or 0 to see the list first.
 */
public class FinancePickers {

    private PaymentConceptController conceptController;
    private GuardianController guardianController;

    public FinancePickers() {
        conceptController = new PaymentConceptController();
        guardianController = new GuardianController();
    }

    public PaymentConcept pickConcept() {
        int id = InputReader.readInt("ID del concepto (0 = ver lista)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            ConsoleUtils.printTable(new String[]{"ID", "Código", "Concepto", "Valor base", "Activo"},
                    conceptController.listConcepts().stream()
                            .map(concept -> new String[]{String.valueOf(concept.getId()), concept.getCode(),
                                    concept.getName(), Formats.money(concept.getBaseAmount()),
                                    Formats.yesNo(concept.isActive())})
                            .collect(Collectors.toList()));
            id = InputReader.readId("ID del concepto");
        }
        PaymentConcept concept = conceptController.findConcept(id);
        ConsoleUtils.printInfo("» " + concept.getName() + " - " + Formats.money(concept.getBaseAmount()));
        return concept;
    }

    public Guardian pickGuardian() {
        int id = InputReader.readInt("ID del acudiente (0 = ver lista)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            ConsoleUtils.printTable(new String[]{"ID", "Documento", "Acudiente", "Parentesco"},
                    guardianController.listGuardians().stream()
                            .map(guardian -> new String[]{String.valueOf(guardian.getId()), guardian.getDocument(),
                                    guardian.getFullName(), guardian.getRelationship()})
                            .collect(Collectors.toList()));
            id = InputReader.readId("ID del acudiente");
        }
        Guardian guardian = guardianController.findGuardian(id);
        ConsoleUtils.printInfo("» " + guardian);
        return guardian;
    }
}
