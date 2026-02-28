package com.studentmanagement.repository;

import com.studentmanagement.model.Assignment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssignmentRepository extends CrudRepository<Assignment, Long> {
    List<Assignment> findByCourseIdOrderByDueDateDesc(Long courseId);

    List<Assignment> findByCreatedByIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Assignment a WHERE a.course.id IN :courseIds ORDER BY a.dueDate ASC")
    List<Assignment> findByCourseIds(@Param("courseIds") List<Long> courseIds);

    List<Assignment> findAllByOrderByDueDateDesc();
}
