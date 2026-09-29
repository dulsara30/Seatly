package com.seatly.backend.user.model;

import com.seatly.backend.common.model.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = UserFieldLimits.NAME_MAX_LENGTH)
    private String name;

    // Always stored lowercase, so the unique constraint is case-insensitive in practice.
    @Column(name = "email", nullable = false, unique = true, length = UserFieldLimits.EMAIL_MAX_LENGTH)
    private String email;

    // A BCrypt hash, never the password itself.
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "bio", length = UserFieldLimits.BIO_MAX_LENGTH)
    private String bio;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
