package org.example.onlinegradebookapp.controller;

import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.payload.SubjectDto;
import org.example.onlinegradebookapp.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<?> getAllSubjects() {
        List<Subject> subjects = subjectService.findAllSubjects();
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long id) {
        Subject subject = subjectService.findSubjectById(id);
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDto dto) {
        subjectService.addSubject(dto);
        return new ResponseEntity<>("Subject created successfully", HttpStatus.CREATED);
    }
}
