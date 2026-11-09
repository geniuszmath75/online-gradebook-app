package org.example.onlinegradebookapp.payload.response;

import lombok.Data;

import java.time.Instant;

@Data
public class StudentResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant updatedAt;
    private Long classId;
}
