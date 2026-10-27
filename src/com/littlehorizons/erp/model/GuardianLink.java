package com.littlehorizons.erp.model;

/**
 * Relationship between a student and one of their guardians (table estudiante_acudiente).
 */
public class GuardianLink {

    private int studentId;
    private int guardianId;
    private boolean primaryContact;
    private boolean emergencyContact = true;

    public GuardianLink() {
    }

    public GuardianLink(int studentId, int guardianId, boolean primaryContact, boolean emergencyContact) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.primaryContact = primaryContact;
        this.emergencyContact = emergencyContact;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(int guardianId) {
        this.guardianId = guardianId;
    }

    public boolean isPrimaryContact() {
        return primaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        this.primaryContact = primaryContact;
    }

    public boolean isEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        this.emergencyContact = emergencyContact;
    }
}
