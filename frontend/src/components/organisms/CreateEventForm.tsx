"use client";

import { useFormik } from "formik";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useSession } from "@/api/AuthApi";
import { useCreateEvent } from "@/api/EventApi";
import { Button, LinkButton } from "@/components/atoms/Button";
import { Input } from "@/components/atoms/Input";
import { SegmentedControl } from "@/components/atoms/SegmentedControl";
import { Stepper } from "@/components/atoms/Stepper";
import { TagChip } from "@/components/atoms/TagChip";
import { EventCard } from "@/components/molecules/EventCard";
import { FormStatus } from "@/components/molecules/FormStatus";
import { PageHeading } from "@/components/templates/PageHeading";
import { Copy } from "@/constants/copy";
import { EventFieldLimits } from "@/constants/fieldLimits";
import { Routes } from "@/constants/routes";
import { DAY_MS } from "@/constants/timing";
import {
  NEW_EVENT_DAYS_AHEAD,
  NEW_EVENT_DEFAULT_HOUR,
  NEW_EVENT_DEFAULT_SEAT_LIMIT,
} from "@/constants/ui";
import { useKnownTags } from "@/hooks/useKnownTags";
import { ToastTone, useToastStore } from "@/store/toastStore";
import { EventMode } from "@/types/entities/enums";
import type { Tag } from "@/types/entities/Tag";
import type { CreateEventRequest } from "@/types/requests/EventRequests";
import { isValidLocalDateTime, toLocalDateTimeInput } from "@/utils/dateTime";
import { showSubmitError } from "@/utils/formErrors";
import {
  createEventSchema,
  type CreateEventFormValues,
} from "@/validation/eventSchemas";

const MODE_OPTIONS = [
  { value: EventMode.PHYSICAL, label: Copy.eventForm.inPerson },
  { value: EventMode.ONLINE, label: Copy.eventForm.online },
] as const;

function initialValues(): CreateEventFormValues {
  const start = new Date(Date.now() + NEW_EVENT_DAYS_AHEAD * DAY_MS);
  start.setHours(NEW_EVENT_DEFAULT_HOUR, 0, 0, 0);
  return {
    name: "",
    description: "",
    mode: EventMode.PHYSICAL,
    location: "",
    meetingLink: "",
    eventDate: toLocalDateTimeInput(start),
    seatLimit: NEW_EVENT_DEFAULT_SEAT_LIMIT,
    tagIds: [],
  };
}

// Only the field the mode uses is sent; the other is null, as the backend
// would store it anyway. Blank optional text is null, not "".
function toRequest(values: CreateEventFormValues): CreateEventRequest {
  return {
    name: values.name.trim(),
    description: values.description.trim(),
    mode: values.mode,
    location:
      values.mode === EventMode.PHYSICAL
        ? (values.location?.trim() ?? null)
        : null,
    meetingLink:
      values.mode === EventMode.ONLINE
        ? (values.meetingLink?.trim() ?? null)
        : null,
    eventDate: values.eventDate,
    seatLimit: values.seatLimit,
    tagIds: values.tagIds,
  };
}

