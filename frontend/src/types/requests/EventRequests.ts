import type { EventMode } from "@/types/entities/enums";
import type { Id, LocalDateTimeString } from "@/types/entities/primitives";

/**
 * event/payload/CreateEventRequestDto.java. tagIds is required - send [] for
 * no tags. The backend rejects a missing list rather than guessing.
 */
export interface CreateEventRequest {
  name: string;
  description: string;
  mode: EventMode;
  location: string | null;
  meetingLink: string | null;
  eventDate: LocalDateTimeString;
  seatLimit: number;
  tagIds: Id[];
}

/**
 * event/payload/UpdateEventRequestDto.java - a partial update. An omitted
 * field is left unchanged. tagIds: omitted keeps the tags, [] removes them all.
 */
export type UpdateEventRequest = Partial<CreateEventRequest>;

/** Query params of GET /v1/events - every filter is optional. */
export interface EventListParams {
  page: number;
  size: number;
  tag?: string;
  mode?: EventMode;
  search?: string;
}

/** The user-chosen part of EventListParams; paging is the query's job. */
export type EventFilters = Omit<EventListParams, "page" | "size">;
