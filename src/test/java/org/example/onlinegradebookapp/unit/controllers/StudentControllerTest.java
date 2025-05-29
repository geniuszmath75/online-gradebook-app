package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.StudentController;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.StudentUpdateDto;
import org.example.onlinegradebookapp.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StudentControllerTest {
    private StudentService studentService;
    private StudentController studentController;

    @BeforeEach
    public void setUp() {
        studentService = Mockito.mock(StudentService.class);
        studentController = new StudentController(studentService);
    }

    @Test
    void getAllStudents_shouldReturnListAndOk() {
        List<Student> students = Arrays.asList(new Student(), new Student());
        when(studentService.findAllStudents()).thenReturn(students);

        ResponseEntity<?> response = studentController.getAllStudents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(students, response.getBody());
        verify(studentService, times(1)).findAllStudents();
    }

    @Test
    void getStudentById_shouldReturnStudentAndOk() {
        Student student = new Student();
        student.setEmail("student@gmail.com");
        student.setPassword("password");
        student.setFirstName("Student");
        student.setLastName("Test");
        when(studentService.findStudentById(1L)).thenReturn(student);

        ResponseEntity<?> response = studentController.getStudentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(student, response.getBody());
        verify(studentService, times(1)).findStudentById(1L);
    }

    @Test
    void updateStudent_shouldUpdateAndReturnOk_whenNoValidationErrors() {
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setEmail("updated@gmail.com");
        dto.setPassword("changedPassword");
        dto.setFirstName("UpdatedStudent");
        dto.setLastName("UpdatedName");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = studentController.updateStudent(1L, dto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Student updated successfully", response.getBody());
        verify(studentService, times(1)).updateStudentAttributes(dto, 1L);
    }

    @Test
    void updateStudent_shouldThrowBadRequest_whenValidationFails() {
        StudentUpdateDto dto = new StudentUpdateDto();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                Collections.singletonList(new ObjectError("student", "Validation failed"))
        );

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                studentController.updateStudent(1L, dto, bindingResult)
        );

        assertEquals("Validation failed", exception.getMessage());
        verify(studentService, never()).updateStudentAttributes(any(), any());
    }

    @Test
    void deleteStudent_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = studentController.deleteStudent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Student deleted successfully", response.getBody());
        verify(studentService, times(1)).deleteStudent(1L);
    }
}
