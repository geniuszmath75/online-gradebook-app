package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.payload.response.MessageResponse;
import org.example.onlinegradebookapp.payload.response.ResponseMapper;
import org.example.onlinegradebookapp.payload.response.UserResponse;
import org.example.onlinegradebookapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations for users(TEACHER/ADMIN)")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (teachers)",
            description = "Get a list of all users (teachers) from the database")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers().stream()
                .map(ResponseMapper::toUser)
                .toList();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get a single user (teacher)",
            description = "Get a single user (teacher) with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserResponse.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        UserResponse user = ResponseMapper.toUser(userService.findUserById(id));
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Update attributes of the single user",
            description = "Update attributes of the single user with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto,
                                           BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        UserResponse updated = ResponseMapper.toUser(userService.updateUserAttributes(dto, id));
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(value = "hasRole('ADMIN')")
    @Operation(summary = "Delete a user",
            description = "Delete a user by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(new MessageResponse("User deleted successfully"), HttpStatus.OK);
    }
}
