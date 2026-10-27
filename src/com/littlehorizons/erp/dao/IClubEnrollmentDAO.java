package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ClubEnrollment;

import java.time.LocalDate;
import java.util.List;

public interface IClubEnrollmentDAO {
    void addClubEnrollment(ClubEnrollment enrollment);

    ClubEnrollment findClubEnrollmentById(int id);

    List<ClubEnrollment> findClubEnrollmentsByClub(int clubId);

    List<ClubEnrollment> findClubEnrollmentsByStudent(int studentId);

    ClubEnrollment findActiveClubEnrollment(int studentId, int clubId);

    int countActiveMembers(int clubId);

    void withdrawFromClub(int enrollmentId, LocalDate withdrawalDate, String reason);
}
