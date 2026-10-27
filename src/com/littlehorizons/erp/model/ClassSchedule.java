package com.littlehorizons.erp.model;

import java.time.LocalTime;

/**
 * Day and time when a teaching assignment takes place (table horarios_clase).
 */
public class ClassSchedule {

    private int id;
    private int assignmentId;
    private WeekDay day;
    private LocalTime startTime;
    private LocalTime endTime;

    // Read-only data loaded with joins
    private int teacherId;
    private String assignmentDescription;

    /**
     * Two time blocks overlap when each one starts before the other ends.
     */
    public boolean overlaps(ClassSchedule other) {
        return day == other.day
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public WeekDay getDay() {
        return day;
    }

    public void setDay(WeekDay day) {
        this.day = day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getAssignmentDescription() {
        return assignmentDescription;
    }

    public void setAssignmentDescription(String assignmentDescription) {
        this.assignmentDescription = assignmentDescription;
    }
}
