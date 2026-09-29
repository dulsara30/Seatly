package com.seatly.backend.user.repository;

import com.seatly.backend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {
}
