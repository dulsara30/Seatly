"use client";

import { useState } from "react";
import { useUpcomingEvents } from "@/api/EventApi";
import { Button } from "@/components/atoms/Button";
import { Skeleton } from "@/components/atoms/Skeleton";
import { EmptyState } from "@/components/molecules/EmptyState";
import { ErrorState } from "@/components/molecules/ErrorState";
import { EventCard } from "@/components/molecules/EventCard";
import { FilterBar } from "@/components/molecules/FilterBar";
import { SearchBar } from "@/components/molecules/SearchBar";
import { Copy } from "@/constants/copy";
import { Routes } from "@/constants/routes";
import { SEARCH_DEBOUNCE_MS } from "@/constants/timing";
import { EVENT_GRID_SKELETON_COUNT } from "@/constants/ui";
import { useDebouncedValue } from "@/hooks/useDebouncedValue";
import { useKnownTags } from "@/hooks/useKnownTags";
import { useMyRsvpLookup } from "@/hooks/useMyRsvpLookup";
import type { EventMode } from "@/types/entities/enums";
import type { EventFilters } from "@/types/requests/EventRequests";

/** 01 Browse - hero, search, grouped filters, the card grid. */
export function EventBrowser() {
  const [search, setSearch] = useState("");
  const [mode, setMode] = useState<EventMode>();
  const [tag, setTag] = useState<string>();
  const debouncedSearch = useDebouncedValue(search.trim(), SEARCH_DEBOUNCE_MS);

  // An empty search is "no search": the key is omitted, not sent as "".
  const filters: EventFilters = {
    search: debouncedSearch === "" ? undefined : debouncedSearch,
    mode,
    tag,
  };
  const hasFilters = Object.values(filters).some(
    (value) => value !== undefined,
  );

  const events = useUpcomingEvents(filters);
  const tags = useKnownTags();
  const { byEventId } = useMyRsvpLookup();

  const clearFilters = () => {
    setSearch("");
    setMode(undefined);
    setTag(undefined);
  };

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-3">
        <p className="text-overline text-gray-500 uppercase">
          {Copy.browse.overline}
        </p>
        <h1 className="text-heading-xl sm:text-display">{Copy.browse.title}</h1>
        <p className="max-w-xl text-body-l text-gray-500">
          {Copy.browse.subtitle}
        </p>
        <div className="mt-3 max-w-xl">
          <SearchBar
            value={search}
            onChange={setSearch}
            label={Copy.browse.searchLabel}
            placeholder={Copy.browse.searchPlaceholder}
          />
        </div>
      </section>

      <FilterBar
        mode={mode}
        onModeChange={setMode}
        tags={tags}
        activeTag={tag}
        onTagChange={setTag}
        resultCount={events.data?.pages[0]?.totalItems}
        onClear={clearFilters}
      />

      <section className="flex flex-col gap-4">
        <h2 className="text-heading-m text-black">{Copy.browse.gridTitle}</h2>
        {events.isPending && <CardGridSkeleton />}
        {events.isError && (
          <ErrorState
            title={Copy.browse.errorTitle}
            message={events.error.message}
            onRetry={() => void events.refetch()}
          />
        )}
        {events.isSuccess && events.data.pages[0]?.totalItems === 0 && (
          <EmptyState
            title={
              hasFilters ? Copy.browse.emptyTitle : Copy.browse.noEventsTitle
            }
            body={hasFilters ? Copy.browse.emptyBody : Copy.browse.noEventsBody}
            action={
              hasFilters && (
                <Button variant="secondary" size="small" onClick={clearFilters}>
                  {Copy.browse.clearAll}
                </Button>
              )
            }
          />
        )}
        {events.isSuccess && (
          <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {events.data.pages.flatMap((page) =>
              page.items.map((event) => (
                <li key={event.id}>
                  <EventCard
                    {...event}
                    hostName={event.organizer.name}
                    myRsvpStatus={byEventId.get(event.id)?.status}
                    href={Routes.eventDetail(event.id)}
                  />
                </li>
              )),
            )}
          </ul>
        )}
        {events.hasNextPage && (
          <div className="flex justify-center">
            <Button
              variant="secondary"
              disabled={events.isFetchingNextPage}
              onClick={() => void events.fetchNextPage()}>
              {events.isFetchingNextPage
                ? Copy.common.loading
                : Copy.common.loadMore}
            </Button>
          </div>
        )}
      </section>
    </div>
  );
}

function CardGridSkeleton() {
  return (
    <ul
      aria-label={Copy.common.loading}
      className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: EVENT_GRID_SKELETON_COUNT }, (_, index) => (
        <li key={index}>
          <Skeleton className="h-72 w-full" />
        </li>
      ))}
    </ul>
  );
}
