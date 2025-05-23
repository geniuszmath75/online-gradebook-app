package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.StudentUpdateDto;
import org.example.onlinegradebookapp.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Operations for students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Get all students",
            description = "Get a list of all students from the database")
    public ResponseEntity<?> getAllStudents() {
        List<Student> students = studentService.findAllStudents();
        return new ResponseEntity<>(students, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single student",
            description = "Get a single student with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Student.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Student ID")
    public ResponseEntity<?> getStudentById(@PathVariable long id) {
        Student student = studentService.findStudentById(id);
        return new ResponseEntity<>(student, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update attributes of the single student",
            description = "Update attributes of the single student with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Student ID")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto dto,
                                                 BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        studentService.updateStudentAttributes(dto, id);
        return new ResponseEntity<>("Student updated successfully" ,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a student",
            description = "Delete a student by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Student ID")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return new ResponseEntity<>("Student deleted successfully" ,HttpStatus.OK);
    }
}
