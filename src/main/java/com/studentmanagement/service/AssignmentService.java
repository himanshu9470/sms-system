package com.studentmanagement.service;

import com.studentmanagement.model.Assignment;
import com.studentmanagement.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    public List<Assignment> getAll() {
        return (List<Assignment>) assignmentRepository.findAllByOrderByDueDateDesc();
    }

    public Optional<Assignment> getById(Long id) {
        return assignmentRepository.findById(id);
    }

    public List<Assignment> getByCourse(Long courseId) {
        return assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);
    }

    public List<Assignment> getByFaculty(Long userId) {
        return assignmentRepository.findByCreatedByIdOrderByCreatedAtDesc(userId);
    }

    public List<Assignment> getByStudentCourses(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty())
            return List.of();
        return assignmentRepository.findByCourseIds(courseIds);
    }

    public Assignment save(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public void delete(Long id) {
        assignmentRepository.deleteById(id);
    }
}
