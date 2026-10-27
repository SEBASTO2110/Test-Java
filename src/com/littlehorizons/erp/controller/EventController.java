package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EventDaoImpl;
import com.littlehorizons.erp.dao.EventParticipationDaoImpl;
import com.littlehorizons.erp.dao.IEventDAO;
import com.littlehorizons.erp.dao.IEventParticipationDAO;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.CompetitionPosition;
import com.littlehorizons.erp.model.EventType;
import com.littlehorizons.erp.model.ParticipationRole;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.StudentPodiumRanking;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business rules of the events, their participants and the competition positions.
 */
public class EventController {
    private IEventDAO eventDAO;
    private IEventParticipationDAO participationDAO;
    private StudentController studentController;
    private TeacherController teacherController;

    public EventController() {
        eventDAO = new EventDaoImpl();
        participationDAO = new EventParticipationDaoImpl();
        studentController = new StudentController();
        teacherController = new TeacherController();
    }

    public List<Event> listEvents() {
        return eventDAO.listAllEvents();
    }

    /**
     * Active events from today on, the closest first.
     */
    public List<Event> listUpcomingEvents() {
        LocalDate today = LocalDate.now();
        return eventDAO.listAllEvents().stream()
                .filter(Event::isActive)
                .filter(event -> !event.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(Event::getEventDate))
                .collect(Collectors.toList());
    }

    public Event findEvent(int eventId) {
        Event event = eventDAO.findEventById(eventId);
        if (event == null) {
            throw new RuntimeException("No existe un evento con ID " + eventId + ".");
        }
        return event;
    }

    public void registerEvent(Event event) {
        validateEvent(event);
        eventDAO.addEvent(event);
    }

    public void updateEvent(Event event) {
        Event current = findEvent(event.getId());
        validateEvent(event);
        if (current.hasCompetition() && !event.hasCompetition() && hasPositions(event.getId())) {
            throw new RuntimeException("El evento ya tiene posiciones registradas; no se le puede quitar la competencia.");
        }
        eventDAO.updateEvent(event);
    }

    public void deleteEvent(int eventId) {
        findEvent(eventId);
        eventDAO.deleteEvent(eventId);
    }

    private void validateEvent(Event event) {
        if (event.getName() == null || event.getName().isBlank()) {
            throw new RuntimeException("El nombre del evento es obligatorio.");
        }
        if (event.getType() == null || event.getEventDate() == null) {
            throw new RuntimeException("El tipo y la fecha del evento son obligatorios.");
        }
        if (event.getOrganizerTeacherId() != null) {
            teacherController.findActiveTeacher(event.getOrganizerTeacherId());
        }
    }

    private boolean hasPositions(int eventId) {
        return participationDAO.findParticipationsByEvent(eventId).stream()
                .anyMatch(participation -> participation.getPosition() != null);
    }

    public EventParticipation registerParticipant(int eventId, int studentId, ParticipationRole role, String notes) {
        Event event = findEvent(eventId);
        if (!event.isActive()) {
            throw new RuntimeException("El evento '" + event.getName() + "' no está activo.");
        }
        Student student = studentController.findActiveStudent(studentId);
        if (participationDAO.existsParticipation(eventId, studentId)) {
            throw new RuntimeException(student.getFullName() + " ya está registrado en este evento.");
        }
        if (role == ParticipationRole.COMPETITOR && !event.hasCompetition()) {
            throw new RuntimeException("El evento no tiene competencia; elija otro rol.");
        }
        EventParticipation participation = new EventParticipation(eventId, studentId, role, notes);
        participationDAO.addParticipation(participation);
        participation.setStudentName(student.getFullName());
        participation.setEventName(event.getName());
        return participation;
    }

    /**
     * Assigns 1st/2nd/3rd place or "Participante". Only for events with competition,
     * and each podium position can belong to only one student.
     */
    public void assignPosition(int participationId, CompetitionPosition position) {
        EventParticipation participation = participationDAO.findParticipationById(participationId);
        if (participation == null) {
            throw new RuntimeException("No existe una participación con ID " + participationId + ".");
        }
        Event event = findEvent(participation.getEventId());
        if (!event.hasCompetition()) {
            throw new RuntimeException("El evento '" + event.getName() + "' no tiene competencia.");
        }
        if (position != CompetitionPosition.PARTICIPANT
                && participationDAO.isPositionTaken(event.getId(), position, participationId)) {
            throw new RuntimeException("La posición '" + position.getLabel() + "' ya fue asignada en este evento.");
        }
        participationDAO.updatePosition(participationId, position);
    }

    public void removeParticipant(int participationId) {
        if (!participationDAO.deleteParticipation(participationId)) {
            throw new RuntimeException("No existe una participación con ID " + participationId + ".");
        }
    }

    public List<EventParticipation> getParticipants(int eventId) {
        findEvent(eventId);
        return participationDAO.findParticipationsByEvent(eventId);
    }

    public List<EventParticipation> getStudentParticipations(int studentId) {
        studentController.findStudent(studentId);
        return participationDAO.findParticipationsByStudent(studentId);
    }

    /**
     * Number of participations per event type (every type appears, even with zero).
     */
    public Map<EventType, Long> participationByType() {
        Map<Integer, EventType> typeByEvent = new HashMap<>();
        for (Event event : eventDAO.listAllEvents()) {
            typeByEvent.put(event.getId(), event.getType());
        }
        Map<EventType, Long> result = new EnumMap<>(EventType.class);
        for (EventType type : EventType.values()) {
            result.put(type, 0L);
        }
        result.putAll(participationDAO.listAllParticipations().stream()
                .collect(Collectors.groupingBy(participation -> typeByEvent.get(participation.getEventId()),
                        Collectors.counting())));
        return result;
    }

    /**
     * Students with at least one podium, sorted by score (gold 3, silver 2, bronze 1).
     */
    public List<StudentPodiumRanking> podiumRanking(int limit) {
        Map<Integer, List<EventParticipation>> podiumsByStudent = participationDAO.listAllParticipations().stream()
                .filter(EventParticipation::isPodium)
                .collect(Collectors.groupingBy(EventParticipation::getStudentId));

        List<StudentPodiumRanking> ranking = new ArrayList<>();
        for (List<EventParticipation> podiums : podiumsByStudent.values()) {
            EventParticipation first = podiums.get(0);
            ranking.add(new StudentPodiumRanking(first.getStudentId(), first.getStudentName(),
                    countPosition(podiums, CompetitionPosition.FIRST_PLACE),
                    countPosition(podiums, CompetitionPosition.SECOND_PLACE),
                    countPosition(podiums, CompetitionPosition.THIRD_PLACE)));
        }
        return ranking.stream()
                .sorted(Comparator.comparingLong(StudentPodiumRanking::getScore).reversed()
                        .thenComparing(StudentPodiumRanking::getStudentName))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private long countPosition(List<EventParticipation> podiums, CompetitionPosition position) {
        return podiums.stream()
                .filter(participation -> participation.getPosition() == position)
                .count();
    }
}
