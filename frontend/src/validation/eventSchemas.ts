import * as yup from "yup";
import { EventFieldLimits } from "@/constants/fieldLimits";
import { MESSAGES } from "@/constants/messages";
import { EventMode } from "@/types/entities/enums";
import { parseLocalDateTime } from "@/utils/dateTime";

/**
 * Mirrors event/payload/CreateEventRequestDto.java - same limits, same keys.
 *
 * Two rules here are NOT Bean Validation on the backend but business rules
 * in EventServiceImpl: ONLINE needs a meeting link, PHYSICAL needs a venue,
 * and the date must be in the future. They're repeated for instant feedback
 * only; Spring still enforces all three, so this is UX, never the guarantee.
 */
export const createEventSchema = yup.object({
  name: yup
    .string()
    .trim()
    .required(MESSAGES.EVENT_ERROR_NAME_REQUIRED)
    .max(EventFieldLimits.NAME_MAX_LENGTH, MESSAGES.EVENT_ERROR_NAME_TOO_LONG),
  description: yup
    .string()
    .trim()
    .required(MESSAGES.EVENT_ERROR_DESCRIPTION_REQUIRED),
  mode: yup
    .mixed<EventMode>()
    .oneOf(Object.values(EventMode), MESSAGES.EVENT_ERROR_MODE_REQUIRED)
    .required(MESSAGES.EVENT_ERROR_MODE_REQUIRED),
  location: yup
    .string()
    .trim()
    .max(
      EventFieldLimits.LOCATION_MAX_LENGTH,
      MESSAGES.EVENT_ERROR_LOCATION_TOO_LONG,
    )
    .when("mode", {
      is: EventMode.PHYSICAL,
      then: (schema) => schema.required(MESSAGES.EVENT_ERROR_LOCATION_REQUIRED),
    }),
  meetingLink: yup
    .string()
    .trim()
    .max(
      EventFieldLimits.MEETING_LINK_MAX_LENGTH,
      MESSAGES.EVENT_ERROR_MEETING_LINK_TOO_LONG,
    )
    .when("mode", {
      is: EventMode.ONLINE,
      then: (schema) =>
        schema.required(MESSAGES.EVENT_ERROR_MEETING_LINK_REQUIRED),
    }),
  eventDate: yup
    .string()
    .required(MESSAGES.EVENT_ERROR_DATE_REQUIRED)
    .test(
      "in-the-future",
      MESSAGES.EVENT_ERROR_DATE_MUST_BE_FUTURE,
      (value) => parseLocalDateTime(value).getTime() > Date.now(),
    ),
  seatLimit: yup
    .number()
    .typeError(MESSAGES.EVENT_ERROR_SEAT_LIMIT_REQUIRED)
    .integer(MESSAGES.EVENT_ERROR_SEAT_LIMIT_MIN)
    .required(MESSAGES.EVENT_ERROR_SEAT_LIMIT_REQUIRED)
    .min(EventFieldLimits.SEAT_LIMIT_MIN, MESSAGES.EVENT_ERROR_SEAT_LIMIT_MIN),
  tagIds: yup
    .array(yup.number().required())
    .required(MESSAGES.EVENT_ERROR_TAG_IDS_REQUIRED),
});

export type CreateEventFormValues = yup.InferType<typeof createEventSchema>;
