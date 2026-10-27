package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ClubController;
import com.littlehorizons.erp.controller.EventController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.stream.Collectors;

/**
 * Prompts to choose a club or an event. Users can type the ID or 0 to see the list first.
 */
public class ExtracurricularPickers {

    private ClubController clubController;
    private EventController eventController;

    public ExtracurricularPickers() {
        clubController = new ClubController();
        eventController = new EventController();
    }

    public Club pickClub() {
        int id = InputReader.readInt("ID del club (0 = ver lista)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            ConsoleUtils.printTable(new String[]{"ID", "Club", "Ocupación", "Activo"}, clubController.listClubs().stream()
                    .map(club -> new String[]{String.valueOf(club.getId()), club.getName(),
                            club.getActiveMembers() + "/" + club.getMaxCapacity(), Formats.yesNo(club.isActive())})
                    .collect(Collectors.toList()));
            id = InputReader.readId("ID del club");
        }
        Club club = clubController.findClub(id);
        ConsoleUtils.printInfo("» " + club.getName() + " (" + club.getActiveMembers() + "/" + club.getMaxCapacity() + ")");
        return club;
    }

    public Event pickEvent() {
        int id = InputReader.readInt("ID del evento (0 = ver lista)", 0, Integer.MAX_VALUE);
        if (id == 0) {
            ConsoleUtils.printTable(new String[]{"ID", "Evento", "Fecha", "Competencia"}, eventController.listEvents()
                    .stream()
                    .map(event -> new String[]{String.valueOf(event.getId()), event.getName(),
                            Formats.date(event.getEventDate()), Formats.yesNo(event.hasCompetition())})
                    .collect(Collectors.toList()));
            id = InputReader.readId("ID del evento");
        }
        Event event = eventController.findEvent(id);
        ConsoleUtils.printInfo("» " + event.getName() + " - " + Formats.date(event.getEventDate()));
        return event;
    }
}
