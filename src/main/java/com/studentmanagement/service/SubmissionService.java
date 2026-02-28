package com.studentmanagement.service;

import com.studentmanagement.model.Submission;
import com.studentmanagement.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    public List<Submission> getByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
    }

    public List<Submission> getByStudent(Long studentId) {
        return submissionRepository.findByStudentStudentIdOrderBySubmittedAtDesc(studentId);
    }

    public Optional<Submission> getByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentStudentId(assignmentId, studentId);
    }

    public Optional<Submission> getById(Long id) {
        return submissionRepository.findById(id);
    }

    public Submission save(Submission submission) {
        return submissionRepository.save(submission);
    }
}
