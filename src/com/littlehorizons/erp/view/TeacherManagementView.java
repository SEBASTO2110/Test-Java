package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 3 of the main menu: teachers, subject assignments and schedules.
 */
public class TeacherManagementView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Gestión de Profesores");
            System.out.println("""
                    1. Directorio de profesores
                    2. Carga académica y horarios
                    0. Volver""");
            option = InputReader.readMenuOption(2);
            switch (option) {
                case 1:
                    new TeacherView().showMenu();
                    break;
                case 2:
                    new AssignmentView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
