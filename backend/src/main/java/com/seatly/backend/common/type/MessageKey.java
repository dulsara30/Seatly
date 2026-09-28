package com.seatly.backend.common.type;

/**
 * Implemented by every per-module message-key enum (EventMessageKey,
 * RsvpMessageKey, ...) so ModuleException can carry a key from any module
 * without depending on any one of them.
 */
public interface MessageKey {

    String getKey();
}
