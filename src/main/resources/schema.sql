-- schema.sql
-- Tables for Student Management System

-- Users table for authentication
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'STUDENT', 'FACULTY') DEFAULT 'STUDENT',
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert admin user

-- Insert sample student user


-- Students table
CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    date_of_birth DATE,
    address TEXT,
    enrollment_date DATE DEFAULT (CURRENT_DATE),
    department VARCHAR(50),
    semester INT,
    cgpa DECIMAL(3,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Courses table
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    department VARCHAR(50),
    semester INT,
    faculty_id INT,
    max_students INT DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES users(id)
);

-- Student-Course enrollment table
CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    enrollment_date DATE DEFAULT CURRENT_DATE,
    grade CHAR(2),
    status VARCHAR(20) DEFAULT 'ENROLLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE (student_id, course_id)
);


INSERT INTO users (username, password, email, full_name, role) VALUES 
('admin', '$2a$10$e2v1zBGwX0WzL1hxcIHDJO0awrOdLpB61wxBQkK.IkDqugyKDu.Ee', 'admin@example.com', 'Administrator', 'ADMIN');

-- Insert sample students
INSERT INTO users (username, password, email, full_name, role) VALUES
('john.doe', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'john.doe@student.edu', 'John Doe', 'STUDENT'),
('jane.smith', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'jane.smith@student.edu', 'Jane Smith', 'STUDENT');

-- Insert corresponding student records
INSERT INTO students (user_id, first_name, last_name, email, phone, department, semester) VALUES
(2, 'John', 'Doe', 'john.doe@student.edu', '1234567890', 'Computer Science', 3),
(3, 'Jane', 'Smith', 'jane.smith@student.edu', '0987654321', 'Electrical Engineering', 4);

-- Insert sample courses
INSERT INTO courses (course_code, course_name, credits, department, semester) VALUES
('CS101', 'Introduction to Programming', 4, 'Computer Science', 1),
('CS201', 'Data Structures', 4, 'Computer Science', 2),
('EE101', 'Circuit Analysis', 3, 'Electrical Engineering', 1),
('MATH101', 'Calculus I', 3, 'Mathematics', 1);