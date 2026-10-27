package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ResourceCategory;

import java.util.List;

public interface IResourceCategoryDAO {
    List<ResourceCategory> listAllCategories();

    ResourceCategory findCategoryById(int id);
}
