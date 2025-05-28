package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@Tag(name = "Grades", description = "Operations for students grades")
public class GradeController {
    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    @Operation(summary = "Get all student grades",
            description = "Get a list of all student grades from the database")
    public ResponseEntity<?> getAllGrades() {
        List<Grade> grades = gradeService.findAllGrades();
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single student grade",
            description = "Get a single student grade with given ID",
    responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Grade.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Grade ID")
    public ResponseEntity<?> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.findGradeById(id);
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new grade for a knowledge test",
    description = "Create a new grade in database")
    public ResponseEntity<?> createGrade(@Valid @RequestBody GradeDto dto) {
        gradeService.addGrade(dto);
        return new ResponseEntity<>("Grade created successfully" ,HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update attributes of the single grade",
    description = "Update attributes of the single grade with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Grade ID")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, @Valid @RequestBody GradeUpdateDto dto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        gradeService.updateGradeAttributes(dto, id);
        return new ResponseEntity<>("Grade updated successfully" ,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Delete a grade",
    description = "Delete a grade by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Grade ID")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return new ResponseEntity<>("Grade deleted successfully" ,HttpStatus.OK);
    }
}
