package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDaoImpl;
import com.littlehorizons.erp.dao.ClubEnrollmentDaoImpl;
import com.littlehorizons.erp.dao.IClubDAO;
import com.littlehorizons.erp.dao.IClubEnrollmentDAO;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubEnrollment;
import com.littlehorizons.erp.model.ClubOccupancy;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business rules of the clubs and their members.
 */
public class ClubController {
    public static final int MIN_CAPACITY = 1;
    public static final int MAX_CAPACITY = 200;

    private IClubDAO clubDAO;
    private IClubEnrollmentDAO enrollmentDAO;
    private StudentController studentController;
    private TeacherController teacherController;

    public ClubController() {
        clubDAO = new ClubDaoImpl();
        enrollmentDAO = new ClubEnrollmentDaoImpl();
        studentController = new StudentController();
        teacherController = new TeacherController();
    }

    public List<Club> listClubs() {
        return clubDAO.listAllClubs();
    }

    public List<Club> listActiveClubs() {
        return clubDAO.listAllClubs().stream()
                .filter(Club::isActive)
                .collect(Collectors.toList());
    }

    public Club findClub(int clubId) {
        Club club = clubDAO.findClubById(clubId);
        if (club == null) {
            throw new RuntimeException("No existe un club con ID " + clubId + ".");
        }
        return club;
    }

    public void registerClub(Club club) {
        validateClub(club, 0);
        clubDAO.addClub(club);
    }

    public void updateClub(Club club) {
        Club current = findClub(club.getId());
        validateClub(club, club.getId());
        if (club.getMaxCapacity() < current.getActiveMembers()) {
            throw new RuntimeException("El cupo no puede ser menor a los " + current.getActiveMembers()
                    + " miembros activos del club.");
        }
        clubDAO.updateClub(club);
    }

    public void changeActive(int clubId, boolean active) {
        Club club = findClub(clubId);
        club.setActive(active);
        clubDAO.updateClub(club);
    }

    /**
     * A club with active members cannot be deleted (its history would be lost); it can be deactivated.
     */
    public void deleteClub(int clubId) {
        Club club = findClub(clubId);
        if (club.getActiveMembers() > 0) {
            throw new RuntimeException("El club tiene " + club.getActiveMembers()
                    + " miembros activos. Retírelos o desactive el club en lugar de eliminarlo.");
        }
        clubDAO.deleteClub(clubId);
    }

    private void validateClub(Club club, int excludedId) {
        if (club.getName() == null || club.getName().isBlank()) {
            throw new RuntimeException("El nombre del club es obligatorio.");
        }
        if (clubDAO.existsClubName(club.getName(), excludedId)) {
            throw new RuntimeException("Ya existe un club llamado '" + club.getName() + "'.");
        }
        if (club.getMaxCapacity() < MIN_CAPACITY || club.getMaxCapacity() > MAX_CAPACITY) {
            throw new RuntimeException("El cupo máximo debe estar entre " + MIN_CAPACITY + " y " + MAX_CAPACITY + ".");
        }
        if (club.getSupervisorTeacherId() != null) {
            teacherController.findActiveTeacher(club.getSupervisorTeacherId());
        }
    }

    /**
     * Enrolls a student checking their status, duplicates and the maximum capacity of the club.
     */
    public ClubEnrollment enrollStudent(int studentId, int clubId, LocalDate joinDate) {
        Student student = studentController.findActiveStudent(studentId);
        Club club = findClub(clubId);
        if (!club.isActive()) {
            throw new RuntimeException("El club '" + club.getName() + "' no está activo.");
        }
        if (joinDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de ingreso no puede ser futura.");
        }
        if (enrollmentDAO.findActiveClubEnrollment(studentId, clubId) != null) {
            throw new RuntimeException(student.getFullName() + " ya es miembro activo de '" + club.getName() + "'.");
        }
        if (enrollmentDAO.countActiveMembers(clubId) >= club.getMaxCapacity()) {
            throw new RuntimeException("El club '" + club.getName() + "' alcanzó su cupo máximo ("
                    + club.getMaxCapacity() + " estudiantes).");
        }
        ClubEnrollment enrollment = new ClubEnrollment(studentId, clubId, joinDate);
        enrollmentDAO.addClubEnrollment(enrollment);
        enrollment.setStudentName(student.getFullName());
        enrollment.setClubName(club.getName());
        return enrollment;
    }

    public void withdrawStudent(int enrollmentId, LocalDate withdrawalDate, String reason) {
        ClubEnrollment enrollment = enrollmentDAO.findClubEnrollmentById(enrollmentId);
        if (enrollment == null) {
            throw new RuntimeException("No existe una membresía con ID " + enrollmentId + ".");
        }
        if (!enrollment.isActive()) {
            throw new RuntimeException("La membresía ya está retirada.");
        }
        if (withdrawalDate.isBefore(enrollment.getJoinDate()) || withdrawalDate.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de retiro debe estar entre " + enrollment.getJoinDate() + " y hoy.");
        }
        enrollmentDAO.withdrawFromClub(enrollmentId, withdrawalDate, reason);
    }

    public List<ClubEnrollment> getMembers(int clubId, boolean onlyActive) {
        findClub(clubId);
        return enrollmentDAO.findClubEnrollmentsByClub(clubId).stream()
                .filter(enrollment -> !onlyActive || enrollment.isActive())
                .collect(Collectors.toList());
    }

    public List<ClubEnrollment> getStudentClubs(int studentId) {
        studentController.findStudent(studentId);
        return enrollmentDAO.findClubEnrollmentsByStudent(studentId);
    }

    /**
     * Active clubs from the most to the least occupied.
     */
    public List<ClubOccupancy> occupancyReport() {
        return listActiveClubs().stream()
                .map(club -> new ClubOccupancy(club.getName(), club.getSupervisorName(), club.getActiveMembers(),
                        club.getMaxCapacity()))
                .sorted(Comparator.comparingDouble(ClubOccupancy::getOccupancyPercentage).reversed())
                .collect(Collectors.toList());
    }
}
