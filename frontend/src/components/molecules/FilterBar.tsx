import type { ReactNode } from "react";
import { TagChip } from "@/components/atoms/TagChip";
import { Copy } from "@/constants/copy";
import { EventMode } from "@/types/entities/enums";
import type { Tag } from "@/types/entities/Tag";

interface FilterBarProps {
  mode: EventMode | undefined;
  onModeChange: (mode: EventMode | undefined) => void;
  tags: Tag[];
  activeTag: string | undefined;
  onTagChange: (tagName: string | undefined) => void;
  resultCount: number | undefined;
  onClear: () => void;
}

const MODE_OPTIONS = [
  { value: EventMode.ONLINE, label: Copy.badge.ONLINE },
  { value: EventMode.PHYSICAL, label: Copy.badge.PHYSICAL },
] as const;

// Single-select and no Status group: GET /v1/events takes one mode/tag, UPCOMING only.
export function FilterBar({ mode, onModeChange, tags, activeTag, onTagChange, resultCount, onClear }: FilterBarProps) {
  const hasActiveFilter = mode !== undefined || activeTag !== undefined;

  return (
    <div className="flex flex-col gap-3">
      <FilterGroup label={Copy.browse.modeGroup}>
        {MODE_OPTIONS.map((option) => (
          <TagChip
            key={option.value}
            label={option.label}
            active={mode === option.value}
            onToggle={() => onModeChange(mode === option.value ? undefined : option.value)}
          />
        ))}
      </FilterGroup>
      {tags.length > 0 && (
        <FilterGroup label={Copy.browse.tagsGroup}>
          {tags.map((tag) => (
            <TagChip
              key={tag.id}
              label={Copy.tag(tag.name)}
              active={activeTag === tag.name}
              onToggle={() => onTagChange(activeTag === tag.name ? undefined : tag.name)}
            />
          ))}
        </FilterGroup>
      )}
      <div className="flex items-center justify-between border-t border-gray-100 pt-3">
        <span className="text-label-s text-black">
          {resultCount !== undefined && Copy.browse.resultCount(resultCount)}
        </span>
        {hasActiveFilter && (
          <button type="button" onClick={onClear} className="text-label-s text-yellow-700 hover:underline">
            {Copy.browse.clearAll}
          </button>
        )}
      </div>
    </div>
  );
}

function FilterGroup({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
      <span className="w-14 shrink-0 text-caption text-gray-400">{label}</span>
      <div className="flex flex-wrap gap-1.5">{children}</div>
    </div>
  );
}
