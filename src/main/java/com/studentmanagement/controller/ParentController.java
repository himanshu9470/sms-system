package com.studentmanagement.controller;

import com.studentmanagement.model.*;
import com.studentmanagement.repository.ParentStudentRepository;
import com.studentmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/parent")
@PreAuthorize("hasRole('PARENT')")
public class ParentController {

    @Autowired
    private UserService userService;

    @Autowired
    private ParentStudentRepository parentStudentRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User parent = userService.getUserByUsername(authentication.getName()).orElse(null);
        if (parent == null)
            return "redirect:/login";

        List<ParentStudent> links = parentStudentRepository.findByParentId(parent.getId());
        List<Student> children = links.stream().map(ParentStudent::getStudent).collect(Collectors.toList());

        model.addAttribute("children", children);
        model.addAttribute("parentLinks", links);
        model.addAttribute("notificationCount", notificationService.getUnreadCount(parent.getId()));
        return "parent/dashboard";
    }

    @GetMapping("/attendance/{studentId}")
    public String viewAttendance(@PathVariable Long studentId, Authentication authentication, Model model) {
        User parent = userService.getUserByUsername(authentication.getName()).orElse(null);
        if (parent == null)
            return "redirect:/login";

        model.addAttribute("attendanceRecords", attendanceService.getAttendanceByStudent(studentId));
        return "parent/attendance";
    }

    @GetMapping("/grades/{studentId}")
    public String viewGrades(@PathVariable Long studentId, Authentication authentication, Model model) {
        User parent = userService.getUserByUsername(authentication.getName()).orElse(null);
        if (parent == null)
            return "redirect:/login";

        model.addAttribute("enrollments", enrollmentService.getStudentEnrollments(studentId));
        return "parent/grades";
    }
}
