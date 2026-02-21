package com.studentmanagement.repository;

import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.Course;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends CrudRepository<Enrollment, Long> {
    
    List<Enrollment> findByStudent(Student student);
    
    List<Enrollment> findByCourse(Course course);
    
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId")
    long countEnrollmentsByStudent(@Param("studentId") Long studentId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countEnrollmentsByCourse(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId ORDER BY e.enrollmentDate DESC")
    List<Enrollment> getStudentEnrollments(@Param("studentId") Long studentId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId ORDER BY e.enrollmentDate")
    List<Enrollment> getCourseEnrollments(@Param("courseId") Long courseId);
    
    @Query("SELECT e FROM Enrollment e WHERE e.grade IS NULL")
    List<Enrollment> findUngradedEnrollments();
}
