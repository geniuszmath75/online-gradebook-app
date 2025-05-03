package org.example.onlinegradebookapp.controller;

import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.payload.KnowledgeTestDto;
import org.example.onlinegradebookapp.service.KnowledgeTestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge_tests")
public class KnowledgeTestController {
    private final KnowledgeTestService knowledgeTestService;

    public KnowledgeTestController(KnowledgeTestService knowledgeTestService) {
        this.knowledgeTestService = knowledgeTestService;
    }

    @GetMapping
    public ResponseEntity<?> getAllKnowledgeTests() {
        List<KnowledgeTest> knowledgeTests = knowledgeTestService.findAllKnowledgeTests();
        return new ResponseEntity<>(knowledgeTests, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgeTestById(@PathVariable Long id) {
        KnowledgeTest knowledgeTest = knowledgeTestService.findKnowledgeTestById(id);
        return new ResponseEntity<>(knowledgeTest, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createKnowledgeTest(@Valid @RequestBody KnowledgeTestDto dto) {
        knowledgeTestService.addKnowledgeTest(dto);
        return new ResponseEntity<>("Knowledge test created successfully", HttpStatus.CREATED);
    }
}
