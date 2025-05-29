package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.UserController;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserControllerTest {
    private UserService userService;
    private UserController userController;

    @BeforeEach
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void getAllUsers_shouldReturnUserListAndOkStatus() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userService.findAllUsers()).thenReturn(users);

        ResponseEntity<?> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(userService, times(1)).findAllUsers();
    }

    @Test
    void getUserById_shouldReturnUserAndOkStatus() {
        User user = new User();
        user.setEmail("teacher@gmail.com");
        user.setPassword("password");
        user.setFirstName("Teacher");
        user.setLastName("One");
        user.setUserRole(UserRole.TEACHER);
        when(userService.findUserById(1L)).thenReturn(user);

        ResponseEntity<?> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
        verify(userService, times(1)).findUserById(1L);
    }

    @Test
    void updateUser_shouldCallServiceAndReturnOkStatus_whenNoValidationErrors() {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFirstName("UpdatedFirstName");
        dto.setLastName("UpdatedLastName");
        dto.setEmail("updated@gmail.com");
        dto.setPassword("updatedpassword");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = userController.updateUser(1L, dto, bindingResult);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User updated successfully", response.getBody());
        verify(userService, times(1)).updateUserAttributes(dto, 1L);
    }

    @Test
    void updateUser_shouldThrowBadRequestException_whenValidationErrorsPresent() {
        UserUpdateDto dto = new UserUpdateDto();
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(
                Collections.singletonList(new ObjectError("user", "Invalid input"))
        );

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                userController.updateUser(1L, dto, bindingResult)
        );

        assertEquals("Invalid input", exception.getMessage());
        verify(userService, never()).updateUserAttributes(any(), any());
    }

    @Test
    void deleteUser_shouldCallServiceAndReturnOkStatus() {
        ResponseEntity<?> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully", response.getBody());
        verify(userService, times(1)).deleteUser(1L);
    }
}
