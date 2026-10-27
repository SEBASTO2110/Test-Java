package com.littlehorizons.erp;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.view.MainView;

/**
 * Entry point of the ERP Little Horizons.
 */
public class Main {
    public static void main(String[] args) {
        try {
            DBConnectionSingleton.getInstance();
        } catch (RuntimeException e) {
            ConsoleUtils.printError("No fue posible conectarse a la base de datos erp_little_horizons.");
            ConsoleUtils.printInfo("Verifique que MySQL esté encendido, que el script docs/database.sql se haya");
            ConsoleUtils.printInfo("ejecutado y que las credenciales de db.properties sean correctas.");
            ConsoleUtils.printInfo("El detalle del error quedó en error_log.txt.");
            System.exit(1);
        }

        MainView mainView = new MainView();
        mainView.showMenu();
    }
}
