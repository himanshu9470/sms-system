package com.studentmanagement.service;

import com.studentmanagement.model.Course;
import com.studentmanagement.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository courseRepository;
    
    public Course createCourse(Course course) {
        return courseRepository.save(course);
    }
    
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Optional<Course> getCourseByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }
    
    public List<Course> getAllCourses() {
        return (List<Course>) courseRepository.findAll();
    }
    
    public List<Course> searchCourses(String keyword) {
        return courseRepository.searchByCourseNameOrCode(keyword);
    }
    
    public Course updateCourse(Long id, Course courseDetails) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            Course existingCourse = course.get();
            if (courseDetails.getCourseCode() != null) {
                existingCourse.setCourseCode(courseDetails.getCourseCode());
            }
            if (courseDetails.getCourseName() != null) {
                existingCourse.setCourseName(courseDetails.getCourseName());
            }
            if (courseDetails.getDescription() != null) {
                existingCourse.setDescription(courseDetails.getDescription());
            }
            if (courseDetails.getCredits() != null) {
                existingCourse.setCredits(courseDetails.getCredits());
            }
            return courseRepository.save(existingCourse);
        }
        return null;
    }
    
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    public long getTotalCourseCount() {
        return courseRepository.getTotalCourseCount();
    }
}
