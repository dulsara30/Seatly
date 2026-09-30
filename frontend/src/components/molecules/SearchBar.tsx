import { useId } from "react";
import { Icon } from "@/components/atoms/Icon";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  label: string;
  placeholder: string;
}

// Debouncing is the caller's job, so the input never lags.
export function SearchBar({ value, onChange, label, placeholder }: SearchBarProps) {
  const id = useId();
  return (
    <div className="relative w-full">
      <label htmlFor={id} className="sr-only">
        {label}
      </label>
      <span className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-gray-400">
        <Icon name="search" />
      </span>
      <input
        id={id}
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="h-11 w-full rounded-md border border-gray-200 bg-white pr-3 pl-10 text-body-m placeholder:text-gray-400 focus:border-black focus:outline-none"
      />
    </div>
  );
}
