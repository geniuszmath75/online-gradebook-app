package org.example.onlinegradebookapp.payload.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
