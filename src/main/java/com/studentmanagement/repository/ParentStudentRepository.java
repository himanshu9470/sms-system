package com.studentmanagement.repository;

import com.studentmanagement.model.ParentStudent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParentStudentRepository extends CrudRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentId(Long parentId);

    List<ParentStudent> findByStudentStudentId(Long studentId);
}
