package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ResourceStatus;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.utils.DBConnectionSingleton;
import com.littlehorizons.erp.utils.ErrorLogger;
import com.littlehorizons.erp.utils.SqlUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ResourceDaoImpl implements IResourceDAO {
    private static final String SELECT = "SELECT r.*, c.nombre_categoria FROM recursos r"
            + " JOIN categorias_recurso c ON c.id_categoria = r.id_categoria";

    private Connection con;

    public ResourceDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addResource(Resource resource) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, "
                + "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, resource);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    resource.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el recurso", e);
        }
    }

    @Override
    public List<Resource> listAllResources() {
        String sql = SELECT + " ORDER BY c.nombre_categoria, r.nombre_recurso";
        List<Resource> resources = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                resources.add(createResource(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los recursos", e);
        }
        return resources;
    }

    @Override
    public Resource findResourceById(int id) {
        String sql = SELECT + " WHERE r.id_recurso = ?";
        Resource resource = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    resource = createResource(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el recurso", e);
        }
        return resource;
    }

    @Override
    public boolean existsResourceCode(String code, int excludedId) {
        String sql = "SELECT COUNT(*) FROM recursos WHERE UPPER(codigo_recurso) = UPPER(?) AND id_recurso <> ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, code);
            pst.setInt(2, excludedId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("verificar el código del recurso", e);
        }
    }

    @Override
    public int countLoans(int resourceId) {
        String sql = "SELECT (SELECT COUNT(*) FROM prestamos_estudiante WHERE id_recurso = ?)"
                + " + (SELECT COUNT(*) FROM prestamos_profesor WHERE id_recurso = ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, resourceId);
            pst.setInt(2, resourceId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("contar los préstamos del recurso", e);
        }
    }

    @Override
    public void updateResource(Resource resource) {
        String sql = "UPDATE recursos SET codigo_recurso = ?, nombre_recurso = ?, id_categoria = ?, descripcion = ?, "
                + "fecha_adquisicion = ?, ubicacion_fisica = ?, estado_recurso = ? WHERE id_recurso = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, resource);
            pst.setInt(8, resource.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el recurso", e);
        }
    }

    @Override
    public void deleteResource(int id) {
        String sql = "DELETE FROM recursos WHERE id_recurso = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el recurso", e);
        }
    }

    private void fillStatement(PreparedStatement pst, Resource resource) throws SQLException {
        pst.setString(1, resource.getCode());
        pst.setString(2, resource.getName());
        pst.setInt(3, resource.getCategoryId());
        SqlUtils.setText(pst, 4, resource.getDescription());
        SqlUtils.setDate(pst, 5, resource.getAcquisitionDate());
        SqlUtils.setText(pst, 6, resource.getLocation());
        pst.setString(7, resource.getStatus().getDbValue());
    }

    private Resource createResource(ResultSet rs) throws SQLException {
        Resource resource = new Resource(rs.getString("codigo_recurso"), rs.getString("nombre_recurso"),
                rs.getInt("id_categoria"), rs.getString("descripcion"), SqlUtils.getDate(rs, "fecha_adquisicion"),
                rs.getString("ubicacion_fisica"));
        resource.setId(rs.getInt("id_recurso"));
        resource.setCategoryName(rs.getString("nombre_categoria"));
        resource.setStatus(ResourceStatus.fromDbValue(rs.getString("estado_recurso")));
        return resource;
    }
}
