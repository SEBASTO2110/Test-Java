package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.PaymentConceptController;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.math.BigDecimal;
import java.util.stream.Collectors;

/**
 * Payment concepts and per-student custom prices (scholarships and discounts).
 */
public class PaymentConceptView {

    private PaymentConceptController conceptController;
    private FinancePickers financePickers;
    private AcademicPickers pickers;

    public PaymentConceptView() {
        conceptController = new PaymentConceptController();
        financePickers = new FinancePickers();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Conceptos de pago y becas");
            System.out.println("""
                1. Registrar concepto de pago
                2. Listar conceptos de pago
                3. Actualizar concepto de pago
                4. Activar / desactivar concepto
                5. Eliminar concepto de pago
                6. Asignar valor personalizado a un estudiante (beca / descuento)
                7. Ver valores personalizados
                8. Quitar valor personalizado
                0. Volver""");
            option = InputReader.readMenuOption(8);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar concepto de pago");
                        registerConcept();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar conceptos de pago");
                        listConcepts();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Actualizar concepto de pago");
                        updateConcept();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Activar / desactivar concepto");
                        toggleConcept();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Eliminar concepto de pago");
                        deleteConcept();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Asignar valor personalizado a un estudiante (beca / descuento)");
                        setCustomPrice();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Ver valores personalizados");
                        listCustomPrices();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Quitar valor personalizado");
                        removeCustomPrice();
                        break;
                    case 0:
                        break;
                    default:
                        ConsoleUtils.printError("Opción inválida.");
                }
            } catch (RuntimeException e) {
                ConsoleUtils.printException(e);
            }
            if (option != 0) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private BigDecimal readAmount(String prompt, BigDecimal min) {
        return InputReader.readDecimal(prompt, min, PaymentConceptController.MAX_AMOUNT, 2);
    }

    private void registerConcept() {
        PaymentConcept concept = new PaymentConcept(
                InputReader.readText("Código (ej: SALIDA-MUSEO)", 20),
                InputReader.readText("Nombre", 100),
                InputReader.readOptionalText("Descripción", 1000),
                readAmount("Valor base en pesos", BigDecimal.ONE),
                InputReader.confirm("¿Es un pago recurrente (mensual)?"));
        conceptController.registerConcept(concept);
        int id = concept.getId();
        ConsoleUtils.printSuccess("Concepto " + concept.getCode() + " registrado con ID " + id + ".");
    }

    private void listConcepts() {
        ConsoleUtils.printTable(new String[]{"ID", "Código", "Concepto", "Valor base", "Recurrente", "Activo"},
                conceptController.listConcepts().stream()
                        .map(concept -> new String[]{String.valueOf(concept.getId()), concept.getCode(),
                                concept.getName(), Formats.money(concept.getBaseAmount()),
                                Formats.yesNo(concept.isRecurring()), Formats.yesNo(concept.isActive())})
                        .collect(Collectors.toList()));
    }

    private void updateConcept() {
        PaymentConcept concept = financePickers.pickConcept();
        ConsoleUtils.printInfo("Presione Enter para conservar el valor actual.");
        concept.setCode(InputReader.readTextOrKeep("Código", concept.getCode(), 20));
        concept.setName(InputReader.readTextOrKeep("Nombre", concept.getName(), 100));
        concept.setDescription(InputReader.readTextOrKeep("Descripción", concept.getDescription(), 1000));
        if (InputReader.confirm("Valor base: " + Formats.money(concept.getBaseAmount()) + ". ¿Desea cambiarlo?")) {
            concept.setBaseAmount(readAmount("Nuevo valor base", BigDecimal.ONE));
        }
        concept.setRecurring(InputReader.confirm("¿Es recurrente? (actual: " + Formats.yesNo(concept.isRecurring()) + ")"));
        conceptController.updateConcept(concept);
        ConsoleUtils.printSuccess("Concepto actualizado.");
    }

    private void toggleConcept() {
        PaymentConcept concept = financePickers.pickConcept();
        boolean activate = !concept.isActive();
        if (InputReader.confirm("¿Desea " + (activate ? "activar" : "desactivar") + " '" + concept.getName() + "'?")) {
            conceptController.changeActive(concept.getId(), activate);
            ConsoleUtils.printSuccess("Concepto " + (activate ? "activado" : "desactivado") + ".");
        }
    }

    private void deleteConcept() {
        PaymentConcept concept = financePickers.pickConcept();
        if (InputReader.confirm("¿Eliminar el concepto '" + concept.getName() + "'?")) {
            conceptController.deleteConcept(concept.getId());
            ConsoleUtils.printSuccess("Concepto eliminado.");
        }
    }

    private void setCustomPrice() {
        Student student = pickers.pickStudent();
        PaymentConcept concept = financePickers.pickConcept();
        BigDecimal amount = readAmount("Valor que pagará el estudiante (0 = beca completa)", BigDecimal.ZERO);
        String reason = InputReader.readText("Motivo (ej: Beca 50% excelencia, Descuento hermanos 10%)", 150);
        conceptController.setCustomPrice(student.getId(), concept.getId(), amount, reason);
        ConsoleUtils.printSuccess(student.getFullName() + " pagará " + Formats.money(amount) + " por "
                + concept.getName() + ".");
    }

    private void listCustomPrices() {
        ConsoleUtils.printTable(new String[]{"ID", "Estudiante", "Concepto", "Valor base", "Valor estudiante",
                        "Descuento", "Motivo"},
                conceptController.listCustomPrices().stream()
                        .map(price -> new String[]{String.valueOf(price.getId()), price.getStudentName(),
                                price.getConceptName(), Formats.money(price.getBaseAmount()),
                                Formats.money(price.getCustomAmount()), price.getDiscountPercentage() + "%",
                                price.getReason()})
                        .collect(Collectors.toList()));
    }

    private void removeCustomPrice() {
        listCustomPrices();
        conceptController.removeCustomPrice(InputReader.readId("ID del valor personalizado a quitar"));
        ConsoleUtils.printSuccess("Valor personalizado eliminado; el estudiante vuelve a pagar el valor base.");
    }
}
