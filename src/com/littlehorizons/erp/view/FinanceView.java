package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

/**
 * Menu 5 of the main menu: payment concepts, payments and invoices.
 */
public class FinanceView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Gestión Financiera");
            System.out.println("""
                    1. Conceptos de pago y becas
                    2. Pagos y facturación
                    0. Volver""");
            option = InputReader.readMenuOption(2);
            switch (option) {
                case 1:
                    new PaymentConceptView().showMenu();
                    break;
                case 2:
                    new PaymentView().showMenu();
                    break;
                default:
                    break;
            }
        } while (option != 0);
    }
}
