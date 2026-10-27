package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.IPaymentDAO;
import com.littlehorizons.erp.dao.PaymentDaoImpl;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.PaymentMethod;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.PaymentResult;
import com.littlehorizons.erp.model.DelinquentStudent;
import com.littlehorizons.erp.model.RecurringPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Business rules of payments, payment history, delinquency and collection reports.
 */
public class PaymentController {
    /** Monthly tuition: every active student must pay it. */
    public static final String TUITION_CONCEPT_CODE = "PENSION";
    /** Calendar A schools in Colombia start the academic year in February. */
    public static final Month ACADEMIC_YEAR_START = Month.FEBRUARY;

    private IPaymentDAO paymentDAO;
    private PaymentConceptController conceptController;
    private InvoiceController invoiceController;
    private StudentController studentController;
    private GuardianController guardianController;

    public PaymentController() {
        paymentDAO = new PaymentDaoImpl();
        conceptController = new PaymentConceptController();
        invoiceController = new InvoiceController();
        studentController = new StudentController();
        guardianController = new GuardianController();
    }

    /**
     * Suggested amount for the payment (custom price or base amount).
     */
    public BigDecimal suggestAmount(int studentId, int conceptId) {
        return conceptController.applicableAmount(studentId, conceptId);
    }

    public List<Guardian> guardiansOf(int studentId) {
        return studentController.getGuardians(studentId);
    }

    /**
     * Default payer: the financially responsible guardian, otherwise the first one.
     */
    public Guardian defaultPayer(List<Guardian> guardians) {
        for (Guardian guardian : guardians) {
            if (guardian.isFinanciallyResponsible()) {
                return guardian;
            }
        }
        return guardians.get(0);
    }

    /**
     * Validates and saves the payment, gives it a receipt number and issues the electronic invoice.
     */
    public PaymentResult registerPayment(Payment payment) {
        Student student = studentController.findActiveStudent(payment.getStudentId());
        PaymentConcept concept = conceptController.findConcept(payment.getConceptId());
        if (!concept.isActive()) {
            throw new RuntimeException("El concepto '" + concept.getName() + "' está inactivo.");
        }
        if (payment.getAmountPaid() == null || payment.getAmountPaid().signum() <= 0
                || payment.getAmountPaid().compareTo(PaymentConceptController.MAX_AMOUNT) > 0) {
            throw new RuntimeException("El monto pagado debe ser mayor que cero.");
        }
        if (payment.getPaymentDate() == null || payment.getPaymentDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de pago no puede ser futura.");
        }
        if (payment.getMethod() == null) {
            throw new RuntimeException("Debe indicar el método de pago.");
        }
        payment.setGuardianId(findPayer(payment));
        int year = payment.getPaymentDate().getYear();
        payment.setReceiptNumber(String.format("REC-%d-%06d", year, paymentDAO.nextReceiptSequence(year)));
        paymentDAO.addPayment(payment);
        payment.setStudentName(student.getFullName());
        payment.setConceptName(concept.getName());
        payment.setConceptCode(concept.getCode());
        try {
            return new PaymentResult(payment, invoiceController.issueInvoice(payment.getId()), null);
        } catch (RuntimeException e) {
            // The payment is already saved; the user can generate the invoice again later.
            return new PaymentResult(payment, null, e.getMessage());
        }
    }

    /**
     * The payer must be one of the student's guardians; by default the financially responsible one.
     */
    private Integer findPayer(Payment payment) {
        List<Guardian> guardians = guardiansOf(payment.getStudentId());
        if (guardians.isEmpty()) {
            throw new RuntimeException("El estudiante no tiene acudientes registrados; no se puede facturar el pago.");
        }
        if (payment.getGuardianId() == null) {
            return defaultPayer(guardians).getId();
        }
        boolean linked = guardians.stream().anyMatch(guardian -> guardian.getId() == payment.getGuardianId());
        if (!linked) {
            throw new RuntimeException("El acudiente " + payment.getGuardianId() + " no está asociado a este estudiante.");
        }
        return payment.getGuardianId();
    }

