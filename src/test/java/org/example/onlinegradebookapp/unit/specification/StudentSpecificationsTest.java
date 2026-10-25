package org.example.onlinegradebookapp.unit.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.specification.StudentSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentSpecificationsTest {

    @Test
    @SuppressWarnings("unchecked")
    void withFilters_shouldApplyClassIdAndEmailPredicates() {
        Root<Student> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> schoolClassPath = mock(Path.class);
        Path<Object> classIdPath = mock(Path.class);
        Path<Object> emailPath = mock(Path.class);
        Expression<String> lowerEmail = mock(Expression.class);
        Predicate classPredicate = mock(Predicate.class);
        Predicate emailPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        when(root.get("schoolClass")).thenReturn(schoolClassPath);
        when(schoolClassPath.get("id")).thenReturn(classIdPath);
        when(root.get("email")).thenReturn(emailPath);
        when(cb.lower(any(Expression.class))).thenReturn(lowerEmail);
        when(cb.equal(classIdPath, 5L)).thenReturn(classPredicate);
        when(cb.like(eq(lowerEmail), anyString(), eq('\\'))).thenReturn(emailPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Specification<Student> spec = StudentSpecifications.withFilters(5L, "Gmail");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(classIdPath, 5L);
        verify(cb).like(eq(lowerEmail), eq("%gmail%"), eq('\\'));
    }
}
