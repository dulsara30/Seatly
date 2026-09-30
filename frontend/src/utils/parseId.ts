import type { Id } from "@/types/entities/primitives";

const DIGITS_ONLY = /^\d+$/;

/** A route segment as an id, or null if it isn't one ("abc", "1.5", "-2"). */
export function parseId(segment: string): Id | null {
  if (!DIGITS_ONLY.test(segment)) {
    return null;
  }
  const id = Number(segment);
  return Number.isSafeInteger(id) ? id : null;
}
