package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SchoolClassDto {
    @NotBlank(message = "Class name cannot be empty")
    private String name;
}