/** 07 Create Event - the form, and beside it the card exactly as attendees will see it. */
export function CreateEventForm() {
  const router = useRouter();
  const session = useSession();
  const createEvent = useCreateEvent();
  const knownTags = useKnownTags();
  const showToast = useToastStore((state) => state.show);
  // Computed once: the default date reads the clock, and must not move on re-render.
  const [startingValues] = useState(initialValues);

  const formik = useFormik<CreateEventFormValues>({
    initialValues: startingValues,
    validationSchema: createEventSchema,
    onSubmit: async (values, helpers) => {
      helpers.setStatus(undefined);
      try {
        const event = await createEvent.mutateAsync(toRequest(values));
        showToast(ToastTone.SUCCESS, Copy.toast.eventCreated);
        router.push(Routes.manageEvent(event.id));
      } catch (error) {
        showSubmitError(error, helpers);
      }
    },
  });

  const fieldError = (field: keyof CreateEventFormValues) => {
    const error = formik.errors[field];
    return formik.touched[field] && typeof error === "string"
      ? error
      : undefined;
  };
  const toggleTag = (tagId: Tag["id"]) =>
    void formik.setFieldValue(
      "tagIds",
      formik.values.tagIds.includes(tagId)
        ? formik.values.tagIds.filter((id) => id !== tagId)
        : [...formik.values.tagIds, tagId],
    );

  const selectedTags = knownTags.filter((tag) =>
    formik.values.tagIds.includes(tag.id),
  );

  return (
    <form
      noValidate
      onSubmit={formik.handleSubmit}
      className="flex flex-col gap-8">
      <PageHeading
        title={Copy.eventForm.title}
        subtitle={Copy.eventForm.subtitle}
      />

      <div className="grid gap-8 lg:grid-cols-5">
        <div className="flex flex-col gap-5 lg:col-span-3">
          <FormStatus message={formik.status} />
          <Input
            label={Copy.eventForm.name}
            placeholder={Copy.eventForm.namePlaceholder}
            maxLength={EventFieldLimits.NAME_MAX_LENGTH}
            error={fieldError("name")}
            {...formik.getFieldProps("name")}
          />
          <Input
            label={Copy.eventForm.description}
            multiline
            placeholder={Copy.eventForm.descriptionPlaceholder}
            error={fieldError("description")}
            {...formik.getFieldProps("description")}
          />
          <div className="grid gap-5 sm:grid-cols-2">
            <Input
              label={Copy.eventForm.date}
              type="datetime-local"
              error={fieldError("eventDate")}
              {...formik.getFieldProps("eventDate")}
            />
            <Stepper
              label={Copy.eventForm.seatLimit}
              value={formik.values.seatLimit}
              min={EventFieldLimits.SEAT_LIMIT_MIN}
              onChange={(value) =>
                void formik.setFieldValue("seatLimit", value)
              }
              decreaseLabel={Copy.eventForm.decrease}
              increaseLabel={Copy.eventForm.increase}
            />
          </div>

          <div className="flex flex-col gap-3">
            <span className="text-label-m text-black">
              {Copy.eventForm.mode}
            </span>
            <SegmentedControl
              label={Copy.eventForm.mode}
              options={MODE_OPTIONS}
              value={formik.values.mode}
              onChange={(mode) => void formik.setFieldValue("mode", mode)}
            />
            {/* The mode decides which venue field exists - never both. */}
            {formik.values.mode === EventMode.PHYSICAL ? (
              <Input
                label={Copy.eventForm.location}
                placeholder={Copy.eventForm.locationPlaceholder}
                maxLength={EventFieldLimits.LOCATION_MAX_LENGTH}
                error={fieldError("location")}
                {...formik.getFieldProps("location")}
              />
            ) : (
              <Input
                label={Copy.eventForm.meetingLink}
                type="url"
                placeholder={Copy.eventForm.meetingLinkPlaceholder}
                hint={Copy.eventForm.meetingLinkHint}
                maxLength={EventFieldLimits.MEETING_LINK_MAX_LENGTH}
                error={fieldError("meetingLink")}
                {...formik.getFieldProps("meetingLink")}
              />
            )}
          </div>

          <fieldset className="flex flex-col gap-2">
            <legend className="text-label-m text-black">
              {Copy.eventForm.tags}
            </legend>
            <p className="text-caption text-gray-500">
              {Copy.eventForm.tagsHint}
            </p>
            {knownTags.length === 0 ? (
              <p className="text-body-s text-gray-400">
                {Copy.eventForm.noTags}
              </p>
            ) : (
              <div className="flex flex-wrap gap-1.5">
                {knownTags.map((tag) => (
                  <TagChip
                    key={tag.id}
                    label={Copy.tag(tag.name)}
                    active={formik.values.tagIds.includes(tag.id)}
                    onToggle={() => toggleTag(tag.id)}
                  />
                ))}
              </div>
            )}
          </fieldset>
        </div>

        <aside className="flex flex-col gap-3 lg:col-span-2">
          <span className="text-overline text-gray-500 uppercase">
            {Copy.eventForm.preview}
          </span>
          <div className="lg:sticky lg:top-24">
            {isValidLocalDateTime(formik.values.eventDate) && (
              <EventCard
                name={
                  formik.values.name.trim() === ""
                    ? Copy.eventForm.previewName
                    : formik.values.name
                }
                eventDate={formik.values.eventDate}
                mode={formik.values.mode}
                location={formik.values.location ?? null}
                seatLimit={formik.values.seatLimit}
                availableSeats={formik.values.seatLimit}
                tags={selectedTags}
                hostName={
                  session.data?.user?.name ?? Copy.eventForm.previewHost
                }
              />
            )}
          </div>
        </aside>
      </div>

      <div className="sticky bottom-0 -mx-4 flex justify-end gap-2 border-t border-gray-200 bg-white px-4 py-4 sm:mx-0 sm:px-0">
        <LinkButton href={Routes.dashboard} variant="ghost">
          {Copy.common.cancel}
        </LinkButton>
        <Button type="submit" variant="accent" disabled={formik.isSubmitting}>
          {Copy.eventForm.publish}
        </Button>
      </div>
    </form>
  );
}
