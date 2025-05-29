package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.service.SchoolClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class SchoolClassServiceTest {
    private SchoolClassRepository classRepository;
    private SchoolClassService classService;

    @BeforeEach
    public void setUp() {
        classRepository = Mockito.mock(SchoolClassRepository.class);
        classService = new SchoolClassService(classRepository);
    }

    @Test
    void findAllSchoolClasses_shouldReturnList() {
        List<SchoolClass> classes = List.of(new SchoolClass(), new SchoolClass());
        when(classRepository.findAll()).thenReturn(classes);

        List<SchoolClass> result = classService.findAllSchoolClasses();

        assertEquals(2, result.size());
        verify(classRepository).findAll();
    }

    @Test
    void findSchoolClassById_shouldReturnSchoolClass_whenFound() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);

        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        SchoolClass result = classService.findSchoolClassById(1L);

        assertEquals(1L, result.getId());
        verify(classRepository).findById(1L);
    }

    @Test
    void findSchoolClassById_shouldThrowException_whenNotFound() {
        when(classRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.findSchoolClassById(1L));
    }

    @Test
    void addSchoolClass_shouldSaveNewClass_whenNameIsUnique() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("1A");

        when(classRepository.existsByName("1A")).thenReturn(false);

        classService.addSchoolClass(dto);

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classRepository).save(captor.capture());

        assertEquals("1A", captor.getValue().getName());
    }

    @Test
    void addSchoolClass_shouldThrowException_whenNameExists() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("1A");

        when(classRepository.existsByName("1A")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> classService.addSchoolClass(dto));
        verify(classRepository, never()).save(any());
    }

    @Test
    void updateSchoolClass_shouldUpdateClass_whenExists() {
        SchoolClass existing = new SchoolClass();
        existing.setId(1L);
        existing.setCreatedAt(Instant.now());

        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("2A");

        when(classRepository.findById(1L)).thenReturn(Optional.of(existing));

        classService.updateSchoolClass(dto, 1L);

        ArgumentCaptor<SchoolClass> captor = ArgumentCaptor.forClass(SchoolClass.class);
        verify(classRepository).save(captor.capture());

        SchoolClass updated = captor.getValue();
        assertEquals("2A", updated.getName());
        assertEquals(1L, updated.getId());
        assertEquals(existing.getCreatedAt(), updated.getCreatedAt());
    }

    @Test
    void updateSchoolClass_shouldThrowException_whenNotFound() {
        SchoolClassDto dto = new SchoolClassDto();
        dto.setName("AnyName");

        when(classRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.updateSchoolClass(dto, 1L));
        verify(classRepository, never()).save(any());
    }

    @Test
    void deleteSchoolClass_shouldDelete_whenExists() {
        when(classRepository.existsById(1L)).thenReturn(true);

        classService.deleteSchoolClass(1L);

        verify(classRepository).deleteById(1L);
    }

    @Test
    void deleteSchoolClass_shouldThrowException_whenNotExists() {
        when(classRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> classService.deleteSchoolClass(1L));
        verify(classRepository, never()).deleteById(anyLong());
    }

}
