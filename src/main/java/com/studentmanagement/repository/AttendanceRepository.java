package com.studentmanagement.repository;

import com.studentmanagement.model.Attendance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends CrudRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId ORDER BY a.attendanceDate DESC, a.student.firstName ASC")
    List<Attendance> findByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<Attendance> findByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.student.studentId = :studentId ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Attendance a WHERE a.student.studentId = :studentId AND a.course.id = :courseId ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    long countByCourseIdAndDate(@Param("courseId") Long courseId, @Param("date") LocalDate date);
}
