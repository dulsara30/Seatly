import { classNames } from "@/utils/classNames";

interface TagChipProps {
  // Displayed as given - pass Copy.tag(name) for a "#tech" hashtag.
  label: string;
  active?: boolean;
  onToggle?: () => void;
}

const BASE_CLASSES =
  "inline-flex h-7 items-center rounded-full px-3 text-label-s transition-colors duration-150";

export function TagChip({ label, active = false, onToggle }: TagChipProps) {
  if (!onToggle) {
    return (
      <span className={classNames(BASE_CLASSES, "bg-gray-100 text-gray-500")}>
        {label}
      </span>
    );
  }
  return (
    <button
      type="button"
      aria-pressed={active}
      onClick={onToggle}
      className={classNames(
        BASE_CLASSES,
        "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black",
        active
          ? "bg-yellow-500 text-black"
          : "border border-gray-200 bg-white text-gray-500 hover:border-black hover:text-black",
      )}>
      {label}
    </button>
  );
}
