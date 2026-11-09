package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.GradeController;
import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.payload.response.ResponseMapper;
import org.example.onlinegradebookapp.payload.response.MessageResponse;
import org.example.onlinegradebookapp.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void getAllGrades_shouldReturnPageAndOk() {
        List<Grade> grades = Arrays.asList(new Grade(), new Grade());
        Pageable pageable = PageRequest.of(0, 20);
        Page<Grade> page = new PageImpl<>(grades, pageable, grades.size());
        when(gradeService.findAllGrades(1L, 2L, null, null, pageable)).thenReturn(page);

        ResponseEntity<?> response = gradeController.getAllGrades(1L, 2L, null, null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.map(ResponseMapper::toGrade), response.getBody());
        verify(gradeService, times(1)).findAllGrades(1L, 2L, null, null, pageable);
    }

    @Test
    void getAllGradeById_shouldReturnGradeAndOk() {
        Grade knowledgeTest = new Grade();
        when(gradeService.findGradeById(1L)).thenReturn(knowledgeTest);

        ResponseEntity<?> response = gradeController.getGradeById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ResponseMapper.toGrade(knowledgeTest), response.getBody());
        verify(gradeService, times(1)).findGradeById(1L);
    }

    @Test
    void createGrade_shouldCallServiceAndReturnCreated() {
        GradeDto dto = new GradeDto();
        dto.setGrade(BigDecimal.valueOf(1.0));
        dto.setDescription("description");
        dto.setTestId(2L);
        dto.setStudentId(3L);
        Grade created = new Grade();
        created.setId(10L);
        when(gradeService.addGrade(dto)).thenReturn(created);

        ResponseEntity<?> response = gradeController.createGrade(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ResponseMapper.toGrade(created), response.getBody());
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
        Grade updated = new Grade();
        updated.setId(1L);
        when(gradeService.updateGradeAttributes(dto, 1L)).thenReturn(updated);

        ResponseEntity<?> response = gradeController.updateGrade(1L, dto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ResponseMapper.toGrade(updated), response.getBody());
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
        assertEquals(new MessageResponse("Grade deleted successfully").getMessage(),
                ((MessageResponse) response.getBody()).getMessage());
        verify(gradeService, times(1)).deleteGrade(1L);
    }
}
