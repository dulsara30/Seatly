import { Copy } from "@/constants/copy";
import { classNames } from "@/utils/classNames";

export type BadgeVariant =
  | "UPCOMING"
  | "CANCELLED"
  | "COMPLETED"
  | "CONFIRMED"
  | "WAITLISTED"
  | "FULL"
  | "GOING"
  | "ONLINE"
  | "PHYSICAL";

const VARIANT_CLASSES: Record<BadgeVariant, string> = {
  UPCOMING: "bg-yellow-50 text-yellow-700",
  CANCELLED: "bg-red-50 text-red-600",
  COMPLETED: "bg-gray-100 text-gray-500",
  CONFIRMED: "bg-green-50 text-green-700",
  WAITLISTED: "bg-gray-100 text-black",
  FULL: "bg-black text-white",
  GOING: "bg-green-50 text-green-700",
  ONLINE: "border border-gray-200 bg-white text-gray-500",
  PHYSICAL: "border border-gray-200 bg-white text-gray-500",
};

const LABELS: Record<BadgeVariant, string> = {
  UPCOMING: Copy.badge.UPCOMING,
  CANCELLED: Copy.badge.CANCELLED,
  COMPLETED: Copy.badge.COMPLETED,
  CONFIRMED: Copy.badge.CONFIRMED,
  WAITLISTED: Copy.badge.WAITLISTED,
  FULL: Copy.badge.full,
  GOING: Copy.badge.going,
  ONLINE: Copy.badge.ONLINE,
  PHYSICAL: Copy.badge.PHYSICAL,
};

export function Badge({ variant }: { variant: BadgeVariant }) {
  return (
    <span
      className={classNames(
        "inline-flex items-center rounded-full px-2 py-0.5 text-overline uppercase",
        VARIANT_CLASSES[variant],
      )}>
      {LABELS[variant]}
    </span>
  );
}
