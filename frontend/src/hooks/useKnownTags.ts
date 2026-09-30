import { useMemo } from "react";
import { useUpcomingEvents } from "@/api/EventApi";
import type { Tag } from "@/types/entities/Tag";

const NO_FILTERS = {};

/**
 * Every tag in use on the upcoming events loaded so far.
 *
 * TODO(backend): GET /v1/tags doesn't exist yet - it's in the API design but
 * was never built. Until it is, the tag list is DERIVED from the unfiltered
 * events query, which means a tag only used by events past the loaded pages
 * won't appear. Swap this for a real useTags() hook once the endpoint exists.
 *
 * It reads the UNFILTERED query on purpose: deriving from the filtered one
 * would make the chip list shrink to the active tag the moment you click it.
 */
export function useKnownTags(): Tag[] {
  const { data } = useUpcomingEvents(NO_FILTERS);

  return useMemo(() => {
    const byId = new Map<Tag["id"], Tag>();
    data?.pages.forEach((page) =>
      page.items.forEach((event) =>
        event.tags.forEach((tag) => byId.set(tag.id, tag)),
      ),
    );
    return [...byId.values()].sort((first, second) =>
      first.name.localeCompare(second.name),
    );
  }, [data]);
}
