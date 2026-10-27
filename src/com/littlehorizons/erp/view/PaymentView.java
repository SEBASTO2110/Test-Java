package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.InvoiceController;
import com.littlehorizons.erp.controller.PaymentConceptController;
import com.littlehorizons.erp.controller.PaymentController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.PaymentMethod;
import com.littlehorizons.erp.model.PaymentPeriod;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.PaymentResult;
import com.littlehorizons.erp.model.DelinquentStudent;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payments, electronic invoices and financial reports.
 */
public class PaymentView {

    private static final int RECENT_PAYMENTS = 25;

    private PaymentController paymentController;
    private InvoiceController invoiceController;
    private FinancePickers financePickers;
    private AcademicPickers pickers;

    public PaymentView() {
        paymentController = new PaymentController();
        invoiceController = new InvoiceController();
        financePickers = new FinancePickers();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Pagos y facturación");
            System.out.println("""
                1. Registrar pago (genera la factura electrónica PDF)
                2. Historial de pagos de un estudiante
                3. Historial de pagos de un acudiente (todos sus hijos)
                4. Reporte de morosos (más de un mes sin pagar)
                5. Listar facturas emitidas
                6. Generar de nuevo el PDF de la factura de un pago
                7. Resumen de recaudo por concepto y método de pago
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar pago (genera la factura electrónica PDF)");
                        registerPayment();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Historial de pagos de un estudiante");
                        showStudentHistory();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Historial de pagos de un acudiente (todos sus hijos)");
                        showGuardianHistory();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Reporte de morosos (más de un mes sin pagar)");
                        showDelinquencyReport();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Listar facturas emitidas");
                        listInvoices();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Generar de nuevo el PDF de la factura de un pago");
                        regenerateInvoice();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Resumen de recaudo por concepto y método de pago");
                        showCollectionSummary();
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

    private void registerPayment() {
        Student student = pickers.pickStudent();
        PaymentConcept concept = financePickers.pickConcept();

        Payment payment = new Payment();
        payment.setStudentId(student.getId());
        payment.setConceptId(concept.getId());
        payment.setGuardianId(readPayer(student.getId()));
        payment.setAmountPaid(readAmount(student.getId(), concept));
        payment.setPaymentDate(InputReader.readDateOrToday("Fecha de pago"));
        payment.setMethod(InputReader.readOption("Método de pago", PaymentMethod.values(), PaymentMethod::getLabel));
        payment.setAcademicYear(InputReader.readInt("Año académico", payment.getPaymentDate().getYear() - 1,
                payment.getPaymentDate().getYear() + 1));
        payment.setPeriod(InputReader.readOption("Período al que aplica", PaymentPeriod.values(), PaymentPeriod::getLabel));
        payment.setNotes(InputReader.readOptionalText("Notas", 500));

        ConsoleUtils.printInfo("Resumen: " + concept.getName() + " | " + student.getFullName() + " | "
                + Formats.money(payment.getAmountPaid()) + " | " + payment.getMethod().getLabel());
        if (!InputReader.confirm("¿Confirmar el pago?")) {
            ConsoleUtils.printWarning("Pago no registrado.");
            return;
        }
        PaymentResult result = paymentController.registerPayment(payment);
        ConsoleUtils.printSuccess("Pago #" + result.getPayment().getId() + " registrado. Recibo: "
                + result.getPayment().getReceiptNumber());
        if (result.hasInvoice()) {
            ConsoleUtils.printSuccess("Factura " + result.getInvoice().getInvoiceNumber() + " generada en:");
            ConsoleUtils.printInfo(result.getInvoice().getFilePath());
        } else {
            ConsoleUtils.printError("No se generó la factura: " + result.getInvoiceError());
            ConsoleUtils.printInfo("Puede intentarlo de nuevo con la opción 'Generar de nuevo el PDF'.");
        }
    }

    /**
     * Shows the student's guardians; Enter keeps the financially responsible one.
     */
    private Integer readPayer(int studentId) {
        List<Guardian> guardians = paymentController.guardiansOf(studentId);
        if (guardians.isEmpty()) {
            return null;
        }
        ConsoleUtils.printTable(new String[]{"ID", "Acudiente", "Parentesco", "Resp. financiero"},
                guardians.stream()
                        .map(guardian -> new String[]{String.valueOf(guardian.getId()), guardian.getFullName(),
                                guardian.getRelationship(), Formats.yesNo(guardian.isFinanciallyResponsible())})
                        .collect(Collectors.toList()));
        Guardian payer = paymentController.defaultPayer(guardians);
        if (guardians.size() == 1 || InputReader.confirm("¿Paga " + payer.getFullName() + "?")) {
            return payer.getId();
        }
        return InputReader.readId("ID del acudiente que paga");
    }

    /**
     * Suggests the amount that applies to the student (custom price or base amount).
     */
    private BigDecimal readAmount(int studentId, PaymentConcept concept) {
        BigDecimal suggested = paymentController.suggestAmount(studentId, concept.getId());
        if (suggested.compareTo(concept.getBaseAmount()) != 0) {
            ConsoleUtils.printInfo("El estudiante tiene un valor personalizado (base " + Formats.money(concept.getBaseAmount())
                    + ").");
        }
        if (suggested.signum() > 0 && InputReader.confirm("Valor a pagar: " + Formats.money(suggested) + ". ¿Es correcto?")) {
            return suggested;
        }
        return InputReader.readDecimal("Monto pagado", new BigDecimal("0.01"), PaymentConceptController.MAX_AMOUNT, 2);
    }

    private void printPayments(List<Payment> payments) {
        ConsoleUtils.printTable(new String[]{"ID", "Fecha", "Recibo", "Estudiante", "Concepto", "Período", "Método",
                        "Monto", "Pagó"},
                payments.stream()
                        .map(payment -> new String[]{String.valueOf(payment.getId()), Formats.date(payment.getPaymentDate()),
                                payment.getReceiptNumber(), payment.getStudentName(), payment.getConceptName(),
                                (payment.getPeriod() == null ? "" : payment.getPeriod().getLabel() + " ")
                                        + Formats.text(payment.getAcademicYear()),
                                payment.getMethod().getLabel(), Formats.money(payment.getAmountPaid()),
                                Formats.text(payment.getGuardianName())})
                        .collect(Collectors.toList()));
    }

    private void showStudentHistory() {
        Student student = pickers.pickStudent();
        List<Payment> payments = paymentController.studentHistory(student.getId());
        printPayments(payments);
        ConsoleUtils.printInfo("Total pagado: " + Formats.money(paymentController.total(payments)));
    }

    private void showGuardianHistory() {
        Guardian guardian = financePickers.pickGuardian();
        List<Payment> payments = paymentController.guardianHistory(guardian.getId());
        printPayments(payments);
        Map<String, BigDecimal> totals = paymentController.totalsByStudent(payments);
        totals.forEach((student, total) -> ConsoleUtils.printInfo("Total " + student + ": " + Formats.money(total)));
        ConsoleUtils.printInfo("TOTAL FAMILIA: " + Formats.money(paymentController.total(payments)));
    }

    /**
     * Also available from the reports menu.
     */
    public void showDelinquencyReport() {
        LocalDate today = LocalDate.now();
        ConsoleUtils.printInfo("Pagos recurrentes pendientes al " + today);
        List<DelinquentStudent> report = paymentController.delinquencyReport(today);
        ConsoleUtils.printTable(new String[]{"ID", "Estudiante", "Concepto", "Último pago", "Meses", "Deuda estimada",
                        "Acudiente", "Teléfono"},
                report.stream()
                        .map(row -> new String[]{String.valueOf(row.getStudentId()), row.getStudentName(), row.getConceptName(),
                                row.getLastPaymentDate() == null ? "Sin pagos" : Formats.date(row.getLastPaymentDate()),
                                String.valueOf(row.getPendingMonths()), Formats.money(row.getEstimatedDebt()),
                                Formats.text(row.getGuardianName()), Formats.text(row.getGuardianPhone())})
                        .collect(Collectors.toList()));
        BigDecimal totalDebt = report.stream()
                .map(DelinquentStudent::getEstimatedDebt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long students = report.stream().mapToInt(DelinquentStudent::getStudentId).distinct().count();
        ConsoleUtils.printInfo("Estudiantes en mora: " + students + " | Cartera estimada: " + Formats.money(totalDebt));
    }

    private void listInvoices() {
        List<Invoice> invoices = invoiceController.listInvoices();
        ConsoleUtils.printTable(new String[]{"Factura", "Pago", "Emisión", "Archivo", "CUFE"},
                invoices.stream()
                        .map(invoice -> new String[]{invoice.getInvoiceNumber(), String.valueOf(invoice.getPaymentId()),
                                Formats.date(invoice.getIssueDate()), Formats.text(invoice.getPdfFileName()),
                                invoice.getCufe().substring(0, 16) + "..."})
                        .collect(Collectors.toList()));
        ConsoleUtils.printInfo("Los PDF se guardan en la carpeta 'invoices' del proyecto.");
    }

    private void regenerateInvoice() {
        int paymentId = InputReader.readInt("ID del pago (0 = ver últimos pagos)", 0, Integer.MAX_VALUE);
        if (paymentId == 0) {
            printPayments(paymentController.listRecentPayments(RECENT_PAYMENTS));
            paymentId = InputReader.readId("ID del pago");
        }
        Invoice invoice = invoiceController.issueInvoice(paymentId);
        ConsoleUtils.printSuccess("Factura " + invoice.getInvoiceNumber() + " generada en:");
        ConsoleUtils.printInfo(invoice.getFilePath());
    }

    /**
     * Also available from the reports menu.
     */
    public void showCollectionSummary() {
        int year = InputReader.readInt("Año", 2000, LocalDate.now().getYear());
        Map<String, BigDecimal> byConcept = paymentController.collectionByConcept(year);
        ConsoleUtils.printHeader("Recaudo por concepto - " + year);
        ConsoleUtils.printTable(new String[]{"Concepto", "Total recaudado"},
                byConcept.entrySet().stream()
                        .map(entry -> new String[]{entry.getKey(), Formats.money(entry.getValue())})
                        .collect(Collectors.toList()));
        ConsoleUtils.printHeader("Recaudo por método de pago - " + year);
        ConsoleUtils.printTable(new String[]{"Método", "Total recaudado"},
                paymentController.collectionByMethod(year).entrySet().stream()
                        .map(entry -> new String[]{entry.getKey().getLabel(), Formats.money(entry.getValue())})
                        .collect(Collectors.toList()));
        BigDecimal total = byConcept.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        ConsoleUtils.printInfo("TOTAL RECAUDADO " + year + ": " + Formats.money(total));
    }
}
