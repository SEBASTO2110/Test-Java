package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.DocumentType;
import com.littlehorizons.erp.model.Gender;
import com.littlehorizons.erp.model.StudentStatus;
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

public class StudentDaoImpl implements IStudentDAO {
    private static final String ORDER = " ORDER BY e.primer_apellido, e.segundo_apellido, e.primer_nombre";

    private Connection con;

    public StudentDaoImpl() {
        this.con = DBConnectionSingleton.getInstance();
    }

    @Override
    public void addStudent(Student student) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, student);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("registrar el estudiante", e);
        }
    }

    @Override
    public List<Student> listAllStudents() {
        return listStudents("SELECT e.* FROM estudiantes e" + ORDER, null, null);
    }

    @Override
    public Student findStudentById(int id) {
        List<Student> students = listStudents("SELECT e.* FROM estudiantes e WHERE e.id_estudiante = ?", id, null);
        return students.isEmpty() ? null : students.get(0);
    }

    @Override
    public Student findStudentByDocument(String documentNumber) {
        String sql = "SELECT e.* FROM estudiantes e WHERE e.numero_documento = ?";
        Student student = null;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, documentNumber);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    student = createStudent(rs);
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar el estudiante por documento", e);
        }
        return student;
    }

    @Override
    public List<Student> findStudentsByName(String text) {
        String sql = "SELECT e.* FROM estudiantes e WHERE " + SqlUtils.fullName("e") + " LIKE ?" + ORDER;
        List<Student> students = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + text + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    students.add(createStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("buscar estudiantes por nombre", e);
        }
        return students;
    }

    @Override
    public List<Student> findStudentsByGuardian(int guardianId) {
        return listStudents("SELECT e.* FROM estudiantes e"
                + " JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante"
                + " WHERE ea.id_acudiente = ?" + ORDER, guardianId, null);
    }

    @Override
    public List<Student> findStudentsByGradeAndYear(int gradeId, int academicYear) {
        return listStudents("SELECT e.* FROM estudiantes e"
                + " JOIN matriculas m ON m.id_estudiante = e.id_estudiante"
                + " WHERE m.id_grado = ? AND m.`año_academico` = ? AND m.estado_matricula <> 'Retirada'" + ORDER,
                gradeId, academicYear);
    }

    @Override
    public void updateStudent(Student student) {
        String sql = "UPDATE estudiantes SET tipo_documento = ?, numero_documento = ?, primer_nombre = ?, "
                + "segundo_nombre = ?, primer_apellido = ?, segundo_apellido = ?, fecha_nacimiento = ?, genero = ?, "
                + "direccion = ?, telefono = ?, correo_electronico = ?, tipo_sangre = ?, alergias = ?, "
                + "condiciones_medicas = ?, fecha_ingreso = ?, estado = ? WHERE id_estudiante = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            fillStatement(pst, student);
            pst.setInt(17, student.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("actualizar el estudiante", e);
        }
    }

    @Override
    public void updateStudentStatus(int id, StudentStatus status) {
        String sql = "UPDATE estudiantes SET estado = ? WHERE id_estudiante = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, status.getDbValue());
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("cambiar el estado del estudiante", e);
        }
    }

    @Override
    public void deleteStudent(int id) {
        String sql = "DELETE FROM estudiantes WHERE id_estudiante = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("eliminar el estudiante", e);
        }
    }

    /**
     * Runs a SELECT with up to two integer parameters (null = not used).
     */
    private List<Student> listStudents(String sql, Integer firstParam, Integer secondParam) {
        List<Student> students = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (firstParam != null) {
                pst.setInt(1, firstParam);
            }
            if (secondParam != null) {
                pst.setInt(2, secondParam);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    students.add(createStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw ErrorLogger.databaseError("consultar los estudiantes", e);
        }
        return students;
    }

    private void fillStatement(PreparedStatement pst, Student student) throws SQLException {
        pst.setString(1, student.getDocumentType().getDbValue());
        pst.setString(2, student.getDocumentNumber());
        pst.setString(3, student.getFirstName());
        SqlUtils.setText(pst, 4, student.getMiddleName());
        pst.setString(5, student.getLastName());
        SqlUtils.setText(pst, 6, student.getSecondLastName());
        SqlUtils.setDate(pst, 7, student.getBirthDate());
        pst.setString(8, student.getGender().getDbValue());
        SqlUtils.setText(pst, 9, student.getAddress());
        SqlUtils.setText(pst, 10, student.getPhone());
        SqlUtils.setText(pst, 11, student.getEmail());
        SqlUtils.setText(pst, 12, student.getBloodType());
        SqlUtils.setText(pst, 13, student.getAllergies());
        SqlUtils.setText(pst, 14, student.getMedicalConditions());
        SqlUtils.setDate(pst, 15, student.getAdmissionDate());
        pst.setString(16, student.getStatus().getDbValue());
    }

    private Student createStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id_estudiante"));
        student.setDocumentType(DocumentType.fromDbValue(rs.getString("tipo_documento")));
        student.setDocumentNumber(rs.getString("numero_documento"));
        student.setFirstName(rs.getString("primer_nombre"));
        student.setMiddleName(rs.getString("segundo_nombre"));
        student.setLastName(rs.getString("primer_apellido"));
        student.setSecondLastName(rs.getString("segundo_apellido"));
        student.setEmail(rs.getString("correo_electronico"));
        student.setAddress(rs.getString("direccion"));
        student.setBirthDate(SqlUtils.getDate(rs, "fecha_nacimiento"));
        student.setGender(Gender.fromDbValue(rs.getString("genero")));
        student.setPhone(rs.getString("telefono"));
        student.setBloodType(rs.getString("tipo_sangre"));
        student.setAllergies(rs.getString("alergias"));
        student.setMedicalConditions(rs.getString("condiciones_medicas"));
        student.setAdmissionDate(SqlUtils.getDate(rs, "fecha_ingreso"));
        student.setStatus(StudentStatus.fromDbValue(rs.getString("estado")));
        return student;
    }
}
