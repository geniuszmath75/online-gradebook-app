package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ForbiddenException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;
    private SchoolClassRepository classRepository;
    private SubjectRepository subjectRepository;
    private KnowledgeTestRepository knowledgeTestRepository;
    private PasswordEncoder passwordEncoder;

    private UserRegistrationDto registrationDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        classRepository = Mockito.mock(SchoolClassRepository.class);
        subjectRepository = Mockito.mock(SubjectRepository.class);
        knowledgeTestRepository = Mockito.mock(KnowledgeTestRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder, classRepository, subjectRepository, knowledgeTestRepository);
        SecurityContextHolder.clearContext();

        registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("test@gmail.com");
        registrationDto.setPassword("secure123");
        registrationDto.setFirstName("Jan");
        registrationDto.setLastName("Kowalski");
        registrationDto.setRole(UserRole.TEACHER);

        updateDto = new UserUpdateDto();
        updateDto.setEmail("updated@gmail.com");
        updateDto.setFirstName("Adam");
        updateDto.setPassword("newPassword");
        updateDto.setLastName("Nowak");
    }

    @Test
    void register_ShouldSaveUser_whenValidData() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@gmail.com");
        dto.setPassword("secret123");
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");
        dto.setRole(UserRole.TEACHER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        userService.register(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo(dto.getEmail());
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getFirstName()).isEqualTo(dto.getFirstName());
        assertThat(saved.getLastName()).isEqualTo(dto.getLastName());
        assertThat(saved.getRole()).isEqualTo(UserRole.TEACHER);
    }

    @Test
    void register_ShouldThrow_WhenEmailExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@gmail.com");
        dto.setPassword("secret123");
        dto.setFirstName("Jan");
        dto.setLastName("Kowalski");
        dto.setRole(UserRole.TEACHER);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void register_ShouldThrow_WhenAdminAlreadyExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("admin@gmail.com");
        dto.setPassword("admin123");
        dto.setFirstName("Admin");
        dto.setLastName("User");
        dto.setRole(UserRole.ADMIN);

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.register(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("only be one ADMIN");
    }

    @Test
    void register_shouldSaveNewUser_whenEmailDoesNotExistAndRoleNotAdmin() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        userService.register(registrationDto);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(registrationDto));
    }

    @Test
    void register_shouldThrowException_whenSecondAdminIsRegistered() {
        registrationDto.setRole(UserRole.ADMIN);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(ConflictException.class, () -> userService.register(registrationDto));
    }

    @Test
    void register_shouldThrowBadRequest_whenRoleIsMissing() {
        registrationDto.setRole(null);

        assertThatThrownBy(() -> userService.register(registrationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Role cannot be null");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldAssignClass_whenClassIdProvided() {
        Long classId = 1L;
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);

        registrationDto.setClassId(classId);

        when(classRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);

        userService.register(registrationDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getSchoolClass()).isEqualTo(schoolClass);
    }

    @Test
    void register_shouldThrowBadRequest_whenClassIdNotFound() {
        Long classId = 99L;
        registrationDto.setClassId(classId);

        when(classRepository.findById(classId)).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> userService.register(registrationDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Class with id=" + classId + " not found");
    }

    @Test
    void register_shouldAssignSubjects_whenSubjectsProvided() {
        SubjectDto subjectDto1 = new SubjectDto();
        subjectDto1.setName("Math");
        SubjectDto subjectDto2 = new SubjectDto();
        subjectDto2.setName("Physics");

        Subject math = new Subject();
        math.setName("Math");
        Subject physics = new Subject();
        physics.setName("Physics");

        registrationDto.setSubjects(List.of(subjectDto1, subjectDto2));

        when(userRepository.existsByEmail(registrationDto.getEmail())).thenReturn(false);
        when(subjectRepository.findByName("Math")).thenReturn(Optional.of(math));
        when(subjectRepository.findByName("Physics")).thenReturn(Optional.of(physics));

        userService.register(registrationDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getSubjects()).containsExactlyInAnyOrder(math, physics);
    }

    @Test
    void findAllUsers_ShouldReturnList() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAllUsers();

        assertThat(result).hasSize(2);
    }

    @Test
    void findUserById_ShouldReturnUser_WhenExists() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.findUserById(1L);
        assertThat(found.getId()).isEqualTo(1L);
    }

    @Test
    void findUserById_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteUser_ShouldDelete_WhenExists() {
        User user = new User();
        user.setId(1L);
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(10L);
        schoolClass.setTeacher(user);
        user.setSchoolClass(schoolClass);
        Subject subject = new Subject();
        subject.setId(20L);
        subject.getUsers().add(user);
        user.getSubjects().add(subject);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        assertThat(user.getSchoolClass()).isNull();
        assertThat(schoolClass.getTeacher()).isNull();
        assertThat(user.getSubjects()).isEmpty();
        assertThat(subject.getUsers()).doesNotContain(user);
        verify(knowledgeTestRepository, never()).deleteAll(anyList());
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateUserAttributes_ShouldUpdateFields_WhenValid() {
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setEmail("old@example.com");
        existing.setFirstName("OldName");
        existing.setLastName("OldLastName");
        existing.setPassword("oldPassword");

        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("new@example.com");
        dto.setFirstName("NewName");
        dto.setLastName("NewLastName");
        dto.setPassword("newPassword");

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        userService.updateUserAttributes(dto, id);

        verify(userRepository).save(existing);
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getFirstName()).isEqualTo("NewName");
        assertThat(existing.getLastName()).isEqualTo("NewLastName");
        assertThat(existing.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    void updateUserAttributes_ShouldThrow_WhenUserNotFound() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("new@gmail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserAttributes(dto, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserAttributes_shouldUpdateClassAndSubjects() {
        User user = new User();
        user.setEmail("old@gmail.com");

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(1L);

        Subject math = new Subject();
        math.setName("matematyka");

        SubjectDto mathDto = new SubjectDto();
        mathDto.setName("matematyka");

        updateDto.setClassId(1L);
        updateDto.setSubjects(List.of(mathDto));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(classRepository.findById(1L)).thenReturn(Optional.of(schoolClass));
        when(subjectRepository.findByName("matematyka")).thenReturn(Optional.of(math));

        userService.updateUserAttributes(updateDto, 1L);

        verify(userRepository, times(1)).save(user);
        assertEquals(user.getSchoolClass(), schoolClass);
        assertEquals(user.getSubjects(), List.of(math));
    }

    @Test
    void updateUserAttributes_shouldThrowException_whenClassNotFound() {
        updateDto.setClassId(100L);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(classRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.updateUserAttributes(updateDto, 1L));
    }

    @Test
    void updateUserAttributes_shouldThrowException_whenSubjectNotFound() {
        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setName("Unknown");

        updateDto.setSubjects(List.of(subjectDto));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(subjectRepository.findByName("Unknown")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.updateUserAttributes(updateDto, 1L));
    }

    @Test
    void updateUserAttributes_shouldThrowBadRequest_whenEmailAlreadyExists() {
        // given
        Long userId = 1L;
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("existing@gmail.com");

        User user = new User();
        user.setId(userId);
        user.setEmail("original@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUserAttributes(dto, userId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email 'existing@gmail.com' already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserAttributes_shouldAllowSameEmail() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("same@gmail.com");
        user.setFirstName("Old");

        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail("same@gmail.com");
        dto.setFirstName("New");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUserAttributes(dto, userId);

        assertThat(updated.getEmail()).isEqualTo("same@gmail.com");
        assertThat(updated.getFirstName()).isEqualTo("New");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserAttributes_shouldThrowBadRequest_whenTryingToAssignSecondAdmin() {
        Long userId = 1L;
        UserUpdateDto dto = new UserUpdateDto();
        dto.setRole(UserRole.ADMIN);

        User user = new User();
        user.setId(userId);
        user.setUserRole(UserRole.TEACHER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );

        assertThatThrownBy(() -> userService.updateUserAttributes(dto, userId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("There can only be one ADMIN user.");
    }

    @Test
    void updateUserAttributes_shouldRejectRoleChange_forNonAdmin() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setUserRole(UserRole.TEACHER);

        UserUpdateDto dto = new UserUpdateDto();
        dto.setRole(UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updateUserAttributes(dto, userId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Only ADMIN users can change user roles.");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserAttributes_shouldDetachOldTeacher_whenClassHasAnotherTeacher() {
        Long userId = 1L;
        Long classId = 10L;

        UserUpdateDto dto = new UserUpdateDto();
        dto.setClassId(classId);

        User currentUser = new User();
        currentUser.setId(userId);

        User oldTeacher = new User();
        oldTeacher.setId(99L);
        oldTeacher.setUserRole(UserRole.TEACHER);
        oldTeacher.setSchoolClass(null);

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        schoolClass.setTeacher(oldTeacher);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(classRepository.findById(classId)).thenReturn(Optional.of(schoolClass));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateUserAttributes(dto, userId);

        verify(userRepository).save(argThat(user -> user.getId().equals(99L) && user.getSchoolClass() == null));
    }

    @Test
    void findSubjectList_shouldReturnSubjects_whenAllExist() {
        SubjectDto dto = new SubjectDto();
        dto.setName("matematyka");

        Subject math = new Subject();
        math.setName("matematyka");

        when(subjectRepository.findByName("matematyka")).thenReturn(Optional.of(math));

        List<Subject> result = ReflectionTestUtils.invokeMethod(userService, "findSubjectList", List.of(dto));

        assertEquals(List.of(math), result);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteUser_shouldDeleteOwnedTests_whenPresent() {
        User user = new User();
        user.setId(1L);
        KnowledgeTest test = new KnowledgeTest();
        test.setId(5L);
        user.getTests().add(test);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(knowledgeTestRepository).deleteAll(List.of(test));
        assertThat(user.getTests()).isEmpty();
        verify(userRepository).delete(user);
    }

    @Test
    void updateUserAttributes_shouldChangeRole_whenNotAdminConflict() {
        User existing = new User();
        existing.setId(1L);
        existing.setUserRole(UserRole.TEACHER);

        UserUpdateDto dto = new UserUpdateDto();
        dto.setRole(UserRole.TEACHER);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User updated = userService.updateUserAttributes(dto, 1L);

        assertEquals(UserRole.TEACHER, updated.getRole());
    }

    @Test
    void getCurrentUserId_shouldReturnPrincipalId() {
        CustomUserDetails principal = mock(CustomUserDetails.class);
        when(principal.getId()).thenReturn(42L);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(42L, userService.getCurrentUserId());
    }

    @Test
    void hasRole_shouldReturnTrue_whenAuthorityMatches() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(userService.hasRole(UserRole.ADMIN)).isTrue();
        assertThat(userService.hasRole(UserRole.TEACHER)).isFalse();
    }

    @Test
    void findSubjectList_shouldThrow_whenSubjectMissing() {
        SubjectDto dto = new SubjectDto();
        dto.setName("unknown");
        when(subjectRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(userService, "findSubjectList", List.of(dto)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("unknown");
    }

}
