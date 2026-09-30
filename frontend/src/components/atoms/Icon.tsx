import type { SVGProps } from "react";

// Stroke paths on a 24×24 grid, drawn in currentColor so an icon takes the
// text colour of wherever it sits.
const PATHS = {
  lock: "M7 11V8a5 5 0 0 1 10 0v3M6 11h12v9H6z",
  more: "M5 12h.01M12 12h.01M19 12h.01",
  minus: "M5 12h14",
  plus: "M12 5v14M5 12h14",
  arrowLeft: "M19 12H5M11 18l-6-6 6-6",
  search: "M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14zM20 20l-4-4",
  check: "M5 12l5 5 9-10",
  close: "M6 6l12 12M18 6L6 18",
} as const;

export type IconName = keyof typeof PATHS;

interface IconProps extends Omit<SVGProps<SVGSVGElement>, "name"> {
  name: IconName;
}

/** Decorative by default (aria-hidden) - the control around it carries the label. */
export function Icon({ name, className = "size-4", ...props }: IconProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className}
      {...props}>
      <path d={PATHS[name]} />
    </svg>
  );
}
