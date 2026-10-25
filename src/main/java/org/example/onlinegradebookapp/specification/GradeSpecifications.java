package org.example.onlinegradebookapp.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.onlinegradebookapp.entity.Grade;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class GradeSpecifications {
    private GradeSpecifications() {
    }

    public static Specification<Grade> withFilters(Long studentId, Long testId,
                                                   Instant createdFrom, Instant createdTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null) {
                predicates.add(cb.equal(root.get("student").get("id"), studentId));
            }

            if (testId != null) {
                predicates.add(cb.equal(root.get("test").get("id"), testId));
            }

            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }

            if (createdTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
