import { useEffect, useState } from "react";

/**
 * The value, but only once it has stopped changing for `delayMs`. Feeding the
 * debounced value into a query key means a search fires once per pause in
 * typing, not once per keystroke - and TanStack cancels nothing, because the
 * in-between values never became queries at all.
 */
export function useDebouncedValue<Value>(value: Value, delayMs: number): Value {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return debounced;
}
