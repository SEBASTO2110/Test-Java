package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Event;

import java.util.List;

public interface IEventDAO {
    void addEvent(Event event);

    List<Event> listAllEvents();

    Event findEventById(int id);

    void updateEvent(Event event);

    void deleteEvent(int id);
}
