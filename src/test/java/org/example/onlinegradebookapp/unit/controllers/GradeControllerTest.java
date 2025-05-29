package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.GradeController;
import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class GradeControllerTest {
    private GradeService gradeService;
    private GradeController gradeController;
    
    @BeforeEach
    public void setUp() {
        gradeService = Mockito.mock(GradeService.class);
        gradeController = new GradeController(gradeService);
    }

    @Test
    void getAllGrades_shouldReturnListAndOk() {
        List<Grade> grades = Arrays.asList(new Grade(), new Grade());
        when(gradeService.findAllGrades()).thenReturn(grades);

        ResponseEntity<?> response = gradeController.getAllGrades();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(grades, response.getBody());
        verify(gradeService, times(1)).findAllGrades();
    }

    @Test
    void getAllGradeById_shouldReturnGradeAndOk() {
        Grade knowledgeTest = new Grade();
        when(gradeService.findGradeById(1L)).thenReturn(knowledgeTest);

        ResponseEntity<?> response = gradeController.getGradeById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(knowledgeTest, response.getBody());
        verify(gradeService, times(1)).findGradeById(1L);
    }

    @Test
    void createGrade_shouldCallServiceAndReturnCreated() {
        GradeDto dto = new GradeDto();
        dto.setGrade(BigDecimal.valueOf(1.0));
        dto.setDescription("description");
        dto.setTestId(2L);
        dto.setStudentId(3L);

        ResponseEntity<?> response = gradeController.createGrade(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Grade created successfully", response.getBody());
        verify(gradeService, times(1)).addGrade(dto);
    }

    @Test
    void updateGrade_shouldUpdateAndReturnOk_whenNoValidationErrors() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setGrade(BigDecimal.valueOf(2.0));
        dto.setTestId(2L);
        dto.setStudentId(3L);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = gradeController.updateGrade(1L, dto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grade updated successfully", response.getBody());
        verify(gradeService, times(1)).updateGradeAttributes(dto, 1L);
    }

    @Test
    void updateGrade_shouldThrowBadRequest_whenValidationFails() {
        GradeUpdateDto dto = new GradeUpdateDto();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                Collections.singletonList(new ObjectError("grade", "Validation failed"))
        );

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                gradeController.updateGrade(1L, dto, bindingResult)
        );

        assertEquals("Validation failed", exception.getMessage());
        verify(gradeService, never()).updateGradeAttributes(any(), any());
    }

    @Test
    void deleteGrade_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = gradeController.deleteGrade(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Grade deleted successfully", response.getBody());
        verify(gradeService, times(1)).deleteGrade(1L);
    }
}
