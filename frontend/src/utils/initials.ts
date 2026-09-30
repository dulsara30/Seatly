import { AVATAR_INITIALS_LENGTH } from "@/constants/ui";

/** "Nimali Perera" -> "NP", "Madonna" -> "MA". */
export function initialsOf(name: string): string {
  const words = name.trim().split(/\s+/).filter(Boolean);
  const letters =
    words.length >= AVATAR_INITIALS_LENGTH
      ? words.slice(0, AVATAR_INITIALS_LENGTH).map((word) => word.charAt(0))
      : [...(words[0] ?? "")].slice(0, AVATAR_INITIALS_LENGTH);
  return letters.join("").toUpperCase();
}
