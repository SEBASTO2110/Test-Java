package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 2 of the main menu: subjects, subject enrollments, grades and attendance.
 */
public class AcademicManagementView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Gestión Académica");
            System.out.println("""
                    1. Materias
                    2. Inscripción en materias
                    3. Calificaciones y promedios
                    4. Control de asistencia
                    0. Volver""");
            option = InputReader.readMenuOption(4);
            switch (option) {
                case 1:
                    new SubjectView().showMenu();
                    break;
                case 2:
                    new SubjectEnrollmentView().showMenu();
                    break;
                case 3:
                    new GradeBookView().showMenu();
                    break;
                case 4:
                    new AttendanceView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
