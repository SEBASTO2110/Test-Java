package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Teacher of the school (table profesores).
 */
public class Teacher extends Person {

    private String phone;
    private String specialization;
    private LocalDate hireDate;
    private TeacherStatus status = TeacherStatus.ACTIVE;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public TeacherStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherStatus status) {
        this.status = status;
    }
}
