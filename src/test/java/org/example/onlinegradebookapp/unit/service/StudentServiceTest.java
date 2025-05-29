package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.StudentRegistrationDto;
import org.example.onlinegradebookapp.payload.request.StudentUpdateDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StudentServiceTest {
    private StudentRepository studentRepository;
    private PasswordEncoder passwordEncoder;
    private SchoolClassRepository classRepository;
    private StudentService studentService;

    @BeforeEach
    public void setUp() {
        studentRepository = Mockito.mock(StudentRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        classRepository = Mockito.mock(SchoolClassRepository.class);
        studentService = new StudentService(studentRepository, passwordEncoder, classRepository);
    }

    @Test
    void register_shouldSaveNewStudent() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setEmail("student@gmail.com");
        dto.setPassword("secret123");
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");

        when(studentRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        studentService.register(dto);

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());

        Student saved = studentCaptor.getValue();
        assertEquals("student@gmail.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals("Jan", saved.getFirstName());
        assertEquals("Kowalski", saved.getLastName());
    }

    @Test
    void register_shouldThrowException_whenEmailExists() {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setEmail("student@gmail.com");

        when(studentRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> studentService.register(dto));
    }

    @Test
    void findAllStudents_shouldReturnList() {
        List<Student> list = List.of(new Student(), new Student());
        when(studentRepository.findAll()).thenReturn(list);

        List<Student> result = studentService.findAllStudents();
        assertEquals(2, result.size());
    }

    @Test
    void findStudentById_shouldReturnStudent_whenFound() {
        Student student = new Student();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        Student result = studentService.findStudentById(1L);
        assertEquals(student, result);
    }

    @Test
    void findStudentById_shouldThrowException_whenNotFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.findStudentById(1L));
    }

    @Test
    void updateStudentAttributes_shouldUpdateStudentFields() {
        Student existing = new Student();
        existing.setEmail("old@gmail.com");
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setEmail("new@gmail.com");
        dto.setPassword("newpassword");
        dto.setFirstName("Adam");
        dto.setLastName("Nowak");

        when(studentRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded");

        studentService.updateStudentAttributes(dto, 1L);

        assertEquals("new@gmail.com", existing.getEmail());
        assertEquals("encoded", existing.getPassword());
        assertEquals("Adam", existing.getFirstName());
        assertEquals("Nowak", existing.getLastName());
        verify(studentRepository).save(existing);
    }

    @Test
    void updateStudentAttributes_shouldUpdateClass_whenClassIdProvided() {
        Student existing = new Student();
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setClassId(99L);

        SchoolClass newClass = new SchoolClass();
        newClass.setId(99L);

        when(studentRepository.existsByEmail(any())).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(classRepository.findById(99L)).thenReturn(Optional.of(newClass));

        studentService.updateStudentAttributes(dto, 1L);

        assertEquals(newClass, existing.getSchoolClass());
        verify(studentRepository).save(existing);
    }

    @Test
    void updateStudentAttributes_shouldThrow_whenStudentNotFound() {
        StudentUpdateDto dto = new StudentUpdateDto();
        when(studentRepository.existsByEmail(any())).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.updateStudentAttributes(dto, 1L));
    }

    @Test
    void updateStudentAttributes_shouldThrow_whenClassIdInvalid() {
        Student existing = new Student();
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setClassId(123L);

        when(studentRepository.existsByEmail(any())).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(classRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> studentService.updateStudentAttributes(dto, 1L));
    }
}
