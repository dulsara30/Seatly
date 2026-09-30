import Link from "next/link";
import type { ButtonHTMLAttributes, ComponentProps } from "react";
import { classNames } from "@/utils/classNames";

export type ButtonVariant =
  | "primary"
  | "accent"
  | "secondary"
  | "ghost"
  | "danger";
export type ButtonSize = "medium" | "small";

/**
 * primary (black) is the default emphasis. accent (yellow) is reserved for
 * the ONE main call to action on a screen - reserve a seat, publish, create.
 * Two accent buttons on one screen means one of them is wrong.
 */
const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: "bg-black text-white hover:bg-black/85",
  accent: "bg-yellow-500 text-black hover:bg-yellow-600",
  secondary: "border border-gray-200 bg-white text-black hover:border-black",
  ghost: "text-black hover:bg-gray-100",
  danger: "bg-red-600 text-white hover:bg-red-600/90",
};

const SIZE_CLASSES: Record<ButtonSize, string> = {
  medium: "h-10 px-4 text-label-m",
  small: "h-8 px-3 text-label-s",
};

const BASE_CLASSES =
  "inline-flex shrink-0 items-center justify-center gap-2 rounded-md font-semibold whitespace-nowrap " +
  "transition-colors duration-150 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-black";

const DISABLED_CLASSES =
  "disabled:cursor-not-allowed disabled:border-transparent disabled:bg-gray-200 disabled:text-gray-400";

interface StyleProps {
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
}

function buttonClasses(
  { variant = "primary", size = "medium", fullWidth = false }: StyleProps,
  extra?: string,
) {
  return classNames(
    BASE_CLASSES,
    VARIANT_CLASSES[variant],
    SIZE_CLASSES[size],
    fullWidth && "w-full",
    extra,
  );
}

type ButtonProps = StyleProps & ButtonHTMLAttributes<HTMLButtonElement>;

export function Button({
  variant,
  size,
  fullWidth,
  className,
  type = "button",
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      className={buttonClasses(
        { variant, size, fullWidth },
        classNames(DISABLED_CLASSES, className),
      )}
      {...props}
    />
  );
}

type LinkButtonProps = StyleProps & ComponentProps<typeof Link>;

/** Looks like a Button, behaves like a link - for actions that are really navigation. */
export function LinkButton({
  variant,
  size,
  fullWidth,
  className,
  ...props
}: LinkButtonProps) {
  return (
    <Link
      className={buttonClasses({ variant, size, fullWidth }, className)}
      {...props}
    />
  );
}
