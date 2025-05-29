package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.SubjectController;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.service.SubjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SubjectControllerTest {
    private SubjectService subjectService;
    private SubjectController subjectController;

    @BeforeEach
    public void setUp() {
        subjectService = Mockito.mock(SubjectService.class);
        subjectController = new SubjectController(subjectService);
    }

    @Test
    void getAllSubjects_shouldReturnListAndOk() {
        List<Subject> subjects = Arrays.asList(new Subject(), new Subject());
        when(subjectService.findAllSubjects()).thenReturn(subjects);

        ResponseEntity<?> response = subjectController.getAllSubjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subjects, response.getBody());
        verify(subjectService, times(1)).findAllSubjects();
    }

    @Test
    void getSubjectById_shouldReturnSubjectAndOk() {
        Subject subject = new Subject();
        when(subjectService.findSubjectById(1L)).thenReturn(subject);

        ResponseEntity<?> response = subjectController.getSubjectById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(subject, response.getBody());
        verify(subjectService, times(1)).findSubjectById(1L);
    }

    @Test
    void createSubject_shouldCallServiceAndReturnCreated() {
        SubjectDto dto = new SubjectDto();
        dto.setName("Subject");

        ResponseEntity<?> response = subjectController.createSubject(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Subject created successfully", response.getBody());
        verify(subjectService, times(1)).addSubject(dto);
    }

    @Test
    void updateSubject_shouldCallServiceAndReturnOk() {
        SubjectDto dto = new SubjectDto();
        dto.setName("Subject");

        ResponseEntity<?> response = subjectController.updateSubject(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Subject updated successfully", response.getBody());
        verify(subjectService, times(1)).updateSubject(dto, 1L);
    }

    @Test
    void deleteSubject_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = subjectController.deleteSubject(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Subject deleted successfully", response.getBody());
        verify(subjectService, times(1)).deleteSubject(1L);
    }
}
