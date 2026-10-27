package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.TeacherStatus;
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

public class TeacherDaoImpl implements ITeacherDAO {
    private static final String ORDER = " ORDER BY p.primer_apellido, p.segundo_apellido, p.primer_nombre";

    private Connection con;

    public TeacherDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addTeacher(Teacher teacher) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, "
                + "fecha_contratacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, teacher);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    teacher.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el profesor", e);
        }
    }

    @Override
    public List<Teacher> listAllTeachers() {
        String sql = "SELECT p.* FROM profesores p" + ORDER;
        List<Teacher> teachers = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                teachers.add(createTeacher(rs));
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("listar los profesores", e);
        }
        return teachers;
    }

    @Override
    public Teacher findTeacherById(int id) {
        String sql = "SELECT p.* FROM profesores p WHERE p.id_profesor = ?";
        Teacher teacher = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    teacher = createTeacher(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el profesor", e);
        }
        return teacher;
    }

    @Override
    public Teacher findTeacherByDocument(String documentNumber) {
        return findOneByText("SELECT p.* FROM profesores p WHERE p.numero_documento = ?", documentNumber);
    }

    @Override
    public Teacher findTeacherByEmail(String email) {
        return findOneByText("SELECT p.* FROM profesores p WHERE p.correo_electronico = ?", email);
    }

    @Override
    public List<Teacher> findTeachersByNameOrSpecialization(String text) {
        String sql = "SELECT p.* FROM profesores p WHERE " + SqlUtils.fullName("p") + " LIKE ?"
                + " OR p.especializacion LIKE ?" + ORDER;
        List<Teacher> teachers = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + text + "%");
            pst.setString(2, "%" + text + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    teachers.add(createTeacher(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar profesores", e);
        }
        return teachers;
    }

    @Override
    public void updateTeacher(Teacher teacher) {
        String sql = "UPDATE profesores SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, "
                + "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, correo_electronico = ?, "
                + "telefono = ?, especializacion = ?, fecha_contratacion = ?, estado = ? WHERE id_profesor = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, teacher);
            pst.setInt(12, teacher.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el profesor", e);
        }
    }

    @Override
    public void updateTeacherStatus(int id, TeacherStatus status) {
        String sql = "UPDATE profesores SET estado = ? WHERE id_profesor = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status.getDbValue());
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("cambiar el estado del profesor", e);
        }
    }

    @Override
    public void deleteTeacher(int id) {
        String sql = "DELETE FROM profesores WHERE id_profesor = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el profesor", e);
        }
    }

    private Teacher findOneByText(String sql, String value) {
        Teacher teacher = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, value);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    teacher = createTeacher(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el profesor", e);
        }
        return teacher;
    }

    private void fillStatement(PreparedStatement pst, Teacher teacher) throws SQLException {
        pst.setString(1, teacher.getDocumentType().getDbValue());
        pst.setString(2, teacher.getDocumentNumber());
        pst.setString(3, teacher.getFirstName());
        SqlUtils.setText(pst, 4, teacher.getMiddleName());
        pst.setString(5, teacher.getLastName());
        SqlUtils.setText(pst, 6, teacher.getSecondLastName());
        pst.setString(7, teacher.getEmail());
        SqlUtils.setText(pst, 8, teacher.getPhone());
        SqlUtils.setText(pst, 9, teacher.getSpecialization());
        SqlUtils.setDate(pst, 10, teacher.getHireDate());
        pst.setString(11, teacher.getStatus().getDbValue());
    }

    private Teacher createTeacher(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setId(rs.getInt("id_profesor"));
        teacher.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        teacher.setDocumentNumber(rs.getString("numero_documento"));
        teacher.setFirstName(rs.getString("primer_nombre"));
        teacher.setMiddleName(rs.getString("segundo_nombre"));
        teacher.setLastName(rs.getString("primer_apellido"));
        teacher.setSecondLastName(rs.getString("segundo_apellido"));
        teacher.setEmail(rs.getString("correo_electronico"));
        teacher.setPhone(rs.getString("telefono"));
        teacher.setSpecialization(rs.getString("especializacion"));
        teacher.setHireDate(SqlUtils.getDate(rs, "fecha_contratacion"));
        teacher.setStatus(TeacherStatus.fromDbValue(rs.getString("estado")));
        return teacher;
    }
}
