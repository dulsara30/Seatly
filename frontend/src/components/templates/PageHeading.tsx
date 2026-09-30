import type { ReactNode } from "react";

interface PageHeadingProps {
  title: string;
  subtitle?: string;
  action?: ReactNode;
}

export function PageHeading({ title, subtitle, action }: PageHeadingProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div className="flex flex-col gap-1">
        <h1 className="text-heading-xl text-black">{title}</h1>
        {subtitle && <p className="text-body-m text-gray-500">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}
