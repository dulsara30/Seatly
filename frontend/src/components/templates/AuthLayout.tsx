import Link from "next/link";
import type { ReactNode } from "react";

interface AuthLayoutProps {
  title: string;
  subtitle: string;
  /** The "no account? / have an account?" line under the card. */
  footer: ReactNode;
  children: ReactNode;
}

/** A single centred card - no distractions on the way in. */
export function AuthLayout({
  title,
  subtitle,
  footer,
  children,
}: AuthLayoutProps) {
  return (
    <div className="mx-auto flex w-full max-w-md flex-col gap-6 px-4 py-12 sm:py-20">
      <div className="flex flex-col gap-2">
        <h1 className="text-heading-xl">{title}</h1>
        <p className="text-body-m text-gray-500">{subtitle}</p>
      </div>
      <div className="rounded-lg border border-gray-200 bg-white p-6">
        {children}
      </div>
      <p className="text-body-s text-gray-500">{footer}</p>
    </div>
  );
}

export function AuthFooterLink({
  prompt,
  href,
  label,
}: {
  prompt: string;
  href: string;
  label: string;
}) {
  return (
    <>
      {prompt}{" "}
      <Link
        href={href}
        className="font-semibold text-black underline-offset-4 hover:underline">
        {label}
      </Link>
    </>
  );
}
