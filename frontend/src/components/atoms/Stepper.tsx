import { useId, type ChangeEvent } from "react";
import { Icon } from "@/components/atoms/Icon";

interface StepperProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  min: number;
  decreaseLabel: string;
  increaseLabel: string;
}

const STEP = 1;
const BUTTON_CLASSES =
  "flex size-10 items-center justify-center text-black transition-colors duration-150 hover:bg-gray-100 " +
  "disabled:cursor-not-allowed disabled:text-gray-300 focus-visible:outline-2 focus-visible:outline-black";

// The middle is a real number input, so 30 → 100 doesn't take seventy clicks.
export function Stepper({ label, value, onChange, min, decreaseLabel, increaseLabel }: StepperProps) {
  const id = useId();

  const handleTyped = (event: ChangeEvent<HTMLInputElement>) => {
    const typed = Number.parseInt(event.target.value, 10);
    if (Number.isFinite(typed)) {
      onChange(Math.max(min, typed));
    }
  };

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={id} className="text-label-m text-black">
        {label}
      </label>
      <div className="inline-flex w-fit items-center overflow-hidden rounded-md border border-gray-200 bg-white">
        <button
          type="button"
          aria-label={decreaseLabel}
          disabled={value <= min}
          onClick={() => onChange(value - STEP)}
          className={BUTTON_CLASSES}
        >
          <Icon name="minus" />
        </button>
        <input
          id={id}
          type="number"
          inputMode="numeric"
          min={min}
          value={value}
          onChange={handleTyped}
          className="h-10 w-16 border-x border-gray-200 text-center text-label-m focus:outline-none"
        />
        <button type="button" aria-label={increaseLabel} onClick={() => onChange(value + STEP)} className={BUTTON_CLASSES}>
          <Icon name="plus" />
        </button>
      </div>
    </div>
  );
}
