package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * School event such as a science fair or sports day (table eventos).
 */
public class Event {

    private int id;
    private String name;
    private String description;
    private EventType type;
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private Integer organizerTeacherId;
    private String organizerName;
    private boolean active = true;
    private int participantCount;

    public Event() {
    }

    public Event(String name, String description, EventType type, LocalDate eventDate, String location,
                 boolean hasCompetition, Integer organizerTeacherId) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
        this.organizerTeacherId = organizerTeacherId;
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

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean hasCompetition() {
        return hasCompetition;
    }

    public void setHasCompetition(boolean hasCompetition) {
        this.hasCompetition = hasCompetition;
    }

    public Integer getOrganizerTeacherId() {
        return organizerTeacherId;
    }

    public void setOrganizerTeacherId(Integer organizerTeacherId) {
        this.organizerTeacherId = organizerTeacherId;
    }

    /** Read-only, filled by the DAO through a join with profesores. */
    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** Read-only, calculated by the DAO. */
    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }
}
