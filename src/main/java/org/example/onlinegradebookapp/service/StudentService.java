package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.StudentRegistrationDto;
import org.example.onlinegradebookapp.payload.request.StudentUpdateDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    // Update attributes of student with given ID
    public void updateStudentAttributes(StudentUpdateDto dto, Long id) throws ResourceNotFoundException {
        // Check if student with given email already exists
        if(studentRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email '"+dto.getEmail()+"' already exists");
        }

        // Find a student if exists in database
        Optional<Student> optionalStudent = studentRepository.findById(id);

        // If the student exists, update it
        if(optionalStudent.isPresent()) {
            Student student = optionalStudent.get();

            // Check if 'email' is given
            if(dto.getEmail() != null) {
                student.setEmail(dto.getEmail());
            }
            // Check if 'password' is given
            if(dto.getPassword() != null) {
                student.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            // Check if 'firstName' is given
            if(dto.getFirstName() != null) {
                student.setFirstName(dto.getFirstName());
            }
            // Check if 'lastName' is given
            if(dto.getLastName() != null) {
                student.setLastName(dto.getLastName());
            }
            // Check if 'classId' is given
            if(dto.getClassId() != null) {
                SchoolClass updatedClass = classRepository
                        .findById(dto.getClassId())
                        .orElseThrow(() -> new BadRequestException("School class with id=" + dto.getClassId() + " not found"));
                student.setSchoolClass(updatedClass);
            }
            studentRepository.save(student);
        } else {
            throw new ResourceNotFoundException("Student with id=" + id + " not found");
        }
    }

    // Delete a student with given ID
    public void deleteStudent(Long id) {
        if(studentRepository.existsById(id)) {
            studentRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Student with id=" + id + " not found");
        }
    }
}
