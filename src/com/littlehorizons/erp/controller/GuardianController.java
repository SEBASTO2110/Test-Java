package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDaoImpl;
import com.littlehorizons.erp.dao.IGuardianDAO;
import com.littlehorizons.erp.dao.IStudentDAO;
import com.littlehorizons.erp.dao.StudentDaoImpl;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.GuardianLink;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.DocumentType;

import java.util.List;

/**
 * Business rules of the guardians. A guardian can be in charge of several students (siblings)
 * and every student needs at least one guardian.
 */
public class GuardianController {
    private IGuardianDAO guardianDAO;
    private IStudentDAO studentDAO;

    public GuardianController() {
        guardianDAO = new GuardianDaoImpl();
        studentDAO = new StudentDaoImpl();
    }

    public void registerGuardian(Guardian guardian) {
        validateGuardian(guardian);
        Guardian existing = guardianDAO.findGuardianByDocument(guardian.getDocumentNumber());
        if (existing != null) {
            throw new RuntimeException("Ya existe un acudiente con el documento " + guardian.getDocumentNumber()
                    + " (ID " + existing.getId() + ").");
        }
        guardianDAO.addGuardian(guardian);
    }

    public void updateGuardian(Guardian guardian) {
        validateGuardian(guardian);
        Guardian existing = guardianDAO.findGuardianByDocument(guardian.getDocumentNumber());
        if (existing != null && existing.getId() != guardian.getId()) {
            throw new RuntimeException("El documento ya pertenece a otro acudiente.");
        }
        guardianDAO.updateGuardian(guardian);
    }

    public Guardian findGuardian(int guardianId) {
        Guardian guardian = guardianDAO.findGuardianById(guardianId);
        if (guardian == null) {
            throw new RuntimeException("No existe un acudiente con ID " + guardianId + ".");
        }
        return guardian;
    }

    public List<Guardian> listGuardians() {
        return guardianDAO.listAllGuardians();
    }

    public List<Student> getStudentsOf(int guardianId) {
        findGuardian(guardianId);
        return studentDAO.findStudentsByGuardian(guardianId);
    }

    public void linkGuardian(int studentId, int guardianId, boolean primaryContact, boolean emergencyContact) {
        if (studentDAO.findStudentById(studentId) == null) {
            throw new RuntimeException("No existe un estudiante con ID " + studentId + ".");
        }
        findGuardian(guardianId);
        if (guardianDAO.isLinked(studentId, guardianId)) {
            throw new RuntimeException("El acudiente ya está vinculado a ese estudiante.");
        }
        guardianDAO.linkGuardian(new GuardianLink(studentId, guardianId, primaryContact, emergencyContact));
    }

    public void unlinkGuardian(int studentId, int guardianId) {
        if (!guardianDAO.isLinked(studentId, guardianId)) {
            throw new RuntimeException("Ese acudiente no está vinculado al estudiante.");
        }
        if (guardianDAO.countGuardiansOf(studentId) <= 1) {
            throw new RuntimeException("No se puede desvincular: el estudiante debe tener al menos un acudiente.");
        }
        guardianDAO.unlinkGuardian(studentId, guardianId);
    }

    public void deleteGuardian(int guardianId) {
        boolean isOnlyGuardianOfSomeone = getStudentsOf(guardianId).stream()
                .anyMatch(student -> guardianDAO.countGuardiansOf(student.getId()) <= 1);
        if (isOnlyGuardianOfSomeone) {
            throw new RuntimeException("No se puede eliminar: es el único acudiente de al menos un estudiante.");
        }
        guardianDAO.deleteGuardian(guardianId);
    }

    private void validateGuardian(Guardian guardian) {
        if (guardian == null) {
            throw new RuntimeException("Error, el acudiente está vacío.");
        }
        if (guardian.getDocumentType() == DocumentType.TI) {
            throw new RuntimeException("Un acudiente debe ser mayor de edad (no puede usar Tarjeta de Identidad).");
        }
    }
}
