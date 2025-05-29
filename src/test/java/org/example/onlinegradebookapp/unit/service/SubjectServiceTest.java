package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.service.SubjectService;
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

public class SubjectServiceTest {
    private SubjectRepository subjectRepository;
    private SubjectService subjectService;

    @BeforeEach
    public void setUp() {
        subjectRepository = Mockito.mock(SubjectRepository.class);
        subjectService = new SubjectService(subjectRepository);
    }

    @Test
    void findAllSubjects_shouldReturnList() {
        List<Subject> subjects = List.of(new Subject(), new Subject());
        when(subjectRepository.findAll()).thenReturn(subjects);

        List<Subject> result = subjectService.findAllSubjects();

        assertEquals(2, result.size());
        verify(subjectRepository).findAll();
    }

    @Test
    void findSubjectById_shouldReturnSubject_whenFound() {
        Subject subject = new Subject();
        subject.setId(1L);

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));

        Subject result = subjectService.findSubjectById(1L);

        assertEquals(1L, result.getId());
        verify(subjectRepository).findById(1L);
    }

    @Test
    void findSubjectById_shouldThrowException_whenNotFound() {
        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.findSubjectById(1L));
    }

    @Test
    void addSubject_shouldSaveNewSubject_whenNameIsUnique() {
        SubjectDto dto = new SubjectDto();
        dto.setName("matematyka");

        when(subjectRepository.existsByName("matematyka")).thenReturn(false);

        subjectService.addSubject(dto);

        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectRepository).save(captor.capture());

        assertEquals("matematyka", captor.getValue().getName());
    }

    @Test
    void addSubject_shouldThrowException_whenNameExists() {
        SubjectDto dto = new SubjectDto();
        dto.setName("matematyka");

        when(subjectRepository.existsByName("matematyka")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> subjectService.addSubject(dto));
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void updateSubject_shouldUpdateSubject_whenExists() {
        Subject existing = new Subject();
        existing.setId(1L);
        existing.setCreatedAt(Instant.now());

        SubjectDto dto = new SubjectDto();
        dto.setName("historia");

        when(subjectRepository.findById(1L)).thenReturn(Optional.of(existing));

        subjectService.updateSubject(dto, 1L);

        ArgumentCaptor<Subject> captor = ArgumentCaptor.forClass(Subject.class);
        verify(subjectRepository).save(captor.capture());

        Subject updated = captor.getValue();
        assertEquals("historia", updated.getName());
        assertEquals(1L, updated.getId());
        assertEquals(existing.getCreatedAt(), updated.getCreatedAt());
    }

    @Test
    void updateSubject_shouldThrowException_whenNotFound() {
        SubjectDto dto = new SubjectDto();
        dto.setName("AnyName");

        when(subjectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> subjectService.updateSubject(dto, 1L));
        verify(subjectRepository, never()).save(any());
    }

    @Test
    void deleteSubject_shouldDelete_whenExists() {
        when(subjectRepository.existsById(1L)).thenReturn(true);

        subjectService.deleteSubject(1L);

        verify(subjectRepository).deleteById(1L);
    }

    @Test
    void deleteSubject_shouldThrowException_whenNotExists() {
        when(subjectRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> subjectService.deleteSubject(1L));
        verify(subjectRepository, never()).deleteById(anyLong());
    }
}
