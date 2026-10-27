package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.model.ResourceStatus;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Prompts to choose a resource or a category. Users can type the ID or 0 to see the list first.
 */
public class ResourcePickers {

    private ResourceController resourceController;

    public ResourcePickers() {
        resourceController = new ResourceController();
    }

    /**
     * @param onlyAvailable when true, the list shows only resources that can be lent
     */
    public Resource pickResource(boolean onlyAvailable) {
        int id = InputReader.readInt("ID del recurso (0 = ver lista)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            List<Resource> resources = onlyAvailable
                    ? resourceController.listByStatus(ResourceStatus.AVAILABLE)
                    : resourceController.listResources();
            ConsoleUtils.printTable(new String[]{"ID", "Código", "Recurso", "Categoría", "Estado"}, resources.stream()
                    .map(resource -> new String[]{String.valueOf(resource.getId()), resource.getCode(),
                            resource.getName(), resource.getCategoryName(), resource.getStatus().getLabel()})
                    .collect(Collectors.toList()));
            id = InputReader.readId("ID del recurso");
        }
        Resource resource = resourceController.findResource(id);
        ConsoleUtils.printInfo("» " + resource.getCode() + " - " + resource.getName() + " ("
                + resource.getStatus().getLabel() + ")");
        return resource;
    }

    public ResourceCategory pickCategory() {
        return InputReader.readFromList("Categoría", resourceController.listCategories(), ResourceCategory::getName);
    }
}
