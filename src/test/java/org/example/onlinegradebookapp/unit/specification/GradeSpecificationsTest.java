package org.example.onlinegradebookapp.unit.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.specification.GradeSpecifications;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GradeSpecificationsTest {

    @Test
    @SuppressWarnings("unchecked")
    void withFilters_shouldApplyAllPredicates() {
        Root<Grade> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> studentPath = mock(Path.class);
        Path<Object> studentIdPath = mock(Path.class);
        Path<Object> testPath = mock(Path.class);
        Path<Object> testIdPath = mock(Path.class);
        Path<Instant> createdAtPath = mock(Path.class);
        Predicate studentPredicate = mock(Predicate.class);
        Predicate testPredicate = mock(Predicate.class);
        Predicate fromPredicate = mock(Predicate.class);
        Predicate toPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        when(root.get("student")).thenReturn(studentPath);
        when(studentPath.get("id")).thenReturn(studentIdPath);
        when(root.get("test")).thenReturn(testPath);
        when(testPath.get("id")).thenReturn(testIdPath);
        when(root.<Instant>get("createdAt")).thenReturn(createdAtPath);
        when(cb.equal(studentIdPath, 1L)).thenReturn(studentPredicate);
        when(cb.equal(testIdPath, 2L)).thenReturn(testPredicate);
        when(cb.greaterThanOrEqualTo(createdAtPath, from)).thenReturn(fromPredicate);
        when(cb.lessThanOrEqualTo(createdAtPath, to)).thenReturn(toPredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(andPredicate);

        Specification<Grade> spec = GradeSpecifications.withFilters(1L, 2L, from, to);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(studentIdPath, 1L);
        verify(cb).equal(testIdPath, 2L);
        verify(cb).greaterThanOrEqualTo(createdAtPath, from);
        verify(cb).lessThanOrEqualTo(createdAtPath, to);
    }
}
