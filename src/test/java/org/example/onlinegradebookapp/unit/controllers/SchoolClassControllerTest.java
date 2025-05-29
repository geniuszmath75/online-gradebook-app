package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.SchoolClassController;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.service.SchoolClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SchoolClassControllerTest {
    private SchoolClassService classService;
    private SchoolClassController classController;

    @BeforeEach
    void setUp() {
        classService = Mockito.mock(SchoolClassService.class);
        classController = new SchoolClassController(classService);
    }

    @Test
    void getAllSchoolClasses_shouldReturnListAndOk() {
        List<SchoolClass> classes = Arrays.asList(new SchoolClass(), new SchoolClass());
        when(classService.findAllSchoolClasses()).thenReturn(classes);

        ResponseEntity<?> response = classController.getAllSchoolClasses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(classes, response.getBody());
        verify(classService, times(1)).findAllSchoolClasses();
    }

    @Test
    void getSchoolClassById_shouldReturnClassAndOk() {
        SchoolClass schoolClass = new SchoolClass();
        when(classService.findSchoolClassById(1L)).thenReturn(schoolClass);

        ResponseEntity<?> response = classController.getSchoolClassById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schoolClass, response.getBody());
        verify(classService, times(1)).findSchoolClassById(1L);
    }

    @Test
    void createSchoolClass_shouldCallServiceAndReturnCreated() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("School Class");

        ResponseEntity<?> response = classController.createSchoolClass(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("School class created successfully", response.getBody());
        verify(classService, times(1)).addSchoolClass(dto);
    }

    @Test
    void updateSchoolClass_shouldCallServiceAndReturnOk() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("School Class");

        ResponseEntity<?> response = classController.updateSchoolClass(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("School class updated successfully", response.getBody());
        verify(classService, times(1)).updateSchoolClass(dto, 1L);
    }

    @Test
    void deleteSchoolClass_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = classController.deleteSchoolClass(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("School class deleted successfully", response.getBody());
        verify(classService, times(1)).deleteSchoolClass(1L);
    }
}
