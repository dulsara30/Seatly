-- Seatly initial schema: user, event, rsvp, tag, event_tag, email_outbox.
-- "user" is a reserved word in PostgreSQL (backs CURRENT_USER), so every
-- reference to the table is double-quoted.

CREATE TABLE "user" (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    email      VARCHAR(255)  NOT NULL,
    password   VARCHAR(255)  NOT NULL,
    bio        VARCHAR(500),
    is_active  BOOLEAN       NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_email UNIQUE (email)
);

CREATE TABLE event (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organizer_id BIGINT        NOT NULL,
    name         VARCHAR(255)  NOT NULL,
    description  TEXT          NOT NULL,
    mode         VARCHAR(20)   NOT NULL,
    location     VARCHAR(255),
    meeting_link VARCHAR(500),
    event_date   TIMESTAMP     NOT NULL,
    seat_limit   INT           NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    link_sent_at TIMESTAMP,
    is_deleted   BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_organizer FOREIGN KEY (organizer_id) REFERENCES "user" (id),
    CONSTRAINT chk_event_mode CHECK (mode IN ('ONLINE', 'PHYSICAL')),
    CONSTRAINT chk_event_status CHECK (status IN ('UPCOMING', 'CANCELLED', 'COMPLETED')),
    CONSTRAINT chk_event_seat_limit CHECK (seat_limit >= 1)
);

CREATE INDEX idx_event_organizer_id ON event (organizer_id);

-- No is_deleted: a cancelled RSVP is represented by status = 'CANCELLED',
-- not a soft-delete flag, since the row itself (and its old waitlist
-- position) stays meaningful history.
CREATE TABLE rsvp (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT        NOT NULL,
    event_id   BIGINT        NOT NULL,
    status     VARCHAR(20)   NOT NULL,
    position   INT,
    created_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rsvp_user FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id) REFERENCES event (id),
    CONSTRAINT uq_rsvp_user_event UNIQUE (user_id, event_id),
    CONSTRAINT chk_rsvp_status CHECK (status IN ('CONFIRMED', 'WAITLISTED', 'CANCELLED'))
);

CREATE INDEX idx_rsvp_event_status ON rsvp (event_id, status);
CREATE INDEX idx_rsvp_user_id ON rsvp (user_id);

-- No audit columns: tags are plain reference data with no independent
-- lifecycle worth tracking, unlike the domain entities.
CREATE TABLE tag (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_tag_name UNIQUE (name)
);

CREATE TABLE event_tag (
    event_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    CONSTRAINT pk_event_tag PRIMARY KEY (event_id, tag_id),
    CONSTRAINT fk_event_tag_event FOREIGN KEY (event_id) REFERENCES event (id),
    CONSTRAINT fk_event_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

CREATE INDEX idx_event_tag_tag_id ON event_tag (tag_id);

-- Deliberately no foreign keys: a row is a snapshot taken at queue time,
-- not a live reference, so it survives the referenced event or user
-- being changed or deleted later.
CREATE TABLE email_outbox (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    recipient      VARCHAR(255) NOT NULL,
    subject        VARCHAR(255) NOT NULL,
    body           TEXT         NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempts       INT          NOT NULL DEFAULT 0,
    reference_type VARCHAR(50),
    reference_id   BIGINT,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at        TIMESTAMP,
    CONSTRAINT chk_email_outbox_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);
