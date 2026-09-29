package com.seatly.backend.user.repository;

import com.seatly.backend.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {

    // Includes soft-deleted accounts on purpose: a deleted user's email stays
    // taken, so nobody can register it and inherit that person's history.
    boolean existsByEmail(String email);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);
}
