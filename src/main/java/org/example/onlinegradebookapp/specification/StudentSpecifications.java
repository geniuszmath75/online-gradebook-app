package org.example.onlinegradebookapp.specification;

import jakarta.persistence.criteria.Predicate;
import org.example.onlinegradebookapp.entity.Student;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class StudentSpecifications {
    private StudentSpecifications() {
    }

    public static Specification<Student> withFilters(Long classId, String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (classId != null) {
                predicates.add(cb.equal(root.get("schoolClass").get("id"), classId));
            }

            if (StringUtils.hasText(email)) {
                String pattern = "%" + escapeLike(email.trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("email")), pattern, '\\'));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
