package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.response.MessageResponse;
import org.example.onlinegradebookapp.service.SubjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Operations for subjects")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    @Operation(summary = "Get all subjects",
            description = "Get a list of all subjects from the database")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectService.findAllSubjects();
        return new ResponseEntity<>(subjects, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single subject",
            description = "Get a single subject with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Subject.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Subject ID")
    public ResponseEntity<Subject> getSubjectById(@PathVariable Long id) {
        Subject subject = subjectService.findSubjectById(id);
        return new ResponseEntity<>(subject, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Create a new subject",
            description = "Create a new subject in database")
    public ResponseEntity<Subject> createSubject(@Valid @RequestBody SubjectDto dto) {
        Subject created = subjectService.addSubject(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Update a single subject",
            description = "Update a single subject with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Subject ID")
    public ResponseEntity<Subject> updateSubject(@PathVariable Long id, @Valid @RequestBody SubjectDto dto) {
        Subject updated = subjectService.updateSubject(dto, id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Delete a subject",
            description = "Delete a subject by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Subject ID")
    public ResponseEntity<MessageResponse> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return new ResponseEntity<>(new MessageResponse("Subject deleted successfully"), HttpStatus.OK);
    }
}
