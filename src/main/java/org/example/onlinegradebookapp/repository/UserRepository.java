package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.onlinegradebookapp.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find the user with given email
    Optional<User> findByEmail(String email);

    // Check if the user exists with given email
    Boolean existsByEmail(String email);

    // Count number of users with given ROLE
    Long countByRole(UserRole role);
}
