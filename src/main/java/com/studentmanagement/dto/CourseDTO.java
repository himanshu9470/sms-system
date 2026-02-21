package com.studentmanagement.dto;

public class CourseDTO {
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private Integer credits;
    private Long enrollmentCount;
    
    public CourseDTO() {}
    
    public CourseDTO(Long id, String courseCode, String courseName, String description, Integer credits) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.description = description;
        this.credits = credits;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }
    
    public Long getEnrollmentCount() { return enrollmentCount; }
    public void setEnrollmentCount(Long enrollmentCount) { this.enrollmentCount = enrollmentCount; }
}
