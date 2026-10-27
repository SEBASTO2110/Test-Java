package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeachingAssignment;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExportMateriasView {
    private static final int ACADEMIC_YEAR = 2025;
    private final TeacherController teacherController = new TeacherController();

    public void showMenu() {
        ConsoleUtils.printActionHeader("Exportador de materias asignadas - Año " + ACADEMIC_YEAR);
        int teacherId = InputReader.readId("ID del profesor");
        Teacher teacher = teacherController.findTeacher(teacherId);
        List<TeachingAssignment> assignments = teacherController.assignmentsOf(teacherId, ACADEMIC_YEAR);
        if (assignments.isEmpty()) {
            ConsoleUtils.printWarning("El profesor no tiene materias asignadas para " + ACADEMIC_YEAR + ".");
            return;
        }

        Path outputDirectory = Path.of("exports");
        Path outputFile = outputDirectory.resolve("materias_profesor_" + teacherId + "_" + ACADEMIC_YEAR + ".csv");
        List<String> rows = new ArrayList<>();
        rows.add("Profesor;Materia;Grado;Sección;Año académico");
        for (TeachingAssignment assignment : assignments) {
            rows.add(String.join(";",
                    csvValue(teacher.getFullName()),
                    csvValue(assignment.getSubjectName()),
                    csvValue(assignment.getGradeName()),
                    csvValue(assignment.getSection()),
                    String.valueOf(assignment.getAcademicYear())));
        }

        try {
            Files.createDirectories(outputDirectory);
            Files.write(outputFile, rows, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible guardar el archivo " + outputFile + ".", e);
        }
        ConsoleUtils.printSuccess("Materias exportadas en " + outputFile + ".");
    }

    private String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        if (safeValue.contains(";") || safeValue.contains("\"")
                || safeValue.contains("\n") || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }
}
