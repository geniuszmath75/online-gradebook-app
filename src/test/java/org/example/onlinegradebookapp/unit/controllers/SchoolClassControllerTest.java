package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.SchoolClassController;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.payload.response.MessageResponse;
import org.example.onlinegradebookapp.payload.response.ResponseMapper;
import org.example.onlinegradebookapp.payload.response.SchoolClassResponse;
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
        List<SchoolClassResponse> classes = Arrays.asList(
                new SchoolClassResponse(1L, "1A", null, null),
                new SchoolClassResponse(2L, "1B", null, null)
        );
        when(classService.findAllSchoolClassResponses()).thenReturn(classes);

        ResponseEntity<?> response = classController.getAllSchoolClasses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(classes, response.getBody());
        verify(classService, times(1)).findAllSchoolClassResponses();
    }

    @Test
    void getSchoolClassById_shouldReturnClassAndOk() {
        SchoolClassResponse schoolClass = new SchoolClassResponse(1L, "1A", null, null);
        when(classService.findSchoolClassResponseById(1L)).thenReturn(schoolClass);

        ResponseEntity<?> response = classController.getSchoolClassById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schoolClass, response.getBody());
        verify(classService, times(1)).findSchoolClassResponseById(1L);
    }

    @Test
    void createSchoolClass_shouldCallServiceAndReturnCreated() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("School Class");
        SchoolClass created = new SchoolClass();
        created.setId(1L);
        created.setName("School Class");
        when(classService.addSchoolClass(dto)).thenReturn(created);

        ResponseEntity<?> response = classController.createSchoolClass(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ResponseMapper.toSchoolClass(created), response.getBody());
        verify(classService, times(1)).addSchoolClass(dto);
    }

    @Test
    void updateSchoolClass_shouldCallServiceAndReturnOk() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("School Class");
        SchoolClass updated = new SchoolClass();
        updated.setId(1L);
        updated.setName("School Class");
        when(classService.updateSchoolClass(dto, 1L)).thenReturn(updated);

        ResponseEntity<?> response = classController.updateSchoolClass(1L, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ResponseMapper.toSchoolClass(updated), response.getBody());
        verify(classService, times(1)).updateSchoolClass(dto, 1L);
    }

    @Test
    void deleteSchoolClass_shouldCallServiceAndReturnOk() {
        ResponseEntity<?> response = classController.deleteSchoolClass(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("School class deleted successfully", ((MessageResponse) response.getBody()).getMessage());
        verify(classService, times(1)).deleteSchoolClass(1L);
    }
}
