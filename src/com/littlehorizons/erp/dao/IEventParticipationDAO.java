package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.CompetitionPosition;
import com.littlehorizons.erp.model.EventParticipation;

import java.util.List;

public interface IEventParticipationDAO {
    void addParticipation(EventParticipation participation);

    EventParticipation findParticipationById(int id);

    List<EventParticipation> findParticipationsByEvent(int eventId);

    List<EventParticipation> findParticipationsByStudent(int studentId);

    List<EventParticipation> listAllParticipations();

    boolean existsParticipation(int eventId, int studentId);

    boolean isPositionTaken(int eventId, CompetitionPosition position, int excludedParticipationId);

    void updatePosition(int participationId, CompetitionPosition position);

    boolean deleteParticipation(int id);
}
