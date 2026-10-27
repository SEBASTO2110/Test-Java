package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClassScheduleDaoImpl;
import com.littlehorizons.erp.dao.IClassScheduleDAO;
import com.littlehorizons.erp.dao.ISubjectDAO;
import com.littlehorizons.erp.dao.ITeacherDAO;
import com.littlehorizons.erp.dao.ITeachingAssignmentDAO;
import com.littlehorizons.erp.dao.SubjectDaoImpl;
import com.littlehorizons.erp.dao.TeacherDaoImpl;
import com.littlehorizons.erp.dao.TeachingAssignmentDaoImpl;
import com.littlehorizons.erp.model.ClassSchedule;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeachingAssignment;
import com.littlehorizons.erp.model.TeacherStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Business rules of the teachers, their subject assignments and their class schedule.
 */
public class TeacherController {
    private static final LocalTime SCHOOL_DAY_START = LocalTime.of(6, 0);
    private static final LocalTime SCHOOL_DAY_END = LocalTime.of(18, 0);

    private ITeacherDAO teacherDAO;
    private ITeachingAssignmentDAO assignmentDAO;
    private IClassScheduleDAO scheduleDAO;
    private ISubjectDAO subjectDAO;

    public TeacherController() {
        teacherDAO = new TeacherDaoImpl();
        assignmentDAO = new TeachingAssignmentDaoImpl();
        scheduleDAO = new ClassScheduleDaoImpl();
        subjectDAO = new SubjectDaoImpl();
    }

    // ---------------------------------------------------------------- teachers

    public void registerTeacher(Teacher teacher) {
        validateTeacher(teacher);
        if (teacherDAO.findTeacherByDocument(teacher.getDocumentNumber()) != null) {
            throw new RuntimeException("Ya existe un profesor con el documento " + teacher.getDocumentNumber() + ".");
        }
        if (teacherDAO.findTeacherByEmail(teacher.getEmail()) != null) {
            throw new RuntimeException("El correo " + teacher.getEmail() + " ya está registrado.");
        }
        teacher.setStatus(TeacherStatus.ACTIVE);
        teacherDAO.addTeacher(teacher);
    }

    public void updateTeacher(Teacher teacher) {
        validateTeacher(teacher);
        Teacher sameDocument = teacherDAO.findTeacherByDocument(teacher.getDocumentNumber());
        if (sameDocument != null && sameDocument.getId() != teacher.getId()) {
            throw new RuntimeException("El documento ya pertenece a otro profesor.");
        }
        Teacher sameEmail = teacherDAO.findTeacherByEmail(teacher.getEmail());
        if (sameEmail != null && sameEmail.getId() != teacher.getId()) {
            throw new RuntimeException("El correo ya pertenece a otro profesor.");
        }
        teacherDAO.updateTeacher(teacher);
    }

    public Teacher findTeacher(int teacherId) {
        Teacher teacher = teacherDAO.findTeacherById(teacherId);
        if (teacher == null) {
            throw new RuntimeException("No existe un profesor con ID " + teacherId + ".");
        }
        return teacher;
    }

