package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GradeServiceTest {
    private GradeRepository gradeRepository;
    private StudentRepository studentRepository;
    private KnowledgeTestRepository testRepository;
    private GradeService gradeService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        gradeRepository = Mockito.mock(GradeRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        testRepository = Mockito.mock(KnowledgeTestRepository.class);
        userService = Mockito.mock(UserService.class);
        gradeService = new GradeService(gradeRepository, studentRepository, testRepository, userService);
    }

    @Test
    void findAllGrades_shouldReturnList() {
        Grade g1 = new Grade(); Grade g2 = new Grade();
        when(gradeRepository.findAll()).thenReturn(List.of(g1, g2));

        List<Grade> result = gradeService.findAllGrades();

        assertEquals(2, result.size());
    }

    @Test
    void findGradeById_shouldReturnGrade_whenExists() {
        Grade grade = new Grade();
        grade.setId(1L);
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));

        Grade result = gradeService.findGradeById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findGradeById_shouldThrowException_whenNotFound() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> gradeService.findGradeById(1L));
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

        when(gradeRepository.existsGradeByStudentId(1L)).thenReturn(false);
        when(gradeRepository.existsGradeByTestId(2L)).thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test));
        when(userService.getCurrentUserId()).thenReturn(10L);
        when(userService.hasRole(UserRole.ADMIN)).thenReturn(false);

        gradeService.addGrade(dto);

        ArgumentCaptor<Grade> captor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository).save(captor.capture());

        Grade capturedGrade = captor.getValue();
        assertEquals(BigDecimal.valueOf(5.0), capturedGrade.getGrade());
        assertEquals(student, captor.getValue().getStudent());
        assertEquals(test, captor.getValue().getTest());
    }


    @Test
    void addGrade_shouldThrowException_whenStudentAlreadyHasGradeForTest() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        when(gradeRepository.existsGradeByStudentId(1L)).thenReturn(true);
        when(gradeRepository.existsGradeByTestId(2L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void addGrade_shouldThrowException_whenStudentNotFound() {
        GradeDto dto = new GradeDto();
        dto.setStudentId(1L);
        dto.setTestId(2L);
        dto.setGrade(BigDecimal.valueOf(4.5));

        when(gradeRepository.existsGradeByStudentId(1L)).thenReturn(false);
        when(gradeRepository.existsGradeByTestId(2L)).thenReturn(false);
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

        assertThrows(UnauthorizedException.class, () -> gradeService.addGrade(dto));
    }

    @Test
    void updateGradeAttributes_shouldUpdateGrade_whenAuthorizedAndValid() {
        GradeUpdateDto dto = new GradeUpdateDto();
        dto.setGrade(BigDecimal.valueOf(3.5));

        Grade grade = new Grade();
        grade.setId(1L);
        grade.setGrade(BigDecimal.valueOf(2.0));

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

        assertThrows(UnauthorizedException.class, () -> gradeService.updateGradeAttributes(dto, 1L));
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

        assertThrows(UnauthorizedException.class, () -> gradeService.deleteGrade(1L));
    }
}
