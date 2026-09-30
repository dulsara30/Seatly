package com.seatly.backend.event.repository;

import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.model.Event_;
import com.seatly.backend.event.payload.EventFilterDto;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.tag.model.Tag;
import com.seatly.backend.tag.model.Tag_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private static final String LIKE_WILDCARD = "%";
    private static final String LIKE_SINGLE_CHAR = "_";
    private static final char LIKE_ESCAPE = '\\';
    private static final String LIKE_ESCAPE_TEXT = String.valueOf(LIKE_ESCAPE);

    private final EntityManager entityManager;

    @Override
    public Page<Event> findUpcoming(EventFilterDto filter, Pageable pageable) {
        return new PageImpl<>(findPageContent(filter, pageable), pageable, countMatching(filter));
    }

    private List<Event> findPageContent(EventFilterDto filter, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> event = query.from(Event.class);

        // A to-one fetch join is safe with paging: it adds columns, never rows.
        event.fetch(Event_.organizer);

        query.select(event)
                .where(buildPredicates(builder, query, event, filter))
                .orderBy(QueryUtils.toOrders(pageable.getSort(), event, builder));

        return entityManager.createQuery(query)
                .setFirstResult(Math.toIntExact(pageable.getOffset()))
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // Fresh root: predicates can't be reused across two CriteriaQuery instances.
    private long countMatching(EventFilterDto filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Event> event = query.from(Event.class);

        query.select(builder.count(event)).where(buildPredicates(builder, query, event, filter));

        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate[] buildPredicates(
            CriteriaBuilder builder, CriteriaQuery<?> query, Root<Event> event, EventFilterDto filter) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.isFalse(event.get(Event_.isDeleted)));
        predicates.add(builder.equal(event.get(Event_.status), EventStatus.UPCOMING));

        if (filter.mode() != null) {
            predicates.add(builder.equal(event.get(Event_.mode), filter.mode()));
        }
        if (filter.tag() != null) {
            predicates.add(hasTag(builder, query, event, filter.tag()));
        }
        if (filter.search() != null) {
            predicates.add(nameOrDescriptionContains(builder, event, filter.search()));
        }
        return predicates.toArray(Predicate[]::new);
    }

    // Subquery, not a join: joining tags would multiply rows and skew the page and the count.
    private Predicate hasTag(CriteriaBuilder builder, CriteriaQuery<?> query, Root<Event> event, String tagName) {
        Subquery<Long> taggedEventIds = query.subquery(Long.class);
        Root<Event> taggedEvent = taggedEventIds.from(Event.class);
        Join<Event, Tag> tag = taggedEvent.join(Event_.tags);
        taggedEventIds.select(taggedEvent.get(Event_.id)).where(builder.equal(tag.get(Tag_.name), tagName));
        return event.get(Event_.id).in(taggedEventIds);
    }

    private Predicate nameOrDescriptionContains(CriteriaBuilder builder, Root<Event> event, String search) {
        String pattern = LIKE_WILDCARD + escapeLikeWildcards(search.toLowerCase(Locale.ROOT)) + LIKE_WILDCARD;
        return builder.or(
                builder.like(builder.lower(event.get(Event_.name)), pattern, LIKE_ESCAPE),
                builder.like(builder.lower(event.get(Event_.description)), pattern, LIKE_ESCAPE));
    }

    // Escape LIKE wildcards, or "100%" and "a_b" are read as patterns, not text.
    private String escapeLikeWildcards(String value) {
        return value.replace(LIKE_ESCAPE_TEXT, LIKE_ESCAPE_TEXT + LIKE_ESCAPE_TEXT)
                .replace(LIKE_WILDCARD, LIKE_ESCAPE_TEXT + LIKE_WILDCARD)
                .replace(LIKE_SINGLE_CHAR, LIKE_ESCAPE_TEXT + LIKE_SINGLE_CHAR);
    }
}
