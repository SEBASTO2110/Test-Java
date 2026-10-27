package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Membership of a student in a club (table membresias_club).
 */
public class ClubEnrollment {

    private int id;
    private int studentId;
    private String studentName;
    private int clubId;
    private String clubName;
    private LocalDate joinDate;
    private MembershipStatus status = MembershipStatus.ACTIVE;
    private LocalDate withdrawalDate;
    private String withdrawalReason;

    public ClubEnrollment() {
    }

    public ClubEnrollment(int studentId, int clubId, LocalDate joinDate) {
        this.studentId = studentId;
        this.clubId = clubId;
        this.joinDate = joinDate;
    }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }
}
