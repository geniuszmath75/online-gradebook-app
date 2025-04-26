package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.StudentRegistrationDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolClassRepository classRepository;

    public StudentService(StudentRepository studentRepository, PasswordEncoder passwordEncoder, SchoolClassRepository classRepository) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
        this.classRepository = classRepository;
    }

    // Registers a new student using data from the registration DTO
    public void register(StudentRegistrationDto dto) {
        // Check if student with given email already exists
        if(studentRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email '"+dto.getEmail()+"' already exists");
        }

        Student student = new Student();
        student.setEmail(dto.getEmail());
        student.setPassword(passwordEncoder.encode(dto.getPassword()));
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());

        // If classId is provided then add it to student entity
        if(dto.getClassId() != null) {
            SchoolClass schoolClass = classRepository.findById(dto.getClassId()).orElseThrow();
            student.setSchoolClass(schoolClass);
        }

        studentRepository.save(student);
    }

    // Finds all students
    public List<Student> findAllStudents() {
        return studentRepository.findAll();
    }

    // Finds a student with given ID
    public Student findStudentById(long id) {
        return studentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id "+id+" not found"));
    }
}
