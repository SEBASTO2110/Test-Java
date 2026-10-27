package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.ResourceStatus;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory of lendable resources: books, lab equipment, sports equipment, devices...
 */
public class ResourceView {

    private ResourceController resourceController;
    private ResourcePickers resourcePickers;

    public ResourceView() {
        resourceController = new ResourceController();
        resourcePickers = new ResourcePickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Inventario de recursos");
            System.out.println("""
                1. Registrar recurso
                2. Listar todo el inventario
                3. Listar recursos por estado
                4. Listar recursos por categoría
                5. Buscar recurso (código, nombre o ubicación)
                6. Actualizar datos de un recurso
                7. Cambiar estado (mantenimiento / dar de baja / disponible)
                8. Eliminar recurso
                9. Resumen del inventario por categoría y estado
                0. Volver""");
            option = InputReader.readMenuOption(9);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar recurso");
                        registerResource();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar todo el inventario");
                        printResources(resourceController.listResources());
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Listar recursos por estado");
                        listByStatus();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Listar recursos por categoría");
                        listByCategory();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Buscar recurso (código, nombre o ubicación)");
                        searchResources();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Actualizar datos de un recurso");
                        updateResource();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Cambiar estado (mantenimiento / dar de baja / disponible)");
                        changeStatus();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Eliminar recurso");
                        deleteResource();
                        break;
                    case 9:
                        ConsoleUtils.printActionHeader("Resumen del inventario por categoría y estado");
                        showSummary();
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

    private void registerResource() {
        String code = InputReader.readText("Código de inventario (ej: LIB-0041)", 50);
        String name = InputReader.readText("Nombre", 150);
        ResourceCategory category = resourcePickers.pickCategory();
        String description = InputReader.readOptionalText("Descripción", 1000);
        Resource resource = new Resource(code, name, category.getId(), description,
                InputReader.readDateOrToday("Fecha de adquisición"),
                InputReader.readOptionalText("Ubicación física (ej: Biblioteca, Laboratorio 1)", 100));
        if (InputReader.confirm("¿El recurso ingresa en mantenimiento?")) {
            resource.setStatus(ResourceStatus.MAINTENANCE);
        }
        resourceController.registerResource(resource);
        int id = resource.getId();
        ConsoleUtils.printSuccess("Recurso " + resource.getCode() + " registrado con ID " + id + ".");
    }

    private void printResources(List<Resource> resources) {
        ConsoleUtils.printTable(new String[]{"ID", "Código", "Recurso", "Categoría", "Ubicación", "Estado", "Adquirido"},
                resources.stream()
                        .map(resource -> new String[]{String.valueOf(resource.getId()), resource.getCode(),
                                resource.getName(), resource.getCategoryName(), Formats.text(resource.getLocation()),
                                resource.getStatus().getLabel(), Formats.date(resource.getAcquisitionDate())})
                        .collect(Collectors.toList()));
    }

    private void updateResource() {
        Resource resource = resourcePickers.pickResource(false);
        ConsoleUtils.printInfo("Presione Enter para conservar el valor actual.");
        resource.setCode(InputReader.readTextOrKeep("Código", resource.getCode(), 50));
        resource.setName(InputReader.readTextOrKeep("Nombre", resource.getName(), 150));
        resource.setDescription(InputReader.readTextOrKeep("Descripción", resource.getDescription(), 1000));
        resource.setLocation(InputReader.readTextOrKeep("Ubicación", resource.getLocation(), 100));
        if (InputReader.confirm("Categoría: " + resource.getCategoryName() + ". ¿Desea cambiarla?")) {
            resource.setCategoryId(resourcePickers.pickCategory().getId());
        }
        resourceController.updateResource(resource);
        ConsoleUtils.printSuccess("Recurso actualizado.");
    }

    private void changeStatus() {
        Resource resource = resourcePickers.pickResource(false);
        List<ResourceStatus> options = Arrays.stream(ResourceStatus.values())
                .filter(status -> status != ResourceStatus.LOANED && status != resource.getStatus())
                .collect(Collectors.toList());
        ResourceStatus newStatus = InputReader.readFromList("Nuevo estado", options, ResourceStatus::getLabel);
        resourceController.changeStatus(resource.getId(), newStatus);
        ConsoleUtils.printSuccess("Estado cambiado a " + newStatus.getLabel() + ".");
    }

    private void deleteResource() {
        Resource resource = resourcePickers.pickResource(false);
        if (InputReader.confirm("¿Eliminar el recurso " + resource.getCode() + " - " + resource.getName() + "?")) {
            resourceController.deleteResource(resource.getId());
            ConsoleUtils.printSuccess("Recurso eliminado.");
        }
    }

    /**
     * Also available from the reports menu.
     */
    public void showSummary() {
        Map<String, Map<ResourceStatus, Long>> summary = resourceController.inventorySummary();
        List<String> headers = new ArrayList<>();
        headers.add("Categoría");
        Arrays.stream(ResourceStatus.values()).map(ResourceStatus::getLabel).forEach(headers::add);
        headers.add("Total");
        ConsoleUtils.printTable(headers.toArray(new String[0]), summary.entrySet().stream()
                .map(entry -> {
                    List<String> row = new ArrayList<>();
                    row.add(entry.getKey());
                    Arrays.stream(ResourceStatus.values())
                            .map(status -> String.valueOf(entry.getValue().getOrDefault(status, 0L)))
                            .forEach(row::add);
                    row.add(String.valueOf(entry.getValue().values().stream().mapToLong(Long::longValue).sum()));
                    return row.toArray(new String[0]);
                })
                .collect(Collectors.toList()));
    }

    private void listByStatus() {
        ResourceStatus status = InputReader.readOption("Estado", ResourceStatus.values(), ResourceStatus::getLabel);
        printResources(resourceController.listByStatus(status));
    }

    private void listByCategory() {
        int categoryId = resourcePickers.pickCategory().getId();
        printResources(resourceController.listByCategory(categoryId));
    }

    private void searchResources() {
        String text = InputReader.readText("Texto a buscar", 100);
        printResources(resourceController.search(text));
    }
}
