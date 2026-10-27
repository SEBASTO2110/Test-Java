package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ClubController;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubEnrollment;
import com.littlehorizons.erp.model.ClubOccupancy;
import com.littlehorizons.erp.utils.ConsoleUtils;
import com.littlehorizons.erp.utils.Formats;
import com.littlehorizons.erp.utils.InputReader;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clubs and club memberships. The view only asks and shows data; rules live in ClubController.
 */
public class ClubView {

    private ClubController clubController;
    private ExtracurricularPickers extracurricularPickers;
    private AcademicPickers pickers;

    public ClubView() {
        clubController = new ClubController();
        extracurricularPickers = new ExtracurricularPickers();
        pickers = new AcademicPickers();
    }

    public void showMenu() {
        int option;
        do {
            ConsoleUtils.printHeader("Clubes");
            System.out.println("""
                1. Registrar club
                2. Listar clubes
                3. Actualizar club
                4. Activar / desactivar club
                5. Eliminar club
                6. Inscribir estudiante en un club
                7. Retirar estudiante de un club
                8. Ver miembros de un club
                9. Ver clubes de un estudiante
                10. Reporte de ocupación de clubes
                0. Volver""");
            option = InputReader.readMenuOption(10);
            try {
                switch (option) {
                    case 1:
                        ConsoleUtils.printActionHeader("Registrar club");
                        registerClub();
                        break;
                    case 2:
                        ConsoleUtils.printActionHeader("Listar clubes");
                        listClubs();
                        break;
                    case 3:
                        ConsoleUtils.printActionHeader("Actualizar club");
                        updateClub();
                        break;
                    case 4:
                        ConsoleUtils.printActionHeader("Activar / desactivar club");
                        toggleClub();
                        break;
                    case 5:
                        ConsoleUtils.printActionHeader("Eliminar club");
                        deleteClub();
                        break;
                    case 6:
                        ConsoleUtils.printActionHeader("Inscribir estudiante en un club");
                        enrollStudent();
                        break;
                    case 7:
                        ConsoleUtils.printActionHeader("Retirar estudiante de un club");
                        withdrawStudent();
                        break;
                    case 8:
                        ConsoleUtils.printActionHeader("Ver miembros de un club");
                        showMembers();
                        break;
                    case 9:
                        ConsoleUtils.printActionHeader("Ver clubes de un estudiante");
                        showStudentClubs();
                        break;
                    case 10:
                        ConsoleUtils.printActionHeader("Reporte de ocupación de clubes");
                        showOccupancyReport();
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

    private void registerClub() {
        Club club = new Club(
                InputReader.readText("Nombre del club", 100),
                InputReader.readOptionalText("Descripción", 1000),
                readOptionalSupervisor(),
                InputReader.readInt("Cupo máximo", ClubController.MIN_CAPACITY, ClubController.MAX_CAPACITY),
                InputReader.readOptionalText("Horario de reunión (ej: Martes 3:00-5:00 PM)", 100));
        clubController.registerClub(club);
        int id = club.getId();
        ConsoleUtils.printSuccess("Club '" + club.getName() + "' registrado con ID " + id + ".");
    }

    private Integer readOptionalSupervisor() {
        if (!InputReader.confirm("¿Desea asignar un profesor encargado?")) {
            return null;
        }
        return pickers.pickTeacher().getId();
    }

    private void listClubs() {
        ConsoleUtils.printTable(new String[]{"ID", "Club", "Profesor encargado", "Horario", "Miembros", "Cupo", "Activo"},
                clubController.listClubs().stream()
                        .map(club -> new String[]{String.valueOf(club.getId()), club.getName(),
                                Formats.text(club.getSupervisorName()), Formats.text(club.getMeetingSchedule()),
                                String.valueOf(club.getActiveMembers()), String.valueOf(club.getMaxCapacity()),
                                Formats.yesNo(club.isActive())})
                        .collect(Collectors.toList()));
    }

    private void updateClub() {
        Club club = extracurricularPickers.pickClub();
        ConsoleUtils.printInfo("Presione Enter para conservar el valor actual.");
        club.setName(InputReader.readTextOrKeep("Nombre", club.getName(), 100));
        club.setDescription(InputReader.readTextOrKeep("Descripción", club.getDescription(), 1000));
        club.setMeetingSchedule(InputReader.readTextOrKeep("Horario", club.getMeetingSchedule(), 100));
        if (InputReader.confirm("Cupo actual: " + club.getMaxCapacity() + ". ¿Desea cambiarlo?")) {
            club.setMaxCapacity(InputReader.readInt("Nuevo cupo máximo", ClubController.MIN_CAPACITY,
                    ClubController.MAX_CAPACITY));
        }
        if (InputReader.confirm("Profesor encargado: " + Formats.text(club.getSupervisorName()) + ". ¿Desea cambiarlo?")) {
            club.setSupervisorTeacherId(readOptionalSupervisor());
        }
        clubController.updateClub(club);
        ConsoleUtils.printSuccess("Club actualizado.");
    }

    private void toggleClub() {
        Club club = extracurricularPickers.pickClub();
        boolean activate = !club.isActive();
        if (InputReader.confirm("El club está " + (club.isActive() ? "ACTIVO" : "INACTIVO") + ". ¿Desea "
                + (activate ? "activarlo" : "desactivarlo") + "?")) {
            clubController.changeActive(club.getId(), activate);
            ConsoleUtils.printSuccess("Club " + (activate ? "activado" : "desactivado") + ".");
        }
    }

    private void deleteClub() {
        Club club = extracurricularPickers.pickClub();
        if (InputReader.confirm("¿Seguro que desea eliminar el club '" + club.getName() + "' y su historial?")) {
            clubController.deleteClub(club.getId());
            ConsoleUtils.printSuccess("Club eliminado.");
        }
    }

    private void enrollStudent() {
        Student student = pickers.pickStudent();
        Club club = extracurricularPickers.pickClub();
        LocalDate joinDate = InputReader.readDateOrToday("Fecha de ingreso");
        ClubEnrollment enrollment = clubController.enrollStudent(student.getId(), club.getId(), joinDate);
        ConsoleUtils.printSuccess(enrollment.getStudentName() + " inscrito en '" + enrollment.getClubName()
                + "' (membresía #" + enrollment.getId() + ").");
    }

    private void withdrawStudent() {
        Club club = extracurricularPickers.pickClub();
        List<ClubEnrollment> members = clubController.getMembers(club.getId(), true);
        printMembers(members);
        if (members.isEmpty()) {
            return;
        }
        int enrollmentId = InputReader.readId("ID de la membresía a retirar");
        LocalDate date = InputReader.readDateOrToday("Fecha de retiro");
        String reason = InputReader.readOptionalText("Motivo del retiro", 500);
        clubController.withdrawStudent(enrollmentId, date, reason);
        ConsoleUtils.printSuccess("Estudiante retirado del club.");
    }

    private void showMembers() {
        Club club = extracurricularPickers.pickClub();
        boolean onlyActive = InputReader.confirm("¿Mostrar solo los miembros activos?");
        printMembers(clubController.getMembers(club.getId(), onlyActive));
    }

    private void printMembers(List<ClubEnrollment> members) {
        ConsoleUtils.printTable(new String[]{"Membresía", "ID Est.", "Estudiante", "Ingreso", "Estado", "Retiro"},
                members.stream()
                        .map(member -> new String[]{String.valueOf(member.getId()), String.valueOf(member.getStudentId()),
                                member.getStudentName(), Formats.date(member.getJoinDate()),
                                member.getStatus().getLabel(), Formats.date(member.getWithdrawalDate())})
                        .collect(Collectors.toList()));
    }

    private void showStudentClubs() {
        Student student = pickers.pickStudent();
        ConsoleUtils.printTable(new String[]{"Membresía", "Club", "Ingreso", "Estado", "Retiro", "Motivo"},
                clubController.getStudentClubs(student.getId()).stream()
                        .map(member -> new String[]{String.valueOf(member.getId()), member.getClubName(),
                                Formats.date(member.getJoinDate()), member.getStatus().getLabel(),
                                Formats.date(member.getWithdrawalDate()), Formats.text(member.getWithdrawalReason())})
                        .collect(Collectors.toList()));
    }

    /**
     * Also available from the reports menu.
     */
    public void showOccupancyReport() {
        List<ClubOccupancy> report = clubController.occupancyReport();
        ConsoleUtils.printTable(new String[]{"Club", "Profesor encargado", "Miembros", "Cupo", "Ocupación"},
                report.stream()
                        .map(row -> new String[]{row.getClubName(), Formats.text(row.getSupervisorName()),
                                String.valueOf(row.getActiveMembers()), String.valueOf(row.getMaxCapacity()),
                                Formats.percentage(row.getOccupancyPercentage())})
                        .collect(Collectors.toList()));
        long fullClubs = report.stream().filter(row -> row.getActiveMembers() >= row.getMaxCapacity()).count();
        int totalMembers = report.stream().mapToInt(ClubOccupancy::getActiveMembers).sum();
        ConsoleUtils.printInfo("Membresías activas: " + totalMembers + " | Clubes llenos: " + fullClubs);
    }
}
