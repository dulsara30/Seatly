-- Demo data matching the Figma design: one organiser, a handful of tags, and
-- six upcoming events.
--
-- Every value is fixed — no now(), no CURRENT_TIMESTAMP defaults — so this
-- migration produces the same rows on every machine, every run.
--
-- DEV DATA: the organiser's password is "seatly-dev-password" (BCrypt below).
-- This must not reach a real deployment; move it to a dev-only Flyway
-- location before the first cloud deploy.

-- The organiser is inserted with an explicit id so it matches
-- EventServiceImpl.CURRENT_USER_ID. OVERRIDING SYSTEM VALUE is required for an
-- explicit id in a GENERATED ALWAYS identity column, and the sequence is then
-- moved past it — otherwise the next real registration would also be given id 1
-- and fail on the primary key.
INSERT INTO app_user (id, name, email, password, bio, is_active, is_deleted, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (1, 'Dulsara M.', 'organiser@seatly.dev',
        '$2a$10$JP1BhHmJjWVs1HdIGi7hyOouXGhahwpPIgk5t3YqWK8KqQZw943D.',
        'Organises product, design and tech meetups around Sri Lanka.',
        TRUE, FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00');

SELECT setval(pg_get_serial_sequence('app_user', 'id'), (SELECT MAX(id) FROM app_user));

-- Stored lowercase, matching the tag module's rule.
INSERT INTO tag (name)
VALUES ('product'), ('design'), ('startups'), ('tech'), ('creative'), ('networking'), ('colombo');

INSERT INTO event (organizer_id, name, description, mode, location, meeting_link,
                   event_date, seat_limit, status, is_deleted, created_at, updated_at)
VALUES
    (1, 'Colombo Product People',
     'An evening of lightning talks and open discussion for product managers, designers and engineers building products in Sri Lanka.',
     'PHYSICAL', 'Trace Expert City, Colombo', NULL,
     '2026-10-15 18:30:00', 30, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00'),

    (1, 'Designing for Calm',
     'A hands-on online workshop on designing interfaces that reduce stress: pacing, whitespace, motion and honest defaults.',
     'ONLINE', NULL, 'https://meet.google.com/sea-tlyd-emo',
     '2026-10-17 10:00:00', 50, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00'),

    (1, 'The Founder''s Table',
     'A small, candid dinner conversation between early-stage founders. Twelve seats, no slides, no recordings.',
     'PHYSICAL', 'The Commons, Colombo', NULL,
     '2026-10-21 19:00:00', 12, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00'),

    (1, 'Build in Public',
     'Makers share what they shipped this month, what broke, and what they learned — live, with an open Q&A.',
     'ONLINE', NULL, 'https://zoom.us/j/0000000000',
     '2026-10-27 17:30:00', 100, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00'),

    (1, 'Creative Coding Club',
     'Generative art, sketches and small experiments in code. Bring a laptop; no experience needed.',
     'PHYSICAL', 'MakerSpace Kandy', NULL,
     '2026-11-01 15:00:00', 25, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00'),

    (1, 'Remote Coffee Chat',
     'An informal early-morning call to meet other people working remotely. Bring your own coffee.',
     'ONLINE', NULL, 'https://meet.google.com/cof-feec-hat',
     '2026-11-06 08:00:00', 12, 'UPCOMING', FALSE, '2026-09-28 09:00:00', '2026-09-28 09:00:00');

-- Joined by name rather than hardcoded ids, so this doesn't depend on the
-- order the identity column handed out ids above.
INSERT INTO event_tag (event_id, tag_id)
SELECT e.id, t.id
FROM (VALUES
          ('Colombo Product People', 'product'),
          ('Colombo Product People', 'networking'),
          ('Colombo Product People', 'colombo'),
          ('Designing for Calm', 'design'),
          ('The Founder''s Table', 'startups'),
          ('The Founder''s Table', 'networking'),
          ('The Founder''s Table', 'colombo'),
          ('Build in Public', 'startups'),
          ('Build in Public', 'tech'),
          ('Creative Coding Club', 'creative'),
          ('Creative Coding Club', 'tech'),
          ('Remote Coffee Chat', 'networking')
     ) AS pairing (event_name, tag_name)
JOIN event e ON e.name = pairing.event_name
JOIN tag t ON t.name = pairing.tag_name;
