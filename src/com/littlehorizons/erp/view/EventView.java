package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.EventController;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.CompetitionPosition;
import com.littlehorizons.erp.model.EventType;
import com.littlehorizons.erp.model.ParticipationRole;
import com.littlehorizons.erp.model.Event;
import com.littlehorizons.erp.model.EventParticipation;
import com.littlehorizons.erp.model.StudentPodiumRanking;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Events, participants and competition results.
 */
public class EventView {

    private static final int RANKING_SIZE = 10;

    private EventController eventController;
    private ExtracurricularPickers extracurricularPickers;
    private AcademicPickers pickers;

    public EventView() {
        eventController = new EventController();
        extracurricularPickers = new ExtracurricularPickers();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Eventos");
            System.out.println("""
                1. Registrar evento
                2. Listar todos los eventos
                3. Ver próximos eventos
                4. Actualizar evento
                5. Eliminar evento
                6. Registrar participante en un evento
                7. Asignar posición (eventos con competencia)
                8. Quitar participante de un evento
                9. Ver participantes de un evento
                10. Ver eventos de un estudiante
                11. Reporte de participación por tipo de evento
                12. Ranking de estudiantes ganadores
                0. Volver""");
            option = InputReader.readMenuOption(12);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar evento");
                        registerEvent();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar todos los eventos");
                        printEvents(eventController.listEvents());
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Ver próximos eventos");
                        printEvents(eventController.listUpcomingEvents());
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Actualizar evento");
                        updateEvent();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Eliminar evento");
                        deleteEvent();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Registrar participante en un evento");
                        registerParticipant();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Asignar posición (eventos con competencia)");
                        assignPosition();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Quitar participante de un evento");
                        removeParticipant();
                        break;
                    case 9:
                        ConsoleUtils.printActionHeader("Ver participantes de un evento");
                        showParticipants();
                        break;
                    case 10:
                        ConsoleUtils.printActionHeader("Ver eventos de un estudiante");
                        showStudentEvents();
                        break;
                    case 11:
                        ConsoleUtils.printActionHeader("Reporte de participación por tipo de evento");
                        showParticipationByType();
                        break;
                    case 12:
                        ConsoleUtils.printActionHeader("Ranking de estudiantes ganadores");
                        showRanking();
                        break;
                    case 0:
                        break;
                    default:
                        ConsoleUtils.printError("Opción inválida.");
                }
            } catch (RuntimeException e) {
                ConsoleUtils.printException(e);
            }
            if (option != 0) {
                ConsoleUtils.pause();
            }
        } while (option != 0);
    }

    private void registerEvent() {
        Event event = new Event(
                InputReader.readText("Nombre del evento", 150),
                InputReader.readOptionalText("Descripción", 1000),
                InputReader.readOption("Tipo de evento", EventType.values(), EventType::getLabel),
                InputReader.readDate("Fecha del evento"),
                InputReader.readOptionalText("Lugar", 150),
                InputReader.confirm("¿El evento tiene competencia con posiciones?"),
                readOptionalOrganizer());
        eventController.registerEvent(event);
        int id = event.getId();
        ConsoleUtils.printSuccess("Evento '" + event.getName() + "' registrado con ID " + id + ".");
    }

    private Integer readOptionalOrganizer() {
        if (!InputReader.confirm("¿Desea asignar un profesor organizador?")) {
            return null;
        }
        return pickers.pickTeacher().getId();
    }

    private void printEvents(List<Event> events) {
        ConsoleUtils.printTable(new String[]{"ID", "Evento", "Tipo", "Fecha", "Lugar", "Compet.", "Organizador",
                        "Particip.", "Activo"},
                events.stream()
                        .map(event -> new String[]{String.valueOf(event.getId()), event.getName(),
                                event.getType().getLabel(), Formats.date(event.getEventDate()),
                                Formats.text(event.getLocation()), Formats.yesNo(event.hasCompetition()),
                                Formats.text(event.getOrganizerName()), String.valueOf(event.getParticipantCount()),
                                Formats.yesNo(event.isActive())})
                        .collect(Collectors.toList()));
    }

    private void updateEvent() {
        Event event = extracurricularPickers.pickEvent();
        ConsoleUtils.printInfo("Presione Enter para conservar el valor actual.");
        event.setName(InputReader.readTextOrKeep("Nombre", event.getName(), 150));
        event.setDescription(InputReader.readTextOrKeep("Descripción", event.getDescription(), 1000));
        event.setLocation(InputReader.readTextOrKeep("Lugar", event.getLocation(), 150));
        if (InputReader.confirm("Tipo: " + event.getType().getLabel() + ", fecha: " + Formats.date(event.getEventDate())
                + ". ¿Desea cambiarlos?")) {
            event.setType(InputReader.readOption("Tipo de evento", EventType.values(), EventType::getLabel));
            event.setEventDate(InputReader.readDate("Fecha del evento"));
        }
        event.setHasCompetition(InputReader.confirm("¿Tiene competencia? (actual: "
                + Formats.yesNo(event.hasCompetition()) + ")"));
        event.setActive(InputReader.confirm("¿Evento activo? (actual: " + Formats.yesNo(event.isActive()) + ")"));
        if (InputReader.confirm("Organizador: " + Formats.text(event.getOrganizerName()) + ". ¿Desea cambiarlo?")) {
            event.setOrganizerTeacherId(readOptionalOrganizer());
        }
        eventController.updateEvent(event);
        ConsoleUtils.printSuccess("Evento actualizado.");
    }

    private void deleteEvent() {
        Event event = extracurricularPickers.pickEvent();
        if (InputReader.confirm("¿Eliminar '" + event.getName() + "' y sus " + event.getParticipantCount()
                + " participantes?")) {
            eventController.deleteEvent(event.getId());
            ConsoleUtils.printSuccess("Evento eliminado.");
        }
    }

    private void registerParticipant() {
        Event event = extracurricularPickers.pickEvent();
        Student student = pickers.pickStudent();
        ParticipationRole role = InputReader.readOption("Rol de participación", ParticipationRole.values(), ParticipationRole::getLabel);
        String notes = InputReader.readOptionalText("Observaciones", 500);
        EventParticipation participation = eventController.registerParticipant(event.getId(), student.getId(), role, notes);
        ConsoleUtils.printSuccess(participation.getStudentName() + " registrado en '" + participation.getEventName()
                + "' como " + role.getLabel() + ".");
    }

    private void assignPosition() {
        Event event = extracurricularPickers.pickEvent();
        List<EventParticipation> participants = eventController.getParticipants(event.getId());
        printParticipants(participants);
        if (participants.isEmpty()) {
            return;
        }
        int participationId = InputReader.readId("ID de la participación");
        CompetitionPosition position = InputReader.readOption("Posición obtenida", CompetitionPosition.values(), CompetitionPosition::getLabel);
        eventController.assignPosition(participationId, position);
        ConsoleUtils.printSuccess("Posición '" + position.getLabel() + "' registrada.");
    }

    private void removeParticipant() {
        Event event = extracurricularPickers.pickEvent();
        List<EventParticipation> participants = eventController.getParticipants(event.getId());
        printParticipants(participants);
        if (participants.isEmpty()) {
            return;
        }
        eventController.removeParticipant(InputReader.readId("ID de la participación a quitar"));
        ConsoleUtils.printSuccess("Participante eliminado del evento.");
    }

    private void showParticipants() {
        Event event = extracurricularPickers.pickEvent();
        printParticipants(eventController.getParticipants(event.getId()));
    }

    private void printParticipants(List<EventParticipation> participants) {
        ConsoleUtils.printTable(new String[]{"Particip.", "ID Est.", "Estudiante", "Rol", "Posición", "Observaciones"},
                participants.stream()
                        .map(item -> new String[]{String.valueOf(item.getId()), String.valueOf(item.getStudentId()),
                                item.getStudentName(), item.getRole().getLabel(),
                                item.getPosition() == null ? "-" : item.getPosition().getLabel(),
                                Formats.text(item.getNotes())})
                        .collect(Collectors.toList()));
    }

    private void showStudentEvents() {
        Student student = pickers.pickStudent();
        ConsoleUtils.printTable(new String[]{"Evento", "Rol", "Posición"},
                eventController.getStudentParticipations(student.getId()).stream()
                        .map(item -> new String[]{item.getEventName(), item.getRole().getLabel(),
                                item.getPosition() == null ? "-" : item.getPosition().getLabel()})
                        .collect(Collectors.toList()));
    }

    /**
     * Also available from the reports menu.
     */
    public void showParticipationByType() {
        Map<EventType, Long> report = eventController.participationByType();
        long total = report.values().stream().mapToLong(Long::longValue).sum();
        ConsoleUtils.printTable(new String[]{"Tipo de evento", "Participaciones", "Porcentaje"},
                report.entrySet().stream()
                        .map(entry -> new String[]{entry.getKey().getLabel(), String.valueOf(entry.getValue()),
                                Formats.percentage(total == 0 ? 0 : entry.getValue() * 100.0 / total)})
                        .collect(Collectors.toList()));
    }

    /**
     * Also available from the reports menu.
     */
    public void showRanking() {
        List<StudentPodiumRanking> ranking = eventController.podiumRanking(RANKING_SIZE);
        ConsoleUtils.printTable(new String[]{"#", "Estudiante", "1er", "2do", "3er", "Podios", "Puntos"},
                IntStream.range(0, ranking.size())
                        .mapToObj(index -> {
                            StudentPodiumRanking row = ranking.get(index);
                            return new String[]{String.valueOf(index + 1), row.getStudentName(),
                                    String.valueOf(row.getFirstPlaces()), String.valueOf(row.getSecondPlaces()),
                                    String.valueOf(row.getThirdPlaces()), String.valueOf(row.getTotalPodiums()),
                                    String.valueOf(row.getScore())};
                        })
                        .collect(Collectors.toList()));
    }
}
