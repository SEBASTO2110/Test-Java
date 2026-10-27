package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResourceCategoryDaoImpl implements IResourceCategoryDAO {
    private Connection con;

    public ResourceCategoryDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public List<ResourceCategory> listAllCategories() {
        String sql = "SELECT * FROM categorias_recurso ORDER BY nombre_categoria";
        List<ResourceCategory> categories = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                categories.add(new ResourceCategory(rs.getInt("id_categoria"), rs.getString("nombre_categoria"),
                        rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar las categorías de recursos", e);
        }
        return categories;
    }

    @Override
    public ResourceCategory findCategoryById(int id) {
        String sql = "SELECT * FROM categorias_recurso WHERE id_categoria = ?";
        ResourceCategory category = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    category = new ResourceCategory(rs.getInt("id_categoria"), rs.getString("nombre_categoria"),
                            rs.getString("descripcion"));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar la categoría", e);
        }
        return category;
    }
}
