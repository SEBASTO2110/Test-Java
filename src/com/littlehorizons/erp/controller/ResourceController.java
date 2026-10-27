package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.IResourceCategoryDAO;
import com.littlehorizons.erp.dao.IResourceDAO;
import com.littlehorizons.erp.dao.ResourceCategoryDaoImpl;
import com.littlehorizons.erp.dao.ResourceDaoImpl;
import com.littlehorizons.erp.model.ResourceStatus;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.ResourceCategory;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Business rules of the inventory of resources that can be lent.
 */
public class ResourceController {
    private IResourceDAO resourceDAO;
    private IResourceCategoryDAO categoryDAO;

    public ResourceController() {
        resourceDAO = new ResourceDaoImpl();
        categoryDAO = new ResourceCategoryDaoImpl();
    }

    public List<Resource> listResources() {
        return resourceDAO.listAllResources();
    }

    public List<Resource> listByStatus(ResourceStatus status) {
        return resourceDAO.listAllResources().stream()
                .filter(resource -> resource.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Resource> listByCategory(int categoryId) {
        return resourceDAO.listAllResources().stream()
                .filter(resource -> resource.getCategoryId() == categoryId)
                .collect(Collectors.toList());
    }

    /**
     * Search by code, name or location (it does not matter if it is upper or lower case).
     */
    public List<Resource> search(String text) {
        String query = text.toLowerCase();
        return resourceDAO.listAllResources().stream()
                .filter(resource -> contains(resource.getCode(), query) || contains(resource.getName(), query)
                        || contains(resource.getLocation(), query))
                .collect(Collectors.toList());
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    public List<ResourceCategory> listCategories() {
        return categoryDAO.listAllCategories();
    }

    public Resource findResource(int resourceId) {
        Resource resource = resourceDAO.findResourceById(resourceId);
        if (resource == null) {
            throw new RuntimeException("No existe un recurso con ID " + resourceId + ".");
        }
        return resource;
    }

    public void registerResource(Resource resource) {
        validateResource(resource, 0);
        if (resource.getStatus() == ResourceStatus.LOANED) {
            throw new RuntimeException("Un recurso nuevo no puede registrarse como Prestado.");
        }
        resourceDAO.addResource(resource);
    }

    /**
     * Updates the descriptive data; the status only changes with changeStatus, loans and returns.
     */
    public void updateResource(Resource resource) {
        Resource current = findResource(resource.getId());
        validateResource(resource, resource.getId());
        resource.setStatus(current.getStatus());
        resourceDAO.updateResource(resource);
    }

    /**
     * Manual status changes (maintenance, write-off). "Prestado" is only set by a loan
     * and only a return can release a loaned resource.
     */
    public void changeStatus(int resourceId, ResourceStatus newStatus) {
        Resource resource = findResource(resourceId);
        if (newStatus == ResourceStatus.LOANED) {
            throw new RuntimeException("Para prestar un recurso use la opción de préstamos.");
        }
        if (resource.getStatus() == ResourceStatus.LOANED) {
            throw new RuntimeException("El recurso está prestado; registre primero la devolución.");
        }
        if (resource.getStatus() == newStatus) {
            throw new RuntimeException("El recurso ya está en estado " + newStatus.getLabel() + ".");
        }
        resource.setStatus(newStatus);
        resourceDAO.updateResource(resource);
    }

    /**
     * Only resources without loan history can be deleted; the others are written off ("Dado de Baja").
     */
    public void deleteResource(int resourceId) {
        Resource resource = findResource(resourceId);
        if (resource.getStatus() == ResourceStatus.LOANED) {
            throw new RuntimeException("El recurso está prestado y no se puede eliminar.");
        }
        if (resourceDAO.countLoans(resourceId) > 0) {
            throw new RuntimeException("El recurso tiene historial de préstamos; márquelo como 'Dado de Baja'.");
        }
        resourceDAO.deleteResource(resourceId);
    }

    private void validateResource(Resource resource, int excludedId) {
        if (resource.getCode() == null || !resource.getCode().matches("[A-Za-z0-9_-]{3,50}")) {
            throw new RuntimeException("El código debe tener de 3 a 50 caracteres (letras, números, - o _).");
        }
        resource.setCode(resource.getCode().toUpperCase());
        if (resourceDAO.existsResourceCode(resource.getCode(), excludedId)) {
            throw new RuntimeException("Ya existe un recurso con el código " + resource.getCode() + ".");
        }
        if (resource.getName() == null || resource.getName().isBlank()) {
            throw new RuntimeException("El nombre del recurso es obligatorio.");
        }
        if (categoryDAO.findCategoryById(resource.getCategoryId()) == null) {
            throw new RuntimeException("La categoría seleccionada no existe.");
        }
        if (resource.getAcquisitionDate() != null && resource.getAcquisitionDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de adquisición no puede ser futura.");
        }
    }

    /**
     * Number of resources per category and status.
     */
    public Map<String, Map<ResourceStatus, Long>> inventorySummary() {
        return resourceDAO.listAllResources().stream()
                .collect(Collectors.groupingBy(Resource::getCategoryName, TreeMap::new,
                        Collectors.groupingBy(Resource::getStatus, () -> new EnumMap<>(ResourceStatus.class),
                                Collectors.counting())));
    }
}
