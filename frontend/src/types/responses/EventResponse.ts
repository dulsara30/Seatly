import type { EventMode, EventStatus } from "@/types/entities/enums";
import type { Id, LocalDateTimeString } from "@/types/entities/primitives";
import type { Tag } from "@/types/entities/Tag";
import type { UserSummary } from "@/types/entities/User";

/**
 * event/payload/EventResponseDto.java — the list card. availableSeats is
 * derived by the backend (seatLimit − confirmed); never compute it here.
 */
export interface EventResponse {
  id: Id;
  name: string;
  eventDate: LocalDateTimeString;
  mode: EventMode;
  location: string | null;
  seatLimit: number;
  availableSeats: number;
  status: EventStatus;
  tags: Tag[];
  organizer: UserSummary;
}

/**
 * event/payload/EventDetailResponseDto.java — adds description and
 * meetingLink. meetingLink is null for PHYSICAL events AND for callers who
 * aren't the organiser or a confirmed attendee; `mode` tells the two apart.
 */
export interface EventDetailResponse extends EventResponse {
  description: string;
  meetingLink: string | null;
}
