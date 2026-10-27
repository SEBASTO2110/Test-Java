package com.littlehorizons.erp.view;

import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.InputReader;
import com.littlehorizons.erp.utils.ScannerSingleton;

/**
 * Main menu of the ERP. Each option opens the menu of one module.
 */
public class MainView {

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printBanner("ERP Colegio Little Horizons", "Sistema de Gestión Educativa");
            System.out.println("""

                    1. Gestión de Estudiantes

                    2. Gestión Académica
                    3. Gestión de Profesores
                    4. Actividades Extracurriculares
                    5. Gestión Financiera
                    6. Gestión de Recursos
                    7. Reportes y Estadísticas
                    8. Exportador de materias
                    9. Salir""");
            option = readOption();
            try {
                switch (option) {
                    case 1:
                        new StudentManagementView().showMenu();
                        break;
                    case 2:
                        new AcademicManagementView().showMenu();
                        break;
                    case 3:
                        new TeacherManagementView().showMenu();
                        break;
                    case 4:
                        new ExtracurricularView().showMenu();
                        break;
                    case 5:
                        new FinanceView().showMenu();
                        break;
                    case 6:
                        new ResourcesView().showMenu();
                        break;
                    case 7:
                        new ReportView().showMenu();
                        break;
                    
                    case 8:
                        new ExportMateriasView().showMenu();
                        break;

                    case 9:
                        ConsoleUtils.printSuccess("Hasta pronto.");
                        break;
                    default:
                        ConsoleUtils.printError("Opción inválida.");
                }
            } catch (RuntimeException e) {
                ConsoleUtils.printException(e);
            }
        } while (option != 9);

        DBConnectionSingleton.closeConnection();
        ScannerSingleton.closeScanner();
    }

    private int readOption() {
        try {
            return InputReader.readInt("\nSeleccione una opción", 1, 9);
        } catch (RuntimeException e) {
            return 9;
        }
    }
}
