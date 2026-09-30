package com.seatly.backend.event.model;

import com.seatly.backend.common.constant.PaginationConstants;
import com.seatly.backend.common.model.Auditable;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.tag.model.Tag;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
public class Event extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @Column(name = "name", nullable = false, length = EventFieldLimits.NAME_MAX_LENGTH)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    // STRING, not ORDINAL: reordering the enum would silently remap every stored row.
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private EventMode mode;

    @Column(name = "location", length = EventFieldLimits.LOCATION_MAX_LENGTH)
    private String location;

    @Column(name = "meeting_link", length = EventFieldLimits.MEETING_LINK_MAX_LENGTH)
    private String meetingLink;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "seat_limit", nullable = false)
    private Integer seatLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EventStatus status;

    @Column(name = "link_sent_at")
    private LocalDateTime linkSentAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    // @BatchSize, not a fetch join: fetch-joining a collection makes Hibernate page in memory.
    @ManyToMany
    @BatchSize(size = PaginationConstants.MAX_PAGE_SIZE)
    @JoinTable(
            name = "event_tag",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();
}
