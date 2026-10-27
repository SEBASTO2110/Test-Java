package com.littlehorizons.erp.model;

/**
 * Extracurricular club (table clubes).
 */
public class Club {

    private int id;
    private String name;
    private String description;
    private Integer supervisorTeacherId;
    private String supervisorName;
    private int maxCapacity;
    private String meetingSchedule;
    private boolean active = true;
    private int activeMembers;

    public Club() {
    }

    public Club(String name, String description, Integer supervisorTeacherId, int maxCapacity, String meetingSchedule) {
        this.name = name;
        this.description = description;
        this.supervisorTeacherId = supervisorTeacherId;
        this.maxCapacity = maxCapacity;
        this.meetingSchedule = meetingSchedule;
    }

    public int getAvailableSpots() {
        return Math.max(0, maxCapacity - activeMembers);
    }

    public boolean isFull() {
        return activeMembers >= maxCapacity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSupervisorTeacherId() {
        return supervisorTeacherId;
    }

    public void setSupervisorTeacherId(Integer supervisorTeacherId) {
        this.supervisorTeacherId = supervisorTeacherId;
    }

    /** Read-only, filled by the DAO through a join with profesores. */
    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Read-only, number of members with status Activa (calculated by the DAO). */
    public int getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }
}
