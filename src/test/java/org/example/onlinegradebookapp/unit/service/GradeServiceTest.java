package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.event.GradeCreatedEvent;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ForbiddenException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.repository.GradeRepository;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.service.GradeService;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class GradeServiceTest {
    private GradeRepository gradeRepository;
    private StudentRepository studentRepository;
    private KnowledgeTestRepository testRepository;
    private GradeService gradeService;
    private UserService userService;
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    public void setUp() {
        gradeRepository = Mockito.mock(GradeRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        testRepository = Mockito.mock(KnowledgeTestRepository.class);
        userService = Mockito.mock(UserService.class);
        eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        gradeService = new GradeService(gradeRepository, studentRepository, testRepository, userService, eventPublisher);
    }

    @Test
    void findAllGrades_shouldReturnPage() {
        Grade g1 = new Grade(); Grade g2 = new Grade();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Grade> page = new PageImpl<>(List.of(g1, g2), pageable, 2);
        when(gradeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Grade> result = gradeService.findAllGrades(1L, 2L, null, null, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(gradeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAllGrades_shouldUseCurrentTeacherForNonAdmin() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(gradeRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        gradeService.findAllGrades(null, null, null, null, pageable);

        verify(userService).getCurrentUserId();
        verify(gradeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findAllGrades_shouldThrowBadRequest_whenDateRangeInvalid() {
        Pageable pageable = PageRequest.of(0, 20);
        Instant from = Instant.parse("2024-06-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-01T00:00:00Z");

        assertThrows(BadRequestException.class,
                () -> gradeService.findAllGrades(null, null, from, to, pageable));
        verify(gradeRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findGradeById_shouldReturnGrade_whenExists() {
        Grade grade = new Grade();
        grade.setId(1L);
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);
        grade.setTest(test);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        Grade result = gradeService.findGradeById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findGradeById_shouldThrowException_whenNotFound() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.findGradeById(1L));
    }

    @Test
    void addGrade_shouldSaveNewGrade_whenDataIsValidAndAuthorized() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(5.0));

        Student student = new Student();
        student.setId(1L);

        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);
        test.setId(2L);

        when(gradeRepository.existsByStudentIdAndTestId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> {
            Grade g = invocation.getArgument(0);
            g.setId(100L);
            return g;
        });

        gradeService.addGrade(dto);

        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).save(captor.capture());

        Grade capturedGrade = captor.getValue();
        assertEquals(BigDecimal.valueOf(5.0), capturedGrade.getGrade());
        assertEquals(student, captor.getValue().getStudent());
        assertEquals(test, captor.getValue().getTest());

        ArgumentCaptor<GradeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(GradeCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        GradeCreatedEvent event = eventCaptor.getValue();
        assertEquals(100L, event.gradeId());
        assertEquals(1L, event.studentId());
        assertEquals(2L, event.testId());
        assertEquals(BigDecimal.valueOf(5.0), event.gradeValue());
        assertEquals(10L, event.createdByUserId());
    }


    @Test
    void addGrade_shouldThrowException_whenStudentAlreadyHasGradeForTest() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        Student student = new Student();
        student.setId(1L);
        KnowledgeTest test = new KnowledgeTest();
        test.setId(2L);
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(gradeRepository.existsByStudentIdAndTestId(1L, 2L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void addGrade_shouldSave_whenStudentAndTestHaveOtherGradesButNotThisPair() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(2L);
        dto.setTestId(3L);
        dto.setGrade(BigDecimal.valueOf(3.5));

        Student student = new Student();
        student.setId(2L);
        KnowledgeTest test = new KnowledgeTest();
        test.setId(3L);
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);

        when(gradeRepository.existsByStudentIdAndTestId(2L, 3L)).thenReturn(false);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(testRepository.findById(3L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            g.setId(200L);
            return g;
        });

        Grade saved = gradeService.addGrade(dto);

        assertEquals(200L, saved.getId());
        assertEquals(student, saved.getStudent());
        assertEquals(test, saved.getTest());
        verify(gradeRepository).existsByStudentIdAndTestId(2L, 3L);
    }

    @Test
    void addGrade_shouldThrowException_whenStudentNotFound() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        when(gradeRepository.existsByStudentIdAndTestId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void addGrade_shouldThrowException_whenTestNotFound() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        Student student = new Student();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void addGrade_shouldThrowUnauthorizedException_whenUserNotAuthorOrAdmin() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        Student student = new Student();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(99L);
        test.setTeacher(teacher);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void updateGradeAttributes_shouldUpdateGrade_whenAuthorizedAndValid() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setGrade(BigDecimal.valueOf(3.5));

        Grade grade = new Grade();
        grade.setId(1L);
        grade.setGrade(BigDecimal.valueOf(2.0));
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);

        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(11L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

        gradeService.updateGradeAttributes(dto, 1L);

        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).save(captor.capture());

        assertEquals(BigDecimal.valueOf(3.5), captor.getValue().getGrade());
    }


    @Test
    void updateGradeAttributes_shouldThrowNotFoundException_whenGradeNotFound() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> gradeService.updateGradeAttributes(new GradeUpdateDto(), 1L));
    }

    @Test
    void updateGradeAttributes_shouldThrowUnauthorizedException_whenUserNotAuthorOrAdmin() {
        Grade grade = new Grade();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(100L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setGrade(BigDecimal.valueOf(4.0));

        assertThrows(ForbiddenException.class, () -> gradeService.updateGradeAttributes(dto, 1L));
    }

    @Test
    void deleteGrade_shouldDeleteGrade() {
        Grade grade = new Grade();
        grade.setId(1L);

        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        gradeService.deleteGrade(1L);

        verify(gradeRepository).deleteById(1L);
    }


    @Test
    void deleteGrade_shouldThrowNotFoundException_whenGradeNotFound() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> gradeService.deleteGrade(1L));
    }

    @Test
    void deleteGrade_shouldThrowUnauthorizedException_whenUserNotAuthorOrAdmin() {
        Grade grade = new Grade();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(100L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> gradeService.deleteGrade(1L));
    }

    @Test
    void addGrade_shouldSetDescription_whenProvided() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.0));
        dto.setDescription("Nice work");

        Student student = new Student();
        student.setId(1L);
        KnowledgeTest test = new KnowledgeTest();
        test.setId(2L);
        User teacher = new User();
        teacher.setId(10L);
        test.setTeacher(teacher);

        when(gradeRepository.existsByStudentIdAndTestId(1L, 2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> {
            Grade g = inv.getArgument(0);
            g.setId(7L);
            return g;
        });

        Grade saved = gradeService.addGrade(dto);

        assertEquals("Nice work", saved.getDescription());
    }

    @Test
    void updateGradeAttributes_shouldUpdateDescriptionStudentAndTest_whenAuthorized() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setDescription("Updated note");
        dto.setStudentId(3L);
        dto.setTestId(4L);

        Grade grade = new Grade();
        grade.setId(1L);
        KnowledgeTest originalTest = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(11L);
        originalTest.setTeacher(teacher);
        grade.setTest(originalTest);

        Student newStudent = new Student();
        newStudent.setId(3L);
        KnowledgeTest newTest = new KnowledgeTest();
        newTest.setId(4L);
        User newTestTeacher = new User();
        newTestTeacher.setId(11L);
        newTest.setTeacher(newTestTeacher);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(studentRepository.findById(3L)).thenReturn(Optional.of(newStudent));
        when(testRepository.findById(4L)).thenReturn(Optional.of(newTest));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

        Grade updated = gradeService.updateGradeAttributes(dto, 1L);

        assertEquals("Updated note", updated.getDescription());
        assertEquals(newStudent, updated.getStudent());
        assertEquals(newTest, updated.getTest());
    }

    @Test
    void updateGradeAttributes_shouldRejectChangingToAnotherTeachersTest() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setTestId(4L);

        Grade grade = new Grade();
        grade.setId(1L);
        Student student = new Student();
        student.setId(3L);
        grade.setStudent(student);

        KnowledgeTest originalTest = new KnowledgeTest();
        User owner = new User();
        owner.setId(11L);
        originalTest.setTeacher(owner);
        grade.setTest(originalTest);

        KnowledgeTest otherTeacherTest = new KnowledgeTest();
        otherTeacherTest.setId(4L);
        User otherTeacher = new User();
        otherTeacher.setId(99L);
        otherTeacherTest.setTeacher(otherTeacher);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(testRepository.findById(4L)).thenReturn(Optional.of(otherTeacherTest));

        assertThrows(ForbiddenException.class,
                () -> gradeService.updateGradeAttributes(dto, 1L));
        verify(gradeRepository, never()).save(any(Grade.class));
    }

    @Test
    void updateGradeAttributes_shouldThrow_whenStudentNotFound() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setStudentId(99L);

        Grade grade = new Grade();
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(11L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> gradeService.updateGradeAttributes(dto, 1L));
    }

    @Test
    void updateGradeAttributes_shouldThrow_whenTestNotFound() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setTestId(99L);

        Grade grade = new Grade();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(11L);
        test.setTeacher(teacher);
        grade.setTest(test);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(11L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);
        when(testRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> gradeService.updateGradeAttributes(dto, 1L));
    }

    @Test
    void updateGradeAttributes_shouldAllowAdmin() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setGrade(BigDecimal.valueOf(5.0));

        Grade grade = new Grade();
        KnowledgeTest test = new KnowledgeTest();
        User teacher = new User();
        teacher.setId(100L);
        test.setTeacher(teacher);
        grade.setTest(test);
        Student student = new Student();
        student.setId(1L);
        grade.setStudent(student);

        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(inv -> inv.getArgument(0));

        Grade updated = gradeService.updateGradeAttributes(dto, 1L);

        assertEquals(BigDecimal.valueOf(5.0), updated.getGrade());
    }
}
