import type { EventMode } from "@/types/entities/enums";
import type { Id, LocalDateTimeString } from "@/types/entities/primitives";

// event/payload/CreateEventRequestDto.java; tagIds is required - send [] for no tags.
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

// event/payload/UpdateEventRequestDto.java; omitted fields are unchanged, tagIds [] clears them.
export type UpdateEventRequest = Partial<CreateEventRequest>;

export interface EventListParams {
  page: number;
  size: number;
  tag?: string;
  mode?: EventMode;
  search?: string;
}

export type EventFilters = Omit<EventListParams, "page" | "size">;