    public List<Payment> listRecentPayments(int limit) {
        return paymentDAO.listAllPayments().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Payment> studentHistory(int studentId) {
        studentController.findStudent(studentId);
        return paymentDAO.findPaymentsByStudent(studentId);
    }

    /**
     * Payments of every student in charge of the guardian (siblings included).
     */
    public List<Payment> guardianHistory(int guardianId) {
        guardianController.findGuardian(guardianId);
        return paymentDAO.findPaymentsByGuardianStudents(guardianId);
    }

    public BigDecimal total(List<Payment> payments) {
        return payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, BigDecimal> totalsByStudent(List<Payment> payments) {
        return payments.stream()
                .collect(Collectors.groupingBy(Payment::getStudentName, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountPaid, BigDecimal::add)));
    }

    /**
     * Students with a recurring concept unpaid for more than a month. The monthly tuition applies
     * to every active student; other recurring services (transport, food) only to the students who
     * paid them at least once. Students with a full scholarship ($ 0) are skipped.
     */
    public List<DelinquentStudent> delinquencyReport(LocalDate today) {
        LocalDate limit = today.minusMonths(1);
        LocalDate yearStart = LocalDate.of(today.getYear(), ACADEMIC_YEAR_START, 1);

        List<DelinquentStudent> report = new ArrayList<>();
        for (RecurringPaymentStatus row : paymentDAO.findRecurringPaymentStatus()) {
            boolean hasToPay = row.getMonthlyAmount().signum() > 0
                    && (TUITION_CONCEPT_CODE.equals(row.getConceptCode()) || row.getLastPaymentDate() != null);
            boolean isLate = row.getLastPaymentDate() == null || row.getLastPaymentDate().isBefore(limit);
            if (hasToPay && isLate) {
                DelinquentStudent delinquent = toDelinquent(row, today, yearStart);
                if (delinquent.getPendingMonths() > 0) {
                    report.add(delinquent);
                }
            }
        }
        report.sort(Comparator.comparingLong(DelinquentStudent::getPendingMonths).reversed()
                .thenComparing(DelinquentStudent::getStudentName));
        return report;
    }

    private DelinquentStudent toDelinquent(RecurringPaymentStatus row, LocalDate today, LocalDate yearStart) {
        LocalDate from;
        if (row.getLastPaymentDate() == null || row.getLastPaymentDate().isBefore(yearStart)) {
            from = yearStart.minusMonths(1);
        } else {
            from = row.getLastPaymentDate();
        }
        long pendingMonths = Math.max(0, ChronoUnit.MONTHS.between(from.withDayOfMonth(1), today.withDayOfMonth(1)));
        return new DelinquentStudent(row.getStudentId(), row.getStudentName(), row.getConceptName(),
                row.getLastPaymentDate(), pendingMonths, row.getMonthlyAmount().multiply(BigDecimal.valueOf(pendingMonths)),
                row.getGuardianName(), row.getGuardianPhone());
    }

    public Map<String, BigDecimal> collectionByConcept(int year) {
        return paymentsOfYear(year).stream()
                .collect(Collectors.groupingBy(Payment::getConceptName, TreeMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountPaid, BigDecimal::add)));
    }

    public Map<PaymentMethod, BigDecimal> collectionByMethod(int year) {
        Map<PaymentMethod, BigDecimal> result = new EnumMap<>(PaymentMethod.class);
        for (Payment payment : paymentsOfYear(year)) {
            BigDecimal current = result.getOrDefault(payment.getMethod(), BigDecimal.ZERO);
            result.put(payment.getMethod(), current.add(payment.getAmountPaid()));
        }
        return result;
    }

    private List<Payment> paymentsOfYear(int year) {
        return paymentDAO.listAllPayments().stream()
                .filter(payment -> payment.getPaymentDate().getYear() == year)
                .collect(Collectors.toList());
    }
}
