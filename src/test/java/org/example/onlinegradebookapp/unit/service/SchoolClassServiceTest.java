package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SchoolClassDto;
import org.example.onlinegradebookapp.payload.response.SchoolClassResponse;
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
    void findAllSchoolClassResponses_shouldReturnList() {
        List<SchoolClassResponse> classes = List.of(
                new SchoolClassResponse(1L, "1A", null, null),
                new SchoolClassResponse(2L, "1B", null, null)
        );
        when(classRepository.findAllResponses()).thenReturn(classes);

        List<SchoolClassResponse> result = classService.findAllSchoolClassResponses();

        assertEquals(2, result.size());
        verify(classRepository).findAllResponses();
    }

    @Test
    void findSchoolClassResponseById_shouldReturnResponse_whenFound() {
        SchoolClassResponse schoolClass = new SchoolClassResponse(1L, "1A", null, null);

        when(classRepository.findResponseById(1L)).thenReturn(Optional.of(schoolClass));

        SchoolClassResponse result = classService.findSchoolClassResponseById(1L);

        assertEquals(1L, result.getId());
        verify(classRepository).findResponseById(1L);
    }

    @Test
    void findSchoolClassResponseById_shouldThrowException_whenNotFound() {
        when(classRepository.findResponseById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.findSchoolClassResponseById(1L));
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

        assertThrows(ConflictException.class, () -> classService.addSchoolClass(dto));
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
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        classService.deleteSchoolClass(1L);

        verify(classRepository).delete(schoolClass);
    }

    @Test
    void deleteSchoolClass_shouldThrowException_whenNotExists() {
        when(classRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> classService.deleteSchoolClass(1L));
        verify(classRepository, never()).delete(any(SchoolClass.class));
    }

    @Test
    void deleteSchoolClass_shouldReject_whenTeacherIsAssigned() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.setTeacher(new User());
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> classService.deleteSchoolClass(1L)
        );

        assertEquals("Cannot delete school class with id=1 because it has related teacher", exception.getMessage());
        verify(classRepository, never()).delete(any(SchoolClass.class));
    }

    @Test
    void deleteSchoolClass_shouldReject_whenStudentsAreAssigned() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.getStudents().add(new Student());
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> classService.deleteSchoolClass(1L)
        );

        assertEquals("Cannot delete school class with id=1 because it has related students", exception.getMessage());
        verify(classRepository, never()).delete(any(SchoolClass.class));
    }

    @Test
    void deleteSchoolClass_shouldReject_whenTestsAreAssigned() {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);
        schoolClass.getTests().add(new KnowledgeTest());
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> classService.deleteSchoolClass(1L)
        );

        assertEquals("Cannot delete school class with id=1 because it has related tests", exception.getMessage());
        verify(classRepository, never()).delete(any(SchoolClass.class));
    }

}
