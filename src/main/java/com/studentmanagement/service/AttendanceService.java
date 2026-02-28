package com.studentmanagement.service;

import com.studentmanagement.model.*;
import com.studentmanagement.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    public Attendance markAttendance(Student student, Course course, LocalDate date, Attendance.AttendanceStatus status,
            User markedBy) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setMarkedBy(markedBy);
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceByCourse(Long courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    public List<Attendance> getAttendanceByCourseAndDate(Long courseId, LocalDate date) {
        return attendanceRepository.findByCourseIdAndDate(courseId, date);
    }

    public List<Attendance> getAttendanceByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    public List<Attendance> getAttendanceByStudentAndCourse(Long studentId, Long courseId) {
        return attendanceRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    public boolean isAttendanceMarked(Long courseId, LocalDate date) {
        return attendanceRepository.countByCourseIdAndDate(courseId, date) > 0;
    }
}
