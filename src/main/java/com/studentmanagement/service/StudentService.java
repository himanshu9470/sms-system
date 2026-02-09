// StudentService.java
package com.studentmanagement.service;

import com.studentmanagement.model.Student;
import com.studentmanagement.model.User;
import com.studentmanagement.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private UserService userService;
    
    public Student createStudent(Student student, String username, String password) {
        // Create user account first
        User user = userService.createUser(
            username, 
            password, 
            student.getEmail(),
            student.getFirstName() + " " + student.getLastName(),
            User.Role.STUDENT
        );
        
        // Link user to student
        student.setUser(user);
        
        return studentRepository.save(student);
    }
    
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }
    
    public List<Student> getAllStudents() {
        return (List<Student>) studentRepository.findAll();
    }
    
    public List<Student> searchStudents(String keyword) {
        return studentRepository.searchByName(keyword);
    }
    
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartment(department);
    }
    
    public Student updateStudent(Long id, Student studentDetails) {
        return studentRepository.findById(id).map(student -> {
            if (studentDetails.getFirstName() != null) 
                student.setFirstName(studentDetails.getFirstName());
            if (studentDetails.getLastName() != null) 
                student.setLastName(studentDetails.getLastName());
            if (studentDetails.getEmail() != null) 
                student.setEmail(studentDetails.getEmail());
            if (studentDetails.getPhone() != null) 
                student.setPhone(studentDetails.getPhone());
            if (studentDetails.getDateOfBirth() != null) 
                student.setDateOfBirth(studentDetails.getDateOfBirth());
            if (studentDetails.getAddress() != null) 
                student.setAddress(studentDetails.getAddress());
            if (studentDetails.getDepartment() != null) 
                student.setDepartment(studentDetails.getDepartment());
            if (studentDetails.getSemester() != null) 
                student.setSemester(studentDetails.getSemester());
            if (studentDetails.getCgpa() != null) 
                student.setCgpa(studentDetails.getCgpa());
            
            return studentRepository.save(student);
        }).orElse(null);
    }
    
    public boolean deleteStudent(Long id) {
        if (studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public Long getStudentCount() {
        return studentRepository.count();
    }
    
    public Long getStudentCountByDepartment(String department) {
        return studentRepository.countByDepartment(department);
    }
    
    public Student getStudentByUsername(String username) {
        return studentRepository.findByUser_Username(username).orElse(null);
    }
}