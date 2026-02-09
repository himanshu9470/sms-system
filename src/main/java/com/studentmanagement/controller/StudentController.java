// StudentController.java
package com.studentmanagement.controller;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping("/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        // Get current student info
        String username = authentication.getName();
        Student student = studentService.getStudentByUsername(username);
        model.addAttribute("student", student);
        return "student/dashboard";
    }
}