package com.studentmanagement.controller;

import com.studentmanagement.model.*;
import com.studentmanagement.repository.ParentStudentRepository;
import com.studentmanagement.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private TimetableService timetableService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ParentStudentRepository parentStudentRepository;

    // ========== DASHBOARD ==========
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("studentCount", studentService.getStudentCount());
        model.addAttribute("userCount", userService.getAllUsers().size());
        model.addAttribute("courseCount", courseService.getTotalCourseCount());
        List<User> faculty = userService.getAllUsers().stream().filter(u -> u.getRole() == User.Role.FACULTY)
                .collect(Collectors.toList());
        model.addAttribute("facultyCount", faculty.size());
        model.addAttribute("enrollmentCount", enrollmentService.getAllEnrollments().size());
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/dashboard";
    }

    // ========== STUDENT MANAGEMENT ==========
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
    public String addStudent(@ModelAttribute Student student, @RequestParam String username,
            @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            studentService.createStudent(student, username, password);
            redirectAttributes.addFlashAttribute("success", "Student added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add student: " + e.getMessage());
        }
        return "redirect:/admin/students";
    }

    @GetMapping("/students/edit/{id}")
    public String showEditStudentForm(@PathVariable Long id, Model model) {
        studentService.getStudentById(id).ifPresent(student -> model.addAttribute("student", student));
        return "admin/edit-student";
    }

    @PostMapping("/students/edit/{id}")
    public String updateStudent(@PathVariable Long id, @ModelAttribute Student student,
            RedirectAttributes redirectAttributes) {
        studentService.updateStudent(id, student);
        redirectAttributes.addFlashAttribute("success", "Student updated successfully!");
        return "redirect:/admin/students";
    }

    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (studentService.deleteStudent(id)) {
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete student!");
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
    public String addCourse(@ModelAttribute Course course, RedirectAttributes redirectAttributes) {
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
        courseService.getCourseById(id).ifPresent(course -> model.addAttribute("course", course));
        return "admin/edit-course";
    }

    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute Course course,
            RedirectAttributes redirectAttributes) {
        courseService.updateCourse(id, course);
        redirectAttributes.addFlashAttribute("success", "Course updated successfully!");
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/{id}/enrollments")
    public String viewCourseEnrollments(@PathVariable Long id, Model model) {
        courseService.getCourseById(id).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("enrollments", enrollmentService.getCourseEnrollments(id));
        });
        return "admin/course-enrollments";
    }

    // ========== ENROLLMENT MANAGEMENT ==========
    @GetMapping("/enrollments")
    public String listAllEnrollments(Model model) {
        model.addAttribute("enrollments", enrollmentService.getAllEnrollments());
        return "admin/enrollments";
    }

    @GetMapping("/enrollments/add")
    public String showAddEnrollmentForm(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin/add-enrollment";
    }

    @PostMapping("/enrollments/add")
    public String addEnrollment(@RequestParam Long studentId, @RequestParam Long courseId,
            RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));
            Course course = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            enrollmentService.enrollStudent(student, course);
            redirectAttributes.addFlashAttribute("success", "Student successfully enrolled in course!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to enroll student: " + e.getMessage());
        }
        return "redirect:/admin/enrollments";
    }

    @PostMapping("/enrollments/{id}/grade")
    public String updateEnrollmentGrade(@PathVariable Long id, @RequestParam String grade,
            RedirectAttributes redirectAttributes) {
        enrollmentService.updateGrade(id, grade);
        redirectAttributes.addFlashAttribute("success", "Grade updated successfully!");
        return "redirect:/admin/enrollments";
    }

    // ========== REPORTS ==========
    @GetMapping("/reports")
    public String reportsPage() {
        return "admin/reports";
    }

    @GetMapping("/reports/student-report")
    public String studentReport(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/student-report";
    }

    @GetMapping("/reports/course-report")
    public String courseReport(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
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
    public String settingsPage() {
        return "admin/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@RequestParam String semester, @RequestParam String academicYear,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Settings updated successfully!");
        return "redirect:/admin/settings";
    }

    // ========== FACULTY MANAGEMENT ==========
    @GetMapping("/faculty")
    public String listFaculty(Model model) {
        List<User> faculty = userService.getAllUsers().stream().filter(u -> u.getRole() == User.Role.FACULTY)
                .collect(Collectors.toList());
        model.addAttribute("faculty", faculty);
        return "admin/faculty";
    }

    @GetMapping("/faculty/add")
    public String showAddFacultyForm() {
        return "admin/add-faculty";
    }

    @PostMapping("/faculty/add")
    public String addFaculty(@RequestParam String username, @RequestParam String password, @RequestParam String email,
            @RequestParam String fullName, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(username, password, email, fullName, User.Role.FACULTY);
            redirectAttributes.addFlashAttribute("success", "Faculty member added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/faculty";
    }

    @GetMapping("/faculty/delete/{id}")
    public String deleteFaculty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("success", "Faculty member removed!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete faculty member!");
        }
        return "redirect:/admin/faculty";
    }

    // ========== ATTENDANCE ==========
    @GetMapping("/attendance")
    public String viewAttendance(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "admin/attendance";
    }

    @GetMapping("/attendance/{courseId}")
    public String viewCourseAttendance(@PathVariable Long courseId, Model model) {
        courseService.getCourseById(courseId).ifPresent(course -> {
            model.addAttribute("course", course);
            model.addAttribute("attendanceRecords", attendanceService.getAttendanceByCourse(courseId));
        });
        return "admin/course-attendance";
    }

    // ========== TIMETABLE MANAGEMENT ==========
    @GetMapping("/timetable")
    public String timetable(Model model) {
        model.addAttribute("timetableByDay", timetableService.getGroupedByDay());
        model.addAttribute("allEntries", timetableService.getAll());
        return "admin/timetable";
    }

    @GetMapping("/timetable/add")
    public String showAddTimetable(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        List<User> faculty = userService.getAllUsers().stream().filter(u -> u.getRole() == User.Role.FACULTY)
                .collect(Collectors.toList());
        model.addAttribute("faculty", faculty);
        model.addAttribute("days", Timetable.DayOfWeek.values());
        return "admin/add-timetable";
    }

    @PostMapping("/timetable/add")
    public String addTimetable(@RequestParam Long courseId, @RequestParam Long facultyId,
            @RequestParam String dayOfWeek, @RequestParam String startTime, @RequestParam String endTime,
            @RequestParam String room, @RequestParam(required = false) String section,
            RedirectAttributes redirectAttributes) {
        try {
            Timetable t = new Timetable();
            t.setCourse(courseService.getCourseById(courseId).orElseThrow());
            t.setFaculty(userService.getUserById(facultyId).orElseThrow());
            t.setDayOfWeek(Timetable.DayOfWeek.valueOf(dayOfWeek));
            t.setStartTime(LocalTime.parse(startTime));
            t.setEndTime(LocalTime.parse(endTime));
            t.setRoom(room);
            t.setSection(section);
            timetableService.save(t);
            redirectAttributes.addFlashAttribute("success", "Timetable entry added!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/timetable";
    }

    @GetMapping("/timetable/delete/{id}")
    public String deleteTimetable(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        timetableService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Timetable entry deleted!");
        return "redirect:/admin/timetable";
    }

    // ========== PARENT MANAGEMENT ==========
    @GetMapping("/parents")
    public String listParents(Model model) {
        List<User> parents = userService.getAllUsers().stream().filter(u -> u.getRole() == User.Role.PARENT)
                .collect(Collectors.toList());
        model.addAttribute("parents", parents);
        model.addAttribute("students", studentService.getAllStudents());
        return "admin/parents";
    }

    @PostMapping("/parents/add")
    public String addParent(@RequestParam String username, @RequestParam String password, @RequestParam String email,
            @RequestParam String fullName, @RequestParam Long studentId, @RequestParam String relationship,
            RedirectAttributes redirectAttributes) {
        try {
            User parent = userService.createUser(username, password, email, fullName, User.Role.PARENT);
            Student student = studentService.getStudentById(studentId).orElseThrow();
            ParentStudent ps = new ParentStudent();
            ps.setParent(parent);
            ps.setStudent(student);
            ps.setRelationship(relationship);
            parentStudentRepository.save(ps);
            redirectAttributes.addFlashAttribute("success", "Parent account created and linked to student!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed: " + e.getMessage());
        }
        return "redirect:/admin/parents";
    }

    @GetMapping("/parents/delete/{id}")
    public String deleteParent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (userService.deleteUser(id)) {
            redirectAttributes.addFlashAttribute("success", "Parent removed!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed!");
        }
        return "redirect:/admin/parents";
    }
}
