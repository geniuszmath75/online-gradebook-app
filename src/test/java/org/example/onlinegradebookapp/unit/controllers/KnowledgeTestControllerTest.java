package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.KnowledgeTestController;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.service.KnowledgeTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class KnowledgeTestControllerTest {
    private KnowledgeTestService testService;
    private KnowledgeTestController testController;

    @BeforeEach
    public void setUp() {
        testService = Mockito.mock(KnowledgeTestService.class);
        testController = new KnowledgeTestController(testService);
    }

    @Test
    void getAllKnowledgeTests_shouldReturnListAndOk() {
        List<KnowledgeTest> tests = Arrays.asList(new KnowledgeTest(), new KnowledgeTest());
        when(testService.findAllKnowledgeTests()).thenReturn(tests);

        ResponseEntity<?> response = testController.getAllKnowledgeTests();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tests, response.getBody());
        verify(testService, times(1)).findAllKnowledgeTests();
    }

    @Test
    void getAllKnowledgeTestById_shouldReturnKnowledgeTestAndOk() {
        KnowledgeTest knowledgeTest = new KnowledgeTest();
        when(testService.findKnowledgeTestById(1L)).thenReturn(knowledgeTest);

        ResponseEntity<?> response = testController.getKnowledgeTestById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(knowledgeTest, response.getBody());
        verify(testService, times(1)).findKnowledgeTestById(1L);
    }

    @Test
    void createKnowledgeTest_shouldCallServiceAndReturnCreated() {
        KnowledgeTestDto dto = new KnowledgeTestDto();
        dto.setName("Test");
        dto.setTestDate(LocalDate.now().plusDays(1));
        dto.setCategory(TestCategory.CLASS_TEST);
        dto.setClassId(2L);
        dto.setSubjectId(3L);

        ResponseEntity<?> response = testController.createKnowledgeTest(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Knowledge test created successfully", response.getBody());
        verify(testService, times(1)).addKnowledgeTest(dto);
    }

    @Test
    void updateKnowledgeTest_shouldUpdateAndReturnOk_whenNoValidationErrors() {
        KnowledgeTestUpdateDto dto = new KnowledgeTestUpdateDto();
        dto.setName("UpdatedTest");
        dto.setTestDate(LocalDate.now().plusDays(2));
        dto.setCategory(TestCategory.CLASSWORK);
        dto.setClassId(2L);
        dto.setSubjectId(3L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = testController.updateKnowledgeTest(1L, dto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Knowledge test updated successfully", response.getBody());
        verify(testService, times(1)).updateKnowledgeTestAttributes(dto, 1L);
    }

    @Test
    void updateKnowledgeTest_shouldThrowBadRequest_whenValidationFails() {
        KnowledgeTestUpdateDto dto = new KnowledgeTestUpdateDto();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                Collections.singletonList(new ObjectError("knowledgeTest", "Validation failed"))
        );

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                testController.updateKnowledgeTest(1L, dto, bindingResult)
        );

        assertEquals("Validation failed", exception.getMessage());
        verify(testService, never()).updateKnowledgeTestAttributes(any(), any());
    }

    @Test
    void deleteKnowledgeTest_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = testController.deleteKnowledgeTest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Knowledge test deleted successfully", response.getBody());
        verify(testService, times(1)).deleteKnowledgeTest(1L);
    }
}
