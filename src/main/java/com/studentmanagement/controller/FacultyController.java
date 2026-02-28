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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/faculty")
@PreAuthorize("hasAnyRole('FACULTY', 'ADMIN')")
public class FacultyController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private UserService userService;
    @Autowired
    private StudentService studentService;
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

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName()).orElse(null);
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("totalCourses", courseService.getTotalCourseCount());
        if (user != null) {
            model.addAttribute("todaySchedule", timetableService.getByDay(getTodayDayOfWeek()));
            model.addAttribute("assignments", assignmentService.getByFaculty(user.getId()));
            model.addAttribute("notificationCount", notificationService.getUnreadCount(user.getId()));
        }
        return "faculty/dashboard";
    }

    @GetMapping("/courses")
    public String courses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "faculty/courses";
    }

    // ========== ATTENDANCE ==========
    @GetMapping("/attendance/mark/{courseId}")
    public String showMarkAttendanceForm(@PathVariable Long courseId, Model model) {
        courseService.getCourseById(courseId).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("enrollments", enrollmentService.getCourseEnrollments(courseId));
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("alreadyMarked", attendanceService.isAttendanceMarked(courseId, LocalDate.now()));
        });
        return "faculty/mark-attendance";
    }

    @PostMapping("/attendance/mark/{courseId}")
    public String markAttendance(@PathVariable Long courseId, @RequestParam("date") String dateStr,
            @RequestParam("studentIds") List<Long> studentIds, @RequestParam("statuses") List<String> statuses,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            User currentUser = userService.getUserByUsername(authentication.getName()).orElse(null);
            Course course = courseService.getCourseById(courseId).orElse(null);
            if (course == null || currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid course or user");
                return "redirect:/faculty/courses";
            }
            for (int i = 0; i < studentIds.size(); i++) {
                Student student = studentService.getStudentById(studentIds.get(i)).orElse(null);
                if (student != null) {
                    Attendance.AttendanceStatus status = Attendance.AttendanceStatus.valueOf(statuses.get(i));
                    attendanceService.markAttendance(student, course, date, status, currentUser);
                }
            }
            redirectAttributes.addFlashAttribute("success", "Attendance marked for " + studentIds.size() + " students");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/faculty/attendance/history/" + courseId;
    }

    @GetMapping("/attendance/history/{courseId}")
    public String attendanceHistory(@PathVariable Long courseId, Model model) {
        courseService.getCourseById(courseId).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("attendanceRecords", attendanceService.getAttendanceByCourse(courseId));
        });
        return "faculty/attendance-history";
    }

    // ========== TIMETABLE ==========
    @GetMapping("/timetable")
    public String timetable(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("timetableByDay", timetableService.getGroupedByDayForFaculty(user.getId()));
        }
        return "faculty/timetable";
    }

    // ========== ASSIGNMENTS ==========
    @GetMapping("/assignments")
    public String assignments(Authentication authentication, Model model) {
        User user = userService.getUserByUsername(authentication.getName()).orElse(null);
        if (user != null) {
            model.addAttribute("assignments", assignmentService.getByFaculty(user.getId()));
        }
        return "faculty/assignments";
    }

    @GetMapping("/assignments/create")
    public String showCreateAssignment(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "faculty/create-assignment";
    }

    @PostMapping("/assignments/create")
    public String createAssignment(@RequestParam String title, @RequestParam String description,
            @RequestParam Long courseId, @RequestParam String dueDate,
            @RequestParam(defaultValue = "100") Integer maxMarks, @RequestParam(required = false) MultipartFile file,
            Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(authentication.getName()).orElse(null);
            Course course = courseService.getCourseById(courseId).orElse(null);
            if (user == null || course == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid data");
                return "redirect:/faculty/assignments";
            }
            Assignment a = new Assignment();
            a.setTitle(title);
            a.setDescription(description);
            a.setCourse(course);
            a.setCreatedBy(user);
            a.setDueDate(LocalDateTime.parse(dueDate + "T23:59:00"));
            a.setMaxMarks(maxMarks);
            if (file != null && !file.isEmpty()) {
                a.setFilePath(fileStorageService.storeFile(file, "assignments"));
            }
            assignmentService.save(a);
            redirectAttributes.addFlashAttribute("success", "Assignment created!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/faculty/assignments";
    }

    @GetMapping("/assignments/{id}/submissions")
    public String viewSubmissions(@PathVariable Long id, Model model) {
        assignmentService.getById(id).ifPresent(a -> {
            model.addAttribute("assignment", a);
            model.addAttribute("submissions", submissionService.getByAssignment(id));
        });
        return "faculty/view-submissions";
    }

    @PostMapping("/assignments/{assignmentId}/grade/{submissionId}")
    public String gradeSubmission(@PathVariable Long assignmentId, @PathVariable Long submissionId,
            @RequestParam Double grade, @RequestParam(required = false) String feedback,
            RedirectAttributes redirectAttributes) {
        submissionService.getById(submissionId).ifPresent(s -> {
            s.setGrade(grade);
            s.setFeedback(feedback);
            s.setStatus(Submission.SubmissionStatus.GRADED);
            submissionService.save(s);
            // Notify student
            if (s.getStudent() != null && s.getStudent().getUser() != null) {
                notificationService.create(s.getStudent().getUser(), "Assignment Graded",
                        "Your submission for '" + s.getAssignment().getTitle() + "' has been graded. Score: " + grade,
                        Notification.NotificationType.SUCCESS);
            }
        });
        redirectAttributes.addFlashAttribute("success", "Submission graded!");
        return "redirect:/faculty/assignments/" + assignmentId + "/submissions";
    }

    private Timetable.DayOfWeek getTodayDayOfWeek() {
        java.time.DayOfWeek d = java.time.LocalDate.now().getDayOfWeek();
        return switch (d) {
        case MONDAY -> Timetable.DayOfWeek.MONDAY;
        case TUESDAY -> Timetable.DayOfWeek.TUESDAY;
        case WEDNESDAY -> Timetable.DayOfWeek.WEDNESDAY;
        case THURSDAY -> Timetable.DayOfWeek.THURSDAY;
        case FRIDAY -> Timetable.DayOfWeek.FRIDAY;
        case SATURDAY -> Timetable.DayOfWeek.SATURDAY;
        default -> Timetable.DayOfWeek.MONDAY;
        };
    }
}
