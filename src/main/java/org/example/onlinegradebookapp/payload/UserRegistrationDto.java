package org.example.onlinegradebookapp.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;

@Data
public class UserRegistrationDto {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Firstname cannot be empty")
    private String firstName;

    @NotBlank(message = "Lastname cannot be empty")
    private String lastName;

    @Pattern(regexp = "TEACHER|ADMIN", message = "Allowed roles: TEACHER, ADMIN")
    private UserRole role;

    private Long classId;
}