    /**
     * Used by the other modules: only active teachers can supervise clubs or borrow resources.
     */
    public Teacher findActiveTeacher(int teacherId) {
        Teacher teacher = findTeacher(teacherId);
        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new RuntimeException("El profesor " + teacher.getFullName() + " no está activo (estado: "
                    + teacher.getStatus().getLabel() + ").");
        }
        return teacher;
    }

    public List<Teacher> listTeachers() {
        return teacherDAO.listAllTeachers();
    }

    public List<Teacher> search(String text) {
        return teacherDAO.findTeachersByNameOrSpecialization(text);
    }

    public void changeStatus(int teacherId, TeacherStatus status) {
        findTeacher(teacherId);
        teacherDAO.updateTeacherStatus(teacherId, status);
    }

    public void deleteTeacher(int teacherId) {
        findTeacher(teacherId);
        teacherDAO.deleteTeacher(teacherId);
    }

    // ---------------------------------------------------------------- assignments

    public TeachingAssignment assignSubject(int teacherId, int subjectId, int academicYear, String section) {
        findActiveTeacher(teacherId);
        Subject subject = subjectDAO.findSubjectById(subjectId);
        if (subject == null) {
            throw new RuntimeException("No existe una materia con ID " + subjectId + ".");
        }
        if (!subject.isActive()) {
            throw new RuntimeException("La materia está inactiva.");
        }
        boolean alreadyAssigned = assignmentDAO.findAssignmentsByTeacher(teacherId, academicYear).stream()
                .anyMatch(assignment -> assignment.getSubjectId() == subjectId
                        && section.equalsIgnoreCase(assignment.getSection()));
        if (alreadyAssigned) {
            throw new RuntimeException("El profesor ya tiene esa materia y sección en " + academicYear + ".");
        }
        TeachingAssignment assignment = new TeachingAssignment();
        assignment.setTeacherId(teacherId);
        assignment.setSubjectId(subjectId);
        assignment.setAcademicYear(academicYear);
        assignment.setSection(section.toUpperCase());
        assignmentDAO.addAssignment(assignment);
        return assignment;
    }

    public TeachingAssignment findAssignment(int assignmentId) {
        TeachingAssignment assignment = assignmentDAO.findAssignmentById(assignmentId);
        if (assignment == null) {
            throw new RuntimeException("No existe una asignación con ID " + assignmentId + ".");
        }
        return assignment;
    }

    public List<TeachingAssignment> assignmentsOf(int teacherId, int academicYear) {
        findTeacher(teacherId);
        return assignmentDAO.findAssignmentsByTeacher(teacherId, academicYear);
    }

    public List<TeachingAssignment> assignmentsOfYear(int academicYear) {
        return assignmentDAO.findAssignmentsByYear(academicYear);
    }

    public void removeAssignment(int assignmentId) {
        findAssignment(assignmentId);
        assignmentDAO.deleteAssignment(assignmentId);
    }

    public boolean teachesSubject(int teacherId, int subjectId, int academicYear) {
        return assignmentDAO.findAssignmentsBySubject(subjectId, academicYear).stream()
                .anyMatch(assignment -> assignment.getTeacherId() == teacherId);
    }

    // ---------------------------------------------------------------- schedule

    /**
     * Adds a class block to an assignment. It is rejected if the teacher already has a class at that time.
     */
    public void addSchedule(ClassSchedule schedule) {
        TeachingAssignment assignment = findAssignment(schedule.getAssignmentId());
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new RuntimeException("La hora final debe ser posterior a la hora inicial.");
        }
        if (schedule.getStartTime().isBefore(SCHOOL_DAY_START) || schedule.getEndTime().isAfter(SCHOOL_DAY_END)) {
            throw new RuntimeException("El horario debe estar entre " + SCHOOL_DAY_START + " y " + SCHOOL_DAY_END + ".");
        }
        List<ClassSchedule> teacherSchedule = scheduleDAO.findSchedulesByTeacher(assignment.getTeacherId(),
                assignment.getAcademicYear());
        for (ClassSchedule other : teacherSchedule) {
            if (schedule.overlaps(other)) {
                throw new RuntimeException("Conflicto de horario: el profesor ya tiene " + other.getAssignmentDescription()
                        + " el " + other.getDay().getLabel() + " de " + other.getStartTime() + " a "
                        + other.getEndTime() + ".");
            }
        }
        scheduleDAO.addSchedule(schedule);
    }

    public List<ClassSchedule> scheduleOf(int teacherId, int academicYear) {
        findTeacher(teacherId);
        return scheduleDAO.findSchedulesByTeacher(teacherId, academicYear);
    }

    public void removeSchedule(int scheduleId) {
        if (!scheduleDAO.deleteSchedule(scheduleId)) {
            throw new RuntimeException("No existe un horario con ID " + scheduleId + ".");
        }
    }

    private void validateTeacher(Teacher teacher) {
        if (teacher == null) {
            throw new RuntimeException("Error, el profesor está vacío.");
        }
        if (teacher.getHireDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de contratación no puede ser futura.");
        }
    }
}
