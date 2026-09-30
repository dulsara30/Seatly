import { classNames } from "@/utils/classNames";
import { initialsOf } from "@/utils/initials";

export type AvatarSize = "small" | "medium" | "large";

const SIZE_CLASSES: Record<AvatarSize, string> = {
  small: "size-7 text-overline",
  medium: "size-9 text-caption",
  large: "size-12 text-label-m",
};

export function Avatar({ name, size = "medium" }: { name: string; size?: AvatarSize }) {
  return (
    <span
      role="img"
      aria-label={name}
      title={name}
      className={classNames(
        "inline-flex shrink-0 items-center justify-center rounded-full bg-black font-semibold text-white",
        SIZE_CLASSES[size],
      )}
    >
      {initialsOf(name)}
    </span>
  );
}
