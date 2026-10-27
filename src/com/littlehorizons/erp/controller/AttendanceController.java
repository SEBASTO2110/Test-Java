package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AttendanceDaoImpl;
import com.littlehorizons.erp.dao.IAttendanceDAO;
import com.littlehorizons.erp.dao.ISubjectEnrollmentDAO;
import com.littlehorizons.erp.dao.ITeachingAssignmentDAO;
import com.littlehorizons.erp.dao.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.dao.TeachingAssignmentDaoImpl;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.model.AcademicPeriod;
import com.littlehorizons.erp.model.AttendanceStatus;
import com.littlehorizons.erp.model.AbsenceAlert;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Daily attendance and absence reports.
 */
public class AttendanceController {
    /** A student with more absences than this in one week is reported. */
    public static final int MAX_WEEKLY_ABSENCES = 3;

    private IAttendanceDAO attendanceDAO;
    private ISubjectEnrollmentDAO subjectEnrollmentDAO;
    private ITeachingAssignmentDAO assignmentDAO;

    public AttendanceController() {
        attendanceDAO = new AttendanceDaoImpl();
        subjectEnrollmentDAO = new SubjectEnrollmentDaoImpl();
        assignmentDAO = new TeachingAssignmentDaoImpl();
    }

    /**
     * Students enrolled in a subject in a period: the list used to take attendance.
     */
    public List<SubjectEnrollment> classList(int subjectId, int academicYear, AcademicPeriod period) {
        List<SubjectEnrollment> students = subjectEnrollmentDAO.findSubjectEnrollmentsBySubject(subjectId,
                academicYear, period);
        if (students.isEmpty()) {
            throw new RuntimeException("No hay estudiantes inscritos en esa materia para el " + period.getLabel()
                    + " de " + academicYear + ".");
        }
        return students;
    }

    public void validateAttendanceDate(LocalDate date) {
        if (date.isAfter(LocalDate.now())) {
            throw new RuntimeException("No se puede registrar asistencia en una fecha futura.");
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new RuntimeException("No hay clases los domingos.");
        }
    }

    public int registerAttendance(int subjectId, int teacherId, LocalDate date, List<Attendance> records) {
        validateAttendanceDate(date);
        boolean teacherAssigned = assignmentDAO.findAssignmentsBySubject(subjectId, date.getYear()).stream()
                .anyMatch(assignment -> assignment.getTeacherId() == teacherId);
        if (!teacherAssigned) {
            throw new RuntimeException("El profesor no tiene asignada esa materia en " + date.getYear() + ".");
        }
        for (Attendance record : records) {
            record.setSubjectId(subjectId);
            record.setDate(date);
            record.setRecordedBy(teacherId);
        }
        return attendanceDAO.saveAttendance(records);
    }

    public List<Attendance> attendanceOfClass(int subjectId, LocalDate date) {
        return attendanceDAO.findAttendanceBySubjectAndDate(subjectId, date);
    }

    public List<Attendance> attendanceOfStudent(int studentId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new RuntimeException("La fecha final no puede ser anterior a la inicial.");
        }
        return attendanceDAO.findAttendanceByStudent(studentId, from, to);
    }

    /**
     * How many times each status appears in a list of attendance records.
     */
    public Map<AttendanceStatus, Long> summarize(List<Attendance> records) {
        Map<AttendanceStatus, Long> summary = new EnumMap<>(AttendanceStatus.class);
        for (AttendanceStatus status : AttendanceStatus.values()) {
            summary.put(status, 0L);
        }
        summary.putAll(records.stream()
                .collect(Collectors.groupingBy(Attendance::getStatus, Collectors.counting())));
        return summary;
    }

    /**
     * Students with more than MAX_WEEKLY_ABSENCES absences in the week (Monday to Sunday)
     * that contains the given day.
     */
    public List<AbsenceAlert> weeklyAbsenceAlerts(LocalDate dayOfWeek) {
        LocalDate monday = dayOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        Map<Integer, List<Attendance>> absencesByStudent = attendanceDAO.findAttendanceByDateRange(monday, sunday)
                .stream()
                .filter(record -> record.getStatus() == AttendanceStatus.ABSENT)
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        List<AbsenceAlert> alerts = new ArrayList<>();
        for (List<Attendance> absences : absencesByStudent.values()) {
            if (absences.size() > MAX_WEEKLY_ABSENCES) {
                Set<String> subjects = absences.stream()
                        .map(Attendance::getSubjectName)
                        .collect(Collectors.toCollection(TreeSet::new));
                Attendance first = absences.get(0);
                alerts.add(new AbsenceAlert(first.getStudentId(), first.getStudentName(), absences.size(), subjects));
            }
        }
        alerts.sort(Comparator.comparingLong(AbsenceAlert::getAbsences).reversed());
        return alerts;
    }
}
