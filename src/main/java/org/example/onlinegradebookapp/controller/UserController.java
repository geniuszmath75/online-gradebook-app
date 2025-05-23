package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Get all users (teachers)",
            description = "Get a list of all users (teachers) from the database")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single user (teacher)",
            description = "Get a single user (teacher) with given ID",
            responses = {@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))})
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<?> getUserById(@PathVariable long id) {
        User user = userService.findUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update attributes of the single user",
            description = "Update attributes of the single user with given ID")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto,
                                           BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }
        userService.updateUserAttributes(dto, id);
        return new ResponseEntity<>("User updated successfully" ,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user",
            description = "Delete a user by ID from database")
    @Parameter(in = ParameterIn.PATH, name = "id", description = "User ID")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>("User deleted successfully" ,HttpStatus.OK);
    }
}
