package com.studentmanagement.repository;

import com.studentmanagement.model.Submission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends CrudRepository<Submission, Long> {
    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Long assignmentId);

    List<Submission> findByStudentStudentIdOrderBySubmittedAtDesc(Long studentId);

    Optional<Submission> findByAssignmentIdAndStudentStudentId(Long assignmentId, Long studentId);

    long countByAssignmentIdAndStatusNot(Long assignmentId, Submission.SubmissionStatus status);
}
