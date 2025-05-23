package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectDto {
    @NotBlank(message = "Subject name cannot be empty")
    private String name;
}
