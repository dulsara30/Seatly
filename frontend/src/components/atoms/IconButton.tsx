import type { ButtonHTMLAttributes } from "react";
import { Icon, type IconName } from "@/components/atoms/Icon";
import { classNames } from "@/utils/classNames";

interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  icon: IconName;
  // Required: an icon alone says nothing to a screen reader.
  label: string;
  variant?: "outline" | "ghost";
}

export function IconButton({ icon, label, variant = "outline", className, type = "button", ...props }: IconButtonProps) {
  return (
    <button
      type={type}
      aria-label={label}
      title={label}
      className={classNames(
        "inline-flex size-10 shrink-0 items-center justify-center rounded-md text-black transition-colors duration-150",
        "focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black",
        variant === "outline" ? "border border-gray-200 bg-white hover:border-black" : "hover:bg-gray-100",
        className,
      )}
      {...props}
    >
      <Icon name={icon} />
    </button>
  );
}
