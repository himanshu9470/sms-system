package com.studentmanagement.controller;

import com.studentmanagement.model.Student;
import com.studentmanagement.model.Course;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.CourseService;
import com.studentmanagement.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/courses")
@PreAuthorize("hasRole('STUDENT')")
public class CourseRegistrationController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @GetMapping
    public String availableCourses(Authentication authentication, Model model) {
        String username = authentication.getName();
        Student student = studentService.getStudentByUsername(username);
        
        // Get all available courses
        model.addAttribute("courses", courseService.getAllCourses());
        
        // Get enrolled courses for this student
        model.addAttribute("enrolledCourses", enrollmentService.getStudentEnrollments(student.getStudentId()));
        
        return "student/available-courses";
    }
    
    @GetMapping("/my-courses")
    public String myEnrolledCourses(Authentication authentication, Model model) {
        String username = authentication.getName();
        Student student = studentService.getStudentByUsername(username);
        
        model.addAttribute("enrollments", enrollmentService.getStudentEnrollments(student.getStudentId()));
        return "student/my-courses";
    }
    
    @PostMapping("/register/{courseId}")
    public String registerCourse(@PathVariable Long courseId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            Student student = studentService.getStudentByUsername(username);
            
            courseService.getCourseById(courseId).ifPresentOrElse(
                course -> {
                    try {
                        enrollmentService.enrollStudent(student, course);
                        redirectAttributes.addFlashAttribute("success", 
                            "Successfully enrolled in " + course.getCourseName());
                    } catch (RuntimeException e) {
                        redirectAttributes.addFlashAttribute("error", e.getMessage());
                    }
                },
                () -> redirectAttributes.addFlashAttribute("error", "Course not found!")
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }
        
        return "redirect:/student/courses";
    }
    
    @PostMapping("/drop/{enrollmentId}")
    public String dropCourse(@PathVariable Long enrollmentId,
                            RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.dropCourse(enrollmentId);
            redirectAttributes.addFlashAttribute("success", "Course dropped successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to drop course: " + e.getMessage());
        }
        
        return "redirect:/student/courses/my-courses";
    }
}
