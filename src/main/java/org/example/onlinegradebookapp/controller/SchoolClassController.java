package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.payload.response.MessageResponse;
import org.example.onlinegradebookapp.payload.response.ResponseMapper;
import org.example.onlinegradebookapp.payload.response.SchoolClassResponse;
import org.example.onlinegradebookapp.service.SchoolClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@Tag(name = "School classes", description = "Operations for school classes")
public class SchoolClassController {
    private final SchoolClassService schoolClassService;

    public SchoolClassController(SchoolClassService schoolClassService) {
        this.schoolClassService = schoolClassService;
    }

    @GetMapping
    @Operation(summary = "Get all school classes",
            description = "Get a list of all school classes from the database")
    public ResponseEntity<List<SchoolClassResponse>> getAllSchoolClasses() {
        List<SchoolClassResponse> schoolClasses = schoolClassService.findAllSchoolClassResponses();
        return new ResponseEntity<>(schoolClasses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single school class",
            description = "Get a single school class with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SchoolClassResponse.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "School class ID")
    public ResponseEntity<SchoolClassResponse> getSchoolClassById(@PathVariable Long id) {
        SchoolClassResponse schoolClass = schoolClassService.findSchoolClassResponseById(id);
        return new ResponseEntity<>(schoolClass, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Create a new school class",
            description = "Create a new school class in database")
    public ResponseEntity<SchoolClassResponse> createSchoolClass(@Valid @RequestBody SchoolClassDto dto) {
        SchoolClassResponse created = ResponseMapper.toSchoolClass(schoolClassService.addSchoolClass(dto));
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Update a single school class",
            description = "Update a single school class with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "School class ID")
    public ResponseEntity<SchoolClassResponse> updateSchoolClass(@PathVariable Long id, @Valid @RequestBody SchoolClassDto dto) {
        SchoolClassResponse updated = ResponseMapper.toSchoolClass(schoolClassService.updateSchoolClass(dto, id));
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Delete a school class",
            description = "Delete a school class by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "School class ID")
    public ResponseEntity<MessageResponse> deleteSchoolClass(@PathVariable Long id) {
        schoolClassService.deleteSchoolClass(id);
        return new ResponseEntity<>(new MessageResponse("School class deleted successfully"), HttpStatus.OK);
    }
}
