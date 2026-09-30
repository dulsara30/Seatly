import type { Id } from "@/types/entities/primitives";

const DIGITS_ONLY = /^\d+$/;

export function parseId(segment: string): Id | null {
  if (!DIGITS_ONLY.test(segment)) {
    return null;
  }
  const id = Number(segment);
  return Number.isSafeInteger(id) ? id : null;
}
