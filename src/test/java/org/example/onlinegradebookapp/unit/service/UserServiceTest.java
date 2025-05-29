package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
    private PasswordEncoder passwordEncoder;

    private UserRegistrationDto registrationDto;
    private UserUpdateDto updateDto;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        classRepository = Mockito.mock(SchoolClassRepository.class);
        subjectRepository = Mockito.mock(SubjectRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder, classRepository, subjectRepository);

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
                .isInstanceOf(BadRequestException.class)
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
                .isInstanceOf(BadRequestException.class)
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

        assertThrows(BadRequestException.class, () -> userService.register(registrationDto));
    }

    @Test
    void register_shouldThrowException_whenSecondAdminIsRegistered() {
        registrationDto.setRole(UserRole.ADMIN);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.countByRole(UserRole.ADMIN)).thenReturn(1L);

        assertThrows(BadRequestException.class, () -> userService.register(registrationDto));
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
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrow_WhenNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

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
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email 'existing@gmail.com' already exists");
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

        assertThatThrownBy(() -> userService.updateUserAttributes(dto, userId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("There can only be one ADMIN user.");
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

}
