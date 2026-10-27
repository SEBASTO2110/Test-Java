package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AttendanceController;
import com.littlehorizons.erp.controller.EnrollmentController;
import com.littlehorizons.erp.controller.GradeBookController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.StudentAverage;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;
import com.littlehorizons.erp.utils.Formats;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Menu 7: reports and statistics of every module.
 */
public class ReportView {

    private static final int RANKING_SIZE = 10;

    private StudentController studentController;
    private EnrollmentController enrollmentController;
    private GradeBookController gradeBookController;
    private AttendanceController attendanceController;
    private AcademicPickers pickers;
    private ClubView clubView;
    private EventView eventView;
    private PaymentView paymentView;
    private LoanView loanView;
    private ResourceView resourceView;

    public ReportView() {
        studentController = new StudentController();
        enrollmentController = new EnrollmentController();
        gradeBookController = new GradeBookController();
        attendanceController = new AttendanceController();
        pickers = new AcademicPickers();
        clubView = new ClubView();
        eventView = new EventView();
        paymentView = new PaymentView();
        loanView = new LoanView();
        resourceView = new ResourceView();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Reportes y Estadísticas");
            System.out.println("""
                1. Estudiantes con inasistencias preocupantes (semana)
                2. Estudiantes que necesitan apoyo académico (promedio < 3.0)
                3. Mejores promedios de un curso
                4. Promedio general del curso en cada materia
                5. Cantidad de estudiantes por grado
                6. Cantidad de estudiantes por estado
                7. Estudiantes activos sin acudiente
                8. Ocupación de clubes
                9. Participación en eventos por tipo
                10. Ranking de estudiantes en competencias
                11. Estudiantes morosos (más de un mes sin pagar)
                12. Resumen de recaudo por concepto y método
                13. Préstamos vencidos
                14. Resumen del inventario de recursos
                0. Volver""");
            option = InputReader.readMenuOption(14);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Estudiantes con inasistencias preocupantes (semana)");
                        weeklyAbsences();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Estudiantes que necesitan apoyo académico (promedio < 3.0)");
                        studentsAtRisk();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Mejores promedios de un curso");
                        courseRanking();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Promedio general del curso en cada materia");
                        courseSubjectAverages();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Cantidad de estudiantes por grado");
                        studentsByGrade();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Cantidad de estudiantes por estado");
                        studentsByStatus();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Estudiantes activos sin acudiente");
                        studentsWithoutGuardian();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Ocupación de clubes");
                        clubView.showOccupancyReport();
                        break;
                    case 9:
                        ConsoleUtils.printActionHeader("Participación en eventos por tipo");
                        eventView.showParticipationByType();
                        break;
                    case 10:
                        ConsoleUtils.printActionHeader("Ranking de estudiantes en competencias");
                        eventView.showRanking();
                        break;
                    case 11:
                        ConsoleUtils.printActionHeader("Estudiantes morosos (más de un mes sin pagar)");
                        paymentView.showDelinquencyReport();
                        break;
                    case 12:
                        ConsoleUtils.printActionHeader("Resumen de recaudo por concepto y método");
                        paymentView.showCollectionSummary();
                        break;
                    case 13:
                        ConsoleUtils.printActionHeader("Préstamos vencidos");
                        loanView.showOverdueReport();
                        break;
                    case 14:
                        ConsoleUtils.printActionHeader("Resumen del inventario de recursos");
                        resourceView.showSummary();
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

    private void weeklyAbsences() {
        AttendanceView.showWeeklyAlerts(attendanceController, null);
    }

    private void studentsAtRisk() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readOptionalTerm();
        printStudentAverages(gradeBookController.studentsAtRisk(grade.getId(), year, period));
    }

    private void courseRanking() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readOptionalTerm();
        printStudentAverages(gradeBookController.studentAveragesOfCourse(grade.getId(), year, period).stream()
                .limit(RANKING_SIZE)
                .collect(Collectors.toList()));
    }

    private void courseSubjectAverages() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readOptionalTerm();
        ConsoleUtils.printTable(new String[]{"Materia", "Promedio del curso", "Notas"},
                gradeBookController.courseSubjectAverages(grade.getId(), year, period).stream()
                        .map(average -> new String[]{average.getSubjectName(), Formats.average(average.getAverage()),
                                String.valueOf(average.getGradesCount())})
                        .collect(Collectors.toList()));
    }

    private void studentsByGrade() {
        int year = pickers.readAcademicYear();
        Map<String, Long> counts = enrollmentController.countStudentsByGrade(year);
        printCounts("Grado", counts);
    }

    private void studentsByStatus() {
        printCounts("Estado", studentController.countByStatus());
    }

    private void studentsWithoutGuardian() {
        ConsoleUtils.printTable(new String[]{"ID", "Documento", "Nombre"},
                studentController.studentsWithoutGuardian().stream()
                        .map(student -> new String[]{String.valueOf(student.getId()), student.getDocument(),
                                student.getFullName()})
                        .collect(Collectors.toList()));
    }

    private void printCounts(String label, Map<String, Long> counts) {
        ConsoleUtils.printTable(new String[]{label, "Estudiantes"}, counts.entrySet().stream()
                .map(entry -> new String[]{entry.getKey(), String.valueOf(entry.getValue())})
                .collect(Collectors.toList()));
        ConsoleUtils.printInfo("Total de estudiantes: " + counts.values().stream().mapToLong(Long::longValue).sum());
    }

    private void printStudentAverages(List<StudentAverage> averages) {
        ConsoleUtils.printTable(new String[]{"#", "ID", "Estudiante", "Promedio", "Notas"},
                IntStream.range(0, averages.size())
                        .mapToObj(index -> {
                            StudentAverage average = averages.get(index);
                            return new String[]{String.valueOf(index + 1), String.valueOf(average.getStudentId()),
                                    average.getStudentName(), Formats.average(average.getAverage()),
                                    String.valueOf(average.getGradesCount())};
                        })
                        .collect(Collectors.toList()));
    }
}
