-- A second demo account, so two different people can compete for the same
-- seat once RSVP exists. A new migration rather than an edit to V2: V2 has
-- already run on every existing database, and changing it would break
-- Flyway's checksum validation there — exactly what happened to V1 before.
--
-- DEV DATA: password is "seatly-dev-password", same as the V2 organiser.
-- Like V2, this must move to a dev-only Flyway location before a real deploy.
--
-- No explicit id: the identity column assigns one. V2 needed id 1 to match a
-- hardcoded placeholder; nothing depends on this user's id, so there's no
-- reason to risk colliding with an account someone already registered locally.
INSERT INTO app_user (name, email, password, bio, is_active, is_deleted, created_at, updated_at)
VALUES ('Ayesha F.', 'attendee@seatly.dev',
        '$2a$10$q8SL5Ss/Oj27prr4iSqeQeNE5FqZCIDaOmkclyMhDMcj583u.tUT2',
        'Goes to design and tech meetups in Colombo.',
        TRUE, FALSE, '2026-09-29 09:00:00', '2026-09-29 09:00:00');
