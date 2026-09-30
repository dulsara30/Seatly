/** Joins class names, skipping the falsy ones - for conditional classes without template-string noise. */
export function classNames(
  ...classes: Array<string | false | null | undefined>
): string {
  return classes.filter(Boolean).join(" ");
}
