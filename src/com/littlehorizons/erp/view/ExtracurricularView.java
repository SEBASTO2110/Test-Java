package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 4 of the main menu: clubs and events.
 */
public class ExtracurricularView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Actividades Extracurriculares");
            System.out.println("""
                    1. Clubes
                    2. Eventos
                    0. Volver""");
            option = InputReader.readMenuOption(2);
            switch (option) {
                case 1:
                    new ClubView().showMenu();
                    break;
                case 2:
                    new EventView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
