package com.seatly.backend.tag.repository;

import com.seatly.backend.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagDao extends JpaRepository<Tag, Long> {
}
