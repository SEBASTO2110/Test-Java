package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.GradeBookController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Grade;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.SubjectAverage;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class GradeBookView {

    private GradeBookController gradeBookController;
    private AcademicPickers pickers;

    public GradeBookView() {
        gradeBookController = new GradeBookController();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Calificaciones y promedios");
            System.out.println("""
                1. Registrar o modificar una nota
                2. Registrar notas de todo un curso en una materia
                3. Ver notas de una materia por período
                4. Promedios de un estudiante (por materia y por período)
                5. Promedio general del curso en cada materia
                6. Historial académico de un estudiante
                7. Eliminar una nota
                0. Volver""");
            option = InputReader.readMenuOption(7);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar o modificar una nota");
                        registerGrade();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Registrar notas de todo un curso en una materia");
                        registerClassGrades();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Ver notas de una materia por período");
                        showSubjectGrades();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Promedios de un estudiante (por materia y por período)");
                        showStudentAverages();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Promedio general del curso en cada materia");
                        showCourseAverages();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Historial académico de un estudiante");
                        showHistory();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Eliminar una nota");
                        deleteGrade();
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

    private void registerGrade() {
        Student student = pickers.pickStudent();
        Subject subject = pickers.pickSubject();
        Teacher teacher = pickers.pickTeacher();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readPeriodIncludingFinal();
        gradeBookController.validateGradeContext(student.getId(), subject.getId(), teacher.getId(), year);

        AcademicRecord existing = gradeBookController.findExisting(student.getId(), subject.getId(), year, period);
        if (existing != null) {
            ConsoleUtils.printWarning("Ya existe una nota de " + Formats.score(existing.getScore())
                    + " para ese período.");
            if (!InputReader.confirm("¿Desea reemplazarla?")) {
                ConsoleUtils.printInfo("No se modificó la nota.");
                return;
            }
        }

        AcademicRecord record = newRecord(student.getId(), subject.getId(), teacher.getId(), year, period);
        record.setScore(InputReader.readDecimal("Nota (0.0 - 5.0)", AcademicRecord.MIN_SCORE,
                AcademicRecord.MAX_SCORE, 1));
        record.setObservations(InputReader.readOptionalText("Observaciones", 500));
        boolean updated = gradeBookController.saveGrade(record);
        ConsoleUtils.printSuccess(updated ? "Nota actualizada." : "Nota registrada.");
    }

    private void registerClassGrades() {
        Subject subject = pickers.pickSubject();
        Teacher teacher = pickers.pickTeacher();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readTerm();
        List<SubjectEnrollment> students = gradeBookController.classList(subject.getId(), year, period);

        ConsoleUtils.printInfo(students.size() + " estudiante(s). Deje la nota vacía para omitir a un estudiante.");
        int saved = 0;
        for (SubjectEnrollment enrollment : students) {
            AcademicRecord existing = gradeBookController.findExisting(enrollment.getStudentId(), subject.getId(),
                    year, period);
            String current = existing == null ? "" : " [actual " + Formats.score(existing.getScore()) + "]";
            BigDecimal score = InputReader.readOptionalDecimal(enrollment.getStudentName() + current,
                    AcademicRecord.MIN_SCORE, AcademicRecord.MAX_SCORE, 1);
            if (score != null) {
                AcademicRecord record = newRecord(enrollment.getStudentId(), subject.getId(), teacher.getId(), year,
                        period);
                record.setScore(score);
                gradeBookController.saveGrade(record);
                saved++;
            }
        }
        ConsoleUtils.printSuccess(saved + " nota(s) guardada(s) para " + subject.getName() + ".");
    }

    private void showSubjectGrades() {
        Subject subject = pickers.pickSubject();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readPeriodIncludingFinal();
        List<AcademicRecord> records = gradeBookController.gradesOfSubject(subject.getId(), year, period);
        ConsoleUtils.printTable(new String[]{"Estudiante", "Nota", "Aprobó", "Profesor", "Fecha"}, records.stream()
                .map(record -> new String[]{record.getStudentName(), Formats.score(record.getScore()),
                        Formats.yesNo(record.isPassing()), record.getTeacherName(), Formats.date(record.getRecordDate())})
                .collect(Collectors.toList()));
        records.stream()
                .mapToDouble(record -> record.getScore().doubleValue())
                .average()
                .ifPresent(average -> ConsoleUtils.printInfo("Promedio del grupo: " + Formats.average(average)));
    }

    private void showStudentAverages() {
        Student student = pickers.pickStudent();
        int year = pickers.readAcademicYear();
        List<SubjectAverage> averages = gradeBookController.subjectAveragesOfStudent(student.getId(), year);
        ConsoleUtils.printHeader("Promedio por materia - " + year);
        printSubjectAverages(averages);

        ConsoleUtils.printHeader("Promedio general por período - " + year);
        List<String[]> rows = new ArrayList<>();
        for (AcademicPeriod period : AcademicPeriod.values()) {
            OptionalDouble average = gradeBookController.periodAverageOfStudent(student.getId(), year, period);
            if (average.isPresent()) {
                rows.add(new String[]{period.getLabel(), Formats.average(average.getAsDouble())});
            }
        }
        ConsoleUtils.printTable(new String[]{"Período", "Promedio general"}, rows);
        averages.stream()
                .mapToDouble(SubjectAverage::getAverage)
                .average()
                .ifPresent(average -> ConsoleUtils.printInfo("Promedio del año: " + Formats.average(average)));
    }

    private void showCourseAverages() {
        Grade grade = pickers.pickGrade();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readOptionalTerm();
        ConsoleUtils.printHeader("Promedio del curso " + grade.getName() + " " + year
                + (period == null ? " (períodos I a IV)" : " - " + period.getLabel()));
        printSubjectAverages(gradeBookController.courseSubjectAverages(grade.getId(), year, period));
    }

    private void showHistory() {
        Student student = pickers.pickStudent();
        Map<Integer, List<AcademicRecord>> history = gradeBookController.academicHistory(student.getId());
        if (history.isEmpty()) {
            ConsoleUtils.printWarning("El estudiante no tiene notas registradas.");
            return;
        }
        String[] headers = {"Materia", "I", "II", "III", "IV", "Final", "Promedio"};
        history.forEach((year, records) -> {
            ConsoleUtils.printHeader("Año " + year);
            Map<String, Map<AcademicPeriod, BigDecimal>> bySubject = records.stream()
                    .collect(Collectors.groupingBy(AcademicRecord::getSubjectName, TreeMap::new,
                            Collectors.toMap(AcademicRecord::getPeriod, AcademicRecord::getScore,
                                    (first, second) -> second, () -> new EnumMap<>(AcademicPeriod.class))));
            List<String[]> rows = bySubject.entrySet().stream()
                    .map(entry -> toHistoryRow(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            ConsoleUtils.printTable(headers, rows);
        });
    }

    private void deleteGrade() {
        Student student = pickers.pickStudent();
        Subject subject = pickers.pickSubject();
        int year = pickers.readAcademicYear();
        AcademicPeriod period = pickers.readPeriodIncludingFinal();
        AcademicRecord record = gradeBookController.findExisting(student.getId(), subject.getId(), year, period);
        if (record == null) {
            throw new RuntimeException("No hay nota registrada para esos datos.");
        }
        if (InputReader.confirm("¿Eliminar la nota " + Formats.score(record.getScore()) + "?")) {
            gradeBookController.deleteGrade(record.getId());
            ConsoleUtils.printSuccess("Nota eliminada.");
        }
    }

    private String[] toHistoryRow(String subjectName, Map<AcademicPeriod, BigDecimal> scores) {
        OptionalDouble termAverage = Arrays.stream(AcademicPeriod.TERMS)
                .filter(scores::containsKey)
                .mapToDouble(period -> scores.get(period).doubleValue())
                .average();
        return new String[]{subjectName,
                Formats.score(scores.get(AcademicPeriod.I)),
                Formats.score(scores.get(AcademicPeriod.II)),
                Formats.score(scores.get(AcademicPeriod.III)),
                Formats.score(scores.get(AcademicPeriod.IV)),
                Formats.score(scores.get(AcademicPeriod.FINAL)),
                termAverage.isPresent() ? Formats.average(termAverage.getAsDouble()) : "-"};
    }

    private void printSubjectAverages(List<SubjectAverage> averages) {
        ConsoleUtils.printTable(new String[]{"Materia", "Promedio", "Notas"}, averages.stream()
                .map(average -> new String[]{average.getSubjectName(), Formats.average(average.getAverage()),
                        String.valueOf(average.getGradesCount())})
                .collect(Collectors.toList()));
    }

    private AcademicRecord newRecord(int studentId, int subjectId, int teacherId, int year, AcademicPeriod period) {
        AcademicRecord record = new AcademicRecord();
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(teacherId);
        record.setAcademicYear(year);
        record.setPeriod(period);
        record.setRecordDate(LocalDate.now());
        return record;
    }
}
