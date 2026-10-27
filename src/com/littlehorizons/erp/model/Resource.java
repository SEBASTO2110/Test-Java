package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Lendable school resource: book, lab equipment, sports equipment, device... (table recursos).
 */
public class Resource {

    private int id;
    private String code;
    private String name;
    private int categoryId;
    private String categoryName;
    private String description;
    private LocalDate acquisitionDate;
    private String location;
    private ResourceStatus status = ResourceStatus.AVAILABLE;

    public Resource() {
    }

    public Resource(String code, String name, int categoryId, String description, LocalDate acquisitionDate,
                    String location) {
        this.code = code;
        this.name = name;
        this.categoryId = categoryId;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.location = location;
    }

    public boolean isAvailable() {
        return status == ResourceStatus.AVAILABLE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }
}
