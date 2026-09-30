import { Copy } from "@/constants/copy";

/**
 * The wordmark: "seatly" in black with a yellow full stop. The dot is brand
 * punctuation - text colour, not a filled surface - so it doesn't count
 * against the three places filled yellow is allowed.
 */
export function Logo() {
  return (
    <span className="text-heading-m text-black">
      {Copy.brand.name}
      <span className="text-yellow-500">{Copy.brand.mark}</span>
    </span>
  );
}
