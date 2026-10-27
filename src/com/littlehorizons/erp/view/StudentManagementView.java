package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 1 of the main menu: students, guardians and grade enrollments.
 */
public class StudentManagementView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Gestión de Estudiantes");
            System.out.println("""
                    1. Estudiantes
                    2. Acudientes
                    3. Matrículas por grado
                    0. Volver""");
            option = InputReader.readMenuOption(3);
            switch (option) {
                case 1:
                    new StudentView().showMenu();
                    break;
                case 2:
                    new GuardianView().showMenu();
                    break;
                case 3:
                    new EnrollmentView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
