import { classNames } from "@/utils/classNames";

export function Skeleton({ className }: { className: string }) {
  return (
    <div
      aria-hidden="true"
      className={classNames("animate-pulse rounded-md bg-gray-100", className)}
    />
  );
}
