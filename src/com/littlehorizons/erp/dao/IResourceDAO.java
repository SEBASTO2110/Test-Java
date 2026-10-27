package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;

import java.util.List;

public interface IResourceDAO {
    void addResource(Resource resource);

    List<Resource> listAllResources();

    Resource findResourceById(int id);

    boolean existsResourceCode(String code, int excludedId);

    int countLoans(int resourceId);

    void updateResource(Resource resource);

    void deleteResource(int id);
}
