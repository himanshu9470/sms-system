package com.studentmanagement.controller;

import com.studentmanagement.model.*;
import com.studentmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY', 'STUDENT')")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TimetableService timetableService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Student student = studentService.getStudentByUsername(authentication.getName());
        if (student == null)
            return "redirect:/login";

        User user = userService.getUserByUsername(authentication.getName()).orElse(null);

        // Enrollments
        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student.getStudentId());
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("totalCourses", enrollments.size());

        // CGPA
        model.addAttribute("cgpa", student.getCgpa() != null ? student.getCgpa() : 0.0);

        // Attendance stats
        List<Attendance> allAttendance = attendanceService.getAttendanceByStudent(student.getStudentId());
        long totalClasses = allAttendance.size();
        long presentClasses = allAttendance.stream().filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT
                || a.getStatus() == Attendance.AttendanceStatus.LATE).count();
        double attendancePct = totalClasses > 0 ? (presentClasses * 100.0 / totalClasses) : 0;
        model.addAttribute("attendancePct", String.format("%.1f", attendancePct));
        model.addAttribute("totalClasses", totalClasses);
        model.addAttribute("presentClasses", presentClasses);

        // Subject-wise attendance
        Map<String, Map<String, Long>> subjectAttendance = new LinkedHashMap<>();
        Map<Long, String> courseNames = new HashMap<>();
        enrollments.forEach(e -> courseNames.put(e.getCourse().getId(), e.getCourse().getCourseName()));
        for (Map.Entry<Long, String> entry : courseNames.entrySet()) {
            List<Attendance> courseAtt = attendanceService.getAttendanceByStudentAndCourse(student.getStudentId(),
                    entry.getKey());
            long total = courseAtt.size();
            long present = courseAtt.stream().filter(a -> a.getStatus() != Attendance.AttendanceStatus.ABSENT).count();
            Map<String, Long> stats = new LinkedHashMap<>();
            stats.put("total", total);
            stats.put("present", present);
            stats.put("pct", total > 0 ? (present * 100 / total) : 0);
            subjectAttendance.put(entry.getValue(), stats);
        }
        model.addAttribute("subjectAttendance", subjectAttendance);

        // Today's timetable
        java.time.DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        Timetable.DayOfWeek todayEnum = switch (today) {
        case MONDAY -> Timetable.DayOfWeek.MONDAY;
        case TUESDAY -> Timetable.DayOfWeek.TUESDAY;
        case WEDNESDAY -> Timetable.DayOfWeek.WEDNESDAY;
        case THURSDAY -> Timetable.DayOfWeek.THURSDAY;
        case FRIDAY -> Timetable.DayOfWeek.FRIDAY;
        case SATURDAY -> Timetable.DayOfWeek.SATURDAY;
        default -> Timetable.DayOfWeek.MONDAY;
        };
        List<Long> enrolledCourseIds = enrollments.stream().map(e -> e.getCourse().getId())
                .collect(Collectors.toList());
        List<Timetable> todaySchedule = timetableService.getByDay(todayEnum).stream()
                .filter(t -> enrolledCourseIds.contains(t.getCourse().getId())).collect(Collectors.toList());
        model.addAttribute("todaySchedule", todaySchedule);

        // Pending assignments
        List<Assignment> assignments = assignmentService.getByStudentCourses(enrolledCourseIds);
        List<Assignment> pending = assignments.stream().filter(a -> a.getDueDate().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
        model.addAttribute("pendingAssignments", pending);

        // Notifications
        if (user != null) {
            model.addAttribute("notifications", notificationService.getAll(user.getId()));
            model.addAttribute("notificationCount", notificationService.getUnreadCount(user.getId()));
        }

        model.addAttribute("student", student);
        return "student/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        Student student = studentService.getStudentByUsername(authentication.getName());
        model.addAttribute("student", student);
        return "student/profile";
    }

    // ========== TIMETABLE ==========
    @GetMapping("/timetable")
    public String timetable(Authentication authentication, Model model) {
        Student student = studentService.getStudentByUsername(authentication.getName());
        if (student == null)
            return "redirect:/login";

        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student.getStudentId());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());

        Map<Timetable.DayOfWeek, List<Timetable>> allByDay = timetableService.getGroupedByDay();
        // Filter to only enrolled courses
        Map<Timetable.DayOfWeek, List<Timetable>> filtered = new LinkedHashMap<>();
        allByDay.forEach((day, slots) -> {
            List<Timetable> studentSlots = slots.stream().filter(t -> courseIds.contains(t.getCourse().getId()))
                    .collect(Collectors.toList());
            if (!studentSlots.isEmpty())
                filtered.put(day, studentSlots);
        });
        model.addAttribute("timetableByDay", filtered);
        return "student/timetable";
    }

    // ========== ASSIGNMENTS ==========
    @GetMapping("/assignments")
    public String assignments(Authentication authentication, Model model) {
        Student student = studentService.getStudentByUsername(authentication.getName());
        if (student == null)
            return "redirect:/login";

        List<Enrollment> enrollments = enrollmentService.getStudentEnrollments(student.getStudentId());
        List<Long> courseIds = enrollments.stream().map(e -> e.getCourse().getId()).collect(Collectors.toList());
        List<Assignment> assignments = assignmentService.getByStudentCourses(courseIds);

        // Mark which ones are submitted
        Map<Long, Submission> submissionMap = new HashMap<>();
        for (Assignment a : assignments) {
            submissionService.getByAssignmentAndStudent(a.getId(), student.getStudentId())
                    .ifPresent(s -> submissionMap.put(a.getId(), s));
        }
        model.addAttribute("assignments", assignments);
        model.addAttribute("submissionMap", submissionMap);
        return "student/assignments";
    }

    @GetMapping("/assignments/{id}")
    public String viewAssignment(@PathVariable Long id, Authentication authentication, Model model) {
        Student student = studentService.getStudentByUsername(authentication.getName());
        assignmentService.getById(id).ifPresent(a -> {
            model.addAttribute("assignment", a);
            if (student != null) {
                submissionService.getByAssignmentAndStudent(id, student.getStudentId())
                        .ifPresent(s -> model.addAttribute("submission", s));
            }
        });
        return "student/view-assignment";
    }

    @PostMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, @RequestParam MultipartFile file,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentByUsername(authentication.getName());
            Assignment assignment = assignmentService.getById(id).orElse(null);
            if (student == null || assignment == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid data");
                return "redirect:/student/assignments";
            }
            String filePath = fileStorageService.storeFile(file, "submissions");
            Submission s = new Submission();
            s.setAssignment(assignment);
            s.setStudent(student);
            s.setFilePath(filePath);
            s.setStatus(assignment.getDueDate().isBefore(LocalDateTime.now()) ? Submission.SubmissionStatus.LATE
                    : Submission.SubmissionStatus.SUBMITTED);
            submissionService.save(s);
            redirectAttributes.addFlashAttribute("success", "Assignment submitted!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/student/assignments/" + id;
    }
}