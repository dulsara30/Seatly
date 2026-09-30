import { classNames } from "@/utils/classNames";

export interface SegmentOption<Value extends string> {
  value: Value;
  label: string;
}

interface SegmentedControlProps<Value extends string> {
  label: string;
  options: readonly SegmentOption<Value>[];
  value: Value;
  onChange: (value: Value) => void;
}

/**
 * The ACTIVE segment is black, not yellow. Yellow is kept for the one primary
 * action on a screen; an active toggle competing with it would dilute both.
 */
export function SegmentedControl<Value extends string>({ label, options, value, onChange }: SegmentedControlProps<Value>) {
  return (
    <div role="radiogroup" aria-label={label} className="inline-flex w-fit rounded-full bg-gray-100 p-1">
      {options.map((option) => {
        const active = option.value === value;
        return (
          <button
            key={option.value}
            type="button"
            role="radio"
            aria-checked={active}
            onClick={() => onChange(option.value)}
            className={classNames(
              "h-8 rounded-full px-4 text-label-s transition-colors duration-150",
              "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black",
              active ? "bg-black text-white" : "text-gray-500 hover:text-black",
            )}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}
