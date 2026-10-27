package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AttendanceController;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.AttendanceStatus;
import com.littlehorizons.erp.model.AbsenceAlert;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AttendanceView {

    private static final List<String> STATUS_CODES = Arrays.stream(AttendanceStatus.values())
            .map(AttendanceStatus::getShortCode)
            .collect(Collectors.toList());

    private AttendanceController attendanceController;
    private AcademicPickers pickers;

    public AttendanceView() {
        attendanceController = new AttendanceController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Control de asistencia");
            System.out.println("""
                1. Tomar asistencia de una clase
                2. Consultar asistencia de una clase en una fecha
                3. Asistencia de un estudiante en un rango de fechas
                4. Reporte de inasistencias de la semana
                0. Volver""");
            option = InputReader.readMenuOption(4);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Tomar asistencia de una clase");
                        takeAttendance();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Consultar asistencia de una clase en una fecha");
                        showClassAttendance();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Asistencia de un estudiante en un rango de fechas");
                        showStudentAttendance();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Reporte de inasistencias de la semana");
                        showWeeklyAlerts(attendanceController, null);
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

    private void takeAttendance() {
        Subject subject = pickers.pickSubject();
        Teacher teacher = pickers.pickTeacher();
        LocalDate date = InputReader.readDateOrToday("Fecha de la clase");
        attendanceController.validateAttendanceDate(date);
        AcademicPeriod period = pickers.readTerm();
        List<SubjectEnrollment> students = attendanceController.classList(subject.getId(), date.getYear(), period);

        ConsoleUtils.printInfo("Códigos: P = Presente, A = Ausente, T = Tardanza, E = Excusa médica");
        List<Attendance> records = new ArrayList<>();
        for (SubjectEnrollment enrollment : students) {
            String code = InputReader.readCode(enrollment.getStudentName(), STATUS_CODES, "P");
            Attendance attendance = new Attendance();
            attendance.setStudentId(enrollment.getStudentId());
            attendance.setStatus(AttendanceStatus.fromShortCode(code));
            if (attendance.getStatus() != AttendanceStatus.PRESENT) {
                attendance.setObservations(InputReader.readOptionalText("  Observación", 300));
            }
            records.add(attendance);
        }
        int saved = attendanceController.registerAttendance(subject.getId(), teacher.getId(), date, records);
        ConsoleUtils.printSuccess("Asistencia guardada para " + saved + " estudiante(s).");
        printSummary(attendanceController.summarize(records));
    }

    private void showClassAttendance() {
        Subject subject = pickers.pickSubject();
        LocalDate date = InputReader.readDateOrToday("Fecha de la clase");
        List<Attendance> records = attendanceController.attendanceOfClass(subject.getId(), date);
        ConsoleUtils.printTable(new String[]{"Estudiante", "Estado", "Observaciones"}, records.stream()
                .map(record -> new String[]{record.getStudentName(), record.getStatus().getLabel(),
                        Formats.text(record.getObservations())})
                .collect(Collectors.toList()));
        if (!records.isEmpty()) {
            printSummary(attendanceController.summarize(records));
        }
    }

    private void showStudentAttendance() {
        Student student = pickers.pickStudent();
        LocalDate from = InputReader.readDate("Desde");
        LocalDate to = InputReader.readDateOrToday("Hasta");
        List<Attendance> records = attendanceController.attendanceOfStudent(student.getId(), from, to);
        ConsoleUtils.printTable(new String[]{"Fecha", "Materia", "Estado", "Observaciones"}, records.stream()
                .map(record -> new String[]{Formats.date(record.getDate()), record.getSubjectName(),
                        record.getStatus().getLabel(), Formats.text(record.getObservations())})
                .collect(Collectors.toList()));
        if (!records.isEmpty()) {
            printSummary(attendanceController.summarize(records));
        }
    }

    /**
     * Also used by the reports menu. If day is null the user is asked for it.
     */
    public static void showWeeklyAlerts(AttendanceController controller, LocalDate day) {
        LocalDate reference = day != null ? day : InputReader.readDateOrToday("Cualquier día de la semana a revisar");
        List<AbsenceAlert> alerts = controller.weeklyAbsenceAlerts(reference);
        ConsoleUtils.printInfo("Estudiantes con más de " + AttendanceController.MAX_WEEKLY_ABSENCES
                + " ausencias en la semana del " + reference + ":");
        ConsoleUtils.printTable(new String[]{"ID", "Estudiante", "Ausencias", "Materias"}, alerts.stream()
                .map(alert -> new String[]{String.valueOf(alert.getStudentId()), alert.getStudentName(),
                        String.valueOf(alert.getAbsences()), String.join(", ", alert.getSubjects())})
                .collect(Collectors.toList()));
    }

    private void printSummary(Map<AttendanceStatus, Long> summary) {
        ConsoleUtils.printInfo("Resumen: " + summary.entrySet().stream()
                .map(entry -> entry.getKey().getLabel() + ": " + entry.getValue())
                .collect(Collectors.joining(" | ")));
    }
}
