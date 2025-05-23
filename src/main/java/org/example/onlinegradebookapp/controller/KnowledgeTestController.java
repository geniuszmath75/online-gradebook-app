package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.service.KnowledgeTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge_tests")
@Tag(name = "Knowledge tests", description = "Operations for knowledge tests writing by students")
public class KnowledgeTestController {
    private final KnowledgeTestService knowledgeTestService;

    public KnowledgeTestController(KnowledgeTestService knowledgeTestService) {
        this.knowledgeTestService = knowledgeTestService;
    }

    @GetMapping
    @Operation(summary = "Get all knowledge tests for all subjects",
            description = "Get a list of all knowledge tests from the database")
    public ResponseEntity<?> getAllKnowledgeTests() {
        List<KnowledgeTest> knowledgeTests = knowledgeTestService.findAllKnowledgeTests();
        return new ResponseEntity<>(knowledgeTests, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single knowledge test",
            description = "Get a single knowledge test with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = KnowledgeTest.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Knowledge test ID")
    public ResponseEntity<?> getKnowledgeTestById(@PathVariable Long id) {
        KnowledgeTest knowledgeTest = knowledgeTestService.findKnowledgeTestById(id);
        return new ResponseEntity<>(knowledgeTest, HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a new knowledge test",
            description = "Create a new knowledge test in database")
    public ResponseEntity<?> createKnowledgeTest(@Valid @RequestBody KnowledgeTestDto dto) {
        knowledgeTestService.addKnowledgeTest(dto);
        return new ResponseEntity<>("Knowledge test created successfully", HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update attributes of the single knowledge test",
            description = "Update attributes of the single knowledge test with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Knowledge test ID")
    public ResponseEntity<?> updateKnowledgeTest(@PathVariable Long id, @Valid @RequestBody KnowledgeTestUpdateDto dto,
                                                 BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        knowledgeTestService.updateKnowledgeTestAttributes(dto, id);
        return new ResponseEntity<>("Knowledge test updated successfully" ,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a knowledge test",
            description = "Delete a knowledge test by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "Knowledge test ID")
    public ResponseEntity<?> deleteKnowledgeTest(@PathVariable Long id) {
        knowledgeTestService.deleteKnowledgeTest(id);
        return new ResponseEntity<>("Knowledge test deleted successfully" ,HttpStatus.OK);
    }
}
