package com.studentmanagement.service;

import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.Student;
import com.studentmanagement.model.Course;
import com.studentmanagement.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {
    
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    
    public Enrollment enrollStudent(Student student, Course course) {
        // Check if student is already enrolled
        Optional<Enrollment> existing = enrollmentRepository.findByStudentAndCourse(student, course);
        if (existing.isPresent()) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        
        return enrollmentRepository.save(enrollment);
    }
    
    public Optional<Enrollment> getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id);
    }
    
    public List<Enrollment> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.getStudentEnrollments(studentId);
    }
    
    public List<Enrollment> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.getCourseEnrollments(courseId);
    }
    
    public long countStudentEnrollments(Long studentId) {
        return enrollmentRepository.countEnrollmentsByStudent(studentId);
    }
    
    public long countCourseEnrollments(Long courseId) {
        return enrollmentRepository.countEnrollmentsByCourse(courseId);
    }
    
    public Enrollment updateGrade(Long enrollmentId, String grade) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(enrollmentId);
        if (enrollment.isPresent()) {
            Enrollment e = enrollment.get();
            e.setGrade(grade);
            return enrollmentRepository.save(e);
        }
        return null;
    }
    
    public void dropCourse(Long enrollmentId) {
        enrollmentRepository.deleteById(enrollmentId);
    }
    
    public List<Enrollment> getUngradedEnrollments() {
        return enrollmentRepository.findUngradedEnrollments();
    }
    
    public List<Enrollment> getAllEnrollments() {
        return (List<Enrollment>) enrollmentRepository.findAll();
    }
}
