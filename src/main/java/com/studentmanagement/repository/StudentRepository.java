// StudentRepository.java
package com.studentmanagement.repository;

import com.studentmanagement.model.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends CrudRepository<Student, Long> {
    
    Optional<Student> findByEmail(String email);
    
    List<Student> findByDepartment(String department);
    
    List<Student> findBySemester(Integer semester);
    
    Optional<Student> findByUser_Username(String username);
    
    @Query("SELECT s FROM Student s WHERE LOWER(s.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Student> searchByName(@Param("name") String name);
    
    @Query("SELECT s FROM Student s ORDER BY s.cgpa DESC")
    List<Student> findAllOrderByCgpaDesc();
    
    @Query("SELECT COUNT(s) FROM Student s WHERE s.department = :department")
    Long countByDepartment(@Param("department") String department);
}