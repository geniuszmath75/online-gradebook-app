package org.example.onlinegradebookapp.payload.response;

import lombok.Data;

import java.time.Instant;

@Data
public class SubjectResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
