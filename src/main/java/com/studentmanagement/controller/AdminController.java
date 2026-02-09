// AdminController.java
package com.studentmanagement.controller;

import com.studentmanagement.model.Student;
import com.studentmanagement.service.StudentService;
import com.studentmanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("studentCount", studentService.getStudentCount());
        model.addAttribute("userCount", userService.getAllUsers().size());
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
}