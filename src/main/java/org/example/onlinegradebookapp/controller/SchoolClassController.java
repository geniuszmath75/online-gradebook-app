package org.example.onlinegradebookapp.controller;

import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.payload.SchoolClassDto;
import org.example.onlinegradebookapp.service.SchoolClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
public class SchoolClassController {
    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    public ResponseEntity<?> getAllSchoolClasses() {
        List<SchoolClass> schoolClasses = schoolClassService.findAllSchoolClasses();
        return new ResponseEntity<>(schoolClasses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSchoolClassById(@PathVariable Long id) {
        SchoolClass schoolClass = schoolClassService.findSchoolClassById(id);
        return new ResponseEntity<>(schoolClass, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createSchoolClass(@Valid @RequestBody SchoolClassDto dto) {
        schoolClassService.addSchoolClass(dto);
        return new ResponseEntity<>("School class created successfully", HttpStatus.CREATED);
    }
}
