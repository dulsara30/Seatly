package com.seatly.backend.rsvp.model;

import com.seatly.backend.common.model.Auditable;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Minimal mapping of rsvp so the event module can count confirmed seats. The
 * rsvp module (locking, waitlist, promotion) builds on this later.
 */
@Entity
@Table(name = "rsvp")
@Getter
@Setter
@NoArgsConstructor
public class Rsvp extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RsvpStatus status;

    @Column(name = "position")
    private Integer position;
}
