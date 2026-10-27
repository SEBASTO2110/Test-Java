package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 6 of the main menu: inventory of resources and loans.
 */
public class ResourcesView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Gestión de Recursos");
            System.out.println("""
                    1. Inventario de recursos
                    2. Préstamos y devoluciones
                    0. Volver""");
            option = InputReader.readMenuOption(2);
            switch (option) {
                case 1:
                    new ResourceView().showMenu();
                    break;
                case 2:
                    new LoanView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
