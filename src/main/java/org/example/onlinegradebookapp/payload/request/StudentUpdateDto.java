package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.onlinegradebookapp.validation.AtLeastOneField;

@Data
@AtLeastOneField(fields = {"email", "password", "firstName", "lastName", "classId"})
public class StudentUpdateDto {
    @Email(message = "Invalid email")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String firstName;

    private String lastName;

    private Long classId;
}
