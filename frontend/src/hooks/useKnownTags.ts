import { useMemo } from "react";
import { useUpcomingEvents } from "@/api/EventApi";
import type { Tag } from "@/types/entities/Tag";

const NO_FILTERS = {};

// TODO(backend): GET /v1/tags isn't built yet; tags derived from loaded events
// Reads the UNFILTERED query so the chip list doesn't shrink to the active tag.
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
