import type { ReactNode } from "react";

interface EmptyStateProps {
  title: string;
  body: string;
  action?: ReactNode;
}

export function EmptyState({ title, body, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-lg border border-dashed border-gray-200 px-6 py-12 text-center">
      <p className="text-heading-s text-black">{title}</p>
      <p className="max-w-sm text-body-m text-gray-500">{body}</p>
      {action}
    </div>
  );
}
