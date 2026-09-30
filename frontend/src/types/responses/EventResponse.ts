import type { EventMode, EventStatus } from "@/types/entities/enums";
import type { Id, LocalDateTimeString } from "@/types/entities/primitives";
import type { Tag } from "@/types/entities/Tag";
import type { UserSummary } from "@/types/entities/User";

// event/payload/EventResponseDto.java; availableSeats is derived by the backend, never here.
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

// event/payload/EventDetailResponseDto.java; meetingLink is null for PHYSICAL or non-attendees.
export interface EventDetailResponse extends EventResponse {
  description: string;
  meetingLink: string | null;
}
