package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.service.KnowledgeTestService;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KnowledgeTestServiceTest {
    private KnowledgeTestRepository testRepository;
    private SchoolClassRepository classRepository;
    private SubjectRepository subjectRepository;
    private UserRepository userRepository;
    private KnowledgeTestService testService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        testRepository = Mockito.mock(KnowledgeTestRepository.class);
        classRepository = Mockito.mock(SchoolClassRepository.class);
        subjectRepository = Mockito.mock(SubjectRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        userService = Mockito.mock(UserService.class);
        testService = new KnowledgeTestService(testRepository, classRepository,
                subjectRepository, userRepository, userService);
    }

    @Test
    void findAllKnowledgeTests_shouldReturnList() {
        List<KnowledgeTest> tests = List.of(new KnowledgeTest(), new KnowledgeTest());
        when(testRepository.findAll()).thenReturn(tests);

        List<KnowledgeTest> result = testService.findAllKnowledgeTests();

        assertEquals(2, result.size());
    }

    @Test
    void findKnowledgeTestById_shouldReturnKnowledgeTest_whenFound() {
        KnowledgeTest test = new KnowledgeTest();
        test.setId(1L);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));

        KnowledgeTest result = testService.findKnowledgeTestById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findKnowledgeTestById_shouldThrowException_whenNotFound() {
        when(testRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> testService.findKnowledgeTestById(1L));
    }

    @Test
    void addKnowledgeTest_shouldSaveNewTest() {
        KnowledgeTestDto dto = new KnowledgeTestDto();
        dto.setName("Test 1");
        dto.setCategory(TestCategory.QUIZ);
        dto.setTestDate(LocalDate.now().plusDays(1));
        dto.setClassId(10L);
        dto.setSubjectId(20L);

        SchoolClass schoolClass = new SchoolClass();
        Subject subject = new Subject();
        User teacher = new User();
        teacher.setId(100L);

        when(testRepository.existsByName("Test 1")).thenReturn(false);
        when(classRepository.findById(10L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findById(20L)).thenReturn(Optional.of(subject));
        when(userService.getCurrentUserId()).thenReturn(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(teacher));

        testService.addKnowledgeTest(dto);

        verify(testRepository).save(any(KnowledgeTest.class));
    }

    @Test
    void addKnowledgeTest_shouldThrowException_whenAddingDuplicateTest() {
        KnowledgeTestDto dto = new KnowledgeTestDto();
        dto.setName("Test 1");

        when(testRepository.existsByName("Test 1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> testService.addKnowledgeTest(dto));
    }

    @Test
    void updateKnowledgeTestAttributes_shouldUpdateKnowledgeTest_whenExists() {
        KnowledgeTestUpdateDto dto = new KnowledgeTestUpdateDto();
        dto.setName("Updated Test");
        dto.setCategory(TestCategory.HOMEWORK);
        dto.setTestDate(LocalDate.now().plusDays(2));

        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(1L);
        test.setTeacher(teacher);
        test.setId(1L);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        testService.updateKnowledgeTestAttributes(dto, 1L);

        verify(testRepository).save(test);
        assertEquals("Updated Test", test.getName());
        assertEquals(TestCategory.HOMEWORK, test.getCategory());
        assertEquals(LocalDate.now().plusDays(2), test.getTestDate());
    }

    @Test
    void updateKnowledgeTestAttributes_shouldThrowException_whenUnauthorizedToUpdate() {
        KnowledgeTestUpdateDto dto = new KnowledgeTestUpdateDto();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(1L);
        test.setTeacher(teacher);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> testService.updateKnowledgeTestAttributes(dto, 1L));
    }

    @Test
    void deleteKnowledgeTest_shouldDelete_whenExists() {
        KnowledgeTest test = new KnowledgeTest();
        test.setId(1L);

        User teacher = new User();
        teacher.setId(2L);
        test.setTeacher(teacher);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        testService.deleteKnowledgeTest(1L);

        verify(testRepository).deleteById(1L);
    }

    @Test
    void deleteKnowledgeTest_shouldThrowException_whenUnauthorizedToDelete() {
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(1L);
        test.setTeacher(teacher);

        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(2L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> testService.deleteKnowledgeTest(1L));
    }
}
