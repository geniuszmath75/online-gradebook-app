package org.example.onlinegradebookapp.controller;

import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.payload.GradeDto;
import org.example.onlinegradebookapp.service.GradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {
    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public ResponseEntity<?> getAllGrades() {
        List<Grade> grades = gradeService.findAllGrades();
        return new ResponseEntity<>(grades, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGradeById(@PathVariable Long id) {
        Grade grade = gradeService.findGradeById(id);
        return new ResponseEntity<>(grade, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createGrade(@Valid @RequestBody GradeDto dto) {
        gradeService.addGrade(dto);
        return new ResponseEntity<>("Grade created successfully" ,HttpStatus.CREATED);
    }
}
