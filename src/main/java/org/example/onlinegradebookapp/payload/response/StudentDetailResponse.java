package org.example.onlinegradebookapp.payload.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentDetailResponse extends StudentResponse {
    private List<GradeResponse> grades;
}
