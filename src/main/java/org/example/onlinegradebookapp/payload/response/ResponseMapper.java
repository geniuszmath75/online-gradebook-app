package org.example.onlinegradebookapp.payload.response;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;

public final class ResponseMapper {
    private ResponseMapper() {
    }

    public static UserResponse toUser(User source) {
        UserResponse response = new UserResponse();
        response.setId(source.getId());
        response.setEmail(source.getEmail());
        response.setFirstName(source.getFirstName());
        response.setLastName(source.getLastName());
        response.setRole(source.getRole() != null ? source.getRole().name() : null);
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());
        response.setClassId(source.getSchoolClass() != null ? source.getSchoolClass().getId() : null);
        return response;
    }

    public static StudentResponse toStudent(Student source) {
        StudentResponse response = new StudentResponse();
        copyStudentFields(source, response);
        return response;
    }

    public static StudentDetailResponse toStudentDetail(Student source) {
        StudentDetailResponse response = new StudentDetailResponse();
        copyStudentFields(source, response);
        response.setGrades(source.getGrades().stream()
                .map(ResponseMapper::toGrade)
                .toList());
        return response;
    }

    public static GradeResponse toGrade(Grade source) {
        GradeResponse response = new GradeResponse();
        response.setId(source.getId());
        response.setGrade(source.getGrade());
        response.setDescription(source.getDescription());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());
        response.setStudentId(source.getStudent() != null ? source.getStudent().getId() : null);
        response.setTestId(source.getTest() != null ? source.getTest().getId() : null);
        return response;
    }

    public static KnowledgeTestResponse toKnowledgeTest(KnowledgeTest source) {
        KnowledgeTestResponse response = new KnowledgeTestResponse();
        response.setId(source.getId());
        response.setName(source.getName());
        response.setCategory(source.getCategory());
        response.setTestDate(source.getTestDate());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());
        response.setClassId(source.getSchoolClass() != null ? source.getSchoolClass().getId() : null);
        response.setSubjectId(source.getSubject() != null ? source.getSubject().getId() : null);
        return response;
    }

    public static SchoolClassResponse toSchoolClass(SchoolClass source) {
        SchoolClassResponse response = new SchoolClassResponse();
        response.setId(source.getId());
        response.setName(source.getName());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());
        return response;
    }

    public static SubjectResponse toSubject(Subject source) {
        SubjectResponse response = new SubjectResponse();
        response.setId(source.getId());
        response.setName(source.getName());
        response.setCreatedAt(source.getCreatedAt());
        response.setUpdatedAt(source.getUpdatedAt());
        return response;
    }

    private static void copyStudentFields(Student source, StudentResponse target) {
        target.setId(source.getId());
        target.setEmail(source.getEmail());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        target.setClassId(source.getSchoolClass() != null ? source.getSchoolClass().getId() : null);
    }
}
