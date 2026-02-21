// AdminController.java
package com.studentmanagement.controller;

import com.studentmanagement.model.Course;
import com.studentmanagement.model.Enrollment;
import com.studentmanagement.model.Student;
import com.studentmanagement.service.CourseService;
import com.studentmanagement.service.EnrollmentService;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private EnrollmentService enrollmentService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("studentCount", studentService.getStudentCount());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("courseCount", courseService.getTotalCourseCount());
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/dashboard";
    }
    
    @GetMapping("/students")
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/students";
    }
    
    @GetMapping("/students/add")
    public String showAddStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "admin/add-student";
    }
    
    @PostMapping("/students/add")
    public String addStudent(@ModelAttribute Student student,
                            @RequestParam String username,
                            @RequestParam String password,
                            RedirectAttributes redirectAttributes) {
        
        try {
            studentService.createStudent(student, username, password);
            redirectAttributes.addFlashAttribute("success", 
                "Student added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to add student: " + e.getMessage());
        }
        
        return "redirect:/admin/students";
    }
    
    @GetMapping("/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        studentService.getStudentById(id).ifPresent(student -> 
            model.addAttribute("student", student));
        return "admin/edit-student";
    }
    
    @PostMapping("/students/edit/{id}")
    public String updateStudent(@PathVariable Long id,
                               @ModelAttribute Student student,
                               RedirectAttributes redirectAttributes) {
        
        studentService.updateStudent(id, student);
        redirectAttributes.addFlashAttribute("success", 
            "Student updated successfully!");
        return "redirect:/admin/students";
    }
    
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        
        if (studentService.deleteStudent(id)) {
            redirectAttributes.addFlashAttribute("success", 
                "Student deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", 
                "Failed to delete student!");
        }
        
        return "redirect:/admin/students";
    }
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
    
    // ========== COURSE MANAGEMENT ==========
    
    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin/courses";
    }
    
    @GetMapping("/courses/add")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        return "admin/add-course";
    }
    
    @PostMapping("/courses/add")
    public String addCourse(@ModelAttribute Course course,
                           RedirectAttributes redirectAttributes) {
        try {
            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("success", "Course added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add course: " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        courseService.getCourseById(id).ifPresent(course -> 
            model.addAttribute("course", course));
        return "admin/edit-course";
    }
    
    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id,
                              @ModelAttribute Course course,
                              RedirectAttributes redirectAttributes) {
        courseService.updateCourse(id, course);
        redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        return "redirect:/admin/courses";
    }
    
    @GetMapping("/courses/{id}/enrollments")
    public String viewCourseEnrollments(@PathVariable Long id, Model model) {
        courseService.getCourseById(id).ifPresent(course -> {
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(id);
            model.addAttribute("course", course);
            model.addAttribute("enrollments", enrollments);
        });
        return "admin/course-enrollments";
    }
    
    // ========== ENROLLMENT/REGISTRATION MANAGEMENT ==========
    
    @GetMapping("/enrollments")
    public String listAllEnrollments(Model model) {
        model.addAttribute("enrollments", enrollmentService.getAllEnrollments());
        return "admin/enrollments";
    }
    
    @PostMapping("/enrollments/{id}/grade")
    public String updateEnrollmentGrade(@PathVariable Long id,
                                       @RequestParam String grade,
                                       RedirectAttributes redirectAttributes) {
        enrollmentService.updateGrade(id, grade);
        redirectAttributes.addFlashAttribute("success", "Grade updated successfully!");
        return "redirect:/admin/enrollments";
    }
    
    // ========== REPORT GENERATION ==========
    
    @GetMapping("/reports")
    public String reportsPage(Model model) {
        return "admin/reports";
    }
    
    @GetMapping("/reports/student-report")
    public String studentReport(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/student-report";
    }
    
    @GetMapping("/reports/course-report")
    public String courseReport(Model model) {
        List<Course> courses = courseService.getAllCourses();
        courses.forEach(course -> {
            long enrollmentCount = enrollmentService.countCourseEnrollments(course.getId());
            // Note: You may want to create a DTO to carry this information
        });
        model.addAttribute("courses", courses);
        return "admin/course-report";
    }
    
    @GetMapping("/reports/enrollment-report")
    public String enrollmentReport(Model model) {
        model.addAttribute("enrollments", enrollmentService.getAllEnrollments());
        model.addAttribute("ungradedEnrollments", enrollmentService.getUngradedEnrollments());
        return "admin/enrollment-report";
    }
    
    @GetMapping("/reports/academic-summary")
    public String academicSummaryReport(Model model) {
        model.addAttribute("studentCount", studentService.getStudentCount());
        model.addAttribute("courseCount", courseService.getTotalCourseCount());
        model.addAttribute("totalEnrollments", enrollmentService.getAllEnrollments().size());
        model.addAttribute("ungradedEnrollments", enrollmentService.getUngradedEnrollments().size());
        return "admin/academic-summary";
    }
    
    // ========== SETTINGS ==========
    
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        return "admin/settings";
    }
    
    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String semester,
                                @RequestParam String academicYear,
                                RedirectAttributes redirectAttributes) {
        // Store settings in session or database as needed
        redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/admin/settings";
    }
}
