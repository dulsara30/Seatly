import { classNames } from "@/utils/classNames";

/** A loading placeholder in the shape of what's coming - sized by the caller. */
export function Skeleton({ className }: { className: string }) {
  return (
    <div
      aria-hidden="true"
      className={classNames("animate-pulse rounded-md bg-gray-100", className)}
    />
  );
}
