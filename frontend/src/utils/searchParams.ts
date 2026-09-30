/** A query param as a single string - Next gives string[] when a key repeats; only the first counts. */
export function firstParam(
  value: string | string[] | undefined,
): string | undefined {
  return Array.isArray(value) ? value[0] : value;
}
